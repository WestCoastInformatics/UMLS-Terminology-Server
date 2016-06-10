/*
 * Copyright 2016 West Coast Informatics, LLC
 */
/*
 * 
 */
package com.wci.umls.server.test.rest;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
import com.wci.umls.server.model.content.SemanticTypeComponent;

/**
 * Implementation of the "MetaEditing Service REST Normal Use" Test Cases.
 */
public class MetaEditingServiceRestEdgeCasesTest
    extends MetaEditingServiceRestTest {

  /** The auth tokens. */
  private static String authToken;

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

    // authenticate the viewer user
    authToken =
        securityService.authenticate(adminUser, adminPassword).getAuthToken();

    // ensure there is a concept associated with the project
    ProjectList projects = projectService.getProjects(authToken);
    assertTrue(projects.getCount() > 0);
    project = projects.getObjects().get(0);

    // verify terminology and branch are expected values
    assertTrue(project.getTerminology().equals(umlsTerminology));
    assertTrue(project.getBranch().equals(Branch.ROOT));
  }

  /**
   * Test terminology and branch mismatches between project and concept
   *
   * @throws Exception the exception
   */
  @Test
  public void testEdgeCaseRestContent001() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

    Logger.getLogger(getClass()).info(
        "TEST - Degenerate use tests for add/remove semantic type to concept");

    ValidationResult result;

    // get the concept
    Concept c = contentService.getConcept("C0000005", umlsTerminology,
        umlsVersion, null, authToken);
    assertNotNull(c);

    // check against project
    assertTrue(c.getBranch().equals(project.getBranch()));

    // check that concept has semantic types
    assertTrue(c.getSemanticTypes().size() > 0);

    // get the first semantic type
    SemanticTypeComponent sty = c.getSemanticTypes().get(0);
    assertNotNull(sty);

    // get a concept with different semantic type (for testing add)
    // NOTE: Testing addition of already present sty done elsewhere
    Concept c2 = contentService.getConcept("C0000039", umlsTerminology,
        umlsVersion, null, authToken);
    assertNotNull(c2);
    SemanticTypeComponentJpa sty2 =
        (SemanticTypeComponentJpa) c2.getSemanticTypes().get(0);
    assertNotNull(sty2);

    //
    // Test calls where terminologies do not match
    //
    project.setTerminology("testTerminology");
    projectService.updateProject((ProjectJpa) project, authToken);

    result = metaEditingService.addSemanticType(project.getId(), c.getId(),
        c.getTimestamp().getTime(), sty2, false, authToken);
    assertTrue(!result.isValid());

    metaEditingService.removeSemanticType(project.getId(), c.getId(),
        c.getTimestamp().getTime(), sty.getId(), false, authToken);
    assertTrue(!result.isValid());

    // reset the terminology
    project.setTerminology(umlsTerminology);
    projectService.updateProject((ProjectJpa) project, authToken);

    //
    // Test calls where branches do not match
    //
    project.setBranch("testBranch");
    projectService.updateProject((ProjectJpa) project, authToken);

    //
    // Test add and remove
    //

    result = metaEditingService.addSemanticType(project.getId(), c.getId(),
        c.getTimestamp().getTime(), sty2, false, authToken);
    assertTrue(!result.isValid());

    result = metaEditingService.removeSemanticType(project.getId(), c.getId(),
        c.getTimestamp().getTime(), sty.getId(), false, authToken);
    assertTrue(!result.isValid());

    // reset the branch
    project.setBranch(Branch.ROOT);
    projectService.updateProject((ProjectJpa) project, authToken);

  }

  /**
   * Check simultaneous editing
   *
   * @throws Exception the exception
   */
  @Test
  public void testEdgeCaseRestContent002() throws Exception {
    Logger.getLogger(getClass()).info(
        "TEST - Degenerate use tests for add/remove semantic type to concept");

    ValidationResult result1, result2;

    // get the concept
    Concept c1 = contentService.getConcept("C0000530", umlsTerminology,
        umlsVersion, null, authToken);
    assertNotNull(c1);
    SemanticTypeComponentJpa sty1 =
        (SemanticTypeComponentJpa) c1.getSemanticTypes().get(0);
    assertNotNull(sty1);

    // remove the semantic type twice
    result1 = metaEditingService.removeSemanticType(project.getId(), c1.getId(),
        c1.getTimestamp().getTime(), sty1.getId(), false, authToken);
    result2 = metaEditingService.removeSemanticType(project.getId(), c1.getId(),
        c1.getTimestamp().getTime(), sty1.getId(), false, authToken);

    // re-add the semantic type
    metaEditingService.addSemanticType(project.getId(), c1.getId(),
        c1.getTimestamp().getTime(), sty1, false, authToken);

    // expect one result to succeed, one result to fail
    assertTrue(result1.isValid() && !result2.isValid()
        || !result1.isValid() && result2.isValid());

    // check that the semantic type was successfully re-added
    c1 = contentService.getConcept("C0000530", umlsTerminology, umlsVersion,
        null, authToken);
    assertTrue(c1.getSemanticTypes().contains(sty1));
  }

  /**
   * Teardown.
   *
   * @throws Exception the exception
   */
  @Override
  @After
  public void teardown() throws Exception {

    // ensure branch and terminology are set correctly
    project.setBranch(Branch.ROOT);
    project.setTerminology(umlsTerminology);
    projectService.updateProject((ProjectJpa) project, authToken);
    // logout
    securityService.logout(authToken);

  }

}
