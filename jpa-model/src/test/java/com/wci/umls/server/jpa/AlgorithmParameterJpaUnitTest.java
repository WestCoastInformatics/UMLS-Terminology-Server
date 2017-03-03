/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.AlgorithmParameter;
import com.wci.umls.server.helpers.CopyConstructorTester;
import com.wci.umls.server.helpers.EqualsHashcodeTester;
import com.wci.umls.server.helpers.GetterSetterTester;
import com.wci.umls.server.helpers.XmlSerializationTester;
import com.wci.umls.server.jpa.helpers.IndexedFieldTester;

/**
 * Unit testing for {@link AlgorithmParameterJpa}.
 */
public class AlgorithmParameterJpaUnitTest extends ModelUnitSupport {

  /** The model object to test. */
  private AlgorithmParameterJpa object;

  /** The test fixture l1. */
  private List<String> l1;

  /** The test fixture l. */
  private List<String> l2;

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
    object = new AlgorithmParameterJpa();

    l1 = new ArrayList<>();
    l1.add("1");
    l2 = new ArrayList<>();
    l2.add("2");
    l2.add("3");
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
    tester.include("name");
    tester.include("fieldName");
    tester.include("description");
    tester.include("length");
    tester.include("possibleValues");
    tester.include("value");
    tester.include("values");
    tester.include("placeholder");
    tester.include("type");

    // Set up objects
    tester.proxy(List.class, 1, l1);
    tester.proxy(List.class, 2, l2);

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

    assertTrue(tester.testCopyConstructor(AlgorithmParameter.class));
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

    assertTrue(tester.testXmlSerialization());
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
    //assertTrue(tester.testNotAnalyzedIndexedFields());

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
