/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.handlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.apache.lucene.search.spell.LuceneLevenshteinDistance;
import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.hibernate.search.jpa.FullTextQuery;

import com.wci.umls.server.helpers.FieldedStringTokenizer;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.PfscParameter;
import com.wci.umls.server.jpa.services.helper.IndexUtility;
import com.wci.umls.server.model.content.AtomClass;
import com.wci.umls.server.services.handlers.SearchHandler;

/**
 * Default implementation of {@link SearchHandler}. This provides an algorithm
 * to aide in lucene searches.
 */
public class DefaultSearchHandler implements SearchHandler {

  /** The acronym expansion map. */
  private Map<String, Set<String>> acronymExpansionMap = new HashMap<>();

  /** The spell checker. */
  private SpellChecker spellChecker = null;

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {

    // Initialize acronyms map
    if (p.containsKey("acronymsFile")) {
      BufferedReader in =
          new BufferedReader(new FileReader(new File(
              p.getProperty("acronymsFile"))));
      String line;
      while ((line = in.readLine()) != null) {
        String[] tokens = FieldedStringTokenizer.split(line, "\t");
        if (!acronymExpansionMap.containsKey(tokens[0])) {
          acronymExpansionMap.put(tokens[0], new HashSet<String>(2));
        }
        acronymExpansionMap.get(tokens[0]).add(tokens[1]);
      }
      in.close();
    } else {
      throw new Exception("Required property acronymsFile not present.");
    }

    // Initialize spell checker
    if (p.containsKey("spellingFile") && p.containsKey("spellingIndex")) {
      // expect properties to have "spellingFile" and "spellingIndex"
      File dir = new File(p.getProperty("spellingIndex"));
      Directory directory = FSDirectory.open(dir);
      spellChecker =
          new SpellChecker(directory, new LuceneLevenshteinDistance());
      IndexWriterConfig indexWriterConfig =
          new IndexWriterConfig(Version.LATEST, new WhitespaceAnalyzer());
      spellChecker.indexDictionary(
          new PlainTextDictionary(new File(p.getProperty("spellingFile"))),
          indexWriterConfig, false);

    } else {
      throw new Exception(
          "Required property spellingFile or spellingIndex not present.");
    }
  }

  /* see superclass */
  @Override
  public <T extends AtomClass> List<T> getQueryResults(String terminology,
    String version, String branch, String query, Class<?> fieldNamesKey,
    Class<T> clazz, PfsParameter pfs, int[] totalCt, EntityManager manager)
    throws Exception {

    // Build an escaped form of the query with wrapped quotes removed
    String escapedQuery = query;
    if (query.startsWith("\"") && query.endsWith("\"")) {
      escapedQuery = escapedQuery.substring(1);
      escapedQuery = escapedQuery.substring(0, query.length() - 1);
    }
    escapedQuery = "\"" + QueryParserBase.escape(escapedQuery) + "\"";

    // Build a combined query with an OR between query typed and exact match
    String combinedQuery = null;
    // For a fielded query search, simply perform the search as written
    // no need for modifications
    if (query.contains(":")) {
      combinedQuery = query;
    } else {
      combinedQuery =
          (query.isEmpty() ? "" : query + " OR ") + "atoms.nameSort:"
              + escapedQuery + "^20.0";

      // create an exact expansion entry. i.e. if the search term exactly
      // matches something in the acronyms file, then use additional "OR"
      // clauses
      if (acronymExpansionMap.containsKey(query)) {
        for (String expansion : acronymExpansionMap.get(query)) {
          combinedQuery += " OR atoms.nameSort:\"" + expansion + "\"" + "^20.0";
        }
      }
    }

    // Check for spelling mistakes (if not a fielded search)
    if (!query.contains(":")) {
      boolean flag = false;
      StringBuilder correctedQuery = new StringBuilder();
      for (String token : FieldedStringTokenizer.split(query,
          " \t-({[)}]_!@#%&*\\:;\"',.?/~+=|<>$`^")) {
        if (correctedQuery.length() != 0) {
          correctedQuery.append(" ");
        }
        if (token.length() == 0) {
          continue;
        }
        if (spellChecker.exist(token.toLowerCase())) {
          correctedQuery.append(token);
        } else {
          String[] suggestions =
              spellChecker.suggestSimilar(token.toLowerCase(), 5, .8f);
          flag = suggestions.length > 0;
          correctedQuery.append(FieldedStringTokenizer.join(suggestions, " "));
        }
      }
      if (flag) {
        combinedQuery +=
            " OR atoms.nameSort:\"" + correctedQuery + "\"" + "^10.0";
      }
    }

    // Build query for pfs conditions
    StringBuilder pfsQuery = new StringBuilder();
    if (terminology != null && !terminology.equals("") && version != null
        && !version.equals("")) {
      pfsQuery.append(" AND terminology:" + terminology + " AND version:"
          + version);
    }
    if (pfs != null) {
      if (pfs.getActiveOnly()) {
        pfsQuery.append(" AND obsolete:false");
      }
      if (pfs.getInactiveOnly()) {
        pfsQuery.append(" AND obsolete:true");
      }
      if (pfs.getQueryRestriction() != null
          && !pfs.getQueryRestriction().isEmpty()) {
        pfsQuery.append(" AND " + pfs.getQueryRestriction());
      }
    }

    // Apply pfs restrictions to query
    String finalQuery = "(" + combinedQuery + ")" + pfsQuery;
    FullTextQuery fullTextQuery = null;
    try {
      System.out.println("query = " + finalQuery);
      fullTextQuery =
          IndexUtility.applyPfsToLuceneQuery(clazz, fieldNamesKey, finalQuery,
              pfs, manager);
    } catch (ParseException e) {
      // If there's a parse exception, try the literal query
      System.out.println("  query = " + escapedQuery + pfsQuery);
      fullTextQuery =
          IndexUtility.applyPfsToLuceneQuery(clazz, fieldNamesKey, escapedQuery
              + pfsQuery, pfs, manager);
    }

    // Apply paging and sorting parameters - if no search criteria
    // This is needed for the combined search with "search criteria"
    if (pfs instanceof PfscParameter
        && ((PfscParameter) pfs).getSearchCriteria().isEmpty()) {
      // Get result size if we know it.
      totalCt[0] = fullTextQuery.getResultSize();
    } else {
      // If with search criteria, save paging
      fullTextQuery.setFirstResult(0);
      fullTextQuery.setMaxResults(Integer.MAX_VALUE);
      totalCt[0] = fullTextQuery.getResultSize();
    }

    // If at this point there are zero results,
    // Run the query through acronym expansion
    if (totalCt[0] == 0) {
      // use wordInd tokenization
      String[] tokens =
          FieldedStringTokenizer.split(query,
              " \t-({[)}]_!@#%&*\\:;\"',.?/~+=|<>$`^");
      StringBuilder newQuery = new StringBuilder();
      newQuery.append("(");
      boolean found = false;
      for (String token : tokens) {
        if (newQuery.length() != 0) {
          newQuery.append(" ");
        }
        // replace with acronym or keep the same
        if (acronymExpansionMap.containsKey(token.toUpperCase())) {
          found = true;
          newQuery.append(FieldedStringTokenizer.join(new ArrayList<>(
              acronymExpansionMap.get(token)), " "));
        } else {
          newQuery.append(token);
        }
      }
      newQuery.append(")");
      // Try the query again (if at least one expansion was found)
      if (found) {
        System.out.println("  query = " + newQuery.toString() + pfsQuery);
        fullTextQuery =
            IndexUtility.applyPfsToLuceneQuery(clazz, fieldNamesKey,
                newQuery.toString() + pfsQuery, pfs, manager);
        totalCt[0] = fullTextQuery.getResultSize();
      }
    }

    // If at this point there are zero results,
    // Run the query through spelling correction
    if (totalCt[0] == 0) {
      // use wordInd tokenization
      String[] tokens =
          FieldedStringTokenizer.split(query,
              " \t-({[)}]_!@#%&*\\:;\"',.?/~+=|<>$`^");
      StringBuilder newQuery = new StringBuilder();
      newQuery.append("(");
      boolean found = false;
      for (String token : tokens) {
        if (newQuery.length() != 0) {
          newQuery.append(" ");
        }
        if (spellChecker.exist(token.toLowerCase())) {
          newQuery.append(token);
        } else if (!token.isEmpty()) {
          String[] suggestions =
              spellChecker.suggestSimilar(token.toLowerCase(), 5, .8f);
          found = suggestions.length > 0;
          newQuery.append(FieldedStringTokenizer.join(suggestions, " "));
        }
      }
      newQuery.append(")");

      // Try the query again (if replacement found)
      if (found) {
        System.out.println("  query = " + newQuery.toString() + pfsQuery);
        fullTextQuery =
            IndexUtility.applyPfsToLuceneQuery(clazz, fieldNamesKey,
                newQuery.toString() + pfsQuery, pfs, manager);
        totalCt[0] = fullTextQuery.getResultSize();
      }
    }

    // TODO: if still zero, do wildcard search at the end of each term of the
    // original query
    // e.g. a* b* c*
    if (totalCt[0] == 0) {
      // use wordInd tokenization
      String[] tokens =
          FieldedStringTokenizer.split(query,
              " \t-({[)}]_!@#%&*\\:;\"',.?/~+=|<>$`^");
      StringBuilder newQuery = new StringBuilder();
      newQuery.append("(");
      for (String token : tokens) {
        if (newQuery.length() != 0) {
          newQuery.append(" ");
        }
        if (token.length() > 0) {
          newQuery.append(token).append("*");
        }
      }
      newQuery.append(")");
      // Try the query again
      System.out.println("  query = " + newQuery.toString() + pfsQuery);
      fullTextQuery =
          IndexUtility.applyPfsToLuceneQuery(clazz, fieldNamesKey,
              newQuery.toString() + pfsQuery, pfs, manager);
      totalCt[0] = fullTextQuery.getResultSize();

    }

    // execute the query
    @SuppressWarnings("unchecked")
    List<T> classes = fullTextQuery.getResultList();

    // Use this code to see the actual score values
    // fullTextQuery.setProjection(FullTextQuery.SCORE, FullTextQuery.ID);
    // List<T> classes = new ArrayList<>();
    // List<Object[]> obj = fullTextQuery.getResultList();
    // for (Object[] objArray : obj) {
    // Object score = objArray[0];
    // long id = (Long)objArray[1];
    // T t = getComponent( id, clazz);
    // classes.add(t);
    // Logger.getLogger(getClass()).info(t.getName() + " = " + score);
    // }

    return classes;

  }
}
