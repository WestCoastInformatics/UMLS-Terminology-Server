/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.rest.impl;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.wci.umls.server.Project;
import com.wci.umls.server.User;
import com.wci.umls.server.UserRole;
import com.wci.umls.server.helpers.ChangeEventList;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.HasProject;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.jpa.helpers.content.ChangeEventListJpa;
import com.wci.umls.server.model.actions.ChangeEvent;
import com.wci.umls.server.model.content.AtomClass;
import com.wci.umls.server.services.ContentService;
import com.wci.umls.server.services.ProjectService;
import com.wci.umls.server.services.SecurityService;
import com.wci.umls.server.services.handlers.ExceptionHandler;

/**
 * Top level class for all REST services.
 */
public class RootServiceRestImpl {

  /** The websocket. */
  private static NotificationWebsocket websocket = null;

  /**
   * Instantiates an empty {@link RootServiceRestImpl}.
   */
  public RootServiceRestImpl() {
    // do nothing
  }

  /**
   * Handle exception.
   *
   * @param e the e
   * @param whatIsHappening the what is happening
   */
  @SuppressWarnings("static-method")
  public void handleException(Exception e, String whatIsHappening) {
    try {
      ExceptionHandler.handleException(e, whatIsHappening, "");
    } catch (Exception e1) {
      // do nothing
    }

    // Ensure message has quotes.
    // When migrating from jersey 1 to jersey 2, messages no longer
    // had quotes around them when returned to client and angular
    // could not parse them as json.
    String message = e.getMessage();
    if (message != null && !message.startsWith("\"")) {
      message = "\"" + message + "\"";
    }
    // throw the local exception as a web application exception
    if (e instanceof LocalException) {
      throw new WebApplicationException(Response.status(500).entity(message).build());
    }

    // throw the web application exception as-is, e.g. for 401 errors
    if (e instanceof WebApplicationException) {
      throw new WebApplicationException(message, e);
    }
    throw new WebApplicationException(Response.status(500)
        .entity("\"Unexpected error " + whatIsHappening + ". Please contact the administrator.\"")
        .build());

  }

  /**
   * Authorize the users application role.
   *
   * @param securityService the security service
   * @param authToken the auth token
   * @param perform the perform
   * @param authRole the auth role
   * @return the string
   * @throws Exception the exception
   */
  public static String authorizeApp(SecurityService securityService, String authToken,
    String perform, UserRole authRole) throws Exception {
    // authorize call
    UserRole role = securityService.getApplicationRoleForToken(authToken);
    UserRole cmpRole = authRole;
    if (cmpRole == null) {
      cmpRole = UserRole.VIEWER;
    }
    if (!role.hasPrivilegesOf(cmpRole))
      throw new WebApplicationException(Response.status(401)
          .entity("User does not have permissions to " + perform + ".").build());
    return securityService.getUsernameForToken(authToken);
  }

  /**
   * Authorize the users project role or accept application ADMIN.
   *
   * @param projectService the project service
   * @param projectId the project id
   * @param securityService the security service
   * @param authToken the auth token
   * @param perform the perform
   * @param requiredProjectRole the required project role
   * @return the username
   * @throws Exception the exception
   */
  public static String authorizeProject(ProjectService projectService, Long projectId,
    SecurityService securityService, String authToken, String perform, UserRole requiredProjectRole)
    throws Exception {

    // Get userName
    final String userName = securityService.getUsernameForToken(authToken);

    // Allow application admin to do anything
    final UserRole appRole = securityService.getApplicationRoleForToken(authToken);
    if (appRole == UserRole.USER || appRole == UserRole.ADMINISTRATOR) {
      return userName;
    }

    // Verify that user project role has privileges of required role
    final Project project = projectService.getProject(projectId);
    if (project == null) {
      throw new Exception("Missing project for id" + projectId);
    }
    final UserRole role = project.getUserRoleMap().get(securityService.getUser(userName));
    final UserRole projectRole = (role == null) ? UserRole.VIEWER : role;
    if (!projectRole.hasPrivilegesOf(requiredProjectRole))
      throw new WebApplicationException(Response.status(401)
          .entity("User does not have permissions to " + perform + ".").build());

    // return username
    return userName;
  }

  /**
   * Returns the total elapsed time str.
   *
   * @param time the time
   * @return the total elapsed time str
   */
  @SuppressWarnings({
      "boxing"
  })
  protected static String getTotalElapsedTimeStr(long time) {
    Long resultnum = (System.nanoTime() - time) / 1000000000;
    String result = resultnum.toString() + "s";
    resultnum = resultnum / 60;
    result = result + " / " + resultnum.toString() + "m";
    resultnum = resultnum / 60;
    result = result + " / " + resultnum.toString() + "h";
    return result;
  }

  /**
   * Returns the notification websocket.
   *
   * @return the notification websocket
   */
  public static NotificationWebsocket getNotificationWebsocket() {
    return websocket;
  }

  /**
   * Sets the notification websocket.
   *
   * @param websocket2 the notification websocket
   */
  public static void setNotificationWebsocket(NotificationWebsocket websocket2) {
    websocket = websocket2;
  }

  /**
   * Send change event.
   *
   * @param key the key
   * @param event the event
   * @throws Exception the exception
   */
  public static void sendChangeEvent(String key, ChangeEvent event) throws Exception {
    if (websocket != null) {
      websocket.send(key, ConfigUtility.getJsonForGraph(event));
    }
  }

  /**
   * Send change events.
   *
   * @param key the key
   * @param events the events
   * @throws Exception the exception
   */
  public static void sendChangeEvents(String key, ChangeEvent... events) throws Exception {
    if (websocket != null) {
      final ChangeEventList list = new ChangeEventListJpa();
      for (final ChangeEvent event : events) {
        list.getObjects().add(event);
      }
      websocket.send(key, ConfigUtility.getJsonForGraph(list));
    }
  }

  /**
   * Verify project.
   *
   * @param p the p
   * @param projectId the project id
   * @throws Exception the exception
   */
  public static void verifyProject(HasProject p, Long projectId) throws Exception {
    if (p == null || p.getProject() == null || !p.getProject().getId().equals(projectId)) {
      throw new Exception("Mismatched project ids: " + projectId + ", "
          + (p == null ? "null" : (p.getProject() == null ? "null" : p.getProject().getId())));
    }
  }

  /**
   * Returns the precedence list. If the user has a local one, use that, if the
   * project has a local one, use that, otherwise use the defaults for the
   * object's terminology/version.
   *
   * @param service the service
   * @param contentService the content service
   * @param userName the user name
   * @param obj the obj
   * @param project the project
   * @return the precedence list
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  public PrecedenceList sortAtoms(SecurityService service, ContentService contentService,
    String userName, AtomClass obj, Project project) throws Exception {
    PrecedenceList list = null;
    final User user = service.getUser(userName);
    if (user.getUserPreferences() != null
        && user.getUserPreferences().getPrecedenceList() != null) {
      list = user.getUserPreferences().getPrecedenceList();
    } else if (project != null) {
      final Project lproject = (project != null ? project
          : contentService.getProject(user.getUserPreferences().getLastProjectId()));
      final PrecedenceList projectList = lproject.getPrecedenceList();
      if (projectList != null) {
        list = projectList;
      }
    }
    // If nothing else, use the terminology/version of the object
    if (list == null) {
      list = contentService.getPrecedenceList(obj.getTerminology(), obj.getVersion());
    }

    obj.setAtoms(contentService.getComputePreferredNameHandler(obj.getTerminology())
        .sortAtoms(obj.getAtoms(), list));
    return list;
  }

}
