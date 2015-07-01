/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.NoResultException;

import org.apache.log4j.Logger;

import com.wci.umls.server.User;
import com.wci.umls.server.UserRole;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.helpers.UserList;
import com.wci.umls.server.jpa.UserJpa;
import com.wci.umls.server.jpa.helpers.UserListJpa;
import com.wci.umls.server.services.ProjectService;
import com.wci.umls.server.services.SecurityService;
import com.wci.umls.server.services.handlers.SecurityServiceHandler;

/**
 * Reference implementation of the {@link SecurityService}.
 */
public class SecurityServiceJpa extends RootServiceJpa implements
    SecurityService {

  /** The token username . */
  private static Map<String, String> tokenUsernameMap = Collections
      .synchronizedMap(new HashMap<String, String>());

  /** The token login time . */
  private static Map<String, Date> tokenTimeoutMap = Collections
      .synchronizedMap(new HashMap<String, Date>());

  /** The handler. */
  private static SecurityServiceHandler handler = null;

  /** The timeout. */
  private static int timeout;

  /**
   * Instantiates an empty {@link SecurityServiceJpa}.
   *
   * @throws Exception the exception
   */
  public SecurityServiceJpa() throws Exception {
    super();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.mapping.services.SecurityService#authenticate(java.lang.
   * String, java.lang.String)
   */
  @Override
  public String authenticate(String username, String password) throws Exception {
    // Check username and password are not null
    if (username == null || username.isEmpty())
      throw new LocalException("Invalid username: null");
    if (password == null || password.isEmpty())
      throw new LocalException("Invalid password: null");

    Properties config = ConfigUtility.getConfigProperties();

    if (handler == null) {
      timeout = Integer.valueOf(config.getProperty("security.timeout"));
      String handlerName = config.getProperty("security.handler");
      handler =
          ConfigUtility.newStandardHandlerInstanceWithConfiguration(
              "security.handler", handlerName, SecurityServiceHandler.class);
    }

    //
    // Call the security service
    //
    User authUser = handler.authenticate(username, password);

    if (authUser == null)
      return null;

    // check if authenticated user matches one of our users
    UserList userList = getUsers();
    User userFound = null;
    for (User user : userList.getObjects()) {
      if (user.getUserName().equals(authUser.getUserName())) {
        userFound = user;
        break;
      }
    }

    // if user was found, update to match settings
    if (userFound != null) {
      Logger.getLogger(getClass()).info(
          "Update user = " + authUser.getUserName());
      userFound.setEmail(authUser.getEmail());
      userFound.setName(authUser.getName());
      userFound.setUserName(authUser.getUserName());
      updateUser(userFound);

    }
    // if User not found, create one for our use
    else {
      Logger.getLogger(getClass()).info("Add user = " + authUser.getUserName());
      User newUser = new UserJpa();
      newUser.setEmail(authUser.getEmail());
      newUser.setName(authUser.getName());
      newUser.setUserName(authUser.getUserName());
      newUser.setApplicationRole(UserRole.VIEWER);
      addUser(newUser);
      clear();
    }

    // Generate application-managed token
    String token = handler.computeTokenForUser(username);
    tokenUsernameMap.put(token, authUser.getUserName());
    tokenTimeoutMap.put(token, new Date(new Date().getTime() + timeout));

    Logger.getLogger(getClass()).info("User = " + authUser.getUserName());

    return token;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.services.SecurityService#logout(java.lang.String)
   */
  @Override
  public void logout(String authToken) throws Exception {
    tokenUsernameMap.remove(authToken);
    tokenTimeoutMap.remove(authToken);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ping.services.SecurityService#getUsernameForToken(java
   * .lang.String)
   */
  @Override
  public String getUsernameForToken(String authToken) throws Exception {
    // use guest user for null auth token
    if (authToken == null)
      throw new LocalException(
          "Attempt to access a service without an authorization token, the user is likely not logged in.");

    // Replace double quotes in auth token.
    String parsedToken = authToken.replace("\"", "");

    // Check auth token against the username map
    if (tokenUsernameMap.containsKey(parsedToken)) {
      String username = tokenUsernameMap.get(parsedToken);

      // Validate that the user has not timed out.
      if (handler.timeoutUser(username)) {

        if (tokenTimeoutMap.get(parsedToken) == null) {
          throw new Exception("No login timeout set for authToken.");
        }

        if (tokenTimeoutMap.get(parsedToken).before(new Date())) {
          throw new LocalException(
              "AuthToken has expired. Please reload and log in again.");
        }
        tokenTimeoutMap.put(parsedToken, new Date(new Date().getTime()
            + timeout));
      }
      return username;
    } else {
      throw new LocalException("AuthToken does not have a valid username.");
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ping.services.SecurityService#authorizeToken(java.lang
   * .String)
   */
  @Override
  public UserRole getApplicationRoleForToken(String authToken) throws Exception {

    if (authToken == null) {
      throw new LocalException(
          "Attempt to access a service without an authorization token, the user is likely not logged in.");
    }
    String parsedToken = authToken.replace("\"", "");
    String username = getUsernameForToken(parsedToken);
    // check for null username
    if (username == null) {
      throw new LocalException(
          "Unable to find user for the authoriztaion token");
    }
    User user = getUser(username.toLowerCase());
    if (user == null) {
      return UserRole.VIEWER;
      // throw new
      // LocalException("Unable to obtain user information for username = " +
      // username);
    }
    return user.getApplicationRole();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.ts.services.SecurityService#getProjectRoleForToken(java.
   * lang.String, java.lang.Long)
   */
  @Override
  public UserRole getProjectRoleForToken(String authToken, Long projectId)
    throws Exception {
    if (authToken == null) {
      throw new LocalException(
          "Attempt to access a service without an authorization token, the user is likely not logged in.");
    }
    if (projectId == null) {
      throw new Exception("Unexpected null project id");
    }

    String username = getUsernameForToken(authToken);
    ProjectService service = new ProjectServiceJpa();
    UserRole result = service.getUserRoleForProject(username, projectId);
    service.close();
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.mapping.services.SecurityService#getUser(java.lang.Long)
   */
  @Override
  public User getUser(Long id) throws Exception {
    return manager.find(UserJpa.class, id);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.mapping.services.SecurityService#getUser(java.lang.String)
   */
  @Override
  public User getUser(String username) throws Exception {
    javax.persistence.Query query =
        manager
            .createQuery("select u from UserJpa u where userName = :userName");
    query.setParameter("userName", username);
    try {
      return (User) query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.mapping.services.SecurityService#addUser(org.ihtsdo.otf.
   * mapping.helpers.User)
   */
  @Override
  public User addUser(User user) {
    Logger.getLogger(getClass()).debug("Security Service - add user " + user);
    try {
      if (getTransactionPerOperation()) {
        tx = manager.getTransaction();
        tx.begin();
        manager.persist(user);
        tx.commit();
      } else {
        manager.persist(user);
      }
    } catch (Exception e) {
      if (tx.isActive()) {
        tx.rollback();
      }
      throw e;
    }

    return user;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.services.SecurityService#removeUser(java.lang.Long)
   */
  @Override
  public void removeUser(Long id) {
    Logger.getLogger(getClass()).debug("Security Service - remove user " + id);
    tx = manager.getTransaction();
    // retrieve this user
    User mu = manager.find(UserJpa.class, id);
    try {
      if (getTransactionPerOperation()) {
        tx.begin();
        if (manager.contains(mu)) {
          manager.remove(mu);
        } else {
          manager.remove(manager.merge(mu));
        }
        tx.commit();

      } else {
        if (manager.contains(mu)) {
          manager.remove(mu);
        } else {
          manager.remove(manager.merge(mu));
        }
      }
    } catch (Exception e) {
      if (tx.isActive()) {
        tx.rollback();
      }
      throw e;
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.mapping.services.SecurityService#updateUser(org.ihtsdo.otf
   * .mapping.helpers.User)
   */
  @Override
  public void updateUser(User user) {
    Logger.getLogger(getClass())
        .debug("Security Service - update user " + user);
    try {
      if (getTransactionPerOperation()) {
        tx = manager.getTransaction();
        tx.begin();
        manager.merge(user);
        tx.commit();
      } else {
        manager.merge(user);
      }
    } catch (Exception e) {
      if (tx.isActive()) {
        tx.rollback();
      }
      throw e;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.mapping.services.SecurityService#getUsers()
   */
  @SuppressWarnings("unchecked")
  @Override
  public UserList getUsers() {
    javax.persistence.Query query =
        manager.createQuery("select u from UserJpa u");
    List<User> m = query.getResultList();
    UserListJpa mapUserList = new UserListJpa();
    mapUserList.setObjects(m);
    mapUserList.setTotalCount(m.size());
    return mapUserList;
  }

  @Override
  public void refreshCaches() throws Exception {
    // n/a
  }

}
