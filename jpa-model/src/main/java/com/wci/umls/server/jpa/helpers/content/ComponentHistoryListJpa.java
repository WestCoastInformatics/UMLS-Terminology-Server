/*
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers.content;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.helpers.AbstractResultList;
import com.wci.umls.server.helpers.content.ComponentHistoryList;
import com.wci.umls.server.helpers.content.ConceptList;
import com.wci.umls.server.jpa.content.ComponentHistoryJpa;
import com.wci.umls.server.model.content.ComponentHistory;

/**
 * JAXB enabled implementation of {@link ConceptList}.
 */
@XmlRootElement(name = "componentHistoryList")
public class ComponentHistoryListJpa extends AbstractResultList<ComponentHistory> implements
ComponentHistoryList {

  /* see superclass */
  @Override
  @XmlElement(type = ComponentHistoryJpa.class, name = "concepts")
  public List<ComponentHistory> getObjects() {
    return super.getObjectsTransient();
  }

}
