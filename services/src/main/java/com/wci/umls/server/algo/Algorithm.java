/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.algo;

import java.util.List;

import com.wci.umls.server.AlgorithmParameter;
import com.wci.umls.server.ProcessExecution;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.Configurable;
import com.wci.umls.server.helpers.HasProject;
import com.wci.umls.server.services.RootService;
import com.wci.umls.server.services.helpers.ProgressReporter;

/**
 * Represents an algortihm. Implementations must fully configure themselves
 * before the compute call is made.
 */
public interface Algorithm
    extends RootService, ProgressReporter, Configurable, HasProject {

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

  /**
   * Returns the process.
   *
   * @return the process
   */
  public ProcessExecution getProcess();

  /**
   * Sets the process.
   *
   * @param process the process
   */
  public void setProcess(ProcessExecution process);

  /**
   * Returns the parameters.
   *
   * @return the parameters
   * @throws Exception the exception
   */
  public List<AlgorithmParameter> getParameters() throws Exception;

  /**
   * Sets the parameters.
   *
   * @param parameter the parameters
   * @throws Exception the exception
   */
  public void setParameters(List<AlgorithmParameter> parameter)
    throws Exception;

  /**
   * Returns the default description, especially for algorithms that are
   * configured via algorighm configs.
   *
   * @return the description
   */
  public String getDescription();
  
  /**
   * Returns the terminology.
   * 
   * @return the terminology
   */
  public String getTerminology();

  /**
   * Sets the terminology.
   * 
   * @param terminology the terminology
   */
  public void setTerminology(String terminology);

  /**
   * Returns the version.
   * 
   * @return the version
   */
  public String getVersion();

  /**
   * Sets the version.
   * 
   * @param version the version
   */
  public void setVersion(String version);  
  
}
