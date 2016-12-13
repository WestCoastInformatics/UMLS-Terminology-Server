/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
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
public abstract class AbstractAlgorithmInfo<T extends ProcessInfo<?>>
    implements AlgorithmInfo<T> {

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

  /** the properties */
  @ElementCollection
  @Column(length = 4000)
  private Map<String, String> properties = new HashMap<>();

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
    properties = new HashMap<>(info.getProperties());
    parameters = new ArrayList<>(info.getParameters());
    algorithmKey = info.getAlgorithmKey();

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
  public String getDescription() {
    return description;
  }

  /* see superclass */
  @Override
  public void setDescription(String description) {
    this.description = description;
  }

  /* see superclass */
  @Override
  public String getAlgorithmKey() {
    return algorithmKey;
  }

  /* see superclass */
  @Override
  public void setAlgorithmKey(String algorithmKey) {
    this.algorithmKey = algorithmKey;

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

  /* see superclass */
  @Override
  @XmlElement(type = AlgorithmParameterJpa.class)
  public List<AlgorithmParameter> getParameters() {
    if (parameters == null) {
      parameters = new ArrayList<>();
    }
    return parameters;
  }

  /* see superclass */
  @Override
  public void setParameters(List<AlgorithmParameter> parameters) {
    this.parameters = parameters;
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
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
        prime * result + ((algorithmKey == null) ? 0 : algorithmKey.hashCode());
    result =
        prime * result + ((description == null) ? 0 : description.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result
        + ((getProjectId() == null) ? 0 : getProjectId().hashCode());
    result =
        prime * result + ((properties == null) ? 0 : properties.hashCode());

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
    if (properties == null) {
      if (other.properties != null)
        return false;
    } else if (!properties.equals(other.properties))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "AbstractAlgorithmInfo [id=" + id + ", lastModified=" + lastModified
        + ", lastModifiedBy=" + lastModifiedBy + ", timestamp=" + timestamp
        + ", name=" + name + ", description=" + description + ", algorithmKey="
        + algorithmKey + ", properties=" + properties + "]";
  }

}
