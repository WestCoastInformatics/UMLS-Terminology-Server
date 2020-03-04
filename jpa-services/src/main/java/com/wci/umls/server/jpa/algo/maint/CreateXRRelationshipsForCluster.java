/*
 *    Copyright 2019 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.maint;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Query;

import com.wci.umls.server.AlgorithmParameter;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.WorklistList;
import com.wci.umls.server.jpa.AlgorithmParameterJpa;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.AbstractInsertMaintReleaseAlgorithm;
import com.wci.umls.server.jpa.algo.action.AddRelationshipMolecularAction;
import com.wci.umls.server.jpa.content.ConceptRelationshipJpa;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.workflow.TrackingRecord;
import com.wci.umls.server.model.workflow.WorkflowStatus;
import com.wci.umls.server.model.workflow.Worklist;

/**
 * Implementation of an algorithm to create XR relationships between every
 * concept listed in a particular cluster
 */
public class CreateXRRelationshipsForCluster
    extends AbstractInsertMaintReleaseAlgorithm {

  private String worklistName;

  private Integer clusterNumber;

  /**
   * Instantiates an empty {@link CreateXRRelationshipsForCluster}.
   * @throws Exception if anything goes wrong
   */
  public CreateXRRelationshipsForCluster() throws Exception {
    super();
    setActivityId(UUID.randomUUID().toString());
    setWorkId("CREATEXRRELATIONSHIPSFORCLUSTER");
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
      throw new Exception(
          "Create XR Relationships For Cluster requires a project to be set");
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
    logInfo("Starting " + getName());
    logInfo("  worklistName = " + worklistName);
    logInfo("  clusterNumber = " + clusterNumber);
    commitClearBegin();

    int relationshipsCreated = 0;

    final Long clusterId = new Long(clusterNumber);

    try {

      // Find the specified worklist
      PfsParameter pfs = new PfsParameterJpa();
      pfs.setQueryRestriction(worklistName);
      WorklistList worklistList =
          this.findWorklists(this.getProject(), null, pfs);

      // There can be only one
      if (worklistList.getTotalCount() != 1) {
        throw new Exception("Expecting a single worklist, but search returned "
            + worklistList.getTotalCount());
      }

      Worklist worklist = worklistList.getObjects().get(0);

      logInfo("[CreateXRRelationshipsForCluster] Worklist loaded: "
          + worklist.getName());

      // Get the specified cluster
      List<TrackingRecord> trackingRecords = worklist.getTrackingRecords();

      TrackingRecord trackingRecord = null;
      for (TrackingRecord record : trackingRecords) {
        if (record.getClusterId().equals(clusterId)) {
          trackingRecord = record;
          break;
        }
      }

      // Throw error if null tracking record
      if (trackingRecord == null) {
        throw new Exception("No tracking record found for worklist="
            + worklist.getName() + " and cluster=" + clusterId);
      }

      // Get NCIMTH concepts for tracking record's atoms
      Query query = getEntityManager()
          .createQuery("select c.id from ConceptJpa c join c.atoms a "
              + "where c.terminology=:projectTerminology and c.version=:projectVersion and a.id in (:atomIds)");
      query.setParameter("projectTerminology", getProject().getTerminology());
      query.setParameter("projectVersion", getProject().getVersion());
      query.setParameter("atomIds", trackingRecord.getComponentIds());

      Set<Concept> concepts = new HashSet<>();

      List<Object> results = query.getResultList();
      for (final Object result : results) {
        final Concept concept =
            this.getConcept(Long.valueOf(result.toString()));
        concepts.add(concept);
      }

      // Throw error if concepts empty
      if (concepts.size() == 0) {
        throw new Exception(
            "No NCIMTH concepts associated with this tracking record.");
      }

      // Set the number of steps to the number of possible pairs (n * (n-1)) / 2
      setSteps(concepts.size() * (concepts.size() - 1) / 2);

      // Track which concept-pairs have already been processed, to avoid
      // unnecessary re-work
      final Set<String> processedConceptPairs = new HashSet<>();

      // Cache which concepts are already related
      final Map<Long, Set<Long>> existingRelationships = new HashMap<>();

      for (Concept concept : concepts) {
        existingRelationships.put(concept.getId(), new HashSet<>());
        for (ConceptRelationship relationship : concept.getRelationships()) {
          existingRelationships.get(concept.getId())
              .add(relationship.getTo().getId());
        }
      }

      // Create XR relationships between all the NCIMTH concepts, IFF there
      // isn't already a relationship between them.
      for (Concept fromConcept : concepts) {
        for (Concept toConcept : concepts) {
          // Don't create self-referential relationships
          if (fromConcept.getId().equals(toConcept.getId())) {
            continue;
          }

          // Don't re-process concepts that have already been looked at
          if (processedConceptPairs
              .contains(fromConcept.getId() + "|" + toConcept.getId())
              || processedConceptPairs
                  .contains(toConcept.getId() + "|" + fromConcept.getId())) {
            continue;
          }

          // Add these concepts to the already-processed list (in both
          // directions)
          processedConceptPairs
              .add(fromConcept.getId() + "|" + toConcept.getId());
          processedConceptPairs
              .add(toConcept.getId() + "|" + fromConcept.getId());

          // Refresh to and from concepts (they may have been modified by a
          // previous step, and the actions need accurate lastModified
          // time-stamps)
          fromConcept = getConcept(fromConcept.getId());
          toConcept = getConcept(toConcept.getId());

          // Don't create XR relationships if a concept-relationship already
          // exists
          if (existingRelationships.get(fromConcept.getId())
              .contains(toConcept.getId())) {
            updateProgress();
            continue;
          }

          // Instantiate services
          final AddRelationshipMolecularAction action =
              new AddRelationshipMolecularAction();
          ConceptRelationship relationship = new ConceptRelationshipJpa();
          try {

            // XR relationships are unpublishable
            relationship.setPublished(false);
            relationship.setPublishable(false);
            relationship.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);

            relationship.setTerminology("NCIMTH");
            relationship.setVersion("latest");
            relationship.setFrom(fromConcept);
            relationship.setTo(toConcept);
            relationship.setRelationshipType("XR");
            relationship.setAdditionalRelationshipType("");
            relationship.setTerminologyId("");

            // Set defaults for a concept level relationship
            relationship.setStated(true);
            relationship.setInferred(true);
            relationship.setSuppressible(false);
            relationship.setObsolete(false);
            relationship.setGroup("");

            // Configure the action
            action.setProject(getProject());
            action.setActivityId("createXRRelationshipsForCluster");
            action.setConceptId(relationship.getFrom().getId());
            action.setConceptId2(relationship.getTo().getId());
            action.setLastModifiedBy("admin");
            action.setLastModified(
                relationship.getFrom().getLastModified().getTime());
            action.setOverrideWarnings(true);
            action.setTransactionPerOperation(false);
            action.setMolecularActionFlag(true);
            action.setChangeStatusFlag(true);

            action.setRelationship(relationship);

            // Perform the action
            final ValidationResult validationResult =
                action.performMolecularAction(action, "admin", true, false);

            // If the action failed, bail out now.
            if (!validationResult.isValid()) {
              logError("Unexpected problem - " + validationResult);
            }

            // Otherwise, increment the successful add-atom count, and add the
            // concept ids to the relationship cache
            relationshipsCreated++;
            existingRelationships.get(fromConcept.getId())
                .add(toConcept.getId());
            existingRelationships.get(toConcept.getId())
                .add(fromConcept.getId());

          } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected exception thrown - please review stack trace.");
          } finally {
            action.close();
          }

          updateProgress();
          commitClearBegin();
        }
      }

    } catch (Exception e) {
      logError("Unexpected problem - " + e.getMessage());
      throw e;
    } finally {
      // n/a
    }

    logInfo("Created " + relationshipsCreated + " XR relationships.");
    logInfo("Finished " + getName());

  }

  /**
   * Reset.
   *
   * @throws Exception the exception
   */
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
        "worklistName", "clusterNumber"
    }, p);
  }

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {

    if (p.getProperty("worklistName") != null) {
      worklistName = String.valueOf(p.getProperty("worklistName"));
    }
    if (p.getProperty("clusterNumber") != null) {
      clusterNumber =
          Integer.parseInt(String.valueOf(p.getProperty("clusterNumber")));
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

    AlgorithmParameter param =
        new AlgorithmParameterJpa("Worklist Name", "worklistName",
            "Name of the worklist", "e.g. wrk19b_ambig_no_rel_default_003", 40,
            AlgorithmParameter.Type.ENUM, "");

    // Only list ambig_no_rel worklists
    PfsParameter pfs = new PfsParameterJpa();
    pfs.setQueryRestriction("ambig_no_rel*");
    WorklistList worklistList = findWorklists(getProject(), null, pfs);

    List<String> worklistNames = new ArrayList<>();
    for (final Worklist aWorklist : worklistList.getObjects()) {
      // Add worklist name to ENUM list
      worklistNames.add(aWorklist.getName());
    }
    param.setPossibleValues(worklistNames);
    params.add(param);

    param = new AlgorithmParameterJpa("Cluster Number", "clusterNumber",
        "Cluster Number", "e.g. 37", 10, AlgorithmParameter.Type.INTEGER, "50");
    params.add(param);

    return params;
  }

  /* see superclass */
  @Override
  public String getDescription() {
    return "Create XR relationships between all concepts listed in a cluster";
  }

}