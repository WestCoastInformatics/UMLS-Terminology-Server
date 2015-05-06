/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.rest;

import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.wci.umls.server.helpers.KeyValuePair;
import com.wci.umls.server.helpers.KeyValuePairList;
import com.wci.umls.server.helpers.KeyValuePairLists;
import com.wci.umls.server.services.MetadataService.MetadataKeys;

/**
 * Implementation of the "Metadata Service REST Normal Use" Test Cases.
 */
public class MetadataServiceRestNormalUseTest extends MetadataServiceRestTest {

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
    authToken = securityService.authenticate(testUser, testPassword);

  }

  /**
   * Test retrieval of all terminology/version pairs.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestMetadata001() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

    KeyValuePairLists keyValuePairLists =
        metadataService.getAllTerminologiesVersions(authToken);
    Logger.getLogger(getClass()).debug("  data = " + keyValuePairLists);

    // flags for whether UMLS, SNOMEDCT_US, and MSH were found
    boolean foundUmls = false;
    boolean foundSnomedct = false;
    boolean foundMsh = false;

    for (KeyValuePairList keyValuePairList : keyValuePairLists
        .getKeyValuePairLists()) {
      for (KeyValuePair keyValuePair : keyValuePairList.getKeyValuePairList()) {

        // test versions
        switch (keyValuePair.getKey()) {
          case "UMLS":
            foundUmls = true;
            assertTrue(keyValuePair.getValue().equals("latest"));
            break;
          case "SNOMEDCT_US":
            foundSnomedct = true;
            assertTrue(keyValuePair.getValue().equals("2014_09_01"));
            break;
          case "MSH":
            foundMsh = true;
            assertTrue(keyValuePair.getValue().equals("2015_2014_09_08"));
            break;
          default:
            // ignore other terminologies, only three above are assumed
            break;
        }
      }
    }

    // test that both were found
    assertTrue(foundSnomedct && foundMsh && foundUmls);
  }

  /**
   * Tests retrieval of all terminology and latest version pairs NOTE: Test is
   * identical to testNormalUseRestMetadata001 but uses different API call.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestMetadata002() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

    // flags for whether SNOMEDCT_US and ICD9CM were found
    boolean foundUmls = false;
    boolean foundSnomedct = false;
    boolean foundMsh = false;

    // make the call
    KeyValuePairList keyValuePairList =
        metadataService.getAllTerminologiesLatestVersions(authToken);

    // cycle over each pair in list
    for (KeyValuePair keyValuePair : keyValuePairList.getKeyValuePairList()) {

      // test versions
      switch (keyValuePair.getKey()) {
        case "UMLS":
          foundUmls = true;
          assertTrue(keyValuePair.getValue().equals("latest"));
          break;
        case "SNOMEDCT_US":
          foundSnomedct = true;
          assertTrue(keyValuePair.getValue().equals("2014_09_01"));
          break;
        case "MSH":
          foundMsh = true;
          assertTrue(keyValuePair.getValue().equals("2015_2014_09_08"));
          break;
        default:
          // ignore other terminologies, only three above are assumed
          break;
      }

    }

    assertTrue(foundSnomedct && foundMsh && foundUmls);
  }

  /**
   * Test retrieving all metadata for a terminology.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestMetadata003() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

    // test UMLS metadata
    assertTrue(testUmlsMetadata(metadataService.getAllMetadata("UMLS",
        "latest", authToken)));

    // test SNOMED metadata
    assertTrue(testSnomedMetadata(metadataService.getAllMetadata("SNOMEDCT_US",
        "2014_09_01", authToken)));

    // test MSH metadata
    assertTrue(testMshMetadata(metadataService.getAllMetadata("MSH",
        "2015_2014_09_08", authToken)));
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
   * Test UMLS metadata
   *
   * @param keyValuePairLists the key value pair lists
   * @return true, if successful
   * @throws Exception the exception
   */
  private boolean testUmlsMetadata(KeyValuePairLists keyValuePairLists)
    throws Exception {

    /**
     * Three checks: (1) Metadata matches hard-coded values taken from
     * SNOMEDCT_US browser (2) Non-group rel types and hierarchical rel types
     * are relationship types (3) Stated and inferred characteristic types are
     * characteristic types
     */

    Logger.getLogger(getClass()).info(
        "Testing UMLS metadata retrieval, " + keyValuePairLists.getCount()
            + " pair lists found (" + MetadataKeys.values().length
            + " expected)");

    // the count of categories successfully passing test
    int categorySuccessCt = 0;

    KeyValuePairList relTypes = null;
    // retrieve relationship types and additional relationship types for ease of
    // access
    for (KeyValuePairList keyValuePairList : keyValuePairLists
        .getKeyValuePairLists()) {
      if (MetadataKeys.valueOf(keyValuePairList.getName()).equals(
          MetadataKeys.Relationship_Types)) {
        relTypes = keyValuePairList;
      }
    }

    // cycle over all retrieved metadata
    for (KeyValuePairList keyValuePairList : keyValuePairLists
        .getKeyValuePairLists()) {

      // initialize the test variables
      int expectedSize = -1;
      int expectedSize2 = -1;
      String expectedId = null;
      Set<String> expectedNames = new HashSet<>();
      Set<KeyValuePair> pairsNotMatched = new HashSet<>();

      Logger.getLogger(getClass()).info(
          "Checking " + keyValuePairList.getKeyValuePairList().size() + " "
              + keyValuePairList.getName());

      // for each type of metadata category, specify:
      // (1) the expected number of concepts returned
      // (2) the id and all possible names of a single concept expected to be in
      // the list
      switch (MetadataKeys.valueOf(keyValuePairList.getName())) {
        case Relationship_Types:
          expectedSize = 9;
          expectedId = "PAR";
          expectedNames
              .add("has parent relationship in a Metathesaurus source vocabulary ");
          break;
        case Additional_Relationship_Types:
          expectedSize = 82;
          expectedId = "isa";
          expectedNames.add("Is a");
          break;
        case Attribute_Names:
          expectedSize = 97;
          expectedSize2 = 445;
          expectedId = "ACCEPTABILITYID";
          expectedNames.add("Acceptability Id");
          break;
        case Semantic_Types:
          expectedSize = 133;
          expectedId = "clnd";
          expectedNames.add("Clinical Drug");
          break;
        case Term_Types:
          expectedSize = 47;
          expectedId = "PT";
          expectedNames.add("Designated preferred name");
          break;
        case General_Metadata_Entries:
          expectedSize = 247;
          expectedId = "SCUI";
          expectedNames.add("Source asserted concept unique identifier");
          break;
        case Hierarchical_Relationship_Types:
          expectedSize = 1;
          expectedId = "CHD";
          expectedNames
              .add("has child relationship in a Metathesaurus source vocabulary");

          // if all values not in the relationship type list,
          // decrement success counter
          for (KeyValuePair pair : keyValuePairList.getKeyValuePairList()) {
            if (relTypes == null || !relTypes.contains(pair)) {
              pairsNotMatched.add(pair);
            }
          }

          if (pairsNotMatched.size() > 0) {
            Logger
                .getLogger(getClass())
                .error(
                    "The following hierarchical relationship types are not found in the set of relationship types:");
            for (KeyValuePair pair : pairsNotMatched) {
              Logger.getLogger(getClass()).error(
                  "  " + pair.getKey() + ", " + pair.getValue());
            }
          }
          break;
        default:
          break;

      }

      List<KeyValuePair> pairs = keyValuePairList.getKeyValuePairList();
      KeyValuePair testCase = null;

      // if this case has been specified, check it
      if (expectedSize != -1 && pairs.size() != 0) {

        for (KeyValuePair pair : pairs) {
          if (expectedId != null && expectedId.equals(pair.getKey())
              && expectedNames.contains(pair.getValue()))
            testCase = pair;
        }

        if (expectedSize != pairs.size() && expectedSize2 != pairs.size()) {
          Logger.getLogger(getClass()).warn(
              "  Expected size " + expectedSize + ", " + expectedSize2
                  + " did not match actual size " + pairs.size());
          Logger.getLogger(getClass()).info("  Retrieved pairs were: ");
          for (KeyValuePair pair : pairs) {
            Logger.getLogger(getClass()).info("    " + pair.toString());
          }
        }

        else if (testCase == null) {
          Logger.getLogger(getClass()).warn(
              "  Could not find pair for id = " + expectedId + ", names "
                  + expectedNames.toString());
          Logger.getLogger(getClass()).info("  Available pairs were: ");
          for (KeyValuePair pair : pairs) {
            Logger.getLogger(getClass()).info("    " + pair.toString());
          }
        } else {
          categorySuccessCt++;
        }
      }
    }

    Logger.getLogger(getClass()).info(
        "UMLS Metadata Categories Validated:  " + categorySuccessCt
            + " out of " + MetadataKeys.values().length);

    return categorySuccessCt == MetadataKeys.values().length;
  }

  /**
   * Test snomed metadata.
   *
   * @param keyValuePairLists the key value pair lists
   * @return true, if successful
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  private boolean testSnomedMetadata(KeyValuePairLists keyValuePairLists)
    throws Exception {

    System.out.println(keyValuePairLists);
    return false;
  }

  /**
   * Test msh metadata.
   *
   * @param keyValuePairLists the key value pair lists
   * @return true, if successful
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  private boolean testMshMetadata(KeyValuePairLists keyValuePairLists)
    throws Exception {
    System.out.println(keyValuePairLists);
    return false;
  }

}
