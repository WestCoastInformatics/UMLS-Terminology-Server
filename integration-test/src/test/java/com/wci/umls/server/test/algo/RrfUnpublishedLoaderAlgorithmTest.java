/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package com.wci.umls.server.test.algo;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.wci.umls.server.jpa.algo.RrfUnpublishedLoaderAlgorithm;
import com.wci.umls.server.jpa.algo.rel.ComputePreferredNamesAlgorithm;

/**
 * Integration testing for {@link ComputePreferredNamesAlgorithm}.
 */
public class RrfUnpublishedLoaderAlgorithmTest
    extends AlgoIntegrationTestSupport {

  /** The algo. */
  private ComputePreferredNamesAlgorithm algo;

  /**
   * Setup.
   *
   * @throws Exception the exception
   */
  @Before
  public void setup() throws Exception {
    // n/a
  }

  /**
   * Test.
   *
   * @throws Exception the exception
   */
  @Test
  public void test() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());
    // RRF Unpublished
    Logger.getLogger(getClass()).info("Run unpublished loader algorithm");
    final RrfUnpublishedLoaderAlgorithm algo =
        new RrfUnpublishedLoaderAlgorithm();
    algo.setActivityId("LOADER");
    algo.setWorkId("LOADER");
    // ASSUMPTION: one project
    algo.setProject(algo.getProjects().getObjects().get(0));
    // ONLY one ".." here because it's running from integration-tests
    algo.setInputPath(
        "../config/src/main/resources/data/SAMPLE_NCI/unpublished");
    if (System.getProperty("input.dir") != null) {
      algo.setInputPath(System.getProperty("input.dir") + "/unpublished");
    }
    algo.setLastModifiedBy("admin");
    algo.setLastModifiedFlag(true);
    algo.setMolecularActionFlag(false);
    algo.setTerminology("NCIMTH");
    algo.setVersion("latest");
    algo.compute();
    Logger.getLogger(getClass()).info("Finished unpublished loader algorithm");

  }

  /**
   * Teardown.
   *
   * @throws Exception the exception
   */
  @After
  public void teardown() throws Exception {
    algo.close();
  }
}
