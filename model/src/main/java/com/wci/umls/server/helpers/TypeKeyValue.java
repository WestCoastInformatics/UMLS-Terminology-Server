/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

import com.wci.umls.server.model.workflow.WorkflowStatus;

// TODO: Auto-generated Javadoc
/**
 * Generically represents a tuple of type, key, and value. Used for configuring
 * filters, acronym lists, etcs.
 */
public interface TypeKeyValue extends HasLastModified {

  /**
   * Returns the type.
   *
   * @return the type
   */
  public String getType();

  /**
   * Sets the type.
   *
   * @param type the type
   */
  public void setType(String type);

  /**
   * Returns the key.
   *
   * @return the key
   */
  public String getKey();

  /**
   * Sets the key.
   *
   * @param key the key
   */
  public void setKey(String key);

  /**
   * Returns the value.
   *
   * @return the value
   */
  public String getValue();

  /**
   * Sets the value.
   *
   * @param value the value
   */
  public void setValue(String value);

  /**
   * Gets the workflow status.
   *
   * @return the workflow status
   */
  public WorkflowStatus getWorkflowStatus();

  /**
   * Sets the workflow status.
   *
   * @param workflowStatus the new workflow status
   */
  public void setWorkflowStatus(WorkflowStatus workflowStatus);
}
