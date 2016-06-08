/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
/*
 * 
 */
package com.wci.umls.server.jpa.services.rest;

import com.wci.umls.server.jpa.content.SemanticTypeComponentJpa;
import com.wci.umls.server.model.content.Concept;

/**
 * The Interface ContentServiceRest.
 */
public interface MetaEditingServiceRest {

  /**
   * Add semantic type.
   *
   * @param projectId the project id
   * @param conceptId the concept id
   * @param semanticTypeComponent the semantic type component
   * @param authToken the auth token
   * @return the concept
   * @throws Exception the exception
   */
  public Concept addSemanticType(Long projectId, Long conceptId, SemanticTypeComponentJpa semanticTypeComponent, String authToken) throws Exception;

  /**
   * Remove semantic type.
   *
   * @param projectId the project id
   * @param conceptId the concept id
   * @param semanticTypeComponentId the semantic type component id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void removeSemanticType(Long projectId, Long conceptId, Long semanticTypeComponentId, String authToken) throws Exception;


}
