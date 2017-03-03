/*
 *    Copyright 2016 West Coast Informatics, LLC
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
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.services.ContentService;

/**
 * Validates merges between two {@link Concept}s where both contain publishable
 * current version <code>MSH</code> {@link Atom}s with different {@link Code}s,
 * specifically (D-D, Q-Q, D-Q, Q-D, Q-C, C-Q). However, D-Q may exist together
 * if the D has termgroup EN, EP, or MH and the Q has termgroup GQ.
 *
 */
public class MGV_H1 extends AbstractValidationCheck {

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

    //
    // Get publishable MSH atoms
    //
    List<Atom> target_atoms = target.getAtoms().stream()
        .filter(a -> a.getTerminology().equals("MSH") && a.isPublishable())
        .collect(Collectors.toList());

    List<Atom> l_source_atoms = source_atoms.stream()
        .filter(a -> a.getTerminology().equals("MSH") && a.isPublishable())
        .collect(Collectors.toList());

    //
    // Find cases where publishable current version MSH atoms with
    // different codes (in the specific combinations) are being merged.
    //

    for (Atom sourceAtom : l_source_atoms) {
      if (sourceAtom.getCodeId().toString().startsWith("D")
          || sourceAtom.getCodeId().toString().startsWith("Q")
          || sourceAtom.getCodeId().toString().startsWith("C")) {
        for (Atom targetAtom : target_atoms) {
          if ((targetAtom.getCodeId().toString().startsWith("D")
              || targetAtom.getCodeId().toString().startsWith("Q")
              || targetAtom.getCodeId().toString().startsWith("C")) &&
          // Make sure not to fire on any of the MGV_H2 Code-combinations
              !((sourceAtom.getCodeId().toString().startsWith("C")
                  && targetAtom.getCodeId().toString().startsWith("C"))
                  || (sourceAtom.getCodeId().toString().startsWith("C")
                      && targetAtom.getCodeId().toString().startsWith("D"))
                  || (sourceAtom.getCodeId().toString().startsWith("D")
                      && targetAtom.getCodeId().toString().startsWith("C")))
              && !targetAtom.getCodeId().equals(sourceAtom.getCodeId())) {
            result.getErrors().add(getName()
                + ": Source and target concepts contain latest version publishable MSH atoms with different codes.");
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
    return this.getClass().getSimpleName();
  }

}
