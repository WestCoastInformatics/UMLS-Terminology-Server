/*
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.lists;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.helpers.ChecklistList;
import com.wci.umls.server.helpers.ProxyTester;
import com.wci.umls.server.jpa.helpers.ChecklistListJpa;
import com.wci.umls.server.jpa.worfklow.ChecklistJpa;
import com.wci.umls.server.model.workflow.Checklist;

/**
 * Unit testing for {@link ChecklistList}.
 */
public class ChecklistListUnitTest extends AbstractListUnit<Checklist> {

  /** The list1 test fixture . */
  private ChecklistList list1;

  /** The list2 test fixture . */
  private ChecklistList list2;

  /** The test fixture o1. */
  private Checklist o1;

  /** The test fixture o2. */
  private Checklist o2;

  /**
   * Setup class.
   */
  @BeforeClass
  public static void setupClass() {
    // do nothing
  }

  /**
   * Setup.
   *
   * @throws Exception the exception
   */
  @Before
  public void setup() throws Exception {
    list1 = new ChecklistListJpa();
    list2 = new ChecklistListJpa();

    ProxyTester tester = new ProxyTester(new ChecklistJpa());
    o1 = (Checklist) tester.createObject(1);
    o2 = (Checklist) tester.createObject(2);
    
    o1.setProject(null);
    o2.setProject(null);

  }

  /**
   * Test normal use of a list.
   * @throws Exception the exception
   */
  @Test
  public void testNormalUse() throws Exception {
    testNormalUse(list1, list2, o1, o2);
  }

  /**
   * Test degenerate use of a list. Show that the underlying data structure
   * should NOT be manipulated.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testDegenerateUse() throws Exception {
    testDegenerateUse(list1, list2, o1, o2);
  }

  /**
   * Test edge cases of a list.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testEdgeCases() throws Exception {
    testEdgeCases(list1, list2, o1, o2);
  }

  /**
   * Test XML serialization of a list.
   *
   * 
   * @throws Exception the exception
   */
  @Test
  public void testXmlSerialization() throws Exception {
    testXmllSerialization(list1, list2, o1, o2);
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
