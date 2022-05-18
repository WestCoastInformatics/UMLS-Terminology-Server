/*
 * Copyright 2022 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.umls.server.jpa.workflow;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;

import com.wci.umls.server.helpers.Note;
import com.wci.umls.server.model.workflow.Checklist;
import com.wci.umls.server.model.workflow.TrackingRecord;

/**
 * JPA-enabled implementation of a {@link Checklist}.
 */
@Entity
@Table(name = "checklists", uniqueConstraints = @UniqueConstraint(columnNames = {
    "name", "project_id"
}))
// @Audited
@Indexed
@XmlRootElement(name = "checklist")
public class ChecklistJpa extends AbstractChecklist {

  /** The tracking records. */
  @OneToMany(targetEntity = TrackingRecordJpa.class)
  @JoinColumn(name = "trackingRecords_id")
  @JoinTable(name = "checklists_tracking_records",
      joinColumns = @JoinColumn(name = "trackingRecords_id"),
      inverseJoinColumns = @JoinColumn(name = "checklists_id"))
  private List<TrackingRecord> trackingRecords = new ArrayList<>();

  /** The notes. */
  @OneToMany(mappedBy = "checklist", targetEntity = ChecklistNoteJpa.class)
  @IndexedEmbedded(targetElement = ChecklistNoteJpa.class)
  private List<Note> notes = new ArrayList<>();

  /**
   * Instantiates an empty {@link ChecklistJpa}.
   */
  public ChecklistJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link ChecklistJpa} from the specified parameters.
   *
   * @param checklist the checklist
   * @param collectionCopy the deep copy
   */
  public ChecklistJpa(Checklist checklist, boolean collectionCopy) {
    super(checklist, collectionCopy);
    if (collectionCopy) {
      trackingRecords = new ArrayList<>(checklist.getTrackingRecords());
      notes = new ArrayList<>(checklist.getNotes());
    }
  }

  /* see superclass */
  @XmlElement(type = ChecklistNoteJpa.class)
  @Override
  public List<Note> getNotes() {
    if (notes == null) {
      notes = new ArrayList<Note>();
    }
    return notes;
  }

  /* see superclass */
  @Override
  public void setNotes(List<Note> notes) {
    this.notes = notes;
  }

  /* see superclass */
  @XmlTransient
  @Override
  public List<TrackingRecord> getTrackingRecords() {
    if (trackingRecords == null) {
      return new ArrayList<>();
    }
    return trackingRecords;
  }

  /* see superclass */
  @Override
  public void setTrackingRecords(List<TrackingRecord> records) {
    this.trackingRecords = records;
  }

}
