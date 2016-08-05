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
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.ConceptRelationshipJpa;
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

    // Check preconditions
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
    final List<Atom> fromAtoms = new ArrayList<>(getFromConcept().getAtoms());
    final List<SemanticTypeComponent> fromStys =
        new CopyOnWriteArrayList<>(getFromConcept().getSemanticTypes());
    final List<ConceptRelationship> fromRelationships =
        new CopyOnWriteArrayList<>(getFromConcept().getRelationships());
    // Also copy the inverse relationships
    List<ConceptRelationship> inverseRelationships =
        new CopyOnWriteArrayList<ConceptRelationship>();
    for (final ConceptRelationship rel : fromRelationships) {
      inverseRelationships
          .add((ConceptRelationship) findInverseRelationship(rel));
    }

    //
    // Remove all objects from the fromConcept
    //
    for (final Atom atom : fromAtoms) {
      getFromConcept().getAtoms().remove(atom);
    }
    for (final SemanticTypeComponent sty : fromStys) {
      getFromConcept().getSemanticTypes().remove(sty);
    }
    for (final ConceptRelationship rel : fromRelationships) {
      getFromConcept().getRelationships().remove(rel);
    }
    for (final ConceptRelationship rel : inverseRelationships) {
      rel.getFrom().getRelationships().remove(rel);
      //Since getToConcept is its own object, keep its status up to date as well.
      if (rel.getFrom().getId().equals(getToConcept().getId())) {
        getToConcept().getRelationships().remove(rel);
      }
    }

    //
    // Update fromConcept and all concepts affected by relationship
    //
    updateConcept(getToConcept());
    updateConcept(getFromConcept());
    for (final ConceptRelationship rel : inverseRelationships) {
      updateConcept(rel.getFrom());
    }

    //
    // Remove the objects from the database
    //
    // Note: don't remove atoms - we just move them instead
    for (final SemanticTypeComponent sty : fromStys) {
      removeSemanticTypeComponent(sty.getId());
    }
    for (final ConceptRelationship rel : fromRelationships) {
      removeRelationship(rel.getId(), rel.getClass());
    }
    for (final ConceptRelationship rel : inverseRelationships) {
      removeRelationship(rel.getId(), rel.getClass());
    }

    //
    // Remove objects that won't be added to the toConcept
    //

    // Don't add semantic type if it already exists in toConcept
    for (SemanticTypeComponent sty : fromStys) {
      if (getToConcept().getSemanticTypes().contains(sty)) {
        fromStys.remove(sty);
      }
    }
    // Remove any relationship between from and to concept
    for (final ConceptRelationship rel : fromRelationships) {
      if (rel.getTo().getId().equals(getToConcept().getId())) {
        fromRelationships.remove(rel);
      }
    }
    for (final ConceptRelationship rel : inverseRelationships) {
      if (rel.getFrom().getId().equals(getToConcept().getId())) {
        inverseRelationships.remove(rel);
      }
    }
    // If a relationship exists between to and related concept that would get
    // overwritten,
    // don't add it in. Instead, keep copy of existing relationship to set to
    // Needs Review later.
    List<ConceptRelationship> existingRels = new ArrayList<>();

    for (final ConceptRelationship rel : fromRelationships) {
      for (final ConceptRelationship toRel : getToConcept()
          .getRelationships()) {
        if (rel.getTo().getId().equals(toRel.getTo().getId())) {
          fromRelationships.remove(rel);
          existingRels.add(toRel);
        }
      }
    }
    for (final ConceptRelationship rel : inverseRelationships) {
      for (final ConceptRelationship toRel : getToConcept()
          .getRelationships()) {
        ConceptRelationship toInverseRel =
            (ConceptRelationship) findInverseRelationship(toRel);
        if (rel.getFrom().getId().equals(toInverseRel.getFrom().getId())) {
          inverseRelationships.remove(rel);
          existingRels.add(toRel);
        }
      }
    }

    //
    // Create new component objects to be attached to the concepts
    //
    List<SemanticTypeComponent> newStys = new ArrayList<>();
    for (SemanticTypeComponent sty : fromStys) {
      sty.setId(null);
      newStys.add(addSemanticTypeComponent(sty, getToConcept()));
    }
    List<ConceptRelationship> newRels = new ArrayList<>();
    for (final ConceptRelationship rel : fromRelationships) {
      ConceptRelationship newRel = new ConceptRelationshipJpa(rel, false);
      newRel.setId(null);
      newRel.setFrom(getToConcept());
      newRels.add((ConceptRelationshipJpa) addRelationship(newRel));
    }
    List<ConceptRelationship> newInverseRels = new ArrayList<>();
    for (final ConceptRelationship rel : inverseRelationships) {
      ConceptRelationship newRel = new ConceptRelationshipJpa(rel, false);
      newRel.setId(null);
      newRel.setTo(getToConcept());
      newInverseRels.add((ConceptRelationshipJpa) addRelationship(newRel));
    }

    //
    // Change status of the components to be added
    //
    if (getChangeStatusFlag()) {
      for (final Atom atom : fromAtoms) {
        atom.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
      }
      for (SemanticTypeComponent sty : newStys) {
        sty.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
      }
      for (final ConceptRelationship rel : newRels) {
        rel.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
      }
      for (final ConceptRelationship rel : newInverseRels) {
        rel.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
      }
    }
    // Also don't forget to update the workflow status on all of the
    // pre-existing relationships that the merge would have duplicated
    for (ConceptRelationship rel : existingRels) {
      rel.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
    }

    //
    // Add the components to the concept (and related concepts)
    //
    for (final Atom atom : fromAtoms) {
      getConcept().getAtoms().add(atom);
    }
    for (SemanticTypeComponent sty : newStys) {
      getConcept().getSemanticTypes().add(sty);
    }
    for (final ConceptRelationship rel : newRels) {
      rel.getFrom().getRelationships().add(rel);
      //Since getToConcept is its own object, keep its status up to date as well.
      if (rel.getFrom().getId().equals(getToConcept().getId())) {
        getToConcept().getRelationships().remove(rel);
      }      
    }
    for (final ConceptRelationship rel : newInverseRels) {
      rel.getFrom().getRelationships().add(rel);
      //Since getToConcept is its own object, keep its status up to date as well.
      if (rel.getFrom().getId().equals(getToConcept().getId())) {
        getToConcept().getRelationships().remove(rel);
      }      
    }

    // Change status of the concept
    if (getChangeStatusFlag()) {
      getConcept().setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
    }

    //
    // update the to and from Concepts, and all concepts a relationship has been
    // added to
    //
    updateConcept(getToConcept());
    updateConcept(getFromConcept());
    for (final ConceptRelationship rel : newInverseRels) {
      updateConcept(rel.getFrom());
    }
    
    //
    // Delete the from concept
    //
    removeConcept(getFromConcept().getId());


    // log the REST calls
    addLogEntry(getUserName(), getProject().getId(), getToConcept().getId(),
        getActivityId(), getWorkId(), getName() + " concept " + getFromConcept().getId()
            + " into concept " + getToConcept().getId());

    // Make copy of toConcept to pass into change event
    toConceptPostUpdates = new ConceptJpa(getToConcept(), false);

  }

}
