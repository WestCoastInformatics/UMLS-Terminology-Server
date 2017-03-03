/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.jpa;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.ProcessExecution;
import com.wci.umls.server.Project;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.ProjectList;
import com.wci.umls.server.jpa.ProcessExecutionJpa;
import com.wci.umls.server.jpa.algo.maint.ProdMidCleanupAlgorithm;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.meta.TerminologyJpa;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.jpa.services.ProcessServiceJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.meta.Terminology;
import com.wci.umls.server.model.workflow.WorkflowStatus;
import com.wci.umls.server.services.ContentService;
import com.wci.umls.server.services.ProcessService;
import com.wci.umls.server.test.helpers.IntegrationUnitSupport;

/**
 * Sample test to get auto complete working.
 */
public class ProdMidCleanupAlgorithmTest extends IntegrationUnitSupport {

  /** The algorithm. */
  ProdMidCleanupAlgorithm algo = null;

  /** The process execution. */
  ProcessExecution processExecution = null;

  /** The process service. */
  ProcessService processService = null;

  /** The content service. */
  ContentService contentService = null;

  /** The project. */
  Project project = null;

  /** The added terminology. */
  Terminology addedTerminology = null;

  /** The added concept. */
  Concept addedConcept = null;

  /** The added atom. */
  Atom addedAtom = null;

  /**
   * Setup class.
   *
   * @throws Exception the exception
   */
  @BeforeClass
  public static void setupClass() throws Exception {
    // n/a
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
    processExecution.setInputPath("terminologies/NCI_INSERT/src"); // <- Set
                                                                   // this to
    // the standard
    // folder
    // location

    //
    // Add a terminology and some content
    //
    final Terminology currentSnomedTerminology =
        contentService.getTerminologyLatestVersion("SNOMEDCT_US");
    final Terminology terminology =
        new TerminologyJpa(currentSnomedTerminology);
    terminology.setId(null);
    terminology.setVersion("Really Old");
    terminology.setCurrent(false);
    addedTerminology = contentService.addTerminology(terminology);

    final Concept concept =
        new ConceptJpa(contentService.getConcept("285407008",
            currentSnomedTerminology.getTerminology(),
            currentSnomedTerminology.getVersion(), Branch.ROOT), false);
    concept.setId(null);
    concept.setTerminology(addedTerminology.getTerminology());
    concept.setVersion(addedTerminology.getVersion());
    concept.setWorkflowStatus(WorkflowStatus.PUBLISHED);
    addedConcept = contentService.addConcept(concept);

    final Atom atom = new AtomJpa();
    atom.setBranch(Branch.ROOT);
    atom.setName("TEST oldId");
    atom.setTerminologyId("TestId");
    atom.setTerminology(addedTerminology.getTerminology());
    atom.setVersion(addedTerminology.getVersion());
    atom.setTimestamp(new Date());
    atom.setPublishable(true);
    atom.setConceptId("TEST Concept Id");
    atom.setLexicalClassId("");
    atom.setStringClassId("");
    atom.setCodeId("");
    atom.setDescriptorId("");
    atom.setLanguage("ENG");
    atom.setTermType("AB");
    atom.setWorkflowStatus(WorkflowStatus.PUBLISHED);
    addedAtom = contentService.addAtom(atom);

    // Create and configure the algorithm
    algo = new ProdMidCleanupAlgorithm();

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
  public void testProdMidCleanup() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    // Confirm the content added in setup is present
    contentService = new ContentServiceJpa();
    contentService.setLastModifiedBy("admin");
    contentService.setMolecularActionFlag(false);

    assertNotNull(contentService.getConcept(addedConcept.getId()));
    assertNotNull(contentService.getAtom(addedAtom.getId()));
    assertNotNull(contentService.getTerminology(
        addedTerminology.getTerminology(), addedTerminology.getVersion()));

    // Run the PRODMIDCLEANUP algorithm
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
      algo.commit();

      // Confirm the concept and atom were removed, but the terminology
      // is still present.
      contentService = new ContentServiceJpa();
      contentService.setLastModifiedBy("admin");
      contentService.setMolecularActionFlag(false);

      addedConcept = contentService.getConcept(addedConcept.getId());
      addedAtom = contentService.getAtom(addedAtom.getId());
      addedTerminology = contentService.getTerminology(
          addedTerminology.getTerminology(), addedTerminology.getVersion());
      assertNull(addedConcept);
      assertNull(addedAtom);
      assertNotNull(addedTerminology);

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
    contentService = new ContentServiceJpa();
    contentService.setLastModifiedBy("admin");
    contentService.setMolecularActionFlag(false);

    if (addedAtom != null) {
      contentService.removeAtom(addedAtom.getId());
    }

    if (addedConcept != null) {
      contentService.removeConcept(addedConcept.getId());
    }

    if (addedTerminology != null) {
      addedTerminology.getFirstReleases().clear();
      addedTerminology.getLastReleases().clear();
      addedTerminology.getRelatedTerminologies().clear();
      addedTerminology.getSynonymousNames().clear();
      addedTerminology.setCitation(null);
      contentService.updateTerminology(addedTerminology);
      contentService.removeTerminology(addedTerminology.getId());
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
