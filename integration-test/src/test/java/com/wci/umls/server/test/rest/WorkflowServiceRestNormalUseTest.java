/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package com.wci.umls.server.test.rest;

import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.wci.umls.server.Project;
import com.wci.umls.server.UserRole;
import com.wci.umls.server.helpers.ProjectList;
import com.wci.umls.server.helpers.WorkflowBinList;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.worfklow.WorkflowBinDefinitionJpa;
import com.wci.umls.server.jpa.worfklow.WorkflowConfigJpa;
import com.wci.umls.server.jpa.worfklow.WorklistJpa;
import com.wci.umls.server.model.workflow.Checklist;
import com.wci.umls.server.model.workflow.QueryType;
import com.wci.umls.server.model.workflow.WorkflowAction;
import com.wci.umls.server.model.workflow.WorkflowBinDefinition;
import com.wci.umls.server.model.workflow.WorkflowBinType;
import com.wci.umls.server.model.workflow.WorkflowConfig;
import com.wci.umls.server.model.workflow.WorkflowStatus;
import com.wci.umls.server.model.workflow.Worklist;

/**
 * Implementation of the "Workflow Service REST Normal Use" Test Cases.
 */
public class WorkflowServiceRestNormalUseTest extends WorkflowServiceRestTest {

  /** The auth token. */
  private static String authToken;

  /** The project. */
  private static Project project;

  /** The umls terminology. */
  private String umlsTerminology = "UMLS";

  /** The umls version. */
  private String umlsVersion = "latest";
  
  /**  The added worklist. */
  private Worklist addedWorklist;


  /**
   * Create test fixtures per test.
   *
   * @throws Exception the exception
   */
  @Override
  @Before
  public void setup() throws Exception {

    // authentication (admin for editing permissions)
    authToken =
        securityService.authenticate(adminUser, adminPassword).getAuthToken();

    // ensure there is a concept associated with the project
    ProjectList projects = projectService.getProjects(authToken);
    assertTrue(projects.getCount() > 0);
    project = projects.getObjects().get(0);

    // verify terminology and branch are expected values
    assertTrue(project.getTerminology().equals(umlsTerminology));
    // TODO assertTrue(project.getBranch().equals(Branch.ROOT));

    
  }

  /**
   * Test add and remove workflow config
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestWorkflow00() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    Logger.getLogger(getClass()).info(
        "TEST - Add and remove workflow config" + umlsTerminology + ", "
            + umlsVersion + ", " + authToken);

    //
    // Prepare the test and check prerequisites
    //
    Date startDate = new Date();

    WorkflowConfigJpa workflowConfig = new WorkflowConfigJpa();
    workflowConfig.setLastModifiedBy(authToken);
    workflowConfig.setTimestamp(new Date());
    workflowConfig.setType(WorkflowBinType.MUTUALLY_EXCLUSIVE);
    workflowConfig.setMutuallyExclusive(true);
    workflowConfig.setProjectId(project.getId());
    workflowConfig.setLastModified(startDate);
    workflowConfig.setTimestamp(startDate);
    workflowConfig.setLastPartitionTime(1L);

    //
    // Test addition
    //

    // add the workflow config
    WorkflowConfig addedWorkflowConfig =
        workflowService.addWorkflowConfig(project.getId(), workflowConfig,
            authToken);
    assertTrue(addedWorkflowConfig.getType() == WorkflowBinType.MUTUALLY_EXCLUSIVE);
    assertTrue(addedWorkflowConfig.isMutuallyExclusive());
    assertTrue(addedWorkflowConfig.getLastModifiedBy().equals(authToken));
    assertTrue(addedWorkflowConfig.getProject().getId().longValue() == project
        .getId().longValue());

    //
    // Test update
    //

    // update the workflow config
    addedWorkflowConfig.setType(WorkflowBinType.AD_HOC);
    addedWorkflowConfig.setMutuallyExclusive(false);
    workflowService.updateWorkflowConfig(project.getId(),
        (WorkflowConfigJpa) addedWorkflowConfig, authToken);

    //
    // Test removal
    //

    // remove the workflow config
    workflowService
        .removeWorkflowConfig(addedWorkflowConfig.getId(), authToken);
    // assertTrue(v.getErrors().isEmpty());

  }

  /**
   * Test add and remove workflow config
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestWorkflow002() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    Logger.getLogger(getClass()).info(
        "TEST - Add and remove workflow bin definition" + umlsTerminology
            + ", " + umlsVersion + ", " + authToken);

    //
    // Prepare the test and check prerequisites
    //
    Date startDate = new Date();

    WorkflowConfigJpa workflowConfig = new WorkflowConfigJpa();
    workflowConfig.setLastModifiedBy(authToken);
    workflowConfig.setTimestamp(new Date());
    workflowConfig.setType(WorkflowBinType.MUTUALLY_EXCLUSIVE);
    workflowConfig.setMutuallyExclusive(true);
    workflowConfig.setProjectId(project.getId());
    workflowConfig.setLastModified(startDate);
    workflowConfig.setTimestamp(startDate);
    workflowConfig.setLastPartitionTime(1L);

    //
    // Test addition
    //

    // add the workflow config
    WorkflowConfig addedWorkflowConfig =
        workflowService.addWorkflowConfig(project.getId(), workflowConfig,
            authToken);

    WorkflowBinDefinitionJpa workflowBinDefinition =
        new WorkflowBinDefinitionJpa();
    workflowBinDefinition.setName("test name");
    workflowBinDefinition.setDescription("test description");
    workflowBinDefinition.setQuery("select * from concepts");
    workflowBinDefinition.setEditable(true);
    workflowBinDefinition.setLastModified(startDate);
    workflowBinDefinition.setLastModifiedBy(authToken);
    workflowBinDefinition.setQueryType(QueryType.SQL);
    workflowBinDefinition.setTimestamp(startDate);
    workflowBinDefinition.setWorkflowConfig(addedWorkflowConfig);

    WorkflowBinDefinition addedWorkflowBinDefinition =
        workflowService.addWorkflowBinDefinition(project.getId(),
            addedWorkflowConfig.getId(), workflowBinDefinition, authToken);

    //
    // Test update
    //

    // update the workflow bin definition
    addedWorkflowBinDefinition.setEditable(false);
    addedWorkflowBinDefinition.setDescription("test description2");
    workflowService.updateWorkflowBinDefinition(project.getId(),
        (WorkflowBinDefinitionJpa) addedWorkflowBinDefinition, authToken);

    //
    // Test removal
    //

    workflowService.removeWorkflowBinDefinition(project.getId(),
        addedWorkflowBinDefinition.getId(), authToken);
    // remove the workflow config
    workflowService
        .removeWorkflowConfig(addedWorkflowConfig.getId(), authToken);
    // assertTrue(v.getErrors().isEmpty());

  }

  /**
   * Test regenerate and clear bins
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestWorkflow003() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    Logger.getLogger(getClass()).info(
        "TEST - Regenerate bins" + umlsTerminology + ", " + umlsVersion + ", "
            + authToken);

    //
    // Regenerate bins and then clear bins
    //
    try {
      workflowService.regenerateBins(project.getId(),
        WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);
    } catch (Exception e) {
      workflowService.clearBins(project.getId(),
          WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);
      throw e;
    }

    workflowService.clearBins(project.getId(),
        WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);

  }

  /**
   * Test create checklist
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestWorkflow004() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    Logger.getLogger(getClass()).info(
        "TEST - Create checklist" + umlsTerminology + ", " + umlsVersion + ", "
            + authToken);

    try {
      workflowService.regenerateBins(project.getId(),
          WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);
    } catch (Exception e) {
      workflowService.clearBins(project.getId(),
          WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);
      throw e;
    }
    WorkflowBinList binList = workflowService
        .findWorkflowBinsForQuery("name:testName", null, authToken);

    //
    // Create checklist with cluster id order tracking records
    //
    Checklist checklistOrderByClusterId;
    try {
      checklistOrderByClusterId = workflowService.createChecklist(
          project.getId(), binList.getObjects().get(0).getId(),
          "checklistOrderByClusterId", false, false, "clusterType:chem",
          new PfsParameterJpa(), authToken);
    } catch (Exception e) {
      workflowService.clearBins(project.getId(),
          WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);
      throw e;
    }

    //
    // Create checklist with random tracking records
    //
    Checklist checklistOrderByRandom;
    try {
      checklistOrderByRandom = workflowService.createChecklist(project.getId(),
          binList.getObjects().get(0).getId(), "checklistOrderByRandom", true,
          false, "clusterType:chem", new PfsParameterJpa(), authToken);
    } catch (Exception e) {
      workflowService.clearBins(project.getId(),
          WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);
      throw e;
    }
    /*assertTrue(!checklistOrderByClusterId.getTrackingRecords()
        .equals(checklistOrderByRandom.getTrackingRecords()));*/
    workflowService.removeChecklist(checklistOrderByClusterId.getId(),
        authToken);
    workflowService.removeChecklist(checklistOrderByRandom.getId(), authToken);

    //
    // Create worklist
    //
    Worklist worklist;
    try {
      worklist = workflowService.createWorklist(project.getId(), binList.getObjects().get(0).getId(), 
          "chem", 0, 5, new PfsParameterJpa(), authToken);
    } catch (Exception e) {
      workflowService.clearBins(project.getId(),
          WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);
      throw e;
    }
    
    workflowService.clearBins(project.getId(),
        WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);


  }
  
  /**
   * Test perform workflow action
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestWorkflow005() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

    Logger.getLogger(getClass()).info(
        "TEST - Perform workflow action" + umlsTerminology + ", " + umlsVersion
            + ", " + authToken);


    //
    // Regenerate bins
    //
    workflowService.regenerateBins(project.getId(),
        WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);

    /*WorkflowBin workflowBin = integrationTestService.getWorkflowBins(authToken).get(0);
    List<TrackingRecord> trackingRecordList = workflowBin.getTrackingRecords();
*/
    WorklistJpa worklist = new WorklistJpa();
    worklist.setName("test worklist");
    worklist.setDescription("test worklist description");
    worklist.setProject(project);
    worklist.setWorkflowStatus(WorkflowStatus.NEW);
    worklist.setTimestamp(new Date());

    addedWorklist = integrationTestService.addWorklist(worklist, authToken);

    // go through the trackingrecords and add the worklist to each object
    /*addedWorklist.setTrackingRecords(trackingRecordList);
    integrationTestServiceJpa.updateWorklist(addedWorklist);*/

    //
    // Test perform workflow action
    //
    workflowService.performWorkflowAction(project.getId(),
        addedWorklist.getId(), authToken, UserRole.AUTHOR,
        WorkflowAction.ASSIGN, authToken);
    assertTrue(integrationTestService.getWorklist(addedWorklist.getId(), authToken)
        .getWorkflowStatus() == WorkflowStatus.NEW);

    workflowService.performWorkflowAction(project.getId(),
        addedWorklist.getId(), authToken, UserRole.AUTHOR,
        WorkflowAction.UNASSIGN, authToken);
    assertTrue(integrationTestService.getWorklist(addedWorklist.getId(), authToken)
        .getWorkflowStatus() == WorkflowStatus.NEW);

    workflowService.performWorkflowAction(project.getId(),
        addedWorklist.getId(), authToken, UserRole.AUTHOR,
        WorkflowAction.ASSIGN, authToken);
    assertTrue(integrationTestService.getWorklist(addedWorklist.getId(), authToken)
        .getWorkflowStatus() == WorkflowStatus.NEW);

    workflowService.performWorkflowAction(project.getId(),
        addedWorklist.getId(), authToken, UserRole.AUTHOR, WorkflowAction.SAVE,
        authToken);
    assertTrue(integrationTestService.getWorklist(addedWorklist.getId(), authToken)
        .getWorkflowStatus() == WorkflowStatus.EDITING_IN_PROGRESS);

    workflowService.performWorkflowAction(project.getId(),
        addedWorklist.getId(), authToken, UserRole.AUTHOR,
        WorkflowAction.UNASSIGN, authToken);
    assertTrue(integrationTestService.getWorklist(addedWorklist.getId(), authToken)
        .getWorkflowStatus() == WorkflowStatus.NEW);

    workflowService.performWorkflowAction(project.getId(),
        addedWorklist.getId(), authToken, UserRole.AUTHOR,
        WorkflowAction.ASSIGN, authToken);
    assertTrue(integrationTestService.getWorklist(addedWorklist.getId(), authToken)
        .getWorkflowStatus() == WorkflowStatus.NEW);

    workflowService.performWorkflowAction(project.getId(),
        addedWorklist.getId(), authToken, UserRole.AUTHOR,
        WorkflowAction.FINISH, authToken);
    assertTrue(integrationTestService.getWorklist(addedWorklist.getId(), authToken)
        .getWorkflowStatus() == WorkflowStatus.EDITING_DONE);

    workflowService.performWorkflowAction(project.getId(),
        addedWorklist.getId(), authToken, UserRole.REVIEWER,
        WorkflowAction.ASSIGN, authToken);
    assertTrue(integrationTestService.getWorklist(addedWorklist.getId(), authToken)
        .getWorkflowStatus() == WorkflowStatus.REVIEW_NEW);

    workflowService.performWorkflowAction(project.getId(),
        addedWorklist.getId(), authToken, UserRole.REVIEWER,
        WorkflowAction.UNASSIGN, authToken);
    assertTrue(integrationTestService.getWorklist(addedWorklist.getId(), authToken)
        .getWorkflowStatus() == WorkflowStatus.EDITING_DONE);

    workflowService.performWorkflowAction(project.getId(),
        addedWorklist.getId(), authToken, UserRole.REVIEWER,
        WorkflowAction.ASSIGN, authToken);
    assertTrue(integrationTestService.getWorklist(addedWorklist.getId(), authToken)
        .getWorkflowStatus() == WorkflowStatus.REVIEW_NEW);

    workflowService.performWorkflowAction(project.getId(),
        addedWorklist.getId(), authToken, UserRole.REVIEWER,
        WorkflowAction.SAVE, authToken);
    assertTrue(integrationTestService.getWorklist(addedWorklist.getId(), authToken)
        .getWorkflowStatus() == WorkflowStatus.REVIEW_IN_PROGRESS);

    workflowService.performWorkflowAction(project.getId(),
        addedWorklist.getId(), authToken, UserRole.REVIEWER,
        WorkflowAction.UNASSIGN, authToken);
    assertTrue(integrationTestService.getWorklist(addedWorklist.getId(), authToken)
        .getWorkflowStatus() == WorkflowStatus.EDITING_DONE);

    workflowService.performWorkflowAction(project.getId(),
        addedWorklist.getId(), authToken, UserRole.REVIEWER,
        WorkflowAction.ASSIGN, authToken);
    assertTrue(integrationTestService.getWorklist(addedWorklist.getId(), authToken)
        .getWorkflowStatus() == WorkflowStatus.REVIEW_NEW);

    workflowService.performWorkflowAction(project.getId(),
        addedWorklist.getId(), authToken, UserRole.REVIEWER,
        WorkflowAction.FINISH, authToken);
    assertTrue(integrationTestService.getWorklist(addedWorklist.getId(), authToken)
        .getWorkflowStatus() == WorkflowStatus.REVIEW_DONE);

    workflowService.performWorkflowAction(project.getId(),
        addedWorklist.getId(), authToken, UserRole.REVIEWER,
        WorkflowAction.FINISH, authToken);
    assertTrue(integrationTestService.getWorklist(addedWorklist.getId(), authToken)
        .getWorkflowStatus() == WorkflowStatus.READY_FOR_PUBLICATION);

    //
    // Test clean up
    //

    /*// remove workflow bin definitions
    workflowService.removeWorkflowBinDefinition(project.getId(),
        addedWorkflowBinDefinition.getId(), authToken);

    // remove the workflow config
    workflowService
        .removeWorkflowConfig(addedWorkflowConfig.getId(), authToken);

    // remove bins and tracking records
    for (WorkflowBin bin : workflowServiceJpa.getWorkflowBins()) {
      for (TrackingRecord record : bin.getTrackingRecords()) {
        workflowServiceJpa.removeTrackingRecord(record.getId());
      }
      workflowServiceJpa.removeWorkflowBin(bin.getId());
    }*/

  }

  /**
   * Teardown.
   *
   * @throws Exception the exception
   */
  @Override
  @After
  public void teardown() throws Exception {

    // logout
    securityService.logout(authToken);
  }

}
