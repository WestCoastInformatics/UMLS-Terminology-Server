/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.content;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;

import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.DescriptorTransitiveRelationship;

/**
 * JPA-enabled implementation of {@link DescriptorTransitiveRelationship}.
 */
@Entity
@Table(name = "descriptor_transitive_rels", uniqueConstraints = @UniqueConstraint(columnNames = {
    "terminologyId", "terminology", "terminologyVersion", "id"
}))
@Audited
@XmlRootElement(name = "descriptorTransitiveRel")
public class DescriptorTransitiveRelationshipJpa extends
    AbstractTransitiveRelationship<Descriptor> implements
    DescriptorTransitiveRelationship {

  /** The super type. */
  @ManyToOne(targetEntity = DescriptorJpa.class, optional = false)
  @JoinColumn(nullable = true)
  private Descriptor superType;

  /** The sub type. */
  @ManyToOne(targetEntity = DescriptorJpa.class, optional = false)
  @JoinColumn(nullable = true)
  private Descriptor subType;

  /**
   * Instantiates an empty {@link DescriptorTransitiveRelationshipJpa}.
   */
  public DescriptorTransitiveRelationshipJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link DescriptorTransitiveRelationshipJpa} from the
   * specified parameters.
   *
   * @param relationship the relationship
   * @param deepCopy the deep copy
   */
  public DescriptorTransitiveRelationshipJpa(
      DescriptorTransitiveRelationship relationship, boolean deepCopy) {
    super(relationship, deepCopy);
    superType = relationship.getSuperType();
    subType = relationship.getSubType();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.TransitiveRelationship#getSuperType()
   */
  @Override
  public Descriptor getSuperType() {
    return superType;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.TransitiveRelationship#setSuperType(com
   * .wci.umls.server.model.content.AtomClass)
   */
  @Override
  public void setSuperType(Descriptor ancestor) {
    this.superType = ancestor;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.TransitiveRelationship#getSubType()
   */
  @Override
  public Descriptor getSubType() {
    return subType;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.TransitiveRelationship#setSubType(com
   * .wci.umls.server.model.content.AtomClass)
   */
  @Override
  public void setSubType(Descriptor descendant) {
    this.subType = descendant;
  }

}
