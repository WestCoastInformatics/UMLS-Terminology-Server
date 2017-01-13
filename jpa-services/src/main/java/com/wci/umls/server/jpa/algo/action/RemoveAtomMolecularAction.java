/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.action;

import java.util.ArrayList;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.content.AtomRelationshipJpa;
import com.wci.umls.server.jpa.content.CodeJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.DescriptorJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.AtomRelationship;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.workflow.WorkflowStatus;

/**
 * A molecular action for removing an atom.
 */
public class RemoveAtomMolecularAction extends AbstractMolecularAction {

  /** The atom. */
  private Atom atom;

  /** The atom id. */
  private Long atomId;

  /**
   * Instantiates an empty {@link RemoveAtomMolecularAction}.
   *
   * @throws Exception the exception
   */
  public RemoveAtomMolecularAction() throws Exception {
    super();
    // n/a
  }

  /**
   * Returns the atom.
   *
   * @return the atom
   */
  public Atom getAtom() {
    return atom;
  }

  /**
   * Returns the atom id.
   *
   * @return the atom id
   */
  public Long getAtomId() {
    return atomId;
  }

  /**
   * Sets the atom id.
   *
   * @param atomId the atom id
   */
  public void setAtomId(Long atomId) {
    this.atomId = atomId;
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {
    final ValidationResult validationResult = new ValidationResultJpa();

    // Perform action specific validation - n/a

    // Exists check
    for (final Atom atm : getConcept().getAtoms()) {
      if (atm.getId().equals(atomId)) {
        atom = atm;
      }
    }
    if (atom == null) {
      rollback();
      throw new LocalException("Atom to remove does not exist");
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

    // Handle codeId, descriptorId, conceptId
    handleCode(atom);
    handleConcept(atom);
    handleDescriptor(atom);

    // If atom has any demotion relationships, remove it from atom, and remove
    // inverses from the other atoms
    for (AtomRelationship relationship : new ArrayList<>(
        atom.getRelationships())) {
      if (relationship.getWorkflowStatus().equals(WorkflowStatus.DEMOTION)) {
        atom.getRelationships().remove(relationship);

        final Atom relatedAtom = getAtom(relationship.getTo().getId());
        final AtomRelationship inverseDemotion =
            (AtomRelationship) getInverseRelationship(relationship);
        relatedAtom.getRelationships().remove(inverseDemotion);

        removeRelationship(relationship.getId(), AtomRelationshipJpa.class);
        removeRelationship(inverseDemotion.getId(), AtomRelationshipJpa.class);

        updateAtom(atom);
        updateAtom(relatedAtom);

      }
    }

    // Remove the atom from the concept
    removeById(getConcept().getAtoms(), atom.getId());

    // Update Concept
    updateConcept(getConcept());

    // Remove the atom
    removeAtom(atom.getId());

    // Change status of concept
    if (getChangeStatusFlag()) {
      getConcept().setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
    }
    // Update Concept
    updateConcept(getConcept());
  }

  /* see superclass */
  @Override
  public void logAction() throws Exception {

    // log the REST call
    addLogEntry(getLastModifiedBy(), getProject().getId(), getConcept().getId(),
        getActivityId(), getWorkId(),
        getName() + " from concept " + getConcept().getId() + " " + atom);

    // Log for the molecular action report
    addLogEntry(getLastModifiedBy(), getProject().getId(),
        getMolecularAction().getId(), getActivityId(), getWorkId(),
        "\nACTION  " + getName() + "\n  concept = " + getConcept().getId() + " "
            + getConcept().getName() + "\n  atom = " + atom.getName() + ", "
            + atom.getTerminology() + "/" + atom.getTermType() + ","
            + atom.getCodeId());

  }

  /**
   * Handle code.
   *
   * @param atom the atom
   * @throws Exception the exception
   */
  private void handleCode(Atom atom) throws Exception {
    Code code = getCode(atom.getCodeId(), atom.getTerminology(),
        atom.getVersion(), Branch.ROOT);
    if (code != null) {
      code = new CodeJpa(code, true);
      removeById(code.getAtoms(), atom.getId());
      updateCode(code);
    }
  }

  /**
   * Handle concept.
   *
   * @param atom the atom
   * @throws Exception the exception
   */
  private void handleConcept(Atom atom) throws Exception {
    Concept concept = getConcept(atom.getConceptId(), atom.getTerminology(),
        atom.getVersion(), Branch.ROOT);
    if (concept != null) {
      concept = new ConceptJpa(concept, true);
      removeById(concept.getAtoms(), atom.getId());
      updateConcept(concept);
    }
  }

  /**
   * Handle descriptor.
   *
   * @param atom the atom
   * @throws Exception the exception
   */
  private void handleDescriptor(Atom atom) throws Exception {
    Descriptor descriptor = getDescriptor(atom.getDescriptorId(),
        atom.getTerminology(), atom.getVersion(), Branch.ROOT);
    if (descriptor != null) {
      descriptor = new DescriptorJpa(descriptor, true);
      removeById(descriptor.getAtoms(), atom.getId());
      updateDescriptor(descriptor);
    }
  }
}
