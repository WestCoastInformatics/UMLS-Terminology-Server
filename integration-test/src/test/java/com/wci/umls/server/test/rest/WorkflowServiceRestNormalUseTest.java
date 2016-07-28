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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.wci.umls.server.Project;
import com.wci.umls.server.User;
import com.wci.umls.server.UserRole;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.QueryType;
import com.wci.umls.server.helpers.StringList;
import com.wci.umls.server.helpers.TrackingRecordList;
import com.wci.umls.server.helpers.WorkflowBinList;
import com.wci.umls.server.helpers.meta.SemanticTypeList;
import com.wci.umls.server.jpa.ProjectJpa;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.services.rest.MetadataServiceRest;
import com.wci.umls.server.jpa.worfklow.WorkflowBinDefinitionJpa;
import com.wci.umls.server.jpa.worfklow.WorkflowConfigJpa;
import com.wci.umls.server.jpa.worfklow.WorkflowEpochJpa;
import com.wci.umls.server.model.meta.SemanticType;
import com.wci.umls.server.model.workflow.Checklist;
import com.wci.umls.server.model.workflow.TrackingRecord;
import com.wci.umls.server.model.workflow.WorkflowAction;
import com.wci.umls.server.model.workflow.WorkflowBin;
import com.wci.umls.server.model.workflow.WorkflowBinDefinition;
import com.wci.umls.server.model.workflow.WorkflowBinType;
import com.wci.umls.server.model.workflow.WorkflowConfig;
import com.wci.umls.server.model.workflow.WorkflowEpoch;
import com.wci.umls.server.model.workflow.WorkflowStatus;
import com.wci.umls.server.model.workflow.Worklist;
import com.wci.umls.server.rest.client.MetadataClientRest;

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
    epoch = workflowService.addWorkflowEpoch(project.getId(),
        (WorkflowEpochJpa) epoch, authToken);

    // Create a workflow config
    config = new WorkflowConfigJpa();
    config.setType(WorkflowBinType.MUTUALLY_EXCLUSIVE);
    config.setMutuallyExclusive(true);
    config.setProject(project);
    config = workflowService.addWorkflowConfig(projectId,
        (WorkflowConfigJpa) config, authToken);

    // Add a workflow definition (as SQL)
    definition = new WorkflowBinDefinitionJpa();
    definition.setName("testName");
    definition.setDescription("test description");
    definition.setQuery(
        "select distinct c.id clusterId, c.id conceptId from concepts c where c.name like '%Amino%';");
    definition.setEditable(true);
    definition.setEnabled(true);
    definition.setQueryType(QueryType.SQL);
    definition.setWorkflowConfig(config);
    definition = workflowService.addWorkflowBinDefinition(projectId, null,
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
    newConfig = workflowService.getWorkflowConfig(projectId, newConfig.getId(),
        authToken);
    Logger.getLogger(getClass()).debug("    config = " + config);
    assertFalse(newConfig.isMutuallyExclusive());

    // Remove the workflow config
    Logger.getLogger(getClass()).debug("  Remove workflow config");
    workflowService.removeWorkflowConfig(projectId, newConfig.getId(),
        authToken);
    WorkflowConfig config2 = workflowService.getWorkflowConfig(projectId,
        newConfig.getId(), authToken);
    assertNull(config2);

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
    definition.setQuery("select a.id as conceptId from concepts a");
    definition.setEditable(true);
    definition.setEnabled(true);
    definition.setQueryType(QueryType.SQL);
    definition.setWorkflowConfig(config);

    // Add workflow bin definition
    WorkflowBinDefinition newDefinition = workflowService
        .addWorkflowBinDefinition(projectId, null, definition, authToken);
    Logger.getLogger(getClass()).debug("    definition = " + newDefinition);
    assertEquals("test name", newDefinition.getName());
    assertEquals("test description", newDefinition.getDescription());
    assertEquals("select a.id as conceptId from concepts a",
        newDefinition.getQuery());
    assertTrue(newDefinition.isEditable());
    assertEquals(QueryType.SQL, newDefinition.getQueryType());
    assertEquals(config.getId(), newDefinition.getWorkflowConfig().getId());

    // Update workflow bin definition
    Logger.getLogger(getClass()).debug("  Update workflow bin definition");
    newDefinition.setEditable(false);
    newDefinition.setEnabled(true);
    newDefinition.setDescription("test description2");
    workflowService.updateWorkflowBinDefinition(projectId,
        (WorkflowBinDefinitionJpa) newDefinition, authToken);
    newDefinition = workflowService.getWorkflowBinDefinition(projectId,
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

    // Add a required SQL bin definition CLUSTER_CONCEPT
    Logger.getLogger(getClass())
        .info("  Add required SQL bin definition - cluster/concept");
    WorkflowBinDefinitionJpa defn = new WorkflowBinDefinitionJpa();
    defn.setName("testSQL - cluster,concept");
    defn.setDescription("Test SQL.");
    defn.setQuery("select a.id clusterId, a.id conceptId "
        + "from concepts a, concepts_atoms b, atoms c "
        + "where a.id = b.concepts_id " + "  and b.atoms_id = c.id  "
        + "  and a.terminology = :terminology and c.terminology='NCI' "
        + "  and c.workflowStatus = 'NEEDS_REVIEW'");
    defn.setEditable(true);
    defn.setEnabled(true);
    defn.setRequired(true);
    defn.setQueryType(QueryType.SQL);
    defn.setWorkflowConfig(config);
    defn = (WorkflowBinDefinitionJpa) workflowService
        .addWorkflowBinDefinition(projectId, null,defn, authToken);

    // Add a required SQL bin definition CONCEPT CONCEPT
    Logger.getLogger(getClass())
        .info("  Add required SQL bin definition - cid1,2");
    defn = new WorkflowBinDefinitionJpa();
    defn.setName("testSQL - concept,concept");
    defn.setDescription("Test SQL.");
    defn.setQuery("select a.from_id conceptId1, a.to_id conceptId2 "
        + "from concept_relationships a, concepts b "
        + "where a.from_id = b.id "
        + "  and b.terminologyId between  'C0000000' and 'C0000500' "
        + "  and a.terminology = :terminology");
    defn.setEditable(true);
    defn.setEnabled(true);
    defn.setRequired(true);
    defn.setQueryType(QueryType.SQL);
    defn.setWorkflowConfig(config);
    defn = (WorkflowBinDefinitionJpa) workflowService
        .addWorkflowBinDefinition(projectId, null,defn, authToken);

    // Add a required SQL bin definition CONCEPT CONCEPT
    Logger.getLogger(getClass())
        .info("  Add required SQL bin definition - cid");
    defn = new WorkflowBinDefinitionJpa();
    defn.setName("testSQL - concept");
    defn.setDescription("Test SQL.");
    defn.setQuery("select a.from_id conceptId "
        + "from concept_relationships a, concepts b "
        + "where a.from_id = b.id "
        + "  and b.terminologyId between 'C0000000' and 'C0000500' "
        + "  and a.terminology = :terminology");
    defn.setEditable(true);
    defn.setEnabled(true);
    defn.setRequired(true);
    defn.setQueryType(QueryType.SQL);
    defn.setWorkflowConfig(config);
    defn = (WorkflowBinDefinitionJpa) workflowService
        .addWorkflowBinDefinition(projectId, null,defn, authToken);

    // Add a disabled SQL bin
    Logger.getLogger(getClass()).info("  Add disabled SQL bin definition");
    defn = new WorkflowBinDefinitionJpa();
    defn.setName("testSQL - DISABLED");
    defn.setDescription("Test SQL.");
    defn.setQuery("select a.from_id conceptId "
        + "from concept_relationships a, concepts b "
        + "where a.from_id = b.id "
        + "  and b.terminologyId between 'C0000000' and 'C0000500' "
        + "  and a.terminology = :terminology");
    defn.setEditable(true);
    defn.setEnabled(false);
    defn.setRequired(true);
    defn.setQueryType(QueryType.SQL);
    defn.setWorkflowConfig(config);
    defn = (WorkflowBinDefinitionJpa) workflowService
        .addWorkflowBinDefinition(projectId, null,defn, authToken);

    // Add a required JQL bin definition
    Logger.getLogger(getClass())
        .info("  Add required JQL bin definition - cid");
    defn = new WorkflowBinDefinitionJpa();
    defn.setName("testJQL");
    defn.setDescription("Test JQL.");
    defn.setQuery("select a.id as conceptId from ConceptJpa a "
        + "where a.terminology = :terminology "
        + "  and a.workflowStatus = 'NEEDS_REVIEW'");
    defn.setEditable(true);
    defn.setEnabled(true);
    defn.setRequired(true);
    defn.setQueryType(QueryType.JQL);
    defn.setWorkflowConfig(config);
    defn = (WorkflowBinDefinitionJpa) workflowService
        .addWorkflowBinDefinition(projectId, null,defn, authToken);

    // Add a required LUCENE bin definition
    Logger.getLogger(getClass()).info("  Add required LUCENE bin definition");
    defn = new WorkflowBinDefinitionJpa();
    defn.setName("testLUCENE");
    defn.setDescription("Test LUCENE.");
    defn.setQuery("atoms.terminology:AIR");
    defn.setEditable(true);
    defn.setEnabled(true);
    defn.setRequired(true);
    defn.setQueryType(QueryType.LUCENE);
    defn.setWorkflowConfig(config);
    defn = (WorkflowBinDefinitionJpa) workflowService
        .addWorkflowBinDefinition(projectId, null,defn, authToken);

    // Regenerate bins
    Logger.getLogger(getClass()).info("  Regenerate bins");
    workflowService.regenerateBins(projectId,
        WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);
    final WorkflowBinList binList = workflowService.getWorkflowBins(projectId,
        WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);
    assertEquals(7, binList.size());

    for (final WorkflowBin bin : binList.getObjects()) {
      Logger.getLogger(getClass())
          .debug("    bin = " + bin.getName() + ", " + bin.getClusterCt());
      final PfsParameterJpa pfs = new PfsParameterJpa();
      pfs.setStartIndex(0);
      pfs.setMaxResults(10);
      final TrackingRecordList list =
          workflowService.findTrackingRecordsForWorkflowBin(projectId,
              bin.getId(), pfs, authToken);
      Logger.getLogger(getClass()).debug("    records = " + list.size());
      if (!bin.getName().equals("testSQL - DISABLED")) {
        assertTrue(bin.getClusterCt() > 0);
        assertTrue(list.size() > 0);
      } else {
        assertEquals(0, bin.getClusterCt());
        assertEquals(0, list.size());
      }
    }

    // Clear bins
    Logger.getLogger(getClass()).debug("  Clear bins");
    workflowService.clearBins(projectId, WorkflowBinType.MUTUALLY_EXCLUSIVE,
        authToken);
    final WorkflowBinList binList2 = workflowService.getWorkflowBins(projectId,
        WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);
    assertEquals(0, binList2.size());

    // Remove the definition
    Logger.getLogger(getClass()).debug("Remove workflow bin definitions");
    for (final WorkflowBinDefinition def : workflowService
        .getWorkflowConfig(projectId, config.getId(), authToken)
        .getWorkflowBinDefinitions()) {
      // Keep the definition created by "setup"
      if (!def.getId().equals(definition.getId())) {
        Logger.getLogger(getClass()).debug("  def = " + def.getName());
        workflowService.removeWorkflowBinDefinition(projectId, def.getId(),
            authToken);
      }
    }

  }

  /**
   * Test regenerating a mutually exclusive bin.
   *
   * @throws Exception the exception
   */
  @Test
  public void testMutuallyExclusive() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Add a required SQL bin definition
    Logger.getLogger(getClass()).info("    Add required SQL bin definition");
    WorkflowBinDefinitionJpa definition = new WorkflowBinDefinitionJpa();
    definition.setName("testSQL");
    definition.setDescription("Test SQL.");
    definition.setQuery("select a.id clusterId, a.id conceptId "
        + "from concepts a, concepts_atoms b, atoms c "
        + "where a.id = b.concepts_id " + "  and b.atoms_id = c.id  "
        + "  and a.terminology = :terminology and c.terminology='NCI' "
        + "  and c.workflowStatus = 'NEEDS_REVIEW'");
    definition.setEditable(true);
    definition.setEnabled(true);
    definition.setRequired(true);
    definition.setQueryType(QueryType.SQL);
    definition.setWorkflowConfig(config);
    definition = (WorkflowBinDefinitionJpa) workflowService
        .addWorkflowBinDefinition(projectId, null,definition, authToken);

    // Add same SQL definition
    Logger.getLogger(getClass()).info("    Add same SQL definition");
    WorkflowBinDefinitionJpa definition2 = new WorkflowBinDefinitionJpa();
    definition2.setName("testSQL2");
    definition2.setDescription("Test SQL2.");
    definition2.setQuery("select a.id clusterId, a.id conceptId "
        + "from concepts a, concepts_atoms b, atoms c "
        + "where a.id = b.concepts_id " + "  and b.atoms_id = c.id  "
        + "  and a.terminology = :terminology and c.terminology='NCI' "
        + "  and c.workflowStatus = 'NEEDS_REVIEW'");
    definition2.setEditable(true);
    definition2.setEnabled(true);
    definition2.setRequired(true);
    definition2.setQueryType(QueryType.SQL);
    definition2.setWorkflowConfig(config);
    definition2 = (WorkflowBinDefinitionJpa) workflowService
        .addWorkflowBinDefinition(projectId, null,definition2, authToken);

    // Regenerate bins
    workflowService.regenerateBins(projectId,
        WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);
    final WorkflowBinList binList = workflowService.getWorkflowBins(projectId,
        WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);
    assertEquals(3, binList.size());

    int testSqlCt = -1;
    int testSql2Ct = -1;
    for (final WorkflowBin bin : binList.getObjects()) {
      if (bin.getName().equals("testSQL")) {
        testSqlCt = bin.getClusterCt();
      }
      if (bin.getName().equals("testSQL2")) {
        testSql2Ct = bin.getClusterCt();
      }
    }
    assertTrue(testSqlCt != testSql2Ct);
    assertEquals(0, testSql2Ct);

    // Clear bins
    Logger.getLogger(getClass()).debug("  Clear and regenerate bins");
    workflowService.clearBins(projectId, WorkflowBinType.MUTUALLY_EXCLUSIVE,
        authToken);
    final WorkflowBinList binList2 = workflowService.getWorkflowBins(projectId,
        WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);
    assertEquals(0, binList2.size());

    // Remove the definition
    workflowService.removeWorkflowBinDefinition(projectId, definition.getId(),
        authToken);
    workflowService.removeWorkflowBinDefinition(projectId, definition2.getId(),
        authToken);

  }

  /**
   * Test regenerating a non-mutually exclusive bin.
   *
   * @throws Exception the exception
   */
  @Test
  public void testNotMutuallyExclusive() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Update workflow config to be not mutually exclusive
    Logger.getLogger(getClass()).debug("  Update workflow config");
    config.setMutuallyExclusive(false);
    workflowService.updateWorkflowConfig(projectId, (WorkflowConfigJpa) config,
        authToken);
    config =
        workflowService.getWorkflowConfig(projectId, config.getId(), authToken);
    assertFalse(config.isMutuallyExclusive());

    // Add a required SQL bin definition
    Logger.getLogger(getClass()).info("    Add required SQL bin definition");
    WorkflowBinDefinitionJpa definition = new WorkflowBinDefinitionJpa();
    definition.setName("testSQL");
    definition.setDescription("Test SQL.");
    definition.setQuery("select a.id clusterId, a.id conceptId "
        + "from concepts a, concepts_atoms b, atoms c "
        + "where a.id = b.concepts_id " + "  and b.atoms_id = c.id  "
        + "  and a.terminology = :terminology and c.terminology='NCI' "
        + "  and c.workflowStatus = 'NEEDS_REVIEW'");
    definition.setEditable(true);
    definition.setEnabled(true);

    definition.setRequired(true);
    definition.setQueryType(QueryType.SQL);
    definition.setWorkflowConfig(config);
    definition = (WorkflowBinDefinitionJpa) workflowService
        .addWorkflowBinDefinition(projectId, null,definition, authToken);

    // Add same SQL definition
    Logger.getLogger(getClass()).info("    Add same SQL definition");
    WorkflowBinDefinitionJpa definition2 = new WorkflowBinDefinitionJpa();
    definition2.setName("testSQL2");
    definition2.setDescription("Test SQL2.");
    definition2.setQuery("select a.id clusterId, a.id conceptId "
        + "from concepts a, concepts_atoms b, atoms c "
        + "where a.id = b.concepts_id " + "  and b.atoms_id = c.id  "
        + "  and a.terminology = :terminology and c.terminology='NCI' "
        + "  and c.workflowStatus = 'NEEDS_REVIEW'");
    definition2.setEditable(true);
    definition2.setEnabled(true);
    definition2.setRequired(true);
    definition2.setQueryType(QueryType.SQL);
    definition2.setWorkflowConfig(config);
    definition2 = (WorkflowBinDefinitionJpa) workflowService
        .addWorkflowBinDefinition(projectId, null,definition2, authToken);

    // Regenerate bins
    workflowService.regenerateBins(projectId,
        WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);
    final WorkflowBinList binList = workflowService.getWorkflowBins(projectId,
        WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);
    assertEquals(3, binList.size());

    int testSqlCt = -1;
    int testSql2Ct = -1;
    for (final WorkflowBin bin : binList.getObjects()) {
      if (bin.getName().equals("testSQL")) {
        testSqlCt = bin.getClusterCt();
      }
      if (bin.getName().equals("testSQL2")) {
        testSql2Ct = bin.getClusterCt();
      }
    }
    assertEquals(testSqlCt, testSql2Ct);

    // Clear bins
    Logger.getLogger(getClass()).debug("  Clear and regenerate bins");
    workflowService.clearBins(projectId, WorkflowBinType.MUTUALLY_EXCLUSIVE,
        authToken);
    final WorkflowBinList binList2 = workflowService.getWorkflowBins(projectId,
        WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);
    assertEquals(0, binList2.size());

    // Remove the definition
    workflowService.removeWorkflowBinDefinition(projectId, definition.getId(),
        authToken);
    workflowService.removeWorkflowBinDefinition(projectId, definition2.getId(),
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

    // Regenerate gins
    workflowService.regenerateBins(projectId,
        WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);

    Logger.getLogger(getClass()).debug("  Find testName workflow bin");
    final WorkflowBinList binList = workflowService.getWorkflowBins(projectId,
        WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);
    WorkflowBin testNameBin = null;
    for (final WorkflowBin bin : binList.getObjects()) {
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
    Logger.getLogger(getClass())
        .debug("  Create checklist in cluster id order");
    final Checklist checklistOrderByClusterId = workflowService.createChecklist(
        projectId, testNameBin.getId(), null, "checklistOrderByClusterId",
        "testDescription", false, false, null, pfs, authToken);
    Logger.getLogger(getClass())
        .debug("    checklist = " + checklistOrderByClusterId);
    // Assert that cluster ids are contiguous and in order
    int i = 0;
    for (final TrackingRecord r : workflowService
        .findTrackingRecordsForChecklist(projectId,
            checklistOrderByClusterId.getId(), pfs, authToken)
        .getObjects()) {
      // The tracking record should have at least one concept too
      assertTrue(r.getConcepts().size() > 0);
      assertEquals(new Long(++i), r.getClusterId());
    }

    //
    // Create checklist with random tracking records
    //
    // Randomize flag picks random tracking records from the bin
    Logger.getLogger(getClass()).debug("  Create checklist in random order");
    final Checklist checklistOrderByRandom = workflowService.createChecklist(
        projectId, testNameBin.getId(), null, "checklistOrderByRandom",
        "testDescription", true, false, null, pfs, authToken);
    Logger.getLogger(getClass())
        .debug("    checklist = " + checklistOrderByRandom);
    // Assert that cluster ids are contiguous and in order
    boolean found = false;
    for (final TrackingRecord r : workflowService
        .findTrackingRecordsForChecklist(projectId,
            checklistOrderByRandom.getId(), pfs, authToken)
        .getObjects()) {
      // The tracking record should have at least one concept too
      assertTrue(r.getConcepts().size() > 0);
      // If the first 5 get randomly picked, this won't work
      if (r.getClusterId() > 5) {
        found = true;
        break;
      }
    }
    assertTrue("Expected at least one cluster id not in the first 5", found);

    // Remove checklist
    Logger.getLogger(getClass()).debug("  Remove checklists");
    workflowService.removeChecklist(projectId, checklistOrderByRandom.getId(),
        authToken);
    workflowService.removeChecklist(projectId,
        checklistOrderByClusterId.getId(), authToken);

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

    // regenerate bins
    workflowService.regenerateBins(projectId,
        WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);

    // get test name bin
    Logger.getLogger(getClass()).debug("  Find testName workflow bin");
    final WorkflowBinList binList = workflowService.getWorkflowBins(projectId,
        WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);
    WorkflowBin testNameBin = null;
    for (final WorkflowBin bin : binList.getObjects()) {
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
    final Worklist worklist = workflowService.createWorklist(projectId,
        testNameBin.getId(), "chem", pfs, authToken);
    Logger.getLogger(getClass()).debug("    worklist = " + worklist);
    assertTrue(worklist.getName().startsWith("wrk"));
    final TrackingRecordList list =
        workflowService.findTrackingRecordsForWorklist(projectId,
            worklist.getId(), null, authToken);
    for (final TrackingRecord record : list.getObjects()) {
      assertEquals("chem", record.getClusterType());
      assertTrue(record.getConcepts().size() > 0);
    }
    assertEquals(5, list.size());

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

    // Regenerate bins
    workflowService.regenerateBins(projectId,
        WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);

    Logger.getLogger(getClass()).debug("  Find testName workflow bin");
    final WorkflowBinList binList = workflowService.getWorkflowBins(projectId,
        WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);
    WorkflowBin testNameBin = null;
    for (final WorkflowBin bin : binList.getObjects()) {
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
    final Worklist worklist = workflowService.createWorklist(projectId,
        testNameBin.getId(), "chem", pfs, authToken);
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

    // Regenerate bins
    workflowService.regenerateBins(projectId,
        WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);

    Logger.getLogger(getClass()).debug("  Find testName workflow bin");
    final WorkflowBinList binList = workflowService.getWorkflowBins(projectId,
        WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);
    WorkflowBin testNameBin = null;
    for (final WorkflowBin bin : binList.getObjects()) {
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
    Worklist worklist = workflowService.createWorklist(projectId,
        testNameBin.getId(), "chem", pfs, authToken);
    // Verify that worklist exists
    worklist =
        workflowService.getWorklist(projectId, worklist.getId(), authToken);
    Logger.getLogger(getClass()).debug("    worklist = " + worklist);

    pfs.setStartIndex(5);
    pfs.setMaxResults(5);
    final Worklist worklist2 = workflowService.createWorklist(projectId,
        testNameBin.getId(), "chem", pfs, authToken);
    Logger.getLogger(getClass()).debug("    worklist2 = " + worklist2);

    // Generate concept report
    Logger.getLogger(getClass()).debug("  Generate concept reports");
    final String reportFileName = workflowService.generateConceptReport(
        projectId, worklist.getId(), 1L, false, "", 0, authToken);
    Logger.getLogger(getClass()).debug("    report = " + reportFileName);

    // Generate concept report
    final String reportFileName2 = workflowService.generateConceptReport(
        projectId, worklist2.getId(), 1L, false, "", 0, authToken);
    Logger.getLogger(getClass()).debug("    report2 = " + reportFileName2);

    // Find reports
    pfs.setStartIndex(0);
    pfs.setMaxResults(1);
    Logger.getLogger(getClass()).debug("  Find reports " + pfs);
    final StringList list = workflowService
        .findGeneratedConceptReports(projectId, ".txt", pfs, authToken);
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
    final String report = workflowService.getGeneratedConceptReport(projectId,
        reportFileName, authToken);
    Logger.getLogger(getClass()).debug("    report = " + report);
    assertTrue(report.contains("ATOMS"));

    // Remove the report (and verify it is gone)
    Logger.getLogger(getClass()).debug("  Remove the reports");
    workflowService.removeGeneratedConceptReport(projectId, reportFileName,
        authToken);
    workflowService.removeGeneratedConceptReport(projectId, reportFileName2,
        authToken);
    final StringList list2 = workflowService.findGeneratedConceptReports(
        projectId, ".txt", new PfsParameterJpa(), authToken);
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

    // Regenerate bins
    workflowService.regenerateBins(projectId,
        WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);

    Logger.getLogger(getClass()).debug("  Find testName workflow bin");
    final WorkflowBinList binList = workflowService.getWorkflowBins(projectId,
        WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);
    WorkflowBin testNameBin = null;
    for (final WorkflowBin bin : binList.getObjects()) {
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
    final Worklist worklist = workflowService.createWorklist(projectId,
        testNameBin.getId(), "chem", pfs, authToken);
    Logger.getLogger(getClass()).debug("    worklist = " + worklist);

    // Get workflow bins
    Logger.getLogger(getClass()).debug("  Get workflow bins");
    final WorkflowBinList list = workflowService.getWorkflowBins(projectId,
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
    final MetadataServiceRest service =
        new MetadataClientRest(ConfigUtility.getConfigProperties());
    final SemanticTypeList styList =
        service.getSemanticTypes(umlsTerminology, umlsVersion, authToken);

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
    }

    return map;
  }

  /**
   * Test regenerating an editable bin - it should have tracking records.
   *
   * @throws Exception the exception
   */
  @Test
  public void testEditableBin() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Add a editable bin definition
    Logger.getLogger(getClass())
        .info("    Add required editable bin definition");
    WorkflowBinDefinitionJpa definition = new WorkflowBinDefinitionJpa();
    definition.setName("testEditable");
    definition.setDescription("Test Editable.");
    definition.setQuery("select a.id clusterId, a.id conceptId "
        + "from concepts a, concepts_atoms b, atoms c "
        + "where a.id = b.concepts_id " + "  and b.atoms_id = c.id  "
        + "  and a.terminology = :terminology and c.terminology='NCI' "
        + "  and c.workflowStatus = 'NEEDS_REVIEW'");
    definition.setEditable(true);
    definition.setEnabled(true);
    definition.setRequired(true);
    definition.setQueryType(QueryType.SQL);
    definition.setWorkflowConfig(config);
    definition = (WorkflowBinDefinitionJpa) workflowService
        .addWorkflowBinDefinition(projectId, null,definition, authToken);

    // Add same not-editable definition
    Logger.getLogger(getClass()).info("    Add same nonEditable definition");
    WorkflowBinDefinitionJpa definition2 = new WorkflowBinDefinitionJpa();
    definition2.setName("testNonEditable");
    definition2.setDescription("Test NonEditable.");
    definition2.setQuery("atoms.terminology:AIR");
    definition2.setEditable(false);
    definition.setEnabled(true);
    definition2.setRequired(true);
    definition2.setQueryType(QueryType.LUCENE);
    definition2.setWorkflowConfig(config);
    definition2 = (WorkflowBinDefinitionJpa) workflowService
        .addWorkflowBinDefinition(projectId, null,definition2, authToken);

    // Regenerate bins
    workflowService.regenerateBins(projectId,
        WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);
    final WorkflowBinList binList = workflowService.getWorkflowBins(projectId,
        WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);
    assertEquals(3, binList.size());

    boolean found1 = false;
    boolean found2 = false;
    for (final WorkflowBin bin : binList.getObjects()) {
      if (bin.getName().equals("testEditable")) {
        // Find tracking records for workflow bin should return records
        final TrackingRecordList list =
            workflowService.findTrackingRecordsForWorkflowBin(projectId,
                bin.getId(), null, authToken);
        assertTrue(list.size() > 0);
        assertTrue(bin.getClusterCt() > 0);
        found1 = true;
      }
      if (bin.getName().equals("testNonEditable")) {
        final TrackingRecordList list =
            workflowService.findTrackingRecordsForWorkflowBin(projectId,
                bin.getId(), null, authToken);
        assertFalse(list.size() > 0);
        assertTrue(bin.getClusterCt() > 0);
        found2 = true;
      }
    }
    assertTrue(found1);
    assertTrue(found2);

    // Clear bins
    Logger.getLogger(getClass()).debug("  Clear and regenerate bins");
    workflowService.clearBins(projectId, WorkflowBinType.MUTUALLY_EXCLUSIVE,
        authToken);
    final WorkflowBinList binList2 = workflowService.getWorkflowBins(projectId,
        WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);
    assertEquals(0, binList2.size());

    // Remove the definition
    workflowService.removeWorkflowBinDefinition(projectId, definition.getId(),
        authToken);
    workflowService.removeWorkflowBinDefinition(projectId, definition2.getId(),
        authToken);

  }

  /**
   * Test create/find/delete worklist.
   *
   * @throws Exception the exception
   */
  @Test
  public void testWorklistChecklist() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // regenerate bins
    workflowService.regenerateBins(projectId,
        WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);

    // get test name bin
    Logger.getLogger(getClass()).debug("  Find testName workflow bin");
    final WorkflowBinList binList = workflowService.getWorkflowBins(projectId,
        WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);
    WorkflowBin testNameBin = null;
    for (final WorkflowBin bin : binList.getObjects()) {
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
    final Worklist worklist = workflowService.createWorklist(projectId,
        testNameBin.getId(), "chem", pfs, authToken);
    Logger.getLogger(getClass()).debug("    worklist = " + worklist);
    final TrackingRecordList list =
        workflowService.findTrackingRecordsForWorklist(projectId,
            worklist.getId(), null, authToken);
    assertEquals(5, list.size());

    // Make a checklist and exclude stuff on worklist.
    Logger.getLogger(getClass()).debug("  Create checklist");
    final Checklist checklist = workflowService.createChecklist(projectId,
        testNameBin.getId(), "chem", "testDescription", "checklistWorklist",
        false, true, "", pfs, authToken);
    final TrackingRecordList list2 =
        workflowService.findTrackingRecordsForChecklist(projectId,
            checklist.getId(), null, authToken);
    assertEquals(5, list2.size());

    // Assert that none of the cluster ids are in common
    Logger.getLogger(getClass())
        .debug("  Verify checklist does not overlap with worklist");
    final List<Long> worklistIds = list.getObjects().stream()
        .map(r -> r.getClusterId()).collect(Collectors.toList());
    final List<Long> checklistIds = list2.getObjects().stream()
        .map(r -> r.getClusterId()).collect(Collectors.toList());
    assertEquals(5, worklistIds.size());
    assertEquals(5, checklistIds.size());
    final List<Long> minusIds = new ArrayList<Long>(worklistIds);
    minusIds.removeAll(checklistIds);
    assertEquals(5, minusIds.size());

    // Remove the worklist
    Logger.getLogger(getClass()).debug("  Remove worklist");
    workflowService.removeWorklist(projectId, worklist.getId(), authToken);

    Logger.getLogger(getClass()).debug("  Remove checklist");
    workflowService.removeChecklist(projectId, checklist.getId(), authToken);

    // clear bins
    Logger.getLogger(getClass()).debug("  Clear bins");
    workflowService.clearBins(projectId, WorkflowBinType.MUTUALLY_EXCLUSIVE,
        authToken);

  }

  /**
   * Test new bins
   *
   * @throws Exception the exception
   */
  @Test
  public void testNewBin() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Add a required SQL bin definition
    Logger.getLogger(getClass()).info("    Add required SQL bin definition");
    WorkflowBinDefinitionJpa definition = new WorkflowBinDefinitionJpa();
    definition.setName("testSQL");
    definition.setDescription("Test SQL.");
    definition.setQuery("select a.id clusterId, a.id conceptId "
        + "from concepts a, concepts_atoms b, atoms c "
        + "where a.id = b.concepts_id " + "  and b.atoms_id = c.id  "
        + "  and a.terminology = :terminology and c.terminology='NCI' "
        + "  and c.workflowStatus = 'NEEDS_REVIEW'");
    definition.setEditable(true);
    definition.setEnabled(true);
    definition.setRequired(true);
    definition.setQueryType(QueryType.SQL);
    definition.setWorkflowConfig(config);
    definition = (WorkflowBinDefinitionJpa) workflowService
        .addWorkflowBinDefinition(projectId, null, definition, authToken);

    // Regenerate bins
    workflowService.regenerateBins(projectId,
        WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);

    // Clear bins
    Logger.getLogger(getClass()).debug("  Clear bins");
    workflowService.clearBins(projectId, WorkflowBinType.MUTUALLY_EXCLUSIVE,
        authToken);
    // Remove the definition
    workflowService.removeWorkflowBinDefinition(projectId, definition.getId(),
        authToken);

  }

  // TODO: need an integration test for concept approval causing a tracking
  // record status to change (include the multilpe concept case)
}
