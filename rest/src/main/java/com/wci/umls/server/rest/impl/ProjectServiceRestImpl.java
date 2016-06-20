/**
 * Copyright 2016 West Coast Informatics, LLC
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
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.KeyValuePairList;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.helpers.LogEntry;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.ProjectList;
import com.wci.umls.server.helpers.StringList;
import com.wci.umls.server.helpers.UserList;
import com.wci.umls.server.jpa.ProjectJpa;
import com.wci.umls.server.jpa.UserJpa;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.CodeJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.DescriptorJpa;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.helpers.ProjectListJpa;
import com.wci.umls.server.jpa.helpers.UserListJpa;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.jpa.services.ProjectServiceJpa;
import com.wci.umls.server.jpa.services.SecurityServiceJpa;
import com.wci.umls.server.jpa.services.rest.ProjectServiceRest;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.services.ContentService;
import com.wci.umls.server.services.ProjectService;
import com.wci.umls.server.services.SecurityService;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * REST implementation for {@link ProjectServiceRest}..
 */
@Path("/project")
@Consumes({
    MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
})
@Produces({
    MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
})
@Api(value = "/project", description = "Operations to get project info")
public class ProjectServiceRestImpl extends RootServiceRestImpl implements
    ProjectServiceRest {

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
    Logger.getLogger(getClass()).info(
        "RESTful call PUT (Project): /add " + project);

    ProjectService projectService = new ProjectServiceJpa();
    try {
      final String authUser =
          authorizeApp(securityService, authToken, "add project",
              UserRole.ADMINISTRATOR);

      // check to see if project already exists
      for (final Project p : projectService.getProjects().getObjects()) {
        if (p.getName().equals(project.getName())
            && p.getDescription().equals(project.getDescription())) {
          throw new Exception(
              "A project with this name and description already exists");
        }
      }

      // Add project
      project.setLastModifiedBy(securityService.getUsernameForToken(authToken));
      Project newProject = projectService.addProject(project);

      projectService.addLogEntry(authUser, project.getId(), project.getId(),
          "ADD project - " + project);

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
    Logger.getLogger(getClass()).info(
        "RESTful call PUT (Project): /update " + project);

    // Create service and configure transaction scope
    ProjectService projectService = new ProjectServiceJpa();
    try {
      String authUser =
          authorizeApp(securityService, authToken, "update project",
              UserRole.ADMINISTRATOR);

      // check to see if project already exists
      boolean found = false;
      for (final Project p : projectService.getProjects().getObjects()) {
        if (p.getId().equals(project.getId())) {
          found = true;
          break;
        }
      }
      if (!found) {
        throw new Exception("Project " + project.getId() + " does not exist");
      }

      // Update project
      project.setLastModifiedBy(securityService.getUsernameForToken(authToken));
      projectService.updateProject(project);

      projectService.addLogEntry(authUser, project.getId(), project.getId(),
          "UPDATE project " + project);

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
    Logger.getLogger(getClass()).info(
        "RESTful call DELETE (Project): /remove/" + id);

    ProjectService projectService = new ProjectServiceJpa();
    try {
      String authUser =
          authorizeApp(securityService, authToken, "remove project",
              UserRole.ADMINISTRATOR);

      // Create service and configure transaction scope
      projectService.removeProject(id);

      projectService.addLogEntry(authUser, id, id, "REMOVE project " + id);

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

    ProjectService projectService = new ProjectServiceJpa();
    try {
      authorizeApp(securityService, authToken, "get the project",
          UserRole.VIEWER);

      Project project = projectService.getProject(id);

      return project;
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

    ProjectService projectService = new ProjectServiceJpa();
    try {
      authorizeApp(securityService, authToken, "get projects", UserRole.VIEWER);

      ProjectList projects = projectService.getProjects();

      return projects;
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
    @ApiParam(value = "User role, e.g. 'ADMINISTRATOR'", required = true) @QueryParam("role") String role,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Project): /assign " + projectId + ", " + userName
            + ", " + role);

    // Test preconditions
    if (projectId == null || userName == null || role == null) {
      handleException(new Exception("Required parameter has a null value"), "");
    }

    final ProjectService projectService = new ProjectServiceJpa();
    try {
      final String authUser =
          authorizeProject(projectService, projectId, securityService,
              authToken, "add user to project", UserRole.AUTHOR);

      User user = securityService.getUser(userName);
      User userCopy = new UserJpa(user);
      Project project = projectService.getProject(projectId);
      Project projectCopy = new ProjectJpa(project);
      project.getUserRoleMap().put(userCopy, UserRole.valueOf(role));
      project.setLastModifiedBy(authUser);
      projectService.updateProject(project);

      user.getProjectRoleMap().put(projectCopy, UserRole.valueOf(role));
      securityService.updateUser(user);

      projectService.addLogEntry(authUser, projectId, projectId,
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
  @Path("/users/{projectId}")
  @ApiOperation(value = "Find users assigned to project", notes = "Finds users with assigned roles on the specified project", response = UserListJpa.class)
  public UserList findAssignedUsersForProject(
    @ApiParam(value = "Project id, e.g. 3", required = true) @PathParam("projectId") Long projectId,
    @ApiParam(value = "Query", required = false) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call PUT (Project): /users/ " + projectId + ", " + query
            + ", " + pfs);

    final ProjectService projectService = new ProjectServiceJpa();
    try {
      authorizeProject(projectService, projectId, securityService, authToken,
          "find users assigned to project", UserRole.AUTHOR);

      // return all users assigned to the project
      if (pfs.getQueryRestriction() == null
          || pfs.getQueryRestriction().isEmpty()) {
        pfs.setQueryRestriction("projectAnyRole:" + projectId);
      } else {
        pfs.setQueryRestriction(pfs.getQueryRestriction()
            + " AND projectAnyRole:" + projectId);

      }
      final UserList list = securityService.findUsersForQuery(query, pfs);
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
  @POST
  @Path("/users/{projectId}/unassigned")
  @ApiOperation(value = "Find candidate users for project", notes = "Finds users who do not yet have assigned roles on the specified project", response = UserListJpa.class)
  public UserList findUnassignedUsersForProject(
    @ApiParam(value = "Project id, e.g. 3", required = true) @PathParam("projectId") Long projectId,
    @ApiParam(value = "Query", required = false) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call PUT (Project): /users/ " + projectId + "/unassigned, "
            + query + ", " + pfs);

    final ProjectService projectService = new ProjectServiceJpa();
    try {
      authorizeProject(projectService, projectId, securityService, authToken,
          "find candidate users for project", UserRole.AUTHOR);
      // return all users assigned to the project
      if (pfs.getQueryRestriction() != null
          && !pfs.getQueryRestriction().isEmpty()) {
        pfs.setQueryRestriction(pfs.getQueryRestriction()
            + " AND NOT projectAnyRole:" + projectId);
      } else {
        pfs.setQueryRestriction("NOT projectAnyRole:" + projectId);
      }
      final UserList list = securityService.findUsersForQuery(query, pfs);
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
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Project): /user/anyrole");
    final ProjectService projectService = new ProjectServiceJpa();
    try {
      final String user =
          authorizeApp(securityService, authToken,
              "check for any project role", UserRole.VIEWER);

      final StringBuilder sb = new StringBuilder();
      sb.append("(");
      sb.append("userRoleMap:" + user + UserRole.ADMINISTRATOR).append(" OR ");
      sb.append("userRoleMap:" + user + UserRole.REVIEWER).append(" OR ");
      sb.append("userRoleMap:" + user + UserRole.AUTHOR).append(")");
      final ProjectList list =
          projectService.findProjectsForQuery(sb.toString(),
              new PfsParameterJpa());
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
    Logger.getLogger(getClass())
        .info(
            "RESTful POST call (Project): /unassign " + projectId + ", "
                + userName);

    // Test preconditions
    if (projectId == null || userName == null) {
      handleException(new Exception("Required parameter has a null value"), "");
    }

    final ProjectService projectService = new ProjectServiceJpa();
    try {
      // Check if user is either an ADMIN overall or an AUTHOR on this project

      String authUser = null;
      try {
        authUser =
            authorizeApp(securityService, authToken,
                "unassign user from project", UserRole.ADMINISTRATOR);
      } catch (Exception e) {
        // now try to validate project role
        authUser =
            authorizeProject(projectService, projectId, securityService,
                authToken, "unassign user from project", UserRole.AUTHOR);
      }

      User user = securityService.getUser(userName);
      User userCopy = new UserJpa(user);
      Project project = projectService.getProject(projectId);
      Project projectCopy = new ProjectJpa(project);

      project.getUserRoleMap().remove(userCopy);
      project.setLastModifiedBy(authUser);
      projectService.updateProject(project);

      user.getProjectRoleMap().remove(projectCopy);
      securityService.updateUser(user);

      projectService.addLogEntry(authUser, projectId, projectId,
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
  @Path("/projects")
  @ApiOperation(value = "Finds projects", notes = "Finds projects for the specified query", response = ProjectListJpa.class)
  public ProjectList findProjectsForQuery(
    @ApiParam(value = "Query", required = false) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Project): find projects for query, " + pfs);

    final ProjectService projectService = new ProjectServiceJpa();
    try {
      authorizeApp(securityService, authToken, "find projects", UserRole.VIEWER);

      return projectService.findProjectsForQuery(query, pfs);
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
    @ApiParam(value = "Terminology, e.g. SNOMED_CT", required = true) @QueryParam("terminology") String terminology,
    @ApiParam(value = "Version, e.g. 20150131", required = true) @QueryParam("version") String version,
    @ApiParam(value = "Activity, e.g. EDITING", required = false) @QueryParam("activity") String activity,
    @ApiParam(value = "Lines, e.g. 5", required = false) @QueryParam("lines") int lines,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Project): /log/" + projectId + ", " + objectId
            + ", " + terminology + ", " + version + ", " + activity + ", "
            + lines);

    final ProjectService projectService = new ProjectServiceJpa();
    try {
      authorizeProject(projectService, projectId, securityService, authToken,
          "get log entries", UserRole.AUTHOR);

      // Precondition checking -- must have terminology/version OR projectId set
      if (projectId == null && terminology == null && version == null) {
        throw new LocalException(
            "Project id or terminology/version must be set");
      }

      PfsParameter pfs = new PfsParameterJpa();
      pfs.setStartIndex(0);
      pfs.setMaxResults(lines);
      pfs.setAscending(false);
      pfs.setSortField("lastModified");

      String query = "";

      // at least one of projectId or terminology/version must be set
      if (projectId != null) {
        query += "projectId:" + projectId;
      }
      if (terminology != null) {
        query +=
            (query.length() == 0 ? "" : " AND ") + "terminology:" + terminology;
      }
      if (version != null) {
        query += (query.length() == 0 ? "" : " AND ") + "version:" + version;
      }

      // optional parameters
      if (objectId != null) {
        query += " AND objectId:" + objectId;
      }
      if (activity != null) {
        query += " AND activity:" + activity;
      }
      
      if (query.isEmpty()) {
        throw new Exception("Must specify at least one parameter for querying log entries");
      }

      final List<LogEntry> entries =
          projectService.findLogEntriesForQuery(query, pfs);

      StringBuilder log = new StringBuilder();
      for (int i = entries.size() - 1; i >= 0; i--) {
        final LogEntry entry = entries.get(i);
        final StringBuilder message = new StringBuilder();
        message.append("[").append(
            ConfigUtility.DATE_FORMAT4.format(entry.getLastModified()));
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
  @GET
  @Path("/validate/concept/merge/{terminology}/{version}/{cui1}/{cui2}")
  @ApiOperation(value = "Validate merge", notes = "Validates the merge of two concepts")
  public ValidationResult validateMerge(
    @ApiParam(value = "The project id (optional), e.g. 1", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Terminology", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Version", required = true) @PathParam("version") String version,
    @ApiParam(value = "Cui for first concept", required = true) @PathParam("cui1") String cui1,
    @ApiParam(value = "Cui for second concept", required = true) @PathParam("cui2") String cui2,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Validation): /validate/concept/merge/" + terminology
            + "/" + version + "/" + cui1 + "/" + cui2);

    ProjectService projectService = new ProjectServiceJpa();
    ContentService contentService = new ContentServiceJpa();
    try {

      // authorize call
      authorizeProject(projectService, projectId, securityService, authToken,
          "merge concepts", UserRole.USER);
      Project project = projectService.getProject(projectId);

      Concept concept1 =
          contentService.getConcept(cui1, terminology, version, Branch.ROOT);
      Concept concept2 =
          contentService.getConcept(cui2, terminology, version, Branch.ROOT);

      ValidationResult result =
          projectService.validateMerge(project, concept1, concept2);

      return result;

    } catch (Exception e) {

      handleException(e, "trying to validate the concept merge");
      return null;
    } finally {
      projectService.close();
      securityService.close();
    }
  }

  @Override
  @PUT
  @Path("/descriptor")
  @ApiOperation(value = "Validate Descriptor", notes = "Validates a descriptor")
  public ValidationResult validateDescriptor(
    @ApiParam(value = "The project id (optional), e.g. 1", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Descriptor", required = true) DescriptorJpa descriptor,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call PUT (Project): /dui " + descriptor);

    ProjectService projectService = new ProjectServiceJpa();
    try {
      Project project = projectService.getProject(projectId);
      authorizeProject(projectService, projectId, securityService, authToken,
          "validate descriptor", UserRole.USER);
      return projectService.validateDescriptor(project, descriptor);
    } catch (Exception e) {
      handleException(e, "trying to validate descriptor");
      return null;
    } finally {
      projectService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @PUT
  @Path("/atom")
  @ApiOperation(value = "Validate Atom", notes = "Validates a atom")
  public ValidationResult validateAtom(
    @ApiParam(value = "The project id (optional), e.g. 1", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Atom", required = true) AtomJpa atom,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call PUT (Project): /aui " + atom);

    ProjectService projectService = new ProjectServiceJpa();
    try {
      authorizeProject(projectService, projectId, securityService, authToken,
          "validate atom", UserRole.USER);
      Project project = projectService.getProject(projectId);
      return projectService.validateAtom(project, atom);
    } catch (Exception e) {
      handleException(e, "trying to validate atom");
      return null;
    } finally {
      projectService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @PUT
  @Path("/code")
  @ApiOperation(value = "Validate Code", notes = "Validates a code")
  public ValidationResult validateCode(
    @ApiParam(value = "The project id (optional), e.g. 1", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Code", required = true) CodeJpa code,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call PUT (Project): /code " + code);

    ProjectService projectService = new ProjectServiceJpa();
    try {
      authorizeProject(projectService, projectId, securityService, authToken,
          "validate code", UserRole.USER);
      Project project = projectService.getProject(projectId);
      return projectService.validateCode(project, code);
    } catch (Exception e) {
      handleException(e, "trying to validate code");
      return null;
    } finally {
      projectService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @PUT
  @Path("/concept")
  @ApiOperation(value = "Validate Concept", notes = "Validates a concept")
  public ValidationResult validateConcept(
    @ApiParam(value = "The project id (optional), e.g. 1", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Concept", required = true) ConceptJpa concept,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call PUT (Project): /concept " + concept);

    ProjectService projectService = new ProjectServiceJpa();
    try {
      authorizeProject(projectService, projectId, securityService, authToken,
          "validate conceptm", UserRole.USER);
      Project project = projectService.getProject(projectId);
      return projectService.validateConcept(project, concept);
    } catch (Exception e) {
      handleException(e, "trying to validate concept");
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
  @ApiOperation(value = "Gets all validation checks for a project", notes = "Gets all validation checks for a project", response = KeyValuePairList.class)
  public KeyValuePairList getValidationChecks(
    @ApiParam(value = "The project id , e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call POST (Validation): /checks ");

    final ProjectService projectService = new ProjectServiceJpa();
    try {
      authorizeApp(securityService, authToken, "get validation checks",
          UserRole.VIEWER);

      Project project = projectService.getProject(projectId);
      final KeyValuePairList list =
          projectService.getValidationCheckNames(project);
      return list;
    } catch (Exception e) {
      handleException(e, "trying to validate all concept");
      return null;
    } finally {
      projectService.close();
      securityService.close();
    }
  }
  
 

}
