/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.jpa;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import org.apache.log4j.Logger;
import org.codehaus.plexus.util.FileUtils;
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
import com.wci.umls.server.jpa.ProcessExecutionJpa;
import com.wci.umls.server.jpa.algo.insert.SemanticTypeLoaderAlgorithm;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.jpa.services.ProcessServiceJpa;
import com.wci.umls.server.test.helpers.IntegrationUnitSupport;

/**
 * Sample test to get auto complete working.
 */
public class SemanticTypeLoaderAlgorithmTest extends IntegrationUnitSupport {

  /** The algorithm. */
  SemanticTypeLoaderAlgorithm algo = null;

  /** The process execution. */
  ProcessExecution processExecution = null;

  /** The process service. */
  ProcessServiceJpa processService = null;

  /** The content service. */
  ContentServiceJpa contentService = null;

  /** The project. */
  Project project = null;

  /** The temporary .src file. */
  private File outputFile = null;

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
    processExecution.setInputPath("terminologies/NCI_INSERT/src"); // <- Set
                                                                   // this to
    // the standard
    // folder
    // location

    // Create the /temp subdirectory
    final File tempSrcDir = new File(
        ConfigUtility.getConfigProperties().getProperty("source.data.dir")
            + File.separator + processExecution.getInputPath() + File.separator
            + "temp");
    FileUtils.mkdir(tempSrcDir.toString());

    // Reset the processExecution input path to /src/temp
    processExecution.setInputPath(
        processExecution.getInputPath() + File.separator + "temp");

    // Create and populate an attributes.src document in the /temp
    // temporary subfolder
    outputFile = new File(tempSrcDir, "attributes.src");

    PrintWriter out = new PrintWriter(new FileWriter(outputFile));
    out.println(
        "1|362166237|C|SEMANTIC_TYPE|Intellectual Product|SRC|R|Y|N|N|SRC_ATOM_ID|||3d9e88091cf4ebbab774e90c8f6d4052|");
    out.println(
        "34|C98033|S|FDA_UNII_Code|ODN00F2SJG|NCI_2016_05E|R|Y|N|N|SOURCE_CUI|NCI_2016_05E||634eb9dd2339a0f372a5f0b3c7b58fed|");
    out.println(
        "43|C118465|C|SEMANTIC_TYPE|Diagnostic Procedure|E-NCI_2016_05E|R|Y|N|N|SOURCE_CUI|NCI_2016_05E||5186070c98e613d1e688b45c983caea2|");
    out.close();

    // Create and configure the algorithm
    algo = new SemanticTypeLoaderAlgorithm();

    // Configure the algorithm
    algo.setLastModifiedBy("admin");
    algo.setLastModifiedFlag(true);
    algo.setProcess(processExecution);
    algo.setProject(processExecution.getProject());
    algo.setTerminology(processExecution.getTerminology());
    algo.setVersion(processExecution.getVersion());
  }

  /**
   * Test relationships loader normal use.
   *
   * @throws Exception the exception
   */
  @Test
  public void testSemanticTypeLoader() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    // Run the SEMANTICTYPELOADER algorithm
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
    FileUtils.forceDelete(outputFile);

    FileUtils.deleteDirectory(new File(
        ConfigUtility.getConfigProperties().getProperty("source.data.dir")
            + File.separator + processExecution.getInputPath()));

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
