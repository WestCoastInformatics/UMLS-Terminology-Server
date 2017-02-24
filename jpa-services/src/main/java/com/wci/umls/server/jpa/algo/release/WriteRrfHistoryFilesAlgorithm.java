/*
 *    Copyright 2017 West Coast Informatics, LLC
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

import javax.persistence.Query;

import org.codehaus.plexus.util.FileUtils;

import com.google.common.io.Files;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.QueryType;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.AbstractInsertMaintReleaseAlgorithm;
import com.wci.umls.server.jpa.algo.FileSorter;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.services.handlers.DefaultComputePreferredNameHandler;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.ComponentHistory;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.services.RootService;

/**
 * Algorithm to write the RRF history files.
 */
public class WriteRrfHistoryFilesAlgorithm
    extends AbstractInsertMaintReleaseAlgorithm {

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
    logInfo("Starting " + getName());
    fireProgressEvent(0, "Starting");

    setSteps(6);
    openWriters();

    logInfo("  Write MRAUI.RRF");
    writeMraui();
    updateProgress();

    logInfo("  Write MRCUI.RRF");
    writeMrcui();
    updateProgress();

    logInfo("  Write NCI code file");
    writeNciCodeCuiMap();
    updateProgress();

    logInfo("  Write NCIMETA history file");
    writeHistory();
    updateProgress();

    closeWriters();

    // sort files
    final File changeDir = new File(dir, "CHANGE");
    for (final String writerName : writerMap.keySet()) {
      File fdir = changeDir;
      if (writerName.equals("MRCUI.RRF") || writerName.equals("MRAUI.RRF")
          || writerName.toLowerCase().contains("nci")) {
        fdir = dir;
      }
      final File inputFile = new File(fdir, writerName);
      final File outputFile = new File(fdir, writerName + ".sorted");
      FileUtils.removePath(outputFile.getPath());
      FileSorter.sortFile(inputFile.getAbsolutePath(),
          outputFile.getAbsolutePath(), ConfigUtility.getByteComparator());
    }

    // move sorted files into orig files
    for (final String writerName : writerMap.keySet()) {
      File fdir = changeDir;
      if (writerName.equals("MRCUI.RRF") || writerName.equals("MRAUI.RRF")
          || writerName.toLowerCase().contains("nci")) {
        fdir = dir;
      }
      final File inputFile = new File(fdir, writerName);
      final File outputFile = new File(fdir, writerName + ".sorted");
      inputFile.delete();
      Files.move(outputFile.getAbsoluteFile(), inputFile.getAbsoluteFile());
    }
    fireProgressEvent(100, "Finished");
    logInfo("Finished " + getName());
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

    final Set<String> currentCuis = new HashSet<>();
    String queryStr = null;
    javax.persistence.Query query = null;
    queryStr = "select c.terminologyId from ConceptJpa c "
        + "where c.terminology = :terminology and c.version = :version "
        + "and c.publishable = true";
    query = manager.createQuery(queryStr);
    query.setParameter("terminology", getProject().getTerminology());
    query.setParameter("version", getProject().getVersion());
    currentCuis.addAll(query.getResultList());

    // atoms in different concept than previous release:
    final Map<String, Set<String>> atomsMoved = new HashMap<>();
    queryStr = "select distinct value(cid), c.terminologyId  "
        + "from ConceptJpa c join c.atoms a join a.conceptTerminologyIds cid "
        + "where c.terminology = :terminology and c.version = :version "
        + "and c.publishable = true and a.publishable = true "
        + "and key(cid) = :terminology";
    query = manager.createQuery(queryStr);
    query.setParameter("terminology", getProject().getTerminology());
    query.setParameter("version", getProject().getVersion());
    final List<Object[]> results = query.getResultList();
    for (final Object[] objArray : results) {
      final String lastReleaseCui = objArray[0].toString();
      final String cui = objArray[1].toString();
      if (lastReleaseCui.equals(cui)) {
        continue;
      }
      if (!atomsMoved.containsKey(lastReleaseCui)) {
        atomsMoved.put(lastReleaseCui, new HashSet<>());
      }
      atomsMoved.get(lastReleaseCui).add(cui);
    }

    // Determine "merge" cases - all keys from atomsMoved where the value is
    // size()==1 and the key is not in currentCuis.
    final Map<String, String> syCui1Cui2Pairs = new HashMap<>();
    for (final Entry<String, Set<String>> entry : atomsMoved.entrySet()) {
      final String lastReleaseCui = entry.getKey();
      if (entry.getValue().size() == 1
          && !currentCuis.contains(lastReleaseCui)) {
        final String cui2 = (String) entry.getValue().toArray()[0];
        // write an SY row where CUI1 is the last release CUI, and CUI2 is the
        // single cui in the value.
        // save these CUI1->CUI2 pairs for later.
        syCui1Cui2Pairs.put(lastReleaseCui, cui2);
        final StringBuilder sb = new StringBuilder();
        // 0 CUI1
        sb.append(lastReleaseCui).append("|");
        // 1 VER
        sb.append(getProcess().getVersion()).append("|");
        // 2 REL
        sb.append("SY").append("|");
        // 3 RELA, 4 MAPREASON
        sb.append("||");
        // 5 CUI2
        sb.append(cui2).append("|");
        // 6 MAPIN
        sb.append("Y").append("|");
        sb.append("\n");

        writerMap.get("MRCUI.RRF").print(sb.toString());
        writerMap.get("MERGEDCUI.RRF")
            .print(lastReleaseCui + "|" + cui2 + "|\n");
      }
    }
    // Determine "split" cases - all keys from atomsMoved where the value is
    // size()>1 and the key is not in current cuis.
    // write RO rows for both "value" CUIs.
    // Note: split concept must be merged into third concept in order to meet
    // !currentCuis requirement
    for (final Entry<String, Set<String>> entry : atomsMoved.entrySet()) {
      final String lastReleaseCui = entry.getKey();
      if (entry.getValue().size() > 1
          && !currentCuis.contains(lastReleaseCui)) {
        // write RO rows for both "value" CUIs.
        final List<String> values = new ArrayList<>(entry.getValue());
        final StringBuilder sb = new StringBuilder();
        // 0 CUI1
        sb.append(lastReleaseCui).append("|");
        // 1 VER
        sb.append(getProcess().getVersion()).append("|");
        // 2 REL
        sb.append("RO").append("|");
        // 3 RELA, 4 MAPREASON
        sb.append("||");
        // 5 CUI2
        sb.append(values.get(0)).append("|");
        // 6 MAPIN
        sb.append("Y").append("|");
        sb.append("\n");
        // 0 CUI1
        sb.append(lastReleaseCui).append("|");
        // 1 VER
        sb.append(getProcess().getVersion()).append("|");
        // 2 REL
        sb.append("RO").append("|");
        // 3 RELA, 4 MAPREASON
        sb.append("||");
        // 5 CUI2
        sb.append(values.get(1)).append("|");
        // 6 MAPIN
        sb.append("Y").append("|");
        sb.append("\n");
        writerMap.get("MRCUI.RRF").print(sb.toString());
      }
    }

    updateProgress();

    // Unpublishable concepts get DEL/bequeathal
    // because unpublishable/unpublished concepts have been removed.
    final List<Long> conceptIds = executeSingleComponentIdQuery(
        "select c.id from ConceptJpa c where c.publishable = false "
            + "and c.terminology = :terminology order by c.terminologyId",
        QueryType.JQL, getDefaultQueryParams(getProject()), ConceptJpa.class,
        false);

    int objectCt = 0;
    for (final Long conceptId : conceptIds) {
      final Concept c = getConcept(conceptId);

      // Skip any concept that doesn't have a real CUI
      if (c.getId().toString().equals(c.getTerminologyId())) {
        continue;
      }

      // If does not have a component history (e.g. newly dead)
      if (c.getComponentHistory() == null
          || c.getComponentHistory().isEmpty()) {
        // If no bequeathal rel, -> write out a "DEL" entry to MRCUI
        final List<ConceptRelationship> bequeathalRels = getBequeathalRels(c);
        if (bequeathalRels.size() == 0) {
          final StringBuilder sb = new StringBuilder();
          sb.append(c.getTerminologyId()).append("|");
          sb.append(getProcess().getVersion()).append("|");
          sb.append("DEL").append("|");
          sb.append("|");
          sb.append("|");
          sb.append("|");
          sb.append("|");
          sb.append("\n");
          writerMap.get("MRCUI.RRF").print(sb.toString());
          writerMap.get("DELETEDCUI.RRF")
              .print(c.getTerminology() + "|" + c.getName() + "|\n");
        }
        // If bequeathal rel -> write out bequeathal entry for each rel
        else {
          for (final ConceptRelationship bequeathalRel : bequeathalRels) {
            final StringBuilder sb = new StringBuilder();
            sb.append(c.getTerminologyId()).append("|");
            sb.append(getProcess().getVersion()).append("|");
            sb.append(bequeathalRel.getRelationshipType().substring(1))
                .append("|");
            sb.append("|");
            sb.append("|");
            sb.append(bequeathalRel.getTo().getTerminologyId()).append("|");
            sb.append("Y").append("|");
            sb.append("\n");
            writerMap.get("MRCUI.RRF").print(sb.toString());
          }
        }

      }

      // If it has a component history. (e.g.
      // component.getComponentHistory().size()>0) - i.e. this is a "historical"
      // concept.
      else if (c.getComponentHistory().size() > 0) {
        for (final ComponentHistory history : c.getComponentHistory()) {
          // if DEL -> write out component history as is.
          if (history.getRelationshipType().equals("DEL")) {
            final StringBuilder sb = new StringBuilder();
            // CUI1
            sb.append(c.getTerminologyId()).append("|");
            // 1 VER
            sb.append(history.getVersion()).append("|");
            // 2 REL
            sb.append("DEL").append("|");
            // 3 RELA, 4 MAPREASON, 5 CUI2, 6 MAPIN
            sb.append("||||");
            sb.append("\n");
            writerMap.get("MRCUI.RRF").print(sb.toString());
          }
          // If SY or R?
          else if (history.getRelationshipType().equals("SY")
              || history.getRelationshipType().startsWith("R")) {

            // if "referenced concept" is publishable, write it out as is
            if (history.getReferencedConcept().isPublishable()) {
              final StringBuilder sb = new StringBuilder();
              // CUI1
              sb.append(c.getTerminologyId()).append("|");
              // 1 VER
              sb.append(history.getVersion()).append("|");
              // 2 REL
              sb.append(history.getRelationshipType()).append("|");
              // 3 RELA
              sb.append(history.getAdditionalRelationshipType()).append("|");
              // 4 MAPREASON
              sb.append(history.getReason()).append("|");
              // 5 CUI2
              sb.append(history.getReferencedConcept().getTerminologyId())
                  .append("|");
              // 6 MAPIN
              sb.append("Y").append("|");
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
                // CUI1
                sb.append(c.getTerminologyId()).append("|");
                // 1 VER
                sb.append(history.getVersion()).append("|");
                // 2 REL
                sb.append(history.getRelationshipType()).append("|");
                // 3 RELA
                sb.append(history.getAdditionalRelationshipType()).append("|");
                // 4 MAPREASON
                sb.append(history.getReason()).append("|");
                // 5 CUI2
                sb.append(syCui1Cui2Pairs
                    .get(history.getReferencedConcept().getTerminologyId()))
                    .append("|");
                // 6 MAPIN
                sb.append("|");
                sb.append("\n");
                writerMap.get("MRCUI.RRF").print(sb.toString());
              } else {
                StringBuilder sb = new StringBuilder();
                // CUI1
                sb.append(c.getTerminologyId()).append("|");
                // 1 VER
                sb.append(history.getVersion()).append("|");
                // 2 REL
                sb.append("DEL").append("|");
                // 3 RELA, 4 MAPREASON, 5 CUI2, 6 MAPIN
                sb.append("||||");
                sb.append("\n");
                writerMap.get("MRCUI.RRF").print(sb.toString());
              }
            }
            // otherwise, don't write a MRCUI entry. (e.g this is a component
            // history that will go away.)
          }
        } // end for
      }

      // Periodically log and commit
      logAndCommit(objectCt++, RootService.logCt, RootService.commitCt);

    }

    updateProgress();

  }

  /**
   * Returns the bequeathal rels.
   *
   * @param c the c
   * @return the bequeathal rels
   */
  @SuppressWarnings("static-method")
  private List<ConceptRelationship> getBequeathalRels(Concept c) {
    final List<ConceptRelationship> bequeathalRels = new ArrayList<>();
    for (final ConceptRelationship rel : c.getRelationships()) {
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

    final List<Object[]> results = new ArrayList<>();

    // Find all atoms where the lastReleaseCui != current CUI, write an entry.
    String queryStr = null;
    queryStr = "select value(aid), value(cid) , c.terminologyId "
        + "from ConceptJpa c join c.atoms a join a.conceptTerminologyIds cid "
        + "join a.alternateTerminologyIds aid "
        + "where c.terminology = :terminology and c.version = :version "
        + "and c.publishable = true and a.publishable = true "
        + "and key(aid) = :terminology and key(cid) = :terminology ";
    final Query query = manager.createQuery(queryStr);
    query.setParameter("terminology", getProject().getTerminology());
    query.setParameter("version", getProject().getVersion());
    results.addAll(query.getResultList());
    for (final Object[] objArray : results) {
      final String aui = objArray[0].toString();
      final String lastReleaseCui = objArray[1].toString();
      final String cui = objArray[2].toString();
      if (lastReleaseCui.equals(cui)) {
        continue;
      }

      // Write an entry for each row.
      final StringBuilder sb = new StringBuilder();
      sb.append(aui).append("|");
      sb.append(lastReleaseCui).append("|");
      sb.append(getProcess().getVersion()).append("|");
      sb.append("|");
      sb.append("|");
      sb.append("move").append("|");
      sb.append(aui).append("|");
      sb.append(cui).append("|");
      sb.append("Y").append("|");
      sb.append("\n");
      writerMap.get("MRAUI.RRF").print(sb.toString());
    }
  }

  /**
   * Write nci code cui map.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  private void writeNciCodeCuiMap() throws Exception {
    // This file maps the NCI concept to it's CUI and the preferred terms of
    // each as well.
    // Field Description
    // 0 NCI concept
    // 1 CUI
    // 2 NCI PT
    // 3 CUI PT
    //
    // e.g.
    // C100000|C3272245|Percutaneous Coronary Intervention for ST Elevation
    // Myocardial Infarction-Stable-Over 12 Hours From Symptom
    // Onset|Percutaneous Coronary Intervention for ST Elevation Myocardial
    // Infarction-Stable-Over 12 Hours From Symptom Onset|
    // C100001|C3272246|Percutaneous Coronary Intervention for ST Elevation
    // Myocardial Infarction-Stable After Successful Full-Dose Thrombolytic
    // Therapy|Percutaneous Coronary Intervention for ST Elevation Myocardial
    // Infarction-Stable After Successful Full-Dose Thrombolytic Therapy|
    // C100002|C3272247|Percutaneous Coronary Intervention for ST Elevation
    // Myocardial Infarction-Unstable-Over 12 Hours From Symptom
    // Onset|Percutaneous Coronary Intervention for ST Elevation Myocardial
    // Infarction-Unstable-Over 12 Hours From Symptom Onset|
    // C100003|C3272248|Percutaneous Mitral Valve Repair|Percutaneous Mitral
    // Valve Repair|

    final List<Object[]> results = new ArrayList<>();
    DefaultComputePreferredNameHandler handler =
        new DefaultComputePreferredNameHandler();

    String queryStr = null;
    queryStr = "select a.id, b.id from ConceptJpa a join a.atoms aa, "
        + "ConceptJpa b join b.atoms ba "
        + "where aa.id = ba.id and a.terminology='NCI' and aa.termType='PT' and b.terminology=:projectTerminology";
    final javax.persistence.Query query = manager.createQuery(queryStr);
    query.setParameter("projectTerminology", getProject().getTerminology());
    results.addAll(query.getResultList());
    for (Object[] objArray : results) {
      final Long id1 = ((Long) (objArray[0])).longValue();
      final Long id2 = ((Long) (objArray[1])).longValue();
      final Concept concept1 = this.getConcept(id1);
      final Concept concept2 = this.getConcept(id2);
      final Atom preferredAtom1 =
          handler
              .sortAtoms(concept1.getAtoms(), getPrecedenceList(
                  getProject().getTerminology(), getProject().getVersion()))
              .get(0);
      final Atom preferredAtom2 =
          handler
              .sortAtoms(concept2.getAtoms(), getPrecedenceList(
                  getProject().getTerminology(), getProject().getVersion()))
              .get(0);

      // Write an entry for each row.
      StringBuilder sb = new StringBuilder();
      sb.append(concept1.getTerminologyId()).append("|");
      sb.append(concept2.getTerminologyId()).append("|");
      sb.append(preferredAtom1.getName()).append("|");
      sb.append(preferredAtom2.getName()).append("|");
      sb.append("\n");
      writerMap.get("nci_code_cui_map_" + getProcess().getVersion() + ".dat")
          .print(sb.toString());
    }
  }

  /**
   * Write history.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  private void writeHistory() throws Exception {
    // This file maps the NCI concept to it's CUI and the preferred terms of
    // each as well.
    // Field Description
    // 0 CUI1
    // 1 CUI1.name
    // 2 split or merge or retire
    // 3 date
    // 4 CUI2
    // 5 CUI2.name
    //
    // e.g.
    // # CUI, preferredName, type, dd-MMM-yyy (for $release+01), CUI2,pn
    // C0000266|Parlodel|split|15-dec-2016|C0546852|Bromocriptine Mesylate
    // C0000325|20-Methylcholanthrene|split|15-dec-2016|C0025732|20-Methylcholanthrene
    // C0000473|4-Aminobenzoic Acid|split|15-dec-2016|C0000473|4-Aminobenzoic
    // Acid
    // C0000530|5'-NUCLEOTIDASE|split|15-dec-2016|C0000530|5'-NUCLEOTIDASE
    // C0000545|Eicosapentaenoic
    // Acid|split|15-dec-2016|C0000545|Eicosapentaenoic Acid
    // C0000598|Ticlopidine Hydrochloride|merge|15-dec-2016|C0000598|Ticlopidine
    // Hydrochloride
    // C0000719|Abbott 46811|split|15-dec-2016|C0887647|Cefsulodin Sodium

    // splits
    final Set<String> currentCuis = new HashSet<>();
    String queryStr = null;
    javax.persistence.Query query = null;
    queryStr = "select c.terminologyId from ConceptJpa c "
        + "where c.terminology = :terminology and c.version = :version "
        + "and c.publishable = true";
    query = manager.createQuery(queryStr);
    query.setParameter("terminology", getProject().getTerminology());
    query.setParameter("version", getProject().getVersion());
    currentCuis.addAll(query.getResultList());

    final Map<String, Set<String>> atomsMoved = new HashMap<>();
    queryStr = "select distinct value(cid), c.terminologyId  "
        + "from ConceptJpa c join c.atoms a join a.conceptTerminologyIds cid "
        + "where c.terminology = :terminology and c.version = :version "
        + "and c.publishable = true and a.publishable = true "
        + "and key(cid) = :terminology";
    query = manager.createQuery(queryStr);
    query.setParameter("terminology", getProject().getTerminology());
    query.setParameter("version", getProject().getVersion());
    final List<Object[]> results = query.getResultList();
    for (final Object[] objArray : results) {
      final String lastReleaseCui = objArray[0].toString();
      final String cui = objArray[1].toString();
      if (lastReleaseCui.equals(cui)) {
        continue;
      }
      if (!atomsMoved.containsKey(lastReleaseCui)) {
        atomsMoved.put(lastReleaseCui, new HashSet<>());
      }
      atomsMoved.get(lastReleaseCui).add(cui);
    }
    // Determine "split" cases - all keys from atomsMoved where the value is
    // size()>1 and the key is not in current cuis.
    // write RO rows for both "value" CUIs.
    // Note: split concept must be merged into third concept in order to meet
    // !currentCuis requirement
    for (final Entry<String, Set<String>> entry : atomsMoved.entrySet()) {
      final String lastReleaseCui = entry.getKey();
      final Concept lastReleaseConcept =
          getConcept(lastReleaseCui, getProcess().getTerminology(),
              getProcess().getVersion(), Branch.ROOT);

      if (entry.getValue().size() > 1
          && !currentCuis.contains(lastReleaseCui)) {
        // write RO rows for both "value" CUIs.
        final List<String> values = new ArrayList<>(entry.getValue());
        final StringBuilder sb = new StringBuilder();
        sb.append(lastReleaseCui).append("|"); // 0 CUI1
        sb.append(lastReleaseConcept.getName()).append("|"); // 1 NAME
        sb.append("").append("|"); // 2 DATE
        sb.append("split|"); // 3 TYPE
        Concept concept =
            getConcept(values.get(0), getProcess().getTerminology(),
                getProcess().getVersion(), Branch.ROOT);
        sb.append(values.get(0)).append("|"); // 4 CUI2
        sb.append(concept.getName()).append("|"); // 5 NAME
        sb.append("\n");
        sb.append(lastReleaseCui).append("|"); // 0 CUI1
        sb.append(getProcess().getVersion()).append("|"); // 1 NAME
        sb.append("").append("|"); // 2 DATE
        sb.append("split|"); // 3 TYPE
        concept = getConcept(values.get(1), getProcess().getTerminology(),
            getProcess().getVersion(), Branch.ROOT);
        sb.append(values.get(1)).append("|"); // 4 CUI2
        sb.append(concept.getName()).append("|"); // 5 NAME
        sb.append("\n");

        writerMap.get("NCIMEME_" + getProcess().getVersion() + "_history.txt")
            .print(sb.toString());
      }
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

    // Make "CHANGE" directory
    final File changeDir = new File(dir, "CHANGE");
    changeDir.mkdirs();
    writerMap.put("MRAUI.RRF",
        new PrintWriter(new FileWriter(new File(dir, "MRAUI.RRF"))));
    writerMap.put("MRCUI.RRF",
        new PrintWriter(new FileWriter(new File(dir, "MRCUI.RRF"))));
    String fileName = "nci_code_cui_map_" + getProcess().getVersion() + ".dat";
    writerMap.put(fileName,
        new PrintWriter(new FileWriter(new File(dir, fileName))));
    fileName = "NCIMEME_" + getProcess().getVersion() + "_history.txt";
    writerMap.put(fileName,
        new PrintWriter(new FileWriter(new File(dir, fileName))));
    writerMap.put("DELETEDCUI.RRF",
        new PrintWriter(new FileWriter(new File(changeDir, "DELETEDCUI.RRF"))));
    writerMap.put("DELETEDLUI.RRF",
        new PrintWriter(new FileWriter(new File(changeDir, "DELETEDLUI.RRF"))));
    writerMap.put("DELETEDSUI.RRF",
        new PrintWriter(new FileWriter(new File(changeDir, "DELETEDSUI.RRF"))));
    writerMap.put("MERGEDCUI.RRF",
        new PrintWriter(new FileWriter(new File(changeDir, "MERGEDCUI.RRF"))));
    writerMap.put("MERGEDLUI.RRF",
        new PrintWriter(new FileWriter(new File(changeDir, "MERGEDLUI.RRF"))));
    writerMap.put("MERGEDSUI.RRF",
        new PrintWriter(new FileWriter(new File(changeDir, "MERGEDSUI.RRF"))));
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
    logInfo("Starting RESET " + getName());
    // cleanup
    dir = new File(config.getProperty("source.data.dir") + "/"
        + getProcess().getInputPath() + "/" + getProcess().getVersion() + "/"
        + "META");
    final File changeDir = new File(dir, "CHANGE");
    FileUtils.deleteDirectory(changeDir);

    FileUtils.forceDelete(new File(dir, "MRCUI.RRF"));
    FileUtils.forceDelete(new File(dir, "MRAUI.RRF"));

    final String ncifile =
        "nci_code_cui_map_" + getProcess().getVersion() + ".dat";
    FileUtils.forceDelete(new File(dir, ncifile));
    final String ncimemefile =
        "NCIMEME_" + getProcess().getVersion() + "_history.txt";
    FileUtils.forceDelete(new File(dir, ncimemefile));

    logInfo("Finished RESET " + getName());
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

  /* see superclass */
  @Override
  public String getDescription() {
    return ConfigUtility.getNameFromClass(getClass());
  }
}
