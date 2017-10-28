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
import com.wci.umls.server.helpers.ProxyTester;
import com.wci.umls.server.helpers.XmlSerializationTester;
import com.wci.umls.server.jpa.ModelUnitSupport;
import com.wci.umls.server.jpa.content.DescriptorJpa;
import com.wci.umls.server.jpa.content.DescriptorTransitiveRelationshipJpa;
import com.wci.umls.server.jpa.helpers.NullableFieldTester;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.DescriptorTransitiveRelationship;

/**
 * Unit testing for {@link DescriptorTransitiveRelationshipJpa}.
 */
public class DescriptorTransitiveRelationshipJpaUnitTest extends
    ModelUnitSupport {

  /** The model object to test. */
  private DescriptorTransitiveRelationshipJpa object;

  /** test fixture */
  private Descriptor descriptor1;

  /** test fixture */
  private Descriptor descriptor2;

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
    object = new DescriptorTransitiveRelationshipJpa();

    ProxyTester tester = new ProxyTester(new DescriptorJpa());
    descriptor1 = (DescriptorJpa) tester.createObject(1);
    descriptor2 = (DescriptorJpa) tester.createObject(2);

    object.setSuperType(descriptor1);
    object.setSubType(descriptor2);
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
    tester.exclude("superTypeId");
    tester.exclude("superTypeTerminologyId");
    tester.exclude("superTypeTerminology");
    tester.exclude("superTypeVersion");
    tester.exclude("superTypeName");
    tester.exclude("subTypeId");
    tester.exclude("subTypeTerminologyId");
    tester.exclude("subTypeTerminology");
    tester.exclude("subTypeVersion");
    tester.exclude("subTypeName");
    tester.exclude("type");
    tester.exclude("name");
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
    tester.include("branch");
    tester.include("terminology");
    tester.include("terminologyId");
    tester.include("superType");
    tester.include("subType");

    tester.proxy(Descriptor.class, 1, new DescriptorJpa(descriptor1, false));
    tester.proxy(Descriptor.class, 2, new DescriptorJpa(descriptor2, false));
    assertTrue(tester.testIdentityFieldEquals());
    tester.proxy(Descriptor.class, 1, new DescriptorJpa(descriptor1, false));
    tester.proxy(Descriptor.class, 2, new DescriptorJpa(descriptor2, false));
    assertTrue(tester.testNonIdentityFieldEquals());
    tester.proxy(Descriptor.class, 1, new DescriptorJpa(descriptor1, false));
    tester.proxy(Descriptor.class, 2, new DescriptorJpa(descriptor2, false));
    assertTrue(tester.testIdentityFieldNotEquals());
    tester.proxy(Descriptor.class, 1, new DescriptorJpa(descriptor1, false));
    tester.proxy(Descriptor.class, 2, new DescriptorJpa(descriptor2, false));
    assertTrue(tester.testIdentityFieldHashcode());
    tester.proxy(Descriptor.class, 1, new DescriptorJpa(descriptor1, false));
    tester.proxy(Descriptor.class, 2, new DescriptorJpa(descriptor2, false));
    assertTrue(tester.testNonIdentityFieldHashcode());
    tester.proxy(Descriptor.class, 1, new DescriptorJpa(descriptor1, false));
    tester.proxy(Descriptor.class, 2, new DescriptorJpa(descriptor2, false));
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
    tester.proxy(Descriptor.class, 1, descriptor1);
    tester.proxy(Descriptor.class, 2, descriptor2);
    assertTrue(tester
        .testCopyConstructorCollection(DescriptorTransitiveRelationship.class));
  }

  /**
   * Test XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlSerialization() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    Logger.getLogger(getClass()).debug("TEST testModelXmlSerialization");
    XmlSerializationTester tester = new XmlSerializationTester(object);
    // The proxy descriptors can have only "id" and "term" set due to xml
    // transient
    Descriptor descriptor1 = new DescriptorJpa();
    descriptor1.setId(1L);
    descriptor1.setName("1");
    Descriptor descriptor2 = new DescriptorJpa();
    descriptor2.setId(2L);
    descriptor2.setName("2");

    tester.proxy(Descriptor.class, 1, descriptor1);
    tester.proxy(Descriptor.class, 2, descriptor2);
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

    String xml = ConfigUtility.getStringForGraph(object);
    assertTrue(xml.contains("<subTypeId>"));
    assertTrue(xml.contains("<subTypeTerminologyId>"));
    assertTrue(xml.contains("<subTypeTerminology>"));
    assertTrue(xml.contains("<subTypeVersion>"));
    assertTrue(xml.contains("<subTypeName>"));
    assertTrue(xml.contains("<superTypeId>"));
    assertTrue(xml.contains("<superTypeTerminologyId>"));
    assertTrue(xml.contains("<superTypeTerminology>"));
    assertTrue(xml.contains("<superTypeVersion>"));
    assertTrue(xml.contains("<superTypeName>"));
    assertFalse(xml.contains("<subType>"));
    assertFalse(xml.contains("<superType>"));

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
    tester.include("depth");
    tester.include("terminology");
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
