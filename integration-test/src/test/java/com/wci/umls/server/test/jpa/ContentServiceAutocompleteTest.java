/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.helpers.StringList;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.services.ContentService;
import com.wci.umls.server.test.helpers.IntegrationUnitSupport;

/**
 * Sample test to get auto complete working
 */
public class ContentServiceAutocompleteTest extends IntegrationUnitSupport {

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
  public void testConceptAutocompleteNormalUse() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());
    StringList results =
        service.autocompleteConcepts("SNOMEDCT_US", "2016_03_01", "lett");
    assertTrue(results.getObjects().size()>10);
    results = service.autocompleteConcepts("SNOMEDCT_US", "2016_03_01", "let");
    assertTrue(results.getObjects().size()>10);

    results = service.autocompleteConcepts("UMLS", "latest", "lett");
    assertTrue(results.getObjects().size()>10);
    results = service.autocompleteConcepts("UMLS", "latest", "let");
    assertTrue(results.getObjects().size()>10);
  }

  /**
   * Normal use of descriptor autocomplete.
   *
   * @throws Exception the exception
   */
  @Test
  public void testDescriptorAutocompleteNormalUse() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());
    StringList results =
        service.autocompleteDescriptors("MSH", "2016_2016_02_26", "infr");
    assertTrue(results.getObjects().size()>10);
    results = service.autocompleteDescriptors("MSH", "2016_2016_02_26", "inf");
    assertTrue(results.getObjects().size()>10);
    results = service.autocompleteDescriptors("MSH", "2016_2016_02_26", "in");
    assertEquals(7, results.getObjects().size());
    results = service.autocompleteDescriptors("MSH", "2016_2016_02_26", "i");
    assertEquals(6, results.getObjects().size());

  }

  /**
   * Normal use of code autocomplete.
   *
   * @throws Exception the exception
   */
  @Test
  public void testCodeAutocompleteNormalUse() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());
    StringList results =
        service.autocompleteCodes("SNOMEDCT_US", "2016_03_01", "lett");
    assertTrue(results.getObjects().size()>10);
    results = service.autocompleteCodes("SNOMEDCT_US", "2016_03_01", "let");
    assertTrue(results.getObjects().size()>10);
    results = service.autocompleteCodes("SNOMEDCT_US", "2016_03_01", "le");
    assertTrue(results.getObjects().isEmpty());
    results = service.autocompleteCodes("SNOMEDCT_US", "2016_03_01", "l");
    assertEquals(2, results.getObjects().size());

    results = service.autocompleteCodes("UMLS", "latest", "lett");
    assertTrue(results.getObjects().isEmpty());
    results = service.autocompleteCodes("UMLS", "latest", "let");
    assertTrue(results.getObjects().isEmpty());

    results = service.autocompleteCodes("UMLS", "latest", "le");
    assertTrue(results.getObjects().isEmpty());
    results = service.autocompleteCodes("UMLS", "latest", "l");
    assertTrue(results.getObjects().isEmpty());
  }

  /**
   * Edge cases of concept autocomplete.
   *
   * @throws Exception the exception
   */
  @Test
  public void testConceptAutocompleteEdgeCases() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());
    StringList results =
        service.autocompleteConcepts("SNOMEDCT_US", "2016_03_01", "le");
    assertTrue(results.getObjects().isEmpty());
    results = service.autocompleteConcepts("SNOMEDCT_US", "2016_03_01", "l");
    assertEquals(2, results.getObjects().size());
    results = service.autocompleteConcepts("UMLS", "latest", "le");
    assertTrue(results.getObjects().size()>10);
    results = service.autocompleteConcepts("UMLS", "latest", "l");
    assertTrue(results.getObjects().size()>10);

  }

  /**
   * Degenerate use of concept autocomplete.
   *
   * @throws Exception the exception
   */
  @Test
  public void testConceptAutocompleteDegenerateUse() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());
    StringList results = service.autocompleteConcepts(null, null, null);
    assertTrue(results.getObjects().isEmpty());
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
