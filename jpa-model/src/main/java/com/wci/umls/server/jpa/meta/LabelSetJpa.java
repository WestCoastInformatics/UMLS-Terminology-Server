/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.meta;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;

import com.wci.umls.server.model.meta.AttributeName;
import com.wci.umls.server.model.meta.LabelSet;

/**
 * JPA-enabled implementation of {@link AttributeName}.
 */
@Entity
@Table(name = "label_sets", uniqueConstraints = @UniqueConstraint(columnNames = {
    "abbreviation", "terminology"
}))
@Audited
@XmlRootElement(name = "labelSet")
public class LabelSetJpa extends AbstractAbbreviation implements LabelSet {

  /** The description. */
  @Column(nullable = false)
  private String description;

  /** The derived flag. */
  @Column(nullable = false)
  private boolean derived;

  /**
   * Instantiates an empty {@link LabelSetJpa}.
   */
  public LabelSetJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link LabelSetJpa} from the specified parameters.
   *
   * @param labelSet the atn
   */
  public LabelSetJpa(LabelSet labelSet) {
    super(labelSet);
    description = labelSet.getDescription();
    derived = labelSet.isDerived();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.LabelSet#getDescription()
   */
  @Override
  public String getDescription() {
    return description;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.LabelSet#setDescription(java.lang.String)
   */
  @Override
  public void setDescription(String description) {
    this.description = description;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.meta.AbstractAbbreviation#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result =
        prime * result + ((description == null) ? 0 : description.hashCode());
    result = prime * result + (derived ? 1231 : 1237);
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.meta.AbstractAbbreviation#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    LabelSetJpa other = (LabelSetJpa) obj;
    if (description == null) {
      if (other.description != null)
        return false;
    } else if (!description.equals(other.description))
      return false;
    if (derived != other.derived)
      return false;
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.meta.AbstractAbbreviation#toString()
   */
  @Override
  public String toString() {
    return "LabelSetJpa [description=" + description + ", derived=" + derived
        + "]";
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.LabelSet#isDerived()
   */
  @Override
  public boolean isDerived() {
    return derived;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.LabelSet#setDerived(boolean)
   */
  @Override
  public void setDerived(boolean derived) {
    this.derived = derived;
  }

}
