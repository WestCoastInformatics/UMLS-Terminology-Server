/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.rest;

import java.util.Properties;

import com.wci.umls.server.helpers.StringList;

/**
 * Represents a service for performing processes and querying their execution.
 */
public interface ProcessServiceRest {

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
