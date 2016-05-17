/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.content;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;

import com.wci.umls.server.helpers.Note;
import com.wci.umls.server.model.content.Definition;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.DescriptorRelationship;

/**
 * The Class DescriptorJpa.
 */
@Entity
@Table(name = "descriptors", uniqueConstraints = @UniqueConstraint(columnNames = {
    "terminologyId", "terminology", "version", "id"
}))
@Audited
@Indexed
@XmlRootElement(name = "descriptor")
public class DescriptorJpa extends AbstractAtomClass implements Descriptor {

  /** The definitions. */
  @OneToMany(orphanRemoval = true, targetEntity = DefinitionJpa.class)
  private List<Definition> definitions = new ArrayList<>(1);

  /** The relationships. */
  @OneToMany(mappedBy = "from", orphanRemoval = true, targetEntity = DescriptorRelationshipJpa.class)
  private List<DescriptorRelationship> relationships = new ArrayList<>(1);

  /** The labels. */
  @ElementCollection(fetch = FetchType.EAGER)
  // consider this: @Fetch(FetchMode.JOIN)
  @Column(nullable = true)
  List<String> labels;

  /** The notes. */
  @OneToMany(mappedBy = "descriptor", targetEntity = DescriptorNoteJpa.class)
  @IndexedEmbedded(targetElement = DescriptorNoteJpa.class)
  private List<Note> notes = new ArrayList<>();

  /**
   * Instantiates a new descriptor jpa.
   */
  public DescriptorJpa() {
    // do nothing
  }

  /**
   * Instantiates a new descriptor jpa.
   *
   * @param descriptor the descriptor
   * @param deepCopy the deep copy
   */
  public DescriptorJpa(Descriptor descriptor, boolean deepCopy) {
    super(descriptor, deepCopy);
    if (descriptor.getLabels() != null) {
      labels = new ArrayList<>(descriptor.getLabels());
    }

    if (deepCopy) {
      for (Definition definition : descriptor.getDefinitions()) {
        addDefinition(new DefinitionJpa(definition, deepCopy));
      }
      for (DescriptorRelationship relationship : descriptor.getRelationships()) {
        addRelationship(new DescriptorRelationshipJpa(relationship, deepCopy));
      }

    }
  }

  /* see superclass */
  @XmlElement(type = DefinitionJpa.class)
  @Override
  public List<Definition> getDefinitions() {
    if (definitions == null) {
      definitions = new ArrayList<>(1);
    }
    return definitions;
  }

  /* see superclass */
  @Override
  public void setDefinitions(List<Definition> definitions) {
    this.definitions = definitions;
  }

  /* see superclass */
  @Override
  public void addDefinition(Definition definition) {
    if (definitions == null) {
      definitions = new ArrayList<>(1);
    }
    definitions.add(definition);

  }

  /* see superclass */
  @Override
  public void removeDefinition(Definition definition) {
    if (definitions == null) {
      definitions = new ArrayList<>(1);
    }
    definitions.remove(definition);

  }

  /* see superclass */
  @XmlElement(type = DescriptorRelationshipJpa.class)
  @Override
  public List<DescriptorRelationship> getRelationships() {
    if (relationships == null) {
      relationships = new ArrayList<>(1);
    }
    return relationships;
  }

  /* see superclass */
  @Override
  public void setRelationships(List<DescriptorRelationship> relationships) {
    this.relationships = relationships;

  }

  /* see superclass */
  @Override
  public void addRelationship(DescriptorRelationship relationship) {
    if (relationships == null) {
      relationships = new ArrayList<>(1);
    }
    relationships.add(relationship);
  }

  /* see superclass */
  @Override
  public void removeRelationship(DescriptorRelationship relationship) {
    if (relationships == null) {
      relationships = new ArrayList<>(1);
    }
    relationships.remove(relationship);
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
  public void setNotes(List<Note> notes) {
    this.notes = notes;

  }

  /* see superclass */
  @XmlElement(type = DescriptorNoteJpa.class)
  @Override
  public List<Note> getNotes() {
    return this.notes;
  }

  /* see superclass */
  @Override
  public void addNote(Note note) {
    if (this.notes == null) {
      this.notes = new ArrayList<>();
    }
    notes.add(note);

  }

  /* see superclass */
  @Override
  public void removeNote(Note note) {
    if (this.notes == null) {
      this.notes = new ArrayList<>();
    }
    notes.remove(note);

  }

}
