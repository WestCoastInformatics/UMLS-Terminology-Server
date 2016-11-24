/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.algo;

import java.util.UUID;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.wci.umls.server.Project;
import com.wci.umls.server.algo.Algorithm;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.ProjectList;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.services.ProjectServiceJpa;
import com.wci.umls.server.jpa.services.WorkflowServiceJpa;
import com.wci.umls.server.services.ProjectService;
import com.wci.umls.server.services.WorkflowService;
import com.wci.umls.server.test.helpers.IntegrationUnitSupport;

/**
 * Support for integration tests
 */
public class AlgoIntegrationTestSupport extends IntegrationUnitSupport {

  /** The project. */
  public static Project project;

  /** The log service. */
  public static WorkflowService logService;

  /**
   * Setup class.
   *
   * @throws Exception the exception
   */
  @BeforeClass
  public static void setupClass() throws Exception {
    final ProjectService service = new ProjectServiceJpa();
    try {
      final PfsParameter pfs = new PfsParameterJpa();
      pfs.setStartIndex(0);
      pfs.setMaxResults(1);
      final ProjectList list = service.findProjects(null, pfs);
      if (list.size() < 1) {
        throw new Exception("No projects exist.");
      }
      project = list.getObjects().get(0);
      service.handleLazyInit(project);

      logService = new WorkflowServiceJpa();
    } catch (Exception e) {
      throw e;
    } finally {
      service.close();
    }
  }

  /**
   * Compute.
   *
   * @param algo the algo
   * @return the string
   * @throws Exception the exception
   */
  public static String compute(Algorithm algo) throws Exception {
    final String workId = UUID.randomUUID().toString();
    algo.setWorkId(workId);
    algo.setTransactionPerOperation(false);
    algo.beginTransaction();
    algo.compute();
    algo.commit();
    return workId;
  }

  /**
   * Returns the project.
   *
   * @return the project
   */
  public static Project getProject() {
    return project;
  }

  /**
   * Returns the log service.
   *
   * @return the log service
   */
  public static WorkflowService getLogService() {
    return logService;
  }

  /**
   * Teardown class.
   *
   * @throws Exception the exception
   */
  @AfterClass
  public static void teardownClass() throws Exception {
    logService.close();
  }
}