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

import com.wci.umls.server.helpers.content.Tree;
import com.wci.umls.server.helpers.content.TreeList;
import com.wci.umls.server.helpers.meta.TermTypeList;
import com.wci.umls.server.jpa.content.ConceptTreePositionJpa;
import com.wci.umls.server.jpa.helpers.content.TreeJpa;
import com.wci.umls.server.jpa.helpers.content.TreeListJpa;
import com.wci.umls.server.model.content.ConceptTreePosition;

/**
 * Unit testing for {@link TermTypeList}.
 */
public class ListUnit028Test extends
    AbstractListUnit<Tree> {

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

    t1 = new TreeJpa();
    ConceptTreePosition tp = new ConceptTreePositionJpa();
    tp.setId(1L);
    tp.setTerminologyId("1");
    List<Tree> list = new ArrayList<>();
    Tree chdTree = new TreeJpa();
    ConceptTreePosition chdTreePos = new ConceptTreePositionJpa();
    chdTreePos.setId(11L);
    list.add(chdTree);
    t1.setChildren(list);
    
    t2 = new TreeJpa();
    ConceptTreePosition tp2 = new ConceptTreePositionJpa();
    tp2.setId(2L);
    tp2.setTerminologyId("2");
    List<Tree> list2 = new ArrayList<>();
    Tree chdTree2 = new TreeJpa();
    ConceptTreePosition chdTreePos2 = new ConceptTreePositionJpa();
    chdTreePos2.setId(22L);
    list2.add(chdTree2);
    t2.setChildren(list2);

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
