/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

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

  /** The terminology. */
  private String terminology;

  /** The terminology version. */
  private String terminologyVersion;

  /** The release version. */
  private String releaseVersion;

  /** The release version date. */
  private Date releaseVersionDate;

  /** The readers. */
  private Rf2Readers readers;

  /** hash sets for retrieving concepts. */
  private Map<String, Concept> conceptCache = new HashMap<>(); // used to

  /** hash sets for retrieving descriptions. */
  private Map<String, Atom> atomCache = new HashMap<>(); // used

  /** The atom subset map. */
  private Map<String, AtomSubset> atomSubsetMap = new HashMap<>();

  /** The concept subset map. */
  private Map<String, ConceptSubset> conceptSubsetMap = new HashMap<>();

  /** hash set for storing default preferred names. */
  Map<String, String> defaultPreferredNames = new HashMap<>();

  /** The language ref set members. */
  Map<String, Set<AtomSubsetMember>> languageRefSetMembers = new HashMap<>();

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
      Logger.getLogger(getClass()).info("  version = " + terminologyVersion);
      Logger.getLogger(getClass()).info("  releaseVersion = " + releaseVersion);
      releaseVersionDate = ConfigUtility.DATE_FORMAT.parse(releaseVersion);

      SimpleDateFormat ft = new SimpleDateFormat("hh:mm:ss a"); // format for

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
      // Load concepts
      //
      Logger.getLogger(getClass()).info("  Loading Concepts...");
      long startTime = System.nanoTime();
      loadConcepts();
      Logger.getLogger(getClass()).info(
          "    elapsed time = " + getElapsedTime(startTime) + "s"
              + " (Ended at " + ft.format(new Date()) + ")");

      // Commit here, then try relationships
      commitClearBegin();

      //
      // Load descriptions and language refsets
      //
      Logger.getLogger(getClass()).info("  Loading Language Ref Sets...");
      startTime = System.nanoTime();
      loadLanguageRefSetMembers();
      Logger.getLogger(getClass()).info(
          "    elapsed time = " + getElapsedTime(startTime) + "s"
              + " (Ended at " + ft.format(new Date()) + ")");

      Logger.getLogger(getClass()).info("  Loading Atoms...");
      startTime = System.nanoTime();
      loadAtoms();
      Logger.getLogger(getClass()).info(
          "    elapsed time = " + getElapsedTime(startTime) + "s"
              + " (Ended at " + ft.format(new Date()) + ")");

      // Commit here, then try relationships
      commitClearBegin();

      //
      // Load relationships
      //
      Logger.getLogger(getClass()).info("  Loading Relationships...");
      startTime = System.nanoTime();
      loadRelationships();
      Logger.getLogger(getClass()).info(
          "    elapsed time = " + getElapsedTime(startTime) + "s"
              + " (Ended at " + ft.format(new Date()) + ")");

      //
      // load AssocationReference RefSets (Content)
      //
      Logger.getLogger(getClass()).info(
          "  Loading Association Reference Ref Sets...");
      startTime = System.nanoTime();
      loadAssociationReferenceRefSets();
      Logger.getLogger(getClass()).info(
          "    elaped time = " + getElapsedTime(startTime).toString() + "s"
              + " (Ended at " + ft.format(new Date()) + ")");

      commitClearBegin();

      //
      // Load AttributeValue RefSets (Content)
      //
      Logger.getLogger(getClass())
          .info("  Loading Attribute Value Ref Sets...");
      startTime = System.nanoTime();
      loadAttributeValueRefSets();
      Logger.getLogger(getClass()).info(
          "    elaped time = " + getElapsedTime(startTime).toString() + "s"
              + " (Ended at " + ft.format(new Date()) + ")");

      commitClearBegin();

      //
      // Load Simple RefSets (Content)
      //
      Logger.getLogger(getClass()).info("  Loading Simple Ref Sets...");
      startTime = System.nanoTime();
      loadSimpleRefSets();
      Logger.getLogger(getClass()).info(
          "    elapsed time = " + getElapsedTime(startTime) + "s"
              + " (Ended at " + ft.format(new Date()) + ")");

      //
      // Load SimpleMapRefSets
      //
      Logger.getLogger(getClass()).info("  Loading Simple Map Ref Sets...");
      startTime = System.nanoTime();
      loadSimpleMapRefSets();
      Logger.getLogger(getClass()).info(
          "    elapsed time = " + getElapsedTime(startTime) + "s"
              + " (Ended at " + ft.format(new Date()) + ")");

      commitClearBegin();

      //
      // Load ComplexMapRefSets
      //
      Logger.getLogger(getClass()).info("  Loading Complex Map Ref Sets...");
      startTime = System.nanoTime();
      loadComplexMapRefSets();
      Logger.getLogger(getClass()).info(
          "    elapsed time = " + getElapsedTime(startTime) + "s"
              + " (Ended at " + ft.format(new Date()) + ")");

      //
      // Load ExtendedMapRefSets
      //
      Logger.getLogger(getClass()).info("  Loading Extended Map Ref Sets...");
      startTime = System.nanoTime();
      loadExtendedMapRefSets();
      Logger.getLogger(getClass()).info(
          "    elapsed time = " + getElapsedTime(startTime) + "s"
              + " (Ended at " + ft.format(new Date()) + ")");

      commitClearBegin();

      //
      // load RefsetDescriptor RefSets (Content)
      //
      Logger.getLogger(getClass()).info(
          "  Loading Refset Descriptor Ref Sets...");
      startTime = System.nanoTime();
      loadRefsetDescriptorRefSets();
      Logger.getLogger(getClass()).info(
          "    elaped time = " + getElapsedTime(startTime).toString() + "s"
              + " (Ended at " + ft.format(new Date()) + ")");

      //
      // load ModuleDependency RefSets (Content)
      //
      Logger.getLogger(getClass()).info(
          "  Loading Module Dependency Ref Sets...");
      startTime = System.nanoTime();
      loadModuleDependencyRefSets();
      Logger.getLogger(getClass()).info(
          "    elaped time = " + getElapsedTime(startTime).toString() + "s"
              + " (Ended at " + ft.format(new Date()) + ")");

      //
      // load AtomType RefSets (Content)
      //
      Logger.getLogger(getClass()).info("  Loading Atom Type Ref Sets...");
      startTime = System.nanoTime();
      loadAtomTypeRefSets();
      Logger.getLogger(getClass()).info(
          "    elaped time = " + getElapsedTime(startTime).toString() + "s"
              + " (Ended at " + ft.format(new Date()) + ")");

      // Load metadata
      // TODO:

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
        info.setTerminologyVersion(terminologyVersion);
        info.setLastModified(releaseVersionDate);
        info.setLastModifiedBy(loader);
        addReleaseInfo(info);
      }
      
      // TODO: Add metadata (including things like sub/super properties)
      //   property chains, non-grouping relationships, etc.

      // Clear concept cache
      // clear and commit
      commitClearBegin();
      conceptCache.clear();

      Logger.getLogger(getClass()).info(
          getComponentStats(terminology, terminologyVersion, Branch.ROOT));

      // Final logging messages
      Logger.getLogger(getClass()).info(
          "      elapsed time = " + getTotalElapsedTimeStr(startTimeOrig));
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
   * Returns the elapsed time.
   *
   * @param time the time
   * @return the elapsed time
   */
  @SuppressWarnings("boxing")
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

        Date date = ConfigUtility.DATE_FORMAT.parse(fields[1]);
        concept.setTerminologyId(fields[0]);
        concept.setTimestamp(date);
        concept.setObsolete(fields[2].equals("0"));
        concept.setSuppressible(concept.isObsolete());
        concept.setFullyDefined(fields[4].equals("900000000000073002"));
        concept.setTerminology(terminology);
        concept.setTerminologyVersion(terminologyVersion);
        concept.setName(initPrefName);
        concept.setLastModified(date);
        concept.setLastModifiedBy(loader);
        concept.setPublished(true);
        concept.setPublishable(true);
        concept.setUsesRelationshipUnion(true);
        concept.setWorkflowStatus(published);

        // Attributes
        Attribute attribute = new AttributeJpa();
        setCommonFields(attribute);
        attribute.setName("moduleId");
        attribute.setValue(fields[3].intern());
        concept.addAttribute(attribute);
        addAttribute(attribute, concept);

        Attribute attribute2 = new AttributeJpa();
        setCommonFields(attribute2);
        attribute2.setName("definitionStatusId");
        attribute2.setValue(fields[4].intern());
        concept.addAttribute(attribute2);
        addAttribute(attribute2, concept);

        // copy concept to shed any hibernate stuff
        addConcept(concept);
        conceptCache.put(fields[0], concept);

        if (++objectCt % logCt == 0) {
          Logger.getLogger(getClass()).info("    count = " + objectCt);
        }

      }
    }

    defaultPreferredNames.clear();

  }

  /**
   * Load relationships.
   * 
   * @throws Exception the exception
   */
  private void loadRelationships() throws Exception {

    String line = "";
    objectCt = 0;
    int conceptCt = 1;
    Concept prevConcept = null;

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
        relationship.setRelationshipType(fields[7]); // typeId
        relationship.setStated(fields[8].equals("900000000000010007"));
        relationship.setInferred(fields[8].equals("900000000000011006"));
        relationship.setTerminology(terminology);
        relationship.setTerminologyVersion(terminologyVersion);
        relationship.setLastModified(releaseVersionDate);
        relationship.setLastModifiedBy(loader);
        relationship.setPublished(true);
        relationship.setPublishable(true);
        relationship.setAssertedDirection(true);

        // Attributes
        Attribute attribute = new AttributeJpa();
        setCommonFields(attribute);
        attribute.setName("moduleId");
        attribute.setValue(fields[3].intern());
        relationship.addAttribute(attribute);
        addAttribute(attribute, relationship);

        Attribute attribute2 = new AttributeJpa();
        setCommonFields(attribute2);
        attribute2.setName("characteristicTypeId");
        attribute2.setValue(fields[8].intern());
        relationship.addAttribute(attribute2);
        addAttribute(attribute2, relationship);

        Attribute attribute3 = new AttributeJpa();
        setCommonFields(attribute3);
        attribute3.setName("characteristicTypeId");
        attribute3.setValue(fields[9].intern());
        relationship.addAttribute(attribute3);
        addAttribute(attribute3, relationship);

        // get concepts from cache, they just need to have ids
        final Concept fromConcept = conceptCache.get(fields[4]);
        final Concept toConcept = conceptCache.get(fields[5]);
        if (fromConcept != null && toConcept != null) {
          relationship.setFrom(fromConcept);
          relationship.setTo(toConcept);
          // unnecessary
          // sourceConcept.addRelationship(relationship);
          addRelationship(relationship);
          if (prevConcept == null) {
            prevConcept = fromConcept;
          }

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

        if (prevConcept == null) {
          prevConcept = fromConcept;
        }

        // Identify when concept has changed, increment concept count, update
        if (!relationship.getFrom().getTerminologyId()
            .equals(prevConcept.getTerminologyId())) {
          conceptCt++;
        }

        logAndCommit(conceptCt);

        // always set prev concept
        prevConcept = relationship.getFrom();

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

    Concept prevConcept = null;
    Set<Long> conceptsTouched = new HashSet<>();
    String line = "";
    objectCt = 0;
    int langCt = 0;
    // counter for concepts
    int conceptCt = 1;

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
        atom.setLanguage(fields[5].intern());
        atom.setTermType(fields[6].intern());
        atom.setName(fields[7]);
        atom.setTerminology(terminology);
        atom.setTerminologyVersion(terminologyVersion);
        atom.setPublished(true);
        atom.setPublishable(true);
        atom.setWorkflowStatus(published);

        // Attributes
        Attribute attribute = new AttributeJpa();
        setCommonFields(attribute);
        attribute.setName("moduleId");
        attribute.setValue(fields[3].intern());
        atom.addAttribute(attribute);
        addAttribute(attribute, atom);

        Attribute attribute2 = new AttributeJpa();
        setCommonFields(attribute2);
        attribute2.setName("caseSignificanceId");
        attribute2.setValue(fields[8].intern());
        atom.addAttribute(attribute2);
        addAttribute(attribute2, atom);

        // set concept from cache and set initial prev concept
        Concept concept = conceptCache.get(fields[4]);
        if (prevConcept == null) {
          prevConcept = concept;
        }

        // Attach language refset members (if there are any)
        if (languageRefSetMembers.containsKey(atom.getTerminologyId())) {
          for (AtomSubsetMember member : languageRefSetMembers.get(atom
              .getTerminologyId())) {
            langCt++;
            member.setMember(atom);
            atom.addMember(member);
          }
          // Remove used ones so we can keep track
          languageRefSetMembers.remove(atom.getTerminologyId());
        } else {
          // Early SNOMED release have no languages
          Logger.getLogger(getClass()).debug(
              "  Atom has no languages: " + atom.getTerminologyId());
        }

        if (concept != null) {
          // unnecessary
          // concept.addAtom(description);

          // this also adds language refset entries
          addAtom(atom);
          // Cache description for connecting refsets later
          atomCache.put(atom.getTerminologyId(), atom);
        } else {
          throw new Exception("Atom " + atom.getTerminologyId()
              + " references non-existent concept " + fields[4]);
        }

        // Log and commit
        logAndCommit(++objectCt);
      }
    }
    if (prevConcept != null) {
      conceptsTouched.add(prevConcept.getId());
    }
    commitClearBegin();

    Logger.getLogger(getClass()).info(
        "      " + objectCt + " descriptions loaded for " + (conceptCt - 1)
            + " concepts");
    Logger.getLogger(getClass()).info(
        "      " + langCt + " language ref sets loaded");

    // Set concept preferred names

    // TODO: compute preferred names of all concepts here

    commitClearBegin();

    if (languageRefSetMembers.size() > 0) {
      throw new Exception("There are unattached language refset members: "
          + languageRefSetMembers);
    }
    languageRefSetMembers = null;
  }

  /**
   * Load and cache all language refset members.
   * @throws Exception the exception
   */
  private void loadLanguageRefSetMembers() throws Exception {

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

        // Universal RefSet attributes
        member.setTerminologyId(fields[0]);
        member.setTimestamp(date);
        member.setLastModified(date);
        member.setLastModifiedBy(loader);
        member.setObsolete(fields[2].equals("0"));
        member.setSuppressible(member.isObsolete());
        member.setTerminology(terminology);
        member.setTerminologyVersion(terminologyVersion);
        member.setPublished(true);
        member.setPublishable(true);

        // Attributes
        Attribute attribute = new AttributeJpa();
        setCommonFields(attribute);
        attribute.setName("acceptabilityId");
        attribute.setValue(fields[6].intern());
        member.addAttribute(attribute);
        addAttribute(attribute, member);

        if (!atomSubsetMap.containsKey(fields[4])) {
          AtomSubset subset = new AtomSubsetJpa();
          setCommonFields(subset);
          subset.setTerminologyId(fields[4].intern());
          subset.setName("TODO: ");
          subset.setDescription("TODO: ");
          subset.setDisjointSubset(false);

          Attribute attribute2 = new AttributeJpa();
          setCommonFields(attribute2);
          attribute2.setName("moduleId");
          attribute2.setValue(fields[3].intern());
          subset.addAttribute(attribute2);
          addAttribute(attribute2, member);

        }
        AtomSubset subset = atomSubsetMap.get(fields[4]);
        member.setSubset(subset);
        subset.addMember(member);

        // Cache language refset members
        if (!languageRefSetMembers.containsKey(fields[5])) {
          languageRefSetMembers.put(fields[5], new HashSet<AtomSubsetMember>());
        }
        languageRefSetMembers.get(fields[5]).add(member);
      }
    }

    // TODO: add atom subsets (maybe after descriptions)
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

        SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset> member =
            null;

        if (conceptCache.containsKey(fields[5])) {
          member = new ConceptSubsetMemberJpa();
          // Retrieve concept -- firstToken is referencedComponentId
          final Concept concept = conceptCache.get(fields[5]);
          ((ConceptSubsetMember) member).setMember(concept);
        } else if (atomCache.containsKey(fields[5])) {
          member = new AtomSubsetMemberJpa();
          final Atom description = atomCache.get(fields[5]);
          ((AtomSubsetMember) member).setMember(description);
        } else {
          throw new Exception(
              "Attribute value member connected to nonexistent object");
        }

        // Universal RefSet attributes
        final Date date = ConfigUtility.DATE_FORMAT.parse(fields[1]);
        member.setTerminologyId(fields[0]);
        member.setTimestamp(date);
        member.setLastModified(date);
        member.setLastModifiedBy(loader);
        member.setObsolete(fields[2].equals("0"));
        member.setSuppressible(member.isObsolete());
        member.setPublished(true);
        member.setPublishable(true);

        // Attributes
        Attribute attribute = new AttributeJpa();
        setCommonFields(attribute);
        attribute.setName("valueId");
        attribute.setValue(fields[6].intern());
        member.addAttribute(attribute);
        addAttribute(attribute, member);

        if (!atomSubsetMap.containsKey(fields[4])) {
          AtomSubset subset = new AtomSubsetJpa();
          setCommonFields(subset);
          subset.setTerminologyId(fields[4].intern());
          subset.setName("TODO: ");
          subset.setDescription("TODO: ");
          subset.setDisjointSubset(false);

          Attribute attribute2 = new AttributeJpa();
          setCommonFields(attribute2);
          attribute2.setName("moduleId");
          attribute2.setValue(fields[3].intern());
          subset.addAttribute(attribute2);
          addAttribute(attribute2, member);

          ((AtomSubsetMember) member).setSubset(subset);
          subset.addMember((AtomSubsetMember) member);
        } else if (!conceptSubsetMap.containsKey(fields[4])) {
          ConceptSubset subset = new ConceptSubsetJpa();
          setCommonFields(subset);
          subset.setTerminologyId(fields[4].intern());
          subset.setName("TODO: ");
          subset.setDescription("TODO: ");
          subset.setDisjointSubset(false);

          Attribute attribute2 = new AttributeJpa();
          setCommonFields(attribute2);
          attribute2.setName("moduleId");
          attribute2.setValue(fields[3].intern());
          subset.addAttribute(attribute2);
          addAttribute(attribute2, member);

          ((ConceptSubsetMember) member).setSubset(subset);
          subset.addMember((ConceptSubsetMember) member);

        }

        // Terminology attributes
        member.setTerminology(terminology);
        member.setTerminologyVersion(terminologyVersion);

        logAndCommit(++objectCt);

      }
    }

    // TODO: add subsets and subset members.
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

        SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset> member =
            null;

        if (conceptCache.containsKey(fields[5])) {
          member = new ConceptSubsetMemberJpa();
          // Retrieve concept -- firstToken is referencedComponentId
          final Concept concept = conceptCache.get(fields[5]);
          ((ConceptSubsetMember) member).setMember(concept);
        } else if (atomCache.containsKey(fields[5])) {
          member = new AtomSubsetMemberJpa();
          final Atom description = atomCache.get(fields[5]);
          ((AtomSubsetMember) member).setMember(description);
        } else {
          throw new Exception(
              "Attribute value member connected to nonexistent object");
        }

        // Universal RefSet attributes
        final Date date = ConfigUtility.DATE_FORMAT.parse(fields[1]);
        member.setTerminologyId(fields[0]);
        member.setTimestamp(date);
        member.setLastModified(date);
        member.setLastModifiedBy(loader);
        member.setObsolete(fields[2].equals("0"));
        member.setSuppressible(member.isObsolete());
        member.setPublished(true);
        member.setPublishable(true);

        // Attributes
        Attribute attribute = new AttributeJpa();
        setCommonFields(attribute);
        attribute.setName("targetComponentId");
        attribute.setValue(fields[6].intern());
        member.addAttribute(attribute);
        addAttribute(attribute, member);

        if (!atomSubsetMap.containsKey(fields[4])) {
          AtomSubset subset = new AtomSubsetJpa();
          setCommonFields(subset);
          subset.setTerminologyId(fields[4].intern());
          subset.setName("TODO: ");
          subset.setDescription("TODO: ");
          subset.setDisjointSubset(false);

          Attribute attribute2 = new AttributeJpa();
          setCommonFields(attribute2);
          attribute2.setName("moduleId");
          attribute2.setValue(fields[3].intern());
          subset.addAttribute(attribute2);
          addAttribute(attribute2, member);

          ((AtomSubsetMember) member).setSubset(subset);
          subset.addMember((AtomSubsetMember) member);
        } else if (!conceptSubsetMap.containsKey(fields[4])) {
          ConceptSubset subset = new ConceptSubsetJpa();
          setCommonFields(subset);
          subset.setTerminologyId(fields[4].intern());
          subset.setName("TODO: ");
          subset.setDescription("TODO: ");
          subset.setDisjointSubset(false);

          Attribute attribute2 = new AttributeJpa();
          setCommonFields(attribute2);
          attribute2.setName("moduleId");
          attribute2.setValue(fields[3].intern());
          subset.addAttribute(attribute2);
          addAttribute(attribute2, member);

          ((ConceptSubsetMember) member).setSubset(subset);
          subset.addMember((ConceptSubsetMember) member);

        }

        // Terminology attributes
        member.setTerminology(terminology);
        member.setTerminologyVersion(terminologyVersion);

        logAndCommit(++objectCt);

      }
    }

    // TODO: add subsets and subset members.
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

        SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset> member =
            null;

        if (conceptCache.containsKey(fields[5])) {
          member = new ConceptSubsetMemberJpa();
          // Retrieve concept -- firstToken is referencedComponentId
          final Concept concept = conceptCache.get(fields[5]);
          ((ConceptSubsetMember) member).setMember(concept);
        } else {
          throw new Exception(
              "Attribute value member connected to nonexistent object");
        }

        // Universal RefSet attributes
        final Date date = ConfigUtility.DATE_FORMAT.parse(fields[1]);
        member.setTerminologyId(fields[0]);
        member.setTimestamp(date);
        member.setLastModified(date);
        member.setLastModifiedBy(loader);
        member.setObsolete(fields[2].equals("0"));
        member.setSuppressible(member.isObsolete());
        member.setPublished(true);
        member.setPublishable(true);

        if (!conceptSubsetMap.containsKey(fields[4])) {
          ConceptSubset subset = new ConceptSubsetJpa();
          setCommonFields(subset);
          subset.setTerminologyId(fields[4].intern());
          subset.setName("TODO: ");
          subset.setDescription("TODO: ");
          subset.setDisjointSubset(false);

          Attribute attribute2 = new AttributeJpa();
          setCommonFields(attribute2);
          attribute2.setName("moduleId");
          attribute2.setValue(fields[3].intern());
          subset.addAttribute(attribute2);
          addAttribute(attribute2, member);

          ((ConceptSubsetMember) member).setSubset(subset);
          subset.addMember((ConceptSubsetMember) member);

        }

        // Terminology attributes
        member.setTerminology(terminology);
        member.setTerminologyVersion(terminologyVersion);

        logAndCommit(++objectCt);

      }
    }

    // TODO: add subsets and subset members.
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

        SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset> member =
            null;

        if (conceptCache.containsKey(fields[5])) {
          member = new ConceptSubsetMemberJpa();
          // Retrieve concept -- firstToken is referencedComponentId
          final Concept concept = conceptCache.get(fields[5]);
          ((ConceptSubsetMember) member).setMember(concept);
        } else {
          throw new Exception(
              "Attribute value member connected to nonexistent object");
        }

        // Universal RefSet attributes
        final Date date = ConfigUtility.DATE_FORMAT.parse(fields[1]);
        member.setTerminologyId(fields[0]);
        member.setTimestamp(date);
        member.setLastModified(date);
        member.setLastModifiedBy(loader);
        member.setObsolete(fields[2].equals("0"));
        member.setSuppressible(member.isObsolete());
        member.setPublished(true);
        member.setPublishable(true);

        // Attributes
        Attribute attribute = new AttributeJpa();
        setCommonFields(attribute);
        attribute.setName("mapTarget");
        attribute.setValue(fields[6].intern());
        member.addAttribute(attribute);
        addAttribute(attribute, member);

        if (!conceptSubsetMap.containsKey(fields[4])) {
          ConceptSubset subset = new ConceptSubsetJpa();
          setCommonFields(subset);
          subset.setTerminologyId(fields[4].intern());
          subset.setName("TODO: ");
          subset.setDescription("TODO: ");
          subset.setDisjointSubset(false);

          Attribute attribute2 = new AttributeJpa();
          setCommonFields(attribute2);
          attribute2.setName("moduleId");
          attribute2.setValue(fields[3].intern());
          subset.addAttribute(attribute2);
          addAttribute(attribute2, member);

          ((ConceptSubsetMember) member).setSubset(subset);
          subset.addMember((ConceptSubsetMember) member);

        }

        // Terminology attributes
        member.setTerminology(terminology);
        member.setTerminologyVersion(terminologyVersion);

        logAndCommit(++objectCt);

      }
    }

    // TODO: add subsets and subset members.
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

        SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset> member =
            null;

        if (conceptCache.containsKey(fields[5])) {
          member = new ConceptSubsetMemberJpa();
          // Retrieve concept -- firstToken is referencedComponentId
          final Concept concept = conceptCache.get(fields[5]);
          ((ConceptSubsetMember) member).setMember(concept);
        } else {
          throw new Exception(
              "Attribute value member connected to nonexistent object");
        }

        // Universal RefSet attributes
        final Date date = ConfigUtility.DATE_FORMAT.parse(fields[1]);
        member.setTerminologyId(fields[0]);
        member.setTimestamp(date);
        member.setLastModified(date);
        member.setLastModifiedBy(loader);
        member.setObsolete(fields[2].equals("0"));
        member.setSuppressible(member.isObsolete());
        member.setPublished(true);
        member.setPublishable(true);

        // Attributes
        Attribute attribute = new AttributeJpa();
        setCommonFields(attribute);
        attribute.setName("attributeDescription");
        attribute.setValue(fields[6].intern());
        member.addAttribute(attribute);
        addAttribute(attribute, member);

        Attribute attribute2 = new AttributeJpa();
        setCommonFields(attribute2);
        attribute2.setName("attributeType");
        attribute2.setValue(fields[7].intern());
        member.addAttribute(attribute2);
        addAttribute(attribute2, member);

        Attribute attribute3 = new AttributeJpa();
        setCommonFields(attribute3);
        attribute3.setName("attributeOrder");
        attribute3.setValue(fields[8].intern());
        member.addAttribute(attribute3);
        addAttribute(attribute3, member);

        if (!conceptSubsetMap.containsKey(fields[4])) {
          ConceptSubset subset = new ConceptSubsetJpa();
          setCommonFields(subset);
          subset.setTerminologyId(fields[4].intern());
          subset.setName("TODO: ");
          subset.setDescription("TODO: ");
          subset.setDisjointSubset(false);

          Attribute attribute4 = new AttributeJpa();
          setCommonFields(attribute4);
          attribute4.setName("moduleId");
          attribute4.setValue(fields[3].intern());
          subset.addAttribute(attribute4);
          addAttribute(attribute4, member);

          ((ConceptSubsetMember) member).setSubset(subset);
          subset.addMember((ConceptSubsetMember) member);

        }

        // Terminology attributes
        member.setTerminology(terminology);
        member.setTerminologyVersion(terminologyVersion);

        logAndCommit(++objectCt);

      }
    }

    // TODO: add subsets and subset members.
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

        SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset> member =
            null;

        if (conceptCache.containsKey(fields[5])) {
          member = new ConceptSubsetMemberJpa();
          // Retrieve concept -- firstToken is referencedComponentId
          final Concept concept = conceptCache.get(fields[5]);
          ((ConceptSubsetMember) member).setMember(concept);
        } else {
          throw new Exception(
              "Attribute value member connected to nonexistent object");
        }

        // Universal RefSet attributes
        final Date date = ConfigUtility.DATE_FORMAT.parse(fields[1]);
        member.setTerminologyId(fields[0]);
        member.setTimestamp(date);
        member.setLastModified(date);
        member.setLastModifiedBy(loader);
        member.setObsolete(fields[2].equals("0"));
        member.setSuppressible(member.isObsolete());
        member.setPublished(true);
        member.setPublishable(true);

        // Attributes
        Attribute attribute = new AttributeJpa();
        setCommonFields(attribute);
        attribute.setName("sourceEffectiveTime");
        attribute.setValue(fields[6].intern());
        member.addAttribute(attribute);
        addAttribute(attribute, member);

        Attribute attribute2 = new AttributeJpa();
        setCommonFields(attribute2);
        attribute2.setName("targetEffectiveTime");
        attribute2.setValue(fields[7].intern());
        member.addAttribute(attribute2);
        addAttribute(attribute2, member);

        if (!conceptSubsetMap.containsKey(fields[4])) {
          ConceptSubset subset = new ConceptSubsetJpa();
          setCommonFields(subset);
          subset.setTerminologyId(fields[4].intern());
          subset.setName("TODO: ");
          subset.setDescription("TODO: ");
          subset.setDisjointSubset(false);

          Attribute attribute4 = new AttributeJpa();
          setCommonFields(attribute4);
          attribute4.setName("moduleId");
          attribute4.setValue(fields[3].intern());
          subset.addAttribute(attribute4);
          addAttribute(attribute4, member);

          ((ConceptSubsetMember) member).setSubset(subset);
          subset.addMember((ConceptSubsetMember) member);

        }

        // Terminology attributes
        member.setTerminology(terminology);
        member.setTerminologyVersion(terminologyVersion);

        logAndCommit(++objectCt);

      }
    }

    // TODO: add subsets and subset members.

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

        SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset> member =
            null;

        if (conceptCache.containsKey(fields[5])) {
          member = new ConceptSubsetMemberJpa();
          // Retrieve concept -- firstToken is referencedComponentId
          final Concept concept = conceptCache.get(fields[5]);
          ((ConceptSubsetMember) member).setMember(concept);
        } else {
          throw new Exception(
              "Attribute value member connected to nonexistent object");
        }

        // Universal RefSet attributes
        final Date date = ConfigUtility.DATE_FORMAT.parse(fields[1]);
        member.setTerminologyId(fields[0]);
        member.setTimestamp(date);
        member.setLastModified(date);
        member.setLastModifiedBy(loader);
        member.setObsolete(fields[2].equals("0"));
        member.setSuppressible(member.isObsolete());
        member.setPublished(true);
        member.setPublishable(true);

        // Attributes
        Attribute attribute = new AttributeJpa();
        setCommonFields(attribute);
        attribute.setName("descriptionFormat");
        attribute.setValue(fields[6].intern());
        member.addAttribute(attribute);
        addAttribute(attribute, member);

        Attribute attribute2 = new AttributeJpa();
        setCommonFields(attribute2);
        attribute2.setName("descriptionLength");
        attribute2.setValue(fields[7].intern());
        member.addAttribute(attribute2);
        addAttribute(attribute2, member);

        if (!conceptSubsetMap.containsKey(fields[4])) {
          ConceptSubset subset = new ConceptSubsetJpa();
          setCommonFields(subset);
          subset.setTerminologyId(fields[4].intern());
          subset.setName("TODO: ");
          subset.setDescription("TODO: ");
          subset.setDisjointSubset(false);

          Attribute attribute4 = new AttributeJpa();
          setCommonFields(attribute4);
          attribute4.setName("moduleId");
          attribute4.setValue(fields[3].intern());
          subset.addAttribute(attribute4);
          addAttribute(attribute4, member);

          ((ConceptSubsetMember) member).setSubset(subset);
          subset.addMember((ConceptSubsetMember) member);

        }

        // Terminology attributes
        member.setTerminology(terminology);
        member.setTerminologyVersion(terminologyVersion);

        logAndCommit(++objectCt);

      }
    }

    // TODO: add subsets and subset members.

  }

  /**
   * Sets the common fields.
   *
   * @param component the common fields
   */
  private void setCommonFields(Component component) {
    component.setLastModified(releaseVersionDate);
    component.setLastModifiedBy(loader);
    component.setObsolete(false);
    component.setPublishable(true);
    component.setPublished(true);
    component.setSuppressible(false);
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
    conceptCache = null;
    atomCache = null;
    defaultPreferredNames = null;
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
