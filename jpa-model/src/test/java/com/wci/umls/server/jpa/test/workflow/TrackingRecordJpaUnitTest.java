/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.test.workflow;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

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
import com.wci.umls.server.model.workflow.TrackingRecord;

/**
 * Unit testing for {@link TrackingRecordJpa}.
 */
public class TrackingRecordJpaUnitTest extends ModelUnitSupport {

  /** The model object to test. */
  private TrackingRecordJpa object;

  /** The fixture l1. */
  private Set<Long> l1;

  /** The fixture l2. */
  private Set<Long> l2;

  /** The fixture s1. */
  private Set<Long> s1;

  /** The fixture s2. */
  private Set<Long> s2;

  /** The fixture p1. */
  private Project p1;

  /** The fixture p2. */
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
    object = new TrackingRecordJpa();

    l1 = new HashSet<>();
    l1.add(1L);
    l2 = new HashSet<>();
    l2.add(2L);
    l2.add(3L);

    s1 = new HashSet<>();
    s1.add(1L);
    s2 = new HashSet<>();
    s2.add(2L);
    s2.add(3L);

    ProxyTester tester = new ProxyTester(new ProjectJpa());
    p1 = (Project) tester.createObject(1);
    p2 = (Project) tester.createObject(2);

    // for xml serialization
    object.setComponentIds(l1);
    object.setOrigConceptIds(s1);

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
    tester.include("clusterId");
    tester.include("clusterType");
    tester.include("terminology");
    tester.include("version");
    tester.include("componentIds");
    tester.include("origConceptIds");
    tester.include("project");
    tester.include("workflowBinName");
    tester.include("worklistName");

    tester.proxy(Set.class, 1, l1);
    tester.proxy(Set.class, 2, l2);
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
   * Test copy constructor.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelCopy() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    CopyConstructorTester tester = new CopyConstructorTester(object);
    tester.proxy(Set.class, 1, l1);
    tester.proxy(Set.class, 2, l2);
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
  public void testModelXmlSerialization() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    XmlSerializationTester tester = new XmlSerializationTester(object);

    Project p1 = new ProjectJpa();
    p1.setId(1L);
    tester.proxy(Project.class, 1, p1);
    tester.proxy(Set.class, 1, s1);
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
    object.setProject(p1);
    String xml = ConfigUtility.getStringForGraph(object);
    assertTrue(xml.contains("<projectId>"));
    assertFalse(xml.contains("<project>"));
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
    tester.include("componentIds");
    tester.include("origConceptIds");
    assertTrue(tester.testAnalyzedIndexedFields());

    // Test non analyzed fields
    tester = new IndexedFieldTester(object);
    tester.include("id");
//    tester.include("lastModified");
    tester.include("lastModifiedBy");
    tester.include("clusterId");
    tester.include("clusterIdSort");
    tester.include("clusterType");
    tester.include("terminology");
    tester.include("version");
    tester.include("projectId");
    tester.include("worklistName");
    tester.include("workflowBinName");
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
