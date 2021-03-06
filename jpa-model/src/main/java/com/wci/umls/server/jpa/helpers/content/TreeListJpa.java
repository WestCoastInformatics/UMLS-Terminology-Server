/*
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers.content;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.helpers.AbstractResultList;
import com.wci.umls.server.helpers.content.Tree;
import com.wci.umls.server.helpers.content.TreeList;

/**
 * JAXB enabled implementation of {@link TreeList}.
 */
@XmlRootElement(name = "treeList")
public class TreeListJpa extends AbstractResultList<Tree> implements TreeList {

  /* see superclass */
  @Override
  @XmlElement(type = TreeJpa.class, name = "trees")
  public List<Tree> getObjects() {
    return super.getObjectsTransient();
  }

}
