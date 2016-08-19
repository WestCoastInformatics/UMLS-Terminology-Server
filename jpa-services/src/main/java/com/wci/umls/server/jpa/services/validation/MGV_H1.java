/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.validation;

import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import com.wci.umls.server.Project;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.meta.Terminology;
import com.wci.umls.server.services.ContentService;

/**
 * Validates merges between two {@link Concept}s where both contain releasable
 * current version <code>MSH</code> {@link Atom}s with different {@link Code}s,
 * specifically (D-D, Q-Q, D-Q, Q-D). However, D-Q may exist together if the D
 * has termgroup EN, EP, or MH and the Q has termgroup GQ.
 *
 */
public class MGV_H1 extends AbstractValidationCheck {

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
    // Get target MSH atoms
    //
    List<Atom> target_atoms =
        target.getAtoms().stream().filter(a -> a.getTerminology().equals("MSH"))
            .collect(Collectors.toList());

    List<Atom> l_source_atoms =
        source_atoms.stream().filter(a -> a.getTerminology().equals("MSH"))
            .collect(Collectors.toList());

    //
    // Find cases where releasable current version MSH atoms with
    // different codes (in the specific combinations) are being merged.
    //
    Terminology sourceTerminology = null;
    Terminology targetTerminology = null;
    
    for (Atom sourceAtom : l_source_atoms) {
      try {
        sourceTerminology = service.getTerminology(
            sourceAtom.getTerminology(), sourceAtom.getVersion());
      } catch (Exception e) {
        result.getErrors().add(
            getName() + ": Terminology lookup failed for atom " + sourceAtom);
        return result;
      }      
      if (sourceAtom.isPublishable() && sourceTerminology.isCurrent()
          && (sourceAtom.getCodeId().toString().startsWith("D")
              || sourceAtom.getCodeId().toString().startsWith("Q"))) {
        for (Atom targetAtom : target_atoms) {
          try {
            targetTerminology = service.getTerminology(
                targetAtom.getTerminology(), sourceAtom.getVersion());
          } catch (Exception e) {
            result.getErrors().add(getName()
                + ": Terminology lookup failed for atom " + targetAtom);
            return result;
          }
          if (targetAtom.isPublishable() && targetTerminology.isCurrent()
              && (targetAtom.getCodeId().toString().startsWith("D")
                  || targetAtom.getCodeId().toString().startsWith("Q"))
              && !targetAtom.getCodeId().equals(sourceAtom.getCodeId())) {
            result.getErrors().add(
                getName() + ": Source and target concepts contain latest version publishable MSH atoms with different codes.");
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
    return "MGV_H1";
  }

}
