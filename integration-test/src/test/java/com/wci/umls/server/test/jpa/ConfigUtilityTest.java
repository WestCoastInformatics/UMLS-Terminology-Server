/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.jpa;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import org.apache.log4j.Logger;
import org.codehaus.plexus.util.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.ProcessExecution;
import com.wci.umls.server.Project;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.ProjectList;
import com.wci.umls.server.jpa.ProcessExecutionJpa;
import com.wci.umls.server.jpa.algo.insert.ContextLoaderAlgorithm;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.jpa.services.ProcessServiceJpa;
import com.wci.umls.server.services.ContentService;
import com.wci.umls.server.services.ProcessService;
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
    }, new String[] {}, false, new File("."), null);
    Logger.getLogger(getClass()).info("    out = " + out);

    Logger.getLogger(getClass()).info("  exec echo $ABC with ABC=DEF");
     out = ConfigUtility.exec(new String[] {
        "echo $ABC"
    }, new String[] {
        "ABC=DEF"
    }, false, new File("."), null);
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
