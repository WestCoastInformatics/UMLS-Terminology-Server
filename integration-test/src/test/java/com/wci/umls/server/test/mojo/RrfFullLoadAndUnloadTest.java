/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.mojo;

import java.io.File;
import java.util.Arrays;
import java.util.Properties;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.Project;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.jpa.services.HistoryServiceJpa;
import com.wci.umls.server.jpa.services.ProjectServiceJpa;
import com.wci.umls.server.jpa.services.SecurityServiceJpa;
import com.wci.umls.server.services.ContentService;
import com.wci.umls.server.services.HistoryService;
import com.wci.umls.server.services.ProjectService;
import com.wci.umls.server.services.SecurityService;

/**
 * Implementation of the "RF2 Full Load and Unload Test Case".
 */
public class RrfFullLoadAndUnloadTest {

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
   * Run Updatedb mojo in "create" mode to clear the database
   *   TEST: verify there is a concepts table with no contents
   * Run Reindex mojo to clear the indexes
   *   TEST: verify there is a ConceptJpa index with no contents.
   * Run the RRF-umls mojo against the sample config/src/resources/data/SCTMTH_2014AB" data.
   *   TEST: verify each content table exists with the expected number of entries.
   * Create a "UMLS" project (name="Sample Project" description="Sample project." terminology=UMLS version=latest scope.concepts=? scope.descendants.flag=true admin.user=admin)
   *   TEST: verify there is a project with the expected name
   * Start an editing cycle for "UMLS"
   *   TEST: verify there is a release info with the expected name and "planned" flag equal to true.
   * Remove all terminologies
   *   TEST: verify there is a concepts table with no contents.
   * </pre>
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  @Test
  public void test() throws Exception {

    // Createdb
    InvocationRequest request = new DefaultInvocationRequest();
    request.setPomFile(new File("../admin/db/pom.xml"));
    request.setProfiles(Arrays.asList("Createdb"));
    request.setGoals(Arrays.asList("clean", "install"));
    Properties p = new Properties();
    p.setProperty("run.config.umls", System.getProperty("run.config.umls"));
    p.setProperty("server", server);
    request.setProperties(p);
    DefaultInvoker invoker = new DefaultInvoker();
    InvocationResult result = invoker.execute(request);
    if (result.getExitCode() != 0) {
      throw result.getExecutionException();
    }

    // Reindex
    request = new DefaultInvocationRequest();
    request.setPomFile(new File("../admin/lucene/pom.xml"));
    request.setProfiles(Arrays.asList("Reindex"));
    request.setGoals(Arrays.asList("clean", "install"));
    p = new Properties();
    p.setProperty("run.config.umls", System.getProperty("run.config.umls"));
    p.setProperty("server", server);
    request.setProperties(p);
    invoker = new DefaultInvoker();
    result = invoker.execute(request);
    if (result.getExitCode() != 0) {
      throw result.getExecutionException();
    }

    // Verify no contents
    ContentService service = new ContentServiceJpa();
    Assert.assertEquals(0, 
        service.getAllConcepts("UMLS", "latest", Branch.ROOT).getCount());
    service.close();
    service.closeFactory();
    
    // Load RF2 full
    request = new DefaultInvocationRequest();
    request.setPomFile(new File("../admin/loader/pom.xml"));
    request.setProfiles(Arrays.asList("RRF-umls"));
    request.setGoals(Arrays.asList("clean", "install"));
    p = new Properties();
    p.setProperty("run.config.umls", System.getProperty("run.config.umls"));
    p.setProperty("server", server);
    p.setProperty("terminology", "UMLS");
    p.setProperty("version", "latest");
    p.setProperty("input.dir",
        "../../config/src/main/resources/data/SCTMSH_2014AB");
    request.setProperties(p);
    invoker = new DefaultInvoker();
    result = invoker.execute(request);
    if (result.getExitCode() != 0) {
      throw result.getExecutionException();
    }

    // Verify expected contents
    service = new ContentServiceJpa();
    Assert.assertEquals(2013,
        service.getAllConcepts("UMLS", "latest", Branch.ROOT).getCount());
    // Test a non-UMLS terminology too
    Assert.assertEquals(3902,
        service.getAllConcepts("SNOMEDCT_US", "2014_09_01", Branch.ROOT).getCount());
    service.close();
    service.closeFactory();

    // Verify release info
    HistoryService historyService = new HistoryServiceJpa();
    Assert.assertNotNull(historyService.getReleaseInfo("UMLS", "latest"));
    // also, release infos should exist for other SABs.
    Assert.assertNotNull(historyService.getReleaseInfo("SNOMEDCT_US", "2014_09_01"));
    historyService.close();
    historyService.closeFactory();

    // Add a SNOMEDCT project
    request = new DefaultInvocationRequest();
    request.setPomFile(new File("../admin/loader/pom.xml"));
    request.setProfiles(Arrays.asList("Project"));
    request.setGoals(Arrays.asList("clean", "install"));
    p = new Properties();
    p.setProperty("run.config.umls", System.getProperty("run.config.umls"));
    p.setProperty("server", server);
    p.setProperty("name", "Sample project");
    p.setProperty("description", "Sample project.");
    p.setProperty("terminology", "UMLS");
    p.setProperty("version", "latest");
    // scope ignored for now
    p.setProperty("scope.descendants.flag", "true");
    p.setProperty("admin.user", "admin");
    request.setProperties(p);
    invoker = new DefaultInvoker();
    result = invoker.execute(request);
    if (result.getExitCode() != 0) {
      throw result.getExecutionException();
    }

    // Verify project exists
    ProjectService projectService = new ProjectServiceJpa();
    boolean found = false;
    for (Project project : projectService.getProjects().getObjects()) {
      if (project.getName().equals("Sample project")
          && project.getDescription().equals("Sample project.")
          && project.getScopeDescendantsFlag()
          && project.getTerminology().equals("UMLS")
          && project.getTerminologyVersion().equals("latest")) {
        // Scope ignored for now -  && project.getScopeConcepts().iterator().next().equals("138875005")) {
        found = true;
      }
    }
    Assert.assertTrue(found);
    projectService.close();
    projectService.closeFactory();

    // Verify admin user
    SecurityService securityService = new SecurityServiceJpa();
    Assert.assertNotNull(securityService.getUser("admin"));
    securityService.close();
    securityService.closeFactory();

    // Start SNOMEDCT editing cycle

    // Add a SNOMEDCT project
    request = new DefaultInvocationRequest();
    request.setPomFile(new File("../admin/release/pom.xml"));
    request.setProfiles(Arrays.asList("StartEditingCycle"));
    request.setGoals(Arrays.asList("clean", "install"));
    p = new Properties();
    p.setProperty("run.config.umls", System.getProperty("run.config.umls"));
    p.setProperty("server", server);
    p.setProperty("release.version", "2015AA");
    p.setProperty("terminology", "UMLS");
    p.setProperty("version", "latest");
    request.setProperties(p);
    invoker = new DefaultInvoker();
    result = invoker.execute(request);
    if (result.getExitCode() != 0) {
      throw result.getExecutionException();
    }

    // Verify release info for 20160131 as "planned"
    // Verify release info
    historyService = new HistoryServiceJpa();
    Assert.assertNotNull(historyService.getReleaseInfo("UMLS", "2015AA"));
    Assert.assertFalse(historyService.getReleaseInfo("UMLS", "2015AA")
        .isPublished());
    Assert.assertTrue(historyService.getReleaseInfo("UMLS", "2015AA")
        .isPlanned());
    historyService.close();
    historyService.closeFactory();

    // TODO: implement this properly (e.g. foreign key constraint issues) and redo.
    // Remove terminology
//    request = new DefaultInvocationRequest();
//    request.setPomFile(new File("../admin/remover/pom.xml"));
//    request.setProfiles(Arrays.asList("Terminology"));
//    request.setGoals(Arrays.asList("clean", "install"));
//    p = new Properties();
//    p.setProperty("run.config.umls", System.getProperty("run.config.umls"));
//    p.setProperty("server", server);
//    p.setProperty("terminology", "SNOMEDCT_US");
//    p.setProperty("version", "2014_09_01");
//    request.setProperties(p);
//    invoker = new DefaultInvoker();
//    result = invoker.execute(request);
//    if (result.getExitCode() != 0) {
//      throw result.getExecutionException();
//    }
//
//    // Verify no contents
//    service = new ContentServiceJpa();
//    Assert.assertEquals(0, service.getAllConcepts("SNOMEDCT_US", "2014_09_01", Branch.ROOT).getCount());
//    service.close();
//    service.closeFactory();
//
//
//    // Remove SNOMEDCT_US terminology
//    request = new DefaultInvocationRequest();
//    request.setPomFile(new File("../admin/remover/pom.xml"));
//    request.setProfiles(Arrays.asList("Terminology"));
//    request.setGoals(Arrays.asList("clean", "install"));
//    p = new Properties();
//    p.setProperty("run.config.umls", System.getProperty("run.config.umls"));
//    p.setProperty("server", server);
//    p.setProperty("terminology", "SNOMEDCT_US");
//    p.setProperty("version", "2014_09_01");
//    request.setProperties(p);
//    invoker = new DefaultInvoker();
//    result = invoker.execute(request);
//    if (result.getExitCode() != 0) {
//      throw result.getExecutionException();
//    }
//
//    // Verify no contents
//    service = new ContentServiceJpa();
//    Assert.assertEquals(0, service.getAllConcepts("UMLS", "latest", Branch.ROOT).getCount());
//    service.close();
//    service.closeFactory();
//    
    // Finish by clearing the DB again
    request = new DefaultInvocationRequest();
    request.setPomFile(new File("../admin/db/pom.xml"));
    request.setProfiles(Arrays.asList("Createdb"));
    request.setGoals(Arrays.asList("clean", "install"));
    p = new Properties();
    p.setProperty("run.config.umls", System.getProperty("run.config.umls"));
    p.setProperty("server", server);
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
