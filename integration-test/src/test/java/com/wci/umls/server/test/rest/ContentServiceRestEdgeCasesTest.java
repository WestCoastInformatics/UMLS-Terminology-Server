/*
 * Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package com.wci.umls.server.test.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.wci.umls.server.helpers.SearchCriteria;
import com.wci.umls.server.helpers.SearchResult;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.helpers.content.SubsetList;
import com.wci.umls.server.helpers.content.SubsetMemberList;
import com.wci.umls.server.jpa.content.CodeJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.DescriptorJpa;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.helpers.PfscParameterJpa;
import com.wci.umls.server.jpa.helpers.SearchCriteriaJpa;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.Subset;
import com.wci.umls.server.model.content.SubsetMember;
import com.wci.umls.server.test.helpers.PfsParameterForComponentTest;

/**
 * Implementation of the "Content Service REST Edge Cases" Test Cases.
 */
public class ContentServiceRestEdgeCasesTest extends ContentServiceRestTest {

  /** The auth token. */
  private static String authToken;

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
   * Test edge cases for getConcept(...)
   *
   * @throws Exception the exception
   */
  @Test
  public void testEdgeCasesRestContent001() throws Exception {
    // n/a
  }

  /**
   * Test edge cases for getDescriptor(...)
   *
   * @throws Exception the exception
   */
  @Test
  public void testEdgeCasesRestContent002() throws Exception {
    // n/a
  }

  /**
   * Test Test edge cases for getCode(...)
   *
   * @throws Exception the exception
   */
  @Test
  public void testEdgeCasesRestContent003() throws Exception {
    // n/a
  }

  /**
   * Test edge cases for getLexicalClass(...)
   * @throws Exception
   */
  @Test
  public void testEdgeCasesRestContent004() throws Exception {
    // n/a
  }

  /**
   * Test edge cases for getStringClass(...)
   * @throws Exception
   */
  @Test
  public void testEdgeCasesRestContent005() throws Exception {
    // n/a
  }

  /**
   * Test edge cases for "get" methods for atom subsets
   * @throws Exception
   */
  @Test
  public void testEdgeCasesRestContent006() throws Exception {

    Logger.getLogger(getClass()).debug("Start test");

    // test high end of pfs retrieval with pfs.setStartIndex()
    SubsetList list =
        contentService.getAtomSubsets(snomedTerminology, snomedVersion,
            authToken);
    assertEquals(3, list.getCount());
    PfsParameterJpa pfs = new PfsParameterJpa();
    pfs.setStartIndex(12680);
    pfs.setMaxResults(20);
    for (Subset subset : list.getObjects()) {
      assertTrue(subset.isPublished());
      assertTrue(subset.isPublishable());
      assertFalse(subset.isObsolete());
      assertFalse(subset.isSuppressible());
      assertFalse(subset.isDisjointSubset());
      assertEquals(0, subset.getAttributes().size());
      assertEquals(subset.getDescription(), subset.getName());
      assertEquals(snomedTerminology, subset.getTerminology());
      assertEquals(snomedVersion, subset.getVersion());
      if (subset.getName().equals("GB English")) {
        assertEquals("900000000000508004", subset.getTerminologyId());
        // Get members
        SubsetMemberList memberList =
            contentService.findAtomSubsetMembers(subset.getTerminologyId(),
                snomedTerminology, snomedVersion, null, pfs, authToken);
        assertEquals(20, memberList.getCount());
        assertEquals(12694, memberList.getTotalCount());

      } 
    }

  }

  /**
   * Test edge cases for "get" methods for concept subsets
   * @throws Exception
   */
  @Test
  public void testEdgeCasesRestContent007() throws Exception {

    Logger.getLogger(getClass()).debug("Start test");

    // test high end of pfs retrieval with pfs.setStartIndex()    
    SubsetList list =
        contentService.getConceptSubsets(snomedTerminology, snomedVersion,
            authToken);
    assertEquals(14, list.getCount());
    PfsParameterJpa pfs = new PfsParameterJpa();
    pfs.setStartIndex(1009);
    pfs.setMaxResults(20);
    for (Subset subset : list.getObjects()) {
      System.out.println(subset.getName());
      assertTrue(subset.isPublished());
      assertTrue(subset.isPublishable());
      assertFalse(subset.isDisjointSubset());
      assertEquals(0, subset.getAttributes().size());
      assertEquals(subset.getDescription(), subset.getName());
      assertEquals(snomedTerminology, subset.getTerminology());
      assertEquals(snomedVersion, subset.getVersion());
      if (subset.getName().equals("SAME AS association reference set")) {
        assertFalse(subset.isObsolete());
        assertFalse(subset.isSuppressible());
        assertEquals("900000000000527005", subset.getTerminologyId());
        // Get members
        SubsetMemberList memberList =
            contentService.findConceptSubsetMembers(subset.getTerminologyId(),
                snomedTerminology, snomedVersion, null, pfs, authToken);
        assertEquals(20, memberList.getCount());
        assertEquals(1029, memberList.getTotalCount());
      } 
    }
  }

  /**
   * Test edge cases for "find" concepts for query.
   * @throws Exception
   */
  @Test
  public void testEdgeCasesRestContent008() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

    PfscParameterJpa pfsc = new PfscParameterJpa();
    SearchResultList searchResults;


    // Simple query with spaces, empty pfs
    Logger.getLogger(getClass()).info("  Simple query, empty pfs");
    searchResults =
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            "heart disease", null, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(133, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(133, searchResults.getCount());



    // Simple query, sorted on name
    Logger.getLogger(getClass()).info("  Simple query, sorted on name");
    pfsc.setSortField("name");
    searchResults =
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            "care", pfsc, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(19, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(19, searchResults.getCount());
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfsc,
        ConceptJpa.class));

    // Simple query, sorted on name, descending order
    Logger.getLogger(getClass()).info(
        "  Simple query, sorted on name, descending order");
    pfsc.setAscending(false);
    searchResults =
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            "care", pfsc, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(19, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(19, searchResults.getCount());
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfsc,
        ConceptJpa.class));

    // store the sorted results for later comparison
    SearchResultList sortedResults = searchResults;

    // Simple query, paged and sorted results, first page
    Logger.getLogger(getClass()).info(
        "  Simple query, paged and sorted results, first page");
    pfsc.setSortField("name");
    pfsc.setStartIndex(0);
    pfsc.setMaxResults(5);
    searchResults =
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            "care", pfsc, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(19, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfsc,
        ConceptJpa.class));
    assertTrue(PfsParameterForComponentTest.testPaging(searchResults,
        sortedResults, pfsc));

    // Simple query, paged and sorted results, second page
    Logger.getLogger(getClass()).info(
        "  Simple query, paged and sorted results, second page");
    pfsc.setSortField("name");
    pfsc.setStartIndex(5);
    pfsc.setMaxResults(5);
    searchResults =
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            "care", pfsc, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(19, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertTrue(PfsParameterForComponentTest.testPaging(searchResults,
        sortedResults, pfsc));
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfsc,
        ConceptJpa.class));

    // Simple query, query restriction
    Logger.getLogger(getClass()).info("  Simple query, query restriction");
    pfsc = new PfscParameterJpa();
    pfsc.setQueryRestriction("terminologyId:169559003");
    searchResults =
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            "care", pfsc, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(1, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(1, searchResults.getCount());
    assertTrue(searchResults.getObjects().get(0).getTerminologyId()
        .equals("169559003"));

    SearchCriteria sc = new SearchCriteriaJpa();

    // Simple query, for "active only", empty pfs
    Logger.getLogger(getClass()).info(
        "  Simple query, for \"active only\", empty pfs");
    pfsc = new PfscParameterJpa();
    pfsc.setActiveOnly(true);
    searchResults =
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            "care", pfsc, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(19, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }

    // No query active only, first page
    Logger.getLogger(getClass()).info("  No query active only, first page");
    pfsc = new PfscParameterJpa();
    pfsc.setActiveOnly(true);
    pfsc.setStartIndex(0);
    pfsc.setMaxResults(10);
    searchResults =
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            null, pfsc, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(3902, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(10, searchResults.getCount());

    // No query, inactive only, first page
    Logger.getLogger(getClass()).info("  No query, inactive only, first page");
    pfsc = new PfscParameterJpa();
    pfsc.setInactiveOnly(true);
    pfsc.setStartIndex(0);
    pfsc.setMaxResults(10);
    searchResults =
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            null, pfsc, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(0, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(0, searchResults.getCount());

    // No query, active and primitive only, first page
    Logger.getLogger(getClass()).info(
        "  No query, active and primitive only, first page");
    sc = new SearchCriteriaJpa();
    sc.setPrimitiveOnly(true);
    pfsc = new PfscParameterJpa();
    pfsc.setActiveOnly(true);
    pfsc.addSearchCriteria(sc);
    pfsc.setStartIndex(0);
    pfsc.setMaxResults(10);
    searchResults =
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            "", pfsc, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(3902, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(10, searchResults.getCount());

    // Simple query, active only, first page
    Logger.getLogger(getClass())
        .info("  Simple query, active only, first page");
    pfsc = new PfscParameterJpa();
    pfsc.setActiveOnly(true);
    pfsc.setStartIndex(0);
    pfsc.setMaxResults(10);
    searchResults =
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            "disease", pfsc, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(133, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(10, searchResults.getCount());
    for (SearchResult sr : searchResults.getObjects()) {
      assertTrue(sr.getValue().contains("disease"));
    }

    // Simple query, inactive only, first page
    Logger.getLogger(getClass()).info(
        "  Simple query, inactive only, first page");
    pfsc = new PfscParameterJpa();
    pfsc.setInactiveOnly(true);
    pfsc.setStartIndex(0);
    pfsc.setMaxResults(10);
    searchResults =
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            "disease", pfsc, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(0, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(0, searchResults.getCount());

    // Simple query, active and primitive only, first page
    Logger.getLogger(getClass()).info(
        "  Simple query, active and primitive only, first page");
    sc = new SearchCriteriaJpa();
    sc.setPrimitiveOnly(true);
    pfsc = new PfscParameterJpa();
    pfsc.setActiveOnly(true);
    pfsc.addSearchCriteria(sc);
    pfsc.setStartIndex(0);
    pfsc.setMaxResults(10);
    searchResults =
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            "disease", pfsc, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(133, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(10, searchResults.getCount());
    for (SearchResult sr : searchResults.getObjects()) {
      assertTrue(sr.getValue().contains("disease"));
    }

    // No query, find "to" relationship from/type specified, first page
    Logger.getLogger(getClass()).info(
        "  No query, find \"to\" relationship from/type specified, first page");
    sc = new SearchCriteriaJpa();
    sc.setFindToByRelationshipFromAndType("isa", "361352008", false);
    pfsc = new PfscParameterJpa();
    pfsc.addSearchCriteria(sc);
    pfsc.setStartIndex(0);
    pfsc.setMaxResults(10);
    searchResults =
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            "", pfsc, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(2, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(2, searchResults.getCount());
    for (SearchResult sr : searchResults.getObjects()) {
      assertTrue(sr.getValue().contains("muscle"));
    }

    // No query, find "to" relationship from/type specified (with descendants),
    // first page
    Logger
        .getLogger(getClass())
        .info(
            "  No query, find \"to\" relationship from/type specified (with descendants), first page");
    sc = new SearchCriteriaJpa();
    sc.setFindToByRelationshipFromAndType("isa", "195879000", true);
    pfsc = new PfscParameterJpa();
    pfsc.addSearchCriteria(sc);
    pfsc.setStartIndex(0);
    pfsc.setMaxResults(10);
    searchResults =
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            "", pfsc, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(4, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(4, searchResults.getCount());

    // No query, find "to" and descendants, relationship from/type specified,
    // first page
    Logger
        .getLogger(getClass())
        .info(
            "  No query, find \"to\" and descendants, relationship from/type specified, first page");
    sc = new SearchCriteriaJpa();
    sc.setFindToByRelationshipFromAndType("isa", "361352008", false);
    sc.setFindDescendants(true);
    pfsc = new PfscParameterJpa();
    pfsc.addSearchCriteria(sc);
    pfsc.setStartIndex(0);
    pfsc.setMaxResults(10);
    searchResults =
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            "", pfsc, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(8, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(8, searchResults.getCount());

    // No query, find "to" and descendants and self, relationship from/type
    // specified, first page
    Logger
        .getLogger(getClass())
        .info(
            "  No query, find \"to\" and descendants and self, relationship from/type specified, first page");
    sc = new SearchCriteriaJpa();
    sc.setFindToByRelationshipFromAndType("isa", "361352008", false);
    sc.setFindDescendants(true);
    sc.setFindSelf(true);
    pfsc = new PfscParameterJpa();
    pfsc.addSearchCriteria(sc);
    pfsc.setStartIndex(0);
    pfsc.setMaxResults(10);
    searchResults =
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            "", pfsc, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(10, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(10, searchResults.getCount());

    // No query, find "to" and descendants, relationship type/from specified
    // with desc, first page
    Logger
        .getLogger(getClass())
        .info(
            "  No query, find \"to\" and descendants, relationship type/from specified with desc, first page");
    sc = new SearchCriteriaJpa();
    sc.setFindToByRelationshipFromAndType("isa", "195879000", true);
    sc.setFindDescendants(true);
    pfsc = new PfscParameterJpa();
    pfsc.addSearchCriteria(sc);
    pfsc.setStartIndex(0);
    pfsc.setMaxResults(10);
    searchResults =
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            "", pfsc, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(45, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(10, searchResults.getCount());

    // No query, find "from", relationships type/to specified
    Logger.getLogger(getClass()).info(
        "  No query, find \"from\", relationships type/to specified");
    sc = new SearchCriteriaJpa();
    sc.setFindFromByRelationshipTypeAndTo("isa", "195879000", false);
    pfsc = new PfscParameterJpa();
    pfsc.addSearchCriteria(sc);
    pfsc.setStartIndex(0);
    pfsc.setMaxResults(10);
    searchResults =
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            "", pfsc, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(1, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(1, searchResults.getCount());
    for (SearchResult sr : searchResults.getObjects()) {
      assertTrue(sr.getValue().contains("muscle"));
    }

    // No query, find "from", relationships type/to specified include
    // descendants
    Logger
        .getLogger(getClass())
        .info(
            "  No query, find \"from\", relationships type/to specified include descendants");
    sc = new SearchCriteriaJpa();
    sc.setFindFromByRelationshipTypeAndTo("isa", "195879000", true);
    pfsc = new PfscParameterJpa();
    pfsc.addSearchCriteria(sc);
    pfsc.setStartIndex(0);
    pfsc.setMaxResults(10);
    searchResults =
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            "", pfsc, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(1, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(1, searchResults.getCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }

    // TODO: test pfs parameter "active only" and "inactive only" features
    // TODO: need to test multiple search criteria in conjunction

  }

  /**
   * Test "find" descriptors by query.
   * @throws Exception
   */
  @Test
  public void testEdgeCasesRestContent009() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

    String query = "amino*";
    PfscParameterJpa pfss = new PfscParameterJpa();
    SearchResultList searchResults;

    // Simple query, empty pfs
    Logger.getLogger(getClass()).info("  Simple query, empty pfs");
    searchResults =
        contentService.findDescriptorsForQuery(mshTerminology, mshVersion,
            query, pfss, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(9, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(9, searchResults.getCount());

    // Simple query, sort by name
    Logger.getLogger(getClass()).info("  Simple query, sort by name");
    pfss.setSortField("name");
    searchResults =
        contentService.findDescriptorsForQuery(mshTerminology, mshVersion,
            query, pfss, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(9, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(9, searchResults.getCount());
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfss,
        DescriptorJpa.class));

    // Simple query, sort by name descending
    Logger.getLogger(getClass())
        .info("  Simple query, sort by name, descending");
    pfss.setAscending(false);
    searchResults =
        contentService.findDescriptorsForQuery(mshTerminology, mshVersion,
            query, pfss, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(9, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(9, searchResults.getCount());
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfss,
        DescriptorJpa.class));

    // store the sorted results
    SearchResultList sortedResults = searchResults;

    // Simple query, sort by name, page
    Logger.getLogger(getClass()).info(
        "  Simple query, sort by name, first page");
    pfss.setSortField("name");
    pfss.setStartIndex(0);
    pfss.setMaxResults(5);
    searchResults =
        contentService.findDescriptorsForQuery(mshTerminology, mshVersion,
            query, pfss, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(9, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfss,
        DescriptorJpa.class));
    assertTrue(PfsParameterForComponentTest.testPaging(searchResults,
        sortedResults, pfss));

    // Simple query, sort by name, page
    Logger.getLogger(getClass()).info(
        "  Simple query, sort by name, second page");
    pfss.setSortField("name");
    pfss.setStartIndex(5);
    pfss.setMaxResults(5);
    searchResults =
        contentService.findDescriptorsForQuery(mshTerminology, mshVersion,
            query, pfss, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(9, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertTrue(PfsParameterForComponentTest.testPaging(searchResults,
        sortedResults, pfss));
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfss,
        DescriptorJpa.class));

    // More complex query using query restriction
    Logger.getLogger(getClass()).info("  Simple query with query restriction");
    pfss = new PfscParameterJpa();
    pfss.setQueryRestriction("terminologyId:C118284");
    searchResults =
        contentService.findDescriptorsForQuery(mshTerminology, mshVersion,
            query, pfss, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(1, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(1, searchResults.getCount());
    assertTrue(searchResults.getObjects().get(0).getTerminologyId()
        .equals("C118284"));

    // search criteria tests
    SearchCriteria sc = new SearchCriteriaJpa();

    pfss = new PfscParameterJpa();
    pfss.setActiveOnly(true);

    // No query, ia active only
    Logger.getLogger(getClass()).info("  No query, active only");
    searchResults =
        contentService.findDescriptorsForQuery(mshTerminology, mshVersion, "",
            pfss, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(997, searchResults.getTotalCount());

    // No query, active only with paging
    Logger.getLogger(getClass()).info("  No query, active only with paging");
    pfss = new PfscParameterJpa();
    pfss.setActiveOnly(true);
    pfss.setStartIndex(0);
    pfss.setMaxResults(10);
    searchResults =
        contentService.findDescriptorsForQuery(mshTerminology, mshVersion, "",
            pfss, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(997, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(10, searchResults.getCount());

    // No query, inactive only with paging
    Logger.getLogger(getClass()).info("  No query, inactive only with paging");
    pfss = new PfscParameterJpa();
    pfss.setInactiveOnly(true);
    pfss.setStartIndex(0);
    pfss.setMaxResults(10);
    searchResults =
        contentService.findDescriptorsForQuery(mshTerminology, mshVersion, "",
            pfss, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(0, searchResults.getCount());

    // No query, active only and primitive only
    Logger.getLogger(getClass()).info(
        "  No query, active only and primitive only");
    pfss = new PfscParameterJpa();
    pfss.setActiveOnly(true);
    pfss.setStartIndex(0);
    pfss.setMaxResults(10);
    searchResults =
        contentService.findDescriptorsForQuery(mshTerminology, mshVersion, "",
            pfss, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(997, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(10, searchResults.getCount());

    // Simple query and active only with paging
    Logger.getLogger(getClass()).info(
        "  Simple query and active only with paging");
    pfss = new PfscParameterJpa();
    pfss.setActiveOnly(true);
    pfss.setStartIndex(0);
    pfss.setMaxResults(10);
    searchResults =
        contentService.findDescriptorsForQuery(mshTerminology, mshVersion,
            "disease", pfss, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(8, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(8, searchResults.getCount());
    for (SearchResult sr : searchResults.getObjects()) {
      assertTrue(sr.getValue().contains("isease"));
    }

    // Simple query and inactive active only with paging
    Logger.getLogger(getClass()).info(
        "  Simple query and inactive only with paging");
    pfss = new PfscParameterJpa();
    pfss.setInactiveOnly(true);
    pfss.setStartIndex(0);
    pfss.setMaxResults(10);
    searchResults =
        contentService.findDescriptorsForQuery(mshTerminology, mshVersion,
            "disease", pfss, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(0, searchResults.getTotalCount());
    assertEquals(0, searchResults.getCount());

    // Simple query and active only and primitive only
    Logger.getLogger(getClass()).info(
        "  Simple query and active only and primitive only with paging");
    sc = new SearchCriteriaJpa();
    sc.setPrimitiveOnly(true);
    pfss = new PfscParameterJpa();
    pfss.setActiveOnly(true);
    pfss.addSearchCriteria(sc);
    pfss.setStartIndex(0);
    pfss.setMaxResults(10);
    searchResults =
        contentService.findDescriptorsForQuery(mshTerminology, mshVersion,
            "disease", pfss, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(8, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(8, searchResults.getCount());
    for (SearchResult sr : searchResults.getObjects()) {
      assertTrue(sr.getValue().contains("isease"));
    }

    // TODO: need to test search criteria for descriptor relationships
    // TODO: need to test multiple search criteria in conjunction
    // TODO: test pfs parameter "active only" and "inactive only" features
  }

  /**
   * Test "find" codes by query.
   * @throws Exception
   */
  @Test
  public void testEdgeCasesRestContent010() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

    String query = "amino*";
    PfscParameterJpa pfsc = new PfscParameterJpa();
    SearchResultList searchResults;

    // Simple query, empty pfs
    Logger.getLogger(getClass()).info("  Simple query, empty pfs");
    searchResults =
        contentService.findCodesForQuery(mshTerminology, mshVersion, query,
            pfsc, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(9, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(9, searchResults.getCount());

    // Simple query, sort by name
    Logger.getLogger(getClass()).info("  Simple query, sort by name");
    pfsc.setSortField("name");
    searchResults =
        contentService.findCodesForQuery(mshTerminology, mshVersion, query,
            pfsc, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(9, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(9, searchResults.getCount());
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfsc,
        CodeJpa.class));

    // Simple query, sort by name descending
    Logger.getLogger(getClass())
        .info("  Simple query, sor by name, descending");
    pfsc.setAscending(false);
    searchResults =
        contentService.findCodesForQuery(mshTerminology, mshVersion, query,
            pfsc, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(9, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(9, searchResults.getCount());
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfsc,
        CodeJpa.class));

    // store the sorted results
    SearchResultList sortedResults = searchResults;

    // Simple query, sort by name, page
    Logger.getLogger(getClass()).info(
        "  Simple query, sort by name, first page");
    pfsc.setSortField("name");
    pfsc.setStartIndex(0);
    pfsc.setMaxResults(5);
    searchResults =
        contentService.findCodesForQuery(mshTerminology, mshVersion, query,
            pfsc, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(9, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfsc,
        CodeJpa.class));
    assertTrue(PfsParameterForComponentTest.testPaging(searchResults,
        sortedResults, pfsc));

    // Simple query, sort by name, page
    Logger.getLogger(getClass()).info(
        "  Simple query, sort by name, second page");
    pfsc.setSortField("name");
    pfsc.setStartIndex(5);
    pfsc.setMaxResults(5);
    searchResults =
        contentService.findCodesForQuery(mshTerminology, mshVersion, query,
            pfsc, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(9, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertTrue(PfsParameterForComponentTest.testPaging(searchResults,
        sortedResults, pfsc));
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfsc,
        CodeJpa.class));

    // More complex query using query restriction
    Logger.getLogger(getClass()).info("  Simple query with query restriction");
    pfsc = new PfscParameterJpa();
    pfsc.setQueryRestriction("terminologyId:C118284");
    searchResults =
        contentService.findCodesForQuery(mshTerminology, mshVersion, query,
            pfsc, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(1, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(1, searchResults.getCount());
    assertTrue(searchResults.getObjects().get(0).getTerminologyId()
        .equals("C118284"));

    // search criteria tests
    SearchCriteria sc = new SearchCriteriaJpa();

    pfsc = new PfscParameterJpa();
    pfsc.setActiveOnly(true);
    // No query, ia active only
    Logger.getLogger(getClass()).info("  No query, active only");
    searchResults =
        contentService.findCodesForQuery(mshTerminology, mshVersion, "", pfsc,
            authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(997, searchResults.getTotalCount());

    // No query, active only with paging
    Logger.getLogger(getClass()).info("  No query, active only with paging");
    pfsc = new PfscParameterJpa();
    pfsc.setActiveOnly(true);
    pfsc.setStartIndex(0);
    pfsc.setMaxResults(10);
    searchResults =
        contentService.findCodesForQuery(mshTerminology, mshVersion, "", pfsc,
            authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(997, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(10, searchResults.getCount());

    // No query, inactive only with paging
    Logger.getLogger(getClass()).info("  No query, inactive only with paging");
    pfsc = new PfscParameterJpa();
    pfsc.setInactiveOnly(true);
    pfsc.setStartIndex(0);
    pfsc.setMaxResults(10);
    searchResults =
        contentService.findCodesForQuery(mshTerminology, mshVersion, "", pfsc,
            authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(0, searchResults.getCount());

    // No query, active only and primitive only
    Logger.getLogger(getClass()).info(
        "  No query, active only and primitive only");
    sc = new SearchCriteriaJpa();
    sc.setPrimitiveOnly(true);
    pfsc = new PfscParameterJpa();
    pfsc.setActiveOnly(true);
    pfsc.addSearchCriteria(sc);
    pfsc.setStartIndex(0);
    pfsc.setMaxResults(10);
    searchResults =
        contentService.findCodesForQuery(mshTerminology, mshVersion, "", pfsc,
            authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(997, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(10, searchResults.getCount());

    // Simple query and active only with paging
    Logger.getLogger(getClass()).info(
        "  Simple query and active only with paging");
    pfsc = new PfscParameterJpa();
    pfsc.setActiveOnly(true);
    pfsc.setStartIndex(0);
    pfsc.setMaxResults(10);
    searchResults =
        contentService.findCodesForQuery(mshTerminology, mshVersion, "disease",
            pfsc, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(8, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(8, searchResults.getCount());
    for (SearchResult sr : searchResults.getObjects()) {
      assertTrue(sr.getValue().contains("isease"));
    }

    // Simple query and inactive active only with paging
    Logger.getLogger(getClass()).info(
        "  Simple query and inactive only with paging");
    pfsc = new PfscParameterJpa();
    pfsc.setInactiveOnly(true);
    pfsc.setStartIndex(0);
    pfsc.setMaxResults(10);
    searchResults =
        contentService.findCodesForQuery(mshTerminology, mshVersion, "disease",
            pfsc, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(0, searchResults.getTotalCount());
    assertEquals(0, searchResults.getCount());

    // Simple query and active only and primitive only
    Logger.getLogger(getClass()).info(
        "  Simple query and active only and primitive only with paging");
    sc = new SearchCriteriaJpa();
    sc.setPrimitiveOnly(true);
    pfsc = new PfscParameterJpa();
    pfsc.setActiveOnly(true);
    pfsc.addSearchCriteria(sc);
    pfsc.setStartIndex(0);
    pfsc.setMaxResults(10);
    searchResults =
        contentService.findCodesForQuery(mshTerminology, mshVersion, "disease",
            pfsc, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(8, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(8, searchResults.getCount());
    for (SearchResult sr : searchResults.getObjects()) {
      assertTrue(sr.getValue().contains("isease"));
    }

    // TODO: test pfs parameter "active only" and "inactive only" features

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
