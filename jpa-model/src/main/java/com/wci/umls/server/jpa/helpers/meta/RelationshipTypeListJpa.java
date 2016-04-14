/*
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers.meta;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.helpers.AbstractResultList;
import com.wci.umls.server.helpers.meta.RelationshipTypeList;
import com.wci.umls.server.jpa.meta.RelationshipTypeJpa;
import com.wci.umls.server.model.meta.RelationshipType;

/**
 * JAXB enabled implementation of {@link RelationshipTypeList}.
 */
@XmlRootElement(name = "relationshipTypeList")
public class RelationshipTypeListJpa extends
    AbstractResultList<RelationshipType> implements RelationshipTypeList {

  /* see superclass */
  @Override
  @XmlElement(type = RelationshipTypeJpa.class, name = "types")
  public List<RelationshipType> getObjects() {
    return super.getObjectsTransient();
  }

}
