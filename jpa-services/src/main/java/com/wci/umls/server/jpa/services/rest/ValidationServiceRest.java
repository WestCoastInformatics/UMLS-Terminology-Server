/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.rest;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.KeyValuePairList;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.CodeJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.DescriptorJpa;

/**
 * Represents a service for validating content.
 */
public interface ValidationServiceRest {

  /**
   * Validates the specified concept. Checks are defined the "run.config.umls"
   * setting for the deployed server.
   *
   * @param concept the concept
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult validateConcept(ConceptJpa concept, String authToken)
    throws Exception;

  /**
   * Validate atom.
   *
   * @param atom the atom
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult validateAtom(AtomJpa atom, String authToken)
    throws Exception;

  /**
   * Validate descriptor.
   *
   * @param descriptor the descriptor
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult validateDescriptor(DescriptorJpa descriptor,
    String authToken) throws Exception;

  /**
   * Validate code.
   *
   * @param code the code
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult validateCode(CodeJpa code, String authToken)
    throws Exception;

  /**
   * Validate merge.
   *
   * @param terminology the terminology
   * @param version the version
   * @param cui1 the cui1
   * @param cui2 the cui2
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult validateMerge(String terminology, String version,
    String cui1, String cui2, String authToken) throws Exception;

  /**
   * Gets the validation checks.
   *
   * @param authToken the auth token
   * @return the validation checks
   * @throws Exception the exception
   */
  public KeyValuePairList getValidationChecks(String authToken)
    throws Exception;
}
