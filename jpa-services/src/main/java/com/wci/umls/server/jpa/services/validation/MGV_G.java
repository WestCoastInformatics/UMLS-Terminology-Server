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
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.meta.Terminology;
import com.wci.umls.server.services.ContentService;

/**
 * Validates merges between two {@link Concept}s where one contains a previous
 * version <code>MSH/MH</code> {@link Atom} and the other contains a releasable,
 * latest version <code>MSH</code> {@link Atom} and their {@link Code}s are
 * different.
 *
 */
public class MGV_G extends AbstractValidationCheck {

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
    // Find case where source and target both have current or previous version
    // MSH
    // MH atoms with different codes. One should have current and the other
    // previous.
    //
    Terminology sourceTerminology = null;
    Terminology targetTerminology = null;

    for (Atom sourceAtom : source_atoms) {
      try {
        sourceTerminology = service.getTerminology(sourceAtom.getTerminology(),
            sourceAtom.getVersion());
      } catch (Exception e) {
        result.getErrors().add(
            getName() + ": Terminology lookup failed for atom " + sourceAtom);
        return result;
      }
      if (sourceAtom.getTerminology().equals("MSH")
          && sourceAtom.getTermType().equals("MH")
          && sourceAtom.isPublishable()) {
        for (Atom targetAtom : target_atoms) {
          try {
            targetTerminology = service.getTerminology(
                targetAtom.getTerminology(), sourceAtom.getVersion());
          } catch (Exception e) {
            result.getErrors().add(getName()
                + ": Terminology lookup failed for atom " + targetAtom);
            return result;
          }
          if (targetAtom.getTerminology().equals("MSH")
              && targetAtom.getTermType().equals("MH")
                  & targetAtom.isPublishable()
              && ((targetTerminology.isCurrent()
                  && !sourceTerminology.isCurrent())
                  || (!targetTerminology.isCurrent()
                      && sourceTerminology.isCurrent()))
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
    return "MGV_G";
  }

}
