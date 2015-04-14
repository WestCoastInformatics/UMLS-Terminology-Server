/*
 * Copyright 2115 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.lists;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.helpers.ProxyTester;
import com.wci.umls.server.helpers.meta.IdentifierTypeList;
import com.wci.umls.server.jpa.helpers.meta.IdentifierTypeListJpa;
import com.wci.umls.server.jpa.meta.IdentifierTypeJpa;
import com.wci.umls.server.model.meta.IdentifierType;

/**
 * Unit testing for {@link IdentifierTypeList}.
 */
public class ListUnit021Test extends AbstractListUnit<IdentifierType> {

  /** The list1 test fixture . */
  private IdentifierTypeList list1;

  /** The list2 test fixture . */
  private IdentifierTypeList list2;

  /** The test fixture o1. */
  private IdentifierType o1;

  /** The test fixture o2. */
  private IdentifierType o2;

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
    list1 = new IdentifierTypeListJpa();
    list2 = new IdentifierTypeListJpa();

    ProxyTester tester = new ProxyTester(new IdentifierTypeJpa());
    o1 = (IdentifierType) tester.createObject(1);
    o2 = (IdentifierType) tester.createObject(2);

  }

  /**
   * Test normal use of a list.
   * @throws Exception the exception
   */
  @Test
  public void testNormalUse021() throws Exception {
    testNormalUse(list1, list2, o1, o2);
  }

  /**
   * Test degenerate use of a list. Show that the underlying data structure
   * should NOT be manipulated.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testDegenerateUse021() throws Exception {
    testDegenerateUse(list1, list2, o1, o2);
  }

  /**
   * Test edge cases of a list.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testEdgeCases021() throws Exception {
    testEdgeCases(list1, list2, o1, o2);
  }

  /**
   * Test XML serialization of a list.
   *
   * 
   * @throws Exception the exception
   */
  @Test
  public void testXmlSerialization021() throws Exception {
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
