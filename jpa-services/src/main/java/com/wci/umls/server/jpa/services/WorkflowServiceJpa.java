package com.wci.umls.server.jpa.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
 * Workflow Service JPA implementation.
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

  /* see superclass */
  @Override
  public TrackingRecord getTrackingRecord(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - get tracking record " + id);
    return getHasLastModified(id, TrackingRecordJpa.class);
  }

  /* see superclass */
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

  /* see superclass */
  @Override
  public void updateTrackingRecord(TrackingRecord trackingRecord)
    throws Exception {
    // tbd

  }

  /* see superclass */
  @Override
  public void removeTrackingRecord(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - remove tracking record " + id);
    // Remove the component
    removeHasLastModified(id, TrackingRecordJpa.class);

  }

  /* see superclass */
  @Override
  public TrackingRecordList findTrackingRecords(Project project, String query,
    PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - find tracking records " + project.getId() + ", "
            + query);

    final TrackingRecordList results = new TrackingRecordListJpa();
    final SearchHandler searchHandler = getSearchHandler(null);
    final int[] totalCt = new int[1];
    final List<TrackingRecordJpa> luceneResults =
        searchHandler.getQueryResults(null, null, "",
            composeQuery(project, query), "", TrackingRecordJpa.class,
            TrackingRecordJpa.class, pfs, totalCt, manager);
    results.setTotalCount(totalCt[0]);
    for (final TrackingRecordJpa trackingRecord : luceneResults) {
      handleLazyInit(trackingRecord);
      results.getObjects().add(trackingRecord);
    }
    return results;
  }

  /* see superclass */
  @Override
  public void handleLazyInit(TrackingRecord record) {
    record.getOrigConceptIds().size();
    record.getComponentIds().size();
  }

  /* see superclass */
  @Override
  public void handleLazyInit(Worklist worklist) {
    worklist.getReviewers().size();
    worklist.getAuthors().size();
    worklist.getWorkflowStateHistory().size();
  }

  /* see superclass */
  @Override
  public void handleLazyInit(WorkflowConfig config) {
    for (final WorkflowBinDefinition def : config.getWorkflowBinDefinitions()) {
      handleLazyInit(def);
    }
  }

  /* see superclass */
  @Override
  public void handleLazyInit(WorkflowBinDefinition definition) {
    definition.getWorkflowConfig().getType().toString();
  }

  /* see superclass */
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

  /* see superclass */
  @Override
  public WorkflowEpoch getCurrentWorkflowEpoch(Project project)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Workflow Service - add workflow epoch ");

    String maxName = "";
    WorkflowEpoch maxEpoch = null;
    for (final WorkflowEpoch epoch : getWorkflowEpochs(project)) {
      if (epoch.getName().compareTo(maxName) > 0) {
        maxEpoch = epoch;
      }
    }
    return maxEpoch;
  }

  /* see superclass */
  @Override
  public void updateWorkflowEpoch(WorkflowEpoch workflowEpoch) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - update workflow epoch " + workflowEpoch);

    // update component
    updateHasLastModified(workflowEpoch);

  }

  /* see superclass */
  @Override
  public void removeWorkflowEpoch(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - remove workflow epoch " + id);
    // Remove the component
    removeHasLastModified(id, WorkflowEpochJpa.class);
  }

  /* see superclass */
  @Override
  public WorkflowEpoch getWorkflowEpoch(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - get workflow epoch " + id);
    return getHasLastModified(id, WorkflowEpochJpa.class);
  }

  /* see superclass */
  @Override
  public List<WorkflowEpoch> getWorkflowEpochs(Project project)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - get workflow epochs - " + project.getId());

    final SearchHandler searchHandler = getSearchHandler(null);
    final int[] totalCt = new int[1];
    final List<WorkflowEpochJpa> results =
        searchHandler.getQueryResults(null, null, "",
            composeQuery(project, ""), "", WorkflowEpochJpa.class,
            WorkflowEpochJpa.class, null, totalCt, manager);
    return new ArrayList<WorkflowEpoch>(results);

  }

  /* see superclass */
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

  /* see superclass */
  @Override
  public void updateWorkflowConfig(WorkflowConfig workflowConfig)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - update project workflow config " + workflowConfig);

    // update component
    updateHasLastModified(workflowConfig);

  }

  /* see superclass */
  @Override
  public void removeWorkflowConfig(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - remove project workflow config " + id);
    // Remove the component
    removeHasLastModified(id, WorkflowConfigJpa.class);
  }

  /* see superclass */
  @Override
  public List<WorkflowConfig> getWorkflowConfigs(Project project)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - get project workflow configs - " + project.getId());

    final SearchHandler searchHandler = getSearchHandler(null);
    final int[] totalCt = new int[1];
    final List<WorkflowConfigJpa> results =
        searchHandler.getQueryResults(null, null, "",
            composeQuery(project, ""), "", WorkflowConfigJpa.class,
            WorkflowConfigJpa.class, null, totalCt, manager);
    return new ArrayList<WorkflowConfig>(results);
  }

  /* see superclass */
  @Override
  public WorkflowConfig getWorkflowConfig(Project project, WorkflowBinType type)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - get project workflow config " + project.getId()
            + ", " + type);
    final SearchHandler searchHandler = getSearchHandler(null);
    final int[] totalCt = new int[1];
    final List<WorkflowConfigJpa> results =
        searchHandler.getQueryResults(null, null, "", composeQuery(project, "")
            + " AND type:" + type.toString(), "", WorkflowConfigJpa.class,
            WorkflowConfigJpa.class, null, totalCt, manager);

    if (results.size() == 0) {
      return null;
    } else if (results.size() == 1) {
      return results.get(0);
    } else {
      throw new Exception(
          "Unexpected number of workflow configs for project and type "
              + project.getId() + "," + type);
    }

  }

  /* see superclass */
  @Override
  public WorkflowConfig getWorkflowConfig(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - get project workflow config " + id);
    return getHasLastModified(id, WorkflowConfigJpa.class);
  }

  /* see superclass */
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

  /* see superclass */
  @Override
  public void updateWorkflowBinDefinition(
    WorkflowBinDefinition workflowBinDefinition) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - update workflow bin definition "
            + workflowBinDefinition);

    // update component
    updateHasLastModified(workflowBinDefinition);

  }

  /* see superclass */
  @Override
  public void removeWorkflowBinDefinition(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - remove workflow bin definition " + id);
    // Remove the component
    removeHasLastModified(id, WorkflowBinDefinitionJpa.class);
  }

  /* see superclass */
  @Override
  public List<WorkflowBinDefinition> getWorkflowBinDefinitions(Project project,
    WorkflowBinType type) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - get workflow bin definitions " + project.getId()
            + ", " + type);

    final WorkflowConfig config = this.getWorkflowConfig(project, type);
    return config.getWorkflowBinDefinitions();
  }

  /* see superclass */
  @Override
  public WorkflowBinDefinition getWorkflowBinDefinition(Long id)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - get workflow bin definition " + id);
    return getHasLastModified(id, WorkflowBinDefinitionJpa.class);
  }

  /* see superclass */
  @Override
  public WorkflowBin addWorkflowBin(WorkflowBin workflowBin) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - add workflow bin " + workflowBin.toString());

    // Add component
    WorkflowBin bin = addHasLastModified(workflowBin);

    // do not inform listeners
    return bin;
  }

  /* see superclass */
  @Override
  public void updateWorkflowBin(WorkflowBin workflowBin) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - update workflow bin " + workflowBin);

    // update component
    updateHasLastModified(workflowBin);

  }

  /* see superclass */
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

  }

  /* see superclass */
  @Override
  public List<WorkflowBin> getWorkflowBins(Project project, WorkflowBinType type)
    throws Exception {
    Logger.getLogger(getClass())
        .debug(
            "Workflow Service - get workflow bins " + project.getId() + ", "
                + type);

    final SearchHandler searchHandler = getSearchHandler(null);
    final int[] totalCt = new int[1];
    final List<WorkflowBinJpa> results =
        searchHandler.getQueryResults(null, null, "", composeQuery(project, "")
            + " AND type:" + type, "", WorkflowBinJpa.class,
            WorkflowBinJpa.class, null, totalCt, manager);
    return new ArrayList<WorkflowBin>(results);

  }

  /* see superclass */
  @Override
  public WorkflowBin getWorkflowBin(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - get workflow bin " + id);
    return getHasLastModified(id, WorkflowBinJpa.class);
  }

  /* see superclass */
  @Override
  public Worklist addWorklist(Worklist worklist) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - add worklist " + worklist.toString());

    // Add component
    Worklist list = addHasLastModified(worklist);

    // do not inform listeners
    return list;
  }

  /* see superclass */
  @Override
  public void updateWorklist(Worklist worklist) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - update worklist " + worklist);

    // update component
    updateHasLastModified(worklist);

  }

  /* see superclass */
  @Override
  public void removeWorklist(Long id, boolean cascade) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - remove worklist " + id + ", " + cascade);
    // if cascade, remove tracking records before removing worklist
    // Manage transaction
    boolean origTpo = getTransactionPerOperation();
    if (origTpo) {
      setTransactionPerOperation(false);
      beginTransaction();
    }

    Worklist worklist = getWorklist(id);
    if (cascade) {
      if (getTransactionPerOperation())
        throw new Exception(
            "Unable to remove worklist, transactionPerOperation must be disabled to perform cascade remove.");

      for (final TrackingRecord record : worklist.getTrackingRecords()) {
        removeTrackingRecord(record.getId());
      }
    }

    // Remove the component
    worklist = removeHasLastModified(id, WorklistJpa.class);

    // Manage transaction
    if (origTpo) {
      commit();
      setTransactionPerOperation(origTpo);
    }

  }

  /* see superclass */
  @Override
  public Worklist getWorklist(Long id) throws Exception {
    Logger.getLogger(getClass()).debug("Workflow Service - get worklist " + id);
    Worklist worklist = getHasLastModified(id, WorklistJpa.class);
    handleLazyInit(worklist);
    return worklist;
  }

  @Override
  public List<Worklist> getWorklists(Project project, WorkflowBin bin)
    throws Exception {

    return null;
  }

  /* see superclass */
  @Override
  public WorklistList findWorklists(Project project, String query,
    PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - find worklists for query " + query);
    final WorklistList results = new WorklistListJpa();
    final SearchHandler searchHandler = getSearchHandler(null);
    final int[] totalCt = new int[1];
    final List<WorklistJpa> luceneResults =
        searchHandler.getQueryResults(null, null, "",
            composeQuery(project, query), "", WorklistJpa.class,
            WorklistJpa.class, pfs, totalCt, manager);
    results.setTotalCount(totalCt[0]);
    for (final WorklistJpa worklist : luceneResults) {
      handleLazyInit(worklist);
      results.getObjects().add(worklist);
    }
    return results;
  }

  /* see superclass */
  @Override
  public Checklist addChecklist(Checklist checklist) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - add checklist " + checklist.toString());

    // Add component
    Checklist list = addHasLastModified(checklist);

    // do not inform listeners
    return list;
  }

  /* see superclass */
  @Override
  public void updateChecklist(Checklist checklist) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - update checklist " + checklist);

    // update component
    updateHasLastModified(checklist);

  }

  /* see superclass */
  @Override
  public void removeChecklist(Long id, boolean cascade) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - remove checklist " + id + ", " + cascade);
    // if cascade, remove tracking records before removing checklist
    // Manage transaction
    boolean origTpo = getTransactionPerOperation();
    if (origTpo) {
      setTransactionPerOperation(false);
      beginTransaction();
    }

    Checklist checklist = getChecklist(id);
    if (cascade) {
      if (getTransactionPerOperation())
        throw new Exception(
            "Unable to remove checklist, transactionPerOperation must be disabled to perform cascade remove.");

      for (final TrackingRecord record : checklist.getTrackingRecords()) {
        removeTrackingRecord(record.getId());
      }
    }

    // Remove the component
    checklist = removeHasLastModified(id, ChecklistJpa.class);

    // Manage transaction
    if (origTpo) {
      commit();
      setTransactionPerOperation(origTpo);
    }

  }

  /* see superclass */
  @Override
  public Checklist getChecklist(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Workflow Service - get checklist " + id);
    return getHasLastModified(id, ChecklistJpa.class);
  }

  /* see superclass */
  @Override
  public ChecklistList findChecklists(Project project, String query,
    PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Workflow Service - find checklists for query " + query);

    ChecklistList results = new ChecklistListJpa();
    final SearchHandler searchHandler = getSearchHandler(null);
    final int[] totalCt = new int[1];
    final List<ChecklistJpa> luceneResults =
        searchHandler.getQueryResults(null, null, "",
            composeQuery(project, query), "", ChecklistJpa.class,
            ChecklistJpa.class, pfs, totalCt, manager);
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

  /* see superclass */
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
        handler.validateWorkflowAction(project, worklist, userName, role,
            action, this);
    if (!result.isValid()) {
      Logger.getLogger(getClass()).error("  validationResult = " + result);
      throw new LocalException(result.getErrors().iterator().next());
    }
    // Perform the action
    Worklist r =
        handler.performWorkflowAction(project, worklist, userName, role,
            action, this);
    return r;
  }

  /**
   * Compose query.
   *
   * @param project the project
   * @param query the query
   * @return the string
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  private String composeQuery(Project project, String query) throws Exception {
    final StringBuilder localQuery = new StringBuilder();
    localQuery.append(query);
    if (!ConfigUtility.isEmpty(query)) {
      localQuery.append(" AND ");
    }
    // Support explicitly null project
    if (project == null) {
      localQuery.append("projectId:[* TO *]");
    } else {
      localQuery.append("projectId:" + project.getId());
    }
    return localQuery.toString();
  }

}
