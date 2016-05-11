/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.helpers.KeyValuePair;
import com.wci.umls.server.helpers.SearchResult;
import com.wci.umls.server.model.meta.IdType;

/**
 * The Class SearchResultJpa.
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
  
  /** The type. */
  private IdType type;
  
  /** The property. */
  private KeyValuePair property;

  /** The value. */
  private String value;

  /** The obsolete. */
  private boolean obsolete;

  /** The score. */
  private Float score = null;

  /**
   * Instantiates a new search result jpa.
   */
  public SearchResultJpa() {
    // left empty
  }

  /**
   * Instantiates a new search result jpa.
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

  /* (non-Javadoc)
   * @see com.wci.umls.server.helpers.SearchResult#getId()
   */
  @Override
  @XmlElement
  public Long getId() {
    return this.id;
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.helpers.SearchResult#setId(java.lang.Long)
   */
  @Override
  public void setId(Long id) {
    this.id = id;

  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.helpers.SearchResult#getTerminologyId()
   */
  @Override
  @XmlElement
  public String getTerminologyId() {
    return this.terminologyId;
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.helpers.SearchResult#setTerminologyId(java.lang.String)
   */
  @Override
  public void setTerminologyId(String terminologyId) {
    this.terminologyId = terminologyId;

  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.helpers.SearchResult#getTerminology()
   */
  /* see superclass */
  @Override
  public String getTerminology() {
    return this.terminology;
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.helpers.SearchResult#setTerminology(java.lang.String)
   */
  /* see superclass */
  @Override
  public void setTerminology(String terminology) {
    this.terminology = terminology;
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.helpers.SearchResult#getVersion()
   */
  /* see superclass */
  @Override
  public String getVersion() {
    return this.version;
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.helpers.SearchResult#setVersion(java.lang.String)
   */
  /* see superclass */
  @Override
  public void setVersion(String version) {
    this.version = version;
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.helpers.SearchResult#getValue()
   */
  /* see superclass */
  @Override
  @XmlElement
  public String getValue() {
    return this.value;
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.helpers.SearchResult#setValue(java.lang.String)
   */
  /* see superclass */
  @Override
  public void setValue(String value) {
    this.value = value;
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.helpers.SearchResult#isObsolete()
   */
  /* see superclass */
  @Override
  public boolean isObsolete() {
    return obsolete;
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.helpers.SearchResult#setObsolete(boolean)
   */
  /* see superclass */
  @Override
  public void setObsolete(boolean obsolete) {
    this.obsolete = obsolete;
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.helpers.SearchResult#getScore()
   */
  /* see superclass */
  @Override
  public Float getScore() {
    return score;
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.helpers.SearchResult#setScore(java.lang.Float)
   */
  /* see superclass */
  @Override
  public void setScore(Float score) {
    this.score = score;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (obsolete ? 1231 : 1237);
    result = prime * result + Float.floatToIntBits(score);
    result =
        prime * result + ((terminology == null) ? 0 : terminology.hashCode());
    result =
        prime * result
            + ((terminologyId == null) ? 0 : terminologyId.hashCode());
    result = prime * result + ((value == null) ? 0 : value.hashCode());
    result = prime * result + ((version == null) ? 0 : version.hashCode());
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
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

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  /* see superclass */
  @Override
  public String toString() {
    return "SearchResultJpa [id=" + id + ", terminologyId=" + terminologyId
        + ", terminology=" + terminology + ", version=" + version + ", value="
        + value + ", obsolete=" + obsolete + ", score=" + score + "]";
  }

}