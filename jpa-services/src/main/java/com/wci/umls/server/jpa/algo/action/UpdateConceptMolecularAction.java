/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.action;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.workflow.WorkflowStatus;

/**
 * A molecular action for updating a concept workflow status.
 */
public class UpdateConceptMolecularAction extends AbstractMolecularAction {

  /** The workflow status. */
  private WorkflowStatus workflowStatus;

  /** The publishable. */
  private boolean publishable;

  /**
   * Instantiates an empty {@link UpdateConceptMolecularAction}.
   *
   * @throws Exception the exception
   */
  public UpdateConceptMolecularAction() throws Exception {
    super();
    // n/a
  }

  /**
   * Sets the workflow status.
   *
   * @param workflowStatus the workflow status
   */
  public void setWorkflowStatus(WorkflowStatus workflowStatus) {
    this.workflowStatus = workflowStatus;
  }

  /**
   * Sets the publishable.
   *
   * @param publishable the publishable
   */
  public void setPublishable(boolean publishable) {
    this.publishable = publishable;
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {
    // Perform action specific validation - n/a

    // Metadata referential integrity checking

    // Check preconditions
    return super.checkPreconditions();
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

    // Make a copy of the concept
    Concept updateConcept = new ConceptJpa(getConcept(), true);

    //
    // Change the concept in the limited ways supported
    //
    updateConcept.setWorkflowStatus(this.workflowStatus);
    updateConcept.setPublishable(publishable);

    //
    // update the Concept
    //
    updateConcept(updateConcept);

    // log the REST calls
    addLogEntry(getLastModifiedBy(), getProject().getId(), getConcept().getId(),
        getActivityId(), getWorkId(), getName() + " " + getConcept());

    // Log for the molecular action report
    addLogEntry(getLastModifiedBy(), getProject().getId(),
        getMolecularAction().getId(), getActivityId(), getWorkId(),
        "\nACTION  " + getName() + "\n  concept = " + getConcept().getId() + " "
            + getConcept().getName() + ", " + getConcept().isPublishable()
            + ", " + getConcept().isSuppressible() + ", "
            + getConcept().getWorkflowStatus());
  }

}
