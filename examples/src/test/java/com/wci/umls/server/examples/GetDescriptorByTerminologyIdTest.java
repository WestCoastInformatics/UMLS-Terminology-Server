/*
 * Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package com.wci.umls.server.examples;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.services.helpers.ReportHelper;

/**
 * Example demonstrating loading descriptors by terminology id.
 */
public class GetDescriptorByTerminologyIdTest extends ExampleSupport {

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
   * Demonstrates how to find a MSH descriptor by D#.
   *
   * @throws Exception the exception
   */
  @Test
  public void getMshDescriptorByDescriptorIdTest() throws Exception {

    // Identify
    String terminologyId = "D002319";
    String terminology = "MSH";
    String version = "2015_2014_09_08";

    // contentClient is defined and initialized in the superclass
    Descriptor descriptor =
        contentClient.getDescriptor(terminologyId, terminology, version,
            authToken);

    // Output the descriptor as XML
    Logger.getLogger(getClass()).info(
        "xml = " + ConfigUtility.getStringForGraph(descriptor));

    // Output the descriptor as JSON
    Logger.getLogger(getClass()).info(
        "json = " + ConfigUtility.getJsonForGraph(descriptor));

    // Report of the descriptor
    Logger.getLogger(getClass()).info(
        ReportHelper.getDescriptorReport(descriptor));

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
