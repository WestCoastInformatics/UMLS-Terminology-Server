/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.release;

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

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.FieldedStringTokenizer;
import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.helpers.QueryType;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.helpers.meta.AdditionalRelationshipTypeList;
import com.wci.umls.server.helpers.meta.RelationshipTypeList;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.AbstractAlgorithm;
import com.wci.umls.server.jpa.content.AtomTreePositionJpa;
import com.wci.umls.server.jpa.content.CodeJpa;
import com.wci.umls.server.jpa.content.CodeTreePositionJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.ConceptTreePositionJpa;
import com.wci.umls.server.jpa.content.DescriptorJpa;
import com.wci.umls.server.jpa.content.DescriptorTreePositionJpa;
import com.wci.umls.server.jpa.services.helper.ReportsAtomComparator;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.AtomRelationship;
import com.wci.umls.server.model.content.AtomSubsetMember;
import com.wci.umls.server.model.content.AtomTreePosition;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.CodeRelationship;
import com.wci.umls.server.model.content.CodeTreePosition;
import com.wci.umls.server.model.content.ComponentInfoRelationship;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.content.ConceptSubsetMember;
import com.wci.umls.server.model.content.ConceptTreePosition;
import com.wci.umls.server.model.content.Definition;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.DescriptorRelationship;
import com.wci.umls.server.model.content.DescriptorTreePosition;
import com.wci.umls.server.model.content.MapSet;
import com.wci.umls.server.model.content.Mapping;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.model.content.SemanticTypeComponent;
import com.wci.umls.server.model.meta.AdditionalRelationshipType;
import com.wci.umls.server.model.meta.RelationshipType;
import com.wci.umls.server.model.meta.SemanticType;
import com.wci.umls.server.model.meta.Terminology;
import com.wci.umls.server.services.RootService;
import com.wci.umls.server.services.handlers.ComputePreferredNameHandler;
import com.wci.umls.server.services.handlers.SearchHandler;

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

  /**
   * Compute.
   *
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public void compute() throws Exception {
    logInfo("Starting write RRF content files");
    fireProgressEvent(0, "Starting");

    // open print writers
    openWriters();

    handler = this.getSearchHandler(ConfigUtility.DEFAULT);

    prepareMaps();

    // Collect all concepts
    final Map<String, String> params = new HashMap<>();
    params.put("terminology", getProject().getTerminology());
    params.put("version", getProject().getVersion());
    // Normalization is only for English
    final List<Long> conceptIds = executeSingleComponentIdQuery(
        "select distinct c.id from ConceptJpa c join c.atoms a "
            + "where c.terminology = :terminology "
            + "  and c.version = :version and a.publishable = true "
            + "  and c.publishable = true order by c.terminologyId",
        QueryType.JQL, params, ConceptJpa.class);
    commitClearBegin();
    steps = conceptIds.size();

    for (final Long conceptId : conceptIds) {
      final Concept c = getConcept(conceptId);

      for (final String line : writeMrconso(c)) {
        writerMap.get("MRCONSO.RRF").print(line);
      }

      for (final String line : writeMrdef(c)) {
        writerMap.get("MRDEF.RRF").print(line);
      }
      for (final String line : writeMrsty(c)) {
        writerMap.get("MRSTY.RRF").print(line);
      }
      for (final String line : writeMrrel(c)) {
        writerMap.get("MRREL.RRF").print(line);
      }
      for (final String line : writeMrsat(c)) {
        writerMap.get("MRSAT.RRF").print(line);
      }
      for (final String line : writeMrhier(c)) {
        writerMap.get("MRHIER.RRF").print(line);
      }
      writerMap.get("MRHIER.RRF").flush();

      updateProgress();
    }

    // close print writers
    closeWriters();

    // TODO:
    // Write AMBIGSUI/LUI

    fireProgressEvent(100, "Finished");
    logInfo("Finished write RRF content files");

  }

  /**
   * Prepare maps.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  private void prepareMaps() throws Exception {

    // First create map of rel and rela inverses
    final RelationshipTypeList relTypeList = getRelationshipTypes(
        getProject().getTerminology(), getProject().getVersion());
    final AdditionalRelationshipTypeList addRelTypeList =
        getAdditionalRelationshipTypes(getProject().getTerminology(),
            getProject().getVersion());
    relToInverseMap = new HashMap<>();
    for (final RelationshipType relType : relTypeList.getObjects()) {
      relToInverseMap.put(relType.getAbbreviation(),
          relType.getInverse().getAbbreviation());
    }
    for (final AdditionalRelationshipType relType : addRelTypeList
        .getObjects()) {
      relToInverseMap.put(relType.getAbbreviation(),
          relType.getInverse().getAbbreviation());
    }

    // make semantic types map
    for (final SemanticType semType : getSemanticTypes(
        getProject().getTerminology(), getProject().getVersion())
            .getObjects()) {
      semTypeMap.put(semType.getExpandedForm(), semType);
    }

    // make terminologies map
    for (final Terminology term : this.getCurrentTerminologies().getObjects()) {
      termMap.put(term.getTerminology(), term);
    }

    for (final Terminology term : this.getTerminologyLatestVersions()
        .getObjects()) {
      Atom srcRhtAtom = null;
      SearchResultList searchResults = findConceptSearchResults(
          getProject().getTerminology(), getProject().getVersion(),
          getProject().getBranch(), " atoms.codeId:V-" + term.getTerminology()
              + " AND atoms.terminology:SRC AND atoms.termType:RPT",
          null);
      if (searchResults.size() == 1) {
        Concept concept = getConcept(searchResults.getObjects().get(0).getId());
        for (final Atom a : concept.getAtoms()) {
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
      } else {
        this.logWarn("missing root SRC concept " + term.getTerminology());
      }
    }

    final ComputePreferredNameHandler handler =
        getComputePreferredNameHandler(getProject().getTerminology());
    final PrecedenceList list = getPrecedenceList(getProject().getTerminology(),
        getProject().getVersion());

    // Determine preferred atoms for all concepts
    final Map<String, String> params = new HashMap<>();
    params.put("terminology", getProject().getTerminology());
    params.put("version", getProject().getVersion());
    final List<Long> conceptIds = executeSingleComponentIdQuery(
        "select c.id from ConceptJpa c where publishable = true", QueryType.JQL,
        params, ConceptJpa.class);
    commitClearBegin();
    int ct = 0;
    for (Long conceptId : conceptIds) {
      final Concept concept = getConcept(conceptId);
      // compute preferred atom of the concept
      final Atom atom = handler.sortAtoms(concept.getAtoms(), list).get(0);
      // Save AUI->CUI map for the project terminology
      if (concept.getTerminology().equals(getProject().getTerminology())) {
        auiCuiMap.put(atom.getAlternateTerminologyIds()
            .get(getProject().getTerminology()), concept.getTerminologyId());
      }
      // otherwise save fact that atom is preferred id of its concept.
      else {
        atomConceptMap.put(atom.getId(), concept.getId());
      }
      conceptAuiMap.put(concept.getId(),
          atom.getAlternateTerminologyIds().get(getProject().getTerminology()));
      logAndCommit(ct++, RootService.logCt, RootService.commitCt);
    }

    // Determine preferred atoms for all descriptors
    final List<Long> descriptorIds = executeSingleComponentIdQuery(
        "select d.id from DescriptorJpa d where publishable = true",
        QueryType.JQL, params, DescriptorJpa.class);
    commitClearBegin();
    ct = 0;
    for (Long descriptorId : descriptorIds) {
      final Descriptor descriptor = getDescriptor(descriptorId);

      // compute preferred atom of the descriptor
      final Atom atom = handler.sortAtoms(descriptor.getAtoms(), list).get(0);
      atomDescriptorMap.put(atom.getId(), descriptor.getId());
      descriptorAuiMap.put(descriptor.getId(),
          atom.getAlternateTerminologyIds().get(getProject().getTerminology()));
      logAndCommit(ct++, RootService.logCt, RootService.commitCt);
    }

    // Determine preferred atoms for all codes
    final List<Long> codeIds = executeSingleComponentIdQuery(
        "select c.id from CodeJpa c where publishable = true", QueryType.JQL,
        params, CodeJpa.class);
    commitClearBegin();
    ct = 0;
    for (Long codeId : codeIds) {
      final Code code = getCode(codeId);
      // compute preferred atom of the code
      final Atom atom = handler.sortAtoms(code.getAtoms(), list).get(0);
      atomCodeMap.put(atom.getId(), code.getId());
      codeAuiMap.put(code.getId(),
          atom.getAlternateTerminologyIds().get(getProject().getTerminology()));
      logAndCommit(ct++, RootService.logCt, RootService.commitCt);
    }

    // Determine terminologies that have relationship attributes
    javax.persistence.Query query =
        manager.createQuery("select distinct r.terminology "
            + "from ConceptRelationshipJpa r join r.attributes a "
            + "where r.terminology != :terminology");
    query.setParameter("terminology", getProject().getTerminology());
    List<String> results = query.getResultList();
    for (final String result : results) {
      ruiAttributeTerminologies.add(result);
    }

    // TBD: because only concept relationships have RUI attributes so far
    // query = manager.createQuery("select distinct r.terminology "
    // + "from CodeRelationshipJpa r join r.attributes a "
    // + "where r.terminology != :terminology");
    // query.setParameter("terminology", getProject().getTerminology());
    // results = query.getResultList();
    // for (final String result : results) {
    // ruiAttributeTerminologies.add(result);
    // }
    //
    // query = manager.createQuery("select distinct r.terminology "
    // + "from CodeRelationshipJpa r join r.attributes a "
    // + "where r.terminology != :terminology");
    // query.setParameter("terminology", getProject().getTerminology());
    // results = query.getResultList();
    // for (final String result : results) {
    // ruiAttributeTerminologies.add(result);
    // }
    //
    // query = manager.createQuery("select distinct r.terminology "
    // + "from CodeRelationshipJpa r join r.attributes a "
    // + "where r.terminology != :terminology");
    // query.setParameter("terminology", getProject().getTerminology());
    // results = query.getResultList();
    // for (final String result : results) {
    // ruiAttributeTerminologies.add(result);
    // }

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
    writerMap.put("MRMAP.RRF",
        new PrintWriter(new FileWriter(new File(dir, "MRMAP.RRF"))));
    writerMap.put("MRSMAP.RRF",
        new PrintWriter(new FileWriter(new File(dir, "MRSMAP.RRF"))));
  }

  /**
   * Close writers.
   */
  private void closeWriters() {
    for (final PrintWriter writer : writerMap.values()) {
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
  private List<String> writeMrconso(Concept c) throws Exception {

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
        new ReportsAtomComparator(c, getPrecedenceList(
            getProject().getTerminology(), getProject().getVersion())));

    String prefLui = null;
    String prevLui = null;
    String prefSui = null;
    String prevSui = null;
    String prefAui = null;
    String prevLat = null;
    final List<String> lines = new ArrayList<>();
    for (final Atom a : sortedAtoms) {
      final StringBuilder sb = new StringBuilder();
      // CUI
      sb.append(c.getTerminologyId()).append("|");
      // LAT
      sb.append(a.getLanguage()).append("|");

      // Compute rank
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

      // TS
      sb.append(ts).append("|");
      // LUI
      sb.append(a.getLexicalClassId()).append("|");
      // STT
      sb.append(stt).append("|");
      // SUI
      sb.append(a.getStringClassId()).append("|");
      // ISPREF
      sb.append(ispref).append("|");
      final String aui =
          a.getAlternateTerminologyIds().get(getProject().getTerminology());
      // AUI
      sb.append(aui != null ? aui : "").append("|");
      // SAUI
      sb.append(a.getTerminologyId()).append("|");
      // SCUI
      sb.append(a.getConceptId()).append("|");
      // SDUI
      sb.append(a.getDescriptorId()).append("|");
      // SAB
      sb.append(a.getTerminology()).append("|");
      // TTY
      sb.append(a.getTermType()).append("|");
      // CODE
      sb.append(a.getCodeId()).append("|");
      // STR
      sb.append(a.getName()).append("|");
      // SRL
      sb.append(termMap.get(a.getTerminology()).getRootTerminology()
          .getRestrictionLevel()).append("|");
      // SUPPRESS
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
      sb.append("|");
      // CVF
      sb.append("|\n");
      lines.add(sb.toString());

      // Collect the mapset concepts and cache
      if (a.getTermType().equals("XM")) {
        MapSet mapSet = this.getMapSet(a.getCodeId(), a.getTerminology(),
            a.getVersion(), Branch.ROOT);

        for (final String line : writeMrmap(mapSet, c.getTerminologyId())) {
          writerMap.get("MRMAP.RRF").print(line);
        }
        for (final String line : writeMrsmap(mapSet, c.getTerminologyId())) {
          writerMap.get("MRSMAP.RRF").print(line);
        }
      }
    }
    Collections.sort(lines);
    return lines;
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
    // C0001175|A0019180|AT38139119||MSH|An acquired...|N||
    final List<String> lines = new ArrayList<>();
    for (final Atom a : c.getAtoms()) {
      for (final Definition d : a.getDefinitions()) {
        final StringBuilder sb = new StringBuilder();
        // CUI
        sb.append(c.getTerminologyId()).append("|");
        // AUI
        final String aui =
            a.getAlternateTerminologyIds().get(getProject().getTerminology());
        sb.append(aui).append("|");
        // ATUI
        String atui =
            d.getAlternateTerminologyIds().get(getProject().getTerminology());
        sb.append(atui).append("|");
        // SATUI
        sb.append(d.getTerminologyId()).append("|");
        // SAB
        sb.append(d.getTerminology()).append("|");
        // DEF
        sb.append(d.getValue()).append("|");
        // SUPPRESS
        if (d.isObsolete()) {
          sb.append("O");
        } else if (d.isSuppressible()) {
          sb.append("Y");
        } else {
          sb.append("N");
        }
        sb.append("|");
        // CVF
        sb.append("|");
        sb.append("\n");
        lines.add(sb.toString());
      }
    }
    Collections.sort(lines);
    return lines;
  }

  /**
   * Write mrsty.
   *
   * @param mapset the mapset
   * @param terminologyId the terminology id
   * @return the list
   */
  private List<String> writeMrmap(MapSet mapset, String terminologyId) {

    // Field Description
    // 0 MAPSETCUI Unique identifier for the UMLS concept which represents the
    // whole map set.
    // 1 MAPSETSAB Source abbreviation (SAB) for the provider of the map set.
    // 2 MAPSUBSETID Map subset identifier used to identify a subset of related
    // mappings within a map set. This is used for cases where the FROMEXPR may
    // have more than one potential mapping (optional).
    // 3 MAPRANK Order in which mappings in a subset should be applied. Used
    // only where MAPSUBSETID is used. (optional)
    // 4 MAPID Unique identifier for this individual mapping. Primary key of
    // this table to identify a particular row.
    // 5 MAPSID Source asserted identifier for this mapping (optional).
    // 6 FROMID Identifier for the entity being mapped from. This is an internal
    // UMLS identifier used to point to an external entity in a source
    // vocabulary (represented by the FROMEXPR). When the source provides such
    // an identifier, it is reused here. Otherwise, it is generated by NLM. The
    // FROMID is only unique within a map set. It is not a pointer to UMLS
    // entities like atoms or concepts. There is a one-to-one correlation
    // between FROMID and a unique set of values in FROMSID, FROMEXPR, FROMTYPE,
    // FROMRULE, and FROMRES within a map set.
    // 7 FROMSID Source asserted identifier for the entity being mapped from
    // (optional).
    // 8 FROMEXPR Entity being mapped from - can be a single code/identifier
    // /concept name or a complex expression involving multiple
    // codes/identifiers/concept names, Boolean operators and/or punctuation
    // 9 FROMTYPE Type of entity being mapped from.
    // 10 FROMRULE Machine processable rule applicable to the entity being
    // mapped from (optional)
    // 11 FROMRES Restriction applicable to the entity being mapped from
    // (optional).
    // 12 REL Relationship of the entity being mapped from to the entity being
    // mapped to.
    // 13 RELA Additional relationship label (optional).
    // 14 TOID Identifier for the entity being mapped to. This is an internal
    // identifier used to point to an external entity in a source vocabulary
    // (represented by the TOEXPR). When the source provides such an identifier,
    // it is reused here. Otherwise, it is generated by NLM. The TOID is only
    // unique within a map set. It is not a pointer to UMLS entities like atoms
    // or concepts. There is a one-to-one correlation between TOID and a unique
    // set of values in TOSID, TOEXPR, TOTYPE, TORULE, TORES within a map set.
    // 15 TOSID Source asserted identifier for the entity being mapped to
    // (optional).
    // 16 TOEXPR Entity being mapped to - can be a single
    // code/identifier/concept name or a complex expression involving multiple
    // codes/identifiers/concept names, Boolean operators and/or punctuation.
    // 17 TOTYPE Type of entity being mapped to.
    // 18 TORULE Machine processable rule applicable to the entity being mapped
    // to (optional).
    // 19 TORES Restriction applicable to the entity being mapped to (optional).
    // 20 MAPRULE Machine processable rule applicable to this mapping
    // (optional).
    // 21 MAPRES Restriction applicable to this mapping (optional).
    // 22 MAPTYPE Type of mapping (optional).
    // 23 MAPATN The name of the attribute associated with this mapping [not yet
    // in use]
    // 24 MAPATV The value of the attribute associated with this mapping [not
    // yet in use]
    // 25 CVF The Content View Flag is a bit field used to indicate membership
    // in a content view.

    // Sample Records
    // C1306694|MTH|||AT28307527||C0011764||C0011764|CUI|||RO||2201||<Developmental
    // Disabilities> AND <Writing>|BOOLEAN_EXPRESSION_STR|||||ATX||||
    // C1306694|MTH|||AT52620421||C0010700||C0010700|CUI|||RN||1552||<Urinary
    // Bladder>/<surgery>|BOOLEAN_EXPRESSION_STR|||||ATX||||
    // C2919943|SNOMEDCT|0|0|AT127959271||302759005||302759005|SCUI|||RN|mapped_to|9571037057|9571037057|799.59|BOOLEAN_EXPRESSION_SDUI|||||2||||
    // C2919943|SNOMEDCT|0|0|AT127959272||43498006||43498006|SCUI|||RQ|mapped_to|9571050056|9571050056|276.69|BOOLEAN_EXPRESSION_SDUI|||||1||||

    final List<String> lines = new ArrayList<>();

    for (final Mapping mapping : mapset.getMappings()) {
      final StringBuilder sb = new StringBuilder();
      // CUI
      sb.append(terminologyId).append("|");
      // MAPSETSAB
      sb.append(mapset.getTerminology()).append("|");
      // MAPSUBSETID
      sb.append(mapping.getGroup()).append("|");
      // MAPRANK
      sb.append(mapping.getRank()).append("|");
      // MAPID
      if (mapping.getAlternateTerminologyIds()
          .containsKey(getProject().getTerminology())) {
        sb.append(mapping.getAlternateTerminologyIds()
            .get(getProject().getTerminology()));
      }
      sb.append("|");
      // MAPSID
      sb.append(mapping.getTerminologyId()).append("|");
      // FROMID
      if (mapping.getAlternateTerminologyIds()
          .containsKey(getProject().getTerminology() + "-FROMID")) {
        sb.append(mapping.getAlternateTerminologyIds()
            .get(getProject().getTerminology() + "-FROMID"));
      }
      sb.append("|");
      // FROMSID
      if (mapping.getAlternateTerminologyIds()
          .containsKey(getProject().getTerminology() + "-FROMSID")) {
        sb.append(mapping.getAlternateTerminologyIds()
            .get(getProject().getTerminology() + "-FROMSID"));
      }
      sb.append("|");
      // FROMEXPR
      sb.append(mapping.getFromTerminologyId()).append("|");
      // FROMTYPE
      if (mapping.getFromIdType().toString().equals("DESCRIPTOR")) {
        sb.append("SDUI");
      } else if (mapping.getFromIdType().toString().equals("CONCEPT") && mapset
          .getFromTerminology().equals(getProject().getTerminology())) {
        sb.append("CUI");
      } else if (mapping.getFromIdType().toString().equals("CONCEPT")) {
        sb.append("SCUI");
      } else {
        mapping.getFromIdType().toString();
      }
      sb.append("|");
      // FROMRULE
      for (Attribute att : mapping.getAttributes()) {
        if (att.getName().equals("FROMRULE")) {
          sb.append(att.getValue());
        }
      }
      sb.append("|");
      // FROMRES
      for (Attribute att : mapping.getAttributes()) {
        if (att.getName().equals("FROMRES")) {
          sb.append(att.getValue());
        }
      }
      sb.append("|");
      // REL
      sb.append(mapping.getRelationshipType()).append("|");
      // RELA
      sb.append(mapping.getAdditionalRelationshipType()).append("|");
      // TOID
      if (mapping.getAlternateTerminologyIds()
          .containsKey(getProject().getTerminology() + "-TOID")) {
        sb.append(mapping.getAlternateTerminologyIds()
            .get(getProject().getTerminology() + "-TOID"));
      }
      sb.append("|");
      // TOSID
      if (mapping.getAlternateTerminologyIds()
          .containsKey(getProject().getTerminology() + "-TOSID")) {
        sb.append(mapping.getAlternateTerminologyIds()
            .get(getProject().getTerminology() + "-TOSID"));
      }
      sb.append("|");
      // TOEXPR
      sb.append(mapping.getToTerminologyId()).append("|");
      // TOTYPE
      if (mapping.getToIdType().toString().equals("DESCRIPTOR")) {
        sb.append("SDUI");
      } else if (mapping.getToIdType().toString().equals("CONCEPT")
          && mapset.getToTerminology().equals(getProject().getTerminology())) {
        sb.append("CUI");
      } else if (mapping.getToIdType().toString().equals("CONCEPT")) {
        sb.append("SCUI");
      } else {
        mapping.getToIdType().toString();
      }
      sb.append("|");
      // TORULE
      for (Attribute att : mapping.getAttributes()) {
        if (att.getName().equals("TORULE")) {
          sb.append(att.getValue());
        }
      }
      sb.append("|");
      // TORES
      for (Attribute att : mapping.getAttributes()) {
        if (att.getName().equals("TORES")) {
          sb.append(att.getValue());
        }
      }
      sb.append("|");
      // MAPRULE
      sb.append(mapping.getRule()).append("|");
      // MAPRES
      sb.append(mapping.getAdvice()).append("|");
      // MAPTYPE
      sb.append(mapset.getMapType() != null ? mapset.getMapType() : "")
          .append("|");
      // MAPATN && MAPATV
      if (mapping.getTerminology().equals("SNOMEDCT_US")) {
        sb.append("ACTIVE").append("|");
        sb.append(mapping.isObsolete() ? "0" : "1").append("|");
      } else {
        sb.append("||");
      }
      // CVF
      sb.append("|");
      sb.append("\n");
      lines.add(sb.toString());
    }
    Collections.sort(lines);
    return lines;
  }

  /**
   * Write mrsty.
   *
   * @param mapset the mapset
   * @param terminologyId the terminology id
   * @return the list
   */
  private List<String> writeMrsmap(MapSet mapset, String terminologyId) {

    // Field Description
    // MAPSETCUI Unique identifier for the UMLS concept which represents the
    // whole map set.
    // MAPSETSAB Source abbreviation for the map set.
    // MAPID Unique identifier for this individual mapping. Primary key of this
    // table to identify a particular row.
    // MAPSID Source asserted identifier for this mapping (optional).
    // FROMEXPR Entity being mapped from - can be a single
    // code/identifier/concept name or a complex expression involving multiple
    // codes/identifiers/concept names, Boolean operators and/or punctuation.
    // FROMTYPE Type of entity being mapped from.
    // REL Relationship of the entity being mapped from to the entity being
    // mapped to.
    // RELA Additional relationship label (optional).
    // TOEXPR Entity being mapped to - can be a single code/identifier /concept
    // name or a complex expression involving multiple codes/identifiers/concept
    // names, Boolean operators and/or punctuation.
    // TOTYPE Type of entity being mapped to.
    // CVF The Content View Flag is a bit field used to indicate membership in a
    // content view.

    // Sample Records
    // C1306694|MTH|AT28312030||C0009215|CUI|SY||<Codeine> AND <Drug
    // Hypersensitivity>|BOOLEAN_EXPRESSION_STR||
    // C1306694|MTH|AT28312033||C0795964|CUI|RU||<Speech
    // Disorders>|BOOLEAN_EXPRESSION_STR||
    // C2919943|SNOMEDCT|AT127959271||302759005|SCUI|RN|mapped_to|799.59|BOOLEAN_EXPRESSION_SDUI||
    // C2919943|SNOMEDCT|AT127959272||43498006|SCUI|RQ|mapped_to|276.69|BOOLEAN_EXPRESSION_SDUI||

    final List<String> lines = new ArrayList<>();

    for (final Mapping mapping : mapset.getMappings()) {
      final StringBuilder sb = new StringBuilder();
      // CUI
      sb.append(terminologyId).append("|");
      // MAPSETSAB
      sb.append(mapset.getTerminology()).append("|");
      // MAPID
      if (mapping.getAlternateTerminologyIds()
          .containsKey(getProject().getTerminology())) {
        sb.append(mapping.getAlternateTerminologyIds()
            .get(getProject().getTerminology()));
      }
      sb.append("|");
      // MAPSID
      sb.append(mapping.getTerminologyId()).append("|");

      // FROMEXPR
      sb.append(mapping.getFromTerminologyId()).append("|");
      // FROMTYPE
      if (mapping.getFromIdType().toString().equals("DESCRIPTOR")) {
        sb.append("SDUI");
      } else if (mapping.getFromIdType().toString().equals("CONCEPT") && mapset
          .getFromTerminology().equals(getProject().getTerminology())) {
        sb.append("CUI");
      } else if (mapping.getFromIdType().toString().equals("CONCEPT")) {
        sb.append("SCUI");
      }
      sb.append("|");

      // REL
      sb.append(mapping.getRelationshipType()).append("|");
      // RELA
      sb.append(mapping.getAdditionalRelationshipType()).append("|");

      // TOEXPR
      sb.append(mapping.getToTerminologyId()).append("|");
      // TOTYPE
      if (mapping.getToIdType().toString().equals("DESCRIPTOR")) {
        sb.append("SDUI");
      } else if (mapping.getToIdType().toString().equals("CONCEPT")
          && mapset.getToTerminology().equals(getProject().getTerminology())) {
        sb.append("CUI");
      } else if (mapping.getToIdType().toString().equals("CONCEPT")) {
        sb.append("SCUI");
      }
      sb.append("|");

      // CVF
      sb.append("|");
      sb.append("\n");
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
    final List<String> lines = new ArrayList<>();

    for (final SemanticTypeComponent sty : c.getSemanticTypes()) {
      final StringBuilder sb = new StringBuilder();
      // CUI
      sb.append(c.getTerminologyId()).append("|");
      // TUI
      sb.append(semTypeMap.get(sty.getSemanticType()).getTypeId()).append("|");
      // STN
      sb.append(semTypeMap.get(sty.getSemanticType()).getTreeNumber())
          .append("|");
      // STY
      sb.append(sty.getSemanticType()).append("|");
      // ATUI
      sb.append(sty.getTerminologyId()).append("|");
      // CVF
      sb.append("|");
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

    final List<String> lines = new ArrayList<>();

    // Concept relationships
    for (final ConceptRelationship rel : c.getInverseRelationships()) {

      final StringBuilder sb = new StringBuilder();
      // CUI1
      sb.append(rel.getTo().getTerminologyId()).append("|");
      // AUI1
      sb.append("|");
      // STYPE1
      sb.append("CUI").append("|");
      // REL
      sb.append(rel.getRelationshipType()).append("|");
      // CUI2
      sb.append(rel.getFrom().getTerminologyId()).append("|");
      // AUI2
      sb.append("|");
      // STYPE2
      sb.append("CUI").append("|");
      // RELA
      sb.append(rel.getAdditionalRelationshipType()).append("|");
      // RUI
      String rui = rel.getTerminologyId();
      sb.append(rui).append("|");
      // SRUI
      sb.append("|");
      // SAB
      sb.append(rel.getTerminology()).append("|");
      // SL Source of relationship labels
      sb.append(rel.getTerminology()).append("|");
      // RG
      sb.append(rel.getGroup()).append("|");
      // DIR
      boolean asserts =
          termMap.get(rel.getTerminology()).isAssertsRelDirection();
      sb.append(asserts ? (rel.isAssertedDirection() ? "Y" : "N") : "")
          .append("|");
      // SUPPRESS
      if (rel.isObsolete()) {
        sb.append("O");
      } else if (rel.isSuppressible()) {
        sb.append("Y");
      } else {
        sb.append("N");
      }
      sb.append("|");
      // CVF
      sb.append("|");
      sb.append("\n");
      lines.add(sb.toString());
    }

    // Atom relationships
    // C0000005|A4345877|AUI|RB|C0036775|A3586555|AUI||R17427607||MSH|MSH|||N||
    for (final Atom a : c.getAtoms()) {

      for (final AtomRelationship r : a.getRelationships()) {
        StringBuilder sb = new StringBuilder();
        sb.append(c.getTerminologyId()).append("|");
        sb.append(
            a.getAlternateTerminologyIds().get(getProject().getTerminology()))
            .append("|");
        sb.append("AUI").append("|");
        sb.append(r.getRelationshipType()).append("|");
        String aui2 = r.getTo().getAlternateTerminologyIds()
            .get(getProject().getTerminology());
        sb.append(auiCuiMap.get(aui2)).append("|");
        sb.append(aui2).append("|");
        sb.append("AUI").append("|");
        sb.append(r.getAdditionalRelationshipType()).append("|");
        String rui =
            r.getAlternateTerminologyIds().get(getProject().getTerminology());
        sb.append(rui != null ? rui : "").append("|");
        sb.append(r.getTerminologyId()).append("|");
        sb.append(r.getTerminology()).append("|");
        sb.append(r.getTerminology()).append("|");
        sb.append(r.getGroup()).append("|");
        boolean asserts =
            termMap.get(r.getTerminology()).isAssertsRelDirection();
        sb.append(asserts ? (r.isAssertedDirection() ? "Y" : "N") : "")
            .append("|");
        if (r.isObsolete()) {
          sb.append("O");
        } else if (r.isSuppressible()) {
          sb.append("Y");
        } else {
          sb.append("N");
        }
        sb.append("|");
        sb.append("|");
        sb.append("\n");
        lines.add(sb.toString());
      }

      // SCUI relationships, if preferred atom of the SCUI
      // e.g.
      // C0000097|A3134287|SCUI|PAR|C0576798|A3476803|SCUI|inverse_isa|R96279727|107042028|SNOMEDCT_US|SNOMEDCT_US|0|N|N||
      if (atomConceptMap.containsKey(a.getId())) {
        final Concept scui = getConcept(atomConceptMap.get(a.getId()));
        for (final ConceptRelationship rel : scui.getRelationships()) {
          final StringBuilder sb = new StringBuilder();
          sb.append(c.getTerminologyId()).append("|");
          sb.append(
              a.getAlternateTerminologyIds().get(getProject().getTerminology()))
              .append("|");
          sb.append("SCUI").append("|");
          sb.append(rel.getRelationshipType()).append("|");
          String aui2 = conceptAuiMap.get(scui.getId());
          sb.append(auiCuiMap.get(aui2)).append("|");
          sb.append(aui2).append("|");
          sb.append("SCUI").append("|");
          sb.append(rel.getAdditionalRelationshipType()).append("|");
          String rui = rel.getAlternateTerminologyIds()
              .get(getProject().getTerminology());
          sb.append(rui != null ? rui : "").append("|");
          sb.append(rel.getTerminologyId()).append("|");
          sb.append(rel.getTerminology()).append("|");
          sb.append(rel.getTerminology()).append("|");
          sb.append(rel.getGroup()).append("|");
          boolean asserts =
              termMap.get(rel.getTerminology()).isAssertsRelDirection();
          sb.append(asserts ? (rel.isAssertedDirection() ? "Y" : "N") : "")
              .append("|");
          if (rel.isObsolete()) {
            sb.append("O");
          } else if (rel.isSuppressible()) {
            sb.append("Y");
          } else {
            sb.append("N");
          }
          sb.append("|");
          sb.append("|");
          sb.append("\n");
          lines.add(sb.toString());
        }

        // look up component info relationships where STYPE1=SCUI
        for (final Relationship<?, ?> relationship : findComponentInfoRelationships(
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
        for (final Relationship<?, ?> relationship : findComponentInfoRelationships(
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
        for (final Relationship<?, ?> relationship : findComponentInfoRelationships(
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

    // Atoms
    for (final Atom atom : c.getAtoms()) {
      int ct = 1;

      // Find tree positions for this atom
      for (final AtomTreePosition treepos : handler.getQueryResults(null, null,
          Branch.ROOT, "nodeId:" + atom.getId(), null,
          AtomTreePositionJpa.class, null, new int[1], manager)) {

        final String aui = atom.getAlternateTerminologyIds()
            .get(getProject().getTerminology());
        final StringBuilder ptr = new StringBuilder();
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

        // e.g. C0001175|A2878223|1|A3316611|SNOMEDCT|isa|
        // A3684559.A3886745.A2880798.A3512117.A3082701.A3316611|||
        final StringBuilder sb = new StringBuilder();
        sb.append(c.getTerminologyId()).append("|");
        sb.append(aui).append("|");
        sb.append("" + ct++).append("|");
        sb.append(paui != null ? paui : "").append("|");
        sb.append(treepos.getTerminology()).append("|");
        sb.append(treepos.getAdditionalRelationshipType()).append("|");
        // If the root string doesn't equal SRC/RHT, write tree-top SRC atom
        String srcRhtName =
            terminologyToSrcRhtNameMap.get(treepos.getTerminology());
        if (root != null && !root.equals(srcRhtName)) {
          sb.append(
              terminologyToSrcAtomIdMap.get(treepos.getTerminology()) + ".");
        }
        sb.append(ptr.toString()).append("|");
        sb.append(treepos.getTerminologyId()).append("|");
        sb.append("|");

        sb.append("\n");
        lines.add(sb.toString());
      }

      // Try for concept treepos
      if (atomConceptMap.containsKey(atom.getId())) {
        for (final ConceptTreePosition treepos : handler.getQueryResults(null,
            null, Branch.ROOT, "nodeId:" + atomConceptMap.get(atom.getId()),
            null, ConceptTreePositionJpa.class, null, new int[1], manager)) {

          final String aui = atom.getAlternateTerminologyIds()
              .get(getProject().getTerminology());
          final StringBuilder ptr = new StringBuilder();
          String paui = null;
          String root = null;
          for (final String conceptId : FieldedStringTokenizer
              .split(treepos.getAncestorPath(), "~")) {
            final Concept concept2 = getConcept(new Long(conceptId));
            if (concept2 == null) {
              throw new Exception("concept from ptr is null " + conceptId);
            }
            if (paui != null) {
              ptr.append(".");
            }
            paui = this.conceptAuiMap.get(conceptId);
            ptr.append(paui);
            if (root == null) {
              root = concept2.getName();
            }
          }

          // e.g. C0001175|A2878223|1|A3316611|SNOMEDCT|isa|
          // A3684559.A3886745.A2880798.A3512117.A3082701.A3316611|||
          final StringBuilder sb = new StringBuilder();
          sb.append(c.getTerminologyId()).append("|");
          sb.append(aui).append("|");
          sb.append("" + ct++).append("|");
          sb.append(paui != null ? paui : "").append("|");
          sb.append(treepos.getTerminology()).append("|");
          sb.append(treepos.getAdditionalRelationshipType()).append("|");
          // If the root string doesn't equal SRC/RHT, write tree-top SRC atom
          String srcRhtName =
              terminologyToSrcRhtNameMap.get(treepos.getTerminology());
          if (root != null && !root.equals(srcRhtName)) {
            sb.append(
                terminologyToSrcAtomIdMap.get(treepos.getTerminology()) + ".");
          }
          sb.append(ptr.toString()).append("|");
          sb.append(treepos.getTerminologyId()).append("|");
          sb.append("|");

          sb.append("\n");
          lines.add(sb.toString());
        }
      }

      // Try for descriptor treepos
      if (atomDescriptorMap.containsKey(atom.getId())) {
        for (final DescriptorTreePosition treepos : handler.getQueryResults(
            null, null, Branch.ROOT,
            "nodeId:" + atomDescriptorMap.get(atom.getId()), null,
            DescriptorTreePositionJpa.class, null, new int[1], manager)) {

          final String aui = atom.getAlternateTerminologyIds()
              .get(getProject().getTerminology());
          final StringBuilder ptr = new StringBuilder();
          String paui = null;
          String root = null;
          for (final String descriptorId : FieldedStringTokenizer
              .split(treepos.getAncestorPath(), "~")) {
            final Descriptor descriptor2 =
                getDescriptor(new Long(descriptorId));
            if (descriptor2 == null) {
              throw new Exception(
                  "descriptor from ptr is null " + descriptorId);
            }
            if (paui != null) {
              ptr.append(".");
            }
            paui = this.descriptorAuiMap.get(descriptorId);
            ptr.append(paui);
            if (root == null) {
              root = descriptor2.getName();
            }
          }

          // e.g. C0001175|A2878223|1|A3316611|SNOMEDCT|isa|
          // A3684559.A3886745.A2880798.A3512117.A3082701.A3316611|||
          final StringBuilder sb = new StringBuilder();
          sb.append(c.getTerminologyId()).append("|");
          sb.append(aui).append("|");
          sb.append("" + ct++).append("|");
          sb.append(paui != null ? paui : "").append("|");
          sb.append(treepos.getTerminology()).append("|");
          sb.append(treepos.getAdditionalRelationshipType()).append("|");
          // If the root string doesn't equal SRC/RHT, write tree-top SRC atom
          String srcRhtName =
              terminologyToSrcRhtNameMap.get(treepos.getTerminology());
          if (root != null && !root.equals(srcRhtName)) {
            sb.append(
                terminologyToSrcAtomIdMap.get(treepos.getTerminology()) + ".");
          }
          sb.append(ptr.toString()).append("|");
          sb.append(treepos.getTerminologyId()).append("|");
          sb.append("|");

          sb.append("\n");
          lines.add(sb.toString());
        }
      }

      // Try for code treepos
      if (atomCodeMap.containsKey(atom.getId())) {
        for (final CodeTreePosition treepos : handler.getQueryResults(null,
            null, Branch.ROOT, "nodeId:" + atomCodeMap.get(atom.getId()), null,
            CodeTreePositionJpa.class, null, new int[1], manager)) {

          final String aui = atom.getAlternateTerminologyIds()
              .get(getProject().getTerminology());
          final StringBuilder ptr = new StringBuilder();
          String paui = null;
          String root = null;
          for (final String codeId : FieldedStringTokenizer
              .split(treepos.getAncestorPath(), "~")) {
            final Code code2 = getCode(new Long(codeId));
            if (code2 == null) {
              throw new Exception("code from ptr is null " + codeId);
            }
            if (paui != null) {
              ptr.append(".");
            }
            paui = this.codeAuiMap.get(codeId);
            ptr.append(paui);
            if (root == null) {
              root = code2.getName();
            }
          }

          // e.g. C0001175|A2878223|1|A3316611|SNOMEDCT|isa|
          // A3684559.A3886745.A2880798.A3512117.A3082701.A3316611|||
          final StringBuilder sb = new StringBuilder();
          sb.append(c.getTerminologyId()).append("|");
          sb.append(aui).append("|");
          sb.append("" + ct++).append("|");
          sb.append(paui != null ? paui : "").append("|");
          sb.append(treepos.getTerminology()).append("|");
          sb.append(treepos.getAdditionalRelationshipType()).append("|");
          // If the root string doesn't equal SRC/RHT, write tree-top SRC atom
          String srcRhtName =
              terminologyToSrcRhtNameMap.get(treepos.getTerminology());
          if (root != null && !root.equals(srcRhtName)) {
            sb.append(
                terminologyToSrcAtomIdMap.get(treepos.getTerminology()) + ".");
          }
          sb.append(ptr.toString()).append("|");
          sb.append(treepos.getTerminologyId()).append("|");
          sb.append("|");

          sb.append("\n");
          lines.add(sb.toString());
        }
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

    // NOTE: MR/ST/DA attributes are not written out for NCIMETA

    final List<String> lines = new ArrayList<>();

    // Concept attributes (CUIs)
    for (final Attribute att : c.getAttributes()) {
      final StringBuilder sb = new StringBuilder();
      // CUI
      sb.append(c.getTerminologyId()).append("|");
      // LUI, SUI, METAUI
      sb.append("|||");
      // STYPE
      sb.append("CUI").append("|");
      // CODE
      sb.append("|");
      // ATUI
      final String atui =
          att.getAlternateTerminologyIds().get(getProject().getTerminology());
      sb.append(atui != null ? atui : "").append("|");
      // SATUI
      sb.append(att.getTerminologyId() != null ? att.getTerminologyId() : "")
          .append("|");
      // ATN
      sb.append(att.getName()).append("|");
      // SAB
      sb.append(att.getTerminology()).append("|");
      // ATV
      sb.append(att.getValue()).append("|");
      // SUPPRESS
      if (att.isObsolete()) {
        sb.append("O");
      } else if (att.isSuppressible()) {
        sb.append("Y");
      } else {
        sb.append("N");
      }
      sb.append("|");
      // CVF
      sb.append("|\n");
      lines.add(sb.toString());
    }

    // Handle atom, and atom class attributes
    for (final Atom a : c.getAtoms()) {

      // Atom attributes (AUIs)
      // e.g.
      // C0000005|L0186915|S2192525|A4345877|AUI|D012711|AT25166652||TERMUI|MSH|T037573|N||
      for (final Attribute att : a.getAttributes()) {
        StringBuilder sb = new StringBuilder();
        // CUI
        sb.append(c.getTerminologyId()).append("|");
        // LUI
        sb.append(a.getLexicalClassId()).append("|");
        // SUI
        sb.append(a.getStringClassId()).append("|");
        // METAUI
        sb.append(
            a.getAlternateTerminologyIds().get(getProject().getTerminology()))
            .append("|");
        // STYPE
        sb.append("AUI").append("|");
        // CODE
        sb.append(a.getCodeId()).append("|");
        // ATUI
        String atui =
            att.getAlternateTerminologyIds().get(getProject().getTerminology());
        sb.append(atui != null ? atui : "").append("|");
        // SATUI
        sb.append(att.getTerminologyId() != null ? att.getTerminologyId() : "")
            .append("|");
        // ATN
        sb.append(att.getName()).append("|");
        // SAB
        sb.append(att.getTerminology()).append("|");
        // ATV
        sb.append(att.getValue()).append("|");
        // SUPPRESS
        if (att.isObsolete()) {
          sb.append("O");
        } else if (att.isSuppressible()) {
          sb.append("Y");
        } else {
          sb.append("N");
        }
        sb.append("|");
        // CVF
        sb.append("|");
        sb.append("\n");
        lines.add(sb.toString());
      }

      // Atom relationship attributes (RUIs)
      // e.g.
      // C0000097|||R94999574|RUI||AT110096379||CHARACTERISTIC_TYPE_ID|SNOMEDCT_US|900000000000011006|N||
      if (ruiAttributeTerminologies.contains(a.getTerminology())) {
        for (final AtomRelationship rel : a.getRelationships()) {
          for (final Attribute attribute : rel.getAttributes()) {
            final StringBuilder sb = new StringBuilder();
            // CUI
            sb.append(c.getTerminologyId()).append("|");
            // LUI
            sb.append("|");
            // SUI
            sb.append("|");
            // METAUI
            sb.append(rel.getAlternateTerminologyIds()
                .get(getProject().getTerminology())).append("|");
            // STYPE
            sb.append("RUI").append("|");
            // CODE
            sb.append("|");
            // ATUI
            String atui = attribute.getAlternateTerminologyIds()
                .get(getProject().getTerminology());
            sb.append(atui != null ? atui : "").append("|");
            // SATUI
            sb.append(attribute.getTerminologyId() != null
                ? attribute.getTerminologyId() : "").append("|");
            // ATN
            sb.append(attribute.getName()).append("|");
            // SAB
            sb.append(attribute.getTerminology()).append("|");
            // ATV
            sb.append(attribute.getValue()).append("|");
            // SUPPRESS
            if (attribute.isObsolete()) {
              sb.append("O");
            } else if (attribute.isSuppressible()) {
              sb.append("Y");
            } else {
              sb.append("N");
            }
            sb.append("|");
            // CVF
            sb.append("|");
            sb.append("\n");
            lines.add(sb.toString());
          }
        }
      }

      // Subset members
      // e.g.
      // C0000052|L3853359|S4536829|A23245828|AUI|58488005|AT166631006|
      // cf28ec3d-cf07-59cb-944a-10ef4f43b725|SUBSET_MEMBER|SCTSPA|
      // 450828004~ACCEPTABILITYID~900000000000549004|N||
      // C0000052|L3853359|S4536829|A23245828|AUI|58488005|AT166631006|
      // cf28ec3d-cf07-59cb-944a-10ef4f43b725|SUBSET_MEMBER|SNOMEDCT|
      // 450828004|N||
      for (final AtomSubsetMember member : a.getMembers()) {
        for (final Attribute att : member.getAttributes()) {
          final StringBuilder sb = new StringBuilder();
          sb.append(c.getTerminologyId()).append("|");
          sb.append(a.getLexicalClassId()).append("|");
          sb.append(a.getStringClassId()).append("|");
          sb.append(
              a.getAlternateTerminologyIds().get(getProject().getTerminology()))
              .append("|");
          sb.append("AUI").append("|");
          sb.append(a.getCodeId()).append("|");
          sb.append(att.getAlternateTerminologyIds()
              .get(getProject().getTerminology())).append("|");
          sb.append(member.getTerminologyId()).append("|");
          sb.append("SUBSET_MEMBER").append("|");
          sb.append(att.getTerminology()).append("|");
          sb.append(member.getSubset().getTerminologyId());
          if (!ConfigUtility.isEmpty(att.getName())) {
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
          sb.append("|");
          sb.append("|");
          sb.append("\n");
          lines.add(sb.toString());
        }

      }

      // Source concept attributes (SCUIs)
      // e.g.
      // C0000102|L0121443|S1286670|A3714229|SCUI|13579002|AT112719256||ACTIVE|SNOMEDCT_US|1|N||
      // If this is the preferred atom id of the scui
      if (atomConceptMap.containsKey(a.getId())) {
        final Concept scui = getConcept(atomConceptMap.get(a.getId()));
        for (final Attribute attribute : scui.getAttributes()) {
          final StringBuilder sb = new StringBuilder();
          sb.append(c.getTerminologyId()).append("|");
          sb.append(a.getLexicalClassId()).append("|");
          sb.append(a.getStringClassId()).append("|");
          sb.append(
              a.getAlternateTerminologyIds().get(getProject().getTerminology()))
              .append("|");
          sb.append("SCUI").append("|");
          sb.append(a.getConceptId()).append("|");
          String atui = attribute.getAlternateTerminologyIds()
              .get(getProject().getTerminology());
          sb.append(atui != null ? atui : "").append("|");
          sb.append(attribute.getTerminologyId() != null
              ? attribute.getTerminologyId() : "").append("|");
          sb.append(attribute.getName()).append("|");
          sb.append(attribute.getTerminology()).append("|");
          sb.append(attribute.getValue()).append("|");
          if (attribute.isObsolete()) {
            sb.append("O");
          } else if (attribute.isSuppressible()) {
            sb.append("Y");
          } else {
            sb.append("N");
          }
          sb.append("|");
          sb.append("|");
          sb.append("\n");
          lines.add(sb.toString());
        }

        // Source concept relationship attributes (RUIs)
        if (ruiAttributeTerminologies.contains(scui.getTerminology())) {
          for (final ConceptRelationship rel : scui.getRelationships()) {
            for (final Attribute attribute : rel.getAttributes()) {
              final StringBuilder sb = new StringBuilder();
              sb.append(c.getTerminologyId()).append("|");
              sb.append("|");
              sb.append("|");
              sb.append(rel.getAlternateTerminologyIds()
                  .get(getProject().getTerminology())).append("|");
              sb.append("RUI").append("|");
              sb.append("|");
              String atui = attribute.getAlternateTerminologyIds()
                  .get(getProject().getTerminology());
              sb.append(atui != null ? atui : "").append("|");
              sb.append(attribute.getTerminologyId() != null
                  ? attribute.getTerminologyId() : "").append("|");
              sb.append(attribute.getName()).append("|");
              sb.append(attribute.getTerminology()).append("|");
              sb.append(attribute.getValue()).append("|");
              if (attribute.isObsolete()) {
                sb.append("O");
              } else if (attribute.isSuppressible()) {
                sb.append("Y");
              } else {
                sb.append("N");
              }
              sb.append("|");
              sb.append("|");
              sb.append("\n");
              lines.add(sb.toString());
            }
          }
        }

        // Concept subset members
        // C0000102|L0121443|S1286670|A3714229|SCUI|13579002|AT109859972|cbe76318-0356-54e6-9935-03962bd340eb|SUBSET_MEMBER|SNOMEDCT_US|900000000000498005~MAPTARGET~C-29040|N||
        for (final ConceptSubsetMember member : scui.getMembers()) {
          for (final Attribute att : member.getAttributes()) {
            final StringBuilder sb = new StringBuilder();
            sb.append(c.getTerminologyId()).append("|");
            sb.append(a.getLexicalClassId()).append("|");
            sb.append(a.getStringClassId()).append("|");
            sb.append(a.getAlternateTerminologyIds()
                .get(getProject().getTerminology())).append("|");
            sb.append("SCUI").append("|");
            sb.append(a.getConceptId()).append("|");
            sb.append(att.getAlternateTerminologyIds()
                .get(getProject().getTerminology())).append("|");
            sb.append(member.getTerminologyId()).append("|");
            sb.append("SUBSET_MEMBER").append("|");
            sb.append(att.getTerminology()).append("|");
            sb.append(member.getSubset().getTerminologyId());
            if (!ConfigUtility.isEmpty(att.getName())) {
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
            sb.append("|");
            sb.append("|");
            sb.append("\n");
            lines.add(sb.toString());
          }

        }
      }

      // Code attributes
      // e.g.
      // C0010654|L1371351|S2026553|A10006797|SCUI|NPO_384|AT73054966||CODE|NPO|NPO_384|N||
      // If atom is the preferred atom of the CODE
      if (atomCodeMap.containsKey(a.getId())) {
        final Code code = getCode(atomCodeMap.get(a.getId()));
        for (final Attribute attribute : code.getAttributes()) {

          StringBuilder sb = new StringBuilder();
          // CUI
          sb.append(c.getTerminologyId()).append("|");
          // LUI
          sb.append(a.getLexicalClassId()).append("|");
          // SUI
          sb.append(a.getStringClassId()).append("|");
          // METAUI
          sb.append(
              a.getAlternateTerminologyIds().get(getProject().getTerminology()))
              .append("|");
          // STYPE
          sb.append("CODE").append("|");
          // CODE
          sb.append("|");
          // ATUI
          String atui = attribute.getAlternateTerminologyIds()
              .get(getProject().getTerminology());
          sb.append(atui).append("|");
          // SATUI
          sb.append(attribute.getTerminologyId() != null
              ? attribute.getTerminologyId() : "").append("|");
          // ATN
          sb.append(attribute.getName()).append("|");
          // SAB
          sb.append(attribute.getTerminology()).append("|");
          // ATV
          sb.append(attribute.getValue()).append("|");
          // SUPPRESS
          if (attribute.isObsolete()) {
            sb.append("O");
          } else if (attribute.isSuppressible()) {
            sb.append("Y");
          } else {
            sb.append("N");
          }
          sb.append("|");
          // CVF
          sb.append("|");
          sb.append("\n");
          lines.add(sb.toString());
        }

        // Code relationship attributes (RUIs)
        // TBD - no data at this point in time

      }

      // Source Descriptor attributes
      // if atom is preferred atom of the descriptor
      if (atomDescriptorMap.containsKey(a.getId())) {
        final Descriptor descriptor =
            getDescriptor(atomDescriptorMap.get(a.getId()));
        for (final Attribute attribute : descriptor.getAttributes()) {
          StringBuilder sb = new StringBuilder();
          sb.append(c.getTerminologyId()).append("|");
          sb.append(a.getLexicalClassId()).append("|");
          sb.append(a.getStringClassId()).append("|");
          sb.append(
              a.getAlternateTerminologyIds().get(getProject().getTerminology()))
              .append("|");
          sb.append("SDUI").append("|");
          sb.append(a.getDescriptorId()).append("|");
          String atui = attribute.getAlternateTerminologyIds()
              .get(getProject().getTerminology());
          sb.append(atui != null ? atui : "").append("|");
          sb.append(attribute.getTerminologyId() != null
              ? attribute.getTerminologyId() : "").append("|");
          sb.append(attribute.getName()).append("|");
          sb.append(attribute.getTerminology()).append("|");
          sb.append(attribute.getValue()).append("|");
          if (attribute.isObsolete()) {
            sb.append("O");
          } else if (attribute.isSuppressible()) {
            sb.append("Y");
          } else {
            sb.append("N");
          }
          sb.append("|");
          sb.append("|");
          sb.append("\n");
          lines.add(sb.toString());
        }

        // Descriptor relationship attributes (RUIs)
        // TBD - no data yet
      }

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
