/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.worfklow;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.model.workflow.ClusterTypeStats;
import com.wci.umls.server.model.workflow.WorkflowBin;
import com.wci.umls.server.model.workflow.WorkflowBinStats;

/**
 * A JAXB-enabled implementation of {@link WorkflowBinStats}.
 */
@XmlRootElement(name = "workflowBinStats")
public class WorkflowBinStatsJpa implements WorkflowBinStats {

  /** The workflow bin. */
  private WorkflowBin workflowBin;

  /** The cluster type stats map. */
  private Map<String, ClusterTypeStats> clusterTypeStatsMap = new HashMap<>();

  /**
   * Instantiates an empty {@link WorkflowBinStatsJpa}.
   */
  public WorkflowBinStatsJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link WorkflowBinStatsJpa} from the specified parameters.
   *
   * @param workflowBinStats the workflow bin stats
   */
  public WorkflowBinStatsJpa(WorkflowBinStats workflowBinStats) {
    this.workflowBin = workflowBinStats.getWorkflowBin();
    this.clusterTypeStatsMap = workflowBinStats.getClusterTypeStatsMap();
  }

  /* see superclass */
  @Override
  public WorkflowBin getWorkflowBin() {
    return workflowBin;
  }

  /* see superclass */
  @Override
  public void setWorkflowBin(WorkflowBin workflowBin) {
    this.workflowBin = workflowBin;
  }

  /* see superclass */
  @Override
  // TODO: @XmlJavaTypeAdapter(UserRoleMapAdapter.class)
  public Map<String, ClusterTypeStats> getClusterTypeStatsMap() {
    return clusterTypeStatsMap;
  }

  /* see superclass */
  @Override
  public void setClusterTypeStatsMap(
    Map<String, ClusterTypeStats> clusterTypeStatsMap) {
    this.clusterTypeStatsMap = clusterTypeStatsMap;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
        prime
            * result
            + ((clusterTypeStatsMap == null) ? 0 : clusterTypeStatsMap
                .hashCode());
    result =
        prime * result + ((workflowBin == null) ? 0 : workflowBin.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    WorkflowBinStatsJpa other = (WorkflowBinStatsJpa) obj;
    if (clusterTypeStatsMap == null) {
      if (other.clusterTypeStatsMap != null)
        return false;
    } else if (!clusterTypeStatsMap.equals(other.clusterTypeStatsMap))
      return false;
    if (workflowBin == null) {
      if (other.workflowBin != null)
        return false;
    } else if (!workflowBin.equals(other.workflowBin))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "WorkflowBinStatsJpa [workflowBin=" + workflowBin
        + ", clusterTypeStatsMap=" + clusterTypeStatsMap + "]";
  }

}
