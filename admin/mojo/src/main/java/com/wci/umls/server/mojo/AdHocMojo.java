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

import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;

import com.wci.umls.server.Project;
import com.wci.umls.server.User;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.services.ProjectServiceJpa;
import com.wci.umls.server.jpa.services.SecurityServiceJpa;
import com.wci.umls.server.jpa.services.rest.SecurityServiceRest;
import com.wci.umls.server.model.workflow.WorkflowBin;
import com.wci.umls.server.model.workflow.WorkflowBinType;
import com.wci.umls.server.rest.impl.IntegrationTestServiceRestImpl;
import com.wci.umls.server.rest.impl.SecurityServiceRestImpl;
import com.wci.umls.server.rest.impl.WorkflowServiceRestImpl;
import com.wci.umls.server.services.ProjectService;
import com.wci.umls.server.services.SecurityService;

/**
 * Goal which executes operations on the db to create and remove test conditions.
 * 
 * See admin/loader/pom.xml for sample usage
 * 
 * @goal ad-hoc
 * @phase package
 */
public class AdHocMojo extends AbstractMojo {

  /**
   * Mode - operation to execute
   * @parameter
   */
  private String mode = null;
  
  /**
   * Project id
   * @parameter
   */
  private Long projectId = null;
  
  /**
   * Id for operation, i.e. worklist or checklist id
   * @parameter
   */
  private Long componentId = null;
  
  /**
   * WorkflowBinType - for determining workflow bin 
   * @parameter
   */
  private WorkflowBinType type = null;



  /**
   * Instantiates a {@link AdHocMojo} from the specified
   * parameters.
   */
  public AdHocMojo() {
    // do nothing
  }

  /* see superclass */
  @Override
  public void execute() throws MojoFailureException {
    getLog().info("Ad hoc mojo");

    try {

      getLog().info("Ad hoc mojo");
      getLog().info("  mode = " + mode);
      getLog().info("  projectId = " + projectId);
      getLog().info("  componentId = " + componentId);
      getLog().info("  type = " + type);

      final Properties properties = ConfigUtility.getConfigProperties();

      // authenticate
      final SecurityService service = new SecurityServiceJpa();
      service.authenticate(properties.getProperty("admin.user"),
              properties.getProperty("admin.password")).getAuthToken();
      service.close();

      boolean serverRunning = ConfigUtility.isServerActive();
      getLog().info(
          "Server status detected:  " + (!serverRunning ? "DOWN" : "UP"));
      if (!serverRunning) {
        throw new Exception("Server must be running to run operation");
      }

      executeAction();

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
  private void executeAction() throws Exception {

    // Initialize
    Logger.getLogger(getClass()).info("Authenticate admin user");
    SecurityServiceRest security = new SecurityServiceRestImpl();
    User admin = security.authenticate("admin", "admin");
    final ProjectService projectService = new ProjectServiceJpa();
    Project project = null;
    if (projectId != null) {
      project = projectService.getProject(projectId);
    } else {
      project = projectService.getProjects().getObjects().get(0);
    }
    projectService.close();

    WorkflowServiceRestImpl workflowService = new WorkflowServiceRestImpl();
    
    List<WorkflowBin> bins = workflowService.getWorkflowBins(projectId, type, admin.getAuthToken());
    

    if (mode.equals("RegenerateBins")) {
      try {
        workflowService.regenerateBins(project.getId(),
            WorkflowBinType.MUTUALLY_EXCLUSIVE, admin.getAuthToken());
      } catch (Exception e) {
        workflowService.clearBins(project.getId(),
            WorkflowBinType.MUTUALLY_EXCLUSIVE, admin.getAuthToken());
        throw e;
      }
    } else if (mode.equals("ClearBins")) {
      workflowService.clearBins(project.getId(), WorkflowBinType.MUTUALLY_EXCLUSIVE, 
          admin.getAuthToken());
    } else if (mode.equals("CreateChecklist")) {
      PfsParameterJpa pfs = new PfsParameterJpa();
      pfs.setMaxResults(5);
      workflowService.createChecklist(project.getId(), bins.get(0).getId(),
              "checklistOrderByClusterId", false, false, "clusterType:chem",
              pfs, admin.getAuthToken());
    } else if (mode.equals("RemoveChecklist")) {
      workflowService.removeChecklist(componentId, admin.getAuthToken());
    } else if (mode.equals("CreateWorklist")) {
      workflowService.createWorklist(project.getId(), bins.get(0).getId(),
              "chem", 0, 5, new PfsParameterJpa(), admin.getAuthToken());
    } else if (mode.equals("RemoveWorklist")) {
      IntegrationTestServiceRestImpl integrationTestService = new IntegrationTestServiceRestImpl();
      integrationTestService.removeWorklist(componentId, true, admin.getAuthToken());
    }
    

  }


}
