/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package com.wci.umls.server.jpa.services.rest;

import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.model.content.Atom;

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
   * @param atom the atom
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void updateAtom(Long projectId, AtomJpa atom, String authToken)
    throws Exception;

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
}
