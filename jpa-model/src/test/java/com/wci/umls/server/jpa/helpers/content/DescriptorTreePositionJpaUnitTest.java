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
import com.wci.umls.server.jpa.content.DescriptorJpa;
import com.wci.umls.server.jpa.content.DescriptorTreePositionJpa;
import com.wci.umls.server.jpa.helpers.NullableFieldTester;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.DescriptorTreePosition;

/**
 * Unit testing for {@link DescriptorTreePositionJpa}.
 */
public class DescriptorTreePositionJpaUnitTest {

  /** The model object to test. */
  private DescriptorTreePositionJpa object;

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
    object = new DescriptorTreePositionJpa();
    descriptor1 = new DescriptorJpa();
    descriptor1.setId(1L);
    descriptor1.setName("1");
    descriptor1.setTerminologyId("1");
    descriptor1.setTerminology("1");
    descriptor1.setVersion("1");
    descriptor2 = new DescriptorJpa();
    descriptor2.setId(2L);
    descriptor2.setName("2");
    descriptor2.setTerminologyId("2");
    descriptor2.setTerminology("2");
    descriptor2.setVersion("2");
  }

  /**
   * Test getter and setter methods of model object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelGetSet047() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelGetSet047");
    GetterSetterTester tester = new GetterSetterTester(object);
    tester.exclude("nodeId");
    tester.exclude("nodeTerminologyId");
    tester.exclude("nodeTerminology");
    tester.exclude("nodeVersion");
    tester.exclude("nodeName");
    tester.test();
  }

  /**
   * Test equals and hasdescriptor methods.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelEqualsHashcode047() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelEqualsHashcode047");
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

    tester.proxy(Descriptor.class, 1, new DescriptorJpa(descriptor1, false));
    tester.proxy(Descriptor.class, 2, new DescriptorJpa(descriptor2, false));
    assertTrue(tester.testIdentitiyFieldEquals());
    tester.proxy(Descriptor.class, 1, new DescriptorJpa(descriptor1, false));
    tester.proxy(Descriptor.class, 2, new DescriptorJpa(descriptor2, false));
    assertTrue(tester.testNonIdentitiyFieldEquals());
    tester.proxy(Descriptor.class, 1, new DescriptorJpa(descriptor1, false));
    tester.proxy(Descriptor.class, 2, new DescriptorJpa(descriptor2, false));
    assertTrue(tester.testIdentityFieldNotEquals());
    tester.proxy(Descriptor.class, 1, new DescriptorJpa(descriptor1, false));
    tester.proxy(Descriptor.class, 2, new DescriptorJpa(descriptor2, false));
    assertTrue(tester.testIdentitiyFieldHashcode());
    tester.proxy(Descriptor.class, 1, new DescriptorJpa(descriptor1, false));
    tester.proxy(Descriptor.class, 2, new DescriptorJpa(descriptor2, false));
    assertTrue(tester.testNonIdentitiyFieldHashcode());
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
  public void testModelCopy047() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelCopy047");
    CopyConstructorTester tester = new CopyConstructorTester(object);
    tester.proxy(Descriptor.class, 1, descriptor1);
    tester.proxy(Descriptor.class, 2, descriptor2);
    assertTrue(tester.testCopyConstructorDeep(DescriptorTreePosition.class));
  }

  /**
   * Test XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlSerialization047() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelXmlSerialization047");
    XmlSerializationTester tester = new XmlSerializationTester(object);
    // The proxy descriptors can have only "id" and "name" set due to xml
    // transient
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
  public void testXmlTransient047() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelXmlTransient047");

    object.setNode(descriptor1);
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
  public void testModelNotNullField047() throws Exception {
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
