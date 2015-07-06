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
public class MultiRrfComputePreferredNameHandler implements
    ComputePreferredNameHandler {

  /** The list. */
  private Map<String,PrecedenceList> listMap = new HashMap<>();

  /** The tty rank map. */
  private Map<String,Map<String, String>> ttyRankMap = new HashMap<>();

  /**
   * Instantiates an empty {@link MultiRrfComputePreferredNameHandler}.
   */
  public MultiRrfComputePreferredNameHandler() {
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
   * @throws Exception
   */
  protected String getRank(Atom atom) throws Exception {
    if (!listMap.containsKey(atom.getTerminology()+atom.getVersion())) {
      // Use the atom's terminology/version.
      // In single source case, this will be correct
      // In UMLS case, this will map to "DEFAULT" which will use the
      // properties passed in for terminology/version
      cacheList(atom.getTerminology(), atom.getVersion());
    }
    String rank = null;
    if (atom.getStringClassId() != null && !atom.getStringClassId().isEmpty()) {
      rank =
          (atom.isObsolete() ? 0 : 1)
              + (atom.isSuppressible() ? 0 : 1)
              + ttyRankMap.get(atom.getTerminology()+atom.getVersion())
                  .get(atom.getTerminology() + "/" + atom.getTermType())
              + (10000000000L - Long.parseLong(atom.getStringClassId()
                  .substring(1))) + (100000000000L - atom.getId());
    } else {
      rank =
          (atom.isObsolete() ? 0 : 1)
              + (atom.isSuppressible() ? 0 : 1)
              + ttyRankMap.get(atom.getTerminology()+atom.getVersion())
                  .get(atom.getTerminology() + "/" + atom.getTermType());
    }
    return rank;
  }

  /**
   * Cache list.
   *
   * @param terminology the terminology
   * @param version the version
   * @throws Exception the exception
   */
  private void cacheList(String terminology, String version) throws Exception {
    MetadataService service = new MetadataServiceJpa();
    listMap.put(terminology+version,service.getDefaultPrecedenceList(terminology, version));
    service.close();
    List<KeyValuePair> list2 = listMap.get(terminology+version).getPrecedence().getKeyValuePairList();
    int ct = 1;
    Map <String,String> localTtyRankMap = new HashMap<>();
    for (int i = list2.size() - 1; i >= 0; i--) {
      String padded = "0000" + ct++;
      padded = padded.substring(padded.length() - 4);
      final KeyValuePair pair = list2.get(i);
      localTtyRankMap.put(pair.getKey() + "/" + pair.getValue(), padded);
    }

    ttyRankMap.put(terminology+version, localTtyRankMap);
    Logger.getLogger(getClass()).info(
        "  default precedence list = " + ttyRankMap);
  }

}
