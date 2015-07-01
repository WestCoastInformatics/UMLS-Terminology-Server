/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.content;

/**
 * Represents a position in a hierarchical tree with a path to the root. Tree
 * positions may be based around {@link Concept}s, {@link Descriptor}s, or
 * {@link Code}s. The data type is defined by the terminology's
 * "organizing class type". See sub classes
 * 
 * @param <T> the type
 * 
 */
public interface TreePosition<T extends AtomClass> extends
    ComponentHasAttributes {

  /**
   * Returns the node.
   *
   * @return the node
   */
  public T getNode();

  /**
   * Sets the node.
   *
   * @param node the node
   */
  public void setNode(T node);

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
   * Returns the child ct.
   *
   * @return the child ct
   */
  public int getChildCt();

  /**
   * Sets the child ct.
   *
   * @param childCt the child ct
   */
  public void setChildCt(int childCt);

  /**
   * Returns the descendant ct.
   *
   * @return the descendant ct
   */
  public int getDescendantCt();

  /**
   * Sets the descendant ct.
   *
   * @param descendantCt the descendant ct
   */
  public void setDescendantCt(int descendantCt);

}