/**
 * Copyright 2015 West Coast Informatics, LLC
 */
/*************************************************************
 * SubsetMember: SubsetMember.java
 * Last Updated: Feb 27, 2009
 *************************************************************/
package com.wci.umls.server.model.content;



/**
 * Represents membership of an {@link Concept} in a {@link Subset}.
 */
public interface ConceptSubsetMember extends SubsetMember<Concept>{


  /**
   * Returns the subset.
   * 
   * @return the subset
   */
  public ConceptSubset getSubset();

  /**
   * Sets the subset.
   * 
   * @param subset the subset
   */
  public void setSubset(ConceptSubset subset);
}
