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

import com.wci.umls.server.model.meta.AdditionalRelationshipType;

/**
 * JPA-enabled implementation of {@link AdditionalRelationshipType}.
 */
@Entity
@Table(name = "additional_relationship_types", uniqueConstraints = @UniqueConstraint(columnNames = {
  "abbreviation"
}))
@Audited
@XmlRootElement(name = "additionalRelationshipType")
public class AdditionalRelationshipTypeJpa extends AbstractAbbreviation
    implements AdditionalRelationshipType {

  /** The concept. */
  @OneToOne(targetEntity = AdditionalRelationshipTypeJpa.class, optional = true)
  private AdditionalRelationshipType inverse;

  /**
   * Instantiates an empty {@link AdditionalRelationshipTypeJpa}.
   */
  public AdditionalRelationshipTypeJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link AdditionalRelationshipTypeJpa} from the specified
   * parameters.
   *
   * @param rela the rela
   */
  public AdditionalRelationshipTypeJpa(AdditionalRelationshipType rela) {
    super(rela);
    inverse = rela.getInverse();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.AdditionalRelationshipType#getInverse()
   */
  @Override
  @XmlTransient
  public AdditionalRelationshipType getInverse() {
    return inverse;
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
      inverse = new AdditionalRelationshipTypeJpa();
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
      inverse = new AdditionalRelationshipTypeJpa();
    }
    inverse.setId(inverseId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.AdditionalRelationshipType#setInverse(com
   * .wci.umls.server.model.meta.AdditionalRelationshipType)
   */
  @Override
  public void setInverse(AdditionalRelationshipType inverse) {
    this.inverse = inverse;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.meta.AbstractAbbreviation#hashCode()
   */
  @Override
  public int hashCode() {
    return super.hashCode();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.meta.AbstractAbbreviation#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    return super.equals(obj);
  }

  // TODO
  // isreflexive, is transitive, is functional, etc.
  // domain/range
  // is non-grouping
  // property chains.

}
