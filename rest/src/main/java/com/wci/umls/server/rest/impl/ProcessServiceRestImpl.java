/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.rest.impl;

import java.util.ArrayList;
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

import com.wci.umls.server.AlgorithmConfig;
import com.wci.umls.server.ProcessConfig;
import com.wci.umls.server.Project;
import com.wci.umls.server.UserRole;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.helpers.ProcessConfigList;
import com.wci.umls.server.helpers.StringList;
import com.wci.umls.server.jpa.AlgorithmConfigJpa;
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
  @Path("/config/add")
  @ApiOperation(value = "Add new processConfig", notes = "Creates a new processConfig", response = ProcessConfigJpa.class)
  public ProcessConfig addProcessConfig(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "ProcessConfig, e.g. newProcessConfig", required = true) ProcessConfigJpa processConfig,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call PUT (Process): /config/add for user "
            + authToken + ", " + processConfig);

    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName =
          authorizeProject(processService, projectId, securityService,
              authToken, "adding a process config", UserRole.ADMINISTRATOR);
      processService.setLastModifiedBy(userName);

      // Make sure processConfig was passed in
      if (processConfig == null) {
        throw new LocalException("Error: trying to add null processConfig");
      }

      // Load project
      Project project = processService.getProject(projectId);
      project.setLastModifiedBy(userName);

      // Re-add project to processConfig (it does not make it intact through
      // XML)
      processConfig.setProject(project);

      // Verify that passed projectId matches ID of the processConfig's project
      verifyProject(processConfig, projectId);

      // check to see if processConfig already exists
      if (processService.findProcessConfigs(projectId,
          "name:\"" + processConfig.getName() + "\"", null).size() > 0) {
        throw new LocalException(
            "A processConfig with this name and description already exists for this project");
      }

      // Add processConfig
      final ProcessConfig newProcessConfig =
          processService.addProcessConfig(processConfig);

      processService.addLogEntry(userName, projectId, processConfig.getId(),
          null, null, "ADD processConfig - " + processConfig);

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
  @Path("/config/update")
  @ApiOperation(value = "Update processConfig", notes = "Updates the specified processConfig")
  public void updateProcessConfig(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "ProcessConfig, e.g. existingProcessConfig", required = true) ProcessConfigJpa processConfig,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call PUT (Process): /config/update for user "
            + authToken + ", " + processConfig);

    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName =
          authorizeProject(processService, projectId, securityService,
              authToken, "update processConfig", UserRole.ADMINISTRATOR);
      processService.setLastModifiedBy(userName);

      // Make sure processConfig was passed in
      if (processConfig == null) {
        throw new LocalException("Error: trying to update null processConfig");
      }

      // Load project
      Project project = processService.getProject(projectId);
      project.setLastModifiedBy(userName);

      // Re-add project to processConfig (it does not make it intact through
      // XML)
      processConfig.setProject(project);

      // Verify that passed projectId matches ID of the processConfig's project
      verifyProject(processConfig, projectId);

      // ensure the processConfig exists in the database
      final ProcessConfig origProcessConfig =
          processService.getProcessConfig(processConfig.getId());
      if (origProcessConfig == null) {
        throw new Exception(
            "ProcessConfig " + processConfig.getId() + " does not exist");
      }

      // Update processConfig
      processService.updateProcessConfig(processConfig);

      processService.addLogEntry(userName, projectId, processConfig.getId(),
          null, null, "UPDATE processConfig " + processConfig);

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
  @Path("/config/remove/{id}")
  @ApiOperation(value = "Remove processConfig", notes = "Removes the processConfig with the specified id")
  public void removeProcessConfig(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "ProcessConfig id, e.g. 3", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Cascade, e.g. true", required = true) @QueryParam("cascade") Boolean cascade,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call DELETE (Process): /config/remove/" + id
            + ", for user " + authToken);

    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName =
          authorizeProject(processService, projectId, securityService,
              authToken, "remove processConfig", UserRole.ADMINISTRATOR);
      processService.setLastModifiedBy(userName);
      // May have multiple transactions
      processService.setTransactionPerOperation(false);
      processService.beginTransaction();

      // Load processConfig object
      ProcessConfig processConfig = processService.getProcessConfig(id);

      // Make sure processConfig exists
      if (processConfig == null) {
        throw new Exception("ProcessConfig " + id + " does not exist");
      }

      // Verify that passed projectId matches ID of the processConfig's project
      verifyProject(processConfig, projectId);

      // If cascade if specified, Remove contained algorithmConfigs, if any, and
      // update ProcessConfig before removing it
      if (cascade && !processConfig.getSteps().isEmpty()) {
        for (AlgorithmConfig algorithmConfig : new ArrayList<AlgorithmConfig>(
            processConfig.getSteps())) {
          processConfig.getSteps().remove(algorithmConfig);
          processService.updateProcessConfig(processConfig);
          processService.removeAlgorithmConfig(algorithmConfig.getId());
        }
      }

      // Remove process config
      processService.removeProcessConfig(id);

      processService.addLogEntry(userName, projectId, id, null, null,
          "REMOVE processConfig " + id);

      processService.commit();
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
  @Path("/config/{id}")
  @ApiOperation(value = "Get processConfig for id", notes = "Gets the processConfig for the specified id", response = ProcessConfigJpa.class)
  public ProcessConfig getProcessConfig(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "ProcessConfig internal id, e.g. 2", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Process): /config/" + id);

    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName =
          authorizeProject(processService, projectId, securityService,
              authToken, "getting the processConfig", UserRole.AUTHOR);
      processService.setLastModifiedBy(userName);

      // Load processConfig object
      ProcessConfig processConfig = processService.getProcessConfig(id);

      if (processConfig == null) {
        return processConfig;
      }

      // Verify that passed projectId matches ID of the processConfig's project
      verifyProject(processConfig, projectId);

      return processConfig;
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
  @Path("/config")
  @ApiOperation(value = "Find processConfigs", notes = "Find processConfigs", response = ProcessConfigListJpa.class)
  public ProcessConfigList findProcessConfigs(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "The query string", required = false) @QueryParam("query") String query,
    @ApiParam(value = "The paging/sorting/filtering parameter", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call POST (Content): /config " + query);

    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName =
          authorizeProject(processService, projectId, securityService,
              authToken, "finding process configs", UserRole.AUTHOR);
      processService.setLastModifiedBy(userName);

      return processService.findProcessConfigs(projectId, query, pfs);

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
  @PUT
  @Path("/config/algo/add")
  @ApiOperation(value = "Add new algorithm config", notes = "Creates a new algorithm config", response = AlgorithmConfigJpa.class)
  public AlgorithmConfig addAlgorithmConfig(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "AlgorithmConfig, e.g. newAlgorithmConfig", required = true) AlgorithmConfigJpa algorithmConfig,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call PUT (Process): /config/algo/add for user "
            + authToken + ", " + algorithmConfig);

    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName =
          authorizeProject(processService, projectId, securityService,
              authToken, "adding a process config", UserRole.ADMINISTRATOR);
      processService.setLastModifiedBy(userName);

      ProcessConfigJpa processConfig = null;
      // Load processConfig
      if (algorithmConfig.getProcess() != null) {
        processConfig = (ProcessConfigJpa) processService
            .getProcessConfig(algorithmConfig.getProcess().getId());
      }

      // Re-add processConfig to algorithmConfig (it does not make it intact
      // through XML)
      algorithmConfig.setProcess(processConfig);

      // Load project
      Project project = processService.getProject(projectId);
      project.setLastModifiedBy(userName);

      // Re-add project to algorithmConfig (it does not make it intact through
      // XML)
      algorithmConfig.setProject(project);

      // Verify that passed projectId matches ID of the algorithmConfig's
      // project
      verifyProject(algorithmConfig, projectId);

      // Add algorithmConfig
      final AlgorithmConfig newAlgorithmConfig =
          processService.addAlgorithmConfig(algorithmConfig);

      // If new algorithm config has an associated processConfig,
      // add the algorithm to it and update
      if (processConfig != null) {
        // Add algorithmConfig to processConfig
        processConfig.getSteps().add(newAlgorithmConfig);

        // update the processConfig
        processService.updateProcessConfig(processConfig);
      }

      processService.addLogEntry(userName, projectId, algorithmConfig.getId(),
          null, null, "ADD algorithmConfig - " + algorithmConfig);

      return newAlgorithmConfig;
    } catch (Exception e) {
      handleException(e, "trying to add an algorithmConfig");
      return null;
    } finally {
      processService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @POST
  @Path("/config/algo/update")
  @ApiOperation(value = "Update algorithm config", notes = "Updates the specified algorithm config")
  public void updateAlgorithmConfig(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "AlgorithmConfig, e.g. existingAlgorithmConfig", required = true) AlgorithmConfigJpa algorithmConfig,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call PUT (Process): /config/algo/update for user "
            + authToken + ", " + algorithmConfig);

    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName =
          authorizeProject(processService, projectId, securityService,
              authToken, "update algorithmConfig", UserRole.ADMINISTRATOR);
      processService.setLastModifiedBy(userName);

      // Load processConfig
      ProcessConfigJpa processConfig = (ProcessConfigJpa) processService
          .getProcessConfig(algorithmConfig.getProcess().getId());

      // Re-add processConfig to algorithmConfig (it does not make it intact
      // through XML)
      algorithmConfig.setProcess(processConfig);

      // Load project
      Project project = processService.getProject(projectId);
      project.setLastModifiedBy(userName);

      // Re-add project to algorithmConfig (it does not make it intact through
      // XML)
      algorithmConfig.setProject(project);

      // Verify that passed projectId matches ID of the algorithmConfig's
      // project
      verifyProject(algorithmConfig, projectId);

      // ensure algorithmConfig exists
      final AlgorithmConfig origAlgorithmConfig =
          processService.getAlgorithmConfig(algorithmConfig.getId());
      if (origAlgorithmConfig == null) {
        throw new Exception(
            "AlgorithmConfig " + algorithmConfig.getId() + " does not exist");
      }

      // Update algorithmConfig
      processService.updateAlgorithmConfig(algorithmConfig);

      processService.addLogEntry(userName, projectId, algorithmConfig.getId(),
          null, null, "UPDATE algorithmConfig " + algorithmConfig);

    } catch (Exception e) {
      handleException(e, "trying to update an algorithmConfig");
    } finally {
      processService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @DELETE
  @Path("/config/algo/remove/{id}")
  @ApiOperation(value = "Remove algorithm config", notes = "Removes the algorithm config with the specified id")
  public void removeAlgorithmConfig(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "AlgorithmConfig id, e.g. 3", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call DELETE (Process): /config/algo/remove/" + id
            + ", for user " + authToken);

    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName =
          authorizeProject(processService, projectId, securityService,
              authToken, "remove algorithmConfig", UserRole.ADMINISTRATOR);
      processService.setLastModifiedBy(userName);

      // Load algorithmConfig object
      AlgorithmConfig algorithmConfig = processService.getAlgorithmConfig(id);

      // ensure algorithmConfig exists
      if (algorithmConfig == null) {
        throw new Exception("AlgorithmConfig " + id + " does not exist");
      }

      // Verify that passed projectId matches ID of the algorithmConfig's
      // project
      verifyProject(algorithmConfig, projectId);

      // If the algorithm config has an associated processConfig,
      // remove the algorithm from it and update
      ProcessConfig processConfig = algorithmConfig.getProcess();

      if (processConfig != null) {
        // Remove algorithmConfig from processConfig
        processConfig.getSteps().add(algorithmConfig);

        // update the processConfig
        processService.updateProcessConfig(processConfig);
      }

      // Remove algorithm config
      processService.removeAlgorithmConfig(id);

      processService.addLogEntry(userName, projectId, id, null, null,
          "REMOVE algorithmConfig " + id);

    } catch (Exception e) {
      handleException(e, "trying to remove an algorithmConfig");
    } finally {
      processService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/config/algo/{id}")
  @ApiOperation(value = "Get algorithm config for id", notes = "Gets the algorithm config for the specified id", response = AlgorithmConfigJpa.class)
  public AlgorithmConfig getAlgorithmConfig(
    @ApiParam(value = "Project internal id, e.g. 2", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "AlgorithmConfig internal id, e.g. 2", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Process): /config/algo/" + id);

    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName =
          authorizeProject(processService, projectId, securityService,
              authToken, "getting the algorithmConfig", UserRole.AUTHOR);
      processService.setLastModifiedBy(userName);

      // Load algorithmConfig object
      AlgorithmConfig algorithmConfig = processService.getAlgorithmConfig(id);

      if (algorithmConfig == null) {
        return algorithmConfig;
      }

      // Verify that passed projectId matches ID of the algorithmConfig's
      // project
      verifyProject(algorithmConfig, projectId);

      return algorithmConfig;
    } catch (Exception e) {
      handleException(e, "trying to get a algorithmConfig");
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
