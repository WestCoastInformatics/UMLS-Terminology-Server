/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.wci.umls.server.ProcessConfig;
import com.wci.umls.server.Project;
import com.wci.umls.server.helpers.ProcessConfigList;
import com.wci.umls.server.helpers.ProjectList;
import com.wci.umls.server.jpa.ProcessConfigJpa;

/**
 * Implementation of the "Project Service REST Normal Use" Test Cases.
 */
public class ProcessServiceRestNormalUseTest extends ProcessServiceRestTest {

  /** The admin auth token. */
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

    // authentication
    authToken =
        securityService.authenticate(adminUser, adminPassword).getAuthToken();

    // ensure there is a concept associated with the project
    ProjectList projects = projectService.getProjects(authToken);
    assertTrue(projects.size() > 0);
    project = projects.getObjects().get(0);

  }

  /**
   * Test get, update, and remove process.
   *
   * @throws Exception the exception
   */
  @Test
  public void testAddUpdateRemoveProcessConfig() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Add a processConfig
    Logger.getLogger(getClass()).info("  Add processConfig");
    ProcessConfigJpa processConfig = new ProcessConfigJpa();

    processConfig.setDescription("Sample");
    processConfig.setName("Sample " + new Date().getTime());
    processConfig.setProject(project);
    processConfig.setTerminology(umlsTerminology);
    processConfig.setVersion(umlsVersion);
    ProcessConfigJpa processConfig2 = (ProcessConfigJpa) processService
        .addProcessConfig(processConfig, authToken);

    // TEST: retrieve the processConfig and verify it is equal
    assertEquals(processConfig, processConfig2);

    // Update that newly added processConfig
    Logger.getLogger(getClass()).info("  Update processConfig");
    processConfig2.setName("Sample 2 " + new Date().getTime());
    processService.updateProcessConfig(processConfig2, authToken);
    ProcessConfig processConfig3 = processService
        .getProcessConfig(project.getId(), processConfig2.getId(), authToken);

    // TEST: retrieve the processConfig and verify it is equal
    assertEquals(processConfig2, processConfig3);

    // Remove the processConfig
    Logger.getLogger(getClass()).info("  Remove processConfig");
    processService.removeProcessConfig(project.getId(), processConfig2.getId(),
        authToken);

    // TEST: verify that it is removed (call should return null)
    processConfig3 = processService.getProcessConfig(project.getId(),
        processConfig2.getId(), authToken);
    assertNull(processConfig3);
  }

  /**
   * Test getProcessConfigs()
   *
   * @throws Exception the exception
   */
  @Test
  public void testAddProcessConfigs() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Add a processConfig
    Logger.getLogger(getClass()).info("  Add processConfig");
    ProcessConfigJpa processConfig = new ProcessConfigJpa();

    processConfig.setDescription("Sample");
    processConfig.setName("Sample " + new Date().getTime());
    processConfig.setProject(project);
    processConfig.setTerminology("UMLS");
    processConfig.setVersion(umlsVersion);
    ProcessConfigJpa processConfig2 = new ProcessConfigJpa(processConfig);
    processConfig = (ProcessConfigJpa) processService
        .addProcessConfig(processConfig, authToken);

    // Add a second processConfig
    Logger.getLogger(getClass()).info("  Add second processConfig");
    processConfig2.setDescription("Sample 2");
    processConfig2.setName("Sample 2 " + new Date().getTime());
    processConfig2.setProject(project);
    processConfig2.setTerminology("UMLS");
    processConfig.setVersion(umlsVersion);
    processConfig2 = (ProcessConfigJpa) processService
        .addProcessConfig(processConfig2, authToken);

    // Get the processConfigs
    Logger.getLogger(getClass()).info("  Get the processConfigs");
    ProcessConfigList processConfigList =
        processService.getProcessConfigs(project.getId(), authToken);
    assertNotNull(processConfigList);
    int processConfigCount = processConfigList.size();
    assertTrue(processConfigList.contains(processConfig));
    assertTrue(processConfigList.contains(processConfig2));

    // remove first processConfig
    Logger.getLogger(getClass()).info("  Remove first processConfig");
    processService.removeProcessConfig(project.getId(), processConfig.getId(),
        authToken);
    processConfigList =
        processService.getProcessConfigs(project.getId(), authToken);
    assertEquals(processConfigCount - 1, processConfigList.size());

    // remove second processConfig
    Logger.getLogger(getClass()).info("  Remove second processConfig");
    processService.removeProcessConfig(project.getId(), processConfig2.getId(),
        authToken);
    processConfigList =
        processService.getProcessConfigs(project.getId(), authToken);
    assertEquals(processConfigCount - 2, processConfigList.size());

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
