/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo;

import gnu.trove.strategy.HashingStrategy;

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
import com.wci.umls.server.algo.Algorithm;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.FieldedStringTokenizer;
import com.wci.umls.server.helpers.KeyValuePair;
import com.wci.umls.server.helpers.KeyValuePairList;
import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.jpa.ReleaseInfoJpa;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.AtomRelationshipJpa;
import com.wci.umls.server.jpa.content.AtomSubsetJpa;
import com.wci.umls.server.jpa.content.AtomSubsetMemberJpa;
import com.wci.umls.server.jpa.content.AttributeJpa;
import com.wci.umls.server.jpa.content.CodeJpa;
import com.wci.umls.server.jpa.content.CodeRelationshipJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.ConceptRelationshipJpa;
import com.wci.umls.server.jpa.content.ConceptSubsetJpa;
import com.wci.umls.server.jpa.content.ConceptSubsetMemberJpa;
import com.wci.umls.server.jpa.content.DefinitionJpa;
import com.wci.umls.server.jpa.content.DescriptorJpa;
import com.wci.umls.server.jpa.content.DescriptorRelationshipJpa;
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
import com.wci.umls.server.jpa.services.HistoryServiceJpa;
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
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.content.ConceptSubset;
import com.wci.umls.server.model.content.ConceptSubsetMember;
import com.wci.umls.server.model.content.Definition;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.DescriptorRelationship;
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
import com.wci.umls.server.services.helpers.ProgressEvent;
import com.wci.umls.server.services.helpers.ProgressListener;
import com.wci.umls.server.services.helpers.PushBackReader;

/**
 * Implementation of an algorithm to import RF2 snapshot data.
 */
public class RrfLoaderAlgorithm extends HistoryServiceJpa implements Algorithm {

  /** Listeners. */
  private List<ProgressListener> listeners = new ArrayList<>();

  /** The logging object ct threshold. */
  private final static int logCt = 2000;

  /** The commit count. */
  private final static int commitCt = 2000;

  /** The terminology. */
  private String terminology;

  /** The terminology version. */
  private String version;

  /** The single mode. */
  private boolean singleMode = false;

  /** The release version. */
  private String releaseVersion;

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

  /** The Constant coreModuleId. */
  private final static String coreModuleId = "900000000000207008";

  /** The Constant metadataModuleId. */
  private final static String metadataModuleId = "900000000000012004";

  /** non-core modules map. */
  private Map<String, Set<Long>> moduleConceptIdMap = new HashMap<>();

  /** The lat code map. */
  private static Map<String, String> latCodeMap = new HashMap<>();

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
  }

  /**
   * Instantiates an empty {@link RrfLoaderAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public RrfLoaderAlgorithm() throws Exception {
    super();
  }

  /**
   * Sets the terminology.
   *
   * @param terminology the terminology
   */
  public void setTerminology(String terminology) {
    this.terminology = terminology;
  }

  /**
   * Sets the terminology version.
   *
   * @param version the terminology version
   */
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   * Sets the release version.
   *
   * @param releaseVersion the rlease version
   */
  public void setReleaseVersion(String releaseVersion) {
    this.releaseVersion = releaseVersion;
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
   * Sets the readers.
   *
   * @param readers the readers
   */
  public void setReaders(RrfReaders readers) {
    this.readers = readers;
  }

  /* see superclass */
  @Override
  public void compute() throws Exception {
    try {
      Logger.getLogger(getClass()).info("Start loading RRF");
      Logger.getLogger(getClass()).info("  terminology = " + terminology);
      Logger.getLogger(getClass()).info("  version = " + version);
      Logger.getLogger(getClass()).info("  single mode = " + singleMode);
      Logger.getLogger(getClass()).info("  releaseVersion = " + releaseVersion);
      releaseVersionDate =
          ConfigUtility.DATE_FORMAT.parse(releaseVersion.substring(0, 4)
              + "0101");

      // Track system level information
      long startTimeOrig = System.nanoTime();

      // control transaction scope
      setTransactionPerOperation(false);
      // Turn of ID computation when loading a terminology
      setAssignIdentifiersFlag(false);
      // Let loader set last modified flags.
      setLastModifiedFlag(false);

      // faster performance.
      beginTransaction();

      //
      // Load the metadata
      //

      // Load semantic types
      if (!singleMode)
        loadSrdef();

      // Load MRDOC data
      loadMrdoc();

      // Load MRSAB data
      loadMrsab();

      // Load precedence info
      loadMrrank();

      // Commit
      commitClearBegin();

      // Load the content
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

      // Need to reset MRSAT reader
      readers.closeReaders();
      readers.openOriginalReaders();

      // Subsets/members
      loadMrsatSubsets();

      // Make subsets and label sets
      loadExtensionLabelSets();

      // Commit
      commitClearBegin();

      // Add release info for individual terminology
      for (Terminology terminology : getTerminologyLatestVersions()
          .getObjects()) {
        final String version = terminology.getVersion();
        ReleaseInfo info =
            getReleaseInfo(terminology.getTerminology(), version);
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
      ReleaseInfo info = getReleaseInfo(terminology, releaseVersion);
      if (info == null) {
        info = new ReleaseInfoJpa();
        info.setName(releaseVersion);
        info.setDescription(terminology + " " + releaseVersion + " release");
        info.setPlanned(false);
        info.setPublished(true);
        info.setReleaseBeginDate(null);
        info.setReleaseFinishDate(releaseVersionDate);
        info.setTerminology(terminology);
        info.setVersion(version);
        info.setLastModified(releaseVersionDate);
        info.setLastModifiedBy(loader);
        addReleaseInfo(info);
      }

      // Clear concept cache
      // clear and commit
      commit();
      clear();

      Logger.getLogger(getClass()).info("Log component stats");
      Map<String, Integer> stats = getComponentStats(null, null, null);
      List<String> statsList = new ArrayList<>(stats.keySet());
      Collections.sort(statsList);
      for (String key : statsList) {
        Logger.getLogger(getClass()).info("  " + key + " = " + stats.get(key));
      }
      // Final logging messages
      Logger.getLogger(getClass()).info(
          "      elapsed time = " + getTotalElapsedTimeStr(startTimeOrig));
      Logger.getLogger(getClass()).info("Done ...");

    } catch (Exception e) {
      throw e;
    }
  }

  /**
   * Load SRDEF. This is responsible for loading {@link SemanticType} metadata.
   *
   * @throws Exception the exception
   */
  private void loadSrdef() throws Exception {
    Logger.getLogger(getClass()).info("  Load Semantic types");
    String line = null;
    int objectCt = 0;
    PushBackReader reader = readers.getReader(RrfReaders.Keys.SRDEF);
    final String[] fields = new String[10];
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
        sty.setTerminology(terminology);
        sty.setVersion(version);
        sty.setTreeNumber(fields[3]);
        sty.setTypeId(fields[1]);
        sty.setUsageNote(fields[6]);
        sty.setValue(fields[2]);

        sty.setTimestamp(releaseVersionDate);
        sty.setLastModified(releaseVersionDate);
        sty.setLastModifiedBy(loader);
        sty.setPublished(true);
        sty.setPublishable(true);
        Logger.getLogger(getClass()).debug("    add semantic type - " + sty);
        addSemanticType(sty);
        logAndCommit(++objectCt);
      }
    }
  }

  /**
   * Load MRDOC. This is responsible for loading much of the metadata.
   *
   * @throws Exception the exception
   */
  private void loadMrdoc() throws Exception {
    Logger.getLogger(getClass()).info("  Load MRDOC abbreviation types");
    String line = null;
    Set<String> atnSeen = new HashSet<>();
    Map<String, RelationshipType> relMap = new HashMap<>();
    Map<String, String> inverseRelMap = new HashMap<>();
    Map<String, AdditionalRelationshipType> relaMap = new HashMap<>();
    Map<String, String> inverseRelaMap = new HashMap<>();
    Map<String, TermType> ttyMap = new HashMap<>();
    PushBackReader reader = readers.getReader(RrfReaders.Keys.MRDOC);
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
        atn.setTerminology(terminology);
        atn.setVersion(version);
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
        lat.setTerminology(terminology);
        lat.setVersion(version);
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
        rela.setTerminology(terminology);
        rela.setVersion(version);
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
          rela1.setInverseType(rela2);
          rela2.setInverseType(rela1);
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
        rel.setTerminology(terminology);
        rel.setVersion(version);
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
        tty.setTerminology(terminology);
        tty.setVersion(version);
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
        GeneralMetadataEntry entry = new GeneralMetadataEntryJpa();

        entry.setTimestamp(releaseVersionDate);
        entry.setLastModified(releaseVersionDate);
        entry.setLastModifiedBy(loader);
        entry.setTerminology(terminology);
        entry.setVersion(version);
        entry.setPublished(true);
        entry.setPublishable(true);

        entry.setKey(fields[0]);
        entry.setAbbreviation(fields[1]);
        entry.setType(fields[2]);
        entry.setExpandedForm(fields[3]);

        addGeneralMetadataEntry(entry);
      }

      logAndCommit(++objectCt);
    }

    // Add TTYs when done
    for (TermType tty : ttyMap.values()) {
      addTermType(tty);
      loadedTermTypes.put(tty.getAbbreviation(), tty);
    }

  }

  /**
   * Load MRSAB. This is responsible for loading {@link Terminology} and
   * {@link RootTerminology} info.
   *
   * @throws Exception the exception
   */
  private void loadMrsab() throws Exception {
    Logger.getLogger(getClass()).info("  Load MRSAB data");
    String line = null;
    PushBackReader reader = readers.getReader(RrfReaders.Keys.MRSAB);
    final String fields[] = new String[25];
    while ((line = reader.readLine()) != null) {

      line = line.replace("\r", "");
      FieldedStringTokenizer.split(line, "|", 25, fields);

      // Skip non-matching in single mode
      if (singleMode && !fields[3].equals(terminology)) {
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
      if (fields[22].equals("N") && !fields[3].equals("MTH")) {
        Logger.getLogger(getClass()).debug("  Skip terminology " + fields[2]);
        continue;
      }

      Terminology term = new TerminologyJpa();

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
      if (singleMode || fields[6].equals(""))
        term.setVersion(version);
      else
        term.setVersion(fields[6]);
      term.setDescriptionLogicTerminology(false);

      if (!loadedRootTerminologies.containsKey(fields[3])) {
        RootTerminology root = new RootTerminologyJpa();
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

      RootTerminology root = loadedRootTerminologies.get(fields[3]);
      term.setRootTerminology(root);
      addTerminology(term);
      // cache terminology by RSAB and VSAB
      loadedTerminologies.put(term.getTerminology(), term);
      if (!fields[2].equals("")) {
        loadedTerminologies.put(fields[2], term);
      }
    }

    // Add the terminology for this load, e.g. "UMLS"
    // Skip in single mode
    if (!singleMode) {
      Terminology term = new TerminologyJpa();
      term.setAssertsRelDirection(false);
      term.setCurrent(true);
      term.setOrganizingClassType(IdType.CONCEPT);
      term.setPreferredName(terminology);
      term.setTimestamp(releaseVersionDate);
      term.setLastModified(releaseVersionDate);
      term.setLastModifiedBy(loader);
      term.setTerminology(terminology);
      term.setVersion(version);
      term.setDescriptionLogicTerminology(false);
      term.setMetathesaurus(true);

      RootTerminology root = new RootTerminologyJpa();
      root.setFamily(terminology);
      root.setPreferredName(terminology);
      root.setRestrictionLevel(0);
      root.setTerminology(terminology);
      root.setTimestamp(releaseVersionDate);
      root.setLastModified(releaseVersionDate);
      root.setLastModifiedBy(loader);
      root.setLanguage(loadedLanguages.get("ENG"));
      if (root.getLanguage() == null) {
        throw new Exception("Unable to find ENG langauge.");
      }
      addRootTerminology(root);
      loadedRootTerminologies.put(root.getTerminology(), root);
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

    PrecedenceList list = new PrecedenceListJpa();
    list.setDefaultList(true);
    list.setTerminology(terminology);
    list.setVersion(version);

    List<KeyValuePair> lkvp = new ArrayList<>();

    Logger.getLogger(getClass()).info("  Load MRRANK data");
    String line = null;
    PushBackReader reader = readers.getReader(RrfReaders.Keys.MRRANK);
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
      if (singleMode && !fields[1].equals(terminology)) {
        continue;
      }

      KeyValuePair pair = new KeyValuePair();
      pair.setKey(fields[1]);
      pair.setValue(fields[2]);
      lkvp.add(pair);

      // Set term-type suppress
      loadedTermTypes.get(fields[2]).setSuppressible(fields[3].equals("Y"));
    }

    KeyValuePairList kvpl = new KeyValuePairList();
    kvpl.setKeyValuePairList(lkvp);
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
    Logger.getLogger(getClass()).info("  Load MRDEF data");
    String line = null;
    int objectCt = 0;
    PushBackReader reader = readers.getReader(RrfReaders.Keys.MRDEF);
    // make set of all atoms that got an additional definition
    Set<Atom> modifiedAtoms = new HashSet<>();
    final String fields[] = new String[8];
    while ((line = reader.readLine()) != null) {

      line = line.replace("\r", "");
      FieldedStringTokenizer.split(line, "|", 8, fields);

      // Skip non-matching in single mode
      if (singleMode && !fields[4].equals(terminology)) {
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

      atom.addDefinition(def);
      modifiedAtoms.add(atom);

      def.setTimestamp(releaseVersionDate);
      def.setLastModified(releaseVersionDate);
      def.setLastModifiedBy(loader);
      def.setObsolete(fields[6].equals("O"));
      def.setSuppressible(!fields[6].equals("N"));
      def.setPublished(true);
      def.setPublishable(true);

      if (!singleMode) {
        def.putAlternateTerminologyId(terminology, fields[2]);
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
      logAndCommit(objectCt);

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
    Logger.getLogger(getClass()).info("  Load MRSAT data");
    String line = null;

    int objectCt = 0;
    PushBackReader reader = readers.getReader(RrfReaders.Keys.MRSAT);
    // make set of all atoms that got an additional attribute
    Set<Atom> modifiedAtoms = new HashSet<>();
    Set<Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes>> modifiedRelationships =
        new HashSet<>();
    Set<Code> modifiedCodes = new HashSet<>();
    Set<Descriptor> modifiedDescriptors = new HashSet<>();
    Set<Concept> modifiedConcepts = new HashSet<>();
    final String fields[] = new String[13];
    while ((line = reader.readLine()) != null) {

      line = line.replace("\r", "");
      FieldedStringTokenizer.split(line, "|", 13, fields);

      // Skip non-matching in single mode
      if (singleMode && !fields[9].equals(terminology)
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
        att.putAlternateTerminologyId(terminology, fields[6]);
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
        continue;

      } else if (fields[4].equals("AUI")) {
        // Get the concept for the AUI
        Atom atom = getAtom(atomIdMap.get(fields[3]));
        atom.addAttribute(att);
        addAttribute(att, atom);
      }
      // Special case of a CODE attribute where the AUI has "NOCODE" as the code
      // UMLS has one case of an early XM atom with NOCODE (ICD9CM to CCS map)
      // In loadMrconso we skip NOCODE codes, never creating them.
      else if (fields[4].equals("CODE")
          && atomCodeIdMap.get(fields[3]).equals("NOCODE")) {
        // Get the concept for the AUI
        Atom atom = getAtom(atomIdMap.get(fields[3]));
        atom.addAttribute(att);
        addAttribute(att, atom);
      } else if (fields[4].equals("RUI")) {
        // Get the relationship for the RUI
        Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> relationship =
            getRelationship(relationshipMap.get(fields[3]), null);
        relationship.addAttribute(att);
        addAttribute(att, relationship);
      } else if (fields[4].equals("CODE")) {
        // Get the code for the terminology and CODE of the AUI
        Code code =
            getCode(codeIdMap.get(atomTerminologyMap.get(fields[3])
                + atomCodeIdMap.get(fields[3])));
        code.addAttribute(att);
        addAttribute(att, code);
      } else if (fields[4].equals("CUI")) {
        // Get the concept for the terminology and CUI
        att.setTerminology(terminology);
        att.setVersion(version);
        Concept concept = getConcept(conceptIdMap.get(terminology + fields[0]));
        concept.addAttribute(att);
        addAttribute(att, concept);
      } else if (fields[4].equals("SDUI")) {
        // Get the descriptor for the terminology and SDUI of the AUI
        Descriptor descriptor =
            getDescriptor(descriptorIdMap.get(atomTerminologyMap.get(fields[3])
                + atomDescriptorIdMap.get(fields[3])));
        descriptor.addAttribute(att);
        addAttribute(att, descriptor);
      } else if (fields[4].equals("SCUI")) {
        // Get the concept for the terminology and SCUI of the AUI
        Concept concept =
            getConcept(conceptIdMap.get(atomTerminologyMap.get(fields[3])
                + atomConceptIdMap.get(fields[3])));
        concept.addAttribute(att);
        addAttribute(att, concept);
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
        for (final Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> r : modifiedRelationships) {
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
          Logger.getLogger(getClass()).info(
              "  extension module = " + fields[10] + ", " + key);
          if (!moduleConceptIdMap.containsKey(key)) {
            moduleConceptIdMap.put(key, new HashSet<Long>());
          }
          Logger.getLogger(getClass()).info(
              "    concept = " + atomConceptIdMap.get(fields[3]));
          moduleConceptIdMap.get(key).add(
              conceptIdMap.get(atomTerminologyMap.get(fields[3])
                  + atomConceptIdMap.get(fields[3])));
        }
      }

      // log and commit
      logAndCommit(objectCt);

      //
      // NOTE: there are no subset attributes in RRF
      //

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
    for (final Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> r : modifiedRelationships) {
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
   * Load subset data from MRSAT. This is responsible for loading {@link Subset}
   * s and {@link SubsetMember}s.
   *
   * @throws Exception the exception
   */
  private void loadMrsatSubsets() throws Exception {
    Logger.getLogger(getClass()).info("  Load MRSAT Subset data");
    String line = null;

    int objectCt = 0;
    PushBackReader reader = readers.getReader(RrfReaders.Keys.MRSAT);
    Map<String, SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset>> addedSubsetMembers =
        new HashMap<>();
    String prevMetaUi = null;
    final String fields[] = new String[13];
    final String atvFields[] = new String[3];
    while ((line = reader.readLine()) != null) {

      line = line.replace("\r", "");
      FieldedStringTokenizer.split(line, "|", 13, fields);

      // Skip non-matching in single mode
      if (singleMode && !fields[9].equals(terminology)
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
        ++objectCt;
        // Ready to commit, clear the subset member cache
        if (objectCt % commitCt == 0) {
          addedSubsetMembers.clear();
        }
        logAndCommit(objectCt);
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
          Attribute memberAtt = new AttributeJpa();
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
          member.addAttribute(memberAtt);

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
    List<ConceptSubset> subsets = new ArrayList<>();
    for (String key : moduleConceptIdMap.keySet()) {
      Logger.getLogger(getClass()).info("  Create subset for module = " + key);
      // bail if concept doesn't exist
      if (!conceptIdMap.containsKey(key)) {
        Logger.getLogger(getClass()).warn("    MISSING CONCEPT");
        continue;
      }
      Concept concept = getConcept(conceptIdMap.get(key));
      ConceptSubset subset = new ConceptSubsetJpa();
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
      Logger.getLogger(getClass()).info("  Add subset memebers");
      for (Long id : moduleConceptIdMap.get(key)) {
        final Concept memberConcept = getConcept(id);

        ConceptSubsetMember member = new ConceptSubsetMemberJpa();
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
        logAndCommit(++objectCt);
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
    Logger.getLogger(getClass()).info("  Load MRREL data");
    String line = null;

    int objectCt = 0;
    PushBackReader reader = readers.getReader(RrfReaders.Keys.MRREL);
    final String fields[] = new String[16];
    while ((line = reader.readLine()) != null) {

      line = line.replace("\r", "");
      FieldedStringTokenizer.split(line, "|", 16, fields);

      // Skip non-matching in single mode
      if (singleMode && !fields[10].equals(terminology)
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

        final Concept fromConcept =
            getConcept(conceptIdMap.get(terminology + fields[4]));
        conceptRel.setFrom(fromConcept);

        final Concept toConcept =
            getConcept(conceptIdMap.get(terminology + fields[0]));
        conceptRel.setTo(toConcept);

        setRelationshipFields(fields, conceptRel);
        conceptRel.setTerminology(terminology);
        conceptRel.setVersion(version);
        addRelationship(conceptRel);
        relationshipMap.put(fields[8], conceptRel.getId());

      } else if (fields[2].equals("SCUI") && fields[6].equals("SCUI")) {
        final ConceptRelationship conceptRel = new ConceptRelationshipJpa();

        final Concept fromConcept =
            getConcept(conceptIdMap.get(atomTerminologyMap.get(fields[5])
                + atomConceptIdMap.get(fields[5])));
        conceptRel.setFrom(fromConcept);

        final Concept toConcept =
            getConcept(conceptIdMap.get(atomTerminologyMap.get(fields[1])
                + atomConceptIdMap.get(fields[1])));
        conceptRel.setTo(toConcept);

        setRelationshipFields(fields, conceptRel);
        addRelationship(conceptRel);
        relationshipMap.put(fields[8], conceptRel.getId());

      } else if (fields[2].equals("SDUI") && fields[6].equals("SDUI")) {
        final DescriptorRelationship descriptorRel =
            new DescriptorRelationshipJpa();

        final Descriptor fromDescriptor =
            getDescriptor(descriptorIdMap.get(atomTerminologyMap.get(fields[5])
                + atomDescriptorIdMap.get(fields[5])));
        descriptorRel.setFrom(fromDescriptor);

        final Descriptor toDescriptor =
            getDescriptor(descriptorIdMap.get(atomTerminologyMap.get(fields[1])
                + atomDescriptorIdMap.get(fields[1])));
        descriptorRel.setTo(toDescriptor);

        setRelationshipFields(fields, descriptorRel);
        addRelationship(descriptorRel);
        relationshipMap.put(fields[8], descriptorRel.getId());

      } else if (fields[2].equals("CODE") && fields[6].equals("CODE")) {
        final CodeRelationship codeRel = new CodeRelationshipJpa();

        final Code fromCode =
            getCode(codeIdMap.get(atomTerminologyMap.get(fields[5])
                + atomCodeIdMap.get(fields[5])));
        codeRel.setFrom(fromCode);

        final Code toCode =
            getCode(codeIdMap.get(atomTerminologyMap.get(fields[1])
                + atomCodeIdMap.get(fields[1])));
        codeRel.setTo(toCode);

        setRelationshipFields(fields, codeRel);
        addRelationship(codeRel);
        relationshipMap.put(fields[8], codeRel.getId());

      } else {
        Logger.getLogger(getClass()).debug(
            "  SKIPPING relationship STYPE1!=STYPE2 - " + line);
        continue;
      }

      logAndCommit(++objectCt);
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
    String[] fields,
    Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> relationship)
    throws Exception {
    relationship.setTimestamp(releaseVersionDate);
    relationship.setLastModified(releaseVersionDate);
    relationship.setLastModifiedBy(loader);
    relationship.setObsolete(fields[14].equals("O"));
    relationship.setSuppressible(!fields[14].equals("N"));
    relationship.setPublished(true);
    relationship.setPublishable(true);

    relationship.setRelationshipType(fields[3]);
    relationship.setAdditionalRelationshipType(fields[7]);

    if (!singleMode) {
      relationship.putAlternateTerminologyId(terminology, fields[8]);
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
    relationship.setGroup(fields[12]);

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
    Logger.getLogger(getClass()).info("  Load MRSTY data");
    String line = null;
    PushBackReader reader = readers.getReader(RrfReaders.Keys.MRSTY);
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
          getConcept(conceptIdMap.get(terminology + fields[0]));
      concept.addSemanticType(sty);
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
      sty.setTerminology(terminology);
      sty.setVersion(version);

      addSemanticTypeComponent(sty, concept);
      // Whenever we are going to commit, update atoms too.
      if (++objectCt % commitCt == 0) {
        for (final Concept c : modifiedConcepts) {
          updateConcept(c);
        }
        modifiedConcepts.clear();
      }
      logAndCommit(objectCt);
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
    Logger.getLogger(getClass()).info("  Load MRCONSO");
    Logger.getLogger(getClass()).info("  Insert atoms and concepts ");

    // Set up maps
    String line = null;

    int objectCt = 0;
    PushBackReader reader = readers.getReader(RrfReaders.Keys.MRCONSO);
    final String fields[] = new String[18];
    String prevCui = null;
    Concept cui = null;
    while ((line = reader.readLine()) != null) {

      line = line.replace("\r", "");
      FieldedStringTokenizer.split(line, "|", 18, fields);

      // Skip non-matching in single mode
      if (singleMode && !fields[11].equals(terminology)) {
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
      loadedRootTerminologies.get(fields[11]).setLanguage(
          loadedLanguages.get(fields[1]));

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
        atom.putAlternateTerminologyId(terminology, fields[7]);
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
          Logger.getLogger(getClass()).error("  Null root " + line);
        } else {
          t.getRootTerminology().setShortName(fields[14]);
        }
      }
      if (fields[11].equals("SRC") && fields[12].equals("RHT")) {
        final Terminology t = loadedTerminologies.get(fields[13].substring(2));
        if (t == null || t.getRootTerminology() == null) {
          Logger.getLogger(getClass()).error("  Null root " + line);
        } else {
          t.getRootTerminology().setHierarchicalName(fields[14]);
        }
      }

      if (fields[11].equals("SRC") && fields[12].equals("RPT")) {
        final Terminology t = loadedTerminologies.get(fields[13].substring(2));
        if (t == null || t.getRootTerminology() == null) {
          Logger.getLogger(getClass()).error("  Null root " + line);
        } else {
          t.getRootTerminology().setPreferredName(fields[14]);
        }
      }
      if (fields[11].equals("SRC") && fields[12].equals("RSY")
          && !fields[14].equals("")) {
        final Terminology t = loadedTerminologies.get(fields[13].substring(2));
        if (t == null || t.getRootTerminology() == null) {
          Logger.getLogger(getClass()).error("  Null root " + line);
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
          Logger.getLogger(getClass()).error("  Null root " + line);
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
        atom.putConceptTerminologyId(terminology, fields[0]);
      }

      // Add atoms and commit periodically
      addAtom(atom);
      logAndCommit(++objectCt);
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
            cui.setName(getComputedPreferredName(cui));
            addConcept(cui);
            conceptIdMap.put(cui.getTerminology() + cui.getTerminologyId(),
                cui.getId());
            logAndCommit(++objectCt);
          }
          cui = new ConceptJpa();
          cui.setTimestamp(releaseVersionDate);
          cui.setLastModified(releaseVersionDate);
          cui.setLastModifiedBy(loader);
          cui.setPublished(true);
          cui.setPublishable(true);
          cui.setTerminology(terminology);
          cui.setTerminologyId(fields[0]);
          cui.setVersion(version);
          cui.setWorkflowStatus(published);
        }
        cui.addAtom(atom);
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
      cui.setName(getComputedPreferredName(cui));
      addConcept(cui);
      conceptIdMap.put(cui.getTerminology() + cui.getTerminologyId(),
          cui.getId());
      logAndCommit(++objectCt);
    }

    // Set the terminology organizing class types
    for (final Terminology terminology : loadedTerminologies.values()) {
      final IdType idType = termIdTypeMap.get(terminology.getTerminology());
      if (idType != null && idType != IdType.CODE) {
        terminology.setOrganizingClassType(idType);
        updateTerminology(terminology);
      }
    }

    Logger.getLogger(getClass()).info("  Add concepts");
    objectCt = 0;
    // NOTE: Hibernate-specific to support iterating
    Session session = manager.unwrap(Session.class);
    org.hibernate.Query hQuery =
        session
            .createQuery(
                "select a from AtomJpa a " + "where conceptId is not null "
                    + "and conceptId != '' order by terminology, conceptId")
            .setReadOnly(true).setFetchSize(1000);
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
          cui.setName(getComputedPreferredName(cui));
          addConcept(cui);
          conceptIdMap.put(cui.getTerminology() + cui.getTerminologyId(),
              cui.getId());
          logAndCommit(++objectCt);
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
      cui.addAtom(atom);
      prevCui = atom.getConceptId();
    }
    if (cui != null) {
      cui.setName(getComputedPreferredName(cui));
      addConcept(cui);
      conceptIdMap.put(cui.getTerminology() + cui.getTerminologyId(),
          cui.getId());
      commitClearBegin();
    }
    results.close();
    Logger.getLogger(getClass()).info("  Add descriptors");
    objectCt = 0;

    // NOTE: Hibernate-specific to support iterating
    hQuery =
        session
            .createQuery(
                "select a from AtomJpa a where descriptorId is not null "
                    + "and descriptorId != '' order by terminology, descriptorId")
            .setReadOnly(true).setFetchSize(1000);
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
          dui.setName(getComputedPreferredName(dui));
          addDescriptor(dui);
          descriptorIdMap.put(dui.getTerminology() + dui.getTerminologyId(),
              dui.getId());
          logAndCommit(++objectCt);
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
      dui.addAtom(atom);
      prevDui = atom.getDescriptorId();
    }
    if (dui != null) {
      dui.setName(getComputedPreferredName(dui));
      addDescriptor(dui);
      descriptorIdMap.put(dui.getTerminology() + dui.getTerminologyId(),
          dui.getId());
      commitClearBegin();
    }
    results.close();

    Logger.getLogger(getClass()).info("  Add codes");
    objectCt = 0;
    // NOTE: Hibernate-specific to support iterating
    // Skip NOCODE
    hQuery =
        session
            .createQuery(
                "select a from AtomJpa a where codeId != 'NOCODE' "
                    + "and codeId is not null and codeId != '' "
                    + "order by terminology, codeId").setReadOnly(true)
            .setFetchSize(1000);
    results = hQuery.scroll(ScrollMode.FORWARD_ONLY);
    String prevCode = null;
    Code code = null;
    while (results.next()) {
      final Atom atom = (Atom) results.get()[0];
      if (atom.getCodeId() == null || atom.getCodeId().isEmpty()) {
        continue;
      }
      // skip where code == concept - problem because rels connect to the code
      // if (atom.getCodeId().equals(atom.getConceptId())) {
      // continue;
      // }
      // skip where code == descriptor
      // if (atom.getCodeId().equals(atom.getDescriptorId())) {
      // continue;
      // }
      if (prevCode == null || !prevCode.equals(atom.getCodeId())) {
        if (code != null) {
          // compute preferred name
          code.setName(getComputedPreferredName(code));
          addCode(code);
          codeIdMap.put(code.getTerminology() + code.getTerminologyId(),
              code.getId());
          logAndCommit(++objectCt);
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
      code.addAtom(atom);
      prevCode = atom.getCodeId();
    }
    if (code != null) {
      code.setName(getComputedPreferredName(code));
      addCode(code);
      codeIdMap.put(code.getTerminology() + code.getTerminologyId(),
          code.getId());
      commitClearBegin();
    }
    results.close();

    // NOTE: for efficiency and lack of use cases, we've temporarily
    // suspended the loading of LexicalClass and StringClass objects

    // // NOTE: atoms are not connected to lexical classes as there are
    // // currently no known uses for this.
    // Logger.getLogger(getClass()).info("  Add lexical classes");
    // objectCt = 0;
    // query =
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
    // logAndCommit(++objectCt);
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
    // logAndCommit(++objectCt);
    // }
    //
    // // NOTE: currently atoms are not loaded for string classes
    // // We simply load the objects themselves ( for SUI maintenance)
    // // There are no known use cases for having the atoms here.
    // Logger.getLogger(getClass()).info("  Add string classes");
    // objectCt = 0;
    // query =
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
    // logAndCommit(++objectCt);
    // }

    // commit
    commitClearBegin();

    // Update all root terminologies now that we know languages and names
    for (RootTerminology root : loadedRootTerminologies.values()) {
      updateRootTerminology(root);
    }

    // Update all root terminologies now that we know languages and names
    for (Terminology terminology : loadedTerminologies.values()) {
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
  public String getComputedPreferredName(AtomClass atomClass) throws Exception {
    final List<Atom> atoms = new ArrayList<>();
    for (Atom atom : atomClass.getAtoms()) {
      atoms.add(getAtom(atom.getId()));
    }
    atomClass.setAtoms(atoms);
    return super.getComputedPreferredName(atomClass);
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

  /* see superclass */
  @Override
  public void reset() throws Exception {
    // do nothing
  }

  /**
   * Fires a {@link ProgressEvent}.
   * @param pct percent done
   * @param note progress note
   */
  public void fireProgressEvent(int pct, String note) {
    ProgressEvent pe = new ProgressEvent(this, pct, pct, note);
    for (int i = 0; i < listeners.size(); i++) {
      listeners.get(i).updateProgress(pe);
    }
    Logger.getLogger(getClass()).info("    " + pct + "% " + note);
  }

  /* see superclass */
  @Override
  public void addProgressListener(ProgressListener l) {
    listeners.add(l);
  }

  /* see superclass */
  @Override
  public void removeProgressListener(ProgressListener l) {
    listeners.remove(l);
  }

  /* see superclass */
  @Override
  public void cancel() {
    throw new UnsupportedOperationException("cannot cancel.");
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
   * Returns the total elapsed time str.
   *
   * @param time the time
   * @return the total elapsed time str
   */
  @SuppressWarnings("boxing")
  private static String getTotalElapsedTimeStr(long time) {
    Long resultnum = (System.nanoTime() - time) / 1000000000;
    String result = resultnum.toString() + "s";
    resultnum = resultnum / 60;
    result = result + " / " + resultnum.toString() + "m";
    resultnum = resultnum / 60;
    result = result + " / " + resultnum.toString() + "h";
    return result;
  }

  /* see superclass */
  @Override
  public void close() throws Exception {
    super.close();
    readers = null;
  }

  /**
   * Commit clear begin transaction.
   *
   * @throws Exception the exception
   */
  private void commitClearBegin() throws Exception {
    commit();
    clear();
    beginTransaction();
  }

  /**
   * Log and commit.
   *
   * @param objectCt the object ct
   * @throws Exception the exception
   */
  private void logAndCommit(int objectCt) throws Exception {

    // log at regular intervals
    if (objectCt % logCt == 0) {
      Logger.getLogger(getClass()).info("    count = " + objectCt);
    }
    if (objectCt % commitCt == 0) {
      commitClearBegin();
    }
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
    return !moduleId.equals(coreModuleId) && !moduleId.equals(metadataModuleId);
  }

}
