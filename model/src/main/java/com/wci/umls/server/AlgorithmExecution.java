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

import com.wci.umls.server.helpers.HasExecution;

/**
 * Represents the result of the execution of an algorithm.
 */
public interface AlgorithmExecution
    extends AlgorithmInfo<ProcessExecution>, HasExecution {

  /**
   * Returns the algorithm config id that this execution is derived from.
   *
   * @return the algorithm config id
   */
  public Long getAlgorithmConfigId();

  /**
   * Sets the algorithm config id.
   *
   * @param algorithmConfigId the algorithm config id
   */
  public void setAlgorithmConfigId(Long algorithmConfigId);

  /**
   * Returns the activity id.
   *
   * @return the activity id
   */
  public String getActivityId();

  /**
   * Sets the activity id.
   *
   * @param activityId the activity id
   */
  public void setActivityId(String activityId);
  
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