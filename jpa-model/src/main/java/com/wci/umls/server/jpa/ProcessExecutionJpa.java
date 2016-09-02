/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.bridge.builtin.LongBridge;

import com.wci.umls.server.AlgorithmExecution;
import com.wci.umls.server.ProcessConfig;
import com.wci.umls.server.ProcessExecution;

/**
 * JPA and JAXB enabled implementation of {@link ProcessExecution}.
 */
@Entity
@Table(name = "process_executions")
@Audited
@Indexed
@XmlRootElement(name = "processExecution")
public class ProcessExecutionJpa extends AbstractProcessInfo<AlgorithmExecution>
    implements ProcessExecution {

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

  /** The process config id. */
  @Column(nullable = false)
  private Long processConfigId;

  /** The work id. */
  @Column(nullable = true)
  private String workId;

  /** The steps . */
  @OneToMany(mappedBy = "process", targetEntity = AlgorithmExecutionJpa.class)
  @OrderColumn
  private List<AlgorithmExecution> steps = new ArrayList<>();

  /**
   * Instantiates an empty {@link ProcessExecutionJpa}.
   */
  public ProcessExecutionJpa() {
    // n/a
  }

  /**
   * Instantiates a {@link ProcessExecutionJpa} from the specified parameters.
   *
   * @param exec the exec
   */
  public ProcessExecutionJpa(ProcessExecution exec) {
    super(exec);
    startDate = exec.getStartDate();
    finishDate = exec.getFinishDate();
    failDate = exec.getFailDate();
    processConfigId = exec.getProcessConfigId();
    workId = exec.getWorkId();
    for (final AlgorithmExecution step : exec.getSteps()) {
      getSteps().add(new AlgorithmExecutionJpa(step));
    }
  }

  /**
   * Instantiates a {@link ProcessExecutionJpa} from the specified parameters.
   *
   * @param config the config
   */
  public ProcessExecutionJpa(ProcessConfig config) {
    super(config);
    processConfigId = config.getId();
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
  @XmlElement(type = AlgorithmExecutionJpa.class)
  public List<AlgorithmExecution> getSteps() {
    return steps;
  }

  /* see superclass */
  @Override
  public void setSteps(List<AlgorithmExecution> steps) {
    this.steps = steps;
  }

  /* see superclass */
  @FieldBridge(impl = LongBridge.class)
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  @Override
  public Long getProcessConfigId() {
    return processConfigId;
  }

  /* see superclass */
  @Override
  public void setProcessConfigId(Long processConfigId) {
    this.processConfigId = processConfigId;
  }

  /* see superclass */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  @Override
  public String getWorkId() {
    return workId;
  }

  /* see superclass */
  @Override
  public void setWorkId(String workId) {
    this.workId = workId;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result
        + ((processConfigId == null) ? 0 : processConfigId.hashCode());
    result = prime * result + ((workId == null) ? 0 : workId.hashCode());
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
    ProcessExecutionJpa other = (ProcessExecutionJpa) obj;
    if (processConfigId == null) {
      if (other.processConfigId != null)
        return false;
    } else if (!processConfigId.equals(other.processConfigId))
      return false;
    if (workId == null) {
      if (other.workId != null)
        return false;
    } else if (!workId.equals(other.workId))
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "ProcessExecutionJpa [startDate=" + startDate + ", finishDate="
        + finishDate + ", failDate=" + failDate + ", steps=" + steps
        + ", workId=" + workId + ", processConfigId=" + processConfigId + "] "
        + super.toString();

  }
}
