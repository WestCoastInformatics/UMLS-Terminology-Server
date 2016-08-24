/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.wci.umls.server.ProcessConfig;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.KeyValuePair;
import com.wci.umls.server.helpers.KeyValuePairList;
import com.wci.umls.server.jpa.ProcessConfigJpa;
import com.wci.umls.server.services.ProcessService;

/**
 * JPA and JAXB enabled implementation of {@link ProcessService}.
 */
public class ProcessServiceJpa extends HistoryServiceJpa
    implements ProcessService {

  /** The insertion algorithms map. */
  private static Map<String, String> insertionAlgorithmsMap =
      new HashMap<>();

  /** The maintenance algorithms map. */
  private static Map<String, String> maintenanceAlgorithmsMap =
      new HashMap<>();

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
      final String key = "insertion.algorithm.handlers";
      for (final String algorithmName : config.getProperty(key).split(",")) {

        String classKey = "insertion.algorithm." + algorithmName + ".class";
        if (config.getProperty(classKey) == null) {
          throw new Exception("Unexpected null classkey " + classKey);
        }
        String algorithmClass = config.getProperty(classKey);        
        
        // Add algorithm to map
        insertionAlgorithmsMap.put(algorithmName, algorithmClass);
      }
    } catch (Exception e) {
      e.printStackTrace();
      insertionAlgorithmsMap = null;
    }

    try {
      config = ConfigUtility.getConfigProperties();
      final String key = "maintenance.algorithm.handlers";
      for (final String algorithmName : config.getProperty(key).split(",")) {

        String classKey = "maintenance.algorithm." + algorithmName + ".class";
        if (config.getProperty(classKey) == null) {
          throw new Exception("Unexpected null classkey " + classKey);
        }
        String algorithmClass = config.getProperty(classKey);        
        
        // Add algorithm to map
        maintenanceAlgorithmsMap.put(algorithmName, algorithmClass);
      }
    } catch (Exception e) {
      e.printStackTrace();
      maintenanceAlgorithmsMap = null;
    }

    try {
      config = ConfigUtility.getConfigProperties();
      final String key = "release.algorithm.handlers";
      for (final String algorithmName : config.getProperty(key).split(",")) {

        String classKey = "release.algorithm." + algorithmName + ".class";
        if (config.getProperty(classKey) == null) {
          throw new Exception("Unexpected null classkey " + classKey);
        }
        String algorithmClass = config.getProperty(classKey);        
        
        // Add algorithm to map
        releaseAlgorithmsMap.put(algorithmName, algorithmClass);
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
          new KeyValuePair(key, insertionAlgorithmsMap.get(key)));
    }

    return algorithmList;
  }

  /* see superclass */
  @Override
  public KeyValuePairList getMaintenanceAlgorithms() throws Exception {
    KeyValuePairList algorithmList = new KeyValuePairList();

    for (String key : maintenanceAlgorithmsMap.keySet()) {
      algorithmList.addKeyValuePair(
          new KeyValuePair(key, maintenanceAlgorithmsMap.get(key)));
    }

    return algorithmList;
  }

  /* see superclass */
  @Override
  public KeyValuePairList getReleaseAlgorithms() throws Exception {
    KeyValuePairList algorithmList = new KeyValuePairList();

    for (String key : releaseAlgorithmsMap.keySet()) {
      algorithmList.addKeyValuePair(
          new KeyValuePair(key, releaseAlgorithmsMap.get(key)));
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

  @Override
  public ProcessConfig addProcessConfig(ProcessConfig processConfig)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Process Service - add processConfig " + processConfig);

    // Add processConfig
    return addHasLastModified(processConfig);
  }

  @Override
  public void removeProcessConfig(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Process Service - remove processConfig " + id);
    // Remove the processConfig
    removeHasLastModified(id, ProcessConfigJpa.class);

  }

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
    final ProcessConfig processConfig = manager.find(ProcessConfigJpa.class, id);
    return processConfig;
  }

}
