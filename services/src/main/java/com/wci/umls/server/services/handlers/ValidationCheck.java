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
   * @param terminologyId1 the terminology id1
   * @param terminologyId2 the terminology id2
   * @param terminology the terminology
   * @param version the version
   * @return the validation result
   */
  public ValidationResult validateMerge(String terminologyId1,
    String terminologyId2, String terminology, String version) ;

}
