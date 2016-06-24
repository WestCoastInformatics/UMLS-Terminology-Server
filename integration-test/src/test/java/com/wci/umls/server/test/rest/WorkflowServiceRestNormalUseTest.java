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
import com.wci.umls.server.jpa.worfklow.WorkflowBinDefinitionJpa;
import com.wci.umls.server.jpa.worfklow.WorkflowConfigJpa;
import com.wci.umls.server.jpa.worfklow.WorklistJpa;
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
   * Test add and remove workflow config and regenerate bins
   *
   * @throws Exception the exception
   */
  //@Test
  public void testNormalUseRestWorkflow003() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    Logger.getLogger(getClass()).info(
        "TEST - Regenerate bins" + umlsTerminology + ", " + umlsVersion + ", "
            + authToken);

    //
    // Prepare the test and check prerequisites
    //
    Date startDate = new Date();

    /*WorkflowConfigJpa workflowConfig = new WorkflowConfigJpa();
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
    workflowBinDefinition
        .setQuery("select c.id clusterId, c.id componentId from concepts c where c.name = 'adopce';");
    workflowBinDefinition.setEditable(true);
    workflowBinDefinition.setLastModified(startDate);
    workflowBinDefinition.setLastModifiedBy(authToken);
    workflowBinDefinition.setQueryType(QueryType.SQL);
    workflowBinDefinition.setTimestamp(startDate);
    workflowBinDefinition.setWorkflowConfig(addedWorkflowConfig);

    WorkflowBinDefinition addedWorkflowBinDefinition =
        workflowService.addWorkflowBinDefinition(project.getId(),
            addedWorkflowConfig.getId(), workflowBinDefinition, authToken);

    WorkflowBinDefinitionJpa workflowBinDefinition2 =
        new WorkflowBinDefinitionJpa();
    workflowBinDefinition2.setName("test name2");
    workflowBinDefinition2.setDescription("test description2");
    workflowBinDefinition2
        .setQuery("select c.id clusterId, c.id componentId from concepts c where c.name = 'AIDS';");
    workflowBinDefinition2.setEditable(true);
    workflowBinDefinition2.setLastModified(startDate);
    workflowBinDefinition2.setLastModifiedBy(authToken);
    workflowBinDefinition2.setQueryType(QueryType.SQL);
    workflowBinDefinition2.setTimestamp(startDate);
    workflowBinDefinition2.setWorkflowConfig(addedWorkflowConfig);

    WorkflowBinDefinition addedWorkflowBinDefinition2 =
        workflowService.addWorkflowBinDefinition(project.getId(),
            addedWorkflowConfig.getId(), workflowBinDefinition2, authToken);
*/
    //
    // Regenerate bins
    //
    workflowService.regenerateBins(project.getId(),
        WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);

    //
    // Test clean up
    //

    // remove workflow bin definitions
    /*workflowService.removeWorkflowBinDefinition(project.getId(),
        addedWorkflowBinDefinition.getId(), authToken);
    workflowService.removeWorkflowBinDefinition(project.getId(),
        addedWorkflowBinDefinition2.getId(), authToken);

    // remove the workflow config
    workflowService.removeWorkflowConfig(addedWorkflowConfig.getId(),
        authToken);*/

    // remove bins and tracking records
    // TODO clear bins
    /*for (WorkflowBin bin : integrationTestService.getWorkflowBins(authToken)) {
      for (TrackingRecord record : bin.getTrackingRecords()) {
        integrationTestService.removeTrackingRecord(record.getId());
      }
      integrationTestService.removeWorkflowBin(bin.getId());
    }*/

  }

  /**
   * Test perform workflow action
   *
   * @throws Exception the exception
   */
  @Test
  public void testNormalUseRestWorkflow004() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

    Logger.getLogger(getClass()).info(
        "TEST - Perform workflow action" + umlsTerminology + ", " + umlsVersion
            + ", " + authToken);

    //
    // Prepare the test and check prerequisites
    //
    Date startDate = new Date();

/*    WorkflowConfigJpa workflowConfig = new WorkflowConfigJpa();
    workflowConfig.setLastModifiedBy(authToken);
    workflowConfig.setTimestamp(startDate);
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
    workflowBinDefinition
        .setQuery("select c.id clusterId, c.id componentId from concepts c where c.name = 'adopce';");
    workflowBinDefinition.setEditable(true);
    workflowBinDefinition.setLastModified(startDate);
    workflowBinDefinition.setLastModifiedBy(authToken);
    workflowBinDefinition.setQueryType(QueryType.SQL);
    workflowBinDefinition.setTimestamp(startDate);
    workflowBinDefinition.setWorkflowConfig(addedWorkflowConfig);

    WorkflowBinDefinition addedWorkflowBinDefinition =
        workflowService.addWorkflowBinDefinition(project.getId(),
            addedWorkflowConfig.getId(), workflowBinDefinition, authToken);
*/
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
