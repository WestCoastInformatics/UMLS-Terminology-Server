/*
 * Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package com.wci.umls.server.test.rest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.wci.umls.server.model.content.Concept;

/**
 * Implementation of the "Content Service REST Edge Cases" Test Cases.
 */
public class ContentServiceRestEdgeCasesTest extends ContentServiceRestTest {

  /** The auth token. */
  private static String authToken;

  /** The test terminology. */
  @SuppressWarnings("unused")
  private String testTerminology;

  /** The test version. */
  @SuppressWarnings("unused")
  private String testVersion;

  /** The concept used in testing. */
  @SuppressWarnings("unused")
  private Concept concept;

  /**
   * Create test fixtures per test.
   *
   * @throws Exception the exception
   */
  @Override
  @Before
  public void setup() throws Exception {

    // authentication
    authToken = securityService.authenticate(testUser, testPassword);

    // set terminology and version
    testTerminology = "SNOMEDCT";
    testVersion = "latest";
   
  }

  /**
   * Test edge cases for "get concept"
   *
   * @throws Exception the exception
   */
  @Test
  public void testEdgeCasesRestContent001() throws Exception {
    
    // TODO: Implement these methods exactly in order according to the spreadsheet
  }

  /**
   * Teardown.
   *
   * @throws Exception the exception
   */
  @Override
  @After
  public void teardown() throws Exception {

    // logout
    securityService.logout(authToken);
  }

  /**
   * Returns the parameter types.
   *
   * @param parameters the parameters
   * @return the parameter types
   */
  @SuppressWarnings("static-method")
  public Class<?>[] getParameterTypes(Object[] parameters) {
    Class<?>[] types = new Class<?>[parameters.length];
    for (int i = 0; i < parameters.length; i++) {
      types[i] = parameters[i].getClass();
    }
    return types;
  }

}
