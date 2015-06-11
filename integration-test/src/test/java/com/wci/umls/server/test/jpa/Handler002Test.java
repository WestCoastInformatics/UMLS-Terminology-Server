/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.jpa;

import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.jpa.content.CodeJpa;
import com.wci.umls.server.jpa.content.CodeRelationshipJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.ConceptRelationshipJpa;
import com.wci.umls.server.jpa.content.DescriptorJpa;
import com.wci.umls.server.jpa.content.DescriptorRelationshipJpa;
import com.wci.umls.server.jpa.content.SemanticTypeComponentJpa;
import com.wci.umls.server.jpa.services.handlers.DefaultGraphResolutionHandler;
import com.wci.umls.server.jpa.services.helper.TerminologyUtility;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.ComponentHasAttributes;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.LexicalClass;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.model.content.SemanticTypeComponent;
import com.wci.umls.server.model.content.StringClass;
import com.wci.umls.server.services.handlers.GraphResolutionHandler;

/**
 * Integration testing for {@link DefaultGraphResolutionHandler}.
 */
public class Handler002Test {

  /** The handler service. */
  private GraphResolutionHandler handlerService;

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
    try {
      handlerService = new DefaultGraphResolutionHandler();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * /** Test normal use of the handler object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testHandlerNormalUse002() throws Exception {
    Logger.getLogger(getClass()).info("TEST testHandlerNormalUse002");

    // Test a UMLS concept
    // Test a UMLS atom
     
    // Test a SNOMED concept
    // Test a SNOMED atom
    // Test a SNOMED subset

    // Test a MSH descriptor
    // Test a MSH atom
  }

  /*
   * Test degenerate use of the handler object.
   * 
   * @throws Exception the exception
   */
  /**
   * Test handler degenerate use002.
   *
   * @throws Exception the exception
   */
  @Test
  public void testHandlerDegenerateUse002() throws Exception {

    // TEST: exception
    try {
      Set<String> chdRel = new HashSet<>();
      chdRel.add("CHD");
      handlerService.resolve((Concept) null, chdRel);
      fail("Calling resolve((Concept)null) should have thrown an exception.");
    } catch (Exception e) {
      // do nothing
    }

    try {
      Set<String> chdRel = new HashSet<>();
      chdRel.add("CHD");
      handlerService.resolve((Descriptor) null, chdRel);
      fail("Calling resolve((Descriptor)null) should have thrown an exception.");
    } catch (Exception e) {
      // do nothing
    }

    try {
      Set<String> chdRel = new HashSet<>();
      chdRel.add("CHD");
      handlerService.resolve((Code) null, chdRel);
      fail("Calling resolve((Code)null) should have thrown an exception.");
    } catch (Exception e) {
      // do nothing
    }

    try {
      handlerService.resolve((LexicalClass) null);
      fail("Calling resolve((LexicalClass)null) should have thrown an exception.");
    } catch (Exception e) {
      // do nothing
    }

    try {
      handlerService.resolve((StringClass) null);
      fail("Calling resolve((StringClass)null) should have thrown an exception.");
    } catch (Exception e) {
      // do nothing
    }

    // Call resolve((Atom)null)
    // TEST: exception
    try {
      handlerService.resolve((Atom) null);
      fail("Calling resolve((Atom)null) should have thrown an exception.");
    } catch (Exception e) {
      // do nothing
    }

    // Call resolve((Relationship)null)
    // TEST: exception
    try {
      handlerService
          .resolve((Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes>) null);
      fail("Calling resolve((Relationship)null) should have thrown an exception.");
    } catch (Exception e) {
      // do nothing
    }

    // Call resolve((SemanticTypeComponent)null)
    // TEST: exception
    try {
      handlerService.resolve((SemanticTypeComponent) null);
      fail("Calling resolve((SemanticTypeComponent)null) should have thrown an exception.");
    } catch (Exception e) {
      // do nothing
    }

    // Call resolveEmpty((Concept)null);
    // TEST: exception
    try {
      handlerService.resolveEmpty((Concept) null);
      fail("Calling resolveEmpty((Concept)null) should have thrown an exception.");
    } catch (Exception e) {
      // do nothing
    }

    // Call resolveEmpty((Descriptor)null);
    // TEST: exception
    try {
      handlerService.resolveEmpty((Descriptor) null);
      fail("Calling resolveEmpty((Descriptor)null) should have thrown an exception.");
    } catch (Exception e) {
      // do nothing
    }

    // Call resolveEmpty((Code)null);
    // TEST: exception
    try {
      handlerService.resolveEmpty((Code) null);
      fail("Calling resolveEmpty((Code)null) should have thrown an exception.");
    } catch (Exception e) {
      // do nothing
    }
  }

  /**
   * Test edge cases of the handler object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testHandlerEdgeCases002() throws Exception {
    // Call computePreferredName(new ConceptJpa())
    // TEST: no exceptions
    handlerService.resolve(new ConceptJpa(),
        TerminologyUtility.getHierarchicalIsaRels("UMLS", "latest"));
    handlerService.resolve(new DescriptorJpa(),
        TerminologyUtility.getHierarchicalIsaRels("UMLS", "latest"));
    handlerService.resolve(new CodeJpa(),
        TerminologyUtility.getHierarchicalIsaRels("UMLS", "latest"));



    handlerService.resolve(new SemanticTypeComponentJpa());
    handlerService.resolve(new ConceptRelationshipJpa());
    handlerService.resolve(new DescriptorRelationshipJpa());
    handlerService.resolve(new CodeRelationshipJpa());

    handlerService.resolveEmpty(new ConceptJpa());
    handlerService.resolveEmpty(new DescriptorJpa());
    handlerService.resolveEmpty(new CodeJpa());
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
