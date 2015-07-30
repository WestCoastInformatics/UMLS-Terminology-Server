/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.rest.client;

import java.util.Properties;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status.Family;

import org.apache.log4j.Logger;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.wci.umls.server.User;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.UserList;
import com.wci.umls.server.jpa.UserJpa;
import com.wci.umls.server.jpa.helpers.UserListJpa;
import com.wci.umls.server.jpa.services.rest.SecurityServiceRest;

/**
 * A client for connecting to a security REST service.
 */
public class SecurityClientRest extends RootClientRest implements
    SecurityServiceRest {

  /** The config. */
  private Properties config = null;

  /**
   * Instantiates a {@link ContentClientRest} from the specified parameters.
   *
   * @param config the config
   */
  public SecurityClientRest(Properties config) {
    this.config = config;
  }

  /* see superclass */
  @Override
  public String authenticate(String username, String password) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Security Client - authenticate " + username);
    validateNotEmpty(username, "username");
    validateNotEmpty(password, "password");
    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url")
            + "/security/authenticate/" + username);
    resource.accept(MediaType.APPLICATION_JSON);
    ClientResponse response = resource.post(ClientResponse.class, password);
    String resultString = response.getEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
    // return auth token
    return resultString.replaceAll("\"", "");
  }

  /* see superclass */
  @Override
  public String logout(String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Security Client - logout");
    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url") + "/security/logout/"
            + authToken);
    resource.accept(MediaType.APPLICATION_JSON);
    ClientResponse response = resource.get(ClientResponse.class);

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
    return null;
  }

  /* see superclass */
  @Override
  public User getUser(Long id, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Security Client - get user " + id);
    validateNotEmpty(id, "id");
    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url") + "/security/user/id/"
            + id);
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).get(ClientResponse.class);

    if (response.getStatus() == 204)
      return null;

    String resultString = response.getEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    UserJpa user =
        (UserJpa) ConfigUtility.getGraphForString(resultString, UserJpa.class);
    return user;
  }

  /* see superclass */
  @Override
  public User getUser(String username, String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Security Client - get user " + username);
    validateNotEmpty(username, "username");
    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url") + "/security/user/name/"
            + username);
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).get(ClientResponse.class);

    if (response.getStatus() == 204)
      return null;

    String resultString = response.getEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    UserJpa user =
        (UserJpa) ConfigUtility.getGraphForString(resultString, UserJpa.class);
    return user;
  }

  /* see superclass */
  @Override
  public UserList getUsers(String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Security Client - get users");
    Client client = Client.create();
    WebResource resource =
        client
            .resource(config.getProperty("base.url") + "/security/user/users");
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).get(ClientResponse.class);

    String resultString = response.getEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    UserListJpa list =
        (UserListJpa) ConfigUtility.getGraphForString(resultString,
            UserListJpa.class);
    return list;
  }

  /* see superclass */
  @Override
  public User addUser(UserJpa user, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Security Client - add user " + user);
    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url") + "/security/user/add");

    String userString =
        (user != null ? ConfigUtility.getStringForGraph(user) : "");
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .header("Content-type", MediaType.APPLICATION_XML)
            .put(ClientResponse.class, userString);

    String resultString = response.getEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    UserJpa result =
        (UserJpa) ConfigUtility.getGraphForString(resultString, UserJpa.class);

    return result;
  }

  /* see superclass */
  @Override
  public void removeUser(Long id, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Security Client - remove user " + id);
    validateNotEmpty(id, "id");
    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url")
            + "/security/user/remove/" + id);
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).delete(ClientResponse.class);

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing
    } else {
      throw new Exception(response.toString());
    }
  }

  /* see superclass */
  @Override
  public void updateUser(UserJpa user, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Security Client - update user " + user);
    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url")
            + "/security/user/update");

    String userString =
        (user != null ? ConfigUtility.getStringForGraph(user) : "");
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .header("Content-type", MediaType.APPLICATION_XML)
            .post(ClientResponse.class, userString);

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing
    } else {
      throw new Exception(response.toString());
    }
  }

}
