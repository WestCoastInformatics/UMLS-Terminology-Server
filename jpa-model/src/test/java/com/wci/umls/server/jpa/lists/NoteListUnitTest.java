/*
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.lists;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.helpers.Note;
import com.wci.umls.server.helpers.NoteList;
import com.wci.umls.server.jpa.content.CodeJpa;
import com.wci.umls.server.jpa.content.CodeNoteJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.ConceptNoteJpa;
import com.wci.umls.server.jpa.content.DescriptorJpa;
import com.wci.umls.server.jpa.content.DescriptorNoteJpa;
import com.wci.umls.server.jpa.helpers.content.NoteListJpa;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Descriptor;

/**
 * Unit testing for {@link NoteList}.
 */
public class NoteListUnitTest
    extends
    AbstractListUnit<Note> {

  /** The list test fixture . */
  private NoteList list;

  /** The list2 test fixture . */
  private NoteList list2;

  /** The test fixture. */
  private Note n1;

  /** The test fixture. */
  private Note n2;

  /** The test fixture. */
  private Note n3;

  /** The concept. */
  private Concept concept;
  
  /** The descriptor. */
  private Descriptor descriptor;
  
  /** The code. */
  private Code code;

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
    list = new NoteListJpa();
    list2 = new NoteListJpa();
    
    // set up component objects
    concept = new ConceptJpa();
    concept.setId(1L);
    concept.setTerminologyId("1");
    concept.setName("1");
    
    descriptor = new DescriptorJpa();
    descriptor.setId(2L);
    descriptor.setTerminologyId("2");
    descriptor.setName("2");
    
    code = new CodeJpa();
    code.setId(3L);
    code.setTerminologyId("3");
    code.setName("3");
    
    // set up a concept note
    n1 = new ConceptNoteJpa();
    n1.setId(1L);
    ((ConceptNoteJpa) n1).setConcept(concept);
    
    // set up a descriptor note
    n2 = new DescriptorNoteJpa();
    n2.setId(2L);
    ((DescriptorNoteJpa) n2).setDescriptor(descriptor);
    n2.setNote("descriptor note");


    // set up a code note
    n3 = new CodeNoteJpa();
    n3.setId(1L);
    ((CodeNoteJpa) n3).setCode(code);
    n3.setNote("code note");

  }

  /**
   * Test normal use of a list.
   * @throws Exception the exception
   */
  @Test
  public void testNormalUse() throws Exception {
    testNormalUse(list, list2, n1, n2);
    list = new NoteListJpa();
    list2 = new NoteListJpa();
    testNormalUse(list, list2, n1, n3);
    list = new NoteListJpa();
    list2 = new NoteListJpa();
    testNormalUse(list, list2, n2, n3);

  }

  /**
   * Test degenerate use of a list. Show that the underlying data structure
   * should NOT be manipulated.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testDegenerateUse() throws Exception {
    testDegenerateUse(list, list2, n1, n2);
    list = new NoteListJpa();
    list2 = new NoteListJpa();
    testDegenerateUse(list, list2, n1, n3);
    list = new NoteListJpa();
    list2 = new NoteListJpa();
    testDegenerateUse(list, list2, n2, n3);
  }

  /**
   * Test edge cases of a list.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testEdgeCases() throws Exception {
    testEdgeCases(list, list2, n1, n2);
    list = new NoteListJpa();
    list2 = new NoteListJpa();
    testEdgeCases(list, list2, n1, n3);
    list = new NoteListJpa();
    list2 = new NoteListJpa();
    testEdgeCases(list, list2, n2, n3);

  }

  /**
   * Test XML serialization of a list.
   *
   * 
   * @throws Exception the exception
   */
  @Test
  public void testXmlSerialization() throws Exception {
    testXmllSerialization(list, list2, n1, n2);
    list = new NoteListJpa();
    list2 = new NoteListJpa();
    testXmllSerialization(list, list2, n1, n3);
    list = new NoteListJpa();
    list2 = new NoteListJpa();
    testXmllSerialization(list, list2, n2, n3);

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
