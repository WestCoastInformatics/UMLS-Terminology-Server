/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server;

import java.util.List;

import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.helpers.ComponentInfo;

/**
 * The Interface UserPreferences.
 */
public interface UserPreferences {

  /**
   * Gets the id.
   *
   * @return the id
   */
  public Long getId();

  /**
   * Sets the id.
   *
   * @param id the new id
   */
  public void setId(Long id);

  /**
   * Gets the user.
   *
   * @return the user
   */
  public User getUser();

  /**
   * Sets the user.
   *
   * @param user the new user
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
   * Gets the precedence list.
   *
   * @return the precedence list
   */
  public PrecedenceList getPrecedenceList();

  /**
   * Sets the precedence list.
   *
   * @param precedenceList the new precedence list
   */
  public void setPrecedenceList(PrecedenceList precedenceList);

  /**
   * Sets the favorites.
   *
   * @param favorites the new favorites
   */
  public void setFavorites(List<ComponentInfo> favorites);

  /**
   * Gets the favorites.
   *
   * @return the favorites
   */
  public List<ComponentInfo> getFavorites();

  /**
   * Add favorite.
   *
   * @param favorite the favorite
   */
  public void addFavorite(ComponentInfo favorite);

  /**
   * Remove favorite.
   *
   * @param favorite the favorite
   */
  public void removeFavorite(ComponentInfo favorite);

}
