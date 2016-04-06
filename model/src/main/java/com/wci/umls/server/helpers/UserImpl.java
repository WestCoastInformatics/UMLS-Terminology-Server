/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

import java.util.HashMap;
import java.util.Map;

import com.wci.umls.server.Project;
import com.wci.umls.server.User;
import com.wci.umls.server.UserPreferences;
import com.wci.umls.server.UserRole;

/**
 * Local implementation of {@link User}.
 */
public class UserImpl implements User {

  /** The id. */
  private Long id;

  /** The user name. */
  private String userName;

  /** The name. */
  private String name;

  /** The email. */
  private String email;

  /** The application role. */
  private UserRole applicationRole;

  /** The user preferences. */
  private UserPreferences userPreferences;

  /** The auth token. */
  private String authToken;

  /** The projects. */
  private Map<Project,UserRole> projectRoleMap;
  
  /**
   * Instantiates an empty {@link UserImpl}.
   */
  public UserImpl() {
    // do nothing
  }

  /**
   * Instantiates a {@link UserImpl} from the specified parameters.
   *
   * @param user the user
   */
  public UserImpl(User user) {
    userName = user.getUserName();
    name = user.getName();
    email = user.getEmail();
    applicationRole = user.getApplicationRole();
    authToken = user.getAuthToken();
    userPreferences = user.getUserPreferences();
  }

  /* see superclass */
  @Override
  public String getUserName() {
    return userName;
  }

  /* see superclass */
  @Override
  public void setUserName(String username) {
    this.userName = username;
  }

  /* see superclass */
  @Override
  public String getName() {
    return name;
  }

  /* see superclass */
  @Override
  public void setName(String name) {
    this.name = name;
  }

  /* see superclass */
  @Override
  public String getEmail() {
    return email;
  }

  /* see superclass */
  @Override
  public void setEmail(String email) {
    this.email = email;
  }

  /* see superclass */
  @Override
  public UserRole getApplicationRole() {
    return applicationRole;
  }

  /* see superclass */
  @Override
  public void setApplicationRole(UserRole role) {
    this.applicationRole = role;
  }

  @Override
  public String getAuthToken() {
    return authToken;
  }

  /* see superclass */
  @Override
  public void setAuthToken(String authToken) {
    this.authToken = authToken;
  }

  /* see superclass */
  @Override
  public Long getId() {
    return id;
  }

  /* see superclass */
  @Override
  public void setId(Long id) {
    this.id = id;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
        prime * result
            + ((applicationRole == null) ? 0 : applicationRole.hashCode());
    result = prime * result + ((email == null) ? 0 : email.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((userName == null) ? 0 : userName.hashCode());
    return result;
  }

  /* see superclass */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    UserImpl other = (UserImpl) obj;
    if (applicationRole != other.applicationRole)
      return false;
    if (email == null) {
      if (other.email != null)
        return false;
    } else if (!email.equals(other.email))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (userName == null) {
      if (other.userName != null)
        return false;
    } else if (!userName.equals(other.userName))
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public UserPreferences getUserPreferences() {
    return userPreferences;
  }

  /* see superclass */
  @Override
  public void setUserPreferences(UserPreferences preferences) {
    this.userPreferences = preferences;
  }

  @Override
  public Map<Project, UserRole> getProjectRoleMap() {
    if (projectRoleMap == null) {
      projectRoleMap = new HashMap<>();
    }
    return projectRoleMap;
  }

  @Override
  public void setProjectRoleMap(Map<Project, UserRole> projectRoleMap) {
    this.projectRoleMap = projectRoleMap;
  }

}