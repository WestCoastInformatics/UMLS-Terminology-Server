/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.ReleaseInfo;
import com.wci.umls.server.helpers.AbstractResultList;
import com.wci.umls.server.helpers.ReleaseInfoList;
import com.wci.umls.server.jpa.ReleaseInfoJpa;

/**
 * JAXB-enabled implementation of {@link ReleaseInfoList}.
 */
@XmlRootElement(name = "releaseInfoList")
public class ReleaseInfoListJpa extends AbstractResultList<ReleaseInfo>
    implements ReleaseInfoList {

  /* see superclass */
  @Override
  @XmlElement(type = ReleaseInfoJpa.class, name = "releaseInfos")
  public List<ReleaseInfo> getObjects() {
    return super.getObjectsTransient();
  }

  /* see superclass */
  @Override
  public String toString() {
    return "ReleaseInfoListJpa [releaseInfos=" + getObjects() + ", getCount()="
        + getCount() + "]";
  }

}
