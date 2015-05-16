/**
 * Copyright 2015 West Coast Informatics, LLC
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
 * JAXB-enabled implementation of {@link ReleasePropertyList}.
 */
@XmlRootElement(name = "releasePropertyList")
public class ReleasePropertyListJpa extends AbstractResultList<ReleaseProperty>
    implements ReleasePropertyList {

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.helpers.AbstractResultList#getObjects()
   */
  @Override
  @XmlElement(type = ReleasePropertyJpa.class, name = "releaseProperty")
  public List<ReleaseProperty> getObjects() {
    return super.getObjectsTransient();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "ReleasePropertyListJpa [releaseProperties =" + getObjects() + ", getCount()="
        + getCount() + "]";
  }

}
