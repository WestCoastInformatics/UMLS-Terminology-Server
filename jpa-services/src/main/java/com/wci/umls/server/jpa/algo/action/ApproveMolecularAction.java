/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.action;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.AtomRelationship;
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
    validationResult.merge(super.checkPreconditions());
    validationResult
        .merge(validateConcept(this.getProject(), this.getConcept()));
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

    // Make copy of the Concept before changes, to pass into
    // change event
    conceptPreUpdates = new ConceptJpa(getConcept(), false);

    //
    // Get each object in the Concept
    //
    List<Atom> atoms = getConcept().getAtoms();
    List<SemanticTypeComponent> stys = getConcept().getSemanticTypes();
    List<ConceptRelationship> relationships = new CopyOnWriteArrayList<>();
    for (final ConceptRelationship rel : getConcept().getRelationships()) {
      relationships.add(rel);
    }
    List<ConceptRelationship> inverseRelationships =
        new CopyOnWriteArrayList<>();
    for (final ConceptRelationship rel : getConcept().getRelationships()) {
      ConceptRelationship inverseRel =
          (ConceptRelationship) findInverseRelationship(rel);
      inverseRelationships.add(inverseRel);
    }

    //
    // Any demotion relationship (AND its inverse) need to be removed
    //
    final Map<Atom, AtomRelationship> atomsDemotions = new HashMap<>();
    for (final Atom atom : atoms) {
      for (AtomRelationship atomRel : atom.getRelationships()) {
        if (atomRel.getWorkflowStatus().equals(WorkflowStatus.DEMOTION)) {
          atomsDemotions.put(atom, atomRel);

          final AtomRelationship inverseRel =
              (AtomRelationship) findInverseRelationship(atomRel);

          // If the inverseDemotion's atom is also in the concept being
          // approved, use That copy of the atom, instead of the one pulled from
          // the demotion (which can cause errors due to making different
          // changes to the different copies of the same atom)
          boolean inverseRelAdded = false;
          for (Atom inverseAtom : atoms) {
            if (inverseAtom.getId().equals(inverseRel.getFrom().getId())) {
              atomsDemotions.put(inverseAtom, inverseRel);
              inverseRelAdded = true;
              break;
            }
          }
          // If the inverseDemotion's atom is NOT in the concept, create a new
          // AtomJpa copy, and use that instead
          if (!inverseRelAdded) {
            atomsDemotions.put(new AtomJpa(inverseRel.getFrom()), inverseRel);
          }
        }
      }
    }

    //
    // Remove demotions from appropriate atoms
    //
    for (Map.Entry<Atom, AtomRelationship> atomDemotion : atomsDemotions
        .entrySet()) {
      Atom atom = atomDemotion.getKey();
      AtomRelationship demotion = atomDemotion.getValue();
      atom.getRelationships().remove(demotion);
    }

    //
    // Update any atom that had demotion removed from it
    //
    for (Atom atom : atomsDemotions.keySet()) {
      updateAtom(atom);
    }

    //
    // Remove the demotions from the database
    //
    for (AtomRelationship demotion : atomsDemotions.values()) {
      removeRelationship(demotion.getId(), demotion.getClass());
    }

    //
    // Change status of the components
    //

    // For each atom, set workflow status to READY_FOR_PUBLICATION if
    // NEEDS_REVIEW
    for (final Atom atm : atoms) {
      if (atm.getWorkflowStatus().equals(WorkflowStatus.NEEDS_REVIEW)) {
        atm.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);
      }
    }

    // For each semantic type component, set workflow status to
    // READY_FOR_PUBLICATION if NEEDS_REVIEW, and update Semantic type component
    for (final SemanticTypeComponent sty : stys) {
      if (sty.getWorkflowStatus().equals(WorkflowStatus.NEEDS_REVIEW)) {
        sty.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);
      }
    }

    // For each relationship that wasn't removed:
    // Change workflow status from NEEDS_REVIEW to READY_FOR_PUBLiCATION
    // Change relationshipType to RO if it is not RO, RB, RN, or XR
    final List<String> typeList = Arrays.asList("RO", "RB", "RN", "XR");
    for (final ConceptRelationship rel : relationships) {
      if (rel.getWorkflowStatus().equals(WorkflowStatus.NEEDS_REVIEW)) {
        rel.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);
      }
      if (!typeList.contains(rel.getRelationshipType())) {
        rel.setRelationshipType("RO");
      }
    }
    for (final ConceptRelationship inverseRel : inverseRelationships) {
      if (inverseRel.getWorkflowStatus().equals(WorkflowStatus.NEEDS_REVIEW)) {
        inverseRel.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);
      }
      if (!typeList.contains(inverseRel.getRelationshipType())) {
        inverseRel.setRelationshipType("RO");
      }
    }

    //
    // Update modified objects
    //
    for (Atom atm : atoms) {
      updateAtom(atm);
    }
    for (SemanticTypeComponent sty : stys) {
      updateSemanticTypeComponent(sty, getConcept());
    }
    for (final ConceptRelationship rel : relationships) {
      updateRelationship(rel);
    }
    for (final ConceptRelationship inverseRel : inverseRelationships) {
      updateRelationship(inverseRel);
    }

    //
    // Change status of the concept
    //
    // Set workflow status to READY_FOR_PUBLICATION if NEEDS_REVIEW
    // Also set the lastApproved and lastApprovedBy,
    if (getConcept().getWorkflowStatus().equals(WorkflowStatus.NEEDS_REVIEW)) {
      getConcept().setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);
    }

    getConcept().setLastApprovedBy(getLastModifiedBy());
    getConcept().setLastApproved(new Date());

    //
    // update the Concept
    //
    updateConcept(getConcept());

    // log the REST calls
    addLogEntry(getLastModifiedBy(), getProject().getId(), getConcept().getId(),
        getActivityId(), getWorkId(), getName() + " concept "
            + getConcept().getId() + " " + getConcept().getName());

    // Log for the molecular action report
    addLogEntry(getLastModifiedBy(), getProject().getId(),
        getMolecularAction().getId(), getActivityId(), getWorkId(),
        "\nACTION  " + getName() + "\n  concept = " + getConcept().getId() + " "
            + getConcept().getName());

  }

}
