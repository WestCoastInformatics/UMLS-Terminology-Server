/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers.content;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.helpers.AbstractResultList;
import com.wci.umls.server.helpers.content.AttributeList;
import com.wci.umls.server.jpa.content.AttributeJpa;
import com.wci.umls.server.model.content.Attribute;

/**
 * JAXB enabled implementation of {@link AttributeList}.
 */
@XmlRootElement(name = "attributeList")
public class AttributeListJpa extends AbstractResultList<Attribute> implements
    AttributeList {


  /* (non-Javadoc)
   * @see com.wci.umls.server.helpers.AbstractResultList#getObjects()
   */
  @Override
  @XmlElement(type = AttributeJpa.class, name = "attribute")
  public List<Attribute> getObjects() {
    return super.getObjects();
  }

}
