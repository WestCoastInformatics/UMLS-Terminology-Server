/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.meta;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.model.meta.LexicalClassIdentity;

/**
 * JPA and JAXB enabled implementation of {@link LexicalClassIdentity}.
 */
@Entity
@Table(name = "lexical_class_identity", uniqueConstraints = @UniqueConstraint(columnNames = {
    "normalizedNameHash", "language", "id"
}))
@XmlRootElement(name = "lexicalClassIdentity")
public class LexicalClassIdentityJpa implements LexicalClassIdentity {

  /** The id. */
  @Id
  private Long id;

  /** The normalized name. */
  @Column(nullable = false, length = 4000)
  private String normalizedName;

  /** The language */
  @Column(nullable = false)
  private String language;

  /** The normalized name hash. */
  @Column(nullable = false)
  private String normalizedNameHash;

  /**
   * Instantiates an empty {@link LexicalClassIdentityJpa}.
   */
  public LexicalClassIdentityJpa() {
    //
  }

  /**
   * Instantiates a {@link LexicalClassIdentityJpa} from the specified
   * parameters.
   *
   * @param identity the identity
   */
  public LexicalClassIdentityJpa(LexicalClassIdentity identity) {
    id = identity.getId();
    language = identity.getLanguage();
    setNormalizedName(identity.getNormalizedName());
  }

  /**
   * Instantiates a {@link LexicalClassIdentityJpa} from the specified
   * parameters.
   *
   * @param normalizedName the normalized string
   */
  public LexicalClassIdentityJpa(String normalizedName) {
    setNormalizedName(normalizedName);
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
  public String getNormalizedName() {
    return normalizedName;
  }

  /* see superclass */
  @Override
  public void setNormalizedName(String normalizedName) {
    this.normalizedName = normalizedName;
    this.normalizedNameHash = ConfigUtility.getMd5(normalizedName);

  }

  /* see superclass */
  @Override
  public String getLanguage() {
    return language;
  }

  /* see superclass */
  @Override
  public void setLanguage(String language) {
    this.language = language;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((language == null) ? 0 : language.hashCode());
    result = prime * result
        + ((normalizedName == null) ? 0 : normalizedName.hashCode());
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
    LexicalClassIdentityJpa other = (LexicalClassIdentityJpa) obj;
    if (language == null) {
      if (other.language != null)
        return false;
    } else if (!language.equals(other.language))
      return false;
    if (normalizedName == null) {
      if (other.normalizedName != null)
        return false;
    } else if (!normalizedName.equals(other.normalizedName))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "LexicalClassIdentityJpa [id=" + id + ", normalizedName="
        + normalizedName + ", language=" + language + ", normalizedNameHash="
        + normalizedNameHash + "]";
  }

}
