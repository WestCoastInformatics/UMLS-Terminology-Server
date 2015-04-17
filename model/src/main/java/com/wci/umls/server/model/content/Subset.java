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
 * Represents a subset of content asserted by a terminology.
 */
public interface Subset extends ComponentHasAttributes {

  /**
   * Returns the name.
   * 
   * @return the name
   */
  public String getName();

  /**
   * Sets the name.
   * 
   * @param name the name
   */
  public void setName(String name);

  /**
   * Returns the description.
   * 
   * @return the description
   */
  public String getDescription();

  /**
   * Sets the description.
   * 
   * @param description the description
   */
  public void setDescription(String description);

  /**
   * Returns the members.
   * 
   * @return the members
   */
  public List<SubsetMember> getMembers();

  /**
   * Sets the members.
   * 
   * @param members the members
   */
  public void setMembers(List<SubsetMember> members);

  /**
   * Adds a member.
   * 
   * @param member the member
   */
  public void addMember(SubsetMember member);

  /**
   * Removes the member.
   *
   * @param member the member
   */
  public void removeMember(SubsetMember member);

}
