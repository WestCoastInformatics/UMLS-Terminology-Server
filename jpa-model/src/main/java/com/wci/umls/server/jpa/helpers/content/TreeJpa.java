/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers.content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import com.wci.umls.server.helpers.content.Tree;
import com.wci.umls.server.jpa.content.AbstractTreePosition;
import com.wci.umls.server.jpa.content.CodeTreePositionJpa;
import com.wci.umls.server.jpa.content.ConceptTreePositionJpa;
import com.wci.umls.server.jpa.content.DescriptorTreePositionJpa;
import com.wci.umls.server.model.content.AtomClass;
import com.wci.umls.server.model.content.ComponentHasAttributesAndName;
import com.wci.umls.server.model.content.TreePosition;

/**
 * JAXB enabled implementation of {@link Tree}.
 */
@XmlRootElement(name = "tree")
@XmlSeeAlso({
    ConceptTreePositionJpa.class, DescriptorTreePositionJpa.class,
    CodeTreePositionJpa.class
})
public class TreeJpa implements Tree {

  /** The self. */
  private TreePosition<? extends ComponentHasAttributesAndName> self;

  /** The children. */
  private List<Tree> children = null;

  /**
   * Instantiates an empty {@link TreeJpa}.
   */
  public TreeJpa() {
    // n/a
  }

  /**
   * Instantiates a {@link TreeJpa} from the specified parameters.
   *
   * @param tree the tree
   */
  public TreeJpa(Tree tree) {
    self = tree.getSelf();
    children = tree.getChildren();
  }

  /**
   * Merge tree.
   *
   * @param tree the tree
   */
  @Override
  public void mergeTree(Tree tree) {
    // fail if both trees do not have the same root
    if (!self.equals(tree.getSelf())) {
      throw new IllegalArgumentException(
          "Unable to merge tree with different root");
    }

    Map<Long, TreePosition<? extends AtomClass>> idTreeposMap = new HashMap<>();
    Map<Long, Set<Long>> parChdMap = new HashMap<>();
    computeIdTreepos(this, idTreeposMap);
    computeIdTreepos(tree, idTreeposMap);
    computeParChd(this, parChdMap);
    computeParChd(tree, parChdMap);

    // Reassemble the tree starting with "self"
    List<Tree> children = new ArrayList<>();
    for (Long chdId : parChdMap.get(self.getId())) {
      children.add(buildTree(chdId, idTreeposMap, parChdMap));
    }
    this.setChildren(children);
  }

  /**
   * Builds the tree.
   *
   * @param id the id
   * @param idTreeposMap the id treepos map
   * @param parChdMap the par chd map
   * @return the tree
   */
  private Tree buildTree(Long id,
    Map<Long, TreePosition<? extends AtomClass>> idTreeposMap,
    Map<Long, Set<Long>> parChdMap) {
    Tree tree = new TreeJpa();
    tree.setSelf(idTreeposMap.get(id));
    List<Tree> children = new ArrayList<>();
    if (parChdMap.containsKey(id)) {
      for (Long chdId : parChdMap.get(id)) {
        children.add(buildTree(chdId, idTreeposMap, parChdMap));
      }
      tree.setChildren(children);
    } else {
      tree.setChildren(null);
    }
    return tree;
  }

  /**
   * Compute id treepos.
   *
   * @param tree the tree
   * @param idTreeposMap the id treepos map
   */
  private void computeIdTreepos(Tree tree,
    Map<Long, TreePosition<? extends AtomClass>> idTreeposMap) {
    idTreeposMap.put(tree.getSelf().getId(), tree.getSelf());
    for (Tree chdTree : tree.getChildren()) {
      computeIdTreepos(chdTree, idTreeposMap);
    }
  }

  /**
   * Compute par chd.
   *
   * @param tree the tree
   * @param parChdMap the par chd map
   */
  private void computeParChd(Tree tree, Map<Long, Set<Long>> parChdMap) {
    for (Tree chdTree : tree.getChildren()) {
      if (!parChdMap.containsKey(tree.getSelf().getId())) {
        parChdMap.put(tree.getSelf().getId(), new HashSet<Long>());
      }
      parChdMap.get(tree.getSelf().getId()).add(chdTree.getSelf().getId());
      computeParChd(chdTree, parChdMap);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.content.Tree#getSelf()
   */
  @Override
  @XmlElement(type = AbstractTreePosition.class)
  public TreePosition<? extends ComponentHasAttributesAndName> getSelf() {
    return self;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.helpers.content.Tree#setSelf(com.wci.umls.server.model
   * .content.TreePosition)
   */
  @Override
  public void setSelf(TreePosition<? extends ComponentHasAttributesAndName> self) {
    this.self = self;

  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.content.Tree#getChildren()
   */
  @Override
  @XmlElement(type = TreeJpa.class, name = "child")
  public List<Tree> getChildren() {
    if (children == null) {
      children = new ArrayList<>();
    }
    return children;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.content.Tree#setChildren(java.util.List)
   */
  @Override
  public void setChildren(List<Tree> children) {
    this.children = children;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    if (self == null)
      return "null";
    sb.append("TreeJpa = " + self.getNode().getId());
    List<Tree> children = getChildren();
    if (children.size() > 0) {
      sb.append(" [ ");
    }
    for (Tree chd : children) {
      sb.append(chd.toString()).append(", ");
    }
    if (children.size() > 0) {
      sb.append(" ]");
    }
    return sb.toString();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((self == null) ? 0 : self.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    TreeJpa other = (TreeJpa) obj;
    if (self == null) {
      if (other.self != null)
        return false;
    } else if (!self.equals(other.self))
      return false;
    return true;
  }

}
