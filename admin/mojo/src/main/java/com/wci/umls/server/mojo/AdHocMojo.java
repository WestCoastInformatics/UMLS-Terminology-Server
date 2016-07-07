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

import java.util.Date;
import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;

import com.wci.umls.server.Project;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.jpa.services.SecurityServiceJpa;
import com.wci.umls.server.jpa.services.rest.ProjectServiceRest;
import com.wci.umls.server.jpa.worfklow.WorkflowBinDefinitionJpa;
import com.wci.umls.server.jpa.worfklow.WorkflowConfigJpa;
import com.wci.umls.server.jpa.worfklow.WorkflowEpochJpa;
import com.wci.umls.server.model.workflow.QueryType;
import com.wci.umls.server.model.workflow.WorkflowBinType;
import com.wci.umls.server.model.workflow.WorkflowConfig;
import com.wci.umls.server.rest.impl.ProjectServiceRestImpl;
import com.wci.umls.server.rest.impl.WorkflowServiceRestImpl;
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

      // Perform operations here
      ProjectServiceRest projectService = new ProjectServiceRestImpl();
      Project project1 = projectService.getProject(1239550L, authToken);

      
      
      
      //
      // Prepare workflow related objects
      //
      getLog().info("Prepare workflow related objects");
      WorkflowServiceRestImpl workflowService = new WorkflowServiceRestImpl();
      Date startDate = new Date();

      // Create a workflow epoch
      // TODO: create an older one and a new one so we can test
      // "get currente epoch"
      getLog().info("  Create an epoch");
      WorkflowEpochJpa workflowEpoch = new WorkflowEpochJpa();
      workflowEpoch.setActive(true);
      workflowEpoch.setName("16a");
      workflowEpoch.setProjectId(project1.getId());
      workflowEpoch.setProject(project1);
      workflowEpoch.setTimestamp(startDate);
      workflowService
          .addWorkflowEpoch(project1.getId(), workflowEpoch, authToken);

      // Add a ME bins workflow config for the current project
      // TODO: also add a QA for testing of non-mutually-excuslive
      getLog().info("  Create a ME workflow config");
      workflowService = new WorkflowServiceRestImpl();
      WorkflowConfigJpa workflowConfig = new WorkflowConfigJpa();
      workflowConfig.setType(WorkflowBinType.MUTUALLY_EXCLUSIVE);
      workflowConfig.setMutuallyExclusive(true);
      workflowConfig.setProjectId(project1.getId());
      workflowConfig.setTimestamp(startDate);
      workflowConfig.setLastPartitionTime(1L);
      workflowService = new WorkflowServiceRestImpl();
      WorkflowConfig addedWorkflowConfig =
          workflowService.addWorkflowConfig(project1.getId(), workflowConfig,
              authToken);

      // Add a workflow definition (as SQL)
      // TODO: create workflow bin definitions exactly matching NCI-META config
      // also
      getLog().info("  Create a workflow definition");
      WorkflowBinDefinitionJpa workflowBinDefinition =
          new WorkflowBinDefinitionJpa();
      workflowBinDefinition.setName("testName");
      workflowBinDefinition.setDescription("test description");
      workflowBinDefinition
          .setQuery("select distinct c.id clusterId, c.id conceptId from concepts c where c.name like '%Amino%';");
      workflowBinDefinition.setEditable(true);
      workflowBinDefinition.setQueryType(QueryType.SQL);
      workflowBinDefinition.setTimestamp(startDate);
      workflowBinDefinition.setWorkflowConfig(addedWorkflowConfig);

      workflowService = new WorkflowServiceRestImpl();
      workflowService.addWorkflowBinDefinition(project1.getId(),
          addedWorkflowConfig.getId(), workflowBinDefinition, authToken);

      // Add a second workflow definition
      getLog().info("  Create a second workflow definition");
      WorkflowBinDefinitionJpa workflowBinDefinition2 =
          new WorkflowBinDefinitionJpa();
      workflowBinDefinition2.setName("testName2");
      workflowBinDefinition2.setDescription("test description2");
      workflowBinDefinition2
          .setQuery("select distinct c.id clusterId, c.id conceptId from concepts c where c.name like '%Acid%';");
      workflowBinDefinition2.setEditable(true);
      workflowBinDefinition2.setQueryType(QueryType.SQL);
      workflowBinDefinition2.setTimestamp(startDate);
      workflowBinDefinition2.setWorkflowConfig(addedWorkflowConfig);

      workflowService = new WorkflowServiceRestImpl();
      workflowService.addWorkflowBinDefinition(project1.getId(),
          addedWorkflowConfig.getId(), workflowBinDefinition2, authToken);
      
      
      

      getLog().info("done ...");
    } catch (Exception e) {
      e.printStackTrace();
      throw new MojoFailureException("Unexpected exception:", e);
    }
  }

}
