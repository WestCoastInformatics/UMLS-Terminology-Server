/*
 *    Copyright 2019 West Coast Informatics, LLC
 */
package com.wci.umls.server.services;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.wci.umls.server.Project;
import com.wci.umls.server.UserRole;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.ChecklistList;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.StringList;
import com.wci.umls.server.helpers.TrackingRecordList;
import com.wci.umls.server.helpers.WorkflowConfigList;
import com.wci.umls.server.helpers.WorklistList;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.workflow.Checklist;
import com.wci.umls.server.model.workflow.TrackingRecord;
import com.wci.umls.server.model.workflow.WorkflowAction;
import com.wci.umls.server.model.workflow.WorkflowBin;
import com.wci.umls.server.model.workflow.WorkflowBinDefinition;
import com.wci.umls.server.model.workflow.WorkflowConfig;
import com.wci.umls.server.model.workflow.WorkflowEpoch;
import com.wci.umls.server.model.workflow.WorkflowStatus;
import com.wci.umls.server.model.workflow.Worklist;
import com.wci.umls.server.services.handlers.WorkflowActionHandler;

/**
 * Generically represents a service for performing workflow operations.
 */
public interface WorkflowService extends ContentService {

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
   * @param project the project
   * @param worklist the worklist
   * @param userName the user name
   * @param role the role
   * @param action the action
   * @return the worklist
   * @throws Exception the exception
   */
  public Worklist performWorkflowAction(Project project, Worklist worklist,
    String userName, UserRole role, WorkflowAction action) throws Exception;

  /**
   * Returns the workflow handler for path.
   *
   * @param workflowPath the workflow path
   * @return the workflow handler for path
   * @throws Exception the exception
   */
  public WorkflowActionHandler getWorkflowHandlerForPath(String workflowPath)
    throws Exception;

  /**
   * Returns the workflow handlers.
   *
   * @return the workflow handlers
   * @throws Exception the exception
   */
  public Set<WorkflowActionHandler> getWorkflowHandlers() throws Exception;

  /**
   * Find tracking records for query. Typically this would be accessed also with
   * a worklist or workflowBin parameter in the query.
   *
   * @param project the project
   * @param query the query
   * @param pfs the pfs
   * @return the tracking record list
   * @throws Exception the exception
   */
  public TrackingRecordList findTrackingRecords(Project project, String query,
    PfsParameter pfs) throws Exception;

  /**
   * Find tracking records for a concept id.
   *
   * @param project the project
   * @param concept the concept
   * @param query the query
   * @param pfs the pfs
   * @return the tracking record list
   * @throws Exception the exception
   */
  public TrackingRecordList findTrackingRecordsForConcept(Project project,
    Concept concept, String query, PfsParameter pfs) throws Exception;

  /**
   * Handle lazy init.
   *
   * @param record the record
   */
  public void handleLazyInit(TrackingRecord record);

  /**
   * Handle lazy init.
   *
   * @param worklist the worklist
   */
  public void handleLazyInit(Worklist worklist);

  /**
   * Handle lazy init.
   *
   * @param config the config
   */
  public void handleLazyInit(WorkflowConfig config);

  /**
   * Handle lazy init.
   *
   * @param definition the definition
   */
  public void handleLazyInit(WorkflowBinDefinition definition);

  /**
   * Add workflow epoch.
   *
   * @param workflowEpoch the workflow epoch
   * @return the workflow epoch
   * @throws Exception the exception
   */
  public WorkflowEpoch addWorkflowEpoch(WorkflowEpoch workflowEpoch)
    throws Exception;

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
   * @param project the project
   * @return the workflow epochs
   * @throws Exception the exception
   */
  public List<WorkflowEpoch> getWorkflowEpochs(Project project)
    throws Exception;

  /**
   * Gets the workflow epoch.
   *
   * @param id the id
   * @return the workflow epoch
   * @throws Exception the exception
   */
  public WorkflowEpoch getWorkflowEpoch(Long id) throws Exception;

  /**
   * Add project workflow config.
   *
   * @param WorkflowConfig the project workflow config
   * @return the project workflow config
   * @throws Exception the exception
   */
  public WorkflowConfig addWorkflowConfig(WorkflowConfig WorkflowConfig)
    throws Exception;

  /**
   * Update project workflow config.
   *
   * @param WorkflowConfig the project workflow config
   * @throws Exception the exception
   */
  public void updateWorkflowConfig(WorkflowConfig WorkflowConfig)
    throws Exception;

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
   * @param project the project
   * @return the project workflow configs
   * @throws Exception the exception
   */
  public List<WorkflowConfig> getWorkflowConfigs(Project project)
    throws Exception;

  /**
   * Gets the project workflow config.
   *
   * @param id the id
   * @return the project workflow config
   * @throws Exception the exception
   */
  public WorkflowConfig getWorkflowConfig(Long id) throws Exception;

  /**
   * Find workflow configs.
   *
   * @param projectId the project id
   * @param query the query
   * @param pfs the pfs
   * @return the workflow config list
   * @throws Exception the exception
   */
  public WorkflowConfigList findWorkflowConfigs(Long projectId, String query,
    PfsParameter pfs) throws Exception;

  /**
   * Add workflow bin definition.
   *
   * @param workflowBinDefinition the workflow bin definition
   * @return the workflow bin definition
   * @throws Exception the exception
   */
  public WorkflowBinDefinition addWorkflowBinDefinition(
    WorkflowBinDefinition workflowBinDefinition) throws Exception;

  /**
   * Update workflow bin definition.
   *
   * @param workflowBinDefinition the workflow bin definition
   * @throws Exception the exception
   */
  public void updateWorkflowBinDefinition(
    WorkflowBinDefinition workflowBinDefinition) throws Exception;

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
   * @param project the project
   * @param type the type
   * @return the workflow bin definitions
   * @throws Exception the exception
   */
  public List<WorkflowBinDefinition> getWorkflowBinDefinitions(Project project,
    String type) throws Exception;

  /**
   * Gets the workflow bin definition.
   *
   * @param id the id
   * @return the workflow bin definition
   * @throws Exception the exception
   */
  public WorkflowBinDefinition getWorkflowBinDefinition(Long id)
    throws Exception;

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
   * @param cascade cascade flag
   * @throws Exception the exception
   */
  public void removeWorkflowBin(Long id, boolean cascade) throws Exception;

  /**
   * Gets the workflow bins.
   *
   * @param project the project
   * @param type the type
   * @return the workflow bins
   * @throws Exception the exception
   */
  public List<WorkflowBin> getWorkflowBins(Project project, String type)
    throws Exception;

  /**
   * Gets the workflow bin.
   *
   * @param id the id
   * @return the workflow bin
   * @throws Exception the exception
   */
  public WorkflowBin getWorkflowBin(Long id) throws Exception;

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
   * @param cascade cascade flag
   * @throws Exception the exception
   */
  public void removeWorklist(Long id, boolean cascade) throws Exception;

  /**
   * Gets the worklist.
   *
   * @param id the id
   * @return the worklist
   * @throws Exception the exception
   */
  public Worklist getWorklist(Long id) throws Exception;

  /**
   * Gets the worklists.
   *
   * @param project the project
   * @param bin the bin
   * @return the worklist
   * @throws Exception the exception
   */
  public List<Worklist> getWorklists(Project project, WorkflowBin bin)
    throws Exception;

  /**
   * Find worklists for query.
   *
   * @param project the project
   * @param query the query
   * @param pfs the pfs
   * @return the worklist list
   * @throws Exception the exception
   */
  public WorklistList findWorklists(Project project, String query,
    PfsParameter pfs) throws Exception;

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
   * @param checklist the checklist
   * @throws Exception the exception
   */
  public void updateChecklist(Checklist checklist) throws Exception;

  /**
   * Remove checklist.
   *
   * @param id the id
   * @param cascade cascade flag
   * @throws Exception the exception
   */
  public void removeChecklist(Long id, boolean cascade) throws Exception;

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
   * @param project project
   * @param query the query
   * @param pfs the pfs
   *
   * @return the checklist list
   * @throws Exception the exception
   */
  public ChecklistList findChecklists(Project project, String query,
    PfsParameter pfs) throws Exception;

  /**
   * Returns the workflow config.
   *
   * @param project the project
   * @param type the type
   * @return the workflow config
   * @throws Exception the exception
   */
  public WorkflowConfig getWorkflowConfig(Project project, String type)
    throws Exception;

  /**
   * Returns the current workflow epoch.
   *
   * @param project the project
   * @return the current workflow epoch
   * @throws Exception the exception
   */
  public WorkflowEpoch getCurrentWorkflowEpoch(Project project)
    throws Exception;

  /**
   * Compute tracking record status.
   *
   * @param record the record
   * @param batch the batch
   * @return the workflow status
   * @throws Exception the exception
   */
  public WorkflowStatus computeTrackingRecordStatus(TrackingRecord record,
    Boolean batch) throws Exception;

  /**
   * Returns the concept id worklist name map.
   *
   * @param projectf the projectf
   * @return the concept id worklist name map
   * @throws Exception the exception
   */
  public Map<Long, String> getConceptIdWorklistNameMap(Project projectf)
    throws Exception;

  /**
   * Lookup tracking record concepts.
   *
   * @param record the record
   * @throws Exception the exception
   */
  public void lookupTrackingRecordConcepts(TrackingRecord record)
    throws Exception;

  /**
   * Handle lazy init.
   *
   * @param checklist the checklist
   */
  public void handleLazyInit(Checklist checklist);

  /**
   * Start process.
   *
   * @param projectId the project id
   * @param process the process
   * @throws Exception the exception
   */
  public void startProcess(Long projectId, String process) throws Exception;

  /**
   * Finish process.
   *
   * @param projectId the project id
   * @param process the process
   * @throws Exception the exception
   */
  public void finishProcess(Long projectId, String process) throws Exception;

  /**
   * Returns the process progress status.
   *
   * @param projectId the project id
   * @param process the process
   * @return the process progress status
   * @throws Exception the exception
   */
  public Boolean getProcessProgressStatus(Long projectId, String process)
    throws Exception;

  /**
   * Sets the process validation result.
   *
   * @param projectId the project id
   * @param process the process
   * @param validationResult the validation result
   * @throws Exception the exception
   */
  public void setProcessValidationResult(Long projectId, String process,
    ValidationResult validationResult) throws Exception;

  /**
   * Returns the process validation result.
   *
   * @param projectId the project id
   * @param process the process
   * @return the process validation result
   * @throws Exception the exception
   */
  public ValidationResult getProcessValidationResult(Long projectId, String process)
    throws Exception;

  /**
   * Removes the process validation result.
   *
   * @param projectId the project id
   * @param process the process
   * @throws Exception the exception
   */
  public void removeProcessValidationResult(Long projectId, String process)
    throws Exception;


}