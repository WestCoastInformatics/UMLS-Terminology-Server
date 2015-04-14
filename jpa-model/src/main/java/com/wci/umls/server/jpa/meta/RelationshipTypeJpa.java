/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.meta;

import javax.persistence.Column;
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
  
  /**  The grouping type. */
  @Column(nullable = false)
  private boolean groupingType = true;
  
  /** The concept. */
  @OneToOne(targetEntity = RelationshipTypeJpa.class, optional = false)
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
    groupingType = rela.isGroupingType();
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
   * @see com.wci.umls.server.model.meta.RelationshipType#isGroupingType()
   */
  @Override
  public boolean isGroupingType() { return groupingType; }
  
  /* (non-Javadoc)
   * @see com.wci.umls.server.model.meta.RelationshipType#setGroupingType(boolean)
   */
  @Override
  public void setGroupingType(boolean groupingType) {
    this.groupingType = groupingType;
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.jpa.meta.AbstractAbbreviation#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + (groupingType ? 1231 : 1237);
    return result;
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.jpa.meta.AbstractAbbreviation#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    RelationshipTypeJpa other = (RelationshipTypeJpa) obj;
    if (groupingType != other.groupingType)
      return false;
    return true;
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.jpa.meta.AbstractAbbreviation#toString()
   */
  @Override
  public String toString() {
    return "RelationshipTypeJpa [groupingType=" + groupingType + "]";
  }

  
  // TODO
  // isreflexive, is transitive, is functional, etc.
  // domain/range
  // is non-grouping
  // property chains.

}
