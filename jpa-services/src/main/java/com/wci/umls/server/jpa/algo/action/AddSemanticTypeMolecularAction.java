/*
 *    Copyright 2015 West Coast Informatics, LLC
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
   * Sets the semantic type.
   *
   * @param sty the semantic type
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

    return validationResult;
  }

  /* see superclass */
  @Override
  public void compute() throws Exception {
    //
    // Perform the action (contentService will create atomic actions for CRUD
    // operations)
    //

    // add the semantic type component itself and set the last modified
    if (getChangeStatusFlag()) {
      sty.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
    }
    sty = addSemanticTypeComponent(sty, getConcept());

    // add the semantic type and set the last modified by
    getConcept().getSemanticTypes().add(sty);
    if (getChangeStatusFlag()) {
      getConcept().setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
    }

    // update the concept
    updateConcept(getConcept());

    // log the REST call
    addLogEntry(getUserName(), getProject().getId(), getConcept().getId(),
        getName() + " " + sty.getSemanticType());

  }

  /* see superclass */
  @Override
  public String getName() {
    return "ADD_SEMANTIC_TYPE";
  }

  /**
   * Returns the semantic type component.
   *
   * @return the semantic type component
   */
  public SemanticTypeComponent getSemanticTypeComponent() {
    return sty;
  }

}
