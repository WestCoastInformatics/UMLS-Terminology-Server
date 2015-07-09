/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.validation;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

  /* (non-Javadoc)
   * @see com.wci.umls.server.jpa.services.validation.AbstractValidationCheck#validate(com.wci.umls.server.model.content.Atom)
   */
  @Override
  public ValidationResult validate(Atom atom) {
    ValidationResult result = new ValidationResultJpa();

    if (atom == null) {     
      return null;
    }
    
    if (atom.getName() == null) {
       result.addError("Atom does not have a preferred name.");
       return result;
    }
    
    // Check for leading whitespace
    if (atom.getName().length() > 0 && Character.isWhitespace(atom.getName().charAt(0))) {
      result.addError("Atom name contains leading whitespace.");
    }

    // Check for trailing whitespace
    if (atom.getName().length() > 0 && Character.isWhitespace(
        atom.getName().charAt(atom.getName().length() - 1))) {
      result.addError("Atom name contains trailing whitespace.");
    }

    // Check for duplicate whitespace
    Pattern pattern = Pattern.compile("(\\s)(\\s)");
    Matcher matcher = pattern.matcher(atom.getName());
    if (matcher.find()) {
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
