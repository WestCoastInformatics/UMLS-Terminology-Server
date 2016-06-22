/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.jpa.services.handlers.DefaultComputePreferredNameHandler;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.services.ContentService;
import com.wci.umls.server.services.handlers.ComputePreferredNameHandler;
import com.wci.umls.server.test.helpers.IntegrationUnitSupport;

/**
 * Integration testing for {@link DefaultComputePreferredNameHandler}.
 */
public class ComputePreferredNameHandlerTest extends IntegrationUnitSupport {

  /** The handler service. */
  private ComputePreferredNameHandler handlerService;

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
      handlerService = new DefaultComputePreferredNameHandler();
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

    // Retrieve concept 728.10 (ICD9CM) from the content service.
    ContentService contentService = new ContentServiceJpa();
    Concept icdConcept =
        contentService.getConcept("421529006", "SNOMEDCT_US", "2014_09_01",
            Branch.ROOT);

    // test compute preferred name
    String pn =
        handlerService.computePreferredName(icdConcept.getAtoms(),
            contentService
                .getDefaultPrecedenceList("SNOMEDCT_US", "2014_09_01"));
    Logger.getLogger(getClass()).info(pn);
    assertEquals(pn,
        "ADC - Acquired immune deficiency syndrome dementia complex");

    // Test that the first one is the preferred one
    pn =
        handlerService
            .sortByPreference(
                icdConcept.getAtoms(),
                contentService.getDefaultPrecedenceList("SNOMEDCT_US",
                    "2014_09_01")).iterator().next().getName();
    Logger.getLogger(getClass()).info(pn);
    assertEquals(pn,
        "ADC - Acquired immune deficiency syndrome dementia complex");
  }

  /*
   * Test degenerate use of the handler object.
   * 
   * @throws Exception the exception
   */
  /**
   * Test handler degenerate use007.
   *
   * @throws Exception the exception
   */
  @Test
  public void testHandlerDegenerateUse() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    // Call computePreferredName(null)
    // TEST: exception
    try {
      handlerService.computePreferredName(null, null);
      fail("Calling computePreferredName(null) should have thrown an exception.");
    } catch (Exception e) {
      // do nothing
    }

    // Call isPreferredName(null)
    // TEST: exception
    try {
      handlerService.sortByPreference(null, null);
      fail("Calling sortByPreference(null) should have thrown an exception.");
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

    // Call computePreferredName(new ConceptJpa())
    // TEST: returns null
    assertEquals(handlerService.computePreferredName(new HashSet<Atom>(),
        (PrecedenceList) null), null);

    // Call computePreferredName(new HashSet<Description>())
    // TEST: returns null
    assertEquals(handlerService.sortByPreference(new HashSet<Atom>(),
        (PrecedenceList) null), new ArrayList<Atom>());

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
