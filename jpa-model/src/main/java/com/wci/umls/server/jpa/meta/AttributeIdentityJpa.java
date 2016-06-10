package com.wci.umls.server.jpa.meta;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.model.meta.AttributeIdentity;
import com.wci.umls.server.model.meta.IdType;

/**
 * The Attribute Identity Object Class
 */
@Entity
@Table(name = "attribute_identities")
@XmlRootElement(name = "attributeIdentity")
public class AttributeIdentityJpa implements AttributeIdentity {

  /** The id. */
  @TableGenerator(name = "EntityIdGen", table = "table_generator", pkColumnValue = "Entity")
  @Id
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "EntityIdGen")
  private Long id;

  /** The attribute name. */
  @Column(nullable = false)
  private String name;

  /** The terminology id. */
  @Column(nullable = true)
  private String terminologyId;

  /** The terminology. */
  @Column(nullable = false)
  private String terminology;

  /** The owner id. */
  @Column(nullable = false)
  private String ownerId;

  /** The owner type. */
  @Column(nullable = false)
  private IdType ownerType;

  /** The owner qualifier. */
  @Column(nullable = true)
  private String ownerQualifier;

  /** The attribute value hash code. */
  @Column(nullable = false)
  private String hashCode;

  /**
   * Default Constructor.
   */
  public AttributeIdentityJpa() {

  }

  /**
   * Copy constructor.
   *
   * @param identity the a
   */
  public AttributeIdentityJpa(AttributeIdentity identity) {
    super();
    this.name = identity.getName();
    this.terminologyId = identity.getTerminologyId();
    this.terminology = identity.getTerminology();
    this.ownerId = identity.getOwnerId();
    this.ownerType = identity.getOwnerType();
    this.ownerQualifier = identity.getOwnerQualifier();
    this.hashCode = identity.getHashCode();
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
  public String getOwnerId() {
    return ownerId;
  }

  /* see superclass */
  @Override
  public void setOwnerId(String ownerId) {
    this.ownerId = ownerId;
  }

  /* see superclass */
  @Override
  public IdType getOwnerType() {
    return ownerType;
  }

  /* see superclass */
  @Override
  public void setOwnerType(IdType ownerType) {
    this.ownerType = ownerType;
  }

  /* see superclass */
  @Override
  public String getOwnerQualifier() {
    return ownerQualifier;
  }

  /* see superclass */
  @Override
  public void setOwnerQualifier(String ownerQualifier) {
    this.ownerQualifier = ownerQualifier;
  }

  /* see superclass */
  @Override
  public String getHashCode() {
    return hashCode;
  }

  /* see superclass */
  @Override
  public void setHashCode(String hashCode) {
    this.hashCode = hashCode;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((hashCode == null) ? 0 : hashCode.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((ownerId == null) ? 0 : ownerId.hashCode());
    result =
        prime * result
            + ((ownerQualifier == null) ? 0 : ownerQualifier.hashCode());
    result = prime * result + ((ownerType == null) ? 0 : ownerType.hashCode());
    result =
        prime * result + ((terminology == null) ? 0 : terminology.hashCode());
    result =
        prime * result
            + ((terminologyId == null) ? 0 : terminologyId.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    AttributeIdentityJpa other = (AttributeIdentityJpa) obj;
    if (hashCode == null) {
      if (other.hashCode != null)
        return false;
    } else if (!hashCode.equals(other.hashCode))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (ownerId == null) {
      if (other.ownerId != null)
        return false;
    } else if (!ownerId.equals(other.ownerId))
      return false;
    if (ownerQualifier == null) {
      if (other.ownerQualifier != null)
        return false;
    } else if (!ownerQualifier.equals(other.ownerQualifier))
      return false;
    if (ownerType != other.ownerType)
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
    return true;
  }

  @Override
  public String toString() {
    return "AttributeIdentityJpa [id=" + id + ", name=" + name
        + ", terminologyId=" + terminologyId + ", terminology=" + terminology
        + ", ownerId=" + ownerId + ", ownerType=" + ownerType
        + ", ownerQualifier=" + ownerQualifier + ", hashCode=" + hashCode + "]";
  }

}
