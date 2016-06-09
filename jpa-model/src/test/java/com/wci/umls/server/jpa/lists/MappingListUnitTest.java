/*
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.lists;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.helpers.content.MappingList;
import com.wci.umls.server.jpa.content.MapSetJpa;
import com.wci.umls.server.jpa.content.MappingJpa;
import com.wci.umls.server.jpa.content.MappingJpa;
import com.wci.umls.server.jpa.helpers.content.MappingListJpa;
import com.wci.umls.server.model.content.MapSet;
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
  private Mapping m1;

  /** The test fixture s2. */
  private Mapping m2;

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
    MapSet ms1 = new MapSetJpa();
    ms1.setId(1L);
    ms1.setTerminologyId("1");
    MapSet ms2 = new MapSetJpa();
    ms2.setId(2L);
    ms2.setTerminologyId("2");

    m1 = new MappingJpa();
    m1.setId(1L);
    m1.setTerminologyId("1");
    m1.setMapSet(ms1);

    m2 = new MappingJpa();
    m2.setId(2L);
    m2.setTerminologyId("2");
    m2.setMapSet(ms2);

  }

  /**
   * Test normal use of a list.
   * @throws Exception the exception
   */
  @Test
  public void testNormalUse002() throws Exception {
    testNormalUse(list, list2, m1, m2);
  }

  /**
   * Test degenerate use of a list. Show that the underlying data structure
   * should NOT be manipulated.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testDegenerateUse002() throws Exception {
    testDegenerateUse(list, list2, m1, m2);
  }

  /**
   * Test edge cases of a list.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testEdgeCases002() throws Exception {
    testEdgeCases(list, list2, m1, m2);
  }

  /**
   * Test XML serialization of a list.
   *
   * 
   * @throws Exception the exception
   */
  @Test
  public void testXmlSerialization002() throws Exception {
    testXmllSerialization(list, list2, m1, m2);
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
