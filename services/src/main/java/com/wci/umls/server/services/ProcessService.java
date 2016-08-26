/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.services;

import com.wci.umls.server.ProcessConfig;
import com.wci.umls.server.helpers.KeyValuePairList;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.ProcessConfigList;

/**
 * Represents a service for performing and monitoring processes.
 */
public interface ProcessService extends ProjectService {

  /**
   * Returns the authoring algorithms.
   *
   * @return the authoring algorithms
   * @throws Exception the exception
   */
  public KeyValuePairList getInsertionAlgorithms() throws Exception;

  /**
   * Returns the maintenance algorithms.
   *
   * @return the maintenance algorithms
   * @throws Exception the exception
   */
  public KeyValuePairList getMaintenanceAlgorithms() throws Exception;

  /**
   * Returns the release algorithms.
   *
   * @return the release algorithms
   * @throws Exception the exception
   */
  public KeyValuePairList getReleaseAlgorithms() throws Exception;

  /**
   * Adds the process config.
   *
   * @param processConfig the process config
   * @return the process config
   * @throws Exception the exception
   */
  public ProcessConfig addProcessConfig(ProcessConfig processConfig)
    throws Exception;

  /**
   * Removes the process config.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeProcessConfig(Long id) throws Exception;

  /**
   * Update process config.
   *
   * @param processConfig the process config
   * @throws Exception the exception
   */
  public void updateProcessConfig(ProcessConfig processConfig) throws Exception;

  /**
   * Returns the process config.
   *
   * @param id the id
   * @return the process config
   * @throws Exception the exception
   */
  public ProcessConfig getProcessConfig(Long id) throws Exception;

  /**
   * Find process configs.
   *
   * @param projectId the project id
   * @param query the query
   * @param pfs the pfs
   * @return the process config
   * @throws Exception the exception
   */
  public ProcessConfigList findProcessConfigs(Long projectId, String query,
    PfsParameter pfs) throws Exception;

  // add/remove/update/get/find process configs
  // add/remove/update/get algorithm configs
  // add/remove/update/get/find process executions
  // add/remove/update/get algorithm executions
  // getProcessProgress(process execution id)
  // getAlgorithmProgress(algorithm execution id)
  // cancel process
  // find unfinished process executions (unfinished processes)
  // find failed process executions (unfinished processes)
  // restart process (process config/process execution)

  // get predefined processes? -> Process? ProcessConfig?

  // TODO: websocket for process execution changed?
}