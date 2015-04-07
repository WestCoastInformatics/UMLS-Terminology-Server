package org.ihtsdo.otf.ts.helpers;

import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.helpers.CancelException;

/**
 * Unit testing for {@link CancelException}.
 */
public class HelperUnit001Test {

  /** The helper object to test. */
  private CancelException object;

  /** The helper object to test. */
  private CancelException object2;

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
    object = new CancelException("test");
    Exception e = new Exception("inner");
    object2 = new CancelException("test2", e);
  }

  /**
   * Test normal use of the helper object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testHelperNormalUse001() throws Exception {
    Logger.getLogger(getClass()).info("TEST testHelperNormalUse001");
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
