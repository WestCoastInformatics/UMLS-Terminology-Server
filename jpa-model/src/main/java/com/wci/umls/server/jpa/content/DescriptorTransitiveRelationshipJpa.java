/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.content;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.envers.Audited;

import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.DescriptorTransitiveRelationship;

/**
 * JPA-enabled implementation of {@link DescriptorTransitiveRelationship}.
 */
@Entity
@Table(name = "descriptor_transitive_rels", uniqueConstraints = @UniqueConstraint(columnNames = {
    "terminologyId", "terminology", "version", "id"
}))
@Audited
@XmlRootElement(name = "descriptorTransitiveRel")
public class DescriptorTransitiveRelationshipJpa extends
    AbstractTransitiveRelationship<Descriptor> implements
    DescriptorTransitiveRelationship {

  /** The super type. */
  @ManyToOne(targetEntity = DescriptorJpa.class, fetch = FetchType.EAGER, optional = false)
  @JoinColumn(nullable = false)
  private Descriptor superType;

  /** The sub type. */
  @ManyToOne(targetEntity = DescriptorJpa.class, fetch = FetchType.EAGER, optional = false)
  @JoinColumn(nullable = false)
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

  /* see superclass */
  @XmlTransient
  @Override
  public Descriptor getSuperType() {
    return superType;
  }

  /* see superclass */
  @Override
  public void setSuperType(Descriptor ancestor) {
    this.superType = ancestor;
  }

  /**
   * Returns the super type id. For JAXB.
   *
   * @return the super type id
   */
  public Long getSuperTypeId() {
    return superType == null ? null : superType.getId();
  }

  /**
   * Sets the super type id.
   *
   * @param id the super type id
   */
  public void setSuperTypeId(Long id) {
    if (superType == null) {
      superType = new DescriptorJpa();
    }
    superType.setId(id);
  }

  /**
   * Returns the super type terminology id. For JAXB.
   *
   * @return the super type terminology id
   */
  public String getSuperTypeTerminologyId() {
    return superType == null ? null : superType.getTerminologyId();
  }

  /**
   * Sets the super type terminology id.
   *
   * @param terminologyId the super type terminology id
   */
  public void setSuperTypeTerminologyId(String terminologyId) {
    if (superType == null) {
      superType = new DescriptorJpa();
    }
    superType.setTerminologyId(terminologyId);
  }

  /**
   * Returns the super type terminology id. For JAXB.
   *
   * @return the super type terminology id
   */
  public String getSuperTypeTerminology() {
    return superType == null ? null : superType.getTerminology();
  }

  /**
   * Sets the super type terminology.
   *
   * @param terminology the super type terminology
   */
  public void setSuperTypeTerminology(String terminology) {
    if (superType == null) {
      superType = new DescriptorJpa();
    }
    superType.setTerminology(terminology);
  }

  /**
   * Returns the super type terminology. For JAXB.
   *
   * @return the super type terminology
   */
  public String getSuperTypeVersion() {
    return superType == null ? null : superType.getVersion();
  }

  /**
   * Sets the super type version.
   *
   * @param version the super type version
   */
  public void setSuperTypeVersion(String version) {
    if (superType == null) {
      superType = new DescriptorJpa();
    }
    superType.setVersion(version);
  }

  /**
   * Returns the super type term. For JAXB.
   *
   * @return the super type term
   */
  public String getSuperTypeName() {
    return superType == null ? null : superType.getName();
  }

  /**
   * Sets the super type term.
   *
   * @param term the super type term
   */
  public void setSuperTypeName(String term) {
    if (superType == null) {
      superType = new DescriptorJpa();
    }
    superType.setName(term);
  }

  /* see superclass */
  @XmlTransient
  @Override
  public Descriptor getSubType() {
    return subType;
  }

  /* see superclass */
  @Override
  public void setSubType(Descriptor descendant) {
    this.subType = descendant;
  }

  /**
   * Returns the sub type id. For JAXB.
   *
   * @return the sub type id
   */
  public Long getSubTypeId() {
    return subType == null ? null : subType.getId();
  }

  /**
   * Sets the sub type id.
   *
   * @param id the sub type id
   */
  public void setSubTypeId(Long id) {
    if (subType == null) {
      subType = new DescriptorJpa();
    }
    subType.setId(id);
  }

  /**
   * Returns the sub type terminology id. For JAXB.
   *
   * @return the sub type terminology id
   */
  public String getSubTypeTerminologyId() {
    return subType == null ? null : subType.getTerminologyId();
  }

  /**
   * Sets the sub type terminology id.
   *
   * @param terminologyId the sub type terminology id
   */
  public void setSubTypeTerminologyId(String terminologyId) {
    if (subType == null) {
      subType = new DescriptorJpa();
    }
    subType.setTerminologyId(terminologyId);
  }

  /**
   * Returns the sub type terminology id. For JAXB.
   *
   * @return the sub type terminology id
   */
  public String getSubTypeTerminology() {
    return subType == null ? null : subType.getTerminology();
  }

  /**
   * Sets the sub type terminology.
   *
   * @param terminology the sub type terminology
   */
  public void setSubTypeTerminology(String terminology) {
    if (subType == null) {
      subType = new DescriptorJpa();
    }
    subType.setTerminology(terminology);
  }

  /**
   * Returns the sub type terminology. For JAXB.
   *
   * @return the sub type terminology
   */
  public String getSubTypeVersion() {
    return subType == null ? null : subType.getVersion();
  }

  /**
   * Sets the sub type version.
   *
   * @param version the sub type version
   */
  public void setSubTypeVersion(String version) {
    if (subType == null) {
      subType = new DescriptorJpa();
    }
    subType.setVersion(version);
  }

  /**
   * Returns the sub type term. For JAXB.
   *
   * @return the sub type term
   */
  public String getSubTypeName() {
    return subType == null ? null : subType.getName();
  }

  /**
   * Sets the sub type term.
   *
   * @param term the sub type term
   */
  public void setSubTypeName(String term) {
    if (subType == null) {
      subType = new DescriptorJpa();
    }
    subType.setName(term);
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((subType == null) ? 0 : subType.hashCode());
    result = prime * result + ((superType == null) ? 0 : superType.hashCode());
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
    DescriptorTransitiveRelationshipJpa other =
        (DescriptorTransitiveRelationshipJpa) obj;
    if (subType == null) {
      if (other.subType != null)
        return false;
    } else if (!subType.equals(other.subType))
      return false;
    if (superType == null) {
      if (other.superType != null)
        return false;
    } else if (!superType.equals(other.superType))
      return false;
    return true;
  }

}
