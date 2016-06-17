/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.workflow;

import java.util.Set;

import com.wci.umls.server.helpers.HasLastModified;
import com.wci.umls.server.helpers.HasTerminology;

/**
 * Represents a tracking record for editing a cluster of concepts. The
 * terminology ids may actually refer to atom ids as the concepts may not have
 * terminology identifiers yet and may merge, move, and split with respect to
 * each other.
 */
public interface TrackingRecord extends HasLastModified, HasTerminology {

  /**
   * Returns the terminology ids.
   *
   * @return the terminology ids
   */
  public Set<Long> getComponentIds();

  /**
   * Sets the terminology ids.
   *
   * @param terminology the terminology ids
   */
  public void setComponentIds(Set<Long> terminology);

  /**
   * Returns the cluster id.
   *
   * @return the cluster id
   */
  public Long getClusterId();

  /**
   * Sets the cluster id.
   *
   * @param clusterId the cluster id
   */
  public void setClusterId(Long clusterId);

  /**
   * Returns the cluster type.
   *
   * @return the cluster type
   */
  public String getClusterType();

  /**
   * Sets the cluster type.
   *
   * @param clusterType the cluster type
   */
  public void setClusterType(String clusterType);

  /**
   * Returns the workflow bin.
   *
   * @return the workflow bin
   */
  public WorkflowBin getWorkflowBin();

  /**
   * Sets the workflow bin.
   *
   * @param workflowBin the workflow bin
   */
  public void setWorkflowBin(WorkflowBin workflowBin);

  /**
   * Returns the worklist.
   *
   * @return the worklist
   */
  public Worklist getWorklist();

  /**
   * Sets the worklist.
   *
   * @param worklist the worklist
   */
  public void setWorklist(Worklist worklist);
}