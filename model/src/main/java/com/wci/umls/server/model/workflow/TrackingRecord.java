/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.workflow;

import java.util.Set;

import com.wci.umls.server.helpers.HasLastModified;
import com.wci.umls.server.helpers.HasProject;
import com.wci.umls.server.helpers.HasTerminology;

/**
 * Represents a tracking record for editing a cluster of concepts. The
 * terminology ids may actually refer to atom ids as the concepts may not have
 * terminology identifiers yet and may merge, move, and split with respect to
 * each other.
 */
public interface TrackingRecord extends HasLastModified, HasTerminology,
    HasProject {

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
  public int getClusterId();

  /**
   * Sets the cluster id.
   *
   * @param clusterId the cluster id
   */
  public void setClusterId(int clusterId);

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
  public String getWorkflowBinName();

  /**
   * Sets the workflow bin.
   *
   * @param workflowBin the workflow bin
   */
  public void setWorkflowBinName(String workflowBin);

  /**
   * Returns the worklist name.
   *
   * @return the worklistname
   */
  public String getWorklistName();

  /**
   * Sets the worklist.
   *
   * @param worklistName the worklist
   */
  public void setWorklistName(String worklistName);

  /**
   * Returns the orig concept ids.
   *
   * @return the orig concept ids
   */
  public Set<Long> getOrigConceptIds();

  /**
   * Sets the orig concept ids.
   *
   * @param origConceptIds the orig concept ids
   */
  public void setOrigConceptIds(Set<Long> origConceptIds);

}