/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.workflow;

import com.wci.umls.server.helpers.HasLastModified;

/**
 * Represents a workflow bin definition.
 */
public interface WorkflowBinDefinition extends HasLastModified {

  /**
   * Gets the name. e.g. "demotions"
   *
   * @return the name
   */
  public String getName();

  /**
   * Sets the name.
   *
   * @param name the new name
   */
  public void setName(String name);

  /**
   * Gets the description.
   *
   * @return the description
   */
  public String getDescription();

  /**
   * Sets the description.
   *
   * @param description the new description
   */
  public void setDescription(String description);

  /**
   * Checks if is editable.
   *
   * @return true, if is editable
   */
  public boolean isEditable();

  /**
   * Sets the editable.
   *
   * @param editable the new editable
   */
  public void setEditable(boolean editable);

  /**
   * Gets the query.
   *
   * @return the query
   */
  public String getQuery();

  /**
   * Sets the query.
   *
   * @param query the new query
   */
  public void setQuery(String query);

  /**
   * Gets the query type. e.g. "HQL"
   *
   * @return the query type
   */
  public String getQueryType();

  /**
   * Sets the query type.
   *
   * @param queryType the new query type
   */
  public void setQueryType(String queryType);

  /**
   * Returns the workflow config.
   *
   * @return the workflow config
   */
  public WorkflowConfig getWorkflowConfig();

  /**
   * Sets the workflow config.
   *
   * @param workflowConfig the workflow config
   */
  public void setWorkflowConfig(WorkflowConfig workflowConfig);

}