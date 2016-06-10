package com.wci.umls.server.jpa.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.NoResultException;

import org.apache.log4j.Logger;

import com.wci.umls.server.User;
import com.wci.umls.server.UserRole;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.StringList;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.worfklow.ChecklistJpa;
import com.wci.umls.server.jpa.worfklow.ChecklistListJpa;
import com.wci.umls.server.jpa.worfklow.ProjectWorkflowConfigJpa;
import com.wci.umls.server.jpa.worfklow.TrackingRecordJpa;
import com.wci.umls.server.jpa.worfklow.TrackingRecordListJpa;
import com.wci.umls.server.jpa.worfklow.WorkflowBinDefinitionJpa;
import com.wci.umls.server.jpa.worfklow.WorkflowBinJpa;
import com.wci.umls.server.jpa.worfklow.WorkflowEpochJpa;
import com.wci.umls.server.jpa.worfklow.WorklistJpa;
import com.wci.umls.server.jpa.worfklow.WorklistListJpa;
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
import com.wci.umls.server.services.WorkflowService;
import com.wci.umls.server.services.handlers.SearchHandler;
import com.wci.umls.server.services.handlers.WorkflowActionHandler;

public class WorkflowServiceJpa extends ContentServiceJpa implements WorkflowService {

  public WorkflowServiceJpa() throws Exception {
    super();
    
  }
  
  @Override
  public TrackingRecord getTrackingRecord(Long id) throws Exception {
    Logger.getLogger(getClass()).debug("Workflow Service - get tracking record " + id);
    return getHasLastModified(id, TrackingRecordJpa.class);
  }



  @Override
  public TrackingRecord addTrackingRecord(TrackingRecord trackingRecord)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - add tracking record " + trackingRecord.toString());

    // Add component
    TrackingRecord record = addHasLastModified(trackingRecord);

    // do not inform listeners
    return record;
  }

  @Override
  public void updateTrackingRecord(TrackingRecord trackingRecord)
    throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void removeTrackingRecord(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - remove tracking record " + id);
    // Remove the component
    removeHasLastModified(id, TrackingRecordJpa.class);

  }

  @Override
  public StringList getWorkflowPaths() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TrackingRecord performWorkflowAction(Long refsetId, User user,
    UserRole projectRole, WorkflowAction action) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TrackingRecord performWorkflowAction(Long translationId, User user,
    UserRole projectRole, WorkflowAction action, Concept concept)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public WorkflowActionHandler getWorkflowHandlerForPath(String workflowPat)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<WorkflowActionHandler> getWorkflowHandlers() throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TrackingRecordList findTrackingRecordsForQuery(String query,
    PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).debug("Workflow Service - find tracking records for query " + query);
    TrackingRecordList results = new TrackingRecordListJpa();
    final SearchHandler searchHandler = getSearchHandler(null);
    final int[] totalCt = new int[1];
    final List<TrackingRecordJpa> luceneResults =
        searchHandler.getQueryResults(null, null, "", query, "",
            TrackingRecordJpa.class, TrackingRecordJpa.class, new PfsParameterJpa(), totalCt, manager);
    results.setTotalCount(totalCt[0]);
    for (final TrackingRecordJpa trackingRecord : luceneResults) {
      results.getObjects().add(trackingRecord);
    }
    return results;
  }

  @Override
  public void handleLazyInit(TrackingRecord record) {
    // TODO Auto-generated method stub

  }

  @Override
  public WorkflowEpoch addWorkflowEpoch(WorkflowEpoch workflowEpoch)
    throws Exception {

    Logger.getLogger(getClass()).debug(
        "Workflow Service - add workflow epoch " + workflowEpoch.toString());

    // Add component
    WorkflowEpoch epoch = addHasLastModified(workflowEpoch);

    // do not inform listeners
    return epoch;
  }

  @Override
  public void updateWorkflowEpoch(WorkflowEpoch workflowEpoch)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - update workflow epoch " + workflowEpoch);

    // update component
    updateHasLastModified(workflowEpoch);

  }

  @Override
  public void removeWorkflowEpoch(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - remove workflow epoch " + id);
    // Remove the component
    removeHasLastModified(id, WorkflowEpochJpa.class);
  }

  @Override
  public List<WorkflowEpoch> getWorkflowEpochs() throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - get workflow epochs " );
    final javax.persistence.Query query =
        manager.createQuery("select a from WorkflowEpochJpa a");

    try {
      @SuppressWarnings("unchecked")
      final List<WorkflowEpoch> m = query.getResultList();
      return m;

    } catch (NoResultException e) {
      return null;
    }
  }

  @Override
  public WorkflowEpoch getWorkflowEpoch(Long id) throws Exception {
    Logger.getLogger(getClass()).debug("Workflow Service - get workflow epoch " + id);
    return getHasLastModified(id, WorkflowEpochJpa.class);
  }
  
  @Override
  public List<WorkflowEpoch> findWorkflowEpochsForQuery(String query)
    throws Exception {
    Logger.getLogger(getClass()).debug("Workflow Service - find workflow epochs for query " + query);
    List<WorkflowEpoch> results = new ArrayList<>();
    final SearchHandler searchHandler = getSearchHandler(null);
    final int[] totalCt = new int[1];
    final List<WorkflowEpochJpa> luceneResults =
        searchHandler.getQueryResults(null, null, "", query, "",
            WorkflowEpochJpa.class, WorkflowEpochJpa.class, new PfsParameterJpa(), totalCt, manager);
    for (final WorkflowEpoch epoch : luceneResults) {
      results.add(epoch);
    }
    return results;
  }

  @Override
  public ProjectWorkflowConfig addProjectWorkflowConfig(
    ProjectWorkflowConfig projectWorkflowConfig) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - add project workflow config " + projectWorkflowConfig.toString());

    // Add component
    ProjectWorkflowConfig config = addHasLastModified(projectWorkflowConfig);

    // do not inform listeners
    return config;
  }

  @Override
  public void updateProjectWorkflowConfig(
    ProjectWorkflowConfig projectWorkflowConfig) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - update project workflow config " + projectWorkflowConfig);

    // update component
    updateHasLastModified(projectWorkflowConfig);

  }

  @Override
  public void removeProjectWorkflowConfig(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - remove project workflow config " + id);
    // Remove the component
    removeHasLastModified(id, ProjectWorkflowConfigJpa.class);
  }

  @Override
  public List<ProjectWorkflowConfig> getProjectWorkflowConfigs()
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - get project workflow configs " );
    final javax.persistence.Query query =
        manager.createQuery("select a from ProjectWorkflowConfigJpa a");

    try {
      @SuppressWarnings("unchecked")
      final List<ProjectWorkflowConfig> m = query.getResultList();
      return m;

    } catch (NoResultException e) {
      return null;
    }
  }

  @Override
  public ProjectWorkflowConfig getProjectWorkflowConfig(Long id)
    throws Exception {
    Logger.getLogger(getClass()).debug("Workflow Service - get project workflow config " + id);
    return getHasLastModified(id, ProjectWorkflowConfigJpa.class);
  }

  @Override
  public List<ProjectWorkflowConfig> findProjectWorkflowConfigsForQuery(
    String query) throws Exception {
    Logger.getLogger(getClass()).debug("Workflow Service - find project workflow config for query " + query);
    List<ProjectWorkflowConfig> results = new ArrayList<>();
    final SearchHandler searchHandler = getSearchHandler(null);
    final int[] totalCt = new int[1];
    final List<ProjectWorkflowConfigJpa> luceneResults =
        searchHandler.getQueryResults(null, null, "", query, "",
            ProjectWorkflowConfigJpa.class, ProjectWorkflowConfigJpa.class, new PfsParameterJpa(), totalCt, manager);
    for (final ProjectWorkflowConfig epoch : luceneResults) {
      results.add(epoch);
    }
    return results;
  }

  @Override
  public WorkflowBinDefinition addWorkflowBinDefinition(
    WorkflowBinDefinition workflowBinDefinition) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - add workflow bin definition " + workflowBinDefinition.toString());

    // Add component
    WorkflowBinDefinition def = addHasLastModified(workflowBinDefinition);

    // do not inform listeners
    return def;
  }

  @Override
  public void updateWorkflowBinDefinition(
    WorkflowBinDefinition workflowBinDefinition) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - update workflow bin definition " + workflowBinDefinition);

    // update component
    updateHasLastModified(workflowBinDefinition);

  }

  @Override
  public void removeWorkflowBinDefinition(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - remove workflow bin definition " + id);
    // Remove the component
    removeHasLastModified(id, WorkflowBinDefinitionJpa.class);
  }

  @Override
  public List<WorkflowBinDefinition> getWorkflowBinDefinitions()
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - get workflow bin definitions " );
    final javax.persistence.Query query =
        manager.createQuery("select a from WorkflowBinDefinitionJpa a");

    try {
      @SuppressWarnings("unchecked")
      final List<WorkflowBinDefinition> m = query.getResultList();
      return m;

    } catch (NoResultException e) {
      return null;
    }
  }

  @Override
  public WorkflowBinDefinition getWorkflowBinDefinition(Long id)
    throws Exception {
    Logger.getLogger(getClass()).debug("Workflow Service - get workflow bin definition " + id);
    return getHasLastModified(id, WorkflowBinDefinitionJpa.class);
  }

  @Override
  public List<WorkflowBinDefinition> findWorkflowBinDefinitionsForQuery(
    String query) throws Exception {
    Logger.getLogger(getClass()).debug("Workflow Service - find workflow bin definitions for query " + query);
    List<WorkflowBinDefinition> results = new ArrayList<>();
    final SearchHandler searchHandler = getSearchHandler(null);
    final int[] totalCt = new int[1];
    final List<WorkflowBinDefinitionJpa> luceneResults =
        searchHandler.getQueryResults(null, null, "", query, "",
            WorkflowBinDefinitionJpa.class, WorkflowBinDefinitionJpa.class, new PfsParameterJpa(), totalCt, manager);
    for (final WorkflowBinDefinition def : luceneResults) {
      results.add(def);
    }
    return results;
  }

  @Override
  public WorkflowBin addWorkflowBin(WorkflowBin workflowBin) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - add workflow bin " + workflowBin.toString());

    // Add component
    WorkflowBin bin = addHasLastModified(workflowBin);

    // do not inform listeners
    return bin;
  }

  @Override
  public void updateWorkflowBin(WorkflowBin workflowBin) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - update workflow bin " + workflowBin);

    // update component
    updateHasLastModified(workflowBin);

  }

  @Override
  public void removeWorkflowBin(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - remove workflow bin " + id);
    // Remove the component
    removeHasLastModified(id, WorkflowBinJpa.class);
  }

  @Override
  public List<WorkflowBin> getWorkflowBins() throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - get workflow bins " );
    final javax.persistence.Query query =
        manager.createQuery("select a from WorkflowBinJpa a");

    try {
      @SuppressWarnings("unchecked")
      final List<WorkflowBin> m = query.getResultList();
      return m;

    } catch (NoResultException e) {
      return null;
    }
  }

  @Override
  public WorkflowBin getWorkflowBin(Long id) throws Exception {
    Logger.getLogger(getClass()).debug("Workflow Service - get workflow bin " + id);
    return getHasLastModified(id, WorkflowBinJpa.class);
  }

  @Override
  public List<WorkflowBin> findWorkflowBinsForQuery(String query)
    throws Exception {
    Logger.getLogger(getClass()).debug("Workflow Service - find workflow bins for query " + query);
    List<WorkflowBin> results = new ArrayList<>();
    final SearchHandler searchHandler = getSearchHandler(null);
    final int[] totalCt = new int[1];
    final List<WorkflowBinJpa> luceneResults =
        searchHandler.getQueryResults(null, null, "", query, "",
            WorkflowBinJpa.class, WorkflowBinJpa.class, new PfsParameterJpa(), totalCt, manager);
    for (final WorkflowBin bin : luceneResults) {
      results.add(bin);
    }
    return results;
  }

  @Override
  public Worklist addWorklist(Worklist worklist) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - add worklist " + worklist.toString());

    // Add component
    Worklist list = addHasLastModified(worklist);

    // do not inform listeners
    return list;
  }

  @Override
  public void updateWorklist(Worklist worklist) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - update worklist " + worklist);

    // update component
    updateHasLastModified(worklist);

  }

  @Override
  public void removeWorklist(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - remove worklist " + id);
    // Remove the component
    removeHasLastModified(id, WorklistJpa.class);
  }

  @Override
  public Worklist getWorklist(Long id) throws Exception {
    Logger.getLogger(getClass()).debug("Workflow Service - get worklist " + id);
    return getHasLastModified(id, WorklistJpa.class);
  }

  @Override
  public WorklistList findWorklistsForQuery(String query, PfsParameter pfs)
    throws Exception {
    Logger.getLogger(getClass()).debug("Workflow Service - find worklists for query " + query);
    WorklistList results = new WorklistListJpa();
    final SearchHandler searchHandler = getSearchHandler(null);
    final int[] totalCt = new int[1];
    final List<WorklistJpa> luceneResults =
        searchHandler.getQueryResults(null, null, "", query, "",
            WorklistJpa.class, WorklistJpa.class, new PfsParameterJpa(), totalCt, manager);
    results.setTotalCount(totalCt[0]);
    for (final WorklistJpa worklist : luceneResults) {
      results.getObjects().add(worklist);
    }
    return results;
  }

  @Override
  public Checklist addChecklist(Checklist checklist) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - add checklist " + checklist.toString());

    // Add component
    Checklist list = addHasLastModified(checklist);

    // do not inform listeners
    return list;
  }

  @Override
  public void updateChecklist(Checklist checklist) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - update checklist " + checklist);

    // update component
    updateHasLastModified(checklist);

  }

  @Override
  public void removeChecklist(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - remove checklist " + id);
    // Remove the component
    removeHasLastModified(id, ChecklistJpa.class);
  }

  @Override
  public Checklist getChecklist(Long id) throws Exception {
    Logger.getLogger(getClass()).debug("Workflow Service - get checklist " + id);
    return getHasLastModified(id, ChecklistJpa.class);
  }

  @Override
  public ChecklistList findChecklistsForQuery(String query, PfsParameter pfs)
    throws Exception {
    Logger.getLogger(getClass()).debug("Workflow Service - find checklists for query " + query);
    ChecklistList results = new ChecklistListJpa();
    final SearchHandler searchHandler = getSearchHandler(null);
    final int[] totalCt = new int[1];
    final List<ChecklistJpa> luceneResults =
        searchHandler.getQueryResults(null, null, "", query, "",
            ChecklistJpa.class, ChecklistJpa.class, new PfsParameterJpa(), totalCt, manager);
    results.setTotalCount(totalCt[0]);
    for (final ChecklistJpa checklist : luceneResults) {
      results.getObjects().add(checklist);
    }
    return results;
  }

}
