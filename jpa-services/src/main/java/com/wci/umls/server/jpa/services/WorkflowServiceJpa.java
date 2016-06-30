package com.wci.umls.server.jpa.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.NoResultException;

import org.apache.log4j.Logger;

import com.wci.umls.server.Project;
import com.wci.umls.server.UserRole;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.ChecklistList;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.StringList;
import com.wci.umls.server.helpers.TrackingRecordList;
import com.wci.umls.server.helpers.WorklistList;
import com.wci.umls.server.jpa.helpers.ChecklistListJpa;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.helpers.TrackingRecordListJpa;
import com.wci.umls.server.jpa.helpers.WorklistListJpa;
import com.wci.umls.server.jpa.worfklow.ChecklistJpa;
import com.wci.umls.server.jpa.worfklow.TrackingRecordJpa;
import com.wci.umls.server.jpa.worfklow.WorkflowBinDefinitionJpa;
import com.wci.umls.server.jpa.worfklow.WorkflowBinJpa;
import com.wci.umls.server.jpa.worfklow.WorkflowConfigJpa;
import com.wci.umls.server.jpa.worfklow.WorkflowEpochJpa;
import com.wci.umls.server.jpa.worfklow.WorklistJpa;
import com.wci.umls.server.model.workflow.Checklist;
import com.wci.umls.server.model.workflow.TrackingRecord;
import com.wci.umls.server.model.workflow.WorkflowAction;
import com.wci.umls.server.model.workflow.WorkflowBin;
import com.wci.umls.server.model.workflow.WorkflowBinDefinition;
import com.wci.umls.server.model.workflow.WorkflowBinType;
import com.wci.umls.server.model.workflow.WorkflowConfig;
import com.wci.umls.server.model.workflow.WorkflowEpoch;
import com.wci.umls.server.model.workflow.Worklist;
import com.wci.umls.server.services.WorkflowService;
import com.wci.umls.server.services.handlers.SearchHandler;
import com.wci.umls.server.services.handlers.WorkflowActionHandler;

/**
 * Workflow Service JPA implementation
 */
public class WorkflowServiceJpa extends ContentServiceJpa implements
    WorkflowService {

  /** The workflow action handlers. */
  static Map<String, WorkflowActionHandler> workflowHandlerMap =
      new HashMap<>();

  static {
    try {
      if (config == null)
        config = ConfigUtility.getConfigProperties();
      final String key = "workflow.action.handler";
      for (final String handlerName : config.getProperty(key).split(",")) {
        if (handlerName.isEmpty())
          continue;
        // Add handlers to map
        final WorkflowActionHandler handlerService =
            ConfigUtility.newStandardHandlerInstanceWithConfiguration(key,
                handlerName, WorkflowActionHandler.class);
        workflowHandlerMap.put(handlerName, handlerService);
      }
      if (!workflowHandlerMap.containsKey(ConfigUtility.DEFAULT)) {
        throw new Exception("workflow.action.handler." + ConfigUtility.DEFAULT
            + " expected and does not exist.");
      }
    } catch (Exception e) {
      Logger.getLogger(WorkflowServiceJpa.class).error(
          "Failed to initialize workflow.action.handler - serious error", e);
      workflowHandlerMap = null;
    }
  }

  /**
   * Instantiates a new workflow service.
   *
   * @throws Exception the exception
   */
  public WorkflowServiceJpa() throws Exception {
    super();

    if (workflowHandlerMap == null) {
      throw new Exception(
          "Workflow action handler did not properly initialize, serious error.");
    }

  }

  @Override
  public TrackingRecord getTrackingRecord(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - get tracking record " + id);
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
    // tbd

  }

  @Override
  public void removeTrackingRecord(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - remove tracking record " + id);
    // Remove the component
    removeHasLastModified(id, TrackingRecordJpa.class);

  }

  @Override
  public TrackingRecordList findTrackingRecordsForQuery(String query,
    PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - find tracking records for query " + query);
    TrackingRecordList results = new TrackingRecordListJpa();
    final SearchHandler searchHandler = getSearchHandler(null);
    final int[] totalCt = new int[1];
    final List<TrackingRecordJpa> luceneResults =
        searchHandler.getQueryResults(null, null, "", query, "",
            TrackingRecordJpa.class, TrackingRecordJpa.class,
            pfs, totalCt, manager);
    results.setTotalCount(totalCt[0]);
    for (final TrackingRecordJpa trackingRecord : luceneResults) {
      results.getObjects().add(trackingRecord);
    }
    return results;
  }

  @Override
  public void handleLazyInit(TrackingRecord record) {
    // TODO
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
  public WorkflowEpoch getCurrentWorkflowEpoch(Project project) throws Exception {
    Logger.getLogger(getClass()).debug("Workflow Service - add workflow epoch ");
    
    PfsParameter pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(1);
    // TODO translated into obsolete:false and no obsolete field pfs.setActiveOnly(true);
    pfs.setSortField("name");
    pfs.setAscending(false);
    
    return findWorkflowEpochsForQuery("projectId:" + project.getId(), pfs).get(0);
  }

  @Override
  public void updateWorkflowEpoch(WorkflowEpoch workflowEpoch) throws Exception {
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
        "Workflow Service - get workflow epochs ");
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
    Logger.getLogger(getClass()).debug(
        "Workflow Service - get workflow epoch " + id);
    return getHasLastModified(id, WorkflowEpochJpa.class);
  }

  @Override
  public List<WorkflowEpoch> findWorkflowEpochsForQuery(String query, PfsParameter pfs)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - find workflow epochs for query " + query);
    List<WorkflowEpoch> results = new ArrayList<>();
    final SearchHandler searchHandler = getSearchHandler(null);
    final int[] totalCt = new int[1];
    final List<WorkflowEpochJpa> luceneResults =
        searchHandler.getQueryResults(null, null, "", query, "",
            WorkflowEpochJpa.class, WorkflowEpochJpa.class,
            pfs, totalCt, manager);
    for (final WorkflowEpoch epoch : luceneResults) {
      results.add(epoch);
    }
    return results;
  }

  @Override
  public WorkflowConfig addWorkflowConfig(WorkflowConfig workflowConfig)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - add project workflow config "
            + workflowConfig.toString());

    // Add component
    WorkflowConfig config = addHasLastModified(workflowConfig);

    // do not inform listeners
    return config;
  }

  @Override
  public void updateWorkflowConfig(WorkflowConfig workflowConfig)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - update project workflow config " + workflowConfig);

    // update component
    updateHasLastModified(workflowConfig);

  }

  @Override
  public void removeWorkflowConfig(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - remove project workflow config " + id);
    // Remove the component
    removeHasLastModified(id, WorkflowConfigJpa.class);
  }

  @Override
  public List<WorkflowConfig> getWorkflowConfigs() throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - get project workflow configs ");
    final javax.persistence.Query query =
        manager.createQuery("select a from WorkflowConfigJpa a");

    try {
      @SuppressWarnings("unchecked")
      final List<WorkflowConfig> m = query.getResultList();
      return m;

    } catch (NoResultException e) {
      return null;
    }
  }

  @Override
  public WorkflowConfig getWorkflowConfig(Long projectId, WorkflowBinType type)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - get project workflow config " + projectId + ", "
            + type);
    final javax.persistence.Query query =
        manager.createQuery("select a from WorkflowConfigJpa a where "
            + "project.id = :projectId and type = :type");
    try {
      query.setParameter("projectId", projectId);
      query.setParameter("type", type);
      final WorkflowConfig m = (WorkflowConfig) query.getSingleResult();
      return m;

    } catch (NoResultException e) {
      return null;
    }
  }

  @Override
  public WorkflowConfig getWorkflowConfig(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - get project workflow config " + id);
    return getHasLastModified(id, WorkflowConfigJpa.class);
  }

  @Override
  public List<WorkflowConfig> findWorkflowConfigsForQuery(String query)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - find project workflow config for query " + query);
    List<WorkflowConfig> results = new ArrayList<>();
    final SearchHandler searchHandler = getSearchHandler(null);
    final int[] totalCt = new int[1];
    final List<WorkflowConfigJpa> luceneResults =
        searchHandler.getQueryResults(null, null, "", query, "",
            WorkflowConfigJpa.class, WorkflowConfigJpa.class,
            new PfsParameterJpa(), totalCt, manager);
    for (final WorkflowConfig epoch : luceneResults) {
      results.add(epoch);
    }
    return results;
  }

  @Override
  public WorkflowBinDefinition addWorkflowBinDefinition(
    WorkflowBinDefinition workflowBinDefinition) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - add workflow bin definition "
            + workflowBinDefinition.toString());

    // Add component
    WorkflowBinDefinition def = addHasLastModified(workflowBinDefinition);

    // do not inform listeners
    return def;
  }

  @Override
  public void updateWorkflowBinDefinition(
    WorkflowBinDefinition workflowBinDefinition) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - update workflow bin definition "
            + workflowBinDefinition);

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
        "Workflow Service - get workflow bin definitions ");
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
    Logger.getLogger(getClass()).debug(
        "Workflow Service - get workflow bin definition " + id);
    return getHasLastModified(id, WorkflowBinDefinitionJpa.class);
  }

  @Override
  public List<WorkflowBinDefinition> findWorkflowBinDefinitionsForQuery(
    String query) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - find workflow bin definitions for query " + query);
    List<WorkflowBinDefinition> results = new ArrayList<>();
    final SearchHandler searchHandler = getSearchHandler(null);
    final int[] totalCt = new int[1];
    final List<WorkflowBinDefinitionJpa> luceneResults =
        searchHandler.getQueryResults(null, null, "", query, "",
            WorkflowBinDefinitionJpa.class, WorkflowBinDefinitionJpa.class,
            new PfsParameterJpa(), totalCt, manager);
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
  public void removeWorkflowBin(Long id, boolean cascade) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - remove workflow bin " + id + ", " + cascade);
    // if cascade, remove tracking records before removing workflow bin

    // Manage transaction
    boolean origTpo = getTransactionPerOperation();
    if (origTpo) {
      setTransactionPerOperation(false);
      beginTransaction();
    }

    WorkflowBin workflowBin = getWorkflowBin(id);
    if (cascade) {
      if (getTransactionPerOperation())
        throw new Exception(
            "Unable to remove workflow bin, transactionPerOperation must be disabled to perform cascade remove.");
      
      for (final TrackingRecord record : workflowBin.getTrackingRecords()) {
        removeTrackingRecord(record.getId());
      }
    }


    // Remove the component
    workflowBin = removeHasLastModified(id, WorkflowBinJpa.class);

    // Manage transaction
    if (origTpo) {
      commit();
      setTransactionPerOperation(origTpo);
    }

    /*if (listenersEnabled) {
      for (final WorkflowListener listener : workflowListeners) {
        listener.refsetChanged(workflowBin, WorkflowListener.Action.REMOVE);
      }
    }*/
  }

  @Override
  public List<WorkflowBin> getWorkflowBins() throws Exception {
    Logger.getLogger(getClass()).debug("Workflow Service - get workflow bins ");
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
    Logger.getLogger(getClass()).debug(
        "Workflow Service - get workflow bin " + id);
    return getHasLastModified(id, WorkflowBinJpa.class);
  }

  @Override
  public List<WorkflowBin> findWorkflowBinsForQuery(String query)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - find workflow bins for query " + query);
    List<WorkflowBin> results = new ArrayList<>();
    final SearchHandler searchHandler = getSearchHandler(null);
    final int[] totalCt = new int[1];
    final List<WorkflowBinJpa> luceneResults =
        searchHandler.getQueryResults(null, null, "", query, "",
            WorkflowBinJpa.class, WorkflowBinJpa.class, new PfsParameterJpa(),
            totalCt, manager);
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
  public void removeWorklist(Long id, boolean cascade) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - remove worklist " + id + ", " + cascade);
    // if cascade, remove tracking records before removing worklist
    if (cascade) {
      Worklist worklist = getWorklist(id);
      for (TrackingRecord record : worklist.getTrackingRecords()) {
        removeHasLastModified(record.getId(), TrackingRecordJpa.class);
      }
    }
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
    Logger.getLogger(getClass()).debug(
        "Workflow Service - find worklists for query " + query);
    WorklistList results = new WorklistListJpa();
    final SearchHandler searchHandler = getSearchHandler(null);
    final int[] totalCt = new int[1];
    final List<WorklistJpa> luceneResults =
        searchHandler.getQueryResults(null, null, "", query, "",
            WorklistJpa.class, WorklistJpa.class, pfs,
            totalCt, manager);
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
  public void removeChecklist(Long id, boolean cascade) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - remove checklist " + id + ", " + cascade);
    // if cascade, remove tracking records before removing checklist
    if (cascade) {
      Checklist checklist = getChecklist(id);
      for (TrackingRecord record : checklist.getTrackingRecords()) {
        removeHasLastModified(record.getId(), TrackingRecordJpa.class);
      }
    }
    // Remove the component
    removeHasLastModified(id, ChecklistJpa.class);
  }

  
  @Override
  public Checklist getChecklist(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Workflow Service - get checklist " + id);
    return getHasLastModified(id, ChecklistJpa.class);
  }

  @Override
  public ChecklistList findChecklistsForQuery(Project project, String query,
    PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - find checklists for query " + query);

    final StringBuilder sb = new StringBuilder();
    if (query != null && !query.equals("")) {
      sb.append(query).append(" AND ");
    }

    ChecklistList results = new ChecklistListJpa();
    final SearchHandler searchHandler = getSearchHandler(null);
    final int[] totalCt = new int[1];
    final List<ChecklistJpa> luceneResults =
        searchHandler.getQueryResults(null, null, "", sb.toString(), "",
            ChecklistJpa.class, ChecklistJpa.class, pfs, totalCt, manager);
    results.setTotalCount(totalCt[0]);
    for (final ChecklistJpa checklist : luceneResults) {
      results.getObjects().add(checklist);
    }
    return results;
  }

  /* see superclass */
  @Override
  public StringList getWorkflowPaths() {
    Logger.getLogger(getClass()).debug("Workflow Service - get workflow paths");
    final List<String> paths = new ArrayList<>();
    for (final String path : workflowHandlerMap.keySet()) {
      paths.add(path);
    }
    Collections.sort(paths);
    final StringList list = new StringList();
    list.setTotalCount(paths.size());
    list.setObjects(paths);
    return list;
  }

  /* see superclass */
  @Override
  public WorkflowActionHandler getWorkflowHandlerForPath(String path)
    throws Exception {
    final WorkflowActionHandler handler = workflowHandlerMap.get(path);
    if (handler == null) {
      throw new Exception("Unable to find workflow handler for path " + path);
    }
    return handler;
  }

  /* see superclass */
  @Override
  public Set<WorkflowActionHandler> getWorkflowHandlers() throws Exception {
    return new HashSet<>(workflowHandlerMap.values());
  }

  @Override
  public Worklist performWorkflowAction(Project project, Worklist worklist,
    String userName, UserRole role, WorkflowAction action) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - perform workflow action " + action + ", "
            + project.getId() + ", " + worklist.getId() + ", " + userName
            + ", " + role);

    // Obtain the handler
    final WorkflowActionHandler handler =
        getWorkflowHandlerForPath(project.getWorkflowPath());
    // Validate the action
    final ValidationResult result =
        handler.validateWorkflowAction(project, worklist, getUser(userName),
            role, action, this);
    if (!result.isValid()) {
      Logger.getLogger(getClass()).error("  validationResult = " + result);
      throw new LocalException(result.getErrors().iterator().next());
    }
    // Perform the action
    Worklist r =
        handler.performWorkflowAction(project, worklist, getUser(userName),
            role, action, this);
    return r;
  }
}
