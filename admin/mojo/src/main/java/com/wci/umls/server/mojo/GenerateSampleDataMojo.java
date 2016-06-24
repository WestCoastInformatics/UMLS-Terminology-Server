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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;

import com.wci.umls.server.User;
import com.wci.umls.server.UserRole;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.meta.SemanticTypeList;
import com.wci.umls.server.jpa.ProjectJpa;
import com.wci.umls.server.jpa.UserJpa;
import com.wci.umls.server.jpa.services.MetadataServiceJpa;
import com.wci.umls.server.jpa.services.ProjectServiceJpa;
import com.wci.umls.server.jpa.services.SecurityServiceJpa;
import com.wci.umls.server.jpa.services.rest.ProjectServiceRest;
import com.wci.umls.server.jpa.services.rest.SecurityServiceRest;
import com.wci.umls.server.jpa.worfklow.WorkflowBinDefinitionJpa;
import com.wci.umls.server.jpa.worfklow.WorkflowConfigJpa;
import com.wci.umls.server.model.meta.SemanticType;
import com.wci.umls.server.model.workflow.QueryType;
import com.wci.umls.server.model.workflow.WorkflowBinType;
import com.wci.umls.server.model.workflow.WorkflowConfig;
import com.wci.umls.server.rest.impl.ContentServiceRestImpl;
import com.wci.umls.server.rest.impl.HistoryServiceRestImpl;
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

      loadSampleData();

      getLog().info("done ...");
    } catch (Exception e) {
      e.printStackTrace();
      throw new MojoFailureException("Unexpected exception:", e);
    }
  }

  /**
   * Load sample data.
   *
   * @throws Exception the exception
   */
  private void loadSampleData() throws Exception {

    // Initialize
    Logger.getLogger(getClass()).info("Authenticate admin user");
    SecurityServiceRest security = new SecurityServiceRestImpl();
    ProjectServiceRest project = new ProjectServiceRestImpl();
    User admin = security.authenticate("admin", "admin");

    //
    // Add admin users
    //
    Logger.getLogger(getClass()).info("Add new admin users");
    UserJpa admin1 = makeUser("admin1", "Admin1");
    admin1 = (UserJpa) security.addUser(admin1, admin.getAuthToken());
    UserJpa admin2 = makeUser("admin3", "Admin2");
    admin2 = (UserJpa) security.addUser(admin2, admin.getAuthToken());
    UserJpa admin3 = makeUser("admin2", "Admin3");
    admin3 = (UserJpa) security.addUser(admin3, admin.getAuthToken());

    //
    // Add reviewer users
    //
    Logger.getLogger(getClass()).info("Add new reviewer users");
    UserJpa reviewer1 = makeUser("reviewer1", "Reviewer1");
    reviewer1 = (UserJpa) security.addUser(reviewer1, admin.getAuthToken());
    UserJpa reviewer2 = makeUser("reviewer2", "Reviewer2");
    reviewer2 = (UserJpa) security.addUser(reviewer2, admin.getAuthToken());
    UserJpa reviewer3 = makeUser("reviewer3", "Reviewer3");
    reviewer3 = (UserJpa) security.addUser(reviewer3, admin.getAuthToken());

    //
    // Add author users
    //
    Logger.getLogger(getClass()).info("Add new author users");
    UserJpa author1 = makeUser("author1", "Author1");
    author1 = (UserJpa) security.addUser(author1, admin.getAuthToken());
    UserJpa author2 = makeUser("author2", "Author2");
    author2 = (UserJpa) security.addUser(author2, admin.getAuthToken());
    UserJpa author3 = makeUser("author3", "Author3");
    author3 = (UserJpa) security.addUser(author3, admin.getAuthToken());

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
    validCategories.add("nonchem");
    project1.setValidCategories(validCategories);

    Map<String, String> semanticTypeCategoryMap = getSemanticTypeCategoryMap();
    project1.setSemanticTypeCategoryMap(semanticTypeCategoryMap);

    final List<String> validationChecks = new ArrayList<>();
    validationChecks.add("DEFAULT");
    project1.setValidationChecks(validationChecks);

    // TODO: get the default precedence list, may need rest call to add a
    // add/update/remove prec list
    // project1.setPrecedenceList(precedenceList);

    project1 = (ProjectJpa) project.addProject(project1, admin.getAuthToken());

    //
    // Assign project roles
    //
    Logger.getLogger(getClass()).info("Assign users to projects");
    project = new ProjectServiceRestImpl();
    project.assignUserToProject(project1.getId(), admin1.getUserName(),
        UserRole.ADMINISTRATOR.toString(), admin.getAuthToken());
    project = new ProjectServiceRestImpl();
    project.assignUserToProject(project1.getId(), admin2.getUserName(),
        UserRole.ADMINISTRATOR.toString(), admin.getAuthToken());

    project = new ProjectServiceRestImpl();
    project.assignUserToProject(project1.getId(), reviewer1.getUserName(),
        UserRole.REVIEWER.toString(), admin.getAuthToken());
    project = new ProjectServiceRestImpl();
    project.assignUserToProject(project1.getId(), reviewer2.getUserName(),
        UserRole.REVIEWER.toString(), admin.getAuthToken());

    project = new ProjectServiceRestImpl();
    project.assignUserToProject(project1.getId(), author1.getUserName(),
        UserRole.AUTHOR.toString(), admin.getAuthToken());
    project = new ProjectServiceRestImpl();
    project.assignUserToProject(project1.getId(), author2.getUserName(),
        UserRole.AUTHOR.toString(), admin.getAuthToken());

    //
    // Start editing cycle
    //
    final HistoryServiceRestImpl historyService = new HistoryServiceRestImpl();
    historyService.startEditingCycle(nextRelease, terminology, version,
        admin.getAuthToken());

    
    //
    // Prepare the test and check prerequisites
    //
    Date startDate = new Date();

    WorkflowConfigJpa workflowConfig = new WorkflowConfigJpa();
    workflowConfig.setType(WorkflowBinType.MUTUALLY_EXCLUSIVE);
    workflowConfig.setMutuallyExclusive(true);
    workflowConfig.setProjectId(project1.getId());
    workflowConfig.setTimestamp(startDate);
    workflowConfig.setLastPartitionTime(1L);


    // add the workflow config
    WorkflowServiceRestImpl workflowService = new WorkflowServiceRestImpl();
    WorkflowConfig addedWorkflowConfig =
        workflowService.addWorkflowConfig(project1.getId(), workflowConfig,
            admin.getAuthToken());

    WorkflowBinDefinitionJpa workflowBinDefinition =
        new WorkflowBinDefinitionJpa();
    workflowBinDefinition.setName("test name");
    workflowBinDefinition.setDescription("test description");
    workflowBinDefinition.setQuery(
        "select distinct c.id clusterId, c.id conceptId from concepts c where c.name like '%Amino%';");
    workflowBinDefinition.setEditable(true);
    workflowBinDefinition.setQueryType(QueryType.SQL);
    workflowBinDefinition.setTimestamp(startDate);
    workflowBinDefinition.setWorkflowConfig(addedWorkflowConfig);
    
    workflowService = new WorkflowServiceRestImpl();
    workflowService.addWorkflowBinDefinition(project1.getId(),
            addedWorkflowConfig.getId(), workflowBinDefinition, admin.getAuthToken());

    WorkflowBinDefinitionJpa workflowBinDefinition2 =
        new WorkflowBinDefinitionJpa();
    workflowBinDefinition2.setName("test name2");
    workflowBinDefinition2.setDescription("test description2");
    workflowBinDefinition2.setQuery(
        "select distinct c.id clusterId, c.id conceptId from ConceptJpa c where c.name like '%Acid%';");
    workflowBinDefinition2.setEditable(true);
    workflowBinDefinition2.setQueryType(QueryType.HQL);
    workflowBinDefinition2.setTimestamp(startDate);
    workflowBinDefinition2.setWorkflowConfig(addedWorkflowConfig);

    workflowService = new WorkflowServiceRestImpl();
    workflowService.addWorkflowBinDefinition(project1.getId(),
            addedWorkflowConfig.getId(), workflowBinDefinition2, admin.getAuthToken());

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
        } else {
          map.put(sty.getExpandedForm(), "nonchem");
        }
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
