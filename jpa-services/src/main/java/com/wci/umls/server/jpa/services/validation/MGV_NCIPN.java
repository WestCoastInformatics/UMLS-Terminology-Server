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
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.services.ContentService;

/**
 * Validates merges between {@link Concept}s where both contain releasable
 * <code>MTH/NCIPN</code> {@link Atom}s.
 *
 */
public class MGV_NCIPN extends AbstractValidationCheck {

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
    // Obtain NCIMTH atoms
    //
    List<Atom> target_atoms = target.getAtoms().stream()
        .filter(a -> a.getTerminology().equals("NCIMTH"))
        .collect(Collectors.toList());

    List<Atom> l_source_atoms =
        source_atoms.stream().filter(a -> a.getTerminology().equals("NCIMTH"))
            .collect(Collectors.toList());

    //
    // Find releasable MTH/NCIPNs in both source and target concepts
    //
    for (Atom sourceAtom : l_source_atoms) {
      if (sourceAtom.isPublishable()) {
        for (Atom targetAtom : target_atoms) {
          if (targetAtom.isPublishable()
              && sourceAtom.getTermType().equals("PN")
              && targetAtom.getTermType().equals("PN")) {
            result.getErrors().add(
                getName() + ": Source and target concepts contain publishable MTH/NCIPN atoms.");
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
    return "MGV_NCIPN";
  }

}
