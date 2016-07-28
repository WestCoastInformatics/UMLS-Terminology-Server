/*
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.rest;

import static org.junit.Assert.assertNull;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.wci.umls.server.Project;
import com.wci.umls.server.helpers.ProjectList;
import com.wci.umls.server.jpa.ProjectJpa;

/**
 * Implementation of the "Project Service REST Normal Use" Test Cases.
 */
public class ProjectServiceRestNormalUseTest extends ProjectServiceRestTest {

  /** The admin auth token. */
  private static String authToken;

  /**
   * Create test fixtures per test.
   *
   * @throws Exception the exception
   */
  @Override
  @Before
  public void setup() throws Exception {

    // authentication
    authToken =
        securityService.authenticate(adminUser, adminPassword).getAuthToken();
  }

  /**
   * Test get, update, and remove project.
   *
   * @throws Exception the exception
   */
  @Test
  public void testAddUpdateRemoveProject() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Add a project
    Logger.getLogger(getClass()).info("  Add project");
    ProjectJpa project = new ProjectJpa();
    Set<String> values = new HashSet<>();
    values.add("PUBLISHED");

    project.setDescription("Sample");
    project.setName("Sample " + new Date().getTime());
    project.setTerminology("UMLS");
    project.setWorkflowPath("DEFAULT");
    ProjectJpa project2 =
        (ProjectJpa) projectService.addProject(project, authToken);

    // TEST: retrieve the project and verify it is equal
    Assert.assertEquals(project, project2);

    // Update that newly added project
    Logger.getLogger(getClass()).info("  Update project");
    project2.setName("Sample 2 " + new Date().getTime());
    projectService.updateProject(project2, authToken);
    Project project3 =
        projectService.getProject(project2.getId(), authToken);

    // TEST: retrieve the project and verify it is equal
    Assert.assertEquals(project2, project3);

    // Remove the project
    Logger.getLogger(getClass()).info("  Remove project");
    projectService.removeProject(project2.getId(), authToken);

    // TEST: verify that it is removed (call should return null)
    project3 = projectService.getProject(project2.getId(), authToken);
    assertNull(project3);
  }

  /**
   * Test getProjects()
   *
   * @throws Exception the exception
   */
  @Test
  public void testAddProjects() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Add a project
    Logger.getLogger(getClass()).info("  Add project");
    ProjectJpa project = new ProjectJpa();
    Set<String> values = new HashSet<>();
    values.add("PUBLISHED");
    project.setDescription("Sample");
    project.setName("Sample " + new Date().getTime());
    project.setTerminology("UMLS");
    project.setWorkflowPath("DEFAULT");
    ProjectJpa project2 = new ProjectJpa(project);
    project = (ProjectJpa) projectService.addProject(project, authToken);

    // Add a second project
    Logger.getLogger(getClass()).info("  Add second project");
    project2.setName("Sample 2 " + new Date().getTime());
    project2.setDescription("Sample 2");
    project2.setTerminology("UMLS");
    project2.setWorkflowPath("DEFAULT");
    project2 = (ProjectJpa) projectService.addProject(project2, authToken);

    // Get the projects
    Logger.getLogger(getClass()).info("  Get the projects");
    ProjectList projectList = projectService.getProjects(authToken);
    int projectCount = projectList.size();
    Assert.assertTrue(projectList.contains(project));
    Assert.assertTrue(projectList.contains(project2));

    // remove first project
    Logger.getLogger(getClass()).info("  Remove first project");
    projectService.removeProject(project.getId(), authToken);
    projectList = projectService.getProjects(authToken);
    Assert.assertEquals(projectCount - 1, projectList.size());

    // remove second project
    Logger.getLogger(getClass()).info("  Remove second project");
    projectService.removeProject(project2.getId(), authToken);
    projectList = projectService.getProjects(authToken);
    Assert.assertEquals(projectCount - 2, projectList.size());

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
