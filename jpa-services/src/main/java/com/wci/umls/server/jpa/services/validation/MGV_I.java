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
import com.wci.umls.server.helpers.TypeKeyValue;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.action.AbstractMolecularAction;
import com.wci.umls.server.jpa.algo.action.MergeMolecularAction;
import com.wci.umls.server.jpa.algo.action.MoveMolecularAction;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.meta.Terminology;
import com.wci.umls.server.services.ContentService;

/**
 * Validates merges between two {@link Concept}s that both contain publishable
 * {@link Atom}s from the same {@link Terminology} and that {@link Terminology}
 * is listed in <code>ic_single</code>.
 *
 */
public class MGV_I extends AbstractValidationCheck {

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


    //
    // Get sources list
    //
    List<TypeKeyValue> sources = project.getValidationDataFor(getName());

    List<String> terminologies =
        sources.stream().map(TypeKeyValue::getKey).collect(Collectors.toList());

    //
    // Get target atoms from specified list of sources
    //
    List<Atom> target_atoms = target.getAtoms().stream()
        .filter(a -> terminologies.contains(a.getTerminology()))
        .collect(Collectors.toList());

    List<Atom> l_source_atoms = source_atoms.stream()
        .filter(a -> terminologies.contains(a.getTerminology()))
        .collect(Collectors.toList());

    //
    // Find cases of merges where the sources are the same but codes are
    // different.
    //
    for (Atom sourceAtom : l_source_atoms) {
      if (sourceAtom.isPublishable()) {
        for (Atom targetAtom : target_atoms) {
          if (targetAtom.isPublishable()
              && targetAtom.getTerminology().equals(sourceAtom.getTerminology())
              && !targetAtom.getCodeId().equals(sourceAtom.getCodeId())) {
            result.getErrors().add(getName()
                + ": Publishable atom in source concept has same Terminology but different CodeId as publishable atom in target concept.");
            return result;
          }
        }
      }
    }
    return result;
  }

  /* see superclass */
  @Override
  public String getName() {
    return "MGV_I";
  }

}
