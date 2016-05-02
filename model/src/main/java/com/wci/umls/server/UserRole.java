/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server;

/**
 * The Enum UserRole.
 *
 */
public enum UserRole {

  /** The viewer. */
  VIEWER("Viewer"),

  /** The author. */
  AUTHOR("Author"),

  /** The lead. */
  REVIEWER("Reviewer"),

  /** The user. */
  USER("User"),

  /** The administrator. */
  ADMINISTRATOR("Admin");

  /** The value. */
  private String value;

  /**
   * Instantiates a {@link UserRole} from the specified parameters.
   *
   * @param value the value
   */
  private UserRole(String value) {
    this.value = value;
  }

  /**
   * Returns the value.
   *
   * @return the value
   */
  public String getValue() {
    return value;
  }

  /**
   * Checks for privileges of.
   *
   * @param role the role
   * @return true, if successful
   */
  public boolean hasPrivilegesOf(UserRole role) {
    if (this == UserRole.VIEWER && role == UserRole.VIEWER)
      return true;
    else if (this == UserRole.AUTHOR
        && (role == UserRole.VIEWER || role == UserRole.AUTHOR))
      return true;
    else if (this == UserRole.REVIEWER
        && (role == UserRole.VIEWER || role == UserRole.USER
            || role == UserRole.AUTHOR || role == UserRole.REVIEWER))
      return true;
    else if (this == UserRole.USER
        && (role == UserRole.VIEWER || role == UserRole.USER || role == UserRole.AUTHOR))
      return true;
    else if (this == UserRole.ADMINISTRATOR)
      return true;
    else
      return false;
  }
}
