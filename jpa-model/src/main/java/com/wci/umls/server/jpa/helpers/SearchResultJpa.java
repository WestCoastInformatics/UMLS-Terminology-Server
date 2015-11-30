/**
 * Copyright 2015 West Coast Informatics, LLC
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

  /** The terminology version. */
  private String version;

  /** The value. */
  private String value;

  /** The obsolete. */
  private boolean obsolete;

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
  }

  /**
   * Returns the id.
   *
   * @return the id
   */
  @Override
  @XmlElement(name = "id")
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
  @XmlElement(name = "terminologyId")
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
  @XmlElement(name = "value")
  public String getValue() {
    return this.value;
  }

  @Override
  public void setValue(String value) {
    this.value = value;
  }

  /* see superclass */
  @Override
  public boolean isObsolete() {
    return obsolete;
  }

  @Override
  public void setObsolete(boolean obsolete) {
    this.obsolete = obsolete;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
        prime * result + ((terminology == null) ? 0 : terminology.hashCode());
    result =
        prime * result
            + ((terminologyId == null) ? 0 : terminologyId.hashCode());
    result = prime * result + ((version == null) ? 0 : version.hashCode());
    result = prime * result + ((value == null) ? 0 : value.hashCode());
    result = obsolete ? 13 : 17;
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
    if (version == null) {
      if (other.version != null)
        return false;
    } else if (!version.equals(other.version))
      return false;
    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!value.equals(other.value))
      return false;
    if (obsolete != other.obsolete)
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "SearchResultJpa [id=" + id + ", terminologyId=" + terminologyId
        + ", terminology=" + terminology + ", version=" + version + ", value="
        + value + ", obsolete=" + obsolete + "]";
  }
}
