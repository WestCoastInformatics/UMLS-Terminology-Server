/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server;

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

}
