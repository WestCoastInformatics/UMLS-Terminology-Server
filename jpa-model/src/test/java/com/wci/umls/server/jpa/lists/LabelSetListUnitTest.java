/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.lists;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.helpers.ProxyTester;
import com.wci.umls.server.helpers.meta.LabelSetList;
import com.wci.umls.server.jpa.helpers.meta.LabelSetListJpa;
import com.wci.umls.server.jpa.meta.LabelSetJpa;
import com.wci.umls.server.model.meta.LabelSet;

/**
 * Unit testing for {@link LabelSetList}.
 */
public class LabelSetListUnitTest extends AbstractListUnit<LabelSet> {

  /** test fixture . */
  private LabelSetList list1;

  /** test fixture . */
  private LabelSetList list2;

  /** test fixture. */
  private LabelSet ms1;

  /** test fixture. */
  private LabelSet ms2;

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
    list1 = new LabelSetListJpa();
    list2 = new LabelSetListJpa();

    ProxyTester tester = new ProxyTester(new LabelSetJpa());
    ms1 = (LabelSet) tester.createObject(1);
    ms2 = (LabelSet) tester.createObject(2);
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
    list1 = new LabelSetListJpa();

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
