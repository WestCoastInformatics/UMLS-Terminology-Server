/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.handlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.services.handlers.ComputePreferredNameHandler;

/**
 * Default implementation of {@link ComputePreferredNameHandler}.
 */
public class DefaultComputePreferredNameHandler
    implements ComputePreferredNameHandler {

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    // Needs a precedence list
  }

  /* see superclass */
  @Override
  public String computePreferredName(Collection<Atom> atoms) throws Exception {
    // Use ranking algorithm from MetamorphoSys
    // [termgroupRank][lrr][inverse SUI][inverse AUI]
    // LRR isn't available here so just don't worry about it.
    return atoms.size() > 0 ? atoms.iterator().next().getName() : null;

  }

  /* see superclass */
  @Override
  public List<Atom> sortByPreference(Collection<Atom> atoms) throws Exception {
    // n/a
    return new ArrayList<>(atoms);
  }

  @Override
  public String getName() {
    return "Default Preferred Name Handler";
  }

}
