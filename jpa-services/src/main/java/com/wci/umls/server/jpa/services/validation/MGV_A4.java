/*
 *    Copyright 2016 West Coast Informatics, LLC
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
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.services.ContentService;

/**
 * Validates merges between two {@link Concept}s that were published previously
 * with different CUIs.
 */
public class MGV_A4 extends AbstractValidationCheck {

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
    final List<Atom> sourceAtoms = (action instanceof MoveMolecularAction
        ? ((MoveMolecularAction) action).getMoveAtoms() : source.getAtoms());

    //
    // Obtain target atoms
    //
    final List<Atom> targetAtoms = target.getAtoms();

    //
    // Find publishable atom from source concept
    // having different last release cui from publishable
    // target concept atom.
    //
    final String projectTerminology = action.getProject().getTerminology();
    for (final Atom sourceAtom : sourceAtoms) {
      if ((sourceAtom.isPublishable()
          || sourceAtom.getTerminology().equals(source.getTerminology()))
          && sourceAtom.getConceptTerminologyIds()
              .get(projectTerminology) != null) {
        for (final Atom targetAtom : targetAtoms) {
          if ((targetAtom.isPublishable()
              || targetAtom.getTerminology().equals(target.getTerminology()))
              && targetAtom.getConceptTerminologyIds()
                  .get(projectTerminology) != null
              && !targetAtom.getConceptTerminologyIds().get(projectTerminology)
                  .equals(sourceAtom.getConceptTerminologyIds()
                      .get(projectTerminology))) {
            result.getErrors()
                .add(getName() + ": different last published cui.");
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
