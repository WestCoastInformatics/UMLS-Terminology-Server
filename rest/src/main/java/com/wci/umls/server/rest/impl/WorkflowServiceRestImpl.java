package com.wci.umls.server.rest.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

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
import com.wci.umls.server.User;
import com.wci.umls.server.UserRole;
import com.wci.umls.server.helpers.ChecklistList;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.SearchResult;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.helpers.StringList;
import com.wci.umls.server.helpers.TrackingRecordList;
import com.wci.umls.server.helpers.WorklistList;
import com.wci.umls.server.jpa.helpers.ChecklistListJpa;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.helpers.TrackingRecordListJpa;
import com.wci.umls.server.jpa.helpers.WorklistListJpa;
import com.wci.umls.server.jpa.services.ReportServiceJpa;
import com.wci.umls.server.jpa.services.SecurityServiceJpa;
import com.wci.umls.server.jpa.services.WorkflowServiceJpa;
import com.wci.umls.server.jpa.services.rest.WorkflowServiceRest;
import com.wci.umls.server.jpa.worfklow.ChecklistJpa;
import com.wci.umls.server.jpa.worfklow.ClusterTypeStatsJpa;
import com.wci.umls.server.jpa.worfklow.TrackingRecordJpa;
import com.wci.umls.server.jpa.worfklow.WorkflowBinDefinitionJpa;
import com.wci.umls.server.jpa.worfklow.WorkflowBinJpa;
import com.wci.umls.server.jpa.worfklow.WorkflowConfigJpa;
import com.wci.umls.server.jpa.worfklow.WorkflowEpochJpa;
import com.wci.umls.server.jpa.worfklow.WorklistJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.SemanticTypeComponent;
import com.wci.umls.server.model.workflow.Checklist;
import com.wci.umls.server.model.workflow.ClusterTypeStats;
import com.wci.umls.server.model.workflow.TrackingRecord;
import com.wci.umls.server.model.workflow.WorkflowAction;
import com.wci.umls.server.model.workflow.WorkflowBin;
import com.wci.umls.server.model.workflow.WorkflowBinDefinition;
import com.wci.umls.server.model.workflow.WorkflowBinType;
import com.wci.umls.server.model.workflow.WorkflowConfig;
import com.wci.umls.server.model.workflow.WorkflowEpoch;
import com.wci.umls.server.model.workflow.WorkflowStatus;
import com.wci.umls.server.model.workflow.Worklist;
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
public class WorkflowServiceRestImpl extends RootServiceRestImpl implements
    WorkflowServiceRest {

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

  /* see superclass */
  @POST
  @Path("/config/add")
  @ApiOperation(value = "Add a workflow config", notes = "Add a workflow config", response = WorkflowConfigJpa.class)
  @Override
  public WorkflowConfig addWorkflowConfig(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Workflow config to add", required = true) WorkflowConfigJpa workflowConfig,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /config/add/" + projectId + " "
            + workflowConfig.toString() + " " + authToken);

    final String action = "trying to add workflow config";
    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      // authorize and get user name from the token
      final String userName =
          authorizeProject(workflowService, projectId, securityService,
              authToken, action, UserRole.AUTHOR);
      workflowService.setLastModifiedBy(userName);

      return workflowService.addWorkflowConfig(workflowConfig);

    } catch (Exception e) {
      handleException(e, "trying to " + action);
      return null;
    } finally {
      workflowService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @POST
  @Path("/config/update")
  @ApiOperation(value = "Update a workflow config", notes = "Update a workflow config")
  public void updateWorkflowConfig(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Workflow config to update", required = true) WorkflowConfigJpa config,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /config/update/" + projectId + " "
            + config.getId() + " " + authToken);

    final String action = "trying to update workflow config";
    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      // authorize and get user name from the token
      final String userName =
          authorizeProject(workflowService, projectId, securityService,
              authToken, action, UserRole.AUTHOR);
      workflowService.setLastModifiedBy(userName);

      workflowService.updateWorkflowConfig(config);

    } catch (Exception e) {
      handleException(e, "trying to " + action);
    } finally {
      workflowService.close();
      securityService.close();
    }

  }

  /* see superclass */
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

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      final String userName =
          authorizeApp(securityService, authToken, "remove workflow config",
              UserRole.USER);
      workflowService.setLastModifiedBy(userName);

      workflowService.removeWorkflowConfig(id);

    } catch (Exception e) {
      handleException(e, "trying to remove a workflow config");
    } finally {
      workflowService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @DELETE
  @Path("/checklist/{id}/remove")
  @ApiOperation(value = "Remove a checklist", notes = "Remove a checklist")
  public void removeChecklist(
    @ApiParam(value = "Checklist id, e.g. 1", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call (Workflow): /checklist/" + id + "/remove");

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      final String userName =
          authorizeApp(securityService, authToken, "remove checklist",
              UserRole.USER);
      workflowService.setLastModifiedBy(userName);

      workflowService.removeChecklist(id, true);

    } catch (Exception e) {
      handleException(e, "trying to remove a checklist");
    } finally {
      workflowService.close();
      securityService.close();
    }
  }

  /* see superclass */
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
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /definition/add/" + projectId + " "
            + configId + " " + binDefinition.getName() + " " + authToken);

    final String action = "trying to add workflow bin definition";
    final WorkflowService workflowService = new WorkflowServiceJpa();

    try {
      // authorize and get user name from the token
      final String userName =
          authorizeProject(workflowService, projectId, securityService,
              authToken, action, UserRole.AUTHOR);
      workflowService.setLastModifiedBy(userName);

      final WorkflowConfig workflowConfig =
          workflowService.getWorkflowConfig(configId);
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

  /* see superclass */
  @Override
  @POST
  @Path("/epoch/add")
  @ApiOperation(value = "Add a workflow epoch", notes = "Add a workflow epoch", response = WorkflowEpochJpa.class)
  public WorkflowEpoch addWorkflowEpoch(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Workflow epoch to add", required = true) WorkflowEpochJpa epoch,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /epoch/add/" + projectId + " "
            + epoch.getName() + " " + authToken);

    final String action = "trying to add workflow bin definition";
    final WorkflowService workflowService = new WorkflowServiceJpa();

    try {
      // authorize and get user name from the token
      final String userName =
          authorizeProject(workflowService, projectId, securityService,
              authToken, action, UserRole.AUTHOR);
      workflowService.setLastModifiedBy(userName);

      return workflowService.addWorkflowEpoch(epoch);

    } catch (Exception e) {
      handleException(e, "trying to add workflow bin definition");
      return null;
    } finally {
      workflowService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @POST
  @Path("/definition/update")
  @ApiOperation(value = "Update a workflow bin definition", notes = "Update a workflow bin definition")
  public void updateWorkflowBinDefinition(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Workflow bin definition to update", required = true) WorkflowBinDefinitionJpa binDefinition,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /definition/update  " + projectId + " "
            + binDefinition.getId() + " " + authToken);

    final String action = "trying to update workflow bin definition";
    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      // authorize and get user name from the token
      String userName =
          authorizeProject(workflowService, projectId, securityService,
              authToken, action, UserRole.AUTHOR);
      workflowService.setLastModifiedBy(userName);

      final WorkflowBinDefinition origBinDefinition =
          workflowService.getWorkflowBinDefinition(binDefinition.getId());
      binDefinition.setWorkflowConfig(origBinDefinition.getWorkflowConfig());
      workflowService.updateWorkflowBinDefinition(binDefinition);

    } catch (Exception e) {
      handleException(e, "trying to update workflow bin definition");
    } finally {
      workflowService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @DELETE
  @Path("/definition/{id}/remove")
  @ApiOperation(value = "Remove a workflow bin definition", notes = "Remove a workflow bin definition")
  public void removeWorkflowBinDefinition(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Workflow bin definition id, e.g. 1", required = true) @PathParam("id") Long binDefinitionId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call (Workflow): /definition/remove");

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      final String userName =
          authorizeApp(securityService, authToken,
              "remove workflow bin definition", UserRole.USER);
      workflowService.setLastModifiedBy(userName);

      // load the bin definition, get its workflow config. Remove it from
      // workflow config, then remove it.
      WorkflowBinDefinition binDefinition =
          workflowService.getWorkflowBinDefinition(binDefinitionId);

      WorkflowConfig workflowConfig = binDefinition.getWorkflowConfig();
      workflowConfig.getWorkflowBinDefinitions().remove(binDefinition);
      workflowService.updateWorkflowConfig(workflowConfig);
      workflowService.removeWorkflowBinDefinition(binDefinitionId);

    } catch (Exception e) {
      handleException(e, "trying to remove a workflow bin definition");
    } finally {
      workflowService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @POST
  @Path("/bin/clear/all")
  @ApiOperation(value = "Clear bins", notes = "Clear bins")
  public void clearBins(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Workflow bin type", required = true) WorkflowBinType type,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /bin/clear/all ");

    final WorkflowServiceJpa workflowService = new WorkflowServiceJpa();
    try {
      final String userName =
          authorizeProject(workflowService, projectId, securityService,
              authToken, "trying to clear bins", UserRole.AUTHOR);
      workflowService.setLastModifiedBy(userName);

      final Project project = workflowService.getProject(projectId);
      final List<WorkflowBin> results =
          workflowService.getWorkflowBins(project, type);

      // remove bins and all of the tracking records in the bins
      for (final WorkflowBin workflowBin : results) {
        workflowService.removeWorkflowBin(workflowBin.getId(), true);
      }

    } catch (Exception e) {
      handleException(e, "trying to clear bins");
    } finally {
      workflowService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @POST
  @Path("/bin/regenerate/all")
  @ApiOperation(value = "Regenerate bins", notes = "Regenerate bins")
  public void regenerateBins(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Workflow bin type", required = true) WorkflowBinType type,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /bin/regenerate/all ");

    final WorkflowServiceJpa workflowService = new WorkflowServiceJpa();
    try {
      final String userName =
          authorizeProject(workflowService, projectId, securityService,
              authToken, "trying to regenerate bins", UserRole.AUTHOR);
      workflowService.setLastModifiedBy(userName);

      // Set transaction mode
      workflowService.setTransactionPerOperation(true);
      workflowService.beginTransaction();

      // Load the project and workflow config
      final Project project = workflowService.getProject(projectId);
      final WorkflowConfig workflowConfig =
          workflowService.getWorkflowConfig(project, type);

      // Start by clearing the bins
      // remove bins and all of the tracking records in the bins
      final List<WorkflowBin> results =
          workflowService.getWorkflowBins(project, type);
      for (final WorkflowBin workflowBin : results) {
        workflowService.removeWorkflowBin(workflowBin.getId(), true);
      }

      // concepts seen set
      final Set<Long> conceptsSeen = new HashSet<>();
      final Map<Long, String> conceptIdWorklistNameMap =
          getConceptIdWorklistNameMap(project, workflowService);

      // Look up the bin definitions
      int rank = 0;
      for (final WorkflowBinDefinition definition : workflowConfig
          .getWorkflowBinDefinitions()) {

        // regenerate bins
        regenerateBinHelper(project, definition, ++rank, conceptsSeen,
            conceptIdWorklistNameMap, workflowService);
      }

      workflowService.commit();
    } catch (Exception e) {
      handleException(e, "trying to regenerate bins");
    } finally {
      workflowService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @POST
  @Path("/record/assigned")
  @ApiOperation(value = "Find assigned work", notes = "Finds tracking records assigned", response = TrackingRecordListJpa.class)
  public TrackingRecordList findAssignedWork(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "User name", required = false) @QueryParam("userName") String userName,
    @ApiParam(value = "User role, e.g. AUTHOR", required = false) @QueryParam("role") UserRole role,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /record/assigned ");

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeProject(workflowService, projectId, securityService, authToken,
          "trying to find assigned work", UserRole.AUTHOR);

      final Project project = workflowService.getProject(projectId);

      // find available tracking records
      final WorkflowActionHandler handler =
          workflowService.getWorkflowHandlerForPath(project.getWorkflowPath());
      final TrackingRecordList trackingRecords =
          handler.findAssignedWork(project, userName, role, pfs,
              workflowService);

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

  /* see superclass */
  @Override
  @POST
  @Path("/record/available")
  @ApiOperation(value = "Find available work", notes = "Finds tracking records available for work", response = TrackingRecordListJpa.class)
  public TrackingRecordList findAvailableWork(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "UserRole", required = false) @QueryParam("role") UserRole role,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /record/available ");

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeProject(workflowService, projectId, securityService, authToken,
          "trying to find available work", UserRole.AUTHOR);

      final Project project = workflowService.getProject(projectId);

      // find available tracking records
      final WorkflowActionHandler handler =
          workflowService.getWorkflowHandlerForPath(project.getWorkflowPath());
      final TrackingRecordList trackingRecords =
          handler.findAvailableWork(project, role, pfs, workflowService);
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

  /* see superclass */
  @Override
  @POST
  @Path("/worklist/assigned")
  @ApiOperation(value = "Find assigned worklists", notes = "Finds worklists assigned for work", response = WorklistListJpa.class)
  public WorklistList findAssignedWorklists(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "User name", required = false) @QueryParam("userName") String userName,
    @ApiParam(value = "User role, e.g. AUTHOR", required = false) @QueryParam("role") UserRole role,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /worklist/assigned ");

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeProject(workflowService, projectId, securityService, authToken,
          "trying to find assigned worklists", UserRole.AUTHOR);

      final Project project = workflowService.getProject(projectId);

      final WorkflowActionHandler handler =
          workflowService.getWorkflowHandlerForPath(project.getWorkflowPath());
      final WorklistList list =
          handler.findAssignedWorklists(project, userName, role, pfs,
              workflowService);

      return list;
    } catch (Exception e) {
      handleException(e, "trying to find assigned worklists");
    } finally {
      workflowService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @POST
  @Path("/checklist")
  @ApiOperation(value = "Find checklists", notes = "Finds checklists for query", response = ChecklistListJpa.class)
  public ChecklistList findChecklists(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Query", required = false) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /checklist/" + projectId + " " + query
            + " " + authToken);

    final String action = "trying to find checklists";
    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      // authorize and get user name from the token
      authorizeProject(workflowService, projectId, securityService, authToken,
          action, UserRole.AUTHOR);

      final Project project = workflowService.getProject(projectId);
      return workflowService.findChecklists(project, query, pfs);

    } catch (Exception e) {
      handleException(e, action);
      return null;
    } finally {
      workflowService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @POST
  @Path("/worklist")
  @ApiOperation(value = "Find worklists", notes = "Finds worklists for query", response = WorklistListJpa.class)
  public WorklistList findWorklists(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Query", required = false) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /worklist/" + projectId + " " + query
            + " " + authToken);

    final String action = "trying to find worklists";
    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      // authorize and get user name from the token
      authorizeProject(workflowService, projectId, securityService, authToken,
          action, UserRole.AUTHOR);

      return workflowService.findWorklists(
          workflowService.getProject(projectId), query, pfs);

    } catch (Exception e) {
      handleException(e, "trying to find worklists");
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
  @GET
  @Path("/worklist/action")
  @ApiOperation(value = "Perform workflow action on a tracking record", notes = "Performs the specified action as the specified refset as the specified user", response = WorklistJpa.class)
  public Worklist performWorkflowAction(
    @ApiParam(value = "Project id, e.g. 5", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Worklist id, e.g. 5", required = false) @QueryParam("worklistId") Long worklistId,
    @ApiParam(value = "User name, e.g. author1", required = true) @QueryParam("userName") String userName,
    @ApiParam(value = "User role, e.g. AUTHOR", required = true) @QueryParam("userRole") UserRole userRole,
    @ApiParam(value = "Workflow action, e.g. 'SAVE'", required = true) @QueryParam("action") WorkflowAction action,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /action " + action + ", " + projectId
            + ", " + worklistId + ", " + userRole + ", " + userName);

    // Test preconditions
    if (projectId == null || userName == null) {
      handleException(new Exception("Required parameter has a null value"), "");
    }

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      final String authName =
          authorizeProject(workflowService, projectId, securityService,
              authToken, "perform workflow action", UserRole.AUTHOR);
      workflowService.setLastModifiedBy(authName);

      final Worklist worklist = workflowService.getWorklist(worklistId);
      final Project project = workflowService.getProject(projectId);
      final Worklist returnWorklist =
          workflowService.performWorkflowAction(project, worklist, userName,
              userRole, action);

      return returnWorklist;
    } catch (Exception e) {
      handleException(e, "trying to perform workflow action");
    } finally {
      workflowService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @POST
  @Path("/worklist/available")
  @ApiOperation(value = "Find available  worklists", notes = "Finds worklists available for work", response = WorklistListJpa.class)
  public WorklistList findAvailableWorklists(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "UserRole", required = false) @QueryParam("role") UserRole role,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /worklist/available ");

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeProject(workflowService, projectId, securityService, authToken,
          "trying to find available worklists", UserRole.AUTHOR);

      final Project project = workflowService.getProject(projectId);

      final WorkflowActionHandler handler =
          workflowService.getWorkflowHandlerForPath(project.getWorkflowPath());
      final WorklistList list =
          handler.findAvailableWorklists(project, role, pfs, workflowService);

      return list;
    } catch (Exception e) {
      handleException(e, "trying to find available worklists");
    } finally {
      workflowService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @POST
  @Path("/checklist/add")
  @ApiOperation(value = "Create checklist", notes = "Create checklist", response = ChecklistJpa.class)
  public Checklist createChecklist(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Workflow bin id, e.g. 5", required = false) @QueryParam("workflowBinId") Long workflowBinId,
    @ApiParam(value = "Checklist name", required = false) @QueryParam("name") String name,
    @ApiParam(value = "Randomize, e.g. false", required = true) @QueryParam("randomize") Boolean randomize,
    @ApiParam(value = "Exclude on worklist, e.g. false", required = true) @QueryParam("excludeOnWorklist") Boolean excludeOnWorklist,
    @ApiParam(value = "Query", required = false) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /checklist ");

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      final String userName =
          authorizeProject(workflowService, projectId, securityService,
              authToken, "trying to create checklist", UserRole.AUTHOR);
      workflowService.setLastModifiedBy(userName);

      final Project project = workflowService.getProject(projectId);
      final WorkflowBin workflowBin =
          workflowService.getWorkflowBin(workflowBinId);
      final StringBuffer sb = new StringBuffer();
      sb.append("workflowBinName:").append(workflowBin.getName());
      if (excludeOnWorklist) {
        sb.append(" AND ").append("NOT worklistName:[* TO *] ");
      }
      if (query != null && !query.equals("")) {
        sb.append(" AND ").append(query);
      }

      if (randomize) {
        pfs.setSortField("RANDOM");
      } else {
        pfs.setSortField("clusterId");
      }

      final TrackingRecordList recordResultList =
          workflowService.findTrackingRecords(project, sb.toString(), pfs);

      final ChecklistJpa checklist = new ChecklistJpa();
      checklist.setName(name);
      checklist.setDescription(name + " description");
      checklist.setProject(project);
      checklist.setTimestamp(new Date());

      final Checklist addedChecklist = workflowService.addChecklist(checklist);
      for (final TrackingRecord record : recordResultList.getObjects()) {
        final TrackingRecord checklistRecord = new TrackingRecordJpa(record);
        checklistRecord.setId(null);
        workflowService.addTrackingRecord(checklistRecord);
        addedChecklist.getTrackingRecords().add(checklistRecord);
        workflowService.updateChecklist(addedChecklist);
      }

      return addedChecklist;
    } catch (Exception e) {
      handleException(e, "trying to create checklist");
    } finally {
      workflowService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @POST
  @Path("/worklist/add")
  @ApiOperation(value = "Create worklist", notes = "Create worklist", response = WorklistJpa.class)
  public Worklist createWorklist(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Workflow bin id, e.g. 5", required = false) @QueryParam("workflowBinId") Long workflowBinId,
    @ApiParam(value = "Cluster type", required = false) @QueryParam("clusterType") String clusterType,
    @ApiParam(value = "Skip this number of clusters, e.g. 3", required = true) @QueryParam("skipClusterCt") int skipClusterCt,
    @ApiParam(value = "Max number of clusters in worklist, e.g. 30", required = true) @QueryParam("clusterCt") int clusterCt,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /worklist ");

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      final String userName =
          authorizeProject(workflowService, projectId, securityService,
              authToken, "trying to create worklist", UserRole.AUTHOR);
      workflowService.setLastModifiedBy(userName);

      final Project project = workflowService.getProject(projectId);
      final WorkflowBin workflowBin =
          workflowService.getWorkflowBin(workflowBinId);
      final WorkflowEpoch currentEpoch =
          workflowService.getCurrentWorkflowEpoch(project);

      // Compose the worklist name from the current epoch, the bin name,
      // and the max worklist id+1. (e.g. wrk16a_demotions_chem_001)
      final StringBuffer worklistName = new StringBuffer();
      worklistName.append("wrk").append(currentEpoch.getName()).append("_");
      worklistName.append(workflowBin.getName()).append("_");
      if (clusterType.equals("chem"))
        worklistName.append("chem").append("_");

      // findWorklists with matching project id, epoch name and bin name and
      // cluster type
      final PfsParameter worklistQueryPfs = new PfsParameterJpa();
      worklistQueryPfs.setStartIndex(0);
      worklistQueryPfs.setMaxResults(1);
      worklistQueryPfs.setSortField("name");
      worklistQueryPfs.setAscending(false);

      final StringBuffer query = new StringBuffer();
      query
          .append("name:")
          .append("wrk")
          .append(
              currentEpoch.getName() + "_" + workflowBin.getName() + "_"
                  + clusterType + '*');
      final WorklistList worklistList =
          workflowService.findWorklists(project, query.toString(),
              worklistQueryPfs);
      int nextNumber =
          worklistList.getObjects().size() == 0 ? 1 : worklistList.getObjects()
              .get(0).getNumber() + 1;
      worklistName.append(new String(Integer.toString(nextNumber + 1000))
          .substring(1));

      // build query to retrieve tracking records that will be in worklist
      final StringBuffer sb = new StringBuffer();
      sb.append("workflowBinName:").append(workflowBin.getName());
      sb.append(" AND ").append("NOT worklistName:[* TO *] ");
      sb.append(" AND ").append("clusterType:").append(clusterType);
      if (pfs.getQueryRestriction() != null
          && !pfs.getQueryRestriction().equals("")) {
        sb.append(" AND ").append(pfs.getQueryRestriction());
      }

      pfs.setSortField("clusterId");
      pfs.setStartIndex(skipClusterCt);
      pfs.setMaxResults(clusterCt);

      final TrackingRecordList recordResultList =
          workflowService.findTrackingRecords(project, sb.toString(), pfs);

      final WorklistJpa worklist = new WorklistJpa();
      worklist.setName(worklistName.toString());
      worklist.setDescription(worklistName.toString() + " description");
      worklist.setProject(project);
      worklist.setWorkflowStatus(WorkflowStatus.NEW);
      worklist.setNumber(nextNumber);
      worklist.setProjectId(project.getId());
      worklist.setTimestamp(new Date());
      worklist.setWorkflowBinName(workflowBin.getName());

      final Worklist addedWorklist = workflowService.addWorklist(worklist);

      for (final TrackingRecord record : recordResultList.getObjects()) {
        final TrackingRecord worklistRecord = new TrackingRecordJpa(record);
        worklistRecord.setId(null);
        worklistRecord.setWorklistName(worklistName.toString());
        workflowService.addTrackingRecord(worklistRecord);
        addedWorklist.getTrackingRecords().add(worklistRecord);
        workflowService.updateWorklist(addedWorklist);
      }

      return addedWorklist;
    } catch (Exception e) {
      handleException(e, "trying to create worklist");
    } finally {
      workflowService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @GET
  @Path("/bin/all")
  @ApiOperation(value = "Get workflow bins", notes = "Gets the workflow bins for the project and type.", response = WorkflowBinJpa.class, responseContainer = "List")
  public List<WorkflowBin> getWorkflowBins(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Workflow bin type, e.g. MUTUALLY_EXCLUSIVE", required = false) @QueryParam("type") WorkflowBinType type,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful POST call (Workflow): /bin/all ");
    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeProject(workflowService, projectId, securityService, authToken,
          "trying to get workflow bin stats", UserRole.AUTHOR);
      final Project project = workflowService.getProject(projectId);
      final List<WorkflowBin> bins =
          workflowService.getWorkflowBins(project, type);

      // Track "editable" and "uneditable"
      final Map<String, Integer> typeUneditableMap = new HashMap<>();
      final Map<String, Integer> typeEditableMap = new HashMap<>();
      for (final WorkflowBin bin : bins) {
        for (final TrackingRecord record : bin.getTrackingRecords()) {
          final String clusterType = record.getClusterType();
          // Initialize map
          if (!typeUneditableMap.containsKey(clusterType)) {
            typeUneditableMap.put(clusterType, 0);
            typeEditableMap.put(clusterType, 0);
          }
          // Increment uneditable
          if (ConfigUtility.isEmpty(record.getWorklistName())) {
            typeUneditableMap.put(clusterType,
                typeUneditableMap.get(clusterType) + 1);
          }
          // Otherwise increment editable
          else {
            typeEditableMap.put(clusterType,
                typeEditableMap.get(clusterType) + 1);
          }
        }
        // Now extract cluster types and add statistics
        for (final String clusterType : typeUneditableMap.keySet()) {
          // Add statistics
          ClusterTypeStats stats = new ClusterTypeStatsJpa();
          stats.setClusterType(clusterType);
          int editable = typeEditableMap.get(clusterType);
          int uneditable = typeUneditableMap.get(clusterType);
          stats.getStats().put("all", editable + uneditable);
          stats.getStats().put("editable", editable);
          stats.getStats().put("uneditable", uneditable);
          bin.getStats().add(stats);
        }
      }

      return bins;

    } catch (Exception e) {
      handleException(e, "trying to get workflow bin stats");
      return null;
    } finally {
      workflowService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/worklist/{id}")
  @ApiOperation(value = "Get the statistics for the worklist", notes = "Gets the statistics for the worklist.", response = WorklistJpa.class)
  public Worklist getWorklist(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Worklist id, e.g. 5", required = false) @PathParam("id") Long worklistId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /worklist/" + worklistId);
    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      final String userName =
          authorizeProject(workflowService, projectId, securityService,
              authToken, "trying to get worklist stats", UserRole.AUTHOR);

      workflowService.setLastModifiedBy(userName);

      final Worklist worklist = workflowService.getWorklist(worklistId);

      // TODO to be done later
      // compute the stats and add them to the stats object
      // n_actions -1 - molecular action search by concept ids on worklist
      // n_approved -1 - "APPROVE_CONCEPT" molecular actions
      // n_approved_by_editor -1 - "APPROVE_CONCEPT" molecular actions with
      // editors initial
      // n_stamped -1 - "APPROVE_CONCEPT" molecular actions with editors
      // stampinginitial
      // n_not_stamped -1 - concepts without APPROVE_CONCEPT actions
      // n_rels_inserted -1 - "ADD_RELATIONSHIP" molecular actions
      // n_stys_inserted -1 - "ADD_SEMANTIC_TYPE" molecular actions
      // n_splits -1 - "SPLIT" molecular actions
      // n_merges -1 - "MERGE" molecular actions

      // return the worklist
      return worklist;

    } catch (Exception e) {
      handleException(e, "trying to get worklist stats");
      return null;
    } finally {
      workflowService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/bin/{id}/clear")
  @ApiOperation(value = "Clear bin", notes = "Clear bin")
  public void clearBin(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Workflow bin id, e.g. 1", required = true) @PathParam("id") Long workflowBinId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /bin/" + workflowBinId + "/clear ");

    final WorkflowServiceJpa workflowService = new WorkflowServiceJpa();
    try {
      final String userName =
          authorizeProject(workflowService, projectId, securityService,
              authToken, "trying to clear bin", UserRole.AUTHOR);
      workflowService.setLastModifiedBy(userName);

      workflowService.getWorkflowBin(workflowBinId);

      // remove bins and all of the tracking records in the bin
      workflowService.removeWorkflowBin(workflowBinId, true);

    } catch (Exception e) {
      handleException(e, "trying to clear bin");
    } finally {
      workflowService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/bin/{id}/regenerate")
  @ApiOperation(value = "Regenerate bin", notes = "Regenerate bin", response = WorkflowBinJpa.class)
  public WorkflowBin regenerateBin(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Workflow bin id, e.g. 5", required = true) @PathParam("id") Long workflowBinId,
    @ApiParam(value = "Workflow bin type", required = true) WorkflowBinType type,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /bin/" + workflowBinId + "/regenerate ");
    final WorkflowServiceJpa workflowService = new WorkflowServiceJpa();
    try {
      final String userName =
          authorizeProject(workflowService, projectId, securityService,
              authToken, "trying to regenerate a single bin", UserRole.AUTHOR);
      workflowService.setLastModifiedBy(userName);

      // Set transaction scope
      workflowService.setTransactionPerOperation(false);
      workflowService.beginTransaction();

      // Read relevant workflow objects
      final Project project = workflowService.getProject(projectId);
      final WorkflowBin bin = workflowService.getWorkflowBin(workflowBinId);

      // Remove the workflow bin
      workflowService.removeWorkflowBin(workflowBinId, true);

      // Get the bin definitions
      final List<WorkflowBinDefinition> definitions =
          workflowService.getWorkflowBinDefinitions(project, type);
      WorkflowBin newBin = null;
      for (final WorkflowBinDefinition definition : definitions) {
        if (definition.getName().equals(bin.getName())) {
          newBin =
              this.regenerateBinHelper(project, definition, bin.getRank(),
                  new HashSet<>(),
                  getConceptIdWorklistNameMap(project, workflowService),
                  workflowService);
          break;
        }
      }

      workflowService.commit();
      return newBin;

    } catch (Exception e) {
      handleException(e, "trying to regenerate a single bin");
    } finally {
      workflowService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @GET
  @Path("/worklist/{id}/report/generate")
  @ApiOperation(value = "Generate concept reports for worklist", notes = "Generate concept reports for the specified worklist", response = String.class)
  public String generateConceptReport(
    @ApiParam(value = "Project id, e.g. 5") @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Worklist id, e.g. 5") @PathParam("id") Long worklistId,
    @ApiParam(value = "Delay", required = false) @QueryParam("delay") Long delay,
    @ApiParam(value = "Send email, e.g. false", required = false) @QueryParam("sendEmail") Boolean sendEmail,
    @ApiParam(value = "Concept report type", required = true) @QueryParam("conceptReportType") String conceptReportType,
    @ApiParam(value = "Relationship count", required = false) @QueryParam("relationshipCt") Integer relationshipCt,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /report/" + worklistId
            + "/report/generate ");
    // TODO: parameters not used
    // TODO: how to determine fileName?

    final WorkflowServiceJpa workflowService = new WorkflowServiceJpa();
    final ReportServiceJpa reportService = new ReportServiceJpa();
    StringBuffer conceptReport = new StringBuffer();
    try {
      final String userName =
          authorizeProject(workflowService, projectId, securityService,
              authToken, "trying to generate concept report", UserRole.AUTHOR);
      workflowService.setLastModifiedBy(userName);

      // Read vars
      final Project project = workflowService.getProject(projectId);
      final Worklist worklist = workflowService.getWorklist(worklistId);
      final TrackingRecordList recordList =
          workflowService.findTrackingRecords(project, "worklistName:"
              + worklist.getName(), new PfsParameterJpa());

      for (final TrackingRecord record : recordList.getObjects()) {
        for (final Long conceptId : record.getOrigConceptIds()) {
          final Concept concept = reportService.getConcept(conceptId);
          // TODO: conceptReportType and relationshipCt will become
          // parameters to getConceptReport
          conceptReport
              .append(reportService.getConceptReport(project, concept));
          conceptReport.append("---------------------------------------------");
        }
      }

      // Construct filename
      String fileName = worklistId + "_rpt.txt";
      final String uploadDir = ConfigUtility.getUploadDir();
      File reportsDir = new File(uploadDir + "/" + projectId + "/reports");
      File file = new File(reportsDir, fileName);
      if (file.exists()) {
        throw new Exception("Worklist report file already exists - "
            + file.getAbsolutePath());
      }

      // Make dirs
      if (!reportsDir.exists()) {
        reportsDir.mkdirs();
      }

      // Handle delay
      if (delay != null) {
        Thread.sleep(delay);
      }

      final BufferedWriter out = new BufferedWriter(new FileWriter(file));
      out.write(conceptReport.toString());
      out.close();

      // If sendEmail, handle sending email - to the email for the user who
      // requested the build
      if (sendEmail) {
        final User user = securityService.getUser(userName);
        final Properties config = ConfigUtility.getConfigProperties();
        ConfigUtility.sendEmail("[Terminology Server] Worklist Concept Report "
            + fileName, config.getProperty("mail.smtp.user"), user.getEmail(),
            "The worklist concept report " + fileName + " has been successfully generated.", config,
            "true".equals(config.get("mail.smtp.auth")));
      }

      return fileName;
    } catch (Exception e) {
      handleException(e, "trying to generate concept report");
    } finally {
      reportService.close();
      workflowService.close();
      securityService.close();
    }
    return "";
  }

  /* see superclass */
  @Override
  @POST
  @Path("/report")
  @ApiOperation(value = "Find concept reports", notes = "Find generated concept reports", response = StringList.class)
  public StringList findGeneratedConceptReports(
    @ApiParam(value = "Project id, e.g. 5") @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Query") @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /report " + projectId + ", " + query);

    // TODO; deal with pfs
    final WorkflowServiceJpa workflowService = new WorkflowServiceJpa();
    StringList stringList = new StringList();
    List<String> matchingFiles = new ArrayList<>();
    try {
      final String uploadDir = ConfigUtility.getUploadDir();
      String filePath = uploadDir + "/" + projectId + "/report/";
      File dir = new File(filePath);
      if (!dir.exists()) {
        throw new Exception("No reports exist for path " + filePath);
      }
      for (String file : dir.list()) {
        if (file.contains(query)) {
          matchingFiles.add(file);
        }
      }
      Collections.sort(matchingFiles);
      stringList.setObjects(matchingFiles);
      stringList.setTotalCount(matchingFiles.size());
      return stringList;

    } catch (Exception e) {
      handleException(e, e.getMessage()
          + ". Trying to find generated concept reports.");
    } finally {

      workflowService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @GET
  @Path("/report/{fileName}")
  @ApiOperation(value = "Get generated concept report", notes = "Get generated concept report", response = String.class)
  public String getGeneratedConceptReport(
    @ApiParam(value = "Project id, e.g. 5") @QueryParam("projectId") Long projectId,
    @ApiParam(value = "File name") @PathParam("fileName") String fileName,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /report/" + fileName);

    final WorkflowServiceJpa workflowService = new WorkflowServiceJpa();
    BufferedReader reader = null;

    try {
      final String uploadDir = ConfigUtility.getUploadDir();
      String filePath = uploadDir + "/" + projectId + "/report/" + fileName;
      File file = new File(filePath);
      if (!file.exists()) {
        throw new Exception("No report exists for path " + filePath);
      }
      reader = new BufferedReader(new FileReader(file));
      String line = null;
      StringBuilder stringBuilder = new StringBuilder();
      String ls = System.getProperty("line.separator");

      while ((line = reader.readLine()) != null) {
        stringBuilder.append(line);
        stringBuilder.append(ls);
      }

      return stringBuilder.toString();

    } catch (Exception e) {
      handleException(e, e.getMessage()
          + ". Trying to find generated concept report.");
    } finally {
      reader.close();
      workflowService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @DELETE
  @Path("/report/{filename}/remove")
  @ApiOperation(value = "Get generated concept report", notes = "Get generated concept report", response = String.class)
  public void removeGeneratedConceptReport(
    @ApiParam(value = "Project id, e.g. 5") @QueryParam("projectId") Long projectId,
    @ApiParam(value = "File name") @PathParam("fileName") String fileName,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /report/" + fileName + "/remove");
    final WorkflowServiceJpa workflowService = new WorkflowServiceJpa();

    try {
      final String uploadDir = ConfigUtility.getUploadDir();
      String filePath = uploadDir + "/" + projectId + "/report/" + fileName;
      File file = new File(filePath);
      if (!file.exists()) {
        throw new Exception("No report exists for path " + filePath);
      }
      file.delete();
    } catch (Exception e) {
      handleException(e, e.getMessage()
          + ". Trying to remove generated concept report.");
    } finally {
      workflowService.close();
      securityService.close();
    }
  }

  /**
   * Execute query.
   *
   * @param query the query
   * @param nativeFlag the native flag
   * @param workflowService the workflow service
   * @return the list
   * @throws Exception the exception
   */
  @SuppressWarnings({
      "unchecked", "static-method"
  })
  private List<Object[]> executeQuery(String query, boolean nativeFlag,
    WorkflowServiceJpa workflowService) throws Exception {

    // check for sql query errors -- throw as local exception
    // this is used to propagate errors back to user when testing queries

    // ensure that query begins with SELECT (i.e. prevent injection
    // problems)
    if (!query.toUpperCase().startsWith("SELECT")) {
      throw new LocalException(
          "SQL Query has bad format:  does not begin with SELECT");
    }

    // check for multiple commands (i.e. multiple semi-colons)
    if (query.indexOf(";") != query.length() - 1 && query.endsWith(";")) {
      throw new LocalException(
          "SQL Query has bad format:  multiple commands detected");
    }

    // crude check: check for data manipulation commands
    if (query.toUpperCase().matches(
        "ALTER |CREATE |DROP |DELETE |INSERT |TRUNCATE |UPDATE ")) {
      throw new LocalException(
          "SQL Query has bad format:  data manipulation request detected");
    }

    // check for proper format for insertion into reports

    if (query.toUpperCase().indexOf("FROM") == -1)
      throw new LocalException(
          "Workflow bin definition query must contain the term FROM");

    String selectSubStr =
        query.substring(0, query.toUpperCase().indexOf("FROM"));

    if (!selectSubStr.contains("clusterId"))
      throw new LocalException(
          "Workflow bin definition query must return column result with name of 'clusterId'");

    if (!selectSubStr.contains("conceptId"))
      throw new LocalException(
          "Workflow bin definition query must return column result with name of 'conceptId'");

    javax.persistence.Query jpaQuery = null;
    if (nativeFlag) {
      jpaQuery = workflowService.getEntityManager().createNativeQuery(query);
    } else {
      jpaQuery = workflowService.getEntityManager().createQuery(query);
    }
    return jpaQuery.getResultList();
  }

  /**
   * Returns the concept id worklist name map for all concept ids that have
   * tracking records out on worklists.
   *
   * @param project the project
   * @param workflowService the workflow service
   * @return the concept id worklist name map
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  private Map<Long, String> getConceptIdWorklistNameMap(Project project,
    WorkflowService workflowService) throws Exception {
    // find active worklists
    final Set<Worklist> worklists = new HashSet<>();
    final StringBuilder sb = new StringBuilder();
    sb.append("NOT type:READY_FOR_PUBLICATION");
    worklists.addAll(workflowService
        .findWorklists(project, sb.toString(), null).getObjects());

    // get tracking records that are on active worklists
    final StringBuilder sb2 = new StringBuilder();
    sb2.append("worklist:[* TO *]");
    final TrackingRecordList recordList =
        workflowService.findTrackingRecords(project, sb2.toString(), null);

    // make map of conceptId -> active worklist name
    // calculate which concepts are already out on worklists
    final Map<Long, String> conceptIdWorklistNameMap = new HashMap<>();
    for (final TrackingRecord trackingRecord : recordList.getObjects()) {
      for (Long conceptId : trackingRecord.getOrigConceptIds()) {
        conceptIdWorklistNameMap.put(conceptId,
            trackingRecord.getWorklistName());
      }
    }
    return conceptIdWorklistNameMap;
  }

  /**
   * Regenerate bin helper. From the set of parameters it creates and populates
   * a single workflow bin. For complete regeneration of bins this can be
   * repeatedly used.
   *
   * @param project the project
   * @param definition the definition
   * @param rank the rank
   * @param conceptsSeen the concepts seen
   * @param conceptIdWorklistNameMap the concept id worklist name map
   * @param workflowService the workflow service
   * @return the workflow bin
   * @throws Exception the exception
   */
  private WorkflowBin regenerateBinHelper(Project project,
    WorkflowBinDefinition definition, int rank, Set<Long> conceptsSeen,
    Map<Long, String> conceptIdWorklistNameMap,
    WorkflowServiceJpa workflowService) throws Exception {

    // Create the workflow bin
    final WorkflowBin bin = new WorkflowBinJpa();
    bin.setCreationTime(new Date().getTime());
    bin.setName(definition.getName());
    bin.setDescription(definition.getDescription());
    bin.setEditable(definition.isEditable());
    bin.setProject(project);
    bin.setRank(rank);
    bin.setTerminology(project.getTerminology());
    bin.setVersion("latest");
    bin.setTerminologyId("");
    bin.setTimestamp(new Date());
    bin.setType(definition.getWorkflowConfig().getType());
    workflowService.addWorkflowBin(bin);

    // execute the query
    final String query = definition.getQuery();
    List<Object[]> results = null;
    switch (definition.getQueryType()) {
      case HQL:
        try {
          results = executeQuery(query, false, workflowService);
        } catch (java.lang.IllegalArgumentException e) {
          throw new LocalException("Error executing HQL query: "
              + e.getMessage());
        }
        break;
      case LUCENE:
        SearchResultList resultList =
            workflowService.findConcepts(project.getTerminology(),
                null, null, query, null);
        results = new ArrayList<>();
        for (SearchResult result : resultList.getObjects()) {
          Object[] objectArray = new Object[1];
          objectArray[0] = result.getId();
          objectArray[1] = result.getValue();
          results.add(objectArray);
        }
        break;
      case SQL:
        try {
          results = executeQuery(query, true, workflowService);
        } catch (javax.persistence.PersistenceException e) {
          throw new LocalException("Error executing SQL query:  "
              + e.getMessage());
        } catch (java.lang.IllegalArgumentException e) {
          throw new LocalException(
              "Error executing SQL query, possible invalid parameters (valid parameters are :MAP_PROJECT_ID:, :TIMESTAMP:):  "
                  + e.getMessage());
        }
        break;
      default:
        break;

    } // end execute query for each definition

    if (results == null)
      throw new Exception("Failed to retrieve results for query");

    final Map<Long, Set<Long>> clusterIdConceptIdsMap = new HashMap<>();

    // put query results into map
    for (final Object[] result : results) {
      Long clusterId = new Long(result[0].toString());
      Long componentId = new Long(result[1].toString());

      // skip result entry where the conceptId is already in conceptsSeen
      // and workflow config is mutually exclusive
      if (!conceptsSeen.contains(componentId)
          || !definition.getWorkflowConfig().isMutuallyExclusive()) {
        if (clusterIdConceptIdsMap.containsKey(clusterId)) {
          Set<Long> componentIds = clusterIdConceptIdsMap.get(clusterId);
          componentIds.add(componentId);
          clusterIdConceptIdsMap.put(clusterId, componentIds);
        } else {
          Set<Long> componentIds = new HashSet<>();
          componentIds.add(componentId);
          clusterIdConceptIdsMap.put(clusterId, componentIds);
        }
      }
      if (definition.getWorkflowConfig().isMutuallyExclusive()) {
        conceptsSeen.add(componentId);
      }
    }

    // for each cluster in clusterIdComponentIdsMap create a tracking record
    Long clusterIdCt = 1L;
    for (Long clusterId : clusterIdConceptIdsMap.keySet()) {
      // TODO: handle definition is not editable
      if (definition.isEditable()) {
        TrackingRecord record = new TrackingRecordJpa();
        record.setClusterId(clusterIdCt++);
        record.setTerminology(project.getTerminology());
        record.setTimestamp(new Date());
        record.setVersion("latest");
        record.setWorkflowBinName(bin.getName());
        record.setProject(project);

        record.setWorklistName(null);
        record.setClusterType("");

        for (Long conceptId : clusterIdConceptIdsMap.get(clusterId)) {
          Concept concept = workflowService.getConcept(conceptId);
          record.getOrigConceptIds().add(conceptId);
          if (record.getClusterType().equals("")) {
            for (SemanticTypeComponent sty : concept.getSemanticTypes()) {
              if (project.getSemanticTypeCategoryMap().containsKey(
                  sty.getSemanticType())) {
                record.setClusterType(project.getSemanticTypeCategoryMap().get(
                    sty.getSemanticType()));
                break;
              }
            }
          }
          for (Atom atom : concept.getAtoms()) {
            record.getComponentIds().add(atom.getId());
          }
          if (record.getWorklistName() == null) {
            if (conceptIdWorklistNameMap.containsKey(conceptId)) {
              record.setWorklistName(conceptIdWorklistNameMap.get(conceptId));
              break;
            }
          }
        }
        workflowService.addTrackingRecord(record);
        bin.getTrackingRecords().add(record);
      }
    }
    workflowService.updateWorkflowBin(bin);

    return bin;

  }

}
