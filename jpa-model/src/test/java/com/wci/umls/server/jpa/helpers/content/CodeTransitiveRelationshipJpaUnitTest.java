/*
 * Copyright 2016 West Coast Informatics, LLC
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
import com.wci.umls.server.jpa.content.CodeJpa;
import com.wci.umls.server.jpa.content.CodeTransitiveRelationshipJpa;
import com.wci.umls.server.jpa.helpers.NullableFieldTester;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.CodeTransitiveRelationship;

/**
 * Unit testing for {@link CodeTransitiveRelationshipJpa}.
 */
public class CodeTransitiveRelationshipJpaUnitTest {

  /** The model object to test. */
  private CodeTransitiveRelationshipJpa object;

  /** test fixture */
  private Code code1;

  /** test fixture */
  private Code code2;

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
    object = new CodeTransitiveRelationshipJpa();

    ProxyTester tester = new ProxyTester(new CodeJpa());
    code1 = (CodeJpa) tester.createObject(1);
    code2 = (CodeJpa) tester.createObject(2);

    object.setSuperType(code1);
    object.setSubType(code2);
  }

  /**
   * Test getter and setter methods of model object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelGetSet045() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelGetSet045");
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
    tester.test();
  }

  /**
   * Test equals and hascode methods.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelEqualsHashcode045() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelEqualsHashcode045");
    EqualsHashcodeTester tester = new EqualsHashcodeTester(object);
    tester.include("suppressible");
    tester.include("obsolete");
    tester.include("publishable");
    tester.include("published");
    tester.include("terminology");
    tester.include("terminologyId");
    tester.include("version");
    tester.include("superType");
    tester.include("subType");

    tester.proxy(Code.class, 1, new CodeJpa(code1, false));
    tester.proxy(Code.class, 2, new CodeJpa(code2, false));
    assertTrue(tester.testIdentityFieldEquals());
    tester.proxy(Code.class, 1, new CodeJpa(code1, false));
    tester.proxy(Code.class, 2, new CodeJpa(code2, false));
    assertTrue(tester.testNonIdentityFieldEquals());
    tester.proxy(Code.class, 1, new CodeJpa(code1, false));
    tester.proxy(Code.class, 2, new CodeJpa(code2, false));
    assertTrue(tester.testIdentityFieldNotEquals());
    tester.proxy(Code.class, 1, new CodeJpa(code1, false));
    tester.proxy(Code.class, 2, new CodeJpa(code2, false));
    assertTrue(tester.testIdentityFieldHashcode());
    tester.proxy(Code.class, 1, new CodeJpa(code1, false));
    tester.proxy(Code.class, 2, new CodeJpa(code2, false));
    assertTrue(tester.testNonIdentityFieldHashcode());
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
  public void testModelCopy045() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelCopy045");
    CopyConstructorTester tester = new CopyConstructorTester(object);
    tester.proxy(Code.class, 1, code1);
    tester.proxy(Code.class, 2, code2);
    assertTrue(tester.testCopyConstructorDeep(CodeTransitiveRelationship.class));
  }

  /**
   * Test XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlSerialization045() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelXmlSerialization045");
    XmlSerializationTester tester = new XmlSerializationTester(object);
    // The proxy codes can have only "id" and "term" set due to xml
    // transient
    Code code1 = new CodeJpa();
    code1.setId(1L);
    code1.setName("1");
    Code code2 = new CodeJpa();
    code2.setId(2L);
    code2.setName("2");

    tester.proxy(Code.class, 1, code1);
    tester.proxy(Code.class, 2, code2);
    assertTrue(tester.testXmlSerialization());
  }

  /**
   * Test xml transient fields
   *
   * @throws Exception the exception
   */
  @Test
  public void testXmlTransient045() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelXmlTransient045");

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
  public void testModelNotNullField045() throws Exception {
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
    tester.include("terminologyId");
    tester.include("version");
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
