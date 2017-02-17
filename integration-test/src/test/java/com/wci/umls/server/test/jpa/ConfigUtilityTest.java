/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.jpa;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.test.helpers.IntegrationUnitSupport;

/**
 * Sample test to get auto complete working.
 */
public class ConfigUtilityTest extends IntegrationUnitSupport {

  /**
   * Setup class.
   */
  @BeforeClass
  public static void setupClass() {
    // do nothing
  }

  /**
   * Setup.
   *
   * @throws Exception the exception
   */
  @Before
  public void setup() throws Exception {
    // n/a
  }

  /**
   * Test exec.
   *
   * @throws Exception the exception
   */
  @Test
  public void testExec() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    Logger.getLogger(getClass()).info("  exec ls");
    String out = ConfigUtility.exec(new String[] {
        "ls"
    }, new String[] {}, false, ".", null);
    Logger.getLogger(getClass()).info("    out = " + out);

    Logger.getLogger(getClass()).info("  exec echo $ABC with ABC=DEF");
     out = ConfigUtility.exec(new String[] {
        "echo $ABC"
    }, new String[] {
        "ABC=DEF"
    }, false, ".", null);
    Logger.getLogger(getClass()).info("    out = " + out);

  }

  /**
   * Teardown class.
   */
  @AfterClass
  public static void teardownClass() {
    // do nothing
  }

}
