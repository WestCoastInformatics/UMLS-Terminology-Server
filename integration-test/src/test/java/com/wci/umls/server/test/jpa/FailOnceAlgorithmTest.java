/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.jpa;

import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.jpa.algo.maint.FailOnceAlgorithm;
import com.wci.umls.server.jpa.services.ProcessServiceJpa;
import com.wci.umls.server.test.helpers.IntegrationUnitSupport;

/**
 * Sample test to get fail once test working.
 */
public class FailOnceAlgorithmTest extends IntegrationUnitSupport {

  /** The algorithm. */
  FailOnceAlgorithm algo = null;

  /** The process service. */
  ProcessServiceJpa processService = null;

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

    // If the algorithm is defined in the config.properties, get from there.
    if (processService.getAlgorithmInstance("FAILONCE") != null) {
      algo =
          (FailOnceAlgorithm) processService.getAlgorithmInstance("FAILONCE");
    }
    // If not, create and configure from scratch
    else {
      algo = new FailOnceAlgorithm();
    }

    // Configure the algorithm (need to do either way)
    algo.setLastModifiedBy("admin");
    algo.setLastModifiedFlag(true);
    algo.setProject(algo.getProjects().getObjects().get(0));
    algo.setTerminology("MTH");
    algo.setVersion("latest");
  }

  /**
   * Test fail once normal use.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFailOnce() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    // Run the FAILONCE algorithm
    try {

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

      // This first run, the algorithm should fail
    } catch (Exception e) {
      assertTrue(e.toString().contains("FAILONCE first run failed."));
    }

    // Run the FAILONCE algorithm again
    try {

      //
      // Perform the algorithm
      //
      algo.compute();

      // This second run, the algorithm should succeed
    } catch (Exception e) {
      // Test to make sure it didn't throw any exceptions on the second run
      assertTrue(false);
      algo.rollback();
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
