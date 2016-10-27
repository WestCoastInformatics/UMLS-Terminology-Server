/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.jpa;

import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.ProcessExecution;
import com.wci.umls.server.Project;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.ProjectList;
import com.wci.umls.server.jpa.ProcessExecutionJpa;
import com.wci.umls.server.jpa.algo.insert.AtomLoaderAlgorithm;
import com.wci.umls.server.jpa.services.ProcessServiceJpa;
import com.wci.umls.server.test.helpers.IntegrationUnitSupport;

/**
 * Sample test to get auto complete working.
 */
public class AtomLoaderAlgorithmTest extends IntegrationUnitSupport {

  /** The algorithm. */
  AtomLoaderAlgorithm algo = null;

  /** The process execution. */
  ProcessExecution processExecution = null;

  /** The process service. */
  ProcessServiceJpa processService = null;

  /** The project. */
  Project project = null;

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

    // load the project (should be only one)
    ProjectList projects = processService.getProjects();
    assertTrue(projects.size() > 0);
    project = projects.getObjects().get(0);

    // Create a dummy process execution, to store some information the algorithm
    // needs (specifically input Path)
    processExecution = new ProcessExecutionJpa();
    processExecution.setProject(project);
    processExecution.setTerminology(project.getTerminology());
    processExecution.setVersion(project.getVersion());
    processExecution.setInputPath("terminologies/NCI_INSERT");

    // Create and configure the algorithm
    algo = new AtomLoaderAlgorithm();

    // Configure the algorithm (need to do either way)
    algo.setLastModifiedBy("admin");
    algo.setLastModifiedFlag(true);
    algo.setProcess(processExecution);
    algo.setProject(processExecution.getProject());
    algo.setTerminology(processExecution.getTerminology());
    algo.setVersion(processExecution.getVersion());
  }

  /**
   * Test atom loader normal use.
   *
   * @throws Exception the exception
   */
  @Test
  public void testAtomLoader() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    // Run the ATOMLOADER algorithm
    try {

      algo.setTransactionPerOperation(false);
      algo.beginTransaction();
      //
      // Check prerequisites
      //
      ValidationResult validationResult = algo.checkPreconditions();
      // if prerequisites fail, return validation result
      if (!validationResult.getErrors().isEmpty()
          || (!validationResult.getWarnings().isEmpty())) {
        // rollback -- unlocks the concept and closes transaction
        algo.rollback();
      }
      assertTrue(validationResult.getErrors().isEmpty());

      //
      // Perform the algorithm
      //
      algo.compute();

      // Result is to get through this all without throwing an error

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      algo.close();
    }
  }

  /**
   * Teardown.
   *
   * @throws Exception the exception
   */
  @After
  public void teardown() throws Exception {
    // do nothing
  }

  /**
   * Teardown class.
   */
  @AfterClass
  public static void teardownClass() {
    // do nothing
  }

}
