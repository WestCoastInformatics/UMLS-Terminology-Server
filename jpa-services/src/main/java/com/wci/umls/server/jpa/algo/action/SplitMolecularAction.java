/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.ConceptRelationshipJpa;
import com.wci.umls.server.jpa.content.SemanticTypeComponentJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.content.SemanticTypeComponent;
import com.wci.umls.server.model.meta.RelationshipType;
import com.wci.umls.server.model.workflow.WorkflowStatus;

/**
 * A molecular action for splitting a concept into two concepts.
 */
public class SplitMolecularAction extends AbstractMolecularAction {

  /** The atom ids. */
  private List<Long> atomIds;

  /** The move atoms. */
  private List<Atom> moveAtoms;

  /** The copy semantic types. */
  private boolean copySemanticTypes;

  /** The copy relationships. */
  private boolean copyRelationships;

  /** The relationship type abbr. */
  private String relationshipType;

  /** The originating concept pre updates. */
  private Concept originatingConceptPreUpdates;

  /** The originating concept post updates. */
  private Concept originatingConceptPostUpdates;

  /** The created concept post updates. */
  private Concept createdConceptPostUpdates;

  /** The created concept. */
  private Concept createdConcept;

  /**
   * Instantiates an empty {@link SplitMolecularAction}.
   *
   * @throws Exception the exception
   */
  public SplitMolecularAction() throws Exception {
    super();
    // n/a
  }

  /**
   * Sets the atom ids.
   *
   * @param atomIds the atom ids
   */
  public void setAtomIds(List<Long> atomIds) {
    this.atomIds = atomIds;
  }

  /**
   * Sets the copy semantic types.
   *
   * @param copySemanticTypes the copy semantic types
   */
  public void setCopySemanticTypes(boolean copySemanticTypes) {
    this.copySemanticTypes = copySemanticTypes;
  }

  /**
   * Sets the copy relationships.
   *
   * @param copyRelationships the copy relationships
   */
  public void setCopyRelationships(boolean copyRelationships) {
    this.copyRelationships = copyRelationships;
  }

  /**
   * Sets the relationship type
   *
   * @param relationshipType the relationship type
   */
  public void setRelationshipType(String relationshipType) {
    this.relationshipType = relationshipType;
  }

  /**
   * Returns the originating concept.
   *
   * @return the originating concept
   */
  public Concept getOriginatingConcept() {
    return getConcept();
  }

  /**
   * Returns the created concept.
   *
   * @return the created concept
   */
  public Concept getCreatedConcept() {
    return createdConcept;
  }

  /**
   * Returns the originating concept pre updates.
   *
   * @return the originating concept pre updates
   */
  public Concept getOriginatingConceptPreUpdates() {
    return originatingConceptPreUpdates;
  }

  /**
   * Returns the originating concept post updates.
   *
   * @return the originating concept post updates
   */
  public Concept getOriginatingConceptPostUpdates() {
    return originatingConceptPostUpdates;
  }

  /**
   * Returns the created concept post updates.
   *
   * @return the created concept post updates
   */
  public Concept getCreatedConceptPostUpdates() {
    return createdConceptPostUpdates;
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {
    final ValidationResult validationResult = new ValidationResultJpa();
    // Perform action specific validation - n/a

    // Metadata referential integrity checking

    // Populate move-atom list, and exists check
    moveAtoms = new ArrayList<Atom>();
    for (final Atom atm : getOriginatingConcept().getAtoms()) {
      if (atomIds.contains(atm.getId())) {
        moveAtoms.add(atm);
      }
    }
    if (!(moveAtoms.size() == atomIds.size())) {
      throw new LocalException("Atom to split out not found in Concept");
    }

    // Exists check on relationship Type, and populate
    final RelationshipType type =
        getRelationshipType(relationshipType, getTerminology(), getVersion());

    if (type == null) {
      throw new LocalException(
          "RelationshipType " + relationshipType + " not found.");
    }

    // TODO - add this method to everywhere it needs to be
    // validateSplit(getProject(), getConcept(), null);

    return validationResult;
  }

  /* see superclass */
  @Override
  public void compute() throws Exception {
    //
    // Perform the action (contentService will create atomic actions for CRUD
    // operations)
    //

    // Make copy of originating Concept before changes, to pass into
    // change event
    originatingConceptPreUpdates =
        new ConceptJpa(getOriginatingConcept(), false);
    // Create copy of current concept and add to content service
    createdConcept = new ConceptJpa(getConcept(), false);
    createdConcept.setId(null);
    createdConcept.setTerminologyId("");
    createdConcept.setName(getComputePreferredNameHandler(getTerminology())
        .computePreferredName(moveAtoms,
            getDefaultPrecedenceList(getTerminology(), getVersion())));
    if (getChangeStatusFlag()) {
      createdConcept.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
    }
    createdConcept = addConcept(createdConcept);

    // Add each listed atom from originatingConcept to createdConcept, delete
    // from
    // fromConcept, and set to NEEDS_REVIEW (if needed).
    moveAtoms(createdConcept, getOriginatingConcept(), moveAtoms);

    if (getChangeStatusFlag()) {
      for (Atom atm : moveAtoms) {
        atm.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
      }
    }

    // Add each semanticType from originatingConcept to createdConcept
    if (copySemanticTypes) {
      final List<SemanticTypeComponent> fromStys =
          new ArrayList<>(getOriginatingConcept().getSemanticTypes());

      for (SemanticTypeComponent sty : fromStys) {
        SemanticTypeComponent newSemanticType = new SemanticTypeComponentJpa(sty);
        newSemanticType.setId(null);
        SemanticTypeComponentJpa newAddedSemanticType =
            (SemanticTypeComponentJpa) addSemanticTypeComponent(newSemanticType,
                createdConcept);

        // add the semantic type and set the last modified by
        getCreatedConcept().getSemanticTypes().add(newAddedSemanticType);
      }

    }
       
    // Add each relationship from originatingConcept to be attached to
    // createdConcept
    if (copyRelationships) {
      List<ConceptRelationship> originatingRelationships =
          new ArrayList<>(getOriginatingConcept().getRelationships());

      // Go through all relationships in the originatingConcept
      for (final ConceptRelationship rel : originatingRelationships) {

        // Create copy of relationships and inverseRelationship, and update
        // to/from sections accordingly

        //
        // Create and add relationship and inverseRelationship
        //

        // Get the other associated concept
        Concept thirdConcept = rel.getTo();

        // set the relationship component last modified
        rel.setId(null);
        ConceptRelationshipJpa newRel =
            (ConceptRelationshipJpa) addRelationship(rel);
        newRel.setFrom(getCreatedConcept());
        if (getChangeStatusFlag()) {
          newRel.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
        }

        // construct inverse relationship
        ConceptRelationshipJpa inverseRel =
            (ConceptRelationshipJpa) createInverseConceptRelationship(newRel);

        // set the inverse relationship component last modified
        ConceptRelationshipJpa newInverseRel =
            (ConceptRelationshipJpa) addRelationship(inverseRel);
        if (getChangeStatusFlag()) {
          newInverseRel.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
        }

        // add relationship and inverse to respective concepts and set last
        // modified by
        getCreatedConcept().getRelationships().add(newRel);
        thirdConcept.getRelationships().add(newInverseRel);

      }
    }

    if (getChangeStatusFlag()) {
      getOriginatingConcept().setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
    }

    //
    // construct relationship between originating and created relationship
    //
    ConceptRelationship newBetweenRel = new ConceptRelationshipJpa();
    newBetweenRel.setBranch(Branch.ROOT);
    newBetweenRel.setRelationshipType(relationshipType);
    newBetweenRel.setAdditionalRelationshipType("");
    newBetweenRel.setFrom(getOriginatingConcept());
    newBetweenRel.setTo(getCreatedConcept());
    newBetweenRel.setTerminology(getTerminology());
    newBetweenRel.setTerminologyId("");
    newBetweenRel.setVersion(getVersion());
    newBetweenRel.setTimestamp(new Date());
    newBetweenRel.setPublishable(true);
    newBetweenRel.setAssertedDirection(false);
    newBetweenRel.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);
    if (getChangeStatusFlag()) {
      newBetweenRel.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
    }

    // set the inverse relationship component last modified
    ConceptRelationshipJpa newAddedBetweenRel =
        (ConceptRelationshipJpa) addRelationship(newBetweenRel);
    
    getOriginatingConcept().getRelationships().add(newAddedBetweenRel);

    // construct inverse relationship
    ConceptRelationshipJpa inverseBetweenRel =
        (ConceptRelationshipJpa) createInverseConceptRelationship(
            newBetweenRel);

    // set the inverse relationship component last modified
    ConceptRelationshipJpa newInverseBetweenRel =
        (ConceptRelationshipJpa) addRelationship(inverseBetweenRel);

    getCreatedConcept().getRelationships().add(newInverseBetweenRel);

    // update the concepts
    updateConcept(getOriginatingConcept());
    updateConcept(getCreatedConcept());
    
    // log the REST calls
    addLogEntry(getUserName(), getProject().getId(),
        getOriginatingConcept().getId(),
        getName() + " " + getOriginatingConcept().getId() + " into concept "
            + getCreatedConcept().getId());
    
    // Make copy of toConcept to pass into change event
    originatingConceptPostUpdates =
        new ConceptJpa(getOriginatingConcept(), false);
    createdConceptPostUpdates = new ConceptJpa(getCreatedConcept(), false);

  }

}
