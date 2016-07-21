/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.worfklow;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.bridge.builtin.EnumBridge;
import org.hibernate.search.bridge.builtin.LongBridge;

import com.wci.umls.server.Project;
import com.wci.umls.server.jpa.ProjectJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.helpers.CollectionToCsvBridge;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.workflow.TrackingRecord;
import com.wci.umls.server.model.workflow.WorkflowStatus;

/**
 * JAXB and JPA enabled implementation of {@link TrackingRecord}.
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
  @Fetch(value = FetchMode.SELECT)
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
  @Column(nullable = true)
  private String workflowBinName;

  /** The worklist name. */
  @Column(nullable = true)
  private String worklistName;

  /** The checklist name. */
  @Column(nullable = true)
  private String checklistName;
  
  /** The original concept ids . */
  @ElementCollection
  @CollectionTable(name = "orig_concept_ids")
  private Set<Long> origConceptIds = new HashSet<>();

  /** The concepts. */
  @Transient
  private List<Concept> concepts = new ArrayList<>();

  /** The project. */
  @ManyToOne(targetEntity = ProjectJpa.class, optional = false)
  private Project project;

  /** The workflow status. */
  @Enumerated(EnumType.STRING)
  @Column(nullable = true)
  private WorkflowStatus workflowStatus;
  
  /**  The indexed data. */
  private String indexedData;

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
    origConceptIds = new HashSet<>(record.getOrigConceptIds());
    workflowBinName = record.getWorkflowBinName();
    worklistName = record.getWorklistName();
    checklistName = record.getChecklistName();
    project = record.getProject();
    workflowStatus = record.getWorkflowStatus();
    indexedData = record.getIndexedData();
  }

  /* see superclass */
  @Override
  @FieldBridge(impl = LongBridge.class)
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
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
  @Field(bridge = @FieldBridge(impl = CollectionToCsvBridge.class), index = Index.YES, analyze = Analyze.YES, store = Store.NO)
  @Override
  public Set<Long> getOrigConceptIds() {
    if (origConceptIds == null) {
      origConceptIds = new HashSet<>();
    }
    return origConceptIds;
  }

  /* see superclass */
  @Override
  public void setOrigConceptIds(Set<Long> origConceptIds) {
    this.origConceptIds = origConceptIds;
  }

  /* see superclass */
  @Override
  @XmlElement(type = ConceptJpa.class)
  public List<Concept> getConcepts() {
    if (concepts == null) {
      concepts = new ArrayList<>();
    }
    return concepts;
  }

  /* see superclass */
  @Override
  public void setConcepts(List<Concept> concepts) {
    this.concepts = concepts;
  }

  /* see superclass */
  @Override
  @Fields({
      @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO, bridge = @FieldBridge(impl = LongBridge.class)),
      @Field(name = "clusterIdSort", index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  })
  public Long getClusterId() {
    return clusterId;
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
  @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
  @Override
  public String getIndexedData() {
    return indexedData;
  }
  
  @Override
  public void setIndexedData(String indexedData) {
    this.indexedData = indexedData;
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
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getWorkflowBinName() {
    return workflowBinName;
  }

  /* see superclass */
  @Override
  public void setWorkflowBinName(String workflowBin) {
    this.workflowBinName = workflowBin;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getWorklistName() {
    return worklistName;
  }

  /* see superclass */
  @Override
  public void setWorklistName(String worklist) {
    this.worklistName = worklist;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getChecklistName() {
    return checklistName;
  }

  /* see superclass */
  @Override
  public void setChecklistName(String checklistName) {
    this.checklistName = checklistName;
  }
  
  /* see superclass */
  @Override
  @FieldBridge(impl = EnumBridge.class)
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public WorkflowStatus getWorkflowStatus() {
    return workflowStatus;
  }

  /* see superclass */
  @Override
  public void setWorkflowStatus(WorkflowStatus workflowStatus) {
    this.workflowStatus = workflowStatus;

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
        prime * result + ((componentIds == null) ? 0 : componentIds.hashCode());
    result =
        prime * result
            + ((origConceptIds == null) ? 0 : origConceptIds.hashCode());
    result = prime * result + ((project == null) ? 0 : project.hashCode());
    result =
        prime * result + ((terminology == null) ? 0 : terminology.hashCode());
    result = prime * result + ((version == null) ? 0 : version.hashCode());
    result =
        prime * result
            + ((workflowBinName == null) ? 0 : workflowBinName.hashCode());
    result =
        prime * result + ((worklistName == null) ? 0 : worklistName.hashCode());
    result =
        prime * result + ((checklistName == null) ? 0 : checklistName.hashCode());
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
    if (componentIds == null) {
      if (other.componentIds != null)
        return false;
    } else if (!componentIds.equals(other.componentIds))
      return false;
    if (origConceptIds == null) {
      if (other.origConceptIds != null)
        return false;
    } else if (!origConceptIds.equals(other.origConceptIds))
      return false;
    if (project == null) {
      if (other.project != null)
        return false;
    } else if (!project.equals(other.project))
      return false;
    if (terminology == null) {
      if (other.terminology != null)
        return false;
    } else if (!terminology.equals(other.terminology))
      return false;
    if (version == null) {
      if (other.version != null)
        return false;
    } else if (!version.equals(other.version))
      return false;
    if (workflowBinName == null) {
      if (other.workflowBinName != null)
        return false;
    } else if (!workflowBinName.equals(other.workflowBinName))
      return false;
    if (worklistName == null) {
      if (other.worklistName != null)
        return false;
    } else if (!worklistName.equals(other.worklistName))
      return false;
    if (checklistName == null) {
      if (other.checklistName != null)
        return false;
    } else if (!checklistName.equals(other.checklistName))
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
        + ", version=" + version + ", origConceptIds=" + origConceptIds
        + ", project=" + project + "]";
  }

}