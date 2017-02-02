/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.rest.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

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
import com.wci.umls.server.helpers.FieldedStringTokenizer;
import com.wci.umls.server.helpers.KeyValuePairList;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.helpers.ProcessConfigList;
import com.wci.umls.server.helpers.ProcessExecutionList;
import com.wci.umls.server.helpers.QueryType;
import com.wci.umls.server.jpa.AlgorithmConfigJpa;
import com.wci.umls.server.jpa.AlgorithmExecutionJpa;
import com.wci.umls.server.jpa.ProcessConfigJpa;
import com.wci.umls.server.jpa.ProcessExecutionJpa;
import com.wci.umls.server.jpa.content.AtomJpa;
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

  /** The lock. */
  static String lock = "LOCK";

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

  /* see superclass */
  @Override
  @PUT
  @Path("/config")
  @ApiOperation(value = "Add new process config", notes = "Creates a new process config", response = ProcessConfigJpa.class)
  public ProcessConfig addProcessConfig(
    @ApiParam(value = "Project id, e.g. 12345", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "ProcessConfig, as POST data", required = true) ProcessConfigJpa process,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Process): /config?projectId=" + projectId
            + " for user " + authToken + ", " + process);

    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName =
          authorizeProject(processService, projectId, securityService,
              authToken, "adding a process config", UserRole.ADMINISTRATOR);
      processService.setLastModifiedBy(userName);

      // Make sure processConfig was passed in
      if (process == null) {
        throw new LocalException("Error: trying to add null process config");
      }

      // Load project
      final Project project = processService.getProject(projectId);
      project.setLastModifiedBy(userName);

      // Re-add project to processConfig (it does not make it intact through
      // XML)
      process.setProject(project);

      final ProcessConfigList list =
          processService.findProcessConfigs(projectId,
              "nameSort:\"" + QueryParserBase.escape(process.getName()) + "\"",
              null);
      if (list.size() > 0) {
        process.setName(process.getName() + " - "
            + ConfigUtility.DATE_YYYYMMDDHHMMSS.format(new Date()));

      }

      // Verify that passed projectId matches ID of the processConfig's project
      verifyProject(process, projectId);

      // check to see if processConfig already exists
      if (processService.findProcessConfigs(projectId,
          "name:\"" + process.getName() + "\"", null).size() > 0) {
        throw new LocalException(
            "A processConfig with this name and description already exists for this project");
      }

      // Add processConfig
      final ProcessConfig newProcess = processService.addProcessConfig(process);

      processService.addLogEntry(userName, projectId, process.getId(), null,
          null, "ADD processConfig - " + process);

      return newProcess;
    } catch (Exception e) {
      handleException(e, "trying to add a process config");
      return null;
    } finally {
      processService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @POST
  @Override
  @Path("/config/import")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @ApiOperation(value = "Import process config", notes = "Imports a process config", response = ProcessConfigJpa.class)
  public ProcessConfig importProcessConfig(
    @ApiParam(value = "Form data header", required = true) @FormDataParam("file") FormDataContentDisposition contentDispositionHeader,
    @ApiParam(value = "Content of members file", required = true) @FormDataParam("file") InputStream in,
    @ApiParam(value = "Project id, e.g. 12345", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Process): /config/import?projectId=" + projectId);

    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName =
          authorizeProject(processService, projectId, securityService,
              authToken, "adding a process config", UserRole.ADMINISTRATOR);
      processService.setLastModifiedBy(userName);
      // This should be atomic
      processService.setTransactionPerOperation(false);
      processService.beginTransaction();

      // Load project
      final Project project = processService.getProject(projectId);

      // Convert to a String
      final String json = IOUtils.toString(in, "UTF-8");

      // Convert to an object
      final ProcessConfigJpa process =
          ConfigUtility.getGraphForJson(json, ProcessConfigJpa.class);

      // Clean up the imported process
      process.setProject(project);
      // Verify that passed projectId matches ID of the processConfig's project
      verifyProject(process, projectId);

      // Save steps
      final List<AlgorithmConfig> configs = new ArrayList<>(process.getSteps());

      // Prep process
      process.setId(null);
      process.getSteps().clear();
      final ProcessConfigList list = processService.findProcessConfigs(
          projectId,
          "name:\"" + QueryParserBase.escape(process.getName()) + "\"", null);
      if (list.size() > 0) {
        process.setName(process.getName() + " - "
            + ConfigUtility.DATE_YYYYMMDDHHMMSS.format(new Date()));
      }
      final ProcessConfig newProcess = processService.addProcessConfig(process);

      // Add steps
      for (final AlgorithmConfig config : configs) {
        config.setId(null);
        config.setProcess(newProcess);
        newProcess.getSteps().add(processService.addAlgorithmConfig(config));
      }

      processService.updateProcessConfig(newProcess);
      processService.addLogEntry(userName, projectId, newProcess.getId(), null,
          null, "IMPORT process config - " + newProcess);

      processService.commit();
      return newProcess;
    } catch (Exception e) {
      handleException(e, "trying to add a process config");
      return null;
    } finally {
      processService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @POST
  @Override
  @Produces("application/octet-stream")
  @Path("/config/export")
  @ApiOperation(value = "Export process config", notes = "Exports a process config", response = InputStream.class)
  public InputStream exportProcessConfig(
    @ApiParam(value = "Project id, e.g. 12345", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Process id, e.g. 23425", required = true) @QueryParam("processId") Long processId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Process): /config/export?projectId=" + projectId);

    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName =
          authorizeProject(processService, projectId, securityService,
              authToken, "adding a process config", UserRole.ADMINISTRATOR);
      processService.setLastModifiedBy(userName);

      // Load project/process
      final ProcessConfig process = processService.getProcessConfig(processId);
      verifyProject(process, projectId);

      return new ByteArrayInputStream(
          ConfigUtility.getJsonForGraph(process).getBytes("UTF-8"));

    } catch (Exception e) {
      handleException(e, "trying to export aprocess config");
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
  @ApiOperation(value = "Update process config", notes = "Updates the specified process config")
  public void updateProcessConfig(
    @ApiParam(value = "Project id, e.g. 12345", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "ProcessConfig, as POST data", required = true) ProcessConfigJpa processConfig,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Process): /config?projectId=" + projectId
            + " for user " + authToken + ", " + processConfig);

    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName =
          authorizeProject(processService, projectId, securityService,
              authToken, "update process config", UserRole.ADMINISTRATOR);
      processService.setLastModifiedBy(userName);

      // Make sure processConfig was passed in
      if (processConfig == null) {
        throw new LocalException("Error: trying to update null process config");
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
      final ProcessConfig origProcess =
          processService.getProcessConfig(processConfig.getId());
      if (origProcess == null) {
        throw new Exception(
            "ProcessConfig " + processConfig.getId() + " does not exist");
      }

      // Update processConfig
      processService.updateProcessConfig(processConfig);

      processService.addLogEntry(userName, projectId, processConfig.getId(),
          null, null, "UPDATE processConfig " + processConfig);

    } catch (Exception e) {
      handleException(e, "trying to update a process config");
    } finally {
      processService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @DELETE
  @Path("/config/{id}")
  @ApiOperation(value = "Remove process config", notes = "Removes the processConfig with the specified id")
  public void removeProcessConfig(
    @ApiParam(value = "Project id, e.g. 12345", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "ProcessConfig id, e.g. 3", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Cascade, e.g. true", required = true) @QueryParam("cascade") Boolean cascade,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Process): /config/" + id + "?projectId="
            + projectId + ((cascade != null && cascade) ? "&cascade=true" : "")
            + " for user " + authToken);

    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName =
          authorizeProject(processService, projectId, securityService,
              authToken, "remove process config", UserRole.ADMINISTRATOR);
      processService.setLastModifiedBy(userName);

      // Load processConfig object
      final ProcessConfig process = processService.getProcessConfig(id);

      // Make sure processConfig exists
      if (process == null) {
        throw new Exception("ProcessConfig " + id + " does not exist");
      }

      // Verify that passed projectId matches ID of the processConfig's project
      verifyProject(process, projectId);

      // If cascade if specified, Remove contained algorithm configs, if any,
      // and
      // update ProcessConfig before removing it
      if (cascade && !process.getSteps().isEmpty()) {
        for (final AlgorithmConfig algo : new ArrayList<AlgorithmConfig>(
            process.getSteps())) {
          // BAC: I dont' think this is necessary on a remove
          // processConfig.getSteps().remove(config);
          // processService.updateProcessConfig(processConfig);
          processService.removeAlgorithmConfig(algo.getId());
        }
      }

      // Remove process config
      processService.removeProcessConfig(id);

      processService.addLogEntry(userName, projectId, id, null, null,
          "REMOVE processConfig " + id);

    } catch (Exception e) {
      handleException(e, "trying to remove a process config");
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
    @ApiParam(value = "Project id, e.g. 12345", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "ProcessConfig internal id, e.g. 2", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful call (Process): /config/" + id
        + "?projectId=" + projectId + " for user " + authToken);

    final ProcessService processService = new ProcessServiceJpa();
    Algorithm instance = null;
    try {
      final String userName =
          authorizeProject(processService, projectId, securityService,
              authToken, "getting the process config", UserRole.AUTHOR);
      processService.setLastModifiedBy(userName);

      // Load processConfig object
      final ProcessConfig process = processService.getProcessConfig(id);

      if (process == null) {
        return process;
      }

      // Verify that passed projectId matches ID of the processConfig's project
      verifyProject(process, projectId);

      // For each of the process' algorithms, populate the parameters based on
      // its properties' values.
      for (final AlgorithmConfig algo : process.getSteps()) {
        instance = processService.getAlgorithmInstance(algo.getAlgorithmKey());
        instance.setProject(processService.getProject(projectId));
        algo.setParameters(instance.getParameters());
        instance.close();
        for (final AlgorithmParameter param : algo.getParameters()) {
          // Populate both Value and Values (UI will determine which is required
          // for each algorithm type)
          if (algo.getProperties().get(param.getFieldName()) != null) {
            if (param.getType().equals(AlgorithmParameter.Type.MULTI)) {
              param.setValues(new ArrayList<String>(Arrays.asList(
                  algo.getProperties().get(param.getFieldName()).split(";"))));
            } else {
              param.setValue(algo.getProperties().get(param.getFieldName()));
            }
          }
        }
      }

      return process;
    } catch (Exception e) {
      handleException(e, "trying to get a process config");
      return null;
    } finally {
      processService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @POST
  @Path("/config/find")
  @ApiOperation(value = "Find processConfigs", notes = "Find processConfigs", response = ProcessConfigListJpa.class)
  public ProcessConfigList findProcessConfigs(
    @ApiParam(value = "Project id, e.g. 12345", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "The query string", required = false) @QueryParam("query") String query,
    @ApiParam(value = "The paging/sorting/filtering parameter", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Process): /config/find?projectId=" + projectId
            + "&query=" + query + " for user " + authToken + ", " + pfs);

    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName =
          authorizeProject(processService, projectId, securityService,
              authToken, "finding process configs", UserRole.AUTHOR);
      processService.setLastModifiedBy(userName);

      final ProcessConfigList processes =
          processService.findProcessConfigs(projectId, query, pfs);

      // Set steps to empty list for all returned processConfigs
      for (final ProcessConfig process : processes.getObjects()) {
        process.setSteps(new ArrayList<AlgorithmConfig>());
      }

      return processes;

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
  @GET
  @Path("/execution/{id}")
  @ApiOperation(value = "Get processExecution for id", notes = "Gets the processExecution for the specified id", response = ProcessExecutionJpa.class)
  public ProcessExecution getProcessExecution(
    @ApiParam(value = "Project id, e.g. 12345", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "ProcessExecution internal id, e.g. 2", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful call (Process): /execution/" + id
        + "?projectId=" + projectId + " for user " + authToken);

    final ProcessService processService = new ProcessServiceJpa();
    Algorithm instance = null;
    try {
      final String userName =
          authorizeProject(processService, projectId, securityService,
              authToken, "getting the process execution", UserRole.AUTHOR);
      processService.setLastModifiedBy(userName);

      // Load processExecution object
      ProcessExecution processExecution = null;
      synchronized (lock) {
        processExecution = processService.getProcessExecution(id);
      }

      if (processExecution == null) {
        return processExecution;
      }

      // Verify that passed projectId matches ID of the processExecution's
      // project
      verifyProject(processExecution, projectId);
      Project project = processService.getProject(projectId);

      // For each of the process' algorithms, populate the parameters based on
      // its properties' values.
      for (final AlgorithmExecution algorithmExecution : processExecution
          .getSteps()) {
        instance = processService
            .getAlgorithmInstance(algorithmExecution.getAlgorithmKey());
        instance.setProject(project);
        algorithmExecution.setParameters(instance.getParameters());
        instance.close();
        for (final AlgorithmParameter param : algorithmExecution
            .getParameters()) {
          // Populate both Value and Values (UI will determine which is required
          // for each algorithm type)
          if (algorithmExecution.getProperties()
              .get(param.getFieldName()) != null) {
            if (param.getType().equals(AlgorithmParameter.Type.MULTI)) {
              param.setValues(
                  new ArrayList<String>(Arrays.asList(algorithmExecution
                      .getProperties().get(param.getFieldName()).split(";"))));
            } else {
              param.setValue(
                  algorithmExecution.getProperties().get(param.getFieldName()));
            }
          }
        }
      }

      // fix state where server crash caused a process failure
      checkBadState(processExecution, projectId, processService);

      return processExecution;
    } catch (Exception e) {
      handleException(e, "trying to get a process execution");
      return null;
    } finally {
      processService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @POST
  @Path("/execution/find")
  @ApiOperation(value = "Find processExecutions", notes = "Find processExecutions", response = ProcessExecutionListJpa.class)
  public ProcessExecutionList findProcessExecutions(
    @ApiParam(value = "Project id, e.g. 12345", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "The query string", required = false) @QueryParam("query") String query,
    @ApiParam(value = "The paging/sorting/filtering parameter", required = false) PfsParameterJpa pfs,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Process): /execution/find?projectId=" + projectId
            + "&query=" + query + " for user " + authToken + ", " + pfs);

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

  /* see superclass */
  @Override
  @GET
  @Path("/executing")
  @ApiOperation(value = "Find currently executing processes", notes = "Find currently executing processes", response = ProcessExecutionListJpa.class)
  public ProcessExecutionList findCurrentlyExecutingProcesses(
    @ApiParam(value = "Project id, e.g. 12345", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Authorization token, e.g. 'author1'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Process): /executing?projectId=" + projectId
            + " for user " + authToken);

    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName =
          authorizeProject(processService, projectId, securityService,
              authToken, "finding process executions", UserRole.AUTHOR);
      processService.setLastModifiedBy(userName);

      return findCurrentlyExecutingHelper(projectId, processService);

    } catch (Exception e) {
      handleException(e, "trying to find process executions");
      return null;
    } finally {
      processService.close();
      securityService.close();
    }
  }

  /**
   * Find currently executing helper.
   *
   * @param projectId the project id
   * @param processService the process service
   * @return the process execution list
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  public ProcessExecutionList findCurrentlyExecutingHelper(Long projectId,
    ProcessService processService) throws Exception {
    final ProcessExecutionList processExecutions =
        processService.findProcessExecutions(projectId,
            "startDate:[* TO *] AND NOT failDate:[* TO *] AND NOT finishDate:[* TO *] AND NOT stopDate:[* TO *]",
            null);

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
  }

  /* see superclass */
  @Override
  @DELETE
  @Path("/execution/{id}")
  @ApiOperation(value = "Remove process execution", notes = "Removes the processExecution with the specified id")
  public void removeProcessExecution(
    @ApiParam(value = "Project id, e.g. 12345", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "ProcessExecution id, e.g. 3", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Cascade, e.g. true", required = true) @QueryParam("cascade") Boolean cascade,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Process): /execution/" + id + "?projectId="
            + projectId + ((cascade != null && cascade) ? "&cascade=true" : "")
            + " for user " + authToken);

    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName =
          authorizeProject(processService, projectId, securityService,
              authToken, "remove process execution", UserRole.ADMINISTRATOR);
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
      handleException(e, "trying to remove a process execution");
    } finally {
      processService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @PUT
  @Path("/config/algo")
  @ApiOperation(value = "Add new algorithm config", notes = "Creates a new algorithm config", response = AlgorithmConfigJpa.class)
  public AlgorithmConfig addAlgorithmConfig(
    @ApiParam(value = "Project id, e.g. 12345", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Process id, e.g. 12345", required = true) @QueryParam("processId") Long processId,
    @ApiParam(value = "AlgorithmConfig, as POST data", required = true) AlgorithmConfigJpa config,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Process): /config/algo?projectId=" + projectId
            + "&processId=" + processId + " for user " + authToken + ", "
            + config);

    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName =
          authorizeProject(processService, projectId, securityService,
              authToken, "adding a process config", UserRole.ADMINISTRATOR);
      processService.setLastModifiedBy(userName);

      final ProcessConfigJpa process =
          (ProcessConfigJpa) processService.getProcessConfig(processId);

      // Re-add processConfig to algorithm config (it does not make it intact
      // through XML)
      config.setProcess(process);

      // Load project
      final Project project = processService.getProject(projectId);
      project.setLastModifiedBy(userName);

      // Re-add project to algorithm config (it does not make it intact through
      // XML)
      config.setProject(project);

      // Verify that passed projectId matches ID of the algorithm config's
      // project
      verifyProject(config, projectId);

      // Populate the algorithm's properties based on its parameters' values.
      for (final AlgorithmParameter param : config.getParameters()) {
        // Note: map either Value OR Values (comma-delimited)
        if (!param.getValues().isEmpty()) {
          config.getProperties().put(param.getFieldName(),
              StringUtils.join(param.getValues(), ';'));
        } else if (!ConfigUtility.isEmpty(param.getValue())) {
          config.getProperties().put(param.getFieldName(), param.getValue());
        }
      }

      // Add algorithm config
      final AlgorithmConfig newAlgo = processService.addAlgorithmConfig(config);

      // If new algorithm config has an associated processConfig,
      // add the algorithm to it and update
      if (process != null) {
        // Add algorithm config to processConfig
        process.getSteps().add(newAlgo);

        // update the processConfig
        processService.updateProcessConfig(process);
      }

      processService.addLogEntry(userName, projectId, config.getId(), null,
          null, "ADD algorithm config - " + config);

      return newAlgo;
    } catch (Exception e) {
      handleException(e, "trying to add an algorithm config");
      return null;
    } finally {
      processService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @POST
  @Path("/config/algo")
  @ApiOperation(value = "Update algorithm config", notes = "Updates the specified algorithm config")
  public void updateAlgorithmConfig(
    @ApiParam(value = "Project id, e.g. 12345", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Process id, e.g. 12345", required = true) @QueryParam("processId") Long processId,
    @ApiParam(value = "AlgorithmConfig, as POST data", required = true) AlgorithmConfigJpa algo,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Process): /config/algo?projectId=" + projectId
            + " for user " + authToken + ", " + algo);

    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName =
          authorizeProject(processService, projectId, securityService,
              authToken, "update algorithm config", UserRole.ADMINISTRATOR);
      processService.setLastModifiedBy(userName);

      // Load processConfig
      final ProcessConfigJpa process = (ProcessConfigJpa) processService
          .getProcessConfig(algo.getProcess().getId());

      // Re-add processConfig to algorithm config (it does not make it intact
      // through XML)
      algo.setProcess(process);

      // Load project
      Project project = processService.getProject(projectId);
      project.setLastModifiedBy(userName);

      // Re-add project to algorithm config (it does not make it intact through
      // XML)
      algo.setProject(project);

      // Verify that passed projectId matches ID of the algorithm config's
      // project
      verifyProject(algo, projectId);

      // ensure algorithm config exists
      final AlgorithmConfig origAlgo =
          processService.getAlgorithmConfig(algo.getId());
      if (origAlgo == null) {
        throw new Exception(
            "AlgorithmConfig " + algo.getId() + " does not exist");
      }

      // Populate the algorithm's properties based on its parameters' values.
      for (final AlgorithmParameter param : algo.getParameters()) {
        if (!param.getValues().isEmpty()) {
          algo.getProperties().put(param.getFieldName(),
              StringUtils.join(param.getValues(), ';'));
        } else if (!ConfigUtility.isEmpty(param.getValue())) {
          algo.getProperties().put(param.getFieldName(), param.getValue());
        }
      }

      // Update algorithm config
      processService.updateAlgorithmConfig(algo);

      processService.addLogEntry(userName, projectId, algo.getId(), null, null,
          "UPDATE algorithm config " + algo);

    } catch (Exception e) {
      handleException(e, "trying to update an algorithm config");
    } finally {
      processService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @POST
  @Path("/config/algo/validate")
  @ApiOperation(value = "Validate algorithm config", notes = "Validates the specified algorithm config properties.")
  public void validateAlgorithmConfig(
    @ApiParam(value = "Project id, e.g. 12345", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Process id, e.g. 34234", required = true) @QueryParam("processId") Long processId,
    @ApiParam(value = "AlgorithmConfig, as POST data", required = true) AlgorithmConfigJpa algo,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Process): /config/algo/validate?projectId="
            + projectId + " for user " + authToken + ", " + algo);
    final ProcessService processService = new ProcessServiceJpa();
    Algorithm algorithm = null;
    try {
      final String userName =
          authorizeProject(processService, projectId, securityService,
              authToken, "adding a process config", UserRole.ADMINISTRATOR);
      processService.setLastModifiedBy(userName);

      // Populate the algorithm's properties based on its parameters' values.
      for (final AlgorithmParameter param : algo.getParameters()) {
        // Note: map either Value OR Values (comma-delimited)
        if (!param.getValues().isEmpty()) {
          algo.getProperties().put(param.getFieldName(),
              StringUtils.join(param.getValues(), ','));
        } else if (!ConfigUtility.isEmpty(param.getValue())) {
          algo.getProperties().put(param.getFieldName(), param.getValue());
        }
      }

      algorithm = processService.getAlgorithmInstance(algo.getAlgorithmKey());
      if (algorithm == null) {
        throw new LocalException(
            "Missing algorithm for key " + algo.getAlgorithmKey());
      }
      algorithm.setProject(processService.getProject(projectId));
      final Properties p = new Properties();
      p.putAll(algo.getProperties());
      algorithm.checkProperties(p);

    } catch (Exception e) {
      handleException(e, "trying to validate algorithm config");
    } finally {
      if (algorithm != null) {
        algorithm.close();
      }
      processService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @DELETE
  @Path("/config/algo/{id}")
  @ApiOperation(value = "Remove algorithm config", notes = "Removes the algorithm config with the specified id")
  public void removeAlgorithmConfig(
    @ApiParam(value = "Project id, e.g. 12345", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "AlgorithmConfig id, e.g. 3", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful call (Process): /config/algo/"
        + id + "?projectId=" + projectId + " for user " + authToken);

    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName =
          authorizeProject(processService, projectId, securityService,
              authToken, "remove algorithm config", UserRole.ADMINISTRATOR);
      processService.setLastModifiedBy(userName);

      // Load algorithm config object
      final AlgorithmConfig algo = processService.getAlgorithmConfig(id);

      // ensure algorithm config exists
      if (algo == null) {
        throw new Exception("Algorithm config " + id + " does not exist");
      }

      // Verify that passed projectId matches ID of the algorithm config's
      // project
      verifyProject(algo, projectId);

      // If the algorithm config has an associated processConfig,
      // remove the algorithm from it and update
      final ProcessConfig process = algo.getProcess();
      if (process != null) {
        // Remove algorithm config from processConfig
        process.getSteps().remove(algo);

      } else {
        throw new Exception("Process config is unexpectedly null");
      }

      // Remove algorithm config
      processService.removeAlgorithmConfig(id);
      // update the processConfig
      processService.updateProcessConfig(process);

      processService.addLogEntry(userName, projectId, id, null, null,
          "REMOVE algorithm config " + id);

    } catch (Exception e) {
      handleException(e, "trying to remove an algorithm config");
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
    Logger.getLogger(getClass()).info("RESTful call (Process): /config/algo/"
        + id + "?projectId=" + projectId + " for user " + authToken);

    final ProcessService processService = new ProcessServiceJpa();
    Algorithm instance = null;
    try {
      final String userName =
          authorizeProject(processService, projectId, securityService,
              authToken, "getting the algorithm config", UserRole.AUTHOR);
      processService.setLastModifiedBy(userName);

      // Load algorithm config object
      final AlgorithmConfig algo = processService.getAlgorithmConfig(id);

      if (algo == null) {
        return algo;
      }

      // Verify that passed projectId matches ID of the algorithm config's
      // project
      verifyProject(algo, projectId);

      // Populate the parameters based on its properties' values.
      instance = processService.getAlgorithmInstance(algo.getAlgorithmKey());
      instance.setProject(processService.getProject(projectId));
      instance.setProcess(new ProcessExecutionJpa(algo.getProcess()));
      algo.setParameters(instance.getParameters());
      for (final AlgorithmParameter param : algo.getParameters()) {
        // Populate both Value and Values (UI will determine which is required
        // for each algorithm type)
        if (algo.getProperties().get(param.getFieldName()) != null) {
          if (param.getType().equals(AlgorithmParameter.Type.MULTI)) {
            param.setValues(new ArrayList<String>(Arrays.asList(
                algo.getProperties().get(param.getFieldName()).split(";"))));
          } else {
            param.setValue(algo.getProperties().get(param.getFieldName()));
          }
        }
      }

      return algo;
    } catch (Exception e) {
      handleException(e, "trying to get a algorithm config");
      return null;
    } finally {
      if (instance != null) {
        instance.close();
      }
      processService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/algo/{type:insertion|maintenance|release|report}")
  @ApiOperation(value = "Get all algorithms", notes = "Gets the algorithms for the specified type", response = KeyValuePairList.class)
  public KeyValuePairList getAlgorithmsForType(
    @ApiParam(value = "Project id, e.g. 12345", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "The type, e.g. insertion, maintenance, release, report", required = true) @PathParam("type") String type,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful call (Process): /algo/" + type
        + "?projectId=" + projectId + " for user " + authToken);

    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName = authorizeProject(processService, projectId,
          securityService, authToken, "getting the " + type + " algorithms",
          UserRole.AUTHOR);
      processService.setLastModifiedBy(userName);

      return processService.getAlgorithmsForType(type);

    } catch (Exception e) {
      handleException(e, "trying to get the " + type + " algorithms");
      return null;
    } finally {
      processService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/config/{id}/prepare")
  @Produces("text/plain")
  @ApiOperation(value = "Prepare a process for execution", notes = "Prepare the specified process configuration for execution", response = Long.class)
  public Long prepareProcess(
    @ApiParam(value = "Project id, e.g. 12345", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Process Config id, e.g. 3", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful call (Process): /config/" + id
        + "/execute?projectId=" + projectId + " for user " + authToken);

    final ProcessService processService = new ProcessServiceJpa();

    Long executionId = null;

    try {
      final String userName =
          authorizeProject(processService, projectId, securityService,
              authToken, "execute process config", UserRole.ADMINISTRATOR);
      processService.setLastModifiedBy(userName);

      // Load processConfig object
      final ProcessConfig process = processService.getProcessConfig(id);

      // Make sure processConfig exists
      if (process == null) {
        throw new Exception("ProcessConfig " + id + " does not exist");
      }

      // Verify that passed projectId matches ID of the processConfig's project
      verifyProject(process, projectId);

      // Make sure this processConfig is not already running
      for (final ProcessExecution exec : findCurrentlyExecutingHelper(projectId,
          processService).getObjects()) {
        if (exec.getProcessConfigId().equals(process.getId())) {
          throw new LocalException(
              "There is already a currently running execution of process "
                  + process.getId());
        }
      }

      // Create and set up a new process Execution
      final ProcessExecution execution = new ProcessExecutionJpa(process);
      // No start date yet
      execution.setWorkId(UUID.randomUUID().toString());
      execution.setSteps(new ArrayList<>());
      final ProcessExecution newExecution =
          processService.addProcessExecution(execution);
      executionId = newExecution.getId();

      // Log the prepare
      processService.addLogEntry(userName, projectId, newExecution.getId(),
          null, newExecution.getWorkId(),
          "PREPARE processConfig for execution - " + process);

      // Always return the execution id
      return executionId;

    } catch (

    Exception e) {
      handleException(e, "trying to preapre a process for execution");
    } finally {
      processService.close();
      securityService.close();
    }
    return executionId;
  }

  /* see superclass */
  @Override
  @GET
  @Path("/execution/{processId}/execute")
  @Produces("text/plain")
  @ApiOperation(value = "Execute a process", notes = "Execute the specified process", response = Long.class)
  public Long executeProcess(
    @ApiParam(value = "Project id, e.g. 12345", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Process Execution id, e.g. 3", required = true) @PathParam("processId") Long processId,
    @ApiParam(value = "Background, e.g. true", required = true) @QueryParam("background") Boolean background,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Process): /execution/" + processId
            + "/execute?projectId=" + projectId
            + ((background != null && background) ? "&background=true" : "")
            + " for user " + authToken);

    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName =
          authorizeProject(processService, projectId, securityService,
              authToken, "execute process", UserRole.ADMINISTRATOR);
      processService.setLastModifiedBy(userName);

      // Load processConfig object
      final ProcessExecution process =
          processService.getProcessExecution(processId);

      // Make sure processConfig exists
      if (process == null) {
        throw new LocalException(
            "Process execution " + processId + " does not exist");
      }

      // Verify that passed projectId matches ID of the processConfig's project
      verifyProject(process, projectId);

      // Make sure this processConfig is not already running
      for (final ProcessExecution exec : findCurrentlyExecutingHelper(projectId,
          processService).getObjects()) {
        if (exec.getProcessConfigId().equals(process.getId())) {
          throw new LocalException(
              "There is already a currently running execution of process "
                  + process.getId());
        }
      }

      // If process has any execution steps, then restart needs to be called
      if (process.getSteps().size() > 0 || process.getStartDate() != null) {
        throw new LocalException(
            "Process unexpectedly has been started, use restart instead.");
      }
      // Create and set up a new process Execution
      process.setStartDate(new Date());
      process.setSteps(new ArrayList<>());
      processService.updateProcessExecution(process);

      // Create a thread and run the process
      runProcessAsThread(projectId, process.getProcessConfigId(),
          process.getId(),
          process.getTerminology() + "_" + process.getVersion(), background,
          false, null);

      // Always return the execution id
      return processId;

    } catch (

    Exception e) {
      handleException(e, "trying to execute a process");
    } finally {
      processService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @GET
  @Path("/execution/{id}/restart")
  @Produces("text/plain")
  @ApiOperation(value = "Execute a process configuration", notes = "Execute the specified process configuration")
  public Long restartProcess(
    @ApiParam(value = "Project id, e.g. 12345", required = true) @QueryParam("projectId") Long projectId,
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
              authToken, "restart process execution", UserRole.ADMINISTRATOR);
      processService.setLastModifiedBy(userName);

      // Load processExecution object
      final ProcessExecution processExecution =
          processService.getProcessExecution(id);

      // Make sure processExecution exists
      if (processExecution == null) {
        throw new LocalException("ProcessExecution " + id + " does not exist");
      }

      // Make sure the processExecution isn't already running
      for (final ProcessExecution exec : findCurrentlyExecutingHelper(projectId,
          processService).getObjects()) {
        if (exec.getId().equals(processExecution.getId())) {
          throw new LocalException("Process execution "
              + processExecution.getId() + " is already currently running");
        }
      }

      // If process has any execution steps, then restart needs to be called
      if (processExecution.getStartDate() == null) {
        throw new LocalException(
            "Process has not yet been started, use execute instead.");
      }

      // Load the processExecution's config
      final ProcessConfig processConfig = processService
          .getProcessConfig(processExecution.getProcessConfigId());

      // Verify that passed projectId matches ID of the processConfig's project
      verifyProject(processConfig, projectId);

      // Create a thread and run the process
      runProcessAsThread(projectId, processConfig.getId(),
          processExecution.getId(), processExecution.getTerminology() + "_"
              + processExecution.getVersion(),
          background, true, null);

      return id;
    } catch (Exception e) {
      handleException(e, "trying to restart a process execution");
    } finally {
      processService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @GET
  @Path("/execution/{id}/step")
  @Produces("text/plain")
  @ApiOperation(value = "Execute a step of  a process configuration", notes = "Execute a step in either direction for a specified process configuration")
  public Long stepProcess(
    @ApiParam(value = "Project id, e.g. 12345", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Process Execution id, e.g. 3", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Step, e.g. -1 to go back, 1 to go forward one step", required = true) @QueryParam("step") Integer step,
    @ApiParam(value = "Background, e.g. true", required = true) @QueryParam("background") Boolean background,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Process): /execution/" + id + "/step?projectId="
            + projectId
            + ((background != null && background) ? "&background=true" : "")
            + ", step = " + step);

    final ProcessService processService = new ProcessServiceJpa();

    try {
      final String userName =
          authorizeProject(processService, projectId, securityService,
              authToken, "restart process execution", UserRole.ADMINISTRATOR);
      processService.setLastModifiedBy(userName);

      // do nothing on a null step.
      if (step == null) {
        return null;
      }

      // Load processExecution object
      final ProcessExecution processExecution =
          processService.getProcessExecution(id);

      // Make sure processExecution exists
      if (processExecution == null) {
        throw new LocalException("ProcessExecution " + id + " does not exist");
      }

      // Make sure the processExecution isn't already running
      for (final ProcessExecution exec : findCurrentlyExecutingHelper(projectId,
          processService).getObjects()) {
        if (exec.getId().equals(processExecution.getId())) {
          throw new LocalException("Process execution "
              + processExecution.getId() + " is already currently running");
        }
      }

      if (step != null && step < -1) {
        throw new LocalException("Steps can only be revered one at a time.");
      }

      if (processExecution.getStartDate() == null) {
        processExecution.setStartDate(new Date());
        processService.updateProcessExecution(processExecution);
      }
      // Load the processExecution's config
      final ProcessConfig processConfig = processService
          .getProcessConfig(processExecution.getProcessConfigId());

      // Verify that passed projectId matches ID of the processConfig's project
      verifyProject(processConfig, projectId);

      // Create a thread and run the process
      runProcessAsThread(projectId, processConfig.getId(),
          processExecution.getId(),
          processExecution.getTerminology() + "_"
              + processExecution.getVersion(),
          background, processExecution.getSteps().size() > 0, step);

      return id;
    } catch (Exception e) {
      handleException(e, "trying to restart a process execution");
    } finally {
      processService.close();
      securityService.close();
    }
    return null;
  }

  /* see superclass */
  @Override
  @GET
  @Path("/execution/{id}/cancel")
  @Produces("text/plain")
  @ApiOperation(value = "Cancel a running process execution", notes = "Execute the specified process configuration")
  public Long cancelProcess(
    @ApiParam(value = "Project id, e.g. 12345", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Process Execution id, e.g. 3", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful call (Process): /execution/" + id
        + "/cancel?projectId=" + projectId + " for user " + authToken);

    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName =
          authorizeProject(processService, projectId, securityService,
              authToken, "cancel process execution", UserRole.ADMINISTRATOR);
      processService.setLastModifiedBy(userName);

      final ProcessExecution processExecution =
          processService.getProcessExecution(id);
      if (processExecution == null) {
        throw new LocalException("Process execution does not exist for " + id);
      }
      // Verify the project
      verifyProject(processExecution, projectId);

      // Find the algorithm and call cancel on it
      if (processAlgorithmMap.containsKey(id)) {
        // this will throw a CancelException which will clean up all the maps
        // and mark everything as cancelled
        processAlgorithmMap.get(id).cancel();
        // Do not close - let run thread close it
        // processAlgorithmMap.get(id).close();
      }

      return id;

    } catch (Exception e) {
      handleException(e, "trying to cancel a process execution");
    } finally {
      processService.close();
      securityService.close();
    }
    return null;
  }

  /**
   * Check bad state.
   *
   * @param processExecution the process execution
   * @param projectId the project id
   * @param processService the process service
   * @throws Exception the exception
   */
  private void checkBadState(ProcessExecution processExecution, Long projectId,
    ProcessService processService) throws Exception {
    // Make sure this process execution is running
    boolean processRunning = false;
    for (final ProcessExecution exec : findCurrentlyExecutingHelper(projectId,
        processService).getObjects()) {
      if (exec.getId().equals(processExecution.getId())) {
        processRunning = true;
        break;
      }
    }
    if (!processRunning) {

      // IF the process thinks it is still running, mark it as failed and save
      // that change
      if (processExecution.getStartDate() != null
          && processExecution.getStopDate() == null
          && processExecution.getFailDate() == null
          && processExecution.getFinishDate() == null) {
        processExecution.setFailDate(new Date());
        processService.updateProcessExecution(processExecution);

        for (final AlgorithmExecution algoExec : processExecution.getSteps()) {
          if (algoExec.getStartDate() != null && algoExec.getFailDate() == null
              && algoExec.getFinishDate() == null) {
            algoExec.setFailDate(new Date());
            processService.updateAlgorithmExecution(algoExec);
          }
        }
      }
    }
  }

  /* see superclass */
  @GET
  @Path("/{id}/progress")
  @Produces("text/plain")
  @ApiOperation(value = "Find progress of specified executing process", notes = "Find progress of specified executing process", response = Integer.class)
  @Override
  public Integer getProcessProgress(
    @ApiParam(value = "Project id, e.g. 12345", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Process execution internal id, e.g. 2", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug("RESTful call POST (Process): /" + id
        + "/progress?projectId=" + projectId + " for user " + authToken);

    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName =
          authorizeProject(processService, projectId, securityService,
              authToken, "finding process progress", UserRole.AUTHOR);
      processService.setLastModifiedBy(userName);

      ProcessExecution processExecution = null;
      synchronized (lock) {
        processExecution = processService.getProcessExecution(id);
      }
      if (processExecution == null) {
        return -1;
      }
      // If process has already completed successfully, return 100
      if (processExecution != null && processExecution.getFinishDate() != null
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
    @ApiParam(value = "Project id, e.g. 12345", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Algorithm execution internal id, e.g. 2", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug("RESTful call POST (Process): /algo/"
        + id + "/progress?projectId=" + projectId + " for user " + authToken);

    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName =
          authorizeProject(processService, projectId, securityService,
              authToken, "finding algorithm progress", UserRole.AUTHOR);
      processService.setLastModifiedBy(userName);

      final AlgorithmExecution algoExecution =
          processService.getAlgorithmExecution(id);
      // If algorithm has already completed successfully, return 100
      if (algoExecution == null) {
        return -1;
      }
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
   * @param step the step
   * @throws Exception the exception
   */
  private void runProcessAsThread(Long projectId, Long processConfigId,
    Long processExecutionId, String userName, Boolean background,
    Boolean restart, Integer step) throws Exception {

    // Set up vars for thread
    final Exception[] exceptions = new Exception[1];
    final boolean handleException = background != null && background;

    final Thread t = new Thread(new Runnable() {

      /* see superclass */
      @SuppressWarnings("cast")
      @Override
      public void run() {
        ProcessServiceJpa processService = null;
        // Declare execution so it can be accessed
        ProcessExecution processExecution = null;
        AlgorithmExecution algorithmExecution = null;
        Algorithm algorithm = null;
        Boolean firstRestartedAlgorithm = false;
        try {
          processService = new ProcessServiceJpa();
          processService.setLastModifiedBy(userName);

          final ProcessConfig processConfig =
              processService.getProcessConfig(processConfigId);
          processExecution =
              processService.getProcessExecution(processExecutionId);

          // Clear out the finish and fail date fields (these could have been
          // populated from a previous run)
          processExecution.setStopDate(null);
          processExecution.setFailDate(null);
          processExecution.setFinishDate(null);
          processService.updateProcessExecution(processExecution);

          // Log starting a process if no algorithm executions
          if (processExecution.getSteps().size() == 0 && !restart) {
            processService.addLogEntry(processExecution.getProject().getId(),
                processExecution.getLastModifiedBy(),
                processExecution.getTerminology(),
                processExecution.getVersion(), null,
                processExecution.getWorkId(),
                "STARTING PROCESS " + processExecution.getId() + ", "
                    + processExecution.getName() + "\n\t  project = "
                    + processExecution.getProject().getId() + ", "
                    + processExecution.getProject().getName());
          }

          // Set initial progress to zero and count the number of steps to
          // execute
          lookupPeProgressMap.put(processExecution.getId(), 0);

          final int enabledSteps = processConfig.getSteps().stream()
              .filter(ac -> ac.isEnabled()).collect(Collectors.toList()).size();

          // Start step counter at 0
          int stepCt = 0;

          // If this is a restart, find the steps that already ran
          final List<Long> previouslyCompletedAlgorithmIds = new ArrayList<>();
          AlgorithmExecution lastCompletedAlgorithm = null;
          AlgorithmExecution algorithmToRestart = null;
          if (restart) {
            final List<AlgorithmExecution> previouslyStartedAlgorithms =
                processExecution.getSteps();
            processExecution.setWarning(false);
            int warningCt = 0;
            for (final AlgorithmExecution ae : previouslyStartedAlgorithms) {
              // Set warning
              if (ae.isWarning()) {
                warningCt++;
                processExecution.setWarning(true);
              }

              // If the algorithm finished, save the algorithm configId (so it
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

              // Track the last completed algorithm
              lastCompletedAlgorithm = ae;
            }

            // If were stepping back and the lastCompletedAlgorithm is
            // finished
            // Remove it from "previously completed algorithm ids"
            if (step != null && step < 0
                && lastCompletedAlgorithm.getFinishDate() != null) {
              previouslyCompletedAlgorithmIds
                  .remove(lastCompletedAlgorithm.getAlgorithmConfigId());
              algorithmToRestart = lastCompletedAlgorithm;
              firstRestartedAlgorithm = true;
              // If there was only one warning, and it was the step being
              // undone
              // set warning back to false;
              if (lastCompletedAlgorithm.isWarning() && warningCt == 1) {
                processExecution.setWarning(false);
              }
            }

            // Update the processExecution progress and step-count
            stepCt = previouslyCompletedAlgorithmIds.size();
            lookupPeProgressMap.put(processExecution.getId(),
                (int) ((100 * stepCt) / enabledSteps));
          }

          // Iterate through algorithm configs
          int ct = 0;
          for (final AlgorithmConfig algo : processConfig.getSteps()) {
            ct++;

            // Skip steps that are not enabled
            if (!algo.isEnabled()) {
              continue;
            }

            // Skip steps that completed on a previous run (if any)
            if (restart
                && previouslyCompletedAlgorithmIds.contains(algo.getId())) {
              continue;
            }

            // If this is a restart, use the loaded algorithm Execution
            if (algorithmToRestart != null && algorithmToRestart
                .getAlgorithmConfigId().equals(algo.getId())) {
              algorithmExecution = algorithmToRestart;
              algorithmExecution.setFailDate(null);
              algorithmExecution.setFinishDate(null);
              processService.updateAlgorithmExecution(algorithmExecution);
            }
            // Otherwise, instantiate and configure the algorithm execution
            else {
              algorithmExecution = new AlgorithmExecutionJpa(algo);
              // Create a copy of the properties to add to algorithmExecution
              // (using same object causes shared references to a collection
              // error
              algorithmExecution.setProperties(
                  new HashMap<String, String>(algo.getProperties()));
              algorithmExecution.setProcess(processExecution);
              algorithmExecution.setActivityId(UUID.randomUUID().toString());
              algorithmExecution.setStartDate(new Date());

              synchronized (lock) {
                algorithmExecution =
                    processService.addAlgorithmExecution(algorithmExecution);
                // Add the execution to the process
                processExecution.getSteps().add(algorithmExecution);
                processService.updateProcessExecution(processExecution);
              }
            }

            // Create and configure the algorithm
            algorithm = processService
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
            // If a previous run is already in the map, make sure to close it
            if (processAlgorithmMap.containsKey(processExecution.getId())) {
              processAlgorithmMap.get(processExecution.getId()).close();
            }
            processAlgorithmMap.put(processExecution.getId(), algorithm);

            // Check preconditions (if this is not an unstep)
            if (!(step != null && step < 0)) {
              final ValidationResult result = algorithm.checkPreconditions();
              if (!result.isValid()) {
                throw new LocalException(
                    "Algorithm " + algorithmExecution.getId()
                        + " failed preconditions: " + FieldedStringTokenizer
                            .join(new ArrayList<>(result.getErrors()), "\n"));
              }
            }

            final Long aeId = algorithmExecution.getId();
            final int currentCt = stepCt;

            // algorithmExecution needs to be recast as final, so it can be
            // modified by updateProgress
            final AlgorithmExecution finalAlgorithmExecution =
                algorithmExecution;
            final ProcessExecution processExecution2 = processExecution;

            algorithm.addProgressListener(new ProgressListener() {
              @Override
              public void updateProgress(ProgressEvent processEvent) {
                if (processEvent.isWarning()) {
                  finalAlgorithmExecution.setWarning(true);
                  processExecution2.setWarning(true);
                  return;
                }
                lookupAeProgressMap.put(aeId, processEvent.getPercent());

                // pe progress is the current progress plus the scaled
                // progress of the ae
                lookupPeProgressMap.put(processExecution2.getId(),
                    (int) ((100 * currentCt) / enabledSteps)
                        + (int) (processEvent.getPercent() / enabledSteps));

              }
            });

            // Start progress at 0 for the algorithm
            lookupAeProgressMap.put(algorithmExecution.getId(), 0);

            // If we're in restart mode, and if this is the First algorithm
            // we're running, reset the algorithm.
            if (restart && firstRestartedAlgorithm
                && algorithmToRestart != null) {
              algorithm.reset();
              // Commit and reset transaction
              algorithm.commitClearBegin();

              // Don't reset on any later algorithms
              firstRestartedAlgorithm = false;
            }

            // If stepping back, remove the algorithm execution
            if (step != null && step < 0) {

              // Remove the algorithm execution from the process
              processExecution.getSteps().remove(algorithmExecution);
              processService.updateProcessExecution(processExecution);
              processService
                  .removeAlgorithmExecution(algorithmExecution.getId());
            }

            else {

              // Execute algorithm
              algorithm.compute();

              // Commit any changes the algorithm wants to make
              algorithm.commit();

              // Take the number of steps completed times 100 and divided by
              // the
              // total number of steps
              lookupPeProgressMap.put(processExecution.getId(),
                  (int) ((100 * ++stepCt) / enabledSteps));

              // algorithm has finished
              algorithmExecution.setFinishDate(new Date());
              processService.updateAlgorithmExecution(algorithmExecution);

              // Update the process execution (in case anything has been done
              // to
              // it by the algorithm)
              processService.updateProcessExecution(processExecution);

            }

            // Mark algorithm as finished
            lookupAeProgressMap.remove(algorithmExecution.getId());
            processAlgorithmMap.remove(processExecution.getId());

            // close the algorithm
            algorithm.close();

            // If this is a "step" operation, we're done.
            if (step != null) {
              processExecution.setStopDate(new Date());
              processService.updateProcessExecution(processExecution);
              break;
            }

          } // end "for algorithm config"

          // Check if process has finished, mark it so
          if (ct == processConfig.getSteps().size()
              && (step == null || step > 0)) {

            // Log starting a process
            processService.addLogEntry(processExecution.getProject().getId(),
                processExecution.getLastModifiedBy(),
                processExecution.getTerminology(),
                processExecution.getVersion(), null,
                processExecution.getWorkId(),
                "FINISHED PROCESS " + processExecution.getId() + ", "
                    + processExecution.getName());

            processExecution.setStopDate(null);
            processExecution.setFinishDate(new Date());
            processService.updateProcessExecution(processExecution);
            processService.saveLogToFile(projectId, processExecution);

            // Mark process as finished
            lookupPeProgressMap.remove(processExecution.getId());

            // Send email notifying about successful completion
            final String recipients = processExecution.getFeedbackEmail();

            if (!ConfigUtility.isEmpty(recipients)) {
              final Properties config = ConfigUtility.getConfigProperties();
              String from;
              if (config.containsKey("mail.smtp.from")) {
                from = config.getProperty("mail.smtp.from");
              } else {
                from = config.getProperty("mail.smtp.user");
              }
              ConfigUtility.sendEmail(
                  "[Terminology Server] Run Complete for Process: "
                      + processExecution.getName(),
                  from, recipients, processService.getProcessLog(projectId,
                      processExecutionId, null),
                  config);
            }
          }

        } catch (Exception e) {
          // e.printStackTrace();
          exceptions[0] = e;

          // Mark algorithm and process as failed
          try {

            // set cancel conditions if cancel was used.
            algorithmExecution.setFailDate(new Date());
            if (e instanceof CancelException) {

              processService.addLogEntry(processExecution.getProject().getId(),
                  processExecution.getLastModifiedBy(),
                  processExecution.getTerminology(),
                  processExecution.getVersion(),
                  algorithmExecution.getActivityId(),
                  processExecution.getWorkId(),
                  "CANCELLED " + algorithmExecution.getName());
              Logger.getLogger(getClass())
                  .info("CANCELLED " + algorithmExecution.getName());

              algorithmExecution.setFinishDate(new Date());
              processExecution.setFinishDate(new Date());
            } else {
              processService.addLogEntry(processExecution.getProject().getId(),
                  processExecution.getLastModifiedBy(),
                  processExecution.getTerminology(),
                  processExecution.getVersion(),
                  algorithmExecution.getActivityId(),
                  processExecution.getWorkId(),
                  "ERROR " + "Unexpected problem - " + e.getMessage());
            }
            processService.updateAlgorithmExecution(algorithmExecution);

            processExecution.setFailDate(new Date());
            processService.updateProcessExecution(processExecution);
            processService.saveLogToFile(projectId, processExecution);

            // Remove process and algorithm from the maps
            if (processAlgorithmMap.containsKey(processExecution.getId())) {
              processAlgorithmMap.get(processExecution.getId()).close();
            }
            processAlgorithmMap.remove(processExecutionId);
            lookupPeProgressMap.remove(processExecutionId);
            lookupAeProgressMap.remove(algorithmExecution.getId());

            // close the algorithm
            if (algorithm != null) {
              algorithm.close();
            }

          } catch (Exception ex) {
            handleException(ex, "trying to update execution info");
          }

          // Send email notifying about failed run
          String recipients = processExecution.getFeedbackEmail();

          if (!ConfigUtility.isEmpty(recipients)) {
            try {
              final Properties config = ConfigUtility.getConfigProperties();
              String from = null;
              if (config.containsKey("mail.smtp.from")) {
                from = config.getProperty("mail.smtp.from");
              } else {
                from = config.getProperty("mail.smtp.user");
              }
              ConfigUtility.sendEmail(
                  "[Terminology Server] Process Run Failed for Process: "
                      + processExecution.getName() + " at Algorithm step: "
                      + algorithmExecution.getName(),
                  from, recipients, processService.getProcessLog(projectId,
                      processExecutionId, null),
                  config);
            } catch (Exception e2) {
              e2.printStackTrace();
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
            e.printStackTrace();
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
    @ApiParam(value = "Project id, e.g. 12345", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Process execution internal id, e.g. 2", required = true) @PathParam("processExecutionId") Long processExecutionId,
    @ApiParam(value = "Query, e.g. 2", required = true) @QueryParam("query") String query,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call (Process): /" + processExecutionId + "/log?projectId="
            + projectId + ", " + query + " for user " + authToken);

    if (projectId == null) {
      throw new Exception("Error: project id must be set.");
    }

    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName = authorizeProject(processService, projectId,
          securityService, authToken,
          "getting the process execution log entries", UserRole.AUTHOR);
      processService.setLastModifiedBy(userName);

      return processService.getProcessLog(projectId, processExecutionId, query);
    } catch (Exception e) {
      handleException(e, "trying to get the process execution log entries");
      return null;
    } finally {
      processService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @GET
  @Path("algo/{algorithmExecutionId}/log")
  @ApiOperation(value = "Get log entries of specified algorithm execution", notes = "Get log entries of specified algorithm execution", response = Integer.class)
  @Override
  public String getAlgorithmLog(
    @ApiParam(value = "Project id, e.g. 12345", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Algorithm execution internal id, e.g. 2", required = true) @PathParam("algorithmExecutionId") Long algorithmExecutionId,
    @ApiParam(value = "Query, e.g. 2", required = true) @QueryParam("query") String query,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Process): /algo/" + algorithmExecutionId
            + "/log?projectId=" + projectId + ", " + query + " for user "
            + authToken);

    if (projectId == null) {
      throw new Exception("Error: project id must be set.");
    }

    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName = authorizeProject(processService, projectId,
          securityService, authToken,
          "getting the algorithm execution log entries", UserRole.AUTHOR);
      processService.setLastModifiedBy(userName);

      return processService.getAlgorithmLog(projectId, algorithmExecutionId,
          query);

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
    @ApiParam(value = "Project id, e.g. 12345", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Process id, e.g. 12345", required = true) @QueryParam("processId") Long processId,
    @ApiParam(value = "Algorithm config key, e.g. MATRIXINT", required = true) @PathParam("key") String key,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Process): /config/algo/" + key + "/new?projectId="
            + projectId + "&processId=" + processId + " for user " + authToken);

    final ProcessService processService = new ProcessServiceJpa();
    Algorithm algorithm = null;
    try {
      final String userName = authorizeProject(processService, projectId,
          securityService, authToken, "adding a new algorithm config",
          UserRole.ADMINISTRATOR);
      processService.setLastModifiedBy(userName);

      // Load project
      final Project project = processService.getProject(projectId);
      final ProcessConfig process = processService.getProcessConfig(processId);
      algorithm = processService.getAlgorithmInstance(key);
      final AlgorithmConfig algo = new AlgorithmConfigJpa();
      algo.setProject(project);
      algo.setProcess(process);
      algo.setDescription(algorithm.getDescription());
      // Algorithm also needs project and process set so that getParameters will
      // function correctly
      algorithm.setProject(algo.getProject());
      algorithm.setProcess(new ProcessExecutionJpa(algo.getProcess()));
      algo.setParameters(algorithm.getParameters());
      return algo;

    } catch (Exception e) {
      handleException(e, "trying to return a new algorithm config");
      return null;
    } finally {
      if (algorithm != null) {
        algorithm.close();
      }
      processService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @SuppressWarnings({
      "unchecked", "rawtypes"
  })
  @Override
  @GET
  @Path("testquery")
  @Produces("text/plain")
  @ApiOperation(value = "Test query", notes = "Attempts to run a query and returns whether it works or not.")
  public Integer testQuery(
    @ApiParam(value = "Project id, e.g. 12345", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Process id, e.g. 12345", required = true) @QueryParam("processId") Long processId,
    @ApiParam(value = "Query Type, e.g. LUCENE", required = true) @QueryParam("queryTypeName") String queryTypeName,
    @ApiParam(value = "Query, e.g. select a.id from AtomJpa a", required = true) @QueryParam("query") String query,
    @ApiParam(value = "Object type name, e.g. AtomJpa", required = false) @QueryParam("objectTypeName") String objectTypeName,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Process): /testquery?projectId=" + projectId
            + "&processId=" + processId + ", " + query + " for user "
            + authToken);

    if (projectId == null) {
      throw new Exception("Error: project id must be set.");
    }
    if (processId == null) {
      throw new Exception("Error: process id must be set.");
    }
    if (ConfigUtility.isEmpty(queryTypeName)) {
      throw new Exception("Error: query type name must be set.");
    }
    if (ConfigUtility.isEmpty(query)) {
      throw new Exception("Error: query must be set.");
    }

    final ProcessService processService = new ProcessServiceJpa();
    try {
      final String userName = authorizeProject(processService, projectId,
          securityService, authToken, "testing a query", UserRole.AUTHOR);
      processService.setLastModifiedBy(userName);

      // If the object type isn't passed in, we can assign it based on the query
      // type
      String lobjectTypeName = objectTypeName;
      if (ConfigUtility.isEmpty(objectTypeName)) {
        if (queryTypeName.equals("LUCENE")) {
          lobjectTypeName = "ConceptJpa";
        } else if (queryTypeName.equals("SQL") || queryTypeName.equals("JQL")) {
          lobjectTypeName = "AtomJpa";
        } else {
          throw new LocalException(
              "Query Type " + queryTypeName + " is unsupported.");
        }
      }

      final ProcessConfig process = processService.getProcessConfig(processId);
      final String componentPath = AtomJpa.class.getName().substring(0,
          AtomJpa.class.getName().indexOf("AtomJpa"));
      Class clazz = Class.forName(componentPath + lobjectTypeName);

      // Generate parameters to pass into query executions
      Map<String, String> params = new HashMap<>();
      params.put("terminology", process.getTerminology());
      params.put("version", process.getVersion());
      params.put("projectTerminology",
          processService.getProject(projectId).getTerminology());
      params.put("projectVersion",
          processService.getProject(projectId).getVersion());

      final List<Long[]> componentIdArrays = new ArrayList<>();
      final List<Long> componentIds = new ArrayList<>();
      int exceptionCount = 0;

      try {
        componentIdArrays.addAll(processService.executeComponentIdPairQuery(
            query, QueryType.valueOf(queryTypeName), params, clazz));
      } catch (Exception e) {
        exceptionCount++;
      }
      // Only run single component query if the pairQuery returned an exception
      if (exceptionCount == 1) {
        try {
          componentIds.addAll(processService.executeSingleComponentIdQuery(
              query, QueryType.valueOf(queryTypeName), params, clazz));
        } catch (Exception e) {
          exceptionCount++;
        }
      }

      // If both of the queries threw exceptions, this query was malformed.
      if (exceptionCount == 2) {
        throw new LocalException("Query malformed: " + query);
      }
      // If either of the queries ran successfully, return the number of results
      else {
        return !componentIdArrays.isEmpty() ? componentIdArrays.size()
            : componentIds.size();
      }
    } finally {
      processService.close();
      securityService.close();
    }

  }

}
