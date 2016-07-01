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
import com.wci.umls.server.model.meta.LexicalClassIdentity;

/**
 * JPA and JAXB enabled implementation of {@link LexicalClassIdentity}.
 */
@Entity
@Table(name = "lexicalClass_identity", uniqueConstraints = @UniqueConstraint(columnNames = {
    "normalizedStringHash", "id"
}))
@XmlRootElement(name = "lexicalClassIdentity")
public class LexicalClassIdentityJpa implements LexicalClassIdentity {

  /** The id. */
  @Id
  private Long id;

  /** The normalized string. */
  @Column(nullable = false, length = 4000)
  private String normalizedString;

  /** The normalized string pre. */
  @Column(nullable = false)
  private String normalizedStringHash;

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
    setNormalizedString(identity.getNormalizedString());
  }
  

  /**
   * Instantiates a {@link LexicalClassIdentityJpa} from the specified parameters.
   *
   * @param normalizedString the normalized string
   */
  public LexicalClassIdentityJpa(String normalizedString) {
    setNormalizedString(normalizedString);
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
  public String getNormalizedString() {
    return normalizedString;
  }

  /* see superclass */
  @Override
  public void setNormalizedString(String normalizedString) {
    this.normalizedString = normalizedString;
    this.normalizedStringHash = ConfigUtility.getMd5(normalizedString);

  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((normalizedString == null) ? 0 : normalizedString.hashCode());

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
    LexicalClassIdentityJpa other = (LexicalClassIdentityJpa) obj;
    if (normalizedString == null) {
      if (other.normalizedString != null)
        return false;
    } else if (!normalizedString.equals(other.normalizedString))
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "LexicalClassIdentityJpa [id=" + id + ", normalizedString="
        + normalizedString + ", normalizedStringHash=" + normalizedStringHash
        + "]";
  }

}
