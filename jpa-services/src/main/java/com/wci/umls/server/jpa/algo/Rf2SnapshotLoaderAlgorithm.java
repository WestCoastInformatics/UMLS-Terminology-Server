/*
 *    Copyright 2015 West Coast Informatics, LLC
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

import org.apache.log4j.Logger;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;

import com.wci.umls.server.AlgorithmParameter;
import com.wci.umls.server.ReleaseInfo;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.CancelException;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.FieldedStringTokenizer;
import com.wci.umls.server.jpa.AlgorithmParameterJpa;
import com.wci.umls.server.jpa.ReleaseInfoJpa;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.Rf2Readers.Keys;
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
import com.wci.umls.server.jpa.meta.RelationshipTypeJpa;
import com.wci.umls.server.jpa.meta.RootTerminologyJpa;
import com.wci.umls.server.jpa.meta.TermTypeJpa;
import com.wci.umls.server.jpa.meta.TerminologyJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.AtomSubset;
import com.wci.umls.server.model.content.AtomSubsetMember;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.Component;
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
import com.wci.umls.server.model.meta.RelationshipType;
import com.wci.umls.server.model.meta.RootTerminology;
import com.wci.umls.server.model.meta.TermType;
import com.wci.umls.server.model.meta.Terminology;
import com.wci.umls.server.model.meta.UsageType;
import com.wci.umls.server.model.workflow.WorkflowStatus;
import com.wci.umls.server.services.RootService;
import com.wci.umls.server.services.helpers.PushBackReader;

/**
 * Implementation of an algorithm to import RF2 snapshot data.
 */
public class Rf2SnapshotLoaderAlgorithm
    extends AbstractTerminologyLoaderAlgorithm {

  /** The isa type rel. */
  private final static String isaTypeRel = "116680003";

  /** The root concept id. */
  private final static String rootConceptId = "138875005";

  /** The Constant coreModuleId. */
  private final static String coreModuleId = "900000000000207008";

  /** The Constant metadataModuleId. */
  private final static String metadataModuleId = "900000000000012004";

  /** The dpn ref set id. */
  private Set<String> dpnRefSetIds = new HashSet<>();

  {
    // US English Language
    dpnRefSetIds.add("900000000000509007");
    // VET extension
    dpnRefSetIds.add("332501000009101");
  }

  /** The dpn acceptability id. */
  private String dpnAcceptabilityId = "900000000000548007";

  /** The dpn type id. */
  private String dpnTypeId = "900000000000013009";

  /** The preferred atoms set. */
  private Set<String> prefAtoms = new HashSet<>();

  /** The release version date. */
  private Date releaseVersionDate = null;

  /** The extension (and optional namespace) information */
  private String extensionInfo = null;

  /** The readers. */
  private Rf2Readers readers;

  /** The definition map. */
  private Map<String, Set<Long>> definitionMap = new HashMap<>();

  /** The atom id map. */
  private Map<String, Long> atomIdMap = new HashMap<>();

  /** The module ids. */
  private Set<String> moduleIds = new HashSet<>();

  /** non-core modules map. */
  private Map<String, Set<String>> moduleConceptIdMap = new HashMap<>();

  /** The concept id map. */
  private Map<String, Long> conceptIdMap = new HashMap<>();

  /** The atom subset map. */
  private Map<String, AtomSubset> atomSubsetMap = new HashMap<>();

  /** The concept subset map. */
  private Map<String, ConceptSubset> conceptSubsetMap = new HashMap<>();

  /** The concept mapset map. */
  private Map<String, MapSet> mapSetMap = new HashMap<>();

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

  /** counter for objects created, reset in each load section. */
  private int objectCt; //

  /** The init pref name. */
  private final String initPrefName = "No default preferred name found";

  /** The loader. */
  private final String loader = "loader";

  /** The id. */
  private final String id = "id";

  /**
   * Instantiates an empty {@link Rf2SnapshotLoaderAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public Rf2SnapshotLoaderAlgorithm() throws Exception {
    super();
  }

  /* see superclass */
  @Override
  public void compute() throws Exception {

    logInfo("Start loading snapshot");
    logInfo("  terminology = " + getTerminology());
    logInfo("  version = " + getVersion());
    logInfo("  inputDir = " + getInputPath());
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
      throw new Exception("Terminology getVersion() must be specified");
    }
    if (getInputPath() == null) {
      throw new Exception("Input directory must be specified");
    }
    // Check the input directory
    File inputFile = new File(getInputPath());
    if (!inputFile.exists()) {
      throw new Exception("Specified input directory does not exist");
    }

    try {

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

      // Get release version if not set externally
      if (getReleaseVersion() == null) {
        setReleaseVersion(getFileVersion());
      }

      // get extension information if not set externally
      try {
        extensionInfo = getFileExtensionInfo();
        Logger.getLogger(getClass()).info("  extensionInfo = " + extensionInfo);
      } catch (Exception e) {
        Logger.getLogger(getClass()).warn(
            "Could not retrieve extension and namespace information from file structure");
      }

      releaseVersionDate = ConfigUtility.DATE_FORMAT.parse(getReleaseVersion());
      Logger.getLogger(getClass())
          .info("  releaseVersion = " + getReleaseVersion());

      // Sort files if indicated (otherwise sorted externally, e.g. by "full"
      // loader)
      if (isSortFiles()) {

        // prepare the sorting algorithm
        Rf2FileSorter sorter = new Rf2FileSorter();
        sorter.setInputDir(getInputPath());
        sorter.setOutputDir(getInputPath() + "/RF2-sorted-temp/");
        sorter.setSortByEffectiveTime(false);
        sorter.setRequireAllFiles(false);
        Logger.getLogger(getClass()).info("  Sort RF2 Files");
        Logger.getLogger(getClass()).info("    sort by effective time: false");
        Logger.getLogger(getClass()).info("    require all files     : false");
        sorter.compute();

      } else if (!new File(getInputPath() + "/RF2-sorted-temp/").exists()) {
        throw new Exception(
            "No sort specified, but previously sorted files do not exist.");
      }

      // Open readers if not opened externally
      boolean leaveReadersOpen = readers != null;
      if (!leaveReadersOpen) {
        readers =
            new Rf2Readers(new File(getInputPath() + "/RF2-sorted-temp/"));
        readers.openReaders();
      }

      //
      // Load concepts
      //
      logInfo("  Loading Concepts...");
      loadConcepts();

      //
      // Load descriptions and language refsets
      //
      logInfo("  Loading Atoms...");
      loadAtoms();
      loadDefinitions();

      logInfo("  Loading Language Ref Sets...");
      loadLanguageRefSetMembers();

      logInfo("  Connecting atoms/concepts and computing preferred names...");
      connectAtomsAndConcepts();

      //
      // Load relationships
      //
      logInfo("  Loading Relationships...");
      loadRelationships();

      //
      // load AssocationReference RefSets (Content)
      //
      logInfo("  Loading Association Reference Ref Sets...");
      loadAssociationReferenceRefSets();
      commitClearBegin();

      //
      // Load AttributeValue RefSets (Content)
      //
      Logger.getLogger(getClass())
          .info("  Loading Attribute Value Ref Sets...");
      loadAttributeValueRefSets();
      commitClearBegin();

      //
      // Load Simple RefSets (Content)
      //
      logInfo("  Loading Simple Ref Sets...");
      loadSimpleRefSets();

      //
      // Load SimpleMapRefSets
      //
      logInfo("  Loading Simple Map Ref Sets...");
      loadSimpleMapRefSets();

      commitClearBegin();

      //
      // Load ComplexMapRefSets
      //
      logInfo("  Loading Complex and Extended Map Ref Sets...");
      loadAllMapRefSets();

      commitClearBegin();

      // load RefsetDescriptor RefSets (Content)
      //
      logInfo("  Loading Refset Descriptor Ref Sets...");
      loadRefsetDescriptorRefSets();

      //
      // load ModuleDependency RefSets (Content)
      //
      logInfo("  Loading Module Dependency Ref Sets...");

      loadModuleDependencyRefSets();

      //
      // load AtomType RefSets (Content)
      //
      logInfo("  Loading Description Type Ref Sets...");
      loadDescriptionTypeRefSets();

      // Load metadata
      loadMetadata();

      // Make subsets and label sets
      loadExtensionLabelSets();

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
        info.setReleaseBeginDate(releaseVersionDate);
        info.setReleaseFinishDate(releaseVersionDate);
        info.setTerminology(getTerminology());
        info.setVersion(getVersion());
        info.setLastModified(releaseVersionDate);
        info.setLastModifiedBy(loader);
        info.setTimestamp(new Date());
        addReleaseInfo(info);
      }

      // Clear concept cache
      // clear and commit
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
      logInfo("Loading objects done.");

      commit();

    } catch (CancelException e) {
      Logger.getLogger(getClass()).info("Cancel request detected");
      throw new CancelException("Compute cancelled");

    } catch (Exception e) {
      throw e;
    }
  }

  /* see superclass */
  @Override
  public void reset() throws Exception {
    // do nothing
  }

  /**
   * Sets the readers.
   *
   * @param readers the readers
   */
  public void setReaders(Rf2Readers readers) {
    this.readers = readers;
    readers.getReader(Keys.ASSOCIATION_REFERENCE);
  }

  /* see superclass */
  @Override
  public String getFileVersion() throws Exception {
    Rf2FileSorter sorter = new Rf2FileSorter();
    sorter.setInputDir(getInputPath());
    return sorter.getFileVersion();
  }

  /**
   * Gets the RF2 extension (and optional namespace information)
   * @return the extension & namespace string
   * @throws Exception
   */
  public String getFileExtensionInfo() throws Exception {
    Rf2FileSorter sorter = new Rf2FileSorter();
    sorter.setInputDir(getInputPath());
    return sorter.getFileExtensionInfo();
  }

  /**
   * Load concepts.
   * 
   * @throws Exception the exception
   */
  private void loadConcepts() throws Exception {

    String line = "";
    objectCt = 0;

    PushBackReader reader = readers.getReader(Rf2Readers.Keys.CONCEPT);
    while ((line = reader.readLine()) != null) {

      final String fields[] = FieldedStringTokenizer.split(line, "\t");
      final Concept concept = new ConceptJpa();

      if (!fields[0].equals(id)) { // header

        // Stop if the effective time is past the release getVersion()
        if (fields[1].compareTo(getReleaseVersion()) > 0) {
          reader.push(line);
          break;
        }

        final Date date = ConfigUtility.DATE_FORMAT.parse(fields[1]);
        concept.setTerminologyId(fields[0]);
        concept.setTimestamp(date);
        concept.setObsolete(fields[2].equals("0"));
        concept.setSuppressible(concept.isObsolete());
        concept.setFullyDefined(fields[4].equals("900000000000073002"));
        concept.setTerminology(getTerminology());
        concept.setVersion(getVersion());
        concept.setName(initPrefName);
        concept.setLastModified(date);
        concept.setLastModifiedBy(loader);
        concept.setPublished(true);
        concept.setPublishable(true);
        concept.setWorkflowStatus(WorkflowStatus.PUBLISHED);
        concept.setUsesRelationshipUnion(true);

        // Attributes
        final Attribute attribute = new AttributeJpa();
        setCommonFields(attribute, date);
        attribute.setName("moduleId");
        attribute.setValue(fields[3].intern());
        cacheAttributeMetadata(attribute);
        concept.getAttributes().add(attribute);
        addAttribute(attribute, concept);

        final Attribute attribute2 = new AttributeJpa();
        setCommonFields(attribute2, date);
        attribute2.setName("definitionStatusId");
        attribute2.setValue(fields[4].intern());
        cacheAttributeMetadata(attribute2);
        concept.getAttributes().add(attribute2);
        addAttribute(attribute2, concept);

        // copy concept to shed any hibernate stuff
        addConcept(concept);
        conceptIdMap.put(concept.getTerminologyId(), concept.getId());

        // Save extension module info
        if (isExtensionModule(fields[3])) {
          moduleIds.add(fields[3]);
          if (!moduleConceptIdMap.containsKey(fields[3])) {
            moduleConceptIdMap.put(fields[3], new HashSet<String>());
          }
          moduleConceptIdMap.get(fields[3]).add(concept.getTerminologyId());
        }

        logAndCommit(++objectCt, RootService.logCt, RootService.commitCt);
      }
    }
    commitClearBegin();
  }

  /**
   * Load relationships.
   * 
   * @throws Exception the exception
   */
  private void loadRelationships() throws Exception {

    String line = "";
    objectCt = 0;

    PushBackReader reader = readers.getReader(Rf2Readers.Keys.RELATIONSHIP);
    // Iterate over relationships
    while ((line = reader.readLine()) != null) {

      // Split line
      final String fields[] = FieldedStringTokenizer.split(line, "\t");
      // Skip header
      if (!fields[0].equals(id)) {

        // Stop if the effective time is past the release getVersion()
        if (fields[1].compareTo(getReleaseVersion()) > 0) {
          reader.push(line);
          break;
        }

        // Configure relationship
        final ConceptRelationship relationship = new ConceptRelationshipJpa();
        final Date date = ConfigUtility.DATE_FORMAT.parse(fields[1]);
        relationship.setTerminologyId(fields[0]);
        relationship.setTimestamp(date);
        relationship.setLastModified(date);
        relationship.setObsolete(fields[2].equals("0")); // active
        relationship.setSuppressible(relationship.isObsolete());
        relationship.setGroup(fields[6].intern()); // relationshipGroup
        relationship.setRelationshipType(
            fields[7].equals(isaTypeRel) ? "Is a" : "other"); // typeId
        relationship.setAdditionalRelationshipType(fields[7]); // typeId
        relationship
            .setHierarchical(relationship.getRelationshipType().equals("Is a"));
        generalEntryValues.add(relationship.getAdditionalRelationshipType());
        additionalRelTypes.add(relationship.getAdditionalRelationshipType());
        relationship.setStated(fields[8].equals("900000000000010007"));
        relationship.setInferred(fields[8].equals("900000000000011006"));
        relationship.setTerminology(getTerminology());
        relationship.setVersion(getVersion());
        relationship.setLastModified(releaseVersionDate);
        relationship.setLastModifiedBy(loader);
        relationship.setPublished(true);
        relationship.setPublishable(true);
        relationship.setWorkflowStatus(WorkflowStatus.PUBLISHED);
        relationship.setAssertedDirection(true);

        // Attributes
        Attribute attribute = new AttributeJpa();
        setCommonFields(attribute, date);
        attribute.setName("moduleId");
        attribute.setValue(fields[3].intern());
        relationship.getAttributes().add(attribute);
        addAttribute(attribute, relationship);

        Attribute attribute2 = new AttributeJpa();
        setCommonFields(attribute2, date);
        attribute2.setName("characteristicTypeId");
        attribute2.setValue(fields[8].intern());
        cacheAttributeMetadata(attribute2);
        relationship.getAttributes().add(attribute2);
        addAttribute(attribute2, relationship);

        Attribute attribute3 = new AttributeJpa();
        setCommonFields(attribute3, date);
        attribute3.setName("modifierId");
        attribute3.setValue(fields[9].intern());
        cacheAttributeMetadata(attribute3);
        relationship.getAttributes().add(attribute3);
        addAttribute(attribute3, relationship);

        // get concepts from cache, they just need to have ids
        final Concept fromConcept = getConcept(conceptIdMap.get(fields[4]));
        final Concept toConcept = getConcept(conceptIdMap.get(fields[5]));
        if (fromConcept != null && toConcept != null) {
          relationship.setFrom(fromConcept);
          relationship.setTo(toConcept);
          // unnecessary
          // sourceConcept.addRelationship(relationship);
          addRelationship(relationship);

        } else {
          if (fromConcept == null) {
            throw new Exception(
                "Relationship " + relationship.getTerminologyId()
                    + " -existent source concept " + fields[4]);
          }
          if (toConcept == null) {
            throw new Exception("Relationship "
                + relationship.getTerminologyId()
                + " references non-existent destination concept " + fields[5]);
          }
        }

        logAndCommit(++objectCt, RootService.logCt, RootService.commitCt);

      }
    }

    // Final commit
    commitClearBegin();

  }

  /**
   * Load descriptions.
   * 
   * @throws Exception the exception
   */
  private void loadAtoms() throws Exception {
    String line = "";
    objectCt = 0;
    PushBackReader reader = readers.getReader(Rf2Readers.Keys.DESCRIPTION);
    while ((line = reader.readLine()) != null) {

      final String fields[] = FieldedStringTokenizer.split(line, "\t");
      if (!fields[0].equals(id)) {
        // Stop if the effective time is past the release getVersion()
        if (fields[1].compareTo(getReleaseVersion()) > 0) {
          reader.push(line);
          break;
        }

        final Atom atom = new AtomJpa();
        final Date date = ConfigUtility.DATE_FORMAT.parse(fields[1]);
        atom.setTerminologyId(fields[0]);
        atom.setTimestamp(date);
        atom.setLastModified(date);
        atom.setLastModifiedBy(loader);
        atom.setObsolete(fields[2].equals("0"));
        atom.setSuppressible(atom.isObsolete());
        atom.setConceptId(fields[4]);
        atom.setDescriptorId("");
        atom.setCodeId("");
        atom.setLexicalClassId("");
        atom.setStringClassId("");
        atom.setLanguage(fields[5].intern());
        languages.add(atom.getLanguage());
        atom.setTermType(fields[6].intern());
        generalEntryValues.add(atom.getTermType());
        termTypes.add(atom.getTermType());
        atom.setName(fields[7]);
        atom.setTerminology(getTerminology());
        atom.setVersion(getVersion());
        atom.setPublished(true);
        atom.setPublishable(true);
        atom.setWorkflowStatus(WorkflowStatus.PUBLISHED);

        // Attributes
        final Attribute attribute = new AttributeJpa();
        setCommonFields(attribute, date);
        attribute.setName("moduleId");
        attribute.setValue(fields[3].intern());
        atom.getAttributes().add(attribute);
        addAttribute(attribute, atom);

        final Attribute attribute2 = new AttributeJpa();
        setCommonFields(attribute2, date);
        attribute2.setName("caseSignificanceId");
        attribute2.setValue(fields[8].intern());
        cacheAttributeMetadata(attribute2);
        atom.getAttributes().add(attribute2);
        addAttribute(attribute2, atom);

        // set concept from cache and set initial prev concept
        final Long conceptId = conceptIdMap.get(fields[4]);
        if (conceptId == null) {
          throw new Exception(
              "Descriptions file references nonexistent concept: " + fields[4]);
        }
        final Concept concept = getConcept(conceptIdMap.get(fields[4]));

        if (concept != null) {
          // this also adds language refset entries
          addAtom(atom);
          atomIdMap.put(atom.getTerminologyId(), atom.getId());
        } else {
          throw new Exception("Atom " + atom.getTerminologyId()
              + " references non-existent concept " + fields[4]);
        }

        logAndCommit(++objectCt, RootService.logCt, RootService.commitCt);
      }
    }
    commitClearBegin();
  }

  /**
   * Load definitions. Treat exactly like descriptions.
   * 
   * @throws Exception the exception
   */
  private void loadDefinitions() throws Exception {
    String line = "";
    objectCt = 0;
    PushBackReader reader = readers.getReader(Rf2Readers.Keys.DEFINITION);
    while ((line = reader.readLine()) != null) {

      final String fields[] = FieldedStringTokenizer.split(line, "\t");
      if (!fields[0].equals(id)) {

        // Stop if the effective time is past the release getVersion()
        if (fields[1].compareTo(getReleaseVersion()) > 0) {
          reader.push(line);
          break;
        }

        final Atom def = new AtomJpa();
        final Date date = ConfigUtility.DATE_FORMAT.parse(fields[1]);
        def.setTerminologyId(fields[0]);
        def.setTimestamp(date);
        def.setLastModified(date);
        def.setLastModifiedBy(loader);
        def.setObsolete(fields[2].equals("0"));
        def.setSuppressible(def.isObsolete());
        def.setConceptId(fields[4]);
        def.setDescriptorId("");
        def.setCodeId("");
        def.setLexicalClassId("");
        def.setStringClassId("");
        def.setLanguage(fields[5].intern());
        languages.add(def.getLanguage());
        def.setTermType(fields[6].intern());
        generalEntryValues.add(def.getTermType());
        termTypes.add(def.getTermType());
        def.setName(fields[7]);
        def.setTerminology(getTerminology());
        def.setVersion(getVersion());
        def.setPublished(true);
        def.setPublishable(true);
        def.setWorkflowStatus(WorkflowStatus.PUBLISHED);

        // Attributes
        final Attribute attribute = new AttributeJpa();
        setCommonFields(attribute, date);
        attribute.setName("moduleId");
        attribute.setValue(fields[3].intern());
        def.getAttributes().add(attribute);
        addAttribute(attribute, def);

        final Attribute attribute2 = new AttributeJpa();
        setCommonFields(attribute2, date);
        attribute2.setName("caseSignificanceId");
        attribute2.setValue(fields[8].intern());
        cacheAttributeMetadata(attribute2);
        def.getAttributes().add(attribute2);
        addAttribute(attribute2, def);

        // set concept from cache and set initial prev concept
        final Concept concept = getConcept(conceptIdMap.get(fields[4]));

        if (concept != null) {
          // this also adds language refset entries
          addAtom(def);
          atomIdMap.put(def.getTerminologyId(), def.getId());
        } else {
          throw new Exception("Atom " + def.getTerminologyId()
              + " references non-existent concept " + fields[4]);
        }

        logAndCommit(++objectCt, RootService.logCt, RootService.commitCt);
      }
    }
    commitClearBegin();
  }

  /**
   * Connect atoms and concepts.
   *
   * @throws Exception the exception
   */
  private void connectAtomsAndConcepts() throws Exception {

    // Connect concepts and atoms and compute preferred names
    logInfo("  Connect atoms and concepts");
    objectCt = 0;
    // NOTE: Hibernate-specific to support iterating
    Session session = manager.unwrap(Session.class);
    org.hibernate.Query hQuery = session
        .createQuery("select a from AtomJpa a " + "where conceptId is not null "
            + "and conceptId != '' and terminology = :terminology "
            + "and version = :version " + "order by terminology, conceptId")
        .setParameter("terminology", getTerminology())
        .setParameter("version", getVersion()).setReadOnly(true)
        .setFetchSize(2000).setCacheable(true);
    ScrollableResults results = hQuery.scroll(ScrollMode.FORWARD_ONLY);
    String prevCui = null;
    String prefName = null;
    String altPrefName = null;
    Concept concept = null;
    while (results.next()) {
      final Atom atom = (Atom) results.get()[0];
      if (atom.getConceptId() == null || atom.getConceptId().isEmpty()) {
        continue;
      }
      if (prevCui == null || !prevCui.equals(atom.getConceptId())) {
        if (concept != null) {
          // compute preferred name
          if (prefName == null) {
            prefName = altPrefName;
            logInfo("Unable to determine preferred name for "
                + concept.getTerminologyId());
            if (altPrefName == null) {
              throw new Exception(
                  "Unable to determine preferred name (or alt pref name) for "
                      + concept.getTerminologyId());
            }
          }

          concept.setName(prefName);
          prefName = null;

          // Add definitions
          if (definitionMap.containsKey(concept.getTerminologyId())) {
            for (final Long id : definitionMap
                .get(concept.getTerminologyId())) {
              concept.getDefinitions().add(getDefinition(id));
            }
          }

          updateConcept(concept);

          // Set atom subset names
          if (atomSubsetMap.containsKey(concept.getTerminologyId())) {
            AtomSubset subset = atomSubsetMap.get(concept.getTerminologyId());
            subset.setName(concept.getName());
            subset.setDescription(concept.getName());
            updateSubset(subset);
          }

          logAndCommit(++objectCt, RootService.logCt, RootService.commitCt);
        }
        concept = getConcept(conceptIdMap.get(atom.getConceptId()));
      }
      concept.getAtoms().add(atom);
      // Active atoms with pref typeId that are preferred from language refset
      // perspective is the pref name. This bypasses the pref name computer
      // but is way faster.
      if (!atom.isObsolete() && atom.getTermType().equals(dpnTypeId)
          && prefAtoms.contains(atom.getTerminologyId())) {
        prefName = atom.getName();
      }
      // Pick an alternative preferred name in case a true pref can't be found
      // this at least lets us choose something to move on.
      if (prefName == null && altPrefName == null) {
        if (!atom.isObsolete() && atom.getTermType().equals(dpnTypeId)) {
          altPrefName = atom.getName();
        }
      }
      // If pref name is null, pick the first non-obsolete atom with the correct
      // term type
      // this guarantees that SOMETHING is picked
      prevCui = atom.getConceptId();
    }
    if (concept != null) {
      if (prefName == null) {
        throw new Exception("Unable to determine preferred name for "
            + concept.getTerminologyId());
      }
      concept.setName(prefName);
      updateConcept(concept);
      commitClearBegin();
    }
    results.close();
    commitClearBegin();

  }

  /**
   * Load and cache all language refset members.
   * @throws Exception the exception
   */
  private void loadLanguageRefSetMembers() throws Exception {

    objectCt = 0;
    PushBackReader reader = readers.getReader(Rf2Readers.Keys.LANGUAGE);
    String line;
    while ((line = reader.readLine()) != null) {

      final String fields[] = FieldedStringTokenizer.split(line, "\t");

      if (!fields[0].equals(id)) { // header

        // Stop if the effective time is past the release getVersion()
        if (fields[1].compareTo(getReleaseVersion()) > 0) {
          reader.push(line);
          return;
        }
        final AtomSubsetMember member = new AtomSubsetMemberJpa();
        final Date date = ConfigUtility.DATE_FORMAT.parse(fields[1]);
        refsetHelper(member, fields);

        // Attributes
        final Attribute attribute = new AttributeJpa();
        setCommonFields(attribute, date);
        attribute.setName("acceptabilityId");
        attribute.setValue(fields[6].intern());
        cacheAttributeMetadata(attribute);
        member.getAttributes().add(attribute);
        addAttribute(attribute, member);

        addSubsetMember(member);

        // Save preferred atom id info
        if (!member.isObsolete() && dpnAcceptabilityId.equals(fields[6])
            && dpnRefSetIds.contains(fields[4])) {
          prefAtoms.add(member.getMember().getTerminologyId());
        }

        logAndCommit(++objectCt, RootService.logCt, RootService.commitCt);

      }
    }

    commitClearBegin();
  }

  /**
   * Load AttributeRefSets (Content).
   * 
   * @throws Exception the exception
   */
  private void loadAttributeValueRefSets() throws Exception {

    String line = "";
    objectCt = 0;

    // Iterate through attribute value entries
    PushBackReader reader = readers.getReader(Rf2Readers.Keys.ATTRIBUTE_VALUE);
    while ((line = reader.readLine()) != null) {
      line = line.replace("\r", "");
      final String fields[] = FieldedStringTokenizer.split(line, "\t");

      if (!fields[0].equals(id)) { // header

        // Stop if the effective time is past the release getVersion()
        if (fields[1].compareTo(getReleaseVersion()) > 0) {
          reader.push(line);
          break;
        }

        final Date date = ConfigUtility.DATE_FORMAT.parse(fields[1]);
        SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset> member =
            null;
        if (conceptIdMap.get(fields[5]) != null) {
          member = new ConceptSubsetMemberJpa();
        } else if (atomIdMap.get(fields[5]) != null) {
          member = new AtomSubsetMemberJpa();
        } else {
          throw new Exception(
              "Attribute value member connected to nonexistent object");
        }
        refsetHelper(member, fields);

        // Attributes
        final Attribute attribute = new AttributeJpa();
        setCommonFields(attribute, date);
        attribute.setName("valueId");
        attribute.setValue(fields[6].intern());
        attributeNames.add(attribute.getName());
        cacheAttributeMetadata(attribute);
        member.getAttributes().add(attribute);
        addAttribute(attribute, member);

        // Add member
        addSubsetMember(member);

        logAndCommit(++objectCt, RootService.logCt, RootService.commitCt);

      }
    }

  }

  /**
   * Load Association Reference Refset (Content).
   * 
   * @throws Exception the exception
   */
  private void loadAssociationReferenceRefSets() throws Exception {

    String line = "";
    objectCt = 0;

    // Iterate through attribute value entries
    PushBackReader reader =
        readers.getReader(Rf2Readers.Keys.ASSOCIATION_REFERENCE);
    while ((line = reader.readLine()) != null) {

      line = line.replace("\r", "");
      final String fields[] = FieldedStringTokenizer.split(line, "\t");

      if (!fields[0].equals(id)) { // header

        // Stop if the effective time is past the release getVersion()
        if (fields[1].compareTo(getReleaseVersion()) > 0) {
          Logger.getLogger(getClass()).debug(
              "Found effective time past release getVersion() at line " + line);
          reader.push(line);
          break;
        }

        if (conceptIdMap.get(fields[4]) == null) {

          Logger.getLogger(getClass()).warn(
              "Association reference member connected to nonexistent refset with terminology id "
                  + fields[4]);

          logWarn("  Line: " + line);
          continue;
          /*
           * throw new Exception(
           * "Association reference member connected to nonexistent object");
           */
        }

        if (conceptIdMap.get(fields[5]) == null) {
          Logger.getLogger(getClass()).warn(
              "Association reference member connected to nonexistent source object with terminology id "
                  + fields[5]);

          logWarn("  Line: " + line);
          continue;
          /*
           * throw new Exception(
           * "Association reference member connected to nonexistent object");
           */
        }

        if (conceptIdMap.get(fields[6]) == null) {
          Logger.getLogger(getClass()).warn(
              "Association reference member connected to nonexistent target object with terminology id "
                  + fields[5]);
          logWarn("  Line: " + line);
          continue;
          /*
           * throw new Exception(
           * "Association reference member connected to nonexistent object");
           */
        }

        ConceptRelationship relationship = new ConceptRelationshipJpa();
        final Date date = ConfigUtility.DATE_FORMAT.parse(fields[1]);

        // set the fields
        // id effectiveTime active moduleId refsetId referencedComponentId
        // targetComponentId
        relationship.setTerminologyId(fields[0]);
        relationship.setTimestamp(date);
        relationship.setLastModified(date);
        relationship.setObsolete(fields[2].equals("0")); // active
        relationship.setSuppressible(relationship.isObsolete());
        relationship.setRelationshipType("other");
        relationship.setHierarchical(false);
        relationship.setAdditionalRelationshipType(fields[4]);
        relationship.setStated(false);
        relationship.setInferred(true);
        relationship.setTerminology(getTerminology());
        relationship.setVersion(getVersion());
        relationship.setLastModified(releaseVersionDate);
        relationship.setLastModifiedBy(loader);
        relationship.setPublished(true);
        relationship.setPublishable(true);
        relationship.setWorkflowStatus(WorkflowStatus.PUBLISHED);
        relationship.setAssertedDirection(true);

        // ensure additional relationship type has been added
        additionalRelTypes.add(relationship.getAdditionalRelationshipType());
        generalEntryValues.add(relationship.getAdditionalRelationshipType());

        // Module Id Attribute
        Attribute attribute = new AttributeJpa();
        setCommonFields(attribute, date);
        attribute.setName("moduleId");
        attribute.setValue(fields[3].intern());
        relationship.getAttributes().add(attribute);
        addAttribute(attribute, relationship);

        // get concepts from cache, they just need to have ids
        final Concept fromConcept = getConcept(conceptIdMap.get(fields[5]));
        final Concept toConcept = getConcept(conceptIdMap.get(fields[6]));

        if (fromConcept != null && toConcept != null) {
          relationship.setFrom(fromConcept);
          relationship.setTo(toConcept);
          addRelationship(relationship);

          Logger.getLogger(getClass())
              .debug("adding RO rel " + (objectCt + 1) + ", "
                  + relationship.getTerminologyId() + ", "
                  + relationship.getFrom().getName() + ", "
                  + getConcept(conceptIdMap
                      .get(relationship.getAdditionalRelationshipType()))
                  + ", " + relationship.getTo().getName());

        } else {
          if (fromConcept == null) {
            throw new Exception(
                "Relationship " + relationship.getTerminologyId()
                    + " references non-existent source concept " + fields[5]);
          }
          if (toConcept == null) {
            throw new Exception("Relationship" + relationship.getTerminologyId()
                + " references non-existent destination concept " + fields[6]);
          }
        }

        logAndCommit(++objectCt, RootService.logCt, RootService.commitCt);

      }
    }
  }

  /**
   * Load SimpleRefSets (Content).
   * 
   * @throws Exception the exception
   */
  private void loadSimpleRefSets() throws Exception {

    String line = "";
    objectCt = 0;

    PushBackReader reader = readers.getReader(Rf2Readers.Keys.SIMPLE);
    while ((line = reader.readLine()) != null) {

      line = line.replace("\r", "");
      final String fields[] = FieldedStringTokenizer.split(line, "\t");

      if (!fields[0].equals(id)) { // header

        // Stop if the effective time is past the release getVersion()
        if (fields[1].compareTo(getReleaseVersion()) > 0) {
          reader.push(line);
          break;
        }

        ConceptSubsetMember member = new ConceptSubsetMemberJpa();
        refsetHelper(member, fields);

        // Add member
        addSubsetMember(member);

        logAndCommit(++objectCt, RootService.logCt, RootService.commitCt);

      }
    }
  }

  /**
   * Load SimpleMapRefSets (Crossmap).
   * 
   * @throws Exception the exception
   */
  private void loadSimpleMapRefSets() throws Exception {

    String line = "";
    objectCt = 0;

    PushBackReader reader = readers.getReader(Rf2Readers.Keys.SIMPLE_MAP);
    while ((line = reader.readLine()) != null) {

      line = line.replace("\r", "");
      final String fields[] = FieldedStringTokenizer.split(line, "\t");
      if (!fields[0].equals(id)) { // header

        // Stop if the effective time is past the release getVersion()
        if (fields[1].compareTo(getReleaseVersion()) > 0) {
          reader.push(line);
          break;
        }

        final Date date = ConfigUtility.DATE_FORMAT.parse(fields[1]);
        ConceptSubsetMember member = new ConceptSubsetMemberJpa();
        refsetHelper(member, fields);

        // Attributes
        Attribute attribute = new AttributeJpa();
        setCommonFields(attribute, date);
        attribute.setName("mapTarget");
        attribute.setValue(fields[6].intern());
        attributeNames.add(attribute.getName());
        member.getAttributes().add(attribute);
        addAttribute(attribute, member);

        // Add member
        addSubsetMember(member);

        logAndCommit(++objectCt, RootService.logCt, RootService.commitCt);

      }
    }
  }

  /**
   * Load complex/extended map refset entries.
   * 
   * @throws Exception the exception
   */
  private void loadAllMapRefSets() throws Exception {
    String line = "";
    objectCt = 0;

    // PushBackReader reader = readers.getReader(Rf2Readers.Keys.COMPLEX_MAP);
    // // Iterate over mappings
    // while ((line = reader.readLine()) != null) {
    //
    // // Split line
    // final String fields[] = FieldedStringTokenizer.split(line, "\t");
    // // Skip header
    // if (!fields[0].equals(id)) {
    //
    // // Stop if the effective time is past the release getVersion()
    // if (fields[1].compareTo(getReleaseVersion()) > 0) {
    // reader.push(line);
    // break;
    // }
    //
    // // Configure mapping
    // final Mapping mapping = new MappingJpa();
    //
    // // configure mapping and create map set if needed
    // mapSetHelper(mapping, fields);
    //
    // logAndCommit(++objectCt, RootService.logCt, RootService.commitCt);
    //
    // }
    // }
    //
    // // Final commit
    // commitClearBegin();

    // Extended maps
    PushBackReader reader = readers.getReader(Rf2Readers.Keys.EXTENDED_MAP);
    // Iterate over mappings
    while ((line = reader.readLine()) != null) {

      // Split line
      final String fields[] = FieldedStringTokenizer.split(line, "\t");
      // Skip header
      if (!fields[0].equals(id)) {

        // Stop if the effective time is past the release getVersion()
        if (fields[1].compareTo(getReleaseVersion()) > 0) {
          reader.push(line);
          break;
        }

        // Configure mapping
        final Mapping mapping = new MappingJpa();

        // configure mapping and create map set if needed
        mapSetHelper(mapping, fields);

        logAndCommit(++objectCt, RootService.logCt, RootService.commitCt);

      }
    }

    // Final commit
    commitClearBegin();
  }

  /**
   * Load refset descriptor ref sets.
   *
   * @throws Exception the exception
   */
  private void loadRefsetDescriptorRefSets() throws Exception {

    String line = "";
    objectCt = 0;

    PushBackReader reader =
        readers.getReader(Rf2Readers.Keys.REFSET_DESCRIPTOR);
    while ((line = reader.readLine()) != null) {

      line = line.replace("\r", "");
      final String fields[] = FieldedStringTokenizer.split(line, "\t");

      if (!fields[0].equals(id)) { // header

        // Stop if the effective time is past the release getVersion()
        if (fields[1].compareTo(getReleaseVersion()) > 0) {
          reader.push(line);
          break;
        }

        final Date date = ConfigUtility.DATE_FORMAT.parse(fields[1]);
        ConceptSubsetMember member = new ConceptSubsetMemberJpa();
        refsetHelper(member, fields);

        // Attributes
        Attribute attribute = new AttributeJpa();
        setCommonFields(attribute, date);
        attribute.setName("attributeDescription");
        attribute.setValue(fields[6].intern());
        cacheAttributeMetadata(attribute);
        member.getAttributes().add(attribute);
        addAttribute(attribute, member);

        Attribute attribute2 = new AttributeJpa();
        setCommonFields(attribute2, date);
        attribute2.setName("attributeType");
        attribute2.setValue(fields[7].intern());
        cacheAttributeMetadata(attribute2);
        member.getAttributes().add(attribute2);
        addAttribute(attribute2, member);

        Attribute attribute3 = new AttributeJpa();
        setCommonFields(attribute3, date);
        attribute3.setName("attributeOrder");
        attribute3.setValue(fields[8].intern());
        cacheAttributeMetadata(attribute3);
        member.getAttributes().add(attribute3);
        addAttribute(attribute3, member);

        // Add member
        addSubsetMember(member);

        logAndCommit(++objectCt, RootService.logCt, RootService.commitCt);

      }
    }
  }

  /**
   * Load module dependency refset members.
   *
   * @throws Exception the exception
   */
  private void loadModuleDependencyRefSets() throws Exception {

    String line = "";
    objectCt = 0;

    PushBackReader reader =
        readers.getReader(Rf2Readers.Keys.MODULE_DEPENDENCY);
    while ((line = reader.readLine()) != null) {

      line = line.replace("\r", "");
      final String fields[] = FieldedStringTokenizer.split(line, "\t");

      if (!fields[0].equals(id)) { // header

        // Stop if the effective time is past the release getVersion()
        if (fields[1].compareTo(getReleaseVersion()) > 0) {
          reader.push(line);
          break;
        }

        final Date date = ConfigUtility.DATE_FORMAT.parse(fields[1]);
        ConceptSubsetMember member = new ConceptSubsetMemberJpa();
        refsetHelper(member, fields);

        // Attributes
        Attribute attribute = new AttributeJpa();
        setCommonFields(attribute, date);
        attribute.setName("sourceEffectiveTime");
        attribute.setValue(fields[6].intern());
        attributeNames.add(attribute.getName());
        member.getAttributes().add(attribute);
        addAttribute(attribute, member);

        Attribute attribute2 = new AttributeJpa();
        setCommonFields(attribute2, date);
        attribute2.setName("targetEffectiveTime");
        attribute2.setValue(fields[7].intern());
        attributeNames.add(attribute2.getName());
        member.getAttributes().add(attribute2);
        addAttribute(attribute2, member);

        addSubsetMember(member);

        logAndCommit(++objectCt, RootService.logCt, RootService.commitCt);

      }
    }

  }

  /**
   * Load description type refset members.
   *
   * @throws Exception the exception
   */
  private void loadDescriptionTypeRefSets() throws Exception {

    String line = "";
    objectCt = 0;

    PushBackReader reader = readers.getReader(Rf2Readers.Keys.DESCRIPTION_TYPE);
    while ((line = reader.readLine()) != null) {

      line = line.replace("\r", "");
      final String fields[] = FieldedStringTokenizer.split(line, "\t");

      if (!fields[0].equals(id)) { // header

        // Stop if the effective time is past the release getVersion()
        if (fields[1].compareTo(getReleaseVersion()) > 0) {
          reader.push(line);
          break;
        }

        final Date date = ConfigUtility.DATE_FORMAT.parse(fields[1]);
        ConceptSubsetMember member = new ConceptSubsetMemberJpa();
        refsetHelper(member, fields);

        // Attributes
        Attribute attribute = new AttributeJpa();
        setCommonFields(attribute, date);
        attribute.setName("descriptionFormat");
        attribute.setValue(fields[6].intern());
        cacheAttributeMetadata(attribute);
        member.getAttributes().add(attribute);
        addAttribute(attribute, member);

        Attribute attribute2 = new AttributeJpa();
        setCommonFields(attribute2, date);
        attribute2.setName("descriptionLength");
        attribute2.setValue(fields[7].intern());
        attributeNames.add(attribute2.getName());
        member.getAttributes().add(attribute2);
        addAttribute(attribute2, member);

        addSubsetMember(member);

        logAndCommit(++objectCt, RootService.logCt, RootService.commitCt);

      }
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

    if (conceptIdMap.get(fields[5]) != null) {
      // Retrieve concept -- firstToken is referencedComponentId
      final Concept concept = getConcept(conceptIdMap.get(fields[5]));
      ((ConceptSubsetMember) member).setMember(concept);
    } else if (atomIdMap.get(fields[5]) != null) {
      final Atom description = getAtom(atomIdMap.get(fields[5]));
      ((AtomSubsetMember) member).setMember(description);
    } else {
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

    if (atomSubsetMap.containsKey(fields[4])) {
      final AtomSubset subset = atomSubsetMap.get(fields[4]);
      ((AtomSubsetMember) member).setSubset(subset);

    } else if (conceptSubsetMap.containsKey(fields[4])) {
      final ConceptSubset subset = conceptSubsetMap.get(fields[4]);
      ((ConceptSubsetMember) member).setSubset(subset);

    } else if (member instanceof AtomSubsetMember
        && !atomSubsetMap.containsKey(fields[4])) {

      final AtomSubset subset = new AtomSubsetJpa();
      setCommonFields(subset, date);
      subset.setTerminologyId(fields[4].intern());
      final Concept concept = getConcept(conceptIdMap.get(fields[4]));
      if (concept == null) {
        throw new Exception(
            "Atom refset member with refset id that cannot be resolved: "
                + fields[4]);
      }
      subset.setName(concept.getName());
      subset.setObsolete(concept.isObsolete());
      subset.setDescription(subset.getName());

      final Attribute attribute2 = new AttributeJpa();
      setCommonFields(attribute2, date);
      attribute2.setName("moduleId");
      attribute2.setValue(concept.getAttributeByName("moduleId").getValue());
      subset.getAttributes().add(attribute2);
      addAttribute(attribute2, member);
      addSubset(subset);
      atomSubsetMap.put(fields[4], subset);
      commitClearBegin();

      ((AtomSubsetMember) member).setSubset(subset);

    } else if (member instanceof ConceptSubsetMember
        && !conceptSubsetMap.containsKey(fields[4])) {

      final ConceptSubset subset = new ConceptSubsetJpa();
      setCommonFields(subset, date);
      subset.setTerminologyId(fields[4].intern());
      final Concept concept = getConcept(conceptIdMap.get(fields[4]));
      if (concept == null) {
        throw new Exception(
            "Concept refset member with  refset id that cannot be resolved: "
                + fields[4]);
      }
      subset.setName(concept.getName());
      subset.setObsolete(concept.isObsolete());
      subset.setDescription(subset.getName());
      subset.setDisjointSubset(false);

      final Attribute attribute2 = new AttributeJpa();
      setCommonFields(attribute2, date);
      attribute2.setName("moduleId");
      attribute2.setValue(concept.getAttributeByName("moduleId").getValue());
      subset.getAttributes().add(attribute2);
      addAttribute(attribute2, member);
      addSubset(subset);
      conceptSubsetMap.put(fields[4], subset);
      commitClearBegin();

      ((ConceptSubsetMember) member).setSubset(subset);

    } else {
      throw new Exception("Unable to determine refset type.");
    }

    // Add moduleId attribute
    final Attribute attribute = new AttributeJpa();
    setCommonFields(attribute, date);
    attribute.setName("moduleId");
    attribute.setValue(fields[3].intern());
    cacheAttributeMetadata(attribute);
    member.getAttributes().add(attribute);
    addAttribute(attribute, member);

  }

  /**
   * Map set helper.
   *
   * @param mapping the mapping
   * @param fields the fields
   * @throws Exception the exception
   */
  private void mapSetHelper(Mapping mapping, String[] fields) throws Exception {

    // Verify that referencedComponentId exists
    if (conceptIdMap.get(fields[5]) != null) {
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

    if (mapSetMap.containsKey(fields[4])) {
      final MapSet subset = mapSetMap.get(fields[4]);
      mapping.setMapSet(subset);

    } else if (!mapSetMap.containsKey(fields[4])) {

      final MapSet mapSet = new MapSetJpa();
      setCommonFields(mapSet, date);
      mapSet.setTerminologyId(fields[4].intern());
      final Concept concept = getConcept(conceptIdMap.get(fields[4]));
      mapSet.setName(concept.getName());
      mapSet.setObsolete(concept.isObsolete());
      mapSet.setFromTerminology(getTerminology());
      mapSet.setFromVersion(getVersion());
      // no way to get this
      mapSet.setToTerminology(null);
      mapSet.setToVersion(null);

      final Attribute attribute2 = new AttributeJpa();
      setCommonFields(attribute2, date);
      attribute2.setName("moduleId");
      attribute2.setValue(concept.getAttributeByName("moduleId").getValue());
      mapSet.getAttributes().add(attribute2);
      addAttribute(attribute2, mapSet);
      addMapSet(mapSet);
      mapSetMap.put(fields[4], mapSet);
      commitClearBegin();

      mapping.setMapSet(mapSet);

    } else {
      throw new Exception("Unable to determine mapset type.");
    }

    // get concepts from cache, they just need to have ids
    final Concept fromConcept = getConcept(conceptIdMap.get(fields[5]));
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
    addMapping(mapping);

    // Add moduleId attribute
    final Attribute attribute = new AttributeJpa();
    setCommonFields(attribute, date);
    attribute.setName("moduleId");
    attribute.setValue(fields[3].intern());
    cacheAttributeMetadata(attribute);
    mapping.getAttributes().add(attribute);
    addAttribute(attribute, mapping);

  }

  /**
   * Load metadata.
   *
   * @throws Exception the exception
   */
  private void loadMetadata() throws Exception {

    // Term types - each description type
    for (final String tty : termTypes) {
      final TermType termType = new TermTypeJpa();
      termType.setTerminology(getTerminology());
      termType.setVersion(getVersion());
      termType.setAbbreviation(tty);
      termType.setCodeVariantType(CodeVariantType.SY);
      termType.setExpandedForm(getConcept(conceptIdMap.get(tty)).getName());
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
      addTermType(termType);
    }

    // Languages - each language value
    Language rootLanguage = null;
    for (final String lat : languages) {
      final Language language = new LanguageJpa();
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
      addLanguage(language);
      if (rootLanguage == null) {
        rootLanguage = language;
      }
    }

    // attribute name
    for (final String atn : attributeNames) {
      final AttributeName name = new AttributeNameJpa();
      name.setTerminology(getTerminology());
      name.setVersion(getVersion());
      name.setLastModified(releaseVersionDate);
      name.setLastModifiedBy(loader);
      name.setPublishable(true);
      name.setPublished(true);
      name.setExpandedForm(atn);
      name.setAbbreviation(atn);
      addAttributeName(name);
    }

    // relationship types - subClassOf, superClassOf
    String[] relTypes = new String[] {
        "other", "Inverse is a", "Is a"
    };
    RelationshipType chd = null;
    RelationshipType par = null;
    RelationshipType ro = null;
    for (final String rel : relTypes) {
      final RelationshipType type = new RelationshipTypeJpa();
      type.setTerminology(getTerminology());
      type.setVersion(getVersion());
      type.setLastModified(releaseVersionDate);
      type.setLastModifiedBy(loader);
      type.setPublishable(true);
      type.setPublished(true);
      type.setAbbreviation(rel);
      type.setHierarchical(false);
      if (rel.equals("Is a")) {
        chd = type;
        type.setExpandedForm("Is a (has parent)");
        type.setHierarchical(true);
      } else if (rel.equals("Inverse is a")) {
        par = type;
        type.setExpandedForm("Inverse is a (has child)");
      } else if (rel.equals("other")) {
        ro = type;
        type.setExpandedForm("Other");
      } else {
        throw new Exception("Unhandled type");
      }
      addRelationshipType(type);
    }
    chd.setInverse(par);
    par.setInverse(chd);
    ro.setInverse(ro);
    updateRelationshipType(chd);
    updateRelationshipType(par);
    updateRelationshipType(ro);

    // additional relationship types (including grouping type, hierarchical
    // type)
    AdditionalRelationshipType directSubstance = null;
    AdditionalRelationshipType hasActiveIngredient = null;
    Map<AdditionalRelationshipType, AdditionalRelationshipType> inverses =
        new HashMap<>();
    for (final String rela : additionalRelTypes) {
      final AdditionalRelationshipType type =
          new AdditionalRelationshipTypeJpa();
      type.setTerminology(getTerminology());
      type.setVersion(getVersion());
      type.setLastModified(releaseVersionDate);
      type.setLastModifiedBy(loader);
      type.setPublishable(true);
      type.setPublished(true);
      type.setExpandedForm(getConcept(conceptIdMap.get(rela)).getName());
      type.setAbbreviation(rela);
      // Hierarchical type
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
      addAdditionalRelationshipType(type);
      if (rela.equals("363701004")) {
        hasActiveIngredient = type;
      } else if (rela.equals("127489000")) {
        directSubstance = type;
      }
      AdditionalRelationshipType inverseType =
          new AdditionalRelationshipTypeJpa(type);
      inverseType.setId(null);
      inverseType.setAbbreviation("inverse_" + type.getAbbreviation());
      inverseType.setExpandedForm("inverse_" + type.getAbbreviation());
      inverses.put(type, inverseType);
      addAdditionalRelationshipType(inverseType);
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

    // semantic types - n/a

    // Compute root terminology name - remove any occurrence of "Concept"
    Concept rootConcept = null;
    if (conceptIdMap.containsKey(rootConceptId)) {
      rootConcept = getConcept(conceptIdMap.get(rootConceptId));
    }

    // Root Terminology
    String rootPrefName = null;
    if (rootConcept != null) {
      rootPrefName = rootConcept.getName().replaceAll("Concept", "")
          + (extensionInfo != null && !extensionInfo.isEmpty()
              ? " " + extensionInfo + " Edition" : "");
    }

    // Reuse root terminology if it exists already
    RootTerminology root = new RootTerminologyJpa();
    if (getRootTerminology(getTerminology()) != null) {
      root = getRootTerminology(getTerminology());
    } else {
      root.setFamily(getTerminology());
      root.setHierarchicalName(
          rootConcept != null ? rootConcept.getName() : "");
      root.setLanguage(
          rootLanguage == null ? "en" : rootLanguage.getAbbreviation());
      root.setTimestamp(releaseVersionDate);
      root.setLastModified(releaseVersionDate);
      root.setLastModifiedBy(loader);
      root.setPolyhierarchy(true);
      root.setHierarchyComputable(true);
      root.setPreferredName(
          rootPrefName == null ? root.getHierarchicalName() : rootPrefName);
      root.setRestrictionLevel(0);
      root.setTerminology(getTerminology());
      addRootTerminology(root);
    }

    // Terminology
    Terminology term = new TerminologyJpa();
    term.setTerminology(getTerminology());
    term.setVersion(getVersion());
    term.setTimestamp(releaseVersionDate);
    term.setLastModified(releaseVersionDate);
    term.setLastModifiedBy(loader);
    term.setAssertsRelDirection(true);
    term.setCurrent(true);
    term.setDescriptionLogicTerminology(true);
    term.setOrganizingClassType(IdType.CONCEPT);
    term.setPreferredName(root.getPreferredName() == null ? ""
        : root.getPreferredName() + ", " + getFileVersion());
    term.setRootTerminology(root);
    addTerminology(term);

    // Add general metadata entries for all the attribute values
    // that are concept ids.
    for (final String conceptId : generalEntryValues) {
      // Skip if there is no concept for this thing
      if (!conceptIdMap.containsKey(conceptId)) {
        logInfo("  Skipping Genral Metadata Entry = " + conceptId);
        continue;
      }
      final String name = getConcept(conceptIdMap.get(conceptId)).getName();
      final GeneralMetadataEntry entry = new GeneralMetadataEntryJpa();
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
      addGeneralMetadataEntry(entry);
    }

    String[] labels = new String[] {
        "Atoms_Label", "Subsets_Label", "Attributes_Label",
        "Semantic_Types_Label", "Obsolete_Label", "Obsolete_Indicator",
    };
    String[] labelValues = new String[] {
        "Descriptions", "Refsets", "Properties", "Semantic Tags", "Retired",
        "Retired"
    };
    int i = 0;
    for (final String label : labels) {
      final GeneralMetadataEntry entry = new GeneralMetadataEntryJpa();
      entry.setTerminology(getTerminology());
      entry.setVersion(getVersion());
      entry.setLastModified(releaseVersionDate);
      entry.setLastModifiedBy(loader);
      entry.setPublishable(true);
      entry.setPublished(true);
      entry.setAbbreviation(label);
      entry.setExpandedForm(labelValues[i++]);
      entry.setKey("label_metadata");
      entry.setType("label_values");
      addGeneralMetadataEntry(entry);
    }
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
    for (final String moduleId : moduleIds) {
      logInfo("  Create subset for module = " + moduleId);
      final Concept concept = getConcept(conceptIdMap.get(moduleId));
      final ConceptSubset subset = new ConceptSubsetJpa();
      subset.setName(concept.getName());
      subset.setDescription("Represents the members of module " + moduleId);
      subset.setDisjointSubset(false);
      subset.setLabelSubset(true);
      subset.setLastModified(releaseVersionDate);
      subset.setTimestamp(releaseVersionDate);
      subset.setLastModifiedBy(loader);
      subset.setObsolete(false);
      subset.setSuppressible(false);
      subset.setPublishable(false);
      subset.setPublished(false);
      subset.setTerminology(getTerminology());
      subset.setTerminologyId(moduleId);
      subset.setVersion(getVersion());
      addSubset(subset);
      subsets.add(subset);
      commitClearBegin();

      // Create members
      int objectCt = 0;
      logInfo("  Add subset members");
      for (final String conceptId : moduleConceptIdMap.get(moduleId)) {
        final Concept memberConcept = getConcept(conceptIdMap.get(conceptId));

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
        member.setTerminology(getTerminology());
        member.setVersion(getVersion());
        member.setSubset(subset);
        addSubsetMember(member);
        logAndCommit(++objectCt, RootService.logCt, RootService.commitCt);
      }
    }
    logInfo("    count = " + objectCt);
  }

  /**
   * xsets the common fields.
   *
   * @param component the common fields
   * @param date the date
   */
  private void setCommonFields(Component component, Date date) {
    component.setTimestamp(date);
    component.setTerminologyId("");
    component.setTerminology(getTerminology());
    component.setVersion(getVersion());
    component.setLastModified(date);
    component.setLastModifiedBy(loader);
    component.setObsolete(false);
    component.setPublishable(true);
    component.setPublished(true);
    component.setSuppressible(false);
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

  /* see superclass */
  @Override
  public void close() throws Exception {
    super.close();
    readers = null;
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {
    return new ValidationResultJpa();
  }

  /* see superclass */
  @Override
  public void checkProperties(Properties p) throws Exception {
    checkRequiredProperties(new String[] {
        "inputFile"
    }, p);
  }

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {

    if (p.getProperty("inputDir") != null) {
      setInputPath(p.getProperty("inputDir"));
    }

  }

  /* see superclass */
  @Override
  public List<AlgorithmParameter> getParameters()  throws Exception{
    final List<AlgorithmParameter> params = super.getParameters();
    AlgorithmParameter param = new AlgorithmParameterJpa("Input Dir",
        "inputDir", "Input RF2 Snapshot directory to load", "", 255,
        AlgorithmParameter.Type.DIRECTORY, "");
    params.add(param);
    return params;

  }
}
