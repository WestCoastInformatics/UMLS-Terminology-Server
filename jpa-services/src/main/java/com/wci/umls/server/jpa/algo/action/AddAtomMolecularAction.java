/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.action;

import java.util.Date;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.content.CodeJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.DescriptorJpa;
import com.wci.umls.server.jpa.content.LexicalClassJpa;
import com.wci.umls.server.jpa.content.StringClassJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Descriptor;
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
    validationResult.merge(
        super.validateAtom(getProject().getValidationChecks(), getAtom()));
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

    // Handle codeId, descriptorId, conceptId
    handleCode(atom);
    handleConcept(atom);
    handleDescriptor(atom);

    // Add the atom to concept
    getConcept().getAtoms().add(atom);

    // update the concept
    updateConcept(getConcept());
  }

  /* see superclass */
  @Override
  public void logAction() throws Exception {

    // log the REST call
    addLogEntry(getLastModifiedBy(), getProject().getId(), getConcept().getId(),
        getActivityId(), getWorkId(),
        getName() + " to concept " + getConcept().getId() + " " + atom);

    // Log for the molecular action report
    addLogEntry(getLastModifiedBy(), getProject().getId(),
        getMolecularAction().getId(), getActivityId(), getWorkId(),
        "\nACTION  " + getName() + "\n  concept = " + getConcept().getId() + " "
            + getConcept().getName() + "\n  atom = " + getAtom().getName()
            + ", " + atom.getTerminology() + "/" + atom.getTermType() + ","
            + atom.getCodeId());
  }

  /**
   * Handle code.
   *
   * @param atom the atom
   * @throws Exception
   */
  private void handleCode(Atom atom) throws Exception {
    if (!ConfigUtility.isEmpty(atom.getCodeId())) {
      Code code = getCode(atom.getCodeId(), atom.getTerminology(),
          atom.getVersion(), Branch.ROOT);
      if (code == null) {
        code = new CodeJpa();
        code.setName(atom.getName());
        code.setObsolete(atom.isObsolete());
        code.setSuppressible(atom.isSuppressible());
        code.setPublishable(atom.isPublishable());
        code.setPublished(false);
        code.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);
        code.setTimestamp(new Date());
        code.setTerminology(atom.getTerminology());
        code.setVersion(atom.getVersion());
        code.setTerminologyId(atom.getCodeId());
        code.getAtoms().add(atom);
        code = addCode(code);
      } else {
        code = new CodeJpa(code, true);
        code.getAtoms().add(atom);
        updateCode(code);
      }
    }
  }

  /**
   * Handle concept.
   *
   * @param atom the atom
   * @throws Exception the exception
   */
  private void handleConcept(Atom atom) throws Exception {
    if (!ConfigUtility.isEmpty(atom.getConceptId())) {
      Concept concept = getConcept(atom.getConceptId(), atom.getTerminology(),
          atom.getVersion(), Branch.ROOT);
      if (concept == null) {
        concept = new ConceptJpa();
        concept.setName(atom.getName());
        concept.setObsolete(atom.isObsolete());
        concept.setSuppressible(atom.isSuppressible());
        concept.setPublishable(atom.isPublishable());
        concept.setPublished(false);
        concept.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);
        concept.setTimestamp(new Date());
        concept.setTerminology(atom.getTerminology());
        concept.setVersion(atom.getVersion());
        concept.getAtoms().add(atom);
        concept.setTerminologyId(atom.getConceptId());
        concept = addConcept(concept);
      } else {
        concept = new ConceptJpa(concept, true);
        concept.getAtoms().add(atom);
        updateConcept(concept);
      }
    }
  }

  /**
   * Handle descriptor.
   *
   * @param atom the atom
   * @throws Exception the exception
   */
  private void handleDescriptor(Atom atom) throws Exception {
    if (!ConfigUtility.isEmpty(atom.getDescriptorId())) {
      Descriptor descriptor = getDescriptor(atom.getDescriptorId(),
          atom.getTerminology(), atom.getVersion(), Branch.ROOT);
      if (descriptor == null) {
        descriptor = new DescriptorJpa();
        descriptor.setName(atom.getName());
        descriptor.setObsolete(atom.isObsolete());
        descriptor.setSuppressible(atom.isSuppressible());
        descriptor.setPublishable(atom.isPublishable());
        descriptor.setPublished(false);
        descriptor.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);
        descriptor.setTimestamp(new Date());
        descriptor.setTerminology(atom.getTerminology());
        descriptor.setVersion(atom.getVersion());
        descriptor.getAtoms().add(atom);
        descriptor.setTerminologyId(atom.getDescriptorId());
        descriptor = addDescriptor(descriptor);
      } else {
        descriptor = new DescriptorJpa(descriptor, true);
        descriptor.getAtoms().add(atom);
        updateDescriptor(descriptor);
      }
    }
  }
}
