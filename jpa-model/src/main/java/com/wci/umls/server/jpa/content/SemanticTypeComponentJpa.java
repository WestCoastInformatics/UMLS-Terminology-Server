/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.content;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

import com.wci.umls.server.model.content.SemanticTypeComponent;
import com.wci.umls.server.model.workflow.WorkflowStatus;

/**
 * JPA and JAXB enabled implementation of {@link SemanticTypeComponent}.
 */
@Entity
@Table(name = "semantic_type_components", uniqueConstraints = @UniqueConstraint(columnNames = {
    "terminologyId", "terminology", "version", "id"
}))
@Audited
@Indexed
@XmlRootElement(name = "semanticTypeComponent")
public class SemanticTypeComponentJpa extends AbstractComponent implements
    SemanticTypeComponent {

  /** The semantic type. */
  @Column(nullable = false, length = 4000)
  private String semanticType;

  /** The workflow status. */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private WorkflowStatus workflowStatus;

  /**
   * Instantiates an empty {@link SemanticTypeComponentJpa}.
   */
  public SemanticTypeComponentJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link SemanticTypeComponentJpa} from the specified
   * parameters.
   *
   * @param semanticType the semantic type
   */
  public SemanticTypeComponentJpa(SemanticTypeComponent semanticType) {
    super(semanticType);
    this.semanticType = semanticType.getSemanticType();
    workflowStatus = semanticType.getWorkflowStatus();
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getSemanticType() {
    return semanticType;
  }

  /* see superclass */
  @Override
  public void setSemanticType(String semanticType) {
    this.semanticType = semanticType;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public WorkflowStatus getWorkflowStatus() {
    return workflowStatus;
  }

  /* see superclass */
  @Override
  public void setWorkflowStatus(WorkflowStatus workflowStatus) {
    this.workflowStatus = workflowStatus;

  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result =
        prime * result + ((semanticType == null) ? 0 : semanticType.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    SemanticTypeComponentJpa other = (SemanticTypeComponentJpa) obj;
    if (semanticType == null) {
      if (other.semanticType != null)
        return false;
    } else if (!semanticType.equals(other.semanticType))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "SemanticTypeComponentJpa [semanticType=" + semanticType
        + ", workflowStatus=" + workflowStatus + "]";
  }

}
