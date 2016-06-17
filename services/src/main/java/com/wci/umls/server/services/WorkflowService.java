/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.services;

import java.util.List;
import java.util.Set;

import com.wci.umls.server.User;
import com.wci.umls.server.UserRole;
import com.wci.umls.server.helpers.ChecklistList;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.StringList;
import com.wci.umls.server.helpers.TrackingRecordList;
import com.wci.umls.server.helpers.WorklistList;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.workflow.Checklist;
import com.wci.umls.server.model.workflow.WorkflowConfig;
import com.wci.umls.server.model.workflow.TrackingRecord;
import com.wci.umls.server.model.workflow.WorkflowAction;
import com.wci.umls.server.model.workflow.WorkflowBin;
import com.wci.umls.server.model.workflow.WorkflowBinDefinition;
import com.wci.umls.server.model.workflow.WorkflowEpoch;
import com.wci.umls.server.model.workflow.Worklist;
import com.wci.umls.server.services.handlers.WorkflowActionHandler;



/**
 * Generically represents a service for performing workflow operations.
 */
public interface WorkflowService extends RootService, ProjectService {

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
  

  /**
   * Add workflow epoch.
   *
   * @param workflowEpoch the workflow epoch
   * @return the workflow epoch
   * @throws Exception the exception
   */
  public WorkflowEpoch addWorkflowEpoch(WorkflowEpoch workflowEpoch) throws Exception;


  /**
   * Update workflow epoch.
   *
   * @param workflowEpoch the workflow epoch
   * @throws Exception the exception
   */
  public void updateWorkflowEpoch(WorkflowEpoch workflowEpoch) throws Exception;
  

  /**
   * Remove workflow epoch.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeWorkflowEpoch(Long id) throws Exception;

 
  /**
   * Gets the workflow epochs.
   *
   * @return the workflow epochs
   * @throws Exception the exception
   */
  public List<WorkflowEpoch> getWorkflowEpochs() throws Exception;
  

  /**
   * Gets the workflow epoch.
   *
   * @param id the id
   * @return the workflow epoch
   * @throws Exception the exception
   */
  public WorkflowEpoch getWorkflowEpoch(Long id) throws Exception;
  

  /**
   * Find workflow epochs for query.
   *
   * @param query the query
   * @return the list
   * @throws Exception the exception
   */
  public List<WorkflowEpoch> findWorkflowEpochsForQuery(String query) throws Exception;
  

  /**
   * Add project workflow config.
   *
   * @param WorkflowConfig the project workflow config
   * @return the project workflow config
   * @throws Exception the exception
   */
  public WorkflowConfig addWorkflowConfig(WorkflowConfig WorkflowConfig) throws Exception;


  /**
   * Update project workflow config.
   *
   * @param WorkflowConfig the project workflow config
   * @throws Exception the exception
   */
  public void updateWorkflowConfig(WorkflowConfig WorkflowConfig) throws Exception;
  

  /**
   * Remove project workflow config.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeWorkflowConfig(Long id) throws Exception;

 
  /**
   * Gets the project workflow configs.
   *
   * @return the project workflow configs
   * @throws Exception the exception
   */
  public List<WorkflowConfig> getWorkflowConfigs() throws Exception;
  

  /**
   * Gets the project workflow config.
   *
   * @param id the id
   * @return the project workflow config
   * @throws Exception the exception
   */
  public WorkflowConfig getWorkflowConfig(Long id) throws Exception;
  

  /**
   * Find project workflow configs for query.
   *
   * @param query the query
   * @return the list
   * @throws Exception the exception
   */
  public List<WorkflowConfig> findWorkflowConfigsForQuery(String query) throws Exception;
  

  /**
   * Add workflow bin definition.
   *
   * @param workflowBinDefinition the workflow bin definition
   * @return the workflow bin definition
   * @throws Exception the exception
   */
  public WorkflowBinDefinition addWorkflowBinDefinition(WorkflowBinDefinition workflowBinDefinition) throws Exception;


  /**
   * Update workflow bin definition.
   *
   * @param workflowBinDefinition the workflow bin definition
   * @throws Exception the exception
   */
  public void updateWorkflowBinDefinition(WorkflowBinDefinition workflowBinDefinition) throws Exception;
  

  /**
   * Remove workflow bin definition.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeWorkflowBinDefinition(Long id) throws Exception;

 
  /**
   * Gets the workflow bin definitions.
   *
   * @return the workflow bin definitions
   * @throws Exception the exception
   */
  public List<WorkflowBinDefinition> getWorkflowBinDefinitions() throws Exception;
  

  /**
   * Gets the workflow bin definition.
   *
   * @param id the id
   * @return the workflow bin definition
   * @throws Exception the exception
   */
  public WorkflowBinDefinition getWorkflowBinDefinition(Long id) throws Exception;
  

  /**
   * Find workflow bin definitions for query.
   *
   * @param query the query
   * @return the list
   * @throws Exception the exception
   */
  public List<WorkflowBinDefinition> findWorkflowBinDefinitionsForQuery(String query) throws Exception;
  
  
  /**
   * Add workflow bin.
   *
   * @param workflowBin the workflow bin
   * @return the workflow bin
   * @throws Exception the exception
   */
  public WorkflowBin addWorkflowBin(WorkflowBin workflowBin) throws Exception;


  /**
   * Update workflow bin.
   *
   * @param workflowBin the workflow bin
   * @throws Exception the exception
   */
  public void updateWorkflowBin(WorkflowBin workflowBin) throws Exception;
  

  /**
   * Remove workflow bin.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeWorkflowBin(Long id) throws Exception;

 
  /**
   * Gets the workflow bins.
   *
   * @return the workflow bins
   * @throws Exception the exception
   */
  public List<WorkflowBin> getWorkflowBins() throws Exception;
  

  /**
   * Gets the workflow bin.
   *
   * @param id the id
   * @return the workflow bin
   * @throws Exception the exception
   */
  public WorkflowBin getWorkflowBin(Long id) throws Exception;
  

  /**
   * Find workflow bins for query.
   *
   * @param query the query
   * @return the list
   * @throws Exception the exception
   */
  public List<WorkflowBin> findWorkflowBinsForQuery(String query) throws Exception;
  
  /**
   * Add worklist.
   *
   * @param worklist the worklist
   * @return the worklist
   * @throws Exception the exception
   */
  public Worklist addWorklist(Worklist worklist) throws Exception;


  /**
   * Update worklist.
   *
   * @param worklist the worklist
   * @throws Exception the exception
   */
  public void updateWorklist(Worklist worklist) throws Exception;
  

  /**
   * Remove worklist.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeWorklist(Long id) throws Exception;
  

  /**
   * Gets the worklist.
   *
   * @param id the id
   * @return the worklist
   * @throws Exception the exception
   */
  public Worklist getWorklist(Long id) throws Exception;
  

  /**
   * Find worklists for query.
   *
   * @param query the query
   * @param pfs the pfs
   * @return the worklist list
   * @throws Exception the exception
   */
  public WorklistList findWorklistsForQuery(String query, PfsParameter pfs) throws Exception; 
  
  /**
   * Add checklist.
   *
   * @param worklist the worklist
   * @return the checklist
   * @throws Exception the exception
   */
  public Checklist addChecklist(Checklist worklist) throws Exception;


  /**
   * Update checklist.
   *
   * @param worklist the worklist
   * @throws Exception the exception
   */
  public void updateChecklist(Checklist worklist) throws Exception;
  

  /**
   * Remove checklist.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeChecklist(Long id) throws Exception;
  

  /**
   * Gets the checklist.
   *
   * @param id the id
   * @return the checklist
   * @throws Exception the exception
   */
  public Checklist getChecklist(Long id) throws Exception;
  

  /**
   * Find checklists for query.
   *
   * @param query the query
   * @param pfs the pfs
   * @return the checklist list
   * @throws Exception the exception
   */
  public ChecklistList findChecklistsForQuery(String query, PfsParameter pfs) throws Exception; 
  
}