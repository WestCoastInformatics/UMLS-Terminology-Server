/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.content;

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
   * Returns the branched to.
   *
   * @return the branched to
   */
  public String getBranchedTo();

  /**
   * Sets the branched to.
   *
   * @param branchedTo the branched to
   */
  public void setBranchedTo(String branchedTo);

  /**
   * Clear members.
   */
  public void clearMembers();
}
