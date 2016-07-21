/*
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.rest;

import static org.junit.Assert.fail;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Implementation of the "Metadata Service REST Degenerate Use" Test Cases.
 */
public class MetadataServiceRestDegenerateUseTest extends
    MetadataServiceRestTest {

  /** The auth token. */
  private static String authToken;

  /**
   * Create test fixtures per test.
   *
   * @throws Exception the exception
   */
  @Override
  @Before
  public void setup() throws Exception {

    // authentication
    authToken =
        securityService.authenticate(testUser, testPassword).getAuthToken();
  }

  /**
   * Test retrieval of all versions for all terminologies
   * @throws Exception
   */
  @Test
  public void testGetCurrentTerminologies() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    try {
      metadataService.getCurrentTerminologies("InvalidAuthToken");
      fail("Getting all terminology/version pairs without authorization token succeeded.");
    } catch (Exception e) {
      // do nothing
    }

  }

  /**
   * Test retrieving all metadata for a terminology
   * @throws Exception
   */
  @Test
  public void testGetAllMetadata() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // test bad authorization
    try {
      metadataService.getAllMetadata("SNOMEDCT", "latest", "InvalidAuthToken");
      fail("Getting metadata for terminology and version without authorization token did not throw expected exception.");
    } catch (Exception e) {
      // do nothing
    }

    // test bad version
    try {
      metadataService.getAllMetadata("SNOMEDCT", "InvalidVersion", authToken);
      fail("Getting metadata for existing terminology with invalid version did not throw expected exception");
    } catch (Exception e) {
      // do nothing
    }

    // test bad terminology
    try {
      metadataService.getAllMetadata("InvalidTerminology", "InvalidVersion",
          authToken);
      fail("Getting metadata for non-existent terminology with invalid version did not throw expected exception");
    } catch (Exception e) {
      // do nothing
    }

  }

  /**
   * Test retrieving terminology objects
   * @throws Exception
   */
  @Test
  public void testGetTerminology() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // test bad authorization
    try {
      metadataService.getTerminology(null, null, authToken);
      fail("Getting terminology with null parameters failed");
    } catch (Exception e) {
      // do nothing
    }

  }

  /**
   * Test degenerate use rest metadata005.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetDefaultPrecedenceList() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // test bad authorization
    try {
      metadataService.getDefaultPrecedenceList(null, null, authToken);
      fail("Getting default precedence list with null parameters failed");
    } catch (Exception e) {
      // do nothing
    }

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

}
