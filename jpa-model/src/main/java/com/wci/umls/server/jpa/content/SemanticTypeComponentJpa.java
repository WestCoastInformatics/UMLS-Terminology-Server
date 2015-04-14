/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.content;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;

import com.wci.umls.server.model.content.SemanticTypeComponent;

/**
 * JPA-enabled implementation of {@link SemanticTypeComponent}.
 */
@Entity
@Table(name = "semantic_type_components", uniqueConstraints = @UniqueConstraint(columnNames = {
    "terminologyId", "terminology", "terminologyVersion", "id"
}))
@Audited
@XmlRootElement(name = "semanticTypeComponent")
public class SemanticTypeComponentJpa extends AbstractComponentHasAttributes implements
    SemanticTypeComponent {

  /** The semantic type. */
  @Column(nullable = false, length = 4000)
  private String semanticType;
  
  /**  The workflow status. */
  @Column(nullable = true)
  private String workflowStatus;

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
   * @param deepCopy the deep copy
   */
  public SemanticTypeComponentJpa(SemanticTypeComponent semanticType,
      boolean deepCopy) {
    super(semanticType, deepCopy);
    this.semanticType = semanticType.getSemanticType();
    workflowStatus = semanticType.getWorkflowStatus();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.content.AbstractComponent#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result =
        prime * result + ((semanticType == null) ? 0 : semanticType.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.content.AbstractComponent#equals(java.lang.Object)
   */
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

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.content.AbstractComponent#toString()
   */
  @Override
  public String toString() {
    return "DefinitionJpa [value=" + semanticType + "]";
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.SemanticTypeComponent#getSemanticType()
   */
  @Override
  public String getSemanticType() {
    return semanticType;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.SemanticTypeComponent#setSemanticType
   * (java.lang.String)
   */
  @Override
  public void setSemanticType(String semanticType) {
    this.semanticType = semanticType;
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.model.content.SemanticTypeComponent#getWorkflowStatus()
   */
  @Override
  public String getWorkflowStatus() {
    return workflowStatus;
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.model.content.SemanticTypeComponent#setWorkflowStatus(java.lang.String)
   */
  @Override
  public void setWorkflowStatus(String workflowStatus) {
    this.workflowStatus = workflowStatus;
    
  }

}
