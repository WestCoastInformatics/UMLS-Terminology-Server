/*
 *    Copyright 2017 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.workflow;

import java.util.List;

import com.wci.umls.server.helpers.HasLastModified;
import com.wci.umls.server.helpers.HasProject;
import com.wci.umls.server.helpers.QueryStyle;

/**
 * Represents a collection of workflow bin definitions of a particular type for
 * a project.
 */
public interface WorkflowConfig extends HasLastModified, HasProject {

  /**
   * Gets the workflow bin definitions.
   *
   * @return the workflow bin definitions
   */
  public List<WorkflowBinDefinition> getWorkflowBinDefinitions();

  /**
   * Sets the workflow bin definitions.
   *
   * @param definitions the new workflow bin definitions
   */
  public void setWorkflowBinDefinitions(
    List<WorkflowBinDefinition> definitions);

  /**
   * Gets the type. MUTUALLY_EXCLUSIVE, QUALITY_ASSURANCE, AD_HOC, etc.
   *
   * @return the type
   */
  public String getType();

  /**
   * Sets the type.
   *
   * @param type the new type
   */
  public void setType(String type);

  /**
   * Gets the mutually exclusive.
   *
   * @return the mutually exclusive
   */
  public boolean isMutuallyExclusive();

  /**
   * Sets the mutually exclusive.
   *
   * @param mutuallyExclusive the new mutually exclusive
   */
  public void setMutuallyExclusive(boolean mutuallyExclusive);

  /**
   * Gets the last partition time.
   *
   * @return the last partition time
   */
  public Long getLastPartitionTime();

  /**
   * Sets the last partition time.
   *
   * @param lastPartitionTime the new last partition time
   */
  public void setLastPartitionTime(Long lastPartitionTime);

  /**
   * Indicates whether or not admin config is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isAdminConfig();

  /**
   * Sets the admin config.
   *
   * @param adminConfig the admin config
   */
  public void setAdminConfig(boolean adminConfig);

  /**
   * Returns the query style.
   *
   * @return the query style
   */
  public QueryStyle getQueryStyle();

  /**
   * Sets the query style.
   *
   * @param queryStyle the query style
   */
  public void setQueryStyle(QueryStyle queryStyle);
}
