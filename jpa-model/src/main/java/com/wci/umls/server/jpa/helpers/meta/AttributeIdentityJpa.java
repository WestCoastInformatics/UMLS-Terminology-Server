package com.wci.umls.server.jpa.helpers.meta;

import javax.persistence.Entity;
import javax.persistence.Table;
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
  private Long id;
  
  /** The name. */
  private String name;

  /** The terminology id. */
  private String terminologyId;

  /** The terminology. */
  private String terminology;

  /** The version. */
  private String version;

  /** The owner id. */
  private Long ownerId;

  /** The owner type. */
  private IdType ownerType;

  /** The owner qualifier. */
  private String ownerQualifier;

  /** The hash code. */
  private String hashCode;

  /**
   * Default Constructor.
   */
  public AttributeIdentityJpa() {

  }

  /**
   * Copy constructor.
   *
   * @param a the a
   */
  public AttributeIdentityJpa(AttributeIdentity a) {
    super();
    this.terminologyId = a.getTerminologyId();
    this.terminology = a.getTerminology();
    this.version = a.getVersion();
    this.ownerId = a.getOwnerId();
    this.ownerType = a.getOwnerType();
    this.ownerQualifier = a.getOwnerQualifier();
    this.hashCode = a.getHashCode();
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
  public String getVersion() {
    return version;
  }

  /* see superclass */
  @Override
  public void setVersion(String version) {
    this.version = version;
  }

  /* see superclass */
  @Override
  public Long getOwnerId() {
    return ownerId;
  }

  /* see superclass */
  @Override
  public void setOwnerId(Long ownerId) {
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

}
