/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package com.wci.umls.server.test.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
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
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.ProjectList;
import com.wci.umls.server.helpers.content.RelationshipList;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.AttributeJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.ConceptRelationshipJpa;
import com.wci.umls.server.jpa.content.SemanticTypeComponentJpa;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.model.actions.AtomicAction;
import com.wci.umls.server.model.actions.MolecularAction;
import com.wci.umls.server.model.actions.MolecularActionList;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.Concept;
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
  private ConceptJpa concept;

  /**  The concept 2. */
  private ConceptJpa concept2;

  /**  The concept 3. */
  private ConceptJpa concept3;

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
    assertTrue(projects.getCount() > 0);
    project = projects.getObjects().get(0);

    // verify terminology and branch are expected values
    assertTrue(project.getTerminology().equals(umlsTerminology));
    // assertTrue(project.getBranch().equals(Branch.ROOT));

    // Copy existing concept to avoid messing with actual database data.
    concept = new ConceptJpa(contentService.getConcept("C0000294",
        umlsTerminology, umlsVersion, null, authToken), false);
    concept.setId(null);
    concept.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);
    concept = (ConceptJpa) testService.addConcept(concept, authToken);

    concept2 = new ConceptJpa(contentService.getConcept("C0002073",
        umlsTerminology, umlsVersion, null, authToken), false);
    concept2.setId(null);
    concept2.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);
    concept2 = (ConceptJpa) testService.addConcept(concept2, authToken);

    concept3 = new ConceptJpa(contentService.getConcept("C0065642",
        umlsTerminology, umlsVersion, null, authToken), false);
    concept3.setId(null);
    concept3.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);
    concept3 = (ConceptJpa) testService.addConcept(concept3, authToken);
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
    semanticType.setBranch(Branch.ROOT);
    semanticType.setSemanticType("Lipid");
    semanticType.setTerminologyId("TestId");
    semanticType.setTerminology(umlsTerminology);
    semanticType.setVersion(umlsVersion);
    semanticType.setTimestamp(new Date());
    semanticType.setPublishable(true);

    //
    // Test addition
    //

    // add the semantic type to the concept
    ValidationResult v =
        metaEditingService.addSemanticType(project.getId(), c.getId(),
            c.getLastModified().getTime(), semanticType, false, authToken);
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
    MolecularActionList list =
        contentService.findMolecularActions(c.getId(), null, pfs, authToken);
    assertTrue(list.getCount() > 0);
    MolecularAction ma = list.getObjects().get(0);
    assertNotNull(ma);
    assertTrue(ma.getTerminologyId().equals(c.getTerminologyId()));
    assertTrue(ma.getLastModified().compareTo(startDate) >= 0);
    assertNotNull(ma.getAtomicActions());

    // Verify that two atomic actions exists for add Semantic Type, and update
    // Concept WorkflowStatus
    pfs.setSortField("idType");
    pfs.setAscending(true);

    List<AtomicAction> atomicActions = contentService
        .findAtomicActions(ma.getId(), null, pfs, authToken).getObjects();
    assertEquals(2, atomicActions.size());
    assertEquals("CONCEPT", atomicActions.get(0).getIdType().toString());
    assertNotNull(atomicActions.get(0).getOldValue());
    assertNotNull(atomicActions.get(0).getNewValue());
    assertEquals("SEMANTIC_TYPE", atomicActions.get(1).getIdType().toString());
    assertNull(atomicActions.get(1).getOldValue());
    assertNotNull(atomicActions.get(1).getNewValue());

    // Verify the log entry exists
    String logEntry =
        projectService.getLog(project.getId(), c.getId(), 1, authToken);
    assertTrue(logEntry
        .contains("ADD_SEMANTIC_TYPE " + semanticType.getSemanticType()));

    //
    // Add second semantic type
    //

    // Due to MySQL rounding to the second, we must also round our comparison
    // startDate.
    startDate = DateUtils.round(new Date(), Calendar.SECOND);

    // construct a second semantic type not present on concept (here, Enzyme)
    SemanticTypeComponentJpa semanticType2 = new SemanticTypeComponentJpa();
    semanticType2.setBranch(Branch.ROOT);
    semanticType2.setSemanticType("Enzyme");
    semanticType2.setTerminologyId("TestId");
    semanticType2.setTerminology(umlsTerminology);
    semanticType2.setVersion(umlsVersion);
    semanticType2.setTimestamp(new Date());
    semanticType2.setPublishable(true);

    // add the second semantic type to the concept
    v = metaEditingService.addSemanticType(project.getId(), c.getId(),
        c.getLastModified().getTime(), semanticType2, false, authToken);
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
    list = contentService.findMolecularActions(c.getId(), null, pfs, authToken);
    assertTrue(list.getCount() > 0);
    ma = list.getObjects().get(0);
    assertNotNull(ma);
    assertTrue(ma.getTerminologyId().equals(c.getTerminologyId()));

    assertTrue(ma.getLastModified().compareTo(startDate) >= 0);
    assertNotNull(ma.getAtomicActions());

    // Verify that ONE atomic actions exists for add Semantic Type (Concept
    // Workflow Status was already set during previous addition)
    pfs.setSortField("idType");
    pfs.setAscending(true);

    atomicActions = contentService
        .findAtomicActions(ma.getId(), null, pfs, authToken).getObjects();
    assertEquals(1, atomicActions.size());
    assertEquals("SEMANTIC_TYPE", atomicActions.get(0).getIdType().toString());
    assertNull(atomicActions.get(0).getOldValue());
    assertNotNull(atomicActions.get(0).getNewValue());

    // Verify the log entry exists
    logEntry = projectService.getLog(project.getId(), c.getId(), 1, authToken);
    assertTrue(logEntry
        .contains("ADD_SEMANTIC_TYPE " + semanticType2.getSemanticType()));

    //
    // Test removal
    //

    // remove the first semantic type from the concept
    v = metaEditingService.removeSemanticType(project.getId(), c.getId(),
        c.getLastModified().getTime(), semanticType.getId(), false, authToken);
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
    list = contentService.findMolecularActions(c.getId(), null, pfs, authToken);
    assertTrue(list.getCount() > 0);
    ma = list.getObjects().get(0);
    assertNotNull(ma);
    assertTrue(ma.getTerminologyId().equals(c.getTerminologyId()));
    assertTrue(ma.getLastModified().compareTo(startDate) >= 0);
    assertNotNull(ma.getAtomicActions());

    // Verify that one atomic action exists for remove Semantic Type
    pfs.setAscending(true);

    atomicActions = contentService
        .findAtomicActions(ma.getId(), null, null, authToken).getObjects();
    assertEquals(atomicActions.size(), 1);
    assertEquals(atomicActions.get(0).getIdType().toString(), "SEMANTIC_TYPE");
    assertNotNull(atomicActions.get(0).getOldValue());
    assertNull(atomicActions.get(0).getNewValue());

    // Verify the log entry exists
    logEntry = projectService.getLog(project.getId(), c.getId(), 1, authToken);
    assertTrue(logEntry
        .contains("REMOVE_SEMANTIC_TYPE " + semanticType.getSemanticType()));

    // remove the second semantic type from the concept (assume verification of
    // MA, atomic actions, and log entry since we just tested those)
    v = metaEditingService.removeSemanticType(project.getId(), c.getId(),
        c.getLastModified().getTime(), semanticType2.getId(), false, authToken);
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
        c.getId(), c.getLastModified().getTime(), attribute, false, authToken);
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

    // verify that alternate ID was created and is correctly formed.
    assertNotNull(attribute.getAlternateTerminologyIds().get(umlsTerminology));
    assertTrue(attribute.getAlternateTerminologyIds().get(umlsTerminology)
        .startsWith("AT"));

    // verify the molecular action exists
    PfsParameterJpa pfs = new PfsParameterJpa();
    pfs.setSortField("lastModified");
    pfs.setAscending(false);
    MolecularActionList list =
        contentService.findMolecularActions(c.getId(), null, pfs, authToken);
    assertTrue(list.getCount() > 0);
    MolecularAction ma = list.getObjects().get(0);
    assertNotNull(ma);
    assertTrue(ma.getTerminologyId().equals(c.getTerminologyId()));
    assertTrue(ma.getLastModified().compareTo(startDate) >= 0);
    assertNotNull(ma.getAtomicActions());

    // Verify that two atomic actions exists for add attribute, and update
    // Concept WorkflowStatus

    pfs.setSortField("idType");
    pfs.setAscending(true);

    List<AtomicAction> atomicActions = contentService
        .findAtomicActions(ma.getId(), null, pfs, authToken).getObjects();
    assertEquals(2, atomicActions.size());
    assertEquals("ATTRIBUTE", atomicActions.get(0).getIdType().toString());
    assertNull(atomicActions.get(0).getOldValue());
    assertNotNull(atomicActions.get(0).getNewValue());
    assertEquals(atomicActions.get(1).getIdType().toString(), "CONCEPT");
    assertNotNull(atomicActions.get(1).getOldValue());
    assertNotNull(atomicActions.get(1).getNewValue());

    // Verify the log entry exists
    String logEntry =
        projectService.getLog(project.getId(), c.getId(), 1, authToken);
    assertTrue(logEntry.contains("ADD_ATTRIBUTE " + attribute.getName()));

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
        c.getLastModified().getTime(), attribute2, false, authToken);
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

    // verify that alternate ID was created and is correctly formed.
    assertNotNull(attribute2.getAlternateTerminologyIds().get(umlsTerminology));
    assertTrue(attribute2.getAlternateTerminologyIds().get(umlsTerminology)
        .startsWith("AT"));

    // verify that attribute2's alternate ID is different from the first one
    assertNotSame(attribute.getAlternateTerminologyIds().get(umlsTerminology),
        attribute2.getAlternateTerminologyIds().get(umlsTerminology));

    // verify the molecular action exists
    pfs = new PfsParameterJpa();
    pfs.setSortField("lastModified");
    pfs.setAscending(false);
    list = contentService.findMolecularActions(c.getId(), null, pfs, authToken);
    assertTrue(list.getCount() > 0);
    ma = list.getObjects().get(0);
    assertNotNull(ma);
    assertTrue(ma.getTerminologyId().equals(c.getTerminologyId()));
    assertTrue(ma.getLastModified().compareTo(startDate) >= 0);
    assertNotNull(ma.getAtomicActions());

    // Verify that ONE atomic actions exists for add attribute (Concept Workflow
    // Status was already set during previous addition)
    pfs.setSortField("idType");
    pfs.setAscending(true);

    atomicActions = contentService
        .findAtomicActions(ma.getId(), null, pfs, authToken).getObjects();
    assertEquals(1, atomicActions.size());
    assertEquals("ATTRIBUTE", atomicActions.get(0).getIdType().toString());
    assertNull(atomicActions.get(0).getOldValue());
    assertNotNull(atomicActions.get(0).getNewValue());

    // Verify the log entry exists
    logEntry = projectService.getLog(project.getId(), c.getId(), 1, authToken);
    assertTrue(logEntry.contains("ADD_ATTRIBUTE " + attribute2.getName()));

    //
    // Test removal
    //

    // remove the first attribute from the concept
    v = metaEditingService.removeAttribute(project.getId(), c.getId(),
        c.getLastModified().getTime(), attribute.getId(), false, authToken);
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
    list = contentService.findMolecularActions(c.getId(), null, pfs, authToken);
    assertTrue(list.getCount() > 0);
    ma = list.getObjects().get(0);
    assertNotNull(ma);
    assertTrue(ma.getTerminologyId().equals(c.getTerminologyId()));
    assertTrue(ma.getLastModified().compareTo(startDate) >= 0);
    assertNotNull(ma.getAtomicActions());

    // Verify that one atomic action exists for remove Attribute
    pfs.setAscending(true);

    atomicActions = contentService
        .findAtomicActions(ma.getId(), null, null, authToken).getObjects();
    assertEquals(atomicActions.size(), 1);
    assertEquals(atomicActions.get(0).getIdType().toString(), "ATTRIBUTE");
    assertNotNull(atomicActions.get(0).getOldValue());
    assertNull(atomicActions.get(0).getNewValue());

    // Verify the log entry exists
    logEntry = projectService.getLog(project.getId(), c.getId(), 1, authToken);
    assertTrue(logEntry.contains("REMOVE_ATTRIBUTE " + attribute.getName()));

    // remove the second attribute from the concept (assume verification of MA,
    // atomic actions, and log entry since we just tested those)
    v = metaEditingService.removeAttribute(project.getId(), c.getId(),
        c.getLastModified().getTime(), attribute2.getId(), false, authToken);
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
    Logger.getLogger(getClass()).debug("Start test");

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

    // add the attribute to the concept
    ValidationResult v = metaEditingService.addAtom(project.getId(), c.getId(),
        c.getLastModified().getTime(), atom, false, authToken);
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
    MolecularActionList list =
        contentService.findMolecularActions(c.getId(), null, pfs, authToken);
    assertTrue(list.getCount() > 0);
    MolecularAction ma = list.getObjects().get(0);
    assertNotNull(ma);
    assertTrue(ma.getTerminologyId().equals(c.getTerminologyId()));
    assertTrue(ma.getLastModified().compareTo(startDate) >= 0);
    assertNotNull(ma.getAtomicActions());

    // Verify that two atomic actions exists for add atom, and update
    // Concept WorkflowStatus

    pfs.setSortField("idType");
    pfs.setAscending(true);

    List<AtomicAction> atomicActions = contentService
        .findAtomicActions(ma.getId(), null, pfs, authToken).getObjects();

    assertEquals(2, atomicActions.size());
    assertEquals("ATOM", atomicActions.get(0).getIdType().toString());
    assertNull(atomicActions.get(0).getOldValue());
    assertNotNull(atomicActions.get(0).getNewValue());
    assertEquals(atomicActions.get(1).getIdType().toString(), "CONCEPT");
    assertNotNull(atomicActions.get(1).getOldValue());
    assertNotNull(atomicActions.get(1).getNewValue());

    // Verify the log entry exists
    String logEntry =
        projectService.getLog(project.getId(), c.getId(), 1, authToken);
    assertTrue(logEntry.contains("ADD_ATOM " + atom.getName()));

    //
    // Add second atom (also ensures alternateTerminologyId increments
    // correctly)
    //

    // Due to MySQL rounding to the second, we must also round our comparison
    // startDate.
    startDate = DateUtils.round(new Date(), Calendar.SECOND);

    // construct a second attribute not present on concept (here, 17
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
    v = metaEditingService.addAtom(project.getId(), c.getId(),
        c.getLastModified().getTime(), atom2, false, authToken);
    assertTrue(v.getErrors().isEmpty());

    // retrieve the concept and check to make sure both attributes are still
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
    list = contentService.findMolecularActions(c.getId(), null, pfs, authToken);
    assertTrue(list.getCount() > 0);
    ma = list.getObjects().get(0);
    assertNotNull(ma);
    assertTrue(ma.getTerminologyId().equals(c.getTerminologyId()));
    assertTrue(ma.getLastModified().compareTo(startDate) >= 0);
    assertNotNull(ma.getAtomicActions());

    // Verify that ONE atomic actions exists for add atom (Concept
    // Workflow Status was already set during previous addition)
    pfs.setSortField("idType");
    pfs.setAscending(true);

    atomicActions = contentService
        .findAtomicActions(ma.getId(), null, pfs, authToken).getObjects();
    assertEquals(1, atomicActions.size());
    assertEquals("ATOM", atomicActions.get(0).getIdType().toString());
    assertNull(atomicActions.get(0).getOldValue());
    assertNotNull(atomicActions.get(0).getNewValue());

    // Verify the log entry exists
    logEntry = projectService.getLog(project.getId(), c.getId(), 1, authToken);
    assertTrue(logEntry.contains("ADD_ATOM " + atom2.getName()));

    //
    // Test removal
    //

    // remove the first atom from the concept
    v = metaEditingService.removeAtom(project.getId(), c.getId(),
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
    list = contentService.findMolecularActions(c.getId(), null, pfs, authToken);
    assertTrue(list.getCount() > 0);
    ma = list.getObjects().get(0);
    assertNotNull(ma);
    assertTrue(ma.getTerminologyId().equals(c.getTerminologyId()));
    assertTrue(ma.getLastModified().compareTo(startDate) >= 0);
    assertNotNull(ma.getAtomicActions());

    // Verify that one atomic action exists for remove Atom
    pfs.setAscending(true);

    atomicActions = contentService
        .findAtomicActions(ma.getId(), null, null, authToken).getObjects();
    assertEquals(atomicActions.size(), 1);
    assertEquals(atomicActions.get(0).getIdType().toString(), "ATOM");
    assertNotNull(atomicActions.get(0).getOldValue());
    assertNull(atomicActions.get(0).getNewValue());

    // Verify the log entry exists
    logEntry = projectService.getLog(project.getId(), c.getId(), 1, authToken);
    assertTrue(logEntry.contains("REMOVE_ATOM " + atom.getName()));

    // remove the second atom from the concept (assume verification of
    // MA, atomic actions, and log entry since we just tested those)
    v = metaEditingService.removeAtom(project.getId(), c.getId(),
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
   * Test add and remove relationship to concept.
   *
   * @throws Exception the exception
   */
  @Test
  public void testAddAndRemoveRelationshipToConcept() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

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

    //
    // Test addition
    //

    // add the relationship to the concept
    ValidationResult v =
        metaEditingService.addRelationship(project.getId(), c.getId(),
            c.getLastModified().getTime(), relationship, false, authToken);
    assertTrue(v.getErrors().isEmpty());

    // retrieve the source concept and check relationships
    c = contentService.getConcept(concept.getId(), project.getId(), authToken);

    RelationshipList relList =
        contentService.findConceptRelationships(c.getTerminologyId(),
            c.getTerminology(), c.getVersion(), null, null, authToken);

    relationship = null;
    for (final Relationship<?, ?> rel : relList.getObjects()) {
      if (rel.getRelationshipType().equals("RN")
          && rel.getTo().getTerminologyId().equals("C0002073")) {
        relationship = (ConceptRelationshipJpa) rel;
      }
    }
    assertNotNull(relationship);

    // retrieve the to concept and check relationships
    c2 = contentService.getConcept(concept2.getId(), project.getId(), authToken);
    
    relList = contentService.findConceptRelationships(c2.getTerminologyId(),
        c2.getTerminology(), c2.getVersion(), null, null, authToken);

    ConceptRelationshipJpa relationship2 = null;
    for (final Relationship<?, ?> rel : relList.getObjects()) {
      if (rel.getFrom().getTerminologyId().equals("C0002073")
          && rel.getTo().getTerminologyId().equals("C0000294")) {
        relationship2 = (ConceptRelationshipJpa) rel;
      }
    }
    assertNotNull(relationship2);

    // verify that alternate ID was created and is correctly formed - RUI assignment was moved to release time.
//    assertNotNull(
//        relationship.getAlternateTerminologyIds().get(umlsTerminology));
//    assertTrue(relationship.getAlternateTerminologyIds().get(umlsTerminology)
//        .startsWith("R"));

    // verify the molecular action exists
    PfsParameterJpa pfs = new PfsParameterJpa();
    pfs.setSortField("lastModified");
    pfs.setAscending(false);
    MolecularActionList list =
        contentService.findMolecularActions(c.getId(), null, pfs, authToken);

    assertTrue(list.getCount() > 0);
    MolecularAction ma = list.getObjects().get(0);
    assertNotNull(ma);
    assertTrue(ma.getTerminologyId().equals(c.getTerminologyId()));
    assertTrue(ma.getLastModified().compareTo(startDate) >= 0);
    assertNotNull(ma.getAtomicActions());

    // Verify that three atomic actions exists for add relationship, and update
    // Concept WorkflowStatus for both affected concepts

    pfs.setSortField("idType");
    pfs.setAscending(true);

    List<AtomicAction> atomicActions = contentService
        .findAtomicActions(ma.getId(), null, pfs, authToken).getObjects();
    assertEquals(3, atomicActions.size());
    assertEquals(atomicActions.get(0).getIdType().toString(), "CONCEPT");
    assertNotNull(atomicActions.get(0).getOldValue());
    assertNotNull(atomicActions.get(0).getNewValue());
    assertEquals("RELATIONSHIP", atomicActions.get(1).getIdType().toString());
    assertNull(atomicActions.get(1).getOldValue());
    assertNotNull(atomicActions.get(1).getNewValue());
    assertEquals("RELATIONSHIP", atomicActions.get(2).getIdType().toString());
    assertNull(atomicActions.get(2).getOldValue());
    assertNotNull(atomicActions.get(2).getNewValue());

    // Verify the log entry exists
    String logEntry =
        projectService.getLog(project.getId(), c.getId(), 1, authToken);
    assertTrue(logEntry.contains("ADD_RELATIONSHIP " + relationship));

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

    //
    // add the second relationship to the concept
    //

    // add the relationship to the concept
    v = metaEditingService.addRelationship(project.getId(), c.getId(),
        c.getLastModified().getTime(), relationship3, false, authToken);
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
          && rel.getTo().getTerminologyId().equals("C0002073")) {
        relationship = (ConceptRelationshipJpa) rel;
      }
      if (rel.getRelationshipType().equals("RB")
          && rel.getTo().getTerminologyId().equals("C0065642")) {
        relationship3 = (ConceptRelationshipJpa) rel;
      }
    }
    assertNotNull(relationship);
    assertNotNull(relationship3);

    // verify that alternate ID was created and is correctly formed - RUI assignment was moved to release time.
//    assertNotNull(
//        relationship3.getAlternateTerminologyIds().get(umlsTerminology));
//    assertTrue(relationship3.getAlternateTerminologyIds().get(umlsTerminology)
//        .startsWith("R"));

    // verify that relationship2's alternate ID is different from the first one - RUI assignment was moved to release time.
//    assertNotSame(
//        relationship.getAlternateTerminologyIds().get(umlsTerminology),
//        relationship3.getAlternateTerminologyIds().get(umlsTerminology));

    // verify the molecular action exists
    pfs = new PfsParameterJpa();
    pfs.setSortField("lastModified");
    pfs.setAscending(false);
    list = contentService.findMolecularActions(c.getId(), null, pfs, authToken);
    assertTrue(list.getCount() > 0);
    ma = list.getObjects().get(0);
    assertNotNull(ma);
    assertTrue(ma.getTerminologyId().equals(c.getTerminologyId()));
    assertTrue(ma.getLastModified().compareTo(startDate) >= 0);
    assertNotNull(ma.getAtomicActions());

    // Verify that TWO atomic actions exists the two add relationships (Concept
    // Workflow Status for FROM concept was already set during previous
    // addition, and Workflow Status for To Concept is not affected.
    // different TO concept will also be updated
    pfs.setSortField("idType");
    pfs.setAscending(true);

    atomicActions = contentService
        .findAtomicActions(ma.getId(), null, pfs, authToken).getObjects();
    assertEquals(2, atomicActions.size());
    assertEquals("RELATIONSHIP", atomicActions.get(0).getIdType().toString());
    assertNull(atomicActions.get(0).getOldValue());
    assertNotNull(atomicActions.get(0).getNewValue());
    assertEquals("RELATIONSHIP", atomicActions.get(1).getIdType().toString());
    assertNull(atomicActions.get(1).getOldValue());
    assertNotNull(atomicActions.get(1).getNewValue());

    // Verify the log entry exists
    logEntry = projectService.getLog(project.getId(), c.getId(), 1, authToken);
    assertTrue(
        logEntry.contains("ADD_RELATIONSHIP " + relationship3));

    //
    // Test removal
    //

    // remove the first relationship from the concept
    v = metaEditingService.removeRelationship(project.getId(), c.getId(),
        c.getLastModified().getTime(), relationship.getId(), false, authToken);
    assertTrue(v.getErrors().isEmpty());

    c = contentService.getConcept(concept.getId(), project.getId(), authToken);
    relList = contentService.findConceptRelationships(c.getTerminologyId(),
        c.getTerminology(), c.getVersion(), null, null, authToken);

    boolean relationshipPresent = false;
    for (final Relationship<?, ?> rel : relList.getObjects()) {
      if (rel.getRelationshipType().equals("RN")
          && rel.getTo().getTerminologyId().equals("C0002073") && 
          rel.getFrom().getId().equals(c.getId())) {
        relationshipPresent = true;
      }
    }
    assertTrue(!relationshipPresent);

    // verify the molecular action exists
    pfs = new PfsParameterJpa();
    pfs.setSortField("lastModified");
    pfs.setAscending(false);
    list = contentService.findMolecularActions(c.getId(), null, pfs, authToken);
    assertTrue(list.getCount() > 0);
    ma = list.getObjects().get(0);
    assertNotNull(ma);
    assertTrue(ma.getTerminologyId().equals(c.getTerminologyId()));
    assertTrue(ma.getLastModified().compareTo(startDate) >= 0);
    assertNotNull(ma.getAtomicActions());

    // Verify that two atomic actions exist for remove Relationship and inverse
    pfs.setAscending(true);

    atomicActions = contentService
        .findAtomicActions(ma.getId(), null, null, authToken).getObjects();
    assertEquals(2, atomicActions.size());
    assertEquals("RELATIONSHIP", atomicActions.get(0).getIdType().toString());
    assertNotNull(atomicActions.get(0).getOldValue());
    assertNull(atomicActions.get(0).getNewValue());
    assertEquals("RELATIONSHIP", atomicActions.get(1).getIdType().toString());
    assertNotNull(atomicActions.get(1).getOldValue());
    assertNull(atomicActions.get(1).getNewValue());

    // Verify the log entry exists
    logEntry = projectService.getLog(project.getId(), c.getId(), 1, authToken);
    //Substringing relationship because removing it alters the lastModified
    assertTrue(logEntry.contains("REMOVE_RELATIONSHIP " + relationship.toString().substring(0, 80)));

    // remove the second relationship from the concept (assume verification of
    // MA,
    // atomic actions, and log entry since we just tested those)
    v = metaEditingService.removeRelationship(project.getId(), c.getId(),
        c.getLastModified().getTime(), relationship3.getId(), false, authToken);
    assertTrue(v.getErrors().isEmpty());

    // retrieve the concept and check relationships
    c = contentService.getConcept(concept.getId(), project.getId(), authToken);

    relList = contentService.findConceptRelationships(c.getTerminologyId(),
        c.getTerminology(), c.getVersion(), null, null, authToken);

    boolean relationship3Present = false;
    for (final Relationship<?, ?> rel : relList.getObjects()) {
      if (rel.getRelationshipType().equals("RB")
          && rel.getTo().getTerminologyId().equals("C0065642") && 
          rel.getFrom().getId().equals(c.getId())) {
        relationship3Present = true;
      }
    }
    assertTrue(!relationship3Present);

  }

  /**
   * Teardown.
   *
   * @throws Exception the exception
   */
  @Override
  @After
  public void teardown() throws Exception {

    // Copy existing concept to avoid messing with actual database data.
    IntegrationTestClientRest testService =
        new IntegrationTestClientRest(ConfigUtility.getConfigProperties());
    testService.removeConcept(concept.getId(), authToken);

    testService =
        new IntegrationTestClientRest(ConfigUtility.getConfigProperties());
    testService.removeConcept(concept2.getId(), authToken);

    testService =
        new IntegrationTestClientRest(ConfigUtility.getConfigProperties());
    testService.removeConcept(concept3.getId(), authToken);

    // logout
    securityService.logout(authToken);

  }

}
