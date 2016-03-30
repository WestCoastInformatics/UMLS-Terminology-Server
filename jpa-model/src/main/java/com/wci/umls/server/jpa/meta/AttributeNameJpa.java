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
    // "id" needed here because ATN sometimes has multiple abbreviations
    // that are the same in a case-insensitive way, which is how the
    // constraint works in MySQL when using standard utf8 collation/charset
    "abbreviation", "terminology", "id"
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

  /** The annotation. */
  @Column(nullable = false)
  private boolean annotation;

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
    annotation = atn.isAnnotation();
    universalQuantification = atn.isUniversalQuantification();
    existentialQuantification = atn.isExistentialQuantification();
  }

  /* see superclass */
  @Override
  public String getDomainId() {
    return domainId;
  }

  /* see superclass */
  @Override
  public void setDomainId(String domainId) {
    this.domainId = domainId;
  }

  /* see superclass */
  @Override
  @XmlTransient
  public AttributeName getEquivalentName() {
    return equivalentName;
  }

  /* see superclass */
  @Override
  public void setEquivalentName(AttributeName equivalentName) {
    this.equivalentName = equivalentName;
  }

  /* see superclass */
  @Override
  public String getRangeId() {
    return rangeId;
  }

  /* see superclass */
  @Override
  public void setRangeId(String rangeId) {
    this.rangeId = rangeId;
  }

  /* see superclass */
  @Override
  @XmlTransient
  public AttributeName getSuperName() {
    return superName;
  }

  /* see superclass */
  @Override
  public void setSuperName(AttributeName superName) {
    this.superName = superName;
  }

  /* see superclass */
  @Override
  public boolean isExistentialQuantification() {
    return existentialQuantification;
  }

  /* see superclass */
  @Override
  public void setExistentialQuantification(boolean existentialQuantification) {
    this.existentialQuantification = existentialQuantification;
  }

  /* see superclass */
  @Override
  public boolean isUniversalQuantification() {
    return universalQuantification;
  }

  /* see superclass */
  @Override
  public void setUniversalQuantification(boolean universalQuantification) {
    this.universalQuantification = universalQuantification;
  }

  /* see superclass */
  @Override
  public boolean isFunctional() {
    return functional;
  }

  /* see superclass */
  @Override
  public void setFunctional(boolean functional) {
    this.functional = functional;
  }

  /* see superclass */
  @Override
  public boolean isAnnotation() {
    return annotation;
  }

  /* see superclass */
  @Override
  public void setAnnotation(boolean annotation) {
    this.annotation = annotation;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((domainId == null) ? 0 : domainId.hashCode());
    result = prime * result + (existentialQuantification ? 1231 : 1237);
    result = prime * result + (functional ? 1231 : 1237);
    result = prime * result + (annotation ? 1231 : 1237);
    result = prime * result + ((rangeId == null) ? 0 : rangeId.hashCode());
    result = prime * result + (universalQuantification ? 1231 : 1237);
    return result;
  }

  /* see superclass */
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
    if (annotation != other.annotation)
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

  /* see superclass */
  @Override
  public String toString() {
    return "AttributeNameJpa [" + super.toString() + "]";
  }

}
