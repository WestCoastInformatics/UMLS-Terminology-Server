/**
 * Copyright 2016 West Coast Informatics, LLC
 */
/*
 * 
 */
package com.wci.umls.server.jpa.helpers.meta;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

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
 * Unit testing for {@link RootTerminologyJpa}.
 */
public class RootTerminologyJpaUnitTest {

  /** The model object to test. */
  private RootTerminologyJpa object;

  /** The contact info proxy. */
  private ContactInfoJpa contactInfoProxy;

  /** The contact info proxy2. */
  private ContactInfoJpa contactInfoProxy2;

  /** The terminology proxy. */
  private Terminology terminologyProxy;

  /** The terminology proxy2. */
  private Terminology terminologyProxy2;

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
   * @throws Exception
   */
  @Before
  public void setup() throws Exception {
    // set up text fixtures
    object = new RootTerminologyJpa();
    ProxyTester tester = new ProxyTester(new ContactInfoJpa());
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
    tester = new ProxyTester(new TerminologyJpa());

    tester.proxy(Citation.class, 1, citationProxy);
    tester.proxy(Citation.class, 2, citationProxy2);
    tester.proxy(List.class, 1, listProxy);
    tester.proxy(List.class, 2, listProxy2);
    terminologyProxy = (TerminologyJpa) tester.createObject(1);
    terminologyProxy2 = (TerminologyJpa) tester.createObject(2);
  }

  /**
   * Test getter and setter methods of model object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelGetSet033() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelGetSet033");
    GetterSetterTester tester = new GetterSetterTester(object);
    tester.proxy(ContactInfo.class, 1, contactInfoProxy);
    tester.proxy(ContactInfo.class, 2, contactInfoProxy2);
    tester.proxy(Terminology.class, 1, terminologyProxy);
    tester.proxy(Terminology.class, 2, terminologyProxy2);
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
  public void testModelEqualsHashcode033() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelEqualsHashcode033");
    EqualsHashcodeTester tester = new EqualsHashcodeTester(object);
    tester.include("acquisitionContact");
    tester.include("contentContact");
    tester.include("family");
    tester.include("hierarchicalName");
    tester.include("language");
    tester.include("licenseContact");
    tester.include("polyhierarchy");
    tester.include("preferredName");
    tester.include("restrictionLevel");
    tester.include("shortName");
    tester.include("synonymousNames");
    tester.include("terminology");

    tester.proxy(ContactInfo.class, 1, contactInfoProxy);
    tester.proxy(ContactInfo.class, 2, contactInfoProxy2);
    tester.proxy(Terminology.class, 1, terminologyProxy);
    tester.proxy(Terminology.class, 2, terminologyProxy2);
    tester.proxy(Language.class, 1, languageProxy);
    tester.proxy(Language.class, 2, languageProxy2);
    tester.proxy(Citation.class, 1, citationProxy);
    tester.proxy(Citation.class, 2, citationProxy2);
    tester.proxy(List.class, 1, listProxy);
    tester.proxy(List.class, 2, listProxy2);

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
  public void testModelCopy033() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelCopy033");
    CopyConstructorTester tester = new CopyConstructorTester(object);

    tester.proxy(ContactInfo.class, 1, contactInfoProxy);
    tester.proxy(ContactInfo.class, 2, contactInfoProxy2);
    tester.proxy(Terminology.class, 1, terminologyProxy);
    tester.proxy(Terminology.class, 2, terminologyProxy2);
    tester.proxy(Language.class, 1, languageProxy);
    tester.proxy(Language.class, 2, languageProxy2);
    tester.proxy(Citation.class, 1, citationProxy);
    tester.proxy(Citation.class, 2, citationProxy2);
    tester.proxy(List.class, 1, listProxy);
    tester.proxy(List.class, 2, listProxy2);

    assertTrue(tester.testCopyConstructor(RootTerminology.class));
  }

  /**
   * Test XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlSerialization033() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelXmlSerialization033");
    XmlSerializationTester tester = new XmlSerializationTester(object);

    tester.proxy(RootTerminology.class, 1, object);
    tester.proxy(RootTerminology.class, 2, object);
    tester.proxy(ContactInfo.class, 1, contactInfoProxy);
    tester.proxy(ContactInfo.class, 2, contactInfoProxy2);
    tester.proxy(Terminology.class, 1, terminologyProxy);
    tester.proxy(Terminology.class, 2, terminologyProxy2);
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
  public void testModelNotNullField033() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelNotNullField033");
    NullableFieldTester tester = new NullableFieldTester(object);
    tester.include("timestamp");
    tester.include("lastModified");
    tester.include("lastModifiedBy");
    tester.include("terminology");
    tester.include("polyhierarchy");
    tester.include("family");
    tester.include("preferredName");
    tester.include("restrictionLevel");

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
