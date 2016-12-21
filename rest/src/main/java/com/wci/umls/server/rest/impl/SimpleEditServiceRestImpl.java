/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.rest.impl;

import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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
import com.wci.umls.server.UserRole;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.jpa.services.SecurityServiceJpa;
import com.wci.umls.server.jpa.services.rest.ContentServiceRest;
import com.wci.umls.server.jpa.services.rest.SimpleEditServiceRest;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.workflow.WorkflowStatus;
import com.wci.umls.server.services.ContentService;
import com.wci.umls.server.services.SecurityService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Info;
import io.swagger.annotations.SwaggerDefinition;

/**
 * Reference implementation of {@link ContentServiceRest}. Includes hibernate
 * tags for MEME database.
 */
@Path("/edit")
@Consumes({
    MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_HTML
})
@Produces({
    MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
})
@Api(value = "/edit")
@SwaggerDefinition(info = @Info(description = "Operations to perform basic content editing for a terminology.", title = "Simple Edit API", version = "1.0.1"))
public class SimpleEditServiceRestImpl extends RootServiceRestImpl
    implements SimpleEditServiceRest {

  /** The security service. */
  private SecurityService securityService;

  /**
   * Instantiates an empty {@link SimpleEditServiceRestImpl}.
   *
   * @throws Exception the exception
   */
  public SimpleEditServiceRestImpl() throws Exception {
    securityService = new SecurityServiceJpa();
  }

  /* see superclass */
  @PUT
  @Path("/atom")
  @ApiOperation(value = "Add an atom to a concept", notes = "Adds an atom to a concept", response = AtomJpa.class)
  @Override
  public Atom addAtomToConcept(
    @ApiParam(value = "Project id, e.g. 12345", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Concept id, e.g. 43232345", required = true) @QueryParam("conceptId") Long conceptId,
    @ApiParam(value = "Atom to add, as POST data", required = true) AtomJpa atom,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful call (Edit): /atom add "
        + projectId + ", " + conceptId + ", " + atom);

    final ContentService contentService = new ContentServiceJpa();
    try {
      final String userName = authorizeProject(contentService, projectId,
          securityService, authToken, "add atom", UserRole.USER);
      contentService.setTransactionPerOperation(false);
      contentService.beginTransaction();
      contentService.setLastModifiedBy(userName);
      contentService.setMolecularActionFlag(false);

      final Project project = contentService.getProject(projectId);
      if (project == null) {
        throw new LocalException("Invalid project = " + projectId);
      }
      final Concept concept = contentService.getConcept(conceptId);
      if (concept == null) {
        throw new LocalException("Invalid concept = " + conceptId);
      }

      // Borrow info from concept
      atom.setStringClassId("");
      atom.setLexicalClassId("");
      atom.setStringClassId("");
      atom.setStringClassId("");
      atom.setCodeId("");
      atom.setDescriptorId("");
      atom.setConceptId(concept.getTerminologyId());
      atom.setTerminology(concept.getTerminology());
      atom.setVersion(concept.getVersion());
      final Atom newAtom = contentService.addAtom(atom);

      concept.getAtoms().add(newAtom);
      if (atom.getWorkflowStatus() == WorkflowStatus.NEEDS_REVIEW) {
        concept.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
      }
      contentService.updateConcept(concept);

      // TODO: consider other atom class maintenance.

      contentService.commit();
      return newAtom;
    } catch (Exception e) {
      handleException(e, "trying to add atom");
    } finally {
      securityService.close();
    }
    return null;

  }

  /* see superclass */
  @POST
  @Path("/atom")
  @ApiOperation(value = "Update an atom", notes = "Updates an atom ")
  @Override
  public void updateAtom(
    @ApiParam(value = "Project id, e.g. 12345", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Concept id, e.g. 43232345", required = true) @QueryParam("conceptId") Long conceptId,
    @ApiParam(value = "Atom to add, as POST data", required = true) AtomJpa atom,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful call (Edit): /atom update "
        + projectId + ", " + conceptId + ", " + atom.getId());

    final ContentService contentService = new ContentServiceJpa();
    try {
      final String userName = authorizeProject(contentService, projectId,
          securityService, authToken, "add atom", UserRole.USER);
      contentService.setLastModifiedBy(userName);
      contentService.setMolecularActionFlag(false);

      final Project project = contentService.getProject(projectId);
      if (project == null) {
        throw new LocalException("Invalid project = " + projectId);
      }
      if (atom.getId() == null) {
        throw new Exception("Unexpected null atom id ");
      }
      final Atom origAtom = contentService.getAtom(atom.getId());
      if (origAtom == null) {
        throw new Exception("Unexpected missing atom = " + atom.getId());
      }
      final Concept concept = contentService.getConcept(conceptId);
      if (concept == null) {
        throw new LocalException("Invalid concept = " + conceptId);
      }
      boolean found = false;
      for (final Atom a : concept.getAtoms()) {
        if (a.getId().equals(atom.getId())) {
          found = true;
          break;
        }
      }
      if (!found) {
        throw new LocalException(
            "Invalid concept/atom combination = " + conceptId + ", " + atom);
      }

      atom.setStringClassId("");
      atom.setLexicalClassId("");
      atom.setStringClassId("");
      atom.setStringClassId("");
      atom.setCodeId("");
      atom.setDescriptorId("");
      atom.setConceptId(origAtom.getTerminologyId());
      atom.setTerminology(origAtom.getTerminology());
      atom.setVersion(origAtom.getVersion());

      contentService.updateAtom(atom);
      // for now, allow all changes
      if (atom.getWorkflowStatus() == WorkflowStatus.NEEDS_REVIEW) {
        concept.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
      }

    } catch (Exception e) {
      handleException(e, "trying to add atom note");
    } finally {
      securityService.close();
    }

  }
  /* see superclass */

  @DELETE
  @Path("/atom/{atomId}")
  @ApiOperation(value = "Remove an atom", notes = "Removes the atom and detaches it from the concept")
  @Override
  public void removeAtom(
    @ApiParam(value = "Project id, e.g. 12345", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Concept id, e.g. 43232345", required = true) @QueryParam("conceptId") Long conceptId,
    @ApiParam(value = "Atom id, e.g. 482831", required = true) @PathParam("atomId") Long atomId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Edit): /atom/" + atomId + " " + conceptId);

    final ContentService contentService = new ContentServiceJpa();
    try {
      final String userName = authorizeApp(securityService, authToken,
          "remove concept note", UserRole.VIEWER);
      contentService.setTransactionPerOperation(false);
      contentService.beginTransaction();
      contentService.setLastModifiedBy(userName);
      contentService.setMolecularActionFlag(false);

      final Project project = contentService.getProject(projectId);
      if (project == null) {
        throw new LocalException("Invalid project = " + projectId);
      }
      final Atom origAtom = contentService.getAtom(atomId);
      if (origAtom == null) {
        throw new Exception("Unexpected missing atom = " + atomId);
      }

      final Concept concept = contentService.getConcept(conceptId);
      if (concept == null) {
        throw new Exception("Unexpected concept id = " + conceptId);
      }
      if (concept.getAtoms().stream().filter(a -> a.getId().equals(atomId))
          .collect(Collectors.toList()).size() != 1) {
        throw new LocalException("Invalid conceptId/atomId combination = "
            + conceptId + ", " + atomId);
      }

      // for now, allow all changes
      contentService.removeAtom(atomId);
      concept.getAtoms().remove(origAtom);
      contentService.updateConcept(concept);

      contentService.commit();
    } catch (

    Exception e) {
      handleException(e, "trying to remove note from concept");
    } finally {
      contentService.close();
      securityService.close();
    }

  }

}
