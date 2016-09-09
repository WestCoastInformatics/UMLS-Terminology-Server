/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.wci.umls.server.AlgorithmConfig;
import com.wci.umls.server.AlgorithmExecution;
import com.wci.umls.server.AlgorithmParameter;
import com.wci.umls.server.ProcessConfig;
import com.wci.umls.server.ProcessExecution;
import com.wci.umls.server.algo.Algorithm;
import com.wci.umls.server.helpers.AlgorithmExecutionList;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.KeyValuePair;
import com.wci.umls.server.helpers.KeyValuePairList;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.ProcessConfigList;
import com.wci.umls.server.helpers.ProcessExecutionList;
import com.wci.umls.server.jpa.AlgorithmConfigJpa;
import com.wci.umls.server.jpa.AlgorithmExecutionJpa;
import com.wci.umls.server.jpa.AlgorithmParameterJpa;
import com.wci.umls.server.jpa.ProcessConfigJpa;
import com.wci.umls.server.jpa.ProcessExecutionJpa;
import com.wci.umls.server.jpa.helpers.AlgorithmExecutionListJpa;
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
  private static Map<String, Algorithm> algorithmsMap = new HashMap<>();

  /** The insertion algorithms map. */
  private static Map<String, Algorithm> insertionAlgorithmsMap =
      new HashMap<>();

  /** The maintenance algorithms map. */
  private static Map<String, Algorithm> maintenanceAlgorithmsMap =
      new HashMap<>();

  /** The release algorithms map. */
  private static Map<String, Algorithm> releaseAlgorithmsMap = new HashMap<>();

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
        algorithmsMap.put(handlerName, handlerService);
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
    KeyValuePairList algorithmList = new KeyValuePairList();

    for (String key : insertionAlgorithmsMap.keySet()) {
      algorithmList.addKeyValuePair(
          new KeyValuePair(key, insertionAlgorithmsMap.get(key).getName()));
    }

    return algorithmList;
  }

  /* see superclass */
  @Override
  public KeyValuePairList getMaintenanceAlgorithms() throws Exception {
    KeyValuePairList algorithmList = new KeyValuePairList();

    for (String key : maintenanceAlgorithmsMap.keySet()) {
      algorithmList.addKeyValuePair(
          new KeyValuePair(key, maintenanceAlgorithmsMap.get(key).getName()));
    }

    return algorithmList;
  }

  /* see superclass */
  @Override
  public KeyValuePairList getReleaseAlgorithms() throws Exception {
    KeyValuePairList algorithmList = new KeyValuePairList();

    for (String key : releaseAlgorithmsMap.keySet()) {
      algorithmList.addKeyValuePair(
          new KeyValuePair(key, releaseAlgorithmsMap.get(key).getName()));
    }

    return algorithmList;
  }

  /* see superclass */
  @Override
  public Algorithm getAlgorithmInstance(String key) throws Exception {
    
    return ConfigUtility.newStandardHandlerInstanceWithConfiguration("algorithm.handler", key, Algorithm.class);
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
    clauses.add("*:*");
    if (projectId != null) {
      clauses.add("projectId:" + projectId);
    }
    if (!ConfigUtility.isEmpty(query)) {
      clauses.add(query);
    }
    String fullQuery = ConfigUtility.composeQuery("AND", clauses);

    List<ProcessConfigJpa> processConfigs = searchHandler.getQueryResults(null,
        null, Branch.ROOT, fullQuery, null, ProcessConfigJpa.class,
        ProcessConfigJpa.class, pfs, totalCt, manager);

    for (final ProcessConfig pc : processConfigs) {
      handleLazyInit(pc);
      results.add(pc);
    }

    final ProcessConfigList processConfigList = new ProcessConfigListJpa();
    processConfigList.setObjects(results);

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
  public ProcessExecutionList findProcessExecutions(Long projectId, String query,
    PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass())
        .info("Project Service - find processExecutions " + "/" + query);

    final SearchHandler searchHandler = getSearchHandler(ConfigUtility.DEFAULT);

    int totalCt[] = new int[1];
    final List<ProcessExecution> results = new ArrayList<>();

    final List<String> clauses = new ArrayList<>();
    clauses.add("*:*");
    if (projectId != null) {
      clauses.add("projectId:" + projectId);
    }    
    if (!ConfigUtility.isEmpty(query)) {
      clauses.add(query);
    }
    String fullQuery = ConfigUtility.composeQuery("AND", clauses);

    List<ProcessExecutionJpa> processExecutions = searchHandler.getQueryResults(null,
        null, Branch.ROOT, fullQuery, null, ProcessExecutionJpa.class,
        ProcessExecutionJpa.class, pfs, totalCt, manager);

    for (final ProcessExecution pe : processExecutions) {
      handleLazyInit(pe);
      results.add(pe);
    }

    final ProcessExecutionList processExecutionList = new ProcessExecutionListJpa();
    processExecutionList.setObjects(results);

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
  public AlgorithmExecution addAlgorithmExecution(AlgorithmExecution algorithmExecution)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Algorithm Service - add algorithmExecution " + algorithmExecution);

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
    Logger.getLogger(getClass())
        .debug("Algorithm Service - update algorithmExecution " + algorithmExecution);
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

  /* see superclass */
  @Override
  public AlgorithmExecutionList findAlgorithmExecutions(Long projectId, String query,
    PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass())
        .info("Project Service - find algorithmExecutions " + "/" + query);

    final SearchHandler searchHandler = getSearchHandler(ConfigUtility.DEFAULT);

    int totalCt[] = new int[1];
    final List<AlgorithmExecution> results = new ArrayList<>();

    final List<String> clauses = new ArrayList<>();
    clauses.add("*:*");
    if (projectId != null) {
      clauses.add("projectId:" + projectId);
    }    
    if (!ConfigUtility.isEmpty(query)) {
      clauses.add(query);
    }
    String fullQuery = ConfigUtility.composeQuery("AND", clauses);

    List<AlgorithmExecutionJpa> algorithmExecutions = searchHandler.getQueryResults(null,
        null, Branch.ROOT, fullQuery, null, AlgorithmExecutionJpa.class,
        AlgorithmExecutionJpa.class, pfs, totalCt, manager);

    for (final AlgorithmExecution ae : algorithmExecutions) {
      handleLazyInit(ae);
      results.add(ae);
    }

    final AlgorithmExecutionList algorithmExecutionList = new AlgorithmExecutionListJpa();
    algorithmExecutionList.setObjects(results);

    return algorithmExecutionList;
  }    
  
  /* see superclass */
  @Override
  public AlgorithmParameter addAlgorithmParameter(AlgorithmParameter algorithmParameter)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Algorithm Service - add algorithmParameter " + algorithmParameter);

    // Add algorithmParameter
    return addHasLastModified(algorithmParameter);
  }

  /* see superclass */
  @Override
  public void removeAlgorithmParameter(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Algorithm Service - remove algorithmParameter " + id);

    // Remove the algorithmParameter
    removeHasLastModified(id, AlgorithmParameterJpa.class);

  }

  /* see superclass */
  @Override
  public void updateAlgorithmParameter(AlgorithmParameter algorithmParameter)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Algorithm Service - update algorithmParameter " + algorithmParameter);
    // update algorithmParameter
    updateHasLastModified(algorithmParameter);

  }

  /* see superclass */
  @Override
  public AlgorithmParameter getAlgorithmParameter(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Algorithm Service - get algorithmParameter " + id);
    final AlgorithmParameter algorithmParameter =
        manager.find(AlgorithmParameterJpa.class, id);

    return algorithmParameter;
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
  
}
