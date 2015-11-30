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
import com.wci.umls.server.helpers.ProxyTester;
import com.wci.umls.server.helpers.XmlSerializationTester;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.GeneralConceptAxiomJpa;
import com.wci.umls.server.jpa.helpers.NullableFieldTester;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.GeneralConceptAxiom;

/**
 * Unit testing for {@link GeneralConceptAxiomJpa}.
 */
public class GeneralConceptAxiomJpaUnitTest {

  /** The model object to test. */
  private GeneralConceptAxiomJpa object;

  /** test fixture */
  private Concept c1;

  /** test fixture */
  private Concept c2;

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
    object = new GeneralConceptAxiomJpa();

    ProxyTester tester = new ProxyTester(new ConceptJpa());
    c1 = (Concept) tester.createObject(1);
    c2 = (Concept) tester.createObject(2);
    object.setLeftHandSide(c1);
    object.setRightHandSide(c2);
  }

  /**
   * Test getter and setter methods of model object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelGetSet052() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelGetSet052");
    GetterSetterTester tester = new GetterSetterTester(object);
    tester.test();
  }

  /**
   * Test equals and hascode methods.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelEqualsHashcode052() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelEqualsHashcode052");
    EqualsHashcodeTester tester = new EqualsHashcodeTester(object);
    tester.include("suppressible");
    tester.include("obsolete");
    tester.include("publishable");
    tester.include("published");
    tester.include("terminology");
    tester.include("terminologyId");
    tester.include("version");
    tester.include("leftHandSide");
    tester.include("rightHandSide");
    tester.include("equivalent");
    tester.include("subClass");

    tester.proxy(Concept.class, 1, c1);
    tester.proxy(Concept.class, 2, c2);
    assertTrue(tester.testIdentitiyFieldEquals());
    tester.proxy(Concept.class, 1, c1);
    tester.proxy(Concept.class, 2, c2);
    assertTrue(tester.testNonIdentitiyFieldEquals());
    tester.proxy(Concept.class, 1, c1);
    tester.proxy(Concept.class, 2, c2);
    assertTrue(tester.testIdentityFieldNotEquals());
    tester.proxy(Concept.class, 1, c1);
    tester.proxy(Concept.class, 2, c2);
    assertTrue(tester.testIdentitiyFieldHashcode());
    tester.proxy(Concept.class, 1, c1);
    tester.proxy(Concept.class, 2, c2);
    assertTrue(tester.testNonIdentitiyFieldHashcode());
    tester.proxy(Concept.class, 1, c1);
    tester.proxy(Concept.class, 2, c2);
    assertTrue(tester.testIdentityFieldDifferentHashcode());
  }

  /**
   * Test copy constructor.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelCopy052() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelCopy052");
    CopyConstructorTester tester = new CopyConstructorTester(object);
    tester.proxy(Concept.class, 1, c1);
    tester.proxy(Concept.class, 2, c2);
    assertTrue(tester.testCopyConstructor(GeneralConceptAxiom.class));
  }

  /**
   * Test XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlSerialization052() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelXmlSerialization052");
    XmlSerializationTester tester = new XmlSerializationTester(object);
    tester.proxy(Concept.class, 1, c1);
    tester.proxy(Concept.class, 2, c2);
    assertTrue(tester.testXmlSerialization());
  }

  /**
   * Test not null fields.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelNotNullField052() throws Exception {
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
    tester.include("leftHandSide");
    tester.include("rightHandSide");
    tester.include("equivalent");
    tester.include("subClass");
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
