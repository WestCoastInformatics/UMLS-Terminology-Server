/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.services;

import com.wci.umls.server.AlgorithmConfig;
import com.wci.umls.server.AlgorithmExecution;
import com.wci.umls.server.ProcessConfig;
import com.wci.umls.server.ProcessExecution;
import com.wci.umls.server.Project;
import com.wci.umls.server.algo.Algorithm;
import com.wci.umls.server.helpers.KeyValuePairList;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.ProcessConfigList;
import com.wci.umls.server.helpers.ProcessExecutionList;

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
   * Returns the algorithm instance.
   *
   * @param key the key
   * @return the algorithm instance
   * @throws Exception the exception
   */
  public Algorithm getAlgorithmInstance(String key) throws Exception;

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

  /**
   * Adds the process execution.
   *
   * @param processExecution the process execution
   * @return the process execution
   * @throws Exception the exception
   */
  public ProcessExecution addProcessExecution(ProcessExecution processExecution)
    throws Exception;

  /**
   * Removes the process execution.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeProcessExecution(Long id) throws Exception;

  /**
   * Update process execution.
   *
   * @param processExecution the process execution
   * @throws Exception the exception
   */
  public void updateProcessExecution(ProcessExecution processExecution)
    throws Exception;

  /**
   * Returns the process execution.
   *
   * @param id the id
   * @return the process execution
   * @throws Exception the exception
   */
  public ProcessExecution getProcessExecution(Long id) throws Exception;

  /**
   * Find process executions.
   *
   * @param projectId the project id
   * @param query the query
   * @param pfs the pfs
   * @return the process execution
   * @throws Exception the exception
   */
  public ProcessExecutionList findProcessExecutions(Long projectId,
    String query, PfsParameter pfs) throws Exception;

  /**
   * Adds the algorithm config.
   *
   * @param algorithmConfig the algorithm config
   * @return the algorithm config
   * @throws Exception the exception
   */
  public AlgorithmConfig addAlgorithmConfig(AlgorithmConfig algorithmConfig)
    throws Exception;

  /**
   * Removes the algorithm config.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeAlgorithmConfig(Long id) throws Exception;

  /**
   * Update algorithm config.
   *
   * @param algorithmConfig the algorithm config
   * @throws Exception the exception
   */
  public void updateAlgorithmConfig(AlgorithmConfig algorithmConfig)
    throws Exception;

  /**
   * Returns the algorithm config.
   *
   * @param id the id
   * @return the algorithm config
   * @throws Exception the exception
   */
  public AlgorithmConfig getAlgorithmConfig(Long id) throws Exception;

  /**
   * Adds the algorithm execution.
   *
   * @param algorithmExecution the algorithm execution
   * @return the algorithm execution
   * @throws Exception the exception
   */
  public AlgorithmExecution addAlgorithmExecution(
    AlgorithmExecution algorithmExecution) throws Exception;

  /**
   * Removes the algorithm execution.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeAlgorithmExecution(Long id) throws Exception;

  /**
   * Update algorithm execution.
   *
   * @param algorithmExecution the algorithm execution
   * @throws Exception the exception
   */
  public void updateAlgorithmExecution(AlgorithmExecution algorithmExecution)
    throws Exception;

  /**
   * Returns the algorithm execution.
   *
   * @param id the id
   * @return the algorithm execution
   * @throws Exception the exception
   */
  public AlgorithmExecution getAlgorithmExecution(Long id) throws Exception;

  /**
   * Execute single algorithm.
   *
   * @param algorithm the algorithm
   * @param project the project
   * @throws Exception the exception
   */
  public void executeSingleAlgorithm(Algorithm algorithm, Project project)
    throws Exception;

  /**
   * Returns the algorithm log.
   *
   * @param projectId the project id
   * @param algorithmExecutionId the algorithm execution id
   * @param query the query
   * @return the algorithm log
   * @throws Exception the exception
   */
  public String getAlgorithmLog(Long projectId, Long algorithmExecutionId,
    String query) throws Exception;

  /**
   * Returns the process log.
   *
   * @param projectId the project id
   * @param processExecutionId the process execution id
   * @param query the query
   * @return the process log
   * @throws Exception the exception
   */
  public String getProcessLog(Long projectId, Long processExecutionId,
    String query) throws Exception;

  /**
   * Save log to file.
   *
   * @param projectId the project id
   * @param processExecution the process execution
   * @throws Exception the exception
   */
  public void saveLogToFile(Long projectId, ProcessExecution processExecution)
    throws Exception;

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