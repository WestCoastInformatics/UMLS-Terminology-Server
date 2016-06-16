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

import com.wci.umls.server.helpers.CopyConstructorTester;
import com.wci.umls.server.helpers.EqualsHashcodeTester;
import com.wci.umls.server.helpers.GetterSetterTester;
import com.wci.umls.server.helpers.ProxyTester;
import com.wci.umls.server.helpers.XmlSerializationTester;
import com.wci.umls.server.jpa.actions.AtomicActionJpa;
import com.wci.umls.server.jpa.actions.MolecularActionJpa;
import com.wci.umls.server.jpa.helpers.IndexedFieldTester;
import com.wci.umls.server.jpa.helpers.NullableFieldTester;
import com.wci.umls.server.model.actions.AtomicAction;
import com.wci.umls.server.model.actions.MolecularAction;

/**
 * Unit testing for {@link AtomicActionJpa}.
 */
public class AtomicActionJpaUnitTest {

  /** The model object to test. */
  private AtomicActionJpa object;

  /** The a1. */
  private MolecularAction a1;

  /** The a2. */
  private MolecularAction a2;

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
    object = new AtomicActionJpa();
    ProxyTester tester = new ProxyTester(new MolecularActionJpa());
    a1 = (MolecularActionJpa) tester.createObject(1);
    a2 = (MolecularActionJpa) tester.createObject(2);

  }

  /**
   * Test getter and setter methods of model object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelGetSet041() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelGetSet041");
    GetterSetterTester tester = new GetterSetterTester(object);
    tester.test();
  }

  /**
   * Test equals and hascode methods.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelEqualsHashcode041() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelEqualsHashcode041");
    EqualsHashcodeTester tester = new EqualsHashcodeTester(object);

    tester.include("idType");
    tester.include("objectId");
    tester.include("oldValue");
    tester.include("newValue");
    tester.include("field");

    tester.proxy(MolecularAction.class, 1, a1);
    tester.proxy(MolecularAction.class, 2, a2);

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
  public void testModelCopy041() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelDeepCopy041");

    CopyConstructorTester tester = new CopyConstructorTester(object);
    tester.proxy(MolecularAction.class, 1, a1);
    tester.proxy(MolecularAction.class, 2, a2);
    assertTrue(tester.testCopyConstructor(AtomicAction.class));
  }

  /**
   * Test XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlSerialization041() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelXmlSerialization041");
    XmlSerializationTester tester = new XmlSerializationTester(object);
    // The proxy concepts can have only "id" and "term" set due to xml transient
    MolecularAction tr1 = new MolecularActionJpa();
    tr1.setId(1L);
    MolecularAction tr2 = new MolecularActionJpa();
    tr2.setId(2L);

    tester.proxy(MolecularAction.class, 1, tr1);
    tester.proxy(MolecularAction.class, 2, tr2);
    tester.proxy(MolecularAction.class, 1, a1);
    tester.proxy(MolecularAction.class, 2, a2);

    assertTrue(tester.testXmlSerialization());
  }

  /**
   * Test not null fields.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelNotNullField041() throws Exception {
    NullableFieldTester tester = new NullableFieldTester(object);

    tester.include("idType");
    tester.include("objectId");
    tester.include("field");
    assertTrue(tester.testNotNullFields());
  }

  /**
   * Test field indexing.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelIndexedFields041() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelIndexedFields041");

    // Test analyzed fields
    IndexedFieldTester tester = new IndexedFieldTester(object);
    // No fields analyzed
    assertTrue(tester.testAnalyzedIndexedFields());

    // Test non analyzed fields
    tester = new IndexedFieldTester(object);
    tester.include("idType");
    tester.include("objectId");
    tester.include("field");
    tester.include("oldValue");
    tester.include("newValue");

    assertTrue(tester.testNotAnalyzedIndexedFields());
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