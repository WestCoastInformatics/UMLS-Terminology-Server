/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.lists;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.helpers.ProxyTester;
import com.wci.umls.server.helpers.content.Tree;
import com.wci.umls.server.helpers.content.TreeList;
import com.wci.umls.server.jpa.helpers.content.TreeJpa;
import com.wci.umls.server.jpa.helpers.content.TreeListJpa;

/**
 * Unit testing for {@link TreeList}.
 */
public class ListUnit028Test extends AbstractListUnit<Tree> {

  /** test fixture . */
  private TreeList list1;

  /** test fixture . */
  private TreeList list2;

  /** test fixture. */
  private Tree t1;

  /** test fixture. */
  private Tree t2;

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
    list1 = new TreeListJpa();
    list2 = new TreeListJpa();

    ProxyTester tester = new ProxyTester(new TreeJpa());
    t1 = (TreeJpa) tester.createObject(1);
    TreeJpa t11 = (TreeJpa) tester.createObject(11);
    t2 = (TreeJpa) tester.createObject(1);
    TreeJpa t22 = (TreeJpa) tester.createObject(22);
    List<Tree> list = new ArrayList<>();
    list.add(t11);
    t1.setChildren(list);
    list = new ArrayList<>();
    list.add(t22);
    t2.setChildren(list);

  }

  /**
   * Test normal use of a list.
   * @throws Exception the exception
   */
  @Test
  public void testNormalUse026() throws Exception {
    testNormalUse(list1, list2, t1, t2);
  }

  /**
   * Test degenerate use of a list. Show that the underlying data structure
   * should NOT be manipulated.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testDegenerateUse026() throws Exception {
    testDegenerateUse(list1, list2, t1, t2);
  }

  /**
   * Test edge cases of a list.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testEdgeCases026() throws Exception {
    testEdgeCases(list1, list2, t1, t2);
    list1 = new TreeListJpa();

  }

  /**
   * Test XML serialization of a list.
   *
   * 
   * @throws Exception the exception
   */
  @Test
  public void testXmlSerialization026() throws Exception {
    testXmllSerialization(list1, list2, t1, t2);
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
