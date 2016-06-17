/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.rest;

import com.wci.umls.server.jpa.worfklow.WorkflowBinDefinitionJpa;
import com.wci.umls.server.jpa.worfklow.WorkflowConfigJpa;
import com.wci.umls.server.model.workflow.WorkflowBinDefinition;
import com.wci.umls.server.model.workflow.WorkflowBinType;
import com.wci.umls.server.model.workflow.WorkflowConfig;

/**
 * The Interface WorkflowServiceRest.
 */
public interface WorkflowServiceRest {

  /**
   * Adds the workflow config.
   *
   * @param projectId the project id
   * @param config the config
   * @param authToken the auth token
   * @return the workflow config
   * @throws Exception the exception
   */
  public WorkflowConfig addWorkflowConfig(Long projectId,
    WorkflowConfigJpa config, String authToken) throws Exception;

  /**
   * Update workflow config.
   *
   * @param projectId the project id
   * @param config the config
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void updateWorkflowConfig(Long projectId, WorkflowConfigJpa config,
    String authToken) throws Exception;

  /**
   * Removes the workflow config.
   *
   * @param workflowConfigId the workflow config id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void removeWorkflowConfig(Long workflowConfigId,
    String authToken) throws Exception;

  /**
   * Adds the workflow bin definition.
   *
   * @param projectId the project id
   * @param workflowConfigId the workflow config id
   * @param binDefinition the bin definition
   * @param authToken the auth token
   * @return the workflow bin definition
   * @throws Exception the exception
   */
  public WorkflowBinDefinition addWorkflowBinDefinition(Long projectId,
    Long workflowConfigId, WorkflowBinDefinitionJpa binDefinition,
    String authToken) throws Exception;

  /**
   * Update workflow bin definition.
   * @param projectId TODO
   * @param definition the definition
   * @param authToken TODO
   *
   * @throws Exception the exception
   */
  public void updateWorkflowBinDefinition(Long projectId, WorkflowBinDefinitionJpa definition, String authToken) throws Exception;

  /**
   * Removes the workflow bin definition.
   *
   * @param projectId the project id
   * @param workflowBinDefinitionId the workflow bin definition id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void removeWorkflowBinDefinition(Long projectId,
    Long workflowBinDefinitionId, String authToken) throws Exception;

  /**
   * Regenerate bins.
   *
   * @param projectId the project id
   * @param type the type
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void regenerateBins(Long projectId, WorkflowBinType type,
    String authToken) throws Exception;

}
