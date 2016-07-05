/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.workflow;

/**
 *  Represents cluster type statistics for a bin, checklist, or worklist.
 */
public interface ClusterTypeStats {
  
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
   * Returns the all.
   *
   * @return the all
   */
  public int getAll();
  
  /**
   * Sets the all.
   *
   * @param all the all
   */
  public void setAll(int all);
  
  /**
   * Returns the editable.
   *
   * @return the editable
   */
  public int getEditable();
  
  /**
   * Sets the editable.
   *
   * @param editable the editable
   */
  public void setEditable(int editable);
  
  /**
   * Returns the uneditable.
   *
   * @return the uneditable
   */
  public int getUneditable();
  
  /**
   * Sets the uneditable.
   *
   * @param uneditable the uneditable
   */
  public void setUneditable(int uneditable);
  
}
