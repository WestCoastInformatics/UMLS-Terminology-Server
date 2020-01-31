/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

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

  /** The type. */
  @Column(nullable = false)
  private String type;

  /** The input path. */
  @Column(nullable = true)
  private String inputPath;
  
  /** The log path. */
  @Column(nullable = true)
  private String logPath;

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
    type = config.getType();
    inputPath = config.getInputPath();
    logPath = config.getLogPath();
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

  /* see superclass */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  @Override
  public String getType() {
    return type;
  }

  /* see superclass */
  @Override
  public void setType(String type) {
    this.type = type;
  }

  /* see superclass */
  @Override
  public String getInputPath() {
    return inputPath;
  }

  /* see superclass */
  @Override
  public void setInputPath(String inputPath) {
    this.inputPath = inputPath;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "ProcessConfigJpa [steps=" + steps + ", type=" + type + "] "
        + super.toString();
  }

  /* see superclass */
  @Override
  public String getLogPath() {
    return logPath;
  }

  /* see superclass */
  @Override
  public void setLogPath(String logPath) {
    this.logPath = logPath;
  }
}
