/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import com.wci.umls.server.AlgorithmParameter;
import com.wci.umls.server.ReleaseInfo;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.FieldedStringTokenizer;
import com.wci.umls.server.helpers.KeyValuePair;
import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.jpa.AlgorithmParameterJpa;
import com.wci.umls.server.jpa.ReleaseInfoJpa;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.ConceptRelationshipJpa;
import com.wci.umls.server.jpa.content.SemanticTypeComponentJpa;
import com.wci.umls.server.jpa.helpers.PrecedenceListJpa;
import com.wci.umls.server.jpa.meta.AdditionalRelationshipTypeJpa;
import com.wci.umls.server.jpa.meta.LanguageJpa;
import com.wci.umls.server.jpa.meta.RelationshipTypeJpa;
import com.wci.umls.server.jpa.meta.RootTerminologyJpa;
import com.wci.umls.server.jpa.meta.SemanticTypeJpa;
import com.wci.umls.server.jpa.meta.TermTypeJpa;
import com.wci.umls.server.jpa.meta.TerminologyJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Component;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.content.SemanticTypeComponent;
import com.wci.umls.server.model.meta.AdditionalRelationshipType;
import com.wci.umls.server.model.meta.CodeVariantType;
import com.wci.umls.server.model.meta.IdType;
import com.wci.umls.server.model.meta.Language;
import com.wci.umls.server.model.meta.NameVariantType;
import com.wci.umls.server.model.meta.RelationshipType;
import com.wci.umls.server.model.meta.RootTerminology;
import com.wci.umls.server.model.meta.SemanticType;
import com.wci.umls.server.model.meta.TermType;
import com.wci.umls.server.model.meta.TermTypeStyle;
import com.wci.umls.server.model.meta.Terminology;
import com.wci.umls.server.model.meta.UsageType;
import com.wci.umls.server.model.workflow.WorkflowStatus;
import com.wci.umls.server.services.RootService;
import com.wci.umls.server.services.helpers.PushBackReader;

/**
 * Implementation of an algorithm to import data from two files.
 * 
 * <pre>
 * 1. a conceptId|type|pt[|sy]* file 2. a par/chd relationships file
 */
public class SimpleLoaderAlgorithm extends AbstractTerminologyLoaderAlgorithm {

  /** The loader. */
  private final String loader = "loader";

  /** The date. */
  private final Date date = new Date();

  /** The concept map. */
  private Map<String, Long> conceptIdMap = new HashMap<>(10000);

  /**
   * Instantiates an empty {@link SimpleLoaderAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public SimpleLoaderAlgorithm() throws Exception {
    super();
  }

  /**
   * Compute.
   *
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public void compute() throws Exception {

    logInfo("Start simple load");
    logInfo("  terminology = " + getTerminology());
    logInfo("  version = " + getVersion());
    logInfo("  inputDir = " + getInputPath());

    // Set the "release version"
    setReleaseVersion(ConfigUtility.DATE_FORMAT.format(date));
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

    // Check the input directory
    File inputDirFile = new File(getInputPath());
    if (!inputDirFile.exists()) {
      throw new Exception("Specified input directory does not exist");
    }
    if (!new File(getInputPath(), "concepts.txt").exists()) {
      throw new Exception(
          "The concepts.txt file of the input directory does not exist");
    }
    if (!new File(getInputPath(), "parChd.txt").exists()) {
      throw new Exception(
          "The parChd.txt file of the input directory does not exist");
    }

    // // Make sure concepts.txt is sorted by the first field
    // if (!FileSorter.checkSortedFile(new File(getInputPath(), "concepts.txt"),
    // // first field comparator
    // getComparator(new int[] {
    // 0
    // }))) {
    // throw new Exception(
    // "The concepts.txt file must be sorted on the first field");
    // }

    // faster performance.
    beginTransaction();

    // Semantic type, termTypes, languages, PAR/CHD rel types, prec list, etc.
    loadMetadata();

    // Assume files concepts.txt, parChd.txt

    loadAtoms();

    loadRelationships();

    // Commit
    commitClearBegin();

    // Add release info for this load
    final Terminology terminology =
        getTerminologyLatestVersion(getTerminology());
    ReleaseInfo info =
        getReleaseInfo(terminology.getTerminology(), getReleaseVersion());
    if (info == null) {
      info = new ReleaseInfoJpa();
      info.setName(getTerminology());
      info.setDescription(terminology.getTerminology() + " "
          + getReleaseVersion() + " release");
      info.setPlanned(false);
      info.setPublished(true);
      info.setReleaseBeginDate(null);
      info.setTerminology(terminology.getTerminology());
      info.setVersion(getReleaseVersion());
      info.setLastModified(date);
      info.setLastModifiedBy(loader);
      addReleaseInfo(info);
    } else {
      throw new Exception("Release info unexpectedly already exists for "
          + getReleaseVersion());
    }

    // Clear concept cache

    logInfo("Log component stats");
    final Map<String, Integer> stats = getComponentStats(null, null, null);
    final List<String> statsList = new ArrayList<>(stats.keySet());
    Collections.sort(statsList);
    for (final String key : statsList) {
      logInfo("  " + key + " = " + stats.get(key));
    }

    // clear and commit
    commit();
    clear();

    // Final logging messages
    Logger.getLogger(getClass())
        .info("      elapsed time = " + getTotalElapsedTimeStr(startTimeOrig));
    Logger.getLogger(getClass()).info("done ...");

  }

  /**
   * Load SRDEF. This is responsible for loading {@link SemanticType} metadata.
   *
   * @throws Exception the exception
   */
  private void loadMetadata() throws Exception {
    logInfo("  Load Semantic types");

    String line = null;
    int objectCt = 0;
    PushBackReader reader = new PushBackReader(
        new FileReader(new File(getInputPath(), "concepts.txt")));
    final String[] fields = new String[10];

    Set<String> types = new HashSet<>();
    while ((line = reader.readLine()) != null) {

      line = line.replace("\r", "");
      FieldedStringTokenizer.split(line, "|", 10, fields);

      if (!ConfigUtility.isEmpty(fields[1])) {
        types.add(fields[1]);
      }
    }
    reader.close();

    // Create a semantic type for each unique value
    for (final String type : types) {

      final SemanticType sty = new SemanticTypeJpa();
      sty.setAbbreviation(type);
      sty.setDefinition("");
      sty.setExample("");
      sty.setExpandedForm(type);
      sty.setNonHuman(false);
      sty.setTerminology(getTerminology());
      sty.setVersion(getVersion());
      sty.setTreeNumber("");
      sty.setTypeId("");
      sty.setUsageNote("");
      sty.setTimestamp(date);
      sty.setLastModified(date);
      sty.setLastModifiedBy(loader);
      sty.setPublished(true);
      sty.setPublishable(true);
      Logger.getLogger(getClass()).debug("    add semantic type - " + sty);
      addSemanticType(sty);
      logAndCommit(++objectCt, RootService.logCt, RootService.commitCt);

    }
    commitClearBegin();

    // Root terminology
    final RootTerminology root = new RootTerminologyJpa();
    root.setFamily(getTerminology());
    root.setPreferredName(getTerminology());
    root.setRestrictionLevel(0);
    root.setTerminology(getTerminology());
    root.setTimestamp(date);
    root.setLastModified(date);
    root.setLastModifiedBy(loader);
    addRootTerminology(root);

    // Terminology
    final Terminology term = new TerminologyJpa();
    term.setAssertsRelDirection(false);
    term.setCurrent(true);
    term.setOrganizingClassType(IdType.CONCEPT);
    term.setPreferredName(getTerminology());
    term.setTimestamp(date);
    term.setLastModified(date);
    term.setLastModifiedBy(loader);
    term.setTerminology(getTerminology());
    term.setVersion(getVersion());
    term.setDescriptionLogicTerminology(false);
    term.setMetathesaurus(false);
    term.setRootTerminology(root);
    addTerminology(term);

    // Languages (ENG)
    final Language lat = new LanguageJpa();
    lat.setAbbreviation("en");
    lat.setExpandedForm("English");
    lat.setTimestamp(date);
    lat.setLastModified(date);
    lat.setLastModifiedBy(loader);
    lat.setTerminology(getTerminology());
    lat.setVersion(getVersion());
    lat.setPublished(true);
    lat.setPublishable(true);
    lat.setISO3Code("ENG");
    lat.setISOCode("en");
    addLanguage(lat);

    // Term types (PT, SY)
    TermType tty = new TermTypeJpa();
    tty.setAbbreviation("PT");
    tty.setExpandedForm("Preferred term");
    tty.setTimestamp(date);
    tty.setLastModified(date);
    tty.setLastModifiedBy(loader);
    tty.setTerminology(getTerminology());
    tty.setVersion(getVersion());
    tty.setPublished(true);
    tty.setPublishable(true);
    tty.setCodeVariantType(CodeVariantType.UNDEFINED);
    tty.setHierarchicalType(false);
    tty.setNameVariantType(NameVariantType.UNDEFINED);
    tty.setSuppressible(false);
    tty.setStyle(TermTypeStyle.UNDEFINED);
    tty.setUsageType(UsageType.UNDEFINED);
    addTermType(tty);

    tty = new TermTypeJpa(tty);
    tty.setId(null);
    tty.setAbbreviation("SY");
    tty.setExpandedForm("Synonym");
    addTermType(tty);

    // Rel types (PAR,CHD) - inverses of each other.
    RelationshipType rel = new RelationshipTypeJpa();
    rel.setAbbreviation("PAR");
    rel.setExpandedForm("Parent of");
    rel.setTimestamp(date);
    rel.setLastModified(date);
    rel.setLastModifiedBy(loader);
    rel.setTerminology(getTerminology());
    rel.setVersion(getVersion());
    rel.setPublished(true);
    rel.setPublishable(true);
    rel.setHierarchical(true);
    final RelationshipType par = addRelationshipType(rel);

    rel = new RelationshipTypeJpa(rel);
    rel.setId(null);
    rel.setAbbreviation("CHD");
    rel.setAbbreviation("Child of");
    final RelationshipType chd = addRelationshipType(rel);

    par.setInverse(chd);
    chd.setInverse(par);
    updateRelationshipType(par);
    updateRelationshipType(chd);

    // Precedence List PT, SY
    final PrecedenceList list = new PrecedenceListJpa();
    list.setTerminology(getTerminology());
    list.setVersion(getVersion());
    list.setLastModified(date);
    list.setTimestamp(date);
    list.setLastModifiedBy(loader);
    list.setName("Default precedence list");
    list.getPrecedence()
        .addKeyValuePair(new KeyValuePair(getTerminology(), "PT"));
    list.getPrecedence()
        .addKeyValuePair(new KeyValuePair(getTerminology(), "SY"));
    addPrecedenceList(list);

    AdditionalRelationshipType rela = new AdditionalRelationshipTypeJpa();
    rela.setAbbreviation("isa");
    rela.setExpandedForm("Is a");
    rela.setTimestamp(date);
    rela.setLastModified(date);
    rela.setLastModifiedBy(loader);
    rela.setTerminology(getTerminology());
    rela.setVersion(getVersion());
    rela.setPublished(true);
    rela.setPublishable(true);
    rela.setHierarchical(true);
    addAdditionalRelationshipType(rela);

    // Attribute names - none

    commitClearBegin();
  }

  /**
   * Load the concepts.txt file
   *
   * @throws Exception the exception
   */
  private void loadAtoms() throws Exception {
    logInfo("  Insert atoms and concepts and semantic types");

    // Set up maps
    String line = null;

    int objectCt = 0;
    final PushBackReader reader = new PushBackReader(
        new FileReader(new File(getInputPath(), "concepts.txt")));

    while ((line = reader.readLine()) != null) {

      line = line.replace("\r", "");
      final String[] fields = FieldedStringTokenizer.split(line, "|");

      // Field Description
      // 0 conceptid
      // 1 type
      // 2 pt
      // 3-9 sy

      final Concept concept = new ConceptJpa();
      setCommonFields(concept);
      concept.setTerminologyId(fields[0]);
      concept.setWorkflowStatus(WorkflowStatus.PUBLISHED);

      // Add preferred term
      final Atom atom = new AtomJpa();
      setCommonFields(atom);
      atom.setWorkflowStatus(WorkflowStatus.PUBLISHED);
      atom.setName(fields[2]);
      atom.setTerminologyId("");
      atom.setTermType("PT");
      atom.setLanguage("en");
      atom.setCodeId("");
      atom.setConceptId(fields[0]);
      atom.setDescriptorId("");
      atom.setStringClassId("");
      atom.setLexicalClassId("");
      // Add atom
      addAtom(atom);
      concept.getAtoms().add(atom);
      concept.setName(atom.getName());

      // Add any synonyms
      for (int i = 3; i < fields.length; i++) {
        final Atom sy = new AtomJpa();
        sy.setTimestamp(date);
        sy.setLastModified(date);
        sy.setLastModifiedBy(loader);
        sy.setObsolete(false);
        sy.setSuppressible(false);
        sy.setPublished(true);
        sy.setPublishable(true);
        sy.setWorkflowStatus(WorkflowStatus.PUBLISHED);
        sy.setName(fields[i]);
        sy.setTerminology(getTerminology());
        sy.setVersion(getVersion());
        sy.setTerminologyId("");
        sy.setTermType("SY");
        sy.setLanguage("en");
        sy.setCodeId("");
        sy.setConceptId(fields[0]);
        sy.setDescriptorId("");
        sy.setStringClassId("");
        sy.setLexicalClassId("");
        // Add atom
        addAtom(sy);
        concept.getAtoms().add(sy);
      }

      // Add semantic type
      final SemanticTypeComponent sty = new SemanticTypeComponentJpa();
      setCommonFields(sty);
      sty.setSemanticType(fields[1]);
      sty.setTerminologyId("");
      sty.setWorkflowStatus(WorkflowStatus.PUBLISHED);
      addSemanticTypeComponent(sty, concept);
      concept.getSemanticTypes().add(sty);

      addConcept(concept);
      conceptIdMap.put(concept.getTerminologyId(), concept.getId());

      // commit periodically
      logAndCommit(++objectCt, RootService.logCt, RootService.commitCt);

    }

    commitClearBegin();
    reader.close();
  }

  /**
   * Load relationships.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings("resource")
  private void loadRelationships() throws Exception {
    logInfo("  Insert par/chd relationships");

    // Set up maps
    String line = null;

    int objectCt = 0;
    final PushBackReader reader = new PushBackReader(
        new FileReader(new File(getInputPath(), "parChd.txt")));
    final String[] fields = new String[2];
    while ((line = reader.readLine()) != null) {

      line = line.replace("\r", "");
      FieldedStringTokenizer.split(line, "|", 2, fields);

      // Look up id for concept terminolgy ids, load, create rel and insert.
      final Concept from = getConcept(conceptIdMap.get(fields[0]));
      if (from == null) {
        throw new Exception(
            "Relationship from nonexistent concept " + fields[0]);
      }
      final Concept to = getConcept(conceptIdMap.get(fields[1]));
      if (to == null) {
        throw new Exception("Relationship to nonexistent concept " + fields[1]);
      }

      final ConceptRelationship par = new ConceptRelationshipJpa();
      setCommonFields(par);
      par.setRelationshipType("PAR");
      par.setAdditionalRelationshipType("");
      par.setFrom(from);
      par.setTo(to);
      par.setTerminologyId("");
      par.setWorkflowStatus(WorkflowStatus.PUBLISHED);
      par.setStated(true);
      par.setInferred(true);
      addRelationship(par);

      final ConceptRelationship chd = new ConceptRelationshipJpa();
      setCommonFields(chd);
      chd.setRelationshipType("CHD");
      chd.setHierarchical(true);
      chd.setAdditionalRelationshipType("isa");
      chd.setFrom(to);
      chd.setTo(from);
      chd.setTerminologyId("");
      chd.setWorkflowStatus(WorkflowStatus.PUBLISHED);
      chd.setStated(true);
      chd.setInferred(true);
      addRelationship(chd);

      logAndCommit(++objectCt, RootService.logCt, RootService.commitCt);

    }

    commitClearBegin();
    reader.close();
  }

  /**
   * Reset.
   *
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public void reset() throws Exception {
    // do nothing
  }

  /**
   * Returns the elapsed time.
   *
   * @param time the time
   * @return the elapsed time
   */
  @SuppressWarnings({
      "boxing", "unused"
  })
  private static Long getElapsedTime(long time) {
    return (System.nanoTime() - time) / 1000000000;
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {
    return new ValidationResultJpa();
  }

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    checkRequiredProperties(new String[] {
        "inputDir"
    }, p);
    if (p.getProperty("inputDir") != null) {
      setInputPath(p.getProperty("inputDir"));
    }
  }

  /* see superclass */
  @Override
  public List<AlgorithmParameter> getParameters() {
    final List<AlgorithmParameter> params = super.getParameters();
    AlgorithmParameter param = new AlgorithmParameterJpa("Input Dir",
        "inputDir", "Input RRF directory to load", "", 255,
        AlgorithmParameter.Type.DIRECTORY);
    params.add(param);
    return params;
  }

  /**
   * Sets the common fields.
   *
   * @param comp the common fields
   */
  private void setCommonFields(Component comp) {
    comp.setTimestamp(date);
    comp.setLastModified(date);
    comp.setLastModifiedBy(loader);
    comp.setObsolete(false);
    comp.setSuppressible(false);
    comp.setPublished(true);
    comp.setPublishable(true);
    comp.setTerminology(getTerminology());
    comp.setVersion(getVersion());
  }

  /**
   * Returns the comparator.
   *
   * @param sortColumns the sort columns
   * @return the comparator
   */
  @SuppressWarnings({
      "static-method", "unused"
  })
  private Comparator<String> getComparator(final int[] sortColumns) {
    return new Comparator<String>() {
      @Override
      public int compare(String s1, String s2) {
        String v1[] = s1.split("\\|");
        String v2[] = s2.split("\\|");
        for (final int sortColumn : sortColumns) {
          final int cmp = v1[sortColumn].compareTo(v2[sortColumn]);
          if (cmp != 0) {
            return cmp;
          }
        }
        return 0;
      }
    };
  }

  /* see superclass */
  @Override
  public String getFileVersion() throws Exception {
    return "";
  }

}
