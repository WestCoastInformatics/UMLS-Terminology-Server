/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.services.handlers;

import java.util.Set;

import com.wci.umls.server.helpers.Configurable;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Concept;

/**
 * Generically represents an algorithm for computing preferred names.
 */
public interface ComputePreferredNameHandler extends Configurable {

  /**
   * Compute preferred name for a concept.
   *
   * @param concept the concept
   * @return the string
   * @throws Exception the exception
   */
  public String computePreferredName(Concept concept) throws Exception;

  /**
   * Compute preferred name among a set of atoms.
   *
   * @param atoms the atoms
   * @return the string
   * @throws Exception the exception
   */
  public String computePreferredName(Set<Atom> atoms) throws Exception;

  /**
   * Indicates whether or not the atom is the preferred name.
   *
   * @param atom the atom
   * @return <code>true</code> if so, <code>false</code> otherwise
   * @throws Exception the exception
   */
  public boolean isPreferredName(Atom atom) throws Exception;

}
