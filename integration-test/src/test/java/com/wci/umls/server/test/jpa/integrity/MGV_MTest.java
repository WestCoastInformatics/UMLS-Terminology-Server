/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.jpa.integrity;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import com.wci.umls.server.helpers.TypeKeyValue;
import com.wci.umls.server.jpa.ProjectJpa;
import com.wci.umls.server.jpa.algo.action.MergeMolecularAction;
import com.wci.umls.server.jpa.helpers.TypeKeyValueJpa;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.jpa.services.validation.MGV_M;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.test.helpers.IntegrationUnitSupport;

/**
 * Integration testing for {@link MGV_M}.
 */
public class MGV_MTest extends IntegrationUnitSupport {

  /** The project. */
  private Project project;

  /** The service. */
  protected ContentServiceJpa contentService;

  /**  The concept ICD 9 CM 1. */
  private Concept conceptICD9CM1 = null;

  /**  The concept ICD 9 CM 2. */
  private Concept conceptICD9CM2 = null;

  /**  The concept CST. */
  private Concept conceptCST = null;
  
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
    conceptICD9CM1 = null;
    conceptICD9CM2 = null;
    conceptCST = null;
    
    // instantiate service
    contentService = new ContentServiceJpa();
    contentService.setLastModifiedBy("admin");
    contentService.setMolecularActionFlag(false);

    // make a copy of the validationTest project
    ProjectList projects = contentService.getProjects();
    assertTrue(projects.size() > 0);
    project = new ProjectJpa(projects.getObjects().get(0));

    // Setup validationData germane to this test, and add to project
    final List<TypeKeyValue> validationData =
        new ArrayList<TypeKeyValue>(project.getValidationData());
    validationData.add(new TypeKeyValueJpa("MGV_M", "MSH", ""));
    project.setValidationData(validationData);    
    
    // Get two concepts that both contain publishable "NEC" atoms from terminology ICD9CM, and 
    // one with "NEC" atoms from terminology CST  
    conceptICD9CM1 =
        contentService.getConcept("C0003123", "UMLS", "latest", Branch.ROOT);
    conceptICD9CM2 =
        contentService.getConcept("C0029592", "UMLS", "latest", Branch.ROOT);
    conceptCST = contentService.getConcept("C0549512", "UMLS", "latest", Branch.ROOT);

    
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
    // Test violation of MGV_M
    // Concepts contain "NEC" atoms with same language but different terminologies
    //

    // Create and configure the action
    final MergeMolecularAction action = new MergeMolecularAction();

    action.setProject(project);
    action.setConceptId(conceptICD9CM2.getId());
    action.setConceptId2(conceptCST.getId());
    action.setLastModifiedBy("admin");
    action.setLastModified(conceptICD9CM2.getLastModified().getTime());
    action.setOverrideWarnings(false);
    action.setTransactionPerOperation(false);
    action.setMolecularActionFlag(true);
    action.setChangeStatusFlag(true);
    action.setValidationChecks(new ArrayList<>(Arrays.asList("MGV_M")));

    // Check whether the action violates the validation check
    final ValidationResult validationResult = checkActionPreconditions(action);

    // Verify that it returned a validation error
    assertFalse(validationResult.isValid());

    //
    // Test non-violation of MGV_M
    // Concepts contain "NEC" atoms with same language and terminologies    
    //

    // Create and configure the action
    final MergeMolecularAction action2 = new MergeMolecularAction();

    action2.setProject(project);
    action2.setConceptId(conceptICD9CM2.getId());
    action2.setConceptId2(conceptICD9CM1.getId());
    action2.setLastModifiedBy("admin");
    action2.setLastModified(conceptICD9CM2.getLastModified().getTime());
    action2.setOverrideWarnings(false);
    action2.setTransactionPerOperation(false);
    action2.setMolecularActionFlag(true);
    action2.setChangeStatusFlag(true);
    action2.setValidationChecks(new ArrayList<>(Arrays.asList("MGV_M")));

    // Check whether the action violates the validation check
    final ValidationResult validationResult2 =
        checkActionPreconditions(action2);

    // Verify that returned no validation errors
    assertTrue(validationResult2.isValid());
 
  }

  /**
   * Teardown.
   *
   * @throws Exception the exception
   */
  @After
  public void teardown() throws Exception {
    // Do nothing
  }

  /**
   * Teardown class.
   */
  @AfterClass
  public static void teardownClass() {
    // do nothing
  }

}
