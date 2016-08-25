/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.NoResultException;

import org.apache.log4j.Logger;

import com.wci.umls.server.ProcessConfig;
import com.wci.umls.server.algo.Algorithm;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.KeyValuePair;
import com.wci.umls.server.helpers.KeyValuePairList;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.ProcessConfigList;
import com.wci.umls.server.jpa.ProcessConfigJpa;
import com.wci.umls.server.jpa.helpers.ProcessConfigListJpa;
import com.wci.umls.server.services.ProcessService;
import com.wci.umls.server.services.handlers.SearchHandler;

/**
 * JPA and JAXB enabled implementation of {@link ProcessService}.
 */
public class ProcessServiceJpa extends ProjectServiceJpa
    implements ProcessService {

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
      final String key = "insertion.algorithm.handler";
      for (final String handlerName : config.getProperty(key).split(",")) {

        // Add handlers to map
        final Algorithm handlerService =
            ConfigUtility.newStandardHandlerInstanceWithConfiguration(key,
                handlerName, Algorithm.class);
        insertionAlgorithmsMap.put(handlerName, handlerService);
      }
    } catch (Exception e) {
      e.printStackTrace();
      insertionAlgorithmsMap = null;
    }

    try {
      config = ConfigUtility.getConfigProperties();
      final String key = "maintenance.algorithm.handler";
      for (final String handlerName : config.getProperty(key).split(",")) {

        // Add handlers to map
        final Algorithm handlerService =
            ConfigUtility.newStandardHandlerInstanceWithConfiguration(key,
                handlerName, Algorithm.class);
        maintenanceAlgorithmsMap.put(handlerName, handlerService);
      }
    } catch (Exception e) {
      e.printStackTrace();
      maintenanceAlgorithmsMap = null;
    }

    try {
      config = ConfigUtility.getConfigProperties();
      final String key = "release.algorithm.handler";
      for (final String handlerName : config.getProperty(key).split(",")) {

        // Add handlers to map
        final Algorithm handlerService =
            ConfigUtility.newStandardHandlerInstanceWithConfiguration(key,
                handlerName, Algorithm.class);
        releaseAlgorithmsMap.put(handlerName, handlerService);
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

  /**
   * Validate init.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  private void validateInit() throws Exception {
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

  /**
   * Handle lazy initialization.
   *
   * @param processConfig the process config
   */
  @SuppressWarnings("static-method")
  private void handleLazyInit(ProcessConfig processConfig) {
    if (processConfig == null) {
      return;
    }
    processConfig.getSteps().size();
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
  @SuppressWarnings("unchecked")
  @Override
  public ProcessConfigList getProcessConfigs(Long projectId) throws Exception {
    Logger.getLogger(getClass()).debug("Process Service - get processConfigs");
    javax.persistence.Query query =
        manager.createQuery("select pc from ProcessConfigJpa pc");

    try {
      final List<ProcessConfig> processConfigs = query.getResultList();

      // Only keep processConfigs associated with the passed project
      final List<ProcessConfig> results = processConfigs.stream()
          .filter(pc -> pc.getProject().getId().equals(projectId))
          .collect(Collectors.toList());

      final ProcessConfigList processConfigList = new ProcessConfigListJpa();
      processConfigList.setObjects(results);

      for (final ProcessConfig processConfig : processConfigList.getObjects()) {
        handleLazyInit(processConfig);
      }

      return processConfigList;
    } catch (NoResultException e) {
      return null;
    }
  }

  /* see superclass */
  @Override
  public ProcessConfigList findProcessConfigs(String terminology,
    String version, String query, PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass())
        .info("Project Service - find projects " + "/" + query);

    final SearchHandler searchHandler = getSearchHandler(ConfigUtility.DEFAULT);

    int totalCt[] = new int[1];
    final List<ProcessConfig> results = new ArrayList<>();

    final List<String> clauses = new ArrayList<>();
    if (!ConfigUtility.isEmpty(query)) {
      clauses.add(query);
    }
    String fullQuery = ConfigUtility.composeQuery("AND", clauses);

    for (final ProcessConfigJpa pc : searchHandler.getQueryResults(terminology,
        version, Branch.ROOT, fullQuery, null, ProcessConfigJpa.class,
        ProcessConfigJpa.class, pfs, totalCt, manager)) {
      results.add(pc);
    }

    final ProcessConfigList processConfigList = new ProcessConfigListJpa();
    processConfigList.setObjects(results);

    return processConfigList;
  }

}
