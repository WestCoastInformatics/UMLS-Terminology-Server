/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers.content;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import com.wci.umls.server.helpers.content.Tree;
import com.wci.umls.server.jpa.content.CodeTreePositionJpa;
import com.wci.umls.server.jpa.content.ConceptTreePositionJpa;
import com.wci.umls.server.jpa.content.DescriptorTreePositionJpa;
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
  public TreePosition<? extends ComponentHasAttributesAndName> self;

  /** The children. */
  public List<Tree> children;

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

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.content.Tree#getSelf()
   */
  @Override
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

}
