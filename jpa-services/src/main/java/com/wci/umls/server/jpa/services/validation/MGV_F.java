/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.validation;

import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import com.wci.umls.server.Project;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.algo.action.MolecularActionAlgorithm;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.action.AbstractMolecularAction;
import com.wci.umls.server.jpa.algo.action.MergeMolecularAction;
import com.wci.umls.server.jpa.algo.action.MoveMolecularAction;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.model.workflow.WorkflowStatus;
import com.wci.umls.server.services.ContentService;

/**
 * Validates merging between two {@link Concept}s. It is connected by an
 * approved, publishable, non RT?, SY, LK, SFO/LFO, BT?, or NT? current version
 * <code>MSH</code> {@link Relationship}.
 *
 */
public class MGV_F extends AbstractValidationCheck {

  /* see superclass */
  @Override
  public void setProperties(Properties p) {
    // n/a
  }

  /* see superclass */
  @SuppressWarnings("unused")
  @Override
  public ValidationResult validateAction(MolecularActionAlgorithm action) {
    final Project project = action.getProject();
    final ContentService service = (AbstractMolecularAction) action;
    final Concept source = (action instanceof MergeMolecularAction
        ? action.getConcept2() : action.getConcept());
    final Concept target = (action instanceof MergeMolecularAction
        ? action.getConcept() : action.getConcept2());
    final List<Atom> source_atoms = (action instanceof MoveMolecularAction
        ? ((MoveMolecularAction)action).getMoveAtoms() : source.getAtoms());

    ValidationResult result = new ValidationResultJpa();

    // Go through all the publishable atoms where terminology=MSH, and collect
    // all the descriptorIDs.
    // Load each descriptor, and get all relationships.
    // Get all descriptors on other side of relationships
    // Get all of the publishable atoms associated with those descriptors
    // If any of those atoms are in target concept, fail.

    // Ignore MEME relationship getName checks.

    //
    // Obtain MSH relationships
    //
    List<ConceptRelationship> relationships = source.getRelationships().stream()
        .filter(r -> r.getTerminology().equals("MSH"))
        .collect(Collectors.toList());

    //
    // Find current version MSH rel connecting source and target
    //
    for (ConceptRelationship rel : relationships) {
      if (rel.isPublishable() && !rel.getName().equals("SFO/LFO")
          && !rel.getName().equals("RT?") && !rel.getName().equals("BT?")
          && !rel.getName().equals("NT?") && !rel.getName().equals("SY")
          && !rel.getName().equals("LK")
          && (rel.getWorkflowStatus().equals(WorkflowStatus.PUBLISHED) || 
              rel.getWorkflowStatus().equals(WorkflowStatus.READY_FOR_PUBLICATION))
          && rel.getTo().equals(target)) {
        result.getErrors().add(getName()
            + ": Concepts are connected by a publishable MSH relationship");
        return result;
      }
    }

    return result;
  }

  /* see superclass */
  @Override
  public String getName() {
    return "MGV_F";
  }

}
