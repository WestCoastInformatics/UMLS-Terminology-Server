/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package com.wci.umls.server.test.algo;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.wci.umls.server.ProcessExecution;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.LogEntry;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.jpa.ProcessExecutionJpa;
import com.wci.umls.server.jpa.algo.rel.ComputePreferredNamesAlgorithm;
import com.wci.umls.server.jpa.algo.rel.CreateNewReleaseAlgorithm;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.model.content.Concept;

/**
 * Integration testing for {@link ComputePreferredNamesAlgorithm}.
 */
public class CreateNewReleaseAlgorithmTest extends AlgoIntegrationTestSupport {

  /** The algo. */
  private CreateNewReleaseAlgorithm algo;

  /**
   * Setup.
   *
   * @throws Exception the exception
   */
  @Before
  public void setup() throws Exception {
    final ProcessExecution process = new ProcessExecutionJpa();
    process.setInputPath("");
    algo = new CreateNewReleaseAlgorithm();
    algo.setProject(getProject());
    algo.setProcess(process);
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
    final ValidationResult result = algo.checkPreconditions();
    Logger.getLogger(
        "  errors = " + result.getErrors().size() + ", " + result.getErrors());
    assertTrue(result.getErrors().size() >= 3);
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

    // Break one concept preferred name
    final PfsParameter pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(1);
    final SearchResultList list =
        getLogService().findConcepts(getProject().getTerminology(),
            getProject().getVersion(), Branch.ROOT, null, pfs);
    final Concept concept =
        getLogService().getConcept(list.getObjects().get(0).getId());
    concept.setName("xyz");
    getLogService().updateConcept(concept);

    final String workId = compute(algo);

    // Get the log from the algorithm and analyze for changes
    List<LogEntry> entries =
        getLogService().findLogEntries("workId:" + workId, null);
    boolean found = false;
    for (final LogEntry entry : entries) {
      if (entry.getMessage().contains("concepts updated = 1")) {
        found = true;
      }
    }
    assertTrue("Integration test failed to update one concept preferred name",
        found);
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
