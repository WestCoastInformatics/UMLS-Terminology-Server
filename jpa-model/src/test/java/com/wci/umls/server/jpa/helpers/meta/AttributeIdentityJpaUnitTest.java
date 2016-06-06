/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers.meta;

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
import com.wci.umls.server.helpers.XmlSerializationTester;
import com.wci.umls.server.jpa.helpers.NullableFieldTester;
import com.wci.umls.server.model.meta.AttributeIdentity;
import com.wci.umls.server.model.meta.IdType;

/**
 * Unit testing for {@link AttributeIdentityJpa}.
 */
public class AttributeIdentityJpaUnitTest {

  /** The model object to test. */
  private AttributeIdentityJpa object;

  /** The test fixture */
  private AttributeIdentityJpa atn;

  /** The test fixture */
  private AttributeIdentityJpa atn2;

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

    object = new AttributeIdentityJpa();
    atn = new AttributeIdentityJpa();
    atn2 = new AttributeIdentityJpa();
    atn.setId(1L);
    atn.setHashCode("9e107d9d372bb6826bd81d3542a419d6");
    atn.setName("name1");
    atn.setOwnerId(1L);
    atn.setOwnerQualifier("qualifier1");
    atn.setOwnerType(IdType.CONCEPT);
    atn.setTerminology("terminology");
    atn.setVersion("version");
    atn.setTerminologyId("1");

    atn2.setId(2L);
    atn2.setHashCode("e4d909c290d0fb1ca068ffaddf22cbd06");
    atn2.setName("name2");
    atn2.setOwnerId(2L);
    atn2.setOwnerQualifier("qualifier2");
    atn2.setOwnerType(IdType.CONCEPT);
    atn2.setTerminology("terminology");
    atn2.setVersion("version");
    atn2.setTerminologyId("2");

  }

  /**
   * Test getter and setter methods of model object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelGetSet026() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelGetSet026");
    GetterSetterTester tester = new GetterSetterTester(object);
    tester.test();
  }

  /**
   * Test equals and hascode methods.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelEqualsHashcode026() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelEqualsHashcode026");
    EqualsHashcodeTester tester = new EqualsHashcodeTester(object);

    tester.include("hashCode");
    tester.include("ownerId");
    tester.include("ownerQualifier");
    tester.include("ownerType");
    tester.include("terminology");
    tester.include("version");
    tester.include("terminologyId");

    tester.exclude("name");

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
  public void testModelCopy026() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelCopy026");
    CopyConstructorTester tester = new CopyConstructorTester(object);
    assertTrue(tester.testCopyConstructor(AttributeIdentity.class));
  }

  /**
   * Test XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlSerialization026() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelXmlSerialization026");
    XmlSerializationTester tester = new XmlSerializationTester(object);
    assertTrue(tester.testXmlSerialization());
  }

  /**
   * Test not null fields.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelNotNullField026() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelNotNullField026");
    NullableFieldTester tester = new NullableFieldTester(object);

    tester.include("hashCode");
    tester.include("name");
    tester.include("ownerId");
    tester.exclude("ownerQualifier");
    tester.include("ownerType");
    tester.include("terminology");
    tester.include("version");
    tester.include("terminologyId");

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
