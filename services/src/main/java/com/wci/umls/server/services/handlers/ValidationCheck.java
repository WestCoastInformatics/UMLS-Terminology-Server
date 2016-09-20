/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.services.handlers;

import java.util.Set;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.algo.action.MolecularActionAlgorithm;
import com.wci.umls.server.helpers.Configurable;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.services.ContentService;

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
   * Return all concepts failing the validation check with ids in the specified
   * set. This is an opportunity for a "batch" implementation of the check
   * across the entire database.
   * 
   * Use sets here because set operations are more important than ordering.
   *
   * @param conceptIds the concept ids
   * @param terminology the terminology
   * @param version the version
   * @param contentService the content service
   * @return the validation result
   * @throws Exception the exception
   */
  public Set<Long> validateConcepts(Set<Long> conceptIds, String terminology,
    String version, ContentService contentService) throws Exception;

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
