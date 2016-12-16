/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.log4j.Logger;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;

import com.google.common.io.Files;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.FieldedStringTokenizer;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.content.AtomNoteJpa;
import com.wci.umls.server.jpa.content.ConceptNoteJpa;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.services.ContentService;
import com.wci.umls.server.services.RootService;

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

    logInfo("Done ...");

    // clear and commit
    commit();
    clear();

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
    logInfo("  Load deleted concept names");

    // TODO: make fault tolerant if data doesn't exist so it works for mini
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
    final ContentService service = new ContentServiceJpa();
    try {
      service.setLastModifiedBy("admin");
      service.setTransactionPerOperation(false);
      service.beginTransaction();
      service.setMolecularActionFlag(false);

      final List<String> lines =
          Files.readLines(new File(getInputPath(), "deletedCuiNames.txt"),
              Charset.forName("UTF-8"));

      int ct = 0;
      for (final String line : lines) {
        final String[] tokens = FieldedStringTokenizer.split(line, "|");
        final String cui = tokens[0];
        final String name = tokens[1];
        final Concept concept = service.getConcept(cui, getTerminology(),
            getVersion(), Branch.ROOT);
        // Skip concepts
        if (concept == null) {
          logInfo("    skip nonexistent = " + cui);
          continue;
        }
        // Skip publishable concepts
        if (concept.isPublishable()) {
          logInfo("    skip unpublishable = " + cui);
          continue;
        }

        concept.setName(name);
        service.updateConcept(concept);
        service.logAndCommit(++ct, RootService.logCt, RootService.commitCt);
      }
      commit();

    } catch (Exception e) {
      throw e;
    } finally {
      service.close();
    }
  }

  /**
   * Load concept notes.
   *
   * @throws Exception the exception
   */
  public void loadConceptNotes() throws Exception {
    // TODO: make fault tolerant if data doesn't exist so it works for mini
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

    setLastModifiedBy("admin");
    setTransactionPerOperation(false);
    beginTransaction();
    setMolecularActionFlag(false);

    final List<String> lines = Files.readLines(
        new File(getInputPath(), "conceptNotes.txt"), Charset.forName("UTF-8"));

    int ct = 0;
    final FastDateFormat df = FastDateFormat.getInstance("yyyy-MM-dd");
    for (final String line : lines) {
      final String[] tokens = FieldedStringTokenizer.split(line, "|");
      final String cui = tokens[0];
      final String note = tokens[1];
      final String lastModifiedBy = tokens[2];
      final String dateStr = "20" + tokens[3];
      final Date date = df.parse(dateStr);

      final Concept concept =
          getConcept(cui, getTerminology(), getVersion(), Branch.ROOT);
      // Skip concepts
      if (concept == null) {
        logInfo("    skip nonexistent = " + cui);
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
    commit();

  }

  /**
   * Load atom notes.
   *
   * @throws Exception the exception
   */
  public void loadAtomNotes() throws Exception {
    // TODO: make fault tolerant if data doesn't exist so it works for mini
    // A3250443|<>Long_Attribute<>:46350541|E-CFC|22-SEP-11
    // A1996431|<>Long_Attribute<>:63319088|E-GSC|18-DEC-13

    // For each line in the file, create a concept note if the CUI exists

    setLastModifiedBy("admin");
    setTransactionPerOperation(false);
    beginTransaction();
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
      final Long id = (Long) results.get()[1];
      map.put(aui, id);
    }

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
      if (atom == null) {
        logInfo("    skip nonexistent = " + aui);
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
    commit();

  }

  /**
   * Load integrity data.
   *
   * @throws Exception the exception
   */
  public void loadIntegrityData() throws Exception {
    // TODO: make fault tolerant if data doesn't exist so it works for mini
    // ==> icPair.txt <==
    //
    // ==> icSingle.txt <==
    // MGV_I|N|SOURCE|CBO2011_02
    // MGV_I|N|SOURCE|ISO3166-2_2012
    // MGV_I|N|SOURCE|ISO3166-2_2012
    // MGV_SCUI|N|SOURCE|NCI_2016_10E
    // MGV_SCUI|N|SOURCE|NCI_2016_11D
    // MGV_SCUI|N|SOURCE|NCI_2016_10E

  }

  /**
   * Load src atom ids.
   *
   * @throws Exception the exception
   */
  public void loadSrcAtomIds() throws Exception {
    // TODO: make fault tolerant if data doesn't exist so it works for mini
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
  }

  /**
   * Load xr relationships.
   *
   * @throws Exception the exception
   */
  public void loadXrRelationships() throws Exception {
    // TODO: make fault tolerant if data doesn't exist so it works for mini
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

    File file;

    final String[] files = new String[] {
        "deletedCuiNames.txt", "conceptNotes.txt", "atomNotes.txt",
        "icSingle.txt", "icPair.txt", "srcAtomIds.txt", "xrRelationships.txt"
    };
    for (final String f : files) {
      // Check file
      file = new File(getInputPath(), f);
      if (!inputDirFile.exists()) {
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
