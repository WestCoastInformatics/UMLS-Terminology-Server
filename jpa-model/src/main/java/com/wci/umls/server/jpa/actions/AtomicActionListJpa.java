/*
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.actions;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.helpers.AbstractResultList;
import com.wci.umls.server.model.actions.AtomicAction;
import com.wci.umls.server.model.actions.AtomicActionList;

/**
 * JAXB enabled implementation of {@link AtomicActionList}.
 */
@XmlRootElement(name = "atomicActionList")
public class AtomicActionListJpa extends AbstractResultList<AtomicAction>
    implements AtomicActionList {

  /* see superclass */
  @Override
  @XmlElement(type = AtomicActionJpa.class, name = "actions")
  public List<AtomicAction> getObjects() {
    return super.getObjectsTransient();
  }

}
