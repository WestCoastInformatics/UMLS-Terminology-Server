/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.jpa;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.Date;
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
import com.wci.umls.server.helpers.QueryType;
import com.wci.umls.server.jpa.ProcessExecutionJpa;
import com.wci.umls.server.jpa.algo.insert.PreInsertionAlgorithm;
import com.wci.umls.server.jpa.algo.maint.QueryActionAlgorithm;
import com.wci.umls.server.jpa.content.SemanticTypeComponentJpa;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.jpa.services.ProcessServiceJpa;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.SemanticTypeComponent;
import com.wci.umls.server.model.meta.IdType;
import com.wci.umls.server.model.workflow.WorkflowStatus;
import com.wci.umls.server.services.ContentService;
import com.wci.umls.server.services.ProcessService;
import com.wci.umls.server.test.helpers.IntegrationUnitSupport;

/**
 * Sample test to get auto complete working.
 */
public class QueryActionAlgorithmTest extends IntegrationUnitSupport {

  /** The algorithm. */
  QueryActionAlgorithm algo = null;

  /** The process execution. */
  ProcessExecution processExecution = null;

  /** The process service. */
  ProcessService processService = null;

  /** The content service. */
  ContentService contentService = null;

  /** The project. */
  Project project = null;

  /** The concept id. */
  Long conceptId;

  /** The sty id. */
  Long styId;

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
    processService.setLastModifiedBy("admin");
    contentService = new ContentServiceJpa();
    contentService.setLastModifiedBy("admin");
    contentService.setMolecularActionFlag(false);

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
    processExecution.setDescription("TESTTEST");
    processExecution.setName("TESTTEST");
    processExecution.setProcessConfigId(1L);
    processExecution.setStartDate(new Date());
    processExecution.setType("Insertion");
    processExecution.setInputPath("terminologies/NCI_INSERT/src");

    // Persist the execution (teardown will remove it later)
    processExecution = processService.addProcessExecution(processExecution);

    // Run the preinsertion algorithm (to populate the
    // executionInfo map on the process execution)
    PreInsertionAlgorithm preInsertionalgo = new PreInsertionAlgorithm();
    preInsertionalgo.setLastModifiedBy("admin");
    preInsertionalgo.setLastModifiedFlag(true);
    preInsertionalgo.setProcess(processExecution);
    preInsertionalgo.setProject(processExecution.getProject());
    preInsertionalgo.setTerminology("NCI");
    preInsertionalgo.setVersion("2016_05E");
    preInsertionalgo.compute();

    // Create and configure the algorithm
    algo = new QueryActionAlgorithm();

    // Configure the algorithm
    algo.setLastModifiedBy("admin");
    algo.setLastModifiedFlag(true);
    algo.setProcess(processExecution);
    algo.setProject(processExecution.getProject());
    algo.setTerminology("NCI");
    algo.setVersion("2016_05E");

  }

  /**
   * Test query action.
   *
   * @throws Exception the exception
   */
  @Test
  public void testQueryAction() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    // Create a new SemanticTypeComponent and attach it to concept 1, so the
    // resolver can identify and remove it.
    conceptId = 1L;

    // Create semantic type component
    Concept concept = contentService.getConcept(conceptId);

    SemanticTypeComponent sty = new SemanticTypeComponentJpa();
    sty.setTerminologyId("");
    sty.setObsolete(false);
    sty.setPublishable(true);
    sty.setPublished(false);
    sty.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
    sty.setSemanticType("TESTHormoneTEST");
    sty.setTerminology(project.getTerminology());
    sty.setVersion(project.getVersion());
    sty.setTimestamp(new Date());

    // Create the sty, and add it to the concept
    contentService.addSemanticTypeComponent(sty, concept);
    styId = sty.getId();
    contentService = new ContentServiceJpa();
    contentService.setLastModifiedBy("admin");
    contentService.setMolecularActionFlag(false);

    concept.getSemanticTypes().add(sty);
    contentService.updateConcept(concept);
    contentService = new ContentServiceJpa();
    contentService.setLastModifiedBy("admin");
    contentService.setMolecularActionFlag(false);

    // Confirm concept contains the new semantic type component
    concept = contentService.getConcept(conceptId);
    boolean conceptHasHormoneSty = false;
    for (SemanticTypeComponent conceptSty : concept.getSemanticTypes()) {
      if (conceptSty.getSemanticType().equals("TESTHormoneTEST")) {
        conceptHasHormoneSty = true;
        break;
      }
    }
    assertTrue(conceptHasHormoneSty);

    // Run the QUERYACTION algorithm
    try {

      algo.setTransactionPerOperation(false);
      algo.beginTransaction();

      //
      // Set properties for the algorithm
      // The passed-in query will return just the newly created Semantic Type
      //

      Properties algoProperties = new Properties();
      algoProperties.put("objectType", IdType.SEMANTIC_TYPE.toString());
      algoProperties.put("action", "Remove");
      algoProperties.put("queryType", QueryType.JQL.toString());
      algoProperties.put("query",
          "SELECT s.id FROM SemanticTypeComponentJpa s WHERE s.id > "
              + processExecution.getExecutionInfo().get("maxStyIdPreInsertion"));
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

      // Confirm the new semantic type component was removed from the concept
      contentService.updateConcept(concept);
      contentService = new ContentServiceJpa();
      contentService.setLastModifiedBy("admin");
      contentService.setMolecularActionFlag(false);

      concept = contentService.getConcept(conceptId);
      conceptHasHormoneSty = false;
      for (SemanticTypeComponent conceptSty : concept.getSemanticTypes()) {
        if (conceptSty.getSemanticType().equals("TESTHormoneTEST")) {
          conceptHasHormoneSty = true;
          break;
        }
      }
      assertFalse(conceptHasHormoneSty);

      // Confirm the semantic type itself was also removed
      assertNull(contentService.getSemanticTypeComponent(styId));

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

    processService = new ProcessServiceJpa();
    processService.setLastModifiedBy("admin");
    contentService = new ContentServiceJpa();
    contentService.setLastModifiedBy("admin");
    contentService.setMolecularActionFlag(false);

    processExecution =
        processService.getProcessExecution(processExecution.getId());
    processService.removeProcessExecution(processExecution.getId());

    // Remove the Sty added in the test, if the algorithm missed it.
    if (contentService.getSemanticTypeComponent(styId) != null) {
      Concept concept = contentService.getConcept(conceptId);
      SemanticTypeComponent sty =
          contentService.getSemanticTypeComponent(styId);
      concept.getSemanticTypes().remove(sty);
      contentService.updateConcept(concept);
      contentService.removeSemanticTypeComponent(styId);
    }

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
