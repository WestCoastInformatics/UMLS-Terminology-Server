/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.rest.impl;

import java.util.ArrayList;
import java.util.Collections;
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
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.helpers.content.RelationshipList;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.actions.ChangeEventJpa;
import com.wci.umls.server.jpa.actions.MolecularActionJpa;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.AttributeJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.ConceptRelationshipJpa;
import com.wci.umls.server.jpa.content.LexicalClassJpa;
import com.wci.umls.server.jpa.content.SemanticTypeComponentJpa;
import com.wci.umls.server.jpa.content.StringClassJpa;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.jpa.services.SecurityServiceJpa;
import com.wci.umls.server.jpa.services.handlers.LuceneNormalizedStringHandler;
import com.wci.umls.server.jpa.services.rest.MetaEditingServiceRest;
import com.wci.umls.server.model.actions.ChangeEvent;
import com.wci.umls.server.model.actions.MolecularAction;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.content.LexicalClass;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.model.content.SemanticTypeComponent;
import com.wci.umls.server.model.content.StringClass;
import com.wci.umls.server.model.meta.IdType;
import com.wci.umls.server.model.workflow.WorkflowStatus;
import com.wci.umls.server.services.ContentService;
import com.wci.umls.server.services.SecurityService;
import com.wci.umls.server.services.handlers.IdentifierAssignmentHandler;
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
      final List<Concept> conceptList = initialize(contentService, project,
          conceptId, null, userName, action, lastModified, validationResult);

      Concept concept = conceptList.get(0);

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
      if (contentService.getTerminology(semanticType.getTerminology(),
          semanticType.getVersion()) == null) {
        throw new LocalException(
            "Cannot add semanticType with invalid terminology - "
                + semanticType.getTerminology() + ", version: "
                + semanticType.getVersion());
      }

      // Duplicate check
      for (final SemanticTypeComponent s : concept.getSemanticTypes()) {
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
      final SemanticTypeComponentJpa newSemanticType =
          (SemanticTypeComponentJpa) contentService
              .addSemanticTypeComponent(semanticType, concept);

      // add the semantic type and set the last modified by
      concept.getSemanticTypes().add(newSemanticType);
      concept.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);

      // update the concept
      contentService.updateConcept(concept);

      // log the REST call
      contentService.addLogEntry(userName, projectId, conceptId,
          action + " " + newSemanticType.getSemanticType() + " to concept "
              + concept.getTerminologyId());

      // commit (also removes the lock)
      contentService.commit();

      // Websocket notification
      final ChangeEvent<SemanticTypeComponentJpa> event =
          new ChangeEventJpa<SemanticTypeComponentJpa>(action, authToken,
              IdType.SEMANTIC_TYPE.toString(), null, newSemanticType, concept);
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

      final List<Concept> conceptList = initialize(contentService, project,
          conceptId, null, userName, action, lastModified, validationResult);

      Concept concept = conceptList.get(0);

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
          action + " " + semanticTypeComponent.getSemanticType()
              + " from concept " + concept.getTerminologyId());

      // commit (also adds the molecular action and removes the lock)
      contentService.commit();

      // Websocket notification
      final ChangeEvent<SemanticTypeComponentJpa> event =
          new ChangeEventJpa<SemanticTypeComponentJpa>(action, authToken,
              IdType.SEMANTIC_TYPE.toString(),
              (SemanticTypeComponentJpa) semanticTypeComponent, null, concept);
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
      final List<Concept> conceptList = initialize(contentService, project,
          conceptId, null, userName, action, lastModified, validationResult);

      Concept concept = conceptList.get(0);

      //
      // Check prerequisites
      //

      if (concept.getTerminologyId() == "") {
        throw new LocalException(
            "Cannot add an attribute to a concept that doesn't have a TerminologyId (Concept: "
                + concept.getName() + ")");
      }

      // Perform action specific validation - n/a

      // Metadata referential integrity checking
      if (contentService.getAttributeName(attribute.getName(),
          concept.getTerminology(), concept.getVersion()) == null) {
        throw new LocalException(
            "Cannot add invalid attribute - " + attribute.getName());
      }
      if (contentService.getTerminology(attribute.getTerminology(),
          attribute.getVersion()) == null) {
        throw new LocalException(
            "Cannot add attribute with invalid terminology - "
                + attribute.getTerminology() + ", version: "
                + attribute.getVersion());
      }

      // Duplicate check
      for (final Attribute a : concept.getAttributes()) {
        if (a.getName().equals(attribute.getName())
            && a.getValue().equals(attribute.getValue())) {
          throw new LocalException("Duplicate attribute - "
              + attribute.getName() + ", with value " + attribute.getValue());
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

      // Assign alternateTerminologyId
      final IdentifierAssignmentHandler handler = contentService
          .getIdentifierAssignmentHandler(concept.getTerminology());
      final String altId = handler.getTerminologyId(attribute, concept);
      attribute.getAlternateTerminologyIds().put(concept.getTerminology(),
          altId);

      // set the attribute component last modified
      final AttributeJpa newAttribute =
          (AttributeJpa) contentService.addAttribute(attribute, concept);

      // add the attribute and set the last modified by
      concept.getAttributes().add(newAttribute);
      concept.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);

      // update the concept
      contentService.updateConcept(concept);

      // log the REST call
      contentService.addLogEntry(userName, projectId, conceptId,
          action + " " + newAttribute.getName() + " to concept "
              + concept.getTerminologyId());

      // commit (also removes the lock)
      contentService.commit();

      // Websocket notification
      final ChangeEvent<AttributeJpa> event =
          new ChangeEventJpa<AttributeJpa>(action, authToken,
              IdType.ATTRIBUTE.toString(), null, newAttribute, concept);
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
      final List<Concept> conceptList = initialize(contentService, project,
          conceptId, null, userName, action, lastModified, validationResult);

      Concept concept = conceptList.get(0);

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

      // remove the attribute type component from the concept and update
      concept.getAttributes().remove(attribute);
      concept.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
      contentService.updateConcept(concept);

      // remove the attribute component
      contentService.removeAttribute(attribute.getId());

      // log the REST call
      contentService.addLogEntry(userName, projectId, conceptId,
          action + " " + attribute.getName() + " from concept "
              + concept.getTerminologyId());

      // commit (also adds the molecular action and removes the lock)
      contentService.commit();

      // Websocket notification
      final ChangeEvent<AttributeJpa> event = new ChangeEventJpa<AttributeJpa>(
          action, authToken, IdType.ATTRIBUTE.toString(),
          (AttributeJpa) attribute, null, concept);
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

    // Prep reusable variables
    final String action = "ADD_ATOM";
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
          conceptId, null, userName, action, lastModified, validationResult);

      Concept concept = conceptList.get(0);

      //
      // Check prerequisites
      //

      // Perform action specific validation - n/a

      // Metadata referential integrity checking
      if (contentService.getTermType(atom.getTermType(),
          concept.getTerminology(), concept.getVersion()) == null) {
        throw new LocalException(
            "Cannot add atom with invalid term type - " + atom.getTermType());
      }
      if (contentService.getLanguage(atom.getLanguage(),
          concept.getTerminology(), concept.getVersion()) == null) {
        throw new LocalException(
            "Cannot add atom with invalid language - " + atom.getLanguage());
      }
      if (contentService.getTerminology(atom.getTerminology(),
          atom.getVersion()) == null) {
        throw new LocalException("Cannot add atom with invalid terminology - "
            + atom.getTerminology() + ", version: " + atom.getVersion());
      }

      // Duplicate check
      for (final Atom a : concept.getAtoms()) {
        if (a.getName().equals(atom.getName())) {
          throw new LocalException("Duplicate atom - " + atom.getName());
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
      atom.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
      // Assign alternateTerminologyId
      final IdentifierAssignmentHandler handler = contentService
          .getIdentifierAssignmentHandler(concept.getTerminology());

      // Add string and lexical classes to get assign their Ids
      final StringClass strClass = new StringClassJpa();
      strClass.setLanguage(atom.getLanguage());
      strClass.setName(atom.getName());
      atom.setStringClassId(handler.getTerminologyId(strClass));

      // Get normalization handler
      final LexicalClass lexClass = new LexicalClassJpa();
      lexClass.setNormalizedName(new LuceneNormalizedStringHandler()
          .getNormalizedString(atom.getName()));
      atom.setLexicalClassId(handler.getTerminologyId(lexClass));

      final String altId = handler.getTerminologyId(atom);
      atom.getAlternateTerminologyIds().put(concept.getTerminology(), altId);

      // set the atom component last modified
      final AtomJpa newAtom = (AtomJpa) contentService.addAtom(atom);

      // add the atom and set the last modified by
      concept.getAtoms().add(newAtom);
      concept.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);

      // update the concept
      contentService.updateConcept(concept);

      // log the REST call
      contentService.addLogEntry(userName, projectId, conceptId, action + " "
          + newAtom.getName() + " to concept " + concept.getTerminologyId());

      // commit (also removes the lock)
      contentService.commit();

      // Websocket notification
      final ChangeEvent<AtomJpa> event = new ChangeEventJpa<AtomJpa>(action,
          authToken, IdType.ATOM.toString(), null, newAtom, concept);
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

    // Prep reusable variables
    final String action = "REMOVE_ATOM";
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
          conceptId, null, userName, action, lastModified, validationResult);

      Concept concept = conceptList.get(0);

      //
      // Check prerequisites
      //

      // Perform action specific validation - n/a

      // Exists check
      Atom atom = null;
      for (final Atom atm : concept.getAtoms()) {
        if (atm.getId().equals(atomId)) {
          atom = atm;
        }
      }
      if (atom == null) {
        throw new LocalException("Atom to remove does not exist");
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

      // remove the atom from the concept and update
      concept.getAtoms().remove(atom);
      concept.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
      contentService.updateConcept(concept);

      // remove the atom
      contentService.removeAtom(atom.getId());

      // log the REST call
      contentService.addLogEntry(userName, projectId, conceptId, action + " "
          + atom.getName() + " from concept " + concept.getTerminologyId());

      // commit (also adds the molecular action and removes the lock)
      contentService.commit();

      // Websocket notification
      final ChangeEvent<AtomJpa> event =
          new ChangeEventJpa<AtomJpa>(action, authToken,
              IdType.ATTRIBUTE.toString(), (AtomJpa) atom, null, concept);
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
      System.out.println("The conceptId is " + conceptId
          + ", and the toConcept ID is " + relationship.getTo().getId());

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
        throw new LocalException(
            "Cannot add relationship with invalid relationship type - "
                + relationship.getRelationshipType());
      }
      if (contentService.getAdditionalRelationshipType(
          relationship.getAdditionalRelationshipType(),
          relationship.getTerminology(), relationship.getVersion()) == null) {
        throw new LocalException(
            "Cannot add relationship with invalid additional relationship type - "
                + relationship.getAdditionalRelationshipType());
      }
      if (contentService.getTerminology(relationship.getTerminology(),
          relationship.getVersion()) == null) {
        throw new LocalException(
            "Cannot add relationship with invalid terminology - "
                + relationship.getTerminology() + ", version: "
                + relationship.getVersion());
      }

      // Duplicate check
      for (final ConceptRelationship a : concept.getRelationships()) {
        if (a.equals(relationship)) {
          throw new LocalException("Duplicate relationship - " + relationship);
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
      // Assign alternateTerminologyId - this was moved to release time.
//      final IdentifierAssignmentHandler handler = contentService
//          .getIdentifierAssignmentHandler(concept.getTerminology());
//
//      final String altId = handler.getTerminologyId(relationship);
//      relationship.getAlternateTerminologyIds().put(concept.getTerminology(),
//          altId);

      // set the relationship component last modified
      final ConceptRelationshipJpa newRelationship =
          (ConceptRelationshipJpa) contentService.addRelationship(relationship);

      // construct inverse relationship
      ConceptRelationshipJpa inverseRelationship =
          (ConceptRelationshipJpa) contentService
              .createInverseConceptRelationship(newRelationship);

      // pass to handler.getTerminologyId - this was moved to release time.
//      final String inverseAltId = handler.getTerminologyId(inverseRelationship);
//      inverseRelationship.getAlternateTerminologyIds()
//          .put(concept.getTerminology(), inverseAltId);

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

    // Look up ToConcept Id
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

      // Exists check for inverse Relationship

      RelationshipList relList = contentService.findConceptRelationships(
          toConcept.getTerminologyId(), toConcept.getTerminology(),
          toConcept.getVersion(), Branch.ROOT, "fromId:"
              + toConcept.getId() + " AND toId:" + concept.getId(),
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
