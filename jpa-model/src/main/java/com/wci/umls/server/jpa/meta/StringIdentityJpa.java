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

import com.wci.umls.server.model.meta.StringIdentity;

/**
 * JPA and JAXB enabled implementation of {@link StringIdentity}.
 */

/**
 * The Class StringIdentityJpa.
 */
@Entity
@Table(name = "string_identity", uniqueConstraints = @UniqueConstraint(columnNames = {
    "stringPre", "lowerStringPre", "id"
}))
@XmlRootElement(name = "stringIdentity")
public class StringIdentityJpa implements StringIdentity {

  /** The id. */
  @Id
  private Long id;

  /** The case insensitive id. */
  @Column(nullable = false)
  private String caseInsensitiveId;

  /** The string pre. */
  @Column(nullable = false)
  private String stringPre;

  /** The string. */
  @Column(nullable = false, length = 4000)
  private String string;

  /** The lower string pre. */
  @Column(nullable = false)
  private String lowerStringPre;

  /** The lower string. */
  @Column(nullable = false, length = 4000)
  private String lowerString;

  /**
   * Instantiates an empty {@link StringIdentityJpa}.
   */
  public StringIdentityJpa() {
    //
  }

  /**
   * Instantiates a {@link StringIdentityJpa} from the specified parameters.
   *
   * @param identity the identity
   */
  public StringIdentityJpa(StringIdentity identity) {
    id = identity.getId();
    stringPre = identity.getStringPre();
    string = identity.getString();

  }

  /**
   * Returns the id.
   *
   * @return the id
   */
  /* see superclass */
  @Override
  public Long getId() {
    return id;
  }

  /**
   * Sets the id.
   *
   * @param id the id
   */
  /* see superclass */
  @Override
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Returns the string pre.
   *
   * @return the string pre
   */
  /* see superclass */
  @Override
  public String getStringPre() {
    return stringPre;
  }

  /**
   * Sets the string pre.
   *
   * @param stringPre the string pre
   */
  /* see superclass */
  @Override
  public void setStringPre(String stringPre) {
    this.stringPre = stringPre;

  }

  /**
   * Returns the string.
   *
   * @return the string
   */
  /* see superclass */
  @Override
  public String getString() {
    return string;
  }

  /**
   * Sets the string.
   *
   * @param string the string
   */
  /* see superclass */
  @Override
  public void setString(String string) {
    this.string = string;

  }

  /**
   * Hash code.
   *
   * @return the int
   */
  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((caseInsensitiveId == null) ? 0 : caseInsensitiveId.hashCode());
    result = prime * result + ((string == null) ? 0 : string.hashCode());

    return result;
  }

  /**
   * Equals.
   *
   * @param obj the obj
   * @return true, if successful
   */
  /* see superclass */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    StringIdentityJpa other = (StringIdentityJpa) obj;
    if (caseInsensitiveId == null) {
      if (other.caseInsensitiveId != null)
        return false;
    } else if (!caseInsensitiveId.equals(other.caseInsensitiveId))
      return false;
    if (stringPre == null) {
      if (other.stringPre != null)
        return false;
    } else if (!stringPre.equals(other.stringPre))
      return false;
    if (string == null) {
      if (other.string != null)
        return false;
    } else if (!string.equals(other.string))
      return false;
    if (lowerStringPre == null) {
      if (other.lowerStringPre != null)
        return false;
    } else if (!lowerStringPre.equals(other.lowerStringPre))
      return false;
    if (lowerString == null) {
      if (other.lowerString != null)
        return false;
    } else if (!lowerString.equals(other.lowerString))
      return false;
    return true;
  }

  /**
   * To string.
   *
   * @return the string
   */
  /* see superclass */
  @Override
  public String toString() {
    return "StringIdentityJpa [id=" + id + ", caseInsensitiveId="
        + caseInsensitiveId + ", stringPre=" + stringPre + ", string=" + string
        + ", lowerStringPre=" + lowerStringPre + ", lowerString=" + lowerString
        + "]";
  }
}
