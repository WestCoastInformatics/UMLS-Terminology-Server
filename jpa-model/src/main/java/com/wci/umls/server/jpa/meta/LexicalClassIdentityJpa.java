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
@Table(name = "lexical_class_identity", uniqueConstraints = @UniqueConstraint(columnNames = {
    "normalizedNameHash", "id"
}))
@XmlRootElement(name = "lexicalClassIdentity")
public class LexicalClassIdentityJpa implements LexicalClassIdentity {

  /** The id. */
  @Id
  private Long id;

  /** The normalized name. */
  @Column(nullable = false, length = 4000)
  private String normalizedName;

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
    setNormalizedName(identity.getNormalizedName());
  }
  

  /**
   * Instantiates a {@link LexicalClassIdentityJpa} from the specified parameters.
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
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((normalizedName == null) ? 0 : normalizedName.hashCode());

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
    if (normalizedName == null) {
      if (other.normalizedName != null)
        return false;
    } else if (!normalizedName.equals(other.normalizedName))
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "LexicalClassIdentityJpa [id=" + id + ", normalizedName="
        + normalizedName + ", normalizedNameHash=" + normalizedNameHash
        + "]";
  }

}
