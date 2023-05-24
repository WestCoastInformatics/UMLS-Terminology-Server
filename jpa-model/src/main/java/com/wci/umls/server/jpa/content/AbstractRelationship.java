/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.content;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlSeeAlso;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.SortableField;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.bridge.builtin.EnumBridge;

import com.wci.umls.server.helpers.ComponentInfo;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.model.workflow.WorkflowStatus;

/**
 * Abstract JPA and JAXB enabled implementation of {@link Relationship}.
 *
 * @param <S> the left hand side of the relationship
 * @param <T> the right hand side of the relationship
 */
//@Audited
@MappedSuperclass
@XmlSeeAlso({
    CodeRelationshipJpa.class, ConceptRelationshipJpa.class,
    DescriptorRelationshipJpa.class, AtomRelationshipJpa.class,
    ComponentInfoRelationshipJpa.class
})
public abstract class AbstractRelationship<S extends ComponentInfo, T extends ComponentInfo>
    extends AbstractComponentHasAttributes implements Relationship<S, T> {

  /** The relationship type. */
  @Column(nullable = false)
  private String relationshipType;

  /** The additional relationship type. */
  @Column(nullable = true)
  private String additionalRelationshipType;

  /** The group. */
  @Column(name = "relGroup", nullable = true)
  private String group = "";

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
   * @param collectionCopy the deep copy
   */
  public AbstractRelationship(Relationship<S, T> relationship,
      boolean collectionCopy) {
    super(relationship, collectionCopy);
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
  @SortableField
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
  @SortableField
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
  @SortableField
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
  @FieldBridge(impl = EnumBridge.class)
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
  public String getName() {
    return null;
  }

  /* see superclass */
  @Override
  public void setName(String name) {
    // n/a
  }

  /**
   * Populate inverse relationship.
   *
   * @param relationship the relationship
   * @param inverseRelationship the inverse relationship
   * @param inverseRelType the inverse rel type
   * @param inverseAdditionalRelType the inverse additional rel type
   * @return the relationship
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  public Relationship<S, T> populateInverseRelationship(
    Relationship<S, T> relationship, Relationship<S, T> inverseRelationship,
    String inverseRelType, String inverseAdditionalRelType) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - create inverse of concept relationship "
            + relationship);
    if (relationship != null && inverseRelationship != null) {
      inverseRelationship.setId(null);
      // Need to duplicate the TerminologyId from theAbs source relationship.
      inverseRelationship.setTerminologyId(relationship.getTerminologyId());
      inverseRelationship.setTerminology(relationship.getTerminology());
      inverseRelationship.setVersion(relationship.getVersion());
      inverseRelationship.setFrom((S) relationship.getTo());
      inverseRelationship.setTo((T) relationship.getFrom());
      inverseRelationship.setRelationshipType(inverseRelType);
      inverseRelationship
          .setAdditionalRelationshipType(inverseAdditionalRelType);
      inverseRelationship
          .setAssertedDirection(!relationship.isAssertedDirection());

      // Inverse relationships don't keep the group value from its originating
      // relationship - clear it out
      inverseRelationship.setGroup("");

      // Inverse relationships are not hierarchical
      inverseRelationship.setHierarchical(false);

      return inverseRelationship;
    } else {

      return null;
    }
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((additionalRelationshipType == null) ? 0
        : additionalRelationshipType.hashCode());
    result = prime * result + (assertedDirection ? 1231 : 1237);
    result = prime * result + ((group == null) ? 0 : group.hashCode());
    result = prime * result + (hierarchical ? 1231 : 1237);
    result = prime * result + (inferred ? 1231 : 1237);
    result = prime * result
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
    AbstractRelationship<?, ?> other = (AbstractRelationship<?, ?>) obj;
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
    return getClass().getName() + " [from=" + getFrom() + ", to=" + getTo()
        + ", relationshipType=" + relationshipType
        + ", additionalRelationshipType=" + additionalRelationshipType
        + ", group=" + group + ", assertedDirection=" + assertedDirection
        + ", workflowStatus=" + workflowStatus + "] " + super.toString();
  }
}
