/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.services;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.model.content.Concept;

/**
 * Generically represents a service for validating content.
 */
public interface ValidationService extends RootService {

  /**
   * Validate concept.
   *
   * @param concept the concept
   * @return the validation result
   */
  public ValidationResult validateConcept(Concept concept);

}