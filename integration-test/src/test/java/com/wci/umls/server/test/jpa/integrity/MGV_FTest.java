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
import com.wci.umls.server.jpa.services.validation.MGV_F;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.test.helpers.IntegrationUnitSupport;

/**
 * Integration testing for {@link MGV_F}.
 */
public class MGV_FTest extends IntegrationUnitSupport {

  /** The project. */
  private Project project;

  /** The service. */
  protected ContentServiceJpa contentService;

  /** The concept MSH related 1. */
  private Concept conceptMSHRelated1 = null;

  /** The concept MSH related 2. */
  private Concept conceptMSHRelated2 = null;

  /** The concept MSH unrelated. */
  private Concept conceptMSHUnrelated = null;

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
    conceptMSHRelated1 = null;
    conceptMSHRelated2 = null;
    conceptMSHUnrelated = null;

    // instantiate service
    contentService = new ContentServiceJpa();
    contentService.setLastModifiedBy("admin");
    contentService.setMolecularActionFlag(false);

    // make a copy of the validationTest project
    ProjectList projects = contentService.getProjects();
    assertTrue(projects.size() > 0);
    project = new ProjectJpa(projects.getObjects().get(0));

    // Get two UMLS concepts connected by MSH relationships, and one that is not
    conceptMSHRelated1 =
        contentService.getConcept("C0044971", "UMLS", "latest", Branch.ROOT);
    conceptMSHRelated2 =
        contentService.getConcept("C0020387", "UMLS", "latest", Branch.ROOT);
    conceptMSHUnrelated =
        contentService.getConcept("C0338361", "UMLS", "latest", Branch.ROOT);

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
    // Test violation of MGV_F
    // Concepts are connected by an MSH relationship
    //

    // Create and configure the action
    final MergeMolecularAction action = new MergeMolecularAction();

    action.setProject(project);
    action.setConceptId(conceptMSHRelated2.getId());
    action.setConceptId2(conceptMSHRelated1.getId());
    action.setLastModifiedBy("admin");
    action.setLastModified(conceptMSHRelated2.getLastModified().getTime());
    action.setOverrideWarnings(false);
    action.setTransactionPerOperation(false);
    action.setMolecularActionFlag(true);
    action.setChangeStatusFlag(true);
    action.setValidationChecks(new ArrayList<>(Arrays.asList("MGV_F")));

    // Check whether the action violates the validation check
    ValidationResult validationResult = checkActionPreconditions(action);

    // Verify that it returned a validation error
    assertFalse(validationResult.isValid());

    //
    // Test non-violation of MGV_F
    // Concepts not connected by an MSH relationship
    //

    // Create and configure the action
    MergeMolecularAction action2 = new MergeMolecularAction();

    action2.setProject(project);
    action2.setConceptId(conceptMSHRelated2.getId());
    action2.setConceptId2(conceptMSHUnrelated.getId());
    action2.setLastModifiedBy("admin");
    action2.setLastModified(conceptMSHRelated2.getLastModified().getTime());
    action2.setOverrideWarnings(false);
    action2.setTransactionPerOperation(false);
    action2.setMolecularActionFlag(true);
    action2.setChangeStatusFlag(true);
    action2.setValidationChecks(new ArrayList<>(Arrays.asList("MGV_F")));

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
