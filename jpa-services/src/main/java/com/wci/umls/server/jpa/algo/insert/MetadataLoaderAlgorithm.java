/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.insert;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import com.wci.umls.server.AlgorithmParameter;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.CancelException;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.FieldedStringTokenizer;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.AbstractSourceInsertionAlgorithm;
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
import com.wci.umls.server.model.meta.IdType;
import com.wci.umls.server.model.meta.NameVariantType;
import com.wci.umls.server.model.meta.RootTerminology;
import com.wci.umls.server.model.meta.TermType;
import com.wci.umls.server.model.meta.TermTypeStyle;
import com.wci.umls.server.model.meta.Terminology;
import com.wci.umls.server.model.meta.UsageType;

/**
 * Implementation of an algorithm to import metadata.
 */
public class MetadataLoaderAlgorithm extends AbstractSourceInsertionAlgorithm {

  /** The loaded organizing class types. */
  private Map<String, IdType> loadedOrganizingClassTypes = null;

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

  /**
   * Check preconditions.
   *
   * @return the validation result
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {

    ValidationResult validationResult = new ValidationResultJpa();

    if (getProject() == null) {
      throw new Exception("Metadata Loading requires a project to be set");
    }

    // Check the input directories

    final String srcFullPath =
        ConfigUtility.getConfigProperties().getProperty("source.data.dir")
            + File.separator + getProcess().getInputPath();

    setSrcDirFile(new File(srcFullPath));
    if (!getSrcDirFile().exists()) {
      throw new Exception("Specified input directory does not exist");
    }

    //
    // Validate AdditionalRelationshipType inverses
    //
    List<String> lines = loadFileIntoStringList(getSrcDirFile(), "MRDOC.RRF",
        "RELA\\|(.*)", null);

    final Set<String> relaMRDOC = new HashSet<>();
    final Set<String> inverseRelaMRDOC = new HashSet<>();

    // Field Description DOCKEY,VALUE,TYPE,EXPL
    // 0 DOCKEY
    // 1 VALUE
    // 2 TYPE
    // 3 EXPL

    // RELA|Has_Salt_Form|expanded_form|Has Salt Form|
    // RELA|Has_Salt_Form|rela_inverse|Has_Free_Acid_Or_Base_Form|

    String fields[] = new String[4];

    // Load all of the rels and inverseRels from MRDOC into a set
    for (String line : lines) {
      FieldedStringTokenizer.split(line, "|", 4, fields);
      if (fields[2].equals("expanded_form") && !relaMRDOC.contains(fields[1])) {
        relaMRDOC.add(fields[1]);
      } else if (fields[2].equals("rela_inverse")) {
        inverseRelaMRDOC.add(fields[3]);
      }
    }

    // If loaded inverse doesn't exist as its own entity in MRDOC or
    // database, fire ERROR.
    // If loaded inverse doesn't exist as its own entity in MRDOC, but it DOES
    // exist in the database, fire warning.
    for (String abbreviation : inverseRelaMRDOC) {
      if (!relaMRDOC.contains(abbreviation)
          && getCachedAdditionalRelationshipType(abbreviation) == null) {
        validationResult.addError(
            "MRDOC references inverse Additional Relationship Type that does not exist in MRDOC nor in the database: "
                + abbreviation);
      }
      if (!relaMRDOC.contains(abbreviation)
          && getCachedAdditionalRelationshipType(abbreviation) != null) {
        validationResult.addWarning(
            "MRDOC references inverse Additional Relationship Type that exists in the database, but is not in MRDOC: "
                + abbreviation);
      }
    }

    return validationResult;
  }

  /**
   * Compute.
   *
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public void compute() throws Exception {
    logInfo("Starting METADATALOADING");

    try {

      runDate = new Date();
      setSteps(4);

      //
      // Load the terminologies from sources.src
      //
      if (isCancelled()) {
        throw new CancelException("Cancelled");
      }
      handleTerminologies();
      updateProgress();

      //
      // Load the TermTypes from termgroups.src and MRDOC
      //
      if (isCancelled()) {
        throw new CancelException("Cancelled");
      }
      handleTermTypes();
      updateProgress();

      //
      // Load the AttibuteNames
      //
      if (isCancelled()) {
        throw new CancelException("Cancelled");
      }
      handleAttributeNames();

      updateProgress();

      //
      // Load AdditionalRelationshipTypes (and inverses)
      //
      if (isCancelled()) {
        throw new CancelException("Cancelled");
      }
      handleAdditionalRelationshipTypes();
      updateProgress();

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

  /**
   * Reset.
   *
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public void reset() throws Exception {
    // n/a - No reset
  }

  /**
   * Load sources.src and contexts.src. These are responsible for loading
   * terminologies and root terminologies.
   *
   * @throws Exception the exception
   */
  private void handleTerminologies() throws Exception {

    logInfo(
        "[MetadataLoader] Checking for new/updated Terminologies and Root Terminologies");

    // Set up a map of all Terminologies to add. Set up new Terminologies based
    // on both
    // input files
    final Map<String, Terminology> termsToAddMap = new HashMap<>();

    //
    // Load the sources.src file
    //
    List<String> lines =
        loadFileIntoStringList(getSrcDirFile(), "sources.src", null, null);

    String fields[] = new String[20];

    // Each line of sources.src corresponds to one terminology.
    // Check to make sure the terminology doesn't already exist in the database
    // If it does, skip it.
    // If it does not, add it.
    for (String line : lines) {
      FieldedStringTokenizer.split(line, "|", 20, fields);

      // Fields:
      // 0 SOURCE_NAME (Used to match to contexts.src)
      // 1 LOW_SOURCE (Not used)
      // 2 RESTRICTION_LEVEL (RootTerminology.restrictionLevel)
      // 3 NORMALIZED_SOURCE (Not used)
      // 4 STRIPPED_SOURCE (RootTerminology.terminology /
      // Teminology.terminology)
      // 5 VERSION (Teminology.version)
      // 6 SOURCE_FAMILY (RootTerminology.family)
      // 7 OFFICIAL_NAME (RootTerminology.preferredName /
      // Terminology.preferredName)
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
      // 19 REL_DIRECTIONALITY_FLAG (Terminology.assertsRelDirection /
      // RootTerminology.polyHierarchy)

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
      // Root Terminology based on input line.
      //
      if (getCachedRootTerminology(fields[4]) == null) {
        // Add if it does not yet exist
        final RootTerminology rootTerm = new RootTerminologyJpa();
        rootTerm.setAcquisitionContact(new ContactInfoJpa(fields[9]));
        rootTerm.setContentContact(new ContactInfoJpa(fields[10]));
        rootTerm.setLicenseContact(new ContactInfoJpa(fields[11]));
        rootTerm.setFamily(fields[6]);
        if (!fields[13].isEmpty()) {
          if (fields[13].contains("MULTIPLE")) {
            rootTerm.setPolyhierarchy(true);
          } else {
            rootTerm.setPolyhierarchy(false);
          }
        }
        rootTerm.setPreferredName(fields[7]);
        rootTerm.setRestrictionLevel(Integer.parseInt(fields[2]));
        rootTerm.setTerminology(fields[4]);
        rootTerm.setLanguage(fields[15]);

        logInfo("[MetadataLoader] Adding Root Terminology: " + rootTerm);
        addRootTerminology(rootTerm);
        getCachedRootTerminologies().put(rootTerm.getTerminology(), rootTerm);
      }
      // If it does already exist, update the existing root terminology
      else {
        final RootTerminology existingRootTerm =
            getCachedRootTerminology(fields[4]);
        existingRootTerm.setAcquisitionContact(new ContactInfoJpa(fields[9]));
        existingRootTerm.setContentContact(new ContactInfoJpa(fields[10]));
        existingRootTerm.setLicenseContact(new ContactInfoJpa(fields[11]));
        existingRootTerm.setFamily(fields[6]);
        if (!fields[13].isEmpty()) {
          if (fields[13].contains("MULTIPLE")) {
            existingRootTerm.setPolyhierarchy(true);
          } else {
            existingRootTerm.setPolyhierarchy(false);
          }
        }
        existingRootTerm.setPreferredName(fields[7]);
        existingRootTerm.setRestrictionLevel(Integer.parseInt(fields[2]));
        existingRootTerm.setTerminology(fields[4]);
        existingRootTerm.setTimestamp(runDate);
        existingRootTerm.setLanguage(fields[15]);

        logInfo(
            "[MetadataLoader] Updating Root Terminology: " + existingRootTerm);

        updateRootTerminology(existingRootTerm);
        getCachedRootTerminologies().put(existingRootTerm.getTerminology(),
            existingRootTerm);
      }

      //
      // Terminology based on input line.
      //
      if (getCachedTerminology(fields[4], fields[5]) == null) {
        // Add if it does not yet exist
        final Terminology term = new TerminologyJpa();
        term.setCitation(new CitationJpa(fields[16]));
        term.setCurrent(true);
        term.setPreferredName(fields[7]);
        term.setTerminology(fields[4]);
        term.setVersion(fields[5]);
        term.setDescriptionLogicTerminology(false);
        if (determineOrganizingClassType(fields[4]) != null) {
          term.setOrganizingClassType(determineOrganizingClassType(fields[4]));
        } else {
          term.setOrganizingClassType(IdType.CODE);
        }
        term.setInverterEmail(fields[12]);
        if (!fields[13].isEmpty()) {
          if (fields[13].contains("NOSIB")) {
            term.setIncludeSiblings(false);
          } else {
            term.setIncludeSiblings(true);
          }
        }
        term.setUrl(fields[14]);
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
        term.setRootTerminology(getCachedRootTerminology(fields[4]));
        termsToAddMap.put(fields[0], term);
      }
      // If it does already exist, update the existing terminology
      else {
        final Terminology existingTerm =
            getCachedTerminology(fields[4], fields[5]);
        existingTerm.setCitation(new CitationJpa(fields[16]));
        existingTerm.setCurrent(true);
        existingTerm.setPreferredName(fields[7]);
        existingTerm.setTerminology(fields[4]);
        existingTerm.setVersion(fields[5]);
        existingTerm.setDescriptionLogicTerminology(false);
        if (determineOrganizingClassType(fields[4]) != null) {
          existingTerm
              .setOrganizingClassType(determineOrganizingClassType(fields[4]));
        } else {
          existingTerm.setOrganizingClassType(IdType.CODE);
        }
        existingTerm.setInverterEmail(fields[12]);
        if (!fields[13].isEmpty()) {
          if (fields[13].contains("NOSIB")) {
            existingTerm.setIncludeSiblings(false);
          } else {
            existingTerm.setIncludeSiblings(true);
          }
        }
        existingTerm.setUrl(fields[14]);
        if (!fields[19].isEmpty()) {
          if (Integer.parseInt(fields[19]) == 1) {
            existingTerm.setAssertsRelDirection(true);
          } else if (Integer.parseInt(fields[19]) == 0) {
            existingTerm.setAssertsRelDirection(false);
          } else {
            throw new Exception("Error: Unexpected value " + fields[19]
                + " for field REL_DIRECTIONALITY_FLAG.  Value can only be 0 or 1.");
          }
        }
        existingTerm.setRootTerminology(getCachedRootTerminology(fields[4]));

        logInfo("[MetadataLoader] Updating Terminology: " + existingTerm);

        updateTerminology(existingTerm);
        getCachedTerminologies().put(fields[0], existingTerm);
      }
    }

    // For every Term about to be added, find any previously existing terms with
    // the same root terminology, and set their current to false.
    for (Terminology newTerm : termsToAddMap.values()) {
      for (Terminology existingTerm : getCachedTerminologies().values()) {
        if (newTerm.getRootTerminology()
            .equals(existingTerm.getRootTerminology())
            && !newTerm.getVersion().equals(existingTerm.getVersion())) {
          if (existingTerm.isCurrent()) {
            existingTerm.setCurrent(false);
            logInfo("[MetadataLoader] Updating Terminology: " + existingTerm);
            updateTerminology(existingTerm);
            getCachedTerminologies().put(
                existingTerm.getTerminology() + "_" + existingTerm.getVersion(),
                existingTerm);
          }
        }
      }
    }

    // After we finish going through the file, add everything that we need to
    // the database
    for (Terminology newTerm : termsToAddMap.values()) {
      logInfo("[MetadataLoader] Adding Terminology: " + newTerm);
      newTerm = addTerminology(newTerm);
      getCachedTerminologies()
          .put(newTerm.getTerminology() + "_" + newTerm.getVersion(), newTerm);
    }
  }

  /**
   * Determine organizing class type.
   *
   * @param terminology the terminology
   * @return the id type
   * @throws Exception the exception
   */
  private IdType determineOrganizingClassType(String terminology)
    throws Exception {

    // If previous version of terminology exists, use previous
    // OrganizingClassType
    for (Map.Entry<String, Terminology> entry : getCachedTerminologies()
        .entrySet()) {
      String termName = null;
      int indexOfUnderscore = entry.getKey().indexOf("_");
      if (indexOfUnderscore == -1) {
        termName = entry.getKey();
      } else {
        termName = entry.getKey().substring(0, entry.getKey().indexOf("_"));
      }
      Terminology term = entry.getValue();
      if (terminology.equals(termName)) {
        return term.getOrganizingClassType();
      }
    }

    // Otherwise, we need to look through contexts.src.
    // If this is the first time this is being called, read contexts.src,
    // and populate the loadedOrganizingClassTypes map
    if (loadedOrganizingClassTypes == null) {

      loadedOrganizingClassTypes = new HashMap<>();

      //
      // Load the contexts.src file
      //
      final List<String> lines =
          loadFileIntoStringList(getSrcDirFile(), "contexts.src", null, null);

      final String[] fields = new String[17];

      // Store a map of how many times each organizingClassType is associated
      // with each terminology, and the total rows observed
      // e.g. "NCI_2016_05E" -> "TOTAL" -> 100
      // "NCI_2016_05E" -> "SOURCE_CUI" -> 97
      // "NCI_2016_05E" -> "SOURCE_DUI" -> 3
      final Map<String, Map<String, Integer>> sourcesOrganizingClassTypeCount =
          new HashMap<>();

      for (String line : lines) {
        FieldedStringTokenizer.split(line, "|", 17, fields);

        // Fields:
        // 0 source_atom_id_1
        // 1 relationship_name
        // 2 relationship_attribute
        // 3 source_atom_id_2
        // 4 source (Used to match to sources.src)
        // 5 source_of_label
        // 6 hcd
        // 7 parent_treenum
        // 8 release mode
        // 9 source_rui
        // 10 relationship_group
        // 11 sg_id_1
        // 12 sg_type_1 (Terminology.organizingClassType)
        // 13 sg_qualifier_1
        // 14 sg_id_2
        // 15 sg_type_2
        // 16 sg_qualifier_2

        // e.g.
        // 362168904|PAR|isa|362174335|NCI_2016_05E|NCI_2016_05E||
        // 31926003.362204588.362250568.362175233.362174339.362174335|00|||C37447|
        // SOURCE_CUI|NCI_2016_05E|C1971|SOURCE_CUI|NCI_2016_05E|

        // Pull first 100 references to each terminology, find the most
        // occurring class type, and save it into the map.

        String source = fields[4];

        // If this is the first time this source has been encountered,
        // initialize its sub-map
        if (sourcesOrganizingClassTypeCount.get(source) == null) {
          sourcesOrganizingClassTypeCount.put(source,
              new HashMap<String, Integer>());
          sourcesOrganizingClassTypeCount.get(source).put("TOTAL", 0);
        }
        // Only read up to 100 observations per source
        if (sourcesOrganizingClassTypeCount.get(source).get("TOTAL") < 100) {
          // Set terminology organizingClassType count based on sg_type
          Integer currentTotalCount =
              sourcesOrganizingClassTypeCount.get(source).get("TOTAL");
          String organizingClassType = fields[12];
          if (sourcesOrganizingClassTypeCount.get(source)
              .get(organizingClassType) == null) {
            sourcesOrganizingClassTypeCount.get(source).put(organizingClassType,
                0);
          }
          Integer currentOrganizingClassTypeCount =
              sourcesOrganizingClassTypeCount.get(source)
                  .get(organizingClassType);

          sourcesOrganizingClassTypeCount.get(source).put(organizingClassType,
              ++currentOrganizingClassTypeCount);
          sourcesOrganizingClassTypeCount.get(source).put("TOTAL",
              ++currentTotalCount);
        }
      }

      // Once the entire file is read, go through each terminology, find the
      // most common organizingClassType, and assign to the
      // loadedOrganizingClassTypes map

      for (Map.Entry<String, Map<String, Integer>> entry : sourcesOrganizingClassTypeCount
          .entrySet()) {
        String term = entry.getKey();
        Map<String, Integer> classTypeCountMap = entry.getValue();

        Integer highestOccurence = 0;
        String mostCommonClassType = null;

        for (Map.Entry<String, Integer> subEntry : classTypeCountMap
            .entrySet()) {
          String classType = subEntry.getKey();
          Integer count = subEntry.getValue();
          if (!classType.equals("TOTAL")) {
            if (count > highestOccurence) {
              highestOccurence = count;
              mostCommonClassType = classType;
            }
          }
        }

        // Once we identify the most common class type, use it to assign:
        // SOURCE_CUI = CONCEPT, SOURCE_DUI = DESCRIPTOR, else CODE
        if (mostCommonClassType.equals("SOURCE_CUI")) {
          loadedOrganizingClassTypes.put(term, IdType.CONCEPT);
        } else if (mostCommonClassType.equals("SOURCE_DUI")) {
          loadedOrganizingClassTypes.put(term, IdType.DESCRIPTOR);
        } else {
          loadedOrganizingClassTypes.put(term, IdType.CODE);
        }
      }
    }

    // Now that that's all taken care of, return the value, if it exists
    return loadedOrganizingClassTypes.get(terminology);

  }

  /**
   * Load MRDOC TTN lines and termgroups.src. These are responsible for loading
   * term types
   *
   * @throws Exception the exception
   */
  private void handleTermTypes() throws Exception {

    logInfo("[MetadataLoader] Checking for new/updated Term Types");

    // Set up a map of all TermTypes to add. Set up new TermTypes based on both
    // input files
    final Map<String, TermType> ttyToAddMap = new HashMap<>();

    //
    // Load TTY lines from the MRDOC file
    //
    List<String> lines = loadFileIntoStringList(getSrcDirFile(), "MRDOC.RRF",
        "TTY\\|(.*)", null);

    String fields[] = new String[4];

    for (String line : lines) {
      FieldedStringTokenizer.split(line, "|", 4, fields);

      // Field Description DOCKEY,VALUE,TYPE,EXPL
      // 0 DOCKEY
      // 1 VALUE
      // 2 TYPE
      // 3 EXPL

      // e.g.
      // TTY|PT|expanded_form|Designated preferred name|
      // TTY|PT|tty_class|preferred|

      if (fields[2].equals("expanded_form")
          && getCachedTermType(fields[1]) == null) {
        // If it does not yet exist, start setting it up and save it to map
        final TermType tty = new TermTypeJpa();
        tty.setAbbreviation(fields[1]);
        tty.setExpandedForm(fields[3]);
        tty.setTerminology(getProject().getTerminology());
        tty.setVersion(getProject().getVersion());
        tty.setPublished(false);
        tty.setPublishable(true);
        tty.setCodeVariantType(CodeVariantType.UNDEFINED);
        // based on TTY class (set later)
        tty.setHierarchicalType(false);
        tty.setNameVariantType(NameVariantType.UNDEFINED);
        tty.setSuppressible(false);
        tty.setStyle(TermTypeStyle.UNDEFINED);
        tty.setUsageType(UsageType.UNDEFINED);
        ttyToAddMap.put(fields[1], tty);
      }

      else if (fields[2].equals("tty_class")
          && getCachedTermType(fields[1]) == null) {
        // If it doesn't exist, finish setting it up
        if (fields[3].equals("attribute")) {
          ttyToAddMap.get(fields[1])
              .setCodeVariantType(CodeVariantType.ATTRIBUTE);
        }
        if (fields[3].equals("abbreviation")) {
          ttyToAddMap.get(fields[1]).setNameVariantType(NameVariantType.AB);
          ttyToAddMap.get(fields[1]).setCodeVariantType(CodeVariantType.SY);
        }
        if (fields[3].equals("synonym")) {
          ttyToAddMap.get(fields[1]).setCodeVariantType(CodeVariantType.SY);
        }
        if (fields[3].equals("preferred")) {
          if (ttyToAddMap.get(fields[1])
              .getCodeVariantType() == CodeVariantType.ET) {
            ttyToAddMap.get(fields[1]).setCodeVariantType(CodeVariantType.PET);
          } else {
            ttyToAddMap.get(fields[1]).setCodeVariantType(CodeVariantType.PN);
          }
        }
        if (fields[3].equals("entry_term")) {
          if (ttyToAddMap.get(fields[1])
              .getCodeVariantType() == CodeVariantType.PN) {
            ttyToAddMap.get(fields[1]).setCodeVariantType(CodeVariantType.PET);
          } else {
            ttyToAddMap.get(fields[1]).setCodeVariantType(CodeVariantType.ET);
          }
        }
        if (fields[3].equals("hierarchical")) {
          ttyToAddMap.get(fields[1]).setHierarchicalType(true);
        }
        if (fields[3].equals("obsolete")) {
          ttyToAddMap.get(fields[1]).setObsolete(true);
        }
        if (fields[3].equals("expanded")) {
          ttyToAddMap.get(fields[1])
              .setNameVariantType(NameVariantType.EXPANDED);
        }
      }

    }

    //
    // Load the termgroups.src file
    //

    lines =
        loadFileIntoStringList(getSrcDirFile(), "termgroups.src", null, null);

    fields = new String[6];

    // For termgroups.src, keep track of whether an abbreviation has been added
    // yet, and only keep the first instance
    List<String> alreadyAddedAbbreviations = new ArrayList<>();

    for (String line : new ArrayList<String>(lines)) {
      FieldedStringTokenizer.split(line, "|", 6, fields);
      if (alreadyAddedAbbreviations.contains(fields[5])) {
        lines.remove(line);
      } else {
        alreadyAddedAbbreviations.add(fields[5]);
      }
    }

    // Each line of termgroups.src corresponds to one termType.
    // Check to make sure the termType doesn't already exist in the database
    // If it does, skip it.
    // If it does not, add it.
    for (String line : lines) {
      FieldedStringTokenizer.split(line, "|", 6, fields);

      // Fields:
      // 0 Hi Term Group (For precedence list)
      // 1 Low Term Group (For precedence list)
      // 2 Suppresible (TermType.suppresible)
      // 3 Exclude (TermType.exclude)
      // 4 Norm Exclude (TermType.normExclude)
      // 5 TTY (TermType.abbreviation)

      // e.g.
      // NCI_2016_05E/PT|NCI_2016_04D/PT|N|N|N|PT|

      //
      // TermType based on input line.
      //
      if (getCachedTermType(fields[5]) == null) {
        // If it doesn't already exist and it was already set up in the map,
        // modify remaining variables
        if (ttyToAddMap.containsKey(fields[5])) {
          ttyToAddMap.get(fields[5]).setSuppressible(fields[2].equals("Y"));
          ttyToAddMap.get(fields[5]).setExclude(fields[3].equals("Y"));
          ttyToAddMap.get(fields[5]).setNormExclude(fields[4].equals("Y"));
        }
        // If it was NOT set up in the map from MRDOC, create a new TermType and
        // add it to the map
        else {
          final TermType termType = new TermTypeJpa();
          termType.setAbbreviation(fields[5]);
          termType.setBranch(Branch.ROOT);
          termType.setSuppressible(fields[2].equals("Y"));
          termType.setExclude(fields[3].equals("Y"));
          termType.setNormExclude(fields[4].equals("Y"));
          termType.setTerminology(getProject().getTerminology());
          termType.setVersion(getProject().getVersion());
          ttyToAddMap.put(fields[5], termType);
        }
      }
      // If it Does already exist, update as necessary
      else {
        TermType loadedTermType = getCachedTermType(fields[5]);
        Boolean termTypeChanged = false;

        if ((loadedTermType.isSuppressible() && !fields[2].equals("Y"))
            || (!loadedTermType.isSuppressible() && fields[2].equals("Y"))) {
          termTypeChanged = true;
          loadedTermType.setSuppressible(fields[2].equals("Y"));
        }
        if ((loadedTermType.isExclude() && !fields[3].equals("Y"))
            || (!loadedTermType.isExclude() && fields[3].equals("Y"))) {
          termTypeChanged = true;
          loadedTermType.setExclude(fields[3].equals("Y"));
        }
        if ((loadedTermType.isNormExclude() && !fields[4].equals("Y"))
            || (!loadedTermType.isNormExclude() && fields[4].equals("Y"))) {
          termTypeChanged = true;
          loadedTermType.setNormExclude(fields[4].equals("Y"));
        }

        if (termTypeChanged) {
          logInfo("[MetadataLoader] Updating Term Type: " + loadedTermType);
          updateTermType(loadedTermType);
        }
      }
    }
    // After we finish going through both files, add everything from the map to
    // the database
    for (TermType newTermType : ttyToAddMap.values()) {
      logInfo("[MetadataLoader] Adding Term Type: " + newTermType);
      addTermType(newTermType);
    }
  }

  /**
   * Load MRDOC ATN lines. This is responsible for loading attribute names
   *
   * @throws Exception the exception
   */
  private void handleAttributeNames() throws Exception {

    logInfo("[MetadataLoader] Checking for new/updated Attribute names");

    // Count number of added AttributeNames, for logging
    int count = 0;

    //
    // Load ATN lines from the MRDOC file
    //
    List<String> lines = loadFileIntoStringList(getSrcDirFile(), "MRDOC.RRF",
        "ATN\\|(.*)", null);

    String fields[] = new String[4];

    for (String line : lines) {
      FieldedStringTokenizer.split(line, "|", 4, fields);

      // Field Description DOCKEY,VALUE,TYPE,EXPL
      // 0 DOCKEY
      // 1 VALUE
      // 2 TYPE
      // 3 EXPL

      // e.g.
      // ATN|OMIM_Number|expanded_form|OMIM Number|

      // Handle AttributeNames
      if (fields[2].equals("expanded_form")
          && getCachedAttributeName(fields[1]) == null) {
        final AttributeName atn = new AttributeNameJpa();
        atn.setAbbreviation(fields[1]);
        atn.setExpandedForm(fields[3]);
        atn.setTerminology(getProject().getTerminology());
        atn.setVersion(getProject().getVersion());
        atn.setPublished(false);
        atn.setPublishable(true);
        addAttributeName(atn);
        count++;
        getCachedAttributeNames().put(fields[1], atn);
      }
    }

    logInfo("[MetadataLoader] Added " + count + " new Attribute Names.");
  }

  /**
   * Load MRDOC RELA lines. This is responsible for loading additional
   * relationship types
   *
   * @throws Exception the exception
   */
  private void handleAdditionalRelationshipTypes() throws Exception {

    logInfo(
        "[MetadataLoader] Checking for new/updated Additional Relationship Types");

    // Count number of added handleAdditionalRelationshipTypes, for logging
    int count = 0;

    final Map<String, AdditionalRelationshipType> relaToAddMap =
        new HashMap<>();
    final Map<String, String> inverseRelaMap = new HashMap<>();

    //
    // Load RELA lines from the MRDOC file
    //
    List<String> lines = loadFileIntoStringList(getSrcDirFile(), "MRDOC.RRF",
        "RELA\\|(.*)", null);

    String fields[] = new String[4];

    for (String line : lines) {
      FieldedStringTokenizer.split(line, "|", 4, fields);

      // Field Description DOCKEY,VALUE,TYPE,EXPL
      // 0 DOCKEY
      // 1 VALUE
      // 2 TYPE
      // 3 EXPL

      // e.g.
      // RELA|Has_Salt_Form|expanded_form|Has Salt Form|
      // RELA|Has_Salt_Form|rela_inverse|Has_Free_Acid_Or_Base_Form|

      // Handle AdditionalRelationshipTypes
      if (fields[2].equals("expanded_form")
          && getCachedAdditionalRelationshipType(fields[1]) == null) {
        // Add if it does not yet exist
        final AdditionalRelationshipType rela =
            new AdditionalRelationshipTypeJpa();
        rela.setAbbreviation(fields[1]);
        rela.setExpandedForm(fields[3]);
        rela.setTerminology(getProject().getTerminology());
        rela.setVersion(getProject().getVersion());
        rela.setPublished(false);
        rela.setPublishable(true);
        // DL fields are all left false, with no domain/range
        // no equivalent types or supertypes included
        relaToAddMap.put(fields[1], rela);
      } else if (fields[2].equals("rela_inverse")) {
        inverseRelaMap.put(fields[1], fields[3]);
      }
    }

    // Add all of the new AdditionalRelationshipTypes
    for (Map.Entry<String, AdditionalRelationshipType> entry : relaToAddMap
        .entrySet()) {
      String abbreviation = entry.getKey();
      AdditionalRelationshipType rela = entry.getValue();

      rela = addAdditionalRelationshipType(rela);
      count++;
      getCachedAdditionalRelationshipTypes().put(abbreviation, rela);
    }

    // Set the inverses, if they exist, and update
    for (Map.Entry<String, AdditionalRelationshipType> entry : relaToAddMap
        .entrySet()) {
      String abbreviation = entry.getKey();
      AdditionalRelationshipType rela = entry.getValue();
      String inverseRelaAbbreviation = inverseRelaMap.get(abbreviation);
      AdditionalRelationshipType inverseRela =
          getCachedAdditionalRelationshipType(inverseRelaAbbreviation);

      if (inverseRela != null) {
        rela.setInverse(inverseRela);
        updateAdditionalRelationshipType(rela);
        getCachedAdditionalRelationshipTypes().put(abbreviation, rela);
      }
    }

    logInfo("[MetadataLoader] Added " + count
        + " new Additional Relationship Types.");

  }

  /* see superclass */
  @Override
  public void checkProperties(Properties p) throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public List<AlgorithmParameter> getParameters() {
    final List<AlgorithmParameter> params = super.getParameters();

    return params;
  }

  @Override
  public String getDescription() {
    return "Loads and processes MRDOC.RRF into metadata objects.";
  }

}