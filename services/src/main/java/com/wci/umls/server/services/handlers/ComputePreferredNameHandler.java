/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.services.handlers;

import java.util.Collection;
import java.util.List;

import com.wci.umls.server.helpers.Configurable;
import com.wci.umls.server.model.content.Atom;

/**
 * Generically represents an algorithm for computing preferred names.
 */
public interface ComputePreferredNameHandler extends Configurable {

  /**
   * Compute preferred name among a set of atoms.
   *
   * @param atoms the atoms
   * @return the string
   * @throws Exception the exception
   */
  public String computePreferredName(Collection<Atom> atoms) throws Exception;

  /**
   * Sort by preference.
   *
   * @param atoms the atoms
   * @return the list
   * @throws Exception the exception
   */
  public List<Atom> sortByPreference(Collection<Atom> atoms) throws Exception;

}
