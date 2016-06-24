/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.workflow;

import java.util.List;

/**
 * Represents a worklist.
 */
public interface Worklist extends Checklist {

  /**
   * Gets the group.
   *
   * @return the group
   */
  public String getWorklistGroup();

  /**
   * Sets the group.
   *
   * @param group the new group
   */
  public void setWorklistGroup(String group);

  /**
   * Returns the workflow bin.
   *
   * @return the workflow bin
   */
  public String getWorkflowBin();
  
  /**
   * Sets the workflow bin.
   *
   * @param bin the workflow bin
   */
  public void setWorkflowBin(String bin);
  
  /**
   * Sets the authors.
   *
   * @param authors the authors
   */
  public void setAuthors(List<String> authors);

  /**
   * Returns the authors.
   *
   * @return the authors
   */
  public List<String> getAuthors();

  /**
   * Returns the reviewers.
   *
   * @return the reviewers
   */
  public List<String> getReviewers();

  /**
   * Sets the reviewers.
   *
   * @param reviewers the reviewers
   */
  public void setReviewers(List<String> reviewers);

  /**
   * Gets the workflow status.
   *
   * @return the workflow status
   */
  public WorkflowStatus getWorkflowStatus();

  /**
   * Sets the workflow status.
   *
   * @param workflowStatus the new workflow status
   */
  public void setWorkflowStatus(WorkflowStatus workflowStatus);

}