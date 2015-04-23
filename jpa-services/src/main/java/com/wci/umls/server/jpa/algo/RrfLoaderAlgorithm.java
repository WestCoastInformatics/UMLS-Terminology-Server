/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.wci.umls.server.ReleaseInfo;
import com.wci.umls.server.algo.Algorithm;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.FieldedStringTokenizer;
import com.wci.umls.server.helpers.KeyValuePair;
import com.wci.umls.server.helpers.KeyValuePairList;
import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.jpa.ReleaseInfoJpa;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.AttributeJpa;
import com.wci.umls.server.jpa.content.CodeJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.DefinitionJpa;
import com.wci.umls.server.jpa.content.DescriptorJpa;
import com.wci.umls.server.jpa.content.LexicalClassJpa;
import com.wci.umls.server.jpa.content.SemanticTypeComponentJpa;
import com.wci.umls.server.jpa.content.StringClassJpa;
import com.wci.umls.server.jpa.helpers.PrecedenceListJpa;
import com.wci.umls.server.jpa.meta.AdditionalRelationshipTypeJpa;
import com.wci.umls.server.jpa.meta.AttributeNameJpa;
import com.wci.umls.server.jpa.meta.CitationJpa;
import com.wci.umls.server.jpa.meta.ContactInfoJpa;
import com.wci.umls.server.jpa.meta.LanguageJpa;
import com.wci.umls.server.jpa.meta.RelationshipTypeJpa;
import com.wci.umls.server.jpa.meta.RootTerminologyJpa;
import com.wci.umls.server.jpa.meta.SemanticTypeJpa;
import com.wci.umls.server.jpa.meta.TermTypeJpa;
import com.wci.umls.server.jpa.meta.TerminologyJpa;
import com.wci.umls.server.jpa.services.HistoryServiceJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.ComponentHasAttributes;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Definition;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.LexicalClass;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.model.content.SemanticTypeComponent;
import com.wci.umls.server.model.content.StringClass;
import com.wci.umls.server.model.meta.AdditionalRelationshipType;
import com.wci.umls.server.model.meta.AttributeName;
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
import com.wci.umls.server.services.helpers.ProgressEvent;
import com.wci.umls.server.services.helpers.ProgressListener;
import com.wci.umls.server.services.helpers.PushBackReader;

/**
 * Implementation of an algorithm to import RF2 snapshot data.
 */
public class RrfLoaderAlgorithm extends HistoryServiceJpa implements Algorithm {

  /** Listeners. */
  @SuppressWarnings("hiding")
  private List<ProgressListener> listeners = new ArrayList<>();

  /** The logging object ct threshold. */
  private final static int logCt = 2000;

  /** The commit count. */
  private final static int commitCt = 5000;

  /** The terminology. */
  private String terminology;

  /** The terminology version. */
  private String terminologyVersion;

  /** The release version. */
  private String releaseVersion;

  /** The release version date. */
  private Date releaseVersionDate;

  /** The readers. */
  private RrfReaders readers;

  /** The all metadata. */
  @SuppressWarnings("unused")
  private Map<String, Map<String, String>> allMetadata;

  /** The loader. */
  private final String loader = "loader";

  /** The published. */
  private final String published = "PUBLISHED";

  /** The loaded terminologies. */
  private Map<String, Terminology> loadedTerminologies = new HashMap<>();

  /** The loaded term types. */
  private Map<String, TermType> loadedTermTypes = new HashMap<>();

  /** The term id type map. */
  private Map<String, IdType> termIdTypeMap = new HashMap<>();

  /** The code map. */
  private Map<String, Code> codeMap = new HashMap<>();

  /** The concept map. */
  private Map<String, Concept> conceptMap = new HashMap<>();

  /** The descriptor map. */
  private Map<String, Descriptor> descriptorMap = new HashMap<>();

  /** The lexical class map. */
  private Map<String, LexicalClass> lexicalClassMap = new HashMap<>();

  /** The string class map. */
  private Map<String, StringClass> stringClassMap = new HashMap<>();

  /** The atom map. */
  private Map<String, Atom> atomMap = new HashMap<>();

  /** The relationship map. */
  private Map<String, Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes>> relationshipMap = new HashMap<>();

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
   * @param terminologyVersion the terminology version
   */
  public void setTerminologyVersion(String terminologyVersion) {
    this.terminologyVersion = terminologyVersion;
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
   * Sets the readers.
   *
   * @param readers the readers
   */
  public void setReaders(RrfReaders readers) {
    this.readers = readers;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.mapping.jpa.algo.Algorithm#compute()
   */
  /**
   * Compute.
   *
   * @throws Exception the exception
   */
  @Override
  public void compute() throws Exception {
    try {
      Logger.getLogger(getClass()).info("Start loading RRF");
      Logger.getLogger(getClass()).info("  terminology = " + terminology);
      Logger.getLogger(getClass()).info("  version = " + terminologyVersion);
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
      loadSrdef();

      // Load MRDOC data
      loadMrdoc();

      // Load MRSAB data
      loadMrsab();

      // Load precedence info
      loadMrrank();

      // Commit
      commit();
      clear();
      beginTransaction();

      // read for later use (perhaps)
      allMetadata = getAllMetadata(terminology, terminologyVersion);

      //
      // Load the content (TODO: subsets)
      //
      loadMrconso();

      // Commit
      commit();
      clear();
      beginTransaction();

      // Definitions
      // loadMrdef();

      // Semantic Types
      // loadMrsty();

      // Relationships

      // Attributes
      // loadMrsat

      // Add release info for individual terminology
      for (Map.Entry<String, String> entry : getTerminologyLatestVersions()
          .entrySet()) {
        Terminology terminology =
            getTerminology(entry.getKey(), entry.getValue());
        String version = entry.getValue();
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
          info.setTerminologyVersion(version);
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
        info.setTerminologyVersion(terminologyVersion);
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
   * Loads the semantic types.
   *
   * @throws Exception the exception
   */
  private void loadSrdef() throws Exception {
    Logger.getLogger(getClass()).info("  Load Semantic types");
    String line = null;
    int objectCt = 0;
    PushBackReader reader = readers.getReader(RrfReaders.Keys.SRDEF);
    while ((line = reader.readLine()) != null) {

      line = line.replace("\r", "");
      final String fields[] = FieldedStringTokenizer.split(line, "|", 10);

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
        sty.setTerminologyVersion(terminologyVersion);
        sty.setTreeNumber(fields[3]);
        sty.setTypeId(fields[1]);
        sty.setUsageNote(fields[6]);
        sty.setValue(fields[2]);

        sty.setLastModified(releaseVersionDate);
        sty.setLastModifiedBy(loader);
        sty.setPublished(true);
        sty.setPublishable(true);
        Logger.getLogger(getClass()).debug("    add semantic type - " + sty);
        addSemanticType(sty);
        // log at regular intervals
        if (++objectCt % logCt == 0) {
          Logger.getLogger(getClass()).info("    count = " + objectCt);
        }
      }
    }
  }

  /**
   * Loads the MRDOC data.
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
    while ((line = reader.readLine()) != null) {

      line = line.replace("\r", "");
      final String fields[] = FieldedStringTokenizer.split(line, "|", 4);

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
        atn.setLastModified(releaseVersionDate);
        atn.setLastModifiedBy(loader);
        atn.setTerminology(terminology);
        atn.setTerminologyVersion(terminologyVersion);
        atn.setPublished(true);
        atn.setPublishable(true);
        Logger.getLogger(getClass()).debug("    add attribute name - " + atn);
        addAttributeName(atn);
        atnSeen.add(fields[1]);
      }

      // Handle Languages
      if (fields[0].equals("LAT") && fields[2].equals("expanded_form")) {
        final Language lat = new LanguageJpa();
        lat.setAbbreviation(fields[1]);
        lat.setExpandedForm(fields[3]);
        lat.setLastModified(releaseVersionDate);
        lat.setLastModifiedBy(loader);
        lat.setTerminology(terminology);
        lat.setTerminologyVersion(terminologyVersion);
        lat.setPublished(true);
        lat.setPublishable(true);
        lat.setISO3Code(fields[1]);
        // TODO: need actual codes.
        lat.setISOCode(fields[1].toLowerCase().substring(0, 2));
        Logger.getLogger(getClass()).debug("    add language - " + lat);
        addLanguage(lat);
      }

      // Handle AdditionalRelationshipLabel
      if (fields[0].equals("RELA") && fields[2].equals("expanded_form")) {
        final AdditionalRelationshipType rela =
            new AdditionalRelationshipTypeJpa();
        rela.setAbbreviation(fields[1]);
        rela.setExpandedForm(fields[3]);
        rela.setLastModified(releaseVersionDate);
        rela.setLastModifiedBy(loader);
        rela.setTerminology(terminology);
        rela.setTerminologyVersion(terminologyVersion);
        rela.setPublished(true);
        rela.setPublishable(true);
        // DL fields are all left false, with no domain/range
        // no equivalent types or supertypes included
        relaMap.put(fields[1], rela);
        Logger.getLogger(getClass()).debug(
            "    add additional relationship type - " + rela);
      }
      if (fields[0].equals("RELA") && fields[2].equals("rela_inverse")) {
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
      if (fields[0].equals("REL") && fields[2].equals("expanded_form")
          && !fields[0].equals("SIB")) {
        final RelationshipType rel = new RelationshipTypeJpa();
        rel.setAbbreviation(fields[1]);
        rel.setExpandedForm(fields[3]);
        rel.setLastModified(releaseVersionDate);
        rel.setLastModifiedBy(loader);
        rel.setTerminology(terminology);
        rel.setTerminologyVersion(terminologyVersion);
        rel.setPublished(true);
        rel.setPublishable(true);
        rel.setGroupingType(true);
        relMap.put(fields[1], rel);
        Logger.getLogger(getClass())
            .debug("    add relationship type - " + rel);
      }
      if (fields[0].equals("REL") && fields[2].equals("rel_inverse")
        && !fields[0].equals("SIB")) {
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

      if (fields[0].equals("TTY") && fields[2].equals("expanded_form")) {
        final TermType tty = new TermTypeJpa();
        tty.setAbbreviation(fields[1]);
        tty.setExpandedForm(fields[3]);
        tty.setLastModified(releaseVersionDate);
        tty.setLastModifiedBy(loader);
        tty.setTerminology(terminology);
        tty.setTerminologyVersion(terminologyVersion);
        tty.setPublished(true);
        tty.setPublishable(true);
        tty.setCodeVariantType(CodeVariantType.UNDEFINED);
        // based on TTY class (set later)
        tty.setHierarchicalType(false);
        tty.setNameVariantType(NameVariantType.UNDEFINED);
        // TODO: set based on MRRANK
        tty.setSuppressible(false);
        tty.setStyle(TermTypeStyle.UNDEFINED);
        tty.setUsageType(UsageType.UNDEFINED);
        ttyMap.put(fields[1], tty);
      }
      if (fields[0].equals("TTY") && fields[2].equals("tty_class")) {
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

      // log at regular intervals
      if (++objectCt % logCt == 0) {
        Logger.getLogger(getClass()).info("    count = " + objectCt);
      }
    }

    // Add TTYs when done
    for (TermType tty : ttyMap.values()) {
      addTermType(tty);
      loadedTermTypes.put(tty.getAbbreviation(), tty);
    }

  }

  /**
   * Load definitions.
   *
   * @throws Exception the exception
   */
  private void loadMrdef() throws Exception {
    Logger.getLogger(getClass()).info("  Load MRDEF data");
    String line = null;
    PushBackReader reader = readers.getReader(RrfReaders.Keys.MRDEF);
    while ((line = reader.readLine()) != null) {

      line = line.replace("\r", "");
      final String fields[] = FieldedStringTokenizer.split(line, "|", 8);

      // Field Description
      // 0 CUI Unique identifier for concept
      // 1 AUI Unique identifier for atom - variable length field, 8 or 9
      // characters
      // 2 ATUI Unique identifier for attribute
      // 3 SATUI Source asserted attribute identifier [optional-present if it
      // exists]
      // 4 SAB Abbreviated source name (SAB) of the source of the definition
      // 5 DEF Definition
      // 6 SUPPRESS Suppressible flag. Values = O, E, Y, or N. Reflects the
      // suppressible status of the attribute; not yet in use. See also SUPPRESS
      // in MRCONSO.RRF, MRREL.RRF, and MRSAT.RRF.
      // 7 CVF Content View Flag. Bit field used to flag rows included in
      // Content View. This field is a varchar field to maximize the number of
      // bits available for use.
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

      Definition def = new DefinitionJpa();

      Atom atom = atomMap.get(fields[1]);
      atom.addDefinition(def);

      def.putAlternateTerminologyId(terminology, fields[2]);
      def.setTerminologyId(fields[3]);

      def.setLastModified(releaseVersionDate);
      def.setLastModifiedBy(loader);
      def.setTerminology(fields[4]);
      if (loadedTerminologies.get(fields[4]) == null) {
        Logger.getLogger(getClass()).info("MISSING TERMINONLOGY " + fields[4]);
      } else {
        def.setTerminologyVersion(loadedTerminologies.get(fields[4])
            .getTerminologyVersion());
      }
      def.setValue(fields[5]);
      if (fields[6].equals("O")) {
        def.setObsolete(true);
      } else if (fields[6].equals("E") || fields[6].equals("Y")) {
        def.setSuppressible(true);
      }

      addDefinition(def);
    }

  }

  /**
   * Load attributes.
   *
   * @throws Exception the exception
   */
  private void loadMrsat() throws Exception {
    Logger.getLogger(getClass()).info("  Load MRSAT data");
    String line = null;
    PushBackReader reader = readers.getReader(RrfReaders.Keys.MRSAT);
    while ((line = reader.readLine()) != null) {

      line = line.replace("\r", "");
      final String fields[] = FieldedStringTokenizer.split(line, "|", 12);

      // Field Description
      // 0 CUI Unique identifier for concept (if METAUI is a relationship
      // identifier, this will be CUI1 for that relationship)
      // 1 LUI Unique identifier for term (optional - present for atom
      // attributes, but not for relationship attributes)
      // 2 SUI Unique identifier for string (optional - present for atom
      // attributes, but not for relationship attributes)
      // 3 METAUI Metathesaurus atom identifier (will have a leading A) or
      // Metathesaurus relationship identifier (will have a leading R) or blank
      // if it is a concept attribute.
      // 4 STYPE The name of the column in MRCONSO.RRF or MRREL.RRF that
      // contains the identifier to which the attribute is attached, i.e. AUI,
      // CODE, CUI, RUI, SCUI, SDUI.
      // 5 CODE Most useful source asserted identifier (if the source vocabulary
      // contains more than one) or a Metathesaurus-generated source entry
      // identifier (if the source vocabulary has none). Optional - present if
      // METAUI is an AUI.
      // 6 ATUI Unique identifier for attribute
      // 7 SATUI Source asserted attribute identifier (optional - present if it
      // exists)
      // 8 ATN Attribute name. Possible values appear in MRDOC.RRF and are
      // described on the Attribute Names page.
      // 9 SAB Abbreviated source name (SAB).
      // 10 ATV Attribute value described under specific attribute name on the
      // Attributes Names page. A few attribute values exceed 1,000 characters.
      // Many of the abbreviations used in attribute values are explained in
      // MRDOC.RRF and included on the Abbreviations Used in Data Elements page.
      // 11 SUPPRESS Suppressible flag. Values = O, E, Y, or N. Reflects the
      // suppressible status of the attribute. See also SUPPRESS in MRCONSO.RRF,
      // MRDEF.RRF, and MRREL.RRF.
      // 12 CVF Content View Flag. Bit field used to flag rows included in
      // Content View. This field is a varchar field to maximize the number of
      // bits available for use.
      // e.g.
      // C0001175|L0001175|S0010339|A0019180|SDUI|D000163|AT38209082||FX|MSH|D015492|N||
      // C0001175|L0001175|S0354232|A2922342|AUI|62479008|AT24600515||DESCRIPTIONSTATUS|SNOMEDCT|0|N||
      // C0001175|L0001842|S0011877|A15662389|CODE|T1|AT100434486||URL|MEDLINEPLUS|http://www.nlm.nih.gov/medlineplus/aids.html|N||
      // C0001175|||R54775538|RUI||AT63713072||CHARACTERISTICTYPE|SNOMEDCT|0|N||
      // C0001175|||R54775538|RUI||AT69142126||REFINABILITY|SNOMEDCT|1|N||

      Attribute att = new AttributeJpa();

      if (fields[4].equals("AUI")) {
        Atom atom = atomMap.get(fields[3]);
        atom.addAttribute(att);
      } else if (fields[4].equals("RUI")) {
        Relationship relationship = relationshipMap.get(fields[3]);
        relationship.addAttribute(att);
      } else if (fields[4].equals("CODE")) {
        Code code = codeMap.get(atomMap.get(fields[3]).getCodeId() + fields[9]);
        code.addAttribute(att);
      } else if (fields[4].equals("CUI")) {
        Concept concept = conceptMap.get(fields[0] + terminology);
        concept.addAttribute(att);
      } else if (fields[4].equals("SDUI")) {
        Descriptor descriptor =
            descriptorMap.get(atomMap.get(fields[3]).getDescriptorId());
        descriptor.addAttribute(att);
      } else if (fields[4].equals("SCUI")) {
        // TODO: turn this back on
        // Concept concept =
        // conceptMap.get(atomMap.get(fields[3]).getConceptId());
        // concept.addAttribute(att);
      }
      // fields[5] CODE not used - redundant

      att.putAlternateTerminologyId(terminology, fields[6]);
      att.setTerminologyId(fields[7]);
      // fields[7] SATUI not used
      att.setLastModified(releaseVersionDate);
      att.setLastModifiedBy(loader);
      att.setName(fields[8]);
      att.setTerminology(fields[9]);
      if (loadedTerminologies.get(fields[9]) == null) {
        Logger.getLogger(getClass()).info("MISSING TERMINONLOGY " + fields[9]);
      } else {
        att.setTerminologyVersion(loadedTerminologies.get(fields[9])
            .getTerminologyVersion());
      }
      att.setValue(fields[10]);
      if (fields[11].equals("O")) {
        att.setObsolete(true);
      } else if (fields[11].equals("E") || fields[11].equals("Y")) {
        att.setSuppressible(true);
      }

      att.setSuppressible(fields[11].equals("Y") ? true : false);
      att.setObsolete(fields[11].equals("O") ? true : false);

      // addAttribute(att);

      // make set of all atoms that got an additional attribute
      // call updateAtom on all of them
      // same for description, rel, etc.
    }
  }

  /**
   * Load semantic types.
   *
   * @throws Exception the exception
   */
  private void loadMrsty() throws Exception {
    Logger.getLogger(getClass()).info("  Load MRSTY data");
    String line = null;
    PushBackReader reader = readers.getReader(RrfReaders.Keys.MRSTY);
    while ((line = reader.readLine()) != null) {

      line = line.replace("\r", "");
      final String fields[] = FieldedStringTokenizer.split(line, "|", 6);

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

      SemanticTypeComponent sty = new SemanticTypeComponentJpa();
      Concept concept = conceptMap.get(fields[0]);
      List<SemanticTypeComponent> stys = concept.getSemanticTypes();
      stys.add(sty);
      concept.setSemanticTypes(stys);

      sty.setSemanticType(fields[1]);
      // fields 2 and 3 are already read from SRDEF
      sty.setTerminologyId(fields[4]);
      sty.setLastModified(releaseVersionDate);
      sty.setLastModifiedBy(loader);

      addSemanticTypeComponent(sty);
    }

  }

  /**
   * Load terminologies.
   *
   * @throws Exception the exception
   */
  private void loadMrsab() throws Exception {
    Logger.getLogger(getClass()).info("  Load MRSAB data");
    String line = null;
    Map<String, RootTerminology> rootTerminologies = new HashMap<>();
    Map<String, Terminology> terminologies = new HashMap<>();
    PushBackReader reader = readers.getReader(RrfReaders.Keys.MRSAB);
    while ((line = reader.readLine()) != null) {

      line = line.replace("\r", "");
      final String fields[] = FieldedStringTokenizer.split(line, "|", 25);

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

      // SKIP SABIN=N
      if (fields[23].equals("N")) {
        continue;
      }
      
      Terminology term = new TerminologyJpa();

      term.setAssertsRelDirection(false); // TODO: extract this from MRRREL
      term.setCitation(new CitationJpa(fields[24]));
      term.setCurrent(fields[21].equals("Y"));
      if (!fields[8].equals("")) {
        term.setEndDate(ConfigUtility.DATE_FORMAT2.parse(fields[8]));
      }

      // TODO: Set properly after loading atoms
      term.setOrganizingClassType(IdType.CODE);
      term.setPreferredName(fields[4]);
      if (!fields[7].equals("")) {
        term.setStartDate(ConfigUtility.DATE_FORMAT2.parse(fields[7]));
      }

      term.setLastModified(releaseVersionDate);
      term.setLastModifiedBy(loader);
      term.setTerminology(fields[3]);
      term.setTerminologyVersion(fields[6]);
      term.setDescriptionLogicTerminology(false);
      terminologies.put(fields[2], term);

      if (!rootTerminologies.containsKey(fields[3])) {
        RootTerminology root = new RootTerminologyJpa();
        root.setAcquisitionContact(null); // no data for this in MRSAB
        root.setContentContact(new ContactInfoJpa(fields[12]));
        root.setFamily(fields[5]);
        root.setHierarchicalName(""); // TODO: extract this from MRCONSO.
        // root.setLanguage(getLanguages(terminology, terminologyVersion));
        root.setLicenseContact(new ContactInfoJpa(fields[11]));
        root.setPolyhierarchy(fields[16].contains("MULTIPLE"));
        root.setPreferredName(fields[4]);
        root.setRestrictionLevel(Integer.parseInt(fields[13]));
        root.setShortName(""); // TODO: extract this from MRCONSO.
        root.setTerminology(fields[3]);
        root.setLastModified(releaseVersionDate);
        root.setLastModifiedBy(loader);
        addRootTerminology(root);
        rootTerminologies.put(fields[3], root);
      }

      RootTerminology root = rootTerminologies.get(fields[3]);
      term.setRootTerminology(root);
      addTerminology(term);
      loadedTerminologies.put(term.getTerminology(), term);
    }

  }

  /**
   * Load PrecedenceList.
   *
   * @throws Exception the exception
   */
  private void loadMrrank() throws Exception {

    PrecedenceList list = new PrecedenceListJpa();
    list.setDefaultList(true);

    List<KeyValuePair> lkvp = new ArrayList<>();

    Logger.getLogger(getClass()).info("  Load MRRANK data");
    String line = null;
    PushBackReader reader = readers.getReader(RrfReaders.Keys.MRRANK);
    while ((line = reader.readLine()) != null) {

      line = line.replace("\r", "");
      final String fields[] = FieldedStringTokenizer.split(line, "|", 4);

      // FIELDS
      // 0 RNK
      // 1 SAB
      // 2 TTY
      // 3 SUPPRESS (ignore this)
      // e.g.
      // 0586|MTH|PN|N|

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
    list.setLastModified(releaseVersionDate);
    list.setLastModifiedBy(loader);
    list.setName("DEFAULT");
    addPrecedenceList(list);
  }

  /**
   * Load MRCONSO.RRF. This is responsible for loading atoms and atom classes.
   *
   * @throws Exception the exception
   */
  private void loadMrconso() throws Exception {
    Logger.getLogger(getClass()).info("  Load MRCONSO");

    String line = null;

    int objectCt = 0;
    PushBackReader reader = readers.getReader(RrfReaders.Keys.MRCONSO);
    while ((line = reader.readLine()) != null) {

      line = line.replace("\r", "");
      final String fields[] = FieldedStringTokenizer.split(line, "|");

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
      // 9 SDUI
      // 10 SCUI
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

      Atom atom = new AtomJpa();
      atom.setLanguage(fields[3]);
      atom.setLastModified(releaseVersionDate);
      atom.setLastModifiedBy(loader);
      atom.setObsolete(fields[16].equals("O"));
      atom.setSuppressible(!fields[16].equals("N"));
      atom.setPublished(true);
      atom.setPublishable(true);
      atom.setTerm(fields[14]);
      atom.setTerminology(fields[11]);
      if (loadedTerminologies.get(fields[11]) == null) {
        Logger.getLogger(getClass()).info("MISSING TERMINONLOGY " + fields[11]);
      } else {
        atom.setTerminologyVersion(loadedTerminologies.get(fields[11])
            .getTerminologyVersion());
      }
      atom.putAlternateTerminologyId(terminology, fields[7]);
      atom.setTerminologyId(fields[8]);
      atom.setTermType(fields[12]);
      atom.setWorkflowStatus(published);

      atom.setCodeId(fields[13]);
      atom.setDescriptorId(fields[9]);
      atom.setConceptId(fields[10]);
      atom.setStringClassId(fields[5]);
      atom.setLexicalClassId(fields[3]);
      atom.setCodeId(fields[13]);

      // Determine organizing class type for terminology
      if (!atom.getDescriptorId().equals("")) {
        termIdTypeMap.put(atom.getTerminology(), IdType.DESCRIPTOR);
      } else if (!atom.getDescriptorId().equals("")) {
        termIdTypeMap.put(atom.getTerminology(), IdType.CONCEPT);
      } // OTHERWISE it remains "CODE"
      atomMap.put(atom.getTerminologyId(), atom);

      // CUI
      Concept cui = null;
      if (conceptMap.containsKey(fields[0])) {
        cui = conceptMap.get(fields[0]);
      } else if (!fields[0].equals("")) {
        cui = new ConceptJpa();
        cui.setLastModified(releaseVersionDate);
        cui.setLastModifiedBy(loader);
        cui.setPublished(true);
        cui.setPublishable(true);
        cui.setTerminology(terminology);
        cui.setTerminologyId(fields[0]);
        cui.setTerminologyVersion(terminologyVersion);
        cui.setWorkflowStatus(published);
        cui.setDefaultPreferredName("TBD");
        conceptMap.put(cui.getTerminologyId(), cui);
      }
      if (cui != null) {
        cui.addAtom(atom);
        atom.putConceptTerminologyId(terminology, cui.getTerminologyId());
      }

      // SCUI
      Concept scui = null;
      if (conceptMap.containsKey(fields[10])) {
        scui = conceptMap.get(fields[10]);
      } else if (!fields[10].equals("")) {
        scui = new ConceptJpa();
        scui.setLastModified(releaseVersionDate);
        scui.setLastModifiedBy(loader);
        scui.setPublished(true);
        scui.setPublishable(true);
        scui.setTerminology(fields[11]);
        scui.setTerminologyId(fields[10]);
        scui.setTerminologyVersion(loadedTerminologies.get(fields[11])
            .getTerminologyVersion());
        scui.setWorkflowStatus(published);
        scui.setDefaultPreferredName("TBD");
        conceptMap.put(scui.getTerminologyId(), scui);
      }
      if (scui != null) {
        scui.addAtom(atom);
      }

      // SDUI
      Descriptor sdui = null;
      if (descriptorMap.containsKey(fields[9])) {
        sdui = descriptorMap.get(fields[9]);
      } else if (!fields[9].equals("")) {
        sdui = new DescriptorJpa();
        sdui.setLastModifiedBy(loader);
        sdui.setLastModified(releaseVersionDate);
        sdui.setPublished(true);
        sdui.setPublishable(true);
        sdui.setTerminology(fields[11]);
        sdui.setTerminologyId(fields[9]);
        sdui.setTerminologyVersion(loadedTerminologies.get(fields[11])
            .getTerminologyVersion());
        sdui.setWorkflowStatus(published);
        sdui.setDefaultPreferredName("TBD");
        descriptorMap.put(sdui.getTerminologyId(), sdui);
      }
      if (sdui != null) {
        sdui.addAtom(atom);
      }

      // CODE
      Code code = null;
      if (codeMap.containsKey(fields[13] + fields[11])) {
        code = codeMap.get(fields[13] + fields[11]);
      } else if (!fields[13].equals("")) {
        code = new CodeJpa();
        code.setLastModified(releaseVersionDate);
        code.setLastModifiedBy(loader);
        code.setPublished(true);
        code.setPublishable(true);
        code.setTerminology(fields[11]);
        code.setTerminologyId(fields[13]);
        code.setTerminologyVersion(loadedTerminologies.get(fields[11])
            .getTerminologyVersion());
        code.setWorkflowStatus(published);
        code.setDefaultPreferredName("TBD");
        codeMap.put(fields[13] + fields[11], code);
      }
      if (code != null) {
        code.addAtom(atom);
      }

      LexicalClass lui = null;
      if (lexicalClassMap.containsKey(fields[3])) {
        lui = lexicalClassMap.get(fields[3]);
      } else if (!fields[3].equals("")) {
        lui = new LexicalClassJpa();
        lui.setLastModified(releaseVersionDate);
        lui.setLastModifiedBy(loader);
        lui.setPublished(true);
        lui.setPublishable(true);
        lui.setTerminology(terminology);
        lui.setTerminologyId(fields[3]);
        lui.setTerminologyVersion(terminologyVersion);
        lexicalClassMap.put(lui.getTerminologyId(), lui);
        lui.setNormalizedString("TBD");
        lui.setWorkflowStatus(published);
        lui.setDefaultPreferredName("TBD");
      }
      if (lui != null) {
        lui.addAtom(atom);
      }

      StringClass sui = null;
      if (stringClassMap.containsKey(fields[5])) {
        sui = stringClassMap.get(fields[5]);
      } else if (!fields[5].equals("")) {
        sui = new StringClassJpa();
        sui.setLastModified(releaseVersionDate);
        sui.setLastModifiedBy(loader);
        sui.setPublished(true);
        sui.setPublishable(true);
        sui.setTerminology(terminology);
        sui.setTerminologyId(fields[5]);
        sui.setTerminologyVersion(terminologyVersion);
        stringClassMap.put(sui.getTerminologyId(), sui);
        sui.setWorkflowStatus(published);
        // prefered name is just the string
        sui.setDefaultPreferredName(atom.getTerm());
        addStringClass(sui);
      }
      if (sui != null) {
        sui.addAtom(atom);
      }

      // Add atoms and commit periodically
      addAtom(atom);
      // log at regular intervals
      if (objectCt % logCt == 0) {
        Logger.getLogger(getClass()).info("    count = " + objectCt);
      }
      if (++objectCt % commitCt == 0) {
        commit();
        clear();
        beginTransaction();
      }

    }

    // Set the terminology organizing class types
    for (Terminology terminology : loadedTerminologies.values()) {
      final IdType idType = termIdTypeMap.get(terminology.getTerminology());
      if (idType != null && idType != IdType.CODE) {
        terminology.setOrganizingClassType(idType);
        updateTerminology(terminology);
      }
    }

    objectCt = 0;
    // Set default preferred names
    for (Concept concept : conceptMap.values()) {
      concept.setDefaultPreferredName(getComputedPreferredName(concept));
      addConcept(concept);
      if (objectCt % logCt == 0) {
        Logger.getLogger(getClass()).info("    count = " + objectCt);
      }
      if (++objectCt % commitCt == 0) {
        commit();
        clear();
        beginTransaction();
      }
    }
    for (Descriptor descriptor : descriptorMap.values()) {
      descriptor.setDefaultPreferredName(getComputedPreferredName(descriptor));
      addDescriptor(descriptor);
      if (objectCt % logCt == 0) {
        Logger.getLogger(getClass()).info("    count = " + objectCt);
      }
      if (++objectCt % commitCt == 0) {
        commit();
        clear();
        beginTransaction();
      }
    }
    for (Code code : codeMap.values()) {
      code.setDefaultPreferredName(getComputedPreferredName(code));
      addCode(code);
      if (objectCt % logCt == 0) {
        Logger.getLogger(getClass()).info("    count = " + objectCt);
      }
      if (++objectCt % commitCt == 0) {
        commit();
        clear();
        beginTransaction();
      }
    }
    for (LexicalClass lui : lexicalClassMap.values()) {
      lui.setDefaultPreferredName(getComputedPreferredName(lui));
      addLexicalClass(lui);
      if (objectCt % logCt == 0) {
        Logger.getLogger(getClass()).info("    count = " + objectCt);
      }
      if (++objectCt % commitCt == 0) {
        commit();
        clear();
        beginTransaction();
      }
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.mapping.jpa.algo.Algorithm#reset()
   */
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.ts.jpa.services.helper.ProgressReporter#addProgressListener
   * (org.ihtsdo.otf.ts.jpa.services.helper.ProgressListener)
   */
  /**
   * Adds the progress listener.
   *
   * @param l the l
   */
  @Override
  public void addProgressListener(ProgressListener l) {
    listeners.add(l);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.ts.jpa.services.helper.ProgressReporter#removeProgressListener
   * (org.ihtsdo.otf.ts.jpa.services.helper.ProgressListener)
   */
  /**
   * Removes the progress listener.
   *
   * @param l the l
   */
  @Override
  public void removeProgressListener(ProgressListener l) {
    listeners.remove(l);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.jpa.algo.Algorithm#cancel()
   */
  /**
   * Cancel.
   */
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

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.jpa.services.RootServiceJpa#close()
   */
  @Override
  public void close() throws Exception {
    super.close();
    readers = null;
  }
}
