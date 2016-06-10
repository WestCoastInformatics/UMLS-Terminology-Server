/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.services;

import java.util.List;
import java.util.Set;

import com.wci.umls.server.User;
import com.wci.umls.server.UserRole;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.StringList;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.workflow.Checklist;
import com.wci.umls.server.model.workflow.ChecklistList;
import com.wci.umls.server.model.workflow.ProjectWorkflowConfig;
import com.wci.umls.server.model.workflow.TrackingRecord;
import com.wci.umls.server.model.workflow.TrackingRecordList;
import com.wci.umls.server.model.workflow.WorkflowAction;
import com.wci.umls.server.model.workflow.WorkflowBin;
import com.wci.umls.server.model.workflow.WorkflowBinDefinition;
import com.wci.umls.server.model.workflow.WorkflowEpoch;
import com.wci.umls.server.model.workflow.Worklist;
import com.wci.umls.server.model.workflow.WorklistList;
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
  

  public WorkflowEpoch addWorkflowEpoch(WorkflowEpoch workflowEpoch) throws Exception;


  public void updateWorkflowEpoch(WorkflowEpoch workflowEpoch) throws Exception;
  

  public void removeWorkflowEpoch(Long id) throws Exception;

 
  public List<WorkflowEpoch> getWorkflowEpochs() throws Exception;
  

  public WorkflowEpoch getWorkflowEpoch(Long id) throws Exception;
  

  public List<WorkflowEpoch> findWorkflowEpochsForQuery(String query) throws Exception;
  

  public ProjectWorkflowConfig addProjectWorkflowConfig(ProjectWorkflowConfig projectWorkflowConfig) throws Exception;


  public void updateProjectWorkflowConfig(ProjectWorkflowConfig projectWorkflowConfig) throws Exception;
  

  public void removeProjectWorkflowConfig(Long id) throws Exception;

 
  public List<ProjectWorkflowConfig> getProjectWorkflowConfigs() throws Exception;
  

  public ProjectWorkflowConfig getProjectWorkflowConfig(Long id) throws Exception;
  

  public List<ProjectWorkflowConfig> findProjectWorkflowConfigsForQuery(String query) throws Exception;
  

  public WorkflowBinDefinition addWorkflowBinDefinition(WorkflowBinDefinition workflowBinDefinition) throws Exception;


  public void updateWorkflowBinDefinition(WorkflowBinDefinition workflowBinDefinition) throws Exception;
  

  public void removeWorkflowBinDefinition(Long id) throws Exception;

 
  public List<WorkflowBinDefinition> getWorkflowBinDefinitions() throws Exception;
  

  public WorkflowBinDefinition getWorkflowBinDefinition(Long id) throws Exception;
  

  public List<WorkflowBinDefinition> findWorkflowBinDefinitionsForQuery(String query) throws Exception;
  
  
  public WorkflowBin addWorkflowBin(WorkflowBin workflowBin) throws Exception;


  public void updateWorkflowBin(WorkflowBin workflowBin) throws Exception;
  

  public void removeWorkflowBin(Long id) throws Exception;

 
  public List<WorkflowBin> getWorkflowBins() throws Exception;
  

  public WorkflowBin getWorkflowBin(Long id) throws Exception;
  

  public List<WorkflowBin> findWorkflowBinsForQuery(String query) throws Exception;
  
  public Worklist addWorklist(Worklist worklist) throws Exception;


  public void updateWorklist(Worklist worklist) throws Exception;
  

  public void removeWorklist(Long id) throws Exception;
  

  public Worklist getWorklist(Long id) throws Exception;
  

  public WorklistList findWorklistsForQuery(String query, PfsParameter pfs) throws Exception; 
  
  public Checklist addChecklist(Checklist worklist) throws Exception;


  public void updateChecklist(Checklist worklist) throws Exception;
  

  public void removeChecklist(Long id) throws Exception;
  

  public Checklist getChecklist(Long id) throws Exception;
  

  public ChecklistList findChecklistsForQuery(String query, PfsParameter pfs) throws Exception; 
  
}