/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers.content;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.helpers.AbstractResultList;
import com.wci.umls.server.helpers.content.SubsetMemberList;
import com.wci.umls.server.jpa.content.AbstractSubsetMember;
import com.wci.umls.server.model.content.ComponentHasAttributesAndName;
import com.wci.umls.server.model.content.Subset;
import com.wci.umls.server.model.content.SubsetMember;

/**
 * JAXB enabled implementation of {@link SubsetMemberList}.
 */
@XmlRootElement(name = "subsetMemberList")
public class SubsetMemberListJpa
    extends
    AbstractResultList<SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset>>
    implements SubsetMemberList {


  /* see superclass */
  @Override
  @XmlElement(type = AbstractSubsetMember.class, name = "members")
  public List<SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset>> getObjects() {
    return super.getObjectsTransient();
  }

}
