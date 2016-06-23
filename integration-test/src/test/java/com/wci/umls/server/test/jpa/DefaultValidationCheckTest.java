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

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.services.validation.DefaultValidationCheck;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.test.helpers.IntegrationUnitSupport;

/**
 * Testing for {@link DefaultValidationCheckTest}.
 */
public class DefaultValidationCheckTest extends IntegrationUnitSupport {

  /** The handler service. */
  private DefaultValidationCheck defaultValidationCheck;

  /**
   * Setup class.
   */
  @BeforeClass
  public static void setupClass() {
    // do nothing
  }

  /**
   * Setup.
   */
  @Before
  public void setup() {

    try {
      defaultValidationCheck = new DefaultValidationCheck();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * /** Test normal use of the validation check.
   *
   * @throws Exception the exception
   */
  @Test
  public void testHandlerNormalUse() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    Atom atom = new AtomJpa();
    atom.setTerminologyId("12345");
    atom.setName("disallowed\twhitespace");
    ValidationResult result = defaultValidationCheck.validate(atom);
    Logger.getLogger(getClass()).info(result);
    assertEquals(result.getErrors().iterator().next(),
        "Atom name contains invalid whitespace.");

    atom.setName(" leadingwhitespace");
    result = defaultValidationCheck.validate(atom);
    Logger.getLogger(getClass()).info(result);
    assertEquals(result.getErrors().iterator().next(),
        "Atom name contains leading whitespace.");

    atom.setName("trailing whitespace ");
    result = defaultValidationCheck.validate(atom);
    Logger.getLogger(getClass()).info(result);
    assertEquals(result.getErrors().iterator().next(),
        "Atom name contains trailing whitespace.");

    atom.setName("duplicate    whitespace");
    result = defaultValidationCheck.validate(atom);
    Logger.getLogger(getClass()).info(result);
    assertEquals(result.getErrors().iterator().next(),
        "Atom name contains duplicate whitespace.");

    atom.setName("duplicate    whitespace and trailing whitespace ");
    result = defaultValidationCheck.validate(atom);
    Logger.getLogger(getClass()).info(result);
    assertEquals(result.getErrors().size(), 2);
  }

  /*
   * Test degenerate use of the validation check.
   * 
   * @throws Exception the exception
   */
  /**
   * Test handler degenerate use007.
   *
   * @throws Exception the exception
   */
  @Test
  public void testHandlerDegenerateUse() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    Atom atom = null;
    ValidationResult result = defaultValidationCheck.validate(atom);
    Logger.getLogger(getClass()).info(result);

    assertEquals(result, null);
  }

  /**
   * Test edge cases of the validation check.
   *
   * @throws Exception the exception
   */
  @Test
  public void testHandlerEdgeCases() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    Atom atom = new AtomJpa();
    ValidationResult result = defaultValidationCheck.validate(atom);
    Logger.getLogger(getClass()).info(result);
    assertEquals(result.getErrors().size(), 1);
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
