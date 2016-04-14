/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.jpa;

import static org.junit.Assert.fail;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.services.handlers.UuidHashIdentifierAssignmentHandler;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.services.handlers.IdentifierAssignmentHandler;

/**
 * Integration testing for {@link UuidHashIdentifierAssignmentHandler}.
 */
public class Handler008Test {

  /** The handler service. */
  private IdentifierAssignmentHandler handlerService;

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
      handlerService = new UuidHashIdentifierAssignmentHandler();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Test normal use of the handler object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testHandlerNormalUse008() throws Exception {
    Logger.getLogger(getClass()).info("TEST testHandlerNormalUse008");

  }

  /*
   * Test degenerate use of the handler object.
   * 
   * @throws Exception the exception
   */
  /**
   * Test handler degenerate use001.
   *
   * @throws Exception the exception
   */
  @Test
  public void testHandlerDegenerateUse008() throws Exception {
    // Call getTerminologyId((Concept)null)
    // TEST: exception
    try {
      handlerService.getTerminologyId((Concept) null);
      fail("Calling resolve((Concept)null) should have thrown an exception.");
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
  public void testHandlerEdgeCases008() throws Exception {
    // Call computePreferredName(new ConceptJpa())
    // TEST: no exceptions
    handlerService.getTerminologyId(new ConceptJpa());

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
