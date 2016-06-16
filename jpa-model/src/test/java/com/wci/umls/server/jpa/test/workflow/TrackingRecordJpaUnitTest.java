/*
 * Copyright 2015 West Coast Informatics, LLC
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

import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.CopyConstructorTester;
import com.wci.umls.server.helpers.EqualsHashcodeTester;
import com.wci.umls.server.helpers.GetterSetterTester;
import com.wci.umls.server.helpers.ProxyTester;
import com.wci.umls.server.helpers.XmlSerializationTester;
import com.wci.umls.server.jpa.ModelUnitSupport;
import com.wci.umls.server.jpa.helpers.IndexedFieldTester;
import com.wci.umls.server.jpa.helpers.NullableFieldTester;
import com.wci.umls.server.jpa.worfklow.TrackingRecordJpa;
import com.wci.umls.server.jpa.worfklow.WorkflowBinJpa;
import com.wci.umls.server.jpa.worfklow.WorklistJpa;
import com.wci.umls.server.model.workflow.TrackingRecord;
import com.wci.umls.server.model.workflow.WorkflowBin;
import com.wci.umls.server.model.workflow.Worklist;

/**
 * Unit testing for {@link TrackingRecordJpa}.
 */
public class TrackingRecordJpaUnitTest extends ModelUnitSupport {

  /** The model object to test. */
  private TrackingRecordJpa object;

  /** The fixture l1. */
  private List<String> l1;

  /** The fixture l2. */
  private List<String> l2;

  /** The fixture w1. */
  private Worklist w1;

  /** The fixture w2. */
  private Worklist w2;

  /** The fixture b1. */
  private WorkflowBin b1;

  /** The fixture b2. */
  private WorkflowBin b2;

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
    object = new TrackingRecordJpa();

    l1 = new ArrayList<>();
    l1.add("1");
    l2 = new ArrayList<>();
    l2.add("2");

    final ProxyTester tester = new ProxyTester(new WorklistJpa());
    w1 = (WorklistJpa) tester.createObject(1);
    w2 = (WorklistJpa) tester.createObject(2);

    final ProxyTester tester2 = new ProxyTester(new WorkflowBinJpa());
    b1 = (WorkflowBinJpa) tester2.createObject(1);
    b2 = (WorkflowBinJpa) tester2.createObject(2);

    // for xml serialization
    object.setTerminologyIds(l1);
    object.setWorklist(w1);
    object.setWorkflowBin(b1);

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
    tester.exclude("conceptId");
    tester.exclude("worklistId");
    tester.exclude("workflowBinId");
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
    tester.include("clusterId");
    tester.include("clusterType");
    tester.include("terminology");
    tester.include("terminologyIds");
    tester.include("version");

    tester.proxy(List.class, 1, l1);
    tester.proxy(List.class, 2, l2);
    tester.proxy(Worklist.class, 1, w1);
    tester.proxy(Worklist.class, 2, w2);
    tester.proxy(WorkflowBin.class, 1, b1);
    tester.proxy(WorkflowBin.class, 2, b2);

    assertTrue(tester.testIdentityFieldEquals());
    assertTrue(tester.testNonIdentityFieldEquals());
    assertTrue(tester.testIdentityFieldNotEquals());
    assertTrue(tester.testIdentityFieldHashcode());
    assertTrue(tester.testNonIdentityFieldHashcode());
    assertTrue(tester.testIdentityFieldDifferentHashcode());
  }

  /**
   * Test copy constructor.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelCopy() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    CopyConstructorTester tester = new CopyConstructorTester(object);
    tester.proxy(List.class, 1, l1);
    tester.proxy(List.class, 2, l2);
    tester.proxy(Worklist.class, 1, w1);
    tester.proxy(Worklist.class, 2, w2);
    tester.proxy(WorkflowBin.class, 1, b1);
    tester.proxy(WorkflowBin.class, 2, b2);
    assertTrue(tester.testCopyConstructor(TrackingRecord.class));
  }

  /**
   * Test XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlSerialization() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    XmlSerializationTester tester = new XmlSerializationTester(object);

    final Worklist w1 = new WorklistJpa();
    w1.setId(1L);
    final WorkflowBin b1 = new WorkflowBinJpa();
    b1.setId(1L);

    tester.proxy(Worklist.class, 1, w1);
    tester.proxy(WorkflowBin.class, 1, b1);
    assertTrue(tester.testXmlSerialization());
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
    assertTrue(xml.contains("<workflowBinId>"));
    assertTrue(xml.contains("<worklistId>"));
    assertFalse(xml.contains("<workflowBin>"));
    assertFalse(xml.contains("<worklist>"));
  }

  /**
   * Test not null fields.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelNotNullField() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    NullableFieldTester tester = new NullableFieldTester(object);
    tester.include("lastModified");
    tester.include("lastModifiedBy");
    tester.include("timestamp");
    tester.include("clusterId");
    tester.include("clusterType");
    tester.include("terminology");
    tester.include("version");
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
    tester.include("terminologyIds");
    assertTrue(tester.testAnalyzedIndexedFields());

    // Test non analyzed fields
    tester = new IndexedFieldTester(object);
    tester.include("lastModifiedBy");
    tester.include("clusterId");
    tester.include("clusterType");
    tester.include("terminology");
    tester.include("version");
    tester.include("projectId");
    tester.include("worklistId");
    tester.include("workflowBinId");
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
