/*
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.lists;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.helpers.content.StringClassList;
import com.wci.umls.server.jpa.content.StringClassJpa;
import com.wci.umls.server.jpa.helpers.content.StringClassListJpa;
import com.wci.umls.server.model.content.StringClass;

/**
 * Unit testing for {@link StringClassList}.
 */
public class StringClassListUnitTest extends AbstractListUnit<StringClass> {

  /** The list test fixture . */
  private StringClassList list;

  /** The list2 test fixture . */
  private StringClassList list2;

  /** The test fixture s1. */
  private StringClass s1;

  /** The test fixture s2. */
  private StringClass s2;

  /**
   * Setup class.
   */
  @BeforeClass
  public static void setupClass() {
    // do nothing
  }

  /**
   * Setup.
   */
  @Before
  public void setup() {
    list = new StringClassListJpa();
    list2 = new StringClassListJpa();
    s1 = new StringClassJpa();
    s1.setId(1L);
    s1.setTerminologyId("1");
    s2 = new StringClassJpa();
    s2.setId(2L);
    s2.setTerminologyId("2");

  }

  /**
   * Test normal use of a list.
   * @throws Exception the exception
   */
  @Test
  public void testNormalUse007() throws Exception {
    testNormalUse(list, list2, s1, s2);
  }

  /**
   * Test degenerate use of a list. Show that the underlying data structure
   * should NOT be manipulated.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testDegenerateUse007() throws Exception {
    testDegenerateUse(list, list2, s1, s2);
  }

  /**
   * Test edge cases of a list.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testEdgeCases007() throws Exception {
    testEdgeCases(list, list2, s1, s2);
  }

  /**
   * Test XML serialization of a list.
   *
   * 
   * @throws Exception the exception
   */
  @Test
  public void testXmlSerialization007() throws Exception {
    testXmllSerialization(list, list2, s1, s2);
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
