/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.lists;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.helpers.content.LexicalClassList;
import com.wci.umls.server.jpa.content.LexicalClassJpa;
import com.wci.umls.server.jpa.helpers.content.LexicalClassListJpa;
import com.wci.umls.server.model.content.LexicalClass;

/**
 * Unit testing for {@link LexicalClassList}.
 */
public class ListUnit004Test extends AbstractListUnit<LexicalClass> {

  /** The list test fixture . */
  private LexicalClassList list;

  /** The list2 test fixture . */
  private LexicalClassList list2;

  /** The test fixture s1. */
  private LexicalClass l1;

  /** The test fixture s2. */
  private LexicalClass l2;

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
    list = new LexicalClassListJpa();
    list2 = new LexicalClassListJpa();
    l1 = new LexicalClassJpa();
    l1.setId(1L);
    l1.setTerminologyId("1");
    l2 = new LexicalClassJpa();
    l2.setId(2L);
    l2.setTerminologyId("2");

  }

  /**
   * Test normal use of a list.
   * @throws Exception the exception
   */
  @Test
  public void testNormalUse004() throws Exception {
    testNormalUse(list, list2, l1, l2);
  }

  /**
   * Test degenerate use of a list. Show that the underlying data structure
   * should NOT be manipulated.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testDegenerateUse004() throws Exception {
    testDegenerateUse(list, list2, l1, l2);
  }

  /**
   * Test edge cases of a list.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testEdgeCases004() throws Exception {
    testEdgeCases(list, list2, l1, l2);
  }

  /**
   * Test XML serialization of a list.
   *
   * 
   * @throws Exception the exception
   */
  @Test
  public void testXmlSerialization004() throws Exception {
    testXmllSerialization(list, list2, l1, l2);
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
