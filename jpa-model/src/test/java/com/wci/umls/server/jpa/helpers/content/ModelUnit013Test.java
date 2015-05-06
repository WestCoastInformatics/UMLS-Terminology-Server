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
import com.wci.umls.server.helpers.ProxyTester;
import com.wci.umls.server.helpers.XmlSerializationTester;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.ConceptTransitiveRelationshipJpa;
import com.wci.umls.server.jpa.helpers.NullableFieldTester;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptTransitiveRelationship;

/**
 * Unit testing for {@link ConceptTransitiveRelationshipJpa}.
 */
public class ModelUnit013Test {

  /** The model object to test. */
  private ConceptTransitiveRelationshipJpa object;

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
    object = new ConceptTransitiveRelationshipJpa();

    ProxyTester tester = new ProxyTester(new ConceptJpa());
    concept1 = (ConceptJpa) tester.createObject(1);
    concept2 = (ConceptJpa) tester.createObject(2);

    object.setSuperType(concept1);
    object.setSubType(concept2);
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
    tester.exclude("superTypeId");
    tester.exclude("superTypeTerminologyId");
    tester.exclude("superTypeDefaultPreferredName");
    tester.exclude("subTypeId");
    tester.exclude("subTypeTerminologyId");
    tester.exclude("subTypeDefaultPreferredName");
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
    tester.include("terminologyVersion");
    tester.include("superType");
    tester.include("subType");

    tester.proxy(Concept.class, 1, new ConceptJpa(concept1, false));
    tester.proxy(Concept.class, 2, new ConceptJpa(concept2, false));
    assertTrue(tester.testIdentitiyFieldEquals());
    tester.proxy(Concept.class, 1, new ConceptJpa(concept1, false));
    tester.proxy(Concept.class, 2, new ConceptJpa(concept2, false));
    assertTrue(tester.testNonIdentitiyFieldEquals());
    tester.proxy(Concept.class, 1, new ConceptJpa(concept1, false));
    tester.proxy(Concept.class, 2, new ConceptJpa(concept2, false));
    assertTrue(tester.testIdentityFieldNotEquals());
    tester.proxy(Concept.class, 1, new ConceptJpa(concept1, false));
    tester.proxy(Concept.class, 2, new ConceptJpa(concept2, false));
    assertTrue(tester.testIdentitiyFieldHashcode());
    tester.proxy(Concept.class, 1, new ConceptJpa(concept1, false));
    tester.proxy(Concept.class, 2, new ConceptJpa(concept2, false));
    assertTrue(tester.testNonIdentitiyFieldHashcode());
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
    assertTrue(tester
        .testCopyConstructorDeep(ConceptTransitiveRelationship.class));
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
    concept1.setDefaultPreferredName("1");
    Concept concept2 = new ConceptJpa();
    concept2.setId(2L);
    concept2.setDefaultPreferredName("2");

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
  public void testXmlTransient012() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelXmlTransient012");

    String xml = ConfigUtility.getStringForGraph(object);
    assertTrue(xml.contains("<subTypeId>"));
    assertTrue(xml.contains("<subTypeTerminologyId>"));
    assertTrue(xml.contains("<subTypeDefaultPreferredName>"));
    assertTrue(xml.contains("<superTypeId>"));
    assertTrue(xml.contains("<superTypeTerminologyId>"));
    assertTrue(xml.contains("<superTypeDefaultPreferredName>"));
    assertFalse(xml.contains("<subType>"));
    assertFalse(xml.contains("<superType>"));

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
    tester.include("terminologyVersion");
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
