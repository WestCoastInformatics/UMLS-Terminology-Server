/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.rest.impl;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import com.wci.umls.server.User;
import com.wci.umls.server.UserPreferences;
import com.wci.umls.server.UserRole;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.helpers.StringList;
import com.wci.umls.server.helpers.UserList;
import com.wci.umls.server.jpa.UserJpa;
import com.wci.umls.server.jpa.UserPreferencesJpa;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.helpers.UserListJpa;
import com.wci.umls.server.jpa.services.SecurityServiceJpa;
import com.wci.umls.server.jpa.services.rest.SecurityServiceRest;
import com.wci.umls.server.services.SecurityService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * REST implementation for {@link SecurityServiceRest}.
 */
@Path("/security")
@Api(value = "/security", description = "Operations supporting security")
@Consumes({
    MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
})
@Produces({
    MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
})
public class SecurityServiceRestImpl extends RootServiceRestImpl
    implements SecurityServiceRest {

  /* see superclass */
  @Override
  @POST
  @Path("/authenticate/{username}")
  @Consumes({
      MediaType.TEXT_PLAIN
  })
  @ApiOperation(value = "Authenticate a user", notes = "Performs authentication on specified username and password and returns a token upon successful authentication. Throws 401 error if not.", response = UserJpa.class)
  public User authenticate(
    @ApiParam(value = "Username, e.g. 'guest'", required = true) @PathParam("username") String username,
    @ApiParam(value = "Password, as string post data, e.g. 'guest'", required = true) String password)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call (Authentication): /authentication for user = "
            + username);
    SecurityService securityService = new SecurityServiceJpa();
    try {
      User user = securityService.authenticate(username, password);

      if (user == null || user.getAuthToken() == null)
        throw new LocalException("Unable to authenticate user");
      return user;
    } catch (Exception e) {
      handleException(e, "trying to authenticate a user");
      return null;
    } finally {
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @GET
  @Path("/logout/{authToken}")
  @ApiOperation(value = "Log out an auth token", notes = "Performs logout on specified auth token", response = String.class)
  public String logout(
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @PathParam("authToken") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Authentication): /logout for authToken = " + authToken);
    SecurityService securityService = new SecurityServiceJpa();
    try {
      securityService.logout(authToken);
      return null;
    } catch (Exception e) {
      securityService.close();
      handleException(e, "trying to authenticate a user");
    } finally {
      securityService.close();
    }
    return null;

  }

  /* see superclass */
  @Override
  @GET
  @Path("/user/{id}")
  @ApiOperation(value = "Get user by id", notes = "Gets the user for the specified id", response = UserJpa.class)
  public User getUser(
    @ApiParam(value = "User internal id, e.g. 2", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful call (Security): /user/" + id);
    SecurityService securityService = new SecurityServiceJpa();
    try {
      authorizeApp(securityService, authToken, "retrieve the user",
          UserRole.VIEWER);
      final User user = securityService.getUser(id);
      if (user != null) {
        return new UserJpa(user);
      }
      return user;
    } catch (Exception e) {
      handleException(e, "trying to retrieve a user");
      return null;
    } finally {
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/user/name/{username}")
  @ApiOperation(value = "Get user by name", notes = "Gets the user for the specified name", response = UserJpa.class)
  public User getUser(
    @ApiParam(value = "Username, e.g. \"guest\"", required = true) @PathParam("username") String username,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Security): /user/name/" + username);
    SecurityService securityService = new SecurityServiceJpa();
    try {
      authorizeApp(securityService, authToken, "retrieve the user by username",
          UserRole.VIEWER);
      final User user = securityService.getUser(username);
      if (user != null) {
        return new UserJpa(user);
      }
      return user;
    } catch (Exception e) {
      handleException(e, "trying to retrieve a user by username");
      return null;
    } finally {
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/user/users")
  @ApiOperation(value = "Get all users", notes = "Gets all users", response = UserListJpa.class)
  public UserList getUsers(
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful call (Security): /user/users");
    SecurityService securityService = new SecurityServiceJpa();
    try {
      authorizeApp(securityService, authToken, "retrieve all users",
          UserRole.VIEWER);
      UserList list = securityService.getUsers();
      for (User user : list.getObjects()) {
        user.getProjectRoleMap().size();
      }
      return list;
    } catch (Exception e) {
      handleException(e, "trying to retrieve all users");
      return null;
    } finally {
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @PUT
  @Path("/user/add")
  @ApiOperation(value = "Add new user", notes = "Creates a new user", response = UserJpa.class)
  public User addUser(
    @ApiParam(value = "User, e.g. newUser", required = true) UserJpa user,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call PUT (Security): /user/add " + user);

    SecurityService securityService = new SecurityServiceJpa();
    try {

      authorizeApp(securityService, authToken, "add concept",
          UserRole.ADMINISTRATOR);

      // Create service and configure transaction scope
      User newUser = securityService.addUser(user);
      return newUser;
    } catch (Exception e) {
      handleException(e, "trying to add a user");
      return null;
    } finally {
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @DELETE
  @Path("/user/remove/{id}")
  @ApiOperation(value = "Remove user by id", notes = "Removes the user for the specified id")
  public void removeUser(
    @ApiParam(value = "User internal id, e.g. 2", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call DELETE (Security): /user/remove/" + id);

    SecurityService securityService = new SecurityServiceJpa();
    try {
      authorizeApp(securityService, authToken, "remove user",
          UserRole.ADMINISTRATOR);

      // Remove user
      securityService.removeUser(id);
    } catch (Exception e) {
      handleException(e, "trying to remove a user");
    } finally {
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @POST
  @Path("/user/update")
  @ApiOperation(value = "Update user", notes = "Updates the specified user")
  public void updateUser(
    @ApiParam(value = "User, e.g. update", required = true) UserJpa user,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call POST (Security): /user/update " + user);
    SecurityService securityService = new SecurityServiceJpa();
    try {
      authorizeApp(securityService, authToken, "update concept",
          UserRole.ADMINISTRATOR);
      securityService.updateUser(user);
    } catch (Exception e) {
      handleException(e, "trying to update a concept");
    } finally {
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @PUT
  @Path("/user/preferences/add")
  @ApiOperation(value = "Add new user preferences", notes = "Adds specified new user preferences. NOTE: the user.id must be set", response = UserPreferencesJpa.class)
  public UserPreferences addUserPreferences(
    @ApiParam(value = "UserPreferencesJpa, e.g. update", required = true) UserPreferencesJpa userPreferences,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call PUT (Security): /user/preferences/add "
            + userPreferences);

    SecurityService securityService = new SecurityServiceJpa();
    try {

      authorizeApp(securityService, authToken, "add new user preferences",
          UserRole.USER);

      // Create service and configure transaction scope
      UserPreferences newUserPreferences =
          securityService.addUserPreferences(userPreferences);
      return newUserPreferences;
    } catch (Exception e) {
      handleException(e, "trying to add a user prefs");
      return null;
    } finally {
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @DELETE
  @Path("/user/preferences/remove/{id}")
  @ApiOperation(value = "Remove user preferences by id", notes = "Removes the user preferences for the specified id")
  public void removeUserPreferences(
    @ApiParam(value = "User id, e.g. 2", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call DELETE (Security): /user/preferences/remove/" + id);

    SecurityService securityService = new SecurityServiceJpa();
    try {
      authorizeApp(securityService, authToken, "remove user preferences",
          UserRole.USER);

      securityService.removeUserPreferences(id);
    } catch (Exception e) {
      handleException(e, "trying to remove user preferences");
    } finally {
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @POST
  @Path("/user/preferences/update")
  @ApiOperation(value = "Update user preferences", notes = "Updates the specified user preferences and returns the updated object in case cascaded data structures were added with new identifiers", response = UserPreferencesJpa.class)
  public UserPreferences updateUserPreferences(
    @ApiParam(value = "UserPreferencesJpa, e.g. update", required = true) UserPreferencesJpa userPreferences,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call POST (Security): /user/preferences/update "
            + userPreferences);
    SecurityService securityService = new SecurityServiceJpa();
    try {
      final String userName = authorizeApp(securityService, authToken,
          "update user preferences", UserRole.VIEWER);

      if (!userPreferences.getUser().getUserName().equals(userName)) {
        throw new Exception(
            "User preferences can only be updated for this user");
      }
      userPreferences.setPrecedenceList(null);
      securityService.updateUserPreferences(userPreferences);
      final User user = securityService.getUser(userName);

      // lazy initialize
      securityService.handleLazyInit(user);

      return user.getUserPreferences();
    } catch (Exception e) {
      handleException(e, "trying to update user preferences");
    } finally {
      securityService.close();
    }
    return null;
  }

  @Override
  @GET
  @Path("/roles")
  @ApiOperation(value = "Get application roles", notes = "Gets list of valid application roles", response = StringList.class)
  public StringList getApplicationRoles(
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful POST call (Security): /roles");

    final SecurityService securityService = new SecurityServiceJpa();
    try {
      authorizeApp(securityService, authToken, "get application roles",
          UserRole.VIEWER);
      final StringList list = new StringList();
      list.setTotalCount(3);
      list.getObjects().add(UserRole.VIEWER.toString());
      list.getObjects().add(UserRole.USER.toString());
      list.getObjects().add(UserRole.ADMINISTRATOR.toString());
      return list;
    } catch (Exception e) {
      handleException(e, "trying to get roles");
      return null;
    } finally {
      securityService.close();
    }
  }

  @POST
  @Path("/user/find")
  @ApiOperation(value = "Find user", notes = "Finds a list of all users for the specified query", response = UserListJpa.class)
  @Override
  public UserList findUsers(
    @ApiParam(value = "The query", required = false) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful POST call (Security): /user/find "
            + (query == null ? "" : "query=" + query));

    // Track system level information
    final SecurityService securityService = new SecurityServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find users", UserRole.VIEWER);

      final UserList list = securityService.findUsers(query, pfs);
      for (final User user : list.getObjects()) {
        user.setUserPreferences(null);
      }
      return list;
    } catch (Exception e) {
      handleException(e, "trying to find users");
    } finally {
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @GET
  @Path("/user")
  @ApiOperation(value = "Get user by auth token", notes = "Gets the user for the specified auth token", response = UserJpa.class)
  public User getUserForAuthToken(
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Security): /user" + authToken);
    final SecurityService securityService = new SecurityServiceJpa();
    try {
      final String userName = authorizeApp(securityService, authToken,
          "retrieve the user by auth token", UserRole.VIEWER);
      final User user = securityService.getUser(userName);
      securityService.handleLazyInit(user);
      return user;
    } catch (Exception e) {
      handleException(e, "trying to retrieve a user by auth token");
      return null;
    } finally {
      securityService.close();
    }
  }
}