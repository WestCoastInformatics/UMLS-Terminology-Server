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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

import com.wci.umls.server.Project;
import com.wci.umls.server.jpa.ProjectJpa;
import com.wci.umls.server.model.workflow.ProjectWorkflowConfig;
import com.wci.umls.server.model.workflow.WorkflowBinDefinition;
import com.wci.umls.server.model.workflow.WorkflowBinType;

/**
 * JPA and JAXB enabled implementation of a {@link ProjectWorkflowConfig}.
 */
@Entity
@Table(name = "project_workflow_configs", uniqueConstraints = @UniqueConstraint(columnNames = {
  "id"
}))
@Indexed
@XmlRootElement(name = "projectWorkflowConfig")
public class ProjectWorkflowConfigJpa implements ProjectWorkflowConfig {

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

  /** The type. */
  @Column(nullable = false)
  private WorkflowBinType type;

  /** The mutually exclusive. */
  @Column(nullable = false)
  private boolean mutuallyExclusive;

  /** The last partition time. */
  /** The last partition time. */
  @Column(nullable = false, unique = false)
  private Long lastPartitionTime;

  /** The workflow bin definitions. */
  @OneToMany(mappedBy = "name", targetEntity = WorkflowBinDefinitionJpa.class)
  private List<WorkflowBinDefinition> workflowBinDefinitions =
      new ArrayList<>();

  /** The project. */
  @ManyToOne(targetEntity = ProjectJpa.class, optional = false)
  private Project project;

  /**
   * Instantiates a new workflow bin definitions jpa.
   */
  public ProjectWorkflowConfigJpa() {
    // do nothing
  }

  /**
   * Instantiates a new project workflow config jpa.
   *
   * @param projectWorkflowConfig the project workflow configuration
   * @param deepCopy the deep copy
   */
  public ProjectWorkflowConfigJpa(ProjectWorkflowConfig projectWorkflowConfig,
      boolean deepCopy) {
    super();
    this.lastModified = projectWorkflowConfig.getLastModified();
    this.lastModifiedBy = projectWorkflowConfig.getLastModifiedBy();
    this.timestamp = projectWorkflowConfig.getTimestamp();
    this.project = projectWorkflowConfig.getProject();
    this.mutuallyExclusive = projectWorkflowConfig.isMutuallyExclusive();
    this.type = projectWorkflowConfig.getType();
    this.lastPartitionTime = projectWorkflowConfig.getLastPartitionTime();
    if (deepCopy) {
      this.workflowBinDefinitions =
          projectWorkflowConfig.getWorkflowBinDefinitions();
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
  @XmlElement(type = WorkflowBinDefinitionJpa.class)
  public List<WorkflowBinDefinition> getWorkflowBinDefinitions() {
    return workflowBinDefinitions;
  }

  /* see superclass */
  @Override
  public void setWorkflowBinDefinitions(List<WorkflowBinDefinition> definitions) {
    this.workflowBinDefinitions = definitions;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
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
  public boolean isMutuallyExclusive() {
    return mutuallyExclusive;
  }

  /* see superclass */
  @Override
  public void setMutuallyExclusive(boolean mutuallyExclusive) {
    this.mutuallyExclusive = mutuallyExclusive;
  }

  /* see superclass */
  @Override
  public Long getLastPartitionTime() {
    return lastPartitionTime;
  }

  /* see superclass */
  @Override
  public void setLastPartitionTime(Long lastPartitionTime) {
    this.lastPartitionTime = lastPartitionTime;
  }

  /* see superclass */
  @Override
  @XmlTransient
  public Project getProject() {
    return project;
  }

  @Override
  public void setProject(Project project) {
    this.project = project;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
        prime * result + ((lastModified == null) ? 0 : lastModified.hashCode());
    result =
        prime * result
            + ((lastModifiedBy == null) ? 0 : lastModifiedBy.hashCode());
    result =
        prime * result
            + ((lastPartitionTime == null) ? 0 : lastPartitionTime.hashCode());
    result = prime * result + (mutuallyExclusive ? 1231 : 1237);
    result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    result = prime * result + ((project == null) ? 0 : project.hashCode());
    result =
        prime
            * result
            + ((workflowBinDefinitions == null) ? 0 : workflowBinDefinitions
                .hashCode());
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
    ProjectWorkflowConfigJpa other = (ProjectWorkflowConfigJpa) obj;
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
    if (project == null) {
      if (other.project != null)
        return false;
    } else if (!project.equals(other.project))
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

  /* see superclass */
  @Override
  public String toString() {
    return "WorkflowBinDefinitionsJpa [id=" + id + ", lastModified="
        + lastModified + ", lastModifiedBy=" + lastModifiedBy + ", timestamp="
        + timestamp + ", type=" + type + ", project=" + project
        + ", mutuallyExclusive=" + mutuallyExclusive + ", lastPartitionTime="
        + lastPartitionTime + ", workflowBinDefinitions="
        + workflowBinDefinitions + "]";
  }

}
