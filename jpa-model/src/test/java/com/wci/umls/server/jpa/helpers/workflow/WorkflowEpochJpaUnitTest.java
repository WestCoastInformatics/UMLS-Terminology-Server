/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers.workflow;

import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.helpers.CopyConstructorTester;
import com.wci.umls.server.helpers.EqualsHashcodeTester;
import com.wci.umls.server.helpers.GetterSetterTester;
import com.wci.umls.server.helpers.ProxyTester;
import com.wci.umls.server.helpers.XmlSerializationTester;
import com.wci.umls.server.jpa.helpers.IndexedFieldTester;
import com.wci.umls.server.jpa.helpers.NullableFieldTester;
import com.wci.umls.server.jpa.worfklow.WorkflowBinJpa;
import com.wci.umls.server.jpa.worfklow.WorkflowEpochJpa;
import com.wci.umls.server.model.workflow.WorkflowBin;
import com.wci.umls.server.model.workflow.WorkflowEpoch;

/**
 * Unit testing for {@link WorkflowEpochJpa}.
 */
public class WorkflowEpochJpaUnitTest {

  /** The model object to test. */
  private WorkflowEpochJpa object;

  /** The m1. */
  private WorkflowBin m1;

  /** The m2. */
  private WorkflowBin m2;

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
    object = new WorkflowEpochJpa();
    ProxyTester tester2 = new ProxyTester(new WorkflowBinJpa());
    m1 = (WorkflowBinJpa) tester2.createObject(1);
    m2 = (WorkflowBinJpa) tester2.createObject(2);
  }

  /**
   * Test getter and setter methods of model object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelGetSet041() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelGetSet041");
    GetterSetterTester tester = new GetterSetterTester(object);
    tester.test();
  }

  /**
   * Test equals and hascode methods.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelEqualsHashcode041() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelEqualsHashcode041");
    EqualsHashcodeTester tester = new EqualsHashcodeTester(object);
    tester.include("suppressible");
    tester.include("obsolete");
    tester.include("publishable");
    tester.include("published");
    tester.include("terminology");
    tester.include("terminologyId");
    tester.include("version");

    tester.include("name");
    tester.include("complexity");
    tester.include("fromComplexity");
    tester.include("toComplexity");
    tester.include("fromExhaustive");
    tester.include("toExhaustive");
    tester.include("type");
    tester.include("fromTerminology");
    tester.include("toTerminology");
    tester.include("fromVersion");
    tester.include("toVersion");

    tester.proxy(WorkflowEpoch.class, 1, m1);
    tester.proxy(WorkflowEpoch.class, 2, m2);

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
  public void testModelDeepCopy041() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelDeepCopy041");

    CopyConstructorTester tester = new CopyConstructorTester(object);
    tester.proxy(WorkflowEpoch.class, 1, m1);
    tester.proxy(WorkflowEpoch.class, 2, m2);
    assertTrue(tester.testCopyConstructorDeep(WorkflowEpoch.class));

  }

  /**
   * Test XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlSerialization041() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelXmlSerialization041");
    XmlSerializationTester tester = new XmlSerializationTester(object);
    assertTrue(tester.testXmlSerialization());
  }

  /**
   * Test not null fields.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelNotNullField041() throws Exception {
    NullableFieldTester tester = new NullableFieldTester(object);
    tester.include("timestamp");
    tester.include("lastModified");
    tester.include("lastModifiedBy");
    tester.include("suppressible");
    tester.include("obsolete");
    tester.include("published");
    tester.include("publishable");
    tester.include("terminology");
    tester.include("terminologyId");
    tester.include("version");
    tester.include("name");

    assertTrue(tester.testNotNullFields());
  }

  /**
   * Test field indexing.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelIndexedFields041() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelIndexedFields041");

    // Test analyzed fields
    IndexedFieldTester tester = new IndexedFieldTester(object);
    tester.include("name");
    assertTrue(tester.testAnalyzedIndexedFields());

    // Test non analyzed fields
    assertTrue(tester.testAnalyzedIndexedFields());
    tester = new IndexedFieldTester(object);
    tester.include("lastModified");
    tester.include("lastModifiedBy");
    tester.include("suppressible");
    tester.include("obsolete");
    tester.include("published");
    tester.include("publishable");
    tester.include("terminologyId");
    tester.include("terminology");
    tester.include("version");
    tester.include("branch");
    tester.include("nameSort");
    tester.include("fromTerminology");
    tester.include("fromVersion");
    tester.include("toTerminology");
    tester.include("toVersion");

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
//