/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.jpa.integrity;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.Project;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.ProjectList;
import com.wci.umls.server.jpa.ProjectJpa;
import com.wci.umls.server.jpa.algo.action.MergeMolecularAction;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.jpa.services.validation.MGV_G;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.test.helpers.IntegrationUnitSupport;

/**
 * Integration testing for {@link MGV_G}.
 */
public class MGV_GTest extends IntegrationUnitSupport {

  /** The project. */
  private Project project;

  /** The service. */
  protected ContentServiceJpa contentService;

  /** The concept MSH current 1. */
  private Concept conceptMSHCurrent1 = null;

  /** The concept MSH current 2. */
  private Concept conceptMSHCurrent2 = null;

  /** The concept MSH previous 3. */
  private Concept conceptMSHPrevious3 = null;

  /** The concept no MSH. */
  private Concept conceptNoMSH = null;

  /** The atom MSH 1. */
  private Atom atomMSH1 = null;

  /** The atom MSH 2. */
  private Atom atomMSH2 = null;

  /** The atom MSH non current. */
  private Atom atomMSHNonCurrent = null;

  /**
   * Setup class.
   *
   * @throws Exception the exception
   */
  @BeforeClass
  public static void setupClass() throws Exception {
    // do nothing
  }

  /**
   * Setup.
   *
   * @throws Exception the exception
   */
  @Before
  public void setup() throws Exception {
    project = null;
    conceptMSHCurrent1 = null;
    conceptMSHCurrent2 = null;
    conceptMSHPrevious3 = null;
    conceptNoMSH = null;
    atomMSH1 = null;
    atomMSH2 = null;
    atomMSHNonCurrent = null;

    // instantiate service
    contentService = new ContentServiceJpa();
    contentService.setLastModifiedBy("admin");
    contentService.setMolecularActionFlag(false);

    // make a copy of the validationTest project
    ProjectList projects = contentService.getProjects();
    assertTrue(projects.size() > 0);
    project = new ProjectJpa(projects.getObjects().get(0));

    // Reset the project's validation check list, so only this integrity check
    // will run.
    project.setValidationChecks(new ArrayList<>(Arrays.asList("MGV_G")));

    // Create three "MSH" atoms with termType "MH".
    for (Atom atom : contentService.getAtoms("", "MSH", "2016_2016_02_26")
        .getObjects()) {
      if (atom.getTerminology().equals("MSH")
          && atom.getTermType().equals("MH")) {
        if (atomMSH1 == null) {
          Atom createAtom = new AtomJpa(atom);
          createAtom.setId(null);
          createAtom = contentService.addAtom(createAtom);
          contentService = new ContentServiceJpa();
          contentService.setLastModifiedBy("admin");
          contentService.setMolecularActionFlag(false);
          atomMSH1 = contentService.getAtom(createAtom.getId());
        } else if (atomMSH2 == null) {
          Atom createAtom = new AtomJpa(atom);
          createAtom.setId(null);
          createAtom = contentService.addAtom(createAtom);
          contentService = new ContentServiceJpa();
          contentService.setLastModifiedBy("admin");
          contentService.setMolecularActionFlag(false);
          atomMSH2 = contentService.getAtom(createAtom.getId());
        } else if (atomMSHNonCurrent == null) {
          Atom createAtom = new AtomJpa(atom);
          createAtom.setId(null);
          // Set this atom to not-publishable (this has a 1:1 relationship to
          // version currentness, and is MUCH faster to look up)
          createAtom.setPublishable(false);
          createAtom = contentService.addAtom(createAtom);
          contentService = new ContentServiceJpa();
          contentService.setLastModifiedBy("admin");
          contentService.setMolecularActionFlag(false);
          atomMSHNonCurrent = contentService.getAtom(createAtom.getId());
          break;
        }
      }
    }

    // Get four UMLS concepts with no MSH/MH atoms, and add retrieve MSH/MH
    // atoms
    conceptMSHCurrent1 =
        contentService.getConcept("C0003123", "UMLS", "latest", Branch.ROOT);
    conceptMSHCurrent2 =
        contentService.getConcept("C0004611", "UMLS", "latest", Branch.ROOT);
    conceptMSHPrevious3 =
        contentService.getConcept("C0338361", "UMLS", "latest", Branch.ROOT);
    conceptNoMSH =
        contentService.getConcept("C0000734", "UMLS", "latest", Branch.ROOT);

    // Add the atoms to the respective concepts
    conceptMSHCurrent1.getAtoms().add(atomMSH1);
    conceptMSHCurrent2.getAtoms().add(atomMSH2);
    conceptMSHPrevious3.getAtoms().add(atomMSHNonCurrent);

    // update the concepts
    contentService.updateConcept(conceptMSHCurrent1);
    contentService = new ContentServiceJpa();
    contentService.setLastModifiedBy("admin");
    contentService.setMolecularActionFlag(false);
    conceptMSHCurrent1 = contentService.getConcept(conceptMSHCurrent1.getId());

    contentService.updateConcept(conceptMSHCurrent2);
    contentService = new ContentServiceJpa();
    contentService.setLastModifiedBy("admin");
    contentService.setMolecularActionFlag(false);
    conceptMSHCurrent2 = contentService.getConcept(conceptMSHCurrent2.getId());

    contentService.updateConcept(conceptMSHPrevious3);
    contentService = new ContentServiceJpa();
    contentService.setLastModifiedBy("admin");
    contentService.setMolecularActionFlag(false);
    conceptMSHPrevious3 =
        contentService.getConcept(conceptMSHPrevious3.getId());

  }

  /**
   * Test merge normal use.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUse() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    //
    // Test violation of MGV_G
    // One concept has previous version MSH/MH atom, and other has current
    // version MSH/MH atom
    //

    // Create and configure the action
    final MergeMolecularAction action3 = new MergeMolecularAction();

    action3.setProject(project);
    action3.setConceptId(conceptMSHCurrent2.getId());
    action3.setConceptId2(conceptMSHPrevious3.getId());
    action3.setLastModifiedBy("admin");
    action3.setLastModified(conceptMSHCurrent2.getLastModified().getTime());
    action3.setOverrideWarnings(false);
    action3.setTransactionPerOperation(false);
    action3.setMolecularActionFlag(true);
    action3.setChangeStatusFlag(true);

    // Check whether the action violates the validation check
    ValidationResult validationResult = checkActionPreconditions(action3);

    // Verify that it returned a validation error
    assertFalse(validationResult.isValid());

    //
    // Test non-violation of MGV_G
    // One concept has no MSH/MH atoms
    //

    // Create and configure the action
    MergeMolecularAction action = new MergeMolecularAction();

    action.setProject(project);
    action.setConceptId(conceptMSHCurrent2.getId());
    action.setConceptId2(conceptNoMSH.getId());
    action.setLastModifiedBy("admin");
    action.setLastModified(conceptMSHCurrent2.getLastModified().getTime());
    action.setOverrideWarnings(false);
    action.setTransactionPerOperation(false);
    action.setMolecularActionFlag(true);
    action.setChangeStatusFlag(true);

    // Check whether the action violates the validation check
    validationResult = checkActionPreconditions(action);

    // Verify that returned no validation errors
    assertTrue(validationResult.isValid());

    //
    // Test non-violation of MGV_G
    // Both concepts' MSH/MH atoms are of current version
    //

    // Create and configure the action
    final MergeMolecularAction action2 = new MergeMolecularAction();

    action2.setProject(project);
    action2.setConceptId(conceptMSHCurrent2.getId());
    action2.setConceptId2(conceptMSHCurrent1.getId());
    action2.setLastModifiedBy("admin");
    action2.setLastModified(conceptMSHCurrent2.getLastModified().getTime());
    action2.setOverrideWarnings(false);
    action2.setTransactionPerOperation(false);
    action2.setMolecularActionFlag(true);
    action2.setChangeStatusFlag(true);

    // Check whether the action violates the validation check
    validationResult = checkActionPreconditions(action2);

    // Verify that it returned a validation error
    assertTrue(validationResult.isValid());

  }

  /**
   * Teardown.
   *
   * @throws Exception the exception
   */
  @After
  public void teardown() throws Exception {

    // Undo the actions that were done in setup
    if (conceptMSHCurrent1 != null && atomMSH1 != null) {
      conceptMSHCurrent1.getAtoms().remove(atomMSH1);

      contentService.updateConcept(conceptMSHCurrent1);
      contentService = new ContentServiceJpa();
      contentService.setLastModifiedBy("admin");
      contentService.setMolecularActionFlag(false);

      contentService.removeAtom(atomMSH1.getId());
      contentService = new ContentServiceJpa();
      contentService.setLastModifiedBy("admin");
      contentService.setMolecularActionFlag(false);
    }
    if (conceptMSHCurrent2 != null && atomMSH2 != null) {
      conceptMSHCurrent2.getAtoms().remove(atomMSH2);

      contentService.updateConcept(conceptMSHCurrent2);
      contentService = new ContentServiceJpa();
      contentService.setLastModifiedBy("admin");
      contentService.setMolecularActionFlag(false);

      contentService.removeAtom(atomMSH2.getId());
      contentService = new ContentServiceJpa();
      contentService.setLastModifiedBy("admin");
      contentService.setMolecularActionFlag(false);
    }
    if (conceptMSHPrevious3 != null && atomMSHNonCurrent != null) {
      conceptMSHPrevious3.getAtoms().remove(atomMSHNonCurrent);

      contentService.updateConcept(conceptMSHPrevious3);
      contentService = new ContentServiceJpa();
      contentService.setLastModifiedBy("admin");
      contentService.setMolecularActionFlag(false);

      contentService.removeAtom(atomMSHNonCurrent.getId());
      contentService = new ContentServiceJpa();
      contentService.setLastModifiedBy("admin");
      contentService.setMolecularActionFlag(false);
    }
  }

  /**
   * Teardown class.
   */
  @AfterClass
  public static void teardownClass() {
    // do nothing
  }

}
