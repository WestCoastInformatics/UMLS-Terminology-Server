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
import com.wci.umls.server.jpa.worfklow.WorkflowBinDefinitionJpa;
import com.wci.umls.server.jpa.worfklow.WorkflowBinDefinitionsJpa;
import com.wci.umls.server.model.workflow.WorkflowBinDefinition;
import com.wci.umls.server.model.workflow.WorkflowBinDefinitions;

/**
 * Unit testing for {@link WorkflowBinDefinitionsJpa}.
 */
public class WorkflowBinDefinitionsJpaUnitTest {

  /** The model object to test. */
  private WorkflowBinDefinitions object;

  /** The m1. */
  private WorkflowBinDefinition m1;

  /** The m2. */
  private WorkflowBinDefinition m2;

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
    object = new WorkflowBinDefinitionsJpa();
    ProxyTester tester2 = new ProxyTester(new WorkflowBinDefinitionJpa());
    m1 = (WorkflowBinDefinition) tester2.createObject(1);
    m2 = (WorkflowBinDefinition) tester2.createObject(2);
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

    tester.include("timestamp");
    tester.include("lastModified");
    tester.include("lastModifiedBy");
    tester.include("type");
    tester.include("mutuallyExclusive");
    tester.include("lastPartitionTime");

    tester.proxy(WorkflowBinDefinition.class, 1, m1);
    tester.proxy(WorkflowBinDefinition.class, 2, m2);

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
    tester.proxy(WorkflowBinDefinition.class, 1, m1);
    tester.proxy(WorkflowBinDefinition.class, 2, m2);
    assertTrue(tester.testCopyConstructorDeep(WorkflowBinDefinitions.class));

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
    tester.include("mutuallyExclusive");
    tester.include("lastPartitionTime");

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
    //assertTrue(tester.testAnalyzedIndexedFields());

    // Test non analyzed fields
    tester = new IndexedFieldTester(object);
    tester.include("lastModifiedBy");
    tester.include("type");

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