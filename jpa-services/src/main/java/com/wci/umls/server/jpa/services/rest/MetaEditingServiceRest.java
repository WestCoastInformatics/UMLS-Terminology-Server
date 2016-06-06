/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
/*
 * 
 */
package com.wci.umls.server.jpa.services.rest;

import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.SemanticTypeComponent;

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
  public Concept addSemanticType(Long projectId, Long conceptId, SemanticTypeComponent semanticTypeComponent, String authToken) throws Exception;

  /**
   * Remove semantic type.
   *
   * @param projectId the project id
   * @param conceptId the concept id
   * @param semanticTypeComponentId the semantic type component id
   * @param authToken the auth token
   * @return the concept
   * @throws Exception the exception
   */
  public Concept removeSemanticType(Long projectId, Long conceptId, Long semanticTypeComponentId, String authToken) throws Exception;


}
