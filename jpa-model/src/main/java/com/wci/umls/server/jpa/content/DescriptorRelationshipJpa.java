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
import com.wci.umls.server.model.content.DescriptorRelationship;

/**
 * JPA-enabled implementation of {@link DescriptorRelationship}.
 */
@Entity
@Table(name = "descriptor_relationships", uniqueConstraints = @UniqueConstraint(columnNames = {
    "terminologyId", "terminology", "terminologyVersion", "id"
}))
@Audited
@XmlRootElement(name = "descriptorRelationship")
public class DescriptorRelationshipJpa extends
    AbstractRelationship<Descriptor, Descriptor> implements
    DescriptorRelationship {

  /** The from concept. */
  @ManyToOne(targetEntity = DescriptorJpa.class, optional = false)
  @JoinColumn(nullable = true)
  private Descriptor from;

  /** the to concept. */
  @ManyToOne(targetEntity = DescriptorJpa.class, optional = false)
  @JoinColumn(nullable = true)
  private Descriptor to;

  /**
   * Instantiates an empty {@link DescriptorRelationshipJpa}.
   */
  public DescriptorRelationshipJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link DescriptorRelationshipJpa} from the specified
   * parameters.
   *
   * @param relationship the concept relationship
   * @param deepCopy the deep copy
   */
  public DescriptorRelationshipJpa(DescriptorRelationship relationship,
      boolean deepCopy) {
    super(relationship, deepCopy);
    to = relationship.getTo();
    from = relationship.getFrom();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.Relationship#getFrom()
   */
  @Override
  public Descriptor getFrom() {
    return from;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.Relationship#setFrom(com.wci.umls.server
   * .model.content.Component)
   */
  @Override
  public void setFrom(Descriptor component) {
    this.from = component;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.Relationship#getTo()
   */
  @Override
  public Descriptor getTo() {
    return to;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.Relationship#setTo(com.wci.umls.server
   * .model.content.Component)
   */
  @Override
  public void setTo(Descriptor component) {
    this.to = component;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.content.AbstractRelationship#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((from == null) ? 0 : from.hashCode());
    result = prime * result + ((to == null) ? 0 : to.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.content.AbstractRelationship#equals(java.lang.Object
   * )
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    DescriptorRelationshipJpa other = (DescriptorRelationshipJpa) obj;
    if (from == null) {
      if (other.from != null)
        return false;
    } else if (!from.equals(other.from))
      return false;
    if (to == null) {
      if (other.to != null)
        return false;
    } else if (!to.equals(other.to))
      return false;
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.content.AbstractRelationship#toString()
   */
  @Override
  public String toString() {
    return "DescriptorRelationshipJpa [from=" + from + ", to=" + to + "]";
  }

}
