/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.FlushModeType;

import org.apache.log4j.Logger;

import com.wci.umls.server.ReleaseInfo;
import com.wci.umls.server.algo.Algorithm;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.content.ConceptList;
import com.wci.umls.server.jpa.ReleaseInfoJpa;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.AtomSubsetMemberJpa;
import com.wci.umls.server.jpa.content.AttributeJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.ConceptRelationshipJpa;
import com.wci.umls.server.jpa.services.HistoryServiceJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.AtomSubset;
import com.wci.umls.server.model.content.AtomSubsetMember;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.Component;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.content.ConceptSubset;
import com.wci.umls.server.services.helpers.ConceptReportHelper;
import com.wci.umls.server.services.helpers.ProgressEvent;
import com.wci.umls.server.services.helpers.ProgressListener;
import com.wci.umls.server.services.helpers.PushBackReader;

/**
 * Implementation of an algorithm to import RF2 delta data.
 */
public class Rf2DeltaLoaderAlgorithm extends HistoryServiceJpa implements
    Algorithm {

  /** The commit count. */
  private final static int commitCt = 5000;

  /** The log count. */
  private final static int logCt = 2000;

  /** Listeners. */
  private List<ProgressListener> listeners = new ArrayList<>();

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

  /** The delta loader start date. */
  @SuppressWarnings("unused")
  private Date deltaLaderStartDate = new Date();

  /** counter for objects created, reset in each load section. */
  int objectCt; //

  /** The concept cache. */
  private Map<String, Concept> conceptCache = new HashMap<>();

  /** The atom cache. */
  private Map<String, Atom> atomCache = new HashMap<>();

  /** The relationship cache. */
  private Map<String, ConceptRelationship> relationshipCache = new HashMap<>();

  /** The language ref set member cache. */
  private Map<String, AtomSubsetMember> languageRefSetMemberCache =
      new HashMap<>();

  /** The atom subset map. */
  private Map<String, AtomSubset> atomSubsetMap = new HashMap<>();

  /** The concept subset map. */
  @SuppressWarnings("unused")
  private Map<String, ConceptSubset> conceptSubsetMap = new HashMap<>();

  /** The existing concept cache. */
  private Map<String, Concept> existingConceptCache = new HashMap<>();

  /** The loader. */
  final String loader = "loader";

  /** The init pref name. */
  final String initPrefName = "null";

  /** The published. */
  final String published = "PUBLISHED";

  /**
   * Instantiates an empty {@link Rf2DeltaLoaderAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public Rf2DeltaLoaderAlgorithm() throws Exception {
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
      Logger.getLogger(getClass()).info("Start loading delta");
      Logger.getLogger(getClass()).info("  terminology = " + terminology);
      Logger.getLogger(getClass()).info("  version = " + terminologyVersion);
      Logger.getLogger(getClass()).info("  releaseVersion = " + releaseVersion);

      releaseVersionDate = ConfigUtility.DATE_FORMAT.parse(releaseVersion);

      // Clear the query cache

      // Track system level information
      long startTimeOrig = System.nanoTime();

      // This is OK because every time we query the database
      // it is for an object graph we have not yet seen so flushing
      // of changes is not important until the end.
      manager.setFlushMode(FlushModeType.COMMIT);

      // Setup history service
      // Turn of ID computation when loading a terminology
      setAssignIdentifiersFlag(false);
      setTransactionPerOperation(false);
      beginTransaction();

      // Precache all existing concept entires (not connected data like
      // rels/descs)
      Logger.getLogger(getClass()).info("  Cache concepts");
      ConceptList conceptList =
          getAllConcepts(terminology, terminologyVersion, Branch.ROOT);
      for (Concept c : conceptList.getObjects()) {
        existingConceptCache.put(c.getTerminologyId(), c);
      }
      Logger.getLogger(getClass())
          .info("    count = " + conceptList.getCount());

      //
      // Load concepts
      //
      Logger.getLogger(getClass()).info("    Loading Concepts ...");
      loadConcepts();

      //
      // Load atoms and definitions
      //
      Logger.getLogger(getClass()).info("    Loading Atoms ...");
      loadAtoms();

      //
      // Load language refset members
      //
      Logger.getLogger(getClass()).info("    Loading Language Ref Sets...");
      loadAtomSubsetMembers();

      // Compute preferred names
      Logger.getLogger(getClass()).info(
          "  Compute preferred names for modified concepts");
      int ct = 0;
      for (String terminologyId : conceptCache.keySet()) {
        Concept concept = conceptCache.get(terminologyId);
        String pn = getComputedPreferredName(concept);
        if (!pn.equals(concept.getName())) {
          ct++;
          concept.setName(pn);
        }
        // Mark all cached concepts for update
        if (existingConceptCache.containsKey(terminologyId)) {
          Logger.getLogger(getClass()).debug(
              ConceptReportHelper.getConceptReport(concept));
          updateConcept(concept);
        }
      }

      commit();
      clear();
      beginTransaction();

      // cache existing concepts again (after relationships)
      // Cascade objects are finished, these just need a concept with an id.
      conceptCache.clear();
      // Save atoms cache for attributeValue/AssocationRef processing
      // atomCache.clear();
      languageRefSetMemberCache.clear();
      Logger.getLogger(getClass()).info("  Cache concepts");
      conceptList =
          getAllConcepts(terminology, terminologyVersion, Branch.ROOT);
      for (Concept c : conceptList.getObjects()) {
        existingConceptCache.put(c.getTerminologyId(), c);
      }
      Logger.getLogger(getClass())
          .info("    count = " + conceptList.getCount());

      //
      // Load relationships - stated and inferred
      //
      Logger.getLogger(getClass()).info("    Loading Relationships ...");
      loadRelationships();

      // Clear relationships cache
      relationshipCache = null;

      commit();
      clear();
      beginTransaction();

      //
      // Load simple refset members
      //
      Logger.getLogger(getClass()).info("    Loading Simple Ref Sets...");
      loadSimpleRefSetMembers();

      commit();
      clear();
      beginTransaction();

      //
      // Load simple map refset members
      //
      Logger.getLogger(getClass()).info("    Loading Simple Map Ref Sets...");
      loadSimpleMapRefSetMembers();

      commit();
      clear();
      beginTransaction();

      //
      // Load complex map refset members
      //
      Logger.getLogger(getClass()).info("    Loading Complex Map Ref Sets...");
      loadComplexMapRefSetMembers();

      //
      // Load extended map refset members
      //
      Logger.getLogger(getClass()).info("    Loading Extended Map Ref Sets...");
      loadExtendedMapRefSetMembers();

      //
      // Load atom type refset members
      //
      Logger.getLogger(getClass()).info("    Loading Atom Type Ref Sets...");
      loadAtomTypeRefSetMembers();

      //
      // Load refset descriptor refset members
      //
      Logger.getLogger(getClass()).info(
          "    Loading Refset Descriptor Ref Sets...");
      loadRefsetDescriptorRefSetMembers();

      //
      // Load module dependency refset members
      //
      Logger.getLogger(getClass()).info(
          "    Loading Module Dependency Ref Sets...");
      loadModuleDependencyRefSetMembers();

      commit();
      clear();
      beginTransaction();

      //
      // Load module dependency refset members
      //
      Logger.getLogger(getClass()).info(
          "    Loading Attribute Value Ref Sets...");
      loadAttributeValueRefSetMembers();

      commit();
      clear();
      beginTransaction();

      //
      // Load association reference refset members
      //
      Logger.getLogger(getClass()).info(
          "    Loading Association Reference Ref Sets...");
      loadAssociationReferenceRefSetMembers();

      commit();
      clear();
      beginTransaction();

      Logger.getLogger(getClass()).info("    changed = " + ct);

      // Commit the content changes
      Logger.getLogger(getClass()).info("  Committing");

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
        info.setTerminology(terminology);
        info.setTerminologyVersion(terminologyVersion);
        info.setLastModified(releaseVersionDate);
        info.setLastModifiedBy(loader);
        addReleaseInfo(info);
      }

      // Commit and clear resources
      commit();
      clear();

      Logger.getLogger(getClass()).info(
          getComponentStats(terminology, terminologyVersion, Branch.ROOT));

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
  @Override
  public void cancel() {
    throw new UnsupportedOperationException("cannot cancel.");
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
   * Loads the concepts from the delta files.
   *
   * @throws Exception the exception
   */
  private void loadConcepts() throws Exception {

    // Setup vars
    String line;
    objectCt = 0;
    int objectsAdded = 0;
    int objectsUpdated = 0;

    // Iterate through concept reader
    PushBackReader reader = readers.getReader(Rf2Readers.Keys.CONCEPT);
    while ((line = reader.readLine()) != null) {

      // Split line
      String fields[] = line.split("\t");

      // if not header
      if (!fields[0].equals("id")) {

        // Skip if the effective time is before the release version
        if (fields[1].compareTo(releaseVersion) < 0) {
          continue;
        }

        // Stop if the effective time is past the release version
        if (fields[1].compareTo(releaseVersion) > 0) {
          reader.push(line);
          break;
        }

        // Check if concept exists from before
        Concept concept = existingConceptCache.get(fields[0]);

        // Setup delta concept (either new or based on existing one)
        Concept newConcept = null;
        if (concept == null) {
          newConcept = new ConceptJpa();
        } else {
          newConcept = new ConceptJpa(concept, true);
        }

        // Set fields
        newConcept.setTerminologyId(fields[0]);
        newConcept.setTimestamp(ConfigUtility.DATE_FORMAT.parse(fields[1]));
        newConcept.setObsolete(fields[2].equals("0"));
        // This is SNOMED specific
        newConcept.setFullyDefined(fields[4].equals("900000000000073002"));
        newConcept.setTerminology(terminology);
        newConcept.setTerminologyVersion(terminologyVersion);
        newConcept.setName(initPrefName);
        newConcept.setLastModifiedBy(loader);
        newConcept.setLastModified(releaseVersionDate);
        newConcept.setPublished(true);
        newConcept.setPublishable(true);
        newConcept.setWorkflowStatus(published);

        // Attributes
        Attribute attribute = new AttributeJpa();
        setCommonFields(attribute);
        attribute.setName("moduleId");
        attribute.setValue(fields[3].intern());
        newConcept.addAttribute(attribute);
        addAttribute(attribute, newConcept);

        Attribute attribute2 = new AttributeJpa();
        setCommonFields(attribute2);
        attribute2.setName("definitionStatusId");
        attribute2.setValue(fields[4].intern());
        newConcept.addAttribute(attribute2);
        addAttribute(attribute2, newConcept);

        // If concept is new, add it
        if (concept == null) {
          newConcept = addConcept(newConcept);
          objectsAdded++;
        }

        // If concept has changed, update it
        else if (!newConcept.equals(concept)) {
          // Do not actually update the concept here, wait for any other
          // changes,
          // then do it at the end (to support cascade)
          objectsUpdated++;
        }

        // Cache the concept
        cacheConcept(newConcept);
      }
    }

    Logger.getLogger(getClass()).info("      new = " + objectsAdded);
    Logger.getLogger(getClass()).info("      updated = " + objectsUpdated);

  }

  /**
   * Load atoms.
   *
   * @throws Exception the exception
   */
  private void loadAtoms() throws Exception {

    // Setup vars
    String line = "";
    objectCt = 0;
    int objectsAdded = 0;
    int objectsUpdated = 0;
    // Iterate through atom reader
    PushBackReader reader = readers.getReader(Rf2Readers.Keys.DESCRIPTION);
    while ((line = reader.readLine()) != null) {
      // split line
      String fields[] = line.split("\t");

      // if not header
      if (!fields[0].equals("id")) {

        // Skip if the effective time is before the release version
        if (fields[1].compareTo(releaseVersion) < 0) {
          continue;
        }

        // Stop if the effective time is past the release version
        if (fields[1].compareTo(releaseVersion) > 0) {
          reader.push(line);
          break;
        }

        // Get concept from cache or from db
        Concept concept = null;
        if (conceptCache.containsKey(fields[4])) {
          concept = conceptCache.get(fields[4]);
        } else if (existingConceptCache.containsKey(fields[4])) {
          concept = existingConceptCache.get(fields[4]);
          // it's not yet in the cache, put it there
          cacheConcept(concept);
        } else {
          // if the concept is new, it will have been added
          // if the concept is existing it will either have been udpated
          // or will be in the existing concept cache
          throw new Exception(
              "Concept of atom should either be in cache or existing cache: "
                  + fields[4]);
        }

        // if the concept is not null
        if (concept != null) {

          // Load atom from cache or db
          Atom atom = null;
          if (atomCache.containsKey(fields[0])) {
            atom = atomCache.get(fields[0]);
          }

          // Setup delta atom (either new or based on existing one)
          Atom newAtom = null;
          if (atom == null) {
            newAtom = new AtomJpa();
          } else {
            newAtom = new AtomJpa(atom, false);
          }

          // Set fields
          newAtom.setTerminologyId(fields[0]);
          newAtom.setTimestamp(ConfigUtility.DATE_FORMAT.parse(fields[1]));
          newAtom.setObsolete(fields[2].equals("0"));
          newAtom.setLanguage(fields[5]);
          newAtom.setTermType(fields[6]);
          newAtom.setName(fields[7]);
          newAtom.setTerminology(terminology);
          newAtom.setTerminologyVersion(terminologyVersion);
          newAtom.setLastModifiedBy(loader);
          newAtom.setLastModified(releaseVersionDate);
          newAtom.setPublished(true);
          newAtom.setWorkflowStatus(published);

          // Attributes
          Attribute attribute = new AttributeJpa();
          setCommonFields(attribute);
          attribute.setName("moduleId");
          attribute.setValue(fields[3].intern());
          newAtom.addAttribute(attribute);
          addAttribute(attribute, newAtom);

          Attribute attribute2 = new AttributeJpa();
          setCommonFields(attribute2);
          attribute2.setName("caseSignificanceId");
          attribute2.setValue(fields[8].intern());
          newAtom.addAttribute(attribute2);
          addAttribute(attribute2, newAtom);

          // If atom is new, add it
          if (atom == null) {
            newAtom = addAtom(newAtom);
            concept.addAtom(newAtom);
            objectsAdded++;
          }

          // If atom has changed, update it
          else if (!newAtom.equals(atom)) {
            Logger.getLogger(getClass()).debug("  update atom - " + newAtom);

            // do not actually update the atom, the concept is cached
            // and will be updated later, simply update the data structure
            concept.removeAtom(atom);
            concept.addAtom(newAtom);
            objectsUpdated++;
          }

          // forcably recache the concept in case the atom is new.
          conceptCache.remove(concept.getTerminologyId());
          cacheConcept(concept);

        }

        // Major error if there is a delta atom with a
        // non-existent concept
        else {
          throw new Exception("Could not find concept " + fields[4]
              + " for Atom " + fields[0]);
        }
      }
    }
    Logger.getLogger(getClass()).info("      new = " + objectsAdded);
    Logger.getLogger(getClass()).info("      updated = " + objectsUpdated);
  }

  /**
   * Load language ref set members.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings("unused")
  private void loadAtomSubsetMembers() throws Exception {

    // Setup variables
    String line = "";
    objectCt = 0;
    int objectsAdded = 0;
    int objectsUpdated = 0;

    // Iterate through language refset reader
    PushBackReader reader = readers.getReader(Rf2Readers.Keys.LANGUAGE);
    while ((line = reader.readLine()) != null) {

      // split line
      String fields[] = line.split("\t");

      // if not header
      if (!fields[0].equals("id")) {
        // Skip if the effective time is before the release version
        if (fields[1].compareTo(releaseVersion) < 0) {
          continue;
        }

        // Stop if the effective time is past the release version
        if (fields[1].compareTo(releaseVersion) > 0) {
          reader.push(line);
          break;
        }

        // Get the atom
        Atom atom = null;
        if (atomCache.containsKey(fields[5])) {
          atom = atomCache.get(fields[5]);
          // if here, the concept has already been cached.
        } else {
          // the atom may not yet be in the cache because
          // the language refset entry could be the first element for
          // the concept that is changed. After the cache concept call
          // below, the atom will be in the cache next time
          atom =
              getAtom(fields[5], terminology, terminologyVersion, Branch.ROOT);
          cacheConcept(getConcept(atom.getConceptId(), terminology,
              terminologyVersion, Branch.ROOT));
        }

        // Ensure effective time is set on all appropriate objects
        AtomSubsetMember member = null;
        if (languageRefSetMemberCache.containsKey(fields[0])) {
          member = languageRefSetMemberCache.get(fields[0]);
          // to investigate if there will be an update
        }

        // Setup delta language entry (either new or based on existing
        // one)
        AtomSubsetMember newMember = null;
        if (member == null) {
          newMember = new AtomSubsetMemberJpa();
        } else {
          newMember = new AtomSubsetMemberJpa(member, false);
        }

        newMember.setMember(atom);

        newMember.setTerminologyId(fields[0]);
        newMember.setTimestamp(ConfigUtility.DATE_FORMAT.parse(fields[1]));
        newMember.setObsolete(fields[2].equals("0"));
        newMember.setTerminology(terminology);
        newMember.setTerminologyVersion(terminologyVersion);
        newMember.setLastModifiedBy(loader);
        newMember.setLastModified(releaseVersionDate);
        newMember.setPublished(true);
        newMember.setPublishable(true);

        // Attributes
        Attribute attribute = new AttributeJpa();
        setCommonFields(attribute);
        attribute.setName("acceptabilityId");
        attribute.setValue(fields[6].intern());
        member.addAttribute(attribute);
        addAttribute(attribute, newMember);

        if (!atomSubsetMap.containsKey(fields[4])) {
          AtomSubset subset =
              (AtomSubset) getSubset(fields[4], terminology,
                  terminologyVersion, Branch.ROOT);
          atomSubsetMap.put(fields[4], subset);
        }
        AtomSubset subset = atomSubsetMap.get(fields[4]);
        member.setSubset(subset);
        subset.addMember(member);

        // If language refset entry is new, add it
        if (member == null) {
          newMember = (AtomSubsetMember) addSubsetMember(newMember);
          atom.addMember(newMember);
          objectsAdded++;
        }

        // If language refset entry is changed, update it
        else if (!newMember.equals(member)) {
          Logger.getLogger(getClass())
              .debug("  update language - " + newMember);

          // do not actually update the language, the atom's concept is
          // cached
          // and will be updated later, simply update the data structure
          atom.removeMember(member);
          atom.addMember(newMember);

          objectsUpdated++;
        }

        // forcably recache the concept
        conceptCache.remove(atom.getConceptId());
        cacheConcept(getConcept(atom.getConceptId(), terminology,
            terminologyVersion, Branch.ROOT));

      }
    }

    Logger.getLogger(getClass()).info("      new = " + objectsAdded);
    Logger.getLogger(getClass()).info("      updated = " + objectsUpdated);

  }

  /**
   * Load simple ref set members.
   *
   * @throws Exception the exception
   */
  private void loadSimpleRefSetMembers() throws Exception {
    // n/a
  }

  /**
   * Load simple map ref set members.
   *
   * @throws Exception the exception
   */
  private void loadSimpleMapRefSetMembers() throws Exception {
    // n/a
  }

  /**
   * Load complex map ref set members.
   *
   * @throws Exception the exception
   */

  private void loadComplexMapRefSetMembers() throws Exception {
    // n/a
  }

  /**
   * Load extended map ref set members.
   *
   * @throws Exception the exception
   */

  private void loadExtendedMapRefSetMembers() throws Exception {
    // TODO: do when we have mapping objects
  }

  /**
   * Load atom type ref set members.
   *
   * @throws Exception the exception
   */

  private void loadAtomTypeRefSetMembers() throws Exception {
    // n/a
  }

  /**
   * Load refset descriptor ref set members.
   *
   * @throws Exception the exception
   */

  private void loadRefsetDescriptorRefSetMembers() throws Exception {
    // n/a
  }

  /**
   * Load module dependency ref set members.
   *
   * @throws Exception the exception
   */

  private void loadModuleDependencyRefSetMembers() throws Exception {
    // n/a
  }

  /**
   * Load attribute value ref set members.
   *
   * @throws Exception the exception
   */

  private void loadAttributeValueRefSetMembers() throws Exception {
    // n/a
  }

  /**
   * Load association reference ref set members.
   *
   * @throws Exception the exception
   */

  private void loadAssociationReferenceRefSetMembers() throws Exception {
    // n/a
  }

  /**
   * Load relationships.
   *
   * @throws Exception the exception
   */

  private void loadRelationships() throws Exception {

    // Setup variables
    String line = "";
    objectCt = 0;
    int objectsAdded = 0;
    int objectsUpdated = 0;

    // Iterate through relationships reader
    PushBackReader reader = readers.getReader(Rf2Readers.Keys.RELATIONSHIP);
    while ((line = reader.readLine()) != null) {

      // Split line
      String fields[] = line.split("\t");

      // If not header
      if (!fields[0].equals("id")) {

        // Skip if the effective time is before the release version
        if (fields[1].compareTo(releaseVersion) < 0) {
          continue;
        }

        // Stop if the effective time is past the release version
        if (fields[1].compareTo(releaseVersion) > 0) {
          reader.push(line);
          break;
        }

        // Retrieve source concept
        Concept sourceConcept = null;
        Concept destinationConcept = null;
        if (existingConceptCache.containsKey(fields[4])) {
          sourceConcept = existingConceptCache.get(fields[4]);
        }
        if (sourceConcept == null) {
          throw new Exception("Relationship " + fields[0] + " source concept "
              + fields[4] + " cannot be found");
        }

        // Retrieve destination concept
        if (existingConceptCache.containsKey(fields[5])) {
          destinationConcept = existingConceptCache.get(fields[5]);
          // no need to reread because we are not caching this concept
        }
        if (destinationConcept == null) {
          throw new Exception("Relationship " + fields[0]
              + " destination concept " + fields[5] + " cannot be found");
        }

        // Retrieve relationship if it exists
        ConceptRelationship relationship = null;
        if (relationshipCache.containsKey(fields[0])) {
          relationship = relationshipCache.get(fields[0]);
        }

        // Setup delta relationship (either new or based on existing one)
        ConceptRelationship newRelationship = null;
        boolean addFlag = false;
        if (relationship == null) {
          newRelationship = new ConceptRelationshipJpa();
          addFlag = true;
        } else {
          newRelationship = new ConceptRelationshipJpa(relationship, true);
        }

        // Set fields
        newRelationship.setTerminologyId(fields[0]);
        newRelationship
            .setTimestamp(ConfigUtility.DATE_FORMAT.parse(fields[1]));
        newRelationship.setObsolete(fields[2].equals("0")); // active
        newRelationship.setGroup(fields[6]); // relationshipGroup
        newRelationship.setRelationshipType(fields[7]);
        // This is SNOMED specific
        newRelationship.setStated(fields[8].equals("900000000000010007"));
        newRelationship.setInferred(fields[8].equals("900000000000011006"));

        newRelationship.setTerminology(terminology);
        newRelationship.setTerminologyVersion(terminologyVersion);
        newRelationship.setFrom(sourceConcept);
        newRelationship.setTo(destinationConcept);
        newRelationship.setLastModifiedBy(loader);
        newRelationship.setLastModified(releaseVersionDate);
        newRelationship.setPublished(true);

        // TODO
        // newRelationship.setModifierId(fields[9]);
        // newRelationship.setModuleId(fields[3]); // moduleId
        // newRelationship.setCharacteristicTypeId(fields[8]); //
        // characteristicTypeId

        // If relationship is new, add it
        if (addFlag) {
          newRelationship = (ConceptRelationship) addRelationship(newRelationship);
          objectsAdded++;
        }

        // If relationship is changed, update it
        else if (relationship != null && !newRelationship.equals(relationship)) {
          updateRelationship(newRelationship);
          objectsUpdated++;

          // TODO: or if the attributes are changed
        }

        logAndCommit(objectsAdded + objectsUpdated);
      }
    }

    Logger.getLogger(getClass()).info("      new = " + objectsAdded);
    Logger.getLogger(getClass()).info("      updated = " + objectsUpdated);

  }

  // helper function to update and store concept
  // as well as putting all descendant objects in the cache
  // for easy retrieval
  /**
   * Cache concept.
   *
   * @param c the c
   * @throws Exception the exception
   */
  private void cacheConcept(Concept c) throws Exception {
    if (!conceptCache.containsKey(c.getTerminologyId())) {
      for (ConceptRelationship r : c.getRelationships()) {
        relationshipCache.put(r.getTerminologyId(),  r);
      }
      for (Atom d : c.getAtoms()) {
        for (AtomSubsetMember l : d.getMembers()) {
          languageRefSetMemberCache.put(l.getTerminologyId(), l);
        }
        atomCache.put(d.getTerminologyId(), d);
      }
      conceptCache.put(c.getTerminologyId(), c);
    }
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
    languageRefSetMemberCache = null;
    existingConceptCache = null;
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
