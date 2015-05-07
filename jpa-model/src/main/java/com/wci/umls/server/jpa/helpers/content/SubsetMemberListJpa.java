/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers.content;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import com.wci.umls.server.helpers.AbstractResultList;
import com.wci.umls.server.helpers.content.SubsetMemberList;
import com.wci.umls.server.jpa.content.AbstractSubsetMember;
import com.wci.umls.server.jpa.content.AtomSubsetMemberJpa;
import com.wci.umls.server.jpa.content.ConceptSubsetMemberJpa;
import com.wci.umls.server.model.content.ComponentHasAttributes;
import com.wci.umls.server.model.content.SubsetMember;

/**
 * JAXB enabled implementation of {@link SubsetMemberList}.
 */
@XmlRootElement(name = "subsetMemberList")
@XmlSeeAlso({
    AtomSubsetMemberJpa.class, ConceptSubsetMemberJpa.class
})
public class SubsetMemberListJpa extends
    AbstractResultList<SubsetMember<? extends ComponentHasAttributes>>
    implements SubsetMemberList {

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.AbstractResultList#getObjects()
   */
  @Override
  @XmlElement(type = AbstractSubsetMember.class, name = "subsetMember")
  public List<SubsetMember<? extends ComponentHasAttributes>> getObjects() {
    return super.getObjects();
  }

}
