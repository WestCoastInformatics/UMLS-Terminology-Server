/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.jpa.integrity;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.Project;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.ProjectList;
import com.wci.umls.server.jpa.ProjectJpa;
import com.wci.umls.server.jpa.algo.action.MergeMolecularAction;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.jpa.services.validation.MGV_E2;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.test.helpers.IntegrationUnitSupport;

/**
 * Integration testing for {@link MGV_E2}.
 */
public class MGV_E2Test extends IntegrationUnitSupport {

  /** The project. */
  private Project project;

  /** The service. */
  protected ContentServiceJpa contentService;

  /** The concept related 1. */
  private Concept conceptRelated1 = null;

  /** The concept related 2. */
  private Concept conceptRelated2 = null;

  /** The concept unrelated. */
  private Concept conceptUnrelated = null;

  /**
   * Setup class.
   *
   * @throws Exception the exception
   */
  @BeforeClass
  public static void setupClass() throws Exception {
    // do nothing
  }

  /**
   * Setup.
   *
   * @throws Exception the exception
   */
  @Before
  public void setup() throws Exception {
    project = null;
    conceptRelated1 = null;
    conceptRelated2 = null;
    conceptUnrelated = null;

    // instantiate service
    contentService = new ContentServiceJpa();
    contentService.setLastModifiedBy("admin");
    contentService.setMolecularActionFlag(false);

    // make a copy of the validationTest project
    ProjectList projects = contentService.getProjects();
    assertTrue(projects.size() > 0);
    project = new ProjectJpa(projects.getObjects().get(0));

    // Get two concepts connected by a project-terminology concept relationship,
    // and one that is not
    conceptRelated1 =
        contentService.getConcept("C0000052", project.getTerminology(), "latest", Branch.ROOT);
    conceptRelated2 =
        contentService.getConcept("C1415001", project.getTerminology(), "latest", Branch.ROOT);
    conceptUnrelated =
        contentService.getConcept("C0000005", project.getTerminology(), "latest", Branch.ROOT);

  }

  /**
   * Test merge normal use.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUse() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    //
    // Test violation of MGV_E2
    // Concepts are connected by an project-terminology concept-relationship
    //

    // Create and configure the action
    final MergeMolecularAction action = new MergeMolecularAction();

    action.setProject(project);
    action.setConceptId(conceptRelated2.getId());
    action.setConceptId2(conceptRelated1.getId());
    action.setLastModifiedBy("admin");
    action.setLastModified(conceptRelated2.getLastModified().getTime());
    action.setOverrideWarnings(false);
    action.setTransactionPerOperation(false);
    action.setMolecularActionFlag(true);
    action.setChangeStatusFlag(true);
    action.setValidationChecks(new ArrayList<>(Arrays.asList("MGV_E2")));

    // Check whether the action violates the validation check
    ValidationResult validationResult = checkActionPreconditions(action);

    // Verify that it returned a validation error
    assertFalse(validationResult.isValid());

    //
    // Test non-violation of MGV_E2
    // Concepts not connected by a project-terminology concept-relationship
    //

    // Create and configure the action
    MergeMolecularAction action2 = new MergeMolecularAction();

    action2.setProject(project);
    action2.setConceptId(conceptRelated2.getId());
    action2.setConceptId2(conceptUnrelated.getId());
    action2.setLastModifiedBy("admin");
    action2.setLastModified(conceptRelated2.getLastModified().getTime());
    action2.setOverrideWarnings(false);
    action2.setTransactionPerOperation(false);
    action2.setMolecularActionFlag(true);
    action2.setChangeStatusFlag(true);
    action2.setValidationChecks(new ArrayList<>(Arrays.asList("MGV_E2")));

    // Check whether the action violates the validation check
    validationResult = checkActionPreconditions(action2);

    // Verify that returned no validation errors
    assertTrue(validationResult.isValid());

  }

  /**
   * Teardown.
   *
   * @throws Exception the exception
   */
  @After
  public void teardown() throws Exception {
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
