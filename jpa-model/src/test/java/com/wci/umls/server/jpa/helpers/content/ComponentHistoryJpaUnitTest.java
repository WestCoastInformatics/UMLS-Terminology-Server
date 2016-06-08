/*
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers.content;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.helpers.CopyConstructorTester;
import com.wci.umls.server.helpers.EqualsHashcodeTester;
import com.wci.umls.server.helpers.GetterSetterTester;
import com.wci.umls.server.helpers.XmlSerializationTester;
import com.wci.umls.server.jpa.content.ComponentHistoryJpa;
import com.wci.umls.server.jpa.helpers.NullableFieldTester;
import com.wci.umls.server.model.content.ComponentHistory;
import com.wci.umls.server.model.content.Concept;

/**
 * Unit testing for {@link ComponentHistoryJpa}.
 */
public class ComponentHistoryJpaUnitTest {

  /** The model object to test. */
  private ComponentHistoryJpa object;

  /** The map fixture 1. */
  private Map<String, String> map1;

  /** The map fixture 2. */
  private Map<String, String> map2;

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
    object = new ComponentHistoryJpa();
    map1 = new HashMap<>();
    map1.put("1", "1");
    map2 = new HashMap<>();
    map2.put("2", "2");
  }

  /**
   * Test getter and setter methods of model object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelGetSet009() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelGetSet009");
    GetterSetterTester tester = new GetterSetterTester(object);
    tester.test();
  }

  /**
   * Test equals and hascode methods.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelEqualsHashcode009() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelEqualsHashcode009");
    EqualsHashcodeTester tester = new EqualsHashcodeTester(object);
    tester.include("suppressible");
    tester.include("obsolete");
    tester.include("publishable");
    tester.include("published");
    tester.include("terminology");
    tester.include("terminologyId");
    tester.include("version");
    tester.include("name");
    tester.include("value");

    tester.include("associatedRelease");
    tester.include("referencedConcept");

    tester.proxy(Map.class, 1, map1);
    tester.proxy(Map.class, 2, map2);

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
  public void testModelCopy009() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelCopy009");
    CopyConstructorTester tester = new CopyConstructorTester(object);
    tester.proxy(Map.class, 1, map1);
    tester.proxy(Map.class, 2, map2);
    assertTrue(tester.testCopyConstructor(ComponentHistory.class));
  }

  /**
   * Test XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlSerialization009() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelXmlSerialization009");
    XmlSerializationTester tester = new XmlSerializationTester(object);
    tester.proxy(Map.class, 1, map1);
    tester.proxy(Map.class, 2, map2);
    assertTrue(tester.testXmlSerialization());
  }

  /**
   * Test not null fields.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelNotNullField009() throws Exception {
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
    tester.include("value");
    tester.include("associatedRelease");
    tester.include("referencedConcept");

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
