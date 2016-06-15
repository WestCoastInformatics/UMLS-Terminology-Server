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
import com.wci.umls.server.helpers.XmlSerializationTester;
import com.wci.umls.server.jpa.ModelUnitSupport;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.AtomTreePositionJpa;
import com.wci.umls.server.jpa.helpers.NullableFieldTester;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.AtomTreePosition;

/**
 * Unit testing for {@link AtomTreePositionJpa}.
 */
public class AtomTreePositionJpaUnitTest extends ModelUnitSupport {

  /** The model object to test. */
  private AtomTreePositionJpa object;

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
    object = new AtomTreePositionJpa();
    atom1 = new AtomJpa();
    atom1.setId(1L);
    atom1.setName("1");
    atom1.setTerminologyId("1");
    atom1.setTerminology("1");
    atom1.setVersion("1");
    atom2 = new AtomJpa();
    atom2.setId(2L);
    atom2.setName("2");
    atom2.setTerminologyId("2");
    atom2.setTerminology("2");
    atom2.setVersion("2");
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
    tester.exclude("nodeId");
    tester.exclude("nodeTerminologyId");
    tester.exclude("nodeTerminology");
    tester.exclude("nodeVersion");
    tester.exclude("nodeName");
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
    tester.include("publishable");
    tester.include("published");
    tester.include("terminology");
    tester.include("terminologyId");
    tester.include("version");
    tester.include("additionalRelationshipType");
    tester.include("ancestorPath");
    tester.include("childCt");
    tester.include("descendantCt");
    tester.include("node");

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
    assertTrue(tester.testCopyConstructorDeep(AtomTreePosition.class));
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
    // The proxy codes can have only "id" and "name" set due to xml
    // transient
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

    object.setNode(atom1);
    String xml = ConfigUtility.getStringForGraph(object);
    assertTrue(xml.contains("<nodeId>"));
    assertTrue(xml.contains("<nodeName>"));
    assertTrue(xml.contains("<nodeTerminologyId>"));
    assertTrue(xml.contains("<nodeTerminology>"));
    assertTrue(xml.contains("<nodeVersion>"));
    assertFalse(xml.contains("<node>"));
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
    tester.include("terminology");
    tester.include("terminologyId");
    tester.include("version");
    tester.include("node");
    tester.include("childCt");
    tester.include("descendantCt");
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
