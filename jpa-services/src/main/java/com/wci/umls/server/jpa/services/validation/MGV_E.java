/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.validation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.wci.umls.server.Project;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.algo.action.MolecularActionAlgorithm;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.TypeKeyValue;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.action.AbstractMolecularAction;
import com.wci.umls.server.jpa.algo.action.MergeMolecularAction;
import com.wci.umls.server.jpa.algo.action.MoveMolecularAction;
import com.wci.umls.server.jpa.helpers.TypeKeyValueJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.DescriptorRelationship;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.services.ContentService;

/**
 * Validates merging between two {@link Concept}s. It is connected by an
 * approved, publishable, non RT?, SY, LK, SFO/LFO, BT?, or NT? current version
 * <code>MSH</code> {@link Relationship}.
 *
 */
public class MGV_E extends AbstractValidationCheck {

  /* see superclass */
  @Override
  public void setProperties(Properties p) {
    // n/a
  }

  /* see superclass */
  @SuppressWarnings("unused")
  @Override
  public ValidationResult validateAction(MolecularActionAlgorithm action) {
    ValidationResult result = new ValidationResultJpa();

    // Only run this check on merge and move actions
    if (!(action instanceof MergeMolecularAction
        || action instanceof MoveMolecularAction)) {
      return result;
    }

    final Project project = action.getProject();
    final ContentService service = (AbstractMolecularAction) action;
    final Concept source = (action instanceof MergeMolecularAction
        ? action.getConcept2() : action.getConcept());
    final Concept target = (action instanceof MergeMolecularAction
        ? action.getConcept() : action.getConcept2());
    final List<Atom> source_atoms = (action instanceof MoveMolecularAction
        ? ((MoveMolecularAction) action).getMoveAtoms() : source.getAtoms());

    //TODO - update (currently is direct copy of MGV_F)
    
    // Go through all the publishable atoms where terminology=MSH, and collect
    // all the unique descriptorIDs.
    // TypeKeyValue: Terminology, Version, DescriptorId
    Set<TypeKeyValue> descriptorIds = new HashSet<>();
    for (Atom a : source.getAtoms()) {
      if (a.isPublishable() && a.getTerminology().equals("MSH")) {
        descriptorIds.add(new TypeKeyValueJpa(a.getTerminology(),
            a.getVersion(), a.getDescriptorId()));
      }
    }

    // Load each descriptor, and get all relationships.
    List<DescriptorRelationship> relationshipList = new ArrayList<>();
    for (TypeKeyValue descriptorId : descriptorIds) {
      try {
        Descriptor d = service.getDescriptor(descriptorId.getValue(),
            descriptorId.getType(), descriptorId.getKey(), Branch.ROOT);
        relationshipList.addAll(d.getRelationships());
      } catch (Exception e) {
        result.getErrors().add(getName()
            + ": At least one atom in the source concept has an invalid descriptorId");
      }
    }

    // Get all descriptors on other side of relationships
    List<Descriptor> toDescriptors = new ArrayList<>();
    for (DescriptorRelationship rel : relationshipList) {
      toDescriptors.add((Descriptor) rel.getTo());
    }

    // Get all of the publishable atoms associated with those descriptors
    List<Atom> toAtoms = new ArrayList<>();
    for (Descriptor d : toDescriptors) {
      for (Atom toAtom : d.getAtoms()) {
        if (toAtom.isPublishable()) {
          toAtoms.add(toAtom);
        }
      }
    }

    // If any of those atoms are in target concept, fail.
    for (Atom toA : toAtoms) {
      if (target.getAtoms().contains(toA)) {
        result.getErrors().add(getName()
            + ": Concepts are connected by a publishable MSH relationship");
        return result;
      }
    }

    // TODO - go through the below with Brian, to make sure if anything from
    // MEME version needs to be included in the above

    // //
    // // Obtain publishable MSH relationships
    // //
    // List<ConceptRelationship> relationships =
    // source.getRelationships().stream()
    // .filter(r -> r.isPublishable() && r.getTerminology().equals("MSH"))
    // .collect(Collectors.toList());
    //
    // //
    // // Find current version MSH rel connecting source and target
    // //
    // for (ConceptRelationship rel : relationships) {
    // if (!rel.getName().equals("SFO/LFO") && !rel.getName().equals("RT?")
    // && !rel.getName().equals("BT?") && !rel.getName().equals("NT?")
    // && !rel.getName().equals("SY") && !rel.getName().equals("LK")
    // && (rel.getWorkflowStatus().equals(WorkflowStatus.PUBLISHED) || rel
    // .getWorkflowStatus().equals(WorkflowStatus.READY_FOR_PUBLICATION))
    // && rel.getTo().equals(target)) {
    // result.getErrors().add(getName()
    // + ": Concepts are connected by a publishable MSH relationship");
    // return result;
    // }
    // }

    return result;
  }

  /* see superclass */
  @Override
  public String getName() {
    return this.getClass().getSimpleName();
  }

}
