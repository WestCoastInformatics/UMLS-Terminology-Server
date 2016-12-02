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
import com.wci.umls.server.jpa.services.validation.MGV_NCIPN;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.test.helpers.IntegrationUnitSupport;

/**
 * Integration testing for {@link MGV_NCIPN}.
 */
public class MGV_NCIPNTest extends IntegrationUnitSupport {

  /** The project. */
  private Project project;

  /** The service. */
  protected ContentServiceJpa contentService;

  /**  The concept NCIPN 1. */
  private Concept conceptNCIPN1 = null;

  /**  The concept NCIPN 2. */
  private Concept conceptNCIPN2 = null;

  /**  The concept no NCIPN. */
  private Concept conceptNoNCIPN = null;

  /**  The atom NCIMTH 1. */
  private Atom atomNCIMTH1 = null;

  /**  The atom NCIMTH 2. */
  private Atom atomNCIMTH2 = null;

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
    conceptNCIPN1 = null;
    conceptNCIPN2 = null;
    conceptNoNCIPN = null;
    atomNCIMTH1 = null;
    atomNCIMTH2 = null;

    // instantiate service
    contentService = new ContentServiceJpa();
    contentService.setLastModifiedBy("admin");
    contentService.setMolecularActionFlag(false);

    // make a copy of the validationTest project
    ProjectList projects = contentService.getProjects();
    assertTrue(projects.size() > 0);
    project = new ProjectJpa(projects.getObjects().get(0));
    
    // Create two atoms with Terminology="NCIMTH", and TermType="PN"
    for (Atom atom : contentService.getAtoms("", "MSH", "2016_2016_02_26")
        .getObjects()) {
      if (atomNCIMTH1 == null) {
        Atom createAtom = new AtomJpa(atom);
        createAtom.setId(null);
        createAtom.setTerminology("NCIMTH");
        createAtom.setTermType("PN");
        createAtom = contentService.addAtom(createAtom);
        contentService = new ContentServiceJpa();
        contentService.setLastModifiedBy("admin");
        contentService.setMolecularActionFlag(false);
        atomNCIMTH1 = contentService.getAtom(createAtom.getId());
      } else if (atomNCIMTH2 == null) {
        Atom createAtom = new AtomJpa(atom);
        createAtom.setId(null);
        createAtom.setTerminology("NCIMTH");
        createAtom.setTermType("PN");
        createAtom = contentService.addAtom(createAtom);
        contentService = new ContentServiceJpa();
        contentService.setLastModifiedBy("admin");
        contentService.setMolecularActionFlag(false);
        atomNCIMTH2 = contentService.getAtom(createAtom.getId());
      } else {
        break;
      }
    }

    // Get three UMLS concepts, and add retrieve NCIMTH/PN atoms to two
    conceptNCIPN1 =
        contentService.getConcept("C0003123", "UMLS", "latest", Branch.ROOT);
    conceptNCIPN2 =
        contentService.getConcept("C0004611", "UMLS", "latest", Branch.ROOT);
    conceptNoNCIPN =
        contentService.getConcept("C0000734", "UMLS", "latest", Branch.ROOT);

    // Add the atoms to the respective concepts
    conceptNCIPN1.getAtoms().add(atomNCIMTH1);
    conceptNCIPN2.getAtoms().add(atomNCIMTH2);

    // update the concepts
    contentService.updateConcept(conceptNCIPN1);
    contentService = new ContentServiceJpa();
    contentService.setLastModifiedBy("admin");
    contentService.setMolecularActionFlag(false);
    conceptNCIPN1 = contentService.getConcept(conceptNCIPN1.getId());

    contentService.updateConcept(conceptNCIPN2);
    contentService = new ContentServiceJpa();
    contentService.setLastModifiedBy("admin");
    contentService.setMolecularActionFlag(false);
    conceptNCIPN2 = contentService.getConcept(conceptNCIPN2.getId());

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
    // Test violation of MGV_NCIPN
    // Concepts contain "NCIMTH" atoms with TermType "PN"
    //

    // Create and configure the action
    final MergeMolecularAction action = new MergeMolecularAction();

    action.setProject(project);
    action.setConceptId(conceptNCIPN2.getId());
    action.setConceptId2(conceptNCIPN1.getId());
    action.setLastModifiedBy("admin");
    action.setLastModified(conceptNCIPN2.getLastModified().getTime());
    action.setOverrideWarnings(false);
    action.setTransactionPerOperation(false);
    action.setMolecularActionFlag(true);
    action.setChangeStatusFlag(true);
    action.setValidationChecks(new ArrayList<>(Arrays.asList("MGV_NCIPN")));

    // Check whether the action violates the validation check
    final ValidationResult validationResult = checkActionPreconditions(action);

    // Verify that it returned a validation error
    assertFalse(validationResult.isValid());

    //
    // Test non-violation of MGV_NCIPN
    // Only one concept contains "NCIMTH" atoms with TermType "PN"
    //

    // Create and configure the action
    final MergeMolecularAction action2 = new MergeMolecularAction();

    action2.setProject(project);
    action2.setConceptId(conceptNCIPN2.getId());
    action2.setConceptId2(conceptNoNCIPN.getId());
    action2.setLastModifiedBy("admin");
    action2.setLastModified(conceptNCIPN2.getLastModified().getTime());
    action2.setOverrideWarnings(false);
    action2.setTransactionPerOperation(false);
    action2.setMolecularActionFlag(true);
    action2.setChangeStatusFlag(true);
    action2.setValidationChecks(new ArrayList<>(Arrays.asList("MGV_NCIPN")));

    // Check whether the action violates the validation check
    final ValidationResult validationResult2 =
        checkActionPreconditions(action2);

    // Verify that returned no validation errors
    assertTrue(validationResult2.isValid());

  }

  /**
   * Teardown.
   *
   * @throws Exception the exception
   */
  @After
  public void teardown() throws Exception {

    // Undo the actions that were done in setup
    if (conceptNCIPN1 != null && atomNCIMTH1 != null) {
      conceptNCIPN1.getAtoms().remove(atomNCIMTH1);

      contentService.updateConcept(conceptNCIPN1);
      contentService = new ContentServiceJpa();
      contentService.setLastModifiedBy("admin");
      contentService.setMolecularActionFlag(false);

      contentService.removeAtom(atomNCIMTH1.getId());
      contentService = new ContentServiceJpa();
      contentService.setLastModifiedBy("admin");
      contentService.setMolecularActionFlag(false);
    }
    if (conceptNCIPN2 != null && atomNCIMTH2 != null) {
      conceptNCIPN2.getAtoms().remove(atomNCIMTH2);

      contentService.updateConcept(conceptNCIPN2);
      contentService = new ContentServiceJpa();
      contentService.setLastModifiedBy("admin");
      contentService.setMolecularActionFlag(false);

      contentService.removeAtom(atomNCIMTH2.getId());
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
