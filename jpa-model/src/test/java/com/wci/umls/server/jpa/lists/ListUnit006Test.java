/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.lists;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.helpers.content.AttributeList;
import com.wci.umls.server.jpa.content.AttributeJpa;
import com.wci.umls.server.jpa.helpers.content.AttributeListJpa;
import com.wci.umls.server.model.content.Attribute;

/**
 * Unit testing for {@link AttributeList}.
 */
public class ListUnit006Test extends AbstractListUnit<Attribute> {

  /** The list test fixture . */
  private AttributeList list;

  /** The list2 test fixture . */
  private AttributeList list2;

  /** The test fixture s1. */
  private Attribute a1;

  /** The test fixture s2. */
  private Attribute a2;

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
    list = new AttributeListJpa();
    list2 = new AttributeListJpa();
    a1 = new AttributeJpa();
    a1.setId(1L);
    a1.setTerminologyId("1");
    a2 = new AttributeJpa();
    a2.setId(2L);
    a2.setTerminologyId("2");

  }

  /**
   * Test normal use of a list.
   * @throws Exception the exception
   */
  @Test
  public void testNormalUse006() throws Exception {
    testNormalUse(list, list2, a1, a2);
  }

  /**
   * Test degenerate use of a list. Show that the underlying data structure
   * should NOT be manipulated.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testDegenerateUse006() throws Exception {
    testDegenerateUse(list, list2, a1, a2);
  }

  /**
   * Test edge cases of a list.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testEdgeCases006() throws Exception {
    testEdgeCases(list, list2, a1, a2);
  }

  /**
   * Test XML serialization of a list.
   *
   * 
   * @throws Exception the exception
   */
  @Test
  public void testXmlSerialization006() throws Exception {
    testXmllSerialization(list, list2, a1, a2);
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
