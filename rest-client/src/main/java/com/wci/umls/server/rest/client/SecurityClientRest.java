/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.rest.client;

import java.net.URLEncoder;
import java.util.Properties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import org.apache.log4j.Logger;

import com.wci.umls.server.User;
import com.wci.umls.server.UserPreferences;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.StringList;
import com.wci.umls.server.helpers.UserList;
import com.wci.umls.server.jpa.UserJpa;
import com.wci.umls.server.jpa.UserPreferencesJpa;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
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
  public User authenticate(String username, String password) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Security Client - authenticate " + username);
    validateNotEmpty(username, "username");
    validateNotEmpty(password, "password");
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/security/authenticate/" + username);

    Response response =
        target.request(MediaType.APPLICATION_XML).post(Entity.text(password));
    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
    // return user
    UserJpa user = ConfigUtility.getGraphForString(resultString, UserJpa.class);
    return user;
  }

  /* see superclass */
  @Override
  public String logout(String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Security Client - logout");
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/security/logout/"
            + authToken);
    Response response = target.request(MediaType.APPLICATION_XML).get();

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
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/security/user/" + id);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).get();

    if (response.getStatus() == 204)
      return null;

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    UserJpa user = ConfigUtility.getGraphForString(resultString, UserJpa.class);
    return user;
  }

  /* see superclass */
  @Override
  public User getUser(String username, String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Security Client - get user " + username);
    validateNotEmpty(username, "username");
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/security/user/name/"
            + username);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).get();

    if (response.getStatus() == 204)
      return null;

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    UserJpa user = ConfigUtility.getGraphForString(resultString, UserJpa.class);
    return user;
  }

  /* see superclass */
  @Override
  public UserList getUsers(String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Security Client - get users");
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/security/user/users");
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    UserListJpa list =
        ConfigUtility.getGraphForString(resultString, UserListJpa.class);
    return list;
  }

  /* see superclass */
  @Override
  public User addUser(UserJpa user, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Security Client - add user " + user);
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/security/user/add");

    String userString =
        (user != null ? ConfigUtility.getStringForGraph(user) : "");
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).put(Entity.xml(userString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    UserJpa result =
        ConfigUtility.getGraphForString(resultString, UserJpa.class);

    return result;
  }

  /* see superclass */
  @Override
  public void removeUser(Long id, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Security Client - remove user " + id);
    validateNotEmpty(id, "id");
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/security/user/remove/"
            + id);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).delete();

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
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/security/user/update");

    String userString =
        (user != null ? ConfigUtility.getStringForGraph(user) : "");
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).post(Entity.xml(userString));

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing
    } else {
      throw new Exception(response.toString());
    }
  }

  /* see superclass */
  @Override
  public User getUserForAuthToken(String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Security Client - get user for auth token " + authToken);
    validateNotEmpty(authToken, "authToken");
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/security/user");
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).get();

    if (response.getStatus() == 204)
      return null;

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    UserJpa user = ConfigUtility.getGraphForString(resultString, UserJpa.class);
    return user;
  }

  /* see superclass */
  @Override
  public UserPreferences addUserPreferences(UserPreferencesJpa userPreferences,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Security Client - add user preferences " + userPreferences);
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/security/user/preferences/add");

    String userPreferencesString =
        (userPreferences != null ? ConfigUtility
            .getStringForGraph(userPreferences) : "");
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .put(Entity.xml(userPreferencesString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    UserPreferencesJpa result =
        ConfigUtility.getGraphForString(resultString, UserPreferencesJpa.class);

    return result;
  }

  /* see superclass */
  @Override
  public void removeUserPreferences(Long id, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Security Client - remove user preferences " + id);
    validateNotEmpty(id, "id");
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/security/user/preferences/remove/" + id);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).delete();

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing
    } else {
      throw new Exception(response.toString());
    }
  }

  /* see superclass */
  @Override
  public UserPreferences updateUserPreferences(
    UserPreferencesJpa userPreferences, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Security Client - update user preferences " + userPreferences);
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/security/user/preferences/update");

    String userPreferencesString =
        (userPreferences != null ? ConfigUtility
            .getStringForGraph(userPreferences) : "");
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .post(Entity.xml(userPreferencesString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    return ConfigUtility.getGraphForString(resultString,
        UserPreferencesJpa.class);
  }

  /* see superclass */
  @Override
  public StringList getApplicationRoles(String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Security Client - getApplicationRoles");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/security/roles");
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    StringList list =
        ConfigUtility.getGraphForString(resultString, StringList.class);
    return list;
  }

  /* see superclass */
  @Override
  public UserList findUsersForQuery(String query, PfsParameterJpa pfs,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Security Client - find users " + query + ", " + pfs);

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/security/user/find"
            + (query != null ? "?query="
                + URLEncoder.encode(query == null ? "" : query, "UTF-8")
                    .replaceAll("\\+", "%20") : ""));
    String pfsString =
        ConfigUtility.getStringForGraph(pfs == null ? new PfsParameterJpa()
            : pfs);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    UserListJpa list =
        ConfigUtility.getGraphForString(resultString, UserListJpa.class);
    return list;

  }

}
