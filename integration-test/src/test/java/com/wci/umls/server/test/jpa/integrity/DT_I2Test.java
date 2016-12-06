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
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.jpa.services.validation.DT_I2;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.test.helpers.IntegrationUnitSupport;

/**
 * Integration testing for {@link DT_I2}.
 */
public class DT_I2Test extends IntegrationUnitSupport {

  /** The project. */
  private Project project;

  /** The service. */
  protected ContentServiceJpa contentService;

  /** The concept merged atom. */
  private Concept conceptMergedAtom = null;

  /** The concept no merged atoms. */
  private Concept conceptNoMergedAtoms = null;

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
    conceptMergedAtom = null;
    conceptNoMergedAtoms = null;

    // instantiate service
    contentService = new ContentServiceJpa();

    // make a copy of the validationTest project
    ProjectList projects = contentService.getProjects();
    assertTrue(projects.size() > 0);
    project = new ProjectJpa(projects.getObjects().get(0));

    // Reset the project's validation check list, so only this integrity check
    // will run.
    project.setValidationChecks(new ArrayList<>(Arrays.asList("DT_I2")));

    // Get two concepts with atoms
    conceptMergedAtom =
        contentService.getConcept("C0004604", "UMLS", "latest", Branch.ROOT);
    conceptNoMergedAtoms =
        contentService.getConcept("C0004611", "UMLS", "latest", Branch.ROOT);

    // Set one atom in conceptMergedAtom to appear to have been merged by the
    // merge engine
    Atom updateAtom = conceptMergedAtom.getAtoms().get(0);
    updateAtom.setLastModifiedBy("ENG-12345");
    updateAtom.setPublishable(true);

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
    // Test violation of DT_I2
    // Concept contains a publishable atom merged by the merge engine
    //

    // Check whether the action violates the validation check
    final ValidationResult validationResult =
        contentService.validateConcept(project.getValidationChecks(), conceptMergedAtom);

    // Verify that it returned a validation error
    assertFalse(validationResult.isValid());

    //
    // Test non-violation of DT_I2
    // Concept contains no atoms merged by the merge engine
    //

    // Check whether the action violates the validation check
    final ValidationResult validationResult2 =
        contentService.validateConcept(project.getValidationChecks(), conceptNoMergedAtoms);

    // Verify that it returned a validation error
    assertTrue(validationResult2.isValid());

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
