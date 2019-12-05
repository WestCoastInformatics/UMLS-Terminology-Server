/*
 **    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.Project;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.ProjectList;
import com.wci.umls.server.jpa.algo.action.AddDemotionMolecularAction;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.AtomRelationshipJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.model.actions.AtomicAction;
import com.wci.umls.server.model.actions.MolecularAction;
import com.wci.umls.server.model.actions.MolecularActionList;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.AtomRelationship;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.workflow.WorkflowStatus;
import com.wci.umls.server.test.helpers.IntegrationUnitSupport;

/**
 * Unit testing for {@link AddDemotionMolecularAction}.
 */
public class AddDemotionTest extends IntegrationUnitSupport {

  /** The service. */
  protected static ContentServiceJpa contentService;

  /** The properties. */
  protected static Properties properties;

  /** The add demotion action/service. */
  protected static AddDemotionMolecularAction action;

  /** The concept. */
  private Concept concept;

  /** The concept 2. */
  private Concept concept2;

  /** The atom. */
  private Atom atom;

  /** The atom 2. */
  private Atom atom2;

  /** The project. */
  private static Project project;

  /** The umls terminology. */
  private String umlsTerminology = "MTH";

  /** The umls version. */
  private String umlsVersion = "latest";

  /**
   * Create test fixtures for class.
   *
   * @throws Exception the exception
   */
  @BeforeClass
  public static void setupClass() throws Exception {

    // instantiate properties
    properties = ConfigUtility.getConfigProperties();

    // instantiate services
    contentService = new ContentServiceJpa();

  }

  /**
   * Setup.
   *
   * @throws Exception the exception
   */
  @Before
  public void setup() throws Exception {

    // ensure there is a concept associated with the project
    ProjectList projects = contentService.getProjects();
    assertTrue(projects.size() > 0);
    project = projects.getObjects().get(0);

    // verify terminology has expected value
    assertTrue(project.getTerminology().equals(umlsTerminology));

    // instantiate required service objects
    contentService = new ContentServiceJpa();
    contentService.setLastModifiedBy("admin");
    contentService.setMolecularActionFlag(false);

    // Copy existing concept to avoid messing with actual database data.
    ConceptJpa conceptJpa = new ConceptJpa(contentService.getConcept("C0000294",
        umlsTerminology, umlsVersion, null), false);
    conceptJpa.setId(null);
    conceptJpa.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);
    conceptJpa = (ConceptJpa) contentService.addConcept(conceptJpa);

    // Re-instantiate service so it can pickup the changed concept.
    contentService = new ContentServiceJpa();
    contentService.setLastModifiedBy("admin");
    contentService.setMolecularActionFlag(false);

    concept = contentService.getConcept(conceptJpa.getId());

    // Copy a second existing concept to avoid messing with actual database
    // data.
    ConceptJpa conceptJpa2 = new ConceptJpa(contentService
        .getConcept("C0002073", umlsTerminology, umlsVersion, null), false);
    conceptJpa2.setId(null);
    conceptJpa2.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);
    conceptJpa2 = (ConceptJpa) contentService.addConcept(conceptJpa2);

    // Re-instantiate service so it can pickup the changed concept.
    contentService = new ContentServiceJpa();
    contentService.setLastModifiedBy("admin");
    contentService.setMolecularActionFlag(false);

    concept2 = contentService.getConcept(conceptJpa2.getId());

    //
    // Create and add atoms to concepts
    //
    atom = new AtomJpa();
    atom.setBranch(Branch.ROOT);
    atom.setName("DCB");
    atom.setTerminologyId("TestId");
    atom.setTerminology(umlsTerminology);
    atom.setVersion(umlsVersion);
    atom.setTimestamp(new Date());
    atom.setPublishable(true);
    atom.setCodeId("C44314");
    atom.setConceptId("M0023181");
    atom.getConceptTerminologyIds().put(concept.getTerminology(),
        concept.getTerminologyId());
    atom.setDescriptorId("");
    atom.setLanguage("ENG");
    atom.setTermType("AB");
    atom.setLexicalClassId("");
    atom.setStringClassId("");
    atom.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);
    atom = contentService.addAtom(atom);

    atom2 = new AtomJpa();
    atom2.setBranch(Branch.ROOT);
    atom2.setName("17 Oxosteroids");
    atom2.setTerminologyId("TestId");
    atom2.setTerminology(umlsTerminology);
    atom2.setVersion(umlsVersion);
    atom2.setTimestamp(new Date());
    atom2.setPublishable(true);
    atom2.setCodeId("D015068");
    atom2.setConceptId("M0023181");
    atom2.getConceptTerminologyIds().put(concept2.getTerminology(),
        concept2.getTerminologyId());
    atom2.setDescriptorId("D015068");
    atom2.setLanguage("ENG");
    atom2.setTermType("PM");
    atom2.setLexicalClassId("");
    atom2.setStringClassId("");
    atom2.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);
    atom2 = contentService.addAtom(atom2);

    // Re-instantiate service so it can pickup the added atoms.
    contentService = new ContentServiceJpa();
    contentService.setLastModifiedBy("admin");
    contentService.setMolecularActionFlag(false);

    // add the atoms to the concepts
    concept.getAtoms().add(atom);
    contentService.updateConcept(concept);

    concept2.getAtoms().add(atom2);
    contentService.updateConcept(concept2);

    // Re-instantiate service so it can pickup the updated concepts.
    contentService = new ContentServiceJpa();
    contentService.setLastModifiedBy("admin");
    contentService.setMolecularActionFlag(false);

    concept = contentService.getConcept(concept.getId());
    concept2 = contentService.getConcept(concept2.getId());
  }

  // TODO - make integration tests for UNDO/REDO

  /**
   * /** Test normal use of the helper object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testAddDemotionNormalUse() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    //
    // Prepare the test and check prerequisites
    //
    // Due to MySQL rounding to the second, we must also round our comparison
    // startDate.
    Date startDate = DateUtils.round(new Date(), Calendar.SECOND);

    // Add a DEMOTION between atoms contained in two different concepts
    final AddDemotionMolecularAction action = new AddDemotionMolecularAction();
    ValidationResult validationResult = null;
    try {

      // Configure the action
      action.setProject(project);
      action.setConceptId(concept.getId());
      action.setConceptId2(concept2.getId());
      action.setLastModifiedBy("admin");
      action.setLastModified(concept.getLastModified().getTime());
      action.setOverrideWarnings(false);
      action.setTransactionPerOperation(false);
      action.setMolecularActionFlag(true);
      action.setChangeStatusFlag(true);
      action.setTerminology(umlsTerminology);
      action.setVersion(umlsVersion);

      action.setAtomId(atom.getId());
      action.setAtomId2(atom2.getId());

      // Perform the action
      validationResult = action.performMolecularAction(action, "admin", true, false);

    } catch (Exception e) {
      action.rollback();
    } finally {
      action.close();
    }

    assertNotNull(validationResult);
    assertTrue(validationResult.getErrors().isEmpty());

    // Re-instantiate service so it can pickup the changed concept and atoms
    contentService = new ContentServiceJpa();
    contentService.setLastModifiedBy("admin");

    // Verify the concept's workflow Status has updated
    concept = contentService.getConcept(concept.getId());
    assertEquals(WorkflowStatus.NEEDS_REVIEW, concept.getWorkflowStatus());

    concept2 = contentService.getConcept(concept2.getId());
    assertEquals(WorkflowStatus.NEEDS_REVIEW, concept2.getWorkflowStatus());

    // Verify the demotion and its inverse has been added to the atoms
    atom = contentService.getAtom(atom.getId());
    atom2 = contentService.getAtom(atom2.getId());

    boolean demotionPresent = false;
    for (AtomRelationship atomRel : atom.getRelationships()) {
      if (atomRel.getWorkflowStatus().equals(WorkflowStatus.DEMOTION)
          && atomRel.getTo().getId().equals(atom2.getId())) {
        demotionPresent = true;
        break;
      }
    }
    assertTrue(demotionPresent);

    boolean inverseDemotionPresent = false;
    for (AtomRelationship atomRel : atom2.getRelationships()) {
      if (atomRel.getWorkflowStatus().equals(WorkflowStatus.DEMOTION)
          && atomRel.getTo().getId().equals(atom.getId())) {
        inverseDemotionPresent = true;
        break;
      }
    }
    assertTrue(inverseDemotionPresent);

    // verify the molecular action exists
    PfsParameterJpa pfs = new PfsParameterJpa();
    pfs.setSortField("lastModified");
    pfs.setAscending(false);
    MolecularActionList list = contentService.findMolecularActions(
        concept.getId(), umlsTerminology, umlsVersion, null, pfs);
    assertTrue(list.size() > 0);
    MolecularAction ma = list.getObjects().get(0);
    assertNotNull(ma);
    assertEquals(concept.getId(), ma.getComponentId());
    assertTrue(ma.getLastModified().compareTo(startDate) >= 0);
    assertNotNull(ma.getAtomicActions());

    // Verify that atomic actions exists for updating atoms, semantic types,
    // relationships, and concept
    // 2 for creating DEMOTION Relationship and inverse
    // 2 for adding DEMOTION relationship and inverse to Atoms
    // 2 for updating Atom workflow status
    // 2 for updating Concept workflow status

    pfs.setSortField(null);

    List<AtomicAction> atomicActions =
        contentService.findAtomicActions(ma.getId(), null, pfs).getObjects();
    Collections.sort(atomicActions,
        (a1, a2) -> a1.getId().compareTo(a2.getId()));
    assertEquals(8, atomicActions.size());
    assertEquals("RELATIONSHIP", atomicActions.get(0).getIdType().toString());
    assertNull(atomicActions.get(0).getOldValue());
    assertNotNull(atomicActions.get(0).getNewValue());
    assertEquals("RELATIONSHIP", atomicActions.get(1).getIdType().toString());
    assertNull(atomicActions.get(1).getOldValue());
    assertNotNull(atomicActions.get(1).getNewValue());
    assertEquals("ATOM", atomicActions.get(2).getIdType().toString());
    assertNull(atomicActions.get(2).getOldValue());
    assertNotNull(atomicActions.get(2).getNewValue());
    assertEquals("relationships", atomicActions.get(2).getField());
    assertEquals("ATOM", atomicActions.get(3).getIdType().toString());
    assertNull(atomicActions.get(3).getOldValue());
    assertNotNull(atomicActions.get(3).getNewValue());
    assertEquals("relationships", atomicActions.get(3).getField());
    assertEquals("ATOM", atomicActions.get(4).getIdType().toString());
    assertNotNull(atomicActions.get(4).getOldValue());
    assertNotNull(atomicActions.get(4).getNewValue());
    assertEquals("ATOM", atomicActions.get(5).getIdType().toString());
    assertNotNull(atomicActions.get(5).getOldValue());
    assertNotNull(atomicActions.get(5).getNewValue());
    assertEquals("CONCEPT", atomicActions.get(6).getIdType().toString());
    assertNotNull(atomicActions.get(6).getOldValue());
    assertNotNull(atomicActions.get(6).getNewValue());
    assertEquals("CONCEPT", atomicActions.get(7).getIdType().toString());
    assertNotNull(atomicActions.get(7).getOldValue());
    assertNotNull(atomicActions.get(7).getNewValue());

  }

  /*
   * Test degenerate use of the helper object.
   * 
   * @throws Exception the exception
   */
  /**
   * Test helper degenerate use.
   *
   * @throws Exception the exception
   */
  @Test
  public void testHelperDegenerateUse() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());
    // n/a
  }

  /**
   * Test edge cases of the helper object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testHelperEdgeCases() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());
    // n/a
  }

  /**
   * Teardown.
   *
   * @throws Exception the exception
   */
  @After
  public void teardown() throws Exception {
    // Delete copies of concepts created during this test
    contentService = new ContentServiceJpa();
    contentService.setLastModifiedBy("admin");
    contentService.setMolecularActionFlag(false);

    if (concept != null && contentService.getConcept(concept.getId()) != null) {
      contentService.removeConcept(concept.getId());
    }
    if (concept2 != null
        && contentService.getConcept(concept2.getId()) != null) {
      contentService.removeConcept(concept2.getId());
    }
    // Remove demotions from Atoms
    if (atom != null && contentService.getAtom(atom.getId()) != null) {
      for (AtomRelationship atomRel : new ArrayList<AtomRelationship>(
          atom.getRelationships())) {
        atom.getRelationships().remove(atomRel);
        contentService.updateAtom(atom);

        contentService.removeRelationship(atomRel.getId(),
            AtomRelationshipJpa.class);
      }
    }
    if (atom2 != null && contentService.getAtom(atom2.getId()) != null) {
      for (AtomRelationship atomRel : new ArrayList<AtomRelationship>(
          atom2.getRelationships())) {
        atom2.getRelationships().remove(atomRel);
        contentService.updateAtom(atom2);

        contentService.removeRelationship(atomRel.getId(),
            AtomRelationshipJpa.class);

      }
    }
    // Once all demotions are gone, remove the atoms
    if (atom != null && contentService.getAtom(atom.getId()) != null) {
      contentService.removeAtom(atom.getId());
    }
    if (atom2 != null && contentService.getAtom(atom2.getId()) != null) {
      contentService.removeAtom(atom2.getId());
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
