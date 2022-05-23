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
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.SortableField;
import org.hibernate.search.annotations.Store;

import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.LexicalClass;

/**
 * JPA and JAXB enabled implementation of {@link LexicalClass}.
 */
@Entity
@Table(name = "lexical_classes", uniqueConstraints = @UniqueConstraint(columnNames = {
    "terminologyId", "terminology", "version", "id"
}))
// @Audited
@XmlRootElement(name = "lexicalClass")
@Indexed
public class LexicalClassJpa extends AbstractAtomClass implements LexicalClass {

  /** The normalized string. */
  @Column(nullable = true, length = 4000)
  private String normalizedName;

  /** The language. */
  @Column(nullable = false)
  String language;

  /** The label sets. */
  @ElementCollection(fetch = FetchType.EAGER)
  @Column(nullable = true)
  List<String> labels;

  /** The descriptions. */
  @ManyToMany(targetEntity = AtomJpa.class)
  @JoinColumn(name = "atoms_id")
  @JoinTable(name = "lexical_classes_atoms", joinColumns = @JoinColumn(name = "atoms_id"),
      inverseJoinColumns = @JoinColumn(name = "lexical_classes_id"))
  // @CollectionTable(name = "lexical_classes_atoms", joinColumns =
  // @JoinColumn(name = "atoms_id"))
  // @IndexedEmbedded(targetElement = AtomJpa.class)
  private List<Atom> atoms = null;

  /** The attributes. */
  @OneToMany(targetEntity = AttributeJpa.class)
  @JoinColumn(name = "attributes_id")
  @JoinTable(name = "lexical_classes_attributes", joinColumns = @JoinColumn(name = "attributes_id"),
      inverseJoinColumns = @JoinColumn(name = "lexical_classes_id"))
  private List<Attribute> attributes = null;

  /**
   * Instantiates an empty {@link LexicalClassJpa}.
   */
  public LexicalClassJpa() {
    setPublishable(true);
  }

  /**
   * Instantiates a {@link LexicalClassJpa} from the specified parameters.
   *
   * @param lexicalClass the lexical class
   * @param collectionCopy the deep copy
   */
  public LexicalClassJpa(LexicalClass lexicalClass, boolean collectionCopy) {
    super(lexicalClass, collectionCopy);
    language = lexicalClass.getLanguage();
    normalizedName = lexicalClass.getNormalizedName();
    if (lexicalClass.getLabels() != null) {
      labels = new ArrayList<>(lexicalClass.getLabels());
    }
    if (collectionCopy) {
      atoms = new ArrayList<>(lexicalClass.getAtoms());
      for (final Attribute attribute : lexicalClass.getAttributes()) {
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

  /**
   * Returns the normalized string.
   *
   * @return the normalized string
   */
  @Override
  @Fields({
      @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO),
      @Field(name = "normalizedNameSort", index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  })
  @SortableField(forField = "normalizedNameSort")
  @Analyzer(definition = "noStopWord")
  public String getNormalizedName() {
    return normalizedName;
  }

  /**
   * Sets the normalized string.
   *
   * @param normalizedName the normalized string
   */
  @Override
  public void setNormalizedName(String normalizedName) {
    this.normalizedName = normalizedName;
  }

  /* see superclass */
  @Override
  public List<String> getLabels() {
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
    result = prime * result + ((normalizedName == null) ? 0 : normalizedName.hashCode());
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
    LexicalClassJpa other = (LexicalClassJpa) obj;
    if (language == null) {
      if (other.language != null)
        return false;
    } else if (!language.equals(other.language))
      return false;
    if (normalizedName == null) {
      if (other.normalizedName != null)
        return false;
    } else if (!normalizedName.equals(other.normalizedName))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "LexicalClassJpa [normalizedName=" + normalizedName + ", language=" + language
        + ", labels=" + labels + "]";
  }

}
