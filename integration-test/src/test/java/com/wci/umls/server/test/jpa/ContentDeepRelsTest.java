/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.jpa;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.ComponentInfo;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.content.RelationshipList;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.services.ContentService;
import com.wci.umls.server.test.helpers.IntegrationUnitSupport;

/**
 * Sample test to get auto complete working
 */
public class ContentDeepRelsTest extends IntegrationUnitSupport {

  /** The service. */
  ContentService service = null;

  /**
   * Setup class.
   */
  @BeforeClass
  public static void setupClass() {
    // do nothing
  }

  /**
   * Setup.
   * @throws Exception
   */
  @Before
  public void setup() throws Exception {
    service = new ContentServiceJpa();
  }

  /**
   * Normal use of concept autocomplete.
   *
   * @throws Exception the exception
   */
  @Test
  public void testfindDeepRels() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    // Basic call
    // inverseFlag, includeConceptRels, preferredOnly, includeSelfReferential
    Logger.getLogger(getClass())
        .info("  Test basic findDeepRelationshipsForConcept call");
    RelationshipList list = service.findConceptDeepRelationships("C0000097",
        "MTH", "latest", Branch.ROOT, null, false, false, false, false,
        new PfsParameterJpa());
    for (final Relationship<? extends ComponentInfo, ? extends ComponentInfo> rel : list
        .getObjects()) {
      Logger.getLogger(getClass())
          .info("  " + rel.getFrom().getTerminologyId() + ", "
              + rel.getTerminology() + ", " + rel.getVersion() + ", "
              + rel.getRelationshipType() + ", "
              + rel.getAdditionalRelationshipType() + ", "
              + rel.getTo().getTerminologyId());

    }
    Logger.getLogger(getClass()).info("  Verify count = " + list.size());
    assertEquals(107, list.size());

    // include concept rels
    // inverseFlag, includeConceptRels, preferredOnly, includeSelfReferential
    Logger.getLogger(getClass())
        .info("  Test findDeepRelationshipsForConcept call - concept");
    list = service.findConceptDeepRelationships("C0000097", "MTH", "latest",
        Branch.ROOT, null, false, true, false, false, new PfsParameterJpa());
    for (final Relationship<? extends ComponentInfo, ? extends ComponentInfo> rel : list
        .getObjects()) {
      Logger.getLogger(getClass())
          .info("  " + rel.getFrom().getTerminologyId() + ", "
              + rel.getTerminology() + ", " + rel.getVersion() + ", "
              + rel.getRelationshipType() + ", "
              + rel.getAdditionalRelationshipType() + ", "
              + rel.getTo().getTerminologyId());

    }
    Logger.getLogger(getClass()).info("  Verify count = " + list.size());
    assertEquals(112, list.size());

    // include self-referential
    // inverseFlag, includeConceptRels, preferredOnly, includeSelfReferential
    Logger.getLogger(getClass())
        .info("  Test findDeepRelationshipsForConcept call - selfref");
    list = service.findConceptDeepRelationships("C0000097", "MTH", "latest",
        Branch.ROOT, null, false, false, false, true, new PfsParameterJpa());
    for (final Relationship<? extends ComponentInfo, ? extends ComponentInfo> rel : list
        .getObjects()) {
      Logger.getLogger(getClass())
          .info("  " + rel.getFrom().getTerminologyId() + ", "
              + rel.getTerminology() + ", " + rel.getVersion() + ", "
              + rel.getRelationshipType() + ", "
              + rel.getAdditionalRelationshipType() + ", "
              + rel.getTo().getTerminologyId());

    }
    Logger.getLogger(getClass()).info("  Verify count = " + list.size());
    assertEquals(123, list.size());

    // include pref only
    Logger.getLogger(getClass())
        .info("  Test findDeepRelationshipsForConcept call - prefonly");
    list = service.findConceptDeepRelationships("C0000097", "MTH", "latest",
        Branch.ROOT, null, false, true, true, false, new PfsParameterJpa());
    for (final Relationship<? extends ComponentInfo, ? extends ComponentInfo> rel : list
        .getObjects()) {
      Logger.getLogger(getClass())
          .info("  " + rel.getFrom().getTerminologyId() + ", "
              + rel.getTerminology() + ", " + rel.getVersion() + ", "
              + rel.getRelationshipType() + ", "
              + rel.getAdditionalRelationshipType() + ", "
              + rel.getTo().getTerminologyId());

    }
    Logger.getLogger(getClass()).info("  Verify count = " + list.size());
    assertEquals(64, list.size());

    Logger.getLogger(getClass()).info(
        "  Test basic findDeepRelationshipsForConcept call with pfs page size and sort order");
    PfsParameter pfs = new PfsParameterJpa();
    pfs.setMaxResults(10);
    pfs.setStartIndex(0);
    pfs.setSortField("relationshipType");
    list = service.findConceptDeepRelationships("C0000097", "MTH", "latest",
        Branch.ROOT, null, false, false, false, false, pfs);
    for (final Relationship<? extends ComponentInfo, ? extends ComponentInfo> rel : list
        .getObjects()) {
      Logger.getLogger(getClass())
          .info("  " + rel.getFrom().getTerminologyId() + ", "
              + rel.getTerminology() + ", " + rel.getVersion() + ", "
              + rel.getRelationshipType() + ", "
              + rel.getAdditionalRelationshipType() + ", "
              + rel.getTo().getTerminologyId());

    }
    // Test 10 objects
    assertEquals(10, list.size());
    List<Relationship<? extends ComponentInfo, ? extends ComponentInfo>> list2 =
        new ArrayList<>(list.getObjects());

    // Test sort ordering by relationship type
    Collections.sort(list2,
        new Comparator<Relationship<? extends ComponentInfo, ? extends ComponentInfo>>() {
          @Override
          public int compare(
            Relationship<? extends ComponentInfo, ? extends ComponentInfo> o1,
            Relationship<? extends ComponentInfo, ? extends ComponentInfo> o2) {
            return o1.getRelationshipType().compareTo(o2.getRelationshipType());
          }
        });
    assertEquals(list.getObjects(), list2);
  }

  /**
   * Teardown.
   */
  @After
  public void teardown() {
    // do nothing
  }

  /**
   * Teardown class.
   */
  @AfterClass
  public static void teardownClass() {
    // do nothing
  }

}
