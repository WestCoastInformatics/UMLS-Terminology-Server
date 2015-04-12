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

import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptTransitiveRelationship;

/**
 * JPA-enabled implementation of {@link ConceptTransitiveRelationship}.
 */
@Entity
@Table(name = "concept_transitive_rels", uniqueConstraints = @UniqueConstraint(columnNames = {
    "terminologyId", "terminology", "terminologyVersion", "id"
}))
@Audited
@XmlRootElement(name = "conceptTransitiveRel")
public class ConceptTransitiveRelationshipJpa extends AbstractComponentHasAttributes
    implements ConceptTransitiveRelationship {

  /** The super type. */
  @ManyToOne(targetEntity = ConceptJpa.class, optional = false)
  @JoinColumn(nullable = true)
  private Concept superType;

  /** The sub type. */
  @ManyToOne(targetEntity = ConceptJpa.class, optional = false)
  @JoinColumn(nullable = true)
  private Concept subType;

  /**
   * Instantiates an empty {@link ConceptTransitiveRelationshipJpa}.
   */
  public ConceptTransitiveRelationshipJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link ConceptTransitiveRelationshipJpa} from the specified
   * parameters.
   *
   * @param relationship the relationship
   * @param deepCopy the deep copy
   */
  public ConceptTransitiveRelationshipJpa(
      ConceptTransitiveRelationship relationship, boolean deepCopy) {
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
  public Concept getSuperType() {
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
  public void setSuperType(Concept ancestor) {
    this.superType = ancestor;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.TransitiveRelationship#getSubType()
   */
  @Override
  public Concept getSubType() {
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
  public void setSubType(Concept descendant) {
    this.subType = descendant;
  }

}
