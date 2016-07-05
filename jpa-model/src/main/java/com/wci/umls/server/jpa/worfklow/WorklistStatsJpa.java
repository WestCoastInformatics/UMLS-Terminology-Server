/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.worfklow;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.model.workflow.Worklist;
import com.wci.umls.server.model.workflow.WorklistStats;

/**
 * The Class WorklistStatsJpa.
 */
@XmlRootElement(name = "worklistStats")
public class WorklistStatsJpa implements WorklistStats {

  /** The worklist. */
  private Worklist worklist;

  /** The action ct. */
  private int actionCt;

  /** The approved ct. */
  private int approvedCt;

  /**
   * Instantiates an empty {@link WorklistStatsJpa}.
   */
  public WorklistStatsJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link WorklistStatsJpa} from the specified parameters.
   *
   * @param worklistStats the worklist stats
   */
  public WorklistStatsJpa(WorklistStats worklistStats) {
    worklist = worklistStats.getWorklist();
    actionCt = worklistStats.getActionCt();
    approvedCt = worklistStats.getApprovedCt();
  }

  /* see superclass */
  @Override
  @XmlElement(type = WorklistJpa.class)
  public Worklist getWorklist() {
    return worklist;
  }

  /* see superclass */
  @Override
  public void setWorklist(Worklist worklist) {
    this.worklist = worklist;
  }

  /* see superclass */
  @Override
  public int getActionCt() {
    return actionCt;
  }

  /* see superclass */
  @Override
  public void setActionCt(int actionCt) {
    this.actionCt = actionCt;
  }

  /* see superclass */
  @Override
  public int getApprovedCt() {
    return approvedCt;
  }

  /* see superclass */
  @Override
  public void setApprovedCt(int approvedCt) {
    this.approvedCt = approvedCt;
  }

}
