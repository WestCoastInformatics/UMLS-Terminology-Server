/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.maint;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import com.wci.umls.server.AlgorithmParameter;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.AbstractAlgorithm;
import com.wci.umls.server.jpa.algo.action.UpdateConceptMolecularAction;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.workflow.WorkflowStatus;
import com.wci.umls.server.services.handlers.SearchHandler;

/**
 * Implementation of an algorithm to perform a recomputation of Metathesaurus
 * concept status based on component status and validation.
 */
public class MatrixInitializerAlgorithm extends AbstractAlgorithm {

  /** The concept ids. */
  public Set<Long> conceptIds = null;

  /**
   * Instantiates an empty {@link MatrixInitializerAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public MatrixInitializerAlgorithm() throws Exception {
    super();
    setActivityId(UUID.randomUUID().toString());
    setWorkId("MATRIXINIT");
    setLastModifiedBy("admin");
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {

    if (getProject() == null) {
      throw new Exception("Matrix initializer requires a project to be set");
    }
    // n/a - NO preconditions
    return new ValidationResultJpa();
  }

  /* see superclass */
  @Override
  public void compute() throws Exception {
    logInfo("Starting " + getName());
    if (conceptIds != null) {
      logInfo("  update mode = " + conceptIds.size());
    } else {
      conceptIds = new HashSet<>(getAllConceptIds(getProject().getTerminology(),
          getProject().getVersion(), Branch.ROOT));
    }

    fireProgressEvent(0, "Starting...find publishable atoms");
    try {

      final SearchHandler handler = getSearchHandler(ConfigUtility.DEFAULT);

      // Get unpublishable concepts with publishable atoms
      final Set<Long> makePublishable =
          new HashSet<>(handler.getIdResults(getProject().getTerminology(),
              getProject().getVersion(), Branch.ROOT,
              "publishable:false AND atoms.publishable:true", null,
              ConceptJpa.class, null, new int[1], manager));
      checkCancel();
      fireProgressEvent(10, "Found concepts to make publishable");
      logInfo("  make publishable = " + makePublishable.size());

      // Get publishable concepts without publishable atoms
      final Set<Long> makeUnpublishable =
          new HashSet<>(handler.getIdResults(getProject().getTerminology(),
              getProject().getVersion(), Branch.ROOT,
              "publishable:true AND NOT atoms.publishable:true", null,
              ConceptJpa.class, null, new int[1], manager));
      checkCancel();
      fireProgressEvent(20, "Found concepts to make unpublishable");
      logInfo("  make unpublishable = " + makePublishable.size());

      // Find concepts connected to needs review relationships
      final Set<String> needsReviewR = new HashSet<>();
      javax.persistence.Query query =
          manager.createQuery("select r from ConceptRelationshipJpa r "
              + " where terminology = :terminology and version = :version "
              + " and workflowStatus in (  :ws )");
      query.setParameter("terminology", getProject().getTerminology());
      query.setParameter("version", getProject().getVersion());
      query.setParameter("ws", WorkflowStatus.NEEDS_REVIEW);

      @SuppressWarnings("unchecked")
      final List<ConceptRelationship> rels = query.getResultList();
      for (final ConceptRelationship rel : rels) {
        needsReviewR.add("id:" + rel.getFrom().getId());
        needsReviewR.add("id:" + rel.getTo().getId());
      }
      checkCancel();
      fireProgressEvent(30, "Find concepts with NEEDS_REVIEW relationships");
      logInfo("  need review rel = " + rels.size());

      // Perform validation and collect failed concept ids
      final Set<Long> failures =
          validateConcepts(getProject(), null, conceptIds);
      checkCancel();
      fireProgressEvent(40, "Found concepts with validation failures");
      logInfo("  validation failures = " + failures.size());

      // Find NEEDS_REVIEW concepts that should be READY_FOR_PUBLICATION
      final Set<Long> makeReviewed =
          new HashSet<>(handler.getIdResults(getProject().getTerminology(),
              getProject().getVersion(), Branch.ROOT,
              "workflowStatus:NEEDS_REVIEW AND NOT atoms.workflowStatus:NEEDS_REVIEW "
                  + "AND NOT atoms.workflowStatus:DEMOTION AND NOT semanticTypes.workflowStatus:NEEDS_REVIEW "
                  + (needsReviewR.size() == 0 ? ""
                      : " AND NOT " + ConfigUtility.composeQuery("OR",
                          new ArrayList<>(needsReviewR))),
              null, ConceptJpa.class, null, new int[1], manager));
      checkCancel();
      fireProgressEvent(50, "Found concepts to make reviewed");
      logInfo("  concepts to make reviewed = " + makeReviewed.size());

      // Find READY_FOR_PUBLICATION or PUBLISHED concepts that should be
      // NEEDS_REVIEW
      final Set<Long> makeNeedsReview =
          new HashSet<>(handler.getIdResults(getProject().getTerminology(),
              getProject().getVersion(), Branch.ROOT,
              "(workflowStatus:READY_FOR_PUBLICATION OR workflowStatus:PUBLISHED) "
                  + "AND (atoms.workflowStatus:NEEDS_REVIEW OR atoms.workflowStatus:DEMOTION "
                  + "OR semanticTypes.workflowStatus:NEEDS_REVIEW "
                  + (needsReviewR.size() == 0 ? ""
                      : " OR " + ConfigUtility.composeQuery("OR",
                          new ArrayList<>(needsReviewR)))
                  + ")",
              null, ConceptJpa.class, null, new int[1], manager));
      checkCancel();
      fireProgressEvent(60, "Found concepts to make needs review");
      logInfo("  concepts to make needs review = " + failures.size());

      final Set<Long> conceptsToChange = new HashSet<>();
      conceptsToChange.addAll(makePublishable);
      conceptsToChange.addAll(makeUnpublishable);
      conceptsToChange.addAll(makeReviewed);
      conceptsToChange.addAll(makeNeedsReview);
      conceptsToChange.addAll(failures);

      int prevProgress = 60;
      int statusChangeCt = 0;
      int publishableChangeCt = 0;
      int warningCt = 0;
      for (final Long conceptId : conceptsToChange) {
        // If in "updater" mode, skip concepts not accounted for.
        if (conceptIds != null && !conceptIds.contains(conceptId)) {
          continue;
        }

        final Concept concept = getConcept(conceptId);

        // determine status change
        int progress =
            (int) (60.0 + ((statusChangeCt * 40.0) / conceptsToChange.size()));
        if (progress != prevProgress) {
          fireProgressEvent(progress, "Iterate through concepts to change...");
          checkCancel();
          prevProgress = progress;
        }

        boolean found = false;
        Boolean publishable = null;
        if (makePublishable.contains(conceptId)) {
          publishable = true;
          logInfo("  publishable change  = " + concept.getId());
          publishableChangeCt++;
          found = true;
        }

        if (makeUnpublishable.contains(conceptId)) {
          publishable = false;
          logInfo("  publishable change  = " + concept.getId());
          publishableChangeCt++;
          found = true;
        }

        WorkflowStatus status = null;
        if (makeReviewed.contains(conceptId)) {
          status = WorkflowStatus.READY_FOR_PUBLICATION;
          logInfo("  status change  = " + concept.getId());
          statusChangeCt++;
          found = true;
        }

        if (makeNeedsReview.contains(conceptId)) {
          status = WorkflowStatus.NEEDS_REVIEW;
          warningCt++;
          statusChangeCt++;
          logInfo("  status change  = " + concept.getId());
          found = true;
        }

        if (failures.contains(conceptId)
            && concept.getWorkflowStatus() != WorkflowStatus.NEEDS_REVIEW) {
          status = WorkflowStatus.NEEDS_REVIEW;
          warningCt++;
          statusChangeCt++;
          logInfo("  status change (failure)  = " + concept.getId());
          found = true;
        }

        // If changing concept, change it
        if (found) {
          // Send change to a conceptUpdate molecular action
          final UpdateConceptMolecularAction action =
              new UpdateConceptMolecularAction();
          try {
            // Configure the action
            action.setProject(getProject());
            action.setConceptId(concept.getId());
            action.setConceptId2(null);
            action.setLastModifiedBy(getLastModifiedBy());
            action.setLastModified(concept.getLastModified().getTime());
            action.setOverrideWarnings(true);
            action.setTransactionPerOperation(false);
            action.setMolecularActionFlag(true);
            action.setChangeStatusFlag(true);

            // One or both of these will be changing..
            if (status != null) {
              action.setWorkflowStatus(status);
            }
            if (publishable != null) {
              action.setPublishable(publishable);
            }
            action.setActivityId(getActivityId());
            action.setWorkId(getWorkId());

            final ValidationResult result =
                performMolecularAction(action, getLastModifiedBy(), true);
            if (!result.isValid()) {
              throw new Exception("Invalid action - " + result);
            }

          } catch (Exception e) {
            action.rollback();
          } finally {
            action.close();
          }
        }

        if (warningCt > 0) {
          // Trigger a warning - for "pre production"
          logWarn("WARNING: some concepts were unapproved = " + warningCt);
        }

      }

      logInfo("  publishable changed = " + publishableChangeCt);
      logInfo("  status changed = " + statusChangeCt);
      fireProgressEvent(100, "Finished ...");
      logInfo("Finished " + getName());

    } catch (

    Exception e) {
      logError("Unexpected problem - " + e.getMessage());
      throw e;
    }

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
    // n/a
  }

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public List<AlgorithmParameter> getParameters() throws Exception {
    return super.getParameters();
  }

  /* see superclass */
  @Override
  public String getDescription() {
    return "Recompute concept status";
  }

  /**
   * Sets the concept ids.
   *
   * @param conceptIds the concept ids
   */
  public void setConceptIds(Set<Long> conceptIds) {
    this.conceptIds = conceptIds;
  }
}
