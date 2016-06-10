/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.worfklow;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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

import com.wci.umls.server.model.workflow.WorkflowBinDefinition;



/**
 * JPA and JAXB enabled implementation of a {@link WorkflowBinDefinition}.
 */
@Entity
@Table(name = "workflow_bin_definition", uniqueConstraints = @UniqueConstraint(columnNames = {
     "id"
}))
@Indexed
@XmlRootElement(name = "workflowBinDefinition")
public class WorkflowBinDefinitionJpa  implements WorkflowBinDefinition {

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
  @Column(nullable = false)
  private String query;
  
  /** The query type. */
  @Column(nullable = false)
  private String queryType;
  
  /** The editable. */
  @Column(nullable = false)
  private boolean editable;
  
  /**
   * Instantiates a new workflow bin definition jpa.
   */
  public WorkflowBinDefinitionJpa() {
    // do nothing
  }
  
  /**
   * Instantiates a new workflow bin definition jpa.
   *
   * @param workflowBinDefinition the workflow bin definition
   */
  public WorkflowBinDefinitionJpa(WorkflowBinDefinition workflowBinDefinition) {
    super();
    this.lastModified = workflowBinDefinition.getLastModified();
    this.lastModifiedBy = workflowBinDefinition.getLastModifiedBy();
    this.timestamp = workflowBinDefinition.getTimestamp();
    this.name = workflowBinDefinition.getName();
    this.description = workflowBinDefinition.getDescription();
    this.query = workflowBinDefinition.getQuery();
    this.queryType = workflowBinDefinition.getQuery();
    this.editable = workflowBinDefinition.isEditable();
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
  @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
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
  @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
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
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getQueryType() {
    return queryType;
  }

  /* see superclass */
  @Override
  public void setQueryType(String queryType) {
    this.queryType = queryType;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
        prime * result + ((description == null) ? 0 : description.hashCode());
    result = prime * result + (editable ? 1231 : 1237);
    result =
        prime * result + ((lastModified == null) ? 0 : lastModified.hashCode());
    result = prime * result
        + ((lastModifiedBy == null) ? 0 : lastModifiedBy.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((query == null) ? 0 : query.hashCode());
    result = prime * result + ((queryType == null) ? 0 : queryType.hashCode());
    result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
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
    if (timestamp == null) {
      if (other.timestamp != null)
        return false;
    } else if (!timestamp.equals(other.timestamp))
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "WorkflowBinDefinitionJpa [lastModified=" + lastModified
        + ", lastModifiedBy=" + lastModifiedBy + ", timestamp=" + timestamp
        + ", name=" + name + ", description=" + description + ", query=" + query
        + ", queryType=" + queryType + ", editable=" + editable + "]";
  }


}
