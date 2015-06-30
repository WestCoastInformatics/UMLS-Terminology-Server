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
import com.wci.umls.server.model.meta.MarkerSet;

/**
 * JPA-enabled implementation of {@link AttributeName}.
 */
@Entity
@Table(name = "marker_sets", uniqueConstraints = @UniqueConstraint(columnNames = {
  "abbreviation"
}))
@Audited
@XmlRootElement(name = "markerSet")
public class MarkerSetJpa extends AbstractAbbreviation implements MarkerSet {

  /** The description. */
  @Column(nullable = false)
  private String description;

  /** The marker for flag. */
  @Column(nullable = false)
  private boolean markerFor;

  /**
   * Instantiates an empty {@link MarkerSetJpa}.
   */
  public MarkerSetJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link MarkerSetJpa} from the specified parameters.
   *
   * @param markerSet the atn
   */
  public MarkerSetJpa(MarkerSet markerSet) {
    super(markerSet);
    description = markerSet.getDescription();
    markerFor = markerSet.isMarkerFor();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.MarkerSet#getDescription()
   */
  @Override
  public String getDescription() {
    return description;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.MarkerSet#setDescription(java.lang.String)
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
    result = prime * result + (markerFor ? 1231 : 1237);
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
    MarkerSetJpa other = (MarkerSetJpa) obj;
    if (description == null) {
      if (other.description != null)
        return false;
    } else if (!description.equals(other.description))
      return false;
    if (markerFor != other.markerFor)
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
    return "MarkerSetJpa [description=" + description + ", markerFor="
        + markerFor + "]";
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.MarkerSet#isMarkerFor()
   */
  @Override
  public boolean isMarkerFor() {
    return markerFor;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.MarkerSet#setMarkerFor(boolean)
   */
  @Override
  public void setMarkerFor(boolean markerFor) {
    this.markerFor = markerFor;
  }

}
