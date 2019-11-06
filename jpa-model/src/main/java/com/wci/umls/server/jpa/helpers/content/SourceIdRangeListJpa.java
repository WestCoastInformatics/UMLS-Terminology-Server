/*
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers.content;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.helpers.AbstractResultList;
import com.wci.umls.server.helpers.content.SourceIdRangeList;
import com.wci.umls.server.jpa.inversion.SourceIdRangeJpa;
import com.wci.umls.server.model.inversion.SourceIdRange;

/**
 * JAXB enabled implementation of {@link SourceIdRangeList}.
 */
@XmlRootElement(name = "atomList")
public class SourceIdRangeListJpa extends AbstractResultList<SourceIdRange> implements SourceIdRangeList {

  /* see superclass */
  @Override
  @XmlElement(type = SourceIdRangeJpa.class, name = "sourceIdRanges")
  public List<SourceIdRange> getObjects() {
    return super.getObjectsTransient();
  }

}
