/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;

import com.wci.umls.server.AlgorithmConfig;
import com.wci.umls.server.ProcessConfig;

/**
 * JPA and JAXB enabled implementation of {@link ProcessConfig}.
 */
@Entity
@Table(name = "process_configs", uniqueConstraints = @UniqueConstraint(columnNames = {
    "name", "project_id"
}))
@Audited
@Indexed
@XmlRootElement(name = "processConfig")
public class ProcessConfigJpa extends AbstractProcessInfo<AlgorithmConfig>
    implements ProcessConfig {

  /** The steps . */
  @OneToMany(mappedBy = "process", targetEntity = AlgorithmConfigJpa.class)
  @OrderColumn
  private List<AlgorithmConfig> steps = new ArrayList<>();

  /**
   * Instantiates an empty {@link ProcessConfigJpa}.
   */
  public ProcessConfigJpa() {
    // n/a
  }

  /**
   * Instantiates a {@link ProcessConfigJpa} from the specified parameters.
   *
   * @param config the config
   */
  public ProcessConfigJpa(ProcessConfig config) {
    super(config);
    steps = new ArrayList<>(config.getSteps());
  }

  /* see superclass */
  @Override
  @XmlElement(type = AlgorithmConfigJpa.class)
  public List<AlgorithmConfig> getSteps() {
    return steps;
  }

  /* see superclass */
  @Override
  public void setSteps(List<AlgorithmConfig> steps) {
    this.steps = steps;
  }

  @Override
  public String toString() {
    return "ProcessConfigJpa [steps=" + steps + "] " + super.toString();
  }

}
