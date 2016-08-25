/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.rest.client;

import java.util.Properties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import org.apache.log4j.Logger;

import com.wci.umls.server.ProcessConfig;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.ProcessConfigList;
import com.wci.umls.server.helpers.StringList;
import com.wci.umls.server.jpa.ProcessConfigJpa;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.helpers.ProcessConfigListJpa;
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
  public ProcessConfig addProcessConfig(ProcessConfigJpa processConfig,
    String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("ProcessConfig Client - add processConfig" + processConfig);

    final Client client = ClientBuilder.newClient();
    final WebTarget target = client
        .target(config.getProperty("base.url") + "/process/processConfig/add");

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
  public void updateProcessConfig(ProcessConfigJpa processConfig,
    String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Process Client - update processConfig " + processConfig);
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(
        config.getProperty("base.url") + "/process/processConfig/update");

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
  public void removeProcessConfig(Long projectId, Long id, String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Process Client - remove processConfig " + id);
    validateNotEmpty(id, "id");
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/process/processConfig/remove/" + id + "?projectId=" + projectId);

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

  @Override
  public ProcessConfig getProcessConfig(Long projectId, Long id,
    String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Process Client - get processConfig " + id);
    validateNotEmpty(id, "id");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(
        config.getProperty("base.url") + "/process/processConfig/" + id);

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
  public ProcessConfigList getProcessConfigs(Long projectId, String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Process Client - get processConfigs for project " + projectId);
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(
        config.getProperty("base.url") + "/process/processConfig/all/" + projectId);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(resultString);
    }

    // converting to object
    ProcessConfigList list =
        ConfigUtility.getGraphForString(resultString, ProcessConfigListJpa.class);
    return list;
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
