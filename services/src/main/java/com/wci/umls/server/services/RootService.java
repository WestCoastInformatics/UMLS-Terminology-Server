/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.services;

import java.util.List;

import com.wci.umls.server.helpers.LogEntry;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.model.actions.AtomicAction;
import com.wci.umls.server.model.actions.MolecularAction;
import com.wci.umls.server.model.actions.MolecularActionList;
import com.wci.umls.server.model.meta.LogActivity;

/**
 * Represents a service.
 */
public interface RootService {

  /** The logging object ct threshold. */
  public final static int logCt = 2000;

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
   * Gets the transaction per operation.
   *
   * @return the transaction per operation
   * @throws Exception the exception
   */
  public boolean getTransactionPerOperation() throws Exception;

  /**
   * Sets the transaction per operation.
   *
   * @param transactionPerOperation the new transaction per operation
   * @throws Exception the exception
   */
  public void setTransactionPerOperation(boolean transactionPerOperation)
    throws Exception;

  /**
   * Commit.
   *
   * @throws Exception the exception
   */
  public void commit() throws Exception;

  /**
   * Rollback.
   *
   * @throws Exception the exception
   */
  public void rollback() throws Exception;

  /**
   * Begin transaction.
   *
   * @throws Exception the exception
   */
  public void beginTransaction() throws Exception;

  /**
   * Closes the manager.
   *
   * @throws Exception the exception
   */
  public void close() throws Exception;

  /**
   * Clears the manager.
   *
   * @throws Exception the exception
   */
  public void clear() throws Exception;

  /**
   * Refresh caches.
   *
   * @throws Exception the exception
   */
  public void refreshCaches() throws Exception;

  /**
   * Commit clear begin transaction.
   *
   * @throws Exception the exception
   */
  public void commitClearBegin() throws Exception;

  /**
   * Log and commit.
   *
   * @param objectCt the object ct
   * @param logCt the log ct
   * @param commitCt the commit ct
   * @throws Exception the exception
   */
  public void logAndCommit(int objectCt, int logCt, int commitCt)
    throws Exception;

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
   * Gets the molecular action.
   *
   * @return the molecular action
   * @throws Exception 
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
  public List<LogEntry> findLogEntriesForQuery(String query, PfsParameter pfs)
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
   * @param activity the activity
   * @param message the message
   * @return the log entry
   * @throws Exception the exception
   */
  public LogEntry addLogEntry(String userName, String terminology,
    String version, LogActivity activity, String message) throws Exception;

  /**
   * Adds the log entry.
   *
   * @param userName the user name
   * @param projectId the project id
   * @param objectId the object id
   * @param message the message
   * @return the log entry
   * @throws Exception the exception
   */
  public LogEntry addLogEntry(String userName, Long projectId, Long objectId,
    String message) throws Exception;

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
   * @param cascadeFlag whether to cascade the operation
   * @return the molecular action
   * @throws Exception the exception
   */
  public MolecularAction addMolecularAction(MolecularAction action) throws Exception;

  /**
   * Remove molecular action.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeMolecularAction(Long id)
    throws Exception;

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
   * @param terminology the terminology
   * @param version the version
   * @param query the query
   * @param pfs the pfs
   * @return the list
   * @throws Exception the exception
   */
  public MolecularActionList findMolecularActions(String terminology, String version, String query, PfsParameter pfs) throws Exception;

}