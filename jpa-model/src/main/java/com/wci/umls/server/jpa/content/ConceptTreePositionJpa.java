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

import com.wci.umls.server.model.content.ConceptTreePosition;

/**
 * JPA-enabled implementation of {@link ConceptTreePosition}.
 */
@Entity
@Table(name = "concept_tree_positions", uniqueConstraints = @UniqueConstraint(columnNames = {
    "terminologyId", "terminology", "terminologyVersion", "id"
}))
@Audited
@XmlRootElement(name = "conceptTreePosition")
public class ConceptTreePositionJpa extends AbstractTreePosition implements
    ConceptTreePosition {

  /** The concept id. */
  @Column(nullable = false)
  private String conceptId;

  /**
   * Instantiates an empty {@link ConceptTreePositionJpa}.
   */
  public ConceptTreePositionJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link ConceptTreePositionJpa} from the specified
   * parameters.
   *
   * @param treepos the treepos
   * @param deepCopy the deep copy
   */
  public ConceptTreePositionJpa(ConceptTreePosition treepos, boolean deepCopy) {
    super(treepos, deepCopy);
    conceptId = treepos.getConceptId();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.ConceptTreePosition#getConceptId()
   */
  @Override
  public String getConceptId() {
    return conceptId;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.ConceptTreePosition#setConceptId(java
   * .lang.String)
   */
  @Override
  public void setConceptId(String conceptId) {
    this.conceptId = conceptId;
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
    result = prime * result + ((conceptId == null) ? 0 : conceptId.hashCode());
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
    ConceptTreePositionJpa other = (ConceptTreePositionJpa) obj;
    if (conceptId == null) {
      if (other.conceptId != null)
        return false;
    } else if (!conceptId.equals(other.conceptId))
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
    return "ConceptTreePositionJpa [conceptId=" + conceptId + "]";
  }

}
