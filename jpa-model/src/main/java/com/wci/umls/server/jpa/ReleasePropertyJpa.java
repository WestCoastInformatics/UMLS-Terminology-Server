/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;

import com.wci.umls.server.ReleaseProperty;

/**
 * JPA enabled implementation of a {@link ReleaseProperty}.
 */
@Entity
@Table(name = "release_properties")
@Audited
@XmlRootElement(name = "property")
public class ReleasePropertyJpa implements ReleaseProperty {

  /** The id. */
  @Id
  @GeneratedValue
  private Long id;

  /** The name. */
  @Column(nullable = false, length = 255)
  private String name;

  /** The value. */
  @Column(nullable = false, length = 4000)
  private String value;

  /**
   * Instantiates an empty {@link ReleasePropertyJpa}.
   */
  public ReleasePropertyJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link ReleasePropertyJpa} from the specified parameters.
   *
   * @param property the property
   */
  public ReleasePropertyJpa(ReleaseProperty property) {
    id = property.getId();
    name = property.getName();
    value = property.getValue();

  }

  /**
   * ID for XML serialization.
   *
   * @return the object id
   */
  @XmlID
  public String getObjectId() {
    return (id == null ? "" : id.toString());
  }

  @Override
  public Long getId() {
    return id;
  }

  /*
   * (non-Javadoc)
   * 
   */
  @Override
  public void setId(Long id) {
    this.id = id;
  }

  /*
   * (non-Javadoc)
   * 
   */
  @Override
  public String getName() {
    return name;
  }

  /*
   * (non-Javadoc)
   * 
   */
  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getValue() {
    return value;
  }

  @Override
  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((value == null) ? 0 : value.hashCode());
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
    ReleasePropertyJpa other = (ReleasePropertyJpa) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!value.equals(other.value))
      return false;
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return name + ", " + value ;
  }

}
