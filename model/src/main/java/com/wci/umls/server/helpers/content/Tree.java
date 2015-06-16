/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers.content;

import java.util.List;

import com.wci.umls.server.model.content.AtomClass;
import com.wci.umls.server.model.content.TreePosition;

/**
 * Represents a tree of {@link TreePosition} objects.
 *
 * @author ${author}
 */
public interface Tree {

  /**
   * Merge specified tree with this one.
   *
   * @param tree the tree
   */
  public void mergeTree(Tree tree);

  /**
   * Returns the children.
   *
   * @return the children
   */
  public List<Tree> getChildren();

  /**
   * Sets the children.
   *
   * @param children the children
   */
  public void setChildren(List<Tree> children);

  /**
   * Returns the total count of tree positions used to build the tree.
   *
   * @return the total count of tree positions
   */
  public int getTotalCount();

  /**
   * Sets the total count.
   *
   * @param totalCount the total count
   */
  public void setTotalCount(int totalCount);

  /**
   * Adds the child.
   *
   * @param child the child
   */
  public void addChild(Tree child);

  /**
   * Returns a full subtree with root matching atomclass a.
   *
   * @param terminologyId the terminology id
   * @param ancestorPath the ancestor path matching this atomclass
   * @return the subtree starting with the given atomclass
   */
  public Tree getSubTree(String terminologyId, String ancestorPath);

  /**
   * Returns the id.
   *
   * @return the id
   */
  public Long getId();

  /**
   * Sets the id.
   *
   * @param id the id
   */
  public void setId(Long id);

  /**
   * Returns the terminology.
   *
   * @return the terminology
   */
  public String getTerminology();

  /**
   * Sets the terminology.
   *
   * @param terminology the terminology
   */
  public void setTerminology(String terminology);

  /**
   * Returns the version.
   *
   * @return the version
   */
  public String getVersion();

  /**
   * Sets the version.
   *
   * @param version the version
   */
  public void setVersion(String version);

  /**
   * Returns the terminology id.
   *
   * @return the terminology id
   */
  public String getTerminologyId();

  /**
   * Sets the terminology id.
   *
   * @param terminologyId the terminology id
   */
  public void setTerminologyId(String terminologyId);

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
   * Sets fields from a tree position
   *
   * @param treePosition the from tree position
   */
  public void setFromTreePosition(TreePosition<? extends AtomClass> treePosition);

}