/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.test.workflow;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.Project;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.CopyConstructorTester;
import com.wci.umls.server.helpers.EqualsHashcodeTester;
import com.wci.umls.server.helpers.GetterSetterTester;
import com.wci.umls.server.helpers.ProxyTester;
import com.wci.umls.server.helpers.XmlSerializationTester;
import com.wci.umls.server.jpa.ModelUnitSupport;
import com.wci.umls.server.jpa.ProjectJpa;
import com.wci.umls.server.jpa.helpers.IndexedFieldTester;
import com.wci.umls.server.jpa.helpers.NullableFieldTester;
import com.wci.umls.server.jpa.worfklow.TrackingRecordJpa;
import com.wci.umls.server.jpa.worfklow.WorkflowBinJpa;
import com.wci.umls.server.jpa.worfklow.WorkflowEpochJpa;
import com.wci.umls.server.model.workflow.TrackingRecord;
import com.wci.umls.server.model.workflow.WorkflowBin;

/**
 * Unit testing for {@link WorkflowEpochJpa}.
 */
public class WorkflowBinJpaUnitTest extends ModelUnitSupport {

  /** The model object to test. */
  private WorkflowBinJpa object;

  /** The fixture l1. */
  private List<TrackingRecord> l1;

  /** The fixture l2. */
  private List<TrackingRecord> l2;

  /** The fixture p1. */
  private Project p1;

  /** The fdixture p2. */
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
   *
   * @throws Exception the exception
   */
  @Before
  public void setup() throws Exception {
    object = new WorkflowBinJpa();

    l1 = new ArrayList<>();
    l2 = new ArrayList<>();
    ProxyTester tester = new ProxyTester(new TrackingRecordJpa());
    l1.add((TrackingRecord) tester.createObject(1));
    l1.add((TrackingRecord) tester.createObject(2));

    final ProxyTester tester2 = new ProxyTester(new ProjectJpa());
    p1 = (ProjectJpa) tester2.createObject(1);
    p2 = (ProjectJpa) tester2.createObject(2);

    object.setProject(p1);
  }

  /**
   * Test getter and setter methods of model object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelGetSet() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    GetterSetterTester tester = new GetterSetterTester(object);
    tester.exclude("projectId");
    tester.test();
  }

  /**
   * Test equals and hascode methods.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelEqualsHashcode() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    EqualsHashcodeTester tester = new EqualsHashcodeTester(object);

    tester.include("description");
    tester.include("editable");
    tester.include("required");
    tester.include("name");
    tester.include("rank");
    tester.include("type");
    tester.include("terminology");
    tester.include("terminologyId");
    tester.include("version");

    tester.proxy(List.class, 1, l1);
    tester.proxy(List.class, 2, l2);
    tester.proxy(Project.class, 1, p1);
    tester.proxy(Project.class, 2, p2);
    assertTrue(tester.testIdentityFieldEquals());
    assertTrue(tester.testNonIdentityFieldEquals());
    assertTrue(tester.testIdentityFieldNotEquals());
    assertTrue(tester.testIdentityFieldHashcode());
    assertTrue(tester.testNonIdentityFieldHashcode());
    assertTrue(tester.testIdentityFieldDifferentHashcode());
  }

  /**
   * Test deep copy constructor.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelDeepCopy() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelDeepCopy");

    CopyConstructorTester tester = new CopyConstructorTester(object);
    tester.proxy(List.class, 1, l1);
    tester.proxy(List.class, 2, l2);
    tester.proxy(Project.class, 1, p1);
    tester.proxy(Project.class, 2, p2);
    assertTrue(tester.testCopyConstructorDeep(WorkflowBin.class));

  }

  /**
   * Test XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlSerialization() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelXmlSerialization");
    XmlSerializationTester tester = new XmlSerializationTester(object);
    final Project p1 = new ProjectJpa();
    p1.setId(1L);
    tester.proxy(Project.class, 1, p1);
    assertTrue(tester.testXmlSerialization());
  }

  /**
   * Test not null fields.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelNotNullField() throws Exception {
    NullableFieldTester tester = new NullableFieldTester(object);
    tester.include("timestamp");
    tester.include("lastModified");
    tester.include("lastModifiedBy");
    tester.include("terminology");
    tester.include("terminologyId");
    tester.include("version");

    tester.include("name");
    tester.include("description");
    tester.include("type");
    tester.include("rank");
    tester.include("editable");
    tester.include("required");
    tester.include("creationTime");

    assertTrue(tester.testNotNullFields());
  }

  /**
   * Test field indexing.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelIndexedFields() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Test analyzed fields
    IndexedFieldTester tester = new IndexedFieldTester(object);
    // no analyzed fields
    assertTrue(tester.testAnalyzedIndexedFields());

    // Test non analyzed fields
    tester = new IndexedFieldTester(object);
    tester.include("lastModifiedBy");
    tester.include("name");
    tester.include("type");
    tester.include("editable");
    tester.include("required");
    tester.include("terminologyId");
    tester.include("terminology");
    tester.include("version");
    tester.include("projectId");

    assertTrue(tester.testNotAnalyzedIndexedFields());
  }

  /**
   * Test XML transient
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlTransient() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    String xml = ConfigUtility.getStringForGraph(object);
    assertTrue(xml.contains("<projectId>"));
    assertFalse(xml.contains("<project>"));
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
//