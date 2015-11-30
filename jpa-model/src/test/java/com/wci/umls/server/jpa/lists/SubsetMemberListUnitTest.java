/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.lists;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.helpers.content.SubsetMemberList;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.AtomSubsetJpa;
import com.wci.umls.server.jpa.content.AtomSubsetMemberJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.ConceptSubsetJpa;
import com.wci.umls.server.jpa.content.ConceptSubsetMemberJpa;
import com.wci.umls.server.jpa.helpers.content.SubsetMemberListJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.AtomSubset;
import com.wci.umls.server.model.content.AtomSubsetMember;
import com.wci.umls.server.model.content.ComponentHasAttributesAndName;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptSubset;
import com.wci.umls.server.model.content.ConceptSubsetMember;
import com.wci.umls.server.model.content.Subset;
import com.wci.umls.server.model.content.SubsetMember;

/**
 * Unit testing for {@link SubsetMemberList}.
 */
public class SubsetMemberListUnitTest
    extends
    AbstractListUnit<SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset>> {

  /** The list test fixture . */
  private SubsetMemberList list;

  /** The list2 test fixture . */
  private SubsetMemberList list2;

  /** The test fixture. */
  @SuppressWarnings("rawtypes")
  private SubsetMember s1;

  /** The test fixture. */
  @SuppressWarnings("rawtypes")
  private SubsetMember s2;

  /** The test fixture. */
  @SuppressWarnings("rawtypes")
  private SubsetMember s3;

  /** The test fixture. */
  @SuppressWarnings("rawtypes")
  private SubsetMember s4;

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
    list = new SubsetMemberListJpa();
    list2 = new SubsetMemberListJpa();
    Atom atom = new AtomJpa();
    atom.setId(1L);
    atom.setTerminologyId("1");
    atom.setName("1");
    AtomSubset atomSubset = new AtomSubsetJpa();
    atomSubset.setId(1L);
    atomSubset.setTerminologyId("1");
    atomSubset.setName("1");

    s1 = new AtomSubsetMemberJpa();
    s1.setId(1L);
    s1.setTerminologyId("1");
    s1.setMember(atom);
    ((AtomSubsetMember) s1).setSubset(atomSubset);

    s2 = new AtomSubsetMemberJpa();
    s2.setId(2L);
    s2.setTerminologyId("2");
    s2.setMember(atom);
    ((AtomSubsetMember) s2).setSubset(atomSubset);

    Concept concept = new ConceptJpa();
    concept.setId(1L);
    concept.setTerminologyId("1");
    concept.setName("1");
    ConceptSubset conceptSubset = new ConceptSubsetJpa();
    conceptSubset.setId(1L);
    conceptSubset.setTerminologyId("1");
    conceptSubset.setName("1");

    s3 = new ConceptSubsetMemberJpa();
    s3.setId(1L);
    s3.setTerminologyId("1");
    s3.setMember(concept);
    ((ConceptSubsetMember) s3).setSubset(conceptSubset);
    s4 = new ConceptSubsetMemberJpa();
    s4.setId(2L);
    s4.setTerminologyId("2");
    s4.setMember(concept);
    ((ConceptSubsetMember) s4).setSubset(conceptSubset);

  }

  /**
   * Test normal use of a list.
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testNormalUse012() throws Exception {
    testNormalUse(list, list2, s1, s2);
    list = new SubsetMemberListJpa();
    list2 = new SubsetMemberListJpa();
    testNormalUse(list, list2, s3, s4);
  }

  /**
   * Test degenerate use of a list. Show that the underlying data structure
   * should NOT be manipulated.
   * 
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testDegenerateUse012() throws Exception {
    testDegenerateUse(list, list2, s1, s2);
    list = new SubsetMemberListJpa();
    list2 = new SubsetMemberListJpa();
    testDegenerateUse(list, list2, s3, s4);
  }

  /**
   * Test edge cases of a list.
   * 
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testEdgeCases012() throws Exception {
    testEdgeCases(list, list2, s1, s2);
    list = new SubsetMemberListJpa();
    list2 = new SubsetMemberListJpa();
    testEdgeCases(list, list2, s3, s4);

  }

  /**
   * Test XML serialization of a list.
   *
   * 
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  @Test
  public void testXmlSerialization012() throws Exception {
    testXmllSerialization(list, list2, s1, s2);
    list = new SubsetMemberListJpa();
    list2 = new SubsetMemberListJpa();
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
