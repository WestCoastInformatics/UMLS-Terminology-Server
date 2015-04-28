/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

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

  /**  The user preferences. */
  private UserPreferences userPreferences;
  
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
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.User#getUserName()
   */
  @Override
  public String getUserName() {
    return userName;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.User#setUserName(java.lang.String)
   */
  @Override
  public void setUserName(String username) {
    this.userName = username;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.User#getName()
   */
  @Override
  public String getName() {
    return name;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.User#setName(java.lang.String)
   */
  @Override
  public void setName(String name) {
    this.name = name;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.User#getEmail()
   */
  @Override
  public String getEmail() {
    return email;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.User#setEmail(java.lang.String)
   */
  @Override
  public void setEmail(String email) {
    this.email = email;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.User#getApplicationRole()
   */
  @Override
  public UserRole getApplicationRole() {
    return applicationRole;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.User#setApplicationRole(org.ihtsdo.otf.ts.UserRole)
   */
  @Override
  public void setApplicationRole(UserRole role) {
    this.applicationRole = role;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.User#getId()
   */
  @Override
  public Long getId() {
    return id;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.User#setId(java.lang.Long)
   */
  @Override
  public void setId(Long id) {
    this.id = id;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
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

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
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

  @Override
  public UserPreferences getUserPreferences() {
    return userPreferences;
  }

  @Override
  public void setUserPreferences(UserPreferences preferences) {
    this.userPreferences = preferences;
  }

}