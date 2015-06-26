/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.lists;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.helpers.ProxyTester;
import com.wci.umls.server.helpers.meta.MarkerSetList;
import com.wci.umls.server.jpa.helpers.meta.MarkerSetListJpa;
import com.wci.umls.server.jpa.meta.MarkerSetJpa;
import com.wci.umls.server.model.meta.MarkerSet;

/**
 * Unit testing for {@link MarkerSetList}.
 */
public class ListUnit029Test extends
    AbstractListUnit<MarkerSet> {

  /** test fixture . */
  private MarkerSetList list1;

  /** test fixture . */
  private MarkerSetList list2;

  /** test fixture. */
  private MarkerSet ms1;

  /** test fixture. */
  private MarkerSet ms2;

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
    list1 = new MarkerSetListJpa();
    list2 = new MarkerSetListJpa();
    
    ProxyTester tester = new ProxyTester(new MarkerSetJpa());
    ms1 = (MarkerSet) tester.createObject(1);
    ms2 = (MarkerSet) tester.createObject(2);
  }

  /**
   * Test normal use of a list.
   * @throws Exception the exception
   */
  @Test
  public void testNormalUse026() throws Exception {
    testNormalUse(list1, list2, ms1, ms2);
  }

  /**
   * Test degenerate use of a list. Show that the underlying data structure
   * should NOT be manipulated.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testDegenerateUse026() throws Exception {
    testDegenerateUse(list1, list2, ms1, ms2);
  }

  /**
   * Test edge cases of a list.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testEdgeCases026() throws Exception {
    testEdgeCases(list1, list2, ms1, ms2);
    list1 = new MarkerSetListJpa();

  }

  /**
   * Test XML serialization of a list.
   *
   * 
   * @throws Exception the exception
   */
  @Test
  public void testXmlSerialization026() throws Exception {
    testXmllSerialization(list1, list2, ms1, ms2);
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
