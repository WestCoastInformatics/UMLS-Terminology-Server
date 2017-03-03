/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.handlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.jpa.AbstractConfigurable;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.services.handlers.ComputePreferredNameHandler;

/**
 * Default implementation of {@link ComputePreferredNameHandler}. This is a
 * dummy implelmentation just to ensure an example exists.
 */
public class DefaultComputePreferredNameHandler extends AbstractConfigurable
    implements ComputePreferredNameHandler {

  /* see superclass */
  @Override
  public String computePreferredName(Collection<Atom> atoms,
    PrecedenceList list) throws Exception {
    // For default implementation, pick first atom.
    return atoms.size() > 0 ? atoms.iterator().next().getName() : null;

  }

  /* see superclass */
  @Override
  public List<Atom> sortAtoms(Collection<Atom> atoms, PrecedenceList list)
    throws Exception {
    // simply return the list
    return new ArrayList<>(atoms);
  }

  @Override
  public String getName() {
    return "Default Preferred Name Handler";
  }

  @Override
  public <T extends Relationship<?, ?>> List<T> sortRelationships(
    Collection<T> rels, PrecedenceList list) throws Exception {
    // just return the list
    return new ArrayList<>(rels);
  }

}
