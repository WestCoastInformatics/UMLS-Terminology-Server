/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.content;

import java.util.List;

import com.wci.umls.server.helpers.HasUserAnnotations;
import com.wci.umls.server.helpers.HasLabelSets;

/**
 * Represents a classification of atoms within a terminology, ontology, or
 * coding scheme. For example Metathesaurus CUIs, SNOMED CT source concepts, or
 * RXCUIs within RXNORM.
 */
public interface AtomClass extends ComponentHasAttributesAndName, HasLabelSets, HasUserAnnotations {

  /**
   * Returns the atoms.
   * 
   * @return the atoms
   */
  public List<Atom> getAtoms();

  /**
   * Sets the atoms.
   * 
   * @param atoms the atoms
   */
  public void setAtoms(List<Atom> atoms);

  /**
   * Adds the atom.
   * 
   * @param atom the atom
   */
  public void addAtom(Atom atom);

  /**
   * Removes the atom.
   * 
   * @param atom the atom
   */
  public void removeAtom(Atom atom);

  /**
   * Returns the workflow status.
   *
   * @return the workflow status
   */
  public String getWorkflowStatus();

  /**
   * Sets the workflow status.
   *
   * @param workflowStatus the workflow status
   */
  public void setWorkflowStatus(String workflowStatus);

  /**
   * Returns the branched to.
   *
   * @return the branched to
   */
  public String getBranchedTo();

  /**
   * Sets the branched to.
   *
   * @param branchedTo the branched to
   */
  public void setBranchedTo(String branchedTo);

  /**
   * Adds the branched to.
   *
   * @param newBranch the new branch
   */
  public void addBranchedTo(String newBranch);

  /**
   * Removes the branched to.
   *
   * @param closedBranch the closed branch
   */
  public void removeBranchedTo(String closedBranch);
}
