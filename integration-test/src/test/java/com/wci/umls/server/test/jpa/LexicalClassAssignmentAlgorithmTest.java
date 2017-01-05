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

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.jpa.algo.maint.LexicalClassAssignmentAlgorithm;
import com.wci.umls.server.test.helpers.IntegrationUnitSupport;

/**
 * Sample test to get auto complete working.
 */
public class LexicalClassAssignmentAlgorithmTest
    extends IntegrationUnitSupport {

  /** The service. */
  LexicalClassAssignmentAlgorithm algo = null;

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

    algo = new LexicalClassAssignmentAlgorithm();

    // Configure the algorithm
    // algo.setActivityId - set by algorithm
    // algo.setWorkId - set by algorithm
    // algo.setUserName - use default
    // algo.setProperties - n/a
    algo.setLastModifiedBy("admin");
    algo.setLastModifiedFlag(true);
    algo.setProject(algo.getProjects().getObjects().get(0));
    algo.setTerminology("MTH");
    algo.setVersion("latest");

  }

  /**
   * Quick test for NCIMTH
   *
   * @throws Exception the exception
   */
  @Test
  public void quickTest() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    algo.setLastModifiedBy("admin");
    algo.setLastModifiedFlag(true);
    algo.setProject(algo.getProjects().getObjects().get(0));
    algo.setTerminology("NCIMTH");
    algo.setVersion("latest");
    algo.setTransactionPerOperation(false);
    algo.beginTransaction();    
    // Send the whole project through the initializer
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
      algo.commit();
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
