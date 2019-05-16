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
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.Relationship;

/**
 * Example demonstrating loading of relationships for a descriptor.
 */
public class GetDescriptorRelationshipsTest extends ExampleSupport {

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
   * Demonstrates how to find MSH descriptor relationships.
   *
   * @throws Exception the exception
   */
  @Test
  public void getMshDescriptorRelationshipsTest() throws Exception {

    // Identify
    String terminologyId = "D000006";
    String terminology = "MSH";
    String version = "2015_2014_09_08";

    // contentClient is defined and initialized in the superclass
    Descriptor descriptor =
        contentClient.getDescriptor(terminologyId, terminology, version, null,
            authToken);

    // The descriptor has no relationships at this point
    // because we use a graph resolver that does not pre-load
    // relationships. Instead we require a call-back.
    Logger.getLogger(getClass()).info(
        "  Relationship count = " + descriptor.getRelationships().size());

    // Loading all relationships for the descriptor
    RelationshipList list =
        contentClient.findDescriptorRelationships(terminologyId,
            terminology, version, "", null, authToken);
    Logger.getLogger(getClass()).info(
        "  Total results = " + list.getTotalCount());
    for (final Relationship<?, ?> result : list.getObjects()) {
      Logger.getLogger(getClass()).info("  " + result);
    }

    // Loading relationships with paging - in case a relationship has MANY
    // relationships.
    PfsParameterJpa pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    list =
        contentClient.findDescriptorRelationships(terminologyId,
            terminology, version, "", pfs, authToken);
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
  
  public Class<?>[] getParameterTypes(Object[] parameters) {
    Class<?>[] types = new Class<?>[parameters.length];
    for (int i = 0; i < parameters.length; i++) {
      types[i] = parameters[i].getClass();
    }
    return types;
  }

}
