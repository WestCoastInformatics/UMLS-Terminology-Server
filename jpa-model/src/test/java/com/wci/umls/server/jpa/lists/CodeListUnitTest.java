/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.lists;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.helpers.content.CodeList;
import com.wci.umls.server.jpa.content.CodeJpa;
import com.wci.umls.server.jpa.helpers.content.CodeListJpa;
import com.wci.umls.server.model.content.Code;

/**
 * Unit testing for {@link CodeList}.
 */
public class CodeListUnitTest extends AbstractListUnit<Code> {

  /** The list test fixture . */
  private CodeList list;

  /** The list2 test fixture . */
  private CodeList list2;

  /** The test fixture s1. */
  private Code c1;

  /** The test fixture s2. */
  private Code c2;

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
    list = new CodeListJpa();
    list2 = new CodeListJpa();
    c1 = new CodeJpa();
    c1.setId(1L);
    c1.setTerminologyId("1");
    c2 = new CodeJpa();
    c2.setId(2L);
    c2.setTerminologyId("2");

  }

  /**
   * Test normal use of a list.
   * @throws Exception the exception
   */
  @Test
  public void testNormalUse002() throws Exception {
    testNormalUse(list, list2, c1, c2);
  }

  /**
   * Test degenerate use of a list. Show that the underlying data structure
   * should NOT be manipulated.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testDegenerateUse002() throws Exception {
    testDegenerateUse(list, list2, c1, c2);
  }

  /**
   * Test edge cases of a list.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testEdgeCases002() throws Exception {
    testEdgeCases(list, list2, c1, c2);
  }

  /**
   * Test XML serialization of a list.
   *
   * 
   * @throws Exception the exception
   */
  @Test
  public void testXmlSerialization002() throws Exception {
    testXmllSerialization(list, list2, c1, c2);
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
