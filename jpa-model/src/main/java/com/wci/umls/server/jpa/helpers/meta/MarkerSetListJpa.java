/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers.meta;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.helpers.AbstractResultList;
import com.wci.umls.server.helpers.meta.MarkerSetList;
import com.wci.umls.server.jpa.meta.MarkerSetJpa;
import com.wci.umls.server.model.meta.MarkerSet;

/**
 * JAXB enabled implementation of {@link MarkerSetList}.
 */
@XmlRootElement(name = "markerSetList")
public class MarkerSetListJpa extends AbstractResultList<MarkerSet> implements
    MarkerSetList {

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.AbstractResultList#getObjects()
   */
  @Override
  @XmlElement(type = MarkerSetJpa.class, name = "name")
  public List<MarkerSet> getObjects() {
    return super.getObjectsTransient();
  }

}
