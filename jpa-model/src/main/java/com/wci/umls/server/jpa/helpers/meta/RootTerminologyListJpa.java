/*
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers.meta;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.helpers.AbstractResultList;
import com.wci.umls.server.helpers.meta.RootTerminologyList;
import com.wci.umls.server.jpa.meta.RootTerminologyJpa;
import com.wci.umls.server.model.meta.RootTerminology;

/**
 * JAXB enabled implementation of {@link RootTerminologyList}.
 */
@XmlRootElement(name = "rootTerminologyList")
public class RootTerminologyListJpa extends AbstractResultList<RootTerminology>
    implements RootTerminologyList {

  /* see superclass */
  @Override
  @XmlElement(type = RootTerminologyJpa.class, name = "rootTerminologies")
  public List<RootTerminology> getObjects() {
    return super.getObjectsTransient();
  }

}
