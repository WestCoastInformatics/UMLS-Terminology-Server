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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;

import com.wci.umls.server.Project;
import com.wci.umls.server.UserRole;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.meta.SemanticTypeList;
import com.wci.umls.server.jpa.ProjectJpa;
import com.wci.umls.server.jpa.UserJpa;
import com.wci.umls.server.jpa.services.MetadataServiceJpa;
import com.wci.umls.server.jpa.services.SecurityServiceJpa;
import com.wci.umls.server.jpa.services.rest.ProjectServiceRest;
import com.wci.umls.server.jpa.services.rest.SecurityServiceRest;
import com.wci.umls.server.jpa.worfklow.WorkflowBinDefinitionJpa;
import com.wci.umls.server.jpa.worfklow.WorkflowConfigJpa;
import com.wci.umls.server.jpa.worfklow.WorkflowEpochJpa;
import com.wci.umls.server.model.meta.SemanticType;
import com.wci.umls.server.model.workflow.QueryType;
import com.wci.umls.server.model.workflow.WorkflowBinType;
import com.wci.umls.server.model.workflow.WorkflowConfig;
import com.wci.umls.server.model.workflow.WorkflowStatus;
import com.wci.umls.server.rest.impl.ProjectServiceRestImpl;
import com.wci.umls.server.rest.impl.SecurityServiceRestImpl;
import com.wci.umls.server.rest.impl.WorkflowServiceRestImpl;
import com.wci.umls.server.services.MetadataService;
import com.wci.umls.server.services.SecurityService;

/**
 * Goal which performs an ad hoc task.
 * 
 * See admin/db/pom.xml for sample usage
 * 
 * @goal ad-hoc
 * @phase package
 */
public class AdHocMojo extends AbstractMojo {

  /** The terminology. */
  String terminology = "UMLS";

  /** The version. */
  String version = "latest";

  /**
   * Instantiates a {@link AdHocMojo} from the specified parameters.
   */
  public AdHocMojo() {
    // do nothing
  }

  /* see superclass */
  @Override
  public void execute() throws MojoFailureException {

    try {

      getLog().info("Ad Hoc Mojo");

      // Handle creating the database if the mode parameter is set
      final Properties properties = ConfigUtility.getConfigProperties();

      // authenticate
      final SecurityService service = new SecurityServiceJpa();
      final String authToken =
          service.authenticate(properties.getProperty("admin.user"),
              properties.getProperty("admin.password")).getAuthToken();
      service.close();

      boolean serverRunning = ConfigUtility.isServerActive();
      getLog().info(
          "Server status detected:  " + (!serverRunning ? "DOWN" : "UP"));
      if (serverRunning) {
        throw new Exception("Server must not be running to generate data");
      }
      //
      // // Initialize
      Logger.getLogger(getClass()).info("Authenticate admin user");
      SecurityServiceRest security = new SecurityServiceRestImpl();
      ProjectServiceRest project = new ProjectServiceRestImpl();

      // Add project
      Project project1 = (ProjectJpa) project.getProject(1239500L, authToken);

      //
      // Prepare workflow related objects
      //
      getLog().info("Prepare workflow related objects");
      WorkflowServiceRestImpl workflowService = new WorkflowServiceRestImpl();

      getLog().info("  Create epoch 16a");
      WorkflowEpochJpa workflowEpoch = new WorkflowEpochJpa();
      workflowEpoch.setActive(true);
      workflowEpoch.setName("16a");
      workflowEpoch.setProject(project1);
      workflowService = new WorkflowServiceRestImpl();
      workflowService.addWorkflowEpoch(project1.getId(), workflowEpoch,
          authToken);

      //
      // Add a ME bins workflow config for the current project
      //
      getLog().info("  Create a ME workflow config");
      workflowService = new WorkflowServiceRestImpl();
      WorkflowConfigJpa config = new WorkflowConfigJpa();
      config.setType(WorkflowBinType.MUTUALLY_EXCLUSIVE);
      config.setMutuallyExclusive(true);
      config.setProjectId(project1.getId());
      workflowService = new WorkflowServiceRestImpl();
      WorkflowConfig newConfig =
          workflowService
              .addWorkflowConfig(project1.getId(), config, authToken);

      // Add workflow definitions
      // demotions
      getLog().info("    Add 'demotions' workflow bin definition");
      WorkflowBinDefinitionJpa definition = new WorkflowBinDefinitionJpa();
      definition.setName("demotions");
      definition
          .setDescription("Clustered concepts that failed insertion merges.  Must be either related or merged.");
      definition.setQuery("select from_id clusterId, from_id conceptId "
          + "from concept_relationships "
          + "where terminology=:terminology and workflowStatus = '"
          + WorkflowStatus.DEMOTION + "' union "
          + "select from_id, to_id from concept_relationships "
          + "where terminology=:terminology and workflowStatus = '"
          + WorkflowStatus.DEMOTION + "' " + "order by 1");
      definition.setEditable(true);
      definition.setRequired(true);
      definition.setQueryType(QueryType.SQL);
      definition.setWorkflowConfig(newConfig);
      workflowService = new WorkflowServiceRestImpl();
      workflowService.addWorkflowBinDefinition(project1.getId(), definition,
          authToken);

      // norelease
      getLog().info("    Add 'norelease' workflow bin definition");
      definition = new WorkflowBinDefinitionJpa();
      definition.setName("norelease");
      definition.setDescription("Concepts where all atoms are unreleasable.");
      definition.setQuery("select a.id clusterId, a.id conceptId "
          + "from concepts a, concepts_atoms b, atoms c "
          + "where a.terminology=:terminology and a.id = b.concepts_id "
          + "and b.atoms_id = c.id and c.publishable = 0 "
          + "and not exists (select * from concepts_atoms d, atoms e "
          + " where a.id = d.concepts_id and d.atoms_id = e.id "
          + " and e.publishable = 1);");
      definition.setEditable(false);
      definition.setRequired(true);
      definition.setQueryType(QueryType.SQL);
      definition.setWorkflowConfig(newConfig);
      workflowService = new WorkflowServiceRestImpl();
      workflowService.addWorkflowBinDefinition(project1.getId(), definition,
          authToken);

      // reviewed
      getLog().info("    Add 'reviewed' workflow bin definition");
      definition = new WorkflowBinDefinitionJpa();
      definition.setName("reviewed");
      definition.setDescription("Concepts that do not require review.");
      definition.setQuery("select a.id clusterId, a.id conceptId "
          + "from concepts a " + "where a.terminology=:terminology "
          + "a.workflowStatus != '" + WorkflowStatus.NEEDS_REVIEW + "'");
      definition.setEditable(false);
      definition.setRequired(true);
      definition.setQueryType(QueryType.SQL);
      definition.setWorkflowConfig(newConfig);
      workflowService = new WorkflowServiceRestImpl();
      workflowService.addWorkflowBinDefinition(project1.getId(), definition,
          authToken);

      // ncithesaurus
      getLog().info("    Add 'ncithesaurus' workflow bin definition");
      definition = new WorkflowBinDefinitionJpa();
      definition.setName("ncithesaurus");
      definition.setDescription("NCI Thesaurus.");
      definition.setQuery("select a.id clusterId, a.id conceptId "
          + "from concepts a, concepts_atoms b, atoms c "
          + "where a.terminology = :terminology and a.id = b.concepts_id "
          + "  and b.atoms_id = c.id  and c.terminology='NCI' "
          + "  and c.workflowStatus = '" + WorkflowStatus.NEEDS_REVIEW + "'");
      definition.setEditable(true);
      definition.setRequired(true);
      definition.setQueryType(QueryType.SQL);
      definition.setWorkflowConfig(newConfig);
      workflowService = new WorkflowServiceRestImpl();
      workflowService.addWorkflowBinDefinition(project1.getId(), definition,
          authToken);

      // snomedct_us
      getLog().info("    Add 'snomedct_us' workflow bin definition");
      definition = new WorkflowBinDefinitionJpa();
      definition.setName("snomedct_us");
      definition.setDescription("SNOMEDCT_US.");
      definition.setQuery("select a.id clusterId, a.id conceptId "
          + "from concepts a, concepts_atoms b, atoms c "
          + "where a.terminology = :terminology and a.id = b.concepts_id "
          + "  and b.atoms_id = c.id and c.terminology='SNOMEDCT_US' "
          + "  and c.workflowStatus = '" + WorkflowStatus.NEEDS_REVIEW + "'");
      definition.setEditable(true);
      definition.setRequired(true);
      definition.setQueryType(QueryType.SQL);
      definition.setWorkflowConfig(newConfig);
      workflowService = new WorkflowServiceRestImpl();
      workflowService.addWorkflowBinDefinition(project1.getId(), definition,
          authToken);

      // leftovers
      getLog().info("    Add 'leftovers' workflow bin definition");
      definition = new WorkflowBinDefinitionJpa();
      definition.setName("leftovers");
      definition.setDescription("SNOMEDCT_US.");
      definition.setQuery("select a.id clusterId, a.id conceptId "
          + "from concepts a where a.workflowStatus = '"
          + WorkflowStatus.NEEDS_REVIEW + "'"
          + "and a.terminology = :terminology");
      definition.setEditable(true);
      definition.setRequired(true);
      definition.setQueryType(QueryType.SQL);
      definition.setWorkflowConfig(newConfig);
      workflowService = new WorkflowServiceRestImpl();
      workflowService.addWorkflowBinDefinition(project1.getId(), definition,
          authToken);

      //
      // Add a QA bins workflow config for the current project
      //
      getLog().info("  Create a QA workflow config");
      workflowService = new WorkflowServiceRestImpl();
      config = new WorkflowConfigJpa();
      config.setType(WorkflowBinType.QUALITY_ASSURANCE);
      config.setMutuallyExclusive(true);
      config.setProjectId(project1.getId());
      workflowService = new WorkflowServiceRestImpl();
      newConfig =
          workflowService
              .addWorkflowConfig(project1.getId(), config, authToken);

      // Required SCUI "merge" bins
      for (final String terminology : new String[] {
          "nci", "rxnorm", "cbo"
      }) {
        getLog().info(
            "    Add '" + terminology + "_merge' workflow bin definition");
        definition = new WorkflowBinDefinitionJpa();
        definition.setName(terminology + "_merge");
        definition.setDescription("Merged " + terminology.toUpperCase()
            + " SCUIs, including merged PTs");
        definition.setQuery("select a.id clusterId, a.id conceptId "
            + "from concepts a, concepts_atoms b, atoms c "
            + "where a.id = b.concepts_id and a.terminology = :terminology"
            + "  and b.atoms_id = c.id  and c.terminology='"
            + terminology.toUpperCase() + "'  "
            + "group by a.id having count(distinct c.conceptId)>1");
        definition.setEditable(true);
        definition.setRequired(true);
        definition.setQueryType(QueryType.SQL);
        definition.setWorkflowConfig(newConfig);
      }

      workflowService = new WorkflowServiceRestImpl();
      workflowService.addWorkflowBinDefinition(project1.getId(), definition,
          authToken);

      // Clear and regenerate all bins
      getLog().info("  Clear and regenerate ME bins");
      // Clear bins
      workflowService = new WorkflowServiceRestImpl();
      workflowService.clearBins(project1.getId(),
          WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);

      // Regenerate bins
      workflowService = new WorkflowServiceRestImpl();
      workflowService.regenerateBins(project1.getId(),
          WorkflowBinType.MUTUALLY_EXCLUSIVE, authToken);

      // Clear and regenerate all bins
      getLog().info("  Clear and regenerate QA bins");
      // Clear bins
      workflowService = new WorkflowServiceRestImpl();
      workflowService.clearBins(project1.getId(),
          WorkflowBinType.QUALITY_ASSURANCE, authToken);

      // Regenerate bins
      workflowService = new WorkflowServiceRestImpl();
      workflowService.regenerateBins(project1.getId(),
          WorkflowBinType.QUALITY_ASSURANCE, authToken);

      getLog().info("done ...");
    } catch (Exception e) {
      e.printStackTrace();
      throw new MojoFailureException("Unexpected exception:", e);
    }
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
}
