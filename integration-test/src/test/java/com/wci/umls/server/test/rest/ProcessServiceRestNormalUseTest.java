/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.wci.umls.server.AlgorithmConfig;
import com.wci.umls.server.AlgorithmExecution;
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
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;

/**
 * Implementation of the "Project Service REST Normal Use" Test Cases.
 */
public class ProcessServiceRestNormalUseTest extends ProcessServiceRestTest {

  /** The admin auth token. */
  private static String authToken;

  /** The project. */
  private static Project project;

  /** The umls terminology. */
  private String umlsTerminology = "MTH";

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
    ProjectList projects = projectService.findProjects(null, null, authToken);
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
    processConfigJpa.setType("MAINTAINENCE");
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
    processConfig1.setType("MAINTAINENCE");
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
    processConfig2.setType("MAINTAINENCE");
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
    algorithmConfig1.setAlgorithmKey("WAIT");
    AlgorithmConfigJpa addedAlgorithmConfig1 =
        (AlgorithmConfigJpa) processService.addAlgorithmConfig(project.getId(),
            processConfig.getId(), algorithmConfig1, authToken);

    // TEST: retrieve the algorithmConfig and verify it is equal
    assertEquals(algorithmConfig1, addedAlgorithmConfig1);

    // Update that newly added algorithmConfig
    Logger.getLogger(getClass()).info("  Update algorithmConfig");
    addedAlgorithmConfig1.setName("Sample 2 " + new Date().getTime());
    processService.updateAlgorithmConfig(project.getId(), processConfig.getId(),
        addedAlgorithmConfig1, authToken);
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
    algorithmConfig2.setAlgorithmKey("WAIT");
    AlgorithmConfigJpa addedAlgorithmConfig2 =
        (AlgorithmConfigJpa) processService.addAlgorithmConfig(project.getId(),
            processConfig.getId(), algorithmConfig2, authToken);

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
    KeyValuePairList insertionAlgorithms = processService
        .getAlgorithmsForType(project.getId(), "insertion", authToken);

    // TEST: make sure insertion Algorithms were returned
    assertNotNull(insertionAlgorithms);

    // Get the maintenance Algorithms
    KeyValuePairList maintenanceAlgorithms = processService
        .getAlgorithmsForType(project.getId(), "maintenance", authToken);

    // TEST: make sure maintenance Algorithms were returned
    assertNotNull(maintenanceAlgorithms);

    // Get the release Algorithms
    KeyValuePairList releaseAlgorithms = processService
        .getAlgorithmsForType(project.getId(), "release", authToken);

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
        processService.findProcessConfigs(project.getId(),
            "name:\"Test Process\"", null, authToken).getObjects().get(0);
    assertNotNull(processConfig);

    // Execute the process
    Long processExecutionId = processService.prepareAndExecuteProcess(
        project.getId(), processConfig.getId(), true, authToken);

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

    // Wait until the process finishes completely
    Thread.sleep(10000);

    // Test to make sure the process completed successfully
    processExecution = processService.getProcessExecution(project.getId(),
        processExecutionId, authToken);
    assertNull(processExecution.getFailDate());
    assertNotNull(processExecution.getFinishDate());

    // Make sure all of the process' algorithms completed successfully
    for (AlgorithmExecution ae : processExecution.getSteps()) {
      assertNull(ae.getFailDate());
      assertNotNull(ae.getFinishDate());
    }

  }

  /**
   * Test multiple algorithm process.
   *
   * @throws Exception the exception
   */
  @Test
  public void testMultipleAlgorithmProcess() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Create a process config that has two algos that both run for 5 secs
    processConfig = new ProcessConfigJpa();
    processConfig.setDescription("Process for testing use - short");
    processConfig.setFeedbackEmail(null);
    processConfig.setName("Short Test Process");
    processConfig.setProject(project);
    processConfig.setTerminology(umlsTerminology);
    processConfig.setVersion(umlsVersion);
    processConfig.setTimestamp(new Date());
    processConfig.setType("MAINTAINENCE");
    processConfig = processService.addProcessConfig(project.getId(),
        (ProcessConfigJpa) processConfig, authToken);

    // Create and add one WAIT algorithm
    algorithmConfig = new AlgorithmConfigJpa();
    algorithmConfig.setAlgorithmKey("WAIT");
    algorithmConfig.setDescription("Algorithm for testing use");
    algorithmConfig.setEnabled(true);
    algorithmConfig.setName("Test WAIT algorithm - Short");
    algorithmConfig.setProcess(processConfig);
    algorithmConfig.setProject(project);
    algorithmConfig.setTimestamp(new Date());

    // Create and set required algorithm parameters
    List<AlgorithmParameter> algoParameters =
        new ArrayList<AlgorithmParameter>();
    AlgorithmParameter algoParameter = new AlgorithmParameterJpa();
    algoParameter.setFieldName("num");
    algoParameter.setValue("5");
    algoParameters.add(algoParameter);
    algorithmConfig.setParameters(algoParameters);

    algorithmConfig = processService.addAlgorithmConfig(project.getId(),
        processConfig.getId(), (AlgorithmConfigJpa) algorithmConfig, authToken);

    processConfig.getSteps().add(algorithmConfig);

    // Create and add another WAIT algorithm
    algorithmConfig2 = new AlgorithmConfigJpa();
    algorithmConfig2.setAlgorithmKey("WAIT");
    algorithmConfig2.setDescription("Algorithm for testing use");
    algorithmConfig2.setEnabled(true);
    algorithmConfig2.setName("Test WAIT algorithm - Short2");
    algorithmConfig2.setProcess(processConfig);
    algorithmConfig2.setProject(project);
    algorithmConfig2.setTimestamp(new Date());

    // Set required algorithm parameters (use same as above)
    algorithmConfig2
        .setParameters(new ArrayList<AlgorithmParameter>(algoParameters));

    algorithmConfig2 = processService.addAlgorithmConfig(project.getId(),
        processConfig.getId(), (AlgorithmConfigJpa) algorithmConfig2,
        authToken);

    processConfig.getSteps().add(algorithmConfig2);

    // Update the process to lock the steps updates
    processService.updateProcessConfig(project.getId(),
        (ProcessConfigJpa) processConfig, authToken);

    // Execute the process in the background
    Long processExecutionId = processService.prepareAndExecuteProcess(
        project.getId(), processConfig.getId(), true, authToken);

    // Wait a few seconds until it gets set up and going
    Thread.sleep(3000);

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

    // Ensure the process execution has a single step, for the first running
    // algorithm
    assertEquals(1, processExecution.getSteps().size());

    // Start another process while this one is going in the background
    // Get the pre-defined test process
    ProcessConfig preDefinedProcessConfig =
        processService.findProcessConfigs(project.getId(),
            "name:\"Test Process\"", null, authToken).getObjects().get(0);
    assertNotNull(processConfig);

    // Execute the process
    Long processExecutionId2 = processService.prepareAndExecuteProcess(
        project.getId(), preDefinedProcessConfig.getId(), true, authToken);

    // Wait a few more seconds, to build the suspense
    Thread.sleep(3000);

    // Ensure that both running processes show up as executing
    ProcessExecutionList executingProcesses = processService
        .findCurrentlyExecutingProcesses(project.getId(), authToken);

    ProcessExecution processExecution1 = processService
        .getProcessExecution(project.getId(), processExecutionId, authToken);
    ProcessExecution processExecution2 = processService
        .getProcessExecution(project.getId(), processExecutionId2, authToken);

    assertTrue(executingProcesses.contains(processExecution1));
    assertTrue(executingProcesses.contains(processExecution2));

    // Wait until all of the processes are completely finished
    Thread.sleep(8000);

  }

  /**
   * Test canceling and restarting a running process
   *
   * @throws Exception the exception
   */
  @Test
  public void testCancelAndRestartProcess() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Create a process config that has two algos that both run for 5 secs

    processConfig2 = new ProcessConfigJpa();
    processConfig2.setDescription("Process for testing use - long");
    processConfig2.setFeedbackEmail(null);
    processConfig2.setName("Long Test Process for Cancel and Restart");
    processConfig2.setProject(project);
    processConfig2.setTerminology(umlsTerminology);
    processConfig2.setVersion(umlsVersion);
    processConfig2.setTimestamp(new Date());
    processConfig2.setType("MAINTAINENCE");
    processConfig2 = processService.addProcessConfig(project.getId(),
        (ProcessConfigJpa) processConfig2, authToken);

    // Create and add one WAIT algorithm
    algorithmConfig = new AlgorithmConfigJpa();
    algorithmConfig.setAlgorithmKey("WAIT");
    algorithmConfig.setDescription("Algorithm for testing use");
    algorithmConfig.setEnabled(true);
    algorithmConfig.setName("Test WAIT algorithm - Short");
    algorithmConfig.setProcess(processConfig2);
    algorithmConfig.setProject(project);
    algorithmConfig.setTimestamp(new Date());

    // Create and set required algorithm parameters
    List<AlgorithmParameter> algoParameters =
        new ArrayList<AlgorithmParameter>();
    AlgorithmParameter algoParameter = new AlgorithmParameterJpa();
    algoParameter.setFieldName("num");
    algoParameter.setValue("5");
    algoParameters.add(algoParameter);
    algorithmConfig.setParameters(algoParameters);

    algorithmConfig = processService.addAlgorithmConfig(project.getId(),
        processConfig2.getId(), (AlgorithmConfigJpa) algorithmConfig,
        authToken);

    processConfig2.getSteps().add(algorithmConfig);

    // Create and add another WAIT algorithm
    algorithmConfig2 = new AlgorithmConfigJpa();
    algorithmConfig2.setAlgorithmKey("WAIT");
    algorithmConfig2.setDescription("Algorithm for testing use");
    algorithmConfig2.setEnabled(true);
    algorithmConfig2.setName("Test WAIT algorithm - Short2");
    algorithmConfig2.setProcess(processConfig2);
    algorithmConfig2.setProject(project);
    algorithmConfig2.setTimestamp(new Date());

    // Set required algorithm parameters (use same as above)
    algorithmConfig2
        .setParameters(new ArrayList<AlgorithmParameter>(algoParameters));

    algorithmConfig2 = processService.addAlgorithmConfig(project.getId(),
        processConfig2.getId(), (AlgorithmConfigJpa) algorithmConfig2,
        authToken);

    processConfig2.getSteps().add(algorithmConfig2);

    // Update the process to lock the steps updates
    processService.updateProcessConfig(project.getId(),
        (ProcessConfigJpa) processConfig2, authToken);

    // Execute the process in the background
    Long processExecutionId = processService.prepareAndExecuteProcess(
        project.getId(), processConfig2.getId(), true, authToken);

    // Wait a few seconds until it gets set up and going
    Thread.sleep(3000);

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

    // Ensure the process execution has a single step, for the first running
    // algorithm
    assertEquals(1, processExecution.getSteps().size());

    // Wait a few more seconds until the first algorithm finishes, and the
    // second algorithm has started
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

    // Confirm the first algorithm execution completed, and the second algorithm
    // execution was canceled
    AlgorithmExecution ae1 = processExecution.getSteps().get(0);
    assertNotNull(ae1);
    assertNotNull(ae1.getFinishDate());
    assertNull(ae1.getFailDate());

    AlgorithmExecution ae2 = processExecution.getSteps().get(1);
    assertNotNull(ae2);
    assertNotNull(ae2.getFinishDate());
    assertNotNull(ae2.getFailDate());

    // Restart the execution, but not in the background
    processService.restartProcess(project.getId(), processExecutionId, false,
        authToken);

    // Test to make sure the process completed successfully
    processExecution = processService.getProcessExecution(project.getId(),
        processExecutionId, authToken);
    assertNull(processExecution.getFailDate());
    assertNotNull(processExecution.getFinishDate());

    // Make sure all of the process' algorithms completed successfully
    for (AlgorithmExecution ae : processExecution.getSteps()) {
      assertNull(ae.getFailDate());
      assertNotNull(ae.getFinishDate());
    }

  }

  /**
   * Test progress monitoring.
   *
   * @throws Exception the exception
   */
  @Test
  public void testProgressMonitoring() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Get the pre-defined test process
    ProcessConfig processConfig =
        processService.findProcessConfigs(project.getId(),
            "name:\"Test Process\"", null, authToken).getObjects().get(0);
    assertNotNull(processConfig);

    // Execute the process
    Long processExecutionId = processService.prepareAndExecuteProcess(
        project.getId(), processConfig.getId(), true, authToken);

    // Wait a couple seconds until it gets set up and going
    Thread.sleep(2000);

    // Make sure the processExecution was created
    ProcessExecution processExecution = processService
        .getProcessExecution(project.getId(), processExecutionId, authToken);
    assertNotNull(processExecution);
    assertNotNull(processExecution.getStartDate());
    assertNull(processExecution.getFailDate());
    assertNull(processExecution.getFinishDate());

    // Check the process progress
    Integer processProgress = processService.getProcessProgress(project.getId(),
        processExecutionId, authToken);
    assertNotNull(processProgress);
    assertTrue(processProgress >= 0 && processProgress <= 100);

    // Get the currently running algorithm
    AlgorithmExecution algorithmExecution = processExecution.getSteps().get(0);
    Long algorithmExecutionId = algorithmExecution.getId();

    // Check the algorithm progress
    Integer algorithmProgress = processService
        .getAlgorithmProgress(project.getId(), algorithmExecutionId, authToken);

    // Wait a couple seconds
    Thread.sleep(2000);

    // Recheck the process progress, and ensure it is farther along than before
    Integer processProgress2 = processService
        .getProcessProgress(project.getId(), processExecutionId, authToken);
    assertNotNull(processProgress2);
    assertTrue(processProgress2 > processProgress);

    // Recheck the algorithm progress, and ensure it is farther along than
    // before
    Integer algorithmProgress2 = processService
        .getAlgorithmProgress(project.getId(), algorithmExecutionId, authToken);
    assertNotNull(algorithmProgress2);
    assertTrue(algorithmProgress2 > algorithmProgress);

    // Wait until the process is completely finished
    Thread.sleep(8000);

    // Make sure that the process and algorithm executions progress have been
    // set to full completion
    Integer processProgress3 = processService
        .getProcessProgress(project.getId(), processExecutionId, authToken);
    assertNotNull(processProgress3);
    assertTrue(processProgress3.equals(100));

    Integer algorithmProgress3 = processService
        .getAlgorithmProgress(project.getId(), algorithmExecutionId, authToken);
    assertNotNull(algorithmProgress3);
    assertTrue(algorithmProgress3.equals(100));

  }

  /**
   * Test fail once process, and email sending. Note: this will only run
   * successfully ONCE. To re-test, the server will need to be reloaded
   *
   * @throws Exception the exception
   */
  @Test
  public void testFailOnceAndEmailProcess() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Create a process config that has one FailOnce algo
    processConfig = new ProcessConfigJpa();
    processConfig.setDescription("Process for testing failOnce");
    processConfig.setFeedbackEmail("rwood@westcoastinformatics.com");
    processConfig.setName("FailOnce Test Process");
    processConfig.setProject(project);
    processConfig.setTerminology(umlsTerminology);
    processConfig.setVersion(umlsVersion);
    processConfig.setTimestamp(new Date());
    processConfig.setType("MAINTAINENCE");
    processConfig = processService.addProcessConfig(project.getId(),
        (ProcessConfigJpa) processConfig, authToken);

    // Create and add one FAILONCE algorithm
    algorithmConfig = new AlgorithmConfigJpa();
    algorithmConfig.setAlgorithmKey("FAILONCE");
    algorithmConfig.setDescription("Algorithm for testing FailOnce");
    algorithmConfig.setEnabled(true);
    algorithmConfig.setName("Test FAILONCE algorithm");
    algorithmConfig.setProcess(processConfig);
    algorithmConfig.setProject(project);
    algorithmConfig.setTimestamp(new Date());
    algorithmConfig.setParameters(new ArrayList<AlgorithmParameter>());
    algorithmConfig = processService.addAlgorithmConfig(project.getId(),
        processConfig.getId(), (AlgorithmConfigJpa) algorithmConfig, authToken);

    processConfig.getSteps().add(algorithmConfig);

    // Update the process to lock the steps
    processService.updateProcessConfig(project.getId(),
        (ProcessConfigJpa) processConfig, authToken);

    // Execute the process (this should fail)
    Long processExecutionId = null;
    try {
      processExecutionId = processService.prepareAndExecuteProcess(
          project.getId(), processConfig.getId(), false, authToken);
      fail("Execute should fail the first time it's run");
    } catch (Exception e) {
      // n/a
    }

    // TEST: Ask Rick if a process failed email showed up in his inbox.

    // Lookup the process execution, so we can get the id number
    PfsParameterJpa pfs = new PfsParameterJpa();
    pfs.setSortField("lastModified");
    pfs.setAscending(false);
    ProcessExecution processExecution = processService
        .findProcessExecutions(project.getId(),
            "name:\"FailOnce Test Process\"", pfs, authToken)
        .getObjects().get(0);

    processExecutionId = processExecution.getId();

    // Ensure the process execution and algorithm execution were set to Fail
    processExecution = processService.getProcessExecution(project.getId(),
        processExecutionId, authToken);
    assertNotNull(processExecution.getFailDate());
    assertNull(processExecution.getFinishDate());

    AlgorithmExecution algoExecution = processExecution.getSteps().get(0);
    assertNotNull(algoExecution.getFailDate());
    assertNull(algoExecution.getFinishDate());

    // Restart the process (this should succeed)
    processService.restartProcess(project.getId(), processExecutionId, false,
        authToken);

    // Ensure the process execution and algorithm execution ran successfully
    processExecution = processService.getProcessExecution(project.getId(),
        processExecutionId, authToken);
    assertNull(processExecution.getFailDate());
    assertNotNull(processExecution.getFinishDate());

    algoExecution = processExecution.getSteps().get(0);
    assertNull(algoExecution.getFailDate());
    assertNotNull(algoExecution.getFinishDate());

    // TEST: Ask Rick if a process complete email showed up in his inbox.

  }

  /**
   * Test get logs.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetLogs() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Create a process config that has two algos that both run for 5 secs
    processConfig = new ProcessConfigJpa();
    processConfig.setDescription("Process for testing use - short");
    processConfig.setFeedbackEmail(null);
    processConfig.setName("Short Test Process");
    processConfig.setProject(project);
    processConfig.setTerminology(umlsTerminology);
    processConfig.setVersion(umlsVersion);
    processConfig.setTimestamp(new Date());
    processConfig.setType("MAINTAINENCE");
    processConfig = processService.addProcessConfig(project.getId(),
        (ProcessConfigJpa) processConfig, authToken);

    // Create and add one WAIT algorithm
    algorithmConfig = new AlgorithmConfigJpa();
    algorithmConfig.setAlgorithmKey("WAIT");
    algorithmConfig.setDescription("Algorithm for testing use");
    algorithmConfig.setEnabled(true);
    algorithmConfig.setName("Test WAIT algorithm - Short");
    algorithmConfig.setProcess(processConfig);
    algorithmConfig.setProject(project);
    algorithmConfig.setTimestamp(new Date());

    // Create and set required algorithm parameters
    List<AlgorithmParameter> algoParameters =
        new ArrayList<AlgorithmParameter>();
    AlgorithmParameter algoParameter = new AlgorithmParameterJpa();
    algoParameter.setFieldName("num");
    algoParameter.setValue("5");
    algoParameters.add(algoParameter);
    algorithmConfig.setParameters(algoParameters);

    algorithmConfig = processService.addAlgorithmConfig(project.getId(),
        processConfig.getId(), (AlgorithmConfigJpa) algorithmConfig, authToken);

    processConfig.getSteps().add(algorithmConfig);

    // Create and add another WAIT algorithm
    algorithmConfig2 = new AlgorithmConfigJpa();
    algorithmConfig2.setAlgorithmKey("WAIT");
    algorithmConfig2.setDescription("Algorithm for testing use");
    algorithmConfig2.setEnabled(true);
    algorithmConfig2.setName("Test WAIT algorithm - Short2");
    algorithmConfig2.setProcess(processConfig);
    algorithmConfig2.setProject(project);
    algorithmConfig2.setTimestamp(new Date());

    // Set required algorithm parameters (use same as above)
    algorithmConfig2
        .setParameters(new ArrayList<AlgorithmParameter>(algoParameters));

    algorithmConfig2 = processService.addAlgorithmConfig(project.getId(),
        processConfig.getId(), (AlgorithmConfigJpa) algorithmConfig2,
        authToken);

    processConfig.getSteps().add(algorithmConfig2);

    // Update the process to lock the steps updates
    processService.updateProcessConfig(project.getId(),
        (ProcessConfigJpa) processConfig, authToken);

    // Execute the process (not in background)
    Long processExecutionId = processService.prepareAndExecuteProcess(
        project.getId(), processConfig.getId(), false, authToken);

    // Get the process execution
    ProcessExecution processExecution = processService
        .getProcessExecution(project.getId(), processExecutionId, authToken);

    // Test to make sure the process created log entries for both algorithms
    String processExecutionLog = processService.getProcessLog(project.getId(),
        processExecutionId, null, authToken);
    assertNotNull(processExecutionLog);
    // Flatten the log into a single line, so we can use regular expressions
    // against it.
    String processExecutionLogFlat = processExecutionLog.replace("\n", " ");
    assertTrue(processExecutionLogFlat
        .matches(".*" + processExecution.getLastModifiedBy() + " Starting "
            + algorithmConfig.getAlgorithmKey() + ".*"
            + processExecution.getLastModifiedBy() + " Starting "
            + algorithmConfig2.getAlgorithmKey() + ".*"));

    // Make sure all of the process' algorithms created log entries for just
    // their own algorithm
    for (AlgorithmExecution ae : processExecution.getSteps()) {
      String algorithmExecutionLog = processService
          .getAlgorithmLog(project.getId(), ae.getId(), null, authToken);
      assertNotNull(algorithmExecutionLog);
      // Flatten the log into a single line, so we can use regular expressions
      // against it.
      String algorithmExecutionLogFlat =
          algorithmExecutionLog.replace("\n", " ");
      // Make sure it doesn't contain BOTH algorithm's log lines
      assertFalse(algorithmExecutionLogFlat.matches(".*"
          + ae.getLastModifiedBy() + " Starting "
          + algorithmConfig.getAlgorithmKey() + ".*" + ae.getLastModifiedBy()
          + " Starting " + algorithmConfig2.getAlgorithmKey() + ".*"));
      // Make sure it DOES contain its own log lines
      assertTrue(algorithmExecutionLogFlat.matches(".*" + ae.getLastModifiedBy()
          + " Starting " + ae.getAlgorithmKey() + ".*"));
    }

  }

  /**
   * Teardown.
   *
   * @throws Exception the exception
   */
  @Override
  @After
  public void teardown() throws Exception {

    // Teardown any objects created during testing
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
    if (processConfig2 != null && processConfig2.getId() != null) {
      processService.removeProcessConfig(project.getId(),
          processConfig2.getId(), true, authToken);
    }
    // logout
    securityService.logout(authToken);
  }

  /**
   * Test remove process execution.
   *
   * @throws Exception the exception
   */
  @Test
  public void testRemoveProcessExecution() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Get the pre-defined test process
    ProcessConfig processConfig =
        processService.findProcessConfigs(project.getId(),
            "name:\"Test Process\"", null, authToken).getObjects().get(0);
    assertNotNull(processConfig);

    // Execute the process
    Long processExecutionId = processService.prepareAndExecuteProcess(
        project.getId(), processConfig.getId(), false, authToken);

    // Make sure the processExecution was created
    ProcessExecution processExecution = processService
        .getProcessExecution(project.getId(), processExecutionId, authToken);
    assertNotNull(processExecution);

    // Remove the processExecution, and its algorithm Executions
    processService.removeProcessExecution(project.getId(), processExecutionId,
        true, authToken);

    // Confirm removal
    assertNull(processService.getProcessExecution(project.getId(),
        processExecutionId, authToken));

  }

  // /**
  // * Teardown.
  // *
  // * @throws Exception the exception
  // */
  // @Test
  // public void inCaseOfEmergenciesTeardown() throws Exception {
  //
  // // Teardown specified objects that have gotten stuck in the database after
  // // failed runs
  // // processService.removeAlgorithmConfig(project.getId(),
  // // 23906L, authToken);
  //
  // //processService.removeProcessExecution(project.getId(), 19050L, true,
  // //authToken);
  //
  // processService.removeProcessConfig(project.getId(),
  // 17800L, true, authToken);
  //
  // // logout
  // securityService.logout(authToken);
  // }

}
