/*
 * Copyright 2016 West Coast Informatics, LLC
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
public class PropertyChainListJpa extends AbstractResultList<PropertyChain>
    implements PropertyChainList {

  /* see superclass */
  @Override
  @XmlElement(type = PropertyChainJpa.class, name = "chains")
  public List<PropertyChain> getObjects() {
    return super.getObjectsTransient();
  }

}
