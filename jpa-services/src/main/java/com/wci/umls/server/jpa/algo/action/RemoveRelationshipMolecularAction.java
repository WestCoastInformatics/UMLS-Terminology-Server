/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.action;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.workflow.WorkflowStatus;

/**
 * A molecular action for removing a relationship.
 */
public class RemoveRelationshipMolecularAction extends AbstractMolecularAction {

  /** The relationship. */
  private ConceptRelationship relationship;

  /** The relationship id. */
  private Long relationshipId;

  /**
   * Instantiates an empty {@link RemoveRelationshipMolecularAction}.
   *
   * @throws Exception the exception
   */
  public RemoveRelationshipMolecularAction() throws Exception {
    super();
    // n/a
  }

  /**
   * Returns the relationship id.
   *
   * @return the relationship id
   */
  public Long getRelationshipId() {
    return relationshipId;
  }

  /**
   * Sets the relationship id.
   *
   * @param relationshipId the relationship id
   */
  public void setRelationshipId(Long relationshipId) {
    this.relationshipId = relationshipId;
  }

  /**
   * Returns the relationship.
   *
   * @return the relationship
   */
  public ConceptRelationship getRelationship() {
    return relationship;
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {
    final ValidationResult validationResult = new ValidationResultJpa();
    // Perform action specific validation - n/a

    // Exists check
    for (final ConceptRelationship rel : getConcept().getRelationships()) {
      if (rel.getId().equals(relationshipId)) {
        relationship = rel;
      }
    }
    if (relationship == null) {
      throw new LocalException("Relationship to remove does not exist");
    }

    // RelationshipList relList =
    // findConceptRelationships(getConcept2().getTerminologyId(),
    // getConcept2().getTerminology(),
    // getConcept2().getVersion(), Branch.ROOT, "fromId:"
    // + getConcept2().getId() + " AND toId:" + getConcept().getId(),
    // false, null);
    //
    // for (final Relationship<? extends ComponentInfo, ? extends ComponentInfo>
    // rel : relList
    // .getObjects()) {
    // if (rel.getTo().getId() == relationship.getFrom().getId()
    // && rel.getFrom().getId() == relationship.getTo().getId()
    // && getRelationshipType(rel.getRelationshipType(),
    // rel.getTerminology(), rel.getVersion()).getInverse()
    // .getAbbreviation()
    // .equals(relationship.getRelationshipType())) {
    // if (inverseRelationship != null) {
    // throw new Exception(
    // "Unexepected more than a single inverse relationship for relationship - "
    // + relationship);
    // }
    //
    // inverseRelationship = (ConceptRelationship) rel;
    // }
    // }

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

    // Remove the relationship from the concepts
    getConcept().getRelationships().remove(relationship);
    getConcept2().getRelationships()
        .remove(findInverseRelationship(relationship));

    // Update Concepts
    updateConcept(getConcept());
    updateConcept(getConcept2());

    // Remove the relationships
    removeRelationship(relationship.getId(), relationship.getClass());
    removeRelationship(findInverseRelationship(relationship).getId(),
        relationship.getClass());

    // Change status of ONLY the source concept
    if (getChangeStatusFlag()) {
      getConcept().setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
    }

    // Update Concepts
    updateConcept(getConcept());

    // log the REST call
    addLogEntry(getUserName(), getProject().getId(), getConcept().getId(),
        getActivityId(), getWorkId(), getName() + " to concept "
            + getConcept2().getId() + " " + relationship);
    addLogEntry(getUserName(), getProject().getId(), getConcept2().getId(),
        getActivityId(), getWorkId(), getName() + " from concept "
            + getConcept().getId() + " " + relationship);

  }

}
