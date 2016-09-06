/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.validation;

import java.util.List;
import java.util.Properties;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Concept;

/**
 * Validates those {@link Concept}s which contain multiple publishable
 * <code>MTH/PN</code> {@link Atom}s.
 *
 */
public class DT_PN2 extends AbstractValidationCheck {

  /* see superclass */
  @Override
  public void setProperties(Properties p) {
    // n/a
  }

  /* see superclass */
  @Override
  public ValidationResult validate(Concept source) {
    ValidationResult result = new ValidationResultJpa();

    if (source==null){
      return result;
    }
    
    //
    // Get atoms
    //
    List<Atom> atoms = source.getAtoms();

    //
    // Count publishable MTH/PN atoms
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
          .add(getName() + ": Concept contains multiple publishable MTH/PN Atoms");
      return result;
    }

    return result;
  }

  /* see superclass */
  @Override
  public String getName() {
    return this.getClass().getSimpleName();
  }

}
