/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.workflow;

import com.wci.umls.server.helpers.HasStats;

/**
 * Represents cluster type statistics for a bin, checklist, or worklist.
 */
public interface ClusterTypeStats extends HasStats {

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

}
