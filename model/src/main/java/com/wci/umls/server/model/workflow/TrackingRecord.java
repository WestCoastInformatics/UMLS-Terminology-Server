/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.workflow;

import java.util.List;
import java.util.Set;

import com.wci.umls.server.helpers.HasLastModified;
import com.wci.umls.server.helpers.HasProject;
import com.wci.umls.server.helpers.HasTerminology;
import com.wci.umls.server.model.content.Concept;

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

  /**
   * Returns the concepts. This uses a list to make the unit test easier to
   * manage. Multiple collections of the same type with different objects is
   * messy.
   *
   * @return the concepts
   */
  public List<Concept> getConcepts();

  /**
   * Sets the concepts.
   *
   * @param concept the concepts
   */
  public void setConcepts(List<Concept> concept);

  /**
   * Returns the workflow status.
   *
   * @return the workflow status
   */
  public WorkflowStatus getWorkflowStatus();

  /**
   * Sets the workflow status.
   *
   * @param workflowStatus the workflow status
   */
  public void setWorkflowStatus(WorkflowStatus workflowStatus);

  /**
   * Returns the indexed data.
   *
   * @return the indexed data
   */
  public String getIndexedData();

  /**
   * Sets the indexed data.
   *
   * @param indexedData the indexed data
   */
  public void setIndexedData(String indexedData);
}