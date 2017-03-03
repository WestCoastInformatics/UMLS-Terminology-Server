/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wci.umls.server.mojo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.wci.umls.server.AlgorithmConfig;
import com.wci.umls.server.ProcessConfig;
import com.wci.umls.server.Project;
import com.wci.umls.server.UserRole;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.QueryStyle;
import com.wci.umls.server.helpers.QueryType;
import com.wci.umls.server.helpers.SearchResult;
import com.wci.umls.server.helpers.TypeKeyValue;
import com.wci.umls.server.helpers.WorkflowBinList;
import com.wci.umls.server.helpers.meta.SemanticTypeList;
import com.wci.umls.server.jpa.AlgorithmConfigJpa;
import com.wci.umls.server.jpa.ProcessConfigJpa;
import com.wci.umls.server.jpa.ProjectJpa;
import com.wci.umls.server.jpa.UserJpa;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.helpers.PrecedenceListJpa;
import com.wci.umls.server.jpa.helpers.TypeKeyValueJpa;
import com.wci.umls.server.jpa.services.SecurityServiceJpa;
import com.wci.umls.server.jpa.services.rest.ContentServiceRest;
import com.wci.umls.server.jpa.services.rest.IntegrationTestServiceRest;
import com.wci.umls.server.jpa.services.rest.MetaEditingServiceRest;
import com.wci.umls.server.jpa.services.rest.MetadataServiceRest;
import com.wci.umls.server.jpa.services.rest.ProcessServiceRest;
import com.wci.umls.server.jpa.services.rest.ProjectServiceRest;
import com.wci.umls.server.jpa.services.rest.SecurityServiceRest;
import com.wci.umls.server.jpa.workflow.WorkflowBinDefinitionJpa;
import com.wci.umls.server.jpa.workflow.WorkflowConfigJpa;
import com.wci.umls.server.jpa.workflow.WorkflowEpochJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.meta.SemanticType;
import com.wci.umls.server.model.workflow.Checklist;
import com.wci.umls.server.model.workflow.WorkflowAction;
import com.wci.umls.server.model.workflow.WorkflowBin;
import com.wci.umls.server.model.workflow.WorkflowConfig;
import com.wci.umls.server.model.workflow.WorkflowStatus;
import com.wci.umls.server.model.workflow.Worklist;
import com.wci.umls.server.rest.impl.ContentServiceRestImpl;
import com.wci.umls.server.rest.impl.IntegrationTestServiceRestImpl;
import com.wci.umls.server.rest.impl.MetaEditingServiceRestImpl;
import com.wci.umls.server.rest.impl.MetadataServiceRestImpl;
import com.wci.umls.server.rest.impl.ProcessServiceRestImpl;
import com.wci.umls.server.rest.impl.ProjectServiceRestImpl;
import com.wci.umls.server.rest.impl.SecurityServiceRestImpl;
import com.wci.umls.server.rest.impl.WorkflowServiceRestImpl;
import com.wci.umls.server.services.SecurityService;

/**
 * Goal which generates sample data for the default dev build. This uses REST
 * services directly and not through the client.
 * 
 * See admin/loader/pom.xml for sample usage
 */
@Mojo(name = "generate-nci-meta-data", defaultPhase = LifecyclePhase.PACKAGE)
public class GenerateNciMetaDataMojo extends AbstractLoaderMojo {

  /**
   * Mode - for recreating db.
   */
  @Parameter
  private String mode = null;

  /** The terminology. */
  @Parameter
  private String terminology = "NCIMTH";

  /** The version. */
  @Parameter
  private String version = "latest";

  /** The next release. */
  // private final String nextRelease = "2016AB";

  /**
   * Instantiates a {@link GenerateNciMetaDataMojo} from the specified
   * parameters.
   */
  public GenerateNciMetaDataMojo() {
    // do nothing
  }

  /* see superclass */
  @Override
  public void execute() throws MojoFailureException {

    try {

      getLog().info("Generate sample data");
      getLog().info("  mode = " + mode);
      getLog().info("  terminology = " + terminology);
      getLog().info("  version = " + version);

      // Handle creating the database if the mode parameter is set
      final Properties properties = ConfigUtility.getConfigProperties();

      // Rebuild the database
      if (mode != null && mode.equals("create")) {
        createDb(false);
      }

      // authenticate
      final SecurityService service = new SecurityServiceJpa();
      final String authToken =
          service.authenticate(properties.getProperty("admin.user"),
              properties.getProperty("admin.password")).getAuthToken();
      service.close();

      loadSampleData(authToken);

      getLog().info("done ...");
    } catch (Exception e) {
      e.printStackTrace();
      throw new MojoFailureException("Unexpected exception:", e);
    }
  }

  /**
   * Load sample data.
   *
   * @param authToken the auth token
   * @throws Exception the exception
   */
  private void loadSampleData(String authToken) throws Exception {
    //
    // // Initialize
    Logger.getLogger(getClass()).info("Authenticate admin user");
    SecurityServiceRest security = new SecurityServiceRestImpl();
    ProjectServiceRest project = new ProjectServiceRestImpl();
    IntegrationTestServiceRest integrationService =
        new IntegrationTestServiceRestImpl();
    //
    // NCIm Users:
    // CFC - Carol Creech
    // BAC - Brian Carlsen
    // RAW - Rick Wood
    // DSS - Deborah Shapiro
    // JFW - Joanne Wong
    // LWW - Larry Wright
    // MWH - MArgaret Haber
    // SDC - Sherri de Coronado
    // GFG - Gilberto Fragoso
    // LAR - Laura Roth
    // LLW - Lori Whiteman
    // TAQ - Theresa Quinn
    // GSC - George Chang
    // HAG - Alpha Garrett
    // TPW - Tammy Powell
    // MJA - Miranda Jarnot
    final String[] initials = new String[] {
        "CFC", "BAC", "RAW", "DSS", "JFW", "LWW", "MWH", "SDC", "GFG", "LAR",
        "LLW", "TAQ", "GSC", "HAG", "TPW", "MJA", "NEO"
    };
    final String[] names = new String[] {
        "Carol Creech", "Brian Carlsen", "Rick Wood", "Deborah Shapiro",
        "Joanne Wong", "Larry Wright", "Margaret Haber", "Sherri de Coronado",
        "Gilberto Fragoso", "Laura Roth", "Lori Whiteman", "Theresa Quinn",
        "George Chang", "Alpha Garret", "Tammy Powell", "Miranda Jarnot",
        "Nels Olson"
    };
    final int[] editorLevels = new int[] {
        0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 5, 0, 5, 0, 5, 5, 0
    };
    final String[] roles = new String[] {
        "AUTHOR", "ADMINISTRATOR", "ADMINISTRATOR", "ADMINISTRATOR",
        "ADMINISTRATOR", "REVIEWER", "REVIEWER", "REVIEWER", "ADMINISTRATOR",
        "REVIEWER", "REVIEWER", "AUTHOR", "AUTHOR", "AUTHOR", "REVIEWER",
        "REVIEWER", "ADMINISTRATOR"
    };

    Logger.getLogger(getClass()).info("Add new users");

    for (int i = 0; i < initials.length; i++) {
      final String inits = initials[i];
      final String name = names[i];
      UserJpa user = (UserJpa) security.getUser(inits, authToken);
      if (user == null) {
        user = makeUser(inits, name);
        user.setEditorLevel(editorLevels[i]);

        user.setApplicationRole(UserRole.valueOf("USER"));
        Logger.getLogger(getClass()).info("  user = " + user);
        user = (UserJpa) security.addUser(user, authToken);
      } else {
        Logger.getLogger(getClass())
            .info("  user = " + inits + " ALREADY EXISTS");
      }
    }

    //
    // Make a project
    //

    ProjectJpa project1 = new ProjectJpa();
    project1.setBranch(Branch.ROOT);
    project1.setDescription("Project for NCI-META Editing");
    project1.setFeedbackEmail("info@westcoastinformatics.com");
    project1.setName("NCI-META Editing " + new Date().getTime());
    project1.setPublic(true);
    project1.setLanguage("ENG");
    project1.setTerminology(terminology);
    project1.setWorkflowPath(ConfigUtility.DEFAULT);
    project1.setVersion(version);
    project1.setEditingEnabled(true);
    project1.setAutomationsEnabled(true);
    List<String> newAtomTermgroups = new ArrayList<>();
    // newAtomTermgroups.add("MTH/PN");
    newAtomTermgroups.add("NCIMTH/PN");
    project1.setNewAtomTermgroups(newAtomTermgroups);

    // Configure valid categories
    final List<String> validCategories = new ArrayList<>();
    validCategories.add("chem");
    project1.setValidCategories(validCategories);

    Map<String, String> semanticTypeCategoryMap =
        getSemanticTypeCategoryMap(authToken);
    project1.setSemanticTypeCategoryMap(semanticTypeCategoryMap);

    final List<String> validationChecks = new ArrayList<>();
    validationChecks.add("DEFAULT");
    validationChecks.add("DT_M1");
    validationChecks.add("DT_I3B");
    validationChecks.add("MGV_H1");
    validationChecks.add("MGV_H2");
    project1.setValidationChecks(validationChecks);

    // Add the default validationData
    final List<TypeKeyValue> validationData = new ArrayList<>();

    integrationService = new IntegrationTestServiceRestImpl();
    TypeKeyValue typeKeyValue1 = integrationService
        .addTypeKeyValue(new TypeKeyValueJpa("MGV_I", "CBO", ""), authToken);
    integrationService = new IntegrationTestServiceRestImpl();
    TypeKeyValue typeKeyValue2 = integrationService.addTypeKeyValue(
        new TypeKeyValueJpa("MGV_I", "ISO3166-2", ""), authToken);
    integrationService = new IntegrationTestServiceRestImpl();
    TypeKeyValue typeKeyValue3 = integrationService
        .addTypeKeyValue(new TypeKeyValueJpa("MGV_SCUI", "NCI", ""), authToken);

    validationData.add(typeKeyValue1);
    validationData.add(typeKeyValue2);
    validationData.add(typeKeyValue3);

    project1.setValidationData(validationData);

    // Handle precedence list
    MetadataServiceRest metadataService = new MetadataServiceRestImpl();
    PrecedenceListJpa list =
        new PrecedenceListJpa(metadataService.getDefaultPrecedenceList(
            project1.getTerminology(), project1.getVersion(), authToken));
    list.setId(null);
    // Make sure the project connected list has no terminology/version
    list.setTerminology("");
    list.setVersion("");
    metadataService = new MetadataServiceRestImpl();
    list =
        (PrecedenceListJpa) metadataService.addPrecedenceList(list, authToken);
    project1.setPrecedenceListId(list.getId());

    // Add project
    project1 = (ProjectJpa) project.addProject(project1, authToken);
    final Long projectId = project1.getId();

    //
    // Assign project roles
    //
    Logger.getLogger(getClass()).info("Assign users to projects");

    for (int i = 0; i < initials.length; i++) {
      final String inits = initials[i];
      final String role = roles[i];
      project = new ProjectServiceRestImpl();
      project.assignUserToProject(projectId, inits, UserRole.valueOf(role),
          authToken);
    }

    // Create and set up a process and algorithm configuration for testing
    ProcessServiceRest process = new ProcessServiceRestImpl();

    ProcessConfig processConfig = new ProcessConfigJpa();
    processConfig.setDescription("Process for testing use");
    processConfig.setFeedbackEmail(null);
    processConfig.setName("Test Process");
    processConfig.setProject(project1);
    processConfig.setTerminology(terminology);
    processConfig.setVersion(version);
    processConfig.setTimestamp(new Date());
    processConfig.setType("Insertion");
    processConfig = process.addProcessConfig(projectId,
        (ProcessConfigJpa) processConfig, authToken);
    process = new ProcessServiceRestImpl();

    AlgorithmConfig algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("WAIT");
    algoConfig.setDescription("Algorithm for testing use");
    algoConfig.setEnabled(true);
    algoConfig.setName("Test WAIT algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());

    // Create and set required algorithm properties
    Map<String, String> algoProperties = new HashMap<String, String>();
    algoProperties.put("num", "10");
    algoConfig.setProperties(algoProperties);

    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();

    processConfig.getSteps().add(algoConfig);
    process.updateProcessConfig(projectId, (ProcessConfigJpa) processConfig,
        authToken);
    process = new ProcessServiceRestImpl();

    //
    // Create and set up process and algorithm configurations
    //
    createNciInsertionProcess(project1, projectId, authToken);
    createSnomedCtInsertionProcess(project1, projectId, authToken);
    createUmlsInsertionProcess(project1, projectId, authToken);
    createTemplateInsertionProcess(project1, projectId, authToken);
    createPreProductionProcess(project1, projectId, authToken);
    createReleaseProcess(project1, projectId, authToken);
    createFeedbackProcess(project1, projectId, authToken);
    createProdMidCleanupProcess(project1, projectId, authToken);
    createReportProcesses(project1, projectId, authToken);
    createLexicalClassAssignmentProcess(project1, projectId, authToken);
    createComputePreferredNamesProcess(project1, projectId, authToken);
    createRemapComponentInfoRelationshipsProcesses(project1, projectId,
        authToken);

    //
    // Fake some data as needs review
    //
    getLog().info("Fake some needs review content");
    ContentServiceRest contentService = new ContentServiceRestImpl();
    IntegrationTestServiceRest testService =
        new IntegrationTestServiceRestImpl();

    //
    // Demotions
    //
    getLog().info("  Add demotions");
    PfsParameterJpa pfs = new PfsParameterJpa();
    pfs.setStartIndex(1000);
    pfs.setMaxResults(80);
    contentService = new ContentServiceRestImpl();
    final Long[] id1s =
        contentService.findConcepts(terminology, version, null, pfs, authToken)
            .getObjects().stream().map(c -> c.getId())
            .collect(Collectors.toList()).toArray(new Long[] {});
    pfs.setStartIndex(1100);
    pfs.setMaxResults(80);
    contentService = new ContentServiceRestImpl();
    final Long[] id2s =
        contentService.findConcepts(terminology, version, null, pfs, authToken)
            .getObjects().stream().map(c -> c.getId())
            .collect(Collectors.toList()).toArray(new Long[] {});
    for (int i = 0; i < id1s.length; i++) {

      contentService = new ContentServiceRestImpl();
      final Concept fromConcept =
          contentService.getConcept(id1s[i], projectId, authToken);
      final Long fromId = fromConcept.getAtoms().get(0).getId();
      contentService = new ContentServiceRestImpl();
      final Long toId = contentService.getConcept(id2s[i], projectId, authToken)
          .getAtoms().iterator().next().getId();

      final MetaEditingServiceRest metaEditingService =
          new MetaEditingServiceRestImpl();
      metaEditingService.addDemotion(projectId, id1s[i], "DEMOTIONS",
          fromConcept.getLastModified().getTime(), id2s[i], fromId, toId, false,
          authToken);
    }

    // Status N NCIt concepts (and atoms)
    getLog().info("  Mark first 50 NCIt concepts as status N");
    pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(50);
    contentService = new ContentServiceRestImpl();
    for (final SearchResult result : contentService.findConcepts(terminology,
        version, "atoms.terminology:NCI", pfs, authToken).getObjects()) {
      contentService = new ContentServiceRestImpl();
      final ConceptJpa concept = new ConceptJpa(
          contentService.getConcept(result.getId(), projectId, authToken),
          true);
      concept.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
      // Make all NCI atoms needs review
      for (final Atom atom : concept.getAtoms()) {
        if (atom.getTerminology().equals("NCI")) {
          atom.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
          testService = new IntegrationTestServiceRestImpl();
          testService.updateAtom(new AtomJpa(atom), authToken);
        }
      }
      testService = new IntegrationTestServiceRestImpl();
      testService.updateConcept(new ConceptJpa(concept, false), authToken);
    }

    // SNOMEDCT_US
    getLog().info("  Mark first 100 SNOMED concepts as status N");
    pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(100);
    contentService = new ContentServiceRestImpl();
    for (final SearchResult result : contentService.findConcepts(terminology,
        version, "atoms.terminology:SNOMEDCT_US", pfs, authToken)
        .getObjects()) {
      contentService = new ContentServiceRestImpl();
      final ConceptJpa concept = new ConceptJpa(
          contentService.getConcept(result.getId(), projectId, authToken),
          true);

      // skip if any concepts have NCI atoms
      if (concept.getAtoms().stream().map(a -> a.getTerminology())
          .filter(t -> t.equals("NCI")).collect(Collectors.toList())
          .size() > 0) {
        continue;
      }
      concept.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);

      // Make all SNOMEDCT_US atoms needs review
      for (final Atom atom : concept.getAtoms()) {
        if (atom.getTerminology().equals("SNOMEDCT_US")) {
          atom.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
          testService = new IntegrationTestServiceRestImpl();
          testService.updateAtom(new AtomJpa(atom), authToken);
        }
      }
      testService = new IntegrationTestServiceRestImpl();
      testService.updateConcept(new ConceptJpa(concept, false), authToken);
    }

    // leftovers
    getLog().info("  Mark first 100 RXNORM concepts as status N");
    pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(100);
    contentService = new ContentServiceRestImpl();
    for (final SearchResult result : contentService.findConcepts(terminology,
        version, "atoms.terminology:RXNORM", pfs, authToken).getObjects()) {
      contentService = new ContentServiceRestImpl();
      final ConceptJpa concept = new ConceptJpa(
          contentService.getConcept(result.getId(), projectId, authToken),
          true);

      // skip if any concepts have NCI atoms
      if (concept.getAtoms().stream().map(a -> a.getTerminology())
          .filter(t -> t.equals("NCI") || t.equals("SNOMEDCT_US"))
          .collect(Collectors.toList()).size() > 0) {
        continue;
      }
      concept.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);

      // Make all SNOMEDCT_US atoms needs review
      for (final Atom atom : concept.getAtoms()) {
        if (!atom.getTerminology().equals("NCI")
            && !atom.getTerminology().equals("SNOMEDCT_US")) {
          atom.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
          testService = new IntegrationTestServiceRestImpl();
          testService.updateAtom(new AtomJpa(atom), authToken);
        }
      }

      testService = new IntegrationTestServiceRestImpl();
      testService.updateConcept(new ConceptJpa(concept, false), authToken);
    }

    //
    // Prepare workflow related objects
    //
    getLog().info("Prepare workflow related objects");
    WorkflowServiceRestImpl workflowService = new WorkflowServiceRestImpl();

    // Create a workflow epoch
    getLog().info("  Create epoch 15a");
    WorkflowEpochJpa workflowEpoch = new WorkflowEpochJpa();
    workflowEpoch.setActive(true);
    workflowEpoch.setName("15a");
    workflowEpoch.setProject(project1);
    workflowService.addWorkflowEpoch(projectId, workflowEpoch, authToken);

    getLog().info("  Create epoch 16a");
    workflowEpoch = new WorkflowEpochJpa();
    workflowEpoch.setActive(true);
    workflowEpoch.setName("16a");
    workflowEpoch.setProject(project1);
    workflowService = new WorkflowServiceRestImpl();
    workflowService.addWorkflowEpoch(projectId, workflowEpoch, authToken);

    //
    // Add a ME bins workflow config for the current project
    //
    getLog().info("  Create a ME workflow config");
    workflowService = new WorkflowServiceRestImpl();
    WorkflowConfigJpa config = new WorkflowConfigJpa();
    config.setType("MUTUALLY_EXCLUSIVE");
    config.setMutuallyExclusive(true);
    config.setQueryStyle(QueryStyle.CLUSTER);
    config.setProjectId(projectId);
    workflowService = new WorkflowServiceRestImpl();
    WorkflowConfig newConfig =
        workflowService.addWorkflowConfig(projectId, config, authToken);

    // Add workflow definitions
    // demotions
    getLog().info("    Add 'demotions' workflow bin definition");
    WorkflowBinDefinitionJpa definition = new WorkflowBinDefinitionJpa();
    definition.setName("demotions");
    definition.setDescription(
        "Clustered concepts that failed insertion merges.  Must be either related or merged.");
    definition
        .setQuery("select d.concepts_id conceptId1, e.concepts_id conceptId2 "
            + "from atom_relationships a, atoms b, atoms c, "
            + "concepts_atoms d, concepts_atoms e, concepts f, concepts g "
            + "where a.terminology = :terminology and a.workflowStatus = 'DEMOTION' "
            + "  and a.from_id = b.id and a.to_id = c.id "
            + "  and b.id = d.atoms_id and c.id = e.atoms_id "
            + "  and d.concepts_id = f.id and e.concepts_id = g.id"
            + "  and f.terminology = :terminology and g.terminology = :terminology");
    definition.setEditable(true);
    definition.setEnabled(true);
    definition.setRequired(true);
    definition.setQueryType(QueryType.SQL);
    definition.setWorkflowConfig(newConfig);
    workflowService = new WorkflowServiceRestImpl();
    workflowService.addWorkflowBinDefinition(projectId, null, definition,
        authToken);

    // norelease
    getLog().info("    Add 'norelease' workflow bin definition");
    definition = new WorkflowBinDefinitionJpa();
    definition.setName("norelease");
    definition.setDescription("Concepts where all atoms are unreleasable.");
    definition.setQuery("published:false AND NOT atoms.published:true");
    definition.setEditable(false);
    definition.setEnabled(true);
    definition.setRequired(false);
    definition.setQueryType(QueryType.LUCENE);
    definition.setWorkflowConfig(newConfig);
    workflowService = new WorkflowServiceRestImpl();
    workflowService.addWorkflowBinDefinition(projectId, null, definition,
        authToken);

    // reviewed
    getLog().info("    Add 'reviewed' workflow bin definition");
    definition = new WorkflowBinDefinitionJpa();
    definition.setName("reviewed");
    definition.setDescription("Concepts that do not require review.");
    definition.setQuery("NOT workflowStatus:NEEDS_REVIEW");
    definition.setEditable(false);
    definition.setEnabled(true);
    definition.setRequired(false);
    definition.setQueryType(QueryType.LUCENE);
    definition.setWorkflowConfig(newConfig);
    workflowService = new WorkflowServiceRestImpl();
    workflowService.addWorkflowBinDefinition(projectId, null, definition,
        authToken);

    // ncithesaurus
    getLog().info("    Add 'ncithesaurus' workflow bin definition");
    definition = new WorkflowBinDefinitionJpa();
    definition.setName("ncithesaurus");
    definition.setDescription("NCI Thesaurus.");
    definition.setQuery("select a.id clusterId, a.id conceptId "
        + "from concepts a, concepts_atoms b, atoms c "
        + "where a.id = b.concepts_id " + "  and b.atoms_id = c.id  "
        + "  and a.terminology = :terminology and c.terminology='NCI' "
        + "  and c.workflowStatus = 'NEEDS_REVIEW'");
    definition.setEditable(true);
    definition.setEnabled(true);
    definition.setRequired(true);
    definition.setQueryType(QueryType.SQL);
    definition.setWorkflowConfig(newConfig);
    workflowService = new WorkflowServiceRestImpl();
    workflowService.addWorkflowBinDefinition(projectId, null, definition,
        authToken);

    // snomedct_us
    getLog().info("    Add 'snomedct_us' workflow bin definition");
    definition = new WorkflowBinDefinitionJpa();
    definition.setName("snomedct_us");
    definition.setDescription("SNOMEDCT_US.");
    definition.setQuery("select a.id clusterId, a.id conceptId "
        + "from concepts a, concepts_atoms b, atoms c "
        + "where a.id = b.concepts_id " + "  and b.atoms_id = c.id  "
        + "  and a.terminology = :terminology and c.terminology='SNOMEDCT_US' "
        + "  and c.workflowStatus = 'NEEDS_REVIEW'");
    definition.setEditable(true);
    definition.setEnabled(true);
    definition.setRequired(true);
    definition.setQueryType(QueryType.SQL);
    definition.setWorkflowConfig(newConfig);
    workflowService = new WorkflowServiceRestImpl();
    workflowService.addWorkflowBinDefinition(projectId, null, definition,
        authToken);

    // leftovers
    getLog().info("    Add 'leftovers' workflow bin definition");
    definition = new WorkflowBinDefinitionJpa();
    definition.setName("leftovers");
    definition.setDescription("SNOMEDCT_US.");
    definition.setQuery("select a.id clusterId, a.id conceptId "
        + "from concepts a where a.workflowStatus = 'NEEDS_REVIEW'");
    definition.setEditable(true);
    definition.setEnabled(true);
    definition.setRequired(true);
    definition.setQueryType(QueryType.SQL);
    definition.setWorkflowConfig(newConfig);
    workflowService = new WorkflowServiceRestImpl();
    workflowService.addWorkflowBinDefinition(projectId, null, definition,
        authToken);

    // Clear and regenerate all bins
    getLog().info("  Clear and regenerate ME bins");
    // Clear bins
    workflowService = new WorkflowServiceRestImpl();
    workflowService.clearBins(projectId, "MUTUALLY_EXCLUSIVE", authToken);

    // Regenerate bins
    workflowService = new WorkflowServiceRestImpl();
    workflowService.regenerateBins(projectId, "MUTUALLY_EXCLUSIVE", authToken);

    // Get bins
    workflowService = new WorkflowServiceRestImpl();
    final WorkflowBinList bins = workflowService.getWorkflowBins(projectId,
        "MUTUALLY_EXCLUSIVE", authToken);

    // For each editable bin, make two worklists of size 5
    Worklist lastWorklist = null;
    int chk = 100;
    for (final WorkflowBin bin : bins.getObjects()) {
      // Log all
      getLog().info(
          "  bin " + bin.getName() + " = " + bin.getTrackingRecords().size());

      // Log "chem" count
      workflowService = new WorkflowServiceRestImpl();
      int chemRecords = workflowService
          .findTrackingRecordsForWorkflowBin(projectId, bin.getId(), null,
              authToken)
          .getObjects().stream().filter(r -> r.getClusterType().equals("chem"))
          .collect(Collectors.toList()).size();
      getLog().info("    chem = " + chemRecords);
      getLog().info(
          "    non chem = " + (bin.getTrackingRecords().size() - chemRecords));

      if (bin.isEditable()) {
        pfs = new PfsParameterJpa();
        pfs.setStartIndex(0);
        pfs.setMaxResults(5);
        workflowService = new WorkflowServiceRestImpl();
        // Create a chem worklist
        Worklist worklist = null;

        if (chemRecords > 0) {
          worklist = workflowService.createWorklist(projectId, bin.getId(),
              "chem", pfs, authToken);
          workflowService = new WorkflowServiceRestImpl();
          getLog()
              .info(
                  "    count = "
                      + workflowService
                          .findTrackingRecordsForWorklist(projectId,
                              worklist.getId(), pfs, authToken)
                          .getTotalCount());
        }

        // Create two non-chem worklist
        workflowService = new WorkflowServiceRestImpl();
        worklist = workflowService.createWorklist(projectId, bin.getId(), null,
            pfs, authToken);
        workflowService = new WorkflowServiceRestImpl();
        getLog().info("    count = "
            + workflowService.findTrackingRecordsForWorklist(projectId,
                worklist.getId(), pfs, authToken).getTotalCount());

        workflowService = new WorkflowServiceRestImpl();
        worklist = workflowService.createWorklist(projectId, bin.getId(), null,
            pfs, authToken);
        workflowService = new WorkflowServiceRestImpl();
        getLog().info("    count = "
            + workflowService.findTrackingRecordsForWorklist(projectId,
                worklist.getId(), pfs, authToken).getTotalCount());

        lastWorklist = worklist;

        // Create some checklist
        pfs.setMaxResults(10);
        workflowService = new WorkflowServiceRestImpl();
        Checklist checklist = workflowService.createChecklist(projectId,
            bin.getId(), null, "chk_random_nonworklist_" + chk++, "test desc",
            true, true, "", pfs, authToken);
        workflowService = new WorkflowServiceRestImpl();
        getLog().info("    count = "
            + workflowService.findTrackingRecordsForChecklist(projectId,
                checklist.getId(), pfs, authToken).getTotalCount());

        workflowService = new WorkflowServiceRestImpl();
        checklist = workflowService.createChecklist(projectId, bin.getId(),
            null, "chk_random_worklist_" + chk++, "test desc", true, false, "",
            pfs, authToken);
        workflowService = new WorkflowServiceRestImpl();
        getLog().info("    count = "
            + workflowService.findTrackingRecordsForChecklist(projectId,
                checklist.getId(), pfs, authToken).getTotalCount());

        workflowService = new WorkflowServiceRestImpl();
        checklist = workflowService.createChecklist(projectId, bin.getId(),
            null, "chk_nonrandom_noworklist_" + chk++, "test desc", false, true,
            "", pfs, authToken);
        workflowService = new WorkflowServiceRestImpl();
        getLog().info("    count = "
            + workflowService.findTrackingRecordsForChecklist(projectId,
                checklist.getId(), pfs, authToken).getTotalCount());

        workflowService = new WorkflowServiceRestImpl();
        checklist = workflowService.createChecklist(projectId, bin.getId(),
            null, "chk_nonrandom_worklist_" + chk++, "test desc", false, false,
            "", pfs, authToken);
        workflowService = new WorkflowServiceRestImpl();
        getLog().info("    count = "
            + workflowService.findTrackingRecordsForChecklist(projectId,
                checklist.getId(), pfs, authToken).getTotalCount());

      }
    }

    // March "last worklist" through some workflow changes so other dates show
    Logger.getLogger(getClass()).debug("  Walk worklist through workflow");
    // Assign
    workflowService = new WorkflowServiceRestImpl();
    workflowService.performWorkflowAction(projectId, lastWorklist.getId(),
        authToken, UserRole.AUTHOR, WorkflowAction.ASSIGN, authToken);

    // Save
    workflowService = new WorkflowServiceRestImpl();
    workflowService.performWorkflowAction(projectId, lastWorklist.getId(),
        authToken, UserRole.AUTHOR, WorkflowAction.SAVE, authToken);

    // Finish
    workflowService = new WorkflowServiceRestImpl();
    workflowService.performWorkflowAction(projectId, lastWorklist.getId(),
        authToken, UserRole.AUTHOR, WorkflowAction.FINISH, authToken);

    // Assign for review
    workflowService = new WorkflowServiceRestImpl();
    workflowService.performWorkflowAction(projectId, lastWorklist.getId(),
        authToken, UserRole.REVIEWER, WorkflowAction.ASSIGN, authToken);

    // Finish review
    workflowService = new WorkflowServiceRestImpl();
    workflowService.performWorkflowAction(projectId, lastWorklist.getId(),
        authToken, UserRole.REVIEWER, WorkflowAction.FINISH, authToken);

    //
    // Add a QA bins workflow config for the current project
    //
    getLog().info("  Create a QA workflow config");
    workflowService = new WorkflowServiceRestImpl();
    config = new WorkflowConfigJpa();
    config.setType("QUALITY_ASSURANCE");
    config.setMutuallyExclusive(false);
    config.setQueryStyle(QueryStyle.CLUSTER);
    config.setProjectId(projectId);
    workflowService = new WorkflowServiceRestImpl();
    newConfig = workflowService.addWorkflowConfig(projectId, config, authToken);

    // SCUI "merge" bins
    getLog().info("    Add required SCUI merge bins");
    for (final String terminology : new String[] {
        "nci"
    }) {
      getLog()
          .info("    Add '" + terminology + "_merge' workflow bin definition");
      definition = new WorkflowBinDefinitionJpa();
      definition.setName(terminology + "_merge");
      definition.setDescription("Merged " + terminology.toUpperCase()
          + " SCUIs, including merged PTs");
      definition.setQuery("select a.id clusterId, a.id conceptId "
          + "from concepts a, concepts_atoms b, atoms c "
          + "where a.terminology = :terminology "
          + "  and a.id = b.concepts_id and b.atoms_id = c.id  "
          + "  and c.terminology='" + terminology.toUpperCase() + "'  "
          + "group by a.id having count(distinct c.conceptId)>1");
      definition.setEditable(true);
      definition.setEnabled(true);
      definition.setRequired(true);
      definition.setQueryType(QueryType.SQL);
      definition.setWorkflowConfig(newConfig);
      workflowService = new WorkflowServiceRestImpl();
      workflowService.addWorkflowBinDefinition(projectId, null, definition,
          authToken);
    }

    // nci_sub_split
    getLog().info("    Add nci_sub_split bin");
    definition = new WorkflowBinDefinitionJpa();
    definition.setName(terminology + "_merge");
    definition
        .setDescription("Split SCUI current version NCI (or sub-source) atoms");
    definition.setQuery("select a.id clusterId, a.id conceptId "
        + "from concepts a, concepts_atoms b, atoms c "
        + "where a.terminology = :terminology "
        + "  and a.id = b.concepts_id and b.atoms_id = c.id  "
        + "  and c.terminology='NCI'  "
        + "group by a.id having count(distinct c.conceptId)>1");
    definition.setEditable(true);
    definition.setEnabled(true);
    definition.setRequired(true);
    definition.setQueryType(QueryType.SQL);
    definition.setWorkflowConfig(newConfig);
    workflowService = new WorkflowServiceRestImpl();
    workflowService.addWorkflowBinDefinition(projectId, null, definition,
        authToken);

    // sct_sepfnpt
    // cdsty_coc
    // multsty
    // styisa
    // sfo_lfo
    // deleted_cui
    //

    //
    // Non-required
    //

    // SCUI "merge" bins
    getLog().info("    Add non-required SCUI merge bins");
    for (final String terminology : new String[] {
        "rxnorm", "cbo"
    }) {
      getLog()
          .info("    Add '" + terminology + "_merge' workflow bin definition");
      definition = new WorkflowBinDefinitionJpa();
      definition.setName(terminology + "_merge");
      definition.setDescription("Merged " + terminology.toUpperCase()
          + " SCUIs, including merged PTs");
      definition.setQuery("select a.id clusterId, a.id conceptId "
          + "from concepts a, concepts_atoms b, atoms c "
          + "where a.terminology = :terminology "
          + "  and a.id = b.concepts_id " + "  and b.atoms_id = c.id  "
          + "  and c.terminology='" + terminology.toUpperCase() + "'  "
          + "group by a.id having count(distinct c.conceptId)>1");
      definition.setEditable(true);
      definition.setEnabled(true);
      definition.setRequired(false);
      definition.setQueryType(QueryType.SQL);
      definition.setWorkflowConfig(newConfig);
      workflowService = new WorkflowServiceRestImpl();
      workflowService.addWorkflowBinDefinition(projectId, null, definition,
          authToken);
    }

    // sct_sepfnpt
    getLog().info("    Add sct_sepfnpt");
    // rxnorm_split
    // nci_pdq_merge
    // nci_sct_merge
    // ambig_no_ncimth_pn
    // ambig_no_mth_pn
    // ambig_no_rel
    // pn_pn_ambig
    // multiple_pn
    // pn_no_ambig
    // ambig_pn
    // pn_orphan
    // cdsty_coc
    // nosty
    // multsty
    // styisa
    // cbo_chem
    // go_chem
    // mdr_chem
    // true_orphan
    // sfo_lfo
    // deleted_cui_split

    // Clear and regenerate all bins
    getLog().info("  Clear and regenerate QA bins");
    // Clear bins
    workflowService = new WorkflowServiceRestImpl();
    workflowService.clearBins(projectId, "QUALITY_ASSURANCE", authToken);

    // Regenerate bins
    workflowService = new WorkflowServiceRestImpl();
    workflowService.regenerateBins(projectId, "QUALITY_ASSURANCE", authToken);

    //
    // Add MID VALIDATOIN
    //
    getLog().info("  Create a MID VALIDATION config");
    workflowService = new WorkflowServiceRestImpl();
    config = new WorkflowConfigJpa();
    config.setType("MID_VALIDATION");
    config.setMutuallyExclusive(false);
    config.setAdminConfig(true);
    config.setQueryStyle(QueryStyle.CLUSTER);
    config.setProjectId(projectId);
    workflowService = new WorkflowServiceRestImpl();
    newConfig = workflowService.addWorkflowConfig(projectId, config, authToken);

    // Approved C rel matching demotion
    getLog().info("    Approved C rel matching demotion");
    definition = new WorkflowBinDefinitionJpa();
    definition.setName("Approved C rel matching demotion");
    definition.setDescription(
        "Finds approved concept relationships matching demotions.");
    definition.setQuery("select cr.from_id conceptId1, cr.to_id conceptId2 "
        + "from atom_relationships ar, concept_relationships cr, "
        + "     concepts_atoms ca1, concepts_atoms ca2, concepts c1, concepts c2 "
        + "where ar.terminology = :terminology and cr.terminology = :terminology "
        + "  and ar.from_id = ca1.atoms_id and ar.to_id = ca2.atoms_id "
        + "  and cr.from_id = ca1.concepts_id and cr.to_id = ca2.concepts_id "
        + "  and ar.workflowStatus = 'DEMOTION' "
        + "  and cr.workflowStatus in ('READY_FOR_PUBLICATION','PUBLISHED')");
    definition.setEditable(true);
    definition.setEnabled(true);
    definition.setRequired(true);
    definition.setQueryType(QueryType.SQL);
    definition.setWorkflowConfig(newConfig);
    workflowService = new WorkflowServiceRestImpl();
    workflowService.addWorkflowBinDefinition(projectId, null, definition,
        authToken);

    //
    // Add MID VALIDATION (NO concepts)
    //
    getLog().info("  Create a MID VALIDATION_NOCONCEPT config");
    workflowService = new WorkflowServiceRestImpl();
    config = new WorkflowConfigJpa();
    config.setType("MID_VALIDATION_OTHER");
    config.setMutuallyExclusive(false);
    config.setAdminConfig(true);
    config.setQueryStyle(QueryStyle.OTHER);
    config.setProjectId(projectId);
    workflowService = new WorkflowServiceRestImpl();
    newConfig = workflowService.addWorkflowConfig(projectId, config, authToken);

    // Atom with leading/trailing junk
    getLog().info("    Atom with leading/trailing junk");
    definition = new WorkflowBinDefinitionJpa();
    definition.setName("Atom with leading/trailing junk");
    definition.setDescription(
        "Finds atoms with leading or trailing whitespace or junk chars");
    definition
        .setQuery("select c.id from atoms a, concepts_atoms ca, concepts c "
            + "where ca.atoms_id = a.id and ca.concepts_id = c.id"
            + "  and a.name like ' %' or a.name like '% ' "
            + "  and c.terminology = :terminology;");
    definition.setEditable(true);
    definition.setEnabled(true);
    definition.setRequired(true);
    definition.setQueryType(QueryType.SQL);
    definition.setWorkflowConfig(newConfig);
    workflowService = new WorkflowServiceRestImpl();
    workflowService.addWorkflowBinDefinition(projectId, null, definition,
        authToken);

    // Relationship group is null (instead of blank)
    getLog().info("    Relationship group is null");
    definition = new WorkflowBinDefinitionJpa();
    definition.setName("Relationship group is null");
    definition.setDescription(
        "Finds atoms with leading or trailing whitespace or junk chars");
    definition.setQuery(
        "select r.id,'CONCEPT' from concept_relationships r where relGroup is null"
            + "union all select r.id,'ATOM' from atom_relationships r where relGroup is null"
            + "union all select r.id,'CODE' from code_relationships r where relGroup is null"
            + "union all select r.id, 'DESCRIPTOR' from descriptor_relationships r where relGroup is null"
            + "union all select r.id,'COMPONENT_INFO' from component_info_relationships r where relGroup is null");
    definition.setEditable(true);
    definition.setEnabled(true);
    definition.setRequired(true);
    definition.setQueryType(QueryType.SQL);
    definition.setWorkflowConfig(newConfig);
    workflowService = new WorkflowServiceRestImpl();
    workflowService.addWorkflowBinDefinition(projectId, null, definition,
        authToken);

    // ComponentInfoRelationship resolves to nothing (auto-fix -> remove), need
    // algorithm?

    // Matrix initializer
    workflowService = new WorkflowServiceRestImpl();
    workflowService.recomputeConceptStatus(projectId, "MATRIXINIT", false,
        authToken);
  }

  /**
   * Create and set up a NCI_2016_11D insertion process and algorithm
   * configurations
   *
   * @param project1 the project 1
   * @param projectId the project id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  private void createNciInsertionProcess(Project project1, Long projectId,
    String authToken) throws Exception {

    ProcessServiceRest process = new ProcessServiceRestImpl();

    ProcessConfig processConfig = new ProcessConfigJpa();
    processConfig.setDescription("Insertion process for NCI");
    processConfig.setFeedbackEmail(null);
    processConfig.setName("Insertion process for NCI");
    processConfig.setProject(project1);
    processConfig.setTerminology("NCI");
    processConfig.setVersion("2016_11D");
    processConfig.setTimestamp(new Date());
    processConfig.setType("Insertion");
    processConfig.setInputPath("inv/NCI_2016_11D/insert");
    processConfig = process.addProcessConfig(projectId,
        (ProcessConfigJpa) processConfig, authToken);
    process = new ProcessServiceRestImpl();

    AlgorithmConfig algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("PREINSERTION");
    algoConfig.setDescription("PREINSERTION Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("PREINSERTION algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("METADATALOADING");
    algoConfig.setDescription("METADATALOADING Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("METADATALOADING algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("ATOMLOADING");
    algoConfig.setDescription("ATOMLOADING Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("ATOMLOADING algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("RELATIONSHIPLOADING");
    algoConfig.setDescription("RELATIONSHIPLOADING Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("RELATIONSHIPLOADING algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("CONTEXTLOADING");
    algoConfig.setDescription("CONTEXTLOADING Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("CONTEXTLOADING algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("SEMANTICTYPELOADING");
    algoConfig.setDescription("SEMANTICTYPELOADING Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("SEMANTICTYPELOADING algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("ATTRIBUTELOADING");
    algoConfig.setDescription("ATTRIBUTELOADING Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("ATTRIBUTELOADING algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("PRECOMPUTEDMERGE");
    algoConfig.setDescription("PRECOMPUTEDMERGE Algorithm for NCI-SRC");
    algoConfig.setEnabled(true);
    algoConfig.setName("PRECOMPUTEDMERGE algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Set properties for the algorithm
    Map<String, String> algoProperties = new HashMap<String, String>();
    algoProperties.put("mergeSet", "NCI-SRC");
    algoProperties.put("checkNames", "");
    algoProperties.put("filterQueryType", null);
    algoProperties.put("filterQuery", null);
    algoConfig.setProperties(algoProperties);
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("PRECOMPUTEDMERGE");
    algoConfig.setDescription("PRECOMPUTEDMERGE Algorithm for NCI-SY");
    algoConfig.setEnabled(true);
    algoConfig.setName("PRECOMPUTEDMERGE algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Set properties for the algorithm
    algoProperties = new HashMap<String, String>();
    algoProperties.put("mergeSet", "NCI-SY");
    // Use all checks
    final Properties properties = ConfigUtility.getConfigProperties();
    String allChecks = properties.getProperty("validation.service.handler")
        .replaceAll(",", ";");
    algoProperties.put("checkNames", allChecks);
    algoProperties.put("filterQueryType", null);
    algoProperties.put("filterQuery", null);
    algoConfig.setProperties(algoProperties);
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("ADDREMOVEINTEGRITYCHECK");
    algoConfig.setDescription("ADDREMOVEINTEGRITYCHECK Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("ADDREMOVEINTEGRITYCHECK algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Set properties for the algorithm
    algoProperties = new HashMap<String, String>();
    algoProperties.put("addRemove", "Add");
    algoProperties.put("checkName", "MGV_SCUI");
    algoProperties.put("value1", "NCI");
    algoConfig.setProperties(algoProperties);
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("GENERATEDMERGE");
    algoConfig.setDescription("GENERATEDMERGE Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("GENERATEDMERGE algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Set properties for the algorithm
    algoProperties = new HashMap<String, String>();
    algoProperties.put("queryType", "JPQL");
    algoProperties.put("query",
        "select distinct a1.id, a2.id from ConceptJpa c1 join c1.atoms a1, "
            + "ConceptJpa c2 join c2.atoms a2 where c1.terminology = :projectTerminology "
            + "and c2.terminology = :projectTerminology and c1.id != c2.id and "
            + "a1.terminology = :terminology and a1.version = :version and "
            + "a1.publishable = true and a2.terminology = :terminology and "
            + "a2.version != :version and a2.publishable = true and a1.codeId = "
            + "a2.codeId and a1.lexicalClassId = a2.lexicalClassId and "
            + "a1.termType in (select tty.abbreviation from TermTypeJpa tty where "
            + "terminology = :projectTerminology and exclude = true) and a2.termType in "
            + "(select tty.abbreviation from TermTypeJpa tty where terminology = "
            + ":projectTerminology and exclude = true) and a1.termType in "
            + "(select tty.abbreviation from TermTypeJpa tty where terminology = "
            + ":projectTerminology and normExclude = true) and a2.termType in "
            + "(select tty.abbreviation from TermTypeJpa tty where terminology = "
            + ":projectTerminology and normExclude = true)");
    // Use all checks
    algoProperties.put("checkNames", allChecks);
    algoProperties.put("newAtomsOnly", "false");
    algoProperties.put("filterQueryType", "LUCENE");
    algoProperties.put("filterQuery",
        "atoms.terminology:SNOMEDCT_US AND atoms.name:\"Entire*\\(body structure\\)\"");
    algoProperties.put("makeDemotions", "true");
    algoProperties.put("changeStatus", "true");
    algoProperties.put("mergeSet", "NCI-REPL");
    algoConfig.setProperties(algoProperties);
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("SAFEREPLACE");
    algoConfig.setDescription("SAFEREPLACE Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("SAFEREPLACE algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Set properties for the algorithm
    algoProperties = new HashMap<String, String>();
    algoProperties.put("stringClassId", "false");
    algoProperties.put("lexicalClassId", "true");
    algoProperties.put("codeId", "true");
    algoProperties.put("conceptId", "false");
    algoProperties.put("descriptorId", "false");
    algoProperties.put("termType", "false");
    algoProperties.put("terminology", "");
    algoConfig.setProperties(algoProperties);
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("MIDMERGE");
    algoConfig.setDescription("MIDMERGE Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("MIDMERGE algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Set properties for the algorithm
    algoProperties = new HashMap<String, String>();
    algoProperties.put("queryType", "JPQL");
    algoProperties.put("query",
        "select distinct a1.id, a2.id from ConceptJpa c1 join c1.atoms a1, ConceptJpa c2 join c2.atoms a2 "
            + "where c1.terminology = :projectTerminology and c2.terminology = :projectTerminology "
            + "and c1.id != c2.id and a1.terminology = :terminology "
            + "and a1.version = :version and a1.workflowStatus = 'NEEDS_REVIEW' "
            + "and a1.publishable = true and a2.terminology != :terminology and a2.publishable = true "
            + "and a1.lexicalClassId = a2.lexicalClassId "
            + "and a1.termType in (select tty.abbreviation from TermTypeJpa tty where terminology = :projectTerminology and exclude = true) "
            + "and a2.termType in (select tty.abbreviation from TermTypeJpa tty where terminology = :projectTerminology and exclude = true) "
            + "and a1.termType in (select tty.abbreviation from TermTypeJpa tty where terminology = :projectTerminology and normExclude = true) "
            + "and a2.termType in (select tty.abbreviation from TermTypeJpa tty where terminology = :projectTerminology and normExclude = true)");
    // Use all checks
    algoProperties.put("checkNames", allChecks);
    algoProperties.put("newAtomsOnly", "true");
    algoProperties.put("filterQueryType", "LUCENE");
    algoProperties.put("filterQuery",
        "atoms.terminology:SNOMEDCT_US AND atoms.name:\"Entire*\\(body structure\\)\"");
    algoProperties.put("makeDemotions", "true");
    algoProperties.put("changeStatus", "true");
    algoProperties.put("mergeSet", "NCI-MID");
    algoConfig.setProperties(algoProperties);
    algoConfig.setProperties(algoProperties);
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("SEMANTICTYPERESOLVER");
    algoConfig.setDescription("SEMANTICTYPERESOLVER Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("SEMANTICTYPERESOLVER algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Set properties for the algorithm
    algoProperties = new HashMap<String, String>();
    algoProperties.put("winLose", "lose");
    algoConfig.setProperties(algoProperties);
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("UPDATERELEASABILITY");
    algoConfig.setDescription("UPDATERELEASABILITY Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("UPDATERELEASABILITY algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("BEQUEATH");
    algoConfig.setDescription("BEQUEATH Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("BEQUEATH algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("REPORTCHECKLIST");
    algoConfig.setDescription("REPORTCHECKLIST Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("REPORTCHECKLIST algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("MATRIXINIT");
    algoConfig.setDescription("MATRIXINIT Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("MATRIXINIT algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("REPARTITION");
    algoConfig.setDescription("REPARTITION Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("REPARTITION algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Set properties for the algorithm
    algoProperties = new HashMap<String, String>();
    algoProperties.put("type", "MUTUALLY_EXCLUSIVE");
    algoConfig.setProperties(algoProperties);
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("REINDEX");
    algoConfig.setDescription("REINDEX Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("REINDEX algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Set properties for the algorithm
    algoProperties = new HashMap<String, String>();
    algoProperties.put("indexedObjects",
        "ConceptRelationshipJpa,CodeRelationshipJpa,DescriptorRelationshipJpa");
    algoConfig.setProperties(algoProperties);
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("POSTINSERTION");
    algoConfig.setDescription("POSTINSERTION Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("POSTINSERTION algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    process.updateProcessConfig(projectId, (ProcessConfigJpa) processConfig,
        authToken);
  }

  /**
   * Create and set up a SNOMEDCT_US_2016_09_01 insertion process and algorithm
   * configurations
   *
   * @param project1 the project 1
   * @param projectId the project id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  private void createSnomedCtInsertionProcess(Project project1, Long projectId,
    String authToken) throws Exception {

    ProcessServiceRest process = new ProcessServiceRestImpl();

    ProcessConfig processConfig = new ProcessConfigJpa();
    processConfig.setDescription("Insertion process for SNOMEDCT_US");
    processConfig.setFeedbackEmail(null);
    processConfig.setName("Insertion process for SNOMEDCT_US");
    processConfig.setProject(project1);
    processConfig.setTerminology("SNOMEDCT_US");
    processConfig.setVersion("2016_09_01");
    processConfig.setTimestamp(new Date());
    processConfig.setType("Insertion");
    processConfig.setInputPath("inv/SNOMEDCT_US_2016_09_01/insert");
    processConfig = process.addProcessConfig(projectId,
        (ProcessConfigJpa) processConfig, authToken);
    process = new ProcessServiceRestImpl();

    AlgorithmConfig algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("PREINSERTION");
    algoConfig.setDescription("PREINSERTION Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("PREINSERTION algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("METADATALOADING");
    algoConfig.setDescription("METADATALOADING Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("METADATALOADING algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("ATOMLOADING");
    algoConfig.setDescription("ATOMLOADING Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("ATOMLOADING algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("RELATIONSHIPLOADING");
    algoConfig.setDescription("RELATIONSHIPLOADING Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("RELATIONSHIPLOADING algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("CONTEXTLOADING");
    algoConfig.setDescription("CONTEXTLOADING Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("CONTEXTLOADING algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("SEMANTICTYPELOADING");
    algoConfig.setDescription("SEMANTICTYPELOADING Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("SEMANTICTYPELOADING algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("MAPSETLOADING");
    algoConfig.setDescription("MAPSETLOADING Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("MAPSETLOADING algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("SUBSETLOADING");
    algoConfig.setDescription("SUBSETLOADING Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("SUBSETLOADING algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("ATTRIBUTELOADING");
    algoConfig.setDescription("ATTRIBUTELOADING Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("ATTRIBUTELOADING algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("PRECOMPUTEDMERGE");
    algoConfig.setDescription("PRECOMPUTEDMERGE Algorithm for SNOMEDCT_US-SRC");
    algoConfig.setEnabled(true);
    algoConfig.setName("PRECOMPUTEDMERGE algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Set properties for the algorithm
    Map<String, String> algoProperties = new HashMap<String, String>();
    algoProperties.put("mergeSet", "SNOMEDCT_US-SRC");
    algoProperties.put("checkNames", "");
    algoConfig.setProperties(algoProperties);
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("GENERATEDMERGE");
    algoConfig.setDescription("GENERATEDMERGE Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("GENERATEDMERGE algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Set properties for the algorithm
    algoProperties = new HashMap<String, String>();
    algoProperties.put("queryType", "JPQL");
    algoProperties.put("query",
        "select distinct a1.id, a2.id from ConceptJpa c1 join c1.atoms a1, "
            + "ConceptJpa c2 join c2.atoms a2 where c1.terminology = :projectTerminology "
            + "and c2.terminology = :projectTerminology and c1.id != c2.id and "
            + "a1.terminology = :terminology and a1.version = :version and "
            + "a1.publishable = true and a2.terminology = :terminology and "
            + "a2.version != :version and a2.publishable = true and a1.codeId = "
            + "a2.codeId and a1.lexicalClassId = a2.lexicalClassId and "
            + "a1.termType in (select tty.abbreviation from TermTypeJpa tty where "
            + "terminology = :projectTerminology and exclude = true) and a2.termType in "
            + "(select tty.abbreviation from TermTypeJpa tty where terminology = "
            + ":projectTerminology and exclude = true) and a1.termType in "
            + "(select tty.abbreviation from TermTypeJpa tty where terminology = "
            + ":projectTerminology and normExclude = true) and a2.termType in "
            + "(select tty.abbreviation from TermTypeJpa tty where terminology = "
            + ":projectTerminology and normExclude = true)");
    // Use all checks
    final Properties properties = ConfigUtility.getConfigProperties();
    String allChecks = properties.getProperty("validation.service.handler")
        .replaceAll(",", ";");
    algoProperties.put("checkNames", allChecks);
    algoProperties.put("newAtomsOnly", "false");
    algoProperties.put("filterQueryType", "");
    algoProperties.put("filterQuery", "");
    algoProperties.put("makeDemotions", "true");
    algoProperties.put("changeStatus", "true");
    algoProperties.put("mergeSet", "SNOMEDCT_US-REPL");
    algoConfig.setProperties(algoProperties);
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("PRECOMPUTEDMERGE");
    algoConfig
        .setDescription("PRECOMPUTEDMERGE Algorithm for SNOMEDCT_US-SCUI");
    algoConfig.setEnabled(true);
    algoConfig.setName("PRECOMPUTEDMERGE algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Set properties for the algorithm
    algoProperties = new HashMap<String, String>();
    algoProperties.put("mergeSet", "SNOMEDCT_US-SCUI");
    // Use all checks
    algoProperties.put("checkNames", allChecks);
    algoProperties.put("filterQueryType", "LUCENE");
    algoProperties.put("filterQuery",
        "atoms.name:\"SNOMED Clinical Terms version*\"");
    algoConfig.setProperties(algoProperties);
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("SAFEREPLACE");
    algoConfig.setDescription("SAFEREPLACE Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("SAFEREPLACE algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Set properties for the algorithm
    algoProperties = new HashMap<String, String>();
    algoProperties.put("stringClassId", "false");
    algoProperties.put("lexicalClassId", "true");
    algoProperties.put("codeId", "true");
    algoProperties.put("conceptId", "false");
    algoProperties.put("descriptorId", "false");
    algoProperties.put("termType", "false");
    algoProperties.put("terminology", "");
    algoConfig.setProperties(algoProperties);
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("MIDMERGE");
    algoConfig.setDescription("MIDMERGE Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("MIDMERGE algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Set properties for the algorithm
    algoProperties = new HashMap<String, String>();
    algoProperties.put("queryType", "JPQL");
    algoProperties.put("query",
        "select distinct a1.id, a2.id from ConceptJpa c1 join c1.atoms a1, "
            + "ConceptJpa c2 join c2.atoms a2 where c1.terminology = :projectTerminology "
            + "and c2.terminology = :projectTerminology and c1.id != c2.id and "
            + "a1.terminology = :terminology and a1.version = :version and "
            + "a1.workflowStatus = 'NEEDS_REVIEW' and a1.publishable = true and "
            + "a2.terminology != :terminology and a2.publishable = true and "
            + "a1.lexicalClassId = a2.lexicalClassId and a1.termType in (select "
            + "tty.abbreviation from TermTypeJpa tty where terminology = "
            + ":projectTerminology and exclude = true) and a2.termType in (select "
            + "tty.abbreviation from TermTypeJpa tty where terminology = "
            + ":projectTerminology and exclude = true) and a1.termType in (select "
            + "tty.abbreviation from TermTypeJpa tty where terminology = "
            + ":projectTerminology and normExclude = true) and a2.termType in (select "
            + "tty.abbreviation from TermTypeJpa tty where terminology = "
            + ":projectTerminology and normExclude = true)");
    // Use all checks
    algoProperties.put("checkNames", allChecks);
    algoProperties.put("newAtomsOnly", "true");
    algoProperties.put("filterQueryType", "LUCENE");
    algoProperties.put("filterQuery",
        "atoms.terminology:SNOMEDCT_US AND atoms.name:\"Entire*\\(body structure\\)\"");
    algoProperties.put("makeDemotions", "true");
    algoProperties.put("changeStatus", "true");
    algoProperties.put("mergeSet", "SNOMEDCT_US-MID");
    algoConfig.setProperties(algoProperties);
    algoConfig.setProperties(algoProperties);
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("SEMANTICTYPERESOLVER");
    algoConfig.setDescription("SEMANTICTYPERESOLVER Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("SEMANTICTYPERESOLVER algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Set properties for the algorithm
    algoProperties = new HashMap<String, String>();
    algoProperties.put("winLose", "lose");
    algoConfig.setProperties(algoProperties);
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("UPDATERELEASABILITY");
    algoConfig.setDescription("UPDATERELEASABILITY Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("UPDATERELEASABILITY algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("BEQUEATH");
    algoConfig.setDescription("BEQUEATH Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("BEQUEATH algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("REPORTCHECKLIST");
    algoConfig.setDescription("REPORTCHECKLIST Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("REPORTCHECKLIST algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("MATRIXINIT");
    algoConfig.setDescription("MATRIXINIT Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("MATRIXINIT algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("REPARTITION");
    algoConfig.setDescription("REPARTITION Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("REPARTITION algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Set properties for the algorithm
    algoProperties = new HashMap<String, String>();
    algoProperties.put("type", "MUTUALLY_EXCLUSIVE");
    algoConfig.setProperties(algoProperties);
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("REINDEX");
    algoConfig.setDescription("REINDEX Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("REINDEX algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Set properties for the algorithm
    algoProperties = new HashMap<String, String>();
    algoProperties.put("indexedObjects",
        "ConceptRelationshipJpa,CodeRelationshipJpa,DescriptorRelationshipJpa");
    algoConfig.setProperties(algoProperties);
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("POSTINSERTION");
    algoConfig.setDescription("POSTINSERTION Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("POSTINSERTION algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    process.updateProcessConfig(projectId, (ProcessConfigJpa) processConfig,
        authToken);
  }

  /**
   * Create and set up a UMLS insertion process and algorithms configurations
   *
   * @param project1 the project 1
   * @param projectId the project id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  private void createUmlsInsertionProcess(Project project1, Long projectId,
    String authToken) throws Exception {

    ProcessServiceRest process = new ProcessServiceRestImpl();

    ProcessConfig processConfig = new ProcessConfigJpa();
    processConfig.setDescription("Insertion process for MTH");
    processConfig.setFeedbackEmail(null);
    processConfig.setName("Insertion process for MTH");
    processConfig.setProject(project1);
    processConfig.setTerminology("MTH");
    processConfig.setVersion("2016AB");
    processConfig.setTimestamp(new Date());
    processConfig.setType("Insertion");
    processConfig.setInputPath("inv/MTH_2016AB/insert");
    processConfig = process.addProcessConfig(projectId,
        (ProcessConfigJpa) processConfig, authToken);
    process = new ProcessServiceRestImpl();

    AlgorithmConfig algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("PREINSERTION");
    algoConfig.setDescription("PREINSERTION Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("PREINSERTION algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("METADATALOADING");
    algoConfig.setDescription("METADATALOADING Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("METADATALOADING algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("ATOMLOADING");
    algoConfig.setDescription("ATOMLOADING Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("ATOMLOADING algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("RELATIONSHIPLOADING");
    algoConfig.setDescription("RELATIONSHIPLOADING Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("RELATIONSHIPLOADING algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("CONTEXTLOADING");
    algoConfig.setDescription("CONTEXTLOADING Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("CONTEXTLOADING algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("SEMANTICTYPELOADING");
    algoConfig.setDescription("SEMANTICTYPELOADING Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("SEMANTICTYPELOADING algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("ATTRIBUTELOADING");
    algoConfig.setDescription("ATTRIBUTELOADING Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("ATTRIBUTELOADING algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("GENERATEDMERGE");
    algoConfig.setDescription("GENERATEDMERGE Algorithm for META-SR");
    algoConfig.setEnabled(true);
    algoConfig.setName("GENERATEDMERGE algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Set properties for the algorithm
    Map<String, String> algoProperties = new HashMap<String, String>();
    algoProperties.put("queryType", "JPQL");
    algoProperties.put("query",
        "select a1.id, a2.id from ConceptJpa c1 join c1.atoms a1, "
            + "ConceptJpa c2 join c2.atoms a2 "
            + "where c1.terminology = :projectTerminology and c2.terminology = :projectTerminology "
            + "and c1.id != c2.id "
            + "and a1.publishable = true and a2.publishable = true "
            + "and a1.terminology = a2.terminology "
            + "and a1.version != a2.version " + "and a1.codeId = a2.codeId "
            + "and a1.stringClassId = a2.stringClassId "
            + "and a1.termType = a2.termType");
    // Use all checks
    final Properties properties = ConfigUtility.getConfigProperties();
    String allChecks = properties.getProperty("validation.service.handler")
        .replaceAll(",", ";");
    algoProperties.put("checkNames", allChecks);
    algoProperties.put("newAtomsOnly", "true");
    algoProperties.put("filterQueryType", "");
    algoProperties.put("filterQuery", "");
    algoProperties.put("makeDemotions", "true");
    algoProperties.put("changeStatus", "false");
    algoProperties.put("mergeSet", "META-SR");
    algoConfig.setProperties(algoProperties);
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("GENERATEDMERGE");
    algoConfig.setDescription("GENERATEDMERGE Algorithm for META-CUI");
    algoConfig.setEnabled(true);
    algoConfig.setName("GENERATEDMERGE algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Set properties for the algorithm
    algoProperties = new HashMap<String, String>();
    algoProperties.put("queryType", "SQL");
    algoProperties.put("query",
        "SELECT a1.atomId atomId1, a2.atomId atomId2 From (SELECT  "
            + "    a.id atomId, cid.conceptTerminologyIds CUI, c.id conceptId "
            + "FROM " + "    concepts c, " + "    concepts_atoms ca, "
            + "    atoms a, " + "    AtomJpa_conceptTerminologyIds cid "
            + "WHERE " + "    c.terminology = :projectTerminology "
            + "        AND c.id = ca.concepts_id "
            + "        AND ca.atoms_Id = a.id "
            + "        AND a.id = cid.AtomJpa_id "
            + "        AND a.publishable = TRUE "
            + "        AND cid.conceptTerminologyIds_KEY = :latestTerminologyVersion) "
            + "        a1, " + "        (SELECT  "
            + "    a.id atomId, cid.conceptTerminologyIds CUI, c.id conceptId "
            + "FROM " + "    concepts c, " + "    concepts_atoms ca, "
            + "    atoms a, " + "    AtomJpa_conceptTerminologyIds cid "
            + "WHERE " + "    c.terminology = :projectTerminology "
            + "        AND c.id = ca.concepts_id "
            + "        AND ca.atoms_Id = a.id "
            + "        AND a.id = cid.AtomJpa_id "
            + "        AND a.publishable = TRUE "
            + "        AND cid.conceptTerminologyIds_KEY = :previousTerminologyVersion) "
            + "        a2 " + "WHERE a1.CUI = a2.CUI "
            + "AND a1.conceptId != a2.conceptId");
    // Use all checks
    algoProperties.put("checkNames", allChecks);
    algoProperties.put("newAtomsOnly", "true");
    algoProperties.put("filterQueryType", "");
    algoProperties.put("filterQuery", "");
    algoProperties.put("makeDemotions", "true");
    algoProperties.put("changeStatus", "false");
    algoProperties.put("mergeSet", "META-CUI");
    algoConfig.setProperties(algoProperties);
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("PRECOMPUTEDMERGE");
    algoConfig.setDescription("PRECOMPUTEDMERGE Algorithm for META-MERGE");
    algoConfig.setEnabled(true);
    algoConfig.setName("PRECOMPUTEDMERGE algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Set properties for the algorithm
    algoProperties = new HashMap<String, String>();
    algoProperties.put("mergeSet", "META-MERGE");
    // Use all checks
    algoProperties.put("checkNames", allChecks);
    algoProperties.put("filterQueryType", null);
    algoProperties.put("filterQuery", null);
    algoConfig.setProperties(algoProperties);
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("PRECOMPUTEDMERGE");
    algoConfig.setDescription("PRECOMPUTEDMERGE Algorithm for META-AUI");
    algoConfig.setEnabled(true);
    algoConfig.setName("PRECOMPUTEDMERGE algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Set properties for the algorithm
    algoProperties = new HashMap<String, String>();
    algoProperties.put("mergeSet", "META-AUI");
    // Use all checks
    algoProperties.put("checkNames", allChecks);
    algoProperties.put("filterQueryType", null);
    algoProperties.put("filterQuery", null);
    algoConfig.setProperties(algoProperties);
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("SAFEREPLACE");
    algoConfig.setDescription("SAFEREPLACE Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("SAFEREPLACE algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Set properties for the algorithm
    algoProperties = new HashMap<String, String>();
    algoProperties.put("stringClassId", "true");
    algoProperties.put("lexicalClassId", "false");
    algoProperties.put("codeId", "true");
    algoProperties.put("conceptId", "false");
    algoProperties.put("descriptorId", "false");
    algoProperties.put("termType", "true");
    algoProperties.put("terminology", "");
    algoConfig.setProperties(algoProperties);
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("UPDATERELEASABILITY");
    algoConfig.setDescription("UPDATERELEASABILITY Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("UPDATERELEASABILITY algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("BEQUEATH");
    algoConfig.setDescription("BEQUEATH Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("BEQUEATH algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("SEMANTICTYPERESOLVER");
    algoConfig.setDescription("SEMANTICTYPERESOLVER Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("SEMANTICTYPERESOLVER algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Set properties for the algorithm
    algoProperties = new HashMap<String, String>();
    algoProperties.put("winLose", "win");
    algoConfig.setProperties(algoProperties);
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("REPORTCHECKLIST");
    algoConfig.setDescription("REPORTCHECKLIST Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("REPORTCHECKLIST algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("MATRIXINIT");
    algoConfig.setDescription("MATRIXINIT Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("MATRIXINIT algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("REPARTITION");
    algoConfig.setDescription("REPARTITION Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("REPARTITION algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Set properties for the algorithm
    algoProperties = new HashMap<String, String>();
    algoProperties.put("type", "MUTUALLY_EXCLUSIVE");
    algoConfig.setProperties(algoProperties);
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("REINDEX");
    algoConfig.setDescription("REINDEX Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("REINDEX algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Set properties for the algorithm
    algoProperties = new HashMap<String, String>();
    algoProperties.put("indexedObjects",
        "ConceptRelationshipJpa,CodeRelationshipJpa,DescriptorRelationshipJpa");
    algoConfig.setProperties(algoProperties);
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("POSTINSERTION");
    algoConfig.setDescription("POSTINSERTION Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("POSTINSERTION algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    process.updateProcessConfig(projectId, (ProcessConfigJpa) processConfig,
        authToken);
  }

  /**
   * Create and set up a template insertion process and algorithms configuration
   *
   * @param project1 the project 1
   * @param projectId the project id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  private void createTemplateInsertionProcess(Project project1, Long projectId,
    String authToken) throws Exception {

    ProcessServiceRest process = new ProcessServiceRestImpl();

    ProcessConfig processConfig = new ProcessConfigJpa();
    processConfig.setDescription("Template Insertion process");
    processConfig.setFeedbackEmail(null);
    processConfig.setName("Template Insertion process");
    processConfig.setProject(project1);
    processConfig.setTerminology("TERMINOLOGY");
    processConfig.setVersion("VERSION");
    processConfig.setTimestamp(new Date());
    processConfig.setType("Insertion");
    processConfig.setInputPath("inv/TERMINOLOGY_VERSION/insert");
    processConfig = process.addProcessConfig(projectId,
        (ProcessConfigJpa) processConfig, authToken);
    process = new ProcessServiceRestImpl();

    AlgorithmConfig algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("PREINSERTION");
    algoConfig.setDescription("PREINSERTION Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("PREINSERTION algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("METADATALOADING");
    algoConfig.setDescription("METADATALOADING Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("METADATALOADING algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("ATOMLOADING");
    algoConfig.setDescription("ATOMLOADING Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("ATOMLOADING algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("RELATIONSHIPLOADING");
    algoConfig.setDescription("RELATIONSHIPLOADING Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("RELATIONSHIPLOADING algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("CONTEXTLOADING");
    algoConfig.setDescription("CONTEXTLOADING Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("CONTEXTLOADING algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("SEMANTICTYPELOADING");
    algoConfig.setDescription("SEMANTICTYPELOADING Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("SEMANTICTYPELOADING algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("MAPSETLOADING");
    algoConfig.setDescription("MAPSETLOADING Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("MAPSETLOADING algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("SUBSETLOADING");
    algoConfig.setDescription("SUBSETLOADING Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("SUBSETLOADING algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("ATTRIBUTELOADING");
    algoConfig.setDescription("ATTRIBUTELOADING Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("ATTRIBUTELOADING algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("PRECOMPUTEDMERGE");
    algoConfig.setDescription("PRECOMPUTEDMERGE Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("PRECOMPUTEDMERGE algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Set properties for the algorithm
    Map<String, String> algoProperties = new HashMap<String, String>();
    algoProperties.put("mergeSet", "TERMINOLOGY-SRC");
    algoProperties.put("checkNames", "");
    algoProperties.put("filterQueryType", null);
    algoProperties.put("filterQuery", null);
    algoConfig.setProperties(algoProperties);
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("GENERATEDMERGE");
    algoConfig.setDescription("GENERATEDMERGE Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("GENERATEDMERGE algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Set properties for the algorithm
    algoProperties = new HashMap<String, String>();
    algoProperties.put("queryType", "JPQL");
    algoProperties.put("query",
        "select distinct a1.id, a2.id from ConceptJpa c1 join c1.atoms a1, "
            + "ConceptJpa c2 join c2.atoms a2 where c1.terminology = :projectTerminology "
            + "and c2.terminology = :projectTerminology and c1.id != c2.id and "
            + "a1.terminology = :terminology and a1.version = :version and "
            + "a1.publishable = true and a2.terminology = :terminology and "
            + "a2.version != :version and a2.publishable = true and a1.codeId = "
            + "a2.codeId and a1.lexicalClassId = a2.lexicalClassId and "
            + "a1.termType in (select tty.abbreviation from TermTypeJpa tty where "
            + "terminology = :projectTerminology and exclude = true) and a2.termType in "
            + "(select tty.abbreviation from TermTypeJpa tty where terminology = "
            + ":projectTerminology and exclude = true) and a1.termType in "
            + "(select tty.abbreviation from TermTypeJpa tty where terminology = "
            + ":projectTerminology and normExclude = true) and a2.termType in "
            + "(select tty.abbreviation from TermTypeJpa tty where terminology = "
            + ":projectTerminology and normExclude = true)");
    // Use all checks
    final Properties properties = ConfigUtility.getConfigProperties();
    String allChecks = properties.getProperty("validation.service.handler")
        .replaceAll(",", ";");
    algoProperties.put("checkNames", allChecks);
    algoProperties.put("newAtomsOnly", "false");
    algoProperties.put("filterQueryType", "");
    algoProperties.put("filterQuery", "");
    algoProperties.put("makeDemotions", "true");
    algoProperties.put("changeStatus", "true");
    algoProperties.put("mergeSet", "TERMINOLOGY-REPL");
    algoConfig.setProperties(algoProperties);
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("SAFEREPLACE");
    algoConfig.setDescription("SAFEREPLACE Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("SAFEREPLACE algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Set properties for the algorithm
    algoProperties = new HashMap<String, String>();
    algoProperties.put("stringClassId", "false");
    algoProperties.put("lexicalClassId", "true");
    algoProperties.put("codeId", "true");
    algoProperties.put("conceptId", "false");
    algoProperties.put("descriptorId", "false");
    algoProperties.put("termType", "true");
    algoProperties.put("terminology", "");
    algoConfig.setProperties(algoProperties);
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("MIDMERGE");
    algoConfig.setDescription("MIDMERGE Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("MIDMERGE algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Set properties for the algorithm
    algoProperties = new HashMap<String, String>();
    algoProperties.put("queryType", "JPQL");
    algoProperties.put("query",
        "select distinct a1.id, a2.id from ConceptJpa c1 join c1.atoms a1, ConceptJpa c2 join c2.atoms a2 where c1.terminology = :projectTerminology and c2.terminology = :projectTerminology and c1.id != c2.id and a1.terminology = :terminology and a1.version = :version and a1.workflowStatus = 'NEEDS_REVIEW' and a1.publishable = true and a2.terminology != :terminology and a2.publishable = true and a1.lexicalClassId = a2.lexicalClassId and a1.termType in (select tty.abbreviation from TermTypeJpa tty where terminology = :projectTerminology and exclude = true) and a2.termType in (select tty.abbreviation from TermTypeJpa tty where terminology = :projectTerminology and exclude = true) and a1.termType in (select tty.abbreviation from TermTypeJpa tty where terminology = :projectTerminology and normExclude = true) and a2.termType in (select tty.abbreviation from TermTypeJpa tty where terminology = :projectTerminology and normExclude = true)");
    // Use all checks
    algoProperties.put("checkNames", allChecks);
    algoProperties.put("newAtomsOnly", "true");
    algoProperties.put("filterQueryType", "LUCENE");
    algoProperties.put("filterQuery", "");
    algoProperties.put("makeDemotions", "true");
    algoProperties.put("changeStatus", "true");
    algoProperties.put("mergeSet", "TERMINOLOGY-MID");
    algoConfig.setProperties(algoProperties);
    algoConfig.setProperties(algoProperties);
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("SEMANTICTYPERESOLVER");
    algoConfig.setDescription("SEMANTICTYPERESOLVER Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("SEMANTICTYPERESOLVER algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Set properties for the algorithm
    algoProperties = new HashMap<String, String>();
    algoProperties.put("winLose", "lose");
    algoConfig.setProperties(algoProperties);
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("UPDATERELEASABILITY");
    algoConfig.setDescription("UPDATERELEASABILITY Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("UPDATERELEASABILITY algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("BEQUEATH");
    algoConfig.setDescription("BEQUEATH Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("BEQUEATH algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("REPORTCHECKLIST");
    algoConfig.setDescription("REPORTCHECKLIST Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("REPORTCHECKLIST algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("MATRIXINIT");
    algoConfig.setDescription("MATRIXINIT Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("MATRIXINIT algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("REPARTITION");
    algoConfig.setDescription("REPARTITION Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("REPARTITION algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Set properties for the algorithm
    algoProperties = new HashMap<String, String>();
    algoProperties.put("type", "MUTUALLY_EXCLUSIVE");
    algoConfig.setProperties(algoProperties);
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("POSTINSERTION");
    algoConfig.setDescription("POSTINSERTION Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("POSTINSERTION algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    process.updateProcessConfig(projectId, (ProcessConfigJpa) processConfig,
        authToken);
  }

  /**
   * Create and set up a PreProduction process
   *
   * @param project1 the project 1
   * @param projectId the project id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  private void createPreProductionProcess(Project project1, Long projectId,
    String authToken) throws Exception {

    ProcessServiceRest process = new ProcessServiceRestImpl();

    ProcessConfig processConfig = new ProcessConfigJpa();
    processConfig.setDescription("Pre-Production Process");
    processConfig.setFeedbackEmail(null);
    processConfig.setName("Pre-Production Process");
    processConfig.setProject(project1);
    processConfig.setTerminology(project1.getTerminology());
    processConfig.setVersion("201611");
    processConfig.setTimestamp(new Date());
    processConfig.setType("Release");
    processConfig.setInputPath("mr");
    processConfig = process.addProcessConfig(projectId,
        (ProcessConfigJpa) processConfig, authToken);
    process = new ProcessServiceRestImpl();

    AlgorithmConfig algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("CREATENEWRELEASE");
    algoConfig.setDescription("CREATENEWRELEASE Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("CREATENEWRELEASE algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Set properties for the algorithm
    Map<String, String> algoProperties = new HashMap<String, String>();
    algoProperties.put("warnValidationChecks", "true");
    algoConfig.setProperties(algoProperties);
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("PREFNAMES");
    algoConfig.setDescription("PREFNAMES Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("PREFNAMES algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("CREATENDCPDQMAP");
    algoConfig.setDescription("CREATENDCPDQMAP Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("CREATENDCPDQMAP algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("ASSIGNRELEASEIDS");
    algoConfig.setDescription("ASSIGNRELEASEIDS Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("ASSIGNRELEASEIDS algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("CONTEXTTYPE");
    algoConfig.setDescription("CONTEXTTYPE Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("CONTEXTTYPE algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Set properties for the algorithm
    algoProperties = new HashMap<String, String>();
    algoProperties.put("siblingsThreshold", "100");
    algoConfig.setProperties(algoProperties);
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("METAMORPHOSYS");
    algoConfig.setDescription("METAMORPHOSYS Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("METAMORPHOSYS algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("MATRIXINIT");
    algoConfig.setDescription("MATRIXINIT Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("MATRIXINIT algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    process.updateProcessConfig(projectId, (ProcessConfigJpa) processConfig,
        authToken);
  }

  /**
   * Create and set up a release process
   *
   * @param project1 the project 1
   * @param projectId the project id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  private void createReleaseProcess(Project project1, Long projectId,
    String authToken) throws Exception {

    ProcessServiceRest process = new ProcessServiceRestImpl();

    ProcessConfig processConfig = new ProcessConfigJpa();
    processConfig.setDescription("Release Process");
    processConfig.setFeedbackEmail(null);
    processConfig.setName("Release Process");
    processConfig.setProject(project1);
    processConfig.setTerminology(project1.getTerminology());
    processConfig.setVersion("201611");
    processConfig.setTimestamp(new Date());
    processConfig.setType("Release");
    processConfig.setInputPath("mr");
    processConfig = process.addProcessConfig(projectId,
        (ProcessConfigJpa) processConfig, authToken);
    process = new ProcessServiceRestImpl();

    AlgorithmConfig algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("RRFMETADATA");
    algoConfig.setDescription("RRFMETADATA Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("RRFMETADATA algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("RRFCONTENT");
    algoConfig.setDescription("RRFCONTENT Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("RRFCONTENT algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("RRFHISTORY");
    algoConfig.setDescription("RRFHISTORY Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("RRFHISTORY algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("RRFINDEX");
    algoConfig.setDescription("RRFINDEX Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("RRFINDEX algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("NCIMETA");
    algoConfig.setDescription("NCIMETA Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("NCIMETA algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    // TODO - once it's available, add ValidateReleaseAlgorithm

    // TODO - once it's available, add RunMetamorphoSysAlgorithm
    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("RUNMMSYS");
    algoConfig.setDescription("RUNMMSYS Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("RUNMMSYS algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    // TODO - once it's available, add PackageReleaseAlgorithm

    process.updateProcessConfig(projectId, (ProcessConfigJpa) processConfig,
        authToken);
  }

  /**
   * Create and set up a feedback process
   *
   * @param project1 the project 1
   * @param projectId the project id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  private void createFeedbackProcess(Project project1, Long projectId,
    String authToken) throws Exception {

    ProcessServiceRest process = new ProcessServiceRestImpl();

    ProcessConfig processConfig = new ProcessConfigJpa();
    processConfig.setDescription("Feedback Process");
    processConfig.setFeedbackEmail(null);
    processConfig.setName("Feedback Process");
    processConfig.setProject(project1);
    processConfig.setTerminology(project1.getTerminology());
    processConfig.setVersion("201611");
    processConfig.setTimestamp(new Date());
    processConfig.setType("Release");
    processConfig.setInputPath("mr");
    processConfig = process.addProcessConfig(projectId,
        (ProcessConfigJpa) processConfig, authToken);
    process = new ProcessServiceRestImpl();

    AlgorithmConfig algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("PREFNAMES");
    algoConfig.setDescription("PREFNAMES Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("PREFNAMES algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("RELOADHISTORY");
    algoConfig.setDescription("RELOADHISTORY Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("RELOADHISTORY algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("FEEDBACKRELEASE");
    algoConfig.setDescription("FEEDBACKRELEASE Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("FEEDBACKRELEASE algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    process.updateProcessConfig(projectId, (ProcessConfigJpa) processConfig,
        authToken);
  }

  /**
   * Create and set up a ProdMid Cleanup process
   *
   * @param project1 the project 1
   * @param projectId the project id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  private void createProdMidCleanupProcess(Project project1, Long projectId,
    String authToken) throws Exception {

    ProcessServiceRest process = new ProcessServiceRestImpl();

    ProcessConfig processConfig = new ProcessConfigJpa();
    processConfig.setDescription("ProdMid Cleanup Process");
    processConfig.setFeedbackEmail(null);
    processConfig.setName("ProdMid Cleanup Process");
    processConfig.setProject(project1);
    processConfig.setTerminology(project1.getTerminology());
    processConfig.setVersion("201611");
    processConfig.setTimestamp(new Date());
    processConfig.setType("Release");
    processConfig.setInputPath("mr");
    processConfig = process.addProcessConfig(projectId,
        (ProcessConfigJpa) processConfig, authToken);
    process = new ProcessServiceRestImpl();

    AlgorithmConfig algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("UPDATEPUBLISHED");
    algoConfig.setDescription("UPDATEPUBLISHED Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("UPDATEPUBLISHED algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("PRODMIDCLEANUP");
    algoConfig.setDescription("PRODMIDCLEANUP Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("PRODMIDCLEANUP algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("PREFNAMES");
    algoConfig.setDescription("PREFNAMES Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("PREFNAMES algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("REINDEX");
    algoConfig.setDescription("REINDEX Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("REINDEX algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Set properties for the algorithm
    final Map<String, String> algoProperties = new HashMap<String, String>();
    algoProperties.put("indexedObjects",
        "ConceptRelationshipJpa,CodeRelationshipJpa,DescriptorRelationshipJpa,ConceptJpa,DescriptorJpa,CodeJpa");
    algoConfig.setProperties(algoProperties);
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    process.updateProcessConfig(projectId, (ProcessConfigJpa) processConfig,
        authToken);
  }

  /**
   * Create and set up a "daily editing report" and "mid validation report"
   * process.
   *
   * @param project1 the project 1
   * @param projectId the project id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  private void createReportProcesses(Project project1, Long projectId,
    String authToken) throws Exception {

    ProcessServiceRest process = new ProcessServiceRestImpl();
    ProcessConfig processConfig = new ProcessConfigJpa();
    processConfig.setDescription("Daily Editing Report");
    processConfig.setFeedbackEmail(null);
    processConfig.setName("Daily Editing Report");
    processConfig.setProject(project1);
    processConfig.setTerminology(project1.getTerminology());
    processConfig.setVersion(project1.getVersion());
    processConfig.setTimestamp(new Date());
    processConfig.setType("Report");
    processConfig = process.addProcessConfig(projectId,
        (ProcessConfigJpa) processConfig, authToken);
    process = new ProcessServiceRestImpl();

    AlgorithmConfig algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("DAILYEDITING");
    algoConfig.setDescription("Daily Editing Report Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("Daily Editing Report Algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    processConfig.getSteps().add(algoConfig);

    process = new ProcessServiceRestImpl();
    process.updateProcessConfig(projectId, (ProcessConfigJpa) processConfig,
        authToken);

    // MID V

    process = new ProcessServiceRestImpl();

    processConfig = new ProcessConfigJpa();
    processConfig.setDescription("MID Validation Report");
    processConfig.setFeedbackEmail(null);
    processConfig.setName("MID Validation Report");
    processConfig.setProject(project1);
    processConfig.setTerminology(project1.getTerminology());
    processConfig.setVersion(project1.getVersion());
    processConfig.setTimestamp(new Date());
    processConfig.setType("Report");
    processConfig = process.addProcessConfig(projectId,
        (ProcessConfigJpa) processConfig, authToken);
    process = new ProcessServiceRestImpl();

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("MIDVALIDATION");
    algoConfig.setDescription("MID Validation Report Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("MID Validation Report Algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    process.updateProcessConfig(projectId, (ProcessConfigJpa) processConfig,
        authToken);

  }

  /**
   * Create and set up a Lexical Class Assignment process
   *
   * @param project1 the project 1
   * @param projectId the project id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  private void createLexicalClassAssignmentProcess(Project project1,
    Long projectId, String authToken) throws Exception {

    ProcessServiceRest process = new ProcessServiceRestImpl();

    ProcessConfig processConfig = new ProcessConfigJpa();
    processConfig.setDescription("Lexical Class Assignment Process");
    processConfig.setFeedbackEmail(null);
    processConfig.setName("Lexical Class Assignment Process");
    processConfig.setProject(project1);
    processConfig.setTerminology(project1.getTerminology());
    processConfig.setVersion(project1.getVersion());
    processConfig.setTimestamp(new Date());
    processConfig.setType("Maintenance");
    processConfig = process.addProcessConfig(projectId,
        (ProcessConfigJpa) processConfig, authToken);
    process = new ProcessServiceRestImpl();

    AlgorithmConfig algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("LEXICALCLASSASSIGNMENT");
    algoConfig.setDescription("LEXICALCLASSASSIGNMENT Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("LEXICALCLASSASSIGNMENT algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    process.updateProcessConfig(projectId, (ProcessConfigJpa) processConfig,
        authToken);
  }

  /**
   * Creates the remap component info relationships processes.
   *
   * @param project1 the project 1
   * @param projectId the project id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  private void createRemapComponentInfoRelationshipsProcesses(Project project1,
    Long projectId, String authToken) throws Exception {

    // This will make two processes, one Insertion, and one Maintenance
    ProcessServiceRest process = new ProcessServiceRestImpl();

    ProcessConfig processConfig = new ProcessConfigJpa();
    processConfig.setDescription("Remap Component Info Relationships Process");
    processConfig.setFeedbackEmail(null);
    processConfig.setName("Remap Component Info Relationships Process");
    processConfig.setProject(project1);
    processConfig.setTerminology(project1.getTerminology());
    processConfig.setVersion(project1.getVersion());
    processConfig.setTimestamp(new Date());
    processConfig.setType("Insertion");
    processConfig = process.addProcessConfig(projectId,
        (ProcessConfigJpa) processConfig, authToken);
    process = new ProcessServiceRestImpl();

    AlgorithmConfig algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("COMPINFORELREMAPPER");
    algoConfig.setDescription("COMPINFORELREMAPPER Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("COMPINFORELREMAPPER algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    process.updateProcessConfig(projectId, (ProcessConfigJpa) processConfig,
        authToken);

    // Now for the Maintenance one
    process = new ProcessServiceRestImpl();

    ProcessConfig processConfig2 = new ProcessConfigJpa();
    processConfig2.setDescription("Remap Component Info Relationships Process");
    processConfig2.setFeedbackEmail(null);
    processConfig2.setName("Remap Component Info Relationships Process");
    processConfig2.setProject(project1);
    processConfig2.setTerminology(project1.getTerminology());
    processConfig2.setVersion(project1.getVersion());
    processConfig2.setTimestamp(new Date());
    processConfig2.setType("Maintenance");
    processConfig2 = process.addProcessConfig(projectId,
        (ProcessConfigJpa) processConfig2, authToken);
    process = new ProcessServiceRestImpl();

    AlgorithmConfig algoConfig2 = new AlgorithmConfigJpa();
    algoConfig2.setAlgorithmKey("COMPINFORELREMAPPER");
    algoConfig2.setDescription("COMPINFORELREMAPPER Algorithm");
    algoConfig2.setEnabled(true);
    algoConfig2.setName("COMPINFORELREMAPPER algorithm");
    algoConfig2.setProcess(processConfig2);
    algoConfig2.setProject(project1);
    algoConfig2.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig2 = process.addAlgorithmConfig(projectId, processConfig2.getId(),
        (AlgorithmConfigJpa) algoConfig2, authToken);
    process = new ProcessServiceRestImpl();
    processConfig2.getSteps().add(algoConfig2);

    process.updateProcessConfig(projectId, (ProcessConfigJpa) processConfig2,
        authToken);
  }

  /**
   * Create and set up a Compute Preferred Names process
   *
   * @param project1 the project 1
   * @param projectId the project id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  private void createComputePreferredNamesProcess(Project project1,
    Long projectId, String authToken) throws Exception {

    ProcessServiceRest process = new ProcessServiceRestImpl();

    ProcessConfig processConfig = new ProcessConfigJpa();
    processConfig.setDescription("Compute Preferred Names Process");
    processConfig.setFeedbackEmail(null);
    processConfig.setName("Compute Preferred Names Process");
    processConfig.setProject(project1);
    processConfig.setTerminology(project1.getTerminology());
    processConfig.setVersion(project1.getVersion());
    processConfig.setTimestamp(new Date());
    processConfig.setType("Maintenance");
    processConfig = process.addProcessConfig(projectId,
        (ProcessConfigJpa) processConfig, authToken);
    process = new ProcessServiceRestImpl();

    AlgorithmConfig

    algoConfig = new AlgorithmConfigJpa();
    algoConfig.setAlgorithmKey("PREFNAMES");
    algoConfig.setDescription("PREFNAMES Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("PREFNAMES algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Add algorithm and insert as step into process
    algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
        (AlgorithmConfigJpa) algoConfig, authToken);
    process = new ProcessServiceRestImpl();
    processConfig.getSteps().add(algoConfig);

    process.updateProcessConfig(projectId, (ProcessConfigJpa) processConfig,
        authToken);
  }

  /**
   * Returns the semantic type category map.
   *
   * @param authToken the auth token
   * @return the semantic type category map
   * @throws Exception the exception
   */
  private Map<String, String> getSemanticTypeCategoryMap(String authToken)
    throws Exception {
    final Map<String, String> map = new HashMap<>();
    final MetadataServiceRest service = new MetadataServiceRestImpl();
    try {
      final SemanticTypeList styList =
          service.getSemanticTypes(terminology, version, authToken);
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
      // n/a
    }
    return map;
  }

  /**
   * Make user.
   *
   * @param userName the user name
   * @param name the name
   * @return the user
   */
  @SuppressWarnings("static-method")
  private UserJpa makeUser(String userName, String name) {
    final UserJpa user = new UserJpa();
    user.setUserName(userName);
    user.setName(name);
    user.setEmail(userName + "@example.com");
    user.setApplicationRole(UserRole.USER);
    user.setTeam("MSC");
    return user;
  }

}
