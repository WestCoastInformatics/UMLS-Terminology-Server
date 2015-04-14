/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.lists;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.ReleaseProperty;
import com.wci.umls.server.helpers.ProxyTester;
import com.wci.umls.server.helpers.ReleasePropertyList;
import com.wci.umls.server.jpa.ReleasePropertyJpa;
import com.wci.umls.server.jpa.helpers.ReleasePropertyListJpa;

/**
 * Unit testing for {@link ReleasePropertyList}.
 */
public class ListUnit017Test extends AbstractListUnit<ReleaseProperty> {

  /** The list1 test fixture . */
  private ReleasePropertyList list1;

  /** The list2 test fixture . */
  private ReleasePropertyList list2;

  /** The test fixture o1. */
  private ReleaseProperty o1;

  /** The test fixture o2. */
  private ReleaseProperty o2;

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
    list1 = new ReleasePropertyListJpa();
    list2 = new ReleasePropertyListJpa();

    ProxyTester tester = new ProxyTester(new ReleasePropertyJpa());
    o1 = (ReleaseProperty) tester.createObject(1);
    o2 = (ReleaseProperty) tester.createObject(2);

  }

  /**
   * Test normal use of a list.
   * @throws Exception the exception
   */
  @Test
  public void testNormalUse017() throws Exception {
    testNormalUse(list1, list2, o1, o2);
  }

  /**
   * Test degenerate use of a list. Show that the underlying data structure
   * should NOT be manipulated.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testDegenerateUse017() throws Exception {
    testDegenerateUse(list1, list2, o1, o2);
  }

  /**
   * Test edge cases of a list.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testEdgeCases017() throws Exception {
    testEdgeCases(list1, list2, o1, o2);
  }

  /**
   * Test XML serialization of a list.
   *
   * 
   * @throws Exception the exception
   */
  @Test
  public void testXmlSerialization017() throws Exception {
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
