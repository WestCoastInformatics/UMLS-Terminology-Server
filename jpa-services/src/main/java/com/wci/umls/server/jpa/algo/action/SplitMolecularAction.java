/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.ConceptRelationshipJpa;
import com.wci.umls.server.jpa.content.SemanticTypeComponentJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.content.Relationship;
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
  private String relationshipType = null;

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
   * Returns the from concept.
   *
   * @return the from concept
   */
  public Concept getFromConcept() {
    return getConcept();
  }

  /**
   * Returns the to concept.
   *
   * @return the to concept
   */
  public Concept getToConcept() {
    return getConcept2();
  }

  /**
   * Sets the to concept.
   *
   * @param toConcept the to concept
   */
  public void setToConcept(Concept toConcept) {
    setConcept2(toConcept);
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
   * Sets the relationship type.
   *
   * @param relationshipType the relationship type
   */
  public void setRelationshipType(String relationshipType) {
    this.relationshipType = relationshipType;
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {
    final ValidationResult validationResult = new ValidationResultJpa();
    // Perform action specific validation - n/a

    // Metadata referential integrity checking
    if (atomIds == null || atomIds.size() == 0) {
      throw new LocalException(
          "Split action requires at least one selected atom");
    }

    // Populate move-atom list, and exists check
    moveAtoms = new ArrayList<Atom>();
    for (final Atom atm : getFromConcept().getAtoms()) {
      if (atomIds.contains(atm.getId())) {
        moveAtoms.add(atm);
      }
    }
    if (!(moveAtoms.size() == atomIds.size())) {
      throw new LocalException("Atom to split out not found in Concept");
    }

    // Exists check on relationship Type (if specified by user), and populate
    if (relationshipType != null) {
      final RelationshipType type =
          getRelationshipType(relationshipType, getTerminology(), getVersion());

      if (type == null) {
        throw new LocalException(
            "RelationshipType " + relationshipType + " not found.");
      }
    }

    // Check preconditions
    validationResult.merge(super.checkPreconditions());
    return validationResult;
  }

  /* see superclass */
  @SuppressWarnings("rawtypes")
  @Override
  public void compute() throws Exception {
    //
    // Perform the action (contentService will create atomic actions for CRUD
    // operations)
    //

    // Get all "inverse" relationships for the "from" concept (e.g.
    // where toId is the id)
    final Map<Long, ConceptRelationship> inverseFromRelsMap = new HashMap<>();
    for (final Relationship rel : findConceptRelationships(null,
        getTerminology(), getVersion(), Branch.ROOT,
        "toId:" + getFromConcept().getId(), false, null).getObjects()) {
      final ConceptRelationship crel =
          new ConceptRelationshipJpa((ConceptRelationship) rel, false);
      if (inverseFromRelsMap.containsKey(crel.getFrom().getId())) {
        throw new Exception("Multiple concept level relationships from "
            + crel.getFrom().getId());
      }
      inverseFromRelsMap.put(crel.getFrom().getId(), crel);
    }

    // Copy atoms in "from" concept
    List<Atom> moveAtomsCopies = new ArrayList<>();
    for (final Atom atom : moveAtoms) {
      moveAtomsCopies.add(new AtomJpa(atom));
    }

    // Copy stys in "from" concept
    List<SemanticTypeComponent> fromStysCopies = new ArrayList<>();
    if (copySemanticTypes) {
      for (final SemanticTypeComponent sty : getFromConcept()
          .getSemanticTypes()) {
        fromStysCopies.add(new SemanticTypeComponentJpa(sty));
      }
    }

    // Copy rels in "from" concept
    List<ConceptRelationship> fromRelationshipsCopies = new ArrayList<>();
    if (copyRelationships) {
      for (final ConceptRelationship rel : getFromConcept()
          .getRelationships()) {
        fromRelationshipsCopies.add(new ConceptRelationshipJpa(rel, false));
      }
    }

    // Prep a list of concepts to update after object removal
    final Set<Concept> conceptsChanged = new HashSet<>();
    conceptsChanged.add(getFromConcept());

    //
    // Remove objects from the Concept
    //
    // Only done for atoms - semantic types and relationships are kept in
    // originating Concept
    for (final Atom atom : moveAtomsCopies) {
      removeById(getFromConcept().getAtoms(), atom.getId());
    }

    //
    // Update originatingConcept
    //
    updateConcept(getFromConcept());

    //
    // Remove the objects from the database
    //
    // Not done for Atoms

    //
    // Create and add the new concept
    //
    setToConcept(new ConceptJpa());
    getToConcept().setTimestamp(new Date());
    getToConcept().setSuppressible(false);
    getToConcept().setObsolete(false);
    getToConcept().setPublished(false);
    getToConcept().setPublishable(true);
    getToConcept().setTerminologyId("");
    getToConcept().setTerminology(getFromConcept().getTerminology());
    getToConcept().setVersion(getFromConcept().getVersion());
    getToConcept().setWorkflowStatus(getChangeStatusFlag()
        ? WorkflowStatus.NEEDS_REVIEW : WorkflowStatus.READY_FOR_PUBLICATION);
    // Compute preferred name - assumes moveAtoms has at least one
    getToConcept()
        .setName(getComputePreferredNameHandler(getTerminology())
            .sortAtoms(moveAtoms,
                getPrecedenceList(getTerminology(), getVersion()))
            .get(0).getName());

    // Add the concept and lookup the terminology id
    setToConcept(new ConceptJpa(addConcept(getToConcept()), false));
    getToConcept().setTerminologyId(getToConcept().getId().toString());
    conceptsChanged.add(getToConcept());

    // Add newly created concept and conceptId to the molecular action (undo
    // action uses
    // this)
    getMolecularAction().setComponentId2(getToConcept().getId());
    updateMolecularAction(getMolecularAction());

    //
    // Update and add objects
    //

    // Set workflow status of "from" atoms and add to the "to" concept.
    for (final Atom atom : moveAtomsCopies) {
      if (getChangeStatusFlag()) {
        atom.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
        updateAtom(atom);
      }
      getToConcept().getAtoms().add(atom);
    }

    // Add new semantic types and wire them to "to" concept (if copying them
    // over)
    if (copySemanticTypes) {
      for (SemanticTypeComponent sty : fromStysCopies) {
        sty.setId(null);
        if (getChangeStatusFlag()) {
          sty.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
        }
        final SemanticTypeComponent newSty =
            addSemanticTypeComponent(sty, getToConcept());
        getToConcept().getSemanticTypes().add(newSty);
      }
    }

    // add concept relationships (if copying them over)
    if (copyRelationships) {
      for (final ConceptRelationship rel : fromRelationshipsCopies) {
        rel.setId(null);
        rel.setFrom(getToConcept());
        if (getChangeStatusFlag()) {
          rel.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
        }
        addRelationship(rel);
        getToConcept().getRelationships().add(rel);
      }

      // Corresponding logic for inverse rels
      for (final ConceptRelationship rel : inverseFromRelsMap.values()) {
        rel.setId(null);
        rel.setTo(getToConcept());
        if (getChangeStatusFlag()) {
          rel.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
        }
        addRelationship(rel);
        final Concept concept = new ConceptJpa(rel.getFrom(), true);
        concept.getRelationships().add(rel);
        conceptsChanged.add(concept);
      }
    }

    //
    // construct relationship between originating and created relationship,
    // if specified by user.
    //
    ConceptRelationship newBetweenRel = null;
    ConceptRelationshipJpa inverseBetweenRel = null;
    if (relationshipType != null) {
      newBetweenRel = new ConceptRelationshipJpa();
      newBetweenRel.setBranch(Branch.ROOT);
      newBetweenRel.setRelationshipType(relationshipType);
      newBetweenRel.setAdditionalRelationshipType("");
      newBetweenRel.setFrom(getFromConcept());
      newBetweenRel.setTo(getToConcept());
      newBetweenRel.setTerminology(getTerminology());
      newBetweenRel.setTerminologyId("");
      newBetweenRel.setVersion(getVersion());
      newBetweenRel.setTimestamp(new Date());
      newBetweenRel.setPublishable(true);
      newBetweenRel.setAssertedDirection(false);
      newBetweenRel.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);

      addRelationship(newBetweenRel);
      getFromConcept().getRelationships().add(newBetweenRel);

      // construct inverse relationship as well
      String inverseRelType =
          getRelationshipType(newBetweenRel.getRelationshipType(),
              getProject().getTerminology(), getProject().getVersion())
                  .getInverse().getAbbreviation();

      String inverseAdditionalRelType = "";
      if (!newBetweenRel.getAdditionalRelationshipType().equals("")) {
        inverseAdditionalRelType = getAdditionalRelationshipType(
            newBetweenRel.getAdditionalRelationshipType(),
            getProject().getTerminology(), getProject().getVersion())
                .getInverse().getAbbreviation();
      }

      // Create the inverse relationship
      inverseBetweenRel =
          (ConceptRelationshipJpa) newBetweenRel.createInverseRelationship(
              newBetweenRel, inverseRelType, inverseAdditionalRelType);

      addRelationship(inverseBetweenRel);
      getToConcept().getRelationships().add(inverseBetweenRel);
    }

    //
    // Change status of the concepts
    //
    if (getChangeStatusFlag()) {
      getFromConcept().setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
      getToConcept().setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
    }

    //
    // update the to and from Concepts, and all concepts a relationship has been
    // added to
    //
    for (final Concept concept : conceptsChanged) {
      updateConcept(concept);
    }

  }

  /* see superclass */
  @Override
  public void logAction() throws Exception {

    // log the REST calls
    addLogEntry(getLastModifiedBy(), getProject().getId(),
        getFromConcept().getId(), getActivityId(), getWorkId(),
        getName() + " from concept " + getFromConcept().getId()
            + " into concept " + getToConcept().getId());

    addLogEntry(getLastModifiedBy(), getProject().getId(),
        getToConcept().getId(), getActivityId(), getWorkId(),
        getName() + " into concept " + getToConcept().getId() + " from concept "
            + getFromConcept().getId());

    // Log for the molecular action report
    final StringBuilder sb = new StringBuilder();
    sb.append("\n  move atoms =");
    for (final Atom atom : moveAtoms) {
      sb.append(("\n    " + atom.getName() + ", " + atom.getTerminology() + "/"
          + atom.getTermType() + "," + atom.getCodeId()));
    }
    addLogEntry(getLastModifiedBy(), getProject().getId(),
        getMolecularAction().getId(), getActivityId(), getWorkId(),
        "\nACTION  " + getName() + "\n  concept (from) = "
            + getFromConcept().getId() + " " + getFromConcept().getName()
            + (getToConcept() != null ? "\n  concept2 (to) = "
                + getToConcept().getId() + " " + getToConcept().getName() : "")
            + sb + "\n  copy rels = " + copyRelationships + "\n  copy stys = "
            + copySemanticTypes);

  }

  /* see superclass */
  @Override
  public boolean lockRelatedConcepts() {
    return true;
  }

}
