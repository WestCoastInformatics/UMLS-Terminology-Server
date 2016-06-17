/**
 * Copyright 2016 West Coast Informatics, LLC
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

import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.jpa.services.rest.WorkflowServiceRest;
import com.wci.umls.server.jpa.worfklow.WorkflowBinDefinitionJpa;
import com.wci.umls.server.jpa.worfklow.WorkflowConfigJpa;
import com.wci.umls.server.model.workflow.WorkflowBinDefinition;
import com.wci.umls.server.model.workflow.WorkflowBinType;
import com.wci.umls.server.model.workflow.WorkflowConfig;

/**
 * A client for connecting to a content REST service.
 */
public class WorkflowClientRest extends RootClientRest
    implements WorkflowServiceRest {

  /** The config. */
  private Properties config = null;

  /**
   * Instantiates a {@link WorkflowClientRest} from the specified parameters.
   *
   * @param config the config
   */
  public WorkflowClientRest(Properties config) {
    this.config = config;
  }



  @Override
  public WorkflowConfig addWorkflowConfig(Long projectId,
    WorkflowConfigJpa workflowConfig, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Workflow Client - add workflow config"
        + projectId + ", " + workflowConfig.toString() + ", " + authToken);

    validateNotEmpty(projectId, "projectId");

    Client client = ClientBuilder.newClient();
    
    WebTarget target = client.target(config.getProperty("base.url")
        + "/workflow/config/add?projectId=" + projectId);

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.json(workflowConfig));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    WorkflowConfig v =
        ConfigUtility.getGraphForString(resultString, WorkflowConfigJpa.class);
    return v;
  }

  @Override
  public void updateWorkflowConfig(Long projectId, WorkflowConfigJpa workflowConfig,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Workflow Client - update workflow config"
        + projectId + ", " + workflowConfig.toString() + ", " + authToken);

    validateNotEmpty(projectId, "projectId");

    Client client = ClientBuilder.newClient();
    
    WebTarget target = client.target(config.getProperty("base.url")
        + "/workflow/config/update?projectId=" + projectId);

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.json(workflowConfig));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    //WorkflowConfig v =
    //    ConfigUtility.getGraphForString(resultString, WorkflowConfigJpa.class);
    // TODO: no return object?
    //return v;
  }

  @Override
  public void removeWorkflowConfig(Long workflowConfigId, String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Workflow Client - remove workflow config " + workflowConfigId
            + ", " + authToken);

    validateNotEmpty(workflowConfigId, "workflowConfigId");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/workflow/config/" + workflowConfigId + "/remove");

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).delete();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    /*
     * ValidationResult v = ConfigUtility.getGraphForString(resultString,
     * ValidationResultJpa.class); return v;
     */
  }

  @Override
  public WorkflowBinDefinition addWorkflowBinDefinition(Long projectId,
    Long workflowConfigId, WorkflowBinDefinitionJpa binDefinition,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Workflow Client - add workflow bin definition"
        + projectId + ", " + workflowConfigId + " ," + binDefinition.toString() + ", " + authToken);

    validateNotEmpty(projectId, "projectId");
    validateNotEmpty(workflowConfigId, "workflowConfigId");


    Client client = ClientBuilder.newClient();
    
    WebTarget target = client.target(config.getProperty("base.url")
        + "/workflow/definition/add?projectId=" + projectId + "&configId=" + workflowConfigId);

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.json(binDefinition));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    WorkflowBinDefinition v =
        ConfigUtility.getGraphForString(resultString, WorkflowBinDefinitionJpa.class);
    return v;
  }

  @Override
  public void updateWorkflowBinDefinition(Long projectId, WorkflowBinDefinitionJpa definition, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug("Workflow Client - update workflow config"
        + projectId + ", " + definition.toString() + ", " + authToken);

    Client client = ClientBuilder.newClient();
    
    WebTarget target = client.target(config.getProperty("base.url")
        + "/workflow/definition/update?projectId=" + projectId);

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.json(definition));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    //WorkflowConfig v =
    //    ConfigUtility.getGraphForString(resultString, WorkflowConfigJpa.class);
    // TODO: no return object?
    //return v;
    
  }

  @Override
  // TODO: why does this include projectId, but removeWorkflowConfig doesn't?
  public void removeWorkflowBinDefinition(Long projectId,
    Long workflowBinDefinitionId, String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Workflow Client - remove workflow bin definition " + workflowBinDefinitionId
            + ", " + authToken);

    validateNotEmpty(workflowBinDefinitionId, "workflowBinDefinitionId");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/workflow/definition/" + workflowBinDefinitionId + "/remove");

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).delete();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    /*
     * ValidationResult v = ConfigUtility.getGraphForString(resultString,
     * ValidationResultJpa.class); return v;
     */
  }

  @Override
  public void regenerateBins(Long projectId, WorkflowBinType type,
    String authToken) throws Exception {
    // TODO Auto-generated method stub
    
  }

}