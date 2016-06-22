/*
 * Copyright 2016 West Coast Informatics, LLC
 */
/*
 * 
 */
package com.wci.umls.server.test.rest;

import static org.junit.Assert.assertEquals;
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
  private String snomedVersion = "2014_09_01";

  /** The msh terminology. */
  private String mshTerminology = "MSH";

  /** The msh version. */
  private String mshVersion = "2015_2014_09_08";

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
    testVersion = "2014_09_01";
    testId = "102466009";

    // get test concept
    concept = contentService.getConcept(testId, testTerminology, testVersion,
        null, authToken);

  }

  /**
   * Test "get" methods for concepts.
   *
   * @throws Exception the exception
   */
  @Test
  public void testEdgeCasesRestContent001() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Test with invalid terminologyId
    Logger.getLogger(getClass()).info("TEST invalid terminology id - "
        + "-1, SNOMEDCT, 2014_09_01, " + authToken);
    try {
      contentService.getConcept("-1", snomedTerminology, snomedVersion, null,
          authToken);
      fail(
          "Exception should be thrown when trying to get a concept with invalid terminologyId.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with invalid terminology
    Logger.getLogger(getClass()).info("TEST invalid terminology - "
        + "M0028634, TTT, 2015_2014_09_08, " + authToken);
    try {
      contentService.getConcept("M0028634", "TTT", mshVersion, null, authToken);
      fail(
          "Exception should be thrown when trying to get a concept with invalid terminology.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with invalid version
    Logger.getLogger(getClass())
        .info("TEST invalid version - " + "M0028634, MSH, TTT , " + authToken);
    try {
      contentService.getConcept("M0028634", "MSH", "TTT", null, authToken);
      fail(
          "Exception should be thrown when trying to get a concept with invalid version.");
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
  public void testEdgeCasesRestContent002() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Test invalid SNOMEDCT_US descriptor
    Logger.getLogger(getClass()).info("TEST invalid SNOMEDCT_US descriptor - "
        + "-1, SNOMEDCT, 2014_09_01, " + authToken);
    try {
      contentService.getDescriptor("-1", snomedTerminology, snomedVersion, null,
          authToken);
      fail(
          "Exception should be thrown when trying to get a descriptor with invalid terminologyId.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with invalid terminology
    Logger.getLogger(getClass()).info("TEST invalid termionlogy - "
        + "M0028634, TTT, 2015_2014_09_08, " + authToken);
    try {
      contentService.getDescriptor("M0028634", "TTT", mshVersion, null, authToken);
      fail(
          "Exception should be thrown when trying to get a descriptor with invalid terminology.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with invalid version
    Logger.getLogger(getClass())
        .info("TEST invalid version - " + "M0028634, MSH, TTT , " + authToken);
    try {
      contentService.getDescriptor("M0028634", "MSH", "TTT", null, authToken);
      fail(
          "Exception should be thrown when trying to get a descriptor with invalid version.");
    } catch (Exception e) {
      // do nothing
    }

  }

  /**
   * Test "get" methods for concepts.
   *
   * @throws Exception the exception
   */
  @Test
  public void testEdgeCasesRestContent003() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Test invalid MSH code
    Logger.getLogger(getClass()).info(
        "TEST invalid MSH code - " + "ABC, MSH, 2015_2014_09_08, " + authToken);
    try {
      contentService.getCode("ABC", mshTerminology, mshVersion, null,
          authToken);
      fail(
          "Exception should be thrown when trying to get a code with null terminologyId.");
    } catch (Exception e) {
      // do nothing
    }

    // Test invalid SNOMEDCT_US code
    Logger.getLogger(getClass()).info("TEST invalid SNOMEDCT_US code - "
        + "ABC, SNOMEDCT, 2014_09_01, " + authToken);
    try {
      contentService.getCode("ABC", snomedTerminology, snomedVersion, null,
          authToken);
      fail(
          "Exception should be thrown when trying to get a code with invalid terminologyId.");
    } catch (Exception e) {
      // do nothing
    }

    // Test invalid UMLS code
    Logger.getLogger(getClass())
        .info("TEST invalid UMLS code - " + "ABC, UMLS, latest, " + authToken);
    try {
      contentService.getCode("ABC", umlsTerminology, umlsVersion, null, authToken);
      fail(
          "Exception should be thrown when trying to get a code with empty string terminologyId.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with invalid version
    Logger.getLogger(getClass())
        .info("TEST invalid version - " + "M0028634, MSH, TTT , " + authToken);
    try {
      contentService.getCode("M0028634", "MSH", "TTT", null, authToken);
      fail(
          "Exception should be thrown when trying to get a code with invalid version.");
    } catch (Exception e) {
      // do nothing
    }

  }

  /**
   * Test "get" method for lexical classes.
   * @throws Exception
   */
  @Test
  public void testEdgeCasesRestContent004() throws Exception {
    // n/a - no data in sample
  }

  /**
   * Test "get" method for string classes.
   * @throws Exception
   */
  @Test
  public void testEdgeCasesRestContent005() throws Exception {
    // n/a - no data in sample
  }

  /**
   * Test "get" methods for atom subsets
   * @throws Exception
   */
  @Test
  public void testEdgeCasesRestContent006() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Test terminology is invalid - empty results
    Logger.getLogger(getClass())
        .info("TEST invalid terminology - " + "TTT, 2014_09_01, " + authToken);
    assertEquals(0, contentService
        .getAtomSubsets("TTT", snomedVersion, authToken).getObjects().size());

    // Test version is invalid - empty results
    Logger.getLogger(getClass())
        .info("TEST invalid version - " + "SNOMEDCT_US, TTT, " + authToken);
    assertEquals(0,
        contentService.getAtomSubsets(snomedTerminology, "TTT", authToken)
            .getObjects().size());

    SubsetList list = contentService.getAtomSubsets(snomedTerminology,
        snomedVersion, authToken);

    // Test negative start index - indicates not to use paging
    Logger.getLogger(getClass()).info("TEST negative PFS start index");
    PfsParameterJpa pfs = new PfsParameterJpa();
    pfs.setStartIndex(-20);
    Subset subset = list.getObjects().get(0);
    try {
      contentService.findAtomSubsetMembers(subset.getTerminologyId(),
          snomedTerminology, snomedVersion, null, pfs, authToken);
    } catch (Exception e) {
      fail(
          "Exception should NOT be thrown when trying to find atom subset members with negative pfs start index.");
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
      fail(
          "Exception should NOT be thrown when trying to find atom subset members with negative pfs max results.");
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
      fail(
          "Exception should be thrown when trying to find atom subset members with empty string sort field.");
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
      fail(
          "Exception should be thrown when trying to find atom subset members with invalid query restriction.");
    } catch (Exception e) { // do nothing
    }

    // Test terminology is invalid - empty result
    Logger.getLogger(getClass())
        .info("TEST invalid terminology - " + "TTT, 2014_09_01, " + authToken);
    assertEquals(0,
        contentService
            .findAtomSubsetMembers(subset.getTerminologyId(), "TTT",
                snomedVersion, null, new PfsParameterJpa(), authToken)
            .getObjects().size());

    // Test version is invalid - empty result
    Logger.getLogger(getClass())
        .info("TEST invalid version- " + "SNOMEDCT_US, TTT, " + authToken);
    assertEquals(0,
        contentService
            .findAtomSubsetMembers(subset.getTerminologyId(), snomedTerminology,
                "TTT", null, new PfsParameterJpa(), authToken)
            .getObjects().size());

  }

  /**
   * Test "get" methods for concept subsets
   * @throws Exception
   */
  @Test
  public void testEdgeCasesRestContent007() throws Exception {

    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Test terminology is invalid - empty result
    Logger.getLogger(getClass())
        .info("TEST invalid terminology - " + "TTT, 2014_09_01, " + authToken);
    assertEquals(0,
        contentService.getConceptSubsets("TTT", snomedVersion, authToken)
            .getObjects().size());

    // Test version is invalid - empty result
    Logger.getLogger(getClass())
        .info("TEST invalid version - " + "SNOMEDCT_US, TTT, " + authToken);
    assertEquals(0,
        contentService.getConceptSubsets(snomedTerminology, "TTT", authToken)
            .getObjects().size());

    SubsetList list = contentService.getConceptSubsets(snomedTerminology,
        snomedVersion, authToken);

    // Test negative start index - indicates not to use paging
    Logger.getLogger(getClass()).info("TEST negative PFS start index");
    PfsParameterJpa pfs = new PfsParameterJpa();
    pfs.setStartIndex(-20);
    Subset subset = list.getObjects().get(0);
    try {
      contentService.findConceptSubsetMembers(subset.getTerminologyId(),
          snomedTerminology, snomedVersion, null, pfs, authToken);
    } catch (Exception e) {
      fail(
          "Exception should NOT be thrown when trying to find concept subset members with negative pfs start index.");
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
      fail(
          "Exception should NOT be thrown when trying to find concept subset members with negative pfs max results.");
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
      fail(
          "Exception should be thrown when trying to find concept subset members with empty string sort field.");
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
      fail(
          "Exception should be thrown when trying to find concept subset members with invalid query restriction.");
    } catch (Exception e) { // do nothing
    }

    // Test terminology is invalid - empty result
    Logger.getLogger(getClass())
        .info("TEST invalid terminology - " + "TTT, 2014_09_01, " + authToken);
    assertEquals(0,
        contentService
            .findConceptSubsetMembers(subset.getTerminologyId(), "TTT",
                snomedVersion, null, new PfsParameterJpa(), authToken)
            .getObjects().size());

    // Test version is invalid - empty result
    Logger.getLogger(getClass())
        .info("TEST invalid version - " + "SNOMEDCT_US, TTT, " + authToken);
    assertEquals(0,
        contentService.findConceptSubsetMembers(subset.getTerminologyId(),
            snomedTerminology, "TTT", null, new PfsParameterJpa(), authToken)
        .getObjects().size());

  }

  /**
   * Test find concepts for query.
   * @throws Exception
   */
  @Test
  public void testEdgeCasesRestContent008() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Test terminology is invalid - empty result
    Logger.getLogger(getClass()).info("TEST invalid termionlogy - ");
    assertEquals(0, contentService.findConceptsForQuery("TTT", snomedVersion,
        "care", new PfsParameterJpa(), authToken).getObjects().size());

    // Test version is invalid - empty result
    Logger.getLogger(getClass()).info("TEST invalid version - ");
    assertEquals(0, contentService.findConceptsForQuery(snomedTerminology,
        "TTT", "care", new PfsParameterJpa(), authToken).getObjects().size());

    // Test query is null - empty results
    Logger.getLogger(getClass()).info("TEST null query - ");
    assertEquals(0,
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            null, new PfsParameterJpa(), authToken).getObjects().size());

    // Test query is empty string - empty results
    Logger.getLogger(getClass()).info("TEST empty query - ");
    assertEquals(0,
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            "", new PfsParameterJpa(), authToken).getObjects().size());

  }

  /**
   * Test find descriptors for query.
   * @throws Exception
   */
  @Test
  public void testEdgeCasesRestContent009() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Test terminology is invalid - empty result
    Logger.getLogger(getClass()).info("TEST invalid terminology - ");
    assertEquals(0, contentService.findDescriptorsForQuery("TTT", snomedVersion,
        "care", new PfsParameterJpa(), authToken).getObjects().size());

    // Test version is invalid - empty results
    Logger.getLogger(getClass()).info("TEST invalid version - ");
    assertEquals(0, contentService.findDescriptorsForQuery(snomedTerminology,
        "TTT", "care", new PfsParameterJpa(), authToken).getObjects().size());

    // Test query is null - empty results
    Logger.getLogger(getClass()).info("TEST null query - ");
    assertEquals(0,
        contentService.findDescriptorsForQuery(snomedTerminology, snomedVersion,
            null, new PfsParameterJpa(), authToken).getObjects().size());

    // Test query is empty string - empty results
    Logger.getLogger(getClass()).info("TEST empty query - ");
    assertEquals(0,
        contentService.findDescriptorsForQuery(snomedTerminology, snomedVersion,
            "", new PfsParameterJpa(), authToken).getObjects().size());
  }

  /**
   * Test find codes for query.
   * @throws Exception
   */
  @Test
  public void testEdgeCasesRestContent010() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Test terminology is invalid - empty results
    Logger.getLogger(getClass()).info("TEST invalid terminology - ");
    assertEquals(0, contentService.findCodesForQuery("TTT", snomedVersion,
        "care", new PfsParameterJpa(), authToken).getObjects().size());

    // Test version is invalid - empty result
    Logger.getLogger(getClass()).info("TEST invalid version - ");
    assertEquals(0, contentService.findCodesForQuery(snomedTerminology, "TTT",
        "care", new PfsParameterJpa(), authToken).getObjects().size());

    // Test query is null - no results
    Logger.getLogger(getClass()).info("TEST null query - ");
    assertEquals(0,
        contentService.findCodesForQuery(snomedTerminology, snomedVersion, null,
            new PfsParameterJpa(), authToken).getObjects().size());

    // Test query is empty string - empty result
    Logger.getLogger(getClass()).info("TEST empty query - ");
    assertEquals(0,
        contentService.findCodesForQuery(snomedTerminology, snomedVersion, "",
            new PfsParameterJpa(), authToken).getObjects().size());

  }

  /**
   * Test find descendant concepts.
   * @throws Exception
   */
  @Test
  public void testEdgeCasesRestContent011() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Test terminology is invalid - empty result
    Logger.getLogger(getClass()).info("TEST invalid terminology - ");
    assertEquals(0,
        contentService.findDescendantConcepts("105590001", "TTT", snomedVersion,
            false, new PfsParameterJpa(), authToken).getObjects().size());

    // Test version is invalid - empty result
    Logger.getLogger(getClass()).info("TEST invalid version - ");
    assertEquals(0,
        contentService.findDescendantConcepts("105590001", snomedTerminology,
            "TTT", false, new PfsParameterJpa(), authToken).getObjects()
        .size());

    // Test terminology is invalid - empty result
    Logger.getLogger(getClass()).info("TEST invalid terminology - ");
    assertEquals(0,
        contentService.findAncestorConcepts("105590001", "TTT", snomedVersion,
            false, new PfsParameterJpa(), authToken).getObjects().size());

    // Test version is invalid - empty result
    Logger.getLogger(getClass()).info("TEST invalid version - ");
    assertEquals(0,
        contentService.findAncestorConcepts("105590001", snomedTerminology,
            "TTT", false, new PfsParameterJpa(), authToken).getObjects()
        .size());

  }

  /**
   * Test find descendant descriptors.
   * @throws Exception
   */
  @Test
  public void testEdgeCasesRestContent012() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Test terminology is invalid - empty result
    Logger.getLogger(getClass()).info("TEST invalid terminology - ");
    assertEquals(0,
        contentService.findDescendantDescriptors("D000005", "TTT", mshVersion,
            false, new PfsParameterJpa(), authToken).getObjects().size());

    // Test version is invalid - empty result
    Logger.getLogger(getClass()).info("TEST invalid version - ");
    assertEquals(0,
        contentService.findDescendantDescriptors("D000005", mshTerminology,
            "TTT", false, new PfsParameterJpa(), authToken).getObjects()
        .size());

    // Test terminology is invalid - empty result
    Logger.getLogger(getClass()).info("TEST invalid terminology - ");
    assertEquals(0,
        contentService.findAncestorDescriptors("D000005", "TTT", mshVersion,
            false, new PfsParameterJpa(), authToken).getObjects().size());

    // Test version is invalid - empty result
    Logger.getLogger(getClass()).info("TEST invalid version - ");
    assertEquals(0,
        contentService.findAncestorDescriptors("D000005", mshTerminology, "TTT",
            false, new PfsParameterJpa(), authToken).getObjects().size());
  }

  /**
   * Test degenerate use rest content013.
   *
   * @throws Exception the exception
   */
  @Test
  public void testEdgeCasesRestContent013() throws Exception {
    // n/a - no code ancestors or descendants
  }

  /**
   * Test "get" subset members for atom or concept.
   *
   * @throws Exception the exception
   */
  @Test
  public void testEdgeCasesRestContent014() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Test invalid concept id - empty list
    Logger.getLogger(getClass()).info("  Test get subset members for concept");
    SubsetMemberList list = contentService.getSubsetMembersForConcept("-1",
        snomedTerminology, snomedVersion, authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + list.getTotalCount());
    assertEquals(0, list.getTotalCount());
    assertEquals(0, list.getCount());

    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Test with invalid terminologyId

    Logger.getLogger(getClass()).info("TEST invalid terminologyId ");
    list = contentService.getSubsetMembersForAtom("-1", snomedTerminology,
        snomedVersion, authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + list.getTotalCount());
    assertEquals(0, list.getTotalCount());
    assertEquals(0, list.getCount());

    // Test with invalid terminology - empty results
    Logger.getLogger(getClass()).info("TEST invalid terminology ");
    assertEquals(0, contentService
        .getSubsetMembersForAtom("166113012", "TTT", snomedVersion, authToken)
        .getObjects().size());

    // Test with invalid version - empty result
    Logger.getLogger(getClass()).info("TEST invalid version");
    assertEquals(0,
        contentService
            .getSubsetMembersForAtom("166113012", "MSH", "TTT", authToken)
            .getObjects().size());

    // Test with invalid terminologyId - empty result
    Logger.getLogger(getClass()).info("TEST invalid terminologyId ");
    assertEquals(0, contentService.getSubsetMembersForConcept("-1",
        snomedTerminology, snomedVersion, authToken).getObjects().size());

    // Test with invalid terminology - empty result
    Logger.getLogger(getClass()).info("TEST invalid terminology ");
    assertEquals(0, contentService
        .getSubsetMembersForConcept("10123006", "TTT", snomedVersion, authToken)
        .getObjects().size());

    // Test with invalid version - empty result
    Logger.getLogger(getClass()).info("TEST invalid version");
    assertEquals(0,
        contentService
            .getSubsetMembersForConcept("10123006", "MSH", "TTT", authToken)
            .getObjects().size());
  }

  /**
   * Test autocomplete for concepts.
   *
   * @throws Exception the exception
   */
  @Test
  public void testEdgeCasesRestContent015() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Test with invalid searchTerm - empty results
    Logger.getLogger(getClass()).info("TEST invalid searchTerm ");
    assertEquals(0, contentService.autocompleteConcepts(snomedTerminology,
        snomedVersion, "qrs", authToken).getObjects().size());
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
