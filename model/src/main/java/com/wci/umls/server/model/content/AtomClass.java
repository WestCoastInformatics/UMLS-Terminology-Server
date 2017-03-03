/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.content;

import java.util.List;

import com.wci.umls.server.helpers.HasLabelSets;
import com.wci.umls.server.model.workflow.WorkflowStatus;

/**
 * Generically represents a classification of atoms.
 */
public interface AtomClass extends ComponentHasAttributesAndName, HasLabelSets {

  /**
   * Gets the atoms.
   *
   * @return the atoms
   */
  public List<Atom> getAtoms();

  /**
   * Sets the atoms.
   *
   * @param atoms the new atoms
   */
  public void setAtoms(List<Atom> atoms);

  /**
   * Gets the workflow status.
   *
   * @return the workflow status
   */
  public WorkflowStatus getWorkflowStatus();

  /**
   * Sets the workflow status.
   *
   * @param workflowStatus the new workflow status
   */
  public void setWorkflowStatus(WorkflowStatus workflowStatus);

  /**
   * Gets the branched to.
   *
   * @return the branched to
   */
  public String getBranchedTo();

  /**
   * Sets the branched to.
   *
   * @param branchedTo the new branched to
   */
  public void setBranchedTo(String branchedTo);

  /**
   * Add branched to.
   *
   * @param newBranch the new branch
   */
  public void addBranchedTo(String newBranch);

  /**
   * Remove branched to.
   *
   * @param closedBranch the closed branch
   */
  public void removeBranchedTo(String closedBranch);

  
}
