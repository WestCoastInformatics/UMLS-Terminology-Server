/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.content;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import org.hibernate.envers.Audited;

import com.wci.umls.server.model.content.TreePosition;

/**
 * Abstract JPA-enabled implementation of {@link TreePosition}.
 */
@Audited
@MappedSuperclass
public abstract class AbstractTreePosition extends
    AbstractComponentHasAttributes implements TreePosition {

  /** The additional relationship type. */
  @Column(nullable = true)
  private String additionalRelationshipType;

  /** The ancestor path. */
  @Column(nullable = true, length = 4000)
  private String ancestorPath;

  /** The default preferred name. */
  @Column(nullable = true, length = 4000)
  private String defaultPreferredName;

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
  public AbstractTreePosition(TreePosition treepos, boolean deepCopy) {
    super(treepos, deepCopy);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.TreePosition#getAdditionalRelationshipType
   * ()
   */
  @Override
  public String getAdditionalRelationshipType() {
    return additionalRelationshipType;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.TreePosition#setAdditionalRelationshipType
   * (java.lang.String)
   */
  @Override
  public void setAdditionalRelationshipType(String additionalRelationshipType) {
    this.additionalRelationshipType = additionalRelationshipType;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.TreePosition#getAncestorPath()
   */
  @Override
  public String getAncestorPath() {
    return ancestorPath;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.TreePosition#setAncestorPath(java.lang
   * .String)
   */
  @Override
  public void setAncestorPath(String ancestorPath) {
    this.ancestorPath = ancestorPath;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.TreePosition#getDefaultPreferredName()
   */
  @Override
  public String getDefaultPreferredName() {
    return defaultPreferredName;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.TreePosition#setDefaultPreferredName(
   * java.lang.String)
   */
  @Override
  public void setDefaultPreferredName(String defaultPreferredName) {
    this.defaultPreferredName = defaultPreferredName;
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
        prime
            * result
            + ((additionalRelationshipType == null) ? 0
                : additionalRelationshipType.hashCode());
    result =
        prime * result + ((ancestorPath == null) ? 0 : ancestorPath.hashCode());
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
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.content.AbstractComponent#toString()
   */
  @Override
  public String toString() {
    return "AbstractTreePosition [additionalRelationshipType="
        + additionalRelationshipType + ", ancestorPath=" + ancestorPath
        + ", defaultPreferredName=" + defaultPreferredName + "]";
  }

}
