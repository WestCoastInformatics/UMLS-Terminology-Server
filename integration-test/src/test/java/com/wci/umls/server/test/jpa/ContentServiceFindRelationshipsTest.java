/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.jpa;

import static org.junit.Assert.assertEquals;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.content.RelationshipList;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.services.ContentService;
import com.wci.umls.server.test.helpers.IntegrationUnitSupport;

/**
 * Sample test to get auto complete working
 */
public class ContentServiceFindRelationshipsTest extends IntegrationUnitSupport {

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
  public void testfindConceptRelationships() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    // 272252 $ 2016-01-01 00:00:00 loader 0 1 1 0 UMLS 2016-01-01 00:00:00
    // latest 0 0 1 RO 1 PUBLISHED 113624 2801

    RelationshipList list =
        service.findConceptRelationships("C0364349", "MTH", "latest",
            Branch.ROOT, null, false, null);
    assertEquals(1, list.size());

    list =
        service.findConceptRelationships("C0364349", "MTH", "latest",
            Branch.ROOT, "relationshipType:RO", false, null);
    assertEquals(1, list.size());
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
