/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.wci.umls.server.AlgorithmConfig;
import com.wci.umls.server.AlgorithmExecution;
import com.wci.umls.server.ProcessConfig;
import com.wci.umls.server.ProcessExecution;
import com.wci.umls.server.algo.Algorithm;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.KeyValuePair;
import com.wci.umls.server.helpers.KeyValuePairList;
import com.wci.umls.server.helpers.LogEntry;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.ProcessConfigList;
import com.wci.umls.server.helpers.ProcessExecutionList;
import com.wci.umls.server.jpa.AlgorithmConfigJpa;
import com.wci.umls.server.jpa.AlgorithmExecutionJpa;
import com.wci.umls.server.jpa.ProcessConfigJpa;
import com.wci.umls.server.jpa.ProcessExecutionJpa;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.helpers.ProcessConfigListJpa;
import com.wci.umls.server.jpa.helpers.ProcessExecutionListJpa;
import com.wci.umls.server.services.ProcessService;
import com.wci.umls.server.services.handlers.SearchHandler;

/**
 * JPA and JAXB enabled implementation of {@link ProcessService}.
 */
public class ProcessServiceJpa extends ProjectServiceJpa
    implements ProcessService {

  /** The algorithms map. */
  private static Map<String, String> algorithmsMap = new HashMap<>();

  /** The insertion algorithms map. */
  private static Map<String, String> insertionAlgorithmsMap = new HashMap<>();

  /** The maintenance algorithms map. */
  private static Map<String, String> maintenanceAlgorithmsMap = new HashMap<>();

  /** The release algorithms map. */
  private static Map<String, String> releaseAlgorithmsMap = new HashMap<>();

  static {
    init();
  }

  /**
   * Static initialization (also used by refreshCaches).
   */
  private static void init() {

    try {
      config = ConfigUtility.getConfigProperties();
      final String key = "algorithm.handler";
      for (final String handlerName : config.getProperty(key).split(",")) {

        // Add handlers to map
        final Algorithm handlerService =
            ConfigUtility.newStandardHandlerInstanceWithConfiguration(key,
                handlerName, Algorithm.class);
        algorithmsMap.put(handlerName, handlerService.getName());
        handlerService.close();
      }
    } catch (Exception e) {
      e.printStackTrace();
      algorithmsMap = null;
    }

    try {
      config = ConfigUtility.getConfigProperties();
      final String key = "insertion.algorithm.handler";
      for (final String handlerName : config.getProperty(key).split(",")) {

        // Pull algorithm from algorithm map, and add to specific algorithm-type
        // map
        insertionAlgorithmsMap.put(handlerName, algorithmsMap.get(handlerName));
      }

    } catch (Exception e) {
      e.printStackTrace();
      insertionAlgorithmsMap = null;
    }

    try {
      config = ConfigUtility.getConfigProperties();
      final String key = "maintenance.algorithm.handler";
      for (final String handlerName : config.getProperty(key).split(",")) {

        // Pull algorithm from algorithm map, and add to specific algorithm-type
        // map
        maintenanceAlgorithmsMap.put(handlerName,
            algorithmsMap.get(handlerName));
      }
    } catch (Exception e) {
      e.printStackTrace();
      maintenanceAlgorithmsMap = null;
    }

    try {
      config = ConfigUtility.getConfigProperties();
      final String key = "release.algorithm.handler";
      for (final String handlerName : config.getProperty(key).split(",")) {

        // Pull algorithm from algorithm map, and add to specific algorithm-type
        // map
        releaseAlgorithmsMap.put(handlerName, algorithmsMap.get(handlerName));
      }
    } catch (Exception e) {
      e.printStackTrace();
      releaseAlgorithmsMap = null;
    }
  }

  /**
   * Instantiates an empty {@link ProcessServiceJpa}.
   *
   * @throws Exception the exception
   */
  public ProcessServiceJpa() throws Exception {
    super();
    validateInit();
  }

  /* see superclass */
  @Override
  public KeyValuePairList getInsertionAlgorithms() throws Exception {
    final KeyValuePairList algorithmList = new KeyValuePairList();

    for (final String key : insertionAlgorithmsMap.keySet()) {

      final String name = insertionAlgorithmsMap.get(key);
      if (name != null) {
        algorithmList.addKeyValuePair(new KeyValuePair(key, name));
      } else {
        throw new Exception(
            "Misalignment between all algorithms and insertion algorithms in config file");
      }
    }

    return algorithmList;
  }

  /* see superclass */
  @Override
  public KeyValuePairList getMaintenanceAlgorithms() throws Exception {
    final KeyValuePairList algorithmList = new KeyValuePairList();

    for (final String key : maintenanceAlgorithmsMap.keySet()) {

      final String name = maintenanceAlgorithmsMap.get(key);
      if (name != null) {
        algorithmList.addKeyValuePair(new KeyValuePair(key, name));
      } else {
        throw new Exception(
            "Misalignment between all algorithms and maintenance algorithms in config file");
      }
    }

    return algorithmList;
  }

  /* see superclass */
  @Override
  public KeyValuePairList getReleaseAlgorithms() throws Exception {
    final KeyValuePairList algorithmList = new KeyValuePairList();

    for (final String key : releaseAlgorithmsMap.keySet()) {

      final String name = releaseAlgorithmsMap.get(key);
      if (name != null) {
        algorithmList.addKeyValuePair(new KeyValuePair(key, name));
      } else {
        throw new Exception(
            "Misalignment between all algorithms and release algorithms in config file");
      }
    }

    return algorithmList;
  }

  /* see superclass */
  @Override
  public Algorithm getAlgorithmInstance(String key) throws Exception {

    return ConfigUtility.newStandardHandlerInstanceWithConfiguration(
        "algorithm.handler", key, Algorithm.class);
  }

  /**
   * Validate init.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  private void validateInit() throws Exception {
    if (algorithmsMap == null) {
      throw new Exception(
          "Algorithms did not properly initialize, serious error.");
    }

    if (insertionAlgorithmsMap == null) {
      throw new Exception(
          "Insertion algorithms did not properly initialize, serious error.");
    }

    if (maintenanceAlgorithmsMap == null) {
      throw new Exception(
          "Maintenance algorithms did not properly initialize, serious error.");
    }

    if (releaseAlgorithmsMap == null) {
      throw new Exception(
          "Release algorithms did not properly initialize, serious error.");
    }
  }

  /* see superclass */
  @Override
  public ProcessConfig addProcessConfig(ProcessConfig processConfig)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Process Service - add processConfig " + processConfig);

    // Add processConfig
    return addHasLastModified(processConfig);
  }

  /* see superclass */
  @Override
  public void removeProcessConfig(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Process Service - remove processConfig " + id);

    // Remove the processConfig
    removeHasLastModified(id, ProcessConfigJpa.class);

  }

  /* see superclass */
  @Override
  public void updateProcessConfig(ProcessConfig processConfig)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Process Service - update processConfig " + processConfig);
    // update processConfig
    updateHasLastModified(processConfig);

  }

  /* see superclass */
  @Override
  public ProcessConfig getProcessConfig(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Process Service - get processConfig " + id);
    final ProcessConfig processConfig =
        manager.find(ProcessConfigJpa.class, id);
    handleLazyInit(processConfig);

    return processConfig;
  }

  /* see superclass */
  @Override
  public ProcessConfigList findProcessConfigs(Long projectId, String query,
    PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass())
        .info("Project Service - find processConfigs " + "/" + query);

    final SearchHandler searchHandler = getSearchHandler(ConfigUtility.DEFAULT);

    int totalCt[] = new int[1];
    final List<ProcessConfig> results = new ArrayList<>();

    final List<String> clauses = new ArrayList<>();

    if (projectId == null) {
      throw new Exception("Error: project must be specified");
    }
    clauses.add("projectId:" + projectId);
    if (!ConfigUtility.isEmpty(query)) {
      clauses.add(query);
    }
    String fullQuery = ConfigUtility.composeQuery("AND", clauses);

    List<ProcessConfigJpa> processConfigs =
        searchHandler.getQueryResults(null, null, Branch.ROOT, fullQuery, null,
            ProcessConfigJpa.class, pfs, totalCt, manager);

    for (final ProcessConfig pc : processConfigs) {
      handleLazyInit(pc);
      results.add(pc);
    }

    final ProcessConfigList processConfigList = new ProcessConfigListJpa();
    processConfigList.setObjects(results);
    processConfigList.setTotalCount(totalCt[0]);

    return processConfigList;
  }

  /* see superclass */
  @Override
  public ProcessExecution addProcessExecution(ProcessExecution processExecution)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Process Service - add processExecution " + processExecution);

    // Add processExecution
    return addHasLastModified(processExecution);
  }

  /* see superclass */
  @Override
  public void removeProcessExecution(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Process Service - remove processExecution " + id);

    // Remove the processExecution
    removeHasLastModified(id, ProcessExecutionJpa.class);

  }

  /* see superclass */
  @Override
  public void updateProcessExecution(ProcessExecution processExecution)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Process Service - update processExecution " + processExecution);
    // update processExecution
    updateHasLastModified(processExecution);

  }

  /* see superclass */
  @Override
  public ProcessExecution getProcessExecution(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Process Service - get processExecution " + id);
    final ProcessExecution processExecution =
        manager.find(ProcessExecutionJpa.class, id);
    handleLazyInit(processExecution);

    return processExecution;
  }

  /* see superclass */
  @Override
  public ProcessExecutionList findProcessExecutions(Long projectId,
    String query, PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass())
        .info("Project Service - find processExecutions " + "/" + query);

    final SearchHandler searchHandler = getSearchHandler(ConfigUtility.DEFAULT);

    int totalCt[] = new int[1];
    final List<ProcessExecution> results = new ArrayList<>();

    final List<String> clauses = new ArrayList<>();
    if (projectId == null) {
      throw new Exception("Error: project must be specified");
    }
    clauses.add("projectId:" + projectId);
    if (!ConfigUtility.isEmpty(query)) {
      clauses.add(query);
    }
    String fullQuery = ConfigUtility.composeQuery("AND", clauses);

    List<ProcessExecutionJpa> processExecutions =
        searchHandler.getQueryResults(null, null, Branch.ROOT, fullQuery, null,
            ProcessExecutionJpa.class, pfs, totalCt, manager);

    for (final ProcessExecution pe : processExecutions) {
      handleLazyInit(pe);
      results.add(pe);
    }

    final ProcessExecutionList processExecutionList =
        new ProcessExecutionListJpa();
    processExecutionList.setObjects(results);
    processExecutionList.setTotalCount(totalCt[0]);

    return processExecutionList;
  }

  /* see superclass */
  @Override
  public AlgorithmConfig addAlgorithmConfig(AlgorithmConfig algorithmConfig)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Algorithm Service - add algorithmConfig " + algorithmConfig);

    // Add algorithmConfig
    return addHasLastModified(algorithmConfig);
  }

  /* see superclass */
  @Override
  public void removeAlgorithmConfig(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Algorithm Service - remove algorithmConfig " + id);

    // Remove the algorithmConfig
    removeHasLastModified(id, AlgorithmConfigJpa.class);

  }

  /* see superclass */
  @Override
  public void updateAlgorithmConfig(AlgorithmConfig algorithmConfig)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Algorithm Service - update algorithmConfig " + algorithmConfig);
    // update algorithmConfig
    updateHasLastModified(algorithmConfig);

  }

  /* see superclass */
  @Override
  public AlgorithmConfig getAlgorithmConfig(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Algorithm Service - get algorithmConfig " + id);
    final AlgorithmConfig algorithmConfig =
        manager.find(AlgorithmConfigJpa.class, id);
    handleLazyInit(algorithmConfig);

    return algorithmConfig;
  }

  /* see superclass */
  @Override
  public AlgorithmExecution addAlgorithmExecution(
    AlgorithmExecution algorithmExecution) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Algorithm Service - add algorithmExecution " + algorithmExecution);

    // Add algorithmExecution
    return addHasLastModified(algorithmExecution);
  }

  /* see superclass */
  @Override
  public void removeAlgorithmExecution(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Algorithm Service - remove algorithmExecution " + id);

    // Remove the algorithmExecution
    removeHasLastModified(id, AlgorithmExecutionJpa.class);

  }

  /* see superclass */
  @Override
  public void updateAlgorithmExecution(AlgorithmExecution algorithmExecution)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Algorithm Service - update algorithmExecution " + algorithmExecution);
    // update algorithmExecution
    updateHasLastModified(algorithmExecution);

  }

  /* see superclass */
  @Override
  public AlgorithmExecution getAlgorithmExecution(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Algorithm Service - get algorithmExecution " + id);
    final AlgorithmExecution algorithmExecution =
        manager.find(AlgorithmExecutionJpa.class, id);
    handleLazyInit(algorithmExecution);

    return algorithmExecution;
  }

  /**
   * Handle lazy initialization.
   *
   * @param processConfig the process config
   */
  private void handleLazyInit(ProcessConfig processConfig) {
    if (processConfig == null) {
      return;
    }
    processConfig.getSteps().size();
    processConfig.getProject().getId();
    for (AlgorithmConfig algo : processConfig.getSteps()) {
      handleLazyInit(algo);
    }

  }

  /**
   * Handle lazy init.
   *
   * @param processExecution the process execution
   */
  private void handleLazyInit(ProcessExecution processExecution) {
    if (processExecution == null) {
      return;
    }
    processExecution.getSteps().size();
    processExecution.getProject().getId();
    for (AlgorithmExecution algo : processExecution.getSteps()) {
      handleLazyInit(algo);
    }

  }

  /**
   * Handle lazy initialization.
   *
   * @param algorithmConfig the algorithm config
   */
  @SuppressWarnings("static-method")
  private void handleLazyInit(AlgorithmConfig algorithmConfig) {
    if (algorithmConfig == null) {
      return;
    }
    algorithmConfig.getParameters().size();
    algorithmConfig.getProperties().size();
    algorithmConfig.getProject().getId();
    algorithmConfig.getProcess().getId();
  }

  /**
   * Handle lazy initialization.
   *
   * @param algorithmExecution the algorithm execution
   */
  @SuppressWarnings("static-method")
  private void handleLazyInit(AlgorithmExecution algorithmExecution) {
    if (algorithmExecution == null) {
      return;
    }
    algorithmExecution.getParameters().size();
    algorithmExecution.getProperties().size();
    algorithmExecution.getProject().getId();
    algorithmExecution.getProcess().getId();
  }

  /* see superclass */
  @Override
  public String getAlgorithmLog(Long projectId, Long algorithmExecutionId)
    throws Exception {
    final PfsParameter pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setAscending(false);
    pfs.setSortField("lastModified");

    // Load the processExecution, to get the activityId
    AlgorithmExecution algorithmExecution =
        getAlgorithmExecution(algorithmExecutionId);
    String activityId = algorithmExecution.getActivityId();

    final List<String> clauses = new ArrayList<>();
    clauses.add("projectId:" + projectId);

    if (!ConfigUtility.isEmpty(activityId)) {
      clauses.add(activityId);
    }
    String fullQuery = ConfigUtility.composeQuery("AND", clauses);

    final List<LogEntry> entries = findLogEntries(fullQuery, pfs);
    Collections.sort(entries, (a1, a2) -> a2.getId().compareTo(a1.getId()));

    final StringBuilder log = new StringBuilder();
    for (int i = entries.size() - 1; i >= 0; i--) {
      final LogEntry entry = entries.get(i);
      final StringBuilder message = new StringBuilder();
      message.append("[")
          .append(ConfigUtility.DATE_FORMAT4.format(entry.getLastModified()));
      message.append("] ");
      message.append(entry.getLastModifiedBy()).append(" ");
      message.append(entry.getMessage()).append("\n");
      log.append(message);
    }

    return log.toString();
  }

  /* see superclass */
  @Override
  public String getProcessLog(Long projectId, Long processExecutionId)
    throws Exception {

    final PfsParameter pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setAscending(false);
    pfs.setSortField("lastModified");

    // Load the processExecution, to get the workId
    ProcessExecution processExecution = getProcessExecution(processExecutionId);
    String workId = processExecution.getWorkId();

    final List<String> clauses = new ArrayList<>();

    clauses.add("projectId:" + projectId);
    if (!ConfigUtility.isEmpty(workId)) {
      clauses.add(workId);
    }
    String fullQuery = ConfigUtility.composeQuery("AND", clauses);

    final List<LogEntry> entries = findLogEntries(fullQuery, pfs);
    Collections.sort(entries, (a1, a2) -> a2.getId().compareTo(a1.getId()));

    final StringBuilder log = new StringBuilder();
    for (int i = entries.size() - 1; i >= 0; i--) {
      final LogEntry entry = entries.get(i);
      final StringBuilder message = new StringBuilder();
      message.append("[")
          .append(ConfigUtility.DATE_FORMAT4.format(entry.getLastModified()));
      message.append("] ");
      message.append(entry.getLastModifiedBy()).append(" ");
      message.append(entry.getMessage()).append("\n");
      log.append(message);
    }

    return log.toString();
  }

  /* see superclass */
  @Override
  public void saveLogToFile(Long projectId, ProcessExecution processExecution)
    throws Exception {

    // Check the input directories
    String srcFullPath =
        ConfigUtility.getConfigProperties().getProperty("source.data.dir")
            + File.separator + processExecution.getInputPath();

    // If input directory is completely empty, don't throw an error (some
    // processes are fine to run without input directory specified)
    if (ConfigUtility.isEmpty(srcFullPath)) {
      return;
    }

    final File saveLocation = new File(srcFullPath);
    if (!saveLocation.exists()) {
      // bail if location doesn't exist
      return;
      // throw new LocalException(
      // "Specified input directory does not exist - could not save Process Log
      // to disk");
    }

    // Create and populate the log
    final String runDate =
        new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    final File outputFile = new File(srcFullPath, "process."
        + processExecution.getProcessConfigId() + "." + runDate + ".log");

    final PrintWriter out = new PrintWriter(new FileWriter(outputFile));
    String processLog = getProcessLog(projectId, processExecution.getId());
    out.print(processLog);
    out.close();

  }

}
