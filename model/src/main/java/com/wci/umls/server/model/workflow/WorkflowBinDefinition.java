/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.workflow;

import com.wci.umls.server.helpers.HasLastModified;
import com.wci.umls.server.helpers.QueryType;

/**
 * Represents a query for identifing concepts/components that meet some set of
 * known criteria. The criteria can be expressed as an SQL, HQL, or Lucene query
 * and produces lists of clustered concepts.
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
   * Sets the editable flag.
   *
   * @param editable the editable flag
   */
  public void setEditable(boolean editable);

  /**
   * Checks if is required for release.
   *
   * @return true, if is required
   */
  public boolean isRequired();

  /**
   * Sets the required flag.
   *
   * @param required the new required flag
   */
  public void setRequired(boolean required);

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
  public QueryType getQueryType();

  /**
   * Sets the query type.
   *
   * @param queryType the new query type
   */
  public void setQueryType(QueryType queryType);

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

  /**
   * Indicates whether or not enabled is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isEnabled();

  /**
   * Sets the enabled.
   *
   * @param enabled the enabled
   */
  public void setEnabled(boolean enabled);

}