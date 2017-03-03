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

import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.jpa.services.handlers.DefaultIdentifierAssignmentHandler;
import com.wci.umls.server.jpa.services.handlers.UmlsIdentifierAssignmentHandler;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.services.ContentService;
import com.wci.umls.server.test.helpers.IntegrationUnitSupport;

/**
 * Integration testing for {@link DefaultIdentifierAssignmentHandler}.
 */
public class UmlsIdentifierAssignmentHandlerTest
    extends IntegrationUnitSupport {

  /** The handler service. */
  private UmlsIdentifierAssignmentHandler handlerService;

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

    // Specifically testing the NCIMTH UI lengths/prefixes
    final ContentService contentService;
    try {
      contentService = new ContentServiceJpa();
      handlerService = (UmlsIdentifierAssignmentHandler) contentService
          .getIdentifierAssignmentHandler("NCIMTH");
      contentService.close();
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

    // Test ATUI assignment
    // identifier.assignment.handler.NCIMTH.atui.length=8
    // identifier.assignment.handler.NCIMTH.atui.prefix=AT
    final String zeroPadATUI = handlerService.convertId(123L, "ATUI");
    final String noPadATUI = handlerService.convertId(12345678L, "ATUI");
    final String overPadATUI = handlerService.convertId(123456789L, "ATUI");

    assertEquals("AT00000123", zeroPadATUI);
    assertEquals("AT12345678", noPadATUI);
    assertEquals("AT123456789", overPadATUI);

    // Test CUI assignment
    // identifier.assignment.handler.NCIMTH.cui.length=6
    // identifier.assignment.handler.NCIMTH.cui.prefix=CL
    final String zeroPadCUI = handlerService.convertId(123L, "CUI");
    final String noPadCUI = handlerService.convertId(123456L, "CUI");
    final String overPadCUI = handlerService.convertId(123456789L, "CUI");

    assertEquals("CL000123", zeroPadCUI);
    assertEquals("CL123456", noPadCUI);
    assertEquals("CL123456789", overPadCUI);
    
    // Test SUI assignment
    // identifier.assignment.handler.NCIMTH.sui.length=7
    // identifier.assignment.handler.NCIMTH.sui.prefix=S
    final String zeroPadSUI = handlerService.convertId(123L, "SUI");
    final String noPadSUI = handlerService.convertId(1234567L, "SUI");
    final String overPadSUI = handlerService.convertId(123456789L, "SUI");

    assertEquals("S0000123", zeroPadSUI);
    assertEquals("S1234567", noPadSUI);  
    assertEquals("S123456789", overPadSUI); 
    
    // Test AUI assignment
    // identifier.assignment.handler.NCIMTH.aui.length=7
    // identifier.assignment.handler.NCIMTH.aui.prefix=A
    final String zeroPadAUI = handlerService.convertId(123L, "AUI");
    final String noPadAUI = handlerService.convertId(1234567L, "AUI");
    final String overPadAUI = handlerService.convertId(123456789L, "AUI");

    assertEquals("A0000123", zeroPadAUI);
    assertEquals("A1234567", noPadAUI);
    assertEquals("A123456789", overPadAUI);     
    
    // Test RUI assignment
    // identifier.assignment.handler.NCIMTH.rui.length=8
    // identifier.assignment.handler.NCIMTH.rui.prefix=R
    final String zeroPadRUI = handlerService.convertId(123L, "RUI");
    final String noPadRUI = handlerService.convertId(12345678L, "RUI");
    final String overPadRUI = handlerService.convertId(123456789L, "RUI");

    assertEquals("R00000123", zeroPadRUI);
    assertEquals("R12345678", noPadRUI);   
    assertEquals("R123456789", overPadRUI);     
    
    // Test LUI assignment
    // identifier.assignment.handler.NCIMTH.lui.length=7
    // identifier.assignment.handler.NCIMTH.lui.prefix=L
    final String zeroPadLUI = handlerService.convertId(123L, "LUI");
    final String noPadLUI = handlerService.convertId(1234567L, "LUI");
    final String overPadLUI = handlerService.convertId(123456789L, "LUI");

    assertEquals("L0000123", zeroPadLUI);
    assertEquals("L1234567", noPadLUI);  
    assertEquals("L123456789", overPadLUI);    
   

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
      fail(
          "Calling getTerminologyId((Concept)null) should have thrown an exception.");
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
