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
        "CFC", "BAC", "DSS", "JFW", "LWW", "MWH", "SDC", "GFG", "LAR", "LLW",
        "TAQ", "GSC", "HAG", "TPW", "MJA"
    };
    final String[] names = new String[] {
        "Carol Creech", "Brian Carlsen", "Deborah Shapiro", "Joanne Wong",
        "Larry Wright", "Margaret Haber", "Sherri de Coronado",
        "Gilberto Fragoso", "Laura Roth", "Lori Whiteman", "Theresa Quinn",
        "George Chang", "Alpha Garret", "Tammy Powell", "Miranda Jarnot"
    };
    final int[] editorLevels = new int[] {
        0, 0, 0, 0, 0, 0, 0, 0, 5, 5, 0, 5, 0, 5, 5
    };
    final String[] roles = new String[] {
        "AUTHOR", "ADMINISTRATOR", "ADMINISTRATOR", "ADMINISTRATOR", "REVIEWER",
        "REVIEWER", "REVIEWER", "ADMINISTRATOR", "REVIEWER", "REVIEWER",
        "AUTHOR", "AUTHOR", "AUTHOR", "REVIEWER", "REVIEWER"
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
    List<String> newAtomTermgroups = new ArrayList<>();
    newAtomTermgroups.add("MTH/PN");
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
            project1.getTerminology(), "latest", authToken));
    list.setId(null);
    metadataService = new MetadataServiceRestImpl();
    list =
        (PrecedenceListJpa) metadataService.addPrecedenceList(list, authToken);
    project1.setPrecedenceList(list);

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

    // Create and set up a test NCI_2016_05E insertion process and algorithm
    // configuration
    createNciInsertionProcess(project1, projectId, authToken);

    // Create and set up a release process and algorithm configuration for
    // testing
    /*
     * TODO add and test processConfig = new ProcessConfigJpa();
     * processConfig.setDescription("Process for release testing use");
     * processConfig.setFeedbackEmail(null); processConfig.setName(
     * "Test Release Process"); processConfig.setProject(project1);
     * processConfig.setTerminology(terminology);
     * processConfig.setVersion(version); processConfig.setTimestamp(new
     * Date()); processConfig.setType("Release"); processConfig =
     * process.addProcessConfig(projectId, (ProcessConfigJpa) processConfig,
     * authToken); process = new ProcessServiceRestImpl();
     * 
     * algoConfig = new AlgorithmConfigJpa();
     * algoConfig.setAlgorithmKey("RRFHISTORY"); algoConfig.setDescription(
     * "Rrf history algorithm for testing use"); algoConfig.setEnabled(true);
     * algoConfig.setName("Test RRF History algorithm");
     * algoConfig.setProcess(processConfig); algoConfig.setProject(project1);
     * algoConfig.setTerminology(terminology); algoConfig.setTimestamp(new
     * Date()); algoConfig.setVersion(version);
     * 
     * algoConfig = process.addAlgorithmConfig(projectId, processConfig.getId(),
     * (AlgorithmConfigJpa) algoConfig, authToken); process = new
     * ProcessServiceRestImpl();
     * 
     * processConfig.getSteps().add(algoConfig);
     * process.updateProcessConfig(projectId, (ProcessConfigJpa) processConfig,
     * authToken);
     */

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
          testService.updateAtom((AtomJpa) atom, authToken);
        }
      }
      testService = new IntegrationTestServiceRestImpl();
      testService.updateConcept(concept, authToken);
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
          testService.updateAtom((AtomJpa) atom, authToken);
        }
      }
      testService = new IntegrationTestServiceRestImpl();
      testService.updateConcept(concept, authToken);
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
          testService.updateAtom((AtomJpa) atom, authToken);
        }
      }

      testService = new IntegrationTestServiceRestImpl();
      testService.updateConcept(concept, authToken);
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

    // Matrix initializer
    workflowService = new WorkflowServiceRestImpl();
    workflowService.recomputeConceptStatus(projectId, "MATRIXINIT", authToken);
  }

  /**
   * Creates the NCI insertion process.
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
    processConfig.setDescription("Insertion process for NCI_2016_05E");
    processConfig.setFeedbackEmail(null);
    processConfig.setName("Insertion process for NCI_2016_05E");
    processConfig.setProject(project1);
    processConfig.setTerminology("NCI");
    processConfig.setVersion("2016_05E");
    processConfig.setTimestamp(new Date());
    processConfig.setType("Insertion");
    processConfig.setInputPath("inv/NCI_2016_05E/insert");
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
    algoConfig.setDescription("PRECOMPUTEDMERGE Algorithm");
    algoConfig.setEnabled(true);
    algoConfig.setName("PRECOMPUTEDMERGE algorithm");
    algoConfig.setProcess(processConfig);
    algoConfig.setProject(project1);
    algoConfig.setTimestamp(new Date());
    // Set properties for the algorithm
    Map<String, String> algoProperties = new HashMap<String, String>();
    algoProperties.put("mergeSet", "NCI-SRC");
    algoProperties.put("checkNames", "");
    algoConfig.setProperties(algoProperties);
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
    algoProperties = new HashMap<String, String>();
    algoProperties.put("mergeSet", "NCI-SY");
    algoProperties.put("checkNames", "");
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
    algoProperties.put("queryType", "JQL");
    algoProperties.put("query",
        "select distinct a1.id, a2.id from ConceptJpa c1 join c1.atoms a1, ConceptJpa c2 join c2.atoms a2 where c1.terminology = :projectTerminology   and c2.terminology = :projectTerminology   and c1.id != c2.id   and a1.terminology = :terminology   and a1.version = :version and a1.publishable = true   and a2.terminology = :terminology   and a2.version != :version and a2.publishable = true   and a1.codeId = a2.codeId   and a1.lexicalClassId = a2.lexicalClassId   and a1.termType in (select tty.abbreviation from TermTypeJpa tty where terminology = :projectTerminology and exclude = true)   and a2.termType in (select tty.abbreviation from TermTypeJpa tty where terminology = :projectTerminology and exclude = true)   and a1.termType in (select tty.abbreviation from TermTypeJpa tty where terminology = :projectTerminology and normExclude = true)   and a2.termType in (select tty.abbreviation from TermTypeJpa tty where terminology = :projectTerminology and normExclude = true)");
    algoProperties.put("checkNames", null);
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
    algoProperties.put("queryType", "JQL");
    algoProperties.put("query",
        "select distinct a1.id, a2.id from ConceptJpa c1 join c1.atoms a1, ConceptJpa c2 join c2.atoms a2 where c1.terminology = :projectTerminology and c2.terminology = :projectTerminology and c1.id != c2.id and a1.terminology = :terminology and a1.version = :version and a1.workflowStatus = 'NEEDS_REVIEW' and a1.publishable = true and a2.terminology != :terminology and a2.publishable = true and a1.lexicalClassId = a2.lexicalClassId and a1.termType in (select tty.abbreviation from TermTypeJpa tty where terminology = :projectTerminology and exclude = true) and a2.termType in (select tty.abbreviation from TermTypeJpa tty where terminology = :projectTerminology and exclude = true) and a1.termType in (select tty.abbreviation from TermTypeJpa tty where terminology = :projectTerminology and normExclude = true) and a2.termType in (select tty.abbreviation from TermTypeJpa tty where terminology = :projectTerminology and normExclude = true)");
    algoProperties.put("checkNames", null);
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
