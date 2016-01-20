/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.lists;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.helpers.ProxyTester;
import com.wci.umls.server.helpers.SearchResult;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.jpa.helpers.SearchResultJpa;
import com.wci.umls.server.jpa.helpers.SearchResultListJpa;

/**
 * Unit testing for {@link SearchResultList}.
 */
public class SearchResultListUnitTest extends AbstractListUnit<SearchResult> {

  /** The list1 test fixture . */
  private SearchResultList list1;

  /** The list2 test fixture . */
  private SearchResultList list2;

  /** The test fixture o1. */
  private SearchResult o1;

  /** The test fixture o2. */
  private SearchResult o2;

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
    list1 = new SearchResultListJpa();
    list2 = new SearchResultListJpa();

    ProxyTester tester = new ProxyTester(new SearchResultJpa());
    o1 = (SearchResult) tester.createObject(1);
    o2 = (SearchResult) tester.createObject(2);

  }

  /**
   * Test normal use of a list.
   * @throws Exception the exception
   */
  @Test
  public void testNormalUse015() throws Exception {
    testNormalUse(list1, list2, o1, o2);
  }

  /**
   * Test degenerate use of a list. Show that the underlying data structure
   * should NOT be manipulated.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testDegenerateUse015() throws Exception {
    testDegenerateUse(list1, list2, o1, o2);
  }

  /**
   * Test edge cases of a list.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testEdgeCases015() throws Exception {
    testEdgeCases(list1, list2, o1, o2);
  }

  /**
   * Test XML serialization of a list.
   *
   * 
   * @throws Exception the exception
   */
  @Test
  public void testXmlSerialization015() throws Exception {
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
