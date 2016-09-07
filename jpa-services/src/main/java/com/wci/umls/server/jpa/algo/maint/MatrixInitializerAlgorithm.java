/*
 *    Copyright 2016 West Coast Informatics, LLC
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
import com.wci.umls.server.jpa.algo.action.UpdateConceptStatusMolecularAction;
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

    try {

      // Get all concepts where any of its atoms are set to NEEDS REVIEW
      SearchResultList searchResult =
          this.findConcepts(getTerminology(), getVersion(), Branch.ROOT,
              "atoms.workflowStatus:" + WorkflowStatus.NEEDS_REVIEW, null);
      Set<Long> atomConceptIds = searchResult.getObjects().stream()
          .map(SearchResult::getId).collect(Collectors.toSet());

      // Get all concepts where any of its semantic type components are set to
      // NEEDS REVIEW
      searchResult = this.findConcepts(getTerminology(), getVersion(),
          Branch.ROOT,
          "semanticTypes.workflowStatus:" + WorkflowStatus.NEEDS_REVIEW, null);
      Set<Long> styConceptIds = searchResult.getObjects().stream()
          .map(SearchResult::getId).collect(Collectors.toSet());

      // Get all concepts where any of its relationships are set to NEEDS REVIEW
      // or DEMOTION
      final javax.persistence.Query query =
          manager.createQuery("select r from ConceptRelationshipJpa r "
              + " where terminology = :terminology and version = :version "
              + " and workflowStatus in ( :ws1, :ws2 )");
      query.setParameter("terminology", getTerminology());
      query.setParameter("version", getVersion());
      query.setParameter("ws1", WorkflowStatus.DEMOTION);
      query.setParameter("ws2", WorkflowStatus.NEEDS_REVIEW);

      @SuppressWarnings("unchecked")
      final List<ConceptRelationship> rels = query.getResultList();

      Set<Long> relConceptIds = new HashSet<Long>();
      for (ConceptRelationship rel : rels) {
        relConceptIds.add(rel.getFrom().getId());
        relConceptIds.add(rel.getTo().getId());
      }

      Set<Long> allNeedsReviewConceptIds = new HashSet<Long>();
      allNeedsReviewConceptIds.addAll(atomConceptIds);
      allNeedsReviewConceptIds.addAll(styConceptIds);
      allNeedsReviewConceptIds.addAll(relConceptIds);

      // Get all concepts for the terminology/version (detach).

      // NOTE: Hibernate-specific to support iterating
      // Restrict to timestamp used for THESE atoms, in case multiple RRF
      // files are loaded
      final Session session = manager.unwrap(Session.class);
      org.hibernate.Query hQuery = session.createQuery(
          "select c from ConceptJpa c " + "where terminology = :terminology "
              + "  and version = :version");
      hQuery.setParameter("terminology", getTerminology());
      hQuery.setParameter("version", getVersion());
      hQuery.setReadOnly(true).setFetchSize(2000);
      ScrollableResults results = hQuery.scroll(ScrollMode.FORWARD_ONLY);

      int ct = 0;
      while (results.next()) {
        final Concept concept =
            getConcept(((Concept) results.get()[0]).getId());

        final WorkflowStatus initialStatus = concept.getWorkflowStatus();

        // Starting point for changing workflow status
        WorkflowStatus status = initialStatus == WorkflowStatus.PUBLISHED
            ? WorkflowStatus.PUBLISHED : WorkflowStatus.READY_FOR_PUBLICATION;

        if (allNeedsReviewConceptIds.contains(concept.getId())) {
          status = WorkflowStatus.NEEDS_REVIEW;
        }

        // Check validation rules
        if (status != WorkflowStatus.NEEDS_REVIEW) {
          final ValidationResult result =
              validateConcept(getProject(), concept);
          if (result.getErrors().size() != 0) {
            status = WorkflowStatus.NEEDS_REVIEW;
          }
        }

        // change either from N to R or R to N
        if (initialStatus != status) {

          // Send change to a conceptUpdate molecular action
          final UpdateConceptStatusMolecularAction action =
              new UpdateConceptStatusMolecularAction();
          try {
            // Configure the action
            action.setProject(this.getProject());
            action.setConceptId(concept.getId());
            action.setConceptId2(null);
            action.setLastModifiedBy(getLastModifiedBy());
            action.setLastModified(concept.getLastModified().getTime());
            action.setOverrideWarnings(false);
            action.setTransactionPerOperation(false);
            action.setMolecularActionFlag(true);
            action.setChangeStatusFlag(true);

            action.setWorkflowStatus(status);
            action.setActivityId(getActivityId());
            action.setWorkId(getWorkId());

            performMolecularAction(action);

          } catch (Exception e) {
            action.rollback();
          } finally {
            action.close();
          }
        }

        // Log
        if (++ct % logCt == 0) {
          logInfo("  count = " + ct);
        }

      }

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
