/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers.content;

import java.util.List;

import com.wci.umls.server.helpers.HasLabelSets;
import com.wci.umls.server.model.content.TreePosition;

/**
 * Represents a tree of {@link TreePosition} objects.
 */
public interface Tree extends HasLabelSets {

  /**
   * Merge specified tree with this one.
   *
   * @param tree the tree
   * @param sortField the sort field
   * @throws Exception the exception
   */
  public void mergeTree(Tree tree, String sortField) throws Exception;

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
   * Returns the node terminology id.
   *
   * @return the node terminology id
   */
  public String getNodeTerminologyId();

  /**
   * Sets the node terminology id.
   *
   * @param nodeTerminologyId the node terminology id
   */
  public void setNodeTerminologyId(String nodeTerminologyId);

  /**
   * Returns the node name.
   *
   * @return the node name
   */
  public String getNodeName();

  /**
   * Sets the node name.
   *
   * @param nodeName the node name
   */
  public void setNodeName(String nodeName);

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
   * Returns all nodes as a list, sorted by ancestor pa th.
   *
   * @return the trees as list
   * @throws Exception the exception
   */
  public List<Tree> getLeafNodes() throws Exception;

}