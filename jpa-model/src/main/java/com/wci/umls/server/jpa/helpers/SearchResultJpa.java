/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.helpers.SearchResult;

/**
 * JPA enabled implementation of a {@link SearchResult}.
 */
@XmlRootElement(name = "searchResult")
public class SearchResultJpa implements SearchResult {

  /** The id. */
  private Long id;

  /** The terminology id. */
  private String terminologyId;

  /** The terminology. */
  private String terminology;

  /** The version. */
  private String version;

  /** The value. */
  private String value;

  /** The obsolete. */
  private boolean obsolete;

  /** The score. */
  private Long score;

  /**
   * Default constructor.
   */
  public SearchResultJpa() {
    // left empty
  }

  /**
   * Constructor.
   *
   * @param result the result
   */
  public SearchResultJpa(SearchResult result) {
    id = result.getId();
    terminology = result.getTerminology();
    terminologyId = result.getTerminologyId();
    version = result.getVersion();
    value = result.getValue();
    obsolete = result.isObsolete();
    score = result.getScore();
  }

  /**
   * Returns the id.
   *
   * @return the id
   */
  @Override
  @XmlElement
  public Long getId() {
    return this.id;
  }

  /**
   * Sets the id.
   *
   * @param id the id
   */
  @Override
  public void setId(Long id) {
    this.id = id;

  }

  /**
   * Returns the id.
   *
   * @return the id
   */
  @Override
  @XmlElement
  public String getTerminologyId() {
    return this.terminologyId;
  }

  /**
   * Sets the id.
   *
   * @param terminologyId the id
   */
  @Override
  public void setTerminologyId(String terminologyId) {
    this.terminologyId = terminologyId;

  }

  /* see superclass */
  @Override
  public String getTerminology() {
    return this.terminology;
  }

  /* see superclass */
  @Override
  public void setTerminology(String terminology) {
    this.terminology = terminology;
  }

  /* see superclass */
  @Override
  public String getVersion() {
    return this.version;
  }

  /* see superclass */
  @Override
  public void setVersion(String version) {
    this.version = version;
  }

  /* see superclass */
  @Override
  @XmlElement
  public String getValue() {
    return this.value;
  }

  /* see superclass */
  @Override
  public void setValue(String value) {
    this.value = value;
  }

  /* see superclass */
  @Override
  public boolean isObsolete() {
    return obsolete;
  }

  /* see superclass */
  @Override
  public void setObsolete(boolean obsolete) {
    this.obsolete = obsolete;
  }

  /* see superclass */
  @Override
  public float getScore() {
    return score;
  }

  /* see superclass */
  @Override
  public void setScore(float score) {
    this.score = score;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (obsolete ? 1231 : 1237);
    result = prime * result + Float.floatToIntBits(score);
    result =
        prime * result + ((terminology == null) ? 0 : terminology.hashCode());
    result = prime * result
        + ((terminologyId == null) ? 0 : terminologyId.hashCode());
    result = prime * result + ((value == null) ? 0 : value.hashCode());
    result = prime * result + ((version == null) ? 0 : version.hashCode());
    return result;
  }

  /* see superclass */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    SearchResultJpa other = (SearchResultJpa) obj;
    if (obsolete != other.obsolete)
      return false;
    if (Float.floatToIntBits(score) != Float.floatToIntBits(other.score))
      return false;
    if (terminology == null) {
      if (other.terminology != null)
        return false;
    } else if (!terminology.equals(other.terminology))
      return false;
    if (terminologyId == null) {
      if (other.terminologyId != null)
        return false;
    } else if (!terminologyId.equals(other.terminologyId))
      return false;
    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!value.equals(other.value))
      return false;
    if (version == null) {
      if (other.version != null)
        return false;
    } else if (!version.equals(other.version))
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "SearchResultJpa [id=" + id + ", terminologyId=" + terminologyId
        + ", terminology=" + terminology + ", version=" + version + ", value="
        + value + ", obsolete=" + obsolete + ", score=" + score + "]";
  }

}