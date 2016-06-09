/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.worfklow;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

import com.wci.umls.server.model.workflow.WorkflowBinDefinition;
import com.wci.umls.server.model.workflow.WorkflowBinDefinitions;
import com.wci.umls.server.model.workflow.WorkflowBinType;



/**
 * JPA and JAXB enabled implementation of a {@link WorkflowBinDefinitions}.
 */
@Entity
@Table(name = "workflow_bin_definitions", uniqueConstraints = @UniqueConstraint(columnNames = {
     "id"
}))
@Indexed
@XmlRootElement(name = "workflowBinDefinitions")
public class WorkflowBinDefinitionsJpa  implements WorkflowBinDefinitions {

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
  
  /** The timestamp. */
  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date timestamp = null;  


  private WorkflowBinType type;
  
  @Column(nullable = false)
  private boolean mutuallyExclusive;
  
  @Column(nullable = false, unique = false)
  private Long lastPartitionTime;
  
  @OneToMany(mappedBy = "name", targetEntity = WorkflowBinDefinitionJpa.class)
  private List<WorkflowBinDefinition> workflowBinDefinitions = new ArrayList<>();
  
  /**
   * Instantiates a new workflow bin definitions jpa.
   */
  public WorkflowBinDefinitionsJpa() {
    // do nothing
  }
  
  /**
   * Instantiates a new workflow bin definition jpa.
   *
   * @param workflowBinDefinition the workflow bin definitions
   */
  public WorkflowBinDefinitionsJpa(WorkflowBinDefinitions workflowBinDefinitions) {
    super();
    this.lastModified = workflowBinDefinitions.getLastModified();
    this.lastModifiedBy = workflowBinDefinitions.getLastModifiedBy();
    this.timestamp = workflowBinDefinitions.getTimestamp();
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

  @Override
  @XmlElement(type = WorkflowBinDefinitionJpa.class)
  public List<WorkflowBinDefinition> getWorkflowBinDefinitions() {
    return workflowBinDefinitions;
  }

  @Override
  public void setWorkflowBinDefinitions(
    List<WorkflowBinDefinition> definitions) {
    this.workflowBinDefinitions = definitions;
  }

  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public WorkflowBinType getType() {
    return type;
  }

  @Override
  public void setType(WorkflowBinType type) {
    this.type = type;
  }

  @Override
  public boolean getMutuallyExclusive() {
    return mutuallyExclusive;
  }

  @Override
  public void setMutuallyExclusive(boolean mutuallyExclusive) {
    this.mutuallyExclusive = mutuallyExclusive;
  }

  @Override
  public Long getLastPartitionTime() {
    return lastPartitionTime;
  }

  @Override
  public void setLastPartitionTime(Long lastPartitionTime) {
    this.lastPartitionTime = lastPartitionTime;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
        prime * result + ((lastModified == null) ? 0 : lastModified.hashCode());
    result = prime * result
        + ((lastModifiedBy == null) ? 0 : lastModifiedBy.hashCode());
    result = prime * result
        + ((lastPartitionTime == null) ? 0 : lastPartitionTime.hashCode());
    result = prime * result + (mutuallyExclusive ? 1231 : 1237);
    result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    result = prime * result + ((workflowBinDefinitions == null) ? 0
        : workflowBinDefinitions.hashCode());
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
    WorkflowBinDefinitionsJpa other = (WorkflowBinDefinitionsJpa) obj;
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
    if (lastPartitionTime == null) {
      if (other.lastPartitionTime != null)
        return false;
    } else if (!lastPartitionTime.equals(other.lastPartitionTime))
      return false;
    if (mutuallyExclusive != other.mutuallyExclusive)
      return false;

    if (timestamp == null) {
      if (other.timestamp != null)
        return false;
    } else if (!timestamp.equals(other.timestamp))
      return false;
    if (type != other.type)
      return false;
    if (workflowBinDefinitions == null) {
      if (other.workflowBinDefinitions != null)
        return false;
    } else if (!workflowBinDefinitions.equals(other.workflowBinDefinitions))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "WorkflowBinDefinitionsJpa [id=" + id + ", lastModified="
        + lastModified + ", lastModifiedBy=" + lastModifiedBy + ", timestamp="
        + timestamp + ", type=" + type
        + ", mutuallyExclusive=" + mutuallyExclusive + ", lastPartitionTime="
        + lastPartitionTime + ", workflowBinDefinitions="
        + workflowBinDefinitions + "]";
  }



}
