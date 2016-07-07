/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.jpa;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.services.ContentService;
import com.wci.umls.server.test.helpers.IntegrationUnitSupport;

/**
 * Sample test to get auto complete working
 */
public class SemanticCategorySearchTest extends IntegrationUnitSupport {

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
   * Test searches.
   *
   * @throws Exception the exception
   */
  @Test
  public void testSearches() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    ContentService service = new ContentServiceJpa();
    SearchResultList list =
        service.findConcepts("SNOMEDCT_US", "2016_03_01", Branch.ROOT,
            "atoms.nameSort:\"[A-Z].* (disorder)\"", new PfsParameterJpa());
    Logger.getLogger(getClass()).info(" list = " + list);

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
