/*
 *    Copyright 2015 West Coast Informatics, LLC
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
import com.wci.umls.server.jpa.services.validation.MGV_H2;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.test.helpers.IntegrationUnitSupport;

/**
 * Integration testing for {@link MGV_H2}.
 */
public class MGV_H2Test extends IntegrationUnitSupport {

  /** The project. */
  private Project project;

  /** The service. */
  protected ContentServiceJpa contentService;

  /**  The concept MS H DC 1. */
  private Concept conceptMSH_DC1 = null;

  /**  The concept MS H DC 2. */
  private Concept conceptMSH_DC2 = null;

  /**  The concept MSH no DC. */
  private Concept conceptMSHNoDC = null;

  /**  The concept no MSH. */
  private Concept conceptNoMSH = null;

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
    conceptMSH_DC1 = null;
    conceptMSH_DC2 = null;
    conceptMSHNoDC = null;
    conceptNoMSH = null;

    // instantiate service
    contentService = new ContentServiceJpa();

    // make a copy of the validationTest project
    ProjectList projects = contentService.getProjects();
    assertTrue(projects.size() > 0);
    project = new ProjectJpa(projects.getObjects().get(0));

    // Reset the project's validation check list, so only this integrity check
    // will run.
    project.setValidationChecks(new ArrayList<>(Arrays.asList("MGV_H2")));
    
    // Get two concepts that both contain publishable D/C "MSH" atoms, one
    // with
    // no D/C "MSH" atoms, and one with no "MSH" atoms at all.
    conceptMSH_DC1 =
        contentService.getConcept("C0021734", "UMLS", "latest", Branch.ROOT);
    conceptMSH_DC2 =
        contentService.getConcept("C0044444", "UMLS", "latest", Branch.ROOT);
    conceptMSHNoDC =
        contentService.getConcept("C0002784", "UMLS", "latest", Branch.ROOT);
    conceptNoMSH =
        contentService.getConcept("C0000734", "UMLS", "latest", Branch.ROOT);


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
    // Test violation of MGV_H2
    // Merge will create C-C atom combination
    //

    // Create and configure the action
    final MergeMolecularAction action = new MergeMolecularAction();

    action.setProject(project);
    action.setConceptId(conceptMSH_DC2.getId());
    action.setConceptId2(conceptMSH_DC1.getId());
    action.setLastModifiedBy("admin");
    action.setLastModified(conceptMSH_DC2.getLastModified().getTime());
    action.setOverrideWarnings(false);
    action.setTransactionPerOperation(false);
    action.setMolecularActionFlag(true);
    action.setChangeStatusFlag(true);

    // Check whether the action violates the validation check
    final ValidationResult validationResult = checkActionPreconditions(action);

    // Verify that it returned a validation error
    assertFalse(validationResult.isValid());

    //
    // Test non-violation of MGV_H2
    // One concept contains no D/C "MSH" atoms
    //

    // Create and configure the action
    final MergeMolecularAction action2 = new MergeMolecularAction();

    action2.setProject(project);
    action2.setConceptId(conceptMSH_DC2.getId());
    action2.setConceptId2(conceptMSHNoDC.getId());
    action2.setLastModifiedBy("admin");
    action2.setLastModified(conceptMSH_DC2.getLastModified().getTime());
    action2.setOverrideWarnings(false);
    action2.setTransactionPerOperation(false);
    action2.setMolecularActionFlag(true);
    action2.setChangeStatusFlag(true);

    // Check whether the action violates the validation check
    final ValidationResult validationResult2 =
        checkActionPreconditions(action2);

    // Verify that returned no validation errors
    assertTrue(validationResult2.isValid());

    //
    // Test non-violation of MGV_H2
    // One concept contains no "MSH" atoms
    //

    // Create and configure the action
    final MergeMolecularAction action3 = new MergeMolecularAction();

    action3.setProject(project);
    action3.setConceptId(conceptMSH_DC2.getId());
    action3.setConceptId2(conceptNoMSH.getId());
    action3.setLastModifiedBy("admin");
    action3.setLastModified(conceptMSH_DC2.getLastModified().getTime());
    action3.setOverrideWarnings(false);
    action3.setTransactionPerOperation(false);
    action3.setMolecularActionFlag(true);
    action3.setChangeStatusFlag(true);

    // Check whether the action violates the validation check
    final ValidationResult validationResult3 =
        checkActionPreconditions(action3);

    // Verify that returned no validation errors
    assertTrue(validationResult3.isValid());

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
