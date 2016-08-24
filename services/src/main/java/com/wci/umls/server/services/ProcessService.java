/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.services;

import java.util.List;

import com.wci.umls.server.algo.Algorithm;

/**
 * Represents a service for performing and monitoring processes.
 */
public interface ProcessService extends HistoryService {
  
  /**
   * Returns the authoring algorithms.
   *
   * @return the authoring algorithms
   * @throws Exception the exception
   */
  public List<Algorithm> getAuthoringAlgorithms() throws Exception;

  /**
   * Returns the maintenance algorithms.
   *
   * @return the maintenance algorithms
   * @throws Exception the exception
   */
  public List<Algorithm> getMaintenanceAlgorithms() throws Exception;
  
  /**
   * Returns the release algorithms.
   *
   * @return the release algorithms
   * @throws Exception the exception
   */
  public List<Algorithm> getReleaseAlgorithms() throws Exception;

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