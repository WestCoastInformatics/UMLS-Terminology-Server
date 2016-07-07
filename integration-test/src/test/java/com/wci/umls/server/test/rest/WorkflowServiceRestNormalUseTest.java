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
import com.wci.umls.server.helpers.WorklistList;
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
    final WorkflowConfigJpa config = new WorkflowConfigJpa();
    config.setType(WorkflowBinType.MUTUALLY_EXCLUSIVE);
    config.setMutuallyExclusive(true);
    config.setProjectId(projectId);

    // Add workflow config
    WorkflowConfig newConfig =
        workflowService.addWorkflowConfig(projectId, config, authToken);
    assertEquals(WorkflowBinType.MUTUALLY_EXCLUSIVE, newConfig.getType());
    assertTrue(newConfig.isMutuallyExclusive());
    assertEquals(adminUser, newConfig.getLastModifiedBy());
    assertEquals(projectId, newConfig.getProject().getId());

    // Update workflow config
    newConfig.setType(WorkflowBinType.QUALITY_ASSURANCE);
    newConfig.setMutuallyExclusive(false);
    workflowService.updateWorkflowConfig(projectId,
        (WorkflowConfigJpa) newConfig, authToken);
    newConfig =
        workflowService.getWorkflowConfig(projectId, newConfig.getId(),
            authToken);
    assertEquals(WorkflowBinType.QUALITY_ASSURANCE, newConfig.getType());
    assertFalse(newConfig.isMutuallyExclusive());

    // Remove the workflow config
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

    // Create workflow config
    final WorkflowConfigJpa config = new WorkflowConfigJpa();
    config.setType(WorkflowBinType.MUTUALLY_EXCLUSIVE);
    config.setMutuallyExclusive(true);
    config.setProjectId(projectId);

    // Add workflow config
    final WorkflowConfig newConfig =
        workflowService.addWorkflowConfig(projectId, config, authToken);

    // Create workflow bin definition
    final WorkflowBinDefinitionJpa definition = new WorkflowBinDefinitionJpa();
    definition.setName("test name");
    definition.setDescription("test description");
    definition.setQuery("select * from concepts");
    definition.setEditable(true);
    definition.setQueryType(QueryType.SQL);
    definition.setWorkflowConfig(newConfig);

    // Add workflow bin definition
    WorkflowBinDefinition newDefinition =
        workflowService.addWorkflowBinDefinition(projectId, definition,
            authToken);
    assertEquals("test name", newDefinition.getName());
    assertEquals("test description", newDefinition.getDescription());
    assertEquals("select * from concepts", newDefinition.getQuery());
    assertTrue(newDefinition.isEditable());
    assertEquals(QueryType.SQL, newDefinition.getQueryType());
    assertEquals(newConfig.getId(), newDefinition.getWorkflowConfig().getId());

    // Update workflow bin definition
    newDefinition.setEditable(false);
    newDefinition.setDescription("test description2");
    workflowService.updateWorkflowBinDefinition(projectId,
        (WorkflowBinDefinitionJpa) newDefinition, authToken);
    newDefinition =
        workflowService.getWorkflowBinDefinition(projectId,
            newDefinition.getId(), authToken);
    assertFalse(newDefinition.isEditable());
    assertEquals("test description2", newDefinition.getDescription());

    // Remove workflow bin definition
    workflowService.removeWorkflowBinDefinition(projectId,
        newDefinition.getId(), authToken);

    // Remove workflow config
    workflowService.removeWorkflowConfig(projectId, newConfig.getId(),
        authToken);

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
    workflowService.clearBins(projectId, WorkflowBinType.MUTUALLY_EXCLUSIVE,
        authToken);

    // Regenerate gins
    workflowService.regenerateBins(projectId,
        WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);

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
    assertNotNull(testNameBin);

    //
    // Create checklist with cluster id order tracking records
    //
    final PfsParameterJpa pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(5);
    pfs.setSortField("clusterId");

    // Non-randomize flag picks consecutive tracking records from the bin
    final Checklist checklistOrderByClusterId =
        workflowService.createChecklist(projectId, testNameBin.getId(),
            "checklistOrderByClusterId", false, false, null, pfs, authToken);
    // Assert that cluster ids are contiguous and in order
    long i = 0L;
    for (final TrackingRecord r : workflowService
        .findTrackingRecordsForChecklist(projectId,
            checklistOrderByClusterId.getId(), pfs, authToken).getObjects()) {
      assertEquals(++i, r.getClusterId().longValue());
    }

    // Remove checklist
    workflowService.removeChecklist(projectId,
        checklistOrderByClusterId.getId(), authToken);

    //
    // Create checklist with random tracking records
    //
    // Randomize flag picks random tracking records from the bin
    final Checklist checklistOrderByRandom =
        workflowService.createChecklist(projectId, testNameBin.getId(),
            "checklistOrderByRandom", true, false, null, pfs, authToken);
    workflowService.clearBins(projectId, WorkflowBinType.MUTUALLY_EXCLUSIVE,
        authToken);
    // Assert that cluster ids are contiguous and in order
    boolean found = false;
    for (final TrackingRecord r : workflowService
        .findTrackingRecordsForChecklist(projectId,
            checklistOrderByRandom.getId(), pfs, authToken).getObjects()) {
      if (r.getClusterId() > 5) {
        found = true;
        break;
      }
    }
    assertTrue("Expected at least one cluster id not in the first 5", found);

    // TODO: test clusterType:chem
    // TODO: test excludeOnWorklist...

    // Remove checklist
    workflowService.removeChecklist(projectId, checklistOrderByRandom.getId(),
        authToken);
    // Clear bins
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
    workflowService.clearBins(projectId, WorkflowBinType.MUTUALLY_EXCLUSIVE,
        authToken);

    // regenerate bins
    workflowService.regenerateBins(projectId,
        WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);

    // get test name bin
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
    assertNotNull(testNameBin);

    // Remove any worklists first
    final WorklistList worklists =
        workflowService.findWorklists(projectId, "projectId:" + projectId,
            null, authToken);
    for (final Worklist worklist : worklists.getObjects()) {
      workflowService.removeWorklist(projectId, worklist.getId(), authToken);
    }

    //
    // Create worklist
    //
    final Worklist worklist =
        workflowService.createWorklist(projectId, testNameBin.getId(), "chem",
            0, 5, new PfsParameterJpa(), authToken);

    // TODO: assert something about this

    // Remove the worklist
    workflowService.removeWorklist(projectId, worklist.getId(), authToken);

    // clear bins
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
    workflowService.clearBins(projectId, WorkflowBinType.MUTUALLY_EXCLUSIVE,
        authToken);

    // Regenerate bins
    workflowService.regenerateBins(projectId,
        WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);

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
    assertNotNull(testNameBin);

    // Remove any worklists first
    final WorklistList worklists =
        workflowService.findWorklists(projectId, "projectId:" + projectId,
            null, authToken);
    for (final Worklist worklist : worklists.getObjects()) {
      workflowService.removeWorklist(projectId, worklist.getId(), authToken);
    }

    //
    // Create worklist
    //
    final Worklist addedWorklist =
        workflowService.createWorklist(projectId, testNameBin.getId(), "chem",
            0, 5, new PfsParameterJpa(), authToken);

    //
    // Test perform workflow action
    //

    // Assign
    workflowService.performWorkflowAction(projectId, addedWorklist.getId(),
        authToken, UserRole.AUTHOR, WorkflowAction.ASSIGN, authToken);
    assertTrue(integrationTestService.getWorklist(addedWorklist.getId(),
        authToken).getWorkflowStatus() == WorkflowStatus.NEW);

    // Unassign
    workflowService.performWorkflowAction(projectId, addedWorklist.getId(),
        authToken, UserRole.AUTHOR, WorkflowAction.UNASSIGN, authToken);
    assertTrue(integrationTestService.getWorklist(addedWorklist.getId(),
        authToken).getWorkflowStatus() == WorkflowStatus.NEW);

    // Assign again
    workflowService.performWorkflowAction(projectId, addedWorklist.getId(),
        authToken, UserRole.AUTHOR, WorkflowAction.ASSIGN, authToken);
    assertTrue(integrationTestService.getWorklist(addedWorklist.getId(),
        authToken).getWorkflowStatus() == WorkflowStatus.NEW);

    // Save
    workflowService.performWorkflowAction(projectId, addedWorklist.getId(),
        authToken, UserRole.AUTHOR, WorkflowAction.SAVE, authToken);
    assertTrue(integrationTestService.getWorklist(addedWorklist.getId(),
        authToken).getWorkflowStatus() == WorkflowStatus.EDITING_IN_PROGRESS);

    // Unassign
    workflowService.performWorkflowAction(projectId, addedWorklist.getId(),
        authToken, UserRole.AUTHOR, WorkflowAction.UNASSIGN, authToken);
    assertTrue(integrationTestService.getWorklist(addedWorklist.getId(),
        authToken).getWorkflowStatus() == WorkflowStatus.NEW);

    // Assign again
    workflowService.performWorkflowAction(projectId, addedWorklist.getId(),
        authToken, UserRole.AUTHOR, WorkflowAction.ASSIGN, authToken);
    assertTrue(integrationTestService.getWorklist(addedWorklist.getId(),
        authToken).getWorkflowStatus() == WorkflowStatus.NEW);

    // Finish
    workflowService.performWorkflowAction(projectId, addedWorklist.getId(),
        authToken, UserRole.AUTHOR, WorkflowAction.FINISH, authToken);
    assertTrue(integrationTestService.getWorklist(addedWorklist.getId(),
        authToken).getWorkflowStatus() == WorkflowStatus.EDITING_DONE);

    // Assign for review
    workflowService.performWorkflowAction(projectId, addedWorklist.getId(),
        authToken, UserRole.REVIEWER, WorkflowAction.ASSIGN, authToken);
    assertTrue(integrationTestService.getWorklist(addedWorklist.getId(),
        authToken).getWorkflowStatus() == WorkflowStatus.REVIEW_NEW);

    // Unassign for review
    workflowService.performWorkflowAction(projectId, addedWorklist.getId(),
        authToken, UserRole.REVIEWER, WorkflowAction.UNASSIGN, authToken);
    assertTrue(integrationTestService.getWorklist(addedWorklist.getId(),
        authToken).getWorkflowStatus() == WorkflowStatus.EDITING_DONE);

    // Assign for review again
    workflowService.performWorkflowAction(projectId, addedWorklist.getId(),
        authToken, UserRole.REVIEWER, WorkflowAction.ASSIGN, authToken);
    assertTrue(integrationTestService.getWorklist(addedWorklist.getId(),
        authToken).getWorkflowStatus() == WorkflowStatus.REVIEW_NEW);

    // Save
    workflowService.performWorkflowAction(projectId, addedWorklist.getId(),
        authToken, UserRole.REVIEWER, WorkflowAction.SAVE, authToken);
    assertTrue(integrationTestService.getWorklist(addedWorklist.getId(),
        authToken).getWorkflowStatus() == WorkflowStatus.REVIEW_IN_PROGRESS);

    // Unassign for review
    workflowService.performWorkflowAction(projectId, addedWorklist.getId(),
        authToken, UserRole.REVIEWER, WorkflowAction.UNASSIGN, authToken);
    assertTrue(integrationTestService.getWorklist(addedWorklist.getId(),
        authToken).getWorkflowStatus() == WorkflowStatus.EDITING_DONE);

    // Assign for review again
    workflowService.performWorkflowAction(projectId, addedWorklist.getId(),
        authToken, UserRole.REVIEWER, WorkflowAction.ASSIGN, authToken);
    assertTrue(integrationTestService.getWorklist(addedWorklist.getId(),
        authToken).getWorkflowStatus() == WorkflowStatus.REVIEW_NEW);

    // Finish review
    workflowService.performWorkflowAction(projectId, addedWorklist.getId(),
        authToken, UserRole.REVIEWER, WorkflowAction.FINISH, authToken);
    assertTrue(integrationTestService.getWorklist(addedWorklist.getId(),
        authToken).getWorkflowStatus() == WorkflowStatus.REVIEW_DONE);

    // Finalize work
    workflowService.performWorkflowAction(projectId, addedWorklist.getId(),
        authToken, UserRole.REVIEWER, WorkflowAction.FINISH, authToken);
    assertTrue(integrationTestService.getWorklist(addedWorklist.getId(),
        authToken).getWorkflowStatus() == WorkflowStatus.READY_FOR_PUBLICATION);

    // clean up
    workflowService.removeWorklist(projectId, addedWorklist.getId(), authToken);

    // clear bins
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
    workflowService.clearBins(projectId, WorkflowBinType.MUTUALLY_EXCLUSIVE,
        authToken);

    // Regenerate bins
    workflowService.regenerateBins(projectId,
        WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);

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
    assertNotNull(testNameBin);

    // Remove any worklists first
    final WorklistList worklists =
        workflowService.findWorklists(projectId, "projectId:" + projectId,
            null, authToken);
    for (Worklist worklist : worklists.getObjects()) {
      workflowService.removeWorklist(projectId, worklist.getId(), authToken);
    }

    //
    // Create worklist
    //
    final Worklist worklist =
        workflowService.createWorklist(projectId, testNameBin.getId(), "chem",
            0, 5, new PfsParameterJpa(), authToken);

    // Generate concept report
    String reportFileName =
        workflowService.generateConceptReport(projectId, worklist.getId(), 1L,
            false, "", 0, authToken);

    // Obtain the report
    String reportContents =
        workflowService.getGeneratedConceptReport(projectId, reportFileName,
            authToken);
    assertTrue(reportContents.contains("ATOMS"));

    // Find the report
    final StringList list =
        workflowService.findGeneratedConceptReports(projectId, ".txt",
            new PfsParameterJpa(), authToken);
    boolean found = false;
    for (final String rpt : list.getObjects()) {
      if (rpt.equals(reportFileName)) {
        found = true;
        break;
      }
    }
    assertTrue(found);

    // Remove the report (and verify it is gone)
    workflowService.removeGeneratedConceptReport(projectId, reportFileName,
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
    workflowService.removeWorklist(projectId, worklist.getId(), authToken);

    // Clear bins
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
    workflowService.clearBins(projectId, WorkflowBinType.MUTUALLY_EXCLUSIVE,
        authToken);

    // Regenerate bins
    workflowService.regenerateBins(projectId,
        WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);

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
    assertNotNull(testNameBin);

    // Remove any worklists first
    final WorklistList worklists =
        workflowService.findWorklists(projectId, "projectId:" + projectId,
            null, authToken);
    for (final Worklist worklist : worklists.getObjects()) {
      workflowService.removeWorklist(projectId, worklist.getId(), authToken);
    }

    //
    // Create worklist
    //
    final Worklist worklist =
        workflowService.createWorklist(projectId, testNameBin.getId(), "chem",
            0, 5, new PfsParameterJpa(), authToken);

    // Get workflow bins
    final List<WorkflowBin> list =
        workflowService.getWorkflowBins(projectId,
            WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);
    // TODO: test stats (editable/uneditable)

    final Worklist worklist2 =
        workflowService.getWorklist(projectId, worklist.getId(), authToken);
    // TODO: test stats

    // Remove worklist
    workflowService.removeWorklist(projectId, worklist.getId(), authToken);

    // clear bins
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
