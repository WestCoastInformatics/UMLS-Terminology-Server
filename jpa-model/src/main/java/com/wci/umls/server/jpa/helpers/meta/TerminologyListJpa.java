/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers.meta;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.helpers.AbstractResultList;
import com.wci.umls.server.helpers.meta.TerminologyList;
import com.wci.umls.server.jpa.meta.TerminologyJpa;
import com.wci.umls.server.model.meta.Terminology;

/**
 * JAXB enabled implementation of {@link TerminologyList}.
 */
@XmlRootElement(name = "terminologyList")
public class TerminologyListJpa extends AbstractResultList<Terminology>
    implements TerminologyList {

  /* see superclass */
  @Override
  @XmlElement(type = TerminologyJpa.class, name = "terminology")
  public List<Terminology> getObjects() {
    return super.getObjectsTransient();
  }

}
