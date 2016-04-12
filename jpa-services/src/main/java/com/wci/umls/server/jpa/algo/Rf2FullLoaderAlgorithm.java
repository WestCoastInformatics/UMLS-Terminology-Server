/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
import com.wci.umls.server.helpers.CancelException;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.FieldedStringTokenizer;
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
import com.wci.umls.server.jpa.services.HistoryServiceJpa;
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
import com.wci.umls.server.services.HistoryService;
import com.wci.umls.server.services.RootService;
import com.wci.umls.server.services.helpers.ProgressEvent;
import com.wci.umls.server.services.helpers.ProgressListener;
import com.wci.umls.server.services.helpers.PushBackReader;

/**
 * Implementation of an algorithm to import RF2 snapshot data.
 */
public class Rf2FullLoaderAlgorithm
    extends AbstractTerminologyLoaderAlgorithm {

  /** Listeners. */
  private List<ProgressListener> listeners = new ArrayList<>();

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

  /** The release version. */
  private String releaseVersion = null;

  /** The release version date. */
  private Date releaseVersionDate = null;

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
  private Map<String, MapSet> conceptMapSetMap = new HashMap<>();

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
  int objectCt; //

  /** The init pref name. */
  final String initPrefName = "No default preferred name found";

  /** The loader. */
  final String loader = "loader";

  /** The id. */
  final String id = "id";

  /** The published. */
  final String published = "PUBLISHED";

  final TreePositionAlgorithm treePosAlgorithm = new TreePositionAlgorithm();

  final TransitiveClosureAlgorithm transClosureAlgorithm =
      new TransitiveClosureAlgorithm();

  final LabelSetMarkedParentAlgorithm labelSetAlgorithm =
      new LabelSetMarkedParentAlgorithm();

  /**
   * Instantiates an empty {@link Rf2FullLoaderAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public Rf2FullLoaderAlgorithm() throws Exception {
    super();
  }
  
  @Override
  public String getFileVersion() throws Exception {
    Rf2FileSorter sorter = new Rf2FileSorter();
    sorter.setInputDir(inputPath);
    return sorter.getFileVersion();
  }

  /* see superclass */
  @Override
  public void compute() throws Exception {

    // check prerequisites
    if (terminology == null) {
      throw new Exception("Terminology name must be specified");
    }
    if (version == null) {
      throw new Exception("Terminology version must be specified");
    }
    if (inputPath == null) {
      throw new Exception("Input directory must be specified");
    }

    try {

      long startTimeOrig = System.nanoTime();

      logInfo("Start loading snapshot");
      logInfo("  terminology = " + terminology);
      logInfo("  version = " + version);
      logInfo("  inputPath = " + inputPath);
      
   // Get the release versions (need to look in complex map too for October
      // releases)
      Logger.getLogger(getClass()).info("  Get release versions");
      Rf2FileSorter sorter = new Rf2FileSorter();
      final File conceptsFile =
          sorter.findFile(new File(inputPath, "Terminology"), "sct2_Concept");
      final Set<String> releaseSet = new HashSet<>();
      BufferedReader reader = new BufferedReader(new FileReader(conceptsFile));
      String line;
      while ((line = reader.readLine()) != null) {
        final String fields[] = FieldedStringTokenizer.split(line, "\t");
        if (!fields[1].equals("effectiveTime")) {
          try {
            ConfigUtility.DATE_FORMAT.parse(fields[1]);
          } catch (Exception e) {
            throw new Exception(
                "Improperly formatted date found: " + fields[1]);
          }
          releaseSet.add(fields[1]);
        }
      }
      reader.close();
      final File complexMapFile = sorter.findFile(
          new File(inputPath, "Refset/Map"), "der2_iissscRefset_ComplexMap");
      reader = new BufferedReader(new FileReader(complexMapFile));
      while ((line = reader.readLine()) != null) {
        final String fields[] = FieldedStringTokenizer.split(line, "\t");
        if (!fields[1].equals("effectiveTime")) {
          try {
            ConfigUtility.DATE_FORMAT.parse(fields[1]);
          } catch (Exception e) {
            throw new Exception(
                "Improperly formatted date found: " + fields[1]);
          }
          releaseSet.add(fields[1]);
        }
      }
      File extendedMapFile = sorter.findFile(new File(inputPath, "Refset/Map"),
          "der2_iisssccRefset_ExtendedMap");
      reader = new BufferedReader(new FileReader(extendedMapFile));
      while ((line = reader.readLine()) != null) {
        final String fields[] = FieldedStringTokenizer.split(line, "\t");
        if (!fields[1].equals("effectiveTime")) {
          try {
            ConfigUtility.DATE_FORMAT.parse(fields[1]);
          } catch (Exception e) {
            throw new Exception(
                "Improperly formatted date found: " + fields[1]);
          }
          releaseSet.add(fields[1]);
        }
      }
      
      
      reader.close();
      final List<String> releases = new ArrayList<>(releaseSet);
      Collections.sort(releases);
      
      final HistoryService historyService = new HistoryServiceJpa();
      Logger.getLogger(getClass()).info("  Releases to process");
      for (final String release : releases) {
        Logger.getLogger(getClass()).info("    release = " + release);
        ReleaseInfo releaseInfo =
            historyService.getReleaseInfo(terminology, release);
        if (releaseInfo != null) {
          throw new Exception("A release info already exists for " + release);
        }
      }
      historyService.close();


    } catch (Exception e) {
      
    } finally {
      
    }

      
  }

  @Override
  public void computeTreePositions() throws Exception {

    try {
      Logger.getLogger(getClass()).info("Computing tree positions");
      treePosAlgorithm.setCycleTolerant(false);
      treePosAlgorithm.setIdType(IdType.CONCEPT);
      // some terminologies may have cycles, allow these for now.
      treePosAlgorithm.setCycleTolerant(true);
      treePosAlgorithm.setComputeSemanticType(true);
      treePosAlgorithm.setTerminology(terminology);
      treePosAlgorithm.setVersion(version);
      treePosAlgorithm.reset();
      treePosAlgorithm.compute();
      treePosAlgorithm.close();
    } catch (CancelException e) {
      Logger.getLogger(getClass()).info("Cancel request detected");
      throw new CancelException("Tree position computation cancelled");
    }

  }

  @Override
  public void computeTransitiveClosures() throws Exception {
    Logger.getLogger(getClass()).info(
        "  Compute transitive closure from  " + terminology + "/" + version);
    try {
      transClosureAlgorithm.setCycleTolerant(false);
      transClosureAlgorithm.setIdType(IdType.CONCEPT);
      transClosureAlgorithm.setTerminology(terminology);
      transClosureAlgorithm.setVersion(version);
      transClosureAlgorithm.reset();
      transClosureAlgorithm.compute();
      transClosureAlgorithm.close();

      // Compute label sets - after transitive closure
      // for each subset, compute the label set
      for (final Subset subset : getConceptSubsets(terminology, version,
          Branch.ROOT).getObjects()) {
        final ConceptSubset conceptSubset = (ConceptSubset) subset;
        if (conceptSubset.isLabelSubset()) {
          Logger.getLogger(getClass())
              .info("  Create label set for subset = " + subset);

          labelSetAlgorithm.setSubset(conceptSubset);
          labelSetAlgorithm.compute();
          labelSetAlgorithm.close();
        }
      }
    } catch (CancelException e) {
      Logger.getLogger(getClass()).info("Cancel request detected");
      throw new CancelException("Tree position computation cancelled");
    }
  }

  /* see superclass */
  @Override
  public void reset() throws Exception {
    // do nothing
  }

  /**
   * Fires a {@link ProgressEvent}.
   *
   * @param pct percent done
   * @param note progress note
   * @throws Exception the exception
   */
  public void fireProgressEvent(int pct, String note) throws Exception {
    ProgressEvent pe = new ProgressEvent(this, pct, pct, note);
    for (int i = 0; i < listeners.size(); i++) {
      listeners.get(i).updateProgress(pe);
    }
    logInfo("    " + pct + "% " + note);
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
  public void cancel() throws Exception {
    // cancel any currently running local algorithms
    treePosAlgorithm.cancel();
    transClosureAlgorithm.cancel();
    labelSetAlgorithm.cancel();

    // invoke superclass cancel
    super.cancel();
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

        // Stop if the effective time is past the release version
        if (fields[1].compareTo(releaseVersion) > 0) {
          reader.push(line);
          break;
        }

        final Date date = ConfigUtility.DATE_FORMAT.parse(fields[1]);
        concept.setTerminologyId(fields[0]);
        concept.setTimestamp(date);
        concept.setObsolete(fields[2].equals("0"));
        concept.setSuppressible(concept.isObsolete());
        concept.setFullyDefined(fields[4].equals("900000000000073002"));
        concept.setTerminology(terminology);
        concept.setVersion(version);
        concept.setName(initPrefName);
        concept.setLastModified(date);
        concept.setLastModifiedBy(loader);
        concept.setPublished(true);
        concept.setPublishable(true);
        concept.setUsesRelationshipUnion(true);
        concept.setWorkflowStatus(published);

        // Attributes
        final Attribute attribute = new AttributeJpa();
        setCommonFields(attribute, date);
        attribute.setName("moduleId");
        attribute.setValue(fields[3].intern());
        cacheAttributeMetadata(attribute);
        concept.addAttribute(attribute);
        addAttribute(attribute, concept);

        final Attribute attribute2 = new AttributeJpa();
        setCommonFields(attribute2, date);
        attribute2.setName("definitionStatusId");
        attribute2.setValue(fields[4].intern());
        cacheAttributeMetadata(attribute2);
        concept.addAttribute(attribute2);
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

        // Stop if the effective time is past the release version
        if (fields[1].compareTo(releaseVersion) > 0) {
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
        relationship.setTerminology(terminology);
        relationship.setVersion(version);
        relationship.setLastModified(releaseVersionDate);
        relationship.setLastModifiedBy(loader);
        relationship.setPublished(true);
        relationship.setPublishable(true);
        relationship.setAssertedDirection(true);

        // Attributes
        Attribute attribute = new AttributeJpa();
        setCommonFields(attribute, date);
        attribute.setName("moduleId");
        attribute.setValue(fields[3].intern());
        relationship.addAttribute(attribute);
        addAttribute(attribute, relationship);

        Attribute attribute2 = new AttributeJpa();
        setCommonFields(attribute2, date);
        attribute2.setName("characteristicTypeId");
        attribute2.setValue(fields[8].intern());
        cacheAttributeMetadata(attribute2);
        relationship.addAttribute(attribute2);
        addAttribute(attribute2, relationship);

        Attribute attribute3 = new AttributeJpa();
        setCommonFields(attribute3, date);
        attribute3.setName("modifierId");
        attribute3.setValue(fields[9].intern());
        cacheAttributeMetadata(attribute3);
        relationship.addAttribute(attribute3);
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
            throw new Exception("Relationship" + relationship.getTerminologyId()
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
        // Stop if the effective time is past the release version
        if (fields[1].compareTo(releaseVersion) > 0) {
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
        atom.setTerminology(terminology);
        atom.setVersion(version);
        atom.setPublished(true);
        atom.setPublishable(true);
        atom.setWorkflowStatus(published);

        // Attributes
        final Attribute attribute = new AttributeJpa();
        setCommonFields(attribute, date);
        attribute.setName("moduleId");
        attribute.setValue(fields[3].intern());
        atom.addAttribute(attribute);
        addAttribute(attribute, atom);

        final Attribute attribute2 = new AttributeJpa();
        setCommonFields(attribute2, date);
        attribute2.setName("caseSignificanceId");
        attribute2.setValue(fields[8].intern());
        cacheAttributeMetadata(attribute2);
        atom.addAttribute(attribute2);
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

        // Stop if the effective time is past the release version
        if (fields[1].compareTo(releaseVersion) > 0) {
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
        def.setTerminology(terminology);
        def.setVersion(version);
        def.setPublished(true);
        def.setPublishable(true);
        def.setWorkflowStatus(published);

        // Attributes
        final Attribute attribute = new AttributeJpa();
        setCommonFields(attribute, date);
        attribute.setName("moduleId");
        attribute.setValue(fields[3].intern());
        def.addAttribute(attribute);
        addAttribute(attribute, def);

        final Attribute attribute2 = new AttributeJpa();
        setCommonFields(attribute2, date);
        attribute2.setName("caseSignificanceId");
        attribute2.setValue(fields[8].intern());
        cacheAttributeMetadata(attribute2);
        def.addAttribute(attribute2);
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
            + "order by terminology, conceptId")
        .setParameter("terminology", terminology).setReadOnly(true)
        .setFetchSize(1000);
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
            logError("Unable to determine preferred name for "
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
            for (Long id : definitionMap.get(concept.getTerminologyId())) {
              concept.addDefinition(getDefinition(id));
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
      concept.addAtom(atom);
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

        // Stop if the effective time is past the release version
        if (fields[1].compareTo(releaseVersion) > 0) {
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
        member.addAttribute(attribute);
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

        // Stop if the effective time is past the release version
        if (fields[1].compareTo(releaseVersion) > 0) {
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
        member.addAttribute(attribute);
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

        // Stop if the effective time is past the release version
        if (fields[1].compareTo(releaseVersion) > 0) {
          Logger.getLogger(getClass()).debug(
              "Found effective time past release version at line " + line);
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
        relationship.setRelationshipType("RO");
        relationship.setHierarchical(false);
        relationship.setAdditionalRelationshipType(fields[4]);
        relationship.setStated(false);
        relationship.setInferred(true);
        relationship.setTerminology(terminology);
        relationship.setVersion(version);
        relationship.setLastModified(releaseVersionDate);
        relationship.setLastModifiedBy(loader);
        relationship.setPublished(true);
        relationship.setPublishable(true);
        relationship.setAssertedDirection(true);

        // ensure additional relationship type has been added
        additionalRelTypes.add(relationship.getAdditionalRelationshipType());
        generalEntryValues.add(relationship.getAdditionalRelationshipType());

        // Module Id Attribute
        Attribute attribute = new AttributeJpa();
        setCommonFields(attribute, date);
        attribute.setName("moduleId");
        attribute.setValue(fields[3].intern());
        relationship.addAttribute(attribute);
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

        // Stop if the effective time is past the release version
        if (fields[1].compareTo(releaseVersion) > 0) {
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

        // Stop if the effective time is past the release version
        if (fields[1].compareTo(releaseVersion) > 0) {
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
        member.addAttribute(attribute);
        addAttribute(attribute, member);

        // Add member
        addSubsetMember(member);

        logAndCommit(++objectCt, RootService.logCt, RootService.commitCt);

      }
    }
  }

  /**
   * Load ComplexMapRefSets (Crossmap).
   * 
   * @throws Exception the exception
   */
  private void loadComplexMapRefSets() throws Exception {
    String line = "";
    objectCt = 0;

    PushBackReader reader = readers.getReader(Rf2Readers.Keys.COMPLEX_MAP);
    // Iterate over mappings
    while ((line = reader.readLine()) != null) {

      // Split line
      final String fields[] = FieldedStringTokenizer.split(line, "\t");
      // Skip header
      if (!fields[0].equals(id)) {

        // Stop if the effective time is past the release version
        if (fields[1].compareTo(releaseVersion) > 0) {
          reader.push(line);
          break;
        }

        // Configure mapping
        final Mapping mapping = new MappingJpa();
        final Date date = ConfigUtility.DATE_FORMAT.parse(fields[1]);
        mapping.setTerminologyId(fields[0]);
        mapping.setTimestamp(date);
        mapping.setLastModified(date);
        mapping.setObsolete(fields[2].equals("0")); // active
        mapping.setSuppressible(mapping.isObsolete());
        mapping.setGroup(fields[6].intern());
        mapping.setRelationshipType("RO");
        mapping.setAdditionalRelationshipType(fields[11]);

        generalEntryValues.add(mapping.getAdditionalRelationshipType());
        additionalRelTypes.add(mapping.getAdditionalRelationshipType());
        mapping.setTerminology(terminology);
        mapping.setVersion(version);
        mapping.setLastModified(releaseVersionDate);
        mapping.setLastModifiedBy(loader);
        mapping.setPublished(true);
        // makes mapSet if it isn't in cache
        mapSetHelper(mapping, fields);
        mapping.setGroup(fields[6]);
        mapping.setRank(fields[7]);
        mapping.setRule(fields[8]);
        mapping.setAdvice(fields[9]);
        /* mapping.setCorrelationId(fields[11]); */

        // Attributes
        Attribute attribute = new AttributeJpa();
        setCommonFields(attribute, date);
        attribute.setName("moduleId");
        attribute.setValue(fields[3].intern());
        mapping.addAttribute(attribute);
        addAttribute(attribute, mapping);

        // get concepts from cache, they just need to have ids
        final Concept fromConcept = getConcept(conceptIdMap.get(fields[4]));
        if (fromConcept != null) {
          mapping.setFromTerminologyId(fromConcept.getTerminologyId());
          mapping.setFromIdType(IdType.CONCEPT);
          mapping.setToTerminologyId(fields[10]);
          mapping.setToIdType(IdType.OTHER);
          addMapping(mapping);

        } else {
          if (fromConcept == null) {
            throw new Exception("mapping " + mapping.getTerminologyId()
                + " -existent source concept " + fields[4]);
          }

        }

        logAndCommit(++objectCt, RootService.logCt, RootService.commitCt);

      }
    }

    // Final commit
    commitClearBegin();
  }

  /**
   * Load extended map ref sets.
   * 
   * 0 id 1 effectiveTime 2 active 3 moduleId 4 refSetId 5 referencedComponentId
   * 6 mapGroup 7 mapPriority 8 mapRule 9 mapAdvice 10 mapTarget 11
   * correlationId 12 mapCategoryId
   *
   * @throws Exception the exception
   */
  private void loadExtendedMapRefSets() throws Exception {
    String line = "";
    objectCt = 0;

    PushBackReader reader = readers.getReader(Rf2Readers.Keys.EXTENDED_MAP);
    // Iterate over mappings
    while ((line = reader.readLine()) != null) {

      // Split line
      final String fields[] = FieldedStringTokenizer.split(line, "\t");
      // Skip header
      if (!fields[0].equals(id)) {

        // Stop if the effective time is past the release version
        if (fields[1].compareTo(releaseVersion) > 0) {
          reader.push(line);
          break;
        }

        // Configure mapping
        final Mapping mapping = new MappingJpa();
        final Date date = ConfigUtility.DATE_FORMAT.parse(fields[1]);
        mapping.setTerminologyId(fields[0]);
        mapping.setTimestamp(date);
        mapping.setLastModified(date);
        mapping.setObsolete(fields[2].equals("0")); // active
        mapping.setSuppressible(mapping.isObsolete());
        mapping.setGroup(fields[6].intern()); // relationshipGroup
        mapping.setRelationshipType("RO");
        mapping.setAdditionalRelationshipType(fields[11]);

        generalEntryValues.add(mapping.getAdditionalRelationshipType());
        additionalRelTypes.add(mapping.getAdditionalRelationshipType());
        mapping.setTerminology(terminology);
        mapping.setVersion(version);
        mapping.setLastModified(releaseVersionDate);
        mapping.setLastModifiedBy(loader);
        mapping.setPublished(true);
        // makes mapSet if it isn't in cache
        mapSetHelper(mapping, fields);
        mapping.setGroup(fields[6]);
        mapping.setRank(fields[7]);
        mapping.setRule(fields[8]);
        mapping.setAdvice(fields[9]);
        /*
         * mapping.setCorrelationId(fields[11]);
         * mapping.setMapCategoryId(fields[12]);
         */

        // Attributes
        Attribute attribute = new AttributeJpa();
        setCommonFields(attribute, date);
        attribute.setName("moduleId");
        attribute.setValue(fields[3].intern());
        mapping.addAttribute(attribute);
        addAttribute(attribute, mapping);

        // get concepts from cache, they just need to have ids
        final Concept fromConcept = getConcept(conceptIdMap.get(fields[4]));
        if (fromConcept != null) {
          mapping.setFromTerminologyId(fromConcept.getTerminologyId());
          mapping.setFromIdType(IdType.CONCEPT);
          mapping.setToTerminologyId(fields[10]);
          mapping.setToIdType(IdType.OTHER);
          addMapping(mapping);

        } else {
          if (fromConcept == null) {
            throw new Exception("mapping " + mapping.getTerminologyId()
                + " -existent source concept " + fields[4]);
          }

        }

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

        // Stop if the effective time is past the release version
        if (fields[1].compareTo(releaseVersion) > 0) {
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
        member.addAttribute(attribute);
        addAttribute(attribute, member);

        Attribute attribute2 = new AttributeJpa();
        setCommonFields(attribute2, date);
        attribute2.setName("attributeType");
        attribute2.setValue(fields[7].intern());
        cacheAttributeMetadata(attribute2);
        member.addAttribute(attribute2);
        addAttribute(attribute2, member);

        Attribute attribute3 = new AttributeJpa();
        setCommonFields(attribute3, date);
        attribute3.setName("attributeOrder");
        attribute3.setValue(fields[8].intern());
        cacheAttributeMetadata(attribute3);
        member.addAttribute(attribute3);
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

        // Stop if the effective time is past the release version
        if (fields[1].compareTo(releaseVersion) > 0) {
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
        member.addAttribute(attribute);
        addAttribute(attribute, member);

        Attribute attribute2 = new AttributeJpa();
        setCommonFields(attribute2, date);
        attribute2.setName("targetEffectiveTime");
        attribute2.setValue(fields[7].intern());
        attributeNames.add(attribute2.getName());
        member.addAttribute(attribute2);
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
  private void loadAtomTypeRefSets() throws Exception {

    String line = "";
    objectCt = 0;

    PushBackReader reader = readers.getReader(Rf2Readers.Keys.DESCRIPTION_TYPE);
    while ((line = reader.readLine()) != null) {

      line = line.replace("\r", "");
      final String fields[] = FieldedStringTokenizer.split(line, "\t");

      if (!fields[0].equals(id)) { // header

        // Stop if the effective time is past the release version
        if (fields[1].compareTo(releaseVersion) > 0) {
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
        member.addAttribute(attribute);
        addAttribute(attribute, member);

        Attribute attribute2 = new AttributeJpa();
        setCommonFields(attribute2, date);
        attribute2.setName("descriptionLength");
        attribute2.setValue(fields[7].intern());
        attributeNames.add(attribute2.getName());
        member.addAttribute(attribute2);
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
          "Attribute value member connected to nonexistent object");
    }

    // Universal RefSet attributes
    final Date date = ConfigUtility.DATE_FORMAT.parse(fields[1]);
    member.setTerminology(terminology);
    member.setVersion(version);
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
      subset.setName(getConcept(conceptIdMap.get(fields[4])).getName());
      subset.setDescription(subset.getName());

      final Attribute attribute2 = new AttributeJpa();
      setCommonFields(attribute2, date);
      attribute2.setName("moduleId");
      attribute2.setValue(fields[3].intern());
      subset.addAttribute(attribute2);
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
      subset.setName(getConcept(conceptIdMap.get(fields[4])).getName());
      subset.setDescription(subset.getName());
      subset.setDisjointSubset(false);

      final Attribute attribute2 = new AttributeJpa();
      setCommonFields(attribute2, date);
      attribute2.setName("moduleId");
      attribute2.setValue(fields[3].intern());
      subset.addAttribute(attribute2);
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
    member.addAttribute(attribute);
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

    if (conceptIdMap.get(fields[5]) != null) {
      mapping.setTerminologyId(fields[5]);
    } else {
      throw new Exception(
          "Attribute value member connected to nonexistent object");
    }

    // Universal RefSet attributes
    final Date date = ConfigUtility.DATE_FORMAT.parse(fields[1]);
    mapping.setTerminology(terminology);
    mapping.setVersion(version);
    mapping.setTerminologyId(fields[0]);
    mapping.setTimestamp(date);
    mapping.setLastModified(date);
    mapping.setLastModifiedBy(loader);
    mapping.setObsolete(fields[2].equals("0"));
    mapping.setSuppressible(mapping.isObsolete());
    mapping.setPublished(true);
    mapping.setPublishable(true);

    if (conceptMapSetMap.containsKey(fields[4])) {
      final MapSet subset = conceptMapSetMap.get(fields[4]);
      mapping.setMapSet(subset);

    } else if (!conceptMapSetMap.containsKey(fields[4])) {

      final MapSet mapSet = new MapSetJpa();
      setCommonFields(mapSet, date);
      mapSet.setTerminologyId(fields[4].intern());
      mapSet.setName(getConcept(conceptIdMap.get(fields[4])).getName());
      mapSet.setFromTerminology(terminology);
      mapSet.setToTerminology(null); // no way to get this
      mapSet.setFromVersion(version);
      mapSet.setToVersion(null);
      mapSet.setMapVersion(version);

      final Attribute attribute2 = new AttributeJpa();
      setCommonFields(attribute2, date);
      attribute2.setName("moduleId");
      attribute2.setValue(fields[3].intern());
      mapSet.addAttribute(attribute2);
      addAttribute(attribute2, mapSet);
      addMapSet(mapSet);
      conceptMapSetMap.put(fields[4], mapSet);
      commitClearBegin();

      mapping.setMapSet(mapSet);

    } else {
      throw new Exception("Unable to determine mapset type.");
    }

    // Add moduleId attribute
    final Attribute attribute = new AttributeJpa();
    setCommonFields(attribute, date);
    attribute.setName("moduleId");
    attribute.setValue(fields[3].intern());
    cacheAttributeMetadata(attribute);
    mapping.addAttribute(attribute);
    addAttribute(attribute, mapping);

  }

  /**
   * Load metadata.
   *
   * @throws Exception the exception
   */
  private void loadMetadata() throws Exception {

    // Term types - each description type
    for (String tty : termTypes) {
      TermType termType = new TermTypeJpa();
      termType.setTerminology(terminology);
      termType.setVersion(version);
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
    for (String lat : languages) {
      Language language = new LanguageJpa();
      language.setTerminology(terminology);
      language.setVersion(version);
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
    for (String atn : attributeNames) {
      AttributeName name = new AttributeNameJpa();
      name.setTerminology(terminology);
      name.setVersion(version);
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
    for (String rel : relTypes) {
      RelationshipType type = new RelationshipTypeJpa();
      type.setTerminology(terminology);
      type.setVersion(version);
      type.setLastModified(releaseVersionDate);
      type.setLastModifiedBy(loader);
      type.setPublishable(true);
      type.setPublished(true);
      type.setAbbreviation(rel);
      if (rel.equals("Is a")) {
        chd = type;
        type.setExpandedForm("Is a (has parent)");
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
    for (String rela : additionalRelTypes) {
      System.out.println("rela : " + rela);
      AdditionalRelationshipType type = new AdditionalRelationshipTypeJpa();
      type.setTerminology(terminology);
      type.setVersion(version);
      type.setLastModified(releaseVersionDate);
      type.setLastModifiedBy(loader);
      type.setPublishable(true);
      type.setPublished(true);
      type.setExpandedForm(getConcept(conceptIdMap.get(rela)).getName());
      type.setAbbreviation(rela);
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
    for (AdditionalRelationshipType type : inverses.keySet()) {
      AdditionalRelationshipType inverseType = inverses.get(type);
      type.setInverse(inverseType);
      inverseType.setInverse(type);
      updateAdditionalRelationshipType(type);
      updateAdditionalRelationshipType(inverseType);
    }

    // property chains (see Owl)
    // $rightid{"363701004"} = "127489000"; # direct-substance o
    // has-active-ingredient -> direct-substance
    PropertyChain chain = new PropertyChainJpa();
    chain.setTerminology(terminology);
    chain.setVersion(version);
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

    // Root Terminology
    RootTerminology root = new RootTerminologyJpa();
    root.setFamily(terminology);
    root.setHierarchicalName(
        getConcept(conceptIdMap.get(rootConceptId)).getName());
    root.setLanguage(rootLanguage);
    root.setTimestamp(releaseVersionDate);
    root.setLastModified(releaseVersionDate);
    root.setLastModifiedBy(loader);
    root.setPolyhierarchy(true);
    root.setPreferredName(root.getHierarchicalName());
    root.setRestrictionLevel(-1);
    root.setTerminology(terminology);
    addRootTerminology(root);

    // Terminology
    Terminology term = new TerminologyJpa();
    term.setTerminology(terminology);
    term.setVersion(version);
    term.setTimestamp(releaseVersionDate);
    term.setLastModified(releaseVersionDate);
    term.setLastModifiedBy(loader);
    term.setAssertsRelDirection(true);
    term.setCurrent(true);
    term.setDescriptionLogicTerminology(true);
    term.setOrganizingClassType(IdType.CONCEPT);
    term.setPreferredName(root.getPreferredName());
    term.setRootTerminology(root);
    addTerminology(term);

    // Add general metadata entries for all the attribute values
    // that are concept ids.
    for (String conceptId : generalEntryValues) {
      // Skip if there is no concept for this thing
      if (!conceptIdMap.containsKey(conceptId)) {
        logInfo("  Skipping Genral Metadata Entry = " + conceptId);
        continue;
      }
      String name = getConcept(conceptIdMap.get(conceptId)).getName();
      logInfo("  Genral Metadata Entry = " + conceptId + ", " + name);
      GeneralMetadataEntry entry = new GeneralMetadataEntryJpa();
      entry.setTerminology(terminology);
      entry.setVersion(version);
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
    for (String label : labels) {
      GeneralMetadataEntry entry = new GeneralMetadataEntryJpa();
      entry.setTerminology(terminology);
      entry.setVersion(version);
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
    for (String moduleId : moduleIds) {
      logInfo("  Create subset for module = " + moduleId);
      Concept concept = getConcept(conceptIdMap.get(moduleId));
      ConceptSubset subset = new ConceptSubsetJpa();
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
      subset.setTerminology(terminology);
      subset.setTerminologyId(moduleId);
      subset.setVersion(version);
      addSubset(subset);
      subsets.add(subset);
      commitClearBegin();

      // Create members
      int objectCt = 0;
      logInfo("  Add subset members");
      for (String conceptId : moduleConceptIdMap.get(moduleId)) {
        final Concept memberConcept = getConcept(conceptIdMap.get(conceptId));

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
        member.setTerminology(terminology);
        member.setVersion(version);
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
    component.setTerminology(terminology);
    component.setVersion(version);
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


}
