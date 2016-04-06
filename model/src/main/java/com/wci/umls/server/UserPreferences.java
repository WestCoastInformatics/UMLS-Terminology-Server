/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server;


/**
 * Represents a user.
 */
public interface UserPreferences {

  /**
   * Returns the id.
   *
   * @return the id
   */
  public Long getId();

  /**
   * Sets the id.
   *
   * @param id the id
   */
  public void setId(Long id);

  /**
   * Returns the user.
   *
   * @return the user
   */
  public User getUser();

  /**
   * Sets the user.
   *
   * @param user the user
   */
  public void setUser(User user);


  /**
   * Gets the last tab.
   *
   * @return the last tab
   */
  public String getLastTab();

  /**
   * Sets the last tab.
   *
   * @param lastTab the new last tab
   */
  public void setLastTab(String lastTab);

  /**
   * Gets the last terminology.
   *
   * @return the last terminology
   */
  public String getLastTerminology();

  /**
   * Sets the last terminology.
   *
   * @param lastTerminology the new last terminology
   */
  public void setLastTerminology(String lastTerminology);

  /**
   * Gets the last project id.
   *
   * @return the last project id
   */
  public Long getLastProjectId();

  /**
   * Sets the last project id.
   *
   * @param lastProjectId the new last project id
   */
  public void setLastProjectId(Long lastProjectId);
  
  /**
   * Returns the feedback email.
   *
   * @return the feedback email
   */
  public String getFeedbackEmail();
  
  /**
   * Sets the feedback email.
   *
   * @param feedbackEmail the feedback email
   */
  public void setFeedbackEmail(String feedbackEmail);
  

}
