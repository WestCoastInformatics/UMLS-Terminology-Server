/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.workflow;

import java.util.List;

import com.wci.umls.server.helpers.HasLastModified;

/**
 * Represents a worklist.
 */
public interface Worklist extends Checklist, HasLastModified {


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
   * Sets the worklist status.
   *
   * @param worklistStatus the new worklist status
   */
  public void setStatus(String worklistStatus);
  
  /**
   * Returns the status.
   *
   * @return the status
   */
  public String getStatus();

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

}