/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.jpa.integrity;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import com.wci.umls.server.jpa.services.validation.DT_M1;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.SemanticTypeComponent;
import com.wci.umls.server.test.helpers.IntegrationUnitSupport;

/**
 * Integration testing for {@link DT_M1}.
 */
public class DT_M1Test extends IntegrationUnitSupport {

  /** The project. */
  private Project project;

  /** The service. */
  protected ContentServiceJpa contentService;

  /** The concept no sty. */
  private Concept conceptNoSty = null;

  /** The concept no publishable sty. */
  private Concept conceptNoPublishableSty = null;

  /** The concept publishable sty. */
  private Concept conceptPublishableSty = null;

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
    conceptNoSty = null;
    conceptNoPublishableSty = null;
    conceptPublishableSty = null;

    // instantiate service
    contentService = new ContentServiceJpa();

    // make a copy of the validationTest project
    ProjectList projects = contentService.getProjects();
    assertTrue(projects.size() > 0);
    project = new ProjectJpa(projects.getObjects().get(0));

    // Reset the project's validation check list, so only this integrity check
    // will run.
    project.setValidationChecks(new ArrayList<>(Arrays.asList("DT_M1")));

    // Get three concepts
    conceptNoSty =
        contentService.getConcept("C0003123", "UMLS", "latest", Branch.ROOT);
    conceptNoPublishableSty =
        contentService.getConcept("C0004611", "UMLS", "latest", Branch.ROOT);
    conceptPublishableSty =
        contentService.getConcept("C0000074", "UMLS", "latest", Branch.ROOT);

    // Remove all Semantic Types from conceptNoSty
    conceptNoSty.setSemanticTypes(new ArrayList<SemanticTypeComponent>());

    // Set all Semantic Types to not-publishable for conceptNoPublishedSty
    for (SemanticTypeComponent sty : conceptNoPublishableSty
        .getSemanticTypes()) {
      sty.setPublishable(false);
    }

    // Confirm that conceptPublishedSty has at least one publishable sty
    boolean containsPublishedSty = false;
    for (SemanticTypeComponent sty : conceptPublishableSty.getSemanticTypes()) {
      if (sty.isPublishable()) {
        containsPublishedSty = true;
        break;
      }
    }
    assertTrue(containsPublishedSty);
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
    // Test violation of DT_M1
    // Concept contains no Semantic Types
    //

    // Check whether the action violates the validation check
    final ValidationResult validationResult =
        contentService.validateConcept(project, conceptNoSty);

    // Verify that it returned a validation error
    assertFalse(validationResult.isValid());

    //
    // Test violation of DT_M1
    // Concept contains no publishable Semantic Types
    //

    // Check whether the action violates the validation check
    final ValidationResult validationResult2 =
        contentService.validateConcept(project, conceptNoPublishableSty);

    // Verify that it returned a validation error
    assertFalse(validationResult2.isValid());

    //
    // Test non-violation of DT_M1
    // Concept contains publishable Semantic Types
    //

    // Check whether the action violates the validation check
    final ValidationResult validationResult3 =
        contentService.validateConcept(project, conceptPublishableSty);

    // Verify that returned no validation errors
    assertTrue(validationResult3.isValid());
  }

  /**
   * Test batch mode.
   *
   * @throws Exception the exception
   */
  @Test
  public void testBatchMode() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    // 1. Read all concepts
    Logger.getLogger(getClass()).info("  Read all concept ids ");
    final List<Long> conceptIds =
        contentService.getAllConceptIds("UMLS", "latest", Branch.ROOT);

    // 2. Perform the batch test
    Logger.getLogger(getClass()).info("  Validate check");
    final DT_M1 check = new DT_M1();
    final Set<Long> failures = check.validateConcepts(new HashSet<>(conceptIds),
        "UMLS", "latest", contentService);
    Logger.getLogger(getClass()).info("    count = " + failures.size());
    for (final Long id : failures) {
      Logger.getLogger(getClass())
          .info("     fail = " + contentService.getConcept(id));
    }
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
