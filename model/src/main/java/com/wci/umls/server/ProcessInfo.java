/*
 *    Copyright 2015 West Coast Informatics, LLC
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

}