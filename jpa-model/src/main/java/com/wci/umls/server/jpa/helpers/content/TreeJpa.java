/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers.content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import com.wci.umls.server.helpers.content.Tree;
import com.wci.umls.server.jpa.content.CodeTreePositionJpa;
import com.wci.umls.server.jpa.content.ConceptTreePositionJpa;
import com.wci.umls.server.jpa.content.DescriptorTreePositionJpa;
import com.wci.umls.server.model.content.AtomClass;
import com.wci.umls.server.model.content.TreePosition;

/**
 * JAXB enabled implementation of {@link Tree}.
 */

/**
 * The Class TreeJpa.
 *
 * @author ${author}
 */
@XmlRootElement(name = "tree")
@XmlSeeAlso({
    ConceptTreePositionJpa.class, DescriptorTreePositionJpa.class,
    CodeTreePositionJpa.class
})
public class TreeJpa implements Tree {

  /** The id. */
  Long id = null;

  /** The terminology. */
  String terminology = null;

  /** The version. */
  String version = null;

  /** The terminology id. */
  String terminologyId = null;

  /** The name. */
  String name = null;

  /** The ancestor path */
  String ancestorPath = null;

  /** The child ct. */
  int childCt = 0;

  /**  The total count of tree positions matching this tree's criteria. */
  int totalCount;

  /** The children. */
  private List<Tree> children = new ArrayList<>();

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
    id = tree.getId();
    terminology = tree.getTerminology();
    version = tree.getVersion();
    name = tree.getName();
    childCt = tree.getChildCt();
    ancestorPath = tree.getAncestorPath();
    totalCount = tree.getTotalCount();
    children = tree.getChildren();
  }

   
 
  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.helpers.content.Tree#mergeTree(com.wci.umls.server.
   * helpers.content.Tree)
   */
  @Override
  public void mergeTree(Tree tree) {
    
    // allow for merging trees with null ids
    if (!(tree.getId() == null && this.getId() == null)) {
      
      // but don't allow merging trees with different ids
      if (!this.getId().equals(tree.getId())) {
      throw new IllegalArgumentException(
          "Unable to merge tree with different root");
    }
    }
    
    // assemble a map of this tree's children 
      Map<Long, Tree> childMap = new HashMap<>();
      for (Tree t : this.getChildren()) {
        childMap.put(t.getId(), t);
  }
      
    for (Tree child: tree.getChildren()) {    
      if (!childMap.containsKey(child.getId())) {
        children.add(child);
      } else {
        childMap.get(child.getId()).mergeTree(child);
      }
    }
  }
  
  /**
   * Instantiates a {@link TreeJpa} from the specified parameters.
   *
   * @param treePosition the tree position
   */
  @Override
  public void setFromTreePosition(TreePosition<? extends AtomClass> treePosition) {
    
    if (treePosition == null)
      throw new IllegalArgumentException("Cannot construct tree from null tree position");
    
    this.id = treePosition.getNode().getId();
    this.terminology = treePosition.getNode().getTerminology();
    this.version = treePosition.getNode().getVersion();
    this.terminologyId = treePosition.getNode().getTerminologyId();
    this.name = treePosition.getNode().getName();
    this.childCt = treePosition.getChildCt();
    this.ancestorPath = treePosition.getAncestorPath();
    this.children = new ArrayList<>();
      }
  
  

  @Override
  public Long getId() {
    return id;
    }

  @Override
  public void setId(Long id) {
    this.id = id;
  }

  @Override
  public String getTerminology() {
    return terminology;
    }

  @Override
  public void setTerminology(String terminology) {
    this.terminology = terminology;
  }

  @Override
  public String getVersion() {
    return version;
      }

  @Override
  public void setVersion(String version) {
    this.version = version;
    }

  @Override
  public String getTerminologyId() {
    return terminologyId;
  }

  @Override
  public void setTerminologyId(String terminologyId) {
    this.terminologyId = terminologyId;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getAncestorPath() {
    return ancestorPath;
  }

  @Override
  public void setAncestorPath(String ancestorPath) {
    this.ancestorPath = ancestorPath;
  }

  @Override
  public int getChildCt() {
    return childCt;
  }

  @Override
  public void setChildCt(int childCt) {
    this.childCt = childCt;
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
   * @see com.wci.umls.server.helpers.content.Tree#getTotalCount()
   */
  @Override
  public int getTotalCount() {
    return totalCount;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.content.Tree#setTotalCount(int)
   */
  @Override
  public void setTotalCount(int totalCount) {
    this.totalCount = totalCount;
    }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.helpers.content.Tree#addChild(com.wci.umls.server.helpers
   * .content.Tree)
   */
  @Override
  public void addChild(Tree child) {
    this.children.add(child);
    }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.content.Tree#getSubTree(java.lang.String,
   * java.lang.String)
   */
  @Override
  public Tree getSubTree(String terminologyId, String ancestorPath) {

    // call the helper
    return getSubTreeForAtomClassHelper(this, ancestorPath);

  }

  /**
   * Helper function to recursively check a tree for a matching subtree.
   *
   * @param tree the tree-portion to check
   * @param ancestorPath the ancestor path to be matched
   * @return the sub tree for atom class helper
   */
  @SuppressWarnings("static-method")
  private Tree getSubTreeForAtomClassHelper(Tree tree, String ancestorPath) {
    // TODO Decide if this is still necessary anyway
    return null;
  }

  @Override
  public String toString() {
    return "TreeJpa [id=" + id + ", terminology=" + terminology + ", version="
        + version + ", terminologyId=" + terminologyId + ", name=" + name
        + ", ancestorPath=" + ancestorPath + ", childCt=" + childCt
        + ", totalCount=" + totalCount + ", children=" + children + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
        prime * result + ((ancestorPath == null) ? 0 : ancestorPath.hashCode());
    result = prime * result + childCt;
    result = prime * result + ((children == null) ? 0 : children.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result =
        prime * result + ((terminology == null) ? 0 : terminology.hashCode());
    result =
        prime * result
            + ((terminologyId == null) ? 0 : terminologyId.hashCode());
    result = prime * result + totalCount;
    result = prime * result + ((version == null) ? 0 : version.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    TreeJpa other = (TreeJpa) obj;
    if (ancestorPath == null) {
      if (other.ancestorPath != null)
        return false;
    } else if (!ancestorPath.equals(other.ancestorPath))
      return false;
    if (childCt != other.childCt)
      return false;
    if (children == null) {
      if (other.children != null)
        return false;
    } else if (!children.equals(other.children))
      return false;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (terminology == null) {
      if (other.terminology != null)
        return false;
    } else if (!terminology.equals(other.terminology))
      return false;
    if (terminologyId == null) {
      if (other.terminologyId != null)
        return false;
    } else if (!terminologyId.equals(other.terminologyId))
      return false;
    if (totalCount != other.totalCount)
      return false;
    if (version == null) {
      if (other.version != null)
        return false;
    } else if (!version.equals(other.version))
      return false;
    return true;
  }

}
