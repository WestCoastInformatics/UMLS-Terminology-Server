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
import com.wci.umls.server.jpa.meta.AttributeNameJpa;
import com.wci.umls.server.model.meta.AttributeName;

/**
 * Unit testing for {@link AttributeNameJpa}.
 */
public class AttributeNameJpaUnitTest {

  /** The model object to test. */
  private AttributeNameJpa object;

  /** The test fixture */
  private AttributeNameJpa atn;

  /** The test fixture */
  private AttributeNameJpa atn2;

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

    object = new AttributeNameJpa();
    atn = new AttributeNameJpa();
    atn2 = new AttributeNameJpa();
    atn.setId(1L);
    atn.setAbbreviation("1");
    atn.setExpandedForm("1");
    atn2.setId(2L);
    atn2.setAbbreviation("2");
    atn2.setExpandedForm("2");
    atn.setEquivalentName(atn);
    atn2.setEquivalentName(atn2);
    atn.setSuperName(atn);
    atn.setSuperName(atn2);
  }

  /**
   * Test getter and setter methods of model object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelGetSet026() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelGetSet026");
    GetterSetterTester tester = new GetterSetterTester(object);
    tester.test();
  }

  /**
   * Test equals and hascode methods.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelEqualsHashcode026() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelEqualsHashcode026");
    EqualsHashcodeTester tester = new EqualsHashcodeTester(object);
    tester.include("abbreviation");
    tester.include("expandedForm");
    tester.include("terminology");
    tester.include("version");
    tester.include("publishable");
    tester.include("published");
    tester.include("domainId");
    tester.include("rangeId");
    tester.include("functional");
    tester.include("annotation");
    tester.include("existentialQuantification");
    tester.include("universalQuantification");

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
  public void testModelCopy026() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelCopy026");
    CopyConstructorTester tester = new CopyConstructorTester(object);
    assertTrue(tester.testCopyConstructor(AttributeName.class));
  }

  /**
   * Test XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlSerialization026() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelXmlSerialization026");
    XmlSerializationTester tester = new XmlSerializationTester(object);
    assertTrue(tester.testXmlSerialization());
  }

  /**
   * Test not null fields.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelNotNullField026() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelNotNullField026");
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
    tester.include("functional");
    tester.include("annotation");
    tester.include("existentialQuantification");
    tester.include("universalQuantification");

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
