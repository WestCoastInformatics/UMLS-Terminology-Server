/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.rest.impl;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import com.wci.umls.server.Project;
import com.wci.umls.server.UserRole;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.content.SemanticTypeComponentJpa;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.jpa.services.ProjectServiceJpa;
import com.wci.umls.server.jpa.services.SecurityServiceJpa;
import com.wci.umls.server.jpa.services.rest.ContentServiceRest;
import com.wci.umls.server.jpa.services.rest.MetaEditingServiceRest;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.SemanticTypeComponent;
import com.wci.umls.server.services.ContentService;
import com.wci.umls.server.services.ProjectService;
import com.wci.umls.server.services.SecurityService;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * REST implementation for {@link ContentServiceRest}..
 */
@Path("/meta")
@Consumes({
    MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
})
@Produces({
    MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
})
@Api(value = "/meta", description = "Operations for metathesaurus editing")
public class MetaEditingServiceRestImpl extends RootServiceRestImpl
    implements MetaEditingServiceRest {

  /** The security service. */
  private SecurityService securityService;

  /** The lock object. */
  private final static String lockObject = "LOCK";

  /**
   * Instantiates an empty {@link MetaEditingServiceRestImpl}.
   *
   * @throws Exception the exception
   */
  public MetaEditingServiceRestImpl() throws Exception {
    securityService = new SecurityServiceJpa();
  }

  /* see superclass */
  @Override
  @POST
  @Path("/sty/add")
  @ApiOperation(value = "Add semantic type to concept", notes = "Add semantic type to concept on a project branch")
  public ValidationResult addSemanticType(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Concept id, e.g. 2", required = true) @QueryParam("conceptId") Long conceptId,
    @ApiParam(value = "Semantic type to add", required = true) SemanticTypeComponentJpa semanticTypeComponent,
    @ApiParam(value = "Authorization token, e.g. 'author'", required = true) @HeaderParam("Authorization") String authToken)
      throws Exception {
    {

      Logger.getLogger(getClass())
          .info("RESTful POST call (MetaEditing): /sty/" + projectId + "/"
              + conceptId + "/add for user " + authToken + " with sty value "
              + semanticTypeComponent.getSemanticType());

      String action = "trying to add semantic type to concept";

      ValidationResult validationResult = new ValidationResultJpa();

      ContentService contentService = new ContentServiceJpa();
      ProjectService projectService = new ProjectServiceJpa();

      try {
        authorizeProject(projectService, projectId, securityService, authToken,
            action, UserRole.AUTHOR);

        // retrieve the project
        Project project = projectService.getProject(projectId);

        // retrieve the concept
        Concept concept = contentService.getConcept(conceptId);
        
        //
        // Check prerequisites
        //
        checkPrerequisitesForProjectAndConcept(project, concept,
            validationResult);

        if (concept.getSemanticTypes().contains(semanticTypeComponent)) {
          validationResult.getErrors()
              .add("Concept already contains semantic type");
        }
        
        // if prerequisites fail, return validation result
        if (!validationResult.getErrors().isEmpty()) {
          return validationResult;
        }

        //
        // Perform the synchronized action
        //
        synchronized (lockObject) {

          // prepare the lock
          contentService.setTransactionPerOperation(false);
          contentService.beginTransaction();

          // re-retrieve the concept
          Concept lockedConcept = contentService.getConcept(conceptId);

          // verify that the last modified matches
          if (!lockedConcept.getLastModified()
              .equals(concept.getLastModified())) {
            validationResult.getErrors()
                .add("Stale concept, aborted operation");
          } else {

            // add the semantic type and set the last modified by
            concept.getSemanticTypes().add(semanticTypeComponent);
            concept.setLastModifiedBy(authToken);

            // update the concept
            contentService.updateConcept(concept);

            // validate the concept
            validationResult
                .merge(projectService.validateConcept(project, concept));
          }

          // rollback or commit based on errors
          if (!validationResult.getErrors().isEmpty()) {
            contentService.rollback();
          } else {
            contentService.commit();
            
            // TODO Add Action and LogEntry objects here once NE-25 complete
          }
          return validationResult;
        }

      } catch (Exception e) {
        handleException(e, action);
        return null;
      } finally {
        contentService.close();
        projectService.close();
        securityService.close();
      }
    }
  }

  @Override
  @POST
  @Path("/sty/remove/{id}")
  @ApiOperation(value = "Remove semantic type from concept", notes = "Remove semantic type from concept on a project branch")
  public ValidationResult removeSemanticType(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Concept id, e.g. 2", required = true) @QueryParam("conceptId") Long conceptId,
    @ApiParam(value = "Semantic type id, e.g. 3", required = true) @PathParam("id") Long semanticTypeComponentId,
    @ApiParam(value = "Authorization token, e.g. 'author'", required = true) @HeaderParam("Authorization") String authToken)
      throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful POST call (MetaEditing): /sty/" + projectId + "/"
            + conceptId + "/remove for user " + authToken + " with id "
            + semanticTypeComponentId);

    String action = "trying to remove semantic type from concept";

    ValidationResult validationResult = new ValidationResultJpa();

    ContentService contentService = new ContentServiceJpa();
    ProjectService projectService = new ProjectServiceJpa();

    try {
      authorizeProject(projectService, projectId, securityService, authToken,
          action, UserRole.AUTHOR);

      // retrieve the project
      Project project = projectService.getProject(projectId);

      // retrieve the concept
      Concept concept = contentService.getConcept(conceptId);

      //
      // Check prerequisites
      //
      checkPrerequisitesForProjectAndConcept(project, concept,
          validationResult);

      SemanticTypeComponent semanticTypeComponent = null;
      for (final SemanticTypeComponent sty : concept.getSemanticTypes()) {
        if (sty.getId().equals(semanticTypeComponentId)) {
          semanticTypeComponent = sty;
        }
      }
      if (semanticTypeComponent == null) {
        validationResult.getErrors().add(
            "Semantic type could not be removed from concept, not present");
      }
      
      // if prerequisites fail, return validation result
      if (!validationResult.getErrors().isEmpty()) {
        return validationResult;
      }
  
      //
      // Perform the synchronized action
      //
      synchronized (lockObject) {

        // prepare the lock
        contentService.setTransactionPerOperation(false);
        contentService.beginTransaction();

        // re-retrieve the concept
        Concept lockedConcept = contentService.getConcept(conceptId);

        // verify that the last modified matches
        if (!lockedConcept.getLastModified()
            .equals(concept.getLastModified())) {
          validationResult.getErrors()
              .add("Stale concept, aborted operation");
        } else {

          // add the semantic type and set the last modified by
          concept.getSemanticTypes().remove(semanticTypeComponent);
          concept.setLastModifiedBy(authToken);

          // update the concept
          contentService.updateConcept(concept);

          // validate the concept
          validationResult
              .merge(projectService.validateConcept(project, concept));
        }

        // rollback or commit based on errors
        if (!validationResult.getErrors().isEmpty()) {
          contentService.rollback();
        } else {
          contentService.commit();
          
          // TODO Add Action and LogEntry objects here once NE-25 complete
        }
        return validationResult;
      }

    } catch (Exception e) {
      handleException(e, action);
      return null;
    } finally {
      contentService.close();
      projectService.close();
      securityService.close();
    }
  }

  /**
   * Validate project and concept.
   *
   * @param project the project
   * @param concept the concept
   * @param validationResult the validation result
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  private void checkPrerequisitesForProjectAndConcept(Project project,
    Concept concept, ValidationResult validationResult) throws Exception {

    // throw exception on null retrieval
    if (project == null) {
      validationResult.getErrors().add("Invalid project id");
    }
    if (concept == null) {
      validationResult.getErrors().add("Invalid project id");
    }

    // throw exception on terminology mismatch
    if (!concept.getTerminology().equals(project.getTerminology())) {
      validationResult.getErrors()
          .add("Project and concept terminologies do not match");
    }

    // throw exception on branch mismatch
    if (!concept.getBranch().equals(project.getBranch())) {
      validationResult.getErrors()
          .add("Project and concept branches do not match");
    }
  }
}
