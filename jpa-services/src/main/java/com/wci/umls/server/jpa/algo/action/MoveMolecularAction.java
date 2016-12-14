/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.action;

import java.util.ArrayList;
import java.util.List;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.AtomRelationshipJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.AtomRelationship;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.workflow.WorkflowStatus;

/**
 * A molecular action for moving atoms from one concept to another.
 */
public class MoveMolecularAction extends AbstractMolecularAction {

  /** The atom ids. */
  private List<Long> atomIds;

  /** The move atoms. */
  private List<Atom> moveAtoms;

  /**
   * Instantiates an empty {@link MoveMolecularAction}.
   *
   * @throws Exception the exception
   */
  public MoveMolecularAction() throws Exception {
    super();
    // n/a
  }

  /**
   * Sets the atom ids.
   *
   * @param atomIds the atom ids
   */
  public void setAtomIds(List<Long> atomIds) {
    this.atomIds = atomIds;
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

  /**
   * Returns the move atoms.
   *
   * @return the move atoms
   */
  public List<Atom> getMoveAtoms() {
    return moveAtoms;
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {
    final ValidationResult validationResult = new ValidationResultJpa();
    // Perform action specific validation - n/a

    // Metadata referential integrity checking

    // Same concept check
    if (getFromConcept() == getToConcept()) {
      throw new LocalException("Cannot move atoms from concept "
          + getFromConcept().getId() + " to concept " + getToConcept().getId()
          + " - identical concept.");
    }

    // Moving concepts must be from the same terminology
    if (!(getFromConcept().getTerminology().toString()
        .equals(getToConcept().getTerminology().toString()))) {
      throw new LocalException(
          "Two concepts must be from the same terminology to have atoms moved between them, but concept "
              + getFromConcept().getId() + " has terminology "
              + getFromConcept().getTerminology() + ", and Concept "
              + getToConcept().getId() + " has terminology "
              + getToConcept().getTerminology());
    }

    // Populate move-atom list, and exists check
    moveAtoms = new ArrayList<Atom>();
    for (final Atom atm : getFromConcept().getAtoms()) {
      if (atomIds.contains(atm.getId())) {
        moveAtoms.add(atm);
      }
    }

    if (!(moveAtoms.size() == atomIds.size())) {
      throw new LocalException("Atom to move not found on from Concept");
    }

    // Check preconditions
    validationResult.merge(super.checkPreconditions());
    return validationResult;
  }

  /* see superclass */
  @Override
  public void compute() throws Exception {
    //
    // Perform the action (contentService will create atomic actions for CRUD
    // operations)
    //

    //
    // Make a copy of the atoms to be moved
    // If any of these atom has a demotion to the "to" concept, remove it,
    // update the atom, and create a copy for later deletion.
    List<Atom> moveAtomsList = moveAtoms;
    List<Atom> moveAtomsCopies = new ArrayList<>();
    final List<AtomRelationship> demotionCopies = new ArrayList<>();
    for (final Atom atom : moveAtoms) {
      Atom atomCopy = new AtomJpa(atom, true);
      moveAtomsCopies.add(atomCopy);
      for (final AtomRelationship atomRel : new ArrayList<AtomRelationship>(
          atom.getRelationships())) {
        if (atomRel.getWorkflowStatus().equals(WorkflowStatus.DEMOTION)
            && getToConcept().getAtoms().contains(atomRel.getTo())) {
          atomCopy.getRelationships().remove(atomRel);
          updateAtom(atomCopy);
          demotionCopies.add(new AtomRelationshipJpa(atomRel, false));
        }
      }
    }

    // If any atom in the toConcept has a demotion to any of the move atoms in
    // "from" concept, remove it, update the atom, and create a copy of the
    // demotion for later deletion.
    for (final Atom atom : getToConcept().getAtoms()) {
      for (final AtomRelationship atomRel : new ArrayList<AtomRelationship>(
          atom.getRelationships())) {
        if (atomRel.getWorkflowStatus().equals(WorkflowStatus.DEMOTION)
            && moveAtoms.contains(atomRel.getTo())) {
          Atom atomCopy = new AtomJpa(atom, true);
          atomCopy.getRelationships().remove(atomRel);
          updateAtom(atomCopy);
          demotionCopies.add(new AtomRelationshipJpa(atomRel, false));
        }
      }
    }

    //
    // Remove all atoms from the fromConcept
    //
    for (final Atom atom : moveAtomsList) {
      removeById(getFromConcept().getAtoms(), atom.getId());
    }

    //
    // Update fromConcept
    //
    updateConcept(getFromConcept());

    //
    // Remove the objects from the database
    //

    // Remove demotions between atoms that will be both in the "to" concept
    for (final AtomRelationship rel : demotionCopies) {
      removeRelationship(rel.getId(), rel.getClass());
    }

    //
    // Change status of the atoms to be added
    //
    if (getChangeStatusFlag()) {
      for (final Atom atom : moveAtomsCopies) {
        atom.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
        updateAtom(atom);
      }
    }

    //
    // Add the atoms to the toConcept
    //
    for (final Atom atom : moveAtomsList) {
      getToConcept().getAtoms().add(atom);
    }

    //
    // Change status of the concepts
    //
    if (getChangeStatusFlag()) {
      getFromConcept().setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
      getToConcept().setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
    }

    //
    // Update the to and from Concepts
    //
    updateConcept(getToConcept());
    updateConcept(getFromConcept());

  }

  /* see superclass */
  @Override
  public void logAction() throws Exception {

    // log the REST calls
    addLogEntry(getLastModifiedBy(), getProject().getId(),
        getFromConcept().getId(), getActivityId(), getWorkId(),
        getName() + " " + atomIds + " from Concept " + getFromConcept().getId()
            + " to concept " + getToConcept().getId());
    addLogEntry(getLastModifiedBy(), getProject().getId(),
        getToConcept().getId(), getActivityId(), getWorkId(),
        getName() + " " + atomIds + " to Concept " + getToConcept().getId()
            + " from concept " + getFromConcept().getId());

    // Log for the molecular action report
    final StringBuilder sb = new StringBuilder();
    sb.append("\n  move atoms =");
    for (final Atom atom : getMoveAtoms()) {
      sb.append(("\n    " + atom.getName() + ", " + atom.getTerminology() + "/"
          + atom.getTermType() + "," + atom.getCodeId()));
    }
    addLogEntry(getLastModifiedBy(), getProject().getId(),
        getMolecularAction().getId(), getActivityId(), getWorkId(),
        "\nACTION  " + getName() + "\n  concept (from) = "
            + getFromConcept().getId() + " " + getFromConcept().getName()
            + (getToConcept() != null ? "\n  concept2 (to) = "
                + getToConcept().getId() + " " + getToConcept().getName() : "")
            + sb);

  }

}
