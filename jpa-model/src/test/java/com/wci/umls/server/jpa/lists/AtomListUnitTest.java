/*
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.lists;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.helpers.content.AtomList;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.helpers.content.AtomListJpa;
import com.wci.umls.server.model.content.Atom;

/**
 * Unit testing for {@link AtomList}.
 */
public class AtomListUnitTest extends AbstractListUnit<Atom> {

  /** The list test fixture . */
  private AtomList list;

  /** The list2 test fixture . */
  private AtomList list2;

  /** The test fixture s1. */
  private Atom a1;

  /** The test fixture s2. */
  private Atom a2;

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
    list = new AtomListJpa();
    list2 = new AtomListJpa();
    a1 = new AtomJpa();
    a1.setId(1L);
    a1.setTerminologyId("1");
    a1.setName("1");
    a2 = new AtomJpa();
    a2.setId(2L);
    a2.setTerminologyId("2");
    a2.setName("2");

  }

  /**
   * Test normal use of a list.
   * @throws Exception the exception
   */
  @Test
  public void testNormalUse() throws Exception {
    testNormalUse(list, list2, a1, a2);
  }

  /**
   * Test degenerate use of a list. Show that the underlying data structure
   * should NOT be manipulated.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testDegenerateUse() throws Exception {
    testDegenerateUse(list, list2, a1, a2);
  }

  /**
   * Test edge cases of a list.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testEdgeCases() throws Exception {
    testEdgeCases(list, list2, a1, a2);
  }

  /**
   * Test XML serialization of a list.
   *
   * 
   * @throws Exception the exception
   */
  @Test
  public void testXmlSerialization() throws Exception {
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
