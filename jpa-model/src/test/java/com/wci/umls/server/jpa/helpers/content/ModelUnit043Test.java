/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers.content;

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
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.AtomSubsetJpa;
import com.wci.umls.server.jpa.content.AtomSubsetMemberJpa;
import com.wci.umls.server.jpa.helpers.NullableFieldTester;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.AtomSubset;
import com.wci.umls.server.model.content.AtomSubsetMember;

/**
 * Unit testing for {@link AtomSubsetMemberJpa}.
 */
public class ModelUnit043Test {

  /** The model object to test. */
  private AtomSubsetMemberJpa object;

  /** Test fixture */
  private Atom atom1;
  /** Test fixture */
  private Atom atom2;

  /** Test fixture */
  private AtomSubset subset1;
  /** Test fixture */
  private AtomSubset subset2;

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
    object = new AtomSubsetMemberJpa();
    
    atom1 = new AtomJpa();
    atom1.setId(1L);
    atom1.setTerminologyId("1");
    atom1.setTerm("1");
    atom2 = new AtomJpa();
    atom2.setId(2L);
    atom2.setTerminologyId("2");
    atom2.setTerm("2");

    subset1 = new AtomSubsetJpa();
    subset1.setId(1L);
    subset1.setTerminologyId("1");
    subset1.setName("2");
    subset2 = new AtomSubsetJpa();
    subset2.setId(2L);
    subset2.setTerminologyId("2");
    subset2.setName("2");
  }

  /**
   * Test getter and setter methods of model object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelGetSet043() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelGetSet043");
    GetterSetterTester tester = new GetterSetterTester(object);
    tester.exclude("memberId");
    tester.exclude("memberTerminologyId");
    tester.exclude("memberTerm");
    tester.exclude("subsetId");
    tester.exclude("subsetTerminologyId");
    tester.exclude("subsetName");
    tester.test();
  }

  /**
   * Test equals and hascode methods.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelEqualsHashcode043() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelEqualsHashcode043");
    EqualsHashcodeTester tester = new EqualsHashcodeTester(object);
    tester.include("suppressible");
    tester.include("obsolete");
    tester.include("publishable");
    tester.include("published");
    tester.include("terminology");
    tester.include("terminologyId");
    tester.include("terminologyVersion");
    tester.include("member");
    tester.include("subset");

    tester.proxy(Atom.class, 1, new AtomJpa(atom1,false));
    tester.proxy(Atom.class, 2, new AtomJpa(atom2,false));
    tester.proxy(AtomSubset.class, 1, new AtomSubsetJpa(subset1,false));
    tester.proxy(AtomSubset.class, 2, new AtomSubsetJpa(subset2,false));
    assertTrue(tester.testIdentitiyFieldEquals());
    tester.proxy(Atom.class, 1, new AtomJpa(atom1,false));
    tester.proxy(Atom.class, 2, new AtomJpa(atom2,false));
    tester.proxy(AtomSubset.class, 1, new AtomSubsetJpa(subset1,false));
    tester.proxy(AtomSubset.class, 2, new AtomSubsetJpa(subset2,false));
    assertTrue(tester.testNonIdentitiyFieldEquals());
    tester.proxy(Atom.class, 1, new AtomJpa(atom1,false));
    tester.proxy(Atom.class, 2, new AtomJpa(atom2,false));
    tester.proxy(AtomSubset.class, 1, new AtomSubsetJpa(subset1,false));
    tester.proxy(AtomSubset.class, 2, new AtomSubsetJpa(subset2,false));
    assertTrue(tester.testIdentityFieldNotEquals());
    tester.proxy(Atom.class, 1, new AtomJpa(atom1,false));
    tester.proxy(Atom.class, 2, new AtomJpa(atom2,false));
    tester.proxy(AtomSubset.class, 1, new AtomSubsetJpa(subset1,false));
    tester.proxy(AtomSubset.class, 2, new AtomSubsetJpa(subset2,false));
    assertTrue(tester.testIdentitiyFieldHashcode());
    tester.proxy(Atom.class, 1, new AtomJpa(atom1,false));
    tester.proxy(Atom.class, 2, new AtomJpa(atom2,false));
    tester.proxy(AtomSubset.class, 1, new AtomSubsetJpa(subset1,false));
    tester.proxy(AtomSubset.class, 2, new AtomSubsetJpa(subset2,false));
    assertTrue(tester.testNonIdentitiyFieldHashcode());
    tester.proxy(Atom.class, 1, new AtomJpa(atom1,false));
    tester.proxy(Atom.class, 2, new AtomJpa(atom2,false));
    tester.proxy(AtomSubset.class, 1, new AtomSubsetJpa(subset1,false));
    tester.proxy(AtomSubset.class, 2, new AtomSubsetJpa(subset2,false));
    assertTrue(tester.testIdentityFieldDifferentHashcode());
  }

  /**
   * Test copy constructor.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelCopy043() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelCopy043");
    CopyConstructorTester tester = new CopyConstructorTester(object);
    tester.proxy(Atom.class, 1, atom1);
    tester.proxy(Atom.class, 2, atom2);
    tester.proxy(AtomSubset.class, 1, subset1);
    tester.proxy(AtomSubset.class, 2, subset2);
    assertTrue(tester.testCopyConstructorDeep(AtomSubsetMember.class));
  }


  /**
   * Test xml transient fields
   *
   * @throws Exception the exception
   */
  @Test
  public void testXmlTransient043() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelXmlTransient043");

    String xml = ConfigUtility.getStringForGraph(object);
    assertTrue(xml.contains("<subsetId>"));
    assertTrue(xml.contains("<subsetTerminologyId>"));
    assertTrue(xml.contains("<subsetName>"));
    assertFalse(xml.contains("<subset>"));
    assertTrue(xml.contains("<memberId>"));
    assertTrue(xml.contains("<memberTerminologyId>"));
    assertTrue(xml.contains("<memberTerm>"));
    assertFalse(xml.contains("<member>"));

  }

  /**
   * Test XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlSerialization043() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelXmlSerialization043");
    XmlSerializationTester tester = new XmlSerializationTester(object);
    tester.proxy(Atom.class, 1, atom1);
    tester.proxy(Atom.class, 2, atom2);
    tester.proxy(AtomSubset.class, 1, subset1);
    tester.proxy(AtomSubset.class, 2, subset2);
    assertTrue(tester.testXmlSerialization());
  }

  /**
   * Test not null fields.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelNotNullField043() throws Exception {
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
    tester.include("terminologyVersion");
    tester.include("member");
    tester.include("subset");
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
//