/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.jpa;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.AlgorithmConfig;
import com.wci.umls.server.AlgorithmExecution;
import com.wci.umls.server.ProcessConfig;
import com.wci.umls.server.ProcessExecution;
import com.wci.umls.server.Project;
import com.wci.umls.server.helpers.ProjectList;
import com.wci.umls.server.jpa.AlgorithmExecutionJpa;
import com.wci.umls.server.jpa.ProcessExecutionJpa;
import com.wci.umls.server.jpa.services.ProcessServiceJpa;
import com.wci.umls.server.test.helpers.IntegrationUnitSupport;

/**
 * Test to check process execution / algorithm execution steps order.
 */
public class ProcessExecutionTest extends IntegrationUnitSupport {

  /** The process service. */
  ProcessServiceJpa processService = null;

  /** The project. */
  private static Project project;
  
  /**
   * Setup class.
   */
  @BeforeClass
  public static void setupClass() {
    // do nothing
  }

  /**
   * Setup.
   *
   * @throws Exception the exception
   */
  @Before
  public void setup() throws Exception {
    processService = new ProcessServiceJpa();
    processService.setLastModifiedBy("admin");
    
    ProjectList projects = processService.getProjects();
    assertTrue(projects.size() > 0);
    project = projects.getObjects().get(0);    
  }


  /**
   * Test process execution.
   *
   * @throws Exception the exception
   */
  @Test
  public void testProcessExecution() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());
    
    // Load the default test process config and algorithm config
    ProcessConfig processConfig = processService.findProcessConfigs(project.getId(), "name:Test Process", null).getObjects().get(0);
    AlgorithmConfig algorithmConfig = processConfig.getSteps().get(0);
    
    // Create a process execution that has two algos
    ProcessExecution processExecution = new ProcessExecutionJpa(processConfig);
    processExecution.setStartDate(new Date());
    processExecution.setWorkId(UUID.randomUUID().toString());
    processExecution = processService.addProcessExecution(processExecution);

    // Create and add one WAIT algorithm
    AlgorithmExecution algorithmExecution = new AlgorithmExecutionJpa(algorithmConfig);
    algorithmExecution.setProperties(
        new HashMap<String, String>(algorithmConfig.getProperties()));
    algorithmExecution.setProcess(processExecution);
    algorithmExecution.setActivityId(UUID.randomUUID().toString());
    algorithmExecution.setStartDate(new Date());    

    algorithmExecution = processService.addAlgorithmExecution(algorithmExecution);

    processExecution.getSteps().add(algorithmExecution);

    //Test to see what the steps values are at this point.
    assertEquals(algorithmExecution,processExecution.getSteps().get(0));
    
    // Update the process to set the steps in algorithm
    processService.updateProcessExecution(processExecution);    
    
    //Ensure the steps values havean't changed with the update
    assertEquals(algorithmExecution,processExecution.getSteps().get(0));
    
    // Create and add another WAIT algorithm
    AlgorithmExecution algorithmExecution2 = new AlgorithmExecutionJpa(algorithmConfig);
    algorithmExecution2.setProperties(
        new HashMap<String, String>(algorithmConfig.getProperties()));
    algorithmExecution2.setProcess(processExecution);
    algorithmExecution2.setActivityId(UUID.randomUUID().toString());
    algorithmExecution2.setStartDate(new Date());   

    algorithmExecution2 = processService.addAlgorithmExecution(algorithmExecution2);

    processExecution.getSteps().add(algorithmExecution2);   

    //Test to see what the steps values are at this point.
    assertEquals(algorithmExecution2,processExecution.getSteps().get(1));
    
    // Update the process to set the steps in algorithm
    processService.updateProcessExecution(processExecution); 


    //Ensure the steps values havean't changed with the update
    assertEquals(algorithmExecution,processExecution.getSteps().get(0));
    assertEquals(algorithmExecution2,processExecution.getSteps().get(1));
    
    //Now load the process execution, and test the same things
    ProcessExecution loadedProcessExecution =
        processService.getProcessExecution(processExecution.getId());
    assertEquals(algorithmExecution,loadedProcessExecution.getSteps().get(0));
    assertEquals(algorithmExecution2,loadedProcessExecution.getSteps().get(1));   
    
    
  }
  

  /**
   * Test process execution with process service reload.
   *
   * @throws Exception the exception
   */
  @Test
  public void testProcessExecutionWithProcessServiceReload() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());
    
    // Load the default test process config and algorithm config
    ProcessConfig processConfig = processService.findProcessConfigs(project.getId(), "name:Test Process", null).getObjects().get(0);
    AlgorithmConfig algorithmConfig = processConfig.getSteps().get(0);
    
    // Create a process execution that has two algos
    ProcessExecution processExecution = new ProcessExecutionJpa(processConfig);
    processExecution.setStartDate(new Date());
    processExecution.setWorkId(UUID.randomUUID().toString());
    processExecution = processService.addProcessExecution(processExecution);
    processExecution = processService.getProcessExecution(processExecution.getId());
    
    Long processExecutionId = processExecution.getId();
    
    // Create and add one WAIT algorithm
    AlgorithmExecution algorithmExecution = new AlgorithmExecutionJpa(algorithmConfig);
    algorithmExecution.setProperties(
        new HashMap<String, String>(algorithmConfig.getProperties()));
    algorithmExecution.setProcess(processExecution);
    algorithmExecution.setActivityId(UUID.randomUUID().toString());
    algorithmExecution.setStartDate(new Date());    

    algorithmExecution = processService.addAlgorithmExecution(algorithmExecution);

    processService.close();
    
    //This is the biggy that causes the issue
    processService = new ProcessServiceJpa();
    processService.setLastModifiedBy("admin");
        
    processExecution.getSteps().add(algorithmExecution);

    //Test to see what the steps values are at this point.
    assertEquals(algorithmExecution,processExecution.getSteps().get(0));
    
    // Update the process to set the steps in algorithm
    processService.updateProcessExecution(processExecution);  
    
    //Look up the process execution by ID
    processExecution = processService.getProcessExecution(processExecutionId);    
    
    //Ensure the steps values havean't changed with the update
    assertEquals(algorithmExecution,processExecution.getSteps().get(0));
    
    // Create and add another WAIT algorithm
    AlgorithmExecution algorithmExecution2 = new AlgorithmExecutionJpa(algorithmConfig);
    algorithmExecution2.setProperties(
        new HashMap<String, String>(algorithmConfig.getProperties()));
    algorithmExecution2.setProcess(processExecution);
    algorithmExecution2.setActivityId(UUID.randomUUID().toString());
    algorithmExecution2.setStartDate(new Date());   

    algorithmExecution2 = processService.addAlgorithmExecution(algorithmExecution2);
    processService = new ProcessServiceJpa();
    processService.setLastModifiedBy("admin");
    
    processExecution.getSteps().add(algorithmExecution2);   

    //Test to see what the steps values are at this point.
    assertEquals(algorithmExecution2,processExecution.getSteps().get(1));
    
    // Update the process to set the steps in algorithm
    processService.updateProcessExecution(processExecution); 
    processService = new ProcessServiceJpa();
    processService.setLastModifiedBy("admin");
    
    //Ensure the steps values havean't changed with the update
    assertEquals(algorithmExecution,processExecution.getSteps().get(0));
    assertEquals(algorithmExecution2,processExecution.getSteps().get(1));
    
  }  

  /**
   * Teardown.
   *
   * @throws Exception the exception
   */
  @After
  public void teardown() throws Exception {
    // n/a
  }

  /**
   * Teardown class.
   */
  @AfterClass
  public static void teardownClass() {
    // do nothing
  }

}
