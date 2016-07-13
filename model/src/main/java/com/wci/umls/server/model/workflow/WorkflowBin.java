/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.workflow;

import java.util.List;

import com.wci.umls.server.helpers.HasLastModified;
import com.wci.umls.server.helpers.HasProject;

/**
 * Represents a collection of tracking records that result from the execution of
 * the query in a {@link WorkflowBinDefinition}.
 */
public interface WorkflowBin extends HasLastModified, HasProject {

  /**
   * Gets the tracking records.
   *
   * @return the tracking records
   */
  public List<TrackingRecord> getTrackingRecords();

  /**
   * Sets the tracking records.
   *
   * @param records the new tracking records
   */
  public void setTrackingRecords(List<TrackingRecord> records);

  /**
   * Gets the name.
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
   * Gets the type.
   *
   * @return the type
   */
  public WorkflowBinType getType();

  /**
   * Sets the type.
   *
   * @param type the new type
   */
  public void setType(WorkflowBinType type);

  /**
   * Gets the rank.
   *
   * @return the rank
   */
  public int getRank();

  /**
   * Sets the rank.
   *
   * @param rank the new rank
   */
  public void setRank(int rank);

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
   * Gets the terminology id.
   *
   * @return the terminology id
   */
  public String getTerminologyId();

  /**
   * Sets the terminology id.
   *
   * @param terminologyId the new terminology id
   */
  public void setTerminologyId(String terminologyId);

  /**
   * Gets the creation time.
   *
   * @return the creation time
   */
  public Long getCreationTime();

  /**
   * Sets the creation time.
   *
   * @param creationTime the new creation time
   */
  public void setCreationTime(Long creationTime);

  /**
   * Returns the cluster count.
   *
   * @return the cluster count
   */
  public int getClusterCt();

  /**
   * Sets the cluster count.
   *
   * @param clusterCt the cluster count
   */
  public void setClusterCt(int clusterCt);

  /**
   * Gets the terminology.
   *
   * @return the terminology
   */
  public String getTerminology();

  /**
   * Sets the terminology.
   *
   * @param terminology the new terminology
   */
  public void setTerminology(String terminology);

  /**
   * Sets the version.
   *
   * @param version the new version
   */
  public void setVersion(String version);

  /**
   * Gets the version.
   *
   * @return the version
   */
  public String getVersion();

  /**
   * Returns the stats.
   *
   * @return the stats
   */
  public List<ClusterTypeStats> getStats();

  /**
   * Sets the stats.
   *
   * @param stats the stats
   */
  public void setStats(List<ClusterTypeStats> stats);
}