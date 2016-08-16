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
 * A molecular action for removing an attribute.
 */
public class RemoveAttributeMolecularAction extends AbstractMolecularAction {

  /** The attribute. */
  private Attribute attribute;

  /** The attribute id. */
  private Long attributeId;

  /**
   * Instantiates an empty {@link RemoveAttributeMolecularAction}.
   *
   * @throws Exception the exception
   */
  public RemoveAttributeMolecularAction() throws Exception {
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
   * Returns the attribute id.
   *
   * @return the attribute id
   */
  public Long getAttributeId() {
    return attributeId;
  }

  /**
   * Sets the attribute id.
   *
   * @param attributeId the attribute id
   */
  public void setAttributeId(Long attributeId) {
    this.attributeId = attributeId;
  }

  /**
   * Check preconditions.
   *
   * @return the validation result
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {
    final ValidationResult validationResult = new ValidationResultJpa();
    // Perform action specific validation - n/a

    // Exists check
    for (final Attribute atr : getConcept().getAttributes()) {
      if (atr.getId().equals(attributeId)) {
        this.attribute = atr;
      }
    }
    if (attribute == null) {
      rollback();
      throw new LocalException("Attribute to remove does not exist");
    }

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
    // Perform the action
    //

    // Remove the attribute from the concept
    getConcept().getAttributes().remove(attribute);

    // Update Concept
    updateConcept(getConcept());

    // Remove the attribute
    removeAttribute(attributeId);

    // Change status of concept
    if (getChangeStatusFlag()) {
      getConcept().setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
    }

    // Update Concept
    updateConcept(getConcept());

    // log the REST call
    addLogEntry(getUserName(), getProject().getId(), getConcept().getId(),
        getActivityId(), getWorkId(), getName() + " from concept "
            + getConcept().getId() + " " + attribute);

  }

}
