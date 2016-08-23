/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.validation;

import java.util.List;
import java.util.Properties;

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
 * Validates merges between two {@link Concept}s where one contains a previous
 * version <code>MSH/MH</code> {@link Atom} and the other contains a
 * publishable, latest version <code>MSH</code> {@link Atom} and their
 * {@link Code}s are different.
 *
 */
public class MGV_G extends AbstractValidationCheck {

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
    // Obtain target atoms
    //
    List<Atom> target_atoms = target.getAtoms();

    // Note: we use publishable flag to determine current/previous version
    // status

    for (Atom sourceAtom : source_atoms) {
      if (sourceAtom.getTerminology().equals("MSH")
          && sourceAtom.getTermType().equals("MH")) {
        for (Atom targetAtom : target_atoms) {
          if (targetAtom.getTerminology().equals("MSH")
              && targetAtom.getTermType().equals("MH")
              && ((targetAtom.isPublishable() && !sourceAtom.isPublishable())
                  || (!targetAtom.isPublishable()
                      && sourceAtom.isPublishable()))
              && !targetAtom.getCodeId().equals(sourceAtom.getCodeId())) {
            result.getErrors().add(getName()
                + ": Source and target concepts contain MSH MH atoms with different codes, and one is from current version while the other is not.");
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
