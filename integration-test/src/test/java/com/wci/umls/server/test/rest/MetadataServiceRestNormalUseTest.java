/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
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
import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.helpers.meta.TerminologyList;
import com.wci.umls.server.model.meta.IdType;
import com.wci.umls.server.model.meta.Terminology;
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
    authToken =
        securityService.authenticate(testUser, testPassword).getAuthToken();

  }

  /**
   * Test retrieval of all terminology/version pairs.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestMetadata001() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

    TerminologyList termList = metadataService.getTerminologies(authToken);
    Logger.getLogger(getClass()).debug("  data = " + termList);

    // flags for whether UMLS, SNOMEDCT_US, and MSH were found
    boolean foundUmls = false;
    boolean foundSnomedct = false;
    boolean foundMsh = false;

    for (Terminology terminology : termList.getObjects()) {
      // test versions
      switch (terminology.getTerminology()) {
        case "UMLS":
          foundUmls = true;
          assertTrue(terminology.getVersion().equals("latest"));
          break;
        case "SNOMEDCT_US":
          foundSnomedct = true;
          assertTrue(terminology.getVersion().equals("2014_09_01"));
          break;
        case "MSH":
          foundMsh = true;
          assertTrue(terminology.getVersion().equals("2015_2014_09_08"));
          break;
        default:
          // ignore other terminologies, only three above are assumed
          break;
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
    TerminologyList termList =
        metadataService.getAllTerminologiesLatestVersions(authToken);

    // cycle over each pair in list
    for (Terminology terminology : termList.getObjects()) {

      // test versions
      switch (terminology.getTerminology()) {
        case "UMLS":
          foundUmls = true;
          assertTrue(terminology.getVersion().equals("latest"));
          break;
        case "SNOMEDCT_US":
          foundSnomedct = true;
          assertTrue(terminology.getVersion().equals("2014_09_01"));
          break;
        case "MSH":
          foundMsh = true;
          assertTrue(terminology.getVersion().equals("2015_2014_09_08"));
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
   * Test normal use of obtaining terminology objects.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestMetadata004() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

    // test UMLS metadata
    Terminology umls =
        metadataService.getTerminology("UMLS", "latest", authToken);
    assertEquals("loader", umls.getLastModifiedBy());
    assertFalse(umls.isAssertsRelDirection());
    assertTrue(umls.isCurrent());
    assertFalse(umls.isDescriptionLogicTerminology());
    assertNull(umls.getEndDate());
    assertNull(umls.getStartDate());
    assertEquals(IdType.CONCEPT, umls.getOrganizingClassType());
    assertEquals("UMLS", umls.getPreferredName());
    assertEquals("UMLS", umls.getTerminology());
    assertEquals("latest", umls.getVersion());

    assertEquals("UMLS", umls.getRootTerminology().getTerminology());
    // Because of XML Transient
    assertNull(umls.getRootTerminology().getLastModifiedBy());
    assertNull(umls.getRootTerminology().getFamily());
    assertNull(umls.getRootTerminology().getHierarchicalName());
    assertNull(umls.getRootTerminology().getLanguage());
    assertNull(umls.getRootTerminology().getPreferredName());
    assertNull(umls.getRootTerminology().getShortName());
    assertNull(umls.getRootTerminology().getAcquisitionContact());
    assertNull(umls.getRootTerminology().getContentContact());
    assertNull(umls.getRootTerminology().getLicenseContact());

    // test UMLS metadata
    Terminology snomed =
        metadataService.getTerminology("SNOMEDCT_US", "2014_09_01", authToken);
    assertEquals("loader", snomed.getLastModifiedBy());
    assertTrue(snomed.isAssertsRelDirection());
    assertTrue(snomed.isCurrent());
    assertFalse(snomed.isDescriptionLogicTerminology());
    assertNull(snomed.getEndDate());
    assertNull(snomed.getStartDate());
    assertEquals(IdType.CONCEPT, snomed.getOrganizingClassType());
    assertEquals("US Edition of SNOMED CT, 2014_09_01",
        snomed.getPreferredName());
    assertEquals("SNOMEDCT_US", snomed.getTerminology());
    assertEquals("2014_09_01", snomed.getVersion());

    assertEquals("SNOMEDCT_US", snomed.getRootTerminology().getTerminology());
    // Because of XML Transient
    assertNull(snomed.getRootTerminology().getLastModifiedBy());
    assertNull(snomed.getRootTerminology().getFamily());
    assertNull(snomed.getRootTerminology().getHierarchicalName());
    assertNull(snomed.getRootTerminology().getLanguage());
    assertNull(snomed.getRootTerminology().getPreferredName());
    assertNull(snomed.getRootTerminology().getShortName());
    assertNull(snomed.getRootTerminology().getAcquisitionContact());
    assertNull(snomed.getRootTerminology().getContentContact());
    assertNull(snomed.getRootTerminology().getLicenseContact());

    // test MSH metadata
    Terminology msh =
        metadataService.getTerminology("MSH", "2015_2014_09_08", authToken);
    assertEquals("loader", msh.getLastModifiedBy());
    assertFalse(msh.isAssertsRelDirection());
    assertTrue(msh.isCurrent());
    assertFalse(msh.isDescriptionLogicTerminology());
    assertNull(msh.getEndDate());
    assertNull(msh.getStartDate());
    assertEquals(IdType.DESCRIPTOR, msh.getOrganizingClassType());
    assertEquals("Medical Subject Headings, 2015_2014_09_08",
        msh.getPreferredName());
    assertEquals("MSH", msh.getTerminology());
    assertEquals("2015_2014_09_08", msh.getVersion());

    assertEquals("MSH", msh.getRootTerminology().getTerminology());
    // Because of XML Transient
    assertNull(msh.getRootTerminology().getLastModifiedBy());
    assertNull(msh.getRootTerminology().getFamily());
    assertNull(msh.getRootTerminology().getHierarchicalName());
    assertNull(msh.getRootTerminology().getLanguage());
    assertNull(msh.getRootTerminology().getPreferredName());
    assertNull(msh.getRootTerminology().getShortName());
    assertNull(msh.getRootTerminology().getAcquisitionContact());
    assertNull(msh.getRootTerminology().getContentContact());
    assertNull(msh.getRootTerminology().getLicenseContact());

  }

  /**
   * Test normal use rest metadata005.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestMetadata005() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

    // test precedence list
    PrecedenceList precedence =
        metadataService.getDefaultPrecedenceList("UMLS", "latest", authToken);
    assertEquals("loader", precedence.getLastModifiedBy());
    assertEquals("UMLS", precedence.getTerminology());
    assertEquals("latest", precedence.getVersion());
    assertEquals("MTH", precedence.getPrecedence().getKeyValuePairs().get(0)
        .getKey());
    assertEquals("PN", precedence.getPrecedence().getKeyValuePairs().get(0)
        .getValue());
    assertEquals("MSH", precedence.getPrecedence().getKeyValuePairs().get(1)
        .getKey());
    assertEquals("MH", precedence.getPrecedence().getKeyValuePairs().get(1)
        .getValue());
    assertEquals("DEFAULT", precedence.getName());

    precedence =
        metadataService.getDefaultPrecedenceList("MSH", "2015_2014_09_08",
            authToken);
    // assertEquals("loader", precedence.getLastModifiedBy());
    assertEquals("UMLS", precedence.getTerminology());
    assertEquals("latest", precedence.getVersion());
    assertEquals("MSH", precedence.getPrecedence().getKeyValuePairs().get(0)
        .getKey());
    assertEquals("MH", precedence.getPrecedence().getKeyValuePairs().get(0)
        .getValue());
    assertEquals("MSH", precedence.getPrecedence().getKeyValuePairs().get(1)
        .getKey());
    assertEquals("TQ", precedence.getPrecedence().getKeyValuePairs().get(1)
        .getValue());

    assertEquals("DEFAULT", precedence.getName());

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
    expectedSizes.put(MetadataKeys.Attribute_Names, 98);
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

    // Languages
    expectedSizes.put(MetadataKeys.Languages, 21);
    expectedSizes2.put(MetadataKeys.Languages, 21);
    expectedIds.put(MetadataKeys.Languages, "ENG");
    expectedNames.put(MetadataKeys.Languages, new HashSet<String>());
    expectedNames.get(MetadataKeys.Languages).add("English");

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
    expectedIds.put(MetadataKeys.Additional_Relationship_Types,
        "has_temporal_context");
    expectedNames.put(MetadataKeys.Additional_Relationship_Types,
        new HashSet<String>());
    expectedNames.get(MetadataKeys.Additional_Relationship_Types).add(
        "Has temporal context");

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
    // expectedNames.get(MetadataKeys.Semantic_Types).add("Clinical Drug");

    // Term types
    expectedSizes.put(MetadataKeys.Term_Types, 18);
    expectedSizes2.put(MetadataKeys.Term_Types, 18);
    expectedIds.put(MetadataKeys.Term_Types, "PT");
    expectedNames.put(MetadataKeys.Term_Types, new HashSet<String>());
    expectedNames.get(MetadataKeys.Term_Types).add("Designated preferred name");

    // Languages
    expectedSizes.put(MetadataKeys.Languages, 1);
    expectedSizes2.put(MetadataKeys.Languages, 1);
    expectedIds.put(MetadataKeys.Languages, "ENG");
    expectedNames.put(MetadataKeys.Languages, new HashSet<String>());
    expectedNames.get(MetadataKeys.Languages).add("English");

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
        "Testing MSH metadata retrieval, " + keyValuePairLists.getCount()
            + " pair lists found (" + MetadataKeys.values().length
            + " expected)");

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
        "has parent relationship in a Metathesaurus source vocabulary");

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
    expectedIds.put(MetadataKeys.Attribute_Names, "TERMUI");
    expectedNames.put(MetadataKeys.Attribute_Names, new HashSet<String>());
    expectedNames.get(MetadataKeys.Attribute_Names).add(
        "Term unique identifier");

    // Semantic types
    expectedSizes.put(MetadataKeys.Semantic_Types, 0);
    expectedSizes2.put(MetadataKeys.Semantic_Types, 0);
    expectedIds.put(MetadataKeys.Semantic_Types, "clnd");
    expectedNames.put(MetadataKeys.Semantic_Types, new HashSet<String>());
    // expectedNames.get(MetadataKeys.Semantic_Types).add("Clinical Drug");

    // Term types
    expectedSizes.put(MetadataKeys.Term_Types, 19);
    expectedSizes2.put(MetadataKeys.Term_Types, 19);
    expectedIds.put(MetadataKeys.Term_Types, "MH");
    expectedNames.put(MetadataKeys.Term_Types, new HashSet<String>());
    expectedNames.get(MetadataKeys.Term_Types).add("Main heading");

    // Languages
    expectedSizes.put(MetadataKeys.Languages, 1);
    expectedSizes2.put(MetadataKeys.Languages, 1);
    expectedIds.put(MetadataKeys.Languages, "ENG");
    expectedNames.put(MetadataKeys.Languages, new HashSet<String>());
    expectedNames.get(MetadataKeys.Languages).add("English");

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

    // cycle over all retrieved metadata
    for (KeyValuePairList keyValuePairList : keyValuePairLists
        .getKeyValuePairLists()) {

      Logger.getLogger(getClass()).info(
          "Checking " + keyValuePairList.getKeyValuePairs().size() + " "
              + keyValuePairList.getName());

      int expectedSize =
          expectedSizes.get(MetadataKeys.valueOf(keyValuePairList.getName()));
      int expectedSize2 =
          expectedSizes2.get(MetadataKeys.valueOf(keyValuePairList.getName()));
      String expectedId =
          expectedIds.get(MetadataKeys.valueOf(keyValuePairList.getName()));
      Set<String> expectedNames =
          expectedNameSets
              .get(MetadataKeys.valueOf(keyValuePairList.getName()));
      List<KeyValuePair> pairs = keyValuePairList.getKeyValuePairs();

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
        "Metadata Categories Validated:  " + categorySuccessCt + " out of "
            + MetadataKeys.values().length);

    return categorySuccessCt == MetadataKeys.values().length;
  }

}
