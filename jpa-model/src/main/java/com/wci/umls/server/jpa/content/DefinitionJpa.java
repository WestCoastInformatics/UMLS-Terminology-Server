/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.content;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;

import com.wci.umls.server.model.content.Definition;

/**
 * JPA-enabled implementation of {@link Definition}.
 */
@Entity
@Table(name = "definitions", uniqueConstraints = @UniqueConstraint(columnNames = {
    "terminologyId", "terminology", "terminologyVersion", "id"
}))
@Audited
@XmlRootElement(name = "definition")
public class DefinitionJpa extends AbstractComponentHasAttributes implements Definition {

  /** The value. */
  @Column(nullable = false, length = 4000)
  private String value;

  /**
   * Instantiates an empty {@link DefinitionJpa}.
   */
  public DefinitionJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link DefinitionJpa} from the specified parameters.
   *
   * @param definition the definition
   * @param deepCopy the deep copy
   */
  public DefinitionJpa(Definition definition, boolean deepCopy) {
    super(definition, deepCopy);
    value = definition.getValue();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.Definition#getValue()
   */
  @Override
  public String getValue() {
    return value;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.Definition#setValue(java.lang.String)
   */
  @Override
  public void setValue(String value) {
    this.value = value;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.content.AbstractComponent#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((value == null) ? 0 : value.hashCode());
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
    DefinitionJpa other = (DefinitionJpa) obj;
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
   * @see com.wci.umls.server.jpa.content.AbstractComponent#toString()
   */
  @Override
  public String toString() {
    return "DefinitionJpa [value=" + value + "]";
  }

}
