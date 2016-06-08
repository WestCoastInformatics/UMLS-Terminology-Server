/*
 *    Copyright 2016 West Coast Informatics, LLC
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.hibernate.search.engine.ProjectionConstants;
import org.hibernate.search.jpa.FullTextQuery;

import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.FieldedStringTokenizer;
import com.wci.umls.server.helpers.HasId;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.jpa.content.AbstractAtomClass;
import com.wci.umls.server.jpa.content.AbstractComponent;
import com.wci.umls.server.jpa.services.helper.IndexUtility;
import com.wci.umls.server.services.handlers.SearchHandler;

/**
 * Default implementation of {@link SearchHandler}. This provides an algorithm
 * to aide in lucene searches of atom classes (Concept, Descriptor, Code)
 */
public class AtomClassSearchHandler implements SearchHandler {

  /** The acronym expansion map. */
  private Map<String, Set<String>> acronymExpansionMap = new HashMap<>();

  /** The spell checker. */
  private SpellChecker spellChecker = null;

  /** The score map. */
  private Map<Long, Float> scoreMap = new HashMap<>();

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {

    // Initialize acronyms map
    if (p.containsKey("acronymsFile")) {
      final BufferedReader in =
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
      final File dir = new File(p.getProperty("spellingIndex"));
      final Directory directory = FSDirectory.open(dir);
      spellChecker =
          new SpellChecker(directory, new LuceneLevenshteinDistance());
      final IndexWriterConfig indexWriterConfig =
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
  public <T extends HasId> List<T> getQueryResults(String terminology,
    String version, String branch, String query, String literalField,
    Class<?> fieldNamesKey, Class<T> clazz, PfsParameter pfs, int[] totalCt,
    EntityManager manager) throws Exception {

    // check assumption: class queried must extend AbstractAtomClass
    if (!AbstractAtomClass.class.isAssignableFrom(clazz)) {
      throw new Exception(
          "AtomClassSearchHandler can only be invoked on AbstractAtomClass objects");
    }

    // if the literal field specified is a sort field, also search normalized
    // field
    String normalizedField = null;
    if (literalField != null && literalField.endsWith("Sort")) {
      normalizedField =
          literalField.substring(0, literalField.length() - 4) + "Norm";
    }

    // Build a quote-stripped query for use in literal and norm fields
    String literalQuery = query == null ? "" : query;

    if (literalQuery.startsWith("\"") && query.endsWith("\"")) {
      literalQuery = query.substring(1, query.length() - 1);
    }

    // Build an escaped, quoted query for use in exact searching
    String escapedQuery = "\"" + QueryParserBase.escape(literalQuery) + "\"";

    // A slash character indicats a regex in lucene, fix that
    final String fixedQuery = query == null ? "" : query.replaceAll("\\/", " ");

    // Construct the universal terminology conditions
    StringBuilder terminologyClause = new StringBuilder();
    if (terminology != null && !terminology.equals("") && version != null
        && !version.equals("")) {
      terminologyClause.append(" AND terminology:" + terminology
          + " AND version:" + version);
    }

    // Build a combined query with an OR between parsed tokens and exact match
    String parsedQuery = "";

    if (!fixedQuery.isEmpty()) {

      // split original query on white space and quoted material
      // NOTE: Preserve the original quotation marks for use in name search
      final List<String> tokens = new ArrayList<String>();
      final Pattern regex = Pattern.compile("[^\\s\"]+|\"[^\"]*\"");
      final Matcher regexMatcher = regex.matcher(fixedQuery);
      while (regexMatcher.find()) {
        tokens.add(regexMatcher.group());
      }

      // add each term (in quotes) to the name field
      if (tokens.size() > 0) {
        for (final String token : tokens) {
          parsedQuery += " OR atoms.name:" + token;
        }
      }

      // search the normalized and literal fields with the quoted literal
      // (unescaped) query
      if (normalizedField != null) {
        parsedQuery +=
            " OR " + normalizedField + ":\""
                + ConfigUtility.normalize(literalQuery) + "\"^5.0";
      }
      if (literalField != null) {
        parsedQuery += " OR " + literalField + ":\"" + literalQuery + "\"^5.0";
      }

      // check for a single term containing numbers, which may be an id
      if (literalQuery.matches(".*[0-9]+.*")
          && literalQuery.split("\\s").length == 1) {
        parsedQuery += " OR (terminologyId:" + literalQuery;
        parsedQuery += " OR atoms.terminologyId:" + literalQuery;
        parsedQuery += " OR atoms.codeId:" + literalQuery;
        parsedQuery += " OR atoms.conceptId:" + literalQuery;
        parsedQuery += " OR atoms.descriptorId:" + literalQuery + ")^5.0";
      }

      // check for exact acronym expansion
      if (acronymExpansionMap.containsKey(fixedQuery)) {
        for (final String expansion : acronymExpansionMap.get(fixedQuery)) {
          if (normalizedField != null) {
            parsedQuery +=
                " OR " + normalizedField + ":\""
                    + ConfigUtility.normalize(expansion) + "\"^5.0";
          }
          if (literalField != null) {
            parsedQuery += " OR " + literalField + ":\"" + expansion + "\"^5.0";
          }
        }
      }

      // Check for spelling mistakes (if not a fielded search)
      if (!fixedQuery.contains(":") && !fixedQuery.isEmpty()) {
        boolean flag = false;
        StringBuilder correctedQuery = new StringBuilder();
        for (final String token : FieldedStringTokenizer.split(fixedQuery,
            " \t-({[)}]_!@#%&*\\:;\"',.?/~+=|<>$`^")) {
          if (token.length() == 0) {
            continue;
          }
          if (spellChecker.exist(token.toLowerCase())) {
            if (correctedQuery.length() != 0) {
              correctedQuery.append(" ");
            }
            correctedQuery.append(token);
          } else {
            String[] suggestions =
                spellChecker.suggestSimilar(token.toLowerCase(), 5, .8f);
            if (suggestions.length > 0) {
              flag = true;
              if (correctedQuery.length() != 0) {
                correctedQuery.append(" ");
              }
              correctedQuery.append(FieldedStringTokenizer.join(suggestions,
                  " "));
            }
          }
        }
        if (flag) {
          // add name norm and name sort with appropriate weightings
          if (normalizedField != null) {
            parsedQuery +=
                " OR " + normalizedField + ":\"" + correctedQuery.toString()
                    + "\"^5.0";
          }
          if (literalField != null) {
            parsedQuery +=
                " OR " + literalField + ":\"" + correctedQuery.toString()
                    + "\"^5.0";
          }
        }
      }

      // check for leading OR in parsed query
      if (parsedQuery.startsWith(" OR ")) {
        parsedQuery = parsedQuery.substring(4);
      }
    }

    // Assemble final parsed query
    StringBuilder finalQuery = new StringBuilder();
    if (fixedQuery.isEmpty()) {
      // Just use PFS and skip the leading "AND"
      finalQuery.append(terminologyClause.substring(5));
    } else if (parsedQuery.contains(" OR ")) {
      // Use parens
      finalQuery.append("(").append(parsedQuery).append(")")
          .append(terminologyClause);
    } else {
      // Don't use parens
      finalQuery.append(parsedQuery).append(terminologyClause);

    }

    // Assemble the fielded query (if appropriate)
    StringBuilder fieldedQuery = null;
    if (fixedQuery.contains(":")) {
      fieldedQuery = new StringBuilder();
      fieldedQuery.append("(").append(fixedQuery).append(")")
          .append(terminologyClause);
    }

    // Construct the full text query and perform the search
    FullTextQuery fullTextQuery = null;

    // if fielded, try literal query first
    try {
      if (fieldedQuery != null) {
        fullTextQuery =
            IndexUtility.applyPfsToLuceneQuery(clazz, fieldNamesKey,
                fieldedQuery.toString(), pfs, manager);
      }
    } catch (ParseException | IllegalArgumentException e) {
      e.printStackTrace();
      fullTextQuery = null;
    }

    // if not a fielded search or fielded search returned no results
    if (fullTextQuery == null || fullTextQuery.getResultSize() == 0) {

      // try the parsed query
      try {
        fullTextQuery =
            IndexUtility.applyPfsToLuceneQuery(clazz, fieldNamesKey,
                finalQuery.toString(), pfs, manager);
      }

      // If there's a parse exception, try the literal query
      catch (ParseException | IllegalArgumentException e) {
        e.printStackTrace();
        fullTextQuery =
            IndexUtility.applyPfsToLuceneQuery(clazz, fieldNamesKey,
                escapedQuery + terminologyClause, pfs, manager);
      }
    }

    // Apply paging and sorting parameters for the PFSC case
    // This is needed for the combined search with "search criteria"
    totalCt[0] = fullTextQuery.getResultSize();

    // Only look to other algorithms if this is NOT a potential fielded query
    // and the query exists
    if (fixedQuery != null && !fixedQuery.isEmpty()
        && !fixedQuery.contains(":")) {

      // If at this point there are zero results,
      // Run the query through acronym expansion
      if (totalCt[0] == 0) {
        // use wordInd tokenization
        String[] tokens =
            FieldedStringTokenizer.split(fixedQuery,
                " \t-({[)}]_!@#%&*\\:;\"',.?/~+=|<>$`^");
        StringBuilder newQuery = new StringBuilder();
        boolean found = false;
        for (final String token : tokens) {
          if (newQuery.length() != 0) {
            newQuery.append(" ");
          }
          // replace with acronym or keep the same
          if (acronymExpansionMap.containsKey(token.toUpperCase())) {
            found = true;
            List<String> tempList = new ArrayList<>();
            tempList.add("\"" + acronymExpansionMap.get(token.toUpperCase())
                + "\"");
            newQuery.append(FieldedStringTokenizer.join(tempList, " "));
          } else {
            newQuery.append(token);
          }
        }
        // Try the query again (if at least one expansion was found)
        if (found) {
          fullTextQuery =
              IndexUtility.applyPfsToLuceneQuery(clazz, fieldNamesKey,
                  newQuery.toString() + terminologyClause, pfs, manager);
          totalCt[0] = fullTextQuery.getResultSize();
        }
      }

      // If at this point there are zero results,
      // Run the query through spelling correction
      if (totalCt[0] == 0) {
        // use wordInd tokenization
        String[] tokens =
            FieldedStringTokenizer.split(fixedQuery,
                " \t-({[)}]_!@#%&*\\:;\"',.?/~+=|<>$`^");
        StringBuilder newQuery = new StringBuilder();
        newQuery.append("(");
        boolean found = false;
        for (final String token : tokens) {
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
          fullTextQuery =
              IndexUtility.applyPfsToLuceneQuery(clazz, fieldNamesKey,
                  newQuery.toString() + terminologyClause, pfs, manager);
          totalCt[0] = fullTextQuery.getResultSize();
        }
      }

      // if still zero, do wildcard search at the end of each term of the
      // original query
      // e.g. a* b* c*
      if (totalCt[0] == 0) {
        // use wordInd tokenization
        String[] tokens =
            FieldedStringTokenizer.split(fixedQuery,
                " \t-({[)}]_!@#%&*\\:;\"',.?/~+=|<>$`^");
        StringBuilder newQuery = new StringBuilder();
        newQuery.append("(");
        for (final String token : tokens) {
          if (newQuery.length() != 1) {
            newQuery.append(" OR ");
          }
          if (token.length() > 0) {
            newQuery.append(token).append("*");
          }
        }
        newQuery.append(")");
        // Try the query again
        fullTextQuery =
            IndexUtility.applyPfsToLuceneQuery(clazz, fieldNamesKey,
                newQuery.toString() + terminologyClause, pfs, manager);
        totalCt[0] = fullTextQuery.getResultSize();

      }

    }

    // Retrieve the scores for the returned objects
    fullTextQuery.setProjection(ProjectionConstants.SCORE,
        ProjectionConstants.THIS);
    final List<T> classes = new ArrayList<>();

    @SuppressWarnings("unchecked")
    final List<Object[]> results = fullTextQuery.getResultList();
    for (final Object[] result : results) {
      final Object score = result[0];
      @SuppressWarnings("unchecked")
      final T t = (T) result[1];
      classes.add(t);

      // cap the score to a maximum of 1.0
      Float normScore = Math.min(1, Float.valueOf(score.toString()));

      // bump up relevance of exact match on terminology id
      if (literalQuery != null
          && literalQuery.equals(((AbstractComponent) t).getTerminologyId())) {
        normScore = 1.0f;
      }

      // store the score for later retrieval
      scoreMap.put(t.getId(), normScore.floatValue());
    }

    return classes;

  }

  /* see superclass */
  @Override
  public String getName() {
    return "Atom Class Search Handler";
  }

  /* see superclass */
  @Override
  public Map<Long, Float> getScoreMap() {
    return scoreMap;
  }
}
