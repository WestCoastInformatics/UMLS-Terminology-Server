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

import javax.persistence.FlushModeType;
import javax.persistence.Query;

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
import com.wci.umls.server.jpa.services.HistoryServiceJpa;
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
import com.wci.umls.server.model.content.Subset;
import com.wci.umls.server.model.content.SubsetMember;
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
  private String version;

  /** The release version. */
  private String releaseVersion;

  /** The release version date. */
  private Date releaseVersionDate;

  /** The readers. */
  private Rf2Readers readers;

  /** The delta loader start date. */
  @SuppressWarnings("unused")
  private Date deltaLoaderStartDate = new Date();

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

  // TODO: manage metadata changes (e.g. new attribute names, etc)
  /** The attribute names. */
  private Set<String> attributeNames = new HashSet<>();

  /** The concept attribute values. */
  private Set<String> generalEntryValues = new HashSet<>();

  /** The loader. */
  final String loader = "loader";

  /** The init pref name. */
  final String initPrefName = "Default prefered name could not be determined";

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

  /* see superclass */
  @Override
  public void compute() throws Exception {
    try {
      Logger.getLogger(getClass()).info("Start loading delta");
      Logger.getLogger(getClass()).info("  terminology = " + terminology);
      Logger.getLogger(getClass()).info("  version = " + version);
      Logger.getLogger(getClass()).info("  releaseVersion = " + releaseVersion);

      releaseVersionDate = ConfigUtility.DATE_FORMAT.parse(releaseVersion);

      // Clear the query cache

      // Track system level information
      long startTimeOrig = System.nanoTime();

      // This is OK because every time we query the database
      // it is for an object graph we have not yet seen so flushing
      // of changes is not important until the end.
      manager.setFlushMode(FlushModeType.COMMIT);

      // Turn of id and lastModified computation when loading a terminology
      setAssignIdentifiersFlag(false);
      setLastModifiedFlag(false);
      setTransactionPerOperation(false);
      beginTransaction();

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
      
      Logger.getLogger(getClass()).info("    Loading Definitions ...");
      loadDefinitions();

      //
      // Cache subsets and members
      //
      cacheSubsetsAndMembers();

      //
      // Load language refset members
      //
      Logger.getLogger(getClass()).info("    Loading Language Ref Sets...");
      loadLanguageRefsetMembers();

      // Compute preferred names
      Logger.getLogger(getClass()).info(
          "  Compute preferred names for modified concepts");
      int ct = 0;
      for (Long id : this.pnRecomputeIds) {
        Concept concept = getConcept(id);
        String pn = getComputedPreferredName(concept);
        if (!pn.equals(concept.getName())) {
          ct++;
          concept.setName(pn);
          Logger.getLogger(getClass()).debug(
              "      compute concept pn = " + concept);
          updateConcept(concept);
        }
        if (ct % logCt == 0) {
          logAndCommit(ct);
        }
      }

      commitClearBegin();


      
      // // Load relationships - stated and inferred //
      Logger.getLogger(getClass()).info("    Loading Relationships ...");
      loadRelationships();
      
      commitClearBegin();
      
      // // Load simple refset members //
      Logger.getLogger(getClass()).info("    Loading Simple Ref Sets...");
      loadSimpleRefSetMembers();
      
      commitClearBegin();
      
      // // Load simple map refset members //
      Logger.getLogger(getClass()).info
      ("    Loading Simple Map Ref Sets..."); 
      loadSimpleMapRefSetMembers();
      
      commitClearBegin();
      
      // // Load complex map refset members //
      Logger.getLogger(getClass()).info
      ("    Loading Complex Map Ref Sets..."); 
      loadComplexMapRefSetMembers();
      
      // // Load extended map refset members //
      Logger.getLogger(getClass()).info
      ("    Loading Extended Map Ref Sets...");
      loadExtendedMapRefSetMembers();
      
      // // Load atom type refset members //
      Logger.getLogger(getClass()).info("    Loading Atom Type Ref Sets...");
      loadAtomTypeRefSetMembers();
      
      // // Load refset descriptor refset members //
      Logger.getLogger(getClass()).info(
      "    Loading Refset Descriptor Ref Sets...");
      loadRefsetDescriptorRefSetMembers();
      
      // // Load module dependency refset members //
      Logger.getLogger(getClass()).info(
      "    Loading Module Dependency Ref Sets...");
      loadModuleDependencyRefSetMembers();
      
      commitClearBegin();
      
      // // Load module dependency refset members //
      Logger.getLogger(getClass()).info(
      "    Loading Attribute Value Ref Sets...");
      loadAttributeValueRefSetMembers();
      
      commitClearBegin();
      
      // // Load association reference refset members //
      Logger.getLogger(getClass()).info(
      "    Loading Association Reference Ref Sets...");
      loadAssociationReferenceRefSetMembers();
      
      commitClearBegin();
      
      Logger.getLogger(getClass()).info("    changed = " + ct);
      
      // Commit the content changes
      Logger.getLogger(getClass()).info("  Committing");
      
      // // Create ReleaseInfo for this release if it does not already exist
       ReleaseInfo info = getReleaseInfo(terminology, releaseVersion); 
      if (info == null) {
        info = new ReleaseInfoJpa();
        info.setName(releaseVersion);
        info.setDescription(terminology + " " + releaseVersion + " release");
        info.setPlanned(false);
        info.setPublished(true);
        info.setTerminology(terminology);
        info.setVersion(version);
        info.setLastModified(releaseVersionDate);
        info.setLastModifiedBy(loader);
        addReleaseInfo(info);
      }
       
      // Commit and clear resources
      commit();
      clear();

      Logger.getLogger(getClass()).info(
          getComponentStats(terminology, version, Branch.ROOT));

      Logger.getLogger(getClass()).info(
          "      elapsed time = " + getTotalElapsedTimeStr(startTimeOrig));

      Logger.getLogger(getClass()).info("Done ...");

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

    // Cache concept ids
    Query query =
        manager.createQuery("select a.terminologyId, a.id from ConceptJpa a "
            + "where version = :version " + "and terminology = :terminology ");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    @SuppressWarnings("unchecked")
    List<Object[]> results = query.getResultList();
    for (Object[] result : results) {
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
        Concept concept =
            idMap.containsKey(fields[0]) ? getConcept(idMap.get(fields[0]))
                : null;

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
        concept2.setTerminology(terminology);
        concept2.setVersion(version);
        concept2.setName(initPrefName);
        concept2.setLastModifiedBy(loader);
        concept2.setLastModified(releaseVersionDate);
        concept2.setPublished(true);
        concept2.setPublishable(true);
        concept2.setWorkflowStatus(published);

        // Attributes
        Attribute attribute = null;
        if (concept != null) {
          attribute = concept.getAttributeByName("moduleId");
        } else {
          attribute = new AttributeJpa();          
          concept2.addAttribute(attribute);
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
          concept2.addAttribute(attribute2);
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
            Logger.getLogger(getClass()).debug(
                "      update concept - " + concept2);
            updateConcept(concept2);
            pnRecomputeIds.add(concept2.getId());
          }
          updateAttributes(concept2, concept);
          objectsUpdated++;
        }

        // Log and commit
        /*if ((objectsAdded + objectsUpdated) % logCt == 0) {
          logAndCommit(objectsAdded + objectsUpdated);
        }*/
      }
      commitClearBegin();
    }

    logAndCommit(objectsAdded + objectsUpdated);

    Logger.getLogger(getClass()).info("      new = " + objectsAdded);
    Logger.getLogger(getClass()).info("      updated = " + objectsUpdated);

  }

  /**
   * Load atoms.
   *
   * @throws Exception the exception
   */
  private void loadAtoms() throws Exception {

    // Cache description (and definition) ids
    Query query =
        manager.createQuery("select a.terminologyId, a.id from AtomJpa a "
            + "where version = :version " + "and terminology = :terminology ");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    @SuppressWarnings("unchecked")
    List<Object[]> results = query.getResultList();
    for (Object[] result : results) {
      idMap.put(result[0].toString(), Long.valueOf(result[1].toString()));
    }

    Set<Concept> modifiedConcepts = new HashSet<>();

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
        if (idMap.containsKey(fields[4])) {
          concept = getConcept(idMap.get(fields[4]));
        } else {
          // if the concept is new, it will have been added
          // if the concept is existing it will either have been udpated
          // or will be in the existing concept cache
          throw new Exception("Concept of atom should already exist: "
              + fields[4]);
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
          atom2.setObsolete(fields[2].equals("0"));
          atom2.setLanguage(fields[5]);
          atom2.setTermType(fields[6]);
          atom2.setName(fields[7]);
          atom2.setTerminology(terminology);
          atom2.setVersion(version);
          atom2.setLastModifiedBy(loader);
          atom2.setLastModified(releaseVersionDate);
          atom2.setPublished(true);
          atom2.setWorkflowStatus(published);
          atom2.setDescriptorId("");
          atom2.setCodeId("");
          atom2.setLexicalClassId("");
          atom2.setStringClassId("");
          atom2.setConceptId(concept.getTerminologyId());

          // Attributes
          Attribute attribute = null;
          if (atom != null) {
            attribute = atom.getAttributeByName("moduleId");
          } else {
            attribute = new AttributeJpa();          
            atom2.addAttribute(attribute);
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
            atom2.addAttribute(attribute2);
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
            concept.addAtom(atom2);
            modifiedConcepts.add(concept);
            objectsAdded++;
          }

          // If atom has changed, update it
          else if (!Rf2EqualityUtility.equals(atom2, atom)) {
            if (!atom.equals(atom2)) {
              Logger.getLogger(getClass())
              .debug("      update atom - " + atom2);
              updateAtom(atom2);
              concept.removeAtom(atom);
              concept.addAtom(atom2);
              modifiedConcepts.add(concept);
            }
            updateAttributes(atom2, atom);
            objectsUpdated++;
          }

          if ((objectsAdded + objectsUpdated) % logCt == 0) {
            logAndCommit(objectsAdded + objectsUpdated);
            for (Concept modifiedConcept : modifiedConcepts) {
              Logger.getLogger(getClass()).debug(
                  "      update concept - " + modifiedConcept);
              updateConcept(modifiedConcept);
              pnRecomputeIds.add(modifiedConcept.getId());
            }
            modifiedConcepts.clear();
          }

        }

        // Major error if there is a delta atom with a
        // non-existent concept
        else {
          throw new Exception("Could not find concept " + fields[4]
              + " for atom " + fields[0]);
        }
      }
    }

    /*logAndCommit(objectsAdded + objectsUpdated);*/

    commitClearBegin();
    for (Concept modifiedConcept : modifiedConcepts) {
      Logger.getLogger(getClass()).debug(
          "      update concept - " + modifiedConcept);
      updateConcept(modifiedConcept);
      pnRecomputeIds.add(modifiedConcept.getId());
    }
    modifiedConcepts.clear();

    Logger.getLogger(getClass()).info("      new = " + objectsAdded);
    Logger.getLogger(getClass()).info("      updated = " + objectsUpdated);
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
        if (idMap.containsKey(fields[4])) {
          concept = getConcept(idMap.get(fields[4]));
        } else {
          // if the concept is new, it will have been added
          // if the concept is existing it will either have been udpated
          // or will be in the existing concept cache
          throw new Exception("Concept of atom should already exist: "
              + fields[4]);
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
          def2.setTermType(fields[6]);
          def2.setName(fields[7]);
          def2.setTerminology(terminology);
          def2.setVersion(version);
          def2.setLastModifiedBy(loader);
          def2.setLastModified(releaseVersionDate);
          def2.setPublished(true);
          def2.setWorkflowStatus(published);
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
            def2.addAttribute(attribute);
          }
          setCommonFields(attribute, date);
          attribute.setName("moduleId");
          attribute.setValue(fields[3].intern());
          
          Attribute attribute2 = null;
          if (def != null) {
            attribute2 = def.getAttributeByName("caseSignificanceId");
          } else {
            attribute2 = new AttributeJpa();          
            def2.addAttribute(attribute2);
          }
          setCommonFields(attribute2, date);
          attribute2.setName("caseSignificanceId");
          attribute2.setValue(fields[8].intern());
          

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
            concept.addAtom(def2);
            modifiedConcepts.add(concept);
            objectsAdded++;
          }

          // If atom has changed, update it
          else if (!Rf2EqualityUtility.equals(def2, def)) {
            if (!def.equals(def2)) {
              Logger.getLogger(getClass()).debug(
                  "      update definition - " + def2);
              updateAtom(def2);
              concept.removeAtom(def);
              concept.addAtom(def2);
              modifiedConcepts.add(concept);
            }
            updateAttributes(def2, def);
            objectsUpdated++;
          }

          if ((objectsAdded + objectsUpdated) % logCt == 0) {
            /*logAndCommit(objectsAdded + objectsUpdated);*/
            commitClearBegin();
            for (Concept modifiedConcept : modifiedConcepts) {
              Logger.getLogger(getClass()).debug(
                  "      update concept - " + modifiedConcept);
              updateConcept(modifiedConcept);
              pnRecomputeIds.add(modifiedConcept.getId());
            }
            modifiedConcepts.clear();
          }

        }

        // Major error if there is a delta atom with a
        // non-existent concept
        else {
          throw new Exception("Could not find concept " + fields[4]
              + " for atom " + fields[0]);
        }
      }
    }

    /*logAndCommit(objectsAdded + objectsUpdated);*/
    commitClearBegin();
    
    for (Concept modifiedConcept : modifiedConcepts) {
      Logger.getLogger(getClass()).debug(
          "      update concept - " + modifiedConcept);
      updateConcept(modifiedConcept);
      pnRecomputeIds.add(modifiedConcept.getId());
    }
    modifiedConcepts.clear();

    Logger.getLogger(getClass()).info("      new = " + objectsAdded);
    Logger.getLogger(getClass()).info("      updated = " + objectsUpdated);
  }

  /**
   * Load language ref set members.
   *
   * @throws Exception the exception
   */
  private void loadLanguageRefsetMembers() throws Exception {

    // Setup variables
    String line = "";
    objectCt = 0;
    int objectsAdded = 0;
    int objectsUpdated = 0;
    Set<Atom> modifiedAtoms = new HashSet<>();

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

        // Ensure effective time is set on all appropriate objects
        AtomSubsetMember member = null;
        if (idMap.containsKey(fields[0])) {
          member =
              (AtomSubsetMember) getSubsetMember(idMap.get(fields[0]),
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

        // Add moduleId attribute
        final Date date = ConfigUtility.DATE_FORMAT.parse(fields[1]);
        /*final Attribute attribute = new AttributeJpa();
        setCommonFields(attribute, date);
        attribute.setName("acceptabilityId");
        attribute.setValue(fields[6].intern());
        cacheAttributeMetadata(attribute);
        member2.addAttribute(attribute);
        if (member != null) {
          attribute.setId(member.getAttributeByName("acceptabilityId").getId());
        }*/
        Attribute attribute = null;
        if (member != null) {
          attribute = member.getAttributeByName("acceptabilityId");
        } else {
          attribute = new AttributeJpa();          
          member2.addAttribute(attribute);
        }
        setCommonFields(attribute, date);
        attribute.setName("acceptabilityId");
        attribute.setValue(fields[6].intern());
        cacheAttributeMetadata(attribute);        

        final Atom atom = getAtom(member2.getMember().getId());

        // If language refset entry is new, add it
        if (member == null) {
          for (Attribute att : member2.getAttributes()) {
            Logger.getLogger(getClass()).debug(
                "      add attribute = " + att);
            addAttribute(att, member2);
          }
          
          Logger.getLogger(getClass()).debug(
              "      add language refset member = " + member2);
          member2 = (AtomSubsetMember) addSubsetMember(member2);
          idMap.put(member2.getTerminologyId(), member2.getId());
          atom.addMember(member2);
          modifiedAtoms.add(atom);
          objectsAdded++;
        }

        // If language refset entry is changed, update it
        else if (!member2.equals(member)
            && Rf2EqualityUtility.compareAttributes(member2, member,
                new String[] {
                    "moduleId", "acceptabilityId"
                })) {
          Logger.getLogger(getClass()).debug("  update language - " + member2);
          if (!member.equals(member2)) {
            Logger.getLogger(getClass()).debug(
                "      update langauge refset member - " + member2);
            updateSubsetMember(member2);
            atom.removeMember(member);
            atom.addMember(member2);
            modifiedAtoms.add(atom);
          }
          updateAttributes(member2, member);
          objectsUpdated++;

        }

        if ((objectsAdded + objectsUpdated) % logCt == 0) {
          logAndCommit(objectsAdded + objectsUpdated);
          for (Atom modifiedAtom : modifiedAtoms) {
            Logger.getLogger(getClass()).debug(
                "      update atom - " + modifiedAtom);
            updateAtom(modifiedAtom);
          }
          modifiedAtoms.clear();
        }

      }
    }

    logAndCommit(objectsAdded + objectsUpdated);
    for (Atom modifiedAtom : modifiedAtoms) {
      Logger.getLogger(getClass()).debug("      update atom - " + modifiedAtom);
      updateAtom(modifiedAtom);
    }
    modifiedAtoms.clear();

    Logger.getLogger(getClass()).info("      new = " + objectsAdded);
    Logger.getLogger(getClass()).info("      updated = " + objectsUpdated);

  }

  /**
   * Load simple ref set members.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings("unused")
  private void loadSimpleRefSetMembers() throws Exception {
    // n/a
  }

  /**
   * Load simple map ref set members.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings("unused")
  private void loadSimpleMapRefSetMembers() throws Exception {
    // n/a
  }

  /**
   * Load complex map ref set members.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings("unused")
  private void loadComplexMapRefSetMembers() throws Exception {
    // n/a
  }

  /**
   * Load extended map ref set members.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings("unused")
  private void loadExtendedMapRefSetMembers() throws Exception {
    // TODO: do when we have mapping objects
  }

  /**
   * Load atom type ref set members.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings("unused")
  private void loadAtomTypeRefSetMembers() throws Exception {
    // n/a
  }

  /**
   * Load refset descriptor ref set members.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings("unused")
  private void loadRefsetDescriptorRefSetMembers() throws Exception {
    // n/a
  }

  /**
   * Load module dependency ref set members.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings("unused")
  private void loadModuleDependencyRefSetMembers() throws Exception {
    // n/a
  }

  /**
   * Load attribute value ref set members.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings("unused")
  private void loadAttributeValueRefSetMembers() throws Exception {
    // n/a
  }

  /**
   * Load association reference ref set members.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings("unused")
  private void loadAssociationReferenceRefSetMembers() throws Exception {
    // n/a
  }

  /**
   * Load relationships.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings("unused")
  private void loadRelationships() throws Exception {

    // Cache description (and definition) ids
    Query query =
        manager
            .createQuery("select a.terminologyId, a.id from ConceptRelationshipJpa a "
                + "where version = :version "
                + "and terminology = :terminology ");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    @SuppressWarnings("unchecked")
    List<Object[]> results = query.getResultList();
    for (Object[] result : results) {
      idMap.put(result[0].toString(), Long.valueOf(result[1].toString()));
    }

    Set<Concept> modifiedConcepts = new HashSet<>();

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
          rel =
              (ConceptRelationship) getRelationship(idMap.get(fields[0]),
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
        rel2.setGroup(fields[6]); // relationshipGroup
        rel2.setRelationshipType(fields[7]);
        // This is SNOMED specific
        rel2.setStated(fields[8].equals("900000000000010007"));
        rel2.setInferred(fields[8].equals("900000000000011006"));

        rel2.setTerminology(terminology);
        rel2.setVersion(version);
        rel2.setFrom(sourceConcept);
        rel2.setTo(destinationConcept);
        rel2.setLastModifiedBy(loader);
        rel2.setLastModified(releaseVersionDate);
        rel2.setPublished(true);

        // Attributes
        Attribute attribute = null;
        if (rel != null) {
          attribute = rel.getAttributeByName("moduleId");
        } else {
          attribute = new AttributeJpa();          
          rel2.addAttribute(attribute);
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
          rel2.addAttribute(attribute2);
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
          rel2.addAttribute(attribute3);
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
          sourceConcept.addRelationship(rel2);
          modifiedConcepts.add(sourceConcept);
          objectsAdded++;
        }

        // If atom has changed, update it
        else if (!Rf2EqualityUtility.equals(rel2, rel)) {
          if (!rel.equals(rel2)) {
            Logger.getLogger(getClass()).debug("      update rel - " + rel2);
            updateRelationship(rel2);
            sourceConcept.removeRelationship(rel);
            sourceConcept.addRelationship(rel);
            modifiedConcepts.add(sourceConcept);
          }
          updateAttributes(rel2, rel);
          objectsUpdated++;
        }

        if ((objectsAdded + objectsUpdated) % logCt == 0) {
          logAndCommit(objectsAdded + objectsUpdated);
          for (Concept modifiedConcept : modifiedConcepts) {
            Logger.getLogger(getClass()).debug(
                "      update concept - " + modifiedConcept);
            updateConcept(modifiedConcept);
          }
          modifiedConcepts.clear();
        }

      }
    }

    logAndCommit(objectsAdded + objectsUpdated);
    for (Concept modifiedConcept : modifiedConcepts) {
      Logger.getLogger(getClass()).debug(
          "      update concept - " + modifiedConcept);
      updateConcept(modifiedConcept);
    }
    modifiedConcepts.clear();

    Logger.getLogger(getClass()).info("      new = " + objectsAdded);
    Logger.getLogger(getClass()).info("      updated = " + objectsUpdated);

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
    for (Attribute a1 : c1.getAttributes()) {
      Attribute a2 = c2.getAttributeByName(a1.getName());
      if (a2 != null) {
        if (!a1.equals(a2)) {
          Logger.getLogger(getClass()).debug("      update attribute - " + a1);
          updateAttribute(a1, c1);
        } 
      } else {
        throw new Exception("Unexpected mismatching attribute: " + a1.getName());
      }
    }
    // TODO: determine if c2 has anything not in c1
  }

  /**
   * Cache subsets and members.
   *
   * @throws Exception the exception
   */
  private void cacheSubsetsAndMembers() throws Exception {
    // Cache existing subsets
    Query query =
        manager.createQuery("select a from AtomSubsetJpa a "
            + "where a.version = :version " + "and a.terminology = :terminology ");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    @SuppressWarnings("unchecked")
    List<AtomSubset> results = query.getResultList();
    for (AtomSubset result : results) {
      atomSubsetMap.put(result.getTerminologyId(), result);
    }

    query =
        manager.createQuery("select a from ConceptSubsetJpa a "
            + "where a.version = :version " + "and a.terminology = :terminology ");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    @SuppressWarnings("unchecked")
    List<ConceptSubset> results2 = query.getResultList();
    for (ConceptSubset result : results2) {
      conceptSubsetMap.put(result.getTerminologyId(), result);
    }

    // Cache subset members
    query =
        manager
            .createQuery("select a.terminologyId, a.id from AtomSubsetMemberJpa a "
                + "where a.version = :version "
                + "and a.terminology = :terminology ");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    @SuppressWarnings("unchecked")
    List<Object[]> results3 = query.getResultList();
    for (Object[] result : results3) {
      idMap.put(result[0].toString(), Long.valueOf(result[1].toString()));
    }

    query =
        manager
            .createQuery("select a.terminologyId, a.id from ConceptSubsetMemberJpa a "
                + "where a.version = :version "
                + "and a.terminology = :terminology ");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
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
      // Retrieve concept -- firstToken is referencedComponentId
      final Concept concept = getConcept(idMap.get(fields[5]));
      if (concept != null)
        ((ConceptSubsetMember) member).setMember(concept);
      
      final Atom description = getAtom(idMap.get(fields[5]));
      if (description != null)
        ((AtomSubsetMember) member).setMember(description);
    } else {
      throw new Exception(
          "Refset member connected to nonexistent object");
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

    manageSubset(member, fields, date);
    
    // Add moduleId attribute
    /*final Attribute attribute = new AttributeJpa();
    setCommonFields(attribute, date);
    attribute.setName("moduleId");
    attribute.setValue(fields[3].intern());
    cacheAttributeMetadata(attribute);
    member.addAttribute(attribute);
    if (member.getAttributeByName("moduleId") != null) {
      attribute.setId(member.getAttributeByName("moduleId").getId());
    }*/

    Attribute attribute = null;
    if (member.getAttributeByName("moduleId") != null) {
      attribute = member.getAttributeByName("moduleId");
    } else {
      attribute = new AttributeJpa();          
      member.addAttribute(attribute);
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
      subset.setName(getConcept(idMap.get(fields[4])).getName());
      subset.setDescription(subset.getName());

      final Attribute attribute = new AttributeJpa();
      setCommonFields(attribute, date);
      attribute.setName("moduleId");
      attribute.setValue(fields[3].intern());
      subset.addAttribute(attribute);
      addAttribute(attribute, member);
      cacheAttributeMetadata(attribute);
      
      addSubset(subset);
      atomSubsetMap.put(fields[4], subset);
      commitClearBegin();

      ((AtomSubsetMember) member).setSubset(subset);

    } else if (member instanceof ConceptSubsetMember
        && !conceptSubsetMap.containsKey(fields[4])) {

      final ConceptSubset subset = new ConceptSubsetJpa();
      setCommonFields(subset, date);
      subset.setTerminologyId(fields[4].intern());
      subset.setName(getConcept(idMap.get(fields[4])).getName());
      subset.setDescription(subset.getName());
      subset.setDisjointSubset(false);

      final Attribute attribute = new AttributeJpa();
      setCommonFields(attribute, date);
      attribute.setName("moduleId");
      attribute.setValue(fields[3].intern());
      subset.addAttribute(attribute);
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
    component.setTerminology(terminology);
    component.setVersion(version);

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
