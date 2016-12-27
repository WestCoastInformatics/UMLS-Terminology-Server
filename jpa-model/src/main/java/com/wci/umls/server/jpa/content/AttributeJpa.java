/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.content;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;

import com.wci.umls.server.model.content.Attribute;

/**
 * JPA and JAXB enabled implementation of {@link Attribute}.
 */
@Entity
@Table(name = "attributes", uniqueConstraints = @UniqueConstraint(columnNames = {
    "terminologyId", "terminology", "version", "id"
}))
@Audited
@XmlRootElement(name = "attribute")
public class AttributeJpa extends AbstractComponent implements Attribute {

  /** The name. */
  @Column(nullable = false)
  private String name;

  /** The value. */
  @Column(nullable = false, length = 4000)
  private String value;

  /** The alternate terminology ids. */
  @ElementCollection(fetch = FetchType.EAGER)
  @MapKeyColumn(length = 100)
  @Column(nullable = true, length = 100)
  private Map<String, String> alternateTerminologyIds;

  /**
   * Instantiates an empty {@link AttributeJpa}.
   */
  public AttributeJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link AttributeJpa} from the specified parameters.
   *
   * @param attribute the attribute
   */
  public AttributeJpa(Attribute attribute) {
    super(attribute);
    name = attribute.getName();
    value = attribute.getValue();
    alternateTerminologyIds =
        new HashMap<>(attribute.getAlternateTerminologyIds());
  }

  /**
   * Returns the name.
   *
   * @return the name
   */
  @Override
  public String getName() {
    return name;
  }

  /**
   * Sets the name.
   *
   * @param name the name
   */
  @Override
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the value.
   *
   * @return the value
   */
  @Override
  public String getValue() {
    return value;
  }

  /**
   * Sets the value.
   *
   * @param value the value
   */
  @Override
  public void setValue(String value) {
    this.value = value;
  }

  /**
   * Returns the alternate terminology ids.
   *
   * @return the alternate terminology ids
   */
  @Override
  public Map<String, String> getAlternateTerminologyIds() {
    if (alternateTerminologyIds == null) {
      alternateTerminologyIds = new HashMap<>();
    }
    return alternateTerminologyIds;
  }

  /**
   * Sets the alternate terminology ids.
   *
   * @param alternateTerminologyIds the alternate terminology ids
   */
  @Override
  public void setAlternateTerminologyIds(
    Map<String, String> alternateTerminologyIds) {
    this.alternateTerminologyIds = alternateTerminologyIds;
  }

  /**
   * CUSTOM to support alternateTerminologyIds.
   *
   * @return the int
   * @see com.wci.umls.server.jpa.content.AbstractComponent#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((value == null) ? 0 : value.hashCode());
    return result;
  }

  /**
   * Equals.
   *
   * @param obj the obj
   * @return true, if successful
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    AttributeJpa other = (AttributeJpa) obj;
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

  /**
   * To string.
   *
   * @return the string
   */
  @Override
  public String toString() {
    return "AttributeJpa [name=" + name + ", value=" + value
        + ", alternateTerminologyIds=" + alternateTerminologyIds + "]";
  }

}
