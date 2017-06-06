/*
 *    Copyright 2017 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.release;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
import com.wci.umls.server.jpa.content.ComponentHistoryJpa;
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

  /** The history. */
  private ConceptHistory history = new ConceptHistory();

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
  @SuppressWarnings("unchecked")
  @Override
  public void compute() throws Exception {
    logInfo("Starting " + getName());
    fireProgressEvent(0, "Starting");

    setSteps(7);
    openWriters();

    logInfo("  Determine current CUIs");
    final Set<String> currentCuis = new HashSet<>();
    Query query =
        manager.createQuery("select c.terminologyId from ConceptJpa c "
            + "where c.terminology = :terminology and c.version = :version "
            + "and c.publishable = true");
    query.setParameter("terminology", getProject().getTerminology());
    query.setParameter("version", getProject().getVersion());
    currentCuis.addAll(query.getResultList());
    updateProgress();

    logInfo("  Determine previously released CUIs");
    final Set<String> previousCuis = new HashSet<>();
    // Last relesed CUIs
    query = manager.createQuery("select distinct value(cid) "
        + "from AtomJpa a join a.conceptTerminologyIds cid "
        + "where key(cid) = :terminology");
    query.setParameter("terminology", getProject().getTerminology());
    previousCuis.addAll(query.getResultList());

    // prior historical CUIs
    query = manager.createQuery("select terminologyId from ConceptJpa a "
        + "where terminology = :terminology and id != terminologyId"
        + "  and publishable = false");
    query.setParameter("terminology", getProject().getTerminology());
    previousCuis.addAll(query.getResultList());
    updateProgress();

    logInfo("  Write MRAUI.RRF");
    writeMraui();
    updateProgress();

    // This also populates the "ConceptHistory" object
    logInfo("  Write MRCUI.RRF");
    writeMrcui(previousCuis, currentCuis);
    updateProgress();

    logInfo("  Write NCI code file");
    writeNciCodeCuiMap();
    updateProgress();

    logInfo("  Write NCIMETA history file");
    writeNciMetaHistory(previousCuis, currentCuis);
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
      if (outputFile.exists()) {
        outputFile.delete();
      }
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
   * @param previousCuis the previous cuis
   * @param currentCuis the current cuis
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  private void writeMrcui(Set<String> previousCuis, Set<String> currentCuis)
    throws Exception {

    // 0 CUI1
    // 1 VER
    // 2 REL
    // 3 RELA
    // 4 MAPREASON
    // 5 CUI2
    // 6 MAPIN
    // C0000401|1993AA|DEL|||||
    // C0000431|2013AB|RB|||C1394823|Y|
    // C0000703|1993AA|SY|||C0002691|Y|

    // Atoms that moved from the previous release
    final Map<String, Set<String>> atomsMoved = new HashMap<>();
    final Query query =
        manager.createQuery("select distinct value(cid), c.terminologyId  "
            + "from ConceptJpa c join c.atoms a join a.conceptTerminologyIds cid "
            + "where c.terminology = :terminology and c.version = :version "
            + "and c.publishable = true and a.publishable = true "
            + "and key(cid) = :terminology");
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
    for (final Entry<String, Set<String>> entry : atomsMoved.entrySet()) {
      final String lastReleaseCui = entry.getKey();
      if (entry.getValue().size() == 1
          && !currentCuis.contains(lastReleaseCui)) {
        final String cui2 = (String) entry.getValue().toArray()[0];

        // Add a merge
        history.addMerge(lastReleaseCui, getProcess().getVersion(), cui2);

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
        // write RO rows for all "value" CUIs.
        for (final String cui2 : entry.getValue()) {
          history.addBequeathal(lastReleaseCui, getProcess().getVersion(), "RO",
              cui2);
        }
      }
    }

    updateProgress();

    // Look for bequeathal rels or historical component history info among
    // unpublishable concepts
    final List<Long> conceptIds = executeSingleComponentIdQuery(
        "select c.id from ConceptJpa c where c.publishable = false "
            + "and c.terminology = :terminology order by c.terminologyId",
        QueryType.JPQL, getDefaultQueryParams(getProject()), ConceptJpa.class,
        false);

    int objectCt = 0;
    for (final Long conceptId : conceptIds) {
      final Concept c = getConcept(conceptId);

      // Skip any concepts never assigned a CUI
      if (c.getId().toString().equals(c.getTerminologyId())) {
        continue;
      }

      // Get bequeathal rels added by editors or insertions
      final List<ConceptRelationship> bequeathalRels =
          getCurrentBequeathalRels(c);

      // If does not have a component history (e.g. newly dead)
      if (c.getComponentHistory() == null
          || c.getComponentHistory().isEmpty()) {

        // If no bequeathal rel -> add a DEL entry
        if (bequeathalRels.size() == 0) {

          history.addDeleted(c.getTerminologyId(), getProcess().getVersion());

        }
        // Otherwise -> add a bequeathal rel entry
        else {
          for (final ConceptRelationship bequeathalRel : bequeathalRels) {
            history.addBequeathal(c.getTerminologyId(),
                getProcess().getVersion(), bequeathalRel.getRelationshipType(),
                bequeathalRel.getTo().getTerminologyId());

          }
        }
      }

      // If there is component history, add it
      else if (c.getComponentHistory().size() > 0) {
        for (final ComponentHistory ch : c.getComponentHistory()) {
          // if DEL -> write out component history as is.
          if (ch.getRelationshipType().equals("DEL")) {
            history.addDeleted(c.getTerminologyId(), ch.getVersion());

          }

          // If SY or R?
          else if (ch.getRelationshipType().equals("SY")
              || ch.getRelationshipType().startsWith("R")) {

            if (ch.getRelationshipType().equals("SY")) {
              history.addMerge(c.getTerminologyId(), ch.getVersion(),
                  ch.getReferencedTerminologyId());
            }

            else {
              history.addBequeathal(c.getTerminologyId(), ch.getVersion(),
                  ch.getRelationshipType(), ch.getReferencedTerminologyId());

            }

          }
        }
      }

      logAndCommit(objectCt++, RootService.logCt, RootService.commitCt);
    }

    //
    // Go through all built up concept history
    //
    logInfo("  Historical CUIs = " + history.getTerminologyIds().size());
    for (final String cui : history.getTerminologyIds()) {

      // Get facts
      final Set<ComponentHistory> facts =
          history.getFacts(cui, previousCuis, currentCuis);
      // Write these entries out
      for (final ComponentHistory fact : facts) {
        // C1584235|201508|RO|||C0000294|Y|
        final StringBuilder sb = new StringBuilder();
        sb.append(fact.getTerminologyId()).append("|");
        sb.append(fact.getAssociatedRelease()).append("|");
        sb.append(fact.getRelationshipType()).append("|||");
        sb.append(fact.getReferencedTerminologyId() == null ? ""
            : fact.getReferencedTerminologyId()).append("|");
        sb.append("Y|\n");
        writerMap.get("MRCUI.RRF").print(sb.toString());
        if (fact.getRelationshipType().equals("SY")) {
          writerMap.get("MERGEDCUI.RRF").print(fact.getTerminologyId() + "|"
              + fact.getReferencedTerminologyId() + "|\n");

        }
        if (fact.getRelationshipType().equals("DEL")) {

          // Write out NCI META history file retirements
          writerMap.get("DELETEDCUI.RRF")
              .print(fact.getTerminologyId() + "|"
                  + getConcept(fact.getTerminologyId(),
                      getProject().getTerminology(), getProject().getVersion(),
                      Branch.ROOT).getName()
                  + "|\n");
        }
      }

    }

  }

  /**
   * Returns the bequeathal rels.
   *
   * @param c the c
   * @return the bequeathal rels
   */
  @SuppressWarnings("static-method")
  private List<ConceptRelationship> getCurrentBequeathalRels(Concept c) {
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

    // 0 AUI1
    // 1 CUI1
    // 2 VER
    // 3 REL
    // 4 RELA
    // 5 MAPREASON
    // 6 AUI2
    // 7 CUI2
    // 8 MAPIN
    //
    // e.g.
    // A0009348|C0030499|201604|||move|A0009348|C0747256|Y|

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
    queryStr = "select distinct scui.id, cui.id "
        + "from ConceptJpa cui join cui.atoms aa, "
        + "     ConceptJpa scui join scui.atoms ba "
        + "where aa.id = ba.id and scui.terminology = 'NCI' "
        + "  and aa.termType = 'PT' and cui.terminology = :projectTerminology";
    final Query query = manager.createQuery(queryStr);
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
   * @param previousCuis the previous cuis
   * @param currentCuis the current cuis
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  private void writeNciMetaHistory(Set<String> previousCuis,
    Set<String> currentCuis)

    throws Exception {
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

    // C0000325|20-Methylcholanthrene|split|15-dec-2016|C0025732|20-Methylcholanthrene
    // CL503757|Zebrafish Model Organism Database, 2016_04D|retire|15-dec-2016||
    // CL505143|Zinc Finger Protein 224|merge|15-dec-2016|C1173181|Zinc Finger
    // Protein 224

    final Map<String, Set<String>> atomsMoved = new HashMap<>();
    Query query =
        manager.createQuery("select distinct value(cid), c.terminologyId  "
            + "from ConceptJpa c join c.atoms a join a.conceptTerminologyIds cid "
            + "where c.terminology = :terminology and c.version = :version "
            + "and c.publishable = true and a.publishable = true "
            + "and key(cid) = :terminology");
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
    final Set<String> splitCuis = new HashSet<>();
    for (final Entry<String, Set<String>> entry : atomsMoved.entrySet()) {
      final String lastReleaseCui = entry.getKey();

      if (entry.getValue().size() > 1 && currentCuis.contains(lastReleaseCui)) {

        for (final String cui2 : entry.getValue()) {

          // Skip entries for the concept itself, and skip entries that were
          // prior CUIs
          if (previousCuis.contains(cui2) || lastReleaseCui.equals(cui2)) {
            continue;
          }

          splitCuis.add(cui2);

          final Concept lastReleaseConcept =
              getConcept(lastReleaseCui, getProcess().getTerminology(),
                  getProcess().getVersion(), Branch.ROOT);

          // write RO rows for both "value" CUIs.
          final StringBuilder sb = new StringBuilder();
          // 0 CUI1
          sb.append(lastReleaseCui).append("|");
          // 1 NAME
          sb.append(lastReleaseConcept.getName()).append("|");
          // 2 DATE
          sb.append(convertDate(getProcess().getVersion() + "01")).append("|");
          // 3 TYPE
          sb.append("split|");
          final Concept concept =
              getConcept(cui2, getProcess().getTerminology(),
                  getProcess().getVersion(), Branch.ROOT);
          // 4 CUI2
          sb.append(cui2).append("|");
          // 5 NAME
          sb.append(concept.getName());
          sb.append("\n");

          writerMap.get("NCIMEME_" + getProcess().getVersion() + "_history.txt")
              .print(sb.toString());

        }

      }
    }

    // Handle "merge" and "retire" cases
    final Set<String> retiredCuis = new HashSet<>();
    for (final String cui : history.getTerminologyIds()) {

      // Get facts
      final Set<ComponentHistory> facts =
          history.getFacts(cui, previousCuis, currentCuis);
      // Write these entries out
      for (final ComponentHistory fact : facts) {
        String type = null;
        Concept concept2 = null;

        if (fact.getRelationshipType().equals("SY")) {
          type = "merge";
          concept2 = getConcept(fact.getReferencedTerminologyId(),
              getProject().getTerminology(), getProject().getVersion(),
              Branch.ROOT);
        } else if (!retiredCuis.contains(cui) && !splitCuis.contains(cui)) {
          retiredCuis.add(cui);
          type = "retire";
        }

        else {
          // split, do nothing
        }

        // Get the dead concept's name
        final Set<Atom> atoms = new HashSet<>();
        for (final Concept concept : findConcepts(getProject().getTerminology(),
            getProject().getVersion(),
            Branch.ROOT, "atom.conceptTerminologyIds:\""
                + getProject().getTerminology() + "=" + cui + "\"",
            null).getObjects()) {
          // Add all atoms having a last release CUI matching the CUI
          atoms.addAll(concept.getAtoms().stream()
              .filter(a -> a.getConceptTerminologyIds()
                  .get(getProject().getTerminology()).equals(cui))
              .collect(Collectors.toSet()));

        }
        String oldConceptName = null;
        if (atoms.size() == 0) {
          final Concept concept = getConcept(cui, getProject().getTerminology(),
              getProject().getVersion(), Branch.ROOT);
          if (concept == null) {
            throw new Exception("Unable to find name for dead cui = " + cui);
          }
          oldConceptName = concept.getName();
        } else {
          getComputePreferredNameHandler(getProject().getTerminology())
              .computePreferredName(atoms, getPrecedenceList(
                  getProject().getTerminology(), getProject().getVersion()));
        }

        final StringBuilder sb = new StringBuilder();
        // 0 CUI1
        sb.append(fact.getTerminologyId()).append("|");
        // 1 NAME
        sb.append(oldConceptName).append("|");
        // 2 DATE
        sb.append(convertDate(getProcess().getVersion() + "01")).append("|");
        // 3 TYPE
        sb.append(type).append("|");
        // 4 CUI2
        sb.append(concept2 == null ? "" : concept2.getTerminologyId())
            .append("|");
        // 5 NAME
        sb.append(concept2 == null ? "" : concept2.getName());
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

  /**
   * Convert date.
   *
   * @param inputDate the input date
   * @return the string
   */
  @SuppressWarnings("static-method")
  private String convertDate(String inputDate) {
    SimpleDateFormat dt = new SimpleDateFormat("yyyyMMdd");
    Date date;
    try {
      date = dt.parse(inputDate);
      SimpleDateFormat dt1 = new SimpleDateFormat("dd-MMM-yyyy");
      return dt1.format(date);
    } catch (ParseException e) {
      e.printStackTrace();
    }
    return "";
  }

  /**
   * Local class for managing concept history.
   */
  class ConceptHistory {

    /** The deleted cuis. */
    private Map<String, Set<ComponentHistory>> factMap = new HashMap<>();

    /**
     * Returns the terminology ids.
     *
     * @return the terminology ids
     */
    public List<String> getTerminologyIds() {
      final List<String> facts = new ArrayList<>(factMap.keySet());
      Collections.sort(facts);
      return facts;
    }

    /**
     * Returns the facts for the specified concept.
     *
     * @param cui the cui
     * @param currentCuis the current cuis
     * @param previousCuis the previous cuis
     * @return the facts
     * @throws Exception the exception
     */
    public Set<ComponentHistory> getFacts(String cui, Set<String> previousCuis,
      Set<String> currentCuis) throws Exception {

      // If the CUI is current, there are no entries
      if (currentCuis.contains(cui)) {
        return new HashSet<>(0);
      }

      // If the CUI1 was not a prior CUI, there are no entries
      if (!previousCuis.contains(cui)) {
        return new HashSet<>(0);

      }

      final Set<ComponentHistory> facts = factMap.get(cui);
      // All facts either have a blank CUI2 or a CUI2 that is alive.
      boolean validCui2 = facts.stream()
          .filter(item -> item.getReferencedTerminologyId() != null
              && !currentCuis.contains(item.getReferencedTerminologyId()))
          .collect(Collectors.toSet()).size() == 0;
      final Set<String> releases = facts.stream()
          .map(item -> item.getAssociatedRelease()).collect(Collectors.toSet());
      final Set<String> relTypes =
          facts.stream().map(item -> item.getRelationshipType().substring(0, 1))
              .collect(Collectors.toSet());
      final Set<ComponentHistory> delFacts = facts.stream()
          .filter(item -> item.getRelationshipType().equals("DEL"))
          .collect(Collectors.toSet());
      final Set<ComponentHistory> syFacts =
          facts.stream().filter(item -> item.getRelationshipType().equals("SY"))
              .collect(Collectors.toSet());
      final Set<ComponentHistory> relFacts = facts.stream()
          .filter(item -> item.getRelationshipType().startsWith("R"))
          .collect(Collectors.toSet());

      // If there are facts from multiple releases, throw an error, this
      // shouldn't happen
      if (releases.size() > 1) {
        throw new Exception(
            "Unexpected facts from multiple releases = " + releases);
      }

      // Handle simpler cases
      if (validCui2) {

        //
        // If there is a single entry, return it
        //
        if (facts.size() == 1) {
          return facts;
        }

        //
        // If there are multiple RO entries, return them
        //
        if (relTypes.size() != 1 && relTypes.iterator().next().equals("R")) {
          return facts;
        }

        //
        // Otherwise, favor SY, then R, then DEL
        //
        if (syFacts.size() == 1) {
          return syFacts;
        } else if (relFacts.size() > 0) {
          return relFacts;
        } else if (delFacts.size() == 1) {
          return delFacts;
        } else {
          throw new Exception("Unexpected combination of facts = " + facts);
        }
      }

      // Handle complex cases where there is a dead CUI2
      else {
        // Expect these to be not the current release
        if (releases.iterator().next().equals(getProcess().getVersion())) {
          throw new Exception(
              "Unexpected dead CUI2 cases with current version = " + facts);
        }

        //
        // SY -> dead CUI2
        //
        if (syFacts.size() == 1) {
          final Set<ComponentHistory> cui2Facts =
              getFacts(syFacts.iterator().next().getReferencedTerminologyId(),
                  previousCuis, currentCuis);

          // If single SY, then just update CUI2 and return
          if (cui2Facts.size() == 1 && cui2Facts.iterator().next()
              .getRelationshipType().equals("SY")) {
            syFacts.iterator().next().setReferencedTerminologyId(
                cui2Facts.iterator().next().getReferencedTerminologyId());
            return syFacts;
          }

        }

        //
        // R -> dead CUI2
        //
        else if (syFacts.size() == 0 && delFacts.size() == 0
            && relFacts.size() > 0) {
          final Set<ComponentHistory> newFacts = new HashSet<>();
          for (final ComponentHistory fact : relFacts) {
            final Set<ComponentHistory> cui2Facts =
                getFacts(syFacts.iterator().next().getReferencedTerminologyId(),
                    previousCuis, currentCuis);

            // If single SY, then just update CUI2 and add to newFacts
            if (cui2Facts.size() == 1 && cui2Facts.iterator().next()
                .getRelationshipType().equals("SY")) {
              fact.setReferencedTerminologyId(
                  cui2Facts.iterator().next().getReferencedTerminologyId());
              newFacts.add(fact);
            }

            // If DEL, then skip
            else if (cui2Facts.size() == 1 && cui2Facts.iterator().next()
                .getRelationshipType().equals("SY")) {
              continue;
            }

            // If R, then replicate input to output
            // Keep REL if they match, otherwise use RO
            else if (cui2Facts.size() > 0 && cui2Facts.iterator().next()
                .getRelationshipType().startsWith("R")) {
              for (final ComponentHistory cui2RelFact : cui2Facts) {
                // if we encounter a non-R thing,fail
                if (!cui2RelFact.getRelationshipType().startsWith("R")) {
                  throw new Exception(
                      "Unexpected mixture of R and non-R = " + cui2Facts);
                }
                final ComponentHistory newFact = new ComponentHistoryJpa();
                newFact.setTerminologyId(cui);
                newFact.setAssociatedRelease(fact.getAssociatedRelease());
                newFact.setRelationshipType(cui2RelFact.getRelationshipType()
                    .equals(fact.getRelationshipType())
                        ? fact.getRelationshipType() : "RO");
                newFact.setReferencedTerminologyId(
                    cui2RelFact.getReferencedTerminologyId());
                newFacts.add(newFact);
              }
            }
          }
          return newFacts;
        }

        //
        // Otherwise, fail
        //
        else {
          throw new Exception(
              "Unexpected state of facts from prior version = " + facts);
        }

      }

      return new HashSet<>();

    }

    /**
     * Adds the deleted.
     *
     * @param cui the cui
     * @param release the release
     */
    public void addDeleted(String cui, String release) {
      final ComponentHistory history = new ComponentHistoryJpa();
      history.setTerminologyId(cui);
      history.setAssociatedRelease(release);
      history.setRelationshipType("DEL");
      if (!factMap.containsKey(cui)) {
        factMap.put(cui, new HashSet<>());
      }
      factMap.get(cui).add(history);
    }

    /**
     * Adds the bequeathal.
     *
     * @param cui the cui
     * @param release the version
     * @param rel the rel
     * @param cui2 the cui 2
     * @throws Exception the exception
     */
    public void addBequeathal(String cui, String release, String rel,
      String cui2) throws Exception {
      final ComponentHistory history = new ComponentHistoryJpa();
      history.setTerminologyId(cui);
      history.setAssociatedRelease(release);
      history.setRelationshipType(rel);
      history.setReferencedTerminologyId(cui2);
      if (!factMap.containsKey(cui)) {
        factMap.put(cui, new HashSet<>());
      }
      factMap.get(cui).add(history);
    }

    /**
     * Adds the merge.
     *
     * @param cui the cui
     * @param release the release
     * @param cui2 the cui 2
     * @throws Exception the exception
     */
    public void addMerge(String cui, String release, String cui2)
      throws Exception {
      final ComponentHistory history = new ComponentHistoryJpa();
      history.setTerminologyId(cui);
      history.setAssociatedRelease(release);
      history.setRelationshipType("SY");
      history.setReferencedTerminologyId(cui2);
      if (!factMap.containsKey(cui)) {
        factMap.put(cui, new HashSet<>());
      }
      factMap.get(cui).add(history);
    }

  }

}
