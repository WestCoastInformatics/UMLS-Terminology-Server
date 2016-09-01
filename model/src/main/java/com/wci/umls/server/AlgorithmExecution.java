/*
 *    Copyright 2015 West Coast Informatics, LLC
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
}