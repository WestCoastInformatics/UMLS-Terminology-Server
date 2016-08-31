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

import com.wci.umls.server.AlgorithmConfig;
import com.wci.umls.server.ProcessConfig;
import com.wci.umls.server.Project;
import com.wci.umls.server.helpers.KeyValuePairList;
import com.wci.umls.server.helpers.ProcessConfigList;
import com.wci.umls.server.helpers.ProjectList;
import com.wci.umls.server.jpa.AlgorithmConfigJpa;
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

  /**  The process config. */
  private ProcessConfig processConfig;

  /**  The algorithm config. */
  private AlgorithmConfig algorithmConfig;

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
   * Setup process config.
   *
   * @throws Exception the exception
   */
  public void setupProcessConfig() throws Exception {
    // Set up a processConfig to be utilized by other objects
    ProcessConfigJpa processConfigJpa = new ProcessConfigJpa();

    processConfigJpa.setDescription("Sample");
    processConfigJpa.setName("Sample " + new Date().getTime());
    processConfigJpa.setProject(project);
    processConfigJpa.setTerminology(umlsTerminology);
    processConfigJpa.setVersion(umlsVersion);
    processConfigJpa = (ProcessConfigJpa) processService
        .addProcessConfig(project.getId(), processConfigJpa, authToken);
    processConfig = (ProcessConfigJpa) processService
        .getProcessConfig(project.getId(), processConfigJpa.getId(), authToken);
  }

  /**
   * Test add, update, get, find, and remove process config.
   *
   * @throws Exception the exception
   */
  @Test
  public void testAddUpdateGetFindRemoveProcessConfig() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Add a processConfig
    Logger.getLogger(getClass()).info("  Add processConfig");
    ProcessConfigJpa processConfig1 = new ProcessConfigJpa();

    processConfig1.setDescription("Sample");
    processConfig1.setName("Sample " + new Date().getTime());
    processConfig1.setProject(project);
    processConfig1.setTerminology(umlsTerminology);
    processConfig1.setVersion(umlsVersion);
    ProcessConfigJpa addedProcessConfig1 = (ProcessConfigJpa) processService
        .addProcessConfig(project.getId(), processConfig1, authToken);

    // TEST: retrieve the processConfig and verify it is equal
    assertEquals(processConfig1, addedProcessConfig1);

    // Update that newly added processConfig
    Logger.getLogger(getClass()).info("  Update processConfig");
    addedProcessConfig1.setName("Sample 2 " + new Date().getTime());
    processService.updateProcessConfig(project.getId(), addedProcessConfig1,
        authToken);
    ProcessConfig updatedProcessConfig1 = processService.getProcessConfig(
        project.getId(), addedProcessConfig1.getId(), authToken);

    // TEST: retrieve the processConfig and verify it is equal
    assertEquals(addedProcessConfig1, updatedProcessConfig1);

    // Add a second processConfig
    ProcessConfigJpa processConfig2 = new ProcessConfigJpa();

    processConfig2.setDescription("Sample 2");
    processConfig2.setName("Sample 2 " + new Date().getTime());
    processConfig2.setProject(project);
    processConfig2.setTerminology(umlsTerminology);
    processConfig2.setVersion(umlsVersion);
    ProcessConfigJpa addedProcessConfig2 = (ProcessConfigJpa) processService
        .addProcessConfig(project.getId(), processConfig2, authToken);

    // Get the processConfigs
    Logger.getLogger(getClass()).info("  Get the processConfigs");
    ProcessConfigList processConfigList = processService
        .findProcessConfigs(project.getId(), null, null, authToken);
    assertNotNull(processConfigList);
    int processConfigCount = processConfigList.size();
    assertTrue(processConfigList.contains(updatedProcessConfig1));
    assertTrue(processConfigList.contains(addedProcessConfig2));

    // Remove the processConfig
    Logger.getLogger(getClass()).info("  Remove processConfig");
    processService.removeProcessConfig(project.getId(),
        updatedProcessConfig1.getId(), true, authToken);
    processConfigList = processService.findProcessConfigs(project.getId(), null,
        null, authToken);
    assertEquals(processConfigCount - 1, processConfigList.size());

    // TEST: verify that it is removed (call should return null)
    assertNull(processService.getProcessConfig(project.getId(),
        updatedProcessConfig1.getId(), authToken));

    // remove second processConfig
    Logger.getLogger(getClass()).info("  Remove second processConfig");
    processService.removeProcessConfig(project.getId(),
        addedProcessConfig2.getId(), true, authToken);
    processConfigList = processService.findProcessConfigs(project.getId(), null,
        null, authToken);
    assertEquals(processConfigCount - 2, processConfigList.size());

    // TEST: verify that it is removed (call should return null)
    assertNull(processService.getProcessConfig(project.getId(),
        addedProcessConfig2.getId(), authToken));

  }

  /**
   * Test add, get, update, and remove algorithm config.
   *
   * @throws Exception the exception
   */
  @Test
  public void testAddUpdateGetRemoveAlgorithmConfig() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    setupProcessConfig();

    // Add an Algorithm Config
    Logger.getLogger(getClass()).info("  Add algorithmConfig");
    AlgorithmConfigJpa algorithmConfig1 = new AlgorithmConfigJpa();

    algorithmConfig1.setDescription("Sample");
    algorithmConfig1.setName("Sample " + new Date().getTime());
    algorithmConfig1.setProject(project);
    algorithmConfig1.setProcess(processConfig);
    algorithmConfig1.setTerminology(umlsTerminology);
    algorithmConfig1.setVersion(umlsVersion);
    algorithmConfig1.setAlgorithmKey("Sample");
    AlgorithmConfigJpa addedAlgorithmConfig1 =
        (AlgorithmConfigJpa) processService.addAlgorithmConfig(project.getId(),
            algorithmConfig1, authToken);

    // TEST: retrieve the algorithmConfig and verify it is equal
    assertEquals(algorithmConfig1, addedAlgorithmConfig1);

    // Update that newly added algorithmConfig
    Logger.getLogger(getClass()).info("  Update algorithmConfig");
    addedAlgorithmConfig1.setName("Sample 2 " + new Date().getTime());
    processService.updateAlgorithmConfig(project.getId(), addedAlgorithmConfig1,
        authToken);
    AlgorithmConfig updatedAlgorithmConfig1 = processService.getAlgorithmConfig(
        project.getId(), addedAlgorithmConfig1.getId(), authToken);

    // TEST: retrieve the processConfig and verify it is equal
    assertEquals(addedAlgorithmConfig1, updatedAlgorithmConfig1);

    // Add a second algorithmConfig
    AlgorithmConfigJpa algorithmConfig2 = new AlgorithmConfigJpa();

    algorithmConfig2.setDescription("Sample 2");
    algorithmConfig2.setName("Sample 2 " + new Date().getTime());
    algorithmConfig2.setProject(project);
    algorithmConfig2.setProcess(processConfig);
    algorithmConfig2.setTerminology(umlsTerminology);
    algorithmConfig2.setVersion(umlsVersion);
    algorithmConfig2.setAlgorithmKey("Sample");
    AlgorithmConfigJpa addedAlgorithmConfig2 =
        (AlgorithmConfigJpa) processService.addAlgorithmConfig(project.getId(),
            algorithmConfig2, authToken);
    
    // Confirm the processConfig contains both algorithmConfigs
    ProcessConfig pc = processService.getProcessConfig(project.getId(),
        algorithmConfig2.getProcess().getId(), authToken);
    assertTrue(pc.getSteps().contains(addedAlgorithmConfig1));
    assertTrue(pc.getSteps().contains(addedAlgorithmConfig2));

    // Remove the algorithmConfig
    Logger.getLogger(getClass()).info("  Remove algorithmConfig");
    processService.removeAlgorithmConfig(project.getId(),
        updatedAlgorithmConfig1.getId(), authToken);

    // TEST: verify that it is removed (call should return null)
    assertNull(processService.getAlgorithmConfig(project.getId(),
        updatedAlgorithmConfig1.getId(), authToken));

    // remove second algorithmConfig
    Logger.getLogger(getClass()).info("  Remove second algorithmConfig");
    processService.removeAlgorithmConfig(project.getId(),
        addedAlgorithmConfig2.getId(), authToken);

    // TEST: verify that it is removed (call should return null)
    assertNull(processService.getAlgorithmConfig(project.getId(),
        addedAlgorithmConfig2.getId(), authToken));

  }
  

  /**
   * Test get predefined algorithms.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetPredefinedAlgorithms() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Get the insertion Algorithms
    KeyValuePairList insertionAlgorithms = processService.getInsertionAlgorithms(project.getId(), authToken);

    // TEST: make sure insertion Algorithms were returned
    assertNotNull(insertionAlgorithms);

    // Get the maintenance Algorithms
    KeyValuePairList maintenanceAlgorithms = processService.getMaintenanceAlgorithms(project.getId(), authToken);

    // TEST: make sure maintenance Algorithms were returned
    assertNotNull(maintenanceAlgorithms);
    
    // Get the release Algorithms
    KeyValuePairList releaseAlgorithms = processService.getReleaseAlgorithms(project.getId(), authToken);

    // TEST: make sure release Algorithms were returned
    assertNotNull(releaseAlgorithms);

  }  
  
  /**
   * Teardown.
   *
   * @throws Exception the exception
   */
  @Override
  @After
  public void teardown() throws Exception {

    // Teardown any objects created during setup
    if (processConfig != null) {
      processService.removeProcessConfig(project.getId(), processConfig.getId(),
          true, authToken);
    }
    if (algorithmConfig != null) {
      processService.removeAlgorithmConfig(project.getId(),
          algorithmConfig.getId(), authToken);
    }
    // logout
    securityService.logout(authToken);
  }

}
