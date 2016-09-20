/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.rest.impl;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import com.wci.umls.server.UserRole;
import com.wci.umls.server.helpers.ComponentInfo;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.TypeKeyValue;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.AtomRelationshipJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.ConceptRelationshipJpa;
import com.wci.umls.server.jpa.helpers.TypeKeyValueJpa;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.jpa.services.SecurityServiceJpa;
import com.wci.umls.server.jpa.services.WorkflowServiceJpa;
import com.wci.umls.server.jpa.services.rest.IntegrationTestServiceRest;
import com.wci.umls.server.jpa.workflow.WorklistJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.AtomRelationship;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.model.content.SemanticTypeComponent;
import com.wci.umls.server.model.workflow.Worklist;
import com.wci.umls.server.services.ContentService;
import com.wci.umls.server.services.SecurityService;
import com.wci.umls.server.services.WorkflowService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Info;
import io.swagger.annotations.SwaggerDefinition;

/**
 * REST implementation for {@link IntegrationTestServiceRest}..
 */
@Path("/test")
@Api(value = "/test")
@SwaggerDefinition(info = @Info(description = "Operations to support integration tests.", title = "Integration test API", version = "1.0.1"))
@Consumes({
    MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
})
@Produces({
    MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
})
public class IntegrationTestServiceRestImpl extends RootServiceRestImpl
    implements IntegrationTestServiceRest {

  /** The security service. */
  private SecurityService securityService;

  /**
   * Instantiates an empty {@link IntegrationTestServiceRestImpl}.
   *
   * @throws Exception the exception
   */
  public IntegrationTestServiceRestImpl() throws Exception {
    securityService = new SecurityServiceJpa();
  }

  /* see superclass */
  @Override
  @PUT
  @Path("/concept/add")
  @ApiOperation(value = "Add new concept", notes = "Creates a new concept", response = ConceptJpa.class)
  public Concept addConcept(
    @ApiParam(value = "Concept, e.g. newConcept", required = true) ConceptJpa concept,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call PUT (TEST): /add " + concept);

    final ContentService contentService = new ContentServiceJpa();
    try {
      final String authUser = authorizeApp(securityService, authToken,
          "add concept", UserRole.ADMINISTRATOR);
      contentService.setLastModifiedBy(authUser);
      contentService.setMolecularActionFlag(false);

      // Add concept
      final Concept newConcept = contentService.addConcept(concept);
      newConcept.setTerminologyId(newConcept.getId().toString());
      contentService.updateConcept(newConcept);
      return newConcept;
    } catch (Exception e) {
      handleException(e, "trying to add a concept");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @PUT
  @Path("/concept/update")
  @ApiOperation(value = "Update concept", notes = "Updates the concept")
  public void updateConcept(
    @ApiParam(value = "Concept, e.g. newConcept", required = true) ConceptJpa concept,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call PUT (TEST): /update " + concept);

    final ContentService contentService = new ContentServiceJpa();
    try {
      final String authUser = authorizeApp(securityService, authToken,
          "update concept", UserRole.ADMINISTRATOR);
      contentService.setLastModifiedBy(authUser);
      contentService.setMolecularActionFlag(false);

      if (concept.getId() == null) {
        throw new Exception(
            "Only a concept that exists can be udpated: " + concept);
      }
      // Update concept
      contentService.updateConcept(concept);

    } catch (Exception e) {
      handleException(e, "trying to update a concept");
    } finally {
      contentService.close();
      securityService.close();
    }

  }

  /**
   * Removes the concept.
   *
   * @param id the id
   * @param cascade the cascade
   * @param authToken the auth token
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  @DELETE
  @Path("/concept/remove/{id}")
  @ApiOperation(value = "Remove concept", notes = "Removes the concept with the specified id")
  public void removeConcept(
    @ApiParam(value = "Concept id, e.g. 3", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Remove all attached components", required = false) @QueryParam("cascade") boolean cascade,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call DELETE (TEST): /remove/" + id);

    final ContentService contentService = new ContentServiceJpa();
    try {
      String authUser = authorizeApp(securityService, authToken,
          "remove concept", UserRole.ADMINISTRATOR);
      contentService.setLastModifiedBy(authUser);
      contentService.setMolecularActionFlag(false);
      Concept concept = contentService.getConcept(id);

      if (cascade) {
        for (ConceptRelationship rel : concept.getRelationships()) {
          // Remove relationship and inverse.
          contentService.removeRelationship(rel.getId(), rel.getClass());

          // Remove inverse as well
          for (Relationship<? extends ComponentInfo, ? extends ComponentInfo> inverseRel : contentService
              .getInverseRelationships(rel).getObjects()) {
            // TODO - figure out what distinguishing feature is
            if (true) {
              contentService.removeRelationship(inverseRel.getId(),
                  rel.getClass());
            }
          }
        }
      }

      // Make copy of the concept, so we can remove all of the components once
      // it's been removed
      Concept copyConcept = new ConceptJpa(concept, false);

      // Create service and configure transaction scope
      contentService.removeConcept(id);

      if (cascade) {
        for (Attribute atr : copyConcept.getAttributes()) {
          contentService.removeAttribute(atr.getId());
        }
        for (Atom a : copyConcept.getAtoms()) {
          contentService.removeAtom(a.getId());
        }

        for (SemanticTypeComponent sty : copyConcept.getSemanticTypes()) {
          contentService.removeSemanticTypeComponent(sty.getId());
        }
      }

    } catch (Exception e) {
      handleException(e, "trying to remove a concept");
    } finally {
      contentService.close();
      securityService.close();
    }

  }

  @Override
  @GET
  @Path("/sty/{styId}")
  @ApiOperation(value = "Get a semantic type component", notes = "Get a semantic type component", response = SemanticTypeComponent.class)
  public SemanticTypeComponent getSemanticTypeComponent(
    @ApiParam(value = "Semantic Type Component id, e.g. 1", required = true) @PathParam("styId") Long styId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Integration Test): /sty/" + styId);

    ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "get semantic type component",
          UserRole.ADMINISTRATOR);
      SemanticTypeComponent newSty =
          contentService.getSemanticTypeComponent(styId);
      if (newSty == null) {
        return null;
      } else {
        contentService.getGraphResolutionHandler(ConfigUtility.DEFAULT)
            .resolve(newSty);
        return newSty;
      }
    } catch (Exception e) {

      handleException(e, "trying to get a semantic type component");
    } finally {
      contentService.close();
      securityService.close();
    }
    return null;
  }

  @Override
  @GET
  @Path("/concept/relationship/{id}")
  @ApiOperation(value = "Get a concept relationship", notes = "Get a concept relationship", response = ConceptRelationship.class)
  public ConceptRelationship getConceptRelationship(
    @ApiParam(value = "Concept Relationship id, e.g. 1", required = true) @PathParam("id") Long relationshipId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call (Integration Test): /relationship/" + relationshipId);

    ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "get concept relationship",
          UserRole.ADMINISTRATOR);

      ConceptRelationship newRel = (ConceptRelationship) contentService
          .getRelationship(relationshipId, ConceptRelationshipJpa.class);
      if (newRel == null) {
        return null;
      } else {
        contentService.getGraphResolutionHandler(ConfigUtility.DEFAULT)
            .resolve(newRel);
        return newRel;
      }

    } catch (Exception e) {

      handleException(e, "trying to get a concept relationship");
    } finally {
      contentService.close();
      securityService.close();
    }
    return null;
  }

  @Override
  @GET
  @Path("/attribute/{attributeId}")
  @ApiOperation(value = "Get an attribute", notes = "Get an attribute", response = Attribute.class)
  public Attribute getAttribute(
    @ApiParam(value = "Attribute id, e.g. 1", required = true) @PathParam("attributeId") Long attributeId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Integration Test): /attribute/" + attributeId);

    ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "get attribute",
          UserRole.ADMINISTRATOR);

      return contentService.getAttribute(attributeId);

    } catch (Exception e) {

      handleException(e, "trying to get an attribute");
    } finally {
      contentService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @GET
  @Path("/atom/{id}")
  @ApiOperation(value = "Get an atom", notes = "Get an atom", response = Atom.class)
  public Atom getAtom(
    @ApiParam(value = "Atom id, e.g. 1", required = true) @PathParam("id") Long atomId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Integration Test): /atom/" + atomId);

    ContentService contentService = new ContentServiceJpa();
    try {
      authorizeApp(securityService, authToken, "get atom",
          UserRole.ADMINISTRATOR);

      Atom newAtom = contentService.getAtom(atomId);
      if (newAtom == null) {
        return null;
      } else {
        contentService.getGraphResolutionHandler(ConfigUtility.DEFAULT)
            .resolve(newAtom);
        return newAtom;
      }
    } catch (Exception e) {

      handleException(e, "trying to get an atom");
    } finally {
      contentService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @PUT
  @Path("/atom/update")
  @ApiOperation(value = "Update atom", notes = "Updates the atom")
  public void updateAtom(
    @ApiParam(value = "Atom, e.g. new atom", required = true) AtomJpa atom,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call PUT (TEST): /update " + atom);

    final ContentService contentService = new ContentServiceJpa();
    try {
      final String authUser = authorizeApp(securityService, authToken,
          "update atom", UserRole.ADMINISTRATOR);
      contentService.setLastModifiedBy(authUser);
      contentService.setMolecularActionFlag(false);

      if (atom.getId() == null) {
        throw new Exception(
            "Only a concept that exists can be udpated: " + atom);
      }
      // Update atom
      contentService.updateAtom(atom);

    } catch (Exception e) {
      handleException(e, "trying to update a atom");
    } finally {
      contentService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @PUT
  @Path("/concept/relationship/add")
  @ApiOperation(value = "Add new concept relationship", notes = "Creates a new concept relationship", response = ConceptRelationshipJpa.class)
  public ConceptRelationship addRelationship(
    @ApiParam(value = "a concept relationship", required = true) ConceptRelationshipJpa relationship,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call PUT (TEST): /relationship/add " + relationship);

    final ContentService contentService = new ContentServiceJpa();
    try {
      final String authUser = authorizeApp(securityService, authToken,
          "add relationship", UserRole.ADMINISTRATOR);
      contentService.setLastModifiedBy(authUser);
      contentService.setMolecularActionFlag(false);

      // Add relationship
      final ConceptRelationship newRel =
          (ConceptRelationship) contentService.addRelationship(relationship);
      return newRel;
    } catch (Exception e) {
      handleException(e, "trying to add a relationship");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @PUT
  @Path("/atom/relationship/add")
  @ApiOperation(value = "Add new atom relationship", notes = "Creates a new atom relationship", response = ConceptRelationshipJpa.class)
  public AtomRelationship addRelationship(
    @ApiParam(value = "A atom relationship", required = true) AtomRelationshipJpa relationship,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call PUT (TEST): /relationship/add " + relationship);

    final ContentService contentService = new ContentServiceJpa();
    try {
      final String authUser = authorizeApp(securityService, authToken,
          "add relationship", UserRole.ADMINISTRATOR);
      contentService.setLastModifiedBy(authUser);
      contentService.setMolecularActionFlag(false);

      // Add relationship
      final AtomRelationship newRel =
          (AtomRelationship) contentService.addRelationship(relationship);
      return newRel;
    } catch (Exception e) {
      handleException(e, "trying to add a relationship");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @PUT
  @Path("/concept/relationship/update")
  @ApiOperation(value = "Update relationship", notes = "Updates the relationship", response = ConceptRelationshipJpa.class)
  public void updateRelationship(
    @ApiParam(value = "ConceptRelationship", required = true) ConceptRelationshipJpa relationship,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call PUT (TEST): /relationship/update " + relationship);

    final ContentService contentService = new ContentServiceJpa();
    try {
      final String authUser = authorizeApp(securityService, authToken,
          "update relationship", UserRole.ADMINISTRATOR);
      contentService.setLastModifiedBy(authUser);
      contentService.setMolecularActionFlag(false);

      if (relationship.getId() == null) {
        throw new Exception(
            "Only a relationship that exists can be udpated: " + relationship);
      }

      // Update relationship
      contentService.updateRelationship(relationship);

    } catch (Exception e) {
      handleException(e, "trying to add a relationship");
    } finally {
      contentService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/worklist/{id}")
  @ApiOperation(value = "Get a worklist", notes = "Get a worklist", response = WorklistJpa.class)
  public Worklist getWorklist(
    @ApiParam(value = "Worklist id, e.g. 1", required = true) @PathParam("id") Long worklistId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Integration Test): /worklist/" + worklistId);

    WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeApp(securityService, authToken, "get worklist", UserRole.USER);
      return workflowService.getWorklist(worklistId);
    } catch (Exception e) {

      handleException(e, "trying to remove a worklist");
    } finally {
      workflowService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @PUT
  @Path("/typekeyvalue/add")
  @ApiOperation(value = "Add new TypeKeyValue", notes = "Creates a new TypeKeyValue", response = TypeKeyValue.class)
  public TypeKeyValue addTypeKeyValue(
    @ApiParam(value = "TypeKeyValue, e.g. newTypeKeyValue", required = true) TypeKeyValueJpa typeKeyValue,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call PUT (TEST): /add " + typeKeyValue);

    final ContentService contentService = new ContentServiceJpa();
    try {
      final String authUser = authorizeApp(securityService, authToken,
          "add typeKeyValue", UserRole.ADMINISTRATOR);
      contentService.setLastModifiedBy(authUser);
      contentService.setMolecularActionFlag(false);

      // Add TypeKeyValue
      return contentService.addTypeKeyValue(typeKeyValue);
    } catch (Exception e) {
      handleException(e, "trying to add a typeKeyValue");
      return null;
    } finally {
      contentService.close();
      securityService.close();
    }

  }

}
