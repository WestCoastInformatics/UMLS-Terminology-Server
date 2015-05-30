/*
 * Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package com.wci.umls.server.test.rest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.wci.umls.server.model.content.Concept;

/**
 * Implementation of the "Content Service REST Degenerate Use" Test Cases.
 */
public class ContentServiceRestDegenerateUseTest extends ContentServiceRestTest {

  /** The auth token. */
  private static String authToken;

  /** The test test id. */
  private String testId;

  /** The test terminology. */
  private String testTerminology;

  /** The test version. */
  private String testVersion;

  /** The concept used in testing. */
  @SuppressWarnings("unused")
  private Concept concept;

  /** The valid parameters used for reflection testing. */
  @SuppressWarnings("unused")
  private Object[] validParameters;

  /**
   * Create test fixtures per test.
   *
   * @throws Exception the exception
   */
  @Override
  @Before
  public void setup() throws Exception {

    // authentication
    authToken = securityService.authenticate(testUser, testPassword);

    // set terminology and version
    testTerminology = "SNOMEDCT";
    testVersion = "latest";
    testId = "102466009";

    // get test concept
    concept =
        contentService.getConcept(testId, testTerminology, testVersion,
            authToken);

  }

  /**
   * Test Get and Find methods for concepts.
   *
   * @throws Exception the exception
   */
  @Test
  public void testDegenerateUseRestContent001() throws Exception {

    // SAMPLE USAGE

//    // get concepts
//    validParameters = new Object[] {
//        testId, testTerminology, testVersion, authToken
//    };
//
//    DegenerateUseMethodTestHelper.testDegenerateArguments(
//        contentService,
//        contentService.getClass().getMethod("getConcepts",
//            getParameterTypes(validParameters)), validParameters,
//
//        // String fields will fail on empty strings, return no results on null
//        // (correct behavior)
//        new ExpectedFailure[] {
//            ExpectedFailure.STRING_INVALID_EXCEPTION_NULL_NO_RESULTS,
//            ExpectedFailure.STRING_INVALID_EXCEPTION_NULL_NO_RESULTS,
//            ExpectedFailure.STRING_INVALID_EXCEPTION_NULL_NO_RESULTS,
//            ExpectedFailure.EXCEPTION
//        });

    
    // TODO: Implement these methods exactly in order according to the spreadsheet
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
