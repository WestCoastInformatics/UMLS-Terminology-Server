/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.content;

/**
 * Represents a position in a hierarchical tree with a path to the root. Tree
 * positions may be based around {@link Concept}s, {@link Descriptor}s, or
 * {@link Code}s. The data type is defined by the terminology's
 * "organizing class type". See sub classes
 */
public interface TreePosition extends ComponentHasAttributes {

  /**
   * Returns the additional relationship label, such as "isa" or "branch_of".
   *
   * @return the additional relationship label
   */
  public String getAdditionalRelationshipType();

  /**
   * Sets the additional relationship label.
   *
   * @param additionalrelationshipType the additional relationship label
   */
  public void setAdditionalRelationshipType(String additionalrelationshipType);

  /**
   * Returns the ancestor path.
   *
   * @return the ancestor path
   */
  public String getAncestorPath();

  /**
   * Sets the ancestor path.
   *
   * @param ancestorPath the ancestor path
   */
  public void setAncestorPath(String ancestorPath);

  /**
   * Returns the default preferred name.
   *
   * @return the default preferred name
   */
  public String getDefaultPreferredName();

  /**
   * Sets the default preferred name.
   *
   * @param name the default preferred name
   */
  public void setDefaultPreferredName(String name);
  
}