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

  /* see superclass */
  @Override
  @XmlElement(type = AttributeJpa.class, name = "attributes")
  public List<Attribute> getObjects() {
    return super.getObjectsTransient();
  }

}
