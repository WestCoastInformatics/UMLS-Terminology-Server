/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers.workflow;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.Project;
import com.wci.umls.server.UserPreferences;
import com.wci.umls.server.helpers.CopyConstructorTester;
import com.wci.umls.server.helpers.GetterSetterTester;
import com.wci.umls.server.helpers.ProxyTester;
import com.wci.umls.server.helpers.XmlSerializationTester;
import com.wci.umls.server.jpa.ModelUnitSupport;
import com.wci.umls.server.jpa.ProjectJpa;
import com.wci.umls.server.jpa.UserPreferencesJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.helpers.IndexedFieldTester;
import com.wci.umls.server.jpa.helpers.NullableFieldTester;
import com.wci.umls.server.jpa.worfklow.TrackingRecordJpa;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.workflow.TrackingRecord;

/**
 * Unit testing for {@link TrackingRecordJpa}.
 */
public class TrackingRecordJpaUnitTest extends ModelUnitSupport {

  /** The model object to test. */
  private TrackingRecordJpa object;

  /** the test fixture c1 */
  private Concept c1;

  /** the test fixture c2 */
  private Concept c2;

  /** the test fixture p1 */
  private Project p1;

  /** the test fixture p2 */
  private Project p2;

  /**
   * Setup class.
   */
  @BeforeClass
  public static void setupClass() {
    // do nothing
  }

  /**
   * Setup.
   * @throws Exception
   */
  @Before
  public void setup() throws Exception {
    object = new TrackingRecordJpa();

    ProxyTester tester = new ProxyTester(new ConceptJpa());
    c1 = (ConceptJpa) tester.createObject(1);
    c2 = (ConceptJpa) tester.createObject(2);

    tester = new ProxyTester(new ProjectJpa());
    p1 = (ProjectJpa) tester.createObject(1);
    p2 = (ProjectJpa) tester.createObject(2);

  }

  /**
   * Test getter and setter methods of model object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelGetSet041() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    GetterSetterTester tester = new GetterSetterTester(object);
    tester.exclude("conceptId");
    tester.test();
  }



  /**
   * Test copy constructor.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelCopy041() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    CopyConstructorTester tester = new CopyConstructorTester(object);
    tester.proxy(Concept.class, 1, c1);
    tester.proxy(Concept.class, 2, c2);
    tester.proxy(Project.class, 1, p1);
    tester.proxy(Project.class, 2, p2);

    assertTrue(tester.testCopyConstructor(TrackingRecord.class));
  }

  /**
   * Test XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlSerialization041() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    XmlSerializationTester tester = new XmlSerializationTester(object);

    Concept concept = new ConceptJpa();
    concept.setId(1L);
    Project project = new ProjectJpa();
    project.setId(1L);
    UserPreferences prefs = new UserPreferencesJpa();
    prefs.setId(1L);
    tester.proxy(Concept.class, 1, concept);
    tester.proxy(Project.class, 1, project);
    assertTrue(tester.testXmlSerialization());
  }

  /**
   * Test not null fields.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelNotNullField041() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    NullableFieldTester tester = new NullableFieldTester(object);
    tester.include("lastModified");
    tester.include("lastModifiedBy");
    tester.include("forAuthoring");
    tester.include("forReview");
    tester.include("revision");
    tester.include("timestamp");
    assertTrue(tester.testNotNullFields());
  }

  /**
   * Test field indexing.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelIndexedFields041() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Test analyzed fields
    IndexedFieldTester tester = new IndexedFieldTester(object);
    tester.include("authors");
    tester.include("reviewers");
    tester.include("conceptName");
    assertTrue(tester.testAnalyzedIndexedFields());

    // Test non analyzed fields
    tester = new IndexedFieldTester(object);
    tester.include("lastModified");
    tester.include("lastModifiedBy");
    tester.include("forReview");
    tester.include("revision");
    tester.include("forAuthoring");
    tester.include("conceptId");
    tester.include("conceptTerminologyId");
    tester.include("conceptNameSort");
    tester.include("workflowStatus");
    assertTrue(tester.testNotAnalyzedIndexedFields());

    
  }

  /**
   * Teardown.
   */
  @After
  public void teardown() {
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
