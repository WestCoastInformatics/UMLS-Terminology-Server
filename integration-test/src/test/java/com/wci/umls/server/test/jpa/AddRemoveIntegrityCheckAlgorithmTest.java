/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.jpa;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertFalse;

import java.util.List;
import java.util.Properties;

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
import com.wci.umls.server.helpers.TypeKeyValue;
import com.wci.umls.server.jpa.ProcessExecutionJpa;
import com.wci.umls.server.jpa.algo.maint.AddRemoveIntegrityCheckAlgorithm;
import com.wci.umls.server.jpa.helpers.TypeKeyValueJpa;
import com.wci.umls.server.jpa.services.ProcessServiceJpa;
import com.wci.umls.server.test.helpers.IntegrationUnitSupport;

/**
 * Sample test to get auto complete working.
 */
public class AddRemoveIntegrityCheckAlgorithmTest extends IntegrationUnitSupport {

  /** The algorithm. */
  AddRemoveIntegrityCheckAlgorithm algo = null;

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
    algo = new AddRemoveIntegrityCheckAlgorithm();

    // Configure the algorithm (need to do either way)
    algo.setLastModifiedBy("admin");
    algo.setLastModifiedFlag(true);
    algo.setProcess(processExecution);
    algo.setProject(processExecution.getProject());
    algo.setTerminology(processExecution.getTerminology());
    algo.setVersion(processExecution.getVersion());

  }

  /**
   * Test add remove integrity check normal use.
   *
   * @throws Exception the exception
   */
  @Test
  public void testAddRemoveIntegrityCheckAlgorithm() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    // Run the ADDREMOVEINTEGRITYCHECK algorithm
    try {
      
      //
      // Add an integrity check to the project
      //
      TypeKeyValue integrityCheck = new TypeKeyValueJpa("MGV_I","NCI","");
      Properties algoProperties = new Properties();
      algoProperties.put("addRemove", "Add");
      algoProperties.put("checkName", integrityCheck.getType());
      algoProperties.put("value1", integrityCheck.getKey());
      algoProperties.put("value2", integrityCheck.getValue());
      algo.setProperties(algoProperties);
      
      //
      // Check prerequisites
      //
      ValidationResult validationResult = algo.checkPreconditions();
      // if prerequisites fail, return validation result
      // for this algorithm, warnings are OK, so only rollback if errors.
      // rollback -- unlocks the concept and closes transaction
      if (!validationResult.getErrors().isEmpty()) {
        Logger.getLogger(getClass())
            .info("Stopping algorithm - Precondition Errors identified:");
        for (String error : validationResult.getErrors()) {
          Logger.getLogger(getClass()).info(error);
        }
        algo.rollback();
      }
      if (!validationResult.getWarnings().isEmpty()) {
        Logger.getLogger(getClass()).info(
            "Precondition Warnings identified, but continuing algorithm run:");
        for (String warning : validationResult.getWarnings()) {
          Logger.getLogger(getClass()).info(warning);
        }
      }
      assertTrue(validationResult.getErrors().isEmpty());

      //
      // Perform the algorithm
      //
      algo.compute();

      // Assert that the newly added integrity check is present in the project
      ProjectList projects = processService.getProjects();
      assertTrue(projects.size() > 0);
      project = projects.getObjects().get(0);
      
      List<TypeKeyValue> validationData = project.getValidationData();
      assertTrue(validationData.contains(integrityCheck));
      
      
      //
      // Remove an integrity check to the project
      //
      algoProperties = new Properties();
      algoProperties.put("addRemove", "Remove");
      algoProperties.put("checkName", integrityCheck.getType());
      algoProperties.put("value1", integrityCheck.getKey());
      algoProperties.put("value2", integrityCheck.getValue());
      algo.setProperties(algoProperties);
      
      //
      // Check prerequisites
      //
      validationResult = algo.checkPreconditions();
      // if prerequisites fail, return validation result
      // for this algorithm, warnings are OK, so only rollback if errors.
      // rollback -- unlocks the concept and closes transaction
      if (!validationResult.getErrors().isEmpty()) {
        Logger.getLogger(getClass())
            .info("Stopping algorithm - Precondition Errors identified:");
        for (String error : validationResult.getErrors()) {
          Logger.getLogger(getClass()).info(error);
        }
        algo.rollback();
      }
      if (!validationResult.getWarnings().isEmpty()) {
        Logger.getLogger(getClass()).info(
            "Precondition Warnings identified, but continuing algorithm run:");
        for (String warning : validationResult.getWarnings()) {
          Logger.getLogger(getClass()).info(warning);
        }
      }
      assertTrue(validationResult.getErrors().isEmpty());

      //
      // Perform the algorithm
      //
      algo.compute();

      // Assert that the newly added integrity check is present in the project
      projects = processService.getProjects();
      assertTrue(projects.size() > 0);
      project = projects.getObjects().get(0);
      
      validationData = project.getValidationData();
      assertFalse(validationData.contains(integrityCheck));      
      

    } catch (Exception e) {
      e.printStackTrace();
      fail("Unexpected exception thrown - please review stack trace.");
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
