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
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.services.helpers.ReportHelper;

/**
 * Example demonstrating loading concepts by terminology id.
 */
public class GetConceptByTerminologyIdTest extends ExampleSupport {

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
   * Demonstrates how to find a SNOMED concept by its concept id.
   *
   * @throws Exception the exception
   */
  @Test
  public void getSnomedConceptByConceptIdTest() throws Exception {

    // Identify
    String terminologyId = "80891009";
    String terminology = "SNOMEDCT_US";
    String version = "2014_09_01";

    // contentClient is defined and initialized in the superclass
    Concept concept =
        contentClient
            .getConcept(terminologyId, terminology, version, authToken);

    // Output the concept as XML
    Logger.getLogger(getClass()).info(
        "xml = " + ConfigUtility.getStringForGraph(concept));

    // Output the concept as JSON
    Logger.getLogger(getClass()).info(
        "json = " + ConfigUtility.getJsonForGraph(concept));

    // Report of the concept
    Logger.getLogger(getClass()).info(ReportHelper.getConceptReport(concept));

  }

  /**
   * Demonstrates how to find a UMLS concept by CUI.
   * 
   * @throws Exception the exception
   */
  @Test
  public void getUmlsConceptByCuiTest() throws Exception {

    // Identify
    String terminologyId = "C0018787";
    String terminology = "UMLS";
    String version = "latest";

    // contentClient is defined and initialized in the superclass
    Concept concept =
        contentClient
            .getConcept(terminologyId, terminology, version, authToken);

    // Output the concept as XML
    Logger.getLogger(getClass()).info(
        "xml = " + ConfigUtility.getStringForGraph(concept));

    // Output the concept as JSON
    Logger.getLogger(getClass()).info(
        "json = " + ConfigUtility.getJsonForGraph(concept));

    // Report of the concept
    Logger.getLogger(getClass()).info(ReportHelper.getConceptReport(concept));

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
