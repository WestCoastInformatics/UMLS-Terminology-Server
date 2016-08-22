/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.rest.impl;

import java.util.List;

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

import com.wci.umls.server.Project;
import com.wci.umls.server.User;
import com.wci.umls.server.UserRole;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.KeyValuePairList;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.helpers.LogEntry;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.ProjectList;
import com.wci.umls.server.helpers.QueryType;
import com.wci.umls.server.helpers.StringList;
import com.wci.umls.server.helpers.UserList;
import com.wci.umls.server.jpa.ProjectJpa;
import com.wci.umls.server.jpa.UserJpa;
import com.wci.umls.server.jpa.actions.AtomicActionListJpa;
import com.wci.umls.server.jpa.actions.MolecularActionListJpa;
import com.wci.umls.server.jpa.algo.maint.ReloadConfigPropertiesAlgorithm;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.helpers.ProjectListJpa;
import com.wci.umls.server.jpa.helpers.UserListJpa;
import com.wci.umls.server.jpa.services.ProjectServiceJpa;
import com.wci.umls.server.jpa.services.SecurityServiceJpa;
import com.wci.umls.server.jpa.services.rest.ProjectServiceRest;
import com.wci.umls.server.model.actions.AtomicActionList;
import com.wci.umls.server.model.actions.MolecularActionList;
import com.wci.umls.server.services.ProjectService;
import com.wci.umls.server.services.SecurityService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Info;
import io.swagger.annotations.SwaggerDefinition;

/**
 * REST implementation for {@link ProjectServiceRest}..
 */
@Path("/project")
@Api(value = "/project")
@SwaggerDefinition(info = @Info(description = "Operations to interact with project info.", title = "Project API", version = "1.0.1"))
@Consumes({
    MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
})
@Produces({
    MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
})
public class ProjectServiceRestImpl extends RootServiceRestImpl
    implements ProjectServiceRest {

  /** The security service. */
  private SecurityService securityService;

  /**
   * Instantiates an empty {@link ProjectServiceRestImpl}.
   *
   * @throws Exception the exception
   */
  public ProjectServiceRestImpl() throws Exception {
    securityService = new SecurityServiceJpa();
  }

  /* see superclass */
  @Override
  @PUT
  @Path("/add")
  @ApiOperation(value = "Add new project", notes = "Creates a new project", response = ProjectJpa.class)
  public Project addProject(
    @ApiParam(value = "Project, e.g. newProject", required = true) ProjectJpa project,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call PUT (Project): /add " + project);

    final ProjectService projectService = new ProjectServiceJpa();
    try {
      final String userName = authorizeApp(securityService, authToken,
          "add project", UserRole.ADMINISTRATOR);
      projectService.setLastModifiedBy(userName);

      // check to see if project already exists
      for (final Project p : projectService.getProjects().getObjects()) {
        if (p.getName().equals(project.getName())
            && p.getDescription().equals(project.getDescription())) {
          throw new LocalException(
              "A project with this name and description already exists");
        }
      }

      // Add project
      final Project newProject = projectService.addProject(project);
      projectService.addLogEntry(userName, project.getId(), project.getId(),
          null, null, "ADD project - " + project);

      return newProject;
    } catch (Exception e) {
      handleException(e, "trying to add a project");
      return null;
    } finally {
      projectService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @POST
  @Path("/update")
  @ApiOperation(value = "Update project", notes = "Updates the specified project")
  public void updateProject(
    @ApiParam(value = "Project, e.g. existingProject", required = true) ProjectJpa project,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call PUT (Project): /update " + project);

    // Create service and configure transaction scope
    final ProjectService projectService = new ProjectServiceJpa();
    try {
      final String userName = authorizeApp(securityService, authToken,
          "update project", UserRole.ADMINISTRATOR);
      projectService.setLastModifiedBy(userName);
      // check to see if project already exists
      final Project origProject = projectService.getProject(project.getId());
      if (origProject == null) {
        throw new Exception("Project " + project.getId() + " does not exist");
      }

      // Update project

      project.setPrecedenceList(origProject.getPrecedenceList());
      projectService.updateProject(project);

      projectService.addLogEntry(userName, project.getId(), project.getId(),
          null, null, "UPDATE project " + project);

    } catch (Exception e) {
      handleException(e, "trying to update a project");
    } finally {
      projectService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @DELETE
  @Path("/remove/{id}")
  @ApiOperation(value = "Remove project", notes = "Removes the project with the specified id")
  public void removeProject(
    @ApiParam(value = "Project id, e.g. 3", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call DELETE (Project): /remove/" + id);

    final ProjectService projectService = new ProjectServiceJpa();
    try {
      final String userName = authorizeApp(securityService, authToken,
          "remove project", UserRole.ADMINISTRATOR);
      projectService.setLastModifiedBy(userName);
      // Create service and configure transaction scope
      projectService.removeProject(id);

      projectService.addLogEntry(userName, id, id, null, null,
          "REMOVE project " + id);

    } catch (Exception e) {
      handleException(e, "trying to remove a project");
    } finally {
      projectService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @GET
  @Path("/{id}")
  @ApiOperation(value = "Get project for id", notes = "Gets the project for the specified id", response = ProjectJpa.class)
  public Project getProject(
    @ApiParam(value = "Project internal id, e.g. 2", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful call (Project): /" + id);

    final ProjectService projectService = new ProjectServiceJpa();
    try {
      authorizeApp(securityService, authToken, "get the project",
          UserRole.VIEWER);
      return projectService.getProject(id);
    } catch (Exception e) {
      handleException(e, "trying to get a project");
      return null;
    } finally {
      projectService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @GET
  @Path("/all")
  @ApiOperation(value = "Get all projects", notes = "Gets all projects", response = ProjectListJpa.class)
  public ProjectList getProjects(
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful call (Project): /all");

    final ProjectService projectService = new ProjectServiceJpa();
    try {
      authorizeApp(securityService, authToken, "get projects", UserRole.VIEWER);
      return projectService.getProjects();
    } catch (Exception e) {
      handleException(e, "trying to get the projects");
      return null;
    } finally {
      projectService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @GET
  @Path("/assign")
  @ApiOperation(value = "Assign user to project", notes = "Assigns the specified user to the specified project with the specified role", response = ProjectJpa.class)
  public Project assignUserToProject(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "User name, e.g. guest", required = true) @QueryParam("userName") String userName,
    @ApiParam(value = "User role, e.g. 'ADMINISTRATOR'", required = true) @QueryParam("role") UserRole role,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful POST call (Project): /assign "
        + projectId + ", " + userName + ", " + role);

    // Test preconditions
    if (projectId == null || userName == null || role == null) {
      handleException(new LocalException("Required parameter has a null value"),
          "");
    }

    final ProjectService projectService = new ProjectServiceJpa();
    try {
      final String authUser = authorizeProject(projectService, projectId,
          securityService, authToken, "add user to project", UserRole.AUTHOR);
      projectService.setLastModifiedBy(authUser);

      final User user = securityService.getUser(userName);
      final User userCopy = new UserJpa(user);
      final Project project = projectService.getProject(projectId);
      final Project projectCopy = new ProjectJpa(project);
      project.getUserRoleMap().put(userCopy, role);
      projectService.updateProject(project);

      user.getProjectRoleMap().put(projectCopy, role);
      securityService.updateUser(user);

      projectService.addLogEntry(authUser, projectId, projectId, null, null,
          "ASSIGN user to project - " + userName);

      return project;

    } catch (Exception e) {
      handleException(e, "trying to add user to project");
    } finally {
      projectService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @POST
  @Path("/{projectId}/users")
  @ApiOperation(value = "Find users assigned to project", notes = "Finds users with assigned roles on the specified project", response = UserListJpa.class)
  public UserList findAssignedUsersForProject(
    @ApiParam(value = "Project id, e.g. 3", required = true) @PathParam("projectId") Long projectId,
    @ApiParam(value = "Query", required = false) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful call PUT (Project): /"
        + projectId + "/users, " + query + ", " + pfs);

    final ProjectService projectService = new ProjectServiceJpa();
    try {
      authorizeProject(projectService, projectId, securityService, authToken,
          "find users assigned to project", UserRole.AUTHOR);

      // return all users assigned to the project
      if (pfs.getQueryRestriction() == null
          || pfs.getQueryRestriction().isEmpty()) {
        pfs.setQueryRestriction("projectAnyRole:" + projectId);
      } else {
        pfs.setQueryRestriction(
            pfs.getQueryRestriction() + " AND projectAnyRole:" + projectId);

      }
      final UserList list = securityService.findUsers(query, pfs);
      // lazy initialize with blank user prefs
      for (final User user : list.getObjects()) {
        user.setUserPreferences(null);
      }
      return list;
    } catch (Exception e) {
      handleException(e, "find users for project");
      return null;
    } finally {
      projectService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/roles")
  @ApiOperation(value = "Get project roles", notes = "Gets list of valid project roles", response = StringList.class)
  public StringList getProjectRoles(
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful POST call (Project): /roles");

    try {
      authorizeApp(securityService, authToken, "get roles", UserRole.VIEWER);
      final StringList list = new StringList();
      list.setTotalCount(3);
      list.getObjects().add(UserRole.AUTHOR.toString());
      list.getObjects().add(UserRole.REVIEWER.toString());
      list.getObjects().add(UserRole.ADMINISTRATOR.toString());
      return list;
    } catch (Exception e) {
      handleException(e, "trying to get roles");
      return null;
    } finally {
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/queryTypes")
  @ApiOperation(value = "Get query types", notes = "Gets list of valid query types", response = StringList.class)
  public StringList getQueryTypes(
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful POST call (Project): /queryTypes");

    try {
      authorizeApp(securityService, authToken, "get query types",
          UserRole.VIEWER);
      final StringList list = new StringList();
      list.setTotalCount(3);
      list.getObjects().add(QueryType.JQL.toString());
      list.getObjects().add(QueryType.SQL.toString());
      list.getObjects().add(QueryType.LUCENE.toString());
      list.getObjects().add(QueryType.PROGRAM.toString());
      return list;
    } catch (Exception e) {
      handleException(e, "trying to get query types");
      return null;
    } finally {
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @POST
  @Path("/{projectId}/users/unassigned")
  @ApiOperation(value = "Find candidate users for project", notes = "Finds users who do not yet have assigned roles on the specified project", response = UserListJpa.class)
  public UserList findUnassignedUsersForProject(
    @ApiParam(value = "Project id, e.g. 3", required = true) @PathParam("projectId") Long projectId,
    @ApiParam(value = "Query", required = false) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful call PUT (Project): /users/ "
        + projectId + "/unassigned, " + query + ", " + pfs);

    final ProjectService projectService = new ProjectServiceJpa();
    try {
      authorizeProject(projectService, projectId, securityService, authToken,
          "find candidate users for project", UserRole.AUTHOR);
      // return all users assigned to the project
      if (pfs.getQueryRestriction() != null
          && !pfs.getQueryRestriction().isEmpty()) {
        pfs.setQueryRestriction(
            pfs.getQueryRestriction() + " AND NOT projectAnyRole:" + projectId);
      } else {
        pfs.setQueryRestriction("NOT projectAnyRole:" + projectId);
      }
      final UserList list = securityService.findUsers(query, pfs);
      // lazy initialize with blank user prefs
      for (final User user : list.getObjects()) {
        user.setUserPreferences(null);
      }

      return list;
    } catch (Exception e) {
      handleException(e, "find users for project");
      return null;
    } finally {
      projectService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Produces("text/plain")
  @Path("/user/anyrole")
  @ApiOperation(value = "Determines whether the user has a project role", notes = "Returns true if the user has any role on any project", response = Boolean.class)
  public Boolean userHasSomeProjectRole(
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful POST call (Project): /user/anyrole");
    final ProjectService projectService = new ProjectServiceJpa();
    try {
      final String user = authorizeApp(securityService, authToken,
          "check for any project role", UserRole.VIEWER);

      final StringBuilder sb = new StringBuilder();
      sb.append("(");
      sb.append("userRoleMap:" + user + UserRole.ADMINISTRATOR).append(" OR ");
      sb.append("userRoleMap:" + user + UserRole.REVIEWER).append(" OR ");
      sb.append("userRoleMap:" + user + UserRole.AUTHOR).append(")");
      final ProjectList list =
          projectService.findProjects(sb.toString(), new PfsParameterJpa());
      return list.getTotalCount() != 0;

    } catch (Exception e) {
      handleException(e, "trying to check for any project role");
    } finally {
      projectService.close();
      securityService.close();
    }
    return false;
  }

  /* see superclass */
  @Override
  @GET
  @Path("/unassign")
  @ApiOperation(value = "Unassign user from project", notes = "Unassigns the specified user from the specified project", response = ProjectJpa.class)
  public Project unassignUserFromProject(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "User name, e.g. guest", required = true) @QueryParam("userName") String userName,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful POST call (Project): /unassign "
        + projectId + ", " + userName);

    // Test preconditions
    if (projectId == null || userName == null) {
      handleException(new Exception("Required parameter has a null value"), "");
    }

    final ProjectService projectService = new ProjectServiceJpa();
    try {
      // Check if user is either an ADMIN overall or an AUTHOR on this project

      String authUser = null;
      try {
        authUser = authorizeApp(securityService, authToken,
            "unassign user from project", UserRole.ADMINISTRATOR);
      } catch (Exception e) {
        // now try to validate project role
        authUser = authorizeProject(projectService, projectId, securityService,
            authToken, "unassign user from project", UserRole.AUTHOR);
      }
      projectService.setLastModifiedBy(authUser);

      User user = securityService.getUser(userName);
      final User userCopy = new UserJpa(user);
      Project project = projectService.getProject(projectId);
      final Project projectCopy = new ProjectJpa(project);

      System.out
          .println("project.userRoleMap.before = " + project.getUserRoleMap());
      System.out.println("  userCopy = " + userCopy);
      project.getUserRoleMap().remove(userCopy);
      projectService.updateProject(project);

      // reread to show
      project = projectService.getProject(projectId);
      System.out
          .println("project.userRoleMap.after = " + project.getUserRoleMap());

      System.out
          .println("user.projRoleMap.before = " + user.getProjectRoleMap());
      System.out.println("  projCopy = " + projectCopy);
      user.getProjectRoleMap().remove(projectCopy);
      securityService.updateUser(user);

      user = securityService.getUser(userName);
      System.out.println("user.projRoleMap.after= " + user.getProjectRoleMap());

      projectService.addLogEntry(authUser, projectId, projectId, null, null,
          "UNASSIGN user from project - " + userName);

      return project;
    } catch (Exception e) {
      handleException(e, "trying to remove user from project");
    } finally {
      projectService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @POST
  @Path("/all")
  @ApiOperation(value = "Finds projects", notes = "Finds projects for the specified query", response = ProjectListJpa.class)
  public ProjectList findProjects(
    @ApiParam(value = "Query", required = false) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info("RESTful call (Project): /all, " + pfs);
    final ProjectService projectService = new ProjectServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find projects",
          UserRole.VIEWER);

      return projectService.findProjects(query, pfs);
    } catch (Exception e) {
      handleException(e, "trying to get projects ");
      return null;
    } finally {
      projectService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @GET
  @Path("/log")
  @Produces("text/plain")
  @ApiOperation(value = "Get log entries", notes = "Returns log entries for specified query parameters", response = String.class)
  @Override
  public String getLog(
    @ApiParam(value = "Project id, e.g. 5", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Object id, e.g. 5", required = false) @QueryParam("objectId") Long objectId,
    @ApiParam(value = "Lines, e.g. 5", required = true) @QueryParam("lines") int lines,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful POST call (Project): /log/"
        + projectId + ", " + objectId + ", " + lines);

    final ProjectService projectService = new ProjectServiceJpa();
    try {
      authorizeProject(projectService, projectId, securityService, authToken,
          "get log entries", UserRole.AUTHOR);

      // Precondition checking -- must have projectId and objectId set
      if (projectId == null) {
        throw new LocalException("Project id and Object id must be set");
      }

      final PfsParameter pfs = new PfsParameterJpa();
      pfs.setStartIndex(0);
      pfs.setMaxResults(lines);
      pfs.setAscending(false);
      pfs.setSortField("lastModified");

      String query = "";

      // projectId and objectId must be set
      if (projectId != null) {
        query += "projectId:" + projectId;
      }
      if (objectId != null) {
        query += " AND objectId:" + objectId;
      }

      if (query.isEmpty()) {
        throw new Exception(
            "Must specify at least one parameter for querying log entries");
      }

      final List<LogEntry> entries = projectService.findLogEntries(query, pfs);

      final StringBuilder log = new StringBuilder();
      for (int i = entries.size() - 1; i >= 0; i--) {
        final LogEntry entry = entries.get(i);
        final StringBuilder message = new StringBuilder();
        message.append("[")
            .append(ConfigUtility.DATE_FORMAT4.format(entry.getLastModified()));
        message.append("] ");
        message.append(entry.getLastModifiedBy()).append(" ");
        message.append(entry.getMessage()).append("\n");
        log.append(message);
      }

      return log.toString();

    } catch (Exception e) {
      handleException(e, "trying to get log");
    } finally {
      projectService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @GET
  @Path("/log/{activity}")
  @Produces("text/plain")
  @ApiOperation(value = "Get log entries", notes = "Returns log entries for specified query parameters", response = String.class)
  @Override
  public String getLog(
    @ApiParam(value = "Terminology, e.g. SNOMED_CT", required = true) @QueryParam("terminology") String terminology,
    @ApiParam(value = "Version, e.g. 20150131", required = true) @QueryParam("version") String version,
    @ApiParam(value = "Activity, e.g. EDITING", required = true) @PathParam("activity") String activity,
    @ApiParam(value = "Lines, e.g. 5", required = true) @QueryParam("lines") int lines,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful POST call (Terminology): /log/"
        + terminology + ", " + version + ", " + activity + ", " + lines);

    final ProjectService projectService = new ProjectServiceJpa();
    try {
      authorizeApp(securityService, authToken, "get log entries",
          UserRole.VIEWER);

      // Precondition checking -- must have terminology version AND activity set
      if (terminology == null || version == null || activity == null) {
        throw new LocalException(
            "Terminology/version and activity must be set");
      }

      final PfsParameter pfs = new PfsParameterJpa();
      pfs.setStartIndex(0);
      pfs.setMaxResults(lines);
      pfs.setAscending(false);
      pfs.setSortField("lastModified");

      String query = "";

      // Terminology/version and activity must be set
      if (terminology != null) {
        query +=
            (query.length() == 0 ? "" : " AND ") + "terminology:" + terminology;
      }
      if (version != null) {
        query += (query.length() == 0 ? "" : " AND ") + "version:" + version;
      }
      if (activity != null) {
        query += " AND activity:" + activity;
      }

      if (query.isEmpty()) {
        throw new Exception(
            "Must specify at least one parameter for querying log entries");
      }

      final List<LogEntry> entries = projectService.findLogEntries(query, pfs);

      final StringBuilder log = new StringBuilder();
      for (int i = entries.size() - 1; i >= 0; i--) {
        final LogEntry entry = entries.get(i);
        final StringBuilder message = new StringBuilder();
        message.append("[")
            .append(ConfigUtility.DATE_FORMAT4.format(entry.getLastModified()));
        message.append("] ");
        message.append(entry.getLastModifiedBy()).append(" ");
        message.append(entry.getMessage()).append("\n");
        log.append(message);
      }

      return log.toString();

    } catch (Exception e) {
      handleException(e, "trying to get log");
    } finally {
      projectService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @POST
  @Path("/actions/molecular")
  @ApiOperation(value = "Get molecular actions", notes = "Get molecular actions", response = MolecularActionListJpa.class)
  public MolecularActionList findMolecularActions(
    @ApiParam(value = "Component Id, e.g. 1", required = false) @QueryParam("componentId") Long componentId,
    @ApiParam(value = "Terminology, e.g. UMLS", required = false) @QueryParam("terminology") String terminology,
    @ApiParam(value = "Version, e.g. latest", required = false) @QueryParam("version") String version,
    @ApiParam(value = "The query string", required = false) @QueryParam("query") String query,
    @ApiParam(value = "The paging/sorting/filtering parameter", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call POST (Content): /actions/molecular " + query);

    final ProjectService projectService = new ProjectServiceJpa();
    try {
      authorizeApp(securityService, authToken,
          "find molecular actions for a concept", UserRole.VIEWER);
      return projectService.findMolecularActions(componentId, terminology,
          version, query, pfs);

    } catch (Exception e) {
      handleException(e, "trying to find molecular actions for a concept");
      return null;
    } finally {
      projectService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @POST
  @Path("/actions/atomic")
  @ApiOperation(value = "Get atomic actions for a molecular action", notes = "Get atomic actions for a molecular action", response = AtomicActionListJpa.class)
  public AtomicActionList findAtomicActions(
    @ApiParam(value = "The molecularActionId id, e.g. 1", required = true) @QueryParam("molecularActionId") Long molecularActionId,
    @ApiParam(value = "The query string", required = false) @QueryParam("query") String query,
    @ApiParam(value = "The paging/sorting/filtering parameter", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call POST (Content): /actions/atomic "
            + molecularActionId + ", " + query);

    final ProjectService projectService = new ProjectServiceJpa();
    try {
      authorizeApp(securityService, authToken,
          "find atomic actions for a molecular action", UserRole.VIEWER);

      return projectService.findAtomicActions(molecularActionId, query, pfs);

    } catch (Exception e) {
      handleException(e,
          "trying to find atomic actions for a molecular action");
      return null;
    } finally {
      projectService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/checks")
  @ApiOperation(value = "Gets all validation checks", notes = "Gets all validation checks", response = KeyValuePairList.class)
  public KeyValuePairList getValidationChecks(
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful call POST (Project): /checks ");

    final ProjectService projectService = new ProjectServiceJpa();
    try {
      authorizeApp(securityService, authToken, "get validation checks",
          UserRole.VIEWER);

      final KeyValuePairList list = projectService.getValidationCheckNames();
      return list;
    } catch (Exception e) {
      handleException(e, "trying to validate all concept");
      return null;
    } finally {
      projectService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @POST
  @Path("/reload")
  @ApiOperation(value = "Reload config properties", notes = "Reloads config properties and clears caches", response = KeyValuePairList.class)
  public void reloadConfigProperties(String authToken) throws Exception {
    Logger.getLogger(getClass()).info("RESTful call POST (Project): /reload ");

    final ReloadConfigPropertiesAlgorithm algo =
        new ReloadConfigPropertiesAlgorithm();
    try {
      authorizeApp(securityService, authToken, "reload config properties",
          UserRole.ADMINISTRATOR);
      algo.compute();
    } catch (Exception e) {
      handleException(e, "trying to reload config properties");
    } finally {
      algo.close();
      securityService.close();
    }

  }

}
