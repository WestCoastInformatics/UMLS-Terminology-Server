/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.rest.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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

import org.apache.log4j.Logger;

import com.wci.umls.server.ProcessConfig;
import com.wci.umls.server.UserRole;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.helpers.ProcessConfigList;
import com.wci.umls.server.helpers.StringList;
import com.wci.umls.server.jpa.ProcessConfigJpa;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.helpers.ProcessConfigListJpa;
import com.wci.umls.server.jpa.services.ProcessServiceJpa;
import com.wci.umls.server.jpa.services.SecurityServiceJpa;
import com.wci.umls.server.jpa.services.rest.ProcessServiceRest;
import com.wci.umls.server.services.ProcessService;
import com.wci.umls.server.services.SecurityService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Info;
import io.swagger.annotations.SwaggerDefinition;

/**
 * REST implementation for {@link ProcessServiceRest}..
 */
@Path("/process")
@Api(value = "/process")
@SwaggerDefinition(info = @Info(description = "Operations to interact with process and algorithm info.", title = "Process API", version = "1.0.1"))
@Consumes({
    MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
})
@Produces({
    MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
})
public class ProcessServiceRestImpl extends RootServiceRestImpl
    implements ProcessServiceRest {

  /** The security service. */
  private SecurityService securityService;

  /**
   * Instantiates an empty {@link ProcessServiceRestImpl}.
   *
   * @throws Exception the exception
   */
  public ProcessServiceRestImpl() throws Exception {
    securityService = new SecurityServiceJpa();
  }

  /* see superclass */
  @Override
  @PUT
  @Path("/processConfig/add")
  @ApiOperation(value = "Add new processConfig", notes = "Creates a new processConfig", response = ProcessConfigJpa.class)
  public ProcessConfig addProcessConfig(
    @ApiParam(value = "ProcessConfig, e.g. newProcessConfig", required = true) ProcessConfigJpa processConfig,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call PUT (Process): /processConfig/add for user "
            + authToken + ", " + processConfig);

    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName = authorizeProject(processService,
          processConfig.getProjectId(), securityService, authToken,
          "adding a process config", UserRole.ADMINISTRATOR);
      processService.setLastModifiedBy(userName);      
      
      // check to see if processConfig already exists
      if (processService.findProcessConfigs(processConfig.getProjectId(),
          "name:\"" + processConfig.getName() + "\"", null).size() > 0) {
        throw new LocalException(
            "A processConfig with this name and description already exists for this project");
      }

      // Add processConfig
      final ProcessConfig newProcessConfig =
          processService.addProcessConfig(processConfig);
      
      processService.addLogEntry(userName, processConfig.getProjectId(),
          processConfig.getId(), null, null,
          "ADD processConfig - " + processConfig);

      return newProcessConfig;
    } catch (Exception e) {
      handleException(e, "trying to add a processConfig");
      return null;
    } finally {
      processService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @POST
  @Path("/processConfig/update")
  @ApiOperation(value = "Update processConfig", notes = "Updates the specified processConfig")
  public void updateProcessConfig(
    @ApiParam(value = "ProcessConfig, e.g. existingProcessConfig", required = true) ProcessConfigJpa processConfig,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call PUT (Process): /processConfig/update for user "
            + authToken + ", " + processConfig);

    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName = authorizeProject(processService,
          processConfig.getProjectId(), securityService, authToken,
          "update processConfig", UserRole.ADMINISTRATOR);
      processService.setLastModifiedBy(userName);

      // check to see if processConfig exists
      final ProcessConfig origProcessConfig =
          processService.getProcessConfig(processConfig.getId());
      if (origProcessConfig == null) {
        throw new Exception(
            "ProcessConfig " + processConfig.getId() + " does not exist");
      }

      // Update processConfig
      processService.updateProcessConfig(processConfig);

      processService.addLogEntry(userName, processConfig.getProjectId(),
          processConfig.getId(), null, null,
          "UPDATE processConfig " + processConfig);

    } catch (Exception e) {
      handleException(e, "trying to update a processConfig");
    } finally {
      processService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @DELETE
  @Path("/processConfig/remove/{id}")
  @ApiOperation(value = "Remove processConfig", notes = "Removes the processConfig with the specified id")
  public void removeProcessConfig(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "ProcessConfig id, e.g. 3", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call DELETE (Process): /processConfig/remove/" + id
            + ", for user " + authToken);

    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName = authorizeProject(processService,
          projectId, securityService, authToken,
          "remove processConfig", UserRole.ADMINISTRATOR);
      processService.setLastModifiedBy(userName);

      // check to see if processConfig exists
      final ProcessConfig origProcessConfig =
          processService.getProcessConfig(id);
      if (origProcessConfig == null) {
        throw new Exception(
            "ProcessConfig " + id + " does not exist");
      }
      
      // Remove process config
      processService.removeProcessConfig(id);

      processService.addLogEntry(userName, projectId, id, null, null,
          "REMOVE processConfig " + id);

    } catch (Exception e) {
      handleException(e, "trying to remove a processConfig");
    } finally {
      processService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/processConfig/{id}")
  @ApiOperation(value = "Get processConfig for id", notes = "Gets the processConfig for the specified id", response = ProcessConfigJpa.class)
  public ProcessConfig getProcessConfig(
    @ApiParam(value = "Project internal id, e.g. 2", required = true) @QueryParam("id") Long projectId,
    @ApiParam(value = "ProcessConfig internal id, e.g. 2", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Process): /processConfig/" + id);

    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName = authorizeProject(processService,
          projectId, securityService, authToken,
          "getting the processConfig", UserRole.ADMINISTRATOR);
      processService.setLastModifiedBy(userName);
      
      return processService.getProcessConfig(id);
    } catch (Exception e) {
      handleException(e, "trying to get a processConfig");
      return null;
    } finally {
      processService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @POST
  @Path("/processConfig/all")
  @ApiOperation(value = "Get processConfigs", notes = "Get processConfigs", response = ProcessConfigListJpa.class)
  public ProcessConfigList findProcessConfigs(
    @ApiParam(value = "Project Id, e.g. 1", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Terminology, e.g. UMLS", required = false) @QueryParam("terminology") String terminology,
    @ApiParam(value = "Version, e.g. latest", required = false) @QueryParam("version") String version,
    @ApiParam(value = "The query string", required = false) @QueryParam("query") String query,
    @ApiParam(value = "The paging/sorting/filtering parameter", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call POST (Content): /processConfig/all " + query);

    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName = authorizeProject(processService,
          projectId, securityService, authToken,
          "finding process configs", UserRole.ADMINISTRATOR);
      processService.setLastModifiedBy(userName);
      
      final List<String> clauses = new ArrayList<>();
      if (!ConfigUtility.isEmpty(query)) {
        clauses.add(query);
      }
      if(!ConfigUtility.isEmpty(terminology)){
        clauses.add("terminology:" + terminology);
      }
      if(!ConfigUtility.isEmpty(version)){
        clauses.add("version:" + version);
      }      
      String fullQuery = ConfigUtility.composeQuery("AND", clauses);

      return processService.findProcessConfigs(projectId, fullQuery, pfs);

    } catch (Exception e) {
      handleException(e, "trying to find process configs");
      return null;
    } finally {
      processService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  public StringList getPredefinedProcesses(String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* see superclass */
  @Override
  public Long runPredefinedProcess(Long projectId, String id, Properties p,
    String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* see superclass */
  @Override
  public Long runProcessConfig(Long projectId, Long processConfigId,
    String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* see superclass */
  @Override
  public int lookupProgress(Long projectId, Long processExecutionId,
    String authToken) throws Exception {
    // TODO Auto-generated method stub
    return 0;
  }

  /* see superclass */
  @Override
  public boolean cancelProcessExecution(Long projectId, Long processExecutionId,
    String authToken) throws Exception {
    // TODO Auto-generated method stub
    return false;
  }

}
