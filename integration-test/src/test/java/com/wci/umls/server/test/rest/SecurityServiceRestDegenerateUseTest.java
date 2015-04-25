/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.rest;

import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.wci.umls.server.User;
import com.wci.umls.server.UserRole;
import com.wci.umls.server.helpers.UserList;
import com.wci.umls.server.jpa.UserJpa;
import com.wci.umls.server.jpa.services.rest.SecurityServiceRest;
import com.wci.umls.server.test.helpers.DegenerateUseMethodTestHelper;
import com.wci.umls.server.test.helpers.DegenerateUseMethodTestHelper.ExpectedFailure;

/**
 * Implementation of the "Security Service REST Degenerate Use" Test Cases.
 */
public class SecurityServiceRestDegenerateUseTest extends
    SecurityServiceRestTest {

  /** The auth token. */
  String authToken = null;

  /**
   * Create test fixtures per test.
   *
   * @throws Exception the exception
   */
  @Before
  public void setup() throws Exception {
    // authenticate user
    authToken = service.authenticate(adminUserName, adminUserPassword);
  }

  /**
   * Test degenerate use of the authenticate methods of
   * {@link SecurityServiceRest}.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testDegenerateUseRestSecurity001() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

    Method method =
        service.getClass().getMethod("authenticate", new Class<?>[] {
            String.class, String.class
        });

    Object[] parameters = new Object[] {
        adminUserName, adminUserPassword
    };

    DegenerateUseMethodTestHelper.testDegenerateArguments(service, method,
        parameters);
  }

  /**
   * Test degenerate use of user management methods for
   * {@link SecurityServiceRest}.
   *
   * @throws Exception the exception
   */
  @Test
  public void testDegenerateUseRestSecurity002() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

    // degenerate helper parameters
    Method method;
    Object[] parameters;

    // local variables
    User user = new UserJpa();
    user.setApplicationRole(UserRole.ADMINISTRATOR);
    user.setName("Bad User");
    user.setUserName(badUserName);
    user.setEmail("baduser@example.com");

    // PROCEDURE 1
    Logger.getLogger(getClass()).info("Procedure 1: ADD services");
    method = service.getClass().getMethod("addUser", new Class<?>[] {
        UserJpa.class, String.class
    });

    parameters = new Object[] {
        user, authToken
    };

    // NOTE: This leaves bad user around for further use
    DegenerateUseMethodTestHelper.testDegenerateArguments(service, method,
        parameters);

    // Add user with incomplete user information (e.g. blank name or email)
    // TEST: Should throw deserialization error
    Logger.getLogger(getClass()).info("    Adding user with incomplete fields");
    for (Field field : UserJpa.class.getFields()) {

      // construct the user
      user.setName(properties.getProperty("bad.user"));
      user.setEmail("no email");
      user.setUserName(properties.getProperty("bad.user"));
      user.setApplicationRole(UserRole.VIEWER);

      // set the current iterated field to null and add it
      field.set(user, null);
      try {
        user = service.addUser((UserJpa) user, authToken);

        // if no exception thrown remove user and fail test
        service.removeUser(user.getId(), authToken);
        fail("ADD user with null field " + field.getName()
            + " did not throw expected exception");
      } catch (Exception e) {
        // do nothing
      }
    }

    // PROCEDURE 2
    Logger.getLogger(getClass()).info("  Procedure 2: GET services");

    // first get the user
    user = service.getUser(adminUserName, authToken);

    // test
    method = service.getClass().getMethod("getUser", new Class<?>[] {
        Long.class, String.class
    });

    parameters = new Object[] {
        user.getId(), authToken
    };

    // invalid Long value should return null
    DegenerateUseMethodTestHelper.testDegenerateArguments(service, method,
        parameters, new ExpectedFailure[] {
            ExpectedFailure.LONG_INVALID_NO_RESULTS_NULL_EXCEPTION,
            ExpectedFailure.EXCEPTION
        });

    // Get user with invalid name (does not exist in database)
    // TEST: Should return null

    // first remove bad user (created by tests above)
    service.removeUser(service.getUser(badUserName, authToken).getId(),
        authToken);
    try {
      if (service.getUser(badUserName, authToken) != null) {
        fail("GET non-existent user did not return null");
      }
    } catch (Exception e) {
      fail("GET non-existent user returned exception instead of null");
    }

    // PROCEDURE 3
    Logger.getLogger(getClass()).info("  Procedure 3: UPDATE services");

    // Update user with null argument
    // TEST: Should throw exception
    try {
      service.updateUser(null, authToken);
      fail("Updating user with null value did not throw expected exception");
    } catch (Exception e) {
      // do nothing
    }

    // Update user with null hibernate id
    // TEST: Should throw exception

    user = new UserJpa();
    user.setName(properties.getProperty("bad.user"));
    user.setEmail("no email");
    user.setUserName(properties.getProperty("bad.user"));
    user.setApplicationRole(UserRole.VIEWER);

    // add the user
    user = service.addUser((UserJpa) user, authToken);
    // save for removing later
    Long userId = user.getId();
    try {
      // set the id to null and update
      user.setId(null);
      service.updateUser((UserJpa) user, authToken);

      fail("Updating user with null hibernate id did not throw expected exception");

    } catch (Exception e) {
      // do nothing
    }

    // Update user with incomplete user information
    // TEST: Should throw deserialization error
    for (Field field : UserJpa.class.getFields()) {

      // construct the user user = new UserJpa();
      user.setName(properties.getProperty("bad.user"));
      user.setEmail("no email");
      user.setUserName(properties.getProperty("bad.user"));
      user.setApplicationRole(UserRole.VIEWER);

      // add the user
      service.addUser((UserJpa) user, authToken);
      try {
        // set the current iterated field to null and add it
        field.set(user, null);
        service.updateUser((UserJpa) user, authToken);

        // if no exception thrown remove user and fail test
        service.removeUser(user.getId(), authToken);
        fail("UPDATE user with null field " + field.getName()
            + " did not throw expected exception");
      } catch (Exception e) {
        // do nothing
      }
    }

    // Procedure 4
    Logger.getLogger(getClass()).info("  Procedure 4: DELETE services");

    // Delete user with null id
    // TEST: Should throw exception
    try {
      service.removeUser(new Long(null), authToken);
      fail("DELETE user with null id did not throw expected exception");
    } catch (Exception e) {
      // do nothing
    }

    // Delete user with invalid hibernate id (does not exist)
    // TEST: Should throw exception

    UserList userList = service.getUsers(authToken);
    Long badId = Collections.max(userList.getObjects(), new Comparator<User>() {

      @Override
      public int compare(User u1, User u2) {
        return u1.getId().compareTo(u2.getId());
      }
    }).getId() + 1;
    try {
      service.removeUser(badId, authToken);
      fail("DELETE user with non-existent id did not throw expected exception");
    } catch (Exception e) {
      // do nothing
    }

    // Cleanup
    service.removeUser(userId, authToken);
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
    // n/a
  }

}
