/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.services;

import com.wci.umls.server.User;
import com.wci.umls.server.UserPreferences;
import com.wci.umls.server.UserRole;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.UserList;

/**
 * The Interface SecurityService.
 */
public interface SecurityService extends RootService {

  /**
   * Authenticate.
   *
   * @param username the username
   * @param password the password
   * @return the user
   * @throws Exception the exception
   */
  public User authenticate(String username, String password) throws Exception;

  /**
   * Logout.
   *
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void logout(String authToken) throws Exception;

  /**
   * Gets the username for token.
   *
   * @param authToken the auth token
   * @return the username for token
   * @throws Exception the exception
   */
  public String getUsernameForToken(String authToken) throws Exception;

  /**
   * Gets the application role for token.
   *
   * @param authToken the auth token
   * @return the application role for token
   * @throws Exception the exception
   */
  public UserRole getApplicationRoleForToken(String authToken) throws Exception;

  /**
   * Gets the project role for token.
   *
   * @param authToken the auth token
   * @param projectId the project id
   * @return the project role for token
   * @throws Exception the exception
   */
  public UserRole getProjectRoleForToken(String authToken, Long projectId)
    throws Exception;

  /**
   * Gets the user.
   *
   * @param id the id
   * @return the user
   * @throws Exception the exception
   */
  public User getUser(Long id) throws Exception;

  /**
   * Gets the user.
   *
   * @param username the username
   * @return the user
   * @throws Exception the exception
   */
  public User getUser(String username) throws Exception;

  /**
   * Gets the users.
   *
   * @return the users
   */
  public UserList getUsers();

  /**
   * Add user.
   *
   * @param user the user
   * @return the user
   */
  public User addUser(User user);

  /**
   * Remove user.
   *
   * @param id the id
   */
  public void removeUser(Long id);

  /**
   * Update user.
   *
   * @param user the user
   */
  public void updateUser(User user);
  
  /**
   * Handle lazy init.
   *
   * @param user the user
   */
  public void handleLazyInit(User user);

  /**
   * Update user preferences.
   *
   * @param userPreferences the user preferences
   */
  public void updateUserPreferences(UserPreferences userPreferences);

  /**
   * Remove user preferences.
   *
   * @param id the id
   */
  public void removeUserPreferences(Long id);

  /**
   * Add user preferences.
   *
   * @param userPreferences the user preferences
   * @return the user preferences
   */
  public UserPreferences addUserPreferences(UserPreferences userPreferences);

  /**
   * Find users for query.
   *
   * @param query the query
   * @param pfs the pfs
   * @return the user list
   * @throws Exception the exception
   */
  public UserList findUsersForQuery(String query, PfsParameter pfs) throws Exception;


}
