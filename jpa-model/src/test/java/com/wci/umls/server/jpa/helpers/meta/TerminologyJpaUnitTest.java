/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers.meta;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

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
import com.wci.umls.server.helpers.ProxyTester;
import com.wci.umls.server.helpers.XmlSerializationTester;
import com.wci.umls.server.jpa.helpers.NullableFieldTester;
import com.wci.umls.server.jpa.meta.CitationJpa;
import com.wci.umls.server.jpa.meta.ContactInfoJpa;
import com.wci.umls.server.jpa.meta.LanguageJpa;
import com.wci.umls.server.jpa.meta.RootTerminologyJpa;
import com.wci.umls.server.jpa.meta.TerminologyJpa;
import com.wci.umls.server.model.meta.Citation;
import com.wci.umls.server.model.meta.ContactInfo;
import com.wci.umls.server.model.meta.Language;
import com.wci.umls.server.model.meta.RootTerminology;
import com.wci.umls.server.model.meta.Terminology;

/**
 * Unit testing for {@link TerminologyJpa}.
 */
public class TerminologyJpaUnitTest {

  /** The model object to test. */
  private TerminologyJpa object;

  /** The root terminology proxy. */
  private RootTerminology rootTerminologyProxy;

  /** The root terminology proxy2. */
  private RootTerminology rootTerminologyProxy2;

  /** The contact info proxy. */
  private ContactInfoJpa contactInfoProxy;

  /** The contact info proxy2. */
  private ContactInfoJpa contactInfoProxy2;

  /** The language proxy. */
  private Language languageProxy;

  /** The language proxy2. */
  private Language languageProxy2;

  /** The citation proxy. */
  private Citation citationProxy;

  /** The citation proxy2. */
  private Citation citationProxy2;

  /** The list proxy. */
  private List<String> listProxy;

  /** The list proxy2. */
  private List<String> listProxy2;

  /**
   * Setup class.
   */
  @BeforeClass
  public static void setupClass() {
    // do nothing
  }

  /**
   * Setup.
   *
   * @throws Exception the exception
   */
  @Before
  public void setup() throws Exception {
    // set up text fixtures
    object = new TerminologyJpa();
    ProxyTester tester = new ProxyTester(new RootTerminologyJpa());
    rootTerminologyProxy = (RootTerminologyJpa) tester.createObject(1);
    rootTerminologyProxy2 = (RootTerminologyJpa) tester.createObject(2);
    tester = new ProxyTester(new ContactInfoJpa());
    contactInfoProxy = (ContactInfoJpa) tester.createObject(1);
    contactInfoProxy2 = (ContactInfoJpa) tester.createObject(2);
    tester = new ProxyTester(new LanguageJpa());
    languageProxy = (LanguageJpa) tester.createObject(1);
    languageProxy2 = (LanguageJpa) tester.createObject(2);
    tester = new ProxyTester(new CitationJpa());
    citationProxy = (CitationJpa) tester.createObject(1);
    citationProxy2 = (CitationJpa) tester.createObject(2);
    listProxy = new ArrayList<>();
    listProxy.add("1");
    listProxy2 = new ArrayList<>();
    listProxy.add("2");
  }

  /**
   * Test getter and setter methods of model object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelGetSet040() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelGetSet040");
    GetterSetterTester tester = new GetterSetterTester(object);

    tester.proxy(RootTerminology.class, 1, rootTerminologyProxy);
    tester.proxy(RootTerminology.class, 2, rootTerminologyProxy2);
    tester.proxy(ContactInfo.class, 1, contactInfoProxy);
    tester.proxy(ContactInfo.class, 2, contactInfoProxy2);
    tester.proxy(Language.class, 1, languageProxy);
    tester.proxy(Language.class, 2, languageProxy2);
    tester.proxy(Citation.class, 1, citationProxy);
    tester.proxy(Citation.class, 2, citationProxy2);
    tester.proxy(List.class, 1, listProxy);
    tester.proxy(List.class, 2, listProxy2);

    tester.test();
  }

  /**
   * Test equals and hascode methods.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelEqualsHashcode040() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelEqualsHashcode040");
    EqualsHashcodeTester tester = new EqualsHashcodeTester(object);
    tester.include("assertsRelDirection");
    tester.include("citation");
    tester.include("current");
    tester.include("metathesaurus");
    tester.include("endDate");
    tester.include("organizingClassType");
    tester.include("preferredName");
    tester.include("startDate");
    tester.include("synonymousNames");
    tester.include("terminology");
    tester.include("version");
    tester.include("descriptionLogicTerminology");
    tester.include("descriptionLogicProfile");

    tester.proxy(RootTerminology.class, 1, rootTerminologyProxy);
    tester.proxy(RootTerminology.class, 2, rootTerminologyProxy2);
    tester.proxy(ContactInfo.class, 1, contactInfoProxy);
    tester.proxy(ContactInfo.class, 2, contactInfoProxy2);
    tester.proxy(Language.class, 1, languageProxy);
    tester.proxy(Language.class, 2, languageProxy2);
    tester.proxy(Citation.class, 1, citationProxy);
    tester.proxy(Citation.class, 2, citationProxy2);
    tester.proxy(List.class, 1, listProxy);
    tester.proxy(List.class, 2, listProxy2);

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
  public void testModelCopy040() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelCopy040");
    CopyConstructorTester tester = new CopyConstructorTester(object);

    tester.proxy(RootTerminology.class, 1, rootTerminologyProxy);
    tester.proxy(RootTerminology.class, 2, rootTerminologyProxy2);
    tester.proxy(ContactInfo.class, 1, contactInfoProxy);
    tester.proxy(ContactInfo.class, 2, contactInfoProxy2);
    tester.proxy(Language.class, 1, languageProxy);
    tester.proxy(Language.class, 2, languageProxy2);
    tester.proxy(Citation.class, 1, citationProxy);
    tester.proxy(Citation.class, 2, citationProxy2);
    tester.proxy(List.class, 1, listProxy);
    tester.proxy(List.class, 2, listProxy2);

    assertTrue(tester.testCopyConstructor(Terminology.class));
  }

  /**
   * Test XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlSerialization040() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelXmlSerialization040");
    XmlSerializationTester tester = new XmlSerializationTester(object);

    tester.proxy(RootTerminology.class, 1, rootTerminologyProxy);
    tester.proxy(RootTerminology.class, 2, rootTerminologyProxy2);
    tester.proxy(ContactInfo.class, 1, contactInfoProxy);
    tester.proxy(ContactInfo.class, 2, contactInfoProxy2);
    tester.proxy(Language.class, 1, languageProxy);
    tester.proxy(Language.class, 2, languageProxy2);
    tester.proxy(Citation.class, 1, citationProxy);
    tester.proxy(Citation.class, 2, citationProxy2);
    tester.proxy(List.class, 1, listProxy);
    tester.proxy(List.class, 2, listProxy2);

    assertTrue(tester.testXmlSerialization());
  }

  /**
   * Test not null fields.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelNotNullField040() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelNotNullField040");
    NullableFieldTester tester = new NullableFieldTester(object);
    tester.include("timestamp");
    tester.include("lastModified");
    tester.include("lastModifiedBy");
    tester.include("terminology");
    tester.include("version");
    tester.include("organizingClassType");
    tester.include("preferredName");
    tester.include("rootTerminology");
    tester.include("assertsRelDirection");
    tester.include("assertsRelDirection");
    tester.include("current");
    tester.include("metathesaurus");
    tester.include("descriptionLogicTerminology");

    assertTrue(tester.testNotNullFields());
  }

  /**
   * Test for root terminology reference in XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlTransient040() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelXmlTransient040");
    ProxyTester tester = new ProxyTester(new TerminologyJpa());
    tester.proxy(RootTerminology.class, 1, rootTerminologyProxy);
    tester.proxy(RootTerminology.class, 2, rootTerminologyProxy2);
    tester.proxy(ContactInfo.class, 1, contactInfoProxy);
    tester.proxy(ContactInfo.class, 2, contactInfoProxy2);
    tester.proxy(Language.class, 1, languageProxy);
    tester.proxy(Language.class, 2, languageProxy2);
    tester.proxy(Citation.class, 1, citationProxy);
    tester.proxy(Citation.class, 2, citationProxy2);
    tester.proxy(List.class, 1, listProxy);
    tester.proxy(List.class, 2, listProxy2);
    TerminologyJpa terminology = (TerminologyJpa) tester.createObject(1);

    String xml = ConfigUtility.getStringForGraph(terminology);
    assertTrue(xml.contains("<rootTerminologyId>"));
    assertTrue(xml.contains("<rootTerminologyAbbreviation>"));
    Assert.assertFalse(xml.contains("<rootTerminology>"));

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
