/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.rest;

import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.wci.umls.server.Project;
import com.wci.umls.server.User;
import com.wci.umls.server.helpers.ProjectList;
import com.wci.umls.server.jpa.ProjectJpa;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;

/**
 * Implementation of the "Project Service REST Normal Use" Test Cases.
 */
public class ProjectServiceRestNormalUseTest extends ProjectServiceRestTest {

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
   * Test get, update, and remove project.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestProject001() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

    // Add a project
    Logger.getLogger(getClass()).info("  Add project");
    ProjectJpa project = new ProjectJpa();
    Set<String> values = new HashSet<>();
    values.add("PUBLISHED");
    User user = securityService.getUser(adminUser, adminAuthToken);
    // TODO add back using userRoleMap
    /*project.addAdministrator(user);
    project.addAuthor(user);
    project.addLead(user);*/
    project.setDescription("Sample");
    project.setName("Sample");
    project.setTerminology("UMLS");
    project.setVersion("latest");

    ProjectJpa project2 =
        (ProjectJpa) projectService.addProject(project, adminAuthToken);

    // TEST: retrieve the project and verify it is equal
    Assert.assertEquals(project, project2);

    // Update that newly added project
    Logger.getLogger(getClass()).info("  Update project");
    project2.setName("Sample 2");
    projectService.updateProject(project2, adminAuthToken);
    Project project3 =
        projectService.getProject(project2.getId(), adminAuthToken);

    // TEST: retrieve the project and verify it is equal
    Assert.assertEquals(project2, project3);

    // Remove the project
    Logger.getLogger(getClass()).info("  Remove project");
    projectService.removeProject(project2.getId(), adminAuthToken);

    // TEST: verify that it is removed (call should return null)
    try {
      project3 = projectService.getProject(project2.getId(), adminAuthToken);
      fail("Cannot retrieve a removed project.");
    } catch (Exception e) {
      // do nothing
    }
  }

  /**
   * Test getProjects()
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestProject002() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

    // Add a project
    Logger.getLogger(getClass()).info("  Add project");
    ProjectJpa project = new ProjectJpa();
    Set<String> values = new HashSet<>();
    values.add("PUBLISHED");
    User user = securityService.getUser(adminUser, adminAuthToken);
    // TODO: add back using UserRoleMap
    /*project.addAdministrator(user);
    project.addAuthor(user);
    project.addLead(user);*/
    project.setDescription("Sample");
    project.setName("Sample");
    project.setTerminology("UMLS");
    project.setVersion("latest");
    ProjectJpa project2 = new ProjectJpa(project);
    project = (ProjectJpa) projectService.addProject(project, adminAuthToken);

    // Add a second project
    Logger.getLogger(getClass()).info("  Add second project");
    project2.setName("Sample 2");
    project2.setDescription("Sample 2");
    project2 = (ProjectJpa) projectService.addProject(project2, adminAuthToken);

    // Get the projects
    Logger.getLogger(getClass()).info("  Get the projects");
    ProjectList projectList = projectService.getProjects(adminAuthToken);
    int projectCount = projectList.getCount();
    Assert.assertTrue(projectList.contains(project));
    Assert.assertTrue(projectList.contains(project2));

    // remove first project
    Logger.getLogger(getClass()).info("  Remove first project");
    projectService.removeProject(project.getId(), adminAuthToken);
    projectList = projectService.getProjects(adminAuthToken);
    Assert.assertEquals(projectCount - 1, projectList.getCount());

    // remove second project
    Logger.getLogger(getClass()).info("  Remove second project");
    projectService.removeProject(project2.getId(), adminAuthToken);
    projectList = projectService.getProjects(adminAuthToken);
    Assert.assertEquals(projectCount - 2, projectList.getCount());

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
