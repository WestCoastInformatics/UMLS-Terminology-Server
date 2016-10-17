/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.action;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    return getConcept();
  }

  /**
   * Returns the to concept.
   *
   * @return the to concept
   */
  public Concept getToConcept() {
    return getConcept2();
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
          "Cannot merge concept " + getFromConcept().getId() + " into concept "
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
    getFromConcept().getAtoms().clear();
    getFromConcept().getSemanticTypes().clear();
    getFromConcept().getRelationships().clear();

    List<Concept> inverseConceptList = new ArrayList<>();
    for (final ConceptRelationship rel : inverseRelationshipsCopies) {
      // rel.getFrom() can reference the same concept as getToConcept
      // If so, remove it from there. Otherwise, remove from the concept pulled
      // from the relationship
      if (rel.getFrom().getId().equals(getToConcept().getId())) {
        getToConcept().getRelationships().remove(rel);
      } else {
        final Concept inverseConcept = new ConceptJpa(rel.getFrom(), true);
        inverseConcept.getRelationships().remove(rel);
        inverseConceptList.add(inverseConcept);
      }
    }

    //
    // Update fromConcept and all concepts a relationship has been removed from
    //
    updateConcept(getToConcept());
    updateConcept(getFromConcept());
    for (final Concept inverseConcept : inverseConceptList) {
      if (!inverseConcept.getId().equals(getToConcept().getId())
          && !inverseConcept.equals(getFromConcept().getId())) {
        updateConcept(inverseConcept);
      }
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

    // If a relationship exists between to and related concept that would get
    // overwritten, don't add it in. Instead, keep copy of existing relationship
    // to set to Needs Review later.
    List<ConceptRelationship> existingRels = new ArrayList<>();
    Set<Long> existingRelsIds = new HashSet<>();

    for (final ConceptRelationship rel : new ArrayList<>(
        fromRelationshipsCopies)) {
      for (final ConceptRelationship toRel : getToConcept()
          .getRelationships()) {
        if (rel.getTo().getId().equals(toRel.getTo().getId())) {
          existingRels.add(toRel);
          existingRelsIds.add(toRel.getId());
        }
      }
    }

    for (final ConceptRelationship rel : new ArrayList<>(
        inverseRelationshipsCopies)) {
      for (final ConceptRelationship toRel : getToConcept()
          .getRelationships()) {
        ConceptRelationship toInverseRel =
            (ConceptRelationship) findInverseRelationship(toRel);
        if (rel.getFrom().getId().equals(toInverseRel.getFrom().getId())) {
          existingRels.add(toRel);
          existingRelsIds.add(toRel.getId());
        }
      }
    }

    //
    // Create the new components to be added, and update modified objects
    //
    for (final Atom atom : fromAtomsCopies) {
      atom.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
      updateAtom(atom);
    }
    List<SemanticTypeComponent> newStys = new ArrayList<>();
    for (SemanticTypeComponent sty : fromStysCopies) {
      // Only create semantic type if it already exists in toConcept
      if (!getToConcept().getSemanticTypes().contains(sty)) {
        sty.setId(null);
        sty.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
        newStys.add(addSemanticTypeComponent(sty, getToConcept()));
      }
    }
    List<ConceptRelationship> newRels = new ArrayList<>();
    for (final ConceptRelationship rel : fromRelationshipsCopies) {

      // Only copy over relationship if
      // It's NOT between from and to concept, and
      // it won't overwrite an existing relationship.
      if (!rel.getTo().getId().equals(getToConcept().getId())
          && !existingRelsIds.contains(rel.getId())) {
        rel.setId(null);
        rel.setFrom(getToConcept());
        rel.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
        newRels.add((ConceptRelationshipJpa) addRelationship(rel));
      }
    }
    List<ConceptRelationship> newInverseRels = new ArrayList<>();
    for (final ConceptRelationship rel : inverseRelationshipsCopies) {

      // Only copy over relationship if
      // It's NOT between from and to concept, and
      // it won't overwrite an existing relationship.
      if (!rel.getFrom().getId().equals(getToConcept().getId())
          && !existingRelsIds.contains(rel.getId())) {
        rel.setId(null);
        rel.setFrom(getToConcept());
        rel.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
        newInverseRels.add((ConceptRelationshipJpa) addRelationship(rel));
      }
    }
    for (ConceptRelationship rel : existingRels) {
      rel.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
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
    inverseConceptList = new ArrayList<>();
    for (final ConceptRelationship rel : newInverseRels) {
      Concept concept = new ConceptJpa(rel.getFrom(), true);
      concept.getRelationships().add(rel);
      inverseConceptList.add(concept);
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
    for (final Concept inverseConcept : inverseConceptList) {
      if (!inverseConcept.getId().equals(getToConcept().getId())
          && !inverseConcept.equals(getFromConcept().getId())) {
        updateConcept(inverseConcept);
      }
    }

    //
    // Delete the from concept
    //
    removeConcept(getFromConcept().getId());

    // log the REST calls
    addLogEntry(getLastModifiedBy(), getProject().getId(),
        getFromConcept().getId(), getActivityId(), getWorkId(),
        getName() + " concept " + getFromConcept().getId() + " into concept "
            + getToConcept().getId());
    addLogEntry(getLastModifiedBy(), getProject().getId(),
        getToConcept().getId(), getActivityId(), getWorkId(),
        getName() + " concept " + getToConcept().getId() + " from concept "
            + getFromConcept().getId());

    // Log for the molecular action report
    addLogEntry(getLastModifiedBy(), getProject().getId(),
        getMolecularAction().getId(), getActivityId(), getWorkId(),
        "\nACTION  " + getName() + "\n  concept (from) = "
            + getFromConcept().getId() + " " + getFromConcept().getName()
            + (getToConcept() != null ? "\n  concept (to) = "
                + getToConcept().getId() + " " + getToConcept().getName()
                : ""));

  }

  /* see superclass */
  @Override
  public boolean lockRelatedConcepts() {
    return true;
  }

}
