/*
 * Copyright 2016 West Coast Informatics, LLC
 */
/*
 * 
 */
package com.wci.umls.server.test.rest;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.wci.umls.server.Project;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.ProjectList;
import com.wci.umls.server.jpa.content.SemanticTypeComponentJpa;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.SemanticTypeComponent;

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

    //
    // Prepare the test and check prerequisites
    //

    // get the concept
    Concept c = contentService.getConcept("C0002520", umlsTerminology,
        umlsVersion, null, authToken);
    assertNotNull(c);

    // check against project
    assertTrue(c.getBranch().equals(project.getBranch()));

    // construct a semantic type not present on concept (here, Lipid)
    SemanticTypeComponentJpa sty = new SemanticTypeComponentJpa();
    sty.setBranch(c.getBranch());
    sty.setLastModifiedBy(authToken);
    sty.setSemanticType("Lipid");
    sty.setTerminologyId("TestId");
    sty.setTerminology(umlsTerminology);
    sty.setVersion(umlsVersion);
    sty.setTimestamp(new Date());
    //
    // Test addition
    //

    // add the semantic type to the concept
    ValidationResult v = metaEditingService.addSemanticType(project.getId(),
        c.getId(), c.getTimestamp().getTime(), sty, false, authToken);
    assertTrue(v.getErrors().isEmpty());

    // retrieve the concept and check semantic types
    c = contentService.getConcept("C0002520", umlsTerminology, umlsVersion,
        null, authToken);
    sty = null;
    for (SemanticTypeComponent s : c.getSemanticTypes()) {
      if (s.getSemanticType().equals("Lipid")) {
        sty = (SemanticTypeComponentJpa) s;
      }
    }
    assertNotNull(sty);

    //
    // Test removal
    //

    // remove the semantic type from the concept
    v = metaEditingService.removeSemanticType(project.getId(), c.getId(),
        c.getTimestamp().getTime(), sty.getId(), false, authToken);
    assertTrue(v.getErrors().isEmpty());

    // retrieve the concept and check semantic types
    c = contentService.getConcept("C0002520", umlsTerminology, umlsVersion,
        null, authToken);
    boolean styPresent = false;
    for (SemanticTypeComponent s : c.getSemanticTypes()) {
      if (s.getSemanticType().equals("Lipid")) {
        styPresent = true;
      }
    }
    assertTrue(!styPresent);

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
