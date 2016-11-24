/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package com.wci.umls.server.test.algo;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.wci.umls.server.helpers.LogEntry;
import com.wci.umls.server.jpa.algo.rel.ComputePreferredNamesAlgorithm;

/**
 * Integration testing for {@link ComputePreferredNamesAlgorithm}.
 */
public class ComputePreferredNamesAlgorithmTest
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
    algo = new ComputePreferredNamesAlgorithm();
    algo.setProject(getProject());
    algo.setTerminology(getProject().getTerminology());
    algo.setVersion(getProject().getVersion());
    algo.setLastModifiedBy("admin");
  }

  /**
   * Test check preconditions.
   *
   * @throws Exception the exception
   */
  @Test
  public void testCheckPreconditions() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());
    algo.checkPreconditions();
    // expect no failure
  }

  /**
   * Test reset.
   *
   * @throws Exception the exception
   */
  @Test
  public void testReset() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());
    algo.reset();
    // expect no failure
  }

  /**
   * Test compute.
   *
   * @throws Exception the exception
   */
  @Test
  public void testCompute() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());
    final String workId = compute(algo);

    // Get the log from the algorithm and analyze for changes
    List<LogEntry> entries =
        getLogService().findLogEntries("workId:" + workId, null);
    for (final LogEntry entry : entries) {
      System.out.println(entry.getMessage());
    }
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
