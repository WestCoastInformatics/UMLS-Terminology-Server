/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.rest.impl;

import java.util.List;
import java.util.Properties;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.helpers.StringList;
import com.wci.umls.server.jpa.ProcessConfigJpa;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
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
  @Path("/processconfig/add")
  @ApiOperation(value = "Add new processConfig", notes = "Creates a new processConfig", response = ProcessConfigJpa.class)
  public ProcessConfig addProcessConfig(
    @ApiParam(value = "ProcessConfig, e.g. newProcessConfig", required = true) ProcessConfigJpa processConfig,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call PUT (Process): /processConfig/add for user " + authToken + ", " + processConfig);

    
    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName = authorizeApp(securityService, authToken,
          "add processConfig", UserRole.ADMINISTRATOR);
      processService.setLastModifiedBy(userName);

      // check to see if processConfig already exists
      for (final ProcessConfig p : processService.getProcessConfigs()) {
        if (p.getName().equals(processConfig.getName())
            && p.getDescription().equals(processConfig.getDescription())) {
          throw new LocalException(
              "A processConfig with this name and description already exists");
        }
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
  @Path("/processconfig/update")
  @ApiOperation(value = "Update processConfig", notes = "Updates the specified processConfig")
  public void updateProcessConfig(
    @ApiParam(value = "ProcessConfig, e.g. existingProcessConfig", required = true) ProcessConfigJpa processConfig,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call PUT (Process): /processConfig/update for user " + authToken + ", " + processConfig);

    // Create service and configure transaction scope
    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName = authorizeApp(securityService, authToken,
          "update processConfig", UserRole.ADMINISTRATOR);
      processService.setLastModifiedBy(userName);
      // check to see if processConfig already exists
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
  @Path("/processconfig/remove/{id}")
  @ApiOperation(value = "Remove processConfig", notes = "Removes the processConfig with the specified id")
  public void removeProcessConfig(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "ProcessConfig id, e.g. 3", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call DELETE (Process): /processConfig/remove/" + id + ", for user " + authToken);

    
    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName = authorizeApp(securityService, authToken,
          "remove processConfig", UserRole.ADMINISTRATOR);
      processService.setLastModifiedBy(userName);

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

  @Override
  public ProcessConfig getProcessConfig(Long projectId, Long id,
    String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<ProcessConfig> getProcessConfigs(Long projectId, String authToken)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ProcessConfig findProcessConfig(Long projectId, String terminology,
    String version, String query, PfsParameterJpa pfs, String authToken)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public StringList getPredefinedProcesses(String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Long runPredefinedProcess(Long projectId, String id, Properties p,
    String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Long runProcessConfig(Long projectId, Long processConfigId,
    String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int lookupProgress(Long projectId, Long processExecutionId,
    String authToken) throws Exception {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public boolean cancelProcessExecution(Long projectId, Long processExecutionId,
    String authToken) throws Exception {
    // TODO Auto-generated method stub
    return false;
  }

}
