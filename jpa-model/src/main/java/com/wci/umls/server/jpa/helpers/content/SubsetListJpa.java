/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers.content;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import com.wci.umls.server.helpers.AbstractResultList;
import com.wci.umls.server.helpers.content.SubsetList;
import com.wci.umls.server.jpa.content.AbstractSubset;
import com.wci.umls.server.model.content.AtomSubset;
import com.wci.umls.server.model.content.ConceptSubset;
import com.wci.umls.server.model.content.Subset;

/**
 * JAXB enabled implementation of {@link SubsetList}.
 */
@XmlRootElement(name = "subsetList")
@XmlSeeAlso({
  AtomSubset.class,
 ConceptSubset.class
})
public class SubsetListJpa extends AbstractResultList<Subset> implements
    SubsetList {


  /* (non-Javadoc)
   * @see com.wci.umls.server.helpers.AbstractResultList#getObjects()
   */
  @Override
  @XmlElement(type = AbstractSubset.class, name = "subset")
  public List<Subset> getObjects() {
    return super.getObjects();
  }

}
