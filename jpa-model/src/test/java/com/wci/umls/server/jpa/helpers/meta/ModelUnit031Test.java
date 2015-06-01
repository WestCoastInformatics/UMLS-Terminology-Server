/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers.meta;

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
import com.wci.umls.server.helpers.XmlSerializationTester;
import com.wci.umls.server.jpa.helpers.NullableFieldTester;
import com.wci.umls.server.jpa.meta.LanguageJpa;
import com.wci.umls.server.model.meta.Language;

/**
 * Unit testing for {@link LanguageJpa}.
 */
public class ModelUnit031Test {

  /** The model object to test. */
  private LanguageJpa object;

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
    object = new LanguageJpa();
  }

  /**
   * Test getter and setter methods of model object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelGetSet031() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelGetSet031");
    GetterSetterTester tester = new GetterSetterTester(object);
    tester.test();
  }

  /**
   * Test equals and hascode methods.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelEqualsHashcode031() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelEqualsHashcode031");
    EqualsHashcodeTester tester = new EqualsHashcodeTester(object);
    tester.include("abbreviation");
    tester.include("expandedForm");
    tester.include("publishable");
    tester.include("published");
    tester.include("terminology");
    tester.include("version");
    tester.include("isoCode");
    tester.include("iso3Code");

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
  public void testModelCopy031() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelCopy031");
    CopyConstructorTester tester = new CopyConstructorTester(object);
    assertTrue(tester.testCopyConstructor(Language.class));
  }

  /**
   * Test XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlSerialization031() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelXmlSerialization031");
    XmlSerializationTester tester = new XmlSerializationTester(object);
    assertTrue(tester.testXmlSerialization());
  }

  /**
   * Test not null fields.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelNotNullField031() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelNotNullField031");
    NullableFieldTester tester = new NullableFieldTester(object);
    tester.include("abbreviation");
    tester.include("expandedForm");
    tester.include("terminology");
    tester.include("version");
    tester.include("publishable");
    tester.include("published");
    tester.include("timestamp");
    tester.include("lastModified");
    tester.include("lastModifiedBy");
    tester.include("isoCode");
    tester.include("iso3Code");
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
