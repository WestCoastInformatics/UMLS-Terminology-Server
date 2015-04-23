/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.rest;

import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;

import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.content.ConceptList;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.rest.client.MetadataClientRest;
import com.wci.umls.server.rest.client.SecurityClientRest;
import com.wci.umls.server.services.ContentService;

/**
 * Implementation of the "Metadata Service REST Normal Use" Test Cases.
 */
@Ignore
public class MetadataServiceRestTest {

  /** The service. */
  protected static MetadataClientRest metadataService;

  /** The security service. */
  protected static SecurityClientRest securityService;

  /** The properties. */
  protected static Properties properties;

  /** The test password. */
  protected static String testUser;

  /** The test password. */
  protected static String testPassword;

  /**
   * Create test fixtures for class.
   *
   * @throws Exception the exception
   */
  @BeforeClass
  public static void setupClass() throws Exception {

    // instantiate properties
    properties = ConfigUtility.getConfigProperties();

    // instantiate required services
    metadataService = new MetadataClientRest(properties);
    securityService = new SecurityClientRest(properties);

    /**
     * Test prerequisites Terminology SNOMEDCT exists in database Terminology
     * ICD9CM exists in database The run.config.ts has "viewer.user" and
     * "viewer.password" specified
     */

    // test run.config.ts has viewer user
    testUser = properties.getProperty("viewer.user");
    testPassword = properties.getProperty("viewer.password");

    if (testUser == null || testUser.isEmpty()) {
      throw new Exception("Test prerequisite: viewer.user must be specified");
    }
    if (testPassword == null || testPassword.isEmpty()) {
      throw new Exception(
          "Test prerequisite: viewer.password must be specified");
    }

    // test that some terminology objects exist for both SNOMEDCT and ICD9CM
    ContentService contentService = new ContentServiceJpa();
    PfsParameter pfs = new PfsParameterJpa();
    pfs.setMaxResults(1);
    ConceptList conceptList;

    // check UMLS (support both SAMPLE_2014AB and SCTMTH_2014AB)
    conceptList = contentService.getAllConcepts("UMLS", "latest", null);
    if (conceptList.getCount() == 0)
      throw new Exception("Could not retrieve any concepts for UMLS");
    if (conceptList.getTotalCount() != 2863 &&
        conceptList.getTotalCount() != 1863) {
      throw new Exception(
          "Metadata service requires UMLS loaded from the config project data.");
    }

    // check SNOMEDCT
    conceptList = contentService.getAllConcepts("SNOMEDCT_US", "2014_09_01", null);
    if (conceptList.getCount() == 0)
      throw new Exception("Could not retrieve any concepts for SNOMEDCT_US");
    if (conceptList.getTotalCount() != 3902) {
      throw new Exception(
          "Metadata service requires SNOMEDCT_US loaded from the config project data.");
    }

    // check MSH
    conceptList = contentService.getAllConcepts("MSH", "2015_2014_09_08", null);
    if (conceptList.getCount() == 0) {
      throw new Exception("Could not retrieve any concepts for MSH");
    }
    if (conceptList.getTotalCount() != 1028) {
      throw new Exception(
          "Metadata service requires MSH loaded from config project data.");
    }
    contentService.close();

  }

  /**
   * Create test fixtures per test.
   *
   * @throws Exception the exception
   */
  @Before
  public void setup() throws Exception {

    /**
     * Prerequisites
     */

  }

  /**
   * Teardown.
   *
   * @throws Exception the exception
   */
  @After
  public void teardown() throws Exception {
    // do nothing
  }

  /**
   * Teardown class.
   *
   * @throws Exception the exception
   */
  @AfterClass
  public static void teardownClass() throws Exception {
    // do nothing
  }

}
