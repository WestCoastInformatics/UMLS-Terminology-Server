/*
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.rest;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.jpa.services.rest.SecurityServiceRest;
import com.wci.umls.server.rest.client.SecurityClientRest;
import com.wci.umls.server.test.helpers.IntegrationUnitSupport;

/**
 * Implementation of the Template Service REST Use Test Cases.
 */
public class TemplateServiceXxxUseTest extends IntegrationUnitSupport {

  /** The service. */
  @SuppressWarnings("unused")
  private SecurityClientRest service;

  /**
   * Create test fixtures for class.
   *
   * @throws Exception the exception
   */
  @BeforeClass
  public static void setupClass() throws Exception {
    // do nothing
  }

  /**
   * Create test fixtures per test.
   *
   * @throws Exception the exception
   */
  @Before
  public void setup() throws Exception {
    service = new SecurityClientRest(ConfigUtility.getConfigProperties());

    // Use Jpa services here to create objects for test
  }

  /**
   * 
   * {@link SecurityServiceRest}.
   * 
   * @throws Exception the exception
   */
  @Test
  public void testXxxUseRestTemplate001() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

  }

  /**
   * Teardown.
   *
   * @throws Exception the exception
   */
  @After
  public void teardown() throws Exception {
    // Use Jpa services here remove objects for test

    // logout
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
