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
import com.wci.umls.server.jpa.services.validation.MGV_E;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.meta.Terminology;
import com.wci.umls.server.test.helpers.IntegrationUnitSupport;

/**
 * Integration testing for {@link MGV_E}.
 */
public class MGV_ETest extends IntegrationUnitSupport {

  /** The project. */
  private Project project;

  /** The service. */
  protected ContentServiceJpa contentService;

  /** The concept ICD10CM related 1. */
  private Concept conceptICD10CMRelated1 = null;

  /** The concept ICD10CM related 2. */
  private Concept conceptICD10CMRelated2 = null;

  /** The concept ICD10CM unrelated. */
  private Concept conceptICD10CMUnrelated = null;

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
    conceptICD10CMRelated1 = null;
    conceptICD10CMRelated2 = null;
    conceptICD10CMUnrelated = null;

    // instantiate service
    contentService = new ContentServiceJpa();
    contentService.setLastModifiedBy("admin");
    contentService.setMolecularActionFlag(false);

    // make a copy of the validationTest project
    ProjectList projects = contentService.getProjects();
    assertTrue(projects.size() > 0);
    project = new ProjectJpa(projects.getObjects().get(0));
    
    // Setup validationData germane to this test, and add to project
    // NOTE: MGV_E runs for any terminology NOT specified in validation data.  
    // So add all terminologies to validation data except ICD10CM.
    final List<TypeKeyValue> validationData =
        new ArrayList<TypeKeyValue>(project.getValidationData());
    contentService.getTerminologies();
    for(Terminology terminology : contentService.getTerminologies().getObjects()){
      if(!terminology.getTerminology().equals("ICD10CM")){
        validationData.add(new TypeKeyValueJpa("MGV_E", terminology.getTerminology(), ""));
      }
    }
    project.setValidationData(validationData);

    // Get two UMLS concepts connected by UMLS relationships, and one that is not
    conceptICD10CMRelated1 =
        contentService.getConcept("C0041327", "MTH", "latest", Branch.ROOT);
    conceptICD10CMRelated2 =
        contentService.getConcept("C0041296", "MTH", "latest", Branch.ROOT);
    conceptICD10CMUnrelated =
        contentService.getConcept("C0000727", "MTH", "latest", Branch.ROOT);

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
    // Test violation of MGV_E
    // Concepts are connected by an ICD10CM relationship
    //

    // Create and configure the action
    final MergeMolecularAction action = new MergeMolecularAction();

    action.setProject(project);
    action.setConceptId(conceptICD10CMRelated2.getId());
    action.setConceptId2(conceptICD10CMRelated1.getId());
    action.setLastModifiedBy("admin");
    action.setLastModified(conceptICD10CMRelated2.getLastModified().getTime());
    action.setOverrideWarnings(false);
    action.setTransactionPerOperation(false);
    action.setMolecularActionFlag(true);
    action.setChangeStatusFlag(true);
    action.setValidationChecks(new ArrayList<>(Arrays.asList("MGV_E")));

    // Check whether the action violates the validation check
    ValidationResult validationResult = checkActionPreconditions(action);

    // Verify that it returned a validation error
    assertFalse(validationResult.isValid());

    //
    // Test non-violation of MGV_E
    // Concepts not connected by an ICD10CM relationship
    //

    // Create and configure the action
    MergeMolecularAction action2 = new MergeMolecularAction();

    action2.setProject(project);
    action2.setConceptId(conceptICD10CMRelated2.getId());
    action2.setConceptId2(conceptICD10CMUnrelated.getId());
    action2.setLastModifiedBy("admin");
    action2.setLastModified(conceptICD10CMRelated2.getLastModified().getTime());
    action2.setOverrideWarnings(false);
    action2.setTransactionPerOperation(false);
    action2.setMolecularActionFlag(true);
    action2.setChangeStatusFlag(true);
    action2.setValidationChecks(new ArrayList<>(Arrays.asList("MGV_E")));

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
