/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.jpa.algo.action.UpdateConceptStatusMolecularAction;
import com.wci.umls.server.jpa.algo.maint.MatrixInitializerAlgorithm;
import com.wci.umls.server.jpa.content.ConceptRelationshipJpa;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.model.actions.AtomicAction;
import com.wci.umls.server.model.actions.MolecularAction;
import com.wci.umls.server.model.actions.MolecularActionList;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.workflow.WorkflowStatus;
import com.wci.umls.server.test.helpers.IntegrationUnitSupport;

/**
 * Sample test to get auto complete working.
 */
public class MatrixInitializerTest extends IntegrationUnitSupport {

  /** The service. */
  MatrixInitializerAlgorithm algo = null;

  /** The content service. */
  ContentServiceJpa contentService = null;

  /** The concept. */
  private Concept concept;

  /** The concept 2. */
  private Concept concept2;

  /** The relationship. */
  private ConceptRelationship relationship;

  /**
   * Setup class.
   */
  @BeforeClass
  public static void setupClass() {
    // do nothing
  }

  /**
   * Setup.
   *
   * @throws Exception the exception
   */
  @Before
  public void setup() throws Exception {
    contentService = new ContentServiceJpa();

    algo = new MatrixInitializerAlgorithm();

    // Configure the algorithm
    // algo.setActivityId - set by algorithm
    // algo.setWorkId - set by algorithm
    // algo.setUserName - use default
    // algo.setProperties - n/a
    algo.setLastModifiedBy("admin");
    algo.setLastModifiedFlag(true);
    algo.setProject(algo.getProjects().getObjects().get(0));
    algo.setTerminology("UMLS");
    algo.setVersion("latest");

    // C0000005 is PUBLISHED, and all components are PUBLISHED as well.
    concept = contentService.getConcept("C0000005", "UMLS", "latest", null);

    // C0029744 is PUBLISHED, but contains a DEMOTION relationship.
    concept2 = contentService.getConcept("C0029744", "UMLS", "latest", null);

    for (ConceptRelationship rel : concept2.getRelationships()) {
      if (rel.getWorkflowStatus().equals(WorkflowStatus.DEMOTION)) {
        relationship = rel;
      }
    }

  }

  /**
   * Test matrix init normal use.
   *
   * @throws Exception the exception
   */
  @Test
  public void testMatrixInitNormalUse() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    //
    // Prepare the test and check prerequisites
    //
    // Due to MySQL rounding to the second, we must also round our comparison
    // startDate.
    Date startDate = DateUtils.round(new Date(), Calendar.SECOND);

    //
    // Update an existing concept to ensure the algorithm will catch something.
    //

    // Save the conceptID for easier lookup later
    Long conceptId = concept.getId();

    // Ensure that the concept's workflow status is PUBLISHED
    assertEquals(WorkflowStatus.PUBLISHED, concept.getWorkflowStatus());

    // Update the WorkflowStatus of the concept to NEEDS_REVIEW
    final UpdateConceptStatusMolecularAction action =
        new UpdateConceptStatusMolecularAction();
    try {

      // Configure the action
      action.setProject(algo.getProject());
      action.setConceptId(conceptId);
      action.setConceptId2(null);
      action.setUserName("admin");
      action.setLastModified(concept.getLastModified().getTime());
      action.setOverrideWarnings(false);
      action.setTransactionPerOperation(false);
      action.setMolecularActionFlag(true);
      action.setChangeStatusFlag(false);

      action.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);

      // Perform the action
      final ValidationResult validationResult =
          action.performMolecularAction(action);
      assertTrue(validationResult.getErrors().isEmpty());

    } catch (Exception e) {
      action.rollback();
    } finally {
      action.close();
    }

    // Make sure the update went through
    contentService = new ContentServiceJpa();
    concept = contentService.getConcept(conceptId);
    assertEquals(WorkflowStatus.NEEDS_REVIEW, concept.getWorkflowStatus());

    //
    // For a second concept, set one of the concept's components to
    // NEEDS_REVIEW, to confirm that it causes the concept to update as well.
    //

    // Save the conceptID for easier lookup later
    Long conceptId2 = concept2.getId();

    Long relId = relationship.getId();

    // Ensure that the relationship's workflow status is DEMOTION
    assertEquals(WorkflowStatus.DEMOTION, relationship.getWorkflowStatus());

    // Make sure containing concept is set to PUBLISHED
     concept2 = contentService.getConcept(conceptId2);
     assertEquals(WorkflowStatus.PUBLISHED, concept2.getWorkflowStatus());

    // Send the whole project through the initializer
    try {

      //
      // Check prerequisites
      //
      ValidationResult validationResult = algo.checkPreconditions();
      // if prerequisites fail, return validation result
      if (!validationResult.getErrors().isEmpty()
          || (!validationResult.getWarnings().isEmpty())) {
        // rollback -- unlocks the concept and closes transaction
        algo.rollback();
      }
      assertTrue(validationResult.getErrors().isEmpty());

      //
      // Perform the algorithm
      //
      algo.compute();

    } catch (Exception e) {
      algo.rollback();
    } finally {
      algo.close();
    }

    // Check to make sure the concept's status was set to
    // READY_FOR_PUBLICATION
    contentService = new ContentServiceJpa();
    concept = contentService.getConcept(conceptId);
    assertEquals(WorkflowStatus.READY_FOR_PUBLICATION,
        concept.getWorkflowStatus());

    // Verify that a molecular action was created for the update
    PfsParameterJpa pfs = new PfsParameterJpa();
    pfs.setSortField("lastModified");
    pfs.setAscending(false);
    MolecularActionList list = contentService
        .findMolecularActions(concept.getId(), "UMLS", "latest", null, pfs);
    assertTrue(list.size() > 0);
    MolecularAction ma = list.getObjects().get(0);
    assertNotNull(ma);
    assertEquals(conceptId, ma.getComponentId());
    assertTrue(ma.getLastModified().compareTo(startDate) >= 0);
    assertNotNull(ma.getAtomicActions());
    assertEquals(algo.getActivityId(), ma.getActivityId());
    assertEquals(algo.getWorkId(), ma.getWorkId());

    // Verify that one atomic actions exists for updating concept workflow
    // status
    pfs.setSortField(null);

    List<AtomicAction> atomicActions =
        contentService.findAtomicActions(ma.getId(), null, pfs).getObjects();
    Collections.sort(atomicActions,
        (a1, a2) -> a1.getId().compareTo(a2.getId()));
    assertEquals(1, atomicActions.size());
    assertEquals("CONCEPT", atomicActions.get(0).getIdType().toString());
    assertNotNull(atomicActions.get(0).getOldValue());
    assertNotNull(atomicActions.get(0).getNewValue());
    assertEquals("workflowStatus", atomicActions.get(0).getField());

    // Verify that the SECOND molecular action was constructed for the second
    // concept
    // that needed updating

    // Check to make sure the concept's status set to NEEDS_REVIEW
    concept2 = contentService.getConcept(conceptId2);
    assertEquals(WorkflowStatus.NEEDS_REVIEW, concept2.getWorkflowStatus());

    // Verify that a molecular action was created for the update
    pfs = new PfsParameterJpa();
    pfs.setSortField("lastModified");
    pfs.setAscending(false);
    list = contentService.findMolecularActions(concept2.getId(), "UMLS",
        "latest", null, pfs);
    assertTrue(list.size() > 0);
    MolecularAction ma2 = list.getObjects().get(0);
    assertNotNull(ma2);
    assertEquals(concept2.getId(), ma2.getComponentId());
    assertTrue(ma2.getLastModified().compareTo(startDate) >= 0);
    assertNotNull(ma2.getAtomicActions());
    assertEquals(algo.getActivityId(), ma2.getActivityId());
    assertEquals(algo.getWorkId(), ma2.getWorkId());

    // Verify that each concept update created a different molecular action
    assertTrue(!ma.getId().equals(ma2.getId()));

    // Verify that one atomic actions exists for updating concept2 workflow
    // status
    pfs.setSortField(null);

    atomicActions =
        contentService.findAtomicActions(ma2.getId(), null, pfs).getObjects();
    Collections.sort(atomicActions,
        (a1, a2) -> a1.getId().compareTo(a2.getId()));
    assertEquals(1, atomicActions.size());
    assertEquals("CONCEPT", atomicActions.get(0).getIdType().toString());
    assertNotNull(atomicActions.get(0).getOldValue());
    assertNotNull(atomicActions.get(0).getNewValue());
    assertEquals("workflowStatus", atomicActions.get(0).getField());

    // Check that the relationship is still DEMOTION
    relationship = (ConceptRelationship) contentService.getRelationship(relId,
        ConceptRelationshipJpa.class);
    assertEquals(WorkflowStatus.DEMOTION, relationship.getWorkflowStatus());

  }

  /**
   * Test matrix init degenerate use.
   *
   * @throws Exception the exception
   */
  @Test
  public void testMatrixInitDegenerateUse() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    // Run with no project
    algo.setProject(null);
    try {
      algo.checkPreconditions();
      fail("Matrix init should fail with no project.");
    } catch (Exception e) {
      // n/a
    }
  }

  /**
   * Teardown.
   *
   * @throws Exception the exception
   */
  @After
  public void teardown() throws Exception {
    // Set all objects back to their original workflow status
    // If something fails, this can be changed to @Test and run to reset
    // everything's original status.
    if (!concept.getWorkflowStatus().equals(WorkflowStatus.PUBLISHED)) {
      final UpdateConceptStatusMolecularAction action =
          new UpdateConceptStatusMolecularAction();
      try {

        // Configure the action
        action.setProject(algo.getProject());
        action.setConceptId(concept.getId());
        action.setConceptId2(null);
        action.setUserName("admin");
        action.setLastModified(concept.getLastModified().getTime());
        action.setOverrideWarnings(false);
        action.setTransactionPerOperation(false);
        action.setMolecularActionFlag(true);
        action.setChangeStatusFlag(false);

        action.setWorkflowStatus(WorkflowStatus.PUBLISHED);

        // Perform the action
        final ValidationResult validationResult =
            action.performMolecularAction(action);
        assertTrue(validationResult.getErrors().isEmpty());

      } catch (Exception e) {
        action.rollback();
      } finally {
        action.close();
      }
    }
    contentService = new ContentServiceJpa();
    concept = contentService.getConcept(concept.getId());

    if (!concept2.getWorkflowStatus().equals(WorkflowStatus.PUBLISHED)) {
      final UpdateConceptStatusMolecularAction action2 =
          new UpdateConceptStatusMolecularAction();
      try {

        // Configure the action
        action2.setProject(algo.getProject());
        action2.setConceptId(concept2.getId());
        action2.setConceptId2(null);
        action2.setUserName("admin");
        action2.setLastModified(concept2.getLastModified().getTime());
        action2.setOverrideWarnings(false);
        action2.setTransactionPerOperation(false);
        action2.setMolecularActionFlag(true);
        action2.setChangeStatusFlag(false);

        action2.setWorkflowStatus(WorkflowStatus.PUBLISHED);

        // Perform the action
        final ValidationResult validationResult =
            action2.performMolecularAction(action2);
        assertTrue(validationResult.getErrors().isEmpty());

      } catch (Exception e) {
        action2.rollback();
      } finally {
        action2.close();
      }

    }
    contentService = new ContentServiceJpa();
    concept2 = contentService.getConcept(concept2.getId());
  }

  /**
   * Teardown class.
   */
  @AfterClass
  public static void teardownClass() {
    // do nothing
  }

}
