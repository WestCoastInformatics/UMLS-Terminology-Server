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

import com.wci.umls.server.helpers.LocalException;

/**
 * Unit testing for {@link LocalException}.
 */
public class LocalExceptionUnitTest {

  /** The helper object to test. */
  private LocalException object;

  /** The helper object to test. */
  private LocalException object2;

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
    object = new LocalException("test");
    Exception e = new Exception("inner");
    object2 = new LocalException("test2", e);
  }

  /**
   * Test normal use of the helper object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testHelperNormalUse006() throws Exception {
    Logger.getLogger(getClass()).info("TEST testHelperNormalUse006");
    assertTrue(object.getMessage().equals("test"));
    assertTrue(object2.getMessage().equals("test2"));
    assertTrue(object2.getCause() != null);
    assertTrue(object2.getCause().getMessage().equals("inner"));
  }

  /**
   * Test degenerate use of the helper object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testHelperDegenerateUse006() throws Exception {
    // n/a
  }

  /**
   * Test edge cases of the helper object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testHelperEdgeCases006() throws Exception {
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
