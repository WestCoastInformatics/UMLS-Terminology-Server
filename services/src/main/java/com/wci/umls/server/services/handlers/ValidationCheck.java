/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.services.handlers;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.Configurable;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Descriptor;

/**
 * Generically represents a validation check on a concept.
 */
public interface ValidationCheck extends Configurable {

  /**
   * Validates the concept.
   *
   * @param concept the concept
   * @return the validation result
   */
  public ValidationResult validate(Concept concept);

  /**
   * Validates the descriptor.
   *
   * @param descriptor the descriptor
   * @return the validation result
   */
  public ValidationResult validate(Descriptor descriptor);

  /**
   * Validates the code.
   *
   * @param code the code
   * @return the validation result
   */
  public ValidationResult validate(Code code);

  /**
   * Validates the atom.
   *
   * @param atom the atom
   * @return the validation result
   */
  public ValidationResult validate(Atom atom);

  /**
   * Validates the concept merge.
   *
   * @param concept1 the concept1
   * @param concept2 the concept2
   * @return the validation result
   */
  public ValidationResult validateMerge(Concept concept1,
    Concept concept2);

}
