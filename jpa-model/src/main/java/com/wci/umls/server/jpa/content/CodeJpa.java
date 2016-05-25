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
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.CodeRelationship;

/**
 * The Class CodeJpa.
 */
@Entity
@Table(name = "codes", uniqueConstraints = @UniqueConstraint(columnNames = {
    "terminologyId", "terminology", "version", "id"
}))
@Audited
@Indexed
@XmlRootElement(name = "code")
public class CodeJpa extends AbstractAtomClass implements Code {

  /** The relationships. */
  @OneToMany(mappedBy = "from", orphanRemoval = true, targetEntity = CodeRelationshipJpa.class)
  private List<CodeRelationship> relationships = new ArrayList<>(1);
  
  /** The notes. */
  @OneToMany(mappedBy = "code", targetEntity = CodeNoteJpa.class)
  @IndexedEmbedded(targetElement = CodeNoteJpa.class)
  private List<Note> notes = new ArrayList<>();

  /** The labels. */
  @ElementCollection(fetch = FetchType.EAGER)
  // consider this: @Fetch(sFetchMode.JOIN)
  @Column(nullable = true)
  List<String> labels;

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
   * @param deepCopy the deep copy
   */
  public CodeJpa(Code code, boolean deepCopy) {
    super(code, deepCopy);
    if (code.getLabels() != null) {
      labels = new ArrayList<>(code.getLabels());
    }

    if (deepCopy) {
      for (CodeRelationship relationship : code.getRelationships()) {
        getRelationships().add(new CodeRelationshipJpa(relationship, deepCopy));
      }

    }
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
  @Override
  public void setRelationships(List<CodeRelationship> relationships) {
    this.relationships = relationships;

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
  @XmlElement(type = CodeNoteJpa.class)
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
