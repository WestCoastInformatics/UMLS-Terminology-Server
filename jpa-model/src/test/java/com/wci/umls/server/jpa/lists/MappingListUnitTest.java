/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.lists;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.helpers.content.MappingList;
import com.wci.umls.server.jpa.content.MappingJpa;
import com.wci.umls.server.jpa.helpers.content.MappingListJpa;
import com.wci.umls.server.model.content.Mapping;

/**
 * Unit testing for {@link MappingList}.
 */
public class MappingListUnitTest extends AbstractListUnit<Mapping> {

  /** The list test fixture . */
  private MappingList list;

  /** The list2 test fixture . */
  private MappingList list2;

  /** The test fixture s1. */
  private Mapping c1;

  /** The test fixture s2. */
  private Mapping c2;

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
    list = new MappingListJpa();
    list2 = new MappingListJpa();
    c1 = new MappingJpa();
    c1.setId(1L);
    c1.setTerminologyId("1");
    c2 = new MappingJpa();
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
