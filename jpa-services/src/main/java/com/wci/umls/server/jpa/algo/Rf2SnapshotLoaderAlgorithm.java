/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo;

import java.util.ArrayList;
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
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.jpa.ReleaseInfoJpa;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.AtomSubsetJpa;
import com.wci.umls.server.jpa.content.AtomSubsetMemberJpa;
import com.wci.umls.server.jpa.content.AttributeJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.ConceptRelationshipJpa;
import com.wci.umls.server.jpa.content.ConceptSubsetJpa;
import com.wci.umls.server.jpa.content.ConceptSubsetMemberJpa;
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
import com.wci.umls.server.services.helpers.ProgressEvent;
import com.wci.umls.server.services.helpers.ProgressListener;
import com.wci.umls.server.services.helpers.PushBackReader;

/**
 * Implementation of an algorithm to import RF2 snapshot data.
 */
public class Rf2SnapshotLoaderAlgorithm extends HistoryServiceJpa implements
    Algorithm {

  /** Listeners. */
  private List<ProgressListener> listeners = new ArrayList<>();

  /** The logging object ct threshold. */
  private final static int logCt = 2000;

  /** The commit count. */
  private final static int commitCt = 2000;

  /** The Constant isaTypeRel. */
  private final static String isaTypeRel = "116680003";

  /** The Constant root. */
  private final static String rootConceptId = "138875005";

  /** The dpn ref set id. */
  private String dpnRefSetId = "900000000000509007";

  /** The dpn acceptability id. */
  private String dpnAcceptabilityId = "900000000000548007";

  /** The dpn type id. */
  private String dpnTypeId = "900000000000013009";

  /** The preferred atoms set. */
  private Set<String> prefAtoms = new HashSet<>();

  /** The terminology. */
  private String terminology;

  /** The terminology version. */
  private String version;

  /** The release version. */
  private String releaseVersion;

  /** The release version date. */
  private Date releaseVersionDate;

  /** The readers. */
  private Rf2Readers readers;

  /** The atom id map. */
  private Map<String, Long> atomIdMap = new HashMap<>();

  /** The concept id map. */
  private Map<String, Long> conceptIdMap = new HashMap<>();

  /** The atom subset map. */
  private Map<String, AtomSubset> atomSubsetMap = new HashMap<>();

  /** The concept subset map. */
  private Map<String, ConceptSubset> conceptSubsetMap = new HashMap<>();

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

  /**
   * Instantiates an empty {@link Rf2SnapshotLoaderAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public Rf2SnapshotLoaderAlgorithm() throws Exception {
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
   * Sets the readers.
   *
   * @param readers the readers
   */
  public void setReaders(Rf2Readers readers) {
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
      Logger.getLogger(getClass()).info("Start loading snapshot");
      Logger.getLogger(getClass()).info("  terminology = " + terminology);
      Logger.getLogger(getClass()).info("  version = " + version);
      Logger.getLogger(getClass()).info("  releaseVersion = " + releaseVersion);
      releaseVersionDate = ConfigUtility.DATE_FORMAT.parse(releaseVersion);

      // control transaction scope
      setTransactionPerOperation(false);
      // Turn of ID computation when loading a terminology
      setAssignIdentifiersFlag(false);
      // Let loader set last modified flags.
      setLastModifiedFlag(false);

      // faster performance.
      beginTransaction();

      //
      // Load concepts
      //
      Logger.getLogger(getClass()).info("  Loading Concepts...");
      loadConcepts();

      //
      // Load descriptions and language refsets
      //
      Logger.getLogger(getClass()).info("  Loading Atoms...");
      loadAtoms();

      Logger.getLogger(getClass()).info("  Loading Language Ref Sets...");
      loadLanguageRefSetMembers();

      Logger.getLogger(getClass()).info(
          "  Connecting atoms/concepts and computing preferred names...");
      connectAtomsAndConcepts();

      //
      // Load relationships
      //
      Logger.getLogger(getClass()).info("  Loading Relationships...");
      loadRelationships();

      //
      // load AssocationReference RefSets (Content)
      //
      Logger.getLogger(getClass()).info(
          "  Loading Association Reference Ref Sets...");
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
      Logger.getLogger(getClass()).info("  Loading Simple Ref Sets...");
      loadSimpleRefSets();

      //
      // Load SimpleMapRefSets
      //
      Logger.getLogger(getClass()).info("  Loading Simple Map Ref Sets...");
      loadSimpleMapRefSets();

      commitClearBegin();

      //
      // Load ComplexMapRefSets
      //
      Logger.getLogger(getClass()).info("  Loading Complex Map Ref Sets...");
      loadComplexMapRefSets();

      //
      // Load ExtendedMapRefSets
      //
      Logger.getLogger(getClass()).info("  Loading Extended Map Ref Sets...");
      loadExtendedMapRefSets();

      commitClearBegin();

      // load RefsetDescriptor RefSets (Content)
      //
      Logger.getLogger(getClass()).info(
          "  Loading Refset Descriptor Ref Sets...");
      loadRefsetDescriptorRefSets();

      //
      // load ModuleDependency RefSets (Content)
      //
      Logger.getLogger(getClass()).info(
          "  Loading Module Dependency Ref Sets...");

      loadModuleDependencyRefSets();

      //
      // load AtomType RefSets (Content)
      //
      Logger.getLogger(getClass()).info("  Loading Atom Type Ref Sets...");

      loadAtomTypeRefSets();

      // Load metadata
      loadMetadata();

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
        info.setReleaseBeginDate(releaseVersionDate);
        info.setReleaseFinishDate(releaseVersionDate);
        info.setTerminology(terminology);
        info.setVersion(version);
        info.setLastModified(releaseVersionDate);
        info.setLastModifiedBy(loader);
        addReleaseInfo(info);
      }

      // Clear concept cache
      // clear and commit
      commitClearBegin();

      Logger.getLogger(getClass()).info(
          getComponentStats(terminology, version, Branch.ROOT));

      Logger.getLogger(getClass()).info("Done ...");

    } catch (Exception e) {
      throw e;
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
   * Load concepts.
   * 
   * @throws Exception the exception
   */
  private void loadConcepts() throws Exception {

    String line = "";
    objectCt = 0;

    PushBackReader reader = readers.getReader(Rf2Readers.Keys.CONCEPT);
    while ((line = reader.readLine()) != null) {

      final String fields[] = line.split("\t");
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

        logAndCommit(++objectCt);
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
      final String fields[] = line.split("\t");
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
        relationship.setRelationshipType(fields[7].equals(isaTypeRel) ? "CHD"
            : "RO"); // typeId
        relationship.setAdditionalRelationshipType(fields[7]); // typeId
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
        attribute3.setName("characteristicTypeId");
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
            throw new Exception("Relationship "
                + relationship.getTerminologyId()
                + " -existent source concept " + fields[4]);
          }
          if (toConcept == null) {
            throw new Exception("Relationship"
                + relationship.getTerminologyId()
                + " references non-existent destination concept " + fields[5]);
          }
        }

        logAndCommit(++objectCt);

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

      final String fields[] = line.split("\t");
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
        final Concept concept = getConcept(conceptIdMap.get(fields[4]));

        if (concept != null) {
          // this also adds language refset entries
          addAtom(atom);
          atomIdMap.put(atom.getTerminologyId(), atom.getId());
        } else {
          throw new Exception("Atom " + atom.getTerminologyId()
              + " references non-existent concept " + fields[4]);
        }

        logAndCommit(++objectCt);
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
    Logger.getLogger(getClass()).info("  Connect atoms and concepts");
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
    String prevCui = null;
    String prefName = null;
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
            throw new Exception("Unable to determine preferred name for "
                + concept.getTerminologyId());
          }
          concept.setName(prefName);
          prefName = null;
          updateConcept(concept);

          // Set atom subset names
          if (atomSubsetMap.containsKey(concept.getTerminologyId())) {
            AtomSubset subset = atomSubsetMap.get(concept.getTerminologyId());
            subset.setName(concept.getName());
            subset.setDescription(concept.getName());
            updateSubset(subset);
          }

          logAndCommit(++objectCt);
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

      final String fields[] = line.split("\t");

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
            && dpnRefSetId.equals(fields[4])) {
          prefAtoms.add(member.getMember().getTerminologyId());
        }

        logAndCommit(++objectCt);

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
      final String fields[] = line.split("\t");

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
        member.addAttribute(attribute);
        addAttribute(attribute, member);

        // Add member
        addSubsetMember(member);

        logAndCommit(++objectCt);

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
      final String fields[] = line.split("\t");

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
        attribute.setName("targetComponentId");
        attribute.setValue(fields[6].intern());
        attributeNames.add(attribute.getName());
        member.addAttribute(attribute);
        addAttribute(attribute, member);

        // Add member
        addSubsetMember(member);

        logAndCommit(++objectCt);

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
      final String fields[] = line.split("\t");

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

        logAndCommit(++objectCt);

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
      final String fields[] = line.split("\t");

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

        logAndCommit(++objectCt);

      }
    }
  }

  /**
   * Load ComplexMapRefSets (Crossmap).
   * 
   * @throws Exception the exception
   */
  private void loadComplexMapRefSets() throws Exception {

    // TODO: for now terminology server doesn't load mappings.
    // later development of mapping objects is needed
  }

  /**
   * Load extended map ref sets.
   *
   * @throws Exception the exception
   */
  private void loadExtendedMapRefSets() throws Exception {
    // TODO: for now terminology server doesn't load mappings.
    // later development of mapping objects is needed
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
      final String fields[] = line.split("\t");

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

        logAndCommit(++objectCt);

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
      final String fields[] = line.split("\t");

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

        logAndCommit(++objectCt);

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
      final String fields[] = line.split("\t");

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

        logAndCommit(++objectCt);

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
      subset.setDisjointSubset(false);

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

    // relationship types - CHD, PAR, and RO
    String[] relTypes = new String[] {
        "RO", "CHD", "PAR"
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
      if (rel.equals("CHD")) {
        chd = type;
        type.setExpandedForm("Child of");
      } else if (rel.equals("PAR")) {
        par = type;
        type.setExpandedForm("Parent of");
      } else if (rel.equals("RO")) {
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
    for (String rela : additionalRelTypes) {
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
    chain
        .setAbbreviation("direct-substance o has-active-ingredient -> direct-substance");
    chain.setExpandedForm(chain.getAbbreviation());
    List<AdditionalRelationshipType> list = new ArrayList<>();
    list.add(directSubstance);
    list.add(hasActiveIngredient);
    chain.setChain(list);
    chain.setResult(directSubstance);
    addPropertyChain(chain);

    // semantic types - n/a

    // Root Terminology
    RootTerminology root = new RootTerminologyJpa();
    root.setFamily(terminology);
    root.setHierarchicalName(getConcept(conceptIdMap.get(rootConceptId))
        .getName());
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
      String name = getConcept(conceptIdMap.get(conceptId)).getName();
      Logger.getLogger(getClass()).info(
          "  Genral Metadata Entry = " + conceptId + ", " + name);
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
    

    commitClearBegin();

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
}
