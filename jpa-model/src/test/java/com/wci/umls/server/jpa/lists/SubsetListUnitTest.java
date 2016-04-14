/*
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.lists;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.helpers.content.SubsetList;
import com.wci.umls.server.jpa.content.AtomSubsetJpa;
import com.wci.umls.server.jpa.content.ConceptSubsetJpa;
import com.wci.umls.server.jpa.helpers.content.SubsetListJpa;
import com.wci.umls.server.model.content.Subset;

/**
 * Unit testing for {@link SubsetList}.
 */
public class SubsetListUnitTest extends AbstractListUnit<Subset> {

  /** The list test fixture . */
  private SubsetList list;

  /** The list2 test fixture . */
  private SubsetList list2;

  /** The test fixture. */
  private Subset s1;

  /** The test fixture. */
  private Subset s2;

  /** The test fixture. */
  private Subset s3;

  /** The test fixture. */
  private Subset s4;

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
    list = new SubsetListJpa();
    list2 = new SubsetListJpa();
    s1 = new AtomSubsetJpa();
    s1.setId(1L);
    s1.setTerminologyId("1");
    s2 = new AtomSubsetJpa();
    s2.setId(2L);
    s2.setTerminologyId("2");
    s3 = new ConceptSubsetJpa();
    s3.setId(1L);
    s3.setTerminologyId("1");
    s4 = new ConceptSubsetJpa();
    s4.setId(2L);
    s4.setTerminologyId("2");

  }

  /**
   * Test normal use of a list.
   * @throws Exception the exception
   */
  @Test
  public void testNormalUse011() throws Exception {
    testNormalUse(list, list2, s1, s2);
    list = new SubsetListJpa();
    list2 = new SubsetListJpa();
    testNormalUse(list, list2, s3, s4);
  }

  /**
   * Test degenerate use of a list. Show that the underlying data structure
   * should NOT be manipulated.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testDegenerateUse011() throws Exception {
    testDegenerateUse(list, list2, s1, s2);
    list = new SubsetListJpa();
    list2 = new SubsetListJpa();
    testDegenerateUse(list, list2, s3, s4);
  }

  /**
   * Test edge cases of a list.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testEdgeCases011() throws Exception {
    testEdgeCases(list, list2, s1, s2);
    list = new SubsetListJpa();
    list2 = new SubsetListJpa();
    testEdgeCases(list, list2, s3, s4);

  }

  /**
   * Test XML serialization of a list.
   *
   * 
   * @throws Exception the exception
   */
  @Test
  public void testXmlSerialization011() throws Exception {
    testXmllSerialization(list, list2, s1, s2);
    list = new SubsetListJpa();
    list2 = new SubsetListJpa();
    testXmllSerialization(list, list2, s3, s4);

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
