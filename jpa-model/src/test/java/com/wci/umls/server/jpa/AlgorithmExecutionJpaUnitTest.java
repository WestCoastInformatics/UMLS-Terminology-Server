/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.AlgorithmExecution;
import com.wci.umls.server.AlgorithmParameter;
import com.wci.umls.server.ProcessExecution;
import com.wci.umls.server.Project;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.CopyConstructorTester;
import com.wci.umls.server.helpers.EqualsHashcodeTester;
import com.wci.umls.server.helpers.GetterSetterTester;
import com.wci.umls.server.helpers.ProxyTester;
import com.wci.umls.server.helpers.XmlSerializationTester;
import com.wci.umls.server.jpa.helpers.IndexedFieldTester;
import com.wci.umls.server.jpa.helpers.NullableFieldTester;

/**
 * Unit testing for {@link AlgorithmConfigJpa}.
 */
public class AlgorithmExecutionJpaUnitTest extends ModelUnitSupport {

  /** The model object to test. */
  private AlgorithmExecutionJpa object;

  /** The test fixture p1. */
  private Project p1;

  /** The test fixture p2. */
  private Project p2;

  /** The test fixture p1. */
  private ProcessExecution pe1;

  /** The test fixture p2. */
  private ProcessExecution pe2;

  /** The test fixture l1. */
  private List<AlgorithmParameter> l1;

  /** The test fixture l2. */
  private List<AlgorithmParameter> l2;

  /** The test fixture m1. */
  private Map<String, String> m1;

  /** The test fixture m2. */
  private Map<String, String> m2;

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
    object = new AlgorithmExecutionJpa();

    p1 = new ProjectJpa();
    p1.setId(1L);
    p2 = new ProjectJpa();
    p2.setId(2L);
    pe1 = new ProcessExecutionJpa();
    pe1.setId(1L);
    pe2 = new ProcessExecutionJpa();
    pe2.setId(2L);
    ProxyTester tester = new ProxyTester(new AlgorithmParameterJpa());
    l1 = new ArrayList<>();
    l1.add((AlgorithmParameter) tester.createObject(1));
    l2 = new ArrayList<>();
    l2.add((AlgorithmParameter) tester.createObject(2));

    m1 = new HashMap<>();
    m1.put("1", "1");
    m2 = new HashMap<>();
    m2.put("1", "2");
    m2.put("3", "4");
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
    tester.exclude("processId");
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
    tester.include("algorithmKey");
    tester.include("name");
    tester.include("project");
    tester.include("description");
    tester.include("process");
    tester.include("algorithmConfigId");
    tester.include("activityId");
    tester.include("properties");

    // This is not a real getter, skip it
    tester.exclude("processId");
    tester.exclude("projectId");

    // Set up objects
    tester.proxy(List.class, 1, l1);
    tester.proxy(List.class, 2, l2);
    tester.proxy(Map.class, 1, m1);
    tester.proxy(Map.class, 2, m2);
    tester.proxy(Project.class, 1, p1);
    tester.proxy(Project.class, 2, p2);
    tester.proxy(ProcessExecution.class, 1, pe1);
    tester.proxy(ProcessExecution.class, 2, pe2);

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

    // Set up objects
    tester.proxy(List.class, 1, l1);
    tester.proxy(List.class, 2, l2);
    tester.proxy(Map.class, 1, m1);
    tester.proxy(Map.class, 2, m2);
    tester.proxy(Project.class, 1, p1);
    tester.proxy(Project.class, 2, p2);
    tester.proxy(ProcessExecution.class, 1, pe1);
    tester.proxy(ProcessExecution.class, 2, pe2);

    assertTrue(tester.testCopyConstructor(AlgorithmExecution.class));
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
    // Set up objects
    tester.proxy(List.class, 1, l1);
    tester.proxy(Project.class, 1, p1);
    tester.proxy(ProcessExecution.class, 1, pe1);
    assertTrue(tester.testXmlSerialization());
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
    tester.include("name");
    tester.include("description");
    tester.include("algorithmKey");
    tester.include("algorithmConfigId");
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

    // Test analyzed fields - none
    IndexedFieldTester tester = new IndexedFieldTester(object);
    assertTrue(tester.testAnalyzedIndexedFields());

    // Test non analyzed fields - none
    tester = new IndexedFieldTester(object);
    // No indexed Fields
    tester.include("projectId");
    tester.include("algorithmConfigId");
    tester.include("processId");
    tester.include("activityId");
    tester.include("failDate");
    tester.include("finishDate");
    tester.include("startDate");
    assertTrue(tester.testNotAnalyzedIndexedFields());

  }

  /**
   * Test XML transient.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlTransient() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    XmlSerializationTester tester = new XmlSerializationTester(object);
    // Set up objects
    tester.proxy(List.class, 1, l1);
    tester.proxy(Project.class, 1, p1);
    tester.proxy(ProcessExecution.class, 1, pe1);
    final AlgorithmExecution config =
        (AlgorithmExecution) tester.createObject(1);
    final String xml = ConfigUtility.getStringForGraph(config);
    assertTrue(xml.contains("<processId>"));
    assertFalse(xml.contains("<process>"));
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
