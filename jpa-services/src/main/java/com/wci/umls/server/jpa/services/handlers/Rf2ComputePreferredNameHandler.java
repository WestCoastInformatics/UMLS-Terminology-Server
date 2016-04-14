/*
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.handlers;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.FieldedStringTokenizer;
import com.wci.umls.server.helpers.PrecedenceList;
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
  private Set<String> dpnRefSetId = new HashSet<>();
  {
    dpnRefSetId.add("900000000000509007");
  }

  /** The dpn acceptability id. */
  private String dpnAcceptabilityId = "900000000000548007";

  /**
   * Instantiates an empty {@link Rf2ComputePreferredNameHandler}.
   */
  public Rf2ComputePreferredNameHandler() {
    // do nothing
  }

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
      dpnRefSetId =
          new HashSet<String>(Arrays.asList(FieldedStringTokenizer.split(prop,
              ",")));
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
  protected String getRank(Atom atom, PrecedenceList list) {

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
          && dpnRefSetId.contains(member.getSubset().getTerminologyId())) {
        langPreferred =
            member.getAttributeByName("acceptabilityId").getValue()
                .indexOf(dpnAcceptabilityId) != -1 ? 2 : 1;
      }
    }
    return "" + active + langPreferred + syOrFn;
  }

}
