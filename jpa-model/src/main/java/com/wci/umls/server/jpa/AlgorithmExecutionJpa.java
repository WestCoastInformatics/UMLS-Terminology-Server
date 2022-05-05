/*
 * Copyright 2020 West Coast Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of West Coast Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * West Coast Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.umls.server.jpa;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
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

import com.wci.umls.server.AlgorithmConfig;
import com.wci.umls.server.AlgorithmExecution;
import com.wci.umls.server.ProcessExecution;

/**
 * JPA and JAXB enabled implementation of {@link AlgorithmExecution}.
 */
@Entity
// TODO: fix this
@Table(name = "algorithm_execs")
@Audited
@Indexed
@XmlRootElement(name = "algorithmExecution")
public class AlgorithmExecutionJpa extends AbstractAlgorithmInfo<ProcessExecution>
    implements AlgorithmExecution {

  /** The last modified. */
  @Column(nullable = true)
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

  /** The project. */
  @ManyToOne(targetEntity = ProcessExecutionJpa.class, optional = false)
  private ProcessExecution process;

  /** Has the algorithm had a warning fired during its execution?. */
  @Column(nullable = false)
  private boolean warning = false;

  /** the properties */
  @ElementCollection
  @MapKeyColumn(length = 100)
  @Column(nullable = true, length = 4000)
  private Map<String, String> properties = new HashMap<>();

  /**
   * Instantiates an empty {@link AlgorithmExecutionJpa}.
   */
  public AlgorithmExecutionJpa() {
    // n/a
  }

  /**
   * Lazy init.
   *
   * @throws Exception the exception
   */
  @Override
  public void lazyInit() throws Exception {
    super.lazyInit();
    getProperties().size();
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
    warning = exec.isWarning();
    properties = new HashMap<>(exec.getProperties());
  }

  /**
   * Instantiates a {@link AlgorithmExecutionJpa} from the specified parameters.
   * 
   * @param config the config
   */
  public AlgorithmExecutionJpa(AlgorithmConfig config) {
    super(config);
    // Clear out the id copied from the config
    this.setId(null);
    algorithmConfigId = config.getId();
    properties = new HashMap<>(config.getProperties());
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
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
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
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
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
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
  @Override
  @XmlTransient
  public Map<String, String> getProperties() {
    if (properties == null) {
      properties = new HashMap<>();
    }
    return properties;
  }

  /* see superclass */
  @Override
  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  /* see superclass */
  @Override
  public Boolean isWarning() {
    return warning;
  }

  /* see superclass */
  @Override
  public void setWarning(Boolean warning) {
    this.warning = warning;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((algorithmConfigId == null) ? 0 : algorithmConfigId.hashCode());
    result = prime * result + ((activityId == null) ? 0 : activityId.hashCode());
    result = prime * result + ((getProcessId() == null) ? 0 : getProcessId().hashCode());
    result = prime * result + ((properties == null) ? 0 : properties.hashCode());
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
    if (properties == null) {
      if (other.properties != null)
        return false;
    } else if (!properties.equals(other.properties))
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "AlgorithmExecutionJpa [startDate=" + startDate + ", finishDate=" + finishDate
        + ", failDate=" + failDate + ", processId=" + getProcessId() + ", activityId=" + activityId
        + ", algorithmConfigId=" + algorithmConfigId + "] " + super.toString();
  }

}
