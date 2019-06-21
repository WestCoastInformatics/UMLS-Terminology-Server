/*
 * Copyright 2016 West Coast Informatics, LLC
 */
/*
 * 
 */
package com.wci.umls.server.test.rest;

import static org.junit.Assert.fail;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.wci.umls.server.helpers.content.SubsetList;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Subset;

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

  /** The snomed terminology. */
  private String snomedTerminology = "SNOMEDCT_US";

  /** The snomed version. */
  private String snomedVersion = "2016_03_01";

  /** The msh terminology. */
  private String mshTerminology = "MSH";

  /** The msh version. */
  private String mshVersion = "2016_2016_02_26";

  /** The umls terminology. */
  private String umlsTerminology = "MTH";

  /** The umls version. */
  private String umlsVersion = "latest";

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
    authToken =
        securityService.authenticate(testUser, testPassword).getAuthToken();

    // set terminology and version
    testTerminology = "SNOMEDCT_US";
    testVersion = "2016_03_01";
    testId = "102466009";

    // get test concept
    concept =
        contentService.getConcept(testId, testTerminology, testVersion, null,
            authToken);

  }

  /**
   * Test "get" methods for concepts.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetConcept() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Test with null terminologyId
    Logger.getLogger(getClass()).info(
        "TEST null terminologyId - " + "null, MSH, 2016_2016_02_26, "
            + authToken);
    try {
      contentService.getConcept(null, mshTerminology, mshVersion, null,
          authToken);
      fail("Exception should be thrown when trying to get a concept with null terminologyId.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with empty string terminologyId
    Logger.getLogger(getClass()).info(
        "TEST empty terminologyId - " + ", UMLS, latest, " + authToken);
    try {
      contentService.getConcept("", umlsTerminology, umlsVersion, null,
          authToken);
      fail("Exception should be thrown when trying to get a concept with empty string terminologyId.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with null terminology
    Logger.getLogger(getClass()).info(
        "TEST null terminology - " + "M0028634, null, 2016_2016_02_26, "
            + authToken);
    try {
      contentService.getConcept("M0028634", null, mshVersion, null, authToken);
      fail("Exception should be thrown when trying to get a concept with null terminology.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with empty string terminology
    Logger.getLogger(getClass()).info(
        "TEST empty terminology - " + "M0028634, , 2016_2016_02_26, "
            + authToken);
    try {
      contentService.getConcept("M0028634", "", mshVersion, null, authToken);
      fail("Exception should be thrown when trying to get a concept with emtpy string terminology.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with null version
    Logger.getLogger(getClass()).info(
        "TEST null version - " + "M0028634, MSH, null, " + authToken);
    try {
      contentService.getConcept("M0028634", mshTerminology, null, null,
          authToken);
      fail("Exception should be thrown when trying to get a concept with null version.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with empty string version
    Logger.getLogger(getClass()).info(
        "TEST empty version - " + "M0028634, MSH, , " + authToken);
    try {
      contentService.getConcept("M0028634", "MSH", "", null, authToken);
      fail("Exception should be thrown when trying to get a concept with empty string version.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with null authToken
    Logger.getLogger(getClass()).info(
        "TEST null authToken - " + "M0028634, MSH, 2016_2016_02_26, ");
    try {
      contentService.getConcept("M0028634", "MSH", "2016_2016_02_26", null,
          null);
      fail("Exception should be thrown when trying to get a concept with null authToken.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with invalid authToken
    Logger.getLogger(getClass()).info(
        "TEST invalid authToken - " + "M0028634, MSH, 2016_2016_02_26, TTT");
    try {
      contentService.getConcept("M0028634", "MSH", "2016_2016_02_26", null,
          "TTT");
      fail("Exception should be thrown when trying to get a concept with invalid authToken.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with empty string authToken
    Logger.getLogger(getClass()).info(
        "TEST empty authToken - " + "M0028634, MSH, 2016_2016_02_26, TTT");
    try {
      contentService.getConcept("M0028634", "MSH", "2016_2016_02_26", null, "");
      fail("Exception should be thrown when trying to get a concept with empty string authToken.");
    } catch (Exception e) {
      // do nothing
    }
  }

  /**
   * Test "get" methods for descriptors.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetDescriptor() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Test MSH descriptor
    Logger.getLogger(getClass()).info(
        "TEST null terminologyId - " + "null, MSH, 2016_2016_02_26, "
            + authToken);
    try {
      contentService.getDescriptor(null, mshTerminology, mshVersion, null,
          authToken);
      fail("Exception should be thrown when trying to get a descriptor with null terminologyId.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with null terminology
    Logger.getLogger(getClass()).info(
        "TEST null terminology - " + "M0028634, null, 2016_2016_02_26, "
            + authToken);
    try {
      contentService.getDescriptor("M0028634", null, mshVersion, null,
          authToken);
      fail("Exception should be thrown when trying to get a descriptor with null terminology.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with empty string terminology
    Logger.getLogger(getClass()).info(
        "TEST empty terminology - " + "M0028634, , 2016_2016_02_26, "
            + authToken);
    try {
      contentService.getDescriptor("M0028634", "", mshVersion, null, authToken);
      fail("Exception should be thrown when trying to get a descriptor with emtpy string terminology.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with null version
    Logger.getLogger(getClass()).info(
        "TEST null version - " + "M0028634, MSH, null, " + authToken);
    try {
      contentService.getDescriptor("M0028634", mshTerminology, null, null,
          authToken);
      fail("Exception should be thrown when trying to get a descriptor with null version.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with empty string version
    Logger.getLogger(getClass()).info(
        "TEST empty version - " + "M0028634, MSH, , " + authToken);
    try {
      contentService.getDescriptor("M0028634", "MSH", "", null, authToken);
      fail("Exception should be thrown when trying to get a descriptor with empty string version.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with null authToken
    Logger.getLogger(getClass()).info(
        "TEST null authToken - " + "M0028634, MSH, 2016_2016_02_26, ");
    try {
      contentService.getDescriptor("M0028634", "MSH", "2016_2016_02_26", null,
          null);
      fail("Exception should be thrown when trying to get a descriptor with null authToken.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with invalid authToken
    Logger.getLogger(getClass()).info(
        "TEST invalid authToken - " + "M0028634, MSH, 2016_2016_02_26, TTT");
    try {
      contentService.getDescriptor("M0028634", "MSH", "2016_2016_02_26", null,
          "TTT");
      fail("Exception should be thrown when trying to get a descriptor with invalid authToken.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with empty string authToken
    Logger.getLogger(getClass()).info(
        "TEST empty authToken - " + "M0028634, MSH, 2016_2016_02_26, TTT");
    try {
      contentService.getDescriptor("M0028634", "MSH", "2016_2016_02_26", null,
          "");
      fail("Exception should be thrown when trying to get a descriptor with empty string authToken.");
    } catch (Exception e) {
      // do nothing
    }
  }

  /**
   * Test "get" methods for codes.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetCode() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Test null MSH code
    Logger.getLogger(getClass()).info(
        "TEST null terminologyId - " + "null, MSH, 2016_2016_02_26, "
            + authToken);
    try {
      contentService.getCode(null, mshTerminology, mshVersion, null, authToken);
      fail("Exception should be thrown when trying to get a code with null terminologyId.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with null terminology
    Logger.getLogger(getClass()).info(
        "TEST null terminology - " + "M0028634, null, 2016_2016_02_26, "
            + authToken);
    try {
      contentService.getCode("M0028634", null, mshVersion, null, authToken);
      fail("Exception should be thrown when trying to get a code with null terminology.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with empty string terminology
    Logger.getLogger(getClass()).info(
        "TEST empty terminology - " + "M0028634, , 2016_2016_02_26, "
            + authToken);
    try {
      contentService.getCode("M0028634", "", mshVersion, null, authToken);
      fail("Exception should be thrown when trying to get a code with emtpy string terminology.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with null version
    Logger.getLogger(getClass()).info(
        "TEST null version - " + "M0028634, MSH, null, " + authToken);
    try {
      contentService.getCode("M0028634", mshTerminology, null, null, authToken);
      fail("Exception should be thrown when trying to get a code with null version.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with empty string version
    Logger.getLogger(getClass()).info(
        "TEST empty version - " + "M0028634, MSH, , " + authToken);
    try {
      contentService.getCode("M0028634", "MSH", "", null, authToken);
      fail("Exception should be thrown when trying to get a code with empty string version.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with null authToken
    Logger.getLogger(getClass()).info(
        "TEST null authToken - " + "M0028634, MSH, 2016_2016_02_26, ");
    try {
      contentService.getCode("M0028634", "MSH", "2016_2016_02_26", null, null);
      fail("Exception should be thrown when trying to get a code with null authToken.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with invalid authToken
    Logger.getLogger(getClass()).info(
        "TEST invalid authToken - " + "M0028634, MSH, 2016_2016_02_26, TTT");
    try {
      contentService.getCode("M0028634", "MSH", "2016_2016_02_26", null, "TTT");
      fail("Exception should be thrown when trying to get a code with invalid authToken.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with empty string authToken
    Logger.getLogger(getClass()).info(
        "TEST empty authToken - " + "M0028634, MSH, 2016_2016_02_26, TTT");
    try {
      contentService.getCode("M0028634", "MSH", "2016_2016_02_26", null, "");
      fail("Exception should be thrown when trying to get a code with empty string authToken.");
    } catch (Exception e) {
      // do nothing
    }
  }

  /**
   * Test "get" method for lexical classes.
   * @throws Exception
   */
  @Test
  public void testGetLexicalClass() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    // n/a - no data in sample
  }

  /**
   * Test "get" method for string classes.
   * @throws Exception
   */
  @Test
  public void testGetStringClass() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    // n/a - no data in sample
  }

  /**
   * Test "get" methods for atom subsets
   * @throws Exception
   */
  @Test
  public void testGetAtomSubsets() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Test terminology is null
    Logger.getLogger(getClass()).info(
        "TEST null terminology - " + "null, 2016_03_01, " + authToken);
    try {
      contentService.getAtomSubsets(null, snomedVersion, authToken);
      fail("Exception should be thrown when trying to get an atom subset with null terminology.");
    } catch (Exception e) {
      // do nothing
    }

    // Test terminology is empty string - exception
    Logger.getLogger(getClass()).info(
        "TEST empty terminology - " + ", 2016_03_01, " + authToken);
    try {
      contentService.getAtomSubsets("", snomedVersion, authToken);
      fail("Exception should be thrown when trying to get an atom subset with empty string terminology.");
    } catch (Exception e) {
      // do nothing
    }

    // Test version is null - exception
    Logger.getLogger(getClass()).info(
        "TEST null version - " + "SNOMEDCT_US, null, " + authToken);
    try {
      contentService.getAtomSubsets(snomedTerminology, null, authToken);
      fail("Exception should be thrown when trying to get an atom subset with null terminology.");
    } catch (Exception e) {
      // do nothing
    }

    // Test version is empty string - exception
    Logger.getLogger(getClass()).info(
        "TEST empty version- " + "SNOMEDCT_US, , " + authToken);
    try {
      contentService.getAtomSubsets(snomedTerminology, "", authToken);
      fail("Exception should be thrown when trying to get an atom subset with empty string terminology.");
    } catch (Exception e) {
      // do nothing
    }

    SubsetList list =
        contentService.getAtomSubsets(snomedTerminology, snomedVersion,
            authToken);
    Subset subset = list.getObjects().get(0);

    // Test terminology is null
    Logger.getLogger(getClass()).info(
        "TEST null terminology - " + "null, 2016_03_01, " + authToken);
    try {
      contentService.findAtomSubsetMembers(subset.getTerminologyId(), null,
          snomedVersion, null, new PfsParameterJpa(), authToken);
      fail("Exception should be thrown when trying to get an atom subset with null terminology.");
    } catch (Exception e) {
      // do nothing
    }

    // Test terminology is empty string - exception
    Logger.getLogger(getClass()).info(
        "TEST empty terminology - " + ", 2016_03_01, " + authToken);
    try {
      contentService.findAtomSubsetMembers(subset.getTerminologyId(), "",
          snomedVersion, null, new PfsParameterJpa(), authToken);
      fail("Exception should be thrown when trying to get an atom subset with empty string terminology.");
    } catch (Exception e) {
      // do nothing
    }

    // Test version is null - exception
    Logger.getLogger(getClass()).info(
        "TEST null version - " + "SNOMEDCT_US, null, " + authToken);
    try {
      contentService.findAtomSubsetMembers(subset.getTerminologyId(),
          snomedTerminology, null, null, new PfsParameterJpa(), authToken);
      fail("Exception should be thrown when trying to get an atom subset with null terminology.");
    } catch (Exception e) {
      // do nothing
    }

    // Test version is empty string - exception
    Logger.getLogger(getClass()).info(
        "TEST empty version - " + "SNOMEDCT_US, , " + authToken);
    try {
      contentService.findAtomSubsetMembers(subset.getTerminologyId(),
          snomedTerminology, "", null, new PfsParameterJpa(), authToken);
      fail("Exception should be thrown when trying to get an atom subset with empty string terminology.");
    } catch (Exception e) {
      // do nothing
    }
  }

  /**
   * Test "get" methods for concept subsets
   * @throws Exception
   */
  @Test
  public void testGetConceptSubsets() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Test terminology is null - exception
    Logger.getLogger(getClass()).info(
        "TEST null terminology - " + "null, 2016_03_01, " + authToken);
    try {
      contentService.getConceptSubsets(null, snomedVersion, authToken);
      fail("Exception should be thrown when trying to get an concept subset with null terminology.");
    } catch (Exception e) {
      // do nothing
    }

    // Test terminology is empty string - exception
    Logger.getLogger(getClass()).info(
        "TEST empty terminology - " + ", 2016_03_01, " + authToken);
    try {
      contentService.getConceptSubsets("", snomedVersion, authToken);
      fail("Exception should be thrown when trying to get an concept subset with empty string terminology.");
    } catch (Exception e) {
      // do nothing
    }

    // Test version is null - exception
    Logger.getLogger(getClass()).info(
        "TEST null version - " + "SNOMEDCT_US, null, " + authToken);
    try {
      contentService.getConceptSubsets(snomedTerminology, null, authToken);
      fail("Exception should be thrown when trying to get an concept subset with null terminology.");
    } catch (Exception e) {
      // do nothing
    }

    // Test version is empty string - exception
    Logger.getLogger(getClass()).info(
        "TEST empty version - " + "SNOMEDCT_US, , " + authToken);
    try {
      contentService.getConceptSubsets(snomedTerminology, "", authToken);
      fail("Exception should be thrown when trying to get an concept subset with empty string terminology.");
    } catch (Exception e) {
      // do nothing
    }

    SubsetList list =
        contentService.getConceptSubsets(snomedTerminology, snomedVersion,
            authToken);
    Subset subset = list.getObjects().get(0);

    // Test terminology is null
    Logger.getLogger(getClass()).info(
        "TEST null terminology - " + "null, 2016_03_01, " + authToken);
    try {
      contentService.findConceptSubsetMembers(subset.getTerminologyId(), null,
          snomedVersion, null, new PfsParameterJpa(), authToken);
      fail("Exception should be thrown when trying to get an concept subset with null terminology.");
    } catch (Exception e) {
      // do nothing
    }

    // Test terminology is empty string - exception
    Logger.getLogger(getClass()).info(
        "TEST empty terminology - " + ", 2016_03_01, " + authToken);
    try {
      contentService.findConceptSubsetMembers(subset.getTerminologyId(), "",
          snomedVersion, null, new PfsParameterJpa(), authToken);
      fail("Exception should be thrown when trying to get an concept subset with empty string terminology.");
    } catch (Exception e) {
      // do nothing
    }

    // Test version is null - exception
    Logger.getLogger(getClass()).info(
        "TEST null version - " + "SNOMEDCT_US, null, " + authToken);
    try {
      contentService.findConceptSubsetMembers(subset.getTerminologyId(),
          snomedTerminology, null, null, new PfsParameterJpa(), authToken);
      fail("Exception should be thrown when trying to get an concept subset with null terminology.");
    } catch (Exception e) {
      // do nothing
    }

    // Test version is empty string - exception
    Logger.getLogger(getClass()).info(
        "TEST empty version - " + "SNOMEDCT_US, , " + authToken);
    try {
      contentService.findConceptSubsetMembers(subset.getTerminologyId(),
          snomedTerminology, "", null, new PfsParameterJpa(), authToken);
      fail("Exception should be thrown when trying to get an concept subset with empty string terminology.");
    } catch (Exception e) {
      // do nothing
    }
  }

  /**
   * Test find concepts for query.
   * @throws Exception
   */
  @Test
  public void testFindConcepts() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Test terminology is null
    Logger.getLogger(getClass()).info("TEST null terminology - ");
    try {
      contentService.findConcepts(null, snomedVersion, "care",
          new PfsParameterJpa(), authToken);
      fail("Exception should be thrown when trying to find concepts for query with null terminology.");
    } catch (Exception e) {
      // do nothing
    }

    // Test terminology is empty string - exception
    Logger.getLogger(getClass()).info("TEST empty terminology - ");
    try {
      contentService.findConcepts("", snomedVersion, "care",
          new PfsParameterJpa(), authToken);
      fail("Exception should be thrown when trying to find concepts for query with empty string terminology.");
    } catch (Exception e) {
      // do nothing
    }

    // Test version is null - exception
    Logger.getLogger(getClass()).info("TEST null version - ");
    try {
      contentService.findConcepts(snomedTerminology, null, "care",
          new PfsParameterJpa(), authToken);
      fail("Exception should be thrown when trying to find concepts for query with null terminology.");
    } catch (Exception e) {
      // do nothing
    }

    // Test version is empty string - exception
    Logger.getLogger(getClass()).info("TEST empty version - ");
    try {
      contentService.findConcepts(snomedTerminology, "", "care",
          new PfsParameterJpa(), authToken);
      fail("Exception should be thrown when trying to find concepts for query with empty string terminology.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with null authToken
    Logger.getLogger(getClass()).info("TEST null authToken - ");
    try {
      contentService.findConcepts(snomedTerminology, snomedVersion, "care",
          new PfsParameterJpa(), null);
      fail("Exception should be thrown when trying to find concepts for query with null authToken.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with invalid authToken
    Logger.getLogger(getClass()).info("TEST invalid authToken - ");
    try {
      contentService.findConcepts(snomedTerminology, snomedVersion, "care",
          new PfsParameterJpa(), "TTT");
      fail("Exception should be thrown when trying to find concepts for query with invalid authToken.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with empty string authToken
    Logger.getLogger(getClass()).info("TEST empty authToken - ");
    try {
      contentService.findConcepts(snomedTerminology, snomedVersion, "care",
          new PfsParameterJpa(), "");
      fail("Exception should be thrown when trying to find concepts for query with empty string authToken.");
    } catch (Exception e) {
      // do nothing
    }
  }

  /**
   * Test find descriptors for query.
   * @throws Exception
   */
  @Test
  public void testFindDescriptors() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Test terminology is null
    Logger.getLogger(getClass()).info("TEST null terminology - ");
    try {
      contentService.findDescriptors(null, snomedVersion, "care",
          new PfsParameterJpa(), authToken);
      fail("Exception should be thrown when trying to find descriptors for query with null terminology.");
    } catch (Exception e) {
      // do nothing
    }

    // Test terminology is empty string - exception
    Logger.getLogger(getClass()).info("TEST empty terminology - ");
    try {
      contentService.findDescriptors("", snomedVersion, "care",
          new PfsParameterJpa(), authToken);
      fail("Exception should be thrown when trying to find descriptors for query with empty string terminology.");
    } catch (Exception e) {
      // do nothing
    }

    // Test version is null - exception
    Logger.getLogger(getClass()).info("TEST null version - ");
    try {
      contentService.findDescriptors(snomedTerminology, null, "care",
          new PfsParameterJpa(), authToken);
      fail("Exception should be thrown when trying to find descriptors for query with null terminology.");
    } catch (Exception e) {
      // do nothing
    }

    // Test version is empty string - exception
    Logger.getLogger(getClass()).info("TEST empty version - ");
    try {
      contentService.findDescriptors(snomedTerminology, "", "care",
          new PfsParameterJpa(), authToken);
      fail("Exception should be thrown when trying to find descriptors for query with empty string terminology.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with null authToken
    Logger.getLogger(getClass()).info("TEST null authToken - ");
    try {
      contentService.findDescriptors(snomedTerminology, snomedVersion, "care",
          new PfsParameterJpa(), null);
      fail("Exception should be thrown when trying to find descriptors for query with null authToken.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with invalid authToken
    Logger.getLogger(getClass()).info("TEST invalid authToken - ");
    try {
      contentService.findDescriptors(snomedTerminology, snomedVersion, "care",
          new PfsParameterJpa(), "TTT");
      fail("Exception should be thrown when trying to find descriptors for query with invalid authToken.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with empty string authToken
    Logger.getLogger(getClass()).info("TEST empty authToken - ");
    try {
      contentService.findDescriptors(snomedTerminology, snomedVersion, "care",
          new PfsParameterJpa(), "");
      fail("Exception should be thrown when trying to find descriptors for query with empty string authToken.");
    } catch (Exception e) {
      // do nothing
    }
  }

  /**
   * Test find codes for query.
   * @throws Exception
   */
  @Test
  public void testFindCodes() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Test terminology is null
    Logger.getLogger(getClass()).info("TEST null terminology - ");
    try {
      contentService.findCodes(null, snomedVersion, "care",
          new PfsParameterJpa(), authToken);
      fail("Exception should be thrown when trying to find codes for query with null terminology.");
    } catch (Exception e) {
      // do nothing
    }

    // Test terminology is empty string
    Logger.getLogger(getClass()).info("TEST empty terminology - ");
    try {
      contentService.findCodes("", snomedVersion, "care",
          new PfsParameterJpa(), authToken);
      fail("Exception should be thrown when trying to find codes for query with empty string terminology.");
    } catch (Exception e) {
      // do nothing
    }

    // Test version is null
    Logger.getLogger(getClass()).info("TEST  null version - ");
    try {
      contentService.findCodes(snomedTerminology, null, "care",
          new PfsParameterJpa(), authToken);
      fail("Exception should be thrown when trying to find codes for query with null terminology.");
    } catch (Exception e) {
      // do nothing
    }

    // Test version is empty string - exception
    Logger.getLogger(getClass()).info("TEST empty version - ");
    try {
      contentService.findCodes(snomedTerminology, "", "care",
          new PfsParameterJpa(), authToken);
      fail("Exception should be thrown when trying to find codes for query with empty string terminology.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with null authToken
    Logger.getLogger(getClass()).info("TEST null authToken - ");
    try {
      contentService.findCodes(snomedTerminology, snomedVersion, "care",
          new PfsParameterJpa(), null);
      fail("Exception should be thrown when trying to find codes for query with null authToken.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with invalid authToken
    Logger.getLogger(getClass()).info("TEST invalid authToken - ");
    try {
      contentService.findCodes(snomedTerminology, snomedVersion, "care",
          new PfsParameterJpa(), "TTT");
      fail("Exception should be thrown when trying to find codes for query with invalid authToken.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with empty string authToken
    Logger.getLogger(getClass()).info("TEST empty authToken - ");
    try {
      contentService.findCodes(snomedTerminology, snomedVersion, "care",
          new PfsParameterJpa(), "");
      fail("Exception should be thrown when trying to find codes for query with empty string authToken.");
    } catch (Exception e) {
      // do nothing
    }
  }

  /**
   * Test find descendant concepts.
   * @throws Exception
   */
  @Test
  public void testFindDescendantConcepts() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Test terminology is null
    Logger.getLogger(getClass()).info("TEST null terminology - ");
    try {
      contentService.findDescendantConcepts("105590001", null, snomedVersion,
          false, new PfsParameterJpa(), authToken);
      fail("Exception should be thrown when trying to find descendant concepts with null terminology.");
    } catch (Exception e) {
      // do nothing
    }

    // Test terminology is empty string
    Logger.getLogger(getClass()).info("TEST empty terminology - ");
    try {
      contentService.findDescendantConcepts("105590001", "", snomedVersion,
          false, new PfsParameterJpa(), authToken);
      fail("Exception should be thrown when trying to find descendant concepts with empty string terminology.");
    } catch (Exception e) {
      // do nothing
    }

    // Test version is null
    Logger.getLogger(getClass()).info("TEST null version - ");
    try {
      contentService.findDescendantConcepts("105590001", snomedTerminology,
          null, false, new PfsParameterJpa(), authToken);
      fail("Exception should be thrown when trying to find descendant concepts with null terminology.");
    } catch (Exception e) {
      // do nothing
    }

    // Test version is empty string
    Logger.getLogger(getClass()).info("TEST empty version - ");
    try {
      contentService.findDescendantConcepts("105590001", snomedTerminology, "",
          false, new PfsParameterJpa(), authToken);
      fail("Exception should be thrown when trying to find descendant concepts with empty string terminology.");
    } catch (Exception e) {
      // do nothing
    }

    // Test self id is null
    Logger.getLogger(getClass()).info("TEST null selfId - ");
    try {
      contentService.findDescendantConcepts(null, snomedTerminology,
          snomedVersion, false, new PfsParameterJpa(), authToken);
      fail("Exception should be thrown when trying to find descendant concepts with null terminology id.");
    } catch (Exception e) {
      // do nothing
    }

    // Test self id is empty string
    Logger.getLogger(getClass()).info("TEST empty selfId - ");
    try {
      contentService.findDescendantConcepts("", snomedTerminology,
          snomedVersion, false, new PfsParameterJpa(), authToken);
      fail("Exception should be thrown when trying to find descendant concepts with empty string terminology id.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with null authToken
    Logger.getLogger(getClass()).info("TEST null authToken - ");
    try {
      contentService.findDescendantConcepts("105590001", snomedTerminology,
          snomedVersion, false, new PfsParameterJpa(), null);
      fail("Exception should be thrown when trying to find descendant concepts with null authToken.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with invalid authToken
    Logger.getLogger(getClass()).info("TEST invalid authToken - ");
    try {
      contentService.findDescendantConcepts("105590001", snomedTerminology,
          null, false, new PfsParameterJpa(), "TTT");
      fail("Exception should be thrown when trying to find descendant concepts with invalid authToken.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with empty string authToken
    Logger.getLogger(getClass()).info("TEST empty authToken - ");
    try {
      contentService.findDescendantConcepts("105590001", snomedTerminology,
          null, false, new PfsParameterJpa(), "");
      fail("Exception should be thrown when trying to find descendant concepts with empty string authToken.");
    } catch (Exception e) {
      // do nothing
    }

    // Test terminology is null
    Logger.getLogger(getClass()).info("TEST null terminology - ");
    try {
      contentService.findAncestorConcepts("105590001", null, snomedVersion,
          false, new PfsParameterJpa(), authToken);
      fail("Exception should be thrown when trying to find ancestor concepts with null terminology.");
    } catch (Exception e) {
      // do nothing
    }

    // Test terminology is empty string
    Logger.getLogger(getClass()).info("TEST empty terminology - ");
    try {
      contentService.findAncestorConcepts("105590001", "", snomedVersion,
          false, new PfsParameterJpa(), authToken);
      fail("Exception should be thrown when trying to find ancestor concepts with empty string terminology.");
    } catch (Exception e) {
      // do nothing
    }

    // Test version is null
    Logger.getLogger(getClass()).info("TEST null version - ");
    try {
      contentService.findAncestorConcepts("105590001", snomedTerminology, null,
          false, new PfsParameterJpa(), authToken);
      fail("Exception should be thrown when trying to find ancestor concepts with null terminology.");
    } catch (Exception e) {
      // do nothing
    }

    // Test version is empty string
    Logger.getLogger(getClass()).info("TEST empty version - ");
    try {
      contentService.findAncestorConcepts("105590001", snomedTerminology, "",
          false, new PfsParameterJpa(), authToken);
      fail("Exception should be thrown when trying to find ancestor concepts with empty string terminology.");
    } catch (Exception e) {
      // do nothing
    }

    // Test self id is null
    Logger.getLogger(getClass()).info("TEST null selfId - ");
    try {
      contentService.findAncestorConcepts(null, snomedTerminology,
          snomedVersion, false, new PfsParameterJpa(), authToken);
      fail("Exception should be thrown when trying to find ancestor concepts with null terminology id.");
    } catch (Exception e) {
      // do nothing
    }

    // Test self id is empty string
    Logger.getLogger(getClass()).info("TEST empty selfId - ");
    try {
      contentService.findAncestorConcepts("", snomedTerminology, snomedVersion,
          false, new PfsParameterJpa(), authToken);
      fail("Exception should be thrown when trying to find ancestor concepts with empty string terminology id.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with null authToken
    Logger.getLogger(getClass()).info("TEST null authToken - ");
    try {
      contentService.findAncestorConcepts("105590001", snomedTerminology,
          snomedVersion, false, new PfsParameterJpa(), null);
      fail("Exception should be thrown when trying to find ancestor concepts with null authToken.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with invalid authToken
    Logger.getLogger(getClass()).info("TEST invalid authToken - ");
    try {
      contentService.findAncestorConcepts("105590001", snomedTerminology, null,
          false, new PfsParameterJpa(), "TTT");
      fail("Exception should be thrown when trying to find ancestor concepts with invalid authToken.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with empty string authToken
    Logger.getLogger(getClass()).info("TEST empty authToken - ");
    try {
      contentService.findAncestorConcepts("105590001", snomedTerminology, null,
          false, new PfsParameterJpa(), "");
      fail("Exception should be thrown when trying to find ancestor concepts with empty string authToken.");
    } catch (Exception e) {
      // do nothing
    }
  }

  /**
   * Test find descendant descriptors.
   * @throws Exception
   */
  @Test
  public void testFindDescendantDescriptors() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Test terminology is null
    Logger.getLogger(getClass()).info("TEST null terminology - ");
    try {
      contentService.findDescendantDescriptors("D000005", null, mshVersion,
          false, new PfsParameterJpa(), authToken);
      fail("Exception should be thrown when trying to find descendant descriptors with null terminology.");
    } catch (Exception e) {
      // do nothing
    }

    // Test terminology is empty string
    Logger.getLogger(getClass()).info("TEST empty terminology - ");
    try {
      contentService.findDescendantDescriptors("D000005", "", mshVersion,
          false, new PfsParameterJpa(), authToken);
      fail("Exception should be thrown when trying to find descendant descriptors with empty string terminology.");
    } catch (Exception e) {
      // do nothing
    }

    // Test version is null
    Logger.getLogger(getClass()).info("TEST null version - ");
    try {
      contentService.findDescendantDescriptors("D000005", mshTerminology, null,
          false, new PfsParameterJpa(), authToken);
      fail("Exception should be thrown when trying to find descendant descriptors with null terminology.");
    } catch (Exception e) {
      // do nothing
    }

    // Test version is empty string
    Logger.getLogger(getClass()).info("TEST empty version - ");
    try {
      contentService.findDescendantDescriptors("D000005", mshTerminology, "",
          false, new PfsParameterJpa(), authToken);
      fail("Exception should be thrown when trying to find descendant descriptors with empty string terminology.");
    } catch (Exception e) {
      // do nothing
    }

    // Test self id is null
    Logger.getLogger(getClass()).info("TEST null selfId - ");
    try {
      contentService.findDescendantDescriptors(null, mshTerminology,
          mshVersion, false, new PfsParameterJpa(), authToken);
      fail("Exception should be thrown when trying to find descendant descriptors with null terminology id.");
    } catch (Exception e) {
      // do nothing
    }

    // Test self id is empty string
    Logger.getLogger(getClass()).info("TEST empty selfId - ");
    try {
      contentService.findDescendantDescriptors("", mshTerminology, mshVersion,
          false, new PfsParameterJpa(), authToken);
      fail("Exception should be thrown when trying to find descendant descriptors with empty string terminology id.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with null authToken
    Logger.getLogger(getClass()).info("TEST null authToken - ");
    try {
      contentService.findDescendantDescriptors("D000005", mshTerminology,
          mshVersion, false, new PfsParameterJpa(), null);
      fail("Exception should be thrown when trying to find descendant descriptors with null authToken.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with invalid authToken
    Logger.getLogger(getClass()).info("TEST invalid authToken - ");
    try {
      contentService.findDescendantDescriptors("D000005", mshTerminology, null,
          false, new PfsParameterJpa(), "TTT");
      fail("Exception should be thrown when trying to find descendant descriptors with invalid authToken.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with empty string authToken
    Logger.getLogger(getClass()).info("TEST empty authToken - ");
    try {
      contentService.findDescendantDescriptors("D000005", mshTerminology, null,
          false, new PfsParameterJpa(), "");
      fail("Exception should be thrown when trying to find descendant descriptors with empty string authToken.");
    } catch (Exception e) {
      // do nothing
    }

    // Test terminology is null
    Logger.getLogger(getClass()).info("TEST null terminology - ");
    try {
      contentService.findAncestorDescriptors("D000005", null, mshVersion,
          false, new PfsParameterJpa(), authToken);
      fail("Exception should be thrown when trying to find ancestor descriptors with null terminology.");
    } catch (Exception e) {
      // do nothing
    }

    // Test terminology is empty string
    Logger.getLogger(getClass()).info("TEST empty terminology - ");
    try {
      contentService.findAncestorDescriptors("D000005", "", mshVersion, false,
          new PfsParameterJpa(), authToken);
      fail("Exception should be thrown when trying to find ancestor descriptors with empty string terminology.");
    } catch (Exception e) {
      // do nothing
    }

    // Test version is null
    Logger.getLogger(getClass()).info("TEST null version - ");
    try {
      contentService.findAncestorDescriptors("D000005", mshTerminology, null,
          false, new PfsParameterJpa(), authToken);
      fail("Exception should be thrown when trying to find ancestor descriptors with null terminology.");
    } catch (Exception e) {
      // do nothing
    }

    // Test version is empty string
    Logger.getLogger(getClass()).info("TEST empty version - ");
    try {
      contentService.findAncestorDescriptors("D000005", mshTerminology, "",
          false, new PfsParameterJpa(), authToken);
      fail("Exception should be thrown when trying to find ancestor descriptors with empty string terminology.");
    } catch (Exception e) {
      // do nothing
    }

    // Test self id is null
    Logger.getLogger(getClass()).info("TEST null selfId - ");
    try {
      contentService.findAncestorDescriptors(null, mshTerminology, mshVersion,
          false, new PfsParameterJpa(), authToken);
      fail("Exception should be thrown when trying to find ancestor descriptors with null terminology id.");
    } catch (Exception e) {
      // do nothing
    }

    // Test self id is empty string
    Logger.getLogger(getClass()).info("TEST empty selfId - ");
    try {
      contentService.findAncestorDescriptors("", mshTerminology, mshVersion,
          false, new PfsParameterJpa(), authToken);
      fail("Exception should be thrown when trying to find ancestor descriptors with empty string terminology id.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with null authToken
    Logger.getLogger(getClass()).info("TEST null authToken - ");
    try {
      contentService.findAncestorDescriptors("D000005", mshTerminology,
          mshVersion, false, new PfsParameterJpa(), null);
      fail("Exception should be thrown when trying to find ancestor descriptors with null authToken.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with invalid authToken
    Logger.getLogger(getClass()).info("TEST invalid authToken - ");
    try {
      contentService.findAncestorDescriptors("D000005", mshTerminology, null,
          false, new PfsParameterJpa(), "TTT");
      fail("Exception should be thrown when trying to find ancestor descriptors with invalid authToken.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with empty string authToken
    Logger.getLogger(getClass()).info("TEST empty authToken - ");
    try {
      contentService.findAncestorDescriptors("D000005", mshTerminology, null,
          false, new PfsParameterJpa(), "");
      fail("Exception should be thrown when trying to find ancestor descriptors with empty string authToken.");
    } catch (Exception e) {
      // do nothing
    }
  }

  /**
   * Test "get" subset members for atom or concept.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetSubsetMembers() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Test with null terminologyId
    Logger.getLogger(getClass()).info("TEST null terminologyId");
    try {
      contentService.getAtomSubsetMembers(null, snomedTerminology,
          snomedVersion, authToken);
      fail("Exception should be thrown when trying to get a concept with null terminologyId.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with empty string terminologyId
    Logger.getLogger(getClass()).info("TEST empty terminologyId ");
    try {
      contentService.getAtomSubsetMembers("", umlsTerminology, umlsVersion,
          authToken);
      fail("Exception should be thrown when trying to get a concept with empty string terminologyId.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with null terminology
    Logger.getLogger(getClass()).info("TEST null terminology");
    try {
      contentService.getAtomSubsetMembers("166113012", null, snomedVersion,
          authToken);
      fail("Exception should be thrown when trying to get a concept with null terminology.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with empty string terminology - no results
    Logger.getLogger(getClass()).info("TEST empty terminology ");
    try {
      contentService.getAtomSubsetMembers("166113012", "", snomedVersion,
          authToken);
      fail("Exception should be thrown when trying to get a concept with empty terminology.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with null version
    Logger.getLogger(getClass()).info("TEST null version");
    try {
      contentService.getAtomSubsetMembers("166113012", snomedTerminology,
          null, authToken);
      fail("Exception should be thrown when trying to get a concept with null version.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with empty string version
    Logger.getLogger(getClass()).info("TEST empty version");
    try {
      contentService.getAtomSubsetMembers("166113012", "MSH", "", authToken);
      fail("Exception should be thrown when trying to get a concept with empty string version.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with null authToken
    Logger.getLogger(getClass()).info("TEST null authToken");
    try {
      contentService.getAtomSubsetMembers("166113012", "MSH",
          "2016_2016_02_26", null);
      fail("Exception should be thrown when trying to get a concept with null authToken.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with invalid authToken
    Logger.getLogger(getClass()).info("TEST invalid authToken");
    try {
      contentService.getAtomSubsetMembers("166113012", "MSH",
          "2016_2016_02_26", "TTT");
      fail("Exception should be thrown when trying to get a concept with invalid authToken.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with empty string authToken
    Logger.getLogger(getClass()).info("TEST empty authToken ");
    try {
      contentService.getAtomSubsetMembers("166113012", "MSH",
          "2016_2016_02_26", "");
      fail("Exception should be thrown when trying to get a concept with empty string authToken.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with null terminologyId
    Logger.getLogger(getClass()).info("TEST null terminologyId");
    try {
      contentService.getConceptSubsetMembers(null, snomedTerminology,
          snomedVersion, authToken);
      fail("Exception should be thrown when trying to get a concept with null terminologyId.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with empty string terminologyId
    Logger.getLogger(getClass()).info("TEST empty terminologyId ");
    try {
      contentService.getConceptSubsetMembers("", umlsTerminology,
          umlsVersion, authToken);
      fail("Exception should be thrown when trying to get a concept with empty string terminologyId.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with null terminology
    Logger.getLogger(getClass()).info("TEST null terminology");
    try {
      contentService.getConceptSubsetMembers("10123006", null,
          snomedVersion, authToken);
      fail("Exception should be thrown when trying to get a concept with null terminology.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with empty string terminology
    Logger.getLogger(getClass()).info("TEST empty terminology ");
    try {
      contentService.getConceptSubsetMembers("10123006", "", snomedVersion,
          authToken);
      fail("Exception should be thrown when trying to get a concept with emtpy string terminology.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with null version
    Logger.getLogger(getClass()).info("TEST null version");
    try {
      contentService.getConceptSubsetMembers("10123006", snomedTerminology,
          null, authToken);
      fail("Exception should be thrown when trying to get a concept with null version.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with empty string version
    Logger.getLogger(getClass()).info("TEST empty version");
    try {
      contentService.getConceptSubsetMembers("10123006", "MSH", "",
          authToken);
      fail("Exception should be thrown when trying to get a concept with empty string version.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with null authToken
    Logger.getLogger(getClass()).info("TEST null authToken");
    try {
      contentService.getConceptSubsetMembers("10123006", "MSH",
          "2016_2016_02_26", null);
      fail("Exception should be thrown when trying to get a concept with null authToken.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with invalid authToken
    Logger.getLogger(getClass()).info("TEST invalid authToken");
    try {
      contentService.getConceptSubsetMembers("10123006", "MSH",
          "2016_2016_02_26", "TTT");
      fail("Exception should be thrown when trying to get a concept with invalid authToken.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with empty string authToken
    Logger.getLogger(getClass()).info("TEST empty authToken ");
    try {
      contentService.getConceptSubsetMembers("10123006", "MSH",
          "2016_2016_02_26", "");
      fail("Exception should be thrown when trying to get a concept with empty string authToken.");
    } catch (Exception e) {
      // do nothing
    }
  }

  /**
   * Test autocomplete for concepts.
   *
   * @throws Exception the exception
   */
  @Test
  public void testAutocompleteConcepts() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Test with empty string searchTerm
    Logger.getLogger(getClass()).info("TEST empty searchTerm ");
    try {
      contentService.autocompleteConcepts(snomedTerminology, snomedVersion, "",
          authToken);
      fail("Exception should be thrown when trying to autocomplete with empty string searchTerm.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with null searchTerm
    Logger.getLogger(getClass()).info("TEST null searchTerm ");
    try {
      contentService.autocompleteConcepts(snomedTerminology, snomedVersion,
          null, authToken);
      fail("Exception should be thrown when trying to autocomplete with null searchTerm.");
    } catch (Exception e) {
      // do nothing
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
    securityService.logout(authToken);
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
      if (parameters[i].toString().contains("oolean"))
        types[i] = boolean.class;
      else
        types[i] = parameters[i].getClass();

    }
    return types;
  }

}
