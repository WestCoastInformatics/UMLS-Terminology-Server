/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.validation;

import java.util.List;
import java.util.Properties;

import com.wci.umls.server.Project;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.jpa.ValidationResultJpa;
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

  /**
   * Validate.
   *
   * @param project the project
   * @param service the service
   * @param source the source
   * @param target the target
   * @param source_atoms the source atoms
   * @return the validation result
   */
  public ValidationResult validate(Project project, ContentService service,
    Concept source, Concept target, List<Atom> source_atoms) {
    ValidationResult result = new ValidationResultJpa();

    //
    // Obtain target atoms
    //
    List<Atom> target_atoms = target.getAtoms();

    //
    // Find releasable atom from source concept
    // having different last release cui from releasable
    // target concept atom.
    //
    for (Atom sourceAtom : source_atoms) {
      if (sourceAtom.isPublishable() && sourceAtom.getConceptTerminologyIds()
          .get(source.getTerminology()) != null) {
        for (Atom targetAtom : target_atoms) {
          if (targetAtom.isPublishable()
              && targetAtom.getConceptTerminologyIds()
                  .get(source.getTerminology()) != null
              && !targetAtom.getConceptTerminologyIds()
                  .get(source.getTerminology())
                  .equals(sourceAtom.getConceptTerminologyIds()
                      .get(source.getTerminology()))) {
            result.getErrors().add(getName()
                + ": Publishable atom in source concept has different Concept Terminology Id than publishable atom in target concept.");
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
    return "MGV_A4";
  }

}
