/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.content;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;

import com.wci.umls.server.model.content.Attribute;

/**
 * JPA-enabled implementation of {@link Attribute}.
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
  @Column(nullable = true)
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
    alternateTerminologyIds = attribute.getAlternateTerminologyIds();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.Attribute#getName()
   */
  @Override
  public String getName() {
    return name;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.Attribute#setName(java.lang.String)
   */
  @Override
  public void setName(String name) {
    this.name = name;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.Attribute#getValue()
   */
  @Override
  public String getValue() {
    return value;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.Attribute#setValue(java.lang.String)
   */
  @Override
  public void setValue(String value) {
    this.value = value;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.HasAlternateTerminologyIds#
   * getAlternateTerminologyIds()
   */
  @Override
  public Map<String, String> getAlternateTerminologyIds() {
    if (alternateTerminologyIds == null) {
      alternateTerminologyIds = new HashMap<>();
    }
    return alternateTerminologyIds;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.HasAlternateTerminologyIds#
   * setAlternateTerminologyIds(java.util.Map)
   */
  @Override
  public void setAlternateTerminologyIds(
    Map<String, String> alternateTerminologyIds) {
    this.alternateTerminologyIds = alternateTerminologyIds;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.HasAlternateTerminologyIds#
   * putAlternateTerminologyId(java.lang.String, java.lang.String)
   */
  @Override
  public void putAlternateTerminologyId(String terminology, String terminologyId) {
    if (alternateTerminologyIds == null) {
      alternateTerminologyIds = new HashMap<>();
    }
    alternateTerminologyIds.put(terminology, terminologyId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.HasAlternateTerminologyIds#
   * removeAlternateTerminologyId(java.lang.String)
   */
  @Override
  public void removeAlternateTerminologyId(String terminology) {
    if (alternateTerminologyIds == null) {
      alternateTerminologyIds = new HashMap<>();
    }
    alternateTerminologyIds.remove(terminology);

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
    result =
        prime
            * result
            + ((alternateTerminologyIds == null) ? 0 : alternateTerminologyIds
                .toString().hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.content.AbstractComponent#equals(java.lang.Object)
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
    if (alternateTerminologyIds == null) {
      if (other.alternateTerminologyIds != null)
        return false;
    } else if (!alternateTerminologyIds.equals(other.alternateTerminologyIds))
      return false;
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.content.AbstractComponent#toString()
   */
  @Override
  public String toString() {
    return "AttributeJpa [name=" + name + ", value=" + value
        + ", alternateTerminologyIds=" + alternateTerminologyIds + "]";
  }

}
