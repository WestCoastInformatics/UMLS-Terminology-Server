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
import com.wci.umls.server.helpers.content.TreePositionList;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.AtomTreePositionJpa;
import com.wci.umls.server.jpa.content.CodeJpa;
import com.wci.umls.server.jpa.content.CodeTreePositionJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.ConceptTreePositionJpa;
import com.wci.umls.server.jpa.content.DescriptorJpa;
import com.wci.umls.server.jpa.content.DescriptorTreePositionJpa;
import com.wci.umls.server.jpa.helpers.content.TreePositionListJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.AtomTreePosition;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.CodeTreePosition;
import com.wci.umls.server.model.content.ComponentHasAttributesAndName;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptTreePosition;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.DescriptorTreePosition;
import com.wci.umls.server.model.content.TreePosition;

/**
 * Unit testing for {@link TreePositionList}.
 */
public class TreePositionListUnitTest extends
    AbstractListUnit<TreePosition<? extends ComponentHasAttributesAndName>> {

  /** test fixture . */
  private TreePositionList list1;

  /** test fixture . */
  private TreePositionList list2;

  /** The ctp1. */
  private ConceptTreePosition ctp1;

  /** The ctp2. */
  private ConceptTreePosition ctp2;

  /** The dtp1. */
  private DescriptorTreePosition dtp1;

  /** The dtp2. */
  private DescriptorTreePosition dtp2;

  /** The codetp1. */
  private CodeTreePosition codetp1;

  /** The codetp2. */
  private CodeTreePosition codetp2;
  
  /** The atomtp1. */
  private AtomTreePosition atomtp1;
  
  /** The atomtp2. */
  private AtomTreePosition atomtp2;

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
    list1 = new TreePositionListJpa();
    list2 = new TreePositionListJpa();

    ProxyTester tester = new ProxyTester(new ConceptTreePositionJpa());
    ctp1 = (ConceptTreePosition) tester.createObject(1);
    ctp2 = (ConceptTreePosition) tester.createObject(2);
    Concept concept = new ConceptJpa();
    concept.setId(1L);
    ctp1.setNode(concept);
    ctp2.setNode(concept);

    ProxyTester tester2 = new ProxyTester(new DescriptorTreePositionJpa());
    dtp1 = (DescriptorTreePosition) tester2.createObject(1);
    dtp2 = (DescriptorTreePosition) tester2.createObject(2);
    Descriptor descriptor = new DescriptorJpa();
    descriptor.setId(1L);
    dtp1.setNode(descriptor);
    dtp2.setNode(descriptor);

    ProxyTester tester3 = new ProxyTester(new CodeTreePositionJpa());
    codetp1 = (CodeTreePosition) tester3.createObject(1);
    codetp2 = (CodeTreePosition) tester3.createObject(2);
    Code code = new CodeJpa();
    code.setId(1L);
    codetp1.setNode(code);
    codetp2.setNode(code);

    ProxyTester tester4 = new ProxyTester(new AtomTreePositionJpa());
    atomtp1 = (AtomTreePosition) tester4.createObject(1);
    atomtp2 = (AtomTreePosition) tester4.createObject(2);
    Atom atom = new AtomJpa();
    atom.setId(1L);
    atomtp1.setNode(atom);
    atomtp2.setNode(atom);
  }

  /**
   * Test normal use of a list.
   * @throws Exception the exception
   */
  @Test
  public void testNormalUse() throws Exception {
    testNormalUse(list1, list2, ctp1, ctp2);
    list1 = new TreePositionListJpa();
    list2 = new TreePositionListJpa();
    testNormalUse(list1, list2, dtp1, dtp2);
    list1 = new TreePositionListJpa();
    list2 = new TreePositionListJpa();
    testNormalUse(list1, list2, codetp1, codetp2);
    list1 = new TreePositionListJpa();
    list2 = new TreePositionListJpa();
    testNormalUse(list1, list2, atomtp1, atomtp2);
  }

  /**
   * Test degenerate use of a list. Show that the underlying data structure
   * should NOT be manipulated.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testDegenerateUse() throws Exception {
    testDegenerateUse(list1, list2, ctp1, ctp2);
    list1 = new TreePositionListJpa();
    list2 = new TreePositionListJpa();
    testDegenerateUse(list1, list2, dtp1, dtp2);
    list1 = new TreePositionListJpa();
    list2 = new TreePositionListJpa();
    testDegenerateUse(list1, list2, codetp1, codetp2);
    list1 = new TreePositionListJpa();
    list2 = new TreePositionListJpa();
    testDegenerateUse(list1, list2, atomtp1, atomtp2);
  }

  /**
   * Test edge cases of a list.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testEdgeCases() throws Exception {
    testEdgeCases(list1, list2, ctp1, ctp2);
    list1 = new TreePositionListJpa();
    list2 = new TreePositionListJpa();
    testEdgeCases(list1, list2, dtp1, dtp2);
    list1 = new TreePositionListJpa();
    list2 = new TreePositionListJpa();
    testEdgeCases(list1, list2, codetp1, codetp2);
    list1 = new TreePositionListJpa();
    list2 = new TreePositionListJpa();
    testEdgeCases(list1, list2, atomtp1, atomtp2);
  }

  /**
   * Test XML serialization of a list.
   *
   * 
   * @throws Exception the exception
   */
  @Test
  public void testXmlSerialization() throws Exception {
    testXmllSerialization(list1, list2, ctp1, ctp2);
    list1 = new TreePositionListJpa();
    list2 = new TreePositionListJpa();
    testXmllSerialization(list1, list2, dtp1, dtp2);
    list1 = new TreePositionListJpa();
    list2 = new TreePositionListJpa();
    testXmllSerialization(list1, list2, codetp1, codetp2);
    list1 = new TreePositionListJpa();
    list2 = new TreePositionListJpa();
    testXmllSerialization(list1, list2, atomtp1, atomtp2);
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
