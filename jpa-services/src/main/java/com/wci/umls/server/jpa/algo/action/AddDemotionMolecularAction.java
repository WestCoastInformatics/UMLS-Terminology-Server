/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.action;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.content.AtomRelationshipJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.AtomRelationship;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.workflow.WorkflowStatus;

/**
 * A molecular action for adding a demotion.
 */
public class AddDemotionMolecularAction extends AbstractMolecularAction {

  /** The atom 1. */
  private Atom atom;

  /** The atom 2. */
  private Atom atom2;

  /**
   * Instantiates an empty {@link AddDemotionMolecularAction}.
   *
   * @throws Exception the exception
   */
  public AddDemotionMolecularAction() throws Exception {
    super();
    // n/a
  }

  /**
   * Sets the atom 1.
   *
   * @param atom the atom 1
   */
  public void setAtom(Atom atom) {
    this.atom = atom;
  }

  /**
   * Sets the atom 2.
   *
   * @param atom2 the atom 2
   */
  public void setAtom2(Atom atom2) {
    this.atom2 = atom2;
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {
    final ValidationResult validationResult = new ValidationResultJpa();

    // Perform action specific validation - n/a

    // Verify concept id1/2 are not the same
    if (getConcept().getId().equals(getConcept2().getId())) {
      throw new Exception(
          "Unexpected self-referential relationship, the fromConcept Id should not match toConcept Id");
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

    // Construct the demotion relationship
    AtomRelationship demotionRelationship = new AtomRelationshipJpa();
    demotionRelationship.setFrom(atom);
    demotionRelationship.setTo(atom2);
    demotionRelationship.setWorkflowStatus(WorkflowStatus.DEMOTION);
    demotionRelationship.setRelationshipType("RO");
    demotionRelationship.setAdditionalRelationshipType("");
    demotionRelationship.setTerminology(getTerminology());
    demotionRelationship.setTerminologyId("");
    demotionRelationship.setVersion(getVersion());

    // construct inverse relationship
    AtomRelationship inverseDemotionRelationship = new AtomRelationshipJpa();
    inverseDemotionRelationship.setFrom(atom2);
    inverseDemotionRelationship.setTo(atom);
    inverseDemotionRelationship.setWorkflowStatus(WorkflowStatus.DEMOTION);
    inverseDemotionRelationship.setRelationshipType("RO");
    inverseDemotionRelationship.setAdditionalRelationshipType("");
    inverseDemotionRelationship.setTerminology(getTerminology());
    inverseDemotionRelationship.setTerminologyId("");
    inverseDemotionRelationship.setVersion(getVersion());

    // Add the demotions
    demotionRelationship =
        (AtomRelationshipJpa) addRelationship(demotionRelationship);
    inverseDemotionRelationship =
        (AtomRelationshipJpa) addRelationship(inverseDemotionRelationship);

    // Add the demotions to atoms
    atom.getRelationships().add(demotionRelationship);
    atom2.getRelationships().add(inverseDemotionRelationship);

    // update the atoms
    updateAtom(atom);
    updateAtom(atom2);

    // Change status of the atoms
    atom.setWorkflowStatus(WorkflowStatus.DEMOTION);
    atom2.setWorkflowStatus(WorkflowStatus.DEMOTION);

    // update the atoms
    updateAtom(atom);
    updateAtom(atom2);

    // Set any matching concept relationships to unreleasable
    if (getChangeStatusFlag()) {
      ConceptRelationship matchingCRel =
          findRelToConceptContainingAtom(getConcept(), atom2);

      if (matchingCRel != null) {
        matchingCRel.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
        updateRelationship(matchingCRel);
      }

      ConceptRelationship matchingInverseCRel =
          findRelToConceptContainingAtom(getConcept2(), atom);

      if (matchingInverseCRel != null) {
        matchingInverseCRel.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
        updateRelationship(matchingInverseCRel);
      }
    }

    // Change status of the concepts
    if (getChangeStatusFlag()) {
      getConcept().setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
      getConcept2().setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);

      // update the concepts
      updateConcept(getConcept());
      updateConcept(getConcept2());
    }

    // log the REST calls
    addLogEntry(getLastModifiedBy(), getProject().getId(), getConcept().getId(),
        getActivityId(), getWorkId(), getName() + " to concept "
            + getConcept().getId() + " " + demotionRelationship);
    addLogEntry(getLastModifiedBy(), getProject().getId(),
        getConcept2().getId(), getActivityId(), getWorkId(),
        getName() + " from concept " + getConcept().getId() + " "
            + inverseDemotionRelationship);

  }

}
