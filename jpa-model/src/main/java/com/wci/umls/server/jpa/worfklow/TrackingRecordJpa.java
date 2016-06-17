/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.worfklow;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.bridge.builtin.LongBridge;

import com.wci.umls.server.jpa.helpers.CollectionToCsvBridge;
import com.wci.umls.server.model.workflow.TrackingRecord;
import com.wci.umls.server.model.workflow.WorkflowBin;
import com.wci.umls.server.model.workflow.Worklist;

/**
 * JPA enabled implementation of {@link TrackingRecord}.
 */
@Entity
@Table(name = "tracking_records")
@Indexed
@XmlRootElement(name = "trackingRecord")
public class TrackingRecordJpa implements TrackingRecord {

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

  /** The component ids . */
  @ElementCollection
  @CollectionTable(name = "component_ids")
  private Set<Long> componentIds = new HashSet<>();

  /** The cluster id. */
  @Column(nullable = false)
  private Long clusterId;

  /** The cluster type. */
  @Column(nullable = false)
  private String clusterType;

  /** The terminology. */
  @Column(nullable = false)
  private String terminology;

  /** The version. */
  @Column(nullable = false)
  private String version;

  /** The workflow bin. */
  @ManyToOne(targetEntity = WorkflowBinJpa.class)
  private WorkflowBin workflowBin;

  /** The worklist . */
  @ManyToOne(targetEntity = WorklistJpa.class, optional = true)
  private Worklist worklist;

  /**
   * Instantiates an empty {@link TrackingRecordJpa}.
   */
  public TrackingRecordJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link TrackingRecordJpa} from the specified parameters.
   *
   * @param record the record
   */
  public TrackingRecordJpa(TrackingRecord record) {
    id = record.getId();
    lastModified = record.getLastModified();
    lastModifiedBy = record.getLastModifiedBy();
    timestamp = record.getTimestamp();
    clusterId = record.getClusterId();
    clusterType = record.getClusterType();
    terminology = record.getTerminology();
    version = record.getVersion();
    componentIds = new HashSet<>(record.getComponentIds());
    workflowBin = record.getWorkflowBin();
    worklist = record.getWorklist();
  }

  /* see superclass */
  @Override
  public Long getId() {
    return this.id;
  }

  /* see superclass */
  @Override
  public void setId(Long id) {
    this.id = id;
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
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  @Override
  public String getLastModifiedBy() {
    return lastModifiedBy;
  }

  /* see superclass */
  @Override
  public void setLastModifiedBy(String lastModifiedBy) {
    this.lastModifiedBy = lastModifiedBy;
  }

  /* see superclass */
  @Field(bridge = @FieldBridge(impl = CollectionToCsvBridge.class), index = Index.YES, analyze = Analyze.YES, store = Store.NO)
  @Override
  public Set<Long> getComponentIds() {
    if (componentIds == null) {
      componentIds = new HashSet<>();
    }
    return componentIds;
  }

  /* see superclass */
  @Override
  public void setComponentIds(Set<Long> componentIds) {
    this.componentIds = componentIds;
  }

  /* see superclass */
  @Override
  @FieldBridge(impl = LongBridge.class)
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public Long getClusterId() {
    return clusterId == null ? null : clusterId;
  }

  /* see superclass */
  @Override
  public void setClusterId(Long clusterId) {
    this.clusterId = clusterId;
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
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  @Override
  public String getClusterType() {
    return clusterType;
  }

  /* see superclass */
  @Override
  public void setClusterType(String clusterType) {
    this.clusterType = clusterType;
  }

  /* see superclass */
  @XmlTransient
  @Override
  public WorkflowBin getWorkflowBin() {
    return workflowBin;
  }

  /* see superclass */
  @Override
  public void setWorkflowBin(WorkflowBin workflowBin) {
    this.workflowBin = workflowBin;
  }

  /**
   * Returns the workflow bin id.
   *
   * @return the workflow bin id
   */
  @FieldBridge(impl = LongBridge.class)
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public Long getWorkflowBinId() {
    return workflowBin == null ? null : workflowBin.getId();
  }

  /**
   * Sets the workflow bin id.
   *
   * @param workflowBinId the workflow bin id
   */
  public void setWorkflowBinId(Long workflowBinId) {
    if (workflowBin == null) {
      workflowBin = new WorkflowBinJpa();
    }
    workflowBin.setId(workflowBinId);
  }

  /* see superclass */
  @XmlTransient
  @Override
  public Worklist getWorklist() {
    return worklist;
  }

  /* see superclass */
  @Override
  public void setWorklist(Worklist worklist) {
    this.worklist = worklist;
  }

  /**
   * Returns the worklist id.
   *
   * @return the worklist id
   */
  @FieldBridge(impl = LongBridge.class)
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public Long getWorklistId() {
    return worklist == null ? null : worklist.getId();
  }

  /**
   * Sets the worklist id.
   *
   * @param worklistId the worklist id
   */
  public void setWorklistId(Long worklistId) {
    if (worklist == null) {
      worklist = new WorklistJpa();
    }
    worklist.setId(worklistId);
  }

  /**
   * Returns the project id. Just for indexing.
   *
   * @return the project id
   * @XmlElement, though then "set" method becomes complicated and
   *              nondeterministic for testing.
   */
  @XmlTransient
  @FieldBridge(impl = LongBridge.class)
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public Long getProjectId() {
    if (this.workflowBin != null) {
      return workflowBin.getProject().getId();
    }
    return null;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((clusterId == null) ? 0 : clusterId.hashCode());
    result =
        prime * result + ((clusterType == null) ? 0 : clusterType.hashCode());
    result =
        prime * result + ((terminology == null) ? 0 : terminology.hashCode());
    result =
        prime * result
            + ((componentIds == null) ? 0 : componentIds.hashCode());
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
    TrackingRecordJpa other = (TrackingRecordJpa) obj;
    if (clusterId == null) {
      if (other.clusterId != null)
        return false;
    } else if (!clusterId.equals(other.clusterId))
      return false;
    if (clusterType == null) {
      if (other.clusterType != null)
        return false;
    } else if (!clusterType.equals(other.clusterType))
      return false;
    if (terminology == null) {
      if (other.terminology != null)
        return false;
    } else if (!terminology.equals(other.terminology))
      return false;
    if (componentIds == null) {
      if (other.componentIds != null)
        return false;
    } else if (!componentIds.equals(other.componentIds))
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
    return "TrackingRecordJpa [id=" + id + ", lastModified=" + lastModified
        + ", lastModifiedBy=" + lastModifiedBy + ", timestamp=" + timestamp
        + ", componentIds=" + componentIds + ", clusterId=" + clusterId
        + ", clusterType=" + clusterType + ", terminology=" + terminology
        + ", version=" + version + "]";
  }

}