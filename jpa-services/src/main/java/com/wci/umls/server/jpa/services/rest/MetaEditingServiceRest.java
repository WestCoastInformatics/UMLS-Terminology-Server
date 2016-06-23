/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
/*
 * 
 */
package com.wci.umls.server.jpa.services.rest;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.jpa.content.AttributeJpa;
import com.wci.umls.server.jpa.content.SemanticTypeComponentJpa;

/**
 * The Interface ContentServiceRest.
 */
public interface MetaEditingServiceRest {

  /**
   * Add semantic type.
   *
   * @param projectId the project id
   * @param conceptId the concept id
   * @param timestamp the timestamp representing concept's state
   * @param semanticTypeComponent the semantic type component
   * @param overrideWarnings whether to override warnings
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult addSemanticType(Long projectId, Long conceptId,
    Long timestamp, SemanticTypeComponentJpa semanticTypeComponent, boolean overrideWarnings,
    String authToken) throws Exception;

  /**
   * Remove semantic type.
   *
   * @param projectId the project id
   * @param conceptId the concept id
   * @param timestamp the timestamp representing concept's state
   * @param semanticTypeComponentId the semantic type component id
   * @param overrideWarnings whether to override warnings
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult removeSemanticType(Long projectId, Long conceptId,
    Long timestamp, Long semanticTypeComponentId, boolean overrideWarnings, String authToken)
      throws Exception;
 
  /**
   * Add attribute.
   *
   * @param projectId the project id
   * @param conceptId the concept id
   * @param timestamp the timestamp
   * @param attribute the attribute
   * @param overrideWarnings the override warnings
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult addAttribute(Long projectId, Long conceptId,
    Long timestamp, AttributeJpa attribute, boolean overrideWarnings,
    String authToken) throws Exception;

  
  /**
   * Remove attribute.
   *
   * @param projectId the project id
   * @param conceptId the concept id
   * @param timestamp the timestamp
   * @param attributeId the attribute id
   * @param overrideWarnings the override warnings
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult removeAttribute(Long projectId, Long conceptId,
    Long timestamp, Long attributeId, boolean overrideWarnings, String authToken)
      throws Exception;

}
