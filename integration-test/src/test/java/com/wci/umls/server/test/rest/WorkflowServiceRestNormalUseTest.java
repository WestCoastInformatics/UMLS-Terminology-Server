/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package com.wci.umls.server.test.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.wci.umls.server.Project;
import com.wci.umls.server.User;
import com.wci.umls.server.UserRole;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.StringList;
import com.wci.umls.server.helpers.meta.SemanticTypeList;
import com.wci.umls.server.jpa.ProjectJpa;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.services.MetadataServiceJpa;
import com.wci.umls.server.jpa.worfklow.WorkflowBinDefinitionJpa;
import com.wci.umls.server.jpa.worfklow.WorkflowConfigJpa;
import com.wci.umls.server.jpa.worfklow.WorkflowEpochJpa;
import com.wci.umls.server.model.meta.SemanticType;
import com.wci.umls.server.model.workflow.Checklist;
import com.wci.umls.server.model.workflow.QueryType;
import com.wci.umls.server.model.workflow.TrackingRecord;
import com.wci.umls.server.model.workflow.WorkflowAction;
import com.wci.umls.server.model.workflow.WorkflowBin;
import com.wci.umls.server.model.workflow.WorkflowBinDefinition;
import com.wci.umls.server.model.workflow.WorkflowBinType;
import com.wci.umls.server.model.workflow.WorkflowConfig;
import com.wci.umls.server.model.workflow.WorkflowEpoch;
import com.wci.umls.server.model.workflow.WorkflowStatus;
import com.wci.umls.server.model.workflow.Worklist;
import com.wci.umls.server.services.MetadataService;

/**
 * Implementation of the "Workflow Service REST Normal Use" Test Cases.
 */
public class WorkflowServiceRestNormalUseTest extends WorkflowServiceRestTest {

  /** The auth token. */
  private static String authToken;

  /** The project. */
  private static Project project;

  /** The project. */
  private static Long projectId;

  /** The config. */
  private static WorkflowConfig config;

  /** The definition. */
  private static WorkflowBinDefinition definition;

  /** The epoch. */
  private static WorkflowEpoch epoch;

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

    // authentication (admin for editing permissions)
    final User user = securityService.authenticate(adminUser, adminPassword);
    authToken = user.getAuthToken();

    project = new ProjectJpa();
    project.setBranch(Branch.ROOT);
    project.setDescription("Test project");
    project.setFeedbackEmail("info@westcoastinformatics.com");
    project.setName("Test Project " + new Date().getTime());
    project.setPublic(true);
    project.setTerminology(umlsTerminology);
    project.setWorkflowPath(ConfigUtility.DEFAULT);
    // Configure valid categories
    final List<String> validCategories = new ArrayList<>();
    validCategories.add("chem");
    project.setValidCategories(validCategories);

    Map<String, String> semanticTypeCategoryMap = getSemanticTypeCategoryMap();
    project.setSemanticTypeCategoryMap(semanticTypeCategoryMap);

    // Add project
    project = projectService.addProject((ProjectJpa) project, authToken);
    projectId = project.getId();

    // Create an epoch
    epoch = new WorkflowEpochJpa();
    epoch.setActive(true);
    epoch.setName("16a");
    epoch.setProject(project);
    epoch =
        workflowService.addWorkflowEpoch(project.getId(),
            (WorkflowEpochJpa) epoch, authToken);

    // Create a workflow config
    config = new WorkflowConfigJpa();
    config.setType(WorkflowBinType.MUTUALLY_EXCLUSIVE);
    config.setMutuallyExclusive(true);
    config.setProject(project);
    config =
        workflowService.addWorkflowConfig(projectId,
            (WorkflowConfigJpa) config, authToken);

    // Add a workflow definition (as SQL)
    // TODO: create workflow bin definitions exactly matching NCI-META config
    // also
    definition = new WorkflowBinDefinitionJpa();
    definition.setName("testName");
    definition.setDescription("test description");
    definition
        .setQuery("select distinct c.id clusterId, c.id conceptId from concepts c where c.name like '%Amino%';");
    definition.setEditable(true);
    definition.setQueryType(QueryType.SQL);
    definition.setWorkflowConfig(config);
    definition =
        workflowService.addWorkflowBinDefinition(projectId,
            (WorkflowBinDefinitionJpa) definition, authToken);

    // verify terminology matches
    assertTrue(project.getTerminology().equals(umlsTerminology));

  }

  /**
   * Test add and remove workflow config.
   *
   * @throws Exception the exception
   */
  @Test
  public void testAddAndRemoveWorkflowConfig() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    //
    // Create workflow config
    //
    Logger.getLogger(getClass()).debug("  Add workflow config");
    final WorkflowConfigJpa config = new WorkflowConfigJpa();
    config.setType(WorkflowBinType.QUALITY_ASSURANCE);
    config.setMutuallyExclusive(true);
    config.setProjectId(projectId);

    // Add workflow config
    WorkflowConfig newConfig =
        workflowService.addWorkflowConfig(projectId, config, authToken);
    Logger.getLogger(getClass()).debug("    config = " + newConfig);
    assertEquals(WorkflowBinType.QUALITY_ASSURANCE, newConfig.getType());
    assertTrue(newConfig.isMutuallyExclusive());
    assertEquals(adminUser, newConfig.getLastModifiedBy());
    assertEquals(projectId, newConfig.getProject().getId());

    // Update workflow config
    Logger.getLogger(getClass()).debug("  Update workflow config");
    newConfig.setMutuallyExclusive(false);
    workflowService.updateWorkflowConfig(projectId,
        (WorkflowConfigJpa) newConfig, authToken);
    newConfig =
        workflowService.getWorkflowConfig(projectId, newConfig.getId(),
            authToken);
    Logger.getLogger(getClass()).debug("    config = " + config);
    assertFalse(newConfig.isMutuallyExclusive());

    // Remove the workflow config
    Logger.getLogger(getClass()).debug("  Remove workflow config");
    workflowService.removeWorkflowConfig(projectId, newConfig.getId(),
        authToken);
    try {
      workflowService
          .getWorkflowConfig(projectId, newConfig.getId(), authToken);
      fail("Expected exception.");
    } catch (Exception e) {
      // do nothing
    }

  }

  /**
   * Test add and remove workflow bin definition.
   *
   * @throws Exception the exception
   */
  @Test
  public void testAddAndRemoveWorkflowBinDefinition() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Create workflow bin definition
    Logger.getLogger(getClass()).debug("  Add workflow bin definition");
    final WorkflowBinDefinitionJpa definition = new WorkflowBinDefinitionJpa();
    definition.setName("test name");
    definition.setDescription("test description");
    definition.setQuery("select * from concepts");
    definition.setEditable(true);
    definition.setQueryType(QueryType.SQL);
    definition.setWorkflowConfig(config);

    // Add workflow bin definition
    WorkflowBinDefinition newDefinition =
        workflowService.addWorkflowBinDefinition(projectId, definition,
            authToken);
    Logger.getLogger(getClass()).debug("    definition = " + newDefinition);
    assertEquals("test name", newDefinition.getName());
    assertEquals("test description", newDefinition.getDescription());
    assertEquals("select * from concepts", newDefinition.getQuery());
    assertTrue(newDefinition.isEditable());
    assertEquals(QueryType.SQL, newDefinition.getQueryType());
    assertEquals(config.getId(), newDefinition.getWorkflowConfig().getId());

    // Update workflow bin definition
    Logger.getLogger(getClass()).debug("  Update workflow bin definition");
    newDefinition.setEditable(false);
    newDefinition.setDescription("test description2");
    workflowService.updateWorkflowBinDefinition(projectId,
        (WorkflowBinDefinitionJpa) newDefinition, authToken);
    newDefinition =
        workflowService.getWorkflowBinDefinition(projectId,
            newDefinition.getId(), authToken);
    Logger.getLogger(getClass()).debug("    definition = definition");
    assertFalse(newDefinition.isEditable());
    assertEquals("test description2", newDefinition.getDescription());

    // Remove workflow bin definition
    Logger.getLogger(getClass()).debug("  Remove workflow bin definition");
    workflowService.removeWorkflowBinDefinition(projectId,
        newDefinition.getId(), authToken);

  }

  /**
   * Test regenerate and clear bins.
   *
   * @throws Exception the exception
   */
  @Test
  public void testClearAndRegenerateBins() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Clear bins
    Logger.getLogger(getClass()).debug("  Clear and regenerate bins");
    workflowService.clearBins(projectId, WorkflowBinType.MUTUALLY_EXCLUSIVE,
        authToken);
    final List<WorkflowBin> binList =
        workflowService.getWorkflowBins(projectId,
            WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);
    assertEquals(0, binList.size());

    // Regenerate bins
    workflowService.regenerateBins(projectId,
        WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);
    final List<WorkflowBin> binList2 =
        workflowService.getWorkflowBins(projectId,
            WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);
    assertEquals(1, binList2.size());

    // Clear bins
    Logger.getLogger(getClass()).debug("  Clear bins");
    workflowService.clearBins(projectId, WorkflowBinType.MUTUALLY_EXCLUSIVE,
        authToken);
  }

  /**
   * Test create/find/delete checklist.
   *
   * @throws Exception the exception
   */
  @Test
  public void testChecklists() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Clear bins
    Logger.getLogger(getClass()).debug("  Clear and regenerate bins");
    workflowService.clearBins(projectId, WorkflowBinType.MUTUALLY_EXCLUSIVE,
        authToken);

    // Regenerate gins
    workflowService.regenerateBins(projectId,
        WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);

    Logger.getLogger(getClass()).debug("  Find testName workflow bin");
    final List<WorkflowBin> binList =
        workflowService.getWorkflowBins(projectId,
            WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);
    WorkflowBin testNameBin = null;
    for (final WorkflowBin bin : binList) {
      if (bin.getName().equals("testName")) {
        testNameBin = bin;
        break;
      }
    }
    Logger.getLogger(getClass()).debug("    testNameBin = " + testNameBin);
    assertNotNull(testNameBin);

    //
    // Create checklist with cluster id order tracking records
    //
    Logger.getLogger(getClass()).debug("  Create checklist in order");
    final PfsParameterJpa pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(5);
    pfs.setSortField("clusterId");

    // Non-randomize flag picks consecutive tracking records from the bin
    final Checklist checklistOrderByClusterId =
        workflowService.createChecklist(projectId, testNameBin.getId(),
            "checklistOrderByClusterId", false, false, null, pfs, authToken);
    Logger.getLogger(getClass()).debug(
        "    checklist = " + checklistOrderByClusterId);
    // Assert that cluster ids are contiguous and in order
    long i = 0L;
    for (final TrackingRecord r : workflowService
        .findTrackingRecordsForChecklist(projectId,
            checklistOrderByClusterId.getId(), pfs, authToken).getObjects()) {
      assertEquals(++i, r.getClusterId().longValue());
    }

    // Remove checklist
    Logger.getLogger(getClass()).debug("  Remove checklist");
    workflowService.removeChecklist(projectId,
        checklistOrderByClusterId.getId(), authToken);

    //
    // Create checklist with random tracking records
    //
    // Randomize flag picks random tracking records from the bin
    Logger.getLogger(getClass()).debug("  Create checklist in random order");
    final Checklist checklistOrderByRandom =
        workflowService.createChecklist(projectId, testNameBin.getId(),
            "checklistOrderByRandom", true, false, null, pfs, authToken);
    Logger.getLogger(getClass()).debug(
        "    checklist = " + checklistOrderByRandom);
    // Assert that cluster ids are contiguous and in order
    boolean found = false;
    for (final TrackingRecord r : workflowService
        .findTrackingRecordsForChecklist(projectId,
            checklistOrderByRandom.getId(), pfs, authToken).getObjects()) {
      // If the first 5 get randomly picked, this won't work
      if (r.getClusterId() > 5) {
        found = true;
        break;
      }
    }
    assertTrue("Expected at least one cluster id not in the first 5", found);

    // TODO: test clusterType:chem
    // TODO: test excludeOnWorklist...

    // Remove checklist
    Logger.getLogger(getClass()).debug("  Remove checklist");
    workflowService.removeChecklist(projectId, checklistOrderByRandom.getId(),
        authToken);
    // Clear bins
    Logger.getLogger(getClass()).debug("  Clear bins");
    workflowService.clearBins(projectId, WorkflowBinType.MUTUALLY_EXCLUSIVE,
        authToken);

  }

  /**
   * Test create/find/delete worklist.
   *
   * @throws Exception the exception
   */
  @Test
  public void testWorklists() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // clear bins
    Logger.getLogger(getClass()).debug("  Clear and regenerate bins");
    workflowService.clearBins(projectId, WorkflowBinType.MUTUALLY_EXCLUSIVE,
        authToken);

    // regenerate bins
    workflowService.regenerateBins(projectId,
        WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);

    // get test name bin
    Logger.getLogger(getClass()).debug("  Find testName workflow bin");
    final List<WorkflowBin> binList =
        workflowService.getWorkflowBins(projectId,
            WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);
    WorkflowBin testNameBin = null;
    for (final WorkflowBin bin : binList) {
      if (bin.getName().equals("testName")) {
        testNameBin = bin;
        break;
      }
    }
    Logger.getLogger(getClass()).debug("    testNameBin = " + testNameBin);
    assertNotNull(testNameBin);

    //
    // Create worklist
    //
    Logger.getLogger(getClass()).debug("  Create worklist");
    final Worklist worklist =
        workflowService.createWorklist(projectId, testNameBin.getId(), "chem",
            new PfsParameterJpa(), authToken);
    Logger.getLogger(getClass()).debug("    worklist = " + worklist);

    // TODO: assert something about this

    // Remove the worklist
    Logger.getLogger(getClass()).debug("  Remove worklist");
    workflowService.removeWorklist(projectId, worklist.getId(), authToken);

    // clear bins
    Logger.getLogger(getClass()).debug("  Clear bins");
    workflowService.clearBins(projectId, WorkflowBinType.MUTUALLY_EXCLUSIVE,
        authToken);

  }

  /**
   * Test perform workflow action.
   *
   * @throws Exception the exception
   */
  @Test
  public void testPerformWorkflowAction() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Clear bins
    Logger.getLogger(getClass()).debug("  Clear and regenerate bins");
    workflowService.clearBins(projectId, WorkflowBinType.MUTUALLY_EXCLUSIVE,
        authToken);

    // Regenerate bins
    workflowService.regenerateBins(projectId,
        WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);

    Logger.getLogger(getClass()).debug("  Find testName workflow bin");
    final List<WorkflowBin> binList =
        workflowService.getWorkflowBins(projectId,
            WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);
    WorkflowBin testNameBin = null;
    for (final WorkflowBin bin : binList) {
      if (bin.getName().equals("testName")) {
        testNameBin = bin;
        break;
      }
    }
    Logger.getLogger(getClass()).debug("    testNameBin = " + testNameBin);
    assertNotNull(testNameBin);

    //
    // Create worklist
    //
    Logger.getLogger(getClass()).debug("  Create worklist");
    final PfsParameterJpa pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(5);
    final Worklist worklist =
        workflowService.createWorklist(projectId, testNameBin.getId(), "chem",
            pfs, authToken);
    Logger.getLogger(getClass()).debug("    worklist = " + worklist);

    //
    // Test perform workflow action
    //

    Logger.getLogger(getClass()).debug("  Walk worklist through workflow");
    // Assign
    workflowService.performWorkflowAction(projectId, worklist.getId(),
        authToken, UserRole.AUTHOR, WorkflowAction.ASSIGN, authToken);
    assertTrue(integrationTestService.getWorklist(worklist.getId(), authToken)
        .getWorkflowStatus() == WorkflowStatus.NEW);

    // Unassign
    workflowService.performWorkflowAction(projectId, worklist.getId(),
        authToken, UserRole.AUTHOR, WorkflowAction.UNASSIGN, authToken);
    assertTrue(integrationTestService.getWorklist(worklist.getId(), authToken)
        .getWorkflowStatus() == WorkflowStatus.NEW);

    // Assign again
    workflowService.performWorkflowAction(projectId, worklist.getId(),
        authToken, UserRole.AUTHOR, WorkflowAction.ASSIGN, authToken);
    assertTrue(integrationTestService.getWorklist(worklist.getId(), authToken)
        .getWorkflowStatus() == WorkflowStatus.NEW);

    // Save
    workflowService.performWorkflowAction(projectId, worklist.getId(),
        authToken, UserRole.AUTHOR, WorkflowAction.SAVE, authToken);
    assertTrue(integrationTestService.getWorklist(worklist.getId(), authToken)
        .getWorkflowStatus() == WorkflowStatus.EDITING_IN_PROGRESS);

    // Unassign
    workflowService.performWorkflowAction(projectId, worklist.getId(),
        authToken, UserRole.AUTHOR, WorkflowAction.UNASSIGN, authToken);
    assertTrue(integrationTestService.getWorklist(worklist.getId(), authToken)
        .getWorkflowStatus() == WorkflowStatus.NEW);

    // Assign again
    workflowService.performWorkflowAction(projectId, worklist.getId(),
        authToken, UserRole.AUTHOR, WorkflowAction.ASSIGN, authToken);
    assertTrue(integrationTestService.getWorklist(worklist.getId(), authToken)
        .getWorkflowStatus() == WorkflowStatus.NEW);

    // Finish
    workflowService.performWorkflowAction(projectId, worklist.getId(),
        authToken, UserRole.AUTHOR, WorkflowAction.FINISH, authToken);
    assertTrue(integrationTestService.getWorklist(worklist.getId(), authToken)
        .getWorkflowStatus() == WorkflowStatus.EDITING_DONE);

    // Assign for review
    workflowService.performWorkflowAction(projectId, worklist.getId(),
        authToken, UserRole.REVIEWER, WorkflowAction.ASSIGN, authToken);
    assertTrue(integrationTestService.getWorklist(worklist.getId(), authToken)
        .getWorkflowStatus() == WorkflowStatus.REVIEW_NEW);

    // Unassign for review
    workflowService.performWorkflowAction(projectId, worklist.getId(),
        authToken, UserRole.REVIEWER, WorkflowAction.UNASSIGN, authToken);
    assertTrue(integrationTestService.getWorklist(worklist.getId(), authToken)
        .getWorkflowStatus() == WorkflowStatus.EDITING_DONE);

    // Assign for review again
    workflowService.performWorkflowAction(projectId, worklist.getId(),
        authToken, UserRole.REVIEWER, WorkflowAction.ASSIGN, authToken);
    assertTrue(integrationTestService.getWorklist(worklist.getId(), authToken)
        .getWorkflowStatus() == WorkflowStatus.REVIEW_NEW);

    // Save
    workflowService.performWorkflowAction(projectId, worklist.getId(),
        authToken, UserRole.REVIEWER, WorkflowAction.SAVE, authToken);
    assertTrue(integrationTestService.getWorklist(worklist.getId(), authToken)
        .getWorkflowStatus() == WorkflowStatus.REVIEW_IN_PROGRESS);

    // Unassign for review
    workflowService.performWorkflowAction(projectId, worklist.getId(),
        authToken, UserRole.REVIEWER, WorkflowAction.UNASSIGN, authToken);
    assertTrue(integrationTestService.getWorklist(worklist.getId(), authToken)
        .getWorkflowStatus() == WorkflowStatus.EDITING_DONE);

    // Assign for review again
    workflowService.performWorkflowAction(projectId, worklist.getId(),
        authToken, UserRole.REVIEWER, WorkflowAction.ASSIGN, authToken);
    assertTrue(integrationTestService.getWorklist(worklist.getId(), authToken)
        .getWorkflowStatus() == WorkflowStatus.REVIEW_NEW);

    // Finish review
    workflowService.performWorkflowAction(projectId, worklist.getId(),
        authToken, UserRole.REVIEWER, WorkflowAction.FINISH, authToken);
    assertTrue(integrationTestService.getWorklist(worklist.getId(), authToken)
        .getWorkflowStatus() == WorkflowStatus.REVIEW_DONE);

    // Finalize work
    workflowService.performWorkflowAction(projectId, worklist.getId(),
        authToken, UserRole.REVIEWER, WorkflowAction.FINISH, authToken);
    assertTrue(integrationTestService.getWorklist(worklist.getId(), authToken)
        .getWorkflowStatus() == WorkflowStatus.READY_FOR_PUBLICATION);

    // clean up
    Logger.getLogger(getClass()).debug("  Remove worklist");
    workflowService.removeWorklist(projectId, worklist.getId(), authToken);

    // clear bins
    Logger.getLogger(getClass()).debug("  Clear bins");
    workflowService.clearBins(projectId, WorkflowBinType.MUTUALLY_EXCLUSIVE,
        authToken);

  }

  /**
   * Test generate/find/get/remove concept report.
   *
   * @throws Exception the exception
   */
  @Test
  public void testGeneratingConceptReport() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Clear bins
    Logger.getLogger(getClass()).debug("  Clear and regenerate bins");
    workflowService.clearBins(projectId, WorkflowBinType.MUTUALLY_EXCLUSIVE,
        authToken);

    // Regenerate bins
    workflowService.regenerateBins(projectId,
        WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);

    Logger.getLogger(getClass()).debug("  Find testName workflow bin");
    final List<WorkflowBin> binList =
        workflowService.getWorkflowBins(projectId,
            WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);
    WorkflowBin testNameBin = null;
    for (final WorkflowBin bin : binList) {
      if (bin.getName().equals("testName")) {
        testNameBin = bin;
        break;
      }
    }
    Logger.getLogger(getClass()).debug("    testNameBin = " + testNameBin);
    assertNotNull(testNameBin);

    //
    // Create worklist
    //
    Logger.getLogger(getClass()).debug("  Create worklists");
    final PfsParameterJpa pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(5);
    final Worklist worklist =
        workflowService.createWorklist(projectId, testNameBin.getId(), "chem",
            pfs, authToken);
    Logger.getLogger(getClass()).debug("    worklist = " + worklist);

    pfs.setStartIndex(5);
    pfs.setMaxResults(5);
    final Worklist worklist2 =
        workflowService.createWorklist(projectId, testNameBin.getId(), "chem",
            pfs, authToken);
    Logger.getLogger(getClass()).debug("    worklist2 = " + worklist2);

    // Generate concept report
    Logger.getLogger(getClass()).debug("  Generate concept reports");
    final String reportFileName =
        workflowService.generateConceptReport(projectId, worklist.getId(), 1L,
            false, "", 0, authToken);
    Logger.getLogger(getClass()).debug("    report = " + reportFileName);

    // Generate concept report
    final String reportFileName2 =
        workflowService.generateConceptReport(projectId, worklist2.getId(), 1L,
            false, "", 0, authToken);
    Logger.getLogger(getClass()).debug("    report2 = " + reportFileName2);

    // Find reports
    pfs.setStartIndex(0);
    pfs.setMaxResults(1);
    Logger.getLogger(getClass()).debug("  Find reports " + pfs);
    final StringList list =
        workflowService.findGeneratedConceptReports(projectId, ".txt", pfs,
            authToken);
    Logger.getLogger(getClass()).debug("    reports = " + list);
    assertEquals(1, list.getObjects().size());
    assertEquals(2, list.getTotalCount());
    boolean found = false;
    for (final String rpt : list.getObjects()) {
      if (rpt.equals(reportFileName)) {
        found = true;
        break;
      }
    }
    assertTrue(found);

    // Get the report
    Logger.getLogger(getClass()).debug("  Get the first report");
    final String report =
        workflowService.getGeneratedConceptReport(projectId, reportFileName,
            authToken);
    Logger.getLogger(getClass()).debug("    report = " + report);
    assertTrue(report.contains("ATOMS"));

    // Remove the report (and verify it is gone)
    Logger.getLogger(getClass()).debug("  Remove the reports");
    workflowService.removeGeneratedConceptReport(projectId, reportFileName,
        authToken);
    workflowService.removeGeneratedConceptReport(projectId, reportFileName2,
        authToken);
    final StringList list2 =
        workflowService.findGeneratedConceptReports(projectId, ".txt",
            new PfsParameterJpa(), authToken);
    found = false;
    for (final String rpt : list2.getObjects()) {
      if (rpt.equals(reportFileName)) {
        found = true;
        break;
      }
    }
    assertTrue(!found);

    // Remove worklist
    Logger.getLogger(getClass()).debug("  Remove worklists");
    workflowService.removeWorklist(projectId, worklist.getId(), authToken);
    workflowService.removeWorklist(projectId, worklist2.getId(), authToken);

    // Clear bins
    Logger.getLogger(getClass()).debug("  Clear bins");
    workflowService.clearBins(projectId, WorkflowBinType.MUTUALLY_EXCLUSIVE,
        authToken);

  }

  /**
   * Test worklist and workflow bin statistics.
   *
   * @throws Exception the exception
   */
  @Test
  public void testWorkflowStatistics() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Clear bins
    Logger.getLogger(getClass()).debug("  Clear and regenerate bins");
    workflowService.clearBins(projectId, WorkflowBinType.MUTUALLY_EXCLUSIVE,
        authToken);

    // Regenerate bins
    workflowService.regenerateBins(projectId,
        WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);

    Logger.getLogger(getClass()).debug("  Find testName workflow bin");
    final List<WorkflowBin> binList =
        workflowService.getWorkflowBins(projectId,
            WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);
    WorkflowBin testNameBin = null;
    for (final WorkflowBin bin : binList) {
      if (bin.getName().equals("testName")) {
        testNameBin = bin;
        break;
      }
    }
    Logger.getLogger(getClass()).debug("    testNameBin = " + testNameBin);
    assertNotNull(testNameBin);

    //
    // Create worklist
    //
    Logger.getLogger(getClass()).debug("  Create worklist");
    final PfsParameterJpa pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(5);
    final Worklist worklist =
        workflowService.createWorklist(projectId, testNameBin.getId(), "chem",
            pfs, authToken);
    Logger.getLogger(getClass()).debug("    worklist = " + worklist);

    // Get workflow bins
    Logger.getLogger(getClass()).debug("  Get workflow bins");
    final List<WorkflowBin> list =
        workflowService.getWorkflowBins(projectId,
            WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);
    Logger.getLogger(getClass()).debug("    list = " + list);
    // TODO: test stats (editable/uneditable)

    Logger.getLogger(getClass()).debug("  Get worklist with stats");
    final Worklist worklist2 =
        workflowService.getWorklist(projectId, worklist.getId(), authToken);
    Logger.getLogger(getClass()).debug("    worklist = " + worklist2);
    // TODO: test stats

    // Remove worklist
    Logger.getLogger(getClass()).debug("  Remove worklist");
    workflowService.removeWorklist(projectId, worklist.getId(), authToken);

    // clear bins
    Logger.getLogger(getClass()).debug("  Clear bins");
    workflowService.clearBins(projectId, WorkflowBinType.MUTUALLY_EXCLUSIVE,
        authToken);

  }

  /**
   * Teardown.
   *
   * @throws Exception the exception
   */
  @Override
  @After
  public void teardown() throws Exception {

    workflowService.removeWorkflowBinDefinition(projectId, definition.getId(),
        authToken);
    workflowService.removeWorkflowConfig(projectId, config.getId(), authToken);
    workflowService.removeWorkflowEpoch(projectId, epoch.getId(), authToken);
    projectService.removeProject(projectId, authToken);
    // logout
    securityService.logout(authToken);
  }

  /**
   * Returns the semantic type category map.
   *
   * @return the semantic type category map
   * @throws Exception the exception
   */
  private Map<String, String> getSemanticTypeCategoryMap() throws Exception {
    final Map<String, String> map = new HashMap<>();
    final MetadataService service = new MetadataServiceJpa();
    try {
      final SemanticTypeList styList =
          service.getSemanticTypes(umlsTerminology, umlsVersion);

      // Obtain "Chemical" semantic type.
      String chemStn = null;
      for (final SemanticType sty : styList.getObjects()) {
        if (sty.getExpandedForm().equals("Chemical")) {
          chemStn = sty.getTreeNumber();
          break;
        }
      }
      if (chemStn == null) {
        throw new Exception("Unable to find 'Chemical' semantic type");
      }

      // Assign "chem" categories
      for (final SemanticType sty : styList.getObjects()) {
        if (sty.getTreeNumber().startsWith(chemStn)) {
          map.put(sty.getExpandedForm(), "chem");
        }
        // the default is not explicitly rendered
        // else {
        // map.put(sty.getExpandedForm(), "nonchem");
        // }
      }

    } catch (Exception e) {
      throw e;
    } finally {
      service.close();
    }
    return map;
  }

}
