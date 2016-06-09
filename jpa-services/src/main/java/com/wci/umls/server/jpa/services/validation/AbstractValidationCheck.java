/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.validation;

import java.util.List;
import java.util.Properties;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.services.handlers.ValidationCheck;

/**
 * Abstract validation check to make implementation easier.
 */
public abstract class AbstractValidationCheck implements ValidationCheck {

  /* see superclass */
  @Override
  public void setProperties(Properties p) {
    // n/a
  }

  /* see superclass */
  @Override
  public ValidationResult validate(Concept c) {
    ValidationResult result = new ValidationResultJpa();
    // no checks
    return result;
  }

  /* see superclass */
  @Override
  public ValidationResult validate(Descriptor descriptor) {
    ValidationResult result = new ValidationResultJpa();
    // no checks
    return result;
  }

  /* see superclass */
  @Override
  public ValidationResult validate(Code code) {
    ValidationResult result = new ValidationResultJpa();
    // no checks
    return result;
  }

  /* see superclass */
  @Override
  public ValidationResult validate(Atom atom) {
    ValidationResult result = new ValidationResultJpa();
    // no checks
    return result;
  }

  /* see superclass */
  @Override
  public ValidationResult validateMerge(Concept concept1, Concept concept2) {
    ValidationResult result = new ValidationResultJpa();
    // no checks
    return result;
  }

  /**
   * Validate split.
   *
   * @param concept the concept
   * @param atoms the atoms
   * @return the validation result
   */
  public ValidationResult validateSplit(Concept concept, List<Atom> atoms) {
    ValidationResult result = new ValidationResultJpa();
    // no checks
    return result;
  }

  /**
   * Validate move.
   *
   * @param concept1 the concept1
   * @param concept2 the concept2
   * @param atoms the atoms
   * @return the validation result
   */
  public ValidationResult validateMove(Concept concept1, Concept concept2,
    List<Atom> atoms) {
    ValidationResult result = new ValidationResultJpa();
    // no checks
    return result;
  }

  /**
   * Validate approve.
   *
   * @param concept the concept
   * @return the validation result
   */
  public ValidationResult validateApprove(Concept concept) {
    ValidationResult result = new ValidationResultJpa();
    // no checks
    return result;
  }

}
