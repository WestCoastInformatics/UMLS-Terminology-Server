/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.handlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.AtomSubsetMember;
import com.wci.umls.server.services.handlers.ComputePreferredNameHandler;

/**
 * A ClaML based implementation of {@link ComputePreferredNameHandler}.
 */
public class Rf2ComputePreferredNameHandler implements
    ComputePreferredNameHandler {

  /** the defaultPreferredNames values. */
  private String dpnTypeId = "900000000000013009";

  /** The dpn ref set id. */
  private String dpnRefSetId = "900000000000509007";

  /** The dpn acceptability id. */
  private String dpnAcceptabilityId = "900000000000548007";

  /**
   * Instantiates an empty {@link Rf2ComputePreferredNameHandler}.
   */
  public Rf2ComputePreferredNameHandler() {
    // do nothing
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.ts.helpers.Configurable#setProperties(java.util.Properties)
   */
  @Override
  public void setProperties(Properties p) throws Exception {
    Properties config = ConfigUtility.getConfigProperties();
    // Use defaults if not otherwise supplied
    String prop = config.getProperty("defaultPreferredNames.typeId");
    if (prop != null) {
      dpnTypeId = prop;
    }
    prop = config.getProperty("defaultPreferredNames.refSetId");
    if (prop != null) {
      dpnRefSetId = prop;
    }
    prop = config.getProperty("defaultPreferredNames.acceptabilityId");
    if (prop != null) {
      dpnAcceptabilityId = prop;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.services.handlers.ComputePreferredNameHandler#
   * computePreferredName(java.util.Collection)
   */
  @Override
  public String computePreferredName(Collection<Atom> atoms) throws Exception {
    String first = null;
    for (Atom atom : atoms) {
      // Use first atom encountered if no preferred is found
      if (first == null) {
        first = atom.getName();
      }
      if (isPreferredName(atom)) {
        return atom.getName();
      }
    }
    return first;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.services.handlers.ComputePreferredNameHandler#
   * sortByPreference(java.util.Collection)
   */
  @Override
  public List<Atom> sortByPreference(Collection<Atom> atoms) throws Exception {
    List<Atom> sortedAtoms = new ArrayList<>();
    for (Atom atom : atoms) {
      if (isPreferredName(atom)) {
        sortedAtoms.add(0, atom);
      }
      sortedAtoms.add(atom);
    }
    return null;
  }

  /**
   * Indicates whether or not preferred name is the case.
   *
   * @param atom the atom
   * @return <code>true</code> if so, <code>false</code> otherwise
   * @throws Exception the exception
   */
  private boolean isPreferredName(Atom atom) throws Exception {
    // TODO: need to be able to ask an atom for its subset members.
    for (AtomSubsetMember member : atom.getMembers()) {
      // Check if this language refset and description form the
      // defaultPreferredName
      if (!atom.isObsolete()
          && atom.getTermType().equals(dpnTypeId)
          && member.getSubset().getTerminology().equals(dpnRefSetId)
          && member.getAttributeByName("acceptabilityId").getValue()
              .equals(dpnAcceptabilityId)) {
        return true;
      }
    }
    return false;
  }
}
