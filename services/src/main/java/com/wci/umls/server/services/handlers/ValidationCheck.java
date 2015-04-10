/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.services.handlers;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.Configurable;
import com.wci.umls.server.model.content.Concept;

/**
 * Generically represents a validation check on a concept
 */
public interface ValidationCheck extends Configurable {

  /**
   * Validates the concept.
   *
   * @param c the c
   * @return the validation result
   */
  public ValidationResult validate(Concept c);
  
}
