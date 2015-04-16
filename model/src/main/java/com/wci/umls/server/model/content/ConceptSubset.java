/**
 * Copyright 2015 West Coast Informatics, LLC
 */
/*************************************************************
 * Subset: Subset.java
 * Last Updated: Feb 27, 2009
 *************************************************************/
package com.wci.umls.server.model.content;

import java.util.List;


/**
 * Represents a subset of {@link Concept}s asserted by a terminology.
 */
public interface ConceptSubset extends Subset {

  /**
   * Returns the members.
   * 
   * @return the members
   */
  public List<ConceptSubsetMember> getMembers();

  /**
   * Sets the members.
   * 
   * @param members the members
   */
  public void setMembers(List<ConceptSubsetMember> members);

  /**
   * Adds a member.
   * 
   * @param member the member
   */
  public void addMember(ConceptSubsetMember member);

  /**
   * Removes the member.
   *
   * @param member the member
   */
  public void removeMember(ConceptSubsetMember member);

}
