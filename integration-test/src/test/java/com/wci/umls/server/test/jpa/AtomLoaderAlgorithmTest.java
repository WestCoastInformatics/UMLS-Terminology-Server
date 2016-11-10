/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.jpa;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

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
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.ProjectList;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.jpa.ProcessExecutionJpa;
import com.wci.umls.server.jpa.algo.insert.AtomLoaderAlgorithm;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.jpa.services.ProcessServiceJpa;
import com.wci.umls.server.test.helpers.IntegrationUnitSupport;

/**
 * Sample test to get auto complete working.
 */
public class AtomLoaderAlgorithmTest extends IntegrationUnitSupport {

  /** The algorithm. */
  AtomLoaderAlgorithm algo = null;

  /** The process execution. */
  ProcessExecution processExecution = null;

  /** The process service. */
  ProcessServiceJpa processService = null;

  /** The content service. */
  ContentServiceJpa contentService = null;

  /** The project. */
  Project project = null;

  /** The temporary relationships.src file. */
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
    processExecution.setInputPath("terminologies/NCI_INSERT/src");// <- Set this to
    // the standard
    // folder
    // location

    // Create the /temp subdirectory
    final File tempSrcDir = new File(
        ConfigUtility.getConfigProperties().getProperty("source.data.dir")
            + File.separator + processExecution.getInputPath() + File.separator
            + "temp");
    FileUtils.mkdir(tempSrcDir.toString());

    // Reset the processExecution input path to /temp
    processExecution.setInputPath(
        processExecution.getInputPath() + File.separator + "temp");

    // Create and populate a relationships.src document in the /temp
    // temporary subfolder
    outputFile = new File(tempSrcDir, "classes_atoms.src");

    final PrintWriter out = new PrintWriter(new FileWriter(outputFile));
    out.println(
        "362166237|SRC|SRC/VPT|V-NCI_2016_05E|N|Y|N|National Cancer Institute Thesaurus, 2016_05E|N||||ENG|1|");
    out.println(
        "362166238|SRC|SRC/VAB|V-NCI_2016_05E|N|Y|N|NCI_2016_05E|N||||ENG|2|");
    out.println(
        "362166292|NCI_2016_05E|NCI_2016_05E/PT|C28776|R|Y|N|(H115D)VHL35 Peptide|N||C28776||ENG|362166292|");
    out.println(
        "362166293|NCI_2016_05E|NCI_2016_05E/PT|C88126|R|Y|N|1+ Score, WHO|N||C88126||ENG|362166293|");
    out.close();

    // Create and configure the algorithm
    algo = new AtomLoaderAlgorithm();

    // Configure the algorithm (need to do either way)
    algo.setLastModifiedBy("admin");
    algo.setLastModifiedFlag(true);
    algo.setProcess(processExecution);
    algo.setProject(processExecution.getProject());
    algo.setTerminology(processExecution.getTerminology());
    algo.setVersion(processExecution.getVersion());
  }

  /**
   * Test atom loader normal use.
   *
   * @throws Exception the exception
   */
  @Test
  public void testAtomLoader() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    // Run the ATOMLOADER algorithm
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

      // Make sure the atoms in the temporary input file were added.
      SearchResultList list =
          contentService.findConcepts("UMLS", "latest", Branch.ROOT,
              "atoms.nameSort:\"National Cancer Institute Thesaurus, 2016_05E\"",
              null);
      assertEquals(1, list.size());

      list = contentService.findConcepts("UMLS", "latest", Branch.ROOT,
          "atoms.nameSort:\"NCI_2016_05E\"", null);
      assertEquals(1, list.size());

      list = contentService.findConcepts("UMLS", "latest", Branch.ROOT,
          "atoms.nameSort:\"(H115D)VHL35 Peptide\"", null);
      assertEquals(1, list.size());

      list = contentService.findConcepts("UMLS", "latest", Branch.ROOT,
          "atoms.nameSort:\"1+ Score, WHO\"", null);
      assertEquals(1, list.size());

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

    File testDirectory = new File(
        ConfigUtility.getConfigProperties().getProperty("source.data.dir")
        + File.separator + processExecution.getInputPath());
    
    FileUtils.deleteDirectory(testDirectory);

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
