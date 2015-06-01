/*
 * Copyright 2015 West Coast Informatics, LLC
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
 * Example demonstrating loading of relationships for a concept.
 */
public class GetConceptRelationshipsTest extends ExampleSupport {

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
    authToken = securityClient.authenticate(testUser, testPassword);

  }

  /**
   * Demonstrates how to find SNOMED concept relationships.
   *
   * @throws Exception the exception
   */
  @Test
  public void getSnomedConceptRelationshipsTest() throws Exception {

    // Identify
    String terminologyId = "106182000";
    String terminology = "SNOMEDCT_US";
    String version = "2014_09_01";

    // contentClient is defined and initialized in the superclass
    Concept concept =
        contentClient
            .getConcept(terminologyId, terminology, version, authToken);

    // The concept has no relationships at this point
    // because we use a graph resolver that does not pre-load
    // relationships. Instead we require a call-back.
    Logger.getLogger(getClass()).info(
        "  Relationship count = " + concept.getRelationships().size());

    // Loading all relationships for the concept
    RelationshipList list =
        contentClient.findRelationshipsForConcept(terminologyId, terminology,
            version, "", null, authToken);
    Logger.getLogger(getClass()).info(
        "  Total results = " + list.getTotalCount());
    for (Relationship<?, ?> result : list.getObjects()) {
      Logger.getLogger(getClass()).info("  " + result);
    }

    // Loading relationships with paging - in case a relationship has MANY relationships.
    PfsParameterJpa pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    list =
        contentClient.findRelationshipsForConcept(terminologyId, terminology,
            version, "", pfs, authToken);
    Logger.getLogger(getClass()).info(
        "  Total results = " + list.getTotalCount());
    for (Relationship<?, ?> result : list.getObjects()) {
      Logger.getLogger(getClass()).info("  " + result);
    }
    

  }

  /**
   * Demonstrates how to find a UMLS concept relationships.
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
            .getConcept(terminologyId, terminology, version, authToken);

    // The concept has no relationships at this point
    // because we use a graph resolver that does not pre-load
    // relationships. Instead we require a call-back.
    Logger.getLogger(getClass()).info(
        "  Relationship count = " + concept.getRelationships().size());

    // Loading all relationships for the concept
    RelationshipList list =
        contentClient.findRelationshipsForConcept(terminologyId, terminology,
            version, "", null, authToken);
    Logger.getLogger(getClass()).info(
        "  Total results = " + list.getTotalCount());
    for (Relationship<?, ?> result : list.getObjects()) {
      Logger.getLogger(getClass()).info("  " + result);
    }

    // Loading relationships with paging - in case a relationship has MANY relationships.
    PfsParameterJpa pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    list =
        contentClient.findRelationshipsForConcept(terminologyId, terminology,
            version, "", pfs, authToken);
    Logger.getLogger(getClass()).info(
        "  Total results = " + list.getTotalCount());
    for (Relationship<?, ?> result : list.getObjects()) {
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
