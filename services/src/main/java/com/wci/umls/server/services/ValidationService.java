/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.services;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Descriptor;

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
  
  /**
   * Validate atom.
   *
   * @param atom the atom
   * @return the validation result
   */
  public ValidationResult validateAtom(Atom atom);
  
  /**
   * Validate descriptor.
   *
   * @param descriptor the descriptor
   * @return the validation result
   */
  public ValidationResult validateDescriptor(Descriptor descriptor);
  
  /**
   * Validate code.
   *
   * @param code the code
   * @return the validation result
   */
  public ValidationResult validateCode(Code code);
  
  /**
   * Validate merge.
   *
   * @param concept1 the concept1
   * @param concept2 the concept2
   * @return the validation result
   */
  public ValidationResult validateMerge(Concept concept1, Concept concept2);

}