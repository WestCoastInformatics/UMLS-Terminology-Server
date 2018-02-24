/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.services;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.algo.action.MolecularActionAlgorithm;
import com.wci.umls.server.helpers.KeyValuePairList;
import com.wci.umls.server.helpers.LogEntry;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.QueryType;
import com.wci.umls.server.helpers.TypeKeyValue;
import com.wci.umls.server.helpers.TypeKeyValueList;
import com.wci.umls.server.model.actions.AtomicAction;
import com.wci.umls.server.model.actions.AtomicActionList;
import com.wci.umls.server.model.actions.MolecularAction;
import com.wci.umls.server.model.actions.MolecularActionList;
import com.wci.umls.server.model.content.Component;
import com.wci.umls.server.services.handlers.SearchHandler;
import com.wci.umls.server.services.handlers.ValidationCheck;

/**
 * Represents a service.
 */
public interface RootService extends Transactionable {

  /** The logging object ct threshold. */
  public final static int logCt = 5000;

  /** The commit count. */
  public final static int commitCt = 2000;

  /**
   * Open the factory.
   *
   * @throws Exception the exception
   */
  public void openFactory() throws Exception;

  /**
   * Close the factory.
   *
   * @throws Exception the exception
   */
  public void closeFactory() throws Exception;

  /**
   * Refresh any cached data or handlers (e.g. reload handlers from the config).
   * This supports the ability for config.properties to be dynamically changed
   * and then reloaded within the server.
   *
   * @throws Exception the exception
   */
  public void refreshCaches() throws Exception;

  /**
   * Apply pfs to list.
   *
   * @param <T> the
   * @param list the list
   * @param clazz the clazz
   * @param totalCt the total ct
   * @param pfs the pfs
   * @return the list
   * @throws Exception the exception
   */
  public <T> List<T> applyPfsToList(List<T> list, Class<T> clazz, int[] totalCt,
    PfsParameter pfs) throws Exception;

  /**
   * Checks if is last modified flag.
   *
   * @return true, if is last modified flag
   */
  public boolean isLastModifiedFlag();

  /**
   * Sets the last modified flag.
   *
   * @param lastModifiedFlag the last modified flag
   */
  public void setLastModifiedFlag(boolean lastModifiedFlag);

  /**
   * Gets the last modified by.
   *
   * @return the last modified by
   */
  public String getLastModifiedBy();

  /**
   * Sets the last modified by.
   *
   * @param lastModifiedBy the new last modified by
   */
  public void setLastModifiedBy(String lastModifiedBy);

  /**
   * Checks if is molecular action flag.
   *
   * @return true, if is molecular action flag
   */
  public boolean isMolecularActionFlag();

  /**
   * Sets the molecular action flag.
   *
   * @param molecularActionFlag the new molecular action flag
   */
  public void setMolecularActionFlag(boolean molecularActionFlag);

  /**
   * Gets the search handler.
   *
   * @param key the key
   * @return the search handler
   * @throws Exception the exception
   */
  public SearchHandler getSearchHandler(String key) throws Exception;

  /**
   * Gets the molecular action.
   *
   * @return the molecular action
   * @throws Exception the exception
   */
  public MolecularAction getMolecularAction() throws Exception;

  /**
   * Sets the molecular action.
   *
   * @param molecularAction the new molecular action
   */
  public void setMolecularAction(MolecularAction molecularAction);

  /**
   * Find log entries for query.
   *
   * @param query the query
   * @param pfs the pfs
   * @return the list
   * @throws Exception the exception
   */
  public List<LogEntry> findLogEntries(String query, PfsParameter pfs)
    throws Exception;

  /**
   * Update log entry.
   *
   * @param logEntry the log entry
   * @throws Exception the exception
   */
  public void updateLogEntry(LogEntry logEntry) throws Exception;

  /**
   * Removes the log entry.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeLogEntry(Long id) throws Exception;

  /**
   * Gets the log entry.
   *
   * @param id the id
   * @return the log entry
   * @throws Exception the exception
   */
  public LogEntry getLogEntry(Long id) throws Exception;

  /**
   * Adds the log entry.
   *
   * @param logEntry the log entry
   * @return the log entry
   * @throws Exception the exception
   */
  public LogEntry addLogEntry(LogEntry logEntry) throws Exception;

  /**
   * Adds the log entry.
   *
   * @param userName the user name
   * @param terminology the terminology
   * @param version the version
   * @param activityId the activity id
   * @param workId the work id
   * @param message the message
   * @return the log entry
   * @throws Exception the exception
   */
  public LogEntry addLogEntry(String userName, String terminology,
    String version, String activityId, String workId, String message)
    throws Exception;

  /**
   * Adds the log entry.
   *
   * @param userName the user name
   * @param projectId the project id
   * @param objectId the object id
   * @param activityId the activity id
   * @param workId the work id
   * @param message the message
   * @return the log entry
   * @throws Exception the exception
   */
  public LogEntry addLogEntry(String userName, Long projectId, Long objectId,
    String activityId, String workId, String message) throws Exception;

  /**
   * Lock Hibernate object.
   *
   * @param object the object
   * @throws Exception the exception
   */
  public void lockObject(Object object) throws Exception;

  /**
   * Unlock Hibernate object.
   *
   * @param object the object
   */
  public void unlockObject(Object object);

  /**
   * Is object locked.
   *
   * @param object the object
   * @return true, if is object locked
   * @throws Exception the exception
   */
  public boolean isObjectLocked(Object object) throws Exception;

  /**
   * Add molecular action.
   *
   * @param action the action
   * @return the molecular action
   * @throws Exception the exception
   */
  public MolecularAction addMolecularAction(MolecularAction action)
    throws Exception;

  /**
   * Remove molecular action.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeMolecularAction(Long id) throws Exception;

  /**
   * Gets the molecular action.
   *
   * @param id the id
   * @return the molecular action
   * @throws Exception the exception
   */
  public MolecularAction getMolecularAction(Long id) throws Exception;

  /**
   * Add atomic action.
   *
   * @param action the action
   * @return the atomic action
   * @throws Exception the exception
   */
  public AtomicAction addAtomicAction(AtomicAction action) throws Exception;

  /**
   * Remove atomic action.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeAtomicAction(Long id) throws Exception;

  /**
   * Gets the atomic action.
   *
   * @param id the id
   * @return the atomic action
   * @throws Exception the exception
   */
  public AtomicAction getAtomicAction(Long id) throws Exception;

  /**
   * Find molecular actions.
   *
   * @param componentId the component id
   * @param terminology the terminology
   * @param version the version
   * @param query the query
   * @param pfs the pfs
   * @return the molecular action list
   * @throws Exception the exception
   */
  public MolecularActionList findMolecularActions(Long componentId,
    String terminology, String version, String query, PfsParameter pfs)
    throws Exception;

  /**
   * Find atomic actions.
   *
   * @param moleculeId the molecule id
   * @param query the query
   * @param pfs the pfs
   * @return the atomic action list
   * @throws Exception the exception
   */
  public AtomicActionList findAtomicActions(Long moleculeId, String query,
    PfsParameter pfs) throws Exception;

  /**
   * Adds the type key value.
   *
   * @param typeKeyValue the type key value
   * @return the type key value
   * @throws Exception the exception
   */
  public TypeKeyValue addTypeKeyValue(TypeKeyValue typeKeyValue)
    throws Exception;

  /**
   * Update type key value.
   *
   * @param typeKeyValue the type key value
   * @throws Exception the exception
   */
  public void updateTypeKeyValue(TypeKeyValue typeKeyValue) throws Exception;

  /**
   * Removes the type key value.
   *
   * @param typeKeyValueId the type key value id
   * @throws Exception the exception
   */
  public void removeTypeKeyValue(Long typeKeyValueId) throws Exception;

  /**
   * Returns the type key value.
   *
   * @param typeKeyValueId the type key value id
   * @return the type key value
   * @throws Exception the exception
   */
  public TypeKeyValue getTypeKeyValue(Long typeKeyValueId) throws Exception;

  /**
   * Find type key values for query.
   *
   * @param query the query
   * @param pfs the pfs
   * @return the list
   * @throws Exception the exception
   */
  public TypeKeyValueList findTypeKeyValuesForQuery(String query,
    PfsParameter pfs) throws Exception;

  /**
   * Gets the validation check names.
   *
   * @return the validation check names
   * @throws Exception the exception
   */
  public KeyValuePairList getValidationCheckNames() throws Exception;

  /**
   * Execute component id pair query.
   *
   * @param query the query
   * @param queryType the query type
   * @param params the params
   * @param clazz the clazz
   * @param test the test
   * @return the list
   * @throws Exception the exception
   */
  public List<Long[]> executeComponentIdPairQuery(String query,
    QueryType queryType, Map<String, String> params,
    Class<? extends Component> clazz, boolean test) throws Exception;

  /**
   * Execute single component id query.
   *
   * @param query the query
   * @param queryType the query type
   * @param params the params
   * @param clazz the clazz
   * @param test the test
   * @return the list
   * @throws Exception the exception
   */
  public List<Long> executeSingleComponentIdQuery(String query,
    QueryType queryType, Map<String, String> params,
    Class<? extends Component> clazz, boolean test) throws Exception;

  /**
   * Execute clustered concept query.
   *
   * @param query the query
   * @param queryType the query type
   * @param params the params
   * @param test the test
   * @return the list
   * @throws Exception the exception
   */
  public List<Long[]> executeClusteredConceptQuery(String query,
    QueryType queryType, Map<String, String> params, boolean test)
    throws Exception;

  /**
   * Execute report query. This is an itemId, itemName 2 col query.
   *
   * @param query the query
   * @param queryType the query type
   * @param params the params
   * @param test the test
   * @return the list
   * @throws Exception the exception
   */
  public List<Object[]> executeReportQuery(String query, QueryType queryType,
    Map<String, String> params, boolean test) throws Exception;

  /**
   * Execute query query.
   *
   * @param query the query
   * @param queryType the query type
   * @param params the params
   * @param test the test
   * @return the list
   * @throws Exception the exception
   */
  public List<Object[]> executeQuery(String query, QueryType queryType,
    Map<String, String> params, boolean test) throws Exception;

  /**
   * Returns the validation handlers map.
   *
   * @return the validation handlers map
   * @throws Exception the exception
   */
  public Map<String, ValidationCheck> getValidationHandlersMap()
    throws Exception;

  /**
   * Validate action.
   *
   * @param action the action
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult validateAction(MolecularActionAlgorithm action)
    throws Exception;

  /**
   * Returns the entity manager.
   *
   * @return the entity manager
   * @throws Exception the exception
   */
  public EntityManager getEntityManager() throws Exception;

  /**
   * Execute clustered concept query ct.
   *
   * @param query the query
   * @param queryType the query type
   * @param params the params
   * @return the int
   * @throws Exception the exception
   */
  public int executeClusteredConceptQueryCt(String query, QueryType queryType,
    Map<String, String> params) throws Exception;

}