/*
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers.meta;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.helpers.AbstractResultList;
import com.wci.umls.server.helpers.meta.SemanticTypeList;
import com.wci.umls.server.jpa.meta.SemanticTypeJpa;
import com.wci.umls.server.model.meta.SemanticType;

/**
 * JAXB enabled implementation of {@link SemanticTypeList}.
 */
@XmlRootElement(name = "semanticTypeList")
public class SemanticTypeListJpa extends AbstractResultList<SemanticType>
    implements SemanticTypeList {

  /* see superclass */
  @Override
  @XmlElement(type = SemanticTypeJpa.class, name = "types")
  public List<SemanticType> getObjects() {
    return super.getObjectsTransient();
  }

}
