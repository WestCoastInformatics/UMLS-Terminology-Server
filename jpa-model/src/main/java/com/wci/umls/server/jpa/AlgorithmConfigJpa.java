/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;

import com.wci.umls.server.AlgorithmConfig;
import com.wci.umls.server.ProcessConfig;

/**
 * JPA and JAXB enabled implementation of {@link AlgorithmConfig}.
 */
@Entity
@Table(name = "algorithm_configs")
@Audited
@Indexed
@XmlRootElement(name = "algorithmConfig")
public class AlgorithmConfigJpa extends AbstractAlgorithmInfo<ProcessConfig>
    implements AlgorithmConfig {

  /** The project. */
  @ManyToOne(targetEntity = ProcessConfigJpa.class, optional = false)
  private ProcessConfig process;

  /**
   * Instantiates an empty {@link AlgorithmConfigJpa}.
   */
  public AlgorithmConfigJpa() {
    // n/a
  }

  /**
   * Instantiates a {@link AlgorithmConfigJpa} from the specified parameters.
   *
   * @param config the config
   */
  public AlgorithmConfigJpa(AlgorithmConfig config) {
    super(config);
    process = config.getProcess();

  }

  /* see superclass */
  @XmlTransient
  @Override
  public ProcessConfig getProcess() {
    return process;
  }

  /* see superclass */
  @Override
  public void setProcess(ProcessConfig process) {
    this.process = process;
  }

  /**
   * Returns the process id. for JAXB
   *
   * @return the process id
   */
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
      process = new ProcessConfigJpa();
    }
    process.setId(processId);
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
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
    AlgorithmConfigJpa other = (AlgorithmConfigJpa) obj;
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
    return "AlgorithmConfigJpa [process=" + process + "] " + super.toString();
  }

}
