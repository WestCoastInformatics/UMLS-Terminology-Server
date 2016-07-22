/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.services.handlers;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.algo.action.MolecularActionAlgorithm;
import com.wci.umls.server.helpers.Configurable;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Descriptor;

/**
 * Represents a validation check on a concept.
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
   * Validate action.
   *
   * @param action the action
   * @return the validation result
   */
  public ValidationResult validateAction(MolecularActionAlgorithm action);

}
