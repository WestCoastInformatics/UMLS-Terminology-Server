/*
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.actions;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.helpers.AbstractResultList;
import com.wci.umls.server.model.actions.MolecularAction;
import com.wci.umls.server.model.actions.MolecularActionList;

/**
 * JAXB enabled implementation of {@link MolecularActionList}.
 */
@XmlRootElement(name = "molecularActionList")
public class MolecularActionListJpa extends AbstractResultList<MolecularAction> implements
    MolecularActionList {

  /* see superclass */
  @Override
  @XmlElement(type = MolecularActionJpa.class, name = "molecularActions")
  public List<MolecularAction> getObjects() {
    return super.getObjectsTransient();
  }

}
