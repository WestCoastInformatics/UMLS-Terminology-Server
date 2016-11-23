/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.hibernate.search.engine.ProjectionConstants;
import org.hibernate.search.jpa.FullTextQuery;

import com.wci.umls.server.helpers.HasId;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.jpa.AbstractConfigurable;
import com.wci.umls.server.jpa.services.helper.IndexUtility;
import com.wci.umls.server.services.handlers.SearchHandler;

/**
 * Default implementation of {@link SearchHandler}. This provides an algorithm
 * to aide in lucene searches.
 */
public class DefaultSearchHandler extends AbstractConfigurable
    implements SearchHandler {

  /** The score map. */
  private Map<Long, Float> scoreMap = new HashMap<>();

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    // na/
  }

  /* see superclass */
  @Override
  public <T extends HasId> List<T> getQueryResults(String terminology,
    String version, String branch, String query, String literalField,
    Class<T> clazz, PfsParameter pfs, int[] totalCt, EntityManager manager)
    throws Exception {

    // Default Search Handler algorithm
    // If empty query or ":" detected, perform query as written
    // If no results, perform tokenized/quoted search
    // If still no results, perform tokenized/wildcarded search

    // Build an escaped form of the query with wrapped quotes removed
    // This will be used for literal/exact searching
    String escapedQuery = query;
    if (query != null && query.startsWith("\"") && query.endsWith("\"")) {
      escapedQuery = escapedQuery.substring(1);
      escapedQuery = escapedQuery.substring(0, query.length() - 2);
    } else {
      escapedQuery = query == null ? "" : escapedQuery;
    }
    escapedQuery = "\"" + QueryParserBase.escape(escapedQuery) + "\"";

    // A slash character indicates a regex in lucene, fix that
    final String fixedQuery = query == null ? "" : query.replaceAll("\\/", " ");

    // Build a combined query with an OR between query typed and exact match
    String combinedQuery = null;

    // OPTION 1: Empty or Fielded query search.
    // Performed if empty query, ":" detected or no field specified
    if (fixedQuery.isEmpty() || query.contains(":")) {
      combinedQuery = fixedQuery;
    } else {
      combinedQuery = fixedQuery.isEmpty() ? "" : fixedQuery;

      if (literalField != null && !literalField.isEmpty()) {
        combinedQuery += " OR " + literalField + ":" + escapedQuery + "^10.0";
      }
    }

    // Add terminology conditions if supplied
    final StringBuilder terminologyClause = new StringBuilder();
    if (terminology != null && !terminology.equals("") && version != null
        && !version.equals("")) {
      terminologyClause.append(
          " AND terminology:" + terminology + " AND version:" + version);
    }

    // Assemble query
    final StringBuilder finalQuery = new StringBuilder();
    if (fixedQuery.isEmpty()) {
      if (terminologyClause.length() > 0) {
        // Just use PFS and skip the leading "AND"
        finalQuery.append(terminologyClause.substring(5));
      }
    } else if (combinedQuery.contains(" OR ")) {
      // Use parens
      finalQuery.append("(").append(combinedQuery).append(")")
          .append(terminologyClause);
    } else {
      // Don't use parens
      finalQuery.append(combinedQuery).append(terminologyClause);

    }
    FullTextQuery fullTextQuery = null;
    try {
      fullTextQuery = IndexUtility.applyPfsToLuceneQuery(clazz,
          finalQuery.toString(), pfs, manager);
    } catch (ParseException | IllegalArgumentException e) {
      e.printStackTrace();
      // If there's a parse exception, try the literal query
      Logger.getLogger(getClass()).debug("PE query = " + finalQuery);
      fullTextQuery = IndexUtility.applyPfsToLuceneQuery(clazz,
          escapedQuery + terminologyClause, pfs, manager);
    }

    totalCt[0] = fullTextQuery.getResultSize();

    // Perform the final query and save score values
    fullTextQuery.setProjection(ProjectionConstants.SCORE,
        ProjectionConstants.THIS);
    final List<T> classes = new ArrayList<>();
    @SuppressWarnings("unchecked")
    final List<Object[]> results = fullTextQuery.getResultList();
    for (final Object[] result : results) {
      Object score = result[0];
      @SuppressWarnings("unchecked")
      T t = (T) result[1];
      classes.add(t);

      // normalize results to a "good match" (lucene score of 5.0+)
      // Double normScore = Math.log(Math.max(5, scoreMap.get(sr.getId())) /
      // Math.log(5));

      // cap the score to a maximum of 5.0 and normalize to the range [0,1]

      Float normScore = Math.min(5, Float.valueOf(score.toString())) / 5;

      // store the score
      scoreMap.put(t.getId(), normScore.floatValue());
    }

    return classes;

  }

  /* see superclass */
  @Override
  public String getName() {
    return "Default Search Handler";
  }

  /* see superclass */
  @Override
  public Map<Long, Float> getScoreMap() {
    return scoreMap;
  }
}
