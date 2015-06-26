/**
 * Copyright 2015 West Coast Informatics, LLC
 */
/*************************************************************
 * Subset: Subset.java
 * Last Updated: Feb 27, 2009
 *************************************************************/
package com.wci.umls.server.model.content;

import com.wci.umls.server.helpers.HasMembers;

/**
 * Represents a subset of {@link Concept}s asserted by a terminology.
 */
public interface ConceptSubset extends Subset, HasMembers<ConceptSubsetMember> {
  /**
   * Indicates whether or not this subset exists to express disjointness between
   * the members.
   *
   * @return true, if is disjoint subset
   */
  public boolean isDisjointSubset();

  /**
   * Sets the disjoint subset.
   *
   * @param disjointSubset the new disjoint subset
   */
  public void setDisjointSubset(boolean disjointSubset);

  /**
   * Indicates whether or not marker subset is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isMarkerSubset();
  
  /**
   * Sets the marker subset.
   *
   * @param markerSubset the marker subset
   */
  public void setMarkerSubset(boolean markerSubset);
}
