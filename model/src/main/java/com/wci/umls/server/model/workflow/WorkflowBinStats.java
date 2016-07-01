/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.workflow;

import java.util.Map;

/**
 * The Interface WorkflowBinStats.
 */
public interface WorkflowBinStats {

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
   * Returns the cluster type stats map.
   *
   * @return the cluster type stats map
   */
  public Map<String, ClusterTypeStats> getClusterTypeStatsMap();

  /**
   * Sets the cluster type stats map.
   *
   * @param clusterTypeStatsMap the cluster type stats map
   */
  public void setClusterTypeStatsMap(
    Map<String, ClusterTypeStats> clusterTypeStatsMap);
}
