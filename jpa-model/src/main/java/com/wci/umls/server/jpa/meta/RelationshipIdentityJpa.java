/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.meta;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.model.meta.IdType;
import com.wci.umls.server.model.meta.RelationshipIdentity;

/**
 * JPA and JAXB enabled implementation of {@link RelationshipIdentity}.
 */
@Entity
@Table(name = "relationship_identity", uniqueConstraints = @UniqueConstraint(columnNames = {
    "additionalRelationshipType", "fromId", "fromTerminology", "fromType",
    "relationshipType", "terminology", "terminologyId", "toId", "toTerminology",
    "toType"
}))
@XmlRootElement(name = "relationshipIdentity")
public class RelationshipIdentityJpa implements RelationshipIdentity {

  /** The id. */
  @Id
  private Long id;

  /** The terminology. */
  @Column(nullable = false)
  private String terminology;

  /** The terminology id. */
  @Column(nullable = false)
  private String terminologyId;

  /** The relationship type. */
  @Column(nullable = false)
  private String relationshipType;

  /** The additional relationship type. */
  @Column(nullable = false)
  private String additionalRelationshipType;

  /** The from id. */
  @Column(nullable = false)
  private String fromId;

  /** The from type. */
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private IdType fromType;

  /** The from terminology. */
  @Column(nullable = false)
  private String fromTerminology;

  /** The to id. */
  @Column(nullable = false)
  private String toId;

  /** The to type. */
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private IdType toType;

  /** The to terminology. */
  @Column(nullable = false)
  private String toTerminology;

  /** The inverse id. */
  @Column(nullable = false)
  private Long inverseId;

  /**
   * Instantiates an empty {@link RelationshipIdentityJpa}.
   */
  public RelationshipIdentityJpa() {
    //
  }

  /**
   * Instantiates a {@link RelationshipIdentityJpa} from the specified
   * parameters.
   *
   * @param identity the identity
   */
  public RelationshipIdentityJpa(RelationshipIdentity identity) {
    id = identity.getId();
    terminology = identity.getTerminology();
    terminologyId = identity.getTerminologyId();
    relationshipType = identity.getRelationshipType();
    additionalRelationshipType = identity.getAdditionalRelationshipType();
    fromId = identity.getFromId();
    fromType = identity.getFromType();
    fromTerminology = identity.getFromTerminology();
    toId = identity.getToId();
    toType = identity.getToType();
    toTerminology = identity.getToTerminology();
    inverseId = identity.getInverseId();
  }

  /* see superclass */
  @Override
  public Long getId() {
    return id;
  }

  /* see superclass */
  @Override
  public void setId(Long id) {
    this.id = id;
  }

  /* see superclass */
  @Override
  public String getTerminologyId() {
    return terminologyId;
  }

  /* see superclass */
  @Override
  public void setTerminologyId(String terminologyId) {
    this.terminologyId = terminologyId;
  }

  /* see superclass */
  @Override
  public String getTerminology() {
    return terminology;
  }

  /* see superclass */
  @Override
  public void setTerminology(String terminology) {
    this.terminology = terminology;
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
  public void setAdditionalRelationshipType(String additionalRelationshipType) {
    this.additionalRelationshipType = additionalRelationshipType;
  }

  /* see superclass */
  @Override
  public String getFromId() {
    return fromId;
  }

  /* see superclass */
  @Override
  public void setFromId(String fromId) {
    this.fromId = fromId;
  }

  /* see superclass */
  @Override
  public IdType getFromType() {
    return fromType;
  }

  /* see superclass */
  @Override
  public void setFromType(IdType fromType) {
    this.fromType = fromType;
  }

  /* see superclass */
  @Override
  public String getFromTerminology() {
    return fromTerminology;
  }

  /* see superclass */
  @Override
  public void setFromTerminology(String fromTerminology) {
    this.fromTerminology = fromTerminology;
  }

  /* see superclass */
  @Override
  public String getToId() {
    return toId;
  }

  /* see superclass */
  @Override
  public void setToId(String toId) {
    this.toId = toId;
  }

  /* see superclass */
  @Override
  public IdType getToType() {
    return toType;
  }

  /* see superclass */
  @Override
  public void setToType(IdType toType) {
    this.toType = toType;
  }

  /* see superclass */
  @Override
  public String getToTerminology() {
    return toTerminology;
  }

  /* see superclass */
  @Override
  public void setToTerminology(String toTerminology) {
    this.toTerminology = toTerminology;
  }

  /* see superclass */
  @Override
  public Long getInverseId() {
    return inverseId;
  }

  /* see superclass */
  @Override
  public void setInverseId(Long inverseId) {
    this.inverseId = inverseId;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((additionalRelationshipType == null) ? 0
        : additionalRelationshipType.hashCode());
    result = prime * result + ((fromId == null) ? 0 : fromId.hashCode());
    result = prime * result
        + ((fromTerminology == null) ? 0 : fromTerminology.hashCode());
    result = prime * result + ((fromType == null) ? 0 : fromType.hashCode());
    result = prime * result + ((inverseId == null) ? 0 : inverseId.hashCode());
    result = prime * result
        + ((relationshipType == null) ? 0 : relationshipType.hashCode());
    result =
        prime * result + ((terminology == null) ? 0 : terminology.hashCode());
    result = prime * result
        + ((terminologyId == null) ? 0 : terminologyId.hashCode());
    result = prime * result + ((toId == null) ? 0 : toId.hashCode());
    result = prime * result
        + ((toTerminology == null) ? 0 : toTerminology.hashCode());
    result = prime * result + ((toType == null) ? 0 : toType.hashCode());
    return result;
  }

  /* see superclass */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    RelationshipIdentityJpa other = (RelationshipIdentityJpa) obj;
    if (additionalRelationshipType == null) {
      if (other.additionalRelationshipType != null)
        return false;
    } else if (!additionalRelationshipType
        .equals(other.additionalRelationshipType))
      return false;
    if (fromId == null) {
      if (other.fromId != null)
        return false;
    } else if (!fromId.equals(other.fromId))
      return false;
    if (fromTerminology == null) {
      if (other.fromTerminology != null)
        return false;
    } else if (!fromTerminology.equals(other.fromTerminology))
      return false;
    if (fromType != other.fromType)
      return false;
    if (inverseId == null) {
      if (other.inverseId != null)
        return false;
    } else if (!inverseId.equals(other.inverseId))
      return false;
    if (relationshipType == null) {
      if (other.relationshipType != null)
        return false;
    } else if (!relationshipType.equals(other.relationshipType))
      return false;
    if (terminology == null) {
      if (other.terminology != null)
        return false;
    } else if (!terminology.equals(other.terminology))
      return false;
    if (terminologyId == null) {
      if (other.terminologyId != null)
        return false;
    } else if (!terminologyId.equals(other.terminologyId))
      return false;
    if (toId == null) {
      if (other.toId != null)
        return false;
    } else if (!toId.equals(other.toId))
      return false;
    if (toTerminology == null) {
      if (other.toTerminology != null)
        return false;
    } else if (!toTerminology.equals(other.toTerminology))
      return false;
    if (toType != other.toType)
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "RelationshipIdentityJpa [id=" + id + ", terminology=" + terminology
        + ", terminologyId=" + terminologyId + ", relationshipType="
        + relationshipType + ", additionalRelationshipType="
        + additionalRelationshipType + ", fromId=" + fromId + ", fromType="
        + fromType + ", fromTerminology=" + fromTerminology + ", toId=" + toId
        + ", toType=" + toType + ", toTerminology=" + toTerminology
        + ", inverseId=" + inverseId + "]";
  }

}
