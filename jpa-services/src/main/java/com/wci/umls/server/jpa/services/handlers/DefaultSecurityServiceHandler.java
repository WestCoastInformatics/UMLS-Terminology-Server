/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.handlers;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import com.wci.umls.server.User;
import com.wci.umls.server.UserPreferences;
import com.wci.umls.server.UserRole;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.jpa.UserJpa;
import com.wci.umls.server.jpa.UserPreferencesJpa;
import com.wci.umls.server.services.handlers.SecurityServiceHandler;

/**
 * Implements a security handler that authorizes via IHTSDO authentication.
 */
public class DefaultSecurityServiceHandler implements SecurityServiceHandler {

  /** The properties. */
  private Properties properties;

  /* see superclass */
  @Override
  public User authenticate(String username, String password) throws Exception {

    // username must not be null
    if (username == null)
      return null;

    // password must not be null
    if (password == null)
      return null;

    // for default security service, the password must equal the user name
    if (!username.equals(password))
      return null;

    // check properties
    if (properties == null) {
      properties = ConfigUtility.getConfigProperties();
    }

    User user = new UserJpa();

    // check specified admin users list from config file
    if (getAdminUsersFromConfigFile().contains(username)) {
      user.setApplicationRole(UserRole.ADMINISTRATOR);
      user.setUserName(username);
      user.setName(username.substring(0, 1).toUpperCase()
          + username.substring(1));
      user.setEmail(username + "@example.com");
      return user;
    }

    if (getViewerUsersFromConfigFile().contains(username)) {
      user.setApplicationRole(UserRole.VIEWER);
      user.setUserName(username);
      user.setName(username.substring(0, 1).toUpperCase()
          + username.substring(1));
      user.setEmail(username + "@example.com");
      return user;
    }

    // if user not specified, return null
    return null;
  }

  /**
   * Times out all users except "guest".
   *
   * @param user the user
   * @return true, if successful
   */
  @Override
  public boolean timeoutUser(String user) {
    if (user.equals("guest")) {
      return false;
    }
    return true;
  }

  /**
   * Use the username as a token.
   *
   * @param user the user
   * @return the string
   */
  @Override
  public String computeTokenForUser(String user) {
    return user;
  }

  /* see superclass */
  @Override
  public void setProperties(Properties properties) {
    this.properties = properties;
  }

  /**
   * Returns the viewer users from config file.
   *
   * @return the viewer users from config file
   */
  private Set<String> getViewerUsersFromConfigFile() {
    HashSet<String> userSet = new HashSet<>();
    String userList = properties.getProperty("users.viewer");

    if (userList == null) {
      Logger
          .getLogger(getClass())
          .warn(
              "Could not retrieve config parameter users.viewer for security handler DEFAULT");
      return userSet;
    }

    for (String user : userList.split(","))
      userSet.add(user);
    return userSet;
  }

  /**
   * Returns the admin users from config file.
   *
   * @return the admin users from config file
   */
  private Set<String> getAdminUsersFromConfigFile() {

    HashSet<String> userSet = new HashSet<>();
    String userList = properties.getProperty("users.admin");

    Logger.getLogger(getClass()).info(properties.keySet());

    if (userList == null) {
      Logger
          .getLogger(getClass())
          .warn(
              "Could not retrieve config parameter users.admin for security handler DEFAULT");
      return userSet;
    }

    for (String user : userList.split(","))
      userSet.add(user);
    return userSet;
  }
  
  /* see superclass */
  @Override
  public String getName() {
    return "Default Security Service Handler";
  }

}
