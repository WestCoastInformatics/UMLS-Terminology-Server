/*
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.rest;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.wci.umls.server.Project;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.ProjectList;
import com.wci.umls.server.jpa.ProjectJpa;
import com.wci.umls.server.jpa.content.CodeJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.DescriptorJpa;

/**
 * Implementation of the "Project Service REST Normal Use" Test Cases.
 */
public class ProjectServiceRestNormalUseTest extends ProjectServiceRestTest {

  /** The viewer auth token. */
  private static String viewerAuthToken;

  /** The admin auth token. */
  private static String adminAuthToken;
  
  /** The msh terminology. */
  private String mshTerminology = "MSH";

  /** The msh version. */
  private String mshVersion = "2015_2014_09_08";


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

    project.setDescription("Sample");
    project.setName("Sample");
    project.setTerminology("UMLS");

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
    project.setDescription("Sample");
    project.setName("Sample");
    project.setTerminology("UMLS");
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
   * Test validation of a concept.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestValidation001() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

    Project p = projectService.getProjects(adminAuthToken).getObjects().get(0);
    
    ConceptJpa c =
        (ConceptJpa) contentService.getConcept("M0028634", mshTerminology,
            mshVersion, p.getId(), adminAuthToken);

    ValidationResult result = projectService.validateConcept(p.getId(), c, adminAuthToken);

    assertTrue(result.getErrors().size() == 0);
    assertTrue(result.getWarnings().size() == 0);
  }

  /**
   * Test validation of an atom.
   *
   * @throws Exception the exception
   */
  // TODO @Test
  public void testNormalUseRestValidation002() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");
    

    /*Project p = projectService.getProjects(adminAuthToken).getObjects().get(0);

    // NOTE: ContentServiceRest has no getAtom method, this was previously a direct JPA invocation
    AtomJpa c =
        (AtomJpa) contentService.getAtom("412904012", snomedTerminology,
            snomedVersion, adminAuthToken);

    ValidationResult result = projectService.validateAtom(p.getId(), c, adminAuthToken);

    assertTrue(result.getErrors().size() == 0);
    assertTrue(result.getWarnings().size() == 0);*/
  }

  /**
   * Test validation of a descriptor.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestValidation003() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");


    Project p = projectService.getProjects(adminAuthToken).getObjects().get(0);
    
    DescriptorJpa c =
        (DescriptorJpa) contentService.getDescriptor("C013093", mshTerminology,
            mshVersion, p.getId(), adminAuthToken);

    ValidationResult result =
        projectService.validateDescriptor(p.getId(), c, adminAuthToken);

    assertTrue(result.getErrors().size() == 0);
    assertTrue(result.getWarnings().size() == 0);
  }

  /**
   * Test validation of a code.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestProject004() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");
    
    Project p = projectService.getProjects(adminAuthToken).getObjects().get(0);

    CodeJpa c =
        (CodeJpa) contentService.getCode("C013093", mshTerminology, mshVersion, p.getId(),
            adminAuthToken);

    ValidationResult result = projectService.validateCode(p.getId(), c, adminAuthToken);

    assertTrue(result.getErrors().size() == 0);
    assertTrue(result.getWarnings().size() == 0);
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
