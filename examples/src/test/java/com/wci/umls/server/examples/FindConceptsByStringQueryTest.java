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
public class FindConceptsByStringQueryTest extends ExampleSupport {

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
   * Demonstrates how to find a SNOMED concepts with search strings.
   *
   * @throws Exception the exception
   */
  @Test
  public void findSnomedConceptsByStringQueryTest() throws Exception {

    // Terminology/version
    String terminology = "SNOMEDCT_US";
    String version = "2014_09_01";

    // Find concepts using a simple query
    Logger.getLogger(getClass()).info("Find concepts for 'aspirin'");
    SearchResultList list =
        contentClient.findConcepts(terminology, version, "aspirin",
            null, authToken);
    for (final SearchResult result : list.getObjects()) {
      Logger.getLogger(getClass()).info("  " + result);
    }

    // Find concepts using a multi-word query
    Logger.getLogger(getClass()).info(
        "Find concepts for 'gestational diabetes'");
    list =
        contentClient.findConcepts(terminology, version,
            "gestational diabetes", null, authToken);
    for (final SearchResult result : list.getObjects()) {
      Logger.getLogger(getClass()).info("  " + result);
    }

    // Find concepts using a wildcard query that return many results, page first
    // results
    Logger.getLogger(getClass()).info(
        "Find concepts for 'ge*' with page size 10, first page");
    PfsParameterJpa pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    list =
        contentClient.findConcepts(terminology, version, "ge*", pfs,
            authToken);
    Logger.getLogger(getClass()).info(
        "  Total results = " + list.getTotalCount());
    for (final SearchResult result : list.getObjects()) {
      Logger.getLogger(getClass()).info("  " + result);
    }

    // Same test, but this time sort on name
    Logger.getLogger(getClass()).info(
        "Find concepts for 'ge*' with page size 10, first page");
    pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    pfs.setSortField("name");
    list =
        contentClient.findConcepts(terminology, version, "ge*", pfs,
            authToken);
    Logger.getLogger(getClass()).info(
        "  Total results = " + list.getTotalCount());
    for (final SearchResult result : list.getObjects()) {
      Logger.getLogger(getClass()).info("  " + result);
    }

  }

  /**
   * Demonstrates how to find a UMLS concepts with search strings.
   * @throws Exception the exception
   */
  @Test
  public void findUmlsConceptsByStringQueryTest() throws Exception {

    // Terminology/version
    String terminology = "UMLS";
    String version = "latest";

    // Find concepts using a simple query
    Logger.getLogger(getClass()).info("Find concepts for 'aspirin'");
    SearchResultList list =
        contentClient.findConcepts(terminology, version, "aspirin",
            null, authToken);
    for (final SearchResult result : list.getObjects()) {
      Logger.getLogger(getClass()).info("  " + result);
    }

    // Find concepts using a multi-word query
    Logger.getLogger(getClass()).info(
        "Find concepts for 'gestational diabetes'");
    list =
        contentClient.findConcepts(terminology, version,
            "gestational diabetes", null, authToken);
    for (final SearchResult result : list.getObjects()) {
      Logger.getLogger(getClass()).info("  " + result);
    }

    // Find concepts using a wildcard query that return many results, page first
    // results
    Logger.getLogger(getClass()).info(
        "Find concepts for 'ge*' with page size 10, first page");
    PfsParameterJpa pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    list =
        contentClient.findConcepts(terminology, version, "ge*", pfs,
            authToken);
    Logger.getLogger(getClass()).info(
        "  Total results = " + list.getTotalCount());
    for (final SearchResult result : list.getObjects()) {
      Logger.getLogger(getClass()).info("  " + result);
    }

    // Same test, but this time sort on name
    Logger.getLogger(getClass()).info(
        "Find concepts for 'ge*' with page size 10, first page");
    pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    pfs.setSortField("name");
    list =
        contentClient.findConcepts(terminology, version, "ge*", pfs,
            authToken);
    Logger.getLogger(getClass()).info(
        "  Total results = " + list.getTotalCount());
    for (final SearchResult result : list.getObjects()) {
      Logger.getLogger(getClass()).info("  " + result);
    }

    // Fielded search
    // Find UMLS concepts but only where SNOMEDCT_US has a particular name
    // Find concepts using a multi-word query
    Logger.getLogger(getClass()).info(
        "Find concepts for 'gestational diabetes' but only within SNOMEDCT_US");
    pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    list =
        contentClient.findConcepts(terminology, version,
            "\"gestational diabetes\" atoms.terminology:SNOMEDCT_US", pfs,
            authToken);
    Logger.getLogger(getClass()).info(
        "  Total results = " + list.getTotalCount());
    Logger.getLogger(getClass()).info(
        "  NOTE large number of results, but top pick is the expected one.");
    for (final SearchResult result : list.getObjects()) {
      Logger.getLogger(getClass()).info("  " + result);
    }

    // Fielded search with required clauses
    // Find UMLS concepts but only where SNOMEDCT_US has a particular name
    // Find concepts using a multi-word query
    Logger.getLogger(getClass()).info(
        "Find concepts for 'gestational diabetes' but only within SNOMEDCT_US");
    list =
        contentClient.findConcepts(terminology, version,
            "+\"gestational diabetes\" +atoms.terminology:SNOMEDCT_US", null,
            authToken);
    Logger.getLogger(getClass()).info(
        "  NOTE this time there is only one result");
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
