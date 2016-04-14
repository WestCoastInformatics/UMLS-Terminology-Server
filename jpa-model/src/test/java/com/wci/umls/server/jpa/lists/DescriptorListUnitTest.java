/*
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.lists;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.helpers.content.DescriptorList;
import com.wci.umls.server.jpa.content.DescriptorJpa;
import com.wci.umls.server.jpa.helpers.content.DescriptorListJpa;
import com.wci.umls.server.model.content.Descriptor;

/**
 * Unit testing for {@link DescriptorList}.
 */
public class DescriptorListUnitTest extends AbstractListUnit<Descriptor> {

  /** The list test fixture . */
  private DescriptorList list;

  /** The list2 test fixture . */
  private DescriptorList list2;

  /** The test fixture s1. */
  private Descriptor d1;

  /** The test fixture s2. */
  private Descriptor d2;

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
    list = new DescriptorListJpa();
    list2 = new DescriptorListJpa();
    d1 = new DescriptorJpa();
    d1.setId(1L);
    d1.setTerminologyId("1");
    d2 = new DescriptorJpa();
    d2.setId(2L);
    d2.setTerminologyId("2");

  }

  /**
   * Test normal use of a list.
   * @throws Exception the exception
   */
  @Test
  public void testNormalUse005() throws Exception {
    testNormalUse(list, list2, d1, d2);
  }

  /**
   * Test degenerate use of a list. Show that the underlying data structure
   * should NOT be manipulated.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testDegenerateUse005() throws Exception {
    testDegenerateUse(list, list2, d1, d2);
  }

  /**
   * Test edge cases of a list.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testEdgeCases005() throws Exception {
    testEdgeCases(list, list2, d1, d2);
  }

  /**
   * Test XML serialization of a list.
   *
   * 
   * @throws Exception the exception
   */
  @Test
  public void testXmlSerialization005() throws Exception {
    testXmllSerialization(list, list2, d1, d2);
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
