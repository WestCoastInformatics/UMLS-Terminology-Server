/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers.content;

import static org.junit.Assert.*;

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
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.ConceptTreePositionJpa;
import com.wci.umls.server.jpa.helpers.NullableFieldTester;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptTreePosition;

/**
 * Unit testing for {@link ConceptTreePositionJpa}.
 */
public class ConceptTreePositionJpaUnitTest {

  /** The model object to test. */
  private ConceptTreePositionJpa object;

  /** test fixture */
  private Concept concept1;

  /** test fixture */
  private Concept concept2;

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
    object = new ConceptTreePositionJpa();
    concept1 = new ConceptJpa();
    concept1.setId(1L);
    concept1.setName("1");
    concept1.setTerminologyId("1");
    concept1.setTerminology("1");
    concept1.setVersion("1");
    concept2 = new ConceptJpa();
    concept2.setId(2L);
    concept2.setName("2");
    concept2.setTerminologyId("2");
    concept2.setTerminology("2");
    concept2.setVersion("2");
  }

  /**
   * Test getter and setter methods of model object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelGetSet046() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelGetSet046");
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
  public void testModelEqualsHashcode046() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelEqualsHashcode046");
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

    tester.proxy(Concept.class, 1, new ConceptJpa(concept1, false));
    tester.proxy(Concept.class, 2, new ConceptJpa(concept2, false));
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
  public void testModelCopy046() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelCopy046");
    CopyConstructorTester tester = new CopyConstructorTester(object);
    tester.proxy(Concept.class, 1, concept1);
    tester.proxy(Concept.class, 2, concept2);
    assertTrue(tester.testCopyConstructorDeep(ConceptTreePosition.class));
  }

  /**
   * Test XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlSerialization046() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelXmlSerialization046");
    XmlSerializationTester tester = new XmlSerializationTester(object);
    // The proxy codes can have only "id" and "name" set due to xml
    // transient
    tester.proxy(Concept.class, 1, concept1);
    tester.proxy(Concept.class, 2, concept2);

    assertTrue(tester.testXmlSerialization());
  }

  /**
   * Test xml transient fields
   *
   * @throws Exception the exception
   */
  @Test
  public void testXmlTransient046() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelXmlTransient046");

    object.setNode(concept1);
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
  public void testModelNotNullField046() throws Exception {
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
