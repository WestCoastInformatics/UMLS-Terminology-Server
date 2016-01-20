/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.lists;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.helpers.content.DefinitionList;
import com.wci.umls.server.jpa.content.DefinitionJpa;
import com.wci.umls.server.jpa.helpers.content.DefinitionListJpa;
import com.wci.umls.server.model.content.Definition;

/**
 * Unit testing for {@link DefinitionList}.
 */
public class DefinitionListUnitTest extends AbstractListUnit<Definition> {

  /** The list test fixture . */
  private DefinitionList list;

  /** The list2 test fixture . */
  private DefinitionList list2;

  /** The test fixture s1. */
  private Definition d1;

  /** The test fixture s2. */
  private Definition d2;

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
    list = new DefinitionListJpa();
    list2 = new DefinitionListJpa();
    d1 = new DefinitionJpa();
    d1.setId(1L);
    d1.setTerminologyId("1");
    d2 = new DefinitionJpa();
    d2.setId(2L);
    d2.setTerminologyId("2");

  }

  /**
   * Test normal use of a list.
   * @throws Exception the exception
   */
  @Test
  public void testNormalUse016() throws Exception {
    testNormalUse(list, list2, d1, d2);
  }

  /**
   * Test degenerate use of a list. Show that the underlying data structure
   * should NOT be manipulated.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testDegenerateUse016() throws Exception {
    testDegenerateUse(list, list2, d1, d2);
  }

  /**
   * Test edge cases of a list.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testEdgeCases016() throws Exception {
    testEdgeCases(list, list2, d1, d2);
  }

  /**
   * Test XML serialization of a list.
   *
   * 
   * @throws Exception the exception
   */
  @Test
  public void testXmlSerialization016() throws Exception {
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
