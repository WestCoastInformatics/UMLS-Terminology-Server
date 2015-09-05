/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.content;

/**
 * Represents a general concept axiom (general inclusion aximo) that has
 * a left hand side and a right hand side both represented by anonymous concepts.
 */
public interface GeneralConceptAxiom extends Component {

  /**
   * Returns the left hand side.
   *
   * @return the left hand side
   */
  public Concept getLeftHandSide();
  
  /**
   * Sets the left hand side.
   *
   * @param leftHandSide the left hand side
   */
  public void setLeftHandSide(Concept leftHandSide);
  
  /**
   * Returns the right hand side.
   *
   * @return the right hand side
   */
  public Concept getRightHandSide();
  
  /**
   * Sets the right hand side.
   *
   * @param rightHandSide the right hand side
   */
  public void setRightHandSide(Concept rightHandSide);
}
