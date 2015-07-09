/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.rest;

import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;

import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.rest.client.SecurityClientRest;
import com.wci.umls.server.rest.client.ValidationClientRest;
import com.wci.umls.server.services.ContentService;

/**
 * Implementation of the "Validation Service REST Normal Use" Test Cases.
 */
@Ignore
public class ValidationServiceRestTest {

  /** The service. */
  protected static ValidationClientRest validationService;

  /** The security service. */
  protected static SecurityClientRest securityService;
  
  /** The content service. */
  protected static ContentService contentService;

  /** The properties. */
  protected static Properties properties;

  /** The test password. */
  protected static String testUser;

  /** The test password. */
  protected static String testPassword;

  /**
   * Create test fixtures for class.
   *
   * @throws Exception the exception
   */
  @BeforeClass
  public static void setupClass() throws Exception {

    // instantiate properties
    properties = ConfigUtility.getConfigProperties();

    // instantiate required services
    validationService = new ValidationClientRest(properties);
    securityService = new SecurityClientRest(properties);

    contentService = new ContentServiceJpa();

    // test run.config.ts has viewer user
    testUser = properties.getProperty("viewer.user");
    testPassword = properties.getProperty("viewer.password");

    if (testUser == null || testUser.isEmpty()) {
      throw new Exception("Test prerequisite: viewer.user must be specified");
    }
    if (testPassword == null || testPassword.isEmpty()) {
      throw new Exception(
          "Test prerequisite: viewer.password must be specified");
    }


  }

  /**
   * Create test fixtures per test.
   *
   * @throws Exception the exception
   */
  @Before
  public void setup() throws Exception {

    /**
     * Prerequisites
     */

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
