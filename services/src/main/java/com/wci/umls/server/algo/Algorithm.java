/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.algo;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.Configurable;
import com.wci.umls.server.services.RootService;
import com.wci.umls.server.services.helpers.ProgressReporter;

/**
 * Represents an algortihm. Implementations must fully configure themselves
 * before the compute call is made.
 */
public interface Algorithm extends RootService, ProgressReporter, Configurable {

  /**
   * Check preconditions for action. This will make use of data structures
   * configured in the action.
   *
   * @return true, if successful
   * @throws Exception the exception
   */
  public ValidationResult checkPreconditions() throws Exception;

  /**
   * Compute.
   *
   * @throws Exception the exception
   */
  public void compute() throws Exception;

  /**
   * Rests to initial conditions.
   *
   * @throws Exception the exception
   */
  public void reset() throws Exception;

  /**
   * Cancel.
   *
   * @throws Exception the exception
   */
  public void cancel() throws Exception;

  /**
   * Sets the user name.
   *
   * @param userName the user name
   */
  public void setUserName(String userName);

  /**
   * Sets the activity id.
   *
   * @param activityId the activity id
   */
  public void setActivityId(String activityId);

  /**
   * Sets the work id.
   *
   * @param workId the work id
   */
  public void setWorkId(String workId);
}
