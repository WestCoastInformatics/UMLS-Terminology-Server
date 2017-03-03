/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.services.handlers.DefaultIdentifierAssignmentHandler;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.services.handlers.IdentifierAssignmentHandler;
import com.wci.umls.server.test.helpers.IntegrationUnitSupport;

/**
 * Integration testing for {@link DefaultIdentifierAssignmentHandler}.
 */
public class IdentifierAssignmentHandlerTest extends IntegrationUnitSupport {

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
      handlerService = new DefaultIdentifierAssignmentHandler();
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
  public void testHandlerNormalUse() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

  }

  /*
   * Test degenerate use of the handler object.
   * 
   * @throws Exception the exception
   */
  /**
   * Test handler degenerate use.
   *
   * @throws Exception the exception
   */
  @Test
  public void testHandlerDegenerateUse() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    // Call getTerminologyId(null)
    // TEST: exception
    try {
      handlerService.getTerminologyId((Concept) null);
      fail("Calling getTerminologyId((Concept)null) should have thrown an exception.");
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
  public void testHandlerEdgeCases() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());
    // Call getTerminologyId(new ConceptJpa())
    // TEST: returns null
    assertEquals(handlerService.getTerminologyId(new ConceptJpa()), null);
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
