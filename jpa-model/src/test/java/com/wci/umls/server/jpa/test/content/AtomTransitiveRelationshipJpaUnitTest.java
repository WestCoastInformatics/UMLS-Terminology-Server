/*
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.test.content;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.CopyConstructorTester;
import com.wci.umls.server.helpers.EqualsHashcodeTester;
import com.wci.umls.server.helpers.GetterSetterTester;
import com.wci.umls.server.helpers.ProxyTester;
import com.wci.umls.server.helpers.XmlSerializationTester;
import com.wci.umls.server.jpa.ModelUnitSupport;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.AtomTransitiveRelationshipJpa;
import com.wci.umls.server.jpa.helpers.NullableFieldTester;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.AtomTransitiveRelationship;

/**
 * Unit testing for {@link AtomTransitiveRelationshipJpa}.
 */
public class AtomTransitiveRelationshipJpaUnitTest extends ModelUnitSupport {

  /** The model object to test. */
  private AtomTransitiveRelationshipJpa object;

  /** test fixture */
  private Atom atom1;

  /** test fixture */
  private Atom atom2;

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
    object = new AtomTransitiveRelationshipJpa();

    ProxyTester tester = new ProxyTester(new AtomJpa());
    atom1 = (AtomJpa) tester.createObject(1);
    atom2 = (AtomJpa) tester.createObject(2);

    object.setSuperType(atom1);
    object.setSubType(atom2);
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
    tester.exclude("superTypeId");
    tester.exclude("superTypeTerminologyId");
    tester.exclude("superTypeTerminology");
    tester.exclude("superTypeVersion");
    tester.exclude("superTypeName");
    tester.exclude("subTypeId");
    tester.exclude("subTypeTerminologyId");
    tester.exclude("subTypeTerminology");
    tester.exclude("subTypeVersion");
    tester.exclude("subTypeName");
    tester.exclude("name");
    tester.exclude("type");
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
    tester.include("suppressible");
    tester.include("obsolete");
    tester.include("branch");
    tester.include("terminology");
    tester.include("terminologyId");
    tester.include("superType");
    tester.include("subType");

    tester.proxy(Atom.class, 1, new AtomJpa(atom1, false));
    tester.proxy(Atom.class, 2, new AtomJpa(atom2, false));
    assertTrue(tester.testIdentityFieldEquals());
    tester.proxy(Atom.class, 1, new AtomJpa(atom1, false));
    tester.proxy(Atom.class, 2, new AtomJpa(atom2, false));
    assertTrue(tester.testNonIdentityFieldEquals());
    tester.proxy(Atom.class, 1, new AtomJpa(atom1, false));
    tester.proxy(Atom.class, 2, new AtomJpa(atom2, false));
    assertTrue(tester.testIdentityFieldNotEquals());
    tester.proxy(Atom.class, 1, new AtomJpa(atom1, false));
    tester.proxy(Atom.class, 2, new AtomJpa(atom2, false));
    assertTrue(tester.testIdentityFieldHashcode());
    tester.proxy(Atom.class, 1, new AtomJpa(atom1, false));
    tester.proxy(Atom.class, 2, new AtomJpa(atom2, false));
    assertTrue(tester.testNonIdentityFieldHashcode());
    tester.proxy(Atom.class, 1, new AtomJpa(atom1, false));
    tester.proxy(Atom.class, 2, new AtomJpa(atom2, false));
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
    tester.proxy(Atom.class, 1, atom1);
    tester.proxy(Atom.class, 2, atom2);
    assertTrue(tester.testCopyConstructorCollection(AtomTransitiveRelationship.class));
  }

  /**
   * Test XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlSerialization() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    XmlSerializationTester tester = new XmlSerializationTester(object);
    // The proxy atoms can have only "id" and "term" set due to xml transient
    Atom atom1 = new AtomJpa();
    atom1.setId(1L);
    atom1.setName("1");
    Atom atom2 = new AtomJpa();
    atom2.setId(2L);
    atom2.setName("2");

    tester.proxy(Atom.class, 1, atom1);
    tester.proxy(Atom.class, 2, atom2);
    assertTrue(tester.testXmlSerialization());
  }

  /**
   * Test xml transient fields
   *
   * @throws Exception the exception
   */
  @Test
  public void testXmlTransient() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    String xml = ConfigUtility.getStringForGraph(object);
    assertTrue(xml.contains("<subTypeId>"));
    assertTrue(xml.contains("<subTypeTerminologyId>"));
    assertTrue(xml.contains("<subTypeTerminology>"));
    assertTrue(xml.contains("<subTypeVersion>"));
    assertTrue(xml.contains("<subTypeName>"));
    assertTrue(xml.contains("<superTypeId>"));
    assertTrue(xml.contains("<superTypeTerminologyId>"));
    assertTrue(xml.contains("<superTypeTerminology>"));
    assertTrue(xml.contains("<superTypeVersion>"));
    assertTrue(xml.contains("<superTypeName>"));
    assertFalse(xml.contains("<subType>"));
    assertFalse(xml.contains("<superType>"));

  }

  /**
   * Test not null fields.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelNotNullField() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    NullableFieldTester tester = new NullableFieldTester(object);
    tester.include("timestamp");
    tester.include("lastModified");
    tester.include("lastModifiedBy");
    tester.include("suppressible");
    tester.include("obsolete");
    tester.include("published");
    tester.include("publishable");
    tester.include("depth");
    tester.include("terminology");
    tester.include("subType");
    tester.include("superType");
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
