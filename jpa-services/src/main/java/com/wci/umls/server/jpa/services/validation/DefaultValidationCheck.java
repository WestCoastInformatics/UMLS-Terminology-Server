/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.validation;

import java.util.Properties;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Concept;

/**
 * Default checks that apply to all terminologies.
 */
public class DefaultValidationCheck extends AbstractValidationCheck {

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.helpers.Configurable#setProperties(java.util.Properties
   * )
   */
  @Override
  public void setProperties(Properties p) {
    // n/a
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.handlers.ValidationCheck#validate(com.wci.
   * umls.server.model.content.Concept)
   */
  @Override
  public ValidationResult validate(Concept c) {
    ValidationResult result = new ValidationResultJpa();
    // tbd
    return result;
  }

  @Override
  public ValidationResult validate(Atom atom) {
    ValidationResult result = new ValidationResultJpa();

    // Check for leading whitespace
    if (atom.getName().matches("^\\s")) {
      result.addError("Atom name contains leading whitespace.");
    }

    // Check for trailing whitespace
    if (atom.getName().matches("\\s$")) {
      result.addError("Atom name contains trailing whitespace.");
    }

    // Check for duplicate whitespace
    if (atom.getName().matches("\\s\\s")) {
      result.addError("Atom name contains duplicate whitespace.");
    }

    // Check for disallowed whitespace
    if (atom.getName().indexOf("\t") != -1
        || atom.getName().indexOf("\r") != -1
        || atom.getName().indexOf("\n") != -1
        // &nbsp;
        || atom.getName().indexOf("\u00A0") != -1
        // zero-width space
        || atom.getName().indexOf("\u200B") != -1) {
      result.addError("Atom name contains invalid whitespace.");
    }

    return result;

  }

}
