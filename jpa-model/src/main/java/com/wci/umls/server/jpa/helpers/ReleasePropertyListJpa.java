/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.ReleaseProperty;
import com.wci.umls.server.helpers.AbstractResultList;
import com.wci.umls.server.helpers.ReleasePropertyList;
import com.wci.umls.server.jpa.ReleasePropertyJpa;

/**
 * JAXB enabled implementation of {@link ReleasePropertyList}.
 */
@XmlRootElement(name = "releasePropertyList")
public class ReleasePropertyListJpa extends AbstractResultList<ReleaseProperty>
    implements ReleasePropertyList {

  /* see superclass */
  @Override
  @XmlElement(type = ReleasePropertyJpa.class, name = "properties")
  public List<ReleaseProperty> getObjects() {
    return super.getObjectsTransient();
  }

  /* see superclass */
  @Override
  public String toString() {
    return "ReleasePropertyListJpa [releaseProperties =" + getObjects()
        + ", getCount()=" + getCount() + "]";
  }

}
