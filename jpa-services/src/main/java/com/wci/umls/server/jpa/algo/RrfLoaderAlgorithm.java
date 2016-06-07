/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo;

import gnu.trove.strategy.HashingStrategy;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;

import com.wci.umls.server.ReleaseInfo;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.ComponentInfo;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.FieldedStringTokenizer;
import com.wci.umls.server.helpers.HasTerminologyId;
import com.wci.umls.server.helpers.KeyValuePair;
import com.wci.umls.server.helpers.KeyValuePairList;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.helpers.meta.SemanticTypeList;
import com.wci.umls.server.jpa.ComponentInfoJpa;
import com.wci.umls.server.jpa.ReleaseInfoJpa;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.AtomRelationshipJpa;
import com.wci.umls.server.jpa.content.AtomSubsetJpa;
import com.wci.umls.server.jpa.content.AtomSubsetMemberJpa;
import com.wci.umls.server.jpa.content.AttributeJpa;
import com.wci.umls.server.jpa.content.CodeJpa;
import com.wci.umls.server.jpa.content.CodeRelationshipJpa;
import com.wci.umls.server.jpa.content.ComponentInfoRelationshipJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.ConceptRelationshipJpa;
import com.wci.umls.server.jpa.content.ConceptSubsetJpa;
import com.wci.umls.server.jpa.content.ConceptSubsetMemberJpa;
import com.wci.umls.server.jpa.content.DefinitionJpa;
import com.wci.umls.server.jpa.content.DescriptorJpa;
import com.wci.umls.server.jpa.content.DescriptorRelationshipJpa;
import com.wci.umls.server.jpa.content.MapSetJpa;
import com.wci.umls.server.jpa.content.MappingJpa;
import com.wci.umls.server.jpa.content.SemanticTypeComponentJpa;
import com.wci.umls.server.jpa.helpers.PrecedenceListJpa;
import com.wci.umls.server.jpa.meta.AdditionalRelationshipTypeJpa;
import com.wci.umls.server.jpa.meta.AttributeNameJpa;
import com.wci.umls.server.jpa.meta.CitationJpa;
import com.wci.umls.server.jpa.meta.ContactInfoJpa;
import com.wci.umls.server.jpa.meta.GeneralMetadataEntryJpa;
import com.wci.umls.server.jpa.meta.LanguageJpa;
import com.wci.umls.server.jpa.meta.RelationshipTypeJpa;
import com.wci.umls.server.jpa.meta.RootTerminologyJpa;
import com.wci.umls.server.jpa.meta.SemanticTypeJpa;
import com.wci.umls.server.jpa.meta.TermTypeJpa;
import com.wci.umls.server.jpa.meta.TerminologyJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.AtomClass;
import com.wci.umls.server.model.content.AtomRelationship;
import com.wci.umls.server.model.content.AtomSubset;
import com.wci.umls.server.model.content.AtomSubsetMember;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.CodeRelationship;
import com.wci.umls.server.model.content.ComponentHasAttributes;
import com.wci.umls.server.model.content.ComponentHasAttributesAndName;
import com.wci.umls.server.model.content.ComponentInfoRelationship;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.content.ConceptSubset;
import com.wci.umls.server.model.content.ConceptSubsetMember;
import com.wci.umls.server.model.content.Definition;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.DescriptorRelationship;
import com.wci.umls.server.model.content.MapSet;
import com.wci.umls.server.model.content.Mapping;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.model.content.SemanticTypeComponent;
import com.wci.umls.server.model.content.Subset;
import com.wci.umls.server.model.content.SubsetMember;
import com.wci.umls.server.model.meta.AdditionalRelationshipType;
import com.wci.umls.server.model.meta.AttributeName;
import com.wci.umls.server.model.meta.CodeVariantType;
import com.wci.umls.server.model.meta.GeneralMetadataEntry;
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
import com.wci.umls.server.services.RootService;
import com.wci.umls.server.services.helpers.PushBackReader;

/**
 * Implementation of an algorithm to import RF2 snapshot data.
 */
public class RrfLoaderAlgorithm extends AbstractTerminologyLoaderAlgorithm {

  /** The prefix. */
  private String prefix = "MR";

  /** An SAB used in a Metathesaurus that really should belong to getTerminology() */
  /** e.g. MTH is really UMLS */
  // TODO: should be configurable
  private String proxySab = "MTH";

  /** The single mode. */
  private boolean singleMode = false;

  /** The codes flag. */
  private boolean codesFlag = true;

  /** The release version date. */
  private Date releaseVersionDate;

  /** The readers. */
  private RrfReaders readers;

  /** The loader. */
  private final String loader = "loader";

  /** The published. */
  private final String published = "PUBLISHED";

  /** The loaded terminologies. */
  private Map<String, Terminology> loadedTerminologies = new HashMap<>();

  /** The loaded root terminologies. */
  private Map<String, RootTerminology> loadedRootTerminologies =
      new HashMap<>();

  /** The loaded languages. */
  private Map<String, Language> loadedLanguages = new HashMap<>();

  /** The loaded term types. */
  private Map<String, TermType> loadedTermTypes = new HashMap<>();

  /** The term id type map. */
  private Map<String, IdType> termIdTypeMap = new HashMap<>();

  /** The code map. */
  private Map<String, Long> codeIdMap = new HashMap<>();

  /** The concept map. */
  private Map<String, Long> conceptIdMap = new HashMap<>(10000);

  /** The descriptor map. */
  private Map<String, Long> descriptorIdMap = new HashMap<>(10000);

  /** The atom map. */
  private Map<String, Long> atomIdMap = new HashMap<>(10000);

  /** The atom concept id map. */
  private Map<String, String> atomConceptIdMap = new HashMap<>(10000);

  /** The atom terminology map. */
  private Map<String, String> atomTerminologyMap = new HashMap<>(10000);

  /** The atom code id map. */
  private Map<String, String> atomCodeIdMap = new HashMap<>(10000);

  /** The atom descriptor id map. */
  private Map<String, String> atomDescriptorIdMap = new HashMap<>(10000);

  /** The relationship map. */
  private Map<String, Long> relationshipMap = new HashMap<>(10000);

  /** The cui aui atom subset map. */
  private Map<String, AtomSubset> cuiAuiAtomSubsetMap = new HashMap<>();

  /** The cui auiconcept subset map. */
  private Map<String, ConceptSubset> cuiAuiConceptSubsetMap = new HashMap<>();

  /** The id atom subset map. */
  private Map<String, AtomSubset> idTerminologyAtomSubsetMap = new HashMap<>();

  /** The id auiconcept subset map. */
  private Map<String, ConceptSubset> idTerminologyConceptSubsetMap =
      new HashMap<>();

  /** The list. */
  private PrecedenceList list;

  /** non-core modules map. */
  private Map<String, Set<Long>> moduleConceptIdMap = new HashMap<>();

  /** The lat code map. */
  private static Map<String, String> latCodeMap = new HashMap<>();

  /** The map set map. */
  private Map<String, MapSet> mapSetMap = new HashMap<>();

  static {

    // from http://www.nationsonline.org/oneworld/country_code_list.htm
    latCodeMap.put("BAQ", "eu");
    latCodeMap.put("CZE", "cz");
    latCodeMap.put("DAN", "dk");
    latCodeMap.put("DUT", "nl");
    latCodeMap.put("ENG", "en");
    latCodeMap.put("FIN", "fi");
    latCodeMap.put("FRE", "fr");
    latCodeMap.put("GER", "de");
    latCodeMap.put("HEB", "he");
    latCodeMap.put("HUN", "hu");
    latCodeMap.put("ITA", "it");
    latCodeMap.put("JPN", "ja");
    latCodeMap.put("KOR", "ko");
    latCodeMap.put("LAV", "lv");
    latCodeMap.put("NOR", "nn");
    latCodeMap.put("POL", "pl");
    latCodeMap.put("POR", "pt");
    latCodeMap.put("RUS", "ru");
    latCodeMap.put("SCR", "sc");
    latCodeMap.put("SPA", "es");
    latCodeMap.put("SWE", "sv");
    latCodeMap.put("CHI", "zh");
    latCodeMap.put("TUR", "tr");
    latCodeMap.put("EST", "et");
    latCodeMap.put("GRE", "el");
  }

  /** The tree pos algorithm. */
  private TreePositionAlgorithm treePosAlgorithm = null;

  /** The trans closure algorithm. */
  private TransitiveClosureAlgorithm transClosureAlgorithm = null;

  /** The label set algorithm. */
  private LabelSetMarkedParentAlgorithm labelSetAlgorithm = null;

  /**
   * Instantiates an empty {@link RrfLoaderAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public RrfLoaderAlgorithm() throws Exception {
    super();
  }

  /**
   * Sets the single mode.
   *
   * @param singleMode the single mode
   */
  public void setSingleMode(boolean singleMode) {
    this.singleMode = singleMode;
  }

  /**
   * Sets the codes flag.
   *
   * @param codesFlag the codes flag
   */
  public void setCodesFlag(boolean codesFlag) {
    this.codesFlag = codesFlag;
  }

  /**
   * Sets the prefix.
   *
   * @param prefix the prefix
   */
  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  @Override
  public String getFileVersion() throws Exception {
    return new RrfFileSorter().getFileVersion(new File(getInputPath()));
  }

  /**
   * Compute.
   *
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public void compute() throws Exception {

    logInfo("Start loading RRF");
    logInfo("  terminology = " + getTerminology());
    logInfo("  version = " + getVersion());
    logInfo("  single mode = " + singleMode);
    logInfo("  inputDir = " + getInputPath());

    // Track system level information
    long startTimeOrig = System.nanoTime();
    // control transaction scope
    setTransactionPerOperation(false);
    // Turn of ID computation when loading a terminology
    setAssignIdentifiersFlag(false);
    // Let loader set last modified flags.
    setLastModifiedFlag(false);

    // Check the input directory
    File inputDirFile = new File(getInputPath());
    if (!inputDirFile.exists()) {
      throw new Exception("Specified input directory does not exist");
    }

    // Sort files - not really needed because files are already sorted
    Logger.getLogger(getClass()).info("  Sort RRF Files");
    final RrfFileSorter sorter = new RrfFileSorter();
    // Be flexible about missing files for RXNORM
    sorter.setRequireAllFiles(!(prefix == null ? "MR" : prefix).equals("RXN"));
    // File outputDir = new File(inputDirFile, "/RRF-sorted-temp/");
    // sorter.sortFiles(inputDirFile, outputDir);
    setReleaseVersion(sorter.getFileVersion(inputDirFile));
    if (getReleaseVersion() == null) {
      setReleaseVersion(getVersion());
    }
    releaseVersionDate =
        ConfigUtility.DATE_FORMAT.parse(getReleaseVersion().substring(0, 4)
            + "0101");
    Logger.getLogger(getClass()).info(
        "  releaseVersion = " + getReleaseVersion());

    // Open readers - just open original RRF, no need to sort
    readers = new RrfReaders(inputDirFile);
    // Use default prefix if not specified
    readers.openOriginalReaders(prefix == null ? "MR" : prefix);

    // faster performance.
    beginTransaction();

    //
    // Load the metadata
    //

    // Load semantic types
    if (!singleMode) {
      loadSrdef();
    }

    // Load MRDOC data
    loadMrdoc();

    // Load MRSAB data
    cacheExistingTerminologies();
    loadMrsab();

    // Load precedence info
    loadMrrank();

    // Commit
    commitClearBegin();

    // Load the content
    list = getDefaultPrecedenceList(getTerminology(), getVersion());
    loadMrconso();

    // Definitions
    loadMrdef();

    // Semantic Types (skip in single mode)
    if (!singleMode) {
      loadMrsty();
    }

    // Relationships
    loadMrrel();

    // Attributes
    loadMrsat();

    // Mappings - only for non-single mode
    if (!singleMode) {
      loadMrmap();
    }

    // Need to reset MRSAT reader
    readers.closeReaders();
    readers.openOriginalReaders(prefix);

    // Subsets/members
    loadMrsatSubsets();

    // Make subsets and label sets
    loadExtensionLabelSets();

    // Commit
    commitClearBegin();

    // Add release info for individual terminology
    for (final Terminology terminology : getTerminologyLatestVersions()
        .getObjects()) {
      final String version = terminology.getVersion();
      ReleaseInfo info = getReleaseInfo(terminology.getTerminology(), version);
      if (info == null) {
        info = new ReleaseInfoJpa();
        info.setName(version);
        info.setDescription(terminology.getTerminology() + " " + version
            + " release");
        info.setPlanned(false);
        info.setPublished(true);
        info.setReleaseBeginDate(null);
        info.setReleaseFinishDate(releaseVersionDate);
        info.setTerminology(terminology.getTerminology());
        info.setVersion(version);
        info.setLastModified(releaseVersionDate);
        info.setLastModifiedBy(loader);
        addReleaseInfo(info);
      }
    }

    //
    // Create ReleaseInfo for this release if it does not already exist
    //
    ReleaseInfo info = getReleaseInfo(getTerminology(), getReleaseVersion());
    if (info == null) {
      info = new ReleaseInfoJpa();
      info.setName(getReleaseVersion());
      info.setDescription(getTerminology() + " " + getReleaseVersion()
          + " release");
      info.setPlanned(false);
      info.setPublished(true);
      info.setReleaseBeginDate(null);
      info.setReleaseFinishDate(releaseVersionDate);
      info.setTerminology(getTerminology());
      info.setVersion(getVersion());
      info.setLastModified(releaseVersionDate);
      info.setLastModifiedBy(loader);
      addReleaseInfo(info);
    }

    // Clear concept cache

    logInfo("Log component stats");
    final Map<String, Integer> stats = getComponentStats(null, null, null);
    final List<String> statsList = new ArrayList<>(stats.keySet());
    Collections.sort(statsList);
    for (final String key : statsList) {
      logInfo("  " + key + " = " + stats.get(key));
    }
    // Final logging messages
    logInfo("      elapsed time = " + getTotalElapsedTimeStr(startTimeOrig));
    logInfo("Done ...");

    // clear and commit
    commit();
    clear();

    // Clean-up

    ConfigUtility.deleteDirectory(new File(inputDirFile, "/RRF-sorted-temp/"));

    // Final logging messages
    Logger.getLogger(getClass()).info(
        "      elapsed time = " + getTotalElapsedTimeStr(startTimeOrig));
    Logger.getLogger(getClass()).info("done ...");

  }

  /**
   * Load SRDEF. This is responsible for loading {@link SemanticType} metadata.
   *
   * @throws Exception the exception
   */
  private void loadSrdef() throws Exception {
    logInfo("  Load Semantic types");
    String line = null;
    int objectCt = 0;
    PushBackReader reader = readers.getReader(RrfReaders.Keys.SRDEF);
    final String[] fields = new String[10];
    String structuralChemicalTreeNumber = "";
    String functionalChemicalTreeNumber = "";
    while ((line = reader.readLine()) != null) {

      line = line.replace("\r", "");
      FieldedStringTokenizer.split(line, "|", 10, fields);

      if (fields[0].equals("STY")) {

        // Field Description
        // 0 RT: Record Type (STY = Semantic Type or RL = Relation).
        // 1 UI: Unique Identifier of the Semantic Type or Relation.
        // 2 STY/RL: Name of the Semantic Type or Relation.
        // 3 STN/RTN: Tree Number of the Semantic Type or Relation.
        // 4 DEF: Definition of the Semantic Type or Relation.
        // 5 EX: Examples of Metathesaurus concepts with this Semantic Type (STY
        // records only).
        // 6 UN: Usage note for Semantic Type assignment (STY records only).
        // 7 NH: The Semantic Type and its descendants allow the non-human flag
        // (STY records only).
        // 8 ABR: Abbreviation of the Relation Name or Semantic Type.
        // 9 RIN: Inverse of the Relation (RL records only).
        //
        // e.g.
        // STY|T001|Organism|A1.1|Generally, a living individual, including all
        // plants and animals.||NULL||orgm||

        final SemanticType sty = new SemanticTypeJpa();
        sty.setAbbreviation(fields[8]);
        sty.setDefinition(fields[4]);
        sty.setExample(fields[5]);
        sty.setExpandedForm(fields[2]);
        sty.setNonHuman(fields[7].equals("Y"));
        sty.setTerminology(getTerminology());
        sty.setVersion(getVersion());
        sty.setTreeNumber(fields[3]);
        sty.setTypeId(fields[1]);
        sty.setUsageNote(fields[6]);
        sty.setValue(fields[2]);
        
        if (fields[2].equals("Chemical Viewed Structurally"))
          structuralChemicalTreeNumber = fields[3];
        if (fields[2].equals("Chemical Viewed Functionally"))
          functionalChemicalTreeNumber = fields[3];        

        sty.setTimestamp(releaseVersionDate);
        sty.setLastModified(releaseVersionDate);
        sty.setLastModifiedBy(loader);
        sty.setPublished(true);
        sty.setPublishable(true);
        Logger.getLogger(getClass()).debug("    add semantic type - " + sty);
        addSemanticType(sty);

        logAndCommit(++objectCt, RootService.logCt, RootService.commitCt);
      }
    }
    commitClearBegin();
    
    SemanticTypeList structuralDescendants = 
        getSemanticTypeDescendants(getTerminology(), getVersion(), structuralChemicalTreeNumber, true);
    for (SemanticType sty : structuralDescendants.getObjects()) {
      sty.setStructuralChemical(true);
      updateSemanticType(sty);
    }
    SemanticTypeList functionalDescendants = 
        getSemanticTypeDescendants(getTerminology(), getVersion(), functionalChemicalTreeNumber, true);
    for (SemanticType sty : functionalDescendants.getObjects()) {
      sty.setFunctionalChemical(true);
      updateSemanticType(sty);
    }

    commitClearBegin();
  }

  /**
   * Load MRDOC. This is responsible for loading much of the metadata.
   *
   * @throws Exception the exception
   */
  private void loadMrdoc() throws Exception {
    logInfo("  Load MRDOC abbreviation types");
    String line = null;
    Set<String> atnSeen = new HashSet<>();
    final Map<String, RelationshipType> relMap = new HashMap<>();
    final Map<String, String> inverseRelMap = new HashMap<>();
    final Map<String, AdditionalRelationshipType> relaMap = new HashMap<>();
    final Map<String, String> inverseRelaMap = new HashMap<>();
    final Map<String, TermType> ttyMap = new HashMap<>();
    final PushBackReader reader = readers.getReader(RrfReaders.Keys.MRDOC);
    int objectCt = 0;
    final String fields[] = new String[4];
    while ((line = reader.readLine()) != null) {
      line = line.replace("\r", "");
      FieldedStringTokenizer.split(line, "|", 4, fields);

      // Field Description DOCKEY,VALUE,TYPE,EXPL
      // 0 DOCKEY
      // 1 VALUE
      // 2 TYPE
      // 3 EXPL

      // e.g.
      // ATN|ACCEPTABILITYID|expanded_form|Acceptability Id|

      // Handle AttributeNames
      if ((fields[0].equals("ATN") || fields[0].equals("MAPATN"))
          && fields[2].equals("expanded_form") && !atnSeen.contains(fields[1])) {
        final AttributeName atn = new AttributeNameJpa();
        atn.setAbbreviation(fields[1]);
        atn.setExpandedForm(fields[3]);
        atn.setTimestamp(releaseVersionDate);
        atn.setLastModified(releaseVersionDate);
        atn.setLastModifiedBy(loader);
        atn.setTerminology(getTerminology());
        atn.setVersion(getVersion());
        atn.setPublished(true);
        atn.setPublishable(true);
        Logger.getLogger(getClass()).debug("    add attribute name - " + atn);
        addAttributeName(atn);
        atnSeen.add(fields[1]);
      }

      // Handle Languages
      else if (fields[0].equals("LAT") && fields[2].equals("expanded_form")) {
        final Language lat = new LanguageJpa();
        lat.setAbbreviation(fields[1]);
        lat.setExpandedForm(fields[3]);
        lat.setTimestamp(releaseVersionDate);
        lat.setLastModified(releaseVersionDate);
        lat.setLastModifiedBy(loader);
        lat.setTerminology(getTerminology());
        lat.setVersion(getVersion());
        lat.setPublished(true);
        lat.setPublishable(true);
        lat.setISO3Code(fields[1]);
        if (latCodeMap.containsKey(fields[1])) {
          lat.setISOCode(latCodeMap.get(fields[1]));
        } else {
          throw new Exception("Language map does not have 2 letter code for "
              + fields[1]);
        }
        Logger.getLogger(getClass()).debug("    add language - " + lat);
        addLanguage(lat);
        loadedLanguages.put(lat.getAbbreviation(), lat);
      }

      // Handle AdditionalRelationshipLabel
      else if (fields[0].equals("RELA") && fields[2].equals("expanded_form")) {
        final AdditionalRelationshipType rela =
            new AdditionalRelationshipTypeJpa();
        rela.setAbbreviation(fields[1]);
        rela.setExpandedForm(fields[3]);
        rela.setTimestamp(releaseVersionDate);
        rela.setLastModified(releaseVersionDate);
        rela.setLastModifiedBy(loader);
        rela.setTerminology(getTerminology());
        rela.setVersion(getVersion());
        rela.setPublished(true);
        rela.setPublishable(true);
        // DL fields are all left false, with no domain/range
        // no equivalent types or supertypes included
        relaMap.put(fields[1], rela);
        Logger.getLogger(getClass()).debug(
            "    add additional relationship type - " + rela);
      } else if (fields[0].equals("RELA") && fields[2].equals("rela_inverse")) {
        inverseRelaMap.put(fields[1], fields[3]);

        if (inverseRelaMap.containsKey(fields[1])
            && inverseRelaMap.containsKey(fields[3])) {
          AdditionalRelationshipType rela1 = relaMap.get(fields[1]);
          AdditionalRelationshipType rela2 = relaMap.get(fields[3]);
          rela1.setInverse(rela2);
          rela2.setInverse(rela1);
          addAdditionalRelationshipType(rela1);
          addAdditionalRelationshipType(rela2);
        }
      }

      // Handle RelationshipLabel
      else if (fields[0].equals("REL") && fields[2].equals("expanded_form")
          && !fields[1].equals("SIB")) {
        final RelationshipType rel = new RelationshipTypeJpa();
        rel.setAbbreviation(fields[1]);
        rel.setExpandedForm(fields[3]);
        rel.setTimestamp(releaseVersionDate);
        rel.setLastModified(releaseVersionDate);
        rel.setLastModifiedBy(loader);
        rel.setTerminology(getTerminology());
        rel.setVersion(getVersion());
        rel.setPublished(true);
        rel.setPublishable(true);
        relMap.put(fields[1], rel);
        Logger.getLogger(getClass())
            .debug("    add relationship type - " + rel);
      } else if (fields[0].equals("REL") && fields[2].equals("rel_inverse")
          && !fields[1].equals("SIB")) {
        inverseRelMap.put(fields[1], fields[3]);
        if (inverseRelMap.containsKey(fields[1])
            && inverseRelMap.containsKey(fields[3])) {
          RelationshipType rel1 = relMap.get(fields[1]);
          RelationshipType rel2 = relMap.get(fields[3]);
          rel1.setInverse(rel2);
          rel2.setInverse(rel1);
          addRelationshipType(rel1);
          addRelationshipType(rel2);
        }
      }

      else if (fields[0].equals("TTY") && fields[2].equals("expanded_form")) {
        final TermType tty = new TermTypeJpa();
        tty.setAbbreviation(fields[1]);
        tty.setExpandedForm(fields[3]);
        tty.setTimestamp(releaseVersionDate);
        tty.setLastModified(releaseVersionDate);
        tty.setLastModifiedBy(loader);
        tty.setTerminology(getTerminology());
        tty.setVersion(getVersion());
        tty.setPublished(true);
        tty.setPublishable(true);
        tty.setCodeVariantType(CodeVariantType.UNDEFINED);
        // based on TTY class (set later)
        tty.setHierarchicalType(false);
        tty.setNameVariantType(NameVariantType.UNDEFINED);
        tty.setSuppressible(false);
        tty.setStyle(TermTypeStyle.UNDEFINED);
        tty.setUsageType(UsageType.UNDEFINED);
        ttyMap.put(fields[1], tty);
      } else if (fields[0].equals("TTY") && fields[2].equals("tty_class")) {
        if (fields[3].equals("attribute")) {
          ttyMap.get(fields[1]).setCodeVariantType(CodeVariantType.ATTRIBUTE);
        }
        if (fields[3].equals("abbreviation")) {
          ttyMap.get(fields[1]).setNameVariantType(NameVariantType.AB);
          ttyMap.get(fields[1]).setCodeVariantType(CodeVariantType.SY);
        }
        if (fields[3].equals("synonym")) {
          ttyMap.get(fields[1]).setCodeVariantType(CodeVariantType.SY);
        }
        if (fields[3].equals("preferred")) {
          if (ttyMap.get(fields[1]).getCodeVariantType() == CodeVariantType.ET) {
            ttyMap.get(fields[1]).setCodeVariantType(CodeVariantType.PET);
          } else {
            ttyMap.get(fields[1]).setCodeVariantType(CodeVariantType.PN);
          }
        }
        if (fields[3].equals("entry_term")) {
          if (ttyMap.get(fields[1]).getCodeVariantType() == CodeVariantType.PN) {
            ttyMap.get(fields[1]).setCodeVariantType(CodeVariantType.PET);
          } else {
            ttyMap.get(fields[1]).setCodeVariantType(CodeVariantType.ET);
          }
        }
        if (fields[3].equals("hierarchical")) {
          ttyMap.get(fields[1]).setHierarchicalType(true);
        }
        if (fields[3].equals("obsolete")) {
          ttyMap.get(fields[1]).setObsolete(true);
        }
        if (fields[3].equals("expanded")) {
          ttyMap.get(fields[1]).setNameVariantType(NameVariantType.EXPANDED);
        }

      }

      // General metadata entries (skip MAPATN)
      else if (!fields[0].equals("MAPATN")) {
        final GeneralMetadataEntry entry = new GeneralMetadataEntryJpa();

        entry.setTimestamp(releaseVersionDate);
        entry.setLastModified(releaseVersionDate);
        entry.setLastModifiedBy(loader);
        entry.setTerminology(getTerminology());
        entry.setVersion(getVersion());
        entry.setPublished(true);
        entry.setPublishable(true);

        entry.setKey(fields[0]);
        entry.setAbbreviation(fields[1]);
        entry.setType(fields[2]);
        entry.setExpandedForm(fields[3]);

        addGeneralMetadataEntry(entry);
      }

      logAndCommit(++objectCt, RootService.logCt, RootService.commitCt);
    }

    // Add TTYs when done
    for (final TermType tty : ttyMap.values()) {
      addTermType(tty);
      loadedTermTypes.put(tty.getAbbreviation(), tty);
    }

    commitClearBegin();
  }

  /**
   * Cache existing terminologies.
   *
   * @throws Exception the exception
   */
  private void cacheExistingTerminologies() throws Exception {

    for (final RootTerminology root : getRootTerminologies().getObjects()) {
      // lazy init
      root.getSynonymousNames().size();
      loadedRootTerminologies.put(root.getTerminology(), root);
    }
    for (final Terminology term : getTerminologies().getObjects()) {
      // lazy init
      term.getSynonymousNames().size();
      term.getRootTerminology().getTerminology();
      loadedTerminologies.put(term.getTerminology(), term);
    }

  }

  /**
   * Load MRSAB. This is responsible for loading {@link Terminology} and
   * {@link RootTerminology} info.
   *
   * @throws Exception the exception
   */
  private void loadMrsab() throws Exception {
    logInfo("  Load MRSAB data");
    String line = null;
    final PushBackReader reader = readers.getReader(RrfReaders.Keys.MRSAB);
    final String fields[] = new String[25];
    while ((line = reader.readLine()) != null) {

      line = line.replace("\r", "");
      FieldedStringTokenizer.split(line, "|", 25, fields);

      // Skip non-matching in single mode
      if (singleMode && !fields[3].equals(getTerminology())) {
        continue;
      }

      // Field Description
      // 0 VCUI
      // 1 RCUI
      // 2 VSAB
      // 3 RSAB
      // 4 SON
      // 5 SF
      // 6 SVER
      // 7 VSTART
      // 8 VEND
      // 9 IMETA
      // 10 RMETA
      // 11 SLC
      // 12 SCC
      // 13 SRL
      // 14 TFR
      // 15 CFR
      // 16 CXTY
      // 17 TTYL
      // 18 ATNL
      // 19 LAT
      // 20 CENC
      // 21 CURVER
      // 22 SABIN
      // 23 SSN
      // 24 SCIT
      //
      // e.g.
      // C3847853|C1140284|RXNORM_14AA_140902F|RXNORM|RxNorm Vocabulary,
      // 14AA_140902F|RXNORM|14AA_140902F|||2014AB||John Kilbourne, M.D. ;Head,
      // MeSH Section;National Library of Medicine;6701 Democracy Blvd.;Suite
      // 202 MSC 4879;Bethesda;Maryland;United
      // States;20892-4879;kilbourj@mail.nlm.nih.gov|John Kilbourne, M.D.;Head,
      // MeSH Section;National Library of Medicine;6701 Democracy Blvd.;Suite
      // 202 MSC 4879;Bethesda;Maryland;United
      // States;20892-4879;kilbourj@mail.nlm.nih.gov|0|1969|278||BN,BPCK,DF,GPCK,IN,MIN,OCD,PIN,PSN,SBD,SBDC,SBDF,SCD,SCDC,SCDF,SCDG,SY,TMSY|AMBIGUITY_FLAG,NDC,ORIG_AMBIGUITY_FLAG,ORIG_CODE,ORIG_SOURCE,ORIG_TTY,ORIG_VSAB,RXAUI,RXCUI,RXN_ACTIVATED,RXN_AVAILABLE_STRENGTH,RXN_BN_CARDINALITY,RXN_HUMAN_DRUG,RXN_OBSOLETED,RXN_QUANTITY,RXN_STRENGTH,RXTERM_FORM|ENG|UTF-8|Y|Y|RXNORM|RxNorm;META2014AA
      // Full Update 2014_09_02;Bethesda, MD;National Library of Medicine|

      // SKIP SABIN=N - may be an issue later for maps.
      if (fields[22].equals("N") && !fields[3].equals(proxySab)) {
        Logger.getLogger(getClass()).debug("  Skip terminology " + fields[2]);
        continue;
      }

      String termVersion = null;
      if (singleMode || fields[6].equals(""))
        termVersion = getVersion();
      else
        termVersion = fields[6];

      Terminology term = loadedTerminologies.get(fields[3]);
      if (term == null || !term.getVersion().equals(termVersion)) {
        term = new TerminologyJpa();

        term.setAssertsRelDirection(false);
        term.setCitation(new CitationJpa(fields[24]));
        term.setCurrent(fields[21].equals("Y"));
        if (!fields[8].equals("")) {
          term.setEndDate(ConfigUtility.DATE_FORMAT2.parse(fields[8]));
        }

        term.setOrganizingClassType(IdType.CODE);
        term.setPreferredName(fields[4]);
        if (!fields[7].equals("")) {
          term.setStartDate(ConfigUtility.DATE_FORMAT2.parse(fields[7]));
        }

        term.setTimestamp(releaseVersionDate);
        term.setLastModified(releaseVersionDate);
        term.setLastModifiedBy(loader);
        term.setTerminology(fields[3]);
        term.setVersion(termVersion);
        term.setDescriptionLogicTerminology(false);

        if (!loadedRootTerminologies.containsKey(fields[3])) {
          // Add if it does not yet exist
          final RootTerminology root = new RootTerminologyJpa();
          root.setAcquisitionContact(null); // no data for this in MRSAB
          root.setContentContact(new ContactInfoJpa(fields[12]));
          root.setFamily(fields[5]);
          root.setLicenseContact(new ContactInfoJpa(fields[11]));
          root.setPolyhierarchy(fields[16].contains("MULTIPLE"));
          root.setPreferredName(fields[4]);
          root.setRestrictionLevel(Integer.parseInt(fields[13]));
          root.setTerminology(fields[3]);
          root.setTimestamp(releaseVersionDate);
          root.setLastModified(releaseVersionDate);
          root.setLastModifiedBy(loader);
          addRootTerminology(root);
          loadedRootTerminologies.put(root.getTerminology(), root);
        }

        final RootTerminology root = loadedRootTerminologies.get(fields[3]);
        term.setRootTerminology(root);
        addTerminology(term);
        // cache terminology by RSAB and VSAB
        loadedTerminologies.put(term.getTerminology(), term);
        if (!fields[2].equals("")) {
          loadedTerminologies.put(fields[2], term);
        }
      }
    }

    // Add the terminology for this load, e.g. "UMLS"
    // Skip in single mode
    if (!singleMode && !loadedTerminologies.containsKey(getTerminology())) {
      final Terminology term = new TerminologyJpa();
      term.setAssertsRelDirection(false);
      term.setCurrent(true);
      term.setOrganizingClassType(IdType.CONCEPT);
      term.setPreferredName(getTerminology());
      term.setTimestamp(releaseVersionDate);
      term.setLastModified(releaseVersionDate);
      term.setLastModifiedBy(loader);
      term.setTerminology(getTerminology());
      term.setVersion(getVersion());
      term.setDescriptionLogicTerminology(false);
      term.setMetathesaurus(true);

      RootTerminology root = loadedRootTerminologies.get(getTerminology());
      if (!loadedRootTerminologies.containsKey(getTerminology())) {
        root = new RootTerminologyJpa();
        root.setFamily(getTerminology());
        root.setPreferredName(getTerminology());
        root.setRestrictionLevel(0);
        root.setTerminology(getTerminology());
        root.setTimestamp(releaseVersionDate);
        root.setLastModified(releaseVersionDate);
        root.setLastModifiedBy(loader);
        root.setLanguage("ENG");
        if (root.getLanguage() == null) {
          throw new Exception("Unable to find ENG langauge.");
        }
        addRootTerminology(root);
        loadedRootTerminologies.put(root.getTerminology(), root);
      }
      term.setRootTerminology(root);
      addTerminology(term);
      loadedTerminologies.put(term.getTerminology(), term);
    }
  }

  /**
   * Load MRRANK. This is responsible for loading the default
   * {@link PrecedenceList}s.
   *
   * @throws Exception the exception
   */
  private void loadMrrank() throws Exception {

    final PrecedenceList list = new PrecedenceListJpa();
    list.setDefaultList(true);
    list.setTerminology(getTerminology());
    list.setVersion(getVersion());

    final List<KeyValuePair> lkvp = new ArrayList<>();

    logInfo("  Load MRRANK data");
    String line = null;
    final PushBackReader reader = readers.getReader(RrfReaders.Keys.MRRANK);
    final String fields[] = new String[4];
    while ((line = reader.readLine()) != null) {

      line = line.replace("\r", "");
      FieldedStringTokenizer.split(line, "|", 4, fields);

      // FIELDS
      // 0 RNK
      // 1 SAB
      // 2 TTY
      // 3 SUPPRESS (ignore this)
      // e.g.
      // 0586|MTH|PN|N|

      // Skip entries for other terminologies
      if (singleMode && !fields[1].equals(getTerminology())) {
        continue;
      }

      final KeyValuePair pair = new KeyValuePair();
      pair.setKey(fields[1]);
      pair.setValue(fields[2]);
      lkvp.add(pair);

      // Set term-type suppress
      loadedTermTypes.get(fields[2]).setSuppressible(fields[3].equals("Y"));
    }

    final KeyValuePairList kvpl = new KeyValuePairList();
    kvpl.setKeyValuePairs(lkvp);
    list.setPrecedence(kvpl);
    list.setTimestamp(releaseVersionDate);
    list.setLastModified(releaseVersionDate);
    list.setLastModifiedBy(loader);
    list.setName("DEFAULT");
    addPrecedenceList(list);

  }

  /**
   * Load MRDEF. This is responsible for loading {@link Definition}s.
   *
   * @throws Exception the exception
   */
  private void loadMrdef() throws Exception {
    logInfo("  Load MRDEF data");
    String line = null;
    int objectCt = 0;
    final PushBackReader reader = readers.getReader(RrfReaders.Keys.MRDEF);
    // make set of all atoms that got an additional definition
    Set<Atom> modifiedAtoms = new HashSet<>();
    final String fields[] = new String[8];
    while ((line = reader.readLine()) != null) {

      line = line.replace("\r", "");
      FieldedStringTokenizer.split(line, "|", 8, fields);

      // Skip non-matching in single mode
      if (singleMode && !fields[4].equals(getTerminology())) {
        continue;
      }

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

      final Definition def = new DefinitionJpa();
      final Atom atom = getAtom(atomIdMap.get(fields[1]));

      atom.getDefinitions().add(def);
      modifiedAtoms.add(atom);

      def.setTimestamp(releaseVersionDate);
      def.setLastModified(releaseVersionDate);
      def.setLastModifiedBy(loader);
      def.setObsolete(fields[6].equals("O"));
      def.setSuppressible(!fields[6].equals("N"));
      def.setPublished(true);
      def.setPublishable(true);

      if (!singleMode) {
        def.putAlternateTerminologyId(getTerminology(), fields[2]);
      }
      def.setTerminologyId(fields[3]);

      def.setTerminology(fields[4].intern());
      if (loadedTerminologies.get(fields[4]) == null) {
        throw new Exception(
            "Definition references terminology that does not exist: "
                + fields[4]);
      } else {
        def.setVersion(loadedTerminologies.get(fields[4]).getVersion());
      }
      def.setValue(fields[5]);

      addDefinition(def, atom);
      // Whenever we are going to commit, update atoms too.
      if (++objectCt % commitCt == 0) {
        for (final Atom a : modifiedAtoms) {
          updateAtom(a);
        }
        modifiedAtoms.clear();
      }
      logAndCommit(objectCt, RootService.logCt, RootService.commitCt);

    }
    // make sure any remaining modified atoms are updated
    for (final Atom a : modifiedAtoms) {
      updateAtom(a);
    }
    modifiedAtoms.clear();
    commitClearBegin();
  }

  /**
   * Load MRSAT. This is responsible for loading {@link Attribute}s.
   *
   * @throws Exception the exception
   */
  private void loadMrsat() throws Exception {
    logInfo("  Load MRSAT data");
    String line = null;

    int objectCt = 0;
    final PushBackReader reader = readers.getReader(RrfReaders.Keys.MRSAT);
    // make set of all atoms that got an additional attribute
    final Set<Atom> modifiedAtoms = new HashSet<>();
    final Set<Relationship<? extends HasTerminologyId, ? extends HasTerminologyId>> modifiedRelationships =
        new HashSet<>();
    final Set<Code> modifiedCodes = new HashSet<>();
    final Set<Descriptor> modifiedDescriptors = new HashSet<>();
    final Set<Concept> modifiedConcepts = new HashSet<>();
    final String fields[] = new String[13];
    while ((line = reader.readLine()) != null) {
      line = line.replace("\r", "");
      FieldedStringTokenizer.split(line, "|", 13, fields);

      // Skip non-matching in single mode
      if (singleMode && !fields[9].equals(getTerminology())
          && !fields[9].equals("SAB")) {
        continue;
      }

      // Skip LT attributes entirely
      // There are issues with the SAB of the atom and the SAB of the LT
      // attribute that need resolving
      if (fields[8].equals("LT")) {
        continue;
      }

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
      final Attribute att = new AttributeJpa();

      att.setTimestamp(releaseVersionDate);
      att.setLastModified(releaseVersionDate);
      att.setLastModifiedBy(loader);
      att.setObsolete(fields[11].equals("O"));
      att.setSuppressible(!fields[11].equals("N"));
      att.setPublished(true);
      att.setPublishable(true);
      // fields[5] CODE not used - redundant
      if (!singleMode) {
        att.putAlternateTerminologyId(getTerminology(), fields[6]);
      }
      att.setTerminologyId(fields[7]);
      att.setTerminology(fields[9].intern());
      if (loadedTerminologies.get(fields[9]) == null) {
        throw new Exception(
            "Attribute references terminology that does not exist: "
                + fields[9]);
      } else {
        att.setVersion(loadedTerminologies.get(fields[9]).getVersion());
      }
      att.setName(fields[8]);
      att.setValue(fields[10]);

      // Skip CV_MEMBER attributes for now
      if (fields[8].equals("CV_MEMBER")) {
        continue;
      }

      // Handle subset members and subset member attributes later
      else if (fields[8].equals("SUBSET_MEMBER")) {
        // Subset members are handled in loadMrsatSubsets()
        continue;

      } else if (fields[4].equals("AUI")) {
        // Get the concept for the AUI
        Atom atom = getAtom(atomIdMap.get(fields[3]));
        atom.getAttributes().add(att);
        addAttribute(att, atom);
      }
      // Special case of a CODE attribute where the AUI has "NOCODE" as the code
      // UMLS has one case of an early XM atom with NOCODE (ICD9CM to CCS map)
      // In loadMrconso we skip NOCODE codes, never creating them as Code
      // objects.
      else if (codesFlag && fields[4].equals("CODE")
          && atomCodeIdMap.get(fields[3]).equals("NOCODE")) {
        // Get the concept for the AUI
        final Atom atom = getAtom(atomIdMap.get(fields[3]));
        atom.getAttributes().add(att);
        addAttribute(att, atom);
      } else if (fields[4].equals("RUI")) {
        // Get the relationship for the RUI
        final Relationship<? extends HasTerminologyId, ? extends HasTerminologyId> relationship =
            getRelationship(relationshipMap.get(fields[3]), null);
        relationship.getAttributes().add(att);
        addAttribute(att, relationship);
      } else if (codesFlag && fields[4].equals("CODE")) {
        final Long codeId =
            codeIdMap.get(atomTerminologyMap.get(fields[3])
                + atomCodeIdMap.get(fields[3]));
        if (codeId == null) {
          // Referential integrity error
          logError("line = " + line);
          Logger.getLogger(getClass()).error(
              "Referential integrity issue with field 3: " + fields[3]);
        } else {
          // Get the code for the terminology and CODE of the AUI
          final Code code = getCode(codeId);
          code.getAttributes().add(att);
          addAttribute(att, code);
        }
      } else if (fields[4].equals("CUI")) {
        // Get the concept for the terminology and CUI
        att.setTerminology(getTerminology());
        att.setVersion(getVersion());
        final Concept concept =
            getConcept(conceptIdMap.get(getTerminology() + fields[0]));
        concept.getAttributes().add(att);
        addAttribute(att, concept);
      } else if (fields[4].equals("SCUI")) {
        // Get the concept for the terminology and SCUI of the AUI
        final Long conceptId =
            conceptIdMap.get(atomTerminologyMap.get(fields[3])
                + atomConceptIdMap.get(fields[3]));
        if (conceptId == null) {
          // Referential integrity error
          logError("line = " + line);
          Logger.getLogger(getClass()).error(
              "Referential integrity issue with field 3: " + fields[3]);

        } else {
          final Concept concept =
              getConcept(conceptIdMap.get(atomTerminologyMap.get(fields[3])
                  + atomConceptIdMap.get(fields[3])));
          concept.getAttributes().add(att);
          addAttribute(att, concept);
        }
      } else if (fields[4].equals("SDUI")) {
        final Long descriptorId =
            descriptorIdMap.get(atomTerminologyMap.get(fields[3])
                + atomDescriptorIdMap.get(fields[3]));
        if (descriptorId == null) {
          // Referential integrity error
          logError("line = " + line);
          Logger.getLogger(getClass()).error(
              "Referential integrity issue with field 3: " + fields[3]);

        } else {
          // Get the descriptor for the terminology and SDUI of the AUI
          final Descriptor descriptor = getDescriptor(descriptorId);
          descriptor.getAttributes().add(att);
          addAttribute(att, descriptor);
        }
      }

      // Avoid if in single mode
      if (!singleMode && isMapSetAttribute(fields[8])) {
        processMapSetAttribute(fields);
      }

      // Update objects before commit
      if (++objectCt % commitCt == 0) {
        // Update objects with new attributes
        for (final Concept c : modifiedConcepts) {
          updateConcept(c);
        }
        modifiedConcepts.clear();
        for (final Atom a : modifiedAtoms) {
          updateAtom(a);
        }
        modifiedAtoms.clear();
        for (final Relationship<? extends HasTerminologyId, ? extends HasTerminologyId> r : modifiedRelationships) {
          updateRelationship(r);
        }
        modifiedRelationships.clear();
        for (final Code code : modifiedCodes) {
          updateCode(code);
        }
        modifiedCodes.clear();
        for (final Descriptor d : modifiedDescriptors) {
          updateDescriptor(d);
        }
        modifiedDescriptors.clear();
      }

      // Handle SNOMED extension modules
      // If ATN=MODULE_ID and ATV=AUI
      // Look up the concept.getId() and save for later (tied to this module)
      if (fields[8].equals("MODULE_ID") && fields[4].equals("AUI")) {
        if (isExtensionModule(fields[10])) {
          // terminology + module concept id
          final String key = fields[9] + fields[10];
          Logger.getLogger(getClass()).debug(
              "  extension module = " + fields[10] + ", " + key);
          if (!moduleConceptIdMap.containsKey(key)) {
            moduleConceptIdMap.put(key, new HashSet<Long>());
          }
          Logger.getLogger(getClass()).debug(
              "    concept = " + atomConceptIdMap.get(fields[3]));
          moduleConceptIdMap.get(key).add(
              conceptIdMap.get(atomTerminologyMap.get(fields[3])
                  + atomConceptIdMap.get(fields[3])));
        }
      }

      // log and commit
      logAndCommit(objectCt, RootService.logCt, RootService.commitCt);

      //
      // NOTE: there are no subset attributes in RRF
      //

    } // end while loop

    // add all of the mapsets
    for (MapSet mapSet : mapSetMap.values()) {
      if (mapSet.getName() == null) {
        logWarn("Mapset has no name set: " + mapSet.toString());
        throw new LocalException("Mapsets must have a name set.");
      }
      if (mapSet.getFromTerminology() == null) {
        logWarn("Mapset has no from terminology set: " + mapSet.toString());
        throw new LocalException("Mapsets must have a from terminology set.");
      }
      if (mapSet.getToTerminology() == null) {
        logWarn("Mapset has no to terminology set: " + mapSet.toString());
        throw new LocalException("Mapsets must have a to terminology set.");
      }
      mapSet.setLastModifiedBy(loader);
      mapSet.setLastModified(releaseVersionDate);
      mapSet.setObsolete(false);
      mapSet.setSuppressible(false);
      mapSet.setPublished(true);
      mapSet.setPublishable(true);
      if (mapSet.getTerminology() == null) {
        mapSet.setTerminology(getTerminology());
      }
      if (mapSet.getVersion() == null) {
        mapSet.setVersion(getVersion());
      }
      if (mapSet.getTerminologyId() == null) {
        mapSet.setTerminologyId("");
      }
      if (mapSet.getTerminology() == null) {
        throw new LocalException("Mapsets has no terminology set.");
      }

      mapSet.setTimestamp(releaseVersionDate);

      // Add map set attributes
      // We have to wait until here before we know what the
      // terminology/version of the map set are
      for (final Attribute attribute : mapSet.getAttributes()) {
        attribute.setTerminology(mapSet.getTerminology());
        attribute.setVersion(mapSet.getVersion());
        addAttribute(attribute, mapSet);
      }

      addMapSet(mapSet);
    }

    // get final updates in
    for (final Concept c : modifiedConcepts) {
      updateConcept(c);
    }
    modifiedConcepts.clear();
    for (final Atom a : modifiedAtoms) {
      updateAtom(a);
    }
    modifiedAtoms.clear();
    for (final Relationship<? extends HasTerminologyId, ? extends HasTerminologyId> r : modifiedRelationships) {
      updateRelationship(r);
    }
    modifiedRelationships.clear();
    for (final Code code : modifiedCodes) {
      updateCode(code);
    }
    modifiedCodes.clear();
    for (final Descriptor d : modifiedDescriptors) {
      updateDescriptor(d);
    }
    modifiedDescriptors.clear();

    // commit
    commitClearBegin();

  }

  /**
   * Checks if is map set attribute.
   *
   * @param atn the atn
   * @return true, if is map set attribute
   */
  @SuppressWarnings("static-method")
  private boolean isMapSetAttribute(String atn) {
    if (atn.equals("MAPSETNAME") || atn.equals("MAPSETVERSION")
        || atn.equals("TOVSAB") || atn.equals("TORSAB")
        || atn.equals("FROMRSAB") || atn.equals("FROMVSAB")
        || atn.equals("MAPSETGRAMMAR") || atn.equals("MAPSETRSAB")
        || atn.equals("MAPSETTYPE") || atn.equals("MAPSETVSAB")
        || atn.equals("MTH_MAPFROMEXHAUSTIVE")
        || atn.equals("MTH_MAPTOEXHAUSTIVE")
        || atn.equals("MTH_MAPSETCOMPLEXITY")
        || atn.equals("MTH_MAPFROMCOMPLEXITY")
        || atn.equals("MTH_MAPTOCOMPLEXITY") || atn.equals("MAPSETXRTARGETID")
        || atn.equals("MAPSETSID")) {
      return true;
    }
    return false;
  }

  /**
   * Load mrmap.
   *
   * @throws Exception the exception
   */
  private void loadMrmap() throws Exception {
    logInfo("  Load MRMAP data");
    String line = null;

    int objectCt = 0;
    final PushBackReader reader = readers.getReader(RrfReaders.Keys.MRMAP);

    final String fields[] = new String[26];
    while ((line = reader.readLine()) != null) {

      line = line.replace("\r", "");
      FieldedStringTokenizer.split(line, "|", 26, fields);

      // Skip non-matching in single mode
      if (singleMode && !fields[1].equals(getTerminology())) {
        continue;
      }

      // Field Description
      // 0 MAPSETCUI
      // 1 MAPSETSAB
      // 2 MAPSUBSETID
      // 3 MAPRANK
      // 4 MAPID
      // 5 MAPSID
      // 6 FROMID
      // 7 FROMSID
      // 8 FROMEXPR
      // 9 FROMTYPE
      // 10 FROMRULE
      // 11 FROMRES
      // 12 REL
      // 13 RELA
      // 14 TOID
      // 15 TOSID
      // 16 TOEXPR
      // 17 TOTYPE
      // 18 TORULE
      // 19 TORES
      // 20 MAPRULE
      // 21 MAPRES
      // 22 MAPTYPE
      // 23 MAPATN
      // 24 MAPATV
      // 25 CVF
      //
      // e.g.
      // C1306694|MTH|||AT28307260||C0155860||C0155860|CUI|||SY||4084||<Pneumonia>
      // AND <Pseudomonas Infections>|BOOLEAN_EXPRESSION_STR|||||ATX||||
      // C1306694|MTH|||AT28307305||C0027498||C0027498|CUI|||SY||3707||<Nausea>
      // OR <Vomiting>|BOOLEAN_EXPRESSION_STR|||||ATX||||
      // C1306694|MTH|||AT28307536||C0796038||C0796038|CUI|||RU||2560||<Facies>|BOOLEAN_EXPRESSION_STR|||||ATX||||
      // C1306694|MTH|||AT28307551||C0795864||C0795864|CUI|||RU||1950||<Chromosome
      // Deletion>|BOOLEAN_EXPRESSION_STR|||||ATX||||
      // C1306694|MTH|||AT28308078||C0796279||C0796279|CUI|||RU||2112||<Cryptorchidism>|BOOLEAN_EXPRESSION_STR|||||ATX||||

      final Mapping mapping = new MappingJpa();

      // look up mapSet from MAPSETCUI
      MapSet mapSet = mapSetMap.get(fields[0]);
      mapping.setMapSet(mapSet);
      mapping.setGroup(fields[2]);
      mapping.setRank(fields[3]);
      mapping.setFromTerminologyId(fields[8]);
      mapping.setFromIdType(IdType.getIdType(fields[9]));
      mapping.setRelationshipType(fields[12]);
      mapping.setAdditionalRelationshipType(fields[13]);
      mapping.setToTerminologyId(fields[16]);
      mapping.setToIdType(IdType.getIdType(fields[17]));

      mapping.setRule(fields[20]);
      mapping.setAdvice(fields[21]);
      // AVOID using these
      // mapping.getAttributes().add(makeAttribute("MAPATN", fields[23]));
      // mapping.getAttributes().add(makeAttribute("MAPATV", fields[24]));

      mapping.setTimestamp(releaseVersionDate);
      mapping.setLastModified(releaseVersionDate);
      mapping.setLastModifiedBy(loader);
      // if MAPATN is "ACTIVE" with nothing -> inactive, with 1 -> active
      mapping.setObsolete(false);
      mapping.setSuppressible(false);
      if (fields[23].equals("ACTIVE")) {
        mapping.setObsolete(!fields[24].equals("1"));
        mapping.setSuppressible(!fields[24].equals("1"));
      }
      mapping.setPublished(true);
      mapping.setPublishable(true);
      // If really a metathesaurus mapping, use terminology/version
      if (fields[1].equals(proxySab)) {
        mapping.setTerminology(getTerminology());
        mapping.setVersion(getVersion());
      }
      // Otherwise use what is indicated
      else {
        mapping.setTerminology(fields[1]);
        mapping.setVersion(loadedTerminologies.get(fields[1]).getVersion());
      }
      mapping.setTerminologyId(fields[5]);

      // Make mapping attributes
      if (fields[4] != null && !fields[4].equals("")) {
        mapping.getAttributes().add(makeAttribute(mapping, "MAPID", fields[4]));
      }
      if (fields[5] != null && !fields[5].equals("")) {
        mapping.getAttributes().add(makeAttribute(mapping, "MAPSID", fields[5]));
      }
      if (fields[6] != null && !fields[6].equals("")) {
        mapping.getAttributes().add(makeAttribute(mapping, "FROMID", fields[6]));
      }
      if (fields[7] != null && !fields[7].equals("")) {
        mapping.getAttributes().add(makeAttribute(mapping, "FROMSID", fields[7]));
      }
      if (fields[10] != null && !fields[10].equals("")) {
        mapping.getAttributes().add(makeAttribute(mapping, "FROMRULE", fields[10]));
      }
      if (fields[11] != null && !fields[11].equals("")) {
        mapping.getAttributes().add(makeAttribute(mapping, "FROMRES", fields[11]));
      }
      if (fields[14] != null && !fields[14].equals("")) {
        mapping.getAttributes().add(makeAttribute(mapping, "TOID", fields[14]));
      }
      if (fields[15] != null && !fields[15].equals("")) {
        mapping.getAttributes().add(makeAttribute(mapping, "TOSID", fields[15]));
      }
      if (fields[18] != null && !fields[18].equals("")) {
        mapping.getAttributes().add(makeAttribute(mapping, "TORULE", fields[18]));
      }
      if (fields[19] != null && !fields[19].equals("")) {
        mapping.getAttributes().add(makeAttribute(mapping, "TORES", fields[19]));
      }

      // mapSet.addMapping(mapping);
      addMapping(mapping);

      logAndCommit(++objectCt, RootService.logCt, RootService.commitCt);
    }

    commitClearBegin();
  }

  /**
   * Make attribute.
   *
   * @param mapping the mapping
   * @param name the name
   * @param value the value
   * @return the attribute
   * @throws Exception the exception
   */
  private Attribute makeAttribute(Mapping mapping, String name, String value)
    throws Exception {
    Attribute att = new AttributeJpa();
    att.setName(name);
    att.setValue(value);
    att.setTimestamp(releaseVersionDate);
    att.setLastModified(releaseVersionDate);
    att.setLastModifiedBy(loader);
    att.setObsolete(false);
    att.setSuppressible(false);
    att.setPublished(true);
    att.setPublishable(true);
    att.setTerminology(mapping.getTerminology());
    att.setVersion(mapping.getVersion());
    att.setTerminologyId("");

    addAttribute(att, mapping);
    return att;
  }

  /**
   * Process map set attribute.
   *
   * @param fields the fields
   * @throws Exception the exception
   */
  private void processMapSetAttribute(String[] fields) throws Exception {
    final String cui = fields[0];
    final String atn = fields[8];
    final String atv = fields[10];
    final String satui = fields[7];
    MapSet mapSet;
    if (!mapSetMap.containsKey(cui)) {
      mapSet = new MapSetJpa();
      mapSetMap.put(cui, mapSet);
      // Set map set name to preferred name of the cui
      mapSet.setName(getConcept(conceptIdMap.get(getTerminology() + cui))
          .getName());
    }
    mapSet = mapSetMap.get(cui);
    if (atn.equals("MAPSETNAME")) {
      mapSet.setName(atv);
    } else if (atn.equals("MAPSETVERSION")) {
      // n/a - version is picked up from the SAB
      // mapSet.setMapVersion(atv);
    } else if (atn.equals("TOVSAB")) {
      if (mapSet.getToTerminology() != null) {
        String version = atv.substring(mapSet.getToTerminology().length());
        mapSet.setToVersion(version.startsWith("_") ? version.substring(1)
            : version);
      } else {
        mapSet.setToVersion(atv);
      }
    } else if (atn.equals("TORSAB")) {
      mapSet.setToTerminology(atv);
      if (mapSet.getToVersion() != null) {
        String version = mapSet.getToVersion().substring(atv.length());
        mapSet.setToVersion(version.startsWith("_") ? version.substring(1)
            : version);
      }
    } else if (atn.equals("FROMRSAB")) {
      if (atv.equals(proxySab)) {
        mapSet.setFromTerminology(getTerminology());
        mapSet.setFromVersion(getVersion());
      } else {
        mapSet.setFromTerminology(atv);
        if (mapSet.getFromVersion() != null) {
          String version = mapSet.getFromVersion().substring(atv.length());
          mapSet.setFromVersion(version.startsWith("_") ? version.substring(1)
              : version);
        }
      }

    } else if (atn.equals("FROMVSAB")) {
      if (atv.equals(proxySab)) {
        mapSet.setFromTerminology(getTerminology());
        mapSet.setFromVersion(getVersion());
      } else {
        if (mapSet.getFromTerminology() != null) {
          String version = atv.substring(mapSet.getFromTerminology().length());
          mapSet.setFromVersion(version.startsWith("_") ? version.substring(1)
              : version);
        } else {
          mapSet.setFromVersion(atv);
        }
      }
    } else if (atn.equals("MAPSETGRAMMAR")) {
      Attribute att = new AttributeJpa();
      att.setName("MAPSETGRAMMAR");
      att.setValue(atv);
      att.setTimestamp(releaseVersionDate);
      att.setLastModified(releaseVersionDate);
      att.setLastModifiedBy(loader);
      att.setObsolete(false);
      att.setSuppressible(false);
      att.setPublished(true);
      att.setPublishable(true);
      att.setTerminology(mapSet.getTerminology());
      att.setVersion(mapSet.getVersion());
      att.setTerminologyId(satui);
      mapSet.getAttributes().add(att);
    } else if (atn.equals("MAPSETXRTARGETID")) {
      Attribute att = new AttributeJpa();
      att.setName("MAPSETXRTARGETID");
      att.setValue(atv);
      att.setTimestamp(releaseVersionDate);
      att.setLastModified(releaseVersionDate);
      att.setLastModifiedBy(loader);
      att.setObsolete(false);
      att.setSuppressible(false);
      att.setPublished(true);
      att.setPublishable(true);
      att.setTerminology(mapSet.getTerminology());
      att.setVersion(mapSet.getVersion());
      att.setTerminologyId(satui);
      mapSet.getAttributes().add(att);
    } else if (atn.equals("MAPSETRSAB")) {
      // If really a metathesaurus mapping, use terminology/version
      if (atv.equals(proxySab)) {
        mapSet.setTerminology(getTerminology());
        mapSet.setVersion(getVersion());
      }
      // Otherwise, use what was given
      else {
        mapSet.setTerminology(atv);
        // In case MAPSETVSAB was set first, strip off the RSAB part and use the
        // rest as the version
        if (mapSet.getVersion() != null) {
          mapSet.setVersion(mapSet.getVersion().substring(atv.length()));
        }
      }

    } else if (atn.equals("MAPSETTYPE")) {
      mapSet.setType(atv);
    } else if (atn.equals("MAPSETVSAB")) {
      // If really a metathesaurus mapping, use terminology/version
      if (atv.equals(proxySab)) {
        mapSet.setTerminology(getTerminology());
        mapSet.setVersion(getVersion());
      }
      // Otherwise, use what was given if MAPSETRSAB already provided
      else {
        mapSet.setVersion(atv);
        // In case MAPSETRSAB was set first, strip off the RSAB part and use the
        // rest as the version
        if (mapSet.getTerminology() != null) {
          mapSet.setVersion(atv.substring(mapSet.getTerminology().length()));
        }
      }
    } else if (atn.equals("MTH_MAPFROMEXHAUSTIVE")) {
      mapSet.setFromExhaustive(atv);
    } else if (atn.equals("MTH_MAPTOEXHAUSTIVE")) {
      mapSet.setToExhaustive(atv);
    } else if (atn.equals("MTH_MAPSETCOMPLEXITY")) {
      mapSet.setComplexity(atv);
    } else if (atn.equals("MTH_MAPFROMCOMPLEXITY")) {
      mapSet.setFromComplexity(atv);
    } else if (atn.equals("MTH_MAPTOCOMPLEXITY")) {
      mapSet.setToComplexity(atv);
    } else if (atn.equals("MAPSETSID")) {
      mapSet.setTerminologyId(atv);
    }
  }

  /**
   * Load subset data from MRSAT. This is responsible for loading {@link Subset}
   * s and {@link SubsetMember}s.
   *
   * @throws Exception the exception
   */
  private void loadMrsatSubsets() throws Exception {
    logInfo("  Load MRSAT Subset data");
    String line = null;

    int objectCt = 0;
    final PushBackReader reader = readers.getReader(RrfReaders.Keys.MRSAT);
    final Map<String, SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset>> addedSubsetMembers =
        new HashMap<>();
    String prevMetaUi = null;
    final String fields[] = new String[13];
    final String atvFields[] = new String[3];
    while ((line = reader.readLine()) != null) {

      line = line.replace("\r", "");
      FieldedStringTokenizer.split(line, "|", 13, fields);

      // Skip non-matching in single mode
      if (singleMode && !fields[9].equals(getTerminology())
          && !fields[9].equals("SAB")) {
        continue;
      }

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

      // Increment the object counter when METAUI changes
      // this allows better tracking of changes to subset members (e.g. new
      // attributes)
      if (!fields[3].equals(prevMetaUi)) {
        // Ready to commit, clear the subset member cache
        if (objectCt % commitCt == 0) {
          addedSubsetMembers.clear();
        }
        logAndCommit(++objectCt, RootService.logCt, RootService.commitCt);
      }

      // Skip everything except SUBSET_MEMBER
      if (!fields[8].equals("SUBSET_MEMBER")) {
        continue;
      }

      // Handle subset members and subset member attributes
      else if (fields[8].equals("SUBSET_MEMBER")) {
        // Create subset member and any subset member attributes.
        // NOTE: subset member may already exist.
        // C3853348|L11739318|S14587084|A24131773|AUI|442311000124105|AT200797951|45bb6996-8734-5033-b069-302708da2761|SUBSET_MEMBER|SNOMEDCT_US|900000000000509007~ACCEPTABILITYID~900000000000548007|N||
        FieldedStringTokenizer.split(fields[10], "~", 3, atvFields);
        final String subsetIdKey = atvFields[0] + fields[9];
        final String subsetMemberIdKey = fields[7] + fields[9];
        SubsetMember<? extends ComponentHasAttributes, ? extends Subset> member =
            addedSubsetMembers.get(subsetMemberIdKey);

        // If not, create it
        if (member == null) {
          if (fields[4].equals("AUI")) {
            final AtomSubset atomSubset =
                idTerminologyAtomSubsetMap.get(subsetIdKey);

            // We now know subset type, insert it and remove the corresponding
            // opposite type
            if (idTerminologyConceptSubsetMap.containsKey(subsetIdKey)) {
              Logger.getLogger(getClass()).debug("  Add subset " + atomSubset);
              addSubset(atomSubset);
              idTerminologyConceptSubsetMap.remove(subsetIdKey);
            }

            final AtomSubsetMember atomMember = new AtomSubsetMemberJpa();
            Atom atom = getAtom(atomIdMap.get(fields[3]));
            atomMember.setMember(atom);
            atomMember.setSubset(atomSubset);
            member = atomMember;

          } else if (fields[4].equals("SCUI")) {

            final ConceptSubset conceptSubset =
                idTerminologyConceptSubsetMap.get(subsetIdKey);

            // We now know subset type, insert it and remove the corresponding
            // opposite type
            if (idTerminologyAtomSubsetMap.containsKey(subsetIdKey)) {
              Logger.getLogger(getClass()).debug(
                  "  Concept subset " + conceptSubset);
              addSubset(conceptSubset);
              idTerminologyAtomSubsetMap.remove(subsetIdKey);
            }

            final ConceptSubsetMember conceptMember =
                new ConceptSubsetMemberJpa();
            Concept concept =
                getConcept(conceptIdMap.get(atomTerminologyMap.get(fields[3])
                    + atomConceptIdMap.get(fields[3])));
            conceptMember.setMember(concept);
            conceptMember.setSubset(conceptSubset);
            member = conceptMember;

          } else {
            throw new Exception("Unexpected subset type for member: " + line);
          }

          // Populate common member fields
          member.setTerminologyId(fields[7]);
          member.setTerminology(fields[9].intern());
          member.setVersion(loadedTerminologies.get(fields[9]).getVersion());
          member.setTimestamp(releaseVersionDate);
          member.setLastModified(releaseVersionDate);
          member.setLastModifiedBy(loader);
          member.setObsolete(fields[11].equals("O"));
          member.setSuppressible(!fields[11].equals("N"));
          member.setPublishable(true);
          member.setPublished(true);
          Logger.getLogger(getClass()).debug("    Add member" + member);
          addSubsetMember(member);

          // Add to the cache - this will be cleared at the next commit.
          addedSubsetMembers.put(subsetMemberIdKey, member);
        }

        // handle subset member attributes
        if (atvFields.length > 1 && atvFields[1] != null) {
          if (atvFields[2] == null) {
            atvFields[2] = "";
          }
          // C3853348|L11739318|S14587084|A24131773|AUI|442311000124105|AT200797951|45bb6996-8734-5033-b069-302708da2761|SUBSET_MEMBER|SNOMEDCT_US|900000000000509007~ACCEPTABILITYID~900000000000548007|N||
          final Attribute memberAtt = new AttributeJpa();
          // No terminology id for the member attribute
          // borrow most other data
          memberAtt.setTerminologyId("");
          memberAtt.setTerminology(fields[9].intern());
          memberAtt.setVersion(loadedTerminologies.get(fields[9]).getVersion());
          memberAtt.setTimestamp(releaseVersionDate);
          memberAtt.setLastModified(releaseVersionDate);
          memberAtt.setLastModifiedBy(loader);
          memberAtt.setObsolete(fields[11].equals("O"));
          memberAtt.setSuppressible(!fields[11].equals("N"));
          memberAtt.setPublishable(true);
          memberAtt.setPublished(true);
          memberAtt.setName(atvFields[1]);
          memberAtt.setValue(atvFields[2]);
          Logger.getLogger(getClass()).debug(
              "        Add member attribute" + memberAtt);
          addAttribute(memberAtt, member);

          // This member is not yet committed, so no need for an
          // "updateSubsetMember" call.
          member.getAttributes().add(memberAtt);

        }

      }

      prevMetaUi = fields[3];
      //
      // NOTE: there are no subset attributes in RRF
      //

    }

    // commit
    commitClearBegin();
  }

  /**
   * Load extension label sets.
   *
   * @throws Exception the exception
   */
  private void loadExtensionLabelSets() throws Exception {

    // for each non core module, create a Subset object
    final List<ConceptSubset> subsets = new ArrayList<>();
    for (final String key : moduleConceptIdMap.keySet()) {
      logInfo("  Create subset for module = " + key);
      // bail if concept doesn't exist
      if (!conceptIdMap.containsKey(key)) {
        logWarn("    MISSING CONCEPT");
        continue;
      }
      final Concept concept = getConcept(conceptIdMap.get(key));
      final ConceptSubset subset = new ConceptSubsetJpa();
      subset.setName(concept.getName());
      subset.setDescription("Represents the members of module "
          + concept.getTerminologyId());
      subset.setDisjointSubset(false);
      subset.setLabelSubset(true);
      subset.setLastModified(releaseVersionDate);
      subset.setTimestamp(releaseVersionDate);
      subset.setLastModifiedBy(loader);
      subset.setObsolete(false);
      subset.setSuppressible(false);
      subset.setPublishable(false);
      subset.setPublished(false);
      subset.setTerminology(concept.getTerminology());
      subset.setTerminologyId(concept.getTerminologyId());
      subset.setVersion(concept.getVersion());
      addSubset(subset);
      subsets.add(subset);
      commitClearBegin();

      // Create members
      int objectCt = 0;
      logInfo("  Add subset members");
      for (final Long id : moduleConceptIdMap.get(key)) {
        final Concept memberConcept = getConcept(id);

        final ConceptSubsetMember member = new ConceptSubsetMemberJpa();
        member.setLastModified(releaseVersionDate);
        member.setTimestamp(releaseVersionDate);
        member.setLastModifiedBy(loader);
        member.setMember(memberConcept);
        member.setObsolete(false);
        member.setSuppressible(false);
        member.setPublishable(false);
        member.setPublishable(false);
        member.setTerminologyId("");
        member.setTerminology(concept.getTerminology());
        member.setVersion(concept.getVersion());
        member.setSubset(subset);
        addSubsetMember(member);
        logAndCommit(++objectCt, RootService.logCt, RootService.commitCt);
      }
    }
    commitClearBegin();
  }

  /**
   * Load MRREL.This is responsible for loading {@link Relationship}s.
   *
   * @throws Exception the exception
   */
  private void loadMrrel() throws Exception {
    logInfo("  Load MRREL data");
    String line = null;

    int objectCt = 0;
    final PushBackReader reader = readers.getReader(RrfReaders.Keys.MRREL);
    final String fields[] = new String[16];
    while ((line = reader.readLine()) != null) {

      line = line.replace("\r", "");
      FieldedStringTokenizer.split(line, "|", 16, fields);

      // Skip non-matching in single mode
      if (singleMode && !fields[10].equals(getTerminology())
          && !fields[10].equals("SAB")) {
        continue;
      }

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

      // No need to update things rels are connected to because setting "from"
      // handles this in the DB. This also means, all we really need is an empty
      // container for the object with the id set.

      // Skip SIB rels
      if (fields[3].equals("SIB")) {
        continue;
      }

      else if (fields[2].equals("AUI") && fields[6].equals("AUI")) {
        final AtomRelationship aRel = new AtomRelationshipJpa();

        final Atom fromAtom = getAtom(atomIdMap.get(fields[5]));
        aRel.setFrom(fromAtom);

        final Atom toAtom = getAtom(atomIdMap.get(fields[1]));
        aRel.setTo(toAtom);

        setRelationshipFields(fields, aRel);
        addRelationship(aRel);
        relationshipMap.put(fields[8], aRel.getId());

      } else if (fields[2].equals("CUI") && fields[6].equals("CUI")) {
        final ConceptRelationship conceptRel = new ConceptRelationshipJpa();

        if (fields[4].isEmpty() || fields[0].isEmpty()) {
          final Concept fromConcept =
              getConcept(conceptIdMap.get(atomTerminologyMap.get(fields[5])
                  + atomConceptIdMap.get(fields[5])));
          conceptRel.setFrom(fromConcept);

          final Concept toConcept =
              getConcept(conceptIdMap.get(atomTerminologyMap.get(fields[1])
                  + atomConceptIdMap.get(fields[1])));
          conceptRel.setTo(toConcept);

        } else {
          final Concept fromConcept =
              getConcept(conceptIdMap.get(getTerminology() + fields[4]));
          conceptRel.setFrom(fromConcept);

          final Concept toConcept =
              getConcept(conceptIdMap.get(getTerminology() + fields[0]));
          conceptRel.setTo(toConcept);
        }
        setRelationshipFields(fields, conceptRel);
        conceptRel.setTerminology(getTerminology());
        conceptRel.setVersion(getVersion());
        addRelationship(conceptRel);
        relationshipMap.put(fields[8], conceptRel.getId());

      } else if (fields[2].equals("SCUI") && fields[6].equals("SCUI")) {
        final ConceptRelationship conceptRel = new ConceptRelationshipJpa();

        final Long fromId =
            conceptIdMap.get(atomTerminologyMap.get(fields[5])
                + atomConceptIdMap.get(fields[5]));
        final Long toId =
            conceptIdMap.get(atomTerminologyMap.get(fields[1])
                + atomConceptIdMap.get(fields[1]));

        if (fromId == null || toId == null) {
          // Referential integrity error, we know this happens in RXNORM
          // because RXAUI 5430346 has a relationship with SCUI type
          // but the SCUI of this atom is null;
          logError("line = " + line);
          logError("Referential integrity issue with field 2 or 6: "
              + fields[1] + ", " + fields[5]);
        } else {
          conceptRel.setFrom(getConcept(fromId));
          conceptRel.setTo(getConcept(toId));
          setRelationshipFields(fields, conceptRel);
          addRelationship(conceptRel);
          relationshipMap.put(fields[8], conceptRel.getId());
        }
      } else if (fields[2].equals("SDUI") && fields[6].equals("SDUI")) {
        final DescriptorRelationship descriptorRel =
            new DescriptorRelationshipJpa();

        final Long fromId =
            descriptorIdMap.get(atomTerminologyMap.get(fields[5])
                + atomDescriptorIdMap.get(fields[5]));
        final Long toId =
            descriptorIdMap.get(atomTerminologyMap.get(fields[1])
                + atomDescriptorIdMap.get(fields[1]));

        if (fromId == null || toId == null) {
          // Referential integrity error
          logError("line = " + line);
          logError("Referential integrity issue with field 2 or 6: "
              + fields[1] + ", " + fields[5]);
        } else {
          descriptorRel.setFrom(getDescriptor(fromId));
          descriptorRel.setTo(getDescriptor(toId));
          setRelationshipFields(fields, descriptorRel);
          addRelationship(descriptorRel);
          relationshipMap.put(fields[8], descriptorRel.getId());
        }

      } else if (codesFlag && fields[2].equals("CODE")
          && fields[6].equals("CODE")) {
        final CodeRelationship codeRel = new CodeRelationshipJpa();

        final Long fromId =
            codeIdMap.get(atomTerminologyMap.get(fields[5])
                + atomCodeIdMap.get(fields[5]));
        final Long toId =
            codeIdMap.get(atomTerminologyMap.get(fields[1])
                + atomCodeIdMap.get(fields[1]));
        if (fromId == null || toId == null) {
          // Referential integrity error
          logError("line = " + line);
          logError("Referential integrity issue with field 2 or 6: "
              + fields[5] + ", " + fields[1]);
        } else {

          codeRel.setFrom(getCode(fromId));
          codeRel.setTo(getCode(toId));
          setRelationshipFields(fields, codeRel);
          addRelationship(codeRel);
          relationshipMap.put(fields[8], codeRel.getId());
        }
      } else {
        String stype1 = fields[2];
        String stype2 = fields[6];
        
        final ComponentInfoRelationship componentInfoRel =
            new ComponentInfoRelationshipJpa();

        ComponentInfo from = null;
        if (stype2.equals("CODE")) { 
          Long fromId = codeIdMap.get(atomTerminologyMap.get(fields[5])
              + atomCodeIdMap.get(fields[5]));
          Code code = getCode(fromId);
          from = new ComponentInfoJpa(code); 

        } else if (stype2.equals("SCUI")) {
          Long fromId = conceptIdMap.get(atomTerminologyMap.get(fields[5])
              + atomConceptIdMap.get(fields[5]));
          Concept concept = getConcept(fromId);
          from = new ComponentInfoJpa(concept); 

        } else if (stype2.equals("SDUI")) {
          Long fromId = descriptorIdMap.get(atomTerminologyMap.get(fields[5])
              + atomDescriptorIdMap.get(fields[5]));
          Descriptor descriptor = getDescriptor(fromId);
          from = new ComponentInfoJpa(descriptor);
          
        } else if (stype2.equals("AUI")) {
          Atom fromAtom = getAtom(atomIdMap.get(fields[5]));
          from = new ComponentInfoJpa(); 
          from.setTerminologyId(fromAtom.getTerminologyId()); 
          from.setType(IdType.ATOM);        
        }
        
        ComponentInfo to = null;
        if (stype1.equals("CODE")) { 
          Long toId = codeIdMap.get(atomTerminologyMap.get(fields[1])
              + atomCodeIdMap.get(fields[1]));
          Code code = getCode(toId);
          to = new ComponentInfoJpa(code); 

        } else if (stype1.equals("SCUI")) {
          Long toId = conceptIdMap.get(atomTerminologyMap.get(fields[1])
              + atomConceptIdMap.get(fields[1]));
          Concept concept = getConcept(toId);
          to = new ComponentInfoJpa(concept); 

        } else if (stype1.equals("SDUI")) {
          Long toId = descriptorIdMap.get(atomTerminologyMap.get(fields[1])
              + atomDescriptorIdMap.get(fields[1]));
          Descriptor descriptor = getDescriptor(toId);
          to = new ComponentInfoJpa(descriptor);
          
        } else if (stype1.equals("AUI")) {
          Atom toAtom = getAtom(atomIdMap.get(fields[1]));
          to = new ComponentInfoJpa(); 
          to.setTerminologyId(toAtom.getTerminologyId()); 
          to.setType(IdType.ATOM); 
        
        }
        if (from == null || to == null) {
          // Referential integrity error
          logError("line = " + line);
          logError("Referential integrity issue with field 2 or 6: "
              + fields[1] + ", " + fields[5]);
        } else {
          componentInfoRel.setFrom(from);
          componentInfoRel.setTo(to);
          setRelationshipFields(fields, componentInfoRel);
          addRelationship(componentInfoRel);
          relationshipMap.put(fields[8], componentInfoRel.getId());
        }

      }

      logAndCommit(++objectCt, RootService.logCt, RootService.commitCt);
    }

    // update terminologies after setting the rel directionality flag
    for (final Terminology terminology : loadedTerminologies.values()) {
      updateTerminology(terminology);
    }
  }

  /**
   * Sets the relationship fields.
   *
   * @param fields the fields
   * @param relationship the relationship
   * @throws Exception the exception
   */
  private void setRelationshipFields(
    final String[] fields,
    final Relationship<? extends HasTerminologyId, ? extends HasTerminologyId> relationship)
    throws Exception {
    relationship.setTimestamp(releaseVersionDate);
    relationship.setLastModified(releaseVersionDate);
    relationship.setLastModifiedBy(loader);
    relationship.setObsolete(fields[14].equals("O"));
    relationship.setSuppressible(!fields[14].equals("N"));
    relationship.setPublished(true);
    relationship.setPublishable(true);

    relationship.setRelationshipType(fields[3]);
    relationship.setHierarchical(fields[3].equals("CHD"));
    relationship.setAdditionalRelationshipType(fields[7]);

    if (!singleMode) {
      relationship.putAlternateTerminologyId(getTerminology(), fields[8]);
    }
    relationship.setTerminologyId(fields[9]);
    relationship.setTerminology(fields[10].intern());
    if (loadedTerminologies.get(fields[10]) == null) {
      throw new Exception(
          "Relationship references terminology that does not exist: "
              + fields[9]);
    } else {
      relationship.setVersion(loadedTerminologies.get(fields[10]).getVersion());
    }
    relationship.setAssertedDirection(fields[13].equals("Y"));
    if (fields[13].equals("Y")) {
      loadedTerminologies.get(fields[10]).setAssertsRelDirection(true);
    }

    // zero groups should be represented as blank values
    relationship.setGroup(fields[12] == "0" ? "" : fields[12]);

    // Since we don't know, have the rels count as "both"
    relationship.setInferred(true);
    relationship.setStated(true);

  }

  /**
   * Load MRSTY. This is responsible for loading {@link SemanticTypeComponent}s.
   *
   * @throws Exception the exception
   */
  private void loadMrsty() throws Exception {
    logInfo("  Load MRSTY data");
    String line = null;
    final PushBackReader reader = readers.getReader(RrfReaders.Keys.MRSTY);
    // make set of all concepts that got an additional sty
    int objectCt = 0;
    Set<Concept> modifiedConcepts = new HashSet<>();
    final String fields[] = new String[6];
    while ((line = reader.readLine()) != null) {

      line = line.replace("\r", "");
      FieldedStringTokenizer.split(line, "|", 6, fields);

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

      final SemanticTypeComponent sty = new SemanticTypeComponentJpa();
      final Concept concept =
          getConcept(conceptIdMap.get(getTerminology() + fields[0]));
      concept.getSemanticTypes().add(sty);
      modifiedConcepts.add(concept);

      sty.setTimestamp(releaseVersionDate);
      sty.setLastModified(releaseVersionDate);
      sty.setLastModifiedBy(loader);
      sty.setObsolete(false);
      sty.setSuppressible(false);
      sty.setPublished(true);
      sty.setPublishable(true);

      sty.setSemanticType(fields[3]);
      // fields 2 and 1 are already read from SRDEF
      sty.setTerminologyId(fields[4]);
      sty.setTerminology(getTerminology());
      sty.setVersion(getVersion());

      addSemanticTypeComponent(sty, concept);
      // Whenever we are going to commit, update atoms too.
      if (++objectCt % commitCt == 0) {
        for (final Concept c : modifiedConcepts) {
          updateConcept(c);
        }
        modifiedConcepts.clear();
      }
      logAndCommit(objectCt, RootService.logCt, RootService.commitCt);
    }
    // Make sure any remaining modified concepts are updated
    for (final Concept c : modifiedConcepts) {
      updateConcept(c);
    }
    modifiedConcepts.clear();
    commitClearBegin();

  }

  /**
   * Load MRCONSO.RRF. This is responsible for loading {@link Atom}s and
   * {@link AtomClass}es.
   *
   * @throws Exception the exception
   */
  private void loadMrconso() throws Exception {
    logInfo("  Load MRCONSO");
    logInfo("  Insert atoms and concepts ");

    // Set up maps
    String line = null;

    int objectCt = 0;
    final PushBackReader reader = readers.getReader(RrfReaders.Keys.MRCONSO);
    final String fields[] = new String[18];
    String prevCui = null;
    Concept cui = null;
    while ((line = reader.readLine()) != null) {

      line = line.replace("\r", "");
      FieldedStringTokenizer.split(line, "|", 18, fields);

      // Skip non-matching in single mode
      if (singleMode && !fields[11].equals(getTerminology())) {
        continue;
      }

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

      // set the root terminology language
      loadedRootTerminologies.get(fields[11]).setLanguage(fields[1]);

      final Atom atom = new AtomJpa();
      atom.setLanguage(fields[1].intern());
      atom.setTimestamp(releaseVersionDate);
      atom.setLastModified(releaseVersionDate);
      atom.setLastModifiedBy(loader);
      atom.setObsolete(fields[16].equals("O"));
      atom.setSuppressible(!fields[16].equals("N"));
      atom.setPublished(true);
      atom.setPublishable(true);
      atom.setName(fields[14]);
      atom.setTerminology(fields[11].intern());
      if (loadedTerminologies.get(fields[11]) == null) {
        throw new Exception("Atom references terminology that does not exist: "
            + fields[11]);
      }
      atom.setVersion(loadedTerminologies.get(fields[11]).getVersion().intern());
      // skip in single mode
      if (!singleMode) {
        atom.putAlternateTerminologyId(getTerminology(), fields[7]);
      }
      atom.setTerminologyId(fields[8]);
      atom.setTermType(fields[12].intern());
      atom.setWorkflowStatus(published);

      atom.setCodeId(fields[13]);
      atom.setDescriptorId(fields[10]);
      atom.setConceptId(fields[9]);

      atom.setStringClassId(fields[5]);
      atom.setLexicalClassId(fields[3]);
      atom.setCodeId(fields[13]);

      // Handle root terminology short name, hierarchical name, and sy names
      if (fields[11].equals("SRC") && fields[12].equals("SSN")) {
        final Terminology t = loadedTerminologies.get(fields[13].substring(2));
        if (t == null || t.getRootTerminology() == null) {
          logError("  Null root " + line);
        } else {
          t.getRootTerminology().setShortName(fields[14]);
        }
      }
      if (fields[11].equals("SRC") && fields[12].equals("RHT")) {
        final Terminology t = loadedTerminologies.get(fields[13].substring(2));
        if (t == null || t.getRootTerminology() == null) {
          logError("  Null root " + line);
        } else {
          t.getRootTerminology().setHierarchicalName(fields[14]);
        }
      }

      if (fields[11].equals("SRC") && fields[12].equals("RPT")) {
        final Terminology t = loadedTerminologies.get(fields[13].substring(2));
        if (t == null || t.getRootTerminology() == null) {
          logError("  Null root " + line);
        } else {
          t.getRootTerminology().setPreferredName(fields[14]);
        }
      }
      if (fields[11].equals("SRC") && fields[12].equals("RSY")
          && !fields[14].equals("")) {
        final Terminology t = loadedTerminologies.get(fields[13].substring(2));
        if (t == null || t.getRootTerminology() == null) {
          logError("  Null root " + line);
        } else {
          List<String> syNames = t.getRootTerminology().getSynonymousNames();
          syNames.add(fields[14]);
        }
      }

      // Handle terminology sy names
      if (fields[11].equals("SRC") && fields[12].equals("VSY")
          && !fields[14].equals("")) {
        final Terminology t = loadedTerminologies.get(fields[13].substring(2));
        if (t == null || t.getRootTerminology() == null) {
          logError("  Null root " + line);
        } else {
          List<String> syNames = t.getSynonymousNames();
          syNames.add(fields[14]);
        }
      }

      // Determine organizing class type for terminology
      if (!atom.getDescriptorId().equals("")) {
        termIdTypeMap.put(atom.getTerminology(), IdType.DESCRIPTOR);
      } else if (!atom.getConceptId().equals("")) {
        termIdTypeMap.put(atom.getTerminology(), IdType.CONCEPT);
      } // OTHERWISE it remains "CODE"

      // skip in single mode
      if (!singleMode) {
        atom.putConceptTerminologyId(getTerminology(), fields[0]);
      }

      // Add atoms and commit periodically
      addAtom(atom);
      logAndCommit(++objectCt, RootService.logCt, RootService.commitCt);
      atomIdMap.put(fields[7], atom.getId());
      atomTerminologyMap.put(fields[7], atom.getTerminology());
      atomConceptIdMap.put(fields[7], atom.getConceptId());
      atomCodeIdMap.put(fields[7], atom.getCodeId());
      atomDescriptorIdMap.put(fields[7], atom.getDescriptorId());

      // CUI - skip in single mode
      if (!singleMode) {
        // Add concept
        if (prevCui == null || !fields[0].equals(prevCui)) {
          if (prevCui != null) {
            cui.setName(getComputedPreferredName(cui, list));
            addConcept(cui);
            conceptIdMap.put(cui.getTerminology() + cui.getTerminologyId(),
                cui.getId());
            logAndCommit(++objectCt, RootService.logCt, RootService.commitCt);
          }
          cui = new ConceptJpa();
          cui.setTimestamp(releaseVersionDate);
          cui.setLastModified(releaseVersionDate);
          cui.setLastModifiedBy(loader);
          cui.setPublished(true);
          cui.setPublishable(true);
          cui.setTerminology(getTerminology());
          cui.setTerminologyId(fields[0]);
          cui.setVersion(getVersion());
          cui.setWorkflowStatus(published);
        }
        cui.getAtoms().add(atom);
        prevCui = fields[0];
      }

      // Handle Subset
      // C3539934|ENG|S|L11195730|PF|S13913746|N|A23460885||900000000000538005||SNOMEDCT_US|SB|900000000000538005|Description
      // format|9|N|256|
      if (fields[12].equals("SB")) {

        // Have to handle the type later, when we get to attributes
        final AtomSubset atomSubset = new AtomSubsetJpa();
        setSubsetFields(atomSubset, fields);
        cuiAuiAtomSubsetMap.put(fields[0] + fields[7], atomSubset);
        idTerminologyAtomSubsetMap.put(atomSubset.getTerminologyId()
            + atomSubset.getTerminology(), atomSubset);
        final ConceptSubset conceptSubset = new ConceptSubsetJpa();
        setSubsetFields(conceptSubset, fields);
        cuiAuiConceptSubsetMap.put(fields[0] + fields[7], conceptSubset);
        idTerminologyConceptSubsetMap.put(conceptSubset.getTerminologyId()
            + conceptSubset.getTerminology(), conceptSubset);
      }

    }
    // Add last concept
    if (prevCui != null) {
      cui.setName(getComputedPreferredName(cui, list));
      addConcept(cui);
      conceptIdMap.put(cui.getTerminology() + cui.getTerminologyId(),
          cui.getId());
      logAndCommit(++objectCt, RootService.logCt, RootService.commitCt);
    }

    // Set the terminology organizing class types
    for (final Terminology terminology : loadedTerminologies.values()) {
      final IdType idType = termIdTypeMap.get(terminology.getTerminology());
      if (idType != null && idType != IdType.CODE) {
        terminology.setOrganizingClassType(idType);
        updateTerminology(terminology);
      }
    }

    logInfo("  Add concepts");
    objectCt = 0;
    // NOTE: Hibernate-specific to support iterating
    // Restrict to timestamp used for THESE atoms, in case multiple RRF
    // files are loaded
    final Session session = manager.unwrap(Session.class);
    org.hibernate.Query hQuery =
        session.createQuery("select a from AtomJpa a "
            + "where conceptId is not null "
            + "and conceptId != '' and timestamp = :timestamp "
            + "order by terminology, conceptId");
    hQuery.setParameter("timestamp", releaseVersionDate);
    hQuery.setReadOnly(true).setFetchSize(1000);
    ScrollableResults results = hQuery.scroll(ScrollMode.FORWARD_ONLY);
    prevCui = null;
    cui = null;
    while (results.next()) {
      final Atom atom = (Atom) results.get()[0];
      if (atom.getConceptId() == null || atom.getConceptId().isEmpty()) {
        continue;
      }
      if (prevCui == null || !prevCui.equals(atom.getConceptId())) {
        if (cui != null) {
          // compute preferred name
          cui.setName(getComputedPreferredName(cui, list));
          addConcept(cui);
          conceptIdMap.put(cui.getTerminology() + cui.getTerminologyId(),
              cui.getId());
          logAndCommit(++objectCt, RootService.logCt, RootService.commitCt);
        }
        cui = new ConceptJpa();
        cui.setTimestamp(releaseVersionDate);
        cui.setLastModified(releaseVersionDate);
        cui.setLastModifiedBy(loader);
        cui.setPublished(true);
        cui.setPublishable(true);
        cui.setTerminology(atom.getTerminology());
        cui.setTerminologyId(atom.getConceptId());
        cui.setVersion(atom.getVersion());
        cui.setWorkflowStatus(published);
      }
      cui.getAtoms().add(atom);
      prevCui = atom.getConceptId();
    }
    if (cui != null) {
      cui.setName(getComputedPreferredName(cui, list));
      addConcept(cui);
      conceptIdMap.put(cui.getTerminology() + cui.getTerminologyId(),
          cui.getId());
      commitClearBegin();
    }
    results.close();
    logInfo("  Add descriptors");
    objectCt = 0;

    // NOTE: Hibernate-specific to support iterating
    hQuery =
        session.createQuery("select a from AtomJpa a "
            + "where descriptorId is not null "
            + "and descriptorId != '' and timestamp = :timestamp "
            + "order by terminology, descriptorId");
    hQuery.setParameter("timestamp", releaseVersionDate);
    hQuery.setReadOnly(true).setFetchSize(1000);
    results = hQuery.scroll(ScrollMode.FORWARD_ONLY);
    String prevDui = null;
    Descriptor dui = null;
    while (results.next()) {
      final Atom atom = (Atom) results.get()[0];
      if (atom.getDescriptorId() == null || atom.getDescriptorId().isEmpty()) {
        continue;
      }
      if (prevDui == null || !prevDui.equals(atom.getDescriptorId())) {
        if (dui != null) {
          // compute preferred name
          dui.setName(getComputedPreferredName(dui, list));
          addDescriptor(dui);
          descriptorIdMap.put(dui.getTerminology() + dui.getTerminologyId(),
              dui.getId());
          logAndCommit(++objectCt, RootService.logCt, RootService.commitCt);
        }
        dui = new DescriptorJpa();
        dui.setTimestamp(releaseVersionDate);
        dui.setLastModified(releaseVersionDate);
        dui.setLastModifiedBy(loader);
        dui.setPublished(true);
        dui.setPublishable(true);
        dui.setTerminology(atom.getTerminology());
        dui.setTerminologyId(atom.getDescriptorId());
        dui.setVersion(atom.getVersion());
        dui.setWorkflowStatus(published);
      }
      dui.getAtoms().add(atom);
      prevDui = atom.getDescriptorId();
    }
    if (dui != null) {
      dui.setName(getComputedPreferredName(dui, list));
      addDescriptor(dui);
      descriptorIdMap.put(dui.getTerminology() + dui.getTerminologyId(),
          dui.getId());
      commitClearBegin();
    }
    results.close();

    // Use flag to decide whether to handle codes
    if (codesFlag) {
      logInfo("  Add codes");
      objectCt = 0;
      // NOTE: Hibernate-specific to support iterating
      // Skip NOCODE
      hQuery =
          session.createQuery("select a from AtomJpa a "
              + "where codeId is not null "
              + "and codeId != '' and timestamp = :timestamp "
              + "order by terminology, codeId");
      hQuery.setParameter("timestamp", releaseVersionDate);
      hQuery.setReadOnly(true).setFetchSize(1000);
      results = hQuery.scroll(ScrollMode.FORWARD_ONLY);
      String prevCode = null;
      Code code = null;
      while (results.next()) {
        final Atom atom = (Atom) results.get()[0];
        if (atom.getCodeId() == null || atom.getCodeId().isEmpty()) {
          continue;
        }

        // TODO: there is a LNC exception here -for now
        if (!atom.getTerminology().equals("LNC")) {
          // skip where code == concept
          if (atom.getCodeId().equals(atom.getConceptId())) {
            continue;
          }
          // skip where code == descriptor
          if (atom.getCodeId().equals(atom.getDescriptorId())) {
            continue;
          }
        }

        if (prevCode == null || !prevCode.equals(atom.getCodeId())) {
          if (code != null) {
            // compute preferred name
            code.setName(getComputedPreferredName(code, list));
            addCode(code);
            codeIdMap.put(code.getTerminology() + code.getTerminologyId(),
                code.getId());
            logAndCommit(++objectCt, RootService.logCt, RootService.commitCt);
          }
          code = new CodeJpa();
          code.setTimestamp(releaseVersionDate);
          code.setLastModified(releaseVersionDate);
          code.setLastModifiedBy(loader);
          code.setPublished(true);
          code.setPublishable(true);
          code.setTerminology(atom.getTerminology());
          code.setTerminologyId(atom.getCodeId());
          code.setVersion(atom.getVersion());
          code.setWorkflowStatus(published);
        }
        code.getAtoms().add(atom);
        prevCode = atom.getCodeId();
      }
      if (code != null) {
        code.setName(getComputedPreferredName(code, list));
        addCode(code);
        codeIdMap.put(code.getTerminology() + code.getTerminologyId(),
            code.getId());
        commitClearBegin();
      }
      results.close();
    }

    // NOTE: for efficiency and lack of use cases, we've temporarily
    // suspended the loading of LexicalClass and StringClass objects

    // // NOTE: atoms are not connected to lexical classes as there are
    // // currently no known uses for this.
    // logInfo(" Add lexical classes");
    // objectCt = 0;
    // query = NEED TO FIX THIS
    // manager
    // .createQuery("select a.id from AtomJpa a order by lexicalClassId");
    // String prevLui = null;
    // LexicalClass lui = null;
    // LexicalClass atoms = null;
    // for (final Long id : (List<Long>) query.getResultList()) {
    // final Atom atom = getAtom(id);
    // if (atom.getLexicalClassId() == null
    // || atom.getLexicalClassId().isEmpty()) {
    // continue;
    // }
    // if (prevLui == null || !prevLui.equals(atom.getLexicalClassId())) {
    // if (lui != null) {
    // // compute preferred name
    // lui.setName(getComputedPreferredName(atoms));
    // addLexicalClass(lui);
    // logAndCommit(++objectCt, RootService.logCt, RootService.commitCt);
    // }
    // // just used to hold atoms, enver saved.
    // atoms = new LexicalClassJpa();
    // lui = new LexicalClassJpa();
    // lui.setTimestamp(releaseVersionDate);
    // lui.setLastModified(releaseVersionDate);
    // lui.setLastModifiedBy(loader);
    // lui.setPublished(true);
    // lui.setPublishable(true);
    // lui.setTerminology(terminology);
    // lui.setTerminologyId(atom.getLexicalClassId());
    // lui.setVersion(version);
    // lui.setWorkflowStatus(published);
    // lui.setNormalizedString(getNormalizedString(atom.getName()));
    // }
    // atoms.addAtom(atom);
    // prevLui = atom.getLexicalClassId();
    // }
    // if (lui != null) {
    // lui.setName(getComputedPreferredName(atoms));
    // commitClearBegin();
    // logAndCommit(++objectCt, RootService.logCt, RootService.commitCt);
    // }
    //
    // // NOTE: currently atoms are not loaded for string classes
    // // We simply load the objects themselves ( for SUI maintenance)
    // // There are no known use cases for having the atoms here.
    // logInfo(" Add string classes");
    // objectCt = 0;
    // query = NEED TO FIX THIS
    // manager
    // .createQuery("select distinct stringClassId, name from AtomJpa a");
    // for (final Object[] suiFields : (List<Object[]>) query.getResultList()) {
    // final StringClass sui = new StringClassJpa();
    // sui.setTimestamp(releaseVersionDate);
    // sui.setLastModified(releaseVersionDate);
    // sui.setLastModifiedBy(loader);
    // sui.setPublished(true);
    // sui.setPublishable(true);
    // sui.setTerminology(terminology);
    // sui.setTerminologyId(suiFields[0].toString());
    // sui.setVersion(version);
    // sui.setWorkflowStatus(published);
    // sui.setName(suiFields[1].toString());
    // addStringClass(sui);
    // logAndCommit(++objectCt, RootService.logCt, RootService.commitCt);
    // }

    // commit
    commitClearBegin();

    // Update all root terminologies now that we know languages and names
    for (final RootTerminology root : loadedRootTerminologies.values()) {
      updateRootTerminology(root);
    }

    // Update all root terminologies now that we know languages and names
    for (final Terminology terminology : loadedTerminologies.values()) {
      updateTerminology(terminology);
    }
    commitClearBegin();

  }

  /**
   * Load atoms before computing.
   *
   * @param atomClass the atom class
   * @return the computed preferred name
   * @throws Exception the exception
   */
  @Override
  public String getComputedPreferredName(AtomClass atomClass,
    PrecedenceList list) throws Exception {
    final List<Atom> atoms = new ArrayList<>();
    for (final Atom atom : atomClass.getAtoms()) {
      atoms.add(getAtom(atom.getId()));
    }
    atomClass.setAtoms(atoms);
    return super.getComputedPreferredName(atomClass, list);
  }

  /**
   * Sets the subset fields.
   *
   * @param subset the subset
   * @param fields the fields
   */
  private void setSubsetFields(Subset subset, String[] fields) {
    // C3539934|ENG|S|L11195730|PF|S13913746|N|A23460885||900000000000538005||SNOMEDCT_US|SB|900000000000538005|Description
    // format|9|N|256|
    subset.setDescription(fields[14]);
    subset.setTimestamp(releaseVersionDate);
    subset.setLastModified(releaseVersionDate);
    subset.setLastModifiedBy(loader);
    subset.setName(fields[14]);
    subset.setObsolete(fields[16].equals("O"));
    subset.setPublishable(true);
    subset.setPublished(true);
    subset.setSuppressible(!fields[16].equals("N"));
    subset.setTerminology(fields[11].intern());
    // already vetted by atom
    subset.setVersion(loadedTerminologies.get(fields[11]).getVersion());
    subset.setTerminologyId(fields[13]);
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

  /**
   * Close.
   *
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public void close() throws Exception {
    super.close();
    readers = null;
  }

  /**
   * Standard hashing strategy.
   */
  @SuppressWarnings("serial")
  public class StandardStrategy implements HashingStrategy<String> {

    /**
     * Instantiates an empty {@link StandardStrategy}.
     */
    public StandardStrategy() {
      // n/a
    }

    /* see superclass */
    @Override
    public int computeHashCode(String object) {
      return object.hashCode();
    }

    /* see superclass */
    @Override
    public boolean equals(String o1, String o2) {
      return o1.equals(o2);
    }

  }

  /**
   * Indicates whether or not extension module is the case.
   *
   * @param moduleId the module id
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  @SuppressWarnings("static-method")
  private boolean isExtensionModule(String moduleId) {
    return !moduleId.equals("900000000000207008")
        && !moduleId.equals("900000000000012004");
  }

  /* see superclass */
  @Override
  public void computeTreePositions() throws Exception {

    // Compute tree positions
    // Refresh caches after metadata has changed in loader
    for (final Terminology t : getTerminologyLatestVersions().getObjects()) {
      // Only compute for organizing class types
      if (t.getOrganizingClassType() != null) {
        treePosAlgorithm = new TreePositionAlgorithm();
        treePosAlgorithm.setTerminology(t.getTerminology());
        treePosAlgorithm.setVersion(t.getVersion());
        treePosAlgorithm.setIdType(t.getOrganizingClassType());
        // some terminologies may have cycles, allow these for now.
        treePosAlgorithm.setCycleTolerant(true);
        // compute "semantic types" for concept hierarchies
        if (t.getOrganizingClassType() == IdType.CONCEPT) {
          treePosAlgorithm.setComputeSemanticType(true);
        }
        treePosAlgorithm.compute();
        treePosAlgorithm.close();
      }
    }

    // Compute label sets - after transitive closure
    // for each subset, compute the label set
    for (final Terminology t : getTerminologyLatestVersions().getObjects()) {
      for (final Subset subset : getConceptSubsets(t.getTerminology(),
          t.getVersion(), Branch.ROOT).getObjects()) {
        final ConceptSubset conceptSubset = (ConceptSubset) subset;
        if (conceptSubset.isLabelSubset()) {
          Logger.getLogger(getClass()).info(
              "  Create label set for subset = " + subset);
          labelSetAlgorithm = new LabelSetMarkedParentAlgorithm();
          labelSetAlgorithm.setTerminology(t.getTerminology());
          labelSetAlgorithm.setVersion(t.getVersion());
          labelSetAlgorithm.setSubset(conceptSubset);
          labelSetAlgorithm.compute();
          labelSetAlgorithm.close();
        }
      }
    }

  }

  /* see superclass */
  @Override
  public void computeTransitiveClosures() throws Exception {
    Logger.getLogger(getClass()).info(
        "  Compute transitive closure from  " + getTerminology() + "/"
            + getVersion());

    // Compute transitive closure
    // Obtain each terminology and run transitive closure on it with the
    // correct id type
    // Refresh caches after metadata has changed in loader
    refreshCaches();
    for (final Terminology t : getTerminologyLatestVersions().getObjects()) {
      // Only compute for organizing class types
      if (t.getOrganizingClassType() != null) {
        transClosureAlgorithm = new TransitiveClosureAlgorithm();
        transClosureAlgorithm.setTerminology(t.getTerminology());
        transClosureAlgorithm.setVersion(t.getVersion());
        transClosureAlgorithm.setIdType(t.getOrganizingClassType());
        // some terminologies may have cycles, allow these for now.
        transClosureAlgorithm.setCycleTolerant(true);
        transClosureAlgorithm.compute();
        transClosureAlgorithm.close();
      }
    }
  }

  /* see superclass */
  @Override
  public void cancel() throws Exception {
    // cancel any currently running local algorithms
    treePosAlgorithm.cancel();
    transClosureAlgorithm.cancel();
    labelSetAlgorithm.cancel();

    // invoke superclass cancel
    super.cancel();
  }
  @Override
  public void computeExpressionIndexes() throws Exception {
   // do nothing
  }
}
