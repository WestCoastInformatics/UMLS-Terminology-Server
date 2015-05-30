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

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.wci.umls.server.helpers.SearchCriteria;
import com.wci.umls.server.helpers.SearchResult;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.helpers.StringList;
import com.wci.umls.server.helpers.content.ConceptList;
import com.wci.umls.server.helpers.content.DescriptorList;
import com.wci.umls.server.helpers.content.RelationshipList;
import com.wci.umls.server.helpers.content.SubsetList;
import com.wci.umls.server.helpers.content.SubsetMemberList;
import com.wci.umls.server.helpers.content.Tree;
import com.wci.umls.server.helpers.content.TreeList;
import com.wci.umls.server.jpa.content.AbstractRelationship;
import com.wci.umls.server.jpa.content.CodeJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.DescriptorJpa;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.helpers.PfscParameterJpa;
import com.wci.umls.server.jpa.helpers.SearchCriteriaJpa;
import com.wci.umls.server.model.content.AtomClass;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.ComponentHasAttributesAndName;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.Subset;
import com.wci.umls.server.model.content.SubsetMember;
import com.wci.umls.server.model.content.TreePosition;
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

    // TODO: test pfs parameter "active only" and "inactive only" features
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

    // TODO: test pfs parameter "active only" and "inactive only" features

  }

  /**
   * Test "find" concepts for query.
   * @throws Exception
   */
  @Test
  public void testNormalUseRestContent008() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

    PfscParameterJpa pfsc = new PfscParameterJpa();
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
  public void testNormalUseRestContent009() throws Exception {
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
        .info("  Simple query, sor by name, descending");
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
  public void testNormalUseRestContent010() throws Exception {
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
    Logger.getLogger(getClass()).info(
        "    totalResults = " + conceptList.getTotalCount());
    assertEquals(62, conceptList.getTotalCount());
    assertEquals(62, conceptList.getCount());

    // Get ancestors for SNOMEDCT concept
    Logger.getLogger(getClass()).info("  Test concept ancestors, empty pfs");
    conceptList =
        contentService.findAncestorConcepts("10697004", snomedTerminology,
            snomedVersion, false, pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalResults = " + conceptList.getTotalCount());
    assertEquals(3, conceptList.getTotalCount());
    assertEquals(3, conceptList.getCount());

    pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(2);

    // Get descendants for SNOMEDCT concept with paging
    Logger.getLogger(getClass()).info(
        "  Test concept descendants, with paging ");
    conceptList =
        contentService.findDescendantConcepts("105590001", snomedTerminology,
            snomedVersion, false, pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalResults = " + conceptList.getTotalCount());
    assertEquals(62, conceptList.getTotalCount());
    assertEquals(2, conceptList.getCount());

    // Get ancestors for SNOMEDCT concept
    Logger.getLogger(getClass()).info("  Test concept ancestors, with paging");
    conceptList =
        contentService.findAncestorConcepts("10697004", snomedTerminology,
            snomedVersion, false, pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalResults = " + conceptList.getTotalCount());
    assertEquals(3, conceptList.getTotalCount());
    assertEquals(2, conceptList.getCount());

    // TODO: need sort order check (by name)
    // TODO: need "parents only" and "chlidren only" checks. (this also needs
    // implementing)
    // TODO: test pfs parameter "active only" and "inactive only" features

  }

  /**
   * Test ancestor/descendant for descriptors.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestContent012() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

    PfsParameterJpa pfs = new PfsParameterJpa();
    DescriptorList descriptorList;

    // Get descendants for MSH descriptor
    Logger.getLogger(getClass()).info(
        "  Test descriptor descendants, empty pfs");
    descriptorList =
        contentService.findDescendantDescriptors("D000005", mshTerminology,
            mshVersion, false, pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalResults = " + descriptorList.getTotalCount());
    assertEquals(4, descriptorList.getTotalCount());
    assertEquals(4, descriptorList.getCount());

    // Get ancestors for MSH Descriptor
    Logger.getLogger(getClass()).info("  Test descriptor ancestors, empty pfs");
    descriptorList =
        contentService.findAncestorDescriptors("D000009", mshTerminology,
            mshVersion, false, pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalResults = " + descriptorList.getTotalCount());
    assertEquals(4, descriptorList.getTotalCount());
    assertEquals(4, descriptorList.getCount());

    pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(2);

    // Get descendants for MSH descriptor with paging
    Logger.getLogger(getClass()).info(
        "  Test descriptor descendants, with paging ");
    descriptorList =
        contentService.findDescendantDescriptors("D000005", mshTerminology,
            mshVersion, false, pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalResults = " + descriptorList.getTotalCount());
    assertEquals(4, descriptorList.getTotalCount());
    assertEquals(2, descriptorList.getCount());

    // Get ancestors for MSH descriptor
    Logger.getLogger(getClass()).info(
        "  Test descriptor ancestors, with paging");
    descriptorList =
        contentService.findAncestorDescriptors("D000009", mshTerminology,
            mshVersion, false, pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalResults = " + descriptorList.getTotalCount());
    assertEquals(4, descriptorList.getTotalCount());
    assertEquals(2, descriptorList.getCount());

    // TODO: need sort order check (by name)
    // TODO: need "parents only" and "chlidren only" checks. (this also needs
    // implementing)
    // TODO: test pfs parameter "active only" and "inactive only" features

  }

  /**
   * Test ancestor/descendant for codes.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestContent013() throws Exception {
    // n/a - no code ancestors or descendants
    // TODO: consider sample data from SAMPLE_2014AB
  }

  /**
   * Test "find" subset members for atom or concept.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestContent014() throws Exception {
    Logger.getLogger(getClass()).info("Start test");

    Logger.getLogger(getClass()).info("  Test get subset members for atom");
    SubsetMemberList list =
        contentService.getSubsetMembersForAtom("166113012", snomedTerminology,
            snomedVersion, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(2, list.getTotalCount());
    assertEquals(2, list.getCount());

    Logger.getLogger(getClass()).info("  Test get subset members for concept");
    list =
        contentService.getSubsetMembersForConcept("10123006",
            snomedTerminology, snomedVersion, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(5, list.getTotalCount());
    assertEquals(5, list.getCount());

  }

  /**
   * Test autocomplete for concepts.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestContent015() throws Exception {
    Logger.getLogger(getClass()).info("Start test");

    Logger.getLogger(getClass())
        .info("  Test autocomplete for snomed concepts");
    StringList list =
        contentService.autocompleteConcepts(snomedTerminology, snomedVersion,
            "let", authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(109, list.getTotalCount());
    assertEquals(17, list.getCount());

    list =
        contentService.autocompleteConcepts(snomedTerminology, snomedVersion,
            "lett", authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(122, list.getTotalCount());
    assertEquals(13, list.getCount());

    list =
        contentService.autocompleteConcepts(snomedTerminology, snomedVersion,
            "lettu", authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(122, list.getTotalCount());
    assertEquals(13, list.getCount());

    Logger.getLogger(getClass()).info("  Test autocomplete for msh concepts");
    list =
        contentService.autocompleteConcepts(mshTerminology, mshVersion, "let",
            authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(19, list.getTotalCount());
    assertEquals(19, list.getCount());

    list =
        contentService.autocompleteConcepts(mshTerminology, mshVersion, "lett",
            authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(22, list.getTotalCount());
    assertEquals(20, list.getCount());

    list =
        contentService.autocompleteConcepts(mshTerminology, mshVersion,
            "lettu", authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(22, list.getTotalCount());
    assertEquals(20, list.getCount());

    Logger.getLogger(getClass()).info("  Test autocomplete for umls concepts");
    list =
        contentService.autocompleteConcepts(umlsTerminology, umlsVersion,
            "let", authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(95, list.getTotalCount());
    assertEquals(20, list.getCount());

    list =
        contentService.autocompleteConcepts(umlsTerminology, umlsVersion,
            "lett", authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(98, list.getTotalCount());
    assertEquals(20, list.getCount());

    list =
        contentService.autocompleteConcepts(umlsTerminology, umlsVersion,
            "lettu", authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(98, list.getTotalCount());
    assertEquals(20, list.getCount());

  }

  /**
   * Test autocomplete for descriptors
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestContent016() throws Exception {
    Logger.getLogger(getClass()).info("Start test");

    Logger.getLogger(getClass())
        .info("  Test autocomplete for msh descriptors");
    StringList list =
        contentService.autocompleteConcepts(mshTerminology, mshVersion, "let",
            authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(19, list.getTotalCount());
    assertEquals(19, list.getCount());

    list =
        contentService.autocompleteConcepts(mshTerminology, mshVersion, "lett",
            authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(22, list.getTotalCount());
    assertEquals(20, list.getCount());

    list =
        contentService.autocompleteConcepts(mshTerminology, mshVersion,
            "lettu", authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(22, list.getTotalCount());
    assertEquals(20, list.getCount());

  }

  /**
   * Test autocomplete for codes.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestContent017() throws Exception {
    Logger.getLogger(getClass()).info("Start test");

    Logger.getLogger(getClass()).info("  Test autocomplete for snomed codes");
    StringList list =
        contentService.autocompleteCodes(snomedTerminology, snomedVersion,
            "let", authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(109, list.getTotalCount());
    assertEquals(17, list.getCount());

    list =
        contentService.autocompleteCodes(snomedTerminology, snomedVersion,
            "lett", authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(122, list.getTotalCount());
    assertEquals(13, list.getCount());

    list =
        contentService.autocompleteCodes(snomedTerminology, snomedVersion,
            "lettu", authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(122, list.getTotalCount());
    assertEquals(13, list.getCount());

    Logger.getLogger(getClass()).info("  Test autocomplete for msh codes");
    list =
        contentService.autocompleteCodes(mshTerminology, mshVersion, "let",
            authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(19, list.getTotalCount());
    assertEquals(19, list.getCount());

    list =
        contentService.autocompleteCodes(mshTerminology, mshVersion, "lett",
            authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(22, list.getTotalCount());
    assertEquals(20, list.getCount());

    list =
        contentService.autocompleteCodes(mshTerminology, mshVersion, "lettu",
            authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(22, list.getTotalCount());
    assertEquals(20, list.getCount());

  }

  /**
   * Test get of deep relationships for a concept.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestContent018() throws Exception {
    Logger.getLogger(getClass()).info("Start test");

    // simple deep rels call
    Logger.getLogger(getClass()).info("  Test deep relationships");
    RelationshipList list =
        contentService.findDeepRelationshipsForConcept("C0000097", "UMLS",
            "latest", new PfsParameterJpa(), authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(66, list.getTotalCount());
    assertEquals(66, list.getCount());
    RelationshipList fullList = list;

    PfsParameterJpa pfs = new PfsParameterJpa();

    // deep rels call with paging
    Logger.getLogger(getClass()).info("  Test deep relationships with paging");
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    list =
        contentService.findDeepRelationshipsForConcept("C0000097", "UMLS",
            "latest", pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(66, list.getTotalCount());
    assertEquals(10, list.getCount());
    assertTrue(PfsParameterForComponentTest.testPaging(list, fullList, pfs));

    // deep rels call with sorting
    Logger.getLogger(getClass()).info("  Test deep relationships with paging");
    pfs = new PfsParameterJpa();
    pfs.setSortField("relationshipType");
    list =
        contentService.findDeepRelationshipsForConcept("C0000097", "UMLS",
            "latest", pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(66, list.getTotalCount());
    assertEquals(66, list.getCount());
    fullList = list;

    // deep rels call with sorting and paging
    Logger.getLogger(getClass()).info(
        "  Test deep relationships with sorting and paging");
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    pfs.setSortField("relationshipType");
    list =
        contentService.findDeepRelationshipsForConcept("C0000097", "UMLS",
            "latest", pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(66, list.getTotalCount());
    assertEquals(10, list.getCount());
    assertTrue(PfsParameterForComponentTest.testPaging(list, fullList, pfs));
    assertTrue(PfsParameterForComponentTest.testSort(list, pfs,
        AbstractRelationship.class));

    // deep rels call with sorting and paging, page 2
    Logger.getLogger(getClass()).info(
        "  Test deep relationships with sorting and paging");
    pfs.setStartIndex(10);
    pfs.setMaxResults(10);
    pfs.setSortField("relationshipType");
    list =
        contentService.findDeepRelationshipsForConcept("C0000097", "UMLS",
            "latest", pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(66, list.getTotalCount());
    assertEquals(10, list.getCount());
    assertTrue(PfsParameterForComponentTest.testPaging(list, fullList, pfs));
    assertTrue(PfsParameterForComponentTest.testSort(list, pfs,
        AbstractRelationship.class));

    // TODO: test pfs parameter "active only" and "inactive only" features

  }

  /**
   * Test find trees for concepts.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestContent023() throws Exception {
    Logger.getLogger(getClass()).info("Start test");

    // tree lookup, empty pfs
    Logger.getLogger(getClass()).info("  Tree lookup, empty pfs");
    TreeList list =
        contentService.findTreesForConcept("259662009", snomedTerminology,
            snomedVersion, new PfsParameterJpa(), authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(5, list.getTotalCount());
    for (Tree tree : list.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + tree);
    }
    assertEquals(5, list.getCount());

    TreeList fullList = list;
    PfsParameterJpa pfs = new PfsParameterJpa();

    // tree lookup, first page
    Logger.getLogger(getClass()).info("  Tree lookup, first page");
    pfs.setStartIndex(0);
    pfs.setMaxResults(2);
    list =
        contentService.findTreesForConcept("259662009", snomedTerminology,
            snomedVersion, pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(5, list.getTotalCount());
    for (Tree tree : list.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + tree);
    }
    assertEquals(2, list.getCount());
    assertTrue(PfsParameterForComponentTest.testPaging(list, fullList, pfs));

    // tree lookup, second page
    Logger.getLogger(getClass()).info("  Tree lookup, second page");
    pfs.setStartIndex(2);
    pfs.setMaxResults(2);
    list =
        contentService.findTreesForConcept("259662009", snomedTerminology,
            snomedVersion, pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(5, list.getTotalCount());
    for (Tree tree : list.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + tree);
    }
    assertEquals(2, list.getCount());
    assertTrue(PfsParameterForComponentTest.testPaging(list, fullList, pfs));

    // tree lookup, first page and sort order
    Logger.getLogger(getClass()).info("  Tree lookup, first page");
    pfs.setStartIndex(0);
    pfs.setMaxResults(2);
    pfs.setSortField("nodeTerminologyId");
    list =
        contentService.findTreesForConcept("259662009", snomedTerminology,
            snomedVersion, pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(5, list.getTotalCount());
    for (Tree tree : list.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + tree);
    }
    assertEquals(2, list.getCount());
    assertTrue(PfsParameterForComponentTest.testPaging(list, fullList, pfs));
    // hard to verify sort order because it's based on the lowest-level node
    // information

  }

  /**
   * Test find trees for descriptors.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestContent024() throws Exception {
    Logger.getLogger(getClass()).info("Start test");

    // tree lookup, empty pfs
    Logger.getLogger(getClass()).info("  Tree lookup, empty pfs");
    TreeList list =
        contentService.findTreesForDescriptor("D018410", mshTerminology,
            mshVersion, new PfsParameterJpa(), authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(3, list.getTotalCount());
    for (Tree tree : list.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + tree);
    }
    assertEquals(3, list.getCount());

    TreeList fullList = list;
    PfsParameterJpa pfs = new PfsParameterJpa();

    // tree lookup, first page
    Logger.getLogger(getClass()).info("  Tree lookup, first page");
    pfs.setStartIndex(0);
    pfs.setMaxResults(1);
    list =
        contentService.findTreesForDescriptor("D018410", mshTerminology,
            mshVersion, pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(3, list.getTotalCount());
    for (Tree tree : list.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + tree);
    }
    assertEquals(1, list.getCount());
    assertTrue(PfsParameterForComponentTest.testPaging(list, fullList, pfs));

    // tree lookup, second page
    Logger.getLogger(getClass()).info("  Tree lookup, second page");
    pfs.setStartIndex(1);
    pfs.setMaxResults(1);
    list =
        contentService.findTreesForDescriptor("D018410", mshTerminology,
            mshVersion, pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(3, list.getTotalCount());
    for (Tree tree : list.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + tree);
    }
    assertEquals(1, list.getCount());
    assertTrue(PfsParameterForComponentTest.testPaging(list, fullList, pfs));

    // tree lookup, first page and sort order
    Logger.getLogger(getClass()).info("  Tree lookup, first page");
    pfs.setStartIndex(0);
    pfs.setMaxResults(1);
    pfs.setSortField("nodeTerminologyId");
    list =
        contentService.findTreesForDescriptor("D018410", mshTerminology,
            mshVersion, pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(3, list.getTotalCount());
    for (Tree tree : list.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + tree);
    }
    assertEquals(1, list.getCount());
    assertTrue(PfsParameterForComponentTest.testPaging(list, fullList, pfs));
    // hard to verify sort order because it's based on the lowest-level node
    // information

  }

  /**
   * Test find trees for codes.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestContent025() throws Exception {
    Logger.getLogger(getClass()).info("Start test");
    // n/a - no sample data
    // TODO: consider sample data from SAMPLE_2014AB
  }

  /**
   * Test general query mechanism.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestContent026() throws Exception {
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
        contentService.findCodesForGeneralQuery("", "SELECT c FROM CodeJpa c",
            new PfsParameterJpa(), authToken);
    assertTrue(sml.getCount() == 5050);

    /** Find codes with hql query and pfs parameter max results 20 */
    pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(20);
    Logger.getLogger(getClass()).info(
        "TEST10 - " + "SELECT c FROM CodeJpa c, SNOMEDCT_US, 2014_09_01, "
            + pfs + authToken);
    sml =
        contentService.findCodesForGeneralQuery("", "SELECT c FROM CodeJpa c",
            pfs, authToken);
    assertTrue(sml.getCount() == 20);
    assertTrue(sml.getTotalCount() == 5050);

    /** Find codes in intersection of lucene and hql queries */
    Logger.getLogger(getClass()).info(
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

  @Test
  public void testNormalUseRestContent020() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

    /** Find concepts with hql query */
    Logger.getLogger(getClass()).info(
        "TEST1 - " + "SELECT c FROM ConceptJpa c, SNOMEDCT_US, 2014_09_01, "
            + authToken);
    PfsParameterJpa pfs = new PfsParameterJpa();
    RelationshipList sml =
        contentService.findRelationshipsForConcept("118613001",
            snomedTerminology, snomedVersion, "", pfs, authToken);
    // assertTrue(sml.getCount() == 6942);
  }

  /**
   * Test find concept trees for query.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestContent027() throws Exception {
    Logger.getLogger(getClass()).info("Start test");

    // tree lookup, empty pfs
    Logger.getLogger(getClass()).info("  Simple query, empty pfs");
    Tree tree =
        contentService.findConceptTreeForQuery(snomedTerminology,
            snomedVersion, "vitamin", new PfsParameterJpa(), authToken);
    Logger.getLogger(getClass()).info(
        "    total leaf count = " + tree.getLeafNodes().size());
    assertEquals(5, tree.getLeafNodes().size());
    Logger.getLogger(getClass()).info("    Result: " + tree);
    // All the leaf TreePosition<AtomClass> tree should contain "vitamin"
    for (TreePosition<? extends AtomClass> leaf : tree.getLeafNodes()) {
      assertTrue(leaf.getNode().getName().toLowerCase().contains("vitamin"));
    }

    PfsParameterJpa pfs = new PfsParameterJpa();
    // tree lookup, limit to 3
    pfs.setStartIndex(0);
    pfs.setMaxResults(3);
    Logger.getLogger(getClass()).info("  Simple query, limit to 3");
    tree =
        contentService.findConceptTreeForQuery(snomedTerminology,
            snomedVersion, "vitamin", pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    total leaf count = " + tree.getLeafNodes().size());
    assertEquals(3, tree.getLeafNodes().size());
    Logger.getLogger(getClass()).info("    Result: " + tree);
    // All the leaf TreePosition<AtomClass> tree should contain "vitamin"
    for (TreePosition<? extends AtomClass> leaf : tree.getLeafNodes()) {
      assertTrue(leaf.getNode().getName().toLowerCase().contains("vitamin"));
    }

    // wider lookup, limit to 10
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    Logger.getLogger(getClass()).info("  Simple query, limit to 3");
    tree =
        contentService.findConceptTreeForQuery(snomedTerminology,
            snomedVersion, "a*", pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    total leaf count = " + tree.getLeafNodes().size());
    assertEquals(8, tree.getLeafNodes().size());
    Logger.getLogger(getClass()).info("    Result: " + tree);
    // All the leaf TreePosition<AtomClass> tree should contain "vitamin"
    for (TreePosition<? extends AtomClass> leaf : tree.getLeafNodes()) {
      assertTrue(leaf.getNode().getName().toLowerCase().contains("a"));
    }

    // TODO: consider other cases of this

  }

  /**
   * Test find descriptor trees for query.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestContent028() throws Exception {
    Logger.getLogger(getClass()).info("Start test");

    // tree lookup, empty pfs
    Logger.getLogger(getClass()).info("  Simple query, empty pfs");
    Tree tree =
        contentService.findDescriptorTreeForQuery(mshTerminology, mshVersion,
            "pneumonia", new PfsParameterJpa(), authToken);
    Logger.getLogger(getClass()).info(
        "    total leaf count = " + tree.getLeafNodes().size());
    assertEquals(4, tree.getLeafNodes().size());
    Logger.getLogger(getClass()).info("    Result: " + tree);
    // All the leaf TreePosition<AtomClass> tree should contain "vitamin"
    for (TreePosition<? extends AtomClass> leaf : tree.getLeafNodes()) {
      assertTrue(leaf.getNode().getName().toLowerCase().contains("pneumonia"));
    }

    PfsParameterJpa pfs = new PfsParameterJpa();
    // tree lookup, limit to 3
    pfs.setStartIndex(0);
    pfs.setMaxResults(3);
    Logger.getLogger(getClass()).info("  Simple query, limit to 3");
    tree =
        contentService.findDescriptorTreeForQuery(mshTerminology, mshVersion,
            "pneumonia", pfs, authToken);
    Logger.getLogger(getClass()).info(
        "    total leaf count = " + tree.getLeafNodes().size());
    assertEquals(1, tree.getLeafNodes().size());
    Logger.getLogger(getClass()).info("    Result: " + tree);
    // All the leaf TreePosition<AtomClass> tree should contain "vitamin"
    for (TreePosition<? extends AtomClass> leaf : tree.getLeafNodes()) {
      assertTrue(leaf.getNode().getName().toLowerCase().contains("pneumonia"));
    }

    // TODO: consider other cases of this, may need bigger data set

  }

  /**
   * Test find code trees for query.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestContent029() throws Exception {
    Logger.getLogger(getClass()).info("Start test");

    // n/a - no sample data
    // TODO: consider sample data from SAMPLE_2014AB
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
