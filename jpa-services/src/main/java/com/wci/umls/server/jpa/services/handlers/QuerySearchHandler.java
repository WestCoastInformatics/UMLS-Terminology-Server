/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.handlers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import com.wci.umls.server.helpers.HasId;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.model.workflow.QueryType;

/**
 * Extension of default search handler to support HQL and SQL querying as well.
 */
public class QuerySearchHandler extends DefaultSearchHandler {

  /* see superclass */
  @Override
  public <T extends HasId> List<T> getQueryResults(String terminology,
    String version, String branch, String query, String literalField,
    Class<?> fieldNamesKey, Class<T> clazz, PfsParameter pfs, int[] totalCt,
    EntityManager manager) throws Exception {

    if (query.toLowerCase().startsWith("select")) {
      final Map<String, String> params = new HashMap<>();
      params.put("terminology", terminology);
      params.put("version", version);
      return executeQuery(query, literalField.equals(QueryType.SQL), clazz,
          params, manager);
    } else {
      return super.getQueryResults(terminology, version, branch, query,
          literalField, fieldNamesKey, clazz, pfs, totalCt, manager);
    }
  }

  /* see superclass */
  @Override
  public String getName() {
    return "Query Search Handler";
  }

  /**
   * Execute query.
   *
   * @param <T> the
   * @param query the query
   * @param nativeFlag the native flag
   * @param clazz the clazz
   * @param params the params
   * @param manager the manager
   * @return the list
   * @throws Exception the exception
   */
  @SuppressWarnings({
      "unchecked", "static-method"
  })
  private <T extends HasId> List<T> executeQuery(String query,
    boolean nativeFlag, Class<T> clazz, Map<String, String> params,
    EntityManager manager) throws Exception {

    // check for sql query errors -- throw as local exception
    // this is used to propagate errors back to user when testing queries

    // ensure that query begins with SELECT (i.e. prevent injection
    // problems)
    if (!query.toUpperCase().startsWith("SELECT")) {
      throw new LocalException(
          "SQL Query has bad format:  does not begin with SELECT");
    }

    // check for multiple commands (i.e. multiple semi-colons)
    if (query.indexOf(";") != query.length() - 1 && query.endsWith(";")) {
      throw new LocalException(
          "SQL Query has bad format:  multiple commands detected");
    }

    // crude check: check for data manipulation commands
    if (query.toUpperCase().matches(
        "ALTER |CREATE |DROP |DELETE |INSERT |TRUNCATE |UPDATE ")) {
      throw new LocalException(
          "SQL Query has bad format:  data manipulation request detected");
    }

    // check for proper format for insertion into reports

    if (query.toUpperCase().indexOf(" FROM ") == -1)
      throw new LocalException(
          "Workflow bin definition query must contain the term FROM");

    javax.persistence.Query jpaQuery = null;
    if (nativeFlag) {
      jpaQuery = manager.createNativeQuery(query);
      if (params != null) {
        for (final String key : params.keySet()) {
          if (query.contains(":" + key)) {
            jpaQuery.setParameter(key, params.get(key));
          }
        }
      }
    } else {
      jpaQuery = manager.createQuery(query);
      if (params != null) {
        for (final String key : params.keySet()) {
          jpaQuery.setParameter(key, params.get(key));
        }
      }
    }
    return jpaQuery.getResultList();
  }

}
