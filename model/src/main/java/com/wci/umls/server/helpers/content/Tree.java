/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers.content;

import java.util.List;

import com.wci.umls.server.model.content.AtomClass;
import com.wci.umls.server.model.content.TreePosition;

// TODO: Auto-generated Javadoc
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
   * Returns the self.
   *
   * @return the self
   */
  public TreePosition<? extends AtomClass> getSelf();

  /**
   * Sets the self.
   *
   * @param self the self
   */
  public void setSelf(TreePosition<? extends AtomClass> self);

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
   * Returns the leaf nodes.
   *
   * @return the leaf nodes
   */
  public List<TreePosition<? extends AtomClass>> getLeafNodes();

  /**
   * Returns the total count of tree positions used to build the tree.
   *
   * @return the total count of tree positions
   */
  public int getTotalCount();
  
  /**
   * Sets the total count.
   *
   * @return the int
   */
  public void setTotalCount(int totalCount);
  
  /**
   * Returns the count.
   *
   * @return the count
   */
  public int getCount();
  
  /**
   * Sets the count.
   *
   * @return the int
   */
  public void setCount(int count);
  
  /**
   * Adds the child.
   *
   * @param child the child
   */
  public void addChild(Tree child);

  /**
   * Returns a full subtree with root matching atomclass a.
   *
   * @param a the atom class
   * @param ancestorPath the ancestor path matching this atomclass
   * @return the subtree starting with the given atomclass
   */
  public Tree getSubTreeForAtomClass(AtomClass a, String ancestorPath);
}