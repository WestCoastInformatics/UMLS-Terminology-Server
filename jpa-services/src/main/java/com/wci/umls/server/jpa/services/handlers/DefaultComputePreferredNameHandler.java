/**
 * Copyright 2015 West Coast Informatics, LLC
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
public class DefaultComputePreferredNameHandler implements
    ComputePreferredNameHandler {

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.helpers.Configurable#setProperties(java.util.Properties
   * )
   */
  @Override
  public void setProperties(Properties p) throws Exception {
    // Needs a precedence list
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.services.handlers.ComputePreferredNameHandler#
   * computePreferredName(java.util.Set)
   */
  @Override
  public String computePreferredName(Collection<Atom> atoms) throws Exception {
    // Use ranking algorithm from MetamorphoSys
    // [termgroupRank][lrr][inverse SUI][inverse AUI]
    // LRR isn't available here so just don't worry about it.
    return atoms.size() > 0 ? atoms.iterator().next().getTerm() : "";

  }

  @Override
  public List<Atom> sortByPreference(Collection<Atom> atoms) throws Exception {
    // n/a
    return new ArrayList<>(atoms);
  }

}
