/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.rest.impl;

import java.util.Date;

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
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.actions.ChangeEventJpa;
import com.wci.umls.server.jpa.actions.MolecularActionJpa;
import com.wci.umls.server.jpa.content.AttributeJpa;
import com.wci.umls.server.jpa.content.SemanticTypeComponentJpa;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.jpa.services.SecurityServiceJpa;
import com.wci.umls.server.jpa.services.rest.ContentServiceRest;
import com.wci.umls.server.jpa.services.rest.MetaEditingServiceRest;
import com.wci.umls.server.model.actions.ChangeEvent;
import com.wci.umls.server.model.actions.MolecularAction;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.SemanticTypeComponent;
import com.wci.umls.server.model.meta.IdType;
import com.wci.umls.server.model.workflow.WorkflowStatus;
import com.wci.umls.server.services.ContentService;
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
  @ApiOperation(value = "Add semantic type to concept", notes = "Add semantic type to concept on a project branch", response = ValidationResultJpa.class)
  public ValidationResult addSemanticType(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Concept id, e.g. 2", required = true) @QueryParam("conceptId") Long conceptId,
    @ApiParam(value = "Concept lastModified, as date", required = true) @QueryParam("lastModified") Long lastModified,
    @ApiParam(value = "Semantic type to add", required = true) SemanticTypeComponentJpa semanticType,
    @ApiParam(value = "Override warnings", required = false) @QueryParam("overrideWarnings") boolean overrideWarnings,
    @ApiParam(value = "Authorization token, e.g. 'author'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful POST call (MetaEditing): /sty/" + projectId + "/"
            + conceptId + "/add for user " + authToken + " with sty value "
            + semanticType.getSemanticType());

    // Prep reusable variables
    final String action = "ADD_SEMANTIC_TYPE";
    final ValidationResult validationResult = new ValidationResultJpa();

    // Instantiate services
    final ContentService contentService = new ContentServiceJpa();

    try {

      // Authorize project role, get userName
      final String userName = authorizeProject(contentService, projectId,
          securityService, authToken, action, UserRole.AUTHOR);

      // Retrieve the project
      final Project project = contentService.getProject(projectId);

      // Do some standard intialization and precondition checking
      // action and prep services
      final Concept concept = initialize(contentService, project, conceptId,
          userName, action, lastModified, validationResult);

      //
      // Check prerequisites
      //

      // Perform action specific validation - n/a

      // Metadata referential integrity checking
      if (contentService.getSemanticType(semanticType.getSemanticType(),
          concept.getTerminology(), concept.getVersion()) == null) {
        throw new LocalException("Cannot add invalid semantic type - "
            + semanticType.getSemanticType());
      }

      // Duplicate check
      for (SemanticTypeComponent s : concept.getSemanticTypes()) {
        if (s.getSemanticType().equals(semanticType.getSemanticType())) {
          throw new LocalException(
              "Duplicate semantic type - " + semanticType.getSemanticType());
        }
      }

      // if prerequisites fail, return validation result
      if (!validationResult.getErrors().isEmpty()
          || (!validationResult.getWarnings().isEmpty() && !overrideWarnings)) {
        // rollback -- unlocks the concept and closes transaction
        contentService.rollback();
        return validationResult;
      }

      //
      // Perform the action (contentService will create atomic actions for CRUD
      // operations)
      //

      // add the semantic type component itself and set the last modified
      semanticType.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
      SemanticTypeComponentJpa newSemanticType =
          (SemanticTypeComponentJpa) contentService
              .addSemanticTypeComponent(semanticType, concept);

      // add the semantic type and set the last modified by
      concept.getSemanticTypes().add(newSemanticType);
      concept.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);

      // update the concept
      contentService.updateConcept(concept);

      // log the REST call
      contentService.addLogEntry(userName, projectId, conceptId,
          "Add semantic type " + newSemanticType.getSemanticType()
              + " to concept " + concept.getTerminologyId());

      // commit (also removes the lock)
      contentService.commit();

      // Websocket notification
      final ChangeEvent<SemanticTypeComponentJpa> event =
          new ChangeEventJpa<SemanticTypeComponentJpa>(action,
              IdType.SEMANTIC_TYPE.toString(), null, newSemanticType);
      sendChangeEvent(event);

      return validationResult;

    } catch (Exception e) {

      handleException(e, action);
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }

  }

  @Override
  @POST
  @Path("/sty/remove/{id}")
  @ApiOperation(value = "Remove semantic type from concept", notes = "Remove semantic type from concept on a project branch", response = ValidationResultJpa.class)
  public ValidationResult removeSemanticType(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Concept id, e.g. 2", required = true) @QueryParam("conceptId") Long conceptId,
    @ApiParam(value = "Concept lastModified, in ms ", required = true) @QueryParam("lastModified") Long lastModified,
    @ApiParam(value = "Semantic type id, e.g. 3", required = true) @PathParam("id") Long semanticTypeComponentId,
    @ApiParam(value = "Override warnings", required = false) @QueryParam("overrideWarnings") boolean overrideWarnings,
    @ApiParam(value = "Authorization token, e.g. 'author'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful POST call (MetaEditing): /sty/" + projectId + "/"
            + conceptId + "/remove for user " + authToken + " with id "
            + semanticTypeComponentId);

    // Prep reusable variables
    final String action = "REMOVE_SEMANTIC_TYPE";
    final ValidationResult validationResult = new ValidationResultJpa();

    // Instantiate services
    final ContentService contentService = new ContentServiceJpa();

    try {

      // Authorize project role, get userName
      final String userName = authorizeProject(contentService, projectId,
          securityService, authToken, action, UserRole.AUTHOR);

      // Retrieve the project
      final Project project = contentService.getProject(projectId);

      // Do some standard intialization and precondition checking
      // action and prep services
      final Concept concept = initialize(contentService, project, conceptId,
          userName, action, lastModified, validationResult);

      //
      // Check prerequisites
      //

      // Perform action specific validation - n/a

      // Exists check
      SemanticTypeComponent semanticTypeComponent = null;
      for (final SemanticTypeComponent sty : concept.getSemanticTypes()) {
        if (sty.getId().equals(semanticTypeComponentId)) {
          semanticTypeComponent = sty;
        }
      }
      if (semanticTypeComponent == null) {
        throw new LocalException("Semantic type to remove does not exist");
      }

      // if prerequisites fail, return validation result
      if (!validationResult.getErrors().isEmpty()
          || (!validationResult.getWarnings().isEmpty() && !overrideWarnings)) {
        // rollback -- unlocks the concept and closes transaction
        contentService.rollback();
        return validationResult;
      }

      //
      // Perform the action
      //

      // remove the semantic type component from the concept and update
      concept.getSemanticTypes().remove(semanticTypeComponent);
      concept.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
      contentService.updateConcept(concept);

      // remove the semantic type component
      contentService.removeSemanticTypeComponent(semanticTypeComponent.getId());

      // log the REST call
      contentService.addLogEntry(userName, projectId, conceptId,
          "Remove semantic type " + semanticTypeComponent.getSemanticType()
              + " from concept " + concept.getTerminologyId());

      // commit (also adds the molecular action and removes the lock)
      contentService.commit();

      // Websocket notification
      final ChangeEvent<SemanticTypeComponentJpa> event =
          new ChangeEventJpa<SemanticTypeComponentJpa>(action,
              IdType.SEMANTIC_TYPE.toString(),
              (SemanticTypeComponentJpa) semanticTypeComponent, null);
      sendChangeEvent(event);

      return validationResult;

    } catch (Exception e) {
      handleException(e, action);
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @POST
  @Path("/attribute/add")
  @ApiOperation(value = "Add attribute to concept", notes = "Add attribute to concept on a project branch", response = ValidationResultJpa.class)
  public ValidationResult addAttribute(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Concept id, e.g. 2", required = true) @QueryParam("conceptId") Long conceptId,
    @ApiParam(value = "Concept lastModified, as date", required = true) @QueryParam("lastModified") Long lastModified,
    @ApiParam(value = "Attribute to add", required = true) AttributeJpa attribute,
    @ApiParam(value = "Override warnings", required = false) @QueryParam("overrideWarnings") boolean overrideWarnings,
    @ApiParam(value = "Authorization token, e.g. 'author'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful POST call (MetaEditing): /attribute/" + projectId + "/"
            + conceptId + "/add for user " + authToken
            + " with attribute value " + attribute.getName());

    // Prep reusable variables
    final String action = "ADD_ATTRIBUTE";
    final ValidationResult validationResult = new ValidationResultJpa();

    // Instantiate services
    final ContentService contentService = new ContentServiceJpa();

    try {

      // Authorize project role, get userName
      final String userName = authorizeProject(contentService, projectId,
          securityService, authToken, action, UserRole.AUTHOR);

      // Retrieve the project
      final Project project = contentService.getProject(projectId);

      // Do some standard intialization and precondition checking
      // action and prep services
      final Concept concept = initialize(contentService, project, conceptId,
          userName, action, lastModified, validationResult);

      //
      // Check prerequisites
      //

      // Perform action specific validation - n/a

      // Metadata referential integrity checking
      //TODO - get ID from handler
      if (contentService.getAttribute(attribute.getId()) == null) {
        throw new LocalException(
            "Cannot add invalid attribute - " + attribute.getName());
      }

      // Duplicate check
      for (Attribute a : concept.getAttributes()) {
        if (a.getName().equals(attribute.getName())) {
          throw new LocalException(
              "Duplicate attribute - " + attribute.getName());
        }
      }

      // if prerequisites fail, return validation result
      if (!validationResult.getErrors().isEmpty()
          || (!validationResult.getWarnings().isEmpty() && !overrideWarnings)) {
        // rollback -- unlocks the concept and closes transaction
        contentService.rollback();
        return validationResult;
      }

      //
      // Perform the action (contentService will create atomic actions for CRUD
      // operations)
      //

      // set the attribute component last modified
      AttributeJpa newAttribute =
          (AttributeJpa) contentService.addAttribute(attribute, concept);

      // add the semantic type and set the last modified by
      concept.getAttributes().add(newAttribute);
      concept.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);

      // update the concept
      contentService.updateConcept(concept);

      // log the REST call
      contentService.addLogEntry(userName, projectId, conceptId,
          "Add attribute " + newAttribute.getName() + " to concept "
              + concept.getTerminologyId());

      // commit (also removes the lock)
      contentService.commit();

      // Websocket notification
      final ChangeEvent<AttributeJpa> event = new ChangeEventJpa<AttributeJpa>(
          action, IdType.ATTRIBUTE.toString(), null, newAttribute);
      getNotificationWebsocket().send(ConfigUtility.getJsonForGraph(event));

      return validationResult;

    } catch (Exception e) {

      handleException(e, action);
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }

  }

  @Override
  @POST
  @Path("/attribute/remove/{id}")
  @ApiOperation(value = "Remove attribute from concept", notes = "Remove attribute from concept on a project branch", response = ValidationResultJpa.class)
  public ValidationResult removeAttribute(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Concept id, e.g. 2", required = true) @QueryParam("conceptId") Long conceptId,
    @ApiParam(value = "Concept lastModified, in ms ", required = true) @QueryParam("lastModified") Long lastModified,
    @ApiParam(value = "Attribute id, e.g. 3", required = true) @PathParam("id") Long attributeId,
    @ApiParam(value = "Override warnings", required = false) @QueryParam("overrideWarnings") boolean overrideWarnings,
    @ApiParam(value = "Authorization token, e.g. 'author'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful POST call (MetaEditing): /attribute/" + projectId + "/"
            + conceptId + "/remove for user " + authToken + " with id "
            + attributeId);

    // Prep reusable variables
    final String action = "REMOVE_ATTRIBUTE";
    final ValidationResult validationResult = new ValidationResultJpa();

    // Instantiate services
    final ContentService contentService = new ContentServiceJpa();
    
    try {

      // Authorize project role, get userName
      final String userName = authorizeProject(contentService, projectId,
          securityService, authToken, action, UserRole.AUTHOR);

      // Retrieve the project
      final Project project = contentService.getProject(projectId);

      // Do some standard intialization and precondition checking
      // action and prep services
      final Concept concept = initialize(contentService, project, conceptId,
          userName, action, lastModified, validationResult);

      //
      // Check prerequisites
      //

      // Perform action specific validation - n/a

      // Exists check
      Attribute attribute = null;
      for (final Attribute atr : concept.getAttributes()) {
        if (atr.getId().equals(attributeId)) {
          attribute = atr;
        }
      }
      if (attribute == null) {
        throw new LocalException("Attribute to remove does not exist");
      }

      // if prerequisites fail, return validation result
      if (!validationResult.getErrors().isEmpty()
          || (!validationResult.getWarnings().isEmpty() && !overrideWarnings)) {
        // rollback -- unlocks the concept and closes transaction
        contentService.rollback();
        return validationResult;
      }

      //
      // Perform the action
      //

      // remove the semantic type component from the concept and update
      concept.getAttributes().remove(attribute);
      concept.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
      contentService.updateConcept(concept);

      // remove the semantic type component
      contentService.removeAttribute(attribute.getId());

      // log the REST call
      contentService.addLogEntry(userName, projectId, conceptId,
          "Remove semantic type " + attribute.getName()
              + " from concept " + concept.getTerminologyId());

      // commit (also adds the molecular action and removes the lock)
      contentService.commit();

      // Websocket notification
      final ChangeEvent<AttributeJpa> event =
          new ChangeEventJpa<AttributeJpa>(action,
              IdType.ATTRIBUTE.toString(),
              (AttributeJpa) attribute, null);
      getNotificationWebsocket().send(ConfigUtility.getJsonForGraph(event));

      return validationResult;

    } catch (Exception e) {
      handleException(e, action);
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }
  }

  /**
   * Helper function to:
   * 
   * <pre>
   * (1) Set transaction mode and begin transaction
   * (1) retrieve and lock concept, 
   * (2) prepare molecular action 
   * (3) configure the service
   * (5) validate project/concept
   * (6) Check dirty flag (concept lastModifiedBy)
   * </pre>
   * 
   * .
   *
   * @param contentService the content service
   * @param project the project
   * @param conceptId the concept id
   * @param userName the user name
   * @param actionType the action type
   * @param lastModified the last modified
   * @param result the result
   * @return the concept
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  private Concept initialize(ContentService contentService, Project project,
    Long conceptId, String userName, String actionType, long lastModified,
    ValidationResult result) throws Exception {

    // Set transaction mode and start transaction
    contentService.setTransactionPerOperation(false);
    contentService.beginTransaction();

    Concept concept;
    synchronized (conceptId.toString().intern()) {

      // retrieve the concept
      concept = contentService.getConcept(conceptId);

      // Verify concept exists
      if (concept == null) {
        throw new Exception("Concept does not exist " + conceptId);
      }

      // Fail if already locked - this is secondary protection
      if (contentService.isObjectLocked(concept)) {
        throw new Exception("Fatal error: concept is locked");
      }

      // lock the concept via JPA
      contentService.lockObject(concept);

    }

    // construct the molecular action
    final MolecularAction molecularAction = new MolecularActionJpa();
    molecularAction.setTerminology(concept.getTerminology());
    molecularAction.setTerminologyId(concept.getTerminologyId());
    molecularAction.setVersion(concept.getVersion());
    molecularAction.setName(actionType);
    molecularAction.setTimestamp(new Date());

    // Prepare the service
    contentService.setMolecularActionFlag(true);
    contentService.setLastModifiedFlag(true);
    contentService.setLastModifiedBy(userName);

    // Add the molecular action and pass to the service.
    // It needs to be added now so that when atomic actions
    // are created by the service, this object already has
    // an identifier.
    final MolecularAction newMolecularAction =
        contentService.addMolecularAction(molecularAction);
    contentService.setMolecularAction(newMolecularAction);

    // throw exception on terminology mismatch
    if (!concept.getTerminology().equals(project.getTerminology())) {
      throw new Exception("Project and concept terminologies do not match");
    }

    if (concept.getLastModified().getTime() != lastModified) {
      throw new LocalException(
          "Concept has changed since last read, please refresh and try again");
    }

    // Return concept
    return concept;
  }
}
