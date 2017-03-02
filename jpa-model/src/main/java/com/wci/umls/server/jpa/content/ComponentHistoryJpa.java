/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.content;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Store;

import com.wci.umls.server.model.content.ComponentHistory;

/**
 * JPA-enabled implementation of {@link ComponentHistory}.
 */
@Entity
@Table(name = "component_histories", uniqueConstraints = @UniqueConstraint(columnNames = {
    "terminologyId", "terminology", "version", "id"
}))
@Audited
@XmlRootElement(name = "componentHistory")
public class ComponentHistoryJpa extends AbstractComponent
    implements ComponentHistory {

  /** The referenced concept's terminology Id. */
  @Column(nullable = true)
  private String referencedTerminologyId;

  /** The reason. */
  @Column(nullable = true, length = 4000)
  private String reason;

  /** The relationship type. */
  @Column(nullable = true)
  private String relationshipType;

  /** The additional relationship type. */
  @Column(nullable = true)
  private String additionalRelationshipType;

  /** The release. */
  @Column(nullable = false)
  private String associatedRelease;

  /**
   * Instantiates an empty {@link ComponentHistoryJpa}.
   */
  public ComponentHistoryJpa() {
    // n/a
  }

  /**
   * Instantiates a {@link ComponentHistoryJpa} from the specified parameters.
   *
   * @param h the h
   */
  public ComponentHistoryJpa(ComponentHistory h) {
    super(h);
    referencedTerminologyId = h.getReferencedTerminologyId();
    reason = h.getReason();
    relationshipType = h.getRelationshipType();
    additionalRelationshipType = h.getAdditionalRelationshipType();
    associatedRelease = h.getAssociatedRelease();
  }

  /* see superclass */
  @Override
  public void setReferencedTerminologyId(String referencedTerminologyId) {
    this.referencedTerminologyId = referencedTerminologyId;
  }

  /**
   * Returns the referenced concept terminology id.
   *
   * @return the referenced concept terminology id
   */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getReferencedTerminologyId() {
    return referencedTerminologyId;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
  public String getReason() {
    return reason;
  }

  /* see superclass */
  @Override
  public void setReason(String reason) {
    this.reason = reason;
  }

  /* see superclass */
  @Override
  public String getRelationshipType() {
    return relationshipType;
  }

  /* see superclass */
  @Override
  public void setRelationshipType(String relationshipType) {
    this.relationshipType = relationshipType;
  }

  /* see superclass */
  @Override
  public String getAdditionalRelationshipType() {
    return additionalRelationshipType;
  }

  /* see superclass */
  @Override
  public void setAdditionalRelationshipType(String relationshipType) {
    this.additionalRelationshipType = relationshipType;
  }

  /* see superclass */
  @Override
  public String getAssociatedRelease() {
    return associatedRelease;
  }

  /* see superclass */
  @Override
  public void setAssociatedRelease(String associatedRelease) {
    this.associatedRelease = associatedRelease;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((additionalRelationshipType == null) ? 0
        : additionalRelationshipType.hashCode());
    result = prime * result + ((reason == null) ? 0 : reason.hashCode());
    result = prime * result + ((referencedTerminologyId == null) ? 0
        : referencedTerminologyId.hashCode());
    result = prime * result
        + ((relationshipType == null) ? 0 : relationshipType.hashCode());
    result = prime * result
        + ((associatedRelease == null) ? 0 : associatedRelease.hashCode());
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
    ComponentHistoryJpa other = (ComponentHistoryJpa) obj;
    if (additionalRelationshipType == null) {
      if (other.additionalRelationshipType != null)
        return false;
    } else if (!additionalRelationshipType
        .equals(other.additionalRelationshipType))
      return false;
    if (reason == null) {
      if (other.reason != null)
        return false;
    } else if (!reason.equals(other.reason))
      return false;
    if (referencedTerminologyId == null) {
      if (other.referencedTerminologyId != null)
        return false;
    } else if (!referencedTerminologyId.equals(other.referencedTerminologyId))
      return false;
    if (relationshipType == null) {
      if (other.relationshipType != null)
        return false;
    } else if (!relationshipType.equals(other.relationshipType))
      return false;
    if (associatedRelease == null) {
      if (other.associatedRelease != null)
        return false;
    } else if (!associatedRelease.equals(other.associatedRelease))
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "ComponentHistoryJpa [referencedConcept=" + referencedTerminologyId
        + ", reason=" + reason + ", relationshipType=" + relationshipType
        + ", additionalRelationshipType=" + additionalRelationshipType
        + ", release=" + associatedRelease + "]";
  }

}
