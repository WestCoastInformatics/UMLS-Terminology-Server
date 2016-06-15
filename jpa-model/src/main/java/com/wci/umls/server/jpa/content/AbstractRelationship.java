/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.content;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlSeeAlso;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Store;

import com.wci.umls.server.helpers.HasTerminologyId;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.model.workflow.WorkflowStatus;

/**
 * Abstract JPA and JAXB enabled implementation of {@link Relationship}.
 *
 * @param <S> the left hand side of the relationship
 * @param <T> the right hand side of the relationship
 */
@Audited
@MappedSuperclass
@XmlSeeAlso({
    CodeRelationshipJpa.class, ConceptRelationshipJpa.class,
    DescriptorRelationshipJpa.class, AtomRelationshipJpa.class,
    ComponentInfoRelationshipJpa.class
})
public abstract class AbstractRelationship<S extends HasTerminologyId, T extends HasTerminologyId>
    extends AbstractComponentHasAttributes implements Relationship<S, T> {

  /** The relationship type. */
  @Column(nullable = false)
  private String relationshipType;

  /** The additional relationship type. */
  @Column(nullable = true)
  private String additionalRelationshipType;

  /** The group. */
  @Column(name = "relGroup", nullable = true)
  private String group;

  /** The inferred. */
  @Column(nullable = false)
  private boolean inferred;

  /** The stated. */
  @Column(nullable = false)
  private boolean stated;

  /** The hierarchical. */
  @Column(nullable = false)
  private boolean hierarchical;

  /** The asserted direction flag. */
  @Column(nullable = false)
  private boolean assertedDirection;

  /** The workflow status. */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private WorkflowStatus workflowStatus;

  /**
   * Instantiates an empty {@link AbstractRelationship}.
   */
  public AbstractRelationship() {
    // do nothing
  }

  /**
   * Instantiates a {@link AbstractRelationship} from the specified parameters.
   *
   * @param relationship the relationship
   * @param deepCopy the deep copy
   */
  public AbstractRelationship(Relationship<S, T> relationship, boolean deepCopy) {
    super(relationship, deepCopy);
    relationshipType = relationship.getRelationshipType();
    additionalRelationshipType = relationship.getAdditionalRelationshipType();
    group = relationship.getGroup();
    inferred = relationship.isInferred();
    stated = relationship.isStated();
    hierarchical = relationship.isHierarchical();
    assertedDirection = relationship.isAssertedDirection();
    workflowStatus = relationship.getWorkflowStatus();
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getRelationshipType() {
    return relationshipType;
  }

  /* see superclass */
  @Override
  public void setRelationshipType(String relationshipType) {
    this.relationshipType = relationshipType;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getAdditionalRelationshipType() {
    return additionalRelationshipType;
  }

  /* see superclass */
  @Override
  public void setAdditionalRelationshipType(String additionalRelationshipType) {
    this.additionalRelationshipType = additionalRelationshipType;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getGroup() {
    return group;
  }

  /* see superclass */
  @Override
  public void setGroup(String group) {
    this.group = group;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public boolean isInferred() {
    return inferred;
  }

  /* see superclass */
  @Override
  public void setInferred(boolean inferred) {
    this.inferred = inferred;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public boolean isStated() {
    return stated;
  }

  /* see superclass */
  @Override
  public void setStated(boolean stated) {
    this.stated = stated;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public boolean isHierarchical() {
    return hierarchical;
  }

  /* see superclass */
  @Override
  public void setHierarchical(boolean hierarchical) {
    this.hierarchical = hierarchical;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public boolean isAssertedDirection() {
    return assertedDirection;
  }

  /* see superclass */
  @Override
  public void setAssertedDirection(boolean assertedDirection) {
    this.assertedDirection = assertedDirection;
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

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result =
        prime
            * result
            + ((additionalRelationshipType == null) ? 0
                : additionalRelationshipType.hashCode());
    result = prime * result + (assertedDirection ? 1231 : 1237);
    result = prime * result + ((group == null) ? 0 : group.hashCode());
    result = prime * result + (hierarchical ? 1231 : 1237);
    result = prime * result + (inferred ? 1231 : 1237);
    result =
        prime * result
            + ((relationshipType == null) ? 0 : relationshipType.hashCode());
    result = prime * result + (stated ? 1231 : 1237);
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
    AbstractRelationship other = (AbstractRelationship) obj;
    if (additionalRelationshipType == null) {
      if (other.additionalRelationshipType != null)
        return false;
    } else if (!additionalRelationshipType
        .equals(other.additionalRelationshipType))
      return false;
    if (assertedDirection != other.assertedDirection)
      return false;
    if (group == null) {
      if (other.group != null)
        return false;
    } else if (!group.equals(other.group))
      return false;
    if (hierarchical != other.hierarchical)
      return false;
    if (inferred != other.inferred)
      return false;
    if (relationshipType == null) {
      if (other.relationshipType != null)
        return false;
    } else if (!relationshipType.equals(other.relationshipType))
      return false;
    if (stated != other.stated)
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return getClass().getSimpleName() + " [from = " + getFrom() + ", to = "
        + getTo() + ", " + super.toString() + ", relationshipType="
        + relationshipType + ", additionalRelationshipType="
        + additionalRelationshipType + ", group=" + group + ", inferred="
        + inferred + ", stated=" + stated + ", assertedDirection="
        + assertedDirection + ", hierarchcial=" + hierarchical
        + ", workflowStatus=" + workflowStatus + "]";
  }
}
