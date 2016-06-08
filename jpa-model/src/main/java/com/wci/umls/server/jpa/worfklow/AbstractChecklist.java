package com.wci.umls.server.jpa.worfklow;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Store;

import com.wci.umls.server.model.workflow.Checklist;
import com.wci.umls.server.model.workflow.TrackingRecord;
import com.wci.umls.server.model.workflow.WorkflowBin;

/**
 * Abstract JPA-enabled implementation of a {@link Checklist}.
 */
@Audited
@MappedSuperclass
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
  @Column(nullable = false)
  private String name;
  
  /** The description. */
  @Column(nullable = false)
  private String description;
  
  /** The tracking records. */
  @OneToMany(mappedBy = "worklist", targetEntity = TrackingRecordJpa.class)
  private List<TrackingRecord> trackingRecords = new ArrayList<>();
  
  @Column(nullable = false)
  private WorkflowBin workflowBin;
  
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
    this.lastModified = checklist.getLastModified();
    this.lastModifiedBy = checklist.getLastModifiedBy();
    this.timestamp = checklist.getTimestamp();
    this.name = checklist.getName();
    this.description = checklist.getDescription();
    this.workflowBin = checklist.getWorkflowBin();
    if (deepCopy) {
      this.trackingRecords = checklist.getTrackingRecords();
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

  /**
   * Returns the object id. Needed for JAXB id
   *
   * @return the object id
   */
  @XmlID
  public String getObjectId() {
    return id == null ? "" : id.toString();
  }

  /**
   * Sets the object id.
   *
   * @param id the object id
   */
  public void setObjectId(String id) {
    if (id != null) {
      this.id = Long.parseLong(id);
    }
  }

  /* see superclass */
  @XmlElement(type = TrackingRecordJpa.class)
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

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
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
  @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
  public String getDescription() {
    return description;
  }

  /* see superclass */
  @Override
  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public WorkflowBin getWorkflowBin() {
    return workflowBin;
  }

  @Override
  public void setWorkflowBin(WorkflowBin workflowBin) {
    this.workflowBin = workflowBin;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
        prime * result + ((description == null) ? 0 : description.hashCode());
    result =
        prime * result + ((lastModified == null) ? 0 : lastModified.hashCode());
    result = prime * result
        + ((lastModifiedBy == null) ? 0 : lastModifiedBy.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
    result = prime * result
        + ((trackingRecords == null) ? 0 : trackingRecords.hashCode());
    result =
        prime * result + ((workflowBin == null) ? 0 : workflowBin.hashCode());
    return result;
  }

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
    if (lastModified == null) {
      if (other.lastModified != null)
        return false;
    } else if (!lastModified.equals(other.lastModified))
      return false;
    if (lastModifiedBy == null) {
      if (other.lastModifiedBy != null)
        return false;
    } else if (!lastModifiedBy.equals(other.lastModifiedBy))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (timestamp == null) {
      if (other.timestamp != null)
        return false;
    } else if (!timestamp.equals(other.timestamp))
      return false;
    if (trackingRecords == null) {
      if (other.trackingRecords != null)
        return false;
    } else if (!trackingRecords.equals(other.trackingRecords))
      return false;
    if (workflowBin == null) {
      if (other.workflowBin != null)
        return false;
    } else if (!workflowBin.equals(other.workflowBin))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "AbstractChecklist [id=" + id + ", lastModified=" + lastModified
        + ", lastModifiedBy=" + lastModifiedBy + ", timestamp=" + timestamp
        + ", name=" + name + ", description=" + description
        + ", trackingRecords=" + trackingRecords + ", workflowBin="
        + workflowBin + "]";
  }
  



}
