/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.worfklow;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.model.workflow.ClusterTypeStats;

/**
 * JAXB-enabled implementation of {@link ClusterTypeStats}.
 */
@XmlRootElement(name = "clusterTypeStats")
public class ClusterTypeStatsJpa implements ClusterTypeStats {

  /** The all count. */
  private String clusterType;

  /** The stats. */
  private Map<String, Integer> stats;

  /**
   * Instantiates an empty {@link ClusterTypeStatsJpa}.
   */
  public ClusterTypeStatsJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link ClusterTypeStatsJpa} from the specified parameters.
   *
   * @param clusterTypeStats the cluster type stats
   */
  public ClusterTypeStatsJpa(ClusterTypeStats clusterTypeStats) {
    clusterType = clusterTypeStats.getClusterType();
    stats = new HashMap<>(clusterTypeStats.getStats());
  }

  /* see superclass */
  @Override
  public String getClusterType() {
    return clusterType;
  }

  /* see superclass */
  @Override
  public void setClusterType(String clusterType) {
    this.clusterType = clusterType;
  }

  /* see superclass */
  @Override
  public Map<String, Integer> getStats() {
    if (stats == null) {
      stats = new HashMap<>();
    }
    return stats;
  }

  /* see superclass */
  @Override
  public void setStats(Map<String, Integer> stats) {
    this.stats = stats;

  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
        prime * result + ((clusterType == null) ? 0 : clusterType.hashCode());
    result = prime * result + ((stats == null) ? 0 : stats.hashCode());
    return result;
  }

  /* see superclass */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ClusterTypeStatsJpa other = (ClusterTypeStatsJpa) obj;
    if (clusterType == null) {
      if (other.clusterType != null)
        return false;
    } else if (!clusterType.equals(other.clusterType))
      return false;
    if (stats == null) {
      if (other.stats != null)
        return false;
    } else if (!stats.equals(other.stats))
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "ClusterTypeStatsJpa [clusterType=" + clusterType + ", stats="
        + stats + "]";
  }

}
