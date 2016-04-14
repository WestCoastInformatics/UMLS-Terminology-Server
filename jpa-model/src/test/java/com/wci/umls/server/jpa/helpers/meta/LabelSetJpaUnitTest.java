/**
 * Copyright 2016 West Coast Informatics, LLC
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
import com.wci.umls.server.jpa.meta.LabelSetJpa;
import com.wci.umls.server.model.meta.LabelSet;

/**
 * Unit testing for {@link LabelSetJpa}.
 */
public class LabelSetJpaUnitTest {

  /** The model object to test. */
  private LabelSetJpa object;

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
    // set up text fixtures
    object = new LabelSetJpa();
  }

  /**
   * Test getter and setter methods of model object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelGetSet040() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelGetSet040");
    GetterSetterTester tester = new GetterSetterTester(object);
    tester.test();
  }

  /**
   * Test equals and hascode methods.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelEqualsHashcode040() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelEqualsHashcode040");
    EqualsHashcodeTester tester = new EqualsHashcodeTester(object);

    tester.include("abbreviation");
    tester.include("expandedForm");
    tester.include("publishable");
    tester.include("published");
    tester.include("terminology");
    tester.include("version");
    tester.include("description");
    tester.include("derived");

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
  public void testModelCopy040() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelCopy040");
    CopyConstructorTester tester = new CopyConstructorTester(object);

    assertTrue(tester.testCopyConstructor(LabelSet.class));
  }

  /**
   * Test XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlSerialization040() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelXmlSerialization040");
    XmlSerializationTester tester = new XmlSerializationTester(object);
    assertTrue(tester.testXmlSerialization());
  }

  /**
   * Test not null fields.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelNotNullField040() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelNotNullField040");
    NullableFieldTester tester = new NullableFieldTester(object);
    tester.include("timestamp");
    tester.include("lastModified");
    tester.include("lastModifiedBy");
    tester.include("terminology");
    tester.include("publishable");
    tester.include("published");
    tester.include("version");
    tester.include("abbreviation");
    tester.include("expandedForm");
    tester.include("description");
    tester.include("derived");

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
