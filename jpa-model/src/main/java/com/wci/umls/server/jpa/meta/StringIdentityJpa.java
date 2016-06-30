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

import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.model.meta.StringIdentity;

/**
 * JPA and JAXB enabled implementation of {@link StringIdentity}.
 */

/**
 * The Class StringIdentityJpa.
 */
@Entity
@Table(name = "string_identity", uniqueConstraints = @UniqueConstraint(columnNames = {
    "stringPre", "id"
}))
@XmlRootElement(name = "stringIdentity")
public class StringIdentityJpa implements StringIdentity {

  /** The id. */
  @Id
  private Long id;

  /** The string pre. */
  @Column(nullable = false)
  private String stringHash;

  /** The string. */
  @Column(nullable = false, length = 4000)
  private String string;

  /** The language. */
  @Column(nullable = false)
  private String language;

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
    setString(identity.getString());
    language = identity.getLanguage();

  }

  /**
   * Instantiates a {@link StringIdentityJpa} from the specified parameters.
   *
   * @param string the string
   * @param language the language
   */
  public StringIdentityJpa(String string, String language) {
    setString(string);
    this.language = language;

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
    this.stringHash = ConfigUtility.getMd5(string);

  }

  /* see superclass */
  public String getLanguage() {
    return language;
  }

  /* see superclass */
  public void setLanguage(String language) {
    this.language = language;
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
    result = prime * result + ((string == null) ? 0 : string.hashCode());
    result = prime * result + ((language == null) ? 0 : language.hashCode());
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
    if (string == null) {
      if (other.string != null)
        return false;
    } else if (!string.equals(other.string))
      return false;
    if (language == null) {
      if (other.language != null)
        return false;
    } else if (!language.equals(other.language))
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
    return "StringIdentityJpa [id=" + id + ", string=" + string
        + ", stringHash=" + stringHash + "]";
  }
}
