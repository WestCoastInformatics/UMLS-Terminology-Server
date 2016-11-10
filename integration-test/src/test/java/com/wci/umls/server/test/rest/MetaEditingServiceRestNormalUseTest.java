/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
/*
 * 
 */
package com.wci.umls.server.test.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.wci.umls.server.Project;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.ComponentInfo;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.ProjectList;
import com.wci.umls.server.helpers.content.RelationshipList;
import com.wci.umls.server.jpa.ProjectJpa;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.AtomRelationshipJpa;
import com.wci.umls.server.jpa.content.AttributeJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.ConceptRelationshipJpa;
import com.wci.umls.server.jpa.content.SemanticTypeComponentJpa;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.model.actions.AtomicAction;
import com.wci.umls.server.model.actions.MolecularAction;
import com.wci.umls.server.model.actions.MolecularActionList;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.AtomRelationship;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.model.content.SemanticTypeComponent;
import com.wci.umls.server.model.workflow.WorkflowStatus;
import com.wci.umls.server.rest.client.IntegrationTestClientRest;

/**
 * Implementation of the "MetaEditing Service REST Normal Use" Test Cases.
 */
public class MetaEditingServiceRestNormalUseTest
    extends MetaEditingServiceRestTest {

  /** The auth token. */
  private static String authToken;

  /** The project. */
  private static Project project;

  /** The umls terminology. */
  private String umlsTerminology = "UMLS";

  /** The umls version. */
  private String umlsVersion = "latest";

  /**
   * The concept (will be copied from existing concept, to avoid affecting
   * database values.
   */
  private Concept concept;

  /** The concept 2. */
  private Concept concept2;

  /** The concept 3. */
  private Concept concept3;

  /** The concept 4. */
  private Concept concept4;

  /**
   * Create test fixtures per test.
   *
   * @throws Exception the exception
   */
  @Override
  @Before
  public void setup() throws Exception {

    // authentication (admin for editing permissions)
    authToken =
        securityService.authenticate(adminUser, adminPassword).getAuthToken();

    // ensure there is a concept associated with the project
    ProjectList projects = projectService.getProjects(authToken);
    assertTrue(projects.size() > 0);
    project = projects.getObjects().get(0);

    // Turn off the standard validation checks for the purposes of these tests
    final List<String> validationChecks = project.getValidationChecks();
    validationChecks.remove("DT_M1");
    validationChecks.remove("DT_I3B");
    validationChecks.remove("MGV_H1");
    validationChecks.remove("MGV_H2");
    project.setValidationChecks(validationChecks);

    projectService.updateProject((ProjectJpa) project, authToken);

    // reload the project after update
    projects = projectService.getProjects(authToken);
    assertTrue(projects.size() > 0);
    project = projects.getObjects().get(0);

    // verify terminology and branch are expected values
    assertTrue(project.getTerminology().equals(umlsTerminology));
    // assertTrue(project.getBranch().equals(Branch.ROOT));

    // Copy existing concept to avoid messing with actual database data.
    ConceptJpa conceptJpa = new ConceptJpa(contentService.getConcept("C0000294",
        umlsTerminology, umlsVersion, null, authToken), false);
    conceptJpa.setId(null);
    conceptJpa.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);
    conceptJpa = (ConceptJpa) testService.addConcept(conceptJpa, authToken);
    concept = contentService.getConcept(conceptJpa.getId(), project.getId(),
        authToken);

    ConceptJpa concept2Jpa =
        new ConceptJpa(contentService.getConcept("C0002073", umlsTerminology,
            umlsVersion, null, authToken), false);
    concept2Jpa.setId(null);
    concept2Jpa.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);
    concept2Jpa = (ConceptJpa) testService.addConcept(concept2Jpa, authToken);
    concept2 = contentService.getConcept(concept2Jpa.getId(), project.getId(),
        authToken);

    ConceptJpa concept3Jpa =
        new ConceptJpa(contentService.getConcept("C0065642", umlsTerminology,
            umlsVersion, null, authToken), false);
    concept3Jpa.setId(null);
    concept3Jpa.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);
    concept3Jpa = (ConceptJpa) testService.addConcept(concept3Jpa, authToken);
    concept3 = contentService.getConcept(concept3Jpa.getId(), project.getId(),
        authToken);

  }

  /**
   * Populate concepts.
   *
   * @throws Exception the exception
   */
  public void populateConcepts() throws Exception {
    Logger.getLogger(getClass()).debug("  Populate Concepts");
    //
    // Create and add atoms to concepts
    //
    AtomJpa atom = new AtomJpa();
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
    atom.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);

    AtomJpa atom2 = new AtomJpa();
    atom2.setBranch(Branch.ROOT);
    atom2.setName("IPA");
    atom2.setTerminologyId("TestId");
    atom2.setTerminology(umlsTerminology);
    atom2.setVersion(umlsVersion);
    atom2.setTimestamp(new Date());
    atom2.setPublishable(true);
    atom2.setCodeId("C67080");
    atom2.setConceptId("C67080");
    atom2.getConceptTerminologyIds().put(concept.getTerminology(),
        concept.getTerminologyId());
    atom2.setDescriptorId("");
    atom2.setLanguage("ENG");
    atom2.setTermType("AB");
    atom2.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);

    AtomJpa atom3 = new AtomJpa();
    atom3.setBranch(Branch.ROOT);
    atom3.setName("17 Oxosteroids");
    atom3.setTerminologyId("TestId");
    atom3.setTerminology(umlsTerminology);
    atom3.setVersion(umlsVersion);
    atom3.setTimestamp(new Date());
    atom3.setPublishable(true);
    atom3.setCodeId("D015068");
    atom3.setConceptId("M0023181");
    atom3.getConceptTerminologyIds().put(concept2.getTerminology(),
        concept2.getTerminologyId());
    atom3.setDescriptorId("D015068");
    atom3.setLanguage("ENG");
    atom3.setTermType("PM");
    atom3.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);

    AtomJpa atom4 = new AtomJpa();
    atom4.setBranch(Branch.ROOT);
    atom4.setName("PABA");
    atom4.setTerminologyId("TestId");
    atom4.setTerminology(umlsTerminology);
    atom4.setVersion(umlsVersion);
    atom4.setTimestamp(new Date());
    atom4.setPublishable(true);
    atom4.setCodeId("D010129");
    atom4.setConceptId("M0015714");
    atom4.getConceptTerminologyIds().put(concept2.getTerminology(),
        concept2.getTerminologyId());
    atom4.setDescriptorId("D010129");
    atom4.setLanguage("ENG");
    atom4.setTermType("EP");
    atom4.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);

    concept =
        contentService.getConcept(concept.getId(), project.getId(), authToken);
    concept2 =
        contentService.getConcept(concept2.getId(), project.getId(), authToken);

    // add the atoms to the concepts
    Logger.getLogger(getClass()).debug("  add atom = " + atom);
    ValidationResult v = metaEditingService.addAtom(project.getId(),
        concept.getId(), "activityId", concept.getLastModified().getTime(),
        atom, false, authToken);
    assertTrue(v.getErrors().isEmpty());
    concept =
        contentService.getConcept(concept.getId(), project.getId(), authToken);

    Logger.getLogger(getClass()).debug("  add atom = " + atom2);
    v = metaEditingService.addAtom(project.getId(), concept.getId(),
        "activityId", concept.getLastModified().getTime(), atom2, false,
        authToken);
    assertTrue(v.getErrors().isEmpty());
    concept =
        contentService.getConcept(concept.getId(), project.getId(), authToken);

    Logger.getLogger(getClass()).debug("  add atom = " + atom3);
    v = metaEditingService.addAtom(project.getId(), concept2.getId(),
        "activityId", concept2.getLastModified().getTime(), atom3, false,
        authToken);
    assertTrue(v.getErrors().isEmpty());
    concept2 =
        contentService.getConcept(concept2.getId(), project.getId(), authToken);

    Logger.getLogger(getClass()).debug("  add atom = " + atom4);
    v = metaEditingService.addAtom(project.getId(), concept2.getId(),
        "activityId", concept2.getLastModified().getTime(), atom4, false,
        authToken);
    assertTrue(v.getErrors().isEmpty());
    concept2 =
        contentService.getConcept(concept2.getId(), project.getId(), authToken);

    //
    // Create and add semantic types to the Concepts
    //
    /*
     * SemanticTypeComponentJpa semanticType = new SemanticTypeComponentJpa();
     * semanticType.setBranch(Branch.ROOT);
     * semanticType.setSemanticType("Lipid");
     * semanticType.setTerminologyId("TestId");
     * semanticType.setTerminology(umlsTerminology);
     * semanticType.setVersion(umlsVersion); semanticType.setTimestamp(new
     * Date()); semanticType.setPublishable(true);
     * 
     * SemanticTypeComponentJpa semanticType2 = new SemanticTypeComponentJpa();
     * semanticType2.setBranch(Branch.ROOT);
     * semanticType2.setSemanticType("Enzyme");
     * semanticType2.setTerminologyId("TestId");
     * semanticType2.setTerminology(umlsTerminology);
     * semanticType2.setVersion(umlsVersion); semanticType2.setTimestamp(new
     * Date()); semanticType2.setPublishable(true);
     * 
     * SemanticTypeComponentJpa semanticType3 = new SemanticTypeComponentJpa();
     * semanticType3.setBranch(Branch.ROOT);
     * semanticType3.setSemanticType("Steroid");
     * semanticType3.setTerminologyId("TestId");
     * semanticType3.setTerminology(umlsTerminology);
     * semanticType3.setVersion(umlsVersion); semanticType3.setTimestamp(new
     * Date()); semanticType3.setPublishable(true);
     */

    String semanticType = "Lipid";
    String semanticType2 = "Enzyme";
    String semanticType3 = "Steroid";

    // Add semantic types to the concepts
    // For semantic type 1, add to both concepts 1 and 2, to test when same
    // semantic type is present on both (used in merge)
    Logger.getLogger(getClass())
        .debug("  add sty = " + concept.getId() + ", " + semanticType);
    v = metaEditingService.addSemanticType(project.getId(), concept.getId(),
        "activityId", concept.getLastModified().getTime(), semanticType, false,
        authToken);
    assertTrue(v.getErrors().isEmpty());
    concept =
        contentService.getConcept(concept.getId(), project.getId(), authToken);

    Logger.getLogger(getClass())
        .debug("  add sty = " + concept2.getId() + ", " + semanticType);
    v = metaEditingService.addSemanticType(project.getId(), concept2.getId(),
        "activityId", concept2.getLastModified().getTime(), semanticType, false,
        authToken);
    assertTrue(v.getErrors().isEmpty());
    concept2 =
        contentService.getConcept(concept2.getId(), project.getId(), authToken);

    // Add other semantic types to Concept2 only
    Logger.getLogger(getClass())
        .debug("  add sty = " + concept2.getId() + ", " + semanticType2);
    v = metaEditingService.addSemanticType(project.getId(), concept2.getId(),
        "activityId", concept2.getLastModified().getTime(), semanticType2,
        false, authToken);
    assertTrue(v.getErrors().isEmpty());
    concept2 =
        contentService.getConcept(concept2.getId(), project.getId(), authToken);

    Logger.getLogger(getClass())
        .debug("  add sty = " + concept2.getId() + ", " + semanticType3);
    v = metaEditingService.addSemanticType(project.getId(), concept2.getId(),
        "activityId", concept2.getLastModified().getTime(), semanticType3,
        false, authToken);
    assertTrue(v.getErrors().isEmpty());
    concept2 =
        contentService.getConcept(concept2.getId(), project.getId(), authToken);

    //
    // Create and add relationships to the concepts
    //
    ConceptRelationshipJpa relationship = new ConceptRelationshipJpa();
    relationship.setBranch(Branch.ROOT);
    relationship.setRelationshipType("RN");
    relationship.setAdditionalRelationshipType("");
    relationship.setFrom(concept2);
    relationship.setTo(concept);
    relationship.setTerminologyId("TestId");
    relationship.setTerminology(umlsTerminology);
    relationship.setVersion(umlsVersion);
    relationship.setTimestamp(new Date());
    relationship.setPublishable(true);
    relationship.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);

    ConceptRelationshipJpa relationship2 = new ConceptRelationshipJpa();
    relationship2.setBranch(Branch.ROOT);
    relationship2.setRelationshipType("RB");
    relationship2.setAdditionalRelationshipType("");
    relationship2.setFrom(concept2);
    relationship2.setTo(concept3);
    relationship2.setTerminologyId("TestId");
    relationship2.setTerminology(umlsTerminology);
    relationship2.setVersion(umlsVersion);
    relationship2.setTimestamp(new Date());
    relationship2.setPublishable(true);
    relationship2.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);

    ConceptRelationshipJpa relationship3 = new ConceptRelationshipJpa();
    relationship3.setBranch(Branch.ROOT);
    relationship3.setRelationshipType("RN");
    relationship3.setAdditionalRelationshipType("");
    relationship3.setFrom(concept);
    relationship3.setTo(concept3);
    relationship3.setTerminologyId("TestId");
    relationship3.setTerminology(umlsTerminology);
    relationship3.setVersion(umlsVersion);
    relationship3.setTimestamp(new Date());
    relationship3.setPublishable(true);
    relationship3.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);

    Logger.getLogger(getClass()).debug("  add rel = " + relationship);
    v = metaEditingService.addRelationship(project.getId(), concept2.getId(),
        "activityId", concept2.getLastModified().getTime(), relationship, false,
        authToken);
    assertTrue(v.getErrors().isEmpty());
    concept2 =
        contentService.getConcept(concept2.getId(), project.getId(), authToken);
    concept =
        contentService.getConcept(concept.getId(), project.getId(), authToken);

    Logger.getLogger(getClass()).debug("  add rel = " + relationship2);
    v = metaEditingService.addRelationship(project.getId(), concept2.getId(),
        "activityId", concept2.getLastModified().getTime(), relationship2,
        false, authToken);
    assertTrue(v.getErrors().isEmpty());
    concept2 =
        contentService.getConcept(concept2.getId(), project.getId(), authToken);
    concept3 =
        contentService.getConcept(concept3.getId(), project.getId(), authToken);

    Logger.getLogger(getClass()).debug("  add rel = " + relationship3);
    v = metaEditingService.addRelationship(project.getId(), concept.getId(),
        "activityId", concept.getLastModified().getTime(), relationship3, false,
        authToken);
    concept =
        contentService.getConcept(concept.getId(), project.getId(), authToken);
    concept3 =
        contentService.getConcept(concept3.getId(), project.getId(), authToken);

  }

  /**
   * Test add and remove semanticType to concept.
   *
   * @throws Exception the exception
   */
  @Test
  public void testAddAndRemoveSemanticTypeToConcept() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

    Logger.getLogger(getClass())
        .info("TEST - Add and remove semantic type to/from " + "C0000294,"
            + umlsTerminology + ", " + umlsVersion + ", " + authToken);

    //
    // Prepare the test and check prerequisites
    //
    // Due to MySQL rounding to the second, we must also round our comparison
    // startDate.
    Date startDate = DateUtils.round(new Date(), Calendar.SECOND);

    // get the concept
    Concept c =
        contentService.getConcept(concept.getId(), project.getId(), authToken);
    assertNotNull(c);

    // check against project
    // assertTrue(c.getBranch().equals(project.getBranch()));

    // construct a semantic type not present on concept (here, Lipid)
    SemanticTypeComponentJpa semanticType = new SemanticTypeComponentJpa();
    /*
     * semanticType.setBranch(Branch.ROOT);
     * semanticType.setSemanticType("Lipid");
     * semanticType.setTerminologyId("TestId");
     * semanticType.setTerminology(umlsTerminology);
     * semanticType.setVersion(umlsVersion); semanticType.setTimestamp(new
     * Date()); semanticType.setPublishable(true);
     */
    String sty = "Lipid";

    //
    // Test addition
    //

    // add the semantic type to the concept
    ValidationResult v =
        metaEditingService.addSemanticType(project.getId(), c.getId(),
            "activityId", c.getLastModified().getTime(), sty, false, authToken);
    assertTrue(v.getErrors().isEmpty());

    // retrieve the concept and check semantic types
    c = contentService.getConcept(concept.getId(), project.getId(), authToken);

    semanticType = null;
    for (SemanticTypeComponent s : c.getSemanticTypes()) {
      if (s.getSemanticType().equals("Lipid")) {
        semanticType = (SemanticTypeComponentJpa) s;
      }
    }
    assertNotNull(semanticType);

    // verify the molecular action exists
    PfsParameterJpa pfs = new PfsParameterJpa();
    pfs.setSortField("lastModified");
    pfs.setAscending(false);
    MolecularActionList list = projectService.findMolecularActions(c.getId(),
        umlsTerminology, umlsVersion, null, pfs, authToken);
    assertTrue(list.size() > 0);
    MolecularAction ma = list.getObjects().get(0);
    assertNotNull(ma);
    assertEquals(c.getId(), ma.getComponentId());
    assertTrue(ma.getLastModified().compareTo(startDate) >= 0);
    assertNotNull(ma.getAtomicActions());

    // Verify that three atomic actions exists for creating Semantic Type,
    // adding to concept, and
    // updating Concept WorkflowStatus
    pfs.setSortField(null);

    List<AtomicAction> atomicActions = projectService
        .findAtomicActions(ma.getId(), null, pfs, authToken).getObjects();
    Collections.sort(atomicActions,
        (a1, a2) -> a1.getId().compareTo(a2.getId()));
    assertEquals(3, atomicActions.size());
    assertEquals("SEMANTIC_TYPE", atomicActions.get(0).getIdType().toString());
    assertNull(atomicActions.get(0).getOldValue());
    assertNotNull(atomicActions.get(0).getNewValue());
    assertEquals("CONCEPT", atomicActions.get(1).getIdType().toString());
    assertNotNull(atomicActions.get(1).getOldValue());
    assertNotNull(atomicActions.get(1).getNewValue());
    assertEquals("workflowStatus", atomicActions.get(1).getField());
    assertEquals("CONCEPT", atomicActions.get(2).getIdType().toString());
    assertNull(atomicActions.get(2).getOldValue());
    assertNotNull(atomicActions.get(2).getNewValue());
    assertEquals("semanticTypes", atomicActions.get(2).getField());

    // Verify the log entry exists
    String logEntry =
        projectService.getLog(project.getId(), c.getId(), null, 1, authToken);
    assertTrue(logEntry.contains("ADD_SEMANTIC_TYPE to concept " + c.getId()));

    //
    // Add second semantic type
    //

    // Due to MySQL rounding to the second, we must also round our comparison
    // startDate.
    startDate = DateUtils.round(new Date(), Calendar.SECOND);

    // construct a second semantic type not present on concept (here, Enzyme)
    SemanticTypeComponentJpa semanticType2 = new SemanticTypeComponentJpa();
    /*
     * semanticType2.setBranch(Branch.ROOT);
     * semanticType2.setSemanticType("Enzyme");
     * semanticType2.setTerminologyId("TestId");
     * semanticType2.setTerminology(umlsTerminology);
     * semanticType2.setVersion(umlsVersion); semanticType2.setTimestamp(new
     * Date()); semanticType2.setPublishable(true);
     */
    String sty2 = "Enzyme";

    // add the second semantic type to the concept
    v = metaEditingService.addSemanticType(project.getId(), c.getId(),
        "activityId", c.getLastModified().getTime(), sty2, false, authToken);
    assertTrue(v.getErrors().isEmpty());

    // retrieve the concept and check semantic types
    c = contentService.getConcept(concept.getId(), project.getId(), authToken);

    semanticType = null;
    semanticType2 = null;
    for (SemanticTypeComponent s : c.getSemanticTypes()) {
      if (s.getSemanticType().equals("Lipid")) {
        semanticType = (SemanticTypeComponentJpa) s;
      }
      if (s.getSemanticType().equals("Enzyme")) {
        semanticType2 = (SemanticTypeComponentJpa) s;
      }
    }
    assertNotNull(semanticType);
    assertNotNull(semanticType2);

    // verify the molecular action exists
    pfs = new PfsParameterJpa();
    pfs.setSortField("lastModified");
    pfs.setAscending(false);
    list = projectService.findMolecularActions(c.getId(), umlsTerminology,
        umlsVersion, null, pfs, authToken);
    assertTrue(list.size() > 0);
    ma = list.getObjects().get(0);
    assertNotNull(ma);
    assertEquals(c.getId(), ma.getComponentId());

    assertTrue(ma.getLastModified().compareTo(startDate) >= 0);
    assertNotNull(ma.getAtomicActions());

    // Verify that TWO atomic actions exists for add Semantic Type (Concept
    // Workflow Status was already set during previous addition)
    pfs.setSortField(null);

    atomicActions = projectService
        .findAtomicActions(ma.getId(), null, pfs, authToken).getObjects();
    Collections.sort(atomicActions,
        (a1, a2) -> a1.getId().compareTo(a2.getId()));
    assertEquals(2, atomicActions.size());
    assertEquals("SEMANTIC_TYPE", atomicActions.get(0).getIdType().toString());
    assertNull(atomicActions.get(0).getOldValue());
    assertNotNull(atomicActions.get(0).getNewValue());
    assertEquals("CONCEPT", atomicActions.get(1).getIdType().toString());
    assertNull(atomicActions.get(1).getOldValue());
    assertNotNull(atomicActions.get(1).getNewValue());
    assertEquals("semanticTypes", atomicActions.get(1).getField());

    // Verify the log entry exists
    logEntry =
        projectService.getLog(project.getId(), c.getId(), null, 1, authToken);
    assertTrue(logEntry.contains("ADD_SEMANTIC_TYPE to concept " + c.getId()));

    //
    // Test removal
    //

    // remove the first semantic type from the concept
    v = metaEditingService.removeSemanticType(project.getId(), c.getId(),
        "activityId", c.getLastModified().getTime(), semanticType.getId(),
        false, authToken);
    assertTrue(v.getErrors().isEmpty());

    // retrieve the concept and check semantic types
    c = contentService.getConcept(concept.getId(), project.getId(), authToken);

    boolean semanticTypePresent = false;
    for (SemanticTypeComponent s : c.getSemanticTypes()) {
      if (s.getSemanticType().equals("Lipid")) {
        semanticTypePresent = true;
      }
    }
    assertTrue(!semanticTypePresent);

    // verify the molecular action exists
    pfs = new PfsParameterJpa();
    pfs.setSortField("lastModified");
    pfs.setAscending(false);
    list = projectService.findMolecularActions(c.getId(), umlsTerminology,
        umlsVersion, null, pfs, authToken);
    assertTrue(list.size() > 0);
    ma = list.getObjects().get(0);
    assertNotNull(ma);
    assertEquals(c.getId(), ma.getComponentId());
    assertTrue(ma.getLastModified().compareTo(startDate) >= 0);
    assertNotNull(ma.getAtomicActions());

    // Verify that two atomic actions exists for remove Semantic Type
    pfs.setSortField(null);

    atomicActions = projectService
        .findAtomicActions(ma.getId(), null, null, authToken).getObjects();
    Collections.sort(atomicActions,
        (a1, a2) -> a1.getId().compareTo(a2.getId()));
    assertEquals(2, atomicActions.size());
    assertEquals("CONCEPT", atomicActions.get(0).getIdType().toString());
    assertNotNull(atomicActions.get(0).getOldValue());
    assertNull(atomicActions.get(0).getNewValue());
    assertEquals("semanticTypes", atomicActions.get(0).getField());
    assertEquals("SEMANTIC_TYPE", atomicActions.get(1).getIdType().toString());
    assertNotNull(atomicActions.get(1).getOldValue());
    assertNull(atomicActions.get(1).getNewValue());

    // Verify the log entry exists
    logEntry =
        projectService.getLog(project.getId(), c.getId(), null, 1, authToken);
    assertTrue(
        logEntry.contains("REMOVE_SEMANTIC_TYPE from concept " + c.getId()));

    // remove the second semantic type from the concept (assume verification of
    // MA, atomic actions, and log entry since we just tested those)
    v = metaEditingService.removeSemanticType(project.getId(), c.getId(),
        "activityId", c.getLastModified().getTime(), semanticType2.getId(),
        false, authToken);
    assertTrue(v.getErrors().isEmpty());

    // retrieve the concept and check attributes
    c = contentService.getConcept(concept.getId(), project.getId(), authToken);

    boolean semanticType2Present = false;
    for (SemanticTypeComponent s : c.getSemanticTypes()) {
      if (s.getSemanticType().equals("Enzyme")) {
        semanticType2Present = true;
      }
    }
    assertTrue(!semanticType2Present);

  }

  /**
   * Test add and remove attribute to concept.
   *
   * @throws Exception the exception
   */
  @Test
  public void testAddAndRemoveAttributeToConcept() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

    Logger.getLogger(getClass())
        .info("TEST - Add and remove attribute to/from " + "C0000294,"
            + umlsTerminology + ", " + umlsVersion + ", " + authToken);

    //
    // Prepare the test and check prerequisites
    //
    // Due to MySQL rounding to the second, we must also round our comparison
    // startDate.
    Date startDate = DateUtils.round(new Date(), Calendar.SECOND);

    // get the concept
    Concept c =
        contentService.getConcept(concept.getId(), project.getId(), authToken);
    assertNotNull(c);

    // construct a attribute not present on concept (here, UMLSRELA)
    AttributeJpa attribute = new AttributeJpa();
    attribute.setBranch(Branch.ROOT);
    attribute.setName("UMLSRELA");
    attribute.setValue("VALUE");
    attribute.setTerminologyId("TestId");
    attribute.setTerminology(umlsTerminology);
    attribute.setVersion(umlsVersion);
    attribute.setTimestamp(new Date());
    attribute.setPublishable(true);

    //
    // Test addition
    //

    // add the attribute to the concept
    ValidationResult v = metaEditingService.addAttribute(project.getId(),
        c.getId(), "activityId", c.getLastModified().getTime(), attribute,
        false, authToken);
    assertTrue(v.getErrors().isEmpty());

    // retrieve the concept and check attributes
    c = contentService.getConcept(concept.getId(), project.getId(), authToken);

    attribute = null;
    for (Attribute s : c.getAttributes()) {
      if (s.getName().equals("UMLSRELA")) {
        attribute = (AttributeJpa) s;
      }
    }
    assertNotNull(attribute);

    // verify the molecular action exists
    PfsParameterJpa pfs = new PfsParameterJpa();
    pfs.setSortField("lastModified");
    pfs.setAscending(false);
    MolecularActionList list = projectService.findMolecularActions(c.getId(),
        umlsTerminology, umlsVersion, null, pfs, authToken);
    assertTrue(list.size() > 0);
    MolecularAction ma = list.getObjects().get(0);
    assertNotNull(ma);
    assertEquals(c.getId(), ma.getComponentId());
    assertTrue(ma.getLastModified().compareTo(startDate) >= 0);
    assertNotNull(ma.getAtomicActions());

    // Verify that three atomic actions exists for add attribute, and update
    // Concept WorkflowStatus
    pfs.setSortField(null);

    List<AtomicAction> atomicActions = projectService
        .findAtomicActions(ma.getId(), null, pfs, authToken).getObjects();
    Collections.sort(atomicActions,
        (a1, a2) -> a1.getId().compareTo(a2.getId()));
    assertEquals(3, atomicActions.size());
    assertEquals("ATTRIBUTE", atomicActions.get(0).getIdType().toString());
    assertNull(atomicActions.get(0).getOldValue());
    assertNotNull(atomicActions.get(0).getNewValue());
    assertEquals("CONCEPT", atomicActions.get(1).getIdType().toString());
    assertNotNull(atomicActions.get(1).getOldValue());
    assertNotNull(atomicActions.get(1).getNewValue());
    assertEquals("workflowStatus", atomicActions.get(1).getField());
    assertEquals("CONCEPT", atomicActions.get(2).getIdType().toString());
    assertNull(atomicActions.get(2).getOldValue());
    assertNotNull(atomicActions.get(2).getNewValue());
    assertEquals("attributes", atomicActions.get(2).getField());

    // Verify the log entry exists
    String logEntry =
        projectService.getLog(project.getId(), c.getId(), null, 1, authToken);
    assertTrue(logEntry.contains("ADD_ATTRIBUTE to concept " + c.getId()));

    //
    // Add second attribute (also ensures alternateTerminologyId increments
    // correctly)
    //

    // Due to MySQL rounding to the second, we must also round our comparison
    // startDate.
    startDate = DateUtils.round(new Date(), Calendar.SECOND);

    // construct a second attribute not present on concept (here, UMLSRELA with
    // Value of VALUE2)
    AttributeJpa attribute2 = new AttributeJpa();
    attribute2.setBranch(Branch.ROOT);
    attribute2.setName("UMLSRELA");
    attribute2.setValue("VALUE2");
    attribute2.setTerminologyId("TestId");
    attribute2.setTerminology(umlsTerminology);
    attribute2.setVersion(umlsVersion);
    attribute2.setTimestamp(new Date());
    attribute2.setPublishable(true);

    //
    // add the second attribute to the concept
    //

    // add the attribute to the concept
    v = metaEditingService.addAttribute(project.getId(), c.getId(),
        "activityId", c.getLastModified().getTime(), attribute2, false,
        authToken);
    assertTrue(v.getErrors().isEmpty());

    // retrieve the concept and check to make sure both attributes are still
    // there
    c = contentService.getConcept(concept.getId(), project.getId(), authToken);

    attribute = null;
    attribute2 = null;
    for (Attribute s : c.getAttributes()) {
      if (s.getName().equals("UMLSRELA") && s.getValue().equals("VALUE")) {
        attribute = (AttributeJpa) s;
      }
      if (s.getName().equals("UMLSRELA") && s.getValue().equals("VALUE2")) {
        attribute2 = (AttributeJpa) s;
      }
    }
    assertNotNull(attribute);
    assertNotNull(attribute2);

    // verify the molecular action exists
    pfs = new PfsParameterJpa();
    pfs.setSortField("lastModified");
    pfs.setAscending(false);
    list = projectService.findMolecularActions(c.getId(), umlsTerminology,
        umlsVersion, null, pfs, authToken);
    assertTrue(list.size() > 0);
    ma = list.getObjects().get(0);
    assertNotNull(ma);
    assertEquals(c.getId(), ma.getComponentId());
    assertTrue(ma.getLastModified().compareTo(startDate) >= 0);
    assertNotNull(ma.getAtomicActions());

    // Verify that two atomic actions exists for add attribute (Concept Workflow
    // Status was already set during previous addition)
    pfs.setSortField(null);

    atomicActions = projectService
        .findAtomicActions(ma.getId(), null, pfs, authToken).getObjects();
    Collections.sort(atomicActions,
        (a1, a2) -> a1.getId().compareTo(a2.getId()));
    assertEquals(2, atomicActions.size());
    assertEquals("ATTRIBUTE", atomicActions.get(0).getIdType().toString());
    assertNull(atomicActions.get(0).getOldValue());
    assertNotNull(atomicActions.get(0).getNewValue());
    assertEquals("CONCEPT", atomicActions.get(1).getIdType().toString());
    assertNull(atomicActions.get(1).getOldValue());
    assertNotNull(atomicActions.get(1).getNewValue());
    assertEquals("attributes", atomicActions.get(1).getField());

    // Verify the log entry exists
    logEntry =
        projectService.getLog(project.getId(), c.getId(), null, 1, authToken);
    assertTrue(logEntry.contains("ADD_ATTRIBUTE to concept " + c.getId()));

    //
    // Test removal
    //

    // remove the first attribute from the concept
    v = metaEditingService.removeAttribute(project.getId(), c.getId(),
        "activityId", c.getLastModified().getTime(), attribute.getId(), false,
        authToken);
    assertTrue(v.getErrors().isEmpty());

    c = contentService.getConcept(concept.getId(), project.getId(), authToken);

    boolean attributePresent = false;
    for (Attribute a : c.getAttributes()) {
      if (a.getName().equals("UMLSRELA") && a.getValue().equals("VALUE")) {
        attributePresent = true;
      }
    }
    assertTrue(!attributePresent);

    // verify the molecular action exists
    pfs = new PfsParameterJpa();
    pfs.setSortField("lastModified");
    pfs.setAscending(false);
    list = projectService.findMolecularActions(c.getId(), umlsTerminology,
        umlsVersion, null, pfs, authToken);
    assertTrue(list.size() > 0);
    ma = list.getObjects().get(0);
    assertNotNull(ma);
    assertEquals(c.getId(), ma.getComponentId());
    assertTrue(ma.getLastModified().compareTo(startDate) >= 0);
    assertNotNull(ma.getAtomicActions());

    // Verify that one atomic action exists for remove Attribute
    pfs.setSortField(null);

    atomicActions = projectService
        .findAtomicActions(ma.getId(), null, null, authToken).getObjects();
    Collections.sort(atomicActions,
        (a1, a2) -> a1.getId().compareTo(a2.getId()));
    assertEquals(2, atomicActions.size());
    assertEquals("CONCEPT", atomicActions.get(0).getIdType().toString());
    assertNotNull(atomicActions.get(0).getOldValue());
    assertNull(atomicActions.get(0).getNewValue());
    assertEquals("attributes", atomicActions.get(0).getField());
    assertEquals(atomicActions.get(1).getIdType().toString(), "ATTRIBUTE");
    assertNotNull(atomicActions.get(1).getOldValue());
    assertNull(atomicActions.get(1).getNewValue());

    // Verify the log entry exists
    logEntry =
        projectService.getLog(project.getId(), c.getId(), null, 1, authToken);
    assertTrue(logEntry.contains("REMOVE_ATTRIBUTE from concept " + c.getId()));

    // remove the second attribute from the concept (assume verification of MA,
    // atomic actions, and log entry since we just tested those)
    v = metaEditingService.removeAttribute(project.getId(), c.getId(),
        "activityId", c.getLastModified().getTime(), attribute2.getId(), false,
        authToken);
    assertTrue(v.getErrors().isEmpty());

    // retrieve the concept and check attributes
    c = contentService.getConcept(concept.getId(), project.getId(), authToken);

    boolean attribute2Present = false;
    for (Attribute a : c.getAttributes()) {
      if (a.getName().equals("UMLSRELA") && a.getValue().equals("VALUE2")) {
        attribute2Present = true;
      }
    }
    assertTrue(!attribute2Present);

  }

  /**
   * Test add and remove atom to concept.
   *
   * @throws Exception the exception
   */
  @Test
  public void testAddAndRemoveAtomToConcept() throws Exception {
    Logger.getLogger(getClass()).debug("Start test " + name.getMethodName());

    Logger.getLogger(getClass())
        .info("TEST - Add and remove atom to/from " + "C0000294,"
            + umlsTerminology + ", " + umlsVersion + ", " + authToken);

    //
    // Prepare the test and check prerequisites
    //
    // Due to MySQL rounding to the second, we must also round our comparison
    // startDate.
    Date startDate = DateUtils.round(new Date(), Calendar.SECOND);

    // get the concept
    Concept c =
        contentService.getConcept(concept.getId(), project.getId(), authToken);
    assertNotNull(c);

    // construct an atom not present on concept (here, DCB)
    AtomJpa atom = new AtomJpa();
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
    atom.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);

    //
    // Test addition
    //

    // add the atom to the concept
    ValidationResult v = metaEditingService.addAtom(project.getId(), c.getId(),
        "activityId", c.getLastModified().getTime(), atom, false, authToken);
    assertTrue(v.getErrors().isEmpty());

    // retrieve the concept and check attributes
    c = contentService.getConcept(concept.getId(), project.getId(), authToken);

    atom = null;
    for (Atom a : c.getAtoms()) {
      if (a.getName().equals("DCB")) {
        atom = (AtomJpa) a;
      }
    }
    assertNotNull(atom);

    // verify that alternate ID was created and is correctly formed.
    assertNotNull(atom.getAlternateTerminologyIds().get(umlsTerminology));
    assertTrue(
        atom.getAlternateTerminologyIds().get(umlsTerminology).startsWith("A"));

    // verify the molecular action exists
    PfsParameterJpa pfs = new PfsParameterJpa();
    pfs.setSortField("lastModified");
    pfs.setAscending(false);
    MolecularActionList list = projectService.findMolecularActions(c.getId(),
        umlsTerminology, umlsVersion, null, pfs, authToken);
    assertTrue(list.size() > 0);
    MolecularAction ma = list.getObjects().get(0);
    assertNotNull(ma);
    assertEquals(c.getId(), ma.getComponentId());
    assertTrue(ma.getLastModified().compareTo(startDate) >= 0);
    assertNotNull(ma.getAtomicActions());

    // Verify that five atomic actions exists for add atom, and update
    // Concept WorkflowStatus
    pfs.setSortField(null);

    List<AtomicAction> atomicActions = projectService
        .findAtomicActions(ma.getId(), null, pfs, authToken).getObjects();
    Collections.sort(atomicActions,
        (a1, a2) -> a1.getId().compareTo(a2.getId()));
    assertEquals(5, atomicActions.size());
    assertEquals("ATOM", atomicActions.get(0).getIdType().toString());
    assertNull(atomicActions.get(0).getOldValue());
    assertNotNull(atomicActions.get(0).getNewValue());
    assertEquals("CODE", atomicActions.get(1).getIdType().toString());
    assertNull(atomicActions.get(1).getOldValue());
    assertNotNull(atomicActions.get(1).getNewValue());
    assertEquals("CONCEPT", atomicActions.get(2).getIdType().toString());
    assertNull(atomicActions.get(2).getOldValue());
    assertNotNull(atomicActions.get(2).getNewValue());
    assertEquals("atoms", atomicActions.get(2).getField());
    assertEquals("CONCEPT", atomicActions.get(3).getIdType().toString());
    assertNotNull(atomicActions.get(3).getOldValue());
    assertNotNull(atomicActions.get(3).getNewValue());
    assertEquals("workflowStatus", atomicActions.get(3).getField());
    assertEquals("CONCEPT", atomicActions.get(4).getIdType().toString());
    assertNull(atomicActions.get(4).getOldValue());
    assertNotNull(atomicActions.get(4).getNewValue());
    assertEquals("atoms", atomicActions.get(4).getField());

    // Verify the log entry exists
    String logEntry =
        projectService.getLog(project.getId(), c.getId(), null, 1, authToken);
    assertTrue(logEntry.contains("ADD_ATOM to concept " + c.getId()));

    //
    // Add second atom (also ensures alternateTerminologyId increments
    // correctly)
    //

    // Due to MySQL rounding to the second, we must also round our comparison
    // startDate.
    startDate = DateUtils.round(new Date(), Calendar.SECOND);

    // construct a second atom not present on concept (here, 17
    // Oxosteroids)
    AtomJpa atom2 = new AtomJpa();
    atom2.setBranch(Branch.ROOT);
    atom2.setName("17 Oxosteroids");
    atom2.setTerminologyId("TestId");
    atom2.setTerminology(umlsTerminology);
    atom2.setVersion(umlsVersion);
    atom2.setTimestamp(new Date());
    atom2.setPublishable(true);
    atom2.setCodeId("D015068");
    atom2.setConceptId("M0023181");
    atom.getConceptTerminologyIds().put(concept.getTerminology(),
        concept.getTerminologyId());
    atom2.setDescriptorId("D015068");
    atom2.setLanguage("ENG");
    atom2.setTermType("PM");
    atom2.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);

    //
    // add the second atom to the concept
    //

    // add the attribute to the concept
    v = metaEditingService.addAtom(project.getId(), c.getId(), "activityId",
        c.getLastModified().getTime(), atom2, false, authToken);
    assertTrue(v.getErrors().isEmpty());

    // retrieve the concept and check to make sure both atoms are still
    // there
    c = contentService.getConcept(concept.getId(), project.getId(), authToken);

    atom = null;
    atom2 = null;
    for (Atom a : c.getAtoms()) {
      if (a.getName().equals("DCB")) {
        atom = (AtomJpa) a;
      }
      if (a.getName().equals("17 Oxosteroids")) {
        atom2 = (AtomJpa) a;
      }
    }
    assertNotNull(atom);
    assertNotNull(atom2);

    // verify that alternate ID was created and is correctly formed.
    assertNotNull(atom2.getAlternateTerminologyIds().get(umlsTerminology));
    assertTrue(atom2.getAlternateTerminologyIds().get(umlsTerminology)
        .startsWith("A"));

    // verify that atom2's alternate ID is different from the first one
    assertNotSame(atom.getAlternateTerminologyIds().get(umlsTerminology),
        atom2.getAlternateTerminologyIds().get(umlsTerminology));

    // verify the molecular action exists
    pfs = new PfsParameterJpa();
    pfs.setSortField("lastModified");
    pfs.setAscending(false);
    list = projectService.findMolecularActions(c.getId(), umlsTerminology,
        umlsVersion, null, pfs, authToken);
    assertTrue(list.size() > 0);
    ma = list.getObjects().get(0);
    assertNotNull(ma);
    assertEquals(c.getId(), ma.getComponentId());
    assertTrue(ma.getLastModified().compareTo(startDate) >= 0);
    assertNotNull(ma.getAtomicActions());

    // Verify that five atomic actions exists for add atom
    pfs.setSortField(null);

    atomicActions = projectService
        .findAtomicActions(ma.getId(), null, pfs, authToken).getObjects();
    Collections.sort(atomicActions,
        (a1, a2) -> a1.getId().compareTo(a2.getId()));
    assertEquals(5, atomicActions.size());
    // TODO - fix or drop
    // assertEquals("ATOM", atomicActions.get(0).getIdType().toString());
    // assertNull(atomicActions.get(0).getOldValue());
    // assertNotNull(atomicActions.get(0).getNewValue());
    // assertEquals("CONCEPT", atomicActions.get(1).getIdType().toString());
    // assertNull(atomicActions.get(1).getOldValue());
    // assertNotNull(atomicActions.get(1).getNewValue());
    // assertEquals("atoms", atomicActions.get(1).getField());

    // Verify the log entry exists
    logEntry =
        projectService.getLog(project.getId(), c.getId(), null, 1, authToken);
    assertTrue(logEntry.contains("ADD_ATOM to concept " + c.getId()));

    //
    // Test removal
    //

    // remove the first atom from the concept
    v = metaEditingService.removeAtom(project.getId(), c.getId(), "activityId",
        c.getLastModified().getTime(), atom.getId(), false, authToken);
    assertTrue(v.getErrors().isEmpty());

    c = contentService.getConcept(concept.getId(), project.getId(), authToken);

    boolean attributePresent = false;
    for (Atom a : c.getAtoms()) {
      if (a.getName().equals("DCB")) {
        attributePresent = true;
      }
    }
    assertTrue(!attributePresent);

    // verify the molecular action exists
    pfs = new PfsParameterJpa();
    pfs.setSortField("lastModified");
    pfs.setAscending(false);
    list = projectService.findMolecularActions(c.getId(), umlsTerminology,
        umlsVersion, null, pfs, authToken);
    assertTrue(list.size() > 0);
    ma = list.getObjects().get(0);
    assertNotNull(ma);
    assertEquals(c.getId(), ma.getComponentId());
    assertTrue(ma.getLastModified().compareTo(startDate) >= 0);
    assertNotNull(ma.getAtomicActions());

    // Verify that one atomic action exists for remove Atom
    pfs.setSortField(null);

    atomicActions = projectService
        .findAtomicActions(ma.getId(), null, null, authToken).getObjects();
    Collections.sort(atomicActions,
        (a1, a2) -> a1.getId().compareTo(a2.getId()));
    assertEquals(4, atomicActions.size());
    // TODO -Fix or drop
    // assertEquals("CONCEPT", atomicActions.get(0).getIdType().toString());
    // assertNotNull(atomicActions.get(0).getOldValue());
    // assertNull(atomicActions.get(0).getNewValue());
    // assertEquals("atoms", atomicActions.get(0).getField());
    // assertEquals(atomicActions.get(1).getIdType().toString(), "ATOM");
    // assertNotNull(atomicActions.get(1).getOldValue());
    // assertNull(atomicActions.get(1).getNewValue());

    // Verify the log entry exists
    logEntry =
        projectService.getLog(project.getId(), c.getId(), null, 1, authToken);
    assertTrue(logEntry.contains("REMOVE_ATOM from concept " + c.getId()));

    // remove the second atom from the concept (assume verification of
    // MA, atomic actions, and log entry since we just tested those)
    v = metaEditingService.removeAtom(project.getId(), c.getId(), "activityId",
        c.getLastModified().getTime(), atom2.getId(), false, authToken);
    assertTrue(v.getErrors().isEmpty());

    // retrieve the concept and check atoms
    c = contentService.getConcept(concept.getId(), project.getId(), authToken);

    boolean atom2Present = false;
    for (Atom a : c.getAtoms()) {
      if (a.getName().equals("17 Oxosteroids")) {
        atom2Present = true;
      }
    }
    assertTrue(!atom2Present);

  }

  /**
   * Test update atom.
   *
   * @throws Exception the exception
   */
  @Test
  public void testUpdateAtom() throws Exception {
    Logger.getLogger(getClass()).debug("Start test " + name.getMethodName());

    Logger.getLogger(getClass()).info("TEST - Update atom on " + "C0000294,"
        + umlsTerminology + ", " + umlsVersion + ", " + authToken);

    //
    // Prepare the test and check prerequisites
    //
    // Due to MySQL rounding to the second, we must also round our comparison
    // startDate.
    Date startDate = DateUtils.round(new Date(), Calendar.SECOND);

    // get the concept
    Concept c =
        contentService.getConcept(concept.getId(), project.getId(), authToken);
    assertNotNull(c);

    // construct an atom not present on concept (here, DCB)
    AtomJpa atom = new AtomJpa();
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
    atom.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);

    // add the atom to the concept
    ValidationResult v = metaEditingService.addAtom(project.getId(), c.getId(),
        "activityId", c.getLastModified().getTime(), atom, false, authToken);
    assertTrue(v.getErrors().isEmpty());

    // retrieve the concept
    c = contentService.getConcept(concept.getId(), project.getId(), authToken);

    // Save a copy of the added atom
    AtomJpa addedAtom = null;
    for (Atom a : c.getAtoms()) {
      if (a.getName().equals("DCB")) {
        addedAtom = (AtomJpa) a;
      }
    }
    assertNotNull(addedAtom);

    //
    // Test updating an identity field on the Atom (this should fail)
    //
    addedAtom.setCodeId("C99999");

    boolean updateFailed = false;
    try {
      v = metaEditingService.updateAtom(project.getId(), c.getId(),
          "activityId", c.getLastModified().getTime(), addedAtom, false,
          authToken);
    } catch (Exception e) {
      updateFailed = true;
    }
    assertTrue(updateFailed);
    c = contentService.getConcept(concept.getId(), project.getId(), authToken);

    // Pull the atom from the concept, and make sure the codeId didn't get
    // modified during the failed update attempt
    addedAtom = null;
    for (Atom a : c.getAtoms()) {
      if (a.getName().equals("DCB")) {
        addedAtom = (AtomJpa) a;
      }
    }
    assertNotNull(addedAtom);
    assertTrue(addedAtom.getCodeId().equals("C44314"));

    //
    // Test updating an allowable field on the Atom (this should succeed)
    //
    addedAtom.setPublishable(false);

    boolean updateSucceded = true;
    try {
      v = metaEditingService.updateAtom(project.getId(), c.getId(),
          "activityId", c.getLastModified().getTime(), addedAtom, false,
          authToken);
    } catch (Exception e) {
      updateSucceded = false;
    }
    assertTrue(updateSucceded);
    c = contentService.getConcept(concept.getId(), project.getId(), authToken);

    // Pull the atom from the concept, and make sure the language
    // updated successfully
    addedAtom = null;
    for (Atom a : c.getAtoms()) {
      if (a.getName().equals("DCB")) {
        addedAtom = (AtomJpa) a;
      }
    }
    assertNotNull(addedAtom);
    assertFalse(addedAtom.isPublishable());

    // verify the molecular action exists
    PfsParameterJpa pfs = new PfsParameterJpa();
    pfs.setSortField("lastModified");
    pfs.setAscending(false);
    MolecularActionList list = projectService.findMolecularActions(c.getId(),
        umlsTerminology, umlsVersion, null, pfs, authToken);
    assertTrue(list.size() > 0);
    MolecularAction ma = list.getObjects().get(0);
    assertNotNull(ma);
    assertEquals(c.getId(), ma.getComponentId());
    assertTrue(ma.getLastModified().compareTo(startDate) >= 0);
    assertNotNull(ma.getAtomicActions());

    // Verify that one atomic actions exists for updating atom
    pfs.setSortField(null);

    List<AtomicAction> atomicActions = projectService
        .findAtomicActions(ma.getId(), null, pfs, authToken).getObjects();
    Collections.sort(atomicActions,
        (a1, a2) -> a1.getId().compareTo(a2.getId()));
    assertEquals(1, atomicActions.size());
    assertEquals("ATOM", atomicActions.get(0).getIdType().toString());
    assertNotNull(atomicActions.get(0).getOldValue());
    assertNotNull(atomicActions.get(0).getNewValue());

    // Verify the log entry exists
    String logEntry =
        projectService.getLog(project.getId(), c.getId(), null, 1, authToken);
    assertTrue(logEntry.contains("UPDATE_ATOM "));

  }

  /**
   * Test add and remove relationship to concept.
   *
   * @throws Exception the exception
   */
  @Test
  public void testAddAndRemoveRelationshipToConcept() throws Exception {
    Logger.getLogger(getClass()).debug("Start test " + name.getMethodName());

    Logger.getLogger(getClass())
        .info("TEST - Add and remove relationship to/from " + "C0000294,"
            + umlsTerminology + ", " + umlsVersion + ", " + authToken);

    //
    // Prepare the test and check prerequisites
    //
    // Due to MySQL rounding to the second, we must also round our comparison
    // startDate.
    Date startDate = DateUtils.round(new Date(), Calendar.SECOND);

    // get the concept
    Concept c =
        contentService.getConcept(concept.getId(), project.getId(), authToken);
    assertNotNull(c);

    Concept c2 =
        contentService.getConcept(concept2.getId(), project.getId(), authToken);
    assertNotNull(c2);

    Concept c3 =
        contentService.getConcept(concept3.getId(), project.getId(), authToken);
    assertNotNull(c3);

    // construct a relationship not present on concept (here, RelationshipType
    // RN to Concept C0002073, ConceptId 7335 (created in setup)
    ConceptRelationshipJpa relationship = new ConceptRelationshipJpa();
    relationship.setBranch(Branch.ROOT);
    relationship.setRelationshipType("RN");
    relationship.setAdditionalRelationshipType("");
    relationship.setFrom(c);
    relationship.setTo(c2);
    relationship.setTerminologyId("TestId");
    relationship.setTerminology(umlsTerminology);
    relationship.setVersion(umlsVersion);
    relationship.setTimestamp(new Date());
    relationship.setPublishable(true);
    relationship.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);

    //
    // Test addition
    //

    // add the relationship to the concept
    ValidationResult v = metaEditingService.addRelationship(project.getId(),
        c.getId(), "activityId", c.getLastModified().getTime(), relationship,
        false, authToken);
    assertTrue(v.getErrors().isEmpty());

    // retrieve the source concept and check relationships
    c = contentService.getConcept(concept.getId(), project.getId(), authToken);

    RelationshipList relList =
        contentService.findConceptRelationships(c.getTerminologyId(),
            c.getTerminology(), c.getVersion(), null, null, authToken);

    relationship = null;
    for (final Relationship<?, ?> rel : relList.getObjects()) {
      if (rel.getRelationshipType().equals("RN")
          && rel.getTo().getId().equals(c2.getId())) {
        relationship = (ConceptRelationshipJpa) rel;
      }
    }
    assertNotNull(relationship);

    // retrieve the to concept and check relationships for the inverse
    c2 = contentService.getConcept(concept2.getId(), project.getId(),
        authToken);

    relList = contentService.findConceptRelationships(c2.getTerminologyId(),
        c2.getTerminology(), c2.getVersion(), null, null, authToken);

    ConceptRelationshipJpa relationship2 = null;
    for (final Relationship<?, ?> rel : relList.getObjects()) {
      if (rel.getFrom().getId().equals(c2.getId())
          && rel.getTo().getId().equals(c.getId())) {
        relationship2 = (ConceptRelationshipJpa) rel;
      }
    }
    assertNotNull(relationship2);

    // verify that alternate ID was created and is correctly formed - RUI
    // assignment was moved to release time.
    // assertNotNull(
    // relationship.getAlternateTerminologyIds().get(umlsTerminology));
    // assertTrue(relationship.getAlternateTerminologyIds().get(umlsTerminology)
    // .startsWith("R"));

    // verify the molecular action exists
    PfsParameterJpa pfs = new PfsParameterJpa();
    pfs.setSortField("lastModified");
    pfs.setAscending(false);
    MolecularActionList list = projectService.findMolecularActions(c.getId(),
        umlsTerminology, umlsVersion, null, pfs, authToken);
    assertTrue(list.size() > 0);
    MolecularAction ma = list.getObjects().get(0);
    assertNotNull(ma);
    assertEquals(c.getId(), ma.getComponentId());
    assertTrue(ma.getLastModified().compareTo(startDate) >= 0);
    assertNotNull(ma.getAtomicActions());

    // Verify that five atomic actions exists for add and attach relationships
    // to both concepts, and update
    // Concept WorkflowStatus for the source concept
    pfs.setSortField(null);

    List<AtomicAction> atomicActions = projectService
        .findAtomicActions(ma.getId(), null, pfs, authToken).getObjects();
    Collections.sort(atomicActions,
        (a1, a2) -> a1.getId().compareTo(a2.getId()));
    assertEquals(5, atomicActions.size());
    assertEquals("RELATIONSHIP", atomicActions.get(0).getIdType().toString());
    assertNull(atomicActions.get(0).getOldValue());
    assertNotNull(atomicActions.get(0).getNewValue());
    assertEquals("RELATIONSHIP", atomicActions.get(1).getIdType().toString());
    assertNull(atomicActions.get(1).getOldValue());
    assertNotNull(atomicActions.get(1).getNewValue());
    assertEquals("CONCEPT", atomicActions.get(2).getIdType().toString());
    assertNull(atomicActions.get(2).getOldValue());
    assertNotNull(atomicActions.get(2).getNewValue());
    assertEquals("relationships", atomicActions.get(2).getField());
    assertEquals("CONCEPT", atomicActions.get(3).getIdType().toString());
    assertNull(atomicActions.get(3).getOldValue());
    assertNotNull(atomicActions.get(3).getNewValue());
    assertEquals("relationships", atomicActions.get(3).getField());
    assertEquals(atomicActions.get(4).getIdType().toString(), "CONCEPT");
    assertNotNull(atomicActions.get(4).getOldValue());
    assertNotNull(atomicActions.get(4).getNewValue());

    // Verify the log entry exists
    String logEntry =
        projectService.getLog(project.getId(), c.getId(), null, 1, authToken);
    assertTrue(logEntry.contains("ADD_RELATIONSHIP to concept " + c2.getId()));
    logEntry =
        projectService.getLog(project.getId(), c2.getId(), null, 1, authToken);
    assertTrue(logEntry.contains("ADD_RELATIONSHIP from concept " + c.getId()));

    //
    // Add second relationship (also ensures alternateTerminologyId increments
    // correctly)
    //

    // Due to MySQL rounding to the second, we must also round our comparison
    // startDate.
    startDate = DateUtils.round(new Date(), Calendar.SECOND);

    // construct a relationship not present on concept (here, RelationshipType
    // RB to Concept CUI C0065642,ConceptId 88009 (set in setup).
    ConceptRelationshipJpa relationship3 = new ConceptRelationshipJpa();
    relationship3.setBranch(Branch.ROOT);
    relationship3.setRelationshipType("RB");
    relationship3.setAdditionalRelationshipType("");
    relationship3.setFrom(c);
    relationship3.setTo(c3);
    relationship3.setTerminologyId("TestId");
    relationship3.setTerminology(umlsTerminology);
    relationship3.setVersion(umlsVersion);
    relationship3.setTimestamp(new Date());
    relationship3.setPublishable(true);
    relationship3.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);

    //
    // add the second relationship to the concept
    //

    // add the relationship to the concept
    v = metaEditingService.addRelationship(project.getId(), c.getId(),
        "activityId", c.getLastModified().getTime(), relationship3, false,
        authToken);
    assertTrue(v.getErrors().isEmpty());

    // retrieve the concept and check to make sure both relationships are still
    // there
    c = contentService.getConcept(concept.getId(), project.getId(), authToken);
    c2 = contentService.getConcept(concept2.getId(), project.getId(),
        authToken);

    relList = contentService.findConceptRelationships(c.getTerminologyId(),
        c.getTerminology(), c.getVersion(), null, null, authToken);

    relationship = null;
    relationship3 = null;
    for (final Relationship<?, ?> rel : relList.getObjects()) {
      if (rel.getRelationshipType().equals("RN")
          && rel.getTo().getId().equals(c2.getId())) {
        relationship = (ConceptRelationshipJpa) rel;
      }
      if (rel.getRelationshipType().equals("RB")
          && rel.getTo().getId().equals(c3.getId())) {
        relationship3 = (ConceptRelationshipJpa) rel;
      }
    }
    assertNotNull(relationship);
    assertNotNull(relationship3);

    // verify the molecular action exists
    pfs = new PfsParameterJpa();
    pfs.setSortField("lastModified");
    pfs.setAscending(false);
    list = projectService.findMolecularActions(c.getId(), umlsTerminology,
        umlsVersion, null, pfs, authToken);
    assertTrue(list.size() > 0);
    ma = list.getObjects().get(0);
    assertNotNull(ma);
    assertEquals(c.getId(), ma.getComponentId());
    assertTrue(ma.getLastModified().compareTo(startDate) >= 0);
    assertNotNull(ma.getAtomicActions());

    // Verify that FOUR atomic actions exists the two add relationships (Concept
    // Workflow Status for FROM concept was already set during previous
    // addition, and Workflow Status for To Concept is not affected.
    // different TO concept will also be updated
    pfs.setSortField(null);

    atomicActions = projectService
        .findAtomicActions(ma.getId(), null, pfs, authToken).getObjects();
    Collections.sort(atomicActions,
        (a1, a2) -> a1.getId().compareTo(a2.getId()));
    assertEquals(4, atomicActions.size());
    assertEquals("RELATIONSHIP", atomicActions.get(0).getIdType().toString());
    assertNull(atomicActions.get(0).getOldValue());
    assertNotNull(atomicActions.get(0).getNewValue());
    assertEquals("RELATIONSHIP", atomicActions.get(1).getIdType().toString());
    assertNull(atomicActions.get(1).getOldValue());
    assertNotNull(atomicActions.get(1).getNewValue());
    assertEquals("CONCEPT", atomicActions.get(2).getIdType().toString());
    assertNull(atomicActions.get(2).getOldValue());
    assertNotNull(atomicActions.get(2).getNewValue());
    assertEquals("relationships", atomicActions.get(2).getField());
    assertEquals("CONCEPT", atomicActions.get(3).getIdType().toString());
    assertNull(atomicActions.get(3).getOldValue());
    assertNotNull(atomicActions.get(3).getNewValue());
    assertEquals("relationships", atomicActions.get(3).getField());

    // Verify the log entry exists
    logEntry =
        projectService.getLog(project.getId(), c.getId(), null, 1, authToken);
    assertTrue(logEntry.contains("ADD_RELATIONSHIP to concept " + c3.getId()));
    logEntry =
        projectService.getLog(project.getId(), c3.getId(), null, 1, authToken);
    assertTrue(logEntry.contains("ADD_RELATIONSHIP from concept " + c.getId()));

    //
    // Test removal
    //

    // remove the first relationship from the concept
    v = metaEditingService.removeRelationship(project.getId(), c.getId(),
        "activityId", c.getLastModified().getTime(), relationship.getId(),
        false, authToken);
    assertTrue(v.getErrors().isEmpty());

    c = contentService.getConcept(concept.getId(), project.getId(), authToken);
    relList = contentService.findConceptRelationships(c.getTerminologyId(),
        c.getTerminology(), c.getVersion(), null, null, authToken);

    boolean relationshipPresent = false;
    for (final Relationship<?, ?> rel : relList.getObjects()) {
      if (rel.getRelationshipType().equals("RN")
          && rel.getTo().getId().equals(c2.getId())
          && rel.getFrom().getId().equals(c.getId())) {
        relationshipPresent = true;
      }
    }
    assertTrue(!relationshipPresent);

    // Make sure inverse was removed too

    // verify the molecular action exists
    pfs = new PfsParameterJpa();
    pfs.setSortField("lastModified");
    pfs.setAscending(false);
    list = projectService.findMolecularActions(c.getId(), umlsTerminology,
        umlsVersion, null, pfs, authToken);
    assertTrue(list.size() > 0);
    ma = list.getObjects().get(0);
    assertNotNull(ma);
    assertEquals(c.getId(), ma.getComponentId());
    assertTrue(ma.getLastModified().compareTo(startDate) >= 0);
    assertNotNull(ma.getAtomicActions());

    // Verify that four atomic actions exist for remove Relationship and inverse
    pfs.setSortField(null);

    atomicActions = projectService
        .findAtomicActions(ma.getId(), null, pfs, authToken).getObjects();
    Collections.sort(atomicActions,
        (a1, a2) -> a1.getId().compareTo(a2.getId()));
    assertEquals(4, atomicActions.size());
    assertEquals("CONCEPT", atomicActions.get(0).getIdType().toString());
    assertNotNull(atomicActions.get(0).getOldValue());
    assertNull(atomicActions.get(0).getNewValue());
    assertEquals("relationships", atomicActions.get(0).getField());
    assertEquals("CONCEPT", atomicActions.get(1).getIdType().toString());
    assertNotNull(atomicActions.get(1).getOldValue());
    assertNull(atomicActions.get(1).getNewValue());
    assertEquals("relationships", atomicActions.get(1).getField());
    assertEquals("RELATIONSHIP", atomicActions.get(2).getIdType().toString());
    assertNotNull(atomicActions.get(2).getOldValue());
    assertNull(atomicActions.get(2).getNewValue());
    assertEquals("RELATIONSHIP", atomicActions.get(3).getIdType().toString());
    assertNotNull(atomicActions.get(3).getOldValue());
    assertNull(atomicActions.get(3).getNewValue());

    // Verify the log entry exists
    logEntry =
        projectService.getLog(project.getId(), c.getId(), null, 1, authToken);
    assertTrue(
        logEntry.contains("REMOVE_RELATIONSHIP to concept " + c2.getId()));
    logEntry =
        projectService.getLog(project.getId(), c2.getId(), null, 1, authToken);
    assertTrue(
        logEntry.contains("REMOVE_RELATIONSHIP from concept " + c.getId()));

    // remove the second relationship from the concept (assume verification of
    // MA,
    // atomic actions, and log entry since we just tested those)
    v = metaEditingService.removeRelationship(project.getId(), c.getId(),
        "activityId", c.getLastModified().getTime(), relationship3.getId(),
        false, authToken);
    assertTrue(v.getErrors().isEmpty());

    // retrieve the concept and check relationships
    c = contentService.getConcept(concept.getId(), project.getId(), authToken);

    relList = contentService.findConceptRelationships(c.getTerminologyId(),
        c.getTerminology(), c.getVersion(), null, null, authToken);

    boolean relationship3Present = false;
    for (final Relationship<?, ?> rel : relList.getObjects()) {
      if (rel.getRelationshipType().equals("RB")
          && rel.getTo().getId().equals(c3.getId())
          && rel.getFrom().getId().equals(c.getId())) {
        relationship3Present = true;
      }
    }
    assertTrue(!relationship3Present);

  }

  /**
   * Test merge concepts.
   *
   * @throws Exception the exception
   */
  @Test
  public void testMergeConcepts() throws Exception {
    Logger.getLogger(getClass()).debug("Start test " + name.getMethodName());

    Logger.getLogger(getClass())
        .info("TEST - Merge concept CONCEPTID into concept CONCEPTID2, "
            + umlsTerminology + ", " + umlsVersion + authToken);

    //
    // Prepare the test and check prerequisites
    //
    // Due to MySQL rounding to the second, we must also round our comparison
    // startDate.
    Date startDate = DateUtils.round(new Date(), Calendar.SECOND);

    // Populate concept components
    populateConcepts();

    // Create a DEMOTION between concept and concept2, and its inverse
    Atom fromAtom = concept.getAtoms().get(0);
    Atom toAtom = concept2.getAtoms().get(0);

    AtomRelationship demotion = new AtomRelationshipJpa();
    demotion.setFrom(fromAtom);
    demotion.setTo(toAtom);
    demotion.setTerminology(umlsTerminology);
    demotion.setTerminologyId("");
    demotion.setBranch(Branch.ROOT);
    demotion.setName("Test Demotion");
    demotion.setVersion(umlsVersion);
    demotion.setRelationshipType("RO");
    demotion.setWorkflowStatus(WorkflowStatus.DEMOTION);
    demotion =
        testService.addRelationship((AtomRelationshipJpa) demotion, authToken);

    AtomRelationship inverseDemotion = new AtomRelationshipJpa();
    inverseDemotion.setFrom(toAtom);
    inverseDemotion.setTo(fromAtom);
    inverseDemotion.setTerminology(umlsTerminology);
    inverseDemotion.setTerminologyId("");
    inverseDemotion.setBranch(Branch.ROOT);
    inverseDemotion.setName("Test Demotion");
    inverseDemotion.setVersion(umlsVersion);
    inverseDemotion.setRelationshipType("RO");
    inverseDemotion.setWorkflowStatus(WorkflowStatus.DEMOTION);
    inverseDemotion = testService
        .addRelationship((AtomRelationshipJpa) inverseDemotion, authToken);

    final Long demotionRelationshipId = demotion.getId();
    final Long inverseDemotionRelationshipId = inverseDemotion.getId();

    // Add demotions to atoms and update
    fromAtom.getRelationships().add(demotion);
    toAtom.getRelationships().add(inverseDemotion);

    testService.updateAtom((AtomJpa) fromAtom, authToken);
    testService.updateAtom((AtomJpa) toAtom, authToken);

    fromAtom = testService.getAtom(fromAtom.getId(), authToken);
    toAtom = testService.getAtom(toAtom.getId(), authToken);

    // Make sure the demotions are there
    boolean demotionPresent = false;
    for (AtomRelationship atomRel : fromAtom.getRelationships()) {
      if (atomRel.getId().equals(demotionRelationshipId)) {
        demotionPresent = true;
        break;
      }
    }
    assertTrue(demotionPresent);

    boolean inverseDemotionPresent = false;
    for (AtomRelationship atomRel : toAtom.getRelationships()) {
      if (atomRel.getId().equals(inverseDemotionRelationshipId)) {
        inverseDemotionPresent = true;
        break;
      }
    }
    assertTrue(inverseDemotionPresent);

    // get the fromConcept, toConcept, and relatedConcept
    Concept toC =
        contentService.getConcept(concept.getId(), project.getId(), authToken);
    assertNotNull(toC);
    Concept fromC =
        contentService.getConcept(concept2.getId(), project.getId(), authToken);
    assertNotNull(fromC);
    Concept relatedC =
        contentService.getConcept(concept3.getId(), project.getId(), authToken);
    assertNotNull(relatedC);

    // Save fromC and ID to check molecular Action and relationship once Concept
    // is removed
    final Long fromCId = fromC.getId();

    // Now that the concepts are all set up, merge them.
    ValidationResult v = metaEditingService.mergeConcepts(project.getId(),
        fromC.getId(), "activityId", fromC.getLastModified().getTime(),
        toC.getId(), false, authToken);
    assertTrue(v.getErrors().isEmpty());

    toC = contentService.getConcept(toC.getId(), project.getId(), authToken);
    relatedC =
        contentService.getConcept(relatedC.getId(), project.getId(), authToken);
    fromAtom = testService.getAtom(fromAtom.getId(), authToken);
    toAtom = testService.getAtom(toAtom.getId(), authToken);

    // Verify fromConcept has been removed
    fromC = contentService.getConcept(fromCId, project.getId(), authToken);
    assertTrue(fromC == null);

    // Update concept, to help with teardown later
    concept2 = fromC;

    // Verify fromConcept atom is now present in toConcept, along with original
    // toConcept atom
    int atomCount = 0;
    for (Atom a : toC.getAtoms()) {
      if (a.getName().equals("DCB") || a.getName().equals("17 Oxosteroids")) {
        atomCount++;
      }
    }
    assertEquals(2, atomCount);

    // Verify fromConcept Semantic type is now present in toConcept, along with
    // original toConcept Semantic type
    int styCount = 0;
    for (SemanticTypeComponent sty : toC.getSemanticTypes()) {
      if (sty.getSemanticType().equals("Lipid")
          || sty.getSemanticType().equals("Enzyme")) {
        styCount++;
      }
    }
    assertEquals(2, styCount);

    // Verify relationship between to and from Concept has been removed
    RelationshipList relList =
        contentService.findConceptRelationships(toC.getTerminologyId(),
            toC.getTerminology(), toC.getVersion(), null, null, authToken);

    boolean relationshipPresent = false;
    for (final Relationship<?, ?> rel : relList.getObjects()) {
      if (rel.getTo().getId().equals(fromCId)
          && rel.getFrom().getId().equals(toC.getId())) {
        relationshipPresent = true;
      }
    }
    assertTrue(!relationshipPresent);

    // Verify the DEMOTION relationship and its inverse have been deleted
    demotionPresent = false;
    for (AtomRelationship atomRel : fromAtom.getRelationships()) {
      if (atomRel.getId().equals(demotionRelationshipId)) {
        demotionPresent = true;
      }
    }
    assertFalse(demotionPresent);

    inverseDemotionPresent = false;
    for (AtomRelationship atomRel : toAtom.getRelationships()) {
      if (atomRel.getId().equals(inverseDemotionRelationshipId)) {
        inverseDemotionPresent = true;
      }
    }
    assertFalse(inverseDemotionPresent);

    // Verify that relationships from fromConcept have been added to toConcept
    relationshipPresent = false;
    for (final Relationship<?, ?> rel : relList.getObjects()) {
      if (rel.getTo().getId().equals(relatedC.getId())
          && rel.getFrom().getId().equals(toC.getId())) {
        relationshipPresent = true;
      }
    }
    assertTrue(relationshipPresent);

    // verify the molecular action exists
    PfsParameterJpa pfs = new PfsParameterJpa();
    pfs.setSortField("lastModified");
    pfs.setAscending(false);
    MolecularActionList list = projectService.findMolecularActions(fromCId,
        umlsTerminology, umlsVersion, null, pfs, authToken);
    assertTrue(list.size() > 0);
    MolecularAction ma = list.getObjects().get(0);
    assertNotNull(ma);
    assertEquals(fromCId, ma.getComponentId());
    assertEquals(toC.getId(), ma.getComponentId2());
    assertTrue(ma.getLastModified().compareTo(startDate) >= 0);
    assertNotNull(ma.getAtomicActions());

    // Verify that atomic actions exists for moving atoms,
    // adding/removing Semantic Types, and for adding/removing Relationships
    // 1 for removing Demotion from Atom
    // 1 for removing Relationship from toConcept
    // 2 for removing Atom from fromConcept
    // 2 for removing Relationships from fromConcept
    // 3 for removing Semantic Type from fromConcept
    // 1 for removing inverse Relationships from related Concept
    // 3 for deleting Semantic Types
    // 4 for deleting Relationships
    // 2 for creating Semantic Types for toConcept
    // 2 for adding Atoms into toConcept
    // 2 for adding Semantic Types into toConcept
    // 1 for deleting fromConcept
    pfs.setSortField(null);

    List<AtomicAction> atomicActions = projectService
        .findAtomicActions(ma.getId(), null, pfs, authToken).getObjects();
    Collections.sort(atomicActions,
        (a1, a2) -> a1.getId().compareTo(a2.getId()));
    assertEquals(27, atomicActions.size());
    // TODO - fix or drop

    // assertEquals("CONCEPT", atomicActions.get(0).getIdType().toString());
    // assertNotNull(atomicActions.get(0).getOldValue());
    // assertNull(atomicActions.get(0).getNewValue());
    // assertEquals("CONCEPT", atomicActions.get(1).getIdType().toString());
    // assertNotNull(atomicActions.get(1).getOldValue());
    // assertNull(atomicActions.get(1).getNewValue());
    // assertEquals("CONCEPT", atomicActions.get(2).getIdType().toString());
    // assertNotNull(atomicActions.get(2).getOldValue());
    // assertNull(atomicActions.get(2).getNewValue());
    // assertEquals("CONCEPT", atomicActions.get(3).getIdType().toString());
    // assertNotNull(atomicActions.get(3).getOldValue());
    // assertNull(atomicActions.get(3).getNewValue());
    // assertEquals("CONCEPT", atomicActions.get(4).getIdType().toString());
    // assertNotNull(atomicActions.get(4).getOldValue());
    // assertNull(atomicActions.get(4).getNewValue());
    // assertEquals("CONCEPT", atomicActions.get(5).getIdType().toString());
    // assertNotNull(atomicActions.get(5).getOldValue());
    // assertNull(atomicActions.get(5).getNewValue());
    // assertEquals("CONCEPT", atomicActions.get(6).getIdType().toString());
    // assertNotNull(atomicActions.get(6).getOldValue());
    // assertNull(atomicActions.get(6).getNewValue());
    // assertEquals("CONCEPT", atomicActions.get(7).getIdType().toString());
    // assertNotNull(atomicActions.get(7).getOldValue());
    // assertNull(atomicActions.get(7).getNewValue());
    // assertEquals("CONCEPT", atomicActions.get(8).getIdType().toString());
    // assertNotNull(atomicActions.get(8).getOldValue());
    // assertNull(atomicActions.get(8).getNewValue());
    // assertEquals("SEMANTIC_TYPE",
    // atomicActions.get(9).getIdType().toString());
    // assertNotNull(atomicActions.get(9).getOldValue());
    // assertNull(atomicActions.get(9).getNewValue());
    // assertEquals("SEMANTIC_TYPE",
    // atomicActions.get(10).getIdType().toString());
    // assertNotNull(atomicActions.get(10).getOldValue());
    // assertNull(atomicActions.get(10).getNewValue());
    // assertEquals("SEMANTIC_TYPE",
    // atomicActions.get(11).getIdType().toString());
    // assertNotNull(atomicActions.get(11).getOldValue());
    // assertNull(atomicActions.get(11).getNewValue());
    // assertEquals("RELATIONSHIP",
    // atomicActions.get(12).getIdType().toString());
    // assertNotNull(atomicActions.get(12).getOldValue());
    // assertNull(atomicActions.get(12).getNewValue());
    // assertEquals("RELATIONSHIP",
    // atomicActions.get(13).getIdType().toString());
    // assertNotNull(atomicActions.get(13).getOldValue());
    // assertNull(atomicActions.get(13).getNewValue());
    // assertEquals("RELATIONSHIP",
    // atomicActions.get(14).getIdType().toString());
    // assertNotNull(atomicActions.get(14).getOldValue());
    // assertNull(atomicActions.get(14).getNewValue());
    // assertEquals("RELATIONSHIP",
    // atomicActions.get(15).getIdType().toString());
    // assertNotNull(atomicActions.get(15).getOldValue());
    // assertNull(atomicActions.get(15).getNewValue());
    // assertEquals("SEMANTIC_TYPE",
    // atomicActions.get(16).getIdType().toString());
    // assertNull(atomicActions.get(16).getOldValue());
    // assertNotNull(atomicActions.get(16).getNewValue());
    // assertEquals("SEMANTIC_TYPE",
    // atomicActions.get(17).getIdType().toString());
    // assertNull(atomicActions.get(17).getOldValue());
    // assertNotNull(atomicActions.get(17).getNewValue());
    // assertEquals("CONCEPT", atomicActions.get(18).getIdType().toString());
    // assertNull(atomicActions.get(18).getOldValue());
    // assertNotNull(atomicActions.get(18).getNewValue());
    // assertEquals("atoms", atomicActions.get(18).getField());
    // assertEquals("CONCEPT", atomicActions.get(19).getIdType().toString());
    // assertNull(atomicActions.get(19).getOldValue());
    // assertNotNull(atomicActions.get(19).getNewValue());
    // assertEquals("atoms", atomicActions.get(19).getField());
    // assertEquals("CONCEPT", atomicActions.get(20).getIdType().toString());
    // assertNull(atomicActions.get(20).getOldValue());
    // assertNotNull(atomicActions.get(20).getNewValue());
    // assertEquals("semanticTypes", atomicActions.get(20).getField());
    // assertEquals("CONCEPT", atomicActions.get(21).getIdType().toString());
    // assertNull(atomicActions.get(21).getOldValue());
    // assertNotNull(atomicActions.get(21).getNewValue());
    // assertEquals("semanticTypes", atomicActions.get(21).getField());
    // assertEquals("CONCEPT", atomicActions.get(22).getIdType().toString());
    // assertNotNull(atomicActions.get(22).getOldValue());
    // assertNull(atomicActions.get(22).getNewValue());

    // Verify the log entry exists
    String logEntry =
        projectService.getLog(project.getId(), fromCId, null, 1, authToken);
    assertTrue(logEntry
        .contains("MERGE concept " + fromCId + " into concept " + toC.getId()));
    logEntry =
        projectService.getLog(project.getId(), toC.getId(), null, 1, authToken);
    assertTrue(logEntry
        .contains("MERGE concept " + toC.getId() + " from concept " + fromCId));

  }

  /**
   * Test move atoms.
   *
   * @throws Exception the exception
   */
  @Test
  public void testMoveAtoms() throws Exception {
    Logger.getLogger(getClass()).debug("Start test " + name.getMethodName());

    Logger.getLogger(getClass()).info(
        "TEST - Move atoms from concept FROMCONCEPTID into concept TOCONCEPTID, "
            + umlsTerminology + ", " + umlsVersion + authToken);

    //
    // Prepare the test and check prerequisites
    //
    // Due to MySQL rounding to the second, we must also round our comparison
    // startDate.
    Date startDate = DateUtils.round(new Date(), Calendar.SECOND);

    // Populate concept components
    populateConcepts();

    // Create a DEMOTION between concept and concept2, and its inverse
    Atom fromAtom = concept.getAtoms().get(0);
    Atom toAtom = concept2.getAtoms().get(0);

    AtomRelationship demotion = new AtomRelationshipJpa();
    demotion.setFrom(fromAtom);
    demotion.setTo(toAtom);
    demotion.setTerminology(umlsTerminology);
    demotion.setTerminologyId("");
    demotion.setBranch(Branch.ROOT);
    demotion.setName("Test Demotion");
    demotion.setVersion(umlsVersion);
    demotion.setRelationshipType("RO");
    demotion.setWorkflowStatus(WorkflowStatus.DEMOTION);
    demotion =
        testService.addRelationship((AtomRelationshipJpa) demotion, authToken);

    AtomRelationship inverseDemotion = new AtomRelationshipJpa();
    inverseDemotion.setFrom(toAtom);
    inverseDemotion.setTo(fromAtom);
    inverseDemotion.setTerminology(umlsTerminology);
    inverseDemotion.setTerminologyId("");
    inverseDemotion.setBranch(Branch.ROOT);
    inverseDemotion.setName("Test Demotion");
    inverseDemotion.setVersion(umlsVersion);
    inverseDemotion.setRelationshipType("RO");
    inverseDemotion.setWorkflowStatus(WorkflowStatus.DEMOTION);
    inverseDemotion = testService
        .addRelationship((AtomRelationshipJpa) inverseDemotion, authToken);

    final Long demotionRelationshipId = demotion.getId();
    final Long inverseDemotionRelationshipId = inverseDemotion.getId();

    // Add demotions to atoms and update
    fromAtom.getRelationships().add(demotion);
    toAtom.getRelationships().add(inverseDemotion);

    testService.updateAtom((AtomJpa) fromAtom, authToken);
    testService.updateAtom((AtomJpa) toAtom, authToken);

    fromAtom = testService.getAtom(fromAtom.getId(), authToken);
    toAtom = testService.getAtom(toAtom.getId(), authToken);

    // Make sure the demotions are there
    boolean demotionPresent = false;
    for (AtomRelationship atomRel : fromAtom.getRelationships()) {
      if (atomRel.getId().equals(demotionRelationshipId)) {
        demotionPresent = true;
        break;
      }
    }
    assertTrue(demotionPresent);

    boolean inverseDemotionPresent = false;
    for (AtomRelationship atomRel : toAtom.getRelationships()) {
      if (atomRel.getId().equals(inverseDemotionRelationshipId)) {
        inverseDemotionPresent = true;
        break;
      }
    }
    assertTrue(inverseDemotionPresent);

    // get the fromConcept and the toConcept
    Concept fromC =
        contentService.getConcept(concept.getId(), project.getId(), authToken);
    assertNotNull(fromC);
    Concept toC =
        contentService.getConcept(concept2.getId(), project.getId(), authToken);
    assertNotNull(toC);

    // Get the atoms so we can extract the IDs
    Atom atom = null;
    Atom atom2 = null;
    Atom atom3 = null;
    Atom atom4 = null;
    for (Atom a : fromC.getAtoms()) {
      if (a.getName().equals("DCB")) {
        atom = a;
      }
      if (a.getName().equals("IPA")) {
        atom2 = a;
      }
    }
    for (Atom a : toC.getAtoms()) {
      if (a.getName().equals("17 Oxosteroids")) {
        atom3 = a;
      }
      if (a.getName().equals("PABA")) {
        atom4 = a;
      }
    }
    assertNotNull(atom);
    assertNotNull(atom2);
    assertNotNull(atom3);
    assertNotNull(atom4);

    List<Long> moveList = new ArrayList<Long>();
    moveList.add(atom.getId());
    moveList.add(atom2.getId());

    // Move all of the atoms fromConcept to toConcept.
    ValidationResult v = metaEditingService.moveAtoms(project.getId(),
        fromC.getId(), "activityId", fromC.getLastModified().getTime(),
        toC.getId(), moveList, false, authToken);
    assertTrue(v.getErrors().isEmpty());

    fromC =
        contentService.getConcept(concept.getId(), project.getId(), authToken);
    toC =
        contentService.getConcept(concept2.getId(), project.getId(), authToken);
    fromAtom = testService.getAtom(fromAtom.getId(), authToken);
    toAtom = testService.getAtom(toAtom.getId(), authToken);

    // Verify fromConcept atoms are now present in toConcept, along with
    // original toConcept atom
    int atomCount = 0;
    for (Atom a : toC.getAtoms()) {
      if (a.getName().equals("DCB") || a.getName().equals("17 Oxosteroids")
          || a.getName().equals("PABA") || a.getName().equals("IPA")) {
        atomCount++;
      }
    }
    assertEquals(4, atomCount);

    // Verify fromConcept atoms that were in the list are no longer present
    atomCount = 0;
    for (Atom a : fromC.getAtoms()) {
      if (moveList.contains(a.getId())) {
        atomCount++;
      }
    }
    assertEquals(0, atomCount);

    // Verify the DEMOTION relationship and its inverse have been deleted
    demotionPresent = false;
    for (AtomRelationship atomRel : fromAtom.getRelationships()) {
      if (atomRel.getId().equals(demotionRelationshipId)) {
        demotionPresent = true;
      }
    }
    assertFalse(demotionPresent);

    inverseDemotionPresent = false;
    for (AtomRelationship atomRel : toAtom.getRelationships()) {
      if (atomRel.getId().equals(inverseDemotionRelationshipId)) {
        inverseDemotionPresent = true;
      }
    }
    assertFalse(inverseDemotionPresent);

    // verify the molecular action exists
    PfsParameterJpa pfs = new PfsParameterJpa();
    pfs.setSortField("lastModified");
    pfs.setAscending(false);
    MolecularActionList list = projectService.findMolecularActions(
        fromC.getId(), umlsTerminology, umlsVersion, null, pfs, authToken);
    assertTrue(list.size() > 0);
    MolecularAction ma = list.getObjects().get(0);
    assertNotNull(ma);
    assertEquals(fromC.getId(), ma.getComponentId());
    assertEquals(toC.getId(), ma.getComponentId2());
    assertTrue(ma.getLastModified().compareTo(startDate) >= 0);
    assertNotNull(ma.getAtomicActions());

    // Verify that atomic action exists, one for each atom move.
    pfs.setSortField(null);

    List<AtomicAction> atomicActions = projectService
        .findAtomicActions(ma.getId(), null, pfs, authToken).getObjects();
    Collections.sort(atomicActions,
        (a1, a2) -> a1.getId().compareTo(a2.getId()));
    assertEquals(8, atomicActions.size());
    // TODO - fix or drop

    // assertEquals("CONCEPT", atomicActions.get(0).getIdType().toString());
    // assertNotNull(atomicActions.get(0).getOldValue());
    // assertNull(atomicActions.get(0).getNewValue());
    // assertEquals("atoms", atomicActions.get(0).getField());
    // assertEquals("CONCEPT", atomicActions.get(1).getIdType().toString());
    // assertNotNull(atomicActions.get(1).getOldValue());
    // assertNull(atomicActions.get(1).getNewValue());
    // assertEquals("atoms", atomicActions.get(1).getField());
    // assertEquals("CONCEPT", atomicActions.get(2).getIdType().toString());
    // assertNull(atomicActions.get(2).getOldValue());
    // assertNotNull(atomicActions.get(2).getNewValue());
    // assertEquals("atoms", atomicActions.get(2).getField());
    // assertEquals("CONCEPT", atomicActions.get(3).getIdType().toString());
    // assertNull(atomicActions.get(3).getOldValue());
    // assertNotNull(atomicActions.get(3).getNewValue());
    // assertEquals("atoms", atomicActions.get(3).getField());

    // Verify the log entry exists
    String logEntry = projectService.getLog(project.getId(), fromC.getId(),
        null, 1, authToken);
    assertTrue(logEntry.contains("MOVE " + moveList + " from Concept "
        + fromC.getId() + " to concept " + toC.getId()));
    logEntry =
        projectService.getLog(project.getId(), toC.getId(), null, 1, authToken);
    assertTrue(logEntry.contains("MOVE " + moveList + " to Concept "
        + toC.getId() + " from concept " + fromC.getId()));

  }

  /**
   * Test split concept.
   *
   * @throws Exception the exception
   */
  @Test
  public void testSplitConcept() throws Exception {
    Logger.getLogger(getClass()).debug("Start test " + name.getMethodName());

    Logger.getLogger(getClass()).info("TEST - Split concept CONCEPTID, "
        + umlsTerminology + ", " + umlsVersion + authToken);

    //
    // Prepare the test and check prerequisites
    //
    // Due to MySQL rounding to the second, we must also round our comparison
    // startDate.
    Date startDate = DateUtils.round(new Date(), Calendar.SECOND);

    // Populate concept components
    populateConcepts();

    // get the fromConcept, toConcept, and relatedConcept
    Concept originatingC =
        contentService.getConcept(concept.getId(), project.getId(), authToken);
    assertNotNull(originatingC);
    Concept relatedC =
        contentService.getConcept(concept3.getId(), project.getId(), authToken);
    assertNotNull(relatedC);

    // Get the atoms so we can extract the IDs
    Atom atom = null;
    Atom atom2 = null;
    for (Atom a : originatingC.getAtoms()) {
      if (a.getName().equals("DCB")) {
        atom = a;
      }
      if (a.getName().equals("IPA")) {
        atom2 = a;
      }
    }
    assertNotNull(atom);
    assertNotNull(atom2);

    // Create a list of the atoms we'll be splitting out into the new concept
    List<Long> moveAtomIds = new ArrayList<Long>();
    moveAtomIds.add(atom2.getId());

    // Split the atoms out into a new concept,and give the concept an RN
    // relation to the new one.
    // Transfer over semantic types or relationships
    ValidationResult v =
        metaEditingService.splitConcept(project.getId(), originatingC.getId(),
            "activityId", originatingC.getLastModified().getTime(), moveAtomIds,
            false, true, true, "RN", authToken);
    assertTrue(v.getErrors().isEmpty());

    // Identify the newly created concept by finding the most recently modified
    // concept that includes one of the moved atoms
    PfsParameterJpa pfs = new PfsParameterJpa();
    pfs = new PfsParameterJpa();
    pfs.setSortField("lastModified");
    pfs.setAscending(false);
    pfs.setMaxResults(1);
    pfs.setStartIndex(0);

    Concept createdC = contentService.getConcept(contentService
        .findConcepts(umlsTerminology, umlsVersion,
            "atoms.id:" + moveAtomIds.get(0), pfs, authToken)
        .getObjects().get(0).getId(), project.getId(), authToken);

    // Set this to concept4, so it can be handled in cleanup.
    concept4 = createdC;

    originatingC =
        contentService.getConcept(concept.getId(), project.getId(), authToken);
    relatedC =
        contentService.getConcept(concept3.getId(), project.getId(), authToken);

    // Verify split atom is now present in created Concept, and not present in
    // originating Concept
    assertTrue(createdC.getAtoms().contains(atom2));
    assertTrue(!originatingC.getAtoms().contains(atom2));

    // Verify non-split atom is still present in originating Concept, and is not
    // present in created Concept
    assertTrue(originatingC.getAtoms().contains(atom));
    assertTrue(!createdC.getAtoms().contains(atom));

    // Verify originating Concept Semantic type is now present in created
    // Concept
    int styCount = 0;
    for (SemanticTypeComponent sty : createdC.getSemanticTypes()) {
      if (sty.getSemanticType().equals("Lipid")) {
        styCount++;
      }
    }
    assertEquals(1, styCount);

    // Verify that the originating and created Concepts have a relationship
    // between them of the specified type
    RelationshipList originatingConceptRels =
        contentService.findConceptRelationships(originatingC.getTerminologyId(),
            originatingC.getTerminology(), originatingC.getVersion(),
            "fromId:" + originatingC.getId(), null, authToken);

    boolean relationshipPresent = false;
    for (final Relationship<?, ?> rel : originatingConceptRels.getObjects()) {
      if (rel.getFrom().getId().equals(originatingC.getId())
          && rel.getTo().getId().equals(createdC.getId())
          && rel.getRelationshipType().equals("RN")) {
        relationshipPresent = true;
      }
    }
    assertTrue(relationshipPresent);

    // Verify that the same is true for the inverse
    RelationshipList createdConceptRels =
        contentService.findConceptRelationships(createdC.getTerminologyId(),
            createdC.getTerminology(), createdC.getVersion(), null, null,
            authToken);

    relationshipPresent = false;
    for (final Relationship<?, ?> rel : createdConceptRels.getObjects()) {
      if (rel.getFrom().getId().equals(createdC.getId())
          && rel.getTo().getId().equals(originatingC.getId())
          && rel.getRelationshipType().equals("RB")) {
        relationshipPresent = true;
      }
    }
    assertTrue(relationshipPresent);

    // Verify that relationships from originatingConcept have been added to
    // createdConcept
    relationshipPresent = false;
    for (final Relationship<?, ?> rel : createdConceptRels.getObjects()) {
      if (rel.getFrom().getId().equals(createdC.getId())
          && rel.getTo().getId().equals(relatedC.getId())
          && rel.getRelationshipType().equals("RN")) {
        relationshipPresent = true;
      }
    }
    assertTrue(relationshipPresent);

    // Verify that the same is true for the inverse
    RelationshipList relatedConceptRels =
        contentService.findConceptRelationships(relatedC.getTerminologyId(),
            relatedC.getTerminology(), relatedC.getVersion(),
            "fromId:" + relatedC.getId(), null, authToken);

    relationshipPresent = false;
    for (final Relationship<?, ?> rel : relatedConceptRels.getObjects()) {
      if (rel.getFrom().getId().equals(relatedC.getId())
          && rel.getTo().getId().equals(createdC.getId())
          && rel.getRelationshipType().equals("RB")) {
        relationshipPresent = true;
      }
    }
    assertTrue(relationshipPresent);

    // verify the molecular action exists
    pfs = new PfsParameterJpa();
    pfs.setSortField("lastModified");
    pfs.setAscending(false);
    MolecularActionList list =
        projectService.findMolecularActions(originatingC.getId(),
            umlsTerminology, umlsVersion, null, pfs, authToken);
    assertTrue(list.size() > 0);
    MolecularAction ma = list.getObjects().get(0);
    assertNotNull(ma);
    assertEquals(originatingC.getId(), ma.getComponentId());
    assertTrue(ma.getLastModified().compareTo(startDate) >= 0);
    assertNotNull(ma.getAtomicActions());

    // Verify that atomic actions exists for splitting off atoms, creating new
    // Concept
    // adding Semantic Types, and for adding Relationships
    // 1 for removing Atom from originatingConcept
    // 1 for creating createdConcept
    // 1 for creating Semantic Types for createdConcept
    // 6 for creating Relationships for createdConcept, originatingConcept, and
    // related concepts.
    // 1 for adding relationship to originatingConcept
    // 1 for updating TerminologyId on the createdConcept
    // 1 for adding Atom to createdConcept
    // 3 for adding Relationships to createdConcept
    // 1 for adding SemanticType to createdConcept
    // 2 for adding Relationships to related concepts

    // pfs.setSortField(null);
    pfs.setSortFields(
        new ArrayList<>(Arrays.asList("idType", "field", "oldValue")));
    pfs.setAscending(true);

    List<AtomicAction> atomicActions = projectService
        .findAtomicActions(ma.getId(), null, pfs, authToken).getObjects();
    assertEquals(18, atomicActions.size());
    // TODO - fix this list, or rip it out.

    // assertEquals("CONCEPT", atomicActions.get(0).getIdType().toString());
    // assertNotNull(atomicActions.get(0).getOldValue());
    // assertNull(atomicActions.get(0).getNewValue());
    // assertEquals("atoms", atomicActions.get(0).getField());
    // assertEquals("CONCEPT", atomicActions.get(1).getIdType().toString());
    // assertNull(atomicActions.get(1).getOldValue());
    // assertNotNull(atomicActions.get(1).getNewValue());
    // assertEquals("SEMANTIC_TYPE",
    // atomicActions.get(2).getIdType().toString());
    // assertNull(atomicActions.get(2).getOldValue());
    // assertNotNull(atomicActions.get(2).getNewValue());
    // assertEquals("RELATIONSHIP",
    // atomicActions.get(3).getIdType().toString());
    // assertNull(atomicActions.get(3).getOldValue());
    // assertNotNull(atomicActions.get(3).getNewValue());
    // assertEquals("RELATIONSHIP",
    // atomicActions.get(4).getIdType().toString());
    // assertNull(atomicActions.get(4).getOldValue());
    // assertNotNull(atomicActions.get(4).getNewValue());
    // assertEquals("RELATIONSHIP",
    // atomicActions.get(5).getIdType().toString());
    // assertNull(atomicActions.get(5).getOldValue());
    // assertNotNull(atomicActions.get(5).getNewValue());
    // assertEquals("RELATIONSHIP",
    // atomicActions.get(6).getIdType().toString());
    // assertNull(atomicActions.get(6).getOldValue());
    // assertNotNull(atomicActions.get(6).getNewValue());
    // assertEquals("RELATIONSHIP",
    // atomicActions.get(7).getIdType().toString());
    // assertNull(atomicActions.get(7).getOldValue());
    // assertNotNull(atomicActions.get(7).getNewValue());
    // assertEquals("RELATIONSHIP",
    // atomicActions.get(8).getIdType().toString());
    // assertNull(atomicActions.get(8).getOldValue());
    // assertNotNull(atomicActions.get(8).getNewValue());
    // assertEquals("CONCEPT", atomicActions.get(9).getIdType().toString());
    // assertNull(atomicActions.get(9).getOldValue());
    // assertNotNull(atomicActions.get(9).getNewValue());
    // assertEquals("relationships", atomicActions.get(9).getField());
    // assertEquals("CONCEPT", atomicActions.get(10).getIdType().toString());
    // assertNull(atomicActions.get(10).getOldValue());
    // assertNotNull(atomicActions.get(10).getNewValue());
    // assertEquals("terminologyId", atomicActions.get(10).getField());
    // assertEquals("CONCEPT", atomicActions.get(11).getIdType().toString());
    // assertEquals("", atomicActions.get(11).getOldValue());
    // assertNotNull(atomicActions.get(11).getNewValue());
    // assertEquals("atoms", atomicActions.get(11).getField());
    // assertEquals("CONCEPT", atomicActions.get(12).getIdType().toString());
    // assertNull(atomicActions.get(12).getOldValue());
    // assertNotNull(atomicActions.get(12).getNewValue());
    // assertEquals("relationships", atomicActions.get(12).getField());
    // assertEquals("CONCEPT", atomicActions.get(13).getIdType().toString());
    // assertNull(atomicActions.get(13).getOldValue());
    // assertNotNull(atomicActions.get(13).getNewValue());
    // assertEquals("relationships", atomicActions.get(13).getField());
    // assertEquals("CONCEPT", atomicActions.get(14).getIdType().toString());
    // assertNull(atomicActions.get(14).getOldValue());
    // assertNotNull(atomicActions.get(14).getNewValue());
    // assertEquals("relationships", atomicActions.get(14).getField());
    // assertEquals("CONCEPT", atomicActions.get(15).getIdType().toString());
    // assertNull(atomicActions.get(15).getOldValue());
    // assertNotNull(atomicActions.get(15).getNewValue());
    // assertEquals("semanticTypes", atomicActions.get(15).getField());
    // assertEquals("CONCEPT", atomicActions.get(16).getIdType().toString());
    // assertNull(atomicActions.get(16).getOldValue());
    // assertNotNull(atomicActions.get(16).getNewValue());
    // assertEquals("relationships", atomicActions.get(16).getField());
    // assertEquals("CONCEPT", atomicActions.get(17).getIdType().toString());
    // assertNull(atomicActions.get(17).getOldValue());
    // assertNotNull(atomicActions.get(17).getNewValue());
    // assertEquals("relationships", atomicActions.get(17).getField());

    // Verify the log entries exists
    String logEntry = projectService.getLog(project.getId(),
        originatingC.getId(), null, 1, authToken);
    assertTrue(logEntry.contains("SPLIT from concept " + originatingC.getId()
        + " into concept " + createdC.getId()));
    logEntry = projectService.getLog(project.getId(), createdC.getId(), null, 1,
        authToken);
    assertTrue(logEntry.contains("SPLIT into concept " + createdC.getId()
        + " from concept " + originatingC.getId()));

    //
    // Run the test again from scratch, but this time with not splitting out
    // Semantic Types and Relationships
    //

    teardown();
    setup();

    //
    // Prepare the test and check prerequisites
    //
    // Due to MySQL rounding to the second, we must also round our comparison
    // startDate.
    startDate = DateUtils.round(new Date(), Calendar.SECOND);

    // Populate concept components
    populateConcepts();

    // get the fromConcept, toConcept, and relatedConcept
    originatingC =
        contentService.getConcept(concept.getId(), project.getId(), authToken);
    assertNotNull(originatingC);
    relatedC =
        contentService.getConcept(concept3.getId(), project.getId(), authToken);
    assertNotNull(relatedC);

    // Get the atoms so we can extract the IDs
    atom = null;
    atom2 = null;
    for (Atom a : originatingC.getAtoms()) {
      if (a.getName().equals("DCB")) {
        atom = a;
      }
      if (a.getName().equals("IPA")) {
        atom2 = a;
      }
    }
    assertNotNull(atom);
    assertNotNull(atom2);

    // Create a list of the atoms we'll be splitting out into the new concept
    moveAtomIds = new ArrayList<Long>();
    moveAtomIds.add(atom2.getId());

    // Split the atoms out into a new concept,and give the concept an RN
    // relation to the new one.
    // Do NOT transfer over semantic types or relationships
    v = metaEditingService.splitConcept(project.getId(), originatingC.getId(),
        "activityId", originatingC.getLastModified().getTime(), moveAtomIds,
        false, false, false, "RN", authToken);
    assertTrue(v.getErrors().isEmpty());

    // Identify the newly created concept by finding the most recently modified
    // concept that includes one of the moved atoms
    pfs = new PfsParameterJpa();
    pfs = new PfsParameterJpa();
    pfs.setSortField("lastModified");
    pfs.setAscending(false);
    pfs.setMaxResults(1);
    pfs.setStartIndex(0);

    createdC = contentService.getConcept(contentService
        .findConcepts(umlsTerminology, umlsVersion,
            "atoms.id:" + moveAtomIds.get(0), pfs, authToken)
        .getObjects().get(0).getId(), project.getId(), authToken);

    // Set this to concept4, so it can be handled in cleanup.
    concept4 = createdC;

    originatingC =
        contentService.getConcept(concept.getId(), project.getId(), authToken);
    relatedC =
        contentService.getConcept(concept3.getId(), project.getId(), authToken);

    // Verify split atom is now present in created Concept, and not present in
    // originating Concept
    assertTrue(createdC.getAtoms().contains(atom2));
    assertTrue(!originatingC.getAtoms().contains(atom2));

    // Verify non-split atom is still present in originating Concept, and is not
    // present in created Concept
    assertTrue(originatingC.getAtoms().contains(atom));
    assertTrue(!createdC.getAtoms().contains(atom));

    // Verify originating Concept Semantic type is NOT present in created
    // Concept
    styCount = 0;
    for (SemanticTypeComponent sty : createdC.getSemanticTypes()) {
      if (sty.getSemanticType().equals("Lipid")) {
        styCount++;
      }
    }
    assertEquals(0, styCount);

    // Verify that the originating and created Concepts have a relationship
    // between them of the specified type
    originatingConceptRels =
        contentService.findConceptRelationships(originatingC.getTerminologyId(),
            originatingC.getTerminology(), originatingC.getVersion(),
            "fromId:" + originatingC.getId(), null, authToken);

    relationshipPresent = false;
    for (final Relationship<?, ?> rel : originatingConceptRels.getObjects()) {
      if (rel.getFrom().getId().equals(originatingC.getId())
          && rel.getTo().getId().equals(createdC.getId())
          && rel.getRelationshipType().equals("RN")) {
        relationshipPresent = true;
      }
    }
    assertTrue(relationshipPresent);

    // Verify that the same is true for the inverse
    createdConceptRels = contentService.findConceptRelationships(
        createdC.getTerminologyId(), createdC.getTerminology(),
        createdC.getVersion(), null, null, authToken);

    relationshipPresent = false;
    for (final Relationship<?, ?> rel : createdConceptRels.getObjects()) {
      if (rel.getFrom().getId().equals(createdC.getId())
          && rel.getTo().getId().equals(originatingC.getId())
          && rel.getRelationshipType().equals("RB")) {
        relationshipPresent = true;
      }
    }
    assertTrue(relationshipPresent);

    // Verify that relationships from originatingConcept have NOT been added to
    // createdConcept
    relationshipPresent = false;
    for (final Relationship<?, ?> rel : createdConceptRels.getObjects()) {
      if (rel.getFrom().getId().equals(createdC.getId())
          && rel.getTo().getId().equals(relatedC.getId())
          && rel.getRelationshipType().equals("RN")) {
        relationshipPresent = true;
      }
    }
    assertTrue(!relationshipPresent);

    // Verify that the same is true for the inverse
    relatedConceptRels = contentService.findConceptRelationships(
        relatedC.getTerminologyId(), relatedC.getTerminology(),
        relatedC.getVersion(), "fromId:" + relatedC.getId(), null, authToken);

    relationshipPresent = false;
    for (final Relationship<?, ?> rel : relatedConceptRels.getObjects()) {
      if (rel.getFrom().getId().equals(relatedC.getId())
          && rel.getTo().getId().equals(createdC.getId())
          && rel.getRelationshipType().equals("RB")) {
        relationshipPresent = true;
      }
    }
    assertTrue(!relationshipPresent);

    // verify the molecular action exists
    pfs = new PfsParameterJpa();
    pfs.setSortField("lastModified");
    pfs.setAscending(false);
    list = projectService.findMolecularActions(originatingC.getId(),
        umlsTerminology, umlsVersion, null, pfs, authToken);
    assertTrue(list.size() > 0);
    ma = list.getObjects().get(0);
    assertNotNull(ma);
    assertEquals(originatingC.getId(), ma.getComponentId());
    assertTrue(ma.getLastModified().compareTo(startDate) >= 0);
    assertNotNull(ma.getAtomicActions());

    // Verify that atomic actions exists for splitting off atoms, creating new
    // Concept
    // adding Semantic Types, and for adding Relationships
    // 1 for removing Atom from originatingConcept
    // 1 for creating createdConcept
    // 2 for creating Relationships for createdConcept and originatingConcept
    // 1 for adding relationship to originatingConcept
    // 1 for updating TerminologyId on the createdConcept
    // 1 for adding Atom to createdConcept
    // 1 for adding Relationships to createdConcept

    pfs.setSortField(null);

    atomicActions = projectService
        .findAtomicActions(ma.getId(), null, pfs, authToken).getObjects();
    Collections.sort(atomicActions,
        (a1, a2) -> a1.getId().compareTo(a2.getId()));
    assertEquals(8, atomicActions.size());
    // TODO - fix this list, or rip it out.
    // assertEquals("CONCEPT", atomicActions.get(0).getIdType().toString());
    // assertNotNull(atomicActions.get(0).getOldValue());
    // assertNull(atomicActions.get(0).getNewValue());
    // assertEquals("atoms", atomicActions.get(0).getField());
    // assertEquals("CONCEPT", atomicActions.get(1).getIdType().toString());
    // assertNull(atomicActions.get(1).getOldValue());
    // assertNotNull(atomicActions.get(1).getNewValue());
    // assertEquals("RELATIONSHIP",
    // atomicActions.get(2).getIdType().toString());
    // assertNull(atomicActions.get(2).getOldValue());
    // assertNotNull(atomicActions.get(2).getNewValue());
    // assertEquals("RELATIONSHIP",
    // atomicActions.get(3).getIdType().toString());
    // assertNull(atomicActions.get(3).getOldValue());
    // assertNotNull(atomicActions.get(3).getNewValue());
    // assertEquals("CONCEPT", atomicActions.get(4).getIdType().toString());
    // assertNull(atomicActions.get(4).getOldValue());
    // assertNotNull(atomicActions.get(4).getNewValue());
    // assertEquals("relationships", atomicActions.get(4).getField());
    // assertEquals("CONCEPT", atomicActions.get(5).getIdType().toString());
    // assertEquals("", atomicActions.get(5).getOldValue());
    // assertNotNull(atomicActions.get(5).getNewValue());
    // assertEquals("terminologyId", atomicActions.get(5).getField());
    // assertEquals("CONCEPT", atomicActions.get(6).getIdType().toString());
    // assertNull(atomicActions.get(6).getOldValue());
    // assertNotNull(atomicActions.get(6).getNewValue());
    // assertEquals("atoms", atomicActions.get(6).getField());
    // assertEquals("CONCEPT", atomicActions.get(7).getIdType().toString());
    // assertNull(atomicActions.get(7).getOldValue());
    // assertNotNull(atomicActions.get(7).getNewValue());
    // assertEquals("relationships", atomicActions.get(7).getField());

    // Verify the log entries exists
    logEntry = projectService.getLog(project.getId(), originatingC.getId(),
        null, 1, authToken);
    assertTrue(logEntry.contains("SPLIT from concept " + originatingC.getId()
        + " into concept " + createdC.getId()));
    logEntry = projectService.getLog(project.getId(), createdC.getId(), null, 1,
        authToken);
    assertTrue(logEntry.contains("SPLIT into concept " + createdC.getId()
        + " from concept " + originatingC.getId()));

  }

  /**
   * Test approve concept.
   *
   * @throws Exception the exception
   */
  @Test
  public void testApproveConcept() throws Exception {
    Logger.getLogger(getClass()).debug("Start test " + name.getMethodName());

    Logger.getLogger(getClass()).info("TEST - Approve concept CONCEPTID, "
        + umlsTerminology + ", " + umlsVersion + authToken);

    //
    // Prepare the test and check prerequisites
    //
    // Due to MySQL rounding to the second, we must also round our comparison
    // startDate.
    Date startDate = DateUtils.round(new Date(), Calendar.SECOND);

    // Populate concept components
    populateConcepts();

    // Create a DEMOTION between concept and concept2, and its inverse
    Atom fromAtom = concept.getAtoms().get(0);
    Atom toAtom = concept2.getAtoms().get(0);

    AtomRelationship demotion = new AtomRelationshipJpa();
    demotion.setFrom(fromAtom);
    demotion.setTo(toAtom);
    demotion.setTerminology(umlsTerminology);
    demotion.setTerminologyId("");
    demotion.setBranch(Branch.ROOT);
    demotion.setName("Test Demotion");
    demotion.setVersion(umlsVersion);
    demotion.setRelationshipType("RO");
    demotion.setWorkflowStatus(WorkflowStatus.DEMOTION);
    demotion =
        testService.addRelationship((AtomRelationshipJpa) demotion, authToken);

    AtomRelationship inverseDemotion = new AtomRelationshipJpa();
    inverseDemotion.setFrom(toAtom);
    inverseDemotion.setTo(fromAtom);
    inverseDemotion.setTerminology(umlsTerminology);
    inverseDemotion.setTerminologyId("");
    inverseDemotion.setBranch(Branch.ROOT);
    inverseDemotion.setName("Test Demotion");
    inverseDemotion.setVersion(umlsVersion);
    inverseDemotion.setRelationshipType("RO");
    inverseDemotion.setWorkflowStatus(WorkflowStatus.DEMOTION);
    inverseDemotion = testService
        .addRelationship((AtomRelationshipJpa) inverseDemotion, authToken);

    final Long demotionRelationshipId = demotion.getId();
    final Long inverseDemotionRelationshipId = inverseDemotion.getId();

    // Add demotions to atoms and update
    fromAtom.getRelationships().add(demotion);
    toAtom.getRelationships().add(inverseDemotion);

    testService.updateAtom((AtomJpa) fromAtom, authToken);
    testService.updateAtom((AtomJpa) toAtom, authToken);

    fromAtom = testService.getAtom(fromAtom.getId(), authToken);
    toAtom = testService.getAtom(toAtom.getId(), authToken);

    // Make sure the demotions are there
    boolean demotionPresent = false;
    for (AtomRelationship atomRel : fromAtom.getRelationships()) {
      if (atomRel.getId().equals(demotionRelationshipId)) {
        demotionPresent = true;
        break;
      }
    }
    assertTrue(demotionPresent);

    boolean inverseDemotionPresent = false;
    for (AtomRelationship atomRel : toAtom.getRelationships()) {
      if (atomRel.getId().equals(inverseDemotionRelationshipId)) {
        inverseDemotionPresent = true;
        break;
      }
    }
    assertTrue(inverseDemotionPresent);

    // get the concept
    Concept c =
        contentService.getConcept(concept.getId(), project.getId(), authToken);
    assertNotNull(c);

    // Now that the concepts are all set up, approve it.
    ValidationResult v =
        metaEditingService.approveConcept(project.getId(), c.getId(),
            "activityId", c.getLastModified().getTime(), false, authToken);
    assertTrue(v.getErrors().isEmpty());

    c = contentService.getConcept(concept.getId(), project.getId(), authToken);
    fromAtom = testService.getAtom(fromAtom.getId(), authToken);
    toAtom = testService.getAtom(toAtom.getId(), authToken);

    // Verify the DEMOTION relationship and its inverse have been deleted
    demotionPresent = false;
    for (AtomRelationship atomRel : fromAtom.getRelationships()) {
      if (atomRel.getId().equals(demotionRelationshipId)) {
        demotionPresent = true;
      }
    }
    assertFalse(demotionPresent);

    inverseDemotionPresent = false;
    for (AtomRelationship atomRel : toAtom.getRelationships()) {
      if (atomRel.getId().equals(inverseDemotionRelationshipId)) {
        inverseDemotionPresent = true;
      }
    }
    assertFalse(inverseDemotionPresent);

    // Verify concept now has a workflow status of "READY_FOR_PUBLICATION"
    assertEquals(WorkflowStatus.READY_FOR_PUBLICATION, c.getWorkflowStatus());

    // Verify the concept has lastApproved and lastApprovedBy correctly
    // populated
    assertEquals("E-" + adminUser, c.getLastApprovedBy());
    assertNotNull(c.getLastApproved());
    assertTrue(c.getLastApproved().compareTo(startDate) >= 0);

    // Verify that all of the concept's atoms have a status of
    // "READY_FOR_PUBLICATION"
    boolean allAtomsReadyForPub = true;
    for (Atom atm : c.getAtoms()) {
      if (!atm.getWorkflowStatus()
          .equals(WorkflowStatus.READY_FOR_PUBLICATION)) {
        allAtomsReadyForPub = false;
      }
    }
    assertTrue(allAtomsReadyForPub);

    // Verify that all of the concept's semantic types have a status of
    // "READY_FOR_PUBLICATION"
    boolean allStyReadyForPub = true;
    for (SemanticTypeComponent sty : c.getSemanticTypes()) {
      if (!sty.getWorkflowStatus()
          .equals(WorkflowStatus.READY_FOR_PUBLICATION)) {
        allStyReadyForPub = false;
      }
    }
    assertTrue(allStyReadyForPub);

    // Verify that all of the concept's relationships and inverses have a status
    // of "READY_FOR_PUBLICATION", and a RelationshipType of RO, RB, RN, or XR
    RelationshipList relList =
        contentService.findConceptRelationships(c.getTerminologyId(),
            c.getTerminology(), c.getVersion(), null, null, authToken);

    final List<String> typeList = Arrays.asList("RO", "RB", "RN", "XR");

    boolean allRelsReadyForPub = true;
    boolean allRelsCorrectType = true;
    boolean allInverseRelsReadyForPub = true;
    boolean allInverseRelsCorrectType = true;
    for (final Relationship<?, ?> rel : relList.getObjects()) {
      if (!rel.getWorkflowStatus()
          .equals(WorkflowStatus.READY_FOR_PUBLICATION)) {
        allRelsReadyForPub = false;
      }
      if (!typeList.contains(rel.getRelationshipType())) {
        allRelsCorrectType = false;
      }

      // Check its inverse also
      String inverseRelType = "";
      switch (rel.getRelationshipType()) {
        case "RN":
          inverseRelType = "RB";
          break;
        case "RB":
          inverseRelType = "RN";
          break;
        case "RO":
          inverseRelType = "RO";
          break;
        case "XR":
          inverseRelType = "XR";
          break;
        default:
          throw new Exception(
              "Unexpedted relationship type: " + rel.getRelationshipType());
      }

      // This will return the single inverse relationship
      RelationshipList inverseRelList =
          contentService
              .findConceptRelationships(rel.getTo().getTerminologyId(),
                  rel.getTo().getTerminology(),
                  rel.getTo().getVersion(), "toId:" + rel.getFrom().getId()
                      + " AND relationshipType:" + inverseRelType,
                  null, authToken);
      if (!inverseRelList.getObjects().get(0).getWorkflowStatus()
          .equals(WorkflowStatus.READY_FOR_PUBLICATION)) {
        allInverseRelsReadyForPub = false;
      }
      if (!typeList
          .contains(inverseRelList.getObjects().get(0).getRelationshipType())) {
        allInverseRelsCorrectType = false;
      }
    }
    assertTrue(allRelsReadyForPub);
    assertTrue(allRelsCorrectType);
    assertTrue(allInverseRelsReadyForPub);
    assertTrue(allInverseRelsCorrectType);

    // verify the molecular action exists
    PfsParameterJpa pfs = new PfsParameterJpa();
    pfs.setSortField("lastModified");
    pfs.setAscending(false);
    MolecularActionList list = projectService.findMolecularActions(c.getId(),
        umlsTerminology, umlsVersion, null, pfs, authToken);
    assertTrue(list.size() > 0);
    MolecularAction ma = list.getObjects().get(0);
    assertNotNull(ma);
    assertEquals(c.getId(), ma.getComponentId());
    assertTrue(ma.getLastModified().compareTo(startDate) >= 0);
    assertNotNull(ma.getAtomicActions());

    // Verify that atomic actions exists for updating atoms, semantic types,
    // relationships, and concept
    // 2 for removing DEMOTION Relationship from Atom and inverse from
    // related Atom
    // 2 for deleting DEMOTION relationship and inverse
    // 2 for updating Atom
    // 1 for updating Semantic Type
    // 4 for updating Relationship and inverse
    // 1 for updating Concept
    pfs.setSortField(null);

    List<AtomicAction> atomicActions = projectService
        .findAtomicActions(ma.getId(), null, pfs, authToken).getObjects();
    Collections.sort(atomicActions,
        (a1, a2) -> a1.getId().compareTo(a2.getId()));
    assertEquals(12, atomicActions.size());
    assertEquals("ATOM", atomicActions.get(0).getIdType().toString());
    assertNotNull(atomicActions.get(0).getOldValue());
    assertNull(atomicActions.get(0).getNewValue());
    assertEquals("relationships", atomicActions.get(1).getField());
    assertEquals("ATOM", atomicActions.get(0).getIdType().toString());
    assertNotNull(atomicActions.get(1).getOldValue());
    assertNull(atomicActions.get(1).getNewValue());
    assertEquals("relationships", atomicActions.get(1).getField());
    assertEquals("RELATIONSHIP", atomicActions.get(2).getIdType().toString());
    assertNotNull(atomicActions.get(2).getOldValue());
    assertNull(atomicActions.get(2).getNewValue());
    assertEquals("RELATIONSHIP", atomicActions.get(3).getIdType().toString());
    assertNotNull(atomicActions.get(3).getOldValue());
    assertNull(atomicActions.get(3).getNewValue());
    assertEquals("ATOM", atomicActions.get(4).getIdType().toString());
    assertNotNull(atomicActions.get(4).getOldValue());
    assertNotNull(atomicActions.get(4).getNewValue());
    assertEquals("ATOM", atomicActions.get(5).getIdType().toString());
    assertNotNull(atomicActions.get(5).getOldValue());
    assertNotNull(atomicActions.get(5).getNewValue());
    assertEquals("SEMANTIC_TYPE", atomicActions.get(6).getIdType().toString());
    assertNotNull(atomicActions.get(6).getOldValue());
    assertNotNull(atomicActions.get(6).getNewValue());
    assertEquals("RELATIONSHIP", atomicActions.get(7).getIdType().toString());
    assertNotNull(atomicActions.get(7).getOldValue());
    assertNotNull(atomicActions.get(7).getNewValue());
    assertEquals("RELATIONSHIP", atomicActions.get(8).getIdType().toString());
    assertNotNull(atomicActions.get(8).getOldValue());
    assertNotNull(atomicActions.get(8).getNewValue());
    assertEquals("RELATIONSHIP", atomicActions.get(9).getIdType().toString());
    assertNotNull(atomicActions.get(9).getOldValue());
    assertNotNull(atomicActions.get(9).getNewValue());
    assertEquals("RELATIONSHIP", atomicActions.get(10).getIdType().toString());
    assertNotNull(atomicActions.get(10).getOldValue());
    assertNotNull(atomicActions.get(10).getNewValue());
    assertEquals("CONCEPT", atomicActions.get(11).getIdType().toString());
    assertNotNull(atomicActions.get(11).getOldValue());
    assertNotNull(atomicActions.get(11).getNewValue());

    // Verify the log entry exists
    String logEntry =
        projectService.getLog(project.getId(), c.getId(), null, 1, authToken);
    assertTrue(logEntry.contains("APPROVE concept " + c.getId()));

  }

  /**
   * Test undo add and remove atom to concept.
   *
   * @throws Exception the exception
   */
  @Test
  public void testUndoAndRedoAddAndRemoveAtomToConcept() throws Exception {
    Logger.getLogger(getClass()).debug("Start test " + name.getMethodName());

    Logger.getLogger(getClass())
        .info("TEST - Perform and undo adding and removing an atom, "
            + umlsTerminology + ", " + umlsVersion + authToken);

    //
    // Prepare the test and check prerequisites
    //
    // Due to MySQL rounding to the second, we must also round our comparison
    // startDate.
    Date startDate = DateUtils.round(new Date(), Calendar.SECOND);

    // get the concept
    Concept c =
        contentService.getConcept(concept.getId(), project.getId(), authToken);
    assertNotNull(c);

    //
    // Create and add an atom to concept
    //
    AtomJpa atom = new AtomJpa();
    atom.setBranch(Branch.ROOT);
    atom.setName("DCB");
    atom.setTerminologyId("TestId");
    atom.setTerminology(umlsTerminology);
    atom.setVersion(umlsVersion);
    atom.setTimestamp(new Date());
    atom.setPublishable(true);
    atom.setCodeId("C44314");
    atom.setConceptId("M0023181");
    atom.getConceptTerminologyIds().put(c.getTerminology(),
        c.getTerminologyId());
    atom.setDescriptorId("");
    atom.setLanguage("ENG");
    atom.setTermType("AB");
    atom.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);

    //
    // Add the Atom
    //
    ValidationResult v = metaEditingService.addAtom(project.getId(), c.getId(),
        "activityId", c.getLastModified().getTime(), atom, false, authToken);
    assertTrue(v.getErrors().isEmpty());
    c = contentService.getConcept(c.getId(), project.getId(), authToken);

    // Save a copy of the added atom
    Atom addedAtom = null;
    for (Atom a : c.getAtoms()) {
      if (a.getName().equals("DCB")) {
        addedAtom = a;
      }
    }
    assertNotNull(addedAtom);

    Long addedAtomId = addedAtom.getId();

    // Get the add atom molecular action

    // verify the molecular action exists
    PfsParameterJpa pfs = new PfsParameterJpa();
    pfs.setSortField("lastModified");
    pfs.setAscending(false);
    MolecularActionList list = projectService.findMolecularActions(c.getId(),
        umlsTerminology, umlsVersion, null, pfs, authToken);
    assertTrue(list.size() > 0);
    MolecularAction ma = list.getObjects().get(0);
    assertNotNull(ma);
    assertEquals(c.getId(), ma.getComponentId());
    assertEquals(null, ma.getComponentId2());
    assertTrue(ma.getLastModified().compareTo(startDate) >= 0);
    assertNotNull(ma.getAtomicActions());

    // Save the molecular action lastModified to compare against later
    Date modDate = ma.getLastModified();

    // Test Undo Add Action

    v = metaEditingService.undoAction(project.getId(), ma.getId(), "activityId",
        false, authToken);
    assertTrue(v.getErrors().isEmpty());

    c = contentService.getConcept(c.getId(), project.getId(), authToken);
    ma = projectService.findMolecularActions(c.getId(), umlsTerminology,
        umlsVersion, null, pfs, authToken).getObjects().get(0);

    // Verify the molecular action undone flag is set, and the lastModified has
    // been updated
    assertEquals(true, ma.isUndoneFlag());
    assertTrue(ma.getLastModified().compareTo(modDate) >= 0);

    // Verify the atom was removed from concept
    boolean atomRemoved = true;
    for (Atom a : c.getAtoms()) {
      if (a.getId().equals(addedAtomId)) {
        atomRemoved = false;
      }
    }
    assertTrue(atomRemoved);

    // Verify the atom was deleted entirely
    assertNull(testService.getAtom(addedAtomId, authToken));

    // Verify the concept workflow status was set back to previous state
    assertEquals(WorkflowStatus.READY_FOR_PUBLICATION, c.getWorkflowStatus());

    // Verify the log entry exists
    String logEntry =
        projectService.getLog(project.getId(), c.getId(), null, 1, authToken);
    assertTrue(logEntry.contains("UNDO " + ma.getName() + ", " + ma.getId()));

    // Test Redo Add Action

    v = metaEditingService.redoAction(project.getId(), ma.getId(), "activityId",
        false, authToken);
    assertTrue(v.getErrors().isEmpty());

    c = contentService.getConcept(c.getId(), project.getId(), authToken);
    ma = projectService.findMolecularActions(c.getId(), umlsTerminology,
        umlsVersion, null, pfs, authToken).getObjects().get(0);

    // Verify the molecular action undone flag is reset, and the lastModified
    // has
    // been updated
    assertEquals(false, ma.isUndoneFlag());
    assertTrue(ma.getLastModified().compareTo(modDate) >= 0);

    // Verify the atom was undeleted
    assertNotNull(testService.getAtom(addedAtomId, authToken));

    // Verify the atom was re-added to concept
    boolean atomPresent = false;
    for (Atom a : c.getAtoms()) {
      if (a.getId().equals(addedAtomId)) {
        atomPresent = true;
      }
    }
    assertTrue(atomPresent);

    // Verify the concept workflow status was set back to its state after action
    assertEquals(WorkflowStatus.NEEDS_REVIEW, c.getWorkflowStatus());

    // Verify the log entry exists
    logEntry =
        projectService.getLog(project.getId(), c.getId(), null, 1, authToken);
    assertTrue(logEntry.contains("REDO " + ma.getName() + ", " + ma.getId()));

    //
    // Remove the Atom
    //
    v = metaEditingService.removeAtom(project.getId(), c.getId(), "activityId",
        c.getLastModified().getTime(), addedAtomId, false, authToken);
    assertTrue(v.getErrors().isEmpty());
    c = contentService.getConcept(c.getId(), project.getId(), authToken);

    // Get the remove atom molecular action

    // verify the molecular action exists
    pfs = new PfsParameterJpa();
    pfs.setSortField("lastModified");
    pfs.setAscending(false);
    list = projectService.findMolecularActions(c.getId(), umlsTerminology,
        umlsVersion, null, pfs, authToken);
    assertTrue(list.size() > 0);
    ma = list.getObjects().get(0);
    assertNotNull(ma);
    assertEquals(c.getId(), ma.getComponentId());
    assertEquals(null, ma.getComponentId2());
    assertTrue(ma.getLastModified().compareTo(startDate) >= 0);
    assertNotNull(ma.getAtomicActions());

    // Save the molecular action lastModified to compare against later
    modDate = ma.getLastModified();

    // Test Undo Remove Action

    v = metaEditingService.undoAction(project.getId(), ma.getId(), "activityId",
        false, authToken);
    assertTrue(v.getErrors().isEmpty());

    c = contentService.getConcept(c.getId(), project.getId(), authToken);
    ma = projectService.findMolecularActions(c.getId(), umlsTerminology,
        umlsVersion, null, pfs, authToken).getObjects().get(0);

    // Verify the molecular action undone flag is set, and the lastModified has
    // been updated
    assertEquals(true, ma.isUndoneFlag());
    assertTrue(ma.getLastModified().compareTo(modDate) >= 0);

    // Verify the atom was re-created
    assertNotNull(testService.getAtom(addedAtomId, authToken));

    // Verify the atom was re-added back to concept
    boolean atomReadded = false;
    for (Atom a : c.getAtoms()) {
      if (a.getId().equals(addedAtomId)) {
        atomReadded = true;
      }
    }
    assertTrue(atomReadded);

    // Verify the log entry exists
    logEntry =
        projectService.getLog(project.getId(), c.getId(), null, 1, authToken);
    assertTrue(logEntry.contains("UNDO " + ma.getName() + ", " + ma.getId()));

    // Test Redo Remove Action

    v = metaEditingService.redoAction(project.getId(), ma.getId(), "activityId",
        false, authToken);
    assertTrue(v.getErrors().isEmpty());

    c = contentService.getConcept(c.getId(), project.getId(), authToken);
    ma = projectService.findMolecularActions(c.getId(), umlsTerminology,
        umlsVersion, null, pfs, authToken).getObjects().get(0);

    // Verify the molecular action undone flag is reset, and the lastModified
    // has
    // been updated
    assertEquals(false, ma.isUndoneFlag());
    assertTrue(ma.getLastModified().compareTo(modDate) >= 0);

    // Verify the atom was re-removed from concept
    boolean atomReRemoved = true;
    for (Atom a : c.getAtoms()) {
      if (a.getId().equals(addedAtomId)) {
        atomReRemoved = false;
      }
    }
    assertTrue(atomReRemoved);

    // Verify the atom was re-deleted
    assertNull(testService.getAtom(addedAtomId, authToken));

    // Verify the log entry exists
    logEntry =
        projectService.getLog(project.getId(), c.getId(), null, 1, authToken);
    assertTrue(logEntry.contains("REDO " + ma.getName() + ", " + ma.getId()));
  }

  /**
   * Test undo and redo add and remove attribute to concept.
   *
   * @throws Exception the exception
   */
  @Test
  public void testUndoAndRedoAddAndRemoveAttributeToConcept() throws Exception {
    Logger.getLogger(getClass()).debug("Start test " + name.getMethodName());

    Logger.getLogger(getClass())
        .info("TEST - Perform and undo adding and removing an attribute, "
            + umlsTerminology + ", " + umlsVersion + authToken);

    //
    // Prepare the test and check prerequisites
    //
    // Due to MySQL rounding to the second, we must also round our comparison
    // startDate.
    Date startDate = DateUtils.round(new Date(), Calendar.SECOND);

    // get the concept
    Concept c =
        contentService.getConcept(concept.getId(), project.getId(), authToken);
    assertNotNull(c);

    //
    // Create and add an attribute to concept, and then undo
    //

    AttributeJpa attribute = new AttributeJpa();
    attribute.setBranch(Branch.ROOT);
    attribute.setName("UMLSRELA");
    attribute.setValue("VALUE");
    attribute.setTerminologyId("TestId");
    attribute.setTerminology(umlsTerminology);
    attribute.setVersion(umlsVersion);
    attribute.setTimestamp(new Date());
    attribute.setPublishable(true);

    //
    // Add the attribute
    //
    ValidationResult v = metaEditingService.addAttribute(project.getId(),
        c.getId(), "activityId", c.getLastModified().getTime(), attribute,
        false, authToken);
    assertTrue(v.getErrors().isEmpty());
    c = contentService.getConcept(c.getId(), project.getId(), authToken);

    // Save a copy of the added atom
    Attribute addedAttribute = null;
    for (Attribute atr : c.getAttributes()) {
      if (attribute.getName().equals("UMLSRELA")) {
        addedAttribute = atr;
      }
    }
    assertNotNull(addedAttribute);

    Long addedAttributeId = addedAttribute.getId();

    // Get the add semantic Type molecular action

    // verify the molecular action exists
    PfsParameterJpa pfs = new PfsParameterJpa();
    pfs.setSortField("lastModified");
    pfs.setAscending(false);
    MolecularActionList list = projectService.findMolecularActions(c.getId(),
        umlsTerminology, umlsVersion, null, pfs, authToken);
    assertTrue(list.size() > 0);
    MolecularAction ma = list.getObjects().get(0);
    assertNotNull(ma);
    assertEquals(c.getId(), ma.getComponentId());
    assertEquals(null, ma.getComponentId2());
    assertTrue(ma.getLastModified().compareTo(startDate) >= 0);
    assertNotNull(ma.getAtomicActions());

    // Save the molecular action lastModified to compare against later
    Date modDate = ma.getLastModified();

    // Test Undo Add Action

    v = metaEditingService.undoAction(project.getId(), ma.getId(), "activityId",
        false, authToken);
    assertTrue(v.getErrors().isEmpty());

    c = contentService.getConcept(c.getId(), project.getId(), authToken);
    ma = projectService.findMolecularActions(c.getId(), umlsTerminology,
        umlsVersion, null, pfs, authToken).getObjects().get(0);

    // Verify the molecular action undone flag is set, and the lastModified has
    // been updated
    assertEquals(true, ma.isUndoneFlag());
    assertTrue(ma.getLastModified().compareTo(modDate) >= 0);

    // Verify the attribute was removed from concept
    boolean attributeRemoved = true;
    for (Attribute attr : c.getAttributes()) {
      if (attr.getId().equals(addedAttributeId)) {
        attributeRemoved = false;
      }
    }
    assertTrue(attributeRemoved);

    // Verify the attribute was deleted entirely
    assertNull(testService.getAttribute(addedAttributeId, authToken));

    // Verify the concept workflow status was set back to previous state
    assertEquals(WorkflowStatus.READY_FOR_PUBLICATION, c.getWorkflowStatus());

    // Verify the log entry exists
    String logEntry =
        projectService.getLog(project.getId(), c.getId(), null, 1, authToken);
    assertTrue(logEntry.contains("UNDO " + ma.getName() + ", " + ma.getId()));

    // Test Redo Add Action

    v = metaEditingService.redoAction(project.getId(), ma.getId(), "activityId",
        false, authToken);
    assertTrue(v.getErrors().isEmpty());

    c = contentService.getConcept(c.getId(), project.getId(), authToken);
    ma = projectService.findMolecularActions(c.getId(), umlsTerminology,
        umlsVersion, null, pfs, authToken).getObjects().get(0);

    // Verify the molecular action undone flag is reset, and the lastModified
    // has
    // been updated
    assertEquals(false, ma.isUndoneFlag());
    assertTrue(ma.getLastModified().compareTo(modDate) >= 0);

    // Verify the attribute was undeleted
    assertNotNull(testService.getAttribute(addedAttributeId, authToken));

    // Verify the attribute was re-added to concept
    boolean attributePresent = false;
    for (Attribute a : c.getAttributes()) {
      if (a.getId().equals(addedAttributeId)) {
        attributePresent = true;
      }
    }
    assertTrue(attributePresent);

    // Verify the concept workflow status was set back to its state after action
    assertEquals(WorkflowStatus.NEEDS_REVIEW, c.getWorkflowStatus());

    // Verify the log entry exists
    logEntry =
        projectService.getLog(project.getId(), c.getId(), null, 1, authToken);
    assertTrue(logEntry.contains("REDO " + ma.getName() + ", " + ma.getId()));

    //
    // Remove the Atom
    //
    v = metaEditingService.removeAttribute(project.getId(), c.getId(),
        "activityId", c.getLastModified().getTime(), addedAttributeId, false,
        authToken);
    assertTrue(v.getErrors().isEmpty());
    c = contentService.getConcept(c.getId(), project.getId(), authToken);

    // Get the remove attribute molecular action

    // verify the molecular action exists
    pfs = new PfsParameterJpa();
    pfs.setSortField("lastModified");
    pfs.setAscending(false);
    list = projectService.findMolecularActions(c.getId(), umlsTerminology,
        umlsVersion, null, pfs, authToken);
    assertTrue(list.size() > 0);
    ma = list.getObjects().get(0);
    assertNotNull(ma);
    assertEquals(c.getId(), ma.getComponentId());
    assertEquals(null, ma.getComponentId2());
    assertTrue(ma.getLastModified().compareTo(startDate) >= 0);
    assertNotNull(ma.getAtomicActions());

    // Save the molecular action lastModified to compare against later
    modDate = ma.getLastModified();

    // Test Undo Remove Action

    v = metaEditingService.undoAction(project.getId(), ma.getId(), "activityId",
        false, authToken);
    assertTrue(v.getErrors().isEmpty());

    c = contentService.getConcept(c.getId(), project.getId(), authToken);
    ma = projectService.findMolecularActions(c.getId(), umlsTerminology,
        umlsVersion, null, pfs, authToken).getObjects().get(0);

    // Verify the molecular action undone flag is set, and the lastModified has
    // been updated
    assertEquals(true, ma.isUndoneFlag());
    assertTrue(ma.getLastModified().compareTo(modDate) >= 0);

    // Verify the attribute was re-created
    assertNotNull(testService.getAttribute(addedAttributeId, authToken));

    // Verify the attribute was re-added back to concept
    boolean attributeReadded = false;
    for (Attribute a : c.getAttributes()) {
      if (a.getId().equals(addedAttributeId)) {
        attributeReadded = true;
      }
    }
    assertTrue(attributeReadded);

    // Verify the log entry exists
    logEntry =
        projectService.getLog(project.getId(), c.getId(), null, 1, authToken);
    assertTrue(logEntry.contains("UNDO " + ma.getName() + ", " + ma.getId()));

    // Test Redo Remove Action

    v = metaEditingService.redoAction(project.getId(), ma.getId(), "activityId",
        false, authToken);
    assertTrue(v.getErrors().isEmpty());

    c = contentService.getConcept(c.getId(), project.getId(), authToken);
    ma = projectService.findMolecularActions(c.getId(), umlsTerminology,
        umlsVersion, null, pfs, authToken).getObjects().get(0);

    // Verify the molecular action undone flag is reset, and the lastModified
    // has
    // been updated
    assertEquals(false, ma.isUndoneFlag());
    assertTrue(ma.getLastModified().compareTo(modDate) >= 0);

    // Verify the attribute was re-removed from concept
    boolean attributeReRemoved = true;
    for (Attribute a : c.getAttributes()) {
      if (a.getId().equals(addedAttributeId)) {
        attributeReRemoved = false;
      }
    }
    assertTrue(attributeReRemoved);

    // Verify the attribute was re-deleted
    assertNull(testService.getAttribute(addedAttributeId, authToken));

    // Verify the log entry exists
    logEntry =
        projectService.getLog(project.getId(), c.getId(), null, 1, authToken);
    assertTrue(logEntry.contains("REDO " + ma.getName() + ", " + ma.getId()));
  }

  /**
   * Test undo and redo add and remove semantic type to concept.
   *
   * @throws Exception the exception
   */
  @Test
  public void testUndoAndRedoAddAndRemoveSemanticTypeToConcept()
    throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

    Logger.getLogger(getClass()).info(
        "TEST - Perform and undo adding and removing a semantic type component, "
            + umlsTerminology + ", " + umlsVersion + authToken);

    //
    // Prepare the test and check prerequisites
    //
    // Due to MySQL rounding to the second, we must also round our comparison
    // startDate.
    Date startDate = DateUtils.round(new Date(), Calendar.SECOND);

    // get the concept
    Concept c =
        contentService.getConcept(concept.getId(), project.getId(), authToken);
    assertNotNull(c);

    //
    // Create and add a semantic type to concept, and then undo
    //
    /*
     * SemanticTypeComponentJpa semanticType = new SemanticTypeComponentJpa();
     * semanticType.setBranch(Branch.ROOT);
     * semanticType.setSemanticType("Lipid");
     * semanticType.setTerminologyId("TestId");
     * semanticType.setTerminology(umlsTerminology);
     * semanticType.setVersion(umlsVersion); semanticType.setTimestamp(new
     * Date()); semanticType.setPublishable(true);
     */
    String semanticType = "Lipid";

    //
    // Add the Semantic Type
    //
    ValidationResult v = metaEditingService.addSemanticType(project.getId(),
        c.getId(), "activityId", c.getLastModified().getTime(), semanticType,
        false, authToken);
    assertTrue(v.getErrors().isEmpty());
    c = contentService.getConcept(c.getId(), project.getId(), authToken);

    // Save a copy of the added semantic type component
    SemanticTypeComponent addedSty = null;
    for (SemanticTypeComponent sty : c.getSemanticTypes()) {
      if (sty.getSemanticType().equals("Lipid")) {
        addedSty = sty;
      }
    }
    assertNotNull(addedSty);

    Long addedSemanticTypeId = addedSty.getId();

    // Get the add semantic Type molecular action

    // verify the molecular action exists
    PfsParameterJpa pfs = new PfsParameterJpa();
    pfs.setSortField("lastModified");
    pfs.setAscending(false);
    MolecularActionList list = projectService.findMolecularActions(c.getId(),
        umlsTerminology, umlsVersion, null, pfs, authToken);
    assertTrue(list.size() > 0);
    MolecularAction ma = list.getObjects().get(0);
    assertNotNull(ma);
    assertEquals(c.getId(), ma.getComponentId());
    assertEquals(null, ma.getComponentId2());
    assertTrue(ma.getLastModified().compareTo(startDate) >= 0);
    assertNotNull(ma.getAtomicActions());

    // Save the molecular action lastModified to compare against later
    Date modDate = ma.getLastModified();

    // Test Undo Add Action

    v = metaEditingService.undoAction(project.getId(), ma.getId(), "activityId",
        false, authToken);
    assertTrue(v.getErrors().isEmpty());

    c = contentService.getConcept(c.getId(), project.getId(), authToken);
    ma = projectService.findMolecularActions(c.getId(), umlsTerminology,
        umlsVersion, null, pfs, authToken).getObjects().get(0);

    // Verify the molecular action undone flag is set, and the lastModified has
    // been updated
    assertEquals(true, ma.isUndoneFlag());
    assertTrue(ma.getLastModified().compareTo(modDate) >= 0);

    // Verify the semanticType was removed from concept
    boolean semanticTypeRemoved = true;
    for (SemanticTypeComponent sty : c.getSemanticTypes()) {
      if (sty.getId().equals(addedSemanticTypeId)) {
        semanticTypeRemoved = false;
      }
    }
    assertTrue(semanticTypeRemoved);

    // Verify the semantic type component was deleted entirely
    assertNull(
        testService.getSemanticTypeComponent(addedSemanticTypeId, authToken));

    // Verify the concept workflow status was set back to previous state
    assertEquals(WorkflowStatus.READY_FOR_PUBLICATION, c.getWorkflowStatus());

    // Verify the log entry exists
    String logEntry = projectService.getLog(project.getId(),
        ma.getComponentId(), null, 1, authToken);
    assertTrue(logEntry.contains("UNDO " + ma.getName() + ", " + ma.getId()));

    // Test Redo Add Action

    v = metaEditingService.redoAction(project.getId(), ma.getId(), "activityId",
        false, authToken);
    assertTrue(v.getErrors().isEmpty());

    c = contentService.getConcept(c.getId(), project.getId(), authToken);
    ma = projectService.findMolecularActions(c.getId(), umlsTerminology,
        umlsVersion, null, pfs, authToken).getObjects().get(0);

    // Verify the molecular action undone flag is reset, and the lastModified
    // has
    // been updated
    assertEquals(false, ma.isUndoneFlag());
    assertTrue(ma.getLastModified().compareTo(modDate) >= 0);

    // Verify the semanticType was undeleted
    assertNotNull(
        testService.getSemanticTypeComponent(addedSemanticTypeId, authToken));

    // Verify the semanticType was re-added to concept
    boolean semanticTypePresent = false;
    for (SemanticTypeComponent sty : c.getSemanticTypes()) {
      if (sty.getId().equals(addedSemanticTypeId)) {
        semanticTypePresent = true;
      }
    }
    assertTrue(semanticTypePresent);

    // Verify the concept workflow status was set back to its state after action
    assertEquals(WorkflowStatus.NEEDS_REVIEW, c.getWorkflowStatus());

    // Verify the log entry exists
    logEntry = projectService.getLog(project.getId(), ma.getComponentId(), null,
        1, authToken);
    assertTrue(logEntry.contains("REDO " + ma.getName() + ", " + ma.getId()));

    //
    // Remove the SemanticType
    //
    v = metaEditingService.removeSemanticType(project.getId(), c.getId(),
        "activityId", c.getLastModified().getTime(), addedSemanticTypeId, false,
        authToken);
    assertTrue(v.getErrors().isEmpty());
    c = contentService.getConcept(c.getId(), project.getId(), authToken);

    // Get the remove semanticType molecular action

    // verify the molecular action exists
    pfs = new PfsParameterJpa();
    pfs.setSortField("lastModified");
    pfs.setAscending(false);
    list = projectService.findMolecularActions(c.getId(), umlsTerminology,
        umlsVersion, null, pfs, authToken);
    assertTrue(list.size() > 0);
    ma = list.getObjects().get(0);
    assertNotNull(ma);
    assertEquals(c.getId(), ma.getComponentId());
    assertEquals(null, ma.getComponentId2());
    assertTrue(ma.getLastModified().compareTo(startDate) >= 0);
    assertNotNull(ma.getAtomicActions());

    // Save the molecular action lastModified to compare against later
    modDate = ma.getLastModified();

    // Test Undo Remove Action

    v = metaEditingService.undoAction(project.getId(), ma.getId(), "activityId",
        false, authToken);
    assertTrue(v.getErrors().isEmpty());

    c = contentService.getConcept(c.getId(), project.getId(), authToken);
    ma = projectService.findMolecularActions(c.getId(), umlsTerminology,
        umlsVersion, null, pfs, authToken).getObjects().get(0);

    // Verify the molecular action undone flag is set, and the lastModified has
    // been updated
    assertEquals(true, ma.isUndoneFlag());
    assertTrue(ma.getLastModified().compareTo(modDate) >= 0);

    // Verify the semanticType was re-created
    assertNotNull(
        testService.getSemanticTypeComponent(addedSemanticTypeId, authToken));

    // Verify the semanticType was re-added back to concept
    boolean semanticTypeReadded = false;
    for (SemanticTypeComponent sty : c.getSemanticTypes()) {
      if (sty.getId().equals(addedSemanticTypeId)) {
        semanticTypeReadded = true;
      }
    }
    assertTrue(semanticTypeReadded);

    // Verify the log entry exists
    logEntry = projectService.getLog(project.getId(), ma.getComponentId(), null,
        1, authToken);
    assertTrue(logEntry.contains("UNDO " + ma.getName() + ", " + ma.getId()));

    // Test Redo Remove Action

    v = metaEditingService.redoAction(project.getId(), ma.getId(), "activityId",
        false, authToken);
    assertTrue(v.getErrors().isEmpty());

    c = contentService.getConcept(c.getId(), project.getId(), authToken);
    ma = projectService.findMolecularActions(c.getId(), umlsTerminology,
        umlsVersion, null, pfs, authToken).getObjects().get(0);

    // Verify the molecular action undone flag is reset, and the lastModified
    // has
    // been updated
    assertEquals(false, ma.isUndoneFlag());
    assertTrue(ma.getLastModified().compareTo(modDate) >= 0);

    // Verify the semanticType was re-removed from concept
    boolean semanticTypeReRemoved = true;
    for (SemanticTypeComponent sty : c.getSemanticTypes()) {
      if (sty.getId().equals(addedSemanticTypeId)) {
        semanticTypeReRemoved = false;
      }
    }
    assertTrue(semanticTypeReRemoved);

    // Verify the semanticType was re-deleted
    assertNull(
        testService.getSemanticTypeComponent(addedSemanticTypeId, authToken));

    // Verify the log entry exists
    logEntry = projectService.getLog(project.getId(), ma.getComponentId(), null,
        1, authToken);
    assertTrue(logEntry.contains("REDO " + ma.getName() + ", " + ma.getId()));
  }

  /**
   * Test undo and redo add and remove relationship to concept.
   *
   * @throws Exception the exception
   */
  @Test
  public void testUndoAndRedoAddAndRemoveRelationshipToConcept()
    throws Exception {
    Logger.getLogger(getClass()).debug("Start test " + name.getMethodName());

    Logger.getLogger(getClass())
        .info("TEST - Perform and undo adding and removing relationship, "
            + umlsTerminology + ", " + umlsVersion + authToken);

    //
    // Prepare the test and check prerequisites
    //
    // Due to MySQL rounding to the second, we must also round our comparison
    // startDate.
    Date startDate = DateUtils.round(new Date(), Calendar.SECOND);

    //
    // Create and add a relationship to concepts, and then undo
    //
    Concept c =
        contentService.getConcept(concept.getId(), project.getId(), authToken);
    Concept c2 =
        contentService.getConcept(concept2.getId(), project.getId(), authToken);

    ConceptRelationshipJpa relationship = new ConceptRelationshipJpa();
    relationship.setBranch(Branch.ROOT);
    relationship.setRelationshipType("RN");
    relationship.setAdditionalRelationshipType("");
    relationship.setFrom(c);
    relationship.setTo(c2);
    relationship.setTerminologyId("TestId");
    relationship.setTerminology(umlsTerminology);
    relationship.setVersion(umlsVersion);
    relationship.setTimestamp(new Date());
    relationship.setPublishable(true);
    relationship.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);

    //
    // Add relationship
    //

    ValidationResult v = metaEditingService.addRelationship(project.getId(),
        c.getId(), "activityId", c.getLastModified().getTime(), relationship,
        false, authToken);
    assertTrue(v.getErrors().isEmpty());
    c = contentService.getConcept(c.getId(), project.getId(), authToken);
    c2 = contentService.getConcept(c2.getId(), project.getId(), authToken);

    // Save a copy of the added relationship
    Relationship<?, ?> addedRel = null;

    RelationshipList relList =
        contentService.findConceptRelationships(c.getTerminologyId(),
            c.getTerminology(), c.getVersion(), null, null, authToken);

    for (final Relationship<?, ?> rel : relList.getObjects()) {
      if (rel.getRelationshipType().equals("RN")
          && rel.getTo().getId().equals(c2.getId())) {
        addedRel = rel;
      }
    }
    assertNotNull(addedRel);

    Long addedRelId = addedRel.getId();

    // Save a copy of the added inverse relationship
    Relationship<?, ?> inverseAddedRel = null;

    relList = contentService.findConceptRelationships(c2.getTerminologyId(),
        c2.getTerminology(), c2.getVersion(), null, null, authToken);

    for (final Relationship<?, ?> rel : relList.getObjects()) {
      if (rel.getRelationshipType().equals("RB")
          && rel.getTo().getId().equals(c.getId())) {
        inverseAddedRel = rel;
      }
    }
    assertNotNull(inverseAddedRel);

    Long inverseAddedRelId = inverseAddedRel.getId();

    // Get the add relationship molecular action

    // verify the molecular action exists
    PfsParameterJpa pfs = new PfsParameterJpa();
    pfs.setSortField("lastModified");
    pfs.setAscending(false);
    MolecularActionList list = projectService.findMolecularActions(c.getId(),
        umlsTerminology, umlsVersion, null, pfs, authToken);
    assertTrue(list.size() > 0);
    MolecularAction ma = list.getObjects().get(0);
    assertNotNull(ma);
    assertEquals(c.getId(), ma.getComponentId());
    assertEquals(c2.getId(), ma.getComponentId2());
    assertTrue(ma.getLastModified().compareTo(startDate) >= 0);
    assertNotNull(ma.getAtomicActions());

    // Save the molecular action lastModified to compare against later
    Date modDate = ma.getLastModified();

    // Test Undo Add Action

    v = metaEditingService.undoAction(project.getId(), ma.getId(), "activityId",
        false, authToken);
    assertTrue(v.getErrors().isEmpty());

    c = contentService.getConcept(c.getId(), project.getId(), authToken);
    c2 = contentService.getConcept(c2.getId(), project.getId(), authToken);
    ma = projectService.findMolecularActions(c.getId(), umlsTerminology,
        umlsVersion, null, pfs, authToken).getObjects().get(0);

    // Verify the molecular action undone flag is set, and the lastModified has
    // been updated
    assertEquals(true, ma.isUndoneFlag());
    assertTrue(ma.getLastModified().compareTo(modDate) >= 0);

    // Verify the relationship was removed from concept
    relList = contentService.findConceptRelationships(c.getTerminologyId(),
        c.getTerminology(), c.getVersion(), null, null, authToken);

    boolean relRemoved = true;
    for (Relationship<? extends ComponentInfo, ? extends ComponentInfo> rel : relList
        .getObjects()) {
      if (rel.getId().equals(addedRelId)) {
        relRemoved = false;
      }
    }
    assertTrue(relRemoved);

    // Verify the inverse relationship was removed from the toConcept
    relList = contentService.findConceptRelationships(c2.getTerminologyId(),
        c2.getTerminology(), c2.getVersion(), null, null, authToken);

    boolean inverseRelRemoved = true;
    for (Relationship<? extends ComponentInfo, ? extends ComponentInfo> rel : relList
        .getObjects()) {
      if (rel.getId().equals(inverseAddedRelId)) {
        inverseRelRemoved = false;
      }
    }
    assertTrue(inverseRelRemoved);

    // Verify the relationship and inverse were deleted entirely
    assertNull(testService.getConceptRelationship(addedRelId, authToken));
    assertNull(
        testService.getConceptRelationship(inverseAddedRelId, authToken));

    // Verify the concept workflow status was set back to previous state
    assertEquals(WorkflowStatus.READY_FOR_PUBLICATION, c.getWorkflowStatus());

    // Verify the log entry exists
    String logEntry =
        projectService.getLog(project.getId(), c.getId(), null, 1, authToken);
    assertTrue(logEntry.contains("UNDO " + ma.getName() + ", " + ma.getId()));

    // Test Redo Add Action

    v = metaEditingService.redoAction(project.getId(), ma.getId(), "activityId",
        false, authToken);
    assertTrue(v.getErrors().isEmpty());

    c = contentService.getConcept(c.getId(), project.getId(), authToken);
    c2 = contentService.getConcept(c2.getId(), project.getId(), authToken);
    ma = projectService.findMolecularActions(c.getId(), umlsTerminology,
        umlsVersion, null, pfs, authToken).getObjects().get(0);

    // Verify the molecular action undone flag is set, and the lastModified has
    // been updated
    assertEquals(false, ma.isUndoneFlag());
    assertTrue(ma.getLastModified().compareTo(modDate) >= 0);

    // Verify the relationship and inverse were re-added
    assertNotNull(testService.getConceptRelationship(addedRelId, authToken));
    assertNotNull(
        testService.getConceptRelationship(inverseAddedRelId, authToken));

    // Verify the relationship was readded to concept
    relList = contentService.findConceptRelationships(c.getTerminologyId(),
        c.getTerminology(), c.getVersion(), null, null, authToken);
    boolean relReadded = false;
    for (Relationship<? extends ComponentInfo, ? extends ComponentInfo> rel : relList
        .getObjects()) {
      if (rel.getId().equals(addedRelId)) {
        relReadded = true;
      }
    }
    assertTrue(relReadded);

    // Verify the inverse relationship was readded to the toConcept
    relList = contentService.findConceptRelationships(c2.getTerminologyId(),
        c2.getTerminology(), c2.getVersion(), null, null, authToken);

    boolean inverseRelReadded = false;
    for (Relationship<? extends ComponentInfo, ? extends ComponentInfo> rel : relList
        .getObjects()) {
      if (rel.getId().equals(inverseAddedRelId)) {
        inverseRelReadded = true;
      }
    }
    assertTrue(inverseRelReadded);

    // Verify the concept workflow status was set back to previous state
    assertEquals(WorkflowStatus.NEEDS_REVIEW, c.getWorkflowStatus());

    // Verify the log entry exists
    logEntry =
        projectService.getLog(project.getId(), c.getId(), null, 1, authToken);
    assertTrue(logEntry.contains("REDO " + ma.getName() + ", " + ma.getId()));

    //
    // Remove relationship
    //

    v = metaEditingService.removeRelationship(project.getId(), c.getId(),
        "activityId", c.getLastModified().getTime(), addedRelId, false,
        authToken);
    assertTrue(v.getErrors().isEmpty());
    c = contentService.getConcept(c.getId(), project.getId(), authToken);
    c2 = contentService.getConcept(c2.getId(), project.getId(), authToken);

    // Get the remove relationship molecular action

    // verify the molecular action exists
    pfs = new PfsParameterJpa();
    pfs.setSortField("lastModified");
    pfs.setAscending(false);
    list = projectService.findMolecularActions(c.getId(), umlsTerminology,
        umlsVersion, null, pfs, authToken);
    assertTrue(list.size() > 0);
    ma = list.getObjects().get(0);
    assertNotNull(ma);
    assertEquals(c.getId(), ma.getComponentId());
    assertEquals(c2.getId(), ma.getComponentId2());
    assertTrue(ma.getLastModified().compareTo(startDate) >= 0);
    assertNotNull(ma.getAtomicActions());

    // Save the molecular action lastModified to compare against later
    modDate = ma.getLastModified();

    // Test Undo Remove Action

    v = metaEditingService.undoAction(project.getId(), ma.getId(), "activityId",
        false, authToken);
    assertTrue(v.getErrors().isEmpty());

    c = contentService.getConcept(c.getId(), project.getId(), authToken);
    c2 = contentService.getConcept(c2.getId(), project.getId(), authToken);
    ma = projectService.findMolecularActions(c.getId(), umlsTerminology,
        umlsVersion, null, pfs, authToken).getObjects().get(0);

    // Verify the molecular action undone flag is set, and the lastModified has
    // been updated
    assertEquals(true, ma.isUndoneFlag());
    assertTrue(ma.getLastModified().compareTo(modDate) >= 0);

    // Verify the relationship and inverse were re-created
    assertNotNull(testService.getConceptRelationship(addedRelId, authToken));
    assertNotNull(
        testService.getConceptRelationship(inverseAddedRelId, authToken));

    // Verify the relationship was readded to concept
    relList = contentService.findConceptRelationships(c.getTerminologyId(),
        c.getTerminology(), c.getVersion(), null, null, authToken);
    relReadded = false;
    for (Relationship<? extends ComponentInfo, ? extends ComponentInfo> rel : relList
        .getObjects()) {
      if (rel.getId().equals(addedRelId)) {
        relReadded = true;
      }
    }
    assertTrue(relReadded);

    // Verify the inverse relationship was readded to the toConcept
    relList = contentService.findConceptRelationships(c2.getTerminologyId(),
        c2.getTerminology(), c2.getVersion(), null, null, authToken);

    inverseRelReadded = false;
    for (Relationship<? extends ComponentInfo, ? extends ComponentInfo> rel : relList
        .getObjects()) {
      if (rel.getId().equals(inverseAddedRelId)) {
        inverseRelReadded = true;
      }
    }
    assertTrue(inverseRelReadded);

    // Verify the log entry exists
    logEntry =
        projectService.getLog(project.getId(), c.getId(), null, 1, authToken);
    assertTrue(logEntry.contains("UNDO " + ma.getName() + ", " + ma.getId()));

    // Test Redo Remove Action

    v = metaEditingService.redoAction(project.getId(), ma.getId(), "activityId",
        false, authToken);
    assertTrue(v.getErrors().isEmpty());

    c = contentService.getConcept(c.getId(), project.getId(), authToken);
    c2 = contentService.getConcept(c2.getId(), project.getId(), authToken);
    ma = projectService.findMolecularActions(c.getId(), umlsTerminology,
        umlsVersion, null, pfs, authToken).getObjects().get(0);

    // Verify the molecular action undone flag is set, and the lastModified has
    // been updated
    assertEquals(false, ma.isUndoneFlag());
    assertTrue(ma.getLastModified().compareTo(modDate) >= 0);

    // Verify the relationship was removed from concept
    relList = contentService.findConceptRelationships(c.getTerminologyId(),
        c.getTerminology(), c.getVersion(), null, null, authToken);
    relRemoved = true;
    for (Relationship<? extends ComponentInfo, ? extends ComponentInfo> rel : relList
        .getObjects()) {
      if (rel.getId().equals(addedRelId)) {
        relRemoved = false;
      }
    }
    assertTrue(relRemoved);

    // Verify the inverse relationship was removed from the toConcept
    relList = contentService.findConceptRelationships(c2.getTerminologyId(),
        c2.getTerminology(), c2.getVersion(), null, null, authToken);

    inverseRelRemoved = true;
    for (Relationship<? extends ComponentInfo, ? extends ComponentInfo> rel : relList
        .getObjects()) {
      if (rel.getId().equals(inverseAddedRelId)) {
        inverseRelRemoved = false;
      }
    }
    assertTrue(inverseRelRemoved);

    // Verify the relationship and inverse relationship was deleted entirely
    assertNull(testService.getConceptRelationship(addedRelId, authToken));
    assertNull(
        testService.getConceptRelationship(inverseAddedRelId, authToken));

    // Verify the log entry exists
    logEntry =
        projectService.getLog(project.getId(), c.getId(), null, 1, authToken);
    assertTrue(logEntry.contains("REDO " + ma.getName() + ", " + ma.getId()));

  }

  /**
   * Test undo and redo move atoms.
   *
   * @throws Exception the exception
   */
  @Test
  public void testUndoAndRedoMoveAtoms() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

    Logger.getLogger(getClass()).info("TEST - Perform and undo moving atoms, "
        + umlsTerminology + ", " + umlsVersion + authToken);

    //
    // Prepare the test and check prerequisites
    //
    // Due to MySQL rounding to the second, we must also round our comparison
    // startDate.
    Date startDate = DateUtils.round(new Date(), Calendar.SECOND);

    // Populate concept components
    populateConcepts();

    // Create a DEMOTION between concept and concept2, and its inverse
    Atom fromAtom = concept.getAtoms().get(0);
    Atom toAtom = concept2.getAtoms().get(0);

    AtomRelationship demotion = new AtomRelationshipJpa();
    demotion.setFrom(fromAtom);
    demotion.setTo(toAtom);
    demotion.setTerminology(umlsTerminology);
    demotion.setTerminologyId("");
    demotion.setBranch(Branch.ROOT);
    demotion.setName("Test Demotion");
    demotion.setVersion(umlsVersion);
    demotion.setRelationshipType("RO");
    demotion.setWorkflowStatus(WorkflowStatus.DEMOTION);
    demotion =
        testService.addRelationship((AtomRelationshipJpa) demotion, authToken);

    AtomRelationship inverseDemotion = new AtomRelationshipJpa();
    inverseDemotion.setFrom(toAtom);
    inverseDemotion.setTo(fromAtom);
    inverseDemotion.setTerminology(umlsTerminology);
    inverseDemotion.setTerminologyId("");
    inverseDemotion.setBranch(Branch.ROOT);
    inverseDemotion.setName("Test Demotion");
    inverseDemotion.setVersion(umlsVersion);
    inverseDemotion.setRelationshipType("RO");
    inverseDemotion.setWorkflowStatus(WorkflowStatus.DEMOTION);
    inverseDemotion = testService
        .addRelationship((AtomRelationshipJpa) inverseDemotion, authToken);

    final Long demotionRelationshipId = demotion.getId();
    final Long inverseDemotionRelationshipId = inverseDemotion.getId();

    // Add demotions to atoms and update
    fromAtom.getRelationships().add(demotion);
    toAtom.getRelationships().add(inverseDemotion);

    testService.updateAtom((AtomJpa) fromAtom, authToken);
    testService.updateAtom((AtomJpa) toAtom, authToken);

    fromAtom = testService.getAtom(fromAtom.getId(), authToken);
    toAtom = testService.getAtom(toAtom.getId(), authToken);

    // Make sure the demotions are there
    boolean demotionPresent = false;
    for (AtomRelationship atomRel : fromAtom.getRelationships()) {
      if (atomRel.getId().equals(demotionRelationshipId)) {
        demotionPresent = true;
        break;
      }
    }
    assertTrue(demotionPresent);

    boolean inverseDemotionPresent = false;
    for (AtomRelationship atomRel : toAtom.getRelationships()) {
      if (atomRel.getId().equals(inverseDemotionRelationshipId)) {
        inverseDemotionPresent = true;
        break;
      }
    }
    assertTrue(inverseDemotionPresent);

    // get the fromConcept and the toConcept
    Concept fromC =
        contentService.getConcept(concept.getId(), project.getId(), authToken);
    assertNotNull(fromC);
    Concept toC =
        contentService.getConcept(concept2.getId(), project.getId(), authToken);
    assertNotNull(toC);

    // Get the atoms so we can extract the IDs
    Atom atom1 = null;
    Atom atom2 = null;
    Atom atom3 = null;
    Atom atom4 = null;
    for (Atom a : fromC.getAtoms()) {
      if (a.getName().equals("DCB")) {
        atom1 = a;
      }
      if (a.getName().equals("IPA")) {
        atom2 = a;
      }
    }
    for (Atom a : toC.getAtoms()) {
      if (a.getName().equals("17 Oxosteroids")) {
        atom3 = a;
      }
      if (a.getName().equals("PABA")) {
        atom4 = a;
      }
    }
    assertNotNull(atom1);
    assertNotNull(atom2);
    assertNotNull(atom3);
    assertNotNull(atom4);

    List<Long> moveAtomIds = new ArrayList<Long>();
    moveAtomIds.add(atom1.getId());
    moveAtomIds.add(atom2.getId());

    // Move all of the atoms fromConcept to toConcept.
    ValidationResult v = metaEditingService.moveAtoms(project.getId(),
        fromC.getId(), "activityId", fromC.getLastModified().getTime(),
        toC.getId(), moveAtomIds, false, authToken);
    assertTrue(v.getErrors().isEmpty());

    fromC =
        contentService.getConcept(fromC.getId(), project.getId(), authToken);
    toC = contentService.getConcept(toC.getId(), project.getId(), authToken);
    fromAtom = testService.getAtom(fromAtom.getId(), authToken);
    toAtom = testService.getAtom(toAtom.getId(), authToken);

    // Get the move molecular action

    // verify the molecular action exists
    PfsParameterJpa pfs = new PfsParameterJpa();
    pfs.setSortField("lastModified");
    pfs.setAscending(false);
    MolecularActionList list = projectService.findMolecularActions(
        fromC.getId(), umlsTerminology, umlsVersion, null, pfs, authToken);
    assertTrue(list.size() > 0);
    MolecularAction ma = list.getObjects().get(0);
    assertNotNull(ma);
    assertEquals(fromC.getId(), ma.getComponentId());
    assertEquals(toC.getId(), ma.getComponentId2());
    assertTrue(ma.getLastModified().compareTo(startDate) >= 0);
    assertNotNull(ma.getAtomicActions());

    // Save the molecular action lastModified to compare against later
    Date modDate = ma.getLastModified();

    // Test Undo Move Action

    v = metaEditingService.undoAction(project.getId(), ma.getId(), "activityId",
        false, authToken);
    assertTrue(v.getErrors().isEmpty());

    fromC =
        contentService.getConcept(fromC.getId(), project.getId(), authToken);
    toC = contentService.getConcept(toC.getId(), project.getId(), authToken);
    fromAtom = testService.getAtom(fromAtom.getId(), authToken);
    toAtom = testService.getAtom(toAtom.getId(), authToken);
    ma = projectService.findMolecularActions(fromC.getId(), umlsTerminology,
        umlsVersion, null, pfs, authToken).getObjects().get(0);

    // Verify the molecular action undone flag is set, and the lastModified has
    // been updated
    assertEquals(true, ma.isUndoneFlag());
    assertTrue(ma.getLastModified().compareTo(modDate) >= 0);

    // Verify the moved atoms are back in the fromConcept.
    int atomCount = 0;
    for (Atom a : fromC.getAtoms()) {
      if (moveAtomIds.contains(a.getId())) {
        atomCount++;
      }
    }
    assertEquals(2, atomCount);

    // Verify the moved atoms are no longer in the toConcept.
    atomCount = 0;
    for (Atom a : toC.getAtoms()) {
      if (moveAtomIds.contains(a.getId())) {
        atomCount++;
      }
    }
    assertEquals(0, atomCount);

    // Verify the demotions are have been restored
    demotionPresent = false;
    for (AtomRelationship atomRel : fromAtom.getRelationships()) {
      if (atomRel.getId().equals(demotionRelationshipId)) {
        demotionPresent = true;
        break;
      }
    }
    assertTrue(demotionPresent);

    inverseDemotionPresent = false;
    for (AtomRelationship atomRel : toAtom.getRelationships()) {
      if (atomRel.getId().equals(inverseDemotionRelationshipId)) {
        inverseDemotionPresent = true;
        break;
      }
    }
    assertTrue(inverseDemotionPresent);    
    
    // Verify the log entry exists
    String logEntry = projectService.getLog(project.getId(), fromC.getId(),
        null, 1, authToken);
    assertTrue(logEntry.contains("UNDO " + ma.getName() + ", " + ma.getId()));

    // Test Redo Move Action

    v = metaEditingService.redoAction(project.getId(), ma.getId(), "activityId",
        false, authToken);
    assertTrue(v.getErrors().isEmpty());

    fromC =
        contentService.getConcept(fromC.getId(), project.getId(), authToken);
    toC = contentService.getConcept(toC.getId(), project.getId(), authToken);
    fromAtom = testService.getAtom(fromAtom.getId(), authToken);
    toAtom = testService.getAtom(toAtom.getId(), authToken);
    ma = projectService.findMolecularActions(fromC.getId(), umlsTerminology,
        umlsVersion, null, pfs, authToken).getObjects().get(0);

    // Verify the molecular action undone flag is set, and the lastModified has
    // been updated
    assertEquals(false, ma.isUndoneFlag());
    assertTrue(ma.getLastModified().compareTo(modDate) >= 0);

    // Verify the moved atoms are no longer in the fromConcept.
    atomCount = 0;
    for (Atom a : fromC.getAtoms()) {
      if (moveAtomIds.contains(a.getId())) {
        atomCount++;
      }
    }
    assertEquals(0, atomCount);

    // Verify the moved atoms are in the toConcept.
    atomCount = 0;
    for (Atom a : toC.getAtoms()) {
      if (moveAtomIds.contains(a.getId())) {
        atomCount++;
      }
    }
    assertEquals(2, atomCount);

    // Verify the DEMOTION relationship and its inverse have been re-deleted
    demotionPresent = false;
    for (AtomRelationship atomRel : fromAtom.getRelationships()) {
      if (atomRel.getId().equals(demotionRelationshipId)) {
        demotionPresent = true;
      }
    }
    assertFalse(demotionPresent);

    inverseDemotionPresent = false;
    for (AtomRelationship atomRel : toAtom.getRelationships()) {
      if (atomRel.getId().equals(inverseDemotionRelationshipId)) {
        inverseDemotionPresent = true;
      }
    }
    assertFalse(inverseDemotionPresent);        
    
    // Verify the log entry exists
    logEntry = projectService.getLog(project.getId(), fromC.getId(), null, 1,
        authToken);
    assertTrue(logEntry.contains("REDO " + ma.getName() + ", " + ma.getId()));
  }

  /**
   * Test undo and redo split concept.
   *
   * @throws Exception the exception
   */
  @Test
  public void testUndoAndRedoSplitConcept() throws Exception {
    Logger.getLogger(getClass()).debug("Start test " + name.getMethodName());

    Logger.getLogger(getClass())
        .info("TEST - Perform and undo splitting a concept, " + umlsTerminology
            + ", " + umlsVersion + authToken);

    //
    // Split a concept into another, and then undo
    //

    // Due to MySQL rounding to the second, we must also round our comparison
    // startDate.
    Date startDate = DateUtils.round(new Date(), Calendar.SECOND);

    // Populate concept components
    populateConcepts();

    // get the fromConcept, toConcept, and relatedConcept
    Concept originatingC =
        contentService.getConcept(concept.getId(), project.getId(), authToken);
    assertNotNull(originatingC);
    Concept relatedC =
        contentService.getConcept(concept3.getId(), project.getId(), authToken);
    assertNotNull(relatedC);

    // Get the atoms so we can extract the IDs
    Atom atom1 = null;
    Atom atom2 = null;
    for (Atom a : originatingC.getAtoms()) {
      if (a.getName().equals("DCB")) {
        atom1 = a;
      }
      if (a.getName().equals("IPA")) {
        atom2 = a;
      }
    }
    assertNotNull(atom1);
    assertNotNull(atom2);

    // Create a list of the atoms we'll be splitting out into the new concept
    List<Long> moveAtomIds = new ArrayList<Long>();
    moveAtomIds.add(atom1.getId());

    // Split the atoms out into a new concept,and give the concept an RN
    // relation to the new one.
    // Transfer over semantic types or relationships
    ValidationResult v =
        metaEditingService.splitConcept(project.getId(), originatingC.getId(),
            "activityId", originatingC.getLastModified().getTime(), moveAtomIds,
            false, true, true, "RN", authToken);
    assertTrue(v.getErrors().isEmpty());

    PfsParameterJpa pfs = new PfsParameterJpa();
    pfs.setSortField("lastModified");
    pfs.setAscending(false);

    originatingC = contentService.getConcept(originatingC.getId(),
        project.getId(), authToken);
    // Need to look up the created concept, since its ID was just assigned.
    Concept createdC = contentService.getConcept(contentService
        .findConcepts(umlsTerminology, umlsVersion,
            "atoms.id:" + moveAtomIds.get(0), pfs, authToken)
        .getObjects().get(0).getId(), project.getId(), authToken);
    relatedC =
        contentService.getConcept(concept3.getId(), project.getId(), authToken);

    Long createdCId = createdC.getId();

    RelationshipList createdConceptRels =
        contentService.findConceptRelationships(createdC.getTerminologyId(),
            createdC.getTerminology(), createdC.getVersion(), null, null,
            authToken);

    List<Long> createdRelationshipIds = new ArrayList<Long>();
    for (Relationship<? extends ComponentInfo, ? extends ComponentInfo> rel : createdConceptRels
        .getObjects()) {
      createdRelationshipIds.add(rel.getId());
    }

    List<Long> createdStyIds = new ArrayList<Long>();
    for (SemanticTypeComponent sty : createdC.getSemanticTypes()) {
      createdStyIds.add(sty.getId());
    }

    // Get the split molecular action

    // verify the molecular action exists
    MolecularActionList list =
        projectService.findMolecularActions(originatingC.getId(),
            umlsTerminology, umlsVersion, null, pfs, authToken);
    assertTrue(list.size() > 0);
    MolecularAction ma = list.getObjects().get(0);
    assertNotNull(ma);
    assertEquals(originatingC.getId(), ma.getComponentId());
    assertEquals(createdC.getId(), ma.getComponentId2());
    assertTrue(ma.getLastModified().compareTo(startDate) >= 0);
    assertNotNull(ma.getAtomicActions());

    // Save the molecular action lastModified to compare against later
    Date modDate = ma.getLastModified();

    //
    // Undo the split action
    //

    v = metaEditingService.undoAction(project.getId(), ma.getId(), "activityId",
        false, authToken);
    assertTrue(v.getErrors().isEmpty());

    originatingC = contentService.getConcept(originatingC.getId(),
        project.getId(), authToken);
    createdC =
        contentService.getConcept(createdCId, project.getId(), authToken);
    relatedC =
        contentService.getConcept(relatedC.getId(), project.getId(), authToken);
    ma = projectService.findMolecularActions(originatingC.getId(),
        umlsTerminology, umlsVersion, null, pfs, authToken).getObjects().get(0);

    // Verify the molecular action undone flag is set, and the lastModified has
    // been updated
    assertEquals(true, ma.isUndoneFlag());
    assertTrue(ma.getLastModified().compareTo(modDate) >= 0);

    // Verify the moved atom is back in the originatingConcept.
    int atomCount = 0;
    for (Atom a : originatingC.getAtoms()) {
      if (moveAtomIds.contains(a.getId())) {
        atomCount++;
      }
    }
    assertEquals(1, atomCount);

    // Verify all of the newly created semantic types and relationships are
    // deleted
    for (Long relId : createdRelationshipIds) {
      assertNull(testService.getConceptRelationship(relId, authToken));
    }
    for (Long styId : createdStyIds) {
      assertNull(testService.getSemanticTypeComponent(styId, authToken));
    }

    // Verify the created concept has been deleted
    assertNull(createdC);

    // Verify the log entry exists
    String logEntry = projectService.getLog(project.getId(),
        originatingC.getId(), null, 1, authToken);
    assertTrue(logEntry.contains("UNDO " + ma.getName() + ", " + ma.getId()));

    //
    // Redo the split action
    //

    v = metaEditingService.redoAction(project.getId(), ma.getId(), "activityId",
        false, authToken);
    assertTrue(v.getErrors().isEmpty());

    originatingC = contentService.getConcept(originatingC.getId(),
        project.getId(), authToken);
    createdC =
        contentService.getConcept(createdCId, project.getId(), authToken);
    relatedC =
        contentService.getConcept(relatedC.getId(), project.getId(), authToken);
    ma = projectService.findMolecularActions(originatingC.getId(),
        umlsTerminology, umlsVersion, null, pfs, authToken).getObjects().get(0);

    // Verify the molecular action undone flag is set, and the lastModified has
    // been updated
    assertEquals(false, ma.isUndoneFlag());
    assertTrue(ma.getLastModified().compareTo(modDate) >= 0);

    // Verify the created concept exists again
    assertNotNull(createdC);

    // Verify split atom is now back in created Concept, and not present in
    // originating Concept
    assertTrue(createdC.getAtoms().contains(atom1));
    assertTrue(!originatingC.getAtoms().contains(atom1));

    // Verify all of the semantic types and relationships are re-created
    for (Long relId : createdRelationshipIds) {
      assertNotNull(testService.getConceptRelationship(relId, authToken));
    }
    for (Long styId : createdStyIds) {
      assertNotNull(testService.getSemanticTypeComponent(styId, authToken));
    }

    // Verify originating Concept Semantic type is now back in created
    // Concept
    int styCount = 0;
    for (SemanticTypeComponent sty : createdC.getSemanticTypes()) {
      if (createdStyIds.contains(sty.getId())) {
        styCount++;
      }
    }
    assertEquals(1, styCount);

    // Verify that the originating and created Concepts have a relationship
    // between them of the specified type again
    RelationshipList originatingConceptRels =
        contentService.findConceptRelationships(originatingC.getTerminologyId(),
            originatingC.getTerminology(), originatingC.getVersion(),
            "fromId:" + originatingC.getId(), null, authToken);

    boolean relationshipPresent = false;
    for (final Relationship<?, ?> rel : originatingConceptRels.getObjects()) {
      if (rel.getFrom().getId().equals(originatingC.getId())
          && rel.getTo().getId().equals(createdC.getId())
          && rel.getRelationshipType().equals("RN")) {
        relationshipPresent = true;
      }
    }
    assertTrue(relationshipPresent);

    // Verify that the same is true for the inverse
    createdConceptRels = contentService.findConceptRelationships(
        createdC.getTerminologyId(), createdC.getTerminology(),
        createdC.getVersion(), null, null, authToken);

    relationshipPresent = false;
    for (final Relationship<?, ?> rel : createdConceptRels.getObjects()) {
      if (rel.getFrom().getId().equals(createdC.getId())
          && rel.getTo().getId().equals(originatingC.getId())
          && rel.getRelationshipType().equals("RB")) {
        relationshipPresent = true;
      }
    }
    assertTrue(relationshipPresent);

    // Verify that relationships from originatingConcept have been re-added to
    // createdConcept
    relationshipPresent = false;
    for (final Relationship<?, ?> rel : createdConceptRels.getObjects()) {
      if (rel.getFrom().getId().equals(createdC.getId())
          && rel.getTo().getId().equals(relatedC.getId())
          && rel.getRelationshipType().equals("RN")) {
        relationshipPresent = true;
      }
    }
    assertTrue(relationshipPresent);

    // Verify that the same is true for the inverse
    RelationshipList relatedConceptRels =
        contentService.findConceptRelationships(relatedC.getTerminologyId(),
            relatedC.getTerminology(), relatedC.getVersion(),
            "fromId:" + relatedC.getId(), null, authToken);

    relationshipPresent = false;
    for (final Relationship<?, ?> rel : relatedConceptRels.getObjects()) {
      if (rel.getFrom().getId().equals(relatedC.getId())
          && rel.getTo().getId().equals(createdC.getId())
          && rel.getRelationshipType().equals("RB")) {
        relationshipPresent = true;
      }
    }
    assertTrue(relationshipPresent);

    // Verify the log entry exists
    logEntry = projectService.getLog(project.getId(), originatingC.getId(),
        null, 1, authToken);
    assertTrue(logEntry.contains("REDO " + ma.getName() + ", " + ma.getId()));

  }

  /**
   * Test undo merge concepts.
   *
   * @throws Exception the exception
   */
  @Test
  public void testUndoAndRedoMergeConcepts() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

    Logger.getLogger(getClass())
        .info("TEST - Merge concept CONCEPTID into concept CONCEPTID2, "
            + umlsTerminology + ", " + umlsVersion + authToken);

    //
    // Prepare the test and check prerequisites
    //
    // Due to MySQL rounding to the second, we must also round our comparison
    // startDate.
    Date startDate = DateUtils.round(new Date(), Calendar.SECOND);

    // Populate concept components
    populateConcepts();

    // Create a DEMOTION between concept and concept2, and its inverse
    Atom fromAtom = concept.getAtoms().get(0);
    Atom toAtom = concept2.getAtoms().get(0);

    AtomRelationship demotion = new AtomRelationshipJpa();
    demotion.setFrom(fromAtom);
    demotion.setTo(toAtom);
    demotion.setTerminology(umlsTerminology);
    demotion.setTerminologyId("");
    demotion.setBranch(Branch.ROOT);
    demotion.setName("Test Demotion");
    demotion.setVersion(umlsVersion);
    demotion.setRelationshipType("RO");
    demotion.setWorkflowStatus(WorkflowStatus.DEMOTION);
    demotion =
        testService.addRelationship((AtomRelationshipJpa) demotion, authToken);

    AtomRelationship inverseDemotion = new AtomRelationshipJpa();
    inverseDemotion.setFrom(toAtom);
    inverseDemotion.setTo(fromAtom);
    inverseDemotion.setTerminology(umlsTerminology);
    inverseDemotion.setTerminologyId("");
    inverseDemotion.setBranch(Branch.ROOT);
    inverseDemotion.setName("Test Demotion");
    inverseDemotion.setVersion(umlsVersion);
    inverseDemotion.setRelationshipType("RO");
    inverseDemotion.setWorkflowStatus(WorkflowStatus.DEMOTION);
    inverseDemotion = testService
        .addRelationship((AtomRelationshipJpa) inverseDemotion, authToken);

    final Long demotionRelationshipId = demotion.getId();
    final Long inverseDemotionRelationshipId = inverseDemotion.getId();

    // Add demotions to atoms and update
    fromAtom.getRelationships().add(demotion);
    toAtom.getRelationships().add(inverseDemotion);

    testService.updateAtom((AtomJpa) fromAtom, authToken);
    testService.updateAtom((AtomJpa) toAtom, authToken);

    fromAtom = testService.getAtom(fromAtom.getId(), authToken);
    toAtom = testService.getAtom(toAtom.getId(), authToken);

    // Make sure the demotions are there
    boolean demotionPresent = false;
    for (AtomRelationship atomRel : fromAtom.getRelationships()) {
      if (atomRel.getId().equals(demotionRelationshipId)) {
        demotionPresent = true;
        break;
      }
    }
    assertTrue(demotionPresent);

    boolean inverseDemotionPresent = false;
    for (AtomRelationship atomRel : toAtom.getRelationships()) {
      if (atomRel.getId().equals(inverseDemotionRelationshipId)) {
        inverseDemotionPresent = true;
        break;
      }
    }
    assertTrue(inverseDemotionPresent);

    // get the fromConcept, toConcept, and relatedConcept
    Concept toC =
        contentService.getConcept(concept.getId(), project.getId(), authToken);
    assertNotNull(toC);
    Concept fromC =
        contentService.getConcept(concept2.getId(), project.getId(), authToken);
    assertNotNull(fromC);
    Concept relatedC =
        contentService.getConcept(concept3.getId(), project.getId(), authToken);
    assertNotNull(relatedC);

    // Save fromC and ID to check molecular Action and relationship once Concept
    // is removed
    final Long fromCId = fromC.getId();

    // Save the components in the fromConcept to check later
    final List<Atom> fromAtomsList = fromC.getAtoms();
    final List<SemanticTypeComponent> fromStyList = fromC.getSemanticTypes();
    final List<ConceptRelationship> fromRelList = fromC.getRelationships();
    // Save the

    // Now that the concepts are all set up, merge them.
    ValidationResult v = metaEditingService.mergeConcepts(project.getId(),
        fromC.getId(), "activityId", fromC.getLastModified().getTime(),
        toC.getId(), false, authToken);
    assertTrue(v.getErrors().isEmpty());

    toC = contentService.getConcept(toC.getId(), project.getId(), authToken);
    relatedC =
        contentService.getConcept(relatedC.getId(), project.getId(), authToken);
    fromAtom = testService.getAtom(fromAtom.getId(), authToken);
    toAtom = testService.getAtom(toAtom.getId(), authToken);

    // Get the merge molecular action

    // verify the molecular action exists
    PfsParameterJpa pfs = new PfsParameterJpa();
    pfs.setSortField("lastModified");
    pfs.setAscending(false);
    MolecularActionList list = projectService.findMolecularActions(fromCId,
        umlsTerminology, umlsVersion, null, pfs, authToken);
    assertTrue(list.size() > 0);
    MolecularAction ma = list.getObjects().get(0);
    assertNotNull(ma);
    assertEquals(fromCId, ma.getComponentId());
    assertEquals(toC.getId(), ma.getComponentId2());
    assertTrue(ma.getLastModified().compareTo(startDate) >= 0);
    assertNotNull(ma.getAtomicActions());

    // Save the molecular action lastModified to compare against later
    Date modDate = ma.getLastModified();

    //
    // Undo the merge action
    //

    v = metaEditingService.undoAction(project.getId(), ma.getId(), "activityId",
        false, authToken);
    assertTrue(v.getErrors().isEmpty());

    toC = contentService.getConcept(toC.getId(), project.getId(), authToken);
    fromC = contentService.getConcept(fromCId, project.getId(), authToken);
    relatedC =
        contentService.getConcept(relatedC.getId(), project.getId(), authToken);
    fromAtom = testService.getAtom(fromAtom.getId(), authToken);
    toAtom = testService.getAtom(toAtom.getId(), authToken);
    ma = projectService.findMolecularActions(fromCId, umlsTerminology,
        umlsVersion, null, pfs, authToken).getObjects().get(0);

    // Verify the molecular action undone flag is set, and the lastModified has
    // been updated
    assertEquals(true, ma.isUndoneFlag());
    assertTrue(ma.getLastModified().compareTo(modDate) >= 0);

    // Verify the from concept has been recreated
    assertNotNull(
        contentService.getConcept(fromCId, project.getId(), authToken));

    // Verify the atoms have been returned from the toConcept to the fromConcept
    for (Atom a : fromAtomsList) {
      assertTrue(fromC.getAtoms().contains(a));
    }
    for (Atom a : toC.getAtoms()) {
      assertTrue(!fromAtomsList.contains(a));
    }

    // Verify all semantic types have been returned to the fromConcept
    for (SemanticTypeComponent sty : fromStyList) {
      assertTrue(fromC.getSemanticTypes().contains(sty));
    }

    // Verify all relationships have been returned to the fromConcept
    for (ConceptRelationship rel : fromRelList) {
      assertTrue(fromC.getRelationships().contains(rel));
    }

    // Verify the demotions are have been restored
    demotionPresent = false;
    for (AtomRelationship atomRel : fromAtom.getRelationships()) {
      if (atomRel.getId().equals(demotionRelationshipId)) {
        demotionPresent = true;
        break;
      }
    }
    assertTrue(demotionPresent);

    inverseDemotionPresent = false;
    for (AtomRelationship atomRel : toAtom.getRelationships()) {
      if (atomRel.getId().equals(inverseDemotionRelationshipId)) {
        inverseDemotionPresent = true;
        break;
      }
    }
    assertTrue(inverseDemotionPresent);

    // Verify the log entry exists
    String logEntry =
        projectService.getLog(project.getId(), fromCId, null, 1, authToken);
    assertTrue(logEntry.contains("UNDO " + ma.getName() + ", " + ma.getId()));

    //
    // Redo the merge action
    //

    v = metaEditingService.redoAction(project.getId(), ma.getId(), "activityId",
        false, authToken);
    assertTrue(v.getErrors().isEmpty());

    toC = contentService.getConcept(toC.getId(), project.getId(), authToken);
    relatedC =
        contentService.getConcept(relatedC.getId(), project.getId(), authToken);
    fromAtom = testService.getAtom(fromAtom.getId(), authToken);
    toAtom = testService.getAtom(toAtom.getId(), authToken);
    ma = projectService.findMolecularActions(fromCId, umlsTerminology,
        umlsVersion, null, pfs, authToken).getObjects().get(0);

    // Verify the molecular action undone flag is set, and the lastModified has
    // been updated
    assertEquals(false, ma.isUndoneFlag());
    assertTrue(ma.getLastModified().compareTo(modDate) >= 0);

    // Verify fromConcept has been re-removed
    fromC = contentService.getConcept(fromCId, project.getId(), authToken);
    assertTrue(fromC == null);

    // Verify the atoms have been re-merged from the fromConcept into the
    // toConcept
    for (Atom a : fromAtomsList) {
      assertTrue(toC.getAtoms().contains(a));
    }

    // Verify all semantic types have been re-merged into the toConcept
    for (SemanticTypeComponent sty : fromStyList) {
      assertTrue(toC.getSemanticTypes().contains(sty));
    }

    // Verify all relationships have been re-merged to the toConcept
    for (ConceptRelationship rel : fromRelList) {
      assertTrue(toC.getRelationships().contains(rel));
    }

    // Verify the DEMOTION relationship and its inverse have been re-deleted
    demotionPresent = false;
    for (AtomRelationship atomRel : fromAtom.getRelationships()) {
      if (atomRel.getId().equals(demotionRelationshipId)) {
        demotionPresent = true;
      }
    }
    assertFalse(demotionPresent);

    inverseDemotionPresent = false;
    for (AtomRelationship atomRel : toAtom.getRelationships()) {
      if (atomRel.getId().equals(inverseDemotionRelationshipId)) {
        inverseDemotionPresent = true;
      }
    }
    assertFalse(inverseDemotionPresent);    
    
    // Verify the log entry exists
    logEntry =
        projectService.getLog(project.getId(), fromCId, null, 1, authToken);
    assertTrue(logEntry.contains("REDO " + ma.getName() + ", " + ma.getId()));

  }

  /**
   * Test undo and redo approve concept.
   *
   * @throws Exception the exception
   */
  @Test
  public void testUndoAndRedoApproveConcept() throws Exception {
    Logger.getLogger(getClass()).debug("Start test " + name.getMethodName());

    Logger.getLogger(getClass()).info("TEST - Approve concept CONCEPTID, "
        + umlsTerminology + ", " + umlsVersion + authToken);

    //
    // Prepare the test and check prerequisites
    //
    // Due to MySQL rounding to the second, we must also round our comparison
    // startDate.
    Date startDate = DateUtils.round(new Date(), Calendar.SECOND);

    // Populate concept components
    populateConcepts();

    // Create a DEMOTION between concept and concept2, and its inverse
    Atom fromAtom = concept.getAtoms().get(0);
    Atom toAtom = concept2.getAtoms().get(0);

    AtomRelationship demotion = new AtomRelationshipJpa();
    demotion.setFrom(fromAtom);
    demotion.setTo(toAtom);
    demotion.setTerminology(umlsTerminology);
    demotion.setTerminologyId("");
    demotion.setBranch(Branch.ROOT);
    demotion.setName("Test Demotion");
    demotion.setVersion(umlsVersion);
    demotion.setRelationshipType("RO");
    demotion.setWorkflowStatus(WorkflowStatus.DEMOTION);
    demotion =
        testService.addRelationship((AtomRelationshipJpa) demotion, authToken);

    AtomRelationship inverseDemotion = new AtomRelationshipJpa();
    inverseDemotion.setFrom(toAtom);
    inverseDemotion.setTo(fromAtom);
    inverseDemotion.setTerminology(umlsTerminology);
    inverseDemotion.setTerminologyId("");
    inverseDemotion.setBranch(Branch.ROOT);
    inverseDemotion.setName("Test Demotion");
    inverseDemotion.setVersion(umlsVersion);
    inverseDemotion.setRelationshipType("RO");
    inverseDemotion.setWorkflowStatus(WorkflowStatus.DEMOTION);
    inverseDemotion = testService
        .addRelationship((AtomRelationshipJpa) inverseDemotion, authToken);

    // Add demotions to atoms and update
    fromAtom.getRelationships().add(demotion);
    toAtom.getRelationships().add(inverseDemotion);

    testService.updateAtom((AtomJpa) fromAtom, authToken);
    testService.updateAtom((AtomJpa) toAtom, authToken);

    fromAtom = testService.getAtom(fromAtom.getId(), authToken);
    toAtom = testService.getAtom(toAtom.getId(), authToken);

    // Make sure the demotions are there
    boolean demotionPresent = false;
    for (AtomRelationship atomRel : fromAtom.getRelationships()) {
      if (atomRel.getWorkflowStatus().equals(WorkflowStatus.DEMOTION)) {
        demotionPresent = true;
        break;
      }
    }
    assertTrue(demotionPresent);

    boolean inverseDemotionPresent = false;
    for (AtomRelationship atomRel : toAtom.getRelationships()) {
      if (atomRel.getWorkflowStatus().equals(WorkflowStatus.DEMOTION)) {
        inverseDemotionPresent = true;
        break;
      }
    }
    assertTrue(inverseDemotionPresent);

    final Long demotionRelationshipId = demotion.getId();
    final Long inverseDemotionRelationshipId = inverseDemotion.getId();

    // get the concept
    Concept c =
        contentService.getConcept(concept.getId(), project.getId(), authToken);
    assertNotNull(c);

    // Now that the concepts are all set up, approve it.
    ValidationResult v =
        metaEditingService.approveConcept(project.getId(), c.getId(),
            "activityId", c.getLastModified().getTime(), false, authToken);
    assertTrue(v.getErrors().isEmpty());

    c = contentService.getConcept(concept.getId(), project.getId(), authToken);

    // Get the merge molecular action

    // verify the molecular action exists
    PfsParameterJpa pfs = new PfsParameterJpa();
    pfs.setSortField("lastModified");
    pfs.setAscending(false);
    MolecularActionList list = projectService.findMolecularActions(c.getId(),
        umlsTerminology, umlsVersion, null, pfs, authToken);
    assertTrue(list.size() > 0);
    MolecularAction ma = list.getObjects().get(0);
    assertNotNull(ma);
    assertEquals(c.getId(), ma.getComponentId());
    assertNull(ma.getComponentId2());
    assertTrue(ma.getLastModified().compareTo(startDate) >= 0);
    assertNotNull(ma.getAtomicActions());

    // Save the molecular action lastModified to compare against later
    Date modDate = ma.getLastModified();

    //
    // Undo the approve action
    //

    v = metaEditingService.undoAction(project.getId(), ma.getId(), "activityId",
        false, authToken);
    assertTrue(v.getErrors().isEmpty());

    c = contentService.getConcept(c.getId(), project.getId(), authToken);
    ma = projectService.findMolecularActions(c.getId(), umlsTerminology,
        umlsVersion, null, pfs, authToken).getObjects().get(0);
    fromAtom = testService.getAtom(fromAtom.getId(), authToken);
    toAtom = testService.getAtom(toAtom.getId(), authToken);

    // Verify the molecular action undone flag is set, and the lastModified has
    // been updated
    assertEquals(true, ma.isUndoneFlag());
    assertTrue(ma.getLastModified().compareTo(modDate) >= 0);

    // Verify the DEMOTION relationship and its inverse have been re-created
    demotionPresent = false;
    for (AtomRelationship atomRel : fromAtom.getRelationships()) {
      if (atomRel.getId().equals(demotionRelationshipId)) {
        demotionPresent = true;
        break;
      }
    }
    assertTrue(demotionPresent);

    inverseDemotionPresent = false;
    for (AtomRelationship atomRel : toAtom.getRelationships()) {
      if (atomRel.getId().equals(inverseDemotionRelationshipId)) {
        inverseDemotionPresent = true;
        break;
      }
    }
    assertTrue(inverseDemotionPresent);

    // Verify concept now a workflow status of "NEEDS_REVIEW"
    assertEquals(WorkflowStatus.NEEDS_REVIEW, c.getWorkflowStatus());

    // Verify that all of the concept's atoms have a status of
    // "NEEDS_REVIEW"
    boolean allAtomsReadyForPub = true;
    for (Atom atm : c.getAtoms()) {
      if (!atm.getWorkflowStatus().equals(WorkflowStatus.NEEDS_REVIEW)) {
        allAtomsReadyForPub = false;
      }
    }
    assertTrue(allAtomsReadyForPub);

    // Verify that all of the concept's semantic types have a status of
    // "NEEDS_REVIEW"
    boolean allStyReadyForPub = true;
    for (SemanticTypeComponent sty : c.getSemanticTypes()) {
      if (!sty.getWorkflowStatus().equals(WorkflowStatus.NEEDS_REVIEW)) {
        allStyReadyForPub = false;
      }
    }
    assertTrue(allStyReadyForPub);

    // Verify that all of the concept's relationships and inverses have a status
    // of "NEEDS_REVIEW"
    RelationshipList relList =
        contentService.findConceptRelationships(c.getTerminologyId(),
            c.getTerminology(), c.getVersion(), null, null, authToken);

    boolean allRelsNotReadyForPub = true;
    boolean allInverseRelsNotReadyForPub = true;
    for (final Relationship<?, ?> rel : relList.getObjects()) {
      if (!rel.getWorkflowStatus().equals(WorkflowStatus.NEEDS_REVIEW)) {
        allRelsNotReadyForPub = false;
      }

      // Check its inverse also
      String inverseRelType = "";
      switch (rel.getRelationshipType()) {
        case "RN":
          inverseRelType = "RB";
          break;
        case "RB":
          inverseRelType = "RN";
          break;
        case "RO":
          inverseRelType = "RO";
          break;
        case "XR":
          inverseRelType = "XR";
          break;
        default:
          throw new Exception(
              "Unexpedted relationship type: " + rel.getRelationshipType());
      }

      // This will return the single inverse relationship
      RelationshipList inverseRelList =
          contentService
              .findConceptRelationships(rel.getTo().getTerminologyId(),
                  rel.getTo().getTerminology(),
                  rel.getTo().getVersion(), "toId:" + rel.getFrom().getId()
                      + " AND relationshipType:" + inverseRelType,
                  null, authToken);
      if (!inverseRelList.getObjects().get(0).getWorkflowStatus()
          .equals(WorkflowStatus.NEEDS_REVIEW)) {
        allInverseRelsNotReadyForPub = false;
      }
    }
    assertTrue(allRelsNotReadyForPub);
    assertTrue(allInverseRelsNotReadyForPub);

    // Verify the log entry exists
    String logEntry =
        projectService.getLog(project.getId(), c.getId(), null, 1, authToken);
    assertTrue(logEntry.contains("UNDO " + ma.getName() + ", " + ma.getId()));

    //
    // Redo the approve action
    //

    v = metaEditingService.redoAction(project.getId(), ma.getId(), "activityId",
        false, authToken);
    assertTrue(v.getErrors().isEmpty());

    c = contentService.getConcept(c.getId(), project.getId(), authToken);
    ma = projectService.findMolecularActions(c.getId(), umlsTerminology,
        umlsVersion, null, pfs, authToken).getObjects().get(0);
    fromAtom = testService.getAtom(fromAtom.getId(), authToken);
    toAtom = testService.getAtom(toAtom.getId(), authToken);

    // Verify the molecular action undone flag is set, and the lastModified has
    // been updated
    assertEquals(false, ma.isUndoneFlag());
    assertTrue(ma.getLastModified().compareTo(modDate) >= 0);

    // Verify the DEMOTION relationship and its inverse have been re-deleted
    demotionPresent = false;
    for (AtomRelationship atomRel : fromAtom.getRelationships()) {
      if (atomRel.getId().equals(demotionRelationshipId)) {
        demotionPresent = true;
        break;
      }
    }
    assertFalse(demotionPresent);

    inverseDemotionPresent = false;
    for (AtomRelationship atomRel : toAtom.getRelationships()) {
      if (atomRel.getId().equals(inverseDemotionRelationshipId)) {
        inverseDemotionPresent = true;
        break;
      }
    }
    assertFalse(inverseDemotionPresent);

    // Verify concept now a workflow status of "READY_FOR_PUBLICATION" again
    assertEquals(WorkflowStatus.READY_FOR_PUBLICATION, c.getWorkflowStatus());

    // Verify that all of the concept's atoms have a status of
    // "READY_FOR_PUBLICATION" again
    allAtomsReadyForPub = true;
    for (Atom atm : c.getAtoms()) {
      if (!atm.getWorkflowStatus()
          .equals(WorkflowStatus.READY_FOR_PUBLICATION)) {
        allAtomsReadyForPub = false;
      }
    }
    assertTrue(allAtomsReadyForPub);

    // Verify that all of the concept's semantic types have a status of
    // "READY_FOR_PUBLICATION" again
    allStyReadyForPub = true;
    for (SemanticTypeComponent sty : c.getSemanticTypes()) {
      if (!sty.getWorkflowStatus()
          .equals(WorkflowStatus.READY_FOR_PUBLICATION)) {
        allStyReadyForPub = false;
      }
    }
    assertTrue(allStyReadyForPub);

    // Verify that all of the concept's relationships and inverses have a status
    // of "READY_FOR_PUBLICATION", and a RelationshipType of RO, RB, RN, or XR
    // again
    relList = contentService.findConceptRelationships(c.getTerminologyId(),
        c.getTerminology(), c.getVersion(), null, null, authToken);

    List<String> typeList = Arrays.asList("RO", "RB", "RN", "XR");

    allRelsNotReadyForPub = true;
    boolean allRelsCorrectType = true;
    allInverseRelsNotReadyForPub = true;
    boolean allInverseRelsCorrectType = true;
    for (final Relationship<?, ?> rel : relList.getObjects()) {
      if (!rel.getWorkflowStatus()
          .equals(WorkflowStatus.READY_FOR_PUBLICATION)) {
        allRelsNotReadyForPub = false;
      }
      if (!typeList.contains(rel.getRelationshipType())) {
        allRelsCorrectType = false;
      }

      // Check its inverse also
      String inverseRelType = "";
      switch (rel.getRelationshipType()) {
        case "RN":
          inverseRelType = "RB";
          break;
        case "RB":
          inverseRelType = "RN";
          break;
        case "RO":
          inverseRelType = "RO";
          break;
        case "XR":
          inverseRelType = "XR";
          break;
        default:
          throw new Exception(
              "Unexpedted relationship type: " + rel.getRelationshipType());
      }

      // This will return the single inverse relationship
      RelationshipList inverseRelList =
          contentService
              .findConceptRelationships(rel.getTo().getTerminologyId(),
                  rel.getTo().getTerminology(),
                  rel.getTo().getVersion(), "toId:" + rel.getFrom().getId()
                      + " AND relationshipType:" + inverseRelType,
                  null, authToken);
      if (!inverseRelList.getObjects().get(0).getWorkflowStatus()
          .equals(WorkflowStatus.READY_FOR_PUBLICATION)) {
        allInverseRelsNotReadyForPub = false;
      }
      if (!typeList
          .contains(inverseRelList.getObjects().get(0).getRelationshipType())) {
        allInverseRelsCorrectType = false;
      }
    }
    assertTrue(allRelsNotReadyForPub);
    assertTrue(allRelsCorrectType);
    assertTrue(allInverseRelsNotReadyForPub);
    assertTrue(allInverseRelsCorrectType);

    // Verify the log entry exists
    logEntry =
        projectService.getLog(project.getId(), c.getId(), null, 1, authToken);
    assertTrue(logEntry.contains("REDO " + ma.getName() + ", " + ma.getId()));

  }

  /**
   * Test undo and redo update atom.
   *
   * @throws Exception the exception
   */
  @Test
  public void testUndoAndRedoUpdateAtom() throws Exception {
    Logger.getLogger(getClass()).debug("Start test " + name.getMethodName());

    Logger.getLogger(getClass()).info("TEST - Update atom on " + "C0000294,"
        + umlsTerminology + ", " + umlsVersion + ", " + authToken);

    //
    // Prepare the test and check prerequisites
    //
    // Due to MySQL rounding to the second, we must also round our comparison
    // startDate.
    Date startDate = DateUtils.round(new Date(), Calendar.SECOND);

    // get the concept
    Concept c =
        contentService.getConcept(concept.getId(), project.getId(), authToken);
    assertNotNull(c);

    // construct an atom not present on concept (here, DCB)
    AtomJpa atom = new AtomJpa();
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
    atom.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);

    // add the atom to the concept
    ValidationResult v = metaEditingService.addAtom(project.getId(), c.getId(),
        "activityId", c.getLastModified().getTime(), atom, false, authToken);
    assertTrue(v.getErrors().isEmpty());

    // retrieve the concept
    c = contentService.getConcept(concept.getId(), project.getId(), authToken);

    // Save a copy of the added atom
    AtomJpa addedAtom = null;
    for (Atom a : c.getAtoms()) {
      if (a.getName().equals("DCB")) {
        addedAtom = (AtomJpa) a;
      }
    }
    assertNotNull(addedAtom);

    //
    // Update updating an allowable field on the Atom
    //
    addedAtom.setPublishable(false);

    boolean updateSucceded = true;
    try {
      v = metaEditingService.updateAtom(project.getId(), c.getId(),
          "activityId", c.getLastModified().getTime(), addedAtom, false,
          authToken);
    } catch (Exception e) {
      updateSucceded = false;
    }
    assertTrue(updateSucceded);
    c = contentService.getConcept(concept.getId(), project.getId(), authToken);

    // Pull the atom from the concept, and make sure the language
    // updated successfully
    addedAtom = null;
    for (Atom a : c.getAtoms()) {
      if (a.getName().equals("DCB")) {
        addedAtom = (AtomJpa) a;
      }
    }
    assertNotNull(addedAtom);
    assertFalse(addedAtom.isPublishable());

    // verify the molecular action exists
    PfsParameterJpa pfs = new PfsParameterJpa();
    pfs.setSortField("lastModified");
    pfs.setAscending(false);
    MolecularActionList list = projectService.findMolecularActions(c.getId(),
        umlsTerminology, umlsVersion, null, pfs, authToken);
    assertTrue(list.size() > 0);
    MolecularAction ma = list.getObjects().get(0);
    assertNotNull(ma);
    assertEquals(c.getId(), ma.getComponentId());
    assertTrue(ma.getLastModified().compareTo(startDate) >= 0);
    assertNotNull(ma.getAtomicActions());

    // Save the molecular action lastModified to compare against later
    Date modDate = ma.getLastModified();

    //
    // Undo the update atom action
    //

    v = metaEditingService.undoAction(project.getId(), ma.getId(), "activityId",
        false, authToken);
    assertTrue(v.getErrors().isEmpty());

    c = contentService.getConcept(c.getId(), project.getId(), authToken);
    ma = projectService.findMolecularActions(c.getId(), umlsTerminology,
        umlsVersion, null, pfs, authToken).getObjects().get(0);

    // Verify the molecular action undone flag is set, and the lastModified has
    // been updated
    assertEquals(true, ma.isUndoneFlag());
    assertTrue(ma.getLastModified().compareTo(modDate) >= 0);

    // Pull the atom from the concept, and make sure the language
    // reverted successfully
    addedAtom = null;
    for (Atom a : c.getAtoms()) {
      if (a.getName().equals("DCB")) {
        addedAtom = (AtomJpa) a;
      }
    }
    assertNotNull(addedAtom);
    assertTrue(addedAtom.isPublishable());

    // Verify the log entry exists
    String logEntry =
        projectService.getLog(project.getId(), c.getId(), null, 1, authToken);
    assertTrue(logEntry.contains("UNDO " + ma.getName() + ", " + ma.getId()));

    //
    // Redo the merge action
    //

    v = metaEditingService.redoAction(project.getId(), ma.getId(), "activityId",
        false, authToken);
    assertTrue(v.getErrors().isEmpty());

    c = contentService.getConcept(c.getId(), project.getId(), authToken);
    ma = projectService.findMolecularActions(c.getId(), umlsTerminology,
        umlsVersion, null, pfs, authToken).getObjects().get(0);

    // Verify the molecular action undone flag is set, and the lastModified has
    // been updated
    assertEquals(false, ma.isUndoneFlag());
    assertTrue(ma.getLastModified().compareTo(modDate) >= 0);

    // Pull the atom from the concept, and make sure the language
    // re-updated successfully
    addedAtom = null;
    for (Atom a : c.getAtoms()) {
      if (a.getName().equals("DCB")) {
        addedAtom = (AtomJpa) a;
      }
    }
    assertNotNull(addedAtom);
    assertFalse(addedAtom.isPublishable());

    // Verify the log entry exists
    logEntry =
        projectService.getLog(project.getId(), c.getId(), null, 1, authToken);
    assertTrue(logEntry.contains("REDO " + ma.getName() + ", " + ma.getId()));

  }

  /**
   * Test force undo and redo.
   *
   * @throws Exception the exception
   */
  @Test
  public void testForceUndoAndRedo() throws Exception {
    Logger.getLogger(getClass()).debug("Start test " + name.getMethodName());

    Logger.getLogger(getClass())
        .info("TEST - Force undoing and redoing changes, " + umlsTerminology
            + ", " + umlsVersion + authToken);

    //
    // Prepare the test and check prerequisites
    //
    // Due to MySQL rounding to the second, we must also round our comparison
    // startDate.
    Date startDate = DateUtils.round(new Date(), Calendar.SECOND);

    // get the concept
    Concept c =
        contentService.getConcept(concept.getId(), project.getId(), authToken);
    assertNotNull(c);

    //
    // Create and add an atom to concept
    //
    AtomJpa atom = new AtomJpa();
    atom.setBranch(Branch.ROOT);
    atom.setName("DCB");
    atom.setTerminologyId("TestId");
    atom.setTerminology(umlsTerminology);
    atom.setVersion(umlsVersion);
    atom.setTimestamp(new Date());
    atom.setPublishable(true);
    atom.setCodeId("C44314");
    atom.setConceptId("M0023181");
    atom.getConceptTerminologyIds().put(c.getTerminology(),
        c.getTerminologyId());
    atom.setDescriptorId("");
    atom.setLanguage("ENG");
    atom.setTermType("AB");
    atom.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);

    //
    // Add the Atom - this will set the concept's WorkflowStatus to NEEDS_REVIEW
    //
    ValidationResult v = metaEditingService.addAtom(project.getId(), c.getId(),
        "activityId", c.getLastModified().getTime(), atom, false, authToken);
    assertTrue(v.getErrors().isEmpty());
    c = contentService.getConcept(c.getId(), project.getId(), authToken);
    assertTrue(c.getWorkflowStatus().equals(WorkflowStatus.NEEDS_REVIEW));

    // Approve this concept - this will set the status to READY_FOR_PUBLICATION
    v = metaEditingService.approveConcept(project.getId(), c.getId(),
        "activityId", c.getLastModified().getTime(), false, authToken);
    assertTrue(v.getErrors().isEmpty());
    c = contentService.getConcept(concept.getId(), project.getId(), authToken);
    assertTrue(
        c.getWorkflowStatus().equals(WorkflowStatus.READY_FOR_PUBLICATION));

    // set the concept's workflow status back to NEEDS_REVIEW
    c.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
    testService.updateConcept((ConceptJpa) c, authToken);
    c = contentService.getConcept(concept.getId(), project.getId(), authToken);
    assertTrue(c.getWorkflowStatus().equals(WorkflowStatus.NEEDS_REVIEW));

    // Get the merge molecular action

    // verify the molecular action exists
    PfsParameterJpa pfs = new PfsParameterJpa();
    pfs.setSortField("lastModified");
    pfs.setAscending(false);
    MolecularActionList list = projectService.findMolecularActions(c.getId(),
        umlsTerminology, umlsVersion, null, pfs, authToken);
    assertTrue(list.size() > 0);
    MolecularAction ma = list.getObjects().get(0);
    assertNotNull(ma);
    assertEquals(c.getId(), ma.getComponentId());
    assertNull(ma.getComponentId2());
    assertTrue(ma.getLastModified().compareTo(startDate) >= 0);
    assertNotNull(ma.getAtomicActions());

    // Reset the concept's workflow status to "NEEDS_REVIEW".
    c.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
    testService.updateConcept((ConceptJpa) c, authToken);

    c = contentService.getConcept(concept.getId(), project.getId(), authToken);
    assertTrue(c.getWorkflowStatus().equals(WorkflowStatus.NEEDS_REVIEW));

    //
    // Try to undo the approve action (this will fail, since the concept's
    // WorkflowStatus is different than it was
    //
    try {
      v = metaEditingService.undoAction(project.getId(), ma.getId(),
          "activityId", false, authToken);
      fail(
          "Undo should fail: concept workflow status has changed since action was performed");
    } catch (Exception e) {
      // n/a
    }
    c = contentService.getConcept(concept.getId(), project.getId(), authToken);

    // Make sure the workflow status didn't get modified during the failed Undo
    // attempt
    assertTrue(c.getWorkflowStatus().equals(WorkflowStatus.NEEDS_REVIEW));

    //
    // Force-Undo the approve action
    //
    boolean undoSucceded = true;
    try {
      v = metaEditingService.undoAction(project.getId(), ma.getId(),
          "activityId", true, authToken);
    } catch (Exception e) {
      undoSucceded = false;
    }
    assertTrue(undoSucceded);
    c = contentService.getConcept(concept.getId(), project.getId(), authToken);

    // verify the atom's WorkflowStatus has been reset to NEEDS_REVIEV
    for (Atom a : c.getAtoms()) {
      assertTrue(a.getWorkflowStatus().equals(WorkflowStatus.NEEDS_REVIEW));
    }

    // Modify the concept's WorkflowStatus again
    c.setWorkflowStatus(WorkflowStatus.REVIEW_DONE);
    testService.updateConcept((ConceptJpa) c, authToken);

    c = contentService.getConcept(concept.getId(), project.getId(), authToken);
    assertTrue(c.getWorkflowStatus().equals(WorkflowStatus.REVIEW_DONE));

    //
    // Try to redo the approve action (this will fail, since the concept's
    // WorkflowStatus is different than it was
    //
    try {
      v = metaEditingService.undoAction(project.getId(), ma.getId(),
          "activityId", false, authToken);
      fail(
          "Redo should fail: concept workflow status has changed since action was performed");
    } catch (Exception e) {
      // n/a
    }
    c = contentService.getConcept(concept.getId(), project.getId(), authToken);

    // Make sure the workflow status didn't get modified during the failed Undo
    // attempt
    assertTrue(c.getWorkflowStatus().equals(WorkflowStatus.REVIEW_DONE));

    //
    // Force-Redo the approve action
    //
    boolean redoSucceded = true;
    try {
      v = metaEditingService.redoAction(project.getId(), ma.getId(),
          "activityId", true, authToken);
    } catch (Exception e) {
      redoSucceded = false;
    }
    assertTrue(redoSucceded);
    c = contentService.getConcept(concept.getId(), project.getId(), authToken);

    // verify the concept and atom's WorkflowStatus has been reset to
    // READY_FOR_PUBLICATION
    assertTrue(
        c.getWorkflowStatus().equals(WorkflowStatus.READY_FOR_PUBLICATION));
    for (Atom a : c.getAtoms()) {
      assertTrue(
          a.getWorkflowStatus().equals(WorkflowStatus.READY_FOR_PUBLICATION));
    }

  }

  /**
   * Teardown.
   *
   * @throws Exception the exception
   */
  @Override
  @After
  public void teardown() throws Exception {

    // Delete copies of concepts created during this test
    if (concept != null && contentService.getConcept(concept.getId(),
        project.getId(), authToken) != null) {
      IntegrationTestClientRest testService =
          new IntegrationTestClientRest(ConfigUtility.getConfigProperties());
      testService.removeConcept(concept.getId(), true, authToken);
    }

    if (concept2 != null && contentService.getConcept(concept2.getId(),
        project.getId(), authToken) != null) {
      testService =
          new IntegrationTestClientRest(ConfigUtility.getConfigProperties());
      testService.removeConcept(concept2.getId(), true, authToken);
    }

    if (concept3 != null && contentService.getConcept(concept3.getId(),
        project.getId(), authToken) != null) {
      testService =
          new IntegrationTestClientRest(ConfigUtility.getConfigProperties());
      testService.removeConcept(concept3.getId(), true, authToken);
    }

    if (concept4 != null && contentService.getConcept(concept4.getId(),
        project.getId(), authToken) != null) {
      testService =
          new IntegrationTestClientRest(ConfigUtility.getConfigProperties());
      testService.removeConcept(concept4.getId(), true, authToken);
    }

    // Turn the standard validation checks back on
    final List<String> validationChecks = project.getValidationChecks();
    validationChecks.add("DT_M1");
    validationChecks.add("DT_I3B");
    validationChecks.add("MGV_H1");
    validationChecks.add("MGV_H2");
    project.setValidationChecks(validationChecks);

    projectService.updateProject((ProjectJpa) project, authToken);

    // logout
    securityService.logout(authToken);

  }

}
