/*
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.rest;

import static org.junit.Assert.fail;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import com.wci.umls.server.jpa.services.rest.SecurityServiceRest;

/**
 * Implementation of the "Security Service REST Degenerate Use" Test Cases.
 */
public class SecurityServiceRestEdgeCasesTest extends SecurityServiceRestTest {

  /**
   * Create test fixtures per test.
   *
   * @throws Exception the exception
   */
  @Before
  public void setup() throws Exception {
    // do nothing
  }

  /**
   * Test edge cases of the authenticate methods of {@link SecurityServiceRest}.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testAuthenticate() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    String authToken;
    // Procedure 1
    // Authenticate user
    // Authenticate user again with same password
    // TEST: no exception
    authToken =
        service.authenticate(viewerUserName, viewerUserPassword).getAuthToken();
    authToken =
        service.authenticate(viewerUserName, viewerUserPassword).getAuthToken();
    service.logout(authToken);
  }

  //
  // No known edge cases for user managment tools
  // so the edge case test 002 is left out.
  //

  /**
   * Test edge cases of logout for {@link SecurityServiceRest}.
   *
   * @throws Exception the exception
   */
  @Test
  public void testLogout() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Procedure 1

    // Logout of "guest" user without logging in.
    // TEST: no exception
    service.logout(viewerUserName);

    // Login as "guest" user with "guest" password
    // Logout of "guest"
    // Logout of "guest" again user without logging in.
    // TEST: no exception

    String authToken =
        service.authenticate(viewerUserName, viewerUserPassword).getAuthToken();
    service.logout(authToken);
    service.logout(authToken);

    // Logout as null/empty user
    // TEST: no exception

    service.authenticate(viewerUserName, viewerUserPassword);
    service.logout(null);

    // Logout as empty string
    // TEST: throws exception
    try {
      service.logout("");
      fail("Logging out with empty string for authentication token failed to throw exception");
    } catch (Exception e) {
      // do nothing
    }
  }

  /**
   * Teardown.
   *
   * @throws Exception the exception
   */
  @After
  public void teardown() throws Exception {
    // do nothing
  }

  /**
   * Teardown class.
   *
   * @throws Exception the exception
   */
  @AfterClass
  public static void teardownClass() throws Exception {
    // do nothing
  }

}