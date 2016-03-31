/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.rest.impl;

import java.util.Calendar;
import java.util.Date;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.wci.umls.server.UserRole;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.helpers.LogEntry;
import com.wci.umls.server.jpa.helpers.LogEntryJpa;
import com.wci.umls.server.model.meta.LogActivity;
import com.wci.umls.server.services.ProjectService;
import com.wci.umls.server.services.RootService;
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
      throw new WebApplicationException(
          Response.status(500).entity(message).build());
    }

    // throw the web application exception as-is, e.g. for 401 errors
    if (e instanceof WebApplicationException) {
      throw new WebApplicationException(message, e);
    }
    throw new WebApplicationException(
        Response
            .status(500).entity("\"Unexpected error trying to "
                + whatIsHappening + ". Please contact the administrator.\"")
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
  public static String authorizeApp(SecurityService securityService,
    String authToken, String perform, UserRole authRole) throws Exception {
    // authorize call
    UserRole role = securityService.getApplicationRoleForToken(authToken);
    UserRole cmpRole = authRole;
    if (cmpRole == null) {
      cmpRole = UserRole.VIEWER;
    }
    if (!role.hasPrivilegesOf(cmpRole))
      throw new WebApplicationException(Response.status(401)
          .entity("User does not have permissions to " + perform + ".")
          .build());
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
  public static String authorizeProject(ProjectService projectService,
    Long projectId, SecurityService securityService, String authToken,
    String perform, UserRole requiredProjectRole) throws Exception {

    // Get userName
    final String userName = securityService.getUsernameForToken(authToken);

    // Allow application admin to do anything
    UserRole appRole = securityService.getApplicationRoleForToken(authToken);
    if (appRole == UserRole.USER || appRole == UserRole.ADMINISTRATOR) {
      return userName;
    }

    // Verify that user project role has privileges of required role
    UserRole role =
        projectService.getProject(projectId).getUserRoleMap()
            .get(securityService.getUser(userName));
    UserRole projectRole = (role == null) ? UserRole.VIEWER : role;
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
  public static void setNotificationWebsocket(
    NotificationWebsocket websocket2) {
    websocket = websocket2;
  }

  /**
   * Adds the log entry.
   *
   * @param service the service
   * @param userName the user name
   * @param action the action
   * @param projectId the project id
   * @param objectId the object id
   * @param detail the detail
   * @return the log entry
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  public LogEntry addLogEntry(RootService service, String userName,
    String action, Long projectId, Long objectId, String detail, LogActivity activity)
    throws Exception {
    LogEntry entry = new LogEntryJpa();
    entry.setLastModifiedBy(userName);
    entry.setObjectId(objectId);
    entry.setProjectId(projectId);
    entry.setActivity(activity);
    entry.setTimestamp(new Date());

    // $action (projectId=$projectId, objectId=$objectId): $detail
    StringBuilder message = new StringBuilder();
    Calendar c = Calendar.getInstance();
    message.append("[").append(ConfigUtility.DATE_FORMAT4.format(c.getTime()));
    message.append("] ");
    message.append(userName).append(" ");
    message.append(action).append(" (projectId=");
    message.append(projectId).append(", objectId=");
    message.append(objectId).append("): ");
    message.append(detail).append("\n");

    entry.setMessage(message.toString());

    // Add component
    LogEntry newLogEntry = service.addLogEntry(entry);

    // do not inform listeners
    return newLogEntry;

  }
}
