/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.wci.umls.server.AlgorithmConfig;
import com.wci.umls.server.AlgorithmParameter;
import com.wci.umls.server.ProcessConfig;
import com.wci.umls.server.ProcessExecution;
import com.wci.umls.server.Project;
import com.wci.umls.server.helpers.KeyValuePairList;
import com.wci.umls.server.helpers.ProcessConfigList;
import com.wci.umls.server.helpers.ProcessExecutionList;
import com.wci.umls.server.helpers.ProjectList;
import com.wci.umls.server.jpa.AlgorithmConfigJpa;
import com.wci.umls.server.jpa.AlgorithmParameterJpa;
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

  /** The process config. */
  private ProcessConfig processConfig;

  /** The process config2. */
  private ProcessConfig processConfig2;

  /** The algorithm config. */
  private AlgorithmConfig algorithmConfig;

  /** The algorithm config 2. */
  private AlgorithmConfig algorithmConfig2;

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
    processConfig = processService.getProcessConfig(project.getId(),
        processConfigJpa.getId(), authToken);
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
    KeyValuePairList insertionAlgorithms =
        processService.getInsertionAlgorithms(project.getId(), authToken);

    // TEST: make sure insertion Algorithms were returned
    assertNotNull(insertionAlgorithms);

    // Get the maintenance Algorithms
    KeyValuePairList maintenanceAlgorithms =
        processService.getMaintenanceAlgorithms(project.getId(), authToken);

    // TEST: make sure maintenance Algorithms were returned
    assertNotNull(maintenanceAlgorithms);

    // Get the release Algorithms
    KeyValuePairList releaseAlgorithms =
        processService.getReleaseAlgorithms(project.getId(), authToken);

    // TEST: make sure release Algorithms were returned
    assertNotNull(releaseAlgorithms);

  }

  /**
   * Test executing a predefined process
   *
   * @throws Exception the exception
   */
  @Test
  public void testExecuteProcess() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Get the pre-defined test process
    ProcessConfig processConfig =
        processService.findProcessConfigs(project.getId(), "name:Test Process",
            null, authToken).getObjects().get(0);
    assertNotNull(processConfig);

    // Execute the process
    Long processExecutionId = processService.executeProcess(project.getId(),
        processConfig.getId(), true, authToken);

    // Wait a couple seconds until it gets set up and going
    Thread.sleep(2000);

    // Make sure the processExecution was created
    ProcessExecution processExecution = processService
        .getProcessExecution(project.getId(), processExecutionId, authToken);
    assertNotNull(processExecution);
    assertNotNull(processExecution.getStartDate());
    assertNull(processExecution.getFailDate());
    assertNull(processExecution.getFinishDate());

    // Make sure the process is showing up as a currentlyExecutingProcesses
    ProcessExecutionList runningProcessExecutions = processService
        .findCurrentlyExecutingProcesses(project.getId(), authToken);

    Boolean processFound = false;
    for (ProcessExecution pe : runningProcessExecutions.getObjects()) {
      if (pe.getId().equals(processExecutionId)) {
        processFound = true;
        break;
      }
    }
    assertTrue(processFound);

  }

  /**
   * Test canceling and restarting a running process
   *
   * @throws Exception the exception
   */
  @Test
  public void testCancelAndRestartProcess() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Create a process config that has two algos that both run for 30 secs

    processConfig2 = new ProcessConfigJpa();
    processConfig2.setDescription("Process for testing use - long");
    processConfig2.setFeedbackEmail("fake@fake.fake");
    processConfig2.setName("Long Test Process");
    processConfig2.setProject(project);
    processConfig2.setTerminology(umlsTerminology);
    processConfig2.setVersion(umlsVersion);
    processConfig2.setTimestamp(new Date());
    processConfig2 = processService.addProcessConfig(project.getId(),
        (ProcessConfigJpa) processConfig2, authToken);

    // Create and add one WAIT algorithm
    algorithmConfig = new AlgorithmConfigJpa();
    algorithmConfig.setAlgorithmKey("WAIT");
    algorithmConfig.setDescription("Algorithm for testing use");
    algorithmConfig.setEnabled(true);
    algorithmConfig.setName("Test WAIT algorithm - Long");
    algorithmConfig.setProcess(processConfig2);
    algorithmConfig.setProject(project);
    algorithmConfig.setTerminology(umlsTerminology);
    algorithmConfig.setTimestamp(new Date());
    algorithmConfig.setVersion(umlsVersion);

    // Create and set required algorithm parameters
    List<AlgorithmParameter> algoParameters =
        new ArrayList<AlgorithmParameter>();
    AlgorithmParameter algoParameter = new AlgorithmParameterJpa();
    algoParameter.setFieldName("num");
    algoParameter.setValue("20");
    algoParameters.add(algoParameter);
    algorithmConfig.setParameters(algoParameters);

    algorithmConfig = processService.addAlgorithmConfig(project.getId(),
        (AlgorithmConfigJpa) algorithmConfig, authToken);

    processConfig2.getSteps().add(algorithmConfig);

    // Create and add another WAIT algorithm
    algorithmConfig2 = new AlgorithmConfigJpa();
    algorithmConfig2.setAlgorithmKey("WAIT");
    algorithmConfig2.setDescription("Algorithm for testing use");
    algorithmConfig2.setEnabled(true);
    algorithmConfig2.setName("Test WAIT algorithm - Long2");
    algorithmConfig2.setProcess(processConfig2);
    algorithmConfig2.setProject(project);
    algorithmConfig2.setTerminology(umlsTerminology);
    algorithmConfig2.setTimestamp(new Date());
    algorithmConfig2.setVersion(umlsVersion);

    // Set required algorithm parameters (use same as above)
    algorithmConfig2
        .setParameters(new ArrayList<AlgorithmParameter>(algoParameters));

    algorithmConfig2 = processService.addAlgorithmConfig(project.getId(),
        (AlgorithmConfigJpa) algorithmConfig2, authToken);

    processConfig2.getSteps().add(algorithmConfig2);

    // Update the process to lock the steps updates
    processService.updateProcessConfig(project.getId(),
        (ProcessConfigJpa) processConfig2, authToken);

    // Execute the process in the background
    Long processExecutionId = processService.executeProcess(project.getId(),
        processConfig2.getId(), true, authToken);

    // Wait a few seconds until it gets set up and going
    Thread.sleep(3000);
     
    // Make sure the process is showing up as a currentlyExecutingProcesses
    ProcessExecutionList runningProcessExecutions = processService
        .findCurrentlyExecutingProcesses(project.getId(), authToken);

    Boolean processFound = false;
    for (ProcessExecution pe : runningProcessExecutions.getObjects()) {
      if (pe.getId().equals(processExecutionId)) {
        processFound = true;
        break;
      }
    }
    assertTrue(processFound);

    // Check the process progress
    Integer processProgress = processService.getProcessProgress(project.getId(),
        processExecutionId, authToken);
    assertNotNull(processProgress);
    assertTrue(processProgress >= 0 && processProgress <= 100);

    // Make sure the processExecution was created
    ProcessExecution processExecution = processService
        .getProcessExecution(project.getId(), processExecutionId, authToken);
    assertNotNull(processExecution);
    assertNotNull(processExecution.getStartDate());
    assertNull(processExecution.getFailDate());
    assertNull(processExecution.getFinishDate());

     // Start another process while this one is going in the background
     // Get the pre-defined test process
     ProcessConfig processConfig =
     processService.findProcessConfigs(project.getId(), "name:Test Process",
     null, authToken).getObjects().get(0);
     assertNotNull(processConfig);
    
     // Execute the process
//     Long processExecutionId2 = processService.executeProcess(project.getId(),
//     processConfig.getId(), true, authToken);    
    
    // Check the algorithm progress
    // Integer algorithmProgress = processService.get

    // Wait a few more seconds, to build the suspense
    Thread.sleep(3000);

    // Cancel the execution
    processService.cancelProcess(project.getId(), processExecutionId,
        authToken);

    // Give the machine a second to let the cancellation process
    Thread.sleep(1000);    
    
    // Confirm execution is canceled (canceled executions will have failDate and
    // finishDate populated
    processExecution = processService.getProcessExecution(project.getId(),
        processExecutionId, authToken);
    assertNotNull(processExecution.getFailDate());
    assertNotNull(processExecution.getFinishDate());

    // Restart the execution in the background
    processService.restartProcess(project.getId(), processExecutionId, true,
        authToken);
    
  }

  /**
   * Test executing a predefined process
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindProcessExecutions() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Get the pre-defined test process
    ProcessConfig processConfig =
        processService.findProcessConfigs(project.getId(), "name:Test Process",
            null, authToken).getObjects().get(0);
    assertNotNull(processConfig);

    // Execute the process in the background (this will currently succeed... in
    // 10 seconds)
    processService.executeProcess(project.getId(), processConfig.getId(), true,
        authToken);

    // Wait for 12 seconds (it takes 10 seconds for the algorithm to run)
    Thread.sleep(12000);

    // Execute the process not in background (this will currently break, not
    // populating either Fail or Finish Dates).
    processService.executeProcess(project.getId(), processConfig.getId(), false,
        authToken);

    // Get all of the processExecutions
    ProcessExecutionList processExecutions = processService
        .findProcessExecutions(project.getId(), null, null, authToken);

    // There should be two processExecutions returned
    // assertEquals(2,processExecutions.size());

    // Now, only get the process Executions that have null for final and finish
    // dates
    ProcessExecutionList nullDatesProcessExecutions =
        processService.findProcessExecutions(project.getId(),
            "NOT failDate:[* TO *] AND NOT finishDate:[* TO *]", null,
            authToken);

    // There should only be 1 process Execution returned for this query
    // assertEquals(1,processExecutions.size());

    System.out.println("TESTTEST Stop Here!");

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
    if (algorithmConfig != null) {
      processService.removeAlgorithmConfig(project.getId(),
          algorithmConfig.getId(), authToken);
    }
    if (algorithmConfig2 != null) {
      processService.removeAlgorithmConfig(project.getId(),
          algorithmConfig2.getId(), authToken);
    }
    if (processConfig != null) {
      processService.removeProcessConfig(project.getId(), processConfig.getId(),
          true, authToken);
    }
    if (processConfig2 != null) {
      processService.removeProcessConfig(project.getId(),
          processConfig2.getId(), true, authToken);
    }
    // logout
    securityService.logout(authToken);
  }
  
//  /**
//   * Teardown.
//   *
//   * @throws Exception the exception
//   */
//  @After
//  public void inCaseOfEmergenciesTeardown() throws Exception {
//
//    // Teardown specified objects that have gotten stuck in the database after failed runs
////      processService.removeAlgorithmConfig(project.getId(),
////          23906L, authToken);
//
//      processService.removeProcessConfig(project.getId(),
//          26950L, true, authToken);
//      
//    // logout
//    securityService.logout(authToken);
//  }  

}
