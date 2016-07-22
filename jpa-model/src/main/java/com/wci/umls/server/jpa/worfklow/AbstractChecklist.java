/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.worfklow;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.bridge.builtin.LongBridge;

import com.wci.umls.server.Project;
import com.wci.umls.server.helpers.Note;
import com.wci.umls.server.jpa.ProjectJpa;
import com.wci.umls.server.model.workflow.Checklist;
import com.wci.umls.server.model.workflow.TrackingRecord;
import com.wci.umls.server.model.workflow.Worklist;

/**
 * Abstract JPA-enabled implementation of a {@link Checklist} or a
 * {@link Worklist}.
 */
@MappedSuperclass
@XmlSeeAlso({
    ChecklistJpa.class, WorklistJpa.class
})
public abstract class AbstractChecklist implements Checklist {

  /** The id. */
  @TableGenerator(name = "EntityIdGenWorkflow", table = "table_generator_wf", pkColumnValue = "Entity")
  @Id
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "EntityIdGenWorkflow")
  private Long id;

  /** The last modified. */
  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date lastModified = new Date();

  /** The last modified. */
  @Column(nullable = false)
  private String lastModifiedBy;

  /** the timestamp. */
  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date timestamp = null;

  /** The name. */
  @Column(nullable = false, length = 255)
  private String name;

  /** The description. */
  @Column(nullable = false, length = 4000)
  private String description;

  /** The project. */
  @ManyToOne(targetEntity = ProjectJpa.class, optional = false)
  private Project project;

  /** The tracking records. */
  @OneToMany(targetEntity = TrackingRecordJpa.class)
  private List<TrackingRecord> trackingRecords = new ArrayList<>();

  /** The notes. */
  @OneToMany(mappedBy = "checklist", targetEntity = ChecklistNoteJpa.class)
  @IndexedEmbedded(targetElement = ChecklistNoteJpa.class)
  private List<Note> notes = new ArrayList<>();
  
  /**
   * The stats - intended only for JAXB serialization and reporting, not
   * persisted.
   */
  @Transient
  private Map<String, Integer> stats = new HashMap<>();

  /**
   * Instantiates an empty {@link AbstractChecklist}.
   */
  public AbstractChecklist() {
    // do nothing
  }

  /**
   * Instantiates a {@link AbstractChecklist} from the specified parameters.
   *
   * @param checklist the checklist
   * @param deepCopy the deep copy
   */
  public AbstractChecklist(Checklist checklist, boolean deepCopy) {
    id = checklist.getId();
    lastModified = checklist.getLastModified();
    lastModifiedBy = checklist.getLastModifiedBy();
    timestamp = checklist.getTimestamp();
    name = checklist.getName();
    description = checklist.getDescription();
    project = checklist.getProject();

    for (Note note : checklist.getNotes()) {
      getNotes().add(new ChecklistNoteJpa((ChecklistNoteJpa) note));
    }
    if (deepCopy) {
      trackingRecords = new ArrayList<>(checklist.getTrackingRecords());
    }
  }

  /* see superclass */
  @Override
  public Date getTimestamp() {
    return timestamp;
  }

  /* see superclass */
  @Override
  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public Date getLastModified() {
    return lastModified;
  }

  /* see superclass */
  @Override
  public void setLastModified(Date lastModified) {
    this.lastModified = lastModified;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getLastModifiedBy() {
    return lastModifiedBy;
  }

  /* see superclass */
  @Override
  public void setLastModifiedBy(String lastModifiedBy) {
    this.lastModifiedBy = lastModifiedBy;
  }

  /* see superclass */
  @Override
  public Long getId() {
    return id;
  }

  /* see superclass */
  @Override
  public void setId(Long id) {
    this.id = id;
  }

  /* see superclass */
  @Override
  @Fields({
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO),
    @Field(name = "nameSort", index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  })
  public String getName() {
    return name;
  }

  /* see superclass */
  @Override
  public void setName(String name) {
    this.name = name;
  }

  /* see superclass */
  @Override
  public String getDescription() {
    return description;
  }

  /* see superclass */
  @Override
  public void setDescription(String description) {
    this.description = description;
  }

  /* see superclass */
  @Override
  @XmlTransient
  public Project getProject() {
    return project;
  }

  /* see superclass */
  @Override
  public void setProject(Project project) {
    this.project = project;
  }

  /**
   * Returns the project id.
   *
   * @return the project id
   */
  @XmlElement
  @FieldBridge(impl = LongBridge.class)
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public Long getProjectId() {
    return project == null ? null : project.getId();
  }

  /**
   * Sets the project id.
   *
   * @param projectId the project id
   */
  public void setProjectId(Long projectId) {
    if (project == null) {
      project = new ProjectJpa();
    }
    project.setId(projectId);
  }

  /* see superclass */
  @Override
  public Map<String, Integer> getStats() {
    if (stats == null) {
      stats = new HashMap<>();
    }
    return stats;
  }

  /* see superclass */
  @Override
  public void setStats(Map<String, Integer> stats) {
    this.stats = stats;
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

  /**
   * Returns the concept ids. For indexing ONLY.
   *
   * @return the concept ids
   */
  @Field(name = "conceptIds", index = Index.YES, analyze = Analyze.YES, store = Store.NO)
  private String getConceptIds() {
    final StringBuilder sb = new StringBuilder();
    for (final TrackingRecord r : getTrackingRecords()) {
      for (final Long conceptId : r.getOrigConceptIds()) {
        sb.append(conceptId).append(" ");
      }
    }
    return sb.toString();
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
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
        prime * result + ((description == null) ? 0 : description.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((project == null) ? 0 : project.hashCode());

    return result;
  }

  /* see superclass */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    AbstractChecklist other = (AbstractChecklist) obj;
    if (description == null) {
      if (other.description != null)
        return false;
    } else if (!description.equals(other.description))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (project == null) {
      if (other.project != null)
        return false;
    } else if (!project.equals(other.project))
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "AbstractChecklist [id=" + id + ", lastModified=" + lastModified
        + ", lastModifiedBy=" + lastModifiedBy + ", timestamp=" + timestamp
        + ", name=" + name + ", description=" + description + ", project="
        + project + ", trackingRecords=" + trackingRecords + "]";
  }

}
