/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.rel;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.FieldedStringTokenizer;
import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.helpers.meta.AdditionalRelationshipTypeList;
import com.wci.umls.server.helpers.meta.RelationshipTypeList;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.AbstractAlgorithm;
import com.wci.umls.server.jpa.content.AtomTreePositionJpa;
import com.wci.umls.server.jpa.services.helper.ReportsAtomComparator;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.AtomRelationship;
import com.wci.umls.server.model.content.AtomSubsetMember;
import com.wci.umls.server.model.content.AtomTreePosition;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.CodeRelationship;
import com.wci.umls.server.model.content.ComponentInfoRelationship;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.content.ConceptSubsetMember;
import com.wci.umls.server.model.content.Definition;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.DescriptorRelationship;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.model.content.SemanticTypeComponent;
import com.wci.umls.server.model.meta.AdditionalRelationshipType;
import com.wci.umls.server.model.meta.RelationshipType;
import com.wci.umls.server.model.meta.SemanticType;
import com.wci.umls.server.model.meta.Terminology;
import com.wci.umls.server.services.handlers.ComputePreferredNameHandler;
import com.wci.umls.server.services.handlers.SearchHandler;

// TODO: Auto-generated Javadoc
/**
 * Algorithm to write the RRF content files.
 */
public class WriteRrfContentFilesAlgorithm extends AbstractAlgorithm {

  /** The previous progress. */
  private int previousProgress;

  /** The steps. */
  private int steps;

  /** The steps completed. */
  private int stepsCompleted;

  /** The sem type map. */
  private Map<String, SemanticType> semTypeMap = new HashMap<>();

  /** The term map. */
  private Map<String, Terminology> termMap = new HashMap<>();

  /** The writer map. */
  private Map<String, PrintWriter> writerMap = new HashMap<>();

  /** The atom concept map. */
  private Map<Long, Long> atomConceptMap = new HashMap<>();

  /** The aui cui map. */
  private Map<String, String> auiCuiMap = new HashMap<>();

  /** The atom code map. */
  private Map<Long, Long> atomCodeMap = new HashMap<>();

  /** The atom descriptor map. */
  private Map<Long, Long> atomDescriptorMap = new HashMap<>();

  /** The concept aui map. */
  private Map<Long, String> conceptAuiMap = new HashMap<>();

  /** The code aui map. */
  private Map<Long, String> codeAuiMap = new HashMap<>();

  /** The descriptor aui map. */
  private Map<Long, String> descriptorAuiMap = new HashMap<>();

  /** The rui attribute terminologies. */
  private Set<String> ruiAttributeTerminologies = new HashSet<>();

  /** The rel to inverse map. */
  private Map<String, String> relToInverseMap = new HashMap<>();

  /** The terminology to src rht name map. */
  private Map<String, String> terminologyToSrcRhtNameMap = new HashMap<>();

  /** The terminology to src atom id map. */
  private Map<String, String> terminologyToSrcAtomIdMap = new HashMap<>();

  /** The handler. */
  private SearchHandler handler = null;

  /**
   * Instantiates an empty {@link WriteRrfContentFilesAlgorithm}.
   *
   * @throws Exception the exception
   */
  public WriteRrfContentFilesAlgorithm() throws Exception {
    super();
    setActivityId(UUID.randomUUID().toString());
    setWorkId("RRFCONTENT");
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {
    return new ValidationResultJpa();
  }

  /* see superclass */
  @Override
  public void compute() throws Exception {

    // open print writers
    openWriters();

    handler = this.getSearchHandler(ConfigUtility.DEFAULT);

    prepareMaps();

    // initialize progress monitoring
    javax.persistence.Query query =
        manager.createQuery("select count(*) from ConceptJpa c "
            + "where c.publishable = true and terminology = :terminology");
    query.setParameter("terminology", getProject().getTerminology());
    steps = Integer.parseInt(query.getSingleResult().toString());

    // get concepts and write components by concept to each file
    final Session session = manager.unwrap(Session.class);
    org.hibernate.Query hQuery = session.createQuery(
        "select a from ConceptJpa a WHERE a.publishable = true and terminology = :terminology order by a.terminologyId");

    hQuery.setParameter("terminology", getProject().getTerminology());
    hQuery.setReadOnly(true).setFetchSize(1000);
    ScrollableResults results = hQuery.scroll(ScrollMode.FORWARD_ONLY);
    while (results.next()) {
      final Concept c = (Concept) results.get()[0];

      writerMap.get("MRCONSO.RRF").print(writeMrconso(c));
      for (String line : writeMrdef(c)) {
        writerMap.get("MRDEF.RRF").print(line);
      }
      for (String line : writeMrsty(c)) {
        writerMap.get("MRSTY.RRF").print(line);
      }
      for (String line : writeMrrel(c)) {
        writerMap.get("MRREL.RRF").print(line);
      }
      for (String line : writeMrsat(c)) {
        writerMap.get("MRSAT.RRF").print(line);
      }
      for (String line : writeMrhier(c)) {
        writerMap.get("MRHIER.RRF").print(line);
      }
      writerMap.get("MRHIER.RRF").flush();

      // TODO later
      // MRMAP.RRF
      // MRSMAP.RRF
      //

      updateProgress();
    }

    // close print writers
    closeWriters();

    // TODO:
    // Write AMBIGSUI/LUI

  }

  /**
   * Prepare maps.
   *
   * @throws Exception the exception
   */
  private void prepareMaps() throws Exception {

    // First create map of rel and rela inverses
    RelationshipTypeList relTypeList = getRelationshipTypes(
        getProject().getTerminology(), getProject().getVersion());
    AdditionalRelationshipTypeList addRelTypeList =
        getAdditionalRelationshipTypes(getProject().getTerminology(),
            getProject().getVersion());
    relToInverseMap = new HashMap<>();
    for (RelationshipType relType : relTypeList.getObjects()) {
      relToInverseMap.put(relType.getAbbreviation(),
          relType.getInverse().getAbbreviation());
    }
    for (AdditionalRelationshipType relType : addRelTypeList.getObjects()) {
      relToInverseMap.put(relType.getAbbreviation(),
          relType.getInverse().getAbbreviation());
    }

    // make semantic types map
    for (SemanticType semType : getSemanticTypes(getProject().getTerminology(),
        getProject().getVersion()).getObjects()) {
      semTypeMap.put(semType.getExpandedForm(), semType);
    }

    // make terminologies map
    for (Terminology term : this.getCurrentTerminologies().getObjects()) {
      termMap.put(term.getTerminology(), term);
    }

    for (Terminology term : this.getTerminologyLatestVersions().getObjects()) {
      Atom srcRhtAtom = null;
      SearchResultList searchResults = findConceptSearchResults(
          getProject().getTerminology(), getProject().getVersion(),
          getProject().getBranch(), " atoms.codeId:V-" + term.getTerminology()
              + " AND atoms.terminology:SRC AND atoms.termType:RPT",
          null);
      if (searchResults.size() == 1) {
        Concept concept = getConcept(searchResults.getObjects().get(0).getId());
        for (Atom a : concept.getAtoms()) {
          if (a.getTermType().equals("RHT") && a.isPublishable()) {
            srcRhtAtom = a;
            break;
          }
        }
        if (srcRhtAtom != null) {
          String srcAtomId = srcRhtAtom.getAlternateTerminologyIds()
              .get(getProject().getTerminology());
          String name = srcRhtAtom.getName();
          terminologyToSrcRhtNameMap.put(term.getTerminology(), name);
          terminologyToSrcAtomIdMap.put(term.getTerminology(), srcAtomId);
        }
      } // TODO fails on HPO else {
      /*
       * throw new Exception( "missing root SRC concept " +
       * term.getTerminology()); }
       */
    }

    final ComputePreferredNameHandler handler =
        getComputePreferredNameHandler(getProject().getTerminology());
    final PrecedenceList list = getPrecedenceList(getProject().getTerminology(),
        getProject().getVersion());

    Session session = manager.unwrap(Session.class);
    org.hibernate.Query hQuery = session
        .createQuery("select c from ConceptJpa c where publishable = true");
    hQuery.setReadOnly(true).setFetchSize(2000).setCacheable(false);
    ScrollableResults results = hQuery.scroll(ScrollMode.FORWARD_ONLY);
    while (results.next()) {
      final Concept concept = (Concept) results.get()[0];

      // compute preferred atom of the concept
      final Atom atom = handler.sortAtoms(concept.getAtoms(), list).get(0);
      atomConceptMap.put(atom.getId(), concept.getId());
      if (concept.getTerminology().equals(getProject().getTerminology())) {
        auiCuiMap.put(atom.getAlternateTerminologyIds()
            .get(getProject().getTerminology()), concept.getTerminologyId());
      }
      conceptAuiMap.put(concept.getId(),
          atom.getAlternateTerminologyIds().get(getProject().getTerminology()));
    }

    session = manager.unwrap(Session.class);
    hQuery = session
        .createQuery("select c from DescriptorJpa c where publishable = true");
    hQuery.setReadOnly(true).setFetchSize(2000).setCacheable(false);
    results = hQuery.scroll(ScrollMode.FORWARD_ONLY);
    while (results.next()) {
      final Descriptor descriptor = (Descriptor) results.get()[0];

      // compute preferred atom of the descriptor
      final Atom atom = handler.sortAtoms(descriptor.getAtoms(), list).get(0);
      atomDescriptorMap.put(atom.getId(), descriptor.getId());
      if (descriptor.getTerminology().equals(getProject().getTerminology())) {
        auiCuiMap.put(atom.getAlternateTerminologyIds()
            .get(getProject().getTerminology()), descriptor.getTerminologyId());
      }
      descriptorAuiMap.put(descriptor.getId(),
          atom.getAlternateTerminologyIds().get(getProject().getTerminology()));
    }

    session = manager.unwrap(Session.class);
    hQuery =
        session.createQuery("select c from CodeJpa c where publishable = true");
    hQuery.setReadOnly(true).setFetchSize(2000).setCacheable(false);
    results = hQuery.scroll(ScrollMode.FORWARD_ONLY);
    while (results.next()) {
      final Code code = (Code) results.get()[0];

      // compute preferred atom of the code
      final Atom atom = handler.sortAtoms(code.getAtoms(), list).get(0);
      atomCodeMap.put(atom.getId(), code.getId());
      if (code.getTerminology().equals(getProject().getTerminology())) {
        auiCuiMap.put(atom.getAlternateTerminologyIds()
            .get(getProject().getTerminology()), code.getTerminologyId());
      }
      codeAuiMap.put(code.getId(),
          atom.getAlternateTerminologyIds().get(getProject().getTerminology()));
    }

    session = manager.unwrap(Session.class);
    hQuery = session.createQuery(
        "select distinct r.terminology from ConceptRelationshipJpa r join r.attributes a "
            + "where r.terminology != :terminology");
    hQuery.setParameter("terminology", getProject().getTerminology());
    hQuery.setReadOnly(true).setFetchSize(2000).setCacheable(false);
    results = hQuery.scroll(ScrollMode.FORWARD_ONLY);
    while (results.next()) {
      final String result = results.get()[0].toString();
      ruiAttributeTerminologies.add(result);
    }

    session = manager.unwrap(Session.class);
    hQuery = session.createQuery(
        "select distinct r.terminology from CodeRelationshipJpa r join r.attributes a "
            + "where r.terminology != :terminology");
    hQuery.setParameter("terminology", getProject().getTerminology());
    hQuery.setReadOnly(true).setFetchSize(2000).setCacheable(false);
    results = hQuery.scroll(ScrollMode.FORWARD_ONLY);
    while (results.next()) {
      final String result = results.get()[0].toString();
      ruiAttributeTerminologies.add(result);
    }

    session = manager.unwrap(Session.class);
    hQuery = session.createQuery(
        "select distinct r.terminology from DescriptorRelationshipJpa r join r.attributes a "
            + "where r.terminology != :terminology");
    hQuery.setParameter("terminology", getProject().getTerminology());
    hQuery.setReadOnly(true).setFetchSize(2000).setCacheable(false);
    results = hQuery.scroll(ScrollMode.FORWARD_ONLY);
    while (results.next()) {
      final String result = results.get()[0].toString();
      ruiAttributeTerminologies.add(result);
    }

    session = manager.unwrap(Session.class);
    hQuery = session.createQuery(
        "select distinct r.terminology from AtomRelationshipJpa r join r.attributes a "
            + "where r.terminology != :terminology");
    hQuery.setParameter("terminology", getProject().getTerminology());
    hQuery.setReadOnly(true).setFetchSize(2000).setCacheable(false);
    results = hQuery.scroll(ScrollMode.FORWARD_ONLY);
    while (results.next()) {
      final String result = results.get()[0].toString();
      ruiAttributeTerminologies.add(result);
    }

  }

  /**
   * Open writers.
   *
   * @throws Exception the exception
   */
  private void openWriters() throws Exception {
    final File dir = new File(config.getProperty("source.data.dir") + "/"
        + getProcess().getInputPath() + "/" + getProcess().getVersion() + "/"
        + "META");

    writerMap.put("MRCONSO.RRF",
        new PrintWriter(new FileWriter(new File(dir, "MRCONSO.RRF"))));
    writerMap.put("MRDEF.RRF",
        new PrintWriter(new FileWriter(new File(dir, "MRDEF.RRF"))));
    writerMap.put("MRREL.RRF",
        new PrintWriter(new FileWriter(new File(dir, "MRREL.RRF"))));
    writerMap.put("MRSTY.RRF",
        new PrintWriter(new FileWriter(new File(dir, "MRSTY.RRF"))));
    writerMap.put("MRSAT.RRF",
        new PrintWriter(new FileWriter(new File(dir, "MRSAT.RRF"))));
    writerMap.put("MRHIER.RRF",
        new PrintWriter(new FileWriter(new File(dir, "MRHIER.RRF"))));
    writerMap.put("MRHIST.RRF",
        new PrintWriter(new FileWriter(new File(dir, "MRHIST.RRF"))));
  }

  /**
   * Close writers.
   */
  private void closeWriters() {
    for (PrintWriter writer : writerMap.values()) {
      writer.close();
    }
  }

  /**
   * Write mrconso.
   *
   * @param c the c
   * @return the string
   * @throws Exception the exception
   */
  private String writeMrconso(Concept c) throws Exception {

    // Field Description
    // 0 CUI
    // 1 LAT
    // 2 TS
    // 3 LUI
    // 4 STT
    // 5 SUI
    // 6 ISPREF
    // 7 AUI
    // 8 SAUI
    // 9 SCUI
    // 10 SDUI
    // 11 SAB
    // 12 TTY
    // 13 CODE
    // 14 STR
    // 15 SRL
    // 16 SUPPRESS
    // 17 CVF
    //
    // e.g.
    // C0000005|ENG|P|L0000005|PF|S0007492|Y|A7755565||M0019694|D012711|MSH|PEN|D012711|(131)I-Macroaggregated
    // Albumin|0|N|256|

    // sort the atoms
    final List<Atom> sortedAtoms = new ArrayList<>(c.getAtoms());
    Collections.sort(sortedAtoms,
        new ReportsAtomComparator(c, getProject().getPrecedenceList()));

    StringBuilder sb = new StringBuilder();

    String prefLui = null;
    String prevLui = null;
    String prefSui = null;
    String prevSui = null;
    String prefAui = null;
    String prevLat = null;
    for (Atom a : sortedAtoms) {
      sb.append(c.getTerminologyId()).append("|"); // 0 CUI
      sb.append(a.getLanguage()).append("|"); // 1 LAT

      if (!a.getLanguage().equals(prevLat)) {
        prefLui = null;
        prefSui = null;
        prefAui = null;
      }

      String ts = "S";
      if (prefLui == null) {
        prefLui = a.getLexicalClassId();
        ts = "P";
      } else if (a.getLexicalClassId().equals(prefLui)) {
        ts = "P";
      } else if (!a.getLexicalClassId().equals(prevLui)) {
        prefSui = null;
      }

      String stt = "VO";
      if (prefSui == null) {
        prefSui = a.getStringClassId();
        stt = "PF";
      } else if (a.getStringClassId().equals(prefSui)) {
        stt = "PF";
      } else if (!a.getStringClassId().equals(prevSui)) {
        prefAui = null;
      }
      String ispref = "N";
      if (prefAui == null) {
        prefAui =
            a.getAlternateTerminologyIds().get(getProject().getTerminology());
        ispref = "Y";
      }

      prevLui = a.getLexicalClassId();
      prevSui = a.getStringClassId();
      prevLat = a.getLanguage();

      sb.append(ts).append("|"); // 2 TS
      sb.append(a.getLexicalClassId()).append("|"); // 3 LUI
      sb.append(stt).append("|"); // 4 STT
      sb.append(a.getStringClassId()).append("|"); // 5 SUI
      sb.append(ispref).append("|"); // 6 ISPREF
      String aui =
          a.getAlternateTerminologyIds().get(getProject().getTerminology());
      sb.append(aui != null ? aui : "").append("|"); // 7 AUI
      sb.append(a.getTerminologyId()).append("|"); // 8 SAUI
      sb.append(a.getConceptId()).append("|"); // 9 SCUI
      sb.append(a.getDescriptorId()).append("|"); // 10 SDUI
      sb.append(a.getTerminology()).append("|"); // 11 SAB
      sb.append(a.getTermType()).append("|"); // 12 TTY
      sb.append(a.getCodeId()).append("|"); // 13 CODE
      sb.append(a.getName()).append("|"); // 14 STR
      sb.append(termMap.get(a.getTerminology()).getRootTerminology()
          .getRestrictionLevel()).append("|"); // 15 SRL
      if (a.isObsolete()) {
        sb.append("O");
      } else if (a.isSuppressible()
          && getTermType(a.getTermType(), getProject().getTerminology(),
              getProject().getVersion()).isSuppressible()) {
        sb.append("Y");
      } else if (a.isSuppressible()
          && !getTermType(a.getTermType(), getProject().getTerminology(),
              getProject().getVersion()).isSuppressible()) {
        sb.append("E");
      } else {
        sb.append("N");
      }
      sb.append("|"); // 16 SUPPRESS
      sb.append("|"); // 17 CVF
      sb.append("\n");
    }
    return sb.toString();
  }

  /**
   * Write mrdef.
   *
   * @param c the c
   * @return the list
   * @throws Exception the exception
   */
  private List<String> writeMrdef(Concept c) throws Exception {

    // Field Description
    // 0 CUI
    // 1 AUI
    // 2 ATUI
    // 3 SATUI
    // 4 SAB
    // 5 DEF
    // 6 SUPPRESS
    // 7 CVF
    //
    // e.g.
    // C0001175|A0019180|AT38139119||MSH|An acquired defect of cellular
    // immunity associated with infection by the human immunodeficiency virus
    // (HIV), a CD4-positive T-lymphocyte count under 200 cells/microliter or
    // less than 14% of total lymphocytes, and increased susceptibility to
    // opportunistic infections and malignant neoplasms. Clinical
    // manifestations also include emaciation (wasting) and dementia. These
    // elements reflect criteria for AIDS as defined by the CDC in 1993.|N||
    // C0001175|A0021048|AT51221477||CSP|one or more indicator diseases,
    // depending on laboratory evidence of HIV infection (CDC); late phase of
    // HIV infection characterized by marked suppression of immune function
    // resulting in opportunistic infections, neoplasms, and other systemic
    // symptoms (NIAID).|N||

    List<String> lines = new ArrayList<>();
    for (Atom a : c.getAtoms()) {

      StringBuilder sb = new StringBuilder();
      for (Definition d : a.getDefinitions()) {
        sb.append(c.getTerminologyId()).append("|"); // 0 CUI
        String aui =
            a.getAlternateTerminologyIds().get(getProject().getTerminology());
        sb.append(aui != null ? aui : "").append("|"); // 1 AUI
        String atui =
            d.getAlternateTerminologyIds().get(getProject().getTerminology());
        sb.append(atui != null ? atui : "").append("|"); // 2 ATUI
        sb.append(d.getTerminologyId()).append("|"); // 3 SATUI
        sb.append(d.getTerminology()).append("|"); // 4 SAB
        sb.append(d.getValue()).append("|"); // 5 DEF
        if (d.isObsolete()) {
          sb.append("O");
        } else if (d.isSuppressible()) {
          sb.append("Y");
        } else {
          sb.append("N");
        }
        sb.append("|"); // 6 SUPPRESS
        sb.append("|"); // 7 CVF
        sb.append("\n");
      }
      lines.add(sb.toString());
    }
    Collections.sort(lines);
    return lines;
  }

  /**
   * Write mrsty.
   *
   * @param c the c
   * @return the list
   */
  private List<String> writeMrsty(Concept c) {

    // Field Description
    // 0 CUI Unique identifier of concept
    // 1 TUI Unique identifier of Semantic Type
    // 2 STN Semantic Type tree number
    // 3 STY Semantic Type. The valid values are defined in the Semantic
    // Network.
    // 4 ATUI Unique identifier for attribute
    // 5 CVF Content View Flag. Bit field used to flag rows included in
    // Content View. This field is a varchar field to maximize the number of
    // bits available for use.

    // Sample Record
    // C0001175|T047|B2.2.1.2.1|Disease or Syndrome|AT17683839|3840|
    List<String> lines = new ArrayList<>();

    for (SemanticTypeComponent sty : c.getSemanticTypes()) {
      StringBuilder sb = new StringBuilder();
      sb.append(c.getTerminologyId()).append("|"); // 0 CUI
      sb.append(semTypeMap.get(sty.getSemanticType()).getTypeId()).append("|"); // 1
                                                                                // TUI
      sb.append(semTypeMap.get(sty.getSemanticType()).getTreeNumber())
          .append("|"); // 2 STN
      sb.append(sty.getSemanticType()).append("|"); // 3 STY
      sb.append(sty.getTerminologyId()).append("|"); // 4 ATUI
      sb.append("|");// 5 CVF
      sb.append("\n");
      lines.add(sb.toString());
    }
    Collections.sort(lines);
    return lines;
  }

  /**
   * Write mrrel.
   *
   * @param c the c
   * @return the list
   * @throws Exception the exception
   */
  private List<String> writeMrrel(Concept c) throws Exception {

    // Field description
    // 0 CUI1
    // 1 AUI1
    // 2 STYPE1
    // 3 REL
    // 4 CUI2
    // 5 AUI2
    // 6 STYPE2
    // 7 RELA
    // 8 RUI
    // 9 SRUI
    // 10 SAB
    // 11 SL
    // 12 RG
    // 13 DIR
    // 14 SUPPRESS
    // 15 CVF
    //
    // e.g. C0002372|A0021548|AUI|SY|C0002372|A16796726|AUI||R112184262||
    // RXNORM|RXNORM|||N|| C0002372|A0022283|AUI|RO|C2241537|A14211642|AUI
    // |has_ingredient|R91984327||MMSL|MMSL|||N||

    List<String> lines = new ArrayList<>();

    for (final ConceptRelationship rel : c.getInverseRelationships()) {

      StringBuilder sb = new StringBuilder();
      sb.append(rel.getFrom().getTerminologyId()).append("|"); // 0 CUI1
      sb.append("").append("|"); // 1 AUI1
      sb.append("CUI").append("|"); // 2 STYPE1
      sb.append(rel.getRelationshipType()).append("|"); // 3 REL
      sb.append(rel.getTo().getTerminologyId()).append("|"); // 4 CUI2
      sb.append("").append("|"); // 5 AUI2
      sb.append("CUI").append("|"); // 6 STYPE2
      sb.append(rel.getAdditionalRelationshipType()).append("|"); // 7 RELA
      String rui =
          rel.getAlternateTerminologyIds().get(getProject().getTerminology());
      sb.append(rui != null ? rui : "").append("|");// 8 RUI
      sb.append(rel.getTerminologyId()).append("|"); // 9 SRUI
      sb.append(rel.getTerminology()).append("|"); // 10 SAB
      sb.append(rel.getTerminology()).append("|"); // 11 SL
      sb.append(rel.getGroup()).append("|"); // 12 RG
      boolean asserts =
          termMap.get(rel.getTerminology()).isAssertsRelDirection();
      sb.append(asserts ? (rel.isAssertedDirection() ? "Y" : "N") : "")
          .append("|"); // 13 DIR
      if (rel.isObsolete()) {
        sb.append("O");
      } else if (rel.isSuppressible()) {
        sb.append("Y");
      } else {
        sb.append("N");
      }
      sb.append("|"); // 14 SUPPRESS
      sb.append("|"); // 15 CVF
      sb.append("\n");
      lines.add(sb.toString());
    }

    for (Atom a : c.getAtoms()) {

      for (AtomRelationship r : a.getRelationships()) {
        StringBuilder sb = new StringBuilder();
        sb.append(c.getTerminologyId()).append("|"); // 0 CUI1
        sb.append(
            a.getAlternateTerminologyIds().get(getProject().getTerminology()))
            .append("|"); // 1 AUI1
        sb.append("AUI").append("|"); // 2 STYPE1
        sb.append(r.getRelationshipType()).append("|"); // 3 REL
        String aui2 = r.getTo().getAlternateTerminologyIds()
            .get(getProject().getTerminology());
        sb.append(auiCuiMap.get(aui2)).append("|"); // 4 CUI2
        sb.append(aui2).append("|"); // 5 AUI2
        sb.append("AUI").append("|"); // 6 STYPE2
        sb.append(r.getAdditionalRelationshipType()).append("|"); // 7 RELA
        String rui =
            r.getAlternateTerminologyIds().get(getProject().getTerminology());
        sb.append(rui != null ? rui : "").append("|");// 8 RUI
        sb.append(r.getTerminologyId()).append("|"); // 9 SRUI
        sb.append(r.getTerminology()).append("|"); // 10 SAB
        sb.append(r.getTerminology()).append("|"); // 11 SL
        sb.append(r.getGroup()).append("|"); // 12 RG
        boolean asserts =
            termMap.get(r.getTerminology()).isAssertsRelDirection();
        sb.append(asserts ? (r.isAssertedDirection() ? "Y" : "N") : "")
            .append("|"); // 13 DIR
        if (r.isObsolete()) {
          sb.append("O");
        } else if (r.isSuppressible()) {
          sb.append("Y");
        } else {
          sb.append("N");
        }
        sb.append("|"); // 14 SUPPRESS
        sb.append("|"); // 15 CVF
        sb.append("\n");
        lines.add(sb.toString());
      }

      if (atomConceptMap.containsKey(a.getId())) {
        final Concept scui = getConcept(atomConceptMap.get(a.getId()));
        for (final ConceptRelationship rel : scui.getRelationships()) {

          // � STYPE1=SCUI, STYPE2=SCUI
          // � AUI1 =
          // atom.getAlternateTerminologyIds().get(getProject().getTerminology());
          // � CUI1 = concept.getTerminologyId
          // � AUI2 = conceptAuiMap.get(scui.getId())
          // � CUI2 = auiCuiMap.get(AUI2);
          StringBuilder sb = new StringBuilder();
          sb.append(c.getTerminologyId()).append("|"); // 0 CUI1
          sb.append(
              a.getAlternateTerminologyIds().get(getProject().getTerminology()))
              .append("|"); // 1 AUI1
          sb.append("SCUI").append("|"); // 2 STYPE1
          sb.append(rel.getRelationshipType()).append("|"); // 3 REL
          String aui2 = conceptAuiMap.get(scui.getId());
          sb.append(auiCuiMap.get(aui2)).append("|"); // 4 CUI2
          sb.append(aui2).append("|"); // 5 AUI2
          sb.append("SCUI").append("|"); // 6 STYPE2
          sb.append(rel.getAdditionalRelationshipType()).append("|"); // 7 RELA
          String rui = rel.getAlternateTerminologyIds()
              .get(getProject().getTerminology());
          sb.append(rui != null ? rui : "").append("|");// 8 RUI
          sb.append(rel.getTerminologyId()).append("|"); // 9 SRUI
          sb.append(rel.getTerminology()).append("|"); // 10 SAB
          sb.append(rel.getTerminology()).append("|"); // 11 SL
          sb.append(rel.getGroup()).append("|"); // 12 RG
          boolean asserts =
              termMap.get(rel.getTerminology()).isAssertsRelDirection();
          sb.append(asserts ? (rel.isAssertedDirection() ? "Y" : "N") : "")
              .append("|"); // 13 DIR
          if (rel.isObsolete()) {
            sb.append("O");
          } else if (rel.isSuppressible()) {
            sb.append("Y");
          } else {
            sb.append("N");
          }
          sb.append("|"); // 14 SUPPRESS
          sb.append("|"); // 15 CVF
          sb.append("\n");
          lines.add(sb.toString());
        }

        // look up component info relationships where STYPE1=SCUI
        for (Relationship<?, ?> relationship : findComponentInfoRelationships(
            scui.getTerminologyId(), scui.getTerminology(), scui.getVersion(),
            scui.getType(), Branch.ROOT, null, true, null).getObjects()) {
          final ComponentInfoRelationship rel =
              (ComponentInfoRelationship) relationship;

          StringBuilder sb = new StringBuilder();
          sb.append(c.getTerminologyId()).append("|"); // 0 CUI1
          sb.append(
              a.getAlternateTerminologyIds().get(getProject().getTerminology()))
              .append("|"); // 1 AUI1
          sb.append(rel.getFrom().getType()).append("|"); // 2 STYPE1
          sb.append(relToInverseMap.get(rel.getRelationshipType())).append("|"); // 3
                                                                                 // REL
          // determine aui2
          String aui2 = "";
          if (rel.getTo().getType().equals("CONCEPT")) {
            aui2 = conceptAuiMap.get(scui.getId());
          } else if (rel.getTo().getType().equals("CODE")) {
            aui2 = codeAuiMap.get(scui.getId());
          } else if (rel.getTo().getType().equals("DESCRIPTOR")) {
            aui2 = descriptorAuiMap.get(scui.getId());
          }
          sb.append(auiCuiMap.get(aui2)).append("|"); // 4 CUI2
          sb.append(aui2).append("|"); // 5 AUI2
          sb.append(rel.getTo().getType()).append("|"); // 6 STYPE2
          sb.append(relToInverseMap.get(rel.getAdditionalRelationshipType()))
              .append("|"); // 7 RELA
          String rui = rel.getAlternateTerminologyIds()
              .get(getProject().getTerminology());
          sb.append(rui != null ? rui : "").append("|");// 8 RUI
          sb.append(rel.getTerminologyId()).append("|"); // 9 SRUI
          sb.append(rel.getTerminology()).append("|"); // 10 SAB
          sb.append(rel.getTerminology()).append("|"); // 11 SL
          sb.append(rel.getGroup()).append("|"); // 12 RG
          boolean asserts =
              termMap.get(rel.getTerminology()).isAssertsRelDirection();
          sb.append(asserts ? (rel.isAssertedDirection() ? "Y" : "N") : "")
              .append("|"); // 13 DIR
          if (rel.isObsolete()) {
            sb.append("O");
          } else if (rel.isSuppressible()) {
            sb.append("Y");
          } else {
            sb.append("N");
          }
          sb.append("|"); // 14 SUPPRESS
          sb.append("|"); // 15 CVF
          sb.append("\n");
        }
      }

      if (atomCodeMap.containsKey(a.getId())) {
        final Code code = getCode(atomCodeMap.get(a.getId()));
        for (final CodeRelationship rel : code.getRelationships()) {

          // � STYPE1=SCUI, STYPE2=SCUI
          // � AUI1 =
          // atom.getAlternateTerminologyIds().get(getProject().getTerminology());
          // � CUI1 = concept.getTerminologyId
          // � AUI2 = conceptAuiMap.get(scui.getId())
          // � CUI2 = auiCuiMap.get(AUI2);
          StringBuilder sb = new StringBuilder();
          sb.append(c.getTerminologyId()).append("|"); // 0 CUI1
          sb.append(
              a.getAlternateTerminologyIds().get(getProject().getTerminology()))
              .append("|"); // 1 AUI1
          sb.append("CODE").append("|"); // 2 STYPE1
          sb.append(rel.getRelationshipType()).append("|"); // 3 REL
          String aui2 = codeAuiMap.get(code.getId());
          sb.append(auiCuiMap.get(aui2)).append("|"); // 4 CUI2
          sb.append(aui2).append("|"); // 5 AUI2
          sb.append("CODE").append("|"); // 6 STYPE2
          sb.append(rel.getAdditionalRelationshipType()).append("|"); // 7 RELA
          String rui = rel.getAlternateTerminologyIds()
              .get(getProject().getTerminology());
          sb.append(rui != null ? rui : "").append("|");// 8 RUI
          sb.append(rel.getTerminologyId()).append("|"); // 9 SRUI
          sb.append(rel.getTerminology()).append("|"); // 10 SAB
          sb.append(rel.getTerminology()).append("|"); // 11 SL
          sb.append(rel.getGroup()).append("|"); // 12 RG
          boolean asserts =
              termMap.get(rel.getTerminology()).isAssertsRelDirection();
          sb.append(asserts ? (rel.isAssertedDirection() ? "Y" : "N") : "")
              .append("|"); // 13
          // DIR
          if (rel.isObsolete()) {
            sb.append("O");
          } else if (rel.isSuppressible()) {
            sb.append("Y");
          } else {
            sb.append("N");
          }
          sb.append("|"); // 14 SUPPRESS
          sb.append("|"); // 15 CVF
          sb.append("\n");
          lines.add(sb.toString());
        }

        // look up component info relationships where STYPE1=CODE
        for (Relationship<?, ?> relationship : findComponentInfoRelationships(
            code.getTerminologyId(), code.getTerminology(), code.getVersion(),
            code.getType(), Branch.ROOT, null, true, null).getObjects()) {
          final ComponentInfoRelationship rel =
              (ComponentInfoRelationship) relationship;

          StringBuilder sb = new StringBuilder();
          sb.append(c.getTerminologyId()).append("|"); // 0 CUI1
          sb.append(
              a.getAlternateTerminologyIds().get(getProject().getTerminology()))
              .append("|"); // 1 AUI1
          sb.append(rel.getFrom().getType()).append("|"); // 2 STYPE1
          sb.append(relToInverseMap.get(rel.getRelationshipType())).append("|"); // 3
                                                                                 // REL
          // determine aui2
          String aui2 = "";
          if (rel.getTo().getType().equals("CONCEPT")) {
            aui2 = conceptAuiMap.get(code.getId());
          } else if (rel.getTo().getType().equals("CODE")) {
            aui2 = codeAuiMap.get(code.getId());
          } else if (rel.getTo().getType().equals("DESCRIPTOR")) {
            aui2 = descriptorAuiMap.get(code.getId());
          }
          sb.append(auiCuiMap.get(aui2)).append("|"); // 4 CUI2
          sb.append(aui2).append("|"); // 5 AUI2
          sb.append(rel.getTo().getType()).append("|"); // 6 STYPE2
          sb.append(relToInverseMap.get(rel.getAdditionalRelationshipType()))
              .append("|"); // 7 RELA
          String rui = rel.getAlternateTerminologyIds()
              .get(getProject().getTerminology());
          sb.append(rui != null ? rui : "").append("|");// 8 RUI
          sb.append(rel.getTerminologyId()).append("|"); // 9 SRUI
          sb.append(rel.getTerminology()).append("|"); // 10 SAB
          sb.append(rel.getTerminology()).append("|"); // 11 SL
          sb.append(rel.getGroup()).append("|"); // 12 RG
          boolean asserts =
              termMap.get(rel.getTerminology()).isAssertsRelDirection();
          sb.append(asserts ? (rel.isAssertedDirection() ? "Y" : "N") : "")
              .append("|"); // 13 DIR
          if (rel.isObsolete()) {
            sb.append("O");
          } else if (rel.isSuppressible()) {
            sb.append("Y");
          } else {
            sb.append("N");
          }
          sb.append("|"); // 14 SUPPRESS
          sb.append("|"); // 15 CVF
          sb.append("\n");
        }
      }

      if (atomDescriptorMap.containsKey(a.getId())) {
        final Descriptor descriptor =
            getDescriptor(atomDescriptorMap.get(a.getId()));
        for (final DescriptorRelationship rel : descriptor.getRelationships()) {

          // � STYPE1=SCUI, STYPE2=SCUI
          // � AUI1 =
          // atom.getAlternateTerminologyIds().get(getProject().getTerminology());
          // � CUI1 = concept.getTerminologyId
          // � AUI2 = conceptAuiMap.get(scui.getId())
          // � CUI2 = auiCuiMap.get(AUI2);
          StringBuilder sb = new StringBuilder();
          sb.append(c.getTerminologyId()).append("|"); // 0 CUI1
          sb.append(
              a.getAlternateTerminologyIds().get(getProject().getTerminology()))
              .append("|"); // 1 AUI1
          sb.append("CODE").append("|"); // 2 STYPE1
          sb.append(rel.getRelationshipType()).append("|"); // 3 REL
          String aui2 = descriptorAuiMap.get(descriptor.getId());
          sb.append(auiCuiMap.get(aui2)).append("|"); // 4 CUI2
          sb.append(aui2).append("|"); // 5 AUI2
          sb.append("CODE").append("|"); // 6 STYPE2
          sb.append(rel.getAdditionalRelationshipType()).append("|"); // 7 RELA
          String rui = rel.getAlternateTerminologyIds()
              .get(getProject().getTerminology());
          sb.append(rui != null ? rui : "").append("|");// 8 RUI
          sb.append(rel.getTerminologyId()).append("|"); // 9 SRUI
          sb.append(rel.getTerminology()).append("|"); // 10 SAB
          sb.append(rel.getTerminology()).append("|"); // 11 SL
          sb.append(rel.getGroup()).append("|"); // 12 RG
          boolean asserts =
              termMap.get(rel.getTerminology()).isAssertsRelDirection();
          sb.append(asserts ? (rel.isAssertedDirection() ? "Y" : "N") : "")
              .append("|"); // 13
          // DIR
          if (rel.isObsolete()) {
            sb.append("O");
          } else if (rel.isSuppressible()) {
            sb.append("Y");
          } else {
            sb.append("N");
          }
          sb.append("|"); // 14 SUPPRESS
          sb.append("|"); // 15 CVF
          sb.append("\n");
          lines.add(sb.toString());
        }

        // look up component info relationships where STYPE1=SDUI
        for (Relationship<?, ?> relationship : findComponentInfoRelationships(
            descriptor.getTerminologyId(), descriptor.getTerminology(),
            descriptor.getVersion(), descriptor.getType(), Branch.ROOT, null,
            true, null).getObjects()) {
          final ComponentInfoRelationship rel =
              (ComponentInfoRelationship) relationship;

          StringBuilder sb = new StringBuilder();
          sb.append(c.getTerminologyId()).append("|"); // 0 CUI1
          sb.append(
              a.getAlternateTerminologyIds().get(getProject().getTerminology()))
              .append("|"); // 1 AUI1
          sb.append(rel.getFrom().getType()).append("|"); // 2 STYPE1
          sb.append(relToInverseMap.get(rel.getRelationshipType())).append("|"); // 3
                                                                                 // REL
          // determine aui2
          String aui2 = "";
          if (rel.getTo().getType().equals("CONCEPT")) {
            aui2 = conceptAuiMap.get(descriptor.getId());
          } else if (rel.getTo().getType().equals("CODE")) {
            aui2 = descriptorAuiMap.get(descriptor.getId());
          } else if (rel.getTo().getType().equals("DESCRIPTOR")) {
            aui2 = descriptorAuiMap.get(descriptor.getId());
          }
          sb.append(auiCuiMap.get(aui2)).append("|"); // 4 CUI2
          sb.append(aui2).append("|"); // 5 AUI2
          sb.append(rel.getTo().getType()).append("|"); // 6 STYPE2
          sb.append(relToInverseMap.get(rel.getAdditionalRelationshipType()))
              .append("|"); // 7 RELA
          String rui = rel.getAlternateTerminologyIds()
              .get(getProject().getTerminology());
          sb.append(rui != null ? rui : "").append("|");// 8 RUI
          sb.append(rel.getTerminologyId()).append("|"); // 9 SRUI
          sb.append(rel.getTerminology()).append("|"); // 10 SAB
          sb.append(rel.getTerminology()).append("|"); // 11 SL
          sb.append(rel.getGroup()).append("|"); // 12 RG
          boolean asserts =
              termMap.get(rel.getTerminology()).isAssertsRelDirection();
          sb.append(asserts ? (rel.isAssertedDirection() ? "Y" : "N") : "")
              .append("|"); // 13 DIR
          if (rel.isObsolete()) {
            sb.append("O");
          } else if (rel.isSuppressible()) {
            sb.append("Y");
          } else {
            sb.append("N");
          }
          sb.append("|"); // 14 SUPPRESS
          sb.append("|"); // 15 CVF
          sb.append("\n");
        }
      }
    } // end for(Atom... concept.getAtoms())

    // TODO: deal with PAR/CHD relationships to/from SRC atoms and top-level
    // things
    // in hierarchies (these don�t get assigned RUIs, and currently there�s an
    // issue of STYPE changing, etc)

    Collections.sort(lines);
    return lines;
  }

  /**
   * Write mrhier.
   *
   * @param c the c
   * @return the list
   * @throws Exception the exception
   */
  private List<String> writeMrhier(Concept c) throws Exception {

    // Field description
    // 0 CUI
    // 1 AUI
    // 2 CXN
    // 3 PAUI
    // 4 SAB
    // 5 RELA
    // 6 PTR
    // 7 HCD
    // 8 CVF
    //
    // e.g. C0001175|A2878223|1|A3316611|SNOMEDCT|isa|
    // A3684559.A3886745.A2880798.A3512117.A3082701.A3316611|||

    final List<String> lines = new ArrayList<>();

    for (final Atom atom : c.getAtoms()) {

      int ct = 1;
      for (final AtomTreePosition treepos : handler.getQueryResults(null, null,
          Branch.ROOT, "nodeId:" + atom.getId(), null,
          AtomTreePositionJpa.class, null, new int[1], manager)) {

        String aui = atom.getAlternateTerminologyIds()
            .get(getProject().getTerminology());
        String cxn = new Integer(ct++).toString();
        String sab = treepos.getTerminology();
        String rela = treepos.getAdditionalRelationshipType();
        String hcd = treepos.getTerminologyId();
        StringBuilder ptr = new StringBuilder();
        String paui = null;
        String root = null;
        for (final String atomId : FieldedStringTokenizer
            .split(treepos.getAncestorPath(), "~")) {
          final Atom atom2 = getAtom(new Long(atomId));
          if (atom2 == null) {
            throw new Exception("atom from ptr is null");
          }
          if (paui != null) {
            ptr.append(".");
          }
          paui = atom2.getAlternateTerminologyIds()
              .get(getProject().getTerminology());
          ptr.append(paui);
          if (root == null) {
            root = atom2.getName();
          }
        }

        StringBuilder sb = new StringBuilder();
        sb.append(c.getTerminologyId()).append("|"); // 0 CUI
        sb.append(aui != null ? aui : "").append("|"); // 1 AUI
        sb.append(cxn != null ? cxn : "").append("|"); // 2 CXN
        sb.append(paui != null ? paui : "").append("|"); // 3 PAUI
        sb.append(sab != null ? sab : "").append("|"); // 4 SAB
        sb.append(rela != null ? rela : "").append("|"); // 5 RELA
        String srcRhtName = terminologyToSrcRhtNameMap.get(sab);
        if (root == null) { // TODO
          System.out.println("root is null " + sb.toString() + " " + ptr);
          sb.append("root is null ");
        } else if (!root.equals(srcRhtName)) {
          sb.append(terminologyToSrcAtomIdMap.get(sab) + ".");
        }
        sb.append(ptr.toString()).append("|"); // 6 PTR
        sb.append(hcd != null ? hcd : "").append("|"); // 7 HCD
        sb.append("|"); // 8 CVF

        sb.append("\n");
        lines.add(sb.toString());
      }

    }

    Collections.sort(lines);
    return lines;
  }

  /**
   * Write mrsat.
   *
   * @param c the c
   * @return the list
   * @throws Exception the exception
   */
  private List<String> writeMrsat(Concept c) throws Exception {

    // Field Description
    // 0 CUI
    // 1 LUI
    // 2 SUI
    // 3 METAUI
    // 4 STYPE
    // 5 CODE
    // 6 ATUI
    // 7 SATUI
    // 8 ATN
    // 9 SAB
    // 10 ATV
    // 11 SUPPRESS
    // 12 CVF
    //
    // e.g.
    // C0001175|L0001175|S0010339|A0019180|SDUI|D000163|AT38209082||FX|MSH|D015492|N||
    // C0001175|L0001175|S0354232|A2922342|AUI|62479008|AT24600515||DESCRIPTIONSTATUS|SNOMEDCT|0|N||
    // C0001175|L0001842|S0011877|A15662389|CODE|T1|AT100434486||URL|MEDLINEPLUS|http://www.nlm.nih.gov/medlineplus/aids.html|N||
    // C0001175|||R54775538|RUI||AT63713072||CHARACTERISTICTYPE|SNOMEDCT|0|N||
    // C0001175|||R54775538|RUI||AT69142126||REFINABILITY|SNOMEDCT|1|N||

    List<String> lines = new ArrayList<>();

    // Concept attributes (CUIs)
    // Note: MR/ST/DA attributes are not written out for NCIMETA
    for (final Attribute att : c.getAttributes()) {
      StringBuilder sb = new StringBuilder();
      sb.append(c.getTerminologyId()).append("|"); // 0 CUI1
      sb.append("|"); // 1 LUI
      sb.append("|"); // 2 SUI
      sb.append("|"); // 3 METAUI
      sb.append("CUI").append("|"); // 4 STYPE
      sb.append("|"); // 5 CODE
      String atui =
          att.getAlternateTerminologyIds().get(getProject().getTerminology());
      sb.append(atui != null ? atui : "").append("|"); // 6 ATUI
      sb.append(att.getTerminologyId()).append("|"); // 7 SATUI
      sb.append(att.getName()).append("|"); // 8 ATN
      sb.append(att.getTerminology()).append("|"); // 9 SAB
      sb.append(att.getValue()).append("|"); // 10 ATV
      if (att.isObsolete()) {
        sb.append("O");
      } else if (att.isSuppressible()) {
        sb.append("Y");
      } else {
        sb.append("N");
      }
      sb.append("|"); // 11 SUPPRESS
      sb.append("|"); // 12 CVF
      sb.append("\n");
      lines.add(sb.toString());
    }

    for (final Atom a : c.getAtoms()) {

      // Atom attributes (AUIs)
      for (Attribute att : a.getAttributes()) {
        StringBuilder sb = new StringBuilder();
        sb.append(c.getTerminologyId()).append("|"); // 0 CUI1
        sb.append(a.getLexicalClassId()).append("|"); // 1 LUI
        sb.append(a.getStringClassId()).append("|"); // 2 SUI
        sb.append(
            a.getAlternateTerminologyIds().get(getProject().getTerminology()))
            .append("|"); // 3 METAUI
        sb.append("AUI").append("|"); // 4 STYPE
        sb.append(a.getCodeId()).append("|"); // 5 CODE
        String atui =
            att.getAlternateTerminologyIds().get(getProject().getTerminology());
        sb.append(atui != null ? atui : "").append("|"); // 6 ATUI
        sb.append(att.getTerminologyId()).append("|"); // 7 SATUI
        sb.append(att.getName()).append("|"); // 8 ATN
        sb.append(att.getTerminology()).append("|"); // 9 SAB
        sb.append(att.getValue()).append("|"); // 10 ATV
        if (att.isObsolete()) {
          sb.append("O");
        } else if (att.isSuppressible()) {
          sb.append("Y");
        } else {
          sb.append("N");
        }
        sb.append("|"); // 11 SUPPRESS
        sb.append("|"); // 15 CVF
        sb.append("\n");
        lines.add(sb.toString());
      }

      // Atom relationship attributes (RUIs)
      if (ruiAttributeTerminologies.contains(a.getTerminology())) {
        for (AtomRelationship rel : a.getRelationships()) {
          for (Attribute attribute : rel.getAttributes()) {
            StringBuilder sb = new StringBuilder();
            sb.append(c.getTerminologyId()).append("|"); // 0 CUI1
            sb.append("|"); // 1 LUI
            sb.append("|"); // 2 SUI
            sb.append(a.getAlternateTerminologyIds()
                .get(getProject().getTerminology())).append("|"); // 3 METAUI
            sb.append("RUI").append("|"); // 4 STYPE
            sb.append("|"); // 5 CODE
            String atui = attribute.getAlternateTerminologyIds()
                .get(getProject().getTerminology());
            sb.append(atui != null ? atui : "").append("|"); // 6 ATUI
            sb.append(attribute.getTerminologyId()).append("|"); // 7 SATUI
            sb.append(attribute.getName()).append("|"); // 8 ATN
            sb.append(attribute.getTerminology()).append("|"); // 9 SAB
            sb.append(attribute.getValue()).append("|"); // 10 ATV
            if (attribute.isObsolete()) {
              sb.append("O");
            } else if (attribute.isSuppressible()) {
              sb.append("Y");
            } else {
              sb.append("N");
            }
            sb.append("|"); // 11 SUPPRESS
            sb.append("|"); // 15 CVF
            sb.append("\n");
            lines.add(sb.toString());
          }
        }
      }

      // TODO subset members - is this sufficient?
      // C0000052|L3853359|S4536829|A23245828|AUI|58488005|AT166631006|
      // cf28ec3d-cf07-59cb-944a-10ef4f43b725|SUBSET_MEMBER|SCTSPA|
      // 450828004~ACCEPTABILITYID~900000000000549004|N||

      for (AtomSubsetMember member : a.getMembers()) {
        for (Attribute att : member.getAttributes()) {
          StringBuilder sb = new StringBuilder();
          sb.append(c.getTerminologyId()).append("|"); // 0 CUI
          sb.append(a.getLexicalClassId()).append("|"); // 1 LUI
          sb.append(a.getStringClassId()).append("|"); // 2 SUI
          sb.append(
              a.getAlternateTerminologyIds().get(getProject().getTerminology()))
              .append("|"); // 3 METAUI
          sb.append("AUI").append("|"); // 4 STYPE
          sb.append(a.getCodeId()).append("|"); // 5 CODE
          sb.append(att.getAlternateTerminologyIds()
              .get(getProject().getTerminology())).append("|"); // 6 ATUI
          sb.append(member.getTerminologyId()).append("|"); // 7 SATUI
          sb.append("SUBSET_MEMBER").append("|"); // 8 ATN
          sb.append(att.getTerminology()).append("|"); // 9 SAB
          sb.append(member.getSubset().getTerminologyId()); // 10 ATV
          if (!ConfigUtility.isNull(att.getName())) {
            sb.append("~").append(att.getName());
            sb.append("~").append(att.getValue());
          }
          sb.append("|");
          if (att.isObsolete()) {
            sb.append("O");
          } else if (att.isSuppressible()) {
            sb.append("Y");
          } else {
            sb.append("N");
          }
          sb.append("|"); // 11 SUPPRESS
          sb.append("|"); // 12 CVF
          sb.append("\n");
          lines.add(sb.toString());
        }

      }

      // Source concept attributes (SCUIs)
      if (atomConceptMap.containsKey(a.getId())) {
        final Concept scui = getConcept(atomConceptMap.get(a.getId()));
        for (final Attribute attribute : scui.getAttributes()) {
          StringBuilder sb = new StringBuilder();
          sb.append(c.getTerminologyId()).append("|"); // 0 CUI
          sb.append(a.getLexicalClassId()).append("|"); // 1 LUI
          sb.append(a.getStringClassId()).append("|"); // 2 SUI
          sb.append(
              a.getAlternateTerminologyIds().get(getProject().getTerminology()))
              .append("|"); // 3 METAUI
          sb.append("SCUI").append("|"); // 4 STYPE
          sb.append(a.getCodeId()).append("|"); // 5 CODE
          String atui = attribute.getAlternateTerminologyIds()
              .get(getProject().getTerminology());
          sb.append(atui != null ? atui : "").append("|"); // 6 ATUI
          sb.append(attribute.getTerminologyId()).append("|"); // 7 SATUI
          sb.append(attribute.getName()).append("|"); // 8 ATN
          sb.append(attribute.getTerminology()).append("|"); // 9 SAB
          sb.append(attribute.getValue()).append("|"); // 10 ATV
          if (attribute.isObsolete()) {
            sb.append("O");
          } else if (attribute.isSuppressible()) {
            sb.append("Y");
          } else {
            sb.append("N");
          }
          sb.append("|"); // 11 SUPPRESS
          sb.append("|"); // 15 CVF
          sb.append("\n");
          lines.add(sb.toString());
        }

        // Concept relationship attributes (RUIs)
        if (ruiAttributeTerminologies.contains(c.getTerminology())) {
          for (ConceptRelationship rel : c.getRelationships()) {
            for (Attribute attribute : rel.getAttributes()) {
              StringBuilder sb = new StringBuilder();
              sb.append(c.getTerminologyId()).append("|"); // 0 CUI
              sb.append("|"); // 1 LUI
              sb.append("|"); // 2 SUI
              sb.append(a.getAlternateTerminologyIds()
                  .get(getProject().getTerminology())).append("|"); // 3 METAUI
              sb.append("RUI").append("|"); // 4 STYPE
              sb.append("|"); // 5 CODE
              String atui = attribute.getAlternateTerminologyIds()
                  .get(getProject().getTerminology());
              sb.append(atui != null ? atui : "").append("|"); // 6 ATUI
              sb.append(attribute.getTerminologyId()).append("|"); // 7 SATUI
              sb.append(attribute.getName()).append("|"); // 8 ATN
              sb.append(attribute.getTerminology()).append("|"); // 9 SAB
              sb.append(attribute.getValue()).append("|"); // 10 ATV
              if (attribute.isObsolete()) {
                sb.append("O");
              } else if (attribute.isSuppressible()) {
                sb.append("Y");
              } else {
                sb.append("N");
              }
              sb.append("|"); // 11 SUPPRESS
              sb.append("|"); // 15 CVF
              sb.append("\n");
              lines.add(sb.toString());
            }
          }
        }
        // is this correct for SCUI subset members? also for code and sdui?
        for (ConceptSubsetMember member : scui.getMembers()) {
          for (Attribute att : member.getAttributes()) {
            StringBuilder sb = new StringBuilder();
            sb.append(c.getTerminologyId()).append("|"); // 0 CUI
            sb.append(a.getLexicalClassId()).append("|"); // 1 LUI
            sb.append(a.getStringClassId()).append("|"); // 2 SUI
            sb.append(a.getAlternateTerminologyIds()
                .get(getProject().getTerminology())).append("|"); // 3 METAUI
            sb.append("SCUI").append("|"); // 4 STYPE
            sb.append(a.getCodeId()).append("|"); // 5 CODE
            sb.append(att.getAlternateTerminologyIds()
                .get(getProject().getTerminology())).append("|"); // 6 ATUI
            sb.append(member.getTerminologyId()).append("|"); // 7 SATUI
            sb.append("SUBSET_MEMBER").append("|"); // 8 ATN
            sb.append(att.getTerminology()).append("|"); // 9 SAB
            sb.append(member.getSubset().getTerminologyId()); // 10 ATV
            if (!ConfigUtility.isNull(att.getName())) {
              sb.append("~").append(att.getName());
              sb.append("~").append(att.getValue());
            }
            sb.append("|");
            if (att.isObsolete()) {
              sb.append("O");
            } else if (att.isSuppressible()) {
              sb.append("Y");
            } else {
              sb.append("N");
            }
            sb.append("|"); // 11 SUPPRESS
            sb.append("|"); // 12 CVF
            sb.append("\n");
            lines.add(sb.toString());
          }

        }
      }

      // Code attributes
      if (atomCodeMap.containsKey(a.getId())) {
        final Code code = getCode(atomCodeMap.get(a.getId()));
        for (final Attribute attribute : code.getAttributes()) {
          StringBuilder sb = new StringBuilder();
          sb.append(c.getTerminologyId()).append("|"); // 0 CUI
          sb.append("|"); // 1 LUI
          sb.append("|"); // 2 SUI
          sb.append(
              a.getAlternateTerminologyIds().get(getProject().getTerminology()))
              .append("|"); // 3 METAUI
          sb.append("CODE").append("|"); // 4 STYPE
          sb.append("|"); // 5 CODE
          String atui = attribute.getAlternateTerminologyIds()
              .get(getProject().getTerminology());
          sb.append(atui != null ? atui : "").append("|"); // 6 ATUI
          sb.append(attribute.getTerminologyId()).append("|"); // 7 SATUI
          sb.append(attribute.getName()).append("|"); // 8 ATN
          sb.append(attribute.getTerminology()).append("|"); // 9 SAB
          sb.append(attribute.getValue()).append("|"); // 10 ATV
          if (attribute.isObsolete()) {
            sb.append("O");
          } else if (attribute.isSuppressible()) {
            sb.append("Y");
          } else {
            sb.append("N");
          }
          sb.append("|"); // 11 SUPPRESS
          sb.append("|"); // 15 CVF
          sb.append("\n");
          lines.add(sb.toString());
        }

        // Code relationship attributes (RUIs)
        if (ruiAttributeTerminologies.contains(c.getTerminology())) {
          for (CodeRelationship rel : code.getRelationships()) {
            for (Attribute attribute : rel.getAttributes()) {
              StringBuilder sb = new StringBuilder();
              sb.append(c.getTerminologyId()).append("|"); // 0 CUI
              sb.append("|"); // 1 LUI
              sb.append("|"); // 2 SUI
              sb.append(a.getAlternateTerminologyIds()
                  .get(getProject().getTerminology())).append("|"); // 3 METAUI
              sb.append("RUI").append("|"); // 4 STYPE
              sb.append("|"); // 5 CODE
              String atui = attribute.getAlternateTerminologyIds()
                  .get(getProject().getTerminology());
              sb.append(atui != null ? atui : "").append("|"); // 6 ATUI
              sb.append(attribute.getTerminologyId()).append("|"); // 7 SATUI
              sb.append(attribute.getName()).append("|"); // 8 ATN
              sb.append(attribute.getTerminology()).append("|"); // 9 SAB
              sb.append(attribute.getValue()).append("|"); // 10 ATV
              if (attribute.isObsolete()) {
                sb.append("O");
              } else if (attribute.isSuppressible()) {
                sb.append("Y");
              } else {
                sb.append("N");
              }
              sb.append("|"); // 11 SUPPRESS
              sb.append("|"); // 15 CVF
              sb.append("\n");
              lines.add(sb.toString());
            }
          }
        }
      }
      // repeat for descriptor, STYPE=SDUI
      if (atomDescriptorMap.containsKey(a.getId())) {
        final Descriptor descriptor =
            getDescriptor(atomDescriptorMap.get(a.getId()));
        for (final Attribute attribute : descriptor.getAttributes()) {
          StringBuilder sb = new StringBuilder();
          sb.append(c.getTerminologyId()).append("|"); // 0 CUI
          sb.append(a.getLexicalClassId()).append("|"); // 1 LUI
          sb.append(a.getStringClassId()).append("|"); // 2 SUI
          sb.append(
              a.getAlternateTerminologyIds().get(getProject().getTerminology()))
              .append("|"); // 3 METAUI
          sb.append("SDUI").append("|"); // 4 STYPE
          sb.append(a.getCodeId()).append("|"); // 5 CODE
          String atui = attribute.getAlternateTerminologyIds()
              .get(getProject().getTerminology());
          sb.append(atui != null ? atui : "").append("|"); // 6 ATUI
          sb.append(attribute.getTerminologyId()).append("|"); // 7 SATUI
          sb.append(attribute.getName()).append("|"); // 8 ATN
          sb.append(attribute.getTerminology()).append("|"); // 9 SAB
          sb.append(attribute.getValue()).append("|"); // 10 ATV
          if (attribute.isObsolete()) {
            sb.append("O");
          } else if (attribute.isSuppressible()) {
            sb.append("Y");
          } else {
            sb.append("N");
          }
          sb.append("|"); // 11 SUPPRESS
          sb.append("|"); // 15 CVF
          sb.append("\n");
          lines.add(sb.toString());
        }

        // Descriptor relationship attributes (RUIs)
        if (ruiAttributeTerminologies.contains(c.getTerminology())) {
          for (DescriptorRelationship rel : descriptor.getRelationships()) {
            for (Attribute attribute : rel.getAttributes()) {
              StringBuilder sb = new StringBuilder();
              sb.append(c.getTerminologyId()).append("|"); // 0 CUI
              sb.append("|"); // 1 LUI
              sb.append("|"); // 2 SUI
              sb.append(a.getAlternateTerminologyIds()
                  .get(getProject().getTerminology())).append("|"); // 3 METAUI
              sb.append("RUI").append("|"); // 4 STYPE
              sb.append("|"); // 5 CODE
              String atui = attribute.getAlternateTerminologyIds()
                  .get(getProject().getTerminology());
              sb.append(atui != null ? atui : "").append("|"); // 6 ATUI
              sb.append(attribute.getTerminologyId()).append("|"); // 7 SATUI
              sb.append(attribute.getName()).append("|"); // 8 ATN
              sb.append(attribute.getTerminology()).append("|"); // 9 SAB
              sb.append(attribute.getValue()).append("|"); // 10 ATV
              if (attribute.isObsolete()) {
                sb.append("O");
              } else if (attribute.isSuppressible()) {
                sb.append("Y");
              } else {
                sb.append("N");
              }
              sb.append("|"); // 11 SUPPRESS
              sb.append("|"); // 15 CVF
              sb.append("\n");
              lines.add(sb.toString());
            }
          }
        }
      }

      // TODO need to do something to write out SUBSET_MEMBER attributes

    } // end for (c.getAtoms)
    Collections.sort(lines);
    return lines;
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
    checkRequiredProperties(new String[] {
        ""
    }, p);
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
          "RRF METADATA progress: " + currentProgress + "%");
      previousProgress = currentProgress;
    }
  }

  /* see superclass */
  @Override
  public String getDescription() {
    return ConfigUtility.getNameFromClass(getClass());
  }
}
