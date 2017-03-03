/*
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers.content;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.helpers.AbstractResultList;
import com.wci.umls.server.helpers.ComponentInfo;
import com.wci.umls.server.helpers.content.RelationshipList;
import com.wci.umls.server.jpa.content.AbstractRelationship;
import com.wci.umls.server.model.content.Relationship;

/**
 * JAXB enabled implementation of {@link RelationshipList}.
 */
@XmlRootElement(name = "relationshipList")
public class RelationshipListJpa
    extends
    AbstractResultList<Relationship<? extends ComponentInfo, ? extends ComponentInfo>>
    implements RelationshipList {

  /* see superclass */
  @Override
  @XmlElement(type = AbstractRelationship.class, name = "relationships")
  public List<Relationship<? extends ComponentInfo, ? extends ComponentInfo>> getObjects() {
    return super.getObjectsTransient();
  }

}
