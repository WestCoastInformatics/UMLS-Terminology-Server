/*
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.mojo;

import java.io.File;
import java.util.Arrays;
import java.util.Properties;

import org.apache.log4j.Logger;
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
import com.wci.umls.server.jpa.algo.RrfUnpublishedLoaderAlgorithm;

/**
 * A mechanism to reset to the stock dev database for NCI-META testing.
 */
public class ResetNciMetaDatabase {

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
   * Load sample data
   * GenrateNciMetaData
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
    p.setProperty("editMode", "true");
    p.setProperty("server", server);
    p.setProperty("mode", "create");
    p.setProperty("terminology", "NCIMTH");
    p.setProperty("version", "latest");
    p.setProperty("input.dir",
        "../../config/src/main/resources/data/SAMPLE_NCI");
    if (System.getProperty("input.dir") != null) {
      p.setProperty("input.dir", System.getProperty("input.dir"));
    }
    request.setProperties(p);
    Invoker invoker = new DefaultInvoker();
    InvocationResult result = invoker.execute(request);
    if (result.getExitCode() != 0) {
      throw result.getExecutionException();
    }

    // Generate Sample Data
    request = new DefaultInvocationRequest();
    request.setPomFile(new File("../admin/loader/pom.xml"));
    request.setProfiles(Arrays.asList("GenerateNciMetaData"));
    request.setGoals(Arrays.asList("clean", "install"));
    p = new Properties();
    p.setProperty("run.config.umls", System.getProperty("run.config.umls"));
    p.setProperty("terminology", "NCIMTH");
    p.setProperty("version", "latest");
    p.setProperty("mode", "update");
    request.setProperties(p);
    invoker = new DefaultInvoker();
    result = invoker.execute(request);
    if (result.getExitCode() != 0) {
      throw result.getExecutionException();
    }

    // RRF Unpublished
    Logger.getLogger(getClass()).info("Run unpublished loader algorithm");
    final RrfUnpublishedLoaderAlgorithm algo =
        new RrfUnpublishedLoaderAlgorithm();
    algo.setActivityId("LOADER");
    algo.setWorkId("LOADER");
    // ASSUMPTION: one project
    algo.setProject(algo.getProjects().getObjects().get(0));
    // ONLY one ".." here because it's running from integration-tests
    algo.setInputPath(
        "../config/src/main/resources/data/SAMPLE_NCI/unpublished");
    if (System.getProperty("input.dir") != null) {
      algo.setInputPath(System.getProperty("input.dir") + "/unpublished");
    }
    algo.setLastModifiedBy("admin");
    algo.setLastModifiedFlag(true);
    algo.setMolecularActionFlag(false);
    algo.setTerminology("NCIMTH");
    algo.setVersion("latest");
    algo.compute();
    Logger.getLogger(getClass()).info("Finished unpublished loader algorithm");

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
