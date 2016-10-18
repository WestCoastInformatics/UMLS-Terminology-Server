/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.ConceptRelationshipJpa;
import com.wci.umls.server.jpa.content.SemanticTypeComponentJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.content.Relationship;
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
  @SuppressWarnings("rawtypes")
  /* see superclass */
  @Override
  public void compute() throws Exception {
    //
    // Perform the action (contentService will create atomic actions for CRUD
    // operations)
    //

    // Get all "inverse" relationships for the "to" and "from" concepts (e.g.
    // where toId is the id)   
    final Map<Long, ConceptRelationship> inverseFromRelsMap = new HashMap<>();
    final Map<Long, ConceptRelationship> inverseToRelsMap = new HashMap<>();
    for (final Relationship rel : findConceptRelationships(null,
        getTerminology(), getVersion(), Branch.ROOT,
        "toId:" + getFromConcept().getId(), false, null).getObjects()) {
      final ConceptRelationship crel =
          new ConceptRelationshipJpa((ConceptRelationship) rel, false);
      if (inverseFromRelsMap.containsKey(crel.getFrom().getId())) {
        throw new Exception("Multiple concept level relationships from "
            + crel.getFrom().getId());
      }
      inverseFromRelsMap.put(crel.getFrom().getId(), crel);
    }
    for (final Relationship rel : findConceptRelationships(null,
        getTerminology(), getVersion(), Branch.ROOT,
        "toId:" + getToConcept().getId(), false, null).getObjects()) {
      final ConceptRelationship crel =
          new ConceptRelationshipJpa((ConceptRelationship) rel, false);
      if (inverseToRelsMap.containsKey(crel.getFrom().getId())) {
        throw new Exception("Multiple concept level relationships from "
            + crel.getFrom().getId());
      }
      inverseToRelsMap.put(crel.getTo().getId(), crel);
    }

    // Copy atoms in "from" concept
    final List<Atom> fromAtomsCopies = new ArrayList<>();
    for (final Atom atom : getFromConcept().getAtoms()) {
      fromAtomsCopies.add(new AtomJpa(atom));
    }

    // Copy stys in "from" concept
    final List<SemanticTypeComponent> fromStysCopies = new ArrayList<>();
    for (final SemanticTypeComponent sty : getFromConcept()
        .getSemanticTypes()) {
      fromStysCopies.add(new SemanticTypeComponentJpa(sty));
    }

    // Copy rels in "from" concept
    final List<ConceptRelationship> fromRelCopies = new ArrayList<>();
    for (final ConceptRelationship rel : getFromConcept().getRelationships()) {
      fromRelCopies.add(new ConceptRelationshipJpa(rel, true));
    }

    // Prep a list of concepts to update after object removal
    final Set<Concept> conceptsChanged = new HashSet<>();
    conceptsChanged.add(getFromConcept());
    conceptsChanged.add(getToConcept());

    //
    // Remove all objects from the fromConcept
    //
    getFromConcept().getAtoms().clear();
    getFromConcept().getSemanticTypes().clear();
    getFromConcept().getRelationships().clear();

    // Remove the inverse "from" relationships
    for (final ConceptRelationship rel : inverseFromRelsMap.values()) {
      // If this is a rel between "from" and "to" remove it
      if (rel.getFrom().getId().equals(getToConcept().getId())) {
        getToConcept().getRelationships().remove(rel);
      }
      // Otherwise remove it from the concept on the other end of the
      // relationship
      else {
        final Concept inverseConcept = new ConceptJpa(rel.getFrom(), true);
        inverseConcept.getRelationships().remove(rel);
        conceptsChanged.add(inverseConcept);
      }
    }

    //
    // Update all the concepts changed by the above section
    //
    for (final Concept inverseConcept : conceptsChanged) {
      updateConcept(inverseConcept);
    }

    //
    // Remove objects
    //

    // Remove the "from" semantic types
    for (final SemanticTypeComponent sty : fromStysCopies) {
      removeSemanticTypeComponent(sty.getId());
    }
    // Remove the "from" relationships
    for (final ConceptRelationship rel : fromRelCopies) {
      removeRelationship(rel.getId(), rel.getClass());
    }
    // Remove the inverses of the "from" relationships
    for (final ConceptRelationship rel : inverseFromRelsMap.values()) {
      removeRelationship(rel.getId(), rel.getClass());
    }

    // Note: don't remove atoms - we just move them instead

    //
    // If both "from" and "to" have relationships to the same third concept,
    // and the changeStatus flag is set, mark the "to" relationship as needs
    // review
    // and as per above the "from" relationship will be removed.
    //
    if (getChangeStatusFlag()) {
      for (final ConceptRelationship toRel : getToConcept()
          .getRelationships()) {
        if (inverseFromRelsMap.containsKey(toRel.getTo().getId())) {
          toRel.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
          updateRelationship(toRel);
        }
      }

      for (final ConceptRelationship inverseToRel : inverseToRelsMap.values()) {
        if (inverseFromRelsMap.containsKey(inverseToRel.getFrom().getId())) {
          inverseToRel.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
          updateRelationship(inverseToRel);
        }
      }
    }

    //
    // Update and add objects
    //
    conceptsChanged.clear();
    conceptsChanged.add(getToConcept());

    // Set workflow status of "from" atoms and add to the "to" concept.
    for (final Atom atom : fromAtomsCopies) {
      if (getChangeStatusFlag()) {
        atom.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
        updateAtom(atom);
      }
      getToConcept().getAtoms().add(atom);
    }

    // Add new semantic types and wire them to "to" concept (unless they match)
    final Set<String> toConceptStys = getToConcept().getSemanticTypes().stream()
        .map(sty -> sty.getSemanticType()).collect(Collectors.toSet());
    for (SemanticTypeComponent sty : fromStysCopies) {
      // Only create semantic type if it already exists in toConcept
      if (!toConceptStys.contains(sty.getSemanticType())) {
        sty.setId(null);
        if (getChangeStatusFlag()) {
          sty.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
        }
        final SemanticTypeComponent newSty =
            addSemanticTypeComponent(sty, getToConcept());
        getToConcept().getSemanticTypes().add(newSty);
      }
    }

    // update "from" concept relationships that do not match "to" concept
    // relationships.
    for (final ConceptRelationship rel : fromRelCopies) {

      // Only copy over relationship if
      // It's NOT between from and to concept, and
      // it won't overwrite an existing relationship.
      if (!rel.getTo().getId().equals(getToConcept().getId())
          && inverseToRelsMap.containsKey(rel.getTo().getId())) {
        rel.setId(null);
        rel.setFrom(getToConcept());
        if (getChangeStatusFlag()) {
          rel.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
        }
        addRelationship(rel);
        getToConcept().getRelationships().add(rel);
      }
    }

    // Corresponding logic for inverse rels
    for (final ConceptRelationship rel : inverseFromRelsMap.values()) {
      // Only copy over relationship if
      // It's NOT between from and to concept, and
      // it won't overwrite an existing relationship.
      if (!rel.getFrom().getId().equals(getToConcept().getId())
          && inverseToRelsMap.containsKey(rel.getFrom().getId())) {
        rel.setId(null);
        rel.setTo(getToConcept());
        if (getChangeStatusFlag()) {
          rel.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
        }
        addRelationship(rel);
        final Concept concept = new ConceptJpa(rel.getFrom(), true);
        concept.getRelationships().add(rel);
        conceptsChanged.add(concept);
      }
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
    for (final Concept concept : conceptsChanged) {
      updateConcept(concept);
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
