/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.maint;

import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;

import com.wci.umls.server.AlgorithmParameter;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.SearchResult;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.AbstractAlgorithm;
import com.wci.umls.server.jpa.algo.action.UpdateConceptMolecularAction;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.workflow.WorkflowStatus;

/**
 * Implementation of an algorithm to perform a recomputation of Metathesaurus
 * concept status based on component status and validation.
 */
public class MatrixInitializerAlgorithm extends AbstractAlgorithm {

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
    logInfo("Starting MATRIXINIT");
    fireProgressEvent(0, "Starting...find publishable atoms");
    try {

      // Get all concepts having at least one releasable atom
      SearchResultList list = findConcepts(getTerminology(), getVersion(),
          Branch.ROOT, "atoms.publishable:true", null);
      final Set<Long> publishableConcepts = list.getObjects().stream()
          .map(SearchResult::getId).collect(Collectors.toSet());
      logInfo("  publishable = " + list.getTotalCount());

      // Get all concepts where any of its atoms are set to NEEDS REVIEW or
      // DEMOTION
      fireProgressEvent(5, "Find needs review atoms...");
      checkCancel();

      list = findConcepts(getTerminology(), getVersion(), Branch.ROOT,
          "(atoms.workflowStatus:" + WorkflowStatus.NEEDS_REVIEW
              + " OR atoms.workflowStatus:" + WorkflowStatus.DEMOTION + ")",
          null);
      final Set<Long> atomConceptIds = list.getObjects().stream()
          .map(SearchResult::getId).collect(Collectors.toSet());
      logInfo("  need review atoms = " + list.getTotalCount());

      // Get all concepts where any of its semantic type components are set to
      // NEEDS REVIEW
      fireProgressEvent(10, "Find needs review STYs...");
      checkCancel();

      list = findConcepts(getTerminology(), getVersion(), Branch.ROOT,
          "semanticTypes.workflowStatus:" + WorkflowStatus.NEEDS_REVIEW, null);
      final Set<Long> styConceptIds = list.getObjects().stream()
          .map(SearchResult::getId).collect(Collectors.toSet());
      logInfo("  need review sty = " + list.getTotalCount());

      // Get all concepts where any of its relationships are set to NEEDS REVIEW
      fireProgressEvent(15, "Find needs review relationships...");
      checkCancel();
      javax.persistence.Query query =
          manager.createQuery("select r from ConceptRelationshipJpa r "
              + " where terminology = :terminology and version = :version "
              + " and workflowStatus in (  :ws )");
      query.setParameter("terminology", getTerminology());
      query.setParameter("version", getVersion());
      query.setParameter("ws", WorkflowStatus.NEEDS_REVIEW);

      @SuppressWarnings("unchecked")
      final List<ConceptRelationship> rels = query.getResultList();
      final Set<Long> relConceptIds = new HashSet<Long>();
      for (final ConceptRelationship rel : rels) {
        relConceptIds.add(rel.getFrom().getId());
        relConceptIds.add(rel.getTo().getId());
      }
      logInfo("  need review rel = " + rels.size());

      // Perform validation and collect failed concept ids
      fireProgressEvent(20, "Find validation failures...");
      checkCancel();
      final Set<Long> conceptIds = new HashSet<>(
          getAllConceptIds(getTerminology(), getVersion(), Branch.ROOT));
      final Set<Long> failures = validateConcepts(getProject(), conceptIds);
      logInfo("  validation failures = " + failures.size());

      final Set<Long> allNeedsReviewConceptIds = new HashSet<Long>();
      allNeedsReviewConceptIds.addAll(atomConceptIds);
      allNeedsReviewConceptIds.addAll(styConceptIds);
      allNeedsReviewConceptIds.addAll(relConceptIds);
      allNeedsReviewConceptIds.addAll(failures);

      // NOTE: Hibernate-specific to support iterating
      // Restrict to timestamp used for THESE atoms, in case multiple RRF
      // files are loaded
      fireProgressEvent(25, "Iterate through all concepts...");
      checkCancel();
      final Session session = manager.unwrap(Session.class);
      org.hibernate.Query hQuery = session.createQuery(
          "select c from ConceptJpa c " + "where terminology = :terminology "
              + "  and version = :version");
      hQuery.setParameter("terminology", getTerminology());
      hQuery.setParameter("version", getVersion());
      hQuery.setReadOnly(true).setFetchSize(2000);
      ScrollableResults results = hQuery.scroll(ScrollMode.FORWARD_ONLY);

      int prevProgress = 25;
      int statusChangeCt = 0;
      int publishableChangeCt = 0;
      while (results.next()) {
        final Concept concept =
            new ConceptJpa((ConceptJpa) results.get()[0], false);

        boolean changePublishable = false;
        // Starting point for changing workflow status
        final WorkflowStatus initialStatus =
            concept.getWorkflowStatus() == WorkflowStatus.NEEDS_REVIEW
                ? WorkflowStatus.NEEDS_REVIEW
                : WorkflowStatus.READY_FOR_PUBLICATION;
        WorkflowStatus status = initialStatus;

        // determine status change
        if (allNeedsReviewConceptIds.contains(concept.getId())) {
          int progress = (int) (25.0
              + ((statusChangeCt * 75.0) / allNeedsReviewConceptIds.size()));
          if (progress != prevProgress) {
            fireProgressEvent(progress, "Iterate through all concepts...");
            checkCancel();
            prevProgress = progress;
          }
          if (status != WorkflowStatus.NEEDS_REVIEW) {
            logInfo("  status change  = " + concept.getId());
            status = WorkflowStatus.NEEDS_REVIEW;
            statusChangeCt++;
          }
        }

        // determine publishable change
        final boolean publishable =
            publishableConcepts.contains(concept.getId());
        if (publishable != concept.isPublishable()) {
          logInfo("  publishable change  = " + concept.getId());
          changePublishable = true;
          publishableChangeCt++;
        }

        // change either from N to R or R to N or publishable from true/false or
        // false/true
        if (initialStatus != status || changePublishable) {
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

            action.setWorkflowStatus(status);
            action.setPublishable(publishable);
            action.setActivityId(getActivityId());
            action.setWorkId(getWorkId());

            final ValidationResult result =
                performMolecularAction(action, getLastModifiedBy());
            if (!result.isValid()) {
              throw new Exception("Invalid action - " + result);
            }

          } catch (Exception e) {
            action.rollback();
          } finally {
            action.close();
          }
        }

      }

      logInfo("  publishable changed = " + publishableChangeCt);
      logInfo("  status changed = " + statusChangeCt);
      fireProgressEvent(100, "Finished ...");
      logInfo("Finished MATRIXINIT");

    } catch (Exception e) {
      logError("Unexpected problem - " + e.getMessage());
      throw e;
    }

  }

  /* see superclass */
  @Override
  public void reset() throws Exception {
    // n/a - No reset
  }

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    checkRequiredProperties(new String[] {
        ""
    }, p);
  }

  /* see superclass */
  @Override
  public List<AlgorithmParameter> getParameters() {
    return super.getParameters();
  }

}
