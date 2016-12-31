/*
 * Copyright 2016 West Coast Informatics, LLC
 */
/*
 * 
 */
package com.wci.umls.server.test.rest;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.wci.umls.server.Project;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.ProjectList;
import com.wci.umls.server.jpa.content.AttributeJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.workflow.WorkflowStatus;
import com.wci.umls.server.rest.client.IntegrationTestClientRest;

//TODO eventually - fill this out

/**
 * Implementation of the "MetaEditing Service REST Normal Use" Test Cases.
 */
public class MetaEditingServiceRestDegenerateUseTest
    extends MetaEditingServiceRestTest {

  /** The auth token. */
  private static String authToken;

  /** The project. */
  private static Project project;

  /** The umls terminology. */
  private String umlsTerminology = "MTH";

  /** The umls version. */
  private String umlsVersion = "latest";

  /**
   * The concept (will be copied from existing concept, to avoid affecting
   * database values.
   */
  private ConceptJpa concept;

  /**
   * Create test fixtures per test.
   *
   * @throws Exception the exception
   */
  @Override
  @Before
  public void setup() throws Exception {

    // authenticate the viewer user
    authToken =
        securityService.authenticate(adminUser, adminPassword).getAuthToken();

    // ensure there is a concept associated with the project
    ProjectList projects = projectService.findProjects(null, null, authToken);
    assertTrue(projects.size() > 0);
    project = projects.getObjects().get(0);

    // verify terminology and branch are expected values
    assertTrue(project.getTerminology().equals(umlsTerminology));
    // assertTrue(project.getBranch().equals(Branch.ROOT));

    // Copy existing concept to avoid messing with actual database data.
    concept = new ConceptJpa(contentService.getConcept("C0000294",
        umlsTerminology, umlsVersion, null, authToken), false);
    concept.setId(null);
    concept.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);
    concept = (ConceptJpa) testService.addConcept(concept, authToken);

  }

  /**
   * Test semanticType degenerate cases
   *
   * @throws Exception the exception
   */
  @Test
  public void testAddRemoveSemanticType() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    Logger.getLogger(getClass()).info(
        "TEST - Degenerate use tests for add/remove semantic type to concept");

    // get the concept
    Concept c =
        contentService.getConcept(concept.getId(), project.getId(), authToken);
    assertNotNull(c);

    // create semantic type
    String sty = "Lipid";

    //
    // Null parameters
    // NOTE: Null parameters should throw exceptions (required arguments)
    //

    // check null project id
    try {
      metaEditingService.addSemanticType(null, c.getId(), "activityId",
          c.getTimestamp().getTime(), sty, false, authToken);
      fail();
    } catch (Exception e) {
      // do nothing
    }

    // check null concept id
    try {
      metaEditingService.addSemanticType(project.getId(), null, "activityId",
          c.getTimestamp().getTime(), sty, false, authToken);
      fail();
    } catch (Exception e) {
      // do nothing
    }

    // check null timestamp
    try {
      metaEditingService.addSemanticType(project.getId(), c.getId(),
          "activityId", null, sty, false, authToken);
      fail();
    } catch (Exception e) {
      // do nothing
    }

    // check null sty id
    try {
      metaEditingService.addSemanticType(project.getId(), c.getId(),
          "activityId", c.getTimestamp().getTime(), null, false, authToken);
      fail();
    } catch (Exception e) {
      // do nothing
    }

    // check null auth token
    try {
      metaEditingService.addSemanticType(project.getId(), c.getId(),
          "activityId", c.getTimestamp().getTime(), sty, false, null);
      fail();
    } catch (Exception e) {
      // do nothing
    }

    // check null project id
    try {
      metaEditingService.removeSemanticType(null, c.getId(), "activityId",
          c.getTimestamp().getTime(), 0L, false, authToken);
      fail();
    } catch (Exception e) {
      // do nothing
    }

    // check null concept id
    try {
      metaEditingService.removeSemanticType(project.getId(), null, "activityId",
          c.getTimestamp().getTime(), 0L, false, authToken);
      fail();
    } catch (Exception e) {
      // do nothing
    }

    // check null timestamp
    try {
      metaEditingService.removeSemanticType(project.getId(), c.getId(),
          "activityId", null, 0L, false, authToken);
      fail();
    } catch (Exception e) {
      // do nothing
    }

    // check null sty id
    try {
      metaEditingService.removeSemanticType(project.getId(), c.getId(),
          "activityId", c.getTimestamp().getTime(), null, false, authToken);
      fail();
    } catch (Exception e) {
      // do nothing
    }

    // check null auth token
    try {
      
      metaEditingService.removeSemanticType(project.getId(), c.getId(),
          "activityId", c.getTimestamp().getTime(), 0L, false, null);
      fail();
    } catch (Exception e) {
      // do nothing
    }

    //
    // Test add where already exists, remove where does not exist, and invalid
    // semantic type
    // NOTE: These return validation result errors
    //
    Concept c2 = contentService.getConcept("C0000005", umlsTerminology,
        umlsVersion, project.getId(), authToken);
    try {
      metaEditingService.addSemanticType(project.getId(), c2.getId(),
          "activityId", c2.getTimestamp().getTime(), sty, false, authToken);
      fail("Attempt to insert a duplicate semantic type should fail");
    } catch (Exception e) {
      // n/a
    }

    try {
      sty= "this string must not match a semantic type name";
      metaEditingService.addSemanticType(project.getId(), c2.getId(),
          "activityId", c2.getTimestamp().getTime(), sty, false, authToken);
      fail("Attempt to insert a bogus semantic type should fail");
    } catch (Exception e) {
      // n/a
    }

  }

  /**
   * Test add/remove attribute degenerate cases
   *
   * @throws Exception the exception
   */
  @Test
  public void testAddRemoveAttribute() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    //
    // Null parameters
    // NOTE: Null parameters should throw exceptions (required arguments)
    //
    // get the concept
    Concept c =
        contentService.getConcept(concept.getId(), project.getId(), authToken);
    assertNotNull(c);
    AttributeJpa attribute = new AttributeJpa();

    // check null project id
    try {
      metaEditingService.addAttribute(null, c.getId(), "activityId",
          c.getTimestamp().getTime(), attribute, false, authToken);
      fail();
    } catch (Exception e) {
      // do nothing
    }

    // check null concept id
    try {
      metaEditingService.addAttribute(project.getId(), null, "activityId",
          c.getTimestamp().getTime(), attribute, false, authToken);
      fail();
    } catch (Exception e) {
      // do nothing
    }

    // check null timestamp
    try {
      metaEditingService.addAttribute(project.getId(), c.getId(), "activityId",
          null, attribute, false, authToken);
      fail();
    } catch (Exception e) {
      // do nothing
    }

    // check null attribute id
    try {
      metaEditingService.addAttribute(project.getId(), c.getId(), "activityId",
          c.getTimestamp().getTime(), null, false, authToken);
      fail();
    } catch (Exception e) {
      // do nothing
    }

    // check null auth token
    try {
      metaEditingService.addAttribute(project.getId(), c.getId(), "activityId",
          c.getTimestamp().getTime(), attribute, false, null);
      fail();
    } catch (Exception e) {
      // do nothing
    }

    // check null project id
    try {
      metaEditingService.removeAttribute(null, c.getId(), "activityId",
          c.getTimestamp().getTime(), attribute.getId(), false, authToken);
      fail();
    } catch (Exception e) {
      // do nothing
    }

    // check null concept id
    try {
      metaEditingService.removeAttribute(project.getId(), null, "activityId",
          c.getTimestamp().getTime(), attribute.getId(), false, authToken);
      fail();
    } catch (Exception e) {
      // do nothing
    }

    // check null timestamp
    try {
      metaEditingService.removeAttribute(project.getId(), c.getId(),
          "activityId", null, attribute.getId(), false, authToken);
      fail();
    } catch (Exception e) {
      // do nothing
    }

    // check null attribute id
    try {
      metaEditingService.removeAttribute(project.getId(), c.getId(),
          "activityId", c.getTimestamp().getTime(), null, false, authToken);
      fail();
    } catch (Exception e) {
      // do nothing
    }

    // check null auth token
    try {
      metaEditingService.removeAttribute(project.getId(), c.getId(),
          "activityId", c.getTimestamp().getTime(), attribute.getId(), false,
          null);
      fail();
    } catch (Exception e) {
      // do nothing
    }

    //
    // Test add where already exists, remove where does not exist, and invalid
    // attribute
    // NOTE: These return validation result errors
    //
    Concept c2 = contentService.getConcept("C0000005", umlsTerminology,
        umlsVersion, project.getId(), authToken);
    attribute = (AttributeJpa) c2.getAttributes().iterator().next();

    try {
      metaEditingService.addAttribute(project.getId(), c2.getId(), "activityId",
          c2.getTimestamp().getTime(), attribute, false, authToken);
      fail("Should throw an exception");
    } catch (Exception e) {
      // n/a
    }

    attribute.setName("this string must not match a attribute name");
    try {
      metaEditingService.addAttribute(project.getId(), c2.getId(), "activityId",
          c2.getTimestamp().getTime(), attribute, false, authToken);
      fail("Should throw an exception");
    } catch (Exception e) {
      // n/a
    }
  }

  /**
   * Teardown.
   *
   * @throws Exception the exception
   */
  @Override
  @After
  public void teardown() throws Exception {

    // Copy existing concept to avoid messing with actual database data.
    IntegrationTestClientRest testService =
        new IntegrationTestClientRest(ConfigUtility.getConfigProperties());
    testService.removeConcept(concept.getId(), true, authToken);
    // logout
    securityService.logout(authToken);

  }

}
