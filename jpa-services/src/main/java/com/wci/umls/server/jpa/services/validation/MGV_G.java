/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.validation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.wci.umls.server.model.meta.Terminology;
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

    //
    // Find case where source and target both have current or previous version
    // MSH MH atoms with different codes. One should have current and the other
    // previous.
    //
    Boolean sourceTerminologyIsCurrent = false;
    Boolean targetTerminologyIsCurrent = false;

    // Cache previously looked up current-status of specific version of
    // terminology
    // Structure: Map<"TerminologyName|Version", True/False>
    Map<String, Boolean> terminologyVersionCurrent =
        new HashMap<String, Boolean>();
    
    for (Atom sourceAtom : source_atoms) {
      sourceTerminologyIsCurrent = terminologyVersionCurrent
          .get(sourceAtom.getTerminology() + "|" + sourceAtom.getVersion());
      // If this Terminology|Version has not been looked up before, do it now
      if (sourceTerminologyIsCurrent == null) {
        try {
          Terminology tempTerm = 
              service.getTerminology(sourceAtom.getTerminology(),
                  sourceAtom.getVersion());
          sourceTerminologyIsCurrent = tempTerm.isCurrent();
          terminologyVersionCurrent.put(tempTerm.getTerminology()+"|"+ tempTerm.getVersion() , tempTerm.isCurrent());
        } catch (Exception e) {
          result.getErrors().add(
              getName() + ": Terminology lookup failed for atom " + sourceAtom);
          return result;
        }
      }
      if (sourceAtom.getTerminology().equals("MSH")
          && sourceAtom.getTermType().equals("MH")
          && sourceAtom.isPublishable()) {
        for (Atom targetAtom : target_atoms) {
          targetTerminologyIsCurrent = terminologyVersionCurrent
              .get(targetAtom.getTerminology() + "|" + targetAtom.getVersion());
          // If this Terminology|Version has not been looked up before, do it now
          if (targetTerminologyIsCurrent == null) {
            try {
              Terminology tempTerm = 
                  service.getTerminology(targetAtom.getTerminology(),
                      targetAtom.getVersion());
              targetTerminologyIsCurrent = tempTerm.isCurrent();
              terminologyVersionCurrent.put(tempTerm.getTerminology()+"|"+ tempTerm.getVersion() , tempTerm.isCurrent());
            } catch (Exception e) {
              result.getErrors().add(
                  getName() + ": Terminology lookup failed for atom " + targetAtom);
              return result;
            }
          }
          if (targetAtom.getTerminology().equals("MSH")
              && targetAtom.getTermType().equals("MH")
                  & targetAtom.isPublishable()
              && ((targetTerminologyIsCurrent
                  && !sourceTerminologyIsCurrent)
                  || (!targetTerminologyIsCurrent
                      && sourceTerminologyIsCurrent))
              && !targetAtom.getCodeId().equals(sourceAtom.getCodeId())) {
            result.getErrors().add(getName()
                + ": Source and target concepts contain publishable MSH MH atoms with different codes, and one is from current version while the other is not.");
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
