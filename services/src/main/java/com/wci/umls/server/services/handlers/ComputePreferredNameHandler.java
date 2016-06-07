/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.services.handlers;

import java.util.Collection;
import java.util.List;

import com.wci.umls.server.helpers.Configurable;
import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.model.content.Atom;

/**
 * Represents an algorithm for computing preferred names.
 */
public interface ComputePreferredNameHandler extends Configurable {

  /**
   * Compute preferred name among a set of atoms.
   *
   * @param atoms the atoms
   * @param list the list
   * @return the string
   * @throws Exception the exception
   */
  public String computePreferredName(Collection<Atom> atoms, PrecedenceList list)
    throws Exception;

  /**
   * Sort by preference.
   *
   * @param atoms the atoms
   * @param list the list
   * @return the list
   * @throws Exception the exception
   */
  public List<Atom> sortByPreference(Collection<Atom> atoms, PrecedenceList list)
    throws Exception;

}
