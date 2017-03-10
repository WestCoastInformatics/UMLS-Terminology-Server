/*
 *    Copyright 2017 West Coast Informatics, LLC
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

import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.jpa.AbstractConfigurable;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.services.handlers.ComputePreferredNameHandler;

/**
 * Implementation {@link ComputePreferredNameHandler} for data with term-type
 * ordering.
 */
public class RrfComputePreferredNameHandler extends AbstractConfigurable
    implements ComputePreferredNameHandler {

  /** The tty rank map. */
  private static Map<Long, Map<String, String>> ttyRankMap = new HashMap<>();

  /** The terminology rank map. */
  private static Map<Long, Map<String, String>> terminologyRankMap =
      new HashMap<>();

  /**
   * Instantiates an empty {@link RrfComputePreferredNameHandler}.
   */
  public RrfComputePreferredNameHandler() {
    // n/a
  }

  /* see superclass */
  @Override
  public String computePreferredName(final Collection<Atom> atoms,
    final PrecedenceList list) throws Exception {

    cacheList(list);
    // Use ranking algorithm from MetamorphoSys
    // [tbr][termgroupRank][lrr][inverse SUI][inverse AUI]
    // LRR isn't available here so just don't worry about it.
    String maxRank = "";
    Atom maxAtom = null;
    for (final Atom atom : atoms) {
      final String rank = getRank(atom, list);
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
  public List<Atom> sortAtoms(final Collection<Atom> atoms,
    final PrecedenceList list) throws Exception {

    cacheList(list);

    final List<Atom> sortedAtoms = new ArrayList<>(atoms);
    // Get each atom rank
    final Map<Atom, String> atomRanks = new HashMap<>();
    for (final Atom atom : atoms) {
      final String rank = getRank(atom, list);
      atomRanks.put(atom, rank);
    }
    // Sort by atom rank - this works because atom ranks are designed to be
    // fixed-length strings that are directly comparable where higher 
    // values are ranked better
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
  @SuppressWarnings("static-method")
  public String getRank(final Atom atom, final PrecedenceList list)
    throws Exception {

    // Bail if no list specified or found
    if (list == null) {
      return "000000000000000000000000000";
    }

    // Fail if list hasn't been cached
    if (!ttyRankMap.containsKey(list.getId())) {
      throw new Exception(
          "Unexpected condition, list is not cached - " + list.getId());
    }

    final Map<String, String> ttyRanks = ttyRankMap.get(list.getId());
    // Compute the rank as a fixed length string
    // [publishable][obsolete][suppressible][tty rank][lrr][SUI][atomId]
    // Higher values are better.
    if (!atom.getStringClassId().isEmpty()) {
      return (atom.isPublishable() ? 1 : 0) + (atom.isObsolete() ? 0 : 1)
          + (atom.isSuppressible() ? 0 : 1)
          + ttyRanks.get(atom.getTerminology() + "/" + atom.getTermType())
          + atom.getLastPublishedRank()
          + +(10000000000L
              - Long.parseLong(atom.getStringClassId().substring(1)))
          + (100000000000L - atom.getId());
    } else {
      return (atom.isPublishable() ? 1 : 0) + (atom.isObsolete() ? 0 : 1)
          + (atom.isSuppressible() ? 0 : 1)
          + ttyRanks.get(atom.getTerminology() + "/" + atom.getTermType())
          + atom.getLastPublishedRank() + (100000000000L - atom.getId());
    }

  }

  /**
   * Returns the rank for the relationship.
   *
   * @param <T> the
   * @param relationship the rel
   * @param list the list
   * @return the rank
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  public <T extends Relationship<?, ?>> String getRank(final T relationship,
    final PrecedenceList list) throws Exception {
    // Bail if no list specified or found
    if (list == null) {
      return "0000000000000000000";
    }

    // Fail if list hasn't been cached
    if (!terminologyRankMap.containsKey(list.getId())) {
      throw new Exception(
          "Unexpected condition, list is not cached - " + list.getId());
    }

    // Compute the rank as a fixed length string
    // [SAB matching list, e.g. project][publishable][terminology_rank][id]
    // Higher values are better.
    final Map<String, String> terminologyRanks =
        terminologyRankMap.get(list.getId());

    // compute rank
    return ""
        + (relationship.getTerminology().equals(list.getTerminology()) ? 1 : 0)
        + (relationship.isPublishable() ? 1 : 0)
        + (relationship.isObsolete() ? 0 : 1)
        + (relationship.isSuppressible() ? 0 : 1)
        + terminologyRanks.get(relationship.getTerminology())
        + (100000000000L - relationship.getId());

  }

  /**
   * Cache list.
   *
   * @param list the list
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  public void cacheList(PrecedenceList list) throws Exception {

    // No list - simply return to try something new
    if (list == null) {
      return;
    }

    // Bail if configured already
    if (ttyRankMap.containsKey(list.getId())) {
      return;
    }
    // Otherwise, build the TTY map
    final Map<String, String> ttyRanks = list.getTermTypeRankMap();
    ttyRankMap.put(list.getId(), ttyRanks);

    // Otherwise, build the terminology map
    final Map<String, String> terminologyRanks = list.getTerminologyRankMap();
    terminologyRankMap.put(list.getId(), terminologyRanks);

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

  /* see superclass */
  @Override
  public <T extends Relationship<?, ?>> List<T> sortRelationships(
    Collection<T> rels, PrecedenceList list) throws Exception {
    cacheList(list);

    final List<T> sortedRels = new ArrayList<>(rels);
    // Get each rel rank
    final Map<T, String> relRanks = new HashMap<>();
    for (final T rel : rels) {
      final String rank = getRank(rel, list);
      relRanks.put(rel, rank);
    }
    // Sort by rel rank - this works because rel ranks are designed to be
    // fixed-length strings that are directly comparable
    Collections.sort(sortedRels, new Comparator<T>() {
      @Override
      public int compare(T o1, T o2) {
        return relRanks.get(o2).compareTo(relRanks.get(o1));
      }
    });
    return sortedRels;
  }

}
