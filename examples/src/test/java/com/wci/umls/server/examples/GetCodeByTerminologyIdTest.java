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
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.services.helpers.ReportHelper;

/**
 * Example demonstrating loading codes by terminology id.
 */
public class GetCodeByTerminologyIdTest extends ExampleSupport {

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
   * Demonstrates how to find a SNOMED code by its code id.
   *
   * @throws Exception the exception
   */
  @Test
  public void getSnomedCodeByCodeIdTest() throws Exception {

    // Identify
    String terminologyId = "80891009";
    String terminology = "SNOMEDCT_US";
    String version = "2014_09_01";

    // contentClient is defined and initialized in the superclass
    Code code =
        contentClient.getCode(terminologyId, terminology, version, authToken);

    // Output the code as XML
    Logger.getLogger(getClass()).info(
        "xml = " + ConfigUtility.getStringForGraph(code));

    // Output the code as JSON
    Logger.getLogger(getClass()).info(
        "json = " + ConfigUtility.getJsonForGraph(code));

    // Report of the code
    Logger.getLogger(getClass()).info(ReportHelper.getCodeReport(code));

  }

  /**
   * Demonstrates how to find a MSH code by D#.
   * 
   * @throws Exception the exception
   */
  @Test
  public void getMshCodeByCodeTest() throws Exception {

    // Identify
    String terminologyId = "D002319";
    String terminology = "MSH";
    String version = "2015_2014_09_08";

    // contentClient is defined and initialized in the superclass
    Code code =
        contentClient.getCode(terminologyId, terminology, version, authToken);

    // Output the code as XML
    Logger.getLogger(getClass()).info(
        "xml = " + ConfigUtility.getStringForGraph(code));

    // Output the code as JSON
    Logger.getLogger(getClass()).info(
        "json = " + ConfigUtility.getJsonForGraph(code));

    // Report of the code
    Logger.getLogger(getClass()).info(ReportHelper.getCodeReport(code));

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
