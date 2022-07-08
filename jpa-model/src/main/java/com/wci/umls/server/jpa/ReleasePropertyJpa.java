/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;

import com.wci.umls.server.ReleaseProperty;

/**
 * JPA and JAXB enabled implementation of a {@link ReleaseProperty}.
 */
@Entity
@Table(name = "release_properties")
//@Audited
@XmlRootElement(name = "property")
public class ReleasePropertyJpa implements ReleaseProperty {

  /** The id. */
  @TableGenerator(name = "EntityIdGen", table = "table_generator", pkColumnValue = "Entity")
  @Id
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "EntityIdGen")
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
  public String getName() {
    return name;
  }

  /* see superclass */
  @Override
  public void setName(String name) {
    this.name = name;
  }

  /* see superclass */
  @Override
  public String getValue() {
    return value;
  }

  /* see superclass */
  @Override
  public void setValue(String value) {
    this.value = value;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((value == null) ? 0 : value.hashCode());
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

  /* see superclass */
  @Override
  public String toString() {
    return name + ", " + value;
  }

}
