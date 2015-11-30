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

import com.wci.umls.server.helpers.CopyConstructorTester;
import com.wci.umls.server.helpers.EqualsHashcodeTester;
import com.wci.umls.server.helpers.GetterSetterTester;
import com.wci.umls.server.helpers.SearchResult;
import com.wci.umls.server.helpers.XmlSerializationTester;
import com.wci.umls.server.jpa.helpers.SearchResultJpa;

/**
 * Unit testing for {@link SearchResultJpa}.
 */
public class SearchResultJpaUnitTest {

  /** The model object to test. */
  private SearchResultJpa object;

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
    object = new SearchResultJpa();
  }

  /**
   * Test getter and setter methods of model object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelGetSet023() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelGetSet023");
    GetterSetterTester tester = new GetterSetterTester(object);
    tester.test();
  }

  /**
   * Test equals and hascode methods.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelEqualsHashcode023() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelEqualsHashcode023");
    EqualsHashcodeTester tester = new EqualsHashcodeTester(object);
    tester.include("terminology");
    tester.include("terminologyId");
    tester.include("version");
    tester.include("value");
    tester.include("obsolete");

    assertTrue(tester.testIdentitiyFieldEquals());
    assertTrue(tester.testNonIdentitiyFieldEquals());
    assertTrue(tester.testIdentityFieldNotEquals());
    assertTrue(tester.testIdentitiyFieldHashcode());
    assertTrue(tester.testNonIdentitiyFieldHashcode());
    assertTrue(tester.testIdentityFieldDifferentHashcode());
  }

  /**
   * Test copy constructor.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelCopy023() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelCopy023");
    CopyConstructorTester tester = new CopyConstructorTester(object);
    assertTrue(tester.testCopyConstructor(SearchResult.class));
  }

  /**
   * Test XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlSerialization023() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelXmlTransient023");
    XmlSerializationTester tester = new XmlSerializationTester(object);
    assertTrue(tester.testXmlSerialization());
  }

  /**
   * Test not null fields.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelNotNullField023() throws Exception {
    // n/a
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
