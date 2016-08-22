/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package com.wci.umls.server.jpa.services.rest;

import java.util.List;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.AttributeJpa;
import com.wci.umls.server.jpa.content.ConceptRelationshipJpa;
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
   * @param activityId the activity id
   * @param lastModified the last modified
   * @param semanticTypeValue the semantic type value
   * @param overrideWarnings whether to override warnings
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult addSemanticType(Long projectId, Long conceptId,
    String activityId, Long lastModified,
    String semanticTypeValue, boolean overrideWarnings,
    String authToken) throws Exception;

  /**
   * Remove semantic type.
   *
   * @param projectId the project id
   * @param conceptId the concept id
   * @param activityId the activity id
   * @param lastModified the last modified
   * @param semanticTypeComponentId the semantic type component id
   * @param overrideWarnings whether to override warnings
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult removeSemanticType(Long projectId, Long conceptId,
    String activityId, Long lastModified, Long semanticTypeComponentId,
    boolean overrideWarnings, String authToken) throws Exception;

  /**
   * Add attribute.
   *
   * @param projectId the project id
   * @param conceptId the concept id
   * @param activityId the activity id
   * @param lastModified the last modified
   * @param attribute the attribute
   * @param overrideWarnings the override warnings
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult addAttribute(Long projectId, Long conceptId,
    String activityId, Long lastModified, AttributeJpa attribute,
    boolean overrideWarnings, String authToken) throws Exception;

  /**
   * Remove attribute.
   *
   * @param projectId the project id
   * @param conceptId the concept id
   * @param activityId the activity id
   * @param lastModified the last modified
   * @param attributeId the attribute id
   * @param overrideWarnings the override warnings
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult removeAttribute(Long projectId, Long conceptId,
    String activityId, Long lastModified, Long attributeId,
    boolean overrideWarnings, String authToken) throws Exception;

  /**
   * Adds the atom.
   *
   * @param projectId the project id
   * @param conceptId the concept id
   * @param activityId the activity id
   * @param lastModified the last modified
   * @param atom the atom
   * @param overrideWarnings the override warnings
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult addAtom(Long projectId, Long conceptId,
    String activityId, Long lastModified, AtomJpa atom,
    boolean overrideWarnings, String authToken) throws Exception;

  /**
   * Removes the atom.
   *
   * @param projectId the project id
   * @param conceptId the concept id
   * @param activityId the activity id
   * @param lastModified the last modified
   * @param atomId the atom id
   * @param overrideWarnings the override warnings
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult removeAtom(Long projectId, Long conceptId,
    String activityId, Long lastModified, Long atomId, boolean overrideWarnings,
    String authToken) throws Exception;

  /**
   * Update atom.
   *
   * @param projectId the project id
   * @param conceptId the concept id
   * @param activityId the activity id
   * @param lastModified the last modified
   * @param atom the atom
   * @param overrideWarnings the override warnings
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult updateAtom(Long projectId, Long conceptId,
    String activityId, Long lastModified, AtomJpa atom,
    boolean overrideWarnings, String authToken) throws Exception;

  /**
   * Adds the relationship.
   *
   * @param projectId the project id
   * @param conceptId the from concept id
   * @param activityId the activity id
   * @param lastModified the last modified
   * @param relationship the relationship
   * @param overrideWarnings the override warnings
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult addRelationship(Long projectId, Long conceptId,
    String activityId, Long lastModified, ConceptRelationshipJpa relationship,
    boolean overrideWarnings, String authToken) throws Exception;

  /**
   * Removes the relationship.
   *
   * @param projectId the project id
   * @param conceptId the from concept id
   * @param activityId the activity id
   * @param lastModified the last modified
   * @param relationshipId the relationship id
   * @param overrideWarnings the override warnings
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult removeRelationship(Long projectId, Long conceptId,
    String activityId, Long lastModified, Long relationshipId,
    boolean overrideWarnings, String authToken) throws Exception;

  /**
   * Merge concepts.
   *
   * @param projectId the project id
   * @param conceptId the concept id
   * @param activityId the activity id
   * @param lastModified the last modified
   * @param conceptId2 the concept id 2
   * @param overrideWarnings the override warnings
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult mergeConcepts(Long projectId, Long conceptId,
    String activityId, Long lastModified, Long conceptId2,
    boolean overrideWarnings, String authToken) throws Exception;

  /**
   * Move atoms.
   *
   * @param projectId the project id
   * @param conceptId the concept id
   * @param activityId the activity id
   * @param lastModified the last modified
   * @param conceptId2 the concept id 2
   * @param atomIds the atom ids
   * @param overrideWarnings the override warnings
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult moveAtoms(Long projectId, Long conceptId,
    String activityId, Long lastModified, Long conceptId2, List<Long> atomIds,
    boolean overrideWarnings, String authToken) throws Exception;

  /**
   * Split concept.
   *
   * @param projectId the project id
   * @param conceptId the concept id
   * @param activityId the activity id
   * @param lastModified the last modified
   * @param atomIds the atom ids
   * @param overrideWarnings the override warnings
   * @param copyRelationships the copy relationships
   * @param copySemanticTypes the copy semantic types
   * @param relationshipType the relationship type
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult splitConcept(Long projectId, Long conceptId,
    String activityId, Long lastModified, List<Long> atomIds,
    boolean overrideWarnings, boolean copyRelationships,
    boolean copySemanticTypes, String relationshipType, String authToken)
    throws Exception;

  /**
   * Approve concept.
   *
   * @param projectId the project id
   * @param conceptId the concept id
   * @param activityId the activity id
   * @param lastModified the last modified
   * @param overrideWarnings the override warnings
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult approveConcept(Long projectId, Long conceptId,
    String activityId, Long lastModified, boolean overrideWarnings,
    String authToken) throws Exception;

  /**
   * Undo action.
   *
   * @param projectId the project id
   * @param molecularActionId the molecular action id
   * @param activityId the activity id
   * @param force the force
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult undoAction(Long projectId, Long molecularActionId,
    String activityId, boolean force, String authToken) throws Exception;

  /**
   * Redo action.
   *
   * @param projectId the project id
   * @param molecularActionId the molecular action id
   * @param activityId the activity id
   * @param force the force
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult redoAction(Long projectId, Long molecularActionId,
    String activityId, boolean force, String authToken) throws Exception;

}
