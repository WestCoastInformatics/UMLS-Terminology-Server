/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers;

import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.helpers.PfsParameter;

/**
 * The JPA enabled implementation of the paging/filtering/sorting object.
 */
@XmlRootElement
public class PfsParameterJpa implements PfsParameter {

  /** The maximum number of results. */
  private int maxResults = -1;

  /** The start index for queries. */
  private int startIndex = -1;

  /** The filter string. */
  private String queryRestriction = null;

  /** The comparator for sorting. */
  private String sortField = null;

  /** The ascending flag. */
  private boolean ascending = true;

  /**
   * The default constructor.
   */
  public PfsParameterJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link PfsParameterJpa} from the specified parameters.
   *
   * @param pfs the pfs
   */
  public PfsParameterJpa(PfsParameter pfs) {
    maxResults = pfs.getMaxResults();
    startIndex = pfs.getStartIndex();
    queryRestriction = pfs.getQueryRestriction();
    sortField = pfs.getSortField();
    ascending = pfs.isAscending();
  }

  /**
   * Returns the maximum number of results.
   *
   * @return the maximum number of results
   */
  @Override
  public int getMaxResults() {
    return maxResults;
  }

  /**
   * Sets the maximum number of results.
   *
   * @param maxResults the maximum number of results
   */
  @Override
  public void setMaxResults(int maxResults) {
    this.maxResults = maxResults;
  }

  /**
   * Returns the starting index of a query result subset.
   *
   * @return the start index
   */
  @Override
  public int getStartIndex() {
    return startIndex;
  }

  /**
   * Sets the starting index of a query result subset.
   *
   * @param startIndex the start index
   */
  @Override
  public void setStartIndex(int startIndex) {
    this.startIndex = startIndex;
  }

  /**
   * Returns the filter string.
   *
   * @return the filter string
   */
  @Override
  public String getQueryRestriction() {
    return queryRestriction;
  }

  /**
   * Sets the filter string.
   *
   * @param queryRestriction the filter string
   */
  @Override
  public void setQueryRestriction(String queryRestriction) {
    this.queryRestriction = queryRestriction;
  }

  /**
   * Indicates whether or not sort is in ascending order
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  @Override
  public boolean isAscending() {
    return ascending;
  }

  /**
   * Sets the ascending.
   *
   * @param ascending the ascending
   */
  @Override
  public void setAscending(boolean ascending) {
    this.ascending = ascending;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (ascending ? 1231 : 1237);
    result = prime * result + maxResults;
    result =
        prime * result
            + ((queryRestriction == null) ? 0 : queryRestriction.hashCode());
    result = prime * result + ((sortField == null) ? 0 : sortField.hashCode());
    result = prime * result + startIndex;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    PfsParameterJpa other = (PfsParameterJpa) obj;
    if (ascending != other.ascending)
      return false;
    if (maxResults != other.maxResults)
      return false;
    if (queryRestriction == null) {
      if (other.queryRestriction != null)
        return false;
    } else if (!queryRestriction.equals(other.queryRestriction))
      return false;
    if (sortField == null) {
      if (other.sortField != null)
        return false;
    } else if (!sortField.equals(other.sortField))
      return false;
    if (startIndex != other.startIndex)
      return false;
    return true;
  }

  /**
   * Gets the sort field.
   * 
   * @return the sort field
   */
  @Override
  public String getSortField() {
    return sortField;
  }

  /**
   * Sets the sort field.
   *
   * @param sortField the sort field
   */
  @Override
  public void setSortField(String sortField) {
    this.sortField = sortField;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.mapping.helpers.PfsParameter#isIndexInRange(int)
   */
  @Override
  public boolean isIndexInRange(int i) {
    return getStartIndex() != -1 && getMaxResults() != -1
        && i >= getStartIndex() && i < (getStartIndex() + getMaxResults());
  }

  @Override
  public String toString() {
    return "PfsParameterJpa [maxResults=" + maxResults + ", startIndex="
        + startIndex + ", queryRestriction=" + queryRestriction
        + ", sortField=" + sortField + ", ascending=" + ascending + "]";
  }

}
