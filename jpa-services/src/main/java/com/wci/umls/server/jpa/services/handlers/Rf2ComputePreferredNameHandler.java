/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.handlers;

import java.util.Properties;

import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.AtomSubsetMember;
import com.wci.umls.server.services.handlers.ComputePreferredNameHandler;

/**
 * A ClaML based implementation of {@link ComputePreferredNameHandler}.
 */
public class Rf2ComputePreferredNameHandler extends
    RrfComputePreferredNameHandler {

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

  /**
   * Returns the rank.
   *
   * @param atom the atom
   * @return the rank
   */
  @Override
  protected String getRank(Atom atom) {

    // [active][LangPreferred][SyOrFn]
    // active = 2, obsolete = 1
    // LangPreferred = 2 (Preferred), 1 (Acceptable), 0 (Missing)
    // SyOrFn = 1 (Sy), 0 (Fn)

    // Find active, synonym from desired refset with desired acceptability id
    int active = atom.isObsolete() ? 1 : 2;
    int langPreferred = 0;
    int syOrFn = atom.getTermType().indexOf(dpnTypeId) != -1 ? 1 : 0;

    for (AtomSubsetMember member : atom.getMembers()) {
      // Check if this language refset and description form the
      // defaultPreferredName. Need to use "index of" because the
      // SNOMED graph resolver replaces the values with values + names
      if (!member.isObsolete()
          && member.getSubset().getTerminologyId().equals(dpnRefSetId)) {
        langPreferred =
            member.getAttributeByName("acceptabilityId").getValue()
                .indexOf(dpnAcceptabilityId) != -1 ? 2 : 1;
      }
    }
    return "" + active + langPreferred + syOrFn;
  }

}
