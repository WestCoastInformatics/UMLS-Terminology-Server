package com.wci.umls.server.model.content;

import java.util.List;

import com.wci.umls.server.helpers.HasAttributes;

/**
 * Represents a classification of atoms within a terminology, ontology, or
 * coding scheme. For example Metathesaurus CUIs, SNOMED CT source concepts, or
 * RXCUIs within RXNORM.
 */
public interface AtomClass extends Component, HasAttributes {

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
   * Returns the preferred atom name.
   * @return the preferred atom name
   */
  public String getDefaultPreferredName();

}