/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.rest;

import com.wci.umls.server.User;
import com.wci.umls.server.UserPreferences;
import com.wci.umls.server.helpers.UserList;
import com.wci.umls.server.jpa.UserJpa;
import com.wci.umls.server.jpa.UserPreferencesJpa;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.helpers.StringList;

/**
 * Represents a security available via a REST service.
 */
public interface SecurityServiceRest {

  /**
   * Authenticate.
   * 
   * @param username the username
   * @param password the password
   * @return the string
   * @throws Exception if anything goes wrong
   */
  public User authenticate(String username, String password) throws Exception;

  /**
   * Logout.
   *
   * @param authToken the auth token
   * @return the string
   * @throws Exception the exception
   */
  public String logout(String authToken) throws Exception;

  /**
   * Get user by id.
   *
   * @param id the id
   * @param authToken the auth token
   * @return the user
   * @throws Exception the exception
   */
  public User getUser(Long id, String authToken) throws Exception;

  /**
   * Get user by user.
   *
   * @param username the username
   * @param authToken the auth token
   * @return the user
   * @throws Exception the exception
   */
  public User getUser(String username, String authToken) throws Exception;

  /**
   * Returns the users.
   *
   * @param authToken the auth token
   * @return the users
   * @throws Exception the exception
   */
  public UserList getUsers(String authToken) throws Exception;

  /**
   * Adds the user.
   *
   * @param user the user
   * @param authToken the auth token
   * @return the user
   * @throws Exception the exception
   */
  public User addUser(UserJpa user, String authToken) throws Exception;

  /**
   * Removes the user.
   *
   * @param id the id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void removeUser(Long id, String authToken) throws Exception;

  /**
   * Update user.
   *
   * @param user the user
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void updateUser(UserJpa user, String authToken) throws Exception;

  /**
   * Gets the user for auth token.
   *
   * @param authToken the auth token
   * @return the user for auth token
   * @throws Exception the exception
   */
  public User getUserForAuthToken(String authToken) throws Exception;

  /**
   * Adds the user preferences.
   *
   * @param userPreferences the user preferences
   * @param authToken the auth token
   * @return the user preferences
   * @throws Exception the exception
   */
  public UserPreferences addUserPreferences(UserPreferencesJpa userPreferences,
    String authToken) throws Exception;

  /**
   * Removes the user preferences.
   *
   * @param id the id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void removeUserPreferences(Long id, String authToken) throws Exception;

  /**
   * Update user preferences.
   *
   * @param userPreferences the user preferences
   * @param authToken the auth token
   * @return the user preferences
   * @throws Exception the exception
   */
  public UserPreferences updateUserPreferences(
    UserPreferencesJpa userPreferences, String authToken) throws Exception;

  /**
   * Gets the application roles.
   *
   * @param authToken the auth token
   * @return the application roles
   * @throws Exception the exception
   */
  public StringList getApplicationRoles(String authToken) throws Exception;

  /**
   * Find users for query.
   *
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the user list
   * @throws Exception the exception
   */
  public UserList findUsersForQuery(String query, PfsParameterJpa pfs,
    String authToken) throws Exception;

  /**
   * Add favorite for user.
   *
   * @param terminology the terminology
   * @param version the version
   * @param terminologyId the terminology id
   * @param name the name
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void addFavoriteForUser(String terminology, String version,
    String terminologyId, String name, String authToken) throws Exception;

  /**
   * Remove favorite for user.
   *
   * @param terminology the terminology
   * @param version the version
   * @param terminologyId the terminology id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void removeFavoriteForUser(String terminology, String version,
    String terminologyId, String authToken) throws Exception;
}