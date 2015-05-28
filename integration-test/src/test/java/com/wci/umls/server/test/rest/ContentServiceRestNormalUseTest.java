/*
 * Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package com.wci.umls.server.test.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.wci.umls.server.helpers.SearchCriteria;
import com.wci.umls.server.helpers.SearchResult;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.helpers.content.ConceptList;
import com.wci.umls.server.helpers.content.RelationshipList;
import com.wci.umls.server.helpers.content.SubsetList;
import com.wci.umls.server.jpa.content.CodeJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.DescriptorJpa;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.helpers.SearchCriteriaJpa;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.services.helpers.ConceptReportHelper;
import com.wci.umls.server.test.helpers.PfsParameterForComponentTest;

/**
 * Implementation of the "Content Service REST Normal Use" Test Cases.
 */
public class ContentServiceRestNormalUseTest extends ContentServiceRestTest {

  /**  The auth token. */
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
   * Test Get and Find methods for concepts
   * @throws Exception
   */
  @Test
  public void testNormalUseRestContent001() throws Exception {

    Logger.getLogger(getClass()).debug("Start test");
    
    /** Get concepts */
    String mshTerminology = "MSH";
    String mshVersion = "2015_2014_09_08";
    
    Logger.getLogger(getClass()).info(
        "TEST - " + "M0028634, MSH, 2015_2014_09_08, " + authToken);
    Concept  c = contentService.getConcept("M0028634", mshTerminology, mshVersion, authToken);
    Logger.getLogger(getClass()).info(
        ConceptReportHelper.getConceptReport(c));
    assertNotNull(c);
    assertNotEquals(c.getName(),
        "No default preferred name found");

    String snomedTerminology = "SNOMEDCT_US";
    String snomedVersion = "2014_09_01";
    
    Logger.getLogger(getClass()).info(
        "TEST - " + "10001005, SNOMEDCT, 2014_09_01, " + authToken);
    c = contentService.getConcept("10001005", snomedTerminology, snomedVersion, authToken);
    Logger.getLogger(getClass()).info(
        ConceptReportHelper.getConceptReport(c));
        assertNotNull(c);
    assertNotEquals(c.getName(),
        "No default preferred name found");
    
    
    /** Find concepts for query */

    // For test, execute findConceptsForQuery("SNOMEDCT_US", "2014_09_01", "care", ...) for
    // SNOMEDCT
    String query = "care";
    PfsParameterJpa pfs = new PfsParameterJpa();
    SearchResultList searchResults;

    // Raw results – No pfs parameter
    // TEST: 19 results
    searchResults =
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            query, pfs, authToken);

    assertTrue(searchResults.getCount() == 19);

    // Sorted results – Pfs parameter with sortField name
    // TEST: 19 results, sorted alphabetically
    pfs.setSortField("name");
    searchResults =
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            query, pfs, authToken);
    assertTrue(searchResults.getCount() == 19);
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfs, ConceptJpa.class));

    // test descending order
    pfs.setAscending(false);

    searchResults =
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            query, pfs, authToken);
    assertTrue(searchResults.getCount() == 19);
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfs, ConceptJpa.class));

    // store the sorted results
    SearchResultList storedResults = searchResults;

    // Paged, sorted results, first page – Pfs parameter with max results 5 and
    // sortField name
    // TEST: 5 results, matching first 5 results from previous test
    pfs.setSortField("name");
    pfs.setStartIndex(0);
    pfs.setMaxResults(5);
    searchResults =
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            query, pfs, authToken);
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfs, ConceptJpa.class));
    assertTrue(PfsParameterForComponentTest.testPaging(searchResults,
        storedResults, pfs));

    // Paged, sorted results, second page – Pfs parameter with startIndex 6, max
    // results 5 and sortField name
    // TEST: 5 results, matching second set of 5 results from previous test
    pfs.setSortField("name");
    pfs.setStartIndex(5);
    pfs.setMaxResults(5);
    searchResults =
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            query, pfs, authToken);

    assertTrue(PfsParameterForComponentTest.testPaging(searchResults,
        storedResults, pfs));
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfs, ConceptJpa.class));

    // test lucene query restriction
    pfs = new PfsParameterJpa();
    pfs.setQueryRestriction("terminologyId:169559003");
    searchResults =
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            query, pfs, authToken);

    Logger.getLogger(getClass()).info("QR results: " + searchResults.toString());

    assertTrue(searchResults.getCount() == 1);
    assertTrue(searchResults.getObjects().get(0).getTerminologyId()
        .equals("169559003"));

  }

  /**
   * Test Get and Find methods for concepts
   * @throws Exception
   */
  @Test
  public void testNormalUseRestContent002() throws Exception {

    Logger.getLogger(getClass()).debug("Start test");
    
    String mshTerminology = "MSH";
    String mshVersion = "2015_2014_09_08";
    
    Logger.getLogger(getClass()).info(
        "TEST - " + "D004891, MSH, 2015_2014_09_08, " + authToken);
    Descriptor descriptor =
        contentService.getDescriptor("D004891", mshTerminology, mshVersion, authToken);
    assertNotNull(descriptor);
    assertEquals(descriptor.getName(), "Erythema Induratum");
    
    
    /** Find descriptions for query */

    String query = "amino*";
    PfsParameterJpa pfs = new PfsParameterJpa();
    SearchResultList searchResults;

    // Raw results – No pfs parameter
    searchResults =
        contentService.findDescriptorsForQuery(mshTerminology, mshVersion,
            query, pfs, authToken);

    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("Result: " + sr.getTerminologyId() + " " + sr.getValue());
    }
    assertTrue(searchResults.getCount() == 9);

    // Sorted results – Pfs parameter with sortField name
    // TEST: 9 results, sorted alphabetically
    pfs.setSortField("name");
    searchResults =
        contentService.findDescriptorsForQuery(mshTerminology, mshVersion,
            query, pfs, authToken);
    assertTrue(searchResults.getCount() == 9);
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfs, DescriptorJpa.class));

    // test descending order
    pfs.setAscending(false);

    searchResults =
        contentService.findDescriptorsForQuery(mshTerminology, mshVersion,
            query, pfs, authToken);
    assertTrue(searchResults.getCount() == 9);
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfs, DescriptorJpa.class));

    // store the sorted results
    SearchResultList storedResults = searchResults;

    // Paged, sorted results, first page – Pfs parameter with max results 5 and
    // sortField name
    // TEST: 5 results, matching first 5 results from previous test
    pfs.setSortField("name");
    pfs.setStartIndex(0);
    pfs.setMaxResults(5);
    searchResults =
        contentService.findDescriptorsForQuery(mshTerminology, mshVersion,
            query, pfs, authToken);
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfs, DescriptorJpa.class));
    assertTrue(PfsParameterForComponentTest.testPaging(searchResults,
        storedResults, pfs));

    // Paged, sorted results, second page – Pfs parameter with startIndex 6, max
    // results 5 and sortField name
    // TEST: 5 results, matching second set of 5 results from previous test
    pfs.setSortField("name");
    pfs.setStartIndex(5);
    pfs.setMaxResults(5);
    searchResults =
        contentService.findDescriptorsForQuery(mshTerminology, mshVersion,
            query, pfs, authToken);

    assertTrue(PfsParameterForComponentTest.testPaging(searchResults,
        storedResults, pfs));
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfs, DescriptorJpa.class));

    // test lucene query restriction
    pfs = new PfsParameterJpa();
    pfs.setQueryRestriction("terminologyId:C118284");
    searchResults =
        contentService.findDescriptorsForQuery(mshTerminology, mshVersion,
            query, pfs, authToken);

    Logger.getLogger(getClass()).info("QR results: " + searchResults.toString());

    assertTrue(searchResults.getCount() == 1);
    assertTrue(searchResults.getObjects().get(0).getTerminologyId()
        .equals("C118284"));

  }

  /**
   * Test Get and Find methods for codes
   * @throws Exception
   */
  @Test
  public void testNormalUseRestContent003() throws Exception {

    Logger.getLogger(getClass()).debug("Start test");
    
    /** Get codes */
    
    Logger.getLogger(getClass()).info(
        "TEST - " + "D008206, MSH, 2015_2014_09_08, " + authToken);
    Code  c = contentService.getCode("D008206", "MSH", "2015_2014_09_08", authToken);
    assertNotNull(c);
    assertNotEquals(c.getName(),
        "No default preferred name found");

    String snomedTerminology = "SNOMEDCT_US";
    String snomedVersion = "2014_09_01";
    
    Logger.getLogger(getClass()).info(
        "TEST - " + "156371008, SNOMEDCT, 2014_09_01, " + authToken);
    c = contentService.getCode("156371008", snomedTerminology, snomedVersion, authToken);
    assertNotNull(c);
    assertNotEquals(c.getName(),
        "No default preferred name found");
    
    
    /** Find codes for query */

    // For test, execute findCodesForQuery("SNOMEDCT_US", "2014_09_01", "care", ...) for
    // SNOMEDCT
    String query = "care";
    PfsParameterJpa pfs = new PfsParameterJpa();
    SearchResultList searchResults;

    // Raw results – No pfs parameter
    // TEST: 19 results
    searchResults =
        contentService.findCodesForQuery(snomedTerminology, snomedVersion,
            query, pfs, authToken);

    assertTrue(searchResults.getCount() == 19);

    // Sorted results – Pfs parameter with sortField name
    // TEST: 19 results, sorted alphabetically
    pfs.setSortField("name");
    searchResults =
        contentService.findCodesForQuery(snomedTerminology, snomedVersion,
            query, pfs, authToken);
    assertTrue(searchResults.getCount() == 19);
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfs, CodeJpa.class));

    // test descending order
    pfs.setAscending(false);

    searchResults =
        contentService.findCodesForQuery(snomedTerminology, snomedVersion,
            query, pfs, authToken);
    assertTrue(searchResults.getCount() == 19);
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfs, CodeJpa.class));

    // store the sorted results
    SearchResultList storedResults = searchResults;

    // Paged, sorted results, first page – Pfs parameter with max results 5 and
    // sortField name
    // TEST: 5 results, matching first 5 results from previous test
    pfs.setSortField("name");
    pfs.setStartIndex(0);
    pfs.setMaxResults(5);
    searchResults =
        contentService.findCodesForQuery(snomedTerminology, snomedVersion,
            query, pfs, authToken);
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfs, CodeJpa.class));
    assertTrue(PfsParameterForComponentTest.testPaging(searchResults,
        storedResults, pfs));

    // Paged, sorted results, second page – Pfs parameter with startIndex 6, max
    // results 5 and sortField name
    // TEST: 5 results, matching second set of 5 results from previous test
    pfs.setSortField("name");
    pfs.setStartIndex(5);
    pfs.setMaxResults(5);
    searchResults =
        contentService.findCodesForQuery(snomedTerminology, snomedVersion,
            query, pfs, authToken);

    assertTrue(PfsParameterForComponentTest.testPaging(searchResults,
        storedResults, pfs));
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfs, CodeJpa.class));

    // test lucene query restriction
    pfs = new PfsParameterJpa();
    pfs.setQueryRestriction("terminologyId:169559003");
    searchResults =
        contentService.findCodesForQuery(snomedTerminology, snomedVersion,
            query, pfs, authToken);

    Logger.getLogger(getClass()).info("QR results: " + searchResults.toString());

    assertTrue(searchResults.getCount() == 1);
    assertTrue(searchResults.getObjects().get(0).getTerminologyId()
        .equals("169559003"));

  }
  
  /**
   * Test Get and Find methods for lexicalClasses
   * @throws Exception
   */
 /* @Test
  public void testNormalUseRestContent004() throws Exception {

    Logger.getLogger(getClass()).debug("Start test");
    
    *//** Get lexicalClasss *//*

    String umlsTerminology = "UMLS";
    String umlsVersion = "latest";
    
    Logger.getLogger(getClass()).info(
        "TEST - " + "L0035343, UMLS, latest, " + authToken);
    
    LexicalClass c = contentService.getLexicalClass("L0035343", umlsTerminology, umlsVersion, authToken);
    Logger.getLogger(getClass()).info(
        LexicalClassReportHelper.getLexicalClassReport(c));
    assertNotNull(c);
    assertNotEquals(c.getName(),
        "No default preferred name found");
    
    
    *//** Find lexicalClasses for query *//*

    // For test, execute findLexicalClassesForQuery("UMLS", "2014AB", "care", ...) for
    // SNOMEDCT
    String query = "care";
    PfsParameterJpa pfs = new PfsParameterJpa();
    SearchResultList searchResults;

    // Raw results – No pfs parameter
    // TEST: 19 results
    searchResults =
        contentService.findLexicalClassesForQuery(umlsTerminology, umlsVersion,
            query, pfs, authToken);

    assertTrue(searchResults.getCount() == 37);

    // Sorted results – Pfs parameter with sortField name
    // TEST: 37 results, sorted alphabetically
    pfs.setSortField("name");
    searchResults =
        contentService.findLexicalClassesForQuery(umlsTerminology, umlsVersion,
            query, pfs, authToken);
    assertTrue(searchResults.getCount() == 37);
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfs, LexicalClassJpa.class));

    // test descending order
    pfs.setAscending(false);

    searchResults =
        contentService.findLexicalClassesForQuery(umlsTerminology, umlsVersion,
            query, pfs, authToken);
    assertTrue(searchResults.getCount() == 37);
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfs, LexicalClassJpa.class));

    // store the sorted results
    SearchResultList storedResults = searchResults;

    // Paged, sorted results, first page – Pfs parameter with max results 5 and
    // sortField name
    // TEST: 5 results, matching first 5 results from previous test
    pfs.setSortField("name");
    pfs.setStartIndex(0);
    pfs.setMaxResults(5);
    searchResults =
        contentService.findLexicalClassesForQuery(umlsTerminology, umlsVersion,
            query, pfs, authToken);
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfs, LexicalClassJpa.class));
    assertTrue(PfsParameterForComponentTest.testPaging(searchResults,
        storedResults, pfs));

    // Paged, sorted results, second page – Pfs parameter with startIndex 6, max
    // results 5 and sortField name
    // TEST: 5 results, matching second set of 5 results from previous test
    pfs.setSortField("name");
    pfs.setStartIndex(5);
    pfs.setMaxResults(5);
    searchResults =
        contentService.findLexicalClassesForQuery(umlsTerminology, umlsVersion,
            query, pfs, authToken);

    assertTrue(PfsParameterForComponentTest.testPaging(searchResults,
        storedResults, pfs));
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfs, LexicalClassJpa.class));

    // test lucene query restriction
    pfs = new PfsParameterJpa();
    pfs.setQueryRestriction("terminologyId:L0771941");
    searchResults =
        contentService.findLexicalClassesForQuery(umlsTerminology, umlsVersion,
            query, pfs, authToken);

    System.out.println("QR results: " + searchResults.toString());

    assertTrue(searchResults.getCount() == 1);
    assertTrue(searchResults.getObjects().get(0).getTerminologyId()
        .equals("L0771941"));

  }*/
  
  /**
   * Test Get and Find methods for stringClasses
   * @throws Exception
   */
/*  @Test
  public void testNormalUseRestContent005() throws Exception {

    Logger.getLogger(getClass()).debug("Start test");
    
    *//** Get stringClass *//*

    String umlsTerminology = "UMLS";
    String umlsVersion = "latest";
    
    Logger.getLogger(getClass()).info(
        "TEST - " + "S0942156, UMLS, latest, " + authToken);
    StringClass c = contentService.getStringClass("S0942156", umlsTerminology, umlsVersion, authToken);
    Logger.getLogger(getClass()).info(
        StringClassReportHelper.getStringClassReport(c));
    assertNotNull(c);
    assertNotEquals(c.getName(),
        "No default preferred name found");
    
    
    *//** Find stringClasses for query *//*

    // For test, execute findStringClassesForQuery("UMLS", "2014AB", "care", ...) for
    // SNOMEDCT
    String query = "care";
    PfsParameterJpa pfs = new PfsParameterJpa();
    SearchResultList searchResults;

    // Raw results – No pfs parameter
    // TEST: 19 results
    searchResults =
        contentService.findStringClassesForQuery(umlsTerminology, umlsVersion,
            query, pfs, authToken);

    assertTrue(searchResults.getCount() == 48);

    // Sorted results – Pfs parameter with sortField name
    // TEST: 37 results, sorted alphabetically
    pfs.setSortField("name");
    searchResults =
        contentService.findStringClassesForQuery(umlsTerminology, umlsVersion,
            query, pfs, authToken);
    assertTrue(searchResults.getCount() == 48);
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfs, StringClassJpa.class));

    // test descending order
    pfs.setAscending(false);

    searchResults =
        contentService.findStringClassesForQuery(umlsTerminology, umlsVersion,
            query, pfs, authToken);
    assertTrue(searchResults.getCount() == 48);
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfs, StringClassJpa.class));

    // store the sorted results
    SearchResultList storedResults = searchResults;

    // Paged, sorted results, first page – Pfs parameter with max results 5 and
    // sortField name
    // TEST: 5 results, matching first 5 results from previous test
    pfs.setSortField("name");
    pfs.setStartIndex(0);
    pfs.setMaxResults(5);
    searchResults =
        contentService.findStringClassesForQuery(umlsTerminology, umlsVersion,
            query, pfs, authToken);
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfs, StringClassJpa.class));
    assertTrue(PfsParameterForComponentTest.testPaging(searchResults,
        storedResults, pfs));

    // Paged, sorted results, second page – Pfs parameter with startIndex 6, max
    // results 5 and sortField name
    // TEST: 5 results, matching second set of 5 results from previous test
    pfs.setSortField("name");
    pfs.setStartIndex(5);
    pfs.setMaxResults(5);
    searchResults =
        contentService.findStringClassesForQuery(umlsTerminology, umlsVersion,
            query, pfs, authToken);

    assertTrue(PfsParameterForComponentTest.testPaging(searchResults,
        storedResults, pfs));
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfs, StringClassJpa.class));

    // test lucene query restriction
    pfs = new PfsParameterJpa();
    pfs.setQueryRestriction("terminologyId:S0942156");
    searchResults =
        contentService.findStringClassesForQuery(umlsTerminology, umlsVersion,
            query, pfs, authToken);

    System.out.println("QR results: " + searchResults.toString());

    assertTrue(searchResults.getCount() == 1);
    assertTrue(searchResults.getObjects().get(0).getTerminologyId()
        .equals("S0942156"));

  }*/
 
  /**
   * Test Get and Find methods for subsets
   * @throws Exception
   */
  // TODO: figure out why this is causing a 415 Unsupported Media Type exception
  /*@Test
  public void testNormalUseRestContent006() throws Exception {

    Logger.getLogger(getClass()).debug("Start test");
    
    *//** Get codes *//*
    Logger.getLogger(getClass()).info(
        "TEST - " + "166113012, SNOMEDCT_US, 2014_09_01, " + authToken);
    String snomedTerminology = "SNOMEDCT_US";
    String snomedVersion = "2014_09_01";

    SubsetMemberList sml = contentService.getSubsetMembersForAtom("166113012", snomedTerminology, snomedVersion, authToken);
    
    assertNotNull(sml);

  } */
  
    /**
     * Test Find methods for relationships
     * @throws Exception
     */
    @Test
    // TODO: figure out why this is causing a 415 Unsupported Media Type exception
    public void testNormalUseRestContent007() throws Exception {

      Logger.getLogger(getClass()).debug("Start test");
      
      /** Get relationships for concept */
      Logger.getLogger(getClass()).info(
          "TEST - " + "198664006, SNOMEDCT_US, 2014_09_01, " + authToken);
      String snomedTerminology = "SNOMEDCT_US";
      String snomedVersion = "2014_09_01";

      RelationshipList sml = contentService.findRelationshipsForConcept("198664006", snomedTerminology, snomedVersion, new PfsParameterJpa(), authToken);

      assertNotNull(sml);
      /*assertNotEquals(c.getName(),
          "No default preferred name found");
  */
      // TODO: test other findRelationshipsFor... on other components
  }
  
    /**
     * Test variations of findConceptsForQuery()
     * @throws Exception
     */
    @Test
    public void testNormalUseRestContent008() throws Exception {

      Logger.getLogger(getClass()).debug("Start test");
      

      String snomedTerminology = "SNOMEDCT_US";
      String snomedVersion = "2014_09_01";

      SearchCriteria sc = new SearchCriteriaJpa();
      sc.setActiveOnly(true);
      List<SearchCriteria> scl = new ArrayList<SearchCriteria>();
      scl.add(sc);
      PfsParameterJpa pfs = new PfsParameterJpa();
      pfs.setSearchCriteria(scl);
      
      /** Get concepts with no query and search criteria active-only, no pfs max*/
      Logger.getLogger(getClass()).info(
          "TEST1 - " + "SNOMEDCT_US, 2014_09_01, " + pfs + " - " + authToken);
      SearchResultList srl = contentService.findConceptsForQuery(snomedTerminology, snomedVersion, "", pfs, authToken);
      Logger.getLogger(getClass()).info(
          "Result count: " + srl.getCount());
     // TODO: assert correct number of results
      
      sc = new SearchCriteriaJpa();
      sc.setActiveOnly(true);
      scl = new ArrayList<SearchCriteria>();
      scl.add(sc);
      pfs = new PfsParameterJpa();
      pfs.setSearchCriteria(scl);
      pfs.setStartIndex(0);
      pfs.setMaxResults(10);
      
      /** Get concepts with no query and search criteria active-only */
      Logger.getLogger(getClass()).info(
          "TEST2 - " + "SNOMEDCT_US, 2014_09_01, " + pfs + " - " + authToken);
      srl = contentService.findConceptsForQuery(snomedTerminology, snomedVersion, "", pfs, authToken);
      assertTrue(srl.getCount() == 10);
      Logger.getLogger(getClass()).info(
          "Result count: " + srl.getCount());
      
      /** Get concepts with no query and inactive-only */
      sc = new SearchCriteriaJpa();
      sc.setInactiveOnly(true);
      scl = new ArrayList<SearchCriteria>();
      scl.add(sc);
      pfs = new PfsParameterJpa();
      pfs.setSearchCriteria(scl);
      pfs.setStartIndex(0);
      pfs.setMaxResults(10);
      Logger.getLogger(getClass()).info(
          "TEST3 - " + "SNOMEDCT_US, 2014_09_01, " + pfs + " - " + authToken);
      srl = contentService.findConceptsForQuery(snomedTerminology, snomedVersion, "", pfs, authToken);
      assertTrue(srl.getCount() == 0);
      Logger.getLogger(getClass()).info(
          "Result count: " + srl.getCount());
      
      /** Get concepts with no query and two search criteria, active-only and primitive-only */
      sc = new SearchCriteriaJpa();
      sc.setActiveOnly(true);
      sc.setPrimitiveOnly(true);
      scl = new ArrayList<SearchCriteria>();
      scl.add(sc);
      pfs = new PfsParameterJpa();
      pfs.setSearchCriteria(scl);
      pfs.setStartIndex(0);
      pfs.setMaxResults(10);
      Logger.getLogger(getClass()).info(
          "TEST4 - " + "SNOMEDCT_US, 2014_09_01, " + pfs + " - " + authToken);
      srl = contentService.findConceptsForQuery(snomedTerminology, snomedVersion, "", pfs, authToken);
      assertTrue(srl.getCount() == 10);
      Logger.getLogger(getClass()).info(
          "Result count: " + srl.getCount());
  

      /** Get concepts with query "disease" and search criteria active-only */
      sc = new SearchCriteriaJpa();
      sc.setActiveOnly(true);
      scl = new ArrayList<SearchCriteria>();
      scl.add(sc);
      pfs = new PfsParameterJpa();
      pfs.setSearchCriteria(scl);
      pfs.setStartIndex(0);
      pfs.setMaxResults(10);
      Logger.getLogger(getClass()).info(
          "TEST5 - " + "SNOMEDCT_US, 2014_09_01, " + pfs + " - " + authToken);
      srl = contentService.findConceptsForQuery(snomedTerminology, snomedVersion, "disease", pfs, authToken);
      assertTrue(srl.getCount() == 10);
      Logger.getLogger(getClass()).info(
          "Result count: " + srl.getCount());
      for (SearchResult sr : srl.getObjects()) {
        assertTrue(sr.getValue().contains("disease"));
      }
      
      /** Get concepts with query "disease" and inactive-only */
      sc = new SearchCriteriaJpa();
      sc.setInactiveOnly(true);
      scl = new ArrayList<SearchCriteria>();
      scl.add(sc);
      pfs = new PfsParameterJpa();
      pfs.setSearchCriteria(scl);
      pfs.setStartIndex(0);
      pfs.setMaxResults(10);
      Logger.getLogger(getClass()).info(
          "TEST6 - " + "SNOMEDCT_US, 2014_09_01, " + pfs + " - " + authToken);
      srl = contentService.findConceptsForQuery(snomedTerminology, snomedVersion, "disease", pfs, authToken);
      assertTrue(srl.getCount() == 0);
      Logger.getLogger(getClass()).info(
          "Result count: " + srl.getCount());
      
      /** Get concepts with query "disease" and two search criteria, active-only and primitive-only */
      sc = new SearchCriteriaJpa();
      sc.setActiveOnly(true);
      sc.setPrimitiveOnly(true);
      scl = new ArrayList<SearchCriteria>();
      scl.add(sc);
      pfs = new PfsParameterJpa();
      pfs.setSearchCriteria(scl);
      pfs.setStartIndex(0);
      pfs.setMaxResults(10);
      Logger.getLogger(getClass()).info(
          "TEST7 - " + "SNOMEDCT_US, 2014_09_01, " + pfs + " - " + authToken);
      srl = contentService.findConceptsForQuery(snomedTerminology, snomedVersion, "disease", pfs, authToken);
      Logger.getLogger(getClass()).info(
          "Result count: " + srl.getCount());
      for (SearchResult sr : srl.getObjects()) {
        Logger.getLogger(getClass()).info("Result: " + sr.getTerminologyId());
      }
      assertTrue(srl.getCount() == 10);
      for (SearchResult sr : srl.getObjects()) {
        assertTrue(sr.getValue().contains("disease"));
      }
      
      /** Get "to" concepts given "from" concept and relationship type */
      sc = new SearchCriteriaJpa();
      sc.setFindToByRelationshipFromAndType("isa", "361352008", false);
      scl = new ArrayList<SearchCriteria>();
      scl.add(sc);
      pfs = new PfsParameterJpa();
      pfs.setSearchCriteria(scl);
      pfs.setStartIndex(0);
      pfs.setMaxResults(10);
      Logger.getLogger(getClass()).info(
          "TEST8 - " + "SNOMEDCT_US, 2014_09_01, " + pfs + " - " + authToken);
      srl = contentService.findConceptsForQuery(snomedTerminology, snomedVersion, "", pfs, authToken);
      Logger.getLogger(getClass()).info(
          "Result count: " + srl.getCount());
      for (SearchResult sr : srl.getObjects()) {
        assertTrue(sr.getValue().contains("muscle"));
        Logger.getLogger(getClass()).info("Result: " + sr.getTerminologyId());
      }
      assertTrue(srl.getCount() == 2);
      
      /** Get "to" concepts given "from" concept and relationship type with transitive relationships*/
      sc = new SearchCriteriaJpa();
      sc.setFindToByRelationshipFromAndType("isa", "195879000", false);
      sc.setRelationshipDescendantsFlag(true);
      scl = new ArrayList<SearchCriteria>();
      scl.add(sc);
      pfs = new PfsParameterJpa();
      pfs.setSearchCriteria(scl);
      pfs.setStartIndex(0);
      pfs.setMaxResults(10);
      Logger.getLogger(getClass()).info(
          "TEST9 - " + "SNOMEDCT_US, 2014_09_01, " + pfs + " - " + authToken);
      srl = contentService.findConceptsForQuery(snomedTerminology, snomedVersion, "", pfs, authToken);
      Logger.getLogger(getClass()).info(
          "Result count: " + srl.getCount());
      for (SearchResult sr : srl.getObjects()) {
        Logger.getLogger(getClass()).info("Result: " + sr.getTerminologyId());
      }
      assertTrue(srl.getCount() == 4);
      
      /** Get "to" concepts given "from" concept and relationship type with descendants*/
      sc = new SearchCriteriaJpa();
      sc.setFindToByRelationshipFromAndType("isa", "361352008", false);
      sc.setFindDescendants(true);
      scl = new ArrayList<SearchCriteria>();
      scl.add(sc);
      pfs = new PfsParameterJpa();
      pfs.setSearchCriteria(scl);
      pfs.setStartIndex(0);
      pfs.setMaxResults(10);
      Logger.getLogger(getClass()).info(
          "TEST10 - " + "SNOMEDCT_US, 2014_09_01, " + pfs + " - " + authToken);
      srl = contentService.findConceptsForQuery(snomedTerminology, snomedVersion, "", pfs, authToken);
      Logger.getLogger(getClass()).info(
          "Result count: " + srl.getCount());
      for (SearchResult sr : srl.getObjects()) {
        Logger.getLogger(getClass()).info("Result: " + sr.getTerminologyId());
      }
      assertTrue(srl.getCount() == 8);
      
      /** Get "to" concepts given "from" concept and relationship type with descendants and self*/
      sc = new SearchCriteriaJpa();
      sc.setFindToByRelationshipFromAndType("isa", "361352008", false);
      sc.setFindDescendants(true);
      sc.setFindSelf(true);
      scl = new ArrayList<SearchCriteria>();
      scl.add(sc);
      pfs = new PfsParameterJpa();
      pfs.setSearchCriteria(scl);
      pfs.setStartIndex(0);
      pfs.setMaxResults(10);
      Logger.getLogger(getClass()).info(
          "TEST11 - " + "SNOMEDCT_US, 2014_09_01, " + pfs + " - " + authToken);
      srl = contentService.findConceptsForQuery(snomedTerminology, snomedVersion, "", pfs, authToken);
      Logger.getLogger(getClass()).info(
          "Result count: " + srl.getCount());
      for (SearchResult sr : srl.getObjects()) {
        Logger.getLogger(getClass()).info("Result: " + sr.getTerminologyId());
      }
      assertTrue(srl.getCount() == 10);
      
      /** Get "to" concepts given "from" concept and relationship type with transitive relationships and descendants*/
      sc = new SearchCriteriaJpa();
      sc.setFindToByRelationshipFromAndType("isa", "195879000", false);
      sc.setRelationshipDescendantsFlag(true);
      sc.setFindDescendants(true);
      scl = new ArrayList<SearchCriteria>();
      scl.add(sc);
      pfs = new PfsParameterJpa();
      pfs.setSearchCriteria(scl);
      pfs.setStartIndex(0);
      pfs.setMaxResults(10);
      Logger.getLogger(getClass()).info(
          "TEST11a - " + "SNOMEDCT_US, 2014_09_01, " + pfs + " - " + authToken);
      srl = contentService.findConceptsForQuery(snomedTerminology, snomedVersion, "", pfs, authToken);
      Logger.getLogger(getClass()).info(
          "Result count: " + srl.getCount());
      for (SearchResult sr : srl.getObjects()) {
        Logger.getLogger(getClass()).info("Result: " + sr.getTerminologyId());
      }
      assertTrue(srl.getCount() == 10);
      
      /** Get "from" concepts given "to" concept and relationship type */
      sc = new SearchCriteriaJpa();
      sc.setFindFromByRelationshipTypeAndTo("isa", "195879000", false);
      scl = new ArrayList<SearchCriteria>();
      scl.add(sc);
      pfs = new PfsParameterJpa();
      pfs.setSearchCriteria(scl);
      pfs.setStartIndex(0);
      pfs.setMaxResults(10);
      Logger.getLogger(getClass()).info(
          "TEST12 - " + "SNOMEDCT_US, 2014_09_01, " + pfs + " - " + authToken);
      srl = contentService.findConceptsForQuery(snomedTerminology, snomedVersion, "", pfs, authToken);
      Logger.getLogger(getClass()).info(
          "Result count: " + srl.getCount());
      for (SearchResult sr : srl.getObjects()) {
        Logger.getLogger(getClass()).info("Result: " + sr.getTerminologyId());
      }
      for (SearchResult sr : srl.getObjects()) {
        assertTrue(sr.getValue().contains("muscle"));
      }
      assertTrue(srl.getCount() == 1);
      
      /** Get "from" concepts given "to" concept and relationship type with descendants*/
      sc = new SearchCriteriaJpa();
      sc.setFindFromByRelationshipTypeAndTo("isa", "195879000", false);
      sc.setRelationshipDescendantsFlag(true);
      scl = new ArrayList<SearchCriteria>();
      scl.add(sc);
      pfs = new PfsParameterJpa();
      pfs.setSearchCriteria(scl);
      pfs.setStartIndex(0);
      pfs.setMaxResults(10);
      Logger.getLogger(getClass()).info(
          "TEST13 - " + "SNOMEDCT_US, 2014_09_01, " + pfs + " - " + authToken);
      srl = contentService.findConceptsForQuery(snomedTerminology, snomedVersion, "", pfs, authToken);
      Logger.getLogger(getClass()).info(
          "Result count: " + srl.getCount());
      for (SearchResult sr : srl.getObjects()) {
        Logger.getLogger(getClass()).info("Result: " + sr.getTerminologyId());
      }
      assertTrue(srl.getCount() == 1);
  }
   
    /**
     * Test Get and Find methods for getAtomSubsets and getConceptSubsets
     * @throws Exception
     */
    @Test
    public void testNormalUseRestContent009() throws Exception {

      Logger.getLogger(getClass()).debug("Start test");
      
      /** Get codes */
      Logger.getLogger(getClass()).info(
          "TEST - " + "SNOMEDCT_US, 2014_09_01, " + authToken);
      String snomedTerminology = "SNOMEDCT_US";
      String snomedVersion = "2014_09_01";

      SubsetList sml = contentService.getAtomSubsets(snomedTerminology, snomedVersion, authToken);
      assertNotNull(sml);

      sml = contentService.getConceptSubsets(snomedTerminology, snomedVersion, authToken);
      assertNotNull(sml);
  }
    
    
    /**
     * Test variations of findDescriptorsForQuery()
     * @throws Exception
     */
    @Test
    public void testNormalUseRestContent010() throws Exception {

      Logger.getLogger(getClass()).debug("Start test");
      

      String mshTerminology = "MSH";
      String mshVersion = "2015_2014_09_08";

      SearchCriteria sc = new SearchCriteriaJpa();
      sc.setActiveOnly(true);
      List<SearchCriteria> scl = new ArrayList<SearchCriteria>();
      scl.add(sc);
      PfsParameterJpa pfs = new PfsParameterJpa();
      pfs.setSearchCriteria(scl);
      
      /** Get descriptors with no query and search criteria active-only, no pfs max*/
      Logger.getLogger(getClass()).info(
          "TEST1 - " + "MSH, 2015_2014_09_08, " + pfs + " - " + authToken);
      SearchResultList srl = contentService.findDescriptorsForQuery(mshTerminology, mshVersion, "", pfs, authToken);
      Logger.getLogger(getClass()).info(
          "Result count: " + srl.getCount());
     // TODO: assert correct number of results
      
      sc = new SearchCriteriaJpa();
      sc.setActiveOnly(true);
      scl = new ArrayList<SearchCriteria>();
      scl.add(sc);
      pfs = new PfsParameterJpa();
      pfs.setSearchCriteria(scl);
      pfs.setStartIndex(0);
      pfs.setMaxResults(10);
      
      /** Get descriptors with no query and search criteria active-only */
      Logger.getLogger(getClass()).info(
          "TEST2 - " + "MSH, 2015_2014_09_08, " + pfs + " - " + authToken);
      srl = contentService.findDescriptorsForQuery(mshTerminology, mshVersion, "", pfs, authToken);
      assertTrue(srl.getCount() == 10);
      Logger.getLogger(getClass()).info(
          "Result count: " + srl.getCount());
      
      /** Get descriptors with no query and inactive-only */
      sc = new SearchCriteriaJpa();
      sc.setInactiveOnly(true);
      scl = new ArrayList<SearchCriteria>();
      scl.add(sc);
      pfs = new PfsParameterJpa();
      pfs.setSearchCriteria(scl);
      pfs.setStartIndex(0);
      pfs.setMaxResults(10);
      Logger.getLogger(getClass()).info(
          "TEST3 - " + "MSH, 2015_2014_09_08, " + pfs + " - " + authToken);
      srl = contentService.findDescriptorsForQuery(mshTerminology, mshVersion, "", pfs, authToken);
      assertTrue(srl.getCount() == 0);
      Logger.getLogger(getClass()).info(
          "Result count: " + srl.getCount());
      
      /** Get descriptors with no query and two search criteria, active-only and primitive-only */
      sc = new SearchCriteriaJpa();
      sc.setActiveOnly(true);
      sc.setPrimitiveOnly(true);
      scl = new ArrayList<SearchCriteria>();
      scl.add(sc);
      pfs = new PfsParameterJpa();
      pfs.setSearchCriteria(scl);
      pfs.setStartIndex(0);
      pfs.setMaxResults(10);
      Logger.getLogger(getClass()).info(
          "TEST4 - " + "MSH, 2015_2014_09_08, " + pfs + " - " + authToken);
      srl = contentService.findDescriptorsForQuery(mshTerminology, mshVersion, "", pfs, authToken);
      assertTrue(srl.getCount() == 10);
      Logger.getLogger(getClass()).info(
          "Result count: " + srl.getCount());
  

      /** Get descriptors with query "disease" and search criteria active-only */
      sc = new SearchCriteriaJpa();
      sc.setActiveOnly(true);
      scl = new ArrayList<SearchCriteria>();
      scl.add(sc);
      pfs = new PfsParameterJpa();
      pfs.setSearchCriteria(scl);
      pfs.setStartIndex(0);
      pfs.setMaxResults(10);
      Logger.getLogger(getClass()).info(
          "TEST5 - " + "MSH, 2015_2014_09_08, " + pfs + " - " + authToken);
      srl = contentService.findDescriptorsForQuery(mshTerminology, mshVersion, "disease", pfs, authToken);
      assertTrue(srl.getCount() == 8);
      Logger.getLogger(getClass()).info(
          "Result count: " + srl.getCount());
      for (SearchResult sr : srl.getObjects()) {
        Logger.getLogger(getClass()).info("Result: " + sr.getTerminologyId() + " " + sr.getValue());
      }
      for (SearchResult sr : srl.getObjects()) {
        assertTrue(sr.getValue().contains("isease"));
      }
      
      /** Get descriptors with query "disease" and inactive-only */
      sc = new SearchCriteriaJpa();
      sc.setInactiveOnly(true);
      scl = new ArrayList<SearchCriteria>();
      scl.add(sc);
      pfs = new PfsParameterJpa();
      pfs.setSearchCriteria(scl);
      pfs.setStartIndex(0);
      pfs.setMaxResults(10);
      Logger.getLogger(getClass()).info(
          "TEST6 - " + "MSH, 2015_2014_09_08, " + pfs + " - " + authToken);
      srl = contentService.findDescriptorsForQuery(mshTerminology, mshVersion, "disease", pfs, authToken);
      assertTrue(srl.getCount() == 0);
      Logger.getLogger(getClass()).info(
          "Result count: " + srl.getCount());
      for (SearchResult sr : srl.getObjects()) {
        Logger.getLogger(getClass()).info("Result: " + sr.getTerminologyId() + " " + sr.getValue());
      }
      
      /** Get descriptors with query "disease" and two search criteria, active-only and primitive-only */
      sc = new SearchCriteriaJpa();
      sc.setActiveOnly(true);
      sc.setPrimitiveOnly(true);
      scl = new ArrayList<SearchCriteria>();
      scl.add(sc);
      pfs = new PfsParameterJpa();
      pfs.setSearchCriteria(scl);
      pfs.setStartIndex(0);
      pfs.setMaxResults(10);
      Logger.getLogger(getClass()).info(
          "TEST7 - " + "MSH, 2015_2014_09_08, " + pfs + " - " + authToken);
      srl = contentService.findDescriptorsForQuery(mshTerminology, mshVersion, "disease", pfs, authToken);
      assertTrue(srl.getCount() == 8);
      Logger.getLogger(getClass()).info(
          "Result count: " + srl.getCount());
      for (SearchResult sr : srl.getObjects()) {
        Logger.getLogger(getClass()).info("Result: " + sr.getTerminologyId() + " " + sr.getValue());
      }
      for (SearchResult sr : srl.getObjects()) {
        assertTrue(sr.getValue().contains("isease"));
      }
  }
    
  /**
   * Test transitive closure methods (ancestors, descendants, children)
   * @throws Exception
   */
  @Test
  public void testNormalUseRestContent011() throws Exception {

    PfsParameterJpa pfs = new PfsParameterJpa();
    ConceptList conceptList;

    Logger.getLogger(getClass()).debug("Start test");
    

    String snomedTerminology = "SNOMEDCT_US";
    String snomedVersion = "2014_09_01";
    String snomedTestId = "26864007";
    

    // Get descendants for SNOMEDCT concept 
    conceptList =
        contentService.findDescendantConcepts(snomedTestId, snomedTerminology,
            snomedVersion, false, pfs, authToken);
    Logger.getLogger(getClass()).info(
        "Result count: " + conceptList.getCount());
    assertTrue(conceptList.getCount() == 2);

    // Get ancestors for SNOMEDCT concept
    // TODO: based on samplemeta in db, should return count == 1, but returns count == 0
    conceptList =
        contentService.findAncestorConcepts(snomedTestId, snomedTerminology,
            snomedVersion, false, pfs, authToken);
    Logger.getLogger(getClass()).info(
        "Result count: " + conceptList.getCount());

    //assertTrue(conceptList.getCount() == 11);

  }

  @Test
  public void testNormalUseRestContent012() throws Exception {

    Logger.getLogger(getClass()).debug("Start test");
    
    /** Find concepts with hql query */
    Logger.getLogger(getClass()).info(
        "TEST - " + ", SNOMEDCT_US, 2014_09_01, " + authToken);
    String snomedTerminology = "SNOMEDCT_US";
    String snomedVersion = "2014_09_01";

    SearchResultList sml = contentService.findConceptsForQuery("null", "null", new PfsParameterJpa(), authToken);
//SELECT c.terminologyId FROM ConceptJpa c
    assertNotNull(sml);
    /*assertNotEquals(c.getName(),
        "No default preferred name found");
*/
    // TODO: test other findRelationshipsFor... on other components
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

}
