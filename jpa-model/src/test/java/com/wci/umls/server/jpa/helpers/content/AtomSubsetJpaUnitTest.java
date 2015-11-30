/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers.content;

import static org.junit.Assert.assertEquals;
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
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.AtomSubsetJpa;
import com.wci.umls.server.jpa.content.AtomSubsetMemberJpa;
import com.wci.umls.server.jpa.content.AttributeJpa;
import com.wci.umls.server.jpa.helpers.NullableFieldTester;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.AtomSubset;
import com.wci.umls.server.model.content.AtomSubsetMember;
import com.wci.umls.server.model.content.Attribute;

/**
 * Unit testing for {@link AtomSubsetJpa}.
 */
public class AtomSubsetJpaUnitTest {

  /** The model object to test. */
  private AtomSubsetJpa object;

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
    object = new AtomSubsetJpa();
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
    tester.include("suppressible");
    tester.include("obsolete");
    tester.include("publishable");
    tester.include("published");
    tester.include("terminology");
    tester.include("terminologyId");
    tester.include("version");

    tester.include("name");
    tester.include("description");

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
  public void testModelCopy041() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelCopy041");
    CopyConstructorTester tester = new CopyConstructorTester(object);
    assertTrue(tester.testCopyConstructorDeep(AtomSubset.class));
  }

  /**
   * Test deep copy constructor.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelDeepCopy041() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelDeepCopy041");

    AtomSubset subset = new AtomSubsetJpa();
    ProxyTester tester = new ProxyTester(subset);
    subset = (AtomSubset) tester.createObject(1);

    ProxyTester tester2 = new ProxyTester(new AttributeJpa());
    Attribute att = (Attribute) tester2.createObject(1);

    ProxyTester tester3 = new ProxyTester(new AtomJpa());
    Atom atom = (Atom) tester3.createObject(1);

    ProxyTester tester4 = new ProxyTester(new AtomSubsetMemberJpa());
    AtomSubsetMember member = (AtomSubsetMember) tester4.createObject(1);
    member.setMember(atom);

    subset.addMember(member);
    subset.addAttribute(att);

    AtomSubset subset2 = new AtomSubsetJpa(subset, false);
    assertEquals(0, subset2.getAttributes().size());
    assertEquals(0, subset2.getMembers().size());

    AtomSubset subset3 = new AtomSubsetJpa(subset, true);
    assertEquals(1, subset3.getAttributes().size());
    assertEquals(att, subset3.getAttributes().iterator().next());
    assertTrue(att != subset3.getAttributes().iterator().next());
    assertEquals(1, subset3.getMembers().size());
    assertEquals(member, subset3.getMembers().iterator().next());
    assertTrue(member != subset3.getMembers().iterator().next());

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
    tester.include("name");
    tester.include("description");
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