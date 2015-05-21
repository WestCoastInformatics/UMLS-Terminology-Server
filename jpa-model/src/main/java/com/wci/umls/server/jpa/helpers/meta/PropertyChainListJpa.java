/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers.meta;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.helpers.AbstractResultList;
import com.wci.umls.server.helpers.meta.PropertyChainList;
import com.wci.umls.server.jpa.meta.PropertyChainJpa;
import com.wci.umls.server.model.meta.PropertyChain;

/**
 * JAXB enabled implementation of {@link PropertyChainList}.
 */
@XmlRootElement(name = "propertyChainList")
public class PropertyChainListJpa extends
    AbstractResultList<PropertyChain> implements PropertyChainList {

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.AbstractResultList#getObjects()
   */
  @Override
  @XmlElement(type = PropertyChainJpa.class, name = "type")
  public List<PropertyChain> getObjects() {
    return super.getObjectsTransient();
  }

}
