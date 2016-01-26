/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers.content;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.helpers.AbstractResultList;
import com.wci.umls.server.helpers.content.GeneralConceptAxiomList;
import com.wci.umls.server.jpa.content.GeneralConceptAxiomJpa;
import com.wci.umls.server.model.content.GeneralConceptAxiom;

/**
 * JAXB enabled implementation of {@link GeneralConceptAxiomList}.
 */
@XmlRootElement(name = "generalConceptAxiomList")
public class GeneralConceptAxiomListJpa extends
    AbstractResultList<GeneralConceptAxiom> implements GeneralConceptAxiomList {

  /* see superclass */
  @Override
  @XmlElement(type = GeneralConceptAxiomJpa.class, name = "generalConceptAxioms")
  public List<GeneralConceptAxiom> getObjects() {
    return super.getObjectsTransient();
  }

}
