/*
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers.content;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.helpers.AbstractResultList;
import com.wci.umls.server.helpers.content.ConceptList;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.model.content.Concept;

/**
 * JAXB enabled implementation of {@link ConceptList}.
 */
@XmlRootElement(name = "conceptList")
public class ConceptListJpa extends AbstractResultList<Concept> implements
    ConceptList {

  /* see superclass */
  @Override
  @XmlElement(type = ConceptJpa.class, name = "concepts")
  public List<Concept> getObjects() {
    return super.getObjectsTransient();
  }

}
