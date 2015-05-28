/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers.content;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

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
import com.wci.umls.server.jpa.content.AttributeJpa;
import com.wci.umls.server.jpa.content.CodeJpa;
import com.wci.umls.server.jpa.content.CodeRelationshipJpa;
import com.wci.umls.server.jpa.helpers.NullableFieldTester;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.CodeRelationship;

/**
 * Unit testing for {@link CodeRelationshipJpa}.
 */
public class ModelUnit021Test {

  /** The model object to test. */
  private CodeRelationshipJpa object;

  /** test fixture */
  private Code code1;

  /** test fixture */
  private Code code2;

  /** The map fixture 1. */
  private Map<String, String> map1;

  /** The map fixture 2. */
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
   * @throws Exception
   */
  @Before
  public void setup() throws Exception {
    object = new CodeRelationshipJpa();

    map1 = new HashMap<>();
    map1.put("1", "1");
    map2 = new HashMap<>();
    map2.put("2", "2");

    ProxyTester tester = new ProxyTester(new CodeJpa());
    tester.proxy(Map.class, 1, map1);
    tester.proxy(Map.class, 2, map2);
    code1 = (CodeJpa) tester.createObject(1);
    code2 = (CodeJpa) tester.createObject(2);

    object.setFrom(code1);
    object.setTo(code2);
  }

  /**
   * Test getter and setter methods of model object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelGetSet021() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelGetSet021");
    GetterSetterTester tester = new GetterSetterTester(object);
    tester.exclude("fromId");
    tester.exclude("fromTerminology");
    tester.exclude("fromTerminologyVersion");
    tester.exclude("fromTerminologyId");
    tester.exclude("fromName");
    tester.exclude("toId");
    tester.exclude("toTerminology");
    tester.exclude("toTerminologyVersion");
    tester.exclude("toTerminologyId");
    tester.exclude("toName");
    tester.test();
  }

  /**
   * Test equals and hascode methods.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelEqualsHashcode021() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelEqualsHashcode021");
    EqualsHashcodeTester tester = new EqualsHashcodeTester(object);
    tester.include("suppressible");
    tester.include("obsolete");
    tester.include("publishable");
    tester.include("published");
    tester.include("terminology");
    tester.include("terminologyId");
    tester.include("terminologyVersion");
    tester.include("alternateTerminologyIds");
    tester.include("assertedDirection");
    tester.include("additionalRelationshipType");
    tester.include("group");
    tester.include("inferred");
    tester.include("relationshipType");
    tester.include("stated");
    tester.include("to");
    tester.include("from");
    tester.exclude("toTerminologyId");
    tester.exclude("fromTerminologyId");

    tester.proxy(Code.class, 1, new CodeJpa(code1, false));
    tester.proxy(Code.class, 2, new CodeJpa(code2, false));
    tester.proxy(Map.class, 1, map1);
    tester.proxy(Map.class, 2, map2);

    assertTrue(tester.testIdentitiyFieldEquals());
    tester.proxy(Code.class, 1, new CodeJpa(code1, false));
    tester.proxy(Code.class, 2, new CodeJpa(code2, false));
    assertTrue(tester.testNonIdentitiyFieldEquals());
    tester.proxy(Code.class, 1, new CodeJpa(code1, false));
    tester.proxy(Code.class, 2, new CodeJpa(code2, false));
    assertTrue(tester.testIdentityFieldNotEquals());
    tester.proxy(Code.class, 1, new CodeJpa(code1, false));
    tester.proxy(Code.class, 2, new CodeJpa(code2, false));
    assertTrue(tester.testIdentitiyFieldHashcode());
    tester.proxy(Code.class, 1, new CodeJpa(code1, false));
    tester.proxy(Code.class, 2, new CodeJpa(code2, false));
    assertTrue(tester.testNonIdentitiyFieldHashcode());
    tester.proxy(Code.class, 1, new CodeJpa(code1, false));
    tester.proxy(Code.class, 2, new CodeJpa(code2, false));
    assertTrue(tester.testIdentityFieldDifferentHashcode());
  }

  /**
   * Test copy constructor.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelCopy021() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelCopy021");
    CopyConstructorTester tester = new CopyConstructorTester(object);
    tester.proxy(Code.class, 1, code1);
    tester.proxy(Code.class, 2, code2);
    tester.proxy(Map.class, 1, map1);
    tester.proxy(Map.class, 2, map2);
    assertTrue(tester.testCopyConstructorDeep(CodeRelationship.class));
  }

  /**
   * Test deep copy constructor.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelDeepCopy021() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelDeepCopy021");

    CodeRelationship rel = new CodeRelationshipJpa();
    ProxyTester tester = new ProxyTester(rel);
    tester.proxy(Map.class, 1, map1);
    rel = (CodeRelationship) tester.createObject(1);

    ProxyTester tester2 = new ProxyTester(new CodeJpa());
    tester.proxy(Map.class, 1, map1);
    tester.proxy(Map.class, 2, map2);
    Code fromCode = (Code) tester2.createObject(1);
    Code toCode = (Code) tester2.createObject(2);

    ProxyTester tester3 = new ProxyTester(new AttributeJpa());
    Attribute att = (Attribute) tester3.createObject(1);

    rel.setFrom(fromCode);
    rel.setTo(toCode);
    rel.addAttribute(att);

    CodeRelationship rel2 = new CodeRelationshipJpa(rel, false);
    assertEquals(0, rel2.getAttributes().size());

    CodeRelationship rel3 = new CodeRelationshipJpa(rel, true);
    assertEquals(1, rel3.getAttributes().size());
    assertEquals(att, rel3.getAttributes().iterator().next());

  }

  /**
   * Test XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlSerialization021() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelXmlSerialization021");
    XmlSerializationTester tester = new XmlSerializationTester(object);
    // The proxy codes can have only "id" and "term" set due to xml transient
    Code code1 = new CodeJpa();
    code1.setId(1L);
    code1.setName("1");
    Code code2 = new CodeJpa();
    code2.setId(2L);
    code2.setName("2");

    tester.proxy(Code.class, 1, code1);
    tester.proxy(Code.class, 2, code2);
    tester.proxy(Map.class, 1, map1);
    tester.proxy(Map.class, 2, map2);
    assertTrue(tester.testXmlSerialization());
  }

  /**
   * Test xml transient fields
   *
   * @throws Exception the exception
   */
  @Test
  public void testXmlTransient021() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelXmlTransient021");

    String xml = ConfigUtility.getStringForGraph(object);
    assertTrue(xml.contains("<fromId>"));
    assertTrue(xml.contains("<fromName>"));
    assertTrue(xml.contains("<toId>"));
    assertTrue(xml.contains("<toName>"));
    assertFalse(xml.contains("<from>"));
    assertFalse(xml.contains("<to>"));

  }

  /**
   * Test not null fields.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelNotNullField021() throws Exception {
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
    tester.include("assertedDirection");
    tester.include("relationshipType");
    tester.include("inferred");
    tester.include("stated");
    tester.include("from");
    tester.include("to");
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
