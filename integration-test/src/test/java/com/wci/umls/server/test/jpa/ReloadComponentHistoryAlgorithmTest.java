/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

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
import com.wci.umls.server.jpa.ProcessExecutionJpa;
import com.wci.umls.server.jpa.algo.release.ReloadConceptHistoryAlgorithm;
import com.wci.umls.server.jpa.services.ProcessServiceJpa;
import com.wci.umls.server.model.content.ComponentHistory;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.test.helpers.IntegrationUnitSupport;

/**
 * Sample test to get auto complete working.
 */
public class ReloadComponentHistoryAlgorithmTest
    extends IntegrationUnitSupport {

  /** The algorithm. */
  ReloadConceptHistoryAlgorithm algo = null;

  /** The process execution. */
  ProcessExecution processExecution = null;

  /** The process service. */
  ProcessServiceJpa service = null;

  /** The project. */
  Project project = null;

  /** The project. */
  Concept concept = null;

  /** The pre-algorithm-run ComponentHistory. */
  ComponentHistory preAlgoComponentHistory = null;

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

    service = new ProcessServiceJpa();

    // load the project (should be only one)
    ProjectList projects = service.getProjects();
    assertTrue(projects.size() > 0);
    project = projects.getObjects().get(0);

    // Create a dummy process execution, to store some information the algorithm
    // needs (specifically input Path)
    processExecution = new ProcessExecutionJpa();
    processExecution.setProject(project);
    processExecution.setTerminology(project.getTerminology());
    processExecution.setVersion(project.getVersion());
    processExecution.setInputPath("mr");// <- Set this
                                        // to
    // the standard
    // folder
    // location

    // Create the /temp subdirectory
    final File tempSrcDir = new File(
        ConfigUtility.getConfigProperties().getProperty("source.data.dir") + "/"
            + processExecution.getInputPath() + "/temp");
    FileUtils.mkdir(tempSrcDir.toString());

    // Reset the processExecution input path to /src/temp
    processExecution.setInputPath(processExecution.getInputPath() + "/temp");

    // Create and populate a MRCUI.RRF document in the /temp
    // temporary subfolder
    outputFile = new File(tempSrcDir, "MRCUI.RRF");

    // Create and configure the algorithm
    algo = new ReloadConceptHistoryAlgorithm();

    // Configure the algorithm (need to do either way)
    algo.setLastModifiedBy("admin");
    algo.setLastModifiedFlag(true);
    algo.setProcess(processExecution);
    algo.setProject(processExecution.getProject());
    algo.setTerminology(processExecution.getTerminology());
    algo.setVersion(processExecution.getVersion());
  }

  /**
   * Test reload concept history.
   *
   * @throws Exception the exception
   */
  @Test
  public void testReloadConceptHistoryNoChange() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    // Save a copy of the concept's component History before the algos run, to
    // restore it at the end
    concept = service.getConcept("C0085721", "NCIMTH", "latest", Branch.ROOT);
    preAlgoComponentHistory = concept.getComponentHistory().get(0);

    //
    // Run the algorithm on a line that matches the concept's histories
    //
    PrintWriter out = new PrintWriter(new FileWriter(outputFile));
    out.println("C0085721|201002|SY|||C0000294|Y|");
    out.close();

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

      service = new ProcessServiceJpa();
      concept = service.getConcept(concept.getId());

      assertEquals(1, concept.getComponentHistory().size());

      assertTrue(
          concept.getComponentHistory().contains(preAlgoComponentHistory));

    } catch (Exception e) {
      e.printStackTrace();
      fail("Unexpected exception thrown - please review stack trace.");
    } finally {
      algo.close();
    }
  }

  /**
   * Test reload concept history.
   *
   * @throws Exception the exception
   */
  @Test
  public void testReloadConceptHistoryAdd() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    // Save a copy of the concept's component History before the algos run, to
    // restore it at the end
    concept = service.getConcept("C0085721", "NCIMTH", "latest", Branch.ROOT);
    preAlgoComponentHistory = concept.getComponentHistory().get(0);

    //
    // Run the algorithm on a line that will result in adding a history to the
    // concept
    //
    PrintWriter out = new PrintWriter(new FileWriter(outputFile));
    out.println("C0085721|201002|SY|||C0000294|Y|");
    out.println("C0085721|201002|SY|||C0678115|Y|");
    out.close();

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

      service = new ProcessServiceJpa();
      concept = service.getConcept(concept.getId());

      assertEquals(2, concept.getComponentHistory().size());

    } catch (Exception e) {
      e.printStackTrace();
      fail("Unexpected exception thrown - please review stack trace.");
    } finally {
      algo.close();
    }

  }

  /**
   * Test reload concept history.
   *
   * @throws Exception the exception
   */
  @Test
  public void testReloadConceptHistoryAddAndRemove() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    // Save a copy of the concept's component History before the algos run, to
    // restore it at the end
    concept = service.getConcept("C0085721", "NCIMTH", "latest", Branch.ROOT);
    preAlgoComponentHistory = concept.getComponentHistory().get(0);

    //
    // Run the algorithm on a line that will result in adding a history to the
    // concept, and removing another
    //
    PrintWriter out = new PrintWriter(new FileWriter(outputFile));
    out.println("C0085721|201002|RO|||C0000294|Y|");
    out.close();

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

      service = new ProcessServiceJpa();
      concept = service.getConcept(concept.getId());

      assertEquals(1, concept.getComponentHistory().size());

      ComponentHistory componentHistory = concept.getComponentHistory().get(0);
      assertEquals("RO", componentHistory.getRelationshipType());

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
    FileUtils.forceDelete(outputFile);

    File testDirectory = new File(
        ConfigUtility.getConfigProperties().getProperty("source.data.dir") + "/"
            + processExecution.getInputPath());

    FileUtils.deleteDirectory(testDirectory);

    // Return the concept's component history to pre-algo-run state.
    service = new ProcessServiceJpa();
    service.setLastModifiedBy("admin");
    service.setMolecularActionFlag(false);

    if (service.getComponentHistory(preAlgoComponentHistory.getId()) == null) {
      preAlgoComponentHistory.setId(null);
      preAlgoComponentHistory =
          service.addComponentHistory(preAlgoComponentHistory);
    }

    concept.setComponentHistory(
        new ArrayList<>(Arrays.asList(preAlgoComponentHistory)));
    service.updateConcept(concept);

    service.close();
  }

  /**
   * Teardown class.
   */
  @AfterClass
  public static void teardownClass() {
    // do nothing
  }

}
