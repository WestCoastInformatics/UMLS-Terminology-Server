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
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

import com.wci.umls.server.model.workflow.WorkflowBin;
import com.wci.umls.server.model.workflow.WorkflowEpoch;

/**
 * JPA-enabled implementation of a {@link WorkflowEpoch}.
 */
@Entity
@Table(name = "workflow_epochs", uniqueConstraints = @UniqueConstraint(columnNames = {
    "name", "id"
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
  
  /**
   * Instantiates an empty {@link WorkflowEpochJpa}.
   */
  public WorkflowEpochJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link WorkflowEpochJpa} from the specified parameters.
   *
   * @param workflowEpoch the workflow epoch
   * @param deepCopy the deep copy
   */
  public WorkflowEpochJpa(WorkflowEpoch workflowEpoch, boolean deepCopy) {
    this.lastModified = workflowEpoch.getLastModified();
    this.lastModifiedBy = workflowEpoch.getLastModifiedBy();
    this.timestamp = workflowEpoch.getTimestamp();
    this.name = workflowEpoch.getName();
    this.active = workflowEpoch.isActive();
    if (deepCopy) {
      this.workflowBins = workflowEpoch.getWorkflowBins();
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
  @Override
  public boolean isActive() {
    return active;
  }

  /* see superclass */
  @Override
  public void setActive(boolean active) {
    this.active = active;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (active ? 1231 : 1237);
    result =
        prime * result + ((lastModified == null) ? 0 : lastModified.hashCode());
    result = prime * result
        + ((lastModifiedBy == null) ? 0 : lastModifiedBy.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
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
