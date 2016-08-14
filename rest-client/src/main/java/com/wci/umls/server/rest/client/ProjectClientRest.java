/*
 *    Copyright 2015 West Coast Informatics, LLC
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

import com.wci.umls.server.Project;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.KeyValuePairList;
import com.wci.umls.server.helpers.ProjectList;
import com.wci.umls.server.helpers.StringList;
import com.wci.umls.server.helpers.UserList;
import com.wci.umls.server.jpa.ProjectJpa;
import com.wci.umls.server.jpa.actions.AtomicActionListJpa;
import com.wci.umls.server.jpa.actions.MolecularActionListJpa;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.helpers.ProjectListJpa;
import com.wci.umls.server.jpa.helpers.UserListJpa;
import com.wci.umls.server.jpa.services.rest.ProjectServiceRest;
import com.wci.umls.server.model.actions.AtomicActionList;
import com.wci.umls.server.model.actions.MolecularActionList;

/**
 * A client for connecting to a project REST service.
 */
public class ProjectClientRest extends RootClientRest
    implements ProjectServiceRest {

  /** The config. */
  private Properties config = null;

  /**
   * Instantiates a {@link ProjectClientRest} from the specified parameters.
   *
   * @param config the config
   */
  public ProjectClientRest(Properties config) {
    this.config = config;
  }

  /* see superclass */
  @Override
  public Project addProject(ProjectJpa project, String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Project Client - add project" + project);

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/project/add");

    String projectString = ConfigUtility
        .getStringForGraph(project == null ? new ProjectJpa() : project);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).put(Entity.xml(projectString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(resultString);
    }

    // converting to object
    ProjectJpa result =
        ConfigUtility.getGraphForString(resultString, ProjectJpa.class);

    return result;
  }

  /* see superclass */
  @Override
  public void updateProject(ProjectJpa project, String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Project Client - update project " + project);
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/project/update");

    String projectString = ConfigUtility
        .getStringForGraph(project == null ? new ProjectJpa() : project);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.xml(projectString));

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing, successful
    } else {
      throw new Exception("Unexpected status - " + response.getStatus());
    }
  }

  /* see superclass */
  @Override
  public void removeProject(Long id, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Project Client - remove project " + id);
    validateNotEmpty(id, "id");
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/project/remove/" + id);

    if (id == null)
      return;

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).delete();

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing, successful
    } else {
      throw new Exception("Unexpected status - " + response.getStatus());
    }
  }

  /* see superclass */
  @Override
  public Project getProject(Long id, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Project Client - get project " + id);
    validateNotEmpty(id, "id");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/project/" + id);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    ProjectJpa project =
        ConfigUtility.getGraphForString(resultString, ProjectJpa.class);
    return project;
  }

  /* see superclass */
  @Override
  public ProjectList getProjects(String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Project Client - get projects");
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/project/all");
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(resultString);
    }

    // converting to object
    ProjectListJpa list =
        ConfigUtility.getGraphForString(resultString, ProjectListJpa.class);
    return list;
  }

  /* see superclass */
  @Override
  public Project assignUserToProject(Long projectId, String userName,
    String role, String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Project Client - assign user to project " + projectId + ", "
            + userName + ", " + role);
    validateNotEmpty(projectId, "projectId");
    validateNotEmpty(userName, "userName");
    validateNotEmpty(role, "role");

    Client client = ClientBuilder.newClient();
    WebTarget target = client
        .target(config.getProperty("base.url") + "/project/assign?projectId="
            + projectId + "&userName=" + userName + "&role=" + role);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    ProjectJpa project =
        ConfigUtility.getGraphForString(resultString, ProjectJpa.class);
    return project;

  }

  /* see superclass */
  @Override
  public Project unassignUserFromProject(Long projectId, String userName,
    String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Project Client - assign user to project " + projectId + ", "
            + userName);
    validateNotEmpty(projectId, "projectId");
    validateNotEmpty(userName, "userName");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/project/unassign?projectId=" + projectId + "&userName=" + userName);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    ProjectJpa project =
        ConfigUtility.getGraphForString(resultString, ProjectJpa.class);
    return project;

  }

  /* see superclass */
  @Override
  public StringList getProjectRoles(String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Project Client - getProjectRoles");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/project/roles");
    Response response = target.request(MediaType.APPLICATION_XML)
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
  public StringList getQueryTypes(String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Project Client - getQueryTypes");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/project/queryTypes");
    Response response = target.request(MediaType.APPLICATION_XML)
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
  public UserList findAssignedUsersForProject(Long projectId, String query,
    PfsParameterJpa pfs, String authToken) throws Exception {
    validateNotEmpty(projectId, "projectId");
    validateNotEmpty(query, "query");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/project/" + projectId + "/users?query="
        + URLEncoder.encode(query == null ? "" : query, "UTF-8")
            .replaceAll("\\+", "%20"));
    String pfsString = ConfigUtility
        .getStringForGraph(pfs == null ? new PfsParameterJpa() : pfs);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    UserList list =
        ConfigUtility.getGraphForString(resultString, UserListJpa.class);
    return list;
  }

  /* see superclass */
  @Override
  public UserList findUnassignedUsersForProject(Long projectId, String query,
    PfsParameterJpa pfs, String authToken) throws Exception {
    validateNotEmpty(projectId, "projectId");
    validateNotEmpty(query, "query");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/project/users/" + projectId + "/unassigned" + "?query="
        + URLEncoder.encode(query == null ? "" : query, "UTF-8")
            .replaceAll("\\+", "%20"));
    String pfsString = ConfigUtility
        .getStringForGraph(pfs == null ? new PfsParameterJpa() : pfs);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    UserList list =
        ConfigUtility.getGraphForString(resultString, UserListJpa.class);
    return list;
  }

  /* see superclass */
  @Override
  public Boolean userHasSomeProjectRole(String authToken) throws Exception {

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/project/user/anyrole");
    Response response = target.request(MediaType.TEXT_PLAIN)
        .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    return resultString.equals("true");

  }

  /* see superclass */
  @Override
  public ProjectList findProjects(String query, PfsParameterJpa pfs,
    String authToken) throws Exception {
    validateNotEmpty(query, "query");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/project/all"
            + "?query=" + URLEncoder.encode(query == null ? "" : query, "UTF-8")
                .replaceAll("\\+", "%20"));
    String pfsString = ConfigUtility
        .getStringForGraph(pfs == null ? new PfsParameterJpa() : pfs);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    ProjectList list =
        ConfigUtility.getGraphForString(resultString, ProjectListJpa.class);
    return list;
  }

  /* see superclass */
  @Override
  public String getLog(Long projectId, Long objectId, int lines,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Project Client - get log");

    validateNotEmpty(projectId, "projectId");
    validateNotEmpty(objectId, "object");
    validateNotEmpty(authToken, "Authorization");

    Client client = ClientBuilder.newClient();
    WebTarget target = client
        .target(config.getProperty("base.url") + "/project/log?" + "projectId="
            + projectId + "&objectId=" + objectId + "&lines=" + lines);
    Response response = target.request(MediaType.TEXT_PLAIN)
        .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    return resultString;

  }

  /* see superclass */
  @Override
  public String getLog(String terminology, String version, String activity,
    int lines, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Project Client - get log");

    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");
    validateNotEmpty(activity, "activity");
    validateNotEmpty(authToken, "Authorization");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/project/log/" + activity + "?" + "terminology=" + terminology
        + "&version=" + version + "&lines=" + lines);
    Response response = target.request(MediaType.TEXT_PLAIN)
        .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    return resultString;

  }

  /* see superclass */
  @Override
  public MolecularActionList findMolecularActions(Long componentId,
    String terminology, String version, String query, PfsParameterJpa pfs,
    String authToken) throws Exception {

    Logger.getLogger(getClass())
        .debug("Project Client - find molecular actions " + query);

    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(terminology, "version");

    final Client client = ClientBuilder.newClient();
    final WebTarget target = client.target(config.getProperty("base.url")
        + "/project/actions/molecular?terminology=" + terminology + "&version="
        + version
        + (componentId == null ? "" : "&componentId=" + componentId)
        + "&query=" + URLEncoder.encode(query == null ? "" : query, "UTF-8")
            .replaceAll("\\+", "%20"));
    final String pfsString = ConfigUtility
        .getStringForGraph(pfs == null ? new PfsParameterJpa() : pfs);
    final Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.xml(pfsString));

    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    return ConfigUtility.getGraphForString(resultString,
        MolecularActionListJpa.class);
  }

  /* see superclass */
  @Override
  public AtomicActionList findAtomicActions(Long molecularActionId,
    String query, PfsParameterJpa pfs, String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Client - find atomic actions for molecular action "
            + molecularActionId);
    validateNotEmpty(molecularActionId, "molecularActionId");

    final Client client = ClientBuilder.newClient();
    final WebTarget target = client.target(config.getProperty("base.url")
        + "/project/actions/atomic?molecularActionId=" + molecularActionId
        + (query == null ? "" : "&query="
            + URLEncoder.encode(query, "UTF-8").replaceAll("\\+", "%20")));
    final String pfsString = ConfigUtility
        .getStringForGraph(pfs == null ? new PfsParameterJpa() : pfs);
    final Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.xml(pfsString));

    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    return ConfigUtility.getGraphForString(resultString,
        AtomicActionListJpa.class);
  }

  /**
   * Returns the validation checks.
   *
   * @param authToken the auth token
   * @return the validation checks
   * @throws Exception the exception
   */
  @Override
  public KeyValuePairList getValidationChecks(String authToken)
    throws Exception {
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/checks");

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      Logger.getLogger(getClass()).debug(resultString);
    } else {
      throw new Exception(resultString);
    }

    // converting to object
    return ConfigUtility.getGraphForString(resultString,
        KeyValuePairList.class);

  }

  /* see superclass */
  @Override
  public void reloadConfigProperties(String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Project Client - reload config properties");

    final Client client = ClientBuilder.newClient();
    final WebTarget target =
        client.target(config.getProperty("base.url") + "/project/reload");
    final Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.text(""));

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing
    } else {
      throw new Exception("Unexpected status " + response.getStatus());
    }

  }
}
