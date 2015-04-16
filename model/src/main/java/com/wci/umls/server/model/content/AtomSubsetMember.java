/**
 * Copyright 2015 West Coast Informatics, LLC
 */
/*************************************************************
 * SubsetMember: SubsetMember.java
 * Last Updated: Feb 27, 2009
 *************************************************************/
package com.wci.umls.server.model.content;



/**
 * Represents membership of an {@link Atom} i0n a {@link Subset}.
 */
public interface AtomSubsetMember extends SubsetMember<Atom>{

  /**
   * Returns the subset.
   * 
   * @return the subset
   */
  public AtomSubset getSubset();

  /**
   * Sets the subset.
   * 
   * @param subset the subset
   */
  public void setSubset(AtomSubset subset);
}
