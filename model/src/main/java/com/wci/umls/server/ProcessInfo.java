/*
 *    Copyright 2019 West Coast Informatics, LLC
 */
package com.wci.umls.server;

import java.util.List;

import com.wci.umls.server.helpers.HasLastModified;
import com.wci.umls.server.helpers.HasProject;
import com.wci.umls.server.helpers.HasTerminology;

/**
 * Represents a process configuration.
 * @param <T> the algorithm info type (e.g. config or execution)
 */
public interface ProcessInfo<T extends AlgorithmInfo<?>>
    extends HasLastModified, HasTerminology, HasProject {

  /**
   * Returns the name.
   * 
   * @return the name
   */
  public String getName();

  /**
   * Sets the name.
   * 
   * @param name the name
   */
  public void setName(String name);

  /**
   * Returns the description.
   * 
   * @return the description
   */
  public String getDescription();

  /**
   * Sets the description.
   * 
   * @param description the description
   */
  public void setDescription(String description);

  /**
   * Gets the feedback email.
   *
   * @return the feedback email
   */
  public String getFeedbackEmail();

  /**
   * Sets the feedback email.
   *
   * @param feedbackEmail the new feedback email
   */
  public void setFeedbackEmail(String feedbackEmail);

  // activityId
  // workId

  /**
   * Returns the steps.
   *
   * @return the steps
   */
  public List<T> getSteps();

  /**
   * Sets the steps.
   *
   * @param steps the steps
   */
  public void setSteps(List<T> steps);

  /**
   * Returns the type.
   *
   * @return the type
   */
  public String getType();

  /**
   * Sets the type.
   *
   * @param type the type
   */
  public void setType(String type);

  /**
   * Returns the input path.
   *
   * @return the input path
   */
  public String getInputPath();
  
  /**
   * Sets the input path.
   *
   * @param inputPath the input path
   */
  public void setInputPath(String inputPath); 
  
  /**
   * Returns the log path.
   *
   * @return the log path
   */
  public String getLogPath();
  
  /**
   * Sets the log path.
   *
   * @param logPath the log path
   */
  public void setLogPath(String logPath);
}