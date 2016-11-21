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
public class MGV_B extends AbstractValidationCheck {

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
    if (!(action instanceof MergeMolecularAction || action instanceof MoveMolecularAction)){
      return result;
    }
    
    final Project project = action.getProject();
    final ContentService service = (AbstractMolecularAction) action;
    final Concept source = (action instanceof MergeMolecularAction
        ? action.getConcept2() : action.getConcept());
    final Concept target = (action instanceof MergeMolecularAction
        ? action.getConcept() : action.getConcept2());
    final List<Atom> source_atoms = (action instanceof MoveMolecularAction
        ? ((MoveMolecularAction)action).getMoveAtoms() : source.getAtoms());

    //
    // Get source data
    //
    final List<TypeKeyValue> sources = project.getValidationDataFor(getName());

      if(sources==null){
        result.getErrors().add(
            getName() + ": Project has no source terminology data associated with this check.");
      }
    
    final List<String> terminologies =
        sources.stream().map(TypeKeyValue::getKey).collect(Collectors.toList());

    //
    // Get publishable atoms from specified list of sources
    //
    final List<Atom> target_atoms = target.getAtoms().stream()
        .filter(a -> terminologies.contains(a.getTerminology()) && a.isPublishable())
        .collect(Collectors.toList());

    final List<Atom> l_source_atoms = source_atoms.stream()
        .filter(a -> terminologies.contains(a.getTerminology()) && a.isPublishable())
        .collect(Collectors.toList());

    //
    // Look for cases of within-source merges involving publishable atoms.
    //
    for (Atom sourceAtom : l_source_atoms) {
        for (Atom targetAtom : target_atoms) {
            if (sourceAtom.getTerminology()
                .equals(targetAtom.getTerminology())) {
              result.getErrors().add(
                  getName() + ": Source and target concept contain atom(s) from Terminology " + sourceAtom.getTerminology());
              return result;
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
