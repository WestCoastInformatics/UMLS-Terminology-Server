/*
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers.content;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.helpers.AbstractResultList;
import com.wci.umls.server.helpers.content.MapSetList;
import com.wci.umls.server.jpa.content.WorkflowEpochJpa;
import com.wci.umls.server.model.content.MapSet;

/**
 * JAXB enabled implementation of {@link MapSetList}.
 */
@XmlRootElement(name = "mapSetList")
public class MapSetListJpa extends AbstractResultList<MapSet> implements
    MapSetList {

  /* see superclass */
  @Override
  @XmlElement(type = WorkflowEpochJpa.class, name = "mapSet")
  public List<MapSet> getObjects() {
    return super.getObjectsTransient();
  }

}
