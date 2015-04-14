/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers.content;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import com.wci.umls.server.helpers.AbstractResultList;
import com.wci.umls.server.helpers.content.RelationshipList;
import com.wci.umls.server.jpa.content.AbstractRelationship;
import com.wci.umls.server.model.content.CodeRelationship;
import com.wci.umls.server.model.content.ComponentHasAttributes;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.content.DescriptorRelationship;
import com.wci.umls.server.model.content.Relationship;

/**
 * JAXB enabled implementation of {@link RelationshipList}.
 */
@XmlRootElement(name = "relationshipList")
@XmlSeeAlso({
    CodeRelationship.class, ConceptRelationship.class,
    DescriptorRelationship.class
})
public class RelationshipListJpa
    extends
    AbstractResultList<Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes>>
    implements RelationshipList {

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.AbstractResultList#getObjects()
   */
  @Override
  @XmlElement(type = AbstractRelationship.class, name = "relationship")
  public List<Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes>> getObjects() {
    return super.getObjects();
  }

}
