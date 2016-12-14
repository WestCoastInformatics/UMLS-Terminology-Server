/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.action;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.workflow.WorkflowStatus;

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

    // Add the attribute
    attribute = addAttribute(attribute, getConcept());

    // Change status of the concept
    if (getChangeStatusFlag()) {
      getConcept().setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
    }

    // Add the attribute to concept
    getConcept().getAttributes().add(attribute);

    // update the concept
    updateConcept(getConcept());
  }

  /* see superclass */
  @Override
  public void logAction() throws Exception {

    // log the REST call
    addLogEntry(getLastModifiedBy(), getProject().getId(), getConcept().getId(),
        getActivityId(), getWorkId(),
        getName() + " to concept " + getConcept().getId() + " " + attribute);

    // Log for the molecular action report
    addLogEntry(getLastModifiedBy(), getProject().getId(),
        getMolecularAction().getId(), getActivityId(), getWorkId(),
        "\nACTION  " + getName() + "\n  concept = " + getConcept().getId() + " "
            + getConcept().getName() + "\n  attribute = "
            + getAttribute().getName() + ", " + attribute.getValue());

  }

}
