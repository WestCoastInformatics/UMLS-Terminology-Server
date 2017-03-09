/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.wci.umls.server.Project;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.FieldedStringTokenizer;
import com.wci.umls.server.helpers.QueryType;
import com.wci.umls.server.jpa.algo.action.AddDemotionMolecularAction;
import com.wci.umls.server.jpa.algo.action.MergeMolecularAction;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;

/**
 * Abstract support for merge algorithms.
 */
public abstract class AbstractMergeAlgorithm
    extends AbstractInsertMaintReleaseAlgorithm {

  /** The concept-pairs that have gone through the merging process. */
  private Set<String> conceptPairs = new HashSet<>();

  /** The atoms and which concept they belong to. */
  private Map<Long, Long> atomsConcepts = new HashMap<>();

  /**
   * Instantiates an empty {@link AbstractMergeAlgorithm}.
   *
   * @throws Exception the exception
   */
  public AbstractMergeAlgorithm() throws Exception {
    // n/a
  }

  /**
   * Merge.
   *
   * @param atomId the atom id
   * @param atomId2 the atom id 2
   * @param validationChecks the integrity check names
   * @param makeDemotion the make demotion
   * @param changeStatus the change status
   * @param project the project
   * @param statsMap the stats map
   * @throws Exception the exception
   */
  public void merge(Long atomId, Long atomId2, List<String> validationChecks,
    boolean makeDemotion, boolean changeStatus, Project project,
    Map<String, Integer> statsMap) throws Exception {

    // If this is the first time merge is getting called, cache the atom Ids and
    // their concept Ids
    if (atomsConcepts.isEmpty()) {
      logInfo("  caching atomId - conceptId pairs");
      cacheAtomsConcepts();
    }

    // Get the two concepts associated with the two atoms
    final Long conceptId = atomsConcepts.get(atomId);
    final Long conceptId2 = atomsConcepts.get(atomId2);

    if (conceptId == null) {
      addLogEntry(getLastModifiedBy(), getProject().getId(), conceptId,
          getActivityId(), getWorkId(),
          "FAIL no project concept found for atom=" + atomId);
      return;
    }
    if (conceptId2 == null) {
      addLogEntry(getLastModifiedBy(), getProject().getId(), conceptId2,
          getActivityId(), getWorkId(),
          "FAIL no project concept found for atom=" + atomId2);
      return;
    }

    // If this concept pair has already had a merge attempted on it, don't try
    // it again
    if (conceptPairs.contains(conceptId + "|" + conceptId2)) {
      return;
    } else {
      conceptPairs.add(conceptId + "|" + conceptId2);
      conceptPairs.add(conceptId2 + "|" + conceptId);
      statsMap.put("conceptPairs", statsMap.get("conceptPairs") + 1);
    }

    // If Atoms are in the same concept, DON'T perform merge, and log that the
    // atoms are already merged.
    if (conceptId.equals(conceptId2)) {
      addLogEntry(getLastModifiedBy(), getProject().getId(), conceptId,
          getActivityId(), getWorkId(),
          "Skip merging atom " + atomId + " with atom " + atomId2
              + " - atoms are both already in the same concept " + conceptId);

      statsMap.put("unsuccessfulMerges",
          statsMap.get("unsuccessfulMerges") + 1);
      return;
    }

    // Identify the from and to concepts, and from/to Atoms
    // FromConcept will be the smaller concept (least number of atoms)
    Concept fromConcept = null;
    Concept toConcept = null;
    Atom fromAtom = null;
    Atom toAtom = null;

    final Concept concept = getConcept(conceptId);
    final Concept concept2 = getConcept(conceptId2);

    if (concept.getAtoms().size() < concept2.getAtoms().size()) {
      fromConcept = concept;
      fromAtom = getAtom(atomId);
      toConcept = concept2;
      toAtom = getAtom(atomId2);
    } else {
      fromConcept = concept2;
      fromAtom = getAtom(atomId2);
      toConcept = concept;
      toAtom = getAtom(atomId);
    }

    // Otherwise, create and set up a merge action
    final MergeMolecularAction action = new MergeMolecularAction();

    try {

      // Configure the action
      action.setProject(getProject());
      action.setTerminology(getProject().getTerminology());
      action.setVersion(getProject().getVersion());
      action.setWorkId(getWorkId());
      action.setActivityId(getActivityId());
      action.setConceptId(fromConcept.getId());
      action.setConceptId2(toConcept.getId());
      action.setLastModifiedBy(getLastModifiedBy());
      action.setLastModified(fromConcept.getLastModified().getTime());
      action.setOverrideWarnings(false);
      action.setTransactionPerOperation(false);
      action.setMolecularActionFlag(true);
      action.setChangeStatusFlag(changeStatus);
      action.setValidationChecks(validationChecks);

      // Perform the action
      final ValidationResult validationResult =
          action.performMolecularAction(action, getLastModifiedBy(), false);

      // If the action failed, log the failure, and make a demotion if
      // makeDemotion=true.
      if (!validationResult.isValid()) {
        statsMap.put("unsuccessfulMerges",
            statsMap.get("unsuccessfulMerges") + 1);

        addLogEntry(getLastModifiedBy(), getProject().getId(),
            fromConcept.getId(), getActivityId(), getWorkId(),
            "FAIL " + action.getName() + " concept " + fromConcept.getId()
                + " into concept " + toConcept.getId() + ": "
                + validationResult);
        addLogEntry(getLastModifiedBy(), getProject().getId(),
            toConcept.getId(), getActivityId(), getWorkId(),
            "FAIL " + action.getName() + " concept " + toConcept.getId()
                + " from concept " + fromConcept.getId() + ": "
                + validationResult);

        if (makeDemotion) {
          // If from and to concepts have a relationship between them,
          // do NOT make demotion, and add log entry saying why
          for (final ConceptRelationship rel : fromConcept.getRelationships()) {
            if (rel.getTo().getId() == toConcept.getId()) {
              addLogEntry(getLastModifiedBy(), getProject().getId(),
                  fromConcept.getId(), getActivityId(), getWorkId(),
                  "Did not create demotion to concept " + toConcept.getId()
                      + " - relationship between concepts already exist.");
              addLogEntry(getLastModifiedBy(), getProject().getId(),
                  toConcept.getId(), getActivityId(), getWorkId(),
                  "Did not create demotion from concept " + fromConcept.getId()
                      + " - relationship between concepts already exist.");
              return;
            }
          }

          final AddDemotionMolecularAction action2 =
              new AddDemotionMolecularAction();
          action2.setTransactionPerOperation(false);
          action2.setProject(getProject());
          action2.setTerminology(getProject().getTerminology());
          action2.setVersion(getProject().getVersion());
          action2.setWorkId(getWorkId());
          action2.setActivityId(getActivityId());
          action2.setAtomId(fromAtom.getId());
          action2.setAtomId2(toAtom.getId());
          action2.setChangeStatusFlag(changeStatus);
          action2.setConceptId(fromConcept.getId());
          action2.setConceptId2(toConcept.getId());
          action2.setLastModifiedBy(getLastModifiedBy());
          ValidationResult demotionValidationResult = action2
              .performMolecularAction(action2, getLastModifiedBy(), false);

          // If there is already a demotion between these two atoms, it will
          // return a validation error
          if (!demotionValidationResult.isValid()) {
            statsMap.put("unsuccessfulDemotions",
                statsMap.get("unsuccessfulDemotions") + 1);

            addLogEntry(getLastModifiedBy(), getProject().getId(),
                fromConcept.getId(), getActivityId(), getWorkId(),
                "FAIL " + action2.getName() + " to concept " + toConcept.getId()
                    + ": " + demotionValidationResult);
            addLogEntry(getLastModifiedBy(), getProject().getId(),
                toConcept.getId(), getActivityId(), getWorkId(),
                "FAIL " + action2.getName() + " from concept "
                    + fromConcept.getId() + ": " + demotionValidationResult);
          }
          // Otherwise, the demotion was successfully added
          else {
            statsMap.put("successfulDemotions",
                statsMap.get("successfulDemotions") + 1);
          }
          action2.close();
        }
        return;
      }
      // Otherwise, it was successful.
      else {
        // Update atomsConcepts map to reflect change made by successful merge
        updateAtomsConcepts(fromConcept.getId(), toConcept.getId());
        statsMap.put("successfulMerges", statsMap.get("successfulMerges") + 1);
        return;
      }

    } catch (Exception e) {
      try {
        action.rollback();
        e.printStackTrace();
      } catch (Exception e2) {
        // do nothing
      }

      statsMap.put("unsuccessfulMerges",
          statsMap.get("unsuccessfulMerges") + 1);

      throw e;
    } finally {
      // NEED to commit here to make sure that any changes made to the database
      // by MergeMolecularAction or AddDemotionMolecularAction are viewable by
      // this algorithm
      commitClearBegin();
      action.close();
    }

  }

  /**
   * Returns the merge sets.
   *
   * @param srcDirFile the src dir file
   * @return the merge sets
   */
  public List<String> getMergeSets(File srcDirFile) {

    final List<String> mergeSets = new ArrayList<>();
    final Set<String> mergeSetsUnique = new HashSet<>();
    List<String> lines = new ArrayList<>();
    //
    // Load the mergefacts.src file
    //
    try {
      lines = loadFileIntoStringList(srcDirFile, "mergefacts.src", null, null);
    }
    // If file not found, return null
    catch (Exception e) {
      return null;
    }

    final int fieldCount = StringUtils.countMatches(lines.get(0), "|") + 1;
    String fields[] = new String[fieldCount];

    // For this method's purpose, the only field we care about is merge_set, at
    // index 7
    for (String line : lines) {
      FieldedStringTokenizer.split(line, "|", fieldCount, fields);
      final String mergeSet = fields[7];
      mergeSetsUnique.add(mergeSet);
    }

    // Add all of the unique mergeSets referenced in the file to the stringList,
    // and return
    mergeSets.addAll(mergeSetsUnique);

    return mergeSets;
  }

  /**
   * Returns the merge level or an atomId pair.
   *
   * @param atomIdPair the atom id pair
   * @return the merge level
   */
  public Long calculateMergeLevel(Pair<Long, Long> atomIdPair) {
    // MergeLevel =
    // 1 => atom1.code=atom2.code && atom1.sui=atom2.sui && atom1.tty=atom2.tty
    // 2 => atom1.code=atom2.code && atom1.lui=atom2.lui && atom1.tty=atom2.tty
    // 3 => atom1.code=atom2.code && atom1.sui=atom2.sui
    // 4 => atom1.code=atom2.code && atom1.lui=atom2.lui
    // 5 => atom1.code=atom2.code
    // 9 => no equivalence, or equivalence not able to be determined

    Long mergeLevel = null;
    Atom atom1 = null;
    Atom atom2 = null;
    try {
      atom1 = getAtom(atomIdPair.getLeft());
      atom2 = getAtom(atomIdPair.getRight());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    if (atom1.getCodeId().equals(atom2.getCodeId())
        && atom1.getStringClassId().equals(atom2.getStringClassId())
        && atom1.getTermType().equals(atom2.getTermType())) {
      mergeLevel = 1L;
    } else if (atom1.getCodeId().equals(atom2.getCodeId())
        && atom1.getLexicalClassId().equals(atom2.getLexicalClassId())
        && atom1.getTermType().equals(atom2.getTermType())) {
      mergeLevel = 2L;
    } else if (atom1.getCodeId().equals(atom2.getCodeId())
        && atom1.getStringClassId().equals(atom2.getStringClassId())) {
      mergeLevel = 3L;
    } else if (atom1.getCodeId().equals(atom2.getCodeId())
        && atom1.getLexicalClassId().equals(atom2.getLexicalClassId())) {
      mergeLevel = 4L;
    } else if (atom1.getCodeId().equals(atom2.getCodeId())) {
      mergeLevel = 5L;
    } else {
      mergeLevel = 9L;
    }

    return mergeLevel;
  }

  /**
   * Sort pairs by merge level and id.
   *
   * @param filteredAtomIdPairs the filtered atom id pairs
   */
  public void sortPairsByMergeLevelAndId(
    List<Pair<Long, Long>> filteredAtomIdPairs) {

    // Order atomIdPairs
    // sort by MergeLevel, atomId1, atomId2
    Collections.sort(filteredAtomIdPairs, new Comparator<Pair<Long, Long>>() {

      @Override
      public int compare(final Pair<Long, Long> atomIdPair1,
        final Pair<Long, Long> atomIdPair2) {
        int c = 0;
        c = calculateMergeLevel(atomIdPair1)
            .compareTo(calculateMergeLevel(atomIdPair2));
        if (c == 0)
          c = atomIdPair1.getLeft().compareTo(atomIdPair2.getLeft());
        if (c == 0)
          c = atomIdPair1.getRight().compareTo(atomIdPair2.getRight());

        return c;
      }
    });
  }

  /**
   * Apply filters.
   *
   * @param atomIdArrays the atom id pairs
   * @param params the params
   * @param filterQueryType the filter query type
   * @param filterQuery the filter query
   * @param newAtomsOnly the new atoms only
   * @param statsMap the stats map
   * @return the list
   * @throws Exception the exception
   */
  public List<Pair<Long, Long>> applyFilters(List<Long[]> atomIdArrays,
    Map<String, String> params, QueryType filterQueryType, String filterQuery,
    Boolean newAtomsOnly, Map<String, Integer> statsMap) throws Exception {

    final List<Pair<Long, Long>> atomIdPairs = new ArrayList<>();
    final List<Pair<Long, Long>> filteredAtomIdPairs = new ArrayList<>();

    // Recast the arrays as Pairs, for easier comparison
    for (final Long[] atomIdArray : atomIdArrays) {
      final Pair<Long, Long> atomIdPair =
          new ImmutablePair<Long, Long>(atomIdArray[0], atomIdArray[1]);
      atomIdPairs.add(atomIdPair);
    }

    // If no filtering is set, just return the atomIdPairs as-is
    if (filterQueryType == null && filterQuery == null
        && newAtomsOnly == false) {
      return atomIdPairs;
    }

    // Run the filters, and save the unique atomIds/atomIdPairs to sets
    // SQL/JPQL queries will populate filterAtomIdPairs set
    // LUCENE queries will populate the filterAtomIds set
    Set<Pair<Long, Long>> filterAtomIdPairs = null;
    Set<Long> filterAtomIds = null;

    // If LUCENE filter query, returns concept id
    if (filterQueryType == QueryType.LUCENE) {
      final List<Long> filterConceptIds = executeSingleComponentIdQuery(
          filterQuery, filterQueryType, params, ConceptJpa.class, false);

      // For each returned concept, filter for all of its atoms' ids
      filterAtomIds = new HashSet<>();
      for (Long conceptId : filterConceptIds) {
        Concept c = getConcept(conceptId);
        for (Atom a : c.getAtoms()) {
          filterAtomIds.add(a.getId());
        }
      }
    }

    // PROGRAM filter queries not supported yet
    else if (filterQueryType == QueryType.PROGRAM) {
      throw new Exception("PROGRAM queries not yet supported");
    }

    // If JPQL/SQL filter query, returns atom1,atom2 Id pairs
    else if (filterQueryType == QueryType.SQL
        || filterQueryType == QueryType.JPQL) {
      final List<Long[]> filterAtomIdPairArray = executeComponentIdPairQuery(
          filterQuery, filterQueryType, params, AtomJpa.class, false);

      // For each returned atom pair, filter for atomIdPairs in 1,2 or 2,1 order
      filterAtomIdPairs = new HashSet<>();
      for (Long[] filterAtomIdPair : filterAtomIdPairArray) {
        final Pair<Long, Long> atomOneTwoPair =
            new ImmutablePair<>(filterAtomIdPair[0], filterAtomIdPair[1]);
        final Pair<Long, Long> atomTwoOnePair =
            new ImmutablePair<>(filterAtomIdPair[1], filterAtomIdPair[0]);
        filterAtomIdPairs.add(atomOneTwoPair);
        filterAtomIdPairs.add(atomTwoOnePair);
      }
    }

    // Go through each atom pair. If it makes it past all of the filters, add
    // it to the filtered list
    for (final Pair<Long, Long> atomIdPair : atomIdPairs) {

      // New Atoms Only Filter
      // Only filter on 'From' atom (left side of pair)
      if (newAtomsOnly) {
        Long maxAtomIdPreInsertion = null;
        if (getProcess().getExecutionInfo()
            .containsKey("maxAtomIdPreInsertion")) {
          maxAtomIdPreInsertion = Long.parseLong(
              getProcess().getExecutionInfo().get("maxAtomIdPreInsertion"));
        }
        if (maxAtomIdPreInsertion != null) {
          if (atomIdPair.getLeft() <= maxAtomIdPreInsertion) {
            statsMap.put("atomPairsRemovedByFilters",
                statsMap.get("atomPairsRemovedByFilters") + 1);
            continue;
          }
        }
      }

      // Check SQL/JPQL filter atom id pairs, if any
      if (filterAtomIdPairs != null) {
        if (filterAtomIdPairs.contains(atomIdPair)) {
          statsMap.put("atomPairsRemovedByFilters",
              statsMap.get("atomPairsRemovedByFilters") + 1);
          continue;
        }
      }

      // Check LUCENE filter atom ids, if any
      // If atomId is one of the Atoms contained in the
      // filter atoms, don't keep pair
      if (filterAtomIds != null) {
        if (filterAtomIds.contains(atomIdPair.getLeft())
            || filterAtomIds.contains(atomIdPair.getRight())) {
          statsMap.put("atomPairsRemovedByFilters",
              statsMap.get("atomPairsRemovedByFilters") + 1);
          continue;
        }
      }

      // This pair has made it past all of the filters!
      filteredAtomIdPairs.add(atomIdPair);
    }

    return filteredAtomIdPairs;
  }

  /**
   * Cache atoms concepts.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  public void cacheAtomsConcepts() throws Exception {

    // Load alternateTerminologyIds
    Query query = getEntityManager()
        .createQuery("select c.id, a.id from ConceptJpa c join c.atoms a "
            + "where c.terminology=:projectTerminology and c.version=:projectVersion");
    query.setParameter("projectTerminology", getProject().getTerminology());
    query.setParameter("projectVersion", getProject().getVersion());

    List<Object[]> list = query.getResultList();
    for (final Object[] entry : list) {
      final Long conceptId = Long.valueOf(entry[0].toString());
      final Long atomId = Long.valueOf(entry[1].toString());
      atomsConcepts.put(atomId, conceptId);
    }
  }

  /**
   * Update atoms concepts.
   *
   * @param fromConceptId the from concept id
   * @param toConceptId the to concept id
   * @throws Exception the exception
   */
  public void updateAtomsConcepts(Long fromConceptId, Long toConceptId)
    throws Exception {
    // For every atom that was merged into a new concept, update its value in
    // the atomsConcepts map.
    for (Map.Entry<Long, Long> entry : atomsConcepts.entrySet()) {
      Long atomId = entry.getKey();
      Long conceptId = entry.getValue();
      if (conceptId.equals(fromConceptId)) {
        atomsConcepts.put(atomId, toConceptId);
      }
    }
  }

}