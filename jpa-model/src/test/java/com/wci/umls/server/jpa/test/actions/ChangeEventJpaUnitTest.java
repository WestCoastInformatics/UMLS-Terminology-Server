/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.test.actions;

import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.helpers.ComponentInfo;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.CopyConstructorTester;
import com.wci.umls.server.helpers.EqualsHashcodeTester;
import com.wci.umls.server.helpers.GetterSetterTester;
import com.wci.umls.server.jpa.ModelUnitSupport;
import com.wci.umls.server.jpa.actions.ChangeEventJpa;
import com.wci.umls.server.jpa.content.AbstractComponent;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.model.actions.ChangeEvent;
import com.wci.umls.server.model.content.Component;

/**
 * Unit testing for {@link ChangeEventJpa}.
 */
public class ChangeEventJpaUnitTest extends ModelUnitSupport {

  /** The model object to test. */
  private ChangeEvent<ConceptJpa> object;

  /** The c1. */
  private ConceptJpa c1;

  /** The c2. */
  private ConceptJpa c2;

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
    object = new ChangeEventJpa<ConceptJpa>();
    c1 = new ConceptJpa();
    c1.setId(1L);
    c1.setTerminologyId("1");
    c2 = new ConceptJpa();
    c2.setId(2L);
    c2.setTerminologyId("2");
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
    tester.include("sessionId");
    tester.include("type");
    tester.include("oldValue");
    tester.include("newValue");
    tester.include("container");

    tester.proxy(Component.class, 1, c1);
    tester.proxy(Component.class, 2, c2);
    tester.proxy(ComponentInfo.class, 1, c1);
    tester.proxy(ComponentInfo.class, 2, c2);

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
    tester.proxy(Component.class, 1, c1);
    tester.proxy(Component.class, 2, c2);
    tester.proxy(ComponentInfo.class, 1, c1);
    tester.proxy(ComponentInfo.class, 2, c2);
    assertTrue(tester.testCopyConstructor(ChangeEvent.class));
  }

  /**
   * Test XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlSerialization() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    object.setOldValue(c1);
    object.setNewValue(c2);
    Logger.getLogger(getClass()).info(
        "xml = " + ConfigUtility.getStringForGraph(object));
    Logger.getLogger(getClass()).info(
        "json = " + ConfigUtility.getJsonForGraph(object));
    // Only testing TO XML/JSON is important
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
//