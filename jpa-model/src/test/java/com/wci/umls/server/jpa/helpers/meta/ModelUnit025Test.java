/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers.meta;

import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.CopyConstructorTester;
import com.wci.umls.server.helpers.EqualsHashcodeTester;
import com.wci.umls.server.helpers.GetterSetterTester;
import com.wci.umls.server.helpers.XmlSerializationTester;
import com.wci.umls.server.jpa.helpers.NullableFieldTester;
import com.wci.umls.server.jpa.meta.AdditionalRelationshipTypeJpa;
import com.wci.umls.server.model.meta.AdditionalRelationshipType;

/**
 * Unit testing for {@link AdditionalRelationshipTypeJpa}.
 */
public class ModelUnit025Test {

  /** The model object to test. */
  private AdditionalRelationshipTypeJpa object;

  /** The rela. */
  private AdditionalRelationshipTypeJpa rela;

  /** The rela2. */
  private AdditionalRelationshipTypeJpa rela2;

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
    object = new AdditionalRelationshipTypeJpa();
    rela = new AdditionalRelationshipTypeJpa();
    rela2 = new AdditionalRelationshipTypeJpa();
    rela.setId(1L);
    rela.setAbbreviation("1");
    rela.setExpandedForm("1");
    rela2.setId(2L);
    rela2.setAbbreviation("2");
    rela2.setExpandedForm("2");
    rela.setInverseType(rela2);
    rela2.setInverseType(rela);
    rela.setEquivalentType(rela);
    rela2.setEquivalentType(rela2);
    rela.setSuperType(rela);
    rela.setSuperType(rela2);
  }

  /**
   * Test getter and setter methods of model object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelGetSet025() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelGetSet025");
    GetterSetterTester tester = new GetterSetterTester(object);
    tester.exclude("inverseTypeAbbreviation");
    tester.exclude("inverseTypeId");
    tester.exclude("equivalentTypeAbbreviation");
    tester.exclude("equivalentTypeId");
    tester.exclude("superTypeAbbreviation");
    tester.exclude("superTypeId");
    tester.test();
  }

  /**
   * Test equals and hascode methods.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelEqualsHashcode025() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelEqualsHashcode025");
    EqualsHashcodeTester tester = new EqualsHashcodeTester(object);
    tester.include("abbreviation");
    tester.include("expandedForm");
    tester.include("terminology");
    tester.include("terminologyVersion");
    tester.include("terminologyVersion");
    tester.include("publishable");
    tester.include("published");
    tester.include("asymmetric");
    tester.include("equivalentClasses");
    tester.include("existentialQuantification");
    tester.include("functional");
    tester.include("inverseFunctional");
    tester.include("irreflexive");
    tester.include("reflexive");
    tester.include("symmetric");
    tester.include("transitive");
    tester.include("universalQuantification");
    tester.include("domainId");
    tester.include("rangeId");

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
  public void testModelCopy025() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelCopy025");
    CopyConstructorTester tester = new CopyConstructorTester(object);
    tester.proxy(AdditionalRelationshipType.class, 1, rela);
    tester.proxy(AdditionalRelationshipType.class, 2, rela2);
    assertTrue(tester.testCopyConstructor(AdditionalRelationshipType.class));
  }

  /**
   * Test XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlSerialization025() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelXmlSerialization025");
    XmlSerializationTester tester = new XmlSerializationTester(object);
    tester.proxy(AdditionalRelationshipType.class, 1, rela);
    tester.proxy(AdditionalRelationshipType.class, 2, rela2);
    assertTrue(tester.testXmlSerialization());
  }

  /**
   * Test not null fields.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelNotNullField025() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelNotNullField025");
    NullableFieldTester tester = new NullableFieldTester(object);
    tester.include("abbreviation");
    tester.include("expandedForm");
    tester.include("terminology");
    tester.include("terminologyVersion");
    tester.include("publishable");
    tester.include("published");
    tester.include("timestamp");
    tester.include("lastModified");
    tester.include("lastModifiedBy");
    tester.include("asymmetric");
    tester.include("equivalentClasses");
    tester.include("existentialQuantification");
    tester.include("functional");
    tester.include("inverseFunctional");
    tester.include("irreflexive");
    tester.include("reflexive");
    tester.include("symmetric");
    tester.include("transitive");
    tester.include("universalQuantification");

    assertTrue(tester.testNotNullFields());
  }

  /**
   * Test concept reference in XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlTransient025() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelXmlTransient025");
    String xml = ConfigUtility.getStringForGraph(rela);
    assertTrue(xml.contains("<inverseTypeId>"));
    assertTrue(xml.contains("<inverseTypeAbbreviation>"));
    assertTrue(xml.contains("<equivalentTypeId>"));
    assertTrue(xml.contains("<equivalentTypeAbbreviation>"));
    assertTrue(xml.contains("<superTypeId>"));
    assertTrue(xml.contains("<superTypeAbbreviation>"));
    Assert.assertFalse(xml.contains("<inverseType>"));
    Assert.assertFalse(xml.contains("<equivalentType>"));
    Assert.assertFalse(xml.contains("<superType>"));

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
