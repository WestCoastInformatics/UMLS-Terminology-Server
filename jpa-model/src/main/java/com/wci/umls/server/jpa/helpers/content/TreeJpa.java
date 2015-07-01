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

import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.content.Tree;
import com.wci.umls.server.jpa.content.CodeTreePositionJpa;
import com.wci.umls.server.jpa.content.ConceptTreePositionJpa;
import com.wci.umls.server.jpa.content.DescriptorTreePositionJpa;
import com.wci.umls.server.model.content.AtomClass;
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

  /** The id. */
  Long id = null;

  /** The terminology. */
  String terminology = null;

  /** The version. */
  String version = null;

  /** The terminology id. */
  String nodeTerminologyId = null;

  /** The name. */
  String nodeName = null;

  /** The ancestor path. */
  String ancestorPath = null;

  /** The child ct. */
  int childCt = 0;

  /** The total count of tree positions matching this tree's criteria. */
  int totalCount;

  /** The children. */
  private List<Tree> children = new ArrayList<>();

  /** The labels. */
  List<String> labels = new ArrayList<>();

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
    nodeTerminologyId = tree.getNodeTerminologyId();
    nodeName = tree.getNodeName();
    childCt = tree.getChildCt();
    ancestorPath = tree.getAncestorPath();
    totalCount = tree.getTotalCount();
    labels = tree.getLabels();

    // deep-copy children
    children = new ArrayList<>();
    for (Tree child : tree.getChildren()) {
      children.add(new TreeJpa(child));
    }
  }

  /**
   * Instantiates a {@link TreeJpa} from the specified {@link TreePosition}.
   *
   * @param treePosition the tree position
   */
  public TreeJpa(TreePosition<? extends AtomClass> treePosition) {

    if (treePosition == null)
      throw new IllegalArgumentException(
          "Cannot construct tree from null tree position");

    this.id = treePosition.getNode().getId();
    this.terminology = treePosition.getNode().getTerminology();
    this.version = treePosition.getNode().getVersion();
    this.nodeTerminologyId = treePosition.getNode().getTerminologyId();
    this.nodeName = treePosition.getNode().getName();
    this.childCt = treePosition.getChildCt();
    this.ancestorPath = treePosition.getAncestorPath();
    this.children = new ArrayList<>();
    this.labels = treePosition.getNode().getLabels();
  }

  @Override
  public void mergeTree(Tree tree, String sortField) throws Exception {

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

    for (Tree child : tree.getChildren()) {
      if (!childMap.containsKey(child.getId())) {
        children.add(child);
      } else {
        childMap.get(child.getId()).mergeTree(child, sortField);
      }
    }
    // Sort the children at this level
    if (sortField != null) {
      ConfigUtility.reflectionSort(children, Tree.class, sortField);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.content.Tree#getId()
   */
  @Override
  public Long getId() {
    return id;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.content.Tree#setId(java.lang.Long)
   */
  @Override
  public void setId(Long id) {
    this.id = id;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.content.Tree#getTerminology()
   */
  @Override
  public String getTerminology() {
    return terminology;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.helpers.content.Tree#setTerminology(java.lang.String)
   */
  @Override
  public void setTerminology(String terminology) {
    this.terminology = terminology;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.content.Tree#getVersion()
   */
  @Override
  public String getVersion() {
    return version;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.content.Tree#setVersion(java.lang.String)
   */
  @Override
  public void setVersion(String version) {
    this.version = version;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.content.Tree#getNodeTerminologyId()
   */
  @Override
  public String getNodeTerminologyId() {
    return nodeTerminologyId;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.helpers.content.Tree#setNodeTerminologyId(java.lang.String)
   */
  @Override
  public void setNodeTerminologyId(String terminologyId) {
    this.nodeTerminologyId = terminologyId;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.content.Tree#getNodeName()
   */
  @Override
  public String getNodeName() {
    return nodeName;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.content.Tree#setNodeName(java.lang.String)
   */
  @Override
  public void setNodeName(String name) {
    this.nodeName = name;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.content.Tree#getAncestorPath()
   */
  @Override
  public String getAncestorPath() {
    return ancestorPath;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.helpers.content.Tree#setAncestorPath(java.lang.String)
   */
  @Override
  public void setAncestorPath(String ancestorPath) {
    this.ancestorPath = ancestorPath;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.content.Tree#getChildCt()
   */
  @Override
  public int getChildCt() {
    return childCt;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.content.Tree#setChildCt(int)
   */
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
   * @see com.wci.umls.server.helpers.HasLabels#getLabels()
   */
  @Override
  public List<String> getLabels() {
    return labels;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.HasLabels#setLabels(java.util.List)
   */
  @Override
  public void setLabels(List<String> labels) {
    this.labels = labels;

  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.HasLabels#addLabel(java.lang.String)
   */
  @Override
  public void addLabel(String label) {
    if (labels == null) {
      labels = new ArrayList<String>();
    }
    labels.add(label);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.HasLabels#removeLabel(java.lang.String)
   */
  @Override
  public void removeLabel(String label) {
    if (labels == null) {
      labels = new ArrayList<String>();
    }
    labels.remove(label);

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
    result =
        prime * result + ((ancestorPath == null) ? 0 : ancestorPath.hashCode());
    result = prime * result + ((children == null) ? 0 : children.hashCode());
    result = prime * result + ((labels == null) ? 0 : labels.hashCode());
    result = prime * result + ((nodeName == null) ? 0 : nodeName.hashCode());
    result =
        prime * result + ((terminology == null) ? 0 : terminology.hashCode());
    result =
        prime * result
            + ((nodeTerminologyId == null) ? 0 : nodeTerminologyId.hashCode());
    result = prime * result + ((version == null) ? 0 : version.hashCode());
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
    if (ancestorPath == null) {
      if (other.ancestorPath != null)
        return false;
    } else if (!ancestorPath.equals(other.ancestorPath))
      return false;
    if (children == null) {
      if (other.children != null)
        return false;
    } else if (!children.equals(other.children))
      return false;
    if (labels == null) {
      if (other.labels != null)
        return false;
    } else if (!labels.equals(other.labels))
      return false;
    if (nodeName == null) {
      if (other.nodeName != null)
        return false;
    } else if (!nodeName.equals(other.nodeName))
      return false;
    if (terminology == null) {
      if (other.terminology != null)
        return false;
    } else if (!terminology.equals(other.terminology))
      return false;
    if (nodeTerminologyId == null) {
      if (other.nodeTerminologyId != null)
        return false;
    } else if (!nodeTerminologyId.equals(other.nodeTerminologyId))
      return false;
    if (version == null) {
      if (other.version != null)
        return false;
    } else if (!version.equals(other.version))
      return false;
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "TreeJpa [id=" + id + ", terminology=" + terminology + ", version="
        + version + ", terminologyId=" + nodeTerminologyId + ", name=" + nodeName
        + ", ancestorPath=" + ancestorPath + ", childCt=" + childCt
        + ", totalCount=" + totalCount + ", children=" + children + "]";
  }

  @Override
  public List<Tree> getLeafNodes() throws Exception {
    // TODO Reimplement this, was lost in a merge (damn you Git!)
    return null;
  }
}