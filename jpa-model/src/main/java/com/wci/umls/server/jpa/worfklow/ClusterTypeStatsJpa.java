/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.worfklow;

import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.model.workflow.ClusterTypeStats;

/**
 * The Class ClusterTypeStatsJpa.
 */
@XmlRootElement(name = "clusterTypeStats")
public class ClusterTypeStatsJpa implements ClusterTypeStats {

  /** The all. */
  private int all;

  /** The editable. */
  private int editable;

  /** The uneditable. */
  private int uneditable;

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
    all = clusterTypeStats.getAll();
    editable = clusterTypeStats.getEditable();
    uneditable = clusterTypeStats.getUneditable();
  }

  /* see superclass */
  @Override
  public int getAll() {
    return all;
  }

  /* see superclass */
  @Override
  public void setAll(int all) {
    this.all = all;
  }

  /* see superclass */
  @Override
  public int getEditable() {
    return editable;
  }

  /* see superclass */
  @Override
  public void setEditable(int editable) {
    this.editable = editable;
  }

  /* see superclass */
  @Override
  public int getUneditable() {
    return uneditable;
  }

  /* see superclass */
  @Override
  public void setUneditable(int uneditable) {
    this.uneditable = uneditable;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + all;
    result = prime * result + editable;
    result = prime * result + uneditable;
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
    if (all != other.all)
      return false;
    if (editable != other.editable)
      return false;
    if (uneditable != other.uneditable)
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "ClusterTypeStatsJpa [all=" + all + ", editable=" + editable
        + ", uneditable=" + uneditable + "]";
  }

}