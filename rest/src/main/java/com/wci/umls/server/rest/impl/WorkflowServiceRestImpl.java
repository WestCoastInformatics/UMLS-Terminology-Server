package com.wci.umls.server.rest.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import com.wci.umls.server.UserRole;
import com.wci.umls.server.helpers.ChecklistList;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.helpers.SearchResult;
import com.wci.umls.server.helpers.SearchResultList;
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
import com.wci.umls.server.jpa.worfklow.ChecklistJpa;
import com.wci.umls.server.jpa.worfklow.TrackingRecordJpa;
import com.wci.umls.server.jpa.worfklow.WorkflowBinDefinitionJpa;
import com.wci.umls.server.jpa.worfklow.WorkflowBinJpa;
import com.wci.umls.server.jpa.worfklow.WorkflowConfigJpa;
import com.wci.umls.server.jpa.worfklow.WorklistJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.SemanticTypeComponent;
import com.wci.umls.server.model.workflow.Checklist;
import com.wci.umls.server.model.workflow.TrackingRecord;
import com.wci.umls.server.model.workflow.WorkflowAction;
import com.wci.umls.server.model.workflow.WorkflowBin;
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

  @Override
  @POST
  @Path("/config/add")
  @ApiOperation(value = "Add a workflow config", notes = "Add a workflow config", response = WorkflowConfigJpa.class)
  public WorkflowConfig addWorkflowConfig(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Workflow config to add", required = true) WorkflowConfigJpa workflowConfig,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /config/add/" + projectId + " "
            + workflowConfig.toString() + " " + authToken);

    String action = "trying to add workflow config";

    WorkflowService workflowService = new WorkflowServiceJpa();

    try {

      // authorize and get user name from the token
      final String authUser = authorizeProject(workflowService, projectId,
          securityService, authToken, action, UserRole.AUTHOR);

      workflowService.setLastModifiedBy(authUser);
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

    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /config/update/" + projectId + " "
            + config.getId() + " " + authToken);

    String action = "trying to update workflow config";

    WorkflowService workflowService = new WorkflowServiceJpa();

    try {

      // authorize and get user name from the token
      authorizeProject(workflowService, projectId, securityService, authToken,
          action, UserRole.AUTHOR);

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

    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /definition/add/" + projectId + " "
            + configId + " " + binDefinition.getName() + " " + authToken);

    String action = "trying to add workflow bin definition";

    WorkflowService workflowService = new WorkflowServiceJpa();

    try {

      // authorize and get user name from the token
      final String authUser = authorizeProject(workflowService, projectId,
          securityService, authToken, action, UserRole.AUTHOR);

      workflowService.setLastModifiedBy(authUser);

      WorkflowConfig workflowConfig =
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

    String action = "trying to update workflow bin definition";

    WorkflowService workflowService = new WorkflowServiceJpa();

    try {

      // authorize and get user name from the token
      authorizeProject(workflowService, projectId, securityService, authToken,
          action, UserRole.AUTHOR);

      workflowService.setLastModifiedBy(binDefinition.getLastModifiedBy());

      WorkflowBinDefinition origBinDefinition =
          workflowService.getWorkflowBinDefinition(binDefinition.getId());
      WorkflowConfig workflowConfig =
          workflowService.getWorkflowConfig(origBinDefinition
              .getWorkflowConfig().getId());
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
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Workflow bin definition id, e.g. 1", required = true) @PathParam("id") Long binDefinitionId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call (Workflow): /definition/remove");

    WorkflowService workflowService = new WorkflowServiceJpa();
    try {

      authorizeApp(securityService, authToken,
          "remove workflow bin definition", UserRole.USER);

      // load the bin definition, get its workflow config. Remove it from
      // workflow config, then remove it.
      WorkflowBinDefinition binDefinition =
          workflowService.getWorkflowBinDefinition(binDefinitionId);
      WorkflowConfig workflowConfig =
          workflowService.getWorkflowConfig(binDefinition.getWorkflowConfig()
              .getId());
      workflowService.setLastModifiedBy(workflowConfig.getLastModifiedBy());
      List<WorkflowBinDefinition> defsToKeep = new ArrayList<>();
      for (WorkflowBinDefinition def : workflowConfig
          .getWorkflowBinDefinitions()) {
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
  @POST
  @Path("/clear")
  @ApiOperation(value = "Clear bins", notes = "Clear bins")
  public void clearBins(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Workflow bin type", required = true) WorkflowBinType type,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info("RESTful POST call (Workflow): /clear ");
    
    final WorkflowServiceJpa workflowService = new WorkflowServiceJpa();
    try {
      String userName =
          authorizeProject(workflowService, projectId, securityService,
              authToken, "trying to clear bins", UserRole.AUTHOR);

      workflowService.setLastModifiedBy(userName);
      Project project = workflowService.getProject(projectId);  
      
   // TODO; error org.hibernate.loader.MultipleBagFetchException: cannot simultaneously fetch multiple bags
      
      // find workflow bins matching type and projectId
      final StringBuilder sb = new StringBuilder();
      
      if (project == null) {
        sb.append("projectId:[* TO *]");
      } else {
        sb.append("projectId:" + project.getId());
      }
      sb.append(" AND ");
      if (type == null || type.equals("")) {
        sb.append("type:[* TO *]");
      } else {
        sb.append("type:" + type);
      } 
      
      List<WorkflowBin> results = workflowService.findWorkflowBinsForQuery(sb.toString());
      
      //List<WorkflowBin> results = workflowService.getWorkflowBins();
      
      // remove bins and all of the tracking records in the bins
      for (WorkflowBin workflowBin : results) {
        workflowService.removeWorkflowBin(workflowBin.getId(), true);
      }
      
    } catch (Exception e) {
      handleException(e, "trying to clear bins");
    } finally {
      workflowService.close();
      securityService.close();
    }
  }  

  @Override
  @POST
  @Path("/bins")
  @ApiOperation(value = "Regenerate bins", notes = "Regenerate bins")
  public void regenerateBins(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Workflow bin type", required = true) WorkflowBinType type,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass()).info("RESTful POST call (Workflow): /bins ");

    final WorkflowServiceJpa workflowService = new WorkflowServiceJpa();
    try {
      String userName =
          authorizeProject(workflowService, projectId, securityService,
              authToken, "trying to regenerate bins", UserRole.AUTHOR);

      workflowService.setLastModifiedBy(userName);
      Project project = workflowService.getProject(projectId);

      WorkflowConfig workflowConfig =
          workflowService.getWorkflowConfig(projectId, type);

      // concepts seen set
      Set<Long> conceptsSeen = new HashSet<>();
      
      // find active worklists
      Set<Worklist> worklists = new HashSet<>();
      StringBuilder sb = new StringBuilder();      
      sb.append("projectId:" + project.getId());
      sb.append(" AND NOT type:READY_FOR_PUBLICATION");
      worklists.addAll(workflowService.findWorklistsForQuery(sb.toString(), null).getObjects());
      
      // get tracking records that are on active worklists
      sb = new StringBuilder();
      sb.append("projectId:" + project.getId());
      sb.append(" AND worklist:[* TO *]");      
      TrackingRecordList recordList = workflowService.findTrackingRecordsForQuery(sb.toString(), null);
            
      // make map of conceptId -> active worklist name 
      // calculate which concepts are already out on worklists
      Map<Long, String> conceptIdWorklistNameMap = new HashMap<>();
      for (TrackingRecord trackingRecord : recordList.getObjects()) {
        for (Long conceptId : trackingRecord.getOrigConceptIds()) {
          conceptIdWorklistNameMap.put(conceptId, trackingRecord.getWorklist());
        }
      }
      

      int i = 0;
      for (WorkflowBinDefinition definition : workflowConfig
          .getWorkflowBinDefinitions()) {

        WorkflowBin bin = new WorkflowBinJpa();
        bin.setCreationTime(new Date());
        bin.setName(definition.getName());
        bin.setDescription(definition.getDescription());
        bin.setEditable(definition.isEditable());
        bin.setProject(project);
        bin.setRank(++i);
        bin.setTerminology(project.getTerminology());
        bin.setVersion("latest");
        bin.setTerminologyId("");
        bin.setTimestamp(new Date());
        bin.setType(type);
        workflowService.addWorkflowBin(bin);

        String query = definition.getQuery();

        // execute the query
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
            SearchResultList resultList = workflowService.findConceptsForQuery(
                project.getTerminology(), null, null, query, null);
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

          // skip result entry where the conceptId is already in conceptsSeen and
          // workflow config is mutually exclusive
          if (!conceptsSeen.contains(componentId)
              || !workflowConfig.isMutuallyExclusive()) {
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
          if (workflowConfig.isMutuallyExclusive()) {
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
            record.setWorkflowBin(bin.getName());
            record.setProject(project);

            record.setWorklist(null);
            record.setClusterType("");

            for (Long conceptId : clusterIdConceptIdsMap.get(clusterId)) {
              Concept concept = workflowService.getConcept(conceptId);
              record.getOrigConceptIds().add(conceptId);
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
              for (Atom atom : concept.getAtoms()) {
                record.getComponentIds().add(atom.getId());
              }
              if (record.getWorklist() == null) {
                if (conceptIdWorklistNameMap.containsKey(conceptId)) {
                  record.setWorklist(conceptIdWorklistNameMap.get(conceptId));
                  break;
                }
              }
            }

            workflowService.addTrackingRecord(record);
            bin.getTrackingRecords().add(record);
          }
        }
        workflowService.updateWorkflowBin(bin);
      }

    } catch (Exception e) {
      handleException(e, "trying to regenerate bins");
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
      WorkflowActionHandler handler =
          workflowService.getWorkflowHandlerForPath(project.getWorkflowPath());
      TrackingRecordList trackingRecords =
          handler.findAssignedWork(project, userName, pfs, workflowService);

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
      WorkflowActionHandler handler =
          workflowService.getWorkflowHandlerForPath(project.getWorkflowPath());
      TrackingRecordList trackingRecords =
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

      WorkflowActionHandler handler =
          workflowService.getWorkflowHandlerForPath(project.getWorkflowPath());
      WorklistList list =
          handler
              .findAssignedWorklists(project, userName, pfs, workflowService);

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

    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /checklists/" + projectId + " " + query
            + " " + authToken);

    String action = "trying to find checklists";

    WorkflowService workflowService = new WorkflowServiceJpa();

    try {

      // authorize and get user name from the token
      authorizeProject(workflowService, projectId, securityService, authToken,
          action, UserRole.AUTHOR);
      Project project = workflowService.getProject(projectId);

      return workflowService.findChecklistsForQuery(project, query, pfs);

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
  @ApiOperation(value = "Perform workflow action on a tracking record", notes = "Performs the specified action as the specified refset as the specified user", response = WorklistJpa.class)
  public Worklist performWorkflowAction(
    @ApiParam(value = "Project id, e.g. 5", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Worklist id, e.g. 5", required = false) @QueryParam("worklistId") Long worklistId,
    @ApiParam(value = "User name, e.g. author1", required = true) @QueryParam("userName") String userName,
    @ApiParam(value = "User role, e.g. AUTHOR", required = true) UserRole role,
    @ApiParam(value = "Workflow action, e.g. 'SAVE'", required = true) @QueryParam("action") WorkflowAction action,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful POST call (Workflow): /action " + action + ", " + projectId
            + ", " + userName);

    // Test preconditions
    if (projectId == null || userName == null) {
      handleException(new Exception("Required parameter has a null value"), "");
    }

    final WorkflowService workflowService = new WorkflowServiceJpa();
    try {
      final String authName =
          authorizeProject(workflowService, projectId, securityService,
              authToken, "perform workflow action", UserRole.AUTHOR);

      // Need to read the accurate, current workflow state of the concept if it
      // exists
      /*
       * if (trackingRecord.getId() != null) { final Concept c2 =
       * workflowService.getConcept(trackingRecord.getId());
       * trackingRecord.setWorkflowStatus(c2.getWorkflowStatus()); } else {
       * trackingRecord.setWorkflowStatus(WorkflowStatus.NEW); }
       */
      Worklist worklist = workflowService.getWorklist(worklistId);
      Project project = workflowService.getProject(projectId);
      // Set last modified by
      workflowService.setLastModifiedBy(authName);
      Worklist returnWorklist =
          workflowService.performWorkflowAction(project, worklist, userName,
              role, action);

      /*
       * TODO addLogEntry(workflowService, userName, "WORKFLOW action",
       * projectId, translationId, action + " as " + projectRole +
       * " on concept " + trackingRecord.getTerminologyId() + ", " +
       * trackingRecord.getName());
       */
      // handleLazyInit(record, workflowService);

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
        /*
         * TODO no project id available authorizeProject(contentService,
         * concept.getProject().getId(), securityService, authToken,
         * "get tracking records for concept", UserRole.AUTHOR);
         */

        authorizeApp(securityService, authToken,
            "get tracking records for concept", UserRole.AUTHOR);

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

      WorkflowActionHandler handler =
          workflowService.getWorkflowHandlerForPath(project.getWorkflowPath());
      WorklistList list =
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


    @Override
    @POST
    @Path("/checklist")
    @ApiOperation(value = "Find assigned work", notes = "Finds tracking records assigned", response = TrackingRecordListJpa.class)
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
      authorizeProject(workflowService, projectId, securityService, authToken,
          "trying to find available worklists", UserRole.AUTHOR);

      Project project = workflowService.getProject(projectId);

      // TODO augment query
      /*
Perform a search based on projectId, workflowBinName, query, pfs (in TrackingRecordJpa)
use pfs.setSortField("clusterId") by default.
for "randomize" -> see how to randomize a lucene search (maybe we can have a special sort field, "random")
excludeOnWorklist means worklistName is null (" AND NOT worklist:[* TO *] ")

       */
      TrackingRecordList recordResultList = workflowService.findTrackingRecordsForQuery(query, pfs);

      ChecklistJpa checklist = new ChecklistJpa();
      checklist.setName("test checklist");
      checklist.setDescription("test checklist description");
      checklist.setProject(project);
      checklist.setTimestamp(new Date());

      Checklist addedChecklist = workflowService.addChecklist(checklist);
      
      for (TrackingRecord record : recordResultList.getObjects()) {
        TrackingRecord checklistRecord = new TrackingRecordJpa(record);
        checklistRecord.setId(null);
        workflowService.addTrackingRecord(checklistRecord);
        addedChecklist.getTrackingRecords().add(checklistRecord);
        workflowService.updateChecklist(addedChecklist);
      }

      return addedChecklist;
    } catch (Exception e) {
      handleException(e, "trying to find available worklists");
    } finally {
      workflowService.close();
      securityService.close();
    }
    return null;
  }

}
