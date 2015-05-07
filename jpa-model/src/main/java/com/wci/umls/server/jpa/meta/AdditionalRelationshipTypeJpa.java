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

  /** The inverse type. */
  @OneToOne(targetEntity = AdditionalRelationshipTypeJpa.class, optional = true)
  private AdditionalRelationshipType inverseType;

  /** The equivalent type. */
  @OneToOne(targetEntity = AdditionalRelationshipTypeJpa.class, optional = true)
  private AdditionalRelationshipType equivalentType;

  /** The super type. */
  @OneToOne(targetEntity = AdditionalRelationshipTypeJpa.class, optional = true)
  private AdditionalRelationshipType superType;

  /** The asymmetric. */
  @Column(nullable = false)
  private boolean asymmetric = false;

  /** The equivalent classes. */
  @Column(nullable = false)
  private boolean equivalentClasses = false;

  /** The existential quantification. */
  @Column(nullable = false)
  private boolean existentialQuantification = false;

  /** The functional. */
  @Column(nullable = false)
  private boolean functional = false;

  /** The inverse functional. */
  @Column(nullable = false)
  private boolean inverseFunctional = false;

  /** The irreflexive. */
  @Column(nullable = false)
  private boolean irreflexive = false;

  /** The reflexive. */
  @Column(nullable = false)
  private boolean reflexive = false;

  /** The symmetric. */
  @Column(nullable = false)
  private boolean symmetric = false;

  /** The transitive. */
  @Column(nullable = false)
  private boolean transitive = false;

  /** The universal quantification. */
  @Column(nullable = false)
  private boolean universalQuantification = false;

  /** The domain id. */
  @Column(nullable = true)
  private String domainId;

  /** The range id. */
  @Column(nullable = true)
  private String rangeId;

  /** The grouping type. */
  @Column(nullable = false)
  private boolean groupingType = true;

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
    inverseType = rela.getInverseType();
    equivalentType = rela.getEquivalentType();
    superType = rela.getSuperType();
    asymmetric = rela.isAsymmetric();
    equivalentClasses = rela.isEquivalentClasses();
    existentialQuantification = rela.isExistentialQuantification();
    functional = rela.isFunctional();
    inverseFunctional = rela.isInverseFunctional();
    irreflexive = rela.isIrreflexive();
    reflexive = rela.isReflexive();
    symmetric = rela.isSymmetric();
    transitive = rela.isTransitive();
    universalQuantification = rela.isUniversalQuantification();
    domainId = rela.getDomainId();
    rangeId = rela.getRangeId();
    groupingType = rela.isGroupingType();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.AdditionalRelationshipType#getInverseType()
   */
  @Override
  @XmlTransient
  public AdditionalRelationshipType getInverseType() {
    return inverseType;
  }

  /**
   * Returns the inverse type abbreviation. For JAXB.
   *
   * @return the inverse type abbreviation
   */
  @XmlElement
  public String getInverseTypeAbbreviation() {
    return inverseType == null ? null : inverseType.getAbbreviation();
  }

  /**
   * Returns the inverse type id. For JAXB.
   *
   * @return the inverse type id
   */
  @XmlElement
  public Long getInverseTypeId() {
    return inverseType == null ? null : inverseType.getId();
  }

  /**
   * Sets the inverse type abbreviation. For JAXB.
   *
   * @param inverseTypeAbbreviation the inverse type abbreviation
   */
  public void setInverseTypeAbbreviation(String inverseTypeAbbreviation) {
    if (inverseType == null) {
      inverseType = new AdditionalRelationshipTypeJpa();
    }
    inverseType.setAbbreviation(inverseTypeAbbreviation);
  }

  /**
   * Sets the inverse type id. For JAXB.
   *
   * @param inverseTypeId the inverse type id
   */
  public void setInverseTypeId(Long inverseTypeId) {
    if (inverseType == null) {
      inverseType = new AdditionalRelationshipTypeJpa();
    }
    inverseType.setId(inverseTypeId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.AdditionalRelationshipType#setInverseType
   * (com .wci.umls.server.model.meta.AdditionalRelationshipType)
   */
  @Override
  public void setInverseType(AdditionalRelationshipType inverseType) {
    this.inverseType = inverseType;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + (asymmetric ? 1231 : 1237);
    result = prime * result + ((domainId == null) ? 0 : domainId.hashCode());
    result = prime * result + (equivalentClasses ? 1231 : 1237);
    result = prime * result + (existentialQuantification ? 1231 : 1237);
    result = prime * result + (functional ? 1231 : 1237);
    result = prime * result + (inverseFunctional ? 1231 : 1237);
    result = prime * result + (irreflexive ? 1231 : 1237);
    result = prime * result + ((rangeId == null) ? 0 : rangeId.hashCode());
    result = prime * result + (reflexive ? 1231 : 1237);
    result = prime * result + (symmetric ? 1231 : 1237);
    result = prime * result + (transitive ? 1231 : 1237);
    result = prime * result + (universalQuantification ? 1231 : 1237);
    result = prime * result + (groupingType ? 1231 : 1237);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    AdditionalRelationshipTypeJpa other = (AdditionalRelationshipTypeJpa) obj;
    if (asymmetric != other.asymmetric)
      return false;
    if (domainId == null) {
      if (other.domainId != null)
        return false;
    } else if (!domainId.equals(other.domainId))
      return false;
    if (equivalentClasses != other.equivalentClasses)
      return false;
    if (existentialQuantification != other.existentialQuantification)
      return false;
    if (functional != other.functional)
      return false;
    if (inverseFunctional != other.inverseFunctional)
      return false;
    if (irreflexive != other.irreflexive)
      return false;
    if (rangeId == null) {
      if (other.rangeId != null)
        return false;
    } else if (!rangeId.equals(other.rangeId))
      return false;
    if (reflexive != other.reflexive)
      return false;
    if (symmetric != other.symmetric)
      return false;
    if (transitive != other.transitive)
      return false;
    if (universalQuantification != other.universalQuantification)
      return false;
    if (groupingType != other.groupingType)
      return false;
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.AdditionalRelationshipType#getEquivalentType
   * ()
   */
  @Override
  @XmlTransient
  public AdditionalRelationshipType getEquivalentType() {
    return equivalentType;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.AdditionalRelationshipType#setEquivalentType
   * (com.wci.umls.server.model.meta.AdditionalRelationshipType)
   */
  @Override
  public void setEquivalentType(AdditionalRelationshipType equivalentType) {
    this.equivalentType = equivalentType;
  }

  /**
   * Returns the equivalent type abbreviation. For JAXB.
   *
   * @return the equivalent type abbreviation
   */
  @XmlElement
  public String getEquivalentTypeAbbreviation() {
    return equivalentType == null ? null : equivalentType.getAbbreviation();
  }

  /**
   * Returns the equivalent type id. For JAXB.
   *
   * @return the equivalent type id
   */
  @XmlElement
  public Long getEquivalentTypeId() {
    return equivalentType == null ? null : equivalentType.getId();
  }

  /**
   * Sets the equivalent type abbreviation. For JAXB.
   *
   * @param equivalentTypeAbbreviation the equivalent type abbreviation
   */
  public void setEquivalentTypeAbbreviation(String equivalentTypeAbbreviation) {
    if (equivalentType == null) {
      equivalentType = new AdditionalRelationshipTypeJpa();
    }
    equivalentType.setAbbreviation(equivalentTypeAbbreviation);
  }

  /**
   * Sets the equivalent type id. For JAXB.
   *
   * @param equivalentTypeId the equivalent type id
   */
  public void setEquivalentTypeId(Long equivalentTypeId) {
    if (equivalentType == null) {
      equivalentType = new AdditionalRelationshipTypeJpa();
    }
    equivalentType.setId(equivalentTypeId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.AdditionalRelationshipType#getSuperType()
   */
  @Override
  @XmlTransient
  public AdditionalRelationshipType getSuperType() {
    return superType;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.AdditionalRelationshipType#setSuperType(
   * com.wci.umls.server.model.meta.AdditionalRelationshipType)
   */
  @Override
  public void setSuperType(AdditionalRelationshipType superType) {
    this.superType = superType;
  }

  /**
   * Returns the super type abbreviation. For JAXB.
   *
   * @return the super type abbreviation
   */
  @XmlElement
  public String getSuperTypeAbbreviation() {
    return superType == null ? null : superType.getAbbreviation();
  }

  /**
   * Returns the super type id. For JAXB.
   *
   * @return the super type id
   */
  @XmlElement
  public Long getSuperTypeId() {
    return superType == null ? null : superType.getId();
  }

  /**
   * Sets the super type abbreviation. For JAXB.
   *
   * @param superTypeAbbreviation the super type abbreviation
   */
  public void setSuperTypeAbbreviation(String superTypeAbbreviation) {
    if (superType == null) {
      superType = new AdditionalRelationshipTypeJpa();
    }
    superType.setAbbreviation(superTypeAbbreviation);
  }

  /**
   * Sets the super type id. For JAXB.
   *
   * @param superTypeId the super type id
   */
  public void setSuperTypeId(Long superTypeId) {
    if (superType == null) {
      superType = new AdditionalRelationshipTypeJpa();
    }
    superType.setId(superTypeId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.AdditionalRelationshipType#isAsymmetric()
   */
  @Override
  public boolean isAsymmetric() {
    return asymmetric;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.AdditionalRelationshipType#setAsymmetric
   * (boolean)
   */
  @Override
  public void setAsymmetric(boolean asymetric) {
    this.asymmetric = asymetric;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.AdditionalRelationshipType#isEquivalentClasses
   * ()
   */
  @Override
  public boolean isEquivalentClasses() {
    return equivalentClasses;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.AdditionalRelationshipType#setEquivalentClasses
   * (boolean)
   */
  @Override
  public void setEquivalentClasses(boolean equivalentClasses) {
    this.equivalentClasses = equivalentClasses;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.AdditionalRelationshipType#
   * isExistentialQuantification()
   */
  @Override
  public boolean isExistentialQuantification() {
    return existentialQuantification;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.AdditionalRelationshipType#
   * setExistentialQuantification(boolean)
   */
  @Override
  public void setExistentialQuantification(boolean existentialQuantification) {
    this.existentialQuantification = existentialQuantification;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.AdditionalRelationshipType#isFunctional()
   */
  @Override
  public boolean isFunctional() {
    return functional;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.AdditionalRelationshipType#setFunctional
   * (boolean)
   */
  @Override
  public void setFunctional(boolean functional) {
    this.functional = functional;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.AdditionalRelationshipType#isInverseFunctional
   * ()
   */
  @Override
  public boolean isInverseFunctional() {
    return inverseFunctional;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.AdditionalRelationshipType#setInverseFunctional
   * (boolean)
   */
  @Override
  public void setInverseFunctional(boolean inverseFunctional) {
    this.inverseFunctional = inverseFunctional;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.AdditionalRelationshipType#isIrreflexive()
   */
  @Override
  public boolean isIrreflexive() {
    return irreflexive;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.AdditionalRelationshipType#setIrreflexive
   * (boolean)
   */
  @Override
  public void setIrreflexive(boolean irreflexive) {
    this.irreflexive = irreflexive;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.AdditionalRelationshipType#isReflexive()
   */
  @Override
  public boolean isReflexive() {
    return reflexive;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.AdditionalRelationshipType#setReflexive(
   * boolean)
   */
  @Override
  public void setReflexive(boolean reflexive) {
    this.reflexive = reflexive;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.AdditionalRelationshipType#isSymmetric()
   */
  @Override
  public boolean isSymmetric() {
    return symmetric;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.AdditionalRelationshipType#setSymmetric(
   * boolean)
   */
  @Override
  public void setSymmetric(boolean symmetric) {
    this.symmetric = symmetric;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.AdditionalRelationshipType#isTransitive()
   */
  @Override
  public boolean isTransitive() {
    return transitive;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.AdditionalRelationshipType#setTransitive
   * (boolean)
   */
  @Override
  public void setTransitive(boolean transitive) {
    this.transitive = transitive;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.AdditionalRelationshipType#
   * isUniversalQuantification()
   */
  @Override
  public boolean isUniversalQuantification() {
    return universalQuantification;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.AdditionalRelationshipType#
   * setUniversalQuantification(boolean)
   */
  @Override
  public void setUniversalQuantification(boolean universalQuantification) {
    this.universalQuantification = universalQuantification;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.AdditionalRelationshipType#getDomainId()
   */
  @Override
  public String getDomainId() {
    return domainId;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.AdditionalRelationshipType#setDomainId(java
   * .lang.String)
   */
  @Override
  public void setDomainId(String domainId) {
    this.domainId = domainId;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.AdditionalRelationshipType#getRangeId()
   */
  @Override
  public String getRangeId() {
    return rangeId;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.AdditionalRelationshipType#setRangeId(java
   * .lang.String)
   */
  @Override
  public void setRangeId(String rangeId) {
    this.rangeId = rangeId;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.RelationshipType#isGroupingType()
   */
  @Override
  public boolean isGroupingType() {
    return groupingType;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.RelationshipType#setGroupingType(boolean)
   */
  @Override
  public void setGroupingType(boolean groupingType) {
    this.groupingType = groupingType;
  }

  @Override
  public String toString() {
    return "AdditionalRelationshipTypeJpa [asymmetric=" + asymmetric
        + ", equivalentClasses=" + equivalentClasses
        + ", existentialQuantification=" + existentialQuantification
        + ", functional=" + functional + ", inverseFunctional="
        + inverseFunctional + ", irreflexive=" + irreflexive + ", reflexive="
        + reflexive + ", symmetric=" + symmetric + ", transitive=" + transitive
        + ", universalQuantification=" + universalQuantification
        + ", domainId=" + domainId + ", rangeId=" + rangeId + "]";
  }

}
