/*
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers.meta;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.helpers.AbstractResultList;
import com.wci.umls.server.helpers.meta.AdditionalRelationshipTypeList;
import com.wci.umls.server.jpa.meta.AdditionalRelationshipTypeJpa;
import com.wci.umls.server.model.meta.AdditionalRelationshipType;

/**
 * JAXB enabled implementation of {@link AdditionalRelationshipTypeList}.
 */
@XmlRootElement(name = "additionalRelationshipTypeList")
public class AdditionalRelationshipTypeListJpa extends
    AbstractResultList<AdditionalRelationshipType> implements
    AdditionalRelationshipTypeList {

  /* see superclass */
  @Override
  @XmlElement(type = AdditionalRelationshipTypeJpa.class, name = "types")
  public List<AdditionalRelationshipType> getObjects() {
    return super.getObjectsTransient();
  }

}
