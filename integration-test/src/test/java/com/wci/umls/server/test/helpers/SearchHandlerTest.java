/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.helpers;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.SearchResult;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.jpa.helpers.PfscParameterJpa;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.services.ContentService;

/**
 * Helper testing class for PfsParameter concept tests.
 */
public class SearchHandlerTest {

  /**
   * Test find concepts for query.
   *
   * @throws Exception the exception
   */
  @Test
  public void testQuery() throws Exception {
    // instantiate content service
    ContentService contentService = new ContentServiceJpa();

    SearchResultList c =
        contentService.findConceptsForQuery("SNOMEDCT_US", "20140731",
            Branch.ROOT, "\"Dermoid\" tumor", new PfscParameterJpa());

    for (SearchResult sr : c.getObjects()) {
      Logger.getLogger(getClass()).info("  sr.getValue() " + sr.getValue());
    }
  }

}
