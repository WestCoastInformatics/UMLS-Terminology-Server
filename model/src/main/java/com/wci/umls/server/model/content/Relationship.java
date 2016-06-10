/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.content;

import com.wci.umls.server.helpers.HasAlternateTerminologyIds;
import com.wci.umls.server.helpers.HasTerminologyId;
import com.wci.umls.server.model.workflow.WorkflowStatus;

/**
 * Represents a relationship between two {@link ComponentHasAttributes}s.
 *
 * @param <S> the "from" object type
 * @param <T> the "to" object type
 */
public interface Relationship<S extends HasTerminologyId, T extends HasTerminologyId>
    extends ComponentHasAttributes, HasAlternateTerminologyIds {
  
  /**
   * Returns the from.
   *
   * @return the from
   */
  public S getFrom();

  /**
   * Sets the from.
   *
   * @param component the from
   */
  public void setFrom(S component);

  /**
   * Returns the to.
   *
   * @return the to
   */
  public T getTo();

  /**
   * Sets the to.
   *
   * @param component the to
   */
  public void setTo(T component);

  /**
   * Returns the relationship label.
   *
   * @return the relationship label
   */
  public String getRelationshipType();

  /**
   * Sets the relationship label.
   *
   * @param relationshipType the relationship label
   */
  public void setRelationshipType(String relationshipType);

  /**
   * Returns the additional relationship label.
   *
   * @return the additional relationship label
   */
  public String getAdditionalRelationshipType();

  /**
   * Sets the additional relationship label.
   *
   * @param additionalrelationshipType the additional relationship label
   */
  public void setAdditionalRelationshipType(String additionalrelationshipType);

  /**
   * Returns the relationship group. This is a mechanism for binding sets of
   * relationships together to either simulate one level of anonymous
   * subclassing or for other purposes.
   * 
   * @return the relationship group
   */
  public String getGroup();

  /**
   * Sets the relationship group.
   * 
   * @param group the relationship group
   */
  public void setGroup(String group);

  /**
   * Indicates whether or not this is the relation direction asserted by the
   * terminology.
   * 
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isAssertedDirection();

  /**
   * Sets the asserted direction flag.
   * 
   * @param assertedDirection the asserted direction flag
   */
  public void setAssertedDirection(boolean assertedDirection);

  /**
   * Indicates whether the relationship is inferred.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isInferred();

  /**
   * Sets the inferred flag.
   *
   * @param inferred the inferred flag
   */
  public void setInferred(boolean inferred);

  /**
   * Indicates whether the relationship is stated.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isStated();

  /**
   * Sets the stated flag.
   *
   * @param stated the stated flag
   */
  public void setStated(boolean stated);

  /**
   * Indicates whether the relationship is hierarchical.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isHierarchical();

  /**
   * Sets the hierarchical flag.
   *
   * @param hierarchical the hierarchical flag
   */
  public void setHierarchical(boolean hierarchical);

  /**
   * Returns the workflow status.
   *
   * @return the workflow status
   */
  public WorkflowStatus getWorkflowStatus();

  /**
   * Sets the workflow status.
   *
   * @param workflowStatus the workflow status
   */
  public void setWorkflowStatus(WorkflowStatus workflowStatus);
}