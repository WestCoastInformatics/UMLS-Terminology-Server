/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.meta;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.envers.Audited;

import com.wci.umls.server.model.meta.RelationshipType;

/**
 * JPA-enabled implementation of {@link RelationshipType}.
 */
@Entity
@Table(name = "relationship_types", uniqueConstraints = @UniqueConstraint(columnNames = {
  "abbreviation"
}))
@Audited
@XmlRootElement(name = "relationshipType")
public class RelationshipTypeJpa extends AbstractAbbreviation implements
    RelationshipType {

  
  /** The concept. */
  @OneToOne(targetEntity = RelationshipTypeJpa.class, optional = true)
  private RelationshipType inverse;

  /**
   * Instantiates an empty {@link RelationshipTypeJpa}.
   */
  public RelationshipTypeJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link RelationshipTypeJpa} from the specified parameters.
   *
   * @param rela the rela
   */
  public RelationshipTypeJpa(RelationshipType rela) {
    super(rela);
    inverse = rela.getInverse();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.RelationshipType#getInverse()
   */
  @Override
  @XmlTransient
  public RelationshipType getInverse() {
    return inverse;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.RelationshipType#setInverse(com
   * .wci.umls.server.model.meta.RelationshipType)
   */
  @Override
  public void setInverse(RelationshipType inverse) {
    this.inverse = inverse;
  }

  /**
   * Returns the inverse abbreviation. For JAXB.
   *
   * @return the inverse abbreviation
   */
  @XmlElement
  public String getInverseAbbreviation() {
    return inverse == null ? null : inverse.getAbbreviation();
  }

  /**
   * Returns the inverse id. For JAXB.
   *
   * @return the inverse id
   */
  @XmlElement
  public Long getInverseId() {
    return inverse == null ? null : inverse.getId();
  }

  /**
   * Sets the inverse abbreviation. For JAXB.
   *
   * @param inverseAbbreviation the inverse abbreviation
   */
  public void setInverseAbbreviation(String inverseAbbreviation) {
    if (inverse == null) {
      inverse = new RelationshipTypeJpa();
    }
    inverse.setAbbreviation(inverseAbbreviation);
  }

  /**
   * Sets the inverse id. For JAXB.
   *
   * @param inverseId the inverse id
   */
  public void setInverseId(Long inverseId) {
    if (inverse == null) {
      inverse = new RelationshipTypeJpa();
    }
    inverse.setId(inverseId);
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.jpa.meta.AbstractAbbreviation#toString()
   */
  @Override
  public String toString() {
    return "RelationshipTypeJpa [] " + super.toString();
  }

}
