/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.meta;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.model.meta.AtomIdentity;

/**
 * JPA and JAXB enabled implementation of {@link AtomIdentity}
 */
@Entity
@Table(name = "atom_identity", uniqueConstraints = @UniqueConstraint(columnNames = {
    "terminology", "id"
}))
@XmlRootElement(name = "attributeIdentity")
public class AtomIdentityJpa implements AtomIdentity {

  /** The id. */
  @Id
  private Long id;

  /** The string class id. */
  @Column(nullable = false)
  private String stringClassId;

  /** The terminology. */
  @Column(nullable = false)
  private String terminology;

  /** The terminology id. */
  @Column(nullable = true)
  private String terminologyId;

  /** The term type. */
  @Column(nullable = false)
  private String termType;

  /** The code. */
  @Column(nullable = false)
  private String code;

  /** The concept id. */
  @Column(nullable = true)
  private String conceptId;

  /** The descriptor id. */
  @Column(nullable = true)
  private String descriptorId;

  /**
   * Instantiates an empty {@link AtomIdentityJpa}.
   */
  public AtomIdentityJpa() {
    //
  }

  /**
   * Instantiates a {@link AtomIdentityJpa} from the specified parameters.
   *
   * @param identity the identity
   */
  public AtomIdentityJpa(AtomIdentity identity) {
    id = identity.getId();
    stringClassId = identity.getStringClassId();
    terminology = identity.getTerminology();
    terminologyId = identity.getTerminologyId();
    termType = identity.getTermType();
    code = identity.getCode();
    conceptId = identity.getConceptId();
    descriptorId = identity.getDescriptorId();

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

  @Override
  public String getStringClassId() {
    return stringClassId;
  }

  @Override
  public void setStringClassId(String stringClassId) {
    this.stringClassId = stringClassId;
  }

  @Override
  public String getTermType() {
    return termType;
  }

  @Override
  public void setTermType(String termType) {
    this.termType = termType;
  }

  @Override
  public String getCode() {
    return code;
  }

  @Override
  public void setCode(String code) {
    this.code = code;

  }

  @Override
  public String getConceptId() {
    return conceptId;
  }

  @Override
  public void setConceptId(String conceptId) {
    this.conceptId = conceptId;
  }

  @Override
  public String getDescriptorId() {
    return descriptorId;
  }

  @Override
  public void setDescriptorId(String descriptorId) {
    this.descriptorId = descriptorId;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((stringClassId == null) ? 0 : stringClassId.hashCode());
    result =
        prime * result + ((terminology == null) ? 0 : terminology.hashCode());
    result = prime * result
        + ((terminologyId == null) ? 0 : terminologyId.hashCode());
    result = prime * result + ((termType == null) ? 0 : termType.hashCode());
    result = prime * result + ((code == null) ? 0 : code.hashCode());
    result = prime * result + ((conceptId == null) ? 0 : conceptId.hashCode());
    result =
        prime * result + ((descriptorId == null) ? 0 : descriptorId.hashCode());

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
    AtomIdentityJpa other = (AtomIdentityJpa) obj;
    if (stringClassId == null) {
      if (other.stringClassId != null)
        return false;
    } else if (!stringClassId.equals(other.stringClassId))
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
    if (termType == null) {
      if (other.termType != null)
        return false;
    } else if (!termType.equals(other.termType))
      return false;
    if (code == null) {
      if (other.code != null)
        return false;
    } else if (!code.equals(other.code))
      return false;
    if (conceptId == null) {
      if (other.conceptId != null)
        return false;
    } else if (!conceptId.equals(other.conceptId))
      return false;
    if (descriptorId == null) {
      if (other.descriptorId != null)
        return false;
    } else if (!descriptorId.equals(other.descriptorId))
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "AtomIdentityJpa [id=" + id + ", stringClassId=" + stringClassId
        + ", terminology=" + terminology + ", terminologyId=" + terminologyId
        + ", termType=" + termType + ", code=" + code + ", conceptId="
        + conceptId + ", descriptorId=" + descriptorId + "]";
  }

}
