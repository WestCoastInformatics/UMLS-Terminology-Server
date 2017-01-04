/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.release;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;

import com.google.common.io.Files;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.AbstractAlgorithm;
import com.wci.umls.server.jpa.algo.FileSorter;
import com.wci.umls.server.model.content.ComponentHistory;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;

/**
 * Algorithm to write the RRF history files.
 */
public class WriteRrfHistoryFilesAlgorithm extends AbstractAlgorithm {

  /** The previous progress. */
  private int previousProgress;

  /** The steps. */
  private int steps = 3;

  /** The steps completed. */
  private int stepsCompleted;

  /** The dir. */
  private File dir;

  /** The writer map. */
  private Map<String, PrintWriter> writerMap = new HashMap<>();

  /**
   * Instantiates an empty {@link WriteRrfHistoryFilesAlgorithm}.
   *
   * @throws Exception the exception
   */
  public WriteRrfHistoryFilesAlgorithm() throws Exception {
    super();
    setActivityId(UUID.randomUUID().toString());
    setWorkId("WRITERRFHISTORY");
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {
    return new ValidationResultJpa();
  }

  /* see superclass */
  @Override
  public void compute() throws Exception {
    logInfo("Starting Write RRF History");

    openWriters();

    previousProgress = 0;
    stepsCompleted = 0;

    writeMraui();
    updateProgress();

    writeMrcui();
    closeWriters();
    logInfo("Finishing Write RRF Indexes");

    // TODO: write CHAGNE/* files
    // only MERGEDCUI/DELETEDCUI, make other ones blank.
  }

  /**
   * Write mrcui.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  private void writeMrcui() throws Exception {
    // 0 CUI1 Unique identifier for first concept - Retired CUI - was present in
    // some prior release, but is currently missing
    // 1 VER The last release version in which CUI1 was a valid CUI
    // 2 REL Relationship
    // 3 RELA Relationship attribute
    // 4 MAPREASON Reason for mapping
    // 5 CUI2 Unique identifier for second concept - the current CUI that CUI1
    // most closely maps to
    // 6 MAPIN Is this map in current subset? Values of Y, N, or null.
    // MetamorphoSys generates the Y or N to indicate whether the CUI2 concept
    // is or is not present in the subset. The null value is for rows where the
    // CUI1 was not present to begin with (i.e., REL=DEL).
    // e.g.
    // C0000401|1993AA|DEL|||||
    // C0000431|2013AB|RB|||C1394823|Y|
    // C0000703|1993AA|SY|||C0002691|Y|
    // Algorithm

    Set<String> currentCuis = new HashSet<>();
    String queryStr = null;
    javax.persistence.Query query = null;
    queryStr = "select c.terminologyId " + "from ConceptJpa c "
        + "where c.terminology = :terminology and c.version = :version "
        + "and c.publishable = true";
    query = manager.createQuery(queryStr);
    query.setParameter("terminology", getProject().getTerminology());
    query.setParameter("version", getProject().getVersion());
    currentCuis.addAll(query.getResultList());

    // atoms in different concept than previous release:
    Map<String, Set<String>> atomsMoved = new HashMap<>();
    queryStr = "select distinct value(cid), c.terminologyId  "
        + "from ConceptJpa c join c.atoms a join a.conceptTerminologyIds cid "
        + "where c.terminology = :terminology and c.version = :version "
        + "and c.publishable = true " + "and a.publishable = true "
        + "and key(cid) = :terminology " + "and value(cid) != c.terminologyId";
    query = manager.createQuery(queryStr);
    query.setParameter("terminology", getProject().getTerminology());
    query.setParameter("version", getProject().getVersion());
    final List<Object[]> results = query.getResultList();
    for (Object[] objArray : results) {
      String lastReleaseCui = objArray[0].toString();
      String cui = objArray[1].toString();
      if (!atomsMoved.containsKey(lastReleaseCui)) {
        atomsMoved.put(lastReleaseCui, new HashSet<>());
      }
      atomsMoved.get(lastReleaseCui).add(cui);
    }

    // Determine "merge" cases - all keys from atomsMoved where the value is
    // size()==1 and the key is not in currentCuis.
    Map<String, String> syCui1Cui2Pairs = new HashMap<>();
    for (Entry<String, Set<String>> entry : atomsMoved.entrySet()) {
      String lastReleaseCui = entry.getKey();
      if (entry.getValue().size() == 1
          && !currentCuis.contains(lastReleaseCui)) {
        String cui2 = (String) entry.getValue().toArray()[0];
        // write an SY row where CUI1 is the last release CUI, and CUI2 is the
        // single cui in the value.
        // save these CUI1->CUI2 pairs for later.
        syCui1Cui2Pairs.put(lastReleaseCui, cui2);
        StringBuilder sb = new StringBuilder();
        sb.append(lastReleaseCui).append("|"); // 0 CUI1
        sb.append(getProcess().getVersion()).append("|"); // 1 VER
        sb.append("SY").append("|"); // 2 REL
        sb.append("|"); // 3 RELA
        sb.append("|"); // 4 MAPREASON
        sb.append(cui2).append("|"); // 5 CUI2
        sb.append("Y").append("|"); // 6 MAPIN
        sb.append("\n");
        writerMap.get("MRCUI.RRF").print(sb.toString());
      }
    }
    // Determine "split" cases - all keys from atomsMoved where the value is
    // size()>1 and the key is not in current cuis.
    // write RO rows for both "value" CUIs.
    // Note: split concept must be merged into third concept in order to meet
    // !currentCuis requirement
    for (Entry<String, Set<String>> entry : atomsMoved.entrySet()) {
      String lastReleaseCui = entry.getKey();
      if (entry.getValue().size() > 1
          && !currentCuis.contains(lastReleaseCui)) {
        // write RO rows for both "value" CUIs.
        List<String> values = new ArrayList<>(entry.getValue());
        StringBuilder sb = new StringBuilder();
        sb.append(lastReleaseCui).append("|"); // 0 CUI1
        sb.append(getProcess().getVersion()).append("|"); // 1 VER
        sb.append("RO").append("|"); // 2 REL
        sb.append("|"); // 3 RELA
        sb.append("|"); // 4 MAPREASON
        sb.append(values.get(0)).append("|"); // 5 CUI2
        sb.append("Y").append("|"); // 6 MAPIN
        sb.append("\n");
        sb.append(lastReleaseCui).append("|"); // 0 CUI1
        sb.append(getProcess().getVersion()).append("|"); // 1 VER
        sb.append("RO").append("|"); // 2 REL
        sb.append("|"); // 3 RELA
        sb.append("|"); // 4 MAPREASON
        sb.append(values.get(1)).append("|"); // 5 CUI2
        sb.append("Y").append("|"); // 6 MAPIN
        sb.append("\n");
        writerMap.get("MRCUI.RRF").print(sb.toString());
      }
    }

    updateProgress();

    // process one concept at a time - only looking at unpublishable concepts
    final Session session = manager.unwrap(Session.class);
    org.hibernate.Query hQuery = session.createQuery(
        "select a from ConceptJpa a WHERE a.publishable = false and terminology = :terminology order by a.terminologyId");

    hQuery.setParameter("terminology", getProject().getTerminology());
    hQuery.setReadOnly(true).setFetchSize(1000);
    ScrollableResults scrollableResults =
        hQuery.scroll(ScrollMode.FORWARD_ONLY);
    while (scrollableResults.next()) {
      final Concept c = (Concept) scrollableResults.get()[0];

      // TODO test look in Sample MRCUI for SY case
      // lookup cui2 and merge that concept into a cui3

      // IF does not have a component history (e.g. newly dead)
      if (c.getComponentHistory() == null
          || c.getComponentHistory().isEmpty()) {
        // If no bequeathal rel, -> write out a "DEL" entry to MRCUI
        List<ConceptRelationship> bequeathalRels = getBequeathalRels(c);
        if (bequeathalRels.size() == 0) {
          StringBuilder sb = new StringBuilder();
          sb.append(c.getTerminologyId()).append("|"); // 0 CUI1
          sb.append(getProcess().getVersion()).append("|"); // 1 VER
          sb.append("DEL").append("|"); // 2 REL
          sb.append("|"); // 3 RELA
          sb.append("|");// 4 MAPREASON
          sb.append("|"); // 5 CUI2
          sb.append("|");// 6 MAPIN blank if CUI2 is blank
          sb.append("\n");
          writerMap.get("MRCUI.RRF").print(sb.toString());
        }
        // If bequeathal rel -> write out bequeathal entry for each rel
        else {
          for (ConceptRelationship bequeathalRel : bequeathalRels) {
            StringBuilder sb = new StringBuilder();
            sb.append(c.getTerminologyId()).append("|"); // 0 CUI1
            sb.append(getProcess().getVersion()).append("|"); // 1 VER
            sb.append(bequeathalRel.getRelationshipType().substring(1))
                .append("|"); // 2 REL
            sb.append("|"); // 3 RELA
            sb.append("|"); // 4 MAPREASON
            sb.append(bequeathalRel.getTo().getTerminologyId()).append("|"); // 5
                                                                             // CUI2
                                                                             // getTo.getTId
            sb.append("Y").append("|"); // 6 MAPIN
            sb.append("\n");
            writerMap.get("MRCUI.RRF").print(sb.toString());
          }
        }

      }

      // If it has a component history. (e.g.
      // component.getComponentHistory().size()>0) - i.e. this is a "historical"
      // concept.
      else if (c.getComponentHistory().size() > 0) {
        for (ComponentHistory history : c.getComponentHistory()) {
          // if DEL -> write out component history as is.
          if (history.getRelationshipType().equals("DEL")) {
            StringBuilder sb = new StringBuilder();
            sb.append(history.getLastModifiedBy()).append("|"); // CUI1
            sb.append(history.getVersion()).append("|"); // 1 VER
            sb.append("DEL").append("|"); // 2 REL
            sb.append("|");// 3 RELA
            sb.append("|"); // 4 MAPREASON
            sb.append("|"); // 5 CUI2
            sb.append("|"); // 6 MAPIN
            sb.append("\n");
            writerMap.get("MRCUI.RRF").print(sb.toString());
          }
          // If SY or R?
          else if (history.getRelationshipType().equals("SY")
              || history.getRelationshipType().startsWith("R")) {

            // if "referenced concept" is publishable, write it out as is
            if (history.getReferencedConcept().isPublishable()) {
              StringBuilder sb = new StringBuilder();
              sb.append(history.getLastModifiedBy()).append("|"); // CUI1
              sb.append(history.getVersion()).append("|"); // 1 VER
              sb.append(history.getRelationshipType()).append("|"); // 2 REL
              sb.append(history.getAdditionalRelationshipType()).append("|");// 3
                                                                             // RELA
              sb.append(history.getReason()).append("|"); // 4 MAPREASON
              sb.append(history.getReferencedConcept().getTerminologyId())
                  .append("|"); // 5 CUI2
              sb.append("Y").append("|"); // 6 MAPIN
              sb.append("\n");
              writerMap.get("MRCUI.RRF").print(sb.toString());
            }
            // If not publishable, check whether the referencedConcept has a
            // terminologyId matching the CUI1 of SY rows computed above.
            else {
              // If so, change the referencedConcept to point to the CUI2
              // concept.
              // TODO: (later) deal with CUI2 bequeathed case
              if (syCui1Cui2Pairs.containsKey(
                  history.getReferencedConcept().getTerminologyId())) {
                StringBuilder sb = new StringBuilder();
                sb.append(history.getLastModifiedBy()).append("|"); // CUI1
                sb.append(history.getVersion()).append("|"); // 1 VER
                sb.append(history.getRelationshipType()).append("|"); // 2 REL
                sb.append(history.getAdditionalRelationshipType()).append("|");// 3
                                                                               // RELA
                sb.append(history.getReason()).append("|"); // 4 MAPREASON
                sb.append(syCui1Cui2Pairs
                    .get(history.getReferencedConcept().getTerminologyId()))
                    .append("|"); // 5 CUI2
                sb.append("|"); // 6 MAPIN
                sb.append("\n");
                writerMap.get("MRCUI.RRF").print(sb.toString());
              } else {
                StringBuilder sb = new StringBuilder();
                sb.append(history.getLastModifiedBy()).append("|"); // CUI1
                sb.append(history.getVersion()).append("|"); // 1 VER
                sb.append("DEL").append("|"); // 2 REL
                sb.append("|");// 3 RELA
                sb.append("|"); // 4 MAPREASON
                sb.append("|"); // 5 CUI2
                sb.append("|"); // 6 MAPIN
                sb.append("\n");
                writerMap.get("MRCUI.RRF").print(sb.toString());
              }
            }
            // otherwise, don't write a MRCUI entry. (e.g this is a component
            // history that will go away.)
          }
        } // end for
      }
    }

    updateProgress();

    // sort files
    for (String writerName : writerMap.keySet()) {
      File inputFile = new File(dir, writerName);
      File outputFile = new File(dir, writerName + ".sorted");
      FileSorter.sortFile(inputFile.getAbsolutePath(),
          outputFile.getAbsolutePath(), ConfigUtility.getByteComparator());
    }

    // move sorted files into orig files
    for (String writerName : writerMap.keySet()) {

      File inputFile = new File(dir, writerName);
      File outputFile = new File(dir, writerName + ".sorted");
      inputFile.delete();
      Files.move(outputFile.getAbsoluteFile(), inputFile.getAbsoluteFile());
    }
  }

  /**
   * Returns the bequeathal rels.
   *
   * @param c the c
   * @return the bequeathal rels
   */
  // this means concept.getRelationships() has 1 or more relationships with BRO,
  // BRN, BRB as relationshiptype (write out without the leading B)
  @SuppressWarnings("static-method")
  private List<ConceptRelationship> getBequeathalRels(Concept c) {
    List<ConceptRelationship> bequeathalRels = new ArrayList<>();
    for (ConceptRelationship rel : c.getRelationships()) {
      if (rel.getRelationshipType().equals("BRO")
          || rel.getRelationshipType().equals("BRN")
          || rel.getRelationshipType().equals("BRB")) {
        bequeathalRels.add(rel);
      }
    }
    return bequeathalRels;
  }

  /**
   * Write mraui.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  private void writeMraui() throws Exception {
    // This file records the movement of Atom Unique Identifiers (AUIs) from a
    // concept (CUI1)
    // in one version of the Metathesaurus to a concept (CUI2) in the next
    // version (VER) of
    // the Metathesaurus. The file is historical.
    // Field Description
    // 0 AUI1
    // 1 CUI1
    // 2 VER version in which this change to the AUI first occurred
    // 3 REL
    // 4 RELA
    // 5 MAPREASON
    // 6 AUI2
    // 7 CUI2 the current CUI that CUI1 most closely maps to
    // 8 MAPIN is AUI2 in current subset
    //
    // e.g.
    // A0009348|C0030499|201604|||move|A0009348|C0747256|Y|
    // atom->alternateTerminologyIds(project.terminolgy) -> A0009348 AUI1
    // atom->conceptTerminologyIds(project.terminolgy) -> C0030499 (aka "last
    // release cui").CUI1
    // C0747256 = concept.getTerminologyId() for the concept containing this
    // atom. CUI2
    // Algorithm
    // Find all atoms where the lastReleaseCui != current CUi, write an entry.

    final List<Object[]> results = new ArrayList<>();

    String queryStr = null;
    javax.persistence.Query query = null;
    queryStr = "select value(aid), value(cid), c.terminologyId  "
        + "from ConceptJpa c join c.atoms a join a.conceptTerminologyIds cid "
        + "join a.alternateTerminologyIds aid "
        + "where c.terminology = :terminology and c.version = :version "
        + "and c.publishable = true " + "and a.publishable = true "
        + "and key(aid) = :terminology " + "and key(cid) = :terminology "
        + "and value(cid) != c.terminologyId";
    query = manager.createQuery(queryStr);
    query.setParameter("terminology", getProject().getTerminology());
    query.setParameter("version", getProject().getVersion());
    results.addAll(query.getResultList());
    for (Object[] objArray : results) {
      String aui = objArray[0].toString();
      String lastReleaseCui = objArray[1].toString();
      String cui = objArray[2].toString();

      // Write an entry for each row.
      StringBuilder sb = new StringBuilder();
      sb.append(aui).append("|"); // 0 AUI1
      sb.append(lastReleaseCui).append("|"); // 1 CUI1 last release cui
      sb.append(getProcess().getVersion()).append("|"); // 2 VER
      sb.append("|"); // 3 REL
      sb.append("|");// 4 RELA
      sb.append("move").append("|"); // 5 MAPREASON
      sb.append(aui).append("|"); // 6 AUI2 same as aui1
      sb.append(cui).append("|"); // 7 CUI2 current cui
      sb.append("Y").append("|"); // 8 MAPIN always Y
      sb.append("\n");
      writerMap.get("MRAUI.RRF").print(sb.toString());
    }
  }

  /**
   * Open writers.
   *
   * @throws Exception the exception
   */
  private void openWriters() throws Exception {
    dir = new File(config.getProperty("source.data.dir") + "/"
        + getProcess().getInputPath() + "/" + getProcess().getVersion() + "/"
        + "META");

    writerMap.put("MRAUI.RRF",
        new PrintWriter(new FileWriter(new File(dir, "MRAUI.RRF"))));
    writerMap.put("MRCUI.RRF",
        new PrintWriter(new FileWriter(new File(dir, "MRCUI.RRF"))));
  }

  /**
   * Close writers.
   */
  private void closeWriters() {
    for (PrintWriter writer : writerMap.values()) {
      writer.close();
    }
  }

  /* see superclass */
  @Override
  public void reset() throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public void checkProperties(Properties p) throws Exception {
    checkRequiredProperties(new String[] {
        ""
    }, p);
  }

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    // n/a
  }

  /**
   * Update progress.
   *
   * @throws Exception the exception
   */
  public void updateProgress() throws Exception {
    stepsCompleted++;
    int currentProgress = (int) ((100.0 * stepsCompleted / steps));
    if (currentProgress > previousProgress) {
      checkCancel();
      fireProgressEvent(currentProgress,
          "WRITE RRF HISTORY progress: " + currentProgress + "%");
      previousProgress = currentProgress;
    }
  }

  /* see superclass */
  @Override
  public String getDescription() {
    return ConfigUtility.getNameFromClass(getClass());
  }
}
