/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers.meta;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.helpers.AbstractResultList;
import com.wci.umls.server.helpers.meta.AttributeNameList;
import com.wci.umls.server.jpa.meta.AttributeNameJpa;
import com.wci.umls.server.model.meta.AttributeName;

/**
 * JAXB enabled implementation of {@link AttributeNameList}.
 */
@XmlRootElement(name = "attributeNameList")
public class AttributeNameListJpa extends AbstractResultList<AttributeName>
    implements AttributeNameList {

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.AbstractResultList#getObjects()
   */
  @Override
  @XmlElement(type = AttributeNameJpa.class, name = "name")
  public List<AttributeName> getObjects() {
    return super.getObjects();
  }

}
