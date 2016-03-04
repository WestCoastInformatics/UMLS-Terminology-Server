/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers.content;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.helpers.AbstractResultList;
import com.wci.umls.server.helpers.content.MappingList;
import com.wci.umls.server.jpa.content.MappingJpa;
import com.wci.umls.server.model.content.Mapping;

/**
 * JAXB enabled implementation of {@link MappingList}.
 */
@XmlRootElement(name = "mappingList")
public class MappingListJpa extends AbstractResultList<Mapping> implements
    MappingList {

  /* see superclass */
  @Override
  @XmlElement(type = MappingJpa.class, name = "mappings")
  public List<Mapping> getObjects() {
    return super.getObjectsTransient();
  }

}
