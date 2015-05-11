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

import com.wci.umls.server.model.content.DescriptorTreePosition;

/**
 * JPA-enabled implementation of {@link DescriptorTreePosition}.
 */
@Entity
@Table(name = "descriptor_tree_positions", uniqueConstraints = @UniqueConstraint(columnNames = {
    "terminologyId", "terminology", "terminologyVersion", "id"
}))
@Audited
@XmlRootElement(name = "descriptorTreePosition")
public class DescriptorTreePositionJpa extends AbstractTreePosition implements
    DescriptorTreePosition {

  /** The descriptor id. */
  @Column(nullable = false)
  private String descriptorId;

  /**
   * Instantiates an empty {@link DescriptorTreePositionJpa}.
   */
  public DescriptorTreePositionJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link DescriptorTreePositionJpa} from the specified
   * parameters.
   *
   * @param treepos the treepos
   * @param deepCopy the deep copy
   */
  public DescriptorTreePositionJpa(DescriptorTreePosition treepos,
      boolean deepCopy) {
    super(treepos, deepCopy);
    descriptorId = treepos.getDescriptorId();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.DescriptorTreePosition#getDescriptorId()
   */
  @Override
  public String getDescriptorId() {
    return descriptorId;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.DescriptorTreePosition#setDescriptorId
   * (java .lang.String)
   */
  @Override
  public void setDescriptorId(String descriptorId) {
    this.descriptorId = descriptorId;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.content.AbstractTreePosition#hashDescriptor()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result =
        prime * result + ((descriptorId == null) ? 0 : descriptorId.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.content.AbstractTreePosition#equals(java.lang.Object
   * )
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    DescriptorTreePositionJpa other = (DescriptorTreePositionJpa) obj;
    if (descriptorId == null) {
      if (other.descriptorId != null)
        return false;
    } else if (!descriptorId.equals(other.descriptorId))
      return false;
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.content.AbstractTreePosition#toString()
   */
  @Override
  public String toString() {
    return "DescriptorTreePositionJpa [descriptorId=" + descriptorId + "]";
  }

}
