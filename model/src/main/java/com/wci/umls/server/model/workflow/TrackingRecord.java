/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.workflow;

import java.util.List;

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
  public List<String> getTerminologyIds();

  /**
   * Sets the terminology ids.
   *
   * @param terminology the terminology ids
   */
  public void setTerminologyIds(List<String> terminology);

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

}