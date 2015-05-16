/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers.meta;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.helpers.AbstractResultList;
import com.wci.umls.server.helpers.meta.TermTypeList;
import com.wci.umls.server.jpa.meta.TermTypeJpa;
import com.wci.umls.server.model.meta.TermType;

/**
 * JAXB enabled implementation of {@link TermTypeList}.
 */
@XmlRootElement(name = "termTypeList")
public class TermTypeListJpa extends AbstractResultList<TermType> implements
    TermTypeList {

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.AbstractResultList#getObjects()
   */
  @Override
  @XmlElement(type = TermTypeJpa.class, name = "type")
  public List<TermType> getObjects() {
    return super.getObjectsTransient();
  }

}
