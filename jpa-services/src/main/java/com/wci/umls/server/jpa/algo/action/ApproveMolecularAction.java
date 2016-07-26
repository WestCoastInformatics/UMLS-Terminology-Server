/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.action;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.content.SemanticTypeComponent;
import com.wci.umls.server.model.workflow.WorkflowStatus;

/**
 * A molecular action for approving a concept.
 */
public class ApproveMolecularAction extends AbstractMolecularAction {

  /** The concept pre updates. */
  private Concept conceptPreUpdates;

  /** The concept post updates. */
  private Concept conceptPostUpdates;

  /**
   * Instantiates an empty {@link ApproveMolecularAction}.
   *
   * @throws Exception the exception
   */
  public ApproveMolecularAction() throws Exception {
    super();
    // n/a
  }

  /**
   * Returns the concept pre updates.
   *
   * @return the concept pre updates
   */
  public Concept getConceptPreUpdates() {
    return conceptPreUpdates;
  }

  /**
   * Returns the concept post updates.
   *
   * @return the concept post updates
   */
  public Concept getConceptPostUpdates() {
    return conceptPostUpdates;
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {
    final ValidationResult validationResult = new ValidationResultJpa();
    // Perform action specific validation - n/a

    // Metadata referential integrity checking

    // Check preconditions
    this.validateConcept(getProject(), getConcept());

    return validationResult;
  }

  /**
   * Compute.
   *
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public void compute() throws Exception {
    //
    // Perform the action (contentService will create atomic actions for CRUD
    // operations)
    //

    // Make copy of the Concept and fromConcept before changes, to pass into
    // change event
    conceptPreUpdates = new ConceptJpa(getConcept(), false);

    // For each atom, set workflow status to READY_FOR_PUBLICATION if
    // NEEDS_REVIEW, and update Atom
    for (Atom atm : getConcept().getAtoms()) {
      if (atm.getWorkflowStatus().equals(WorkflowStatus.NEEDS_REVIEW)) {
        atm.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);
        updateAtom(atm);
      }
    }

    // For each semantic type component, set workflow status to
    // READY_FOR_PUBLICATION if NEEDS_REVIEW, and update Semantic type component
    for (SemanticTypeComponent sty : getConcept().getSemanticTypes()) {
      if (sty.getWorkflowStatus().equals(WorkflowStatus.NEEDS_REVIEW)) {
        sty.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);
        updateSemanticTypeComponent(sty, getConcept());
      }
    }

    // For each relationship:
    // Remove any with workflowStatus=DEMOTION AND its inverse
    // Change workflow status from NEEDS_REVIEW to READY_FOR_PUBLiCATION
    // Change relationshipType to RO if it is not RO, RB, RN, or XR
    // UpdateRelationship (also do this for its inverse)
    final List<String> typeList = Arrays.asList("RO", "RB", "RN", "XR");
    for (ConceptRelationship rel : getConcept().getRelationships()) {
      if (rel.getWorkflowStatus().equals(WorkflowStatus.NEEDS_REVIEW)) {
        rel.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);
      }
      if (!typeList.contains(rel.getRelationshipType())) {
        rel.setRelationshipType("RO");
      }
      updateRelationship(rel);

      final ConceptRelationship inverseRel =
          (ConceptRelationship) findInverseRelationship(rel);
      if (inverseRel.getWorkflowStatus().equals(WorkflowStatus.NEEDS_REVIEW)) {
        inverseRel.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);
      }
      if (!typeList.contains(inverseRel.getRelationshipType())) {
        inverseRel.setRelationshipType("RO");
      }
      updateRelationship(inverseRel);
    }

    // For the concept itself:
    // Set the lastApproved and lastApprovedBy,
    // set workflow status to READY_FOR_PUBLICATION if NEEDS_REVIEW, and update
    // the concept
    getConcept().setLastApprovedBy(getUserName());
    getConcept().setLastApproved(new Date());

    if (getConcept().getWorkflowStatus().equals(WorkflowStatus.NEEDS_REVIEW)) {
      getConcept().setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);
    }
    
    updateConcept(getConcept());

    
    // Make copy of the Concept and fromConcept before changes, to pass into
    // change event
    conceptPostUpdates = new ConceptJpa(getConcept(), false);

    // log the REST calls
    addLogEntry(getUserName(), getProject().getId(), getConcept().getId(),
        getName() + " " + getConcept());

  }

}
