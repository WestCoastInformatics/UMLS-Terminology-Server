/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.maint;

import static java.lang.Math.toIntExact;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.Query;

import com.wci.umls.server.AlgorithmParameter;
import com.wci.umls.server.Project;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.ChecklistList;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.FieldedStringTokenizer;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.helpers.QueryType;
import com.wci.umls.server.helpers.meta.TerminologyList;
import com.wci.umls.server.jpa.AlgorithmParameterJpa;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.AbstractInsertMaintReleaseAlgorithm;
import com.wci.umls.server.jpa.algo.action.AddAtomMolecularAction;
import com.wci.umls.server.jpa.algo.action.AddSemanticTypeMolecularAction;
import com.wci.umls.server.jpa.algo.action.RedoMolecularAction;
import com.wci.umls.server.jpa.algo.action.RemoveSemanticTypeMolecularAction;
import com.wci.umls.server.jpa.algo.action.UndoMolecularAction;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.AtomRelationshipJpa;
import com.wci.umls.server.jpa.content.AtomSubsetJpa;
import com.wci.umls.server.jpa.content.AtomSubsetMemberJpa;
import com.wci.umls.server.jpa.content.AtomTreePositionJpa;
import com.wci.umls.server.jpa.content.ComponentHistoryJpa;
import com.wci.umls.server.jpa.content.ComponentInfoRelationshipJpa;
import com.wci.umls.server.jpa.content.ConceptRelationshipJpa;
import com.wci.umls.server.jpa.content.ConceptSubsetJpa;
import com.wci.umls.server.jpa.content.ConceptSubsetMemberJpa;
import com.wci.umls.server.jpa.content.SemanticTypeComponentJpa;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.inversion.SourceIdRangeJpa;
import com.wci.umls.server.jpa.meta.AdditionalRelationshipTypeJpa;
import com.wci.umls.server.jpa.services.InversionServiceJpa;
import com.wci.umls.server.jpa.services.UmlsIdentityServiceJpa;
import com.wci.umls.server.jpa.services.WorkflowServiceJpa;
import com.wci.umls.server.jpa.workflow.ChecklistJpa;
import com.wci.umls.server.jpa.workflow.TrackingRecordJpa;
import com.wci.umls.server.model.actions.MolecularAction;
import com.wci.umls.server.model.actions.MolecularActionList;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.AtomRelationship;
import com.wci.umls.server.model.content.AtomSubsetMember;
import com.wci.umls.server.model.content.AtomTreePosition;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.CodeTreePosition;
import com.wci.umls.server.model.content.ComponentHistory;
import com.wci.umls.server.model.content.ComponentInfoRelationship;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.content.ConceptSubsetMember;
import com.wci.umls.server.model.content.ConceptTreePosition;
import com.wci.umls.server.model.content.Definition;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.DescriptorTreePosition;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.model.content.SemanticTypeComponent;
import com.wci.umls.server.model.inversion.SourceIdRange;
import com.wci.umls.server.model.meta.AdditionalRelationshipType;
import com.wci.umls.server.model.meta.RelationshipIdentity;
import com.wci.umls.server.model.meta.RelationshipType;
import com.wci.umls.server.model.meta.RootTerminology;
import com.wci.umls.server.model.meta.Terminology;
import com.wci.umls.server.model.workflow.Checklist;
import com.wci.umls.server.model.workflow.TrackingRecord;
import com.wci.umls.server.model.workflow.WorkflowStatus;
import com.wci.umls.server.model.workflow.Worklist;
import com.wci.umls.server.services.InversionService;
import com.wci.umls.server.services.UmlsIdentityService;
import com.wci.umls.server.services.WorkflowService;
import com.wci.umls.server.services.handlers.IdentifierAssignmentHandler;

/**
 * Implementation of an algorithm to execute an action based on a user-defined
 * query.
 */
public class AdHocAlgorithm extends AbstractInsertMaintReleaseAlgorithm {

  /** The actionName. */
  private String actionName;

  /**
   * Instantiates an empty {@link AdHocAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public AdHocAlgorithm() throws Exception {
    super();
    setActivityId(UUID.randomUUID().toString());
    setWorkId("QUERYACTION");
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
      throw new Exception("Ad Hoc algorithms requires a project to be set");
    }

    final String srcFullPath =
        ConfigUtility.getConfigProperties().getProperty("source.data.dir")
            + File.separator + getProcess().getInputPath();

    setSrcDirFile(new File(srcFullPath));

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
    logInfo("Starting " + getName());
    logInfo("  actionName = " + actionName);

    // No Molecular actions will be generated by this algorithm
    setMolecularActionFlag(false);

    commitClearBegin();

    // Add all ad hoc actions to if-statement chain.
    if (actionName.equals("Fix Orphan Definitions")) {
      fixOrphanDefinitions();
    } else if (actionName.equals("Undo Stampings")) {
      undoStampings();
    } else if (actionName.equals("Remove Bad Relationships")) {
      removeBadRelationships();
    } else if (actionName.equals("Remove SNOMED Subsets")) {
      removeSNOMEDSubsets();
    } else if (actionName.equals("Remove SNOMED Atom Subsets")) {
      removeSNOMEDAtomSubsets();
    } else if (actionName.equals("Remove Concepts without Atoms")) {
      removeConceptsWithoutAtoms();
    } else if (actionName.equals("Remove Orphaned Tracking Records")) {
      removeOrphanedTrackingRecords();
    } else if (actionName.equals("Inactivate Old SRC atoms and AtomRels")) {
      inactivateOldSRCContent();
    } else if (actionName.equals("Fix SRC_ATOM_IDs")) {
      fixScrAtomIds();
    } else if (actionName.equals("Redo Molecular Actions")) {
      redoMolecularActions();
    } else if (actionName.equals("Fix Bad Relationship Identities")) {
      fixBadRelationshipIdentities();
    } else if (actionName.equals("Fix Component Info Relationships")) {
      fixComponentInfoRelationships();
    } else if (actionName
        .equals("Set Component Info Relationships To Publishable")) {
      setComponentInfoRelationshipsToPublishable();
    } else if (actionName
        .equals("Set Stamped Worklists To Ready For Publication")) {
      setStampedWorklistsToReadyForPublication();
    } else if (actionName.equals("Add Disposition Atoms")) {
      addDispositionAtoms();
    } else if (actionName.equals("Fix RelGroups")) {
      fixRelGroups();
    } else if (actionName.equals("Fix Source Level Rels")) {
      fixSourceLevelRels();
    } else if (actionName.equals("Fix AdditionalRelType Inverses")) {
      fixAdditionalRelTypeInverses();
    } else if (actionName.equals("Fix Snomed Family")) {
      fixSnomedFamily();
    } else if (actionName.equals("Turn off CTRP-SDC")) {
      turnOffCTRPSDC();
    } else if (actionName.equals("Fix Terminology Names")) {
      fixTerminologyNames();
    } else if (actionName.equals("Fix Terminologies")) {
      fixTerminologies();
    } else if (actionName.equals("Fix RHT Atoms")) {
      fixRHTAtoms();
    } else if (actionName.equals("Fix MDR Descriptors")) {
      fixMDRDescriptors();
    } else if (actionName.equals("Clear Worklists and Checklists")) {
      removeOldWorklistsChecklists();
    } else if (actionName.equals("Fix Duplicate PDQ Mapping Attributes")) {
      fixDuplicatePDQMappingAttributes();
    } else if (actionName.equals("Fix Duplicate Concepts")) {
      fixDuplicateConcepts();
    } else if (actionName.equals("Fix Null RUIs")) {
      fixNullRUIs();
    } else if (actionName.equals("Remove old MTH relationships")) {
      removeOldMTHRelationships();
    } else if (actionName.equals("Remove old relationships")) {
      removeOldRelationships();
    } else if (actionName.equals("Assign Missing STY ATUIs")) {
      assignMissingStyAtui();
    } else if (actionName.equals("Fix Component History Version")) {
      fixComponentHistoryVersion();
    } else if (actionName.equals("Fix AdditionalRelType Inverses 2")) {
      fixAdditionalRelTypeInverses2();
    } else if (actionName.equals("Fix AdditionalRelType Inverses 3")) {
      fixAdditionalRelTypeInverses3();
    } else if (actionName.equals("Remove Demotions")) {
      removeDemotions();
    } else if (actionName.equals("Revise Semantic Types")) {
      reviseSemanticTypes();
    } else if (actionName.equals("Fix Atom Last Release CUI")) {
      fixAtomLastReleaseCui();
    } else if (actionName.equals("Fix VPT and Terminologies")) {
      fixVPTAndTerminologies();
    } else if (actionName.equals("Fix Atom Suppressible and Obsolete")) {
      fixAtomSuppressibleAndObsolete();
    } else if (actionName.equals("Change null treeposition Relas to blank")) {
      changeNullTreePositionRelasToBlank();
    } else if (actionName.equals("Initialize Source Atom Id Range App")) {
      initializeSourceAtomIdRanges();
    } else if (actionName.equals("Remove Deprecated Termgroups")) {
      removeOldTermgroups();
    } else if (actionName.equals("Fix overlapping bequeathal rels")) {
      fixOverlappingBRORels();
    } else if (actionName.equals("Fix NCBI VPT atom")) {
      fixNCBIVPT();
    } else if (actionName.equals("Inactivate old tree positions")) {
      inactivateOldTreePositions();
    } else {
      throw new Exception("Valid Action Name not specified.");
    }

    
    
    commitClearBegin();

    logInfo("  project = " + getProject().getId());
    logInfo("  workId = " + getWorkId());
    logInfo("  activityId = " + getActivityId());
    logInfo("  user  = " + getLastModifiedBy());
    logInfo("Finished " + getName());

  }

  private void undoStampings() throws Exception {
    // 11/29/2017 - A Stamping action was run on a checklist of 14,000 concepts,
    // and it was decided they didn't want them to be stamped after all. Based
    // on the activityId: chk_sct_new_approve, identify all molecular actions
    // and undo them

    int successful = 0;
    int unsuccessful = 0;
    final String activityId = "chk_sct_new_approve";

    // Find all molecular actions associated with the activityId
    final PfsParameter pfs = new PfsParameterJpa();
    pfs.setAscending(false);
    pfs.setSortField("timestamp");
    final MolecularActionList molecularActions =
        findMolecularActions(null, getProject().getTerminology(),
            getProject().getVersion(), "activityId:" + activityId, pfs);

    for (final MolecularAction molecularAction : molecularActions
        .getObjects()) {

      if (molecularAction.isUndoneFlag()) {
        logInfo("Already undone: molecularAction=" + molecularAction.getId()
            + ", for concept=" + molecularAction.getComponentId());
        successful++;
      }

      else {
        // Create and set up an undo action
        final UndoMolecularAction undoAction = new UndoMolecularAction();

        try {
          // Configure and run the undo action
          undoAction.setProject(getProject());
          undoAction.setActivityId(molecularAction.getActivityId());
          undoAction.setConceptId(molecularAction.getComponentId());
          undoAction.setConceptId2(molecularAction.getComponentId2());
          undoAction.setLastModifiedBy(molecularAction.getLastModifiedBy());
          undoAction.setTransactionPerOperation(false);
          undoAction.setMolecularActionFlag(false);
          undoAction.setChangeStatusFlag(true);
          undoAction.setMolecularActionId(molecularAction.getId());
          undoAction.setForce(true);
          undoAction.performMolecularAction(undoAction, getLastModifiedBy(),
              false, false);

          logInfo(
              "Successful undo for molecularAction=" + molecularAction.getId()
                  + ", for concept=" + molecularAction.getComponentId());
          successful++;
        } catch (Exception e) {
          logInfo("Could not undo molecularAction=" + molecularAction.getId()
              + ", for concept=" + molecularAction.getComponentId());
          unsuccessful++;
        } finally {
          undoAction.close();
        }
      }

      logAndCommit(unsuccessful + successful, 100, 100);

    }

    logInfo(
        "[UndoStampings] " + successful + " stampings successfully undone.");
    logInfo("[UndoStampings] " + unsuccessful + " stampings unable to undo.");

  }

  private void fixOrphanDefinitions() throws Exception {
    // 11/14/2017 - Bug in SplitMolecularAction found where definitions weren't
    // being copied over with atoms when split out, resulting in orphaned
    // definitions.
    // Load these definitions and re-add them to the appropriate atom.

    int successful = 0;

    final Map<Long, Long> definitionIdAtomIdMap = new HashMap<>();
    definitionIdAtomIdMap.put(37014L, 338961L);
    definitionIdAtomIdMap.put(275324L, 6783080L);
    definitionIdAtomIdMap.put(275326L, 6783082L);
    definitionIdAtomIdMap.put(275327L, 6783083L);
    definitionIdAtomIdMap.put(275328L, 6783084L);
    definitionIdAtomIdMap.put(275329L, 6783085L);
    definitionIdAtomIdMap.put(275330L, 6783086L);
    definitionIdAtomIdMap.put(275333L, 6783089L);
    definitionIdAtomIdMap.put(275562L, 6783318L);
    definitionIdAtomIdMap.put(275825L, 6783578L);
    definitionIdAtomIdMap.put(327827L, 6815915L);
    definitionIdAtomIdMap.put(327871L, 6815960L);
    definitionIdAtomIdMap.put(327938L, 6816027L);
    definitionIdAtomIdMap.put(327972L, 6816061L);
    definitionIdAtomIdMap.put(327975L, 6816064L);
    definitionIdAtomIdMap.put(328045L, 6816138L);
    definitionIdAtomIdMap.put(328111L, 6816205L);
    definitionIdAtomIdMap.put(362093L, 6854283L);
    definitionIdAtomIdMap.put(362094L, 6851063L);
    definitionIdAtomIdMap.put(362125L, 6854335L);
    definitionIdAtomIdMap.put(362126L, 6851079L);
    definitionIdAtomIdMap.put(362199L, 6854454L);
    definitionIdAtomIdMap.put(362200L, 6851116L);
    definitionIdAtomIdMap.put(5322L, 49047L);
    definitionIdAtomIdMap.put(40739L, 470079L);
    definitionIdAtomIdMap.put(75071L, 1446086L);
    definitionIdAtomIdMap.put(50708L, 685841L);

    for (Map.Entry<Long, Long> entry : definitionIdAtomIdMap.entrySet()) {
      final Long definitionId = entry.getKey();
      final Long atomId = entry.getValue();

      final Atom atom = getAtom(atomId);
      if (atom == null) {
        logWarn("Could not find atom with id=" + atomId);
        continue;
      }

      final Definition definition = getDefinition(definitionId);
      if (definition == null) {
        logWarn("Could not find definition with id=" + definitionId);
        continue;
      }

      if (atom.getDefinitions().contains(definition)) {
        logWarn(
            "atom=" + atomId + " already contains definition=" + definitionId);
        continue;
      }

      atom.getDefinitions().add(definition);
      updateAtom(atom);
      successful++;
    }

    logInfo("[FixOrphanDefinitions] " + successful
        + " orphan definitions successfully reattached.");

  }

  private void removeBadRelationships() throws Exception {
    // 3/1/2018 Bug in RelationshipLoader during a UMLS insertion
    // created multiple relationships between concepts.
    // It also created a number of self-referential relationships.
    // Identify and remove them.
    // 11/13/2018 Same issue happened again with MTH2018AA insertion. Updating
    // version.
    // 12/15/2018 Sigh - same thing happened AGAIN with MTH2018AB.

    int removals = 0;

    Set<Long> relIds = new HashSet<>();
    Set<Long> alreadyRemovedRelIds = new HashSet<>();
    Set<String> seenRelIdPairs = new HashSet<>();

    // Get self-referential relationships
    Query query = getEntityManager().createQuery("select a.id from "
        + "ConceptRelationshipJpa a "
        + "where a.terminology = :terminology and a.version = :version and a.publishable=true");
    query.setParameter("terminology", "MTH");
    query.setParameter("version", "2018AB");

    logInfo("[RemoveBadRelationships] Loading "
        + "ConceptRelationship ids for relationships created by the MTH 2017AB insertion");

    List<Object> list = query.getResultList();
    for (final Object entry : list) {
      final Long id = Long.valueOf(entry.toString());
      relIds.add(id);
    }

    setSteps(relIds.size());

    logInfo("[RemoveBadRelationships] " + relIds.size()
        + " ConceptRelationship ids loaded");

    for (Long id : relIds) {
      final ConceptRelationship rel =
          (ConceptRelationshipJpa) getRelationship(id,
              ConceptRelationshipJpa.class);

      if (alreadyRemovedRelIds.contains(id)) {
        updateProgress();
        continue;
      }

      if (rel == null) {
        logWarn("Could not find concept relationship with id=" + id);
        updateProgress();
        continue;
      }

      // If this is a self-referential relationship, remove Only it
      // Its inverse will be removed by a later run
      if (rel.getFrom().getId().equals(rel.getTo().getId())) {
        logInfo(
            "[RemoveBadRelationships] Removing self-referential relationship: "
                + rel.getId());
        removeRelationship(id, ConceptRelationshipJpa.class);
        removals++;
      }

      // If this the concept-pair has been seen, remove this relationship and
      // its inverse
      else if (seenRelIdPairs
          .contains(rel.getFrom().getId() + "|" + rel.getTo().getId())) {
        logInfo("[RemoveBadRelationships] Removing overlapping relationship: "
            + rel.getId());
        removeRelationship(id, ConceptRelationshipJpa.class);
        alreadyRemovedRelIds.add(id);
        removals++;

        ConceptRelationship inverseRel = null;
        try {
          inverseRel = (ConceptRelationshipJpa) getInverseRelationship(
              getProject().getTerminology(), getProject().getVersion(), rel);
        } catch (Exception e) {
          logInfo(
              "[RemoveBadRelationships] Could not find inverse relationship for: "
                  + rel.getId());
        }

        if (inverseRel != null) {
          logInfo(
              "[RemoveBadRelationships] Removing overlapping inverse relationship: "
                  + inverseRel.getId());
          removeRelationship(inverseRel.getId(), ConceptRelationshipJpa.class);
          alreadyRemovedRelIds.add(inverseRel.getId());
          removals++;
        }
      }

      // Otherwise, log this concept-pair as seen.
      else {
        seenRelIdPairs.add(rel.getFrom().getId() + "|" + rel.getTo().getId());
      }

      updateProgress();

    }

    logInfo("[RemoveBadRelationships] " + removals
        + " bad relationships successfully removed.");

  }

  private void removeOrphanedTrackingRecords() throws Exception {
    // 3/5/2018 Bug identified where tracking records exist that are not
    // associated with any bin, worklist, or checklist.
    // Get rid of them.

    int removals = 0;

    Set<Long> trackingRecordIds = new HashSet<>();

    // Get self-referential relationships
    Query query = getEntityManager().createNativeQuery(
        "select tr.id from tracking_records tr left join checklists_tracking_records ctr on tr.id=ctr.trackingRecords_id left join worklists_tracking_records wtr on tr.id=wtr.trackingRecords_id left join workflow_bins_tracking_records wbtr on tr.id=wbtr.trackingRecords_id where ctr.trackingRecords_id is null and wtr.trackingRecords_id is null and wbtr.trackingRecords_id is null");

    logInfo("[RemoveOrphanedTrackingRecords] Loading "
        + "TrackingRecord ids for orphaned tracking records");

    List<Object> list = query.getResultList();
    for (final Object entry : list) {
      final Long id = Long.valueOf(entry.toString());
      trackingRecordIds.add(id);
    }

    setSteps(trackingRecordIds.size());

    logInfo("[RemoveOrphanedTrackingRecords] " + trackingRecordIds.size()
        + " Orphaned TrackingRecord ids loaded");

    for (Long id : trackingRecordIds) {

      removeTrackingRecord(id);
      updateProgress();

    }

    logInfo("[RemoveOrphanedTrackingRecords] " + removals
        + " orphaned tracking records successfully removed.");

  }

  private void inactivateOldSRCContent() throws Exception {
    // 3/7/2018 Bug identified where old SRC atoms and AtomRelationships were
    // not getting caught by UpdateReleasibility.
    // Set them to publishable = false.

    try {

      logInfo("  Making all old version content unpublishable");

      // Mark all non-current SRC atoms as unpublishable.
      String query = "SELECT a.id " + "FROM AtomJpa a, TerminologyJpa t "
          + "WHERE a.terminology='SRC' AND a.publishable=true AND t.current = false AND a.codeId=CONCAT('V-',t.terminology,'_',t.version)";

      // Perform a QueryActionAlgorithm using the class and query
      QueryActionAlgorithm queryAction = new QueryActionAlgorithm();
      try {
        queryAction.setLastModifiedBy(getLastModifiedBy());
        queryAction.setLastModifiedFlag(isLastModifiedFlag());
        queryAction.setProcess(getProcess());
        queryAction.setProject(getProject());
        queryAction.setTerminology(getTerminology());
        queryAction.setVersion(getVersion());
        queryAction.setWorkId(getWorkId());
        queryAction.setActivityId(getActivityId());

        queryAction.setObjectTypeClass(AtomJpa.class);
        queryAction.setAction("Make Unpublishable");
        queryAction.setQueryType(QueryType.JPQL);
        queryAction.setQuery(query);

        queryAction.setTransactionPerOperation(false);
        queryAction.beginTransaction();

        //
        // Check prerequisites
        //
        ValidationResult validationResult = queryAction.checkPreconditions();
        // if prerequisites fail, return validation result
        if (!validationResult.getErrors().isEmpty()
            || (!validationResult.getWarnings().isEmpty())) {
          // rollback -- unlocks the concept and closes transaction
          queryAction.rollback();
        }
        assertTrue(validationResult.getErrors().isEmpty());

        //
        // Perform the algorithm
        //
        queryAction.compute();

        // Commit the algorithm.
        queryAction.commit();

      } catch (Exception e) {
        queryAction.rollback();
        e.printStackTrace();
        fail("Unexpected exception thrown - please review stack trace.");
      } finally {
        // Close algorithm for each loop
        queryAction.close();
      }

      // Mark all SRC-owned atom relationships to unpublishable, if either of
      // their atoms are unpublishable.
      query = "SELECT a.id " + "FROM AtomRelationshipJpa a "
          + "WHERE a.terminology='SRC' AND a.publishable=true AND (a.from.publishable=false OR a.to.publishable=false)";

      // Perform a QueryActionAlgorithm using the class and query
      queryAction = new QueryActionAlgorithm();
      try {
        queryAction.setLastModifiedBy(getLastModifiedBy());
        queryAction.setLastModifiedFlag(isLastModifiedFlag());
        queryAction.setProcess(getProcess());
        queryAction.setProject(getProject());
        queryAction.setTerminology(getTerminology());
        queryAction.setVersion(getVersion());
        queryAction.setWorkId(getWorkId());
        queryAction.setActivityId(getActivityId());

        queryAction.setObjectTypeClass(AtomRelationshipJpa.class);
        queryAction.setAction("Make Unpublishable");
        queryAction.setQueryType(QueryType.JPQL);
        queryAction.setQuery(query);

        queryAction.setTransactionPerOperation(false);
        queryAction.beginTransaction();

        //
        // Check prerequisites
        //
        ValidationResult validationResult = queryAction.checkPreconditions();
        // if prerequisites fail, return validation result
        if (!validationResult.getErrors().isEmpty()
            || (!validationResult.getWarnings().isEmpty())) {
          // rollback -- unlocks the concept and closes transaction
          queryAction.rollback();
        }
        assertTrue(validationResult.getErrors().isEmpty());

        //
        // Perform the algorithm
        //
        queryAction.compute();

        // Commit the algorithm.
        queryAction.commit();

      } catch (Exception e) {
        queryAction.rollback();
        e.printStackTrace();
        fail("Unexpected exception thrown - please review stack trace.");
      } finally {
        // Close algorithm for each loop
        queryAction.close();
      }

      logInfo("Finished " + getName());

    } catch (

    Exception e) {
      logError("Unexpected problem - " + e.getMessage());
      throw e;
    }

  }

  private void fixScrAtomIds() throws Exception {
    // 3/26/2018 Bug identified where atom alternate Ids were imported
    // incorrectly from MEME4
    // Use identified list to fix and update

    logInfo(" Fix incorrect atom alternate terminology Ids");

    int successful = 0;

    // Find atoms based on old NCIMTH-SRC alternateTerminologyIds
    final String queryBase =
        "select a.id from AtomJpa a join a.alternateTerminologyIds b where ( KEY(b)  = 'NCIMTH-SRC' and b =";

    final Map<String, String> oldIdToNewIdMap = new HashMap<>();

    // Test data set
    // oldIdToNewIdMap.put("258167703","999999999");

    // Real data set
    oldIdToNewIdMap.put("257175511", "261357780");
    oldIdToNewIdMap.put("238431363", "259982165");
    oldIdToNewIdMap.put("238431363", "259982165");
    oldIdToNewIdMap.put("251796731", "258986997");
    oldIdToNewIdMap.put("251796726", "258986991");
    oldIdToNewIdMap.put("251796727", "258986992");
    oldIdToNewIdMap.put("254102219", "261309548");
    oldIdToNewIdMap.put("257269628", "261896942");
    oldIdToNewIdMap.put("257269626", "261896940");
    oldIdToNewIdMap.put("256903433", "260700556");
    oldIdToNewIdMap.put("256629133", "260214361");
    oldIdToNewIdMap.put("256629132", "260214360");
    oldIdToNewIdMap.put("256629133", "260214361");
    oldIdToNewIdMap.put("256629132", "260214360");
    oldIdToNewIdMap.put("256080093", "258986711");
    oldIdToNewIdMap.put("255240539", "257847398");
    oldIdToNewIdMap.put("255240538", "257847397");
    oldIdToNewIdMap.put("255670330", "258297786");
    oldIdToNewIdMap.put("255670328", "258297784");
    oldIdToNewIdMap.put("256328534", "259642255");
    oldIdToNewIdMap.put("256328532", "259642253");
    oldIdToNewIdMap.put("256328533", "259642254");
    oldIdToNewIdMap.put("251627436", "258814569");
    oldIdToNewIdMap.put("251627434", "258814567");
    oldIdToNewIdMap.put("251627431", "258814564");
    oldIdToNewIdMap.put("256174404", "259226265");
    oldIdToNewIdMap.put("256174402", "259226263");
    oldIdToNewIdMap.put("256174400", "259226261");
    oldIdToNewIdMap.put("256174401", "259226262");
    oldIdToNewIdMap.put("257222505", "261668814");
    oldIdToNewIdMap.put("257222508", "261668817");
    oldIdToNewIdMap.put("257222507", "261668816");
    oldIdToNewIdMap.put("257222506", "261668815");
    oldIdToNewIdMap.put("251553070", "258741488");
    oldIdToNewIdMap.put("251553071", "258741489");
    oldIdToNewIdMap.put("251553072", "258741490");
    oldIdToNewIdMap.put("256082432", "258987136");
    oldIdToNewIdMap.put("256082430", "258987134");
    oldIdToNewIdMap.put("239859246", "261335544");
    oldIdToNewIdMap.put("239859242", "261335540");
    oldIdToNewIdMap.put("239859244", "261335542");
    oldIdToNewIdMap.put("256082438", "258987142");
    oldIdToNewIdMap.put("256082433", "258987137");
    oldIdToNewIdMap.put("256082436", "258987140");

    for (Map.Entry<String, String> entry : oldIdToNewIdMap.entrySet()) {
      final String oldId = entry.getKey();
      final String newId = entry.getValue();

      final String fullQuery = queryBase + " '" + oldId + "')";

      final Query query = getEntityManager().createQuery(fullQuery);

      final Long atomId = Long.valueOf(query.getSingleResult().toString());

      final Atom atom = getAtom(atomId);
      if (atom == null) {
        logWarn("Could not find atom with alternate terminology id=" + oldId);
        continue;
      }

      // Replace old alternateTerminologyId with new alternateTerminologyId
      atom.getAlternateTerminologyIds().put("NCIMTH-SRC", newId);

      updateAtom(atom);
      successful++;
    }

    logInfo(
        "[FixSrcAtomIds] " + successful + " SRC atom Id successfully updated.");
  }

  private void redoMolecularActions() throws Exception {
    // 4/2/2018 A bunch of molecular actions were undone during a failed
    // generated merge reset. Need to re-do them before the reset can continue

    try {

      logInfo("  Redoing molecular actions");

      List<Long> molecularActionIds = new ArrayList<>(Arrays.asList(3204914L,
          3204915L, 3204916L, 3204917L, 3204918L, 3204919L, 3204920L, 3204921L,
          3204922L, 3204923L, 3204924L, 3204925L, 3204926L, 3204927L, 3204928L,
          3204929L, 3204930L, 3204931L, 3204932L, 3204933L, 3204934L, 3204935L,
          3204936L, 3204937L, 3204938L, 3204939L, 3204940L, 3204941L, 3204942L,
          3204943L, 3204944L, 3204945L, 3204946L, 3204947L, 3204948L, 3204949L,
          3204950L, 3205251L, 3205252L, 3205253L, 3205254L, 3205255L, 3205256L,
          3205257L, 3205258L, 3205259L, 3205260L, 3205261L, 3205262L, 3205263L,
          3205264L, 3205265L, 3205266L, 3205267L, 3205268L, 3205269L, 3205270L,
          3205271L, 3205272L, 3205273L, 3205274L, 3205276L, 3205277L, 3205278L,
          3205279L, 3205280L, 3205281L, 3205282L, 3205283L, 3205284L, 3205285L,
          3205286L, 3205288L, 3205289L, 3205290L, 3205291L, 3205292L, 3205293L,
          3205294L, 3205295L, 3205296L, 3205297L, 3205299L, 3205300L, 3205601L,
          3205602L, 3205603L, 3205604L, 3205606L, 3205607L, 3205608L, 3205609L,
          3205610L, 3205611L, 3205612L, 3205613L, 3205614L, 3205615L, 3205616L,
          3205617L, 3205618L, 3205619L, 3205620L, 3205621L, 3205622L, 3205623L,
          3205624L, 3205625L, 3205626L, 3205627L, 3205628L, 3205629L, 3205631L,
          3205632L, 3205634L, 3205635L, 3205636L, 3205637L, 3205639L, 3205640L,
          3205641L, 3205642L, 3205643L, 3205644L, 3205645L, 3205646L, 3205647L,
          3205648L, 3205649L, 3205650L, 3205901L, 3205902L, 3205903L, 3205904L,
          3205905L, 3205906L, 3205907L, 3205908L, 3205909L, 3205910L, 3205911L,
          3205912L, 3205913L, 3205914L, 3205915L, 3205916L, 3205917L, 3205918L,
          3205919L, 3205920L, 3205921L, 3205922L, 3205923L, 3205924L, 3205925L,
          3205926L, 3205927L, 3205928L, 3205929L, 3205930L, 3205931L, 3205932L,
          3205933L, 3205934L, 3205935L, 3205936L, 3205937L, 3205938L, 3205939L,
          3205940L, 3205941L, 3205942L, 3205943L, 3205944L, 3205945L, 3205946L,
          3205947L, 3205948L, 3205949L, 3205950L, 3206302L, 3206303L, 3206304L,
          3206305L, 3206306L, 3206307L, 3206308L, 3206309L, 3206310L, 3206311L,
          3206312L, 3206313L, 3206314L, 3206315L, 3206317L, 3206318L, 3206319L,
          3206321L, 3206322L, 3206323L, 3206324L, 3206325L, 3206326L, 3206327L,
          3206328L, 3206329L, 3206330L, 3206331L, 3206332L, 3206333L, 3206334L,
          3206335L, 3206336L, 3206337L, 3206338L, 3206339L, 3206340L, 3206341L,
          3206342L, 3206343L, 3206344L, 3206345L, 3206346L, 3206347L, 3206348L,
          3206349L, 3206350L, 3206751L, 3206752L, 3206753L, 3206754L, 3206755L,
          3206756L, 3206757L, 3206758L, 3206759L, 3206760L, 3206761L, 3206762L,
          3206763L, 3206764L, 3206765L, 3206766L, 3206767L, 3206768L, 3206769L,
          3206770L, 3206771L, 3206772L, 3206773L, 3206774L, 3206775L, 3206776L,
          3206777L, 3206778L, 3206779L, 3206780L, 3206781L, 3206782L, 3206783L,
          3206784L, 3206785L, 3206786L, 3206787L, 3206788L, 3206789L, 3206790L,
          3206791L, 3206792L, 3206793L, 3206794L, 3206795L, 3206796L, 3206797L,
          3206798L, 3206799L, 3206800L, 3207451L, 3207452L, 3207453L, 3207454L,
          3207455L, 3207456L, 3207458L, 3207459L, 3207460L, 3207461L, 3207462L,
          3207463L, 3207464L, 3207465L, 3207466L, 3207467L, 3207468L, 3207469L,
          3207470L, 3207471L, 3207472L, 3207473L, 3207474L, 3207475L, 3207476L,
          3207477L, 3207478L, 3207479L, 3207480L, 3207481L, 3207482L, 3207483L,
          3207484L, 3207485L, 3207486L, 3207487L, 3207488L, 3207489L, 3207491L,
          3207492L, 3207493L, 3207494L, 3207495L, 3207496L, 3207497L, 3207498L,
          3207499L, 3207500L, 3207951L, 3207952L, 3207953L, 3207954L, 3207955L,
          3207956L, 3207957L, 3207958L, 3207959L, 3207960L, 3207961L, 3207962L,
          3207963L, 3207964L, 3207965L, 3207966L, 3207967L, 3207968L, 3207969L,
          3207970L, 3207971L, 3207972L, 3207973L, 3207974L, 3207975L, 3207976L,
          3207977L, 3207978L, 3207979L, 3207980L, 3207981L, 3207982L, 3207983L,
          3207984L, 3207985L, 3207986L, 3207987L, 3207988L, 3207989L, 3207990L,
          3207991L, 3207992L, 3207993L, 3207994L, 3207995L, 3207996L, 3207997L,
          3207998L, 3207999L, 3208000L, 3208301L, 3208302L, 3208303L, 3208304L,
          3208305L, 3208306L, 3208307L, 3208308L, 3208309L, 3208310L, 3208311L,
          3208312L, 3208313L, 3208314L, 3208315L, 3208316L, 3208317L, 3208318L,
          3208319L, 3208320L, 3208321L, 3208322L, 3208323L, 3208324L, 3208325L,
          3208326L, 3208327L, 3208328L, 3208329L, 3208330L, 3208331L, 3208332L,
          3208333L, 3208334L, 3208335L, 3208336L, 3208337L, 3208338L, 3208339L,
          3208340L, 3208341L, 3208342L, 3208343L, 3208344L, 3208345L, 3208346L,
          3208347L, 3208348L, 3208349L, 3208701L, 3208704L, 3208705L, 3208706L,
          3208707L, 3208708L, 3208709L, 3208710L, 3208712L, 3208713L, 3208714L,
          3208715L, 3208716L, 3208717L, 3208718L, 3208719L, 3208720L, 3208721L,
          3208722L, 3208723L, 3208724L, 3208725L, 3208726L, 3208727L, 3208728L,
          3208729L, 3208730L, 3208731L, 3208732L, 3208733L, 3208734L, 3208735L,
          3208736L, 3208737L, 3208738L, 3208739L, 3208740L, 3208741L, 3208742L,
          3208743L, 3208744L, 3208745L, 3208747L, 3208748L, 3208749L, 3208750L,
          3209301L, 3209302L, 3209303L, 3209304L, 3209305L, 3209306L, 3209307L,
          3209308L, 3209309L, 3209310L, 3209311L, 3209312L, 3209313L, 3209314L,
          3209315L, 3209316L, 3209317L, 3209318L, 3209319L, 3209320L, 3209321L,
          3209322L, 3209323L, 3209324L, 3209325L, 3209326L, 3209328L, 3209329L,
          3209330L, 3209331L, 3209332L, 3209333L, 3209334L, 3209335L, 3209336L,
          3209337L, 3209338L, 3209339L, 3209340L, 3209341L, 3209342L, 3209343L,
          3209344L, 3209345L, 3209346L, 3209347L, 3209348L, 3209349L, 3209350L,
          3209851L, 3209852L, 3209853L, 3209854L, 3209855L, 3209856L, 3209857L,
          3209858L, 3209859L, 3209860L, 3209861L, 3209862L, 3209863L, 3209864L,
          3209865L, 3209866L, 3209867L, 3209868L, 3209869L, 3209870L, 3209871L,
          3209872L, 3209873L, 3209874L, 3209875L, 3209876L, 3209877L, 3209878L,
          3209879L, 3209880L, 3209881L, 3209882L, 3209883L, 3209884L, 3209885L,
          3209886L, 3209887L, 3209888L, 3209889L, 3209890L, 3209891L, 3209892L,
          3209893L, 3209894L, 3209895L, 3209896L, 3209897L, 3209898L, 3209899L,
          3209900L, 3210201L, 3210202L, 3210203L, 3210204L, 3210205L, 3210206L,
          3210207L, 3210208L, 3210209L, 3210210L, 3210211L, 3210212L, 3210213L,
          3210214L, 3210215L, 3210216L, 3210217L, 3210218L, 3210219L, 3210220L,
          3210221L, 3210222L, 3210223L, 3210224L, 3210225L, 3210226L, 3210227L,
          3210228L, 3210229L, 3210230L, 3210231L, 3210232L, 3210233L, 3210234L,
          3210235L, 3210236L, 3210237L, 3210238L, 3210239L, 3210240L, 3210241L,
          3210242L, 3210243L, 3210244L, 3210245L, 3210247L, 3210248L, 3210249L,
          3210250L, 3210451L, 3210452L, 3210453L, 3210454L, 3210455L, 3210456L,
          3210457L, 3210458L, 3210459L, 3210460L, 3210461L, 3210462L, 3210463L,
          3210464L, 3210465L, 3210466L, 3210468L, 3210469L, 3210470L, 3210471L,
          3210473L, 3210474L, 3210475L, 3210476L, 3210477L, 3210478L, 3210479L,
          3210480L, 3210481L, 3210482L, 3210483L, 3210484L, 3210485L, 3210486L,
          3210487L, 3210488L, 3210489L, 3210490L, 3210491L, 3210492L, 3210494L,
          3210495L, 3210496L, 3210497L, 3210498L, 3210499L, 3210500L, 3210901L,
          3210902L, 3210903L, 3210904L, 3210905L, 3210906L, 3210907L, 3210908L,
          3210909L, 3210910L, 3210911L, 3210912L, 3210913L, 3210914L, 3210915L,
          3210916L, 3210917L, 3210918L, 3210919L, 3210920L, 3210921L, 3210922L,
          3210923L, 3210924L, 3210925L, 3210926L, 3210927L, 3210928L, 3210929L,
          3210930L, 3210931L, 3210932L, 3210933L, 3210934L, 3210935L, 3210936L,
          3210937L, 3210938L, 3210939L, 3210941L, 3210942L, 3210943L, 3210944L,
          3210945L, 3210946L, 3210947L, 3210948L, 3210949L, 3210950L, 3211651L,
          3211652L, 3211653L, 3211654L, 3211655L, 3211658L, 3211659L, 3211660L,
          3211661L, 3211662L, 3211663L, 3211664L, 3211665L, 3211666L, 3211667L,
          3211668L, 3211669L, 3211670L, 3211671L, 3211672L, 3211673L, 3211674L,
          3211675L, 3211676L, 3211677L, 3211678L, 3211679L, 3211680L, 3211681L,
          3211682L, 3211683L, 3211684L, 3211685L, 3211686L, 3211687L, 3211688L,
          3211689L, 3211690L, 3211691L, 3211692L, 3211693L, 3211694L, 3211696L,
          3211697L, 3211698L, 3211699L, 3211700L, 3212351L, 3212352L, 3212353L,
          3212354L, 3212355L, 3212356L, 3212357L, 3212358L, 3212359L, 3212360L,
          3212361L, 3212362L, 3212363L, 3212364L, 3212365L, 3212366L, 3212367L,
          3212368L, 3212370L, 3212371L, 3212373L, 3212374L, 3212375L, 3212376L,
          3212377L, 3212378L, 3212379L, 3212380L, 3212381L, 3212382L, 3212383L,
          3212384L, 3212385L, 3212386L, 3212387L, 3212388L, 3212389L, 3212390L,
          3212391L, 3212392L, 3212393L, 3212394L, 3212395L, 3212396L, 3212397L,
          3212398L, 3212399L, 3212400L, 3213001L, 3213002L, 3213003L, 3213004L,
          3213005L, 3213006L, 3213007L, 3213008L, 3213009L, 3213010L, 3213011L,
          3213012L, 3213013L, 3213014L, 3213015L, 3213016L, 3213017L, 3213018L,
          3213019L, 3213020L, 3213021L, 3213022L, 3213023L, 3213024L, 3213025L,
          3213026L, 3213027L, 3213028L, 3213029L, 3213030L, 3213031L, 3213032L,
          3213033L, 3213034L, 3213035L, 3213036L, 3213037L, 3213038L, 3213039L,
          3213040L, 3213041L, 3213042L, 3213043L, 3213044L, 3213045L, 3213046L,
          3213047L, 3213048L, 3213049L, 3213050L, 3213301L, 3213302L, 3213303L,
          3213304L, 3213305L, 3213306L, 3213307L, 3213308L, 3213309L, 3213310L,
          3213311L, 3213312L, 3213313L, 3213314L, 3213315L, 3213316L, 3213317L,
          3213318L, 3213319L, 3213320L, 3213321L, 3213322L, 3213323L, 3213324L,
          3213325L, 3213326L, 3213327L, 3213328L, 3213329L, 3213330L, 3213331L,
          3213332L, 3213333L, 3213334L, 3213335L, 3213336L, 3213337L, 3213338L,
          3213339L, 3213340L, 3213341L, 3213342L, 3213343L, 3213344L, 3213345L,
          3213346L, 3213347L, 3213348L, 3213349L, 3213350L, 3213651L, 3213652L,
          3213653L, 3213654L, 3213655L, 3213656L, 3213657L, 3213658L, 3213659L,
          3213660L, 3213661L, 3213662L, 3213663L, 3213664L, 3213665L, 3213666L,
          3213667L, 3213668L, 3213669L, 3213670L, 3213671L, 3213672L, 3213673L,
          3213674L, 3213675L, 3213676L, 3213677L, 3213678L, 3213679L, 3213680L,
          3213681L, 3213682L, 3213683L, 3213684L, 3213685L, 3213686L, 3213687L,
          3213688L, 3213689L, 3213690L, 3213691L, 3213692L, 3213693L, 3213694L,
          3213695L, 3213696L, 3213697L, 3213698L, 3213699L, 3213700L, 3214001L,
          3214002L, 3214003L, 3214004L, 3214005L, 3214006L, 3214007L, 3214008L,
          3214009L, 3214010L, 3214011L, 3214012L, 3214013L, 3214014L, 3214015L,
          3214016L, 3214017L, 3214018L, 3214019L, 3214020L, 3214021L, 3214022L,
          3214023L, 3214024L, 3214025L, 3214026L, 3214027L, 3214028L, 3214029L,
          3214030L, 3214031L, 3214032L, 3214033L, 3214034L, 3214035L, 3214036L,
          3214037L, 3214038L, 3214039L, 3214040L, 3214041L, 3214042L, 3214043L,
          3214044L, 3214045L, 3214046L, 3214047L, 3214048L, 3214049L, 3214050L,
          3214351L, 3214352L, 3214353L, 3214354L, 3214355L, 3214356L, 3214357L,
          3214358L, 3214359L, 3214360L, 3214361L, 3214362L, 3214363L, 3214364L,
          3214365L, 3214366L, 3214367L, 3214368L, 3214369L, 3214370L, 3214371L,
          3214372L, 3214373L, 3214374L, 3214375L, 3214376L, 3214377L, 3214378L,
          3214379L, 3214380L, 3214381L, 3214382L, 3214383L, 3214384L, 3214385L,
          3214386L, 3214387L, 3214388L, 3214389L, 3214390L, 3214391L, 3214392L,
          3214393L, 3214394L, 3214395L, 3214396L, 3214397L, 3214398L, 3214399L,
          3214400L, 3214651L, 3214652L, 3214653L, 3214654L, 3214655L, 3214656L,
          3214657L, 3214658L, 3214659L, 3214660L, 3214661L, 3214662L, 3214663L,
          3214664L, 3214665L, 3214666L, 3214667L, 3214668L, 3214669L, 3214670L,
          3214671L, 3214672L, 3214673L, 3214674L, 3214675L, 3214676L, 3214677L,
          3214678L, 3214679L, 3214680L, 3214681L, 3214682L, 3214683L, 3214684L,
          3214685L, 3214686L, 3214687L, 3214688L, 3214689L, 3214690L, 3214691L,
          3214692L, 3214693L, 3214694L, 3214695L, 3214696L, 3214697L, 3214699L,
          3214700L, 3215051L, 3215052L, 3215053L, 3215054L, 3215055L, 3215056L,
          3215057L, 3215058L, 3215059L, 3215060L, 3215061L, 3215062L, 3215063L,
          3215064L, 3215065L, 3215066L, 3215067L, 3215068L, 3215069L, 3215070L,
          3215071L, 3215072L, 3215073L, 3215074L, 3215075L, 3215076L, 3215077L,
          3215078L, 3215079L, 3215080L, 3215081L, 3215082L, 3215083L, 3215084L,
          3215085L, 3215086L, 3215087L, 3215088L, 3215089L, 3215090L, 3215091L,
          3215092L, 3215093L, 3215094L, 3215095L, 3215096L, 3215097L, 3215098L,
          3215099L, 3215100L, 3215351L, 3215352L, 3215353L, 3215354L, 3215355L,
          3215356L, 3215357L, 3215358L, 3215359L, 3215360L, 3215361L, 3215362L,
          3215363L, 3215364L, 3215365L, 3215366L, 3215367L, 3215368L, 3215369L,
          3215370L, 3215371L, 3215372L, 3215373L, 3215374L, 3215375L, 3215376L,
          3215377L, 3215378L, 3215379L, 3215380L, 3215381L, 3215382L, 3215383L,
          3215384L, 3215385L, 3215386L, 3215387L, 3215388L, 3215389L, 3215390L,
          3215391L, 3215392L, 3215393L, 3215394L, 3215395L, 3215396L, 3215397L,
          3215398L, 3215399L, 3215400L, 3215651L, 3215652L, 3215653L, 3215654L,
          3215655L, 3215656L, 3215657L, 3215658L, 3215659L, 3215660L, 3215661L,
          3215662L, 3215663L, 3215664L, 3215665L, 3215666L, 3215667L, 3215668L,
          3215669L, 3215670L, 3215671L, 3215672L, 3215673L, 3215674L, 3215675L,
          3215676L, 3215677L, 3215678L, 3215679L, 3215680L, 3215681L, 3215682L,
          3215683L, 3215684L, 3215685L, 3215686L, 3215687L, 3215688L, 3215689L,
          3215690L, 3215691L, 3215692L, 3215693L, 3215694L, 3215695L, 3215696L,
          3215697L, 3215698L, 3215699L, 3215700L, 3216101L, 3216102L, 3216103L,
          3216104L, 3216105L, 3216106L, 3216107L, 3216108L, 3216109L, 3216110L,
          3216111L, 3216112L, 3216113L, 3216114L, 3216115L, 3216116L, 3216117L,
          3216118L, 3216119L, 3216121L, 3216123L, 3216124L, 3216125L, 3216126L,
          3216127L, 3216128L, 3216129L, 3216130L, 3216131L, 3216132L, 3216133L,
          3216134L, 3216135L, 3216136L, 3216137L, 3216138L, 3216139L, 3216140L,
          3216141L, 3216142L, 3216143L, 3216144L, 3216145L, 3216146L, 3216147L,
          3216148L, 3216150L, 3216551L, 3216552L, 3216553L, 3216554L, 3216555L,
          3216556L, 3216558L, 3216560L, 3216561L, 3216562L, 3216563L, 3216564L,
          3216565L, 3216566L, 3216567L, 3216568L, 3216569L, 3216570L, 3216571L,
          3216572L, 3216573L, 3216574L, 3216575L, 3216576L, 3216577L, 3216578L,
          3216579L, 3216580L, 3216581L, 3216582L, 3216583L, 3216584L, 3216585L,
          3216586L, 3216588L, 3216589L, 3216590L, 3216592L, 3216593L, 3216594L,
          3216595L, 3216597L, 3216598L, 3216599L, 3216600L, 3217301L, 3217302L,
          3217303L, 3217304L, 3217305L, 3217306L, 3217307L, 3217308L, 3217309L,
          3217310L, 3217311L, 3217312L, 3217313L, 3217314L, 3217315L, 3217316L,
          3217317L, 3217319L, 3217320L, 3217321L, 3217322L, 3217323L, 3217324L,
          3217325L, 3217326L, 3217327L, 3217328L, 3217329L, 3217330L, 3217331L,
          3217332L, 3217333L, 3217334L, 3217335L, 3217336L, 3217337L, 3217338L,
          3217339L, 3217340L, 3217341L, 3217342L, 3217343L, 3217344L, 3217345L,
          3217346L, 3217347L, 3217348L, 3217350L, 3217751L, 3217752L, 3217753L,
          3217754L, 3217755L, 3217757L, 3217758L, 3217760L, 3217761L, 3217762L,
          3217763L, 3217764L, 3217765L, 3217766L, 3217767L, 3217768L, 3217769L,
          3217770L, 3217771L, 3217772L, 3217773L, 3217774L, 3217775L, 3217776L,
          3217778L, 3217780L, 3217781L, 3217782L, 3217783L, 3217784L, 3217785L,
          3217786L, 3217787L, 3217788L, 3217789L, 3217790L, 3217791L, 3217792L,
          3217793L, 3217794L, 3217795L, 3217796L, 3217797L, 3217798L, 3217799L,
          3217800L, 3218301L, 3218302L, 3218303L, 3218304L, 3218305L, 3218306L,
          3218307L, 3218308L, 3218309L, 3218310L, 3218311L, 3218312L, 3218313L,
          3218314L, 3218315L, 3218316L, 3218317L, 3218318L, 3218319L, 3218320L,
          3218321L, 3218322L, 3218323L, 3218324L, 3218325L, 3218326L, 3218327L,
          3218328L, 3218329L, 3218330L, 3218331L, 3218332L, 3218333L, 3218334L,
          3218335L, 3218336L, 3218337L, 3218338L, 3218339L, 3218340L, 3218341L,
          3218342L, 3218343L, 3218344L, 3218345L, 3218346L, 3218347L, 3218348L,
          3218349L, 3218350L, 3218651L, 3218652L, 3218653L, 3218654L, 3218655L,
          3218656L, 3218657L, 3218658L, 3218659L, 3218660L, 3218661L, 3218662L,
          3218663L, 3218664L, 3218665L, 3218666L, 3218667L, 3218668L, 3218669L,
          3218670L, 3218671L, 3218672L, 3218673L, 3218674L, 3218675L, 3218676L,
          3218677L, 3218678L, 3218679L, 3218680L, 3218681L, 3218682L, 3218683L,
          3218684L, 3218685L, 3218686L, 3218687L, 3218688L, 3218689L, 3218690L,
          3218691L, 3218692L, 3218693L, 3218694L, 3218695L, 3218696L, 3218697L,
          3218698L, 3218699L, 3218700L, 3218951L, 3218952L, 3218953L, 3218954L,
          3218955L, 3218956L, 3218957L, 3218958L, 3218959L, 3218960L, 3218961L,
          3218962L, 3218963L, 3218964L, 3218965L, 3218966L, 3218967L, 3218968L,
          3218969L, 3218970L, 3218971L, 3218972L, 3218973L, 3218974L, 3218975L,
          3218976L, 3218977L, 3218978L, 3218979L, 3218980L, 3218981L, 3218982L,
          3218983L, 3218984L, 3218985L, 3218986L, 3218987L, 3218988L, 3218989L,
          3218990L, 3218991L, 3218992L, 3218993L, 3218994L, 3218995L, 3218996L,
          3218997L, 3218998L, 3218999L, 3219000L, 3219301L, 3219302L, 3219303L,
          3219304L, 3219305L, 3219306L, 3219307L, 3219308L, 3219309L, 3219310L,
          3219311L, 3219312L, 3219313L, 3219314L, 3219315L, 3219316L, 3219317L,
          3219318L, 3219319L, 3219320L, 3219321L, 3219322L, 3219323L, 3219324L,
          3219325L, 3219326L, 3219327L, 3219328L, 3219329L, 3219330L, 3219331L,
          3219332L, 3219333L, 3219334L, 3219335L, 3219336L, 3219337L, 3219338L,
          3219339L, 3219340L, 3219341L, 3219342L, 3219343L, 3219344L, 3219345L,
          3219346L, 3219347L, 3219348L, 3219349L, 3219350L, 3219651L, 3219652L,
          3219653L, 3219654L, 3219655L, 3219656L, 3219657L, 3219658L, 3219659L,
          3219660L, 3219661L, 3219662L, 3219663L, 3219664L, 3219665L, 3219666L,
          3219667L, 3219668L, 3219669L, 3219670L, 3219671L, 3219672L, 3219673L,
          3219674L, 3219675L, 3219676L, 3219677L, 3219678L, 3219679L, 3219680L,
          3219681L, 3219682L, 3219683L, 3219684L, 3219685L, 3219686L, 3219687L,
          3219688L, 3219689L, 3219690L, 3219691L, 3219692L, 3219693L, 3219694L,
          3219695L, 3219696L, 3219697L, 3219698L, 3219699L, 3219700L, 3220001L,
          3220002L, 3220003L, 3220004L, 3220005L, 3220006L, 3220007L, 3220008L,
          3220009L, 3220010L, 3220011L, 3220012L, 3220013L, 3220014L, 3220015L,
          3220016L, 3220017L, 3220018L, 3220019L, 3220020L, 3220021L, 3220022L,
          3220023L, 3220024L, 3220025L, 3220026L, 3220027L, 3220028L, 3220029L,
          3220030L, 3220031L, 3220032L, 3220033L, 3220034L, 3220035L, 3220036L,
          3220037L, 3220038L, 3220039L, 3220040L, 3220041L, 3220042L, 3220043L,
          3220044L, 3220045L, 3220046L, 3220047L, 3220048L, 3220049L, 3220050L,
          3220301L, 3220302L, 3220303L, 3220304L, 3220305L, 3220306L, 3220307L,
          3220308L, 3220309L, 3220310L, 3220311L, 3220312L, 3220313L, 3220314L,
          3220315L, 3220316L));

      for (Long molecularActionId : molecularActionIds) {

        final MolecularAction molecularAction =
            getMolecularAction(molecularActionId);
        // Create and set up an undo action
        final RedoMolecularAction redoAction = new RedoMolecularAction();

        // Configure and run the undo action
        redoAction.setProject(getProject());
        redoAction.setActivityId(molecularAction.getActivityId());
        redoAction.setConceptId(molecularAction.getComponentId());
        redoAction.setConceptId2(molecularAction.getComponentId2());
        redoAction.setLastModifiedBy(molecularAction.getLastModifiedBy());
        redoAction.setTransactionPerOperation(false);
        redoAction.setMolecularActionFlag(false);
        redoAction.setChangeStatusFlag(true);
        redoAction.setMolecularActionId(molecularAction.getId());
        redoAction.setForce(false);
        redoAction.performMolecularAction(redoAction, getLastModifiedBy(),
            false, false);

        redoAction.close();
      }

    } catch (Exception e) {
      e.printStackTrace();
      fail("Unexpected exception thrown - please review stack trace.");
    } finally {
      // Close algorithm for each loop
    }

    logInfo("Finished " + getName());
  }

  private void fixBadRelationshipIdentities() throws Exception {
    // 5/7/2018 Issues with MTH relationship identities imported from
    // MEME4 were identified.

    logInfo(" Fix bad relationship identities");

    UmlsIdentityService identityService = new UmlsIdentityServiceJpa();

    int removedIdentities = 0;
    int updatedIdentities = 0;
    List<RelationshipIdentity> relationshipIdentities = new ArrayList<>();

    try {

      // Identify all relationship identities that have duplicates
      // REAL QUERY
      Query query = getEntityManager().createNativeQuery(
          "select id from relationship_identity where terminology='MTH' group by "
              + "additionalRelationshipType,fromId,fromTerminology,fromType,"
              + "relationshipType,terminology,terminologyId,toId,toTerminology,toType "
              + "having count(*) > 1");

      // TEST QUERY
      // Query query = getEntityManager().createNativeQuery(
      // "select id from relationship_identity where fromId='C0002335' and
      // toId='C0593527' group by "
      // + "additionalRelationshipType,fromId,fromTerminology,fromType,"
      // + "relationshipType,terminology,terminologyId,toId,toTerminology,toType
      // "
      // + " having count(*) > 1");

      logInfo("[FixBadRelationshipIdentities] Identifying "
          + "duplicate relationship identities");

      List<Object> list = query.getResultList();
      for (final Object entry : list) {
        final Long id = Long.valueOf(entry.toString());
        relationshipIdentities.add(identityService.getRelationshipIdentity(id));
      }

      setSteps(relationshipIdentities.size());

      logInfo("[FixBadRelationshipIdentities] " + relationshipIdentities.size()
          + " RelationshipIdentity duplicates identified");

      for (final RelationshipIdentity identity : relationshipIdentities) {

        List<RelationshipIdentity> duplicateRelationshipIdentities =
            new ArrayList<>();
        List<Long> duplicateRelationshipIds = new ArrayList<>();

        // get the identity's duplicate(s)
        query = getEntityManager().createQuery("select r.id from "
            + "RelationshipIdentityJpa r "
            + "where r.terminology = :terminology and r.fromId = :fromId and r.toId = :toId and r.relationshipType = :relationshipType");
        query.setParameter("terminology", "MTH");
        query.setParameter("fromId", identity.getFromId());
        query.setParameter("toId", identity.getToId());
        query.setParameter("relationshipType", identity.getRelationshipType());

        List<Object> list2 = query.getResultList();
        for (final Object entry : list2) {
          final Long id = Long.valueOf(entry.toString());
          RelationshipIdentity duplicateRelationshipIdentity =
              identityService.getRelationshipIdentity(id);
          if (duplicateRelationshipIdentity != null) {
            duplicateRelationshipIds.add(id);
            duplicateRelationshipIdentities.add(duplicateRelationshipIdentity);
          }
        }

        // It is possible that no duplicates will be identified (they may have
        // been removed by an earlier iteration). If so, skip.
        if (duplicateRelationshipIds.size() < 2) {
          updateProgress();
          continue;
        }

        List<RelationshipIdentity> inverseRelationshipIdentities =
            new ArrayList<>();
        List<Long> inverseRelationshipIds = new ArrayList<>();

        // Look up the inverse identity(s) (reverse To/From values, and
        // inverse relationship types)
        query = getEntityManager().createQuery("select r.id from "
            + "RelationshipIdentityJpa r "
            + "where r.terminology = :terminology and r.fromId = :fromId and r.toId = :toId and r.relationshipType = :inverseRelationshipType");
        query.setParameter("terminology", "MTH");
        query.setParameter("fromId", identity.getToId());
        query.setParameter("toId", identity.getFromId());
        query.setParameter("inverseRelationshipType",
            getInverseRelationshipType(getProject().getTerminology(),
                getProject().getVersion(), identity.getRelationshipType()));

        list = query.getResultList();
        for (final Object entry : list) {
          final Long id = Long.valueOf(entry.toString());
          RelationshipIdentity inverseRelationshipIdentity =
              identityService.getRelationshipIdentity(id);
          if (inverseRelationshipIdentity != null) {
            inverseRelationshipIds.add(id);
            inverseRelationshipIdentities.add(inverseRelationshipIdentity);
          }
        }

        List<String> RUIs = new ArrayList<>();

        // If an existing concept relationship uses any of these identities for
        // its RUI, that is the identity we need to keep
        query = getEntityManager().createQuery("select value(a) from "
            + "ConceptRelationshipJpa r join r.from c1 join r.to c2 join r.alternateTerminologyIds a "
            + "where KEY(a) = :projectTerminology "
            + "and r.terminology = :terminology and c1.terminologyId = :fromId and c2.terminologyId = :toId and r.relationshipType = :relationshipType");
        query.setParameter("projectTerminology", "NCIMTH");
        query.setParameter("terminology", "MTH");
        query.setParameter("fromId", identity.getFromId());
        query.setParameter("toId", identity.getToId());
        query.setParameter("relationshipType", identity.getRelationshipType());

        list = query.getResultList();
        for (final Object entry : list) {
          RUIs.add(entry.toString());
        }

        // And do again for the inverse
        query = getEntityManager().createQuery("select value(a) from "
            + "ConceptRelationshipJpa r join r.from c1 join r.to c2 join r.alternateTerminologyIds a "
            + "where KEY(a) = :projectTerminology "
            + "and r.terminology = :terminology and c1.terminologyId = :fromId and c2.terminologyId = :toId and r.relationshipType = :inverseRelationshipType");
        query.setParameter("projectTerminology", "NCIMTH");
        query.setParameter("terminology", "MTH");
        query.setParameter("fromId", identity.getToId());
        query.setParameter("toId", identity.getFromId());
        query.setParameter("inverseRelationshipType",
            getInverseRelationshipType(getProject().getTerminology(),
                getProject().getVersion(), identity.getRelationshipType()));

        list = query.getResultList();
        for (final Object entry : list) {
          RUIs.add(entry.toString());
        }

        // For any identitified RUI, parse out the numeric part of it, and
        // recast as Long. These are the ids of the relationship_identity(s) we
        // need to keep.
        final List<Long> keepList = new ArrayList<>();
        for (final String RUI : RUIs) {
          keepList.add(Long.parseLong(RUI.substring(1)));
        }

        // Identify which identity is linked to the existing relationship(if
        // any)
        RelationshipIdentity keepDuplicateIdentity = null;

        // System.out.println("KeepList= " + keepList);
        for (RelationshipIdentity duplicateIdentity : duplicateRelationshipIdentities) {
          if (keepList.contains(duplicateIdentity.getId())) {
            keepDuplicateIdentity = duplicateIdentity;
          }
        }

        RelationshipIdentity keepInverseIdentity = null;

        for (RelationshipIdentity inverseIdentity : inverseRelationshipIdentities) {
          if (keepList.contains(inverseIdentity.getId())) {
            keepInverseIdentity = inverseIdentity;
          }
        }

        // If a duplicate identity is linked but not an inverse
        if (keepDuplicateIdentity != null && keepInverseIdentity == null) {
          // Check if duplicate points to a valid inverse
          if (inverseRelationshipIds
              .contains(keepDuplicateIdentity.getInverseId())) {
            keepInverseIdentity = identityService
                .getRelationshipIdentity(keepDuplicateIdentity.getInverseId());
          }
          // Otherwise, look for an inverse that points to the duplicate
          else {
            for (RelationshipIdentity relationshipIdentity : inverseRelationshipIdentities) {
              if (relationshipIdentity.getInverseId()
                  .equals(keepDuplicateIdentity.getId())) {
                keepInverseIdentity = relationshipIdentity;
              }
            }
          }
        }

        // if an inverse identity is linked but not a duplicate
        else if (keepInverseIdentity != null && keepDuplicateIdentity == null) {
          // Check if inverse points to a valid duplicate
          if (duplicateRelationshipIds
              .contains(keepInverseIdentity.getInverseId())) {
            keepDuplicateIdentity = identityService
                .getRelationshipIdentity(keepInverseIdentity.getInverseId());
          }
          // Otherwise, look for a duplicate that points to the inverse
          else {
            for (RelationshipIdentity relationshipIdentity : duplicateRelationshipIdentities) {
              if (relationshipIdentity.getInverseId()
                  .equals(keepInverseIdentity.getId())) {
                keepDuplicateIdentity = relationshipIdentity;
              }
            }
          }
        }

        // If none of the identities are linked to an existing
        // relationship, try to find ones that point to each other
        else if (keepInverseIdentity == null && keepDuplicateIdentity == null) {
          for (RelationshipIdentity duplicateRelationshipIdentity : duplicateRelationshipIdentities) {
            if (keepDuplicateIdentity != null) {
              break;
            }
            for (RelationshipIdentity inverseRelationshipIdentity : inverseRelationshipIdentities) {
              if (duplicateRelationshipIdentity.getInverseId()
                  .equals(inverseRelationshipIdentity.getId())
                  || inverseRelationshipIdentity.getInverseId()
                      .equals(duplicateRelationshipIdentity.getId())) {
                keepDuplicateIdentity = duplicateRelationshipIdentity;
                keepInverseIdentity = inverseRelationshipIdentity;
                break;
              }
            }
          }

          // If nothing was found that points to each other, just choose
          // arbitrarily
          if (keepDuplicateIdentity == null && keepInverseIdentity == null) {
            keepDuplicateIdentity = duplicateRelationshipIdentities.get(0);
            keepInverseIdentity = inverseRelationshipIdentities.get(0);
          }
        }

        // Now that we've identified which identities to keep, make sure they
        // point to each other, and remove the rest
        if (keepDuplicateIdentity != null && keepInverseIdentity != null) {
          if (!keepDuplicateIdentity.getInverseId()
              .equals(keepInverseIdentity.getId())) {
            keepDuplicateIdentity.setInverseId(keepInverseIdentity.getId());
            identityService.updateRelationshipIdentity(keepInverseIdentity);
            updatedIdentities++;
          }
          if (!keepInverseIdentity.getInverseId()
              .equals(keepDuplicateIdentity.getId())) {
            keepInverseIdentity.setInverseId(keepDuplicateIdentity.getId());
            identityService.updateRelationshipIdentity(keepDuplicateIdentity);
            updatedIdentities++;
          }
          // Remove the superfluous duplicate identities
          for (final RelationshipIdentity relationshipIdentity : duplicateRelationshipIdentities) {
            if (relationshipIdentity.getId()
                .equals(keepDuplicateIdentity.getId())) {
              continue;
            } else {
              identityService
                  .removeRelationshipIdentity(relationshipIdentity.getId());
              removedIdentities++;
            }
          }
          // Remove the superfluous inverse identities
          for (final RelationshipIdentity relationshipIdentity : inverseRelationshipIdentities) {
            if (relationshipIdentity.getId()
                .equals(keepInverseIdentity.getId())) {
              continue;
            } else {
              identityService
                  .removeRelationshipIdentity(relationshipIdentity.getId());
              removedIdentities++;
            }
          }
        }
        // We should never get here
        else {
          logError("WHAT HAPPENED!!!????");
        }

        updateProgress();

      }
    } catch (Exception e) {
      e.printStackTrace();
      fail("Unexpected exception thrown - please review stack trace.");
    } finally {
      identityService.close();
    }

    logInfo("Removed " + removedIdentities
        + " bad inverse relationship identities.");
    logInfo("Updated " + updatedIdentities
        + " relationship identities updated to point to their true inverses.");
    logInfo("Finished " + getName());
  }

  private void fixComponentInfoRelationships() throws Exception {
    // 5/23/2018 Issues identified where componentInfoRelationships had blank
    // to/from terminologyIds. These all were associated with a single Atom:
    // terminology='NCIMTH', name= 'NCI Thesaurus', AUI=31926003
    // Update to componentInfoRelationships to have to/from TerminologyIds point
    // to the AUI 31926003

    logInfo(" Fix Component Info Relationships");

    int updatedRelationships = 0;

    final List<ComponentInfoRelationshipJpa> componentInfoRelationships =
        new ArrayList<>();

    try {
      Query query = getEntityManager().createNativeQuery(
          "select id from component_info_relationships where fromTerminologyId='' or toTerminologyId=''");

      logInfo("[FixComponentInfoRelationships] Identifying "
          + "ComponentInfoRelationships with blank from/to Terminology Ids");

      List<Object> list = query.getResultList();
      for (final Object entry : list) {
        final Long id = Long.valueOf(entry.toString());
        componentInfoRelationships
            .add((ComponentInfoRelationshipJpa) getRelationship(id,
                ComponentInfoRelationshipJpa.class));
      }

      setSteps(componentInfoRelationships.size());

      logInfo("[FixComponentInfoRelationships] "
          + componentInfoRelationships.size()
          + " ComponentInfoRelationships with blank from/to Terminology Ids identified");

      for (final ComponentInfoRelationship componentInfoRelationship : componentInfoRelationships) {
        if (ConfigUtility
            .isEmpty(componentInfoRelationship.getFromTerminologyId())
            && componentInfoRelationship.getFromName()
                .equals("NCI Thesaurus")) {
          componentInfoRelationship.setFromTerminologyId("31926003");
          updateRelationship(componentInfoRelationship);
          updatedRelationships++;
        } else if (ConfigUtility
            .isEmpty(componentInfoRelationship.getToTerminologyId())
            && componentInfoRelationship.getToName().equals("NCI Thesaurus")) {
          componentInfoRelationship.setToTerminologyId("31926003");
          updateRelationship(componentInfoRelationship);
          updatedRelationships++;
        } else {
          logError(
              "ComponentInfoRelationship with unexpected blank to/from TerminologyId identified: "
                  + componentInfoRelationship);
        }

        updateProgress();
      }
    } catch (Exception e) {
      e.printStackTrace();
      fail("Unexpected exception thrown - please review stack trace.");
    } finally {
    }

    logInfo(
        "Updated " + updatedRelationships + " component info relationships.");
    logInfo("Finished " + getName());
  }

  private void setComponentInfoRelationshipsToPublishable() throws Exception {
    // 5/30/2018 A bad release test run set a bunch of
    // ComponentInfoRelationships to unpublishable. Set them back to
    // publishable.

    logInfo(" Set Component Info Relationships To Publishable");

    int updatedRelationships = 0;

    final List<ComponentInfoRelationshipJpa> componentInfoRelationships =
        new ArrayList<>();

    try {
      Query query = getEntityManager().createNativeQuery(
          "select id from component_info_relationships where lastModifiedBy='NCIMTH_201805'");

      List<Object> list = query.getResultList();
      for (final Object entry : list) {
        final Long id = Long.valueOf(entry.toString());
        componentInfoRelationships
            .add((ComponentInfoRelationshipJpa) getRelationship(id,
                ComponentInfoRelationshipJpa.class));
      }

      setSteps(componentInfoRelationships.size());

      logInfo("[SetComponentInfoRelationshipsToPublishable] "
          + componentInfoRelationships.size()
          + " ComponentInfoRelationships that need to be set to publishable");

      for (final ComponentInfoRelationship componentInfoRelationship : componentInfoRelationships) {
        if (!componentInfoRelationship.isPublishable()) {
          componentInfoRelationship.setPublishable(true);
          updateRelationship(componentInfoRelationship);
          updatedRelationships++;
        } else {
          logError("ComponentInfoRelationship was already set to publishable: "
              + componentInfoRelationship);
        }

        updateProgress();
      }
    } catch (Exception e) {
      e.printStackTrace();
      fail("Unexpected exception thrown - please review stack trace.");
    } finally {
    }

    logInfo(
        "Updated " + updatedRelationships + " component info relationships.");
    logInfo("Finished " + getName());
  }

  private void setStampedWorklistsToReadyForPublication() throws Exception {
    // 6/25/2018 It was determined that a previous code change left Stamped
    // worklists in a non-terminal state: "REVIEW_DONE". Identify these
    // worklists, and update them to "READY_FOR_PUBLICATION"

    logInfo(" Set Stamped Worklists To Ready For Publication");

    int updatedRelationships = 0;

    final List<Worklist> stampedWorklists = new ArrayList<>();

    try {
      Query query = getEntityManager().createNativeQuery(
          "select id from worklists where workflowStatus='REVIEW_DONE'");

      List<Object> list = query.getResultList();
      for (final Object entry : list) {
        final Long id = Long.valueOf(entry.toString());
        stampedWorklists.add(getWorklist(id));
      }

      setSteps(stampedWorklists.size());

      logInfo("[SetStampedWorklistsToReadyForPublication] "
          + stampedWorklists.size()
          + " Stamped Worklists that need to be set to READY_FOR_PUBLICATION");

      for (final Worklist worklist : stampedWorklists) {
        worklist.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);
        updatedRelationships++;
        updateProgress();
      }
    } catch (Exception e) {
      e.printStackTrace();
      fail("Unexpected exception thrown - please review stack trace.");
    } finally {
    }

    logInfo("Updated " + updatedRelationships
        + " Stamped worklists' workflow status.");
    logInfo("Finished " + getName());
  }

  private void addDispositionAtoms() throws Exception {
    // 7/3/2018 Add an NCIMTH/PN atom to every concept that has a SNOMEDCT_US/FN
    // atom in it with the word (disposition) at the end of the name.

    logInfo(" Add Disposition NCIMTH/PN Atoms");

    int addedAtomsCount = 0;
    int skippedConceptCount = 0;

    final List<Concept> concepts = new ArrayList<>();

    try {

      Query query = getEntityManager().createNativeQuery(
          "select c.id from concepts c, concepts_atoms ca, atoms a "
              + "where ca.concepts_id = c.id and ca.atoms_id = a.id and "
              + "c.terminology='NCIMTH' and a.terminology='SNOMEDCT_US' and "
              + "a.name like '%(disposition)' and a.termType='FN'");

      List<Object> list = query.getResultList();
      for (final Object entry : list) {
        final Long id = Long.valueOf(entry.toString());
        concepts.add(getConcept(id));
      }

      setSteps(concepts.size());

      logInfo("[AddDispositionNCIMTHPNAtoms] " + concepts.size()
          + " Concepts that need an NCIMTH/PN disposition atom");

      for (final Concept concept : concepts) {
        // Identify the SNOMEDCT_US (disposition) atom
        Atom dispositionAtom = null;
        int dispositionAtomCount = 0;
        int ncimthpnAtomCount = 0;
        for (Atom atom : concept.getAtoms()) {
          if (atom.getName().matches(".*\\(disposition\\)")) {
            dispositionAtom = atom;
            dispositionAtomCount++;
          }
          if (atom.getTerminology().equals("NCIMTH")
              && atom.getTermType().equals("PN")) {
            ncimthpnAtomCount++;
          }
        }

        if (dispositionAtom == null) {
          logError("No disposition atoms found for concept " + concept);
          skippedConceptCount++;
          updateProgress();
          continue;
        }

        if (dispositionAtomCount > 1) {
          logWarn(
              "More than one disposition atom - skipping concept " + concept);
          skippedConceptCount++;
          updateProgress();
          continue;
        }

        if (ncimthpnAtomCount > 0) {
          logWarn("There is already an NCIMTH/PN atom - skipping concept "
              + concept);
          skippedConceptCount++;
          updateProgress();
          continue;
        }

        Atom atomToAdd = new AtomJpa();
        atomToAdd.setTerminology("NCIMTH");
        atomToAdd.setVersion("latest");
        atomToAdd.setTermType("PN");
        atomToAdd.setLanguage("ENG");
        atomToAdd.setName(dispositionAtom.getName());
        atomToAdd.setCodeId("NOCODE");
        atomToAdd.setConceptId("");
        atomToAdd.setDescriptorId("");
        atomToAdd.setTerminologyId("");
        atomToAdd.setPublishable(true);
        atomToAdd.setPublished(false);

        // Instantiate services
        final AddAtomMolecularAction action = new AddAtomMolecularAction();
        try {

          // Configure the action
          action.setProject(getProject());
          action.setActivityId("AddDispositionNCIMTHPNAtoms");
          action.setConceptId(concept.getId());
          action.setConceptId2(null);
          action.setLastModifiedBy("admin");
          action.setLastModified(concept.getLastModified().getTime());
          action.setOverrideWarnings(false);
          action.setTransactionPerOperation(false);
          action.setMolecularActionFlag(true);
          action.setChangeStatusFlag(true);

          action.setAtom(atomToAdd);

          // Perform the action
          final ValidationResult validationResult =
              action.performMolecularAction(action, "admin", true, false);

          // If the action failed, bail out now.
          if (!validationResult.isValid()) {
            logError("Unexpected problem - " + validationResult);
          }

          // Otherwise, increment the successful add-atom count
          addedAtomsCount++;

        } catch (Exception e) {
          e.printStackTrace();
          fail("Unexpected exception thrown - please review stack trace.");
        } finally {
          action.close();
        }

        updateProgress();
      }
    } catch (Exception e) {
      e.printStackTrace();
      fail("Unexpected exception thrown - please review stack trace.");
    } finally {
    }

    logInfo("Added " + addedAtomsCount + " disposition NCIMTH/PN atoms.");
    logInfo("Skipped " + skippedConceptCount
        + " concepts that had more than one disposition atom.");
    logInfo("Finished " + getName());
  }

  private void fixRelGroups() throws Exception {
    // 8/28/2018 Issues identified with relationship groups being set to null
    // intead of blank.
    logInfo(" Fix null RelGroups");

    int updatedRelationships = 0;
    List<ConceptRelationshipJpa> relationships = new ArrayList<>();

    try {

      // Identify all relationship with relGroup set to null
      // REAL QUERY
      Query query = getEntityManager().createNativeQuery(
          "select id from concept_relationships where relGroup is null");

      // TEST QUERY
      // Query query = getEntityManager().createNativeQuery(
      // "select id from concept_relationships where relGroup is null limit 5");

      logInfo("[FixRelGroups] Identifying "
          + "relationships with rel groups set to NULL");

      List<Object> list = query.getResultList();
      for (final Object entry : list) {
        final Long id = Long.valueOf(entry.toString());
        relationships.add((ConceptRelationshipJpa) getRelationship(id,
            ConceptRelationshipJpa.class));
      }

      setSteps(relationships.size());

      logInfo("[FixRelGroups] " + relationships.size()
          + " Relationships identified");

      for (final ConceptRelationship relationship : relationships) {

        // Set the relationship's relGroup from NULL to blank.
        if (relationship.getGroup() == null) {
          relationship.setGroup("");
          updateRelationship(relationship);
          updatedRelationships++;
        }
        // We should never get here
        else {
          logError("WHAT HAPPENED!!!????");
        }
        updateProgress();
      }
    } catch (Exception e) {
      e.printStackTrace();
      fail("Unexpected exception thrown - please review stack trace.");
    } finally {
      // n/a
    }

    logInfo("Updated " + updatedRelationships
        + " relationship relGroups to blank.");
    logInfo("Finished " + getName());
  }

  private void fixSourceLevelRels() throws Exception {
    // 9/4/2018 Issues identified with source level rels that had status=N.
    // Update to be status=R.
    logInfo(" Fix Source Level Rels");

    int updatedRelationships = 0;
    List<ConceptRelationshipJpa> relationships = new ArrayList<>();

    try {

      // Identify all source-level relationship with status=N
      // REAL QUERY
      Query query = getEntityManager().createNativeQuery(
          "select id from concept_relationships where terminology!='NCIMTH' and workflowStatus='NEEDS_REVIEW'");

      // TEST QUERY
      // Query query = getEntityManager().createNativeQuery(
      // "select id from concept_relationships where terminology!='NCIMTH' and
      // workflowStatus='NEEDS_REVIEW' limit 5");

      logInfo("[FixSourceLevelRels] Identifying "
          + "source-level relationships with status=N");

      List<Object> list = query.getResultList();
      for (final Object entry : list) {
        final Long id = Long.valueOf(entry.toString());
        relationships.add((ConceptRelationshipJpa) getRelationship(id,
            ConceptRelationshipJpa.class));
      }

      setSteps(relationships.size());

      logInfo("[FixSourceLevelRels] " + relationships.size()
          + " Relationships identified");

      for (final ConceptRelationship relationship : relationships) {

        // Set the relationship's status from N to R.
        if (relationship.getWorkflowStatus()
            .equals(WorkflowStatus.NEEDS_REVIEW)) {
          relationship.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);
          updateRelationship(relationship);
          updatedRelationships++;
        }
        // We should never get here
        else {
          logError("WHAT HAPPENED!!!????");
        }
        updateProgress();
      }
    } catch (Exception e) {
      e.printStackTrace();
      fail("Unexpected exception thrown - please review stack trace.");
    } finally {
      // n/a
    }

    logInfo("Updated " + updatedRelationships
        + " source-level relationships to status=R.");
    logInfo("Finished " + getName());
  }

  private void fixAdditionalRelTypeInverses() throws Exception {
    // 9/5/2018 Issues identified with additional relationship types where
    // multiple entries have the same inverse.
    // For 2 cases, for NCI, the new inverse replaces the old inverse, and so
    // set the old inverse to pubishable=false.
    // For 1 case, for MED-RT, a bad inversion made it point to the wrong
    // inverse. Update it to point to the correct one.
    logInfo(" Fix Additional Rel Type Inverses");

    int updatedAdditionalRelationshipTypes = 0;
    List<AdditionalRelationshipTypeJpa> additionalRelationshipsTypes =
        new ArrayList<>();

    try {

      // Get the three affected additional relationship types
      Query query = getEntityManager().createNativeQuery(
          "select abbreviation from additional_relationship_types where id in (1259,327352,850135)");

      List<Object> list = query.getResultList();
      for (final Object entry : list) {
        final String abbreviation = entry.toString();
        additionalRelationshipsTypes
            .add((AdditionalRelationshipTypeJpa) getAdditionalRelationshipType(
                abbreviation, "NCIMTH", "latest"));
      }

      setSteps(additionalRelationshipsTypes.size());

      logInfo("[FixAdditionalRelTypeInverses] "
          + additionalRelationshipsTypes.size()
          + " additional relationship types identified");

      for (final AdditionalRelationshipType additionalRelationshipType : additionalRelationshipsTypes) {

        // Set the two no-longer-referened additionalRelationshipTypes to
        // publishable=false
        if (additionalRelationshipType.getId() == 1259
            || additionalRelationshipType.getId() == 327352) {
          additionalRelationshipType.setPublishable(false);
          updateAdditionalRelationshipType(additionalRelationshipType);
          updatedAdditionalRelationshipTypes++;
        }
        // Set the one incorrectly-inverted additional relationship type to its
        // correct inverse
        else if (additionalRelationshipType.getId() == 850135) {
          AdditionalRelationshipType inverseRelType =
              getAdditionalRelationshipType("may_treat_MEDRT", "NCIMTH",
                  "latest");
          additionalRelationshipType.setInverse(inverseRelType);
          updateAdditionalRelationshipType(additionalRelationshipType);
          updatedAdditionalRelationshipTypes++;
        }
        // We should never get here
        else {
          logError("WHAT HAPPENED!!!????");
        }
        updateProgress();
      }
    } catch (Exception e) {
      e.printStackTrace();
      fail("Unexpected exception thrown - please review stack trace.");
    } finally {
      // n/a
    }

    logInfo("Updated " + updatedAdditionalRelationshipTypes
        + " additional relationship types updated.");
    logInfo("Finished " + getName());
  }

  private void fixSnomedFamily() throws Exception {
    // 9/12/2018 Snomed family should be SNOMEDCT_US, not SNOMED.
    // 8/23/2019 Added fix for CCS_10 source family, not CCS.
    logInfo(" Fix Snomed Family");

    RootTerminology rootTerminology = getRootTerminology("SNOMEDCT_US");
    rootTerminology.setFamily("SNOMEDCT_US");
    updateRootTerminology(rootTerminology);

    rootTerminology = getRootTerminology("CCS_10");
    rootTerminology.setFamily("CCS_10");
    updateRootTerminology(rootTerminology);

    logInfo("Finished " + getName());
  }

  private void turnOffCTRPSDC() throws Exception {
    // 9/14/2018 CTRP-SDC was retired - turn off terminology.
    logInfo(" Turn off CTRP-SDC");

    Terminology terminology = getTerminology("CTRP-SDC", "2017_12D");
    terminology.setCurrent(false);
    updateTerminology(terminology);

    logInfo("Finished " + getName());
  }

  private void fixTerminologyNames() throws Exception {
    // 9/12/2018 Terminology names should be versioned.
    // e.g. "US Edition of SNOMED CT" should be "US Edition of SNOMED CT,
    // 2018_03_01"
    logInfo(" Fix Terminology Names");

    int updatedTerminologies = 0;

    try {

      // Get all terminologies
      TerminologyList terminolgyList = getTerminologies();

      setSteps(terminolgyList.getObjects().size());

      for (final Terminology terminology : terminolgyList.getObjects()) {
        String versionSuffix = null;
        // Add version in different format based on terminology family
        if (terminology.getRootTerminology().getFamily().equals("NCI")
            || terminology.getRootTerminology().getFamily()
                .equals("SNOMEDCT_US")
            || terminology.getRootTerminology().getFamily().equals("MED-RT")
            || terminology.getRootTerminology().getFamily().equals("NCBI")
            || terminology.getRootTerminology().getFamily().equals("MTH")) {
          versionSuffix = ", " + terminology.getVersion();
        } else if (terminology.getRootTerminology().getFamily().equals("MDR")) {
          versionSuffix = ", " + terminology.getVersion().replace("_", ".");
        }

        if (versionSuffix == null) {
          updateProgress();
          continue;
        }

        if (terminology.getPreferredName().endsWith(versionSuffix)) {
          updateProgress();
          continue;
        } else {
          terminology
              .setPreferredName(terminology.getPreferredName() + versionSuffix);
          updatedTerminologies++;
        }

        updateProgress();
      }
    } catch (Exception e) {
      e.printStackTrace();
      fail("Unexpected exception thrown - please review stack trace.");
    } finally {
      // n/a
    }

    logInfo("Updated " + updatedTerminologies
        + " terminology names to contain version.");
    logInfo("Finished " + getName());
  }

  private void fixTerminologies() throws Exception {
    // 8/20/2019 Terminology codes, names and versions out of sync.
    // See NE-619
    logInfo(" Fix Terminologies");

    int updatedTerminologies = 0;

    try {

      // Get all terminologies
      TerminologyList terminolgyList = getTerminologies();

      setSteps(terminolgyList.getObjects().size());

      for (final Terminology terminology : terminolgyList.getObjects()) {
        String[] segments = terminology.getPreferredName().split(",");
        String newPreferredName = "";
        if (segments.length >= 2 && segments[segments.length - 1]
            .equals(segments[segments.length - 2])) {
          newPreferredName = terminology.getPreferredName().substring(0,
              terminology.getPreferredName().lastIndexOf(','));
        }

        if (!newPreferredName.equals("")) {
          terminology.setPreferredName(newPreferredName);
          updatedTerminologies++;
        }

        updateProgress();
      }
    } catch (Exception e) {
      e.printStackTrace();
      fail("Unexpected exception thrown - please review stack trace.");
    } finally {
      // n/a
    }

    logInfo("Updated " + updatedTerminologies
        + " terminology names to remove duplicate versions.");
    logInfo("Finished " + getName());
  }

  private void fixVPTAndTerminologies() throws Exception {
    // 8/26/2019 Inconsistencies between VPT and terminology names came in with
    // MTH_2019AA, and were caught by the 201908 release.
    // See NE-625
    logInfo(" Fix VPT and Terminologies");

    int updatedTerminologies = 0;
    int updatedVPTs = 0;

    try {

      // Get all terminologies
      TerminologyList terminolgyList = getTerminologies();

      setSteps(4);

      for (final Terminology terminology : terminolgyList.getObjects()) {
        // Only look through current terminologies
        if (!terminology.isCurrent()) {
          continue;
        }
        String newPreferredName = "";
        if (terminology.getPreferredName()
            .equals("National Drug File, FDASPL, 2018_02_05_18_12_03")) {
          newPreferredName = "National Drug File - FDASPL, 2018_02_05";
        } else if (terminology.getPreferredName()
            .equals("National Drug File, FMTSME, 2018_02_05_18_12_03")) {
          newPreferredName = "National Drug File - FMTSME, 2018_02_05";
        } else if (terminology.getPreferredName()
            .equals("Vaccines Administered, 2017_02_08, 2018_10_18")) {
          newPreferredName = "Vaccines Administered, 2018_10_18, 2019_03_04";
        }
        // Not one of the terminologies we need to change
        else {
          continue;
        }

        if (!newPreferredName.equals("")) {
          terminology.setPreferredName(newPreferredName);
          updateTerminology(terminology);
          updatedTerminologies++;

          updateProgress();
        }

      }

      Atom atom = getAtom(8902115L);
      atom.setName("NCBI Taxonomy, 2018_04_19");
      updateAtom(atom);
      updatedVPTs++;
      updateProgress();

      atom = getAtom(11042820L);
      atom.setName("Vaccines Administered, 2018_10_18, 2019_03_04");
      updateAtom(atom);
      updatedVPTs++;
      updateProgress();

      commitClearBegin();

    } catch (Exception e) {
      e.printStackTrace();
      fail("Unexpected exception thrown - please review stack trace.");
    } finally {
      // n/a
    }

    logInfo("Updated " + updatedTerminologies + " terminology names.");
    logInfo("Updated " + updatedVPTs + " VPT names.");
    logInfo("Finished " + getName());
  }

  private void fixNCBIVPT() throws Exception {
    // 03/20/2020 Inconsistencies between VPT and terminology names came in with
    // MTH_2019AB, and were caught by the 202003 release.
    logInfo(" Fix NCBI VPT name");

    int updatedTerminologies = 0;
    int updatedVPTs = 0;

    try {

      setSteps(1);

      Atom atom = getAtom(11881919L);
      atom.setName("NCBI Taxonomy, 2019_05_08");
      updateAtom(atom);
      updatedVPTs++;
      updateProgress();

      commitClearBegin();

    } catch (Exception e) {
      e.printStackTrace();
      fail("Unexpected exception thrown - please review stack trace.");
    } finally {
      // n/a
    }

    logInfo("Updated " + updatedVPTs + " VPT names.");
    logInfo("Finished " + getName());
  }

  private void fixRHTAtoms() throws Exception {
    // 9/20/2018 Issues identified where RHT atoms had terminology of 'NCIMTH',
    // instead of 'SRC'.
    logInfo(" Fix RHT Atoms");

    int updatedAtoms = 0;
    List<Atom> atoms = new ArrayList<>();

    try {

      // Get the three affected additional relationship types
      Query query = getEntityManager().createNativeQuery(
          "select id from atoms where termType='RHT' and terminology='NCIMTH'");

      List<Object> list = query.getResultList();
      for (final Object entry : list) {
        final Long atomId = Long.valueOf(entry.toString());
        atoms.add(getAtom(atomId));
      }

      setSteps(atoms.size());

      logInfo("[FixRHTAtoms] " + atoms.size() + " atoms identified");

      for (final Atom atom : atoms) {

        // Set the terminology to SRC
        if (atom.getTerminology().equals("NCIMTH")) {
          atom.setTerminology("SRC");
          updateAtom(atom);
          updatedAtoms++;
        }
        // We should never get here
        else {
          logError("WHAT HAPPENED!!!????");
        }
        updateProgress();
      }
    } catch (Exception e) {
      e.printStackTrace();
      fail("Unexpected exception thrown - please review stack trace.");
    } finally {
      // n/a
    }

    logInfo(
        "Updated " + updatedAtoms + " additional relationship types updated.");
    logInfo("Finished " + getName());
  }

  private void fixMDRDescriptors() throws Exception {
    // 9/27/2018 Discovered that existing MDR descriptors weren't getting during
    // insertion, but instead new ones were being created.
    // We need to:
    // 1) Remove MDR descriptors not brought over by MEME4 loader
    // 2) Set MDR descriptors loaded by MEME4 that are publishable=false to
    // publishable=true (so they can be picked up by the atom loader)
    // 3) Re-run the atom loader to update reused descriptors to current version
    // (done in separate Atom Loader algo)
    // 4) Re-run attribute loader for SDUI attributes, to ensure all descriptor
    // attributes are attached accordingly (done in separate Replace Attribute
    // algo)
    // 5) Set descriptors that were not updated to publishable=false (done in
    // separate Update Releasibility algo)

    int removedDescriptors = 0;
    int updatedDescriptors = 0;

    try {

      // Remove descriptors created by insertions
      Query query = getEntityManager().createNativeQuery(
          "select id from descriptors where terminology='MDR' and version!='20_0'");
      List<Object> list = query.getResultList();

      // Set publishable=false MDR descriptors to publishable=true
      Query query2 = getEntityManager().createNativeQuery(
          "select id from descriptors where terminology='MDR' and version='20_0' and publishable=false");
      List<Object> list2 = query2.getResultList();

      setSteps(list.size() + list2.size());

      logInfo(" Remove MDR descriptors created by insertions");

      logInfo("[FixMDRDesciptors] " + list.size()
          + " descriptors created by insertions identified");

      for (final Object entry : list) {
        final Long descriptorId = Long.valueOf(entry.toString());
        removeDescriptor(descriptorId);
        removedDescriptors++;
        updateProgress();
      }

      logInfo(" Set MDR descriptors loaded from MEME4 to publishable=true");
      logInfo("[FixMDRDesciptors] " + list2.size()
          + " descriptors that need to be set to publishable=true");

      for (final Object entry : list2) {
        final Long descriptorId = Long.valueOf(entry.toString());
        Descriptor descriptor = getDescriptor(descriptorId);
        descriptor.setPublishable(true);
        updateDescriptor(descriptor);
        updatedDescriptors++;
        updateProgress();
      }

    } catch (Exception e) {
      e.printStackTrace();
      fail("Unexpected exception thrown - please review stack trace.");
    } finally {
      // n/a
    }

    logInfo("Removed " + removedDescriptors
        + " MDR descriptors created by insertions.");
    logInfo("Updated " + updatedDescriptors
        + " loaded MDR descriptors tp publishable=true.");
    logInfo("Finished " + getName());

  }

  private void removeOldWorklistsChecklists() throws Exception {
    // 10/22/2018 Remove old worklists and checklists that should have
    // been removed during release process cleanup

    int removals = 0;

    WorkflowService workflowService = new WorkflowServiceJpa();
    workflowService.setLastModifiedBy("admin");

    Set<Long> worklistIdsToRemove = new HashSet<>();
    Set<Long> checklistIdsToRemove = new HashSet<>();

    // Get worklists
    Query query = getEntityManager()
        .createQuery("select a.id from " + "WorklistJpa a where epoch = '17b'");

    logInfo("[RemoveOldWorklistsChecklists] Loading ");

    List<Object> list = query.getResultList();
    for (final Object entry : list) {
      final Long id = Long.valueOf(entry.toString());
      worklistIdsToRemove.add(id);
    }

    // Get checklists
    query = getEntityManager().createQuery(
        "select a.id from " + "ChecklistJpa a where timestamp < '2018-08-23'");

    logInfo("[RemoveOldWorklistsChecklists] Loading ");

    list = query.getResultList();
    for (final Object entry : list) {
      final Long id = Long.valueOf(entry.toString());
      checklistIdsToRemove.add(id);
    }

    setSteps(checklistIdsToRemove.size() + worklistIdsToRemove.size());

    logInfo("[RemoveOldWorklistsChecklists] " + checklistIdsToRemove.size()
        + " checklists to be removed");
    logInfo("[RemoveOldWorklistsChecklists] " + worklistIdsToRemove.size()
        + " worklists to be removed");

    // Remove checklists
    for (Long id : checklistIdsToRemove) {
      logInfo("[RemoveOldWorklistsChecklists] " + id);
      workflowService.removeChecklist(id, true);
      updateProgress();
      removals++;
    }

    // Remove worklists
    for (Long id : worklistIdsToRemove) {
      logInfo("[RemoveOldWorklistsChecklists] " + id);
      workflowService.removeWorklist(id, true);
      updateProgress();
      removals++;
    }

    logInfo("[RemoveOldWorklistsChecklists] " + removals
        + " lists successfully removed.");

  }

  private void removeSNOMEDSubsets() throws Exception {

    logInfo(" Remove Duplicate Subset Member Attributes");

    int updatedRelationships = 0;

    final List<ConceptSubsetJpa> conceptSubsets = new ArrayList<>();
    final List<AtomSubsetJpa> atomSubsets = new ArrayList<>();
    try {
      Query query = getEntityManager().createNativeQuery(
          "select id from concept_subsets where terminology=:terminology and version=:version");
      query.setParameter("terminology", "SNOMEDCT_US");
      query.setParameter("version", "2019_03_01");

      List<Object> list = query.getResultList();
      for (final Object entry : list) {
        final Long id = Long.valueOf(entry.toString());
        conceptSubsets
            .add((ConceptSubsetJpa) getSubset(id, ConceptSubsetJpa.class));
      }

      setSteps(conceptSubsets.size());

      logInfo("[RemoveSNOMEDSubsets] " + conceptSubsets.size()
          + " Concept Subsets identified");

      for (final ConceptSubsetJpa subset : conceptSubsets) {
        logInfo("[RemoveSNOMEDSubsets] " + subset.getMembers().size()
            + " Before removal concept Subset Members  identified on: "
            + subset.getTerminologyId() + " " + subset.getId());
        for (final ConceptSubsetMember member : subset.getMembers()) {
          for (final Attribute att : member.getAttributes()) {
            removeAttribute(att.getId());
          }
          member.setAttributes(null);
          updateSubsetMember(member);
          removeSubsetMember(member.getId(), ConceptSubsetMemberJpa.class);
        }
        subset.clearMembers();
        updateSubset(subset);
        removeSubset(subset.getId(), ConceptSubsetJpa.class);
        logInfo("[RemoveSNOMEDSubsets] " + subset.getMembers().size()
            + " After removal concept Subset Members  identified on: "
            + subset.getTerminologyId() + " " + subset.getId());
        updateProgress();
      }
    } catch (Exception e) {
      e.printStackTrace();
      fail("Unexpected exception thrown - please review stack trace.");
    } finally {
    }

    logInfo(
        "Updated " + updatedRelationships + " component info relationships.");
    logInfo("Finished " + getName());
  }

  private void removeSNOMEDAtomSubsets() throws Exception {

    logInfo(" Remove SNOMED Atom Subsets");

    int totalSubsetMembersSize = 0;
    final List<AtomSubsetJpa> atomSubsets = new ArrayList<>();
    try {
      Query query = getEntityManager().createNativeQuery(
          "select id from atom_subsets where terminology=:terminology and version=:version");
      query.setParameter("terminology", "SNOMEDCT_US");
      query.setParameter("version", "2019_03_01");

      List<Object> list = query.getResultList();
      for (final Object entry : list) {
        final Long id = Long.valueOf(entry.toString());
        AtomSubsetJpa atomSubset =
            (AtomSubsetJpa) getSubset(id, AtomSubsetJpa.class);
        atomSubsets.add(atomSubset);
        totalSubsetMembersSize += atomSubset.getMembers().size();
      }

      setSteps(totalSubsetMembersSize);

      logInfo("[RemoveSNOMEDSubsets] " + atomSubsets.size()
          + " Atom Subsets identified");
      logInfo("[RemoveSNOMEDSubsets] " + totalSubsetMembersSize
          + " Atom Subset Members identified");

      // handle lazy init error
      for (final AtomSubsetJpa subset : atomSubsets) {
        for (final AtomSubsetMember member : subset.getMembers()) {
          member.getAttributes().size();
        }
      }

      for (final AtomSubsetJpa subset : atomSubsets) {
        logInfo("[RemoveSNOMEDSubsets] " + subset.getMembers().size()
            + " Before removal atom Subset Members  identified on: "
            + subset.getTerminologyId() + " " + subset.getId());
        for (final AtomSubsetMember member : subset.getMembers()) {
          for (final Attribute att : member.getAttributes()) {
            removeAttribute(att.getId());
          }
          member.setAttributes(null);
          updateSubsetMember(member);
          removeSubsetMember(member.getId(), AtomSubsetMemberJpa.class);

          updateProgress();
        }
        subset.clearMembers();
        updateSubset(subset);
        removeSubset(subset.getId(), AtomSubsetJpa.class);
        logInfo("[RemoveSNOMEDSubsets] " + subset.getMembers().size()
            + " After removal atom Subset Members  identified on: "
            + subset.getTerminologyId() + " " + subset.getId());
      }
    } catch (Exception e) {
      e.printStackTrace();
      fail("Unexpected exception thrown - please review stack trace.");
    } finally {
    }

    logInfo("Removed " + totalSubsetMembersSize + " atom subset members.");
    logInfo("Finished " + getName());

    logInfo("Finished " + getName());
  }

  private void removeConceptsWithoutAtoms() throws Exception {
    // 11/20/2018 Mark unpublishable shell concepts that have no atoms

    logInfo(" Mark Unpublishable Concepts without Atoms");

    int markedConcepts = 0;

    List<ConceptSubsetJpa> conceptsWithoutAtoms = new ArrayList<>();

    try {
      Query query = getEntityManager().createQuery("select c1.id from "
          + "ConceptJpa c1 where c1.terminology = :terminology and c1.publishable=true and c1.id NOT IN (select c2.id from ConceptJpa c2 JOIN c2.atoms)");
      query.setParameter("terminology", "NCIMTH");
      conceptsWithoutAtoms = query.getResultList();
      setSteps(conceptsWithoutAtoms.size());

      for (final Object entry : conceptsWithoutAtoms) {

        final Long id = Long.valueOf(entry.toString());
        Concept concept = getConcept(id);
        if (concept == null) {
          logWarn(
              "[AdHoc Algorithm] Concept designated to be marked unpublished that is null:"
                  + id);
          continue;
        }
        concept.setPublishable(false);
        for (Definition def : concept.getDefinitions()) {
          def.setPublishable(false);
          updateDefinition(def, concept);
        }
        for (Attribute att : concept.getAttributes()) {
          att.setPublishable(false);
          updateAttribute(att, concept);
        }
        for (ConceptRelationship rel : concept.getInverseRelationships()) {
          rel.setPublishable(false);
          updateRelationship(rel);
        }
        for (ConceptRelationship rel : concept.getRelationships()) {
          rel.setPublishable(false);
          updateRelationship(rel);
        }
        for (SemanticTypeComponent sty : concept.getSemanticTypes()) {
          sty.setPublishable(false);
          updateSemanticTypeComponent(sty, concept);
        }
        for (ConceptSubsetMember member : concept.getMembers()) {
          member.setPublishable(false);
          updateSubsetMember(member);
        }
        for (ConceptTreePosition treePos : concept.getTreePositions()) {
          treePos.setPublishable(false);
          updateTreePosition(treePos);
        }
        concept.setNotes(null);
        updateConcept(concept);

        updateProgress();
        markedConcepts++;
      }

    } catch (Exception e) {
      e.printStackTrace();
      fail("Unexpected exception thrown - please review stack trace.");
    } finally {

    }
    logInfo(
        "Marked unpublishable " + markedConcepts + " concepts without atoms.");
    logInfo("Finished " + getName());

  }

  private void fixDuplicatePDQMappingAttributes() throws Exception {
    // 11/28/2018 Mark unpublishable older pdq mapping attributes

    logInfo(" Mark Unpublishable Older PDQ Mapping Attributes");

    int markedAttributes = 0;

    List<Code> pdqNciMappingCodes = new ArrayList<>();

    try {

      Query query = getEntityManager().createQuery("select a.id from "
          + "CodeJpa a "
          + "where a.terminology = :terminology and a.name like :name or name = 'name'");
      query.setParameter("name", "PDQ%to NCI%Mappings");
      query.setParameter("terminology", "PDQ");
      List<Object> list = query.getResultList();
      // get codes
      for (final Object entry : list) {
        final Long id = Long.valueOf(entry.toString());
        Code code = getCode(id);
        pdqNciMappingCodes.add(code);
        if (code.getName().equals("name")) {
          code.setName("PDQ_2016_07_31 to NCI_2018_07E Mappings");
          updateCode(code);
        }
      }
      // get older code
      Code olderCode = pdqNciMappingCodes.get(0);
      for (Code code : pdqNciMappingCodes) {
        if (code.getLastModified().before(olderCode.getLastModified())) {
          olderCode = code;
        }
      }
      // turn off all attributes on older code
      for (Attribute att : olderCode.getAttributes()) {
        att.setPublishable(false);
        updateAttribute(att, olderCode);
        markedAttributes++;
      }
      commitClearBegin();

    } catch (Exception e) {
      e.printStackTrace();
      fail("Unexpected exception thrown - please review stack trace.");
    } finally {

    }
    logInfo("Marked unpublishable " + markedAttributes
        + " duplicate PDQ mapping attributes.");
    logInfo("Finished " + getName());

  }

  private void fixDuplicateConcepts() throws Exception {
    // 5/7/2018 These were created erroneously during the load from MEME4
    // (having to do with the loading of component_histories and dead CUIs),
    // and need to be taken care of.

    logInfo(" Fix duplicate concepts");

    List<Concept> duplicateConcepts = new ArrayList<>();

    try {

      // Identify all relationship identities that have duplicates
      // REAL QUERY
      Query query = getEntityManager().createNativeQuery(
          "select id from concepts where terminology='NCIMTH' group by "
              + "terminologyId having count(*) > 1");

      logInfo("[FixDuplicateConcepts] Identifying " + "duplicate concepts");

      List<Object> list = query.getResultList();
      for (final Object entry : list) {
        final Long id = Long.valueOf(entry.toString());
        Concept cpt = getConcept(id);
        duplicateConcepts.add(cpt);
      }

      setSteps(duplicateConcepts.size());

      logInfo("[FixDuplicateConcepts] " + duplicateConcepts.size()
          + " Concept duplicates identified");

      for (final Concept concept : duplicateConcepts) {
        query = getEntityManager().createNativeQuery(
            "select id from concepts where terminologyId = :terminologyId");
        query.setParameter("terminologyId", concept.getTerminologyId());

        List<Concept> conceptsWithSameCUI = new ArrayList<>();
        Concept namedConceptToKeep = null;
        Set<ComponentHistory> componentHistoriesToMove = new HashSet<>();
        list = query.getResultList();
        for (final Object entry : list) {
          final Long id = Long.valueOf(entry.toString());
          Concept cpt = getConcept(id);

          conceptsWithSameCUI.add(cpt);
          if (!cpt.getName().isEmpty()) {
            namedConceptToKeep = cpt;
          } else {
            componentHistoriesToMove.addAll(cpt.getComponentHistory());
            cpt.setComponentHistory(null);
            updateConcept(cpt);
            for (Definition def : cpt.getDefinitions()) {
              removeDefinition(def.getId());
            }
            for (Attribute att : cpt.getAttributes()) {
              removeAttribute(att.getId());
            }
            for (ConceptRelationship rel : cpt.getInverseRelationships()) {
              removeRelationship(rel.getId(), rel.getClass());
            }
            for (ConceptRelationship rel : cpt.getRelationships()) {
              removeRelationship(rel.getId(), rel.getClass());
            }
            for (SemanticTypeComponent sty : cpt.getSemanticTypes()) {
              removeSemanticTypeComponent(sty.getId());
            }
            for (ComponentHistory history : cpt.getComponentHistory()) {
              removeComponentHistory(history.getId());
            }
            for (ConceptSubsetMember member : cpt.getMembers()) {
              removeSubsetMember(member.getId(), member.getClass());
            }
            for (ConceptTreePosition treePos : cpt.getTreePositions()) {
              removeTreePosition(treePos.getId(), treePos.getClass());
            }
            cpt.setNotes(null);
            removeConcept(cpt.getId());
          }
        }
        List<ComponentHistory> namedConceptComponentHistories =
            namedConceptToKeep.getComponentHistory();
        namedConceptComponentHistories.addAll(componentHistoriesToMove);
        updateConcept(namedConceptToKeep);
        updateProgress();
      }
    } catch (Exception e) {
      e.printStackTrace();
      fail("Unexpected exception thrown - please review stack trace.");
    } finally {

    }

    logInfo("Finished " + getName());

  }

  /*
   * private void fixDuplicateConcepts() throws Exception { // 5/7/2018 These
   * were created erroneously during the load from MEME4 // (having to do with
   * the loading of component_histories and dead CUIs), // and need to be taken
   * care of.
   * 
   * logInfo(" Fix duplicate concepts");
   * 
   * List<ConceptRelationship> duplicateConcepts = new ArrayList<>();
   * 
   * try {
   * 
   * // Identify all relationship identities that have duplicates // REAL QUERY
   * Query query = getEntityManager().createNativeQuery(
   * "select id from concept_relationships where publishable and terminology != 'NCIMTH' limit 55"
   * );
   * 
   * 
   * logInfo("[TEST] ");
   * 
   * List<Object> list = query.getResultList(); int ct = 0; for (final Object
   * entry : list) { final Long id = Long.valueOf(entry.toString());
   * ConceptRelationship cptRel = (ConceptRelationship)getRelationship(id,
   * ConceptRelationshipJpa.class); duplicateConcepts.add(cptRel); ct++; if (ct
   * == 50) { break; } }
   * 
   * setSteps(duplicateConcepts.size());
   * 
   * logInfo("[TEST] " + duplicateConcepts.size() +
   * " Concept duplicates identified");
   * 
   * for (final ConceptRelationship rel : duplicateConcepts) {
   * System.out.println("rel before: " + rel);
   * rel.getAlternateTerminologyIds().clear(); updateRelationship(rel);
   * //updateProgress(); //System.out.println("rel after: " + rel); }
   * 
   * commitClearBegin(); } catch (Exception e) { e.printStackTrace(); fail(
   * "Unexpected exception thrown - please review stack trace."); } finally {
   * 
   * }
   * 
   * logInfo("Finished " + getName());
   * 
   * }
   */

  private void fixNullRUIs() throws Exception {
    // 12/17/2018 Ensure all components are attached to their identity.
    // Concept rels inexplicably got null RUIs and the RUIs need to be
    // reassigned.

    logInfo(" Fix null RUIs");

    List<Long> relsToFix = new ArrayList<>();
    Map<String, String> relTypeMap = new HashMap<>();

    IdentifierAssignmentHandler handler =
        newIdentifierAssignmentHandler(getProject().getTerminology());
    handler.setTransactionPerOperation(false);
    handler.beginTransaction();

    try {

      for (final RelationshipType rel : getRelationshipTypes(
          getProject().getTerminology(), getProject().getVersion())
              .getObjects()) {
        relTypeMap.put(rel.getAbbreviation(),
            rel.getInverse().getAbbreviation());
      }
      for (final AdditionalRelationshipType rel : getAdditionalRelationshipTypes(
          getProject().getTerminology(), getProject().getVersion())
              .getObjects()) {
        relTypeMap.put(rel.getAbbreviation(),
            rel.getInverse().getAbbreviation());
      }

      // Identify all concept relationships with null alternate terminology ids
      // (RUIs)
      // REAL QUERY
      Query query = getEntityManager().createNativeQuery(
          "select cr.id from concept_relationships cr left join conceptrelationshipjpa_alternateterminologyids crat on cr.id=crat.ConceptRelationshipJpa_id where cr.publishable and crat.alternateTerminologyIds is null and terminology != 'NCIMTH'");

      List<Object> list = query.getResultList();

      for (final Object entry : list) {
        final Long id = Long.valueOf(entry.toString());
        relsToFix.add(id);
      }

      logInfo("[FixNullRUIs] " + relsToFix.size()
          + " Concept relationships identified with null RUIs");

      setSteps(relsToFix.size());
      for (Long relId : relsToFix) {
        ConceptRelationship relationship =
            (ConceptRelationship) getRelationship(relId,
                ConceptRelationshipJpa.class);

        if (!relationship.getTerminology().equals("MTH")) {
          continue;
        }
        final String inverseRelType =
            relTypeMap.get(relationship.getRelationshipType());
        final String inverseAdditionalRelType =
            relTypeMap.get(relationship.getAdditionalRelationshipType());
        final String relationshipRui = handler.getTerminologyId(relationship,
            inverseRelType, inverseAdditionalRelType);
        relationship.getAlternateTerminologyIds().size();
        relationship.getAlternateTerminologyIds()
            .put(getProject().getTerminology(), relationshipRui);
        updateRelationship(relationship);
        updateProgress();
      }

      commitClearBegin();
      handler.commit();
      handler.close();

    } catch (Exception e) {
      e.printStackTrace();
      handler.rollback();
      handler.close();
      fail("Unexpected exception thrown - please review stack trace.");
    } finally {

    }

    logInfo("Finished " + getName());

  }

  private void removeOldMTHRelationships() throws Exception {
    // 12/15/2018 concept_relationships were created by MTH_2018AB test
    // insertion that duplicated existing concept_relationships from old
    // insertions.
    // Remove the old relationships.
    // 12/10/2019 - still having same issue in MTH_2019AB. Updated to new
    // version.
    // 05/28/2020 - still having same issue in MTH_2020AA. Updated to new
    // version.

    logInfo(" Remove old MTH relationships");

    Query query = getEntityManager().createNativeQuery("select cr.id from "
        + " concept_relationships cr, concepts c1, concepts c2 "
        + " where cr.from_id = c1.id " + " and cr.to_id = c2.id "
        + " AND from_id < to_id " + " and cr.terminology = 'MTH' "
        + " and cr.terminology != '2020AA' " + " and c1.terminology = 'NCIMTH' "
        + " and c2.terminology = 'NCIMTH' "
        + " GROUP BY c1.terminologyId, c2.terminologyId HAVING COUNT(*) > 1");

    logInfo("[RemoveOldMTHRelationships] Loading "
        + "ConceptRelationship ids for old relationships that now have duplicates caused by the MTH 2018AB insertion");

    List<Object> list = query.getResultList();
    setSteps(list.size());
    logInfo("[RemoveOldMTHRelationships] " + list.size()
        + " ConceptRelationship ids loaded");

    for (final Object entry : list) {
      final Long id = Long.valueOf(entry.toString());
      removeRelationship(id, ConceptRelationshipJpa.class);
      updateProgress();
    }

    logInfo("Finished " + getName());

  }

  private void removeOldRelationships() throws Exception {
    // 07/24/2020 duplicate bequeathal concept rels with older ones getting removed

    logInfo(" Remove old relationships");

    Query query = getEntityManager().createNativeQuery("select cr.id from "
        + " concept_relationships cr, concepts c1, concepts c2 "
        + " where cr.from_id = c1.id " + " and cr.to_id = c2.id "
        + " AND from_id < to_id  " + " and c1.terminology = 'NCIMTH' "
        + " and c2.terminology = 'NCIMTH' "
        + " GROUP BY c1.terminologyId, c2.terminologyId HAVING COUNT(*) > 1");

    logInfo("[RemoveOldRelationships] Loading "
        + "ConceptRelationship ids for old relationships that now have duplicates caused by the MTH 2018AB insertion");

    List<Object> list = query.getResultList();
    setSteps(list.size());
    logInfo("[RemoveOldRelationships] " + list.size()
        + " ConceptRelationship ids loaded");

    for (final Object entry : list) {
      final Long id = Long.valueOf(entry.toString());
      removeRelationship(id, ConceptRelationshipJpa.class);
      updateProgress();
    }

    logInfo("Finished " + getName());

  }

  private void assignMissingStyAtui() throws Exception {
    // 1/8/2019 ATUI is missing on STY C0303976|T104|A1.4.1.2|Chemical Viewed
    // Structurally|||
    // May need to be run ONLY DURING RELEASE PROCESS if
    // AssignReleaseIdentifiersAlgorithm misses it again

    logInfo(" Assign missing sty ATUI");

    logInfo("[AssignMissingStyAtui] Assigning "
        + "missing ATUI to publishable concept semantic type component");

    // get handler
    final IdentifierAssignmentHandler handler =
        getIdentifierAssignmentHandler(getProject().getTerminology());

    // Assign ATUIs for semantic types
    final javax.persistence.Query query = manager.createQuery(
        "select c.id, s.id from ConceptJpa c join c.semanticTypes s "
            + "where c.terminology = :terminology "
            + "  and c.version = :version and s.terminologyId = '' and c.publishable = true ");
    query.setParameter("terminology", getProject().getTerminology());
    query.setParameter("version", getProject().getVersion());
    @SuppressWarnings("unchecked")
    final List<Object[]> ids = query.getResultList();

    setSteps(ids.size());
    for (final Object[] result : ids) {
      final Concept c = getConcept(Long.valueOf(result[0].toString()));
      final SemanticTypeComponent sty =
          getSemanticTypeComponent(Long.valueOf(result[1].toString()));

      if (sty == null) {
        logInfo("sty is null " + result.toString() + " " + c.toString());
      }
      logInfo("[AssignMissingStyAtui]  " + c.getTerminologyId() + " "
          + sty.getId());
      // For each semantic type component (e.g. concept.getSemanticTypes())
      final String origAtui = sty.getTerminologyId();
      sty.setTerminologyId("");

      final String atui = handler.getTerminologyId(sty, c);
      logInfo("[AssignMissingStyAtui] atui= " + atui);
      if (!origAtui.equals(atui)) {
        sty.setTerminologyId(atui);
        updateSemanticTypeComponent(sty, c);
      } else {
        sty.setTerminologyId(origAtui);
      }

      updateProgress();
    }
    commitClearBegin();
    updateProgress();
    logInfo("Finished " + getName());

  }

  private void fixComponentHistoryVersion() throws Exception {
    // 1/16/2019 ComponentHistory version should match associatedRelease, not be
    // 'latest'

    logInfo(" Fix ComponentHistory Version");

    logInfo("[FixComponentHistoryVersion] Assigning "
        + "associatedRelease to version field");

    int updatedHistories = 0;

    final List<ComponentHistoryJpa> componentHistories = new ArrayList<>();

    try {
      Query query = getEntityManager().createNativeQuery(
          "select id from component_histories where version='latest'");

      logInfo("[FixComponentHistoryVersion] Identifying "
          + "ComponentHistories with version 'latest'");

      List<Object> list = query.getResultList();
      for (final Object entry : list) {
        final Long id = Long.valueOf(entry.toString());
        componentHistories.add(
            (ComponentHistoryJpa) getComponent(id, ComponentHistoryJpa.class));
      }

      setSteps(componentHistories.size());

      logInfo("[FixComponentHistoryVersion] " + componentHistories.size()
          + " ComponentHistories with version 'latest'");

      for (final ComponentHistory componentHistory : componentHistories) {

        componentHistory.setVersion(componentHistory.getAssociatedRelease());
        updateComponent(componentHistory);
        updatedHistories++;
        updateProgress();
      }
    } catch (Exception e) {
      e.printStackTrace();
      fail("Unexpected exception thrown - please review stack trace.");
    } finally {
    }

    logInfo("Updated " + updatedHistories + " component histories.");
    logInfo("Finished " + getName());
  }

  private void fixAdditionalRelTypeInverses2() throws Exception {
    // 1/25/2019 Issues identified with additional relationship types where
    // new one was added, but old one was not made unpublishable or detached
    // from inverse.
    // set the old inverse to pubishable=false.
    // Update inverse to point to the new correct one.
    // 6/2019 Add functionality to address more Invalid
    // additional_relationship_types
    // including those related to NICHD and CDRH
    logInfo(" Fix Additional Rel Type Inverses 2");

    int updatedAdditionalRelationshipTypes = 0;
    int updatedRelationships = 0;
    List<AdditionalRelationshipTypeJpa> additionalRelationshipsTypes =
        new ArrayList<>();
    List<AtomRelationshipJpa> atomRelationships = new ArrayList<>();

    try {

      // Get the three affected additional relationship types
      Query query = getEntityManager().createNativeQuery(
          "select abbreviation from additional_relationship_types where id in (1398,1399,1322352,1260,1259,598402,327351,327352,598404)");

      List<Object> list = query.getResultList();
      for (final Object entry : list) {
        final String abbreviation = entry.toString();
        additionalRelationshipsTypes
            .add((AdditionalRelationshipTypeJpa) getAdditionalRelationshipType(
                abbreviation, "NCIMTH", "latest"));
      }

      setSteps(additionalRelationshipsTypes.size());

      logInfo("[FixAdditionalRelTypeInverses2] "
          + additionalRelationshipsTypes.size()
          + " additional relationship types identified");

      for (final AdditionalRelationshipType additionalRelationshipType : additionalRelationshipsTypes) {

        // Set the no-longer-referenced additionalRelationshipType to
        // publishable=false
        if (additionalRelationshipType.getId() == 1398) {
          additionalRelationshipType.setPublishable(false);
          updateAdditionalRelationshipType(additionalRelationshipType);
          updatedAdditionalRelationshipTypes++;
        }
        // Set the one incorrectly-inverted additional relationship type to its
        // correct inverse
        else if (additionalRelationshipType.getId() == 1399) {
          AdditionalRelationshipType inverseRelType =
              getAdditionalRelationshipType("develops_into", "NCIMTH",
                  "latest");
          additionalRelationshipType.setInverse(inverseRelType);
          updateAdditionalRelationshipType(additionalRelationshipType);
          updatedAdditionalRelationshipTypes++;
        }
        // Set another incorrectly-inverted additional relationship type to its
        // correct inverse
        // Confirm that Parent_Is_NICHD is inverse of Has_NICHD_Parent
        else if (additionalRelationshipType.getId() == 1260) {
          AdditionalRelationshipType inverseRelType =
              getAdditionalRelationshipType("Parent_Is_NICHD", "NCIMTH",
                  "latest");
          additionalRelationshipType.setInverse(inverseRelType);
          updateAdditionalRelationshipType(additionalRelationshipType);
          updatedAdditionalRelationshipTypes++;
        }
        // Parent_Is_NICHD (publishable)
        else if (additionalRelationshipType.getId() == 1259) {
          additionalRelationshipType.setPublishable(true);
          updateAdditionalRelationshipType(additionalRelationshipType);
          updatedAdditionalRelationshipTypes++;
        }
        // Parent_Is_CDRH (publishable)
        else if (additionalRelationshipType.getId() == 327352) {
          additionalRelationshipType.setPublishable(true);
          updateAdditionalRelationshipType(additionalRelationshipType);
          updatedAdditionalRelationshipTypes++;
        }
        // NICHD_Parent_Of (not publishable)
        else if (additionalRelationshipType.getId() == 598402) {
          additionalRelationshipType.setPublishable(false);
          updateAdditionalRelationshipType(additionalRelationshipType);
          updatedAdditionalRelationshipTypes++;
        }
        // CDRH_Parent_Of (not publishable)
        else if (additionalRelationshipType.getId() == 598404) {
          additionalRelationshipType.setPublishable(false);
          updateAdditionalRelationshipType(additionalRelationshipType);
          updatedAdditionalRelationshipTypes++;
        }
        // We should never get here
        else {
          logError("WHAT HAPPENED!!!????");
        }
        updateProgress();
      }

      // update atom_relationships from 'gives_rise_to' to 'develops_into'
      query = getEntityManager().createNativeQuery(
          "select id from atom_relationships where additionalRelationshipType = 'gives_rise_to'");

      List<Object> ids = query.getResultList();

      for (final Object result : ids) {
        final Relationship<?, ?> rel =
            (AtomRelationship) getRelationship(Long.valueOf(result.toString()),
                AtomRelationshipJpa.class);
        rel.setAdditionalRelationshipType("develops_into");
        updateRelationship(rel);
        updatedRelationships++;
      }

    } catch (Exception e) {
      e.printStackTrace();
      fail("Unexpected exception thrown - please review stack trace.");
    } finally {
      // n/a
    }

    logInfo("Updated " + updatedAdditionalRelationshipTypes
        + " additional relationship types updated 2.");
    logInfo("Updated " + updatedRelationships + " relationships updated 2.");
    logInfo("Finished " + getName());
  }

  private void fixAdditionalRelTypeInverses3() throws Exception {
    // 7/24/2020 fix concept_relationships that have old inverse_has_units rela
    
    logInfo(" Fix Additional Rel Type Inverses 3");

    int updatedRelationships = 0;

    try {
 
      // update atom_relationships from 'gives_rise_to' to 'develops_into'
      Query query = getEntityManager().createNativeQuery(
          "select id from concept_relationships where additionalRelationshipType = 'inverse_has_units'");

      List<Object> ids = query.getResultList();

      for (final Object result : ids) {
        final Relationship<?, ?> rel =
            (ConceptRelationship) getRelationship(Long.valueOf(result.toString()),
                ConceptRelationshipJpa.class);
        rel.setAdditionalRelationshipType("units_of");
        updateRelationship(rel);
        updatedRelationships++;
      }

    } catch (Exception e) {
      e.printStackTrace();
      fail("Unexpected exception thrown - please review stack trace.");
    } finally {
      // n/a
    }

    logInfo("Updated " + updatedRelationships + " relationships updated 3.");
    logInfo("Finished " + getName());
  }
  
  
  private void removeDemotions() throws Exception {
    // 3/22/2019 Clean up demotions that should have been removed during concept
    // approval.

    logInfo(" Remove Demotions");

    try {

      Query query = getEntityManager().createNativeQuery("select "
          + "ar.id relId, " + "cr.to_id c1Id, " + "cr.from_id c2Id " + "from "
          + "atom_relationships ar, " + "concept_relationships cr, "
          + "concepts_atoms ca1, " + "concepts_atoms ca2 " + "where "
          + "ar.terminology = 'NCIMTH' " + "and cr.terminology = 'NCIMTH' "
          + "and ar.from_id = ca1.atoms_id " + "and ar.to_id = ca2.atoms_id "
          + "and cr.from_id = ca1.concepts_id "
          + "and cr.to_id = ca2.concepts_id "
          + "and ar.workflowStatus = 'DEMOTION' "
          + "and cr.workflowStatus in ('READY_FOR_PUBLICATION', 'PUBLISHED') "

      );

      final List<Object[]> ids = query.getResultList();

      setSteps(ids.size());
      for (final Object[] result : ids) {
        ;
        final Relationship<?, ?> rel = (AtomRelationship) getRelationship(
            Long.valueOf(result[0].toString()), AtomRelationshipJpa.class);
        final Concept c1 = getConcept(Long.valueOf(result[1].toString()));
        final Concept c2 = getConcept(Long.valueOf(result[2].toString()));

        logInfo("Remove demotion: " + rel.getId() + " between " + c1.getId()
            + " and " + c2.getId());
        if (rel != null) {
          removeRelationship(rel.getId(), AtomRelationshipJpa.class);
        }
        updateProgress();
      }

    } catch (Exception e) {
      e.printStackTrace();
      fail("Unexpected exception thrown - please review stack trace.");
    } finally {
      // n/a
    }

    logInfo("Finished " + getName());
  }

  private boolean noXRRel(Concept a, Concept b) {
    for (ConceptRelationship cr : a.getRelationships()) {
      if (cr.getRelationshipType().equals("XR")
          && (cr.getFrom().getId() == b.getId()
              || cr.getTo().getId() == b.getId())) {
        System.out.println("found XR rel: " + a.getId() + " " + b.getId());
        return false;
      }
    }
    return true;
  }

  private void reviseSemanticTypes() throws Exception {
    // 11/30/2019 Revise for SNOMED insertion the stys for 'Alergy to...'
    // concepts
    // 6/25/2019 Revise for SNOMED insertion the stys for '

    logInfo(" Revise semantic types");

    List<Concept> conceptToBeRevised = new ArrayList<>();

    try {

      /*
       * Query query = getEntityManager().createNativeQuery(
       * "select concepts.id from concepts, concepts_atoms, atoms, concepts_semantic_type_components, semantic_type_components "
       * +
       * " where concepts.name like '% only product in % dose form%' and concepts.lastModifiedBy = 'SNOMEDCT_US_2019_03_01' "
       * + " and concepts.workflowStatus = 'NEEDS_REVIEW' " +
       * " and concepts.terminology = 'NCIMTH' " +
       * " and concepts.id = concepts_semantic_type_components.concepts_id " +
       * " and concepts_semantic_type_components.semanticTypes_id  = semantic_type_components.id "
       * + " and semantic_type_components.semanticType != 'Clinical Drug' " +
       * " and concepts.id not in ( " +
       * " select concepts.id from concepts, concepts_atoms, atoms " +
       * " where concepts.id = concepts_atoms.concepts_id " +
       * " and concepts_atoms.atoms_id = atoms.id " +
       * " and atoms.workflowStatus = 'DEMOTION') " +
       * " and concepts.id = concepts_atoms.concepts_id " +
       * " and atoms.id = concepts_atoms.atoms_id " +
       * " and atoms.terminology = 'SNOMEDCT_US' " +
       * " group by atoms.codeId having count(distinct(atoms.codeId)) = 1");
       */

      Query query =
          getEntityManager().createNativeQuery(" select distinct concepts.id "
              + " from concepts, atoms, concepts_atoms, concepts_semantic_type_components, semantic_type_components "
              + " where concepts.name like '% in % dosage form%' "
              + " and concepts.workflowStatus = 'NEEDS_REVIEW' "
              + " and concepts.terminology = 'NCIMTH' "
              + " and concepts.lastModifiedBy = 'NCIMTH_latest' "
              + " and concepts.id = concepts_atoms.concepts_id "
              + " and atoms.id = concepts_atoms.atoms_id "
              + " and concepts.id = concepts_semantic_type_components.concepts_id "
              + " and concepts_semantic_type_components.semanticTypes_id  = semantic_type_components.id "
              + " and semantic_type_components.semanticType != 'Clinical Drug';");

      logInfo("[ReviseSemanticTypes] Identifying concepts with incorrect stys");

      List<Object> list = query.getResultList();
      for (final Object entry : list) {
        final Long id = Long.valueOf(entry.toString());
        Concept cpt = getConcept(id);
        conceptToBeRevised.add(cpt);
      }

      setSteps(conceptToBeRevised.size());

      logInfo("[ReviseSemanticTypes] " + conceptToBeRevised.size()
          + " Concepts with incorrect stys identified");

      RemoveSemanticTypeMolecularAction action =
          new RemoveSemanticTypeMolecularAction();
      AddSemanticTypeMolecularAction addAction =
          new AddSemanticTypeMolecularAction();

      for (Concept concept : conceptToBeRevised) {
        logInfo("[ReviseSemanticTypes] " + concept);

        // Authorize project role, get userName
        final String userName = "DSS";
        try {
          action = new RemoveSemanticTypeMolecularAction();

          // Retrieve the project
          final Project project = action.getProject(39751L);
          if (!project.isEditingEnabled()) {
            throw new LocalException(
                "Editing is disabled on project: " + project.getName());
          }

          // prevent stale concept
          concept = getConcept(concept.getId());

          for (SemanticTypeComponent sty : concept.getSemanticTypes()) {
            // Configure the action
            action.setProject(project);
            action.setActivityId("AdHocReviseSTYS");
            action.setConceptId(concept.getId());
            action.setConceptId2(null);
            action.setLastModifiedBy("E-" + userName);
            action.setLastModified(concept.getLastModified().getTime());
            action.setOverrideWarnings(true);
            action.setTransactionPerOperation(false);
            action.setMolecularActionFlag(true);
            action.setChangeStatusFlag(false);

            action.setSemanticTypeComponentId(sty.getId());

            // Perform the action
            final ValidationResult validationResult =
                action.performMolecularAction(action, userName, true, false);

            // If the action failed, bail out now.
            if (!validationResult.isValid()) {
              logInfo(
                  "[ReviseSemanticTypes] validation error " + validationResult);
              throw new Exception();
            }

            commitClearBegin();

            // prevent stale concept - get concept's revised lastModified time
            concept = getConcept(concept.getId());
          }
        } catch (Exception e) {
          e.printStackTrace();
          fail("Unexpected exception thrown - please review stack trace.");
        } finally {
          action.close();
        }

        // prevent stale concept
        concept = getConcept(concept.getId());

        // Instantiate services
        try {
          addAction = new AddSemanticTypeMolecularAction();

          // Retrieve the project
          final Project project = addAction.getProject(39751L);
          if (!project.isEditingEnabled()) {
            throw new LocalException(
                "Editing is disabled on project: " + project.getName());
          }

          // Create semantic type component
          final SemanticTypeComponent sty = new SemanticTypeComponentJpa();
          sty.setTerminologyId("");
          sty.setObsolete(false);
          sty.setPublishable(true);
          sty.setPublished(false);
          sty.setWorkflowStatus(WorkflowStatus.PUBLISHED);
          sty.setSemanticType("Clinical Drug");
          sty.setTerminology("NCIMTH");
          sty.setVersion("latest");
          sty.setTimestamp(new Date());

          // Configure the addAction
          addAction.setProject(project);
          addAction.setActivityId("AdHocReviseSTYS");
          addAction.setConceptId(concept.getId());
          addAction.setConceptId2(null);
          addAction.setLastModifiedBy("E-" + userName);
          addAction.setLastModified(concept.getLastModified().getTime());
          addAction.setOverrideWarnings(true);
          addAction.setTransactionPerOperation(false);
          addAction.setMolecularActionFlag(true);
          addAction.setChangeStatusFlag(false);

          addAction.setSemanticTypeComponent(sty);

          // Perform the addAction
          final ValidationResult validationResult = addAction
              .performMolecularAction(addAction, userName, true, false);

          // If the addAction failed, bail out now.
          if (!validationResult.isValid()) {
            logInfo(
                "[ReviseSemanticTypes] validation error " + validationResult);
            throw new Exception();
          }
        } catch (Exception e) {
          e.printStackTrace();
          fail("Unexpected exception thrown - please review stack trace.");
        } finally {
          addAction.close();
        }

        updateProgress();
        commitClearBegin();
      }
    } catch (Exception e) {
      e.printStackTrace();
      fail("Unexpected exception thrown - please review stack trace.");
    } finally {

    }

    logInfo("Finished " + getName());

  }

  private void fixAtomLastReleaseCui() throws Exception {
    // 08/22/2019 The previous FeedbackReleaseAlgorithm ignored unpublishable
    // atoms, and so some didn't have their last release CUI updated
    // (atom.getConceptTerminologyIds().get('NCIMTH'))
    // Load the 201904 MRCONSO, and update unpublishable atoms' last release CUI
    // 3/9/2020 This is still an issue, so update AdHoc to have version be
    // variable

    logInfo(" Fix atom last release CUI");

    int updatedAtomCount = 0;

    try {

      logInfo("[FixAtomLastReleaseCUI] Loading the AUI/CUI map for "
          + getProcess().getVersion() + " MRCONSO");

      Map<String, String> auiCuiMap = new HashMap<>();

      // Check the mr directory
      String mrPath = config.getProperty("source.data.dir") + "/"
          + getProcess().getInputPath() + "/" + getProcess().getVersion()
          + "/META";

      final File mrDirFile = new File(mrPath);
      if (!mrDirFile.exists()) {
        throw new Exception(
            "Specified input directory does not exist = " + mrPath);
      }

      final List<String> lines =
          loadFileIntoStringList(mrDirFile, "MRCONSO.RRF", null, null, null);

      final String fields[] = new String[19];

      for (final String line : lines) {
        FieldedStringTokenizer.split(line, "|", 19, fields);

        auiCuiMap.put(fields[7], fields[0]);

      }

      logInfo("[FixAtomLastReleaseCUI] Finsihed loading the AUI/CUI map for "
          + getProcess().getVersion() + " MRCONSO");

      Query query = getEntityManager()
          .createNativeQuery(" select id from atoms where publishable=false");

      logInfo("[FixAtomLastReleaseCUI] Loading all unpublishable atoms");

      List<Object> list = query.getResultList();
      setSteps(list.size());

      logInfo("[FixAtomLastReleaseCUI] " + list.size()
          + " unpublishable atoms to have their last release CUIs checked to see if they need to be updated");

      for (final Object entry : list) {
        final Long id = Long.valueOf(entry.toString());
        Atom atom = getAtom(id);
        String atomAUI = atom.getAlternateTerminologyIds()
            .get(getProject().getTerminology());

        // There is a very particular edge-case where an MTH/PN atom was set to
        // unpublishable by an editor prior to the last release (and is
        // therefore not represented in the AUICUI map).
        // For these, clear out the lastReleaseCUI
        if (!auiCuiMap.containsKey(atomAUI)
            && atom.getTerminology().equals("MTH")
            && atom.getTermType().equals("PN")
            && !ConfigUtility.isEmpty(atom.getConceptTerminologyIds()
                .get(getProject().getTerminology()))) {
          atom.getConceptTerminologyIds().remove(getProject().getTerminology());
          updateAtom(atom);
          updatedAtomCount++;
        }
        // Additional edge case: PDQ_2016_07_31 to NCI_YYYY_MM mapping atoms
        // were unpublishable for the last release, but didn't have their
        // lastReleaseCUI cleared out by ProdMidCleanup. Clear it out now.
        else if (!auiCuiMap.containsKey(atomAUI)
            && atom.getTerminology().equals("PDQ")
            && atom.getTermType().equals("XM") && !atom.isPublishable()) {
          atom.getConceptTerminologyIds().remove(getProject().getTerminology());
          updateAtom(atom);
          updatedAtomCount++;
        }
        // If this atom is not represented in the AUICUI map, skip it
        else if (!auiCuiMap.containsKey(atomAUI)) {
          // Do nothing - it will auto-skip due to the else-if
        }
        // Otherwise, if the atom's lastReleaseCui doesn't match the CUI in the
        // AUICUI map, update the atom
        else {
          final String atomlastReleaseCUI = atom.getConceptTerminologyIds()
              .get(getProject().getTerminology());

          if (!auiCuiMap.get(atomAUI).equals(atomlastReleaseCUI)) {
            atom.getConceptTerminologyIds().put(getProject().getTerminology(),
                auiCuiMap.get(atomAUI));
            updateAtom(atom);
            updatedAtomCount++;
          }
        }

        updateProgress();
      }

      commitClearBegin();

    } catch (Exception e) {
      e.printStackTrace();
      fail("Unexpected exception thrown - please review stack trace.");
    } finally {

    }

    logInfo("Updated " + updatedAtomCount + " atoms last release CUI.");
    logInfo("Finished " + getName());

  }

  private void fixAtomSuppressibleAndObsolete() throws Exception {
    // 9/5/2019 201908 release identified atoms that needed their Suppressible
    // and Obsolete values changed.
    // All AB atoms should be suppressible=true, and all IS and OP atoms should
    // be suppressible=true and obsolete=true
    logInfo(" Fix Atom Suppressible and Obsolete");

    int updatedAtoms = 0;
    List<Long> atomIds = new ArrayList<>();

    try {

      // Get all AB, IS, and OP atoms.
      Query query = getEntityManager().createNativeQuery(
          "select id from atoms where termType in ('AB','IS','OP')");

      List<Object> list = query.getResultList();
      for (final Object entry : list) {
        final Long atomId = Long.valueOf(entry.toString());
        atomIds.add(atomId);
      }

      setSteps(atomIds.size());

      logInfo("[FixAtomSuppressibleAndObsolete] " + atomIds.size()
          + " AB, IS, and OP atoms identified");

      for (final Long atomId : atomIds) {

        final Atom atom = getAtom(atomId);

        // Update AB atoms as needed
        if (atom.getTermType().equals("AB")) {
          if (!atom.isSuppressible()) {
            atom.setSuppressible(true);
            updateAtom(atom);
            updatedAtoms++;
          }
        }
        // Update OP and IS atoms as needed
        else if (atom.getTermType().equals("OP")
            || atom.getTermType().equals("IS")) {
          Boolean atomChanged = false;
          if (!atom.isSuppressible()) {
            atom.setSuppressible(true);
            atomChanged = true;
          }
          if (!atom.isObsolete()) {
            atom.setObsolete(true);
            atomChanged = true;
          }
          if (atomChanged) {
            updateAtom(atom);
            updatedAtoms++;
          }
        }
        // We should never get here
        else {
          logError("WHAT HAPPENED!!!????");
        }
        updateProgress();
      }
    } catch (Exception e) {
      e.printStackTrace();
      fail("Unexpected exception thrown - please review stack trace.");
    } finally {
      // n/a
    }

    logInfo("Updated " + updatedAtoms
        + " AB, IS, and OP atoms' suppressible and/or obsolete values.");
    logInfo("Finished " + getName());
  }

  private void changeNullTreePositionRelasToBlank() throws Exception {
    // 9/6/2019 201908 release threw an error where additionalRelationshipTypes
    // were set to 'null'. Change all NULL values to ""
    logInfo(" Change Null TreePosition Relas To Blank");

    int updatedTreePositions = 0;
    List<Long> treePositionsIds = new ArrayList<>();

    try {

      // atom_tree_positions that have NULL relas.
      Query query = getEntityManager().createNativeQuery(
          "select id from atom_tree_positions where additionalRelationshipType is null");

      List<Object> list = query.getResultList();
      for (final Object entry : list) {
        final Long treePositionId = Long.valueOf(entry.toString());
        treePositionsIds.add(treePositionId);
      }

      setSteps(treePositionsIds.size());

      logInfo("[ChangeNullTreePositionRelasToBlank] " + treePositionsIds.size()
          + " tree positions need to have NULL RELAs set to blank");

      for (final Long treePositionId : treePositionsIds) {

        final AtomTreePositionJpa atomTreePosition =
            (AtomTreePositionJpa) getTreePosition(treePositionId,
                AtomTreePositionJpa.class);

        if (atomTreePosition.getAdditionalRelationshipType() == null) {
          atomTreePosition.setAdditionalRelationshipType("");
          updateTreePosition(atomTreePosition);
          updatedTreePositions++;
        }
        // We should never get here
        else {
          throw new Exception("WHAT HAPPENED!!!????");
        }
        updateProgress();
      }
    } catch (Exception e) {
      e.printStackTrace();
      fail("Unexpected exception thrown - please review stack trace.");
    } finally {
      // n/a
    }

    logInfo("Updated " + updatedTreePositions + " atom tree positions.");
    logInfo("Finished " + getName());
  }

  private void initializeSourceAtomIdRanges() throws Exception {
    // 11/05/2019 Initialize the source atom id range application with the final
    // data from MEME4

    logInfo(" Initialize Source Atom Id Range App");

    int rangeCount = 0;

    try {

      logInfo("[InitializeSourceAtomIdRanges] Loading the initial ranges");

      InversionService service = new InversionServiceJpa();

      // Check the mr directory
      String mrPath = config.getProperty("source.data.dir") + "/"
          + getProcess().getInputPath() + "/" + getProcess().getVersion()
          + "/META";

      final File mrDirFile = new File(mrPath);
      if (!mrDirFile.exists()) {
        throw new Exception(
            "Specified input directory does not exist = " + mrPath);
      }

      final List<String> lines = loadFileIntoStringList(mrDirFile,
          "src_atom_id_range.out", null, null, null);

      final String fields[] = new String[4];

      Map<String, String> latestEntries = new HashMap<>();

      // unique the rows so only get the latest for each vsab
      for (final String line : lines) {
        FieldedStringTokenizer.split(line, "|", 4, fields);
        // vsab is the key, other fields comprise the value
        latestEntries.put(fields[0], line);
      }

      // now add source range for each vsab
      for (Entry<String, String> entry : latestEntries.entrySet()) {
        FieldedStringTokenizer.split(entry.getValue(), "|", 4, fields);
        SourceIdRange range = new SourceIdRangeJpa();
        range.setBeginSourceId(Long.parseLong(fields[1]));
        range.setEndSourceId(Long.parseLong(fields[2]));
        String pattern = "dd-MMM-yy";
        SimpleDateFormat df = new SimpleDateFormat(pattern);
        range.setLastModified(new Date());
        range.setTerminology(entry.getKey());
        range.setLastModifiedBy("DSS");
        range.setProject(getProject());
        range.setTimestamp(df.parse(fields[3]));

        service.setLastModifiedBy("DSS");
        service.addSourceIdRange(range);
        rangeCount++;
        commitClearBegin();
      }

    } catch (Exception e) {
      e.printStackTrace();
      fail("Unexpected exception thrown - please review stack trace.");
    } finally {
      // n/a
    }
  }

  private void removeOldTermgroups() throws Exception {
    // 11/21/2019 Remove deprecated termgroups from precedence list

    int removals = 0;

    List<String> termgroupsToRemove = new ArrayList<>();
    termgroupsToRemove.add("CVX/OP");
    termgroupsToRemove.add("CVX/OA");
    termgroupsToRemove.add("CTRP-SDC/PT");
    termgroupsToRemove.add("CTRP-SDC/SY");
    termgroupsToRemove.add("CTRP-SDC/DN");

    termgroupsToRemove.add("AAA/BB");

    // First check to make sure no atoms have the termgroups to be removed
    Query query = null;
    for (String termgroup : termgroupsToRemove) {
      query = getEntityManager().createQuery("select a.id from " + "AtomJpa a "
          + "where a.terminology = :source and a.termType = :termType");
      String source = termgroup.substring(0, termgroup.indexOf('/'));
      String termType = termgroup.substring(termgroup.indexOf('/') + 1);
      query.setParameter("source", source);
      query.setParameter("termType", termType);
      List<Object> list = query.getResultList();
      if (list.size() > 0) {
        logError("[RemoveOldTermgroups] Error due to atoms having termgroup "
            + termgroup);
        continue;
      }

      // remove termgroup from project precedence list
      getProject().getPrecedenceList().removeTerminologyTermType(source,
          termType);

      // remove termgroup from default precedence list
      query = manager.createQuery("SELECT p.id from PrecedenceListJpa p"
          + " where terminology = :terminology " + " and version = :version");
      query.setParameter("terminology", "NCIMTH");
      query.setParameter("version", "latest");

      final Long precedenceListId = (Long) query.getSingleResult();
      final PrecedenceList precedenceList = getPrecedenceList(precedenceListId);
      // Handle lazy init
      precedenceList.getTermTypeRankMap().size();
      precedenceList.getTerminologyRankMap().size();
      precedenceList.getPrecedence().getName();
      precedenceList.removeTerminologyTermType(source, termType);

      commitClearBegin();
      removals++;
    }

    logInfo(
        "[RemoveOldTermgroups] Remove deprecated termgroups from precedence list");

    setSteps(termgroupsToRemove.size());

    logInfo("[RemoveOldTermgroups] " + termgroupsToRemove.size()
        + " Termgroups to be removed");

    updateProgress();

    logInfo("[RemoveOldTermgroups] " + removals
        + " old termgroups successfully removed.");

  }

  /* see superclass */
  @Override
  public void reset() throws Exception {
    logInfo("Starting RESET " + getName());
    // n/a - No reset
    logInfo("Finished RESET " + getName());
  }

  /* see superclass */
  @Override
  public void checkProperties(Properties p) throws Exception {
    checkRequiredProperties(new String[] {
        "actionName"
    }, p);
  }

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {

    if (p.getProperty("actionName") != null) {
      actionName = String.valueOf(p.getProperty("actionName"));
    }

  }

  /**
   * Returns the parameters.
   *
   * @return the parameters
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public List<AlgorithmParameter> getParameters() throws Exception {
    final List<AlgorithmParameter> params = super.getParameters();
    AlgorithmParameter param = new AlgorithmParameterJpa("Action Name",
        "actionName", "Name of Ad Hoc Action to be performed",
        "e.g. Fix Orphan Definitions", 200, AlgorithmParameter.Type.ENUM, "");
    param.setPossibleValues(Arrays.asList("Fix Orphan Definitions",
        "Undo Stampings", "Remove Bad Relationships", "Remove SNOMED Subsets",
        "Remove SNOMED Atom Subsets", "Remove Orphaned Tracking Records",
        "Inactivate Old SRC atoms and AtomRels", "Fix SRC_ATOM_IDs",
        "Redo Molecular Actions", "Fix Bad Relationship Identities",
        "Fix Component Info Relationships", "Remove Concepts without Atoms",
        "Set Component Info Relationships To Publishable",
        "Set Stamped Worklists To Ready For Publication",
        "Add Disposition Atoms", "Fix RelGroups", "Fix Source Level Rels",
        "Fix AdditionalRelType Inverses", "Fix Snomed Family",
        "Turn off CTRP-SDC", "Fix Terminology Names", "Fix Terminologies",
        "Fix RHT Atoms", "Fix MDR Descriptors",
        "Clear Worklists and Checklists",
        "Fix Duplicate PDQ Mapping Attributes", "Fix Duplicate Concepts",
        "Fix Null RUIs", "Remove old MTH relationships", "Remove old relationships", "Assign Missing STY ATUIs",
        "Fix Component History Version", "Fix AdditionalRelType Inverses 2", "Fix AdditionalRelType Inverses 3",
        "Remove Demotions", "Revise Semantic Types",
        "Fix Atom Last Release CUI", "Fix VPT and Terminologies",
        "Fix Atom Suppressible and Obsolete",
        "Initialize Source Atom Id Range App", "Remove Deprecated Termgroups",
        "Change null treeposition Relas to blank",
        "Fix overlapping bequeathal rels","Fix NCBI VPT atom","Inactivate old tree positions"));
    params.add(param);

    return params;
  }

  /* see superclass */
  @Override
  public String getDescription() {
    return "Perform Ad Hoc Action, normally for data fixes.";
  }

  private void convertToChecklist(String name, long projectId,
    List<Long[]> results, PfsParameter pfs) throws Exception {
    WorkflowService workflowService = new WorkflowServiceJpa();
    final Project project = workflowService.getProject(projectId);
    final ChecklistList checklists = findChecklists(project, null, null);
    for (final Checklist checklist : checklists.getObjects()) {
      if (checklist.getName().equals(name)
          && checklist.getProject().equals(project)) {
        throw new LocalException("A checklist for project " + project.getName()
            + " with name " + checklist.getName() + " already exists.");

      }
    }

    // Add checklist
    final Checklist checklist = new ChecklistJpa();
    checklist.setName(name);
    checklist.setDescription(name + " description");
    checklist.setProject(project);
    checklist.setTimestamp(new Date());

    // keys should remain sorted
    final Set<Long> clustersEncountered = new HashSet<>();
    final Map<Long, List<Long>> entries = new TreeMap<>();
    for (final Long[] result : results) {
      clustersEncountered.add(result[0]);

      final PfsParameter localPfs =
          (pfs == null) ? new PfsParameterJpa() : new PfsParameterJpa(pfs);
      // Keep only prescribed range from the query
      if ((localPfs.getStartIndex() > -1
          && (clustersEncountered.size() - 1) < localPfs.getStartIndex())
          || (localPfs.getMaxResults() > -1
              && clustersEncountered.size() > localPfs.getMaxResults())) {
        continue;
      }

      if (!entries.containsKey(result[0])) {
        entries.put(result[0], new ArrayList<>());
      }
      entries.get(result[0]).add(result[1]);
    }
    clustersEncountered.clear();

    // Add tracking records
    long i = 1L;
    for (final Long clusterId : entries.keySet()) {

      final TrackingRecord record = new TrackingRecordJpa();
      record.setChecklistName(name);
      // recluster from 1
      record.setClusterId(i++);
      record.setClusterType("");
      record.setProject(project);
      record.setTerminology(project.getTerminology());
      record.setTimestamp(new Date());
      record.setVersion(project.getVersion());
      final StringBuilder sb = new StringBuilder();
      for (final Long conceptId : entries.get(clusterId)) {
        final Concept concept = getConcept(conceptId);
        record.getComponentIds().addAll(concept.getAtoms().stream()
            .map(a -> a.getId()).collect(Collectors.toSet()));
        if (!record.getOrigConceptIds().contains(concept.getId())) {
          sb.append(concept.getName()).append(" ");
        }
        record.getOrigConceptIds().add(concept.getId());

      }

      record.setIndexedData(sb.toString());
      record.setWorkflowStatus(computeTrackingRecordStatus(record, true));
      final TrackingRecord newRecord = addTrackingRecord(record);

      // Add the record to the checklist.
      checklist.getTrackingRecords().add(newRecord);

      silentIntervalCommit(toIntExact(i), logCt, commitCt);
    }

    // Add the checklist
    final Checklist newChecklist = addChecklist(checklist);
  }

  private void fixOverlappingBRORels() throws Exception {
    // 3/10/2020 - BRO relationships were created on top of existing C-level
    // relationships.
    // If the old relationship was already a bequeathal rel (BRN, BRB), remove
    // the BRO relationship
    // If the old relationships was not a bequeathal rel (RO, RN, RB), remove
    // the old relationship

    logInfo(" Fix Overlapping BRO Relationships");

    Query query = getEntityManager().createNativeQuery(
        "select r.to_id conceptId1, r.from_id conceptId2 from concept_relationships r where terminology = 'NCIMTH' group by r.to_id, r.from_id having count(*)>1");

    logInfo("[FixOverlappingBRORelationships] Loading "
        + "Concept id pairs that have overlapping BRO relationships between them");

    List<Object[]> results = query.getResultList();
    setSteps(results.size());
    logInfo("[FixOverlappingBRORelationships] " + results.size()
        + " Concept id pairs loaded");

    for (final Object[] entry : results) {

      final Concept fromConcept =
          getConcept(Long.parseLong(entry[0].toString()));
      final Concept toConcept = getConcept(Long.parseLong(entry[1].toString()));

      // Find the overlapping relationships between the two concepts
      final List<ConceptRelationship> overlappingRelationships =
          new ArrayList<>();

      for (ConceptRelationship relationship : fromConcept.getRelationships()) {
        if (relationship.getTo().getId().equals(toConcept.getId())) {
          overlappingRelationships.add(relationship);
        }
      }

      if (overlappingRelationships.size() < 2) {
        throw new Exception(
            "Unexpectedly unable to find multiple relationships between concepts "
                + fromConcept.getId() + ", and " + toConcept.getId());
      }

      // This fix assumes exactly 2 relationships between concepts. Any more,
      // and it will need to be handled separately.
      if (overlappingRelationships.size() > 2) {
        updateProgress();
        continue;
      }

      // Identify which was the original relationship, and which is the new
      // relationship
      ConceptRelationship originalRelationship = null;
      ConceptRelationship newRelationship = null;

      if (overlappingRelationships.get(0).getTimestamp()
          .before(overlappingRelationships.get(1).getTimestamp())) {
        originalRelationship = overlappingRelationships.get(0);
        newRelationship = overlappingRelationships.get(1);
      } else {
        originalRelationship = overlappingRelationships.get(1);
        newRelationship = overlappingRelationships.get(0);
      }

      // If the original relationship was a bequeathal relationship, remove the
      // new relationship
      if (originalRelationship.getRelationshipType().startsWith("B")) {
        fromConcept.getRelationships().remove(newRelationship);
        updateConcept(fromConcept);
        removeRelationship(newRelationship.getId(), newRelationship.getClass());
      } else {
        // Otherwise, remove the original relationship
        fromConcept.getRelationships().remove(originalRelationship);
        updateConcept(fromConcept);
        removeRelationship(originalRelationship.getId(),
            originalRelationship.getClass());
      }

      updateProgress();
    }

    logInfo("Finished " + getName());

  }

  private void inactivateOldTreePositions() throws Exception {
    // 7/29/2030 Bug identified where old tree positions were
    // not getting caught by UpdateReleasibility.
    // Set them to publishable = false.

    try {

      logInfo("  Making old version atom tree positions unpublishable");

      // Mark all non-current atom tree positions as unpublishable.
      String query = "SELECT a.id " + "FROM AtomTreePositionJpa a, TerminologyJpa t "
          + "WHERE a.terminology=t.terminology AND a.version=t.version AND a.publishable=true AND t.current = false";

      // Perform a QueryActionAlgorithm using the class and query
      QueryActionAlgorithm queryAction = new QueryActionAlgorithm();
      try {
        queryAction.setLastModifiedBy(getLastModifiedBy());
        queryAction.setLastModifiedFlag(isLastModifiedFlag());
        queryAction.setProcess(getProcess());
        queryAction.setProject(getProject());
        queryAction.setTerminology(getTerminology());
        queryAction.setVersion(getVersion());
        queryAction.setWorkId(getWorkId());
        queryAction.setActivityId(getActivityId());

        queryAction.setObjectTypeClass(AtomTreePosition.class);
        queryAction.setAction("Make Unpublishable");
        queryAction.setQueryType(QueryType.JPQL);
        queryAction.setQuery(query);

        queryAction.setTransactionPerOperation(false);
        queryAction.beginTransaction();

        //
        // Check prerequisites
        //
        ValidationResult validationResult = queryAction.checkPreconditions();
        // if prerequisites fail, return validation result
        if (!validationResult.getErrors().isEmpty()
            || (!validationResult.getWarnings().isEmpty())) {
          // rollback -- unlocks the concept and closes transaction
          queryAction.rollback();
        }
        assertTrue(validationResult.getErrors().isEmpty());

        //
        // Perform the algorithm
        //
        queryAction.compute();

        // Commit the algorithm.
        queryAction.commit();

      } catch (Exception e) {
        queryAction.rollback();
        e.printStackTrace();
        fail("Unexpected exception thrown - please review stack trace.");
      } finally {
        // Close algorithm for each loop
        queryAction.close();
      }

      
      logInfo("  Making old version concept tree positions unpublishable");

      // Mark all non-current concept tree positions as unpublishable.
      query = "SELECT a.id " + "FROM ConceptTreePositionJpa a, TerminologyJpa t "
          + "WHERE a.terminology=t.terminology AND a.version=t.version AND a.publishable=true AND t.current = false";

      // Perform a QueryActionAlgorithm using the class and query
      queryAction = new QueryActionAlgorithm();
      try {
        queryAction.setLastModifiedBy(getLastModifiedBy());
        queryAction.setLastModifiedFlag(isLastModifiedFlag());
        queryAction.setProcess(getProcess());
        queryAction.setProject(getProject());
        queryAction.setTerminology(getTerminology());
        queryAction.setVersion(getVersion());
        queryAction.setWorkId(getWorkId());
        queryAction.setActivityId(getActivityId());

        queryAction.setObjectTypeClass(ConceptTreePosition.class);
        queryAction.setAction("Make Unpublishable");
        queryAction.setQueryType(QueryType.JPQL);
        queryAction.setQuery(query);

        queryAction.setTransactionPerOperation(false);
        queryAction.beginTransaction();

        //
        // Check prerequisites
        //
        ValidationResult validationResult = queryAction.checkPreconditions();
        // if prerequisites fail, return validation result
        if (!validationResult.getErrors().isEmpty()
            || (!validationResult.getWarnings().isEmpty())) {
          // rollback -- unlocks the concept and closes transaction
          queryAction.rollback();
        }
        assertTrue(validationResult.getErrors().isEmpty());

        //
        // Perform the algorithm
        //
        queryAction.compute();

        // Commit the algorithm.
        queryAction.commit();

      } catch (Exception e) {
        queryAction.rollback();
        e.printStackTrace();
        fail("Unexpected exception thrown - please review stack trace.");
      } finally {
        // Close algorithm for each loop
        queryAction.close();
      }

      
      logInfo("  Making old version code tree positions unpublishable");

      // Mark all non-current code tree positions as unpublishable.
      query = "SELECT a.id " + "FROM CodeTreePositionJpa a, TerminologyJpa t "
          + "WHERE a.terminology=t.terminology AND a.version=t.version AND a.publishable=true AND t.current = false";

      // Perform a QueryActionAlgorithm using the class and query
      queryAction = new QueryActionAlgorithm();
      try {
        queryAction.setLastModifiedBy(getLastModifiedBy());
        queryAction.setLastModifiedFlag(isLastModifiedFlag());
        queryAction.setProcess(getProcess());
        queryAction.setProject(getProject());
        queryAction.setTerminology(getTerminology());
        queryAction.setVersion(getVersion());
        queryAction.setWorkId(getWorkId());
        queryAction.setActivityId(getActivityId());

        queryAction.setObjectTypeClass(CodeTreePosition.class);
        queryAction.setAction("Make Unpublishable");
        queryAction.setQueryType(QueryType.JPQL);
        queryAction.setQuery(query);

        queryAction.setTransactionPerOperation(false);
        queryAction.beginTransaction();

        //
        // Check prerequisites
        //
        ValidationResult validationResult = queryAction.checkPreconditions();
        // if prerequisites fail, return validation result
        if (!validationResult.getErrors().isEmpty()
            || (!validationResult.getWarnings().isEmpty())) {
          // rollback -- unlocks the concept and closes transaction
          queryAction.rollback();
        }
        assertTrue(validationResult.getErrors().isEmpty());

        //
        // Perform the algorithm
        //
        queryAction.compute();

        // Commit the algorithm.
        queryAction.commit();

      } catch (Exception e) {
        queryAction.rollback();
        e.printStackTrace();
        fail("Unexpected exception thrown - please review stack trace.");
      } finally {
        // Close algorithm for each loop
        queryAction.close();
      }

      
      logInfo("  Making old version descriptor tree positions unpublishable");

      // Mark all non-current atom tree positions as unpublishable.
      query = "SELECT a.id " + "FROM DescriptorTreePositionJpa a, TerminologyJpa t "
          + "WHERE a.terminology=t.terminology AND a.version=t.version AND a.publishable=true AND t.current = false";

      // Perform a QueryActionAlgorithm using the class and query
      queryAction = new QueryActionAlgorithm();
      try {
        queryAction.setLastModifiedBy(getLastModifiedBy());
        queryAction.setLastModifiedFlag(isLastModifiedFlag());
        queryAction.setProcess(getProcess());
        queryAction.setProject(getProject());
        queryAction.setTerminology(getTerminology());
        queryAction.setVersion(getVersion());
        queryAction.setWorkId(getWorkId());
        queryAction.setActivityId(getActivityId());

        queryAction.setObjectTypeClass(DescriptorTreePosition.class);
        queryAction.setAction("Make Unpublishable");
        queryAction.setQueryType(QueryType.JPQL);
        queryAction.setQuery(query);

        queryAction.setTransactionPerOperation(false);
        queryAction.beginTransaction();

        //
        // Check prerequisites
        //
        ValidationResult validationResult = queryAction.checkPreconditions();
        // if prerequisites fail, return validation result
        if (!validationResult.getErrors().isEmpty()
            || (!validationResult.getWarnings().isEmpty())) {
          // rollback -- unlocks the concept and closes transaction
          queryAction.rollback();
        }
        assertTrue(validationResult.getErrors().isEmpty());

        //
        // Perform the algorithm
        //
        queryAction.compute();

        // Commit the algorithm.
        queryAction.commit();

      } catch (Exception e) {
        queryAction.rollback();
        e.printStackTrace();
        fail("Unexpected exception thrown - please review stack trace.");
      } finally {
        // Close algorithm for each loop
        queryAction.close();
      }
      
      
      logInfo("Finished " + getName());

    } catch (

    Exception e) {
      logError("Unexpected problem - " + e.getMessage());
      throw e;
    }

  }
  
  
}