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
import org.hibernate.search.annotations.Store;

import com.wci.umls.server.model.content.AtomClass;
import com.wci.umls.server.model.content.TreePosition;

/**
 * Abstract JPA and JAXB enabled implementation of {@link TreePosition}.
 * @param <T> the type
 */
@Audited
@MappedSuperclass
@XmlSeeAlso({
    CodeTreePositionJpa.class, ConceptTreePositionJpa.class,
    DescriptorTreePositionJpa.class
})
public abstract class AbstractTreePosition<T extends AtomClass> extends
    AbstractComponentHasAttributes implements TreePosition<T> {

  /** The additional relationship type. */
  @Column(nullable = true)
  private String additionalRelationshipType;

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
   * @param deepCopy the deep copy
   */
  public AbstractTreePosition(TreePosition<T> treepos, boolean deepCopy) {
    super(treepos, deepCopy);
    additionalRelationshipType = treepos.getAdditionalRelationshipType();
    ancestorPath = treepos.getAncestorPath();
    childCt = treepos.getChildCt();
    descendantCt = treepos.getDescendantCt();
  }

  @Override
  public String getAdditionalRelationshipType() {
    return additionalRelationshipType;
  }

  @Override
  public void setAdditionalRelationshipType(String additionalRelationshipType) {
    this.additionalRelationshipType = additionalRelationshipType;
  }

  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  @Override
  public String getAncestorPath() {
    return ancestorPath;
  }

  @Override
  public void setAncestorPath(String ancestorPath) {
    this.ancestorPath = ancestorPath;
  }

  @Override
  public int getChildCt() {
    return childCt;
  }

  @Override
  public void setChildCt(int childCt) {
    this.childCt = childCt;
  }

  @Override
  public int getDescendantCt() {
    return descendantCt;
  }

  @Override
  public void setDescendantCt(int descendantCt) {
    this.descendantCt = descendantCt;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result =
        prime
            * result
            + ((additionalRelationshipType == null) ? 0
                : additionalRelationshipType.hashCode());
    result =
        prime * result + ((ancestorPath == null) ? 0 : ancestorPath.hashCode());
    result = prime * result + childCt;
    result = prime * result + descendantCt;
    return result;
  }

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

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[node=" + getNode() + ", "
        + super.toString() + ", ancestorPath=" + ancestorPath + "]";
  }

}
