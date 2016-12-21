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
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.wci.umls.server.UserRole;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.helpers.QueryType;
import com.wci.umls.server.jpa.ProjectJpa;
import com.wci.umls.server.jpa.UserJpa;
import com.wci.umls.server.jpa.helpers.PrecedenceListJpa;
import com.wci.umls.server.jpa.services.SecurityServiceJpa;
import com.wci.umls.server.jpa.services.rest.MetadataServiceRest;
import com.wci.umls.server.jpa.services.rest.ProjectServiceRest;
import com.wci.umls.server.jpa.services.rest.SecurityServiceRest;
import com.wci.umls.server.jpa.workflow.WorkflowBinDefinitionJpa;
import com.wci.umls.server.jpa.workflow.WorkflowConfigJpa;
import com.wci.umls.server.jpa.workflow.WorkflowEpochJpa;
import com.wci.umls.server.model.workflow.WorkflowConfig;
import com.wci.umls.server.rest.impl.MetadataServiceRestImpl;
import com.wci.umls.server.rest.impl.ProjectServiceRestImpl;
import com.wci.umls.server.rest.impl.SecurityServiceRestImpl;
import com.wci.umls.server.rest.impl.WorkflowServiceRestImpl;
import com.wci.umls.server.services.SecurityService;

/**
 * Goal which generates demo data for the default dev build. This uses REST
 * services directly and not through the client.
 * 
 * See admin/loader/pom.xml for demo usage
 */
@Mojo(name = "generate-demo-data", defaultPhase = LifecyclePhase.PACKAGE)
public class GenerateDemoDataMojo extends AbstractLoaderMojo {

  /**
   * Mode - for recreating db.
   */
  @Parameter
  private String mode = null;

  /** The next release. */
  // private final String nextRelease = "2016AB";

  /**
   * Instantiates a {@link GenerateDemoDataMojo} from the specified parameters.
   */
  public GenerateDemoDataMojo() {
    // do nothing
  }

  /* see superclass */
  @Override
  public void execute() throws MojoFailureException {
    getLog().info("Generating demo data");

    try {

      getLog().info("Generate demo data");
      getLog().info("  mode = " + mode);

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

    //
    // Add admin users
    //
    Logger.getLogger(getClass()).info("Add new admin users");
    UserJpa admin1 = (UserJpa) security.getUser("admin1", authToken);
    if (admin1 == null) {
      admin1 = makeUser("admin1", "Admin1", 0);
      admin1 = (UserJpa) security.addUser(admin1, authToken);
    }
    UserJpa admin2 = (UserJpa) security.getUser("admin2", authToken);
    if (admin2 == null) {
      admin2 = makeUser("admin2", "Admin2", 0);
      admin2 = (UserJpa) security.addUser(admin2, authToken);
    }
    UserJpa admin3 = (UserJpa) security.getUser("admin3", authToken);
    if (admin3 == null) {
      admin3 = makeUser("admin3", "Admin3", 0);
      admin3 = (UserJpa) security.addUser(admin3, authToken);
    }

    //
    // Add reviewer users
    //
    Logger.getLogger(getClass()).info("Add new reviewer users");
    UserJpa reviewer1 = (UserJpa) security.getUser("reviewer1", authToken);
    if (reviewer1 == null) {
      reviewer1 = makeUser("reviewer1", "Reviewer1", 0);
      reviewer1 = (UserJpa) security.addUser(reviewer1, authToken);
    }
    UserJpa reviewer2 = (UserJpa) security.getUser("reviewer2", authToken);
    if (reviewer2 == null) {
      reviewer2 = makeUser("reviewer2", "Reviewer2", 0);
      reviewer2 = (UserJpa) security.addUser(reviewer2, authToken);
    }
    UserJpa reviewer3 = (UserJpa) security.getUser("reviewer3", authToken);
    if (reviewer3 == null) {
      reviewer3 = makeUser("reviewer3", "Reviewer3", 0);
      reviewer3 = (UserJpa) security.addUser(reviewer3, authToken);
    }

    //
    // Add author users
    //
    Logger.getLogger(getClass()).info("Add new author users");
    UserJpa author1 = (UserJpa) security.getUser("author1", authToken);
    if (author1 == null) {
      author1 = makeUser("author1", "Author1", 0);
      author1 = (UserJpa) security.addUser(author1, authToken);
    }
    UserJpa author2 = (UserJpa) security.getUser("author2", authToken);
    if (author2 == null) {
      author2 = makeUser("author2", "Author2", 0);
      author2 = (UserJpa) security.addUser(author2, authToken);
    }
    UserJpa author3 = (UserJpa) security.getUser("author3", authToken);
    if (author3 == null) {
      author3 = makeUser("author3", "Author3", 0);
      author3 = (UserJpa) security.addUser(author3, authToken);
    }

    //
    // Make a project
    //

    final String[] terminologies = new String[] {
        "SNOMEDCT", "SNOMEDCT_US", "ICD9CM", "ICD10CM", "LNC"
    };
    final String[] versions = new String[] {
        "20160731", "20160901", "2013", "2016", "248"
    };

    for (int i = 0; i < terminologies.length; i++) {

      ProjectJpa project1 = new ProjectJpa();
      project1.setBranch(Branch.ROOT);
      project1.setDescription("Project for " + terminologies[i] + " Editing");
      project1.setFeedbackEmail("info@westcoastinformatics.com");
      project1.setName(terminologies[i] + " Editing " + new Date().getTime());
      project1.setPublic(true);
      project1.setLanguage("ENG");
      project1.setTerminology(terminologies[i]);
      project1.setVersion(versions[i]);
      project1.setWorkflowPath(ConfigUtility.DEFAULT);
      List<String> newAtomTermgroups = new ArrayList<>();
      newAtomTermgroups.add(terminologies[i] + "/SY");
      project1.setNewAtomTermgroups(newAtomTermgroups);

      final List<String> validationChecks = new ArrayList<>();
      validationChecks.add("DEFAULT");
      project1.setValidationChecks(validationChecks);

      // Handle precedence list (if exists)
      MetadataServiceRest metadataService = new MetadataServiceRestImpl();
      PrecedenceList origList = metadataService.getDefaultPrecedenceList(
          project1.getTerminology(), "latest", authToken);

      if (origList != null) {
        PrecedenceListJpa list = new PrecedenceListJpa(origList);
        list.setId(null);
        list.setTerminology("");
        list.setVersion("");
        metadataService = new MetadataServiceRestImpl();
        list = (PrecedenceListJpa) metadataService.addPrecedenceList(list,
            authToken);
        project1.setPrecedenceList(list);
      }

      // Add project
      project = new ProjectServiceRestImpl();
      project1 = (ProjectJpa) project.addProject(project1, authToken);
      final Long projectId = project1.getId();

      //
      // Assign project roles
      //
      Logger.getLogger(getClass()).info("Assign users to projects");
      project = new ProjectServiceRestImpl();
      project.assignUserToProject(projectId, admin1.getUserName(),
          UserRole.ADMINISTRATOR, authToken);
      project = new ProjectServiceRestImpl();
      project.assignUserToProject(projectId, admin2.getUserName(),
          UserRole.ADMINISTRATOR, authToken);

      project = new ProjectServiceRestImpl();
      project.assignUserToProject(projectId, reviewer1.getUserName(),
          UserRole.REVIEWER, authToken);
      project = new ProjectServiceRestImpl();
      project.assignUserToProject(projectId, reviewer2.getUserName(),
          UserRole.REVIEWER, authToken);

      project = new ProjectServiceRestImpl();
      project.assignUserToProject(projectId, author1.getUserName(),
          UserRole.AUTHOR, authToken);
      project = new ProjectServiceRestImpl();
      project.assignUserToProject(projectId, author2.getUserName(),
          UserRole.AUTHOR, authToken);

      //
      // Prepare workflow related objects
      //
      getLog().info("Prepare workflow related objects");
      WorkflowServiceRestImpl workflowService = new WorkflowServiceRestImpl();

      // Create a workflow epoch
      getLog().info("  Create epoch 16a");
      WorkflowEpochJpa workflowEpoch = new WorkflowEpochJpa();
      workflowEpoch.setActive(true);
      workflowEpoch.setName("16a");
      workflowEpoch.setProject(project1);
      workflowService.addWorkflowEpoch(projectId, workflowEpoch, authToken);

      getLog().info("  Create epoch 17a");
      workflowEpoch = new WorkflowEpochJpa();
      workflowEpoch.setActive(true);
      workflowEpoch.setName("17a");
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

      // norelease
      getLog().info("    Add 'norelease' workflow bin definition");
      WorkflowBinDefinitionJpa definition = new WorkflowBinDefinitionJpa();
      definition.setName("norelease");
      definition.setDescription("Concepts where all atoms are unreleasable.");
      definition.setQuery("select a.id clusterId, a.id conceptId "
          + "from concepts a, concepts_atoms b, atoms c "
          + "where a.terminology = :terminology and a.id = b.concepts_id "
          + "and b.atoms_id = c.id and c.publishable = 0 "
          + "and not exists (select * from concepts_atoms d, atoms e "
          + " where a.id = d.concepts_id and d.atoms_id = e.id "
          + " and e.publishable = 1);");
      definition.setEditable(false);
      definition.setEnabled(true);
      definition.setRequired(false);
      definition.setQueryType(QueryType.SQL);
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

      // need review
      getLog().info("    Add 'need review' workflow bin definition");
      definition = new WorkflowBinDefinitionJpa();
      definition.setName("need review");
      definition.setDescription("Concepts needing review");
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
      workflowService.regenerateBins(projectId, "MUTUALLY_EXCLUSIVE",
          authToken);

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
      newConfig =
          workflowService.addWorkflowConfig(projectId, config, authToken);

      getLog().info("    Add 'allergy' concepts");
      definition = new WorkflowBinDefinitionJpa();
      definition.setName("allergy");
      definition.setDescription("Concepts containing the word 'allergy'");
      definition.setQuery("atoms.name:allergy");
      definition.setEditable(true);
      definition.setEnabled(true);
      definition.setRequired(true);
      definition.setQueryType(QueryType.LUCENE);
      definition.setWorkflowConfig(newConfig);
      workflowService = new WorkflowServiceRestImpl();
      workflowService.addWorkflowBinDefinition(projectId, null, definition,
          authToken);

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
      workflowService.recomputeConceptStatus(projectId, "MATRIXINIT",
          authToken);

    }

  }

  /**
   * Make user.
   *
   * @param userName the user name
   * @param name the name
   * @param editorLevel the editor level
   * @return the user
   */
  @SuppressWarnings("static-method")
  private UserJpa makeUser(String userName, String name, int editorLevel) {
    final UserJpa user = new UserJpa();
    user.setUserName(userName);
    user.setName(name);
    user.setEmail(userName + "@example.com");
    user.setApplicationRole(UserRole.USER);
    user.setEditorLevel(editorLevel);
    user.setTeam("TEAM" + userName.substring(userName.length() - 1));
    return user;
  }

}
