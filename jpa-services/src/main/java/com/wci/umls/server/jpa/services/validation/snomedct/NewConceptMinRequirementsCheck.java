/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.validation.snomedct;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.services.validation.AbstractValidationCheck;
import com.wci.umls.server.model.content.Concept;

/**
 * A sample validation check for a new concept meeting the minimum qualifying
 * criteria.
 */
public class NewConceptMinRequirementsCheck extends AbstractValidationCheck {

  /* see superclass */
  @Override
  public ValidationResult validate(Concept c) {
    ValidationResult result = new ValidationResultJpa();

    // TBD

    return result;
  }

  @Override
  public String getName() {
    return "New Concept Minimum Requirements Check";
  }

}
