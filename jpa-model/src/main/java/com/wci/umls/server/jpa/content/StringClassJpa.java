/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.content;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;

import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.StringClass;

/**
 * JPA and JAXB enabled implementation of {@link StringClass}.
 */
@Entity
@Table(name = "string_classes", uniqueConstraints = @UniqueConstraint(columnNames = {
    "terminologyId", "terminology", "version", "id"
}))
@Audited
@XmlRootElement(name = "stringClass")
@Indexed
public class StringClassJpa extends AbstractAtomClass implements StringClass {

  /** The labels. */
  @ElementCollection(fetch = FetchType.EAGER)
  @Column(nullable = true)
  List<String> labels;

  /** The language. */
  @Column(nullable = false)
  String language;

  /** The descriptions. */
  @ManyToMany(targetEntity = AtomJpa.class)
  @CollectionTable(name = "concepts_atoms", joinColumns = @JoinColumn(name = "concepts_id"))
  @IndexedEmbedded(targetElement = AtomJpa.class)
  private List<Atom> atoms = null;

  /** The attributes. */
  @OneToMany(targetEntity = AttributeJpa.class)
  @JoinColumn(name = "attributes_id")
  @JoinTable(name = "string_classes_attributes", joinColumns = @JoinColumn(name = "attributes_id"),
      inverseJoinColumns = @JoinColumn(name = "string_classes_id"))
  private List<Attribute> attributes = null;

  /**
   * Instantiates a new string class jpa.
   */
  public StringClassJpa() {
    setPublishable(true);
  }

  /**
   * Instantiates a new string class jpa.
   *
   * @param stringClass the string class
   * @param collectionCopy the deep copy
   */
  public StringClassJpa(StringClass stringClass, boolean collectionCopy) {
    super(stringClass, collectionCopy);
    labels = new ArrayList<>(stringClass.getLabels());
    language = stringClass.getLanguage();
    if (collectionCopy) {
      atoms = new ArrayList<>(stringClass.getAtoms());
      for (final Attribute attribute : stringClass.getAttributes()) {
        getAttributes().add(new AttributeJpa(attribute));
      }
    }
  }

  /* see superclass */
  @XmlElement(type = AtomJpa.class)
  @Override
  public List<Atom> getAtoms() {
    if (atoms == null) {
      atoms = new ArrayList<>();
    }
    return atoms;
  }

  /* see superclass */
  @Override
  public void setAtoms(List<Atom> atoms) {
    this.atoms = atoms;
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
  public List<String> getLabels() {
    if (labels == null) {
      labels = new ArrayList<>();
    }
    return labels;
  }

  /* see superclass */
  @Override
  public void setLabels(List<String> labels) {
    this.labels = labels;

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
    int result = super.hashCode();
    result = prime * result + ((language == null) ? 0 : language.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    StringClassJpa other = (StringClassJpa) obj;
    if (language == null) {
      if (other.language != null)
        return false;
    } else if (!language.equals(other.language))
      return false;
    return true;
  }

}
