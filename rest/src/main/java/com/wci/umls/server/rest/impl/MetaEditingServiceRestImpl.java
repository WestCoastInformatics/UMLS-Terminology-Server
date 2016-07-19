/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.rest.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.helpers.content.RelationshipList;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.actions.ChangeEventJpa;
import com.wci.umls.server.jpa.actions.MolecularActionJpa;
import com.wci.umls.server.jpa.algo.action.AddAtomMolecularAction;
import com.wci.umls.server.jpa.algo.action.AddAttributeMolecularAction;
import com.wci.umls.server.jpa.algo.action.AddSemanticTypeMolecularAction;
import com.wci.umls.server.jpa.algo.action.RemoveAtomMolecularAction;
import com.wci.umls.server.jpa.algo.action.RemoveAttributeMolecularAction;
import com.wci.umls.server.jpa.algo.action.RemoveSemanticTypeMolecularAction;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.AttributeJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.ConceptRelationshipJpa;
import com.wci.umls.server.jpa.content.SemanticTypeComponentJpa;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.jpa.services.SecurityServiceJpa;
import com.wci.umls.server.jpa.services.rest.MetaEditingServiceRest;
import com.wci.umls.server.model.actions.ChangeEvent;
import com.wci.umls.server.model.actions.MolecularAction;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.model.content.SemanticTypeComponent;
import com.wci.umls.server.model.meta.IdType;
import com.wci.umls.server.model.workflow.WorkflowStatus;
import com.wci.umls.server.services.ContentService;
import com.wci.umls.server.services.SecurityService;
import com.wci.umls.server.services.handlers.GraphResolutionHandler;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * REST implementation for {@link MetaEditingServiceRest}..
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
        .info("RESTful POST call (MetaEditing): /sty/add " + projectId + ","
            + conceptId + " for user " + authToken + " with sty value "
            + semanticType.getSemanticType());

    // Instantiate services
    final AddSemanticTypeMolecularAction action =
        new AddSemanticTypeMolecularAction();
    try {

      // Start transaction
      action.setTransactionPerOperation(false);
      action.beginTransaction();
      action.setSemanticTypeComponent(semanticType);
      action.setChangeStatusFlag(true);

      // Authorize project role, get userName
      final String userName =
          authorizeProject(action, projectId, securityService, authToken,
              "adding a semantic type", UserRole.AUTHOR);

      // Retrieve the project
      final Project project = action.getProject(projectId);

      // Do some standard intialization and precondition checking
      // action and prep services
      action.initialize(project, conceptId, null, userName, lastModified);

      //
      // Check prerequisites
      //
      final ValidationResult validationResult = action.checkPreconditions();

      // if prerequisites fail, return validation result
      if (!validationResult.getErrors().isEmpty()
          || (!validationResult.getWarnings().isEmpty() && !overrideWarnings)) {
        // rollback -- unlocks the concept and closes transaction
        action.rollback();
        return validationResult;
      }

      //
      // Perform the action
      //
      action.compute();

      // commit (also removes the lock)
      action.commit();

      // Websocket notification
      final ChangeEvent<SemanticTypeComponent> event =
          new ChangeEventJpa<SemanticTypeComponent>(action.getName(), authToken,
              IdType.SEMANTIC_TYPE.toString(), null,
              action.getSemanticTypeComponent(), action.getConcept());
      sendChangeEvent(event);

      return validationResult;

    } catch (Exception e) {
      handleException(e, "adding a semantic type");
      return null;
    } finally {
      action.close();
      securityService.close();
    }

  }

  /* see superclass */
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

    // Instantiate services
    final RemoveSemanticTypeMolecularAction action =
        new RemoveSemanticTypeMolecularAction();
    try {

      // Start transaction
      action.setTransactionPerOperation(false);
      action.beginTransaction();
      action.setSemanticTypeComponentId(semanticTypeComponentId);
      action.setChangeStatusFlag(true);

      // Authorize project role, get userName
      final String userName =
          authorizeProject(action, projectId, securityService, authToken,
              "removing a semantic type", UserRole.AUTHOR);

      // Retrieve the project
      final Project project = action.getProject(projectId);

      // Do some standard intialization and precondition checking
      // action and prep services
      action.initialize(project, conceptId, null, userName, lastModified);

      //
      // Check prerequisites
      //
      final ValidationResult validationResult = action.checkPreconditions();

      // if prerequisites fail, return validation result
      if (!validationResult.getErrors().isEmpty()
          || (!validationResult.getWarnings().isEmpty() && !overrideWarnings)) {
        // rollback -- unlocks the concept and closes transaction
        action.rollback();
        return validationResult;
      }

      //
      // Perform the action
      //
      action.compute();

      // commit (also removes the lock)
      action.commit();

      // Websocket notification
      final ChangeEvent<SemanticTypeComponent> event =
          new ChangeEventJpa<SemanticTypeComponent>(action.getName(), authToken,
              IdType.SEMANTIC_TYPE.toString(),
              action.getSemanticTypeComponent(), null, action.getConcept());
      sendChangeEvent(event);

      return validationResult;

    } catch (Exception e) {
      handleException(e, "removing a semantic type");
      return null;
    } finally {
      action.close();
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

    // Instantiate services
    final AddAttributeMolecularAction action =
        new AddAttributeMolecularAction();
    try {

      // Start transaction
      action.setTransactionPerOperation(false);
      action.beginTransaction();
      action.setAttribute(attribute);
      action.setChangeStatusFlag(true);

      // Authorize project role, get userName
      final String userName = authorizeProject(action, projectId,
          securityService, authToken, "adding an attribute", UserRole.AUTHOR);

      // Retrieve the project
      final Project project = action.getProject(projectId);

      // Do some standard intialization and precondition checking
      // action and prep services
      action.initialize(project, conceptId, null, userName, lastModified);

      //
      // Check prerequisites
      //
      final ValidationResult validationResult = action.checkPreconditions();

      // if prerequisites fail, return validation result
      if (!validationResult.getErrors().isEmpty()
          || (!validationResult.getWarnings().isEmpty() && !overrideWarnings)) {
        // rollback -- unlocks the concept and closes transaction
        action.rollback();
        return validationResult;
      }

      // if prerequisites fail, return validation result
      if (!validationResult.getErrors().isEmpty()
          || (!validationResult.getWarnings().isEmpty() && !overrideWarnings)) {
        // rollback -- unlocks the concept and closes transaction
        action.rollback();
        return validationResult;
      }

      //
      // Perform the action
      //
      action.compute();

      // commit (also removes the lock)
      action.commit();

      // Websocket notification
      final ChangeEvent<Attribute> event = new ChangeEventJpa<Attribute>(
          action.getName(), authToken, IdType.ATTRIBUTE.toString(), null,
          action.getAttribute(), action.getConcept());
      sendChangeEvent(event);

      return validationResult;

    } catch (Exception e) {

      handleException(e, "adding an attribute");
      return null;
    } finally {
      action.close();
      securityService.close();
    }

  }

  /* see superclass */
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

    // Instantiate services
    final RemoveAttributeMolecularAction action =
        new RemoveAttributeMolecularAction();
    try {

      // Start transaction
      action.setTransactionPerOperation(false);
      action.beginTransaction();
      action.setAttributeId(attributeId);
      action.setChangeStatusFlag(true);

      // Authorize project role, get userName
      final String userName = authorizeProject(action, projectId,
          securityService, authToken, "removing an attribute", UserRole.AUTHOR);

      // Retrieve the project
      final Project project = action.getProject(projectId);

      // Do some standard intialization and precondition checking
      // action and prep services
      action.initialize(project, conceptId, null, userName, lastModified);

      //
      // Check prerequisites
      //
      final ValidationResult validationResult = action.checkPreconditions();

      // if prerequisites fail, return validation result
      if (!validationResult.getErrors().isEmpty()
          || (!validationResult.getWarnings().isEmpty() && !overrideWarnings)) {
        // rollback -- unlocks the concept and closes transaction
        action.rollback();
        return validationResult;
      }

      //
      // Perform the action
      //
      action.compute();

      // commit (also removes the lock)
      action.commit();

      // Websocket notification
      final ChangeEvent<Attribute> event = new ChangeEventJpa<Attribute>(
          action.getName(), authToken, IdType.ATTRIBUTE.toString(),
          action.getAttribute(), null, action.getConcept());
      sendChangeEvent(event);

      return validationResult;

    } catch (Exception e) {
      handleException(e, "removing an attribute");
      return null;
    } finally {
      action.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @POST
  @Path("/atom/add")
  @ApiOperation(value = "Add atom to concept", notes = "Add atom to concept on a project branch", response = ValidationResultJpa.class)
  public ValidationResult addAtom(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Concept id, e.g. 2", required = true) @QueryParam("conceptId") Long conceptId,
    @ApiParam(value = "Concept lastModified, as date", required = true) @QueryParam("lastModified") Long lastModified,
    @ApiParam(value = "Atom to add", required = true) AtomJpa atom,
    @ApiParam(value = "Override warnings", required = false) @QueryParam("overrideWarnings") boolean overrideWarnings,
    @ApiParam(value = "Authorization token, e.g. 'author'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful POST call (MetaEditing): /atom/" + projectId + "/"
            + conceptId + "/add for user " + authToken + " with atom value "
            + atom.getName());

    // Instantiate services
    final AddAtomMolecularAction action = new AddAtomMolecularAction();
    try {

      // Start transaction
      action.setTransactionPerOperation(false);
      action.beginTransaction();
      action.setAtom(atom);
      action.setChangeStatusFlag(true);

      // Authorize project role, get userName
      final String userName = authorizeProject(action, projectId,
          securityService, authToken, "adding an atom", UserRole.AUTHOR);

      // Retrieve the project
      final Project project = action.getProject(projectId);

      // Do some standard intialization and precondition checking
      // action and prep services
      action.initialize(project, conceptId, null, userName, lastModified);

      //
      // Check prerequisites
      //
      final ValidationResult validationResult = action.checkPreconditions();

      // if prerequisites fail, return validation result
      if (!validationResult.getErrors().isEmpty()
          || (!validationResult.getWarnings().isEmpty() && !overrideWarnings)) {
        // rollback -- unlocks the concept and closes transaction
        action.rollback();
        return validationResult;
      }

      //
      // Perform the action
      //
      action.compute();

      // commit (also removes the lock)
      action.commit();

      // Websocket notification
      final ChangeEvent<Atom> event = new ChangeEventJpa<Atom>("adding an atom",
          authToken, IdType.ATOM.toString(), null, action.getAtom(),
          action.getConcept());
      sendChangeEvent(event);

      return validationResult;

    } catch (Exception e) {

      handleException(e, "adding an atom");
      return null;
    } finally {
      action.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @POST
  @Path("/atom/remove/{id}")
  @ApiOperation(value = "Remove atom from concept", notes = "Remove atom from concept on a project branch", response = ValidationResultJpa.class)
  public ValidationResult removeAtom(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Concept id, e.g. 2", required = true) @QueryParam("conceptId") Long conceptId,
    @ApiParam(value = "Concept lastModified, in ms ", required = true) @QueryParam("lastModified") Long lastModified,
    @ApiParam(value = "Atom id, e.g. 3", required = true) @PathParam("id") Long atomId,
    @ApiParam(value = "Override warnings", required = false) @QueryParam("overrideWarnings") boolean overrideWarnings,
    @ApiParam(value = "Authorization token, e.g. 'author'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful POST call (MetaEditing): /atom/" + projectId + "/"
            + conceptId + "/remove for user " + authToken + " with id "
            + atomId);

    // Instantiate services
    final RemoveAtomMolecularAction action = new RemoveAtomMolecularAction();
    try {

      // Start transaction
      action.setTransactionPerOperation(false);
      action.beginTransaction();
      action.setAtomId(atomId);
      action.setChangeStatusFlag(true);

      // Authorize project role, get userName
      final String userName = authorizeProject(action, projectId,
          securityService, authToken, "removing an atom", UserRole.AUTHOR);

      // Retrieve the project
      final Project project = action.getProject(projectId);

      // Do some standard intialization and precondition checking
      // action and prep services
      action.initialize(project, conceptId, null, userName, lastModified);

      //
      // Check prerequisites
      //
      final ValidationResult validationResult = action.checkPreconditions();

      // if prerequisites fail, return validation result
      if (!validationResult.getErrors().isEmpty()
          || (!validationResult.getWarnings().isEmpty() && !overrideWarnings)) {
        // rollback -- unlocks the concept and closes transaction
        action.rollback();
        return validationResult;
      }

      //
      // Perform the action
      //
      action.compute();

      // commit (also removes the lock)
      action.commit();

      // Websocket notification
      final ChangeEvent<Atom> event = new ChangeEventJpa<Atom>(action.getName(),
          authToken, IdType.ATTRIBUTE.toString(), action.getAtom(), null,
          action.getConcept());
      sendChangeEvent(event);

      return validationResult;

    } catch (Exception e) {
      handleException(e, "removing an atom");
      return null;
    } finally {
      action.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @POST
  @Path("/relationship/add")
  @ApiOperation(value = "Add relationship to concept", notes = "Add relationship to concept on a project branch", response = ValidationResultJpa.class)
  public ValidationResult addRelationship(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Concept id, e.g. 2", required = true) @QueryParam("conceptId") Long conceptId,
    @ApiParam(value = "Concept lastModified, as date", required = true) @QueryParam("lastModified") Long lastModified,
    @ApiParam(value = "Relationship to add", required = true) ConceptRelationshipJpa relationship,
    @ApiParam(value = "Override warnings", required = false) @QueryParam("overrideWarnings") boolean overrideWarnings,
    @ApiParam(value = "Authorization token, e.g. 'author'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful POST call (MetaEditing): /relationship/" + projectId
            + "/" + conceptId + "/add for user " + authToken
            + " with relationship value " + relationship);

    // Prep reusable variables
    final String action = "ADD_RELATIONSHIP";
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
      final List<Concept> conceptList = initialize(contentService, project,
          conceptId, relationship.getTo().getId(), userName, action,
          lastModified, validationResult);

      Concept concept = conceptList.get(0);
      Concept toConcept = conceptList.get(1);

      //
      // Check prerequisites
      //

      // Perform action specific validation - n/a

      // Metadata referential integrity checking
      if (contentService.getRelationshipType(relationship.getRelationshipType(),
          relationship.getTerminology(), relationship.getVersion()) == null) {
        contentService.rollback();
        throw new LocalException(
            "Cannot add relationship with invalid relationship type - "
                + relationship.getRelationshipType());
      }
      if (contentService.getAdditionalRelationshipType(
          relationship.getAdditionalRelationshipType(),
          relationship.getTerminology(), relationship.getVersion()) == null) {
        contentService.rollback();
        throw new LocalException(
            "Cannot add relationship with invalid additional relationship type - "
                + relationship.getAdditionalRelationshipType());
      }
      if (contentService.getTerminology(relationship.getTerminology(),
          relationship.getVersion()) == null) {
        contentService.rollback();
        throw new LocalException(
            "Cannot add relationship with invalid terminology - "
                + relationship.getTerminology() + ", version: "
                + relationship.getVersion());
      }

      // Duplicate check
      for (final ConceptRelationship a : concept.getRelationships()) {
        if (a.equals(relationship)) {
          contentService.rollback();
          throw new LocalException(
              "Duplicate relationship - " + relationship.getName());
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
      // Perform the action (contentService will create atomic actions
      // for CRUD
      // operations)
      //

      relationship.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
      // Assign alternateTerminologyId
      // final IdentifierAssignmentHandler handler = contentService
      // .getIdentifierAssignmentHandler(concept.getTerminology());
      //
      // final String altId = handler.getTerminologyId(relationship);
      // relationship.getAlternateTerminologyIds().put(concept.getTerminology(),
      // altId);

      // set the relationship component last modified
      final ConceptRelationshipJpa newRelationship =
          (ConceptRelationshipJpa) contentService.addRelationship(relationship);

      // construct inverse relationship
      final ConceptRelationshipJpa inverseRelationship =
          (ConceptRelationshipJpa) contentService
              .createInverseConceptRelationship(newRelationship);

      // pass to handler.getTerminologyId
      // final String inverseAltId =
      // handler.getTerminologyId(inverseRelationship);
      // inverseRelationship.getAlternateTerminologyIds()
      // .put(concept.getTerminology(), inverseAltId);

      // set the relationship component last modified
      final ConceptRelationshipJpa newInverseRelationship =
          (ConceptRelationshipJpa) contentService
              .addRelationship(inverseRelationship);

      // add the relationship and set the last modified by
      concept.getRelationships().add(newRelationship);
      toConcept.getRelationships().add(newInverseRelationship);
      concept.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);

      // update the concept
      contentService.updateConcept(toConcept);
      contentService.updateConcept(concept);

      // log the REST calls
      contentService.addLogEntry(userName, projectId, conceptId, action + " "
          + newRelationship + " to concept " + concept.getTerminologyId());

      // commit (also removes the lock)
      contentService.commit();

      // Websocket notification
      final ChangeEvent<ConceptRelationshipJpa> event =
          new ChangeEventJpa<ConceptRelationshipJpa>(action, authToken,
              IdType.RELATIONSHIP.toString(), null, newRelationship, concept);
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
  @SuppressWarnings("rawtypes")
  @Override
  @POST
  @Path("/relationship/remove/{id}")
  @ApiOperation(value = "Remove relationship from concept", notes = "Remove relationship from concept on a project branch", response = ValidationResultJpa.class)
  public ValidationResult removeRelationship(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Concept id, e.g. 2", required = true) @QueryParam("conceptId") Long conceptId,
    @ApiParam(value = "Concept lastModified, in ms ", required = true) @QueryParam("lastModified") Long lastModified,
    @ApiParam(value = "Relationship id, e.g. 3", required = true) @PathParam("id") Long relationshipId,
    @ApiParam(value = "Override warnings", required = false) @QueryParam("overrideWarnings") boolean overrideWarnings,
    @ApiParam(value = "Authorization token, e.g. 'author'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful POST call (MetaEditing): /relationship/" + projectId
            + "/" + conceptId + "/remove for user " + authToken + " with id "
            + relationshipId);

    // Prep reusable variables
    final String action = "REMOVE_RELATIONSHIP";
    final ValidationResult validationResult = new ValidationResultJpa();

    // Instantiate services
    final ContentService contentService = new ContentServiceJpa();

    // TODO: actually look up second conceptId somehow.
    final Long conceptId2 = contentService
        .getRelationship(relationshipId, ConceptRelationshipJpa.class).getTo()
        .getId();

    try {

      // Authorize project role, get userName
      final String userName = authorizeProject(contentService, projectId,
          securityService, authToken, action, UserRole.AUTHOR);

      // Retrieve the project
      final Project project = contentService.getProject(projectId);

      // Do some standard intialization and precondition checking
      // action and prep services
      final List<Concept> conceptList =
          initialize(contentService, project, conceptId, conceptId2, userName,
              action, lastModified, validationResult);

      Concept concept = conceptList.get(0);
      Concept toConcept = conceptList.get(1);

      //
      // Check prerequisites
      //

      // Perform action specific validation - n/a

      // Exists check
      ConceptRelationship relationship = null;
      for (final ConceptRelationship atr : concept.getRelationships()) {
        if (atr.getId().equals(relationshipId)) {
          relationship = atr;
        }
      }
      if (relationship == null) {
        throw new LocalException("Relationship to remove does not exist");
      }

      // Exists check for inverse Relationshop

      RelationshipList relList =
          contentService.findConceptRelationships(toConcept.getTerminologyId(),
              toConcept.getTerminology(), toConcept.getVersion(), Branch.ROOT,
              "fromId:" + toConcept.getId() + " AND toId:" + concept.getId(),
              false, null);

      ConceptRelationship inverseRelationship = null;
      for (final Relationship rel : relList.getObjects()) {
        if (rel.getTo().getId() == relationship.getFrom().getId()
            && rel.getFrom().getId() == relationship.getTo().getId()) {
          if (inverseRelationship != null) {
            throw new Exception(
                "Unexepected more than a single inverse relationship for relationship - "
                    + relationship);
          }

          inverseRelationship = (ConceptRelationship) rel;
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
      // Perform the action
      //

      // remove the relationship type component from the concept and update
      concept.getRelationships().remove(relationship);

      // remove the relationship component
      contentService.removeRelationship(relationship.getId(),
          relationship.getClass());

      // remove the inverse relationship type component from the concept and
      // update
      toConcept.getRelationships().remove(inverseRelationship);

      // remove the inverse relationship component
      contentService.removeRelationship(inverseRelationship.getId(),
          inverseRelationship.getClass());

      contentService.updateConcept(toConcept);

      concept.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
      contentService.updateConcept(concept);

      // log the REST call
      contentService.addLogEntry(userName, projectId, conceptId,
          action + " " + relationship);

      // commit (also adds the molecular action and removes the lock)
      contentService.commit();

      // Websocket notification
      final ChangeEvent<ConceptRelationshipJpa> event =
          new ChangeEventJpa<ConceptRelationshipJpa>(action, authToken,
              IdType.RELATIONSHIP.toString(),
              (ConceptRelationshipJpa) relationship, null, concept);
      sendChangeEvent(event);

      return validationResult;

    } catch (

    Exception e) {
      handleException(e, action);
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @SuppressWarnings("rawtypes")
  @Override
  @POST
  @Path("/concept/merge")
  @ApiOperation(value = "Merge concepts together", notes = "Merge concepts together on a project branch", response = ValidationResultJpa.class)
  public ValidationResult mergeConcepts(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Concept id, e.g. 2", required = true) @QueryParam("conceptId") Long conceptId,
    @ApiParam(value = "Concept lastModified, as date", required = true) @QueryParam("lastModified") Long lastModified,
    @ApiParam(value = "Concept id, e.g. 3", required = true) @QueryParam("conceptId2") Long conceptId2,
    @ApiParam(value = "Override warnings", required = false) @QueryParam("overrideWarnings") boolean overrideWarnings,
    @ApiParam(value = "Make demotions", required = false) @QueryParam("makeDemotions") boolean makeDemotions,
    @ApiParam(value = "Unapprove content", required = false) @QueryParam("unapproveContent") boolean unapproveContent,
    @ApiParam(value = "Authorization token, e.g. 'author'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful POST call (MetaEditing): /concept/" + projectId + "/"
            + conceptId + "/merge for user " + authToken + " with concept "
            + conceptId2);

    // Prep reusable variables
    final String action = "MERGE";
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
      final List<Concept> conceptList =
          initialize(contentService, project, conceptId, conceptId2, userName,
              action, lastModified, validationResult);

      // Assign the concept with the lowest id to survive, and the one with the
      // highest id to get destroyed.
      List<Concept> sortedConceptList = conceptList.stream()
          .sorted((c1, c2) -> Long.compare(c1.getId(), c2.getId()))
          .collect(Collectors.toList());

      Concept toConcept = sortedConceptList.get(0);
      Concept fromConcept = sortedConceptList.get(1);

      // Make copy of toConcept and fromConcept before changes, to pass into
      // change event
      Concept toConceptPreUpdates = new ConceptJpa(toConcept, false);
      Concept fromConceptPreUpdates = new ConceptJpa(fromConcept, false);

      //
      // Check prerequisites
      //

      // Perform action specific validation - n/a

      // Metadata referential integrity checking

      // Same concept check
      if (conceptId == conceptId2) {
        throw new LocalException("Cannot merge concept " + conceptId
            + " to concept " + conceptId2 + " - identical concept.");
      }

      contentService.validateMerge(project, toConcept, fromConcept);

      // if prerequisites fail, return validation result
      if (!validationResult.getErrors().isEmpty()
          || (!validationResult.getWarnings().isEmpty() && !overrideWarnings)) {
        // rollback -- unlocks the concepts and closes transaction
        contentService.rollback();
        return validationResult;
      }

      //
      // Perform the actions (contentService will create atomic actions
      // for CRUD
      // operations)
      //

      // Add each atom from fromConcept to toConcept, delete from
      // fromConcept, and set to NEEDS_REVIEW
      final List<Atom> fromAtoms = new ArrayList<>(fromConcept.getAtoms());
      contentService.moveAtoms(toConcept, fromConcept, fromAtoms);

      for (Atom atm : fromAtoms) {
        atm.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
      }

      // Add each semanticType from fromConcept to toConcept, and delete from
      // fromConcept
      // NOTE: Only add semantic type if it doesn't already exist in toConcept
      final List<SemanticTypeComponent> fromStys =
          new ArrayList<>(fromConcept.getSemanticTypes());
      final List<SemanticTypeComponent> toStys = toConcept.getSemanticTypes();

      for (SemanticTypeComponent sty : fromStys) {
        // remove the semantic type from the fromConcept
        fromConcept.getSemanticTypes().remove(sty);

        // remove the semantic type component
        contentService.removeSemanticTypeComponent(sty.getId());

        if (!toStys.contains(sty)) {
          sty.setId(null);
          SemanticTypeComponentJpa newSemanticType =
              (SemanticTypeComponentJpa) contentService
                  .addSemanticTypeComponent(sty, toConcept);
          newSemanticType.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);

          // add the semantic type and set the last modified by
          toConcept.getSemanticTypes().add(newSemanticType);
        }

      }

      // Add each relationship from/to fromConcept to be attached to toConcept,
      // and delete from fromConcept

      List<ConceptRelationship> fromRelationships =
          new ArrayList<>(fromConcept.getRelationships());

      // Go through all relationships in the fromConcept
      for (final ConceptRelationship rel : fromRelationships) {
        // Any relationship between from and toConcept is deleted
        if (toConcept.getId() == rel.getTo().getId()) {

          // remove the relationship type component from the concept and update
          fromConcept.getRelationships().remove(rel);
          // remove the relationship component
          contentService.removeRelationship(rel.getId(), rel.getClass());

          // If inverse relationship exists, remove it as well
          List<ConceptRelationship> toRelationships =
              new ArrayList<>(toConcept.getRelationships());

          ConceptRelationship inverseRel = null;
          for (final Relationship innerInverseRel : toRelationships) {
            if (innerInverseRel.getTo().getId() == rel.getFrom().getId()
                && innerInverseRel.getFrom().getId() == rel.getTo().getId()) {
              if (inverseRel != null) {
                throw new Exception(
                    "Unexepected more than a single inverse relationship for relationship - "
                        + rel);
              }

              inverseRel = (ConceptRelationship) innerInverseRel;
            }
          }

          if (inverseRel != null) {
            // remove the inverse relationship type component from the concept
            // and
            // update
            toConcept.getRelationships().remove(inverseRel);

            // remove the inverse relationship component
            contentService.removeRelationship(inverseRel.getId(),
                inverseRel.getClass());
          }

        }
        // If relationship is not between two merging concepts, add relationship
        // and inverse toConcept and remove from fromConcept
        else {
          //
          // Remove relationship and inverseRelationship
          //
          // remove the relationship type component from the concept and update
          fromConcept.getRelationships().remove(rel);

          // remove the relationship component
          contentService.removeRelationship(rel.getId(), rel.getClass());

          // If inverse relationship exists, remove it as well
          Concept thirdConcept = rel.getTo();
          List<ConceptRelationship> thirdRelationships =
              new ArrayList<>(thirdConcept.getRelationships());

          ConceptRelationship inverseRel2 = null;
          for (final Relationship innerInverseRel : thirdRelationships) {
            if (innerInverseRel.getTo().getId() == rel.getFrom().getId()
                && innerInverseRel.getFrom().getId() == rel.getTo().getId()) {
              if (inverseRel2 != null) {
                throw new Exception(
                    "Unexepected more than a single inverse relationship for relationship - "
                        + rel);
              }

              inverseRel2 = (ConceptRelationship) innerInverseRel;
            }
          }

          if (inverseRel2 != null) {
            // remove the inverse relationship type component from the concept
            // and update
            thirdConcept.getRelationships().remove(inverseRel2);

            // remove the inverse relationship component
            contentService.removeRelationship(inverseRel2.getId(),
                inverseRel2.getClass());
          }

          //
          // Create and add relationship and inverseRelationship
          //

          // set the relationship component last modified
          rel.setId(null);
          ConceptRelationshipJpa newRel =
              (ConceptRelationshipJpa) contentService.addRelationship(rel);
          newRel.setFrom(toConcept);
          newRel.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);

          // construct inverse relationship
          ConceptRelationshipJpa inverseRel =
              (ConceptRelationshipJpa) contentService
                  .createInverseConceptRelationship(newRel);

          // set the inverse relationship component last modified
          ConceptRelationshipJpa newInverseRel =
              (ConceptRelationshipJpa) contentService
                  .addRelationship(inverseRel);
          newInverseRel.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);

          // add relationship and inverse to respective concepts and set last
          // modified by
          toConcept.getRelationships().add(newRel);
          thirdConcept.getRelationships().add(newInverseRel);

        }
      }

      toConcept.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);

      // update the to concept, and delete the from concept
      contentService.updateConcept(toConcept);
      contentService.removeConcept(fromConcept.getId());

      // log the REST calls
      contentService.addLogEntry(userName, projectId, conceptId, action + " "
          + fromConcept.getId() + " into concept " + toConcept.getId());

      // commit (also removes the lock)
      contentService.commit();

      // Re-read toConcept
      Concept toConceptPostUpdates =
          contentService.getConcept(toConcept.getId());

      // Resolve all three concepts with graphresolutionhandler.resolve(concept)
      // below
      GraphResolutionHandler graphHandler =
          contentService.getGraphResolutionHandler(toConcept.getTerminology());
      graphHandler.resolve(fromConceptPreUpdates);
      graphHandler.resolve(toConceptPreUpdates);
      graphHandler.resolve(toConceptPostUpdates);

      // Websocket notification - one for the updating of the toConcept, and one
      // for the deletion of the fromConcept
      final ChangeEvent<ConceptJpa> event =
          new ChangeEventJpa<ConceptJpa>(action, authToken,
              IdType.CONCEPT.toString(), (ConceptJpa) toConceptPreUpdates,
              (ConceptJpa) toConceptPostUpdates, null);
      sendChangeEvent(event);

      final ChangeEvent<ConceptJpa> event2 = new ChangeEventJpa<ConceptJpa>(
          action, authToken, IdType.CONCEPT.toString(),
          (ConceptJpa) fromConceptPreUpdates, null, null);
      sendChangeEvent(event2);

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
  @Path("/concept/move")
  @ApiOperation(value = "Move atoms from concept to concept", notes = "Move atoms from concept to concept on a project branch", response = ValidationResultJpa.class)
  public ValidationResult moveAtoms(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Concept id, e.g. 2", required = true) @QueryParam("fromConceptId") Long fromConceptId,
    @ApiParam(value = "Concept lastModified, as date", required = true) @QueryParam("lastModified") Long lastModified,
    @ApiParam(value = "Concept id, e.g. 3", required = true) @QueryParam("toConceptId") Long toConceptId,
    @ApiParam(value = "Atoms to move", required = true) List<Long> atomIds,
    @ApiParam(value = "Override warnings", required = false) @QueryParam("overrideWarnings") boolean overrideWarnings,
    @ApiParam(value = "Authorization token, e.g. 'author'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful POST call (MetaEditing): /concept/move/" + projectId
            + "/" + fromConceptId + "/move atoms for user " + authToken
            + " to concept " + toConceptId);

    // Prep reusable variables
    final String action = "MOVE";
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
      final List<Concept> conceptList =
          initialize(contentService, project, fromConceptId, toConceptId,
              userName, action, lastModified, validationResult);

      // Order may have been changed in initialize
      Concept toConcept = null;
      Concept fromConcept = null;
      for (Concept cpt : conceptList) {
        if (cpt.getId() == fromConceptId) {
          fromConcept = cpt;
        }
        if (cpt.getId() == toConceptId) {
          toConcept = cpt;
        }
      }

      if (toConcept == null || fromConcept == null) {
        throw new LocalException(
            "Initialize was unable to correctly load one of the concepts");
      }

      // Make copy of toConcept and fromConcept before changes, to pass into
      // change event
      Concept toConceptPreUpdates = new ConceptJpa(toConcept, false);
      Concept fromConceptPreUpdates = new ConceptJpa(fromConcept, false);

      //
      // Check prerequisites
      //

      // Perform action specific validation - n/a

      // Metadata referential integrity checking

      // Same concept check
      if (fromConceptId == toConceptId) {
        throw new LocalException(
            "Cannot move atoms from concept " + fromConceptId + " to concept "
                + toConceptId + " - identical concept.");
      }

      // Populate move-atom list, and exists check
      List<Atom> moveAtoms = new ArrayList<>();

      for (final Atom atm : fromConcept.getAtoms()) {
        if (atomIds.contains(atm.getId())) {
          moveAtoms.add(atm);
        }
      }

      if (!(moveAtoms.size() == atomIds.size())) {
        throw new LocalException("Atom to move not found on from Concept");
      }

      // TODO - check with Brian if this is required
      // contentService.validateMerge(project, toConcept, fromConcept);

      // if prerequisites fail, return validation result
      if (!validationResult.getErrors().isEmpty()
          || (!validationResult.getWarnings().isEmpty() && !overrideWarnings)) {
        // rollback -- unlocks the concepts and closes transaction
        contentService.rollback();
        return validationResult;
      }

      //
      // Perform the actions (contentService will create atomic actions
      // for CRUD
      // operations)
      //

      // Add each listed atom from fromConcept to toConcept, delete from
      // fromConcept, and set to NEEDS_REVIEW
      contentService.moveAtoms(toConcept, fromConcept, moveAtoms);

      for (Atom atm : moveAtoms) {
        atm.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
      }

      toConcept.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
      fromConcept.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);

      // update the to concept, and delete the from concept
      contentService.updateConcept(toConcept);
      contentService.updateConcept(fromConcept);

      // log the REST calls
      contentService.addLogEntry(userName, projectId, fromConceptId,
          action + " " + atomIds + " from Concept " + fromConcept.getId()
              + " to concept " + toConcept.getId());

      // commit (also removes the lock)
      contentService.commit();

      // Re-read from and toConcept
      Concept fromConceptPostUpdates =
          contentService.getConcept(fromConcept.getId());
      Concept toConceptPostUpdates =
          contentService.getConcept(toConcept.getId());

      // Resolve all four concepts with graphresolutionhandler.resolve(concept)
      // below
      GraphResolutionHandler graphHandler =
          contentService.getGraphResolutionHandler(toConcept.getTerminology());
      graphHandler.resolve(fromConceptPreUpdates);
      graphHandler.resolve(fromConceptPostUpdates);
      graphHandler.resolve(toConceptPreUpdates);
      graphHandler.resolve(toConceptPostUpdates);

      // Websocket notification - one each for the updating the from and
      // toConcept
      final ChangeEvent<ConceptJpa> event =
          new ChangeEventJpa<ConceptJpa>(action, authToken,
              IdType.CONCEPT.toString(), (ConceptJpa) toConceptPreUpdates,
              (ConceptJpa) toConceptPostUpdates, null);
      sendChangeEvent(event);

      final ChangeEvent<ConceptJpa> event2 =
          new ChangeEventJpa<ConceptJpa>(action, authToken,
              IdType.CONCEPT.toString(), (ConceptJpa) fromConceptPreUpdates,
              (ConceptJpa) fromConceptPostUpdates, null);
      sendChangeEvent(event2);

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
   * @param conceptId2 the concept id 2
   * @param userName the user name
   * @param actionType the action type
   * @param lastModified the last modified
   * @param result the result
   * @return the concept
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  private List<Concept> initialize(ContentService contentService,
    Project project, Long conceptId, Long conceptId2, String userName,
    String actionType, long lastModified, ValidationResult result)
    throws Exception {

    // Set transaction mode and start transaction
    contentService.setTransactionPerOperation(false);
    contentService.beginTransaction();

    List<Concept> conceptList = new ArrayList<Concept>();

    List<Long> conceptIdList = new ArrayList<Long>();
    conceptIdList.add(conceptId);
    if (conceptId2 != null && !(conceptId.equals(conceptId2))) {
      conceptIdList.add(conceptId2);
    }

    Collections.sort(conceptIdList);

    Concept mainConcept = null;
    Concept secondaryConcept = null;

    for (final Long i : conceptIdList) {
      Concept tempConcept;

      synchronized (i.toString().intern()) {

        // retrieve the concept
        tempConcept = contentService.getConcept(i);

        // Verify concept exists
        if (tempConcept == null) {
          throw new Exception("Concept does not exist " + i);
        }

        if (i == conceptId) {
          mainConcept = tempConcept;
        }
        if (i == conceptId2) {
          secondaryConcept = tempConcept;
        }

        // Fail if already locked - this is secondary protection
        if (contentService.isObjectLocked(tempConcept)) {
          throw new Exception("Fatal error: concept is locked " + i);
        }

        // lock the concept via JPA
        contentService.lockObject(tempConcept);

        // add the concept to the list
        conceptList.add(new ConceptJpa(tempConcept, true));

      }
    }
    if (secondaryConcept == null) {
      secondaryConcept = mainConcept;
    }

    // construct the molecular action
    final MolecularAction molecularAction = new MolecularActionJpa();
    molecularAction.setTerminology(mainConcept.getTerminology());
    molecularAction.setTerminologyId(mainConcept.getTerminologyId());
    if (conceptId2 != null) {
      molecularAction.setTerminologyId2(secondaryConcept.getTerminologyId());
    }
    molecularAction.setVersion(mainConcept.getVersion());
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
    if (!mainConcept.getTerminology().equals(project.getTerminology())) {
      throw new Exception("Project and concept terminologies do not match");
    }

    if (mainConcept.getLastModified().getTime() != lastModified) {
      throw new LocalException(
          "Concept has changed since last read, please refresh and try again");
    }

    // Return concept list
    return conceptList;
  }

}
