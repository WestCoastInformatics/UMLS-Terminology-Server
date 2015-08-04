/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.rest;

import static org.junit.Assert.fail;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import com.wci.umls.server.UserRole;
import com.wci.umls.server.jpa.UserJpa;
import com.wci.umls.server.jpa.services.rest.SecurityServiceRest;

/**
 * Implementation of the "Security Service REST Degenerate Use" Test Cases.
 */
public class SecurityServiceRestRoleCheckTest extends SecurityServiceRestTest {

  /** The admin user auth token. */
  private static String viewerUserAuthToken;

  /** The admin user auth token. */
  private static String adminUserAuthToken;

  /**
   * Create test fixtures per test.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  @Before
  public void setup() throws Exception {

    // ensure useres are logged in prior to tests
    adminUserAuthToken =
        service.authenticate(adminUserName, adminUserPassword).getAuthToken();
    viewerUserAuthToken =
        service.authenticate(viewerUserName, viewerUserPassword).getAuthToken();
  }

  //
  // No degenerate test case for testRoleCheckRestSecurity001
  //

  /**
   * Test role check of user management REST calls in SecurityService
   * {@link SecurityServiceRest}.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testRoleCheckRestSecurity002() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

    // create test user
    UserJpa testUser = new UserJpa();
    testUser.setUserName(properties.getProperty("bad.user"));
    testUser.setEmail("no email");
    testUser.setName("Test User");
    testUser.setApplicationRole(UserRole.VIEWER);

    // test add
    try {
      service.addUser(testUser, viewerUserAuthToken);
      fail("Viewer was able to add a user");
    } catch (Exception e) {
      // do nothing
    }

    // must add user to test update/delete
    testUser = (UserJpa) service.addUser(testUser, adminUserAuthToken);

    // test get
    try {
      if (service.getUser(testUser.getId(), viewerUserAuthToken) == null)
        fail("Viewer user was not able to retrieve user");
    } catch (Exception e) {
      fail("Unexpected error for viewer retrieving user");
    }

    // test update
    try {
      service.updateUser(testUser, viewerUserAuthToken);
      fail("Viewer was able to update a user");
    } catch (Exception e) {
      // do nothing
    }

    // test delete
    try {
      service.removeUser(testUser.getId(), viewerUserAuthToken);
      fail("Viewer was able to remove a user");
    } catch (Exception e) {
      // do nothing
    }

    // remove the user as admin
    service.removeUser(testUser.getId(), adminUserAuthToken);

    Logger.getLogger(getClass()).info("  Done!");
  }

  //
  // No degenerate test case for testDegenerateUseRestSecurity003: Logout
  //

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
