/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.rest;

import java.util.Properties;

import com.wci.umls.server.AlgorithmConfig;
import com.wci.umls.server.ProcessConfig;
import com.wci.umls.server.helpers.ProcessConfigList;
import com.wci.umls.server.helpers.StringList;
import com.wci.umls.server.jpa.AlgorithmConfigJpa;
import com.wci.umls.server.jpa.ProcessConfigJpa;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;

/**
 * Represents a service for performing processes and querying their execution.
 */
public interface ProcessServiceRest {

  /**
   * Adds the process config.
   *
   * @param projectId the project id
   * @param processConfig the process config
   * @param authToken the auth token
   * @return the process config
   * @throws Exception the exception
   */
  public ProcessConfig addProcessConfig(Long projectId,
    ProcessConfigJpa processConfig, String authToken) throws Exception;

  /**
   * Update process config.
   *
   * @param projectId the project id
   * @param processConfig the process config
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void updateProcessConfig(Long projectId,
    ProcessConfigJpa processConfig, String authToken) throws Exception;

  /**
   * Removes the process config.
   *
   * @param projectId the project id
   * @param id the id
   * @param cascade the cascade
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void removeProcessConfig(Long projectId, Long id, Boolean cascade,
    String authToken) throws Exception;

  /**
   * Returns the process config.
   *
   * @param projectId the project id
   * @param id the id
   * @param authToken the auth token
   * @return the process config
   * @throws Exception the exception
   */
  public ProcessConfig getProcessConfig(Long projectId, Long id,
    String authToken) throws Exception;

  /**
   * Find process config.
   *
   * @param projectId the project id
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the process config
   * @throws Exception the exception
   */
  public ProcessConfigList findProcessConfigs(Long projectId, String query,
    PfsParameterJpa pfs, String authToken) throws Exception;

  /**
   * Adds the algorithm config.
   *
   * @param projectId the project id
   * @param algorithmConfig the algorithm config
   * @param authToken the auth token
   * @return the algorithm config
   * @throws Exception the exception
   */
  public AlgorithmConfig addAlgorithmConfig(Long projectId,
    AlgorithmConfigJpa algorithmConfig, String authToken) throws Exception;

  /**
   * Update algorithm config.
   *
   * @param projectId the project id
   * @param algorithmConfig the algorithm config
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void updateAlgorithmConfig(Long projectId,
    AlgorithmConfigJpa algorithmConfig, String authToken) throws Exception;

  /**
   * Removes the algorithm config.
   *
   * @param projectId the project id
   * @param id the id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void removeAlgorithmConfig(Long projectId, Long id, String authToken)
    throws Exception;

  /**
   * Returns the algorithm config.
   *
   * @param projectId the project id
   * @param id the id
   * @param authToken the auth token
   * @return the algorithm config
   * @throws Exception the exception
   */
  public AlgorithmConfig getAlgorithmConfig(Long projectId, Long id,
    String authToken) throws Exception;

  /**
   * Returns the predefined processes.
   *
   * @param authToken the auth token
   * @return the predefined processes
   * @throws Exception the exception
   */
  public StringList getPredefinedProcesses(String authToken) throws Exception;

  // public ProcessConfigList getProcessConfigs(String authToken) throws
  // Exception;

  /**
   * Run predefined process.
   *
   * @param projectId the project id
   * @param id the id
   * @param p the p
   * @param authToken the auth token
   * @return the long
   * @throws Exception the exception
   */
  // returns process execution id
  public Long runPredefinedProcess(Long projectId, String id, Properties p,
    String authToken) throws Exception;

  /**
   * Run process config.
   *
   * @param projectId the project id
   * @param processConfigId the process config id
   * @param authToken the auth token
   * @return the long
   * @throws Exception the exception
   */
  // returns process execution id after immediately returning.
  public Long runProcessConfig(Long projectId, Long processConfigId,
    String authToken) throws Exception;

  /**
   * Lookup progress.
   *
   * @param projectId the project id
   * @param processExecutionId the process execution id
   * @param authToken the auth token
   * @return the int
   * @throws Exception the exception
   */
  public int lookupProgress(Long projectId, Long processExecutionId,
    String authToken) throws Exception;

  /**
   * Cancel process execution.
   *
   * @param projectId the project id
   * @param processExecutionId the process execution id
   * @param authToken the auth token
   * @return true, if successful
   * @throws Exception the exception
   */
  // Cancels a running process
  public boolean cancelProcessExecution(Long projectId, Long processExecutionId,
    String authToken) throws Exception;
}
