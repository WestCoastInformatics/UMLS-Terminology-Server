/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.handlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

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
      } else if (rank.compareTo(maxRank) > 0) {
        maxAtom = atom;
        maxRank = rank;
      }
    }

    if (maxAtom != null) {
      return maxAtom.getName();
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.services.handlers.ComputePreferredNameHandler#
   * sortByPreference(java.util.Collection)
   */
  @Override
  public List<Atom> sortByPreference(Collection<Atom> atoms) throws Exception {
    List<Atom> sortedAtoms = new ArrayList<>(atoms);
    // Get each atom rank
    final Map<Atom, String> atomRanks = new HashMap<>();
    for (Atom atom : atoms) {
      final String rank = getRank(atom);
      atomRanks.put(atom, rank);
    }
    // Sort by atom rank - this works because atom ranks are designed to be
    // fixed-length strings that are directly comparable
    Collections.sort(sortedAtoms, new Comparator<Atom>() {
      @Override
      public int compare(Atom o1, Atom o2) {
        return atomRanks.get(o2).compareTo(atomRanks.get(o1));
      }
    });
    return sortedAtoms;
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
        + (10000000000L - Long.parseLong(atom.getAlternateTerminologyIds()
            .get("UMLS").substring(1)));
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
    int ct = 1;
    for (int i = list2.size() - 1; i >= 0; i--) {
      String padded = "0000" + ct++;
      padded = padded.substring(padded.length() - 4);
      final KeyValuePair pair = list2.get(i);
      ttyRankMap.put(pair.getKey() + "/" + pair.getValue(), padded);
    }
    Logger.getLogger(getClass()).info(
        "  default precedence list = " + ttyRankMap);
  }

}
