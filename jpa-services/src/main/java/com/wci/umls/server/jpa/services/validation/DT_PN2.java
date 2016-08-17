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
 * Validates those {@link Concept}s which contain multiple releasable
 * <code>MTH/PN</code> {@link Atom}s.
 *
 */
public class DT_PN2 extends AbstractValidationCheck {

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
   * @return the validation result
   */
  public ValidationResult validate(Project project, ContentService service,
    Concept source) {
    ValidationResult result = new ValidationResultJpa();

    //
    // Get atoms
    //
    List<Atom> atoms = source.getAtoms();

    //
    // Count releasable MTH/PN atoms
    //
    int l_ctr = 0;
    for (Atom atom : atoms) {
      if (atom.getTerminology().equals("MTH") && atom.getTermType().equals("PN")
          && atom.isPublishable()) {
        l_ctr++;
      }
    }

    //
    // Are there more than one?
    //
    if (l_ctr > 1) {
      result.getErrors()
          .add(getName() + ": Concept contains multiple releasable MTH/PN Atoms");
      return result;
    }

    return result;
  }

  /* see superclass */
  @Override
  public String getName() {
    return "DT_PN2";
  }

}
