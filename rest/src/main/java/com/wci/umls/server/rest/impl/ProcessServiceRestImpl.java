/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.rest.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
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

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.wci.umls.server.AlgorithmConfig;
import com.wci.umls.server.AlgorithmExecution;
import com.wci.umls.server.AlgorithmParameter;
import com.wci.umls.server.ProcessConfig;
import com.wci.umls.server.ProcessExecution;
import com.wci.umls.server.Project;
import com.wci.umls.server.UserRole;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.algo.Algorithm;
import com.wci.umls.server.helpers.CancelException;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.KeyValuePairList;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.helpers.ProcessConfigList;
import com.wci.umls.server.helpers.ProcessExecutionList;
import com.wci.umls.server.jpa.AlgorithmConfigJpa;
import com.wci.umls.server.jpa.AlgorithmExecutionJpa;
import com.wci.umls.server.jpa.ProcessConfigJpa;
import com.wci.umls.server.jpa.ProcessExecutionJpa;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.helpers.ProcessConfigListJpa;
import com.wci.umls.server.jpa.helpers.ProcessExecutionListJpa;
import com.wci.umls.server.jpa.services.ProcessServiceJpa;
import com.wci.umls.server.jpa.services.SecurityServiceJpa;
import com.wci.umls.server.jpa.services.rest.ProcessServiceRest;
import com.wci.umls.server.services.ProcessService;
import com.wci.umls.server.services.SecurityService;
import com.wci.umls.server.services.helpers.ProgressEvent;
import com.wci.umls.server.services.helpers.ProgressListener;

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

  /** The lookup process execution progress map. */
  static Map<Long, Integer> lookupPeProgressMap = new HashMap<Long, Integer>();

  /** The lookup algorithm execution progress map. */
  static Map<Long, Integer> lookupAeProgressMap = new HashMap<Long, Integer>();

  /** The map of which algorithm a process is currently running. */
  static Map<Long, Algorithm> processAlgorithmMap =
      new HashMap<Long, Algorithm>();

  /**
   * Instantiates an empty {@link ProcessServiceRestImpl}.
   *
   * @throws Exception the exception
   */
  public ProcessServiceRestImpl() throws Exception {
    securityService = new SecurityServiceJpa();
  }

  /**
   * Adds the process config.
   *
   * @param projectId the project id
   * @param processConfig the process config
   * @param authToken the auth token
   * @return the process config
   * @throws Exception the exception
   */
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
        .info("RESTful call (Process): /config/add for user " + authToken + ", "
            + processConfig);

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
      final Project project = processService.getProject(projectId);
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

  /**
   * Update process config.
   *
   * @param projectId the project id
   * @param processConfig the process config
   * @param authToken the auth token
   * @throws Exception the exception
   */
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
        .info("RESTful call (Process): /config/update for user " + authToken
            + ", " + processConfig);

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
      final Project project = processService.getProject(projectId);
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

  /**
   * Removes the process config.
   *
   * @param projectId the project id
   * @param id the id
   * @param cascade the cascade
   * @param authToken the auth token
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  @DELETE
  @Path("/config/{id}/remove")
  @ApiOperation(value = "Remove processConfig", notes = "Removes the processConfig with the specified id")
  public void removeProcessConfig(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "ProcessConfig id, e.g. 3", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Cascade, e.g. true", required = true) @QueryParam("cascade") Boolean cascade,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful call (Process): /config/" + id
        + "/remove, for user " + authToken);

    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName =
          authorizeProject(processService, projectId, securityService,
              authToken, "remove processConfig", UserRole.ADMINISTRATOR);
      processService.setLastModifiedBy(userName);

      // Load processConfig object
      final ProcessConfig processConfig = processService.getProcessConfig(id);

      // Make sure processConfig exists
      if (processConfig == null) {
        throw new Exception("ProcessConfig " + id + " does not exist");
      }

      // Verify that passed projectId matches ID of the processConfig's project
      verifyProject(processConfig, projectId);

      // If cascade if specified, Remove contained algorithmConfigs, if any, and
      // update ProcessConfig before removing it
      if (cascade && !processConfig.getSteps().isEmpty()) {
        for (final AlgorithmConfig algorithmConfig : new ArrayList<AlgorithmConfig>(
            processConfig.getSteps())) {
          // BAC: I dont' think this is necessary on a remove
          // processConfig.getSteps().remove(algorithmConfig);
          // processService.updateProcessConfig(processConfig);
          processService.removeAlgorithmConfig(algorithmConfig.getId());
        }
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

  /**
   * Returns the process config.
   *
   * @param projectId the project id
   * @param id the id
   * @param authToken the auth token
   * @return the process config
   * @throws Exception the exception
   */
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
    Logger.getLogger(getClass()).info(
        "RESTful call (Process): /config/" + id + ", for user " + authToken);

    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName =
          authorizeProject(processService, projectId, securityService,
              authToken, "getting the processConfig", UserRole.AUTHOR);
      processService.setLastModifiedBy(userName);

      // Load processConfig object
      final ProcessConfig processConfig = processService.getProcessConfig(id);

      if (processConfig == null) {
        return processConfig;
      }

      // Verify that passed projectId matches ID of the processConfig's project
      verifyProject(processConfig, projectId);

      // For each of the process' algorithms, populate the parameters based on
      // its properties' values.
      for (final AlgorithmConfig algorithmConfig : processConfig.getSteps()) {
        Algorithm instance = processService
            .getAlgorithmInstance(algorithmConfig.getAlgorithmKey());
        algorithmConfig.setParameters(instance.getParameters());
        for (final AlgorithmParameter param : algorithmConfig.getParameters()) {
          // Populate both Value and Values (UI will determine which is required
          // for each algorithm type)
          if (algorithmConfig.getProperties()
              .get(param.getFieldName()) != null) {
            param.setValue(
                algorithmConfig.getProperties().get(param.getFieldName()));
            param.setValues(new ArrayList<String>(Arrays.asList(algorithmConfig
                .getProperties().get(param.getFieldName()).split(","))));
          }
        }
      }

      return processConfig;
    } catch (Exception e) {
      handleException(e, "trying to get a processConfig");
      return null;
    } finally {
      processService.close();
      securityService.close();
    }
  }

  /**
   * Find process configs.
   *
   * @param projectId the project id
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the process config list
   * @throws Exception the exception
   */
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
    Logger.getLogger(getClass()).info(
        "RESTful call (Process): /config " + query + ", for user " + authToken);

    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName =
          authorizeProject(processService, projectId, securityService,
              authToken, "finding process configs", UserRole.AUTHOR);
      processService.setLastModifiedBy(userName);

      final ProcessConfigList processConfigs =
          processService.findProcessConfigs(projectId, query, pfs);

      // Set steps to empty list for all returned processConfigs
      for (final ProcessConfig processConfig : processConfigs.getObjects()) {
        processConfig.setSteps(new ArrayList<AlgorithmConfig>());
      }

      return processConfigs;

    } catch (Exception e) {
      handleException(e, "trying to find process configs");
      return null;
    } finally {
      processService.close();
      securityService.close();
    }
  }

  /**
   * Returns the process execution.
   *
   * @param projectId the project id
   * @param id the id
   * @param authToken the auth token
   * @return the process execution
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  @GET
  @Path("/execution/{id}")
  @ApiOperation(value = "Get processExecution for id", notes = "Gets the processExecution for the specified id", response = ProcessExecutionJpa.class)
  public ProcessExecution getProcessExecution(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "ProcessExecution internal id, e.g. 2", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call (Process): /execution/" + id + ", for user " + authToken);

    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName =
          authorizeProject(processService, projectId, securityService,
              authToken, "getting the processExecution", UserRole.AUTHOR);
      processService.setLastModifiedBy(userName);

      // Load processExecution object
      final ProcessExecution processExecution =
          processService.getProcessExecution(id);

      if (processExecution == null) {
        return processExecution;
      }

      // Verify that passed projectId matches ID of the processExecution's
      // project
      verifyProject(processExecution, projectId);

      // For each of the process' algorithms, populate the parameters based on
      // its properties' values.
      for (final AlgorithmExecution algorithmExecution : processExecution
          .getSteps()) {
        final Algorithm instance = processService
            .getAlgorithmInstance(algorithmExecution.getAlgorithmKey());
        algorithmExecution.setParameters(instance.getParameters());
        for (final AlgorithmParameter param : algorithmExecution
            .getParameters()) {
          // Populate both Value and Values (UI will determine which is required
          // for each algorithm type)
          if (algorithmExecution.getProperties()
              .get(param.getFieldName()) != null) {
            param.setValue(
                algorithmExecution.getProperties().get(param.getFieldName()));
            param.setValues(
                new ArrayList<String>(Arrays.asList(algorithmExecution
                    .getProperties().get(param.getFieldName()).split(","))));
          }
        }
      }

      return processExecution;
    } catch (Exception e) {
      handleException(e, "trying to get a processExecution");
      return null;
    } finally {
      processService.close();
      securityService.close();
    }
  }

  /**
   * Find process executions.
   *
   * @param projectId the project id
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the process execution list
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  @POST
  @Path("/execution")
  @ApiOperation(value = "Find processExecutions", notes = "Find processExecutions", response = ProcessExecutionListJpa.class)
  public ProcessExecutionList findProcessExecutions(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "The query string", required = false) @QueryParam("query") String query,
    @ApiParam(value = "The paging/sorting/filtering parameter", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful call (Process): /execution "
        + query + ", for user " + authToken);

    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName =
          authorizeProject(processService, projectId, securityService,
              authToken, "finding process executions", UserRole.AUTHOR);
      processService.setLastModifiedBy(userName);

      final ProcessExecutionList processExecutions =
          processService.findProcessExecutions(projectId, query, pfs);

      // Set steps to empty list for all returned processExecutions
      for (final ProcessExecution processExecution : processExecutions
          .getObjects()) {
        processExecution.setSteps(new ArrayList<AlgorithmExecution>());
      }

      return processExecutions;

    } catch (Exception e) {
      handleException(e, "trying to find process executions");
      return null;
    } finally {
      processService.close();
      securityService.close();
    }
  }

  /**
   * Find process executions.
   *
   * @param projectId the project id
   * @param authToken the auth token
   * @return the process execution list
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  @GET
  @Path("/executing")
  @ApiOperation(value = "Find currently executing processes", notes = "Find currently executing processes", response = ProcessExecutionListJpa.class)
  public ProcessExecutionList findCurrentlyExecutingProcesses(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Process): /executing, for user " + authToken);

    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName =
          authorizeProject(processService, projectId, securityService,
              authToken, "finding process executions", UserRole.AUTHOR);
      processService.setLastModifiedBy(userName);

      final ProcessExecutionList processExecutions =
          processService.findProcessExecutions(projectId,
              "NOT failDate:[* TO *] AND NOT finishDate:[* TO *]", null);

      // Only keep process Executions if they in the currently
      // executing processes progress map and have a progress of less than 100
      for (final ProcessExecution processExecution : new ArrayList<ProcessExecution>(
          processExecutions.getObjects())) {
        if (!lookupPeProgressMap.containsKey(processExecution.getId())
            || lookupPeProgressMap.get(processExecution.getId()) == 100) {
          processExecutions.getObjects().remove(processExecution);
        }
      }
      return processExecutions;

    } catch (Exception e) {
      handleException(e, "trying to find process executions");
      return null;
    } finally {
      processService.close();
      securityService.close();
    }
  }

  /**
   * Removes the process execution.
   *
   * @param projectId the project id
   * @param id the id
   * @param cascade the cascade
   * @param authToken the auth token
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  @DELETE
  @Path("/execution/{id}/remove")
  @ApiOperation(value = "Remove processExecution", notes = "Removes the processExecution with the specified id")
  public void removeProcessExecution(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "ProcessExecution id, e.g. 3", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Cascade, e.g. true", required = true) @QueryParam("cascade") Boolean cascade,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful call (Process): /execution/" + id
        + "/remove, for user " + authToken);

    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName =
          authorizeProject(processService, projectId, securityService,
              authToken, "remove processExecution", UserRole.ADMINISTRATOR);
      processService.setLastModifiedBy(userName);

      // Load processExecution object
      final ProcessExecution processExecution =
          processService.getProcessExecution(id);

      // Make sure processExecution exists
      if (processExecution == null) {
        throw new Exception("ProcessExecution " + id + " does not exist");
      }

      // Verify that passed projectId matches ID of the processExecution's
      // project
      verifyProject(processExecution, projectId);

      // If cascade if specified, Remove contained algorithmExecutions, if any,
      // and
      // update ProcessExecution before removing it
      if (cascade && !processExecution.getSteps().isEmpty()) {
        for (final AlgorithmExecution algorithmExecution : new ArrayList<AlgorithmExecution>(
            processExecution.getSteps())) {
          processExecution.getSteps().remove(algorithmExecution);
          processService.updateProcessExecution(processExecution);
          processService.removeAlgorithmExecution(algorithmExecution.getId());
        }
      }

      // Remove process execution
      processService.removeProcessExecution(id);

      processService.addLogEntry(userName, projectId, id, null, null,
          "REMOVE processExecution " + id);

    } catch (Exception e) {
      handleException(e, "trying to remove a processExecution");
    } finally {
      processService.close();
      securityService.close();
    }
  }

  /**
   * Adds the algorithm config.
   *
   * @param projectId the project id
   * @param algorithmConfig the algorithm config
   * @param authToken the auth token
   * @return the algorithm config
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  @PUT
  @Path("/config/algo/add")
  @ApiOperation(value = "Add new algorithm config", notes = "Creates a new algorithm config", response = AlgorithmConfigJpa.class)
  public AlgorithmConfig addAlgorithmConfig(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Process id, e.g. 1", required = true) @QueryParam("processId") Long processId,
    @ApiParam(value = "AlgorithmConfig, e.g. newAlgorithmConfig", required = true) AlgorithmConfigJpa algorithmConfig,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Process): /config/algo/add for user " + processId
            + ", " + authToken + ", " + algorithmConfig);

    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName =
          authorizeProject(processService, projectId, securityService,
              authToken, "adding a process config", UserRole.ADMINISTRATOR);
      processService.setLastModifiedBy(userName);

      final ProcessConfigJpa processConfig =
          (ProcessConfigJpa) processService.getProcessConfig(processId);

      // Re-add processConfig to algorithmConfig (it does not make it intact
      // through XML)
      algorithmConfig.setProcess(processConfig);

      // Load project
      final Project project = processService.getProject(projectId);
      project.setLastModifiedBy(userName);

      // Re-add project to algorithmConfig (it does not make it intact through
      // XML)
      algorithmConfig.setProject(project);

      // Verify that passed projectId matches ID of the algorithmConfig's
      // project
      verifyProject(algorithmConfig, projectId);

      // Populate the algorithm's properties based on its parameters' values.
      for (final AlgorithmParameter param : algorithmConfig.getParameters()) {
        // Note: map either Value OR Values (comma-delimited)
        if (!param.getValues().isEmpty()) {
          algorithmConfig.getProperties().put(param.getFieldName(),
              StringUtils.join(param.getValues(), ','));
        } else if (!ConfigUtility.isEmpty(param.getValue())) {
          algorithmConfig.getProperties().put(param.getFieldName(),
              param.getValue());
        }
      }

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

  /**
   * Update algorithm config.
   *
   * @param projectId the project id
   * @param algorithmConfig the algorithm config
   * @param authToken the auth token
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  @POST
  @Path("/config/algo/update")
  @ApiOperation(value = "Update algorithm config", notes = "Updates the specified algorithm config")
  public void updateAlgorithmConfig(
    @ApiParam(value = "Project id, "
        + "e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "AlgorithmConfig, e.g. existingAlgorithmConfig", required = true) AlgorithmConfigJpa algorithmConfig,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Process): /config/algo/update for user "
            + authToken + ", " + algorithmConfig);

    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName =
          authorizeProject(processService, projectId, securityService,
              authToken, "update algorithmConfig", UserRole.ADMINISTRATOR);
      processService.setLastModifiedBy(userName);

      // Load processConfig
      final ProcessConfigJpa processConfig = (ProcessConfigJpa) processService
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

      // Populate the algorithm's properties based on its parameters' values.
      for (final AlgorithmParameter param : algorithmConfig.getParameters()) {
        algorithmConfig.getProperties().put(param.getFieldName(),
            param.getValue());
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

  /**
   * Removes the algorithm config.
   *
   * @param projectId the project id
   * @param id the id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  @DELETE
  @Path("/config/algo/{id}/remove")
  @ApiOperation(value = "Remove algorithm config", notes = "Removes the algorithm config with the specified id")
  public void removeAlgorithmConfig(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "AlgorithmConfig id, e.g. 3", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful call (Process): /config/algo/"
        + id + "/remove, for user " + authToken);

    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName =
          authorizeProject(processService, projectId, securityService,
              authToken, "remove algorithmConfig", UserRole.ADMINISTRATOR);
      processService.setLastModifiedBy(userName);

      // Load algorithmConfig object
      final AlgorithmConfig algorithmConfig =
          processService.getAlgorithmConfig(id);

      // ensure algorithmConfig exists
      if (algorithmConfig == null) {
        throw new Exception("AlgorithmConfig " + id + " does not exist");
      }

      // Verify that passed projectId matches ID of the algorithmConfig's
      // project
      verifyProject(algorithmConfig, projectId);

      // If the algorithm config has an associated processConfig,
      // remove the algorithm from it and update
      final ProcessConfig processConfig = algorithmConfig.getProcess();
      if (processConfig != null) {
        // Remove algorithmConfig from processConfig
        processConfig.getSteps().remove(algorithmConfig);

      } else {
        throw new Exception("Process config is unexpectedly null");
      }

      // Remove algorithm config
      processService.removeAlgorithmConfig(id);
      // update the processConfig
      processService.updateProcessConfig(processConfig);

      processService.addLogEntry(userName, projectId, id, null, null,
          "REMOVE algorithmConfig " + id);

    } catch (Exception e) {
      handleException(e, "trying to remove an algorithmConfig");
    } finally {
      processService.close();
      securityService.close();
    }
  }

  /**
   * Returns the algorithm config.
   *
   * @param projectId the project id
   * @param id the id
   * @param authToken the auth token
   * @return the algorithm config
   * @throws Exception the exception
   */
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
    Logger.getLogger(getClass()).info("RESTful call (Process): /config/algo/"
        + id + ", for user " + authToken);

    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName =
          authorizeProject(processService, projectId, securityService,
              authToken, "getting the algorithmConfig", UserRole.AUTHOR);
      processService.setLastModifiedBy(userName);

      // Load algorithmConfig object
      final AlgorithmConfig algorithmConfig =
          processService.getAlgorithmConfig(id);

      if (algorithmConfig == null) {
        return algorithmConfig;
      }

      // Verify that passed projectId matches ID of the algorithmConfig's
      // project
      verifyProject(algorithmConfig, projectId);

      // Populate the parameters based on its properties' values.
      final Algorithm instance = processService
          .getAlgorithmInstance(algorithmConfig.getAlgorithmKey());
      algorithmConfig.setParameters(instance.getParameters());
      for (final AlgorithmParameter param : algorithmConfig.getParameters()) {
        // Populate both Value and Values (UI will determine which is required
        // for each algorithm type)
        if (algorithmConfig.getProperties().get(param.getFieldName()) != null) {
          param.setValue(
              algorithmConfig.getProperties().get(param.getFieldName()));
          param.setValues(new ArrayList<String>(Arrays.asList(algorithmConfig
              .getProperties().get(param.getFieldName()).split(","))));
        }
      }

      return algorithmConfig;
    } catch (Exception e) {
      handleException(e, "trying to get a algorithmConfig");
      return null;
    } finally {
      processService.close();
      securityService.close();
    }
  }

  /**
   * Returns the algorithm config.
   *
   * @param projectId the project id
   * @param key the key
   * @param authToken the auth token
   * @return the algorithm config
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  @GET
  @Path("/config/algo/key/{key}")
  @ApiOperation(value = "Get algorithm config for key", notes = "Gets the algorithm config for the specified key", response = AlgorithmConfigJpa.class)
  public AlgorithmConfig getAlgorithmConfigForKey(
    @ApiParam(value = "Project internal id, e.g. 2", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "AlgorithmConfig key, e.g. MATRIXINIT", required = true) @PathParam("key") String key,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful call (Process): /config/algo/"
        + key + ", for user " + authToken);

    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName =
          authorizeProject(processService, projectId, securityService,
              authToken, "getting the algorithmConfig", UserRole.AUTHOR);
      processService.setLastModifiedBy(userName);

      // Load algorithmConfig object
      final AlgorithmConfig algorithmConfig =
          (AlgorithmConfig) processService.getAlgorithmInstance(key);

      if (algorithmConfig == null) {
        return algorithmConfig;
      }

      // Verify that passed projectId matches ID of the algorithmConfig's
      // project
      verifyProject(algorithmConfig, projectId);

      // Populate the parameters based on its properties' values.
      final Algorithm instance = processService
          .getAlgorithmInstance(algorithmConfig.getAlgorithmKey());
      algorithmConfig.setParameters(instance.getParameters());
      for (final AlgorithmParameter param : algorithmConfig.getParameters()) {
        // Populate both Value and Values (UI will determine which is required
        // for each algorithm type)
        if (algorithmConfig.getProperties().get(param.getFieldName()) != null) {
          param.setValue(
              algorithmConfig.getProperties().get(param.getFieldName()));
          param.setValues(new ArrayList<String>(Arrays.asList(algorithmConfig
              .getProperties().get(param.getFieldName()).split(","))));
        }
      }

      return algorithmConfig;
    } catch (Exception e) {
      handleException(e, "trying to get an algorithmConfig");
      return null;
    } finally {
      processService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/algo/insertion")
  @ApiOperation(value = "Get all insertion algorithms", notes = "Gets the insertion algorithms", response = KeyValuePairList.class)
  public KeyValuePairList getInsertionAlgorithms(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Process): /algo/insertion, for user " + authToken);

    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName =
          authorizeProject(processService, projectId, securityService,
              authToken, "getting the insertion algorithms", UserRole.AUTHOR);
      processService.setLastModifiedBy(userName);

      return processService.getInsertionAlgorithms();
    } catch (Exception e) {
      handleException(e, "trying to get the insertion algorithms");
      return null;
    } finally {
      processService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/algo/maintenance")
  @ApiOperation(value = "Get all maintenance algorithms", notes = "Gets the maintenance algorithms", response = KeyValuePairList.class)
  public KeyValuePairList getMaintenanceAlgorithms(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call (Process): /algo/maintenance, for user " + authToken);

    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName =
          authorizeProject(processService, projectId, securityService,
              authToken, "getting the maintenance algorithms", UserRole.AUTHOR);
      processService.setLastModifiedBy(userName);

      return processService.getMaintenanceAlgorithms();
    } catch (Exception e) {
      handleException(e, "trying to get the maintenance algorithms");
      return null;
    } finally {
      processService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/algo/release")
  @ApiOperation(value = "Get all release algorithms", notes = "Gets the release algorithms", response = KeyValuePairList.class)
  public KeyValuePairList getReleaseAlgorithms(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Process): /algo/release, for user " + authToken);

    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName =
          authorizeProject(processService, projectId, securityService,
              authToken, "getting the release algorithms", UserRole.AUTHOR);
      processService.setLastModifiedBy(userName);

      return processService.getReleaseAlgorithms();
    } catch (Exception e) {
      handleException(e, "trying to get the release algorithms");
      return null;
    } finally {
      processService.close();
      securityService.close();
    }
  }

  /**
   * Execute process.
   *
   * @param projectId the project id
   * @param id the id
   * @param background the background
   * @param authToken the auth token
   * @return the long
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  @GET
  @Path("/config/{id}/execute")
  @Produces("text/plain")
  @ApiOperation(value = "Execute a process configuration", notes = "Execute the specified process configuration")
  public Long executeProcess(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Process Config id, e.g. 3", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Background, e.g. true", required = true) @QueryParam("background") Boolean background,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful call (Process): /config/" + id
        + "/execute, for user " + authToken);

    final ProcessService processService = new ProcessServiceJpa();

    Long executionId = null;

    try {
      final String userName =
          authorizeProject(processService, projectId, securityService,
              authToken, "execute processConfig", UserRole.ADMINISTRATOR);
      processService.setLastModifiedBy(userName);

      // Load processConfig object
      final ProcessConfig processConfig = processService.getProcessConfig(id);

      // Make sure processConfig exists
      if (processConfig == null) {
        throw new Exception("ProcessConfig " + id + " does not exist");
      }

      // Verify that passed projectId matches ID of the processConfig's project
      verifyProject(processConfig, projectId);

      // Make sure this processConfig is not already running
      for (final ProcessExecution exec : findCurrentlyExecutingProcesses(
          projectId, authToken).getObjects()) {
        if (exec.getProcessConfigId().equals(processConfig.getId())) {
          throw new Exception(
              "There is already a currently running execution of process "
                  + processConfig.getId());
        }
      }

      // Create and set up a new process Execution
      final ProcessExecution execution = new ProcessExecutionJpa(processConfig);
      execution.setStartDate(new Date());
      execution.setWorkId(UUID.randomUUID().toString());
      execution.setSteps(new ArrayList<>());
      final ProcessExecution processExecution =
          processService.addProcessExecution(execution);
      executionId = processExecution.getId();

      // Create a thread and run the process
      runProcessAsThread(projectId, processConfig.getId(),
          processExecution.getId(), userName, background, false);

      // Always return the execution id
      return executionId;

    } catch (

    Exception e) {
      handleException(e, "trying to execute a process");
    } finally {
      processService.close();
      securityService.close();
    }
    return executionId;
  }

  /* see superclass */
  @Override
  @GET
  @Path("/execution/{id}/restart")
  @ApiOperation(value = "Execute a process configuration", notes = "Execute the specified process configuration")
  public void restartProcess(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Process Execution id, e.g. 3", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Background, e.g. true", required = true) @QueryParam("background") Boolean background,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Process): /execution/" + id + "/restart?projectId="
            + projectId
            + ((background != null && background) ? "&background=true" : "")
            + " for user " + authToken);

    final ProcessService processService = new ProcessServiceJpa();

    try {
      final String userName =
          authorizeProject(processService, projectId, securityService,
              authToken, "restart processExecution", UserRole.ADMINISTRATOR);
      processService.setLastModifiedBy(userName);

      // Load processExecution object
      final ProcessExecution processExecution =
          processService.getProcessExecution(id);

      // Make sure processExecution exists
      if (processExecution == null) {
        throw new Exception("ProcessExecution " + id + " does not exist");
      }

      // Make sure the processExecution isn't already running
      for (final ProcessExecution exec : findCurrentlyExecutingProcesses(
          projectId, authToken).getObjects()) {
        if (exec.getId().equals(processExecution.getId())) {
          throw new Exception("Process execution " + processExecution.getId()
              + " is already currently running");
        }
      }

      // Load the processExecution's config
      final ProcessConfig processConfig = processService
          .getProcessConfig(processExecution.getProcessConfigId());

      // Verify that passed projectId matches ID of the processConfig's project
      verifyProject(processConfig, projectId);

      // Create a thread and run the process
      runProcessAsThread(projectId, processConfig.getId(),
          processExecution.getId(), userName, background, true);

    } catch (

    Exception e) {
      handleException(e, "trying to restart a process execution");
    } finally {
      processService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/execution/{id}/cancel")
  @ApiOperation(value = "Cancel a running process execution", notes = "Execute the specified process configuration")
  public void cancelProcess(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Process Execution id, e.g. 3", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful call (Process): /execution/" + id
        + "/cancel, for user " + authToken);

    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName =
          authorizeProject(processService, projectId, securityService,
              authToken, "cancel process execution", UserRole.ADMINISTRATOR);
      processService.setLastModifiedBy(userName);

      final ProcessExecution processExecution =
          processService.getProcessExecution(id);
      if (processExecution == null) {
        throw new Exception("Process execution does not exist for " + id);
      }
      // Verify the project
      verifyProject(processExecution, projectId);

      // Make sure this process execution is running
      boolean processRunning = false;
      for (final ProcessExecution exec : findCurrentlyExecutingProcesses(
          projectId, authToken).getObjects()) {
        if (exec.getId().equals(id)) {
          processRunning = true;
          break;
        }
      }

      if (!processRunning) {
        throw new Exception("Error canceling process Execution " + id
            + ": not currently running.");
      }

      // Find the algorithm and call cancel on it
      if (processAlgorithmMap.containsKey(id)) {
        // this will throw a CancelException which will clean up all the maps
        // and mark everything as cancelled
        processAlgorithmMap.get(id).cancel();
      }

    } catch (Exception e) {
      handleException(e, "trying to cancel a process execution");
    } finally {
      processService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @GET
  @Path("/{id}/progress")
  @Produces("text/plain")
  @ApiOperation(value = "Find progress of specified executing process", notes = "Find progress of specified executing process", response = Integer.class)
  @Override
  public Integer getProcessProgress(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Process execution internal id, e.g. 2", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful call POST (Process): /" + id
        + "/progress, for user " + authToken);

    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName =
          authorizeProject(processService, projectId, securityService,
              authToken, "finding process progress", UserRole.AUTHOR);
      processService.setLastModifiedBy(userName);

      final ProcessExecution processExecution =
          processService.getProcessExecution(id);
      // If process has already completed successfully, return 100
      if (processExecution.getFinishDate() != null
          && processExecution.getFailDate() == null) {
        return 100;
      }

      if (lookupPeProgressMap.containsKey(id)) {
        return lookupPeProgressMap.get(id);
      }
      // Return -1 if id not contained in progress map.
      else {
        return -1;
      }

    } catch (Exception e) {
      handleException(e, "trying to get progress");
      return null;
    } finally {
      processService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @GET
  @Path("algo/{id}/progress")
  @Produces("text/plain")
  @ApiOperation(value = "Find progress of specified executing algorithm", notes = "Find progress of specified executing algorithm", response = Integer.class)
  @Override
  public Integer getAlgorithmProgress(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Algorithm execution internal id, e.g. 2", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful call POST (Process): /algo/" + id
        + "/progress, for user " + authToken);

    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName =
          authorizeProject(processService, projectId, securityService,
              authToken, "finding algorithm progress", UserRole.AUTHOR);
      processService.setLastModifiedBy(userName);

      final AlgorithmExecution algoExecution =
          processService.getAlgorithmExecution(id);
      // If algorithm has already completed successfully, return 100
      if (algoExecution.getFinishDate() != null
          && algoExecution.getFailDate() == null) {
        return 100;
      }
      if (lookupAeProgressMap.containsKey(id)) {
        return lookupAeProgressMap.get(id);
      }
      // Return -1 if id not contained in progress map.
      else {
        return -1;
      }

    } catch (Exception e) {
      handleException(e, "trying to get progress");
      return null;
    } finally {
      processService.close();
      securityService.close();
    }
  }

  /**
   * Run process as thread.
   *
   * @param projectId the project id
   * @param processConfigId the process config id
   * @param processExecutionId the process execution id
   * @param userName the user name
   * @param background the background
   * @param restart the restart
   * @throws Exception the exception
   */
  private void runProcessAsThread(Long projectId, Long processConfigId,
    Long processExecutionId, String userName, Boolean background,
    Boolean restart) throws Exception {

    // Set up vars for thread
    final Exception[] exceptions = new Exception[1];
    final boolean handleException = background != null && background;

    // Set up the service, and load the process Config and Execution
    final ProcessService processService = new ProcessServiceJpa();
    processService.setLastModifiedBy(userName);

    final ProcessConfig processConfig =
        processService.getProcessConfig(processConfigId);
    final ProcessExecution processExecution =
        processService.getProcessExecution(processExecutionId);

    // Clear out the finish and fail date fields (these could have been
    // populated from a previous run)
    processExecution.setFailDate(null);
    processExecution.setFinishDate(null);

    final Thread t = new Thread(new Runnable() {

      /* see superclass */
      @SuppressWarnings("cast")
      @Override
      public void run() {
        // Declare execution so it can be accessed
        AlgorithmExecution algorithmExecution = null;
        Boolean firstRestartedAlgorithm = false;
        try {

          // Set initial progress to zero and count the number of steps to
          // execute
          lookupPeProgressMap.put(processExecution.getId(), 0);

          final int enabledSteps = processConfig.getSteps().stream()
              .filter(ac -> ac.isEnabled()).collect(Collectors.toList()).size();

          // Start step counter at 0
          int stepCt = 0;

          // If this is a restart, find the steps that already ran
          List<Long> previouslyCompletedAlgorithmIds = new ArrayList<>();
          AlgorithmExecution algorithmToRestart = null;
          if (restart) {
            final List<AlgorithmExecution> previouslyStartedAlgorithms =
                processExecution.getSteps();
            for (final AlgorithmExecution ae : previouslyStartedAlgorithms) {
              // If the algorithm finished, save the algorithmConfigId (so it
              // can be skipped later)
              if (ae.getFinishDate() != null && ae.getFailDate() == null) {
                previouslyCompletedAlgorithmIds.add(ae.getAlgorithmConfigId());
              }
              // If the algorithm was mid-run, save the algorithm Execution to
              // run
              else if (ae.getFailDate() != null) {
                algorithmToRestart = ae;
                firstRestartedAlgorithm = true;
              }
            }
            // Update the processExecution progress and step-count
            stepCt = previouslyCompletedAlgorithmIds.size();
            lookupPeProgressMap.put(processExecution.getId(),
                (int) ((100 * stepCt) / enabledSteps));
          }

          // Iterate through algorithm configs
          for (final AlgorithmConfig algorithmConfig : processConfig
              .getSteps()) {

            // Skip steps that are not enabled
            if (!algorithmConfig.isEnabled()) {
              continue;
            }

            // Skip steps that completed on a previous run (if any)
            if (restart && previouslyCompletedAlgorithmIds
                .contains(algorithmConfig.getId())) {
              continue;
            }

            // If this is a restart, use the loaded algorithm Execution
            if (algorithmToRestart != null && algorithmToRestart
                .getAlgorithmConfigId().equals(algorithmConfig.getId())) {
              algorithmExecution = algorithmToRestart;
              algorithmExecution.setFailDate(null);
              algorithmExecution.setFinishDate(null);
            }
            // Otherwise, instantiate and configure the algorithm execution
            else {
              algorithmExecution = new AlgorithmExecutionJpa(algorithmConfig);
              // Create a copy of the properties to add to algorithmExecution
              // (using same object causes shared references to a collection
              // error
              algorithmExecution.setProperties(
                  new HashMap<String, String>(algorithmConfig.getProperties()));
              algorithmExecution.setProcess(processExecution);
              algorithmExecution.setActivityId(UUID.randomUUID().toString());
              algorithmExecution.setStartDate(new Date());
              algorithmExecution =
                  processService.addAlgorithmExecution(algorithmExecution);

              // Add the algorithm execution to the process
              processExecution.getSteps().add(algorithmExecution);
              processService.updateProcessExecution(processExecution);
            }

            // Create and configure the algorithm
            // TODO: this method should create a fully configured instance of
            // the algorithm, including taking into account config.properties
            // settings for the algorithm
            final Algorithm algorithm = processService
                .getAlgorithmInstance(algorithmExecution.getAlgorithmKey());
            algorithm.setProject(processExecution.getProject());
            algorithm.setProcess(processExecution);
            algorithm.setWorkId(processExecution.getWorkId());
            algorithm.setActivityId(algorithmExecution.getActivityId());
            algorithm.setLastModifiedBy(userName);
            algorithm.setTransactionPerOperation(false);
            algorithm.beginTransaction();
            // Convert Map<String,String> into properties to configure
            // algorithm
            final Properties prop = new Properties();
            for (final Map.Entry<String, String> entry : algorithmExecution
                .getProperties().entrySet()) {
              prop.setProperty(entry.getKey(), entry.getValue());
            }
            algorithm.setProperties(prop);

            // track currently running algorithm
            processAlgorithmMap.put(processExecution.getId(), algorithm);

            // Check preconditions
            final ValidationResult result = algorithm.checkPreconditions();
            if (!result.isValid()) {
              throw new Exception("Algorithm " + algorithmExecution.getId()
                  + " failed preconditions: " + result.getErrors());
            }

            final Long aeId = algorithmExecution.getId();
            final int currentCt = stepCt;

            // algorithmExecution needs to be recast as final, so it can be
            // modified by updateProgress
            final AlgorithmExecution finalAlgorithmExecution =
                algorithmExecution;

            algorithm.addProgressListener(new ProgressListener() {
              @Override
              public void updateProgress(ProgressEvent processEvent) {
                if (processEvent.isWarning()) {
                  finalAlgorithmExecution.setWarning(true);
                  return;
                }
                lookupAeProgressMap.put(aeId, processEvent.getPercent());

                // pe progress is the current progress plus the scaled
                // progress of the ae
                lookupPeProgressMap.put(processExecution.getId(),
                    (int) ((100 * currentCt) / enabledSteps)
                        + (int) (processEvent.getPercent() / enabledSteps));

              }
            });

            // Start progress at 0 for the algorithm
            lookupAeProgressMap.put(algorithmExecution.getId(), 0);

            // If we're in restart mode, and if this is the First algorithm
            // we're running, reset the algorithm.
            if (restart && firstRestartedAlgorithm) {
              algorithm.reset();
              // Commit and reset transaction
              algorithm.commitClearBegin();
              // Don't reset on any later algorithms
              firstRestartedAlgorithm = false;
            }

            // Execute algorithm
            algorithm.compute();

            // Commit any changes the algorithm wants to make
            algorithm.commit();

            // Take the number of steps completed times 100 and divided by the
            // total number of steps
            lookupPeProgressMap.put(processExecution.getId(),
                (int) ((100 * ++stepCt) / enabledSteps));

            // algorithm has finished
            algorithmExecution.setFinishDate(new Date());
            processService.updateAlgorithmExecution(algorithmExecution);

            // Mark algorithm as finished
            lookupAeProgressMap.remove(algorithmExecution.getId());
            processAlgorithmMap.remove(processExecution.getId());

          }

          // Process has finished
          processExecution.setFinishDate(new Date());
          processService.updateProcessExecution(processExecution);

          // Mark process as finished
          lookupPeProgressMap.remove(processExecution.getId());

          // Send email notifying about successful completion
          String recipients = processExecution.getFeedbackEmail();

          if (!ConfigUtility.isEmpty(recipients)) {
            final Properties config = ConfigUtility.getConfigProperties();
            ConfigUtility.sendEmail(
                "[Terminology Server] Run Complete for Process: "
                    + processExecution.getName(),
                config.getProperty("mail.smtp.user"), recipients,
                processService.getProcessLog(projectId, processExecutionId),
                config, "true".equals(config.get("mail.smtp.auth")));
          }

        } catch (Exception e) {
          exceptions[0] = e;

          // Remove process and algorithm from the maps
          processAlgorithmMap.remove(processExecutionId);
          lookupPeProgressMap.remove(processExecutionId);
          lookupAeProgressMap.remove(algorithmExecution.getId());

          // Mark algorithm and process as failed
          try {
            // set cancel conditions if cancel was used.
            if (e instanceof CancelException) {
              algorithmExecution.setFinishDate(new Date());
              processExecution.setFinishDate(new Date());
            }
            algorithmExecution.setFailDate(new Date());
            processService.updateAlgorithmExecution(algorithmExecution);

            processExecution.setFailDate(new Date());
            processService.updateProcessExecution(processExecution);
          } catch (Exception ex) {
            handleException(ex, "trying to update execution info");
          }

          // Send email notifying about failed run
          String recipients = processExecution.getFeedbackEmail();

          if (!ConfigUtility.isEmpty(recipients)) {
            try {
              final Properties config = ConfigUtility.getConfigProperties();
              ConfigUtility.sendEmail(
                  "[Terminology Server] Process Run Failed for Process: "
                      + processExecution.getName() + " at Algorithm step: "
                      + algorithmExecution.getName(),
                  config.getProperty("mail.smtp.user"), recipients,
                  processService.getProcessLog(projectId, processExecutionId),
                  config, "true".equals(config.get("mail.smtp.auth")));
            } catch (Exception e2) {
              // n/a
            }
          }

          // Do this if IS running in the background
          if (handleException) {
            handleException(e, "trying to execute a process");
          }

        } finally {
          try {
            processService.close();
          } catch (Exception e) {
            // n/a
          }
        }

      }
    });
    if (background != null && background == true) {
      t.start();
    } else {
      t.start();
      t.join();
      if (exceptions[0] != null) {
        throw new Exception(exceptions[0]);
      }
    }

  }

  /* see superclass */
  @GET
  @Path("{processExecutionId}/log")
  @ApiOperation(value = "Get log entries of specified process execution", notes = "Get log entries of specified process execution", response = Integer.class)
  @Override
  public String getProcessLog(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Process execution internal id, e.g. 2", required = true) @PathParam("processExecutionId") Long processExecutionId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful call (Process): /"
        + processExecutionId + "/log, for user " + authToken);

    if (projectId == null) {
      throw new Exception("Error: project id must be set.");
    }

    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName = authorizeProject(processService, projectId,
          securityService, authToken,
          "getting the process execution log entries", UserRole.AUTHOR);
      processService.setLastModifiedBy(userName);

      return processService.getProcessLog(projectId, processExecutionId);
    } catch (Exception e) {
      handleException(e, "trying to get the process execution log entries");
      return null;
    } finally {
      processService.close();
      securityService.close();
    }

  }

  @GET
  @Path("algo/{algorithmExecutionId}/log")
  @ApiOperation(value = "Get log entries of specified algorithm execution", notes = "Get log entries of specified algorithm execution", response = Integer.class)
  @Override
  public String getAlgorithmLog(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Algorithm execution internal id, e.g. 2", required = true) @PathParam("algorithmExecutionId") Long algorithmExecutionId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful call (Process): /algo/"
        + algorithmExecutionId + "/log, for user " + authToken);

    if (projectId == null) {
      throw new Exception("Error: project id must be set.");
    }

    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName = authorizeProject(processService, projectId,
          securityService, authToken,
          "getting the algorithm execution log entries", UserRole.AUTHOR);
      processService.setLastModifiedBy(userName);

      return processService.getAlgorithmLog(projectId, algorithmExecutionId);

    } catch (Exception e) {
      handleException(e, "trying to get the algorithm execution log entries");
      return null;
    } finally {
      processService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @GET
  @Path("/config/algo/{key}/new")
  @ApiOperation(value = "Get an empty new algorithm config", notes = "Returns an empty new algorithm config", response = AlgorithmConfigJpa.class)
  public AlgorithmConfig newAlgorithmConfig(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Algorithm config key, e.g. MATRIXINT", required = true) @PathParam("key") String key,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful call (Process): /config/algo/"
        + key + "/new, for user " + authToken);

    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName = authorizeProject(processService, projectId,
          securityService, authToken, "adding a new algorithm config",
          UserRole.ADMINISTRATOR);
      processService.setLastModifiedBy(userName);

      // Load project
      final Project project = processService.getProject(projectId);
      final Algorithm algo = processService.getAlgorithmInstance(key);
      final AlgorithmConfig algorithmConfig = new AlgorithmConfigJpa();
      algorithmConfig.setParameters(algo.getParameters());
      algorithmConfig.setProject(project);
      return algorithmConfig;

    } catch (Exception e) {
      handleException(e, "trying to return a new algorithm config");
      return null;
    } finally {
      processService.close();
      securityService.close();
    }

  }

}
