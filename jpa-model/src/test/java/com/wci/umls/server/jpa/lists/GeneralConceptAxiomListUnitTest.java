/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.lists;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.helpers.ProxyTester;
import com.wci.umls.server.helpers.content.GeneralConceptAxiomList;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.GeneralConceptAxiomJpa;
import com.wci.umls.server.jpa.helpers.content.GeneralConceptAxiomListJpa;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.GeneralConceptAxiom;

/**
 * Unit testing for {@link GeneralConceptAxiomList}.
 */
public class GeneralConceptAxiomListUnitTest extends
    AbstractListUnit<GeneralConceptAxiom> {

  /** test fixture . */
  private GeneralConceptAxiomList list1;

  /** test fixture . */
  private GeneralConceptAxiomList list2;

  /** test fixture. */
  private GeneralConceptAxiom ms1;

  /** test fixture. */
  private GeneralConceptAxiom ms2;

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
    list1 = new GeneralConceptAxiomListJpa();
    list2 = new GeneralConceptAxiomListJpa();

    ProxyTester tester = new ProxyTester(new GeneralConceptAxiomJpa());
    ms1 = (GeneralConceptAxiom) tester.createObject(1);
    ms2 = (GeneralConceptAxiom) tester.createObject(2);

    ProxyTester tester2 = new ProxyTester(new ConceptJpa());
    ms1.setLeftHandSide((Concept) tester2.createObject(1));
    ms1.setRightHandSide((Concept) tester2.createObject(2));
    ms2.setLeftHandSide((Concept) tester2.createObject(3));
    ms2.setRightHandSide((Concept) tester2.createObject(4));

  }

  /**
   * Test normal use of a list.
   * @throws Exception the exception
   */
  @Test
  public void testNormalUse026() throws Exception {
    testNormalUse(list1, list2, ms1, ms2);
  }

  /**
   * Test degenerate use of a list. Show that the underlying data structure
   * should NOT be manipulated.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testDegenerateUse026() throws Exception {
    testDegenerateUse(list1, list2, ms1, ms2);
  }

  /**
   * Test edge cases of a list.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testEdgeCases026() throws Exception {
    testEdgeCases(list1, list2, ms1, ms2);
    list1 = new GeneralConceptAxiomListJpa();

  }

  /**
   * Test XML serialization of a list.
   *
   * 
   * @throws Exception the exception
   */
  @Test
  public void testXmlSerialization026() throws Exception {
    testXmllSerialization(list1, list2, ms1, ms2);
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
