/**
 * Copyright 2015 West Coast Informatics, LLC
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
  LEAD("Lead"),

  /** The administrator. */
  ADMINISTRATOR("Administrator");

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
    if (this.equals(UserRole.VIEWER) && role.equals(UserRole.VIEWER))
      return true;
    else if (this.equals(UserRole.AUTHOR)
        && (role.equals(UserRole.VIEWER) || role.equals(UserRole.AUTHOR)))
      return true;
    else if (this.equals(UserRole.LEAD)
        && (role.equals(UserRole.VIEWER) || role.equals(UserRole.AUTHOR) || role
            .equals(UserRole.LEAD)))
      return true;
    else if (this.equals(UserRole.ADMINISTRATOR))
      return true;
    else
      return false;
  }

}
