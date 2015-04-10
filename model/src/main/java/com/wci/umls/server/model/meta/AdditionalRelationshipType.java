/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.meta;

/**
 * Represents a further specification of a relation.
 * @see RelationshipType
 */
public interface AdditionalRelationshipType extends Abbreviation {

  /**
   * Returns the inverse.
   * 
   * @return the inverse
   */
  public AdditionalRelationshipType getInverse();

  /**
   * Sets the inverse.
   * 
   * @param inverse the inverse
   */
  public void setInverse(AdditionalRelationshipType inverse);
}
