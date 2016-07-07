/*
 * Copyright 2016 West Coast Informatics, LLC
 */
/*
 * 
 */
package com.wci.umls.server.examples;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.wci.umls.server.helpers.SearchResult;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;

/**
 * Example demonstrating finding descriptors by query.
 */
public class FindDescriptorsByStringQueryTest extends ExampleSupport {

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
        securityClient.authenticate(testUser, testPassword).getAuthToken();

  }

  /**
   * Demonstrates how to find a MSH descriptor by query.
   *
   * @throws Exception the exception
   */
  @Test
  public void findMshDescriptorsByStringQueryTest() throws Exception {

    // Terminology/version
    String terminology = "MSH";
    String version = "2015_2014_09_08";

    // Find descriptors using a simple query
    Logger.getLogger(getClass()).info("Find descriptors for 'aspirin'");
    SearchResultList list =
        contentClient.findDescriptors(terminology, version, "aspirin",
            null, authToken);
    for (final SearchResult result : list.getObjects()) {
      Logger.getLogger(getClass()).info("  " + result);
    }

    // Find descriptors using a multi-word query
    Logger.getLogger(getClass()).info(
        "Find descriptors for 'gestational diabetes'");
    list =
        contentClient.findDescriptors(terminology, version,
            "gestational diabetes", null, authToken);
    for (final SearchResult result : list.getObjects()) {
      Logger.getLogger(getClass()).info("  " + result);
    }

    // Find descriptors using a wildcard query that return many results, page
    // first
    // results
    Logger.getLogger(getClass()).info(
        "Find descriptors for 'ge*' with page size 10, first page");
    PfsParameterJpa pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    list =
        contentClient.findDescriptors(terminology, version, "ge*",
            pfs, authToken);
    Logger.getLogger(getClass()).info(
        "  Total results = " + list.getTotalCount());
    for (final SearchResult result : list.getObjects()) {
      Logger.getLogger(getClass()).info("  " + result);
    }

    // Same test, but this time sort on name
    Logger.getLogger(getClass()).info(
        "Find descriptors for 'ge*' with page size 10, first page");
    pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    pfs.setSortField("name");
    list =
        contentClient.findDescriptors(terminology, version, "ge*",
            pfs, authToken);
    Logger.getLogger(getClass()).info(
        "  Total results = " + list.getTotalCount());
    for (final SearchResult result : list.getObjects()) {
      Logger.getLogger(getClass()).info("  " + result);
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
    securityClient.logout(authToken);
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
