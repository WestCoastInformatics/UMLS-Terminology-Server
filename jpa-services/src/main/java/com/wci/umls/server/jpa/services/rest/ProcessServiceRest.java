/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.rest;

import java.io.InputStream;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import com.wci.umls.server.AlgorithmConfig;
import com.wci.umls.server.ProcessConfig;
import com.wci.umls.server.ProcessExecution;
import com.wci.umls.server.helpers.KeyValuePairList;
import com.wci.umls.server.helpers.ProcessConfigList;
import com.wci.umls.server.helpers.ProcessExecutionList;
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
   * Import process config.
   *
   * @param contentDispositionHeader the content disposition header
   * @param in the in
   * @param projectId the project id
   * @param authToken the auth token
   * @return the process config
   * @throws Exception the exception
   */
  public ProcessConfig importProcessConfig(
    FormDataContentDisposition contentDispositionHeader, InputStream in,
    Long projectId, String authToken) throws Exception;

  /**
   * Export process config.
   *
   * @param projectId the project id
   * @param processId the process id
   * @param authToken the auth token
   * @return the input stream
   * @throws Exception the exception
   */
  public InputStream exportProcessConfig(Long projectId, Long processId,
    String authToken) throws Exception;

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
   * Returns the process execution.
   *
   * @param projectId the project id
   * @param id the id
   * @param authToken the auth token
   * @return the process execution
   * @throws Exception the exception
   */
  public ProcessExecution getProcessExecution(Long projectId, Long id,
    String authToken) throws Exception;

  /**
   * Find process execution.
   *
   * @param projectId the project id
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the process execution
   * @throws Exception the exception
   */
  public ProcessExecutionList findProcessExecutions(Long projectId,
    String query, PfsParameterJpa pfs, String authToken) throws Exception;

  /**
   * Find currently executing processes.
   *
   * @param projectId the project id
   * @param authToken the auth token
   * @return the process execution list
   * @throws Exception the exception
   */
  public ProcessExecutionList findCurrentlyExecutingProcesses(Long projectId,
    String authToken) throws Exception;

  /**
   * Removes the process execution.
   *
   * @param projectId the project id
   * @param id the id
   * @param cascade the cascade
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void removeProcessExecution(Long projectId, Long id, Boolean cascade,
    String authToken) throws Exception;

  /**
   * Adds the algorithm config.
   *
   * @param projectId the project id
   * @param processId the process id
   * @param algorithmConfig the algorithm config
   * @param authToken the auth token
   * @return the algorithm config
   * @throws Exception the exception
   */
  public AlgorithmConfig addAlgorithmConfig(Long projectId, Long processId,
    AlgorithmConfigJpa algorithmConfig, String authToken) throws Exception;

  /**
   * Update algorithm config.
   *
   * @param projectId the project id
   * @param processId the process id
   * @param algorithmConfig the algorithm config
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void updateAlgorithmConfig(Long projectId, Long processId,
    AlgorithmConfigJpa algorithmConfig, String authToken) throws Exception;

  /**
   * Validate algorithm config.
   *
   * @param projectId the project id
   * @param processId the process id
   * @param algorithmConfig the algorithm config
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void validateAlgorithmConfig(Long projectId, Long processId,
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
   * Returns the algorithms for the type.
   *
   * @param projectId the project id
   * @param type the type
   * @param authToken the auth token
   * @return the release algorithms
   * @throws Exception the exception
   */
  public KeyValuePairList getAlgorithmsForType(Long projectId, String type,
    String authToken) throws Exception;

  /**
   * Execute process.
   *
   * @param projectId the project id
   * @param processId the process config id
   * @param background the background
   * @param authToken the auth token
   * @return the long process execution id
   * @throws Exception the exception
   */
  public Long executeProcess(Long projectId, Long processId, Boolean background,
    String authToken) throws Exception;

  /**
   * Prepare process.
   *
   * @param projectId the project id
   * @param processId the process id
   * @param authToken the auth token
   * @return the long
   * @throws Exception the exception
   */
  public Long prepareProcess(Long projectId, Long processId, String authToken)
    throws Exception;

  /**
   * Step process.
   *
   * @param projectId the project id
   * @param processId the process id
   * @param step the step, -1 to unstep, 1 to step forward, 0/null does nothing.
   * @param background the background
   * @param authToken the auth token
   * @return the long
   * @throws Exception the exception
   */
  public Long stepProcess(Long projectId, Long processId, Integer step,
    Boolean background, String authToken) throws Exception;

  /**
   * Cancel process.
   *
   * @param projectId the project id
   * @param id the id
   * @param authToken the auth token
   * @return the processId
   * @throws Exception the exception
   */
  public Long cancelProcess(Long projectId, Long id, String authToken)
    throws Exception;

  /**
   * Restart process.
   *
   * @param projectId the project id
   * @param id the id
   * @param background the background
   * @param authToken the auth token
   * @return the processId
   * @throws Exception the exception
   */
  public Long restartProcess(Long projectId, Long id, Boolean background,
    String authToken) throws Exception;

  /**
   * Returns the process progress.
   *
   * @param projectId the project id
   * @param id the id
   * @param authToken the auth token
   * @return the progress
   * @throws Exception the exception
   */
  public Integer getProcessProgress(Long projectId, Long id, String authToken)
    throws Exception;

  /**
   * Returns the algorithm progress.
   *
   * @param projectId the project id
   * @param id the id
   * @param authToken the auth token
   * @return the algorithm progress
   * @throws Exception the exception
   */
  public Integer getAlgorithmProgress(Long projectId, Long id, String authToken)
    throws Exception;

  /**
   * Returns the process log.
   *
   * @param projectId the project id
   * @param processId the process id
   * @param query the query
   * @param authToken the auth token
   * @return the process log
   * @throws Exception the exception
   */
  public String getProcessLog(Long projectId, Long processId, String query,
    String authToken) throws Exception;

  /**
   * Returns the algorithm log.
   *
   * @param projectId the project id
   * @param algorithmId the algorithm id
   * @param query the query
   * @param authToken the auth token
   * @return the algorithm log
   * @throws Exception the exception
   */
  public String getAlgorithmLog(Long projectId, Long algorithmId, String query,
    String authToken) throws Exception;

  /**
   * New algorithm config.
   *
   * @param projectId the project id
   * @param processId the process id
   * @param key the key
   * @param authToken the auth token
   * @return the algorithm config
   * @throws Exception the exception
   */
  public AlgorithmConfig newAlgorithmConfig(Long projectId, Long processId,
    String key, String authToken) throws Exception;

  /**
   * Test query.
   *
   * @param projectId the project id
   * @param processId the process id
   * @param queryTypeName the query type name
   * @param query the query
   * @param objectTypeName the object type name
   * @param authToken the auth token
   * @return the integer
   * @throws Exception the exception
   */
  public Integer testQuery(Long projectId, Long processId, String queryTypeName,
    String query, String objectTypeName, String authToken) throws Exception;

}
