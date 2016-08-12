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
import com.wci.umls.server.jpa.algo.action.UpdateAtomMolecularAction;
import com.wci.umls.server.jpa.algo.action.UpdateConceptStatusMolecularAction;
import com.wci.umls.server.jpa.algo.maint.MatrixInitializerAlgorithm;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.model.actions.AtomicAction;
import com.wci.umls.server.model.actions.MolecularAction;
import com.wci.umls.server.model.actions.MolecularActionList;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Concept;
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

  /**  The atom. */
  private Atom atom;
  
  /** The concept initial status. */
  private WorkflowStatus conceptInitialStatus;

  /** The concept 2 initial status. */
  private WorkflowStatus concept2InitialStatus;

  /**  The atom initial status. */
  private WorkflowStatus atomInitialStatus;
  
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

    concept = contentService.getConcept("C0000294", "UMLS", "latest", null);
    conceptInitialStatus = concept.getWorkflowStatus();

    concept2 = contentService.getConcept("C0036442", "UMLS", "latest", null);
    concept2InitialStatus = concept2.getWorkflowStatus();
    
    atom = concept2.getAtoms().get(0);
    atomInitialStatus = atom.getWorkflowStatus();
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

    // Ensure that the concept's workflow status is READY_FOR_PUBLICATION
    assertEquals(WorkflowStatus.READY_FOR_PUBLICATION,
        concept.getWorkflowStatus());

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
      action.setChangeStatusFlag(true);

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

    Long atomId = atom.getId();

    // Ensure that the atom's workflow status is PUBLISHED
    assertEquals(WorkflowStatus.PUBLISHED, atom.getWorkflowStatus());

    atom.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);

    // Update the WorkflowStatus of the atom
    final UpdateAtomMolecularAction action2 = new UpdateAtomMolecularAction();
    try {

      // Configure the action
      action2.setProject(algo.getProject());
      action2.setConceptId(conceptId2);
      action2.setConceptId2(null);
      action2.setUserName("admin");
      action2.setLastModified(concept2.getLastModified().getTime());
      action2.setOverrideWarnings(false);
      action2.setTransactionPerOperation(false);
      action2.setMolecularActionFlag(true);
      action2.setChangeStatusFlag(true);

      action2.setAtom(atom);
      
      // Perform the action
      final ValidationResult validationResult =
          action2.performMolecularAction(action2);
      assertTrue(validationResult.getErrors().isEmpty());

    } catch (Exception e) {
      action2.rollback();
    } finally {
      action2.close();
    }

    // Make sure the update went through
    contentService = new ContentServiceJpa();
    atom = contentService.getAtom(atomId);
    assertEquals(WorkflowStatus.NEEDS_REVIEW, atom.getWorkflowStatus());

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

    // Check to make sure the concept's status was reset to READY_FOR_PUBLICATION
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

    // Verify that the SECOND molecular action was constructed for the second concept
    // that needed updating

    // Check to make sure the concept's status set to NEEDS_REVIEW
    contentService = new ContentServiceJpa();
    concept2 = contentService.getConcept(conceptId2);
    assertEquals(WorkflowStatus.NEEDS_REVIEW,
        concept2.getWorkflowStatus());

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
    // if anything's workflow status has changed from what it
    // started as, reset it
    if (!concept.getWorkflowStatus().equals(conceptInitialStatus)) {
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
        action.setChangeStatusFlag(true);

        action.setWorkflowStatus(conceptInitialStatus);

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

    if (!concept2.getWorkflowStatus().equals(concept2InitialStatus)) {
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
        action2.setChangeStatusFlag(true);

        action2.setWorkflowStatus(concept2InitialStatus);

        // Perform the action
        final ValidationResult validationResult =
            action2.performMolecularAction(action2);
        assertTrue(validationResult.getErrors().isEmpty());

      } catch (Exception e) {
        action2.rollback();
      } finally {
        action2.close();
      }


      if (!atom.getWorkflowStatus().equals(atomInitialStatus)) {
        final UpdateAtomMolecularAction action =
            new UpdateAtomMolecularAction();
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
          action.setChangeStatusFlag(true);

          action.setAtom(atom);

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
      
    }

  }


  @Test
  public void reset() throws Exception {
    // if either of the concepts' workflow status has changed from what it
    // started as, reset it
    if (!concept.getWorkflowStatus().equals(WorkflowStatus.READY_FOR_PUBLICATION)) {
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
        action.setChangeStatusFlag(true);

        action.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);

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
        action2.setChangeStatusFlag(true);

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

  }  
  
  /**
   * Teardown class.
   */
  @AfterClass
  public static void teardownClass() {
    // do nothing
  }

}
