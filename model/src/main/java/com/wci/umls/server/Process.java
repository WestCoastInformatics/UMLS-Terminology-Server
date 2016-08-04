/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server;

import java.util.Date;
import java.util.List;

import com.wci.umls.server.helpers.HasLastModified;
import com.wci.umls.server.helpers.HasTerminology;

/**
 * Represents a process configuration.
 */
public interface Process extends HasLastModified, HasTerminology {

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

  /**
   * Returns the steps.
   *
   * @return the steps
   */
  public List<AlgorithmConfig> getSteps();

  /**
   * Sets the steps.
   *
   * @param steps the steps
   */
  public void setSteps(List<AlgorithmConfig> steps);

  /**
   * Indicates whether or not this is a template process, meaning that it holds
   * a standard configuration but will not be executed or represent an
   * execution.  In other words, the "date" fields for a template will
   * always be null.
   * 
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isTemplate();

  /**
   * Sets the template.
   *
   * @param template the template
   */
  public void setTemplate(boolean template);

  /**
   * Returns the start date.
   *
   * @return the start date
   */
  public Date getStartDate();

  /**
   * Sets the start date.
   *
   * @param startDate the start date
   */
  public void setStartDate(Date startDate);

  /**
   * Returns the finish date.
   *
   * @return the finish date
   */
  public Date getFinishDate();

  /**
   * Sets the finish date.
   *
   * @param startDate the finish date
   */
  public void setFinishDate(Date startDate);

  /**
   * Returns the fail date.
   *
   * @return the fail date
   */
  public Date getFailDate();

  /**
   * Sets the fail date.
   *
   * @param startDate the fail date
   */
  public void setFailDate(Date startDate);
}