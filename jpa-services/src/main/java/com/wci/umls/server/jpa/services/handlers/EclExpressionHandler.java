package com.wci.umls.server.jpa.services.handlers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.RecognitionException;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;

import com.google.common.collect.Lists;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.helpers.SearchResult;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.jpa.AbstractConfigurable;
import com.wci.umls.server.jpa.helpers.SearchResultJpa;
import com.wci.umls.server.jpa.helpers.SearchResultListJpa;
import com.wci.umls.server.jpa.services.handlers.expr.EclConceptFieldNames;
import com.wci.umls.server.jpa.services.handlers.expr.ExpressionConstraintToLuceneConverter;
import com.wci.umls.server.services.handlers.ExpressionHandler;

/**
 * The Class EclExpressionHandler.
 */
public class EclExpressionHandler extends AbstractConfigurable
    implements ExpressionHandler {

  /** The terminology. */
  private String terminology = null;

  /** The version. */
  private String version = null;

  /** The maximum number of results returned */
  private Integer maxResults = Integer.MAX_VALUE;

  /** The converter. */
  private ExpressionConstraintToLuceneConverter converter = null;

  /** The query parser. */
  private QueryParser queryParser = null;

  /** The index searcher. */
  private IndexSearcher indexSearcher = null;

  /** The internal function pattern map. */
  private final Map<ExpressionConstraintToLuceneConverter.InternalFunction, Pattern> internalFunctionPatternMap =
      new TreeMap<>();

  /**
   * Instantiates a new ecl expression handler.
   *
   * @param terminology the terminology
   * @param version the version
   * @throws Exception the exception
   */
  public EclExpressionHandler(String terminology, String version)
      throws Exception {

    // instantiate the index searcher
    String indexDir =
        ConfigUtility.getExpressionIndexDirectoryName(terminology, version);
    Directory dirFile = new NIOFSDirectory(Paths.get(indexDir));
    indexSearcher = new IndexSearcher(DirectoryReader.open(dirFile));

    // instantiate the modified SQS lucene converter
    converter = new ExpressionConstraintToLuceneConverter();

    // instantiate the query parser
    queryParser =
        new QueryParser(EclConceptFieldNames.ID, new StandardAnalyzer());
    queryParser.setAllowLeadingWildcard(true);

    // compute the internal functions from the modified SQS lucene converter
    // NOTE: Kept out of Converter to minimize SQS code modification
    for (final ExpressionConstraintToLuceneConverter.InternalFunction internalFunction : ExpressionConstraintToLuceneConverter.InternalFunction
        .values()) {
      internalFunctionPatternMap.put(internalFunction,
          Pattern.compile(".*(" + internalFunction + "\\(([^\\)]+)\\)).*"));
    }
  }

  @Override
  public String getName() {
    return "Expression Constraint Language Lucene Query Handler";
  }

  @Override
  public void setProperties(Properties p) throws Exception {
    // Properties (from Configurable) not used

  }

  @Override
  public String parse(String expr) {
    return converter.parse(expr);
  }

  @Override
  public Integer getCount(String expr) throws Exception {
    if (terminology == null || version == null) {
      throw new Exception(
          "Terminology and version required before resolving ECL query");
    }
    return 0;
  }

  @Override
  public SearchResultList resolve(String ecQuery) throws Exception {

    // the results list to return (for ecl, concepts)
    final SearchResultList results = new SearchResultListJpa();

    if (ecQuery != null && !ecQuery.isEmpty()) {
      String luceneQuery;
      try {
        luceneQuery = parse(ecQuery);
        Logger.getLogger(getClass())
            .info("EC Query: " + ecQuery + " -> parsed: " + luceneQuery);

      } catch (RecognitionException e) {
        throw new LocalException(
            "Expression cannot be parsed, must reference an id", e);
      } catch (UnsupportedOperationException e) {
        throw new LocalException(e.getMessage(), e);
      }
      try {
        for (final ExpressionConstraintToLuceneConverter.InternalFunction internalFunction : internalFunctionPatternMap
            .keySet()) {
          while (luceneQuery.contains(internalFunction.name())) {
            luceneQuery =
                processInternalFunction(luceneQuery, internalFunction);
          }
        }
      } catch (IOException e) {
        throw new InternalError("Error preparing internal search query.", e);
      }
      try {

        // parse the revised query after internal function expansion
        final Query query = queryParser.parse(luceneQuery);

        // execute the revised query
        final TopDocs topDocs =
            indexSearcher.search(query, maxResults, Sort.INDEXORDER);
        final ScoreDoc[] scoreDocs = topDocs.scoreDocs;

        // construct the search results
        results.setTotalCount(topDocs.totalHits);

        for (int a = 0; a < scoreDocs.length; a++) {
          ScoreDoc scoreDoc = scoreDocs[a];
          Document conceptDoc = getDocument(scoreDoc);
          SearchResult result = new SearchResultJpa();
          result.setId(
              Long.parseLong(conceptDoc.get(EclConceptFieldNames.INTERNAL_ID)));
          result.setTerminology(terminology);
          result.setVersion(version);
          result.setTerminologyId(conceptDoc.get(EclConceptFieldNames.ID));
          results.getObjects().add(result);
        }
        Logger.getLogger(getClass())
            .info("  results = " + results.size() + ", query = " + luceneQuery);
        return results;
      } catch (ParseException e) {
        throw new InternalError("Error parsing internal search query.", e);
      }
    }
    return results;
  }

  /**
   * Process internal function.
   *
   * @param luceneQuery the lucene query
   * @param internalFunction the internal function
   * @return the string
   * @throws Exception
   */
  private String processInternalFunction(String luceneQuery,
    ExpressionConstraintToLuceneConverter.InternalFunction internalFunction)
    throws Exception {

    // apply the pattern matcher
    final Matcher matcher =
        internalFunctionPatternMap.get(internalFunction).matcher(luceneQuery);

    // if no match, log and throw error
    if (!matcher.matches() || matcher.groupCount() != 2) {
      final String message = "Failed to extract the id from the function "
          + internalFunction + " in internal query '" + luceneQuery + "'";
      Logger.getLogger(getClass()).error(message);
      throw new IllegalStateException(message);
    }

    // extract the terminology id
    final String terminologyId = matcher.group(2);
    List<String> conceptRelatives;

    // if ancestor function
    if (internalFunction.isAncestorType()) {

      // get the list of ancestors for this concept
      conceptRelatives = Lists.newArrayList(getConceptDocument(terminologyId)
          .getValues(EclConceptFieldNames.ANCESTOR));
    }

    // if not ancestor function
    else {

      // get the concepts for which this concept is an ancestor
      final TopDocs topDocs = indexSearcher.search(
          new TermQuery(new Term(EclConceptFieldNames.ANCESTOR, terminologyId)),
          maxResults);

      conceptRelatives = new ArrayList<>();
      for (final ScoreDoc scoreDoc : topDocs.scoreDocs) {
        conceptRelatives
            .add(getDocument(scoreDoc).get(EclConceptFieldNames.ID));
      }
    }
    if (internalFunction.isIncludeSelf()) {
      conceptRelatives.add(terminologyId);
    }

    String newLuceneQuery =
        luceneQuery.replace(matcher.group(1), buildOptionsList(conceptRelatives,
            !internalFunction.isAttributeType()));

    return newLuceneQuery;
  }

  /**
   * Build options list.
   *
   * @param conceptRelatives the concept relatives
   * @param includeIdFieldName the include id field name
   * @return the string
   */
  @SuppressWarnings("static-method")
  private String buildOptionsList(List<String> conceptRelatives,
    boolean includeIdFieldName) {
    final StringBuilder relativesIdBuilder = new StringBuilder();
    if (!conceptRelatives.isEmpty()) {
      relativesIdBuilder.append("(");
      boolean first = true;
      for (final String conceptRelative : conceptRelatives) {
        if (first) {
          first = false;
        } else {
          relativesIdBuilder.append(" OR ");
        }
        if (includeIdFieldName) {
          relativesIdBuilder.append(EclConceptFieldNames.ID).append(":");
        }
        relativesIdBuilder.append(conceptRelative);
      }
      relativesIdBuilder.append(")");
    }
    return relativesIdBuilder.toString();
  }

  /**
   * Gets the concept document.
   *
   * @param conceptId the concept id
   * @return the concept document
   * @throws Exception the exception
   */
  private Document getConceptDocument(String conceptId) throws Exception {

    // get the top document (i.e. restrict to 1 result)
    final TopDocs docs = indexSearcher
        .search(new TermQuery(new Term(EclConceptFieldNames.ID, conceptId)), 1);
    if (docs.totalHits < 1) {
      throw new Exception(conceptId + " has no index document");
    }
    return indexSearcher.doc(docs.scoreDocs[0].doc);
  }

  /**
   * Gets the full document from the scored document
   *
   * @param scoreDoc the score doc
   * @return the document
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private Document getDocument(ScoreDoc scoreDoc) throws IOException {
    return indexSearcher.doc(scoreDoc.doc);
  }

}
