/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server;

import java.util.Map;

/**
 * Represents a user.
 */
public interface User {

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
   * Returns the name.
   *
   * @return the name
   */
  public String getName();

  /**
   * Returns the email.
   *
   * @return the email
   */
  public String getEmail();

  /**
   * Returns the application role.
   *
   * @return the application role
   */
  public UserRole getApplicationRole();

  /**
   * Returns the user name.
   *
   * @return the user name
   */
  public String getUserName();

  /**
   * Sets the user name.
   *
   * @param userName the user name
   */
  public void setUserName(String userName);

  /**
   * Sets the name.
   *
   * @param fullName the name
   */
  public void setName(String fullName);

  /**
   * Sets the email.
   *
   * @param email the email
   */
  public void setEmail(String email);

  /**
   * Sets the application role.
   *
   * @param role the application role
   */
  public void setApplicationRole(UserRole role);

  /**
   * Returns the user preferences.
   *
   * @return the user preferences
   */
  public UserPreferences getUserPreferences();

  /**
   * Sets the user preferences.
   *
   * @param preferences the user preferences
   */
  public void setUserPreferences(UserPreferences preferences);

  /**
   * Returns the auth token.
   *
   * @return the auth token
   */
  public String getAuthToken();

  /**
   * Sets the auth token.
   *
   * @param authToken the auth token
   */
  public void setAuthToken(String authToken);

  /**
   * Returns the project role map.
   *
   * @return the project role map
   */
  public Map<Project, UserRole> getProjectRoleMap();

  /**
   * Sets the project role map.
   *
   * @param projectRoleMap the project role map
   */
  public void setProjectRoleMap(Map<Project, UserRole> projectRoleMap);
}
