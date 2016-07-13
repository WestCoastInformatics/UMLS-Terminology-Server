/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.workflow;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.wci.umls.server.helpers.HasStats;

/**
 * Represents a worklist which is a collection of tracking records that
 * participate in workflow.
 */
public interface Worklist extends Checklist, HasStats {

  /**
   * Returns the workflow bin.
   *
   * @return the workflow bin
   */
  public String getWorkflowBinName();

  /**
   * Sets the workflow bin.
   *
   * @param bin the workflow bin
   */
  public void setWorkflowBinName(String bin);

  /**
   * Returns the workflow state history.
   *
   * @return the workflow state history
   */
  public Map<String, Date> getWorkflowStateHistory();

  /**
   * Sets the workflow state history.
   *
   * @param workflowStateHistory the workflow state history
   */
  public void setWorkflowStateHistory(Map<String, Date> workflowStateHistory);

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
   * Returns the authoring time.
   *
   * @return the authoring time
   */
  public Long getAuthorTime();

  /**
   * Sets the authoring time.
   *
   * @param authoringTime the authoring time
   */
  public void setAuthorTime(Long authoringTime);

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
   * Returns the reviewer time.
   *
   * @return the reviewer time
   */
  public Long getReviewerTime();

  /**
   * Sets the reviewer time.
   *
   * @param authoringTime the reviewer time
   */
  public void setReviewerTime(Long authoringTime);

  /**
   * Returns the team.
   *
   * @return the team
   */
  public String getTeam();

  /**
   * Sets the team.
   *
   * @param team the team
   */
  public void setTeam(String team);

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

  /**
   * Sets the number.
   *
   * @param number the number
   */
  public void setNumber(int number);

  /**
   * Returns the number.
   *
   * @return the number
   */
  public int getNumber();

}