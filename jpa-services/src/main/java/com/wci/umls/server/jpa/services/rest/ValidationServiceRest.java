/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.rest;


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
//  public ValidationResult validateConcept(ConceptJpa concept, String authToken)
//    throws Exception;

}
