/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.meta;

/**
 * Represents a kind of relationship.
 */
public interface RelationshipType extends Abbreviation {

  /**
   * Indicates whether or not the relationship type is necessarily hierarchical.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isHierarchical();
  
  /**
   * Sets the hierarchical flag.
   *
   * @param hierarchical the hierarchical
   */
  public void setHierarchical(boolean hierarchical);
  
  /**
   * Returns the inverse.
   * 
   * @return the inverse
   */
  public RelationshipType getInverse();

  /**
   * Sets the inverse.
   * 
   * @param inverse the inverse
   */
  public void setInverse(RelationshipType inverse);

}
