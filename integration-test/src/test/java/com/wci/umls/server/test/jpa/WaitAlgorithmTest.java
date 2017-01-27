/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.jpa;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.AlgorithmParameter;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.jpa.algo.maint.WaitAlgorithm;
import com.wci.umls.server.jpa.services.ProcessServiceJpa;
import com.wci.umls.server.test.helpers.IntegrationUnitSupport;

/**
 * Sample test to get auto complete working.
 */
public class WaitAlgorithmTest extends IntegrationUnitSupport {

  /** The algorithm. */
  WaitAlgorithm algo = null;

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
    if (processService.getAlgorithmInstance("WAIT") != null) {
      algo = (WaitAlgorithm) processService.getAlgorithmInstance("WAIT");
    }
    // If not, create and configure from scratch
    else {
      algo = new WaitAlgorithm();

      // Also need to create and pass in required parameters.
      List<AlgorithmParameter> algoParams = algo.getParameters();
      algoParams.get(0).setValue("10");
      algo.setParameters(algoParams);
    }

    // Configure the algorithm (need to do either way)
    algo.setLastModifiedBy("admin");
    algo.setLastModifiedFlag(true);
    algo.setProject(algo.getProjects().getObjects().get(0));
    algo.setTerminology("MTH");
    algo.setVersion("latest");
    algo.setTransactionPerOperation(false);
    algo.beginTransaction();
  }

  /**
   * Test matrix init normal use.
   *
   * @throws Exception the exception
   */
  @Test
  public void testWait() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    // Run the WAIT algorithm
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

    } catch (Exception e) {
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
