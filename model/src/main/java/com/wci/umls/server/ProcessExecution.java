/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server;

import com.wci.umls.server.helpers.HasExecution;

/**
 * Represents the result of the execution of a process.
 */
public interface ProcessExecution
    extends ProcessInfo<AlgorithmExecution>, HasExecution {

  /**
   * Returns the work id.
   *
   * @return the work id
   */
  public String getWorkId();

  /**
   * Sets the work id.
   *
   * @param workId the work id
   */
  public void setWorkId(String workId);

  /**
   * Returns the process config id that this execution is derived from.
   *
   * @return the process config id
   */
  public Long getProcessConfigId();

  /**
   * Sets the process config id.
   *
   * @param processConfigId the process config id
   */
  public void setProcessConfigId(Long processConfigId);
}