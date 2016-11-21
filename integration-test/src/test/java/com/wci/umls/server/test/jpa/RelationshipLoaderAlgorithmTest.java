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
import com.wci.umls.server.helpers.content.RelationshipList;
import com.wci.umls.server.jpa.ProcessExecutionJpa;
import com.wci.umls.server.jpa.algo.insert.RelationshipLoaderAlgorithm;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.jpa.services.ProcessServiceJpa;
import com.wci.umls.server.services.ContentService;
import com.wci.umls.server.services.ProcessService;
import com.wci.umls.server.test.helpers.IntegrationUnitSupport;

/**
 * Sample test to get auto complete working.
 */
public class RelationshipLoaderAlgorithmTest extends IntegrationUnitSupport {

  /** The algorithm. */
  RelationshipLoaderAlgorithm algo = null;

  /** The process execution. */
  ProcessExecution processExecution = null;

  /** The process service. */
  ProcessService processService = null;

  /** The content service. */
  ContentService contentService = null;

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

    // Create and populate a relationships.src document in the /temp
    // temporary subfolder
    outputFile = new File(tempSrcDir, "relationships.src");

    PrintWriter out = new PrintWriter(new FileWriter(outputFile));
    out.println(
        "1|S|V-NCI_2016_05E|BT|has_version|V-NCI|SRC|SRC|R|Y|N|N|CODE_SOURCE|SRC|CODE_SOURCE|SRC|||");
    out.println(
        "31|S|C63923|RT|Concept_In_Subset|C98033|NCI_2016_05E|NCI_2016_05E|R|Y|N|N|SOURCE_CUI|NCI_2016_05E|SOURCE_CUI|NCI_2016_05E|||");
    out.close();

    // Also create and populate a contexts.src document in the /temp
    // temporary subfolder
    outputFile = new File(tempSrcDir, "contexts.src");

    out = new PrintWriter(new FileWriter(outputFile));
    out.println(
        "362168904|PAR|isa|362174335|NCI_2016_05E|NCI_2016_05E||31926003.362204588.362250568.362175233.362174339.362174335|00|||C37447|SOURCE_CUI|NCI_2016_05E|C1971|SOURCE_CUI|NCI_2016_05E|");
    out.println(
        "362199564|PAR|isa|362199578|NCI_2016_05E|NCI_2016_05E||31926003.362214991.362254908.362254885.362207285.362246398.362199581.362199578|00|||C25948|SOURCE_CUI|NCI_2016_05E|C16484|SOURCE_CUI|NCI_2016_05E|");
    out.close();

    // Create and configure the algorithm
    algo = new RelationshipLoaderAlgorithm();

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
  public void testRelationshipLoader() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    // Run the RELATIONSHIPLOADER algorithm
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

      // Make sure the relationships and inverses in the temporary input file
      // exist (added or updated)
      RelationshipList relList =
          contentService.findCodeRelationships("V-NCI", "SRC", "latest",
              Branch.ROOT, "toTerminologyId:V-NCI_2016_05E", false, null);
      assertEquals(1, relList.size());
      
      relList =
          contentService.findCodeRelationships("V-NCI", "SRC", "latest",
              Branch.ROOT, "fromTerminologyId:V-NCI_2016_05E", true, null);
      assertEquals(1, relList.size());

      relList = contentService.findConceptRelationships("C98033", "NCI",
          "2016_05E", Branch.ROOT, "toTerminologyId:C63923", false, null);
      assertEquals(1, relList.size());

      relList = contentService.findConceptRelationships("C98033", "NCI",
          "2016_05E", Branch.ROOT, "fromTerminologyId:C63923", true, null);
      assertEquals(1, relList.size());
      
      relList = contentService.findConceptRelationships("C37447", "NCI",
          "2016_05E", Branch.ROOT, "toTerminologyId:C1971", false, null);
      assertEquals(1, relList.size());
      
      relList = contentService.findConceptRelationships("C37447", "NCI",
          "2016_05E", Branch.ROOT, "fromTerminologyId:C1971", true, null);
      assertEquals(1, relList.size());
    
      relList = contentService.findConceptRelationships("C25948", "NCI",
          "2016_05E", Branch.ROOT, "toTerminologyId:C16484", false, null);
      assertEquals(1, relList.size());

      relList = contentService.findConceptRelationships("C25948", "NCI",
          "2016_05E", Branch.ROOT, "fromTerminologyId:C16484", true, null);
      assertEquals(1, relList.size());
      
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
