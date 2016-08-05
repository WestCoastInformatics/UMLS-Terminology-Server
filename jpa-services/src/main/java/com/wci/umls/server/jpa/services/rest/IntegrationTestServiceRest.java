/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package com.wci.umls.server.jpa.services.rest;

import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.ConceptRelationshipJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.content.SemanticTypeComponent;
import com.wci.umls.server.model.workflow.Worklist;

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
   * Update concept.
   *
   * @param concept the concept
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void updateConcept(ConceptJpa concept, String authToken)
    throws Exception;

  /**
   * Removes the concept.
   *
   * @param conceptId the concept id
   * @param cascade the cascade
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void removeConcept(Long conceptId, boolean cascade, String authToken)
    throws Exception;

  /**
   * Update atom.
   *
   * @param atom the atom
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void updateAtom(AtomJpa atom, String authToken) throws Exception;

  /**
   * Adds the relationship.
   *
   * @param relationship the relationship
   * @param authToken the auth token
   * @return the concept relationship
   * @throws Exception the exception
   */
  public ConceptRelationship addRelationship(
    ConceptRelationshipJpa relationship, String authToken) throws Exception;

  /**
   * Returns the worklist.
   *
   * @param worklistId the worklist id
   * @param authToken the auth token
   * @return the worklist
   * @throws Exception the exception
   */
  public Worklist getWorklist(Long worklistId, String authToken)
    throws Exception;

  /**
   * Returns the atom.
   *
   * @param atomId the atom id
   * @param authToken the auth token
   * @return the atom
   * @throws Exception the exception
   */
  public Atom getAtom(Long atomId, String authToken) throws Exception;

  /**
   * Returns the semantic type component.
   *
   * @param styId the sty id
   * @param authToken the auth token
   * @return the semantic type component
   * @throws Exception the exception
   */
  public SemanticTypeComponent getSemanticTypeComponent(Long styId,
    String authToken) throws Exception;

  /**
   * Returns the concept relationship.
   *
   * @param relationshipId the relationship id
   * @param authToken the auth token
   * @return the concept relationship
   * @throws Exception the exception
   */
  public ConceptRelationship getConceptRelationship(Long relationshipId,
    String authToken) throws Exception;

  /**
   * Returns the attribute.
   *
   * @param attributeId the attribute id
   * @param authToken the auth token
   * @return the attribute
   * @throws Exception the exception
   */
  public Attribute getAttribute(Long attributeId, String authToken)
    throws Exception;

}
