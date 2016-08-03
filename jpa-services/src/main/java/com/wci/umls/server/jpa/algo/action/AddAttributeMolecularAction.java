/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.action;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.workflow.WorkflowStatus;
import com.wci.umls.server.services.handlers.IdentifierAssignmentHandler;

/**
 * A molecular action for adding an attribute.
 */
public class AddAttributeMolecularAction extends AbstractMolecularAction {

  /** The attribute. */
  private Attribute attribute;

  /**
   * Instantiates an empty {@link AddAttributeMolecularAction}.
   *
   * @throws Exception the exception
   */
  public AddAttributeMolecularAction() throws Exception {
    super();
    // n/a
  }

  /**
   * Returns the attribute.
   *
   * @return the attribute
   */
  public Attribute getAttribute() {
    return attribute;
  }

  /**
   * Sets the attribute.
   *
   * @param attribute the attribute
   */
  public void setAttribute(Attribute attribute) {
    this.attribute = attribute;
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {
    final ValidationResult validationResult = new ValidationResultJpa();

    if (getConcept().getTerminologyId() == "") {
      rollback();
      throw new LocalException(
          "Cannot add an attribute to a concept that doesn't have a TerminologyId (Concept: "
              + getConcept().getName() + ")");
    }

    // Perform action specific validation - n/a

    // Metadata referential integrity checking
    if (getAttributeName(attribute.getName(), getConcept().getTerminology(),
        getConcept().getVersion()) == null) {
      rollback();
      throw new LocalException(
          "Cannot add invalid attribute - " + attribute.getName());
    }
    if (getTerminology(attribute.getTerminology(),
        attribute.getVersion()) == null) {
      rollback();
      throw new LocalException(
          "Cannot add attribute with invalid terminology - "
              + attribute.getTerminology() + ", version: "
              + attribute.getVersion());
    }

    // Duplicate check
    for (final Attribute a : getConcept().getAttributes()) {
      if (a.getName().equals(attribute.getName())
          && a.getValue().equals(attribute.getValue())) {
        rollback();
        throw new LocalException("Duplicate attribute - " + attribute.getName()
            + ", with value " + attribute.getValue());
      }
    }

    return validationResult;
  }

  /* see superclass */
  @Override
  public void compute() throws Exception {
    //
    // Perform the action (contentService will create atomic actions for CRUD
    // operations)
    //

    // Assign alternateTerminologyId
    final IdentifierAssignmentHandler handler =
        getIdentifierAssignmentHandler(getConcept().getTerminology());
    final String altId = handler.getTerminologyId(attribute, getConcept());
    attribute.getAlternateTerminologyIds().put(getConcept().getTerminology(),
        altId);

    // set the attribute component last modified
    attribute = addAttribute(attribute, getConcept());

    // add the attribute and set the last modified by
    getConcept().getAttributes().add(attribute);
    if (getChangeStatusFlag()) {
      getConcept().setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
    }

    // update the concept
    updateConcept(getConcept());

    // log the REST call
    addLogEntry(getUserName(), getProject().getId(), getConcept().getId(),
        getMolecularAction().getActivityId(), getMolecularAction().getWorkId(),
        getName() + " " + attribute.getName() + " to concept "
            + getConcept().getTerminologyId());
  }

}
