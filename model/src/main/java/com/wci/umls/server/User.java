/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server;

import java.util.Date;
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
   * Sets the name.
   *
   * @param fullName the name
   */
  public void setName(String fullName);

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

  /**
   * Returns the editor level. This is to support additional behavior and/or
   * fine-grained access control beyond AUTHOR/REVIEWER/ADMIN.
   *
   * @return the editor level
   */
  public int getEditorLevel();

  /**
   * Sets the editor level.
   *
   * @param editorLevel the editor level
   */
  public void setEditorLevel(int editorLevel);
  
  /**
   * Returns the last time the user logged into the site.
   * 
   * @return timestamp of the user's last login
   */
  public Date getLastLogin();
  
  
  /**
   * Set the user's last login timestamp
   * 
   * @param timestamp
   */
  public void setLastLogin(Date timestamp);
  
  /**
   * Return the number of times a user has used the application's API.
   * Not all APIs used may be counted.
   * 
   * @return long count of the user's API usage.
   */
  public Long getApiUsageCount();
  
  /**
   * Set the number of times a user has used the application's API.
   *  
   * @param apiUsageCount
   */
  public void setApiUsageCount(Long apiUsageCount);
  
  
  /**
   * Return the number of times a user has logged into the application
   * 
   * @return long count of the user login.
   */
  public Long getLoginCount();
  
  /**
   * Set the number of times a user has logged into the application.
   *  
   * @param loginCount
   */
  public void setLoginCount(Long loginCount);
  
  /**
   * Track if the user's email has been verified.
   *  
   * @return Boolean emailVerified
   */
  public Boolean getEmailVerified();
  
  /**
   * Track if the user's email has been verified.
   *  
   * @param emailVerified
   */
  public void setEmailVerified(Boolean emailVerified);
  
  /**
   * Returns the token.
   *
   * @return the token
   */
  public String getUserToken();

  /**
   * Sets the token.
   *
   * @param token the token
   */
  public void setUserToken(String token);
  
}
