/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.meta;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.envers.Audited;

import com.wci.umls.server.model.meta.AttributeName;

/**
 * JPA-enabled implementation of {@link AttributeName}.
 */
@Entity
@Table(name = "attribute_names", uniqueConstraints = @UniqueConstraint(columnNames = {
  "abbreviation"
}))
@Audited
@XmlRootElement(name = "attributeName")
public class AttributeNameJpa extends AbstractAbbreviation implements
    AttributeName {

  /** The domain id. */
  @Column(nullable = true)
  private String domainId;

  /** The range id. */
  @Column(nullable = true)
  private String rangeId;

  /** The equivalent name. */
  @OneToOne(targetEntity = AttributeNameJpa.class, optional = true)
  private AttributeName equivalentName;

  /** The super name. */
  @OneToOne(targetEntity = AttributeNameJpa.class, optional = true)
  private AttributeName superName;

  /** The existential quantification. */
  @Column(nullable = false)
  private boolean existentialQuantification;

  /** The universal quantification. */
  @Column(nullable = false)
  private boolean universalQuantification;

  /** The funcitonal. */
  @Column(nullable = false)
  private boolean functional;

  /**
   * Instantiates an empty {@link AttributeNameJpa}.
   */
  public AttributeNameJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link AttributeNameJpa} from the specified parameters.
   *
   * @param atn the atn
   */
  public AttributeNameJpa(AttributeName atn) {
    super(atn);
    domainId = atn.getDomainId();
    rangeId = atn.getRangeId();
    equivalentName = atn.getEquivalentName();
    superName = atn.getSuperName();
    functional = atn.isFunctional();
    universalQuantification = atn.isUniversalQuantification();
    existentialQuantification= atn.isExistentialQuantification();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.AttributeName#getDomainId()
   */
  @Override
  public String getDomainId() {
    return domainId;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.AttributeName#setDomainId(java.lang.String)
   */
  @Override
  public void setDomainId(String domainId) {
    this.domainId = domainId;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.AttributeName#getEquivalentName()
   */
  @Override
  @XmlTransient
  public AttributeName getEquivalentName() {
    return equivalentName;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.AttributeName#setEquivalentName(com.wci.
   * umls.server.model.meta.AttributeName)
   */
  @Override
  public void setEquivalentName(AttributeName equivalentName) {
    this.equivalentName = equivalentName;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.AttributeName#getRangeId()
   */
  @Override
  public String getRangeId() {
    return rangeId;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.AttributeName#setRangeId(java.lang.String)
   */
  @Override
  public void setRangeId(String rangeId) {
    this.rangeId = rangeId;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.AttributeName#getSuperName()
   */
  @Override
  @XmlTransient
  public AttributeName getSuperName() {
    return superName;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.AttributeName#setSuperName(com.wci.umls.
   * server.model.meta.AttributeName)
   */
  @Override
  public void setSuperName(AttributeName superName) {
    this.superName = superName;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.AttributeName#isExistentialQuantification()
   */
  @Override
  public boolean isExistentialQuantification() {
    return existentialQuantification;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.AttributeName#setExistentialQuantification
   * (boolean)
   */
  @Override
  public void setExistentialQuantification(boolean existentialQuantification) {
    this.existentialQuantification = existentialQuantification;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.AttributeName#isUniversalQuantification()
   */
  @Override
  public boolean isUniversalQuantification() {
    return universalQuantification;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.AttributeName#setUniversalQuantification
   * (boolean)
   */
  @Override
  public void setUniversalQuantification(boolean universalQuantification) {
    this.universalQuantification = universalQuantification;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.AttributeName#isFunctional()
   */
  @Override
  public boolean isFunctional() {
    return functional;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.AttributeName#setFunctional(boolean)
   */
  @Override
  public void setFunctional(boolean functional) {
    this.functional = functional;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.meta.AbstractAbbreviation#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((domainId == null) ? 0 : domainId.hashCode());
    result = prime * result + (existentialQuantification ? 1231 : 1237);
    result = prime * result + (functional ? 1231 : 1237);
    result = prime * result + ((rangeId == null) ? 0 : rangeId.hashCode());
    result = prime * result + (universalQuantification ? 1231 : 1237);
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.meta.AbstractAbbreviation#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    AttributeNameJpa other = (AttributeNameJpa) obj;
    if (domainId == null) {
      if (other.domainId != null)
        return false;
    } else if (!domainId.equals(other.domainId))
      return false;
    if (existentialQuantification != other.existentialQuantification)
      return false;
    if (functional != other.functional)
      return false;
    if (rangeId == null) {
      if (other.rangeId != null)
        return false;
    } else if (!rangeId.equals(other.rangeId))
      return false;
    if (universalQuantification != other.universalQuantification)
      return false;
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.meta.AbstractAbbreviation#toString()
   */
  @Override
  public String toString() {
    return "AttributeNameJpa [domainId=" + domainId + ", rangeId=" + rangeId
        + ", equivalentName=" + equivalentName + ", superName=" + superName
        + ", existentialQuantification=" + existentialQuantification
        + ", universalQuantification=" + universalQuantification
        + ", functional=" + functional + "]";
  }

}
