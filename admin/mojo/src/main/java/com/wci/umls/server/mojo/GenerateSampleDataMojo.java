/**
 * Copyright 2016 West Coast Informatics, LLC
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;

import com.wci.umls.server.UserRole;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.meta.SemanticTypeList;
import com.wci.umls.server.jpa.ProjectJpa;
import com.wci.umls.server.jpa.UserJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.ConceptRelationshipJpa;
import com.wci.umls.server.jpa.helpers.PrecedenceListJpa;
import com.wci.umls.server.jpa.services.MetadataServiceJpa;
import com.wci.umls.server.jpa.services.ProjectServiceJpa;
import com.wci.umls.server.jpa.services.SecurityServiceJpa;
import com.wci.umls.server.jpa.services.rest.ContentServiceRest;
import com.wci.umls.server.jpa.services.rest.IntegrationTestServiceRest;
import com.wci.umls.server.jpa.services.rest.MetadataServiceRest;
import com.wci.umls.server.jpa.services.rest.ProjectServiceRest;
import com.wci.umls.server.jpa.services.rest.SecurityServiceRest;
import com.wci.umls.server.jpa.worfklow.WorkflowBinDefinitionJpa;
import com.wci.umls.server.jpa.worfklow.WorkflowConfigJpa;
import com.wci.umls.server.jpa.worfklow.WorkflowEpochJpa;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.meta.SemanticType;
import com.wci.umls.server.model.workflow.QueryType;
import com.wci.umls.server.model.workflow.WorkflowBinType;
import com.wci.umls.server.model.workflow.WorkflowConfig;
import com.wci.umls.server.model.workflow.WorkflowStatus;
import com.wci.umls.server.rest.impl.ContentServiceRestImpl;
import com.wci.umls.server.rest.impl.HistoryServiceRestImpl;
import com.wci.umls.server.rest.impl.IntegrationTestServiceRestImpl;
import com.wci.umls.server.rest.impl.MetadataServiceRestImpl;
import com.wci.umls.server.rest.impl.ProjectServiceRestImpl;
import com.wci.umls.server.rest.impl.SecurityServiceRestImpl;
import com.wci.umls.server.rest.impl.WorkflowServiceRestImpl;
import com.wci.umls.server.services.MetadataService;
import com.wci.umls.server.services.SecurityService;

/**
 * Goal which generates sample data for the default dev build. This uses REST
 * services directly and not through the client.
 * 
 * See admin/loader/pom.xml for sample usage
 * 
 * @goal generate-sample-data
 * @phase package
 */
public class GenerateSampleDataMojo extends AbstractMojo {

  /**
   * Mode - for recreating db.
   *
   * @parameter
   */
  private String mode = null;

  /** The terminology. */
  private final String terminology = "UMLS";

  /** The version. */
  private final String version = "latest";

  /** The next release. */
  private final String nextRelease = "2016AB";

  /**
   * Instantiates a {@link GenerateSampleDataMojo} from the specified
   * parameters.
   */
  public GenerateSampleDataMojo() {
    // do nothing
  }

  /* see superclass */
  @Override
  public void execute() throws MojoFailureException {
    getLog().info("Generating sample data");

    try {

      getLog().info("Generate sample data");
      getLog().info("  mode = " + mode);

      // Handle creating the database if the mode parameter is set
      final Properties properties = ConfigUtility.getConfigProperties();
      if (mode != null && mode.equals("create")) {
        getLog().info("Recreate database");
        // This will trigger a rebuild of the db
        properties.setProperty("hibernate.hbm2ddl.auto", mode);
        // Trigger a JPA event
        new ProjectServiceJpa().close();
        properties.remove("hibernate.hbm2ddl.auto");
      }

      // authenticate
      final SecurityService service = new SecurityServiceJpa();
      final String authToken =
          service.authenticate(properties.getProperty("admin.user"),
              properties.getProperty("admin.password")).getAuthToken();
      service.close();

      // Handle reindexing database if mode is set
      if (mode != null && mode.equals("create")) {
        ContentServiceRestImpl contentService = new ContentServiceRestImpl();
        contentService.luceneReindex(null, authToken);
      }

      boolean serverRunning = ConfigUtility.isServerActive();
      getLog().info(
          "Server status detected:  " + (!serverRunning ? "DOWN" : "UP"));
      if (serverRunning) {
        throw new Exception("Server must not be running to generate data");
      }

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

    //
    // Add admin users
    //
    Logger.getLogger(getClass()).info("Add new admin users");
    UserJpa admin1 = makeUser("admin1", "Admin1");
    admin1 = (UserJpa) security.addUser(admin1, authToken);
    UserJpa admin2 = makeUser("admin3", "Admin2");
    admin2 = (UserJpa) security.addUser(admin2, authToken);
    UserJpa admin3 = makeUser("admin2", "Admin3");
    admin3 = (UserJpa) security.addUser(admin3, authToken);

    //
    // Add reviewer users
    //
    Logger.getLogger(getClass()).info("Add new reviewer users");
    UserJpa reviewer1 = makeUser("reviewer1", "Reviewer1");
    reviewer1 = (UserJpa) security.addUser(reviewer1, authToken);
    UserJpa reviewer2 = makeUser("reviewer2", "Reviewer2");
    reviewer2 = (UserJpa) security.addUser(reviewer2, authToken);
    UserJpa reviewer3 = makeUser("reviewer3", "Reviewer3");
    reviewer3 = (UserJpa) security.addUser(reviewer3, authToken);

    //
    // Add author users
    //
    Logger.getLogger(getClass()).info("Add new author users");
    UserJpa author1 = makeUser("author1", "Author1");
    author1 = (UserJpa) security.addUser(author1, authToken);
    UserJpa author2 = makeUser("author2", "Author2");
    author2 = (UserJpa) security.addUser(author2, authToken);
    UserJpa author3 = makeUser("author3", "Author3");
    author3 = (UserJpa) security.addUser(author3, authToken);

    //
    // Make a project
    //

    ProjectJpa project1 = new ProjectJpa();
    project1.setBranch(Branch.ROOT);
    project1.setDescription("Project for NCI-META Editing");
    project1.setFeedbackEmail("info@westcoastinformatics.com");
    project1.setName("NCI-META Editing");
    project1.setPublic(true);
    project1.setTerminology(terminology);
    project1.setWorkflowPath(ConfigUtility.DEFAULT);

    // Configure valid categories
    final List<String> validCategories = new ArrayList<>();
    validCategories.add("chem");
    project1.setValidCategories(validCategories);

    Map<String, String> semanticTypeCategoryMap = getSemanticTypeCategoryMap();
    project1.setSemanticTypeCategoryMap(semanticTypeCategoryMap);

    final List<String> validationChecks = new ArrayList<>();
    validationChecks.add("DEFAULT");
    project1.setValidationChecks(validationChecks);

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

    //
    // Assign project roles
    //
    Logger.getLogger(getClass()).info("Assign users to projects");
    project = new ProjectServiceRestImpl();
    project.assignUserToProject(project1.getId(), admin1.getUserName(),
        UserRole.ADMINISTRATOR.toString(), authToken);
    project = new ProjectServiceRestImpl();
    project.assignUserToProject(project1.getId(), admin2.getUserName(),
        UserRole.ADMINISTRATOR.toString(), authToken);

    project = new ProjectServiceRestImpl();
    project.assignUserToProject(project1.getId(), reviewer1.getUserName(),
        UserRole.REVIEWER.toString(), authToken);
    project = new ProjectServiceRestImpl();
    project.assignUserToProject(project1.getId(), reviewer2.getUserName(),
        UserRole.REVIEWER.toString(), authToken);

    project = new ProjectServiceRestImpl();
    project.assignUserToProject(project1.getId(), author1.getUserName(),
        UserRole.AUTHOR.toString(), authToken);
    project = new ProjectServiceRestImpl();
    project.assignUserToProject(project1.getId(), author2.getUserName(),
        UserRole.AUTHOR.toString(), authToken);

    //
    // Start editing cycle
    //
    getLog().info("Start editing cycle");
    final HistoryServiceRestImpl historyService = new HistoryServiceRestImpl();
    historyService.startEditingCycle(nextRelease, terminology, version,
        authToken);

    //
    // Fake some data as needs review
    //
    getLog().info("Fake some needs review content");
    ContentServiceRest contentService = new ContentServiceRestImpl();
    IntegrationTestServiceRest testService =
        new IntegrationTestServiceRestImpl();

    // Demotions
    //
    // 129247, 129656
    // 129248, 129657
    // 129650, 129664
    //
    getLog().info("  Add demotions");
    final Long[] id1s = new Long[] {
        129247L, 129248L, 129650L
    };
    final Long[] id2s = new Long[] {
        129656L, 129657L, 129664L
    };
    for (int i = 0; i < id1s.length; i++) {
      final ConceptRelationshipJpa rel = new ConceptRelationshipJpa();
      contentService = new ContentServiceRestImpl();
      final Concept from =
          contentService.getConcept(id1s[i], project1.getId(), authToken);
      contentService = new ContentServiceRestImpl();
      final Concept to =
          contentService.getConcept(id2s[i], project1.getId(), authToken);
      rel.setFrom(from);
      rel.setTo(to);
      rel.setRelationshipType("RO");
      rel.setAdditionalRelationshipType("");
      rel.setTerminologyId("");
      rel.setTerminology(project1.getTerminology());
      rel.setVersion("latest");
      rel.setWorkflowStatus(WorkflowStatus.DEMOTION);
      testService = new IntegrationTestServiceRestImpl();
      testService.addRelationship(rel, authToken);
    }

    // Status N NCIt concepts (and atoms)
    getLog().info("  Mark some NCIt concepts as status N");
    /**
     * <pre>
     * create table tbac as select distinct c.id from atoms a,
     * atomjpa_conceptterminologyids b, concepts c where AtomJpa_id = a.id and
     * a.terminology='NCI' and c.terminology='UMLS' and c.terminologyId =
     * conceptTerminologyIds;
     * </pre>
     */
    for (final Long conceptId : new Long[] {
        2818L, 2821L, 2823L, 2826L, 2827L, 2829L, 2830L, 2831L, 2834L, 2836L,
        2837L, 2838L
    }) {
      contentService = new ContentServiceRestImpl();
      final ConceptJpa concept =
          new ConceptJpa(contentService.getConcept(conceptId, project1.getId(),
              authToken), true);
      concept.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
      testService = new IntegrationTestServiceRestImpl();
      testService.updateConcept(concept, authToken);
    }

    // Leftovers
    getLog().info("  Mark some non-NCIt concepts as status N");
    /**
     * <pre>
     * drop table tbac; 
     * create table tbac as select distinct c.id from atoms a,
     * atomjpa_conceptterminologyids b, concepts c where AtomJpa_id = a.id and
     * a.terminology='NCI' and c.terminology='UMLS' and c.terminologyId =
     * conceptTerminologyIds;
     * 
     * drop table tbac2; 
     * create table tbac2 as select distinct c.id from atoms
     * a, atomjpa_conceptterminologyids b, concepts c where AtomJpa_id = a.id
     * and a.terminology!='NCI' and c.terminology='UMLS' and c.terminologyId =
     * conceptTerminologyIds;
     * 
     * select * from tbac2 a where id not in (select id from tbac);
     * </pre>
     */
    for (final Long conceptId : new Long[] {
        92907L, 114192L, 68911L, 116076L, 7328L, 7334L, 10252L, 10276L, 10282L
    }) {
      contentService = new ContentServiceRestImpl();
      final ConceptJpa concept =
          new ConceptJpa(contentService.getConcept(conceptId, project1.getId(),
              authToken), true);
      concept.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
      testService = new IntegrationTestServiceRestImpl();
      testService.updateConcept(concept, authToken);
    }

    //
    // Prepare workflow related objects
    //
    getLog().info("Prepare workflow related objects");
    WorkflowServiceRestImpl workflowService = new WorkflowServiceRestImpl();

    // Create a workflow epoch
    // TODO: create an older one and a new one so we can test
    // "get currente epoch"
    getLog().info("  Create an epoch");
    WorkflowEpochJpa workflowEpoch = new WorkflowEpochJpa();
    workflowEpoch.setActive(true);
    workflowEpoch.setName("16a");
    workflowEpoch.setProject(project1);
    workflowService
        .addWorkflowEpoch(project1.getId(), workflowEpoch, authToken);

    // Add a ME bins workflow config for the current project
    // TODO: also add a QA for testing of non-mutually-excuslive
    getLog().info("  Create a ME workflow config");
    workflowService = new WorkflowServiceRestImpl();
    WorkflowConfigJpa config = new WorkflowConfigJpa();
    config.setType(WorkflowBinType.MUTUALLY_EXCLUSIVE);
    config.setMutuallyExclusive(true);
    config.setProjectId(project1.getId());
    workflowService = new WorkflowServiceRestImpl();
    WorkflowConfig newConfig =
        workflowService.addWorkflowConfig(project1.getId(), config, authToken);

    // Add a workflow definition (as SQL)
    // TODO: create workflow bin definitions exactly matching NCI-META config
    // also
    getLog().info("  Create a workflow definition");
    WorkflowBinDefinitionJpa definition = new WorkflowBinDefinitionJpa();
    definition.setName("testName");
    definition.setDescription("test description");
    definition
        .setQuery("select distinct c.id clusterId, c.id conceptId from concepts c where c.name like '%Amino%';");
    definition.setEditable(true);
    definition.setQueryType(QueryType.SQL);
    definition.setWorkflowConfig(newConfig);

    workflowService = new WorkflowServiceRestImpl();
    workflowService.addWorkflowBinDefinition(project1.getId(), definition,
        authToken);

    // Add a second workflow definition
    getLog().info("  Create a second workflow definition");
    WorkflowBinDefinitionJpa definition2 = new WorkflowBinDefinitionJpa();
    definition2.setName("testName2");
    definition2.setDescription("test description2");
    definition2
        .setQuery("select distinct c.id clusterId, c.id conceptId from concepts c where c.name like '%Acid%';");
    definition2.setEditable(true);
    definition2.setQueryType(QueryType.SQL);
    definition2.setWorkflowConfig(newConfig);

    workflowService = new WorkflowServiceRestImpl();
    workflowService.addWorkflowBinDefinition(project1.getId(), definition2,
        authToken);

    // Clear and regenerate all bins
    getLog().info("  Clear and regenerate all bins");
    // Clear bins
    workflowService.clearBins(project1.getId(),
        WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);

    // Regenerate bins
    workflowService.regenerateBins(project1.getId(),
        WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);

    // TODO: create a few checklists from bins (including randomizing)
    getLog().info("  Create a random checklist");

    getLog().info("  Create a non-random checklist");

    // TODO: create a few worklist from bins
    getLog().info("  Create a few worklists from the bins");

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
          service.getSemanticTypes(terminology, version);

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
    return user;
  }

}
