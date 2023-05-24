/*
 *    Copyright 2017 West Coast Informatics, LLC
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
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;

import com.wci.umls.server.helpers.Note;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.CodeRelationship;
import com.wci.umls.server.model.content.CodeTreePosition;
import com.wci.umls.server.model.meta.IdType;

/**
 * JPA and JAXB enabled implementation of a {@link Code}.
 */
@Entity
@Table(name = "codes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {
        "terminologyId", "terminology", "version", "id"
    }), @UniqueConstraint(columnNames = {
        "terminology", "version", "id"
    })
})
//@Audited
@Indexed
@XmlRootElement(name = "code")
public class CodeJpa extends AbstractAtomClass implements Code {

  /** The relationships. */
  @OneToMany(mappedBy = "from", orphanRemoval = true, targetEntity = CodeRelationshipJpa.class)
  private List<CodeRelationship> relationships = new ArrayList<>(1);

  /** The treee positions. */
  @OneToMany(mappedBy = "node", orphanRemoval = true, targetEntity = CodeTreePositionJpa.class)
  private List<CodeTreePosition> treePositions = new ArrayList<>(1);

  /** The inverse relationships. */
  @OneToMany(mappedBy = "to", orphanRemoval = true, targetEntity = CodeRelationshipJpa.class)
  private List<CodeRelationship> inverseRelationships = new ArrayList<>(1);

  /** The notes. */
  @OneToMany(mappedBy = "code", targetEntity = CodeNoteJpa.class)
  @IndexedEmbedded(targetElement = CodeNoteJpa.class)
  private List<Note> notes = new ArrayList<>();

  /** The labels. */
  @ElementCollection(fetch = FetchType.EAGER)
  @Fetch(FetchMode.JOIN)
  @Column(nullable = true)
  List<String> labels;

  /** The descriptions. */
  @ManyToMany(targetEntity = AtomJpa.class)
  @CollectionTable(name = "codes_atoms", joinColumns = @JoinColumn(name = "codes_id"))
  @IndexedEmbedded(targetElement = AtomJpa.class)
  private List<Atom> atoms = null;

  /** The attributes. */
  @OneToMany(targetEntity = AttributeJpa.class)
  @JoinColumn(name = "attributes_id")
  @JoinTable(name = "codes_attributes", inverseJoinColumns = @JoinColumn(name = "attributes_id"),
      joinColumns = @JoinColumn(name = "codes_id"))
  private List<Attribute> attributes = null;

  /**
   * Instantiates a new code jpa.
   */
  public CodeJpa() {
    // do nothing
  }

  /**
   * Instantiates a new code jpa.
   *
   * @param code the code
   * @param collectionCopy the deep copy
   */
  public CodeJpa(Code code, boolean collectionCopy) {
    super(code, collectionCopy);
    labels = new ArrayList<>(code.getLabels());

    if (collectionCopy) {
      relationships = new ArrayList<>(code.getRelationships());
      treePositions = new ArrayList<>(code.getTreePositions());
      atoms = new ArrayList<>(code.getAtoms());
      for (final Attribute attribute : code.getAttributes()) {
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
  @XmlElement(type = CodeRelationshipJpa.class)
  @Override
  public List<CodeRelationship> getRelationships() {
    if (relationships == null) {
      relationships = new ArrayList<>(1);
    }
    return relationships;
  }

  /* see superclass */
  @XmlTransient
  @Override
  public List<CodeRelationship> getInverseRelationships() {
    if (inverseRelationships == null) {
      inverseRelationships = new ArrayList<>(1);
    }
    return inverseRelationships;
  }

  /* see superclass */
  @Override
  public void setRelationships(List<CodeRelationship> relationships) {
    this.relationships = relationships;

  }

  /* see superclass */
  @XmlTransient
  @Override
  public List<CodeTreePosition> getTreePositions() {
    if (treePositions == null) {
      treePositions = new ArrayList<>(1);
    }
    return treePositions;
  }

  /* see superclass */
  @Override
  public void setTreePositions(List<CodeTreePosition> treePositions) {
    this.treePositions = treePositions;

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
  public void setNotes(List<Note> notes) {
    this.notes = notes;

  }

  /* see superclass */
  @XmlElement(type = CodeNoteJpa.class)
  @Override
  public List<Note> getNotes() {
    if (this.notes == null) {
      this.notes = new ArrayList<>(1);
    }
    return this.notes;
  }

  @Override
  public void setType(IdType type) {
    // N/A
  }

  @Override
  public IdType getType() {
    return IdType.CODE;
  }

}
