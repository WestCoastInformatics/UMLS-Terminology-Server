/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.action;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.content.LexicalClassJpa;
import com.wci.umls.server.jpa.content.StringClassJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.LexicalClass;
import com.wci.umls.server.model.content.StringClass;
import com.wci.umls.server.model.workflow.WorkflowStatus;
import com.wci.umls.server.services.handlers.IdentifierAssignmentHandler;

/**
 * A molecular action for adding an atom.
 */
public class AddAtomMolecularAction extends AbstractMolecularAction {

  /** The atom. */
  private Atom atom;

  /**
   * Instantiates an empty {@link AddAtomMolecularAction}.
   *
   * @throws Exception the exception
   */
  public AddAtomMolecularAction() throws Exception {
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
   * Sets the atom.
   *
   * @param atom the atom
   */
  public void setAtom(Atom atom) {
    this.atom = atom;
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {
    final ValidationResult validationResult = new ValidationResultJpa();
    // Perform "adding an atom" specific validation - n/a

    // Metadata referential integrity checking
    if (getTermType(atom.getTermType(), getConcept().getTerminology(),
        getConcept().getVersion()) == null) {
      rollback();
      throw new LocalException(
          "Cannot add atom with invalid term type - " + atom.getTermType());
    }
    if (getLanguage(atom.getLanguage(), getConcept().getTerminology(),
        getConcept().getVersion()) == null) {
      rollback();
      throw new LocalException(
          "Cannot add atom with invalid language - " + atom.getLanguage());
    }
    if (getTerminology(atom.getTerminology(), atom.getVersion()) == null) {
      rollback();
      throw new LocalException("Cannot add atom with invalid terminology - "
          + atom.getTerminology() + ", version: " + atom.getVersion());
    }

    // Duplicate check
    for (final Atom a : getConcept().getAtoms()) {
      if (a.getName().equals(atom.getName())) {
        rollback();
        throw new LocalException("Duplicate atom - " + atom.getName());
      }
    }

    // Check preconditions
    validationResult.merge(super.checkPreconditions());
    validationResult.merge(super.validateAtom(getProject(), getAtom()));
    return validationResult;
  }

  /* see superclass */
  @Override
  public void compute() throws Exception {
    //
    // Perform the "adding an atom" (contentService will create atomic "adding
    // an atom"s for CRUD
    // operations)
    //

    // Assign alternateTerminologyId
    final IdentifierAssignmentHandler handler =
        getIdentifierAssignmentHandler(getConcept().getTerminology());

    // Add string and lexical classes to get assign their Ids
    final StringClass strClass = new StringClassJpa();
    strClass.setLanguage(atom.getLanguage());
    strClass.setName(atom.getName());
    atom.setStringClassId(handler.getTerminologyId(strClass));

    // Get normalization handler
    final LexicalClass lexClass = new LexicalClassJpa();
    lexClass.setLanguage(atom.getLanguage());
    lexClass.setNormalizedName(getNormalizedString(atom.getName()));
    atom.setLexicalClassId(handler.getTerminologyId(lexClass));

    final String altId = handler.getTerminologyId(atom);
    atom.getAlternateTerminologyIds().put(getConcept().getTerminology(), altId);

    // Change status of the atom
    if (getChangeStatusFlag()) {
      atom.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
    }

    // Add the atom
    atom = addAtom(atom);

    // Change status of the concept
    if (getChangeStatusFlag()) {
      getConcept().setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
    }

    // Add the atom to concept
    getConcept().getAtoms().add(atom);

    // update the concept
    updateConcept(getConcept());

    // log the REST call
    addLogEntry(getLastModifiedBy(), getProject().getId(), getConcept().getId(),
        getActivityId(), getWorkId(),
        getName() + " to concept " + getConcept().getId() + " " + atom);

    addLogEntry(getLastModifiedBy(), getProject().getId(),
        getMolecularAction().getId(), getActivityId(), getWorkId(),
        "\nACTION  " + getName() + "\n  concept = " + getConcept().getId() + " " + getConcept().getName() +
        "\n  atom id = " + getAtom().getId() +
        "\n  terminology = " + getTerminology() +
        "\n  version = " + getVersion());
  }

}
