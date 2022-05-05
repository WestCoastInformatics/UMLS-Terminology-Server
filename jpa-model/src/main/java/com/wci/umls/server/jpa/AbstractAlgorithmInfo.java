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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.bridge.builtin.LongBridge;

import com.wci.umls.server.AlgorithmInfo;
import com.wci.umls.server.AlgorithmParameter;
import com.wci.umls.server.ProcessInfo;
import com.wci.umls.server.Project;

/**
 * JPA and JAXB enabled implementation of {@link AlgorithmInfo}.
 * @param <T> the process info type (e.g. config or execution)
 */
@Audited
@MappedSuperclass
@XmlSeeAlso({
    AlgorithmConfigJpa.class, AlgorithmExecutionJpa.class
})
public abstract class AbstractAlgorithmInfo<T extends ProcessInfo<?>> implements AlgorithmInfo<T> {

  /** The id. */
  @TableGenerator(name = "EntityIdGen", table = "table_generator", pkColumnValue = "Entity")
  @Id
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "EntityIdGen")
  private Long id;

  /** The last modified. */
  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date lastModified;

  /** The last modified. */
  @Column(nullable = false)
  private String lastModifiedBy;

  /** The last modified. */
  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date timestamp = new Date();

  /** The name. */
  @Column(nullable = false)
  private String name;

  /** The description. */
  @Column(nullable = false)
  private String description;

  /** The algorithm key. */
  @Column(nullable = false)
  private String algorithmKey;

  /** The project. */
  @ManyToOne(targetEntity = ProjectJpa.class, optional = false)
  private Project project;

  /** parameters. */
  @Transient
  private List<AlgorithmParameter> parameters = new ArrayList<>();

  /**
   * Instantiates an empty {@link AbstractAlgorithmInfo}.
   */
  public AbstractAlgorithmInfo() {
    // n/a
  }

  /**
   * Lazy init.
   *
   * @throws Exception the exception
   */
  public void lazyInit() throws Exception {
    project.lazyInit();
  }

  /**
   * Instantiates a {@link AbstractAlgorithmInfo} from the specified parameters.
   *
   * @param info the config
   */
  public AbstractAlgorithmInfo(AlgorithmInfo<?> info) {
    id = info.getId();
    timestamp = info.getTimestamp();
    lastModified = info.getLastModified();
    lastModifiedBy = info.getLastModifiedBy();
    name = info.getName();
    description = info.getDescription();
    project = info.getProject();
    parameters = new ArrayList<>(info.getParameters());
    algorithmKey = info.getAlgorithmKey();

  }

  /**
   * Returns the id.
   *
   * @return the id
   */
  /* see superclass */
  @Override
  public Long getId() {
    return id;
  }

  /**
   * Sets the id.
   *
   * @param id the id
   */
  /* see superclass */
  @Override
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Returns the last modified.
   *
   * @return the last modified
   */
  /* see superclass */
  @Override
  public Date getLastModified() {
    return lastModified;
  }

  /**
   * Sets the last modified.
   *
   * @param lastModified the last modified
   */
  /* see superclass */
  @Override
  public void setLastModified(Date lastModified) {
    this.lastModified = lastModified;
  }

  /**
   * Returns the last modified by.
   *
   * @return the last modified by
   */
  /* see superclass */
  @Override
  public String getLastModifiedBy() {
    return lastModifiedBy;
  }

  /**
   * Sets the last modified by.
   *
   * @param lastModifiedBy the last modified by
   */
  /* see superclass */
  @Override
  public void setLastModifiedBy(String lastModifiedBy) {
    this.lastModifiedBy = lastModifiedBy;
  }

  /**
   * Returns the timestamp.
   *
   * @return the timestamp
   */
  /* see superclass */
  @Override
  public Date getTimestamp() {
    return timestamp;
  }

  /**
   * Sets the timestamp.
   *
   * @param timestamp the timestamp
   */
  /* see superclass */
  @Override
  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  /**
   * Returns the name.
   *
   * @return the name
   */
  /* see superclass */
  @Override
  public String getName() {
    return name;
  }

  /**
   * Sets the name.
   *
   * @param name the name
   */
  /* see superclass */
  @Override
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the description.
   *
   * @return the description
   */
  /* see superclass */
  @Override
  public String getDescription() {
    return description;
  }

  /**
   * Sets the description.
   *
   * @param description the description
   */
  /* see superclass */
  @Override
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Returns the algorithm key.
   *
   * @return the algorithm key
   */
  /* see superclass */
  @Override
  public String getAlgorithmKey() {
    return algorithmKey;
  }

  /**
   * Sets the algorithm key.
   *
   * @param algorithmKey the algorithm key
   */
  /* see superclass */
  @Override
  public void setAlgorithmKey(String algorithmKey) {
    this.algorithmKey = algorithmKey;

  }

  /**
   * Returns the project.
   *
   * @return the project
   */
  /* see superclass */
  @Override
  @XmlTransient
  public Project getProject() {
    return project;
  }

  /**
   * Sets the project.
   *
   * @param project the project
   */
  /* see superclass */
  @Override
  public void setProject(Project project) {
    this.project = project;
  }

  /**
   * Returns the project id. For JPA and JAXB.
   *
   * @return the project id
   */
  @FieldBridge(impl = LongBridge.class)
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public Long getProjectId() {
    return project == null ? null : project.getId();
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

  /**
   * Returns the parameters.
   *
   * @return the parameters
   */
  /* see superclass */
  @Override
  @XmlElement(type = AlgorithmParameterJpa.class)
  public List<AlgorithmParameter> getParameters() {
    if (parameters == null) {
      parameters = new ArrayList<>();
    }
    return parameters;
  }

  /**
   * Sets the parameters.
   *
   * @param parameters the parameters
   */
  /* see superclass */
  @Override
  public void setParameters(List<AlgorithmParameter> parameters) {
    this.parameters = parameters;
  }

  /**
   * Hash code.
   *
   * @return the int
   */
  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((algorithmKey == null) ? 0 : algorithmKey.hashCode());
    result = prime * result + ((description == null) ? 0 : description.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((getProjectId() == null) ? 0 : getProjectId().hashCode());

    return result;
  }

  /**
   * Equals.
   *
   * @param obj the obj
   * @return true, if successful
   */
  /* see superclass */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    @SuppressWarnings("rawtypes")
    AbstractAlgorithmInfo other = (AbstractAlgorithmInfo) obj;
    if (algorithmKey == null) {
      if (other.algorithmKey != null)
        return false;
    } else if (!algorithmKey.equals(other.algorithmKey))
      return false;
    if (description == null) {
      if (other.description != null)
        return false;
    } else if (!description.equals(other.description))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (getProjectId() == null) {
      if (other.getProjectId() != null)
        return false;
    } else if (!getProjectId().equals(other.getProjectId()))
      return false;

    return true;
  }

  /**
   * To string.
   *
   * @return the string
   */
  @Override
  public String toString() {
    return "AbstractAlgorithmInfo [id=" + id + ", lastModified=" + lastModified
        + ", lastModifiedBy=" + lastModifiedBy + ", timestamp=" + timestamp + ", name=" + name
        + ", description=" + description + ", algorithmKey=" + algorithmKey + "]";
  }

}
