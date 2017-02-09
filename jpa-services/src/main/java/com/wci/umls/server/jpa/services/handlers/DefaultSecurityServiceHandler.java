/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.handlers;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import com.wci.umls.server.User;
import com.wci.umls.server.UserRole;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.jpa.AbstractConfigurable;
import com.wci.umls.server.jpa.UserJpa;
import com.wci.umls.server.services.handlers.SecurityServiceHandler;

/**
 * Implements a security handler that authorizes via IHTSDO authentication.
 */
public class DefaultSecurityServiceHandler extends AbstractConfigurable
    implements SecurityServiceHandler {

  /** The properties. */
  private Properties properties;

  /* see superclass */
  @Override
  public User authenticate(String userName, String password) throws Exception {

    // userName must not be null
    if (userName == null)
      return null;

    // password must not be null
    if (password == null)
      return null;

    // for default security service, the password must equal the user name
    if (!userName.equals(password))
      return null;

    // check properties
    if (properties == null) {
      properties = ConfigUtility.getConfigProperties();
    }

    User user = new UserJpa();

    // check specified admin users list from config file
    if (getAdminUsersFromConfigFile().contains(userName)) {
      user.setApplicationRole(UserRole.ADMINISTRATOR);
      user.setUserName(userName);
      user.setName(
          userName.substring(0, 1).toUpperCase() + userName.substring(1));
      user.setEmail(userName + "@example.com");
      return user;
    }

    if (getUserUsersFromConfigFile().contains(userName)) {
      user.setApplicationRole(UserRole.USER);
      user.setUserName(userName);
      user.setName(
          userName.substring(0, 1).toUpperCase() + userName.substring(1));
      user.setEmail(userName + "@example.com");
      return user;
    }

    if (getViewerUsersFromConfigFile().contains(userName)) {
      user.setApplicationRole(UserRole.VIEWER);
      user.setUserName(userName);
      user.setName(
          userName.substring(0, 1).toUpperCase() + userName.substring(1));
      user.setEmail(userName + "@example.com");
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
   * Use the user name as a token.
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
      Logger.getLogger(getClass()).warn(
          "Could not retrieve config parameter users.viewer for security handler DEFAULT");
      return userSet;
    }

    for (final String user : userList.split(","))
      userSet.add(user);
    return userSet;
  }

  /**
   * Returns the user users from config file.
   *
   * @return the user users from config file
   */
  private Set<String> getUserUsersFromConfigFile() {

    HashSet<String> userSet = new HashSet<>();
    String userList = properties.getProperty("users.user");

    Logger.getLogger(getClass()).info(properties.keySet());

    if (userList == null) {
      Logger.getLogger(getClass()).warn(
          "Could not retrieve config parameter users.user for security handler DEFAULT");
      return userSet;
    }

    for (final String user : userList.split(","))
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
      Logger.getLogger(getClass()).warn(
          "Could not retrieve config parameter users.admin for security handler DEFAULT");
      return userSet;
    }

    for (final String user : userList.split(","))
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
      Logger.getLogger(getClass()).warn(
          "Could not retrieve config parameter users.admin for security handler DEFAULT");
      return userSet;
    }
  
    for (final String user : userList.split(","))
      userSet.add(user);
    return userSet;
  }

  /* see superclass */
  @Override
  public String getName() {
    return "Default Security Service Handler";
  }

}
