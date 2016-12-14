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
 * A molecular action for removing a semantic type.
 */
public class RemoveSemanticTypeMolecularAction extends AbstractMolecularAction {

  /** The semantic type component. */
  private SemanticTypeComponent sty;

  /** The semantic type component id. */
  private Long semanticTypeComponentId;

  /**
   * Instantiates an empty {@link RemoveSemanticTypeMolecularAction}.
   *
   * @throws Exception the exception
   */
  public RemoveSemanticTypeMolecularAction() throws Exception {
    super();
    // n/a
  }

  /**
   * Returns the semantic type component.
   *
   * @return the semantic type component
   */
  public SemanticTypeComponent getSemanticTypeComponent() {
    return sty;
  }

  /**
   * Returns the semantic type component id.
   *
   * @return the semantic type component id
   */
  public Long getSemanticTypeComponentId() {
    return semanticTypeComponentId;
  }

  /**
   * Sets the semantic type component id.
   *
   * @param semanticTypeComponentId the semantic type component id
   */
  public void setSemanticTypeComponentId(Long semanticTypeComponentId) {
    this.semanticTypeComponentId = semanticTypeComponentId;
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {
    final ValidationResult validationResult = new ValidationResultJpa();
    // Perform action specific validation - n/a

    // Exists check, and set SemanticTypeComponent
    for (final SemanticTypeComponent sty : getConcept().getSemanticTypes()) {
      if (sty.getId().equals(semanticTypeComponentId)) {
        this.sty = sty;
      }
    }
    if (sty == null) {
      rollback();
      throw new LocalException("Semantic type to remove does not exist");
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

    // Remove the semantic type from the concept
    removeById(getConcept().getSemanticTypes(), sty.getId());

    // Update Concept
    updateConcept(getConcept());

    // remove the semantic type component
    removeSemanticTypeComponent(semanticTypeComponentId);

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
        getName() + " from concept " + getConcept().getId() + " " + sty);

    // Log for the molecular action report
    addLogEntry(getLastModifiedBy(), getProject().getId(),
        getMolecularAction().getId(), getActivityId(), getWorkId(),
        "\nACTION  " + getName() + "\n  concept = " + getConcept().getId() + " "
            + getConcept().getName() + "\n  semantic type = "
            + sty.getSemanticType());
  }

}
