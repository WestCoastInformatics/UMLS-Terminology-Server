/*
 * Copyright 2016 West Coast Informatics, LLC
 */
/*
 * 
 */
package com.wci.umls.server.test.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.wci.umls.server.helpers.content.SubsetList;
import com.wci.umls.server.helpers.content.SubsetMemberList;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Subset;

/**
 * Implementation of the "Content Service REST Edge Cases" Test Cases.
 */
public class ContentServiceRestEdgeCasesTest extends ContentServiceRestTest {

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
  private String umlsTerminology = "UMLS";

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

    // Test with invalid terminologyId
    Logger.getLogger(getClass()).info(
        "TEST invalid terminology id - " + "-1, SNOMEDCT, 2016_03_01, "
            + authToken);
    assertNull(contentService.getConcept("-1", snomedTerminology,
        snomedVersion, null, authToken));

    // Test with invalid terminology
    Logger.getLogger(getClass()).info(
        "TEST invalid terminology - " + "M0028634, TTT, 2016_2016_02_26, "
            + authToken);
    assertNull(contentService.getConcept("M0028634", "TTT", mshVersion, null,
        authToken));

    // Test with invalid version
    Logger.getLogger(getClass()).info(
        "TEST invalid version - " + "M0028634, MSH, TTT , " + authToken);
    assertNull(contentService.getConcept("M0028634", "MSH", "TTT", null,
        authToken));

  }

  /**
   * Test "get" methods for descriptors.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetDescriptor() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Test invalid SNOMEDCT_US descriptor
    Logger.getLogger(getClass()).info(
        "TEST invalid SNOMEDCT_US descriptor - " + "-1, SNOMEDCT, 2016_03_01, "
            + authToken);

    assertNull(contentService.getDescriptor("-1", snomedTerminology,
        snomedVersion, null, authToken));

    // Test with invalid terminology
    Logger.getLogger(getClass()).info(
        "TEST invalid termionlogy - " + "M0028634, TTT, 2016_2016_02_26, "
            + authToken);
    assertNull(contentService.getDescriptor("M0028634", "TTT", mshVersion,
        null, authToken));
    // Test with invalid version
    Logger.getLogger(getClass()).info(
        "TEST invalid version - " + "M0028634, MSH, TTT , " + authToken);
    assertNull(contentService.getDescriptor("M0028634", "MSH", "TTT", null,
        authToken));

  }

  /**
   * Test "get" methods for codes.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetCode() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Test invalid MSH code
    Logger.getLogger(getClass()).info(
        "TEST invalid MSH code - " + "-1, MSH, 2016_2016_02_26, " + authToken);
    assertNull(contentService.getCode("-1", mshTerminology, mshVersion, null,
        authToken));

    // Test invalid SNOMEDCT_US code
    Logger.getLogger(getClass()).info(
        "TEST invalid SNOMEDCT_US code - " + "ABC, SNOMEDCT, 2016_03_01, "
            + authToken);
    assertNull(contentService.getCode("ABC", snomedTerminology, snomedVersion,
        null, authToken));

    // Test invalid UMLS code
    Logger.getLogger(getClass()).info(
        "TEST invalid UMLS code - " + "ABC, UMLS, latest, " + authToken);
    assertNull(contentService.getCode("ABC", umlsTerminology, umlsVersion,
        null, authToken));

    // Test with invalid version
    Logger.getLogger(getClass()).info(
        "TEST invalid version - " + "M0028634, MSH, TTT , " + authToken);
    assertNull(contentService
        .getCode("M0028634", "MSH", "TTT", null, authToken));

  }

  /**
   * Test "get" method for lexical classes.
   * @throws Exception
   */
  @Test
  public void testGetLexicalClass() throws Exception {
    // n/a - no data in sample
  }

  /**
   * Test "get" method for string classes.
   * @throws Exception
   */
  @Test
  public void testGetStringClass() throws Exception {
    // n/a - no data in sample
  }

  /**
   * Test "get" methods for atom subsets
   * @throws Exception
   */
  @Test
  public void testGetAtomSubset() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Test terminology is invalid - empty results
    Logger.getLogger(getClass()).info(
        "TEST invalid terminology - " + "TTT, 2016_03_01, " + authToken);
    assertEquals(0,
        contentService.getAtomSubsets("TTT", snomedVersion, authToken)
            .getObjects().size());

    // Test version is invalid - empty results
    Logger.getLogger(getClass()).info(
        "TEST invalid version - " + "SNOMEDCT_US, TTT, " + authToken);
    assertEquals(0,
        contentService.getAtomSubsets(snomedTerminology, "TTT", authToken)
            .getObjects().size());

    SubsetList list =
        contentService.getAtomSubsets(snomedTerminology, snomedVersion,
            authToken);

    // Test negative start index - indicates not to use paging
    Logger.getLogger(getClass()).info("TEST negative PFS start index");
    PfsParameterJpa pfs = new PfsParameterJpa();
    pfs.setStartIndex(-20);
    Subset subset = list.getObjects().get(0);
    try {
      contentService.findAtomSubsetMembers(subset.getTerminologyId(),
          snomedTerminology, snomedVersion, null, pfs, authToken);
    } catch (Exception e) {
      fail("Exception should NOT be thrown when trying to find atom subset members with negative pfs start index.");
    }

    // Test invalid pfs max results - indicates not to use paging
    Logger.getLogger(getClass()).info("TEST negative PFS max results");
    pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(-20);
    try {
      contentService.findAtomSubsetMembers(subset.getTerminologyId(),
          snomedTerminology, snomedVersion, null, pfs, authToken);
    } catch (Exception e) {
      fail("Exception should NOT be thrown when trying to find atom subset members with negative pfs max results.");
    }

    // Test invalid pfs sort field
    Logger.getLogger(getClass()).info("TEST invalid PFS sort field");
    pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(20);
    pfs.setSortField("TTT");
    try {
      contentService.findAtomSubsetMembers(subset.getTerminologyId(),
          snomedTerminology, snomedVersion, null, pfs, authToken);
      fail("Exception should be thrown when trying to find atom subset members with empty string sort field.");
    } catch (Exception e) {
      // do nothing
    }

    // Test invalid query restriction

    Logger.getLogger(getClass()).info("TEST invalid PFS query restrictiohn");
    pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(20);
    pfs.setQueryRestriction("TTT:TTT");
    try {
      contentService.findAtomSubsetMembers(subset.getTerminologyId(),
          snomedTerminology, snomedVersion, null, pfs, authToken);
      fail("Exception should be thrown when trying to find atom subset members with invalid query restriction.");
    } catch (Exception e) { // do nothing
    }

    // Test terminology is invalid - empty result
    Logger.getLogger(getClass()).info(
        "TEST invalid terminology - " + "TTT, 2016_03_01, " + authToken);
    assertEquals(
        0,
        contentService
            .findAtomSubsetMembers(subset.getTerminologyId(), "TTT",
                snomedVersion, null, new PfsParameterJpa(), authToken)
            .getObjects().size());

    // Test version is invalid - empty result
    Logger.getLogger(getClass()).info(
        "TEST invalid version- " + "SNOMEDCT_US, TTT, " + authToken);
    assertEquals(
        0,
        contentService
            .findAtomSubsetMembers(subset.getTerminologyId(),
                snomedTerminology, "TTT", null, new PfsParameterJpa(),
                authToken).getObjects().size());

  }

  /**
   * Test "get" methods for concept subsets
   * @throws Exception
   */
  @Test
  public void testGetConceptSubset() throws Exception {

    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Test terminology is invalid - empty result
    Logger.getLogger(getClass()).info(
        "TEST invalid terminology - " + "TTT, 2016_03_01, " + authToken);
    assertEquals(0,
        contentService.getConceptSubsets("TTT", snomedVersion, authToken)
            .getObjects().size());

    // Test version is invalid - empty result
    Logger.getLogger(getClass()).info(
        "TEST invalid version - " + "SNOMEDCT_US, TTT, " + authToken);
    assertEquals(0,
        contentService.getConceptSubsets(snomedTerminology, "TTT", authToken)
            .getObjects().size());

    SubsetList list =
        contentService.getConceptSubsets(snomedTerminology, snomedVersion,
            authToken);

    // Test negative start index - indicates not to use paging
    Logger.getLogger(getClass()).info("TEST negative PFS start index");
    PfsParameterJpa pfs = new PfsParameterJpa();
    pfs.setStartIndex(-20);
    Subset subset = list.getObjects().get(0);
    try {
      contentService.findConceptSubsetMembers(subset.getTerminologyId(),
          snomedTerminology, snomedVersion, null, pfs, authToken);
    } catch (Exception e) {
      fail("Exception should NOT be thrown when trying to find concept subset members with negative pfs start index.");
    }

    // Test invalid pfs max results - indicates not to use paging
    Logger.getLogger(getClass()).info("TEST negative PFS max results");
    pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(-20);
    try {
      contentService.findConceptSubsetMembers(subset.getTerminologyId(),
          snomedTerminology, snomedVersion, null, pfs, authToken);
    } catch (Exception e) {
      fail("Exception should NOT be thrown when trying to find concept subset members with negative pfs max results.");
    }

    // Test invalid pfs sort field
    Logger.getLogger(getClass()).info("TEST invalid PFS sort field");
    pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(20);
    pfs.setSortField("TTT");
    try {
      contentService.findConceptSubsetMembers(subset.getTerminologyId(),
          snomedTerminology, snomedVersion, null, pfs, authToken);
      fail("Exception should be thrown when trying to find concept subset members with empty string sort field.");
    } catch (Exception e) {
      // do nothing
    }

    // Test invalid query restriction
    Logger.getLogger(getClass()).info("TEST invalid PFS query restriction");
    pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(20);
    pfs.setQueryRestriction("TTT:TTT");
    try {
      contentService.findConceptSubsetMembers(subset.getTerminologyId(),
          snomedTerminology, snomedVersion, null, pfs, authToken);
      fail("Exception should be thrown when trying to find concept subset members with invalid query restriction.");
    } catch (Exception e) { // do nothing
    }

    // Test terminology is invalid - empty result
    Logger.getLogger(getClass()).info(
        "TEST invalid terminology - " + "TTT, 2016_03_01, " + authToken);
    assertEquals(
        0,
        contentService
            .findConceptSubsetMembers(subset.getTerminologyId(), "TTT",
                snomedVersion, null, new PfsParameterJpa(), authToken)
            .getObjects().size());

    // Test version is invalid - empty result
    Logger.getLogger(getClass()).info(
        "TEST invalid version - " + "SNOMEDCT_US, TTT, " + authToken);
    assertEquals(
        0,
        contentService
            .findConceptSubsetMembers(subset.getTerminologyId(),
                snomedTerminology, "TTT", null, new PfsParameterJpa(),
                authToken).getObjects().size());

  }

  /**
   * Test find concepts for query.
   * @throws Exception
   */
  @Test
  public void testFindConcepts() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Test terminology is invalid - empty result
    Logger.getLogger(getClass()).info("TEST invalid termionlogy - ");
    assertEquals(
        0,
        contentService
            .findConcepts("TTT", snomedVersion, "care", new PfsParameterJpa(),
                authToken).getObjects().size());

    // Test version is invalid - empty result
    Logger.getLogger(getClass()).info("TEST invalid version - ");
    assertEquals(
        0,
        contentService
            .findConcepts(snomedTerminology, "TTT", "care",
                new PfsParameterJpa(), authToken).getObjects().size());

    // Test query is null - All concepts
    Logger.getLogger(getClass()).info("TEST null query - ");
    assertEquals(
        3902,
        contentService
            .findConcepts(snomedTerminology, snomedVersion, null,
                new PfsParameterJpa(), authToken).getObjects().size());

    // Test query is empty string - empty results
    Logger.getLogger(getClass()).info("TEST empty query - ");
    assertEquals(
        3902,
        contentService
            .findConcepts(snomedTerminology, snomedVersion, "",
                new PfsParameterJpa(), authToken).getObjects().size());

  }

  /**
   * Test find descriptors for query.
   * @throws Exception
   */
  @Test
  public void testFindDescriptors() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Test terminology is invalid - empty result
    Logger.getLogger(getClass()).info("TEST invalid terminology - ");
    assertEquals(
        0,
        contentService
            .findDescriptors("TTT", snomedVersion, "care",
                new PfsParameterJpa(), authToken).getObjects().size());

    // Test version is invalid - empty results
    Logger.getLogger(getClass()).info("TEST invalid version - ");
    assertEquals(
        0,
        contentService
            .findDescriptors(snomedTerminology, "TTT", "care",
                new PfsParameterJpa(), authToken).getObjects().size());

    // Test query is null - empty results
    Logger.getLogger(getClass()).info("TEST null query - ");
    assertEquals(
        0,
        contentService
            .findDescriptors(snomedTerminology, snomedVersion, null,
                new PfsParameterJpa(), authToken).getObjects().size());

    // Test query is empty string - empty results
    Logger.getLogger(getClass()).info("TEST empty query - ");
    assertEquals(
        0,
        contentService
            .findDescriptors(snomedTerminology, snomedVersion, "",
                new PfsParameterJpa(), authToken).getObjects().size());
  }

  /**
   * Test find codes for query.
   * @throws Exception
   */
  @Test
  public void testFindCodes() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Test terminology is invalid - empty results
    Logger.getLogger(getClass()).info("TEST invalid terminology - ");
    assertEquals(
        0,
        contentService
            .findCodes("TTT", snomedVersion, "care", new PfsParameterJpa(),
                authToken).getObjects().size());

    // Test version is invalid - empty result
    Logger.getLogger(getClass()).info("TEST invalid version - ");
    assertEquals(
        0,
        contentService
            .findCodes(snomedTerminology, "TTT", "care", new PfsParameterJpa(),
                authToken).getObjects().size());

    // Test query is null - no results
    Logger.getLogger(getClass()).info("TEST null query - ");
    assertEquals(
        3902,
        contentService
            .findCodes(snomedTerminology, snomedVersion, null,
                new PfsParameterJpa(), authToken).getObjects().size());

    // Test query is empty string - empty result
    Logger.getLogger(getClass()).info("TEST empty query - ");
    assertEquals(
        3902,
        contentService
            .findCodes(snomedTerminology, snomedVersion, "",
                new PfsParameterJpa(), authToken).getObjects().size());

  }

  /**
   * Test find descendant concepts.
   * @throws Exception
   */
  @Test
  public void testFindDescendantConcepts() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Test terminology is invalid - empty result
    Logger.getLogger(getClass()).info("TEST invalid terminology - ");
    assertEquals(
        0,
        contentService
            .findDescendantConcepts("105590001", "TTT", snomedVersion, false,
                new PfsParameterJpa(), authToken).getObjects().size());

    // Test version is invalid - empty result
    Logger.getLogger(getClass()).info("TEST invalid version - ");
    assertEquals(
        0,
        contentService
            .findDescendantConcepts("105590001", snomedTerminology, "TTT",
                false, new PfsParameterJpa(), authToken).getObjects().size());

    // Test terminology is invalid - empty result
    Logger.getLogger(getClass()).info("TEST invalid terminology - ");
    assertEquals(
        0,
        contentService
            .findAncestorConcepts("105590001", "TTT", snomedVersion, false,
                new PfsParameterJpa(), authToken).getObjects().size());

    // Test version is invalid - empty result
    Logger.getLogger(getClass()).info("TEST invalid version - ");
    assertEquals(
        0,
        contentService
            .findAncestorConcepts("105590001", snomedTerminology, "TTT", false,
                new PfsParameterJpa(), authToken).getObjects().size());

  }

  /**
   * Test find descendant descriptors.
   * @throws Exception
   */
  @Test
  public void testFindDescendantDescriptors() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Test terminology is invalid - empty result
    Logger.getLogger(getClass()).info("TEST invalid terminology - ");
    assertEquals(
        0,
        contentService
            .findDescendantDescriptors("D000005", "TTT", mshVersion, false,
                new PfsParameterJpa(), authToken).getObjects().size());

    // Test version is invalid - empty result
    Logger.getLogger(getClass()).info("TEST invalid version - ");
    assertEquals(
        0,
        contentService
            .findDescendantDescriptors("D000005", mshTerminology, "TTT", false,
                new PfsParameterJpa(), authToken).getObjects().size());

    // Test terminology is invalid - empty result
    Logger.getLogger(getClass()).info("TEST invalid terminology - ");
    assertEquals(
        0,
        contentService
            .findAncestorDescriptors("D000005", "TTT", mshVersion, false,
                new PfsParameterJpa(), authToken).getObjects().size());

    // Test version is invalid - empty result
    Logger.getLogger(getClass()).info("TEST invalid version - ");
    assertEquals(
        0,
        contentService
            .findAncestorDescriptors("D000005", mshTerminology, "TTT", false,
                new PfsParameterJpa(), authToken).getObjects().size());
  }

  /**
   * Test "get" subset members for atom or concept.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetSubsetMembers() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Test invalid concept id - empty list
    Logger.getLogger(getClass()).info("  Test get subset members for concept");
    SubsetMemberList list =
        contentService.getConceptSubsetMembers("-1", snomedTerminology,
            snomedVersion, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(0, list.getTotalCount());
    assertEquals(0, list.size());

    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Test with invalid terminologyId

    Logger.getLogger(getClass()).info("TEST invalid terminologyId ");
    list =
        contentService.getAtomSubsetMembers("-1", snomedTerminology,
            snomedVersion, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(0, list.getTotalCount());
    assertEquals(0, list.size());

    // Test with invalid terminology - empty results
    Logger.getLogger(getClass()).info("TEST invalid terminology ");
    assertEquals(
        0,
        contentService
            .getAtomSubsetMembers("166113012", "TTT", snomedVersion,
                authToken).getObjects().size());

    // Test with invalid version - empty result
    Logger.getLogger(getClass()).info("TEST invalid version");
    assertEquals(
        0,
        contentService
            .getAtomSubsetMembers("166113012", "MSH", "TTT", authToken)
            .getObjects().size());

    // Test with invalid terminologyId - empty result
    Logger.getLogger(getClass()).info("TEST invalid terminologyId ");
    assertEquals(
        0,
        contentService
            .getConceptSubsetMembers("-1", snomedTerminology, snomedVersion,
                authToken).getObjects().size());

    // Test with invalid terminology - empty result
    Logger.getLogger(getClass()).info("TEST invalid terminology ");
    assertEquals(
        0,
        contentService
            .getConceptSubsetMembers("10123006", "TTT", snomedVersion,
                authToken).getObjects().size());

    // Test with invalid version - empty result
    Logger.getLogger(getClass()).info("TEST invalid version");
    assertEquals(
        0,
        contentService
            .getConceptSubsetMembers("10123006", "MSH", "TTT", authToken)
            .getObjects().size());
  }

  /**
   * Test autocomplete for concepts.
   *
   * @throws Exception the exception
   */
  @Test
  public void testAutocompleteConcepts() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Test with invalid searchTerm - empty results
    Logger.getLogger(getClass()).info("TEST invalid searchTerm ");
    assertEquals(
        0,
        contentService
            .autocompleteConcepts(snomedTerminology, snomedVersion, "qrs",
                authToken).getObjects().size());
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
      if (parameters[i].toString().contains("oolean"))
        types[i] = boolean.class;
      else
        types[i] = parameters[i].getClass();

    }
    return types;
  }

}
