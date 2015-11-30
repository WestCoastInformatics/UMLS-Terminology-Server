/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.lists;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.helpers.content.RelationshipList;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.AtomRelationshipJpa;
import com.wci.umls.server.jpa.content.CodeJpa;
import com.wci.umls.server.jpa.content.CodeRelationshipJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.ConceptRelationshipJpa;
import com.wci.umls.server.jpa.content.DescriptorJpa;
import com.wci.umls.server.jpa.content.DescriptorRelationshipJpa;
import com.wci.umls.server.jpa.helpers.content.RelationshipListJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.ComponentHasAttributes;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.Relationship;

/**
 * Unit testing for {@link RelationshipList}.
 */
@SuppressWarnings("rawtypes")
public class RelationshipListUnitTest
    extends
    AbstractListUnit<Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes>> {

  /** The list test fixture . */
  private RelationshipList list;

  /** The list2 test fixture . */
  private RelationshipList list2;

  /** The test fixture. */
  private Relationship r1;

  /** The test fixture. */
  private Relationship r2;

  /** The test fixture. */
  private Relationship r3;

  /** The test fixture. */
  private Relationship r4;

  /** The test fixture. */
  private Relationship r5;

  /** The test fixture. */
  private Relationship r6;

  /** The test fixture. */
  private Relationship r7;

  /** The test fixture. */
  private Relationship r8;

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
  @SuppressWarnings("unchecked")
  @Before
  public void setup() {
    list = new RelationshipListJpa();
    list2 = new RelationshipListJpa();
    Concept c = new ConceptJpa();
    c.setId(1L);
    c.setTerminologyId("1");
    c.setName("1");
    r1 = new ConceptRelationshipJpa();
    r1.setId(1L);
    r1.setTerminologyId("1");
    r1.setFrom(c);
    r1.setTo(c);
    r2 = new ConceptRelationshipJpa();
    r2.setId(2L);
    r2.setTerminologyId("2");
    r2.setFrom(c);
    r2.setTo(c);

    Code code = new CodeJpa();
    code.setId(1L);
    code.setTerminologyId("1");
    code.setName("1");
    r3 = new CodeRelationshipJpa();
    r3.setId(1L);
    r3.setTerminologyId("1");
    r3.setFrom(code);
    r3.setTo(code);
    r4 = new CodeRelationshipJpa();
    r4.setId(2L);
    r4.setTerminologyId("2");
    r4.setFrom(code);
    r4.setTo(code);

    Descriptor d = new DescriptorJpa();
    d.setId(1L);
    d.setTerminologyId("1");
    d.setName("1");
    r5 = new DescriptorRelationshipJpa();
    r5.setId(1L);
    r5.setTerminologyId("1");
    r5.setFrom(d);
    r5.setTo(d);
    r6 = new DescriptorRelationshipJpa();
    r6.setId(2L);
    r6.setTerminologyId("2");
    r6.setFrom(d);
    r6.setTo(d);

    Atom a = new AtomJpa();
    a.setId(1L);
    a.setTerminologyId("1");
    a.setName("1");
    r7 = new AtomRelationshipJpa();
    r7.setId(1L);
    r7.setTerminologyId("1");
    r7.setFrom(a);
    r7.setTo(a);
    r8 = new AtomRelationshipJpa();
    r8.setId(2L);
    r8.setTerminologyId("2");
    r8.setFrom(a);
    r8.setTo(a);

  }

  /**
   * Test normal use of a list.
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testNormalUse008() throws Exception {
    testNormalUse(list, list2, r1, r2);
    list = new RelationshipListJpa();
    list2 = new RelationshipListJpa();
    testNormalUse(list, list2, r3, r4);
    list = new RelationshipListJpa();
    list2 = new RelationshipListJpa();
    testNormalUse(list, list2, r5, r6);
    list = new RelationshipListJpa();
    list2 = new RelationshipListJpa();
    testNormalUse(list, list2, r7, r8);
  }

  /**
   * Test degenerate use of a list. Show that the underlying data structure
   * should NOT be manipulated.
   * 
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testDegenerateUse008() throws Exception {
    testDegenerateUse(list, list2, r1, r2);
    list = new RelationshipListJpa();
    list2 = new RelationshipListJpa();
    testDegenerateUse(list, list2, r3, r4);
    list = new RelationshipListJpa();
    list2 = new RelationshipListJpa();
    testDegenerateUse(list, list2, r5, r6);
    list = new RelationshipListJpa();
    list2 = new RelationshipListJpa();
    testDegenerateUse(list, list2, r5, r6);
    list = new RelationshipListJpa();
    list2 = new RelationshipListJpa();
    testDegenerateUse(list, list2, r7, r8);
  }

  /**
   * Test edge cases of a list.
   * 
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testEdgeCases008() throws Exception {
    testEdgeCases(list, list2, r1, r2);
    list = new RelationshipListJpa();
    list2 = new RelationshipListJpa();
    testEdgeCases(list, list2, r3, r4);
    list = new RelationshipListJpa();
    list2 = new RelationshipListJpa();
    testEdgeCases(list, list2, r5, r6);
    list = new RelationshipListJpa();
    list2 = new RelationshipListJpa();
    testEdgeCases(list, list2, r7, r8);
  }

  /**
   * Test XML serialization of a list.
   *
   * 
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testXmlSerialization008() throws Exception {
    testXmllSerialization(list, list2, r1, r2);
    list = new RelationshipListJpa();
    list2 = new RelationshipListJpa();
    testXmllSerialization(list, list2, r3, r4);
    list = new RelationshipListJpa();
    list2 = new RelationshipListJpa();
    testXmllSerialization(list, list2, r5, r6);
    list = new RelationshipListJpa();
    list2 = new RelationshipListJpa();
    testXmllSerialization(list, list2, r7, r8);
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
