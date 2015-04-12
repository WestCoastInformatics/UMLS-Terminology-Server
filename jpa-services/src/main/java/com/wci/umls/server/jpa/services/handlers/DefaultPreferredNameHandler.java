/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.handlers;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.services.handlers.ComputePreferredNameHandler;

/**
 * Default implementation of {@link ComputePreferredNameHandler}.
 */
public class DefaultPreferredNameHandler implements ComputePreferredNameHandler {

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
   * computePreferredName(com.wci.umls.server.model.content.Concept)
   */
  @Override
  public String computePreferredName(Concept concept) throws Exception {
    return computePreferredName(new HashSet<Atom>(concept.getAtoms()));
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.services.handlers.ComputePreferredNameHandler#
   * computePreferredName(java.util.Set)
   */
  @Override
  public String computePreferredName(Set<Atom> atoms) throws Exception {
    // Use ranking algorithm from MetamorphoSys
    // [termgroupRank][lrr][inverse SUI][inverse AUI]
    // LRR isn't available here so just don't worry about it.
    return null;

  }

}
