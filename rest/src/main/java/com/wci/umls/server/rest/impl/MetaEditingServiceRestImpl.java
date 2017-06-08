/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.rest.impl;

import java.util.ArrayList;
import java.util.Date;
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
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.actions.ChangeEventJpa;
import com.wci.umls.server.jpa.algo.action.AddAtomMolecularAction;
import com.wci.umls.server.jpa.algo.action.AddAttributeMolecularAction;
import com.wci.umls.server.jpa.algo.action.AddDemotionMolecularAction;
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
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.content.SemanticTypeComponent;
import com.wci.umls.server.model.meta.IdType;
import com.wci.umls.server.model.workflow.WorkflowStatus;
import com.wci.umls.server.services.SecurityService;

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

  /* see superclass */
  @Override
  @POST
  @Path("/sty/add")
  @ApiOperation(value = "Add semantic type to concept", notes = "Add semantic type to concept on a project branch", response = ValidationResultJpa.class)
  public ValidationResult addSemanticType(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Concept id, e.g. 2", required = true) @QueryParam("conceptId") Long conceptId,
    @ApiParam(value = "Activity id, e.g. wrk16a_demotions_001", required = true) @QueryParam("activityId") String activityId,
    @ApiParam(value = "Concept lastModified, as date", required = true) @QueryParam("lastModified") Long lastModified,
    @ApiParam(value = "Semantic type to add", required = true) @QueryParam("semanticType") String semanticTypeValue,
    @ApiParam(value = "Override warnings", required = false) @QueryParam("overrideWarnings") boolean overrideWarnings,
    @ApiParam(value = "Authorization token, e.g. 'author'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call (MetaEditing): /sty/add " + projectId + ","
            + conceptId + " for user " + authToken + " with sty value "
            + semanticTypeValue);

    // Instantiate services
    final AddSemanticTypeMolecularAction action =
        new AddSemanticTypeMolecularAction();
    try {

      // Authorize project role, get userName
      final String userName =
          authorizeProject(action, projectId, securityService, authToken,
              "adding a semantic type", UserRole.AUTHOR);

      // Retrieve the project
      final Project project = action.getProject(projectId);
      if (!project.isEditingEnabled()) {
        throw new LocalException(
            "Editing is disabled on project: " + project.getName());
      }

      // Create semantic type component
      final SemanticTypeComponent sty = new SemanticTypeComponentJpa();
      sty.setTerminologyId("");
      sty.setObsolete(false);
      sty.setPublishable(true);
      sty.setPublished(false);
      sty.setWorkflowStatus(WorkflowStatus.PUBLISHED);
      sty.setSemanticType(semanticTypeValue);
      sty.setTerminology(project.getTerminology());
      sty.setVersion(project.getVersion());
      sty.setTimestamp(new Date());

      // Configure the action
      action.setProject(project);
      action.setActivityId(activityId);
      action.setConceptId(conceptId);
      action.setConceptId2(null);
      action.setLastModifiedBy("E-" + userName);
      action.setLastModified(lastModified);
      action.setOverrideWarnings(overrideWarnings);
      action.setTransactionPerOperation(false);
      action.setMolecularActionFlag(true);
      action.setChangeStatusFlag(true);

      action.setSemanticTypeComponent(sty);

      // Perform the action
      final ValidationResult validationResult =
          action.performMolecularAction(action, userName, true);

      // If the action failed, bail out now.
      if (!validationResult.isValid()
          || (!overrideWarnings && validationResult.getWarnings().size() > 0)) {
        return validationResult;
      }

      // Websocket notification
      final ChangeEvent event = new ChangeEventJpa(action.getName(), authToken,
          IdType.SEMANTIC_TYPE.toString(),
          action.getSemanticTypeComponent().getId(), action.getConcept());
      sendChangeEvent(event);

      return validationResult;

    } catch (Exception e) {
      try {
        action.rollback();
      } catch (Exception e2) {
        // do nothing
      }
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
    @ApiParam(value = "Activity id, e.g. wrk16a_demotions_001", required = true) @QueryParam("activityId") String activityId,
    @ApiParam(value = "Concept lastModified, in ms ", required = true) @QueryParam("lastModified") Long lastModified,
    @ApiParam(value = "Semantic type id, e.g. 3", required = true) @PathParam("id") Long semanticTypeComponentId,
    @ApiParam(value = "Override warnings", required = false) @QueryParam("overrideWarnings") boolean overrideWarnings,
    @ApiParam(value = "Authorization token, e.g. 'author'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call (MetaEditing): /sty/remove " + projectId + ","
            + conceptId + " for user " + authToken + " with id "
            + semanticTypeComponentId);

    // Instantiate services
    final RemoveSemanticTypeMolecularAction action =
        new RemoveSemanticTypeMolecularAction();
    try {
      // Authorize project role, get userName
      final String userName =
          authorizeProject(action, projectId, securityService, authToken,
              "removing a semantic type", UserRole.AUTHOR);

      // Retrieve the project
      final Project project = action.getProject(projectId);
      if (!project.isEditingEnabled()) {
        throw new LocalException(
            "Editing is disabled on project: " + project.getName());
      }

      // Configure the action
      action.setProject(project);
      action.setActivityId(activityId);
      action.setConceptId(conceptId);
      action.setConceptId2(null);
      action.setLastModifiedBy("E-" + userName);
      action.setLastModified(lastModified);
      action.setOverrideWarnings(overrideWarnings);
      action.setTransactionPerOperation(false);
      action.setMolecularActionFlag(true);
      action.setChangeStatusFlag(true);

      action.setSemanticTypeComponentId(semanticTypeComponentId);

      // Perform the action
      final ValidationResult validationResult =
          action.performMolecularAction(action, userName, true);

      // If the action failed, bail out now.
      if (!validationResult.isValid()
          || (!overrideWarnings && validationResult.getWarnings().size() > 0)) {
        return validationResult;
      }

      // Websocket notification
      final ChangeEvent event = new ChangeEventJpa(action.getName(), authToken,
          IdType.SEMANTIC_TYPE.toString(),
          action.getSemanticTypeComponent().getId(), action.getConcept());
      sendChangeEvent(event);

      return validationResult;

    } catch (Exception e) {
      try {
        action.rollback();
      } catch (Exception e2) {
        // do nothing
      }
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
    @ApiParam(value = "Activity id, e.g. wrk16a_demotions_001", required = true) @QueryParam("activityId") String activityId,
    @ApiParam(value = "Concept lastModified, as date", required = true) @QueryParam("lastModified") Long lastModified,
    @ApiParam(value = "Attribute to add", required = true) AttributeJpa attribute,
    @ApiParam(value = "Override warnings", required = false) @QueryParam("overrideWarnings") boolean overrideWarnings,
    @ApiParam(value = "Authorization token, e.g. 'author'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call (MetaEditing): /attribute/add " + projectId + ","
            + conceptId + " for user " + authToken + " with attribute value "
            + attribute.getName());

    // Instantiate services
    final AddAttributeMolecularAction action =
        new AddAttributeMolecularAction();
    try {

      // Authorize project role, get userName
      final String userName = authorizeProject(action, projectId,
          securityService, authToken, "adding an attribute", UserRole.AUTHOR);

      // Retrieve the project
      final Project project = action.getProject(projectId);
      if (!project.isEditingEnabled()) {
        throw new LocalException(
            "Editing is disabled on project: " + project.getName());
      }

      // All new content is unpublished and publishable
      attribute.setPublished(false);
      attribute.setPublishable(true);

      // Configure the action
      action.setProject(project);
      action.setActivityId(activityId);
      action.setConceptId(conceptId);
      action.setConceptId2(null);
      action.setLastModifiedBy("E-" + userName);
      action.setLastModified(lastModified);
      action.setOverrideWarnings(overrideWarnings);
      action.setTransactionPerOperation(false);
      action.setMolecularActionFlag(true);
      action.setChangeStatusFlag(true);

      action.setAttribute(attribute);

      // Perform the action
      final ValidationResult validationResult =
          action.performMolecularAction(action, userName, true);

      // If the action failed, bail out now.
      if (!validationResult.isValid()
          || (!overrideWarnings && validationResult.getWarnings().size() > 0)) {
        return validationResult;
      }

      // Websocket notification
      final ChangeEvent event = new ChangeEventJpa(action.getName(), authToken,
          IdType.ATTRIBUTE.toString(), action.getAttribute().getId(),
          action.getConcept());
      sendChangeEvent(event);

      return validationResult;

    } catch (Exception e) {
      try {
        action.rollback();
      } catch (Exception e2) {
        // do nothing
      }

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
    @ApiParam(value = "Activity id, e.g. wrk16a_demotions_001", required = true) @QueryParam("activityId") String activityId,
    @ApiParam(value = "Concept lastModified, in ms ", required = true) @QueryParam("lastModified") Long lastModified,
    @ApiParam(value = "Attribute id, e.g. 3", required = true) @PathParam("id") Long attributeId,
    @ApiParam(value = "Override warnings", required = false) @QueryParam("overrideWarnings") boolean overrideWarnings,
    @ApiParam(value = "Authorization token, e.g. 'author'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call (MetaEditing): /attribute/remove " + projectId + ","
            + conceptId + " for user " + authToken + " with id " + attributeId);

    // Instantiate services
    final RemoveAttributeMolecularAction action =
        new RemoveAttributeMolecularAction();
    try {

      // Authorize project role, get userName
      final String userName = authorizeProject(action, projectId,
          securityService, authToken, "removing an attribute", UserRole.AUTHOR);

      // Retrieve the project
      final Project project = action.getProject(projectId);
      if (!project.isEditingEnabled()) {
        throw new LocalException(
            "Editing is disabled on project: " + project.getName());
      }

      // Configure the action
      action.setProject(project);
      action.setActivityId(activityId);
      action.setConceptId(conceptId);
      action.setConceptId2(null);
      action.setLastModifiedBy("E-" + userName);
      action.setLastModified(lastModified);
      action.setOverrideWarnings(overrideWarnings);
      action.setTransactionPerOperation(false);
      action.setMolecularActionFlag(true);
      action.setChangeStatusFlag(true);

      action.setAttributeId(attributeId);

      // Perform the action
      final ValidationResult validationResult =
          action.performMolecularAction(action, userName, true);

      // If the action failed, bail out now.
      if (!validationResult.isValid()
          || (!overrideWarnings && validationResult.getWarnings().size() > 0)) {
        return validationResult;
      }

      // Websocket notification
      final ChangeEvent event = new ChangeEventJpa(action.getName(), authToken,
          IdType.ATTRIBUTE.toString(), action.getAttribute().getId(),
          action.getConcept());
      sendChangeEvent(event);

      return validationResult;

    } catch (Exception e) {
      try {
        action.rollback();
      } catch (Exception e2) {
        // do nothing
      }
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
    @ApiParam(value = "Activity id, e.g. wrk16a_demotions_001", required = true) @QueryParam("activityId") String activityId,
    @ApiParam(value = "Concept lastModified, as date", required = true) @QueryParam("lastModified") Long lastModified,
    @ApiParam(value = "Atom to add", required = true) AtomJpa atom,
    @ApiParam(value = "Override warnings", required = false) @QueryParam("overrideWarnings") boolean overrideWarnings,
    @ApiParam(value = "Authorization token, e.g. 'author'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call (MetaEditing): /atom/add " + projectId + ","
            + conceptId + " for user " + authToken + " with atom value "
            + atom.getName());

    // Instantiate services
    final AddAtomMolecularAction action = new AddAtomMolecularAction();
    try {

      // Authorize project role, get userName
      final String userName = authorizeProject(action, projectId,
          securityService, authToken, "adding an atom", UserRole.AUTHOR);

      // Retrieve the project
      final Project project = action.getProject(projectId);
      if (!project.isEditingEnabled()) {
        throw new LocalException(
            "Editing is disabled on project: " + project.getName());
      }

      // All new content is unpublished and publishable
      atom.setPublished(false);
      atom.setPublishable(true);

      // Configure the action
      action.setProject(project);
      action.setActivityId(activityId);
      action.setConceptId(conceptId);
      action.setConceptId2(null);
      action.setLastModifiedBy("E-" + userName);
      action.setLastModified(lastModified);
      action.setOverrideWarnings(overrideWarnings);
      action.setTransactionPerOperation(false);
      action.setMolecularActionFlag(true);
      action.setChangeStatusFlag(true);

      action.setAtom(atom);

      // Perform the action
      final ValidationResult validationResult =
          action.performMolecularAction(action, userName, true);

      // If the action failed, bail out now.
      if (!validationResult.isValid()
          || (!overrideWarnings && validationResult.getWarnings().size() > 0)) {
        return validationResult;
      }

      // Websocket notification
      final ChangeEvent event = new ChangeEventJpa("adding an atom", authToken,
          IdType.ATOM.toString(), action.getAtom().getId(),
          action.getConcept());
      sendChangeEvent(event);

      return validationResult;

    } catch (Exception e) {
      try {
        action.rollback();
      } catch (Exception e2) {
        // do nothing
      }
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
    @ApiParam(value = "Activity id, e.g. wrk16a_demotions_001", required = true) @QueryParam("activityId") String activityId,
    @ApiParam(value = "Concept lastModified, in ms ", required = true) @QueryParam("lastModified") Long lastModified,
    @ApiParam(value = "Atom id, e.g. 3", required = true) @PathParam("id") Long atomId,
    @ApiParam(value = "Override warnings", required = false) @QueryParam("overrideWarnings") boolean overrideWarnings,
    @ApiParam(value = "Authorization token, e.g. 'author'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call (MetaEditing): /atom/remove " + projectId + ","
            + conceptId + " remove for user " + authToken + " with id "
            + atomId);

    // Instantiate services
    final RemoveAtomMolecularAction action = new RemoveAtomMolecularAction();
    try {

      // Authorize project role, get userName
      final String userName = authorizeProject(action, projectId,
          securityService, authToken, "removing an atom", UserRole.AUTHOR);

      // Retrieve the project
      final Project project = action.getProject(projectId);
      if (!project.isEditingEnabled()) {
        throw new LocalException(
            "Editing is disabled on project: " + project.getName());
      }

      // Configure the action
      action.setProject(project);
      action.setActivityId(activityId);
      action.setConceptId(conceptId);
      action.setConceptId2(null);
      action.setLastModifiedBy("E-" + userName);
      action.setLastModified(lastModified);
      action.setOverrideWarnings(overrideWarnings);
      action.setTransactionPerOperation(false);
      action.setMolecularActionFlag(true);
      action.setChangeStatusFlag(true);

      action.setAtomId(atomId);

      // Perform the action
      final ValidationResult validationResult =
          action.performMolecularAction(action, userName, true);

      // If the action failed, bail out now.
      if (!validationResult.isValid()
          || (!overrideWarnings && validationResult.getWarnings().size() > 0)) {
        return validationResult;
      }

      // Websocket notification
      final ChangeEvent event = new ChangeEventJpa(action.getName(), authToken,
          IdType.ATTRIBUTE.toString(), action.getAtom().getId(),
          action.getConcept());
      sendChangeEvent(event);

      return validationResult;

    } catch (Exception e) {
      try {
        action.rollback();
      } catch (Exception e2) {
        // do nothing
      }
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
    @ApiParam(value = "Activity id, e.g. wrk16a_demotions_001", required = true) @QueryParam("activityId") String activityId,
    @ApiParam(value = "Concept lastModified, as date", required = true) @QueryParam("lastModified") Long lastModified,
    @ApiParam(value = "Atom to add", required = true) AtomJpa atom,
    @ApiParam(value = "Override warnings", required = false) @QueryParam("overrideWarnings") boolean overrideWarnings,
    @ApiParam(value = "Authorization token, e.g. 'author'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call (MetaEditing): /atom/update " + projectId
            + ", for user " + authToken + " with atom value " + atom.getName());

    // Instantiate services
    final UpdateAtomMolecularAction action = new UpdateAtomMolecularAction();
    try {

      // Authorize project role, get userName
      final String userName = authorizeProject(action, projectId,
          securityService, authToken, "updating an atom", UserRole.AUTHOR);

      // Retrieve the project
      final Project project = action.getProject(projectId);
      if (!project.isEditingEnabled()) {
        throw new LocalException(
            "Editing is disabled on project: " + project.getName());
      }

      // Configure the action
      action.setProject(project);
      action.setActivityId(activityId);
      action.setConceptId(conceptId);
      action.setConceptId2(null);
      action.setLastModifiedBy("E-" + userName);
      action.setLastModified(lastModified);
      action.setOverrideWarnings(overrideWarnings);
      action.setTransactionPerOperation(false);
      action.setMolecularActionFlag(true);
      action.setChangeStatusFlag(true);

      action.setAtom(atom);

      // Perform the action
      final ValidationResult validationResult =
          action.performMolecularAction(action, userName, true);

      // If the action failed, bail out now.
      if (!validationResult.isValid()
          || (!overrideWarnings && validationResult.getWarnings().size() > 0)) {
        return validationResult;
      }

      // Websocket notification
      final ChangeEvent event = new ChangeEventJpa("updating an atom",
          authToken, IdType.ATOM.toString(), action.getAtom().getId(),
          action.getConcept());
      sendChangeEvent(event);

      return validationResult;

    } catch (Exception e) {
      try {
        action.rollback();
      } catch (Exception e2) {
        // do nothing
      }
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
    @ApiParam(value = "Activity id, e.g. wrk16a_demotions_001", required = true) @QueryParam("activityId") String activityId,
    @ApiParam(value = "Concept lastModified, as date", required = true) @QueryParam("lastModified") Long lastModified,
    @ApiParam(value = "Relationship to add", required = true) ConceptRelationshipJpa relationship,
    @ApiParam(value = "Override warnings", required = false) @QueryParam("overrideWarnings") boolean overrideWarnings,
    @ApiParam(value = "Authorization token, e.g. 'author'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call (MetaEditing): /relationship/add " + projectId + ","
            + conceptId + " for user " + authToken + " with relationship value "
            + relationship);

    // Instantiate services
    final AddRelationshipMolecularAction action =
        new AddRelationshipMolecularAction();
    try {

      // Authorize project role, get userName
      final String userName = authorizeProject(action, projectId,
          securityService, authToken, "adding a relationship", UserRole.AUTHOR);

      // Retrieve the project
      final Project project = action.getProject(projectId);
      if (!project.isEditingEnabled()) {
        throw new LocalException(
            "Editing is disabled on project: " + project.getName());
      }

      // All new content is unpublished and publishable
      relationship.setPublished(false);
      if (relationship.getRelationshipType().equals("XR")) {
        relationship.setPublishable(false);
      } else {
        relationship.setPublishable(true);
      }
      // Set defaults for a concept level relationship
      relationship.setStated(true);
      relationship.setInferred(true);
      relationship.setSuppressible(false);
      relationship.setObsolete(false);

      // Configure the action
      action.setProject(project);
      action.setActivityId(activityId);
      // The relationship is FROM conceptId -> conceptId2, and REL
      // is represented in that direction
      action.setConceptId(conceptId);
      action.setConceptId2(relationship.getTo().getId());
      action.setLastModifiedBy("E-" + userName);
      action.setLastModified(lastModified);
      action.setOverrideWarnings(overrideWarnings);
      action.setTransactionPerOperation(false);
      action.setMolecularActionFlag(true);
      action.setChangeStatusFlag(true);

      action.setRelationship(relationship);

      // Perform the action
      final ValidationResult validationResult =
          action.performMolecularAction(action, userName, true);

      // If the action failed, bail out now.
      if (!validationResult.isValid()
          || (!overrideWarnings && validationResult.getWarnings().size() > 0)) {
        return validationResult;
      }

      // Websocket notification
      final ChangeEvent event = new ChangeEventJpa(action.getName(), authToken,
          IdType.RELATIONSHIP.toString(), action.getRelationship().getId(),
          action.getConcept());
      sendChangeEvent(event);

      return validationResult;

    } catch (Exception e) {
      try {
        action.rollback();
      } catch (Exception e2) {
        // do nothing
      }
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
  @Path("/relationships/add")
  @ApiOperation(value = "Add relationships to concept", notes = "Add relationships to concept on a project branch", response = ValidationResultJpa.class)
  public ValidationResult addRelationships(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Concept id, e.g. 2", required = true) @QueryParam("conceptId") Long conceptId,
    @ApiParam(value = "Activity id, e.g. wrk16a_demotions_001", required = true) @QueryParam("activityId") String activityId,
    @ApiParam(value = "Concept lastModified, as date", required = true) @QueryParam("lastModified") Long lastModified,
    @ApiParam(value = "Relationships to add", required = true) List<ConceptRelationshipJpa> relationships,
    @ApiParam(value = "Override warnings", required = false) @QueryParam("overrideWarnings") boolean overrideWarnings,
    @ApiParam(value = "Authorization token, e.g. 'author'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call (MetaEditing): /relationships/add " + projectId
            + "," + conceptId + " for user " + authToken
            + " with relationships = " + relationships);

    // Pre-create all of the vars that will be used across all added
    // relationships
    String userName = null;
    Project project = null;
    Long innerLastModified = lastModified;
    final ValidationResult allValidationResults = new ValidationResultJpa();

    // Create seperate molecular actions for each relationship to be added
    for (ConceptRelationship relationship : relationships) {

      // Instantiate services
      final AddRelationshipMolecularAction action =
          new AddRelationshipMolecularAction();

      try {

        // Authorize project role, get userName (first time only)
        if (userName == null) {
          userName = authorizeProject(action, projectId, securityService,
              authToken, "adding a relationship", UserRole.AUTHOR);
        }

        // Retrieve the project (first time only)
        if (project == null) {
          project = action.getProject(projectId);
          if (!project.isEditingEnabled()) {
            throw new LocalException(
                "Editing is disabled on project: " + project.getName());
          }
        }

        // All new content is unpublished and publishable
        relationship.setPublished(false);
        if (relationship.getRelationshipType().equals("XR")) {
          relationship.setPublishable(false);
        } else {
          relationship.setPublishable(true);
        }
        // Set defaults for a concept level relationship
        relationship.setStated(true);
        relationship.setInferred(true);
        relationship.setSuppressible(false);
        relationship.setObsolete(false);

        // Configure the action
        action.setProject(project);
        action.setActivityId(activityId);
        // The relationship is FROM conceptId -> conceptId2, and REL
        // is represented in that direction
        action.setConceptId(conceptId);
        action.setConceptId2(relationship.getTo().getId());
        action.setLastModifiedBy("E-" + userName);
        action.setLastModified(innerLastModified);
        action.setOverrideWarnings(overrideWarnings);
        action.setTransactionPerOperation(false);
        action.setMolecularActionFlag(true);
        action.setChangeStatusFlag(true);

        action.setRelationship(relationship);

        // Perform the action
        final ValidationResult validationResult =
            action.performMolecularAction(action, userName, true);

        // Add all of the errors/warnings/comments to allValidationResults
        for (String error : validationResult.getErrors()) {
          allValidationResults.getErrors().add(error);
        }
        for (String warning : validationResult.getWarnings()) {
          allValidationResults.getWarnings().add(warning);
        }
        for (String comment : validationResult.getComments()) {
          allValidationResults.getComments().add(comment);
        }

        // If this action failed, bail out here.
        if (!validationResult.isValid() || (!overrideWarnings
            && validationResult.getWarnings().size() > 0)) {
          return allValidationResults;
        }

        // Websocket notification
        final ChangeEvent event = new ChangeEventJpa(action.getName(),
            authToken, IdType.RELATIONSHIP.toString(),
            action.getRelationship().getId(), action.getConcept());
        sendChangeEvent(event);

        // Reload the fromConcept for next pass
        Concept fromConcept = action.getConcept(conceptId);
        innerLastModified = fromConcept.getLastModified().getTime();

      } catch (Exception e) {
        try {
          action.rollback();
        } catch (Exception e2) {
          // do nothing
        }
        handleException(e, "adding relationships");
        return allValidationResults;
      } finally {
        action.close();
        securityService.close();
      }
    }

    return allValidationResults;

  }

  /* see superclass */
  @Override
  @POST
  @Path("/demotion/add")
  @ApiOperation(value = "Add demotion between atoms", notes = "Add demotion between atoms on a project branch", response = ValidationResultJpa.class)
  public ValidationResult addDemotion(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "From Concept id, e.g. 2", required = true) @QueryParam("conceptId") Long conceptId,
    @ApiParam(value = "Activity id, e.g. wrk16a_demotions_001", required = true) @QueryParam("activityId") String activityId,
    @ApiParam(value = "From Concept lastModified, as date", required = true) @QueryParam("lastModified") Long lastModified,
    @ApiParam(value = "To Concept id, e.g. 3", required = true) @QueryParam("conceptId2") Long conceptId2,
    @ApiParam(value = "From Atom id, e.g. 3", required = true) @PathParam("atomId") Long atomId,
    @ApiParam(value = "To Atom id, e.g. 3", required = true) @PathParam("atomId2") Long atomId2,
    @ApiParam(value = "Override warnings", required = false) @QueryParam("overrideWarnings") boolean overrideWarnings,
    @ApiParam(value = "Authorization token, e.g. 'author'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call (MetaEditing): /demotion/add " + projectId + ","
            + conceptId + " from atom " + atomId + " to atom " + atomId2
            + " for user " + authToken);

    // Instantiate services
    final AddDemotionMolecularAction action = new AddDemotionMolecularAction();
    try {

      // Authorize project role, get userName
      final String userName = authorizeProject(action, projectId,
          securityService, authToken, "adding a demotion", UserRole.AUTHOR);

      // Retrieve the project
      final Project project = action.getProject(projectId);
      if (!project.isEditingEnabled()) {
        throw new LocalException(
            "Editing is disabled on project: " + project.getName());
      }

      // Configure the action
      action.setProject(project);
      action.setActivityId(activityId);
      action.setConceptId(conceptId);
      action.setLastModifiedBy("E-" + userName);
      action.setLastModified(lastModified);
      action.setOverrideWarnings(overrideWarnings);
      action.setTransactionPerOperation(false);
      action.setMolecularActionFlag(true);
      action.setChangeStatusFlag(true);

      action.setConceptId2(conceptId2);
      action.setAtomId(atomId);
      action.setAtomId2(atomId2);

      // Perform the action
      final ValidationResult validationResult =
          action.performMolecularAction(action, userName, true);

      // If the action failed, bail out now.
      if (!validationResult.isValid()
          || (!overrideWarnings && validationResult.getWarnings().size() > 0)) {
        return validationResult;
      }

      // Websocket notification
      final ChangeEvent event = new ChangeEventJpa(action.getName(), authToken,
          IdType.RELATIONSHIP.toString(),
          action.getDemotionRelationship().getId(), action.getConcept());
      sendChangeEvent(event);

      return validationResult;

    } catch (Exception e) {
      try {
        action.rollback();
      } catch (Exception e2) {
        // do nothing
      }
      handleException(e, "adding a demotion");
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
    @ApiParam(value = "Activity id, e.g. wrk16a_demotions_001", required = true) @QueryParam("activityId") String activityId,
    @ApiParam(value = "Concept lastModified, in ms ", required = true) @QueryParam("lastModified") Long lastModified,
    @ApiParam(value = "Relationship id, e.g. 3", required = true) @PathParam("id") Long relationshipId,
    @ApiParam(value = "Override warnings", required = false) @QueryParam("overrideWarnings") boolean overrideWarnings,
    @ApiParam(value = "Authorization token, e.g. 'author'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call (MetaEditing): /relationship/remove " + projectId
            + "," + conceptId + " for user " + authToken + " with id "
            + relationshipId);

    // Instantiate services
    final RemoveRelationshipMolecularAction action =
        new RemoveRelationshipMolecularAction();
    try {

      // Authorize project role, get userName
      final String userName =
          authorizeProject(action, projectId, securityService, authToken,
              "removing a relationship", UserRole.AUTHOR);

      // Retrieve the project
      final Project project = action.getProject(projectId);
      if (!project.isEditingEnabled()) {
        throw new LocalException(
            "Editing is disabled on project: " + project.getName());
      }

      // Look up second conceptId.
      final Long conceptId2 =
          action.getRelationship(relationshipId, ConceptRelationshipJpa.class)
              .getTo().getId();

      // Configure the action
      action.setProject(project);
      action.setActivityId(activityId);
      action.setConceptId(conceptId);
      action.setConceptId2(conceptId2);
      action.setLastModifiedBy("E-" + userName);
      action.setLastModified(lastModified);
      action.setOverrideWarnings(overrideWarnings);
      action.setTransactionPerOperation(false);
      action.setMolecularActionFlag(true);
      action.setChangeStatusFlag(true);

      action.setRelationshipId(relationshipId);

      // Perform the action
      final ValidationResult validationResult =
          action.performMolecularAction(action, userName, true);

      // If the action failed, bail out now.
      if (!validationResult.isValid()
          || (!overrideWarnings && validationResult.getWarnings().size() > 0)) {
        return validationResult;
      }
      // Websocket notification
      final ChangeEvent event = new ChangeEventJpa(action.getName(), authToken,
          IdType.RELATIONSHIP.toString(), action.getRelationship().getId(),
          action.getConcept());
      sendChangeEvent(event);

      return validationResult;

    } catch (Exception e) {
      try {
        action.rollback();
      } catch (Exception e2) {
        // do nothing
      }
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
    @ApiParam(value = "From Concept id, e.g. 2", required = true) @QueryParam("conceptId") Long conceptId,
    @ApiParam(value = "Activity id, e.g. wrk16a_demotions_001", required = true) @QueryParam("activityId") String activityId,
    @ApiParam(value = "From Concept lastModified, as date", required = true) @QueryParam("lastModified") Long lastModified,
    @ApiParam(value = "To Concept id, e.g. 3", required = true) @QueryParam("conceptId2") Long conceptId2,
    @ApiParam(value = "Override warnings", required = false) @QueryParam("overrideWarnings") boolean overrideWarnings,
    @ApiParam(value = "Authorization token, e.g. 'author'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call (MetaEditing): /concept/merge " + projectId + ","
            + conceptId + " with concept " + conceptId2 + " for user "
            + authToken);

    // Instantiate services
    final MergeMolecularAction action = new MergeMolecularAction();

    try {

      // Authorize project role, get userName
      final String userName = authorizeProject(action, projectId,
          securityService, authToken, "merging concepts", UserRole.AUTHOR);

      // Retrieve the project
      final Project project = action.getProject(projectId);
      if (!project.isEditingEnabled()) {
        throw new LocalException(
            "Editing is disabled on project: " + project.getName());
      }

      // Configure the action
      action.setProject(project);
      action.setActivityId(activityId);
      action.setConceptId(conceptId);
      action.setConceptId2(conceptId2);
      action.setLastModifiedBy("E-" + userName);
      action.setLastModified(lastModified);
      action.setOverrideWarnings(overrideWarnings);
      action.setTransactionPerOperation(false);
      action.setMolecularActionFlag(true);
      action.setChangeStatusFlag(true);

      // Perform the action
      final ValidationResult validationResult =
          action.performMolecularAction(action, userName, true);

      // If the action failed, bail out now.
      if (!validationResult.isValid()
          || (!overrideWarnings && validationResult.getWarnings().size() > 0)) {
        return validationResult;
      }

      // Websocket notification - one for the updating of the toConcept, and one
      // for the deletion of the fromConcept

      final ChangeEvent event = new ChangeEventJpa(action.getName(), authToken,
          IdType.CONCEPT.toString(), action.getToConcept().getId(),
          action.getToConcept());
      final ChangeEvent event2 = new ChangeEventJpa(action.getName(), authToken,
          IdType.CONCEPT.toString(), action.getFromConcept().getId(),
          action.getFromConcept());
      sendChangeEvents(event2, event);

      return validationResult;

    } catch (Exception e) {
      try {
        action.rollback();
      } catch (Exception e2) {
        // do nothing
      }
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
  @Path("/atom/move")
  @ApiOperation(value = "Move atoms from concept to concept", notes = "Move atoms from concept to concept on a project branch", response = ValidationResultJpa.class)
  public ValidationResult moveAtoms(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "From Concept id, e.g. 2", required = true) @QueryParam("conceptId") Long conceptId,
    @ApiParam(value = "Activity id, e.g. wrk16a_demotions_001", required = true) @QueryParam("activityId") String activityId,
    @ApiParam(value = "From Concept lastModified, as date", required = true) @QueryParam("lastModified") Long lastModified,
    @ApiParam(value = "To Concept id, e.g. 3", required = true) @QueryParam("conceptId2") Long conceptId2,
    @ApiParam(value = "Atoms to move", required = true) List<Long> atomIds,
    @ApiParam(value = "Override warnings", required = false) @QueryParam("overrideWarnings") boolean overrideWarnings,
    @ApiParam(value = "Authorization token, e.g. 'author'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call (MetaEditing): /atom/move " + projectId + ","
            + conceptId + " move atoms for user " + authToken + " to concept "
            + conceptId2);

    // Instantiate services
    final MoveMolecularAction action = new MoveMolecularAction();
    try {

      // Authorize project role, get userName
      final String userName = authorizeProject(action, projectId,
          securityService, authToken, "moving atoms", UserRole.AUTHOR);

      // Retrieve the project
      final Project project = action.getProject(projectId);
      if (!project.isEditingEnabled()) {
        throw new LocalException(
            "Editing is disabled on project: " + project.getName());
      }

      // Configure the action
      action.setProject(project);
      action.setActivityId(activityId);
      action.setConceptId(conceptId);
      action.setConceptId2(conceptId2);
      action.setLastModifiedBy("E-" + userName);
      action.setLastModified(lastModified);
      action.setOverrideWarnings(overrideWarnings);
      action.setTransactionPerOperation(false);
      action.setMolecularActionFlag(true);
      action.setChangeStatusFlag(true);

      action.setAtomIds(atomIds);

      // Perform the action
      final ValidationResult validationResult =
          action.performMolecularAction(action, userName, true);

      // If the action failed, bail out now.
      if (!validationResult.isValid()
          || (!overrideWarnings && validationResult.getWarnings().size() > 0)) {
        return validationResult;
      }

      // Websocket notification - one each for the updating the from and
      // toConcept
      final ChangeEvent event = new ChangeEventJpa(action.getName(), authToken,
          IdType.CONCEPT.toString(), action.getToConcept().getId(),
          action.getToConcept());
      final ChangeEvent event2 = new ChangeEventJpa(action.getName(), authToken,
          IdType.CONCEPT.toString(), action.getFromConcept().getId(),
          action.getFromConcept());
      sendChangeEvents(event, event2);

      return validationResult;

    } catch (Exception e) {
      try {
        action.rollback();
      } catch (Exception e2) {
        // do nothing
      }
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
    @ApiParam(value = "Activity id, e.g. wrk16a_demotions_001", required = true) @QueryParam("activityId") String activityId,
    @ApiParam(value = "Concept lastModified, as date", required = true) @QueryParam("lastModified") Long lastModified,
    @ApiParam(value = "Atoms to move", required = true) List<Long> atomIds,
    @ApiParam(value = "Override warnings", required = false) @QueryParam("overrideWarnings") boolean overrideWarnings,
    @ApiParam(value = "Copy relationships", required = false) @QueryParam("copyRelationships") boolean copyRelationships,
    @ApiParam(value = "Copy semantic types", required = false) @QueryParam("copySemanticTypes") boolean copySemanticTypes,
    @ApiParam(value = "Relationship to new concept", required = true) @QueryParam("relationshipType") String relationshipType,
    @ApiParam(value = "Authorization token, e.g. 'author'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call (MetaEditing): /concept/split " + projectId + ","
            + conceptId + " for user " + authToken);

    // Instantiate services
    final SplitMolecularAction action = new SplitMolecularAction();
    try {

      // Authorize project role, get userName
      final String userName = authorizeProject(action, projectId,
          securityService, authToken, "splitting concept", UserRole.AUTHOR);

      // Retrieve the project
      final Project project = action.getProject(projectId);
      if (!project.isEditingEnabled()) {
        throw new LocalException(
            "Editing is disabled on project: " + project.getName());
      }

      // Configure the action
      action.setProject(project);
      action.setActivityId(activityId);
      action.setConceptId(conceptId);
      action.setConceptId2(null);
      action.setLastModifiedBy("E-" + userName);
      action.setLastModified(lastModified);
      action.setOverrideWarnings(overrideWarnings);
      action.setTransactionPerOperation(false);
      action.setMolecularActionFlag(true);
      action.setChangeStatusFlag(true);

      action.setAtomIds(atomIds);
      action.setRelationshipType(relationshipType);
      action.setCopyRelationships(copyRelationships);
      action.setCopySemanticTypes(copySemanticTypes);

      // Perform the action
      final ValidationResult validationResult =
          action.performMolecularAction(action, userName, true);

      // If the action failed, bail out now.
      if (!validationResult.isValid()
          || (!overrideWarnings && validationResult.getWarnings().size() > 0)) {
        return validationResult;
      }

      // Websocket notification - one for the updating of the originating
      // Concept, and one
      // for the created Concept
      final ChangeEvent event = new ChangeEventJpa(action.getName(), authToken,
          IdType.CONCEPT.toString(), action.getFromConcept().getId(),
          action.getFromConcept());

      final ChangeEvent event2 = new ChangeEventJpa(action.getName(), authToken,
          IdType.CONCEPT.toString(), action.getToConcept().getId(),
          action.getToConcept());
      sendChangeEvents(event, event2);

      return validationResult;

    } catch (Exception e) {
      try {
        action.rollback();
      } catch (Exception e2) {
        // do nothing
      }
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
    @ApiParam(value = "Activity id, e.g. wrk16a_demotions_001", required = true) @QueryParam("activityId") String activityId,
    @ApiParam(value = "Concept lastModified, as date", required = true) @QueryParam("lastModified") Long lastModified,
    @ApiParam(value = "Override warnings", required = false) @QueryParam("overrideWarnings") boolean overrideWarnings,
    @ApiParam(value = "Authorization token, e.g. 'author'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call (MetaEditing): /concept/approve " + projectId + ","
            + conceptId + " for user " + authToken);

    // Instantiate services
    final ApproveMolecularAction action = new ApproveMolecularAction();
    try {

      // Authorize project role, get userName
      final String userName = authorizeProject(action, projectId,
          securityService, authToken, "approving concept", UserRole.AUTHOR);

      // Retrieve the project
      final Project project = action.getProject(projectId);
      if (!project.isEditingEnabled()) {
        throw new LocalException(
            "Editing is disabled on project: " + project.getName());
      }

      // Configure the action
      action.setProject(project);
      action.setActivityId(activityId);
      action.setConceptId(conceptId);
      action.setConceptId2(null);
      action.setLastModifiedBy("E-" + userName);
      action.setLastModified(lastModified);
      action.setOverrideWarnings(overrideWarnings);
      action.setTransactionPerOperation(false);
      action.setMolecularActionFlag(true);
      action.setChangeStatusFlag(true);

      // Perform the action
      final ValidationResult validationResult =
          action.performMolecularAction(action, userName, true);

      // If the action failed, bail out now.
      if (!validationResult.isValid()
          || (!overrideWarnings && validationResult.getWarnings().size() > 0)) {
        return validationResult;
      }

      // Websocket notification - one for the updating of the toConcept, and one
      // for the deletion of the fromConcept
      final ChangeEvent event = new ChangeEventJpa(action.getName(), authToken,
          IdType.CONCEPT.toString(), action.getConcept().getId(),
          action.getConcept());
      sendChangeEvent(event);

      return validationResult;

    } catch (Exception e) {
      try {
        action.rollback();
      } catch (Exception e2) {
        // do nothing
      }
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
    @ApiParam(value = "Activity id, e.g. wrk16a_demotions_001", required = true) @QueryParam("activityId") String activityId,
    @ApiParam(value = "Force action", required = false) @QueryParam("force") boolean force,
    @ApiParam(value = "Authorization token, e.g. 'author'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call (MetaEditing): /action/undo " + projectId
            + ", undo action with id " + molecularActionId + " for user "
            + authToken);

    // Instantiate services
    final UndoMolecularAction action = new UndoMolecularAction();
    try {

      // Authorize project role, get userName
      final String userName = authorizeProject(action, projectId,
          securityService, authToken, "undoing action", UserRole.AUTHOR);

      // Retrieve the project
      final Project project = action.getProject(projectId);
      if (!project.isEditingEnabled()) {
        throw new LocalException(
            "Editing is disabled on project: " + project.getName());
      }

      // Note - the undo action doesn't create its own molecular and atomic
      // actions

      // Get concepts
      final Long conceptId =
          action.getMolecularAction(molecularActionId).getComponentId();
      final Long conceptId2 =
          action.getMolecularAction(molecularActionId).getComponentId2();
      Concept concept = action.getConcept(conceptId);
      Concept concept2 = action.getConcept(conceptId2);

      // Configure the action
      action.setProject(project);
      action.setActivityId(activityId);
      action.setConceptId(conceptId);
      action.setConceptId2(conceptId2);
      action.setLastModifiedBy("E-" + userName);
      action.setTransactionPerOperation(false);
      action.setMolecularActionFlag(false);
      action.setChangeStatusFlag(true);

      action.setMolecularActionId(molecularActionId);
      action.setForce(force);

      // Perform the action
      final ValidationResult validationResult =
          action.performMolecularAction(action, userName, true);

      // If the action failed, bail out now.
      if (!validationResult.getErrors().isEmpty()) {
        return validationResult;
      }

      // Reread concepts in case they were null before the action
      if (concept == null) {
        concept = action.getConcept(conceptId);
      }
      if (concept2 == null) {
        concept2 = action.getConcept(conceptId2);
      }
      // Websocket notification
      final List<ChangeEvent> events = new ArrayList<>();
      final ChangeEvent event = new ChangeEventJpa(action.getName(), authToken,
          IdType.CONCEPT.toString(), null, concept);
      events.add(event);

      if (action.getMolecularAction(molecularActionId)
          .getComponentId2() != null) {
        final ChangeEvent event2 = new ChangeEventJpa(action.getName(),
            authToken, IdType.CONCEPT.toString(), null, concept2);
        events.add(event2);
      }
      sendChangeEvents(events.toArray(new ChangeEvent[] {}));

      return validationResult;

    } catch (Exception e) {
      try {
        action.rollback();
      } catch (Exception e2) {
        // do nothing
      }
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
    @ApiParam(value = "Activity id, e.g. wrk16a_demotions_001", required = true) @QueryParam("activityId") String activityId,
    @ApiParam(value = "Force action", required = false) @QueryParam("force") boolean force,
    @ApiParam(value = "Authorization token, e.g. 'author'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call (MetaEditing): /action/redo " + projectId
            + ", redo action with id " + molecularActionId + " for user "
            + authToken);

    // Instantiate services
    final RedoMolecularAction action = new RedoMolecularAction();
    try {

      // Authorize project role, get userName
      final String userName = authorizeProject(action, projectId,
          securityService, authToken, "undoing action", UserRole.AUTHOR);

      // Retrieve the project
      final Project project = action.getProject(projectId);
      if (!project.isEditingEnabled()) {
        throw new LocalException(
            "Editing is disabled on project: " + project.getName());
      }

      // Note - the redo action doesn't create its own molecular and atomic
      // actions

      // Get concepts
      final Long conceptId =
          action.getMolecularAction(molecularActionId).getComponentId();
      final Long conceptId2 =
          action.getMolecularAction(molecularActionId).getComponentId2();
      Concept concept = action.getConcept(conceptId);
      Concept concept2 = action.getConcept(conceptId2);

      // Configure the action
      action.setProject(project);
      action.setActivityId(activityId);
      action.setConceptId(conceptId);
      action.setConceptId2(conceptId2);
      action.setLastModifiedBy("E-" + userName);
      action.setTransactionPerOperation(false);
      action.setMolecularActionFlag(false);
      action.setChangeStatusFlag(true);

      action.setMolecularActionId(molecularActionId);
      action.setForce(force);

      // Perform the action
      final ValidationResult validationResult =
          action.performMolecularAction(action, userName, true);

      // If the action failed, bail out now.
      if (!validationResult.getErrors().isEmpty()) {
        return validationResult;
      }

      // Reread concepts in case they were null before the action
      if (concept == null) {
        concept = action.getConcept(conceptId);
      }
      if (concept2 == null) {
        concept2 = action.getConcept(conceptId2);
      }

      // Websocket notification
      final List<ChangeEvent> events = new ArrayList<>();
      final ChangeEvent event = new ChangeEventJpa(action.getName(), authToken,
          IdType.CONCEPT.toString(), null, concept);
      events.add(event);

      if (action.getMolecularAction(molecularActionId)
          .getComponentId2() != null) {
        final ChangeEvent event2 = new ChangeEventJpa(action.getName(),
            authToken, IdType.CONCEPT.toString(), null, concept2);
        events.add(event2);
      }
      sendChangeEvents(events.toArray(new ChangeEvent[] {}));

      return validationResult;

    } catch (Exception e) {
      try {
        action.rollback();
      } catch (Exception e2) {
        // do nothing
      }
      handleException(e, "undoing action");
      return null;
    } finally {
      action.close();
      securityService.close();
    }

  }

}
