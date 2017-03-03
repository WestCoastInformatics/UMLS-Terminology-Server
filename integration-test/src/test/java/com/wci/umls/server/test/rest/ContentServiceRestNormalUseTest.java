/*
 *    Copyright 2016 West Coast Informatics, LLC
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

import com.wci.umls.server.Project;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.SearchResult;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.helpers.StringList;
import com.wci.umls.server.helpers.content.ConceptList;
import com.wci.umls.server.helpers.content.DescriptorList;
import com.wci.umls.server.helpers.content.MapSetList;
import com.wci.umls.server.helpers.content.MappingList;
import com.wci.umls.server.helpers.content.RelationshipList;
import com.wci.umls.server.helpers.content.SubsetList;
import com.wci.umls.server.helpers.content.SubsetMemberList;
import com.wci.umls.server.helpers.content.Tree;
import com.wci.umls.server.helpers.content.TreeList;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.CodeJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.ConceptSubsetJpa;
import com.wci.umls.server.jpa.content.DescriptorJpa;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.services.rest.ProjectServiceRest;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.ComponentHasAttributesAndName;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.MapSet;
import com.wci.umls.server.model.content.Subset;
import com.wci.umls.server.model.content.SubsetMember;
import com.wci.umls.server.model.workflow.WorkflowStatus;
import com.wci.umls.server.rest.client.ProjectClientRest;
import com.wci.umls.server.test.helpers.PfsParameterForComponentTest;

/**
 * Implementation of the "Content Service REST Normal Use" Test Cases.
 */
public class ContentServiceRestNormalUseTest extends ContentServiceRestTest {

  /** The auth token. */
  private static String authToken;

  /** The admin token. */
  private static String adminToken;

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
    adminToken =
        securityService.authenticate(adminUser, adminPassword).getAuthToken();

  }

  /**
   * Test "get" methods for concepts.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetConcept() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Test MSH concept

    Logger.getLogger(getClass())
        .info("TEST - " + "M0028634, MSH, 2016_2016_02_26, " + authToken);
    Concept c = contentService.getConcept("M0028634", mshTerminology,
        mshVersion, null, authToken);
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
    assertEquals(mshVersion, c.getVersion());
    assertEquals("M0028634", c.getTerminologyId());
    assertFalse(c.getUsesRelationshipUnion());
    assertTrue(c.getUsesRelationshipIntersection());
    assertEquals(WorkflowStatus.PUBLISHED, c.getWorkflowStatus());
    assertEquals("loader", c.getLastModifiedBy());

    // Test SNOMEDCT_US concept
    Logger.getLogger(getClass())
        .info("TEST - " + "40667002, SNOMEDCT, 2016_03_01, " + authToken);
    c = contentService.getConcept("40667002", snomedTerminology, snomedVersion,
        null, authToken);
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
    assertEquals(1, c.getSemanticTypes().size());
    assertEquals(snomedTerminology, c.getTerminology());
    assertEquals(snomedVersion, c.getVersion());
    assertEquals("40667002", c.getTerminologyId());
    assertFalse(c.getUsesRelationshipUnion());
    assertTrue(c.getUsesRelationshipIntersection());
    assertEquals(WorkflowStatus.PUBLISHED, c.getWorkflowStatus());
    assertEquals("admin", c.getLastModifiedBy());

    // Test UMLS concept

    Logger.getLogger(getClass())
        .info("TEST - " + "C0018787, UMLS, latest, " + authToken);
    c = contentService.getConcept("C0018787", umlsTerminology, umlsVersion,
        null, authToken);
    // Validate the concept returned
    assertNotNull(c);
    assertEquals("srdce", c.getName());
    assertTrue(c.isPublishable());
    assertTrue(c.isPublished());
    assertFalse(c.isObsolete());
    assertFalse(c.isSuppressible());
    assertFalse(c.isAnonymous());
    assertFalse(c.isFullyDefined());
    assertEquals(80, c.getAtoms().size());
    assertEquals(3, c.getAttributes().size());
    // definitions still at atom level
    assertEquals(0, c.getDefinitions().size());
    // relationships require a callback by default
    assertEquals(0, c.getRelationships().size());
    assertEquals(1, c.getSemanticTypes().size());
    assertEquals(umlsTerminology, c.getTerminology());
    assertEquals(umlsVersion, c.getVersion());
    assertEquals("C0018787", c.getTerminologyId());
    assertFalse(c.getUsesRelationshipUnion());
    assertTrue(c.getUsesRelationshipIntersection());
    assertEquals(WorkflowStatus.PUBLISHED, c.getWorkflowStatus());
    assertEquals("loader", c.getLastModifiedBy());

  }

  /**
   * Test "get" methods for descriptors.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetDescriptor() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    Logger.getLogger(getClass())
        .info("TEST - " + "D019226, MSH, 2016_2016_02_26, " + authToken);
    Descriptor d = contentService.getDescriptor("D019226", mshTerminology,
        mshVersion, null, authToken);

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
    assertEquals(mshVersion, d.getVersion());
    assertEquals("D019226", d.getTerminologyId());
    assertEquals(WorkflowStatus.PUBLISHED, d.getWorkflowStatus());
    assertEquals("loader", d.getLastModifiedBy());
  }

  /**
   * Test "get" methods for codes.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetCodes() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    Logger.getLogger(getClass())
        .info("TEST - " + "D019226, MSH, 2016_2016_02_26, " + authToken);
    Code c = contentService.getCode("D019226", mshTerminology, mshVersion, null,
        authToken);

    // Validate the code returned
    assertEquals("D019226", c.getTerminologyId());
    assertEquals(mshTerminology, c.getTerminology());
    assertEquals(mshVersion, c.getVersion());

    // Test SNOMEDCT_US code
    Logger.getLogger(getClass())
        .info("TEST - " + "40667002, SNOMEDCT, 2016_03_01, " + authToken);
    c = contentService.getCode("40667002", snomedTerminology, snomedVersion,
        null, authToken);
    // Validate the code returned
    assertEquals("40667002", c.getTerminologyId());
    assertEquals(snomedTerminology, c.getTerminology());
    assertEquals(snomedVersion, c.getVersion());

  }

  /**
   * Test "get" method for lexical classes.
   * @throws Exception
   */
  @Test
  public void testGetLexicalClasses() throws Exception {
    // n/a
  }

  /**
   * Test "get" method for string classes.
   * @throws Exception
   */
  @Test
  public void testGetStringClasses() throws Exception {
    // n/a
  }

  /**
   * Test "get" methods for atom subsets
   * @throws Exception
   */
  @Test
  public void testGetAtomSubsets() throws Exception {

    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    SubsetList list = contentService.getAtomSubsets(snomedTerminology,
        snomedVersion, authToken);
    assertEquals(4, list.size());
    int foundCt = 0;
    PfsParameterJpa pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(20);
    for (Subset subset : list.getObjects()) {
      assertTrue(subset.isPublished());
      assertTrue(subset.isPublishable());
      assertFalse(subset.isObsolete());
      assertFalse(subset.isSuppressible());
      assertEquals(0, subset.getAttributes().size());
      assertEquals(subset.getDescription(), subset.getName());
      assertEquals(snomedTerminology, subset.getTerminology());
      assertEquals(snomedVersion, subset.getVersion());
      if (subset.getName().equals("GB English")) {
        foundCt++;
        assertEquals("900000000000508004", subset.getTerminologyId());
        // Get members
        SubsetMemberList memberList =
            contentService.findAtomSubsetMembers(subset.getTerminologyId(),
                snomedTerminology, snomedVersion, null, pfs, authToken);
        assertEquals(20, memberList.size());
        assertEquals(12694, memberList.getTotalCount());
        memberList =
            contentService.findAtomSubsetMembers(subset.getTerminologyId(),
                snomedTerminology, snomedVersion, "heart", pfs, authToken);
        assertEquals(15, memberList.size());
        assertEquals(15, memberList.getTotalCount());

      } else if (subset.getName().equals("US English")) {
        assertEquals("900000000000509007", subset.getTerminologyId());
        foundCt++;
        // Get members
        SubsetMemberList memberList =
            contentService.findAtomSubsetMembers(subset.getTerminologyId(),
                snomedTerminology, snomedVersion, null, pfs, authToken);
        assertEquals(20, memberList.size());
        assertEquals(12689, memberList.getTotalCount());
        memberList =
            contentService.findAtomSubsetMembers(subset.getTerminologyId(),
                snomedTerminology, snomedVersion, "heart", pfs, authToken);
        assertEquals(15, memberList.size());
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
        assertEquals(snomedVersion, member.getVersion());
      } else if (subset.getName()
          .equals("REFERS TO concept association reference set")) {
        assertEquals("900000000000531004", subset.getTerminologyId());
        foundCt++;
        // Get members
        SubsetMemberList memberList =
            contentService.findAtomSubsetMembers(subset.getTerminologyId(),
                snomedTerminology, snomedVersion, null, pfs, authToken);
        assertEquals(20, memberList.size());
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
  public void testGetConceptSubsets() throws Exception {

    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    SubsetList list = contentService.getConceptSubsets(snomedTerminology,
        snomedVersion, authToken);
    assertEquals(17, list.size());
    int foundCt = 0;
    PfsParameterJpa pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(20);
    for (Subset subset : list.getObjects()) {
      if (subset instanceof ConceptSubsetJpa
          && ((ConceptSubsetJpa) subset).isLabelSubset()) {
        continue;
      }
      assertTrue(subset.isPublished());
      assertTrue(subset.isPublishable());
      assertEquals(0, subset.getAttributes().size());
      assertEquals(subset.getDescription(), subset.getName());
      assertEquals(snomedTerminology, subset.getTerminology());
      assertEquals(snomedVersion, subset.getVersion());
      if (subset.getName().equals("SAME AS association reference set")) {
        foundCt++;
        assertFalse(subset.isObsolete());
        assertFalse(subset.isSuppressible());
        assertEquals("900000000000527005", subset.getTerminologyId());
        // Get members
        SubsetMemberList memberList =
            contentService.findConceptSubsetMembers(subset.getTerminologyId(),
                snomedTerminology, snomedVersion, null, pfs, authToken);
        assertEquals(20, memberList.size());
        assertEquals(1029, memberList.getTotalCount());
        memberList =
            contentService.findConceptSubsetMembers(subset.getTerminologyId(),
                snomedTerminology, snomedVersion, "Karyotype", pfs, authToken);
        assertEquals(2, memberList.size());
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
        assertEquals(5, memberList.size());
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
        assertEquals(20, memberList.size());
        assertEquals(1153, memberList.getTotalCount());
        memberList =
            contentService.findConceptSubsetMembers(subset.getTerminologyId(),
                snomedTerminology, snomedVersion, "syndrome", pfs, authToken);
        assertEquals(20, memberList.size());
        assertEquals(116, memberList.getTotalCount());
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
        assertEquals(snomedVersion, member.getVersion());
      }
    }
    assertEquals(3, foundCt);

  }

  /**
   * Test "find" concepts for query.
   * @throws Exception
   */
  @Test
  public void testFindConcepts() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    PfsParameterJpa pfs = new PfsParameterJpa();
    SearchResultList searchResults;

    // Simple query, empty pfs
    Logger.getLogger(getClass()).info("  Simple query, empty pfs");
    searchResults = contentService.findConcepts(snomedTerminology,
        snomedVersion, "care", null, authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + searchResults.getTotalCount());
    assertEquals(19, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(19, searchResults.size());

    // Simple query with spaces, empty pfs
    Logger.getLogger(getClass()).info("  Simple query, empty pfs");
    searchResults = contentService.findConcepts(snomedTerminology,
        snomedVersion, "heart disease", null, authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + searchResults.getTotalCount());
    assertEquals(217, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(217, searchResults.size());

    // Complex fielded query, empty pfs
    Logger.getLogger(getClass()).info("  Simple query, empty pfs");
    searchResults =
        contentService.findConcepts(snomedTerminology, snomedVersion,
            "heart disease AND obsolete:false AND suppressible:false AND published:true",
            null, authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + searchResults.getTotalCount());
    assertEquals(210, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(210, searchResults.size());

    // Simple query, sorted on name
    Logger.getLogger(getClass()).info("  Simple query, sorted on name");
    pfs.setSortField("name");
    searchResults = contentService.findConcepts(snomedTerminology,
        snomedVersion, "care", pfs, authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + searchResults.getTotalCount());
    assertEquals(19, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(19, searchResults.size());
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfs,
        ConceptJpa.class));

    // Simple query, sorted on name, descending order
    Logger.getLogger(getClass())
        .info("  Simple query, sorted on name, descending order");
    pfs.setAscending(false);
    searchResults = contentService.findConcepts(snomedTerminology,
        snomedVersion, "care", pfs, authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + searchResults.getTotalCount());
    assertEquals(19, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(19, searchResults.size());
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfs,
        ConceptJpa.class));

    // store the sorted results for later comparison
    SearchResultList sortedResults = searchResults;

    // Simple query, paged and sorted results, first page
    Logger.getLogger(getClass())
        .info("  Simple query, paged and sorted results, first page");
    pfs.setSortField("name");
    pfs.setStartIndex(0);
    pfs.setMaxResults(5);
    searchResults = contentService.findConcepts(snomedTerminology,
        snomedVersion, "care", pfs, authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + searchResults.getTotalCount());
    assertEquals(19, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfs,
        ConceptJpa.class));
    assertTrue(PfsParameterForComponentTest.testPaging(searchResults,
        sortedResults, pfs));

    // Simple query, paged and sorted results, second page
    Logger.getLogger(getClass())
        .info("  Simple query, paged and sorted results, second page");
    pfs.setSortField("name");
    pfs.setStartIndex(5);
    pfs.setMaxResults(5);
    searchResults = contentService.findConcepts(snomedTerminology,
        snomedVersion, "care", pfs, authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + searchResults.getTotalCount());
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
    searchResults = contentService.findConcepts(snomedTerminology,
        snomedVersion, "care", pfs, authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + searchResults.getTotalCount());
    assertEquals(1, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(1, searchResults.size());
    assertTrue(searchResults.getObjects().get(0).getTerminologyId()
        .equals("169559003"));

    // Simple query, for "active only", empty pfs
    Logger.getLogger(getClass())
        .info("  Simple query, for \"active only\", empty pfs");
    pfs = new PfsParameterJpa();
    pfs.setActiveOnly(true);
    searchResults = contentService.findConcepts(snomedTerminology,
        snomedVersion, "care", pfs, authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + searchResults.getTotalCount());
    assertEquals(19, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }

    // No query active only, first page
    Logger.getLogger(getClass()).info("  No query active only, first page");
    pfs = new PfsParameterJpa();
    pfs.setActiveOnly(true);
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    searchResults = contentService.findConcepts(snomedTerminology,
        snomedVersion, null, pfs, authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + searchResults.getTotalCount());
    Logger.getLogger(getClass()).info("    count = " + searchResults.size());
    assertEquals(3902, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(10, searchResults.size());

    // No query, inactive only, first page
    Logger.getLogger(getClass()).info("  No query, inactive only, first page");
    pfs = new PfsParameterJpa();
    pfs.setInactiveOnly(true);
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    searchResults = contentService.findConcepts(snomedTerminology,
        snomedVersion, null, pfs, authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + searchResults.getTotalCount());
    assertEquals(0, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(0, searchResults.size());

    // Simple query, active only, first page
    Logger.getLogger(getClass())
        .info("  Simple query, active only, first page");
    pfs = new PfsParameterJpa();
    pfs.setActiveOnly(true);
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    searchResults = contentService.findConcepts(snomedTerminology,
        snomedVersion, "disease", pfs, authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + searchResults.getTotalCount());
    assertEquals(210, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(10, searchResults.size());
    for (SearchResult sr : searchResults.getObjects()) {
      assertTrue(sr.getValue().contains("disease"));
    }

    // Simple query, inactive only, first page
    Logger.getLogger(getClass())
        .info("  Simple query, inactive only, first page");
    pfs = new PfsParameterJpa();
    pfs.setInactiveOnly(true);
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    searchResults = contentService.findConcepts(snomedTerminology,
        snomedVersion, "disease", pfs, authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + searchResults.getTotalCount());
    assertEquals(0, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(0, searchResults.size());

  }

  /**
   * Test "find" descriptors by query.
   * @throws Exception
   */
  @Test
  public void testFindDescriptors() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    String query = "amino*";
    PfsParameterJpa pfs = new PfsParameterJpa();
    SearchResultList searchResults;

    // Simple query, empty pfs
    Logger.getLogger(getClass()).info("  Simple query, empty pfs");
    searchResults = contentService.findDescriptors(mshTerminology, mshVersion,
        query, pfs, authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + searchResults.getTotalCount());
    assertEquals(21, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(21, searchResults.size());

    // Simple query, sort by name
    Logger.getLogger(getClass()).info("  Simple query, sort by name");
    pfs.setSortField("name");
    searchResults = contentService.findDescriptors(mshTerminology, mshVersion,
        query, pfs, authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + searchResults.getTotalCount());
    assertEquals(21, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(21, searchResults.size());
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfs,
        DescriptorJpa.class));

    // Simple query, sort by name descending
    Logger.getLogger(getClass())
        .info("  Simple query, sort by name, descending");
    pfs.setAscending(false);
    searchResults = contentService.findDescriptors(mshTerminology, mshVersion,
        query, pfs, authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + searchResults.getTotalCount());
    assertEquals(21, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(21, searchResults.size());
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfs,
        DescriptorJpa.class));

    // store the sorted results
    SearchResultList sortedResults = searchResults;

    // Simple query, sort by name, page
    Logger.getLogger(getClass())
        .info("  Simple query, sort by name, first page");
    pfs.setSortField("name");
    pfs.setStartIndex(0);
    pfs.setMaxResults(5);
    searchResults = contentService.findDescriptors(mshTerminology, mshVersion,
        query, pfs, authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + searchResults.getTotalCount());
    assertEquals(21, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfs,
        DescriptorJpa.class));
    assertTrue(PfsParameterForComponentTest.testPaging(searchResults,
        sortedResults, pfs));

    // Simple query, sort by name, page
    Logger.getLogger(getClass())
        .info("  Simple query, sort by name, second page");
    pfs.setSortField("name");
    pfs.setStartIndex(5);
    pfs.setMaxResults(5);
    searchResults = contentService.findDescriptors(mshTerminology, mshVersion,
        query, pfs, authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + searchResults.getTotalCount());
    assertEquals(21, searchResults.getTotalCount());
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
    searchResults = contentService.findDescriptors(mshTerminology, mshVersion,
        query, pfs, authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + searchResults.getTotalCount());
    assertEquals(1, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(1, searchResults.size());
    assertTrue(
        searchResults.getObjects().get(0).getTerminologyId().equals("C118284"));

    pfs = new PfsParameterJpa();
    pfs.setActiveOnly(true);

    // No query, ia active only
    Logger.getLogger(getClass()).info("  No query, active only");
    searchResults = contentService.findDescriptors(mshTerminology, mshVersion,
        "", pfs, authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + searchResults.getTotalCount());
    assertEquals(997, searchResults.getTotalCount());

    // No query, active only with paging
    Logger.getLogger(getClass()).info("  No query, active only with paging");
    pfs = new PfsParameterJpa();
    pfs.setActiveOnly(true);
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    searchResults = contentService.findDescriptors(mshTerminology, mshVersion,
        "", pfs, authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + searchResults.getTotalCount());
    assertEquals(997, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(10, searchResults.size());

    // No query, inactive only with paging
    Logger.getLogger(getClass()).info("  No query, inactive only with paging");
    pfs = new PfsParameterJpa();
    pfs.setInactiveOnly(true);
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    searchResults = contentService.findDescriptors(mshTerminology, mshVersion,
        "", pfs, authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + searchResults.getTotalCount());
    assertEquals(0, searchResults.size());

    // No query, active only and primitive only
    Logger.getLogger(getClass())
        .info("  No query, active only and primitive only");
    pfs = new PfsParameterJpa();
    pfs.setActiveOnly(true);
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    searchResults = contentService.findDescriptors(mshTerminology, mshVersion,
        "", pfs, authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + searchResults.getTotalCount());
    assertEquals(997, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(10, searchResults.size());

    // Simple query and active only with paging
    Logger.getLogger(getClass())
        .info("  Simple query and active only with paging");
    pfs = new PfsParameterJpa();
    pfs.setActiveOnly(true);
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    searchResults = contentService.findDescriptors(mshTerminology, mshVersion,
        "disease", pfs, authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + searchResults.getTotalCount());
    assertEquals(69, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(10, searchResults.size());

    // Simple query and inactive active only with paging
    Logger.getLogger(getClass())
        .info("  Simple query and inactive only with paging");
    pfs = new PfsParameterJpa();
    pfs.setInactiveOnly(true);
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    searchResults = contentService.findDescriptors(mshTerminology, mshVersion,
        "disease", pfs, authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + searchResults.getTotalCount());
    assertEquals(0, searchResults.getTotalCount());
    assertEquals(0, searchResults.size());
  }

  /**
   * Test "find" codes by query.
   * @throws Exception
   */
  @Test
  public void testFindCodes() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    String query = "amino*";
    PfsParameterJpa pfs = new PfsParameterJpa();
    SearchResultList searchResults;

    // Simple query, empty pfs
    Logger.getLogger(getClass()).info("  Simple query, empty pfs");
    searchResults = contentService.findCodes(mshTerminology, mshVersion, query,
        pfs, authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + searchResults.getTotalCount());
    assertEquals(21, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(21, searchResults.size());

    // Simple query, sort by name
    Logger.getLogger(getClass()).info("  Simple query, sort by name");
    pfs.setSortField("name");
    searchResults = contentService.findCodes(mshTerminology, mshVersion, query,
        pfs, authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + searchResults.getTotalCount());
    assertEquals(21, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(21, searchResults.size());
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfs,
        CodeJpa.class));

    // Simple query, sort by name descending
    Logger.getLogger(getClass())
        .info("  Simple query, sort by name, descending");
    pfs.setAscending(false);
    searchResults = contentService.findCodes(mshTerminology, mshVersion, query,
        pfs, authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + searchResults.getTotalCount());
    assertEquals(21, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(21, searchResults.size());
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfs,
        CodeJpa.class));

    // store the sorted results
    SearchResultList sortedResults = searchResults;

    // Simple query, sort by name, page
    Logger.getLogger(getClass())
        .info("  Simple query, sort by name, first page");
    pfs.setSortField("name");
    pfs.setStartIndex(0);
    pfs.setMaxResults(5);
    searchResults = contentService.findCodes(mshTerminology, mshVersion, query,
        pfs, authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + searchResults.getTotalCount());
    assertEquals(21, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfs,
        CodeJpa.class));
    assertTrue(PfsParameterForComponentTest.testPaging(searchResults,
        sortedResults, pfs));

    // Simple query, sort by name, page
    Logger.getLogger(getClass())
        .info("  Simple query, sort by name, second page");
    pfs.setSortField("name");
    pfs.setStartIndex(5);
    pfs.setMaxResults(5);
    searchResults = contentService.findCodes(mshTerminology, mshVersion, query,
        pfs, authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + searchResults.getTotalCount());
    assertEquals(21, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    // assertTrue(PfsParameterForComponentTest.testPaging(searchResults,
    // sortedResults, pfs));
    // assertTrue(PfsParameterForComponentTest.testSort(searchResults, pfs,
    // CodeJpa.class));

    // More complex query using query restriction
    Logger.getLogger(getClass()).info("  Simple query with query restriction");
    pfs = new PfsParameterJpa();
    pfs.setQueryRestriction("terminologyId:C118284");
    searchResults = contentService.findCodes(mshTerminology, mshVersion, query,
        pfs, authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + searchResults.getTotalCount());
    assertEquals(1, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(1, searchResults.size());
    assertTrue(
        searchResults.getObjects().get(0).getTerminologyId().equals("C118284"));

    pfs = new PfsParameterJpa();
    pfs.setActiveOnly(true);
    // No query, is active only
    Logger.getLogger(getClass()).info("  No query, active only");
    searchResults = contentService.findCodes(mshTerminology, mshVersion, "",
        pfs, authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + searchResults.getTotalCount());
    assertEquals(997, searchResults.getTotalCount());

    // No query, active only with paging
    Logger.getLogger(getClass()).info("  No query, active only with paging");
    pfs = new PfsParameterJpa();
    pfs.setActiveOnly(true);
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    searchResults = contentService.findCodes(mshTerminology, mshVersion, "",
        pfs, authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + searchResults.getTotalCount());
    assertEquals(997, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(10, searchResults.size());

    // No query, inactive only with paging
    Logger.getLogger(getClass()).info("  No query, inactive only with paging");
    pfs = new PfsParameterJpa();
    pfs.setInactiveOnly(true);
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    searchResults = contentService.findCodes(mshTerminology, mshVersion, "",
        pfs, authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + searchResults.getTotalCount());
    assertEquals(0, searchResults.size());

    // Simple query and active only with paging
    Logger.getLogger(getClass())
        .info("  Simple query and active only with paging");
    pfs = new PfsParameterJpa();
    pfs.setActiveOnly(true);
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    searchResults = contentService.findCodes(mshTerminology, mshVersion,
        "disease", pfs, authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + searchResults.getTotalCount());
    assertEquals(69, searchResults.getTotalCount());
    for (SearchResult sr : searchResults.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + sr.getTerminologyId());
    }
    assertEquals(10, searchResults.size());

    // Simple query and inactive active only with paging
    Logger.getLogger(getClass())
        .info("  Simple query and inactive only with paging");
    pfs = new PfsParameterJpa();
    pfs.setInactiveOnly(true);
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    searchResults = contentService.findCodes(mshTerminology, mshVersion,
        "disease", pfs, authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + searchResults.getTotalCount());
    assertEquals(0, searchResults.getTotalCount());
    assertEquals(0, searchResults.size());

  }

  /**
   * Test ancestor/descendant for concepts.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindDescendantConcepts() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    PfsParameterJpa pfs = new PfsParameterJpa();
    ConceptList conceptList;

    // Get descendants for SNOMEDCT concept
    Logger.getLogger(getClass()).info("  Test concept descendants, empty pfs");
    conceptList = contentService.findDescendantConcepts("105590001",
        snomedTerminology, snomedVersion, false, pfs, authToken);
    Logger.getLogger(getClass())
        .info("    totalResults = " + conceptList.getTotalCount());
    assertEquals(62, conceptList.getTotalCount());
    assertEquals(62, conceptList.size());

    // Get ancestors for SNOMEDCT concept
    Logger.getLogger(getClass()).info("  Test concept ancestors, empty pfs");
    conceptList = contentService.findAncestorConcepts("10697004",
        snomedTerminology, snomedVersion, false, pfs, authToken);
    Logger.getLogger(getClass())
        .info("    totalResults = " + conceptList.getTotalCount());
    assertEquals(3, conceptList.getTotalCount());
    assertEquals(3, conceptList.size());

    pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(2);

    // Get descendants for SNOMEDCT concept with paging
    Logger.getLogger(getClass())
        .info("  Test concept descendants, with paging ");
    conceptList = contentService.findDescendantConcepts("105590001",
        snomedTerminology, snomedVersion, false, pfs, authToken);
    Logger.getLogger(getClass())
        .info("    totalResults = " + conceptList.getTotalCount());
    assertEquals(62, conceptList.getTotalCount());
    assertEquals(2, conceptList.size());

    // Get ancestors for SNOMEDCT concept
    Logger.getLogger(getClass()).info("  Test concept ancestors, with paging");
    conceptList = contentService.findAncestorConcepts("10697004",
        snomedTerminology, snomedVersion, false, pfs, authToken);
    Logger.getLogger(getClass())
        .info("    totalResults = " + conceptList.getTotalCount());
    assertEquals(3, conceptList.getTotalCount());
    assertEquals(2, conceptList.size());

  }

  /**
   * Test ancestor/descendant for descriptors.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindDescendantDescriptors() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    PfsParameterJpa pfs = new PfsParameterJpa();
    DescriptorList descriptorList;

    // Get descendants for MSH descriptor
    Logger.getLogger(getClass())
        .info("  Test descriptor descendants, empty pfs");
    descriptorList = contentService.findDescendantDescriptors("D000005",
        mshTerminology, mshVersion, false, pfs, authToken);
    Logger.getLogger(getClass())
        .info("    totalResults = " + descriptorList.getTotalCount());
    assertEquals(4, descriptorList.getTotalCount());
    assertEquals(4, descriptorList.size());

    // Get ancestors for MSH Descriptor
    Logger.getLogger(getClass()).info("  Test descriptor ancestors, empty pfs");
    descriptorList = contentService.findAncestorDescriptors("D000009",
        mshTerminology, mshVersion, false, pfs, authToken);
    Logger.getLogger(getClass())
        .info("    totalResults = " + descriptorList.getTotalCount());
    assertEquals(4, descriptorList.getTotalCount());
    assertEquals(4, descriptorList.size());

    pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(2);

    // Get descendants for MSH descriptor with paging
    Logger.getLogger(getClass())
        .info("  Test descriptor descendants, with paging ");
    descriptorList = contentService.findDescendantDescriptors("D000005",
        mshTerminology, mshVersion, false, pfs, authToken);
    Logger.getLogger(getClass())
        .info("    totalResults = " + descriptorList.getTotalCount());
    assertEquals(4, descriptorList.getTotalCount());
    assertEquals(2, descriptorList.size());

    // Get ancestors for MSH descriptor
    Logger.getLogger(getClass())
        .info("  Test descriptor ancestors, with paging");
    descriptorList = contentService.findAncestorDescriptors("D000009",
        mshTerminology, mshVersion, false, pfs, authToken);
    Logger.getLogger(getClass())
        .info("    totalResults = " + descriptorList.getTotalCount());
    assertEquals(4, descriptorList.getTotalCount());
    assertEquals(2, descriptorList.size());
  }

  /**
   * Test ancestor/descendant for codes.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindDescendantCodes() throws Exception {
    // n/a - no code ancestors or descendants
  }

  /**
   * Test "find" subset members for atom or concept.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetSubsetMembers() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    Logger.getLogger(getClass()).info("  Test get subset members for atom");
    SubsetMemberList list = contentService.getAtomSubsetMembers("166113012",
        snomedTerminology, snomedVersion, authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + list.getTotalCount());
    assertEquals(3, list.getTotalCount());
    assertEquals(3, list.size());

    Logger.getLogger(getClass()).info("  Test get subset members for concept");
    list = contentService.getConceptSubsetMembers("10123006", snomedTerminology,
        snomedVersion, authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + list.getTotalCount());
    assertEquals(5, list.getTotalCount());
    assertEquals(5, list.size());

  }

  /**
   * Test autocomplete for concepts.
   *
   * @throws Exception the exception
   */
  @Test
  public void testAutocompleteConcepts() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    Logger.getLogger(getClass())
        .info("  Test autocomplete for snomed concepts");
    StringList list = contentService.autocompleteConcepts(snomedTerminology,
        snomedVersion, "let", authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + list.getTotalCount());
    assertEquals(70, list.getTotalCount());
    assertEquals(19, list.size());

    list = contentService.autocompleteConcepts(snomedTerminology, snomedVersion,
        "lett", authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + list.getTotalCount());
    assertEquals(73, list.getTotalCount());
    assertEquals(19, list.size());

    list = contentService.autocompleteConcepts(snomedTerminology, snomedVersion,
        "lettu", authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + list.getTotalCount());
    assertEquals(73, list.getTotalCount());
    assertEquals(19, list.size());

    Logger.getLogger(getClass()).info("  Test autocomplete for msh concepts");
    list = contentService.autocompleteConcepts(mshTerminology, mshVersion,
        "let", authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + list.getTotalCount());
    assertEquals(19, list.getTotalCount());
    assertEquals(19, list.size());

    list = contentService.autocompleteConcepts(mshTerminology, mshVersion,
        "lett", authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + list.getTotalCount());
    assertEquals(22, list.getTotalCount());
    assertEquals(20, list.size());

    list = contentService.autocompleteConcepts(mshTerminology, mshVersion,
        "lettu", authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + list.getTotalCount());
    assertEquals(22, list.getTotalCount());
    assertEquals(20, list.size());

    Logger.getLogger(getClass()).info("  Test autocomplete for umls concepts");
    list = contentService.autocompleteConcepts(umlsTerminology, umlsVersion,
        "let", authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + list.getTotalCount());
    assertEquals(235, list.getTotalCount());
    assertEquals(20, list.size());

    list = contentService.autocompleteConcepts(umlsTerminology, umlsVersion,
        "lett", authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + list.getTotalCount());
    assertEquals(315, list.getTotalCount());
    assertEquals(20, list.size());

    list = contentService.autocompleteConcepts(umlsTerminology, umlsVersion,
        "lettu", authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + list.getTotalCount());
    assertEquals(330, list.getTotalCount());
    assertEquals(20, list.size());

  }

  /**
   * Test autocomplete for descriptors
   *
   * @throws Exception the exception
   */
  @Test
  public void testAutocompleteDescriptors() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    Logger.getLogger(getClass())
        .info("  Test autocomplete for msh descriptors");
    StringList list = contentService.autocompleteConcepts(mshTerminology,
        mshVersion, "let", authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + list.getTotalCount());
    assertEquals(19, list.getTotalCount());
    assertEquals(19, list.size());

    list = contentService.autocompleteConcepts(mshTerminology, mshVersion,
        "lett", authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + list.getTotalCount());
    assertEquals(22, list.getTotalCount());
    assertEquals(20, list.size());

    list = contentService.autocompleteConcepts(mshTerminology, mshVersion,
        "lettu", authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + list.getTotalCount());
    assertEquals(22, list.getTotalCount());
    assertEquals(20, list.size());

  }

  /**
   * Test autocomplete for codes.
   *
   * @throws Exception the exception
   */
  @Test
  public void testAutocompleteCodes() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    Logger.getLogger(getClass()).info("  Test autocomplete for snomed codes");
    StringList list = contentService.autocompleteCodes(snomedTerminology,
        snomedVersion, "let", authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + list.getTotalCount());
    assertEquals(70, list.getTotalCount());
    assertEquals(19, list.size());

    list = contentService.autocompleteCodes(snomedTerminology, snomedVersion,
        "lett", authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + list.getTotalCount());
    assertEquals(73, list.getTotalCount());
    assertEquals(19, list.size());

    list = contentService.autocompleteCodes(snomedTerminology, snomedVersion,
        "lettu", authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + list.getTotalCount());
    assertEquals(73, list.getTotalCount());
    assertEquals(19, list.size());

    Logger.getLogger(getClass()).info("  Test autocomplete for msh codes");
    list = contentService.autocompleteCodes("MTH", "latest", "hys", authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + list.getTotalCount());
    assertEquals(2, list.getTotalCount());
    assertEquals(2, list.size());

    list = contentService.autocompleteCodes("MTH", "latest", "mesn", authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + list.getTotalCount());
    assertEquals(1, list.getTotalCount());
    assertEquals(1, list.size());

    list =
        contentService.autocompleteCodes("MTH", "latest", "mesna", authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + list.getTotalCount());
    assertEquals(1, list.getTotalCount());
    assertEquals(1, list.size());

  }

  /**
   * Test get of deep relationships for a concept.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGetDeepRelationships() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // simple deep rels call
    Logger.getLogger(getClass()).info("  Test deep relationships");
    RelationshipList list = contentService.findConceptDeepRelationships(
        "C0000097", "MTH", "latest", false, false, false, false, new PfsParameterJpa(), null, authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + list.getTotalCount());
    assertEquals(128, list.getTotalCount());
    assertEquals(128, list.size());
    RelationshipList fullList = list;

    PfsParameterJpa pfs = new PfsParameterJpa();

    // deep rels call with paging
    Logger.getLogger(getClass()).info("  Test deep relationships with paging");
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    list = contentService.findConceptDeepRelationships("C0000097", "MTH",
        "latest", false, false, false, false, pfs, null, authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + list.getTotalCount());
    assertEquals(128, list.getTotalCount());
    assertEquals(10, list.size());
    assertTrue(PfsParameterForComponentTest.testPaging(list, fullList, pfs));

    // deep rels call with sorting
    Logger.getLogger(getClass()).info("  Test deep relationships with paging");
    pfs = new PfsParameterJpa();
    pfs.setSortField("relationshipType");
    list = contentService.findConceptDeepRelationships("C0000097", "MTH",
        "latest", false, false, false, false, pfs, null, authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + list.getTotalCount());
    assertEquals(128, list.getTotalCount());
    assertEquals(128, list.size());
    fullList = list;

    // deep rels call with sorting and paging
    Logger.getLogger(getClass())
        .info("  Test deep relationships with sorting and paging");
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    pfs.setSortField("relationshipType");
    list = contentService.findConceptDeepRelationships("C0000097", "MTH",
        "latest", false, false, false, false, pfs, null, authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + list.getTotalCount());
    assertEquals(128, list.getTotalCount());
    assertEquals(10, list.size());
    // assertTrue(PfsParameterForComponentTest.testPaging(list, fullList, pfs));
    // assertTrue(PfsParameterForComponentTest.testSort(list, pfs,
    // AbstractRelationship.class));

    // deep rels call with sorting and paging, page 2
    Logger.getLogger(getClass())
        .info("  Test deep relationships with sorting and paging");
    pfs.setStartIndex(10);
    pfs.setMaxResults(10);
    pfs.setSortField("relationshipType");
    list = contentService.findConceptDeepRelationships("C0000097", "MTH",
        "latest", false, false, false, false, pfs, null, authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + list.getTotalCount());
    assertEquals(128, list.getTotalCount());
    assertEquals(10, list.size());
    // assertTrue(PfsParameterForComponentTest.testPaging(list, fullList, pfs));
    // assertTrue(PfsParameterForComponentTest.testSort(list, pfs,
    // AbstractRelationship.class));

  }

  /**
   * Test find trees for concepts.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindConceptTrees() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // tree lookup, empty pfs
    Logger.getLogger(getClass()).info("  Tree lookup, empty pfs");
    TreeList list = contentService.findConceptTrees("422089004",
        snomedTerminology, snomedVersion, new PfsParameterJpa(), authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + list.getTotalCount());
    assertEquals(4, list.getTotalCount());
    for (Tree tree : list.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + tree);
    }
    assertEquals(4, list.size());

    TreeList fullList = list;
    PfsParameterJpa pfs = new PfsParameterJpa();

    // tree lookup, first page
    Logger.getLogger(getClass()).info("  Tree lookup, first page");
    pfs.setStartIndex(0);
    pfs.setMaxResults(2);
    list = contentService.findConceptTrees("422089004", snomedTerminology,
        snomedVersion, pfs, authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + list.getTotalCount());
    assertEquals(4, list.getTotalCount());
    for (Tree tree : list.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + tree);
    }
    assertEquals(2, list.size());
    assertTrue(PfsParameterForComponentTest.testPaging(list, fullList, pfs));

    // tree lookup, second page
    Logger.getLogger(getClass()).info("  Tree lookup, second page");
    pfs.setStartIndex(2);
    pfs.setMaxResults(2);
    list = contentService.findConceptTrees("422089004", snomedTerminology,
        snomedVersion, pfs, authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + list.getTotalCount());
    assertEquals(4, list.getTotalCount());
    for (Tree tree : list.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + tree);
    }
    assertEquals(2, list.size());
    assertTrue(PfsParameterForComponentTest.testPaging(list, fullList, pfs));

    // tree lookup, first page and sort order
    Logger.getLogger(getClass()).info("  Tree lookup, first page");
    pfs.setStartIndex(0);
    pfs.setMaxResults(2);
    pfs.setSortField("nodeTerminologyId");
    list = contentService.findConceptTrees("422089004", snomedTerminology,
        snomedVersion, pfs, authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + list.getTotalCount());
    assertEquals(4, list.getTotalCount());
    for (Tree tree : list.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + tree);
    }
    assertEquals(2, list.size());
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
  public void testFindDescriptorTrees() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // tree lookup, empty pfs
    Logger.getLogger(getClass()).info("  Tree lookup, empty pfs");
    TreeList list = contentService.findDescriptorTrees("D018410",
        mshTerminology, mshVersion, new PfsParameterJpa(), authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + list.getTotalCount());
    assertEquals(3, list.getTotalCount());
    for (Tree tree : list.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + tree);
    }
    assertEquals(3, list.size());

    TreeList fullList = list;
    PfsParameterJpa pfs = new PfsParameterJpa();

    // tree lookup, first page
    Logger.getLogger(getClass()).info("  Tree lookup, first page");
    pfs.setStartIndex(0);
    pfs.setMaxResults(1);
    list = contentService.findDescriptorTrees("D018410", mshTerminology,
        mshVersion, pfs, authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + list.getTotalCount());
    assertEquals(3, list.getTotalCount());
    for (Tree tree : list.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + tree);
    }
    assertEquals(1, list.size());
    assertTrue(PfsParameterForComponentTest.testPaging(list, fullList, pfs));

    // tree lookup, second page
    Logger.getLogger(getClass()).info("  Tree lookup, second page");
    pfs.setStartIndex(1);
    pfs.setMaxResults(1);
    list = contentService.findDescriptorTrees("D018410", mshTerminology,
        mshVersion, pfs, authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + list.getTotalCount());
    assertEquals(3, list.getTotalCount());
    for (Tree tree : list.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + tree);
    }
    assertEquals(1, list.size());
    assertTrue(PfsParameterForComponentTest.testPaging(list, fullList, pfs));

    // tree lookup, first page and sort order
    Logger.getLogger(getClass()).info("  Tree lookup, first page");
    pfs.setStartIndex(0);
    pfs.setMaxResults(1);
    pfs.setSortField("nodeTerminologyId");
    list = contentService.findDescriptorTrees("D018410", mshTerminology,
        mshVersion, pfs, authToken);
    Logger.getLogger(getClass())
        .info("    totalCount = " + list.getTotalCount());
    assertEquals(3, list.getTotalCount());
    for (Tree tree : list.getObjects()) {
      Logger.getLogger(getClass()).info("    Result: " + tree);
    }
    assertEquals(1, list.size());
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
  public void testFindCodeTrees() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    // n/a - no sample data

  }

  /**
   * Test general query mechanism.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGeneralQuery() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    /** Find concepts with hql query */
    Logger.getLogger(getClass()).info("TEST1 - "
        + "SELECT c FROM ConceptJpa c, SNOMEDCT_US, 2016_03_01, " + authToken);
    SearchResultList sml = contentService.findConceptsForGeneralQuery("",
        "SELECT c FROM ConceptJpa c WHERE c.terminologyId != c.id",
        new PfsParameterJpa(), authToken);
    assertEquals(30617, sml.size());

    /** Find concepts with hql query and pfs parameter max results 20 */
    PfsParameterJpa pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(20);
    Logger.getLogger(getClass()).info(
        "TEST2 - " + "SELECT c FROM ConceptJpa c, SNOMEDCT_US, 2016_03_01, "
            + pfs + authToken);
    sml = contentService.findConceptsForGeneralQuery("",
        "SELECT c FROM ConceptJpa c WHERE c.terminologyId != c.id", pfs,
        authToken);
    assertEquals(20, sml.size());
    assertEquals(30617, sml.getTotalCount());

    /** Find concepts in intersection of lucene and hql queries */
    Logger.getLogger(getClass())
        .info("TEST3 - "
            + "name:amino, SELECT c FROM ConceptJpa c, SNOMEDCT_US, 2016_03_01, "
            + authToken);
    sml = contentService.findConceptsForGeneralQuery("name:amino",
        "SELECT c FROM ConceptJpa c", new PfsParameterJpa(), authToken);
    assertEquals(37, sml.size());
    assertEquals(37, sml.getTotalCount());

    /** Find concepts in lucene query */
    Logger.getLogger(getClass())
        .info("TEST4 - " + "name:amino, SNOMEDCT_US, 2016_03_01, " + authToken);
    sml = contentService.findConceptsForGeneralQuery("name:amino", "",
        new PfsParameterJpa(), authToken);
    assertEquals(37, sml.size());
    assertEquals(37, sml.getTotalCount());

    /** Find descriptors with hql query */
    Logger.getLogger(getClass())
        .info("TEST5 - "
            + "SELECT c FROM DescriptorJpa c, SNOMEDCT_US, 2016_03_01, "
            + authToken);
    sml = contentService.findDescriptorsForGeneralQuery("",
        "SELECT c FROM DescriptorJpa c", new PfsParameterJpa(), authToken);
    assertEquals(21116, sml.size());

    /** Find descriptors with hql query and pfs parameter max results 20 */
    pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(20);
    Logger.getLogger(getClass())
        .info("TEST6 - "
            + "SELECT c FROM DescriptorJpa c, SNOMEDCT_US, 2016_03_01, " + pfs
            + authToken);
    sml = contentService.findDescriptorsForGeneralQuery("",
        "SELECT c FROM DescriptorJpa c", pfs, authToken);
    assertEquals(20, sml.size());
    assertEquals(21116, sml.getTotalCount());

    /** Find descriptors in intersection of lucene and hql queries */
    Logger.getLogger(getClass())
        .info("TEST7 - "
            + "name:amino, SELECT c FROM DescriptorJpa c, SNOMEDCT_US, 2016_03_01, "
            + authToken);
    sml = contentService.findDescriptorsForGeneralQuery("name:amino",
        "SELECT c FROM DescriptorJpa c", new PfsParameterJpa(), authToken);
    assertEquals(14, sml.size());
    assertEquals(14, sml.getTotalCount());

    /** Find descriptors in lucene query */
    Logger.getLogger(getClass())
        .info("TEST8 - " + "name:amino, SNOMEDCT_US, 2016_03_01, " + authToken);
    sml = contentService.findDescriptorsForGeneralQuery("name:amino", "",
        new PfsParameterJpa(), authToken);
    assertEquals(14, sml.size());
    assertEquals(14, sml.getTotalCount());

    /** Find codes with hql query */
    Logger.getLogger(getClass()).info("TEST9 - "
        + "SELECT c FROM CodeJpa c, SNOMEDCT_US, 2016_03_01, " + authToken);
    sml = contentService.findCodesForGeneralQuery("", "SELECT c FROM CodeJpa c",
        new PfsParameterJpa(), authToken);
    assertEquals(71663, sml.size());

    /** Find codes with hql query and pfs parameter max results 20 */
    pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(20);
    Logger.getLogger(getClass())
        .info("TEST10 - " + "SELECT c FROM CodeJpa c, SNOMEDCT_US, 2016_03_01, "
            + pfs + authToken);
    sml = contentService.findCodesForGeneralQuery("", "SELECT c FROM CodeJpa c",
        pfs, authToken);
    assertEquals(20, sml.size());
    assertEquals(71663, sml.getTotalCount());

    /** Find codes in intersection of lucene and hql queries */
    Logger.getLogger(getClass())
        .info("TEST11 - "
            + "name:amino, SELECT c FROM CodeJpa c, SNOMEDCT_US, 2016_03_01, "
            + authToken);
    sml = contentService.findCodesForGeneralQuery("name:amino",
        "SELECT c FROM CodeJpa c", new PfsParameterJpa(), authToken);
    assertEquals(95, sml.size());
    assertEquals(95, sml.getTotalCount());

    /** Find codes in lucene query */
    Logger.getLogger(getClass()).info(
        "TEST12 - " + "name:amino, SNOMEDCT_US, 2016_03_01, " + authToken);
    sml = contentService.findCodesForGeneralQuery("name:amino", "",
        new PfsParameterJpa(), authToken);
    assertEquals(95, sml.size());
    assertEquals(95, sml.getTotalCount());
  }

  /**
   * Test finding relationships for a concept.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindConceptRelationships() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    /** Find relationships for given concept */
    Logger.getLogger(getClass())
        .info("TEST1 - " + "C0000737, UMLS, latest, " + authToken);
    PfsParameterJpa pfs = new PfsParameterJpa();
    RelationshipList l = contentService.findConceptRelationships("C0000737",
        umlsTerminology, umlsVersion, "", pfs, authToken);
    assertEquals(22, l.size());

    /** Find relationships for given concept with pfs */
    Logger.getLogger(getClass())
        .info("TEST2 - " + "C0000737, UMLS, latest, " + authToken);
    pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(3);
    l = contentService.findConceptRelationships("C0000737", umlsTerminology,
        umlsVersion, "", pfs, authToken);
    assertEquals(3, l.size());
    assertEquals(22, l.getTotalCount());

  }

  /**
   * Test finding relationships for a descriptor.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindDescriptorRelationships() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    /** Find relationships for given descriptor */
    Logger.getLogger(getClass())
        .info("TEST1 - " + "D000015, MSH, mshVersion, " + authToken);
    PfsParameterJpa pfs = new PfsParameterJpa();
    RelationshipList l = contentService.findDescriptorRelationships("D000015",
        mshTerminology, mshVersion, "", pfs, authToken);
    assertEquals(50, l.size());

    /** Find relationships for given descriptor with pfs */
    Logger.getLogger(getClass())
        .info("TEST2 - " + "D000015, MSH, mshVersion, " + authToken);
    pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(3);
    l = contentService.findDescriptorRelationships("D000015", mshTerminology,
        mshVersion, "", pfs, authToken);
    assertEquals(3, l.size());
    assertEquals(50, l.getTotalCount());
  }

  /**
   * Test finding relationships for a code.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindCodeRelationships() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    /** Find relationships for given code */
    Logger.getLogger(getClass())
        .info("TEST1 - " + "U000019, MSH, mshVersion, " + authToken);
    PfsParameterJpa pfs = new PfsParameterJpa();
    RelationshipList l = contentService.findCodeRelationships("U000019",
        mshTerminology, mshVersion, "", pfs, authToken);
    assertEquals(15, l.size());

    /** Find relationships for given code with pfs */
    Logger.getLogger(getClass())
        .info("TEST2 - " + "U000019, MSH, mshVersion, " + authToken);
    pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(3);
    l = contentService.findCodeRelationships("U000019", mshTerminology,
        mshVersion, "", pfs, authToken);
    assertEquals(3, l.size());
    assertEquals(15, l.getTotalCount());
  }

  /**
   * Test find concept trees for query.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindConceptTree() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // tree lookup, empty pfs
    Logger.getLogger(getClass()).info("  Simple query, empty pfs");
    Tree tree = contentService.findConceptTree(snomedTerminology, snomedVersion,
        "vitamin", new PfsParameterJpa(), authToken);

    Logger.getLogger(getClass()).info("    Result: " + tree);
    // All the leaf TreePosition<AtomClass> tree should contain "vitamin"
    for (Tree leaf : tree.getLeafNodes()) {
      assertTrue(leaf.getNodeName().toLowerCase().contains("vitamin"));
    }

    PfsParameterJpa pfs = new PfsParameterJpa();
    // tree lookup, limit to 3
    pfs.setStartIndex(0);
    pfs.setMaxResults(3);
    Logger.getLogger(getClass()).info("  Simple query, limit to 3");
    tree = contentService.findConceptTree(snomedTerminology, snomedVersion,
        "vitamin", pfs, authToken);
    Logger.getLogger(getClass())
        .info("    total leaf count = " + tree.getLeafNodes().size());
    assertEquals(3, tree.getLeafNodes().size());
    Logger.getLogger(getClass()).info("    Result: " + tree);
    // All the leaf TreePosition<AtomClass> tree should contain "vitamin"
    for (Tree leaf : tree.getLeafNodes()) {
      assertTrue(leaf.getNodeName().toLowerCase().contains("vitamin"));
    }

    // wider lookup, limit to 10
    pfs.setStartIndex(0);
    pfs.setMaxResults(10);
    Logger.getLogger(getClass()).info("  Simple query, limit to 3");
    tree = contentService.findConceptTree(snomedTerminology, snomedVersion,
        "a*", pfs, authToken);
    Logger.getLogger(getClass())
        .info("    total leaf count = " + tree.getLeafNodes().size());
    assertEquals(9, tree.getLeafNodes().size());
    Logger.getLogger(getClass()).info("    Result: " + tree);
    // All the leaf TreePosition<AtomClass> tree should contain "vitamin"
    for (Tree leaf : tree.getLeafNodes()) {
      assertTrue(leaf.getNodeName().toLowerCase().contains("a"));
    }

  }

  /**
   * Test find descriptor trees for query.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindDescriptorTree() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // tree lookup, empty pfs
    Logger.getLogger(getClass()).info("  Simple query, empty pfs");
    Tree tree = contentService.findDescriptorTree(mshTerminology, mshVersion,
        "pneumonia", new PfsParameterJpa(), authToken);
    Logger.getLogger(getClass())
        .info("    total leaf count = " + tree.getLeafNodes().size());
    assertEquals(3, tree.getLeafNodes().size());
    Logger.getLogger(getClass()).info("    Result: " + tree);
    // All the leaf TreePosition<AtomClass> tree should contain "vitamin"
    for (Tree leaf : tree.getLeafNodes()) {
      assertTrue(leaf.getNodeName().toLowerCase().contains("pneumonia"));
    }

    PfsParameterJpa pfs = new PfsParameterJpa();
    // tree lookup, limit to 3
    pfs.setStartIndex(0);
    pfs.setMaxResults(3);
    Logger.getLogger(getClass()).info("  Simple query, limit to 3");
    tree = contentService.findDescriptorTree(mshTerminology, mshVersion,
        "pneumonia", pfs, authToken);
    Logger.getLogger(getClass())
        .info("    total leaf count = " + tree.getLeafNodes().size());
    assertEquals(2, tree.getLeafNodes().size());
    Logger.getLogger(getClass()).info("    Result: " + tree);
    // All the leaf TreePosition<AtomClass> tree should contain "vitamin"
    for (Tree leaf : tree.getLeafNodes()) {
      assertTrue(leaf.getNodeName().toLowerCase().contains("pneumonia"));
    }

  }

  /**
   * Test find code trees for query.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindCodeTree() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // n/a - no sample data
  }

  /**
   * Test get mapset.
   * @throws Exception
   */
  @Test
  public void testGetMapSet() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    Logger.getLogger(getClass())
        .info("TEST - " + "447562003, SNOMEDCT_US, 2016_03_01, " + authToken);
    MapSet c = contentService.getMapSet("447562003", "SNOMEDCT_US",
        "2016_03_01", authToken);
    // Validate the concept returned
    assertNotNull(c);
    assertEquals(c.getName(), "ICD-10 complex map reference set");
    assertTrue(c.isPublishable());
    assertTrue(c.isPublished());
    assertFalse(c.isObsolete());
    assertFalse(c.isSuppressible());
    assertEquals(1, c.getAttributes().size());
    assertEquals("SNOMEDCT_US", c.getTerminology());
    assertEquals("2016_03_01", c.getVersion());
    assertEquals("447562003", c.getTerminologyId());
    assertEquals("loader", c.getLastModifiedBy());
  }

  /**
   * Test get mapsets.
   * @throws Exception
   */
  @Test
  public void testGetMapSets() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    Logger.getLogger(getClass())
        .info("TEST - " + "SNOMEDCT_US, 2016_03_01, " + authToken);
    MapSetList c =
        contentService.getMapSets("SNOMEDCT_US", "2016_03_01", authToken);
    // Validate the concept returned
    assertNotNull(c);
    assertEquals(1, c.getObjects().size());
  }

  /**
   * Test find mappings for mapset
   * @throws Exception
   */
  @Test
  public void testFindMappings() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    Logger.getLogger(getClass())
        .info("TEST - " + "SNOMEDCT_US, 2016_03_01, " + authToken);
    MappingList c = contentService.findMappings("447562003", "SNOMEDCT_US",
        "2016_03_01", "", new PfsParameterJpa(), authToken);

    // Validate the concept returned
    assertNotNull(c);
    assertEquals(c.getObjects().size(), 334);
  }

  /**
   * Test find mappings for concept
   * @throws Exception
   */
  @Test
  public void testFindConceptMappings() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    Logger.getLogger(getClass())
        .info("TEST - " + "C0155860, UMLS, latest" + authToken);
    MappingList c = contentService.findConceptMappings("C0155860", "MTH",
        "latest", "", new PfsParameterJpa(), authToken);

    // Validate the concept returned
    assertNotNull(c);
    assertEquals(1, c.getObjects().size());
  }

  /**
   * Test validation of a concept.
   *
   * @throws Exception the exception
   */
  @Test
  public void testValidateConcept() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    ProjectServiceRest projectService = new ProjectClientRest(properties);
    Project p = projectService.findProjects(null, null, authToken).getObjects().get(0);

    ConceptJpa concept = (ConceptJpa) contentService.getConcept("M0028634",
        mshTerminology, mshVersion, p.getId(), authToken);

    ValidationResult result =
        contentService.validateConcept(p.getId(), concept, null, adminToken);

    assertTrue(result.getErrors().size() == 0);
    assertTrue(result.getWarnings().size() == 0);
  }

  /**
   * Test validation of a descriptor.
   *
   * @throws Exception the exception
   */
  @Test
  public void testValidateDescriptor() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    ProjectServiceRest projectService = new ProjectClientRest(properties);
    Project p = projectService.findProjects(null, null, authToken).getObjects().get(0);

    DescriptorJpa c = (DescriptorJpa) contentService.getDescriptor("C013093",
        mshTerminology, mshVersion, p.getId(), authToken);

    ValidationResult result =
        contentService.validateDescriptor(p.getId(), c, adminToken);

    assertTrue(result.getErrors().size() == 0);
    assertTrue(result.getWarnings().size() == 0);
  }

  /**
   * Test validation of a code.
   *
   * @throws Exception the exception
   */
  @Test
  public void testValidateCode() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    ProjectServiceRest projectService = new ProjectClientRest(properties);
    Project p = projectService.findProjects(null, null, authToken).getObjects().get(0);

    CodeJpa c = (CodeJpa) contentService.getCode("C013093", mshTerminology,
        mshVersion, p.getId(), authToken);

    ValidationResult result =
        contentService.validateCode(p.getId(), c, adminToken);

    assertTrue(result.getErrors().size() == 0);
    assertTrue(result.getWarnings().size() == 0);
  }

  /**
   * Test validation of an atom.
   *
   * @throws Exception the exception
   */
  @Test
  public void testValidateAtom() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    ProjectServiceRest projectService = new ProjectClientRest(properties);
    Project p = projectService.findProjects(null, null, authToken).getObjects().get(0);

    ConceptJpa concept = (ConceptJpa) contentService.getConcept("M0028634",
        mshTerminology, mshVersion, p.getId(), authToken);

    ValidationResult result = contentService.validateAtom(p.getId(),
        (AtomJpa) concept.getAtoms().get(0), adminToken);

    assertTrue(result.getErrors().size() == 0);
    assertTrue(result.getWarnings().size() == 0);

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
