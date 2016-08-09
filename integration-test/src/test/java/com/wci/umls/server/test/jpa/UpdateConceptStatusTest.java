/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.Project;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.ProjectList;
import com.wci.umls.server.jpa.algo.action.UpdateConceptStatusMolecularAction;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.jpa.services.ProjectServiceJpa;
import com.wci.umls.server.model.actions.AtomicAction;
import com.wci.umls.server.model.actions.MolecularAction;
import com.wci.umls.server.model.actions.MolecularActionList;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.workflow.WorkflowStatus;
import com.wci.umls.server.test.helpers.IntegrationUnitSupport;

/**
 * Unit testing for {@link UpdateConceptStatusMolecularAction}.
 */
public class UpdateConceptStatusTest extends IntegrationUnitSupport {

  /** The service. */
  protected static ContentServiceJpa contentService;

  /** The project service. */
  protected static ProjectServiceJpa projectService;

  /** The properties. */
  protected static Properties properties;

  /** The test password. */
  protected static String testUser;

  /** The test password. */
  protected static String testPassword;

  /** The test password. */
  protected static String adminUser;

  /** The test password. */
  protected static String adminPassword;

  /** The update concept status action/service */
  protected static UpdateConceptStatusMolecularAction action;

  /** The concept. */
  private Concept concept;

  /** The project. */
  private static Project project;

  /** The umls terminology. */
  private String umlsTerminology = "UMLS";

  /** The umls version. */
  private String umlsVersion = "latest";

  /**
   * Create test fixtures for class.
   *
   * @throws Exception the exception
   */
  @BeforeClass
  public static void setupClass() throws Exception {

    // instantiate properties
    properties = ConfigUtility.getConfigProperties();

    // instantiate services
    projectService = new ProjectServiceJpa();
    contentService = new ContentServiceJpa();

    // test run.config.ts has viewer user
    testUser = properties.getProperty("viewer.user");
    testPassword = properties.getProperty("viewer.password");

    // test run.config.ts has admin user
    adminUser = properties.getProperty("admin.user");
    adminPassword = properties.getProperty("admin.password");

    if (testUser == null || testUser.isEmpty()) {
      throw new Exception("Test prerequisite: viewer.user must be specified");
    }
    if (testPassword == null || testPassword.isEmpty()) {
      throw new Exception(
          "Test prerequisite: viewer.password must be specified");
    }
    if (adminUser == null || adminUser.isEmpty()) {
      throw new Exception("Test prerequisite: admin.user must be specified");
    }
    if (adminPassword == null || adminPassword.isEmpty()) {
      throw new Exception(
          "Test prerequisite: admin.password must be specified");
    }
  }

  /**
   * Setup.
   *
   * @throws Exception the exception
   */
  @Before
  public void setup() throws Exception {

    // ensure there is a concept associated with the project
    ProjectList projects = contentService.getProjects();
    assertTrue(projects.size() > 0);
    project = projects.getObjects().get(0);

    // verify terminology has expected value
    assertTrue(project.getTerminology().equals(umlsTerminology));

    //instantiate required service objects
    contentService = new ContentServiceJpa();
    contentService.setLastModifiedBy(adminUser);
    contentService.setMolecularActionFlag(false);

    // Copy existing concept to avoid messing with actual database data.
    ConceptJpa conceptJpa = new ConceptJpa(contentService.getConcept("C0000294",
        umlsTerminology, umlsVersion, null), false);
    conceptJpa.setId(null);
    conceptJpa.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);
    conceptJpa = (ConceptJpa) contentService.addConcept(conceptJpa);
    concept = contentService.getConcept(conceptJpa.getId());

  }

  /**
   * /** Test normal use of the helper object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testUpdateConceptStatusNormalUse() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    //
    // Prepare the test and check prerequisites
    //
    // Due to MySQL rounding to the second, we must also round our comparison
    // startDate.
    Date startDate = DateUtils.round(new Date(), Calendar.SECOND);

    // Update the WorkflowStatus of the concept from READY_FOR_PUBLICATION to
    // NEEDS_REVIEW
    final UpdateConceptStatusMolecularAction action =
        new UpdateConceptStatusMolecularAction();
    try {
      // Start transaction
      action.setTransactionPerOperation(false);
      action.beginTransaction();
      action.setChangeStatusFlag(true);
      action.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);

      // Do some standard intialization and precondition checking
      // action and prep services
      // Due to MySQL rounding to the second, we must also round our
      // lastModified time
      Long conceptLastModified =
          DateUtils.round(concept.getLastModified(), Calendar.SECOND).getTime();
      action.initialize(project, concept.getId(), null, adminUser,
          conceptLastModified, true);

      //
      // Check prerequisites
      //
      final ValidationResult validationResult = action.checkPreconditions();

      // if prerequisites fail, return validation result
      if (!validationResult.getErrors().isEmpty()
          || (!validationResult.getWarnings().isEmpty())) {
        // rollback -- unlocks the concept and closes transaction
        action.rollback();
        throw new Exception("Preconditions failed for " + action.getName());
      }

      //
      // Perform the action
      //
      action.compute();

      // commit (also removes the lock)
      action.commit();

      // Perform post-action maintenance on affected concept(s)
      action.postActionMaintenance();
      
      // Re-instantiate service object.
      contentService = new ContentServiceJpa();

      // Verify the concept's workflow Status has updated
      concept = contentService.getConcept(concept.getId());
      assertEquals(WorkflowStatus.NEEDS_REVIEW, concept.getWorkflowStatus());

      // verify the molecular action exists
      PfsParameterJpa pfs = new PfsParameterJpa();
      pfs.setSortField("lastModified");
      pfs.setAscending(false);
      MolecularActionList list = projectService.findMolecularActions(
          concept.getTerminologyId(), umlsTerminology, umlsVersion, null, pfs);
      assertTrue(list.size() > 0);
      MolecularAction ma = list.getObjects().get(0);
      assertNotNull(ma);
      assertEquals(concept.getId(), ma.getComponentId());
      assertTrue(ma.getLastModified().compareTo(startDate) >= 0);
      assertNotNull(ma.getAtomicActions());

      // Verify that one atomic actions exists for updating concept workflow
      // status
      pfs.setSortField(null);

      List<AtomicAction> atomicActions =
          projectService.findAtomicActions(ma.getId(), null, pfs).getObjects();
      Collections.sort(atomicActions,
          (a1, a2) -> a1.getId().compareTo(a2.getId()));
      assertEquals(1, atomicActions.size());
      assertEquals("CONCEPT", atomicActions.get(0).getIdType().toString());
      assertNotNull(atomicActions.get(0).getOldValue());
      assertNotNull(atomicActions.get(0).getNewValue());
      assertEquals("workflowStatus", atomicActions.get(0).getField());

    } catch (Exception e) {
      action.rollback();
    } finally {
      action.close();
    }

  }

  /*
   * Test degenerate use of the helper object.
   * 
   * @throws Exception the exception
   */
  /**
   * Test helper degenerate use.
   *
   * @throws Exception the exception
   */
  @Test
  public void testHelperDegenerateUse() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());
    // n/a
  }

  /**
   * Test edge cases of the helper object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testHelperEdgeCases() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());
    // n/a
  }

  /**
   * Teardown.
   */
  @After
  public void teardown() {
    // do nothing
  }

  /**
   * Teardown class.
   */
  @AfterClass
  public static void teardownClass() {
    // do nothing
  }

}
