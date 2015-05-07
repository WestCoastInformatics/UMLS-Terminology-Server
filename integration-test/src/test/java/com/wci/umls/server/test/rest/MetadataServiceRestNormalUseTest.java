/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.rest;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
   * Test UMLS metadata.
   *
   * @param keyValuePairLists the key value pair lists
   * @return true, if successful
   * @throws Exception the exception
   */
  private boolean testUmlsMetadata(KeyValuePairLists keyValuePairLists)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "Testing UMLS metadata retrieval, " + keyValuePairLists.getCount()
            + " pair lists found (" + MetadataKeys.values().length
            + " expected)");
    Logger.getLogger(getClass()).info(keyValuePairLists);

    Map<MetadataKeys, Integer> expectedSizes = new HashMap<>();
    Map<MetadataKeys, Integer> expectedSizes2 = new HashMap<>();
    Map<MetadataKeys, String> expectedIds = new HashMap<>();
    Map<MetadataKeys, Set<String>> expectedNames = new HashMap<>();

    // Relationship types
    expectedSizes.put(MetadataKeys.Relationship_Types, 9);
    expectedSizes2.put(MetadataKeys.Relationship_Types, 9);
    expectedIds.put(MetadataKeys.Relationship_Types, "PAR");
    expectedNames.put(MetadataKeys.Relationship_Types, new HashSet<String>());
    expectedNames.get(MetadataKeys.Relationship_Types).add(
        "has parent relationship in a Metathesaurus source vocabulary");

    // Additional relationship types
    expectedSizes.put(MetadataKeys.Additional_Relationship_Types, 82);
    expectedSizes2.put(MetadataKeys.Additional_Relationship_Types, 82);
    expectedIds.put(MetadataKeys.Additional_Relationship_Types, "isa");
    expectedNames.put(MetadataKeys.Additional_Relationship_Types,
        new HashSet<String>());
    expectedNames.get(MetadataKeys.Additional_Relationship_Types).add("Is a");

    // Attribute names
    expectedSizes.put(MetadataKeys.Attribute_Names, 97);
    expectedSizes2.put(MetadataKeys.Attribute_Names, 445);
    expectedIds.put(MetadataKeys.Attribute_Names, "ACCEPTABILITYID");
    expectedNames.put(MetadataKeys.Attribute_Names, new HashSet<String>());
    expectedNames.get(MetadataKeys.Attribute_Names).add("Acceptability Id");

    // Semantic types
    expectedSizes.put(MetadataKeys.Semantic_Types, 133);
    expectedSizes2.put(MetadataKeys.Semantic_Types, 133);
    expectedIds.put(MetadataKeys.Semantic_Types, "clnd");
    expectedNames.put(MetadataKeys.Semantic_Types, new HashSet<String>());
    expectedNames.get(MetadataKeys.Semantic_Types).add("Clinical Drug");

    // Term types
    expectedSizes.put(MetadataKeys.Term_Types, 47);
    expectedSizes2.put(MetadataKeys.Term_Types, 47);
    expectedIds.put(MetadataKeys.Term_Types, "PT");
    expectedNames.put(MetadataKeys.Term_Types, new HashSet<String>());
    expectedNames.get(MetadataKeys.Term_Types).add("Designated preferred name");

    // Hierarchical relationship types
    expectedSizes.put(MetadataKeys.Hierarchical_Relationship_Types, 1);
    expectedSizes2.put(MetadataKeys.Hierarchical_Relationship_Types, 1);
    expectedIds.put(MetadataKeys.Hierarchical_Relationship_Types, "CHD");
    expectedNames.put(MetadataKeys.Hierarchical_Relationship_Types,
        new HashSet<String>());
    expectedNames.get(MetadataKeys.Hierarchical_Relationship_Types).add(
        "has child relationship in a Metathesaurus source vocabulary");

    boolean result =
        testHelper(keyValuePairLists, expectedSizes, expectedSizes2,
            expectedIds, expectedNames);

    return result;
  }

  /**
   * Test snomed metadata.
   *
   * @param keyValuePairLists the key value pair lists
   * @return true, if successful
   * @throws Exception the exception
   */
  private boolean testSnomedMetadata(KeyValuePairLists keyValuePairLists)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "Testing SNOMEDCT_US metadata retrieval, "
            + keyValuePairLists.getCount() + " pair lists found ("
            + MetadataKeys.values().length + " expected)");

    Map<MetadataKeys, Integer> expectedSizes = new HashMap<>();
    Map<MetadataKeys, Integer> expectedSizes2 = new HashMap<>();
    Map<MetadataKeys, String> expectedIds = new HashMap<>();
    Map<MetadataKeys, Set<String>> expectedNames = new HashMap<>();

    // Relationship types
    expectedSizes.put(MetadataKeys.Relationship_Types, 6);
    expectedSizes2.put(MetadataKeys.Relationship_Types, 6);
    expectedIds.put(MetadataKeys.Relationship_Types, "PAR");
    expectedNames.put(MetadataKeys.Relationship_Types, new HashSet<String>());
    expectedNames.get(MetadataKeys.Relationship_Types).add(
        "has parent relationship in a Metathesaurus source vocabulary");

    // Additional relationship types
    expectedSizes.put(MetadataKeys.Additional_Relationship_Types, 61);
    expectedSizes2.put(MetadataKeys.Additional_Relationship_Types, 61);
    expectedIds.put(MetadataKeys.Additional_Relationship_Types, "has_temporal_context");
    expectedNames.put(MetadataKeys.Additional_Relationship_Types,
        new HashSet<String>());
    expectedNames.get(MetadataKeys.Additional_Relationship_Types).add("Has temporal context");

    // Attribute names
    expectedSizes.put(MetadataKeys.Attribute_Names, 43);
    expectedSizes2.put(MetadataKeys.Attribute_Names, 43);
    expectedIds.put(MetadataKeys.Attribute_Names, "ACCEPTABILITYID");
    expectedNames.put(MetadataKeys.Attribute_Names, new HashSet<String>());
    expectedNames.get(MetadataKeys.Attribute_Names).add("Acceptability Id");

    // Semantic types
    expectedSizes.put(MetadataKeys.Semantic_Types, 0);
    expectedSizes2.put(MetadataKeys.Semantic_Types, 0);
    expectedIds.put(MetadataKeys.Semantic_Types, "clnd");
    expectedNames.put(MetadataKeys.Semantic_Types, new HashSet<String>());
    //expectedNames.get(MetadataKeys.Semantic_Types).add("Clinical Drug");

    // Term types
    expectedSizes.put(MetadataKeys.Term_Types, 18);
    expectedSizes2.put(MetadataKeys.Term_Types, 18);
    expectedIds.put(MetadataKeys.Term_Types, "PT");
    expectedNames.put(MetadataKeys.Term_Types, new HashSet<String>());
    expectedNames.get(MetadataKeys.Term_Types).add("Designated preferred name");

    // Hierarchical relationship types
    expectedSizes.put(MetadataKeys.Hierarchical_Relationship_Types, 1);
    expectedSizes2.put(MetadataKeys.Hierarchical_Relationship_Types, 1);
    expectedIds.put(MetadataKeys.Hierarchical_Relationship_Types, "CHD");
    expectedNames.put(MetadataKeys.Hierarchical_Relationship_Types,
        new HashSet<String>());
    expectedNames.get(MetadataKeys.Hierarchical_Relationship_Types).add(
        "has child relationship in a Metathesaurus source vocabulary");

    boolean result =
        testHelper(keyValuePairLists, expectedSizes, expectedSizes2,
            expectedIds, expectedNames);

    return result;
  }
  

  /**
   * Test msh metadata.
   *
   * @param keyValuePairLists the key value pair lists
   * @return true, if successful
   * @throws Exception the exception
   */
  private boolean testMshMetadata(KeyValuePairLists keyValuePairLists)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "Testing MSH metadata retrieval, "
            + keyValuePairLists.getCount() + " pair lists found ("
            + MetadataKeys.values().length + " expected)");

    Map<MetadataKeys, Integer> expectedSizes = new HashMap<>();
    Map<MetadataKeys, Integer> expectedSizes2 = new HashMap<>();
    Map<MetadataKeys, String> expectedIds = new HashMap<>();
    Map<MetadataKeys, Set<String>> expectedNames = new HashMap<>();

    // Relationship types
    expectedSizes.put(MetadataKeys.Relationship_Types, 7);
    expectedSizes2.put(MetadataKeys.Relationship_Types, 7);
    expectedIds.put(MetadataKeys.Relationship_Types, "PAR");
    expectedNames.put(MetadataKeys.Relationship_Types, new HashSet<String>());
    expectedNames.get(MetadataKeys.Relationship_Types).add(
        "has parent relationship in a Metathesaurus source vocabulary ");

    // Additional relationship types
    expectedSizes.put(MetadataKeys.Additional_Relationship_Types, 7);
    expectedSizes2.put(MetadataKeys.Additional_Relationship_Types, 7);
    expectedIds.put(MetadataKeys.Additional_Relationship_Types, "isa");
    expectedNames.put(MetadataKeys.Additional_Relationship_Types,
        new HashSet<String>());
    expectedNames.get(MetadataKeys.Additional_Relationship_Types).add("Is a");

    // Attribute names
    expectedSizes.put(MetadataKeys.Attribute_Names, 27);
    expectedSizes2.put(MetadataKeys.Attribute_Names, 27);
    expectedIds.put(MetadataKeys.Attribute_Names, "ACCEPTABILITYID");
    expectedNames.put(MetadataKeys.Attribute_Names, new HashSet<String>());
    expectedNames.get(MetadataKeys.Attribute_Names).add("Acceptability Id");

    // Semantic types
    expectedSizes.put(MetadataKeys.Semantic_Types, 0);
    expectedSizes2.put(MetadataKeys.Semantic_Types, 0);
    expectedIds.put(MetadataKeys.Semantic_Types, "clnd");
    expectedNames.put(MetadataKeys.Semantic_Types, new HashSet<String>());
    //expectedNames.get(MetadataKeys.Semantic_Types).add("Clinical Drug");

    // Term types
    expectedSizes.put(MetadataKeys.Term_Types, 19);
    expectedSizes2.put(MetadataKeys.Term_Types, 19);
    expectedIds.put(MetadataKeys.Term_Types, "PT");
    expectedNames.put(MetadataKeys.Term_Types, new HashSet<String>());
    expectedNames.get(MetadataKeys.Term_Types).add("Designated preferred name");

    // General metadata entries
    expectedSizes.put(MetadataKeys.Term_Types, 247);
    expectedSizes2.put(MetadataKeys.Term_Types, 247);
    expectedIds.put(MetadataKeys.Term_Types, "SCUI");
    expectedNames.put(MetadataKeys.Term_Types, new HashSet<String>());
    expectedNames.get(MetadataKeys.Term_Types).add(
        "Source asserted concept unique identifier");

    // Hierarchical relationship types
    expectedSizes.put(MetadataKeys.Hierarchical_Relationship_Types, 1);
    expectedSizes2.put(MetadataKeys.Hierarchical_Relationship_Types, 1);
    expectedIds.put(MetadataKeys.Hierarchical_Relationship_Types, "CHD");
    expectedNames.put(MetadataKeys.Hierarchical_Relationship_Types,
        new HashSet<String>());
    expectedNames.get(MetadataKeys.Hierarchical_Relationship_Types).add(
        "has child relationship in a Metathesaurus source vocabulary");

    boolean result =
        testHelper(keyValuePairLists, expectedSizes, expectedSizes2,
            expectedIds, expectedNames);

    return result;
  }

  /**
   * Test helper.
   *
   * @param keyValuePairLists the key value pair lists
   * @param expectedSizes the expected sizes
   * @param expectedSizes2 the expected sizes2
   * @param expectedIds the expected ids
   * @param expectedNameSets the expected names
   * @return true, if successful
   * @throws Exception the exception
   */
  private boolean testHelper(KeyValuePairLists keyValuePairLists,
    Map<MetadataKeys, Integer> expectedSizes,
    Map<MetadataKeys, Integer> expectedSizes2,
    Map<MetadataKeys, String> expectedIds,
    Map<MetadataKeys, Set<String>> expectedNameSets) throws Exception {

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
      Set<KeyValuePair> pairsNotMatched = new HashSet<>();

      Logger.getLogger(getClass()).info(
          "Checking " + keyValuePairList.getKeyValuePairList().size() + " "
              + keyValuePairList.getName());

      // for each type of metadata category, specify:
      // (1) the expected number of concepts returned
      // (2) the id and all possible names of a single concept expected to be in
      // the list
      switch (MetadataKeys.valueOf(keyValuePairList.getName())) {
        case Hierarchical_Relationship_Types:
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

      int expectedSize =
          expectedSizes.get(MetadataKeys.valueOf(keyValuePairList.getName()));
      int expectedSize2 =
          expectedSizes2.get(MetadataKeys.valueOf(keyValuePairList.getName()));
      String expectedId =
          expectedIds.get(MetadataKeys.valueOf(keyValuePairList.getName()));
      Set<String> expectedNames =
          expectedNameSets
              .get(MetadataKeys.valueOf(keyValuePairList.getName()));
      List<KeyValuePair> pairs = keyValuePairList.getKeyValuePairList();

      KeyValuePair testCase = null;

      if (expectedSize == 0 && pairs.size() == 0) {
        categorySuccessCt++;
      } 
      // if this case has been specified, check it
      else if (expectedSize != -1 && pairs.size() != 0) {

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
        "Metadata Categories Validated:  " + categorySuccessCt
            + " out of " + MetadataKeys.values().length);

    return categorySuccessCt == MetadataKeys.values().length;
  }


}
