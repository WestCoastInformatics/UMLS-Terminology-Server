package com.wci.umls.server.rest.impl;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import com.wci.umls.server.Project;
import com.wci.umls.server.UserRole;
import com.wci.umls.server.helpers.ChecklistList;
import com.wci.umls.server.helpers.StringList;
import com.wci.umls.server.helpers.TrackingRecordList;
import com.wci.umls.server.helpers.WorklistList;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.helpers.TrackingRecordListJpa;
import com.wci.umls.server.jpa.helpers.WorklistListJpa;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.jpa.services.SecurityServiceJpa;
import com.wci.umls.server.jpa.services.WorkflowServiceJpa;
import com.wci.umls.server.jpa.services.rest.WorkflowServiceRest;
import com.wci.umls.server.jpa.worfklow.TrackingRecordJpa;
import com.wci.umls.server.jpa.worfklow.WorkflowBinDefinitionJpa;
import com.wci.umls.server.jpa.worfklow.WorkflowConfigJpa;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.workflow.TrackingRecord;
import com.wci.umls.server.model.workflow.WorkflowAction;
import com.wci.umls.server.model.workflow.WorkflowBinDefinition;
import com.wci.umls.server.model.workflow.WorkflowBinType;
import com.wci.umls.server.model.workflow.WorkflowConfig;
import com.wci.umls.server.model.workflow.Worklist;
import com.wci.umls.server.services.ContentService;
import com.wci.umls.server.services.SecurityService;
import com.wci.umls.server.services.WorkflowService;
import com.wci.umls.server.services.handlers.WorkflowActionHandler;
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

      WorkflowConfig workflowConfig = workflowService.getWorkflowConfig(id);
      workflowService.setLastModifiedBy(workflowConfig.getLastModifiedBy());
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

      // load the bin definition, get its workflow config. Remove it from workflow config, then remove it.      
      WorkflowBinDefinition binDefinition = workflowService.getWorkflowBinDefinition(binDefinitionId);
      WorkflowConfig workflowConfig = workflowService.getWorkflowConfig(binDefinition.getWorkflowConfig().getId());
      workflowService.setLastModifiedBy(workflowConfig.getLastModifiedBy());
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


    @Override
    @POST
    @Path("/records/assigned")
    @ApiOperation(value = "Find assigned work", notes = "Finds tracking records assigned", response = TrackingRecordListJpa.class)
    public TrackingRecordList findAssignedWork(
      @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
      @ApiParam(value = "User name", required = false) @QueryParam("userName") String userName,
      @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
      @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
      throws Exception {
      Logger.getLogger(getClass()).info(
          "RESTful POST call (Workflow): /refset/assigned ");

      final WorkflowService workflowService = new WorkflowServiceJpa();
      try {
        authorizeProject(workflowService, projectId, securityService, authToken,
            "trying to find assigned work", UserRole.AUTHOR);

        Project project = workflowService.getProject(projectId);
        
        // Get available tracking records
        WorkflowActionHandler handler = workflowService.getWorkflowHandlerForPath(project.getWorkflowPath());
        TrackingRecordList trackingRecords = handler.findAssignedWork(project, userName, pfs, workflowService);

        for (final TrackingRecord tr : trackingRecords.getObjects()) {
          workflowService.handleLazyInit(tr);
        }
        return trackingRecords;
      } catch (Exception e) {
        handleException(e, "trying to find assigned work");
      } finally {
        workflowService.close();
        securityService.close();
      }
      return null;
    }

  
    @Override
    @POST
    @Path("/records/available")
    @ApiOperation(value = "Find available  work", notes = "Finds tracking records available for work", response = TrackingRecordListJpa.class)
    public TrackingRecordList findAvailableWork(
      @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
      @ApiParam(value = "UserRole", required = false) @QueryParam("role") UserRole role,
      @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
      @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
      throws Exception {
      Logger.getLogger(getClass()).info(
          "RESTful POST call (Workflow): /records/available ");
      
      final WorkflowService workflowService = new WorkflowServiceJpa();
      try {
        authorizeProject(workflowService, projectId, securityService, authToken,
            "trying to find available work", UserRole.AUTHOR);

        Project project = workflowService.getProject(projectId);
        
        // Get available tracking records
        WorkflowActionHandler handler = workflowService.getWorkflowHandlerForPath(project.getWorkflowPath());
        TrackingRecordList trackingRecords = handler.findAvailableWork(project, role, pfs, workflowService);

        for (final TrackingRecord tr : trackingRecords.getObjects()) {
          workflowService.handleLazyInit(tr);
        }
        return trackingRecords;
      } catch (Exception e) {
        handleException(e, "trying to find available work");
      } finally {
        workflowService.close();
        securityService.close();
      }
      return null;
    }

    
    @Override
    @POST
    @Path("/worklists/assigned")
    @ApiOperation(value = "Find assigned  worklists", notes = "Finds worklists assigned for work", response = WorklistListJpa.class)
    public WorklistList findAssignedWorklists(
      @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
      @ApiParam(value = "User name", required = false) @QueryParam("userName") String userName,
      @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
      @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
      throws Exception {
      Logger.getLogger(getClass()).info(
          "RESTful POST call (Workflow): /worklists/assigned ");

      final WorkflowService workflowService = new WorkflowServiceJpa();
      try {
        authorizeProject(workflowService, projectId, securityService, authToken,
            "trying to find assigned worklists", UserRole.AUTHOR);

        Project project = workflowService.getProject(projectId);
        
        WorkflowActionHandler handler = workflowService.getWorkflowHandlerForPath(project.getWorkflowPath());
        WorklistList list = handler.findAssignedWorklists(project, userName, pfs, workflowService);

        return list;
      } catch (Exception e) {
        handleException(e, "trying to find assigned worklists");
      } finally {
        workflowService.close();
        securityService.close();
      }
      return null;
    }

  @Override
  @POST
  @Path("/checklists")
  public ChecklistList findChecklists(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Query", required = false) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful POST call (Workflow): /checklists/" + projectId + " "
           + query + " " + authToken);

    String action = "trying to find checklists";

    WorkflowService workflowService = new WorkflowServiceJpa();

    try {

      // authorize and get user name from the token
      authorizeProject(workflowService, projectId,
          securityService, authToken, action, UserRole.AUTHOR);
      Project project = workflowService.getProject(projectId);

      return workflowService.findChecklistsForQuery(query, pfs);
      
    } catch (Exception e) {
      handleException(e, "trying to find checklists");
      return null;
    } finally {
      workflowService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @GET
  @Path("/paths")
  @ApiOperation(value = "Get workflow paths", notes = "Gets the supported workflow paths", response = StringList.class)
  public StringList getWorkflowPaths(
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful POST call (Workflow): /paths");

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeApp(securityService, authToken, "get workflow paths",
          UserRole.VIEWER);

      return workflowService.getWorkflowPaths();

    } catch (Exception e) {
      handleException(e, "trying to get workflow paths");
    } finally {
      workflowService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @POST
  @Path("/action")
  @ApiOperation(value = "Perform workflow action on a tracking record", notes = "Performs the specified action as the specified refset as the specified user", response = TrackingRecordJpa.class)
  public Worklist performWorkflowAction(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Worklist id, e.g. 5", required = false) @QueryParam("worklistId") Long worklistId,
    @ApiParam(value = "User name, e.g. author1", required = true) @QueryParam("userName") String userName,
    @ApiParam(value = "User role, e.g. AUTHOR", required = true)  UserRole role,
    @ApiParam(value = "Workflow action, e.g. 'SAVE'", required = true) WorkflowAction action,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /action " + action + ", "
            + projectId + ", " + userName);

    // Test preconditions
    if (projectId == null ||  userName == null) {
      handleException(new Exception("Required parameter has a null value"), "");
    }

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      final String authName =
          authorizeProject(workflowService, projectId, securityService,
              authToken, "perform workflow action",
              UserRole.AUTHOR);

      // Need to read the accurate, current workflow state of the concept if it exists
      /*if (trackingRecord.getId() != null) {
        final Concept c2 = workflowService.getConcept(trackingRecord.getId());
        trackingRecord.setWorkflowStatus(c2.getWorkflowStatus());
      } else {
        trackingRecord.setWorkflowStatus(WorkflowStatus.NEW);
      }*/
      Worklist worklist = workflowService.getWorklist(worklistId);
      Project project = workflowService.getProject(projectId);
      // Set last modified by
      worklist.setLastModifiedBy(authName);
      Worklist returnWorklist = workflowService.performWorkflowAction(project, worklist, userName,
              role, action);

      /* TODO addLogEntry(workflowService, userName, "WORKFLOW action", projectId,
          translationId, action + " as " + projectRole + " on concept "
              + trackingRecord.getTerminologyId() + ", " + trackingRecord.getName());
*/
      //handleLazyInit(record, workflowService);

      return returnWorklist;
    } catch (Exception e) {
      handleException(e, "trying to perform workflow action");
    } finally {
      workflowService.close();
      securityService.close();
    }
    return null;
  }




    
    
  @Override
  @GET
  @Path("/records")
  @ApiOperation(value = "Get the tracking records for concept", notes = "Gets the tracking records for the specified concept", response = TrackingRecordListJpa.class)
  public TrackingRecordList getTrackingRecordsForConcept(
    @ApiParam(value = "Concept id, e.g. 5", required = false) @QueryParam("conceptId") Long conceptId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /records" + ", " + conceptId);

    // Test preconditions
    if (conceptId == null) {
      handleException(new Exception("Required parameter has a null value"), "");
    }

    ContentService contentService = new ContentServiceJpa();
    try {
      final Concept concept = contentService.getConcept(conceptId);

      if (concept != null) {
        /* TODO no project id available
         * authorizeProject(contentService, concept.getProject().getId(),
            securityService, authToken, "get tracking records for concept",
            UserRole.AUTHOR);*/
        
        authorizeApp(securityService, authToken, "get tracking records for concept",
            UserRole.AUTHOR);

        TrackingRecordList record =
            getTrackingRecordsForConcept(conceptId, null);
        
        return record;
      }

      return null;
    } catch (Exception e) {
      handleException(e, "trying to get tracking records for refset");
    } finally {
      contentService.close();
      securityService.close();
    }
    return null;
  }


  @Override
  @POST
  @Path("/worklists/available")
  @ApiOperation(value = "Find available  worklists", notes = "Finds worklists available for work", response = WorklistListJpa.class)
  public WorklistList findAvailableWorklists(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "UserRole", required = false) @QueryParam("role") UserRole role,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /worklists/available ");

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeProject(workflowService, projectId, securityService, authToken,
          "trying to find available worklists", UserRole.AUTHOR);

      Project project = workflowService.getProject(projectId);
      
      WorkflowActionHandler handler = workflowService.getWorkflowHandlerForPath(project.getWorkflowPath());
      WorklistList list = handler.findAvailableWorklists(project, role, pfs, workflowService);

      return list;
    } catch (Exception e) {
      handleException(e, "trying to find available worklists");
    } finally {
      workflowService.close();
      securityService.close();
    }
    return null;
  }


}
