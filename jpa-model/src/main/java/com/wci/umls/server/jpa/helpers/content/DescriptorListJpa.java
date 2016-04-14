/*
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers.content;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.helpers.AbstractResultList;
import com.wci.umls.server.helpers.content.DescriptorList;
import com.wci.umls.server.jpa.content.DescriptorJpa;
import com.wci.umls.server.model.content.Descriptor;

/**
 * JAXB enabled implementation of {@link DescriptorList}.
 */
@XmlRootElement(name = "descriptorList")
public class DescriptorListJpa extends AbstractResultList<Descriptor> implements
    DescriptorList {

  /* see superclass */
  @Override
  @XmlElement(type = DescriptorJpa.class, name = "descriptors")
  public List<Descriptor> getObjects() {
    return super.getObjectsTransient();
  }

}
