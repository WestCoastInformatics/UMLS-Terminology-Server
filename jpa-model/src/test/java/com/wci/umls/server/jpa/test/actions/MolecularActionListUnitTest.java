/*
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.test.actions;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.jpa.actions.MolecularActionJpa;
import com.wci.umls.server.jpa.actions.MolecularActionListJpa;
import com.wci.umls.server.jpa.lists.AbstractListUnit;
import com.wci.umls.server.model.actions.MolecularAction;
import com.wci.umls.server.model.actions.MolecularActionList;

/**
 * Unit testing for {@link MolecularActionList}.
 */
public class MolecularActionListUnitTest extends
    AbstractListUnit<MolecularAction> {

  /** The list test fixture . */
  private MolecularActionList list;

  /** The list2 test fixture . */
  private MolecularActionList list2;

  /** The test fixture s1. */
  private MolecularAction c1;

  /** The test fixture s2. */
  private MolecularAction c2;

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
    list = new MolecularActionListJpa();
    list2 = new MolecularActionListJpa();
    c1 = new MolecularActionJpa();
    c1.setId(1L);
    c1.setComponentId(1L);
    c2 = new MolecularActionJpa();
    c2.setId(2L);
    c2.setComponentId(2L);

  }

  /**
   * Test normal use of a list.
   * @throws Exception the exception
   */
  @Test
  public void testNormalUse() throws Exception {
    testNormalUse(list, list2, c1, c2);
  }

  /**
   * Test degenerate use of a list. Show that the underlying data structure
   * should NOT be manipulated.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testDegenerateUse() throws Exception {
    testDegenerateUse(list, list2, c1, c2);
  }

  /**
   * Test edge cases of a list.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testEdgeCases() throws Exception {
    testEdgeCases(list, list2, c1, c2);
  }

  /**
   * Test XML serialization of a list.
   *
   * 
   * @throws Exception the exception
   */
  @Test
  public void testXmlSerialization() throws Exception {
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
