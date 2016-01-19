/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers.content;

import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.ReleaseInfo;
import com.wci.umls.server.helpers.CopyConstructorTester;
import com.wci.umls.server.helpers.EqualsHashcodeTester;
import com.wci.umls.server.helpers.GetterSetterTester;
import com.wci.umls.server.helpers.XmlSerializationTester;
import com.wci.umls.server.jpa.ReleaseInfoJpa;
import com.wci.umls.server.jpa.helpers.NullableFieldTester;

/**
 * Unit testing for {@link ReleaseInfoJpa}.
 */
public class ReleaseInfoJpaUnitTest {

  /** The model object to test. */
  private ReleaseInfoJpa object;

  /**
   * Setup class.
   */
  @BeforeClass
  public static void setupClass() {
    // do nothing
  }

  /**
   * Setup.
   */
  @Before
  public void setup() {
    object = new ReleaseInfoJpa();
  }

  /**
   * Test getter and setter methods of model object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelGetSet002() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelGetSet002");
    GetterSetterTester tester = new GetterSetterTester(object);
    tester.test();
  }

  /**
   * Test equals and hascode methods.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelEqualsHashcode002() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelEqualsHashcode002");
    EqualsHashcodeTester tester = new EqualsHashcodeTester(object);
    tester.include("description");
    tester.include("name");
    tester.include("planned");
    tester.include("published");
    tester.include("terminology");
    tester.include("version");

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
  public void testModelCopy002() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelCopy002");
    CopyConstructorTester tester = new CopyConstructorTester(object);
    assertTrue(tester.testCopyConstructor(ReleaseInfo.class));
  }

  /**
   * Test XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlSerialization002() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelXmlTransient002");
    XmlSerializationTester tester = new XmlSerializationTester(object);
    assertTrue(tester.testXmlSerialization());
  }

  /**
   * Test not null fields.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelNotNullField002() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelNotNullField002");
    NullableFieldTester tester = new NullableFieldTester(object);
    tester.include("name");
    tester.include("description");
    tester.include("planned");
    tester.include("published");
    tester.include("terminology");
    tester.include("version");
    tester.include("lastModified");
    tester.include("lastModifiedBy");

    assertTrue(tester.testNotNullFields());
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
