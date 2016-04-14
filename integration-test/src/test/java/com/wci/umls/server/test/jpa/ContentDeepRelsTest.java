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
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.content.RelationshipList;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.model.content.ComponentHasAttributes;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.services.ContentService;

/**
 * Sample test to get auto complete working
 */
public class ContentDeepRelsTest {

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
    Logger.getLogger(getClass()).info("Start test");

    Logger.getLogger(getClass()).info(
        "  Test basic findDeepRelationshipsForConcept call");
    RelationshipList list =
        service.findDeepRelationshipsForConcept("C0000097", "UMLS", "latest",
            Branch.ROOT, null, false, new PfsParameterJpa());
    for (Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> rel : list
        .getObjects()) {
      Logger.getLogger(getClass()).info(
          "  " + rel.getFrom().getTerminologyId() + ", " + rel.getTerminology()
              + ", " + rel.getVersion() + ", " + rel.getRelationshipType()
              + ", " + rel.getAdditionalRelationshipType() + ", "
              + rel.getTo().getTerminologyId());

    }
    Logger.getLogger(getClass()).info("  Verify count = 68");
    assertEquals(68, list.getCount());

    Logger
        .getLogger(getClass())
        .info(
            "  Test basic findDeepRelationshipsForConcept call with pfs page size and sort order");
    PfsParameter pfs = new PfsParameterJpa();
    pfs.setMaxResults(10);
    pfs.setStartIndex(0);
    pfs.setSortField("relationshipType");
    list =
        service.findDeepRelationshipsForConcept("C0000097", "UMLS", "latest",
            Branch.ROOT, null, false, pfs);
    for (Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> rel : list
        .getObjects()) {
      Logger.getLogger(getClass()).info(
          "  " + rel.getFrom().getTerminologyId() + ", " + rel.getTerminology()
              + ", " + rel.getVersion() + ", " + rel.getRelationshipType()
              + ", " + rel.getAdditionalRelationshipType() + ", "
              + rel.getTo().getTerminologyId());

    }
    // Test 10 objects
    assertEquals(10, list.getCount());
    List<Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes>> list2 =
        new ArrayList<>(list.getObjects());

    // Test sort ordering by relationship type
    Collections
        .sort(
            list2,
            new Comparator<Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes>>() {
              @Override
              public int compare(
                Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> o1,
                Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> o2) {
                return o1.getRelationshipType().compareTo(
                    o2.getRelationshipType());
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
