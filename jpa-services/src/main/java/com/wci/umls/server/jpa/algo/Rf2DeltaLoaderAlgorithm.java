/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.persistence.Query;

import org.apache.log4j.Logger;

import com.wci.umls.server.AlgorithmParameter;
import com.wci.umls.server.ReleaseInfo;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.FieldedStringTokenizer;
import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.jpa.AlgorithmParameterJpa;
import com.wci.umls.server.jpa.ReleaseInfoJpa;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.AtomSubsetJpa;
import com.wci.umls.server.jpa.content.AtomSubsetMemberJpa;
import com.wci.umls.server.jpa.content.AttributeJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.ConceptRelationshipJpa;
import com.wci.umls.server.jpa.content.ConceptSubsetJpa;
import com.wci.umls.server.jpa.content.ConceptSubsetMemberJpa;
import com.wci.umls.server.jpa.content.MapSetJpa;
import com.wci.umls.server.jpa.content.MappingJpa;
import com.wci.umls.server.jpa.meta.AdditionalRelationshipTypeJpa;
import com.wci.umls.server.jpa.meta.AttributeNameJpa;
import com.wci.umls.server.jpa.meta.GeneralMetadataEntryJpa;
import com.wci.umls.server.jpa.meta.LanguageJpa;
import com.wci.umls.server.jpa.meta.PropertyChainJpa;
import com.wci.umls.server.jpa.meta.TermTypeJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.AtomSubset;
import com.wci.umls.server.model.content.AtomSubsetMember;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.Component;
import com.wci.umls.server.model.content.ComponentHasAttributes;
import com.wci.umls.server.model.content.ComponentHasAttributesAndName;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.content.ConceptSubset;
import com.wci.umls.server.model.content.ConceptSubsetMember;
import com.wci.umls.server.model.content.MapSet;
import com.wci.umls.server.model.content.Mapping;
import com.wci.umls.server.model.content.Subset;
import com.wci.umls.server.model.content.SubsetMember;
import com.wci.umls.server.model.meta.AdditionalRelationshipType;
import com.wci.umls.server.model.meta.AttributeName;
import com.wci.umls.server.model.meta.CodeVariantType;
import com.wci.umls.server.model.meta.GeneralMetadataEntry;
import com.wci.umls.server.model.meta.IdType;
import com.wci.umls.server.model.meta.Language;
import com.wci.umls.server.model.meta.NameVariantType;
import com.wci.umls.server.model.meta.PropertyChain;
import com.wci.umls.server.model.meta.TermType;
import com.wci.umls.server.model.meta.UsageType;
import com.wci.umls.server.model.workflow.WorkflowStatus;
import com.wci.umls.server.services.RootService;
import com.wci.umls.server.services.helpers.PushBackReader;

/**
 * Implementation of an algorithm to import RF2 delta data.
 */
public class Rf2DeltaLoaderAlgorithm
    extends AbstractTerminologyLoaderAlgorithm {

  /** The isa type rel. */
  private final static String isaTypeRel = "116680003";

  /** The release version date. */
  private Date releaseVersionDate;

  /** The readers. */
  private Rf2Readers readers;

  /** counter for objects created, reset in each load section. */
  int objectCt; //

  /** The map of terminologyId to id. */
  private Map<String, Long> idMap = new HashMap<>();

  /** The pn recompute ids. */
  private Set<Long> pnRecomputeIds = new HashSet<>();

  /** The atom subset map. */
  private Map<String, AtomSubset> atomSubsetMap = new HashMap<>();

  /** The concept subset map. */
  private Map<String, ConceptSubset> conceptSubsetMap = new HashMap<>();

  /** The subsets updated. */
  private Set<String> subsetsUpdated = new HashSet<>();

  /** The map set map. */
  private Map<String, MapSet> mapSetMap = new HashMap<>();

  /** The map sets updated. */
  private Set<String> mapSetsUpdated = new HashSet<>();

  /** The term types. */
  private Set<String> termTypes = new HashSet<>();

  /** The additional rel types. */
  private Set<String> additionalRelTypes = new HashSet<>();

  /** The languages. */
  private Set<String> languages = new HashSet<>();

  /** The attribute names. */
  private Set<String> attributeNames = new HashSet<>();

  /** The concept attribute values. */
  private Set<String> generalEntryValues = new HashSet<>();

  /** The loader. */
  private final String loader = "loader";

  /** The init pref name. */
  private final String initPrefName =
      "Default prefered name could not be determined";

  /** The RF2 File sorting algorithm. */
  private final Rf2FileSorter sorter = new Rf2FileSorter();

  /**
   * Instantiates an empty {@link Rf2DeltaLoaderAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public Rf2DeltaLoaderAlgorithm() throws Exception {
    super();
  }

  /* see superclass */
  @Override
  public String getFileVersion() throws Exception {
    Rf2FileSorter sorter = new Rf2FileSorter();
    sorter.setInputDir(getInputPath());
    return sorter.getFileVersion();
  }

  /* see superclass */
  @Override
  public void compute() throws Exception {

    logInfo("Start loading delta");
    logInfo("  terminology = " + getTerminology());
    logInfo("  version = " + getVersion());
    logInfo("  inputPath = " + getInputPath());
    logInfo("  sorting files = " + isSortFiles());
    logInfo("  releaseVersion = " + (getReleaseVersion() == null
        ? "COMPUTE FROM FILES" : getReleaseVersion()));
    logInfo("  readers = " + (readers == null ? null : "PASSED IN"));

    long startTimeOrig = System.nanoTime();

    // check prerequisites
    if (getTerminology() == null) {
      throw new Exception("Terminology name must be specified");
    }
    if (getVersion() == null) {
      throw new Exception("Terminology version must be specified");
    }
    if (getInputPath() == null) {
      throw new Exception("Input directory must be specified");
    }

    // File preparation
    // Check the input directory
    File inputPathFile = new File(getInputPath());
    if (!inputPathFile.exists()) {
      throw new Exception("Specified input directory does not exist");
    }

    // control transaction scope
    setTransactionPerOperation(false);
    // Turn of ID computation when loading a terminology
    setAssignIdentifiersFlag(false);
    // Let loader set last modified flags.
    setLastModifiedFlag(false);
    // Turn off action handling
    setMolecularActionFlag(false);

    // faster performance.
    beginTransaction();

    // Read RF2 from the "sorted" directory
    File outputDir = new File(inputPathFile, "/RF2-sorted-temp/");

    // Sort files if indicated (otherwise sorted externally, e.g. by "full"
    // loader)
    if (isSortFiles()) {

      // Sort files
      logInfo("  Sort RF2 Files");
      logInfo("    sort by effective time: false");
      logInfo("    require all files     : false");

      // prepare the sorting algorithm
      sorter.setInputDir(getInputPath());
      sorter.setOutputDir(outputDir.getAbsolutePath());
      sorter.setSortByEffectiveTime(false);
      sorter.setRequireAllFiles(true);
      sorter.compute();
    }

    // Compute the release version if not passed in
    if (getReleaseVersion() == null) {
      setReleaseVersion(getFileVersion());
    }
    releaseVersionDate = ConfigUtility.DATE_FORMAT.parse(getReleaseVersion());
    Logger.getLogger(getClass())
        .info("  releaseVersion = " + getReleaseVersion());

    // Open readers if not opened externally
    boolean leaveReadersOpen = readers != null;
    if (!leaveReadersOpen) {
      readers = new Rf2Readers(new File(getInputPath() + "/RF2-sorted-temp/"));
      readers.openReaders();
    }

    //
    // Load concepts
    //
    logInfo("    Loading Concepts ...");
    loadConcepts();

    //
    // Load atoms and definitions
    //
    logInfo("    Loading Atoms ...");
    loadAtoms();

    logInfo("    Loading Definitions ...");
    loadDefinitions();

    //
    // Cache subsets and members
    //
    cacheSubsetsAndMembers();

    //
    // Load language refset members
    // This also caches all atom subset members
    //
    logInfo("    Loading Language Ref Sets...");
    loadLanguageRefsetMembers();

    // Compute preferred names
    final PrecedenceList list =
        this.getPrecedenceList(getTerminology(), getVersion());
    logInfo("  Compute preferred names for modified concepts");
    int ct = 0;
    for (final Long id : this.pnRecomputeIds) {
      final Concept concept = getConcept(id);
      final String pn = getComputedPreferredName(concept, list);
      if (!pn.equals(concept.getName())) {
        ct++;
        concept.setName(pn);
        Logger.getLogger(getClass())
            .debug("      compute concept pn = " + concept);
        updateConcept(concept);
      }
      if (ct > 0 && ct % logCt == 0) {
        logAndCommit(ct, RootService.logCt, RootService.commitCt);
      }
    }

    commitClearBegin();

    //
    // Load relationships - stated and inferred
    //
    logInfo("    Loading Relationships ...");
    loadRelationships();

    commitClearBegin();

    //
    // Load simple refset members
    // This also caches all concept subset members
    //
    logInfo("    Loading Simple Ref Sets...");
    loadSimpleRefSetMembers();

    commitClearBegin();

    //
    // Load simple map refset members
    //
    logInfo("    Loading Simple Map Ref Sets...");
    loadSimpleMapRefSetMembers();

    commitClearBegin();

    //
    // Load complex map refset members
    //
    logInfo("    Loading Complex and Extended Map Ref Sets...");
    loadAllMapRefSetMembers();

    //
    // Load atom type refset members
    //
    logInfo("    Loading Description Type Ref Sets...");
    loadDescriptionTypeRefSetMembers();

    //
    // Load refset descriptor refset members
    //
    logInfo("    Loading Refset Descriptor Ref Sets...");
    loadRefsetDescriptorRefSetMembers();

    //
    // Load module dependency refset members
    //
    logInfo("    Loading Module Dependency Ref Sets...");
    loadModuleDependencyRefSetMembers();

    commitClearBegin();

    //
    // Load module dependency refset members
    //
    logInfo("    Loading Attribute Value Ref Sets...");
    loadAttributeValueRefSetMembers();

    commitClearBegin();

    //
    // Load association reference refset members
    //
    logInfo("    Loading Association Reference Ref Sets...");
    loadAssociationReferenceRefSetMembers();

    commitClearBegin();

    //
    // Load metadata
    //
    logInfo("    Loading Metadata...");
    loadMetadata();

    //
    // Commit the content changes
    //
    logInfo("    changed = " + ct);
    logInfo("  Committing");
    commitClearBegin();

    //
    // Create ReleaseInfo for this release if it does not already exist
    //
    ReleaseInfo info = getReleaseInfo(getTerminology(), getReleaseVersion());
    if (info == null) {
      info = new ReleaseInfoJpa();
      info.setName(getReleaseVersion());
      info.setDescription(
          getTerminology() + " " + getReleaseVersion() + " release");
      info.setPlanned(false);
      info.setPublished(true);
      info.setTerminology(getTerminology());
      info.setVersion(getVersion());
      info.setLastModified(releaseVersionDate);
      info.setLastModifiedBy(loader);
      info.setTimestamp(new Date());
      addReleaseInfo(info);
    }

    commitClearBegin();

    // Close readers only if not externally passed in
    if (!leaveReadersOpen) {
      readers.closeReaders();
    }

    // Remove sort directory if sorting was done locally
    if (isSortFiles()) {
      ConfigUtility
          .deleteDirectory(new File(getInputPath(), "/RF2-sorted-temp/"));
    }

    // Final logging messages
    logInfo("      elapsed time = " + getTotalElapsedTimeStr(startTimeOrig));

    logInfo(getComponentStats(getTerminology(), getVersion(), Branch.ROOT)
        .toString());
    logInfo("Done ...");
    commit();

  }

  /* see superclass */
  @Override
  public void reset() throws Exception {
    // do nothing
  }

  /**
   * Loads the concepts from the delta files.
   *
   * @throws Exception the exception
   */
  private void loadConcepts() throws Exception {

    // Cache concept ids
    final Query query =
        manager.createQuery("select a.terminologyId, a.id from ConceptJpa a "
            + "where version = :version " + "and terminology = :terminology ");
    query.setParameter("terminology", getTerminology());
    query.setParameter("version", getVersion());
    @SuppressWarnings("unchecked")
    final List<Object[]> results = query.getResultList();
    for (final Object[] result : results) {
      idMap.put(result[0].toString(), Long.valueOf(result[1].toString()));
    }

    // Setup vars
    String line;
    objectCt = 0;
    int objectsAdded = 0;
    int objectsUpdated = 0;

    // Iterate through concept reader
    PushBackReader reader = readers.getReader(Rf2Readers.Keys.CONCEPT);
    while ((line = reader.readLine()) != null) {

      // Split line
      String fields[] = FieldedStringTokenizer.split(line, "\t");

      // if not header
      if (!fields[0].equals("id")) {

        // Skip if the effective time is before the release version
        if (fields[1].compareTo(getReleaseVersion()) < 0) {
          continue;
        }

        // Stop if the effective time is past the release version
        if (fields[1].compareTo(getReleaseVersion()) > 0) {
          reader.push(line);
          break;
        }

        // Check if concept exists from before
        Concept concept = idMap.containsKey(fields[0])
            ? getConcept(idMap.get(fields[0])) : null;

        // Setup delta concept (either new or based on existing one)
        Concept concept2 = null;
        if (concept == null) {
          concept2 = new ConceptJpa();
        } else {
          // Initialize attributes (for comparison)
          concept.getAttributes().size();
          concept2 = new ConceptJpa(concept, true);
        }

        // Set fields
        final Date date = ConfigUtility.DATE_FORMAT.parse(fields[1]);
        concept2.setTerminologyId(fields[0]);
        concept2.setTimestamp(date);
        concept2.setObsolete(fields[2].equals("0"));
        // This is SNOMED specific
        concept2.setFullyDefined(fields[4].equals("900000000000073002"));
        concept2.setTerminology(getTerminology());
        concept2.setVersion(getVersion());
        concept2.setName(initPrefName);
        concept2.setLastModifiedBy(loader);
        concept2.setLastModified(releaseVersionDate);
        concept2.setPublished(true);
        concept2.setPublishable(true);
        concept2.setWorkflowStatus(WorkflowStatus.PUBLISHED);
        concept2.setWorkflowStatus(WorkflowStatus.PUBLISHED);
        concept2.setUsesRelationshipUnion(true);

        // Attributes
        Attribute attribute = null;
        if (concept != null) {
          attribute = concept.getAttributeByName("moduleId");
        } else {
          attribute = new AttributeJpa();
          concept2.getAttributes().add(attribute);
        }
        setCommonFields(attribute, date);
        attribute.setName("moduleId");
        attribute.setValue(fields[3].intern());
        cacheAttributeMetadata(attribute);

        Attribute attribute2 = null;
        if (concept != null) {
          attribute2 = concept.getAttributeByName("definitionStatusId");
        } else {
          attribute2 = new AttributeJpa();
          concept2.getAttributes().add(attribute2);
        }
        setCommonFields(attribute2, date);
        attribute2.setName("definitionStatusId");
        attribute2.setValue(fields[4].intern());
        cacheAttributeMetadata(attribute2);

        // If concept is new, add it and all of its attributes
        if (concept == null) {
          Logger.getLogger(getClass()).debug("      add att - " + attribute);
          addAttribute(attribute, concept2);
          Logger.getLogger(getClass()).debug("      add att - " + attribute2);
          addAttribute(attribute2, concept2);
          Logger.getLogger(getClass()).debug("      add concept - " + concept2);
          concept2 = addConcept(concept2);
          idMap.put(concept2.getTerminologyId(), concept2.getId());
          pnRecomputeIds.add(concept2.getId());
          objectsAdded++;
        }

        // If concept has changed, update it and any changed attributes
        else if (!Rf2EqualityUtility.equals(concept2, concept)) {
          if (!concept.equals(concept2)) {
            Logger.getLogger(getClass())
                .debug("      update concept - " + concept2);
            updateConcept(concept2);
            pnRecomputeIds.add(concept2.getId());
          }
          updateAttributes(concept2, concept);
          objectsUpdated++;
        }

        // Log and commit
        /*
         * if ((objectsAdded + objectsUpdated) % logCt == 0) {
         * logAndCommit(objectsAdded + objectsUpdated); }
         */
      }
      commitClearBegin();
    }

    logAndCommit(objectsAdded + objectsUpdated, RootService.logCt,
        RootService.commitCt);

    logInfo("      new = " + objectsAdded);
    logInfo("      updated = " + objectsUpdated);

  }

  /**
   * Load atoms.
   *
   * @throws Exception the exception
   */
  private void loadAtoms() throws Exception {

    // Cache description (and definition) ids
    final Query query =
        manager.createQuery("select a.terminologyId, a.id from AtomJpa a "
            + "where version = :version " + "and terminology = :terminology ");
    query.setParameter("terminology", getTerminology());
    query.setParameter("version", getVersion());
    @SuppressWarnings("unchecked")
    final List<Object[]> results = query.getResultList();
    for (final Object[] result : results) {
      idMap.put(result[0].toString(), Long.valueOf(result[1].toString()));
    }

    final Set<Concept> modifiedConcepts = new HashSet<>();

    // Setup vars
    String line = "";
    objectCt = 0;
    int objectsAdded = 0;
    int objectsUpdated = 0;
    // Iterate through atom reader
    PushBackReader reader = readers.getReader(Rf2Readers.Keys.DESCRIPTION);
    while ((line = reader.readLine()) != null) {
      // split line
      String fields[] = FieldedStringTokenizer.split(line, "\t");

      // if not header
      if (!fields[0].equals("id")) {

        // Skip if the effective time is before the release version
        if (fields[1].compareTo(getReleaseVersion()) < 0) {
          continue;
        }

        // Stop if the effective time is past the release version
        if (fields[1].compareTo(getReleaseVersion()) > 0) {
          reader.push(line);
          break;
        }

        // Get concept from cache or from db
        Concept concept = null;
        if (idMap.containsKey(fields[4])) {
          concept = getConcept(idMap.get(fields[4]));
        } else {
          // if the concept is new, it will have been added
          // if the concept is existing it will either have been udpated
          // or will be in the existing concept cache
          throw new Exception(
              "Concept of atom should already exist: " + fields[4]);
        }

        // if the concept is not null
        if (concept != null) {

          // Load atom from cache or db
          Atom atom = null;
          if (idMap.containsKey(fields[0])) {
            atom = getAtom(idMap.get(fields[0]));
          }

          // Setup delta atom (either new or based on existing one)
          Atom atom2 = null;
          if (atom == null) {
            atom2 = new AtomJpa();
          } else {
            atom.getAttributes().size();
            atom2 = new AtomJpa(atom, true);
          }

          // Set fields
          final Date date = ConfigUtility.DATE_FORMAT.parse(fields[1]);
          atom2.setTerminologyId(fields[0]);
          atom2.setTimestamp(date);
          atom2.setLastModifiedBy(loader);
          atom2.setLastModified(releaseVersionDate);
          atom2.setObsolete(fields[2].equals("0"));
          atom2.setSuppressible(atom2.isObsolete());
          atom2.setConceptId(fields[4]);
          atom2.setDescriptorId("");
          atom2.setCodeId("");
          atom2.setLexicalClassId("");
          atom2.setStringClassId("");
          atom2.setLanguage(fields[5].intern());
          languages.add(atom2.getLanguage());
          atom2.setTermType(fields[6].intern());
          generalEntryValues.add(atom2.getTermType());
          termTypes.add(atom2.getTermType());
          atom2.setName(fields[7]);
          atom2.setTerminology(getTerminology());
          atom2.setVersion(getVersion());
          atom2.setPublished(true);
          atom2.setPublishable(true);
          atom2.setWorkflowStatus(WorkflowStatus.PUBLISHED);

          // Attributes
          Attribute attribute = null;
          if (atom != null) {
            attribute = atom.getAttributeByName("moduleId");
          } else {
            attribute = new AttributeJpa();
            atom2.getAttributes().add(attribute);
          }
          setCommonFields(attribute, date);
          attribute.setName("moduleId");
          attribute.setValue(fields[3].intern());
          cacheAttributeMetadata(attribute);

          Attribute attribute2 = null;
          if (atom != null) {
            attribute2 = atom.getAttributeByName("caseSignificanceId");
          } else {
            attribute2 = new AttributeJpa();
            atom2.getAttributes().add(attribute2);
          }
          setCommonFields(attribute2, date);
          attribute2.setName("caseSignificanceId");
          attribute2.setValue(fields[8].intern());
          cacheAttributeMetadata(attribute2);

          // If atom is new, add it
          if (atom == null) {
            Logger.getLogger(getClass()).debug("      add att - " + attribute);
            addAttribute(attribute, atom2);
            Logger.getLogger(getClass()).debug("      add att - " + attribute2);
            addAttribute(attribute2, atom2);
            Logger.getLogger(getClass()).debug("      add atom - " + atom2);
            atom2 = addAtom(atom2);
            idMap.put(atom2.getTerminologyId(), atom2.getId());
            concept.getAtoms().add(atom2);
            modifiedConcepts.add(concept);
            objectsAdded++;
          }

          // If atom has changed, update it
          else if (!Rf2EqualityUtility.equals(atom2, atom)) {
            if (!atom.equals(atom2)) {
              Logger.getLogger(getClass())
                  .debug("      update atom - " + atom2);
              updateAtom(atom2);
              concept.getAtoms().remove(atom);
              concept.getAtoms().add(atom2);
              modifiedConcepts.add(concept);
            }
            updateAttributes(atom2, atom);
            objectsUpdated++;
          }

          if ((objectsAdded + objectsUpdated) % logCt == 0) {
            for (final Concept modifiedConcept : modifiedConcepts) {
              Logger.getLogger(getClass())
                  .debug("      update concept - " + modifiedConcept);
              updateConcept(modifiedConcept);
              pnRecomputeIds.add(modifiedConcept.getId());
            }
            logAndCommit(objectsAdded + objectsUpdated, RootService.logCt,
                RootService.commitCt);
            modifiedConcepts.clear();
          }

        }

        // Major error if there is a delta atom with a
        // non-existent concept
        else {
          throw new Exception(
              "Could not find concept " + fields[4] + " for atom " + fields[0]);
        }
      }
    }

    // Handle modified concepts
    for (final Concept modifiedConcept : modifiedConcepts) {
      Logger.getLogger(getClass())
          .debug("      update concept - " + modifiedConcept);
      updateConcept(modifiedConcept);
      pnRecomputeIds.add(modifiedConcept.getId());
    }
    commitClearBegin();
    modifiedConcepts.clear();

    logInfo("      new = " + objectsAdded);
    logInfo("      updated = " + objectsUpdated);
  }

  /**
   * Load definitions.
   *
   * @throws Exception the exception
   */
  private void loadDefinitions() throws Exception {

    // Already loaded definitions into idMap in loadAtoms()

    // Setup vars
    Set<Concept> modifiedConcepts = new HashSet<>();
    String line = "";
    objectCt = 0;
    int objectsAdded = 0;
    int objectsUpdated = 0;
    // Iterate through atom reader
    PushBackReader reader = readers.getReader(Rf2Readers.Keys.DESCRIPTION);
    while ((line = reader.readLine()) != null) {
      // split line
      String fields[] = FieldedStringTokenizer.split(line, "\t");

      // if not header
      if (!fields[0].equals("id")) {

        // Skip if the effective time is before the release version
        if (fields[1].compareTo(getReleaseVersion()) < 0) {
          continue;
        }

        // Stop if the effective time is past the release version
        if (fields[1].compareTo(getReleaseVersion()) > 0) {
          reader.push(line);
          break;
        }

        // Get concept from cache or from db
        Concept concept = null;
        if (idMap.containsKey(fields[4])) {
          concept = getConcept(idMap.get(fields[4]));
        } else {
          // if the concept is new, it will have been added
          // if the concept is existing it will either have been udpated
          // or will be in the existing concept cache
          throw new Exception(
              "Concept of atom should already exist: " + fields[4]);
        }

        // if the concept is not null
        if (concept != null) {

          // Load atom from cache or db
          Atom def = null;
          if (idMap.containsKey(fields[0])) {
            def = getAtom(idMap.get(fields[0]));
          }

          // Setup delta atom (either new or based on existing one)
          Atom def2 = null;
          if (def == null) {
            def2 = new AtomJpa();
          } else {
            def.getAttributes().size();
            def2 = new AtomJpa(def, true);
          }

          // Set fields
          final Date date = ConfigUtility.DATE_FORMAT.parse(fields[1]);
          def2.setTerminologyId(fields[0]);
          def2.setTimestamp(date);
          def2.setObsolete(fields[2].equals("0"));
          def2.setLanguage(fields[5]);
          languages.add(def2.getLanguage());
          def2.setTermType(fields[6]);
          generalEntryValues.add(def2.getTermType());
          termTypes.add(def2.getTermType());

          def2.setName(fields[7]);
          def2.setTerminology(getTerminology());
          def2.setVersion(getVersion());
          def2.setLastModifiedBy(loader);
          def2.setLastModified(releaseVersionDate);
          def2.setPublished(true);
          def2.setWorkflowStatus(WorkflowStatus.PUBLISHED);
          def2.setDescriptorId("");
          def2.setCodeId("");
          def2.setLexicalClassId("");
          def2.setStringClassId("");

          // Attributes
          Attribute attribute = null;
          if (def != null) {
            attribute = def.getAttributeByName("moduleId");
          } else {
            attribute = new AttributeJpa();
            def2.getAttributes().add(attribute);
          }
          setCommonFields(attribute, date);
          attribute.setName("moduleId");
          attribute.setValue(fields[3].intern());
          cacheAttributeMetadata(attribute);

          Attribute attribute2 = null;
          if (def != null) {
            attribute2 = def.getAttributeByName("caseSignificanceId");
          } else {
            attribute2 = new AttributeJpa();
            def2.getAttributes().add(attribute2);
          }
          setCommonFields(attribute2, date);
          attribute2.setName("caseSignificanceId");
          attribute2.setValue(fields[8].intern());
          cacheAttributeMetadata(attribute2);

          // If atom is new, add it
          if (def == null) {
            Logger.getLogger(getClass()).debug("      add att - " + attribute);
            addAttribute(attribute, def2);
            Logger.getLogger(getClass()).debug("      add att - " + attribute2);
            addAttribute(attribute2, def2);
            Logger.getLogger(getClass())
                .debug("      add definition - " + def2);
            def2 = addAtom(def2);
            idMap.put(def2.getTerminologyId(), def2.getId());
            concept.getAtoms().add(def2);
            modifiedConcepts.add(concept);
            objectsAdded++;
          }

          // If atom has changed, update it
          else if (!Rf2EqualityUtility.equals(def2, def)) {
            if (!def.equals(def2)) {
              Logger.getLogger(getClass())
                  .debug("      update definition - " + def2);
              updateAtom(def2);
              concept.getAtoms().remove(def);
              concept.getAtoms().add(def2);
              modifiedConcepts.add(concept);
            }
            updateAttributes(def2, def);
            objectsUpdated++;
          }

          if ((objectsAdded + objectsUpdated) % logCt == 0) {
            for (final Concept modifiedConcept : modifiedConcepts) {
              Logger.getLogger(getClass())
                  .debug("      update concept - " + modifiedConcept);
              updateConcept(modifiedConcept);
              pnRecomputeIds.add(modifiedConcept.getId());
            }
            logAndCommit(objectsAdded + objectsUpdated, RootService.logCt,
                RootService.commitCt);
            modifiedConcepts.clear();
          }

        }

        // Major error if there is a delta atom with a
        // non-existent concept
        else {
          throw new Exception(
              "Could not find concept " + fields[4] + " for atom " + fields[0]);
        }
      }
    }

    for (final Concept modifiedConcept : modifiedConcepts) {
      Logger.getLogger(getClass())
          .debug("      update concept - " + modifiedConcept);
      updateConcept(modifiedConcept);
      pnRecomputeIds.add(modifiedConcept.getId());
    }
    commitClearBegin();
    modifiedConcepts.clear();

    logInfo("      new = " + objectsAdded);
    logInfo("      updated = " + objectsUpdated);
  }

  /**
   * Load language ref set members.
   *
   * @throws Exception the exception
   */
  private void loadLanguageRefsetMembers() throws Exception {

    // Cache atom subset members
    final Query query = manager
        .createQuery("select a.terminologyId, a.id from AtomSubsetMemberJpa a "
            + "where version = :version " + "and terminology = :terminology ");
    query.setParameter("terminology", getTerminology());
    query.setParameter("version", getVersion());
    @SuppressWarnings("unchecked")
    final List<Object[]> results = query.getResultList();
    for (final Object[] result : results) {
      idMap.put(result[0].toString(), Long.valueOf(result[1].toString()));
    }

    final Set<Atom> modifiedAtoms = new HashSet<>();

    // Setup variables
    String line = "";
    int objectsAdded = 0;
    int objectsUpdated = 0;

    // Iterate through language refset reader
    final PushBackReader reader = readers.getReader(Rf2Readers.Keys.LANGUAGE);
    while ((line = reader.readLine()) != null) {

      // split line
      String fields[] = FieldedStringTokenizer.split(line, "\t");

      // if not header
      if (!fields[0].equals("id")) {
        // Skip if the effective time is before the release version
        if (fields[1].compareTo(getReleaseVersion()) < 0) {
          continue;
        }

        // Stop if the effective time is past the release version
        if (fields[1].compareTo(getReleaseVersion()) > 0) {
          reader.push(line);
          break;
        }

        // Ensure effective time is set on all appropriate objects
        AtomSubsetMember member = null;
        if (idMap.containsKey(fields[0])) {
          member = (AtomSubsetMember) getSubsetMember(idMap.get(fields[0]),
              AtomSubsetMemberJpa.class);
        }

        // Setup delta language entry (either new or based on existing
        // one)
        AtomSubsetMember member2 = null;
        if (member == null) {
          member2 = new AtomSubsetMemberJpa();
        } else {
          member.getAttributes().size();
          member.getSubset().getName();
          member2 = new AtomSubsetMemberJpa(member, true);
        }

        // Populate and handle subset aspects of member
        refsetHelper(member2, fields);

        // Add acceptabilityId attribute
        final Date date = ConfigUtility.DATE_FORMAT.parse(fields[1]);
        Attribute attribute = null;
        if (member != null) {
          attribute = member.getAttributeByName("acceptabilityId");
        } else {
          attribute = new AttributeJpa();
          member2.getAttributes().add(attribute);
        }
        setCommonFields(attribute, date);
        attribute.setName("acceptabilityId");
        attribute.setValue(fields[6].intern());
        cacheAttributeMetadata(attribute);

        final Atom atom = getAtom(member2.getMember().getId());

        // If language refset entry is new, add it
        if (member == null) {
          for (final Attribute att : member2.getAttributes()) {
            Logger.getLogger(getClass()).debug("      add attribute = " + att);
            addAttribute(att, member2);
          }

          Logger.getLogger(getClass())
              .debug("      add language refset member = " + member2);
          member2 = (AtomSubsetMember) addSubsetMember(member2);
          idMap.put(member2.getTerminologyId(), member2.getId());
          atom.getMembers().add(member2);
          modifiedAtoms.add(atom);
          objectsAdded++;
        }

        // If language refset entry is changed, update it
        else if (!member2.equals(member) || !Rf2EqualityUtility
            .compareAttributes(member2, member, new String[] {
                "moduleId", "acceptabilityId"
        })) {
          Logger.getLogger(getClass()).debug("  update language - " + member2);
          if (!member.equals(member2)) {
            Logger.getLogger(getClass())
                .debug("      update langauge refset member - " + member2);
            updateSubsetMember(member2);
            atom.getMembers().remove(member);
            atom.getMembers().add(member2);
            modifiedAtoms.add(atom);
          }
          updateAttributes(member2, member);
          objectsUpdated++;

        }

        if ((objectsAdded + objectsUpdated) % logCt == 0) {
          for (final Atom modifiedAtom : modifiedAtoms) {
            Logger.getLogger(getClass())
                .debug("      update atom - " + modifiedAtom);
            updateAtom(modifiedAtom);
          }
          logAndCommit(objectsAdded + objectsUpdated, RootService.logCt,
              RootService.commitCt);
          modifiedAtoms.clear();
        }

      }
    }

    for (final Atom modifiedAtom : modifiedAtoms) {
      Logger.getLogger(getClass()).debug("      update atom - " + modifiedAtom);
      updateAtom(modifiedAtom);
    }
    commitClearBegin();
    modifiedAtoms.clear();

    logInfo("      new = " + objectsAdded);
    logInfo("      updated = " + objectsUpdated);

  }

  /**
   * Load simple ref set members.
   *
   * @throws Exception the exception
   */
  private void loadSimpleRefSetMembers() throws Exception {

    // Cache concept subset members
    final Query query = manager.createQuery(
        "select a.terminologyId, a.id from ConceptSubsetMemberJpa a "
            + "where version = :version " + "and terminology = :terminology ");
    query.setParameter("terminology", getTerminology());
    query.setParameter("version", getVersion());
    @SuppressWarnings("unchecked")
    final List<Object[]> results = query.getResultList();
    for (final Object[] result : results) {
      idMap.put(result[0].toString(), Long.valueOf(result[1].toString()));
    }

    Set<Concept> modifiedConcepts = new HashSet<>();

    // Setup variables
    String line = "";
    int objectsAdded = 0;
    int objectsUpdated = 0;

    // Iterate through simple refset reader
    final PushBackReader reader = readers.getReader(Rf2Readers.Keys.SIMPLE);
    while ((line = reader.readLine()) != null) {

      // split line
      String fields[] = FieldedStringTokenizer.split(line, "\t");

      // if not header
      if (!fields[0].equals("id")) {
        // Skip if the effective time is before the release version
        if (fields[1].compareTo(getReleaseVersion()) < 0) {
          continue;
        }

        // Stop if the effective time is past the release version
        if (fields[1].compareTo(getReleaseVersion()) > 0) {
          reader.push(line);
          break;
        }

        // Ensure effective time is set on all appropriate objects
        ConceptSubsetMember member = null;
        if (idMap.containsKey(fields[0])) {
          member = (ConceptSubsetMember) getSubsetMember(idMap.get(fields[0]),
              ConceptSubsetMemberJpa.class);
        }

        // Setup delta simple entry (either new or based on existing
        // one)
        ConceptSubsetMember member2 = null;
        if (member == null) {
          member2 = new ConceptSubsetMemberJpa();
        } else {
          member.getAttributes().size();
          member.getSubset().getName();
          member2 = new ConceptSubsetMemberJpa(member, true);
        }

        // Populate and handle subset aspects of member
        refsetHelper(member2, fields);

        // no attributes for simple refset member

        final Concept concept = getConcept(member2.getMember().getId());

        // If simple refset entry is new, add it
        if (member == null) {
          for (final Attribute att : member2.getAttributes()) {
            Logger.getLogger(getClass()).debug("      add attribute = " + att);
            addAttribute(att, member2);
          }

          Logger.getLogger(getClass())
              .debug("      add simple refset member = " + member2);
          member2 = (ConceptSubsetMember) addSubsetMember(member2);
          idMap.put(member2.getTerminologyId(), member2.getId());
          concept.getMembers().add(member2);
          modifiedConcepts.add(concept);
          objectsAdded++;
        }

        // If simple refset entry is changed, update it
        else if (!member2.equals(member) || !Rf2EqualityUtility
            .compareAttributes(member2, member, new String[] {
                "moduleId"
        })) {
          Logger.getLogger(getClass()).debug("  update simple - " + member2);
          if (!member.equals(member2)) {
            Logger.getLogger(getClass())
                .debug("      update simple refset member - " + member2);
            updateSubsetMember(member2);
            concept.getMembers().remove(member);
            concept.getMembers().add(member2);
            modifiedConcepts.add(concept);
          }
          updateAttributes(member2, member);
          objectsUpdated++;

        }

        if ((objectsAdded + objectsUpdated) % logCt == 0) {
          for (final Concept modifiedConcept : modifiedConcepts) {
            Logger.getLogger(getClass())
                .debug("      update concept - " + modifiedConcept);
            updateConcept(modifiedConcept);
          }
          logAndCommit(objectsAdded + objectsUpdated, RootService.logCt,
              RootService.commitCt);
          modifiedConcepts.clear();
        }

      }
    }

    for (final Concept modifiedConcept : modifiedConcepts) {
      Logger.getLogger(getClass())
          .debug("      update concept - " + modifiedConcept);
      updateConcept(modifiedConcept);
    }
    commitClearBegin();
    modifiedConcepts.clear();

    logInfo("      new = " + objectsAdded);
    logInfo("      updated = " + objectsUpdated);

  }

  /**
   * Load simple map ref set members.
   *
   * @throws Exception the exception
   */
  private void loadSimpleMapRefSetMembers() throws Exception {

    Set<Concept> modifiedConcepts = new HashSet<>();

    // Setup variables
    String line = "";
    int objectsAdded = 0;
    int objectsUpdated = 0;

    // Iterate through simple refset reader
    PushBackReader reader = readers.getReader(Rf2Readers.Keys.SIMPLE_MAP);
    while ((line = reader.readLine()) != null) {

      // split line
      String fields[] = FieldedStringTokenizer.split(line, "\t");

      // if not header
      if (!fields[0].equals("id")) {
        // Skip if the effective time is before the release version
        if (fields[1].compareTo(getReleaseVersion()) < 0) {
          continue;
        }

        // Stop if the effective time is past the release version
        if (fields[1].compareTo(getReleaseVersion()) > 0) {
          reader.push(line);
          break;
        }

        // Ensure effective time is set on all appropriate objects
        ConceptSubsetMember member = null;
        if (idMap.containsKey(fields[0])) {
          member = (ConceptSubsetMember) getSubsetMember(idMap.get(fields[0]),
              ConceptSubsetMemberJpa.class);
        }

        // Setup delta simple map entry (either new or based on existing
        // one)
        ConceptSubsetMember member2 = null;
        if (member == null) {
          member2 = new ConceptSubsetMemberJpa();
        } else {
          member.getAttributes().size();
          member.getSubset().getName();
          member2 = new ConceptSubsetMemberJpa(member, true);
        }

        // Populate and handle subset aspects of member
        refsetHelper(member2, fields);

        // Add mapTarget attribute
        final Date date = ConfigUtility.DATE_FORMAT.parse(fields[1]);
        Attribute attribute = null;
        if (member != null) {
          attribute = member.getAttributeByName("mapTarget");
        } else {
          attribute = new AttributeJpa();
          member2.getAttributes().add(attribute);
        }
        setCommonFields(attribute, date);
        attribute.setName("mapTarget");
        attribute.setValue(fields[6].intern());
        // Don't do this for "mapTarget"
        // cacheAttributeMetadata(attribute);

        final Concept concept = getConcept(member2.getMember().getId());

        // If simple map refset entry is new, add it
        if (member == null) {
          for (final Attribute att : member2.getAttributes()) {
            Logger.getLogger(getClass()).debug("      add attribute = " + att);
            addAttribute(att, member2);
          }

          Logger.getLogger(getClass())
              .debug("      add simple map refset member = " + member2);
          member2 = (ConceptSubsetMember) addSubsetMember(member2);
          idMap.put(member2.getTerminologyId(), member2.getId());
          concept.getMembers().add(member2);
          modifiedConcepts.add(concept);
          objectsAdded++;
        }

        // If simple refset entry is changed, update it
        else if (!member2.equals(member) || !Rf2EqualityUtility
            .compareAttributes(member2, member, new String[] {
                "moduleId", "mapTarget"
        })) {
          Logger.getLogger(getClass()).debug("  update simple - " + member2);
          if (!member.equals(member2)) {
            Logger.getLogger(getClass())
                .debug("      update simple map refset member - " + member2);
            updateSubsetMember(member2);
            concept.getMembers().remove(member);
            concept.getMembers().add(member2);
            modifiedConcepts.add(concept);
          }
          updateAttributes(member2, member);
          objectsUpdated++;

        }

        if ((objectsAdded + objectsUpdated) % logCt == 0) {
          for (final Concept modifiedConcept : modifiedConcepts) {
            Logger.getLogger(getClass())
                .debug("      update concept - " + modifiedConcept);
            updateConcept(modifiedConcept);
          }
          logAndCommit(objectsAdded + objectsUpdated, RootService.logCt,
              RootService.commitCt);
          modifiedConcepts.clear();
        }

      }
    }

    for (final Concept modifiedConcept : modifiedConcepts) {
      Logger.getLogger(getClass())
          .debug("      update concept - " + modifiedConcept);
      updateConcept(modifiedConcept);
    }
    commitClearBegin();
    modifiedConcepts.clear();

    logInfo("      new = " + objectsAdded);
    logInfo("      updated = " + objectsUpdated);

  }

  /**
   * Load complex/extended map refset entries.
   *
   * @throws Exception the exception
   */
  private void loadAllMapRefSetMembers() throws Exception {

    // Cache mapping ids
    Query query =
        manager.createQuery("select a.terminologyId, a.id from MappingJpa a "
            + "where version = :version " + "and terminology = :terminology ");
    query.setParameter("terminology", getTerminology());
    query.setParameter("version", getVersion());
    @SuppressWarnings("unchecked")
    final List<Object[]> results = query.getResultList();
    for (final Object[] result : results) {
      idMap.put(result[0].toString(), Long.valueOf(result[1].toString()));
    }

    // Cache mapsets
    query = manager.createQuery("select a from MapSetJpa a "
        + "where version = :version " + "and terminology = :terminology ");
    query.setParameter("terminology", getTerminology());
    query.setParameter("version", getVersion());
    @SuppressWarnings("unchecked")
    final List<MapSet> mapSetResults = query.getResultList();
    for (final MapSet result : mapSetResults) {
      mapSetMap.put(result.getTerminologyId(), result);
      // lazy initialize
      result.getAttributes().size();
    }

    PushBackReader reader = readers.getReader(Rf2Readers.Keys.COMPLEX_MAP);
    loadMapSetMembers(reader);
    reader = readers.getReader(Rf2Readers.Keys.EXTENDED_MAP);
    loadMapSetMembers(reader);

  }

  /**
   * Load map set members.
   *
   * @param reader the reader
   * @throws Exception the exception
   */
  private void loadMapSetMembers(PushBackReader reader) throws Exception {
    // Setup variables
    String line = "";
    objectCt = 0;
    int objectsAdded = 0;
    int objectsUpdated = 0;

    // Iterate through relationships reader
    while ((line = reader.readLine()) != null) {

      // Split line
      String fields[] = FieldedStringTokenizer.split(line, "\t");

      // If not header
      if (!fields[0].equals("id")) {

        // Skip if the effective time is before the release version
        if (fields[1].compareTo(getReleaseVersion()) < 0) {
          continue;
        }

        // Stop if the effective time is past the release version
        if (fields[1].compareTo(getReleaseVersion()) > 0) {
          reader.push(line);
          break;
        }

        // Retrieve mapping if it exists
        Mapping mapping = null;
        if (idMap.containsKey(fields[0])) {
          mapping = getMapping(idMap.get(fields[0]));
        }

        // Setup delta mapping (either new or based on existing one)
        Mapping mapping2 = null;
        if (mapping == null) {
          mapping2 = new MappingJpa();
        } else {
          mapping.getAttributes().size();
          mapping2 = new MappingJpa(mapping, true);
        }

        // Handle mapping and map set so that mapping2 reflects current state
        mappingHelper(mapping2, fields);

        // If mapping is new, add it
        if (mapping == null) {
          for (final Attribute att : mapping2.getAttributes()) {
            Logger.getLogger(getClass()).debug("      add attribute = " + att);
            addAttribute(att, mapping2);
          }
          Logger.getLogger(getClass()).debug("      add mapping = " + mapping2);
          mapping2 = addMapping(mapping2);
          idMap.put(mapping2.getTerminologyId(), mapping2.getId());
          objectsAdded++;
        }

        // If mapping has changed, update it
        else if (!Rf2EqualityUtility.equals(mapping2, mapping)) {
          if (!mapping.equals(mapping2)) {
            Logger.getLogger(getClass())
                .debug("      update mapping - " + mapping2);
            updateMapping(mapping2);
          }
          updateAttributes(mapping2, mapping);
          objectsUpdated++;
        }

        logAndCommit(objectsAdded + objectsUpdated, RootService.logCt,
            RootService.commitCt);

      }
    }

    logAndCommit(objectsAdded + objectsUpdated, RootService.logCt,
        RootService.commitCt);

    logInfo("      new = " + objectsAdded);
    logInfo("      updated = " + objectsUpdated);

  }

  /**
   * Load atom type ref set members.
   *
   * @throws Exception the exception
   */
  private void loadDescriptionTypeRefSetMembers() throws Exception {
    Set<Concept> modifiedConcepts = new HashSet<>();

    // Setup variables
    String line = "";
    int objectsAdded = 0;
    int objectsUpdated = 0;

    // Iterate through simple refset reader
    PushBackReader reader = readers.getReader(Rf2Readers.Keys.DESCRIPTION_TYPE);
    while ((line = reader.readLine()) != null) {

      // split line
      String fields[] = FieldedStringTokenizer.split(line, "\t");

      // if not header
      if (!fields[0].equals("id")) {
        // Skip if the effective time is before the release version
        if (fields[1].compareTo(getReleaseVersion()) < 0) {
          continue;
        }

        // Stop if the effective time is past the release version
        if (fields[1].compareTo(getReleaseVersion()) > 0) {
          reader.push(line);
          break;
        }

        // Ensure effective time is set on all appropriate objects
        ConceptSubsetMember member = null;
        if (idMap.containsKey(fields[0])) {
          member = (ConceptSubsetMember) getSubsetMember(idMap.get(fields[0]),
              ConceptSubsetMemberJpa.class);
        }

        // Setup delta simple entry (either new or based on existing
        // one)
        ConceptSubsetMember member2 = null;
        if (member == null) {
          member2 = new ConceptSubsetMemberJpa();
        } else {
          member.getAttributes().size();
          member.getSubset().getName();
          member2 = new ConceptSubsetMemberJpa(member, true);
        }

        // Populate and handle subset aspects of member
        refsetHelper(member2, fields);

        // Add descriptionFormat attribute
        final Date date = ConfigUtility.DATE_FORMAT.parse(fields[1]);
        Attribute attribute = null;
        if (member != null) {
          attribute = member.getAttributeByName("descriptionFormat");
        } else {
          attribute = new AttributeJpa();
          member2.getAttributes().add(attribute);
        }
        setCommonFields(attribute, date);
        attribute.setName("descriptionFormat");
        attribute.setValue(fields[6].intern());
        cacheAttributeMetadata(attribute);

        // Add descriptionLength attribute
        if (member != null) {
          attribute = member.getAttributeByName("descriptionLength");
        } else {
          attribute = new AttributeJpa();
          member2.getAttributes().add(attribute);
        }
        setCommonFields(attribute, date);
        attribute.setName("descriptionLength");
        attribute.setValue(fields[7].intern());
        // don't do this for descriptionLength
        // cacheAttributeMetadata(attribute);

        final Concept concept = getConcept(member2.getMember().getId());

        // If atom type refset entry is new, add it
        if (member == null) {
          for (final Attribute att : member2.getAttributes()) {
            Logger.getLogger(getClass()).debug("      add attribute = " + att);
            addAttribute(att, member2);
          }

          Logger.getLogger(getClass())
              .debug("      add atom type refset member = " + member2);
          member2 = (ConceptSubsetMember) addSubsetMember(member2);
          idMap.put(member2.getTerminologyId(), member2.getId());
          concept.getMembers().add(member2);
          modifiedConcepts.add(concept);
          objectsAdded++;
        }

        // If atom type refset entry is changed, update it
        else if (!member2.equals(member) || !Rf2EqualityUtility
            .compareAttributes(member2, member, new String[] {
                "moduleId", "descriptionFormat", "descriptionLength"
        })) {
          Logger.getLogger(getClass())
              .debug("  update atom type  - " + member2);
          if (!member.equals(member2)) {
            Logger.getLogger(getClass())
                .debug("      update atom type refset member - " + member2);
            updateSubsetMember(member2);
            concept.getMembers().remove(member);
            concept.getMembers().add(member2);
            modifiedConcepts.add(concept);
          }
          updateAttributes(member2, member);
          objectsUpdated++;

        }

        if ((objectsAdded + objectsUpdated) % logCt == 0) {
          for (final Concept modifiedConcept : modifiedConcepts) {
            Logger.getLogger(getClass())
                .debug("      update concept - " + modifiedConcept);
            updateConcept(modifiedConcept);
          }
          logAndCommit(objectsAdded + objectsUpdated, RootService.logCt,
              RootService.commitCt);
          modifiedConcepts.clear();
        }

      }
    }

    for (final Concept modifiedConcept : modifiedConcepts) {
      Logger.getLogger(getClass())
          .debug("      update concept - " + modifiedConcept);
      updateConcept(modifiedConcept);
    }
    commitClearBegin();
    modifiedConcepts.clear();

    logInfo("      new = " + objectsAdded);
    logInfo("      updated = " + objectsUpdated);

  }

  /**
   * Load refset descriptor ref set members.
   *
   * @throws Exception the exception
   */
  private void loadRefsetDescriptorRefSetMembers() throws Exception {
    Set<Concept> modifiedConcepts = new HashSet<>();

    // Setup variables
    String line = "";
    int objectsAdded = 0;
    int objectsUpdated = 0;

    // Iterate through module dependency refset reader
    PushBackReader reader =
        readers.getReader(Rf2Readers.Keys.REFSET_DESCRIPTOR);
    while ((line = reader.readLine()) != null) {

      // split line
      String fields[] = FieldedStringTokenizer.split(line, "\t");

      // if not header
      if (!fields[0].equals("id")) {
        // Skip if the effective time is before the release version
        if (fields[1].compareTo(getReleaseVersion()) < 0) {
          continue;
        }

        // Stop if the effective time is past the release version
        if (fields[1].compareTo(getReleaseVersion()) > 0) {
          reader.push(line);
          break;
        }

        // Ensure effective time is set on all appropriate objects
        ConceptSubsetMember member = null;
        if (idMap.containsKey(fields[0])) {
          member = (ConceptSubsetMember) getSubsetMember(idMap.get(fields[0]),
              ConceptSubsetMemberJpa.class);
        }

        // Setup delta module dependency entry (either new or based on existing
        // one)
        ConceptSubsetMember member2 = null;
        if (member == null) {
          member2 = new ConceptSubsetMemberJpa();
        } else {
          member.getAttributes().size();
          member.getSubset().getName();
          member2 = new ConceptSubsetMemberJpa(member, true);
        }

        // Populate and handle subset aspects of member
        refsetHelper(member2, fields);

        // Add attributeDescription attribute
        final Date date = ConfigUtility.DATE_FORMAT.parse(fields[1]);
        Attribute attribute = null;
        if (member != null) {
          attribute = member.getAttributeByName("attributeDescription");
        } else {
          attribute = new AttributeJpa();
          member2.getAttributes().add(attribute);
        }
        setCommonFields(attribute, date);
        attribute.setName("attributeDescription");
        attribute.setValue(fields[6].intern());
        cacheAttributeMetadata(attribute);

        // Add attributeType attribute
        if (member != null) {
          attribute = member.getAttributeByName("attributeType");
        } else {
          attribute = new AttributeJpa();
          member2.getAttributes().add(attribute);
        }
        setCommonFields(attribute, date);
        attribute.setName("attributeType");
        attribute.setValue(fields[7].intern());
        cacheAttributeMetadata(attribute);

        // Add attributeOrder attribute
        if (member != null) {
          attribute = member.getAttributeByName("attributeOrder");
        } else {
          attribute = new AttributeJpa();
          member2.getAttributes().add(attribute);
        }
        setCommonFields(attribute, date);
        attribute.setName("attributeOrder");
        attribute.setValue(fields[8].intern());
        cacheAttributeMetadata(attribute);

        final Concept concept = getConcept(member2.getMember().getId());

        // If refset descriptor refset entry is new, add it
        if (member == null) {
          for (final Attribute att : member2.getAttributes()) {
            Logger.getLogger(getClass()).debug("      add attribute = " + att);
            addAttribute(att, member2);
          }

          Logger.getLogger(getClass())
              .debug("      add refset descriptor refset member = " + member2);
          member2 = (ConceptSubsetMember) addSubsetMember(member2);
          idMap.put(member2.getTerminologyId(), member2.getId());
          concept.getMembers().add(member2);
          modifiedConcepts.add(concept);
          objectsAdded++;
        }

        // If refset descriptor refset entry is changed, update it
        else if (!member2.equals(member) || !Rf2EqualityUtility
            .compareAttributes(member2, member, new String[] {
                "moduleId", "attributeDescription", "attributeType",
                "attributeOrder"
        })) {
          Logger.getLogger(getClass())
              .debug("  update refset descriptor  - " + member2);
          if (!member.equals(member2)) {
            Logger.getLogger(getClass()).debug(
                "      update refset descriptor refset member - " + member2);
            updateSubsetMember(member2);
            concept.getMembers().remove(member);
            concept.getMembers().add(member2);
            modifiedConcepts.add(concept);
          }
          updateAttributes(member2, member);
          objectsUpdated++;

        }

        if ((objectsAdded + objectsUpdated) % logCt == 0) {
          for (final Concept modifiedConcept : modifiedConcepts) {
            Logger.getLogger(getClass())
                .debug("      update concept - " + modifiedConcept);
            updateConcept(modifiedConcept);
          }
          logAndCommit(objectsAdded + objectsUpdated, RootService.logCt,
              RootService.commitCt);
          modifiedConcepts.clear();
        }

      }
    }

    for (final Concept modifiedConcept : modifiedConcepts) {
      Logger.getLogger(getClass())
          .debug("      update concept - " + modifiedConcept);
      updateConcept(modifiedConcept);
    }
    commitClearBegin();
    modifiedConcepts.clear();

    logInfo("      new = " + objectsAdded);
    logInfo("      updated = " + objectsUpdated);

  }

  /**
   * Load module dependency ref set members.
   *
   * @throws Exception the exception
   */
  private void loadModuleDependencyRefSetMembers() throws Exception {
    Set<Concept> modifiedConcepts = new HashSet<>();

    // Setup variables
    String line = "";
    int objectsAdded = 0;
    int objectsUpdated = 0;

    // Iterate through module dependency refset reader
    PushBackReader reader =
        readers.getReader(Rf2Readers.Keys.MODULE_DEPENDENCY);
    while ((line = reader.readLine()) != null) {

      // split line
      String fields[] = FieldedStringTokenizer.split(line, "\t");

      // if not header
      if (!fields[0].equals("id")) {
        // Skip if the effective time is before the release version
        if (fields[1].compareTo(getReleaseVersion()) < 0) {
          continue;
        }

        // Stop if the effective time is past the release version
        if (fields[1].compareTo(getReleaseVersion()) > 0) {
          reader.push(line);
          break;
        }

        // Ensure effective time is set on all appropriate objects
        ConceptSubsetMember member = null;
        if (idMap.containsKey(fields[0])) {
          member = (ConceptSubsetMember) getSubsetMember(idMap.get(fields[0]),
              ConceptSubsetMemberJpa.class);
        }

        // Setup delta module dependency entry (either new or based on existing
        // one)
        ConceptSubsetMember member2 = null;
        if (member == null) {
          member2 = new ConceptSubsetMemberJpa();
        } else {
          member.getAttributes().size();
          member.getSubset().getName();
          member2 = new ConceptSubsetMemberJpa(member, true);
        }

        // Populate and handle subset aspects of member
        refsetHelper(member2, fields);

        // Add sourceEffectiveTime attribute
        final Date date = ConfigUtility.DATE_FORMAT.parse(fields[1]);
        Attribute attribute = null;
        if (member != null) {
          attribute = member.getAttributeByName("sourceEffectiveTime");
        } else {
          attribute = new AttributeJpa();
          member2.getAttributes().add(attribute);
        }
        setCommonFields(attribute, date);
        attribute.setName("sourceEffectiveTime");
        attribute.setValue(fields[6].intern());
        // not for this field
        // cacheAttributeMetadata(attribute);

        // Add targetEffectiveTime attribute
        if (member != null) {
          attribute = member.getAttributeByName("targetEffectiveTime");
        } else {
          attribute = new AttributeJpa();
          member2.getAttributes().add(attribute);
        }
        setCommonFields(attribute, date);
        attribute.setName("targetEffectiveTime");
        attribute.setValue(fields[7].intern());
        // not for this field
        // cacheAttributeMetadata(attribute);

        final Concept concept = getConcept(member2.getMember().getId());

        // If module dependency refset entry is new, add it
        if (member == null) {
          for (final Attribute att : member2.getAttributes()) {
            Logger.getLogger(getClass()).debug("      add attribute = " + att);
            addAttribute(att, member2);
          }

          Logger.getLogger(getClass())
              .debug("      add module dependency refset member = " + member2);
          member2 = (ConceptSubsetMember) addSubsetMember(member2);
          idMap.put(member2.getTerminologyId(), member2.getId());
          concept.getMembers().add(member2);
          modifiedConcepts.add(concept);
          objectsAdded++;
        }

        // If module dependency refset entry is changed, update it
        else if (!member2.equals(member) || !Rf2EqualityUtility
            .compareAttributes(member2, member, new String[] {
                "moduleId", "sourceEffectiveTime", "targetEffectiveTime"
        })) {
          Logger.getLogger(getClass())
              .debug("  update module dependency  - " + member2);
          if (!member.equals(member2)) {
            Logger.getLogger(getClass()).debug(
                "      update module dependency refset member - " + member2);
            updateSubsetMember(member2);
            concept.getMembers().remove(member);
            concept.getMembers().add(member2);
            modifiedConcepts.add(concept);
          }
          updateAttributes(member2, member);
          objectsUpdated++;

        }

        if ((objectsAdded + objectsUpdated) % logCt == 0) {
          for (final Concept modifiedConcept : modifiedConcepts) {
            Logger.getLogger(getClass())
                .debug("      update concept - " + modifiedConcept);
            updateConcept(modifiedConcept);
          }
          logAndCommit(objectsAdded + objectsUpdated, RootService.logCt,
              RootService.commitCt);
          modifiedConcepts.clear();
        }

      }
    }

    for (final Concept modifiedConcept : modifiedConcepts) {
      Logger.getLogger(getClass())
          .debug("      update concept - " + modifiedConcept);
      updateConcept(modifiedConcept);
    }
    commitClearBegin();
    modifiedConcepts.clear();

    logInfo("      new = " + objectsAdded);
    logInfo("      updated = " + objectsUpdated);

  }

  /**
   * Load attribute value ref set members.
   *
   * @throws Exception the exception
   */
  private void loadAttributeValueRefSetMembers() throws Exception {
    Set<Concept> modifiedConcepts = new HashSet<>();
    Set<Atom> modifiedAtoms = new HashSet<>();

    // Setup variables
    String line = "";
    int objectsAdded = 0;
    int objectsUpdated = 0;

    // Iterate through simple refset reader
    PushBackReader reader = readers.getReader(Rf2Readers.Keys.ATTRIBUTE_VALUE);
    while ((line = reader.readLine()) != null) {

      // split line
      String fields[] = FieldedStringTokenizer.split(line, "\t");

      // if not header
      if (!fields[0].equals("id")) {
        // Skip if the effective time is before the release version
        if (fields[1].compareTo(getReleaseVersion()) < 0) {
          continue;
        }

        // Stop if the effective time is past the release version
        if (fields[1].compareTo(getReleaseVersion()) > 0) {
          reader.push(line);
          break;
        }

        // Is the member a concept
        boolean isConcept = getConcept(idMap.get(fields[5])) != null;

        SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset> member =
            null;
        if (idMap.containsKey(fields[0])) {
          if (isConcept) {
            member = getSubsetMember(idMap.get(fields[0]),
                ConceptSubsetMemberJpa.class);
          } else {
            member = getSubsetMember(idMap.get(fields[0]),
                AtomSubsetMemberJpa.class);
          }
        }

        // Setup delta simple entry (either new or based on existing
        // one)
        SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset> member2 =
            null;
        if (member == null) {
          if (isConcept) {
            member2 = new ConceptSubsetMemberJpa();
          } else {
            member2 = new AtomSubsetMemberJpa();
          }
        } else {
          member.getAttributes().size();
          member.getSubset().getName();
          if (isConcept) {
            member2 =
                new ConceptSubsetMemberJpa((ConceptSubsetMember) member, true);
          } else {
            member2 = new AtomSubsetMemberJpa((AtomSubsetMember) member, true);
          }
        }

        // Populate and handle subset aspects of member
        refsetHelper(member2, fields);

        // Add valueId attribute
        final Date date = ConfigUtility.DATE_FORMAT.parse(fields[1]);
        Attribute attribute = null;
        if (member != null) {
          attribute = member.getAttributeByName("valueId");
        } else {
          attribute = new AttributeJpa();
          member2.getAttributes().add(attribute);
        }
        setCommonFields(attribute, date);
        attribute.setName("valueId");
        attribute.setValue(fields[6].intern());
        cacheAttributeMetadata(attribute);

        final Concept concept = getConcept(member2.getMember().getId());
        final Atom atom = getAtom(member2.getMember().getId());

        // If refset entry is new, add it
        if (member == null) {
          for (final Attribute att : member2.getAttributes()) {
            Logger.getLogger(getClass()).debug("      add attribute = " + att);
            addAttribute(att, member2);
          }

          Logger.getLogger(getClass())
              .debug("      add attribute value refset member = " + member2);
          member2 = addSubsetMember(member2);
          idMap.put(member2.getTerminologyId(), member2.getId());
          if (isConcept) {
            concept.getMembers().add((ConceptSubsetMember) member2);
            modifiedConcepts.add(concept);
          } else {
            atom.getMembers().add((AtomSubsetMember) member2);
            modifiedAtoms.add(atom);
          }
          objectsAdded++;
        }

        // If refset entry is changed, update it
        else if (!member2.equals(member) || !Rf2EqualityUtility
            .compareAttributes(member2, member, new String[] {
                "moduleId", "valueId"
        })) {
          Logger.getLogger(getClass()).debug("  update simple - " + member2);
          if (!member.equals(member2)) {
            Logger.getLogger(getClass()).debug(
                "      update attribute value refset member - " + member2);
            updateSubsetMember(member2);
            if (isConcept) {
              concept.getMembers().remove(member);
              concept.getMembers().add((ConceptSubsetMember) member2);
              modifiedConcepts.add(concept);
            } else {
              atom.getMembers().remove(member);
              atom.getMembers().add((AtomSubsetMember) member2);
              modifiedAtoms.add(atom);
            }
          }
          updateAttributes(member2, member);
          objectsUpdated++;

        }

        // Periodic commit
        if ((objectsAdded + objectsUpdated) % logCt == 0) {
          for (final Concept modifiedConcept : modifiedConcepts) {
            Logger.getLogger(getClass())
                .debug("      update concept - " + modifiedConcept);
            updateConcept(modifiedConcept);
          }
          for (final Atom modifiedAtom : modifiedAtoms) {
            Logger.getLogger(getClass())
                .debug("      update atom - " + modifiedAtom);
            updateAtom(modifiedAtom);
          }
          logAndCommit(objectsAdded + objectsUpdated, RootService.logCt,
              RootService.commitCt);
          modifiedAtoms.clear();
          modifiedConcepts.clear();
        }

      }
    }

    // Commit any remaining concept or atom changes
    for (final Concept modifiedConcept : modifiedConcepts) {
      Logger.getLogger(getClass())
          .debug("      update concept - " + modifiedConcept);
      updateConcept(modifiedConcept);
    }
    for (final Atom modifiedAtom : modifiedAtoms) {
      Logger.getLogger(getClass())
          .debug("      update concept - " + modifiedAtom);
      updateAtom(modifiedAtom);
    }
    commitClearBegin();
    modifiedConcepts.clear();
    modifiedAtoms.clear();

    logInfo("      new = " + objectsAdded);
    logInfo("      updated = " + objectsUpdated);

  }

  /**
   * Load association reference ref set members.
   *
   * @throws Exception the exception
   */
  private void loadAssociationReferenceRefSetMembers() throws Exception {

    Set<Concept> modifiedConcepts = new HashSet<>();

    // Setup variables
    String line = "";
    objectCt = 0;
    int objectsAdded = 0;
    int objectsUpdated = 0;

    // Iterate through relationships reader
    PushBackReader reader =
        readers.getReader(Rf2Readers.Keys.ASSOCIATION_REFERENCE);

    while ((line = reader.readLine()) != null) {

      // Split line
      String fields[] = FieldedStringTokenizer.split(line, "\t");

      // If not header
      if (!fields[0].equals("id")) {

        // Skip if the effective time is before the release version
        if (fields[1].compareTo(getReleaseVersion()) < 0) {
          continue;
        }

        // Stop if the effective time is past the release version
        if (fields[1].compareTo(getReleaseVersion()) > 0) {
          reader.push(line);
          break;
        }

        // Retrieve source concept
        Concept associationConcept = null;
        Concept sourceConcept = null;
        Concept destinationConcept = null;

        // retrieve association concept
        if (idMap.containsKey(fields[4])) {
          associationConcept = getConcept(idMap.get(fields[4]));
        }
        if (associationConcept == null) {
          Logger.getLogger(getClass()).warn(
              "Association reference member connected to nonexistent refset with terminology id "
                  + fields[5]);
          logWarn("  Line: " + line);
          continue;
          /*
           * throw new Exception( "Relationship " + fields[0] +
           * " association refset concept " + fields[4] + " cannot be found");
           */
        }

        // retrieve source concept
        if (idMap.containsKey(fields[5])) {
          sourceConcept = getConcept(idMap.get(fields[5]));
        }
        if (sourceConcept == null) {
          Logger.getLogger(getClass()).warn(
              "Association reference member connected to nonexistent source object with terminology id "
                  + fields[5]);
          logWarn("  Line: " + line);
          continue;
          /*
           * throw new Exception("Relationship " + fields[0] +
           * " source concept " + fields[5] + " cannot be found");
           */
        }

        // Retrieve destination concept
        if (idMap.containsKey(fields[6])) {
          destinationConcept = getConcept(idMap.get(fields[6]));
        }
        if (destinationConcept == null) {
          Logger.getLogger(getClass()).warn(
              "Association reference member connected to nonexistent target object with terminology id "
                  + fields[6]);
          logWarn("  Line: " + line);
          continue;
          /*
           * throw new Exception("Relationship " + fields[0] +
           * " destination concept " + fields[6] + " cannot be found");
           */
        }

        // Retrieve relationship if it exists
        ConceptRelationship rel = null;
        if (idMap.containsKey(fields[0])) {
          rel = (ConceptRelationship) getRelationship(idMap.get(fields[0]),
              ConceptRelationshipJpa.class);
        }

        // Setup delta relationship (either new or based on existing one)
        ConceptRelationship rel2 = null;
        if (rel == null) {
          rel2 = new ConceptRelationshipJpa();
        } else {
          rel.getAttributes().size();
          rel2 = new ConceptRelationshipJpa(rel, true);
        }

        // Set fields
        final Date date = ConfigUtility.DATE_FORMAT.parse(fields[1]);
        rel2.setTerminologyId(fields[0]);
        rel2.setTimestamp(date);
        rel2.setLastModified(releaseVersionDate);
        rel2.setObsolete(fields[2].equals("0")); // active
        rel2.setSuppressible(rel2.isObsolete());
        rel2.setRelationshipType("other"); // typeId
        rel2.setHierarchical(false);
        rel2.setAdditionalRelationshipType(fields[4]); // typeId
        rel2.setStated(false);
        rel2.setInferred(true);
        rel2.setTerminology(getTerminology());
        rel2.setVersion(getVersion());
        rel2.setLastModifiedBy(loader);
        rel2.setPublished(true);
        rel2.setPublishable(true);
        rel2.setWorkflowStatus(WorkflowStatus.PUBLISHED);
        rel2.setAssertedDirection(true);

        // ensure additional relationship type & general entry has been added
        generalEntryValues.add(rel2.getAdditionalRelationshipType());
        additionalRelTypes.add(rel2.getAdditionalRelationshipType());

        // get concepts from cache, they just need to have ids
        final Concept fromConcept = getConcept(idMap.get(fields[5]));
        final Concept toConcept = getConcept(idMap.get(fields[6]));
        if (fromConcept != null && toConcept != null) {
          rel2.setFrom(fromConcept);
          rel2.setTo(toConcept);
        } else {
          if (fromConcept == null) {
            throw new Exception("Relationship " + rel2.getTerminologyId()
                + " references non-existent source concept " + fields[5]);
          }
          if (toConcept == null) {
            throw new Exception("Relationship" + rel2.getTerminologyId()
                + " references non-existent destination concept " + fields[6]);
          }
        }
        // Attributes
        Attribute attribute = null;
        if (rel != null) {
          attribute = rel.getAttributeByName("moduleId");
        } else {
          attribute = new AttributeJpa();
          rel2.getAttributes().add(attribute);
        }
        setCommonFields(attribute, date);
        attribute.setName("moduleId");
        attribute.setValue(fields[3].intern());
        cacheAttributeMetadata(attribute);

        // If atom is new, add it
        if (rel == null) {
          addAttribute(attribute, rel2);
          Logger.getLogger(getClass()).debug("      add rel - " + rel2);
          rel2 = (ConceptRelationship) addRelationship(rel2);
          idMap.put(rel2.getTerminologyId(), rel2.getId());
          sourceConcept.getRelationships().add(rel2);
          modifiedConcepts.add(sourceConcept);
          objectsAdded++;
        }

        // If atom has changed, update it
        else if (!Rf2EqualityUtility.equals(rel2, rel)) {
          if (!rel.equals(rel2)) {
            Logger.getLogger(getClass()).debug("      update rel - " + rel2);
            updateRelationship(rel2);
            sourceConcept.getRelationships().remove(rel);
            sourceConcept.getRelationships().add(rel);
            modifiedConcepts.add(sourceConcept);
          }
          updateAttributes(rel2, rel);
          objectsUpdated++;
        }

        // if unchanged, log for debug
        else {
          Logger.getLogger(getClass()).debug("      unchanged rel - " + rel2);
        }

        if ((objectsAdded + objectsUpdated) % logCt == 0) {
          logAndCommit(objectsAdded + objectsUpdated, RootService.logCt,
              RootService.commitCt);
          for (final Concept modifiedConcept : modifiedConcepts) {
            Logger.getLogger(getClass())
                .debug("      update concept - " + modifiedConcept);
            updateConcept(modifiedConcept);
          }
          modifiedConcepts.clear();
        }

      }
    }

    // Commit any remaining concept or atom changes
    for (final Concept modifiedConcept : modifiedConcepts) {
      Logger.getLogger(getClass())
          .debug("      update concept - " + modifiedConcept);
      updateConcept(modifiedConcept);
    }

    commitClearBegin();
    modifiedConcepts.clear();

    logInfo("      new = " + objectsAdded);
    logInfo("      updated = " + objectsUpdated);

  }

  /**
   * Load relationships.
   *
   * @throws Exception the exception
   */
  private void loadRelationships() throws Exception {

    // Cache description (and definition) ids
    final Query query = manager.createQuery(
        "select a.terminologyId, a.id from ConceptRelationshipJpa a "
            + "where version = :version " + "and terminology = :terminology ");
    query.setParameter("terminology", getTerminology());
    query.setParameter("version", getVersion());
    @SuppressWarnings("unchecked")
    final List<Object[]> results = query.getResultList();
    for (final Object[] result : results) {
      idMap.put(result[0].toString(), Long.valueOf(result[1].toString()));
    }

    Set<Concept> modifiedConcepts = new HashSet<>();

    // Setup variables
    String line = "";
    objectCt = 0;
    int objectsAdded = 0;
    int objectsUpdated = 0;

    // Iterate through relationships reader
    final PushBackReader reader =
        readers.getReader(Rf2Readers.Keys.RELATIONSHIP);
    while ((line = reader.readLine()) != null) {

      // Split line
      String fields[] = FieldedStringTokenizer.split(line, "\t");

      // If not header
      if (!fields[0].equals("id")) {

        // Skip if the effective time is before the release version
        if (fields[1].compareTo(getReleaseVersion()) < 0) {
          continue;
        }

        // Stop if the effective time is past the release version
        if (fields[1].compareTo(getReleaseVersion()) > 0) {
          reader.push(line);
          break;
        }

        // Retrieve source concept
        Concept sourceConcept = null;
        Concept destinationConcept = null;
        if (idMap.containsKey(fields[4])) {
          sourceConcept = getConcept(idMap.get(fields[4]));
        }
        if (sourceConcept == null) {
          throw new Exception("Relationship " + fields[0] + " source concept "
              + fields[4] + " cannot be found");
        }

        // Retrieve destination concept
        if (idMap.containsKey(fields[5])) {
          destinationConcept = getConcept(idMap.get(fields[5]));
        }
        if (destinationConcept == null) {
          throw new Exception("Relationship " + fields[0]
              + " destination concept " + fields[5] + " cannot be found");
        }

        // Retrieve relationship if it exists
        ConceptRelationship rel = null;
        if (idMap.containsKey(fields[0])) {
          rel = (ConceptRelationship) getRelationship(idMap.get(fields[0]),
              ConceptRelationshipJpa.class);
        }

        // Setup delta relationship (either new or based on existing one)
        ConceptRelationship rel2 = null;
        if (rel == null) {
          rel2 = new ConceptRelationshipJpa();
        } else {
          rel.getAttributes().size();
          rel2 = new ConceptRelationshipJpa(rel, true);
        }

        // Set fields
        final Date date = ConfigUtility.DATE_FORMAT.parse(fields[1]);
        rel2.setTerminologyId(fields[0]);
        rel2.setTimestamp(date);
        rel2.setObsolete(fields[2].equals("0")); // active
        rel2.setSuppressible(rel2.isObsolete());
        rel2.setGroup(fields[6].intern()); // relationshipGroup
        rel2.setRelationshipType(
            fields[7].equals(isaTypeRel) ? "Is a" : "other"); // typeId
        rel2.setHierarchical(rel2.getRelationshipType().equals("Is a"));
        rel2.setAdditionalRelationshipType(fields[7]); // typeId
        generalEntryValues.add(rel2.getAdditionalRelationshipType());
        additionalRelTypes.add(rel2.getAdditionalRelationshipType());
        rel2.setStated(fields[8].equals("900000000000010007"));
        rel2.setInferred(fields[8].equals("900000000000011006"));
        rel2.setTerminology(getTerminology());
        rel2.setVersion(getVersion());
        rel2.setLastModified(releaseVersionDate);
        rel2.setLastModifiedBy(loader);
        rel2.setPublished(true);
        rel2.setPublishable(true);
        rel2.setWorkflowStatus(WorkflowStatus.PUBLISHED);
        rel2.setAssertedDirection(true);

        // get concepts from cache, they just need to have ids
        final Concept fromConcept = getConcept(idMap.get(fields[4]));
        final Concept toConcept = getConcept(idMap.get(fields[5]));
        if (fromConcept != null && toConcept != null) {
          rel2.setFrom(fromConcept);
          rel2.setTo(toConcept);
        } else {
          if (fromConcept == null) {
            throw new Exception("Relationship " + rel2.getTerminologyId()
                + " -existent source concept " + fields[4]);
          }
          if (toConcept == null) {
            throw new Exception("Relationship" + rel2.getTerminologyId()
                + " references non-existent destination concept " + fields[5]);
          }
        }
        // Attributes
        Attribute attribute = null;
        if (rel != null) {
          attribute = rel.getAttributeByName("moduleId");
        } else {
          attribute = new AttributeJpa();
          rel2.getAttributes().add(attribute);
        }
        setCommonFields(attribute, date);
        attribute.setName("moduleId");
        attribute.setValue(fields[3].intern());
        cacheAttributeMetadata(attribute);

        Attribute attribute2 = null;
        if (rel != null) {
          attribute2 = rel.getAttributeByName("characteristicTypeId");
        } else {
          attribute2 = new AttributeJpa();
          rel2.getAttributes().add(attribute2);
        }
        setCommonFields(attribute2, date);
        attribute2.setName("characteristicTypeId");
        attribute2.setValue(fields[8].intern());
        cacheAttributeMetadata(attribute2);

        Attribute attribute3 = null;
        if (rel != null) {
          attribute3 = rel.getAttributeByName("modifierId");
        } else {
          attribute3 = new AttributeJpa();
          rel2.getAttributes().add(attribute3);
        }
        setCommonFields(attribute3, date);
        attribute3.setName("modifierId");
        attribute3.setValue(fields[9].intern());
        cacheAttributeMetadata(attribute3);

        // If atom is new, add it
        if (rel == null) {
          addAttribute(attribute, rel2);
          addAttribute(attribute2, rel2);
          addAttribute(attribute3, rel2);
          Logger.getLogger(getClass()).debug("      add rel - " + rel2);
          rel2 = (ConceptRelationship) addRelationship(rel2);
          idMap.put(rel2.getTerminologyId(), rel2.getId());
          sourceConcept.getRelationships().add(rel2);
          modifiedConcepts.add(sourceConcept);
          objectsAdded++;
        }

        // If atom has changed, update it
        else if (!Rf2EqualityUtility.equals(rel2, rel)) {
          if (!rel.equals(rel2)) {
            Logger.getLogger(getClass()).debug("      update rel - " + rel2);
            updateRelationship(rel2);
            sourceConcept.getRelationships().remove(rel);
            sourceConcept.getRelationships().add(rel);
            modifiedConcepts.add(sourceConcept);
          }
          updateAttributes(rel2, rel);
          objectsUpdated++;
        }

        if ((objectsAdded + objectsUpdated) % logCt == 0) {
          logAndCommit(objectsAdded + objectsUpdated, RootService.logCt,
              RootService.commitCt);
          for (final Concept modifiedConcept : modifiedConcepts) {
            Logger.getLogger(getClass())
                .debug("      update concept - " + modifiedConcept);
            updateConcept(modifiedConcept);
          }
          modifiedConcepts.clear();
        }

      }
    }

    logAndCommit(objectsAdded + objectsUpdated, RootService.logCt,
        RootService.commitCt);
    for (final Concept modifiedConcept : modifiedConcepts) {
      Logger.getLogger(getClass())
          .debug("      update concept - " + modifiedConcept);
      updateConcept(modifiedConcept);
    }
    modifiedConcepts.clear();

    logInfo("      new = " + objectsAdded);
    logInfo("      updated = " + objectsUpdated);

  }

  /**
   * Update attributes for the given component. We can assume they have the same
   * attribute names in common.
   *
   * @param c1 the c1
   * @param c2 the c2
   * @throws Exception the exception
   */
  private void updateAttributes(ComponentHasAttributes c1,
    ComponentHasAttributes c2) throws Exception {
    for (final Attribute a1 : c1.getAttributes()) {
      final Attribute a2 = c2.getAttributeByName(a1.getName());
      if (a2 != null) {
        if (!a1.equals(a2)) {
          Logger.getLogger(getClass()).debug("      update attribute - " + a1);
          updateAttribute(a1, c1);
        }
      } else {
        throw new Exception(
            "Unexpected mismatching attribute: " + a1.getName());
      }
    }

  }

  /**
   * Cache subsets and members.
   *
   * @throws Exception the exception
   */
  private void cacheSubsetsAndMembers() throws Exception {
    // Cache existing subsets
    Query query = manager.createQuery("select a from AtomSubsetJpa a "
        + "where a.version = :version " + "and a.terminology = :terminology ");
    query.setParameter("terminology", getTerminology());
    query.setParameter("version", getVersion());
    @SuppressWarnings("unchecked")
    final List<AtomSubset> results = query.getResultList();
    for (final AtomSubset result : results) {
      atomSubsetMap.put(result.getTerminologyId(), result);
      // lazy initialize
      result.getAttributes().size();
    }

    query = manager.createQuery("select a from ConceptSubsetJpa a "
        + "where a.version = :version " + "and a.terminology = :terminology ");
    query.setParameter("terminology", getTerminology());
    query.setParameter("version", getVersion());
    @SuppressWarnings("unchecked")
    List<ConceptSubset> results2 = query.getResultList();
    for (ConceptSubset result : results2) {
      conceptSubsetMap.put(result.getTerminologyId(), result);
      // lazy initialize
      result.getAttributes().size();
    }

    // Cache subset members
    query = manager
        .createQuery("select a.terminologyId, a.id from AtomSubsetMemberJpa a "
            + "where a.version = :version "
            + "and a.terminology = :terminology ");
    query.setParameter("terminology", getTerminology());
    query.setParameter("version", getVersion());
    @SuppressWarnings("unchecked")
    List<Object[]> results3 = query.getResultList();
    for (Object[] result : results3) {
      idMap.put(result[0].toString(), Long.valueOf(result[1].toString()));
    }

    query = manager.createQuery(
        "select a.terminologyId, a.id from ConceptSubsetMemberJpa a "
            + "where a.version = :version "
            + "and a.terminology = :terminology ");
    query.setParameter("terminology", getTerminology());
    query.setParameter("version", getVersion());
    @SuppressWarnings("unchecked")
    List<Object[]> results4 = query.getResultList();
    for (Object[] result : results4) {
      idMap.put(result[0].toString(), Long.valueOf(result[1].toString()));
    }
  }

  /**
   * Refset helper.
   *
   * @param member the member
   * @param fields the fields
   * @throws Exception the exception
   */
  private void refsetHelper(
    SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset> member,
    String[] fields) throws Exception {

    if (idMap.get(fields[5]) != null) {
      // Retrieve concept -- tokens[5] is referencedComponentId
      final Concept concept = getConcept(idMap.get(fields[5]));
      if (concept != null) {
        ((ConceptSubsetMember) member).setMember(concept);
      }

      final Atom description = getAtom(idMap.get(fields[5]));
      if (description != null) {
        ((AtomSubsetMember) member).setMember(description);
      }

    }

    if (member.getMember() == null) {
      throw new Exception(
          "Refset member connected to nonexistent object - " + fields[5]);
    }

    // Universal RefSet attributes
    final Date date = ConfigUtility.DATE_FORMAT.parse(fields[1]);
    member.setTerminology(getTerminology());
    member.setVersion(getVersion());
    member.setTerminologyId(fields[0]);
    member.setTimestamp(date);
    member.setLastModified(date);
    member.setLastModifiedBy(loader);
    member.setObsolete(fields[2].equals("0"));
    member.setSuppressible(member.isObsolete());
    member.setPublished(true);
    member.setPublishable(true);

    manageSubset(member, fields, date);
    Attribute attribute = null;
    if (member.getAttributeByName("moduleId") != null) {
      attribute = member.getAttributeByName("moduleId");
    } else {
      attribute = new AttributeJpa();
      member.getAttributes().add(attribute);
    }
    setCommonFields(attribute, date);
    attribute.setName("moduleId");
    attribute.setValue(fields[3].intern());
    cacheAttributeMetadata(attribute);
  }

  /**
   * Finds or creates the corresponding subset object, wires it to the member.
   *
   * @param member the member
   * @param fields the fields
   * @param date the date
   * @throws Exception the exception
   */
  @SuppressWarnings("rawtypes")
  public void manageSubset(SubsetMember member, String[] fields, Date date)
    throws Exception {

    // if concept refset already exists, update it if needed
    if (conceptSubsetMap.containsKey(fields[4])) {
      final ConceptSubset subset = conceptSubsetMap.get(fields[4]);
      ((ConceptSubsetMember) member).setSubset(subset);

      // If we have not yet updated this, try now
      if (!subsetsUpdated.contains(fields[4])) {
        final ConceptSubset subset2 =
            (ConceptSubset) getSubset(subset.getId(), ConceptSubsetJpa.class);
        setCommonFields(subset2, date);
        subset2.setTerminologyId(fields[4].intern());
        final Concept concept = getConcept(idMap.get(fields[4]));
        subset2.setName(concept.getName());
        subset2.setObsolete(concept.isObsolete());
        subset2.setDescription(subset.getName());

        // Get latest attribute
        Attribute attribute = null;
        if (subset2.getAttributeByName("moduleId") != null) {
          attribute = subset2.getAttributeByName("moduleId");
        } else {
          attribute = new AttributeJpa();
          subset2.getAttributes().add(attribute);
        }
        setCommonFields(attribute, date);
        attribute.setName("moduleId");
        attribute.setValue(fields[3].intern());
        cacheAttributeMetadata(attribute);

        // Update object and attributes as needed
        if (!Rf2EqualityUtility.equals(subset2, subset)) {
          if (!subset.equals(subset2)) {
            Logger.getLogger(getClass())
                .debug("      update map set - " + subset2);
            updateSubset(subset2);
          }
          updateAttributes(subset2, subset);
        }
        subsetsUpdated.add(fields[4]);
      }

    }

    // if atom refset already exists, update it if needed
    else if (atomSubsetMap.containsKey(fields[4])) {
      final AtomSubset subset = atomSubsetMap.get(fields[4]);
      ((AtomSubsetMember) member).setSubset(subset);

      // If we have not yet updated this, try now
      if (!subsetsUpdated.contains(fields[4])) {
        final AtomSubset subset2 =
            (AtomSubset) getSubset(subset.getId(), AtomSubsetJpa.class);
        setCommonFields(subset2, date);
        subset2.setTerminologyId(fields[4].intern());
        final Concept concept = getConcept(idMap.get(fields[4]));
        subset2.setName(concept.getName());
        subset2.setObsolete(concept.isObsolete());
        subset2.setDescription(subset.getName());

        // get latest attribute
        Attribute attribute = null;
        if (subset2.getAttributeByName("moduleId") != null) {
          attribute = subset2.getAttributeByName("moduleId");
        } else {
          attribute = new AttributeJpa();
          subset2.getAttributes().add(attribute);
        }
        setCommonFields(attribute, date);
        attribute.setName("moduleId");
        attribute.setValue(fields[3].intern());
        cacheAttributeMetadata(attribute);

        // Update object and attributes as needed
        if (!Rf2EqualityUtility.equals(subset2, subset)) {
          if (!subset.equals(subset2)) {
            Logger.getLogger(getClass())
                .debug("      update map set - " + subset2);
            updateSubset(subset2);
          }
          updateAttributes(subset2, subset);
        }
        subsetsUpdated.add(fields[4]);
      }
    }

    // If atom refset does not exists, create it
    else if (member instanceof AtomSubsetMember
        && !atomSubsetMap.containsKey(fields[4])) {

      final AtomSubset subset = new AtomSubsetJpa();
      setCommonFields(subset, date);
      subset.setTerminologyId(fields[4].intern());
      final Concept concept = getConcept(idMap.get(fields[4]));
      subset.setName(concept.getName());
      subset.setObsolete(concept.isObsolete());
      subset.setDescription(subset.getName());

      final Attribute attribute = new AttributeJpa();
      setCommonFields(attribute, date);
      attribute.setName("moduleId");
      attribute.setValue(concept.getAttributeByName("moduleId").getValue());
      subset.getAttributes().add(attribute);
      addAttribute(attribute, member);
      cacheAttributeMetadata(attribute);

      addSubset(subset);
      atomSubsetMap.put(fields[4], subset);
      commitClearBegin();

      ((AtomSubsetMember) member).setSubset(subset);

    }

    // If concept refset does not exists, create it
    else if (member instanceof ConceptSubsetMember
        && !conceptSubsetMap.containsKey(fields[4])) {

      final ConceptSubset subset = new ConceptSubsetJpa();
      setCommonFields(subset, date);
      subset.setTerminologyId(fields[4].intern());
      final Concept concept = getConcept(idMap.get(fields[4]));
      subset.setName(concept.getName());
      subset.setObsolete(concept.isObsolete());
      subset.setDescription(subset.getName());
      subset.setDisjointSubset(false);

      final Attribute attribute = new AttributeJpa();
      setCommonFields(attribute, date);
      attribute.setName("moduleId");
      attribute.setValue(concept.getAttributeByName("moduleId").getValue());
      subset.getAttributes().add(attribute);
      addAttribute(attribute, member);
      cacheAttributeMetadata(attribute);

      addSubset(subset);
      conceptSubsetMap.put(fields[4], subset);
      commitClearBegin();

      ((ConceptSubsetMember) member).setSubset(subset);

    } else {
      throw new Exception("Unable to determine refset type.");
    }
  }

  /**
   * Map set helper.
   *
   * @param mapping the mapping
   * @param fields the fields
   * @throws Exception the exception
   */
  public void mappingHelper(Mapping mapping, String[] fields) throws Exception {

    // Verify that referencedComponentId exists
    if (idMap.get(fields[5]) != null) {
      // do nothing
    } else if (fields[5].equals("367491007")) {
      // Continue - this is a known issue in SNOMED
      return;
    } else {
      throw new Exception(
          "Mapping member connected to nonexistent object - " + fields[5]);
    }

    // Universal mapping attributes
    final Date date = ConfigUtility.DATE_FORMAT.parse(fields[1]);
    mapping.setTerminology(getTerminology());
    mapping.setVersion(getVersion());
    mapping.setTerminologyId(fields[0]);
    mapping.setTimestamp(date);
    mapping.setLastModified(date);
    mapping.setLastModifiedBy(loader);
    mapping.setObsolete(fields[2].equals("0"));
    mapping.setSuppressible(mapping.isObsolete());
    mapping.setPublished(true);
    mapping.setPublishable(true);
    mapping.setGroup(fields[6].intern());
    mapping.setRelationshipType("other");
    mapping.setAdditionalRelationshipType(fields[11]);
    generalEntryValues.add(mapping.getAdditionalRelationshipType());
    additionalRelTypes.add(mapping.getAdditionalRelationshipType());
    mapping.setGroup(fields[6]);
    mapping.setRank(fields[7]);
    mapping.setRule(fields[8]);
    mapping.setAdvice(fields[9]);

    // get concepts from cache, they just need to have ids
    final Concept fromConcept = getConcept(idMap.get(fields[5]));
    if (fromConcept != null) {
      mapping.setFromTerminologyId(fromConcept.getTerminologyId());
      mapping.setFromIdType(IdType.CONCEPT);

    } else {
      throw new Exception("mapping " + mapping.getTerminologyId()
          + " -existent source concept " + fields[5]);
    }
    // Handle "to" terminology id
    mapping.setToTerminologyId(fields[10]);
    mapping.setToIdType(IdType.OTHER);

    // Handle map set maintenance
    manageMapSet(mapping, fields, date);

    Attribute attribute = null;
    if (mapping.getAttributeByName("moduleId") != null) {
      attribute = mapping.getAttributeByName("moduleId");
    } else {
      attribute = new AttributeJpa();
      mapping.getAttributes().add(attribute);
    }
    setCommonFields(attribute, date);
    attribute.setName("moduleId");
    attribute.setValue(fields[3].intern());
    cacheAttributeMetadata(attribute);

  }

  /**
   * Manage map set.
   *
   * @param mapping the mapping
   * @param fields the fields
   * @param date the date
   * @throws Exception the exception
   */
  public void manageMapSet(Mapping mapping, String[] fields, Date date)
    throws Exception {

    // If map set already exists, update it if needed
    if (mapSetMap.containsKey(fields[4])) {
      final MapSet mapSet = mapSetMap.get(fields[4]);
      mapping.setMapSet(mapSet);

      // If we have not yet updated this, try now
      if (!mapSetsUpdated.contains(fields[4])) {
        final MapSet mapSet2 = getMapSet(mapSet.getId());
        setCommonFields(mapSet2, date);
        mapSet2.setTerminologyId(fields[4].intern());
        final Concept concept = getConcept(idMap.get(fields[4]));
        mapSet2.setName(concept.getName());
        mapSet2.setObsolete(concept.isObsolete());
        mapSet2.setFromTerminology(getTerminology());
        mapSet2.setFromVersion(getVersion());
        mapSet2.setToTerminology(null);
        mapSet2.setToVersion(null);

        Attribute attribute = null;
        if (mapSet2.getAttributeByName("moduleId") != null) {
          attribute = mapSet2.getAttributeByName("moduleId");
        } else {
          attribute = new AttributeJpa();
          mapSet2.getAttributes().add(attribute);
        }
        setCommonFields(attribute, date);
        attribute.setName("moduleId");
        attribute.setValue(fields[3].intern());
        cacheAttributeMetadata(attribute);

        // Update object and attributes as needed
        if (!Rf2EqualityUtility.equals(mapSet2, mapSet)) {
          if (!mapSet.equals(mapSet2)) {
            Logger.getLogger(getClass())
                .debug("      update map set - " + mapSet2);
            updateMapSet(mapSet2);
          }
          updateAttributes(mapSet2, mapSet);
        }
        mapSetsUpdated.add(fields[4]);
      }
    }

    // Otherwise, add a new one
    else if (!mapSetMap.containsKey(fields[4])) {

      final MapSet mapSet = new MapSetJpa();
      setCommonFields(mapSet, date);
      mapSet.setTerminologyId(fields[4].intern());
      final Concept concept = getConcept(idMap.get(fields[4]));
      mapSet.setName(concept.getName());
      mapSet.setObsolete(concept.isObsolete());
      mapSet.setFromTerminology(getTerminology());
      mapSet.setFromVersion(getVersion());
      mapSet.setToTerminology(null);
      mapSet.setToVersion(null);

      final Attribute attribute = new AttributeJpa();
      setCommonFields(attribute, date);
      attribute.setName("moduleId");
      attribute.setValue(concept.getAttributeByName("moduleId").getValue());
      mapSet.getAttributes().add(attribute);
      addAttribute(attribute, mapping);
      cacheAttributeMetadata(attribute);

      addMapSet(mapSet);
      mapSetMap.put(fields[4], mapSet);
      commitClearBegin();

      mapping.setMapSet(mapSet);

    } else {
      throw new Exception("Unable to determine mapset type.");
    }

  }

  /**
   * Load metadata.
   *
   * @throws Exception the exception
   */
  private void loadMetadata() throws Exception {

    // Term types - each description type
    Map<String, TermType> ttyMap = new HashMap<>();
    for (final TermType tty : getTermTypes(getTerminology(), getVersion())
        .getObjects()) {
      ttyMap.put(tty.getAbbreviation(), tty);
    }
    for (final String tty : termTypes) {
      TermType termType = null;
      if (ttyMap.containsKey(tty)) {
        termType = ttyMap.get(tty);
      } else {
        termType = new TermTypeJpa();
      }
      // Set all fields
      termType.setTerminology(getTerminology());
      termType.setVersion(getVersion());
      termType.setAbbreviation(tty);
      termType.setCodeVariantType(CodeVariantType.SY);
      termType.setExpandedForm(getConcept(idMap.get(tty)).getName());
      termType.setHierarchicalType(false);
      termType.setTimestamp(releaseVersionDate);
      termType.setLastModified(releaseVersionDate);
      termType.setLastModifiedBy(loader);
      termType.setNameVariantType(NameVariantType.UNDEFINED);
      termType.setObsolete(false);
      termType.setSuppressible(false);
      termType.setPublishable(true);
      termType.setPublished(true);
      termType.setUsageType(UsageType.UNDEFINED);
      // Add or udpate
      if (ttyMap.containsKey(tty)) {
        updateTermType(termType);
      } else {
        addTermType(termType);
      }
    }

    // Languages - each language value
    // root language doesn't change
    Map<String, Language> latMap = new HashMap<>();
    for (final Language lat : getLanguages(getTerminology(), getVersion())
        .getObjects()) {
      latMap.put(lat.getAbbreviation(), lat);
    }
    for (final String lat : languages) {
      Language language = null;
      if (latMap.containsKey(lat)) {
        language = latMap.get(lat);
      } else {
        language = new LanguageJpa();
      }
      language.setTerminology(getTerminology());
      language.setVersion(getVersion());
      language.setTimestamp(releaseVersionDate);
      language.setLastModified(releaseVersionDate);
      language.setLastModifiedBy(loader);
      language.setPublishable(true);
      language.setPublished(true);
      language.setExpandedForm(lat);
      language.setAbbreviation(lat);
      language.setISO3Code("???");
      language.setISOCode(lat.substring(0, 2));
      if (latMap.containsKey(lat)) {
        updateLanguage(language);
      } else {
        addLanguage(language);
      }
    }

    // attribute name
    Map<String, AttributeName> atnMap = new HashMap<>();
    for (final AttributeName atn : getAttributeNames(getTerminology(),
        getVersion()).getObjects()) {
      atnMap.put(atn.getAbbreviation(), atn);
    }
    for (final String atn : attributeNames) {
      AttributeName name = null;
      if (atnMap.containsKey(atn)) {
        name = atnMap.get(atn);
      } else {
        name = new AttributeNameJpa();
      }
      name.setTerminology(getTerminology());
      name.setVersion(getVersion());
      name.setLastModified(releaseVersionDate);
      name.setLastModifiedBy(loader);
      name.setPublishable(true);
      name.setPublished(true);
      name.setExpandedForm(atn);
      name.setAbbreviation(atn);
      if (atnMap.containsKey(atn)) {
        updateAttributeName(name);
      } else {
        addAttributeName(name);
      }

    }

    // relationship types - CHD, PAR, and RO
    // These don't change with delta
    // no-op

    // additional relationship types (including grouping type, hierarchical
    // type)

    Map<String, AdditionalRelationshipType> relaMap = new HashMap<>();
    for (final AdditionalRelationshipType rela : getAdditionalRelationshipTypes(
        getTerminology(), getVersion()).getObjects()) {
      relaMap.put(rela.getAbbreviation(), rela);
    }
    AdditionalRelationshipType directSubstance = null;
    AdditionalRelationshipType hasActiveIngredient = null;
    Map<AdditionalRelationshipType, AdditionalRelationshipType> inverses =
        new HashMap<>();
    for (final String rela : additionalRelTypes) {
      AdditionalRelationshipType type = null;
      if (relaMap.containsKey(rela)) {
        type = relaMap.get(rela);
      } else {
        type = new AdditionalRelationshipTypeJpa();
      }
      type.setTerminology(getTerminology());
      type.setVersion(getVersion());
      type.setLastModified(releaseVersionDate);
      type.setLastModifiedBy(loader);
      type.setPublishable(true);
      type.setPublished(true);
      type.setExpandedForm(getConcept(idMap.get(rela)).getName());
      type.setAbbreviation(rela);
      // Hierarchical
      type.setHierarchical(false);
      if (rela.equals(isaTypeRel)) {
        type.setHierarchical(true);
      }
      // $nevergrouped{"123005000"} = "T"; # part-of is never grouped
      // $nevergrouped{"272741003"} = "T"; # laterality is never grouped
      // $nevergrouped{"127489000"} = "T"; # has-active-ingredient is never
      // grouped
      // $nevergrouped{"411116001"} = "T"; # has-dose-form is never grouped
      if (rela.equals("123005000") || rela.equals("272741003")
          || rela.equals("127489000") || rela.equals("411116001")) {
        type.setGroupingType(false);
      } else {
        type.setGroupingType(true);
      }
      if (relaMap.containsKey(rela)) {
        updateAdditionalRelationshipType(type);
      } else {
        addAdditionalRelationshipType(type);
      }
      if (rela.equals("363701004")) {
        hasActiveIngredient = type;
      } else if (rela.equals("127489000")) {
        directSubstance = type;
      }
      AdditionalRelationshipType inverseType = null;
      if (relaMap.containsKey("inverse_" + rela)) {
        inverseType = relaMap.get("inverse_" + rela);
      } else {
        inverseType = new AdditionalRelationshipTypeJpa(type);
        inverseType.setId(null);
      }
      inverseType.setAbbreviation("inverse_" + type.getAbbreviation());
      inverseType.setExpandedForm("inverse_" + type.getAbbreviation());
      inverses.put(type, inverseType);
      if (relaMap.containsKey("inverse_" + rela)) {
        updateAdditionalRelationshipType(inverseType);
      } else {
        addAdditionalRelationshipType(inverseType);
      }
    }
    // handle inverses
    for (final AdditionalRelationshipType type : inverses.keySet()) {
      final AdditionalRelationshipType inverseType = inverses.get(type);
      type.setInverse(inverseType);
      inverseType.setInverse(type);
      updateAdditionalRelationshipType(type);
      updateAdditionalRelationshipType(inverseType);
    }

    // property chains (see Owl)
    // $rightid{"363701004"} = "127489000"; # direct-substance o
    // has-active-ingredient -> direct-substance
    // Add if not already added
    if (this.getPropertyChains(getTerminology(), getVersion()).size() == 0) {
      PropertyChain chain = new PropertyChainJpa();
      chain.setTerminology(getTerminology());
      chain.setVersion(getVersion());
      chain.setLastModified(releaseVersionDate);
      chain.setLastModifiedBy(loader);
      chain.setPublishable(true);
      chain.setPublished(true);
      chain.setAbbreviation(
          "direct-substance o has-active-ingredient -> direct-substance");
      chain.setExpandedForm(chain.getAbbreviation());
      List<AdditionalRelationshipType> list = new ArrayList<>();
      list.add(directSubstance);
      list.add(hasActiveIngredient);
      chain.setChain(list);
      chain.setResult(directSubstance);
      // do this only when the available rels exist
      if (chain.getChain().size() > 0 && chain.getResult() != null) {
        addPropertyChain(chain);
      }
    }

    // semantic types - n/a

    // Root Terminology
    // does not change

    // Terminology
    // does not change

    // Add general metadata entries for all the attribute values
    // that are concept ids.

    // todo : do this for entry and label
    Map<String, GeneralMetadataEntry> entryMap = new HashMap<>();
    for (final GeneralMetadataEntry entry : getGeneralMetadataEntries(
        getTerminology(), getVersion()).getObjects()) {
      entryMap.put(entry.getAbbreviation(), entry);
    }
    for (final String conceptId : generalEntryValues) {
      // Skip if there is no concept for this thing
      if (!idMap.containsKey(conceptId)) {
        logInfo("  Skipping Genral Metadata Entry = " + conceptId);
        continue;
      }
      final String name = getConcept(idMap.get(conceptId)).getName();
      logInfo("  Genral Metadata Entry = " + conceptId + ", " + name);
      GeneralMetadataEntry entry = null;
      if (entryMap.containsKey(conceptId)) {
        entry = entryMap.get(conceptId);
      } else {
        entry = new GeneralMetadataEntryJpa();
      }
      entry.setTerminology(getTerminology());
      entry.setVersion(getVersion());
      entry.setLastModified(releaseVersionDate);
      entry.setLastModifiedBy(loader);
      entry.setPublishable(true);
      entry.setPublished(true);
      entry.setAbbreviation(conceptId);
      entry.setExpandedForm(name);
      entry.setKey("concept_metadata");
      entry.setType("concept_name");
      if (entryMap.containsKey(conceptId)) {
        updateGeneralMetadataEntry(entry);
      } else {
        addGeneralMetadataEntry(entry);
      }
    }

    // General metadata entries for label values do not change
    commitClearBegin();

  }

  /**
   * Sets the common fields.
   *
   * @param component the common fields
   * @param timestamp the timestamp
   */
  private void setCommonFields(Component component, Date timestamp) {
    component.setTimestamp(timestamp);
    component.setLastModified(releaseVersionDate);
    component.setLastModifiedBy(loader);
    component.setObsolete(false);
    component.setPublishable(true);
    component.setPublished(true);
    component.setSuppressible(false);

    component.setTerminologyId("");
    component.setTerminology(getTerminology());
    component.setVersion(getVersion());

  }

  /**
   * Cache attribute value.
   *
   * @param attribute the attribute
   */
  private void cacheAttributeMetadata(Attribute attribute) {
    attributeNames.add(attribute.getName());
    if (attribute.getValue().matches("^\\d[\\d]{6,}$")) {
      generalEntryValues.add(attribute.getValue());
    }
  }

  /* see superclass */
  @Override
  public void close() throws Exception {
    super.close();
    readers = null;
    idMap = null;
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {
    return new ValidationResultJpa();
  }

  /**
   * Sets the readers.
   *
   * @param readers the readers
   */
  public void setReaders(Rf2Readers readers) {
    this.readers = readers;
  }

  /* see superclass */
  @Override
  public void checkProperties(Properties p) throws Exception {

    checkRequiredProperties(new String[] {
        "inputDir"
    }, p);
  }

  @Override
  public void setProperties(Properties p) throws Exception {

    if (p.getProperty("inputDir") != null) {
      setInputPath(p.getProperty("inputDir"));
    }

  }

  /* see superclass */
  @Override
  public List<AlgorithmParameter> getParameters() {
    final List<AlgorithmParameter> params = super.getParameters();
    AlgorithmParameter param = new AlgorithmParameterJpa("Input Dir",
        "inputDir", "Input RF2 Delta directory to load", "", 255,
        AlgorithmParameter.Type.DIRECTORY, "");
    params.add(param);
    return params;

  }
}
