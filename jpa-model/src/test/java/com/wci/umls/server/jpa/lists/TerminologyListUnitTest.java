/*
 * Copyright 2515 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.lists;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.helpers.ProxyTester;
import com.wci.umls.server.helpers.meta.TerminologyList;
import com.wci.umls.server.jpa.helpers.meta.TerminologyListJpa;
import com.wci.umls.server.jpa.meta.CitationJpa;
import com.wci.umls.server.jpa.meta.ContactInfoJpa;
import com.wci.umls.server.jpa.meta.RootTerminologyJpa;
import com.wci.umls.server.jpa.meta.TerminologyJpa;
import com.wci.umls.server.model.meta.Citation;
import com.wci.umls.server.model.meta.ContactInfo;
import com.wci.umls.server.model.meta.RootTerminology;
import com.wci.umls.server.model.meta.Terminology;

/**
 * Unit testing for {@link TerminologyList}.
 */
public class TerminologyListUnitTest extends AbstractListUnit<Terminology> {

  /** The list1 test fixture . */
  private TerminologyList list1;

  /** The list2 test fixture . */
  private TerminologyList list2;

  /** The test fixture o1. */
  private Terminology o1;

  /** The test fixture o2. */
  private Terminology o2;

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
    list1 = new TerminologyListJpa();
    list2 = new TerminologyListJpa();

    ProxyTester tester = new ProxyTester(new TerminologyJpa());
    o1 = (Terminology) tester.createObject(1);
    o2 = (Terminology) tester.createObject(2);

    ProxyTester tester2 = new ProxyTester(new CitationJpa());
    Citation cit = (Citation) tester2.createObject(1);
    Citation cit2 = (Citation) tester2.createObject(2);
    o1.setCitation(cit);
    o2.setCitation(cit2);

    ProxyTester tester3 = new ProxyTester(new RootTerminologyJpa());
    RootTerminology rsab = (RootTerminology) tester3.createObject(1);
    RootTerminology rsab2 = (RootTerminology) tester3.createObject(2);
    o1.setRootTerminology(rsab);
    o1.setRootTerminology(rsab2);

    ProxyTester tester4 = new ProxyTester(new ContactInfoJpa());
    ContactInfo info = (ContactInfo) tester4.createObject(1);
    ContactInfo info2 = (ContactInfo) tester4.createObject(2);
    rsab.setAcquisitionContact(info);
    rsab.setContentContact(info);
    rsab.setLicenseContact(info);
    rsab2.setAcquisitionContact(info2);
    rsab2.setContentContact(info2);
    rsab2.setLicenseContact(info2);

    rsab.setLanguage("1");
    rsab2.setLanguage("2");

  }

  /**
   * Test normal use of a list.
   * @throws Exception the exception
   */
  @Test
  public void testNormalUse025() throws Exception {
    testNormalUse(list1, list2, o1, o2);
  }

  /**
   * Test degenerate use of a list. Show that the underlying data structure
   * should NOT be manipulated.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testDegenerateUse025() throws Exception {
    testDegenerateUse(list1, list2, o1, o2);
  }

  /**
   * Test edge cases of a list.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testEdgeCases025() throws Exception {
    testEdgeCases(list1, list2, o1, o2);
  }

  /**
   * Test XML serialization of a list.
   *
   * 
   * @throws Exception the exception
   */
  @Test
  public void testXmlSerialization025() throws Exception {
    testXmllSerialization(list1, list2, o1, o2);
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
