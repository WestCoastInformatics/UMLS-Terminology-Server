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
import com.wci.umls.server.jpa.meta.CitationJpa;
import com.wci.umls.server.model.meta.Citation;

/**
 * Unit testing for {@link CitationJpa}.
 */
public class ModelUnit027Test {

  /** The model object to test. */
  private CitationJpa object;

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
    object = new CitationJpa();
  }

  /**
   * Test getter and setter methods of model object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelGetSet027() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelGetSet027");
    GetterSetterTester tester = new GetterSetterTester(object);
    tester.test();
  }

  /**
   * Test equals and hascode methods.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelEqualsHashcode027() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelEqualsHashcode027");
    EqualsHashcodeTester tester = new EqualsHashcodeTester(object);
    tester.include("address");
    tester.include("author");
    tester.include("availabilityStatement");
    tester.include("contentDesignator");
    tester.include("dateOfPublication");
    tester.include("dateOfRevision");
    tester.include("edition");
    tester.include("editor");
    tester.include("extent");
    tester.include("location");
    tester.include("mediumDesignator");
    tester.include("notes");
    tester.include("organization");
    tester.include("placeOfPublication");
    tester.include("publisher");
    tester.include("series");
    tester.include("title");
    tester.include("unstructuredValue");

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
  public void testModelCopy027() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelCopy027");
    CopyConstructorTester tester = new CopyConstructorTester(object);
    assertTrue(tester.testCopyConstructor(Citation.class));
  }

  /**
   * Test XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlSerialization027() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelXmlSerialization027");
    XmlSerializationTester tester = new XmlSerializationTester(object);
    assertTrue(tester.testXmlSerialization());
  }

  /**
   * Test not null fields.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelNotNullField027() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelNotNullField027");
    NullableFieldTester tester = new NullableFieldTester(object);
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
