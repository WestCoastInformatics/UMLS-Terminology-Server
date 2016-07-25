/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.test.meta;

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
import com.wci.umls.server.jpa.ModelUnitSupport;
import com.wci.umls.server.jpa.helpers.NullableFieldTester;
import com.wci.umls.server.jpa.meta.AdditionalRelationshipTypeJpa;
import com.wci.umls.server.model.meta.AdditionalRelationshipType;

/**
 * Unit testing for {@link AdditionalRelationshipTypeJpa}.
 */
public class AdditionalRelationshipTypeJpaUnitTest extends ModelUnitSupport {

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
    rela.setInverse(rela2);
    rela2.setInverse(rela);
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
  public void testModelGetSet() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    GetterSetterTester tester = new GetterSetterTester(object);
    tester.exclude("inverseAbbreviation");
    tester.exclude("inverseId");
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
  public void testModelEqualsHashcode() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    EqualsHashcodeTester tester = new EqualsHashcodeTester(object);
    tester.include("abbreviation");
    tester.include("expandedForm");
    tester.include("terminology");
    tester.include("version");
    tester.include("version");
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
    tester.include("groupingType");

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
  public void testModelCopy() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
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
  public void testModelXmlSerialization() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
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
  public void testModelNotNullField() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    NullableFieldTester tester = new NullableFieldTester(object);
    tester.include("abbreviation");
    tester.include("expandedForm");
    tester.include("terminology");
    tester.include("version");
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
    tester.include("groupingType");
    tester.include("hierarchical");

    assertTrue(tester.testNotNullFields());
  }

  /**
   * Test concept reference in XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlTransient() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    String xml = ConfigUtility.getStringForGraph(rela);
    assertTrue(xml.contains("<inverseId>"));
    assertTrue(xml.contains("<inverseAbbreviation>"));
    assertTrue(xml.contains("<equivalentTypeId>"));
    assertTrue(xml.contains("<equivalentTypeAbbreviation>"));
    assertTrue(xml.contains("<superTypeId>"));
    assertTrue(xml.contains("<superTypeAbbreviation>"));
    Assert.assertFalse(xml.contains("<inverse>"));
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
