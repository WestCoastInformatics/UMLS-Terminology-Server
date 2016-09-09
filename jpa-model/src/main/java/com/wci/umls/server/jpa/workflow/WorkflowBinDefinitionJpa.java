/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.workflow;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
import org.hibernate.search.bridge.builtin.EnumBridge;
import org.hibernate.search.bridge.builtin.LongBridge;

import com.wci.umls.server.helpers.QueryType;
import com.wci.umls.server.model.workflow.WorkflowBinDefinition;
import com.wci.umls.server.model.workflow.WorkflowConfig;

/**
 * JPA and JAXB enabled implementation of a {@link WorkflowBinDefinition}.
 */
@Entity
@Table(name = "workflow_bin_definitions", uniqueConstraints = @UniqueConstraint(columnNames = {
    "name", "workflowConfig_id"
}))
@Indexed
@XmlRootElement(name = "workflowBinDefinition")
public class WorkflowBinDefinitionJpa implements WorkflowBinDefinition {

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

  /** The description. */
  @Column(nullable = false)
  private String description;

  /** The query. */
  @Column(nullable = false, length = 4000)
  private String query;

  /** The query type. */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private QueryType queryType;

  /** The editable flag. */
  @Column(nullable = false)
  private boolean editable;

  /**  The enabled flag. */
  @Column(nullable = false)
  private boolean enabled;

  /** The required flag. */
  @Column(nullable = false)
  private boolean required;

  /** The workflow config. */
  @ManyToOne(targetEntity = WorkflowConfigJpa.class, optional = false)
  @JoinColumn(nullable = false, name = "workflowConfig_id")
  private WorkflowConfig workflowConfig;

  /**
   * Instantiates a new workflow bin definition jpa.
   */
  public WorkflowBinDefinitionJpa() {
    // do nothing
  }

  /**
   * Instantiates a new workflow bin definition jpa.
   *
   * @param def the workflow bin definition
   */
  public WorkflowBinDefinitionJpa(WorkflowBinDefinition def) {
    id = def.getId();
    lastModified = def.getLastModified();
    lastModifiedBy = def.getLastModifiedBy();
    timestamp = def.getTimestamp();
    name = def.getName();
    description = def.getDescription();
    query = def.getQuery();
    queryType = def.getQueryType();
    editable = def.isEditable();
    enabled = def.isEnabled();
    required = def.isRequired();
    workflowConfig = def.getWorkflowConfig();
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
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
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
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public boolean isEditable() {
    return editable;
  }

  /* see superclass */
  @Override
  public void setEditable(boolean editable) {
    this.editable = editable;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
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
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public boolean isRequired() {
    return required;
  }

  /* see superclass */
  @Override
  public void setRequired(boolean required) {
    this.required = required;
  }

  /* see superclass */
  @Override
  public String getQuery() {
    return query;
  }

  /* see superclass */
  @Override
  public void setQuery(String query) {
    this.query = query;
  }

  /* see superclass */
  @Override
  @Field(bridge = @FieldBridge(impl = EnumBridge.class), index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public QueryType getQueryType() {
    return queryType;
  }

  /* see superclass */
  @Override
  public void setQueryType(QueryType queryType) {
    this.queryType = queryType;
  }

  /* see superclass */
  @XmlTransient
  @Override
  public WorkflowConfig getWorkflowConfig() {
    return workflowConfig;
  }

  /* see superclass */
  @Override
  public void setWorkflowConfig(WorkflowConfig workflowConfig) {
    this.workflowConfig = workflowConfig;
  }

  /**
   * Returns the workflow config id.
   *
   * @return the workflow config id
   */
  @FieldBridge(impl = LongBridge.class)
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public Long getWorkflowConfigId() {
    return workflowConfig == null ? null : workflowConfig.getId();
  }

  /**
   * Sets the workflow config id.
   *
   * @param id the workflow config id
   */
  @Field()
  public void setWorkflowConfigId(Long id) {
    if (workflowConfig == null) {
      workflowConfig = new WorkflowConfigJpa();
    }
    workflowConfig.setId(id);
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
        prime * result + ((description == null) ? 0 : description.hashCode());
    result = prime * result + (editable ? 1231 : 1237);
    result = prime * result + (enabled ? 1231 : 1237);
    result = prime * result + (required ? 1231 : 1237);
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((query == null) ? 0 : query.hashCode());
    result = prime * result + ((queryType == null) ? 0 : queryType.hashCode());
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
    WorkflowBinDefinitionJpa other = (WorkflowBinDefinitionJpa) obj;
    if (description == null) {
      if (other.description != null)
        return false;
    } else if (!description.equals(other.description))
      return false;
    if (editable != other.editable)
      return false;
    if (enabled != other.enabled)
      return false;
    if (required != other.required)
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (query == null) {
      if (other.query != null)
        return false;
    } else if (!query.equals(other.query))
      return false;
    if (queryType == null) {
      if (other.queryType != null)
        return false;
    } else if (!queryType.equals(other.queryType))
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "WorkflowBinDefinitionJpa [id=" + id + ", lastModified="
        + lastModified + ", lastModifiedBy=" + lastModifiedBy + ", timestamp="
        + timestamp + ", name=" + name + ", description=" + description
        + ", query=" + query + ", queryType=" + queryType + ", editable="
        + editable + ", required=" + required + ", enabled=" + enabled + "]";
  }

}
