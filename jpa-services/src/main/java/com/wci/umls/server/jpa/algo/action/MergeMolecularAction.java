/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.action;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.ConceptRelationshipJpa;
import com.wci.umls.server.jpa.content.SemanticTypeComponentJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.content.SemanticTypeComponent;
import com.wci.umls.server.model.workflow.WorkflowStatus;

/**
 * A molecular action for merging two concepts.
 */
public class MergeMolecularAction extends AbstractMolecularAction {

  /** The to concept pre updates. */
  private Concept toConceptPreUpdates;

  /** The to concept post updates. */
  private Concept toConceptPostUpdates;

  /** The from concept pre updates. */
  private Concept fromConceptPreUpdates;

  /**
   * Instantiates an empty {@link MergeMolecularAction}.
   *
   * @throws Exception the exception
   */
  public MergeMolecularAction() throws Exception {
    super();
    // n/a
  }

  /**
   * Returns the from concept.
   *
   * @return the from concept
   */
  public Concept getFromConcept() {
    return getConcept2();
  }

  /**
   * Returns the to concept.
   *
   * @return the to concept
   */
  public Concept getToConcept() {
    return getConcept();
  }

  /**
   * Returns the to concept pre updates.
   *
   * @return the to concept pre updates
   */
  public Concept getToConceptPreUpdates() {
    return toConceptPreUpdates;
  }

  /**
   * Returns the to concept post updates.
   *
   * @return the to concept post updates
   */
  public Concept getToConceptPostUpdates() {
    return toConceptPostUpdates;
  }

  /**
   * Returns the from concept pre updates.
   *
   * @return the from concept pre updates
   */
  public Concept getFromConceptPreUpdates() {
    return fromConceptPreUpdates;
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {
    final ValidationResult validationResult = new ValidationResultJpa();
    // Perform action specific validation - n/a

    // Metadata referential integrity checking

    // Check to make sure concepts are different
    if (getFromConcept() == getToConcept()) {
      throw new LocalException(
          "Cannot merge concept " + getFromConcept().getId() + " with concept "
              + getToConcept().getId() + " - identical concept.");
    }

    // Merging concepts must be from the same terminology
    if (!(getFromConcept().getTerminology().toString()
        .equals(getToConcept().getTerminology().toString()))) {
      throw new LocalException(
          "Two concepts must be from the same terminology to be merged, but concept "
              + getFromConcept().getId() + " has terminology "
              + getFromConcept().getTerminology() + ", and Concept "
              + getToConcept().getId() + " has terminology "
              + getToConcept().getTerminology());
    }

    // Check superclass validation
    validationResult.merge(super.checkPreconditions());
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

    // Make copy of toConcept and fromConcept before changes, to pass into
    // change event
    toConceptPreUpdates = new ConceptJpa(getToConcept(), false);
    fromConceptPreUpdates = new ConceptJpa(getFromConcept(), false);

    //
    // Make a copy of each object in the fromConcept
    //
    List<Atom> fromAtomsCopies = new ArrayList<>();
    for (final Atom atom : getFromConcept().getAtoms()) {
      fromAtomsCopies.add(new AtomJpa(atom));
    }

    List<SemanticTypeComponent> fromStysCopies = new CopyOnWriteArrayList<>();
    for (final SemanticTypeComponent sty : getFromConcept()
        .getSemanticTypes()) {
      fromStysCopies.add(new SemanticTypeComponentJpa(sty));
    }

    List<ConceptRelationship> fromRelationshipsCopies =
        new CopyOnWriteArrayList<>();
    List<ConceptRelationship> inverseRelationshipsCopies =
        new CopyOnWriteArrayList<>();
    for (final ConceptRelationship rel : getFromConcept().getRelationships()) {
      fromRelationshipsCopies.add(new ConceptRelationshipJpa(rel, false));
    }
    // Also make copies of the inverse relationships
    for (final ConceptRelationship rel : getFromConcept().getRelationships()) {
      inverseRelationshipsCopies.add(new ConceptRelationshipJpa(
          (ConceptRelationship) findInverseRelationship(rel), false));
    }

    //
    // Remove all objects from the fromConcept
    //
    for (final Atom atom : fromAtomsCopies) {
      getFromConcept().getAtoms().remove(atom);
    }
    for (final SemanticTypeComponent sty : fromStysCopies) {
      getFromConcept().getSemanticTypes().remove(sty);
    }
    for (final ConceptRelationship rel : fromRelationshipsCopies) {
      getFromConcept().getRelationships().remove(rel);
    }
    for (final ConceptRelationship rel : inverseRelationshipsCopies) {
      rel.getFrom().getRelationships().remove(rel);
      // rel.getFrom() can reference the same concept as getToConcept
      // But since getToConept is its own object, remove from there as well to
      // keep its status up to date as well.
      if (rel.getFrom().getId().equals(getToConcept().getId())) {
        getToConcept().getRelationships().remove(rel);
      }
    }

    //
    // Update fromConcept and all concepts a relationship has been removed from
    //
    updateConcept(getToConcept());
    updateConcept(getFromConcept());
    for (final ConceptRelationship rel : inverseRelationshipsCopies) {
      updateConcept(rel.getFrom());
    }

    //
    // Remove the objects from the database
    //
    // Note: don't remove atoms - we just move them instead
    for (final SemanticTypeComponent sty : fromStysCopies) {
      removeSemanticTypeComponent(sty.getId());
    }
    for (final ConceptRelationship rel : fromRelationshipsCopies) {
      removeRelationship(rel.getId(), rel.getClass());
    }
    for (final ConceptRelationship rel : inverseRelationshipsCopies) {
      removeRelationship(rel.getId(), rel.getClass());
    }

    //
    // Remove objects that won't be added to the toConcept
    //

    // Don't add semantic type if it already exists in toConcept
    for (SemanticTypeComponent sty : fromStysCopies) {
      if (getToConcept().getSemanticTypes().contains(sty)) {
        fromStysCopies.remove(sty);
      }
    }
    // Remove any relationship between from and to concept
    for (final ConceptRelationship rel : fromRelationshipsCopies) {
      if (rel.getTo().getId().equals(getToConcept().getId())) {
        fromRelationshipsCopies.remove(rel);
      }
    }
    for (final ConceptRelationship rel : inverseRelationshipsCopies) {
      if (rel.getFrom().getId().equals(getToConcept().getId())) {
        inverseRelationshipsCopies.remove(rel);
      }
    }
    // If a relationship exists between to and related concept that would get
    // overwritten,
    // don't add it in. Instead, keep copy of existing relationship to set to
    // Needs Review later.
    List<ConceptRelationship> existingRels = new ArrayList<>();

    for (final ConceptRelationship rel : fromRelationshipsCopies) {
      for (final ConceptRelationship toRel : getToConcept()
          .getRelationships()) {
        if (rel.getTo().getId().equals(toRel.getTo().getId())) {
          fromRelationshipsCopies.remove(rel);
          existingRels.add(toRel);
        }
      }
    }
    for (final ConceptRelationship rel : inverseRelationshipsCopies) {
      for (final ConceptRelationship toRel : getToConcept()
          .getRelationships()) {
        ConceptRelationship toInverseRel =
            (ConceptRelationship) findInverseRelationship(toRel);
        if (rel.getFrom().getId().equals(toInverseRel.getFrom().getId())) {
          inverseRelationshipsCopies.remove(rel);
          existingRels.add(toRel);
        }
      }
    }

    //
    // Prepare copies of component objects to be created into new objects
    //
    for (SemanticTypeComponent sty : fromStysCopies) {
      sty.setId(null);
    }
    for (final ConceptRelationship rel : fromRelationshipsCopies) {
      rel.setId(null);
      rel.setFrom(getToConcept());
    }
    for (final ConceptRelationship rel : inverseRelationshipsCopies) {
      rel.setId(null);
      rel.setTo(getToConcept());
    }

    //
    // Change status of the components to be added
    //
    if (getChangeStatusFlag()) {
      for (final Atom atom : fromAtomsCopies) {
        atom.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
      }
      for (SemanticTypeComponent sty : fromStysCopies) {
        sty.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
      }
      for (final ConceptRelationship rel : fromRelationshipsCopies) {
        rel.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
      }
      for (final ConceptRelationship rel : inverseRelationshipsCopies) {
        rel.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
      }
    }
    // Also don't forget to update the workflow status on all of the
    // pre-existing relationships that the merge would have duplicated
    for (ConceptRelationship rel : existingRels) {
      rel.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
    }

    //
    // Create the new components to be added, and update modified objects
    //
    for (final Atom atom : fromAtomsCopies) {
      updateAtom(atom);
    }
    List<SemanticTypeComponent> newStys = new ArrayList<>();
    for (SemanticTypeComponent sty : fromStysCopies) {
      newStys.add(addSemanticTypeComponent(sty, getToConcept()));
    }
    List<ConceptRelationship> newRels = new ArrayList<>();
    for (final ConceptRelationship rel : fromRelationshipsCopies) {
      newRels.add((ConceptRelationshipJpa) addRelationship(rel));
    }
    List<ConceptRelationship> newInverseRels = new ArrayList<>();
    for (final ConceptRelationship rel : inverseRelationshipsCopies) {
      newInverseRels.add((ConceptRelationshipJpa) addRelationship(rel));
    }
    for (ConceptRelationship rel : existingRels) {
      updateRelationship(rel);
    }

    //
    // Add the components to the concept (and related concepts)
    //
    for (final Atom atom : fromAtomsCopies) {
      getToConcept().getAtoms().add(atom);
    }
    for (SemanticTypeComponent sty : newStys) {
      getToConcept().getSemanticTypes().add(sty);
    }
    for (final ConceptRelationship rel : newRels) {
      getToConcept().getRelationships().add(rel);
    }
    for (final ConceptRelationship rel : newInverseRels) {
      rel.getFrom().getRelationships().add(rel);
    }

    //
    // Change status of the concept
    //
    if (getChangeStatusFlag()) {
      getToConcept().setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
    }

    //
    // update the to and from Concepts, and all concepts a relationship has been
    // added to
    //
    updateConcept(getToConcept());
    updateConcept(getFromConcept());
    for (final ConceptRelationship rel : newInverseRels) {
      if (!rel.getFrom().getId().equals(getToConcept().getId())
          && !rel.getFrom().getId().equals(getFromConcept().getId())) {
        updateConcept(rel.getFrom());
      }
    }

    //
    // Delete the from concept
    //
    removeConcept(getFromConcept().getId());

    // log the REST calls
    addLogEntry(getUserName(), getProject().getId(), getFromConcept().getId(),
        getActivityId(), getWorkId(),
        getName() + " concept " + getFromConcept().getId() + " into concept "
            + getToConcept().getId());
    addLogEntry(getUserName(), getProject().getId(), getToConcept().getId(),
        getActivityId(), getWorkId(),
        getName() + " concept " + getToConcept().getId() + " from concept "
            + getFromConcept().getId());

    // Make copy of toConcept to pass into change event
    toConceptPostUpdates = new ConceptJpa(getToConcept(), false);

  }

}
