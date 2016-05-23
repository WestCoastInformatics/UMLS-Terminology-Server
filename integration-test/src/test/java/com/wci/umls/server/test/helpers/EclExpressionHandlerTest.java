/*
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.helpers;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.PfscParameter;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.jpa.helpers.PfscParameterJpa;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.jpa.services.handlers.EclExpressionHandler;
import com.wci.umls.server.services.ContentService;

/**
 * Helper testing class for PfsParameter concept tests.
 */
public class EclExpressionHandlerTest {

  /** The handler. */
  private static EclExpressionHandler handler = null;

  /**
   * Setup.
   *
   * @throws Exception the exception
   */
  // TODO Remove this after development work and create formal integration
  // testing
  @BeforeClass
  public static void setup() throws Exception {
    System.setProperty("run.config.umls",
        "D:/umlsserver/config/config.snomed.properties");
    handler = new EclExpressionHandler("SNOMEDCT", "latest");
  }

  /**
   * Test find concepts for query.
   *
   * @throws Exception the exception
   */
  @Test
  public void testParsing() throws Exception {
    // Self 404684003 |clinical finding| Yes
    try {
      Logger.getLogger(getClass()).info(
          handler.parse("404684003 |clinical finding|"));
    } catch (Exception e) {
      Logger.getLogger(getClass()).info(e.getMessage());
    }
    // Descendant Of < 404684003 |clinical finding| Yes
    try {
      Logger.getLogger(getClass()).info(
          handler.parse("< 404684003 |clinical finding|"));
    } catch (Exception e) {
      Logger.getLogger(getClass()).info(e.getMessage());
    }
    // Descendant Or Self Of << 73211009 |diabetes mellitus| Yes
    try {
      Logger.getLogger(getClass()).info(
          handler.parse("<< 73211009 |diabetes mellitus|"));
    } catch (Exception e) {
      Logger.getLogger(getClass()).info(e.getMessage());
    }

    // Nested Attribute < 404684003 |clinical finding|: 47429007 |associated
    // with| = (< 404684003 |clinical finding|: 116676008 |associated
    // morphology| = << 55641003 |infarct|) No
    try {
      Logger
          .getLogger(getClass())
          .info(
              handler
                  .parse("< 19829001 |disorder of lung|: 116676008 |associated morphology| = 79654002 |edema|"));
    } catch (Exception e) {
      Logger.getLogger(getClass()).info(e.getMessage());
    }
  }

  /**
   * Test count.
   *
   * @throws Exception the exception
   */
  @Test
  public void testCount() throws Exception {
    // n/a
  }

  /**
   * Test resolve any.
   *
   * @throws Exception the exception
   */
  @Test
  public void testResolveAny() throws Exception {
    // test any (wildcard)
    testEclQuery("*", 10293);
  }

  /**
   * Test resolve self.
   *
   * @throws Exception the exception
   */
  @Test
  public void testResolveSelf() throws Exception {

    // test self retrieval (with name)
    SearchResultList results = testEclQuery("404684003  |clinical finding|", 1);
    assertTrue(results.getObjects().get(0).getTerminologyId()
        .equals("404684003"));

    // test self retrieval (without name)
    results = testEclQuery("404684003", 1);
    assertTrue(results.getObjects().get(0).getTerminologyId()
        .equals("404684003"));

  }

  /**
   * Test resolve descendant.
   *
   * @throws Exception the exception
   */
  @Test
  public void testResolveDescendant() throws Exception {

    // test descendant (not self) retrieval (with name)
    testEclQuery("< 91723000 |anatomical structure|", 1512);

    // test descendant (not self) retrieval (without name)
    testEclQuery("< 91723000", 1512);

    // test descendant (with self) retrieval (with name)
    testEclQuery("<< 91723000 |anatomical structure|", 1513);

    // test descendant (with self) retrieval (without name)
    testEclQuery("<< 91723000", 1513);
  }

  /**
   * Test resolve ancestor.
   *
   * @throws Exception the exception
   */
  @Test
  public void testResolveAncestor() throws Exception {

    // test ancestor (not self) retrieval (with name)
    testEclQuery("> 91723000 |anatomical structure|", 3);

    // test ancestor (not self) retrieval (without name)
    testEclQuery("> 91723000", 3);

    // test ancestor (with self) retrieval (with name)
    testEclQuery(">> 91723000 |anatomical structure|", 4);

    // test ancestor (with self) retrieval (without name)
    testEclQuery(">> 91723000", 4);

  }

  /**
   * Test resolve attribute.
   *
   * @throws Exception the exception
   */
  @Test
  public void testResolveAttributeAny() throws Exception {

    // test attribute of 127294003 Traumatic AND/OR non-traumatic brain injury
    testEclQuery("* : 363698007 |finding site| = 12738006 |brain structure|",
        47);

    // test without names
    testEclQuery("* : 363698007 = 12738006", 47);
  }

  /**
   * Test resolve attribute focus concept.
   *
   * @throws Exception the exception
   */
  @Test
  public void testResolveFocusConceptAttribute() throws Exception {

    // test concept range with single attribute
    testEclQuery(
        "127294003 |Traumatic AND/OR non-traumatic brain injury| : 363698007 |finding site| = 12738006 |brain structure|",
        1);

    // test without names
    testEclQuery("127294003 : 363698007 = 12738006", 1);
  }

  /**
   * Test resolve focus c oncept range attribute.
   *
   * @throws Exception the exception
   */
  @Test
  public void testResolveFocusConceptRangeAttribute() throws Exception {

    // test focus concept range with single attribute (with names)
    testEclQuery(
        "< 404684003 |clinical finding|: 363698007 |finding site| =  39057004 |pulmonary valve structure|",
        5);

    // test without names
    testEclQuery("< 404684003 : 363698007 = 39057004", 5);
  }

  /**
   * Test resolve focus concept attribute range.
   *
   * @throws Exception the exception
   */

  @Test
  public void testResolveFocusConceptRangeAttributeRange() throws Exception {
   
    testEclQuery(
        "< 404684003 |clinical finding|: 363698007 |finding site| = << 39057004 |pulmonary valve structure|",
        5);

    // test without names
    testEclQuery("< 404684003: 363698007 = << 39057004", 5);
  }

  /**
   * Test search with ecl.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  @Test
  public void testSearchWithEcl() throws Exception {
    ContentService contentService = new ContentServiceJpa();
    PfscParameter pfsc = new PfscParameterJpa();
    pfsc.setExpression("< 91723000");
    SearchResultList results =
        contentService.findConceptsForQuery("SNOMEDCT", "latest", Branch.ROOT,
            null, pfsc);
    assertTrue(results.getTotalCount() == 1512);
    results =
        contentService.findConceptsForQuery("SNOMEDCT", "latest", Branch.ROOT,
            "joint", pfsc);
    assertTrue(results.getTotalCount() == 56);

  }

  /**
   * Helper function to execute query and compare to expected count.
   *
   * @param eclQuery the ecl query
   * @param expectedCount the expected count
   * @return the search result list
   * @throws Exception the exception
   */
  private SearchResultList testEclQuery(String eclQuery, int expectedCount)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "Expecting " + expectedCount + " results:" + eclQuery);
    try {
      SearchResultList results = handler.resolve(eclQuery);
      if (expectedCount != results.getCount()) {
        fail("Expected/actual count: " + expectedCount + "/"
            + results.getCount() + " for query: " + eclQuery);
      }
      return results;
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
    return null;
  }

}
