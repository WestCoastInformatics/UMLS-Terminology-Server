/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.rest.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
import com.wci.umls.server.AlgorithmParameter;
import com.wci.umls.server.ProcessConfig;
import com.wci.umls.server.ProcessExecution;
import com.wci.umls.server.Project;
import com.wci.umls.server.UserRole;
import com.wci.umls.server.algo.Algorithm;
import com.wci.umls.server.helpers.KeyValuePairList;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.helpers.ProcessConfigList;
import com.wci.umls.server.jpa.AlgorithmConfigJpa;
import com.wci.umls.server.jpa.ProcessConfigJpa;
import com.wci.umls.server.jpa.ProcessExecutionJpa;
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

  /** The lookup process execution progress map. */
  private static Map<Long, Integer> lookupPeProgressMap;

  /** The lookup algorithm execution progress map. */
  private static Map<Long, Integer> lookupAeProgressMap;

  /** The map of which algorithm a process is currently running. */
  private static Map<Long, Algorithm> processAlgorithmMap;

  /**
   * Instantiates an empty {@link ProcessServiceRestImpl}.
   *
   * @throws Exception the exception
   */
  public ProcessServiceRestImpl() throws Exception {
    securityService = new SecurityServiceJpa();
    lookupPeProgressMap = new HashMap<Long, Integer>();
    lookupAeProgressMap = new HashMap<Long, Integer>();
    processAlgorithmMap = new HashMap<Long, Algorithm>();
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
        .info("RESTful call PUT (Process): /config/add for user " + authToken
            + ", " + processConfig);

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
        .info("RESTful call PUT (Process): /config/update for user " + authToken
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
    Logger.getLogger(getClass()).info("RESTful call DELETE (Process): /config/"
        + id + "/remove, for user " + authToken);

    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName =
          authorizeProject(processService, projectId, securityService,
              authToken, "remove processConfig", UserRole.ADMINISTRATOR);
      processService.setLastModifiedBy(userName);

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
    Logger.getLogger(getClass()).info("RESTful call (Process): /config/" + id);

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

      // For each of the process' algorithms, populate the parameters based on
      // its properties' values.
      for (AlgorithmConfig algorithmConfig : processConfig.getSteps()) {
        Algorithm instance = processService
            .getAlgorithmInstance(algorithmConfig.getAlgorithmKey());
        algorithmConfig.setParameters(instance.getParameters());
        for (AlgorithmParameter param : algorithmConfig.getParameters()) {
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
    Logger.getLogger(getClass())
        .info("RESTful call POST (Process): /config " + query);

    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName =
          authorizeProject(processService, projectId, securityService,
              authToken, "finding process configs", UserRole.AUTHOR);
      processService.setLastModifiedBy(userName);

      ProcessConfigList processConfigs =
          processService.findProcessConfigs(projectId, query, pfs);

      // Set steps to empty list for all returned processConfigs
      for (ProcessConfig processConfig : processConfigs.getObjects()) {
        processConfig.setSteps(new ArrayList<AlgorithmConfig>());
      }

      return processService.findProcessConfigs(projectId, query, pfs);

    } catch (Exception e) {
      handleException(e, "trying to find process configs");
      return null;
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

      // Populate the algorithm's properties based on its parameters' values.
      for (AlgorithmParameter param : algorithmConfig.getParameters()) {
        // Note: map either Value OR Values (comma-delimited)
        if (!param.getValues().isEmpty()) {
          algorithmConfig.getProperties().put(param.getFieldName(),
              StringUtils.join(param.getValues(), ','));
        } else if (!param.getValue().isEmpty()) {
          algorithmConfig.getProperties().put(param.getFieldName(),
              param.getValue());
        } else {
          throw new Exception(
              "Parameter " + param + " does not have valid value(s).");
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

      // Populate the algorithm's properties based on its parameters' values.
      for (AlgorithmParameter param : algorithmConfig.getParameters()) {
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
    Logger.getLogger(getClass())
        .info("RESTful call DELETE (Process): /config/algo/" + id
            + "/remove, for user " + authToken);

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

      // Populate the parameters based on its properties' values.
      Algorithm instance = processService
          .getAlgorithmInstance(algorithmConfig.getAlgorithmKey());
      algorithmConfig.setParameters(instance.getParameters());
      for (AlgorithmParameter param : algorithmConfig.getParameters()) {
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
        .info("RESTful call (Process): /algo/insertion");

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
    Logger.getLogger(getClass())
        .info("RESTful call (Process): /algo/maintenance");

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
    Logger.getLogger(getClass()).info("RESTful call (Process): /algo/release");

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

  /* see superclass */  
  @Override
  @POST
  @Path("/config/{id}/execute")
  @ApiOperation(value = "Execute a process configuration", notes = "Execute the specified process configuration")
  public Long executeProcess(
    @ApiParam(value = "Project id, e.g. 1", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "ProcessConfig id, e.g. 3", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Background, e.g. true", required = true) @QueryParam("background") Boolean background,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful call (Process): /config/" + id + "/execute, for user " + authToken);
 
    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName =
          authorizeProject(processService, projectId, securityService,
              authToken, "execute processConfig", UserRole.ADMINISTRATOR);
      processService.setLastModifiedBy(userName);

      // Load processConfig object
      ProcessConfig processConfig = processService.getProcessConfig(id);

      // Make sure processConfig exists
      if (processConfig == null) {
        throw new Exception("ProcessConfig " + id + " does not exist");
      }

      // Verify that passed projectId matches ID of the processConfig's project
      verifyProject(processConfig, projectId);

      // Make sure this processConfig is not already running
      if(processService.findProcessExecutions(projectId, "processConfigId:" + processConfig.getId(), null).size()!=0){
        throw new Exception("There is already a currently running execution of process " + id);
      }
      
      // Create a new process Execution
      ProcessExecution execution = new ProcessExecutionJpa();
      
      final Exception[] exceptions = new Exception[1];
      Thread t = new Thread(new Runnable() {

        @Override
        public void run() {
          //SourceDataHandler handler = null;
          try {
//            // instantiate the handler
//            Class<?> sourceDataHandlerClass =
//                Class.forName(sourceData.getHandler());
//            handler = (SourceDataHandler) sourceDataHandlerClass.newInstance();
//            handler.setLastModifiedBy(userName);
//            handler.setSourceData(sourceData);
//            handler.remove();

          } catch (Exception e) {
//            handleException(e,
//                " during removal of loaded data from source data");
          } finally {
//            if (handler != null) {
//              try {
//                handler.close();
//              } catch (Exception e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//              }
//            }
          }
        }
      });
      if (background != null && background == true) {
        t.start();
        return 1L;
      } else {
        t.join();
        if (exceptions[0] != null) {
          throw new Exception(exceptions[0]);
        }
      }
    }  catch (Exception e) {
      handleException(e, "trying to remove a processConfig");
    } finally {
      processService.close();
      securityService.close();
    }
    return 1L;
  }

  // /* see superclass */
  // @Override
  // @PUT
  // @Path("/parameter/algo/add")
  // @ApiOperation(value = "Add new algorithm parameter", notes = "Creates a new
  // algorithm parameter", response = AlgorithmParameterJpa.class)
  // public AlgorithmParameter addAlgorithmParameter(
  // @ApiParam(value = "Project id, e.g. 1", required = true)
  // @QueryParam("projectId") Long projectId,
  // @ApiParam(value = "AlgorithmParameter, e.g. newAlgorithmParameter",
  // required = true) AlgorithmParameterJpa algorithmParameter,
  // @ApiParam(value = "Authorization token, e.g. 'guest'", required = true)
  // @HeaderParam("Authorization") String authToken)
  // throws Exception {
  // Logger.getLogger(getClass())
  // .info("RESTful call PUT (Process): /parameter/algo/add for user "
  // + authToken + ", " + algorithmParameter);
  //
  // final ProcessService processService = new ProcessServiceJpa();
  // try {
  // final String userName =
  // authorizeProject(processService, projectId, securityService,
  // authToken, "adding a process parameter", UserRole.ADMINISTRATOR);
  // processService.setLastModifiedBy(userName);
  //
  // ProcessParameterJpa processParameter = null;
  // // Load processParameter
  // if (algorithmParameter.getProcess() != null) {
  // processParameter = (ProcessParameterJpa) processService
  // .getProcessParameter(algorithmParameter.getProcess().getId());
  // }
  //
  // // Re-add processParameter to algorithmParameter (it does not make it
  // intact
  // // through XML)
  // algorithmParameter.setProcess(processParameter);
  //
  // // Load project
  // Project project = processService.getProject(projectId);
  // project.setLastModifiedBy(userName);
  //
  // // Re-add project to algorithmParameter (it does not make it intact through
  // // XML)
  // algorithmParameter.setProject(project);
  //
  // // Verify that passed projectId matches ID of the algorithmParameter's
  // // project
  // verifyProject(algorithmParameter, projectId);
  //
  // // Populate the algorithm's properties based on its parameters' values.
  // for (AlgorithmParameter param : algorithmParameter.getParameters()) {
  // algorithmParameter.getProperties().put(param.getFieldName(),
  // param.getValue());
  // }
  //
  // // Add algorithmParameter
  // final AlgorithmParameter newAlgorithmParameter =
  // processService.addAlgorithmParameter(algorithmParameter);
  //
  // // If new algorithm parameter has an associated processParameter,
  // // add the algorithm to it and update
  // if (processParameter != null) {
  // // Add algorithmParameter to processParameter
  // processParameter.getSteps().add(newAlgorithmParameter);
  //
  // // update the processParameter
  // processService.updateProcessParameter(processParameter);
  // }
  //
  // processService.addLogEntry(userName, projectId, algorithmParameter.getId(),
  // null, null, "ADD algorithmParameter - " + algorithmParameter);
  //
  // return newAlgorithmParameter;
  // } catch (Exception e) {
  // handleException(e, "trying to add an algorithmParameter");
  // return null;
  // } finally {
  // processService.close();
  // securityService.close();
  // }
  //
  // }
  //
  // /* see superclass */
  // @Override
  // @POST
  // @Path("/parameter/algo/update")
  // @ApiOperation(value = "Update algorithm parameter", notes = "Updates the
  // specified algorithm parameter")
  // public void updateAlgorithmParameter(
  // @ApiParam(value = "Project id, e.g. 1", required = true)
  // @QueryParam("projectId") Long projectId,
  // @ApiParam(value = "AlgorithmParameter, e.g. existingAlgorithmParameter",
  // required = true) AlgorithmParameterJpa algorithmParameter,
  // @ApiParam(value = "Authorization token, e.g. 'guest'", required = true)
  // @HeaderParam("Authorization") String authToken)
  // throws Exception {
  // Logger.getLogger(getClass())
  // .info("RESTful call PUT (Process): /parameter/algo/update for user "
  // + authToken + ", " + algorithmParameter);
  //
  // final ProcessService processService = new ProcessServiceJpa();
  // try {
  // final String userName =
  // authorizeProject(processService, projectId, securityService,
  // authToken, "update algorithmParameter", UserRole.ADMINISTRATOR);
  // processService.setLastModifiedBy(userName);
  //
  // // Load processParameter
  // ProcessParameterJpa processParameter = (ProcessParameterJpa) processService
  // .getProcessParameter(algorithmParameter.getProcess().getId());
  //
  // // Re-add processParameter to algorithmParameter (it does not make it
  // intact
  // // through XML)
  // algorithmParameter.setProcess(processParameter);
  //
  // // Load project
  // Project project = processService.getProject(projectId);
  // project.setLastModifiedBy(userName);
  //
  // // Re-add project to algorithmParameter (it does not make it intact through
  // // XML)
  // algorithmParameter.setProject(project);
  //
  // // Verify that passed projectId matches ID of the algorithmParameter's
  // // project
  // verifyProject(algorithmParameter, projectId);
  //
  // // ensure algorithmParameter exists
  // final AlgorithmParameter origAlgorithmParameter =
  // processService.getAlgorithmParameter(algorithmParameter.getId());
  // if (origAlgorithmParameter == null) {
  // throw new Exception(
  // "AlgorithmParameter " + algorithmParameter.getId() + " does not exist");
  // }
  //
  // // Populate the algorithm's properties based on its parameters' values.
  // for (AlgorithmParameter param : algorithmParameter.getParameters()) {
  // algorithmParameter.getProperties().put(param.getFieldName(),
  // param.getValue());
  // }
  //
  // // Update algorithmParameter
  // processService.updateAlgorithmParameter(algorithmParameter);
  //
  // processService.addLogEntry(userName, projectId, algorithmParameter.getId(),
  // null, null, "UPDATE algorithmParameter " + algorithmParameter);
  //
  // } catch (Exception e) {
  // handleException(e, "trying to update an algorithmParameter");
  // } finally {
  // processService.close();
  // securityService.close();
  // }
  //
  // }
  //
  // /* see superclass */
  // @Override
  // @DELETE
  // @Path("/parameter/algo/{id}/remove")
  // @ApiOperation(value = "Remove algorithm parameter", notes = "Removes the
  // algorithm parameter with the specified id")
  // public void removeAlgorithmParameter(
  // @ApiParam(value = "Project id, e.g. 1", required = true)
  // @QueryParam("projectId") Long projectId,
  // @ApiParam(value = "AlgorithmParameter id, e.g. 3", required = true)
  // @PathParam("id") Long id,
  // @ApiParam(value = "Authorization token, e.g. 'guest'", required = true)
  // @HeaderParam("Authorization") String authToken)
  // throws Exception {
  // Logger.getLogger(getClass())
  // .info("RESTful call DELETE (Process): /parameter/algo/" + id
  // + "/remove, for user " + authToken);
  //
  // final ProcessService processService = new ProcessServiceJpa();
  // try {
  // final String userName =
  // authorizeProject(processService, projectId, securityService,
  // authToken, "remove algorithmParameter", UserRole.ADMINISTRATOR);
  // processService.setLastModifiedBy(userName);
  //
  // // Load algorithmParameter object
  // AlgorithmParameter algorithmParameter =
  // processService.getAlgorithmParameter(id);
  //
  // // ensure algorithmParameter exists
  // if (algorithmParameter == null) {
  // throw new Exception("AlgorithmParameter " + id + " does not exist");
  // }
  //
  // // Verify that passed projectId matches ID of the algorithmParameter's
  // // project
  // verifyProject(algorithmParameter, projectId);
  //
  // // If the algorithm parameter has an associated processParameter,
  // // remove the algorithm from it and update
  // ProcessParameter processParameter = algorithmParameter.getProcess();
  //
  // if (processParameter != null) {
  // // Remove algorithmParameter from processParameter
  // processParameter.getSteps().add(algorithmParameter);
  //
  // // update the processParameter
  // processService.updateProcessParameter(processParameter);
  // }
  //
  // // Remove algorithm parameter
  // processService.removeAlgorithmParameter(id);
  //
  // processService.addLogEntry(userName, projectId, id, null, null,
  // "REMOVE algorithmParameter " + id);
  //
  // } catch (Exception e) {
  // handleException(e, "trying to remove an algorithmParameter");
  // } finally {
  // processService.close();
  // securityService.close();
  // }
  // }
  //
  // /* see superclass */
  // @Override
  // @GET
  // @Path("/parameter/algo/{id}")
  // @ApiOperation(value = "Get algorithm parameter for id", notes = "Gets the
  // algorithm parameter for the specified id", response =
  // AlgorithmParameterJpa.class)
  // public AlgorithmParameter getAlgorithmParameter(
  // @ApiParam(value = "Project internal id, e.g. 2", required = true)
  // @QueryParam("projectId") Long projectId,
  // @ApiParam(value = "AlgorithmParameter internal id, e.g. 2", required =
  // true) @PathParam("id") Long id,
  // @ApiParam(value = "Authorization token, e.g. 'guest'", required = true)
  // @HeaderParam("Authorization") String authToken)
  // throws Exception {
  // Logger.getLogger(getClass())
  // .info("RESTful call (Process): /parameter/algo/" + id);
  //
  // final ProcessService processService = new ProcessServiceJpa();
  // try {
  // final String userName =
  // authorizeProject(processService, projectId, securityService,
  // authToken, "getting the algorithmParameter", UserRole.AUTHOR);
  // processService.setLastModifiedBy(userName);
  //
  // // Load algorithmParameter object
  // AlgorithmParameter algorithmParameter =
  // processService.getAlgorithmParameter(id);
  //
  // if (algorithmParameter == null) {
  // return algorithmParameter;
  // }
  //
  // // Verify that passed projectId matches ID of the algorithmParameter's
  // // project
  // verifyProject(algorithmParameter, projectId);
  //
  // return algorithmParameter;
  // } catch (Exception e) {
  // handleException(e, "trying to get a algorithmParameter");
  // return null;
  // } finally {
  // processService.close();
  // securityService.close();
  // }
  // }


}
