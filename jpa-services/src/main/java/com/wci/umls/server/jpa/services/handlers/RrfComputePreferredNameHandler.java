/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.handlers;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.wci.umls.server.helpers.KeyValuePair;
import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.jpa.services.MetadataServiceJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.services.MetadataService;
import com.wci.umls.server.services.handlers.ComputePreferredNameHandler;

/**
 * Default implementation of {@link ComputePreferredNameHandler}.
 */
public class RrfComputePreferredNameHandler implements
    ComputePreferredNameHandler {

  /** The list. */
  private PrecedenceList list = null;

  /** The tty rank map. */
  private Map<String, String> ttyRankMap = new HashMap<>();

  /**
   * Instantiates an empty {@link RrfComputePreferredNameHandler}.
   *
   * @throws Exception the exception
   */
  public RrfComputePreferredNameHandler() throws Exception {
    // n/a
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.helpers.Configurable#setProperties(java.util.Properties
   * )
   */
  @Override
  public void setProperties(Properties p) throws Exception {
    // n/a
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.services.handlers.ComputePreferredNameHandler#
   * computePreferredName(java.util.Collection)
   */
  @Override
  public String computePreferredName(Collection<Atom> atoms) throws Exception {
    if (list == null) {
      cacheList();
    }
    // Use ranking algorithm from MetamorphoSys
    // [termgroupRank][lrr][inverse SUI][inverse AUI]
    // LRR isn't available here so just don't worry about it.
    String maxRank = "";
    Atom maxAtom = null;
    for (Atom atom : atoms) {
      String rank = getRank(atom);
      if (maxAtom == null) {
        maxAtom = atom;
        maxRank = rank;
      } else if (maxRank.compareTo(rank) < 0) {
        maxAtom = atom;
        maxRank = rank;
      }
    }

    if (maxAtom != null) {
      return maxAtom.getTerm();
    }
    return "[Could not determine preferred name]";
  }

  /**
   * Returns the rank.
   *
   * @param atom the atom
   * @return the rank
   */
  private String getRank(Atom atom) {
    return ttyRankMap.get(atom.getTerminology() + "/" + atom.getTermType())
        + (10000000000L - Long.parseLong(atom.getStringClassId().substring(1)))
        + (10000000000L - Long.parseLong(atom.getAlternateTerminologyIds().get(
            "UMLS")));
  }

  /**
   * Cache list.
   *
   * @throws Exception the exception
   */
  private void cacheList() throws Exception {
    MetadataService service = new MetadataServiceJpa();
    list = service.getDefaultPrecedenceList("DEFAULT", "");
    service.close();
    List<KeyValuePair> list2 = list.getPrecedence().getKeyValuePairList();
    for (int i = 0; i < list2.size(); i++) {
      String padded = "0000" + i;
      padded = padded.substring(padded.length() - 5);
      final KeyValuePair pair = list2.get(i);
      ttyRankMap.put(pair.getKey() + "/" + pair.getValue(), padded);
    }

  }
}
