/*
 *    Copyright 2017 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo;

import java.io.File;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.Query;

import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.log4j.Logger;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;

import com.google.common.io.Files;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.FieldedStringTokenizer;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.content.AtomNoteJpa;
import com.wci.umls.server.jpa.content.AtomRelationshipJpa;
import com.wci.umls.server.jpa.content.CodeRelationshipJpa;
import com.wci.umls.server.jpa.content.ConceptNoteJpa;
import com.wci.umls.server.jpa.content.ConceptRelationshipJpa;
import com.wci.umls.server.jpa.content.DescriptorRelationshipJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.AtomRelationship;
import com.wci.umls.server.model.content.CodeRelationship;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.content.DescriptorRelationship;
import com.wci.umls.server.model.workflow.WorkflowStatus;
import com.wci.umls.server.services.RootService;
import com.wci.umls.server.services.handlers.SearchHandler;

/**
 * Implementation of an algorithm to import unpublished MEME4 data that does not
 * appear in RRF.
 */
public class RrfUnpublishedLoaderAlgorithm
    extends AbstractTerminologyLoaderAlgorithm {

  /**
   * Instantiates an empty {@link RrfUnpublishedLoaderAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public RrfUnpublishedLoaderAlgorithm() throws Exception {
    super();
  }

  /* see superclass */
  @Override
  public void compute() throws Exception {

    logInfo("Start loading unpublished MEME4 RRF data");
    logInfo("  terminology = " + getTerminology());
    logInfo("  version = " + getVersion());
    logInfo("  inputDir = " + getInputPath());
    // Track system level information
    long startTimeOrig = System.nanoTime();
    // control transaction scope
    setTransactionPerOperation(false);
    // Turn of ID computation when loading a terminology
    setAssignIdentifiersFlag(false);
    // Let loader set last modified flags.
    setLastModifiedFlag(false);
    // Turn off action handling
    setMolecularActionFlag(false);

    loadDeletedConceptNames();
    loadConceptNotes();
    loadAtomNotes();
    loadIntegrityData();
    loadSrcAtomIds();
    loadXrRelationships();
    loadRuiDa();
    loadUmlscui();

    logInfo("Done ...");

    // Final logging messages
    Logger.getLogger(getClass())
        .info("      elapsed time = " + getTotalElapsedTimeStr(startTimeOrig));
    Logger.getLogger(getClass()).info("done ...");

  }

  /**
   * Load deleted concept names.
   *
   * @throws Exception the exception
   */
  public void loadDeletedConceptNames() throws Exception {

    // ==> deletedCuiNames.txt <==
    // C0000266|Parlodel
    // C0000325|20-Methylcholanthrene
    // C0000366|3' orf Protein
    // C0000431|3-Methylcholanthrene
    // C0000598|Ticlopidine Hydrochloride
    // C0000612|6 Carboxycellulose
    // C0000719|Abbott 46811
    // C0000733|Abdominal Injury
    // C0000791|Abortion Law
    // C0000811|Therapeutic Abortion

    // For each concept in the file, find the CUI, expect it to be
    // unpublished, and set the name and save
    logInfo("  Load deleted concept names");
    setLastModifiedBy("admin");
    setMolecularActionFlag(false);

    final List<String> lines =
        Files.readLines(new File(getInputPath(), "deletedCuiNames.txt"),
            Charset.forName("UTF-8"));

    int ct = 0;
    for (final String line : lines) {
      final String[] tokens = FieldedStringTokenizer.split(line, "|");
      final String cui = tokens[0];
      final String name = tokens[1];
      final Concept concept =
          getConcept(cui, getTerminology(), getVersion(), Branch.ROOT);
      // Skip concepts
      if (concept == null) {
        logInfo("    skip nonexistent = " + cui);
        continue;
      }
      // Skip publishable concepts
      if (concept.isPublishable()) {
        logInfo("    skip publishable = " + cui);
        continue;
      }

      concept.setName(name);
      updateConcept(concept);
      logAndCommit(++ct, RootService.logCt, RootService.commitCt);
    }
    commitClearBegin();
    logInfo("    count = " + ct);
    logInfo("    done.");

  }

  /**
   * Load concept notes.
   *
   * @throws Exception the exception
   */
  public void loadConceptNotes() throws Exception {
    // C3250443|<>Long_Attribute<>:46350541|E-CFC|22-SEP-11
    // C1996431|<>Long_Attribute<>:63319088|E-GSC|18-DEC-13
    // CL031047|<>Long_Attribute<>:29498031|E-LLW|27-AUG-09
    // C0674431|<>Long_Attribute<>:65682679|E-MNC|01-MAR-14
    // C0389836|<>Long_Attribute<>:85273873|E-GSC|23-SEP-16
    // CL449028|<>Long_Attribute<>:32239781|E-AKJ|03-DEC-09
    // C1705617|<>Long_Attribute<>:11112409|E-LLW|06-JUN-06
    // C0282042|<>Long_Attribute<>:13241025|E-HAG|06-FEB-07
    // C2984172|Dec 28, 2012-- Discussed with Terry, NICHD belongs with the
    // gene. LLW|E-LLW|29-NOV-12
    // C0113293|note: "Precedex" is actually dexmedetomidine hydrochloride; this
    // will be fixed in NCIt. MNC 4/20/11|E-MNC|20-APR-11

    // For each line in the file, create a concept note if the CUI exists
    logInfo("  Load concept notes");
    setLastModifiedBy("admin");
    setMolecularActionFlag(false);

    final List<String> lines = Files.readLines(
        new File(getInputPath(), "conceptNotes.txt"), Charset.forName("UTF-8"));

    int ct = 0;
    // dates to parese - 29-NOV-12
    final DateFormat df = new SimpleDateFormat("dd-MMM-yy");
    for (final String line : lines) {
      final String[] tokens = FieldedStringTokenizer.split(line, "|");

      final String cui = tokens[0];
      final String note = tokens[1];
      final String lastModifiedBy = tokens[2];
      final String dateStr = tokens[3];
      final Date date = df.parse(dateStr);

      final Concept concept =
          getConcept(cui, getTerminology(), getVersion(), Branch.ROOT);
      // Skip concepts
      if (concept != null) {
        logInfo("    add note " + cui);
      } else {
        continue;
      }

      final ConceptNoteJpa cnote = new ConceptNoteJpa();
      cnote.setNote(note);
      cnote.setConcept(concept);
      cnote.setLastModifiedBy(lastModifiedBy);
      cnote.setLastModified(date);
      cnote.setTimestamp(date);
      // manage lastmodified/by
      setLastModifiedFlag(false);
      addNote(cnote);
      setLastModifiedFlag(true);

      concept.getNotes().add(cnote);
      updateConcept(concept);

      logAndCommit(++ct, RootService.logCt, RootService.commitCt);
    }
    commitClearBegin();
    logInfo("    count = " + ct);
    logInfo("    done.");

  }

  /**
   * Load atom notes.
   *
   * @throws Exception the exception
   */
  public void loadAtomNotes() throws Exception {
    // A3250443|<>Long_Attribute<>:46350541|E-CFC|22-SEP-11
    // A1996431|<>Long_Attribute<>:63319088|E-GSC|18-DEC-13

    // For each line in the file, create a concept note if the CUI exists
    logInfo("  Load atom notes");
    setLastModifiedBy("admin");
    setMolecularActionFlag(false);

    final List<String> lines = Files.readLines(
        new File(getInputPath(), "atomNotes.txt"), Charset.forName("UTF-8"));

    final Session session = manager.unwrap(Session.class);
    final org.hibernate.Query hQuery =
        session.createSQLQuery("select b.alternateTerminologyIds, a.id "
            + "from atoms a, AtomJpa_alternateTerminologyIds b "
            + "where b.AtomJpa_id = a.id "
            + "  and b.alternateTerminologyIds_KEY = :terminology ");
    hQuery.setParameter("terminology", getTerminology());
    hQuery.setReadOnly(true).setFetchSize(2000).setCacheable(false);
    final ScrollableResults results = hQuery.scroll(ScrollMode.FORWARD_ONLY);
    final Map<String, Long> map = new HashMap<>();
    while (results.next()) {
      final String aui = (String) results.get()[0];
      final Long id = ((BigInteger) results.get()[1]).longValue();
      map.put(aui, id);
    }
    results.close();

    int ct = 0;
    final FastDateFormat df = FastDateFormat.getInstance("yyyy-MM-dd");
    for (final String line : lines) {
      final String[] tokens = FieldedStringTokenizer.split(line, "|");
      final String aui = tokens[0];
      final String note = tokens[1];
      final String lastModifiedBy = tokens[2];
      final String dateStr = "20" + tokens[3];
      final Date date = df.parse(dateStr);

      final Atom atom = getAtom(map.get(aui));
      // Skip concepts
      if (atom != null) {
        logInfo("    add note = " + aui);
      } else {
        continue;
      }

      final AtomNoteJpa anote = new AtomNoteJpa();
      anote.setNote(note);
      anote.setAtom(atom);
      anote.setLastModifiedBy(lastModifiedBy);
      anote.setLastModified(date);
      anote.setTimestamp(date);
      // manage lastmodified/by
      setLastModifiedFlag(false);
      addNote(anote);
      setLastModifiedFlag(true);

      atom.getNotes().add(anote);
      updateAtom(atom);

      logAndCommit(++ct, RootService.logCt, RootService.commitCt);
    }
    commitClearBegin();
    logInfo("    count = " + ct);
    logInfo("    done.");

  }

  /**
   * Load integrity data.
   *
   * @throws Exception the exception
   */
  public void loadIntegrityData() throws Exception {
    // ==> icPair.txt <==
    //
    // ==> icSingle.txt <==
    // MGV_I|N|SOURCE|CBO2011_02
    // MGV_I|N|SOURCE|ISO3166-2_2012
    // MGV_I|N|SOURCE|ISO3166-2_2012
    // MGV_SCUI|N|SOURCE|NCI_2016_10E
    // MGV_SCUI|N|SOURCE|NCI_2016_11D
    // MGV_SCUI|N|SOURCE|NCI_2016_10E
    // n/a - this is already handled by GenerateNciMetaDataMojo

  }

  /**
   * Load src atom ids.
   *
   * @throws Exception the exception
   */
  public void loadSrcAtomIds() throws Exception {

    // ==> srcAtomIds.txt <==
    // 255554504|A4222345
    // 255554522|A11234054
    // 254716446|A3571305
    // 254716483|A3494831
    // 255554561|A11130085
    // 254716508|A4330739
    // 254716515|A3150639
    // 380470158|A3177443
    // 380933951|A14753603
    // 254716564|A14843410

    // For each line in the file, create a concept note if the CUI exists
    logInfo("  Load src atom ids");
    setLastModifiedBy("admin");
    setMolecularActionFlag(false);

    final List<String> lines = Files.readLines(
        new File(getInputPath(), "srcAtomIds.txt"), Charset.forName("UTF-8"));

    final Session session = manager.unwrap(Session.class);
    final org.hibernate.Query hQuery =
        session.createSQLQuery("select b.alternateTerminologyIds, a.id "
            + "from atoms a, AtomJpa_alternateTerminologyIds b "
            + "where b.AtomJpa_id = a.id "
            + "  and b.alternateTerminologyIds_KEY = :terminology ");
    hQuery.setParameter("terminology", getTerminology());
    hQuery.setReadOnly(true).setFetchSize(2000).setCacheable(false);
    final ScrollableResults results = hQuery.scroll(ScrollMode.FORWARD_ONLY);
    final Map<String, Long> map = new HashMap<>();
    while (results.next()) {
      final String aui = (String) results.get()[0];
      final Long id = ((BigInteger) results.get()[1]).longValue();
      map.put(aui, id);
    }
    results.close();
    manager.clear();

    int ct = 0;
    for (final String line : lines) {
      final String[] tokens = FieldedStringTokenizer.split(line, "|");
      final String src = tokens[0];
      final String aui = tokens[1];

      final Atom atom = getAtom(map.get(aui));
      // Skip concepts
      if (atom == null) {
        continue;
      }

      // Set the SRC atom id
      atom.getAlternateTerminologyIds().put(getTerminology() + "-SRC", src);
      updateAtom(atom);

      logAndCommit(++ct, RootService.logCt, RootService.commitCt);
    }
    commitClearBegin();
    logInfo("    count = " + ct);
    logInfo("    done.");

  }

  /**
   * Load xr relationships.
   *
   * @throws Exception the exception
   */
  public void loadXrRelationships() throws Exception {

    // ==> xrRelationships.txt <==
    // C0281567|C0520526|E-MNC
    // C4050020|C1439306|E-GSC
    // C2986918|C2828054|E-MNC
    // C0460137|C0011581|E-CFC
    // C1420712|C1334098|E-MNC
    // C0878195|C3853704|MTH_2015AB
    // C1704653|C1269647|E-CFC
    // C1948049|C1269647|E-CFC
    // C1413700|C2986600|E-MNC
    // C1423936|C1435016|E-GSC

    // For each concept in the file, find the CUI, expect it to be
    // unpublished, and set the name and save
    logInfo("  Load XR relationships");
    setLastModifiedBy("admin");
    setMolecularActionFlag(false);
    setLastModifiedFlag(false);

    final List<String> lines =
        Files.readLines(new File(getInputPath(), "xrRelationships.txt"),
            Charset.forName("UTF-8"));

    int ct = 0;
    for (final String line : lines) {
      final String[] tokens = FieldedStringTokenizer.split(line, "|");
      final String cui1 = tokens[0];
      final String cui2 = tokens[1];
      final String lastModifiedBy = tokens[2];
      final Concept concept =
          getConcept(cui1, getTerminology(), getVersion(), Branch.ROOT);
      // Skip concepts
      if (concept == null) {
        continue;
      }
      // Skip unpublishable concepts
      if (!concept.isPublishable()) {
        continue;
      }
      final Concept concept2 =
          getConcept(cui2, getTerminology(), getVersion(), Branch.ROOT);
      // Skip concepts
      if (concept2 == null) {
        continue;
      }
      // Skip unpublishable concepts
      if (!concept2.isPublishable()) {
        continue;
      }

      final ConceptRelationship xr = new ConceptRelationshipJpa();
      xr.setAdditionalRelationshipType("");
      xr.setAssertedDirection(false);
      xr.setFrom(concept);
      xr.setLastModifiedBy(lastModifiedBy);
      xr.setLastModified(new Date());
      xr.setTimestamp(new Date());
      xr.setObsolete(false);
      xr.setPublishable(false);
      xr.setPublished(false);
      xr.setRelationshipType("XR");
      xr.setStated(true);
      xr.setInferred(true);
      xr.setTerminology(getTerminology());
      xr.setTerminologyId("");
      xr.setTo(concept2);
      xr.setVersion(getVersion());
      xr.setGroup("");
      xr.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);
      logInfo("    add xr = " + xr);
      addRelationship(xr);
      logAndCommit(++ct, RootService.logCt, RootService.commitCt);
      
      // Also create the inverse relationship
      final ConceptRelationship xr2 = new ConceptRelationshipJpa();
      xr2.setAdditionalRelationshipType("");
      xr2.setAssertedDirection(false);
      xr2.setFrom(concept2);
      xr2.setLastModifiedBy(lastModifiedBy);
      xr2.setLastModified(new Date());
      xr2.setTimestamp(new Date());
      xr2.setObsolete(false);
      xr2.setPublishable(false);
      xr2.setPublished(false);
      xr2.setRelationshipType("XR");
      xr2.setStated(true);
      xr2.setInferred(true);
      xr2.setTerminology(getTerminology());
      xr2.setTerminologyId("");
      xr2.setTo(concept);
      xr2.setVersion(getVersion());
      xr2.setGroup("");
      xr2.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);
      logInfo("    add xr2 = " + xr2);
      addRelationship(xr2);
      logAndCommit(++ct, RootService.logCt, RootService.commitCt);      
    }
    commitClearBegin();
    logInfo("    count = " + ct);
    logInfo("    done.");
  }

  /**
   * Load rui da.
   *
   * @throws Exception the exception
   */
  public void loadRuiDa() throws Exception {

    // ==> ruiDaFlags.txt <==
    // R115537943|1|
    // R107246533|1|
    // R107246535|1|
    // R107214084|1|
    // R107168552|1|
    // R107274014|1|
    // R107240012|1|

    // For each RUI, find the relationship and append a ~DA:# terminology id
    logInfo("  Load RUI da flags");
    setLastModifiedBy("admin");
    setMolecularActionFlag(false);
    setLastModifiedFlag(false);

    final List<String> lines = Files.readLines(
        new File(getInputPath(), "ruiDaFlags.txt"), Charset.forName("UTF-8"));

    final SearchHandler handler = getSearchHandler(ConfigUtility.DEFAULT);
    int ct = 0;
    for (final String line : lines) {
      final String[] tokens = FieldedStringTokenizer.split(line, "|");
      final String rui = tokens[0];
      final String flag = "~DA:" + tokens[1];

      logAndCommit(++ct, RootService.logCt, RootService.commitCt);

      // Try atom relationship
      final List<AtomRelationshipJpa> aList =
          handler.getQueryResults(null, null, Branch.ROOT,
              "alternateTerminologyIds:\"NCIMTH=" + rui + "\"", null,
              AtomRelationshipJpa.class, null, new int[1], getEntityManager());
      if (aList.size() == 1) {
        final AtomRelationship rel = aList.get(0);
        // skip if re-running this
        if (rel.getTerminologyId().endsWith(flag)) {
          continue;
        }
        rel.setTerminologyId(rel.getTerminologyId() + flag);
        logInfo("  fixing A rel = " + rui + ", " + rel);
        updateRelationship(rel);
        // found, go to next case
        continue;
      } else if (aList.size() > 1) {
        logError("ERROR: unexpected number of A RUI matches = " + aList.size());
      }

      // Try atom relationship
      final List<ConceptRelationshipJpa> crList = handler.getQueryResults(null,
          null, Branch.ROOT, "alternateTerminologyIds:\"NCIMTH=" + rui + "\"",
          null, ConceptRelationshipJpa.class, null, new int[1],
          getEntityManager());
      if (crList.size() == 1) {
        final ConceptRelationship rel = crList.get(0);
        // skip if re-running this
        if (rel.getTerminologyId().endsWith(flag)) {
          continue;
        }
        rel.setTerminologyId(rel.getTerminologyId() + flag);
        logInfo("  fixing C rel = " + rui + ", " + rel);
        updateRelationship(rel);
        // found, go to next case
        continue;
      } else if (crList.size() > 1) {
        logError(
            "ERROR: unexpected number of C RUI matches = " + crList.size());
      }

      // Try code relationship
      final List<CodeRelationshipJpa> cdrList =
          handler.getQueryResults(null, null, Branch.ROOT,
              "alternateTerminologyIds:\"NCIMTH=" + rui + "\"", null,
              CodeRelationshipJpa.class, null, new int[1], getEntityManager());
      if (cdrList.size() == 1) {
        final CodeRelationship rel = cdrList.get(0);
        // skip if re-running this
        if (rel.getTerminologyId().endsWith(flag)) {
          continue;
        }
        rel.setTerminologyId(rel.getTerminologyId() + flag);
        logInfo("  fixing CODE rel = " + rui + ", " + rel);
        updateRelationship(rel);
        // found, go to next case
        continue;
      } else if (cdrList.size() > 1) {
        logError(
            "ERROR: unexpected number of CODE RUI matches = " + cdrList.size());
      }

      // Try descriptor relationship
      final List<DescriptorRelationshipJpa> drList =
          handler.getQueryResults(null, null, Branch.ROOT,
              "alternateTerminologyIds:\"NCIMTH=" + rui + "\"", null,
              DescriptorRelationshipJpa.class, null, new int[1],
              getEntityManager());
      if (drList.size() == 1) {
        final DescriptorRelationship rel = drList.get(0);
        // skip if re-running this
        if (rel.getTerminologyId().endsWith(flag)) {
          continue;
        }
        rel.setTerminologyId(rel.getTerminologyId() + flag);
        logInfo("  fixing D rel = " + rui + ", " + rel);
        updateRelationship(rel);
        // found, go to next case
        continue;
      } else if (drList.size() > 1) {
        logError(
            "ERROR: unexpected number of D RUI matches = " + drList.size());
      }

      logError("ERROR: unable to find matching relationship for = " + rui);

    }
    commitClearBegin();
    logInfo("    count = " + ct);
    logInfo("    done.");
  }

  /**
   * Load umlscui.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  public void loadUmlscui() throws Exception {

    // ==> umlscui.txt <==
    // R115537943|1|
    // R107246533|1|
    // R107246535|1|
    // R107214084|1|
    // R107168552|1|
    // R107274014|1|
    // R107240012|1|

    // For each RUI, find the relationship and append a ~DA:# terminology id
    logInfo("  Load UMLSCUI identifiers");
    setLastModifiedBy("admin");
    setMolecularActionFlag(false);
    setLastModifiedFlag(false);

    // Atom -> AUI map
    // Load alternateTerminologyIds
    logInfo("  Cache AUI -> AtomId map");
    final Map<String, Long> auiAtomMap = new HashMap<>();
    final Query query = getEntityManager().createQuery(
        "select value(b), a.id from AtomJpa a join a.alternateTerminologyIds b "
            + "where KEY(b) = :terminology and a.publishable=true");
    query.setParameter("terminology", getProject().getTerminology());
    final List<Object[]> results2 = query.getResultList();
    int ct = 0;
    for (final Object[] result : results2) {
      final String alternateTerminologyId = result[0].toString();
      final Long id = Long.valueOf(result[1].toString());
      auiAtomMap.put(alternateTerminologyId, id);
      logAndCommit(ct++, RootService.logCt, RootService.commitCt);
    }

    final String mth = "MTH" + getLatestVersion("MTH");
    logInfo("  Process umlscui.txt =  " + mth);
    final List<String> lines = Files.readLines(
        new File(getInputPath(), "umlscui.txt"), Charset.forName("UTF-8"));
    ct = 0;
    for (final String line : lines) {
      final String[] tokens = FieldedStringTokenizer.split(line, "|");
      final String aui = tokens[0];
      final String cui = tokens[1];

      final Long atomId = auiAtomMap.get(aui);
      // Skip non-existent atoms
      if (atomId == null) {
        continue;
      }
      final Atom atom = getAtom(auiAtomMap.get(aui));
      atom.getConceptTerminologyIds().put(mth, cui);
      updateAtom(atom);
      logAndCommit(++ct, RootService.logCt, RootService.commitCt);

    }
    commitClearBegin();
    logInfo("    count = " + ct);
    logInfo("    done.");
  }

  /* see superclass */
  @Override
  public String getFileVersion() throws Exception {
    // n/a
    return null;
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {

    // Check the input directory
    File inputDirFile = new File(getInputPath());
    if (!inputDirFile.exists()) {
      throw new Exception("Specified input directory does not exist");
    }

    File file = null;

    final String[] files = new String[] {
        "deletedCuiNames.txt", "conceptNotes.txt", "atomNotes.txt",
        /*"icSingle.txt", "icPair.txt", */
        "srcAtomIds.txt", "xrRelationships.txt", "ruiDaFlags.txt", "umlscui.txt"
    };
    for (final String f : files) {
      // Check file
      file = new File(getInputPath(), f);
      if (!file.exists()) {
        throw new Exception("Specified " + f + " does not exist");
      }
    }
    return new ValidationResultJpa();

  }

  /* see superclass */
  @Override
  public void reset() throws Exception {
    // n/a

  }

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    // n/a

  }

  /* see superclass */
  @Override
  public void checkProperties(Properties p) throws Exception {
    // n/a

  }

}
