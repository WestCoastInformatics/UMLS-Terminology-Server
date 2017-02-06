/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package com.wci.umls.server.jpa.services.rest;

import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.meta.SemanticTypeJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.SemanticTypeComponent;

/**
 * Represents a service for managing content.
 */
public interface SimpleEditServiceRest {

  /**
   * Adds an atom to the specified concept.
   *
   * @param projectId the project id
   * @param conceptId the concept id
   * @param atom the atom
   * @param authToken the auth token
   * @return the atom
   * @throws Exception the exception
   */
  public Atom addAtomToConcept(Long projectId, Long conceptId, AtomJpa atom,
    String authToken) throws Exception;

  /**
   * Update atom.
   *
   * @param projectId the project id
   * @param conceptId the concept id
   * @param atom the atom
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void updateAtom(Long projectId, Long conceptId, AtomJpa atom,
    String authToken) throws Exception;

  /**
   * Removes the atom.
   *
   * @param projectId the project id
   * @param conceptId the concept id
   * @param atomId the atom id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void removeAtom(Long projectId, Long conceptId, Long atomId,
    String authToken) throws Exception;

  /**
   * Adds the semantic type to concept.
   *
   * @param projectId the project id
   * @param conceptId the concept id
   * @param semanticType the semantic type
   * @param authToken the auth token
   * @return the semantic type component
   * @throws Exception the exception
   */
  public SemanticTypeComponent addSemanticTypeToConcept(Long projectId,
    Long conceptId, SemanticTypeJpa semanticType, String authToken)
    throws Exception;

  /**
   * Removes the semantic type.
   *
   * @param projectId the project id
   * @param conceptId the concept id
   * @param semanticTypeId the semantic type id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void removeSemanticType(Long projectId, Long conceptId,
    Long semanticTypeId, String authToken) throws Exception;

  /**
   * Adds the concept.
   *
   * @param projectId the project id
   * @param concept the concept
   * @param authToken the auth token
   * @return the concept
   * @throws Exception the exception
   */
  public Concept addConcept(Long projectId, ConceptJpa concept,
    String authToken) throws Exception;

  /**
   * Update concept.
   *
   * @param projectId the project id
   * @param concept the concept
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void updateConcept(Long projectId, ConceptJpa concept,
    String authToken) throws Exception;

  /**
   * Removes the concept.
   *
   * @param projectId the project id
   * @param conceptId the concept id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void removeConcept(Long projectId, Long conceptId, String authToken)
    throws Exception;

  /**
   * Removes the concepts.
   *
   * @param projectId the project id
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void removeConcepts(Long projectId, String query, PfsParameterJpa pfs,
    String authToken) throws Exception;
}
