/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.persistence.NoResultException;

import org.apache.log4j.Logger;
import org.apache.lucene.queryparser.classic.QueryParserBase;

import com.wci.umls.server.Project;
import com.wci.umls.server.UserRole;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.ChecklistList;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.helpers.Note;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.QueryType;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.helpers.StringList;
import com.wci.umls.server.helpers.TrackingRecordList;
import com.wci.umls.server.helpers.WorkflowConfigList;
import com.wci.umls.server.helpers.WorklistList;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.helpers.ChecklistListJpa;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.helpers.TrackingRecordListJpa;
import com.wci.umls.server.jpa.helpers.WorkflowConfigListJpa;
import com.wci.umls.server.jpa.helpers.WorklistListJpa;
import com.wci.umls.server.jpa.workflow.ChecklistJpa;
import com.wci.umls.server.jpa.workflow.ChecklistNoteJpa;
import com.wci.umls.server.jpa.workflow.TrackingRecordJpa;
import com.wci.umls.server.jpa.workflow.WorkflowBinDefinitionJpa;
import com.wci.umls.server.jpa.workflow.WorkflowBinJpa;
import com.wci.umls.server.jpa.workflow.WorkflowConfigJpa;
import com.wci.umls.server.jpa.workflow.WorkflowEpochJpa;
import com.wci.umls.server.jpa.workflow.WorklistJpa;
import com.wci.umls.server.jpa.workflow.WorklistNoteJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.SemanticTypeComponent;
import com.wci.umls.server.model.workflow.Checklist;
import com.wci.umls.server.model.workflow.TrackingRecord;
import com.wci.umls.server.model.workflow.WorkflowAction;
import com.wci.umls.server.model.workflow.WorkflowBin;
import com.wci.umls.server.model.workflow.WorkflowBinDefinition;
import com.wci.umls.server.model.workflow.WorkflowConfig;
import com.wci.umls.server.model.workflow.WorkflowEpoch;
import com.wci.umls.server.model.workflow.WorkflowStatus;
import com.wci.umls.server.model.workflow.Worklist;
import com.wci.umls.server.services.WorkflowService;
import com.wci.umls.server.services.handlers.SearchHandler;
import com.wci.umls.server.services.handlers.WorkflowActionHandler;

/**
 * Workflow Service JPA implementation.
 */
public class WorkflowServiceJpa extends HistoryServiceJpa
    implements WorkflowService {

  /** The workflow action handlers. */
  static Map<String, WorkflowActionHandler> workflowHandlerMap =
      new HashMap<>();

  private Set<Long> atomIdsForTrackingRecordNeedsReview = null;

  static {
    init();
  }

  /**
   * Static initialization (used by refresh caches).
   */
  private static void init() {
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

  }

  /* see superclass */
  @Override
  public TrackingRecord getTrackingRecord(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Workflow Service - get tracking record " + id);
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
    updateHasLastModified(trackingRecord);
  }

  /* see superclass */
  @Override
  public void removeTrackingRecord(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Workflow Service - remove tracking record " + id);
    // Remove the component
    removeHasLastModified(id, TrackingRecordJpa.class);

  }

  /* see superclass */
  @Override
  public TrackingRecordList findTrackingRecords(Project project, String query,
    PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass())
        .debug("Workflow Service - find tracking records " + project.getId()
            + ", " + query);

    final TrackingRecordList results = new TrackingRecordListJpa();
    final SearchHandler searchHandler = getSearchHandler(null);
    final int[] totalCt = new int[1];
    final List<TrackingRecordJpa> luceneResults = searchHandler.getQueryResults(
        null, null, Branch.ROOT, composeQuery(project, query), "",
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
  public TrackingRecordList findTrackingRecordsForConcept(Project project,
    Concept concept, String query, PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass())
        .debug("Workflow Service - find tracking records for concept "
            + project.getId() + ", " + concept.getId() + ", " + query);

    // If concept has no atoms, it won't be on any tracking record list
    final List<Atom> atoms = concept.getAtoms();
    if (atoms.size() == 0) {
      return null;
    }

    // Create a query
    final List<String> clauses = atoms.stream()
        .map(a -> "componentIds:" + a.getId()).collect(Collectors.toList());
    final String atomQuery = ConfigUtility.composeQuery("OR", clauses);

    // Avoid searching ready for publication lists
    final String finalQuery = ConfigUtility.composeQuery("AND", atomQuery,
        composeQuery(project, query), "finished:false");

    final TrackingRecordList results = new TrackingRecordListJpa();
    final SearchHandler searchHandler = getSearchHandler(null);
    final int[] totalCt = new int[1];
    final List<TrackingRecordJpa> luceneResults =
        searchHandler.getQueryResults(null, null, Branch.ROOT, finalQuery, "",
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
    worklist.getNotes().size();
  }

  /* see superclass */
  @Override
  public void handleLazyInit(Checklist checklist) {
    checklist.getNotes().size();
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
    definition.getWorkflowConfig().getType();
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

    // TODO: make epoch part of the project -> so we can just say
    // project.getWorkflowEpoch...
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
  public void updateWorkflowEpoch(WorkflowEpoch workflowEpoch)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Workflow Service - update workflow epoch " + workflowEpoch);

    // update component
    updateHasLastModified(workflowEpoch);

  }

  /* see superclass */
  @Override
  public void removeWorkflowEpoch(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Workflow Service - remove workflow epoch " + id);
    // Remove the component
    removeHasLastModified(id, WorkflowEpochJpa.class);
  }

  /* see superclass */
  @Override
  public WorkflowEpoch getWorkflowEpoch(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Workflow Service - get workflow epoch " + id);
    return getHasLastModified(id, WorkflowEpochJpa.class);
  }

  /* see superclass */
  @Override
  public List<WorkflowEpoch> getWorkflowEpochs(Project project)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Workflow Service - get workflow epochs - " + project.getId());

    final SearchHandler searchHandler = getSearchHandler(null);
    final int[] totalCt = new int[1];
    final List<WorkflowEpochJpa> results =
        searchHandler.getQueryResults(null, null, "", composeQuery(project, ""),
            "", WorkflowEpochJpa.class, null, totalCt, manager);
    return new ArrayList<WorkflowEpoch>(results);

  }

  /* see superclass */
  @Override
  public WorkflowConfig addWorkflowConfig(WorkflowConfig workflowConfig)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Workflow Service - add project workflow config "
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
    Logger.getLogger(getClass())
        .debug("Workflow Service - remove project workflow config " + id);
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
        searchHandler.getQueryResults(null, null, "", composeQuery(project, ""),
            "", WorkflowConfigJpa.class, null, totalCt, manager);
    return new ArrayList<WorkflowConfig>(results);

  }

  /* see superclass */
  @Override
  public WorkflowConfig getWorkflowConfig(Project project, String type)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Workflow Service - get project workflow config "
            + project.getId() + ", " + type);
    final SearchHandler searchHandler = getSearchHandler(null);
    final int[] totalCt = new int[1];

    final List<WorkflowConfigJpa> results =
        searchHandler.getQueryResults(null, null, "",
            composeQuery(project, "") + " AND type:\""
                + QueryParserBase.escape(type) + "\"",
            "", WorkflowConfigJpa.class, null, totalCt, manager);

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
    Logger.getLogger(getClass())
        .debug("Workflow Service - get project workflow config " + id);
    return getHasLastModified(id, WorkflowConfigJpa.class);
  }

  /* see superclass */
  @Override
  public WorkflowConfigList findWorkflowConfigs(Long projectId, String query,
    PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass())
        .info("Workflow Service - find workflowConfigs " + "/" + query);

    final SearchHandler searchHandler = getSearchHandler(ConfigUtility.DEFAULT);

    int totalCt[] = new int[1];
    final List<WorkflowConfig> results = new ArrayList<>();

    final List<String> clauses = new ArrayList<>();

    if (projectId == null) {
      throw new Exception("Error: project must be specified");
    }
    clauses.add("projectId:" + projectId);
    if (!ConfigUtility.isEmpty(query)) {
      clauses.add(query);
    }
    String fullQuery = ConfigUtility.composeQuery("AND", clauses);

    List<WorkflowConfigJpa> workflowConfigs =
        searchHandler.getQueryResults(null, null, Branch.ROOT, fullQuery, null,
            WorkflowConfigJpa.class, pfs, totalCt, manager);

    for (final WorkflowConfig wc : workflowConfigs) {
      handleLazyInit(wc);
      results.add(wc);
    }

    final WorkflowConfigList workflowConfigList = new WorkflowConfigListJpa();
    workflowConfigList.setObjects(results);
    workflowConfigList.setTotalCount(totalCt[0]);

    return workflowConfigList;
  }

  /* see superclass */
  @Override
  public WorkflowBinDefinition addWorkflowBinDefinition(
    WorkflowBinDefinition workflowBinDefinition) throws Exception {
    Logger.getLogger(getClass())
        .debug("Workflow Service - add workflow bin definition "
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
    Logger.getLogger(getClass())
        .debug("Workflow Service - update workflow bin definition "
            + workflowBinDefinition);

    // update component
    updateHasLastModified(workflowBinDefinition);

  }

  /* see superclass */
  @Override
  public void removeWorkflowBinDefinition(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Workflow Service - remove workflow bin definition " + id);

    // Remove the component
    removeHasLastModified(id, WorkflowBinDefinitionJpa.class);
  }

  /* see superclass */
  @Override
  public List<WorkflowBinDefinition> getWorkflowBinDefinitions(Project project,
    String type) throws Exception {
    Logger.getLogger(getClass())
        .debug("Workflow Service - get workflow bin definitions "
            + project.getId() + ", " + type);

    final WorkflowConfig config = this.getWorkflowConfig(project, type);
    return config.getWorkflowBinDefinitions();
  }

  /* see superclass */
  @Override
  public WorkflowBinDefinition getWorkflowBinDefinition(Long id)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Workflow Service - get workflow bin definition " + id);
    return getHasLastModified(id, WorkflowBinDefinitionJpa.class);
  }

  /* see superclass */
  @Override
  public WorkflowBin addWorkflowBin(WorkflowBin workflowBin) throws Exception {
    Logger.getLogger(getClass())
        .debug("Workflow Service - add workflow bin " + workflowBin.toString());

    // Add component
    WorkflowBin bin = addHasLastModified(workflowBin);

    // do not inform listeners
    return bin;
  }

  /* see superclass */
  @Override
  public void updateWorkflowBin(WorkflowBin workflowBin) throws Exception {
    Logger.getLogger(getClass())
        .debug("Workflow Service - update workflow bin " + workflowBin);

    // update component
    updateHasLastModified(workflowBin);

  }

  /* see superclass */
  @Override
  public void removeWorkflowBin(Long id, boolean cascade) throws Exception {
    Logger.getLogger(getClass())
        .debug("Workflow Service - remove workflow bin " + id + ", " + cascade);
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

  /**
   * Regenerate bin helper. From the set of parameters it creates and populates
   * a single workflow bin. For complete regeneration of bins this can be
   * repeatedly used.
   *
   * @param project the project
   * @param definition the definition
   * @param rank the rank
   * @param conceptsSeen the concepts seen
   * @param conceptIdWorklistNameMap the concept id worklist name map
   * @return the workflow bin
   * @throws Exception the exception
   */
  public WorkflowBin regenerateBinHelper(Project project,
    WorkflowBinDefinition definition, int rank, Set<Long> conceptsSeen,
    Map<Long, String> conceptIdWorklistNameMap) throws Exception {
    Logger.getLogger(getClass()).info("Regenerate bin " + definition.getName());

    RegenerateBinThread thread = new RegenerateBinThread(project, definition, rank, conceptsSeen,
        conceptIdWorklistNameMap);
    Thread t = new Thread(thread);
    t.start();
    t.join();
    return thread.getBin();
  }

  /* see superclass */
  @Override
  public List<WorkflowBin> getWorkflowBins(Project project, String type)
    throws Exception {
    Logger.getLogger(getClass()).debug("Workflow Service - get workflow bins "
        + project.getId() + ", " + type);

    final SearchHandler searchHandler = getSearchHandler(null);
    final int[] totalCt = new int[1];
    final List<WorkflowBinJpa> results =
        searchHandler.getQueryResults(null, null, "",
            composeQuery(project, "")
                + (type == null ? "" : " AND type:" + type),
            "", WorkflowBinJpa.class, null, totalCt, manager);
    return new ArrayList<WorkflowBin>(results);

  }

  /* see superclass */
  @Override
  public WorkflowBin getWorkflowBin(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Workflow Service - get workflow bin " + id);
    return getHasLastModified(id, WorkflowBinJpa.class);
  }

  /* see superclass */
  @Override
  public Worklist addWorklist(Worklist worklist) throws Exception {
    Logger.getLogger(getClass())
        .debug("Workflow Service - add worklist " + worklist.toString());

    // Add component
    Worklist list = addHasLastModified(worklist);

    // do not inform listeners
    return list;
  }

  /* see superclass */
  @Override
  public void updateWorklist(Worklist worklist) throws Exception {
    Logger.getLogger(getClass())
        .debug("Workflow Service - update worklist " + worklist);

    // update component
    updateHasLastModified(worklist);

  }

  /* see superclass */
  @Override
  public void removeWorklist(Long id, boolean cascade) throws Exception {
    Logger.getLogger(getClass())
        .debug("Workflow Service - remove worklist " + id + ", " + cascade);
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
      
      final List<Note> worklistNotesCopies = new ArrayList<>();
      for (final Note note : worklist.getNotes()) {
        worklistNotesCopies.add(new WorklistNoteJpa((WorklistNoteJpa) note));
      }

      worklist.getNotes().clear();
      
      for (final Note note : worklistNotesCopies) {
        removeNote(note.getId(), WorklistNoteJpa.class);
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
    if (worklist != null) {
      handleLazyInit(worklist);
    }
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
    Logger.getLogger(getClass())
        .debug("Workflow Service - find worklists for query " + query);
    final WorklistList results = new WorklistListJpa();
    final SearchHandler searchHandler = getSearchHandler(null);
    final int[] totalCt = new int[1];
    final List<WorklistJpa> luceneResults = searchHandler.getQueryResults(null,
        null, "", composeQuery(project, query), "", WorklistJpa.class, pfs,
        totalCt, manager);
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
    Logger.getLogger(getClass())
        .debug("Workflow Service - add checklist " + checklist.toString());

    // Add component
    Checklist list = addHasLastModified(checklist);

    // do not inform listeners
    return list;
  }

  /* see superclass */
  @Override
  public void updateChecklist(Checklist checklist) throws Exception {
    Logger.getLogger(getClass())
        .debug("Workflow Service - update checklist " + checklist);

    // update component
    updateHasLastModified(checklist);

  }

  /* see superclass */
  @Override
  public void removeChecklist(Long id, boolean cascade) throws Exception {
    Logger.getLogger(getClass())
        .debug("Workflow Service - remove checklist " + id + ", " + cascade);
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
      
      final List<Note> checklistNotesCopies = new ArrayList<>();
      for (final Note note : checklist.getNotes()) {
        checklistNotesCopies.add(new ChecklistNoteJpa((ChecklistNoteJpa) note));
      }

      checklist.getNotes().clear();
      
      for (final Note note : checklistNotesCopies) {
        removeNote(note.getId(), ChecklistNoteJpa.class);
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
    Logger.getLogger(getClass())
        .debug("Workflow Service - find checklists for query " + query);

    ChecklistList results = new ChecklistListJpa();
    final SearchHandler searchHandler = getSearchHandler(null);
    final int[] totalCt = new int[1];
    final List<ChecklistJpa> luceneResults = searchHandler.getQueryResults(null,
        null, "", composeQuery(project, query), "", ChecklistJpa.class, pfs,
        totalCt, manager);
    results.setTotalCount(totalCt[0]);
    for (final ChecklistJpa checklist : luceneResults) {
      results.getObjects().add(checklist);
    }
    return results;
  }

  /**
   * Compute checklist.
   *
   * @param project the project
   * @param query the query
   * @param queryType the query type
   * @param name the name
   * @param pfs the pfs
   * @param override the override
   * @return the checklist
   * @throws Exception the exception
   */
  public Checklist computeChecklist(Project project, String query,
    QueryType queryType, String name, PfsParameterJpa pfs, Boolean override)
    throws Exception {

    // Check to see if checklist with the same name and project already exists
    // if override flag is set, remove the old checklist
    // if override flag is not set, throw LocalException
    final ChecklistList checklists = findChecklists(project, null, null);
    for (final Checklist checklist : checklists.getObjects()) {
      if (checklist.getName().equals(name)
          && checklist.getProject().equals(project)) {
        if (override) {
          removeChecklist(checklist.getId(), true);
          commitClearBegin();
        } else {
          throw new LocalException(
              "A checklist for project " + project.getName() + " with name "
                  + checklist.getName() + " already exists.");
        }
      }
    }

    // Add checklist
    final Checklist checklist = new ChecklistJpa();
    checklist.setName(name);
    checklist.setDescription(name + " description");
    checklist.setProject(project);
    checklist.setTimestamp(new Date());

    // Aggregate into clusters
    final List<Long[]> results = executeClusteredConceptQuery(query, queryType,
        getDefaultQueryParams(project), false);

    // keys should remain sorted
    final Set<Long> clustersEncountered = new HashSet<>();
    final Map<Long, List<Long>> entries = new TreeMap<>();
    for (final Long[] result : results) {
      clustersEncountered.add(result[0]);

      final PfsParameter localPfs =
          (pfs == null) ? new PfsParameterJpa() : new PfsParameterJpa(pfs);
      // Keep only prescribed range from the query
      if ((localPfs.getStartIndex() > -1
          && (clustersEncountered.size() - 1) < localPfs.getStartIndex())
          || (localPfs.getMaxResults() > -1
              && clustersEncountered.size() > localPfs.getMaxResults())) {
        continue;
      }

      if (!entries.containsKey(result[0])) {
        entries.put(result[0], new ArrayList<>());
      }
      entries.get(result[0]).add(result[1]);
    }
    clustersEncountered.clear();

    // Add tracking records
    long i = 1L;
    for (final Long clusterId : entries.keySet()) {

      final TrackingRecord record = new TrackingRecordJpa();
      record.setChecklistName(name);
      // recluster from 1
      record.setClusterId(i++);
      record.setClusterType("");
      record.setProject(project);
      record.setTerminology(project.getTerminology());
      record.setTimestamp(new Date());
      record.setVersion(project.getVersion());
      final StringBuilder sb = new StringBuilder();
      for (final Long conceptId : entries.get(clusterId)) {
        final Concept concept = getConcept(conceptId);
        record.getComponentIds().addAll(concept.getAtoms().stream()
            .map(a -> a.getId()).collect(Collectors.toSet()));
        if (!record.getOrigConceptIds().contains(concept.getId())) {
          sb.append(concept.getName()).append(" ");
        }
        record.getOrigConceptIds().add(concept.getId());

      }

      record.setIndexedData(sb.toString());
      record.setWorkflowStatus(computeTrackingRecordStatus(record, true));
      final TrackingRecord newRecord = addTrackingRecord(record);

      // Add the record to the checklist.
      checklist.getTrackingRecords().add(newRecord);
    }

    // Add the checklist
    final Checklist newChecklist = addChecklist(checklist);

    return newChecklist;
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
    Logger.getLogger(getClass())
        .debug("Workflow Service - perform workflow action " + action + ", "
            + project.getId() + ", " + worklist.getId() + ", " + userName + ", "
            + role);

    // Obtain the handler
    final WorkflowActionHandler handler =
        getWorkflowHandlerForPath(project.getWorkflowPath());
    // Validate the action
    final ValidationResult result = handler.validateWorkflowAction(project,
        worklist, userName, role, action, this);
    if (!result.isValid()) {
      Logger.getLogger(getClass()).error("  validationResult = " + result);
      throw new LocalException(result.getErrors().iterator().next());
    }
    // Perform the action
    Worklist r = handler.performWorkflowAction(project, worklist, userName,
        role, action, this);
    return r;
  }

  /* see superclass */
  @Override
  public WorkflowStatus computeTrackingRecordStatus(TrackingRecord record,
    Boolean batch) throws Exception {
    // Bail if no atom components.
    if (record.getComponentIds().size() == 0) {
      return null;
    }

    if (batch) {

      // If the cache isn't populated, do so now
      // Identify all of the concepts that would case a
      // tracking-record to be NEEDS_REVIEW, and store all of its' atoms' ids
      if (atomIdsForTrackingRecordNeedsReview == null) {
        atomIdsForTrackingRecordNeedsReview = new HashSet<>();

        final PfsParameter pfs = new PfsParameterJpa();
        SearchResultList searchResults =
            findConceptSearchResults(record.getTerminology(), null, Branch.ROOT,
                "atoms.workflowStatus:NEEDS_REVIEW OR "
                    + "workflowStatus:NEEDS_REVIEW OR "
                    + "semanticTypes.workflowStatus:NEEDS_REVIEW",
                pfs);

        for (int i = 0; i < searchResults.getObjects().size(); i++) {
          final Concept concept =
              getConcept(searchResults.getObjects().get(i).getId());
          final List<Long> atomIds = concept.getAtoms().stream()
              .map(a -> a.getId()).collect(Collectors.toList());
          atomIdsForTrackingRecordNeedsReview.addAll(atomIds);
        }
      }

      // If tracking record contains any of the caches atom Ids, the tracking
      // record should be NEEDS_REVIEW
      for (final Long atomId : record.getComponentIds()) {
        if (atomIdsForTrackingRecordNeedsReview.contains(atomId)) {
          return WorkflowStatus.NEEDS_REVIEW;
        }
      }

      return WorkflowStatus.READY_FOR_PUBLICATION;
    }

    // If not batch, run single query to determine this tracking records' status

    // Create a query
    final List<String> clauses = record.getComponentIds().stream()
        .map(l -> "atoms.id:" + l).collect(Collectors.toList());

    final String query = ConfigUtility.composeQuery("OR", clauses);

    WorkflowStatus status = WorkflowStatus.READY_FOR_PUBLICATION;

    // find all cases of concepts with atom ids in the list that are either
    // NEEDS review or that have needs review atoms
    final PfsParameter pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(1);
    // NOTE: this is a simplification of the matrix initializer algortihm,
    // but generally good enough.
    if (

    findConceptSearchResults(record.getTerminology(), null, Branch.ROOT,
        query + " AND (atoms.workflowStatus:NEEDS_REVIEW OR "
            + "workflowStatus:NEEDS_REVIEW OR "
            + "semanticTypes.workflowStatus:NEEDS_REVIEW)",
        pfs).getObjects().size() > 0) {
      status = WorkflowStatus.NEEDS_REVIEW;
    }
    // Return final computed value
    return status;
  }

  /* see superclass */
  @Override
  public Map<Long, String> getConceptIdWorklistNameMap(Project project)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - get concept id -> worklist name map "
            + project.getId());
    final String epoch = getCurrentWorkflowEpoch(project).getName();
    // TODO: Make this JPQL query
    final javax.persistence.Query query = manager.createNativeQuery(
        "select distinct toc.origConceptIds, w.name from worklists w, tracking_records t, "
            + "worklists_tracking_records wt, orig_concept_ids toc "
            + "where w.project_id = :projectId " + "  and w.epoch = :epoch "
            + "  and w.workflowStatus not in ('READY_FOR_PUBLICATION','PUBLISHED') "
            + "  and w.id = wt.worklists_id  "
            + "  and wt.trackingRecords_id = t.id  "
            + "  and t.id = toc.TrackingRecordJpa_id");

    // Try to retrieve the single expected result If zero or more than one
    // result are returned, log error and set result to null
    try {
      query.setParameter("projectId", project.getId());
      query.setParameter("epoch", epoch);
      @SuppressWarnings("unchecked")
      final List<Object[]> results = query.getResultList();
      final Map<Long, String> map = new HashMap<>();
      for (final Object[] result : results) {
        final Long conceptId = Long.valueOf(result[0].toString());
        final String name = result[1].toString();
        map.put(conceptId, name);
      }
      return map;

    } catch (NoResultException e) {
      return null;
    }
  }

  @Override
  public void lookupTrackingRecordConcepts(TrackingRecord record)
    throws Exception {

    // Bail if no atom components.
    if (record.getComponentIds().size() == 0) {
      return;
    }

    // Create a query
    final List<String> clauses = record.getComponentIds().stream()
        .map(l -> "atoms.id:" + l).collect(Collectors.toList());
    final String query = ConfigUtility.composeQuery("OR", clauses);

    // add concepts
    for (final Concept concept : findConcepts(record.getTerminology(), null,
        Branch.ROOT, query, null).getObjects()) {
      // copy without collections
      record.getConcepts().add(new ConceptJpa(concept, false));
    }

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
    if (query != null && !query.equals("null"))
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

  /* see superclass */
  @Override
  public void refreshCaches() throws Exception {
    super.refreshCaches();
    init();
    validateInit();
  }

  /**
   * Validate init.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  private void validateInit() throws Exception {

    if (workflowHandlerMap == null) {
      throw new Exception(
          "Workflow action handler did not properly initialize, serious error.");
    }
  }
  
  /**
   * Class for threaded operation of lookupNames.
   */
  public class RegenerateBinThread implements Runnable {

    private Project project;
    private WorkflowBinDefinition definition;
    private int rank;
    private Set<Long> conceptsSeen;
    private Map<Long, String> conceptIdWorklistNameMap;
    private WorkflowBin bin = new WorkflowBinJpa();


    /**
     * Instantiates a {@link RegenerateBinThread} from the specified
     * parameters.
     *
     * @param id the id
     * @param members the members
     * @param label the label
     * @param saveMembers the save members
     * @throws Exception the exception
     */
    public RegenerateBinThread(Project project,
      WorkflowBinDefinition definition, int rank, Set<Long> conceptsSeen,
      Map<Long, String> conceptIdWorklistNameMap) throws Exception {
      this.project = project;
      this.definition = definition;
      this.rank = rank;
      this.conceptsSeen = conceptsSeen;
      this.conceptIdWorklistNameMap = conceptIdWorklistNameMap;
    }

    /**
     * Gets the regenerated bin.
     *
     * @return the bin
     */
    public WorkflowBin getBin() {
      return bin;
    }
    
    /* see superclass */
    @Override
    public void run() {
      try {
        Logger.getLogger(WorkflowServiceJpa.this.getClass())
            .info("Starting regenerateBinThread - " + definition.getName());
        Logger.getLogger(getClass()).info("Regenerate bin " + definition.getName());

        setTransactionPerOperation(false);
        final Date startDate = new Date();

        // Create the workflow bin        
        bin.setName(definition.getName());
        bin.setDescription(definition.getDescription());
        bin.setEditable(definition.isEditable());
        bin.setEnabled(definition.isEnabled());
        bin.setRequired(definition.isRequired());
        bin.setProject(project);
        bin.setRank(rank);
        bin.setTerminology(project.getTerminology());
        bin.setVersion(getLatestVersion(project.getTerminology()));
        bin.setTerminologyId("");
        bin.setTimestamp(new Date());
        bin.setType(definition.getWorkflowConfig().getType());
        addWorkflowBin(bin);

        // Bail if the definition is not enabled
        if (!definition.isEnabled()) {
          return;
        }

        // execute the query
        final String query = definition.getQuery();
        final List<Long[]> results = executeClusteredConceptQuery(query,
            definition.getQueryType(), getDefaultQueryParams(project), false);

        if (results == null)
          throw new Exception("Failed to retrieve results for query");

        final Map<Long, Set<Long>> clusterIdConceptIdsMap = new HashMap<>();
        Logger.getLogger(getClass()).info("  results = " + results.size());

        // put query results into map
        for (final Long[] result : results) {
          final Long clusterId = Long.parseLong(result[0].toString());
          final Long componentId = Long.parseLong(result[1].toString());
          //if (clusterId == 7634944L || clusterId == 409350L) {
            Logger.getLogger(getClass())
            .info(clusterId + " " + componentId);
          //}

          // skip result entry where the conceptId is already in conceptsSeen
          // and workflow config is mutually exclusive and bin is not a
          // conceptId/conceptId2 pair bin
          if (!conceptsSeen.contains(componentId)
              || !definition.getWorkflowConfig().isMutuallyExclusive()
              || definition.getQuery().matches(".*conceptId2.*")) {
            if (clusterIdConceptIdsMap.containsKey(clusterId)) {
              final Set<Long> componentIds = clusterIdConceptIdsMap.get(clusterId);
              componentIds.add(componentId);
              clusterIdConceptIdsMap.put(clusterId, componentIds);
            } else {
              final Set<Long> componentIds = new HashSet<>();
              componentIds.add(componentId);
              clusterIdConceptIdsMap.put(clusterId, componentIds);
            }
          }
          if (definition.getWorkflowConfig().isMutuallyExclusive()) {
            conceptsSeen.add(componentId);
          }
        }

        // Set the raw cluster count
        bin.setClusterCt(clusterIdConceptIdsMap.size());
        Logger.getLogger(getClass())
            .info("  clusters = " + clusterIdConceptIdsMap.size());

        // for each cluster in clusterIdComponentIdsMap create a tracking record if
        // unassigned bin
        if (definition.isEditable()) {
          long clusterIdCt = 1L;
          final String latestVersion = getLatestVersion(project.getTerminology());
          for (final Long clusterId : clusterIdConceptIdsMap.keySet()) {

            // Create the tracking record
            final TrackingRecord record = new TrackingRecordJpa();
            record.setClusterId(clusterIdCt++);
            record.setTerminology(project.getTerminology());
            record.setTimestamp(new Date());
            record.setVersion(latestVersion);
            record.setWorkflowBinName(bin.getName());
            record.setProject(project);
            record.setWorklistName(null);
            record.setClusterType("");
            record.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);

            // Load the concept ids involved
            final StringBuilder conceptNames = new StringBuilder();
            for (final Long conceptId : clusterIdConceptIdsMap.get(clusterId)) {
              final Concept concept = getConcept(conceptId);
              record.getOrigConceptIds().add(conceptId);
              // collect all the concept names for the indexed data
              conceptNames.append(concept.getName()).append(" ");

              // Set cluster type if a concept has an STY associated with a cluster
              // type in the project
              if (record.getClusterType().equals("")) {
                for (final SemanticTypeComponent sty : concept.getSemanticTypes()) {
                  if (project.getSemanticTypeCategoryMap()
                      .containsKey(sty.getSemanticType())) {
                    record.setClusterType(project.getSemanticTypeCategoryMap()
                        .get(sty.getSemanticType()));
                    break;
                  }
                }
              }
              // Add all atom ids as component ids
              for (final Atom atom : concept.getAtoms()) {
                record.getComponentIds().add(atom.getId());

                // compute workflow status for atoms
                if (atom.getWorkflowStatus() == WorkflowStatus.NEEDS_REVIEW) {
                  record.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
                }
              }

              // Set the worklist name
              if (record.getWorklistName() == null) {
                if (conceptIdWorklistNameMap.containsKey(conceptId)) {
                  record.setWorklistName(conceptIdWorklistNameMap.get(conceptId));
                }
              }

              // Compute workflow status for tracking record
              if (concept.getWorkflowStatus() == WorkflowStatus.NEEDS_REVIEW) {
                record.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
              }

            }
            record.setIndexedData(conceptNames.toString());
            Logger.getLogger(getClass())
            .info("  before addTrackingRecord " + clusterIdConceptIdsMap.size());
            addTrackingRecord(record);
            Logger.getLogger(getClass())
            .info("  before bin.getTrackingRecords.add(record) " + clusterIdConceptIdsMap.size());
            bin.getTrackingRecords().add(record);

            if (clusterIdCt % 50 == 0) {
              if (clusterIdCt % 1000 == 0) {
                Logger.getLogger(getClass()).info("  count = " + clusterIdCt);
              }
              Logger.getLogger(getClass())
              .info("  before commitClearBegin " + clusterIdConceptIdsMap.size());
              commitClearBegin();
            }
          }
        }

        commitClearBegin();
        setTransactionPerOperation(false);
        bin.setCreationTime(new Date().getTime() - startDate.getTime());
        Logger.getLogger(getClass())
        .info("  before updatWorkflowBin " + clusterIdConceptIdsMap.size());
        updateWorkflowBin(bin);
        Logger.getLogger(getClass()).info("Now I'm done!");
        return;
      } catch (Exception e) {
        Logger.getLogger(getClass()).info("I wasn't done!" );
        e.printStackTrace();
    }
  }
  }
}
