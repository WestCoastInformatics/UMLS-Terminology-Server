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
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.ProjectList;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.SemanticTypeComponentJpa;
import com.wci.umls.server.model.content.Concept;

/**
 * Implementation of the "MetaEditing Service REST Normal Use" Test Cases.
 */
public class MetaEditingServiceRestNormalUseTest
    extends MetaEditingServiceRestTest {

  /** The auth token. */
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

    // authentication (admin for editing permissions)
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
   * Test add and remove semanticType to concept
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestContent001() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

    Logger.getLogger(getClass())
        .info("TEST - Add and remove semantic type to/from " + "C0000005,"
            + umlsTerminology + ", " + umlsVersion + ", " + authToken);

    // get the concept
    Concept c = null;
    try {
     c = contentService.getConcept("C0000005", umlsTerminology,
        umlsVersion, authToken);
    } catch (Exception e) {
      e.printStackTrace();
    }
    assertNotNull(c);

    // check against project
    assertTrue(c.getBranch().equals(project.getBranch()));

    // check that concept has semantic types
    assertTrue(c.getSemanticTypes().size() > 0);

    // get the first semantic type
    SemanticTypeComponentJpa sty = (SemanticTypeComponentJpa) c.getSemanticTypes().get(0);
    assertNotNull(sty);

    //
    // Test removal
    //

    // remove the semantic type from the concept
    c = metaEditingService.removeSemanticType(project.getId(), c.getId(),
        sty.getId(), authToken);
    assertTrue(!c.getSemanticTypes().contains(sty));

    // validate the concept
    validationService.validateConcept((ConceptJpa) c, authToken);

    // add the semantic type to the concept
    c = metaEditingService.addSemanticType(project.getId(), c.getId(), sty,
        authToken);
    assertTrue(c.getSemanticTypes().contains(sty));

    // validate the concept
    validationService.validateConcept((ConceptJpa) c, authToken);

  }

  /**
   * Teardown.
   *
   * @throws Exception the exception
   */
  @Override
  @After
  public void teardown() throws Exception {

    // logout
    securityService.logout(authToken);

  }

}
