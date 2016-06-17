package com.wci.umls.server.rest.impl;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import com.wci.umls.server.UserRole;
import com.wci.umls.server.jpa.services.SecurityServiceJpa;
import com.wci.umls.server.jpa.services.WorkflowServiceJpa;
import com.wci.umls.server.jpa.services.rest.WorkflowServiceRest;
import com.wci.umls.server.jpa.worfklow.WorkflowBinDefinitionJpa;
import com.wci.umls.server.jpa.worfklow.WorkflowConfigJpa;
import com.wci.umls.server.model.workflow.WorkflowBinDefinition;
import com.wci.umls.server.model.workflow.WorkflowBinType;
import com.wci.umls.server.model.workflow.WorkflowConfig;
import com.wci.umls.server.services.SecurityService;
import com.wci.umls.server.services.WorkflowService;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * REST implementation for {@link WorkflowServiceRest}.
 */
@Path("/workflow")
@Api(value = "/workflow", description = "Operations supporting workflow")
@Consumes({
  MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_HTML
})
@Produces({
    MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
})

public class WorkflowServiceRestImpl extends RootServiceRestImpl implements WorkflowServiceRest {


  /** The security service. */
  private SecurityService securityService;

  /**
   * Instantiates an empty {@link WorkflowServiceRestImpl}.
   *
   * @throws Exception the exception
   */
  public WorkflowServiceRestImpl() throws Exception {
    securityService = new SecurityServiceJpa();
  }
  
  @Override
  @POST
  @Path("/config/add")
  @ApiOperation(value = "Add a workflow config", notes = "Add a workflow config", response = WorkflowConfigJpa.class)
  public WorkflowConfig addWorkflowConfig(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Workflow config to add", required = true) WorkflowConfigJpa workflowConfig,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful POST call (Workflow): /config/add/" + projectId + " "
            + workflowConfig.toString() + " " + authToken);

    String action = "trying to add workflow config";

    WorkflowService workflowService = new WorkflowServiceJpa();

    try {

      // authorize and get user name from the token
      authorizeProject(workflowService, projectId,
          securityService, authToken, action, UserRole.AUTHOR);

      // TODO: should this be necessary to set last modified by?
      workflowService.setLastModifiedBy(workflowConfig.getLastModifiedBy());
      return workflowService.addWorkflowConfig(workflowConfig);
      
    } catch (Exception e) {
      handleException(e, "trying to add workflow config");
      return null;
    } finally {
      workflowService.close();
      securityService.close();
    }

  }

  
  @Override
  @POST
  @Path("/config/update")
  @ApiOperation(value = "Update a workflow config", notes = "Update a workflow config")
  public void updateWorkflowConfig(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Workflow config to update", required = true) WorkflowConfigJpa config,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful POST call (Workflow): /config/update/" + projectId + " "
            + config.getId() + " " + authToken);

    String action = "trying to update workflow config";

    WorkflowService workflowService = new WorkflowServiceJpa();

    try {

      // authorize and get user name from the token
      authorizeProject(workflowService, projectId,
          securityService, authToken, action, UserRole.AUTHOR);

      // TODO: should this be necessary to set last modified by?
      workflowService.setLastModifiedBy(config.getLastModifiedBy());
      workflowService.updateWorkflowConfig(config);
    } catch (Exception e) {
      handleException(e, "trying to update workflow config");
    } finally {
      workflowService.close();
      securityService.close();
    }

  }

  
  @Override
  @DELETE
  @Path("/config/{id}/remove")
  @ApiOperation(value = "Remove a workflow config", notes = "Remove a workflow config")
  public void removeWorkflowConfig(
    @ApiParam(value = "Workflow config id, e.g. 1", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
      throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Workflow): /config/remove");

    WorkflowService workflowService = new WorkflowServiceJpa();
    try {

      authorizeApp(securityService, authToken, "remove workflow config",
          UserRole.USER);

      // TODO: should this be necessary to set last modified by? if so, need to get actual workflow config
      workflowService.setLastModifiedBy("admin");
      workflowService.removeWorkflowConfig(id);
    } catch (Exception e) {

      handleException(e, "trying to remove a workflow config");
    } finally {
      workflowService.close();
      securityService.close();
    }

  }


  
  @Override
  @POST
  @Path("/definition/add")
  @ApiOperation(value = "Add a workflow bin definition", notes = "Add a workflow bin definition", response = WorkflowBinDefinitionJpa.class)
  public WorkflowBinDefinition addWorkflowBinDefinition(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Workflow config id, e.g. 1", required = true) @QueryParam("configId") Long configId,
    @ApiParam(value = "Workflow bin definition to add", required = true) WorkflowBinDefinitionJpa binDefinition,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful POST call (Workflow): /definition/add/" + projectId + " "
            + configId + " " + binDefinition.getName() + " " + authToken);

    String action = "trying to add workflow bin definition";

    WorkflowService workflowService = new WorkflowServiceJpa();

    try {

      // authorize and get user name from the token
      authorizeProject(workflowService, projectId,
          securityService, authToken, action, UserRole.AUTHOR);

      // TODO: should this be necessary to set last modified by?
      workflowService.setLastModifiedBy(binDefinition.getLastModifiedBy());
      
      WorkflowConfig workflowConfig = workflowService.getWorkflowConfig(configId);
      binDefinition.setWorkflowConfig(workflowConfig);
      return workflowService.addWorkflowBinDefinition(binDefinition);
    } catch (Exception e) {
      handleException(e, "trying to add workflow bin definition");
      return null;
    } finally {
      workflowService.close();
      securityService.close();
    }

  }

  @Override
  @POST
  @Path("/definition/update")
  @ApiOperation(value = "Update a workflow bin definition", notes = "Update a workflow bin definition")
  public void updateWorkflowBinDefinition(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Workflow bin definition to update", required = true) WorkflowBinDefinitionJpa binDefinition, @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful POST call (Workflow): /definition/update  " + projectId + " "
            + binDefinition.getId() + " " + authToken);

    String action = "trying to update workflow bin definition";

    WorkflowService workflowService = new WorkflowServiceJpa();

    try {

      // authorize and get user name from the token
      authorizeProject(workflowService, projectId,
          securityService, authToken, action, UserRole.AUTHOR);

      // TODO: should this be necessary to set last modified by?
      workflowService.setLastModifiedBy(binDefinition.getLastModifiedBy());
      
      WorkflowBinDefinition origBinDefinition = workflowService.getWorkflowBinDefinition(binDefinition.getId());
      WorkflowConfig workflowConfig = workflowService.getWorkflowConfig(origBinDefinition.getWorkflowConfig().getId());
      binDefinition.setWorkflowConfig(workflowConfig);
      workflowService.updateWorkflowBinDefinition(binDefinition);
      
    } catch (Exception e) {
      handleException(e, "trying to update workflow bin definition");
    } finally {
      workflowService.close();
      securityService.close();
    }

  }



  @Override
  @DELETE
  @Path("/definition/{id}/remove")
  @ApiOperation(value = "Remove a workflow bin definition", notes = "Remove a workflow bin definition")
  public void removeWorkflowBinDefinition(
    @ApiParam(value = "Project id, e.g. 1", required = true) @PathParam("projectId") Long projectId,
    @ApiParam(value = "Workflow bin definition id, e.g. 1", required = true) @PathParam("id") Long binDefinitionId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
      throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Workflow): /definition/remove");
   
    WorkflowService workflowService = new WorkflowServiceJpa();
    try {

      authorizeApp(securityService, authToken, "remove workflow bin definition",
          UserRole.USER);

      // TODO: should this be necessary to set last modified by? if so, need to get actual workflow config
      workflowService.setLastModifiedBy("admin");
      // load the bin definition, get its workflow config. Remove it from workflow config, then remove it.      
      WorkflowBinDefinition binDefinition = workflowService.getWorkflowBinDefinition(binDefinitionId);
      WorkflowConfig workflowConfig = workflowService.getWorkflowConfig(binDefinition.getWorkflowConfig().getId());
      List<WorkflowBinDefinition> defsToKeep = new ArrayList<>();
      for (WorkflowBinDefinition def : workflowConfig.getWorkflowBinDefinitions()) {
        if (def.getId() != binDefinitionId) {
          defsToKeep.add(def);
        }
      }
      workflowConfig.setWorkflowBinDefinitions(defsToKeep);
      workflowService.updateWorkflowConfig(workflowConfig);
      workflowService.removeWorkflowBinDefinition(binDefinitionId);
    } catch (Exception e) {

      handleException(e, "trying to remove a workflow bin definition");
    } finally {
      workflowService.close();
      securityService.close();
    }

  }

  
  @Override
  public void regenerateBins(Long projectId, WorkflowBinType type,
    String authToken) throws Exception {
    // TODO Auto-generated method stub

  }

}
