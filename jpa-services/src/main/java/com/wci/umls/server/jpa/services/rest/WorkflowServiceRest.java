/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.rest;

import com.wci.umls.server.UserRole;
import com.wci.umls.server.helpers.ChecklistList;
import com.wci.umls.server.helpers.StringList;
import com.wci.umls.server.helpers.TrackingRecordList;
import com.wci.umls.server.helpers.WorklistList;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.worfklow.WorkflowBinDefinitionJpa;
import com.wci.umls.server.jpa.worfklow.WorkflowConfigJpa;
import com.wci.umls.server.model.workflow.WorkflowAction;
import com.wci.umls.server.model.workflow.WorkflowBinDefinition;
import com.wci.umls.server.model.workflow.WorkflowBinType;
import com.wci.umls.server.model.workflow.WorkflowConfig;
import com.wci.umls.server.model.workflow.Worklist;

/**
 * The Interface WorkflowServiceRest.
 */
public interface WorkflowServiceRest {

  /**
   * Adds the workflow config.
   *
   * @param projectId the project id
   * @param config the config
   * @param authToken the auth token
   * @return the workflow config
   * @throws Exception the exception
   */
  public WorkflowConfig addWorkflowConfig(Long projectId,
    WorkflowConfigJpa config, String authToken) throws Exception;

  /**
   * Update workflow config.
   *
   * @param projectId the project id
   * @param config the config
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void updateWorkflowConfig(Long projectId, WorkflowConfigJpa config,
    String authToken) throws Exception;

  /**
   * Removes the workflow config.
   *
   * @param workflowConfigId the workflow config id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void removeWorkflowConfig(Long workflowConfigId, String authToken)
    throws Exception;

  /**
   * Adds the workflow bin definition.
   *
   * @param projectId the project id
   * @param workflowConfigId the workflow config id
   * @param binDefinition the bin definition
   * @param authToken the auth token
   * @return the workflow bin definition
   * @throws Exception the exception
   */
  public WorkflowBinDefinition addWorkflowBinDefinition(Long projectId,
    Long workflowConfigId, WorkflowBinDefinitionJpa binDefinition,
    String authToken) throws Exception;

  /**
   * Update workflow bin definition.
   * @param projectId project id
   * @param definition the definition
   * @param authToken auth token
   *
   * @throws Exception the exception
   */
  public void updateWorkflowBinDefinition(Long projectId,
    WorkflowBinDefinitionJpa definition, String authToken) throws Exception;

  /**
   * Removes the workflow bin definition.
   *
   * @param projectId the project id
   * @param workflowBinDefinitionId the workflow bin definition id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void removeWorkflowBinDefinition(Long projectId,
    Long workflowBinDefinitionId, String authToken) throws Exception;

  /**
   * Regenerate bins.
   *
   * @param projectId the project id
   * @param type the type
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void regenerateBins(Long projectId, WorkflowBinType type,
    String authToken) throws Exception;

  /**
   * Find assigned work.
   *
   * @param projectId the project id
   * @param userName the user name
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the tracking record list
   * @throws Exception the exception
   */
  public TrackingRecordList findAssignedWork(Long projectId, String userName,
    PfsParameterJpa pfs, String authToken) throws Exception;

  /**
   * Find available work.
   *
   * @param projectId the project id
   * @param role the role
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the tracking record list
   * @throws Exception the exception
   */
  public TrackingRecordList findAvailableWork(Long projectId, UserRole role,
    PfsParameterJpa pfs, String authToken) throws Exception;
  
  /**
   * Find assigned worklists.
   *
   * @param projectId the project id
   * @param userName the user name
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the worklist list
   * @throws Exception the exception
   */
  public WorklistList findAssignedWorklists(Long projectId, String userName,
    PfsParameterJpa pfs, String authToken) throws Exception;

  /**
   * Find checklists.
   *
   * @param projectId the project id
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the checklist list
   * @throws Exception the exception
   */
  public ChecklistList findChecklists(Long projectId, String query,
    PfsParameterJpa pfs, String authToken) throws Exception;
  
  /**
   * Returns the workflow paths defined by the supported listeners.
   *
   * @param authToken the auth token
   * @return the workflow paths
   * @throws Exception the exception
   */
  public StringList getWorkflowPaths(String authToken) throws Exception;
  
  /**
   * Perform workflow action.
   *
   * @param projectId the project id
   * @param worklistId the worklist id
   * @param userName the user name
   * @param role the role
   * @param action the action
   * @param authToken the auth token
   * @return the tracking record
   * @throws Exception the exception
   */
  public Worklist performWorkflowAction(Long projectId, Long worklistId,
    String userName, UserRole role, WorkflowAction action,
    String authToken) throws Exception;
  
  
  /**
   * Returns the tracking records for concept.
   *
   * @param conceptId the concept id
   * @param authToken the auth token
   * @return the tracking records for concept
   * @throws Exception the exception
   */
  public TrackingRecordList getTrackingRecordsForConcept(Long conceptId,
    String authToken) throws Exception;  

  /**
   * Find available worklists.
   *
   * @param projectId the project id
   * @param role the role
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the worklist list
   * @throws Exception the exception
   */
  public WorklistList findAvailableWorklists(Long projectId,
    UserRole role, PfsParameterJpa pfs, String authToken)
    throws Exception;
  
  
}
