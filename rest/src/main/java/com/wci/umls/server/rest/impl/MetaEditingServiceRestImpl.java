/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.rest.impl;

import java.util.ArrayList;
import java.util.List;

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
import com.wci.umls.server.helpers.TrackingRecordList;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.actions.ChangeEventJpa;
import com.wci.umls.server.jpa.algo.action.AbstractMolecularAction;
import com.wci.umls.server.jpa.algo.action.AddAtomMolecularAction;
import com.wci.umls.server.jpa.algo.action.AddAttributeMolecularAction;
import com.wci.umls.server.jpa.algo.action.AddRelationshipMolecularAction;
import com.wci.umls.server.jpa.algo.action.AddSemanticTypeMolecularAction;
import com.wci.umls.server.jpa.algo.action.ApproveMolecularAction;
import com.wci.umls.server.jpa.algo.action.MergeMolecularAction;
import com.wci.umls.server.jpa.algo.action.MoveMolecularAction;
import com.wci.umls.server.jpa.algo.action.RedoMolecularAction;
import com.wci.umls.server.jpa.algo.action.RemoveAtomMolecularAction;
import com.wci.umls.server.jpa.algo.action.RemoveAttributeMolecularAction;
import com.wci.umls.server.jpa.algo.action.RemoveRelationshipMolecularAction;
import com.wci.umls.server.jpa.algo.action.RemoveSemanticTypeMolecularAction;
import com.wci.umls.server.jpa.algo.action.SplitMolecularAction;
import com.wci.umls.server.jpa.algo.action.UndoMolecularAction;
import com.wci.umls.server.jpa.algo.action.UpdateAtomMolecularAction;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.AttributeJpa;
import com.wci.umls.server.jpa.content.ConceptRelationshipJpa;
import com.wci.umls.server.jpa.content.SemanticTypeComponentJpa;
import com.wci.umls.server.jpa.services.SecurityServiceJpa;
import com.wci.umls.server.jpa.services.rest.MetaEditingServiceRest;
import com.wci.umls.server.model.actions.ChangeEvent;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.content.SemanticTypeComponent;
import com.wci.umls.server.model.meta.IdType;
import com.wci.umls.server.model.workflow.TrackingRecord;
import com.wci.umls.server.model.workflow.WorkflowStatus;
import com.wci.umls.server.services.SecurityService;
import com.wci.umls.server.services.handlers.GraphResolutionHandler;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Info;
import io.swagger.annotations.SwaggerDefinition;

/**
 * REST implementation for {@link MetaEditingServiceRest}..
 */
@Path("/meta")
@Api(value = "/meta")
// TODO: consider renaming this to "MetathesaurusRestImpl" vs "Authoring API"
@SwaggerDefinition(info = @Info(description = "Operations to support metathesaurus editing.", title = "Meta Editing API", version = "1.0.1"))
@Consumes({
    MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
})
@Produces({
    MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
})
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

  /**
   * Post action maintenance.
   *
   * @param action the action
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  private void postActionMaintenance(AbstractMolecularAction action)
    throws Exception {

    List<Concept> conceptList = new ArrayList<Concept>();
    conceptList.add(action.getConcept());
    conceptList.add(action.getConcept2());

    // Only concepts that exist and contain atoms will need to go through this
    // process
    for (Concept c : conceptList) {
      if (c != null && !c.getAtoms().isEmpty()) {

        // Start a new action that doesn't create molecular/atomic actions
        action.beginTransaction();
        action.setMolecularActionFlag(false);

        //
        // Recompute tracking record workflow status
        //

        // Any tracking record that references this concept may potentially be
        // updated.
        final TrackingRecordList trackingRecords = action
            .findTrackingRecordsForConcept(action.getProject(), c, null, null);

        // Set trackingRecord to READY_FOR_PUBLICATION if all contained
        // concepts and atoms are all set to READY_FOR_PUBLICATION.
        if (trackingRecords != null) {
          for (TrackingRecord rec : trackingRecords.getObjects()) {
            final WorkflowStatus status =
                action.computeTrackingRecordStatus(rec);
            rec.setWorkflowStatus(status);
            action.updateTrackingRecord(rec);
          }
        }

        //
        // Recompute the concept's preferred name
        //

        c.setName(action.getComputePreferredNameHandler(c.getTerminology())
            .computePreferredName(c.getAtoms(),
                action.getPrecedenceList(c.getTerminology(), c.getVersion())));

        action.commit();
      }
    }

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
      action.initialize(project, conceptId, null, userName, lastModified, true);

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

      // Perform post-action maintenance on affected concept(s)
      postActionMaintenance(action);

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
        .info("RESTful POST call (MetaEditing): /sty/remove " + projectId + ","
            + conceptId + " for user " + authToken + " with id "
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
      action.initialize(project, conceptId, null, userName, lastModified, true);

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

      // Perform post-action maintenance on affected concept(s)
      postActionMaintenance(action);

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
        .info("RESTful POST call (MetaEditing): /attribute/add " + projectId
            + "," + conceptId + " for user " + authToken
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
      action.initialize(project, conceptId, null, userName, lastModified, true);

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

      // Perform post-action maintenance on affected concept(s)
      postActionMaintenance(action);

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
        .info("RESTful POST call (MetaEditing): /attribute/remove " + projectId
            + "," + conceptId + " for user " + authToken + " with id "
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
      action.initialize(project, conceptId, null, userName, lastModified, true);

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

      // Perform post-action maintenance on affected concept(s)
      postActionMaintenance(action);

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
        .info("RESTful POST call (MetaEditing): /atom/add " + projectId + ","
            + conceptId + " for user " + authToken + " with atom value "
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
      action.initialize(project, conceptId, null, userName, lastModified, true);

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

      // Perform post-action maintenance on affected concept(s)
      postActionMaintenance(action);

      // Websocket notification
      final ChangeEvent<Atom> event = new ChangeEventJpa<Atom>("adding an atom",
          authToken, IdType.ATOM.toString(), null, action.getAtom(),
          action.getConcept());
      sendChangeEvent(event);

      return validationResult;

    } catch (Exception e) {
      action.rollback();
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
        .info("RESTful POST call (MetaEditing): /atom/remove " + projectId + ","
            + conceptId + " remove for user " + authToken + " with id "
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
      action.initialize(project, conceptId, null, userName, lastModified, true);

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

      // Perform post-action maintenance on affected concept(s)
      postActionMaintenance(action);

      // Websocket notification
      final ChangeEvent<Atom> event = new ChangeEventJpa<Atom>(action.getName(),
          authToken, IdType.ATTRIBUTE.toString(), action.getAtom(), null,
          action.getConcept());
      sendChangeEvent(event);

      return validationResult;

    } catch (Exception e) {
      action.rollback();
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
  @Path("/atom/update")
  @ApiOperation(value = "Update an atom", notes = "Update an atom on a project branch", response = ValidationResultJpa.class)
  public ValidationResult updateAtom(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Concept id, e.g. 2", required = true) @QueryParam("conceptId") Long conceptId,
    @ApiParam(value = "Concept lastModified, as date", required = true) @QueryParam("lastModified") Long lastModified,
    @ApiParam(value = "Atom to add", required = true) AtomJpa atom,
    @ApiParam(value = "Override warnings", required = false) @QueryParam("overrideWarnings") boolean overrideWarnings,
    @ApiParam(value = "Authorization token, e.g. 'author'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful POST call (MetaEditing): /atom/update " + projectId
            + ", for user " + authToken + " with atom value " + atom.getName());

    // Instantiate services
    final UpdateAtomMolecularAction action = new UpdateAtomMolecularAction();
    try {

      // Start transaction
      action.setTransactionPerOperation(false);
      action.beginTransaction();
      action.setAtom(atom);
      action.setChangeStatusFlag(true);

      // Authorize project role, get userName
      final String userName = authorizeProject(action, projectId,
          securityService, authToken, "updating an atom", UserRole.AUTHOR);

      // Retrieve the project
      final Project project = action.getProject(projectId);

      // Do some standard intialization and precondition checking
      // action and prep services
      action.initialize(project, conceptId, null, userName, lastModified, true);

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

      // Perform post-action maintenance on affected concept(s)
      postActionMaintenance(action);

      // Websocket notification
      final ChangeEvent<Atom> event = new ChangeEventJpa<Atom>(
          "updating an atom", authToken, IdType.ATOM.toString(), null,
          action.getAtom(), action.getConcept());
      sendChangeEvent(event);

      return validationResult;

    } catch (Exception e) {
      action.rollback();
      handleException(e, "updating an atom");
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
        .info("RESTful POST call (MetaEditing): /relationship/add " + projectId
            + "," + conceptId + " for user " + authToken
            + " with relationship value " + relationship);

    // Instantiate services
    final AddRelationshipMolecularAction action =
        new AddRelationshipMolecularAction();
    try {

      // Start transaction
      action.setTransactionPerOperation(false);
      action.beginTransaction();
      action.setRelationship(relationship);
      action.setChangeStatusFlag(true);

      // Authorize project role, get userName
      final String userName = authorizeProject(action, projectId,
          securityService, authToken, "adding a relationship", UserRole.AUTHOR);

      // Retrieve the project
      final Project project = action.getProject(projectId);

      // Do some standard intialization and precondition checking
      // action and prep services
      action.initialize(project, conceptId, relationship.getTo().getId(),
          userName, lastModified, true);

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

      // Perform post-action maintenance on affected concept(s)
      postActionMaintenance(action);

      // Websocket notification
      final ChangeEvent<ConceptRelationship> event =
          new ChangeEventJpa<ConceptRelationship>(action.getName(), authToken,
              IdType.RELATIONSHIP.toString(), null, action.getRelationship(),
              action.getConcept());
      sendChangeEvent(event);

      return validationResult;

    } catch (Exception e) {
      action.rollback();
      handleException(e, "adding a relationship");
      return null;
    } finally {
      action.close();
      securityService.close();
    }

  }

  /* see superclass */
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
        .info("RESTful POST call (MetaEditing): /relationship/remove "
            + projectId + "," + conceptId + " for user " + authToken
            + " with id " + relationshipId);

    // Instantiate services
    final RemoveRelationshipMolecularAction action =
        new RemoveRelationshipMolecularAction();
    try {

      // Start transaction
      action.setTransactionPerOperation(false);
      action.beginTransaction();
      action.setRelationshipId(relationshipId);
      action.setChangeStatusFlag(true);

      // Look up second conceptId.
      final Long conceptId2 =
          action.getRelationship(relationshipId, ConceptRelationshipJpa.class)
              .getTo().getId();

      // Authorize project role, get userName
      final String userName =
          authorizeProject(action, projectId, securityService, authToken,
              "removing a relationship", UserRole.AUTHOR);

      // Retrieve the project
      final Project project = action.getProject(projectId);

      // Do some standard intialization and precondition checking
      // action and prep services
      action.initialize(project, conceptId, conceptId2, userName, lastModified,
          true);

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

      // Perform post-action maintenance on affected concept(s)
      postActionMaintenance(action);

      // Websocket notification
      final ChangeEvent<ConceptRelationship> event =
          new ChangeEventJpa<ConceptRelationship>(action.getName(), authToken,
              IdType.RELATIONSHIP.toString(), action.getRelationship(), null,
              action.getConcept());
      sendChangeEvent(event);

      return validationResult;

    } catch (

    Exception e) {
      action.rollback();
      handleException(e, "removing a relationship");
      return null;
    } finally {
      action.close();
      securityService.close();
    }
  }

  /* see superclass */
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
    @ApiParam(value = "Authorization token, e.g. 'author'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful POST call (MetaEditing): /concept/merge " + projectId
            + "," + conceptId + " with concept " + conceptId2 + " for user "
            + authToken);

    // Instantiate services
    final MergeMolecularAction action = new MergeMolecularAction();
    try {
      // Start transaction
      action.setTransactionPerOperation(false);
      action.beginTransaction();
      action.setChangeStatusFlag(true);

      // Authorize project role, get userName
      final String userName = authorizeProject(action, projectId,
          securityService, authToken, "merging concepts", UserRole.AUTHOR);

      // Retrieve the project
      final Project project = action.getProject(projectId);
      action.setValidationChecks(project.getValidationChecks());

      // For merge only, need to check the concept Ids, so we can assign the
      // concept with the lowest id to survive, and the one with the highest id
      // to get destroyed.
      Long toConceptId = Math.min(conceptId, conceptId2);
      Long fromConceptId = Math.max(conceptId, conceptId2);

      // Do some standard intialization and precondition checking
      // action and prep services
      action.initialize(project, toConceptId, fromConceptId, userName,
          lastModified, true);

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

      // Perform post-action maintenance on affected concept(s)
      postActionMaintenance(action);

      // Resolve all three concepts with graphresolutionhandler.resolve(concept)
      // so they can be appropriately read by ChangeEvent
      GraphResolutionHandler graphHandler = action
          .getGraphResolutionHandler(action.getToConcept().getTerminology());
      graphHandler.resolve(action.getFromConceptPreUpdates());
      graphHandler.resolve(action.getToConceptPreUpdates());
      graphHandler.resolve(action.getToConceptPostUpdates());

      // Websocket notification - one for the updating of the toConcept, and one
      // for the deletion of the fromConcept
      final ChangeEvent<Concept> event = new ChangeEventJpa<Concept>(
          action.getName(), authToken, IdType.CONCEPT.toString(),
          action.getToConceptPreUpdates(), action.getToConceptPostUpdates(),
          action.getToConceptPostUpdates());
      sendChangeEvent(event);

      final ChangeEvent<Concept> event2 =
          new ChangeEventJpa<Concept>(action.getName(), authToken,
              IdType.CONCEPT.toString(), action.getFromConceptPreUpdates(),
              null, action.getFromConceptPreUpdates());
      sendChangeEvent(event2);

      return validationResult;

    } catch (Exception e) {
      action.rollback();
      handleException(e, "merging concepts");
      return null;
    } finally {
      action.close();
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
    @ApiParam(value = "Concept id, e.g. 2", required = true) @QueryParam("conceptId") Long conceptId,
    @ApiParam(value = "Concept lastModified, as date", required = true) @QueryParam("lastModified") Long lastModified,
    @ApiParam(value = "Concept id, e.g. 3", required = true) @QueryParam("conceptId2") Long conceptId2,
    @ApiParam(value = "Atoms to move", required = true) List<Long> atomIds,
    @ApiParam(value = "Override warnings", required = false) @QueryParam("overrideWarnings") boolean overrideWarnings,
    @ApiParam(value = "Authorization token, e.g. 'author'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful POST call (MetaEditing): /concept/move " + projectId
            + "," + conceptId + " move atoms for user " + authToken
            + " to concept " + conceptId2);

    // Instantiate services
    final MoveMolecularAction action = new MoveMolecularAction();
    try {
      // Start transaction
      action.setTransactionPerOperation(false);
      action.beginTransaction();
      action.setAtomIds(atomIds);
      action.setChangeStatusFlag(true);

      // Authorize project role, get userName
      final String userName = authorizeProject(action, projectId,
          securityService, authToken, "moving atoms", UserRole.AUTHOR);

      // Retrieve the project
      final Project project = action.getProject(projectId);
      action.setValidationChecks(project.getValidationChecks());

      // Do some standard intialization and precondition checking
      // action and prep services
      action.initialize(project, conceptId, conceptId2, userName, lastModified,
          true);

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

      // Perform post-action maintenance on affected concept(s)
      postActionMaintenance(action);

      // Resolve all three concepts with graphresolutionhandler.resolve(concept)
      // so they can be appropriately read by ChangeEvent
      GraphResolutionHandler graphHandler = action
          .getGraphResolutionHandler(action.getToConcept().getTerminology());
      graphHandler.resolve(action.getFromConceptPreUpdates());
      graphHandler.resolve(action.getToConceptPreUpdates());
      graphHandler.resolve(action.getFromConceptPostUpdates());
      graphHandler.resolve(action.getToConceptPostUpdates());

      // Websocket notification - one each for the updating the from and
      // toConcept
      final ChangeEvent<Concept> event = new ChangeEventJpa<Concept>(
          action.getName(), authToken, IdType.CONCEPT.toString(),
          action.getToConceptPreUpdates(), action.getToConceptPostUpdates(),
          action.getToConceptPostUpdates());
      sendChangeEvent(event);

      final ChangeEvent<Concept> event2 = new ChangeEventJpa<Concept>(
          action.getName(), authToken, IdType.CONCEPT.toString(),
          action.getFromConceptPreUpdates(), action.getFromConceptPostUpdates(),
          action.getFromConceptPostUpdates());
      sendChangeEvent(event2);

      return validationResult;

    } catch (Exception e) {
      action.rollback();
      handleException(e, "moving atoms");
      return null;
    } finally {
      action.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @POST
  @Path("/concept/split")
  @ApiOperation(value = "Split concept into two", notes = "Split concept into two on a project branch", response = ValidationResultJpa.class)
  public ValidationResult splitConcept(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Concept id, e.g. 2", required = true) @QueryParam("conceptId") Long conceptId,
    @ApiParam(value = "Concept lastModified, as date", required = true) @QueryParam("lastModified") Long lastModified,
    @ApiParam(value = "Atoms to move", required = true) List<Long> atomIds,
    @ApiParam(value = "Override warnings", required = false) @QueryParam("overrideWarnings") boolean overrideWarnings,
    @ApiParam(value = "Copy relationships", required = false) @QueryParam("copyRelationships") boolean copyRelationships,
    @ApiParam(value = "Copy semantic types", required = false) @QueryParam("copySemanticTypes") boolean copySemanticTypes,
    @ApiParam(value = "Relationship to new concept", required = true) @QueryParam("relationshipType") String relationshipType,
    @ApiParam(value = "Authorization token, e.g. 'author'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful POST call (MetaEditing): /concept/split " + projectId
            + "," + conceptId + " for user " + authToken);

    // Instantiate services
    final SplitMolecularAction action = new SplitMolecularAction();
    try {
      // Start transaction
      action.setTransactionPerOperation(false);
      action.beginTransaction();
      action.setAtomIds(atomIds);
      action.setRelationshipType(relationshipType);
      action.setCopyRelationships(copyRelationships);
      action.setCopySemanticTypes(copySemanticTypes);
      action.setChangeStatusFlag(true);

      // Authorize project role, get userName
      final String userName = authorizeProject(action, projectId,
          securityService, authToken, "splitting concept", UserRole.AUTHOR);

      // Retrieve the project
      final Project project = action.getProject(projectId);
      action.setValidationChecks(project.getValidationChecks());

      // Do some standard intialization and precondition checking
      // action and prep services
      action.initialize(project, conceptId, null, userName, lastModified, true);

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

      // Perform post-action maintenance on affected concept(s)
      postActionMaintenance(action);

      // Resolve all three concepts with graphresolutionhandler.resolve(concept)
      // so they can be appropriately read by ChangeEvent
      GraphResolutionHandler graphHandler = action.getGraphResolutionHandler(
          action.getCreatedConcept().getTerminology());
      graphHandler.resolve(action.getOriginatingConceptPreUpdates());
      graphHandler.resolve(action.getOriginatingConceptPostUpdates());
      graphHandler.resolve(action.getCreatedConceptPostUpdates());

      // Websocket notification - one for the updating of the originating
      // Concept, and one
      // for the created Concept
      final ChangeEvent<Concept> event = new ChangeEventJpa<Concept>(
          action.getName(), authToken, IdType.CONCEPT.toString(),
          action.getOriginatingConceptPreUpdates(),
          action.getOriginatingConceptPostUpdates(),
          action.getOriginatingConceptPostUpdates());
      sendChangeEvent(event);

      final ChangeEvent<Concept> event2 = new ChangeEventJpa<Concept>(
          action.getName(), authToken, IdType.CONCEPT.toString(), null,
          action.getCreatedConceptPostUpdates(),
          action.getCreatedConceptPostUpdates());
      sendChangeEvent(event2);

      return validationResult;

    } catch (Exception e) {
      action.rollback();
      handleException(e, "splitting concept");
      return null;
    } finally {
      action.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @POST
  @Path("/concept/approve")
  @ApiOperation(value = "Approve concept", notes = "Approve concept on a project branch", response = ValidationResultJpa.class)
  public ValidationResult approveConcept(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Concept id, e.g. 2", required = true) @QueryParam("conceptId") Long conceptId,
    @ApiParam(value = "Concept lastModified, as date", required = true) @QueryParam("lastModified") Long lastModified,
    @ApiParam(value = "Override warnings", required = false) @QueryParam("overrideWarnings") boolean overrideWarnings,
    @ApiParam(value = "Authorization token, e.g. 'author'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful POST call (MetaEditing): /concept/approve " + projectId
            + "," + conceptId + " for user " + authToken);

    // Instantiate services
    final ApproveMolecularAction action = new ApproveMolecularAction();
    try {
      // Start transaction
      action.setTransactionPerOperation(false);
      action.beginTransaction();
      action.setChangeStatusFlag(true);

      // Authorize project role, get userName
      final String userName = authorizeProject(action, projectId,
          securityService, authToken, "approving concept", UserRole.AUTHOR);

      // Retrieve the project
      final Project project = action.getProject(projectId);
      action.setValidationChecks(project.getValidationChecks());

      // Do some standard intialization and precondition checking
      // action and prep services
      action.initialize(project, conceptId, null, userName, lastModified, true);

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

      // Perform post-action maintenance on affected concept(s)
      postActionMaintenance(action);

      // Websocket notification - one for the updating of the toConcept, and one
      // for the deletion of the fromConcept
      final ChangeEvent<Concept> event =
          new ChangeEventJpa<Concept>(action.getName(), authToken,
              IdType.CONCEPT.toString(), action.getConceptPreUpdates(),
              action.getConceptPostUpdates(), action.getConceptPostUpdates());
      sendChangeEvent(event);

      return validationResult;

    } catch (Exception e) {
      action.rollback();
      handleException(e, "approving concept");
      return null;
    } finally {
      action.close();
      securityService.close();
    }

  }
  
  /* see superclass */
  @Override
  @POST
  @Path("/action/undo")
  @ApiOperation(value = "Undo action", notes = "Undo a previously performed action", response = ValidationResultJpa.class)
  public ValidationResult undoAction(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Molecular Action id, e.g. 2", required = true) @QueryParam("molecularActionId") Long molecularActionId,
    @ApiParam(value = "Concept lastModified, as date", required = true) @QueryParam("lastModified") Long lastModified,
    @ApiParam(value = "Override warnings", required = false) @QueryParam("overrideWarnings") boolean overrideWarnings,
    @ApiParam(value = "Force action", required = false) @QueryParam("force") boolean force,
    @ApiParam(value = "Authorization token, e.g. 'author'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful POST call (MetaEditing): /action/undo " + projectId
            + ", undo action with id " + molecularActionId + " for user "
            + authToken);

    // Instantiate services
    final UndoMolecularAction action = new UndoMolecularAction();
    try {
      // Start transaction
      action.setTransactionPerOperation(false);
      action.setMolecularActionFlag(false);
      action.beginTransaction();
      action.setMolecularActionId(molecularActionId);
      action.setForce(force);

      // Authorize project role, get userName
      final String userName = authorizeProject(action, projectId,
          securityService, authToken, "undoing action", UserRole.AUTHOR);

      // Retrieve the project
      final Project project = action.getProject(projectId);
      action.setValidationChecks(project.getValidationChecks());

      // Do some standard intialization and precondition checking
      // action and prep services
      // Note - the undo action doesn't create its own molecular and atomic
      // actions
      // Note - if we're undoing a merge, ComponentId2 won't point to an
      // existing concept, so leave that null.
      Long componentId =
          action.getMolecularAction(molecularActionId).getComponentId();
      Long componentId2;
      if (action.getMolecularAction(molecularActionId).getName()
          .equals("MERGE")) {
        componentId2 = null;
      } else {
        componentId2 =
            action.getMolecularAction(molecularActionId).getComponentId2();
      }

      action.initialize(project, componentId, componentId2, userName,
          lastModified, false);

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

      // Perform post-action maintenance on affected concept(s)
      postActionMaintenance(action);

      // Websocket notification
      final ChangeEvent<Concept> event = new ChangeEventJpa<Concept>(
          action.getName(), authToken, IdType.CONCEPT.toString(), null, null,
          action.getConcept(
              action.getMolecularAction(molecularActionId).getComponentId()));
      sendChangeEvent(event);

      if (action.getMolecularAction(molecularActionId).getComponentId2() != null
          && action.getConcept(action.getMolecularAction(molecularActionId)
              .getComponentId2()) != null) {
        final ChangeEvent<Concept> event2 =
            new ChangeEventJpa<Concept>(action.getName(), authToken,
                IdType.CONCEPT.toString(), null, null, action.getConcept(action
                    .getMolecularAction(molecularActionId).getComponentId2()));
        sendChangeEvent(event2);
      }

      return validationResult;

    } catch (Exception e) {
      action.rollback();
      handleException(e, "undoing action");
      return null;
    } finally {
      action.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @POST
  @Path("/action/redo")
  @ApiOperation(value = "Redo action", notes = "Redo a previously undone action", response = ValidationResultJpa.class)
  public ValidationResult redoAction(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Molecular Action id, e.g. 2", required = true) @QueryParam("molecularActionId") Long molecularActionId,
    @ApiParam(value = "Concept lastModified, as date", required = true) @QueryParam("lastModified") Long lastModified,
    @ApiParam(value = "Override warnings", required = false) @QueryParam("overrideWarnings") boolean overrideWarnings,
    @ApiParam(value = "Force action", required = false) @QueryParam("force") boolean force,
    @ApiParam(value = "Authorization token, e.g. 'author'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful POST call (MetaEditing): /action/redo " + projectId
            + ", redo action with id " + molecularActionId + " for user "
            + authToken);

    // Instantiate services
    final RedoMolecularAction action = new RedoMolecularAction();
    try {
      // Start transaction
      action.setTransactionPerOperation(false);
      action.setMolecularActionFlag(false);
      action.beginTransaction();
      action.setMolecularActionId(molecularActionId);
      action.setForce(force);

      // Authorize project role, get userName
      final String userName = authorizeProject(action, projectId,
          securityService, authToken, "undoing action", UserRole.AUTHOR);

      // Retrieve the project
      final Project project = action.getProject(projectId);
      action.setValidationChecks(project.getValidationChecks());

      // Do some standard intialization and precondition checking
      // action and prep services
      // Note - the redo action doesn't create its own molecular and atomic
      // actions
      // Note - if we're redoing a split, ComponentId2 won't point to an
      // existing concept, so leave that null.
      Long componentId =
          action.getMolecularAction(molecularActionId).getComponentId();
      Long componentId2;
      if (action.getMolecularAction(molecularActionId).getName()
          .equals("SPLIT")) {
        componentId2 = null;
      } else {
        componentId2 =
            action.getMolecularAction(molecularActionId).getComponentId2();
      }

      action.initialize(project, componentId, componentId2, userName,
          lastModified, false);

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

      // Perform post-action maintenance on affected concept(s)
      postActionMaintenance(action);

      // Websocket notification
      final ChangeEvent<Concept> event = new ChangeEventJpa<Concept>(
          action.getName(), authToken, IdType.CONCEPT.toString(), null, null,
          action.getConcept(
              action.getMolecularAction(molecularActionId).getComponentId()));
      sendChangeEvent(event);

      if (action.getMolecularAction(molecularActionId).getComponentId2() != null
          && action.getConcept(action.getMolecularAction(molecularActionId)
              .getComponentId2()) != null) {
        final ChangeEvent<Concept> event2 =
            new ChangeEventJpa<Concept>(action.getName(), authToken,
                IdType.CONCEPT.toString(), null, null, action.getConcept(action
                    .getMolecularAction(molecularActionId).getComponentId2()));
        sendChangeEvent(event2);
      }

      return validationResult;

    } catch (Exception e) {
      action.rollback();
      handleException(e, "undoing action");
      return null;
    } finally {
      action.close();
      securityService.close();
    }

  }

}
