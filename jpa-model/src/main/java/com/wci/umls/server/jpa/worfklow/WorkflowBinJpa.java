package com.wci.umls.server.jpa.worfklow;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.bridge.builtin.BooleanBridge;
import org.hibernate.search.bridge.builtin.EnumBridge;
import org.hibernate.search.bridge.builtin.LongBridge;

import com.wci.umls.server.Project;
import com.wci.umls.server.jpa.ProjectJpa;
import com.wci.umls.server.model.workflow.TrackingRecord;
import com.wci.umls.server.model.workflow.WorkflowBin;
import com.wci.umls.server.model.workflow.WorkflowBinType;

/**
 * JPA-enabled implementation of a {@link WorkflowBin}.
 */
@Entity
@Table(name = "workflow_bins", uniqueConstraints = @UniqueConstraint(columnNames = {
    "terminologyId", "id"
}))
@Indexed
@XmlRootElement(name = "workflowBin")
public class WorkflowBinJpa implements WorkflowBin {

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

  /** The terminology id. */
  @Column(nullable = false)
  private String terminologyId;

  /** The terminology. */
  @Column(nullable = false)
  private String terminology;

  /** The version. */
  @Column(nullable = false)
  private String version;

  /** The type. */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private WorkflowBinType type = WorkflowBinType.MUTUALLY_EXCLUSIVE;

  /** The rank. */
  @Column(nullable = false)
  private int rank;

  /** The editable. */
  @Column(nullable = false)
  private boolean editable;

  /** The tracking records. */
  @OneToMany(mappedBy = "workflowBin", targetEntity = TrackingRecordJpa.class)
  private List<TrackingRecord> trackingRecords = new ArrayList<>();

  /** The creation time. */
  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date creationTime = null;

  /** The project. */
  @ManyToOne(targetEntity = ProjectJpa.class, optional = false)
  private Project project;
  
  
  /**
   * Instantiates an empty {@link WorkflowBinJpa}.
   */
  public WorkflowBinJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link WorkflowBinJpa} from the specified parameters.
   *
   * @param bin the workflow bin
   * @param deepCopy the deep copy
   */
  public WorkflowBinJpa(WorkflowBin bin, boolean deepCopy) {
    id = bin.getId();
    lastModified = bin.getLastModified();
    lastModifiedBy = bin.getLastModifiedBy();
    timestamp = bin.getTimestamp();
    name = bin.getName();
    description = bin.getDescription();
    terminologyId = bin.getTerminologyId();
    terminology = bin.getTerminology();
    version = bin.getVersion();
    type = bin.getType();
    rank = bin.getRank();
    editable = bin.isEditable();
    creationTime = bin.getCreationTime();
    project = bin.getProject();
    if (deepCopy) {
      trackingRecords = new ArrayList<>(bin.getTrackingRecords());
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

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
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
  @Field(bridge = @FieldBridge(impl = EnumBridge.class), index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  @Override
  public WorkflowBinType getType() {
    return type;
  }

  /* see superclass */
  @Override
  public void setType(WorkflowBinType type) {
    this.type = type;
  }

  /* see superclass */
  @Override
  public int getRank() {
    return rank;
  }

  /* see superclass */
  @Override
  public void setRank(int rank) {
    this.rank = rank;
  }

  /* see superclass */
  @Override
  @FieldBridge(impl = BooleanBridge.class)
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public boolean isEditable() {
    return editable;
  }

  /* see superclass */
  @Override
  public void setEditable(boolean editable) {
    this.editable = editable;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getTerminologyId() {
    return terminologyId;
  }

  /* see superclass */
  @Override
  public void setTerminologyId(String terminologyId) {
    this.terminologyId = terminologyId;
  }

  /* see superclass */
  @Override
  public Date getCreationTime() {
    return creationTime;
  }

  /* see superclass */
  @Override
  public void setCreationTime(Date creationTime) {
    this.creationTime = creationTime;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getTerminology() {
    return terminology;
  }

  /* see superclass */
  @Override
  public void setTerminology(String terminology) {
    this.terminology = terminology;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getVersion() {
    return version;
  }

  /* see superclass */
  @Override
  public void setVersion(String version) {
    this.version = version;
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
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
        prime * result + ((description == null) ? 0 : description.hashCode());
    result = prime * result + (editable ? 1231 : 1237);
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + rank;
    result =
        prime * result + ((terminology == null) ? 0 : terminology.hashCode());
    result =
        prime * result
            + ((terminologyId == null) ? 0 : terminologyId.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    result = prime * result + ((version == null) ? 0 : version.hashCode());
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
    WorkflowBinJpa other = (WorkflowBinJpa) obj;
    if (description == null) {
      if (other.description != null)
        return false;
    } else if (!description.equals(other.description))
      return false;
    if (editable != other.editable)
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (rank != other.rank)
      return false;
    if (terminology == null) {
      if (other.terminology != null)
        return false;
    } else if (!terminology.equals(other.terminology))
      return false;
    if (terminologyId == null) {
      if (other.terminologyId != null)
        return false;
    } else if (!terminologyId.equals(other.terminologyId))
      return false;
    if (type != other.type)
      return false;
    if (version == null) {
      if (other.version != null)
        return false;
    } else if (!version.equals(other.version))
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "WorkflowBinJpa [id=" + id + ", lastModified=" + lastModified
        + ", lastModifiedBy=" + lastModifiedBy + ", timestamp=" + timestamp
        + ", name=" + name + ", description=" + description
        + ", terminologyId=" + terminologyId + ", terminology=" + terminology
        + ", version=" + version + ", type=" + type + ", rank=" + rank
        + ", editable=" + editable + ", creationTime=" + creationTime + "]";
  }

}
