/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.jpa.integrity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.jpa.services.validation.DT_PN2;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.test.helpers.IntegrationUnitSupport;

/**
 * Integration testing for {@link DT_PN2}.
 */
public class DT_PN2Test extends IntegrationUnitSupport {

  /** The project. */
  private Project project;

  /** The service. */
  protected ContentServiceJpa contentService;

  /** The concept multi MTHPN. */
  private Concept conceptMultiMTHPN = null;

  /** The concept single MTHPN. */
  private Concept conceptSingleMTHPN = null;

  /** The concept no MTHPN. */
  private Concept conceptNoMTHPN = null;

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
    conceptMultiMTHPN = null;
    conceptSingleMTHPN = null;
    conceptNoMTHPN = null;

    // instantiate service
    contentService = new ContentServiceJpa();

    // make a copy of the validationTest project
    ProjectList projects = contentService.getProjects();
    assertTrue(projects.size() > 0);
    project = new ProjectJpa(projects.getObjects().get(0));

    // Reset the project's validation check list, so only this integrity check
    // will run.
    project.setValidationChecks(new ArrayList<>(Arrays.asList("DT_PN2")));

    // Get three concepts that have MTH atoms
    conceptMultiMTHPN =
        contentService.getConcept("C0004604", "UMLS", "latest", Branch.ROOT);
    conceptSingleMTHPN =
        contentService.getConcept("C0003123", "UMLS", "latest", Branch.ROOT);
    conceptNoMTHPN =
        contentService.getConcept("C0004611", "UMLS", "latest", Branch.ROOT);

    // Set all MTH atoms in conceptMultiMTHPN to have TermType PN
    for (Atom atom : conceptMultiMTHPN.getAtoms()) {
      atom.setTermType("PN");
    }

    // Confirm conceptMultiMTHPN only has a single MTH/PN atom
    int count = 0;
    for (Atom atom : conceptSingleMTHPN.getAtoms()) {
      if (atom.getTerminology().equals("MTH")
          && atom.getTermType().equals("PN")) {
        ++count;
      }
    }
    assertEquals(1, count);

    // Remove all MTH/PN atoms from conceptNoMTHPN
    List<Atom> removeAtoms = new ArrayList<Atom>();
    for (Atom atom : conceptNoMTHPN.getAtoms()) {
      if (atom.getTerminology().equals("MTH")
          && atom.getTermType().equals("PN")) {
        removeAtoms.add(atom);
      }
    }
    for (Atom atom : removeAtoms) {
      conceptNoMTHPN.getAtoms().remove(atom);
    }
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
    // Test violation of DT_PN2
    // Concept contains multiple MTH/PN atoms
    //

    // Check whether the action violates the validation check
    final ValidationResult validationResult =
        contentService.validateConcept(project.getValidationChecks(), conceptMultiMTHPN);

    // Verify that it returned a validation error
    assertFalse(validationResult.isValid());

    //
    // Test non-violation of DT_PN2
    // Concept contains a single MTH/PN atoms
    //

    // Check whether the action violates the validation check
    final ValidationResult validationResult2 =
        contentService.validateConcept(project.getValidationChecks(), conceptSingleMTHPN);

    // Verify that it returned a validation error
    assertTrue(validationResult2.isValid());

    //
    // Test non-violation of DT_PN2
    // Concept contains no MTH/PN atoms
    //

    // Check whether the action violates the validation check
    final ValidationResult validationResult3 =
        contentService.validateConcept(project.getValidationChecks(), conceptNoMTHPN);

    // Verify that returned no validation errors
    assertTrue(validationResult3.isValid());
  }

  /**
   * Teardown.
   *
   * @throws Exception the exception
   */
  @After
  public void teardown() throws Exception {
    // do nothing
  }

  /**
   * Teardown class.
   */
  @AfterClass
  public static void teardownClass() {
    // do nothing
  }

}
