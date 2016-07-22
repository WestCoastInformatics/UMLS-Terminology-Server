/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.worfklow;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.bridge.builtin.LongBridge;

import com.wci.umls.server.helpers.Note;
import com.wci.umls.server.jpa.content.AbstractNote;
import com.wci.umls.server.model.workflow.Checklist;

/**
 * JPA enabled implementation of {@link Note} connected to a {@link Checklist}.
 * NOTE: the checklist is not exposed through the API, it exists to separate notes
 * by type and avoid a table
 * 
 */
@Entity
@Table(name = "checklist_notes")
@Audited
@XmlRootElement(name = "note")
public class ChecklistNoteJpa extends AbstractNote {

  /** The Checklist. */
  @ManyToOne(targetEntity = ChecklistJpa.class, optional = false)
  private Checklist checklist;

  /**
   * The default constructor.
   */
  public ChecklistNoteJpa() {
    // n/a
  }

  /**
   * Instantiates a new note jpa.
   *
   * @param note the note
   */
  public ChecklistNoteJpa(ChecklistNoteJpa note) {
    super(note);
    checklist = note.getChecklist();
  }

  /**
   * Returns the checklist.
   *
   * @return the checklist
   */
  @XmlTransient
  public Checklist getChecklist() {
    return checklist;
  }

  /**
   * Sets the checklist.
   *
   * @param checklist the checklist
   */
  public void setChecklist(Checklist checklist) {
    this.checklist = checklist;
  }

  /**
   * Returns the checklist id.
   *
   * @return the checklist id
   */
  @XmlElement
  @FieldBridge(impl = LongBridge.class)
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public Long getChecklistId() {
    return (checklist != null) ? checklist.getId() : 0;
  }

  /**
   * Sets the checklist id.
   *
   * @param checklistId the checklist id
   */
  @SuppressWarnings("unused")
  private void setChecklistId(Long checklistId) {
    if (checklist == null) {
      checklist = new ChecklistJpa();
    }
    checklist.setId(checklistId);
  }



  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((checklist == null) ? 0 : checklist.hashCode());
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
    ChecklistNoteJpa other = (ChecklistNoteJpa) obj;
    if (checklist == null) {
      if (other.checklist != null)
        return false;
    } else if (!checklist.equals(other.checklist))
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "ChecklistNoteJpa [checklist=" + checklist + ", getLastModified()="
        + getLastModified() + ", getLastModifiedBy()=" + getLastModifiedBy()
        + ", getClass()=" + getClass()
        + ", toString()=" + super.toString() + "]";
  }

}
