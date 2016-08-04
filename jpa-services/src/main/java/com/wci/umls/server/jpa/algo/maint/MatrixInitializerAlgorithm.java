/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.maint;

import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.AbstractAlgorithm;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.content.SemanticTypeComponent;
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
    setActivityId("MATRIXINIT");
    setWorkId("MAINTENANCE");
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

      // Single transaction
      setTransactionPerOperation(false);
      beginTransaction();

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

        // Staring point for changing workflow status
        WorkflowStatus status = initialStatus == WorkflowStatus.PUBLISHED
            ? WorkflowStatus.PUBLISHED : WorkflowStatus.READY_FOR_PUBLICATION;

        // Check semantic types
        for (final SemanticTypeComponent sty : concept.getSemanticTypes()) {
          if (sty.getWorkflowStatus() == WorkflowStatus.NEEDS_REVIEW) {
            status = WorkflowStatus.NEEDS_REVIEW;
            break;
          }
        }

        // Check Atoms
        if (status != WorkflowStatus.NEEDS_REVIEW) {
          for (final Atom atom : concept.getAtoms()) {
            if (atom.getWorkflowStatus() == WorkflowStatus.NEEDS_REVIEW) {
              status = WorkflowStatus.NEEDS_REVIEW;
              break;
            }
          }
        }

        // Check Relationships
        if (status != WorkflowStatus.NEEDS_REVIEW) {
          for (final ConceptRelationship rel : concept.getRelationships()) {
            if (rel.getWorkflowStatus() == WorkflowStatus.NEEDS_REVIEW) {
              status = WorkflowStatus.NEEDS_REVIEW;
              break;
            }
          }
        }

        if (status != WorkflowStatus.NEEDS_REVIEW) {
          final ValidationResult result =
              validateConcept(getProject(), concept);
          if (result.getErrors().size() != 0) {
            status = WorkflowStatus.NEEDS_REVIEW;
          }
        }

        // change either from N to R or R to N
        if (initialStatus != status) {
          concept.setWorkflowStatus(status);
          updateConcept(concept);
        }

        // Log
        if (++ct % logCt == 0) {
          logInfo("  count = " + ct);
        }

      }

      logInfo("Finished MATRIXINIT");
      commit();

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

}
