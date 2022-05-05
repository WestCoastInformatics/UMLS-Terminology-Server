/*
 * Copyright 2020 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.umls.server;

import java.util.Date;
import java.util.Map;

import com.wci.umls.server.helpers.HasExecution;

/**
 * Represents the result of the execution of a process.
 */
public interface ProcessExecution extends ProcessInfo<AlgorithmExecution>, HasExecution {
  /**
   * Returns the stop date.
   *
   * @return the stop date
   */
  public Date getStopDate();

  /**
   * Sets the stop date.
   *
   * @param stopDate the stop date
   */
  public void setStopDate(Date stopDate);

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

  /**
   * Returns the execution info.
   *
   * @return the execution info
   */
  public Map<String, String> getExecutionInfo();

  /**
   * Sets the execution info.
   *
   * @param executionInfo the execution info
   */
  public void setExecutionInfo(Map<String, String> executionInfo);

  /**
   * Is warning.
   *
   * @return the boolean
   */
  public Boolean isWarning();

  /**
   * Sets the warning.
   *
   * @param warning the warning
   */
  public void setWarning(Boolean warning);

  /**
   * Lazy init.
   *
   * @throws Exception the exception
   */
  public void lazyInit() throws Exception;

}