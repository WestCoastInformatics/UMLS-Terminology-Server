/*
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.lists;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.jpa.actions.MolecularActionJpa;
import com.wci.umls.server.jpa.actions.MolecularActionListJpa;
import com.wci.umls.server.model.actions.MolecularAction;
import com.wci.umls.server.model.actions.MolecularActionList;

/**
 * Unit testing for {@link MolecularActionList}.
 */
public class MolecularActionListUnitTest extends AbstractListUnit<MolecularAction> {

  /** The list test fixture . */
  private MolecularActionList list;

  /** The list2 test fixture . */
  private MolecularActionList list2;

  /** The test fixture s1. */
  private MolecularAction a1;

  /** The test fixture s2. */
  private MolecularAction a2;

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
    a1 = new MolecularActionJpa();
    a1.setId(1L);
    a1.setComponentId(1L);
   
    a2 = new MolecularActionJpa();
    a2.setId(2L);
    a2.setComponentId(2L);

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
