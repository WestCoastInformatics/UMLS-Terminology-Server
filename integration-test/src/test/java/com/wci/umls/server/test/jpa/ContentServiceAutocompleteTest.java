/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.helpers.StringList;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.services.ContentService;

/**
 * Sample test to get auto complete working
 */
public class ContentServiceAutocompleteTest {

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
    StringList results =
        service.autocompleteConcepts("SNOMEDCT_US", "2014_09_01", "lett");
    assertEquals(13, results.getObjects().size());
    results = service.autocompleteConcepts("SNOMEDCT_US", "2014_09_01", "let");
    assertEquals(17, results.getObjects().size());

    results = service.autocompleteConcepts("UMLS", "latest", "lett");
    assertEquals(20, results.getObjects().size());
    results = service.autocompleteConcepts("UMLS", "latest", "let");
    assertEquals(20, results.getObjects().size());
  }

  /**
   * Normal use of descriptor autocomplete.
   *
   * @throws Exception the exception
   */
  @Test
  public void testDescriptorAutocompleteNormalUse() throws Exception {
    StringList results =
        service.autocompleteDescriptors("MSH", "2015_2014_09_08", "dipa");
    assertEquals(7, results.getObjects().size());
    results = service.autocompleteDescriptors("MSH", "2015_2014_09_08", "dip");
    assertEquals(7, results.getObjects().size());
    results = service.autocompleteDescriptors("MSH", "2015_2014_09_08", "di");
    assertTrue(results.getObjects().isEmpty());
    results = service.autocompleteDescriptors("MSH", "2015_2014_09_08", "d");
    assertTrue(results.getObjects().isEmpty());

  }

  /**
   * Normal use of code autocomplete.
   *
   * @throws Exception the exception
   */
  @Test
  public void testCodeAutocompleteNormalUse() throws Exception {
    StringList results =
        service.autocompleteCodes("SNOMEDCT_US", "2014_09_01", "lett");
    assertEquals(13, results.getObjects().size());
    results = service.autocompleteCodes("SNOMEDCT_US", "2014_09_01", "let");
    assertEquals(17, results.getObjects().size());
    results = service.autocompleteCodes("SNOMEDCT_US", "2014_09_01", "le");
    assertTrue(results.getObjects().isEmpty());
    results = service.autocompleteCodes("SNOMEDCT_US", "2014_09_01", "l");
    assertTrue(results.getObjects().isEmpty());

    results = service.autocompleteCodes("UMLS", "latest", "lett");
    assertEquals(0, results.getObjects().size());
    results = service.autocompleteCodes("UMLS", "latest", "let");
    assertEquals(0, results.getObjects().size());
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
    StringList results =
        service.autocompleteConcepts("SNOMEDCT_US", "2014_09_01", "le");
    assertTrue(results.getObjects().isEmpty());
    results = service.autocompleteConcepts("SNOMEDCT_US", "2014_09_01", "l");
    assertTrue(results.getObjects().isEmpty());

    results = service.autocompleteConcepts("UMLS", "latest", "le");
    assertTrue(results.getObjects().isEmpty());
    results = service.autocompleteConcepts("UMLS", "latest", "l");
    assertTrue(results.getObjects().isEmpty());
  }

  /**
   * Degenerate use of concept autocomplete.
   *
   * @throws Exception the exception
   */
  @Test
  public void testConceptAutocompleteDegenerateUse() throws Exception {
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
