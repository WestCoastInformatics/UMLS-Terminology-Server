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
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.ProjectList;
import com.wci.umls.server.jpa.ProjectJpa;
import com.wci.umls.server.jpa.content.SemanticTypeComponentJpa;
import com.wci.umls.server.model.content.Concept;

/**
 * Implementation of the "MetaEditing Service REST Normal Use" Test Cases.
 */
public class MetaEditingServiceRestDegenerateUseTest
    extends MetaEditingServiceRestTest {

  /** The auth tokens. */
  private static String viewerToken;

  /** The admin token. */
  private static String adminToken;

  /** The project. */
  private static Project project;

  /** The umls terminology. */
  private String umlsTerminology = "UMLS";

  /** The umls version. */
  private String umlsVersion = "latest";

  /**
   * Create test fixtures per test.
   *
   * @throws Exception the exception
   */
  @Override
  @Before
  public void setup() throws Exception {

    // authentication
    viewerToken =
        securityService.authenticate(testUser, testPassword).getAuthToken();

    // authenticate the viewer user
    adminToken =
        securityService.authenticate(adminUser, adminPassword).getAuthToken();

    // ensure there is a concept associated with the project
    ProjectList projects = projectService.getProjects(adminToken);
    assertTrue(projects.getCount() > 0);
    project = projects.getObjects().get(0);

    // verify terminology and branch are expected values
    assertTrue(project.getTerminology().equals(umlsTerminology));
    assertTrue(project.getBranch().equals(Branch.ROOT));
  }

  /**
   * Test semanticType degenerate cases
   *
   * @throws Exception the exception
   */
  @Test
  public void testDegenerateUseRestContent001() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

    Logger.getLogger(getClass()).info(
        "TEST - Degenerate use tests for add/remove semantic type to concept");

    // get the concept
    Concept c = contentService.getConcept("C0000005", umlsTerminology,
        umlsVersion, null, adminToken);
    assertNotNull(c);

    // check against project
    assertTrue(c.getBranch().equals(project.getBranch()));

    // check that concept has semantic types
    assertTrue(c.getSemanticTypes().size() > 0);

    // get the first semantic type
    SemanticTypeComponentJpa sty =
        (SemanticTypeComponentJpa) c.getSemanticTypes().get(0);
    assertNotNull(sty);

    // get a concept with different semantic type (for testing add)
    // NOTE: Testing addition of already present sty done elsewhere
    Concept c2 = contentService.getConcept("C0000039", umlsTerminology,
        umlsVersion, null, adminToken);
    assertNotNull(c2);
    SemanticTypeComponentJpa sty2 =
        (SemanticTypeComponentJpa) c2.getSemanticTypes().get(0);
    assertNotNull(sty2);

    //
    // Null parameters
    // NOTE: Null parameters should throw exceptions (required arguments)
    //

    // check null project id
    try {
      metaEditingService.addSemanticType(null, c.getId(), sty2, adminToken);
      fail();
    } catch (Exception e) {
      // do nothing
    }

    // check null concept id
    try {
      metaEditingService.addSemanticType(project.getId(), null, sty2,
          adminToken);
      fail();
    } catch (Exception e) {
      // do nothing
    }

    // check null sty id
    try {
      metaEditingService.addSemanticType(project.getId(), c.getId(), null,
          adminToken);
      fail();
    } catch (Exception e) {
      // do nothing
    }

    // check null auth token
    try {
      metaEditingService.addSemanticType(project.getId(), c.getId(), sty2,
          null);
      fail();
    } catch (Exception e) {
      // do nothing
    }

    // check null project id
    try {
      metaEditingService.removeSemanticType(null, c.getId(), sty.getId(),
          adminToken);
      fail();
    } catch (Exception e) {
      // do nothing
    }

    // check null concept id
    try {
      metaEditingService.removeSemanticType(project.getId(), null, sty.getId(),
          adminToken);
      fail();
    } catch (Exception e) {
      // do nothing
    }

    // check null sty id
    try {
      metaEditingService.removeSemanticType(project.getId(), c.getId(), null,
          adminToken);
      fail();
    } catch (Exception e) {
      // do nothing
    }

    // check null auth token
    try {
      metaEditingService.removeSemanticType(project.getId(), c.getId(),
          sty.getId(), null);
      fail();
    } catch (Exception e) {
      // do nothing
    }

    //
    // Test add where already exists, remove where does not exist
    // NOTE: These return validation result errors
    //
    ValidationResult result;

    result = metaEditingService.addSemanticType(project.getId(), c.getId(), sty,
        adminToken);
    assertTrue(!result.isValid());

    result = metaEditingService.removeSemanticType(project.getId(), c.getId(),
        sty2.getId(), adminToken);
    assertTrue(!result.isValid());

    //
    // Check authorization
    // NOTE: These should throw exceptions
    //

    try {
      metaEditingService.addSemanticType(project.getId(), c.getId(), sty2,
          viewerToken);
      fail();
    } catch (Exception e) {
      // do nothing
    }

    // verify addition did not succeed (concept does not contain sty2)
    assertTrue(!c.getSemanticTypes().contains(sty2));

    try {
      metaEditingService.removeSemanticType(project.getId(), c.getId(),
          sty.getId(), viewerToken);
      fail();
    } catch (Exception e) {
      // do nothing
    }

    // verify removal did not succeed (concept still contains original sty)
    assertTrue(c.getSemanticTypes().contains(sty));

  }

  /**
   * Teardown.
   *
   * @throws Exception the exception
   */
  @Override
  @After
  public void teardown() throws Exception {

    // ensure project has correct branch reset (safety check)
    if (!project.getBranch().equals(Branch.ROOT)) {
      project.setBranch(Branch.ROOT);
      projectService.updateProject((ProjectJpa) project, adminToken);
    }

    // logout
    securityService.logout(adminToken);
    securityService.logout(viewerToken);

  }

}
