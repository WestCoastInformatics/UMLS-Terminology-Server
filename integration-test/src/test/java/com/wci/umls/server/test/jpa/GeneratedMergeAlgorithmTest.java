/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.jpa;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
import com.wci.umls.server.jpa.ProcessExecutionJpa;
import com.wci.umls.server.jpa.algo.insert.GeneratedMergeAlgorithm;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.jpa.services.ProcessServiceJpa;
import com.wci.umls.server.services.ContentService;
import com.wci.umls.server.services.ProcessService;
import com.wci.umls.server.test.helpers.IntegrationUnitSupport;

/**
 * Sample test to get auto complete working.
 */
public class GeneratedMergeAlgorithmTest extends IntegrationUnitSupport {

  /** The algorithm. */
  GeneratedMergeAlgorithm algo = null;

  /** The process execution. */
  ProcessExecution processExecution = null;

  /** The process service. */
  ProcessService processService = null;

  /** The content service. */
  ContentService contentService = null;

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
    contentService = new ContentServiceJpa();

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
    processExecution.setInputPath("terminologies/NCI_INSERT/src");
    processExecution.getExecutionInfo().put("maxAtomIdPreInsertion", "374673");
                    
    // Create and configure the algorithm
    algo = new GeneratedMergeAlgorithm();

    // Configure the algorithm
    algo.setLastModifiedBy("admin");
    algo.setLastModifiedFlag(true);
    algo.setProcess(processExecution);
    algo.setProject(processExecution.getProject());
    algo.setTerminology("NCI");
    algo.setVersion("2016_05E");

  }

  /**
   * Test generated merge normal use.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGeneratedMerge() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    // Run the PRECOMPUTEDMERGE algorithm
    try {

      algo.setTransactionPerOperation(false);
      algo.beginTransaction();

      //
      // Set properties for the algorithm
      //
      Properties algoProperties = new Properties();
      algoProperties.put("queryType", "JQL");
      algoProperties.put("query",
          "select a1.id, a2.id "
              + "from ConceptJpa c1 join c1.atoms a1, ConceptJpa c2 join c2.atoms a2 "             
//              + "where c1.terminology = :projectTerminology "
//              + "and c2.terminology = :projectTerminology "
//              + "and c1.id != c2.id " 
//              + "and a1.terminology = :terminology "
//              + "and a1.version = :version "
//              + "and a2.terminology = :terminology "
//              + "and a2.version = :version "
              + "where a1.id in (100,1) "
              + "and a2.id in (2,99,5) ");
//              + "and a1.codeId = a2.codeId "
//              + "and a1.stringClassId = a2.stringClassId "
//              + "and a1.termType = a2.termType");
      algoProperties.put("checkNames", "MGV_A4;MGV_B;MGV_C");
      algoProperties.put("newAtomsOnly", "false");
      algoProperties.put("filterQueryType", "LUCENE");
      algoProperties.put("filterQuery", "atoms.id:(1)");
//      algoProperties.put("filterQueryType", "JQL");
//      algoProperties.put("filterQuery", "select a1.id, a2.id "
//          + "from ConceptJpa c1 join c1.atoms a1, ConceptJpa c2 join c2.atoms a2 "             
//          + "where a1.id in (100,1) "
//          + "and a2.id in (2,99) ");
      algoProperties.put("makeDemotions", "true");
      algoProperties.put("changeStatus", "true");
      algoProperties.put("mergeSet", "NCI-SY");
      algo.setProperties(algoProperties);

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
      fail("Unexpected exception thrown - please review stack trace.");
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

    processService.close();
    contentService.close();
  }

  /**
   * Teardown class.
   */
  @AfterClass
  public static void teardownClass() {
    // do nothing
  }

}
