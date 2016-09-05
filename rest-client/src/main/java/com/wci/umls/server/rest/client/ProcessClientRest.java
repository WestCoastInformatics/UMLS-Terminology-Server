/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.rest.client;

import java.net.URLEncoder;
import java.util.Properties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import org.apache.log4j.Logger;

import com.wci.umls.server.AlgorithmConfig;
import com.wci.umls.server.ProcessConfig;
import com.wci.umls.server.ProcessExecution;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.KeyValuePairList;
import com.wci.umls.server.helpers.ProcessConfigList;
import com.wci.umls.server.helpers.ProcessExecutionList;
import com.wci.umls.server.jpa.AlgorithmConfigJpa;
import com.wci.umls.server.jpa.ProcessConfigJpa;
import com.wci.umls.server.jpa.ProcessExecutionJpa;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.helpers.ProcessConfigListJpa;
import com.wci.umls.server.jpa.helpers.ProcessExecutionListJpa;
import com.wci.umls.server.jpa.services.rest.ProcessServiceRest;

/**
 * A client for connecting to a processClient REST service.
 */
public class ProcessClientRest extends RootClientRest
    implements ProcessServiceRest {

  /** The config. */
  private Properties config = null;

  /**
   * Instantiates a {@link ProcessClientRest} from the specified parameters.
   *
   * @param config the config
   */
  public ProcessClientRest(Properties config) {
    this.config = config;
  }

  /* see superclass */
  @Override
  public ProcessConfig addProcessConfig(Long projectId,
    ProcessConfigJpa processConfig, String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("ProcessConfig Client - add processConfig" + processConfig);

    final Client client = ClientBuilder.newClient();
    final WebTarget target = client.target(config.getProperty("base.url")
        + "/process/config/add" + "?projectId=" + projectId);

    final String processConfigString = ConfigUtility.getStringForGraph(
        processConfig == null ? new ProcessConfigJpa() : processConfig);

    final Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken)
        .put(Entity.xml(processConfigString));

    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(resultString);
    }

    // converting to object
    ProcessConfigJpa result =
        ConfigUtility.getGraphForString(resultString, ProcessConfigJpa.class);

    return result;
  }

  /* see superclass */
  @Override
  public void updateProcessConfig(Long projectId,
    ProcessConfigJpa processConfig, String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Process Client - update processConfig " + processConfig);
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/process/config/update" + "?projectId=" + projectId);

    String processConfigString = ConfigUtility.getStringForGraph(
        processConfig == null ? new ProcessConfigJpa() : processConfig);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken)
        .post(Entity.xml(processConfigString));

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing, successful
    } else {
      throw new Exception("Unexpected status - " + response.getStatus());
    }
  }

  /* see superclass */
  @Override
  public void removeProcessConfig(Long projectId, Long id, Boolean cascade,
    String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Process Client - remove processConfig " + id);
    validateNotEmpty(id, "id");
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/process/config/" + id + "/remove?projectId=" + projectId
        + (cascade ? "&cascade=true" : ""));

    if (id == null)
      return;

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).delete();

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing, successful
    } else {
      throw new Exception("Unexpected status - " + response.getStatus());
    }
  }

  /* see superclass */
  @Override
  public ProcessConfig getProcessConfig(Long projectId, Long id,
    String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Process Client - get processConfig " + id);
    validateNotEmpty(id, "id");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/process/config/" + id + "?projectId=" + projectId);

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    ProcessConfigJpa processConfig =
        ConfigUtility.getGraphForString(resultString, ProcessConfigJpa.class);
    return processConfig;
  }

  /* see superclass */
  @Override
  public ProcessConfigList findProcessConfigs(Long projectId, String query,
    PfsParameterJpa pfs, String authToken) throws Exception {

    Logger.getLogger(getClass())
        .debug("Project Client - find processConfigs " + query);

    validateNotEmpty(projectId, "projectId");

    final Client client = ClientBuilder.newClient();
    final WebTarget target = client.target(config.getProperty("base.url")
        + "/process/config" + "?projectId=" + projectId + "&query="
        + URLEncoder.encode(query == null ? "" : query, "UTF-8")
            .replaceAll("\\+", "%20"));
    final String pfsString = ConfigUtility
        .getStringForGraph(pfs == null ? new PfsParameterJpa() : pfs);
    final Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.xml(pfsString));

    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    return ConfigUtility.getGraphForString(resultString,
        ProcessConfigListJpa.class);

  }

  /* see superclass */
  @Override
  public ProcessExecution getProcessExecution(Long projectId, Long id,
    String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Process Client - get processExecution " + id);
    validateNotEmpty(id, "id");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/process/execution/" + id + "?projectId=" + projectId);

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    ProcessExecutionJpa processExecution = ConfigUtility
        .getGraphForString(resultString, ProcessExecutionJpa.class);
    return processExecution;
  }

  /* see superclass */
  @Override
  public ProcessExecutionList findProcessExecutions(Long projectId,
    String query, PfsParameterJpa pfs, String authToken) throws Exception {

    Logger.getLogger(getClass())
        .debug("Project Client - find processExecutions " + query);

    validateNotEmpty(projectId, "projectId");

    final Client client = ClientBuilder.newClient();
    final WebTarget target = client.target(config.getProperty("base.url")
        + "/process/execution" + "?projectId=" + projectId + "&query="
        + URLEncoder.encode(query == null ? "" : query, "UTF-8")
            .replaceAll("\\+", "%20"));
    final String pfsString = ConfigUtility
        .getStringForGraph(pfs == null ? new PfsParameterJpa() : pfs);
    final Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.xml(pfsString));

    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    return ConfigUtility.getGraphForString(resultString,
        ProcessExecutionListJpa.class);

  }

  /* see superclass */
  @Override
  public ProcessExecutionList findCurrentlyExecutingProcesses(Long projectId,
    String authToken) throws Exception {

    Logger.getLogger(getClass())
        .debug("Project Client - find currently executing processes ");

    validateNotEmpty(projectId, "projectId");

    final Client client = ClientBuilder.newClient();
    final WebTarget target = client.target(config.getProperty("base.url")
        + "/process/executing" + "?projectId=" + projectId);
    final Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    return ConfigUtility.getGraphForString(resultString,
        ProcessExecutionList.class);

  }

  /* see superclass */
  @Override
  public void removeProcessExecution(Long projectId, Long id, Boolean cascade,
    String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Process Client - remove processExecution " + id);
    validateNotEmpty(id, "id");
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/process/execution/" + id + "/remove?projectId=" + projectId
        + (cascade ? "&cascade=true" : ""));

    if (id == null)
      return;

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).delete();

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing, successful
    } else {
      throw new Exception("Unexpected status - " + response.getStatus());
    }
  }

  /* see superclass */
  @Override
  public AlgorithmConfig addAlgorithmConfig(Long projectId,
    AlgorithmConfigJpa algorithmConfig, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "AlgorithmConfig Client - add algorithmConfig" + algorithmConfig);

    final Client client = ClientBuilder.newClient();
    final WebTarget target = client.target(config.getProperty("base.url")
        + "/process/config/algo/add" + "?projectId=" + projectId);

    final String algorithmConfigString = ConfigUtility.getStringForGraph(
        algorithmConfig == null ? new AlgorithmConfigJpa() : algorithmConfig);

    final Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken)
        .put(Entity.xml(algorithmConfigString));

    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(resultString);
    }

    // converting to object
    AlgorithmConfigJpa result =
        ConfigUtility.getGraphForString(resultString, AlgorithmConfigJpa.class);

    return result;
  }

  /* see superclass */
  @Override
  public void updateAlgorithmConfig(Long projectId,
    AlgorithmConfigJpa algorithmConfig, String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Process Client - update algorithmConfig " + algorithmConfig);
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/process/config/algo/update" + "?projectId=" + projectId);

    String algorithmConfigString = ConfigUtility.getStringForGraph(
        algorithmConfig == null ? new AlgorithmConfigJpa() : algorithmConfig);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken)
        .post(Entity.xml(algorithmConfigString));

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing, successful
    } else {
      throw new Exception("Unexpected status - " + response.getStatus());
    }
  }

  /* see superclass */
  @Override
  public void removeAlgorithmConfig(Long projectId, Long id, String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Process Client - remove algorithmConfig " + id);
    validateNotEmpty(id, "id");
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/process/config/algo/" + id + "/remove?projectId=" + projectId);

    if (id == null)
      return;

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).delete();

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing, successful
    } else {
      throw new Exception("Unexpected status - " + response.getStatus());
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
  @Override
  public AlgorithmConfig getAlgorithmConfig(Long projectId, Long id,
    String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Process Client - get algorithmConfig " + id);
    validateNotEmpty(id, "id");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/process/config/algo/" + id + "?projectId=" + projectId);

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    AlgorithmConfigJpa algorithmConfig =
        ConfigUtility.getGraphForString(resultString, AlgorithmConfigJpa.class);
    return algorithmConfig;
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
  @Override
  public AlgorithmConfig getAlgorithmConfigForKey(Long projectId, String key,
    String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Process Client - get algorithmConfig " + key);
    validateNotEmpty(key, "key");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/process/config/algo/key/" + key + "?projectId=" + projectId);

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    AlgorithmConfigJpa algorithmConfig =
        ConfigUtility.getGraphForString(resultString, AlgorithmConfigJpa.class);
    return algorithmConfig;
  }

  /* see superclass */
  @Override
  public KeyValuePairList getInsertionAlgorithms(Long projectId,
    String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Process Client - get insertion Algorithms");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/process/algo/insertion?projectId=" + projectId);

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    KeyValuePairList insertionAlgorithms =
        ConfigUtility.getGraphForString(resultString, KeyValuePairList.class);
    return insertionAlgorithms;
  }

  /* see superclass */
  @Override
  public KeyValuePairList getMaintenanceAlgorithms(Long projectId,
    String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Process Client - get maintenance Algorithms");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/process/algo/maintenance?projectId=" + projectId);

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    KeyValuePairList maintenanceAlgorithms =
        ConfigUtility.getGraphForString(resultString, KeyValuePairList.class);
    return maintenanceAlgorithms;
  }

  /* see superclass */
  @Override
  public KeyValuePairList getReleaseAlgorithms(Long projectId, String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Process Client - get release Algorithms");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/process/algo/release?projectId=" + projectId);

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    KeyValuePairList releaseAlgorithms =
        ConfigUtility.getGraphForString(resultString, KeyValuePairList.class);
    return releaseAlgorithms;
  }

  /* see superclass */
  @Override
  public Long executeProcess(Long projectId, Long id, Boolean background,
    String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /* see superclass */
  @Override
  public void cancelProcess(Long projectId, Long id, String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Process Client - cancel process execution " + id);
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/execution/" + id + "/cancel" + "?projectId=" + projectId);

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing, successful
    } else {
      throw new Exception("Unexpected status - " + response.getStatus());
    }
  }

  /* see superclass */
  @Override
  public Integer getProcessProgress(Long projectId, Long id, String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .debug("Project Client - find progress of currently executing process");

    validateNotEmpty(projectId, "projectId");

    final Client client = ClientBuilder.newClient();
    final WebTarget target = client.target(config.getProperty("base.url")
        + "/process/" + id + "/progress?projectId=" + projectId);
    final Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    return ConfigUtility.getGraphForString(resultString, Integer.class);

  }

  /* see superclass */
  @Override
  public Integer getAlgorithmProgress(Long projectId, Long id, String authToken)
    throws Exception {

    Logger.getLogger(getClass()).debug(
        "Project Client - find progress of currently executing algorithm");

    validateNotEmpty(projectId, "projectId");

    final Client client = ClientBuilder.newClient();
    final WebTarget target = client.target(config.getProperty("base.url")
        + "/process/algo/" + id + "/progress?projectId=" + projectId);
    final Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    return ConfigUtility.getGraphForString(resultString, Integer.class);

  }

}
