/*
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.rest;

import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.wci.umls.server.Project;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.CodeJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.DescriptorJpa;

/**
 * Implementation of the "Validation Service REST Normal Use" Test Cases.
 */
public class ValidationServiceRestNormalUseTest extends
    ValidationServiceRestTest {

  /** The auth token. */
  private static String authToken;

  /** The snomed terminology. */
  private String snomedTerminology = "SNOMEDCT_US";

  /** The snomed version. */
  private String snomedVersion = "2014_09_01";

  /** The msh terminology. */
  private String mshTerminology = "MSH";

  /** The msh version. */
  private String mshVersion = "2015_2014_09_08";

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

  }

  /**
   * Test validation of a concept.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestValidation001() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

    Project p = projectService.getProjects(authToken).getObjects().get(0);
    
    ConceptJpa c =
        (ConceptJpa) contentService.getConcept("M0028634", mshTerminology,
            mshVersion, authToken);

    ValidationResult result = validationService.validateConcept(p.getId(), c, authToken);

    assertTrue(result.getErrors().size() == 0);
    assertTrue(result.getWarnings().size() == 0);
  }

  /**
   * Test validation of an atom.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestValidation002() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");
    

    Project p = projectService.getProjects(authToken).getObjects().get(0);

    AtomJpa c =
        (AtomJpa) contentService.getAtom("412904012", snomedTerminology,
            snomedVersion, authToken);

    ValidationResult result = validationService.validateAtom(p.getId(), c, authToken);

    assertTrue(result.getErrors().size() == 0);
    assertTrue(result.getWarnings().size() == 0);
  }

  /**
   * Test validation of a descriptor.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestValidation003() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");


    Project p = projectService.getProjects(authToken).getObjects().get(0);
    
    DescriptorJpa c =
        (DescriptorJpa) contentService.getDescriptor("C013093", mshTerminology,
            mshVersion, authToken);

    ValidationResult result =
        validationService.validateDescriptor(p.getId(), c, authToken);

    assertTrue(result.getErrors().size() == 0);
    assertTrue(result.getWarnings().size() == 0);
  }

  /**
   * Test validation of a code.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestValidation004() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");
    
    Project p = projectService.getProjects(authToken).getObjects().get(0);

    CodeJpa c =
        (CodeJpa) contentService.getCode("C013093", mshTerminology, mshVersion,
            authToken);

    ValidationResult result = validationService.validateCode(p.getId(), c, authToken);

    assertTrue(result.getErrors().size() == 0);
    assertTrue(result.getWarnings().size() == 0);
  }

}
