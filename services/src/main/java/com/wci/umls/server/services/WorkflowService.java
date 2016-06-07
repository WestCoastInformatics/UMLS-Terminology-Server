/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.services;

import java.util.Set;

import com.wci.umls.server.User;
import com.wci.umls.server.UserRole;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.StringList;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.workflow.TrackingRecord;
import com.wci.umls.server.model.workflow.TrackingRecordList;
import com.wci.umls.server.model.workflow.WorkflowAction;
import com.wci.umls.server.services.handlers.WorkflowActionHandler;



/**
 * Generically represents a service for performing workflow operations.
 */
public interface WorkflowService {

  /**
   * Returns the tracking record.
   *
   * @param id the id
   * @return the tracking record
   * @throws Exception the exception
   */
  public TrackingRecord getTrackingRecord(Long id) throws Exception;


  /**
   * Returns the tracking records for refset. The refset is assigned to at most
   * one person at a time.
   *
   * @param refsetId the refset id
   * @param userId the user id
   * @return the tracking records for refset
   * @throws Exception the exception
   */
  public TrackingRecord getTrackingRecordsForRefset(Long refsetId, Long userId)
    throws Exception;

  /**
   * Adds the tracking record.
   *
   * @param trackingRecord the tracking record
   * @return the tracking record
   * @throws Exception the exception
   */
  public TrackingRecord addTrackingRecord(TrackingRecord trackingRecord)
    throws Exception;

  /**
   * Update tracking record.
   *
   * @param trackingRecord the tracking record
   * @throws Exception the exception
   */
  public void updateTrackingRecord(TrackingRecord trackingRecord)
    throws Exception;

  /**
   * Removes the tracking record.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeTrackingRecord(Long id) throws Exception;

  /**
   * Returns the workflow paths defined by the supported listeners.
   *
   * @return the workflow paths
   */
  public StringList getWorkflowPaths();

  /**
   * Perform workflow action.
   *
   * @param refsetId the refset id
   * @param user the user
   * @param projectRole the project role
   * @param action the action
   * @return the tracking record
   * @throws Exception the exception
   */
  public TrackingRecord performWorkflowAction(Long refsetId, User user,
    UserRole projectRole, WorkflowAction action) throws Exception;

  /**
   * Perform workflow action.
   *
   * @param translationId the translation id
   * @param user the user
   * @param projectRole the project role
   * @param action the action
   * @param concept the concept
   * @return the tracking record
   * @throws Exception the exception
   */
  public TrackingRecord performWorkflowAction(Long translationId, User user,
    UserRole projectRole, WorkflowAction action, Concept concept)
    throws Exception;

  /**
   * Returns the workflow handler for path.
   *
   * @param workflowPat the workflow pat
   * @return the workflow handler for path
   * @throws Exception the exception
   */
  public WorkflowActionHandler getWorkflowHandlerForPath(String workflowPat)
    throws Exception;

  /**
   * Returns the workflow handlers.
   *
   * @return the workflow handlers
   * @throws Exception the exception
   */
  public Set<WorkflowActionHandler> getWorkflowHandlers() throws Exception;

  /**
   * Find tracking records for query.
   *
   * @param query the query
   * @param pfs the pfs
   * @return the tracking record list
   * @throws Exception the exception
   */
  public TrackingRecordList findTrackingRecordsForQuery(String query,
    PfsParameter pfs) throws Exception;


  /**
   * Handle lazy init.
   *
   * @param record the record
   */
  public void handleLazyInit(TrackingRecord record);
}