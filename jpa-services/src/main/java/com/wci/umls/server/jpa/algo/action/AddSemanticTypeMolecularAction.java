/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.action;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.model.content.SemanticTypeComponent;
import com.wci.umls.server.model.workflow.WorkflowStatus;

/**
 * A molecular action for adding a semantic type.
 */
public class AddSemanticTypeMolecularAction extends AbstractMolecularAction {

  /** The semantic type component. */
  private SemanticTypeComponent sty;

  /**
   * Instantiates an empty {@link AddSemanticTypeMolecularAction}.
   *
   * @throws Exception the exception
   */
  public AddSemanticTypeMolecularAction() throws Exception {
    super();
    // n/a
  }

  /**
   * Returns the sty.
   *
   * @return the sty
   */
  public SemanticTypeComponent getSemanticTypeComponent() {
    return sty;
  }

  /**
   * Sets the sty.
   *
   * @param sty the sty
   */
  public void setSemanticTypeComponent(SemanticTypeComponent sty) {
    this.sty = sty;
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {
    final ValidationResult validationResult = new ValidationResultJpa();
    // Perform action specific validation - n/a

    // Metadata referential integrity checking
    if (getSemanticType(sty.getSemanticType(), getConcept().getTerminology(),
        getConcept().getVersion()) == null) {
      rollback();
      throw new LocalException(
          "Cannot add invalid semantic type - " + sty.getSemanticType());
    }
    if (getTerminology(sty.getTerminology(), sty.getVersion()) == null) {
      rollback();
      throw new LocalException(
          "Cannot add semanticType with invalid terminology - "
              + sty.getTerminology() + ", version: " + sty.getVersion());
    }

    // Duplicate check
    for (final SemanticTypeComponent s : getConcept().getSemanticTypes()) {
      if (s.getSemanticType().equals(sty.getSemanticType())) {
        rollback();
        throw new LocalException(
            "Duplicate semantic type - " + sty.getSemanticType());
      }
    }

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

    // Change status of the semantic type component
    if (getChangeStatusFlag()) {
      sty.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
    }

    // Add the semantic type component
    sty = addSemanticTypeComponent(sty, getConcept());

    // Change status of the concept
    if (getChangeStatusFlag()) {
      getConcept().setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
    }

    // Add the semantic type component to concept
    getConcept().getSemanticTypes().add(sty);

    // update the concept
    updateConcept(getConcept());
  }

  /* see superclass */
  @Override
  public void logAction() throws Exception {

    // log the REST call
    addLogEntry(getLastModifiedBy(), getProject().getId(), getConcept().getId(),
        getActivityId(), getWorkId(),
        getName() + " to concept " + getConcept().getId() + " " + sty);

    // Log for the molecular action report
    addLogEntry(getLastModifiedBy(), getProject().getId(),
        getMolecularAction().getId(), getActivityId(), getWorkId(),
        "\nACTION  " + getName() + "\n  concept = " + getConcept().getId() + " "
            + getConcept().getName() + "\n  semantic type = "
            + getSemanticTypeComponent().getSemanticType());

  }

}
