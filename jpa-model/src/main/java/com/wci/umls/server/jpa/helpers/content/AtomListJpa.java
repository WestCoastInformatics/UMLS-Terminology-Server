/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers.content;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.helpers.AbstractResultList;
import com.wci.umls.server.helpers.content.AtomList;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.model.content.Atom;

/**
 * JAXB enabled implementation of {@link AtomList}.
 */
@XmlRootElement(name = "atomList")
public class AtomListJpa extends AbstractResultList<Atom> implements AtomList {

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.AbstractResultList#getObjects()
   */
  @Override
  @XmlElement(type = AtomJpa.class, name = "atom")
  public List<Atom> getObjects() {
    return super.getObjectsTransient();
  }

}
