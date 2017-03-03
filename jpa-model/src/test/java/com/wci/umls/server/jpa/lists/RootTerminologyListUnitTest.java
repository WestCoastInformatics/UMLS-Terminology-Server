/*
 * Copyright 2315 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.lists;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.helpers.ProxyTester;
import com.wci.umls.server.helpers.meta.RootTerminologyList;
import com.wci.umls.server.jpa.helpers.meta.RootTerminologyListJpa;
import com.wci.umls.server.jpa.meta.ContactInfoJpa;
import com.wci.umls.server.jpa.meta.RootTerminologyJpa;
import com.wci.umls.server.model.meta.ContactInfo;
import com.wci.umls.server.model.meta.RootTerminology;

/**
 * Unit testing for {@link RootTerminologyList}.
 */
public class RootTerminologyListUnitTest extends
    AbstractListUnit<RootTerminology> {

  /** The list1 test fixture . */
  private RootTerminologyList list1;

  /** The list2 test fixture . */
  private RootTerminologyList list2;

  /** The test fixture o1. */
  private RootTerminology o1;

  /** The test fixture o2. */
  private RootTerminology o2;

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
    list1 = new RootTerminologyListJpa();
    list2 = new RootTerminologyListJpa();

    ProxyTester tester = new ProxyTester(new RootTerminologyJpa());
    o1 = (RootTerminology) tester.createObject(1);
    o2 = (RootTerminology) tester.createObject(2);

    ProxyTester tester2 = new ProxyTester(new ContactInfoJpa());
    ContactInfo info = (ContactInfo) tester2.createObject(1);
    ContactInfo info2 = (ContactInfo) tester2.createObject(2);

    o1.setAcquisitionContact(info);
    o1.setContentContact(info);
    o1.setLicenseContact(info);

    o2.setAcquisitionContact(info2);
    o2.setContentContact(info2);
    o2.setLicenseContact(info2);

    o1.setLanguage("1");
    o2.setLanguage("2");

  }

  /**
   * Test normal use of a list.
   * @throws Exception the exception
   */
  @Test
  public void testNormalUse() throws Exception {
    testNormalUse(list1, list2, o1, o2);
  }

  /**
   * Test degenerate use of a list. Show that the underlying data structure
   * should NOT be manipulated.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testDegenerateUse() throws Exception {
    testDegenerateUse(list1, list2, o1, o2);
  }

  /**
   * Test edge cases of a list.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testEdgeCases() throws Exception {
    testEdgeCases(list1, list2, o1, o2);
  }

  /**
   * Test XML serialization of a list.
   *
   * 
   * @throws Exception the exception
   */
  @Test
  public void testXmlSerialization() throws Exception {
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
