/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.wci.umls.server.AlgorithmParameter;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.FieldedStringTokenizer;
import com.wci.umls.server.jpa.AlgorithmParameterJpa;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.meta.AdditionalRelationshipTypeJpa;
import com.wci.umls.server.jpa.meta.AttributeNameJpa;
import com.wci.umls.server.jpa.meta.CitationJpa;
import com.wci.umls.server.jpa.meta.ContactInfoJpa;
import com.wci.umls.server.jpa.meta.RootTerminologyJpa;
import com.wci.umls.server.jpa.meta.TermTypeJpa;
import com.wci.umls.server.jpa.meta.TerminologyJpa;
import com.wci.umls.server.model.meta.AdditionalRelationshipType;
import com.wci.umls.server.model.meta.AttributeName;
import com.wci.umls.server.model.meta.CodeVariantType;
import com.wci.umls.server.model.meta.Language;
import com.wci.umls.server.model.meta.NameVariantType;
import com.wci.umls.server.model.meta.RootTerminology;
import com.wci.umls.server.model.meta.TermType;
import com.wci.umls.server.model.meta.TermTypeStyle;
import com.wci.umls.server.model.meta.Terminology;
import com.wci.umls.server.model.meta.UsageType;
import com.wci.umls.server.services.RootService;
import com.wci.umls.server.services.helpers.PushBackReader;

/**
 * Implementation of an algorithm to import metadata.
 */
public class MetadataLoaderAlgorithm extends AbstractAlgorithm {

  /** The directory (relative to source.data.dir). */
  private String directory = null;

  /** The full directory where the src files are. */
  private File srcDirFile = null;

  /** The readers. */
  private RrfReaders readers;

  /** The loader. */
  private final String loader = "loader";

  /** The loaded terminologies. */
  private Map<String, Terminology> loadedTerminologies = new HashMap<>();

  /** The loaded root terminologies. */
  private Map<String, RootTerminology> loadedRootTerminologies =
      new HashMap<>();

  /** The loaded languages. */
  private Map<String, Language> loadedLanguages = new HashMap<>();

  /** The loaded term types. */
  private Map<String, TermType> loadedTermTypes = new HashMap<>();

  /** The run date. */
  private Date runDate = null;

  /**
   * Instantiates an empty {@link MetadataLoaderAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public MetadataLoaderAlgorithm() throws Exception {
    super();
    setActivityId(UUID.randomUUID().toString());
    setWorkId("METADATALOADER");
    setLastModifiedBy("admin");
  }

  //TODO - Cancel-checks, progress-monitoring, transaction (no commits), and logging. 
  
  /**
   * Sets the directory.
   *
   * @param directory the directory
   */
  public void setDirectory(String directory) {
    this.directory = directory;
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {

    if (getProject() == null) {
      throw new Exception("Metadata Loading requires a project to be set");
    }
    if (directory == null) {
      throw new Exception("Metadata Loading requires a directory to be set.");
    }

    // Check the input directories

    String srcFullPath =
        ConfigUtility.getConfigProperties().getProperty("source.data.dir")
            + File.separator + directory;

    srcDirFile = new File(srcFullPath);
    if (!srcDirFile.exists()) {
      throw new Exception("Specified input directory does not exist");
    }

    // n/a - NO preconditions
    return new ValidationResultJpa();
  }

  /* see superclass */
  @Override
  public void compute() throws Exception {
    logInfo("Starting METADATALOADING");

    try {

      runDate = new Date();

      // Load the terminologies from sources.src
      cacheExistingTerminologies();
      loadSources();

      logInfo("  project = " + getProject().getId());
      logInfo("  workId = " + getWorkId());
      logInfo("  activityId = " + getActivityId());
      logInfo("  user  = " + getLastModifiedBy());
      logInfo("Finished METADATALOADING");

    } catch (Exception e) {
      logError("Unexpected problem - " + e.getMessage());
      throw e;
    }

  }

  /* see superclass */
  @Override
  public void reset() throws Exception {
    // n/a - No reset
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
    //TODO - uncomment once database rebuilt.
//    for (final Terminology term : getTerminologies().getObjects()) {
//      // lazy init
//      term.getSynonymousNames().size();
//      term.getRootTerminology().getTerminology();
//      loadedTerminologies.put(term.getTerminology(), term);
//    }

  }

  /**
   * Load sources.src. This is responsible for loading terminologies and root
   * terminologies.
   *
   * @throws Exception the exception
   */
  private void loadSources() throws Exception {
    //
    // Load the sources.src file
    //
    String sourcesFile =
        srcDirFile + File.separator + "src" + File.separator + "sources.src";
    BufferedReader sources = null;
    try {
      sources = new BufferedReader(new FileReader(sourcesFile));
    } catch (Exception e) {
      throw new Exception("File not found: " + sourcesFile);
    }

    final String fields[] = new String[20];
    final List<String> lines = new ArrayList<>();
    String linePre = null;
    while ((linePre = sources.readLine()) != null) {
      linePre = linePre.replace("\r", "");
      lines.add(linePre);
    }

    // Each line of sources.src corresponds to one terminology.
    // Check to make sure the terminology doesn't already exist in the database
    // If it does, skip it.
    // If it does not, add it.
    for (String line : lines) {
      System.out.println("TESTTEST: " + line);
      FieldedStringTokenizer.split(line, "|", 20, fields);

      // Fields:
      // 0 SOURCE_NAME (Not used)
      // 1 LOW_SOURCE (Not used)
      // 2 RESTRICTION_LEVEL (RootTerminology.restrictionLevel)
      // 3 NORMALIZED_SOURCE (Not used)
      // 4 STRIPPED_SOURCE (Not used)
      // 5 VERSION (Not used)
      // 6 SOURCE_FAMILY (RootTerminology.family)
      // 7 OFFICIAL_NAME (RootTerminology.preferredName)
      // 8 NLM_CONTACT (Not used)
      // 9 ACQUISITION_CONTACT (RootTerminology.aquisitionContact)
      // 10 CONTENT_CONTACT (RootTerminology.contentContact)
      // 11 LICENSE_CONTACT (RootTerminology.licenseContact)
      // 12 INVERTER (Terminology.inverterEmail)
      // 13 CONTEXT_TYPE (Terminology.includeSiblings)
      // 14 URL (Terminology.url)
      // 15 LANGUAGE (RootTerminology.language)
      // 16 CITATION (RootTerminology.citation)
      // 17 LICENSE_INFO (Not used)
      // 18 CHARACTER_SET (Not used)
      // 19 REL_DIRECTIONALITY_FLAG (Terminology.assertsRelDirection)

      // e.g.
      // NCI_2016_05E|NCI_2016_04D|0|NCI_2016_05E|NCI|2016_05E|NCI|National
      // Cancer Institute Thesaurus||Sherri de Coronado, Center for
      // Bioinformatics, National Cancer Institute, 9609 Medical Center Dr.,
      // Rockville MD 20850; phone: 925-377-5960; email:
      // decorons@osp.nci.nih.gov|Sherri de Coronado;;Center for Bioinformatics,
      // National Cancer Institute;9609 Medical Center
      // Dr.;;Rockville;MD;USA;20850;925-377-5960;;decorons@osp.nci.nih.gov;;|Sherri
      // de Coronado;;Center for Bioinformatics, National Cancer Institute;9609
      // Medical Center
      // Dr.;;Rockville;MD;USA;20850;925-377-5960;;decorons@osp.nci.nih.gov;;|Sharon
      // Quan,
      // quansh@mail.nih.gov||http://www.cancer.gov/cancerinfo/terminologyresources|ENG|;;National
      // Cancer Institute, National Institutes of Health;;NCI Thesaurus;Sherri
      // de Coronado, decorons@osp.nci.nih.gov;;;;;;May 2016, Protege
      // version;Rockville, MD;;;;;;;;||UTF-8||

      //
      //Construct Root Terminology based on input line.
      //
      RootTerminology rootTerm = new RootTerminologyJpa();
      if (!fields[2].isEmpty()) {
        rootTerm.setRestrictionLevel(Integer.parseInt(fields[2]));
      }
      if (!fields[6].isEmpty()) {
        rootTerm.setFamily(fields[6]);
      }
      if (!fields[7].isEmpty()) {
        rootTerm.setPreferredName(fields[7]);
      }
      if (!fields[9].isEmpty()) {
        rootTerm.setAcquisitionContact(new ContactInfoJpa(fields[9]));
      }
      if (!fields[10].isEmpty()) {
        rootTerm.setContentContact(new ContactInfoJpa(fields[10]));
      }
      if (!fields[11].isEmpty()) {
        rootTerm.setLicenseContact(new ContactInfoJpa(fields[11]));
      }
      if (!fields[15].isEmpty()) {
        rootTerm.setLanguage(fields[15]);
      }
      
      //
      //Construct Terminology based on input line.
      //
      Terminology term = new TerminologyJpa();
      if (!fields[12].isEmpty()) {
        term.setInverterEmail(fields[12]);
      }
      if (!fields[13].isEmpty()) {
        if (fields[13].contains("NOSIB")) {
          term.setIncludeSiblings(false);
        } else {
          term.setIncludeSiblings(true);
        }
      }
      if (!fields[16].isEmpty()) {
        term.setCitation(new CitationJpa(fields[16]));
      }
      if (!fields[14].isEmpty()) {
        term.setUrl(fields[14]);
      }
      if (!fields[19].isEmpty()) {
        if (Integer.parseInt(fields[19]) == 1) {
          term.setAssertsRelDirection(true);
        } else if (Integer.parseInt(fields[19]) == 0) {
          term.setAssertsRelDirection(false);
        } else {
          throw new Exception("Error: Unexpected value " + fields[19]
              + " for field REL_DIRECTIONALITY_FLAG.  Value can only be 0 or 1.");
        }
      }

    }

    sources.close();
  }

  /**
   * Load MRDOC. This is responsible for loading much of the metadata.
   *
   * @throws Exception the exception
   */
  private void loadMrdoc() throws Exception {
    logInfo("  Load MRDOC abbreviation types");
    String linePre = null;
    Set<String> atnSeen = new HashSet<>();
    // final Map<String, RelationshipType> relMap = new HashMap<>();
    // final Map<String, String> inverseRelMap = new HashMap<>();

    final Map<String, AdditionalRelationshipType> relaMap = new HashMap<>();
    final Map<String, String> inverseRelaMap = new HashMap<>();
    final Map<String, TermType> ttyMap = new HashMap<>();
    final PushBackReader reader = readers.getReader(RrfReaders.Keys.MRDOC);
    int objectCt = 0;
    final String fields[] = new String[4];
    final List<String> lines = new ArrayList<>();
    while ((linePre = reader.readLine()) != null) {
      linePre = linePre.replace("\r", "");
      lines.add(linePre);
    }
    //TODO - make sure objects don't exist in database before committing.
    
    // // Fake MRDOC entries for XR, BRO, BRB, BRN
    // // REL|RN|expanded_form|has a narrower relationship|
    // // REL|RN|rel_inverse|RB|
    // lines.add("REL|XR|expanded_form|Not related|");
    // lines.add("REL|XR|rel_inverse|XR|");
    // lines.add("REL|BRO|expanded_form|Bequeath otherwise|");
    // lines.add("REL|BRN|expanded_form|Bequeath narrower|");
    // lines.add("REL|BRB|expanded_form|Bequeath broader|");
    // lines.add("REL|BRO|rel_inverse|BRO|");
    // lines.add("REL|BRN|rel_inverse|BRB|");
    // lines.add("REL|BRB|rel_inverse|BRN|");
    for (String line : lines) {
      FieldedStringTokenizer.split(line, "|", 4, fields);

      // Field Description DOCKEY,VALUE,TYPE,EXPL
      // 0 DOCKEY
      // 1 VALUE
      // 2 TYPE
      // 3 EXPL

      // e.g.
      // ATN|ACCEPTABILITYID|expanded_form|Acceptability Id|

      // Handle AttributeNames
      if (fields[0].equals("ATN") && fields[2].equals("expanded_form")
          && !atnSeen.contains(fields[1])) {
        final AttributeName atn = new AttributeNameJpa();
        atn.setAbbreviation(fields[1]);
        atn.setExpandedForm(fields[3]);
        atn.setTimestamp(runDate);
        atn.setLastModified(runDate);
        atn.setLastModifiedBy(loader);
        atn.setTerminology(getTerminology());
        atn.setVersion(getVersion());
        atn.setPublished(true);
        atn.setPublishable(true);
        Logger.getLogger(getClass()).debug("    add attribute name - " + atn);
        addAttributeName(atn);
        atnSeen.add(fields[1]);
      }

      // // Handle Languages
      // else if (fields[0].equals("LAT") && fields[2].equals("expanded_form"))
      // {
      // final Language lat = new LanguageJpa();
      // lat.setAbbreviation(fields[1]);
      // lat.setExpandedForm(fields[3]);
      // lat.setTimestamp(runDate);
      // lat.setLastModified(runDate);
      // lat.setLastModifiedBy(loader);
      // lat.setTerminology(getTerminology());
      // lat.setVersion(getVersion());
      // lat.setPublished(true);
      // lat.setPublishable(true);
      // lat.setISO3Code(fields[1]);
      // if (latCodeMap.containsKey(fields[1])) {
      // lat.setISOCode(latCodeMap.get(fields[1]));
      // } else {
      // throw new Exception(
      // "Language map does not have 2 letter code for " + fields[1]);
      // }
      // Logger.getLogger(getClass()).debug(" add language - " + lat);
      // addLanguage(lat);
      // loadedLanguages.put(lat.getAbbreviation(), lat);
      // }

      // Handle AdditionalRelationshipLabel
      else if (fields[0].equals("RELA") && fields[2].equals("expanded_form")) {
        final AdditionalRelationshipType rela =
            new AdditionalRelationshipTypeJpa();
        rela.setAbbreviation(fields[1]);
        rela.setExpandedForm(fields[3]);
        rela.setTimestamp(runDate);
        rela.setLastModified(runDate);
        rela.setLastModifiedBy(loader);
        rela.setTerminology(getTerminology());
        rela.setVersion(getVersion());
        rela.setPublished(true);
        rela.setPublishable(true);
        // DL fields are all left false, with no domain/range
        // no equivalent types or supertypes included
        relaMap.put(fields[1], rela);
        Logger.getLogger(getClass())
            .debug("    add additional relationship type - " + rela);
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

      // // Handle RelationshipLabel
      // else if (fields[0].equals("REL") && fields[2].equals("expanded_form"))
      // {
      // final RelationshipType rel = new RelationshipTypeJpa();
      // rel.setAbbreviation(fields[1]);
      // rel.setExpandedForm(fields[3]);
      // rel.setTimestamp(runDate);
      // rel.setLastModified(runDate);
      // rel.setLastModifiedBy(loader);
      // rel.setTerminology(getTerminology());
      // rel.setVersion(getVersion());
      // rel.setPublished(true);
      // rel.setPublishable(true);
      // rel.setHierarchical(false);
      // if (fields[1].equals("CHD")) {
      // rel.setHierarchical(true);
      // }
      // relMap.put(fields[1], rel);
      // Logger.getLogger(getClass())
      // .debug(" add relationship type - " + rel);
      // } else if (fields[0].equals("REL") && fields[2].equals("rel_inverse")
      // && !fields[1].equals("SIB")) {
      // inverseRelMap.put(fields[1], fields[3]);
      // if (inverseRelMap.containsKey(fields[1])
      // && inverseRelMap.containsKey(fields[3])) {
      // RelationshipType rel1 = relMap.get(fields[1]);
      // RelationshipType rel2 = relMap.get(fields[3]);
      // rel1.setInverse(rel2);
      // rel2.setInverse(rel1);
      // addRelationshipType(rel1);
      // addRelationshipType(rel2);
      // }
      // }

      else if (fields[0].equals("TTY") && fields[2].equals("expanded_form")) {
        final TermType tty = new TermTypeJpa();
        tty.setAbbreviation(fields[1]);
        tty.setExpandedForm(fields[3]);
        tty.setTimestamp(runDate);
        tty.setLastModified(runDate);
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
          if (ttyMap.get(fields[1])
              .getCodeVariantType() == CodeVariantType.ET) {
            ttyMap.get(fields[1]).setCodeVariantType(CodeVariantType.PET);
          } else {
            ttyMap.get(fields[1]).setCodeVariantType(CodeVariantType.PN);
          }
        }
        if (fields[3].equals("entry_term")) {
          if (ttyMap.get(fields[1])
              .getCodeVariantType() == CodeVariantType.PN) {
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
      
      // // General metadata entries (skip MAPATN)
      // else if (!fields[0].equals("MAPATN")) {
      // final GeneralMetadataEntry entry = new GeneralMetadataEntryJpa();
      //
      // entry.setTimestamp(runDate);
      // entry.setLastModified(runDate);
      // entry.setLastModifiedBy(loader);
      // entry.setTerminology(getTerminology());
      // entry.setVersion(getVersion());
      // entry.setPublished(true);
      // entry.setPublishable(true);
      //
      // entry.setKey(fields[0]);
      // entry.setAbbreviation(fields[1]);
      // entry.setType(fields[2]);
      // entry.setExpandedForm(fields[3]);
      //
      // addGeneralMetadataEntry(entry);
      // }

    }

    // Add TTYs when done
    for (final TermType tty : ttyMap.values()) {
      addTermType(tty);
      loadedTermTypes.put(tty.getAbbreviation(), tty);
    }

  }

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    checkRequiredProperties(new String[] {

    }, p);

    directory = String.valueOf(p.getProperty("directory"));

  }

  /* see superclass */
  @Override
  public List<AlgorithmParameter> getParameters() {
    final List<AlgorithmParameter> params = super.getParameters();
    AlgorithmParameter param = new AlgorithmParameterJpa("Directory",
        "directory", "Directory of input files, relative to source.data.dir.",
        "e.g. terminologies/NCI_INSERT", 2000, AlgorithmParameter.Type.STRING);
    params.add(param);

    return params;
  }

}