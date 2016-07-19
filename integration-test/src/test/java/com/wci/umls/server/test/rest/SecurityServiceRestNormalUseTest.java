/*
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.wci.umls.server.User;
import com.wci.umls.server.UserRole;
import com.wci.umls.server.jpa.UserJpa;
import com.wci.umls.server.jpa.services.rest.SecurityServiceRest;

/**
 * Implementation of the "Security Service REST Normal Use" Test Cases.
 */
public class SecurityServiceRestNormalUseTest extends SecurityServiceRestTest {

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
   * Test normal use of the authenticate methods of {@link SecurityServiceRest}.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testAuthenticate() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    String authToken =
        service.authenticate(viewerUserName, viewerUserPassword).getAuthToken();
    if (authToken == null || authToken.isEmpty()) {
      fail("Failed to authenticate viewer user");
    }

    authToken =
        service.authenticate(adminUserName, adminUserPassword).getAuthToken();
    if (authToken == null || authToken.isEmpty()) {
      fail("Failed to authenticate admin user");
    }
  }

  /**
   * Test normal use of user management methods for {@link SecurityServiceRest}.
   *
   * @throws Exception the exception
   */
  @Test
  public void testUserManagement() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // local variables
    User user;
    String viewerUserNameAuthToken, adminAuthToken;

    // authorize the user
    adminAuthToken =
        service.authenticate(adminUserName, adminUserPassword).getAuthToken();

    // PROCEDURE 1: add a user
    Logger.getLogger(getClass()).info("  Procedure 1: add a user");

    user = new UserJpa();
    user.setApplicationRole(UserRole.VIEWER);
    user.setEmail("none");
    user.setName(badUserName);
    user.setUserName(badUserName + new Date().getTime());

    // add the user and verify that hibernate id has been set
    user = service.addUser((UserJpa) user, adminAuthToken);
    assertTrue(user != null);

    // PROCEDURE 2: get a user
    Logger.getLogger(getClass()).info("  Procedure 2: get a user");

    user = service.getUser(user.getId(), adminAuthToken);
    assertTrue(user != null);

    // PROCEDURE 3: update a user
    Logger.getLogger(getClass()).info("  Procedure 3: update a user");
    user.setEmail("new email");
    service.updateUser((UserJpa) user, adminAuthToken);
    user = service.getUser(user.getId(), adminAuthToken);
    assertEquals("new email", user.getEmail());

    // PROCEDURE 4: remove a user
    Logger.getLogger(getClass()).info("  Procedure 4: remove a user");

    service.removeUser(user.getId(), adminAuthToken);
    user = service.getUser(user.getId(), adminAuthToken);
    assertTrue(user == null);

  }

  /**
   * Test normal use of logout for {@link SecurityServiceRest}.
   *
   * @throws Exception the exception
   */
  @Test
  public void testLogout() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    service.authenticate("guest", "guest");

    service.authenticate("admin", "admin");
    service.logout("guest");
    service.logout("admin");
  }

  /**
   * Teardown.
   *
   * @throws Exception the exception
   */
  @After
  public void teardown() throws Exception {
    // n/a
  }

}
