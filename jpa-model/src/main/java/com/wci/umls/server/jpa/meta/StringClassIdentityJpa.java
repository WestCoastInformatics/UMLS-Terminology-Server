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

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.bridge.builtin.LongBridge;

import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.model.meta.StringClassIdentity;

/**
 * JPA and JAXB enabled implementation of {@link StringClassIdentity}.
 */

@Entity
@Table(name = "string_class_identity", uniqueConstraints = @UniqueConstraint(columnNames = {
    "nameHash", "language", "id"
}))
@XmlRootElement(name = "stringIdentity")
@Indexed
public class StringClassIdentityJpa implements StringClassIdentity {

  /** The id. */
  @Id
  private Long id;

  /** The name. */
  @Column(nullable = false, length = 4000)
  private String name;

  /** The name pre. */
  @Column(nullable = false)
  private String nameHash;

  /** The language. */
  @Column(nullable = false)
  private String language;

  /**
   * Instantiates an empty {@link StringClassIdentityJpa}.
   */
  public StringClassIdentityJpa() {
    //
  }

  /**
   * Instantiates a {@link StringClassIdentityJpa} from the specified
   * parameters.
   *
   * @param identity the identity
   */
  public StringClassIdentityJpa(StringClassIdentity identity) {
    id = identity.getId();
    setName(identity.getName());
    language = identity.getLanguage();

  }

  /**
   * Instantiates a {@link StringClassIdentityJpa} from the specified
   * parameters.
   *
   * @param name the name
   * @param language the language
   */
  public StringClassIdentityJpa(String name, String language) {
    setName(name);
    this.language = language;

  }

  /**
   * Returns the id.
   *
   * @return the id
   */
  /* see superclass */
  @Override
  @FieldBridge(impl = LongBridge.class)
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.YES)
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
   * Returns the name.
   *
   * @return the name
   */
  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getName() {
    return name;
  }

  /**
   * Sets the name.
   *
   * @param name the name
   */
  /* see superclass */
  @Override
  public void setName(String name) {
    this.name = name;
    this.nameHash = ConfigUtility.getMd5(name);

  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getLanguage() {
    return language;
  }

  /* see superclass */
  @Override
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
    result = prime * result + ((name == null) ? 0 : name.hashCode());
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
    StringClassIdentityJpa other = (StringClassIdentityJpa) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (language == null) {
      if (other.language != null)
        return false;
    } else if (!language.equals(other.language))
      return false;
    return true;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getIdentityCode() {
    return name + language;
  }

  /* see superclass */
  @Override
  public void setIdentityCode(String identityCode) {
    // n/a
  }

  /**
   * To name.
   *
   * @return the name
   */
  /* see superclass */
  @Override
  public String toString() {
    return "StringIdentityJpa [id=" + id + ", name=" + name + ", nameHash="
        + nameHash + "]";
  }
}
