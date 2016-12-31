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
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.ProjectList;
import com.wci.umls.server.jpa.ProcessConfigJpa;
import com.wci.umls.server.jpa.ProcessExecutionJpa;
import com.wci.umls.server.jpa.algo.insert.GeneratedMergeAlgorithm;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.jpa.services.ProcessServiceJpa;
import com.wci.umls.server.jpa.services.rest.ProcessServiceRest;
import com.wci.umls.server.rest.client.ProcessClientRest;
import com.wci.umls.server.rest.client.SecurityClientRest;
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
  ProcessConfigJpa processConfig = null;

  /** The process execution. */
  ProcessExecution processExecution = null;

  /** The process service. */
  ProcessService processService = null;

  /** The content service. */
  ContentService contentService = null;

  /** The content service. */
  SecurityClientRest securityService = null;

  /** The process service rest. */
  ProcessServiceRest processServiceRest = null;

  /** The project. */
  Project project = null;

  /** The auth token. */
  String authToken = null;

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

    // instantiate properties
    final Properties properties = ConfigUtility.getConfigProperties();

    // instantiate required services
    processServiceRest = new ProcessClientRest(properties);

    processService = new ProcessServiceJpa();
    processService.setLastModifiedBy("admin");
    securityService = new SecurityClientRest(properties);
    contentService = new ContentServiceJpa();

    // authentication
    authToken =
        securityService.authenticate(properties.getProperty("admin.user"),
            properties.getProperty("admin.password")).getAuthToken();

    // load the project (should be only one)
    ProjectList projects = processService.getProjects();
    assertTrue(projects.size() > 0);
    project = projects.getObjects().get(0);

    // Create and save a process config - this is only needed for TestQuery.
    processConfig = new ProcessConfigJpa();
    processConfig.setTerminology("NCI");
    processConfig.setVersion("2016_05E");
    processConfig.setType("INSERTION");
    processConfig.setName("Test GeneratedMergeAlgorithm");
    processConfig.setDescription("Test GeneratedMergeAlgorithm");
    processConfig.setProject(project);
    processService.addProcessConfig(processConfig);

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
      algoProperties.put("query", "select a1.id, a2.id "
          + "from ConceptJpa c1 join c1.atoms a1, ConceptJpa c2 join c2.atoms a2 "
          + "where a1.id in (9999999999) " + "and a2.id in (2,99,5) ");
      algoProperties.put("checkNames", "MGV_A4;MGV_B;MGV_C");
      algoProperties.put("newAtomsOnly", "false");
      algoProperties.put("filterQueryType", "LUCENE");
      algoProperties.put("filterQuery", "atoms.id:(1)");
      algoProperties.put("makeDemotions", "true");
      algoProperties.put("changeStatus", "true");
      algoProperties.put("mergeSet", "NCI-SY");
      algo.setProperties(algoProperties);

      //
      // Test the queries
      //
      try {
        Integer result = processServiceRest.testQuery(project.getId(),
            processConfig.getId(), algoProperties.getProperty("queryType"),
            algoProperties.getProperty("query"), "ConceptJpa", authToken);
        assertTrue(result >= 0);
      } catch (Exception e) {
        assertTrue(e.getMessage().startsWith("Query malformed:"));
      }

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

    if (processConfig != null) {
      processService.removeProcessConfig(processConfig.getId());
    }
    // logout
    securityService.logout(authToken);
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
