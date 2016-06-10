/*
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers.content;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

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
import com.wci.umls.server.jpa.content.AtomRelationshipJpa;
import com.wci.umls.server.jpa.content.AttributeJpa;
import com.wci.umls.server.jpa.content.DefinitionJpa;
import com.wci.umls.server.jpa.helpers.IndexedFieldTester;
import com.wci.umls.server.jpa.helpers.NullableFieldTester;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.AtomRelationship;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.Definition;

/**
 * Unit testing for {@link AtomJpa}.
 */
public class AtomJpaUnitTest {

  /** The model object to test. */
  private AtomJpa object;

  /** Test Fixture */
  private Map<String, String> map1;

  /** Test Fixture */
  private Map<String, String> map2;

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
    object = new AtomJpa();
    map1 = new HashMap<>();
    map1.put("1", "1");
    map2 = new HashMap<>();
    map2.put("2", "2");
  }

  /**
   * Test getter and setter methods of model object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelGetSet007() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelGetSet007");
    GetterSetterTester tester = new GetterSetterTester(object);
    tester.test();
  }

  /**
   * Test equals and hascode methods.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelEqualsHashcode007() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelEqualsHashcode007");
    EqualsHashcodeTester tester = new EqualsHashcodeTester(object);
    tester.include("suppressible");
    tester.include("obsolete");
    tester.include("publishable");
    tester.include("published");
    tester.include("terminology");
    tester.include("terminologyId");
    tester.include("version");
    tester.include("alternateTerminologyIds");
    tester.include("codeId");
    tester.include("descriptorId");
    tester.include("conceptId");
    tester.include("conceptTerminologyIds");
    tester.include("language");
    tester.include("lexicalClassId");
    tester.include("stringClassId");
    tester.include("name");
    tester.include("termType");
    tester.include("workflowStatus");

    tester.proxy(Map.class, 1, map1);
    tester.proxy(Map.class, 2, map2);
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
  public void testModelCopy007() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelCopy007");
    CopyConstructorTester tester = new CopyConstructorTester(object);
    tester.proxy(Map.class, 1, map1);
    tester.proxy(Map.class, 2, map2);
    assertTrue(tester.testCopyConstructor(Atom.class));
  }

  /**
   * Test deep copy constructor.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelDeepCopy007() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelDeepCopy007");

    Atom atom = new AtomJpa();
    ProxyTester tester = new ProxyTester(atom);
    tester.proxy(Map.class, 1, map1);
    atom = (Atom) tester.createObject(1);
    Atom toAtom = (Atom) tester.createObject(2);

    ProxyTester tester2 = new ProxyTester(new AttributeJpa());
    Attribute att = (Attribute) tester2.createObject(1);

    ProxyTester tester3 = new ProxyTester(new AtomRelationshipJpa());
    AtomRelationship rel = (AtomRelationship) tester3.createObject(1);
    rel.setFrom(atom);
    rel.setTo(toAtom);

    ProxyTester tester4 = new ProxyTester(new DefinitionJpa());
    Definition def = (Definition) tester4.createObject(1);

    atom.getAttributes().add(att);
    atom.getDefinitions().add(def);
    atom.getRelationships().add(rel);

    Atom atom2 = new AtomJpa(atom, false);
    assertEquals(0, atom2.getAttributes().size());
    assertEquals(0, atom2.getDefinitions().size());
    assertEquals(0, atom2.getRelationships().size());

    Atom atom3 = new AtomJpa(atom, true);
    assertEquals(1, atom3.getAttributes().size());
    assertEquals(att, atom3.getAttributes().iterator().next());
    assertEquals(1, atom3.getDefinitions().size());
    assertEquals(rel, atom3.getRelationships().iterator().next());
    assertEquals(1, atom3.getRelationships().size());
    assertEquals(def, atom3.getDefinitions().iterator().next());

  }

  /**
   * Test XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlSerialization007() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelXmlSerialization007");
    XmlSerializationTester tester = new XmlSerializationTester(object);
    tester.proxy(Map.class, 1, map1);
    tester.proxy(Map.class, 2, map2);
    assertTrue(tester.testXmlSerialization());
  }

  /**
   * Test not null fields.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelNotNullField007() throws Exception {
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
    tester.include("conceptTerminologyIds");
    tester.include("codeId");
    tester.include("conceptId");
    tester.include("descriptorId");
    tester.include("language");
    tester.include("lexicalClassId");
    tester.include("stringClassId");
    tester.include("name");
    tester.include("termType");
    tester.include("workflowStatus");

    assertTrue(tester.testNotNullFields());
  }

  /**
   * Test field indexing.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelIndexedFields007() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelIndexedFields007");

    // Test analyzed fields
    IndexedFieldTester tester = new IndexedFieldTester(object);
    tester.include("name");
    tester.include("edgeNGramName");
    tester.include("nGramName");
    tester.include("conceptTerminologyIds");
    tester.include("alternateTerminologyIds");
    assertTrue(tester.testAnalyzedIndexedFields());

    // Test non analyzed fields
    assertTrue(tester.testAnalyzedIndexedFields());
    tester = new IndexedFieldTester(object);
    tester.include("lastModified");
    tester.include("lastModifiedBy");
    tester.include("suppressible");
    tester.include("obsolete");
    tester.include("published");
    tester.include("publishable");
    tester.include("terminologyId");
    tester.include("terminology");
    tester.include("version");
    tester.include("nameSort");
    tester.include("nameNorm");
    tester.include("codeId");
    tester.include("descriptorId");
    tester.include("conceptId");
    tester.include("lexicalClassId");
    tester.include("stringClassId");
    tester.include("termType");
    tester.include("language");
    tester.include("workflowStatus");
    tester.include("branch");

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
