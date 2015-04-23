/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.rest;

import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;

import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.rest.client.SecurityClientRest;

/**
 * The Class SecurityServiceRestTest.
 */
@Ignore
public class SecurityServiceRestTest {
  /** The service. */
  protected static SecurityClientRest service;

  /** The properties. */
  protected static Properties properties;

  /** The viewer user password. */
  protected static String viewerUserName;

  /** The viewer user password. */
  protected static String viewerUserPassword;

  /** The admin user password. */
  protected static String adminUserName;

  /** The admin user password. */
  protected static String adminUserPassword;

  /** The bad user password. */
  protected static String badUserName;

  /** The bad user password. */
  protected static String badUserPassword;

  /**
   * Create test fixtures for class.
   *
   * @throws Exception the exception
   */
  @BeforeClass
  public static void setupClass() throws Exception {

    // get the properties
    properties = ConfigUtility.getConfigProperties();

    // instantiate the service
    service = new SecurityClientRest(properties);

    /**
     * Prerequisites
     */

    // test.user and test.password must be set
    viewerUserName = properties.getProperty("viewer.user");
    viewerUserPassword = properties.getProperty("viewer.password");

    if (viewerUserName == null || viewerUserName.isEmpty()) {
      throw new Exception(
          "Test prerequisite:  viewer.user must be set in config properties file");
    }

    if (viewerUserPassword == null || viewerUserPassword.isEmpty()) {
      throw new Exception(
          "Test prerequisite:  viewer.password must be set in config properties file");
    }

    // admin.user and admin.password must be set
    adminUserName = properties.getProperty("admin.user");
    adminUserPassword = properties.getProperty("admin.password");

    if (adminUserName == null || adminUserName.isEmpty()) {
      throw new Exception(
          "Test prerequisite:  admin.user must be set in config properties file");
    }

    if (adminUserPassword == null || adminUserPassword.isEmpty()) {
      throw new Exception(
          "Test prerequisite:  admin.password must be set in config properties file");
    }

    // bad user must be specified
    badUserName = properties.getProperty("bad.user");

    if (badUserName == null || badUserName.isEmpty()) {
      throw new Exception(
          "Test prerequisite:  A non-existent (bad) user must be specified in config properties file");
    }

    String authToken = service.authenticate(adminUserName, adminUserPassword);
    if (service.getUser(badUserName, authToken) != null) {
      throw new Exception(
          "Test prerequisite:  The bad user specified in config properties file should not exist in database");
    }
    service.logout(authToken);
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
