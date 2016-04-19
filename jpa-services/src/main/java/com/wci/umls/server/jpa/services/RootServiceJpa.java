/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services;

import java.lang.reflect.Field;
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
import com.wci.umls.server.helpers.HasLastModified;
import com.wci.umls.server.helpers.LogEntry;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.jpa.helpers.LogEntryJpa;
import com.wci.umls.server.jpa.services.helper.IndexUtility;
import com.wci.umls.server.model.meta.LogActivity;
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
        localQueryStr.append("  AND a.obsolete = 0 ");
      }
      if (pfs.getInactiveOnly()) {
        localQueryStr.append("  AND a.obsolete = 1 ");
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
   * Retrieves the sort field value from an object
   * @param o the object
   * @param sortField the period-separated X list of sequential getX methods,
   *          e.g. a.b.c
   * @return the value of the requested sort field
   * @throws Exception
   */
  @SuppressWarnings("static-method")
  Object getSortFieldValue(Object o, String sortField) throws Exception {
    // split the fields for method retrieval, e.g. a.b.c. =
    // o.getA().getB().getC()
    String[] splitFields = sortField.split("\\.");

    int i = 0;
    Method finalMethod = null;
    Object finalObject = o;

    while (i < splitFields.length) {
      finalMethod =
          finalObject.getClass().getMethod(
              "get" + ConfigUtility.capitalize(splitFields[i]),
              new Class<?>[] {});
      finalMethod.setAccessible(true);
      finalObject = finalMethod.invoke(finalObject, new Object[] {});
      i++;
    }

    // verify that final object is actually a string, enum, or date
    if (!finalMethod.getReturnType().equals(String.class)
        && !finalMethod.getReturnType().isEnum()
        && !finalMethod.getReturnType().equals(Date.class)) {
      throw new Exception(
          "Requested sort field value is not string, enum, or date value");
    }
    return finalObject;

  }

  /**
   * Retrieves the sort field value from an object
   * @param o the object
   * @param sortField the period-separated X list of sequential getX methods,
   *          e.g. a.b.c
   * @return the value of the requested sort field
   * @throws Exception
   */
  // package visibility
  @SuppressWarnings("static-method")
  Class<?> getSortFieldType(Object o, String sortField) throws Exception {
    // split the fields for method retrieval, e.g. a.b.c. =
    // o.getA().getB().getC()
    String[] splitFields = sortField.split("\\.");

    int i = 0;
    Method finalMethod = null;
    Object finalObject = o;

    while (i < splitFields.length) {
      finalMethod =
          finalObject.getClass().getMethod(
              "get" + ConfigUtility.capitalize(splitFields[i]),
              new Class<?>[] {});
      finalMethod.setAccessible(true);
      finalObject = finalMethod.invoke(finalObject, new Object[] {});
      i++;
    }

    // verify that final object is actually a string, enum, or date
    if (!finalMethod.getReturnType().equals(String.class)
        && !finalMethod.getReturnType().isEnum()
        && !finalMethod.getReturnType().equals(Date.class)) {
      throw new Exception(
          "Requested sort field value is not string, enum, or date value");
    }
    return finalMethod.getReturnType();

  }

  /**
   * Apply pfs to List.
   *
   * @param <T> the
   * @param list the list
   * @param clazz the clazz
   * @param totalCt the total ct
   * @param pfs the pfs
   * @return the paged, filtered, and sorted list
   * @throws Exception the exception
   */
  @Override
  public <T> List<T> applyPfsToList(List<T> list, Class<T> clazz,
    int[] totalCt, PfsParameter pfs) throws Exception {

    // Skip empty pfs
    if (pfs == null) {
      return list;
    }

    // NOTE: does not handle active/inactive logic

    List<T> result = list;

    // handle filtering based on query restriction
    if (pfs != null && pfs.getQueryRestriction() != null
        && !pfs.getQueryRestriction().isEmpty()) {
      result = new ArrayList<>();
      for (T t : list) {
        if (t.toString().toLowerCase()
            .indexOf(pfs.getQueryRestriction().toLowerCase()) != -1) {
          result.add(t);
        }
      }
    }

    // check if sorting required
    if (pfs != null) {

      List<String> sortFields = new ArrayList<>();

      // if sort field specified, add to list of sort fields
      if (pfs.getSortField() != null && pfs.getSortField().isEmpty()) {
        sortFields.add(pfs.getSortField());
      }

      // otherwise, if multiple sort fields specified
      else if (pfs.getSortFields() != null && !pfs.getSortFields().isEmpty()) {
        sortFields = pfs.getSortFields();
      }

      // if one or more sort fields found, apply sorting
      if (!sortFields.isEmpty()) {

        final boolean ascending = (pfs != null) ? pfs.isAscending() : true;

        // sort the list
        Collections.sort(result, new Comparator<T>() {

          @Override
          public int compare(T t1, T t2) {
            // if an exception is returned, simply pass equality
            try {

              for (String sortField : pfs.getSortFields()) {
                final Object s1 = getSortFieldValue(t1, sortField);
                final Object s2 = getSortFieldValue(t2, sortField);

                final boolean isDate =
                    getSortFieldType(t1, sortField).equals(Date.class);

                // if both values null, skip to next sort field
                if (s1 != null || s2 != null) {

                  // handle date comparison by long value
                  if (isDate) {
                    Long l1 = s1 == null ? null : ((Date) s1).getTime();
                    Long l2 = s2 == null ? null : ((Date) s2).getTime();
                    if (ascending) {
                      if (l1 == null && s2 != null) {
                        return -1;
                      }
                      if (l1.compareTo(l2) != 0) {
                        return l1.compareTo(l2);
                      }
                    } else {
                      if (l2 == null && l1 != null) {
                        return -1;
                      }
                      if (l2.compareTo(l2) != 0) {
                        return l2.compareTo(l1);
                      }
                    }
                  }

                  // otherwise handle via string comparison
                  else if (ascending) {
                    if (s1 == null && s2 != null) {
                      return -1;
                    }
                    if (s1.toString().compareTo(s2.toString()) != 0) {
                      return s1.toString().compareTo(s2.toString());
                    }
                  } else {
                    if (s2 == null && s1 != null) {
                      return -1;
                    }
                    if (((String) s2).compareTo((String) s1) != 0) {
                      return ((String) s2).compareTo((String) s1);
                    }
                  }
                }
              }
              // if no return after checking all sort fields, return equality
              return 0;
            } catch (Exception e) {
              e.printStackTrace();
              return 0;
            }
          }
        });
      }
    }

    // set the total count
    totalCt[0] = result.size();

    // get the start and end indexes based on paging parameters
    int startIndex = 0;

    int toIndex = result.size();
    if (pfs != null && pfs.getStartIndex() != -1) {
      startIndex = pfs.getStartIndex();
      toIndex = Math.min(result.size(), startIndex + pfs.getMaxResults());
      if (startIndex > toIndex)

      {
        startIndex = 0;
      }
      result = result.subList(startIndex, toIndex);

    }

    return result;
  }

  /**
   * Returns the pfs comparator.
   *
   * @param <T> the
   * @param clazz the clazz
   * @param pfs the pfs
   * @return the pfs comparator
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  protected <T> Comparator<T> getPfsComparator(Class<T> clazz, PfsParameter pfs)
    throws Exception {
    if (pfs != null
        && (pfs.getSortField() != null && !pfs.getSortField().isEmpty())) {
      // check that specified sort field exists on Concept and is
      // a string
      final Field sortField = clazz.getField(pfs.getSortField());

      // allow the field to access the Concept values
      sortField.setAccessible(true);

      if (pfs.isAscending()) {
        // make comparator
        return new Comparator<T>() {
          @Override
          public int compare(T o1, T o2) {
            try {
              // handle dates explicitly
              if (o2 instanceof Date) {
                return ((Date) sortField.get(o1)).compareTo((Date) sortField
                    .get(o2));
              } else {
                // otherwise, sort based on conversion to string
                return (sortField.get(o1).toString()).compareTo(sortField.get(
                    o2).toString());
              }
            } catch (IllegalAccessException e) {
              // on exception, return equality
              return 0;
            }
          }
        };
      } else {
        // make comparator
        return new Comparator<T>() {

          @Override
          public int compare(T o2, T o1) {
            try {
              // handle dates explicitly
              if (o2 instanceof Date) {
                return ((Date) sortField.get(o1)).compareTo((Date) sortField
                    .get(o2));
              } else {
                // otherwise, sort based on conversion to string
                return (sortField.get(o1).toString()).compareTo(sortField.get(
                    o2).toString());
              }
            } catch (IllegalAccessException e) {
              // on exception, return equality
              return 0;
            }
          }

        };
      }

    } else {
      return null;
    }
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

  /* see superclass */
  @Override
  public void commitClearBegin() throws Exception {
    commit();
    clear();
    beginTransaction();
  }

  /* see superclass */
  @Override
  public void logAndCommit(int objectCt, int logCt, int commitCt)
    throws Exception {
    // log at regular intervals
    if (objectCt % logCt == 0) {
      Logger.getLogger(getClass()).info("    count = " + objectCt);
    }
    if (objectCt % commitCt == 0) {
      commitClearBegin();
    }
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

  /**
   * Adds the has last modified.
   *
   * @param <T> the
   * @param hasLastModified the has last modified
   * @return the t
   * @throws Exception the exception
   */
  protected <T extends HasLastModified> T addHasLastModified(T hasLastModified)
    throws Exception {
    // Set last modified date
    if (lastModifiedFlag) {
      hasLastModified.setLastModified(new Date());
    }
    return addObject(hasLastModified);

  }

  /**
   * Adds the object.
   *
   * @param <T> the
   * @param object the object
   * @return the t
   * @throws Exception the exception
   */
  protected <T extends Object> T addObject(T object) throws Exception {
    try {
      // add
      if (getTransactionPerOperation()) {
        tx = manager.getTransaction();
        tx.begin();
        manager.persist(object);
        tx.commit();
      } else {
        manager.persist(object);
      }
      return object;
    } catch (Exception e) {
      if (tx.isActive()) {
        tx.rollback();
      }
      throw e;
    }
  }

  /**
   * Update has last modified.
   *
   * @param <T> the
   * @param hasLastModified the has last modified
   * @throws Exception the exception
   */
  protected <T extends HasLastModified> void updateHasLastModified(
    T hasLastModified) throws Exception {
    // Set modification date
    if (lastModifiedFlag) {
      hasLastModified.setLastModified(new Date());
    }
    updateObject(hasLastModified);

  }

  /**
   * Update object.
   *
   * @param <T> the
   * @param object the object
   * @throws Exception the exception
   */
  protected <T extends Object> void updateObject(T object) throws Exception {
    try {
      // update
      if (getTransactionPerOperation()) {
        tx = manager.getTransaction();
        tx.begin();
        manager.merge(object);
        tx.commit();
      } else {
        manager.merge(object);
      }
    } catch (Exception e) {
      if (tx.isActive()) {
        tx.rollback();
      }
      throw e;
    }

  }

  /**
   * Removes the has last modified.
   *
   * @param <T> the
   * @param id the id
   * @param clazz the clazz
   * @return the t
   * @throws Exception the exception
   */
  protected <T extends HasLastModified> T removeHasLastModified(Long id,
    Class<T> clazz) throws Exception {
    try {
      // Get transaction and object
      tx = manager.getTransaction();
      T hasLastModified = manager.find(clazz, id);

      // Set modification date
      if (lastModifiedFlag) {
        hasLastModified.setLastModified(new Date());
      }

      // Remove
      if (getTransactionPerOperation()) {
        // remove refset member
        tx.begin();
        if (manager.contains(hasLastModified)) {
          manager.remove(hasLastModified);
        } else {
          manager.remove(manager.merge(hasLastModified));
        }
        tx.commit();
      } else {
        if (manager.contains(hasLastModified)) {
          manager.remove(hasLastModified);
        } else {
          manager.remove(manager.merge(hasLastModified));
        }
      }
      return hasLastModified;
    } catch (Exception e) {
      if (tx.isActive()) {
        tx.rollback();
      }
      throw e;
    }
  }

  /**
   * Removes the object.
   *
   * @param <T> the
   * @param object the object
   * @param clazz the clazz
   * @return the t
   * @throws Exception the exception
   */
  protected <T extends Object> T removeObject(T object, Class<T> clazz)
    throws Exception {
    try {
      // Get transaction and object
      tx = manager.getTransaction();
      // Remove
      if (getTransactionPerOperation()) {
        // remove refset member
        tx.begin();
        if (manager.contains(object)) {
          manager.remove(object);
        } else {
          manager.remove(manager.merge(object));
        }
        tx.commit();
      } else {
        if (manager.contains(object)) {
          manager.remove(object);
        } else {
          manager.remove(manager.merge(object));
        }
      }
      return object;
    } catch (Exception e) {
      if (tx.isActive()) {
        tx.rollback();
      }
      throw e;
    }
  }

  /**
   * Returns the checks for object.
   *
   * @param <T> the
   * @param id the id
   * @param clazz the clazz
   * @return the checks for object
   * @throws Exception the exception
   */
  protected <T extends Object> T getObject(Long id, Class<T> clazz)
    throws Exception {
    // Get transaction and object
    tx = manager.getTransaction();
    T component = manager.find(clazz, id);
    return component;
  }

  /**
   * Returns the checks for last modified.
   *
   * @param <T> the
   * @param id the id
   * @param clazz the clazz
   * @return the checks for last modified
   * @throws Exception the exception
   */
  protected <T extends HasLastModified> T getHasLastModified(Long id,
    Class<T> clazz) throws Exception {
    // Get transaction and object
    tx = manager.getTransaction();
    T component = manager.find(clazz, id);
    return component;
  }

  /**
   * Returns the checks for last modifieds.
   *
   * @param <T> the
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param clazz the clazz
   * @return the checks for last modifieds
   */
  @SuppressWarnings("unchecked")
  protected <T extends HasLastModified> T getHasLastModified(
    String terminologyId, String terminology, String version, Class<T> clazz) {
    try {
      javax.persistence.Query query =
          manager
              .createQuery("select a from "
                  + clazz.getName()
                  + " a where terminologyId = :terminologyId and version = :version and terminology = :terminology");
      query.setParameter("terminologyId", terminologyId);
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      return (T) query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public List<LogEntry> findLogEntriesForQuery(String query, PfsParameter pfs)
    throws Exception {

    final StringBuilder sb = new StringBuilder();
    if (query != null && !query.equals("")) {
      sb.append(query);
    }

    int[] totalCt = new int[1];
    final List<LogEntry> list =
        (List<LogEntry>) getQueryResults(sb.toString(), LogEntryJpa.class,
            LogEntryJpa.class, pfs, totalCt);

    return list;
  }

  /* see superclass */
  @Override
  public LogEntry addLogEntry(LogEntry logEntry) throws Exception {
    return addHasLastModified(logEntry);
  }

  /* see superclass */
  @Override
  public void updateLogEntry(LogEntry logEntry) throws Exception {
    updateHasLastModified(logEntry);
  }

  /* see superclass */
  @Override
  public void removeLogEntry(Long id) throws Exception {
    removeHasLastModified(id, LogEntry.class);
  }

  /* see superclass */
  @Override
  public LogEntry getLogEntry(Long id) throws Exception {
    return getHasLastModified(id, LogEntry.class);
  }

  /* see superclass */
  @Override
  public LogEntry addLogEntry(String userName, Long projectId, Long objectId,
    String message) throws Exception {
    LogEntry entry = new LogEntryJpa();
    entry.setLastModifiedBy(userName);
    entry.setObjectId(objectId);
    entry.setProjectId(projectId);
    entry.setTimestamp(new Date());
    entry.setMessage(message);
    
    // TODO Confirm this with Brian/Deborah
    entry.setActivity(LogActivity.PROJECT);

    // Add component
    LogEntry newLogEntry = addLogEntry(entry);

    // do not inform listeners
    return newLogEntry;

  }

  /* see superclass */
  @Override
  public LogEntry addLogEntry(String userName, String terminology,
    String version, LogActivity activity, String message) throws Exception {
    LogEntry entry = new LogEntryJpa();
    entry.setLastModifiedBy(userName);
    entry.setTerminology(terminology);
    entry.setVersion(version);
    entry.setTimestamp(new Date());
    entry.setMessage(message);
    entry.setActivity(activity);

    // Add component
    LogEntry newLogEntry = addLogEntry(entry);

    // do not inform listeners
    return newLogEntry;

  }
  
}
