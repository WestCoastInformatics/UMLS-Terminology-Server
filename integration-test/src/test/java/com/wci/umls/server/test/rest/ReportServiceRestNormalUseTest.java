/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
/*
 * 
 */
package com.wci.umls.server.test.rest;

import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.wci.umls.server.Project;
import com.wci.umls.server.helpers.ProjectList;
import com.wci.umls.server.model.content.Concept;

/**
 * Implementation of the "Report Service REST Normal Use" Test Cases.
 */
public class ReportServiceRestNormalUseTest extends ReportServiceRestTest {

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
    assertTrue(projects.size() > 0);
    project = projects.getObjects().get(0);

    // verify terminology and branch are expected values
    assertTrue(project.getTerminology().equals(umlsTerminology));
    // TODO assertTrue(project.getBranch().equals(Branch.ROOT));

    
  }

  /**
   * Test get concept report
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetConceptReport() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    Logger.getLogger(getClass()).info(
        "TEST - Get concept report" + project + ", " + authToken);
    Concept concept = contentService.getConcept("C0002499", umlsTerminology, umlsVersion, project.getId(), authToken);
    String report = reportService.getConceptReport(project.getId(), concept.getId(), authToken);
    assertTrue(report.contains(concept.getTerminologyId()));
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
