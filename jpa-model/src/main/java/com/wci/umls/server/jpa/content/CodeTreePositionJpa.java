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

import com.wci.umls.server.model.content.CodeTreePosition;

/**
 * JPA-enabled implementation of {@link CodeTreePosition}.
 */
@Entity
@Table(name = "code_tree_positions", uniqueConstraints = @UniqueConstraint(columnNames = {
    "terminologyId", "terminology", "terminologyVersion", "id"
}))
@Audited
@XmlRootElement(name = "codeTreePosition")
public class CodeTreePositionJpa extends AbstractTreePosition implements
    CodeTreePosition {

  /** The code id. */
  @Column(nullable = false)
  private String codeId;

  /**
   * Instantiates an empty {@link CodeTreePositionJpa}.
   */
  public CodeTreePositionJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link CodeTreePositionJpa} from the specified
   * parameters.
   *
   * @param treepos the treepos
   * @param deepCopy the deep copy
   */
  public CodeTreePositionJpa(CodeTreePosition treepos, boolean deepCopy) {
    super(treepos, deepCopy);
    codeId = treepos.getCodeId();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.CodeTreePosition#getCodeId()
   */
  @Override
  public String getCodeId() {
    return codeId;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.CodeTreePosition#setCodeId(java
   * .lang.String)
   */
  @Override
  public void setCodeId(String codeId) {
    this.codeId = codeId;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.content.AbstractTreePosition#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((codeId == null) ? 0 : codeId.hashCode());
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
    CodeTreePositionJpa other = (CodeTreePositionJpa) obj;
    if (codeId == null) {
      if (other.codeId != null)
        return false;
    } else if (!codeId.equals(other.codeId))
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
    return "CodeTreePositionJpa [codeId=" + codeId + "]";
  }

}
