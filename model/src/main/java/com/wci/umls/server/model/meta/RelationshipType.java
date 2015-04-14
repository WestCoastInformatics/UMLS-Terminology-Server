/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.meta;

/**
 * Represents a kind of relationship.
 */
public interface RelationshipType extends Abbreviation {

  /**
   * Indicates whether or not grouping type is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isGroupingType();

  /**
   * Sets the grouping type.
   *
   * @param groupingType the grouping type
   */
  public void setGroupingType(boolean groupingType);

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
