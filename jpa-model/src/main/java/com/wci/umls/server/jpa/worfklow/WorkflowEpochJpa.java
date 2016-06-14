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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.bridge.builtin.BooleanBridge;
import org.hibernate.search.bridge.builtin.LongBridge;

import com.wci.umls.server.Project;
import com.wci.umls.server.jpa.ProjectJpa;
import com.wci.umls.server.model.workflow.WorkflowBin;
import com.wci.umls.server.model.workflow.WorkflowEpoch;

/**
 * JPA-enabled implementation of a {@link WorkflowEpoch}.
 */
@Entity
@Table(name = "workflow_epochs", uniqueConstraints = @UniqueConstraint(columnNames = {
    "name", "project_id"
}))
@Indexed
@XmlRootElement(name = "workflowEpoch")
public class WorkflowEpochJpa implements WorkflowEpoch {

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

  /** The name. */
  @Column(nullable = false)
  private String name;

  /** The active. */
  @Column(nullable = false)
  private boolean active;

  /** The workflow bins. */
  @OneToMany(targetEntity = WorkflowBinJpa.class)
  private List<WorkflowBin> workflowBins = null;

  /** The project. */
  @ManyToOne(targetEntity = ProjectJpa.class, optional = false)
  private Project project;

  /**
   * Instantiates an empty {@link WorkflowEpochJpa}.
   */
  public WorkflowEpochJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link WorkflowEpochJpa} from the specified parameters.
   *
   * @param epoch the workflow epoch
   * @param deepCopy the deep copy
   */
  public WorkflowEpochJpa(WorkflowEpoch epoch, boolean deepCopy) {
    id = epoch.getId();
    lastModified = epoch.getLastModified();
    lastModifiedBy = epoch.getLastModifiedBy();
    timestamp = epoch.getTimestamp();
    name = epoch.getName();
    active = epoch.isActive();
    project = epoch.getProject();
    if (deepCopy) {
      workflowBins = new ArrayList<>(epoch.getWorkflowBins());
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
  @FieldBridge(impl = BooleanBridge.class)
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  @Override
  public boolean isActive() {
    return active;
  }

  /* see superclass */
  @Override
  public void setActive(boolean active) {
    this.active = active;
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
    return project == null ? 0L : project.getId();
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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (active ? 1231 : 1237);
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result =
        prime * result + ((workflowBins == null) ? 0 : workflowBins.hashCode());
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
    WorkflowEpochJpa other = (WorkflowEpochJpa) obj;
    if (active != other.active)
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (workflowBins == null) {
      if (other.workflowBins != null)
        return false;
    } else if (!workflowBins.equals(other.workflowBins))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "WorkflowEpochJpa [lastModified=" + lastModified
        + ", lastModifiedBy=" + lastModifiedBy + ", timestamp=" + timestamp
        + ", name=" + name + ", active=" + active + "]";
  }

  @Override
  public List<WorkflowBin> getWorkflowBins() {
    if (workflowBins == null) {
      return new ArrayList<>();
    }
    return workflowBins;
  }

  @Override
  public void setWorkflowBins(List<WorkflowBin> workflowBins) {
    this.workflowBins = workflowBins;
  }

}
