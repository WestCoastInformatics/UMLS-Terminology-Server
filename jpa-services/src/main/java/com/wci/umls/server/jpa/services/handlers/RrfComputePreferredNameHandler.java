/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.handlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.services.handlers.ComputePreferredNameHandler;

/**
 * Implementation {@link ComputePreferredNameHandler} for data with term-type
 * ordering.
 */
public class RrfComputePreferredNameHandler implements
    ComputePreferredNameHandler {

  /** The defaults list. */
  private Set<Long> listSet = new HashSet<>();

  /** The tty rank map. */
  private Map<Long, Map<String, String>> ttyRankMap = new HashMap<>();

  /**
   * Instantiates an empty {@link RrfComputePreferredNameHandler}.
   */
  public RrfComputePreferredNameHandler() {
    // n/a
  }

  /* see superclass */
  @Override
  public String computePreferredName(Collection<Atom> atoms, PrecedenceList list)
    throws Exception {

    cacheList(list);
    // Use ranking algorithm from MetamorphoSys
    // [termgroupRank][lrr][inverse SUI][inverse AUI]
    // LRR isn't available here so just don't worry about it.
    String maxRank = "";
    Atom maxAtom = null;
    for (Atom atom : atoms) {
      String rank = getRank(atom, list);
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

  /* see superclass */
  @Override
  public List<Atom> sortByPreference(Collection<Atom> atoms, PrecedenceList list)
    throws Exception {

    cacheList(list);

    final List<Atom> sortedAtoms = new ArrayList<>(atoms);
    // Get each atom rank
    final Map<Atom, String> atomRanks = new HashMap<>();
    for (Atom atom : atoms) {
      final String rank = getRank(atom, list);
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
   * @param list the list
   * @return the rank
   * @throws Exception the exception
   */
  protected String getRank(Atom atom, PrecedenceList list) throws Exception {

    // Bail if no list specified or found
    if (list == null) {
      return "00000000000000000000000000";
    }

    // Fail if list hasn't been cached
    if (!ttyRankMap.containsKey(list.getId())) {
      throw new Exception("Unexpected condition, list is not cached - "
          + list.getId());
    }

    final Map<String, String> ttyRanks = ttyRankMap.get(list.getId());
    // Compute the rank as a fixed length string
    // [obsolete][suppressible][tty rank][SUI][atomId]
    // Higher values are better.
    String rank = null;
    if (atom.getStringClassId() != null && !atom.getStringClassId().isEmpty()) {
      rank =
          (atom.isObsolete() ? 0 : 1)
              + (atom.isSuppressible() ? 0 : 1)
              + ttyRanks.get(atom.getTerminology() + "/" + atom.getTermType())
              + (10000000000L - Long.parseLong(atom.getStringClassId()
                  .substring(1))) + (100000000000L - atom.getId());
    } else {
      rank =
          (atom.isObsolete() ? 0 : 1) + (atom.isSuppressible() ? 0 : 1)
              + ttyRanks.get(atom.getTerminology() + "/" + atom.getTermType())
              + (100000000000L - atom.getId());
    }
    return rank;
  }

  /**
   * Cache list.
   *
   * @param list the list
   * @throws Exception the exception
   */
  private void cacheList(PrecedenceList list) throws Exception {

    // No list - simply return to try something new
    if (list == null) {
      return;
    }

    // Bail if configured already
    if (listSet.contains(list.getId())) {
      return;
    }

    // Otherwise, build the TTY map
    Map<String, String> ttyRanks = list.getTermTypeRankMap();
    Logger.getLogger(getClass())
        .info("  default precedence list = " + ttyRanks);
    ttyRankMap.put(list.getId(), ttyRanks);

  }

  /* see superclass */
  @Override
  public String getName() {
    return "RRF Compute Preferred Name Handler";
  }

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    // n/a
  }

}
