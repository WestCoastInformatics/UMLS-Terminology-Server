/*
 * Copyright 2016 West Coast Informatics, LLC
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
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.ConceptRelationshipJpa;
import com.wci.umls.server.jpa.helpers.NullableFieldTester;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;

/**
 * Unit testing for {@link ConceptRelationshipJpa}.
 */
public class ConceptRelationshipJpaUnitTest {

  /** The model object to test. */
  private ConceptRelationshipJpa object;

  /** test fixture */
  private Concept concept1;

  /** test fixture */
  private Concept concept2;

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
    object = new ConceptRelationshipJpa();

    map1 = new HashMap<>();
    map1.put("1", "1");
    map2 = new HashMap<>();
    map2.put("2", "2");

    ProxyTester tester = new ProxyTester(new ConceptJpa());
    tester.proxy(Map.class, 1, map1);
    tester.proxy(Map.class, 2, map2);
    concept1 = (ConceptJpa) tester.createObject(1);
    concept2 = (ConceptJpa) tester.createObject(2);

    object.setFrom(concept1);
    object.setTo(concept2);
  }

  /**
   * Test getter and setter methods of model object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelGetSet012() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelGetSet012");
    GetterSetterTester tester = new GetterSetterTester(object);
    tester.exclude("fromId");
    tester.exclude("fromTerminology");
    tester.exclude("fromVersion");
    tester.exclude("fromTerminologyId");
    tester.exclude("fromName");
    tester.exclude("toId");
    tester.exclude("toTerminology");
    tester.exclude("toVersion");
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
  public void testModelEqualsHashcode012() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelEqualsHashcode012");
    EqualsHashcodeTester tester = new EqualsHashcodeTester(object);
    tester.include("suppressible");
    tester.include("obsolete");
    tester.include("publishable");
    tester.include("published");
    tester.include("terminology");
    tester.include("terminologyId");
    tester.include("version");
    tester.include("alternateTerminologyIds");
    tester.include("assertedDirection");
    tester.include("additionalRelationshipType");
    tester.include("group");
    tester.include("inferred");
    tester.include("relationshipType");
    tester.include("stated");
    tester.include("hierarchical");
    tester.include("to");
    tester.include("from");
    tester.exclude("toTerminologyId");
    tester.exclude("fromTerminologyId");

    tester.proxy(Concept.class, 1, new ConceptJpa(concept1, false));
    tester.proxy(Concept.class, 2, new ConceptJpa(concept2, false));
    tester.proxy(Map.class, 1, map1);
    tester.proxy(Map.class, 2, map2);

    assertTrue(tester.testIdentityFieldEquals());
    tester.proxy(Concept.class, 1, new ConceptJpa(concept1, false));
    tester.proxy(Concept.class, 2, new ConceptJpa(concept2, false));
    assertTrue(tester.testNonIdentityFieldEquals());
    tester.proxy(Concept.class, 1, new ConceptJpa(concept1, false));
    tester.proxy(Concept.class, 2, new ConceptJpa(concept2, false));
    assertTrue(tester.testIdentityFieldNotEquals());
    tester.proxy(Concept.class, 1, new ConceptJpa(concept1, false));
    tester.proxy(Concept.class, 2, new ConceptJpa(concept2, false));
    assertTrue(tester.testIdentityFieldHashcode());
    tester.proxy(Concept.class, 1, new ConceptJpa(concept1, false));
    tester.proxy(Concept.class, 2, new ConceptJpa(concept2, false));
    assertTrue(tester.testNonIdentityFieldHashcode());
    tester.proxy(Concept.class, 1, new ConceptJpa(concept1, false));
    tester.proxy(Concept.class, 2, new ConceptJpa(concept2, false));
    assertTrue(tester.testIdentityFieldDifferentHashcode());
  }

  /**
   * Test copy constructor.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelCopy012() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelCopy012");
    CopyConstructorTester tester = new CopyConstructorTester(object);
    tester.proxy(Concept.class, 1, concept1);
    tester.proxy(Concept.class, 2, concept2);
    tester.proxy(Map.class, 1, map1);
    tester.proxy(Map.class, 2, map2);
    assertTrue(tester.testCopyConstructorDeep(ConceptRelationship.class));
  }

  /**
   * Test deep copy constructor.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelDeepCopy012() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelDeepCopy012");

    ConceptRelationship rel = new ConceptRelationshipJpa();
    ProxyTester tester = new ProxyTester(rel);
    tester.proxy(Map.class, 1, map1);
    rel = (ConceptRelationship) tester.createObject(1);

    ProxyTester tester2 = new ProxyTester(new ConceptJpa());
    tester.proxy(Map.class, 1, map1);
    tester.proxy(Map.class, 2, map2);
    Concept fromConcept = (Concept) tester2.createObject(1);
    Concept toConcept = (Concept) tester2.createObject(2);

    ProxyTester tester3 = new ProxyTester(new AttributeJpa());
    Attribute att = (Attribute) tester3.createObject(1);

    rel.setFrom(fromConcept);
    rel.setTo(toConcept);
    rel.addAttribute(att);

    ConceptRelationship rel2 = new ConceptRelationshipJpa(rel, false);
    assertEquals(0, rel2.getAttributes().size());

    ConceptRelationship rel3 = new ConceptRelationshipJpa(rel, true);
    assertEquals(1, rel3.getAttributes().size());
    assertEquals(att, rel3.getAttributes().iterator().next());

  }

  /**
   * Test XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlSerialization012() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelXmlSerialization012");
    XmlSerializationTester tester = new XmlSerializationTester(object);
    // The proxy concepts can have only "id" and "term" set due to xml transient
    Concept concept1 = new ConceptJpa();
    concept1.setId(1L);
    concept1.setName("1");
    Concept concept2 = new ConceptJpa();
    concept2.setId(2L);
    concept2.setName("2");

    tester.proxy(Concept.class, 1, concept1);
    tester.proxy(Concept.class, 2, concept2);
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
  public void testXmlTransient012() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelXmlTransient012");

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
  public void testModelNotNullField012() throws Exception {
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
    tester.include("assertedDirection");
    tester.include("relationshipType");
    tester.include("inferred");
    tester.include("stated");
    tester.include("hierarchical");
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
