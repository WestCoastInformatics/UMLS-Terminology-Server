/*
 * Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package com.wci.umls.server.test.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
import com.wci.umls.server.helpers.content.DescriptorList;
import com.wci.umls.server.helpers.content.SubsetList;
import com.wci.umls.server.helpers.content.SubsetMemberList;
import com.wci.umls.server.jpa.content.CodeJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.DescriptorJpa;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.helpers.SearchCriteriaJpa;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.ComponentHasAttributesAndName;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.Subset;
import com.wci.umls.server.model.content.SubsetMember;
import com.wci.umls.server.test.helpers.PfsParameterForComponentTest;

/**
 * Implementation of the "Content Service REST Normal Use" Test Cases.
 */
public class ContentServiceRestNormalUseTest extends ContentServiceRestTest {

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
   * Test "get" methods for concepts.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestContent001() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

    // Test MSH concept

    Logger.getLogger(getClass()).info(
        "TEST - " + "M0028634, MSH, 2015_2014_09_08, " + authToken);
    Concept c =
        contentService.getConcept("M0028634", mshTerminology, mshVersion,
            authToken);
    // Validate the concept returned
    assertNotNull(c);
    assertEquals(c.getName(), "Oral Ulcer");
    assertTrue(c.isPublishable());
    assertTrue(c.isPublished());
    assertFalse(c.isObsolete());
    assertFalse(c.isSuppressible());
    assertFalse(c.isAnonymous());
    assertFalse(c.isFullyDefined());
    assertEquals(8, c.getAtoms().size());
    assertEquals(0, c.getAttributes().size());
    // atom definitions still used
    assertEquals(0, c.getDefinitions().size());
    assertEquals(0, c.getRelationships().size());
    assertEquals(0, c.getSemanticTypes().size());
    assertEquals(mshTerminology, c.getTerminology());
    assertEquals(mshVersion, c.getTerminologyVersion());
    assertEquals("M0028634", c.getTerminologyId());
    assertFalse(c.getUsesRelationshipUnion());
    assertTrue(c.getUsesRelationshipIntersection());
    assertEquals("PUBLISHED", c.getWorkflowStatus());
    assertEquals("loader", c.getLastModifiedBy());
    // TODO: test atoms as well.

    // Test SNOMEDCT_US concept
    Logger.getLogger(getClass()).info(
        "TEST - " + "40667002, SNOMEDCT, 2014_09_01, " + authToken);
    c =
        contentService.getConcept("40667002", snomedTerminology, snomedVersion,
            authToken);
    // Validate the concept returned
    assertNotNull(c);
    assertEquals(c.getName(), "Fixation of small intestine");
    assertTrue(c.isPublishable());
    assertTrue(c.isPublished());
    assertFalse(c.isObsolete());
    assertFalse(c.isSuppressible());
    assertFalse(c.isAnonymous());
    assertFalse(c.isFullyDefined());
    assertEquals(3, c.getAtoms().size());
    assertEquals(5, c.getAttributes().size());
    assertEquals(0, c.getDefinitions().size());
    // relationships require a callback by default
    assertEquals(0, c.getRelationships().size());
    assertEquals(0, c.getSemanticTypes().size());
    assertEquals(snomedTerminology, c.getTerminology());
    assertEquals(snomedVersion, c.getTerminologyVersion());
    assertEquals("40667002", c.getTerminologyId());
    assertFalse(c.getUsesRelationshipUnion());
    assertTrue(c.getUsesRelationshipIntersection());
    assertEquals("PUBLISHED", c.getWorkflowStatus());
    assertEquals("loader", c.getLastModifiedBy());
    // TODO: test atoms too

    // Test UMLS concept

    Logger.getLogger(getClass()).info(
        "TEST - " + "C0018787, UMLS, latest, " + authToken);
    c =
        contentService.getConcept("C0018787", umlsTerminology, umlsVersion,
            authToken);
    // Validate the concept returned
    assertNotNull(c);
    assertEquals(c.getName(), "Heart");
    assertTrue(c.isPublishable());
    assertTrue(c.isPublished());
    assertFalse(c.isObsolete());
    assertFalse(c.isSuppressible());
    assertFalse(c.isAnonymous());
    assertFalse(c.isFullyDefined());
    assertEquals(10, c.getAtoms().size());
    assertEquals(3, c.getAttributes().size());
    // definitions still at atom level
    assertEquals(0, c.getDefinitions().size());
    // relationships require a callback by default
    assertEquals(0, c.getRelationships().size());
    assertEquals(1, c.getSemanticTypes().size());
    assertEquals(umlsTerminology, c.getTerminology());
    assertEquals(umlsVersion, c.getTerminologyVersion());
    assertEquals("C0018787", c.getTerminologyId());
    assertFalse(c.getUsesRelationshipUnion());
    assertTrue(c.getUsesRelationshipIntersection());
    assertEquals("PUBLISHED", c.getWorkflowStatus());
    assertEquals("loader", c.getLastModifiedBy());

  }

  /**
   * Test "get" methods for descriptors.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestContent002() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

    Logger.getLogger(getClass()).info(
        "TEST - " + "D019226, MSH, 2015_2014_09_08, " + authToken);
    Descriptor d =
        contentService.getDescriptor("D019226", mshTerminology, mshVersion,
            authToken);

    // Validate the concept returned
    assertNotNull(d);
    assertEquals(d.getName(), "Oral Ulcer");
    assertTrue(d.isPublishable());
    assertTrue(d.isPublished());
    assertFalse(d.isObsolete());
    assertFalse(d.isSuppressible());
    assertEquals(8, d.getAtoms().size());
    assertEquals(12, d.getAttributes().size());
    // atom definitions still used
    assertEquals(0, d.getDefinitions().size());
    // relationships require a callback by default
    assertEquals(0, d.getRelationships().size());
    assertEquals(mshTerminology, d.getTerminology());
    assertEquals(mshVersion, d.getTerminologyVersion());
    assertEquals("D019226", d.getTerminologyId());
    assertEquals("PUBLISHED", d.getWorkflowStatus());
    assertEquals("loader", d.getLastModifiedBy());
    // TODO: test atoms as well.

  }

  /**
   * Test "get" methods for codes.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestContent003() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

    Logger.getLogger(getClass()).info(
        "TEST - " + "D019226, MSH, 2015_2014_09_08, " + authToken);
    Code c =
        contentService
            .getCode("D019226", mshTerminology, mshVersion, authToken);

    // Validate the concept returned
    assertNotNull(c);
    assertEquals(c.getName(), "Oral Ulcer");
    assertTrue(c.isPublishable());
    assertTrue(c.isPublished());
    assertFalse(c.isObsolete());
    assertFalse(c.isSuppressible());
    assertEquals(8, c.getAtoms().size());
    assertEquals(0, c.getAttributes().size());
    // atom definitions still used
    assertEquals(0, c.getRelationships().size());
    assertEquals(mshTerminology, c.getTerminology());
    assertEquals(mshVersion, c.getTerminologyVersion());
    assertEquals("D019226", c.getTerminologyId());
    assertEquals("PUBLISHED", c.getWorkflowStatus());
    assertEquals("loader", c.getLastModifiedBy());
    // TODO: test atoms as well.

    // Test SNOMEDCT_US concept
    Logger.getLogger(getClass()).info(
        "TEST - " + "40667002, SNOMEDCT, 2014_09_01, " + authToken);
    c =
        contentService.getCode("40667002", snomedTerminology, snomedVersion,
            authToken);
    // Validate the concept returned
    assertNotNull(c);
    assertEquals(c.getName(), "Fixation of small intestine");
    assertTrue(c.isPublishable());
    assertTrue(c.isPublished());
    assertFalse(c.isObsolete());
    assertFalse(c.isSuppressible());
    assertEquals(3, c.getAtoms().size());
    assertEquals(0, c.getAttributes().size());
    assertEquals(0, c.getRelationships().size());
    assertEquals(snomedTerminology, c.getTerminology());
    assertEquals(snomedVersion, c.getTerminologyVersion());
    assertEquals("40667002", c.getTerminologyId());
    assertEquals("PUBLISHED", c.getWorkflowStatus());
    assertEquals("loader", c.getLastModifiedBy());
    // TODO: test atoms too
  }

  /**
   * Test "get" method for lexical classes.
   * @throws Exception
   */
  @Test
  public void testNormalUseRestContent004() throws Exception {
    // n/a
  }

  /**
   * Test "get" method for string classes.
   * @throws Exception
   */
  @Test
  public void testNormalUseRestContent005() throws Exception {
    // n/a
  }

  /**
   * Test "get" methods for atom subsets
   * @throws Exception
   */
  @Test
  public void testNormalUseRestContent006() throws Exception {

    Logger.getLogger(getClass()).debug("Start test");

    SubsetList list =
        contentService.getAtomSubsets(snomedTerminology, snomedVersion,
            authToken);
    assertEquals(3, list.getCount());
    int foundCt = 0;
    PfsParameterJpa pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
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
      assertEquals(snomedVersion, subset.getTerminologyVersion());
      if (subset.getName().equals("GB English")) {
        foundCt++;
        assertEquals("900000000000508004", subset.getTerminologyId());
        // Get members
        SubsetMemberList memberList =
            contentService.findAtomSubsetMembers(subset.getTerminologyId(),
                snomedTerminology, snomedVersion, null, pfs, authToken);
        assertEquals(20, memberList.getCount());
        assertEquals(12694, memberList.getTotalCount());
        memberList =
            contentService.findAtomSubsetMembers(subset.getTerminologyId(),
                snomedTerminology, snomedVersion, "heart", pfs, authToken);
        assertEquals(15, memberList.getCount());
        assertEquals(15, memberList.getTotalCount());

      } else if (subset.getName().equals("US English")) {
        assertEquals("900000000000509007", subset.getTerminologyId());
        foundCt++;
        // Get members
        SubsetMemberList memberList =
            contentService.findAtomSubsetMembers(subset.getTerminologyId(),
                snomedTerminology, snomedVersion, null, pfs, authToken);
        assertEquals(20, memberList.getCount());
        assertEquals(12689, memberList.getTotalCount());
        memberList =
            contentService.findAtomSubsetMembers(subset.getTerminologyId(),
                snomedTerminology, snomedVersion, "heart", pfs, authToken);
        assertEquals(15, memberList.getCount());
        assertEquals(15, memberList.getTotalCount());
        SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset> member =
            memberList.getObjects().get(0);
        assertTrue(member.isPublishable());
        assertTrue(member.isPublished());
        assertFalse(member.isObsolete());
        assertFalse(member.isSuppressible());
        assertEquals(1, member.getAttributes().size());
        assertEquals("loader", member.getLastModifiedBy());
        // Not completely equal because of XmlTransient
        assertEquals(member.getSubset().getName(), subset.getName());
        assertEquals(snomedTerminology, member.getTerminology());
        assertEquals(snomedVersion, member.getTerminologyVersion());
      } else if (subset.getName().equals(
          "REFERS TO concept association reference set")) {
        assertEquals("900000000000531004", subset.getTerminologyId());
        foundCt++;
        // Get members
        SubsetMemberList memberList =
            contentService.findAtomSubsetMembers(subset.getTerminologyId(),
                snomedTerminology, snomedVersion, null, pfs, authToken);
        assertEquals(20, memberList.getCount());
        assertEquals(46, memberList.getTotalCount());
      }
    }
    assertEquals(3, foundCt);
  }

  /**
   * Test "get" methods for concept subsets
   * @throws Exception
   */
  @Test
  public void testNormalUseRestContent007() throws Exception {

    Logger.getLogger(getClass()).debug("Start test");

    SubsetList list =
        contentService.getConceptSubsets(snomedTerminology, snomedVersion,
            authToken);
    assertEquals(14, list.getCount());
    int foundCt = 0;
    PfsParameterJpa pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(20);
    for (Subset subset : list.getObjects()) {
      System.out.println(subset.getName());
      assertTrue(subset.isPublished());
      assertTrue(subset.isPublishable());
      assertFalse(subset.isDisjointSubset());
      assertEquals(0, subset.getAttributes().size());
      assertEquals(subset.getDescription(), subset.getName());
      assertEquals(snomedTerminology, subset.getTerminology());
      assertEquals(snomedVersion, subset.getTerminologyVersion());
      if (subset.getName().equals("SAME AS association reference set")) {
        foundCt++;
        assertFalse(subset.isObsolete());
        assertFalse(subset.isSuppressible());
        assertEquals("900000000000527005", subset.getTerminologyId());
        // Get members
        SubsetMemberList memberList =
            contentService.findConceptSubsetMembers(subset.getTerminologyId(),
                snomedTerminology, snomedVersion, null, pfs, authToken);
        assertEquals(20, memberList.getCount());
        assertEquals(1029, memberList.getTotalCount());
        memberList =
            contentService.findConceptSubsetMembers(subset.getTerminologyId(),
                snomedTerminology, snomedVersion, "Karyotype", pfs, authToken);
        assertEquals(2, memberList.getCount());
        assertEquals(2, memberList.getTotalCount());

      } else if (subset.getName().equals("Non-human simple reference set")) {
        assertTrue(subset.isObsolete());
        assertTrue(subset.isSuppressible());
        assertEquals("447564002", subset.getTerminologyId());
        foundCt++;
        // Get members
        SubsetMemberList memberList =
            contentService.findConceptSubsetMembers(subset.getTerminologyId(),
                snomedTerminology, snomedVersion, null, pfs, authToken);
        assertEquals(5, memberList.getCount());
        assertEquals(5, memberList.getTotalCount());

      } else if (subset.getName().equals("ICD-10 complex map reference set")) {
        foundCt++;
        assertFalse(subset.isObsolete());
        assertFalse(subset.isSuppressible());
        assertEquals("447562003", subset.getTerminologyId());
        // Get members
        SubsetMemberList memberList =
            contentService.findConceptSubsetMembers(subset.getTerminologyId(),
                snomedTerminology, snomedVersion, null, pfs, authToken);
        assertEquals(20, memberList.getCount());
        assertEquals(1153, memberList.getTotalCount());
        memberList =
            contentService.findConceptSubsetMembers(subset.getTerminologyId(),
                snomedTerminology, snomedVersion, "syndrome", pfs, authToken);
        assertEquals(20, memberList.getCount());
        assertEquals(71, memberList.getTotalCount());
        SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset> member =
            memberList.getObjects().get(0);
        assertTrue(member.isPublishable());
        assertTrue(member.isPublished());
        assertFalse(member.isObsolete());
        assertFalse(member.isSuppressible());
        assertEquals(7, member.getAttributes().size());
        assertEquals("loader", member.getLastModifiedBy());
        // Not completely equal because of XmlTransient
        assertEquals(member.getSubset().getName(), subset.getName());
        assertEquals(snomedTerminology, member.getTerminology());
        assertEquals(snomedVersion, member.getTerminologyVersion());
      }
    }
    assertEquals(3, foundCt);
  }

  /**
   * Test "find" concepts for query.
   * @throws Exception
   */
  @Test
  public void testNormalUseRestContent008() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

    PfsParameterJpa pfs = new PfsParameterJpa();
    SearchResultList searchResults;

    // Simple query, empty pfs
    Logger.getLogger(getClass()).info("  Simple query, empty pfs");
    searchResults =
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            "care", null, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(19, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(19, searchResults.getCount());

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

    // Complex fielded query, empty pfs
    Logger.getLogger(getClass()).info("  Simple query, empty pfs");
    searchResults =
        contentService
            .findConceptsForQuery(
                snomedTerminology,
                snomedVersion,
                "heart disease AND obsolete:false AND suppressible:false AND published:true",
                null, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(133, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(133, searchResults.getCount());

    // Simple query, sorted on name
    Logger.getLogger(getClass()).info("  Simple query, sorted on name");
    pfs.setSortField("name");
    searchResults =
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            "care", pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(19, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(19, searchResults.getCount());
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfs,
        ConceptJpa.class));

    // Simple query, sorted on name, descending order
    Logger.getLogger(getClass()).info(
        "  Simple query, sorted on name, descending order");
    pfs.setAscending(false);
    searchResults =
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            "care", pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(19, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(19, searchResults.getCount());
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfs,
        ConceptJpa.class));

    // store the sorted results for later comparison
    SearchResultList sortedResults = searchResults;

    // Simple query, paged and sorted results, first page
    Logger.getLogger(getClass()).info(
        "  Simple query, paged and sorted results, first page");
    pfs.setSortField("name");
    pfs.setStartIndex(0);
    pfs.setMaxResults(5);
    searchResults =
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            "care", pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(19, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfs,
        ConceptJpa.class));
    assertTrue(PfsParameterForComponentTest.testPaging(searchResults,
        sortedResults, pfs));

    // Simple query, paged and sorted results, second page
    Logger.getLogger(getClass()).info(
        "  Simple query, paged and sorted results, second page");
    pfs.setSortField("name");
    pfs.setStartIndex(5);
    pfs.setMaxResults(5);
    searchResults =
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            "care", pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(19, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertTrue(PfsParameterForComponentTest.testPaging(searchResults,
        sortedResults, pfs));
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfs,
        ConceptJpa.class));

    // Simple query, query restriction
    Logger.getLogger(getClass()).info("  Simple query, query restriction");
    pfs = new PfsParameterJpa();
    pfs.setQueryRestriction("terminologyId:169559003");
    searchResults =
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            "care", pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(1, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(1, searchResults.getCount());
    assertTrue(searchResults.getObjects().get(0).getTerminologyId()
        .equals("169559003"));

    // Simple query, for "active only", empty pfs
    Logger.getLogger(getClass()).info(
        "  Simple query, for \"active only\", empty pfs");
    SearchCriteria sc = new SearchCriteriaJpa();
    sc.setActiveOnly(true);
    List<SearchCriteria> scl = new ArrayList<SearchCriteria>();
    scl.add(sc);
    pfs = new PfsParameterJpa();
    pfs.setSearchCriteria(scl);
    searchResults =
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            "care", pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(19, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }

    // No query active only, first page
    Logger.getLogger(getClass()).info("  No query active only, first page");
    sc = new SearchCriteriaJpa();
    sc.setActiveOnly(true);
    scl = new ArrayList<SearchCriteria>();
    scl.add(sc);
    pfs = new PfsParameterJpa();
    pfs.setSearchCriteria(scl);
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    searchResults =
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            null, pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(3902, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(10, searchResults.getCount());

    // No query, inactive only, first page
    Logger.getLogger(getClass()).info("  No query, inactive only, first page");
    sc = new SearchCriteriaJpa();
    sc.setInactiveOnly(true);
    scl = new ArrayList<SearchCriteria>();
    scl.add(sc);
    pfs = new PfsParameterJpa();
    pfs.setSearchCriteria(scl);
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    searchResults =
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            null, pfs, authToken);
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
    sc.setActiveOnly(true);
    sc.setPrimitiveOnly(true);
    scl = new ArrayList<SearchCriteria>();
    scl.add(sc);
    pfs = new PfsParameterJpa();
    pfs.setSearchCriteria(scl);
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    searchResults =
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            "", pfs, authToken);
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
    sc = new SearchCriteriaJpa();
    sc.setActiveOnly(true);
    scl = new ArrayList<SearchCriteria>();
    scl.add(sc);
    pfs = new PfsParameterJpa();
    pfs.setSearchCriteria(scl);
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    searchResults =
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            "disease", pfs, authToken);
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
    sc = new SearchCriteriaJpa();
    sc.setInactiveOnly(true);
    scl = new ArrayList<SearchCriteria>();
    scl.add(sc);
    pfs = new PfsParameterJpa();
    pfs.setSearchCriteria(scl);
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    searchResults =
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            "disease", pfs, authToken);
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
    sc.setActiveOnly(true);
    sc.setPrimitiveOnly(true);
    scl = new ArrayList<SearchCriteria>();
    scl.add(sc);
    pfs = new PfsParameterJpa();
    pfs.setSearchCriteria(scl);
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    searchResults =
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            "disease", pfs, authToken);
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
    scl = new ArrayList<SearchCriteria>();
    scl.add(sc);
    pfs = new PfsParameterJpa();
    pfs.setSearchCriteria(scl);
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    searchResults =
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            "", pfs, authToken);
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
    sc.setFindToByRelationshipFromAndType("isa", "195879000", false);
    sc.setRelationshipDescendantsFlag(true);
    scl = new ArrayList<SearchCriteria>();
    scl.add(sc);
    pfs = new PfsParameterJpa();
    pfs.setSearchCriteria(scl);
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    searchResults =
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            "", pfs, authToken);
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
    scl = new ArrayList<SearchCriteria>();
    scl.add(sc);
    pfs = new PfsParameterJpa();
    pfs.setSearchCriteria(scl);
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    searchResults =
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            "", pfs, authToken);
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
    scl = new ArrayList<SearchCriteria>();
    scl.add(sc);
    pfs = new PfsParameterJpa();
    pfs.setSearchCriteria(scl);
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    searchResults =
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            "", pfs, authToken);
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
    sc.setFindToByRelationshipFromAndType("isa", "195879000", false);
    sc.setRelationshipDescendantsFlag(true);
    sc.setFindDescendants(true);
    scl = new ArrayList<SearchCriteria>();
    scl.add(sc);
    pfs = new PfsParameterJpa();
    pfs.setSearchCriteria(scl);
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    searchResults =
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            "", pfs, authToken);
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
    scl = new ArrayList<SearchCriteria>();
    scl.add(sc);
    pfs = new PfsParameterJpa();
    pfs.setSearchCriteria(scl);
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    searchResults =
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            "", pfs, authToken);
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
    sc.setFindFromByRelationshipTypeAndTo("isa", "195879000", false);
    sc.setRelationshipDescendantsFlag(true);
    scl = new ArrayList<SearchCriteria>();
    scl.add(sc);
    pfs = new PfsParameterJpa();
    pfs.setSearchCriteria(scl);
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    searchResults =
        contentService.findConceptsForQuery(snomedTerminology, snomedVersion,
            "", pfs, authToken);
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

    // TODO: need to test multiple search criteria in conjunction

  }

  /**
   * Test "find" descriptors by query.
   * @throws Exception
   */
  @Test
  public void testNormalUseRestContent009() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

    String query = "amino*";
    PfsParameterJpa pfs = new PfsParameterJpa();
    SearchResultList searchResults;

    // Simple query, empty pfs
    Logger.getLogger(getClass()).info("  Simple query, empty pfs");
    searchResults =
        contentService.findDescriptorsForQuery(mshTerminology, mshVersion,
            query, pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(9, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(9, searchResults.getCount());

    // Simple query, sort by name
    Logger.getLogger(getClass()).info("  Simple query, sort by name");
    pfs.setSortField("name");
    searchResults =
        contentService.findDescriptorsForQuery(mshTerminology, mshVersion,
            query, pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(9, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(9, searchResults.getCount());
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfs,
        DescriptorJpa.class));

    // Simple query, sort by name descending
    Logger.getLogger(getClass())
        .info("  Simple query, sor by name, descending");
    pfs.setAscending(false);
    searchResults =
        contentService.findDescriptorsForQuery(mshTerminology, mshVersion,
            query, pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(9, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(9, searchResults.getCount());
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfs,
        DescriptorJpa.class));

    // store the sorted results
    SearchResultList sortedResults = searchResults;

    // Simple query, sort by name, page
    Logger.getLogger(getClass()).info(
        "  Simple query, sort by name, first page");
    pfs.setSortField("name");
    pfs.setStartIndex(0);
    pfs.setMaxResults(5);
    searchResults =
        contentService.findDescriptorsForQuery(mshTerminology, mshVersion,
            query, pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(9, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfs,
        DescriptorJpa.class));
    assertTrue(PfsParameterForComponentTest.testPaging(searchResults,
        sortedResults, pfs));

    // Simple query, sort by name, page
    Logger.getLogger(getClass()).info(
        "  Simple query, sort by name, second page");
    pfs.setSortField("name");
    pfs.setStartIndex(5);
    pfs.setMaxResults(5);
    searchResults =
        contentService.findDescriptorsForQuery(mshTerminology, mshVersion,
            query, pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(9, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertTrue(PfsParameterForComponentTest.testPaging(searchResults,
        sortedResults, pfs));
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfs,
        DescriptorJpa.class));

    // More complex query using query restriction
    Logger.getLogger(getClass()).info("  Simple query with query restriction");
    pfs = new PfsParameterJpa();
    pfs.setQueryRestriction("terminologyId:C118284");
    searchResults =
        contentService.findDescriptorsForQuery(mshTerminology, mshVersion,
            query, pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(1, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(1, searchResults.getCount());
    assertTrue(searchResults.getObjects().get(0).getTerminologyId()
        .equals("C118284"));

    // earch criteria tests
    SearchCriteria sc = new SearchCriteriaJpa();
    sc.setActiveOnly(true);
    List<SearchCriteria> scl = new ArrayList<SearchCriteria>();
    scl.add(sc);
    pfs = new PfsParameterJpa();
    pfs.setSearchCriteria(scl);

    // No query, ia active only
    Logger.getLogger(getClass()).info("  No query, active only");
    searchResults =
        contentService.findDescriptorsForQuery(mshTerminology, mshVersion, "",
            pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(997, searchResults.getTotalCount());

    // No query, active only with paging
    Logger.getLogger(getClass()).info("  No query, active only with paging");
    sc = new SearchCriteriaJpa();
    sc.setActiveOnly(true);
    scl = new ArrayList<SearchCriteria>();
    scl.add(sc);
    pfs = new PfsParameterJpa();
    pfs.setSearchCriteria(scl);
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    searchResults =
        contentService.findDescriptorsForQuery(mshTerminology, mshVersion, "",
            pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(997, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(10, searchResults.getCount());

    // No query, inactive only with paging
    Logger.getLogger(getClass()).info("  No query, inactive only with paging");
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
    searchResults =
        contentService.findDescriptorsForQuery(mshTerminology, mshVersion, "",
            pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(0, searchResults.getCount());

    // No query, active only and primitive only
    Logger.getLogger(getClass()).info(
        "  No query, active only and primitive only");
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
    searchResults =
        contentService.findDescriptorsForQuery(mshTerminology, mshVersion, "",
            pfs, authToken);
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
    searchResults =
        contentService.findDescriptorsForQuery(mshTerminology, mshVersion,
            "disease", pfs, authToken);
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
    searchResults =
        contentService.findDescriptorsForQuery(mshTerminology, mshVersion,
            "disease", pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(0, searchResults.getTotalCount());
    assertEquals(0, searchResults.getCount());

    // Simple query and active only and primitive only
    Logger.getLogger(getClass()).info(
        "  Simple query and active only and primitive only with paging");
    sc = new SearchCriteriaJpa();
    sc.setActiveOnly(true);
    sc.setPrimitiveOnly(true);
    scl = new ArrayList<SearchCriteria>();
    scl.add(sc);
    pfs = new PfsParameterJpa();
    pfs.setSearchCriteria(scl);
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    searchResults =
        contentService.findDescriptorsForQuery(mshTerminology, mshVersion,
            "disease", pfs, authToken);
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
  }

  
  /**
   * Test "find" codes by query.
   * @throws Exception
   */
  @Test
  public void testNormalUseRestContent010() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

    String query = "amino*";
    PfsParameterJpa pfs = new PfsParameterJpa();
    SearchResultList searchResults;

    // Simple query, empty pfs
    Logger.getLogger(getClass()).info("  Simple query, empty pfs");
    searchResults =
        contentService.findCodesForQuery(mshTerminology, mshVersion,
            query, pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(9, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(9, searchResults.getCount());

    // Simple query, sort by name
    Logger.getLogger(getClass()).info("  Simple query, sort by name");
    pfs.setSortField("name");
    searchResults =
        contentService.findCodesForQuery(mshTerminology, mshVersion,
            query, pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(9, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(9, searchResults.getCount());
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfs,
        CodeJpa.class));

    // Simple query, sort by name descending
    Logger.getLogger(getClass())
        .info("  Simple query, sor by name, descending");
    pfs.setAscending(false);
    searchResults =
        contentService.findCodesForQuery(mshTerminology, mshVersion,
            query, pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(9, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(9, searchResults.getCount());
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfs,
        CodeJpa.class));

    // store the sorted results
    SearchResultList sortedResults = searchResults;

    // Simple query, sort by name, page
    Logger.getLogger(getClass()).info(
        "  Simple query, sort by name, first page");
    pfs.setSortField("name");
    pfs.setStartIndex(0);
    pfs.setMaxResults(5);
    searchResults =
        contentService.findCodesForQuery(mshTerminology, mshVersion,
            query, pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(9, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfs,
        CodeJpa.class));
    assertTrue(PfsParameterForComponentTest.testPaging(searchResults,
        sortedResults, pfs));

    // Simple query, sort by name, page
    Logger.getLogger(getClass()).info(
        "  Simple query, sort by name, second page");
    pfs.setSortField("name");
    pfs.setStartIndex(5);
    pfs.setMaxResults(5);
    searchResults =
        contentService.findCodesForQuery(mshTerminology, mshVersion,
            query, pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(9, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertTrue(PfsParameterForComponentTest.testPaging(searchResults,
        sortedResults, pfs));
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfs,
        CodeJpa.class));

    // More complex query using query restriction
    Logger.getLogger(getClass()).info("  Simple query with query restriction");
    pfs = new PfsParameterJpa();
    pfs.setQueryRestriction("terminologyId:C118284");
    searchResults =
        contentService.findCodesForQuery(mshTerminology, mshVersion,
            query, pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(1, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(1, searchResults.getCount());
    assertTrue(searchResults.getObjects().get(0).getTerminologyId()
        .equals("C118284"));

    // earch criteria tests
    SearchCriteria sc = new SearchCriteriaJpa();
    sc.setActiveOnly(true);
    List<SearchCriteria> scl = new ArrayList<SearchCriteria>();
    scl.add(sc);
    pfs = new PfsParameterJpa();
    pfs.setSearchCriteria(scl);

    // No query, ia active only
    Logger.getLogger(getClass()).info("  No query, active only");
    searchResults =
        contentService.findCodesForQuery(mshTerminology, mshVersion, "",
            pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(997, searchResults.getTotalCount());

    // No query, active only with paging
    Logger.getLogger(getClass()).info("  No query, active only with paging");
    sc = new SearchCriteriaJpa();
    sc.setActiveOnly(true);
    scl = new ArrayList<SearchCriteria>();
    scl.add(sc);
    pfs = new PfsParameterJpa();
    pfs.setSearchCriteria(scl);
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    searchResults =
        contentService.findCodesForQuery(mshTerminology, mshVersion, "",
            pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(997, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(10, searchResults.getCount());

    // No query, inactive only with paging
    Logger.getLogger(getClass()).info("  No query, inactive only with paging");
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
    searchResults =
        contentService.findCodesForQuery(mshTerminology, mshVersion, "",
            pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(0, searchResults.getCount());

    // No query, active only and primitive only
    Logger.getLogger(getClass()).info(
        "  No query, active only and primitive only");
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
    searchResults =
        contentService.findCodesForQuery(mshTerminology, mshVersion, "",
            pfs, authToken);
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
    searchResults =
        contentService.findCodesForQuery(mshTerminology, mshVersion,
            "disease", pfs, authToken);
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
    searchResults =
        contentService.findCodesForQuery(mshTerminology, mshVersion,
            "disease", pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + searchResults.getTotalCount());
    assertEquals(0, searchResults.getTotalCount());
    assertEquals(0, searchResults.getCount());

    // Simple query and active only and primitive only
    Logger.getLogger(getClass()).info(
        "  Simple query and active only and primitive only with paging");
    sc = new SearchCriteriaJpa();
    sc.setActiveOnly(true);
    sc.setPrimitiveOnly(true);
    scl = new ArrayList<SearchCriteria>();
    scl.add(sc);
    pfs = new PfsParameterJpa();
    pfs.setSearchCriteria(scl);
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    searchResults =
        contentService.findCodesForQuery(mshTerminology, mshVersion,
            "disease", pfs, authToken);
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
  }  
  
  /**
   * Test ancestor/descendant for concepts.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestContent011() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

    PfsParameterJpa pfs = new PfsParameterJpa();
    ConceptList conceptList;

    // Get descendants for SNOMEDCT concept
    Logger.getLogger(getClass()).info("  Test concept descendants, empty pfs");
    conceptList =
        contentService.findDescendantConcepts("105590001", snomedTerminology,
            snomedVersion, false, pfs, authToken);
    Logger.getLogger(getClass())
    .info("    totalResults = " + conceptList.getTotalCount());
    assertEquals(62, conceptList.getTotalCount());
    assertEquals(62, conceptList.getCount());

    // Get ancestors for SNOMEDCT concept
    Logger.getLogger(getClass()).info("  Test concept ancestors, empty pfs");
    conceptList =
        contentService.findAncestorConcepts("10697004", snomedTerminology,
            snomedVersion, false, pfs, authToken);
    Logger.getLogger(getClass())
    .info("    totalResults = " + conceptList.getTotalCount());
    assertEquals(3, conceptList.getTotalCount());
    assertEquals(3, conceptList.getCount());

    pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(2);
    
    // Get descendants for SNOMEDCT concept with paging
    Logger.getLogger(getClass()).info("  Test concept descendants, with paging ");
    conceptList =
        contentService.findDescendantConcepts("105590001", snomedTerminology,
            snomedVersion, false, pfs, authToken);
    Logger.getLogger(getClass())
    .info("    totalResults = " + conceptList.getTotalCount());
    assertEquals(62, conceptList.getTotalCount());
    assertEquals(2, conceptList.getCount());

    // Get ancestors for SNOMEDCT concept
    Logger.getLogger(getClass()).info("  Test concept ancestors, with paging");
    conceptList =
        contentService.findAncestorConcepts("10697004", snomedTerminology,
            snomedVersion, false, pfs, authToken);
    Logger.getLogger(getClass())
    .info("    totalResults = " + conceptList.getTotalCount());
    assertEquals(3, conceptList.getTotalCount());
    assertEquals(2, conceptList.getCount());

    // TODO: need sort order check (by name)
    // TODO: need "parents only" and "chlidren only" checks. (this also needs implementing)

  }

  /**
   * Test ancestor/descendant for descriptor.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestContent012() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

    PfsParameterJpa pfs = new PfsParameterJpa();
    DescriptorList descriptorList;

    // Get descendants for MSH descriptor
    Logger.getLogger(getClass()).info("  Test descriptor descendants, empty pfs");
    descriptorList =
        contentService.findDescendantDescriptors("D000005", mshTerminology,
            mshVersion, false, pfs, authToken);
    Logger.getLogger(getClass())
    .info("    totalResults = " + descriptorList.getTotalCount());
    assertEquals(4, descriptorList.getTotalCount());
    assertEquals(4, descriptorList.getCount());

    // Get ancestors for MSH Descriptor
    Logger.getLogger(getClass()).info("  Test descriptor ancestors, empty pfs");
    descriptorList =
        contentService.findAncestorDescriptors("D000009", mshTerminology,
            mshVersion, false, pfs, authToken);
    Logger.getLogger(getClass())
    .info("    totalResults = " + descriptorList.getTotalCount());
    assertEquals(4, descriptorList.getTotalCount());
    assertEquals(4, descriptorList.getCount());

    pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(2);
    

    // Get descendants for MSH descriptor with paging
    Logger.getLogger(getClass()).info("  Test descriptor descendants, with paging ");
    descriptorList =
        contentService.findDescendantDescriptors("D000005", mshTerminology,
            mshVersion, false, pfs, authToken);
    Logger.getLogger(getClass())
    .info("    totalResults = " + descriptorList.getTotalCount());
    assertEquals(4, descriptorList.getTotalCount());
    assertEquals(2, descriptorList.getCount());

  
    // Get ancestors for MSH descriptor
    Logger.getLogger(getClass()).info("  Test descriptor ancestors, with paging");
    descriptorList =
        contentService.findAncestorDescriptors("D000009", mshTerminology,
            mshVersion, false, pfs, authToken);
    Logger.getLogger(getClass())
    .info("    totalResults = " + descriptorList.getTotalCount());
    assertEquals(4, descriptorList.getTotalCount());
    assertEquals(2, descriptorList.getCount());

    // TODO: need sort order check (by name)
    // TODO: need "parents only" and "chlidren only" checks. (this also needs implementing)

  }

  @Test
  public void testNormalUseRestContent013() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");
    
    /** Find concepts with hql query */
    Logger.getLogger(getClass()).info(
        "TEST1 - " + "SELECT c FROM ConceptJpa c, SNOMEDCT_US, 2014_09_01, "
            + authToken);
    SearchResultList sml =
        contentService.findConceptsForGeneralQuery("",
            "SELECT c FROM ConceptJpa c", new PfsParameterJpa(), authToken);
    assertTrue(sml.getCount() == 6942);

    /** Find concepts with hql query and pfs parameter max results 20 */
    PfsParameterJpa pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(20);
    Logger.getLogger(getClass()).info(
        "TEST2 - " + "SELECT c FROM ConceptJpa c, SNOMEDCT_US, 2014_09_01, "
            + pfs + authToken);
    sml =
        contentService.findConceptsForGeneralQuery("",
            "SELECT c FROM ConceptJpa c", pfs, authToken);
    assertTrue(sml.getCount() == 20);
    assertTrue(sml.getTotalCount() == 6942);

    /** Find concepts in intersection of lucene and hql queries */
    Logger
        .getLogger(getClass())
        .info(
            "TEST3 - "
                + "name:amino, SELECT c FROM ConceptJpa c, SNOMEDCT_US, 2014_09_01, "
                + authToken);
    sml =
        contentService.findConceptsForGeneralQuery("name:amino",
            "SELECT c FROM ConceptJpa c", new PfsParameterJpa(), authToken);
    assertTrue(sml.getCount() == 10);
    assertTrue(sml.getTotalCount() == 10);

    /** Find concepts in lucene query */
    Logger.getLogger(getClass()).info(
        "TEST4 - " + "name:amino, SNOMEDCT_US, 2014_09_01, " + authToken);
    sml =
        contentService.findConceptsForGeneralQuery("name:amino", "",
            new PfsParameterJpa(), authToken);
    assertTrue(sml.getCount() == 10);
    assertTrue(sml.getTotalCount() == 10);
    
    /** Find descriptors with hql query */
    Logger.getLogger(getClass()).info(
        "TEST5 - " + "SELECT c FROM DescriptorJpa c, SNOMEDCT_US, 2014_09_01, "
            + authToken);
    sml =
        contentService.findDescriptorsForGeneralQuery("",
            "SELECT c FROM DescriptorJpa c", new PfsParameterJpa(), authToken);
    assertTrue(sml.getCount() == 997);

    /** Find descriptors with hql query and pfs parameter max results 20 */
    pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(20);
    Logger.getLogger(getClass()).info(
        "TEST6 - " + "SELECT c FROM DescriptorJpa c, SNOMEDCT_US, 2014_09_01, "
            + pfs + authToken);
    sml =
        contentService.findDescriptorsForGeneralQuery("",
            "SELECT c FROM DescriptorJpa c", pfs, authToken);
    assertTrue(sml.getCount() == 20);
    assertTrue(sml.getTotalCount() == 997);

    /** Find descriptors in intersection of lucene and hql queries */
    Logger
        .getLogger(getClass())
        .info(
            "TEST7 - "
                + "name:amino, SELECT c FROM DescriptorJpa c, SNOMEDCT_US, 2014_09_01, "
                + authToken);
    sml =
        contentService.findDescriptorsForGeneralQuery("name:amino",
            "SELECT c FROM DescriptorJpa c", new PfsParameterJpa(), authToken);
    assertTrue(sml.getCount() == 4);
    assertTrue(sml.getTotalCount() == 4);

    /** Find descriptors in lucene query */
    Logger.getLogger(getClass()).info(
        "TEST8 - " + "name:amino, SNOMEDCT_US, 2014_09_01, " + authToken);
    sml =
        contentService.findDescriptorsForGeneralQuery("name:amino", "",
            new PfsParameterJpa(), authToken);
    assertTrue(sml.getCount() == 4);
    assertTrue(sml.getTotalCount() == 4);    
    
    /** Find codes with hql query */
    Logger.getLogger(getClass()).info(
        "TEST9 - " + "SELECT c FROM CodeJpa c, SNOMEDCT_US, 2014_09_01, "
            + authToken);
    sml =
        contentService.findCodesForGeneralQuery("",
            "SELECT c FROM CodeJpa c", new PfsParameterJpa(), authToken);
    assertTrue(sml.getCount() == 5050);

    /** Find codes with hql query and pfs parameter max results 20 */
    pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(20);
    Logger.getLogger(getClass()).info(
        "TEST10 - " + "SELECT c FROM CodeJpa c, SNOMEDCT_US, 2014_09_01, "
            + pfs + authToken);
    sml =
        contentService.findCodesForGeneralQuery("",
            "SELECT c FROM CodeJpa c", pfs, authToken);
    assertTrue(sml.getCount() == 20);
    assertTrue(sml.getTotalCount() == 5050);

    /** Find codes in intersection of lucene and hql queries */
    Logger
        .getLogger(getClass())
        .info(
            "TEST11 - "
                + "name:amino, SELECT c FROM CodeJpa c, SNOMEDCT_US, 2014_09_01, "
                + authToken);
    sml =
        contentService.findCodesForGeneralQuery("name:amino",
            "SELECT c FROM CodeJpa c", new PfsParameterJpa(), authToken);
    assertTrue(sml.getCount() == 6);
    assertTrue(sml.getTotalCount() == 6);

    /** Find codes in lucene query */
    Logger.getLogger(getClass()).info(
        "TEST12 - " + "name:amino, SNOMEDCT_US, 2014_09_01, " + authToken);
    sml =
        contentService.findCodesForGeneralQuery("name:amino", "",
            new PfsParameterJpa(), authToken);
    assertTrue(sml.getCount() == 6);
    assertTrue(sml.getTotalCount() == 6); 
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
