/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.rest.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.wci.umls.server.Project;
import com.wci.umls.server.User;
import com.wci.umls.server.UserRole;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.ChecklistList;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.helpers.Note;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.QueryType;
import com.wci.umls.server.helpers.SearchResult;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.helpers.StringList;
import com.wci.umls.server.helpers.TrackingRecordList;
import com.wci.umls.server.helpers.WorklistList;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.helpers.ChecklistListJpa;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.helpers.TrackingRecordListJpa;
import com.wci.umls.server.jpa.helpers.WorklistListJpa;
import com.wci.umls.server.jpa.services.ReportServiceJpa;
import com.wci.umls.server.jpa.services.SecurityServiceJpa;
import com.wci.umls.server.jpa.services.WorkflowServiceJpa;
import com.wci.umls.server.jpa.services.rest.WorkflowServiceRest;
import com.wci.umls.server.jpa.worfklow.ChecklistJpa;
import com.wci.umls.server.jpa.worfklow.ChecklistNoteJpa;
import com.wci.umls.server.jpa.worfklow.ClusterTypeStatsJpa;
import com.wci.umls.server.jpa.worfklow.TrackingRecordJpa;
import com.wci.umls.server.jpa.worfklow.WorkflowBinDefinitionJpa;
import com.wci.umls.server.jpa.worfklow.WorkflowBinJpa;
import com.wci.umls.server.jpa.worfklow.WorkflowConfigJpa;
import com.wci.umls.server.jpa.worfklow.WorkflowEpochJpa;
import com.wci.umls.server.jpa.worfklow.WorklistJpa;
import com.wci.umls.server.jpa.worfklow.WorklistNoteJpa;
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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * REST implementation for {@link WorkflowServiceRest}.
 */
@Path("/workflow")
@Api(value = "/workflow", description = "Operations supporting workflow")
@Consumes({
    MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
})
@Produces({
    MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
})
public class WorkflowServiceRestImpl extends RootServiceRestImpl
    implements WorkflowServiceRest {

  /** The lock. */
  private static String lock = "LOCK";

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
    Logger.getLogger(getClass())
        .info("RESTful POST call (Workflow): /config/add/" + projectId + " "
            + workflowConfig.toString() + " " + authToken);

    final String action = "trying to add workflow config";
    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      // authorize and get user name from the token
      final String userName = authorizeProject(workflowService, projectId,
          securityService, authToken, action, UserRole.AUTHOR);
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
    Logger.getLogger(getClass())
        .info("RESTful POST call (Workflow): /config/update/" + projectId + " "
            + config.getId() + " " + authToken);

    final String action = "trying to update workflow config";
    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      // authorize and get user name from the token
      final String userName = authorizeProject(workflowService, projectId,
          securityService, authToken, action, UserRole.AUTHOR);
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
  @POST
  @Path("/worklist/update")
  @ApiOperation(value = "Update a worklist", notes = "Update a worklist")
  public void updateWorklist(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Worklist to update", required = true) WorklistJpa worklist,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /worklist/update/" + projectId + " "
            + worklist.getId() + " " + authToken);

    final String action = "trying to update a worklist";
    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      // authorize and get user name from the token
      final String userName =
          authorizeProject(workflowService, projectId, securityService,
              authToken, action, UserRole.AUTHOR);
      workflowService.setLastModifiedBy(userName);
      
      // reconnect tracking records before saving worklist
      // (parameter worklist will have no records on it)
      Worklist origWorklist = workflowService.getWorklist(worklist.getId());
      worklist.setTrackingRecords(origWorklist.getTrackingRecords());
      
      workflowService.updateWorklist(worklist);

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
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Workflow config id, e.g. 1", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call (Workflow): /config/remove " + id + " " + projectId);

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      // authorize and get user name from the token
      final String userName =
          authorizeProject(workflowService, projectId, securityService,
              authToken, "remove workflow config", UserRole.AUTHOR);
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
  @GET
  @Path("/config/{id}")
  @ApiOperation(value = "Get workflow config", notes = "Gets a workflow config", response = WorkflowConfigJpa.class)
  public WorkflowConfig getWorkflowConfig(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Workflow config id, e.g. 1", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Workflow): /config/" + id + "  " + projectId);

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeProject(workflowService, projectId, securityService, authToken,
          "remove workflow config", UserRole.AUTHOR);

      final WorkflowConfig config = workflowService.getWorkflowConfig(id);
      if (config != null) {
        workflowService.handleLazyInit(config);
      }
      return config;

    } catch (Exception e) {
      handleException(e, "trying to get a workflow config");
    } finally {
      workflowService.close();
      securityService.close();
    }
    return null;

  }

  /* see superclass */
  @Override
  @GET
  @Path("/config/all")
  @ApiOperation(value = "Get workflow configs", notes = "Gets a workflow configs", response = WorkflowConfigJpa.class, responseContainer = "List")
  public List<WorkflowConfig> getWorkflowConfigs(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Workflow): /config/all" + "  " + projectId);

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeProject(workflowService, projectId, securityService, authToken,
          "remove workflow config", UserRole.AUTHOR);

      final Project project = workflowService.getProject(projectId);
      final List<WorkflowConfig> configs =
          workflowService.getWorkflowConfigs(project);
      for (WorkflowConfig config : configs) {
        workflowService.handleLazyInit(config);
      }
      return configs;

    } catch (Exception e) {
      handleException(e, "trying to get a workflow config");
    } finally {
      workflowService.close();
      securityService.close();
    }
    return null;

  }

  /* see superclass */
  @Override
  @DELETE
  @Path("/worklist/{id}/remove")
  @ApiOperation(value = "Remove a worklist", notes = "Remove a worklist")
  public void removeWorklist(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Worklist id, e.g. 1", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Workflow): /worklist/" + id + "/remove");

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      final String userName =
          authorizeProject(workflowService, projectId, securityService,
              authToken, "remove workflow config", UserRole.AUTHOR);
      workflowService.setLastModifiedBy(userName);
      // do all of this in one transaction
      workflowService.setTransactionPerOperation(false);
      workflowService.beginTransaction();

      final Project project = workflowService.getProject(projectId);
      final Worklist worklist = workflowService.getWorklist(id);

      // Find workflow bin name
      final List<WorkflowBin> list =
          workflowService.getWorkflowBins(project, null);
      for (final WorkflowBin bin : list) {
        if (bin.getName().equals(worklist.getWorkflowBinName())) {
          for (final TrackingRecord record : bin.getTrackingRecords()) {
            if (worklist.getName().equals(record.getWorklistName())) {
              record.setWorklistName(null);
              workflowService.updateTrackingRecord(record);
            }
          }
        }
      }

      workflowService.removeWorklist(id, true);
      workflowService.commit();
    } catch (Exception e) {
      handleException(e, "trying to remove a worklist");
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
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Checklist id, e.g. 1", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Workflow): /checklist/" + id + "/remove");

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      final String userName =
          authorizeProject(workflowService, projectId, securityService,
              authToken, "remove workflow config", UserRole.AUTHOR);
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
    @ApiParam(value = "Workflow bin definition to add", required = true) WorkflowBinDefinitionJpa binDefinition,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful POST call (Workflow): /definition/add/" + projectId + " "
            + binDefinition.getName() + " " + authToken);

    final String action = "trying to add workflow bin definition";
    final WorkflowService workflowService = new WorkflowServiceJpa();

    try {
      // authorize and get user name from the token
      final String userName = authorizeProject(workflowService, projectId,
          securityService, authToken, action, UserRole.AUTHOR);
      workflowService.setLastModifiedBy(userName);

      // Add definition
      final WorkflowBinDefinition def =
          workflowService.addWorkflowBinDefinition(binDefinition);

      // Add to list in workflow config and save
      final WorkflowConfig config = workflowService
          .getWorkflowConfig(binDefinition.getWorkflowConfig().getId());
      config.getWorkflowBinDefinitions().add(def);
      workflowService.updateWorkflowConfig(config);

      return def;
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
    Logger.getLogger(getClass())
        .info("RESTful POST call (Workflow): /epoch/add/" + projectId + " "
            + epoch.getName() + " " + authToken);

    final String action = "trying to add workflow bin definition";
    final WorkflowService workflowService = new WorkflowServiceJpa();

    try {
      // authorize and get user name from the token
      final String userName = authorizeProject(workflowService, projectId,
          securityService, authToken, action, UserRole.AUTHOR);
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
  @DELETE
  @Path("/epoch/{id}/remove")
  @ApiOperation(value = "Remove a workflow epoch", notes = "Remove a workflow epoch")
  public void removeWorkflowEpoch(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Workflow epoch id, e.g. 1", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Workflow): /epoch/remove " + id + " " + projectId);

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      // authorize and get user name from the token
      final String userName = authorizeProject(workflowService, projectId,
          securityService, authToken, "remove workflow epoch", UserRole.AUTHOR);
      workflowService.setLastModifiedBy(userName);

      workflowService.removeWorkflowEpoch(id);

    } catch (Exception e) {
      handleException(e, "trying to remove a workflow epoch");
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
    Logger.getLogger(getClass())
        .info("RESTful POST call (Workflow): /definition/update  " + projectId
            + " " + binDefinition.getId() + " " + authToken);

    final String action = "trying to update workflow bin definition";
    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      // authorize and get user name from the token
      String userName = authorizeProject(workflowService, projectId,
          securityService, authToken, action, UserRole.AUTHOR);
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
    @ApiParam(value = "Workflow bin definition id, e.g. 1", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Workflow): /definition/" + id + "/remove");

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      final String userName =
          authorizeProject(workflowService, projectId, securityService,
              authToken, "remove workflow bin definition", UserRole.AUTHOR);
      workflowService.setLastModifiedBy(userName);

      // load the bin definition, get its workflow config. Remove it from
      // workflow config, then remove it.
      WorkflowBinDefinition binDefinition =
          workflowService.getWorkflowBinDefinition(id);

      WorkflowConfig workflowConfig = binDefinition.getWorkflowConfig();
      workflowConfig.getWorkflowBinDefinitions().remove(binDefinition);
      workflowService.updateWorkflowConfig(workflowConfig);
      workflowService.removeWorkflowBinDefinition(id);

    } catch (Exception e) {
      handleException(e, "trying to remove a workflow bin definition");
    } finally {
      workflowService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @DELETE
  @Path("/bin/{id}/remove")
  @ApiOperation(value = "Remove a workflow bin ", notes = "Remove a workflow bin ")
  public void removeWorkflowBin(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Workflow bin id, e.g. 1", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Workflow): /bin/" + id + "/remove");

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      final String userName =
          authorizeProject(workflowService, projectId, securityService,
              authToken, "remove workflow bin definition", UserRole.AUTHOR);
      workflowService.setLastModifiedBy(userName);

      workflowService.removeWorkflowBin(id, true);

    } catch (Exception e) {
      handleException(e, "trying to remove a workflow bin");
    } finally {
      workflowService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @GET
  @Path("/definition/{id}")
  @ApiOperation(value = "Get workflow bin definition", notes = "Gets workflow bin definition")
  public WorkflowBinDefinition getWorkflowBinDefinition(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Workflow bin definition id, e.g. 1", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Workflow): /definition/" + id + " " + projectId);

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeProject(workflowService, projectId, securityService, authToken,
          "get workflow bin definition", UserRole.AUTHOR);

      final WorkflowBinDefinition definition =
          workflowService.getWorkflowBinDefinition(id);
      if (definition != null) {
        workflowService.handleLazyInit(definition);
      }
      return definition;

    } catch (Exception e) {
      handleException(e, "trying to get a workflow bin definition");
    } finally {
      workflowService.close();
      securityService.close();
    }
    return null;

  }

  /* see superclass */
  @Override
  @GET
  @Path("/definition")
  @ApiOperation(value = "Get workflow bin definition", notes = "Gets workflow bin definition by name")
  public WorkflowBinDefinition getWorkflowBinDefinition(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Workflow bin definition name, e.g. demotions", required = true) @QueryParam("name") String name,
    @ApiParam(value = "Workflow bin type", required = true) @QueryParam("type") WorkflowBinType type,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call (Workflow): /definition/" + name + " " + projectId);

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeProject(workflowService, projectId, securityService, authToken,
          "get workflow bin definition", UserRole.AUTHOR);
      final Project project = workflowService.getProject(projectId);
      final List<WorkflowBinDefinition> definitions =
          workflowService.getWorkflowBinDefinitions(project, type);
      for (WorkflowBinDefinition definition : definitions) {
        if (definition.getName().equals(name)) {
          workflowService.handleLazyInit(definition);
          return definition;
        }
      }
      return null;

    } catch (Exception e) {
      handleException(e, "trying to get a workflow bin definition");
    } finally {
      workflowService.close();
      securityService.close();
    }
    return null;

  }

  /* see superclass */
  @Override
  @GET
  @Path("/bin/clear/all")
  @ApiOperation(value = "Clear bins", notes = "Clear bins")
  public void clearBins(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Workflow bin type", required = true) WorkflowBinType type,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful POST call (Workflow): /bin/clear/all ");

    final WorkflowServiceJpa workflowService = new WorkflowServiceJpa();
    try {
      final String userName = authorizeProject(workflowService, projectId,
          securityService, authToken, "trying to clear bins", UserRole.AUTHOR);
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
  @GET
  @Path("/bin/regenerate/all")
  @ApiOperation(value = "Regenerate bins", notes = "Regenerate bins")
  public void regenerateBins(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Workflow bin type", required = true) @QueryParam("type") WorkflowBinType type,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful POST call (Workflow): /bin/regenerate/all ");

    // Only one user can regenerate bins at a time
    synchronized (lock) {
      final WorkflowServiceJpa workflowService = new WorkflowServiceJpa();
      try {
        final String userName =
            authorizeProject(workflowService, projectId, securityService,
                authToken, "trying to regenerate bins", UserRole.AUTHOR);
        workflowService.setLastModifiedBy(userName);

        // Set transaction mode
        workflowService.setTransactionPerOperation(false);
        workflowService.beginTransaction();

        // Load the project and workflow config
        Project project = workflowService.getProject(projectId);
        
        // Start by clearing the bins
        // remove bins and all of the tracking records in the bins
        final List<WorkflowBin> results =
            workflowService.getWorkflowBins(project, type);
        for (final WorkflowBin workflowBin : results) {
          workflowService.removeWorkflowBin(workflowBin.getId(), true);
        }
        
        workflowService.commit();
        workflowService.beginTransaction();
        
        // reread after the commit
        project = workflowService.getProject(projectId);
        
        final WorkflowConfig workflowConfig =
            workflowService.getWorkflowConfig(project, type);

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
    Logger.getLogger(getClass())
        .info("RESTful POST call (Workflow): /record/assigned ");

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeProject(workflowService, projectId, securityService, authToken,
          "trying to find assigned work", UserRole.AUTHOR);

      final Project project = workflowService.getProject(projectId);

      // find available tracking records
      final WorkflowActionHandler handler =
          workflowService.getWorkflowHandlerForPath(project.getWorkflowPath());
      final TrackingRecordList trackingRecords = handler
          .findAssignedWork(project, userName, role, pfs, workflowService);

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
    Logger.getLogger(getClass())
        .info("RESTful POST call (Workflow): /record/available ");

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
  @Path("/checklist/{id}/records")
  @ApiOperation(value = "Find tracking records for checklist", notes = "Finds tracking records for checklist", response = TrackingRecordListJpa.class)
  public TrackingRecordList findTrackingRecordsForChecklist(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Checklist id, e.g. 5", required = false) @PathParam("id") Long id,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful POST call (Workflow): /checklist/" + id + "/records");

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeProject(workflowService, projectId, securityService, authToken,
          "trying to find records for checklist", UserRole.AUTHOR);

      final Project project = workflowService.getProject(projectId);
      final Checklist checklist = workflowService.getChecklist(id);
      // Compose query of all of the tracking record ids
      final List<String> clauses = checklist.getTrackingRecords().stream()
          .map(r -> "id:" + r.getId()).collect(Collectors.toList());
      final String query = ConfigUtility.composeQuery("OR", clauses);
      if (query.isEmpty()) {
        return new TrackingRecordListJpa();
      }

      final TrackingRecordList list =
          workflowService.findTrackingRecords(project, query, pfs);
      for (final TrackingRecord record : list.getObjects()) {
        lookupTrackingRecordConcepts(record, workflowService);
      }

      return list;

    } catch (Exception e) {
      handleException(e, "trying to find records for checklist ");
    } finally {
      workflowService.close();
      securityService.close();
    }
    return null;
  }

  /**
   * Lookup tracking record concepts.
   *
   * @param record the record
   * @param service the service
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  private void lookupTrackingRecordConcepts(TrackingRecord record,
    WorkflowService service) throws Exception {

    // Bail if no atom components.
    if (record.getComponentIds().size() == 0) {
      return;
    }

    // Create a query
    final List<String> clauses = record.getComponentIds().stream()
        .map(l -> "atoms.id:" + l).collect(Collectors.toList());
    final String query = ConfigUtility.composeQuery("OR", clauses);

    // add concepts
    for (final SearchResult result : service
        .findConcepts(record.getTerminology(), null, Branch.ROOT, query, null)
        .getObjects()) {
      record.getConcepts().add(new ConceptJpa(result));
    }

  }

  /* see superclass */
  @Override
  @POST
  @Path("/worklist/{id}/records")
  @ApiOperation(value = "Find records for worklist", notes = "Finds tracking records for worklist", response = TrackingRecordListJpa.class)
  public TrackingRecordList findTrackingRecordsForWorklist(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Worklist id, e.g. 5", required = false) @PathParam("id") Long id,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful POST call (Workflow): /worklist/" + id + "/records");

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeProject(workflowService, projectId, securityService, authToken,
          "trying to find records for worklist", UserRole.AUTHOR);

      final Project project = workflowService.getProject(projectId);
      final Worklist worklist = workflowService.getWorklist(id);
      // Compose query of all of the tracking record ids
      final List<String> clauses = worklist.getTrackingRecords().stream()
          .map(r -> "id:" + r.getId()).collect(Collectors.toList());
      final String query = ConfigUtility.composeQuery("OR", clauses);

      if (query.isEmpty()) {
        return new TrackingRecordListJpa();
      }

      final TrackingRecordList list =
          workflowService.findTrackingRecords(project, query, pfs);
      for (final TrackingRecord record : list.getObjects()) {
        lookupTrackingRecordConcepts(record, workflowService);
      }

      return list;

    } catch (Exception e) {
      handleException(e, "trying to find records for worklist ");
    } finally {
      workflowService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @POST
  @Path("/bin/{id}/records")
  @ApiOperation(value = "Find records for workflow bin", notes = "Finds tracking records for workflow bin", response = TrackingRecordListJpa.class)
  public TrackingRecordList findTrackingRecordsForWorkflowBin(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "WorkflowBin id, e.g. 5", required = false) @PathParam("id") Long id,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful POST call (Workflow): /bin/" + id + "/records");

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeProject(workflowService, projectId, securityService, authToken,
          "trying to find records for workflow bin", UserRole.AUTHOR);

      final Project project = workflowService.getProject(projectId);
      final WorkflowBin bin = workflowService.getWorkflowBin(id);
      // Compose query of all of the tracking record ids
      final List<String> clauses = bin.getTrackingRecords().stream()
          .map(r -> "id:" + r.getId()).collect(Collectors.toList());
      final String query = ConfigUtility.composeQuery("OR", clauses);

      if (query.isEmpty()) {
        return new TrackingRecordListJpa();
      }

      final TrackingRecordList list =
          workflowService.findTrackingRecords(project, query, pfs);
      for (final TrackingRecord record : list.getObjects()) {
        lookupTrackingRecordConcepts(record, workflowService);
      }

      return list;

    } catch (Exception e) {
      handleException(e, "trying to find records for bin ");
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
    Logger.getLogger(getClass())
        .info("RESTful POST call (Workflow): /worklist/assigned ");

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      authorizeProject(workflowService, projectId, securityService, authToken,
          "trying to find assigned worklists", UserRole.AUTHOR);

      final Project project = workflowService.getProject(projectId);

      final WorkflowActionHandler handler =
          workflowService.getWorkflowHandlerForPath(project.getWorkflowPath());
      final WorklistList list = handler.findAssignedWorklists(project, userName,
          role, pfs, workflowService);

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

    Logger.getLogger(getClass())
        .info("RESTful POST call (Workflow): /checklist/" + projectId + " "
            + query + " " + authToken);

    final String action = "trying to find checklists";
    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      // authorize and get user name from the token
      authorizeProject(workflowService, projectId, securityService, authToken,
          action, UserRole.AUTHOR);

      final Project project = workflowService.getProject(projectId);
      ChecklistList list = workflowService.findChecklists(project, query, pfs);
      for (Checklist checklist : list.getObjects()) {
        workflowService.handleLazyInit(checklist);
      }

      // Compute "cluster" and "concept" counts
      for (final Checklist checklist : list.getObjects()) {
        checklist.getStats().put("clusterCt",
            checklist.getTrackingRecords().size());
        // Add up orig concepts size from all tracking records
        checklist.getStats().put("conceptCt",
            checklist.getTrackingRecords().stream().collect(
                Collectors.summingInt(w -> w.getOrigConceptIds().size())));
      }
      return list;
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

    Logger.getLogger(getClass()).info("RESTful POST call (Workflow): /worklist/"
        + projectId + " " + query + " " + authToken);

    final String action = "trying to find worklists";
    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      // authorize and get user name from the token
      authorizeProject(workflowService, projectId, securityService, authToken,
          action, UserRole.AUTHOR);

      // find worklists
      final WorklistList list = workflowService
          .findWorklists(workflowService.getProject(projectId), query, pfs);

      // Compute "cluster" and "concept" counts
      for (final Worklist worklist : list.getObjects()) {
        worklist.getStats().put("clusterCt",
            worklist.getTrackingRecords().size());
        // Add up orig concepts size from all tracking records
        worklist.getStats().put("conceptCt",
            worklist.getTrackingRecords().stream().collect(
                Collectors.summingInt(w -> w.getOrigConceptIds().size())));
      }

      return list;
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
  @ApiOperation(value = "Perform workflow action on a tracking record", notes = "Performs the specified action as the specified worklist as the specified user", response = WorklistJpa.class)
  public Worklist performWorkflowAction(
    @ApiParam(value = "Project id, e.g. 5", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Worklist id, e.g. 5", required = false) @QueryParam("worklistId") Long worklistId,
    @ApiParam(value = "User name, e.g. author1", required = true) @QueryParam("userName") String userName,
    @ApiParam(value = "User role, e.g. AUTHOR", required = true) @QueryParam("userRole") UserRole userRole,
    @ApiParam(value = "Workflow action, e.g. 'SAVE'", required = true) @QueryParam("action") WorkflowAction action,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful POST call (Workflow): /action "
        + projectId + ", " + worklistId + ", " + userName);

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
      // UserRole role = UserRole.valueOf(userRole);
      final Worklist returnWorklist = workflowService
          .performWorkflowAction(project, worklist, userName, userRole, action);

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
    Logger.getLogger(getClass())
        .info("RESTful POST call (Workflow): /worklist/available ");

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
    @ApiParam(value = "Cluster type", required = false) @QueryParam("clusterType") String clusterType,
    @ApiParam(value = "Checklist name", required = false) @QueryParam("name") String name,
    @ApiParam(value = "Checklist description", required = false) @QueryParam("description") String description,
    @ApiParam(value = "Randomize, e.g. false", required = true) @QueryParam("randomize") Boolean randomize,
    @ApiParam(value = "Exclude on worklist, e.g. false", required = true) @QueryParam("excludeOnWorklist") Boolean excludeOnWorklist,
    @ApiParam(value = "Query", required = false) @QueryParam("query") String query,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful POST call (Workflow): /checklist/add " + projectId + ", "
            + workflowBinId + ", " + clusterType + ", " + name + ", "
            + randomize);

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      final String userName =
          authorizeProject(workflowService, projectId, securityService,
              authToken, "trying to create checklist", UserRole.AUTHOR);
      workflowService.setLastModifiedBy(userName);

      final Project project = workflowService.getProject(projectId);
      final WorkflowBin workflowBin =
          workflowService.getWorkflowBin(workflowBinId);

      // Build up list of identifiers
      final List<String> clauses = workflowBin.getTrackingRecords().stream()
          // Skip records on worklists if excludeWorklist is used
          // Skip records with a clusterType if cluster type doesn't match
          .filter(record -> !(excludeOnWorklist
              && !ConfigUtility.isEmpty(record.getWorklistName()))
              && !(clusterType != null
                  && !record.getClusterType().equals(clusterType)))
          .map(r -> "id:" + r.getId()).collect(Collectors.toList());
      final String idQuery = ConfigUtility.composeQuery("OR", clauses);
      final String finalQuery =
          ConfigUtility.composeQuery("AND", idQuery, query);

      // Handle "randomize"
      if (randomize) {
        pfs.setSortField("RANDOM");
      } else {
        pfs.setSortField("clusterId");
      }

      final TrackingRecordList list =
          workflowService.findTrackingRecords(project, finalQuery, pfs);

      final ChecklistJpa checklist = new ChecklistJpa();
      checklist.setName(name);
      if (description != null) {
        checklist.setDescription(description);
      } else {
        checklist.setDescription(name + " description");
      }
      checklist.setProject(project);
      checklist.setTimestamp(new Date());

      final Checklist newChecklist = workflowService.addChecklist(checklist);
      for (final TrackingRecord record : list.getObjects()) {
        final TrackingRecord copy = new TrackingRecordJpa(record);
        copy.setId(null);
        copy.setChecklistName(name);
        workflowService.addTrackingRecord(copy);
        newChecklist.getTrackingRecords().add(copy);
      }
      workflowService.updateChecklist(newChecklist);

      return newChecklist;
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
    @ApiParam(value = "Project id, e.g. 5", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Workflow bin id, e.g. 5", required = true) @QueryParam("workflowBinId") Long workflowBinId,
    @ApiParam(value = "Cluster type", required = false) @QueryParam("clusterType") String clusterType,
    @ApiParam(value = "PFS Parameter, e.g. '{ \"startIndex\":\"1\", \"maxResults\":\"5\" }'", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful POST call (Workflow): /worklist/add ");

    // Only allow one user in here at a time.
    synchronized (lock) {
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

        if (workflowBin == null) {
          throw new LocalException(
              "Attempt to create a worklist from a nonexistent bin "
                  + workflowBinId);
        }

        if (currentEpoch == null) {
          throw new Exception(
              "No current workflow epoch exists for this project " + projectId);
        }

        // Compose the worklist name from the current epoch, the bin name,
        // and the max worklist id+1. (e.g. wrk16a_demotions_chem_001)
        final StringBuilder worklistName = new StringBuilder();
        worklistName.append("wrk").append(currentEpoch.getName()).append("_");
        worklistName.append(workflowBin.getName()).append("_");
        if (clusterType != null)
          worklistName.append(clusterType).append("_");

        // Obtain the next worklist number for this naming scheme
        final PfsParameter worklistQueryPfs = new PfsParameterJpa();
        worklistQueryPfs.setStartIndex(0);
        worklistQueryPfs.setMaxResults(1);
        worklistQueryPfs.setSortField("name");
        worklistQueryPfs.setAscending(false);
        final StringBuilder query = new StringBuilder();
        if (clusterType == null) {
          query.append("name:").append("wrk").append(currentEpoch.getName()
              + "_" + workflowBin.getName() + "_0" + '*');
        } else {
          query.append("name:").append("wrk").append(currentEpoch.getName()
              + "_" + workflowBin.getName() + "_" + clusterType + '*');
        }
        final WorklistList worklistList = workflowService.findWorklists(project,
            query.toString(), worklistQueryPfs);
        int nextNumber = worklistList.getObjects().size() == 0 ? 1
            : worklistList.getObjects().get(0).getNumber() + 1;
        worklistName.append(
            new String(Integer.toString(nextNumber + 1000)).substring(1));

        // build query to retrieve tracking records that will be in worklist
        final StringBuilder sb = new StringBuilder();
        sb.append("workflowBinName:").append(workflowBin.getName());
        sb.append(" AND ").append("NOT worklistName:[* TO *] ");
        if (clusterType != null) {
          sb.append(" AND ").append("clusterType:").append(clusterType);
        } else {
          sb.append(" AND NOT clusterType:[* TO *]");
        }

        final PfsParameter localPfs =
            pfs == null ? new PfsParameterJpa() : new PfsParameterJpa(pfs);
        // Always work in clusterId order
        localPfs.setSortField("clusterId");
        final TrackingRecordList recordResultList = workflowService
            .findTrackingRecords(project, sb.toString(), localPfs);

        // Bail if there are no more records to make worklists from
        if (recordResultList.getCount() == 0) {
          throw new LocalException(
              "No more unassigned clusters in workflow bin");
        }

        final WorklistJpa worklist = new WorklistJpa();
        worklist.setName(worklistName.toString());
        worklist.setDescription(worklistName.toString() + " description");
        worklist.setProject(project);
        worklist.setWorkflowStatus(WorkflowStatus.NEW);
        worklist.setNumber(nextNumber);
        worklist.setProjectId(project.getId());
        worklist.setTimestamp(new Date());
        worklist.setWorkflowBinName(workflowBin.getName());

        final Worklist newWorklist = workflowService.addWorklist(worklist);

        for (final TrackingRecord record : recordResultList.getObjects()) {
          // Set worklist name of bin's copy of tracking record
          record.setWorklistName(worklistName.toString());
          workflowService.updateTrackingRecord(record);
          // Reuse bins tracking record for worklist
          final TrackingRecord worklistRecord = new TrackingRecordJpa(record);
          worklistRecord.setId(null);
          worklistRecord.setWorklistName(worklistName.toString());
          workflowService.addTrackingRecord(worklistRecord);
          newWorklist.getTrackingRecords().add(worklistRecord);
        }
        workflowService.updateWorklist(newWorklist);

        return newWorklist;
      } catch (Exception e) {
        handleException(e, "trying to create worklist");
      } finally {
        workflowService.close();
        securityService.close();
      }
      return null;
    }
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
          "trying to get workflow bins", UserRole.AUTHOR);
      final Project project = workflowService.getProject(projectId);
      final List<WorkflowBin> bins =
          workflowService.getWorkflowBins(project, type);

      // Track "unassigned" and "assigned"
      final Map<String, Integer> typeAssignedMap = new HashMap<>();
      final Map<String, Integer> typeUnassignedMap = new HashMap<>();
      for (final WorkflowBin bin : bins) {
        typeAssignedMap.clear();
        typeUnassignedMap.clear();
        final List<TrackingRecord> list = bin.getTrackingRecords();

        // If no tracking records, get the raw cluster ct
        if (list.size() == 0) {
          final ClusterTypeStats stats = new ClusterTypeStatsJpa();
          stats.setClusterType("all");
          stats.getStats().put("all", bin.getClusterCt());
          bin.getStats().add(stats);

          // skip the next section in this case
          continue;
        }

        for (final TrackingRecord record : list) {
          String clusterType = record.getClusterType();
          if (clusterType.isEmpty()) {
            clusterType = "default";
          }

          // Initialize map
          if (!typeAssignedMap.containsKey(clusterType)) {
            typeAssignedMap.put(clusterType, 0);
            typeUnassignedMap.put(clusterType, 0);
          }
          // compute "all" cluster type
          if (!typeAssignedMap.containsKey("all")) {
            typeAssignedMap.put("all", 0);
            typeUnassignedMap.put("all", 0);
          }

          // Increment assigned
          if (!ConfigUtility.isEmpty(record.getWorklistName())) {
            typeAssignedMap.put(clusterType,
                typeAssignedMap.get(clusterType) + 1);
            typeAssignedMap.put("all", typeAssignedMap.get("all") + 1);
          }

          // Otherwise increment unassigned
          else {
            typeUnassignedMap.put(clusterType,
                typeUnassignedMap.get(clusterType) + 1);
            typeUnassignedMap.put("all", typeUnassignedMap.get("all") + 1);
          }

        }
        // Now extract cluster types and add statistics
        for (final String clusterType : typeAssignedMap.keySet()) {

          // Skip "all" if there is only one cluster type
          if (typeAssignedMap.keySet().size() == 2
              && clusterType.equals("all")) {
            continue;
          }
          // Add statistics
          ClusterTypeStats stats = new ClusterTypeStatsJpa();
          stats.setClusterType(clusterType);
          int unassigned = typeUnassignedMap.get(clusterType);
          int assigned = typeAssignedMap.get(clusterType);
          stats.getStats().put("all", unassigned + assigned);
          stats.getStats().put("unassigned", unassigned);
          stats.getStats().put("assigned", assigned);
          bin.getStats().add(stats);
        }
      }

      Collections.sort(bins, (o1, o2) -> o1.getRank() - o2.getRank());

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
  @ApiOperation(value = "Get the worklist", notes = "Gets the statistics for the worklist.", response = WorklistJpa.class)
  public Worklist getWorklist(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Worklist id, e.g. 5", required = false) @PathParam("id") Long id,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful POST call (Workflow): /worklist/" + id);
    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      final String userName =
          authorizeProject(workflowService, projectId, securityService,
              authToken, "trying to get worklist stats", UserRole.AUTHOR);

      workflowService.setLastModifiedBy(userName);

      final Worklist worklist = workflowService.getWorklist(id);

      worklist.getStats().put("clusterCt",
          worklist.getTrackingRecords().size());
      // Add up orig concepts size from all tracking records
      worklist.getStats().put("conceptCt",
          worklist.getTrackingRecords().stream().collect(
              Collectors.summingInt(w -> w.getOrigConceptIds().size())));

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
  @Path("/checklist/{id}")
  @ApiOperation(value = "Get the checklist", notes = "Gets the statistics for the checklist.", response = ChecklistJpa.class)
  public Checklist getChecklist(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Checklist id, e.g. 5", required = false) @PathParam("id") Long id,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful POST call (Workflow): /checklist/" + id);
    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      final String userName =
          authorizeProject(workflowService, projectId, securityService,
              authToken, "trying to get checklist stats", UserRole.AUTHOR);

      workflowService.setLastModifiedBy(userName);

      final Checklist checklist = workflowService.getChecklist(id);

      checklist.getStats().put("clusterCt",
          checklist.getTrackingRecords().size());
      // Add up orig concepts size from all tracking records
      checklist.getStats().put("conceptCt",
          checklist.getTrackingRecords().stream().collect(
              Collectors.summingInt(w -> w.getOrigConceptIds().size())));

      // TODO to be done later
      // compute the stats and add them to the stats object
      // n_actions -1 - molecular action search by concept ids on checklist
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

      // return the checklist
      workflowService.handleLazyInit(checklist);
      return checklist;

    } catch (Exception e) {
      handleException(e, "trying to get checklist stats");
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
    @ApiParam(value = "Workflow bin id, e.g. 1", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful POST call (Workflow): /bin/" + id + "/clear ");

    final WorkflowServiceJpa workflowService = new WorkflowServiceJpa();
    try {
      final String userName = authorizeProject(workflowService, projectId,
          securityService, authToken, "trying to clear bin", UserRole.AUTHOR);
      workflowService.setLastModifiedBy(userName);

      workflowService.getWorkflowBin(id);

      // remove bins and all of the tracking records in the bin
      workflowService.removeWorkflowBin(id, true);

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
    @ApiParam(value = "Workflow bin id, e.g. 5", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Workflow bin type", required = true) WorkflowBinType type,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful POST call (Workflow): /bin/" + id + "/regenerate ");

    // Only one user can regenerate a bin at a time
    synchronized (lock) {

      final WorkflowServiceJpa workflowService = new WorkflowServiceJpa();
      try {
        final String userName = authorizeProject(workflowService, projectId,
            securityService, authToken, "trying to regenerate a single bin",
            UserRole.AUTHOR);
        workflowService.setLastModifiedBy(userName);

        // Set transaction scope
        workflowService.setTransactionPerOperation(false);
        workflowService.beginTransaction();

        // Read relevant workflow objects
        final Project project = workflowService.getProject(projectId);
        final WorkflowBin bin = workflowService.getWorkflowBin(id);

        // Remove the workflow bin
        workflowService.removeWorkflowBin(id, true);

        // Get the bin definitions
        final List<WorkflowBinDefinition> definitions =
            workflowService.getWorkflowBinDefinitions(project, type);
        WorkflowBin newBin = null;
        for (final WorkflowBinDefinition definition : definitions) {
          if (definition.getName().equals(bin.getName())) {
            newBin = this.regenerateBinHelper(project, definition,
                bin.getRank(), new HashSet<>(),
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
  }

  /* see superclass */
  @Override
  @GET
  @Produces(MediaType.TEXT_PLAIN)
  @Path("/worklist/{id}/report/generate")
  @ApiOperation(value = "Generate concept reports for worklist", notes = "Generate concept reports for the specified worklist", response = String.class)
  public String generateConceptReport(
    @ApiParam(value = "Project id, e.g. 5") @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Worklist id, e.g. 5") @PathParam("id") Long id,
    @ApiParam(value = "Delay", required = false) @QueryParam("delay") Long delay,
    @ApiParam(value = "Send email, e.g. false", required = false) @QueryParam("sendEmail") Boolean sendEmail,
    @ApiParam(value = "Concept report type", required = true) @QueryParam("conceptReportType") String conceptReportType,
    @ApiParam(value = "Relationship count", required = false) @QueryParam("relationshipCt") Integer relationshipCt,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /report/" + id + "/report/generate ");

    final WorkflowServiceJpa workflowService = new WorkflowServiceJpa();
    final ReportServiceJpa reportService = new ReportServiceJpa();
    StringBuilder conceptReport = new StringBuilder();
    try {
      final String userName =
          authorizeProject(workflowService, projectId, securityService,
              authToken, "trying to generate concept report", UserRole.AUTHOR);
      workflowService.setLastModifiedBy(userName);

      // Read vars
      final Project project = workflowService.getProject(projectId);
      final Worklist worklist = workflowService.getWorklist(id);
      final PfsParameter pfs = new PfsParameterJpa();
      pfs.setSortField("clusterId");
      final TrackingRecordList recordList = workflowService.findTrackingRecords(
          project, "worklistName:" + worklist.getName(), pfs);

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
      final String fileName = worklist.getName() + "_rpt.txt";
      final String uploadDir = ConfigUtility.getUploadDir();
      final File reportsDir =
          new File(uploadDir + "/" + projectId + "/reports");
      final File file = new File(reportsDir, fileName);
      if (file.exists()) {
        throw new Exception(
            "Worklist report file already exists - " + file.getAbsolutePath());
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
      out.flush();
      out.close();

      // If sendEmail, handle sending email - to the email for the user who
      // requested the build
      if (sendEmail) {
        final User user = securityService.getUser(userName);
        final Properties config = ConfigUtility.getConfigProperties();
        ConfigUtility.sendEmail(
            "[Terminology Server] Worklist Concept Report " + fileName,
            config.getProperty("mail.smtp.user"), user.getEmail(),
            "The worklist concept report " + fileName
                + " has been successfully generated.",
            config, "true".equals(config.get("mail.smtp.auth")));
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

    final WorkflowServiceJpa workflowService = new WorkflowServiceJpa();
    StringList stringList = new StringList();
    List<String> matchingFiles = new ArrayList<>();
    try {
      final String uploadDir = ConfigUtility.getUploadDir();
      final String filePath = uploadDir + "/" + projectId + "/reports";
      final File dir = new File(filePath);
      if (!dir.exists()) {
        throw new Exception("No reports exist for path " + filePath);
      }
      int i = 0;
      for (final String file : dir.list()) {
        i++;
        if (ConfigUtility.isEmpty(query) || file.contains(query)) {
          matchingFiles.add(file);
        }
      }
      Collections.sort(matchingFiles);
      if (pfs != null && pfs.getStartIndex() == -1) {
        stringList.setObjects(matchingFiles);
      } else {
        // Or get a substring
        stringList.setObjects(matchingFiles.subList(pfs.getStartIndex(),
            Math.min((pfs.getStartIndex() + pfs.getMaxResults()),
                matchingFiles.size() - 1)));
      }
      stringList.setTotalCount(i);
      return stringList;

    } catch (Exception e) {
      handleException(e,
          e.getMessage() + ". Trying to find generated concept reports.");
    } finally {

      workflowService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @GET
  @Produces(MediaType.TEXT_PLAIN)
  @Path("/report/{fileName}")
  @ApiOperation(value = "Get generated concept report", notes = "Get generated concept report", response = String.class)
  public String getGeneratedConceptReport(
    @ApiParam(value = "Project id, e.g. 5") @QueryParam("projectId") Long projectId,
    @ApiParam(value = "File name") @PathParam("fileName") String fileName,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful POST call (Workflow): /report/" + fileName);

    final WorkflowServiceJpa workflowService = new WorkflowServiceJpa();
    try {
      final String uploadDir = ConfigUtility.getUploadDir();
      final String filePath =
          uploadDir + "/" + projectId + "/reports/" + fileName;
      final File file = new File(filePath);
      if (!file.exists()) {
        throw new Exception("No report exists for path " + filePath);
      }
      // Return file contents

      return FileUtils.readFileToString(file, "UTF-8");

    } catch (Exception e) {
      handleException(e,
          e.getMessage() + ". Trying to find generated concept report.");
    } finally {
      workflowService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @DELETE
  @Path("/report/{fileName}/remove")
  @ApiOperation(value = "Get generated concept report", notes = "Get generated concept report", response = String.class)
  public void removeGeneratedConceptReport(
    @ApiParam(value = "Project id, e.g. 5") @QueryParam("projectId") Long projectId,
    @ApiParam(value = "File name") @PathParam("fileName") String fileName,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful POST call (Workflow): /report/" + fileName + "/remove");
    final WorkflowServiceJpa workflowService = new WorkflowServiceJpa();

    try {
      final String uploadDir = ConfigUtility.getUploadDir();
      final String filePath =
          uploadDir + "/" + projectId + "/reports/" + fileName;
      FileUtils.forceDelete(new File(filePath));
    } catch (Exception e) {
      handleException(e,
          e.getMessage() + ". Trying to remove generated concept report.");
    } finally {
      workflowService.close();
      securityService.close();
    }
  }

  /**
   * Execute query.
   *
   * @param query the query
   * @param queryType the query type
   * @param params the params
   * @param workflowService the workflow service
   * @return the list
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  private List<Long[]> executeQuery(String query, QueryType queryType,
    Map<String, String> params, WorkflowServiceJpa workflowService)
    throws Exception {

    // Handle the LUCENE case
    if (queryType == QueryType.LUCENE) {
      final PfsParameter pfs = new PfsParameterJpa();
      pfs.setQueryRestriction(query);
      // precondition check
      if (params == null || !params.containsKey("terminology")) {
        throw new Exception(
            "Execute query should be passed params with the key 'terminology'"
                + params);
      }
      // Perform search
      final SearchResultList resultList = workflowService
          .findConcepts(params.get("terminology"), null, null, null, pfs);
      // Cluster results
      final List<Long[]> results = new ArrayList<>();
      for (final SearchResult concept : resultList.getObjects()) {
        final Long[] result = new Long[2];
        result[0] = concept.getId();
        result[1] = concept.getId();
        results.add(result);
      }
      return results;
    }

    // Handle PROGRAM queries
    if (queryType == QueryType.PROGRAM) {
      throw new Exception("PROGRAM queries not yet supported");
    }

    // Handle SQL and JQL queries here
    // Check for JQL/SQL errors
    // ensure that query begins with SELECT (i.e. prevent injection
    // problems)
    if (!query.toUpperCase().startsWith("SELECT")) {
      throw new LocalException(
          "Query has bad format:  does not begin with SELECT");
    }

    // check for multiple commands (i.e. multiple semi-colons)
    if (query.indexOf(";") != query.length() - 1 && query.endsWith(";")) {
      throw new LocalException(
          "Query has bad format:  multiple queries detected");
    }

    // crude check: check for data manipulation commands
    if (query.toUpperCase()
        .matches("ALTER |CREATE |DROP |DELETE |INSERT |TRUNCATE |UPDATE ")) {
      throw new LocalException("Query has bad format:  DDL request detected");
    }

    // check for proper format for insertion into reports

    if (query.toUpperCase().indexOf("FROM") == -1) {
      throw new LocalException("Query must contain the term FROM");
    }

    final String selectSubStr =
        query.substring(0, query.toUpperCase().indexOf(" FROM "));

    boolean conceptQuery = false;
    boolean dualConceptQuery = false;
    boolean clusterQuery = false;

    if (selectSubStr.contains("conceptId")) {
      conceptQuery = true;
    }

    if (selectSubStr.contains("conceptId1")
        && selectSubStr.contains("conceptId2")) {
      dualConceptQuery = true;
    }

    if (selectSubStr.contains("clusterId")) {
      clusterQuery = true;
    }

    if (!conceptQuery && !dualConceptQuery && !clusterQuery) {
      throw new LocalException(
          "Query must have either clusterId,conceptId OR conceptId OR conceptId1,conceptId2 fields in the SELECT statement");
    }

    if (dualConceptQuery && clusterQuery) {
      throw new LocalException(
          "Query must have either clusterId,conceptId OR conceptId OR conceptId1,conceptId2 fields in the SELECT statement");
    }

    // Execute the query
    javax.persistence.Query jpaQuery = null;
    if (queryType == QueryType.SQL) {
      jpaQuery = workflowService.getEntityManager().createNativeQuery(query);
    } else if (queryType == QueryType.JQL) {
      jpaQuery = workflowService.getEntityManager().createQuery(query);
    } else {
      throw new Exception("Unsupported query type " + queryType);
    }
    if (params != null) {
      for (final String key : params.keySet()) {
        if (query.contains(":" + key)) {
          jpaQuery.setParameter(key, params.get(key));
        }
      }
    }
    Logger.getLogger(getClass()).info("  query = " + query);

    // Handle simple concept type
    if (conceptQuery && !dualConceptQuery && !clusterQuery) {
      final List<Object[]> list = jpaQuery.getResultList();
      final List<Long[]> results = new ArrayList<>();
      for (final Object[] entry : list) {
        final Long conceptId = ((BigInteger) entry[0]).longValue();
        final Long[] result = new Long[2];
        result[0] = conceptId;
        result[1] = conceptId;
        results.add(result);
      }
      return results;
    }

    // Handle concept,concept type
    if (dualConceptQuery) {
      final List<Object[]> list = jpaQuery.getResultList();

      final Map<Long, Set<Long>> parChd = new HashMap<>();
      final Map<Long, Set<Long>> chdPar = new HashMap<>();
      for (final Object[] entry : list) {
        final Long conceptId1 = ((BigInteger) entry[0]).longValue();
        final Long conceptId2 = ((BigInteger) entry[1]).longValue();
        final Long par = Math.min(conceptId1, conceptId2);
        final Long chd = Math.max(conceptId1, conceptId2);
        // skip self-ref
        if (par.equals(chd)) {
          continue;
        }
        if (!parChd.containsKey(par)) {
          parChd.put(par, new HashSet<>());
        }
        parChd.get(par).add(chd);
        if (!chdPar.containsKey(chd)) {
          chdPar.put(chd, new HashSet<>());
        }
        chdPar.get(chd).add(par);
      }

      // Recurse down the parChd tree for each key that isn't also a child (e.g.
      // these are the roots and also the cluster ids)
      final List<Long[]> results = new ArrayList<>();
      for (final Long par : parChd.keySet()) {
        // Skip keys that are themselves children of other nodes
        if (chdPar.containsKey(par)) {
          continue;
        }
        // Put the parent itself into the results
        final Long[] result = new Long[2];
        result[0] = par;
        result[1] = par;
        results.add(result);

        // recurse down the entire parChd graph for "root" keys
        final Set<Long> descendants = new HashSet<>();
        getDescendants(par, parChd, descendants);
        for (final Long desc : descendants) {
          // Create and add results for each descendatn
          final Long[] result2 = new Long[2];
          result2[0] = par;
          result2[1] = desc;
          results.add(result);
        }
      }
      return results;
    }

    // Otherwise, just return the result list as longs.
    // this is just the regular clusterQuery case
    final List<Object[]> list = jpaQuery.getResultList();
    final List<Long[]> results = new ArrayList<>();
    for (final Object[] entry : list) {
      final Long[] result = new Long[] {
          ((BigInteger) entry[0]).longValue(),
          ((BigInteger) entry[1]).longValue()
      };
      results.add(result);
    }
    return results;

  }

  /**
   * get all descendants of the parent node.
   *
   * @param par the par
   * @param parChd the par chd
   * @param result the result
   */
  private void getDescendants(Long par, Map<Long, Set<Long>> parChd,
    Set<Long> result) {
    if (!parChd.containsKey(par)) {
      return;
    }
    // Iterate through all children, add and recurse
    for (final Long chd : parChd.get(par)) {
      result.add(chd);
      getDescendants(chd, parChd, result);
    }

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
    worklists.addAll(workflowService.findWorklists(project, sb.toString(), null)
        .getObjects());

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
    Logger.getLogger(getClass()).info("Regenerate bin " + definition.getName());

    // Create the workflow bin
    final WorkflowBin bin = new WorkflowBinJpa();
    bin.setCreationTime(new Date().getTime());
    bin.setName(definition.getName());
    bin.setDescription(definition.getDescription());
    bin.setEditable(definition.isEditable());
    bin.setEnabled(definition.isEnabled());
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
    final Map<String, String> params = new HashMap<>();
    params.put("terminology", project.getTerminology());

    List<Long[]> results =
        executeQuery(query, definition.getQueryType(), params, workflowService);

    if (results == null)
      throw new Exception("Failed to retrieve results for query");

    final Map<Long, Set<Long>> clusterIdConceptIdsMap = new HashMap<>();
    Logger.getLogger(getClass()).info("  results = " + results.size());

    // put query results into map
    for (final Long[] result : results) {
      final Long clusterId = Long.parseLong(result[0].toString());
      final Long componentId = Long.parseLong(result[1].toString());

      // skip result entry where the conceptId is already in conceptsSeen
      // and workflow config is mutually exclusive
      if (!conceptsSeen.contains(componentId)
          || !definition.getWorkflowConfig().isMutuallyExclusive()) {
        if (clusterIdConceptIdsMap.containsKey(clusterId)) {
          final Set<Long> componentIds = clusterIdConceptIdsMap.get(clusterId);
          componentIds.add(componentId);
          clusterIdConceptIdsMap.put(clusterId, componentIds);
        } else {
          final Set<Long> componentIds = new HashSet<>();
          componentIds.add(componentId);
          clusterIdConceptIdsMap.put(clusterId, componentIds);
        }
      }
      if (definition.getWorkflowConfig().isMutuallyExclusive()) {
        conceptsSeen.add(componentId);
      }
    }

    // Set the raw cluster count
    bin.setClusterCt(clusterIdConceptIdsMap.size());
    Logger.getLogger(getClass())
        .info("  clusters = " + clusterIdConceptIdsMap.size());

    // for each cluster in clusterIdComponentIdsMap create a tracking record if
    // unassigned bin
    if (definition.isEditable() && definition.isEnabled()) {
      long clusterIdCt = 1L;
      for (Long clusterId : clusterIdConceptIdsMap.keySet()) {

        // Create the tracking record
        final TrackingRecord record = new TrackingRecordJpa();
        record.setClusterId(clusterIdCt++);
        record.setTerminology(project.getTerminology());
        record.setTimestamp(new Date());
        record.setVersion("latest");
        record.setWorkflowBinName(bin.getName());
        record.setProject(project);
        record.setWorklistName(null);
        record.setClusterType("");
        record.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);

        // Load the concept ids involved
        StringBuilder conceptNames = new StringBuilder();
        for (final Long conceptId : clusterIdConceptIdsMap.get(clusterId)) {
          final Concept concept = workflowService.getConcept(conceptId);
          record.getOrigConceptIds().add(conceptId);
          // collect all the concept names for the indexed data
          conceptNames.append(concept.getName()).append(" ");

          // Set cluster type if a concept has an STY associated with a cluster
          // type in th eproject
          if (record.getClusterType().equals("")) {
            for (SemanticTypeComponent sty : concept.getSemanticTypes()) {
              if (project.getSemanticTypeCategoryMap()
                  .containsKey(sty.getSemanticType())) {
                record.setClusterType(project.getSemanticTypeCategoryMap()
                    .get(sty.getSemanticType()));
                break;
              }
            }
          }
          // Add all atom ids as component ids
          for (final Atom atom : concept.getAtoms()) {
            record.getComponentIds().add(atom.getId());

            // compute workflow status for atoms
            if (atom.getWorkflowStatus() == WorkflowStatus.NEEDS_REVIEW) {
              record.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
            }
          }

          // Set the worklist name
          if (record.getWorklistName() == null) {
            if (conceptIdWorklistNameMap.containsKey(conceptId)) {
              record.setWorklistName(conceptIdWorklistNameMap.get(conceptId));
            }
          }

          // Compute workflow status for tracking record
          if (concept.getWorkflowStatus() == WorkflowStatus.NEEDS_REVIEW) {
            record.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
          }

        }
        record.setIndexedData(conceptNames.toString());

        workflowService.addTrackingRecord(record);
        bin.getTrackingRecords().add(record);
      }
    }
    workflowService.updateWorkflowBin(bin);

    return bin;

  }

  /* see superclass */
  @Override
  @PUT
  @Path("/checklist/{id}/note/add")
  @Consumes("text/plain")
  @ApiOperation(value = "Add checklist note", notes = "Adds a checklist note", response = ChecklistNoteJpa.class)
  public Note addChecklistNote(
    @ApiParam(value = "Project id, e.g. 3", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Checklist id, e.g. 3", required = true) @PathParam("id") Long checklistId,
    @ApiParam(value = "The note, e.g. \"this is a sample note\"", required = true) String note,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful POST call (Checklist): /checklist/" + checklistId
            + "/note/add " + note);

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      final String userName = authorizeProject(workflowService, projectId,
          securityService, authToken, "adding checklist note", UserRole.AUTHOR);
      workflowService.setLastModifiedBy(userName);

      final Checklist checklist = workflowService.getChecklist(checklistId);
      if (checklist == null) {
        throw new Exception("Invalid checklist id " + checklistId);
      }

      final Note checklistNote = new ChecklistNoteJpa();
      checklistNote.setLastModifiedBy(userName);
      checklistNote.setNote(note);
      ((ChecklistNoteJpa) checklistNote).setChecklist(checklist);

      // Add and return the note
      final Note newNote = workflowService.addNote(checklistNote);

      // For indexing
      checklist.getNotes().add(newNote);
      workflowService.updateChecklist(checklist);

      return newNote;
    } catch (Exception e) {
      handleException(e, "trying to add note");
      return null;
    } finally {
      workflowService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @PUT
  @Path("/worklist/{id}/note/add")
  @Consumes("text/plain")
  @ApiOperation(value = "Add worklist note", notes = "Adds a worklist note", response = WorklistNoteJpa.class)
  public Note addWorklistNote(
    @ApiParam(value = "Project id, e.g. 3", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Worklist id, e.g. 3", required = true) @PathParam("id") Long worklistId,
    @ApiParam(value = "The note, e.g. \"this is a sample note\"", required = true) String note,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful POST call (Worklist): /worklist/"
        + worklistId + "/note/add " + note);

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      final String userName = authorizeProject(workflowService, projectId,
          securityService, authToken, "adding worklist note", UserRole.AUTHOR);
      workflowService.setLastModifiedBy(userName);

      final Worklist worklist = workflowService.getWorklist(worklistId);
      if (worklist == null) {
        throw new Exception("Invalid worklist id " + worklistId);
      }

      final Note worklistNote = new WorklistNoteJpa();
      worklistNote.setLastModifiedBy(userName);
      worklistNote.setNote(note);
      ((WorklistNoteJpa) worklistNote).setWorklist(worklist);

      // Add and return the note
      final Note newNote = workflowService.addNote(worklistNote);

      // For indexing
      worklist.getNotes().add(newNote);
      workflowService.updateWorklist(worklist);

      return newNote;
    } catch (Exception e) {
      handleException(e, "trying to add note");
      return null;
    } finally {
      workflowService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @DELETE
  @Path("/checklist/note/{id}/remove")
  @ApiOperation(value = "Remove checklist note", notes = "Removes the specified checklist note")
  public void removeChecklistNote(
    @ApiParam(value = "Project id, e.g. 3", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Note id, e.g. 3", required = true) @PathParam("id") Long noteId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call DELETE (Checklist): /checklist/note/" + noteId
            + "/remove");

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      final String userName = authorizeProject(workflowService, projectId,
          securityService, authToken, "remove checklist note", UserRole.AUTHOR);
      workflowService.setLastModifiedBy(userName);

      final ChecklistNoteJpa note = (ChecklistNoteJpa) workflowService
          .getNote(noteId, ChecklistNoteJpa.class);
      final Checklist checklist = note.getChecklist();

      if (!checklist.getProject().getId().equals(projectId)) {
        throw new Exception(
            "Attempt to remove a note from a different project.");
      }

      // remove note
      workflowService.removeNote(noteId, ChecklistNoteJpa.class);

      // For indexing
      checklist.getNotes().remove(note);
      workflowService.updateChecklist(checklist);

    } catch (Exception e) {
      handleException(e, "trying to remove a checklist note");
    } finally {
      workflowService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @DELETE
  @Path("/worklist/note/{id}/remove")
  @ApiOperation(value = "Remove worklist note", notes = "Removes the specified worklist note")
  public void removeWorklistNote(
    @ApiParam(value = "Project id, e.g. 3", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Note id, e.g. 3", required = true) @PathParam("id") Long noteId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call DELETE (Worklist): /worklist/note/" + noteId + "/remove");

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      final String userName = authorizeProject(workflowService, projectId,
          securityService, authToken, "remove worklist note", UserRole.AUTHOR);
      workflowService.setLastModifiedBy(userName);

      final WorklistNoteJpa note = (WorklistNoteJpa) workflowService
          .getNote(noteId, WorklistNoteJpa.class);
      final Worklist worklist = note.getWorklist();

      if (!worklist.getProject().getId().equals(projectId)) {
        throw new Exception(
            "Attempt to remove a note from a different project.");
      }

      // remove note
      workflowService.removeNote(noteId, WorklistNoteJpa.class);

      // For indexing
      worklist.getNotes().remove(note);
      workflowService.updateWorklist(worklist);

    } catch (Exception e) {
      handleException(e, "trying to remove a worklist note");
    } finally {
      workflowService.close();
      securityService.close();
    }
  }
}
