/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package com.wci.umls.server.jpa.services.rest;

import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.model.content.Concept;

/**
 * Represents a service for supporting CRUD operations needed by integration
 * tests.
 */
public interface IntegrationTestServiceRest {

  /**
   * Adds the concept.
   *
   * @param concept the concept
   * @param authToken the auth token
   * @return the concept
   * @throws Exception the exception
   */
  public Concept addConcept(ConceptJpa concept, String authToken)
    throws Exception;

  /**
   * Removes the concept.
   *
   * @param conceptId the concept id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void removeConcept(Long conceptId, String authToken) throws Exception;

}
