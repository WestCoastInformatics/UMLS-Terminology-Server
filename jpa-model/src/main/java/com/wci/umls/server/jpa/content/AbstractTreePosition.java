/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.content;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlSeeAlso;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.SortableField;
import org.hibernate.search.annotations.Store;

import com.wci.umls.server.model.content.ComponentHasAttributesAndName;
import com.wci.umls.server.model.content.TreePosition;

/**
 * Abstract JPA and JAXB enabled implementation of {@link TreePosition}.
 * @param <T> the type
 */
@Audited
@MappedSuperclass
@XmlSeeAlso({
    CodeTreePositionJpa.class, ConceptTreePositionJpa.class,
    DescriptorTreePositionJpa.class, AtomTreePositionJpa.class
})
public abstract class AbstractTreePosition<T extends ComponentHasAttributesAndName>
    extends AbstractComponentHasAttributes implements TreePosition<T> {

  /** The additional relationship type. */
  @Column(nullable = true)
  private String additionalRelationshipType = "";

  /** The ancestor path. */
  @Column(nullable = true, length = 4000)
  private String ancestorPath;

  /** The child ct. */
  @Column(nullable = false)
  private int childCt;

  /** The descendant ct. */
  @Column(nullable = false)
  private int descendantCt;

  /**
   * Instantiates an empty {@link AbstractTreePosition}.
   */
  public AbstractTreePosition() {
    // do nothing
  }

  /**
   * Instantiates a {@link AbstractTreePosition} from the specified parameters.
   *
   * @param treepos the treepos
   * @param collectionCopy the deep copy
   */
  public AbstractTreePosition(TreePosition<T> treepos, boolean collectionCopy) {
    //super(treepos, collectionCopy);
    additionalRelationshipType = treepos.getAdditionalRelationshipType();
    ancestorPath = treepos.getAncestorPath();
    childCt = treepos.getChildCt();
    descendantCt = treepos.getDescendantCt();
  }

  /* see superclass */
  @Override
  public String getAdditionalRelationshipType() {
    return additionalRelationshipType;
  }

  /* see superclass */
  @Override
  public void setAdditionalRelationshipType(String additionalRelationshipType) {
    this.additionalRelationshipType = additionalRelationshipType;
  }

  /* see superclass */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  @SortableField(forField = "ancestorPath")
  @Override
  public String getAncestorPath() {
    return ancestorPath;
  }

  /* see superclass */
  @Override
  public void setAncestorPath(String ancestorPath) {
    this.ancestorPath = ancestorPath;
  }

  /* see superclass */
  @Override
  public int getChildCt() {
    return childCt;
  }

  /* see superclass */
  @Override
  public void setChildCt(int childCt) {
    this.childCt = childCt;
  }

  /* see superclass */
  @Override
  public int getDescendantCt() {
    return descendantCt;
  }

  /* see superclass */
  @Override
  public void setDescendantCt(int descendantCt) {
    this.descendantCt = descendantCt;
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
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((additionalRelationshipType == null) ? 0
        : additionalRelationshipType.hashCode());
    result =
        prime * result + ((ancestorPath == null) ? 0 : ancestorPath.hashCode());
    result = prime * result + childCt;
    result = prime * result + descendantCt;
    return result;
  }

  /* see superclass */
  @SuppressWarnings("rawtypes")
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    AbstractTreePosition other = (AbstractTreePosition) obj;
    if (additionalRelationshipType == null) {
      if (other.additionalRelationshipType != null)
        return false;
    } else if (!additionalRelationshipType
        .equals(other.additionalRelationshipType))
      return false;
    if (ancestorPath == null) {
      if (other.ancestorPath != null)
        return false;
    } else if (!ancestorPath.equals(other.ancestorPath))
      return false;
    if (childCt != other.childCt)
      return false;
    if (descendantCt != other.descendantCt)
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[node=" + getNode() + ", "
        + super.toString() + ", ancestorPath=" + ancestorPath + "]";
  }

}
