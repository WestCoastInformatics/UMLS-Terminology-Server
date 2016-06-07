/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.ts.helpers;

import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.helpers.ConfigUtility;

/**
 * Unit testing for {@link ConfigUtility}.
 */
public class ConfigUtilityTest {

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
    // n/a
  }

  /**
   * Test {@link ConfigUtility#normalize(String)}.
   * @throws Exception the exception
   */
  @Test
  public void testHelperNormalize() throws Exception {
    Logger.getLogger(getClass()).info("TEST ConfigUtility normalize");

    String normStr;

    // test lower case
    normStr = ConfigUtility.normalize("Heart Attack");
    assertTrue(normStr.equals("heart attack"));

    // test dashes, spaces, commas
    normStr = ConfigUtility.normalize("1,2-hydroxy");
    assertTrue(normStr.equals("1 2 hydroxy"));

    normStr = ConfigUtility.normalize("1-2 hydroxy");
    assertTrue(normStr.equals("1 2 hydroxy"));

    normStr = ConfigUtility.normalize("1,2 hydroxy");
    assertTrue(normStr.equals("1 2 hydroxy"));

    normStr = ConfigUtility.normalize("1 2 hydroxy");
    assertTrue(normStr.equals("1 2 hydroxy"));

    // test parantheses and brackets
    normStr = ConfigUtility.normalize("(1 2) hydroxy");
    assertTrue(normStr.equals("1 2 hydroxy"));
    normStr = ConfigUtility.normalize("[1] 2) hydroxy");
    assertTrue(normStr.equals("1 2 hydroxy"));

    normStr = ConfigUtility.normalize("(1 {2} hydroxy");
    assertTrue(normStr.equals("1 2 hydroxy"));

  }

  /**
   * Test degenerate use of the helper object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testHelperDegenerateUse001() throws Exception {
    // n/a
  }

  /**
   * Test edge cases of the helper object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testHelperEdgeCases001() throws Exception {
    // n/a
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
