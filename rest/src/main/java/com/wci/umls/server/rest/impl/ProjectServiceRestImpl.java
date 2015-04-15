/**
 * Copyright 2015 West Coast Informatics, LLC
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
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import com.wci.umls.server.Project;
import com.wci.umls.server.UserRole;
import com.wci.umls.server.helpers.ProjectList;
import com.wci.umls.server.helpers.content.ConceptList;
import com.wci.umls.server.jpa.ProjectJpa;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.services.ProjectServiceJpa;
import com.wci.umls.server.jpa.services.SecurityServiceJpa;
import com.wci.umls.server.jpa.services.rest.ProjectServiceRest;
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
@Api(value = "/project", description = "Operations to retrieve project info.")
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.ts.rest.ContentServiceRest#addProject(org.ihtsdo.otf.ts.
   * jpa.ProjectJpa, java.lang.String)
   */
  @Override
  @PUT
  @Path("/add")
  @ApiOperation(value = "Add new project", notes = "Creates a new project.", response = Project.class)
  public Project addProject(
    @ApiParam(value = "Project, e.g. newProject", required = true) ProjectJpa project,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call PUT (ContentChange): /add " + project);

    try {
      authenticate(securityService, authToken, "add project",
          UserRole.ADMINISTRATOR);

      // Create service and configure transaction scope
      ProjectService projectService = new ProjectServiceJpa();

      // check to see if project already exists
      for (Project p : projectService.getProjects().getObjects()) {
        if (p.getName().equals(project.getName())
            && p.getDescription().equals(project.getDescription())) {
          throw new Exception(
              "A project with this name and description already exists.");
        }
      }

      // Add project
      project.setLastModifiedBy(securityService.getUsernameForToken(authToken));
      Project newProject = projectService.addProject(project);
      projectService.close();
      return newProject;
    } catch (Exception e) {
      handleException(e, "trying to add a project");
      return null;
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.ts.rest.ProjectServiceRest#updateProject(org.ihtsdo.otf.
   * ts.jpa.ProjectJpa, java.lang.String)
   */
  @Override
  @POST
  @Path("/update")
  @ApiOperation(value = "Update project", notes = "Updates the specified project.")
  public void updateProject(
    @ApiParam(value = "Project, e.g. existingProject", required = true) ProjectJpa project,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call PUT (ContentChange): /update " + project);

    try {
      authenticate(securityService, authToken, "update project",
          UserRole.ADMINISTRATOR);

      // Create service and configure transaction scope
      ProjectService projectService = new ProjectServiceJpa();

      // check to see if project already exists
      boolean found = false;
      for (Project p : projectService.getProjects().getObjects()) {
        if (p.getId().equals(project.getId())) {
          found = true;
          break;
        }
      }
      if (!found) {
        throw new Exception("Project " + project.getId() + " does not exist.");
      }

      // Update project
      project.setLastModifiedBy(securityService.getUsernameForToken(authToken));
      projectService.updateProject(project);
      projectService.close();

    } catch (Exception e) {
      handleException(e, "trying to update a project");
    }
  }

  @Override
  @DELETE
  @Path("/remove/id/{id}")
  @ApiOperation(value = "Delete project", notes = "Deletes the project with the specified id.")
  public void removeProject(
    @ApiParam(value = "Project id, e.g. 3", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call PUT (ContentChange): /remove/id/" + id);

    try {
      authenticate(securityService, authToken, "remove project",
          UserRole.ADMINISTRATOR);

      // Create service and configure transaction scope
      ProjectService projectService = new ProjectServiceJpa();
      projectService.removeProject(id);
      projectService.close();

    } catch (Exception e) {
      handleException(e, "trying to remove a project");
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.ts.rest.ContentServiceRest#getConceptsInScope(java.lang.
   * Long, java.lang.String)
   */
  @Override
  @POST
  @Path("/scope/id/{id}")
  @ApiOperation(value = "Find project scope for the project id", notes = "Gets all concpets in scope for this project.", response = ConceptList.class)
  public ConceptList findConceptsInScope(
    @ApiParam(value = "Project internal id, e.g. 2", required = true) @PathParam("id") Long id,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken) {

    Logger.getLogger(getClass()).info("RESTful call (Content): scope/id/" + id);

    try {
      authenticate(securityService, authToken, "get project scope",
          UserRole.VIEWER);

      ProjectService projectService = new ProjectServiceJpa();
      @SuppressWarnings("unused")
      ConceptList list =
          projectService
              .findConceptsInScope(projectService.getProject(id), pfs);
      // Need to detach the concepts.
      // TODO
//      ConceptList list2 = new ConceptListJpa();
//      for (Concept c : list.getObjects()) {
//        list2.addObject(new ConceptJpa(c, false, false));
//      }

//      list2.setTotalCount(list.getTotalCount());
//      projectService.close();
//      return list2;
      return null;
    } catch (Exception e) {
      handleException(e, "trying to retrieve scope concepts for project " + id);
      return null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.rest.ContentServiceRest#getProject(java.lang.Long,
   * java.lang.String)
   */
  @Override
  @GET
  @Path("/id/{id}")
  @ApiOperation(value = "Get project for id", notes = "Gets the project for the specified id.", response = ConceptList.class)
  public Project getProject(
    @ApiParam(value = "Project internal id, e.g. 2", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken) {
    Logger.getLogger(getClass()).info("RESTful call (Content): /id/" + id);

    try {
      authenticate(securityService, authToken, "retrieve the project",
          UserRole.VIEWER);

      ProjectService projectService = new ProjectServiceJpa();
      Project project = projectService.getProject(id);
      projectService.close();
      return project;
    } catch (Exception e) {
      handleException(e, "trying to retrieve a project");
      return null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.ts.rest.ContentServiceRest#getProjects(java.lang.String)
   */
  @Override
  @GET
  @Path("/projects")
  @ApiOperation(value = "Get all projects", notes = "Gets all projects.", response = ConceptList.class)
  public ProjectList getProjects(
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken) {
    Logger.getLogger(getClass()).info("RESTful call (Content): /projects");

    try {
      authenticate(securityService, authToken, "retrieve projects",
          UserRole.VIEWER);

      ProjectService projectService = new ProjectServiceJpa();
      ProjectList projects = projectService.getProjects();
      for (Project project : projects.getObjects()) {
        project.getScopeConcepts().size();
        project.getScopeExcludesConcepts().size();
        project.getActionWorkflowStatusValues().size();
      }
      projectService.close();
      return projects;
    } catch (Exception e) {
      handleException(e, "trying to retrieve the projects");
      return null;
    }
  }

}
