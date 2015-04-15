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
import com.wci.umls.server.jpa.ReleaseInfoJpa;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.CodeJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.DescriptorJpa;
import com.wci.umls.server.jpa.content.LexicalClassJpa;
import com.wci.umls.server.jpa.content.StringClassJpa;
import com.wci.umls.server.jpa.meta.AdditionalRelationshipTypeJpa;
import com.wci.umls.server.jpa.meta.AttributeNameJpa;
import com.wci.umls.server.jpa.meta.CitationJpa;
import com.wci.umls.server.jpa.meta.ContactInfoJpa;
import com.wci.umls.server.jpa.meta.IdentifierTypeJpa;
import com.wci.umls.server.jpa.meta.LanguageJpa;
import com.wci.umls.server.jpa.meta.RelationshipTypeJpa;
import com.wci.umls.server.jpa.meta.RootTerminologyJpa;
import com.wci.umls.server.jpa.meta.SemanticTypeJpa;
import com.wci.umls.server.jpa.meta.TermTypeJpa;
import com.wci.umls.server.jpa.meta.TerminologyJpa;
import com.wci.umls.server.jpa.services.HistoryServiceJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.LexicalClass;
import com.wci.umls.server.model.content.StringClass;
import com.wci.umls.server.model.meta.AdditionalRelationshipType;
import com.wci.umls.server.model.meta.AttributeName;
import com.wci.umls.server.model.meta.CodeVariantType;
import com.wci.umls.server.model.meta.IdentifierType;
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
  @SuppressWarnings("unused")
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
      loadSemanticTypes();

      // Load MRDOC data
      loadAbbreviations();

      // Load MRSAB data
      loadTerminologies();

      commit();
      clear();
      beginTransaction();

      // read for later use
      allMetadata = getAllMetadata(terminology, terminologyVersion);

      //
      // Load the content
      //
      loadMrconso();

      commit();
      clear();
      beginTransaction();

      // TODO: cache any/all objects to which data can be attached.
      // if attachign to @IndexEmbedded stuff, need to read it all in.

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
          info.setDescription(terminology + " " + version + " release");
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
      Map<String, Integer> stats = getComponentStats(null, null);
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
   * @throws Exception
   */
  private void loadSemanticTypes() throws Exception {
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
        // regularly commit at intervals
        if (++objectCt % logCt == 0) {
          Logger.getLogger(getClass()).info("    count = " + objectCt);
        }
      }
    }
  }

  /**
   * Loads the MRDOC data.
   * @throws Exception
   */
  private void loadAbbreviations() throws Exception {
    Logger.getLogger(getClass()).info("  Load MRDOC abbreviation types");
    String line = null;
    Set<String> idTypeSeen = new HashSet<>();
    Set<String> atnSeen = new HashSet<>();
    Map<String, RelationshipType> relMap = new HashMap<>();
    Map<String, String> inverseRelMap = new HashMap<>();
    Map<String, AdditionalRelationshipType> relaMap = new HashMap<>();
    Map<String, String> inverseRelaMap = new HashMap<>();
    Map<String, TermType> ttyMap = new HashMap<>();
    PushBackReader reader = readers.getReader(RrfReaders.Keys.MRDOC);
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

      // Handle IdentifierTypes
      if (fields[2].equals("expanded_form")
          && (fields[0].equals("FROMTYPE") || fields[0].equals("TOTYPE")
              || fields[0].equals("STYPE") || fields[0].equals("STYPE1") || fields[0]
                .equals("STYPE2")) && !idTypeSeen.contains(fields[1])) {
        final IdentifierType idType = new IdentifierTypeJpa();
        idType.setAbbreviation(fields[1]);
        idType.setExpandedForm(fields[3]);
        idType.setLastModified(releaseVersionDate);
        idType.setLastModifiedBy(loader);
        idType.setTerminology(terminology);
        idType.setTerminologyVersion(terminologyVersion);
        idType.setPublished(true);
        idType.setPublishable(true);
        Logger.getLogger(getClass()).debug(
            "    add identifier type - " + idType);
        addIdentifierType(idType);
        idTypeSeen.add(fields[1]);
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
          rela2.setInverseType(rela2);
          addAdditionalRelationshipType(rela1);
          addAdditionalRelationshipType(rela2);
        }
      }

      // Handle RelationshipLabel
      if (fields[0].equals("REL") && fields[2].equals("expanded_form")) {
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
      if (fields[0].equals("REL") && fields[2].equals("rel_inverse")) {
        inverseRelMap.put(fields[1], fields[3]);
        if (inverseRelMap.containsKey(fields[1])
            && inverseRelMap.containsKey(fields[3])) {
          RelationshipType rel1 = relMap.get(fields[1]);
          RelationshipType rel2 = relMap.get(fields[3]);
          rel1.setInverse(rel2);
          rel2.setInverse(rel2);
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
        // based on TTY class
        tty.setHierarchicalType(false);
        tty.setNameVariantType(NameVariantType.UNDEFINED);
        // based on tty_class
        // tty.setObsolete("TODO");
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

    }

    // Add TTYs when done
    for (TermType tty : ttyMap.values()) {
      addTermType(tty);
    }

  }

  /**
   * Load terminologies.
   * @throws Exception
   */
  private void loadTerminologies() throws Exception {
    Logger.getLogger(getClass()).info("  Load MRSAB data");
    String line = null;
    Map<String, RootTerminology> rootTerminologies = new HashMap<>();
    Map<String, Terminology> terminologies = new HashMap<>();
    PushBackReader reader = readers.getReader(RrfReaders.Keys.MRSAB);
    while ((line = reader.readLine()) != null) {

      line = line.replace("\r", "");
      final String fields[] = FieldedStringTokenizer.split(line, "|", 26);

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
      // 23 SABIN
      // 24 SSN
      // 25 SCIT
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

      Terminology term = new TerminologyJpa();

      term.setAssertsRelDirection(false); // TODO: extract this from MRRREL
      term.setCitation(new CitationJpa(fields[25]));
      if (!fields[8].equals("")) {
        term.setEndDate(ConfigUtility.DATE_FORMAT2.parse(fields[8]));
      }

      term.setOrganizingClassType(null); // TODO; handle with config file
                                         // (later)
      term.setPreferredName(fields[4]);
      if (!fields[7].equals("")) {
        term.setStartDate(ConfigUtility.DATE_FORMAT2.parse(fields[7]));
      }
      term.setLastModified(releaseVersionDate);
      term.setLastModifiedBy(loader);
      term.setTerminology(fields[2]);
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

    }

  }

  /**
   * Load MRCONSO.RRF. This is responsible for loading atoms and atom classes.
   *
   * @throws Exception the exception
   */
  private void loadMrconso() throws Exception {
    Logger.getLogger(getClass()).info("  Load MRCONSO");

    String line = null;
    Map<String, Code> codeMap = new HashMap<>();
    Map<String, Concept> conceptMap = new HashMap<>();
    Map<String, Descriptor> descriptorMap = new HashMap<>();
    Map<String, LexicalClass> lexicalClassMap = new HashMap<>();
    Map<String, StringClass> stringClassMap = new HashMap<>();
    @SuppressWarnings("unused")
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
      // TODO: get root terminology and get current version and set it.
      atom.setTerminologyVersion("TODO");
      atom.setTerminologyId(fields[7]);
      atom.setTermType(fields[12]);
      atom.setWorkflowStatus(published);

      atom.setCodeId(fields[13]);
      atom.setDescriptorId(fields[9]);
      atom.setStringClassId(fields[5]);
      atom.setLexicalClassId(fields[3]);
      atom.setCodeId(fields[13]);
      objectCt++;

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
        conceptMap.put(cui.getTerminologyId(), cui);
        addConcept(cui);
      }
      if (cui != null) {
        cui.addAtom(atom);
        atom.addConcept(cui);
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
        // TODO: get root terminology and get current version and set it.
        scui.setTerminologyVersion("TODO");
        scui.setWorkflowStatus(published);
        conceptMap.put(scui.getTerminologyId(), scui);
        addConcept(scui);
      }
      if (scui != null) {
        scui.addAtom(atom);
        atom.addConcept(scui);
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
        // TODO: get root terminology and get current version and set it.
        sdui.setTerminologyVersion("TODO");
        sdui.setWorkflowStatus(published);
        descriptorMap.put(sdui.getTerminologyId(), sdui);
        addDescriptor(sdui);
      }
      if (sdui != null) {
        sdui.addAtom(atom);
        atom.setDescriptorId(sdui.getTerminologyId());
      }

      // CODE
      Code code = null;
      if (codeMap.containsKey(fields[13])) {
        code = codeMap.get(fields[13]);
      } else if (!fields[13].equals("")) {
        code = new CodeJpa();
        code.setLastModified(releaseVersionDate);
        code.setLastModifiedBy(loader);
        code.setPublished(true);
        code.setPublishable(true);
        code.setTerminology(fields[11]);
        code.setTerminologyId(fields[13]);
        // TODO: get root terminology and get current version and set it.
        code.setTerminologyVersion("TODO");
        code.setWorkflowStatus(published);
        codeMap.put(code.getTerminologyId(), code);
        addCode(code);
      }
      if (code != null) {
        code.addAtom(atom);
        atom.setCodeId(code.getTerminologyId());
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
        addLexicalClass(lui);
      }
      if (lui != null) {
        lui.addAtom(atom);
        atom.setLexicalClassId(lui.getTerminologyId());
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
        sui.setString(fields[14]);
        sui.setWorkflowStatus(published);
        addStringClass(sui);
      }
      if (sui != null) {
        sui.addAtom(atom);
        atom.setStringClassId(sui.getTerminologyId());
      }

      addAtom(atom);

    }

    // TODO: Set default preferred names

    // TODO: then iterate through and add atoms, then atom class data structure
    // commiting every so often.
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
