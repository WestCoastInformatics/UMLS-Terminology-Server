/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.content;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import org.hibernate.envers.Audited;

import com.wci.umls.server.model.content.ComponentHasAttributesAndName;
import com.wci.umls.server.model.content.TransitiveRelationship;

/**
 * Abstract JPA and JAXB enabled implementation of
 * {@link TransitiveRelationship}.
 *
 * @param <T> the component type
 */
@Audited
@MappedSuperclass
public abstract class AbstractTransitiveRelationship<T extends ComponentHasAttributesAndName>
    extends AbstractComponentHasAttributes implements TransitiveRelationship<T> {

  /** The depth. */
  @Column(nullable = false)
  private int depth;

  /**
   * Instantiates an empty {@link AbstractTransitiveRelationship}.
   */
  public AbstractTransitiveRelationship() {
    // do nothing
  }

  /**
   * Instantiates a {@link AbstractTransitiveRelationship} from the specified
   * parameters.
   *
   * @param relationship the relationship
   * @param deepCopy the deep copy
   */
  public AbstractTransitiveRelationship(TransitiveRelationship<T> relationship,
      boolean deepCopy) {
    super(relationship, deepCopy);
  }

  /* see superclass */
  @Override
  public int getDepth() {
    return depth;
  }

  /* see superclass */
  @Override
  public void setDepth(int depth) {
    this.depth = depth;
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

  /* see superclass */
  @Override
  public String toString() {
    return getClass().getSimpleName() + " [superType=" + getSuperType()
        + ", subType=" + getSubType() + ", " + super.toString() + "]";
  }
}
