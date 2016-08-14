/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.bridge.builtin.LongBridge;

import com.wci.umls.server.AlgorithmExecution;
import com.wci.umls.server.ProcessExecution;

/**
 * JPA and JAXB enabled implementation of {@link AlgorithmExecution}.
 */
@Entity
@Table(name = "algorithm_executions")
@Audited
@Indexed
@XmlRootElement(name = "algortihmExecution")
public class AlgorithmExecutionJpa extends
    AbstractAlgorithmInfo<ProcessExecution> implements AlgorithmExecution {

  /** The last modified. */
  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date startDate;

  /** The finish date. */
  @Column(nullable = true)
  @Temporal(TemporalType.TIMESTAMP)
  private Date finishDate;

  /** The fail date. */
  @Column(nullable = true)
  @Temporal(TemporalType.TIMESTAMP)
  private Date failDate;

  /** The algorithm config id. */
  @Column(nullable = false)
  private Long algorithmConfigId;

  /** The activity id. */
  @Column(nullable = true)
  private String activityId;

  @Column(nullable = false)
  private boolean enabled = true;

  /** The project. */
  @ManyToOne(targetEntity = ProcessExecutionJpa.class, optional = false)
  private ProcessExecution process;

  /**
   * Instantiates an empty {@link AlgorithmExecutionJpa}.
   */
  public AlgorithmExecutionJpa() {
    // n/a
  }

  /**
   * Instantiates a {@link AlgorithmExecutionJpa} from the specified parameters.
   *
   * @param exec the exec
   */
  public AlgorithmExecutionJpa(AlgorithmExecution exec) {
    super(exec);
    process = exec.getProcess();
    startDate = exec.getStartDate();
    finishDate = exec.getFinishDate();
    failDate = exec.getFailDate();
    algorithmConfigId = exec.getAlgorithmConfigId();
    activityId = exec.getActivityId();
    enabled = exec.isEnabled();
  }

  /* see superclass */
  @Override
  public Date getStartDate() {
    return startDate;
  }

  /* see superclass */
  @Override
  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  /* see superclass */
  @Override
  public Date getFinishDate() {
    return finishDate;
  }

  /* see superclass */
  @Override
  public void setFinishDate(Date finishDate) {
    this.finishDate = finishDate;
  }

  /* see superclass */
  @Override
  public Date getFailDate() {
    return failDate;
  }

  /* see superclass */
  @Override
  public void setFailDate(Date failDate) {
    this.failDate = failDate;
  }

  /* see superclass */

  @Override
  @XmlTransient
  public ProcessExecution getProcess() {
    return process;
  }

  /* see superclass */
  @Override
  public void setProcess(ProcessExecution process) {
    this.process = process;
  }

  /**
   * Returns the process id. for JAXB
   *
   * @return the process id
   */
  @FieldBridge(impl = LongBridge.class)
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public Long getProcessId() {
    return process == null ? null : process.getId();
  }

  /**
   * Sets the process id.
   *
   * @param processId the process id
   */
  public void setProcessId(Long processId) {
    if (process == null) {
      process = new ProcessExecutionJpa();
    }
    process.setId(processId);
  }

  /* see superclass */
  @FieldBridge(impl = LongBridge.class)
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  @Override
  public Long getAlgorithmConfigId() {
    return algorithmConfigId;
  }

  /* see superclass */
  @Override
  public void setAlgorithmConfigId(Long algorithmConfigId) {
    this.algorithmConfigId = algorithmConfigId;
  }

  /* see superclass */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  @Override
  public String getActivityId() {
    return activityId;
  }

  /**
   * Sets the activity id.
   *
   * @param activityId the activity id
   */
  /* see superclass */
  @Override
  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }

  /* see superclass */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  @Override
  public boolean isEnabled() {
    return enabled;
  }

  /* see superclass */
  @Override
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result
        + ((algorithmConfigId == null) ? 0 : algorithmConfigId.hashCode());
    result =
        prime * result + ((activityId == null) ? 0 : activityId.hashCode());
    result = prime * result
        + ((getProcessId() == null) ? 0 : getProcessId().hashCode());
    return result;
  }

  /* see superclass */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    AlgorithmExecutionJpa other = (AlgorithmExecutionJpa) obj;
    if (algorithmConfigId == null) {
      if (other.algorithmConfigId != null)
        return false;
    } else if (!algorithmConfigId.equals(other.algorithmConfigId))
      return false;
    if (activityId == null) {
      if (other.activityId != null)
        return false;
    } else if (!activityId.equals(other.activityId))
      return false;
    if (getProcessId() == null) {
      if (other.getProcessId() != null)
        return false;
    } else if (!getProcessId().equals(other.getProcessId()))
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "AlgorithmExecutionJpa [startDate=" + startDate + ", finishDate="
        + finishDate + ", failDate=" + failDate + ", process=" + process
        + ", activityId=" + activityId + ", algorithmConfigId="
        + algorithmConfigId + "] " + super.toString();
  }

}
