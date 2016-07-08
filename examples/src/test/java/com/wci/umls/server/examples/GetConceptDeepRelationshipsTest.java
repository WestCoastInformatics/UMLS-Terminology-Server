/*
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.examples;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.wci.umls.server.helpers.content.RelationshipList;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Relationship;

/**
 * Example demonstrating loading of deep relationships for a concept.
 */
public class GetConceptDeepRelationshipsTest extends ExampleSupport {

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
   * Demonstrates how to find UMLS concept "deep" relationships. This is used
   * when a user wants to know all of the potential CUI2 on the other side of
   * relationships in, say, the MRREL.RRF perspective. This is a call that is
   * only sensible to make on a "metathesaurus"-style terminology.
   * 
   * @throws Exception the exception
   */
  @Test
  public void getUmlsConceptByCuiTest() throws Exception {

    // Identify
    String terminologyId = "C0000294";
    String terminology = "UMLS";
    String version = "latest";

    // contentClient is defined and initialized in the superclass
    Concept concept =
        contentClient
            .getConcept(terminologyId, terminology, version, null, authToken);

    // The concept has no relationships at this point
    // because we use a graph resolver that does not pre-load
    // relationships. Instead we require a call-back.
    Logger.getLogger(getClass()).info(
        "  Relationship count = " + concept.getRelationships().size());

    // Loading all relationships for the concept
    RelationshipList list =
        contentClient.findConceptRelationships(terminologyId, terminology,
            version, "", null, authToken);
    Logger.getLogger(getClass()).info(
        "  Total results = " + list.getTotalCount());
    // See that this concept has only 25 relationships

    // Loading all deep relationships for the concept
    list =
        contentClient.findConceptDeepRelationships(terminologyId,
            terminology, version, null, null, authToken);
    Logger.getLogger(getClass()).info(
        "  Total results = " + list.getTotalCount());
    for (final Relationship<?, ?> result : list.getObjects()) {
      Logger.getLogger(getClass()).info("  " + result);
    }
    // Now, see that this concept has only 126 "deep" relationships (which
    // include the original 25)

    // Loading deep relationships with paging
    PfsParameterJpa pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    list =
        contentClient.findConceptDeepRelationships(terminologyId,
            terminology, version, pfs, null, authToken);
    Logger.getLogger(getClass()).info(
        "  Total results = " + list.getTotalCount());
    for (final Relationship<?, ?> result : list.getObjects()) {
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
