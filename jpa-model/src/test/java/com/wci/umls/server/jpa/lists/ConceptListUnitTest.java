/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.lists;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.helpers.content.ConceptList;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.helpers.content.ConceptListJpa;
import com.wci.umls.server.model.content.Concept;

/**
 * Unit testing for {@link ConceptList}.
 */
public class ConceptListUnitTest extends AbstractListUnit<Concept> {

  /** The list test fixture . */
  private ConceptList list;

  /** The list2 test fixture . */
  private ConceptList list2;

  /** The test fixture s1. */
  private Concept c1;

  /** The test fixture s2. */
  private Concept c2;

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
    list = new ConceptListJpa();
    list2 = new ConceptListJpa();
    c1 = new ConceptJpa();
    c1.setId(1L);
    c1.setTerminologyId("1");
    c2 = new ConceptJpa();
    c2.setId(2L);
    c2.setTerminologyId("2");

  }

  /**
   * Test normal use of a list.
   * @throws Exception the exception
   */
  @Test
  public void testNormalUse003() throws Exception {
    testNormalUse(list, list2, c1, c2);
  }

  /**
   * Test degenerate use of a list. Show that the underlying data structure
   * should NOT be manipulated.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testDegenerateUse003() throws Exception {
    testDegenerateUse(list, list2, c1, c2);
  }

  /**
   * Test edge cases of a list.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testEdgeCases003() throws Exception {
    testEdgeCases(list, list2, c1, c2);
  }

  /**
   * Test XML serialization of a list.
   *
   * 
   * @throws Exception the exception
   */
  @Test
  public void testXmlSerialization003() throws Exception {
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
