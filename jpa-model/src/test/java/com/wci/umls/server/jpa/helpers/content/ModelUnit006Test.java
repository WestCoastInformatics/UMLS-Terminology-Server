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
import com.wci.umls.server.helpers.SearchCriteria;
import com.wci.umls.server.helpers.XmlSerializationTester;
import com.wci.umls.server.jpa.helpers.SearchCriteriaJpa;

/**
 * Unit testing for {@link SearchCriteriaJpa}.
 */
public class ModelUnit006Test {

  /** The model object to test. */
  private SearchCriteriaJpa object;

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
    object = new SearchCriteriaJpa();
  }

  /**
   * Test getter and setter methods of model object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelGetSet006() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelGetSet006");
    GetterSetterTester tester = new GetterSetterTester(object);
    tester.test();
  }

  /**
   * Test equals and hascode methods.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelEqualsHashcode006() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelEqualsHashcode006");
    EqualsHashcodeTester tester = new EqualsHashcodeTester(object);
    tester.include("definedOnly");
    tester.include("findDescendants");
    tester.include("primitiveOnly");
    tester.include("findSelf");
    // no set methods for these things:
    tester.include("relationshipToId");
    tester.include("relationshipDescendantsFlag");
    tester.include("relationshipType");
    tester.include("relationshipFromId");

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
  public void testModelCopy006() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelCopy006");
    CopyConstructorTester tester = new CopyConstructorTester(object);

    assertTrue(tester.testCopyConstructor(SearchCriteria.class));
  }

  /**
   * Test XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlSerialization006() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelXmlTransient006");
    XmlSerializationTester tester = new XmlSerializationTester(object);

    assertTrue(tester.testXmlSerialization());
  }

  /**
   * Test not null fields.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelNotNullField006() throws Exception {
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
