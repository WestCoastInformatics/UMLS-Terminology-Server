/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Store;

import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.Definition;

/**
 * JPA and JAXB enabled implementation of {@link Definition}.
 */
@Entity
@Table(name = "definitions", uniqueConstraints = @UniqueConstraint(columnNames = {
    "terminologyId", "terminology", "version", "id"
}))
@Audited
@XmlRootElement(name = "definition")
public class DefinitionJpa extends AbstractComponentHasAttributes
    implements Definition {

  /** The value. */
  @Column(nullable = false, length = 4000)
  private String value;

  /** The alternate terminology ids. */
  @ElementCollection(fetch = FetchType.EAGER)
  @Fetch(FetchMode.JOIN)
  @Column(nullable = true)
  private Map<String, String> alternateTerminologyIds;

  /** The attributes. */
  @OneToMany(targetEntity = AttributeJpa.class)
  @JoinColumn(name = "attributes_id")
  @JoinTable(name = "definitions_attributes",
      joinColumns = @JoinColumn(name = "attributes_id"),
      inverseJoinColumns = @JoinColumn(name = "definitions_id"))
  private List<Attribute> attributes = null;
  
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
   * @param collectionCopy the deep copy
   */
  public DefinitionJpa(Definition definition, boolean collectionCopy) {
    //super(definition, collectionCopy);
    value = definition.getValue();
    alternateTerminologyIds =
        new HashMap<>(definition.getAlternateTerminologyIds());
    
    if (collectionCopy) {
        for (final Attribute attribute : definition.getAttributes()) {
            getAttributes().add(new AttributeJpa(attribute));
        }
      }
  }

  /* see superclass */
@Override
@XmlElement(type = AttributeJpa.class)
public List<Attribute> getAttributes() {
  if (attributes == null) {
    attributes = new ArrayList<>(1);
  }
  return attributes;
}

/* see superclass */
@Override
public void setAttributes(List<Attribute> attributes) {
  this.attributes = attributes;
}

/* see superclass */
@Override
public Attribute getAttributeByName(String name) {
  for (final Attribute attribute : getAttributes()) {
    // If there are more than one, this just returns the first.
    if (attribute.getName().equals(name)) {
      return attribute;
    }
  }
  return null;
}

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
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
  public Map<String, String> getAlternateTerminologyIds() {
    if (alternateTerminologyIds == null) {
      alternateTerminologyIds = new HashMap<>();
    }
    return alternateTerminologyIds;
  }

  /* see superclass */
  @Override
  public void setAlternateTerminologyIds(
    Map<String, String> alternateTerminologyIds) {
    this.alternateTerminologyIds = alternateTerminologyIds;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((value == null) ? 0 : value.hashCode());
    return result;
  }

  /* see superclass */
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

  /* see superclass */
  @Override
  public String toString() {
    return "DefinitionJpa [value=" + value + ", alternateTerminologyIds="
        + alternateTerminologyIds + "]";
  }

}
