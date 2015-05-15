/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers.content;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.helpers.AbstractResultList;
import com.wci.umls.server.helpers.content.TreePositionList;
import com.wci.umls.server.jpa.content.AbstractTreePosition;
import com.wci.umls.server.model.content.ComponentHasAttributesAndName;
import com.wci.umls.server.model.content.TreePosition;

/**
 * JAXB enabled implementation of {@link TreePositionList}.
 */
@XmlRootElement(name = "treePositionList")
public class TreePositionListJpa
    extends
    AbstractResultList<TreePosition<? extends ComponentHasAttributesAndName>>
    implements TreePositionList {

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.AbstractResultList#getObjects()
   */
  @Override
  @XmlElement(type = AbstractTreePosition.class, name = "treepos")
  public List<TreePosition<? extends ComponentHasAttributesAndName>> getObjects() {
    return super.getObjects();
  }

}
