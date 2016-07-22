/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.action;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.content.ConceptRelationshipJpa;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.workflow.WorkflowStatus;

/**
 * A molecular action for adding a relationship.
 */
public class AddRelationshipMolecularAction extends AbstractMolecularAction {

  /** The relationship. */
  private ConceptRelationship relationship;

  /**
   * Instantiates an empty {@link AddRelationshipMolecularAction}.
   *
   * @throws Exception the exception
   */
  public AddRelationshipMolecularAction() throws Exception {
    super();
    // n/a
  }

  /**
   * Returns the relationship.
   *
   * @return the relationship
   */
  public ConceptRelationship getRelationship() {
    return relationship;
  }

  /**
   * Sets the relationship.
   *
   * @param relationship the relationship
   */
  public void setRelationship(ConceptRelationship relationship) {
    this.relationship = relationship;
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {
    final ValidationResult validationResult = new ValidationResultJpa();

    // Perform action specific validation - n/a

    // Metadata referential integrity checking
    if (getRelationshipType(relationship.getRelationshipType(),
        relationship.getTerminology(), relationship.getVersion()) == null) {
      rollback();
      throw new LocalException(
          "Cannot add relationship with invalid relationship type - "
              + relationship.getRelationshipType());
    }
    if (getAdditionalRelationshipType(
        relationship.getAdditionalRelationshipType(),
        relationship.getTerminology(), relationship.getVersion()) == null) {
      rollback();
      throw new LocalException(
          "Cannot add relationship with invalid additional relationship type - "
              + relationship.getAdditionalRelationshipType());
    }
    if (getTerminology(relationship.getTerminology(),
        relationship.getVersion()) == null) {
      rollback();
      throw new LocalException(
          "Cannot add relationship with invalid terminology - "
              + relationship.getTerminology() + ", version: "
              + relationship.getVersion());
    }

    // Duplicate check
    for (final ConceptRelationship a : getConcept().getRelationships()) {
      if (a.equals(relationship)) {
        rollback();
        throw new LocalException(
            "Duplicate relationship - " + relationship.getName());
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

    if (getChangeStatusFlag()) {
      relationship.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
    }
    // Assign alternateTerminologyId
    // final IdentifierAssignmentHandler handler = contentService
    // .getIdentifierAssignmentHandler(concept.getTerminology());
    //
    // final String altId = handler.getTerminologyId(relationship);
    // relationship.getAlternateTerminologyIds().put(concept.getTerminology(),
    // altId);

    //XR (not related) relationships need to be set to not-released
    if (relationship.getRelationshipType().equals("XR")){
      relationship.setPublishable(false);
    }
    
    // set the relationship component last modified
    relationship = (ConceptRelationshipJpa) addRelationship(relationship);

    // construct inverse relationship
    final ConceptRelationshipJpa inverseRelationship =
        (ConceptRelationshipJpa) createInverseConceptRelationship(relationship);

    // pass to handler.getTerminologyId
    // final String inverseAltId =
    // handler.getTerminologyId(inverseRelationship);
    // inverseRelationship.getAlternateTerminologyIds()
    // .put(concept.getTerminology(), inverseAltId);

    // set the relationship component last modified
    final ConceptRelationshipJpa newInverseRelationship =
        (ConceptRelationshipJpa) addRelationship(inverseRelationship);

    // add the relationship and set the last modified by
    getConcept().getRelationships().add(relationship);
    getConcept2().getRelationships().add(newInverseRelationship);
    if (getChangeStatusFlag()) {
      getConcept().setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
    }
    
    // update the concept
    updateConcept(getConcept2());
    updateConcept(getConcept());

    // log the REST calls
    addLogEntry(getUserName(), getProject().getId(), getConcept().getId(),
        getName() + " " + relationship + " to concept "
            + getConcept().getTerminologyId());

  }

}
