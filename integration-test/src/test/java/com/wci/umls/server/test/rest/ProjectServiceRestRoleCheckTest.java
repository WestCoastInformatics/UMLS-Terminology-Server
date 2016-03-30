/*
 * Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package com.wci.umls.server.test.rest;

import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.wci.umls.server.jpa.ProjectJpa;

/**
 * Implementation of the "Project Service REST Normal Use" Test Cases.
 */
public class ProjectServiceRestRoleCheckTest extends ProjectServiceRestTest {

  /** The viewer auth token. */
  private static String viewerAuthToken;

  /** The admin auth token. */
  private static String adminAuthToken;

  /**
   * Create test fixtures per test.
   *
   * @throws Exception the exception
   */
  @Override
  @Before
  public void setup() throws Exception {

    // authentication
    viewerAuthToken =
        securityService.authenticate(testUser, testPassword).getAuthToken();
    adminAuthToken =
        securityService.authenticate(adminUser, adminPassword).getAuthToken();

  }

  /**
   * Test role requirements for project service calls.
   *
   * @throws Exception the exception
   */
  @Test
  public void testRoleCheckRestProject001() throws Exception {
    Logger.getLogger(getClass()).debug("RUN testRoleCheckRestProject001");

    // Attempt to add a project with viewer authorization level
    ProjectJpa project = new ProjectJpa();
    Set<String> values = new HashSet<>();
    values.add("PUBLISHED");

    project.setDescription("Sample");
    project.setName("Sample");
    project.setTerminology("UMLS");
    project.setVersion("latest");

    try {
      projectService.addProject(project, viewerAuthToken);
      fail("Attempt to add a project with viewer authorization level passed.");
    } catch (Exception e) {
      // do nothing
    }

    // Attempt to update an existing project with viewer authorization level
    project.setDescription("Sample Revised");
    try {
      projectService.updateProject(project, viewerAuthToken);
      fail("Attempt to update a project with viewer authorization level passed.");
    } catch (Exception e) {
      // do nothing
    }

    // Attempt to remove an existing project with viewer authorization level
    // first add the project with valid admin authentication
    ProjectJpa project2 =
        (ProjectJpa) projectService.addProject(project, adminAuthToken);
    try {
      projectService.removeProject(project2.getId(), viewerAuthToken);
      fail("Attempt to remove a project with viewer authorization level passed.");
    } catch (Exception e) {
      // do nothing
    }

    // remove the project with valid admin authentication
    projectService.removeProject(project2.getId(), adminAuthToken);
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
    securityService.logout(viewerAuthToken);
    securityService.logout(adminAuthToken);
  }

}
