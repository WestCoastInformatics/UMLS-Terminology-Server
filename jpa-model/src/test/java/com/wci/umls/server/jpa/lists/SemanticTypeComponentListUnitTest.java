/*
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.lists;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.helpers.content.SemanticTypeComponentList;
import com.wci.umls.server.jpa.content.SemanticTypeComponentJpa;
import com.wci.umls.server.jpa.helpers.content.SemanticTypeComponentListJpa;
import com.wci.umls.server.model.content.SemanticTypeComponent;

/**
 * Unit testing for {@link SemanticTypeComponentList}.
 */
public class SemanticTypeComponentListUnitTest extends
    AbstractListUnit<SemanticTypeComponent> {

  /** The list test fixture . */
  private SemanticTypeComponentList list;

  /** The list2 test fixture . */
  private SemanticTypeComponentList list2;

  /** The test fixture s1. */
  private SemanticTypeComponent s1;

  /** The test fixture s2. */
  private SemanticTypeComponent d2;

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
    list = new SemanticTypeComponentListJpa();
    list2 = new SemanticTypeComponentListJpa();
    s1 = new SemanticTypeComponentJpa();
    s1.setId(1L);
    s1.setTerminologyId("1");
    d2 = new SemanticTypeComponentJpa();
    d2.setId(2L);
    d2.setTerminologyId("2");

  }

  /**
   * Test normal use of a list.
   * @throws Exception the exception
   */
  @Test
  public void testNormalUse() throws Exception {
    testNormalUse(list, list2, s1, d2);
  }

  /**
   * Test degenerate use of a list. Show that the underlying data structure
   * should NOT be manipulated.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testDegenerateUse() throws Exception {
    testDegenerateUse(list, list2, s1, d2);
  }

  /**
   * Test edge cases of a list.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testEdgeCases() throws Exception {
    testEdgeCases(list, list2, s1, d2);
  }

  /**
   * Test XML serialization of a list.
   *
   * 
   * @throws Exception the exception
   */
  @Test
  public void testXmlSerialization() throws Exception {
    testXmllSerialization(list, list2, s1, d2);
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
