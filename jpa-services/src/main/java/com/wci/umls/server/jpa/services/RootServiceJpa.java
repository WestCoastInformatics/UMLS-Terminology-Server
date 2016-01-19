/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;

import org.apache.log4j.Logger;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.hibernate.search.jpa.FullTextQuery;

import com.wci.umls.server.User;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.jpa.services.helper.IndexUtility;
import com.wci.umls.server.services.RootService;

/**
 * The root service for managing the entity manager factory and hibernate search
 * field names.
 */
public abstract class RootServiceJpa implements RootService {

  /** The last modified flag. */
  protected boolean lastModifiedFlag = true;

  /** The user map. */
  protected static Map<String, User> userMap = new HashMap<>();

  /** The factory. */
  protected static EntityManagerFactory factory = null;
  static {
    Logger.getLogger(RootServiceJpa.class).info(
        "Setting root service entity manager factory.");
    Properties config;
    try {
      config = ConfigUtility.getConfigProperties();
      factory = Persistence.createEntityManagerFactory("TermServiceDS", config);
    } catch (Exception e) {
      e.printStackTrace();
      factory = null;
    }
  }

  /** The manager. */
  protected EntityManager manager;

  /** The transaction per operation. */
  protected boolean transactionPerOperation = true;

  /** The transaction entity. */
  protected EntityTransaction tx;

  /**
   * Instantiates an empty {@link RootServiceJpa}.
   *
   * @throws Exception the exception
   */
  public RootServiceJpa() throws Exception {
    // created once or if the factory has closed
    if (factory == null) {
      throw new Exception("Factory is null, serious problem.");
    }
    if (!factory.isOpen()) {
      Logger.getLogger(getClass()).info(
          "Setting root service entity manager factory.");
      Properties config = ConfigUtility.getConfigProperties();
      factory = Persistence.createEntityManagerFactory("TermServiceDS", config);
    }

    // created on each instantiation
    manager = factory.createEntityManager();
    tx = manager.getTransaction();
  }

  /* see superclass */
  @Override
  public void openFactory() throws Exception {

    // if factory has not been instantiated or has been closed, open it
    if (factory == null) {
      throw new Exception("Factory is null, serious problem.");
    }
    if (!factory.isOpen()) {
      Logger.getLogger(getClass()).info(
          "Setting root service entity manager factory.");
      Properties config = ConfigUtility.getConfigProperties();
      factory = Persistence.createEntityManagerFactory("TermServiceDS", config);
    }
  }

  /* see superclass */
  @Override
  public void closeFactory() throws Exception {
    if (factory.isOpen()) {
      factory.close();
    }
  }

  /* see superclass */
  @Override
  public boolean getTransactionPerOperation() {
    return transactionPerOperation;
  }

  /* see superclass */
  @Override
  public void setTransactionPerOperation(boolean transactionPerOperation) {
    this.transactionPerOperation = transactionPerOperation;
  }

  /* see superclass */
  @Override
  public void beginTransaction() throws Exception {

    if (getTransactionPerOperation())
      throw new IllegalStateException(
          "Error attempting to begin a transaction when using transactions per operation mode.");
    else if (tx != null && tx.isActive())
      throw new IllegalStateException(
          "Error attempting to begin a transaction when there "
              + "is already an active transaction");
    tx = manager.getTransaction();
    tx.begin();
  }

  /* see superclass */
  @Override
  public void commit() throws Exception {

    if (getTransactionPerOperation()) {
      throw new IllegalStateException(
          "Error attempting to commit a transaction when using transactions per operation mode.");
    } else if (tx != null && !tx.isActive()) {
      throw new IllegalStateException(
          "Error attempting to commit a transaction when there "
              + "is no active transaction");
    } else if (tx != null) {
      tx.commit();
      manager.clear();
    }
  }

  /* see superclass */
  @Override
  public void rollback() throws Exception {

    if (getTransactionPerOperation()) {
      throw new IllegalStateException(
          "Error attempting to rollback a transaction when using transactions per operation mode.");
    } else if (tx != null && !tx.isActive()) {
      throw new IllegalStateException(
          "Error attempting to rollback a transaction when there "
              + "is no active transaction");
    } else if (tx != null) {
      tx.rollback();
      manager.clear();
    }
  }

  /* see superclass */
  @Override
  public void close() throws Exception {
    if (manager.isOpen()) {
      manager.close();
    }
  }

  /* see superclass */
  @Override
  public void clear() throws Exception {
    if (manager.isOpen()) {
      manager.clear();
    }
  }

  /* see superclass */
  @Override
  public void refreshCaches() throws Exception {
    // n/a
  }

  /**
   * Returns the entity manager.
   *
   * @return the entity manager
   * @throws Exception the exception
   */
  public EntityManager getEntityManager() throws Exception {
    return manager;
  }

  /**
   * Apply pfs to query.
   *
   * @param queryStr the query str
   * @param pfs the pfs
   * @return the javax.persistence. query
   * @throws Exception the exception
   */
  public javax.persistence.Query applyPfsToJqlQuery(String queryStr,
    PfsParameter pfs) throws Exception {
    StringBuilder localQueryStr = new StringBuilder();
    localQueryStr.append(queryStr);

    // Query restriction assumes a driving table called "a"
    if (pfs != null) {
      if (pfs.getQueryRestriction() == null) {
        throw new Exception("Query restriction not supported for JQL queries");
      }

      if (pfs.getActiveOnly()) {
        localQueryStr.append("  AND a.active = 1 ");
      }
      if (pfs.getInactiveOnly()) {
        localQueryStr.append("  AND a.active = 0 ");
      }

      // add an order by clause to end of the query, assume driving table
      // called
      // "a"
      if (pfs.getSortField() != null) {
        localQueryStr.append(" order by a.").append(pfs.getSortField());
      }
    }

    javax.persistence.Query query =
        manager.createQuery(localQueryStr.toString());
    if (pfs != null && pfs.getStartIndex() > -1 && pfs.getMaxResults() > -1) {
      query.setFirstResult(pfs.getStartIndex());
      query.setMaxResults(pfs.getMaxResults());
    }
    return query;
  }

  /**
   * Apply pfs to List.
   *
   * @param <T> the
   * @param list the list
   * @param clazz the clazz
   * @param pfs the pfs
   * @return the javax.persistence. query
   * @throws Exception the exception
   */
  @Override
  public <T> List<T> applyPfsToList(List<T> list, Class<T> clazz,
    PfsParameter pfs) throws Exception {

    // Skip empty pfs
    if (pfs == null) {
      return list;
    }

    // NOTE: does not handle active/inactive logic

    List<T> result = list;

    // Handle sorting

    // apply paging, and sorting if appropriate
    if (pfs != null
        && (pfs.getSortField() != null && !pfs.getSortField().isEmpty())) {

      // check that specified sort field exists on Concept and is
      // a string
      final Method sortMethod =
          clazz.getMethod("get" + ConfigUtility.capitalize(pfs.getSortField()),
              new Class<?>[] {});

      if (!sortMethod.getReturnType().equals(String.class)
          && !sortMethod.getReturnType().equals(Date.class)) {
        throw new Exception("Referenced sort field is not of type String");
      }

      // allow the method to be accessed
      sortMethod.setAccessible(true);

      // sort the list
      Collections.sort(result, new Comparator<T>() {
        @Override
        public int compare(T t1, T t2) {
          // if an exception is returned, simply pass equality
          try {
            final String s1 = (String) sortMethod.invoke(t1, new Object[] {});
            final String s2 = (String) sortMethod.invoke(t2, new Object[] {});
            return s1.compareTo(s2);
          } catch (Exception e) {
            return 0;
          }
        }
      });
    }

    // Handle filtering based on toString()
    if (pfs != null
        && (pfs.getQueryRestriction() != null && !pfs.getQueryRestriction()
            .isEmpty())) {

      // Strip last char off if it is a *
      String match = pfs.getQueryRestriction();
      if (match.lastIndexOf('*') == match.length() - 1) {
        match = match.substring(0, match.length() - 1);
      }
      final List<T> filteredResult = new ArrayList<T>();
      for (T t : result) {
        if (t.toString().toLowerCase().indexOf(match.toLowerCase()) != -1) {
          filteredResult.add(t);
        }
      }

      if (filteredResult.size() != result.size()) {
        result = filteredResult;
      }
    }

    // get the start and end indexes based on paging parameters
    int startIndex = 0;
    int toIndex = result.size();
    if (pfs != null && pfs.getStartIndex() != -1) {
      startIndex = pfs.getStartIndex();
      toIndex = Math.min(result.size(), startIndex + pfs.getMaxResults());
      if (startIndex > toIndex) {
        startIndex = 0;
      }
      result = result.subList(startIndex, toIndex);
    }

    return result;
  }

  /**
   * Returns the user for the userName. Utility method.
   *
   * @param userName the userName
   * @return the user
   * @throws Exception the exception
   */
  public User getUser(String userName) throws Exception {
    if (userMap.containsKey(userName)) {
      return userMap.get(userName);
    }
    javax.persistence.Query query =
        manager
            .createQuery("select u from UserJpa u where userName = :userName");
    query.setParameter("userName", userName);
    try {
      User user = (User) query.getSingleResult();
      userMap.put(userName, user);
      return user;
    } catch (NoResultException e) {
      return null;
    }
  }

  /* see superclass */
  @Override
  public void setLastModifiedFlag(boolean lastModifiedFlag) {
    this.lastModifiedFlag = lastModifiedFlag;
  }

  /**
   * Returns the query results.
   *
   * @param <T> the
   * @param query the query
   * @param fieldNamesKey the field names key
   * @param clazz the clazz
   * @param pfs the pfs
   * @param totalCt the total ct
   * @return the query results
   * @throws Exception the exception
   */
  public <T> List<?> getQueryResults(String query, Class<?> fieldNamesKey,
    Class<T> clazz, PfsParameter pfs, int[] totalCt) throws Exception {

    if (query == null || query.isEmpty()) {
      throw new Exception("Unexpected empty query.");
    }

    FullTextQuery fullTextQuery = null;
    try {
      fullTextQuery =
          IndexUtility.applyPfsToLuceneQuery(clazz, fieldNamesKey, query, pfs,
              manager);
    } catch (ParseException e) {
      // If parse exception, try a literal query
      StringBuilder escapedQuery = new StringBuilder();
      if (query != null && !query.isEmpty()) {
        escapedQuery.append(QueryParserBase.escape(query));
      }
      fullTextQuery =
          IndexUtility.applyPfsToLuceneQuery(clazz, fieldNamesKey,
              escapedQuery.toString(), pfs, manager);
    }

    totalCt[0] = fullTextQuery.getResultSize();
    return fullTextQuery.getResultList();

  }

}
