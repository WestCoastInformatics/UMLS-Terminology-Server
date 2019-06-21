/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.mojo;

import java.io.File;
import java.util.Arrays;
import java.util.Properties;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.helpers.ConfigUtility;

/**
 * A mechanism to reset to the stock dev database.
 */
public class ResetDevDatabase {

  /** The properties. */
  static Properties config;

  /** The server. */
  static String server = "false";

  /**
   * Create test fixtures for class.
   *
   * @throws Exception the exception
   */
  @BeforeClass
  public static void setupClass() throws Exception {
    config = ConfigUtility.getConfigProperties();
    if (ConfigUtility.isServerActive()) {
      server = "true";
    }
  }

  /**
   * Test the sequence:
   * 
   * <pre>
   * Run the RRF-umls mojo against the sample config/src/resources/data/SAMPLE_2014AB" data.  This will create db and reindex.
   * Create a "MTH" project (name="Sample Project" description="Sample project." terminology=MTH version=latest scope.concepts=? scope.descendants.flag=true admin.user=admin)
   * Start an editing cycle for "MTH"
   * stop here and the db is ready to use
   * </pre>
   * 
   * @throws Exception the exception
   */
  
  @Test
  public void test() throws Exception {

    // Load the new RF2 full
    // Run "generate sample data" -

    // Load RF2 full
    InvocationRequest request = new DefaultInvocationRequest();
    request.setPomFile(new File("../admin/loader/pom.xml"));
    request.setProfiles(Arrays.asList("RRF-umls"));
    request.setGoals(Arrays.asList("clean", "install"));
    Properties p = new Properties();
    p.setProperty("run.config.umls", System.getProperty("run.config.umls"));
    p.setProperty("edit.mode", "true");
    p.setProperty("server", server);
    p.setProperty("mode", "create");
    p.setProperty("terminology", "MTH");
    p.setProperty("version", "latest");
    p.setProperty("input.dir",
        "../../config/src/main/resources/data/SAMPLE_UMLS");
    if (System.getProperty("input.dir") != null) {
      p.setProperty("input.dir", System.getProperty("input.dir"));
    }
    request.setProperties(p);
    request.setDebug(false);
    Invoker invoker = new DefaultInvoker();
    InvocationResult result = invoker.execute(request);
    if (result.getExitCode() != 0) {
      throw result.getExecutionException();
    }

    // Generate Sample Data
    request = new DefaultInvocationRequest();
    request.setPomFile(new File("../admin/loader/pom.xml"));
    request.setProfiles(Arrays.asList("GenerateSampleData"));
    request.setGoals(Arrays.asList("clean", "install"));
    p = new Properties();
    p.setProperty("run.config.umls", System.getProperty("run.config.umls"));
    p.setProperty("mode", "update");
    p.setProperty("terminology", "MTH");
    p.setProperty("version", "latest");
    request.setProperties(p);
    invoker = new DefaultInvoker();
    result = invoker.execute(request);
    if (result.getExitCode() != 0) {
      throw result.getExecutionException();
    }

  }

  /**
   * Create test fixtures per test.
   *
   * @throws Exception the exception
   */
  @Before
  public void setup() throws Exception {
    // n/a
  }

  /**
   * Teardown.
   *
   * @throws Exception the exception
   */
  @After
  public void teardown() throws Exception {
    // n/a
  }

  /**
   * Teardown class.
   *
   * @throws Exception the exception
   */
  @AfterClass
  public static void teardownClass() throws Exception {
    // n/a
  }

}
