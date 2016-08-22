/*
 *    Copyright 2015 West Coast Informatics, LLC
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
import com.wci.umls.server.jpa.ProjectJpa;
import com.wci.umls.server.jpa.content.ConceptRelationshipJpa;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.jpa.services.validation.DT_I3B;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.workflow.WorkflowStatus;
import com.wci.umls.server.test.helpers.IntegrationUnitSupport;

/**
 * Integration testing for {@link DT_I3B}.
 */
public class DT_I3BTest extends IntegrationUnitSupport {

  /** The project. */
  private Project project;

  /** The service. */
  protected ContentServiceJpa contentService;

  /** The concept demotions with corresponding Rels. */
  private Concept conceptDemotionsWithCorresponding = null;

  /** The concept no demotions with no corresponding Rels. */
  private Concept conceptDemotionsNoCorresponding = null;

  /** The concept no demotions. */
  private Concept conceptNoDemotions = null;

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
    conceptDemotionsWithCorresponding = null;
    conceptDemotionsNoCorresponding = null;
    conceptNoDemotions = null;

    // instantiate service
    contentService = new ContentServiceJpa();

    // make a copy of the validationTest project
    ProjectList projects = contentService.getProjects();
    assertTrue(projects.size() > 0);
    project = new ProjectJpa(projects.getObjects().get(0));

    // Reset the project's validation check list, so only this integrity check
    // will run.
    project.setValidationChecks(new ArrayList<>(Arrays.asList("DT_I3B")));

    // Get three concepts, two with DEMOTION relationships,
    // and one without any DEMOTION relationships
    conceptDemotionsNoCorresponding =
        contentService.getConcept("C0029744", "UMLS", "latest", Branch.ROOT);
    conceptDemotionsWithCorresponding =
        contentService.getConcept("C0032460", "UMLS", "latest", Branch.ROOT);
    conceptNoDemotions =
        contentService.getConcept("C0004611", "UMLS", "latest", Branch.ROOT);

    // Add matching conceptRelationships to any DEMOTION relationships for
    // conceptDemotionsAndRels
    List<ConceptRelationship> addList = new ArrayList<>();
    for (ConceptRelationship rel : conceptDemotionsWithCorresponding
        .getRelationships()) {
      if (rel.getWorkflowStatus().equals(WorkflowStatus.DEMOTION)) {
        ConceptRelationship matchingRel =
            new ConceptRelationshipJpa(rel, false);
        matchingRel.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
        matchingRel.setPublishable(true);
        addList.add(matchingRel);
      }
    }
    conceptDemotionsWithCorresponding.getRelationships().addAll(addList);

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
    // Test violation of DT_I3B
    // Concept contains Demotion relationships, but no corresponding
    // ConceptRelationship
    //

    // Check whether the action violates the validation check
    final ValidationResult validationResult = contentService
        .validateConcept(project, conceptDemotionsNoCorresponding);

    // Verify that it returned a validation error
    assertFalse(validationResult.isValid());

    //
    // Test non-violation of DT_I3B
    // Concept contains Demotion relationships, and corresponding
    // ConceptRelationships
    //

    // Check whether the action violates the validation check
    final ValidationResult validationResult2 = contentService
        .validateConcept(project, conceptDemotionsWithCorresponding);

    // Verify that it returned a validation error
    assertTrue(validationResult2.isValid());

    //
    // Test non-violation of DT_I3B
    // Concept contains no Demotion relationships
    //

    // Check whether the action violates the validation check
    final ValidationResult validationResult3 =
        contentService.validateConcept(project, conceptNoDemotions);

    // Verify that it returned a validation error
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
