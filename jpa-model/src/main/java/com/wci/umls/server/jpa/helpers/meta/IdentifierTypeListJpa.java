/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers.meta;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.helpers.AbstractResultList;
import com.wci.umls.server.helpers.meta.IdentifierTypeList;
import com.wci.umls.server.jpa.meta.IdentifierTypeJpa;
import com.wci.umls.server.model.meta.IdentifierType;

/**
 * JAXB enabled implementation of {@link IdentifierTypeList}.
 */
@XmlRootElement(name = "identifierTypeList")
public class IdentifierTypeListJpa extends AbstractResultList<IdentifierType>
    implements IdentifierTypeList {

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.AbstractResultList#getObjects()
   */
  @Override
  @XmlElement(type = IdentifierTypeJpa.class, name = "type")
  public List<IdentifierType> getObjects() {
    return super.getObjects();
  }

}
