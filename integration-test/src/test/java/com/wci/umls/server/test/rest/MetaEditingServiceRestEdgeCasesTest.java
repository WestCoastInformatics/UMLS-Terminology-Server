/*
 * Copyright 2016 West Coast Informatics, LLC
 */
/*
 * 
 */
package com.wci.umls.server.test.rest;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.wci.umls.server.Project;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.ProjectList;
import com.wci.umls.server.jpa.ProjectJpa;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.content.SemanticTypeComponentJpa;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.SemanticTypeComponent;

//TODO eventually - fill this out

/**
 * Implementation of the "MetaEditing Service REST Normal Use" Test Cases.
 */
public class MetaEditingServiceRestEdgeCasesTest
    extends MetaEditingServiceRestTest {

  /** The auth tokens. */
  static String authToken;

  /** The project. */
  static Project project;

  /** The umls terminology. */
  private String umlsTerminology = "UMLS";

  /** The umls version. */
  private String umlsVersion = "latest";

  /**
   * Create test fixtures per test.
   *
   * @throws Exception the exception
   */
  @Override
  @Before
  public void setup() throws Exception {

    // authenticate the viewer user
    authToken =
        securityService.authenticate(adminUser, adminPassword).getAuthToken();

    // ensure there is a concept associated with the project
    ProjectList projects = projectService.getProjects(authToken);
    assertTrue(projects.getCount() > 0);
    project = projects.getObjects().get(0);

    // verify terminology and branch are expected values
    assertTrue(project.getTerminology().equals(umlsTerminology));
    assertTrue(project.getBranch().equals(Branch.ROOT));
  }

  /**
   * Test terminology and branch mismatches between project and concept
   * NOTE: Only need this one test, as the project/concept checks are uniform
   * across all MetaEditing REST calls
   *
   * @throws Exception the exception
   */
  @Test
  public void testEdgeCaseRestContent001() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    Logger.getLogger(getClass()).info(
        "TEST - Degenerate use tests for add/remove semantic type to concept");

    ValidationResult result;

    // get the concept
    Concept c = contentService.getConcept("C0000005", umlsTerminology,
        umlsVersion, null, authToken);
    assertNotNull(c);

    // check against project
    assertTrue(c.getBranch().equals(project.getBranch()));

    // check that concept has semantic types
    assertTrue(c.getSemanticTypes().size() > 0);

    // get the first semantic type
    SemanticTypeComponent sty = c.getSemanticTypes().get(0);
    assertNotNull(sty);

    // get a concept with different semantic type (for testing add)
    // NOTE: Testing addition of already present sty done elsewhere
    Concept c2 = contentService.getConcept("C0000039", umlsTerminology,
        umlsVersion, null, authToken);
    assertNotNull(c2);
    SemanticTypeComponentJpa sty2 =
        (SemanticTypeComponentJpa) c2.getSemanticTypes().get(0);
    assertNotNull(sty2);

    //
    // Test calls where terminologies do not match
    //
    project.setTerminology("testTerminology");
    projectService.updateProject((ProjectJpa) project, authToken);

    result = metaEditingService.addSemanticType(project.getId(), c.getId(),
        c.getTimestamp().getTime(), sty2, false, authToken);
    assertTrue(!result.isValid());

    metaEditingService.removeSemanticType(project.getId(), c.getId(),
        c.getTimestamp().getTime(), sty.getId(), false, authToken);
    assertTrue(!result.isValid());

    // reset the terminology
    project.setTerminology(umlsTerminology);
    projectService.updateProject((ProjectJpa) project, authToken);

    //
    // Test calls where branches do not match
    //
    project.setBranch("testBranch");
    projectService.updateProject((ProjectJpa) project, authToken);

    //
    // Test add and remove
    //

    result = metaEditingService.addSemanticType(project.getId(), c.getId(),
        c.getTimestamp().getTime(), sty2, false, authToken);
    assertTrue(!result.isValid());

    result = metaEditingService.removeSemanticType(project.getId(), c.getId(),
        c.getTimestamp().getTime(), sty.getId(), false, authToken);
    assertTrue(!result.isValid());

    // reset the branch
    project.setBranch(Branch.ROOT);
    projectService.updateProject((ProjectJpa) project, authToken);

  }

  /**
   * Test synchronicity.
   * @throws Exception
   */
  @Test
  public void testSynchronicity() throws Exception {

    ValidationResult result = new ValidationResultJpa();

    // get the concept
    Concept c1 = contentService.getConcept("C0000530", umlsTerminology,
        umlsVersion, null, authToken);

    // construct a semantic type not present on concept (here, Lipid)
    SemanticTypeComponent sty = new SemanticTypeComponentJpa();
    sty.setBranch(c1.getBranch());
    sty.setLastModifiedBy(authToken);
    sty.setSemanticType("Lipid");
    sty.setTerminologyId("TestId");
    sty.setTerminology(umlsTerminology);
    sty.setVersion(umlsVersion);
    sty.setTimestamp(new Date());

    // add the sty
    result = metaEditingService.addSemanticType(project.getId(), c1.getId(),
        c1.getTimestamp().getTime(), (SemanticTypeComponentJpa) sty, false,
        authToken);
    if (!result.isValid()) {
      Logger.getLogger(getClass()).info("Invalid result: " + result.toString());
    }
    assertTrue(result.isValid());

    // get the concept
    c1 = contentService.getConcept("C0000530", umlsTerminology, umlsVersion,
        null, authToken);

    sty = null;
    for (SemanticTypeComponent s : c1.getSemanticTypes()) {
      if (s.getSemanticType().equals("Lipid")) {
        sty = s;
      }
    }
    assertNotNull(sty);

    final Long cId = c1.getId();
    final Long cDate = c1.getTimestamp().getTime();
    final Long styId = sty.getId();

    // number of repeated calls to make
    final int[] nThreads = {
        10
    };

    // runnable instanes
    final Thread[] threads = new Thread[nThreads[0]];

    // accessible validation results from within runnables
    final ValidationResult[] v = new ValidationResultJpa[nThreads[0]];

    // accessible index, success, and exception counters
    int ct[] = new int[1];
    int completeCt[] = {
        0
    };
    int successCt[] = {
        0
    };
    int exceptionCt[] = {
        0
    };

    for (int i = 0; i < nThreads[0]; i++) {
      v[i] = new ValidationResultJpa();
      ct[0] = i;
      threads[i] = new Thread(new Runnable() {
        @Override
        public void run() {
          Logger.getLogger(getClass()).info("Running new thread");
          
          // introduce slight delay (really for cleanliness of log)
          try {
            Thread.sleep(500);
          } catch (InterruptedException e1) {
            Logger.getLogger(getClass()).info("  Sleep command failed");
          }
          try {
            if (metaEditingService.removeSemanticType(project.getId(), cId,
                cDate, styId, false, authToken).isValid()) {
              Logger.getLogger(getClass()).info("  Thread returned success");
              successCt[0]++;
            } else {
              Logger.getLogger(getClass()).info("  Thread returned expected failure");
            }
          } catch (Exception e) {
            Logger.getLogger(getClass()).info("  Unexpected exception encountered");
            exceptionCt[0]++;
          } finally {
            completeCt[0]++;
            Logger.getLogger(getClass()).info("Thread complete: " + completeCt[0] + "/"
                + nThreads[0] + " (" + exceptionCt[0] + " exceptions, "
                + successCt[0] + " successes)");
            // on all threads complete, expect only one valid result and no
            // exceptions
            // NOTE: Exceptions indicate a commit error (asynchronous failure)
            // NOTE: Failures due to data conditions are captured as validation
            // results (isValid check above)
            if (completeCt[0] == nThreads[0]) {
              assertTrue(exceptionCt[0] == 0);
              assertTrue(successCt[0] == 1);
              Logger.getLogger(getClass()).info("Synchronicity test complete");
              assertTrue(false);
            }
          }
        }
      });
    }

    // execute the simultaneous requests
    for (int i = 0; i < nThreads[0]; i++) {
      threads[i].start();
    }
    
    // wait an arbitrary amount of time
    Thread.sleep(20000);

  }

  /**
   * Teardown.
   *
   * @throws Exception the exception
   */
  @Override
  @After
  public void teardown() throws Exception {

    // ensure branch and terminology are set correctly
    project.setBranch(Branch.ROOT);
    project.setTerminology(umlsTerminology);
    projectService.updateProject((ProjectJpa) project, authToken);
    // logout
    securityService.logout(authToken);

  }

}
