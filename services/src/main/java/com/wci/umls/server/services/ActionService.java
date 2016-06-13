/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.services;

import com.wci.umls.server.Project;
import com.wci.umls.server.helpers.KeyValuesMap;
import com.wci.umls.server.helpers.content.RelationshipList;
import com.wci.umls.server.model.actions.AtomicAction;
import com.wci.umls.server.model.actions.MolecularAction;

/**
 * Represents a service for performing maintenance actions.
 */
public interface ActionService extends RootService {

  /**
   * Enable listeners.
   */
  public void enableListeners();

  /**
   * Disable listeners.
   */
  public void disableListeners();

  /**
   * Configure action service.
   *
   * @param project the project
   * @return the sessionToken
   * @throws Exception the exception
   */
  public String configureActionService(Project project) throws Exception;

  /**
   * Clear all resources for the specified token.
   *
   * @param sessionToken the session token
   */
  public void clear(String sessionToken);

  /**
   * Gets the progress for any currently-running operations for the specified
   * session token.
   *
   * @param sessionToken the session token
   * @return the progress
   * @throws Exception the exception
   */
  public float getProgress(String sessionToken) throws Exception;

  /**
   * Cancels any currently-running operations for the specified session token.
   *
   * @param sessionToken the session token
   * @throws Exception the exception
   */
  public void cancel(String sessionToken) throws Exception;

  /**
   * Prepares data structures for full classification. This mostly involves
   * building classifier axioms from the data. In theory, this only needs to be
   * done once per session (assuming only add operations).
   *
   * @param sessionToken the session token
   * @throws Exception the exception
   */
  public void prepareToClassify(String sessionToken) throws Exception;

  /**
   * Verifies that “prepare” successfully completed, and performs a full
   * classification, leaving the classified ontology in memory for later
   * retrieval.
   *
   * @param sessionToken the session token
   * @throws Exception the exception
   */
  public void classify(String sessionToken) throws Exception;

  /**
   * Verifies that “prepare” and a full classification were performed, obtains
   * changes since last classification run, adds needed axioms, and performs an
   * incremental classification. Note: incremental classification is not
   * supported if changes include retirement or removal of content – only
   * additions are supported.
   *
   * @param sessionToken the session token
   * @throws Exception the exception
   */
  public void incrementalClassify(String sessionToken) throws Exception;

  /**
   * Gets the classification equivalents.
   *
   * @param sessionToken the session token
   * @return the classification equivalents
   * @throws Exception the exception
   */
  public KeyValuesMap getClassificationEquivalents(String sessionToken)
    throws Exception;

  /**
   * Gets the old inferred relationships.
   *
   * @param sessionToken the session token
   * @return the old inferred relationships
   * @throws Exception the exception
   */
  public RelationshipList getOldInferredRelationships(String sessionToken)
    throws Exception;

  /**
   * Gets the new inferred relationships.
   *
   * @param sessionToken the session token
   * @return the new inferred relationships
   * @throws Exception the exception
   */
  public RelationshipList getNewInferredRelationships(String sessionToken)
    throws Exception;

  /**
   * Adds the new inferred relationships.
   *
   * @param sessionToken the session token
   * @throws Exception the exception
   */
  public void addNewInferredRelationships(String sessionToken) throws Exception;

  /**
   * Retire old inferred relationships. Removes not-yet-published inferred
   * relationships.
   *
   * @param sessionToken the session token
   * @throws Exception the exception
   */
  public void retireOldInferredRelationships(String sessionToken)
    throws Exception;

  /**
   * Add molecular action.
   *
   * @param action the action
   * @return the molecular action
   * @throws Exception the exception
   */
  public MolecularAction addMolecularAction(MolecularAction action)
    throws Exception;

  /**
   * Update molecular action.
   *
   * @param action the action
   * @throws Exception the exception
   */
  public void updateMolecularAction(MolecularAction action) throws Exception;

  /**
   * Remove molecular action.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeMolecularAction(Long id) throws Exception;

  /**
   * Gets the molecular action.
   *
   * @param id the id
   * @return the molecular action
   * @throws Exception the exception
   */
  public MolecularAction getMolecularAction(Long id) throws Exception;

  /**
   * Add atomic action.
   *
   * @param action the action
   * @return the atomic action
   * @throws Exception the exception
   */
  public AtomicAction addAtomicAction(AtomicAction action) throws Exception;

  /**
   * Update atomic action.
   *
   * @param action the action
   * @throws Exception the exception
   */
  public void updateAtomicAction(AtomicAction action) throws Exception;

  /**
   * Remove atomic action.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeAtomicAction(Long id) throws Exception;

  /**
   * Gets the atomic action.
   *
   * @param id the id
   * @return the atomic action
   * @throws Exception the exception
   */
  public AtomicAction getAtomicAction(Long id) throws Exception;

}