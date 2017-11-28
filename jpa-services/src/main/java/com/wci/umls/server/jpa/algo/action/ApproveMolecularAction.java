/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.ConceptRelationshipJpa;
import com.wci.umls.server.jpa.content.SemanticTypeComponentJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.AtomRelationship;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.model.content.SemanticTypeComponent;
import com.wci.umls.server.model.workflow.WorkflowStatus;

/**
 * A molecular action for approving a concept.
 */
public class ApproveMolecularAction extends AbstractMolecularAction {

  /**
   * Instantiates an empty {@link ApproveMolecularAction}.
   *
   * @throws Exception the exception
   */
  public ApproveMolecularAction() throws Exception {
    super();
    // n/a
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {
    final ValidationResult validationResult = new ValidationResultJpa();
    // Perform action specific validation - n/a

    // Metadata referential integrity checking

    // Check preconditions
    validationResult.merge(super.checkPreconditions());
    validationResult.merge(validateConcept(
        this.getProject().getValidationChecks(), this.getConcept()));
    return validationResult;
  }

  /**
   * Compute.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings("rawtypes")
  /* see superclass */
  @Override
  public void compute() throws Exception {
    //
    // Perform the action (contentService will create atomic actions for CRUD
    // operations)
    //

    // Get all "inverse" relationships for the concept (e.g.
    // where toId is the id)
    final Map<Long, ConceptRelationship> inverseRelsMap = new HashMap<>();
    for (final Relationship rel : findConceptRelationships(null,
        getTerminology(), getVersion(), Branch.ROOT,
        "toId:" + getConcept().getId(), false, null).getObjects()) {
      final ConceptRelationship crel =
          new ConceptRelationshipJpa((ConceptRelationship) rel, false);
      if (inverseRelsMap.containsKey(crel.getFrom().getId())) {
        throw new Exception("Multiple concept level relationships from "
            + crel.getFrom().getId());
      }
      inverseRelsMap.put(crel.getFrom().getId(), crel);
    }

    //
    // Copy each object in the Concept
    //
    final List<Atom> atoms = new ArrayList<>();
    for (final Atom atom : getConcept().getAtoms()) {
      atoms.add(new AtomJpa(atom, true));
    }

    final List<SemanticTypeComponent> stys = new ArrayList<>();
    for (final SemanticTypeComponent sty : getConcept().getSemanticTypes()) {
      stys.add(new SemanticTypeComponentJpa(sty));
    }

    final List<ConceptRelationship> relationships = new ArrayList<>();
    for (final ConceptRelationship rel : getConcept().getRelationships()) {
      relationships.add(new ConceptRelationshipJpa(rel, true));
    }

    List<ConceptRelationship> inverseRelationships =
        new ArrayList<>(inverseRelsMap.values());

    //
    // Any demotion relationship (AND its inverse) need to be removed
    //
    final Map<Atom, AtomRelationship> atomsDemotions = new HashMap<>();
    final Set<Atom> changedAtoms = new HashSet<>();
    for (final Atom atom : atoms) {
      for (final AtomRelationship atomRel : new ArrayList<>(
          atom.getRelationships())) {
        if (atomRel.getWorkflowStatus().equals(WorkflowStatus.DEMOTION)) {
          atomsDemotions.put(atom, atomRel);
          changedAtoms.add(atom);
          atom.getRelationships().remove(atomRel);

          final Atom inverseAtom = new AtomJpa(atomRel.getTo(), true);

          for (final AtomRelationship inverseRel : new ArrayList<>(
              inverseAtom.getRelationships())) {
            if (inverseRel.getTo().getId().equals(atom.getId()) && inverseRel
                .getWorkflowStatus().equals(WorkflowStatus.DEMOTION)) {
              atomsDemotions.put(inverseAtom, inverseRel);
              changedAtoms.add(inverseAtom);
              inverseAtom.getRelationships().remove(inverseRel);
            }
          }
        }
      }
    }

    //
    // Remove demotions from appropriate atoms
    //
    for (Map.Entry<Atom, AtomRelationship> atomDemotion : atomsDemotions
        .entrySet()) {
      Atom atom = atomDemotion.getKey();
      AtomRelationship demotion = atomDemotion.getValue();
      removeById(atom.getRelationships(), demotion.getId());
    }

    //
    // Update any atom that had demotion removed from it
    //
    for (final Atom atom : changedAtoms) {
      updateAtom(atom);
    }

    //
    // Remove the demotions from the database
    //
    for (final AtomRelationship demotion : atomsDemotions.values()) {
      removeRelationship(demotion.getId(), demotion.getClass());
    }

    //
    // Change status and update the components
    //

    // For each atom, set workflow status to READY_FOR_PUBLICATION
    for (final Atom atm : atoms) {
      if (!atm.getWorkflowStatus()
          .equals(WorkflowStatus.READY_FOR_PUBLICATION)) {
        atm.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);
      }
    }

    // For each semantic type component, set workflow status to
    // READY_FOR_PUBLICATION, and update Semantic type component
    for (final SemanticTypeComponent sty : stys) {
      if (!sty.getWorkflowStatus()
          .equals(WorkflowStatus.READY_FOR_PUBLICATION)) {
        sty.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);
      }
    }

    // For each relationship:
    // Change workflow status to READY_FOR_PUBLiCATION
    // Change relationshipType to RO if it is not B/RO, B/RB, B/RN, or XR
    final List<String> typeList =
        Arrays.asList("BRO", "BRB", "BRN", "RO", "RB", "RN", "XR");
    for (final ConceptRelationship rel : relationships) {
      if (!rel.getWorkflowStatus()
          .equals(WorkflowStatus.READY_FOR_PUBLICATION)) {
        rel.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);
      }
      if (!typeList.contains(rel.getRelationshipType())) {
        rel.setRelationshipType("RO");
      }
    }
    for (final ConceptRelationship inverseRel : inverseRelationships) {
      if (!inverseRel.getWorkflowStatus()
          .equals(WorkflowStatus.READY_FOR_PUBLICATION)) {
        inverseRel.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);
      }
      if (!typeList.contains(inverseRel.getRelationshipType())) {
        inverseRel.setRelationshipType("RO");
      }
    }

    //
    // Update modified objects
    //
    for (Atom atm : atoms) {
      updateAtom(atm);
    }
    for (SemanticTypeComponent sty : stys) {
      updateSemanticTypeComponent(sty, getConcept());
    }
    for (final ConceptRelationship rel : relationships) {
      updateRelationship(rel);
    }
    for (final ConceptRelationship inverseRel : inverseRelationships) {
      updateRelationship(inverseRel);
    }

    //
    // Change status of the concept
    //
    // Set workflow status to READY_FOR_PUBLICATION
    // Also set the lastApproved and lastApprovedBy,
    if (!getConcept().getWorkflowStatus()
        .equals(WorkflowStatus.READY_FOR_PUBLICATION)) {
      getConcept().setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);
    }

    getConcept().setLastApprovedBy(getLastModifiedBy());
    getConcept().setLastApproved(new Date());

    //
    // update the Concept
    //
    updateConcept(getConcept());
  }

  /* see superclass */
  @Override
  public void logAction() throws Exception {

    // log the REST calls
    addLogEntry(getLastModifiedBy(), getProject().getId(), getConcept().getId(),
        getActivityId(), getWorkId(), getName() + " concept "
            + getConcept().getId() + " " + getConcept().getName());

    // Log for the molecular action report
    addLogEntry(getLastModifiedBy(), getProject().getId(),
        getMolecularAction().getId(), getActivityId(), getWorkId(),
        "\nACTION  " + getName() + "\n  concept = " + getConcept().getId() + " "
            + getConcept().getName());

  }

}
