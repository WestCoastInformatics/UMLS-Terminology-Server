/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.workflow;

/**
 * The Interface WorklistStats.
 */
public interface WorklistStats {

  /**
   * Returns the worklist.
   *
   * @return the worklist
   */
  public Worklist getWorklist();
  
  /**
   * Sets the worklist.
   *
   * @param worklist the worklist
   */
  public void setWorklist(Worklist worklist);
  
  /**
   * Returns the action ct.
   *
   * @return the action ct
   */
  public int getActionCt();
  
  /**
   * Sets the action ct.
   *
   * @param actionCt the action ct
   */
  public void setActionCt(int actionCt);
  
  /**
   * Returns the approved ct.
   *
   * @return the approved ct
   */
  public int getApprovedCt();
  
  /**
   * Sets the approved ct.
   *
   * @param approvedCt the approved ct
   */
  public void setApprovedCt(int approvedCt);
}
